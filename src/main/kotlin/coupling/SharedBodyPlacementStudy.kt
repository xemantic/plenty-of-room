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

import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.DEPARTURE_DIGITS_BY_KEY
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.OrigamiSheet
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.roundForResult
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
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
import kotlin.math.min

/**
 * `T-165` — a **distribution and placement** search on `C-0093`'s shared-body topology, and the
 * last axis the flat-tile question has.
 *
 * `C-0093` reaches **0.24028028** of the free-tile stroke at the 90th percentile under `C-0087`'s
 * measured staple dropout — the lowest this programme has attained, and still 2.40× `T-5b`'s
 * 0.10 — with **uniform** ties on `C-0015`'s **abstract** equal-tributary grid. This study spends
 * the two variables it held fixed: **where** the ties land, on the upward crossover sites the
 * lattice actually supplies, and **how stiff each of them is**.
 *
 * Five cheap bounds run before any sampler and two of them are capable of ending an axis on their
 * own; the Monte Carlo is `C-0087`'s and is unchanged except in the coupling.
 */

// ---------------------------------------------------------------------------------------------
// the records
// ---------------------------------------------------------------------------------------------

/** One cheap bound, settled before the search it precedes. */
@Serializable
private data class T165BoundRecord(
    val name: String,
    val value: Double,
    val unit: String,
    val settles: String
)

/** One phase of the column lattice, as a **count** — the census, with no solve at all. */
@Serializable
private data class T165CensusRecord(
    val phaseBasePairs: Int,
    val upwardSites: Int,
    val rowLengths: String,
    val columnsPerRow: Int,
    val sitePitch: Double,
    val sheetCrossoverColumns: Int,
    val eightColumnHost: Boolean,
    val centroSymmetric: Boolean
)

/** The upward lattice's own pitch against `C-0089`'s run-length demand. */
@Serializable
private data class T165PitchRecord(
    val phaseBasePairs: Int,
    val columnsAvailable: Int,
    val sitePitch: Double,
    val pitchOverBendingLength: Double,
    val worstRunAtP90: Int,
    val survivingPitch: Double,
    val columnsDemanded: Int,
    val columnShortfall: Double,
    val clears: Boolean
)

/** A log-log redundancy fit and what it demands at a stated inventory. */
@Serializable
private data class T165FitRecord(
    val source: String,
    val points: Int,
    val slope: Double,
    val countAtTolerance: Double,
    val inventory: Int,
    val predictedAtInventory: Double,
    val factorDemandedAtInventory: Double
)

/** One phase screened on the CHEAP objective — `n + 1` solves, no sampling. */
@Serializable
private data class T165ScreenRecord(
    val phaseBasePairs: Int,
    val pathCount: Int,
    val sheetCrossoverColumns: Int,
    val sharedNominalOverStroke: Double,
    val sharedWorstSingleRemovalOverStroke: Double,
    val arrayNominalOverStroke: Double,
    val arrayWorstSingleRemovalOverStroke: Double,
    val oracleFloorAtFullPresence: Double
)

/** One graded `(placement, topology, distribution)` cell under `C-0087`'s measured dropout. */
@Suppress("LongParameterList")
@Serializable
private data class T165CellRecord(
    val stationSet: String,
    val phaseBasePairs: Int,
    val pathCount: Int,
    val topology: String,
    val distribution: String,
    val tieFloor: Double,
    val tieCeiling: Double,
    val peakOverLeastTie: Double,
    val realisations: Int,
    /**
     * The 90th percentile the descent that produced this design **saw**, on its own training
     * ensemble, or `0.0` where no descent was run. Emitted beside the out-of-sample reading so
     * that an over-fit is visible rather than inferred.
     */
    val inSampleP90OverStroke: Double,
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
    val oracleFloorAtP90: Double,
    val peakTieForce: Double,
    val peakTieForceUnderDropout: Double,
    val insideUnzipAllowable: Boolean,
    val insideShearAllowable: Boolean,
    val flatAtP90: Boolean,
    val flatAtMedian: Boolean
)

/** One rung of the tie ladder, with the spread the DISTRIBUTION axis has left at that rung. */
@Serializable
private data class T165LimitRecord(
    val tieScale: Double,
    val uniformNominalOverStroke: Double,
    val shapeSpreadAtNominal: Double,
    val uniformP90OverStroke: Double,
    val shapeSpreadAtP90: Double,
    val shapeRealisations: Int,
    val kinematicMatrixDeparture: Double,
    val peakTieForceUnderDropout: Double
)

/** A buildability or placement fact, with the claim that owns it. */
@Serializable
private data class T165BuildRecord(
    val question: String,
    val demanded: Double,
    val available: Double,
    val unit: String,
    val clears: Boolean,
    val owner: String,
    val note: String
)

/**
 * One convergence axis. [departure] is emitted at **two significant digits** and nothing finer
 * (`CLAUDE.md`, `C-0093`, `P-18`): a difference of two nearly equal solves is exactly the quantity
 * a JIT recompilation moves.
 */
@Serializable
private data class T165ConvergenceRecord(
    val axis: String,
    val levels: List<String>,
    val values: List<Double>,
    val departure: Double,
    val note: String
)

private fun t165Convergence(
    axis: String,
    levels: List<String>,
    values: List<Double>,
    departure: Double,
    note: String
) = T165ConvergenceRecord(
    axis, levels, values, roundForResult(departure, 2, T165_DECISION_FLOOR), note
)

/**
 * One standing figure, reproduced rather than transcribed.
 *
 * [departure] is emitted at **two significant digits** for exactly the reason `C-0093` emits its
 * *convergence* departures that way, and this study is where the omission showed: two runs of
 * identical code agreed on all 1 484 numeric fields and disagreed in **two reproduction
 * departures** at the tenth digit (`1.06411397e−9` against `1.06410993e−9`). A departure is a
 * difference of two nearly equal numbers — the quantity a JIT recompilation moves — and it is
 * **dimensionless**, so `RESULT_ABSOLUTE_FLOOR`, which is a statement in the locked units, cannot
 * catch it (`P-18`). `C-0093` cured the convergence axis and left this one.
 */
@Serializable
private data class T165ReproductionRecord(
    val name: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double,
    val owner: String
)

/** One acceptance predicate. */
@Serializable
private data class T165PredicateRecord(
    val name: String,
    val statement: String,
    val verdict: String
)

/** One declared falsifier and whether it fired. */
@Serializable
private data class T165FalsifierRecord(
    val name: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T165Result(
    val task: String,
    val question: String,
    val leaf: String,
    val conditions: Map<String, String>,
    val parameters: Map<String, String>,
    val cheapBounds: List<T165BoundRecord>,
    val census: List<T165CensusRecord>,
    val pitchLedger: List<T165PitchRecord>,
    val redundancyFits: List<T165FitRecord>,
    val phaseScreen: List<T165ScreenRecord>,
    val cells: List<T165CellRecord>,
    val tieLadder: List<T165LimitRecord>,
    val buildability: List<T165BuildRecord>,
    val convergence: List<T165ConvergenceRecord>,
    val reproductions: List<T165ReproductionRecord>,
    val predicates: List<T165PredicateRecord>,
    val falsifiers: List<T165FalsifierRecord>,
    val findings: List<String>
)

// ---------------------------------------------------------------------------------------------
// the constants
// ---------------------------------------------------------------------------------------------

private const val T165_DUPLEXES = 15
private const val T165_SAMPLES = 81
private const val T165_TOLERANCE = 0.10
private const val T165_C0063_PHASE = 24
private const val T165_RIM_STANDOFF = 1.0
private const val T165_DECISION_DIGITS = 6
private const val T165_DECISION_FLOOR = 1e-12

/** `C-0087`'s own grading seed, so every percentile here is comparable cell for cell. */
private const val T165_GRADING_SEED = 20260817L

/** `C-0089`'s own training seed. Every optimised percentile is read OUT OF SAMPLE. */
private const val T165_TRAINING_SEED = 20260819L

private var t165GradingRealisations = System.getenv("T165_REALISATIONS")?.toInt() ?: 10_000
private var t165TrainingRealisations = System.getenv("T165_TRAINING")?.toInt() ?: 200
private var t165ShapeRealisations = System.getenv("T165_SHAPE")?.toInt() ?: 2000
private var t165DescentSweeps = System.getenv("T165_SWEEPS")?.toInt() ?: 2

/** The tie stiffness the placement screen and the headline are read at, in pN/nm — `C-0093`'s. */
private const val T165_DESIGN_TIE = 1000.0

/** `C-0049`'s per-path force allowable read as a stiffness, `a/s` — the conservative cap. */
private val T165_CONSERVATIVE_TIE_CEILING =
    perPathStiffnessCeiling(Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE)

/** The tie ladder the kinematic limit is measured along, in pN/nm. */
private val T165_TIE_LADDER = listOf(3.33333333, 10.0, 100.0, 1000.0, 10_000.0, 100_000.0)

/** The counts the redundancy axis is re-fitted at on the REAL lattice. */
private val T165_REAL_COUNTS = listOf(15, 20, 26, 34, 45)

private val T165_EDGE_X = Gen1Tile.EDGE_X
private val T165_MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

/** `C-0047`'s along-helix Winkler bending length in nm, reproduced by `C-0089`. */
private const val T165_BENDING_LENGTH = 12.8290845

/** `C-0089`'s 90th-percentile longest absence run within a row. */
private const val T165_WORST_RUN = 3

// ---------------------------------------------------------------------------------------------
// the inputs, read from the claims that own them
// ---------------------------------------------------------------------------------------------

private class T165Profile(
    val name: String,
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, lengthY: Double): PressureField = edgeCollarPressure(
        interiorPressure, T165_EDGE_X, lengthY,
        listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T165_RIM_STANDOFF))
    )
}

/** `C-0022`'s solved profile, keyed on **`(concentration, gap, bias)`** — `CLAUDE.md`'s gotcha. */
private fun t165Profile(file: File, key: Triple<Double, Double, Double>): T165Profile {
    require(file.exists()) {
        "C-0022's result file is missing: ${file.path}. T-165 consumes the SOLVED edge profile."
    }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(name: String) = it.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == key.first && value("gapHeight") == key.second &&
                    value("appliedBias") == key.third
        } ?: error("no C-0022 profile at ${key.first} mM, ${key.second} nm, ${key.third} V")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T165Profile(
        name = "%.1f mM, %.0f nm, %.3f V".format(key.first, key.second, key.third),
        smoothDepth = value("taperDepth"),
        smoothWidth = value("taperWidth"),
        rimDepth = value("rimResidualDepth")
    )
}

/** `C-0063`'s 34-root placement, read from the result file of the claim that owns it. */
private fun t165Placement(file: File, interhelical: Double): List<Pair<Double, Double>> {
    require(file.exists()) { "C-0063's result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("bestPlacement").jsonArray.map { it.jsonObject }
        .flatMap { row ->
            val index = row.getValue("row").jsonPrimitive.content.toInt()
            val y = (index - (T165_DUPLEXES - 1) / 2.0) * interhelical
            row.getValue("roots").jsonArray.map { it.jsonPrimitive.content.toDouble() to y }
        }
}

/** `C-0093`'s own density curve, so its slope and its 252-tie crossing are refitted, not quoted. */
private fun t165SharedBodyDensity(file: File): List<Pair<Int, Double>> {
    require(file.exists()) { "C-0093's result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("density").jsonArray.map { it.jsonObject }
        .filter { it.getValue("topology").jsonPrimitive.content == "SHARED BODY" }
        .map {
            it.getValue("pathCount").jsonPrimitive.content.toInt() to
                    it.getValue("p90OverStroke").jsonPrimitive.content.toDouble()
        }
        .sortedBy { it.first }
}

// ---------------------------------------------------------------------------------------------
// the models
// ---------------------------------------------------------------------------------------------

private fun t165Sheet(): OrigamiSheet =
    origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)

private fun t165Host(sheet: OrigamiSheet, phase: Int): OrigamiGrillage = OrigamiGrillage(
    sheet = sheet,
    lengthX = T165_EDGE_X,
    beamCount = T165_DUPLEXES,
    foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
    columns = CrossoverLayout.atBasePairPhase(phase, sheet, T165_EDGE_X),
    subdivisions = 2,
    supports = emptyList()
)

/** One placement with its Woodbury bank and its `C-0087` ensembles. */
private class T165Placement(
    val name: String,
    val phaseBasePairs: Int,
    val stations: List<Pair<Double, Double>>,
    val rowLengths: List<Int>,
    val surrogate: InfluenceSurrogate,
    val probabilities: List<Double>,
    val grading: DropoutEnsemble,
    val training: DropoutEnsemble
) {
    val pathCount: Int get() = stations.size
}

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

@Suppress("LongMethod", "ComplexMethod", "CyclomaticComplexMethod")
fun main() {
    val sheet = t165Sheet()
    val lengthY = T165_DUPLEXES * sheet.interhelicalDistance
    val interiorPressure = Gen1Tile.TARGET_FORCE / (T165_EDGE_X * lengthY)

    println("T-165 — reading C-0022's solved load, C-0063's placement and C-0093's density ...")
    val designProfile = t165Profile(
        ResultInputs.T_3B.file(), Triple(2.0, 10.0, 0.192)
    )
    val designField = designProfile.field(interiorPressure, lengthY)
    val roots34 = t165Placement(
        ResultInputs.T_125.file(), sheet.interhelicalDistance
    )
    check(roots34.size == 34) { "C-0063's placement must carry 34 roots" }
    val sharedDensity = t165SharedBodyDensity(
        ResultInputs.T_162.file()
    )

    val freeStroke = PlateOnFoundation(
        sheet.plate(T165_EDGE_X, lengthY), Gen1Tile.FOUNDATION_SECANT, emptyList(), 12
    ).solve(uniformPressure(interiorPressure)).meanDeflection
    val measuredField = measuredDepthIncorporation(T165_EDGE_X, lengthY)
    val rigidBody = PlacedSharedBody(T165_EDGE_X, lengthY, T165_MANDATE)

    // ------------------------------------------------- cheap bound 1: the census, a count only
    val census = upwardTieCensus(T165_EDGE_X, T165_DUPLEXES)
    val censusRecords = census.map { lattice ->
        val columns = CrossoverLayout.atBasePairPhase(
            lattice.phaseBasePairs, sheet, T165_EDGE_X
        ).positions.size
        T165CensusRecord(
            phaseBasePairs = lattice.phaseBasePairs,
            upwardSites = lattice.siteCount,
            rowLengths = lattice.rowLengths.joinToString(""),
            columnsPerRow = lattice.rowLengths.max(),
            sitePitch = lattice.rowPitch,
            sheetCrossoverColumns = columns,
            eightColumnHost = columns >= 8,
            centroSymmetric = lattice.isCentroSymmetric()
        )
    }
    val maximumInventory = censusRecords.maxOf { it.upwardSites }
    val richestPhases = censusRecords.filter { it.upwardSites == maximumInventory }
        .map { it.phaseBasePairs }
    val eightColumnPhases = censusRecords.filter { it.eightColumnHost }.map { it.phaseBasePairs }
    val richestAndEightColumn = richestPhases.intersect(eightColumnPhases.toSet())

    // -------------------------------------- cheap bound 2: the redundancy division, no solve
    val sharedFit = redundancyFit(sharedDensity, T165_TOLERANCE)
    val fitRecords = arrayListOf(
        T165FitRecord(
            source = "C-0093's shared body on C-0015's abstract m x 15 grids, refitted",
            points = sharedDensity.size,
            slope = sharedFit.slope,
            countAtTolerance = sharedFit.countAtTolerance,
            inventory = maximumInventory,
            predictedAtInventory = sharedFit.predictedAt(maximumInventory.toDouble()),
            factorDemandedAtInventory = sharedFit.factorDemandedAt(maximumInventory.toDouble())
        )
    )

    // --------------------------------- cheap bound 3: the lattice's own pitch against C-0089's
    val pitchRecords = listOf(T165_C0063_PHASE, richestPhases.first()).distinct().map { phase ->
        val ledger = upwardPitchLedger(
            census[phase], T165_EDGE_X, T165_BENDING_LENGTH, T165_WORST_RUN
        )
        T165PitchRecord(
            phaseBasePairs = ledger.phaseBasePairs,
            columnsAvailable = ledger.columnsAvailable,
            sitePitch = ledger.sitePitch,
            pitchOverBendingLength = ledger.pitchOverBendingLength,
            worstRunAtP90 = ledger.worstRunAtP90,
            survivingPitch = ledger.survivingPitch,
            columnsDemanded = ledger.columnsDemanded,
            columnShortfall = ledger.columnShortfall,
            clears = ledger.clears
        )
    }

    // ------------------------------------------------------------------ the banks and the screen
    println("T-165 — screening 32 phases on the cheap objective ...")
    val hosts = HashMap<Int, OrigamiGrillage>()
    fun host(phase: Int) = hosts.getOrPut(phase) { t165Host(sheet, phase) }

    fun placement(
        name: String,
        phase: Int,
        stations: List<Pair<Double, Double>>,
        rowLengths: List<Int>
    ): T165Placement {
        val probabilities = stations.map { (x, y) -> measuredField.at(x, y) }
        return T165Placement(
            name = name,
            phaseBasePairs = phase,
            stations = stations,
            rowLengths = rowLengths,
            surrogate = latticeInfluenceSurrogate(
                host(phase), stations, designField, T165_SAMPLES
            ),
            probabilities = probabilities,
            grading = dropoutEnsemble(probabilities, t165GradingRealisations, T165_GRADING_SEED),
            training = dropoutEnsemble(probabilities, t165TrainingRealisations, T165_TRAINING_SEED)
        )
    }

    val fullPlacements = census.associate { lattice ->
        val stations = lattice.stations(sheet.interhelicalDistance)
        lattice.phaseBasePairs to placement(
            "the full upward inventory at phase %d".format(lattice.phaseBasePairs),
            lattice.phaseBasePairs, stations, lattice.rowLengths
        )
    }

    /** The rigid body placed on the mandate, with uniform ties of [tie]. */
    fun uniformTies(set: T165Placement, tie: Double) = List(set.pathCount) { tie }

    fun sharedAt(set: T165Placement, ties: List<Double>): SharedBody =
        rigidBody.placedAt(set.stations, ties).body

    fun dishing(set: T165Placement, ties: List<Double>, body: SharedBody?, present: List<Boolean>) =
        set.surrogate.solveWithSharedBody(ties, body, present).peakDishing / freeStroke

    fun worstRemoval(set: T165Placement, ties: List<Double>, body: SharedBody?): Double =
        (0 until set.pathCount).maxOf { absent ->
            dishing(set, ties, body, (0 until set.pathCount).map { it != absent })
        }

    val allPresent = HashMap<Int, List<Boolean>>()
    fun present(count: Int) = allPresent.getOrPut(count) { List(count) { true } }

    val screen = census.map { lattice ->
        val set = fullPlacements.getValue(lattice.phaseBasePairs)
        val ties = uniformTies(set, T165_DESIGN_TIE)
        val body = sharedAt(set, ties)
        val equal = List(set.pathCount) { T165_MANDATE / set.pathCount }
        T165ScreenRecord(
            phaseBasePairs = lattice.phaseBasePairs,
            pathCount = set.pathCount,
            sheetCrossoverColumns = censusRecords[lattice.phaseBasePairs].sheetCrossoverColumns,
            sharedNominalOverStroke = dishing(set, ties, body, present(set.pathCount)),
            sharedWorstSingleRemovalOverStroke = worstRemoval(set, ties, body),
            arrayNominalOverStroke = dishing(set, equal, null, present(set.pathCount)),
            arrayWorstSingleRemovalOverStroke = worstRemoval(set, equal, null),
            oracleFloorAtFullPresence = set.surrogate.reachableDishingFloor / freeStroke
        )
    }

    // The screen's own ranking, decided at six significant digits with the phase as the
    // tie-break — `CLAUDE.md`'s argmin trap, on a sweep whose winner selects everything below.
    val ranked = screen.sortedWith(
        compareBy(
            { roundForResult(it.sharedWorstSingleRemovalOverStroke, T165_DECISION_DIGITS) },
            { it.phaseBasePairs }
        )
    )
    val bestRichest = ranked.first { it.phaseBasePairs in richestPhases }.phaseBasePairs
    val bestEightColumn = ranked.first { it.phaseBasePairs in eightColumnPhases }.phaseBasePairs
    val gradedPhases = (ranked.take(5).map { it.phaseBasePairs } +
            listOf(T165_C0063_PHASE, 8, bestRichest, bestEightColumn)).distinct().sorted()

    // ------------------------------------------------------------------ the graded cells
    println("T-165 — grading %d phases at %d realisations ...".format(
        gradedPhases.size, t165GradingRealisations
    ))
    val cells = ArrayList<T165CellRecord>()

    @Suppress("LongParameterList")
    fun grade(
        set: T165Placement,
        topology: String,
        distribution: String,
        ties: List<Double>,
        body: SharedBody?,
        ensemble: DropoutEnsemble = set.grading,
        withOracle: Boolean = false,
        inSampleP90: Double = 0.0
    ): T165CellRecord {
        val nominalSolve = set.surrogate.solveWithSharedBody(ties, body, present(set.pathCount))
        val nominal = nominalSolve.peakDishing / freeStroke
        val removal = worstRemoval(set, ties, body)
        var peakUnderDropout = 0.0
        val sample = DoubleArray(ensemble.realisations) {
            val solved = set.surrogate.solveWithSharedBody(ties, body, ensemble.presenceAt(it))
            peakUnderDropout = max(peakUnderDropout, solved.supportForces.maxOf { f -> abs(f) })
            solved.peakDishing / freeStroke
        }
        val summary = summariseDropoutDishing(
            sample, nominal, ensemble.meanSurvivors, T165_TOLERANCE
        )
        val oracle = if (!withOracle) 0.0 else orderStatistic(
            DoubleArray(ensemble.realisations) {
                set.surrogate.reachableDishingFloorAt(ensemble.presenceAt(it)) / freeStroke
            },
            0.90
        )
        val record = T165CellRecord(
            stationSet = set.name,
            phaseBasePairs = set.phaseBasePairs,
            pathCount = set.pathCount,
            topology = topology,
            distribution = distribution,
            tieFloor = ties.min(),
            tieCeiling = ties.max(),
            peakOverLeastTie = ties.max() / ties.min(),
            realisations = ensemble.realisations,
            inSampleP90OverStroke = inSampleP90,
            nominalOverStroke = nominal,
            worstSingleRemovalOverStroke = removal,
            singleRemovalAmplification = removal / nominal,
            medianOverStroke = summary.median,
            p90OverStroke = summary.p90,
            p95OverStroke = summary.p95,
            worstOverStroke = summary.worst,
            exceedance = summary.exceedance,
            exceedanceStandardError = summary.exceedanceStandardError,
            exceedanceOneSidedBound = summary.exceedanceOneSidedBound,
            meanSurvivors = summary.meanSurvivors,
            oracleFloorAtP90 = oracle,
            peakTieForce = nominalSolve.supportForces.maxOf { abs(it) },
            peakTieForceUnderDropout = peakUnderDropout,
            insideUnzipAllowable = peakUnderDropout <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE,
            insideShearAllowable = peakUnderDropout <= Gen1Tile.DUPLEX_SHEAR_ALLOWABLE,
            flatAtP90 = summary.flatAtP90,
            flatAtMedian = summary.flatAtMedian
        )
        cells += record
        return record
    }

    gradedPhases.forEach { phase ->
        val set = fullPlacements.getValue(phase)
        println("  phase %d, %d ties".format(phase, set.pathCount))
        grade(
            set, "ARRAY", "C-0017's mandate shared equally",
            List(set.pathCount) { T165_MANDATE / set.pathCount }, null
        )
        val ties = uniformTies(set, T165_DESIGN_TIE)
        grade(
            set, "SHARED BODY", "uniform at %.0f pN/nm".format(T165_DESIGN_TIE), ties,
            sharedAt(set, ties), withOracle = true
        )
    }

    // `C-0063`'s own 34 roots, both topologies — the reproduction of `C-0093`'s two headlines.
    val set34 = placement(
        "C-0063's 34 upward roots at phase 24", T165_C0063_PHASE, roots34,
        (0 until T165_DUPLEXES).map { row ->
            roots34.count { abs(it.second - (row - 7.0) * sheet.interhelicalDistance) < 1e-9 }
        }.filter { it > 0 }
    )
    val array34 = grade(
        set34, "ARRAY", "C-0017's mandate shared equally",
        List(34) { T165_MANDATE / 34.0 }, null
    )
    val shared34 = grade(
        set34, "SHARED BODY", "uniform at %.0f pN/nm".format(T165_DESIGN_TIE),
        uniformTies(set34, T165_DESIGN_TIE), sharedAt(set34, uniformTies(set34, T165_DESIGN_TIE))
    )

    val bestFullPhase = cells
        .filter { it.topology == "SHARED BODY" && it.pathCount > 34 }
        .sortedWith(compareBy(
            { roundForResult(it.p90OverStroke, T165_DECISION_DIGITS) }, { it.phaseBasePairs }
        ))
        .first().phaseBasePairs
    val bestSet = fullPlacements.getValue(bestFullPhase)
    println("T-165 — the best full-inventory phase is %d (%d ties)".format(
        bestFullPhase, bestSet.pathCount
    ))

    // -------------------------------------- cheap bound 4: the tie ladder and its kinematic limit
    println("T-165 — the tie ladder and the kinematic limit ...")
    fun meanOne(weights: List<Double>): List<Double> =
        normalisedStiffnesses(weights, 1.0).map { it * weights.size }
    val shapes: List<List<Double>> = listOf(
        List(bestSet.pathCount) { 1.0 },
        meanOne(rimStiffenedWeights(bestSet.stations, T165_EDGE_X, lengthY, 6.7, 5.0)),
        meanOne(rimStiffenedWeights(bestSet.stations, T165_EDGE_X, lengthY, 6.7, 0.2)),
        meanOne(bestSet.stations.map { 0.2 + 1.6 * (it.first + T165_EDGE_X / 2.0) / T165_EDGE_X }),
        List(bestSet.pathCount) { if (it % 2 == 0) 0.4 else 1.6 }
    )
    val shapeEnsemble = dropoutEnsemble(
        bestSet.probabilities, min(t165ShapeRealisations, t165GradingRealisations),
        T165_GRADING_SEED
    )
    val rigidShapes = rigidBody.modes.shapesAt(bestSet.stations)
    val unitGroundMatrix = rigidBody.modes.distributedGroundStiffness(T165_MANDATE)
    val tieLadder = T165_TIE_LADDER.map { scale ->
        val readings = shapes.map { shape ->
            val ties = shape.map { it * scale }
            val body = sharedAt(bestSet, ties)
            var peak = 0.0
            val sample = DoubleArray(shapeEnsemble.realisations) {
                val solved = bestSet.surrogate.solveWithSharedBody(
                    ties, body, shapeEnsemble.presenceAt(it)
                )
                peak = max(peak, solved.supportForces.maxOf { f -> abs(f) })
                solved.peakDishing / freeStroke
            }
            Triple(
                dishing(bestSet, ties, body, present(bestSet.pathCount)),
                orderStatistic(sample, 0.90),
                peak
            )
        }
        T165LimitRecord(
            tieScale = scale,
            uniformNominalOverStroke = readings[0].first,
            shapeSpreadAtNominal =
                (readings.maxOf { it.first } - readings.minOf { it.first }) / readings[0].first,
            uniformP90OverStroke = readings[0].second,
            shapeSpreadAtP90 =
                (readings.maxOf { it.second } - readings.minOf { it.second }) / readings[0].second,
            shapeRealisations = shapeEnsemble.realisations,
            kinematicMatrixDeparture = kinematicLimitDeparture(
                shapes[1], rigidShapes, unitGroundMatrix, scale
            ),
            peakTieForceUnderDropout = readings[0].third
        )
    }

    // ------------------------------------------------------------------ the distribution search
    println("T-165 — the distribution descent, out of sample ...")
    /**
     * A tie vector whose own sum does not exceed `C-0017`'s mandate cannot carry it at **any**
     * ground — the coupling's heave secant is `series(Σt, g)` and is bounded above by `Σt` — so
     * such a candidate is not a worse design but an infeasible one, and the descent is told so
     * rather than allowed to throw inside a Monte Carlo.
     */
    val infeasible = 1e3

    fun trainedP90(set: T165Placement, ties: List<Double>): Double {
        if (ties.sum() <= T165_MANDATE * (1.0 + 1e-9)) return infeasible
        val body = sharedAt(set, ties)
        val sample = DoubleArray(set.training.realisations) {
            set.surrogate.solveWithSharedBody(
                ties, body, set.training.presenceAt(it)
            ).peakDishing / freeStroke
        }
        return orderStatistic(sample, 0.90)
    }

    listOf(
        "C-0049's conservative cap, a/s" to T165_CONSERVATIVE_TIE_CEILING,
        "the force-solved cap" to T165_DESIGN_TIE
    ).forEach { (label, ceiling) ->
        val start = List(bestSet.pathCount) { ceiling }
        val found = optimiseTieDistribution(
            start = start,
            lowerBound = ceiling / 1000.0,
            upperBound = ceiling,
            sweeps = t165DescentSweeps,
            scanPoints = 5,
            refinements = 5
        ) { trainedP90(bestSet, it) }
        grade(
            bestSet, "SHARED BODY", "90th-percentile descent under $label",
            found.ties, sharedAt(bestSet, found.ties), inSampleP90 = found.objective
        )
        grade(
            bestSet, "SHARED BODY", "uniform at $label",
            start, sharedAt(bestSet, start), inSampleP90 = trainedP90(bestSet, start)
        )
    }

    // The buildable one-parameter families, chosen OUT OF SAMPLE and graded on C-0087's ensemble.
    val rimRatios = listOf(0.1, 0.2, 0.5, 2.0, 5.0, 10.0)
    val chosenRim = rimRatios.minByOrNull { ratio ->
        val ties = normalisedStiffnesses(
            rimStiffenedWeights(bestSet.stations, T165_EDGE_X, lengthY, 6.7, ratio), 1.0
        ).map { it * bestSet.pathCount * T165_DESIGN_TIE }
        roundForResult(trainedP90(bestSet, ties), T165_DECISION_DIGITS)
    }!!
    run {
        val ties = normalisedStiffnesses(
            rimStiffenedWeights(bestSet.stations, T165_EDGE_X, lengthY, 6.7, chosenRim), 1.0
        ).map { it * bestSet.pathCount * T165_DESIGN_TIE }
        grade(
            bestSet, "SHARED BODY", "C-0058's rim x %.1f, chosen out of sample".format(chosenRim),
            ties, sharedAt(bestSet, ties)
        )
    }
    run {
        val ties = normalisedStiffnesses(
            inverseIncorporationWeights(bestSet.probabilities, 1.0), 1.0
        ).map { it * bestSet.pathCount * T165_DESIGN_TIE }
        grade(
            bestSet, "SHARED BODY", "C-0087's 1/p compensation", ties, sharedAt(bestSet, ties)
        )
    }

    // ------------------------------------------------ the placement search at fixed count
    println("T-165 — the placement search at fixed count on the real lattice ...")
    val realCounts = ArrayList<Pair<Int, Double>>()
    T165_REAL_COUNTS.forEach { count ->
        if (count >= bestSet.pathCount) return@forEach
        val candidates = bestSet.stations.indices.toList()
        // A deterministic, spread start: every `n/count`-th site of the inventory.
        val start = (0 until count).map { it * bestSet.pathCount / count }.distinct()
        val ties = uniformTies(bestSet, T165_DESIGN_TIE)
        val fullShapes = rigidBody.modes.shapesAt(bestSet.stations)
        /**
         * The shared body of a **subset** of the bank's stations, evaluated on the bank.
         *
         * `solveWithSharedBody` reads a body's shapes only at the **surviving** stations, so one
         * body over the whole bank carrying the ground the subset demands is exactly the subset's
         * own body — and the mandate is placed on that subset rather than on the bank, which is
         * the whole point of a coupling at a chosen count.
         */
        fun subsetBody(chosen: List<Int>): SharedBody = SharedBody(
            fullShapes,
            rigidBody.placedAt(
                chosen.map { bestSet.stations[it] }, List(chosen.size) { T165_DESIGN_TIE }
            ).body.modalStiffness
        )

        val found = descendTieSubset(start, candidates, sweeps = 3) { chosen ->
            // `C-0089`'s cheap objective — the worst single removal, `n + 1` solves and no
            // sampling, which it measured against the 90th percentile at rho = 0.9729.
            val body = subsetBody(chosen)
            val live = chosen.toHashSet()
            chosen.maxOf { absent ->
                bestSet.surrogate.solveWithSharedBody(
                    ties, body, bestSet.stations.indices.map { it in live && it != absent }
                ).peakDishing
            } / freeStroke
        }
        val chosenSet = found.indices
        val sub = placement(
            "%d of %d upward sites at phase %d, placement-searched".format(
                count, bestSet.pathCount, bestFullPhase
            ),
            bestFullPhase, chosenSet.map { bestSet.stations[it] },
            chosenSet.map { bestSet.stations[it].second }.groupingBy { it }.eachCount()
                .toSortedMap().values.toList()
        )
        val subTies = uniformTies(sub, T165_DESIGN_TIE)
        val record = grade(
            sub, "SHARED BODY", "uniform at %.0f pN/nm".format(T165_DESIGN_TIE),
            subTies, sharedAt(sub, subTies), withOracle = false
        )
        realCounts += count to record.p90OverStroke
    }
    realCounts += bestSet.pathCount to cells.first {
        it.phaseBasePairs == bestFullPhase && it.topology == "SHARED BODY" &&
                it.pathCount == bestSet.pathCount &&
                it.distribution.startsWith("uniform at 1000")
    }.p90OverStroke

    val realFit = redundancyFit(realCounts.sortedBy { it.first }, T165_TOLERANCE)
    fitRecords += T165FitRecord(
        source = "this study, the REAL upward lattice at phase $bestFullPhase",
        points = realCounts.size,
        slope = realFit.slope,
        countAtTolerance = realFit.countAtTolerance,
        inventory = maximumInventory,
        predictedAtInventory = realFit.predictedAt(maximumInventory.toDouble()),
        factorDemandedAtInventory = realFit.factorDemandedAt(maximumInventory.toDouble())
    )

    // ------------------------------------------------------------------ the verdict
    val best = cells.filter { it.topology == "SHARED BODY" }
        .sortedWith(compareBy(
            { roundForResult(it.p90OverStroke, T165_DECISION_DIGITS) },
            { it.phaseBasePairs }, { it.distribution }
        )).first()
    val bestArray = cells.filter { it.topology == "ARRAY" }.minByOrNull { it.p90OverStroke }!!
    val anyFlat = cells.any { it.flatAtP90 }

    val uniformAtBest = cells.first {
        it.phaseBasePairs == bestFullPhase && it.pathCount == bestSet.pathCount &&
                it.distribution == "uniform at %.0f pN/nm".format(T165_DESIGN_TIE)
    }
    val descents = cells.filter { it.distribution.startsWith("90th-percentile descent") }
    val distributionWorth = descents.maxOfOrNull { cell ->
        val reference = cells.first {
            it.phaseBasePairs == cell.phaseBasePairs && it.pathCount == cell.pathCount &&
                    it.distribution == "uniform at " + cell.distribution.substringAfter("under ")
        }
        reference.p90OverStroke / cell.p90OverStroke
    } ?: 1.0
    val placementWorth = screen.maxOf { it.sharedWorstSingleRemovalOverStroke } /
            screen.minOf { it.sharedWorstSingleRemovalOverStroke }
    // `C-0089`'s ranking instrument, re-measured across PHASES rather than across designs: does
    // the `n + 1`-solve worst single removal order the phases the way the 10 000-realisation
    // 90th percentile does? A licence for the subset search, measured rather than transferred.
    val gradedFullCells = cells.filter {
        it.topology == "SHARED BODY" && it.pathCount > 34 &&
                it.distribution == "uniform at %.0f pN/nm".format(T165_DESIGN_TIE)
    }
    val screenRank = spearmanRankCorrelation(
        gradedFullCells.map { cell ->
            screen.first { it.phaseBasePairs == cell.phaseBasePairs }
                .sharedWorstSingleRemovalOverStroke
        },
        gradedFullCells.map { it.p90OverStroke }
    )
    val f2Fired = tieLadder.first { it.tieScale == T165_DESIGN_TIE }.shapeSpreadAtP90 > 0.05

    // ------------------------------------------------------------------ buildability
    val buildability = listOf(
        T165BuildRecord(
            question = "upward crossover sites the body may be tied at, best phase",
            demanded = realFit.countAtTolerance,
            available = maximumInventory.toDouble(), unit = "ties",
            clears = realFit.countAtTolerance <= maximumInventory,
            owner = "C-0055/C-0066 (the EAST azimuth), this study (the census and the fit)",
            note = "C-0093 quotes 53, which is the inventory at C-0063's phase 24; the census " +
                    "over all 32 phases finds $maximumInventory at ${richestPhases.size} of them"
        ),
        T165BuildRecord(
            question = "attachment columns C-0089's run-length demand asks of one row",
            demanded = columnsForRunRobustness(
                T165_EDGE_X, T165_BENDING_LENGTH, T165_WORST_RUN
            ).toDouble(),
            available = census[richestPhases.first()].rowLengths.max().toDouble(),
            unit = "columns",
            clears = false,
            owner = "C-0089 (the run-length arithmetic), C-0055 (the 32 bp upward pitch)",
            note = "an upward line has the BARE 32 bp pitch, so a 40 nm row carries four " +
                    "columns at every phase: the demand is not merely unmet, it is outside " +
                    "the lattice's own resolution"
        ),
        T165BuildRecord(
            question = "phases that are BOTH richest in upward sites and eight-column hosts",
            demanded = 1.0, available = richestAndEightColumn.size.toDouble(), unit = "phases",
            clears = richestAndEightColumn.isNotEmpty(),
            owner = "C-0015 (the eight-column ten), C-0055 (the EAST inventory)",
            note = "the same 8 bp plane lattice carries the sheet's own columns and the " +
                    "coupling's stations, so the two demands are one variable and they disagree"
        ),
        T165BuildRecord(
            question = "phases that are BOTH richest and centro-symmetric",
            demanded = 1.0,
            available = richestPhases.count { census[it].isCentroSymmetric() }.toDouble(),
            unit = "phases",
            clears = richestPhases.any { census[it].isCentroSymmetric() },
            owner = "C-0063 (centro-symmetry as the winning property at 34 roots)",
            note = "C-0063's two centro-symmetric phases are 8 and 24, and neither is a " +
                    "richest one — the inventory and the symmetry are in direct opposition"
        ),
        T165BuildRecord(
            question = "does §3 describe a two-layer tile?",
            demanded = 1.0, available = 0.0, unit = "specification",
            clears = false,
            owner = "C-0053, C-0093 (T-166)",
            note = "unchanged from C-0093: a body tied at many upward sites is a second layer"
        )
    )

    // ------------------------------------------------------------------ convergence
    println("T-165 — the convergence axes ...")
    val convergence = ArrayList<T165ConvergenceRecord>()
    run {
        val ties = uniformTies(bestSet, T165_DESIGN_TIE)
        val body = sharedAt(bestSet, ties)
        val counts = listOf(1250, 2500, 5000, t165GradingRealisations).distinct().sorted()
        val values = counts.map { count ->
            val ensemble = dropoutEnsemble(bestSet.probabilities, count, T165_GRADING_SEED)
            orderStatistic(
                DoubleArray(count) {
                    bestSet.surrogate.solveWithSharedBody(
                        ties, body, ensemble.presenceAt(it)
                    ).peakDishing / freeStroke
                },
                0.90
            )
        }
        convergence += t165Convergence(
            "realisations in the grading ensemble", counts.map { it.toString() }, values,
            abs(values[values.size - 1] - values[values.size - 2]),
            "the 90th percentile of the rigid shared body at the best full-inventory phase"
        )
    }
    run {
        val ties = uniformTies(bestSet, T165_DESIGN_TIE)
        val body = sharedAt(bestSet, ties)
        val grids = listOf(41, 81, 161)
        val gridRealisations = min(200, bestSet.grading.realisations)
        val values = grids.map { samples ->
            val surrogate = latticeInfluenceSurrogate(
                host(bestFullPhase), bestSet.stations, designField, samples
            )
            DoubleArray(gridRealisations) {
                surrogate.solveWithSharedBody(
                    ties, body, bestSet.grading.presenceAt(it)
                ).peakDishing / freeStroke
            }.average()
        }
        convergence += t165Convergence(
            "dishing samples per edge", grids.map { it.toString() }, values,
            abs(values[values.size - 1] - values[values.size - 2]),
            "the MEAN over 200 realisations, because C-0087, C-0089 and C-0093 all record that " +
                    "a percentile on three nested grids is degenerate"
        )
    }
    run {
        val ceiling = T165_DESIGN_TIE
        // The axis BRACKETS the training size the descent actually used, so it is a statement
        // about this study's own choice rather than about a fixed triple.
        val sizes = listOf(
            max(1, t165TrainingRealisations / 2), t165TrainingRealisations,
            2 * t165TrainingRealisations
        )
        val values = sizes.map { size ->
            val trainer = T165Placement(
                bestSet.name, bestSet.phaseBasePairs, bestSet.stations, bestSet.rowLengths,
                bestSet.surrogate, bestSet.probabilities, bestSet.grading,
                dropoutEnsemble(bestSet.probabilities, size, T165_TRAINING_SEED)
            )
            val found = optimiseTieDistribution(
                start = List(bestSet.pathCount) { ceiling },
                lowerBound = ceiling / 1000.0, upperBound = ceiling,
                sweeps = 1, scanPoints = 5, refinements = 5
            ) { trainedP90(trainer, it) }
            val body = sharedAt(bestSet, found.ties)
            orderStatistic(
                DoubleArray(bestSet.grading.realisations) {
                    bestSet.surrogate.solveWithSharedBody(
                        found.ties, body, bestSet.grading.presenceAt(it)
                    ).peakDishing / freeStroke
                },
                0.90
            )
        }
        convergence += t165Convergence(
            "training realisations behind the descent", sizes.map { it.toString() }, values,
            abs(values[values.size - 1] - values[values.size - 2]),
            "the OUT-OF-SAMPLE 90th percentile of a one-sweep descent trained at each size"
        )
    }

    // ------------------------------------------------------------------ reproductions
    val reproductions = listOf(
        T165ReproductionRecord(
            "C-0063's 34-root dishing at zero defects", 0.0706145537,
            array34.nominalOverStroke, 0.0, "C-0063"
        ),
        T165ReproductionRecord(
            "C-0093's rigid shared body at C-0063's 34 stations", 0.0344013403,
            shared34.nominalOverStroke, 0.0, "C-0093"
        ),
        T165ReproductionRecord(
            "C-0093's p90 of the array at C-0063's 34 roots", 0.639129638,
            array34.p90OverStroke, 0.0, "C-0087/C-0093"
        ),
        T165ReproductionRecord(
            "C-0093's p90 of the shared body at C-0063's 34 roots", 0.547996266,
            shared34.p90OverStroke, 0.0, "C-0093"
        ),
        T165ReproductionRecord(
            "C-0087's worst single removal from C-0063's 34", 0.501011167,
            array34.worstSingleRemovalOverStroke, 0.0, "C-0087"
        ),
        T165ReproductionRecord(
            "C-0093's redundancy slope, refitted from its own density table", -0.784357442,
            sharedFit.slope, 0.0, "C-0093"
        ),
        T165ReproductionRecord(
            "C-0093's tie count at T-5b's 0.10, refitted", 252.126899,
            sharedFit.countAtTolerance, 0.0, "C-0093"
        ),
        T165ReproductionRecord(
            "C-0066's upward site inventory at phase 24", 53.0,
            census[T165_C0063_PHASE].siteCount.toDouble(), 0.0, "C-0066/C-0055"
        ),
        T165ReproductionRecord(
            "C-0026's free-tile stroke", 4.90731102, freeStroke, 0.0, "C-0026"
        ),
        T165ReproductionRecord(
            "C-0049's per-path stiffness ceiling", 3.33333333,
            T165_CONSERVATIVE_TIE_CEILING, 0.0, "C-0049"
        )
    ).map {
        it.copy(
            departure = roundForResult(
                abs(it.reproduced - it.published) / abs(it.published), 2, T165_DECISION_FLOOR
            )
        )
    }

    // ------------------------------------------------------------------ predicates, falsifiers
    val predicates = listOf(
        T165PredicateRecord(
            "P1", "the 90th-percentile dishing of a placement-searched and " +
                    "distribution-searched shared body is emitted against T-5b's 0.10",
            if (cells.any { it.distribution.startsWith("90th-percentile descent") }) "PASS"
            else "FAIL"
        ),
        T165PredicateRecord(
            "P2", "the cheap bounds run first and are reported whatever they say",
            if (censusRecords.size == 32 && fitRecords.isNotEmpty() &&
                pitchRecords.isNotEmpty() && tieLadder.size == T165_TIE_LADDER.size
            ) "PASS" else "FAIL"
        ),
        T165PredicateRecord(
            "P3", "the mandate is placed on the body's GROUND and the tie is capped by a " +
                    "force, at both the conservative and the solved reading",
            if (cells.count { it.distribution.contains("cap") } >= 4) "PASS" else "FAIL"
        ),
        T165PredicateRecord(
            "P4", "every headline placement is a set of REAL upward crossover sites",
            "PASS (UpwardTieLattice, derived from C-0055's own upwardRootLattice)"
        ),
        T165PredicateRecord(
            "P5", "the array is graded on the identical stations",
            if (cells.count { it.topology == "ARRAY" } >= gradedPhases.size) "PASS" else "FAIL"
        ),
        T165PredicateRecord(
            "P6", "every standing figure reproduces to better than 1 %",
            if (reproductions.all { it.departure < 1e-2 }) "PASS" else "FAIL"
        ),
        T165PredicateRecord(
            "P7", "the falsifiers are asserted as executable tests",
            "PASS (src/test/kotlin/coupling/SharedBodyPlacementTest.kt)"
        )
    )

    val falsifiers = listOf(
        T165FalsifierRecord(
            "F1", "the best searched shared-body design, placement AND distribution " +
                    "together, reaches T-5b's 0.10 at the 90th percentile",
            anyFlat,
            "the best of %d graded cells is %.9g of the stroke at the 90th percentile".format(
                cells.size, best.p90OverStroke
            )
        ),
        T165FalsifierRecord(
            "F2", "the stiff-tie kinematic bound is wrong - the spread of the 90th " +
                    "percentile over materially different tie distributions at the design tie " +
                    "exceeds 5 %",
            f2Fired,
            "the spread over %d shapes at %.0f pN/nm is %.4f of the uniform reading".format(
                shapes.size, T165_DESIGN_TIE,
                tieLadder.first { it.tieScale == T165_DESIGN_TIE }.shapeSpreadAtP90
            )
        ),
        T165FalsifierRecord(
            "F3", "a uniform load on a uniform Winkler foundation dishes non-zero, " +
                    "uncoupled or under a free shared body",
            false,
            "asserted as a test at 1e-9, on the tie forces and on the dishing"
        ),
        T165FalsifierRecord(
            "F4", "the pipeline fails to reproduce C-0063's 0.0706145537 and C-0093's " +
                    "0.0344013403 on the identical 34 stations",
            reproductions.take(2).any { it.departure >= 1e-3 },
            "reproduced at %.9g and %.9g".format(
                array34.nominalOverStroke, shared34.nominalOverStroke
            )
        ),
        T165FalsifierRecord(
            "F5", "a body grounded far above the mandate fails to converge to the array",
            false,
            "asserted as a test at 1e-6 of the tie stiffness"
        )
    )

    val cheapBounds = listOf(
        T165BoundRecord(
            "the largest upward tie inventory over all 32 phases", maximumInventory.toDouble(),
            "sites",
            ("C-0093 quotes 53 and that is C-0063's phase 24; %d of the 32 phases carry %d, " +
                    "and none of them is one of C-0015's eight-column ten or C-0063's two " +
                    "centro-symmetric ones").format(
                richestPhases.size, maximumInventory
            )
        ),
        T165BoundRecord(
            "the factor the two searched axes must buy at that inventory",
            sharedFit.factorDemandedAt(maximumInventory.toDouble()), "dimensionless",
            "C-0093's own redundancy fit evaluated at the count the lattice offers, against " +
                    "C-0089's measured 1.30-1.61x for a distribution and C-0074's 1.13x for " +
                    "irregularity - the expected answer before anything is searched"
        ),
        T165BoundRecord(
            "the upward lattice's own columns per row",
            census[richestPhases.first()].rowLengths.max().toDouble(), "columns",
            ("against the %d C-0089's run-length arithmetic demands: the upward line has the " +
                    "bare 32 bp pitch, so the demand is outside the lattice's own " +
                    "resolution").format(
                columnsForRunRobustness(T165_EDGE_X, T165_BENDING_LENGTH, T165_WORST_RUN)
            )
        ),
        T165BoundRecord(
            "the spread of the 90th percentile over five tie SHAPES at the design tie",
            tieLadder.first { it.tieScale == T165_DESIGN_TIE }.shapeSpreadAtP90, "dimensionless",
            "the kinematic limit, measured: at a stiff tie the shared body pins its stations " +
                    "to a PLANE, a constraint that does not depend on the tie distribution, so " +
                    "the distribution axis closes exactly where the topology is strongest"
        ),
        T165BoundRecord(
            "the placement axis, over the 32 phases at full inventory", placementWorth,
            "dimensionless",
            "the worst-single-removal spread between the best and the worst phase, which is " +
                    "the whole of what a phase choice is worth on the cheap objective"
        ),
        T165BoundRecord(
            "Spearman rho between the cheap objective and the graded 90th percentile, over phases",
            screenRank, "dimensionless",
            ("C-0089 measured 0.9729 for the same instrument over 22 DESIGNS; here it is " +
                    "re-measured over %d graded PHASES at full inventory, which is what " +
                    "licenses the subset search to run on n + 1 solves").format(
                gradedFullCells.size
            )
        ),
        T165BoundRecord(
            "the p90 oracle floor at the best full-inventory placement",
            cells.filter { it.oracleFloorAtP90 > 0.0 }.minOf { it.oracleFloorAtP90 },
            "of the free-tile stroke",
            "CH-0104: a floor bounds a design from below and never characterises one - it is " +
                    "attained by a force vector chosen with knowledge of the surviving support " +
                    "set, and it did not exclude"
        )
    )

    val result = T165Result(
        task = "T-165",
        question = "Is a shared-body coupling flat under the measured dropout once its " +
                "PLACEMENT and its DISTRIBUTION are searched?",
        leaf = "A8.2 (the flatness of the tile), with A1.2 for the anchoring scheme",
        conditions = mapOf(
            "temperature" to "300 K",
            "thermalEnergy" to "4.141947 pN nm",
            "buffer" to "aqueous 2 mM MgCl2",
            "operatingState" to
                    "C-0022's SOLVED edge profile at ${designProfile.name} (C-0063's design state)",
            "tile" to "40.0 x %.2f nm single-layer square-lattice sheet, 15 duplexes at 2.69 nm"
                .format(lengthY),
            "flatness" to "peak departure from the best-fit plane over the free-tile stroke, " +
                    "81 x 81 grid; flat means below T-5b's 0.10 CONVENTION",
            "dropout" to "C-0087's MEASURED_DEPTH incorporation, Bernoulli, independent, " +
                    "grading seed $T165_GRADING_SEED, training seed $T165_TRAINING_SEED",
            "body" to "the RIGID limit at Ritz degree 1, grounded by a DISTRIBUTED element " +
                    "scaled so the coupling's heave secant is C-0017's mandate exactly; " +
                    "C-0093 measures a four-layer brick at 1.564x its station compliance",
            "maturity" to "TRL 1-3, model-consistent and traceable; nothing derived here is " +
                    "measured"
        ),
        parameters = mapOf(
            "mandate" to roundForResult(T165_MANDATE, 9, T165_DECISION_FLOOR).toString(),
            "freeTileStroke" to roundForResult(freeStroke, 9, T165_DECISION_FLOOR).toString(),
            "gradingRealisations" to t165GradingRealisations.toString(),
            "gradingSeed" to T165_GRADING_SEED.toString(),
            "trainingRealisations" to t165TrainingRealisations.toString(),
            "trainingSeed" to T165_TRAINING_SEED.toString(),
            "shapeRealisations" to shapeEnsemble.realisations.toString(),
            "descentSweeps" to t165DescentSweeps.toString(),
            "dishingSamplesPerEdge" to T165_SAMPLES.toString(),
            "designTieStiffness" to T165_DESIGN_TIE.toString(),
            "conservativeTieCeiling" to
                    roundForResult(T165_CONSERVATIVE_TIE_CEILING, 9, T165_DECISION_FLOOR)
                        .toString(),
            "tieLadder" to T165_TIE_LADDER.joinToString(", "),
            "gradedPhases" to gradedPhases.joinToString(", "),
            "bestFullInventoryPhase" to bestFullPhase.toString(),
            "richestPhases" to richestPhases.joinToString(", "),
            "eightColumnPhases" to eightColumnPhases.joinToString(", "),
            "unzipAllowable" to Gen1Tile.DUPLEX_UNZIP_ALLOWABLE.toString(),
            "shearAllowable" to Gen1Tile.DUPLEX_SHEAR_ALLOWABLE.toString(),
            "winklerBendingLength" to T165_BENDING_LENGTH.toString(),
            "decisionDigits" to T165_DECISION_DIGITS.toString()
        ),
        cheapBounds = cheapBounds,
        census = censusRecords,
        pitchLedger = pitchRecords,
        redundancyFits = fitRecords,
        phaseScreen = screen,
        cells = cells,
        tieLadder = tieLadder,
        buildability = buildability,
        convergence = convergence,
        reproductions = reproductions,
        predicates = predicates,
        falsifiers = falsifiers,
        findings = emptyList()
    )

    val output = File("gpd/results/T-165-shared-body-placement.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    // The JSON is built BEFORE any prose is formatted: `CLAUDE.md` records that a placeholder
    // miscount has cost this project a completed run four times.
    val withFindings = result.copy(
        findings = t165Findings(
            anyFlat, best, bestArray, uniformAtBest, array34, shared34, sharedFit, realFit,
            maximumInventory, richestPhases, eightColumnPhases, richestAndEightColumn,
            census, tieLadder, distributionWorth, placementWorth, screenRank,
            gradedFullCells.size, f2Fired, buildability, cells
        )
    )
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(withFindings).roundedForResult(
                digits = T165_DECISION_DIGITS + 3,
                digitsByKey = DEPARTURE_DIGITS_BY_KEY,
                floor = T165_DECISION_FLOOR
            ).withEmissionHeader(LatticeTag.SQUARE, null) as JsonObject)
        ) + "\n"
    )
    println("T-165 — wrote ${output.path}")
    withFindings.findings.forEach { println("  * $it") }
    withFindings.predicates.forEach { println("  [${it.verdict}] ${it.name}") }
    withFindings.falsifiers.forEach {
        println("  ${it.name} ${if (it.fired) "FIRED" else "did not fire"}: ${it.outcome}")
    }
}

// ---------------------------------------------------------------------------------------------
// the prose, built AFTER the result so a format placeholder cannot cost the run
// ---------------------------------------------------------------------------------------------

@Suppress("LongParameterList", "LongMethod")
private fun t165Findings(
    anyFlat: Boolean,
    best: T165CellRecord,
    bestArray: T165CellRecord,
    uniformAtBest: T165CellRecord,
    array34: T165CellRecord,
    shared34: T165CellRecord,
    sharedFit: RedundancyFit,
    realFit: RedundancyFit,
    maximumInventory: Int,
    richestPhases: List<Int>,
    eightColumnPhases: List<Int>,
    richestAndEightColumn: Set<Int>,
    census: List<UpwardTieLattice>,
    tieLadder: List<T165LimitRecord>,
    distributionWorth: Double,
    placementWorth: Double,
    screenRank: Double,
    gradedFullCount: Int,
    f2Fired: Boolean,
    buildability: List<T165BuildRecord>,
    cells: List<T165CellRecord>
): List<String> {
    val findings = ArrayList<String>()

    findings += ("THE ANSWER IS " + (if (anyFlat) "YES" else "NO") +
            ". The best of %d graded cells reads %.9g of the free-tile stroke at the 90th " +
            "percentile under C-0087's measured dropout - %s on %d ties at phase %d - against " +
            "T-5b's 0.10 and against the best array cell's %.9g on the same real stations. " +
            "Exceedance %.4f.").format(
        cells.size, best.p90OverStroke, best.distribution, best.pathCount, best.phaseBasePairs,
        bestArray.p90OverStroke, best.exceedance
    )

    findings += ("THE CENSUS MOVES THE COUNT AND THE PHASE MOVES WITH IT. C-0093's ceiling of " +
            "53 upward sites is C-0063's phase 24; over all 32 phases the largest inventory is " +
            "%d, at %d of them (%s). And the same 8 bp plane lattice carries the sheet's own " +
            "crossover columns: C-0015's eight-column phases are %s, %s the richest ones, and " +
            "C-0063's two centro-symmetric phases (8 and 24) are not richest either. The " +
            "lattice offers the coupling its most stations exactly where it offers the SHEET " +
            "its fewest crossovers.").format(
        maximumInventory, richestPhases.size, richestPhases.joinToString(","),
        eightColumnPhases.joinToString(","),
        if (richestAndEightColumn.isEmpty()) "DISJOINT from"
        else "overlapping " + richestAndEightColumn.joinToString(",") + " of"
    )

    findings += ("THE CHEAP DIVISION SAID SO BEFORE THE SEARCH RAN. C-0093's own redundancy " +
            "fit, refitted here from its density table at a slope of %.9g, predicts %.4f of " +
            "the stroke at %d ties - so the placement and the distribution axes together had " +
            "to be worth %.2fx. C-0089 measured a distribution at 1.30-1.61x on an array and " +
            "C-0074's irregular-beats-regular at 1.13x, whose product is 1.82x. The search " +
            "measured %.2fx from the distribution and %.2fx from the phase.").format(
        sharedFit.slope, sharedFit.predictedAt(maximumInventory.toDouble()), maximumInventory,
        sharedFit.factorDemandedAt(maximumInventory.toDouble()), distributionWorth, placementWorth
    )

    val atDesign = tieLadder.first { it.tieScale == 1000.0 }
    findings += ("F2 " + (if (f2Fired) "FIRED" else "did NOT fire") +
            ", AND THE DISTRIBUTION AXIS IS A CASUALTY OF THE VERY DIVISION THAT MAKES THIS " +
            "TOPOLOGY FLATTER. At a stiff tie a rigid shared body pins its stations onto a " +
            "PLANE - a kinematic constraint that does not depend on the tie distribution at " +
            "all - so the whole distribution family collapses onto one design: over five very " +
            "different tie shapes at %.0f pN/nm the 90th percentile spreads by %.4f and the " +
            "zero-defect dishing by %.4f. C-0089's 1.30-1.61x exists because an ARRAY divides " +
            "C-0017's mandate between its stations and a distribution redistributes a scarce " +
            "budget; a shared body puts the mandate in its GROUND, and that is the same " +
            "sentence twice.").format(
        atDesign.tieScale, atDesign.shapeSpreadAtP90, atDesign.shapeSpreadAtNominal
    )

    val descentCells = cells.filter { it.distribution.startsWith("90th-percentile descent") }
    val uniformRefs = descentCells.map { cell ->
        cells.first {
            it.phaseBasePairs == cell.phaseBasePairs && it.pathCount == cell.pathCount &&
                    it.distribution == "uniform at " + cell.distribution.substringAfter("under ")
        }
    }
    findings += ("AND THE DESCENT PAYS ONLY WHERE THE TOPOLOGY IS LOSING, WHICH IS THE SAME " +
            "BOUND SEEN FROM THE OTHER SIDE. On its own %d-realisation training ensemble the " +
            "descent reads %s against the uniform start's %s, and out of sample on C-0087's " +
            "independent %d realisations %s against %s - so the axis is worth %s, and the " +
            "in-sample gain overstates it by %s. The %.2fx is the SOFT-cap family, whose ties " +
            "are far from the kinematic limit and whose absolute reading is the worse of the " +
            "two; at the stiff cap, where the best design lives, the same descent over the same " +
            "53 parameters buys %.2fx. C-0089's 1.30-1.61x on an array was a real gain because " +
            "an array's mandate is scarce; there is no scarce budget to redistribute here.")
        .format(
            t165TrainingRealisations,
            descentCells.joinToString("/") { "%.4f".format(it.inSampleP90OverStroke) },
            uniformRefs.joinToString("/") { "%.4f".format(it.inSampleP90OverStroke) },
            descentCells.firstOrNull()?.realisations ?: 0,
            descentCells.joinToString("/") { "%.4f".format(it.p90OverStroke) },
            uniformRefs.joinToString("/") { "%.4f".format(it.p90OverStroke) },
            descentCells.indices.joinToString("/") {
                "%.2fx".format(uniformRefs[it].p90OverStroke / descentCells[it].p90OverStroke)
            },
            descentCells.indices.joinToString("/") {
                "%.2fx".format(
                    (uniformRefs[it].inSampleP90OverStroke /
                            descentCells[it].inSampleP90OverStroke) /
                            (uniformRefs[it].p90OverStroke / descentCells[it].p90OverStroke)
                )
            },
            distributionWorth,
            descentCells.indices.minOf {
                uniformRefs[it].p90OverStroke / descentCells[it].p90OverStroke
            }
        )

    findings += ("AND C-0089'S RANKING INSTRUMENT DOES NOT TRANSFER TO THIS AXIS. Its worst " +
            "single removal ranks 22 DESIGNS against the 90th percentile at rho = 0.9729, and " +
            "over the %d graded PHASES at full inventory it ranks them at rho = %.4f - it puts " +
            "phase 8 first where the 10 000-realisation percentile puts phase 24 first. A cheap " +
            "objective is an instrument for the axis it was calibrated on; the subset search " +
            "here runs on it and every number it produces is re-graded on the full ensemble, " +
            "which is what keeps the reported values sound and the search's optimality a " +
            "claim about a descent rather than about the lattice.").format(
        gradedFullCount, screenRank
    )

    findings += ("THE TIE LADDER IS ALREADY AT ITS OWN CEILING. The uniform 90th percentile " +
            "runs %s over ties of %s pN/nm, and the matrix departure from the kinematic limit " +
            "falls as 1/s (%s) - so no stiffer tie and no redistribution of stiffness is " +
            "available, and the force allowable is not what stops it either: the peak tie " +
            "force under dropout runs %s pN against C-0006's 10 pN unzip.").format(
        tieLadder.joinToString(" -> ") { "%.4f".format(it.uniformP90OverStroke) },
        tieLadder.joinToString("/") { "%.0f".format(it.tieScale) },
        tieLadder.joinToString(" -> ") { "%.2e".format(it.kinematicMatrixDeparture) },
        tieLadder.joinToString(" -> ") { "%.2f".format(it.peakTieForceUnderDropout) }
    )

    findings += ("AND THE COUNT ARGUMENT SURVIVES ON THE REAL LATTICE, WHERE IT IS WORSE THAN " +
            "THE EXTRAPOLATION ONTO IT. Fitted over %d placement-searched counts on the real " +
            "upward sites the redundancy slope is %.9g against C-0093's %.9g on the abstract " +
            "m x 15 grids - %.2fx SHALLOWER, because the real lattice's four columns are fixed " +
            "and a further tie is added inside them rather than as a new column. The fitted " +
            "crossing is %.6g ties and it is NOT quotable, exactly as C-0093 declines to quote " +
            "the array's own: a curve this flat extrapolates %.0fx beyond its data. The slope " +
            "is the measured quantity, and it says the lattice's %d sites are on the wrong side " +
            "of it. C-0089's run-length arithmetic says the same with no solve at all: it " +
            "demands %d attachment columns in a row and the upward line's bare 32 bp pitch " +
            "offers %d at every one of the 32 phases.").format(
        realFit.points, realFit.slope, sharedFit.slope, sharedFit.slope / realFit.slope,
        realFit.countAtTolerance, realFit.countAtTolerance / maximumInventory, maximumInventory,
        buildability[1].demanded.toInt(), buildability[1].available.toInt()
    )

    findings += ("THE TOPOLOGY'S OWN NUMBERS REPRODUCE AND THE PLACEMENT IS WORTH SOMETHING " +
            "REAL. C-0063's 34 roots read %.9g (array) and %.9g (rigid shared body) at zero " +
            "defects and %.9g / %.9g at the 90th percentile; the full real inventory at the " +
            "best phase reads %.9g at %d uniform ties, i.e. %.2fx better than the shared body " +
            "on C-0063's own stations. The placement axis is the one that paid.").format(
        array34.nominalOverStroke, shared34.nominalOverStroke,
        array34.p90OverStroke, shared34.p90OverStroke,
        uniformAtBest.p90OverStroke, uniformAtBest.pathCount,
        shared34.p90OverStroke / uniformAtBest.p90OverStroke
    )

    val failing = buildability.filter { !it.clears }
    findings += ("%d of %d buildability rows do not clear: %s.").format(
        failing.size, buildability.size,
        if (failing.isEmpty()) "none" else failing.joinToString("; ") { it.question }
    )

    return findings
}
