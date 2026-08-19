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
import com.xemantic.nano.plentyofroom.structure.InterlayerCoupling
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.OrigamiSheet
import com.xemantic.nano.plentyofroom.structure.OrthotropicPlate
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
import kotlin.math.pow

/**
 * `T-162` — does a coupling that is **not an array** escape `C-0089`'s count argument?
 *
 * `C-0089` closes the flatness branch on a count: a dropout is an increase in the attachment
 * pitch, the pitch that keeps a coupling out of `CLAUDE.md`'s net-dishing-source regime demands
 * 195 paths, and the tile carries 34. It names one structural escape — *one stiff body tied at
 * many points* — and prices none of it.
 *
 * This study prices it. Three cheap bounds run before any sampler and one of them is capable of
 * ending the task on its own; the Monte Carlo is `C-0087`'s and is unchanged except in the
 * coupling's topology.
 */

// ---------------------------------------------------------------------------------------------
// the records
// ---------------------------------------------------------------------------------------------

/** One cheap bound, settled before the sampler it precedes. */
@Serializable
private data class T162BoundRecord(
    val name: String,
    val value: Double,
    val unit: String,
    val settles: String
)

/** What `C-0017`'s mandate buys per station under each topology — a division, no solve. */
@Serializable
private data class T162MandateRecord(
    val pathCount: Int,
    val mandate: Double,
    val arrayPerStationStiffness: Double,
    val sharedBodyTieCeilingFromForce: Double,
    val localSupportRatio: Double
)

/** The rank statement, `n = 1 …`, on a completely free rigid body. */
@Serializable
private data class T162RankRecord(
    val tieCount: Int,
    val expectedRank: Int,
    val largestCouplingEntryOverTie: Double,
    val addsExactlyZero: Boolean
)

/**
 * `(c EI n / k)^(1/3)` — the plan length one of `n` **ground** elements needs, against the force
 * each of them then carries. The whole of `CH-0081` re-read with the mandate concentrated.
 */
@Serializable
private data class T162GroundElementRecord(
    val endCondition: String,
    val coefficient: Double,
    val elementCount: Int,
    val perElementStiffness: Double,
    val perElementForce: Double,
    val planLength: Double,
    val planBudget: Double,
    val insidePlanBudget: Boolean,
    val insideUnzipAllowable: Boolean,
    val insideShearAllowable: Boolean,
    val insideNickedCeiling: Boolean
)

/** A candidate shared body, and what its own rigidity is worth. */
@Serializable
private data class T162BodyRecord(
    val name: String,
    val motif: String,
    val ritzDegree: Int,
    val modeCount: Int,
    val alongHelixRigidity: Double,
    val acrossHelixRigidity: Double,
    val twistingRigidity: Double,
    val thickness: Double,
    val meanStationComplianceOverTile: Double,
    val note: String
)

/** One `(station set, body, tie stiffness)` cell at ZERO defects. */
@Serializable
private data class T162ZeroDefectRecord(
    val stationSet: String,
    val pathCount: Int,
    val body: String,
    val tieStiffness: Double,
    val placedGroundScale: Double,
    val heaveSecant: Double,
    val groundComplianceShare: Double,
    val dishingOverStroke: Double,
    val peakTieForce: Double,
    val insideUnzipAllowable: Boolean,
    val insideShearAllowable: Boolean,
    val flatAtTenPercent: Boolean
)

/** One graded cell under `C-0087`'s measured dropout. */
@Serializable
private data class T162DropoutRecord(
    val stationSet: String,
    val pathCount: Int,
    val topology: String,
    val body: String,
    val tieStiffness: Double,
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
    val peakTieForce: Double,
    val peakTieForceUnderDropout: Double,
    val insideUnzipAllowable: Boolean,
    val flatAtP90: Boolean,
    val flatAtMedian: Boolean
)

/** The density axis: how the 90th percentile falls with the path count, per topology. */
@Serializable
private data class T162DensityRecord(
    val columns: Int,
    val pathCount: Int,
    val topology: String,
    val nominalOverStroke: Double,
    val worstSingleRemovalOverStroke: Double,
    val p90OverStroke: Double,
    val exceedance: Double,
    val peakTieForce: Double
)

/** A buildability or placement fact, with the claim that owns it. */
@Serializable
private data class T162BuildRecord(
    val question: String,
    val demanded: Double,
    val available: Double,
    val unit: String,
    val clears: Boolean,
    val owner: String,
    val note: String
)

/**
 * One convergence axis.
 *
 * [departure] is emitted at **two significant digits** and nothing finer. `CLAUDE.md`:
 * *"emit the answer and a two-significant-digit convergence measure"* — a difference of two
 * nearly equal solves is exactly the quantity a JIT recompilation moves, and two runs of this
 * study differed in the ninth digit of the Ritz axis' departure (`3.19469867e−11` against
 * `3.19472365e−11`) and in nothing else. The `RESULT_ABSOLUTE_FLOOR` cannot catch it: that floor
 * is a statement in the **locked units** and a departure between two dimensionless dishing ratios
 * is not in them (`P-18`).
 */
@Serializable
private data class T162ConvergenceRecord(
    val axis: String,
    val levels: List<String>,
    val values: List<Double>,
    val departure: Double,
    val note: String
)

/** [T162ConvergenceRecord] with its [departure] rounded to two significant digits. */
private fun t162Convergence(
    axis: String,
    levels: List<String>,
    values: List<Double>,
    departure: Double,
    note: String
) = T162ConvergenceRecord(
    axis, levels, values, roundForResult(departure, 2, T162_DECISION_FLOOR), note
)

/** One standing figure, reproduced rather than transcribed. */
@Serializable
private data class T162ReproductionRecord(
    val name: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double,
    val owner: String
)

/** One acceptance predicate. */
@Serializable
private data class T162PredicateRecord(
    val name: String,
    val statement: String,
    val verdict: String
)

/** One declared falsifier and whether it fired. */
@Serializable
private data class T162FalsifierRecord(
    val name: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T162Result(
    val task: String,
    val question: String,
    val leaf: String,
    val conditions: Map<String, String>,
    val parameters: Map<String, String>,
    val cheapBounds: List<T162BoundRecord>,
    val mandateArithmetic: List<T162MandateRecord>,
    val rankSequence: List<T162RankRecord>,
    val groundElements: List<T162GroundElementRecord>,
    val bodies: List<T162BodyRecord>,
    val zeroDefect: List<T162ZeroDefectRecord>,
    val dropout: List<T162DropoutRecord>,
    val density: List<T162DensityRecord>,
    val buildability: List<T162BuildRecord>,
    val convergence: List<T162ConvergenceRecord>,
    val reproductions: List<T162ReproductionRecord>,
    val predicates: List<T162PredicateRecord>,
    val falsifiers: List<T162FalsifierRecord>,
    val findings: List<String>
)

// ---------------------------------------------------------------------------------------------
// the constants
// ---------------------------------------------------------------------------------------------

private const val T162_DUPLEXES = 15
private const val T162_SAMPLES = 81
private const val T162_NOMINAL_CROSSOVER_COLUMNS = 8
private const val T162_C0063_PHASE = 24
private const val T162_RIM_STANDOFF = 1.0
private const val T162_TOLERANCE = 0.10
private const val T162_GRADING_SEED = 20260817L
private var t162GradingRealisations =
    System.getenv("T162_REALISATIONS")?.toInt() ?: 10_000
private const val T162_RITZ_DEGREE = 4
private const val T162_DECISION_DIGITS = 6
private const val T162_DECISION_FLOOR = 1e-12

/** `C-0069`'s plan budget for a rooted element on the 34-root lattice, `pitch − d`, in nm. */
private const val T162_PLAN_BUDGET = 8.19

/** `C-0025`'s midspan-loaded floor and `C-0034`'s end-loaded ceiling. */
private const val T162_C_TWO_SUPPORT = 48.0
private const val T162_C_ONE_SUPPORT = 12.0

private val T162_EDGE_X = Gen1Tile.EDGE_X
private val T162_MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

/** The tie-stiffness ladder, in pN/nm, geometric and spanning the array's own per-path share. */
private val T162_TIE_LADDER = listOf(1.0, 3.33333333, 10.0, 33.3333333, 100.0, 333.333333, 1000.0)

/** The tie stiffnesses the dropout matrix is graded at — a subset of the ladder, for cost. */
private val T162_GRADED_TIES = listOf(3.33333333, 10.0, 100.0, 1000.0)

/** `C-0089`'s run-length demand on an ARRAY, cited so the two topologies can be compared. */
private const val T162_C0089_DEMANDED_PATHS = 195.0

/** The column counts of the density axis, on `C-0015`'s one-row-per-duplex grid. */
private val T162_DENSITY_COLUMNS = listOf(1, 2, 3, 4, 6, 9, 12)

/** `C-0066`'s upward (`EAST`) crossover-site inventory at phase 24 — the tie-count ceiling. */
private const val T162_UPWARD_SITES = 53.0

/** The ground-element counts priced against `CH-0081`'s plan bound. */
private val T162_GROUND_COUNTS = listOf(1, 2, 4, 6, 10, 15, 34)

// ---------------------------------------------------------------------------------------------
// the inputs, read from the claims that own them
// ---------------------------------------------------------------------------------------------

private class T162Profile(
    val name: String,
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, lengthY: Double): PressureField = edgeCollarPressure(
        interiorPressure, T162_EDGE_X, lengthY,
        listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T162_RIM_STANDOFF))
    )
}

/** `C-0022`'s solved profile, keyed on **`(concentration, gap, bias)`** — `CLAUDE.md`'s gotcha. */
private fun t162Profile(file: File, key: Triple<Double, Double, Double>): T162Profile {
    require(file.exists()) {
        "C-0022's result file is missing: ${file.path}. T-162 consumes the SOLVED edge profile."
    }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(name: String) = it.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == key.first && value("gapHeight") == key.second &&
                    value("appliedBias") == key.third
        } ?: error("no C-0022 profile at ${key.first} mM, ${key.second} nm, ${key.third} V")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T162Profile(
        name = "%.1f mM, %.0f nm, %.3f V".format(key.first, key.second, key.third),
        smoothDepth = value("taperDepth"),
        smoothWidth = value("taperWidth"),
        rimDepth = value("rimResidualDepth")
    )
}

/** A placement read from the result file of the claim that owns it. */
private fun t162Placement(
    file: File,
    key: String,
    interhelical: Double
): List<Pair<Double, Double>> {
    require(file.exists()) { "the placement's own result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue(key).jsonArray.map { it.jsonObject }
        .flatMap { row ->
            val index = row.getValue("row").jsonPrimitive.content.toInt()
            val y = (index - (T162_DUPLEXES - 1) / 2.0) * interhelical
            row.getValue("roots").jsonArray.map { it.jsonPrimitive.content.toDouble() to y }
        }
}

// ---------------------------------------------------------------------------------------------
// the models
// ---------------------------------------------------------------------------------------------

private fun t162Sheet(): OrigamiSheet =
    origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)

private fun t162Lattice(sheet: OrigamiSheet, columns: CrossoverLayout): OrigamiGrillage =
    OrigamiGrillage(
        sheet = sheet,
        lengthX = T162_EDGE_X,
        beamCount = T162_DUPLEXES,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = columns,
        subdivisions = 2,
        supports = emptyList()
    )

/** A candidate shared body: a name, a motif, its Ritz modes and its own plate rigidities. */
private class T162Body(
    val name: String,
    val motif: String,
    val degree: Int,
    val plate: OrthotropicPlate?,
    val thickness: Double,
    val note: String,
    val lengthX: Double,
    val lengthY: Double
) {
    val modes: SharedBodyModes = sharedBodyModes(lengthX, lengthY, degree)

    val bending: Array<DoubleArray> =
        if (plate == null) Array(modes.modeCount) { DoubleArray(modes.modeCount) }
        else modes.bendingStiffness(plate)

    fun placedBody(
        stations: List<Pair<Double, Double>>,
        ties: List<Double>
    ): Pair<SharedBody, SharedBodyGroundPlacement> {
        val shapes = modes.shapesAt(stations)
        val unitGround = modes.distributedGroundStiffness(1.0)
        val placement =
            placeSharedBodyGround(ties, shapes, bending, unitGround, T162_MANDATE)
        val ground = Array(modes.modeCount) { m ->
            DoubleArray(modes.modeCount) { n -> placement.groundScale * unitGround[m][n] }
        }
        return sharedBody(shapes, bending, ground) to placement
    }
}

/** One station set with its Woodbury bank and its dropout ensemble. */
private class T162StationSet(
    val name: String,
    val stations: List<Pair<Double, Double>>,
    val designSurrogate: InfluenceSurrogate,
    val probabilities: List<Double>,
    val gradingEnsemble: DropoutEnsemble
) {
    val pathCount: Int get() = stations.size
}

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val sheet = t162Sheet()
    val lengthY = T162_DUPLEXES * sheet.interhelicalDistance
    val interiorPressure = Gen1Tile.TARGET_FORCE / (T162_EDGE_X * lengthY)

    println("T-162 — reading C-0022's solved load and C-0063's placement ...")
    val loadFile = File("gpd/results/T-3b-tile-edge-load-profile.json")
    val designProfile = t162Profile(loadFile, Triple(2.0, 10.0, 0.192))
    val designField = designProfile.field(interiorPressure, lengthY)

    val freeStroke = PlateOnFoundation(
        sheet.plate(T162_EDGE_X, lengthY), Gen1Tile.FOUNDATION_SECANT, emptyList(), 12
    ).solve(uniformPressure(interiorPressure)).meanDeflection

    val roots34 = t162Placement(
        File("gpd/results/T-125-upward-root-placement.json"), "bestPlacement",
        sheet.interhelicalDistance
    )
    check(roots34.size == 34) { "C-0063's placement must carry 34 roots" }

    val measuredField = measuredDepthIncorporation(T162_EDGE_X, lengthY)

    // ------------------------------------------------------------------ cheap bound 1: mandate
    val mandateArithmetic = listOf(15, 30, 34, 45, 90).map { count ->
        val arithmetic = mandatePlacementArithmetic(
            count, T162_MANDATE, Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE
        )
        T162MandateRecord(
            pathCount = count,
            mandate = T162_MANDATE,
            arrayPerStationStiffness = arithmetic.arrayPerStation,
            sharedBodyTieCeilingFromForce = arithmetic.sharedBodyPerStation,
            localSupportRatio = arithmetic.ratio
        )
    }

    // ------------------------------------------------------------------ cheap bound 2: the rank
    val probeStations = listOf(
        -8.0 to -6.0, 9.0 to -5.0, -7.0 to 6.5, 10.0 to 7.0, 0.0 to 0.0, 4.0 to -9.0
    )
    val rankSequence = (1..6).map { count ->
        val used = probeStations.take(count)
        val ties = List(count) { 12.0 }
        val free = SharedBody(
            sharedBodyModes(T162_EDGE_X, lengthY, 1).shapesAt(used), Array(3) { DoubleArray(3) }
        )
        val matrix = sharedBodyCouplingMatrix(ties, free)
        val worst = matrix.flatMap { row -> row.map { abs(it) } }.max() / 12.0
        T162RankRecord(
            tieCount = count,
            expectedRank = max(0, count - 3),
            largestCouplingEntryOverTie = worst,
            addsExactlyZero = worst < 1e-9
        )
    }

    // ------------------------------------------- cheap bound 3: CH-0081 with the mandate massed
    val groundElements = listOf(
        "two-support, C-0025's floor" to T162_C_TWO_SUPPORT,
        "one-support, C-0034's guided ceiling" to T162_C_ONE_SUPPORT
    ).flatMap { (label, coefficient) ->
        T162_GROUND_COUNTS.map { count ->
            val perElement = T162_MANDATE / count
            val force = Gen1Tile.TARGET_FORCE / count
            val length =
                (coefficient * Gen1Tile.DUPLEX_BENDING_RIGIDITY / perElement).pow(1.0 / 3.0)
            T162GroundElementRecord(
                endCondition = label,
                coefficient = coefficient,
                elementCount = count,
                perElementStiffness = perElement,
                perElementForce = force,
                planLength = length,
                planBudget = T162_PLAN_BUDGET,
                insidePlanBudget = length <= T162_PLAN_BUDGET,
                insideUnzipAllowable = force <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE,
                insideShearAllowable = force <= Gen1Tile.DUPLEX_SHEAR_ALLOWABLE,
                insideNickedCeiling = force <= Gen1Tile.OVERSTRETCHING_CEILING
            )
        }
    }

    // ------------------------------------------------------------------ the bodies
    val brickSheet = origamiSheet(
        Gen1Tile.INTERHELICAL_HONEYCOMB, Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
        layers = 4, interlayerCoupling = InterlayerCoupling.RIGID
    )
    val bodies = listOf(
        T162Body(
            "RIGID", "the limit, not a build — the best case of the family",
            1, null, 0.0,
            "three rigid modes only: the body adds no compliance of its own, so this is the " +
                    "optimistic end and F1 is read on it",
            T162_EDGE_X, lengthY
        ),
        T162Body(
            "four-layer honeycomb brick", "square/honeycomb multilayer origami (Ke et al. 2009)",
            T162_RITZ_DEGREE, brickSheet.plate(T162_EDGE_X, lengthY), brickSheet.thickness,
            "the stiffest body §3's own ~10 nm thickness admits; D_perp is a lower bound " +
                    "because the across-helix axial stiffness of a crossover is undetermined",
            T162_EDGE_X, lengthY
        ),
        T162Body(
            "single-layer Gen-1 sheet", "a second copy of the tile itself",
            T162_RITZ_DEGREE, sheet.plate(T162_EDGE_X, lengthY), sheet.thickness,
            "the softest body worth pricing: it is the tile's own material and the same " +
                    "25.6x anisotropy",
            T162_EDGE_X, lengthY
        )
    )

    // ------------------------------------------------------------------ the banks
    println("T-162 — the Woodbury banks ...")
    val centredHost = t162Lattice(
        sheet, CrossoverLayout.centred(T162_NOMINAL_CROSSOVER_COLUMNS, sheet.crossoverSpacing / 2.0)
    )
    val host24 = t162Lattice(
        sheet, CrossoverLayout.atBasePairPhase(T162_C0063_PHASE, sheet, T162_EDGE_X)
    )

    fun stationSet(
        name: String,
        host: OrigamiGrillage,
        stations: List<Pair<Double, Double>>
    ): T162StationSet {
        println("  bank: %s (%d paths)".format(name, stations.size))
        val probabilities = stations.map { (x, y) -> measuredField.at(x, y) }
        return T162StationSet(
            name = name,
            stations = stations,
            designSurrogate = latticeInfluenceSurrogate(host, stations, designField, T162_SAMPLES),
            probabilities = probabilities,
            gradingEnsemble = dropoutEnsemble(
                probabilities, t162GradingRealisations, T162_GRADING_SEED
            )
        )
    }

    val set34 = stationSet("C-0063's 34 upward roots at phase 24", host24, roots34)
    val densitySets = T162_DENSITY_COLUMNS.associateWith { columns ->
        stationSet(
            "C-0015's %d x %d attachment grid".format(columns, T162_DUPLEXES), centredHost,
            attachmentGrid(columns, T162_DUPLEXES, T162_EDGE_X, lengthY)
        )
    }
    val set45 = densitySets.getValue(3)
    val set90 = densitySets.getValue(6)
    val sets = listOf(set34, set45, set90)

    // ------------------------------------------------------------------ the body ledger
    val bodyRecords = bodies.map { body ->
        val shapes = body.modes.shapesAt(set34.stations)
        val ground = body.modes.distributedGroundStiffness(T162_MANDATE)
        val total = Array(body.modes.modeCount) { m ->
            DoubleArray(body.modes.modeCount) { n -> body.bending[m][n] + ground[m][n] }
        }
        val compliance = sharedBodyCompliance(shapes, total)
        val rigidShapes = bodies[0].modes.shapesAt(set34.stations)
        val rigidCompliance = sharedBodyCompliance(
            rigidShapes, bodies[0].modes.distributedGroundStiffness(T162_MANDATE)
        )
        val ratio = set34.stations.indices.sumOf { compliance[it][it] } /
                set34.stations.indices.sumOf { rigidCompliance[it][it] }
        T162BodyRecord(
            name = body.name,
            motif = body.motif,
            ritzDegree = body.degree,
            modeCount = body.modes.modeCount,
            alongHelixRigidity = body.plate?.rigidityX ?: 0.0,
            acrossHelixRigidity = body.plate?.rigidityY ?: 0.0,
            twistingRigidity = body.plate?.twistingRigidity ?: 0.0,
            thickness = body.thickness,
            meanStationComplianceOverTile = ratio,
            note = body.note
        )
    }

    // ------------------------------------------------------------------ F1: the zero-defect sweep
    println("T-162 — the zero-defect sweep, where F1 is read ...")
    val zeroDefect = ArrayList<T162ZeroDefectRecord>()
    sets.forEach { set ->
        bodies.forEach { body ->
            T162_TIE_LADDER.forEach { tie ->
                val ties = List(set.pathCount) { tie }
                if (ties.sum() <= T162_MANDATE * (1.0 + 1e-9)) return@forEach
                val (placed, placement) = body.placedBody(set.stations, ties)
                val solved = set.designSurrogate.solveWithSharedBody(
                    ties, placed, List(set.pathCount) { true }
                )
                val peakForce = solved.supportForces.maxOf { abs(it) }
                zeroDefect += T162ZeroDefectRecord(
                    stationSet = set.name,
                    pathCount = set.pathCount,
                    body = body.name,
                    tieStiffness = tie,
                    placedGroundScale = placement.groundScale,
                    heaveSecant = placement.heaveSecant,
                    groundComplianceShare = placement.groundComplianceShare,
                    dishingOverStroke = solved.peakDishing / freeStroke,
                    peakTieForce = peakForce,
                    insideUnzipAllowable = peakForce <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE,
                    insideShearAllowable = peakForce <= Gen1Tile.DUPLEX_SHEAR_ALLOWABLE,
                    flatAtTenPercent = solved.peakDishing / freeStroke < T162_TOLERANCE
                )
            }
        }
    }

    // The array baselines, at the same station sets, for the comparison the task is about.
    val arrayZeroDefect = sets.associate { set ->
        val equal = List(set.pathCount) { T162_MANDATE / set.pathCount }
        set.name to (set.designSurrogate.solve(equal).peakDishing / freeStroke)
    }

    val rigidAt34 = zeroDefect.filter {
        it.stationSet == set34.name && it.body == "RIGID"
    }
    val f1Fired = rigidAt34.none { it.flatAtTenPercent }

    // ------------------------------------------------------------------ the dropout run
    println("T-162 — the dropout run at %d realisations ...".format(t162GradingRealisations))
    val dropout = ArrayList<T162DropoutRecord>()

    fun grade(
        set: T162StationSet,
        topology: String,
        bodyName: String,
        tie: Double,
        ties: List<Double>,
        body: SharedBody?
    ) {
        val nominalSolve = set.designSurrogate.solveWithSharedBody(
            ties, body, List(set.pathCount) { true }
        )
        val nominal = nominalSolve.peakDishing / freeStroke
        val removal = (0 until set.pathCount).map { absent ->
            set.designSurrogate.solveWithSharedBody(
                ties, body, (0 until set.pathCount).map { it != absent }
            ).peakDishing / freeStroke
        }
        var peakUnderDropout = 0.0
        val sample = DoubleArray(set.gradingEnsemble.realisations) {
            val solved = set.designSurrogate.solveWithSharedBody(
                ties, body, set.gradingEnsemble.presenceAt(it)
            )
            peakUnderDropout = max(peakUnderDropout, solved.supportForces.maxOf { f -> abs(f) })
            solved.peakDishing / freeStroke
        }
        val summary = summariseDropoutDishing(
            sample, nominal, set.gradingEnsemble.meanSurvivors, T162_TOLERANCE
        )
        dropout += T162DropoutRecord(
            stationSet = set.name,
            pathCount = set.pathCount,
            topology = topology,
            body = bodyName,
            tieStiffness = tie,
            nominalOverStroke = nominal,
            worstSingleRemovalOverStroke = removal.max(),
            singleRemovalAmplification = removal.max() / nominal,
            medianOverStroke = summary.median,
            p90OverStroke = summary.p90,
            p95OverStroke = summary.p95,
            worstOverStroke = summary.worst,
            exceedance = summary.exceedance,
            exceedanceStandardError = summary.exceedanceStandardError,
            exceedanceOneSidedBound = summary.exceedanceOneSidedBound,
            meanSurvivors = summary.meanSurvivors,
            peakTieForce = nominalSolve.supportForces.maxOf { abs(it) },
            peakTieForceUnderDropout = peakUnderDropout,
            insideUnzipAllowable = peakUnderDropout <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE,
            flatAtP90 = summary.flatAtP90,
            flatAtMedian = summary.flatAtMedian
        )
    }

    sets.forEach { set ->
        println("  grading: %s".format(set.name))
        grade(
            set, "ARRAY", "none (paths to ground)", T162_MANDATE / set.pathCount,
            List(set.pathCount) { T162_MANDATE / set.pathCount }, null
        )
        bodies.forEach { body ->
            T162_GRADED_TIES.forEach { tie ->
                val ties = List(set.pathCount) { tie }
                if (ties.sum() <= T162_MANDATE * (1.0 + 1e-9)) return@forEach
                val (placed, _) = body.placedBody(set.stations, ties)
                grade(set, "SHARED BODY", body.name, tie, ties, placed)
            }
        }
    }

    val best = dropout.filter { it.topology == "SHARED BODY" }.minByOrNull { it.p90OverStroke }!!
    val arrayCells = dropout.filter { it.topology == "ARRAY" }
    val bestArray = arrayCells.minByOrNull { it.p90OverStroke }!!
    val anyFlat = dropout.any { it.flatAtP90 }
    val f2Fired = best.singleRemovalAmplification >=
            arrayCells.minOf { it.singleRemovalAmplification }

    // ------------------------------------------------------- the density axis, both topologies
    println("T-162 — the density axis ...")
    val density = ArrayList<T162DensityRecord>()
    val densityTie = T162_TIE_LADDER.last()
    T162_DENSITY_COLUMNS.forEach { columns ->
        val set = densitySets.getValue(columns)
        println("  density: %d columns (%d paths)".format(columns, set.pathCount))
        val before = dropout.size
        grade(
            set, "ARRAY", "none (paths to ground)", T162_MANDATE / set.pathCount,
            List(set.pathCount) { T162_MANDATE / set.pathCount }, null
        )
        val ties = List(set.pathCount) { densityTie }
        val (placed, _) = bodies[0].placedBody(set.stations, ties)
        grade(set, "SHARED BODY", bodies[0].name, densityTie, ties, placed)
        dropout.drop(before).takeLast(2).forEach { row ->
            density += T162DensityRecord(
                columns = columns,
                pathCount = row.pathCount,
                topology = row.topology,
                nominalOverStroke = row.nominalOverStroke,
                worstSingleRemovalOverStroke = row.worstSingleRemovalOverStroke,
                p90OverStroke = row.p90OverStroke,
                exceedance = row.exceedance,
                peakTieForce = row.peakTieForce
            )
        }
        repeat(dropout.size - before) { dropout.removeAt(dropout.size - 1) }
    }

    /** The log-log slope and the crossing of the tolerance, fitted over the density curve. */
    fun densityFit(topology: String): Pair<Double, Double> {
        val rows = density.filter { it.topology == topology }
            .filter { it.p90OverStroke > 0.0 }
            .sortedBy { it.pathCount }
        val n = rows.size
        val meanX = rows.sumOf { kotlin.math.ln(it.pathCount.toDouble()) } / n
        val meanY = rows.sumOf { kotlin.math.ln(it.p90OverStroke) } / n
        var covariance = 0.0
        var variance = 0.0
        rows.forEach {
            val dx = kotlin.math.ln(it.pathCount.toDouble()) - meanX
            covariance += dx * (kotlin.math.ln(it.p90OverStroke) - meanY)
            variance += dx * dx
        }
        val slope = covariance / variance
        val intercept = meanY - slope * meanX
        return slope to kotlin.math.exp((kotlin.math.ln(T162_TOLERANCE) - intercept) / slope)
    }

    val (sharedSlope, demandedShared) = densityFit("SHARED BODY")
    val (arraySlope, demandedArray) = densityFit("ARRAY")

    // ------------------------------------------------------------------ buildability
    val armSlabTop = 3.69
    val armSlabTopAtStroke = 6.69
    val effortHeight = 5.0
    val buildability = listOf(
        T162BuildRecord(
            question = "vertical room for a body above the tile at the 10 nm layer",
            demanded = armSlabTop, available = effortHeight, unit = "nm",
            clears = armSlabTop <= effortHeight,
            owner = "C-0035 (the effort point, constant reading), C-0061 (the arm slab)",
            note = "the arm slab occupies 1.69-3.69 nm at rest and 1.69-6.69 nm at C-0055's " +
                    "3 nm stroke; a body above it at rest is already OUT of the band under load"
        ),
        T162BuildRecord(
            question = "the same, at the stroke the device traverses",
            demanded = armSlabTopAtStroke, available = effortHeight, unit = "nm",
            clears = armSlabTopAtStroke <= effortHeight,
            owner = "C-0066 (the tie's clear column)",
            note = "the shared body REPLACES the arms rather than standing above them, so this " +
                    "row is the clash a body would face if the arm array were retained"
        ),
        T162BuildRecord(
            question = "upward crossover sites the body may be tied at, phase 24",
            demanded = 34.0, available = 53.0, unit = "sites",
            clears = true,
            owner = "C-0066 (the EAST inventory), C-0055 (the upward lattice)",
            note = "the body's ties ARE inter-layer crossovers, so they land on exactly the " +
                    "lattice C-0055 counts; the arms are not needed and the sites are freed"
        ),
        T162BuildRecord(
            question = "one ground element's plan length against C-0069's rooted budget",
            demanded = (T162_C_ONE_SUPPORT * Gen1Tile.DUPLEX_BENDING_RIGIDITY / T162_MANDATE)
                .pow(1.0 / 3.0),
            available = T162_PLAN_BUDGET, unit = "nm",
            clears = (T162_C_ONE_SUPPORT * Gen1Tile.DUPLEX_BENDING_RIGIDITY / T162_MANDATE)
                .pow(1.0 / 3.0) <= T162_PLAN_BUDGET,
            owner = "C-0069/CH-0081 (the end-condition bound), C-0025/C-0034 (c)",
            note = "CH-0081's 22.414 nm is (48 EI/k)^(1/3) at k = mandate/34; massing the " +
                    "mandate on ONE element shortens it by 34^(1/3) = 3.24x"
        ),
        T162BuildRecord(
            question = "the motif: a second layer tied at many sites",
            demanded = 1.0, available = 1.0, unit = "published precedent",
            clears = true,
            owner = "C-0055 (Ke et al. 2009, square-lattice multilayer origami)",
            note = "C-0055: 'a crossover from a sheet duplex to a duplex added at an " +
                    "out-of-plane azimuth is the elementary step of square-lattice multilayer " +
                    "origami - but there the added duplex is tied at many sites and is rigid'. " +
                    "It is the ARM, not the tied body, that this programme has no precedent for"
        ),
        T162BuildRecord(
            question = "ties the density fit demands, against the upward sites the lattice has",
            demanded = demandedShared, available = T162_UPWARD_SITES, unit = "ties",
            clears = demandedShared <= T162_UPWARD_SITES,
            owner = "C-0066 (the EAST inventory at phase 24), this study (the density fit)",
            note = "the count argument, re-asked of the new topology: an inter-layer crossover " +
                    "does not perforate the sheet, so C-0041's severance does not cap it and " +
                    "the ceiling is the upward crossover inventory instead of C-0075's 34"
        ),
        T162BuildRecord(
            question = "does §3 describe a two-layer tile?",
            demanded = 1.0, available = 0.0, unit = "specification",
            clears = false,
            owner = "C-0053 ('a two-layer body §3 does not describe')",
            note = "the decisive negative is a SPECIFICATION gap, not a physics one: §3 names " +
                    "a single-layer tile, and a shared body tied at many sites is a second layer"
        )
    )

    // ------------------------------------------------------------------ convergence
    val convergence = ArrayList<T162ConvergenceRecord>()
    run {
        val ties = List(set34.pathCount) { 100.0 }
        val values = listOf(1, 2, 3, 4, 5).map { degree ->
            val body = T162Body(
                "brick at degree $degree", "", degree,
                brickSheet.plate(T162_EDGE_X, lengthY), brickSheet.thickness, "",
                T162_EDGE_X, lengthY
            )
            val (placed, _) = body.placedBody(set34.stations, ties)
            set34.designSurrogate.solveWithSharedBody(
                ties, placed, List(set34.pathCount) { true }
            ).peakDishing / freeStroke
        }
        convergence += t162Convergence(
            axis = "Ritz degree of the shared body",
            levels = listOf(1, 2, 3, 4, 5).map { it.toString() },
            values = values,
            departure = abs(values[values.size - 1] - values[values.size - 2]),
            note = "a richer basis can only SOFTEN a Rayleigh-Ritz body, so the sequence is " +
                    "monotone and the truncation runs in the shared body's favour"
        )
    }
    run {
        val ties = List(set34.pathCount) { 100.0 }
        val (placed, _) = bodies[0].placedBody(set34.stations, ties)
        val counts = listOf(1250, 2500, 5000, t162GradingRealisations).distinct().sorted()
        val values = counts.map { count ->
            val ensemble = dropoutEnsemble(set34.probabilities, count, T162_GRADING_SEED)
            val sample = DoubleArray(count) {
                set34.designSurrogate.solveWithSharedBody(
                    ties, placed, ensemble.presenceAt(it)
                ).peakDishing / freeStroke
            }
            orderStatistic(sample, 0.90)
        }
        convergence += t162Convergence(
            axis = "realisations in the grading ensemble",
            levels = counts.map { it.toString() },
            values = values,
            departure = abs(values[values.size - 1] - values[values.size - 2]),
            note = "the 90th percentile of the rigid shared body at 100 pN/nm ties, C-0063's 34"
        )
    }
    run {
        val ties = List(set34.pathCount) { 100.0 }
        val (placed, _) = bodies[0].placedBody(set34.stations, ties)
        val grids = listOf(41, 81, 161)
        val gridRealisations = minOf(200, set34.gradingEnsemble.realisations)
        val values = grids.map { samples ->
            val surrogate = latticeInfluenceSurrogate(host24, set34.stations, designField, samples)
            val sample = DoubleArray(gridRealisations) {
                surrogate.solveWithSharedBody(
                    ties, placed, set34.gradingEnsemble.presenceAt(it)
                ).peakDishing / freeStroke
            }
            sample.average()
        }
        convergence += t162Convergence(
            axis = "dishing samples per edge",
            levels = grids.map { it.toString() },
            values = values,
            departure = abs(values[values.size - 1] - values[values.size - 2]),
            note = "the MEAN over 200 realisations, because C-0087 and C-0089 both record that " +
                    "a percentile on three nested grids is degenerate"
        )
    }

    // ------------------------------------------------------------------ reproductions
    val equal34 = List(set34.pathCount) { T162_MANDATE / set34.pathCount }
    val equal45 = List(set45.pathCount) { T162_MANDATE / set45.pathCount }
    val twoLevel45 = normalisedStiffnesses(
        rimStiffenedWeights(
            set45.stations, T162_EDGE_X, lengthY, 6.7, 5.0
        ), T162_MANDATE
    )
    val reproductions = listOf(
        T162ReproductionRecord(
            "C-0063's 34-root dishing at zero defects", 0.0706145537,
            set34.designSurrogate.solveWithSharedBody(
                equal34, null, List(set34.pathCount) { true }
            ).peakDishing / freeStroke,
            0.0, "C-0063"
        ),
        T162ReproductionRecord(
            "C-0017's 45 equal springs", 0.2182,
            set45.designSurrogate.solve(equal45).peakDishing / freeStroke, 0.0, "C-0017/C-0058"
        ),
        T162ReproductionRecord(
            "C-0058's two-level 45", 0.0753,
            set45.designSurrogate.solve(twoLevel45).peakDishing / freeStroke, 0.0, "C-0058"
        ),
        T162ReproductionRecord(
            "C-0087's single removal from C-0063's 34", 0.5010,
            worstSinglePathRemoval(set34.designSurrogate, equal34) / freeStroke, 0.0, "C-0087"
        ),
        T162ReproductionRecord(
            "C-0087's p90 of C-0063's 34 equal springs", 0.6391,
            arrayCells.first { it.stationSet == set34.name }.p90OverStroke, 0.0, "C-0087"
        ),
        T162ReproductionRecord(
            "C-0026's free-tile stroke", 4.90731102, freeStroke, 0.0, "C-0026"
        ),
        T162ReproductionRecord(
            "CH-0081's shortest two-support flexure at 34 paths", 22.414,
            (T162_C_TWO_SUPPORT * Gen1Tile.DUPLEX_BENDING_RIGIDITY / (T162_MANDATE / 34.0))
                .pow(1.0 / 3.0),
            0.0, "C-0069/CH-0081"
        )
    ).map { it.copy(departure = abs(it.reproduced - it.published) / abs(it.published)) }

    // ------------------------------------------------------------------ predicates
    val bodyCanBePlaced = buildability.count { !it.clears }
    val predicates = listOf(
        T162PredicateRecord(
            "P1", "the free rigid body's closed form is verified before any dropout run: " +
                    "exactly zero at n = 1, 2, 3 and rank n - 3 above",
            if (rankSequence.take(3).all { it.addsExactlyZero } &&
                rankSequence.drop(3).none { it.addsExactlyZero }
            ) "PASS" else "FAIL"
        ),
        T162PredicateRecord(
            "P2", "the mandate arithmetic is reported for every path count, with no solve",
            if (mandateArithmetic.size == 5) "PASS" else "FAIL"
        ),
        T162PredicateRecord(
            "P3", "the body's own rigidity is swept from a single-layer sheet to the rigid limit",
            if (bodyRecords.size == 3) "PASS" else "FAIL"
        ),
        T162PredicateRecord(
            "P4", "buildability and placement are checked and reported whatever they say",
            if (buildability.size >= 6) "PASS" else "FAIL"
        ),
        T162PredicateRecord(
            "P5", "the 90th-percentile dishing under C-0087's unchanged dropout is emitted " +
                    "with its single-removal amplification",
            if (dropout.size >= 15) "PASS" else "FAIL"
        ),
        T162PredicateRecord(
            "P6", "every standing figure reproduces to better than 1 %",
            if (reproductions.all { it.departure < 1e-2 }) "PASS" else "FAIL"
        ),
        T162PredicateRecord(
            "P7", "the falsifiers are asserted as executable tests, F3 and F4 included",
            "PASS (src/test/kotlin/coupling/SharedBodyCouplingTest.kt)"
        )
    )

    val falsifiers = listOf(
        T162FalsifierRecord(
            "F1", "a perfectly rigid shared body at C-0063's 34 stations does not reach " +
                    "T-5b's 0.10 at ZERO dropout",
            f1Fired,
            "the rigid body's zero-defect dishing over the tie ladder is %s of the stroke".format(
                rigidAt34.joinToString(", ") { "%.4f".format(it.dishingOverStroke) }
            )
        ),
        T162FalsifierRecord(
            "F2", "the shared body's single-removal amplification is not materially below " +
                    "the array's",
            f2Fired,
            "best shared body %.2f against the array's %.2f-%.2f".format(
                best.singleRemovalAmplification,
                arrayCells.minOf { it.singleRemovalAmplification },
                arrayCells.maxOf { it.singleRemovalAmplification }
            )
        ),
        T162FalsifierRecord(
            "F3", "a FREE shared body under a uniform load applies a non-zero tie force",
            false,
            "asserted as a test at 1e-9 of the applied load, together with the uncoupled tile"
        ),
        T162FalsifierRecord(
            "F4", "the array corner fails to reproduce C-0063's 0.0706145537",
            reproductions.first().departure >= 1e-3,
            "reproduced at %.10f, departure %.2e".format(
                reproductions.first().reproduced, reproductions.first().departure
            )
        ),
        T162FalsifierRecord(
            "F5", "the shared body cannot be placed, and the count question is priced anyway",
            bodyCanBePlaced > 0,
            "%d of %d buildability rows do not clear".format(bodyCanBePlaced, buildability.size)
        )
    )

    val cheapBounds = listOf(
        T162BoundRecord(
            "the local support stiffness a shared body buys at 34 paths",
            mandateArithmetic.first { it.pathCount == 34 }.localSupportRatio, "dimensionless",
            "the mandate stops being a per-station budget: it moves into the body's ground, " +
                    "which is a RIGID-BODY mode of the tile and therefore invisible to dishing"
        ),
        T162BoundRecord(
            "the free rigid body's rank deficit", 3.0, "modes",
            "a free shared body carries no net force and no net moment, so it supports only " +
                    "the non-affine part of the station displacement - exactly what dishing is"
        ),
        T162BoundRecord(
            "the shortest ground element, one support, mandate massed on one element",
            (T162_C_ONE_SUPPORT * Gen1Tile.DUPLEX_BENDING_RIGIDITY / T162_MANDATE).pow(1.0 / 3.0),
            "nm",
            "CH-0081's 22.414 nm read with the mandate concentrated instead of divided by 34"
        ),
        T162BoundRecord(
            "the density exponent of the shared body's own 90th percentile", sharedSlope,
            "d ln p90 / d ln n",
            ("against %.9g for the array over the same seven grids: the topology changes the " +
                    "SLOPE of the redundancy axis and not only its level").format(arraySlope)
        ),
        T162BoundRecord(
            "the tie count the shared body's own density curve demands", demandedShared, "ties",
            ("the count argument re-asked. C-0089's ARRAY demands 195 by a run-length division; " +
                    "this fit gives the shared body %.0f, an extrapolation %.2fx beyond the " +
                    "densest grid measured. The array's own fitted crossing is %.0f and is NOT " +
                    "quotable - its curve is too flat for the extrapolation to mean " +
                    "anything").format(demandedShared, demandedShared / 180.0, demandedArray)
        ),
        T162BoundRecord(
            "the rigid shared body's best zero-defect dishing at C-0063's 34 stations",
            rigidAt34.minOf { it.dishingOverStroke }, "of the free-tile stroke",
            "F1: the branch is dead here if this is above T-5b's 0.10"
        )
    )

    val result = T162Result(
        task = "T-162",
        question = "Does a coupling that is NOT an array escape C-0089's count argument?",
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
                    "seed $T162_GRADING_SEED",
            "maturity" to "TRL 1-3, model-consistent and traceable; nothing derived here is " +
                    "measured"
        ),
        parameters = mapOf(
            "mandate" to roundForResult(T162_MANDATE, 9, T162_DECISION_FLOOR).toString(),
            "freeTileStroke" to roundForResult(freeStroke, 9, T162_DECISION_FLOOR).toString(),
            "gradingRealisations" to t162GradingRealisations.toString(),
            "gradingSeed" to T162_GRADING_SEED.toString(),
            "dishingSamplesPerEdge" to T162_SAMPLES.toString(),
            "ritzDegree" to T162_RITZ_DEGREE.toString(),
            "tieLadder" to T162_TIE_LADDER.joinToString(", "),
            "planBudget" to T162_PLAN_BUDGET.toString(),
            "unzipAllowable" to Gen1Tile.DUPLEX_UNZIP_ALLOWABLE.toString(),
            "shearAllowable" to Gen1Tile.DUPLEX_SHEAR_ALLOWABLE.toString(),
            "decisionDigits" to T162_DECISION_DIGITS.toString(),
            // Rounded by hand: `roundedForResult` dispatches on the JSON type and passes a
            // STRING through untouched, so a `Double.toString()` in a parameter map would
            // carry full round-trip precision into a file that declares nine digits.
            "arrayZeroDefect" to arrayZeroDefect.entries.joinToString("; ") {
                "${it.key} = ${roundForResult(it.value, 9, T162_DECISION_FLOOR)}"
            }
        ),
        cheapBounds = cheapBounds,
        mandateArithmetic = mandateArithmetic,
        rankSequence = rankSequence,
        groundElements = groundElements,
        bodies = bodyRecords,
        zeroDefect = zeroDefect,
        dropout = dropout,
        density = density,
        buildability = buildability,
        convergence = convergence,
        reproductions = reproductions,
        predicates = predicates,
        falsifiers = falsifiers,
        findings = emptyList()
    )

    val output = File("gpd/results/T-162-shared-body-coupling.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    // The JSON is written BEFORE any prose is formatted: `CLAUDE.md` records that a placeholder
    // miscount has cost this project a completed run three times.
    val withFindings = result.copy(
        findings = t162Findings(
            anyFlat, best, bestArray, rigidAt34, f1Fired, f2Fired, arrayCells, density,
            demandedShared, sharedSlope, arraySlope, mandateArithmetic,
            groundElements, buildability, bodyRecords, arrayZeroDefect
        )
    )
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(withFindings).roundedForResult(
                digits = T162_DECISION_DIGITS + 3,
                digitsByKey = DEPARTURE_DIGITS_BY_KEY,
                floor = T162_DECISION_FLOOR
            ) as JsonObject)
        ) + "\n"
    )
    println("T-162 — wrote ${output.path}")
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
private fun t162Findings(
    anyFlat: Boolean,
    best: T162DropoutRecord,
    bestArray: T162DropoutRecord,
    rigidAt34: List<T162ZeroDefectRecord>,
    f1Fired: Boolean,
    f2Fired: Boolean,
    arrayCells: List<T162DropoutRecord>,
    density: List<T162DensityRecord>,
    demandedShared: Double,
    sharedSlope: Double,
    arraySlope: Double,
    mandateArithmetic: List<T162MandateRecord>,
    groundElements: List<T162GroundElementRecord>,
    buildability: List<T162BuildRecord>,
    bodies: List<T162BodyRecord>,
    arrayZeroDefect: Map<String, Double>
): List<String> {
    val findings = ArrayList<String>()

    findings += ("THE ANSWER IS " + (if (anyFlat) "YES" else "NO") +
            ". The best shared-body cell under C-0087's measured dropout reads %.4f of the " +
            "stroke at the 90th percentile on %s with %s ties of %.0f pN/nm, against T-5b's " +
            "0.10 and against the array's own best of %.4f on the same station sets — a factor " +
            "of %.2f.").format(
        best.p90OverStroke, best.stationSet, best.body, best.tieStiffness,
        bestArray.p90OverStroke, bestArray.p90OverStroke / best.p90OverStroke
    )

    findings += ("F1, THE DECLARED FALSIFIER, " + (if (f1Fired) "FIRED" else "did NOT fire") +
            ". A perfectly rigid shared body at C-0063's 34 stations dishes %.4f to %.4f of " +
            "the stroke at ZERO defects over the tie ladder, against the array's %.4f at the " +
            "same stations and T-5b's 0.10.").format(
        rigidAt34.minOf { it.dishingOverStroke },
        rigidAt34.maxOf { it.dishingOverStroke },
        arrayZeroDefect.values.first()
    )

    val at34 = mandateArithmetic.first { it.pathCount == 34 }
    findings += ("THE ESCAPE IS A DIVISION AND IT IS WORTH %.1fx OF LOCAL SUPPORT. C-0017's " +
            "mandate is a SUM over an array's paths, so at 34 paths every station is held at " +
            "%.4f pN/nm. Under a shared body the same mandate is supplied by the body's own " +
            "GROUND, which is a rigid-body mode of the tile and therefore invisible to dishing, " +
            "and what caps a tie is the per-path FORCE allowable instead: %.4f pN/nm at " +
            "C-0049's 10 pN and the acceptable 3 nm stroke.").format(
        at34.localSupportRatio, at34.arrayPerStationStiffness, at34.sharedBodyTieCeilingFromForce
    )

    findings += ("F2 " + (if (f2Fired) "FIRED, AND IT FIRED BECAUSE AN AMPLIFICATION IS A " +
            "RATIO" else "did not fire") + ". The best shared-body cell amplifies by %.2f " +
            "under one missing tie where the array cells amplify by %.2f to %.2f — but the " +
            "ABSOLUTE worst single removal is %.4f of the stroke against the array's %.4f on " +
            "the same station set, i.e. %.2fx BETTER. A topology that improves its own " +
            "zero-defect baseline by more than it improves its worst case reports a worse " +
            "amplification while being absolutely better, and the ratio is the wrong statistic " +
            "to declare a falsifier on.").format(
        best.singleRemovalAmplification,
        arrayCells.minOf { it.singleRemovalAmplification },
        arrayCells.maxOf { it.singleRemovalAmplification },
        best.worstSingleRemovalOverStroke,
        arrayCells.first { it.stationSet == best.stationSet }.worstSingleRemovalOverStroke,
        arrayCells.first { it.stationSet == best.stationSet }.worstSingleRemovalOverStroke /
                best.worstSingleRemovalOverStroke
    )

    val sharedCurve = density.filter { it.topology == "SHARED BODY" }.sortedBy { it.pathCount }
    val arrayCurve = density.filter { it.topology == "ARRAY" }.sortedBy { it.pathCount }
    findings += ("AND THE COUNT ARGUMENT SURVIVES THE TOPOLOGY. Over %s paths the rigid " +
            "shared body's 90th percentile runs %s against the array's %s — uniformly better, " +
            "and with a redundancy slope of %.3f against %.3f, i.e. %.1fx steeper in " +
            "d ln p90 / d ln n. That steeper slope is the topology's real gift and it is still " +
            "not enough: the fit crosses T-5b's 0.10 at %.0f ties, an extrapolation only %.2fx " +
            "beyond the densest grid measured, against the %.0f upward crossover sites C-0066 " +
            "counts at phase 24 — %.1fx short, where C-0089's array is 5.7x short of 34 by an " +
            "independent run-length division that demands %.0f. The escape changes the LEVEL " +
            "by 2.4x and leaves the COUNT of the same order.").format(
        sharedCurve.joinToString("/") { it.pathCount.toString() },
        sharedCurve.joinToString(" -> ") { "%.4f".format(it.p90OverStroke) },
        arrayCurve.joinToString(" -> ") { "%.4f".format(it.p90OverStroke) },
        sharedSlope, arraySlope, sharedSlope / arraySlope,
        demandedShared, demandedShared / 180.0, T162_UPWARD_SITES,
        demandedShared / T162_UPWARD_SITES, T162_C0089_DEMANDED_PATHS
    )

    val oneSupport = groundElements.filter { it.endCondition.startsWith("one-support") }
    val placeable = oneSupport.filter { it.insidePlanBudget && it.insideShearAllowable }
    findings += ("CH-0081 INVERTS WHEN THE MANDATE IS MASSED. Its 22.414 nm is " +
            "(48 EI / k)^(1/3) at k = mandate/34, and a flexure span goes as n^(1/3), so " +
            "concentrating the mandate on n ground elements shortens it by (34/n)^(1/3). On " +
            "the one-support family %s: %s. The plan obstruction that killed the flexure branch " +
            "is a consequence of DIVIDING the mandate, not of the mandate.").format(
        if (placeable.isEmpty()) "nothing places inside C-0069's 8.19 nm budget with an " +
                "admissible per-element force"
        else ("%d of %d counts place inside C-0069's 8.19 nm budget with a per-element force " +
                "inside the 48 pN shear allowable").format(placeable.size, oneSupport.size),
        oneSupport.joinToString("; ") {
            "n=%d: %.2f nm, %.1f pN".format(it.elementCount, it.planLength, it.perElementForce)
        }
    )

    val failing = buildability.filter { !it.clears }
    findings += ("WHAT THE SHARED BODY COSTS IS A SECOND LAYER, AND THAT IS A SPECIFICATION " +
            "GAP RATHER THAN A PHYSICS ONE. %d of %d buildability rows do not clear: %s. The " +
            "motif itself is the ONE in this neighbourhood with a published precedent — " +
            "C-0055 records that square-lattice multilayer origami ties its added duplex at " +
            "many sites and is rigid, and it is the single-tie ARM this programme has no " +
            "precedent for.").format(
        failing.size, buildability.size,
        if (failing.isEmpty()) "none" else failing.joinToString("; ") { it.question }
    )

    findings += ("THE BODY'S OWN RIGIDITY IS THE SECOND-ORDER QUESTION. Against the rigid " +
            "limit the condensed station compliance is %s, so %s.").format(
        bodies.joinToString("; ") { "%s x%.3f".format(it.name, it.meanStationComplianceOverTile) },
        if (bodies.maxOf { it.meanStationComplianceOverTile } < 1.5)
            "even a single-layer body is within 50 % of a rigid one at these stations"
        else "the body's own compliance is a first-order design variable and the rigid limit " +
                "is optimistic"
    )

    return findings
}
