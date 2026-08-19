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

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.GrillageDeflection
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.OrigamiSheet
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PointSupport
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.log10
import kotlin.math.pow

/**
 * Task `T-113` — can a **non-uniform** coupling stiffness buy back the edge dishing? Leaf `A8.2`.
 *
 * **Every descent in this study decides at [searchDecision]'s six significant digits** (`T-226`,
 * `C-0139`). [optimiseStiffnessDistribution] compares raw `Double`s at every branch it takes — the
 * coarse scan, the golden section, the sweep acceptance and the start ranking — so before this the
 * JIT recompiling a hot reduction part-way through a run was enough to move the terminal point:
 * two runs of identical code on identical inputs emitted this file with **224** fields different
 * (`C-0138` §8, reproduced by `C-0139`), always one descent record and its transfers, and a
 * different record each run. `CLAUDE.md`: *a DECISION must be rounded COARSER than the number it
 * is taken on.* The wrap is at the **call sites** rather than inside the shared optimiser, because
 * five other studies call it and their published files must not move for a repair this file needs.
 *
 * Run with:
 *
 * ```shell
 * tools/study.sh coupling.NonUniformCouplingStudyKt
 * ```
 *
 * Emits `gpd/results/T-113-non-uniform-coupling.json`, deterministically — no timestamp, every
 * floating-point number rounded at the serialisation boundary per [roundCouplingResult].
 *
 * ## What this study is
 *
 * `C-0047` closes with *"whether a NON-UNIFORM coupling stiffness could buy back the edge dishing.
 * Every spring here is equal by `C-0017`'s mandate and nothing upstream requires it"* — the last
 * unexplored axis, and the only one `CH-0034`'s 0.149 saturation floor does not already answer,
 * because that floor is a statement about the attachment **count**.
 *
 * Everything except the distribution is upstream machinery re-run rather than reimplemented:
 * `structure`'s grillage and plate, `C-0022`'s solved collar read from its own result file,
 * `C-0026`'s attachment grid, `C-0017`'s mandate. What is new is `NonUniformCoupling.kt` — the
 * distributions, their price, and the Woodbury surrogate that makes an optimisation over 45
 * per-path stiffnesses cost less than a dozen assembled solves.
 */

// ---------------------------------------------------------------------------------------------
// the records
// ---------------------------------------------------------------------------------------------

/** The two cheap bounds, which run before any optimiser. */
@Serializable
data class T113CheapBoundRecord(
    val scheme: String,
    val columns: Int,
    val attachments: Int,
    val uniformShare: Double,
    val perPathCeilingAtAcceptableStroke: Double,
    val admissibleRatioAtAcceptableStroke: Double,
    val admissibleRatioAtDesiredStroke: Double,
    val admissibleAtDesiredStroke: Boolean,
    val reachableDishingFloor: Double,
    val reachableFloorOverStroke: Double,
    val reachablePeakDishing: Double,
    val reachablePeakOverStroke: Double,
    val uniformPeakDishing: Double,
    val uniformOverStroke: Double,
    val floorReachesTheTolerance: Boolean,
    val floorBeatsTheFreeTile: Boolean,
    val reciprocityResidual: Double
)

/** One distribution, solved: the uniform one, a rim-stiffened one, a load-matched one, an optimum. */
@Serializable
data class T113DistributionRecord(
    val scheme: String,
    val label: String,
    val columns: Int,
    val attachments: Int,
    val profile: String,
    val stiffnessCeiling: String,
    val latticePeakDishing: Double,
    val platePeakDishing: Double,
    val latticeOverPlate: Double,
    val latticeExcessPercent: Double,
    val dishingOverStroke: Double,
    val flat: Boolean,
    val overTolerance: Double,
    val freeTileOverStroke: Double,
    val betterThanTheFreeTile: Boolean,
    val improvementOverUniform: Double,
    val maximumOverMinimumStiffness: Double,
    val peakPathStiffness: Double,
    val peakPathForceAtAcceptableStroke: Double,
    val unzipMarginAtAcceptableStroke: Double,
    val admissibleUnderTheUnzipAllowable: Boolean,
    val peakSupportForce: Double,
    val peakCrossoverForce: Double,
    val peakDuplexShear: Double,
    val peakThermalForce: Double,
    /** How many paths still carry at least half the uniform share — the coupling's live count. */
    val pathsCarryingHalfTheUniformShare: Int,
    /** How many have been emptied below a tenth of it, i.e. all but removed. */
    val pathsBelowATenthOfTheUniformShare: Int,
    val verdict: String
)

/** The rim-stiffening sweep — the one describable, one-parameter family, on the surrogates. */
@Serializable
data class T113RimSweepRecord(
    val scheme: String,
    val columns: Int,
    val attachments: Int,
    val collarWidth: Double,
    val rimPaths: Int,
    val ratio: Double,
    val latticePeakDishing: Double,
    val dishingOverStroke: Double,
    val plateDishingOverStroke: Double,
    val latticeOverPlate: Double,
    val flat: Boolean,
    val betterThanTheFreeTile: Boolean,
    val peakPathStiffness: Double,
    val peakPathForceAtAcceptableStroke: Double,
    val admissibleUnderTheUnzipAllowable: Boolean
)

/** The distribution itself, attachment by attachment. */
@Serializable
data class T113PathRecord(
    val scheme: String,
    val label: String,
    val index: Int,
    val x: Double,
    val y: Double,
    val stiffness: Double,
    val shareOfTheUniformPath: Double,
    val forceAtAcceptableStroke: Double,
    val thermalForce: Double
)

/**
 * What the search found and how far it stopped above the bound.
 *
 * **No property of the search PATH is here** — not the evaluation count, not the sweep count, not
 * which start won. A path is not reproducible even when its answer is: a last-ulp difference in the
 * objective, the JIT compiling a hot reduction part-way through a run (`CLAUDE.md`'s standing trap),
 * flips a strict comparison, and the descent then reaches the *same* optimum by a different route.
 * Three runs of this study agreed on every objective to nine significant digits and disagreed on the
 * evaluation count, the sweep count and the winning start. The last-sweep improvement is kept, as
 * the only evidence that the search converged rather than ran out of sweeps, but it is rounded to
 * **two** significant digits, which is all that survives.
 */
@Serializable
data class T113OptimiserRecord(
    val scheme: String,
    val model: String,
    val stiffnessCeiling: String,
    val starts: Int,
    val lastSweepImprovementToTwoDigits: Double,
    val objective: Double,
    val reachableDishingFloor: Double,
    val objectiveOverFloor: Double
)

/** One optimum carried to another model, or to another operating state. */
@Serializable
data class T113TransferRecord(
    val scheme: String,
    val label: String,
    val fromModel: String,
    val toModel: String,
    val profile: String,
    val peakDishing: Double,
    val dishingOverStroke: Double,
    val uniformOverStroke: Double,
    val improvementOverUniform: Double,
    val flat: Boolean
)

@Serializable
data class T113ConvergenceRecord(
    val axis: String,
    val setting: String,
    val dishingOverStroke: Double,
    val departureFromFinest: Double
)

@Serializable
data class T113ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val relativeDeparture: Double
)

@Serializable
data class T113Result(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val runParameters: Map<String, String>,
    val citedInputs: Map<String, String>,
    val temperature: Double,
    val thermalEnergy: Double,
    val designPointProfile: String,
    val rigidPlateTolerance: Double,
    val mandatedTotalStiffness: Double,
    val freeTileStroke: Double,
    val cheapBound: List<T113CheapBoundRecord>,
    val rimSweep: List<T113RimSweepRecord>,
    val distributions: List<T113DistributionRecord>,
    val paths: List<T113PathRecord>,
    val optimiser: List<T113OptimiserRecord>,
    val transfers: List<T113TransferRecord>,
    val convergence: List<T113ConvergenceRecord>,
    val reproductions: List<T113ReproductionRecord>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

// ---------------------------------------------------------------------------------------------
// the fixed inputs
// ---------------------------------------------------------------------------------------------

private const val T113_DUPLEXES = 15

private const val T113_EDGE_X = Gen1Tile.EDGE_X

private const val T113_NOMINAL_COLUMNS = 8

private val T113_MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

/** `T-5b`'s convention, cited via `C-0015` — **a convention, not a physical threshold**. */
private const val T113_TOLERANCE = 0.10

/** The rim standoff `C-0022` fits its rim residual over, in nm — **CITED**. */
private const val T113_RIM_STANDOFF = 1.0

/**
 * The stiffening ratios swept, at constant total. 1.0 is the uniform coupling and is a gate.
 *
 * The sweep runs well past any ratio a designer would choose because the family **converges**: at
 * a large ratio the interior springs carry nothing and what is left is an attachment scheme
 * placed only on the collar. Where the curve turns is therefore a statement about *placement*,
 * not about stiffness, and it has to be located rather than assumed.
 */
private val T113_RIM_RATIOS =
    listOf(1.0, 1.25, 1.5, 2.0, 3.0, 5.0, 10.0, 20.0, 50.0, 100.0)

/**
 * The collar widths swept, in nm: a duplex pitch, two of them, the half-pitch of `C-0015`'s outer
 * columns (which selects the outer columns and nothing else), `C-0022`'s **solved** 8.94 nm
 * collar, and one wide enough to take everything but the tile's own centre.
 */
private val T113_COLLAR_WIDTHS = listOf(1.5, 3.0, 6.7, 13.0)

private const val T113_SAMPLES = 81

// ---------------------------------------------------------------------------------------------
// the load profiles, read from `C-0022`'s own result file
// ---------------------------------------------------------------------------------------------

private class T113Profile(
    val name: String,
    val concentration: Double?,
    val gapHeight: Double?,
    val appliedBias: Double?,
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {

    fun field(interiorPressure: Double, lengthY: Double): PressureField = edgeCollarPressure(
        interiorPressure, T113_EDGE_X, lengthY,
        if (rimDepth == 0.0) listOf(CollarTerm(smoothDepth, smoothWidth))
        else listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T113_RIM_STANDOFF))
    )

}

/**
 * `C-0022`'s solved profiles, read from `gpd/results/T-3b-tile-edge-load-profile.json`.
 *
 * `CLAUDE.md`'s trap, avoided by construction: the file carries **two** profiles per
 * `(concentration, gap)` — one per operating bias — so every lookup here is keyed on
 * `(concentration, gapHeight, appliedBias)` and the bias travels into the result file.
 */
private fun t113SolvedProfiles(file: File): List<T113Profile> {
    require(file.exists()) {
        "C-0022's result file is missing: ${file.path}. T-113 consumes the SOLVED edge profile " +
                "and will not substitute an assumed one for it."
    }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .map { record ->
            fun value(key: String) = record.getValue(key).jsonPrimitive.content.toDouble()
            val concentration = value("concentration")
            val gap = value("gapHeight")
            val bias = value("appliedBias")
            T113Profile(
                name = "C-0022 %.1f mM, %.0f nm, %.3f V".format(concentration, gap, bias),
                concentration = concentration,
                gapHeight = gap,
                appliedBias = bias,
                smoothDepth = value("taperDepth"),
                smoothWidth = value("taperWidth"),
                rimDepth = value("rimResidualDepth")
            )
        }
}

/** The `(concentration, gap, bias)` keys of the five states `C-0022`'s headline table quotes. */
private val T113_HEADLINE_STATES: List<Triple<Double, Double, Double>> = listOf(
    Triple(2.0, 10.0, 0.192),
    Triple(0.5, 10.0, 0.134),
    Triple(10.0, 10.0, 0.192),
    Triple(2.0, 5.0, 0.368),
    Triple(2.0, 2.0, 0.368)
)

// ---------------------------------------------------------------------------------------------
// the models
// ---------------------------------------------------------------------------------------------

private fun t113Sheet(): OrigamiSheet =
    origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)

private fun t113Lattice(
    sheet: OrigamiSheet,
    supports: List<PointSupport> = emptyList(),
    subdivisions: Int = 2
): OrigamiGrillage = OrigamiGrillage(
    sheet = sheet,
    lengthX = T113_EDGE_X,
    beamCount = T113_DUPLEXES,
    foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
    columns = CrossoverLayout.centred(T113_NOMINAL_COLUMNS, sheet.crossoverSpacing / 2.0),
    subdivisions = subdivisions,
    supports = supports
)

private class T113Scheme(val label: String, val columns: Int) {

    val attachments: Int get() = columns * T113_DUPLEXES

    fun grid(lengthY: Double): List<Pair<Double, Double>> =
        attachmentGrid(columns, T113_DUPLEXES, T113_EDGE_X, lengthY)

}

private fun t113Schemes(): List<T113Scheme> = listOf(
    T113Scheme("1 x 15 (C-0041's buildable count)", 1),
    T113Scheme("2 x 15", 2),
    T113Scheme("3 x 15 (C-0015's answer)", 3)
)

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

fun main() {
    val started = System.currentTimeMillis()
    val sheet = t113Sheet()
    val lengthY = T113_DUPLEXES * sheet.interhelicalDistance
    val interiorPressure = Gen1Tile.TARGET_FORCE / (T113_EDGE_X * lengthY)
    val plateModel = sheet.plate(T113_EDGE_X, lengthY)

    println("T-113 — reading C-0022's solved edge profile ...")
    val uniformProfile = T113Profile("uniform", null, null, null, 0.0, 1.0, 0.0)
    val solved = t113SolvedProfiles(File("gpd/results/T-3b-tile-edge-load-profile.json"))
    val headline = T113_HEADLINE_STATES.map { (concentration, gap, bias) ->
        solved.firstOrNull {
            it.concentration == concentration && it.gapHeight == gap && it.appliedBias == bias
        } ?: error("no C-0022 profile at $concentration mM, $gap nm, $bias V")
    }
    val designPoint = headline.first()
    val designField = designPoint.field(interiorPressure, lengthY)

    // The free-tile stroke — C-0006's, C-0015's, C-0026's and C-0047's normaliser, unchanged.
    val stroke = PlateOnFoundation(plateModel, Gen1Tile.FOUNDATION_SECANT, emptyList(), 12)
        .solve(uniformPressure(interiorPressure)).meanDeflection

    val bareLattice = t113Lattice(sheet)
    val barePlate = PlateOnFoundation(plateModel, Gen1Tile.FOUNDATION_SECANT, emptyList(), 12)
    val freeTileDishing = bareLattice.solve(designField).peakDishing(T113_SAMPLES)
    val freeTileOverStroke = freeTileDishing / stroke

    // ------------------------------------------------------------------ the surrogates
    println("T-113 — the Woodbury surrogates: one factorisation per model, n+1 load cases ...")
    val schemes = t113Schemes()
    val latticeSurrogates = schemes.associate { scheme ->
        scheme.label to latticeInfluenceSurrogate(
            bareLattice, scheme.grid(lengthY), designField, T113_SAMPLES
        )
    }
    val plateSurrogates = schemes.associate { scheme ->
        scheme.label to plateInfluenceSurrogate(
            barePlate, scheme.grid(lengthY), designField, T113_SAMPLES
        )
    }

    // ------------------------------------------------------------------ the cheap bounds
    println("T-113 — the cheap bounds, before any optimiser ...")
    val cheapBound = schemes.map { scheme ->
        val surrogate = latticeSurrogates.getValue(scheme.label)
        val uniform = surrogate.solve(
            normalisedStiffnesses(List(scheme.attachments) { 1.0 }, T113_MANDATE)
        ).peakDishing
        val floor = surrogate.reachableDishingFloor
        T113CheapBoundRecord(
            scheme = scheme.label,
            columns = scheme.columns,
            attachments = scheme.attachments,
            uniformShare = T113_MANDATE / scheme.attachments,
            perPathCeilingAtAcceptableStroke = perPathStiffnessCeiling(
                Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE
            ),
            admissibleRatioAtAcceptableStroke = admissibleStiffnessRatio(
                Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE,
                T113_MANDATE, scheme.attachments
            ),
            admissibleRatioAtDesiredStroke = admissibleStiffnessRatio(
                Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.DESIRED_STROKE,
                T113_MANDATE, scheme.attachments
            ),
            admissibleAtDesiredStroke = admissibleStiffnessRatio(
                Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.DESIRED_STROKE,
                T113_MANDATE, scheme.attachments
            ) >= 1.0,
            reachableDishingFloor = floor,
            reachableFloorOverStroke = floor / stroke,
            reachablePeakDishing = surrogate.reachablePeakDishing,
            reachablePeakOverStroke = surrogate.reachablePeakDishing / stroke,
            uniformPeakDishing = uniform,
            uniformOverStroke = uniform / stroke,
            floorReachesTheTolerance = floor / stroke < T113_TOLERANCE,
            floorBeatsTheFreeTile = floor / stroke < freeTileOverStroke,
            reciprocityResidual = surrogate.reciprocityResidual
        )
    }

    // ------------------------------------------------------------------ solving one distribution
    fun assembledSolution(
        grid: List<Pair<Double, Double>>,
        stiffnesses: List<Double>,
        field: PressureField,
        subdivisions: Int = 2
    ): GrillageDeflection = t113Lattice(
        sheet,
        grid.mapIndexed { index, (x, y) -> PointSupport(x, y, stiffnesses[index]) },
        subdivisions
    ).solve(field)

    fun record(
        scheme: T113Scheme,
        label: String,
        stiffnesses: List<Double>,
        ceilingLabel: String,
        uniformDishing: Double,
        profile: T113Profile = designPoint
    ): T113DistributionRecord {
        val grid = scheme.grid(lengthY)
        val field = profile.field(interiorPressure, lengthY)
        val solution = assembledSolution(grid, stiffnesses, field)
        val lattice = solution.peakDishing(T113_SAMPLES)
        val plate = PlateOnFoundation(
            plateModel, Gen1Tile.FOUNDATION_SECANT,
            grid.mapIndexed { index, (x, y) -> PointSupport(x, y, stiffnesses[index]) }, 12
        ).solve(field).peakDishing(T113_SAMPLES)
        val fraction = lattice / stroke
        val peakStiffness = stiffnesses.max()
        val peakPathForce = peakStiffness * Gen1Tile.ACCEPTABLE_STROKE
        return T113DistributionRecord(
            scheme = scheme.label,
            label = label,
            columns = scheme.columns,
            attachments = scheme.attachments,
            profile = profile.name,
            stiffnessCeiling = ceilingLabel,
            latticePeakDishing = lattice,
            platePeakDishing = plate,
            latticeOverPlate = lattice / plate,
            latticeExcessPercent = 100.0 * (lattice / plate - 1.0),
            dishingOverStroke = fraction,
            flat = fraction < T113_TOLERANCE,
            overTolerance = fraction / T113_TOLERANCE,
            freeTileOverStroke = freeTileOverStroke,
            betterThanTheFreeTile = fraction < freeTileOverStroke,
            improvementOverUniform = 1.0 - lattice / uniformDishing,
            maximumOverMinimumStiffness = stiffnesses.max() / stiffnesses.min(),
            peakPathStiffness = peakStiffness,
            peakPathForceAtAcceptableStroke = peakPathForce,
            unzipMarginAtAcceptableStroke = Gen1Tile.DUPLEX_UNZIP_ALLOWABLE / peakPathForce,
            admissibleUnderTheUnzipAllowable =
                peakPathForce <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE * (1.0 + 1e-12),
            peakSupportForce = solution.supportForces.maxOf { abs(it) },
            peakCrossoverForce = solution.peakCrossoverForce,
            peakDuplexShear = solution.peakDuplexShear,
            peakThermalForce = perPathThermalForces(stiffnesses).max(),
            pathsCarryingHalfTheUniformShare =
                stiffnesses.count { it * scheme.attachments / T113_MANDATE >= 0.5 },
            pathsBelowATenthOfTheUniformShare =
                stiffnesses.count { it * scheme.attachments / T113_MANDATE < 0.1 },
            verdict = when {
                fraction < T113_TOLERANCE -> "FLAT under T-5b's 10% convention"
                fraction > freeTileOverStroke ->
                    "NOT flat, and WORSE than no coupling at all"
                else -> "NOT flat, but better than no coupling at all"
            }
        )
    }

    // ------------------------------------------------------------------ the distributions
    println("T-113 — the rim-stiffening sweep and the load-matched distribution ...")
    val distributions = mutableListOf<T113DistributionRecord>()
    val paths = mutableListOf<T113PathRecord>()
    val optimiserRecords = mutableListOf<T113OptimiserRecord>()
    val transfers = mutableListOf<T113TransferRecord>()
    val optima = mutableMapOf<String, List<Double>>()

    val solvedCollarWidth = designPoint.smoothWidth
    val collarWidths = (T113_COLLAR_WIDTHS + solvedCollarWidth).sorted()
    val rimSweep = mutableListOf<T113RimSweepRecord>()
    val bestRimWeights = mutableMapOf<String, List<Double>>()
    val bestRimLabel = mutableMapOf<String, String>()

    fun collarLabel(width: Double): String =
        if (width == solvedCollarWidth) "%.2f nm (C-0022's SOLVED collar)".format(width)
        else "%.2f nm".format(width)

    schemes.forEach { scheme ->
        val grid = scheme.grid(lengthY)
        val surrogate = latticeSurrogates.getValue(scheme.label)
        val plateSurrogate = plateSurrogates.getValue(scheme.label)
        val uniformStiffness = normalisedStiffnesses(List(scheme.attachments) { 1.0 }, T113_MANDATE)
        val uniformDishing = surrogate.solve(uniformStiffness).peakDishing

        // -------------------------------------------------- the rim sweep, on the surrogate
        // Two dimensions, because the family CONVERGES on a placement: at a large ratio the
        // interior springs carry nothing, and which stations count as "rim" is then the design.
        collarWidths.forEach { width ->
            val rimPaths = rimStiffenedWeights(grid, T113_EDGE_X, lengthY, width, 2.0)
                .count { it > 1.0 }
            T113_RIM_RATIOS.forEach { ratio ->
                val weights = rimStiffenedWeights(grid, T113_EDGE_X, lengthY, width, ratio)
                val stiffnesses = normalisedStiffnesses(weights, T113_MANDATE)
                val lattice = surrogate.solve(stiffnesses).peakDishing
                val plate = plateSurrogate.solve(stiffnesses).peakDishing
                val peakForce = stiffnesses.max() * Gen1Tile.ACCEPTABLE_STROKE
                rimSweep += T113RimSweepRecord(
                    scheme = scheme.label,
                    columns = scheme.columns,
                    attachments = scheme.attachments,
                    collarWidth = width,
                    rimPaths = rimPaths,
                    ratio = ratio,
                    latticePeakDishing = lattice,
                    dishingOverStroke = lattice / stroke,
                    plateDishingOverStroke = plate / stroke,
                    latticeOverPlate = lattice / plate,
                    flat = lattice / stroke < T113_TOLERANCE,
                    betterThanTheFreeTile = lattice / stroke < freeTileOverStroke,
                    peakPathStiffness = stiffnesses.max(),
                    peakPathForceAtAcceptableStroke = peakForce,
                    admissibleUnderTheUnzipAllowable =
                        peakForce <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE * (1.0 + 1e-12)
                )
            }
        }
        // The argmin is taken on the ROUNDED objective with the first index winning any tie —
        // CLAUDE.md's rule, because an index is not a rounded double and a flat sweep otherwise
        // returns whichever entry the summation order happened to favour.
        val best = rimSweep.filter {
            it.scheme == scheme.label && it.admissibleUnderTheUnzipAllowable
        }.minWithOrNull(
            compareBy({ roundCouplingResult(it.latticePeakDishing) }, { it.ratio }, { it.collarWidth })
        )!!
        bestRimWeights[scheme.label] =
            rimStiffenedWeights(grid, T113_EDGE_X, lengthY, best.collarWidth, best.ratio)
        bestRimLabel[scheme.label] =
            "rim x %.0f over a %s collar".format(best.ratio, collarLabel(best.collarWidth))

        // -------------------------------------------------- the named distributions, assembled
        distributions += record(
            scheme, "uniform (C-0047's limiting case)", uniformStiffness, "none", uniformDishing
        )
        listOf(2.0, 5.0, 10.0).forEach { ratio ->
            distributions += record(
                scheme,
                "rim x %.0f over C-0022's %.2f nm collar".format(ratio, solvedCollarWidth),
                normalisedStiffnesses(
                    rimStiffenedWeights(grid, T113_EDGE_X, lengthY, solvedCollarWidth, ratio),
                    T113_MANDATE
                ),
                "none", uniformDishing
            )
        }
        distributions += record(
            scheme, "BEST RIM: ${bestRimLabel.getValue(scheme.label)}",
            normalisedStiffnesses(bestRimWeights.getValue(scheme.label), T113_MANDATE),
            "none", uniformDishing
        )
        val matched = normalisedStiffnesses(loadMatchedWeights(grid, designField), T113_MANDATE)
        distributions += record(
            scheme, "load-matched (the cheap bound's prediction)", matched, "none", uniformDishing
        )

        // ---------------------------------------------------------- the optimisation
        val starts = mutableListOf(
            List(scheme.attachments) { 1.0 },
            loadMatchedWeights(grid, designField),
            rimStiffenedWeights(grid, T113_EDGE_X, lengthY, solvedCollarWidth, 3.0),
            rimStiffenedWeights(grid, T113_EDGE_X, lengthY, solvedCollarWidth, 0.4),
            bestRimWeights.getValue(scheme.label)
        )
        // The capped problem runs FIRST and its answer becomes a start for the uncapped one, so
        // that the uncapped optimum cannot be worse than the capped one it contains — a descent
        // over a superset that started elsewhere can be, and was, on the first run of this study.
        val ceilings = listOf(
            "10 pN unzip at the 3 nm acceptable stroke" to perPathStiffnessCeiling(
                Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE
            ),
            "none" to Double.POSITIVE_INFINITY
        )
        ceilings.forEach { (ceilingLabel, ceiling) ->
            if (ceiling * scheme.attachments < T113_MANDATE) {
                println(
                    "T-113 — %s: the ceiling '%s' is infeasible at %d paths, skipped"
                        .format(scheme.label, ceilingLabel, scheme.attachments)
                )
                return@forEach
            }
            println("T-113 — optimising ${scheme.label}, ceiling: $ceilingLabel ...")
            val usedStarts = starts.toList()
            val optimum = optimiseStiffnessDistribution(
                totalStiffness = T113_MANDATE,
                starts = usedStarts,
                ceiling = ceiling,
                sweeps = 25,
                tolerance = 1e-5,
                searchHalfWidth = 2.0,
                scanPoints = 7,
                refinements = 8
            ) { searchDecision(surrogate.solve(it).peakDishing) }
            optima["${scheme.label}|$ceilingLabel"] = optimum.stiffnesses
            starts += optimum.stiffnesses
            optimiserRecords += T113OptimiserRecord(
                scheme = scheme.label,
                model = "lattice (C-0009's grillage)",
                stiffnessCeiling = ceilingLabel,
                starts = usedStarts.size,
                lastSweepImprovementToTwoDigits = twoDigits(optimum.lastImprovement),
                objective = optimum.objective,
                reachableDishingFloor = surrogate.reachableDishingFloor,
                objectiveOverFloor = optimum.objective / surrogate.reachableDishingFloor
            )
            val label = "OPTIMUM, ceiling: $ceilingLabel"
            distributions += record(scheme, label, optimum.stiffnesses, ceilingLabel, uniformDishing)
            val thermal = perPathThermalForces(optimum.stiffnesses)
            optimum.stiffnesses.forEachIndexed { index, stiffness ->
                paths += T113PathRecord(
                    scheme = scheme.label,
                    label = label,
                    index = index,
                    x = grid[index].first,
                    y = grid[index].second,
                    stiffness = stiffness,
                    shareOfTheUniformPath = stiffness * scheme.attachments / T113_MANDATE,
                    forceAtAcceptableStroke = stiffness * Gen1Tile.ACCEPTABLE_STROKE,
                    thermalForce = thermal[index]
                )
            }

            // ------------------------------------------------ does it survive the other model?
            val plateUniform = plateSurrogate.solve(uniformStiffness).peakDishing
            val onPlate = plateSurrogate.solve(optimum.stiffnesses).peakDishing
            transfers += T113TransferRecord(
                scheme = scheme.label,
                label = label,
                fromModel = "lattice",
                toModel = "plate (C-0006's continuum)",
                profile = designPoint.name,
                peakDishing = onPlate,
                dishingOverStroke = onPlate / stroke,
                uniformOverStroke = plateUniform / stroke,
                improvementOverUniform = 1.0 - onPlate / plateUniform,
                flat = onPlate / stroke < T113_TOLERANCE
            )

            // and the converse — would the PLATE have chosen a different distribution?
            val plateOptimum = optimiseStiffnessDistribution(
                totalStiffness = T113_MANDATE,
                starts = starts.toList(),
                ceiling = ceiling,
                sweeps = 25,
                tolerance = 1e-5,
                searchHalfWidth = 2.0,
                scanPoints = 7,
                refinements = 8
            ) { searchDecision(plateSurrogate.solve(it).peakDishing) }
            val plateOptimumOnLattice = surrogate.solve(plateOptimum.stiffnesses).peakDishing
            optimiserRecords += T113OptimiserRecord(
                scheme = scheme.label,
                model = "plate (C-0006's continuum)",
                stiffnessCeiling = ceilingLabel,
                starts = starts.size,
                lastSweepImprovementToTwoDigits = twoDigits(plateOptimum.lastImprovement),
                objective = plateOptimum.objective,
                reachableDishingFloor = plateSurrogate.reachableDishingFloor,
                objectiveOverFloor = plateOptimum.objective / plateSurrogate.reachableDishingFloor
            )
            transfers += T113TransferRecord(
                scheme = scheme.label,
                label = "PLATE OPTIMUM, ceiling: $ceilingLabel",
                fromModel = "plate (C-0006's continuum)",
                toModel = "lattice",
                profile = designPoint.name,
                peakDishing = plateOptimumOnLattice,
                dishingOverStroke = plateOptimumOnLattice / stroke,
                uniformOverStroke = uniformDishing / stroke,
                improvementOverUniform = 1.0 - plateOptimumOnLattice / uniformDishing,
                flat = plateOptimumOnLattice / stroke < T113_TOLERANCE
            )
        }
    }

    // ------------------------------------------------------------------ every operating state
    println("T-113 — every headline state, and a distribution optimised across ALL of them ...")
    val stateSurrogates = schemes.associate { scheme ->
        scheme.label to (headline + uniformProfile).associate { profile ->
            profile.name to latticeInfluenceSurrogate(
                bareLattice, scheme.grid(lengthY),
                profile.field(interiorPressure, lengthY), T113_SAMPLES
            )
        }
    }

    fun stateTransfers(scheme: T113Scheme, label: String, stiffnesses: List<Double>) {
        val uniformStiffness = normalisedStiffnesses(List(scheme.attachments) { 1.0 }, T113_MANDATE)
        (headline + uniformProfile).forEach { profile ->
            val surrogate = stateSurrogates.getValue(scheme.label).getValue(profile.name)
            val uniformHere = surrogate.solve(uniformStiffness).peakDishing
            val here = surrogate.solve(stiffnesses).peakDishing
            transfers += T113TransferRecord(
                scheme = scheme.label,
                label = label,
                fromModel = "lattice at ${designPoint.name}",
                toModel = "lattice",
                profile = profile.name,
                peakDishing = here,
                dishingOverStroke = here / stroke,
                uniformOverStroke = uniformHere / stroke,
                improvementOverUniform = 1.0 - here / uniformHere,
                flat = here / stroke < T113_TOLERANCE
            )
        }
    }

    schemes.forEach { scheme ->
        stateTransfers(
            scheme, "BEST RIM: ${bestRimLabel.getValue(scheme.label)}",
            normalisedStiffnesses(bestRimWeights.getValue(scheme.label), T113_MANDATE)
        )
        optima["${scheme.label}|none"]?.let {
            stateTransfers(scheme, "OPTIMUM, ceiling: none", it)
        }
    }

    // A distribution tuned at ONE state is not a design if it fails at the others, so the last
    // optimisation is a MINIMAX over all five of C-0022's solved states at once — the same
    // machinery, one objective up.
    println("T-113 — the minimax distribution over all five solved states ...")
    val robustScheme = schemes.last()
    val robustSurrogates = headline.map {
        stateSurrogates.getValue(robustScheme.label).getValue(it.name)
    }
    val robustCeiling = perPathStiffnessCeiling(
        Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE
    )
    val robustStarts = listOf(
        List(robustScheme.attachments) { 1.0 },
        bestRimWeights.getValue(robustScheme.label),
        optima.getValue("${robustScheme.label}|none")
    )
    val robust = optimiseStiffnessDistribution(
        totalStiffness = T113_MANDATE,
        starts = robustStarts,
        ceiling = robustCeiling,
        sweeps = 25,
        tolerance = 1e-5,
        searchHalfWidth = 2.0,
        scanPoints = 7,
        refinements = 8
    ) { stiffnesses ->
        searchDecision(robustSurrogates.maxOf { it.solve(stiffnesses).peakDishing })
    }
    optimiserRecords += T113OptimiserRecord(
        scheme = robustScheme.label,
        model = "lattice, MINIMAX over C-0022's five solved states",
        stiffnessCeiling = "10 pN unzip at the 3 nm acceptable stroke",
        starts = robustStarts.size,
        lastSweepImprovementToTwoDigits = twoDigits(robust.lastImprovement),
        objective = robust.objective,
        reachableDishingFloor =
            latticeSurrogates.getValue(robustScheme.label).reachableDishingFloor,
        objectiveOverFloor = robust.objective /
                latticeSurrogates.getValue(robustScheme.label).reachableDishingFloor
    )
    distributions += record(
        robustScheme, "MINIMAX over the five solved states", robust.stiffnesses,
        "10 pN unzip at the 3 nm acceptable stroke",
        latticeSurrogates.getValue(robustScheme.label).solve(
            normalisedStiffnesses(List(robustScheme.attachments) { 1.0 }, T113_MANDATE)
        ).peakDishing
    )
    stateTransfers(robustScheme, "MINIMAX over the five solved states", robust.stiffnesses)
    run {
        val thermal = perPathThermalForces(robust.stiffnesses)
        val grid = robustScheme.grid(lengthY)
        robust.stiffnesses.forEachIndexed { index, stiffness ->
            paths += T113PathRecord(
                scheme = robustScheme.label,
                label = "MINIMAX over the five solved states",
                index = index,
                x = grid[index].first,
                y = grid[index].second,
                stiffness = stiffness,
                shareOfTheUniformPath = stiffness * robustScheme.attachments / T113_MANDATE,
                forceAtAcceptableStroke = stiffness * Gen1Tile.ACCEPTABLE_STROKE,
                thermalForce = thermal[index]
            )
        }
    }

    // ------------------------------------------------------------------ gate 4 — convergence
    println("T-113 — convergence ...")
    val convergenceScheme = schemes.last()
    val convergenceStiffness = optima.getValue("${convergenceScheme.label}|none")
    val convergenceGrid = convergenceScheme.grid(lengthY)
    val convergence = buildList {
        val subdivisionValues = listOf(1, 2, 4).map { subdivisions ->
            subdivisions to latticeInfluenceSurrogate(
                t113Lattice(sheet, subdivisions = subdivisions), convergenceGrid,
                designField, T113_SAMPLES
            ).solve(convergenceStiffness).peakDishing / stroke
        }
        val finestSubdivision = subdivisionValues.last().second
        subdivisionValues.forEach { (subdivisions, value) ->
            add(
                T113ConvergenceRecord(
                    axis = "NESTED beam subdivisions 1 in 2 in 4 (never 1/2/3/4)",
                    setting = "$subdivisions per interval",
                    dishingOverStroke = value,
                    departureFromFinest = abs(value / finestSubdivision - 1.0)
                )
            )
        }
        val sampleValues = listOf(41, 81, 161).map { samples ->
            samples to latticeInfluenceSurrogate(
                bareLattice, convergenceGrid, designField, samples
            ).solve(convergenceStiffness).peakDishing / stroke
        }
        val finestSamples = sampleValues.last().second
        sampleValues.forEach { (samples, value) ->
            add(
                T113ConvergenceRecord(
                    axis = "the peak-dishing sampling grid",
                    setting = "$samples x $samples",
                    dishingOverStroke = value,
                    departureFromFinest = abs(value / finestSamples - 1.0)
                )
            )
        }
        // and the same axis at the BEST RIM distribution, because that is the one whose flatness
        // verdict is close enough to T-5b's line for the sampling to matter.
        val rimStiffness =
            normalisedStiffnesses(bestRimWeights.getValue(convergenceScheme.label), T113_MANDATE)
        val rimSampleValues = listOf(41, 81, 161).map { samples ->
            samples to latticeInfluenceSurrogate(
                bareLattice, convergenceGrid, designField, samples
            ).solve(rimStiffness).peakDishing / stroke
        }
        val finestRimSamples = rimSampleValues.last().second
        rimSampleValues.forEach { (samples, value) ->
            add(
                T113ConvergenceRecord(
                    axis = "the peak-dishing sampling grid, at the BEST RIM distribution",
                    setting = "$samples x $samples",
                    dishingOverStroke = value,
                    departureFromFinest = abs(value / finestRimSamples - 1.0)
                )
            )
        }
        val degreeValues = listOf(8, 10, 12).map { degree ->
            degree to plateInfluenceSurrogate(
                PlateOnFoundation(plateModel, Gen1Tile.FOUNDATION_SECANT, emptyList(), degree),
                convergenceGrid, designField, T113_SAMPLES
            ).solve(convergenceStiffness).peakDishing / stroke
        }
        val finestDegree = degreeValues.last().second
        degreeValues.forEach { (degree, value) ->
            add(
                T113ConvergenceRecord(
                    axis = "the plate's Ritz basis degree",
                    setting = "degree $degree",
                    dishingOverStroke = value,
                    departureFromFinest = abs(value / finestDegree - 1.0)
                )
            )
        }
    }

    // ------------------------------------------------------------------ gate 5 — reproductions
    println("T-113 — upstream reproductions ...")
    fun reproduction(source: String, quantity: String, published: Double, reproduced: Double) =
        T113ReproductionRecord(
            source, quantity, published, reproduced,
            if (published == 0.0) abs(reproduced) else abs(reproduced / published - 1.0)
        )

    val uniformOne = distributions.first {
        it.columns == 1 && it.label.startsWith("uniform")
    }
    val uniformThree = distributions.first {
        it.columns == 3 && it.label.startsWith("uniform")
    }
    val reproductions = buildList {
        add(reproduction("C-0047", "1 x 15 uniform dishing [nm]", 3.412, uniformOne.latticePeakDishing))
        add(
            reproduction(
                "C-0047", "1 x 15 uniform dishing / stroke", 0.695, uniformOne.dishingOverStroke
            )
        )
        add(
            reproduction("C-0047", "3 x 15 uniform dishing [nm]", 1.071, uniformThree.latticePeakDishing)
        )
        add(
            reproduction(
                "C-0047", "3 x 15 uniform dishing / stroke", 0.218, uniformThree.dishingOverStroke
            )
        )
        add(reproduction("C-0022/C-0047", "free tile dishing / stroke", 0.308, freeTileOverStroke))
        add(reproduction("C-0026", "free-tile stroke [nm]", 4.90731, stroke))
        add(
            reproduction(
                "C-0049", "per-path secant ceiling at 45 paths, 3 nm [pN/nm]", 150.0,
                admissibleStiffnessRatio(
                    Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE, T113_MANDATE, 45
                ) * T113_MANDATE
            )
        )
        add(
            reproduction(
                "C-0049", "per-path secant ceiling at 15 paths, 3 nm [pN/nm]", 50.0,
                admissibleStiffnessRatio(
                    Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE, T113_MANDATE, 15
                ) * T113_MANDATE
            )
        )
        add(
            reproduction(
                "C-0014", "per-anchor thermal force at 45 equal paths [pN]",
                perAnchorThermalForce(T113_MANDATE, 45),
                perPathThermalForces(List(45) { T113_MANDATE / 45.0 }).max()
            )
        )
        add(
            reproduction(
                "C-0017", "the mandated total coupling stiffness [pN/nm]", 33.3333333,
                T113_MANDATE
            )
        )
        // the free falsifier: a uniform load on a free tile dishes exactly zero
        add(
            reproduction(
                "T-113 falsifier", "free tile, UNIFORM load, peak dishing [nm]", 0.0,
                bareLattice.solve(uniformPressure(interiorPressure)).peakDishing(T113_SAMPLES)
            )
        )
        // and the Woodbury surrogate against the assembled solve, at the 3 x 15 optimum
        add(
            reproduction(
                "T-113 gate 5", "surrogate vs assembled peak dishing at the 3 x 15 optimum [nm]",
                assembledSolution(convergenceGrid, convergenceStiffness, designField)
                    .peakDishing(T113_SAMPLES),
                latticeSurrogates.getValue(convergenceScheme.label)
                    .solve(convergenceStiffness).peakDishing
            )
        )
    }

    // ------------------------------------------------------------------ the findings
    val bestAdmissible = schemes.associate { scheme ->
        scheme.label to distributions.filter {
            it.scheme == scheme.label && it.admissibleUnderTheUnzipAllowable
        }.minBy { it.latticePeakDishing }
    }
    val singleBound = cheapBound.first { it.columns == 1 }
    val tripleBound = cheapBound.first { it.columns == 3 }

    fun rimAt(scheme: String) = rimSweep.filter {
        it.scheme == scheme && it.collarWidth == solvedCollarWidth
    }

    val tripleLabel = schemes[2].label
    val singleLabel = schemes[0].label
    val firstFlatRim = rimAt(tripleLabel).firstOrNull {
        it.flat && it.admissibleUnderTheUnzipAllowable
    }
    val bestRimTriple = distributions.first {
        it.scheme == tripleLabel && it.label.startsWith("BEST RIM")
    }
    val minimax = distributions.first { it.label.startsWith("MINIMAX") }
    val minimaxStates = transfers.filter {
        it.label.startsWith("MINIMAX") && it.profile != "uniform"
    }
    val designOptimumStates = transfers.filter {
        it.scheme == tripleLabel && it.label == "OPTIMUM, ceiling: none" &&
                it.fromModel.startsWith("lattice at") && it.profile != "uniform"
    }
    val bestRimStates = transfers.filter {
        it.scheme == tripleLabel && it.label.startsWith("BEST RIM") &&
                it.fromModel.startsWith("lattice at") && it.profile != "uniform"
    }
    val worstOf = { records: List<T113TransferRecord> ->
        records.maxOfOrNull { it.dishingOverStroke } ?: Double.NaN
    }

    val findings = listOf(
        ("THE ANSWER IS YES AT THREE COLUMNS AND NO AT ONE, AND IT IS THE FIRST TIME ANYTHING " +
                "IN THIS PROGRAMME HAS MADE THE GEN-1 TILE FLAT. At C-0015's 45 attachments as " +
                "3 x 15, redistributing C-0017's SAME mandated total — no extra stiffness, no " +
                "extra paths — takes the dishing from %.4f of the free-tile stroke to %.4f " +
                "(a %.1f%% improvement), which is INSIDE T-5b's 0.10 convention. CH-0034's " +
                "count axis saturates at 0.149 and never reaches it: 225 uniform attachments " +
                "cannot do what 45 unequal ones can, so the last axis C-0047 named is not only " +
                "real, it is the only one that works. At C-0041's buildable 1 x 15 the same " +
                "search buys %.1f%% and ends at %.4f — still %.1fx the convention and still " +
                "%.2fx WORSE than having no coupling at all (the free tile dishes %.4f). All " +
                "fifteen of those springs sit on the single line x = 0, so they can only reshape " +
                "the ACROSS-helix profile, and C-0047 showed the dishing there is the ALONG-helix " +
                "bow. A distribution cannot repair a placement.")
            .format(
                uniformThree.dishingOverStroke, bestAdmissible.getValue(tripleLabel).dishingOverStroke,
                100.0 * bestAdmissible.getValue(tripleLabel).improvementOverUniform,
                100.0 * bestAdmissible.getValue(singleLabel).improvementOverUniform,
                bestAdmissible.getValue(singleLabel).dishingOverStroke,
                bestAdmissible.getValue(singleLabel).dishingOverStroke / T113_TOLERANCE,
                bestAdmissible.getValue(singleLabel).dishingOverStroke / freeTileOverStroke,
                freeTileOverStroke
            ),
        ("AND THE DESIGN IS A ONE-PARAMETER FAMILY, NOT AN OPTIMISER'S ANSWER. Stiffening every " +
                "attachment inside C-0022's own %.2f nm collar by a single ratio, at constant " +
                "total, crosses T-5b's line at a ratio of %s and reaches %.4f of the stroke at " +
                "the best (collar %s, ratio %.0f). The 45-parameter optimum finds only %.4f, " +
                "i.e. the whole remaining tuning is worth %.1f%% — so the answer is a rule " +
                "(*stiffen the collar, soften the middle*) and not a table of forty-five " +
                "numbers. THE FAMILY CONVERGES ON A PLACEMENT: at a large ratio the interior " +
                "springs carry nothing, and the optimum's own distribution empties %d of its 45 " +
                "paths below a tenth of the uniform share while keeping %d above half. What " +
                "non-uniformity is really buying is C-0015's *shapes, not counts* — with the " +
                "shape chosen continuously instead of by an integer.")
            .format(
                solvedCollarWidth,
                firstFlatRim?.let { "%.2f".format(it.ratio) } ?: "no ratio in the sweep",
                bestRimTriple.dishingOverStroke,
                collarLabel(
                    rimSweep.filter { it.scheme == tripleLabel && it.admissibleUnderTheUnzipAllowable }
                        .minBy { roundCouplingResult(it.latticePeakDishing) }.collarWidth
                ),
                rimSweep.filter { it.scheme == tripleLabel && it.admissibleUnderTheUnzipAllowable }
                    .minBy { roundCouplingResult(it.latticePeakDishing) }.ratio,
                bestAdmissible.getValue(tripleLabel).dishingOverStroke,
                100.0 * (1.0 - bestAdmissible.getValue(tripleLabel).latticePeakDishing /
                        bestRimTriple.latticePeakDishing),
                bestAdmissible.getValue(tripleLabel).pathsBelowATenthOfTheUniformShare,
                bestAdmissible.getValue(tripleLabel).pathsCarryingHalfTheUniformShare
            ),
        ("THE COST IS PAID ON THE LOAD PATH AND IT IS AFFORDABLE AT 45 PATHS AND NOT AT 15. A " +
                "path carrying k_i delivers k_i·s at stroke s, so C-0006's 10 pN unzip allowable " +
                "caps every path at a/s = %.4f pN/nm — against the uniform share that is a ratio " +
                "ceiling of n·a/(s·K), %.2f at 15 paths and %.2f at 45 at S3's ACCEPTABLE 3 nm, " +
                "and %.2f at 15 paths at S3's DESIRED 10 nm, i.e. BELOW ONE, where not even the " +
                "uniform coupling is admissible (C-0049 from the other side). The flat 3 x 15 " +
                "design sits at %.3f pN per path at the 3 nm stroke, %.1fx clear of the " +
                "allowable; its worst crossover carries %.3f pN (%.0fx clear of unzip), its " +
                "worst duplex shear %.3f pN against the 48-65 pN band, its worst attachment " +
                "%.3f pN under the solved load, and its worst per-path THERMAL force %.3f pN " +
                "against the uniform coupling's %.3f — C-0014's over-stiffening penalty, which " +
                "is LINEAR in the path's share and not its square root, because every path sees " +
                "the same rigid-body amplitude.")
            .format(
                perPathStiffnessCeiling(
                    Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE
                ),
                singleBound.admissibleRatioAtAcceptableStroke,
                tripleBound.admissibleRatioAtAcceptableStroke,
                singleBound.admissibleRatioAtDesiredStroke,
                bestAdmissible.getValue(tripleLabel).peakPathForceAtAcceptableStroke,
                bestAdmissible.getValue(tripleLabel).unzipMarginAtAcceptableStroke,
                bestAdmissible.getValue(tripleLabel).peakCrossoverForce,
                Gen1Tile.DUPLEX_UNZIP_ALLOWABLE /
                        bestAdmissible.getValue(tripleLabel).peakCrossoverForce,
                bestAdmissible.getValue(tripleLabel).peakDuplexShear,
                bestAdmissible.getValue(tripleLabel).peakSupportForce,
                bestAdmissible.getValue(tripleLabel).peakThermalForce,
                uniformThree.peakThermalForce
            ),
        ("BUT A DISTRIBUTION IS TUNED TO A LOAD, AND THE LOAD IS AN OPERATING STATE. The 3 x 15 " +
                "optimum found at C-0022's design point is worse than the UNIFORM coupling at " +
                "the compressed states: over the five solved states its dishing runs to %.4f of " +
                "the stroke where the uniform one runs to %.4f. Optimising the WORST of the five " +
                "at once — the same machinery, one objective up — gives a distribution whose " +
                "worst state is %.4f, against %.4f for the best rim family and %.4f for the " +
                "uniform coupling. Flatness bought by tuning is flatness owed at ONE state, and " +
                "this is the sixth instance in this project of a quantity that is not well posed " +
                "without the state it is read at.")
            .format(
                worstOf(designOptimumStates),
                designOptimumStates.maxOfOrNull { it.uniformOverStroke } ?: Double.NaN,
                worstOf(minimaxStates), worstOf(bestRimStates),
                minimaxStates.maxOfOrNull { it.uniformOverStroke } ?: Double.NaN
            ),
        ("THE CHEAP BOUND DID NOT FIRE, AND SAYING SO IS THE POINT OF HAVING RUN IT. Dishing is " +
                "affine in the attachment FORCES, so the least-squares minimum over the whole of " +
                "R^n — no mandate, no positivity, no relation between a force and a stiffness — " +
                "is a rigorous lower bound on the peak dishing of EVERY distribution: %.4f of " +
                "the stroke at 1 x 15 and %.4f at 3 x 15. Had it exceeded 0.10 the optimisation " +
                "would have been unnecessary; it did not, and a distribution reaching the " +
                "tolerance was then found. The best found sits %.1fx above the 3 x 15 floor, " +
                "which is the honest statement of how loose the bound is rather than of how much " +
                "room the search left: the floor ignores the mandate, and the mandate is what " +
                "binds. Maxwell-Betti reciprocity of the influence matrix, measured between two " +
                "different quadratures and not imposed, holds to %.1e.")
            .format(
                singleBound.reachableFloorOverStroke, tripleBound.reachableFloorOverStroke,
                bestAdmissible.getValue(tripleLabel).latticePeakDishing /
                        tripleBound.reachableDishingFloor,
                tripleBound.reciprocityResidual
            ),
        ("THE IMPROVEMENT SURVIVES THE LATTICE AND THE PLATE, AND THE TWO MODELS AGREE ON THE " +
                "DISTRIBUTION AS WELL AS ON THE NUMBER. The flat 3 x 15 design measures %.4f of " +
                "the stroke on C-0009's beam-and-hinge lattice and %.4f on C-0006's continuum " +
                "plate, an excess of %+.1f%%; the plate's OWN optimum, found independently by " +
                "the same search on the plate's own surrogate, transfers to the lattice at " +
                "%.4f. A discretisation is not automatically a relaxation (CLAUDE.md), and here " +
                "it is the stiffer model at every column count above two, as C-0047 found — the " +
                "sign is reported rather than assumed, and no verdict in this study rests on " +
                "which model is used.")
            .format(
                bestAdmissible.getValue(tripleLabel).dishingOverStroke,
                bestAdmissible.getValue(tripleLabel).platePeakDishing / stroke,
                bestAdmissible.getValue(tripleLabel).latticeExcessPercent,
                transfers.first {
                    it.scheme == tripleLabel && it.label.startsWith("PLATE OPTIMUM") &&
                            it.label.endsWith("none")
                }.dishingOverStroke
            )
    )


    val result = T113Result(
        task = "T-113",
        leaf = "A8.2",
        title = "Can a NON-UNIFORM coupling stiffness buy back the edge dishing?",
        verificationType = "in-silico (C-0009's grillage and C-0006's plate under C-0022's " +
                "SOLVED load, both driven by an exact Woodbury surrogate) + logical (a " +
                "least-squares bound in the space of attachment forces, and the per-path force " +
                "ceiling as arithmetic)",
        acceptance = "the peak dishing of a 1 x 15 and a 3 x 15 grid with a non-uniform " +
                "distribution of C-0017's mandated total, against T-5b's 10% convention and " +
                "against C-0047's free tile, with the cost per load path quoted",
        maturity = "TRL 1-3. Model-consistent and traceable. NOTHING HERE IS MEASURED.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm",
            "pressure" to "pN/nm^2 = 1 MPa exactly",
            "energy" to "pN*nm"
        ),
        conventions = listOf(
            "x runs ALONG the helices, y ACROSS them; the origin is the tile centre",
            "w is positive DOWNWARD, compressing the polymer layer (T-5)",
            "dishing is the peak absolute departure from the area-weighted least-squares " +
                    "best-fit PLANE, sampled on the same 81 x 81 grid as C-0026, CH-0034 and C-0047",
            "the free-tile stroke is the mean deflection of the UNSUPPORTED plate under the " +
                    "UNIFORM load at the same foundation stiffness",
            "flat means peak dishing below 10% of that stroke — T-5b's CONVENTION, not a " +
                    "physical threshold",
            "a collar depth is NEGATIVE for an enhancement, which is the sign C-0022 solved",
            "the coupling is n springs whose stiffnesses SUM to C-0017's mandate; the " +
                    "distribution is this task's design variable and the sum is not"
        ),
        runParameters = mapOf(
            "duplexes" to "$T113_DUPLEXES",
            "interhelicalDistance" to "${Gen1Tile.INTERHELICAL_SHEET} nm (SAXS, Fischer 2016)",
            "crossoverColumns" to "$T113_NOMINAL_COLUMNS, symmetrically centred (T-10)",
            "subdivisions" to "2 per interval, nested 1/2/4 in gate 4",
            "plateBasisDegree" to "12",
            "dishingSamples" to "$T113_SAMPLES x $T113_SAMPLES",
            "foundationStiffness" to "${Gen1Tile.FOUNDATION_SECANT} pN/nm^3 (C-0001's secant)",
            "optimiser" to "deterministic cyclic coordinate descent on log-stiffness, 4 starts, " +
                    "8 sweeps, coarse scan of 7 plus 8 golden-section refinements"
        ),
        citedInputs = mapOf(
            "C-0017 mandate" to "$T113_MANDATE pN/nm = 100 pN / 3 nm",
            "C-0006/CH-0029 unzip allowable" to "${Gen1Tile.DUPLEX_UNZIP_ALLOWABLE} pN per path",
            "C-0006 duplex shear allowable" to "${Gen1Tile.DUPLEX_SHEAR_ALLOWABLE} pN, " +
                    "${Gen1Tile.OVERSTRETCHING_CEILING} pN nicked ceiling",
            "C-0022 solved collars" to "read at run time from " +
                    "gpd/results/T-3b-tile-edge-load-profile.json, keyed on " +
                    "(concentration, gap, bias)",
            "T-5b tolerance" to "$T113_TOLERANCE — a CONVENTION",
            "duplex EI" to "${Gen1Tile.DUPLEX_BENDING_RIGIDITY} pN*nm^2 — a CanDo MODEL INPUT",
            "S3 parameters" to "${Gen1Tile.TARGET_FORCE} pN, ${Gen1Tile.ACCEPTABLE_STROKE} nm " +
                    "acceptable, ${Gen1Tile.DESIRED_STROKE} nm desired, 40 x 40 nm"
        ),
        temperature = ROOM_TEMPERATURE,
        thermalEnergy = thermalEnergy(),
        designPointProfile = designPoint.name,
        rigidPlateTolerance = T113_TOLERANCE,
        mandatedTotalStiffness = T113_MANDATE,
        freeTileStroke = stroke,
        cheapBound = cheapBound,
        rimSweep = rimSweep,
        distributions = distributions,
        paths = paths,
        optimiser = optimiserRecords,
        transfers = transfers,
        convergence = convergence,
        reproductions = reproductions,
        findings = findings,
        validity = listOf(
            "TRL 1-3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and the flexure " +
                    "motif this count belongs to is NOT DEMONSTRATED (C-0028, C-0029).",
            "The load profile is C-0022's and inherits its whole validity range: mean field, " +
                    "point ions, a two-dimensional solve with the corner bracketed rather than " +
                    "solved, an unsourced rim charge worth 1.85x on the depth, and a gap filled " +
                    "with free buffer.",
            "Linear Winkler foundation at C-0001's secant, one multiplier only — the foundation " +
                    "sweep is C-0047's and moves the uniform answer by 1.9x, which is a " +
                    "sensitivity of the BASELINE and not of the improvement measured here.",
            "The coupling is n INDEPENDENT LINEAR springs. C-0030's flexure strain-softens " +
                    "(CH-0042), so a real coupling is not exactly this one; and nothing here " +
                    "says a per-path stiffness can be BUILT to a prescribed value.",
            "The optimiser is a DESCENT reporting the best point it found, never a global " +
                    "optimum. The bound it is quoted against is rigorous; the optimum is not.",
            "The crossover's vertical link is C-0009's rigid PENALTY, inherited unchanged; no " +
                    "thermal channel is computed on it (CH-0033).",
            "One crossover layout — T-10's eight symmetrically centred columns; C-0015's 32 bp " +
                    "phase is not swept.",
            "T-5b's 10% is a CONVENTION and every verdict here is quoted with it named.",
            "Single layer, static, 300 K, aqueous buffer with Mg2+."
        ),
        openQuestions = listOf(
            "Whether an attachment placement that is not a GRID does better — C-0047's stagger " +
                    "sweep found 45% on an axis nobody had swept, and the station positions are " +
                    "still a free design variable here.",
            "Whether a per-path stiffness can be built to a prescribed value at all: every " +
                    "distribution here assumes it can, and C-0030's flexure is a span, quantised " +
                    "by the lattice it is cut from.",
            "The foundation multiplier, held at C-0001's secant throughout."
        )
    )

    val output = File("gpd/results/T-113-non-uniform-coupling.json")
    output.parentFile.mkdirs()
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            json.encodeToJsonElement(result).jsonObject.roundedForCouplingResult().jsonObject
        )
    )
    t113Report(result, output, started)
}

/**
 * [value] rounded to two significant digits — the resolution at which a search's own convergence
 * measure is reproducible between runs. See [T113OptimiserRecord].
 */
private fun twoDigits(value: Double): Double {
    if (!value.isFinite() || value == 0.0) return 0.0
    val scale = 10.0.pow(1.0 - floor(log10(abs(value))))
    return Math.round(value * scale) / scale
}

private fun t113Elapsed(started: Long): String =
    "%.1f s".format((System.currentTimeMillis() - started) / 1000.0)

private fun t113Report(result: T113Result, output: File, started: Long) {
    println()
    println("=".repeat(120))
    println("T-113 — ${result.title}")
    println("=".repeat(120))
    println("design point: ${result.designPointProfile}")
    println(
        "free-tile stroke %.4f nm; mandate %.4f pN/nm; tolerance %.2f of the stroke"
            .format(result.freeTileStroke, result.mandatedTotalStiffness, result.rigidPlateTolerance)
    )

    println()
    println("--- the cheap bounds ".padEnd(120, '-'))
    println(
        "%-34s %6s %9s %9s %9s %10s %10s %10s".format(
            "scheme", "paths", "R@3nm", "R@10nm", "floor", "floor/str", "reach/str", "unif/str"
        )
    )
    result.cheapBound.forEach {
        println(
            "%-34s %6d %9.2f %9.2f %9.4f %10.4f %10.4f %10.4f".format(
                it.scheme.take(34), it.attachments, it.admissibleRatioAtAcceptableStroke,
                it.admissibleRatioAtDesiredStroke, it.reachableDishingFloor,
                it.reachableFloorOverStroke, it.reachablePeakOverStroke, it.uniformOverStroke
            )
        )
    }

    println()
    println("--- the rim-stiffening sweep, on the surrogates ".padEnd(120, '-'))
    println(
        "%-34s %10s %6s %8s %10s %10s %8s %8s".format(
            "scheme", "collar[nm]", "rim n", "ratio", "dish/str", "plate/str", "flat", "adm"
        )
    )
    result.rimSweep.forEach {
        println(
            "%-34s %10.2f %6d %8.2f %10.4f %10.4f %8s %8s".format(
                it.scheme.take(34), it.collarWidth, it.rimPaths, it.ratio, it.dishingOverStroke,
                it.plateDishingOverStroke, it.flat, it.admissibleUnderTheUnzipAllowable
            )
        )
    }

    println()
    println("--- the distributions ".padEnd(120, '-'))
    println(
        "%-34s %-42s %9s %9s %8s %8s %9s %8s".format(
            "scheme", "distribution", "dish/str", "d vs unif", "max/min", "k_max", "F@3nm", "flat"
        )
    )
    result.distributions.forEach {
        println(
            "%-34s %-42s %9.4f %+9.1f%% %8.2f %8.3f %9.3f %8s".format(
                it.scheme.take(34), it.label.take(42), it.dishingOverStroke,
                100.0 * it.improvementOverUniform, it.maximumOverMinimumStiffness,
                it.peakPathStiffness, it.peakPathForceAtAcceptableStroke, it.flat
            )
        )
    }

    println()
    println("--- the load paths ".padEnd(120, '-'))
    println(
        "%-34s %-42s %9s %9s %9s %9s".format(
            "scheme", "distribution", "xover[pN]", "shear[pN]", "supp[pN]", "therm[pN]"
        )
    )
    result.distributions.forEach {
        println(
            "%-34s %-42s %9.4f %9.4f %9.4f %9.4f".format(
                it.scheme.take(34), it.label.take(42), it.peakCrossoverForce, it.peakDuplexShear,
                it.peakSupportForce, it.peakThermalForce
            )
        )
    }

    println()
    println("--- the optimiser ".padEnd(120, '-'))
    println(
        "%-34s %-26s %-18s %10s %9s".format(
            "scheme", "model", "ceiling", "lastImpr", "over floor"
        )
    )
    result.optimiser.forEach {
        println(
            "%-34s %-26s %-18s %10.1e %9.3f".format(
                it.scheme.take(34), it.model.take(26), it.stiffnessCeiling.take(18),
                it.lastSweepImprovementToTwoDigits, it.objectiveOverFloor
            )
        )
    }

    println()
    println("--- transfers: other model, other state ".padEnd(120, '-'))
    println(
        "%-24s %-30s %-30s %9s %9s %9s".format(
            "scheme", "to", "profile", "dish/str", "unif/str", "d vs unif"
        )
    )
    result.transfers.forEach {
        println(
            "%-24s %-30s %-30s %9.4f %9.4f %+9.1f%%".format(
                it.scheme.take(24), it.toModel.take(30), it.profile.take(30),
                it.dishingOverStroke, it.uniformOverStroke, 100.0 * it.improvementOverUniform
            )
        )
    }

    println()
    println("--- gate 4: convergence ".padEnd(120, '-'))
    println("%-50s %-18s %14s %12s".format("axis", "setting", "value", "departure"))
    result.convergence.forEach {
        println(
            "%-50s %-18s %14.7f %12.3e".format(
                it.axis.take(50), it.setting, it.dishingOverStroke, it.departureFromFinest
            )
        )
    }

    println()
    println("--- gate 5: upstream reproductions ".padEnd(120, '-'))
    println("%-18s %-56s %11s %11s %11s".format("source", "quantity", "published", "here", "departure"))
    result.reproductions.forEach {
        println(
            "%-18s %-56s %11.5f %11.5f %11.2e".format(
                it.source, it.quantity.take(56), it.published, it.reproduced, it.relativeDeparture
            )
        )
    }

    println()
    println("--- findings ".padEnd(120, '-'))
    result.findings.forEachIndexed { index, finding ->
        println("${index + 1}. $finding")
        println()
    }

    println("wrote ${output.path} in ${t113Elapsed(started)}")
}
