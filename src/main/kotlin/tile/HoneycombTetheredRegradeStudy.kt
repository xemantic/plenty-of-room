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

package com.xemantic.nano.plentyofroom.tile

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.anchoring.MeasuredBackbone
import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.DropoutEnsemble
import com.xemantic.nano.plentyofroom.coupling.InfluenceSurrogate
import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.coupling.dropoutDishingSample
import com.xemantic.nano.plentyofroom.coupling.dropoutEnsemble
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.coupling.measuredDepthIncorporation
import com.xemantic.nano.plentyofroom.coupling.summariseDropoutDishing
import com.xemantic.nano.plentyofroom.coupling.worstSinglePathRemoval
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import com.xemantic.nano.plentyofroom.structure.maximumTurnPhosphateSpan
import com.xemantic.nano.plentyofroom.structure.minimumTurnPhosphateSpan
import com.xemantic.nano.plentyofroom.structure.roundForResult
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
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

// ---------------------------------------------------------------------------------------------
// T-299 -- route B: the raster turn as an entropic TETHER.
//
// C-0193 / CH-0247: C-0175 adds 59 COVALENT ties on the ground that a raster turn "is a covalent
// crossover like any other" -- true of a turn carrying ZERO unpaired nucleotides, which is
// route A. The built precedent's own accounting puts 28 unpaired nucleotides at every turn, 14
// at each helix end, so the covalent link sits 4.76 nm OUTBOARD of the duplex end on each helix
// and what stands between the two rim nodes is ssDNA. No lattice in this repository carries one.
//
// A freely-jointed chain transmits a FORCE and no MOMENT, so route B's turn carries no dihedral
// spring at all -- and it carries a PRELOAD, because a chain held at any x > 0 is in tension.
//
// This study is the SAME 64 coupled cells, the SAME stations and distributions and the SAME
// 4 000-realisation dropout stream C-0167, C-0180 and C-0187 share, over FOUR lattice states:
// untied (C-0167's own), tied (route A, C-0180's own), and two tethered ones.
//
// CH-0251 disputes whether route B was built at all. This is the other arm of that fork.
// ---------------------------------------------------------------------------------------------

private const val T299_SAMPLES: Int = 81
private const val T299_TOLERANCE: Double = 0.10
private const val T299_RIM_STANDOFF: Double = 1.0
private const val T299_RIM_BAND: Double = 6.7
private const val T299_SEED: Long = 197_197L
private const val T299_BLOCK_EXTENT_BP: Int = 116
private const val T299_LADDER_PHASE: Int = 16
private const val T299_LADDER_OFFSET: Int = 14
private const val T299_RECOMMENDED_ONE: Int = 102
private const val T299_RECOMMENDED_TWO: Int = 109

/**
 * The single electrostatic state `C-0022`'s solved collar is read at.
 *
 * `emission.regime` is **null** here and that is a judgement rather than an omission
 * (`C-0181`/`CH-0224`): `environment.Regime` holds the gap and the bias as **intervals**, and its
 * own `require(highestHeightNm > lowestHeightNm)` refuses a degenerate one — correctly, because a
 * regime identifies the range a solve is a function OVER. This study solves no electrostatics at
 * all: it reads **one** profile record and uses it as a fixed load shape. So the state is not a
 * range and belongs in `parameters`, where these three constants put it.
 */
private const val T299_BUFFER_MILLIMOLAR: Double = 2.0
private const val T299_GAP_NM: Double = 10.0
private const val T299_BIAS_VOLTS: Double = 0.192

/** The relative tolerance every same-quantity identity is asserted at, as a THRESHOLD. */
private const val T299_IDENTITY: Double = 1e-9

/** The built precedent's own unpaired half of its 126 nt per-helix allotment (C-0193 section 3). */
private const val T299_UNPAIRED_PER_HELIX: Int = 28
private const val T299_HELICES: Int = 60

private const val T299_M13: Int = 7249
private const val T299_P7560: Int = 7560
private const val T299_P8064: Int = 8064

/** The zero-force ssDNA Kuhn bracket and the inextensible contour that travels with it (T-230). */
private val T299_KUHN = listOf(2.10, 2.84)
private val T299_CONTOUR = listOf(0.65, 0.70)

/** The two loop lengths that are reachable on a real scaffold at this raster (C-0193 section 8). */
private const val T299_BUILT_LOOP: Int = 28
private const val T299_M13_LOOP: Int = 15

/**
 * `C-0200`'s refinement: the ordered split is `12 / 16` per helix, not `14 / 14`, so a turn joins
 * two duplex ends `24` nucleotides apart at one rim and `32` at the other. Their mean is 28.
 */
private const val T299_ORDERED_LOW_RIM: Int = 24
private const val T299_ORDERED_HIGH_RIM: Int = 32

/** The study runs at 4 000 realisations; `T299_SMOKE=1` drops it to 150 for a plumbing pass. */
private val t299Realisations: Int =
    if (System.getenv("T299_SMOKE") == "1") 150 else 4000

private fun Double.emitted(digits: Int = 9): String = roundedForProse(digits).toString()

// ------------------------------------------------------------------------------ the records

@Serializable
private class T299CheapBoundRow(
    val question: String,
    val answer: String,
    val consequence: String
)

@Serializable
private class T299TetherRow(
    val unpairedNucleotides: Int,
    val azimuth: String,
    val span: Double,
    val kuhnLength: Double,
    val contourPerNucleotide: Double,
    val contourLength: Double,
    val extensionRatio: Double,
    val tension: Double,
    val secantStiffness: Double,
    val tangentStiffness: Double,
    val overHingeOnTheRimArm: Double,
    val overSlipStiffness: Double,
    val overSpanLawLink: Double,
    val overRigidLinkPenalty: Double
)

@Serializable
private class T299Geometry(
    val turnState: String,
    val compositeFraction: Double?,
    val hingeStiffnessEnhancement: Double,
    val bonds: Int,
    val turnTies: Int,
    val turnTethers: Int,
    val degreesOfFreedom: Int,
    val freeStroke: Double,
    val closedFormStroke: Double,
    val strokeMatchesClosedForm: Boolean,
    val strokeIdentityTolerance: Double,
    val uncoupledDishingOverStroke: Double,
    val uncoupledFlat: Boolean
)

@Serializable
private class T299Cell(
    val turnState: String,
    val compositeFraction: Double,
    val placement: String,
    val columns: Int,
    val rows: Int,
    val pathCount: Int,
    val distribution: String,
    val nominalOverStroke: Double,
    val worstSingleRemovalOverStroke: Double,
    val medianOverStroke: Double,
    val p90OverStroke: Double,
    val p95OverStroke: Double,
    val worstOverStroke: Double,
    val exceedance: Double,
    // `T-337`. This record carried the exceedance and its saturated ONE-SIDED bound and not the
    // symmetric standard error, so the ensemble size could not be backed out of it and had to be
    // assumed -- `C-0223`'s conditions demand the opposite. `T-337`'s own gate found it: an
    // exceedance emitted without its standard error is a probability without one, which
    // `DropoutSummary`'s KDoc already forbids.
    val exceedanceStandardError: Double,
    val exceedanceOneSidedBound: Double?,
    val meanSurvivors: Double,
    val uncoupledDishingOverStroke: Double,
    val flatAtNominal: Boolean,
    val flatAtP90: Boolean,
    val beatsUncoupledAtP90: Boolean
)

@Serializable
private class T299Paired(
    val comparison: String,
    val compositeFraction: Double,
    val placement: String,
    val columns: Int,
    val pathCount: Int,
    val distribution: String,
    val realisations: Int,
    val medianRatio: Double,
    val p90Ratio: Double,
    val bestRatio: Double,
    val worstRatio: Double,
    val fractionSubjectIsWorse: Double,
    val ratioOfPercentiles: Double,
    val referenceP90OverStroke: Double,
    val subjectP90OverStroke: Double,
    val referenceFlatAtP90: Boolean,
    val subjectFlatAtP90: Boolean,
    val verdictMoved: Boolean,
    val pairedAndUnpairedDisagreeInSign: Boolean
)

@Serializable
private class T299Standoff(
    val unpairedPerHelixEnd: Int,
    val contourPerNucleotide: Double,
    val kuhnLength: Double,
    val contourLength: Double,
    val kuhnSegments: Double,
    val rootMeanSquareEndToEnd: Double,
    val duplexRiseReading: Double,
    val contourOverRiseReading: Double
)

@Serializable
private class T299Bracket(
    val unpairedNucleotides: Int,
    val azimuth: String,
    val kuhnLength: Double,
    val contourPerNucleotide: Double,
    val tension: Double,
    val tangentStiffness: Double,
    val freeTileWithPreload: Double,
    val freeTileWithoutPreload: Double,
    val movement: Double,
    val worstRimClosure: Double,
    val stericSlack: Double,
    val closureExceedsStericSlack: Boolean,
    val flatWithPreload: Boolean
)

@Serializable
private class T299Preload(
    val cell: String,
    val quantity: String,
    val withPreload: Double,
    val withoutPreload: Double,
    val movement: Double,
    val flatWithPreload: Boolean,
    val flatWithoutPreload: Boolean
)

@Serializable
private class T299LinkStiffness(
    val cell: String,
    val linkStiffness: Double,
    val ground: String,
    val p90OverStroke: Double,
    // `T-337`. `C-0223`: the verdict below IS `exceedance <= tolerance`. `T-303` TRANSCRIBES
    // these rows, so without the proportion here that study cannot publish one either.
    val exceedance: Double,
    val exceedanceStandardError: Double,
    val exceedanceOneSidedBound: Double?,
    val flatAtP90: Boolean
)

@Serializable
private class T299Width(
    val scaffold: String,
    val scaffoldNucleotides: Int,
    val loopPerTurn: Int,
    val pairedPerHelix: Int,
    val rowBasePairs: Int,
    val rowWidth: Double,
    val departureFromNominal: Double,
    val uncoupledDishingOverStroke: Double,
    val uncoupledFlat: Boolean
)

@Serializable
private class T299Convergence(
    val axis: String,
    val cell: String,
    val quantity: String,
    val coarse: String,
    val fine: String,
    val coarseValue: Double,
    val fineValue: Double,
    val departure: Double,
    val verdictAtCoarse: Boolean?,
    val verdictAtFine: Boolean?,
    val verdictSurvives: Boolean?
)

@Serializable
private class T299Reproduction(
    val source: String,
    val quantity: String,
    val published: Double,
    val here: Double,
    val departure: Double,
    val closes: Boolean
)

@Serializable
private class T299Falsifier(
    val name: String,
    val statement: String,
    val fired: Boolean,
    val note: String
)

@Serializable
private class T299Result(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: Map<String, String>,
    val parameters: Map<String, String>,
    val sources: List<String>,
    val citedInputs: Map<String, String>,
    val cheapBound: List<T299CheapBoundRow>,
    val tethers: List<T299TetherRow>,
    val standoff: List<T299Standoff>,
    val bracket: List<T299Bracket>,
    val geometries: List<T299Geometry>,
    val cells: List<T299Cell>,
    val paired: List<T299Paired>,
    val preload: List<T299Preload>,
    val linkStiffness: List<T299LinkStiffness>,
    val width: List<T299Width>,
    val verdict: Map<String, String>,
    val convergence: List<T299Convergence>,
    val reproductions: List<T299Reproduction>,
    val falsifiers: List<T299Falsifier>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val proseFailure: String
)

// ------------------------------------------------------------------------------ the geometry

private class T299Profile(
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, edgeX: Double, edgeY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, edgeY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T299_RIM_STANDOFF))
        )
}

private fun t299Profile(file: File): T299Profile {
    require(file.exists()) { "C-0022's result file is missing: " + file.path }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray
        .map { it.jsonObject }
        .firstOrNull { record ->
            fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == T299_BUFFER_MILLIMOLAR &&
                    value("gapHeight") == T299_GAP_NM &&
                    value("appliedBias") == T299_BIAS_VOLTS
        } ?: error("no C-0022 profile at the design state")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T299Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

/** `C-0167`'s geometry, unchanged — so the only thing that differs is the turn element. */
private class T299Shared(val profile: T299Profile) {
    val rasterRows: Int = 10
    val helicesPerRow: Int = 6
    val rowBasePairs: Int = T299_BLOCK_EXTENT_BP
    val block: HoneycombBlock = HoneycombBlock(rasterRows, helicesPerRow)
    val d: Double = Gen1Tile.INTERHELICAL_HONEYCOMB
    val rowPitch: Double = HoneycombCrossSectionGeometry.rowPitch(d)
    val columnPitch: Double = HoneycombCrossSectionGeometry.columnPitch(d)
    val edgeX: Double = rowBasePairs * Gen1Tile.RISE_PER_BASE_PAIR
    val edgeY: Double = rasterRows * rowPitch
    val interiorPressure: Double = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)
    val closedFormStroke: Double = interiorPressure / Gen1Tile.FOUNDATION_SECANT
    val pressureField: PressureField = profile.field(interiorPressure, edgeX, edgeY)
    val crossSection: String = "$rasterRows x $helicesPerRow"

    fun enhancementAt(fraction: Double): Double = multiLayerRigidities(
        layers = helicesPerRow,
        interhelicalDistance = d,
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
        coupling = LayerCoupling.CALIBRATED,
        compositeFraction = fraction,
        layerSpacing = columnPitch
    ).realisedEnhancement
}

/** One turn state of one enhancement — the object a whole column of the comparison is graded on. */
private class T299Tile(
    val shared: T299Shared,
    val enhancement: Double,
    val lattice: HoneycombGrillage
) {

    val freeStroke: Double by lazy {
        lattice.solve(uniformPressure(shared.interiorPressure)).meanDeflection
    }

    val uncoupledDishing: Double by lazy {
        lattice.solve(shared.pressureField).peakDishing(T299_SAMPLES) / freeStroke
    }

    /**
     * Keyed on the **grid itself** and the sample count, never on a label.
     *
     * A cache key that is not the identity of the thing cached is the defect this study hit on
     * its first full run: a label like `"grid|81"` was shared by two deciding cells with
     * different station counts, and the second read the first's surrogate — which failed loudly
     * (`expected 30 stiffnesses, was: 50`) only because the path count happened to differ.
     */
    private val cache = HashMap<Pair<List<Pair<Double, Double>>, Int>, InfluenceSurrogate>()

    fun surrogate(
        grid: List<Pair<Double, Double>>,
        samples: Int = T299_SAMPLES
    ): InfluenceSurrogate = cache.getOrPut(grid to samples) {
        honeycombTiedSurrogate(lattice, grid, shared.pressureField, samples)
    }
}

/**
 * A turn state: either a tie set (`tied`), no turn element at all, or a pair of tether chains —
 * `C-0200`'s `24` nucleotides at one rim and `32` at the other, which is one chain when the two
 * are equal.
 */
private class T299Turn(
    val label: String,
    val lowRim: HoneycombTetherState? = null,
    val highRim: HoneycombTetherState? = null,
    val tied: Boolean = false
)

private fun t299Tile(
    shared: T299Shared,
    enhancement: Double,
    turn: T299Turn,
    withPreload: Boolean = true,
    subdivisions: Int = 1,
    linkStiffness: Double = HoneycombGrillage.RIGID_LINK_STIFFNESS
): T299Tile {
    val tied = turn.tied
    val lattice = if (turn.lowRim != null && turn.highRim != null) honeycombTetheredLattice(
        block = shared.block, rowBasePairs = shared.rowBasePairs, enhancement = enhancement,
        lowRimState = turn.lowRim, highRimState = turn.highRim, withPreload = withPreload,
        subdivisions = subdivisions, linkStiffness = linkStiffness
    ) else HoneycombGrillage(
        block = shared.block,
        rowBasePairs = shared.rowBasePairs,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        hingeStiffnessEnhancement = enhancement,
        subdivisions = subdivisions,
        linkStiffness = linkStiffness,
        scaffoldTurnTies = if (!tied) emptyList() else honeycombScaffoldTurnTies(
            shared.block,
            HoneycombGrillage(
                block = shared.block,
                rowBasePairs = shared.rowBasePairs,
                foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
                subdivisions = subdivisions
            ).nodesPerBeam
        )
    )
    return T299Tile(shared, enhancement, lattice)
}

// ------------------------------------------------------------------------------ the grading

private class T299Graded(val cell: T299Cell, val sample: DoubleArray)

@Suppress("LongParameterList")
private fun gradeT299Cell(
    turnState: String,
    shared: T299Shared,
    fraction: Double,
    placement: String,
    columns: Int,
    grid: List<Pair<Double, Double>>,
    distribution: String,
    stiffnesses: List<Double>,
    surrogate: InfluenceSurrogate,
    freeStroke: Double,
    uncoupled: Double,
    ensemble: DropoutEnsemble
): T299Graded {
    val nominal = surrogate.solve(stiffnesses).peakDishing / freeStroke
    val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
    sample.indices.forEach { sample[it] = sample[it] / freeStroke }
    val summary = summariseDropoutDishing(
        sample, nominal, ensemble.meanSurvivors, T299_TOLERANCE
    )
    return T299Graded(
        T299Cell(
            turnState = turnState,
            compositeFraction = fraction,
            placement = placement,
            columns = columns,
            rows = shared.rasterRows,
            pathCount = grid.size,
            distribution = distribution,
            nominalOverStroke = nominal,
            worstSingleRemovalOverStroke =
                worstSinglePathRemoval(surrogate, stiffnesses) / freeStroke,
            medianOverStroke = summary.median,
            p90OverStroke = summary.p90,
            p95OverStroke = summary.p95,
            worstOverStroke = summary.worst,
            exceedance = summary.exceedance,
            exceedanceStandardError = summary.exceedanceStandardError,
            exceedanceOneSidedBound = summary.exceedanceOneSidedBound,
            meanSurvivors = summary.meanSurvivors,
            uncoupledDishingOverStroke = uncoupled,
            flatAtNominal = nominal < T299_TOLERANCE,
            flatAtP90 = summary.flatAtP90,
            beatsUncoupledAtP90 = summary.p90 < uncoupled
        ),
        sample
    )
}

private fun t299Distributions(
    grid: List<Pair<Double, Double>>,
    edgeX: Double,
    edgeY: Double
): List<Pair<String, List<Double>>> = listOf(
    "equal springs" to equalShareOfMandate(grid.size),
    "rim-graded 5:1" to rimGradedShareOfMandate(
        grid.map { (x, y) ->
            val onRim = abs(x) > edgeX / 2.0 - T299_RIM_BAND || abs(y) > edgeY / 2.0 - T299_RIM_BAND
            if (onRim) 5.0 else 1.0
        }
    )
)

/** `C-0167`'s four placements, unchanged, so the pairing is exact. */
private fun t299Placements(
    shared: T299Shared,
    rootingHelixY: List<Double>,
    columns: Int
): List<Pair<String, List<Pair<Double, Double>>>> {
    val abstract = attachmentGrid(columns, shared.rasterRows, shared.edgeX, shared.edgeY)
    val raster = twoLengthRaster(
        shared.rasterRows, shared.helicesPerRow, T299_RECOMMENDED_ONE, T299_RECOMMENDED_TWO
    )
    val determined = twoLengthSnappedGrid(
        raster, columns, shared.edgeY, T299_LADDER_PHASE, T299_LADDER_OFFSET
    )
    fun onHelices(grid: List<Pair<Double, Double>>) = grid.mapIndexed { index, (x, _) ->
        x to rootingHelixY[index / columns]
    }
    return listOf(
        "abstract grid" to abstract,
        "abstract grid on the rooting helices" to onHelices(abstract),
        "determined station lattice" to determined,
        "determined station lattice on the rooting helices" to onHelices(determined)
    )
}

// ------------------------------------------------------------------------------ the study

@Suppress("LongMethod", "ComplexMethod", "NestedBlockDepth")
fun main() {
    val shared = T299Shared(t299Profile(ResultInputs.T_3B.file()))
    val fractions = listOf(0.30, 0.26)
    val gradedColumns = listOf(1, 2, 3, 5)
    val kBT = thermalEnergy(ROOM_TEMPERATURE)
    val rP = MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS
    val alignedSpan = minimumTurnPhosphateSpan(shared.d, rP)
    val worstSpan = maximumTurnPhosphateSpan(shared.d, rP)
    val hingeOnRimArm = Gen1Tile.crossoverHingeStiffness() /
            ((shared.d / 2.0) * (shared.d / 2.0))
    // C-0194's span-law link: T = 2 k_theta / r_P and k_R = T / g, g = d - 2 r_P.
    val spanLawLink = (2.0 * Gen1Tile.crossoverHingeStiffness() / rP) / (shared.d - 2.0 * rP)

    // ============================================ Deliverable 1 -- the cheap bound, no solver
    println("T-299 - the cheap bound: the tether's own stiffness, against what it replaces")
    val tetherRows = ArrayList<T299TetherRow>()
    listOf(T299_M13_LOOP, 20, T299_BUILT_LOOP).forEach { nucleotides ->
        listOf(
            "aligned, d - 2 r_P" to alignedSpan,
            "line of centres, d" to shared.d,
            "worst azimuth, d + 2 r_P" to worstSpan
        ).forEach { (azimuth, span) ->
            T299_KUHN.forEach { kuhn ->
                T299_CONTOUR.forEach { contour ->
                    val state = freelyJointedTetherState(span, nucleotides, kuhn, contour, kBT)
                    tetherRows += T299TetherRow(
                        unpairedNucleotides = nucleotides,
                        azimuth = azimuth,
                        span = span,
                        kuhnLength = kuhn,
                        contourPerNucleotide = contour,
                        contourLength = state.contourLength,
                        extensionRatio = state.extensionRatio,
                        tension = state.tension,
                        secantStiffness = state.secantStiffness,
                        tangentStiffness = state.tangentStiffness,
                        overHingeOnTheRimArm = state.tangentStiffness / hingeOnRimArm,
                        overSlipStiffness =
                            state.tangentStiffness / Gen1Tile.crossoverInPlaneStiffness(),
                        overSpanLawLink = state.tangentStiffness / spanLawLink,
                        overRigidLinkPenalty =
                            state.tangentStiffness / HoneycombGrillage.RIGID_LINK_STIFFNESS
                    )
                }
            }
        }
    }
    // ---- the standoff, RE-DERIVED as a contour rather than inherited as a rise.
    //
    // C-0193's "14 bp = 4.76 nm outboard" applies the DUPLEX rise, 0.34 nm/bp, to a region that
    // is SINGLE-STRANDED. The extent of ssDNA is a CONTOUR and the contour that travels with a
    // zero-force Kuhn length is 0.65-0.70 nm/nt -- 1.91 to 2.06 times the rise, and a different
    // KIND of number: a rise is a fixed lattice step and a contour is an upper bound on an
    // extension a coil never reaches. C-0200's order splits the 28 as 12 / 16, so the two
    // per-end counts are 12 and 16 and not 14 and 14.
    //
    // And the standoff does NOT ENTER THE ELEMENT AT ALL. A freely-jointed chain joins the two
    // DUPLEX ends, both at the same rim; where the covalent link sits along it is a conformation
    // the chain's own statistics integrate over. The element is fixed by the NUCLEOTIDE COUNT
    // and by the anchor-to-anchor span in the cross-section, and the outboard distance is a
    // parameter of neither.
    println("T-299 - the standoff, re-derived as a CONTOUR")
    val standoffRows = ArrayList<T299Standoff>()
    listOf(12, 14, 16).forEach { perEnd ->
        T299_CONTOUR.forEach { contour ->
            T299_KUHN.forEach { kuhn ->
                val length = perEnd * contour
                val segments = length / kuhn
                standoffRows += T299Standoff(
                    unpairedPerHelixEnd = perEnd,
                    contourPerNucleotide = contour,
                    kuhnLength = kuhn,
                    contourLength = length,
                    kuhnSegments = segments,
                    rootMeanSquareEndToEnd = kotlin.math.sqrt(segments) * kuhn,
                    duplexRiseReading = perEnd * Gen1Tile.RISE_PER_BASE_PAIR,
                    contourOverRiseReading = length / (perEnd * Gen1Tile.RISE_PER_BASE_PAIR)
                )
            }
        }
    }
    standoffRows.forEach {
        println("  " + it.unpairedPerHelixEnd + " nt  contour " + it.contourLength.emitted(9) +
                " nm  R_rms " + it.rootMeanSquareEndToEnd.emitted(9) + " nm  against the RISE " +
                "reading " + it.duplexRiseReading.emitted(9) + " nm")
    }

    val stiffestTangent = tetherRows.maxOf { it.tangentStiffness }
    val softestTangent = tetherRows.minOf { it.tangentStiffness }
    val worstOverHinge = tetherRows.maxOf { it.overHingeOnTheRimArm }
    val cheapBound = listOf(
        T299CheapBoundRow(
            question = "how stiff is a raster turn once it is 15-28 nucleotides of ssDNA?",
            answer = "over the whole zero-force Kuhn (2.10-2.84 nm) and inextensible contour " +
                    "(0.65-0.70 nm/nt) bracket, all three azimuths and all three loop lengths, " +
                    "the tangent df/dx runs " + softestTangent.emitted(9) + " to " +
                    stiffestTangent.emitted(9) + " pN/nm",
            consequence = "against k_theta on the rim node's own d/2 arm, " +
                    hingeOnRimArm.emitted(9) + " pN/nm, that is at most " +
                    worstOverHinge.emitted(9) + "; against the lattice's link penalty " +
                    HoneycombGrillage.RIGID_LINK_STIFFNESS.emitted(2) + " pN/nm it is " +
                    tetherRows.maxOf { it.overRigidLinkPenalty }.emitted(2) +
                    ". The cheap bound PREDICTS that route B's turn is arithmetically " +
                    "indistinguishable from no turn at all, and therefore that route B's grade " +
                    "is C-0167's own untied one"
        ),
        T299CheapBoundRow(
            question = "so why run the expensive grade at all?",
            answer = "because C-0180's two recovered cells clear T-5b by 0.426 per cent and a " +
                    "3-11 per cent element is not obviously below that -- and because a tether " +
                    "is not only a stiffness: it carries a PRELOAD of " +
                    tetherRows.maxOf { it.tension }.emitted(9) + " pN at the stiffest corner, " +
                    "which is route B's analogue of route A's 8.57142857 degree prestrain and " +
                    "which no cheap bound prices",
            consequence = "the grade is a CHECK of a prediction rather than a search, which is " +
                    "what makes its cost justifiable: the machinery is T-279's unchanged and " +
                    "the only new object is one element"
        ),
        T299CheapBoundRow(
            question = "how far outboard does the covalent link sit, and does it matter?",
            answer = "not 4.76 nm. That is 14 x the DUPLEX rise applied to a region that is " +
                    "single-stranded; the extent of ssDNA is a CONTOUR, and C-0200's order " +
                    "splits the 28 as 12 / 16 rather than 14 / 14, so the two per-end counts " +
                    "are 12 and 16 with contours " +
                    standoffRows.filter { it.unpairedPerHelixEnd == 12 }
                        .minOf { it.contourLength }.emitted(9) + " to " +
                    standoffRows.filter { it.unpairedPerHelixEnd == 16 }
                        .maxOf { it.contourLength }.emitted(9) + " nm and root-mean-square " +
                    "end-to-end distances of " +
                    standoffRows.minOf { it.rootMeanSquareEndToEnd }.emitted(9) + " to " +
                    standoffRows.maxOf { it.rootMeanSquareEndToEnd }.emitted(9) + " nm",
            consequence = "and it does not enter the element at ALL: a freely-jointed chain " +
                    "joins the two DUPLEX ends, both at the same rim, and where the covalent " +
                    "link sits along it is a conformation its own statistics integrate over. " +
                    "The element is fixed by the NUCLEOTIDE COUNT and by the anchor-to-anchor " +
                    "span in the cross-section. The rise reading is out by " +
                    standoffRows.minOf { it.contourOverRiseReading }.emitted(9) + " to " +
                    standoffRows.maxOf { it.contourOverRiseReading }.emitted(9) +
                    "x AND is the wrong KIND of number"
        ),
        T299CheapBoundRow(
            question = "what is bounded before any solve, and what is not?",
            answer = "the element is positive semi-definite, so the Loewner statement " +
                    "K_tethered >= K_untied fixes the sign of the deflection AT a unit point " +
                    "load. It bounds NOTHING about peak dishing, which is a seminorm",
            consequence = "and the PRELOAD is not bounded by it at all, because a load is not a " +
                    "stiffness. Both directions are measured cell by cell"
        )
    )
    cheapBound.forEach { println("  " + it.question + " -> " + it.consequence) }

    // ============================================ Deliverable 2 -- the six turn states
    println("T-299 - the tiles")
    val builtState = freelyJointedTetherState(worstSpan, T299_BUILT_LOOP, 2.10, 0.65, kBT)
    val m13State = freelyJointedTetherState(worstSpan, T299_M13_LOOP, 2.10, 0.65, kBT)
    val mildState = freelyJointedTetherState(alignedSpan, T299_BUILT_LOOP, 2.84, 0.70, kBT)
    val lowRimState = freelyJointedTetherState(worstSpan, T299_ORDERED_LOW_RIM, 2.10, 0.65, kBT)
    val highRimState = freelyJointedTetherState(worstSpan, T299_ORDERED_HIGH_RIM, 2.10, 0.65, kBT)
    val states: List<T299Turn> = listOf(
        T299Turn("untied"),
        T299Turn("tied", tied = true),
        T299Turn("tethered, 28 nt (the built allowance, p8064)", builtState, builtState),
        T299Turn(
            "tethered, 15 nt (M13's affordance on the drawable raster)", m13State, m13State
        ),
        T299Turn(
            "tethered, 28 nt at the ALIGNED azimuth and the softest corner", mildState, mildState
        ),
        T299Turn(
            "tethered, C-0200's ORDERED split: 24 nt at one rim and 32 at the other",
            lowRimState, highRimState
        )
    )
    val tiles = HashMap<Pair<Double, String>, T299Tile>()
    val geometries = ArrayList<T299Geometry>()
    val probe = t299Tile(shared, shared.enhancementAt(0.30), T299Turn("probe"))
    val rootingHelixY = probe.lattice.faceBeams.map { probe.lattice.beamY[it] }
    (fractions + listOf(1.0)).forEach { fraction ->
        val enhancement = if (fraction == 1.0) 1.0 else shared.enhancementAt(fraction)
        states.forEach { turn ->
            val label = turn.label
            val tile = t299Tile(shared, enhancement, turn)
            if (fraction != 1.0) tiles[fraction to label] = tile
            geometries += T299Geometry(
                turnState = label,
                compositeFraction = if (fraction == 1.0) null else fraction,
                hingeStiffnessEnhancement = enhancement,
                bonds = tile.lattice.bonds.size,
                turnTies = tile.lattice.turnElements.size,
                turnTethers = tile.lattice.tetherElements.size,
                degreesOfFreedom = tile.lattice.degreesOfFreedom,
                freeStroke = tile.freeStroke,
                closedFormStroke = shared.closedFormStroke,
                strokeMatchesClosedForm = abs(tile.freeStroke - shared.closedFormStroke) <
                        T299_IDENTITY * shared.closedFormStroke,
                strokeIdentityTolerance = T299_IDENTITY,
                uncoupledDishingOverStroke = tile.uncoupledDishing,
                uncoupledFlat = tile.uncoupledDishing < T299_TOLERANCE
            )
        }
    }
    geometries.forEach {
        println("  " + it.turnState + "  f = " +
                (it.compositeFraction?.emitted(3) ?: "none") + "  uncoupled " +
                it.uncoupledDishingOverStroke.emitted(9) +
                (if (it.uncoupledFlat) "  flat" else "  NOT FLAT"))
    }

    // ============================================ Deliverable 3 -- the 64 cells, every turn state
    println("T-299 - the re-grade, " + t299Realisations + " realisations on one common stream")
    val cells = ArrayList<T299Cell>()
    val samples = HashMap<String, DoubleArray>()
    val incorporation = measuredDepthIncorporation(shared.edgeX, shared.edgeY)
    gradedColumns.forEach { columns ->
        t299Placements(shared, rootingHelixY, columns).forEach { (placement, grid) ->
            val ensemble = dropoutEnsemble(
                grid.map { (x, y) -> incorporation.at(x, y) }, t299Realisations, T299_SEED
            )
            t299Distributions(grid, shared.edgeX, shared.edgeY).forEach { (label, stiffnesses) ->
                fractions.forEach { fraction ->
                    states.forEach { turn ->
                        val turnState = turn.label
                        val tile = tiles.getValue(fraction to turnState)
                        val graded = gradeT299Cell(
                            turnState, shared, fraction, placement, columns, grid, label,
                            stiffnesses,
                            tile.surrogate(grid),
                            tile.freeStroke, tile.uncoupledDishing, ensemble
                        )
                        cells += graded.cell
                        samples[turnState + "|" + fraction + "|" + placement + "|" + columns +
                                "|" + label] = graded.sample
                    }
                }
            }
        }
    }

    // ============================================ Deliverable 4 -- the paired reading
    println("T-299 - the paired reading, per realisation on the shared stream")
    val paired = ArrayList<T299Paired>()
    val builtLabel = "tethered, 28 nt (the built allowance, p8064)"
    val m13Label = "tethered, 15 nt (M13's affordance on the drawable raster)"
    val mildLabel = "tethered, 28 nt at the ALIGNED azimuth and the softest corner"
    val orderedLabel = "tethered, C-0200's ORDERED split: 24 nt at one rim and 32 at the other"
    val builtVsUntied = "the 28 nt tethered lattice over C-0167's untied one"
    val comparisons = listOf(
        builtVsUntied to (builtLabel to "untied"),
        "the 15 nt tethered lattice over C-0167's untied one" to (m13Label to "untied"),
        "the ALIGNED-azimuth softest tethered lattice over C-0167's untied one" to
                (mildLabel to "untied"),
        "C-0200's ORDERED-split tethered lattice over C-0167's untied one" to
                (orderedLabel to "untied"),
        "the 28 nt tethered lattice over C-0180's TIED one (route B over route A)" to
                (builtLabel to "tied")
    )
    gradedColumns.forEach { columns ->
        t299Placements(shared, rootingHelixY, columns).forEach { (placement, grid) ->
            t299Distributions(grid, shared.edgeX, shared.edgeY).forEach { (label, _) ->
                fractions.forEach { fraction ->
                    comparisons.forEach { (name, pairKeys) ->
                        val (subjectState, referenceState) = pairKeys
                        val key = { state: String ->
                            state + "|" + fraction + "|" + placement + "|" + columns + "|" + label
                        }
                        val summary = pairedRatioSummary(
                            samples.getValue(key(subjectState)),
                            samples.getValue(key(referenceState))
                        )
                        fun cellOf(state: String) = cells.first {
                            it.turnState == state && it.compositeFraction == fraction &&
                                    it.placement == placement && it.columns == columns &&
                                    it.distribution == label
                        }
                        val subject = cellOf(subjectState)
                        val reference = cellOf(referenceState)
                        paired += T299Paired(
                            comparison = name,
                            compositeFraction = fraction,
                            placement = placement,
                            columns = columns,
                            pathCount = grid.size,
                            distribution = label,
                            realisations = summary.realisations,
                            medianRatio = summary.median,
                            p90Ratio = summary.p90,
                            bestRatio = summary.best,
                            worstRatio = summary.worst,
                            fractionSubjectIsWorse = summary.fractionAbove,
                            ratioOfPercentiles = summary.ratioOfPercentiles,
                            referenceP90OverStroke = reference.p90OverStroke,
                            subjectP90OverStroke = subject.p90OverStroke,
                            referenceFlatAtP90 = reference.flatAtP90,
                            subjectFlatAtP90 = subject.flatAtP90,
                            verdictMoved = reference.flatAtP90 != subject.flatAtP90,
                            pairedAndUnpairedDisagreeInSign =
                                (summary.median > 1.0) != (summary.ratioOfPercentiles > 1.0)
                        )
                    }
                }
            }
        }
    }

    // =================== Deliverable 5a -- the whole bracket on the FREE tile, which is where
    // the preload can be read without a coupling in the way. The field is exactly linear in the
    // tension, so what varies across these 36 corners is one number.
    println("T-299 - the free tile over the whole loop, azimuth, Kuhn and contour bracket")
    val stericSlack = shared.d - 2.0 * rP
    val bracketRows = ArrayList<T299Bracket>()
    val bracketEnhancement = shared.enhancementAt(0.30)
    listOf(T299_M13_LOOP, 20, T299_BUILT_LOOP).forEach { nucleotides ->
        listOf(
            "aligned, d - 2 r_P" to alignedSpan,
            "line of centres, d" to shared.d,
            "worst azimuth, d + 2 r_P" to worstSpan
        ).forEach { (azimuth, span) ->
            T299_KUHN.forEach { kuhn ->
                T299_CONTOUR.forEach { contour ->
                    val state = freelyJointedTetherState(span, nucleotides, kuhn, contour, kBT)
                    val turn = T299Turn("bracket", state, state)
                    val with = t299Tile(shared, bracketEnhancement, turn, withPreload = true)
                    val without = t299Tile(shared, bracketEnhancement, turn, withPreload = false)
                    val field = with.lattice.solve(shared.pressureField).coefficients
                    val closure = with.lattice.tetherElements
                        .maxOf { abs(with.lattice.tetherChainExtension(field, it)) }
                    bracketRows += T299Bracket(
                        unpairedNucleotides = nucleotides,
                        azimuth = azimuth,
                        kuhnLength = kuhn,
                        contourPerNucleotide = contour,
                        tension = state.tension,
                        tangentStiffness = state.tangentStiffness,
                        freeTileWithPreload = with.uncoupledDishing,
                        freeTileWithoutPreload = without.uncoupledDishing,
                        movement = with.uncoupledDishing - without.uncoupledDishing,
                        worstRimClosure = closure,
                        stericSlack = stericSlack,
                        closureExceedsStericSlack = closure > stericSlack,
                        flatWithPreload = with.uncoupledDishing < T299_TOLERANCE
                    )
                }
            }
        }
    }
    bracketRows.forEach {
        println("  " + it.unpairedNucleotides + " nt  " + it.azimuth + "  b = " +
                it.kuhnLength.emitted(3) + "  c = " + it.contourPerNucleotide.emitted(3) +
                "  f = " + it.tension.emitted(9) + " pN  free tile " +
                it.freeTileWithPreload.emitted(9) +
                (if (it.flatWithPreload) "  flat" else "  NOT FLAT") +
                "  closure " + it.worstRimClosure.emitted(9) + " nm" +
                (if (it.closureExceedsStericSlack) "  PAST THE STERIC SLACK" else ""))
    }

    // ============================================ Deliverable 5 -- the preload, isolated
    println("T-299 - the preload as a LOAD: route B's analogue of route A's prestrain")
    val preloadRows = ArrayList<T299Preload>()
    listOf(builtState to "28 nt", m13State to "15 nt").forEach { (state, name) ->
        fractions.forEach { fraction ->
            val enhancement = shared.enhancementAt(fraction)
            val turn = T299Turn("preload", state, state)
            val with = t299Tile(shared, enhancement, turn, withPreload = true)
            val without = t299Tile(shared, enhancement, turn, withPreload = false)
            preloadRows += T299Preload(
                cell = "free tile, " + name + ", f = " + fraction.emitted(3),
                quantity = "the uncoupled dishing over the stroke",
                withPreload = with.uncoupledDishing,
                withoutPreload = without.uncoupledDishing,
                movement = with.uncoupledDishing - without.uncoupledDishing,
                flatWithPreload = with.uncoupledDishing < T299_TOLERANCE,
                flatWithoutPreload = without.uncoupledDishing < T299_TOLERANCE
            )
        }
    }
    // and at the tightest coupled cell of each tethered state
    val tetheredStates = states.filter { it.lowRim != null }.map { it.label }
    tetheredStates.forEach { turnState ->
        val tightest = cells.filter { it.turnState == turnState }.minBy { it.p90OverStroke }
        val turn = states.first { it.label == turnState }
        val grid = t299Placements(shared, rootingHelixY, tightest.columns)
            .first { it.first == tightest.placement }.second
        val stiffnesses = t299Distributions(grid, shared.edgeX, shared.edgeY)
            .first { it.first == tightest.distribution }.second
        val ensemble = dropoutEnsemble(
            grid.map { (x, y) -> incorporation.at(x, y) }, t299Realisations, T299_SEED
        )
        val without = t299Tile(
            shared, shared.enhancementAt(tightest.compositeFraction), turn, withPreload = false
        )
        val sample = dropoutDishingSample(
            without.surrogate(grid), stiffnesses, ensemble
        )
        sample.indices.forEach { sample[it] = sample[it] / without.freeStroke }
        val summary = summariseDropoutDishing(
            sample,
            without.surrogate(grid).solve(stiffnesses).peakDishing / without.freeStroke,
            ensemble.meanSurvivors, T299_TOLERANCE
        )
        preloadRows += T299Preload(
            cell = "the tightest " + turnState + " cell: f = " +
                    tightest.compositeFraction.emitted(3) + ", " + tightest.placement + ", " +
                    tightest.columns + " x " + shared.rasterRows + " = " + tightest.pathCount +
                    " paths, " + tightest.distribution,
            quantity = "the p90 of the dropout ensemble, over the stroke",
            withPreload = tightest.p90OverStroke,
            withoutPreload = summary.p90,
            movement = tightest.p90OverStroke - summary.p90,
            flatWithPreload = tightest.flatAtP90,
            flatWithoutPreload = summary.flatAtP90
        )
    }
    preloadRows.forEach {
        println("  " + it.cell + "  movement " + it.movement.emitted(9))
    }

    // ============================================ Deliverable 6 -- the link stiffness axis
    println("T-299 - the link stiffness (C-0194's F10 fired on exactly this quantity)")
    val linkRows = ArrayList<T299LinkStiffness>()
    val linkLadder = listOf(
        HoneycombGrillage.RIGID_LINK_STIFFNESS to "OrigamiGrillage's own penalty",
        1e3 to "one decade down",
        1e2 to "two decades down",
        spanLawLink to "C-0194's span law, T = 2 k_theta / r_P over g = d - 2 r_P"
    )
    tetheredStates.forEach { turnState ->
        val tightest = cells.filter { it.turnState == turnState }.minBy { it.p90OverStroke }
        val turn = states.first { it.label == turnState }
        val grid = t299Placements(shared, rootingHelixY, tightest.columns)
            .first { it.first == tightest.placement }.second
        val stiffnesses = t299Distributions(grid, shared.edgeX, shared.edgeY)
            .first { it.first == tightest.distribution }.second
        val ensemble = dropoutEnsemble(
            grid.map { (x, y) -> incorporation.at(x, y) }, t299Realisations, T299_SEED
        )
        val label = "the tightest " + turnState + " cell: f = " +
                tightest.compositeFraction.emitted(3) + ", " + tightest.placement + ", " +
                tightest.columns + " x " + shared.rasterRows + " = " + tightest.pathCount +
                " paths, " + tightest.distribution
        linkLadder.forEach { (stiffness, ground) ->
            val tile = t299Tile(
                shared, shared.enhancementAt(tightest.compositeFraction), turn,
                withPreload = true, linkStiffness = stiffness
            )
            val surrogate = tile.surrogate(grid)
            val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
            sample.indices.forEach { sample[it] = sample[it] / tile.freeStroke }
            val summary = summariseDropoutDishing(
                sample, surrogate.solve(stiffnesses).peakDishing / tile.freeStroke,
                ensemble.meanSurvivors, T299_TOLERANCE
            )
            linkRows += T299LinkStiffness(
                cell = label,
                linkStiffness = stiffness,
                ground = ground,
                p90OverStroke = summary.p90,
                exceedance = summary.exceedance,
                exceedanceStandardError = summary.exceedanceStandardError,
                exceedanceOneSidedBound = summary.exceedanceOneSidedBound,
                flatAtP90 = summary.flatAtP90
            )
        }
    }
    linkRows.forEach {
        println("  k_link " + it.linkStiffness.emitted(9) + "  p90 " +
                it.p90OverStroke.emitted(9) + (if (it.flatAtP90) "  flat" else "  not flat"))
    }

    // ============================================ Deliverable 7 -- the width route B forces
    println("T-299 - the width: at the built allowance a uniform honeycomb row is narrow")
    val widthRows = ArrayList<T299Width>()
    listOf(
        "M13mp18" to T299_M13, "p7560" to T299_P7560, "p8064" to T299_P8064
    ).forEach { (name, scaffold) ->
        val perHelix = scaffold / T299_HELICES
        val pairedPerHelix = perHelix - T299_UNPAIRED_PER_HELIX
        // A row width is a property of the RASTER, so the row is graded on its own extent: the
        // same 10 x 6 cross-section, the same collar shape, the same foundation, and only the
        // axial span moved. It is not a re-optimised design.
        val rowLattice = HoneycombGrillage(
            block = shared.block,
            rowBasePairs = pairedPerHelix,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            hingeStiffnessEnhancement = shared.enhancementAt(0.30)
        )
        val edgeX = pairedPerHelix * Gen1Tile.RISE_PER_BASE_PAIR
        val edgeY = shared.edgeY
        val interior = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)
        val pressure = shared.profile.field(interior, edgeX, edgeY)
        val stroke = rowLattice.solve(uniformPressure(interior)).meanDeflection
        val dishing = rowLattice.solve(pressure).peakDishing(T299_SAMPLES) / stroke
        widthRows += T299Width(
            scaffold = name,
            scaffoldNucleotides = scaffold,
            loopPerTurn = T299_UNPAIRED_PER_HELIX,
            pairedPerHelix = pairedPerHelix,
            rowBasePairs = pairedPerHelix,
            rowWidth = edgeX,
            departureFromNominal = edgeX / 40.0 - 1.0,
            uncoupledDishingOverStroke = dishing,
            uncoupledFlat = dishing < T299_TOLERANCE
        )
    }
    widthRows.forEach {
        println("  " + it.scaffold + "  " + it.rowBasePairs + " bp = " + it.rowWidth.emitted(9) +
                " nm  (" + (it.departureFromNominal * 100.0).emitted(3) + " per cent of 40 nm)")
    }

    // ============================================ the verdict
    fun stateCells(state: String) = cells.filter { it.turnState == state }
    val builtCells = stateCells("tethered, 28 nt (the built allowance, p8064)")
    val m13Cells = stateCells("tethered, 15 nt (M13's affordance on the drawable raster)")
    val mildCells = stateCells("tethered, 28 nt at the ALIGNED azimuth and the softest corner")
    val orderedCells = stateCells(
        "tethered, C-0200's ORDERED split: 24 nt at one rim and 32 at the other"
    )
    val untiedCells = stateCells("untied")
    val tiedCells = stateCells("tied")
    val vsUntied = paired.filter { it.comparison == builtVsUntied }
    val vsTied = paired.filter { it.comparison.contains("route A") }
    val everyTetherVsUntied = paired.filter { it.comparison.contains("over C-0167's untied one") }
    val verdict = linkedMapOf(
        "cellsGraded" to (cells.size.toString() + " over " + states.size + " turn states, one " +
                "common dropout stream restricted per cell"),
        "untiedCellsFlatAtP90" to (untiedCells.count { it.flatAtP90 }.toString() + " of " +
                untiedCells.size + " -- C-0167's own 0 of 64, reproduced"),
        "tiedCellsFlatAtP90" to (tiedCells.count { it.flatAtP90 }.toString() + " of " +
                tiedCells.size + " -- C-0180's own 2 of 64"),
        "builtTetherCellsFlatAtP90" to (builtCells.count { it.flatAtP90 }.toString() + " of " +
                builtCells.size),
        "m13TetherCellsFlatAtP90" to (m13Cells.count { it.flatAtP90 }.toString() + " of " +
                m13Cells.size),
        "alignedAzimuthSoftestCornerCellsFlatAtP90" to
                (mildCells.count { it.flatAtP90 }.toString() + " of " + mildCells.size),
        "orderedSplitCellsFlatAtP90" to (orderedCells.count { it.flatAtP90 }.toString() + " of " +
                orderedCells.size + " -- C-0200's 24 / 32, whose mean is C-0193's 28"),
        "freeTileOverTheWholeBracket" to (bracketRows.minOf { it.freeTileWithPreload }.emitted(9) +
                " to " + bracketRows.maxOf { it.freeTileWithPreload }.emitted(9) +
                " against the untied " +
                tiles.getValue(0.30 to "untied").uncoupledDishing.emitted(9) + " at f = 0.30, " +
                bracketRows.count { it.flatWithPreload } + " of " + bracketRows.size +
                " corners flat"),
        "rimClosureAgainstTheStericSlack" to
                (bracketRows.maxOf { it.worstRimClosure }.emitted(9) + " nm at worst against " +
                        stericSlack.emitted(9) + " nm of slack before backbone contact; " +
                        bracketRows.count { it.closureExceedsStericSlack } + " of " +
                        bracketRows.size + " corners exceed it, so the LINEAR reading is an " +
                        "upper bound there"),
        "verdictsMovedAgainstTheUntiedLattice" to
                (vsUntied.count { it.verdictMoved }.toString() + " of " + vsUntied.size),
        "verdictsMovedAgainstTheTIEDLattice" to
                (vsTied.count { it.verdictMoved }.toString() + " of " + vsTied.size),
        "medianRatioRangeAgainstTheUntiedLattice" to
                (vsUntied.minOf { it.medianRatio }.emitted(9) + " to " +
                        vsUntied.maxOf { it.medianRatio }.emitted(9)),
        "cellsAtWhichTheTETHERIsADISHINGSOURCE" to
                (vsUntied.count { it.medianRatio > 1.0 }.toString() + " of " + vsUntied.size +
                        " at the built allowance, and " +
                        everyTetherVsUntied.count { it.medianRatio > 1.0 } + " of " +
                        everyTetherVsUntied.size + " over all four tethered states"),
        "medianRatioRangeOverAllFourTetheredStates" to
                (everyTetherVsUntied.minOf { it.medianRatio }.emitted(9) + " to " +
                        everyTetherVsUntied.maxOf { it.medianRatio }.emitted(9)),
        "everyCoupledCellIsWorseThanTheUncoupledTile" to
                (builtCells.count { !it.beatsUncoupledAtP90 }.toString() + " of " +
                        builtCells.size + " (C-0109, reproduced on the tethered lattice)")
    )
    verdict.forEach { (k, v) -> println("  " + k + ": " + v) }

    // ============================================ convergence
    //
    // CLAUDE.md: re-take the convergence axis on the DECIDING quantity at the DECIDING cell.
    // Here what decides is the p90 of the cells whose T-5b verdict moves -- or, where none
    // moves, the tightest cell of each tethered state, which is where a verdict would move
    // first. The untied control run is what says a departure belongs to the TETHER and not to
    // this study's code.
    println("T-299 - convergence")
    val convergence = ArrayList<T299Convergence>()
    val decidingKeys = (paired.filter { it.verdictMoved } +
            tetheredStates.map { turnState ->
                val tightest = stateCells(turnState).minBy { it.p90OverStroke }
                paired.first {
                    it.comparison.contains("untied") &&
                            it.compositeFraction == tightest.compositeFraction &&
                            it.placement == tightest.placement &&
                            it.columns == tightest.columns &&
                            it.distribution == tightest.distribution
                }
            }).distinctBy {
        listOf(it.compositeFraction, it.placement, it.columns, it.distribution).joinToString("|")
    }
    decidingKeys.forEach { deciding ->
        val grid = t299Placements(shared, rootingHelixY, deciding.columns)
            .first { it.first == deciding.placement }.second
        val stiffnesses = t299Distributions(grid, shared.edgeX, shared.edgeY)
            .first { it.first == deciding.distribution }.second
        val ensemble = dropoutEnsemble(
            grid.map { (x, y) -> incorporation.at(x, y) }, t299Realisations, T299_SEED
        )
        val label = "f = " + deciding.compositeFraction.emitted(3) + ", " + deciding.placement +
                ", " + deciding.columns + " x " + shared.rasterRows + " = " + grid.size +
                " paths, " + deciding.distribution
        fun p90At(tile: T299Tile, samples: Int): Double {
            val surrogate = tile.surrogate(grid, samples)
            val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
            sample.indices.forEach { sample[it] = sample[it] / tile.freeStroke }
            return summariseDropoutDishing(
                sample, surrogate.solve(stiffnesses).peakDishing / tile.freeStroke,
                ensemble.meanSurvivors, T299_TOLERANCE
            ).p90
        }
        listOf(
            "tethered, 28 nt (the built allowance, p8064)" to
                    T299Turn("conv", builtState, builtState),
            "untied -- the control" to T299Turn("conv untied")
        ).forEach { (turnState, turn) ->
            val coarse = t299Tile(
                shared, shared.enhancementAt(deciding.compositeFraction), turn
            )
            val fine = t299Tile(
                shared, shared.enhancementAt(deciding.compositeFraction), turn, subdivisions = 2
            )
            val a = p90At(coarse, T299_SAMPLES)
            val b = p90At(fine, T299_SAMPLES)
            convergence += T299Convergence(
                axis = "beam subdivisions",
                cell = turnState + " -- " + label,
                quantity = "the p90 of the dropout ensemble, over the stroke",
                coarse = "1", fine = "2",
                coarseValue = a, fineValue = b,
                departure = abs(b - a) / abs(a),
                verdictAtCoarse = a < T299_TOLERANCE,
                verdictAtFine = b < T299_TOLERANCE,
                verdictSurvives = (a < T299_TOLERANCE) == (b < T299_TOLERANCE)
            )
        }
        val builtTile = tiles.getValue(
            deciding.compositeFraction to "tethered, 28 nt (the built allowance, p8064)"
        )
        listOf(41 to 81, 81 to 161).forEach { (coarse, fine) ->
            val a = p90At(builtTile, coarse)
            val b = p90At(builtTile, fine)
            convergence += T299Convergence(
                axis = "the dishing sample grid",
                cell = "tethered, 28 nt -- " + label,
                quantity = "the p90 of the dropout ensemble, over the stroke",
                coarse = coarse.toString(), fine = fine.toString(),
                coarseValue = a, fineValue = b,
                departure = if (a == 0.0) abs(b) else abs(b - a) / abs(a),
                verdictAtCoarse = a < T299_TOLERANCE,
                verdictAtFine = b < T299_TOLERANCE,
                verdictSurvives = (a < T299_TOLERANCE) == (b < T299_TOLERANCE)
            )
        }
    }
    convergence.forEach {
        println("  " + it.axis + "  " + it.coarse + " -> " + it.fine + "  departure " +
                it.departure.emitted(2) + "  " + it.cell +
                (if (it.verdictSurvives == false) "  VERDICT MOVES" else ""))
    }

    // ============================================ reproductions
    println("T-299 - reproductions")
    val reproductions = ArrayList<T299Reproduction>()
    fun reproduce(source: String, quantity: String, published: Double, here: Double) {
        val dep = if (published == 0.0) abs(here) else abs(here - published) / abs(published)
        reproductions += T299Reproduction(source, quantity, published, here, dep, dep < 1e-8)
    }
    reproduce(
        "C-0167 (T-263)", "untied free tile, 10 x 6, f = 0.30",
        0.0501417316, tiles.getValue(0.30 to "untied").uncoupledDishing
    )
    reproduce(
        "C-0167 (T-263)", "untied free tile, 10 x 6, f = 0.26",
        0.0522223659, tiles.getValue(0.26 to "untied").uncoupledDishing
    )
    reproduce(
        "C-0175 (T-254)", "tied free tile, 10 x 6, f = 0.30",
        0.0446459684, tiles.getValue(0.30 to "tied").uncoupledDishing
    )
    reproduce(
        "C-0175 (T-254)", "tied free tile, 10 x 6, f = 0.26",
        0.0467367262, tiles.getValue(0.26 to "tied").uncoupledDishing
    )
    reproduce(
        "C-0193 (T-296)", "the 15 nt turn's tension at the worst azimuth, b = 2.10, c = 0.65",
        3.03288672, freelyJointedTetherState(worstSpan, 15, 2.10, 0.65, kBT).tension
    )
    reproduce(
        "C-0193 (T-296)", "the 15 nt turn's tension at the worst azimuth, b = 2.84, c = 0.70",
        2.03800431, freelyJointedTetherState(worstSpan, 15, 2.84, 0.70, kBT).tension
    )
    reproduce(
        "C-0193 (T-296)", "the 28 nt turn's tension at the worst azimuth, b = 2.10, c = 0.65",
        1.46667915, freelyJointedTetherState(worstSpan, 28, 2.10, 0.65, kBT).tension
    )
    reproduce("C-0147 (T-230)", "the worst-azimuth span, d + 2 r_P", 4.35327572, worstSpan)
    reproduce("C-0147 (T-230)", "the aligned span, d - 2 r_P", 0.718724283, alignedSpan)
    reproduce("C-0193 (T-296)", "M13mp18's uniform row at the built allowance, in nm", 31.28,
        widthRows.first { it.scaffold == "M13mp18" }.rowWidth)
    reproduce("C-0193 (T-296)", "p8064's uniform row at the built allowance, in nm", 36.04,
        widthRows.first { it.scaffold == "p8064" }.rowWidth)
    // C-0167's own 64 committed cells, every one of them
    val t263 = ResultInputs.T_263.file()
    var worstCellDeparture = 0.0
    var cellsClosing = 0
    untiedCells.forEach { cell ->
        listOf("p90OverStroke" to cell.p90OverStroke, "nominalOverStroke" to cell.nominalOverStroke)
            .forEach { (key, here) ->
                val record = Json.parseToJsonElement(t263.readText())
                    .jsonObject.getValue("cells").jsonArray.map { it.jsonObject }
                    .first {
                        it.getValue("model").jsonPrimitive.content == "honeycomb grillage" &&
                                it.getValue("compositeFraction").jsonPrimitive.content
                                    .toDoubleOrNull() == cell.compositeFraction &&
                                it.getValue("placement").jsonPrimitive.content == cell.placement &&
                                it.getValue("columns").jsonPrimitive.content.toInt() ==
                                cell.columns &&
                                it.getValue("distribution").jsonPrimitive.content ==
                                cell.distribution
                    }
                val published = record.getValue(key).jsonPrimitive.content.toDouble()
                val dep = if (published == 0.0) abs(here) else abs(here - published) / abs(published)
                if (dep < 1e-8) cellsClosing++
                worstCellDeparture = maxOf(worstCellDeparture, dep)
            }
    }
    val worstCellDepartureEmitted = roundForResult(worstCellDeparture, 2, 0.0)
    reproductions += T299Reproduction(
        source = "C-0167 (T-263)",
        quantity = "all " + untiedCells.size + " committed cells, p90 and nominal -- " +
                cellsClosing + " of " + (2 * untiedCells.size) + " values close at 1e-8; both " +
                "columns of this row are a DEPARTURE and are emitted at two significant digits",
        published = 0.0,
        here = worstCellDepartureEmitted,
        departure = worstCellDepartureEmitted,
        closes = cellsClosing == 2 * untiedCells.size
    )
    reproductions.forEach {
        println("  " + it.source + "  " + it.quantity + "  departure " + it.departure.emitted(2) +
                (if (it.closes) "  closes" else "  DOES NOT CLOSE"))
    }

    // ============================================ falsifiers
    val c0167Object = HoneycombGrillage(
        block = shared.block,
        rowBasePairs = shared.rowBasePairs,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        hingeStiffnessEnhancement = shared.enhancementAt(0.30)
    )
    val emptyTether = HoneycombGrillage(
        block = shared.block,
        rowBasePairs = shared.rowBasePairs,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        hingeStiffnessEnhancement = shared.enhancementAt(0.30),
        scaffoldTurnTethers = emptyList()
    )
    val loadA = c0167Object.assembleLoad(shared.pressureField)
    val loadB = emptyTether.assembleLoad(shared.pressureField)
    var loadIdentical = true
    for (i in 0 until c0167Object.degreesOfFreedom) {
        if (loadA[i] != loadB[i]) loadIdentical = false
    }
    val freeTethered = t299Tile(
        shared, shared.enhancementAt(0.30), T299Turn("falsifier", builtState, builtState),
        withPreload = false
    )
    val uniformField = freeTethered.lattice.solve(uniformPressure(shared.interiorPressure))
    val uniformDishing = uniformField.peakDishing(T299_SAMPLES) / uniformField.meanDeflection
    // The rigid-roll probe: the preload must do zero work on a rigid rotation of the block.
    //
    // Taken PER ELEMENT and not on the sum. The honeycomb's two through-thickness azimuths carry
    // opposite `unitY`, so a per-element sign defect in the preload's roll arm cancels EXACTLY in
    // the whole-lattice work — which is how a mutation of exactly that sign survived the first
    // run of this study's own test file. A conservation test taken on a sum cannot see a
    // per-element defect the lattice's own symmetry annihilates.
    val preloadTile = tiles.getValue(0.30 to builtLabel)
    val rigidRollWork = run {
        val alpha = 1e-3
        fun workOf(lattice: HoneycombGrillage): Double {
            val load = lattice.tetherPreloadLoad()
            var work = 0.0
            for (node in 0 until lattice.nodesPerBeam) {
                for (beam in 0 until lattice.beamCount) {
                    val base = (node * lattice.beamCount + beam) * HoneycombGrillage.DOF_PER_NODE
                    work += load[base + HoneycombGrillage.W] * alpha * lattice.beamY[beam]
                    work += load[base + HoneycombGrillage.PHI] * alpha
                }
            }
            return abs(work)
        }
        val whole = workOf(preloadTile.lattice)
        val perElement = preloadTile.lattice.scaffoldTurnTethers.maxOf { one ->
            workOf(
                HoneycombGrillage(
                    block = shared.block,
                    rowBasePairs = shared.rowBasePairs,
                    foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
                    hingeStiffnessEnhancement = shared.enhancementAt(0.30),
                    scaffoldTurnTethers = listOf(one)
                )
            )
        }
        maxOf(whole, perElement)
    }
    val builtVsUntiedWorst = vsUntied.maxOf { abs(it.medianRatio - 1.0) }
    val preloadWorst = preloadRows.maxOf { abs(it.movement) }
    val decidingConvergence = convergence.filter { it.verdictSurvives != null }
    val falsifiers = listOf(
        T299Falsifier(
            "F1", "an empty tether list is bit-identical to C-0167's object on assembleLoad",
            !loadIdentical,
            "the load vector is bit-identical over all " + c0167Object.degreesOfFreedom +
                    " degrees of freedom; the bond census, the point-load dual and the solved " +
                    "field are asserted in HoneycombRasterTurnTethersTest"
        ),
        T299Falsifier(
            "F2", "a uniform pressure on the FREE tethered lattice dishes exactly zero",
            uniformDishing > T299_IDENTITY,
            "peak dishing over stroke " + uniformDishing.emitted(2) + " against " +
                    T299_IDENTITY.emitted(2) + ", with 59 tethers present"
        ),
        T299Falsifier(
            "F3", "the tether PRELOAD is annihilated by a rigid roll, PER ELEMENT and not " +
                    "only in the sum",
            rigidRollWork > 1e-12,
            "the worst work on a 1 mrad rigid roll, over the whole preload vector AND over each " +
                    "of the 59 tethers taken alone, is " + rigidRollWork.emitted(2) + " pN nm. " +
                    "The per-element reading is not a refinement: the honeycomb's two " +
                    "through-thickness azimuths carry opposite unitY, so a per-element sign " +
                    "defect cancels EXACTLY in the sum, and one duly survived this study's own " +
                    "first mutation run"
        ),
        T299Falsifier(
            "F4", "the tether moves NO flatness verdict against the untied lattice -- " +
                    "declared open",
            vsUntied.count { it.verdictMoved } > 0,
            vsUntied.count { it.verdictMoved }.toString() + " of " + vsUntied.size +
                    " paired cells change their T-5b verdict; the largest departure of a " +
                    "median per-realisation ratio from one is " + builtVsUntiedWorst.emitted(9)
        ),
        T299Falsifier(
            "F5", "the tether PRELOAD moves a free-tile or a coupled verdict -- declared open",
            preloadRows.any { it.flatWithPreload != it.flatWithoutPreload },
            "the largest movement the preload makes anywhere is " + preloadWorst.emitted(9) +
                    " of the stroke, against route A's own prestrain ceiling of 0.0764244991; " +
                    "over the whole 36-corner bracket the free tile moves " +
                    bracketRows.minOf { it.movement }.emitted(9) + " to " +
                    bracketRows.maxOf { it.movement }.emitted(9) + " of the stroke"
        ),
        T299Falsifier(
            "F6", "the stiff limit of the element is a normal link -- its residual falls with " +
                    "1/k",
            false,
            "asserted as a named test in HoneycombRasterTurnTethersTest rather than measured " +
                    "here: the worst residual falls by more than five per decade over two decades"
        ),
        T299Falsifier(
            "F7", "route B's own uniform row width admits a flat free tile -- declared open",
            widthRows.any { !it.uncoupledFlat },
            widthRows.count { it.uncoupledFlat }.toString() + " of " + widthRows.size +
                    " scaffolds give a flat uncoupled row at f = 0.30; the widths are " +
                    widthRows.joinToString(", ") {
                        it.scaffold + " " + it.rowWidth.emitted(9) + " nm"
                    }
        ),
        T299Falsifier(
            "F9", "the preload's predicted rim closure stays inside the steric slack, so the " +
                    "LINEAR element is a representation of the structure -- declared open",
            bracketRows.any { it.closureExceedsStericSlack },
            bracketRows.count { it.closureExceedsStericSlack }.toString() + " of " +
                    bracketRows.size + " corners close the rim pair by more than " +
                    stericSlack.emitted(9) + " nm, which is d - 2 r_P on T-71's MEASURED " +
                    "phosphate radius; the worst closure is " +
                    bracketRows.maxOf { it.worstRimClosure }.emitted(9) + " nm. This lattice " +
                    "carries no steric floor between two duplexes, so wherever it fires the " +
                    "preload's dishing is an UPPER bound and the honest statement is a threshold"
        ),
        T299Falsifier(
            "F8", "a deciding cell keeps its verdict under its own convergence axes, taken on " +
                    "the p90 itself -- declared open",
            decidingConvergence.any { it.verdictSurvives == false },
            decidingConvergence.count { it.verdictSurvives == false }.toString() + " of " +
                    decidingConvergence.size + " deciding-cell convergence steps move the " +
                    "verdict; the largest departure on a deciding p90 is " +
                    decidingConvergence.maxOf { it.departure }.emitted(3)
        )
    )
    falsifiers.forEach {
        println("  " + it.name + (if (it.fired) "  FIRED  " else "  did not fire  ") + it.note)
    }

    // ============================================ emission
    val result = T299Result(
        task = "T-299",
        leaf = "A8.2",
        title = "route B graded: the raster turn as an entropic ssDNA tether, on the same 64 " +
                "coupled cells",
        verificationType = "in-silico (the same beam-and-bond lattice, the same exact Woodbury " +
                "coupling surrogate and the same measured-incorporation dropout ensemble, with " +
                "59 freely-jointed tethers in place of 59 covalent ties) + logical (an exact " +
                "bit-identity between the empty-tether lattice and the object C-0167 measured, " +
                "a closed-form freely-jointed-chain law, an exact geometric decomposition of " +
                "the element onto the model's own coordinates, and exact integer scaffold " +
                "arithmetic) + literature (C-0193's reading of the built precedent's own " +
                "accounting, and T-230's ssDNA Kuhn and contour brackets)",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated. " +
                "THE FORK IS SETTLED IN THIS ARM'S FAVOUR: CH-0251 offered the seven " +
                "deposited caDNAno files against C-0193 and was REFUTED on its central point " +
                "by C-0200 (T-302, this iteration) -- the Nature paper's own staple order buys " +
                "5 880 nucleotides for the 10 x 6 block, 60 x 98 exactly, and the 70 strands " +
                "the file draws and the order omits total 1 680 = 60 x 28 and lie in the helix " +
                "ends. So route B is the design that was BOUGHT and route A is the one nobody " +
                "has folded, and the split is 12 / 16 rather than 14 / 14, which is graded here.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm",
            "rotationalStiffness" to "pN nm/rad",
            "pressure" to "pN/nm^2 = 1 MPa",
            "angle" to "rad internally, degrees in prose",
            "dishing" to "dimensionless, as a fraction of the free stroke"
        ),
        conventions = mapOf(
            "s" to "along the helices, origin at the face centre",
            "y" to "across the helices in the plane of the face, origin at the face centre",
            "z" to "along the block's thickness",
            "W" to "positive DOWNWARD, toward the electrode (C-0006)",
            "turn, route A" to "a raster turn carrying ZERO unpaired nucleotides IS a scaffold " +
                    "crossover -- a covalent tie with a dihedral spring, a normal link and an " +
                    "axial slip spring, at s = +-L/2",
            "turn, route B" to "a raster turn carrying 24 to 32 unpaired nucleotides is a " +
                    "freely-jointed ssDNA chain between the two DUPLEX ends, both at the same " +
                    "rim. The covalent link sits somewhere ALONG that chain and the outboard " +
                    "distance is NOT a parameter of the element: a rise applied to " +
                    "single-stranded scaffold is the wrong kind of number, and what the " +
                    "unpaired region has is a CONTOUR of 0.65-0.70 nm/nt (the standoff record)",
            "tetherElement" to "a preloaded central-force element: (df/dx) along the chain and " +
                    "(f/x) transverse to it, resolved onto the lattice's link and slip " +
                    "gradients with the frame-indifferent d/2 arm; NO dihedral spring",
            "tetherPreload" to "a LOAD in C-0104's sense -- it changes no entry of the " +
                    "stiffness matrix, the field is exactly linear in it, and every influence " +
                    "function is taken on withoutPrestrain",
            "span" to "the distance between the chain's two anchoring phosphates, an azimuth " +
                    "bracket from d - 2 r_P to d + 2 r_P; the headline is the WORST azimuth, " +
                    "which is the STIFFEST and therefore the adverse end"
        ),
        parameters = mapOf(
            "crossSection" to shared.crossSection,
            "rowBasePairs" to shared.rowBasePairs.toString(),
            "edgeX" to shared.edgeX.emitted(9),
            "edgeY" to shared.edgeY.emitted(9),
            "interhelicalDistance" to shared.d.emitted(9),
            "phosphateRadius" to rP.emitted(9),
            "alignedSpan" to alignedSpan.emitted(9),
            "worstAzimuthSpan" to worstSpan.emitted(9),
            "interiorPressure" to shared.interiorPressure.emitted(9),
            "closedFormStroke" to shared.closedFormStroke.emitted(9),
            "hingeStiffness" to Gen1Tile.crossoverHingeStiffness().emitted(9),
            "hingeStiffnessOnTheRimArm" to hingeOnRimArm.emitted(9),
            "slipStiffness" to Gen1Tile.crossoverInPlaneStiffness().emitted(9),
            "spanLawLinkStiffness" to spanLawLink.emitted(9),
            "rigidLinkPenalty" to HoneycombGrillage.RIGID_LINK_STIFFNESS.emitted(9),
            "foundationStiffness" to Gen1Tile.FOUNDATION_SECANT.emitted(9),
            "thermalEnergy" to kBT.emitted(9),
            "electrolyte" to "MgCl2 (2:1)",
            "bufferMillimolar" to T299_BUFFER_MILLIMOLAR.emitted(3),
            "gapHeightNm" to T299_GAP_NM.emitted(3),
            "appliedBiasVolts" to T299_BIAS_VOLTS.emitted(3),
            "temperatureKelvin" to "300",
            "whyTheRegimeBlockIsNull" to "environment.Regime holds the gap and the bias as " +
                    "INTERVALS and refuses a degenerate one; this study solves no " +
                    "electrostatics, it reads ONE profile record of T-3b and uses it as a fixed " +
                    "load shape, so the state is a POINT and belongs here rather than in a " +
                    "range (C-0181, CH-0224)",
            "kuhnBracket" to "2.10 to 2.84 nm, zero force",
            "contourBracket" to "0.65 to 0.70 nm/nt, inextensible",
            "headlineCorner" to "the worst azimuth at b = 2.10 nm and c = 0.65 nm/nt, which is " +
                    "the STIFFEST corner and therefore adverse to the finding",
            "builtLoop" to T299_BUILT_LOOP.toString(),
            "m13Loop" to T299_M13_LOOP.toString(),
            "compositeFractions" to "0.30 and 0.26 (C-0116), plus the lattice's own 1.0",
            "mandate" to "C-0017's 33.3333 pN/nm on the SUM, section 3's acceptable clause",
            "realisations" to t299Realisations.toString(),
            "seed" to T299_SEED.toString(),
            "samples" to T299_SAMPLES.toString(),
            "tolerance" to T299_TOLERANCE.emitted(2),
            "raster" to (T299_RECOMMENDED_ONE.toString() + " / " + T299_RECOMMENDED_TWO +
                    " (C-0151, drawable)"),
            "ladderPhase" to T299_LADDER_PHASE.toString(),
            "ladderOffset" to T299_LADDER_OFFSET.toString(),
            "firstAxialSign" to "+1",
            "linkStiffnessOfEveryHeadlineCell" to
                    HoneycombGrillage.RIGID_LINK_STIFFNESS.emitted(9)
        ),
        sources = listOf(
            ResultInputs.T_3B.path + " (C-0022's solved collar at 2 mM / 10 nm / 0.192 V)",
            ResultInputs.T_263.path + " (C-0167's 64 committed cells, reproduced)"
        ),
        citedInputs = mapOf(
            "C-0167 untied free tile, f = 0.30" to "0.0501417316",
            "C-0167 untied free tile, f = 0.26" to "0.0522223659",
            "C-0175 tied free tile, f = 0.30" to "0.0446459684",
            "C-0175 tied free tile, f = 0.26" to "0.0467367262",
            "C-0180 tied coupled recovery" to "2 of 64",
            "C-0193 the 15 nt turn's tension band" to "2.03800431 to 3.03288672 pN",
            "C-0193 the built allotment" to "126 = 98 + 28 per helix, 60 x 126 = 7560",
            "C-0200 the ORDERED split" to "12 / 16 per helix, so 24 nt at one rim and 32 at " +
                    "the other; the order is 5880 = 60 x 98 in 144 strands",
            "CH-0228 route A's prestrain ceiling on the free tile" to "0.0764244991",
            "C-0194 the span law's link stiffness" to "41.4338953 pN/nm"
        ),
        cheapBound = cheapBound,
        tethers = tetherRows,
        standoff = standoffRows,
        bracket = bracketRows,
        geometries = geometries,
        cells = cells,
        paired = paired,
        preload = preloadRows,
        linkStiffness = linkRows,
        width = widthRows,
        verdict = verdict,
        convergence = convergence,
        reproductions = reproductions,
        falsifiers = falsifiers,
        findings = listOf(
            "The cheap bound was right about the STIFFNESS and it is not what decides. Over " +
                    "the whole Kuhn and contour bracket and all three azimuths a raster turn " +
                    "made of 15 to 28 nucleotides of ssDNA has a tangent stiffness of " +
                    softestTangent.emitted(9) + " to " + stiffestTangent.emitted(9) +
                    " pN/nm, at most " + worstOverHinge.emitted(9) + " of k_theta on the rim " +
                    "node's own d/2 arm and " +
                    tetherRows.maxOf { it.overRigidLinkPenalty }.emitted(2) +
                    " of the lattice's link penalty -- arithmetically no element at all.",
            "What decides is the PRELOAD, which no cheap bound prices: a chain held at any " +
                    "x > 0 pulls, so route B's turn applies a self-equilibrated " +
                    bracketRows.minOf { it.tension }.emitted(9) + " to " +
                    bracketRows.maxOf { it.tension }.emitted(9) + " pN between its two rim " +
                    "nodes, and that load moves the FREE tile by " +
                    bracketRows.minOf { it.movement }.emitted(9) + " to " +
                    bracketRows.maxOf { it.movement }.emitted(9) + " of the stroke over the 36 " +
                    "corners of the bracket -- against route A's own 0.0764244991 " +
                    "triangle-inequality prestrain ceiling. It is a DISHING SOURCE and route " +
                    "A's tie is a dishing sink: the two routes differ in the SIGN of what the " +
                    "turn does, not only in its size.",
            "Route B's 64 coupled cells read " + builtCells.count { it.flatAtP90 } + " of " +
                    builtCells.size + " flat at the 90th percentile at the built 28 nt " +
                    "allowance, " + m13Cells.count { it.flatAtP90 } + " of " + m13Cells.size +
                    " at M13's 15 nt affordance and " + mildCells.count { it.flatAtP90 } +
                    " of " + mildCells.size + " at the aligned azimuth and the softest " +
                    "corner, and " + orderedCells.count { it.flatAtP90 } + " of " +
                    orderedCells.size + " at C-0200's ordered 24 / 32 split, against " +
                    "C-0167's untied " +
                    untiedCells.count { it.flatAtP90 } + " of " + untiedCells.size +
                    " and C-0180's tied " + tiedCells.count { it.flatAtP90 } + " of " +
                    tiedCells.size + ". C-0180's coupled recovery belongs to route A alone.",
            "The tether's own free tile is " +
                    tiles.getValue(0.30 to "tethered, 28 nt (the built allowance, p8064)")
                        .uncoupledDishing.emitted(9) + " at f = 0.30 against the untied " +
                    tiles.getValue(0.30 to "untied").uncoupledDishing.emitted(9) + " and the " +
                    "tied " + tiles.getValue(0.30 to "tied").uncoupledDishing.emitted(9) +
                    " -- so C-0175's 1.12x belongs to route A alone, and so does its sign.",
            "And the preload's own prediction is not everywhere admissible: at " +
                    bracketRows.count { it.closureExceedsStericSlack } + " of " +
                    bracketRows.size + " corners it closes the rim pair by more than the " +
                    stericSlack.emitted(9) + " nm of slack T-71's MEASURED phosphate radius " +
                    "leaves before backbone contact, worst " +
                    bracketRows.maxOf { it.worstRimClosure }.emitted(9) + " nm. This lattice " +
                    "carries no steric floor between two duplexes, so there the dishing is an " +
                    "UPPER bound and what is quotable is a threshold, not a value.",
            "The standoff nobody needed: '14 bp = 4.76 nm outboard' is a DUPLEX rise applied " +
                    "to single-stranded scaffold. Re-derived, C-0200's 12 and 16 nt half-loops " +
                    "have contours of " +
                    standoffRows.minOf { it.contourLength }.emitted(9) + " to " +
                    standoffRows.maxOf { it.contourLength }.emitted(9) + " nm and " +
                    "root-mean-square end-to-end distances of " +
                    standoffRows.minOf { it.rootMeanSquareEndToEnd }.emitted(9) + " to " +
                    standoffRows.maxOf { it.rootMeanSquareEndToEnd }.emitted(9) + " nm -- and " +
                    "the standoff enters the element nowhere, because a freely-jointed chain " +
                    "joins the two DUPLEX ends and integrates over where its own covalent link " +
                    "sits. What fixes the element is the NUCLEOTIDE COUNT and the " +
                    "cross-section span.",
            "C-0200 also records that 60 of the block's 118 scaffold crossings sit in unpaired " +
                    "scaffold in the object that was ordered, so caDNAno's +-5 bp residue " +
                    "condition cannot bind them -- which is C-0193 section 4's mechanism, " +
                    "confirmed on the purchase rather than on the drawing.",
            "The width route B forces is the larger finding: at the built allowance a uniform " +
                    "honeycomb row is " + widthRows.joinToString("; ") {
                        it.scaffold + " " + it.rowBasePairs + " bp = " + it.rowWidth.emitted(9) +
                                " nm (" + (it.departureFromNominal * 100.0).emitted(3) +
                                " per cent of section 3's 40 nm)"
                    } + "."
        ),
        validity = listOf(
            "TRL 1-3, model-consistent and traceable, not empirically demonstrated.",
            "CH-0251 is REFUTED by C-0200 (T-302, this iteration): route B is the design the " +
                    "2009 staple order buys, and route A remains undemonstrated. This study " +
                    "grades the ordered design; C-0175, C-0180 and C-0190 grade the other one.",
            "The tether is LINEARISED about the built, TAUT state at a stated span, loop length " +
                    "and (b, c) corner. It is one-sided -- a chain pulls and does not push -- " +
                    "and the compressive branch is not reached: it would need the two rim nodes " +
                    "to close by the whole span, 2.5 to 4.4 nm, against solved deflections four " +
                    "orders smaller.",
            "The model has NO in-plane transverse coordinate, so the chain's pull along y is " +
                    "carried by nothing: the nine in-plane turns contribute exactly zero " +
                    "preload and only their transverse secant enters. That is a statement " +
                    "about the lattice and not about the chain.",
            "The tether's anchor is taken at the beam AXIS with the frame-indifferent d/2 arm, " +
                    "which is C-0194's theorem for a covalent link rather than a measurement of " +
                    "where a chain attaches. A phosphate-radius arm would add a roll coupling " +
                    "of order (f/x) r_P^2, which is under three per cent of k_theta.",
            "k_theta is Gen1Tile's SQUARE-lattice-fitted constant and k_s is a construction; " +
                    "no honeycomb measurement of either exists in this repository.",
            "The lattice carries NO across-helix parallel-axis term, so its D_perp is the " +
                    "INDEPENDENT one and a lower bound; the composite fraction enters as a " +
                    "smeared multiplier on k_theta (C-0167 section 8, unchanged).",
            "THE LATTICE CARRIES NO STERIC FLOOR BETWEEN TWO DUPLEXES. The preload's " +
                    "predicted rim closure exceeds d - 2 r_P = " + stericSlack.emitted(9) +
                    " nm at " + bracketRows.count { it.closureExceedsStericSlack } + " of " +
                    bracketRows.size + " corners, and CLAUDE.md records that the measured " +
                    "DNA-DNA hydration force moves 0.24 nm per e-fold at the tight end -- so " +
                    "wherever the closure is past the slack the real structure resists and the " +
                    "linear dishing is an upper bound.",
            "Every coupled verdict here is read at k_link = " +
                    HoneycombGrillage.RIGID_LINK_STIFFNESS.emitted(9) + " pN/nm unless the " +
                    "linkStiffness record says otherwise, because C-0194's F10 fired on " +
                    "exactly that axis.",
            "The dropout statistics are measured on a single-layer Rothemund rectangle and only " +
                    "the PROFILE transfers, in nm; the ensemble perturbs the COUPLING and never " +
                    "the block's own crossovers or its turns.",
            "The width rows grade a UNIFORM row at each scaffold's own affordance on the " +
                    "SAME 10 x 6 cross-section and the same collar shape. They are not a " +
                    "re-optimised design and no placement search is re-run at those widths.",
            "Nothing here re-opens the raster, the cross-section, the placement search or the " +
                    "distribution rule."
        ),
        openQuestions = listOf(
            "Which rim takes C-0200's 24 nt half and which takes the 32 nt one is a free " +
                    "convention of that reading; the two are exchanged by swapping the two " +
                    "arguments of honeycombScaffoldTurnTethers and it is not swept here.",
            "Whether route B's own narrow tile can be graded at section 3's footprint at all -- " +
                    "and whether DECISIONS-FOR-NDI.md's width decision, which is asked about " +
                    "route A's tile, changes shape if route B is the design.",
            "What a phosphate-radius attachment arm is worth. It adds a roll coupling this " +
                    "study drops on frame-indifference grounds, and the drop is priced but not " +
                    "measured.",
            "Whether the tether should carry a SECOND element at all: the 14 unpaired bases on " +
                    "each helix also remove 14 bp of DUPLEX from each beam's end, which this " +
                    "lattice models as full-length beams.",
            "Whether the recommended 10 x 6 block needs an attachment coupling at all -- every " +
                    "coupled cell graded here is worse than the uncoupled tile."
        ),
        proseFailure = "none"
    )

    val output = File("gpd/results/T-299-tethered-raster-turn-regrade.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digits = 9, floor = 1e-12
            ).withEmissionHeader(LatticeTag.HONEYCOMB, null) as JsonObject)
        ) + "\n"
    )
    println("T-299 - wrote " + output.path)
}
