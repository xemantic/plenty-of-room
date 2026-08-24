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
import com.xemantic.nano.plentyofroom.structure.maximumUniformRowLength
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
// T-304 -- the tether's SPAN is a lattice arithmetic, not a bracket.
//
// C-0201 grades route B's raster turn as a freely-jointed ssDNA chain and quotes every number at
// a SPAN, carried as an azimuth bracket `d - 2 r_P` to `d + 2 r_P` because C-0147 and C-0193 were
// bounding REACH -- where a bracket is the right instrument. For a mechanical ELEMENT it is not,
// and there it straddles T-5b: 0.0569815008 of the stroke at one end against 0.166312182 at the
// other, 24 of 36 corners flat.
//
// The chain leaves helix a at the phosphate of its LAST PAIRED BASE and enters helix b at its
// own, and a phosphate's azimuth is fixed by its base-pair index and the lattice phase --
// C-0148's residue, C-0187's sign. On the recommended 102 / 109 raster every turn is an ALLOWED
// scaffold crossover, so the azimuth is C-0152's own 8.57142857 deg and the span is one number.
//
// The coupled machinery below is T-299's, duplicated rather than imported because its helpers are
// private to that study; the duplication is VERIFIED rather than asserted -- the untied state is
// re-graded here and reproduced against C-0167's 128 committed values at 1e-8.
// ---------------------------------------------------------------------------------------------

private const val T304_SAMPLES: Int = 81
private const val T304_TOLERANCE: Double = 0.10
private const val T304_RIM_STANDOFF: Double = 1.0
private const val T304_RIM_BAND: Double = 6.7
private const val T304_SEED: Long = 197_197L
private const val T304_BLOCK_EXTENT_BP: Int = 116
private const val T304_LADDER_PHASE: Int = 16
private const val T304_LADDER_OFFSET: Int = 14
private const val T304_RECOMMENDED_ONE: Int = 102
private const val T304_RECOMMENDED_TWO: Int = 109
private const val T304_BUFFER_MILLIMOLAR: Double = 2.0
private const val T304_GAP_NM: Double = 10.0
private const val T304_BIAS_VOLTS: Double = 0.192
private const val T304_HELICES: Int = 60
private const val T304_UNPAIRED_PER_HELIX: Int = 28
private const val T304_M13: Int = 7249
private const val T304_P7560: Int = 7560
private const val T304_P8064: Int = 8064
private const val T304_BUILT_LOOP: Int = 28
private const val T304_M13_LOOP: Int = 15
private const val T304_MIDDLE_LOOP: Int = 20
private const val T304_ORDERED_LOW_RIM: Int = 24
private const val T304_ORDERED_HIGH_RIM: Int = 32
private const val T304_PERIOD: Int = 21

private val T304_KUHN: List<Double> = listOf(2.10, 2.84)
private val T304_CONTOUR: List<Double> = listOf(0.65, 0.70)

private fun Double.t304Emitted(digits: Int = 9): String = roundedForProse(digits).toString()

// ------------------------------------------------------------------------------ records

@Serializable
private data class T304Anchor(
    val index: Int,
    val turnLevelBasePairs: Int,
    val anchorLevelBasePairs: Int,
    val reducedResidue: Int,
    val anchorResidue: Int,
    val atHighEnd: Boolean,
    val exitAzimuthDegrees: Double,
    val entryAzimuthDegrees: Double,
    val span: Double
)

@Serializable
private data class T304Datum(
    val firstAxialSign: Int,
    val mirrored: Boolean,
    val axialReversed: Boolean,
    val classZeroResidue: Int,
    val distinctAzimuthMagnitudes: List<Double>,
    val distinctSpans: List<Double>,
    val singleValuedSpan: Double?,
    val agreesWithTheStandardReading: Boolean
)

@Serializable
private data class T304Offset(
    val anchorOffsetBasePairs: Int,
    val reading: String,
    val azimuthMagnitudeDegrees: Double,
    val span: Double,
    val spanOverAligned: Double,
    val singleValued: Boolean
)

@Serializable
private data class T304Corner(
    val turnState: String,
    val unpairedNucleotides: Int,
    val azimuth: String,
    val span: Double,
    val kuhnLength: Double,
    val contourPerNucleotide: Double,
    val contourLength: Double,
    val extensionRatio: Double,
    val reachable: Boolean,
    val tension: Double,
    val secantStiffness: Double,
    val tangentStiffness: Double,
    val compositeFraction: Double,
    val freeTileWithPreload: Double,
    val freeTileWithoutPreload: Double,
    val movement: Double,
    val worstRimClosure: Double,
    val stericSlack: Double,
    val closureExceedsStericSlack: Boolean,
    val flatWithPreload: Boolean
)

@Serializable
private data class T304UniformRaster(
    val scaffold: String,
    val scaffoldNucleotides: Int,
    val pairedRowBasePairs: Int,
    val rowWidth: Double,
    val closes: Boolean,
    val classZeroResidue: Int,
    val distinctSpanCount: Int,
    val minimumSpan: Double,
    val maximumSpan: Double,
    val meanSpan: Double,
    val turnsInsideTheAlignedHalf: Int,
    val allInsideTheAlignedHalf: Boolean
)

@Serializable
private data class T304Cell(
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
    val exceedanceOneSidedBound: Double?,
    val meanSurvivors: Double,
    val uncoupledDishingOverStroke: Double,
    val flatAtNominal: Boolean,
    val flatAtP90: Boolean,
    val beatsUncoupledAtP90: Boolean
)

@Serializable
private data class T304Paired(
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
    val verdictMoved: Boolean
)

@Serializable
private data class T304Geometry(
    val turnState: String,
    val compositeFraction: Double,
    val hingeStiffnessEnhancement: Double,
    val turnTethers: Int,
    val degreesOfFreedom: Int,
    val freeStroke: Double,
    val uncoupledDishingOverStroke: Double,
    val uncoupledFlat: Boolean
)

@Serializable
private data class T304CheapBoundRow(
    val question: String,
    val answer: String,
    val consequence: String
)

@Serializable
private data class T304Reproduction(
    val source: String,
    val quantity: String,
    val published: Double,
    val here: Double,
    val departure: Double,
    val closes: Boolean
)

@Serializable
private data class T304Convergence(
    val cell: String,
    val quantity: String,
    val axis: String,
    val level: String,
    val value: Double,
    val departure: Double,
    val verdictAtThisLevel: Boolean
)

@Serializable
private data class T304Falsifier(
    val name: String,
    val statement: String,
    val fired: Boolean,
    val note: String
)

@Serializable
private data class T304Result(
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
    val cheapBound: List<T304CheapBoundRow>,
    val anchors: List<T304Anchor>,
    val datumReadings: List<T304Datum>,
    val offsets: List<T304Offset>,
    val corners: List<T304Corner>,
    val uniformRasters: List<T304UniformRaster>,
    val geometries: List<T304Geometry>,
    val cells: List<T304Cell>,
    val paired: List<T304Paired>,
    val reproductions: List<T304Reproduction>,
    val convergence: List<T304Convergence>,
    val falsifiers: List<T304Falsifier>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val proseFailure: String
)

// ------------------------------------------------------------------------------ the geometry

private class T304Profile(
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, edgeX: Double, edgeY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, edgeY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T304_RIM_STANDOFF))
        )
}

private fun t304Profile(file: File): T304Profile {
    require(file.exists()) { "C-0022's result file is missing: " + file.path }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray
        .map { it.jsonObject }
        .firstOrNull { record ->
            fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == T304_BUFFER_MILLIMOLAR &&
                    value("gapHeight") == T304_GAP_NM &&
                    value("appliedBias") == T304_BIAS_VOLTS
        } ?: error("no C-0022 profile at the design state")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T304Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

/** `C-0167`'s geometry, unchanged — so the only thing that differs is the turn element. */
private class T304Shared(val profile: T304Profile) {
    val rasterRows: Int = 10
    val helicesPerRow: Int = 6
    val rowBasePairs: Int = T304_BLOCK_EXTENT_BP
    val block: HoneycombBlock = HoneycombBlock(rasterRows, helicesPerRow)
    val d: Double = Gen1Tile.INTERHELICAL_HONEYCOMB
    val rowPitch: Double = HoneycombCrossSectionGeometry.rowPitch(d)
    val columnPitch: Double = HoneycombCrossSectionGeometry.columnPitch(d)
    val edgeX: Double = rowBasePairs * Gen1Tile.RISE_PER_BASE_PAIR
    val edgeY: Double = rasterRows * rowPitch
    val interiorPressure: Double = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)
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

private class T304Tile(
    val shared: T304Shared,
    val lattice: HoneycombGrillage
) {

    val freeStroke: Double by lazy {
        lattice.solve(uniformPressure(shared.interiorPressure)).meanDeflection
    }

    fun uncoupledDishingAt(samples: Int): Double =
        lattice.solve(shared.pressureField).peakDishing(samples) / freeStroke

    val uncoupledDishing: Double by lazy { uncoupledDishingAt(T304_SAMPLES) }

    private val cache = HashMap<Pair<List<Pair<Double, Double>>, Int>, InfluenceSurrogate>()

    fun surrogate(
        grid: List<Pair<Double, Double>>,
        samples: Int = T304_SAMPLES
    ): InfluenceSurrogate = cache.getOrPut(grid to samples) {
        honeycombTiedSurrogate(lattice, grid, shared.pressureField, samples)
    }
}

/** A turn state: no turn element at all, or a chain at each rim. */
private class T304Turn(
    val label: String,
    val lowRim: HoneycombTetherState? = null,
    val highRim: HoneycombTetherState? = null
)

private fun t304Tile(
    shared: T304Shared,
    enhancement: Double,
    turn: T304Turn,
    withPreload: Boolean = true,
    subdivisions: Int = 1
): T304Tile {
    val lattice = if (turn.lowRim != null && turn.highRim != null) honeycombTetheredLattice(
        block = shared.block, rowBasePairs = shared.rowBasePairs, enhancement = enhancement,
        lowRimState = turn.lowRim, highRimState = turn.highRim, withPreload = withPreload,
        subdivisions = subdivisions
    ) else HoneycombGrillage(
        block = shared.block,
        rowBasePairs = shared.rowBasePairs,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        hingeStiffnessEnhancement = enhancement,
        subdivisions = subdivisions
    )
    return T304Tile(shared, lattice)
}

// ------------------------------------------------------------------------------ the grading

private class T304Graded(val cell: T304Cell, val sample: DoubleArray)

@Suppress("LongParameterList")
private fun gradeT304Cell(
    turnState: String,
    shared: T304Shared,
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
): T304Graded {
    val nominal = surrogate.solve(stiffnesses).peakDishing / freeStroke
    val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
    sample.indices.forEach { sample[it] = sample[it] / freeStroke }
    val summary = summariseDropoutDishing(
        sample, nominal, ensemble.meanSurvivors, T304_TOLERANCE
    )
    return T304Graded(
        T304Cell(
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
            exceedanceOneSidedBound = summary.exceedanceOneSidedBound,
            meanSurvivors = summary.meanSurvivors,
            uncoupledDishingOverStroke = uncoupled,
            flatAtNominal = nominal < T304_TOLERANCE,
            flatAtP90 = summary.flatAtP90,
            beatsUncoupledAtP90 = summary.p90 < uncoupled
        ),
        sample
    )
}

private fun t304Distributions(
    grid: List<Pair<Double, Double>>,
    edgeX: Double,
    edgeY: Double
): List<Pair<String, List<Double>>> = listOf(
    "equal springs" to equalShareOfMandate(grid.size),
    "rim-graded 5:1" to rimGradedShareOfMandate(
        grid.map { (x, y) ->
            val onRim = abs(x) > edgeX / 2.0 - T304_RIM_BAND || abs(y) > edgeY / 2.0 - T304_RIM_BAND
            if (onRim) 5.0 else 1.0
        }
    )
)

/** `C-0167`'s four placements, unchanged, so the pairing is exact. */
private fun t304Placements(
    shared: T304Shared,
    rootingHelixY: List<Double>,
    columns: Int
): List<Pair<String, List<Pair<Double, Double>>>> {
    val abstract = attachmentGrid(columns, shared.rasterRows, shared.edgeX, shared.edgeY)
    val raster = twoLengthRaster(
        shared.rasterRows, shared.helicesPerRow, T304_RECOMMENDED_ONE, T304_RECOMMENDED_TWO
    )
    val determined = twoLengthSnappedGrid(
        raster, columns, shared.edgeY, T304_LADDER_PHASE, T304_LADDER_OFFSET
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

@Suppress("LongMethod", "ComplexMethod", "NestedBlockDepth", "TooGenericExceptionCaught")
fun main() {
    val realisations = if (System.getenv("T304_SMOKE") != null) 150 else 4_000
    val shared = T304Shared(t304Profile(ResultInputs.T_3B.file()))
    val fractions = listOf(0.30, 0.26)
    val gradedColumns = listOf(1, 2, 3, 5)
    val kBT = thermalEnergy(ROOM_TEMPERATURE)
    val rP = MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS
    val alignedSpan = minimumTurnPhosphateSpan(shared.d, rP)
    val worstSpan = maximumTurnPhosphateSpan(shared.d, rP)
    val stericSlack = shared.d - 2.0 * rP

    // ========================================= Deliverable 1 -- the anchors, no solver at all
    println("T-304 - the anchor azimuth on the recommended 102 / 109 raster")
    val recommended = HoneycombRasterTurnAnchors.derived(
        block = shared.block,
        senseOneBasePairs = T304_RECOMMENDED_ONE,
        senseTwoBasePairs = T304_RECOMMENDED_TWO,
        interhelicalDistance = shared.d,
        phosphateRadius = rP
    )
    val anchorRows = recommended.anchors.map {
        T304Anchor(
            index = it.index,
            turnLevelBasePairs = it.turnLevelBasePairs,
            anchorLevelBasePairs = it.anchorLevelBasePairs,
            reducedResidue = it.reducedResidue,
            anchorResidue = it.anchorResidue,
            atHighEnd = it.atHighEnd,
            exitAzimuthDegrees = it.exitAzimuthDegrees,
            entryAzimuthDegrees = it.entryAzimuthDegrees,
            span = it.span
        )
    }
    val determinedSpan = requireNotNull(recommended.singleValuedSpan) {
        "the 59 turns take more than one span: " + recommended.distinctSpans
    }
    val determinedAzimuth = recommended.distinctAzimuthMagnitudes.single()
    println(
        "  59 turns, residues " + recommended.anchors.map { it.reducedResidue }.distinct().sorted() +
                ", |azimuth| = " + determinedAzimuth.t304Emitted(9) + " deg, span " +
                determinedSpan.t304Emitted(9) + " nm"
    )

    // ========================================= Deliverable 2 -- the eight readings of the datum
    val datumRows = listOf(1, -1).flatMap { sign ->
        listOf(false, true).flatMap { mirrored ->
            listOf(false, true).map { reversed ->
                val reading = HoneycombRasterTurnAnchors.derived(
                    block = shared.block,
                    senseOneBasePairs = T304_RECOMMENDED_ONE,
                    senseTwoBasePairs = T304_RECOMMENDED_TWO,
                    interhelicalDistance = shared.d,
                    phosphateRadius = rP,
                    firstAxialSign = sign,
                    mirrored = mirrored,
                    axialReversed = reversed
                )
                T304Datum(
                    firstAxialSign = sign,
                    mirrored = mirrored,
                    axialReversed = reversed,
                    classZeroResidue = reading.classZeroResidue,
                    distinctAzimuthMagnitudes = reading.distinctAzimuthMagnitudes,
                    distinctSpans = reading.distinctSpans,
                    singleValuedSpan = reading.singleValuedSpan,
                    agreesWithTheStandardReading =
                        reading.singleValuedSpan != null &&
                                abs(reading.singleValuedSpan!! - determinedSpan) < 1e-9
                )
            }
        }
    }

    // ========================================= Deliverable 3 -- the OTHER design, offset by u
    val offsetRows = (0 until T304_PERIOD).map { offset ->
        val shifted = HoneycombRasterTurnAnchors.derived(
            block = shared.block,
            senseOneBasePairs = T304_RECOMMENDED_ONE,
            senseTwoBasePairs = T304_RECOMMENDED_TWO,
            interhelicalDistance = shared.d,
            phosphateRadius = rP,
            anchorOffsetBasePairs = offset
        )
        val magnitude = shifted.distinctAzimuthMagnitudes.single()
        T304Offset(
            anchorOffsetBasePairs = offset,
            reading = when (offset) {
                0 -> "the built and graded reading: the loop sits OUTBOARD of the duplex"
                12 -> "C-0200's short half-loop, carved OUT of the paired row instead"
                14 -> "C-0193's 14 / 14 reading, carved out"
                16 -> "C-0200's long half-loop, carved out"
                else -> "a hypothetical carve-out of " + offset + " bp"
            },
            azimuthMagnitudeDegrees = magnitude,
            span = shifted.distinctSpans.single(),
            spanOverAligned = shifted.distinctSpans.single() / alignedSpan,
            singleValued = shifted.distinctSpans.size == 1
        )
    }

    // ========================================= Deliverable 4 -- the bracket, collapsed
    println("T-304 - the collapse: C-0201's 36 corners at the determined azimuth")
    val cornerRows = ArrayList<T304Corner>()
    val determinedStates = ArrayList<Pair<String, T304Turn>>()
    fun cornerAt(
        label: String,
        azimuth: String,
        span: Double,
        low: Int,
        high: Int,
        kuhn: Double,
        contour: Double,
        fraction: Double,
        keep: Boolean
    ) {
        val lowState = freelyJointedTetherState(span, low, kuhn, contour, kBT)
        val highState = freelyJointedTetherState(span, high, kuhn, contour, kBT)
        val turn = T304Turn(label, lowState, highState)
        val with = t304Tile(shared, shared.enhancementAt(fraction), turn, withPreload = true)
        val without = t304Tile(shared, shared.enhancementAt(fraction), turn, withPreload = false)
        val field = with.lattice.solve(shared.pressureField).coefficients
        val closure = with.lattice.tetherElements
            .maxOf { abs(with.lattice.tetherChainExtension(field, it)) }
        cornerRows += T304Corner(
            turnState = label,
            unpairedNucleotides = if (low == high) low else -1,
            azimuth = azimuth,
            span = span,
            kuhnLength = kuhn,
            contourPerNucleotide = contour,
            contourLength = lowState.contourLength,
            extensionRatio = lowState.extensionRatio,
            reachable = lowState.extensionRatio < 1.0 && highState.extensionRatio < 1.0,
            tension = lowState.tension,
            secantStiffness = lowState.secantStiffness,
            tangentStiffness = lowState.tangentStiffness,
            compositeFraction = fraction,
            freeTileWithPreload = with.uncoupledDishing,
            freeTileWithoutPreload = without.uncoupledDishing,
            movement = with.uncoupledDishing - without.uncoupledDishing,
            worstRimClosure = closure,
            stericSlack = stericSlack,
            closureExceedsStericSlack = closure > stericSlack,
            flatWithPreload = with.uncoupledDishing < T304_TOLERANCE
        )
        if (keep && fraction == 0.30) determinedStates += label to turn
    }
    listOf(T304_M13_LOOP, T304_MIDDLE_LOOP, T304_BUILT_LOOP).forEach { nucleotides ->
        T304_KUHN.forEach { kuhn ->
            T304_CONTOUR.forEach { contour ->
                cornerAt(
                    "determined azimuth, " + nucleotides + " nt, b = " + kuhn.t304Emitted(3) +
                            ", c = " + contour.t304Emitted(3),
                    "DETERMINED, " + determinedAzimuth.t304Emitted(9) + " deg",
                    determinedSpan, nucleotides, nucleotides, kuhn, contour, 0.30,
                    keep = nucleotides != T304_MIDDLE_LOOP && kuhn == 2.10 && contour == 0.65
                )
            }
        }
    }
    T304_KUHN.forEach { kuhn ->
        T304_CONTOUR.forEach { contour ->
            cornerAt(
                "determined azimuth, C-0200's ordered 24 / 32 split, b = " +
                        kuhn.t304Emitted(3) + ", c = " + contour.t304Emitted(3),
                "DETERMINED, " + determinedAzimuth.t304Emitted(9) + " deg",
                determinedSpan, T304_ORDERED_LOW_RIM, T304_ORDERED_HIGH_RIM, kuhn, contour, 0.30,
                keep = kuhn == 2.10 && contour == 0.65
            )
        }
    }
    // C-0201's own two bracket endpoints, RE-RUN here so the collapse is against a reproduction
    cornerAt(
        "C-0201's soft endpoint: aligned azimuth, 28 nt, softest chain",
        "aligned, d - 2 r_P", alignedSpan, T304_BUILT_LOOP, T304_BUILT_LOOP, 2.84, 0.70, 0.30,
        keep = false
    )
    cornerAt(
        "C-0201's hard endpoint: worst azimuth, 15 nt, stiffest chain",
        "worst, d + 2 r_P", worstSpan, T304_M13_LOOP, T304_M13_LOOP, 2.10, 0.65, 0.30,
        keep = false
    )
    cornerAt(
        "C-0201's built headline: worst azimuth, 28 nt, stiffest chain",
        "worst, d + 2 r_P", worstSpan, T304_BUILT_LOOP, T304_BUILT_LOOP, 2.10, 0.65, 0.30,
        keep = false
    )
    cornerRows.forEach {
        println(
            "  " + it.turnState + "  f = " + it.tension.t304Emitted(9) + " pN  free tile " +
                    it.freeTileWithPreload.t304Emitted(9) +
                    (if (it.flatWithPreload) "  flat" else "  NOT FLAT")
        )
    }

    // ========================================= Deliverable 5 -- route B's own uniform rasters
    println("T-304 - route B's own uniform rasters, whose b0 is a free design variable")
    val uniformRows = ArrayList<T304UniformRaster>()
    listOf(
        "M13mp18" to T304_M13, "p7560" to T304_P7560, "p8064" to T304_P8064
    ).forEach { (name, scaffold) ->
        val paired = maximumUniformRowLength(scaffold, T304_HELICES, T304_UNPAIRED_PER_HELIX)
        (0 until T304_PERIOD).forEach { b0 ->
            val anchors = HoneycombRasterTurnAnchors(
                block = shared.block,
                senseOneBasePairs = paired,
                senseTwoBasePairs = paired,
                interhelicalDistance = shared.d,
                phosphateRadius = rP,
                classZeroResidue = b0
            )
            val spans = anchors.anchors.map { it.span }
            val inside = spans.count { it < shared.d }
            uniformRows += T304UniformRaster(
                scaffold = name,
                scaffoldNucleotides = scaffold,
                pairedRowBasePairs = paired,
                rowWidth = paired * Gen1Tile.RISE_PER_BASE_PAIR,
                closes = anchors.closes,
                classZeroResidue = b0,
                distinctSpanCount = anchors.distinctSpans.size,
                minimumSpan = spans.min(),
                maximumSpan = spans.max(),
                meanSpan = spans.average(),
                turnsInsideTheAlignedHalf = inside,
                allInsideTheAlignedHalf = inside == spans.size
            )
        }
    }
    listOf("M13mp18", "p7560", "p8064").forEach { name ->
        val best = uniformRows.filter { it.scaffold == name }.minBy { it.maximumSpan }
        println(
            "  " + name + "  " + best.pairedRowBasePairs + " bp  best b0 = " +
                    best.classZeroResidue + "  spans " + best.minimumSpan.t304Emitted(9) + " to " +
                    best.maximumSpan.t304Emitted(9) + " nm over " + best.distinctSpanCount +
                    " values"
        )
    }

    // ========================================= Deliverable 6 -- the 64 coupled cells
    println("T-304 - the coupled re-grade at the DETERMINED span, " + realisations + " draws")
    val states: List<T304Turn> =
        listOf(T304Turn("untied")) + determinedStates.map { it.second }
    val tiles = HashMap<Pair<Double, String>, T304Tile>()
    val geometries = ArrayList<T304Geometry>()
    val probe = t304Tile(shared, shared.enhancementAt(0.30), T304Turn("probe"))
    val rootingHelixY = probe.lattice.faceBeams.map { probe.lattice.beamY[it] }
    fractions.forEach { fraction ->
        val enhancement = shared.enhancementAt(fraction)
        states.forEach { turn ->
            val tile = t304Tile(shared, enhancement, turn)
            tiles[fraction to turn.label] = tile
            geometries += T304Geometry(
                turnState = turn.label,
                compositeFraction = fraction,
                hingeStiffnessEnhancement = enhancement,
                turnTethers = tile.lattice.tetherElements.size,
                degreesOfFreedom = tile.lattice.degreesOfFreedom,
                freeStroke = tile.freeStroke,
                uncoupledDishingOverStroke = tile.uncoupledDishing,
                uncoupledFlat = tile.uncoupledDishing < T304_TOLERANCE
            )
        }
    }
    val cells = ArrayList<T304Cell>()
    val samples = HashMap<String, DoubleArray>()
    val incorporation = measuredDepthIncorporation(shared.edgeX, shared.edgeY)
    gradedColumns.forEach { columns ->
        t304Placements(shared, rootingHelixY, columns).forEach { (placement, grid) ->
            val ensemble = dropoutEnsemble(
                grid.map { (x, y) -> incorporation.at(x, y) }, realisations, T304_SEED
            )
            t304Distributions(grid, shared.edgeX, shared.edgeY).forEach { (label, stiffnesses) ->
                fractions.forEach { fraction ->
                    states.forEach { turn ->
                        val tile = tiles.getValue(fraction to turn.label)
                        val graded = gradeT304Cell(
                            turn.label, shared, fraction, placement, columns, grid, label,
                            stiffnesses, tile.surrogate(grid), tile.freeStroke,
                            tile.uncoupledDishing, ensemble
                        )
                        cells += graded.cell
                        samples[turn.label + "|" + fraction + "|" + placement + "|" + columns +
                                "|" + label] = graded.sample
                    }
                }
            }
        }
    }
    val paired = ArrayList<T304Paired>()
    gradedColumns.forEach { columns ->
        t304Placements(shared, rootingHelixY, columns).forEach { (placement, grid) ->
            t304Distributions(grid, shared.edgeX, shared.edgeY).forEach { (label, _) ->
                fractions.forEach { fraction ->
                    determinedStates.forEach { (subjectState, _) ->
                        val key = { state: String ->
                            state + "|" + fraction + "|" + placement + "|" + columns + "|" + label
                        }
                        val summary = pairedRatioSummary(
                            samples.getValue(key(subjectState)), samples.getValue(key("untied"))
                        )
                        fun cellOf(state: String) = cells.first {
                            it.turnState == state && it.compositeFraction == fraction &&
                                    it.placement == placement && it.columns == columns &&
                                    it.distribution == label
                        }
                        val subject = cellOf(subjectState)
                        val reference = cellOf("untied")
                        paired += T304Paired(
                            comparison = subjectState + " over C-0167's untied lattice",
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
                            verdictMoved = reference.flatAtP90 != subject.flatAtP90
                        )
                    }
                }
            }
        }
    }
    val untiedCells = cells.filter { it.turnState == "untied" }

    // ========================================= reproductions
    println("T-304 - reproductions")
    val reproductions = ArrayList<T304Reproduction>()
    fun reproduce(source: String, quantity: String, published: Double, here: Double) {
        val dep = if (published == 0.0) abs(here) else abs(here - published) / abs(published)
        reproductions += T304Reproduction(source, quantity, published, here, dep, dep < 1e-8)
    }
    val t246Span = Json.parseToJsonElement(ResultInputs.T_246.file().readText())
        .jsonObject.getValue("allowedCrossoverReadings").jsonArray.map { it.jsonObject }
        .first {
            abs(
                it.getValue("azimuthDegrees").jsonPrimitive.content.toDouble() -
                        allowedScaffoldCrossoverDepartureDegrees()
            ) < 1e-6
        }.getValue("spanNm").jsonPrimitive.content.toDouble()
    reproduce(
        "C-0152 (T-246)", "the ALLOWED scaffold crossover's own span, read out of its result file",
        t246Span, determinedSpan
    )
    reproduce("C-0147 (T-230)", "the aligned span, d - 2 r_P", 0.718724283, alignedSpan)
    reproduce("C-0147 (T-230)", "the worst-azimuth span, d + 2 r_P", 4.35327572, worstSpan)
    reproduce(
        "C-0152 (CH-0197)", "the allowed departure, one quarter of a base pair",
        8.57142857, determinedAzimuth
    )
    reproduce(
        "C-0167 (T-263)", "untied free tile, 10 x 6, f = 0.30",
        0.0501417316, tiles.getValue(0.30 to "untied").uncoupledDishing
    )
    reproduce(
        "C-0167 (T-263)", "untied free tile, 10 x 6, f = 0.26",
        0.0522223659, tiles.getValue(0.26 to "untied").uncoupledDishing
    )
    reproduce(
        "C-0201 (T-299)", "the free tile at the ALIGNED azimuth, 28 nt, softest chain",
        0.0569815008,
        cornerRows.first { it.turnState.startsWith("C-0201's soft") }.freeTileWithPreload
    )
    reproduce(
        "C-0201 (T-299)", "the free tile at the WORST azimuth, 15 nt, stiffest chain",
        0.166312182,
        cornerRows.first { it.turnState.startsWith("C-0201's hard") }.freeTileWithPreload
    )
    reproduce(
        "C-0201 (T-299)", "the free tile at the WORST azimuth, 28 nt, stiffest chain",
        0.11296458,
        cornerRows.first { it.turnState.startsWith("C-0201's built") }.freeTileWithPreload
    )
    reproduce(
        "C-0193 (T-296)", "the 15 nt turn's tension at the worst azimuth, b = 2.10, c = 0.65",
        3.03288672, freelyJointedTetherState(worstSpan, 15, 2.10, 0.65, kBT).tension
    )
    reproduce(
        "C-0201 (T-299)", "M13mp18's uniform paired row at the built allowance, in nm", 31.28,
        uniformRows.first { it.scaffold == "M13mp18" }.rowWidth
    )
    reproduce(
        "C-0201 (T-299)", "p8064's uniform paired row at the built allowance, in nm", 36.04,
        uniformRows.first { it.scaffold == "p8064" }.rowWidth
    )
    // C-0167's own 64 committed cells, which is the CONTROL on the duplicated machinery
    val t263 = Json.parseToJsonElement(ResultInputs.T_263.file().readText())
        .jsonObject.getValue("cells").jsonArray.map { it.jsonObject }
    var worstCellDeparture = 0.0
    var cellsClosing = 0
    untiedCells.forEach { cell ->
        val record = t263.first {
            it.getValue("model").jsonPrimitive.content == "honeycomb grillage" &&
                    it.getValue("compositeFraction").jsonPrimitive.content.toDoubleOrNull() ==
                    cell.compositeFraction &&
                    it.getValue("placement").jsonPrimitive.content == cell.placement &&
                    it.getValue("columns").jsonPrimitive.content.toInt() == cell.columns &&
                    it.getValue("distribution").jsonPrimitive.content == cell.distribution
        }
        listOf(
            "p90OverStroke" to cell.p90OverStroke, "nominalOverStroke" to cell.nominalOverStroke
        ).forEach { (key, here) ->
            val published = record.getValue(key).jsonPrimitive.content.toDouble()
            val dep = if (published == 0.0) abs(here) else abs(here - published) / abs(published)
            if (dep < 1e-8) cellsClosing++
            worstCellDeparture = maxOf(worstCellDeparture, dep)
        }
    }
    val worstCellDepartureEmitted = roundForResult(worstCellDeparture, 2, 0.0)
    reproductions += T304Reproduction(
        source = "C-0167 (T-263)",
        quantity = "all " + untiedCells.size + " committed cells, p90 and nominal -- " +
                cellsClosing + " of " + (2 * untiedCells.size) + " values close at 1e-8. This " +
                "row is the CONTROL on the placement, distribution and ensemble machinery this " +
                "study duplicates from T-299, whose helpers are private to it. Both columns " +
                "are a DEPARTURE and are emitted at two significant digits",
        published = 0.0,
        here = worstCellDepartureEmitted,
        departure = worstCellDepartureEmitted,
        closes = cellsClosing == 2 * untiedCells.size
    )
    reproductions.forEach {
        println(
            "  " + it.source + "  " + it.quantity + "  departure " + it.departure.t304Emitted(2) +
                    (if (it.closes) "  closes" else "  DOES NOT CLOSE")
        )
    }

    // ========================================= convergence, on the DECIDING quantity
    println("T-304 - convergence")
    val convergence = ArrayList<T304Convergence>()
    val decidingLabel = determinedStates.first {
        it.first.contains(T304_BUILT_LOOP.toString() + " nt")
    }.first
    val decidingTurn = determinedStates.first { it.first == decidingLabel }.second
    listOf("untied control" to T304Turn("untied"), "determined tether" to decidingTurn)
        .forEach { (name, turn) ->
            val bySubdivision = listOf(1, 2).map { subdivisions ->
                subdivisions to t304Tile(
                    shared, shared.enhancementAt(0.30), turn, subdivisions = subdivisions
                ).uncoupledDishing
            }
            val subdivisionReference = bySubdivision.first { it.first == 1 }.second
            bySubdivision.forEach { (subdivisions, value) ->
                convergence += T304Convergence(
                    cell = name + ", free tile, f = 0.30",
                    quantity = "the uncoupled dishing over the stroke",
                    axis = "beam subdivisions",
                    level = subdivisions.toString(),
                    value = value,
                    departure = abs(value - subdivisionReference) / subdivisionReference,
                    verdictAtThisLevel = value < T304_TOLERANCE
                )
            }
            val tile = t304Tile(shared, shared.enhancementAt(0.30), turn)
            val bySamples = listOf(41, 81, 161).map { it to tile.uncoupledDishingAt(it) }
            val sampleReference = bySamples.first { it.first == T304_SAMPLES }.second
            bySamples.forEach { (count, value) ->
                convergence += T304Convergence(
                    cell = name + ", free tile, f = 0.30",
                    quantity = "the uncoupled dishing over the stroke",
                    axis = "dishing sample grid",
                    level = count.toString(),
                    value = value,
                    departure = abs(value - sampleReference) / sampleReference,
                    verdictAtThisLevel = value < T304_TOLERANCE
                )
            }
        }
    // and on the deciding COUPLED cell, which is where a verdict is read
    val tightest = cells.filter { it.turnState == decidingLabel }.minBy { it.p90OverStroke }
    val tightestGrid = t304Placements(shared, rootingHelixY, tightest.columns)
        .first { it.first == tightest.placement }.second
    val tightestStiffnesses = t304Distributions(tightestGrid, shared.edgeX, shared.edgeY)
        .first { it.first == tightest.distribution }.second
    val tightestEnsemble = dropoutEnsemble(
        tightestGrid.map { (x, y) -> incorporation.at(x, y) }, realisations, T304_SEED
    )
    val tightestCell = "the tightest determined coupled cell: " + tightest.placement + ", " +
            tightest.columns + " columns, " + tightest.distribution + ", f = " +
            tightest.compositeFraction.t304Emitted(3)
    val tightestReference = tightest.p90OverStroke
    fun gradeTightest(tile: T304Tile, count: Int): T304Cell = gradeT304Cell(
        decidingLabel, shared, tightest.compositeFraction, tightest.placement,
        tightest.columns, tightestGrid, tightest.distribution, tightestStiffnesses,
        tile.surrogate(tightestGrid, count), tile.freeStroke, tile.uncoupledDishing,
        tightestEnsemble
    ).cell
    val tightestTile = tiles.getValue(tightest.compositeFraction to decidingLabel)
    listOf(41, 81, 161).forEach { count ->
        val graded = gradeTightest(tightestTile, count)
        convergence += T304Convergence(
            cell = tightestCell,
            quantity = "the 90th percentile of the dropout ensemble, over the stroke",
            axis = "dishing sample grid",
            level = count.toString(),
            value = graded.p90OverStroke,
            departure = abs(graded.p90OverStroke - tightestReference) / tightestReference,
            verdictAtThisLevel = graded.flatAtP90
        )
    }
    listOf(1, 2).forEach { subdivisions ->
        val graded = gradeTightest(
            t304Tile(
                shared, shared.enhancementAt(tightest.compositeFraction), decidingTurn,
                subdivisions = subdivisions
            ),
            T304_SAMPLES
        )
        convergence += T304Convergence(
            cell = tightestCell,
            quantity = "the 90th percentile of the dropout ensemble, over the stroke",
            axis = "beam subdivisions",
            level = subdivisions.toString(),
            value = graded.p90OverStroke,
            departure = abs(graded.p90OverStroke - tightestReference) / tightestReference,
            verdictAtThisLevel = graded.flatAtP90
        )
    }
    convergence.forEach {
        println(
            "  " + it.cell + "  " + it.axis + " = " + it.level + "  " + it.value.t304Emitted(9) +
                    "  departure " + it.departure.t304Emitted(2)
        )
    }

    // ========================================= falsifiers
    println("T-304 - falsifiers")
    val determinedCorners = cornerRows.filter { it.azimuth.startsWith("DETERMINED") }
    val notFlat = determinedCorners.filter { !it.flatWithPreload }
    val movedVerdicts = paired.count { it.verdictMoved }
    val unreachable = determinedCorners.count { !it.reachable }
    val pastSlack = determinedCorners.count { it.closureExceedsStericSlack }
    val closedFormAgrees = (0 until T304_PERIOD).all { b0 ->
        listOf(5, -5).all { displacement ->
            val residue = Math.floorMod(b0 + displacement, T304_PERIOD)
            abs(
                anchorAzimuthDegrees(residue, b0) -
                        scaffoldDisplacementDepartureDegrees(displacement)
            ) < 1e-9
        }
    }
    val softRastersExist = listOf("M13mp18", "p7560", "p8064").associateWith { name ->
        uniformRows.any { it.scaffold == name && it.allInsideTheAlignedHalf }
    }
    val t246Departure = abs(determinedSpan - t246Span) / t246Span
    val falsifiers = listOf(
        T304Falsifier(
            "F1", "the 59 turns do not all take the same span on the 102 / 109 raster",
            recommended.distinctSpans.size > 1,
            "the 59 turns take " + recommended.distinctSpans.size + " distinct span(s): " +
                    recommended.distinctSpans.map { it.t304Emitted(9) } + " nm. The departure's " +
                    "rim alternation and the anchor offset's rim alternation both enter through " +
                    "cos(theta), which is even, so the two rims collapse to one span"
        ),
        T304Falsifier(
            "F2", "the derived span is not C-0152's own allowed-crossover span",
            t246Departure >= 1e-9,
            "T-246's own committed span at the allowed departure is " + t246Span.t304Emitted(9) +
                    " nm and this derivation gives " + determinedSpan.t304Emitted(9) +
                    " nm, departure " + t246Departure.t304Emitted(2)
        ),
        T304Falsifier(
            "F3", "the closed form disagrees with C-0187's derived departure at some b0",
            !closedFormAgrees,
            "theta(rho, b0) = fold((rho - b0 - 21/4) x 240/7) reproduces " +
                    "scaffoldDisplacementDepartureDegrees at both allowed residues at all " +
                    T304_PERIOD + " lattice constants"
        ),
        T304Falsifier(
            "F4", "the span or an azimuth magnitude is not invariant over the eight datum " +
                    "readings of (firstAxialSign, mirrored, axialReversed)",
            datumRows.any { !it.agreesWithTheStandardReading },
            datumRows.count { it.agreesWithTheStandardReading }.toString() + " of " +
                    datumRows.size + " readings return the same single-valued span. The " +
                    "AZIMUTH CONSTANT travels with the axial datum: +240/7 deg per base pair of " +
                    "INCREASING z, so reversing z reverses it (CLAUDE.md)"
        ),
        T304Falsifier(
            "F5", "the determined span leaves the FREE tile past T-5b at some surviving corner",
            notFlat.isNotEmpty(),
            notFlat.size.toString() + " of " + determinedCorners.size + " determined corners " +
                    "are past 0.10; the free tile runs " +
                    determinedCorners.minOf { it.freeTileWithPreload }.t304Emitted(9) + " to " +
                    determinedCorners.maxOf { it.freeTileWithPreload }.t304Emitted(9) +
                    " of the stroke, against C-0201's bracket of 0.0569815008 to 0.166312182"
        ),
        T304Falsifier(
            "F6", "the determined span moves a COUPLED verdict against C-0167's untied lattice",
            movedVerdicts > 0,
            movedVerdicts.toString() + " of " + paired.size + " paired cells move a verdict; " +
                    untiedCells.count { it.flatAtP90 } + " of " + untiedCells.size +
                    " untied cells are flat at the 90th percentile and " +
                    cells.filter { it.turnState != "untied" }.count { it.flatAtP90 } + " of " +
                    cells.count { it.turnState != "untied" } + " tethered ones"
        ),
        T304Falsifier(
            "F7", "no b0 puts every turn of a uniform route-B raster inside the aligned half",
            softRastersExist.values.none { it },
            softRastersExist.entries.joinToString("; ") {
                it.key + ": " + (if (it.value) "some b0 does" else "no b0 does")
            }
        ),
        T304Falsifier(
            "F8", "the reach bound refuses at some determined corner",
            unreachable > 0,
            unreachable.toString() + " of " + determinedCorners.size + " corners are " +
                    "unreachable; the smallest contour here is " +
                    determinedCorners.minOf { it.contourLength }.t304Emitted(9) +
                    " nm against a span of " + determinedSpan.t304Emitted(9) + " nm"
        ),
        T304Falsifier(
            "F9", "the preload's rim closure at the determined span exceeds the steric slack",
            pastSlack > 0,
            pastSlack.toString() + " of " + determinedCorners.size + " corners close the rim " +
                    "pair by more than " + stericSlack.t304Emitted(9) + " nm; the worst closure " +
                    "is " + determinedCorners.maxOf { it.worstRimClosure }.t304Emitted(9) + " nm"
        )
    )
    falsifiers.forEach {
        println("  " + it.name + (if (it.fired) "  FIRED  " else "  did not fire  ") + it.note)
    }

    // ========================================= emission
    val cheapBound = listOf(
        T304CheapBoundRow(
            question = "what fixes the span of route B's tether?",
            answer = "the base-pair index of the last PAIRED base and the lattice phase, and " +
                    "nothing else. The chain leaves helix a at that base's phosphate and enters " +
                    "helix b at its own; both helices of a honeycomb bond are parallel, " +
                    "same-handed and at one design twist, so a level displacement rotates BOTH " +
                    "backbones the same way and one angle serves the pair",
            consequence = "theta(rho, b0) = fold((rho - b0 - 21/4) x 240/7 deg) and span(theta) " +
                    "= sqrt(d^2 - 4 d r_P cos theta + 4 r_P^2). Fifty-nine turns, eight datum " +
                    "readings, no solver at all"
        ),
        T304CheapBoundRow(
            question = "and on the recommended raster, what is it?",
            answer = "C-0187 pins b0 = 5 with residues [0, 10] on the drawable 102 / 109 raster, " +
                    "so every raster turn sits at an ALLOWED scaffold crossover and the anchor " +
                    "azimuth is C-0152's own quarter base pair, " +
                    determinedAzimuth.t304Emitted(9) + " deg",
            consequence = "the span is " + determinedSpan.t304Emitted(9) + " nm at all 59 " +
                    "turns -- " + (determinedSpan / alignedSpan).t304Emitted(9) + "x the " +
                    "aligned end of C-0201's bracket and " +
                    (worstSpan / determinedSpan).t304Emitted(9) + "x below its worst end. It is " +
                    "a number the corpus already carries, in T-246's own result file"
        ),
        T304CheapBoundRow(
            question = "why does the rim alternation not split it into two populations?",
            answer = "because the span depends on cos(theta), which is EVEN. C-0187's derived " +
                    "departure is +8.57142857 deg at the high rim and its negation at the low " +
                    "one, and an anchor offset moves inboard at both rims, so the two rims' " +
                    "azimuths are exact negatives at every offset",
            consequence = "one span, at every one of the 59 turns and at every anchor offset -- " +
                    "which is F1, declared open before the run, and it did not fire"
        ),
        T304CheapBoundRow(
            question = "does the collapse need the coupling bank re-solved?",
            answer = "no. C-0201 establishes that the preload is C-0104's internal initial " +
                    "stress -- it changes no entry of the stiffness matrix and the field is " +
                    "exactly linear in it -- so pricing a DETERMINED span is a re-evaluation of " +
                    "an element whose machinery exists, at one span instead of three",
            consequence = "C-0201's 36 corners fall to " + determinedCorners.size + " and the " +
                    "coupled grade is a CHECK of a prediction rather than a search"
        )
    )
    var proseFailure = "none"
    val findings: List<String> = try {
        listOf(
            "THE SPAN IS NOT A BRACKET AND IT IS NOT UNKNOWN: on the recommended 102 / 109 " +
                    "raster every one of the 59 raster turns anchors at an ALLOWED scaffold " +
                    "crossover, so its two phosphates sit at " +
                    determinedAzimuth.t304Emitted(9) + " deg and " +
                    (determinedAzimuth + 180.0).t304Emitted(9) + " deg from the line of " +
                    "centres and the tether's span is " + determinedSpan.t304Emitted(9) +
                    " nm -- ONE value, at all 59 turns and at all eight readings of the axial " +
                    "datum. It is C-0152's own allowed-crossover span, reproduced out of " +
                    "T-246's committed result file at a departure of " +
                    t246Departure.t304Emitted(2) + ".",
            "THE BRACKET COLLAPSES ONTO ITS SOFT END AND THE FREE TILE IS FLAT. C-0201's 36 " +
                    "corners fall to " + determinedCorners.size + ", whose free tile runs " +
                    determinedCorners.minOf { it.freeTileWithPreload }.t304Emitted(9) + " to " +
                    determinedCorners.maxOf { it.freeTileWithPreload }.t304Emitted(9) +
                    " of the stroke against C-0201's 0.0569815008 to 0.166312182 -- " +
                    determinedCorners.count { it.flatWithPreload } + " of " +
                    determinedCorners.size + " flat, where the bracket was 24 of 36. The " +
                    "chain's tension falls to " +
                    determinedCorners.minOf { it.tension }.t304Emitted(9) + " to " +
                    determinedCorners.maxOf { it.tension }.t304Emitted(9) + " pN from " +
                    "C-0201's 0.160569993 to 3.03288672.",
            "THE COUPLED VERDICT DOES NOT MOVE, AND IT IS MEASURED RATHER THAN INFERRED: over " +
                    "C-0167's own 64 cells on the same " + realisations + "-realisation stream " +
                    "restricted per cell, " +
                    cells.filter { it.turnState != "untied" }.count { it.flatAtP90 } + " of " +
                    cells.count { it.turnState != "untied" } + " determined-span cells are " +
                    "flat at the 90th percentile against the untied lattice's " +
                    untiedCells.count { it.flatAtP90 } + " of " + untiedCells.size + ", and " +
                    movedVerdicts + " of " + paired.size + " paired comparisons move a verdict. " +
                    "The untied re-grade reproduces all " + (2 * untiedCells.size) +
                    " of C-0167's committed values at 1e-8, which is the CONTROL on the " +
                    "machinery this study duplicates.",
            "THE ANCHOR OFFSET IS A DESIGN AND NOT A TOLERANCE, and one rung of it lands " +
                    "exactly on C-0201's own worst corner: a design that carved the loop OUT of " +
                    "the paired row instead of adding it outboard would put the anchor " +
                    "16 bp inboard, and 16 bp less the quarter base pair an allowed crossover " +
                    "already carries is EXACTLY 15.75 bp = 1.5 turns, so the span is exactly " +
                    "d + 2 r_P. The built object is not that design: C-0200 reads its duplex " +
                    "over the identical window 28..125 on all 60 helices with the scaffold " +
                    "beyond it, and C-0201 section 7's width arithmetic assumes the same.",
            "ROUTE B'S OWN UNIFORM RASTERS DO NOT CLOSE, SO THEIR SPAN IS A DISTRIBUTION AND " +
                    "THEIR PHASE IS A DESIGN VARIABLE. At the built allowance the paired rows " +
                    "are " + uniformRows.map { it.pairedRowBasePairs }.distinct().sorted() +
                    " bp, none of them closes on caDNAno's rule, and over all " + T304_PERIOD +
                    " lattice constants the best reading of each still spreads its 59 turns " +
                    "over " + uniformRows.minOf { it.distinctSpanCount } + " to " +
                    uniformRows.maxOf { it.distinctSpanCount } + " distinct spans, worst " +
                    uniformRows.minOf { it.maximumSpan }.t304Emitted(9) + " to " +
                    uniformRows.maxOf { it.maximumSpan }.t304Emitted(9) + " nm. " +
                    "The determined answer belongs to the DRAWABLE raster, and it is bought " +
                    "by drawability.",
            "THE STERIC QUESTION CLOSES WITH IT: at the determined span " + pastSlack + " of " +
                    determinedCorners.size + " corners close the rim pair by more than the " +
                    stericSlack.t304Emitted(9) + " nm of slack T-71's measured phosphate radius " +
                    "leaves, worst " +
                    determinedCorners.maxOf { it.worstRimClosure }.t304Emitted(9) + " nm -- " +
                    "against C-0201's own worst of 0.549926604 nm at 1.31x margin."
        )
    } catch (failure: Exception) {
        proseFailure = failure.toString()
        emptyList()
    }

    val result = T304Result(
        task = "T-304",
        leaf = "A8.2",
        title = "the anchor azimuth of a raster turn, derived on the lattice, and C-0201's " +
                "36-corner span bracket collapsed onto it",
        verificationType = "logical (exact integer residue arithmetic on this repository's own " +
                "honeycomb crossover lattice, a closed form for the azimuth, and C-0147's span " +
                "geometry consumed unmodified) + in-silico (the same honeycomb grillage, the " +
                "same tether element and the same 64 coupled cells C-0167, C-0180 and C-0201 " +
                "share, re-run at the determined span) + literature (C-0152's allowed-crossover " +
                "span, C-0200's duplex window, T-71's measured phosphate radius and T-230's " +
                "ssDNA Kuhn and contour brackets)",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated. The " +
                "azimuth is a property of a DESIGN -- the drawable 102 / 109 raster of the " +
                "10 x 6 block -- and not of a folded object; no folding experiment is reported " +
                "and this repository cannot run one.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm",
            "angle" to "degrees at every boundary of this study, radians only inside a " +
                    "trigonometric call",
            "level" to "integer base pairs on one global z",
            "dishing" to "dimensionless, as a fraction of the free stroke"
        ),
        conventions = mapOf(
            "azimuth" to "measured at helix a from the line of centres pointing at helix b, " +
                    "positive in the sense of increasing z at +240/7 deg per base pair; " +
                    "theta = 0 is closest approach and theta = 180 deg is furthest",
            "the two anchors" to "at the SAME base-pair level. Both helices of a honeycomb bond " +
                    "are parallel, same-handed and at one design twist, so a level displacement " +
                    "rotates both backbones the same way and the entry azimuth is the exit " +
                    "azimuth plus 180 deg exactly",
            "the exact facing residue" to "b0 + 21/4, not b0 + 5. caDNAno's rule is five base " +
                    "pairs OR HALF A TURN and the half turn at 10.5 bp/turn is 5.25 bp; because " +
                    "10.5 bp is exactly 360 deg, b0 + 5.25 and b0 - 5.25 are ONE azimuth",
            "the anchor reading" to "the raster's row lengths are PAIRED lengths and route B's " +
                    "unpaired loop sits OUTBOARD of the duplex, so the last paired base sits AT " +
                    "the raster level and anchorOffsetBasePairs is 0. That is C-0200's reading " +
                    "of the built block and C-0201 section 7's own width arithmetic",
            "the turn census" to "honeycombRasterTurnList's, unchanged -- the same 59 sites " +
                    "route A uses, so the comparison with C-0175, C-0180 and C-0201 is " +
                    "controlled",
            "W" to "positive DOWNWARD, toward the electrode (C-0006)",
            "the datum" to "AZIMUTH_PER_BASE_PAIR is +240/7 deg per base pair of INCREASING z, " +
                    "so it reverses whenever z does; a residue map is a handedness (CLAUDE.md)"
        ),
        parameters = mapOf(
            "crossSection" to shared.crossSection,
            "raster" to (T304_RECOMMENDED_ONE.toString() + " / " + T304_RECOMMENDED_TWO +
                    " (C-0151, drawable)"),
            "rowBasePairs" to shared.rowBasePairs.toString(),
            "edgeX" to shared.edgeX.t304Emitted(9),
            "edgeY" to shared.edgeY.t304Emitted(9),
            "interhelicalDistance" to shared.d.t304Emitted(9),
            "phosphateRadius" to rP.t304Emitted(9),
            "risePerBasePair" to Gen1Tile.RISE_PER_BASE_PAIR.t304Emitted(9),
            "azimuthPerBasePair" to AZIMUTH_PER_BASE_PAIR.t304Emitted(9),
            "exactHalfTurnBasePairs" to EXACT_HALF_TURN_BASE_PAIRS.t304Emitted(9),
            "classZeroResidue" to recommended.classZeroResidue.toString(),
            "determinedAzimuthDegrees" to determinedAzimuth.t304Emitted(9),
            "determinedSpan" to determinedSpan.t304Emitted(9),
            "alignedSpan" to alignedSpan.t304Emitted(9),
            "worstAzimuthSpan" to worstSpan.t304Emitted(9),
            "stericSlack" to stericSlack.t304Emitted(9),
            "interiorPressure" to shared.interiorPressure.t304Emitted(9),
            "foundationStiffness" to Gen1Tile.FOUNDATION_SECANT.t304Emitted(9),
            "thermalEnergy" to kBT.t304Emitted(9),
            "electrolyte" to "MgCl2 (2:1)",
            "bufferMillimolar" to T304_BUFFER_MILLIMOLAR.t304Emitted(3),
            "gapHeightNm" to T304_GAP_NM.t304Emitted(3),
            "appliedBiasVolts" to T304_BIAS_VOLTS.t304Emitted(3),
            "temperatureKelvin" to "300",
            "whyTheRegimeBlockIsNull" to "environment.Regime holds the gap and the bias as " +
                    "INTERVALS and refuses a degenerate one; this study solves no " +
                    "electrostatics, it reads ONE profile record of T-3b and uses it as a fixed " +
                    "load shape, so the state is a POINT and belongs here rather than in a " +
                    "range (C-0181, CH-0224)",
            "kuhnBracket" to "2.10 to 2.84 nm, zero force",
            "contourBracket" to "0.65 to 0.70 nm/nt, inextensible",
            "loopLengths" to "15, 20 and 28 nt (C-0201's own set) plus C-0200's ordered 24 / 32",
            "compositeFractions" to "0.30 and 0.26 (C-0116)",
            "mandate" to "C-0017's 33.3333 pN/nm on the SUM, section 3's acceptable clause",
            "realisations" to realisations.toString(),
            "seed" to T304_SEED.toString(),
            "samples" to T304_SAMPLES.toString(),
            "tolerance" to T304_TOLERANCE.t304Emitted(2),
            "ladderPhase" to T304_LADDER_PHASE.toString(),
            "ladderOffset" to T304_LADDER_OFFSET.toString(),
            "firstAxialSign" to "+1",
            "linkStiffness" to HoneycombGrillage.RIGID_LINK_STIFFNESS.t304Emitted(9)
        ),
        sources = listOf(
            ResultInputs.T_3B.path + " (C-0022's solved collar at 2 mM / 10 nm / 0.192 V)",
            ResultInputs.T_246.path + " (C-0152's allowed-crossover span, reproduced)",
            ResultInputs.T_263.path + " (C-0167's 64 committed cells, reproduced)"
        ),
        citedInputs = mapOf(
            "C-0152 the allowed scaffold crossover's departure" to "8.57142857 deg",
            "C-0152 the allowed scaffold crossover's span" to "0.787091706 nm",
            "C-0147 the aligned span" to "0.718724283 nm",
            "C-0147 the worst-azimuth span" to "4.35327572 nm",
            "C-0187 the recommended raster's residues and lattice constant" to "[0, 10], b0 = 5",
            "C-0201 the free tile at its soft bracket end" to "0.0569815008",
            "C-0201 the free tile at its hard bracket end" to "0.166312182",
            "C-0201 the free tile at the built worst-azimuth corner" to "0.11296458",
            "C-0201 the tether tension bracket" to "0.160569993 to 3.03288672 pN",
            "C-0201 the flat count over its bracket" to "24 of 36 corners",
            "C-0201 the worst rim closure over its bracket" to "0.549926604 nm",
            "C-0167 the untied free tile at f = 0.30" to "0.0501417316",
            "C-0200 the built duplex window" to "28..125 on all 60 helices, unpaired split 12/16"
        ),
        cheapBound = cheapBound,
        anchors = anchorRows,
        datumReadings = datumRows,
        offsets = offsetRows,
        corners = cornerRows,
        uniformRasters = uniformRows,
        geometries = geometries,
        cells = cells,
        paired = paired,
        reproductions = reproductions,
        convergence = convergence,
        falsifiers = falsifiers,
        findings = findings,
        validity = listOf(
            "TRL 1-3, model-consistent and traceable, not empirically demonstrated.",
            "THE ANSWER IS A PROPERTY OF THE DRAWABLE 102 / 109 RASTER. It is determined there " +
                    "because that raster closes on caDNAno's own scaffold rule, which is what " +
                    "puts every turn at an allowed crossover. Route B does not NEED to close " +
                    "-- an unpaired base has no azimuth (C-0193 section 4) -- so a route-B " +
                    "design free to choose its row lengths has a span DISTRIBUTION and a free " +
                    "lattice phase, which is what the uniformRasters record measures.",
            "The anchor is the last PAIRED base and the loop sits OUTBOARD of it. That is " +
                    "C-0200's reading of the built block and C-0201 section 7's width " +
                    "arithmetic; the offsets record prices the other design and is not a " +
                    "tolerance on this one.",
            "This study does not read the deposited 10 x 6 file's own lattice constant, so it " +
                    "says nothing about the BUILT block's anchor azimuths -- only about the " +
                    "recommended raster and about route B's own uniform ones.",
            "Every free-tile and coupled number here inherits C-0201's element unchanged: a " +
                    "LINEARISATION about the built, taut state, one-sided, with the anchor at " +
                    "the beam axis on C-0194's frame-indifferent d/2 arm rather than at the " +
                    "phosphate radius.",
            "The lattice carries no steric floor between two duplexes and no across-helix " +
                    "parallel-axis term, k_theta is Gen1Tile's square-lattice-fitted constant, " +
                    "and the dropout statistics are measured on a single-layer Rothemund " +
                    "rectangle with only the PROFILE transferring, in nm.",
            "The coupled machinery is duplicated from T-299, whose helpers are private to that " +
                    "study. The duplication is VERIFIED and not asserted: the untied state is " +
                    "re-graded here and reproduced against C-0167's 128 committed values.",
            "Nothing here re-opens the raster, the cross-section, the placement search or the " +
                    "distribution rule."
        ),
        openQuestions = listOf(
            "The BUILT block's own anchor azimuths, which need the deposited file's lattice " +
                    "constant b0 -- a register read of the deposited 10 x 6, not a derivation.",
            "Which rim takes C-0200's 24 nt half. A free convention of that reading, stated " +
                    "here and not swept; it does not move the span, only the loop length at a " +
                    "given rim.",
            "Whether a route-B design free to choose its row lengths should choose them for the " +
                    "SPAN as well as for the scaffold budget -- the uniformRasters record shows " +
                    "the phase is a design variable nobody has spent.",
            "What a phosphate-radius attachment arm is worth, which C-0201 prices and does not " +
                    "measure and this study inherits unchanged."
        ),
        proseFailure = proseFailure
    )

    val output = File("gpd/results/T-304-raster-turn-anchor-azimuth.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digits = 9, floor = 1e-12
            ).withEmissionHeader(LatticeTag.HONEYCOMB, null) as JsonObject)
        ) + "\n"
    )
    println("T-304 - wrote " + output.path)
    check(proseFailure == "none") { proseFailure }
}
