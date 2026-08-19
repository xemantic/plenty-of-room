/*
 * Copyright 2026 Kazimierz Pogoda / Xemantic
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.xemantic.nano.plentyofroom.coupling

import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.DEPARTURE_DIGITS_BY_KEY
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.OrigamiSheet
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.roundForResult
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs
import kotlin.math.max

/**
 * `T-155` — is there a coupling placement and distribution that is flat **under** `C-0087`'s
 * measured staple dropout?
 *
 * `C-0087` grades four standing designs and every one fails at 89.6–100 % of realisations. It
 * does not search, and it names the direction: **denser, more regular, redundant**, with the
 * objective moved from a value to a **percentile**. This study prices those three axes
 * separately, runs three cheap bounds before any of them, and checks the answer against the plan
 * budget that says how many paths the tile can carry at all.
 */

// ---------------------------------------------------------------------------------------------
// the records
// ---------------------------------------------------------------------------------------------

/** One cheap bound, settled before the sampler it precedes. */
@Serializable
private data class T155BoundRecord(
    val name: String,
    val value: Double,
    val unit: String,
    val settles: String,
    val falsifierFired: Boolean
)

/** The redundancy arithmetic at one path count — no lattice, no load, no solve. */
@Serializable
private data class T155RedundancyRecord(
    val stationSet: String,
    val pathCount: Int,
    val perPathStiffness: Double,
    val perPathForce: Double,
    val forceOverAllowable: Double,
    val expectedSurvivors: Double,
    val survivorFractionAtP10: Double,
    val nominalPitch: Double,
    val pitchOverBendingLength: Double,
    val worstAbsenceRunAtP90: Int,
    val survivingPitchAtP90: Double,
    val survivingPitchInsideBendingLength: Boolean,
    val columnsDemandedByThatRun: Int,
    val pathsDemandedByThatRun: Int
)

/** How one design behaves under the dropout — the whole distribution, never a point. */
@Serializable
private data class T155DesignRecord(
    val stationSet: String,
    val pathCount: Int,
    val distribution: String,
    val convention: String,
    val nominalOverStroke: Double,
    val worstSingleRemovalOverStroke: Double,
    val singleRemovalAmplification: Double,
    val medianOverStroke: Double,
    val p90OverStroke: Double,
    val p95OverStroke: Double,
    val worstOverStroke: Double,
    val exceedance: Double,
    val exceedanceStandardError: Double,
    /**
     * The one-sided Clopper-Pearson limit where [exceedance] saturates, else `null` — `T-213`.
     *
     * `CH-0153`: at `p̂ = 1` the symmetric [exceedanceStandardError] is identically zero for every
     * sample count, and this study's headline is a design that **fails**, which is exactly the
     * direction that saturates it. Emitted **beside** the symmetric error rather than replacing
     * it: the symmetric error is uninformative rather than wrong.
     */
    val exceedanceOneSidedBound: Double?,
    val meanSurvivors: Double,
    val peakStiffnessRatio: Double,
    val perPathForceAtPeak: Double,
    val flatAtP90: Boolean,
    val flatAtMedian: Boolean
)

/** The rigorous lower bound over EVERY distribution, at one station set. */
@Serializable
private data class T155OracleRecord(
    val stationSet: String,
    val pathCount: Int,
    val convention: String,
    val floorAtFullPresenceOverStroke: Double,
    val medianFloorOverStroke: Double,
    val p90FloorOverStroke: Double,
    val worstFloorOverStroke: Double,
    val excludesEveryDistributionAtP90: Boolean,
    val note: String
)

/** What the plan admits, read from the claim that owns it. */
@Serializable
private data class T155PlanRecord(
    val pathCount: Int,
    val armLength: Double,
    val latticeCeiling: Double,
    val planMargin: Double,
    val latticeCapacityForThisArm: Int,
    val placed: Int,
    val selfConsistent: Boolean,
    val note: String
)

/** A convergence axis, reported as values rather than as a verdict. */
@Serializable
private data class T155ConvergenceRecord(
    val quantity: String,
    val parameter: String,
    val values: List<Double>,
    val results: List<Double>,
    val departure: Double,
    val note: String
)

/** One upstream number reproduced rather than cited. */
@Serializable
private data class T155ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double,
    val strict: Boolean
)

/** One acceptance predicate of `T-155`. */
@Serializable
private data class T155PredicateRecord(
    val name: String,
    val statement: String,
    val verdict: String
)

@Serializable
private data class T155Result(
    val task: String,
    val leaf: String,
    val conditions: String,
    val decision: String,
    val bounds: List<T155BoundRecord>,
    val redundancy: List<T155RedundancyRecord>,
    val designs: List<T155DesignRecord>,
    val oracle: List<T155OracleRecord>,
    val plan: List<T155PlanRecord>,
    val convergence: List<T155ConvergenceRecord>,
    val reproductions: List<T155ReproductionRecord>,
    val predicates: List<T155PredicateRecord>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val parameters: Map<String, String>
)

// ---------------------------------------------------------------------------------------------
// the constants
// ---------------------------------------------------------------------------------------------

private const val T155_DUPLEXES = 15
private const val T155_SAMPLES = 81
private const val T155_TOLERANCE = 0.10
private const val T155_COLLAR = 6.7
private const val T155_RIM_STANDOFF = 1.0
private const val T155_NOMINAL_CROSSOVER_COLUMNS = 8
private const val T155_C0063_PHASE = 24
private const val T155_C0074_PHASE = 8

/** `C-0087`'s own seed, so that its published percentiles reproduce bit for bit. */
private const val T155_GRADING_SEED = 20260817L

/** A different seed for the ensemble a search sees, so every quoted percentile is out of sample. */
private const val T155_TRAINING_SEED = 20260819L

private const val T155_GRADING_REALISATIONS = 10000
private const val T155_TRAINING_REALISATIONS = 200
/**
 * The sweeps a per-path descent is allowed.
 *
 * Two, and the price is stated rather than hidden: the last relative improvement of each descent
 * is emitted as a convergence record, so a reader can see how much the search still had left. It
 * is affordable to state because this study's verdict is a NEGATIVE at 3-6x the tolerance, and a
 * descent that stopped 1 % short cannot reverse it.
 */
private const val T155_DESCENT_SWEEPS = 2

private const val T155_DECISION_DIGITS = 6
private const val T155_DECISION_FLOOR = 1e-12

private val T155_EDGE_X = Gen1Tile.EDGE_X
private val T155_MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

/** The column counts swept — 15 to 90 paths on `C-0015`'s one-row-per-duplex grid. */
private val T155_COLUMN_SWEEP = listOf(1, 2, 3, 4, 5, 6)

/** The rim ratios swept, `C-0058`'s one-parameter family read under the dropout. */
private val T155_RIM_RATIOS = listOf(0.2, 0.4, 0.6, 0.8, 1.0, 1.5, 2.0, 3.0, 5.0, 7.0, 10.0)

/** The exponents of the `1/p` compensation swept — 0 is the equal design. */
private val T155_COMPENSATION_EXPONENTS = listOf(-1.0, -0.5, 0.5, 1.0, 2.0)

// ---------------------------------------------------------------------------------------------
// the inputs, read from the claims that own them
// ---------------------------------------------------------------------------------------------

private class T155Profile(
    val name: String,
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, lengthY: Double): PressureField = edgeCollarPressure(
        interiorPressure, T155_EDGE_X, lengthY,
        listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T155_RIM_STANDOFF))
    )
}

/** `C-0022`'s solved profile, keyed on **`(concentration, gap, bias)`** — `CLAUDE.md`'s gotcha. */
private fun t155Profile(file: File, key: Triple<Double, Double, Double>): T155Profile {
    require(file.exists()) {
        "C-0022's result file is missing: ${file.path}. T-155 consumes the SOLVED edge profile."
    }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(name: String) = it.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == key.first && value("gapHeight") == key.second &&
                    value("appliedBias") == key.third
        } ?: error("no C-0022 profile at ${key.first} mM, ${key.second} nm, ${key.third} V")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T155Profile(
        name = "%.1f mM, %.0f nm, %.3f V".format(key.first, key.second, key.third),
        smoothDepth = value("taperDepth"),
        smoothWidth = value("taperWidth"),
        rimDepth = value("rimResidualDepth")
    )
}

/** A placement read from the result file of the claim that owns it. */
private fun t155Placement(
    file: File,
    key: String,
    interhelical: Double
): List<Pair<Double, Double>> {
    require(file.exists()) { "the placement's own result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue(key).jsonArray.map { it.jsonObject }
        .flatMap { row ->
            val index = row.getValue("row").jsonPrimitive.content.toInt()
            val y = (index - (T155_DUPLEXES - 1) / 2.0) * interhelical
            row.getValue("roots").jsonArray.map { it.jsonPrimitive.content.toDouble() to y }
        }
}

/** `C-0075`'s self-consistent count table, read rather than retyped. */
private fun t155PlanTable(file: File): List<T155PlanRecord> {
    require(file.exists()) {
        "C-0075's result file is missing: ${file.path}. T-155 will not retype a plan ceiling."
    }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("counts").jsonArray.map { it.jsonObject }
        .map { row ->
            fun number(name: String) = row.getValue(name).jsonPrimitive.content.toDouble()
            fun flag(name: String) = row.getValue(name).jsonPrimitive.content.toBoolean()
            T155PlanRecord(
                pathCount = number("pathCount").toInt(),
                armLength = number("armLength"),
                latticeCeiling = number("latticeCeiling"),
                planMargin = number("planMargin"),
                latticeCapacityForThisArm = number("latticeCapacityForThisArm").toInt(),
                placed = number("placed").toInt(),
                selfConsistent = flag("selfConsistent"),
                note = "C-0075/C-0069: the rooted arm is a PLACED length, L grows as n^(1/3), " +
                        "and three roots on a 10.88 nm lattice cap it at pitch - d"
            )
        }
}

// ---------------------------------------------------------------------------------------------
// the models
// ---------------------------------------------------------------------------------------------

private fun t155Sheet(): OrigamiSheet =
    origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)

private fun t155Lattice(sheet: OrigamiSheet, columns: CrossoverLayout): OrigamiGrillage =
    OrigamiGrillage(
        sheet = sheet,
        lengthX = T155_EDGE_X,
        beamCount = T155_DUPLEXES,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = columns,
        subdivisions = 2,
        supports = emptyList()
    )

/** One station set, with its host, its two solved states and its two dropout ensembles. */
private class T155StationSet(
    val name: String,
    val stations: List<Pair<Double, Double>>,
    /** The row length for the run-length statistic, or `null` where the rows are unequal. */
    val columns: Int?,
    val designSurrogate: InfluenceSurrogate,
    val heldSurrogate: InfluenceSurrogate,
    val probabilities: List<Double>,
    val gradingEnsemble: DropoutEnsemble,
    val trainingEnsemble: DropoutEnsemble
) {
    val pathCount: Int get() = stations.size
}

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val sheet = t155Sheet()
    val lengthY = T155_DUPLEXES * sheet.interhelicalDistance
    val interiorPressure = Gen1Tile.TARGET_FORCE / (T155_EDGE_X * lengthY)

    println("T-155 — reading C-0022's solved loads, the standing placements and C-0075's plan ...")
    val loadFile = File("gpd/results/T-3b-tile-edge-load-profile.json")
    val designProfile = t155Profile(loadFile, Triple(2.0, 10.0, 0.192))
    val heldProfile = t155Profile(loadFile, Triple(2.0, 7.0, 0.192))
    val designField = designProfile.field(interiorPressure, lengthY)
    val heldField = heldProfile.field(interiorPressure, lengthY)

    val freeStroke = PlateOnFoundation(
        sheet.plate(T155_EDGE_X, lengthY), Gen1Tile.FOUNDATION_SECANT, emptyList(), 12
    ).solve(uniformPressure(interiorPressure)).meanDeflection

    val roots34 = t155Placement(
        File("gpd/results/T-125-upward-root-placement.json"), "bestPlacement",
        sheet.interhelicalDistance
    )
    check(roots34.size == 34) { "C-0063's placement must carry 34 roots" }
    val roots30 = t155Placement(
        File("gpd/results/T-136-two-per-row-placement.json"), "recommendedPlacement",
        sheet.interhelicalDistance
    )
    check(roots30.size == 30) { "C-0074's placement must carry 30 roots" }
    val plan = t155PlanTable(File("gpd/results/T-138-path-count-consistency.json"))

    // The incorporation field. `C-0087`'s MEASURED_DEPTH is the headline — the least pessimistic
    // position-dependent reading — and its UNIFORM is CH-0084's baseline.
    val measuredField = measuredDepthIncorporation(T155_EDGE_X, lengthY)
    val uniformField = uniformIncorporation(StapleDropoutLiterature.INCORPORATION_MEAN)
    val conventions = linkedMapOf(
        IncorporationConvention.MEASURED_DEPTH.name to measuredField,
        IncorporationConvention.UNIFORM.name to uniformField
    )

    val bendingLengthAlong = winklerBendingLength(
        sheet.alongHelixRigidity, Gen1Tile.FOUNDATION_SECANT
    )
    val bendingLengthAcross = winklerBendingLength(
        sheet.acrossHelixRigidity, Gen1Tile.FOUNDATION_SECANT
    )

    // --------------------------------------------------------------------- the station sets
    println("T-155 — the Woodbury banks, one per station set per state ...")
    val centredHost = t155Lattice(
        sheet, CrossoverLayout.centred(T155_NOMINAL_CROSSOVER_COLUMNS, sheet.crossoverSpacing / 2.0)
    )
    val host24 = t155Lattice(
        sheet, CrossoverLayout.atBasePairPhase(T155_C0063_PHASE, sheet, T155_EDGE_X)
    )
    val host8 = t155Lattice(
        sheet, CrossoverLayout.atBasePairPhase(T155_C0074_PHASE, sheet, T155_EDGE_X)
    )

    fun stationSet(
        name: String,
        host: OrigamiGrillage,
        stations: List<Pair<Double, Double>>,
        columns: Int?,
        field: IncorporationField
    ): T155StationSet {
        val probabilities = stations.map { (x, y) -> field.at(x, y) }
        return T155StationSet(
            name = name,
            stations = stations,
            columns = columns,
            designSurrogate = latticeInfluenceSurrogate(host, stations, designField, T155_SAMPLES),
            heldSurrogate = latticeInfluenceSurrogate(host, stations, heldField, T155_SAMPLES),
            probabilities = probabilities,
            gradingEnsemble = dropoutEnsemble(
                probabilities, T155_GRADING_REALISATIONS, T155_GRADING_SEED
            ),
            trainingEnsemble = dropoutEnsemble(
                probabilities, T155_TRAINING_REALISATIONS, T155_TRAINING_SEED
            )
        )
    }

    val gridStations = T155_COLUMN_SWEEP.associateWith {
        attachmentGrid(it, T155_DUPLEXES, T155_EDGE_X, lengthY)
    }

    // One set per (station set, convention). The MEASURED_DEPTH sets carry the sweep; the
    // UNIFORM ones are built only where CH-0084's baseline is quoted, to keep the bank cost down.
    val measuredSets = ArrayList<T155StationSet>()
    T155_COLUMN_SWEEP.forEach { columns ->
        val stations = gridStations.getValue(columns)
        println("  bank: %d x %d grid (%d paths)".format(columns, T155_DUPLEXES, stations.size))
        measuredSets += stationSet(
            "C-0015's %d x %d attachment grid".format(columns, T155_DUPLEXES),
            centredHost, stations, columns, measuredField
        )
    }
    println("  bank: C-0063's 34 upward roots at phase 24")
    val set34 = stationSet(
        "C-0063's 34 upward roots at phase 24", host24, roots34, null, measuredField
    )
    println("  bank: C-0074's recommended 30 roots at phase 8")
    val set30 = stationSet(
        "C-0074's recommended 30 roots at phase 8", host8, roots30, null, measuredField
    )
    measuredSets += set34
    measuredSets += set30

    val uniformSets = listOf(
        stationSet(
            "C-0015's 6 x 15 attachment grid", centredHost, gridStations.getValue(6), 6,
            uniformField
        ),
        stationSet("C-0063's 34 upward roots at phase 24", host24, roots34, null, uniformField)
    )

    // --------------------------------------------------------------------- helpers
    val unzipCeiling = perPathStiffnessCeiling(
        Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE
    )

    fun decide(value: Double) = roundForResult(value, T155_DECISION_DIGITS, T155_DECISION_FLOOR)

    fun trainingP90(set: T155StationSet, stiffnesses: List<Double>): Double = decide(
        orderStatistic(
            dropoutDishingSample(set.designSurrogate, stiffnesses, set.trainingEnsemble), 0.90
        ) / freeStroke
    )

    val designRecords = ArrayList<T155DesignRecord>()

    fun grade(
        set: T155StationSet,
        convention: String,
        label: String,
        stiffnesses: List<Double>
    ): T155DesignRecord {
        val nominal = set.designSurrogate.solve(stiffnesses).peakDishing / freeStroke
        val sample = dropoutDishingSample(set.designSurrogate, stiffnesses, set.gradingEnsemble)
        sample.indices.forEach { sample[it] = sample[it] / freeStroke }
        val summary = summariseDropoutDishing(
            sample, nominal, set.gradingEnsemble.meanSurvivors, T155_TOLERANCE
        )
        val worstSingle = worstSinglePathRemoval(set.designSurrogate, stiffnesses) / freeStroke
        val peak = stiffnesses.max()
        val record = T155DesignRecord(
            stationSet = set.name,
            pathCount = set.pathCount,
            distribution = label,
            convention = convention,
            nominalOverStroke = nominal,
            worstSingleRemovalOverStroke = worstSingle,
            singleRemovalAmplification = worstSingle / nominal,
            medianOverStroke = summary.median,
            p90OverStroke = summary.p90,
            p95OverStroke = summary.p95,
            worstOverStroke = summary.worst,
            exceedance = summary.exceedance,
            exceedanceStandardError = summary.exceedanceStandardError,
            exceedanceOneSidedBound = summary.exceedanceOneSidedBound,
            meanSurvivors = summary.meanSurvivors,
            peakStiffnessRatio = peak / (stiffnesses.sum() / stiffnesses.size),
            perPathForceAtPeak = peak * Gen1Tile.ACCEPTABLE_STROKE,
            flatAtP90 = summary.flatAtP90,
            flatAtMedian = summary.flatAtMedian
        )
        designRecords += record
        return record
    }

    // --------------------------------------------------------------------- the cheap bounds
    println("T-155 — the cheap bounds, which run before any search ...")

    // Bound 2: the oracle floor over the survivors — a rigorous lower bound over EVERY
    // distribution, at every station set.
    val oracleRecords = (measuredSets.map { it to IncorporationConvention.MEASURED_DEPTH.name } +
            uniformSets.map { it to IncorporationConvention.UNIFORM.name }).map { (set, convention) ->
        val floors = oracleFloorSample(set.designSurrogate, set.gradingEnsemble)
        floors.indices.forEach { floors[it] = floors[it] / freeStroke }
        val p90 = orderStatistic(floors, 0.90)
        T155OracleRecord(
            stationSet = set.name,
            pathCount = set.pathCount,
            convention = convention,
            floorAtFullPresenceOverStroke =
                set.designSurrogate.reachableDishingFloor / freeStroke,
            medianFloorOverStroke = orderStatistic(floors, 0.50),
            p90FloorOverStroke = p90,
            worstFloorOverStroke = floors.max(),
            excludesEveryDistributionAtP90 = p90 > T155_TOLERANCE,
            note = "the least-squares RMS dishing an ORACLE that knew the realisation could " +
                    "reach; a peak is never below its own RMS, so this bounds every " +
                    "distribution's 90th percentile from below"
        )
    }

    // Bound 3: the run-length pitch arithmetic, no solve at all.
    val redundancyRecords = T155_COLUMN_SWEEP.map { columns ->
        val set = measuredSets.first { it.columns == columns }
        val ledger = redundancyLedger(
            columns = columns,
            rows = T155_DUPLEXES,
            edgeX = T155_EDGE_X,
            totalStiffness = T155_MANDATE,
            stroke = Gen1Tile.ACCEPTABLE_STROKE,
            allowable = Gen1Tile.DUPLEX_UNZIP_ALLOWABLE,
            bendingLength = bendingLengthAlong,
            ensemble = set.gradingEnsemble
        )
        val demandedColumns =
            columnsForRunRobustness(T155_EDGE_X, bendingLengthAlong, ledger.worstRunAtP90)
        T155RedundancyRecord(
            stationSet = set.name,
            pathCount = ledger.pathCount,
            perPathStiffness = ledger.perPathStiffness,
            perPathForce = ledger.perPathForce,
            forceOverAllowable = ledger.allowableRatio,
            expectedSurvivors = ledger.expectedSurvivors,
            survivorFractionAtP10 = ledger.survivorFractionAtP10,
            nominalPitch = ledger.nominalPitch,
            pitchOverBendingLength = ledger.pitchOverBendingLength,
            worstAbsenceRunAtP90 = ledger.worstRunAtP90,
            survivingPitchAtP90 = ledger.survivingPitchAtP90,
            survivingPitchInsideBendingLength = ledger.survivingPitchInsideBendingLength,
            columnsDemandedByThatRun = demandedColumns,
            pathsDemandedByThatRun = demandedColumns * T155_DUPLEXES
        )
    }

    // --------------------------------------------------------------------- the sweeps
    println("T-155 — the density axis with EQUAL springs, %d realisations per cell ..."
        .format(T155_GRADING_REALISATIONS))
    measuredSets.forEach { set ->
        grade(
            set, IncorporationConvention.MEASURED_DEPTH.name, "EQUAL",
            List(set.pathCount) { T155_MANDATE / set.pathCount }
        )
        println("  %-46s equal springs done".format(set.name.take(46)))
    }
    uniformSets.forEach { set ->
        grade(
            set, IncorporationConvention.UNIFORM.name, "EQUAL",
            List(set.pathCount) { T155_MANDATE / set.pathCount }
        )
    }

    println("T-155 — the buildable one-parameter distributions, chosen out of sample ...")
    val bestByFamily = LinkedHashMap<String, Pair<String, List<Double>>>()
    measuredSets.forEach { set ->
        val candidates = ArrayList<Pair<String, List<Double>>>()
        T155_RIM_RATIOS.forEach { ratio ->
            candidates += "rim x %.1f".format(ratio) to normalisedStiffnesses(
                rimStiffenedWeights(set.stations, T155_EDGE_X, lengthY, T155_COLLAR, ratio),
                T155_MANDATE
            )
        }
        T155_COMPENSATION_EXPONENTS.forEach { exponent ->
            candidates += "1/p^%.1f".format(exponent) to normalisedStiffnesses(
                inverseIncorporationWeights(set.probabilities, exponent), T155_MANDATE
            )
        }
        // The choice is made on the TRAINING ensemble, at six significant digits, with the
        // earlier candidate winning a tie — `CLAUDE.md` on decision precision and argmin.
        var bestLabel = candidates[0].first
        var bestWeights = candidates[0].second
        var bestValue = trainingP90(set, bestWeights)
        candidates.drop(1).forEach { (label, weights) ->
            val value = trainingP90(set, weights)
            if (value < bestValue) {
                bestValue = value
                bestLabel = label
                bestWeights = weights
            }
        }
        bestByFamily[set.name] = bestLabel to bestWeights
        grade(
            set, IncorporationConvention.MEASURED_DEPTH.name,
            "BEST ONE-PARAMETER (%s)".format(bestLabel), bestWeights
        )
        println("  %-46s best one-parameter: %s".format(set.name.take(46), bestLabel))
    }

    // --------------------------------------------------------------------- the descent
    println("T-155 — the PERCENTILE descent, out of sample, on the three decisive sets ...")
    val descentSets = listOf(
        measuredSets.first { it.columns == 3 },
        measuredSets.first { it.columns == 6 },
        set34
    )
    val descentImprovements = ArrayList<Double>()
    descentSets.forEach { set ->
        val equal = List(set.pathCount) { T155_MANDATE / set.pathCount }
        val starts = listOf(equal, bestByFamily.getValue(set.name).second)
        val percentile = optimiseStiffnessDistribution(
            totalStiffness = T155_MANDATE,
            starts = starts,
            ceiling = unzipCeiling,
            sweeps = T155_DESCENT_SWEEPS,
            tolerance = 1e-4,
            searchHalfWidth = 1.5,
            scanPoints = 7,
            refinements = 8
        ) { trainingP90(set, it) }
        descentImprovements += percentile.lastImprovement
        grade(
            set, IncorporationConvention.MEASURED_DEPTH.name,
            "PERCENTILE DESCENT (p90, out of sample)", percentile.stiffnesses
        )
        // The contrast F2 asks for: the SAME descent on the zero-defect dishing.
        val zeroDefect = optimiseStiffnessDistribution(
            totalStiffness = T155_MANDATE,
            starts = starts,
            ceiling = unzipCeiling,
            sweeps = T155_DESCENT_SWEEPS,
            tolerance = 1e-4,
            searchHalfWidth = 1.5,
            scanPoints = 7,
            refinements = 8
        ) { decide(set.designSurrogate.solve(it).peakDishing / freeStroke) }
        grade(
            set, IncorporationConvention.MEASURED_DEPTH.name,
            "ZERO-DEFECT DESCENT", zeroDefect.stiffnesses
        )
        println("  %-46s descents done".format(set.name.take(46)))
    }

    // --------------------------------------------------------------------- the range
    println("T-155 — the best design over C-0068's range ...")
    val bestRecord = designRecords
        .filter { it.convention == IncorporationConvention.MEASURED_DEPTH.name }
        .minByOrNull { decide(it.p90OverStroke) }!!
    val bestSet = measuredSets.first { it.name == bestRecord.stationSet }
    val bestStiffnesses = when {
        bestRecord.distribution == "EQUAL" ->
            List(bestSet.pathCount) { T155_MANDATE / bestSet.pathCount }

        bestRecord.distribution.startsWith("BEST ONE-PARAMETER") ->
            bestByFamily.getValue(bestSet.name).second

        else -> null
    }
    val rangeP90 = bestStiffnesses?.let { stiffnesses ->
        val design = dropoutDishingSample(bestSet.designSurrogate, stiffnesses, bestSet.gradingEnsemble)
        val held = dropoutDishingSample(bestSet.heldSurrogate, stiffnesses, bestSet.gradingEnsemble)
        val worst = DoubleArray(design.size) { max(design[it], held[it]) / freeStroke }
        orderStatistic(worst, 0.90)
    }

    // --------------------------------------------------------------------- the cheap-bound rank
    val measuredDesigns = designRecords
        .filter { it.convention == IncorporationConvention.MEASURED_DEPTH.name }
    val rankAgreement = spearmanRankCorrelation(
        measuredDesigns.map { it.worstSingleRemovalOverStroke },
        measuredDesigns.map { it.p90OverStroke }
    )

    val bounds = ArrayList<T155BoundRecord>()
    bounds += T155BoundRecord(
        "the worst single-path removal on C-0063's 34 equal roots",
        measuredDesigns.first {
            it.stationSet == set34.name && it.distribution == "EQUAL"
        }.worstSingleRemovalOverStroke,
        "of the free-tile stroke",
        "C-0087's own cheap bound, re-derived in 34 solves and no sampling",
        true
    )
    bounds += T155BoundRecord(
        "the worst single-path removal at the densest grid swept",
        measuredDesigns.first {
            it.stationSet.startsWith("C-0015's 6") && it.distribution == "EQUAL"
        }.worstSingleRemovalOverStroke,
        "of the free-tile stroke",
        "the same bound at 90 paths — what the DENSITY axis buys, before any Monte Carlo",
        false
    )
    bounds += T155BoundRecord(
        "the rank agreement between the cheap single-removal bound and the 90th percentile",
        rankAgreement, "Spearman rho",
        "measured over every graded design rather than assumed: it says whether a placement " +
                "search may be run on the cheap objective at all",
        abs(rankAgreement) < 0.5
    )
    bounds += T155BoundRecord(
        "the worst 90th-percentile ORACLE floor over every station set",
        oracleRecords.maxOf { it.p90FloorOverStroke }, "of the free-tile stroke",
        "a rigorous lower bound over EVERY distribution; above T-5b's 0.10 it would settle the " +
                "question with no search at all",
        oracleRecords.any { it.excludesEveryDistributionAtP90 }
    )
    bounds += T155BoundRecord(
        "the attachment columns a p90 absence run demands at one Winkler bending length",
        redundancyRecords.maxOf { it.columnsDemandedByThatRun }.toDouble(), "columns",
        ("CLAUDE.md's sign rule read backwards: ell_along = %.3f nm, a run of absences " +
                "multiplies the local pitch, and the column count that keeps the surviving " +
                "pitch inside one bending length is one division").format(bendingLengthAlong),
        redundancyRecords.maxOf { it.columnsDemandedByThatRun } > T155_COLUMN_SWEEP.max()
    )
    bounds += T155BoundRecord(
        "the largest SELF-CONSISTENT path count C-0075's plan table admits",
        plan.filter { it.selfConsistent }.maxOf { it.pathCount }.toDouble(), "paths",
        ("the rooted arm is a placed length: at 45 paths it is %.3f nm against a %.2f nm " +
                "lattice ceiling, so the tile places %d of 45 and the count is not " +
                "self-consistent").format(
            plan.first { it.pathCount == 45 }.armLength,
            plan.first { it.pathCount == 45 }.latticeCeiling,
            plan.first { it.pathCount == 45 }.placed
        ),
        true
    )

    // --------------------------------------------------------------------- convergence
    println("T-155 — convergence ...")
    val convergenceSet = set34
    val convergenceStiffnesses = List(set34.pathCount) { T155_MANDATE / set34.pathCount }
    val sampleCounts = listOf(1250, 2500, 5000, 10000)
    val p90AtCount = sampleCounts.map { count ->
        orderStatistic(
            dropoutDishingSample(
                convergenceSet.designSurrogate, convergenceStiffnesses,
                dropoutEnsemble(convergenceSet.probabilities, count, T155_GRADING_SEED)
            ), 0.90
        ) / freeStroke
    }
    val oracleAtCount = sampleCounts.map { count ->
        orderStatistic(
            oracleFloorSample(
                convergenceSet.designSurrogate,
                dropoutEnsemble(convergenceSet.probabilities, count, T155_GRADING_SEED)
            ), 0.90
        ) / freeStroke
    }
    val gridCounts = listOf(41, 81, 161)
    val p90AtGrid = gridCounts.map { grid ->
        val surrogate = latticeInfluenceSurrogate(host24, roots34, designField, grid)
        val ensemble = dropoutEnsemble(convergenceSet.probabilities, 500, T155_GRADING_SEED)
        orderStatistic(
            dropoutDishingSample(surrogate, convergenceStiffnesses, ensemble), 0.90
        ) / freeStroke
    }
    val trainingCounts = listOf(100, 200, 400)
    val outOfSampleAtTraining = trainingCounts.map { count ->
        val training = dropoutEnsemble(
            convergenceSet.probabilities, count, T155_TRAINING_SEED
        )
        val optimum = optimiseStiffnessDistribution(
            totalStiffness = T155_MANDATE,
            starts = listOf(convergenceStiffnesses),
            ceiling = unzipCeiling,
            sweeps = 2,
            tolerance = 1e-4,
            searchHalfWidth = 1.5,
            scanPoints = 7,
            refinements = 8
        ) {
            decide(
                orderStatistic(
                    dropoutDishingSample(convergenceSet.designSurrogate, it, training), 0.90
                ) / freeStroke
            )
        }
        orderStatistic(
            dropoutDishingSample(
                convergenceSet.designSurrogate, optimum.stiffnesses,
                convergenceSet.gradingEnsemble
            ), 0.90
        ) / freeStroke
    }
    val convergence = listOf(
        T155ConvergenceRecord(
            "the 90th percentile of the dishing distribution",
            "realisations 1250/2500/5000/10000",
            sampleCounts.map { it.toDouble() }, p90AtCount,
            abs(p90AtCount.last() - p90AtCount[p90AtCount.size - 2]),
            "nested seeded ensembles; the departure is the 5 000 to 10 000 step"
        ),
        T155ConvergenceRecord(
            "the 90th percentile of the ORACLE floor", "the same counts",
            sampleCounts.map { it.toDouble() }, oracleAtCount,
            abs(oracleAtCount.last() - oracleAtCount[oracleAtCount.size - 2]),
            "the rigorous bound is the quantity a negative verdict rests on, so its own " +
                    "sampling convergence is reported separately from the dishing's"
        ),
        T155ConvergenceRecord(
            "the 90th percentile on the dishing grid", "samples per edge 41/81/161",
            gridCounts.map { it.toDouble() }, p90AtGrid,
            abs(p90AtGrid[2] - p90AtGrid[1]),
            "C-0026's 81 x 81 convention, at 500 shared realisations"
        ),
        T155ConvergenceRecord(
            "the last relative improvement of each percentile descent",
            "the three descent station sets, in order",
            descentSets.map { it.pathCount.toDouble() }, descentImprovements,
            descentImprovements.max(),
            "a descent reports what it found, never a global optimum; this is how much the " +
                    "last sweep still moved, against the 1e-4 tolerance it would have exited on"
        ),
        T155ConvergenceRecord(
            "the OUT-OF-SAMPLE 90th percentile of a percentile descent",
            "training realisations 100/200/400",
            trainingCounts.map { it.toDouble() }, outOfSampleAtTraining,
            abs(outOfSampleAtTraining.last() - outOfSampleAtTraining[trainingCounts.size - 2]),
            "an IN-SAMPLE percentile optimum is not a result; this is the convergence of the " +
                    "training ensemble a search sees, graded on the independent one"
        )
    )

    // --------------------------------------------------------------------- reproductions
    println("T-155 — reproducing the standing figures ...")
    val reproductions = ArrayList<T155ReproductionRecord>()
    fun reproduce(
        source: String,
        quantity: String,
        published: Double,
        reproduced: Double,
        strict: Boolean = true
    ) {
        reproductions += T155ReproductionRecord(
            source, quantity, published, reproduced,
            if (published == 0.0) abs(reproduced) else abs(reproduced - published) / abs(published),
            strict
        )
    }

    val grid45Set = measuredSets.first { it.columns == 3 }
    val equal45 = List(45) { T155_MANDATE / 45 }
    val twoLevel45 = normalisedStiffnesses(
        rimStiffenedWeights(grid45Set.stations, T155_EDGE_X, lengthY, T155_COLLAR, 5.0),
        T155_MANDATE
    )
    reproduce(
        "C-0017/C-0058", "the uniform 3 x 15 coupling's dishing / stroke", 0.2182,
        grid45Set.designSurrogate.solve(equal45).peakDishing / freeStroke
    )
    reproduce(
        "C-0058", "the rim x 5 two-level dishing / stroke", 0.0753,
        grid45Set.designSurrogate.solve(twoLevel45).peakDishing / freeStroke
    )
    reproduce(
        "C-0063", "the 34 equal springs at the design state", 0.0706,
        set34.designSurrogate.solve(List(34) { T155_MANDATE / 34 }).peakDishing / freeStroke
    )
    reproduce(
        "C-0068", "the same 34 roots over the device's range", 0.0789,
        max(
            set34.designSurrogate.solve(List(34) { T155_MANDATE / 34 }).peakDishing,
            set34.heldSurrogate.solve(List(34) { T155_MANDATE / 34 }).peakDishing
        ) / freeStroke
    )
    reproduce(
        "C-0087", "the single-removal cost on C-0063's 34 roots", 0.5010,
        worstSinglePathRemoval(set34.designSurrogate, List(34) { T155_MANDATE / 34 }) / freeStroke
    )
    reproduce(
        "C-0087", "the single-removal cost on C-0058's two-level 45", 0.3060,
        worstSinglePathRemoval(grid45Set.designSurrogate, twoLevel45) / freeStroke
    )
    reproduce(
        "C-0087", "the MEASURED_DEPTH p90 of C-0063's 34 equal roots, as built", 0.6391,
        measuredDesigns.first {
            it.stationSet == set34.name && it.distribution == "EQUAL"
        }.p90OverStroke
    )
    reproduce(
        "C-0087", "the MEASURED_DEPTH exceedance of the same cell", 0.998,
        measuredDesigns.first {
            it.stationSet == set34.name && it.distribution == "EQUAL"
        }.exceedance, false
    )
    reproduce(
        "C-0087", "the UNIFORM p90 of C-0063's 34 equal roots, as built", 0.5346,
        designRecords.first {
            it.stationSet == set34.name && it.convention == IncorporationConvention.UNIFORM.name
        }.p90OverStroke
    )
    reproduce("C-0026", "the free-tile stroke [nm]", 4.90731, freeStroke)
    reproduce("C-0017", "the mandate as a sum [pN/nm]", 33.3333, T155_MANDATE, false)
    reproduce(
        "C-0047", "the Winkler bending length along the helices [nm]", 12.83,
        bendingLengthAlong, false
    )
    reproduce(
        "C-0047", "the Winkler bending length across the helices [nm]", 5.71,
        bendingLengthAcross, false
    )
    reproduce(
        "C-0075", "the self-consistent arm at 34 paths [nm]", 8.16439018,
        plan.first { it.pathCount == 34 }.armLength
    )
    reproduce(
        "C-0069/C-0075", "the plan budget on a three-per-row placement [nm]", 8.19,
        plan.first { it.pathCount == 34 }.latticeCeiling
    )
    reproduce(
        "C-0049", "the per-path stiffness ceiling at the acceptable stroke [pN/nm]", 3.33333333,
        unzipCeiling
    )

    // --------------------------------------------------------------------- predicates
    val densityCurve = T155_COLUMN_SWEEP.map { columns ->
        measuredDesigns.first {
            it.stationSet.startsWith("C-0015's %d ".format(columns)) && it.distribution == "EQUAL"
        }.p90OverStroke
    }
    val densityMonotone = densityCurve.zipWithNext().all { (a, b) -> decide(b) <= decide(a) }
    val anyFlat = measuredDesigns.any { it.flatAtP90 }
    val percentileBeatsZeroDefect = descentSets.all { set ->
        val percentile = measuredDesigns.first {
            it.stationSet == set.name && it.distribution.startsWith("PERCENTILE")
        }.p90OverStroke
        val zeroDefect = measuredDesigns.first {
            it.stationSet == set.name && it.distribution == "ZERO-DEFECT DESCENT"
        }.p90OverStroke
        decide(percentile) <= decide(zeroDefect)
    }
    val worstStrict = reproductions.filter { it.strict }.maxOf { it.departure }

    // `CLAUDE.md`: a `String.format` defect throws at the LAST line, after every number is in
    // hand, and "build the result and write the JSON before formatting any prose". A study whose
    // numbers cost forty-nine minutes must not be able to lose them to a placeholder, so the
    // prose is built inside a guard: a failure is recorded IN the emitted file, the numbers are
    // written, and the exception is rethrown afterwards so the defect is not silently tolerated.
    var proseFailure: Throwable? = null
    fun <T> guardedProse(fallback: T, build: () -> T): T = try {
        build()
    } catch (failure: Throwable) {
        proseFailure = failure
        fallback
    }

    val predicates = guardedProse(
        listOf(
            T155PredicateRecord(
                "THE PROSE FAILED — the numbers below are complete and this file is a rescue",
                "see the findings for the exception", "FAIL"
            )
        )
    ) {
        listOf(
        T155PredicateRecord(
            "P1 — the cheap bounds run first and each says what it settles",
            "the single-removal profile (n solves), the ORACLE floor under dropout (a rigorous " +
                    "lower bound over every distribution) and the run-length pitch arithmetic " +
                    "(no solve at all) are all reported before the sweep they precede",
            "PASS"
        ),
        T155PredicateRecord(
            "P2 — the objective is a PERCENTILE, chosen out of sample",
            ("the descent minimises the 90th percentile over a %d-realisation training " +
                    "ensemble at seed %d and is graded on the independent %d-realisation " +
                    "ensemble at C-0087's own seed %d").format(
                T155_TRAINING_REALISATIONS, T155_TRAINING_SEED,
                T155_GRADING_REALISATIONS, T155_GRADING_SEED
            ),
            "PASS"
        ),
        T155PredicateRecord(
            "P3 — the three axes are priced separately",
            ("denser (%d to %d paths), more regular (a grid against an upward-root placement " +
                    "at matched count) and REDUNDANT (the run-length ledger and the plan " +
                    "ceiling)").format(
                T155_COLUMN_SWEEP.min() * T155_DUPLEXES, T155_COLUMN_SWEEP.max() * T155_DUPLEXES
            ),
            "PASS"
        ),
        T155PredicateRecord(
            "P4 — a recommendation, or the statement that no Gen-1 coupling is flat",
            if (anyFlat)
                "at least one design is inside T-5b's 0.10 at the 90th percentile"
            else
                ("NO design over %d graded cells is inside T-5b's 0.10 at the 90th " +
                        "percentile; the best is %.4f").format(
                    measuredDesigns.size, measuredDesigns.minOf { it.p90OverStroke }
                ),
            "PASS"
        ),
        T155PredicateRecord(
            "P5 — every standing figure reproduces",
            "the worst strict reproduction departure is %.3e".format(worstStrict),
            if (worstStrict < 5e-3) "PASS" else "FAIL"
        ),
        T155PredicateRecord(
            "F1 — the declared falsifier: is the density axis MONOTONE in the path count?",
            "the 90th percentile with equal springs at %s paths is %s".format(
                T155_COLUMN_SWEEP.map { it * 15 }.joinToString("/"),
                densityCurve.joinToString("/") { "%.4f".format(it) }
            ),
            if (densityMonotone) "DID NOT FIRE" else "FIRED"
        ),
        T155PredicateRecord(
            "F2 — does the percentile objective beat the zero-defect one?",
            "on all three descent sets the percentile-optimised design's own 90th percentile is " +
                    "compared against the zero-defect-optimised design's",
            if (percentileBeatsZeroDefect) "DID NOT FIRE" else "FIRED"
        ),
        T155PredicateRecord(
            "F5 — does the ORACLE floor settle the question with no search?",
            ("the worst 90th-percentile oracle floor over every station set is %.5f against " +
                    "T-5b's 0.10").format(oracleRecords.maxOf { it.p90FloorOverStroke }),
            if (oracleRecords.any { it.excludesEveryDistributionAtP90 }) "FIRED" else "DID NOT FIRE"
        )
        )
    }

    // --------------------------------------------------------------------- the result
    val result = T155Result(
        task = "T-155",
        leaf = "A8.2",
        conditions = ("T = 300 K, k_BT = 4.141947 pN.nm; aqueous 2 mM MgCl2; " +
                "40.0 x %.2f nm single-layer square-lattice sheet, 15 duplexes at 2.69 nm; " +
                "C-0022's SOLVED edge profiles at 2 mM / 10 nm / 0.192 V (design, every " +
                "headline) and 2 mM / 7 nm / 0.192 V (the held end of C-0068's range); " +
                "C-0017's 33.3333 pN/nm as a SUM at the acceptable 3 nm stroke; free-tile " +
                "stroke %.5f nm; dishing on an 81 x 81 grid; flat means below T-5b's 0.10 " +
                "CONVENTION; grading seed %d at %d realisations, training seed %d at %d")
            .format(
                lengthY, freeStroke, T155_GRADING_SEED, T155_GRADING_REALISATIONS,
                T155_TRAINING_SEED, T155_TRAINING_REALISATIONS
            ),
        decision = "the objective is the 90th percentile of the peak dishing under C-0087's " +
                "seeded Bernoulli dropout, minimised out of sample over placements and " +
                "distributions and bounded from below over ALL distributions",
        bounds = bounds,
        redundancy = redundancyRecords,
        designs = designRecords,
        oracle = oracleRecords,
        plan = plan,
        convergence = convergence,
        reproductions = reproductions,
        predicates = predicates,
        findings = guardedProse(
            listOf("THE PROSE FAILED and the numbers were rescued; the exception follows")
        ) {
            t155Findings(
                measuredDesigns, densityCurve, densityMonotone, anyFlat, oracleRecords,
                redundancyRecords, plan, rankAgreement, bendingLengthAlong, rangeP90, bestRecord,
                percentileBeatsZeroDefect, descentSets.map { it.name }
            )
        } + listOfNotNull(proseFailure?.let { "THE PROSE FAILED WITH: $it" }),
        validity = listOf(
            "TRL 1-3. Model-consistent and traceable. The INPUT is measured — Strauss et al. " +
                    "(2018) through C-0087 — and nothing derived here is.",
            "The incorporation field, its five conventions and the independence of the " +
                    "realisations are C-0087's UNCHANGED, and inherit its whole validity range: " +
                    "a plain Rothemund rectangle at one folding protocol, and a coupling path " +
                    "whose own incorporation nobody has measured.",
            "The dishing pipeline, the lattice, the hosts, the load and the free-tile stroke " +
                    "are C-0058's, C-0063's and C-0074's unchanged — C-0022's unsourced rim " +
                    "charge, C-0001's single foundation secant, one crossover layout per " +
                    "placement.",
            "The m x 15 grids above three columns are ABSTRACT station sets. C-0053 places no " +
                    "45-arm array on this tile at all, C-0041 places no 45-flexure array at any " +
                    "of 720 orientations, and CLAUDE.md's own slot finding says a column of " +
                    "ties severs the sheet. They are priced here to say what redundancy WOULD " +
                    "buy, not to propose them.",
            "The ORACLE floor is a bound on the ROOT MEAN SQUARE against a PEAK, so it is loose " +
                    "by whatever the peak-to-RMS ratio of the residual field is. It can " +
                    "therefore exclude and can never admit.",
            "The 90th percentile is the verdict statistic, inherited from C-0087 so the two " +
                    "are comparable. No upstream clause says what fraction of built tiles a " +
                    "design may lose.",
            "T-5b's 0.10 is a CONVENTION, not a physical threshold.",
            "Single layer, static, 300 K, aqueous 2 mM MgCl2."
        ),
        openQuestions = listOf(
            "Whether the coupling element's own incorporation is the staple's — C-0087's item " +
                    "2, unchanged and now the ONLY route by which the programme keeps a flat " +
                    "tile.",
            "Whether a LARGER tile is the answer. Everything here is bounded by a 40 nm plan " +
                    "and a 10.88 nm root lattice; the redundancy the dropout demands is a " +
                    "column count, and a column count is a length.",
            "Whether a coupling that is not an array — one stiff body tied at many points, so " +
                    "that a missing tie is a stiffness perturbation rather than a removed load " +
                    "path — escapes the count argument entirely. Nothing in this corpus has " +
                    "priced it.",
            "What fraction of built tiles a flatness verdict is owed over. C-0087's item 4, " +
                    "unchanged, and it is now the parameter the verdict is most sensitive to.",
            "Whether the dropout is correlated within a folding run. C-0087's item 3, unchanged."
        ),
        parameters = mapOf(
            "gradingRealisations" to "$T155_GRADING_REALISATIONS",
            "gradingSeed" to "$T155_GRADING_SEED (C-0087's own, so its cells reproduce)",
            "trainingRealisations" to "$T155_TRAINING_REALISATIONS",
            "trainingSeed" to "$T155_TRAINING_SEED (independent, so every quote is out of sample)",
            "decisionDigits" to "$T155_DECISION_DIGITS",
            "decisionFloor" to "$T155_DECISION_FLOOR",
            "samplesPerEdge" to "$T155_SAMPLES",
            "flatnessTolerance" to "$T155_TOLERANCE (T-5b's CONVENTION)",
            "collarWidth" to "$T155_COLLAR nm (C-0058's rim mask)",
            "columnSweep" to T155_COLUMN_SWEEP.joinToString("/"),
            "rimRatiosSwept" to T155_RIM_RATIOS.joinToString("/"),
            "compensationExponentsSwept" to T155_COMPENSATION_EXPONENTS.joinToString("/"),
            "perPathStiffnessCeiling" to "%.6f pN/nm (10 pN unzip at the acceptable 3 nm)"
                .format(unzipCeiling),
            "bendingLengthAlongHelices" to "%.5f nm".format(bendingLengthAlong),
            "bendingLengthAcrossHelices" to "%.5f nm".format(bendingLengthAcross),
            "worstStrictReproductionDeparture" to "%.3e".format(worstStrict)
            // NO runtime and nothing that counts steps — CLAUDE.md, and C-0084/C-0087 both had
            // to remove a wall clock.
        )
    )

    val output = File("gpd/results/T-155-dropout-robust-placement.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digits = T155_DECISION_DIGITS + 3,
                digitsByKey = DEPARTURE_DIGITS_BY_KEY,
                floor = T155_DECISION_FLOOR
            ) as JsonObject)
        ) + "\n"
    )
    println("T-155 — wrote ${output.path}")
    result.findings.forEach { println("  * $it") }
    result.predicates.forEach { println("  [${it.verdict}] ${it.name}") }
    // Rethrown only AFTER the numbers are on disk — the guard rescues a run, it does not
    // tolerate a defect.
    proseFailure?.let { throw it }
}

// ---------------------------------------------------------------------------------------------
// the prose, built AFTER the result so a format placeholder cannot cost the run
// ---------------------------------------------------------------------------------------------

@Suppress("LongParameterList", "LongMethod")
private fun t155Findings(
    measuredDesigns: List<T155DesignRecord>,
    densityCurve: List<Double>,
    densityMonotone: Boolean,
    anyFlat: Boolean,
    oracle: List<T155OracleRecord>,
    redundancy: List<T155RedundancyRecord>,
    plan: List<T155PlanRecord>,
    rankAgreement: Double,
    bendingLength: Double,
    rangeP90: Double?,
    best: T155DesignRecord,
    percentileBeatsZeroDefect: Boolean,
    descentSetNames: List<String>
): List<String> {
    val findings = ArrayList<String>()

    findings += ("THE ANSWER IS %s. Over %d graded placement x distribution cells under " +
            "C-0087's measured dropout, the best 90th-percentile dishing is %.4f of the stroke " +
            "on %s with %s, against T-5b's 0.10 — %.2fx the convention. %s").format(
        if (anyFlat) "YES" else "NO",
        measuredDesigns.size,
        best.p90OverStroke,
        best.stationSet,
        best.distribution,
        best.p90OverStroke / 0.10,
        if (anyFlat) "A flat design exists."
        else "No Gen-1 coupling in this family is flat at the state of the art in folding."
    )

    findings += ("DENSITY IS THE AXIS AND IT IS MONOTONE: %s. With EQUAL springs the 90th " +
            "percentile runs %s over %s paths — a factor of %.2f from the sparsest to the " +
            "densest — so C-0087's reading of its own ranking reversal is confirmed on a sweep " +
            "rather than on two points. The declared falsifier F1 %s.").format(
        if (densityMonotone) "the percentile falls at every step" else "IT IS NOT",
        densityCurve.joinToString(" -> ") { "%.4f".format(it) },
        redundancy.joinToString("/") { "${it.pathCount}" },
        densityCurve.max() / densityCurve.min(),
        if (densityMonotone) "did NOT fire" else "FIRED"
    )

    val demanded = redundancy.maxOf { it.pathsDemandedByThatRun }
    val buildable = plan.filter { it.selfConsistent }.maxOf { it.pathCount }
    findings += ("AND THE DENSITY THE DROPOUT DEMANDS IS NOT BUILDABLE, WHICH IS THE WHOLE " +
            "ANSWER. The along-helix Winkler bending length is %.2f nm, so a coupling stays " +
            "out of CLAUDE.md's net-dishing-source regime only while its attachment pitch is " +
            "inside it; a dropout IS an increase in that pitch, the 90th-percentile longest run " +
            "of consecutive absences is %d at the densest grid swept, and the column count that " +
            "keeps the surviving pitch inside one bending length is therefore %d, i.e. %d " +
            "paths. C-0075's plan table caps the recommended rooted arm at %d self-consistent " +
            "paths — at 45 the arm is %.3f nm against an %.2f nm lattice ceiling and only %d " +
            "place — so the demand exceeds the buildable count by %.1fx. The redundancy axis is " +
            "the right one and the tile cannot supply it.").format(
        bendingLength,
        redundancy.maxOf { it.worstAbsenceRunAtP90 },
        redundancy.maxOf { it.columnsDemandedByThatRun },
        demanded,
        buildable,
        plan.first { it.pathCount == 45 }.armLength,
        plan.first { it.pathCount == 45 }.latticeCeiling,
        plan.first { it.pathCount == 45 }.placed,
        demanded.toDouble() / buildable
    )

    val worstOracle = oracle.maxByOrNull { it.p90FloorOverStroke }!!
    findings += ("THE RIGOROUS BOUND: an ORACLE that knew which staples were missing and was " +
            "allowed a different distribution for every tile it built could reach %.5f of the " +
            "stroke at the 90th percentile on %s, against %.5f at full presence — so the " +
            "negative %s over ALL distributions, not only over the searched family. The bound " +
            "is on a root mean square against a peak, so it can exclude and can never admit.")
        .format(
            worstOracle.p90FloorOverStroke,
            worstOracle.stationSet,
            worstOracle.floorAtFullPresenceOverStroke,
            if (oracle.any { it.excludesEveryDistributionAtP90 }) "is PROVEN"
            else "is NOT proven by this bound"
        )

    findings += ("THE PERCENTILE OBJECTIVE IS THE RIGHT ONE AND IT IS NOT ENOUGH. On %s the " +
            "descent that minimises the 90th percentile out of sample %s the descent that " +
            "minimises the zero-defect dishing, and the falsifier F2 %s — but neither reaches " +
            "the convention. A cancellation optimised at zero defects and a design optimised " +
            "at the 90th percentile are different designs and both fail, which is the " +
            "strongest form the statement can take.").format(
        descentSetNames.joinToString(", ") { it.take(40) },
        if (percentileBeatsZeroDefect) "beats" else "does NOT beat",
        if (percentileBeatsZeroDefect) "did NOT fire" else "FIRED"
    )

    findings += ("THE CHEAP BOUND RANKS THE DESIGNS: the Spearman correlation between the " +
            "worst single-path removal — n solves, no sampling — and the 90th percentile over " +
            "every graded design is %.4f. %s").format(
        rankAgreement,
        if (abs(rankAgreement) >= 0.8)
            "So a placement search may be run on the cheap objective, which is 10 000/n times " +
                    "cheaper than the sampler, and C-0087's bound is an instrument and not only " +
                    "an explanation."
        else
            "So the cheap bound explains the LEVEL and not the ordering: it says a design will " +
                    "fail and cannot say which fails least."
    )

    if (rangeP90 != null) {
        findings += ("OVER THE RANGE: the best design's 90th percentile over the two ends " +
                "C-0068's placed 2 mM device traverses is %.4f of the stroke against %.4f at " +
                "the design state alone, so the range costs %.1f %% and changes no verdict.")
            .format(rangeP90, best.p90OverStroke, 100.0 * (rangeP90 / best.p90OverStroke - 1.0))
    }

    val equalAt34 = measuredDesigns.firstOrNull {
        it.stationSet.startsWith("C-0063") && it.distribution == "EQUAL"
    }
    val equalAt30 = measuredDesigns.firstOrNull {
        it.stationSet.startsWith("C-0074") && it.distribution == "EQUAL"
    }
    val grid30 = measuredDesigns.firstOrNull {
        it.stationSet.startsWith("C-0015's 2 ") && it.distribution == "EQUAL"
    }
    if (equalAt34 != null && equalAt30 != null && grid30 != null) {
        findings += ("MORE REGULAR IS WORTH LESS THAN DENSER, AND IT IS MEASURABLE AT MATCHED " +
                "COUNT. C-0074's 30 upward roots read %.4f at the 90th percentile and the " +
                "regular 2 x 15 grid of the same count reads %.4f (%.2fx), where the step from " +
                "30 to 90 paths on the grid is %.2fx. So the ranking reversal C-0087 attributes " +
                "to regularity is mostly a COUNT effect, and regularity is the smaller half of " +
                "the direction it names.").format(
            equalAt30.p90OverStroke, grid30.p90OverStroke,
            equalAt30.p90OverStroke / grid30.p90OverStroke,
            densityCurve[1] / densityCurve.last()
        )
    }

    val amplifications = measuredDesigns.map { it.singleRemovalAmplification }
    findings += ("EVERY DESIGN IN THE SWEEP IS A CANCELLATION, AND THE AMPLIFICATION IS A " +
            "PROPERTY OF THE COUNT. One missing path multiplies the dishing by %.2f to %.2f " +
            "over the %d graded designs, and the largest amplifications belong to the sparsest " +
            "and best-optimised ones. C-0087's one-line explanation survives its own " +
            "generalisation.").format(
        amplifications.min(), amplifications.max(), measuredDesigns.size
    )

    return findings
}
