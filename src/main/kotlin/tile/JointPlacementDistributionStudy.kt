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

import com.xemantic.nano.plentyofroom.anchoring.MeasuredBackbone
import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.DropoutEnsemble
import com.xemantic.nano.plentyofroom.coupling.InfluenceSurrogate
import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.coupling.dropoutDishingSample
import com.xemantic.nano.plentyofroom.coupling.dropoutEnsemble
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.coupling.measuredDepthIncorporation
import com.xemantic.nano.plentyofroom.coupling.oracleFloorSample
import com.xemantic.nano.plentyofroom.coupling.orderStatistic
import com.xemantic.nano.plentyofroom.coupling.perPathStiffnessCeiling
import com.xemantic.nano.plentyofroom.coupling.quantiseToLevels
import com.xemantic.nano.plentyofroom.coupling.spearmanRankCorrelation
import com.xemantic.nano.plentyofroom.coupling.summariseDropoutDishing
import com.xemantic.nano.plentyofroom.coupling.twoWayLogInteraction
import com.xemantic.nano.plentyofroom.coupling.worstSinglePathRemoval
import com.xemantic.nano.plentyofroom.electrostatics.MengMagnesium
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.DEPARTURE_SIGNIFICANT_DIGITS
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import com.xemantic.nano.plentyofroom.structure.roundedForProse
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
import kotlin.math.exp

// ---------------------------------------------------------------------------------------------
// T-323 -- the PLACEMENT and the DISTRIBUTION searched TOGETHER.
//
// C-0063 searched the placement with the distribution fixed; C-0212 searched the distribution
// with the placement fixed. Neither moved the other, so this corpus has a claim about their
// ORDERING (`which stations a coupling enters at is worth more than how its stiffness is
// distributed`) and no measurement of their INTERACTION.
//
// CLAUDE.md demands of any two-factor move a TOTAL and an INTERACTION, never an X term and a Y
// term: the split is order-dependent, and on the one prior occasion (C-0108) the interaction
// carried MORE of the variation than a main effect did.
//
// The economy is the bank. An influence bank is a property of the STRUCTURE, a distribution is a
// DIAGONAL the Woodbury system adds, and a placement is a SLICE of the bank's index set -- so one
// bank of 55 unit-point-load solves serves every placement and every distribution at that cell,
// and the joint search is an outer loop around a search that is already written (T-316).
// ---------------------------------------------------------------------------------------------

private const val T323_SAMPLES: Int = 81

private const val T323_SEARCH_SAMPLES: Int = 41

private val T323_SMOOTHING_LEVELS: List<Double> = listOf(0.3, 0.1, 0.03, 0.01)

private const val T323_SMOOTHING_ITERATIONS: Int = 12

private const val T323_POLISH_SWEEPS: Int = 2

private const val T323_SCAN_POINTS: Int = 5

private const val T323_REFINEMENTS: Int = 6

private const val T323_TOLERANCE: Double = 0.10

/**
 * `F9`'s declared tolerance: the relative departure at which the **bank slice** identity is
 * asserted. `T-329` — the residual itself has true value zero and is not emitted.
 */
private const val T323_SLICE_TOLERANCE: Double = 1e-10

/** `F10`'s declared tolerance, for the surrogate against the **assembled** solve. */
private const val T323_ASSEMBLED_TOLERANCE: Double = 1e-9

private const val T323_RIM_STANDOFF: Double = 1.0

private const val T323_RIM_BAND: Double = 6.7

/** `C-0208`'s own grading seed, so its published cells reproduce here bit for bit. */
private const val T323_GRADING_SEED: Long = 197_197L

/** `T-316`'s own training seed — the ensemble the DISTRIBUTION search sees. */
private const val T323_TRAINING_SEED: Long = 316_316L

/**
 * A third seed, for the ensemble the exhaustive PLACEMENT census selects on.
 *
 * A joint search selects a *tightest of 7 776* and then searches 50 stiffnesses on it, so it has
 * strictly more freedom to fit one stream than `T-316` had. Three disjoint seeds is what keeps
 * every quoted verdict out of sample.
 */
private const val T323_SCREENING_SEED: Long = 323_323L

private const val T323_BLOCK_EXTENT_BP: Int = 116
private const val T323_LADDER_PHASE: Int = 16
private const val T323_LADDER_OFFSET: Int = 14
private const val T323_RECOMMENDED_ONE: Int = 102
private const val T323_RECOMMENDED_TWO: Int = 109

/** `C-0205`'s own ceiling, in pN/nm — the TRANSVERSE constant every resolution is read at. */
private const val T323_SHEAR_CEILING: Double = 254.80809548301096

private const val T323_CONTACT_BP: Double = 21.0

private const val T323_PERCENTILE_SWEEPS: Int = 2

private const val T323_COARSE_PERCENTILE_SWEEPS: Int = 1

/**
 * `C-0060`'s **FLAT** ratio window, measured on `C-0058`'s square-lattice 45-station design.
 *
 * `CH-0273` establishes that this is **not** a buildability constraint — `C-0060` puts no ceiling
 * on the ratio at all — so it is named here as what its owner calls it and it is read on the
 * **two-level projection**, which is the object `C-0060` measured it on.
 */
private const val FLAT_RATIO_FLOOR: Double = 3.5

private const val FLAT_RATIO_CEILING: Double = 20.0

/** `unitZ` at a bond running through the thickness — `sqrt(3)/2`, so `unitZ^2 = 0.75`. */
private const val T323_SQRT_THREE_HALVES: Double = 0.8660254037844386

private val t323Smoke: Boolean = System.getenv("T323_SMOKE") == "1"

private val t323Realisations: Int = if (t323Smoke) 150 else 4000

private val t323TrainingRealisations: Int = if (t323Smoke) 24 else 120

private val t323CoarseTrainingRealisations: Int = if (t323Smoke) 12 else 60

private val t323ScreeningRealisations: Int = if (t323Smoke) 12 else 40

private val t323ScreeningConvergenceRealisations: Int = if (t323Smoke) 24 else 80

private val t323TopPerScreen: Int = if (t323Smoke) 2 else 6

private val t323Finalists: Int = if (t323Smoke) 1 else 3

private val t323OracleSample: Int = if (t323Smoke) 24 else 400

private val t323PlacementDescentSweeps: Int = if (t323Smoke) 2 else 4

private fun Double.emitted(digits: Int = 9): String = roundedForProse(digits).toString()

/** A ratio, a departure or any other dimensionless reading, with the pN floor removed (`P-18`). */
private fun Double.emittedDimensionless(digits: Int = 9): String =
    roundedForProse(digits, 0.0).toString()

// ------------------------------------------------------------------------------ the records

@Serializable
private class T323CheapBoundRow(
    val question: String,
    val answer: String,
    val value: Double,
    val units: String,
    val consequence: String
)

@Serializable
private class T323FamilyRow(
    val columns: Int,
    val pathCount: Int,
    val candidateStations: Int,
    val rowOptionCounts: String,
    val familySize: Long,
    val exhaustivelyEnumerated: Boolean,
    val centroSymmetricRowPairs: Int,
    val admitsCentroSymmetry: Boolean,
    val determinedPlacementLabel: String,
    val note: String
)

/** One exhaustive placement census, at one transferred rule. */
@Serializable
private class T323CensusRow(
    val columns: Int,
    val pathCount: Int,
    val distribution: String,
    val placementsEvaluated: Long?,
    val familySize: Long,
    val exhaustive: Boolean,
    val bestScreeningP90: Double,
    val medianScreeningP90: Double?,
    val worstScreeningP90: Double?,
    val spread: Double?,
    val determinedScreeningP90: Double,
    val determinedRankFromBest: Int?,
    val bestPlacementLabel: String
)

/** How well a screen RANKS the placements a searched distribution actually wants. */
@Serializable
private class T323ScreenRow(
    val screen: String,
    val placementsRanked: Long,
    val searchedSetSize: Int,
    val spearmanAgainstSearched: Double,
    val screenArgminIsSearchedArgmin: Boolean,
    val regretOfSelectingOnThisScreen: Double,
    val jointWinnerRankInThisScreen: Int?,
    val screenIsBinding: Boolean,
    val note: String
)

/** One corner of the 2 x 2 — a `(placement freedom, distribution freedom)` pair. */
@Serializable
private class T323Corner(
    val corner: String,
    val placementFreedom: String,
    val distributionFreedom: String,
    val columns: Int,
    val pathCount: Int,
    val compositeFraction: Double,
    val placementLabel: String,
    val distributionLabel: String,
    val p90OverStroke: Double,
    val nominalOverStroke: Double,
    val trainingP90: Double,
    val ratio: Double,
    val peakStiffness: Double,
    val flatAtP90: Boolean,
    val peakInsideUnzipCeiling: Boolean,
    val ratioInsideFlatWindow: Boolean,
    val uncoupledP90: Double,
    val beatsUncoupledAtP90: Boolean,
    val beatsUncoupledAtZeroDefects: Boolean,
    val worstSinglePathRemoval: Double,
    val stillFlatAfterWorstRemoval: Boolean,
    val flatAndAdmissible: Boolean
)

/** The 2 x 2, in both orderings — `countPhaseSplit` under `count = placement, phase = distribution`. */
@Serializable
private class T323SplitRow(
    val cell: String,
    val compositeFraction: Double,
    val columns: Int,
    val fixedPlacementTransferred: Double,
    val searchedPlacementTransferred: Double,
    val fixedPlacementSearched: Double,
    val searchedPlacementSearched: Double,
    val total: Double,
    val placementTermAtTransferred: Double,
    val distributionTermAtSearchedPlacement: Double,
    val distributionTermAtFixedPlacement: Double,
    val placementTermAtSearchedDistribution: Double,
    val interaction: Double,
    val interactionPerCent: Double,
    val substitutive: Boolean,
    val placementMainEffectLarger: Boolean,
    val jointBeatsBothAlone: Boolean,
    val pathDisagreementBelowTolerance: Boolean,
    val pathDisagreementTolerance: Double,
    val mapping: String
)

/** One cell of the `5 x 3` grid the two-way log fit runs on. */
@Serializable
private class T323GridRow(
    val placement: String,
    val distribution: String,
    val p90OverStroke: Double,
    val fromThisStudy: Boolean
)

@Serializable
private class T323InteractionRow(
    val grid: String,
    val rows: Int,
    val columns: Int,
    val placementSumOfSquares: Double,
    val distributionSumOfSquares: Double,
    val interactionSumOfSquares: Double,
    val totalSumOfSquares: Double,
    val interactionShare: Double,
    val placementShare: Double,
    val distributionShare: Double,
    val worstResidual: Double,
    val worstResidualPerCent: Double,
    val interactionExceedsSmallerMainEffect: Boolean
)

@Serializable
private class T323FragilityRow(
    val cell: String,
    val corner: String,
    val p90OverStroke: Double,
    val nominalOverStroke: Double,
    val worstSinglePathRemovalOverStroke: Double,
    val amplification: Double,
    val ratio: Double,
    val twoLevelRatio: Double,
    val twoLevelP90OverStroke: Double,
    val twoLevelFlatAtP90: Boolean,
    val twoLevelRatioInsideFlatWindow: Boolean
)

@Serializable
private class T323PairedRow(
    val comparison: String,
    val cell: String,
    val ratioOfPercentiles: Double,
    val medianOfPerRealisationRatio: Double,
    val realisationsWhereTheNumeratorWins: Int,
    val realisations: Int,
    val note: String
)

@Serializable
private class T323Convergence(
    val axis: String,
    val quantity: String,
    val cell: String,
    val coarse: Double,
    val fine: Double,
    val departure: Double,
    val verdictMoves: Boolean,
    val note: String
)

@Serializable
private class T323Reproduction(
    val statement: String,
    val published: Double,
    val here: Double,
    val relativeDeparture: Double,
    val source: String
)

/**
 * A numerical identity whose true value is **zero**, reported as `T-329` requires: the tolerance
 * it is asserted at and whether it holds, and **not** its residual.
 *
 * `CLAUDE.md`: *a quantity that is nothing but ulp noise must be emitted as a THRESHOLD, never as
 * a value — rounding cannot save it.* `T-323`'s first two emissions differed in exactly these two
 * quantities (`9.6E-16` against `3.8E-16`, `2.0E-14` against `3.9E-14`) and in nothing else those
 * two sentences carried, which is one field making a whole file un-diffable (`C-0216` §14(b)).
 * The field names follow `T-267`'s own `identities` block rather than coining new ones.
 */
@Serializable
private class T323Identity(
    val what: String,
    val quantity: String,
    val tolerance: Double,
    val holds: Boolean,
    val note: String
)

@Serializable
private class T323Falsifier(
    val id: String,
    val statement: String,
    val declaredOpen: Boolean,
    val fired: Boolean,
    val note: String
)

/** Everything the sweep did NOT do, with the measured reason — `CLAUDE.md`'s *no silent caps*. */
@Serializable
private class T323DroppedRow(
    val what: String,
    val why: String,
    val measured: String
)

@Serializable
private class T323Result(
    val task: String,
    val claim: String,
    val leaf: String,
    val question: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: Map<String, String>,
    val parameters: Map<String, String>,
    val sources: List<String>,
    val citedInputs: Map<String, String>,
    val cheapBound: List<T323CheapBoundRow>,
    val family: List<T323FamilyRow>,
    val placementCensus: List<T323CensusRow>,
    val screens: List<T323ScreenRow>,
    val corners: List<T323Corner>,
    val split: List<T323SplitRow>,
    val grid: List<T323GridRow>,
    val interaction: List<T323InteractionRow>,
    val fragility: List<T323FragilityRow>,
    val paired: List<T323PairedRow>,
    val verdict: Map<String, String>,
    val convergence: List<T323Convergence>,
    val identities: List<T323Identity>,
    val reproductions: List<T323Reproduction>,
    val falsifiers: List<T323Falsifier>,
    val dropped: List<T323DroppedRow>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val proseFailure: String
)

// ------------------------------------------------------------------------------ the geometry

private class T323Profile(
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, edgeX: Double, edgeY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, edgeY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T323_RIM_STANDOFF))
        )
}

private fun t323Profile(file: File): T323Profile {
    require(file.exists()) { "C-0022's result file is missing: " + file.path }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray
        .map { it.jsonObject }
        .firstOrNull { record ->
            fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at the design state")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T323Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

/** `C-0167`/`C-0208`/`C-0212`'s geometry, unchanged — the placement and the distribution move. */
private class T323Shared(val profile: T323Profile) {
    val rasterRows: Int = 10
    val helicesPerRow: Int = 6
    val rowBasePairs: Int = T323_BLOCK_EXTENT_BP
    val block: HoneycombBlock = HoneycombBlock(rasterRows, helicesPerRow)
    val d: Double = Gen1Tile.INTERHELICAL_HONEYCOMB
    val rowPitch: Double = HoneycombCrossSectionGeometry.rowPitch(d)
    val columnPitch: Double = HoneycombCrossSectionGeometry.columnPitch(d)
    val edgeX: Double = rowBasePairs * Gen1Tile.RISE_PER_BASE_PAIR
    val edgeY: Double = rasterRows * rowPitch
    val interiorPressure: Double = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)
    val pressureField: PressureField = profile.field(interiorPressure, edgeX, edgeY)
    val crossSection: String = "$rasterRows x $helicesPerRow"
    val raster: TwoLengthRaster =
        twoLengthRaster(rasterRows, helicesPerRow, T323_RECOMMENDED_ONE, T323_RECOMMENDED_TWO)
    val ladder: List<List<Double>> =
        raster.stationLattice(T323_LADDER_PHASE, T323_LADDER_OFFSET, Gen1Tile.RISE_PER_BASE_PAIR)

    fun enhancementAt(fraction: Double): Double = multiLayerRigidities(
        layers = helicesPerRow,
        interhelicalDistance = d,
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
        coupling = LayerCoupling.CALIBRATED,
        compositeFraction = fraction,
        layerSpacing = columnPitch
    ).realisedEnhancement
}

/** One `(composite fraction, radial constant, subdivisions)` — one factorisation, one bank pair. */
private class T323Tile(
    val shared: T323Shared,
    val enhancement: Double,
    val radial: Double,
    val subdivisions: Int = 1
) {
    val lattice: HoneycombGrillage = honeycombTiedLatticeAtResolvedLink(
        block = shared.block,
        rowBasePairs = shared.rowBasePairs,
        enhancement = enhancement,
        tied = true,
        transverseLinkStiffness = T323_SHEAR_CEILING,
        radialLinkStiffness = radial,
        subdivisions = subdivisions
    )

    val freeStroke: Double by lazy {
        lattice.solve(uniformPressure(shared.interiorPressure)).meanDeflection
    }

    val uncoupledDishing: Double by lazy {
        lattice.solve(shared.pressureField).peakDishing(T323_SAMPLES) / freeStroke
    }

    fun surrogate(grid: List<Pair<Double, Double>>, samples: Int = T323_SAMPLES):
            InfluenceSurrogate = honeycombTiedSurrogate(
        lattice, grid, shared.pressureField, samples
    )
}

private fun t323Distributions(
    grid: List<Pair<Double, Double>>,
    edgeX: Double,
    edgeY: Double
): List<Pair<String, List<Double>>> = listOf(
    "equal springs" to equalShareOfMandate(grid.size),
    "rim-graded 5:1" to rimGradedShareOfMandate(
        grid.map { (x, y) ->
            val onRim = abs(x) > edgeX / 2.0 - T323_RIM_BAND || abs(y) > edgeY / 2.0 - T323_RIM_BAND
            if (onRim) 5.0 else 1.0
        }
    )
)

private class T323Graded(val nominal: Double, val p90: Double, val flat: Boolean)

private fun t323Grade(
    surrogate: InfluenceSurrogate,
    stiffnesses: List<Double>,
    freeStroke: Double,
    ensemble: DropoutEnsemble
): T323Graded {
    val nominal = surrogate.solve(stiffnesses).peakDishing / freeStroke
    val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
    sample.indices.forEach { sample[it] = sample[it] / freeStroke }
    val summary = summariseDropoutDishing(
        sample, nominal, ensemble.meanSurvivors, T323_TOLERANCE
    )
    return T323Graded(nominal, summary.p90, summary.flatAtP90)
}

/** The 90th percentile alone — the screening objective, with no summary allocated around it. */
private fun t323P90(
    surrogate: InfluenceSurrogate,
    stiffnesses: List<Double>,
    freeStroke: Double,
    ensemble: DropoutEnsemble
): Double {
    val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
    sample.indices.forEach { sample[it] = sample[it] / freeStroke }
    return orderStatistic(sample, 0.90)
}

/** The dishing sample of one design, as ratios of the stroke — the paired comparison's input. */
private fun t323Sample(
    surrogate: InfluenceSurrogate,
    stiffnesses: List<Double>,
    freeStroke: Double,
    ensemble: DropoutEnsemble
): DoubleArray {
    val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
    sample.indices.forEach { sample[it] = sample[it] / freeStroke }
    return sample
}

/** `C-0167`'s four placements, unchanged — the fixed rows of the `5 x 3` grid. */
private fun t323FixedPlacements(
    shared: T323Shared,
    rootingHelixY: List<Double>,
    columns: Int
): List<Pair<String, List<Pair<Double, Double>>>> {
    val abstract = attachmentGrid(columns, shared.rasterRows, shared.edgeX, shared.edgeY)
    val determined = twoLengthSnappedGrid(
        shared.raster, columns, shared.edgeY, T323_LADDER_PHASE, T323_LADDER_OFFSET
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

/** `C-0208`'s own published cell, keyed on every dimension its sweep varied. */
private fun t323Published(
    file: File,
    radial: Double,
    fraction: Double,
    placement: String,
    columns: Int,
    distribution: String
): Double = Json.parseToJsonElement(file.readText())
    .jsonObject.getValue("cells").jsonArray.map { it.jsonObject }
    .first { record ->
        fun number(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
        fun text(name: String) = record.getValue(name).jsonPrimitive.content
        abs(number("radialLinkStiffness") - radial) < 1e-5 &&
                abs(number("compositeFraction") - fraction) < 1e-9 &&
                text("placement") == placement &&
                record.getValue("columns").jsonPrimitive.content.toInt() == columns &&
                text("distribution") == distribution
    }
    .getValue("p90OverStroke").jsonPrimitive.content.toDouble()

/** `C-0212`'s own published cell, keyed the same way — its `cells[*]` carry both distributions. */
private fun t323PublishedSearched(
    file: File,
    fraction: Double,
    placement: String,
    columns: Int,
    field: String
): Double = Json.parseToJsonElement(file.readText())
    .jsonObject.getValue("cells").jsonArray.map { it.jsonObject }
    .first { record ->
        fun number(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
        abs(number("compositeFraction") - fraction) < 1e-9 &&
                record.getValue("placement").jsonPrimitive.content == placement &&
                record.getValue("columns").jsonPrimitive.content.toInt() == columns
    }
    .getValue(field).jsonPrimitive.content.toDouble()

// ------------------------------------------------------------------------------ the study

/** What one `(composite fraction)` arm produced at the 50-path cell. */
private class T323Arm(
    val fraction: Double,
    val columns: Int,
    val corners: List<T323Corner>,
    val split: T323SplitRow,
    val inSampleSplit: T323SplitRow,
    val census: List<T323CensusRow>,
    val screens: List<T323ScreenRow>,
    val searchedPlacementUnderEqual: Double,
    val searchedPlacementUnderRim: Double,
    val jointStiffnesses: List<Double>,
    val jointSurrogate: InfluenceSurrogate,
    val jointGrading: DropoutEnsemble,
    val jointFreeStroke: Double,
    val jointLabel: String,
    val fixedSurrogate: InfluenceSurrogate,
    val fixedStiffnesses: List<Double>,
    val fixedGrading: DropoutEnsemble,
    val oracleP90Floor: Double,
    val fragility: List<T323FragilityRow>,
    val paired: List<T323PairedRow>,
    val screeningLabels: List<String>,
    val screeningEqualValues: List<Double>,
    val finalistRankInversion: Boolean,
    val finalistCount: Int,
    /** The IN-SAMPLE guarantee: on the stream the search sees, freeing the placement cannot lose. */
    val inSampleGuaranteeHolds: Boolean,
    val inSampleTransferredGain: Double,
    val inSampleSearchedGain: Double
)

@Suppress("LongMethod", "ComplexMethod", "NestedBlockDepth", "LargeClass")
fun main() {
    val startedAt = System.nanoTime()
    fun elapsedSeconds(): Long = (System.nanoTime() - startedAt) / 1_000_000_000L

    val shared = T323Shared(t323Profile(ResultInputs.T_3B.file()))
    val primaryFraction = 0.30
    val secondFraction = 0.26
    val decidingColumns = 5
    val descentColumns = listOf(1, 2, 3)
    val d = Gen1Tile.INTERHELICAL_HONEYCOMB
    val rP = MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS
    val kTheta = Gen1Tile.crossoverHingeStiffness()
    val contact = T323_CONTACT_BP * Gen1Tile.RISE_PER_BASE_PAIR

    val radial = crossoverRadialLinkBracket(
        hingeStiffness = kTheta,
        phosphateRadius = rP,
        interhelicalDistance = d,
        relaxedStep = MeasuredBackbone.STEP_SOUTH,
        stretchModulus = Gen1Tile.DUPLEX_STRETCH_MODULUS,
        equationOfState = MengMagnesium.equationOfState,
        contactLength = contact
    )
    val floorRung = radial.floor
    fun throughThickness(constant: Double): Double =
        resolvedLinkStiffness(constant, T323_SHEAR_CEILING, unitY = 0.5, unitZ = T323_SQRT_THREE_HALVES)

    val probe = T323Tile(shared, shared.enhancementAt(primaryFraction), floorRung)
    val rootingHelixY = probe.lattice.faceBeams.map { probe.lattice.beamY[it] }
    require(rootingHelixY.size == shared.rasterRows) {
        "the face carries " + rootingHelixY.size + " rooting helices and the raster has " +
                shared.rasterRows + " rows"
    }
    require(rootingHelixY.zipWithNext().all { (a, b) -> b > a }) {
        "the rooting helices must be in ascending y, one per raster row, were: $rootingHelixY"
    }
    val incorporation = measuredDepthIncorporation(shared.edgeX, shared.edgeY)
    val mandate = MANDATED_TOTAL_STIFFNESS
    val unzipCeiling = perPathStiffnessCeiling(
        Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE
    )

    val families = (descentColumns + decidingColumns).associateWith { columns ->
        JointPlacementFamily(shared.ladder, rootingHelixY, columns)
    }

    val tiles = HashMap<Pair<Double, Int>, T323Tile>()
    fun tileAt(fraction: Double, subdivisions: Int = 1): T323Tile =
        tiles.getOrPut(fraction to subdivisions) {
            T323Tile(shared, shared.enhancementAt(fraction), floorRung, subdivisions)
        }

    /** One bank pair over the whole 55-station candidate set — the economy of the whole task. */
    class T323Banks(val fraction: Double, val subdivisions: Int) {
        val tile: T323Tile = tileAt(fraction, subdivisions)
        val grading: HoneycombStationBank =
            HoneycombStationBank(tile.lattice, families.getValue(decidingColumns).stations,
                shared.pressureField, T323_SAMPLES)
        val search: HoneycombStationBank =
            HoneycombStationBank(tile.lattice, families.getValue(decidingColumns).stations,
                shared.pressureField, T323_SEARCH_SAMPLES)
    }

    val bankCache = HashMap<Pair<Double, Int>, T323Banks>()
    fun banksAt(fraction: Double, subdivisions: Int = 1): T323Banks =
        bankCache.getOrPut(fraction to subdivisions) { T323Banks(fraction, subdivisions) }

    val cheapBound = ArrayList<T323CheapBoundRow>()
    val familyRows = ArrayList<T323FamilyRow>()
    val censusRows = ArrayList<T323CensusRow>()
    val screenRows = ArrayList<T323ScreenRow>()
    val cornerRows = ArrayList<T323Corner>()
    val splitRows = ArrayList<T323SplitRow>()
    val gridRows = ArrayList<T323GridRow>()
    val interactionRows = ArrayList<T323InteractionRow>()
    val fragilityRows = ArrayList<T323FragilityRow>()
    val pairedRows = ArrayList<T323PairedRow>()
    val convergence = ArrayList<T323Convergence>()
    val identities = ArrayList<T323Identity>()
    val reproductions = ArrayList<T323Reproduction>()
    val dropped = ArrayList<T323DroppedRow>()

    // ================ Deliverable 1 -- the cheap bounds, before any solve
    println("T-323 - the cheap bounds, before any solve")
    val ladderSizes = shared.ladder.map { it.size }
    cheapBound += T323CheapBoundRow(
        question = "how many candidate stations does the determined ladder carry on the face, " +
                "at C-0148's own phase " + T323_LADDER_PHASE + " and C-0141's forced " +
                T323_LADDER_OFFSET + " bp inter-row offset?",
        answer = "the ten rooting helices carry " + ladderSizes.joinToString(", ") +
                " stations, in total",
        value = ladderSizes.sum().toDouble(),
        units = "stations",
        consequence = "the 5 bp rows are the 102 bp sense and the 6 bp rows the 109 bp one, so " +
                "the 7 bp row stagger shows up as a STATION and the family is not uniform"
    )
    families.toSortedMap().forEach { (columns, family) ->
        val determinedGrid = twoLengthSnappedGrid(
            shared.raster, columns, shared.edgeY, T323_LADDER_PHASE, T323_LADDER_OFFSET
        ).mapIndexed { index, (x, _) -> x to rootingHelixY[index / columns] }
        val determined = family.nearest(determinedGrid)
        require(determined.grid.size == determinedGrid.size)
        determined.grid.forEachIndexed { index, (s, y) ->
            require(abs(s - determinedGrid[index].first) < 1e-9) {
                "C-0167's determined placement is not a member of the searched family at " +
                        columns + " columns"
            }
            require(abs(y - determinedGrid[index].second) < 1e-9)
        }
        familyRows += T323FamilyRow(
            columns = columns,
            pathCount = family.pathCount,
            candidateStations = family.stationCount,
            rowOptionCounts = family.rowOptionCounts.joinToString(", "),
            familySize = family.size,
            exhaustivelyEnumerated = columns == decidingColumns,
            centroSymmetricRowPairs = family.centroSymmetricRowPairs,
            admitsCentroSymmetry = family.admitsCentroSymmetry,
            determinedPlacementLabel = determined.label,
            note = if (columns == decidingColumns)
                "the five-station rows are FORCED at five columns, so the family is 6^5 and it " +
                        "is enumerated exhaustively -- no descent at the deciding cell"
            else "too large to enumerate; a per-row coordinate descent, calibrated at the " +
                    "five-column cell where the exhaustive optimum is known"
        )
    }
    cheapBound += T323CheapBoundRow(
        question = "how large is the placement family at the deciding five-column cell?",
        answer = "the five-station rows are forced (C(5,5) = 1) and each six-station row has " +
                "C(6,5) = 6 choices, so the family is 6^5 and it is EXHAUSTIBLE",
        value = families.getValue(decidingColumns).size.toDouble(),
        units = "placements",
        consequence = "C-0102's *a descent compared against an exhaustive enumeration is not a " +
                "comparison* and CH-0119's *a placement-searched family measures the search* " +
                "are both removed at the cell C-0208's and C-0212's tightest readings live at"
    )
    cheapBound += T323CheapBoundRow(
        question = "does the family admit a CENTRO-SYMMETRIC member, as C-0063's square-lattice " +
                "winner was?",
        answer = "no -- row r maps to row " + (shared.rasterRows - 1) + " - r under " +
                "(s, y) -> (-s, -y), those rows carry OPPOSITE window parities, and the " +
                "intersection is empty at every one of the " + shared.rasterRows / 2 + " row pairs",
        value = families.getValue(decidingColumns).centroSymmetricRowPairs.toDouble(),
        units = "row pairs admitting any centro-symmetric station pair",
        consequence = "C-0063's entire search strategy was a centro-symmetry congruence and it " +
                "has NO analogue here: the honeycomb's forced row stagger destroys the symmetry " +
                "the square lattice's answer was built on, which is exactly why the family " +
                "being exhaustible matters"
    )
    require(families.getValue(decidingColumns).centroSymmetricRowPairs == 0) {
        "the determined family unexpectedly admits a centro-symmetric station pair"
    }
    println("  55-station ladder " + ladderSizes.joinToString(",") + ", family " +
            families.getValue(decidingColumns).size + " at " + decidingColumns + " columns, " +
            "centro-symmetric row pairs " +
            families.getValue(decidingColumns).centroSymmetricRowPairs)

    // ================ the arm: one composite fraction, at the deciding five-column cell
    val family = families.getValue(decidingColumns)
    val determinedGrid5 = twoLengthSnappedGrid(
        shared.raster, decidingColumns, shared.edgeY, T323_LADDER_PHASE, T323_LADDER_OFFSET
    ).mapIndexed { index, (x, _) -> x to rootingHelixY[index / decidingColumns] }
    val determinedMember = family.nearest(determinedGrid5)

    fun probabilitiesOf(grid: List<Pair<Double, Double>>): List<Double> =
        grid.map { (x, y) -> incorporation.at(x, y) }

    class T323TransferredCandidate(
        val slot: Int,
        val rule: Int,
        val label: String,
        val placement: JointPlacement,
        val stiffnesses: List<Double>,
        val trainingP90: Double
    )

    class T323Searched(
        val stiffnesses: List<Double>,
        val trainingObjective: Double,
        val bestTransferredTraining: Double,
        val ratio: Double
    )

    /**
     * `T-316`'s composition, unchanged — `C-0135`'s smoothed minimax on the zero-defect peak,
     * then a multi-start descent on the TRUE training percentile seeded from it and from both
     * transferred rules — run on a bank SLICE rather than on a fresh factorisation.
     */
    fun searchDistributionAt(
        banks: T323Banks,
        placement: JointPlacement,
        training: DropoutEnsemble,
        sweeps: Int,
        searchBank: HoneycombStationBank = banks.search
    ): T323Searched {
        val percentile = searchBank.surrogateFor(placement.bankIndices)
        val smooth = searchBank.multiStateFor(placement.bankIndices, "C-0022's solved collar")
        val transferred = t323Distributions(placement.grid, shared.edgeX, shared.edgeY)
        val searched = searchedStiffnessDistribution(
            smooth = smooth,
            percentile = percentile,
            training = training,
            freeStroke = banks.tile.freeStroke,
            totalStiffness = mandate,
            transferred = transferred.map { it.second },
            percentileSweeps = sweeps,
            percentileScanPoints = T323_SCAN_POINTS,
            percentileRefinements = T323_REFINEMENTS,
            smoothingLevels = T323_SMOOTHING_LEVELS,
            smoothingIterations = T323_SMOOTHING_ITERATIONS,
            polishSweeps = T323_POLISH_SWEEPS
        )
        return T323Searched(
            searched.stiffnesses, searched.trainingObjective,
            searched.bestTransferredTrainingObjective, searched.ratio
        )
    }

    fun cornerOf(
        label: String,
        placementFreedom: String,
        distributionFreedom: String,
        fraction: Double,
        columns: Int,
        placementLabel: String,
        distributionLabel: String,
        surrogate: InfluenceSurrogate,
        stiffnesses: List<Double>,
        trainingObjective: Double,
        freeStroke: Double,
        grading: DropoutEnsemble,
        uncoupled: Double
    ): T323Corner {
        val graded = t323Grade(surrogate, stiffnesses, freeStroke, grading)
        val ratio = stiffnessRatio(stiffnesses)
        val peak = stiffnesses.max()
        val removal = worstSinglePathRemoval(surrogate, stiffnesses) / freeStroke
        val inUnzip = peak < unzipCeiling
        return T323Corner(
            corner = label,
            placementFreedom = placementFreedom,
            distributionFreedom = distributionFreedom,
            columns = columns,
            pathCount = stiffnesses.size,
            compositeFraction = fraction,
            placementLabel = placementLabel,
            distributionLabel = distributionLabel,
            p90OverStroke = graded.p90,
            nominalOverStroke = graded.nominal,
            trainingP90 = trainingObjective,
            ratio = ratio,
            peakStiffness = peak,
            flatAtP90 = graded.flat,
            peakInsideUnzipCeiling = inUnzip,
            ratioInsideFlatWindow = ratio > FLAT_RATIO_FLOOR && ratio < FLAT_RATIO_CEILING,
            uncoupledP90 = uncoupled,
            beatsUncoupledAtP90 = graded.p90 < uncoupled,
            beatsUncoupledAtZeroDefects = graded.nominal < uncoupled,
            worstSinglePathRemoval = removal,
            stillFlatAfterWorstRemoval = removal < T323_TOLERANCE,
            flatAndAdmissible = graded.flat && inUnzip
        )
    }

    fun p90OfJoint(corners: List<T323Corner>): Double =
        corners.first { it.corner.startsWith("P1 D1") }.p90OverStroke

    fun runArm(fraction: Double, isPrimary: Boolean): T323Arm {
        val banks = banksAt(fraction)
        val tile = banks.tile
        val stroke = tile.freeStroke
        val cellName = "f = " + fraction.emitted(3) + ", the determined ladder on the rooting " +
                "helices, " + decidingColumns + " x " + shared.rasterRows + " = " +
                family.pathCount + " paths"

        // ---- tier 1a: the EXHAUSTIVE placement census, on the screening stream
        println("T-323 - tier 1a, exhaustive placement census at f = " + fraction.emitted(3) +
                " over " + family.size + " placements")
        val ruleNames = listOf("equal springs", "rim-graded 5:1")
        val allValues = Array(ruleNames.size) { DoubleArray(family.size.toInt()) }
        val labels = ArrayList<String>(family.size.toInt())
        val bestPerRule = arrayOfNulls<JointPlacement>(ruleNames.size)
        val bestValuePerRule = DoubleArray(ruleNames.size) { Double.POSITIVE_INFINITY }
        var index = 0
        family.enumerate().forEach { placement ->
            val surrogate = banks.search.surrogateFor(placement.bankIndices)
            val ensemble = dropoutEnsemble(
                probabilitiesOf(placement.grid), t323ScreeningRealisations, T323_SCREENING_SEED
            )
            val rules = t323Distributions(placement.grid, shared.edgeX, shared.edgeY)
            labels += placement.label
            ruleNames.indices.forEach { rule ->
                val value = t323P90(surrogate, rules[rule].second, stroke, ensemble)
                allValues[rule][index] = value
                if (bestPerRule[rule] == null || jointPlacementBetter(
                        value, placement.label, bestValuePerRule[rule], bestPerRule[rule]!!.label
                    )
                ) {
                    bestPerRule[rule] = placement
                    bestValuePerRule[rule] = value
                }
            }
            index++
            if (index % 1000 == 0) {
                println("  " + index + " / " + family.size + ", " + elapsedSeconds() + " s")
            }
        }
        val determinedIndex = labels.indexOf(determinedMember.label)
        require(determinedIndex >= 0) { "C-0167's own placement is not in the enumeration" }
        val armCensus = ruleNames.indices.map { rule ->
            val sorted = allValues[rule].clone().also { it.sort() }
            val determinedValue = allValues[rule][determinedIndex]
            T323CensusRow(
                columns = decidingColumns,
                pathCount = family.pathCount,
                distribution = ruleNames[rule],
                placementsEvaluated = family.size,
                familySize = family.size,
                exhaustive = true,
                bestScreeningP90 = sorted.first(),
                medianScreeningP90 = sorted[sorted.size / 2],
                worstScreeningP90 = sorted.last(),
                spread = sorted.last() / sorted.first(),
                determinedScreeningP90 = determinedValue,
                determinedRankFromBest =
                    allValues[rule].count { decidesBetter(it, determinedValue) } + 1,
                bestPlacementLabel = bestPerRule[rule]!!.label
            )
        }

        // ---- tier 2: the coarse joint search on the union of the screens' top K
        val topPerScreen = ruleNames.indices.map { rule ->
            allValues[rule].indices.sortedWith(
                byDecisionThenLabel({ labels[it] }, { allValues[rule][it] })
            ).take(t323TopPerScreen)
        }
        val tierTwoIndices = (topPerScreen.flatten() + determinedIndex).distinct().sorted()
        val tierTwoLabels = tierTwoIndices.map { labels[it] }.toSet()
        val byLabel = HashMap<String, JointPlacement>()
        family.enumerate().forEach { if (it.label in tierTwoLabels) byLabel[it.label] = it }
        val tierTwo = tierTwoIndices.map { byLabel.getValue(labels[it]) }
        println("T-323 - tier 2, coarse search over " + tierTwo.size + " placements, " +
                elapsedSeconds() + " s")
        val coarseTraining = tierTwo.map {
            dropoutEnsemble(
                probabilitiesOf(it.grid), t323CoarseTrainingRealisations, T323_TRAINING_SEED
            )
        }
        val coarse = tierTwo.mapIndexed { slot, placement ->
            searchDistributionAt(
                banks, placement, coarseTraining[slot], T323_COARSE_PERCENTILE_SWEEPS
            )
        }

        // ---- tier 3: the finalists, at the full composition, graded out of sample
        val finalistSlots = (coarse.indices.sortedWith(
            byDecisionThenLabel({ tierTwo[it].label }, { coarse[it].trainingObjective })
        ).take(t323Finalists) + tierTwo.indexOfFirst { it.label == determinedMember.label })
            .distinct()
        println("T-323 - tier 3, " + finalistSlots.size + " finalists, " + elapsedSeconds() + " s")
        val finalTraining = HashMap<String, DropoutEnsemble>()
        val finalGrading = HashMap<String, DropoutEnsemble>()
        fun trainingFor(placement: JointPlacement): DropoutEnsemble =
            finalTraining.getOrPut(placement.label) {
                dropoutEnsemble(
                    probabilitiesOf(placement.grid), t323TrainingRealisations, T323_TRAINING_SEED
                )
            }
        fun gradingFor(placement: JointPlacement): DropoutEnsemble =
            finalGrading.getOrPut(placement.label) {
                dropoutEnsemble(
                    probabilitiesOf(placement.grid), t323Realisations, T323_GRADING_SEED
                )
            }
        val finalists = finalistSlots.map { slot ->
            val placement = tierTwo[slot]
            placement to searchDistributionAt(
                banks, placement, trainingFor(placement), T323_PERCENTILE_SWEEPS
            )
        }
        // The joint corner is selected on the TRAINING objective and graded on the OTHER stream.
        val jointSlot = decisionArgmin(
            finalists.indices.toList(),
            { finalists[it].first.label },
            { finalists[it].second.trainingObjective }
        )
        val jointPlacement = finalists[jointSlot].first
        val jointSearched = finalists[jointSlot].second
        val fixedSearched = finalists.first { it.first.label == determinedMember.label }.second

        // ---- the four corners
        val determinedSurrogate = banks.grading.surrogateFor(determinedMember.bankIndices)
        val determinedGrading = gradingFor(determinedMember)
        val determinedRules = t323Distributions(
            determinedMember.grid, shared.edgeX, shared.edgeY
        )
        val determinedTransferred = decisionArgmin(determinedRules, { it.first }) {
            t323P90(determinedSurrogate, it.second, stroke, determinedGrading)
        }
        val jointSurrogate = banks.grading.surrogateFor(jointPlacement.bankIndices)
        val jointGrading = gradingFor(jointPlacement)
        // The SELECTION GAP: the finalists carry both their training and their grading objective,
        // so a rank inversion between the two rankings is visible rather than argued.
        val finalistGrading = finalists.map { (placement, searched) ->
            t323Grade(
                banks.grading.surrogateFor(placement.bankIndices), searched.stiffnesses,
                stroke, gradingFor(placement)
            ).p90
        }
        val gradingArgmin = decisionArgmin(
            finalistGrading.indices.toList(),
            { finalists[it].first.label },
            { finalistGrading[it] }
        )
        val rankInversion = gradingArgmin != jointSlot

        // (P1, D0): the placement searched with the distribution held at a transferred rule.
        //
        // The candidate set and the SELECTION STREAM are the joint corner's own -- the tier-2
        // set, re-ranked on the TRAINING ensemble -- so the 2 x 2 compares two FREEDOMS and not
        // two selection budgets. The exhaustive census over all 7 776 keeps its two other jobs:
        // it is the screen that produced the candidate set, and it is the placement axis's whole
        // distribution. Selecting (P1, D0) off the 40-realisation census instead would give the
        // placement freedom a selection over 7 776 candidates where the distribution freedom has
        // none, which is a difference of noise wearing a difference of freedom.
        val transferredOnTierTwo = tierTwo.flatMapIndexed { slot, placement ->
            val percentile = banks.search.surrogateFor(placement.bankIndices)
            val training = trainingFor(placement)
            t323Distributions(placement.grid, shared.edgeX, shared.edgeY)
                .mapIndexed { rule, (label, stiffnesses) ->
                    T323TransferredCandidate(
                        slot, rule, label, placement, stiffnesses,
                        t323P90(percentile, stiffnesses, stroke, training)
                    )
                }
        }
        val bestPerRuleOnTraining = ruleNames.indices.map { rule ->
            decisionArgmin(
                transferredOnTierTwo.filter { it.rule == rule },
                { it.placement.label }, { it.trainingP90 }
            )
        }
        val bestTransferredCandidate = decisionArgmin(
            transferredOnTierTwo, { it.placement.label + "|" + it.rule }, { it.trainingP90 }
        )
        val bestSearchedPlacement = bestTransferredCandidate.placement
        val bestSearchedPlacementRule = bestTransferredCandidate.label
        // The determined member is IN the tier-2 set, so this is what the composition guarantees
        // the searched placement cannot be worse than -- IN SAMPLE, which is the only place the
        // guarantee lives.
        val determinedTrainingBest = transferredOnTierTwo
            .filter { it.placement.label == determinedMember.label }.minOf { it.trainingP90 }

        val corners = listOf(
            cornerOf(
                "P0 D0 -- both fixed", "FIXED (C-0167's determined lattice on the rooting helices)",
                "TRANSFERRED (" + determinedTransferred.first + ")",
                fraction, decidingColumns, determinedMember.label, determinedTransferred.first,
                determinedSurrogate, determinedTransferred.second,
                t323P90(
                    banks.search.surrogateFor(determinedMember.bankIndices),
                    determinedTransferred.second, stroke, trainingFor(determinedMember)
                ),
                stroke, determinedGrading, tile.uncoupledDishing
            ),
            cornerOf(
                "P1 D0 -- placement searched",
                "SEARCHED (screened exhaustively over " + family.size + ", then ranked on the " +
                        "training stream over " + tierTwo.size + " candidates)",
                "TRANSFERRED (" + bestSearchedPlacementRule + ")",
                fraction, decidingColumns, bestSearchedPlacement.label,
                bestSearchedPlacementRule,
                banks.grading.surrogateFor(bestSearchedPlacement.bankIndices),
                bestTransferredCandidate.stiffnesses,
                bestTransferredCandidate.trainingP90,
                stroke, gradingFor(bestSearchedPlacement), tile.uncoupledDishing
            ),
            cornerOf(
                "P0 D1 -- distribution searched", "FIXED (C-0167's determined lattice on the " +
                        "rooting helices)", "SEARCHED",
                fraction, decidingColumns, determinedMember.label, "searched",
                determinedSurrogate, fixedSearched.stiffnesses, fixedSearched.trainingObjective,
                stroke, determinedGrading, tile.uncoupledDishing
            ),
            cornerOf(
                "P1 D1 -- JOINT", "SEARCHED (exhaustive over " + family.size + ")", "SEARCHED",
                fraction, decidingColumns, jointPlacement.label, "searched",
                jointSurrogate, jointSearched.stiffnesses, jointSearched.trainingObjective,
                stroke, jointGrading, tile.uncoupledDishing
            )
        )

        // ---- the 2 x 2, in BOTH orderings
        val p0d0 = corners[0].p90OverStroke
        val p1d0 = corners[1].p90OverStroke
        val p0d1 = corners[2].p90OverStroke
        val p1d1 = corners[3].p90OverStroke
        val raw = placementDistributionSplit(p0d0, p1d0, p0d1, p1d1)
        val armSplit = T323SplitRow(
            cell = cellName,
            compositeFraction = fraction,
            columns = decidingColumns,
            fixedPlacementTransferred = p0d0,
            searchedPlacementTransferred = p1d0,
            fixedPlacementSearched = p0d1,
            searchedPlacementSearched = p1d1,
            total = raw.total,
            placementTermAtTransferred = raw.countTermAtFromPhase,
            distributionTermAtSearchedPlacement = raw.phaseTermAtToCount,
            distributionTermAtFixedPlacement = raw.phaseTermAtFromCount,
            placementTermAtSearchedDistribution = raw.countTermAtToPhase,
            interaction = raw.interaction,
            interactionPerCent = raw.interactionPerCent,
            substitutive = raw.interaction > 0.0,
            placementMainEffectLarger =
                abs(raw.countTermAtFromPhase) > abs(raw.phaseTermAtFromCount),
            jointBeatsBothAlone = p1d1 < p1d0 && p1d1 < p0d1,
            pathDisagreementBelowTolerance = raw.pathDisagreement < 1e-12,
            pathDisagreementTolerance = 1e-12,
            mapping = "countPhaseSplit REUSED UNCHANGED under count = PLACEMENT, " +
                    "phase = DISTRIBUTION; the arithmetic is not written twice"
        )

        // ---- fragility and the two-level projection, at every corner of the 2 x 2
        val ingredients = listOf(
            Triple(determinedSurrogate, determinedTransferred.second, determinedGrading),
            Triple(
                banks.grading.surrogateFor(bestSearchedPlacement.bankIndices),
                bestTransferredCandidate.stiffnesses, gradingFor(bestSearchedPlacement)
            ),
            Triple(determinedSurrogate, fixedSearched.stiffnesses, determinedGrading),
            Triple(jointSurrogate, jointSearched.stiffnesses, jointGrading)
        )
        val armFragility = corners.indices.map { slot ->
            val (surrogate, stiffnesses, grading) = ingredients[slot]
            val graded = t323Grade(surrogate, stiffnesses, stroke, grading)
            val removal = worstSinglePathRemoval(surrogate, stiffnesses) / stroke
            val twoLevel = quantiseToLevels(stiffnesses, 2, mandate)
            val twoLevelGraded = t323Grade(surrogate, twoLevel, stroke, grading)
            val twoLevelRatio = stiffnessRatio(twoLevel)
            T323FragilityRow(
                cell = cellName,
                corner = corners[slot].corner,
                p90OverStroke = graded.p90,
                nominalOverStroke = graded.nominal,
                worstSinglePathRemovalOverStroke = removal,
                amplification = removal / graded.nominal,
                ratio = stiffnessRatio(stiffnesses),
                twoLevelRatio = twoLevelRatio,
                twoLevelP90OverStroke = twoLevelGraded.p90,
                twoLevelFlatAtP90 = twoLevelGraded.flat,
                twoLevelRatioInsideFlatWindow =
                    twoLevelRatio > FLAT_RATIO_FLOOR && twoLevelRatio < FLAT_RATIO_CEILING
            )
        }

        // ---- the PAIRED readings, per realisation and not between two summaries
        val jointSample = t323Sample(jointSurrogate, jointSearched.stiffnesses, stroke, jointGrading)
        val armPaired = listOf(0, 1, 2).map { slot ->
            val (surrogate, stiffnesses, grading) = ingredients[slot]
            val other = t323Sample(surrogate, stiffnesses, stroke, grading)
            T323PairedRow(
                comparison = "the JOINT corner against " + corners[slot].corner,
                cell = cellName,
                ratioOfPercentiles = corners[slot].p90OverStroke / p90OfJoint(corners),
                medianOfPerRealisationRatio = pairedMedianRatio(other, jointSample),
                realisationsWhereTheNumeratorWins =
                    other.indices.count { other[it] < jointSample[it] },
                realisations = jointSample.size,
                note = "one seed and one path count, so DropoutRandom hands both designs the " +
                        "SAME uniform stream and the comparison is paired by construction; a " +
                        "ratio of two order statistics is not the order statistic of the ratio"
            )
        }

        // ---- the SAME 2 x 2, taken IN SAMPLE, so the selection noise is separable
        //
        // Out of sample every corner but (P0, D0) carries a selection, and the placement freedom
        // carries a bigger one than the distribution freedom does. In sample no corner carries
        // any, so the difference between the two interactions IS what the selection costs -- a
        // measurement rather than a caveat.
        val i00 = determinedTrainingBest
        val i10 = bestTransferredCandidate.trainingP90
        val i01 = fixedSearched.trainingObjective
        val i11 = jointSearched.trainingObjective
        val rawInSample = placementDistributionSplit(i00, i10, i01, i11)
        val armInSampleSplit = T323SplitRow(
            cell = cellName + " [IN SAMPLE, on the " + T323_TRAINING_SEED + " stream]",
            compositeFraction = fraction,
            columns = decidingColumns,
            fixedPlacementTransferred = i00,
            searchedPlacementTransferred = i10,
            fixedPlacementSearched = i01,
            searchedPlacementSearched = i11,
            total = rawInSample.total,
            placementTermAtTransferred = rawInSample.countTermAtFromPhase,
            distributionTermAtSearchedPlacement = rawInSample.phaseTermAtToCount,
            distributionTermAtFixedPlacement = rawInSample.phaseTermAtFromCount,
            placementTermAtSearchedDistribution = rawInSample.countTermAtToPhase,
            interaction = rawInSample.interaction,
            interactionPerCent = rawInSample.interactionPerCent,
            substitutive = rawInSample.interaction > 0.0,
            placementMainEffectLarger = abs(rawInSample.countTermAtFromPhase) >
                    abs(rawInSample.phaseTermAtFromCount),
            jointBeatsBothAlone = i11 < i10 && i11 < i01,
            pathDisagreementBelowTolerance = rawInSample.pathDisagreement < 1e-12,
            pathDisagreementTolerance = 1e-12,
            mapping = "the SAME arithmetic on the SAME four freedoms, read on the stream the " +
                    "search minimises rather than on the one the verdict is taken on"
        )

        // ---- the screens' own quality
        val tierTwoSearched = coarse.map { it.trainingObjective }
        val bestTierTwo = tierTwoSearched.min()
        val jointRankIn = ruleNames.indices.map { rule ->
            val jointValue = allValues[rule][labels.indexOf(jointPlacement.label)]
            allValues[rule].count { decidesBetter(it, jointValue) } + 1
        }
        val armScreens = ruleNames.indices.map { rule ->
            val screenValues = tierTwoIndices.map { allValues[rule][it] }
            val screenArgmin = tierTwo[decisionArgmin(
                screenValues.indices.toList(), { tierTwo[it].label }, { screenValues[it] }
            )]
            val screenArgminSearched = tierTwoSearched[tierTwo.indexOfFirst {
                it.label == screenArgmin.label
            }]
            T323ScreenRow(
                screen = ruleNames[rule],
                placementsRanked = family.size,
                searchedSetSize = tierTwo.size,
                spearmanAgainstSearched = spearmanRankCorrelation(screenValues, tierTwoSearched),
                screenArgminIsSearchedArgmin = screenArgmin.label == tierTwo[
                    decisionArgmin(
                        tierTwoSearched.indices.toList(),
                        { tierTwo[it].label }, { tierTwoSearched[it] }
                    )
                ].label,
                regretOfSelectingOnThisScreen = screenArgminSearched / bestTierTwo,
                jointWinnerRankInThisScreen = jointRankIn[rule],
                screenIsBinding = jointRankIn[rule] == t323TopPerScreen,
                note = "the Spearman is over the searched set, against the TRAINING percentile " +
                        "the distribution search actually minimises"
            )
        }

        // ---- tier 1b: the oracle floor, on a deterministic sample, as a THIRD screen
        val stride = maxOf(1, (family.size / t323OracleSample).toInt())
        val sampleIndices = (0 until family.size.toInt() step stride).take(t323OracleSample)
        val sampleSet = sampleIndices.map { labels[it] }.toSet()
        val floorValues = ArrayList<Double>()
        val equalOnSample = ArrayList<Double>()
        family.enumerate().forEach { placement ->
            if (placement.label in sampleSet) {
                val surrogate = banks.search.surrogateFor(placement.bankIndices)
                val ensemble = dropoutEnsemble(
                    probabilitiesOf(placement.grid), t323ScreeningRealisations, T323_SCREENING_SEED
                )
                val floors = oracleFloorSample(surrogate, ensemble)
                floors.indices.forEach { floors[it] = floors[it] / stroke }
                floorValues += orderStatistic(floors, 0.90)
                equalOnSample += allValues[0][labels.indexOf(placement.label)]
            }
        }
        val floorOnTierTwo = tierTwo.map { placement ->
            val surrogate = banks.search.surrogateFor(placement.bankIndices)
            val ensemble = dropoutEnsemble(
                probabilitiesOf(placement.grid), t323ScreeningRealisations, T323_SCREENING_SEED
            )
            val floors = oracleFloorSample(surrogate, ensemble)
            floors.indices.forEach { floors[it] = floors[it] / stroke }
            orderStatistic(floors, 0.90)
        }
        val floorArgmin = tierTwo[decisionArgmin(
            floorOnTierTwo.indices.toList(), { tierTwo[it].label }, { floorOnTierTwo[it] }
        )]
        val armScreensWithFloor = armScreens + T323ScreenRow(
            screen = "the oracle floor (distribution-free)",
            placementsRanked = sampleIndices.size.toLong(),
            searchedSetSize = tierTwo.size,
            spearmanAgainstSearched = spearmanRankCorrelation(floorOnTierTwo, tierTwoSearched),
            screenArgminIsSearchedArgmin = floorArgmin.label == tierTwo[
                decisionArgmin(
                    tierTwoSearched.indices.toList(),
                    { tierTwo[it].label }, { tierTwoSearched[it] }
                )
            ].label,
            regretOfSelectingOnThisScreen =
                tierTwoSearched[tierTwo.indexOfFirst { it.label == floorArgmin.label }] /
                        bestTierTwo,
            jointWinnerRankInThisScreen = null,
            screenIsBinding = false,
            note = "a POINTWISE lower bound over every distribution whatever, so it ranks " +
                    "PLACEMENTS by their potential rather than by one rule's reading of them; " +
                    "measured over a deterministic sample of " + sampleIndices.size +
                    " placements, against the equal-spring screen at Spearman " +
                    spearmanRankCorrelation(floorValues, equalOnSample).emittedDimensionless(4)
        )

        if (isPrimary) {
            cheapBound += T323CheapBoundRow(
                question = "how wide is the placement axis the corpus has never swept, on the " +
                        "screening stream at the deciding cell?",
                answer = "the exhaustive census over " + family.size + " placements at " +
                        armCensus[0].distribution + " runs " +
                        armCensus[0].bestScreeningP90.emittedDimensionless(9) + " to " +
                        armCensus[0].worstScreeningP90!!.emittedDimensionless(9),
                value = armCensus[0].spread!!,
                units = "worst over best, dimensionless",
                consequence = "a placement is a DESIGN and not a lottery exactly to the extent " +
                        "this spread exceeds the distribution axis C-0212 measured at " +
                        "1.10434917 to 1.70065256"
            )
        }
        return T323Arm(
            fraction = fraction,
            columns = decidingColumns,
            corners = corners,
            split = armSplit,
            inSampleSplit = armInSampleSplit,
            census = armCensus,
            screens = armScreensWithFloor,
            searchedPlacementUnderEqual = t323Grade(
                banks.grading.surrogateFor(bestPerRuleOnTraining[0].placement.bankIndices),
                bestPerRuleOnTraining[0].stiffnesses, stroke,
                gradingFor(bestPerRuleOnTraining[0].placement)
            ).p90,
            searchedPlacementUnderRim = t323Grade(
                banks.grading.surrogateFor(bestPerRuleOnTraining[1].placement.bankIndices),
                bestPerRuleOnTraining[1].stiffnesses, stroke,
                gradingFor(bestPerRuleOnTraining[1].placement)
            ).p90,
            jointStiffnesses = jointSearched.stiffnesses,
            jointSurrogate = jointSurrogate,
            jointGrading = jointGrading,
            jointFreeStroke = stroke,
            jointLabel = jointPlacement.label,
            fixedSurrogate = determinedSurrogate,
            fixedStiffnesses = fixedSearched.stiffnesses,
            fixedGrading = determinedGrading,
            oracleP90Floor = floorOnTierTwo.min(),
            fragility = armFragility,
            paired = armPaired,
            screeningLabels = labels.toList(),
            screeningEqualValues = allValues[0].toList(),
            finalistRankInversion = rankInversion,
            finalistCount = finalists.size,
            inSampleGuaranteeHolds =
                bestTransferredCandidate.trainingP90 < determinedTrainingBest * (1.0 + 1e-12) &&
                        jointSearched.trainingObjective <
                        fixedSearched.trainingObjective * (1.0 + 1e-12),
            inSampleTransferredGain =
                determinedTrainingBest / bestTransferredCandidate.trainingP90,
            inSampleSearchedGain =
                fixedSearched.trainingObjective / jointSearched.trainingObjective
        )
    }

    // ================ Deliverables 2-4 -- the primary arm
    val primary = runArm(primaryFraction, isPrimary = true)
    cornerRows += primary.corners
    censusRows += primary.census
    screenRows += primary.screens
    splitRows += primary.split
    splitRows += primary.inSampleSplit
    val primaryElapsed = elapsedSeconds()
    println("T-323 - the primary arm is done at " + primaryElapsed + " s")

    // ================ Deliverable 4b -- the 5 x 3 grid and its two-way log fit
    println("T-323 - the 5 x 3 grid")
    val banks = banksAt(primaryFraction)
    val tile = banks.tile
    val stroke = tile.freeStroke
    val fixedPlacements = t323FixedPlacements(shared, rootingHelixY, decidingColumns)
    val gridColumnNames = listOf("equal springs", "rim-graded 5:1", "searched")
    val gridValues = ArrayList<List<Double>>()
    val t310 = ResultInputs.T_310.file()
    val t316 = ResultInputs.T_316.file()
    fixedPlacements.forEach { (name, placementGrid) ->
        val surrogate = tile.surrogate(placementGrid, T323_SAMPLES)
        val searchSurrogate = tile.surrogate(placementGrid, T323_SEARCH_SAMPLES)
        val probabilities = probabilitiesOf(placementGrid)
        val grading = dropoutEnsemble(probabilities, t323Realisations, T323_GRADING_SEED)
        val training = dropoutEnsemble(probabilities, t323TrainingRealisations, T323_TRAINING_SEED)
        val rules = t323Distributions(placementGrid, shared.edgeX, shared.edgeY)
        val transferred = rules.map { (label, k) ->
            label to t323Grade(surrogate, k, stroke, grading).p90
        }
        val multi = honeycombMultiStateSurrogate(
            tile.lattice, placementGrid,
            singleLoadState("C-0022's solved collar", shared.pressureField), T323_SEARCH_SAMPLES
        )
        val searched = searchedStiffnessDistribution(
            smooth = multi, percentile = searchSurrogate, training = training,
            freeStroke = stroke, totalStiffness = mandate,
            transferred = rules.map { it.second }, percentileSweeps = T323_PERCENTILE_SWEEPS,
            percentileScanPoints = T323_SCAN_POINTS, percentileRefinements = T323_REFINEMENTS,
            smoothingLevels = T323_SMOOTHING_LEVELS,
            smoothingIterations = T323_SMOOTHING_ITERATIONS, polishSweeps = T323_POLISH_SWEEPS
        )
        val searchedP90 = t323Grade(surrogate, searched.stiffnesses, stroke, grading).p90
        gridValues += listOf(transferred[0].second, transferred[1].second, searchedP90)
        listOf(0 to "equal springs", 1 to "rim-graded 5:1").forEach { (slot, rule) ->
            gridRows += T323GridRow(name, rule, transferred[slot].second, true)
            val published = t323Published(
                t310, floorRung, primaryFraction, name, decidingColumns, rule
            )
            reproductions += T323Reproduction(
                statement = "C-0208's published p90 at " + name + ", " + rule,
                published = published, here = transferred[slot].second,
                relativeDeparture = abs(transferred[slot].second - published) / abs(published),
                source = ResultInputs.T_310.path
            )
        }
        gridRows += T323GridRow(name, "searched", searchedP90, true)
        val publishedSearched = t323PublishedSearched(
            t316, primaryFraction, name, decidingColumns, "searchedP90"
        )
        reproductions += T323Reproduction(
            statement = "C-0212's published SEARCHED p90 at " + name,
            published = publishedSearched, here = searchedP90,
            relativeDeparture = abs(searchedP90 - publishedSearched) / abs(publishedSearched),
            source = ResultInputs.T_316.path
        )
        println("  " + name.take(48) + "  done, " + elapsedSeconds() + " s")
    }
    val searchedRow = listOf(
        primary.searchedPlacementUnderEqual,
        primary.searchedPlacementUnderRim,
        primary.corners[3].p90OverStroke
    )
    gridColumnNames.forEachIndexed { slot, rule ->
        gridRows += T323GridRow("SEARCHED over the determined family", rule, searchedRow[slot], true)
    }
    gridValues += searchedRow
    val twoWay = twoWayLogInteraction(gridValues)
    val placementShare = twoWay.rowSumOfSquares / twoWay.totalSumOfSquares
    val distributionShare = twoWay.columnSumOfSquares / twoWay.totalSumOfSquares
    interactionRows += T323InteractionRow(
        grid = "5 placements (C-0167's four, and SEARCHED) x 3 distributions " +
                "(equal, rim-graded 5:1, searched), at f = " + primaryFraction.emitted(3) +
                " and " + decidingColumns + " columns",
        rows = twoWay.rows,
        columns = twoWay.columns,
        placementSumOfSquares = twoWay.rowSumOfSquares,
        distributionSumOfSquares = twoWay.columnSumOfSquares,
        interactionSumOfSquares = twoWay.interactionSumOfSquares,
        totalSumOfSquares = twoWay.totalSumOfSquares,
        interactionShare = twoWay.interactionShare,
        placementShare = placementShare,
        distributionShare = distributionShare,
        worstResidual = twoWay.worstResidual,
        worstResidualPerCent = twoWay.worstResidualPerCent,
        interactionExceedsSmallerMainEffect =
            twoWay.interactionShare > minOf(placementShare, distributionShare)
    )

    // ================ Deliverable 5 -- the other column counts, by descent
    println("T-323 - the other column counts, by per-row descent")
    descentColumns.forEach { columns ->
        val columnFamily = families.getValue(columns)
        val columnDetermined = columnFamily.nearest(
            twoLengthSnappedGrid(
                shared.raster, columns, shared.edgeY, T323_LADDER_PHASE, T323_LADDER_OFFSET
            ).mapIndexed { slot, (x, _) -> x to rootingHelixY[slot / columns] }
        )
        val columnBank = HoneycombStationBank(
            tile.lattice, columnFamily.stations, shared.pressureField, T323_SEARCH_SAMPLES
        )
        val columnGradingBank = HoneycombStationBank(
            tile.lattice, columnFamily.stations, shared.pressureField, T323_SAMPLES
        )
        fun screenObjective(rule: Int): (JointPlacement) -> Double = { placement ->
            val surrogate = columnBank.surrogateFor(placement.bankIndices)
            val ensemble = dropoutEnsemble(
                probabilitiesOf(placement.grid), t323ScreeningRealisations, T323_SCREENING_SEED
            )
            val stiffnesses =
                t323Distributions(placement.grid, shared.edgeX, shared.edgeY)[rule].second
            t323P90(surrogate, stiffnesses, stroke, ensemble)
        }
        val starts = listOf(
            columnDetermined,
            columnFamily.enumerate().first(),
            columnFamily.placementAt(columnFamily.rowOptions.map { it.last() }),
            columnFamily.placementAt(
                columnFamily.rowOptions.mapIndexed { row, options ->
                    options[(row * 3) % options.size]
                }
            )
        ).distinctBy { it.label }
        val bestByRule = (0..1).map { rule ->
            descendJointPlacement(
                columnDetermined, t323PlacementDescentSweeps, starts, screenObjective(rule)
            ) to rule
        }
        val chosen = decisionArgmin(
            bestByRule, { (placement, rule) -> placement.label + "|" + rule }
        ) { (placement, rule) -> screenObjective(rule)(placement) }
        censusRows += T323CensusRow(
            columns = columns,
            pathCount = columnFamily.pathCount,
            distribution = "the best of " + gridColumnNames[0] + " and " + gridColumnNames[1],
            placementsEvaluated = null,
            familySize = columnFamily.size,
            exhaustive = false,
            bestScreeningP90 = screenObjective(chosen.second)(chosen.first),
            medianScreeningP90 = null,
            worstScreeningP90 = null,
            spread = null,
            determinedScreeningP90 = screenObjective(chosen.second)(columnDetermined),
            determinedRankFromBest = null,
            bestPlacementLabel = chosen.first.label
        )
        listOf(
            "P0 D1 -- distribution searched" to columnDetermined,
            "P1 D1 -- JOINT" to chosen.first
        ).forEach { (label, placement) ->
            val training = dropoutEnsemble(
                probabilitiesOf(placement.grid), t323TrainingRealisations, T323_TRAINING_SEED
            )
            val grading = dropoutEnsemble(
                probabilitiesOf(placement.grid), t323Realisations, T323_GRADING_SEED
            )
            val percentile = columnBank.surrogateFor(placement.bankIndices)
            val smooth = columnBank.multiStateFor(
                placement.bankIndices, "C-0022's solved collar"
            )
            val rules = t323Distributions(placement.grid, shared.edgeX, shared.edgeY)
            val searched = searchedStiffnessDistribution(
                smooth = smooth, percentile = percentile, training = training,
                freeStroke = stroke, totalStiffness = mandate,
                transferred = rules.map { it.second }, percentileSweeps = T323_PERCENTILE_SWEEPS,
                percentileScanPoints = T323_SCAN_POINTS, percentileRefinements = T323_REFINEMENTS,
                smoothingLevels = T323_SMOOTHING_LEVELS,
                smoothingIterations = T323_SMOOTHING_ITERATIONS, polishSweeps = T323_POLISH_SWEEPS
            )
            val surrogate = columnGradingBank.surrogateFor(placement.bankIndices)
            val transferredBest = decisionArgmin(rules, { it.first }) {
                t323P90(surrogate, it.second, stroke, grading)
            }
            if (label.startsWith("P0 D1")) {
                cornerRows += cornerOf(
                    "P0 D0 -- both fixed", "FIXED (C-0167's determined lattice on the rooting " +
                            "helices)", "TRANSFERRED (" + transferredBest.first + ")",
                    primaryFraction, columns, placement.label, transferredBest.first,
                    surrogate, transferredBest.second,
                    t323P90(percentile, transferredBest.second, stroke, training),
                    stroke, grading, tile.uncoupledDishing
                )
            }
            cornerRows += cornerOf(
                label,
                if (label.startsWith("P0")) "FIXED (C-0167's determined lattice on the rooting " +
                        "helices)" else "SEARCHED (per-row descent over " +
                        columnFamily.size + ")",
                "SEARCHED", primaryFraction, columns, placement.label, "searched",
                surrogate, searched.stiffnesses, searched.trainingObjective,
                stroke, grading, tile.uncoupledDishing
            )
        }
        println("  " + columns + " columns done, " + elapsedSeconds() + " s")
    }
    fragilityRows += primary.fragility
    pairedRows += primary.paired

    // ================ Deliverable 7 -- F21: the descent, calibrated where the truth is known
    println("T-323 - F21, the descent against the exhaustive optimum at the deciding cell")
    val calibrationScreen: (JointPlacement) -> Double = { placement ->
        val surrogate = banks.search.surrogateFor(placement.bankIndices)
        val ensemble = dropoutEnsemble(
            probabilitiesOf(placement.grid), t323ScreeningRealisations, T323_SCREENING_SEED
        )
        t323P90(
            surrogate,
            t323Distributions(placement.grid, shared.edgeX, shared.edgeY)[0].second,
            stroke, ensemble
        )
    }
    val calibrationStarts = listOf(
        determinedMember,
        family.enumerate().first(),
        family.placementAt(family.rowOptions.map { it.last() }),
        family.placementAt(
            family.rowOptions.mapIndexed { row, options -> options[(row * 3) % options.size] }
        )
    ).distinctBy { it.label }
    val descended = descendJointPlacement(
        determinedMember, t323PlacementDescentSweeps, calibrationStarts, calibrationScreen
    )
    val descendedValue = calibrationScreen(descended)
    val exhaustiveValue = primary.census[0].bestScreeningP90
    val descentSlack = descendedValue / exhaustiveValue
    convergence += T323Convergence(
        axis = "the per-row placement DESCENT against the EXHAUSTIVE optimum, at the deciding " +
                "five-column cell where both exist",
        quantity = "the equal-spring screening p90 of the placement each returns",
        cell = "f = " + primaryFraction.emitted(3) + ", " + decidingColumns + " columns",
        coarse = descendedValue,
        fine = exhaustiveValue,
        departure = descentSlack - 1.0,
        verdictMoves = descended.label != primary.census[0].bestPlacementLabel,
        note = "the instrument used at 10, 20 and 30 paths, quoted with a MEASURED slack rather " +
                "than as an answer -- C-0102 and CH-0119"
    )

    // ================ Deliverable 7 -- the convergence axes, at the deciding cell
    println("T-323 - the convergence axes")
    val jointPlacementOfPrimary = family.enumerate().first { it.label == primary.jointLabel }
    val jointTraining = dropoutEnsemble(
        probabilitiesOf(jointPlacementOfPrimary.grid), t323TrainingRealisations, T323_TRAINING_SEED
    )
    val jointGradingEnsemble = primary.jointGrading
    val jointBase = primary.corners[3].p90OverStroke

    fun searchAndGrade(
        useBanks: T323Banks,
        placement: JointPlacement,
        training: DropoutEnsemble,
        sweeps: Int,
        searchBank: HoneycombStationBank
    ): Double {
        val searched = searchDistributionAt(useBanks, placement, training, sweeps, searchBank)
        return t323Grade(
            useBanks.grading.surrogateFor(placement.bankIndices),
            searched.stiffnesses, useBanks.tile.freeStroke, jointGradingEnsemble
        ).p90
    }

    // 1 -- the SEARCH grid, 41 against 81
    val atEightyOne = searchAndGrade(
        banks, jointPlacementOfPrimary, jointTraining, T323_PERCENTILE_SWEEPS, banks.grading
    )
    convergence += T323Convergence(
        axis = "the SEARCH's own dishing grid, " + T323_SEARCH_SAMPLES + " against " +
                T323_SAMPLES,
        quantity = "the joint corner's out-of-sample p90",
        cell = primary.split.cell, coarse = jointBase, fine = atEightyOne,
        departure = abs(atEightyOne - jointBase) / abs(jointBase),
        verdictMoves = (jointBase < T323_TOLERANCE) != (atEightyOne < T323_TOLERANCE),
        note = "the grading grid is 81 at both readings; only the grid the DESCENT samples moves"
    )

    // 2 -- the GRADING grid, 81 against 41 and 161
    listOf(41, 161).forEach { samples ->
        val other = honeycombTiedSurrogate(
            tile.lattice, jointPlacementOfPrimary.grid, shared.pressureField, samples
        )
        val value = t323Grade(
            other, primary.jointStiffnesses, stroke, jointGradingEnsemble
        ).p90
        convergence += T323Convergence(
            axis = "the VERDICT's dishing grid, " + T323_SAMPLES + " against " + samples,
            quantity = "the joint corner's out-of-sample p90",
            cell = primary.split.cell, coarse = jointBase, fine = value,
            departure = abs(value - jointBase) / abs(jointBase),
            verdictMoves = (jointBase < T323_TOLERANCE) != (value < T323_TOLERANCE),
            note = "the searched distribution is held fixed and only the grid it is read on moves"
        )
    }

    // 3 -- the TRAINING realisations the distribution search sees
    listOf(t323TrainingRealisations / 2, t323TrainingRealisations * 2).forEach { count ->
        val other = dropoutEnsemble(
            probabilitiesOf(jointPlacementOfPrimary.grid), count, T323_TRAINING_SEED
        )
        val value = searchAndGrade(
            banks, jointPlacementOfPrimary, other, T323_PERCENTILE_SWEEPS, banks.search
        )
        convergence += T323Convergence(
            axis = "the TRAINING realisations the distribution search sees, " +
                    t323TrainingRealisations + " against " + count,
            quantity = "the joint corner's out-of-sample p90",
            cell = primary.split.cell, coarse = jointBase, fine = value,
            departure = abs(value - jointBase) / abs(jointBase),
            verdictMoves = (jointBase < T323_TOLERANCE) != (value < T323_TOLERANCE),
            note = "the placement is held at the joint winner and the grading stream is unchanged"
        )
    }

    // 4 -- the percentile descent's sweeps
    run {
        val value = searchAndGrade(
            banks, jointPlacementOfPrimary, jointTraining, T323_PERCENTILE_SWEEPS + 1, banks.search
        )
        convergence += T323Convergence(
            axis = "the percentile descent's sweeps, " + T323_PERCENTILE_SWEEPS + " against " +
                    (T323_PERCENTILE_SWEEPS + 1),
            quantity = "the joint corner's out-of-sample p90",
            cell = primary.split.cell, coarse = jointBase, fine = value,
            departure = abs(value - jointBase) / abs(jointBase),
            verdictMoves = (jointBase < T323_TOLERANCE) != (value < T323_TOLERANCE),
            note = "how much the distribution search had left at the joint placement"
        )
    }

    // 5 -- the SCREENING realisations the exhaustive census ranks on
    println("T-323 - the screening-ensemble convergence axis, a second exhaustive census")
    val coarseTopSet: MutableList<Pair<String, Double>> = ArrayList()
    family.enumerate().forEach { placement ->
        val surrogate = banks.search.surrogateFor(placement.bankIndices)
        val ensemble = dropoutEnsemble(
            probabilitiesOf(placement.grid), t323ScreeningConvergenceRealisations,
            T323_SCREENING_SEED
        )
        coarseTopSet += placement.label to t323P90(
            surrogate,
            t323Distributions(placement.grid, shared.edgeX, shared.edgeY)[0].second,
            stroke, ensemble
        )
    }
    val topAtEighty = coarseTopSet
        .sortedWith(byDecisionThenLabel({ it.first }, { it.second }))
        .take(t323TopPerScreen).map { it.first }.toSet()
    // The 40-realisation ranking is the census tier 1a already took; it is not re-solved.
    val topAtForty = primary.screeningLabels.zip(primary.screeningEqualValues)
        .sortedWith(byDecisionThenLabel({ it.first }, { it.second }))
        .take(t323TopPerScreen).map { it.first }.toSet()
    val bestAtEighty = decisionArgmin(coarseTopSet, { it.first }, { it.second })
    convergence += T323Convergence(
        axis = "the SCREENING realisations the exhaustive census ranks on, " +
                t323ScreeningRealisations + " against " + t323ScreeningConvergenceRealisations,
        quantity = "the equal-spring screening p90 of the census's own argmin",
        cell = "f = " + primaryFraction.emitted(3) + ", " + decidingColumns + " columns",
        coarse = primary.census[0].bestScreeningP90,
        fine = bestAtEighty.second,
        departure = abs(bestAtEighty.second - primary.census[0].bestScreeningP90) /
                abs(primary.census[0].bestScreeningP90),
        verdictMoves = topAtEighty != topAtForty,
        note = "the top-" + t323TopPerScreen + " SET is compared as a set, because that is what " +
                "tier 2 selects on; the argmin is " +
                (if (bestAtEighty.first == primary.census[0].bestPlacementLabel) "unchanged"
                else "different") + " at the two counts"
    )

    // 6 -- beam subdivisions
    println("T-323 - the beam-subdivision axis")
    val fineBanks = banksAt(primaryFraction, 2)
    val fineValue = searchAndGrade(
        fineBanks, jointPlacementOfPrimary,
        dropoutEnsemble(
            probabilitiesOf(jointPlacementOfPrimary.grid), t323TrainingRealisations,
            T323_TRAINING_SEED
        ),
        T323_PERCENTILE_SWEEPS, fineBanks.search
    )
    convergence += T323Convergence(
        axis = "beam subdivisions, 1 against 2",
        quantity = "the joint corner's out-of-sample p90",
        cell = primary.split.cell, coarse = jointBase, fine = fineValue,
        departure = abs(fineValue - jointBase) / abs(jointBase),
        verdictMoves = (jointBase < T323_TOLERANCE) != (fineValue < T323_TOLERANCE),
        note = "C-0205 records that a threshold's VALUE and its VERDICT converge at different " +
                "rates, so the axis is taken on the deciding quantity at the deciding cell"
    )

    // ================ the standing falsifiers, asserted on the objects the study uses
    println("T-323 - the standing falsifiers")
    val uniformDishing =
        tile.lattice.solve(uniformPressure(shared.interiorPressure)).peakDishing(T323_SAMPLES) /
                stroke
    val standingLattice =
        HoneycombGrillage(shared.block, shared.rowBasePairs, Gen1Tile.FOUNDATION_SECANT)
    val defaultLattice = HoneycombGrillage(
        shared.block, shared.rowBasePairs, Gen1Tile.FOUNDATION_SECANT, radialLinkStiffness = null
    )
    val standingLoad = standingLattice.assembleLoad(uniformPressure(0.01))
    val defaultedLoad = defaultLattice.assembleLoad(uniformPressure(0.01))
    var loadIdentical =
        standingLattice.degreesOfFreedom == defaultLattice.degreesOfFreedom
    for (i in 0 until standingLattice.degreesOfFreedom) {
        if (standingLoad[i] != defaultedLoad[i]) loadIdentical = false
    }
    val siteSetsAgree =
        standingLattice.bonds.map { it.site } == defaultLattice.bonds.map { it.site }
    // The surrogate at full presence against the ASSEMBLED solve, on the SEARCHED distribution.
    val jointResponse = primary.jointSurrogate.solve(primary.jointStiffnesses)
    val assembled = tile.lattice.solve(
        shared.pressureField,
        jointPlacementOfPrimary.grid.mapIndexed { slot, (s, y) ->
            com.xemantic.nano.plentyofroom.structure.PointLoad(
                s, y, -jointResponse.supportForces[slot]
            )
        }
    )
    val assembledDeparture =
        abs(assembled.peakDishing(T323_SAMPLES) - jointResponse.peakDishing) /
                abs(jointResponse.peakDishing)
    // The bank SLICE against a surrogate built on that placement alone.
    val aloneSurrogate = tile.surrogate(jointPlacementOfPrimary.grid, T323_SAMPLES)
    val sliceDeparture = abs(
        aloneSurrogate.solve(primary.jointStiffnesses).peakDishing - jointResponse.peakDishing
    ) / abs(jointResponse.peakDishing)
    // T-329: both residuals are quantities whose true value is ZERO, so every digit of them is
    // machine noise and neither is printable. What is emitted is the tolerance each identity is
    // asserted at and whether it holds -- which is what F9 and F10 are declared on anyway.
    val sliceIdentityHolds = identityHolds(sliceDeparture, T323_SLICE_TOLERANCE)
    val assembledIdentityHolds = identityHolds(assembledDeparture, T323_ASSEMBLED_TOLERANCE)
    // The searched p90 against the oracle floor, which is a pointwise theorem.
    val floorViolations = if (primary.corners[3].p90OverStroke < primary.oracleP90Floor) 1 else 0

    identities += T323Identity(
        what = "the BANK SLICE against a surrogate built on the joint placement alone (F9)",
        quantity = "the zero-defect peak dishing of the searched distribution at the joint " +
                "placement, in nm",
        tolerance = T323_SLICE_TOLERANCE,
        holds = sliceIdentityHolds,
        note = "the identity the whole method rests on: a placement is a SLICE of the bank's " +
                "index set, so 7 776 evaluations are evaluations of the placements they name. " +
                "This was a convergence row until T-329: coarse and fine are ONE number by " +
                "construction and its `fine` was synthesised from the residual, so the row was " +
                "an identity wearing an axis's clothes. The residual's true value is ZERO and " +
                "it is therefore not emitted"
    )
    identities += T323Identity(
        what = "the surrogate at full presence against the ASSEMBLED solve, with its own " +
                "Woodbury support forces applied as point loads (F10)",
        quantity = "the peak dishing of the searched distribution at the joint placement",
        tolerance = T323_ASSEMBLED_TOLERANCE,
        holds = assembledIdentityHolds,
        note = "taken on the SEARCHED distribution rather than on a transferred rule, because " +
                "that is the object every corner of the 2 x 2 is graded on. The residual's true " +
                "value is ZERO and it is therefore not emitted (T-329)"
    )

    // ================ P8 -- the second composite fraction, if the measured rate admits it
    val budgetSeconds = 5L * 3600L
    val secondArmAdmitted = !t323Smoke && (elapsedSeconds() + primaryElapsed / 2L) < budgetSeconds
    var secondArm: T323Arm? = null
    if (secondArmAdmitted) {
        println("T-323 - P8, the second composite fraction, at " + elapsedSeconds() + " s")
        secondArm = runArm(secondFraction, isPrimary = false)
        cornerRows += secondArm.corners
        censusRows += secondArm.census
        screenRows += secondArm.screens
        splitRows += secondArm.split
        splitRows += secondArm.inSampleSplit
        fragilityRows += secondArm.fragility
        pairedRows += secondArm.paired
    } else {
        dropped += T323DroppedRow(
            what = "P8 -- the same 2 x 2 at C-0116's second composite fraction f = " +
                    secondFraction.emitted(3),
            why = "the declared elastic: the primary arm's own measured cost projects the " +
                    "second arm past the declared " + budgetSeconds + " s budget",
            measured = "the primary arm took " + primaryElapsed + " s and the study stood at " +
                    elapsedSeconds() + " s when the decision was taken; the second arm is " +
                    "estimated at half the primary arm, i.e. " + (primaryElapsed / 2L) + " s"
        )
    }
    dropped += T323DroppedRow(
        what = "the EXHAUSTIVE enumeration at 10, 20 and 30 paths",
        why = "the family holds " + families.getValue(1).size + ", " +
                families.getValue(2).size + " and " + families.getValue(3).size +
                " placements there; only the five-column cell is exhaustible",
        measured = "a per-row coordinate descent is used instead, and its slack is MEASURED at " +
                "the five-column cell where the exhaustive optimum is known (F21)"
    )
    dropped += T323DroppedRow(
        what = "the oracle floor as a full-family SCREEN",
        why = "a ranking quality is a CORRELATION and a correlation needs a sample, not a " +
                "census; running the floor over all " + family.size + " placements would cost " +
                "as much as the census itself and buy nothing the sample does not",
        measured = "the floor is ranked over a deterministic sample of " + t323OracleSample +
                " placements and over the whole searched set"
    )
    dropped += T323DroppedRow(
        what = "the other four radial rungs of C-0208's bracket",
        why = "T-323 opens the PLACEMENT, not the link; C-0212 measured the rung axis and its " +
                "flat verdict moved at none of the five",
        measured = "the bracket FLOOR alone, " + floorRung.emitted(9) + " pN/nm, which is where " +
                "C-0208's and C-0212's tightest cells both live"
    )

    // ================ the reproductions that are not the grid's
    reproductions += T323Reproduction(
        statement = "C-0212's uncoupled 10 x 6 block at the resolved link, f = " +
                primaryFraction.emitted(3),
        published = t323PublishedSearched(
            t316, primaryFraction, "determined station lattice on the rooting helices",
            decidingColumns, "uncoupledDishingOverStroke"
        ),
        here = tile.uncoupledDishing,
        relativeDeparture = abs(
            tile.uncoupledDishing - t323PublishedSearched(
                t316, primaryFraction, "determined station lattice on the rooting helices",
                decidingColumns, "uncoupledDishingOverStroke"
            )
        ) / abs(tile.uncoupledDishing),
        source = ResultInputs.T_316.path
    )
    val worstReproduction = reproductions.maxOf { it.relativeDeparture }
    val worstTransferredReproduction = reproductions
        .filter { it.statement.startsWith("C-0208") }.maxOf { it.relativeDeparture }
    println("  worst reproduction departure over " + reproductions.size + ": " +
            worstReproduction.emittedDimensionless(2))

    // ================ the falsifiers
    val split = primary.split
    val inSample = primary.inSampleSplit
    val jointCorner = primary.corners.first { it.corner.startsWith("P1 D1") }
    val fixedSearchedCorner = primary.corners.first { it.corner.startsWith("P0 D1") }
    val jointDepartures = convergence
        .filter { it.quantity == "the joint corner's out-of-sample p90" }
        .map { it.departure }
    val worstJointDeparture = if (jointDepartures.isEmpty()) 0.0 else jointDepartures.max()
    val interactionMagnitude = abs(exp(abs(split.interaction)) - 1.0)
    val transferredScreens = screenRows.filter { it.screen != "the oracle floor (distribution-free)" }
    val screenIsWrongQuantity = transferredScreens.any {
        it.spearmanAgainstSearched < 0.5 || it.regretOfSelectingOnThisScreen > 1.05
    }
    val fiveByThreeSearchedRowIsBest = gridColumnNames.indices.all { column ->
        val searchedValue = gridValues.last()[column]
        gridValues.dropLast(1).all { row -> searchedValue < row[column] + 1e-12 }
    }
    val gainOverFixed = fixedSearchedCorner.p90OverStroke - jointCorner.p90OverStroke
    val overFit = jointCorner.p90OverStroke - jointCorner.trainingP90

    val falsifiers = listOf(
        T323Falsifier(
            "F1", "a JOINT search reaches an out-of-sample p90 strictly better than both " +
                    "(P1, D0) and (P0, D1) at the deciding cell", true,
            split.jointBeatsBothAlone,
            "joint " + jointCorner.p90OverStroke.emittedDimensionless(9) + " against " +
                    split.searchedPlacementTransferred.emittedDimensionless(9) +
                    " placement-only and " +
                    split.fixedPlacementSearched.emittedDimensionless(9) + " distribution-only"
        ),
        T323Falsifier(
            "F2", "the INTERACTION is resolvable -- its per-cent exceeds the study's own worst " +
                    "convergence departure on the searched p90", true,
            interactionMagnitude > worstJointDeparture,
            "interaction " + split.interactionPerCent.emittedDimensionless(3) + " % against a " +
                    "worst convergence departure of " +
                    (100.0 * worstJointDeparture).emittedDimensionless(3) + " % on the same " +
                    "quantity"
        ),
        T323Falsifier(
            "F3", "the interaction is POSITIVE -- the two freedoms are substitutive, so the two " +
                    "separately measured gains OVERSTATE what a joint search buys", true,
            split.substitutive,
            "declared as the expectation in T-323's Plan, before the run; " +
                    (if (split.substitutive) "substitutive" else "SYNERGISTIC, and the " +
                            "declared expectation is wrong")
        ),
        T323Falsifier(
            "F4", "the interaction carries a LARGER share of the variation than the smaller " +
                    "main effect does, over the 5 x 3 grid -- C-0108's finding on a new pair " +
                    "of factors", true,
            interactionRows.first().interactionExceedsSmallerMainEffect,
            "interaction share " +
                    interactionRows.first().interactionShare.emittedDimensionless(4) +
                    " against placement " +
                    interactionRows.first().placementShare.emittedDimensionless(4) +
                    " and distribution " +
                    interactionRows.first().distributionShare.emittedDimensionless(4)
        ),
        T323Falsifier(
            "F5", "C-0063's ORDERING reverses -- the distribution main effect is larger than " +
                    "the placement main effect on this lattice", true,
            !split.placementMainEffectLarger,
            "the placement term at the transferred rule is " +
                    split.placementTermAtTransferred.emittedDimensionless(4) +
                    " and the distribution term at the fixed placement " +
                    split.distributionTermAtFixedPlacement.emittedDimensionless(4) +
                    ", both in log units"
        ),
        T323Falsifier(
            "F6", "the inherited sentence, measured -- a transferred-rule placement ranking is " +
                    "the WRONG quantity once the distribution is free (Spearman below 0.5, or " +
                    "a regret above 1.05x)", true,
            screenIsWrongQuantity,
            transferredScreens.joinToString("; ") {
                it.screen + ": Spearman " + it.spearmanAgainstSearched.emittedDimensionless(4) +
                        ", regret " + it.regretOfSelectingOnThisScreen.emittedDimensionless(6)
            }
        ),
        T323Falsifier(
            "F7", "a uniform pressure on the free honeycomb lattice at the resolved link does " +
                    "not dish exactly zero", false, uniformDishing > 1e-9,
            "peak dishing over the free stroke: " + uniformDishing.emittedDimensionless(2)
        ),
        T323Falsifier(
            "F8", "the default lattice is not bit-identical to the standing object at " +
                    "assembleLoad, or its crossover site set differs", false,
            !loadIdentical || !siteSetsAgree,
            standingLattice.degreesOfFreedom.toString() + " degrees of freedom and " +
                    standingLattice.bonds.size + " bond sites, identical"
        ),
        T323Falsifier(
            "F9", "the BANK SLICE differs from a surrogate built on that placement alone by " +
                    "more than " + T323_SLICE_TOLERANCE.emittedDimensionless(2) + " relative",
            false, !sliceIdentityHolds,
            "the identity HOLDS to " + T323_SLICE_TOLERANCE.emittedDimensionless(2) +
                    "; its residual is a quantity whose true value is ZERO, so it is reported " +
                    "as a threshold and a boolean and not as a value (T-329), and the record " +
                    "is identities[0]"
        ),
        T323Falsifier(
            "F10", "the surrogate at full presence does not reproduce the ASSEMBLED solve with " +
                    "its own Woodbury support forces, at " +
                    T323_ASSEMBLED_TOLERANCE.emittedDimensionless(2) + " relative", false,
            !assembledIdentityHolds,
            "the identity HOLDS to " + T323_ASSEMBLED_TOLERANCE.emittedDimensionless(2) +
                    ", taken on the SEARCHED distribution at the joint placement; its residual " +
                    "is a quantity whose true value is ZERO and is reported as a threshold and " +
                    "a boolean (T-329), and the record is identities[1]"
        ),
        T323Falsifier(
            "F11", "the transferred rules and the searched distributions at C-0167's four " +
                    "placements do not reproduce C-0208's and C-0212's published p90", false,
            worstReproduction > 1e-6,
            "worst of " + reproductions.size + " is " +
                    worstReproduction.emittedDimensionless(2) + "; worst over the TRANSFERRED " +
                    "readings alone is " + worstTransferredReproduction.emittedDimensionless(2)
        ),
        T323Falsifier(
            "F12", "the SEARCHED placement is worse than the best FIXED placement in the same " +
                    "column of the 5 x 3 grid", false, !fiveByThreeSearchedRowIsBest,
            "DECLARED as a property of the composition, and the declaration is one word short: " +
                    "the composition guarantees it IN SAMPLE, on the stream the search sees, and " +
                    "the 5 x 3 grid is graded OUT of sample. The in-sample statement -- freeing " +
                    "the placement over the tier-2 set cannot lose against C-0167's own member, " +
                    "which is in that set -- " +
                    (if (primary.inSampleGuaranteeHolds) "HOLDS" else "FAILS, which is a defect") +
                    ", at a transferred gain of " +
                    primary.inSampleTransferredGain.emittedDimensionless(9) +
                    "x and a searched gain of " +
                    primary.inSampleSearchedGain.emittedDimensionless(9) + "x. Two of the five " +
                    "grid rows are placements the searched family does not contain at all, so " +
                    "no guarantee of any kind covers them. Both readings are published rather " +
                    "than one being picked."
        ),
        T323Falsifier(
            "F13", "the 2 x 2's path disagreement exceeds 1e-12 of a log unit", false,
            !split.pathDisagreementBelowTolerance,
            "an arithmetic error, never a result; the two orderings share their endpoints"
        ),
        T323Falsifier(
            "F14", "the finalists' TRAINING ranking and their GRADING ranking disagree at rank " +
                    "1 -- the selection over " + family.size + " showing as noise", true,
            primary.finalistRankInversion,
            primary.finalistCount.toString() + " finalists, each carrying both objectives"
        ),
        T323Falsifier(
            "F15", "the census's top-" + t323TopPerScreen + " SET at " +
                    t323ScreeningRealisations + " screening realisations differs from its " +
                    "top-" + t323TopPerScreen + " at " + t323ScreeningConvergenceRealisations,
            true, topAtEighty != topAtForty,
            "the exhaustive ranking being a property of the placements rather than of the draws"
        ),
        T323Falsifier(
            "F16", "the joint optimum's per-path PEAK exceeds C-0023's " +
                    unzipCeiling.emitted(9) + " pN/nm, read over section 3's acceptable stroke",
            true, !jointCorner.peakInsideUnzipCeiling,
            "peak " + jointCorner.peakStiffness.emitted(9) + " pN/nm against a uniform share of " +
                    (mandate / jointCorner.pathCount).emitted(9)
        ),
        T323Falsifier(
            "F17", "the joint optimum BEATS the uncoupled tile at the 90th percentile", true,
            jointCorner.beatsUncoupledAtP90,
            "joint " + jointCorner.p90OverStroke.emittedDimensionless(9) + " against an " +
                    "uncoupled " + jointCorner.uncoupledP90.emittedDimensionless(9) +
                    "; at ZERO defects the joint corner reads " +
                    jointCorner.nominalOverStroke.emittedDimensionless(9) + ", which " +
                    (if (jointCorner.beatsUncoupledAtZeroDefects) "DOES" else "does not") +
                    " beat it"
        ),
        T323Falsifier(
            "F18", "the joint optimum loses T-5b to its worst single missing path", true,
            jointCorner.flatAtP90 && !jointCorner.stillFlatAfterWorstRemoval,
            "worst single-path removal " +
                    jointCorner.worstSinglePathRemoval.emittedDimensionless(9) +
                    " against T-5b's " + T323_TOLERANCE.emitted(2)
        ),
        T323Falsifier(
            "F19", "the joint optimum's out-of-sample p90 is worse than its in-sample training " +
                    "objective by more than the whole gain it reports over (P0, D1)", true,
            overFit > gainOverFixed,
            "out of sample minus in sample is " + overFit.emittedDimensionless(4) +
                    " against a gain over the fixed placement of " +
                    gainOverFixed.emittedDimensionless(4) +
                    (if (jointCorner.placementLabel == fixedSearchedCorner.placementLabel)
                        " -- and the two corners are the SAME PLACEMENT, so there is no gain " +
                                "for an over-fit to consume and the comparison is degenerate " +
                                "rather than adverse"
                    else "")
        ),
        T323Falsifier(
            "F20", "the SCREEN is binding -- the joint winner is the last-ranked placement " +
                    "admitted to tier 2, so the answer is a property of K", true,
            transferredScreens.any { it.screenIsBinding },
            transferredScreens.joinToString("; ") {
                it.screen + ": the joint winner ranks " + it.jointWinnerRankInThisScreen +
                        " of " + it.placementsRanked
            }
        ),
        T323Falsifier(
            "F21", "the per-row placement DESCENT does not find the exhaustive optimum at the " +
                    "50-path cell", true,
            descended.label != primary.census[0].bestPlacementLabel,
            "the descent's own screening objective is " +
                    descentSlack.emittedDimensionless(9) + "x the exhaustive optimum's"
        ),
        T323Falsifier(
            "F22", "the placement family census does not reproduce -- " +
                    ladderSizes.sum() + " stations, per-row " + ladderSizes.joinToString(", ") +
                    ", family sizes " + families.toSortedMap().values.joinToString(", ") {
                        it.size.toString()
                    } + ", and NO centro-symmetric member at the determined phase", false,
            false,
            "asserted against the lattice object rather than transcribed, and it runs before " +
                    "any solve"
        ),
        T323Falsifier(
            "F23", "two independent runs of the study do not produce a byte-identical result file",
            false, false,
            "a run cannot assert byte-identity about itself, so this is measured EXTERNALLY, by " +
                    "two emissions in two snapshots diffed outside the study. It FIRED at the " +
                    "first emission (C-0216 section 14, 26 of 1 252 leaves) because the " +
                    "sentence beside it was an assertion and not a property: five of this " +
                    "study's nineteen selection sites decided at searchDecision's six " +
                    "significant digits and fourteen compared a raw Double. T-328 routes every " +
                    "one of them through decidesBetter, byDecisionThenLabel or decisionArgmin, " +
                    "and T-329 stops the two identity residuals being printable; C-0217 " +
                    "reports the re-emission and its diff"
        )
    )

    // ================ the verdict, the findings, and the emission
    val flatCorners = cornerRows.count { it.flatAtP90 }
    val admissibleCorners = cornerRows.count { it.flatAndAdmissible }
    val beatUncoupled = cornerRows.count { it.beatsUncoupledAtP90 }
    val verdict = linkedMapOf(
        "does a JOINT search reach what neither search reaches alone" to
                ((if (split.jointBeatsBothAlone) "YES" else "NO") + " -- joint " +
                        jointCorner.p90OverStroke.emittedDimensionless(9) + " against " +
                        split.searchedPlacementTransferred.emittedDimensionless(9) +
                        " for the placement alone and " +
                        split.fixedPlacementSearched.emittedDimensionless(9) +
                        " for the distribution alone, all out of sample on the " +
                        T323_GRADING_SEED + " stream"),
        "the TOTAL of the two-factor move, in log units" to
                (split.total.emittedDimensionless(9) + ", i.e. a factor of " +
                        (split.fixedPlacementTransferred / split.searchedPlacementSearched)
                            .emittedDimensionless(9) + " on the deciding statistic"),
        "the INTERACTION, in log units and as a per cent" to
                (split.interaction.emittedDimensionless(4) + " and " +
                        split.interactionPerCent.emittedDimensionless(4) + " %, " +
                        (if (split.substitutive) "POSITIVE -- the two freedoms are SUBSTITUTIVE, " +
                                "so the two separately measured gains OVERSTATE what a joint " +
                                "search buys"
                        else "NEGATIVE -- the two freedoms are SYNERGISTIC, so each is worth " +
                                "more when the other is free")),
        "the same INTERACTION taken IN SAMPLE, where no corner carries a selection" to
                (inSample.interaction.emittedDimensionless(4) + " and " +
                        inSample.interactionPerCent.emittedDimensionless(4) + " %, " +
                        (if (inSample.substitutive) "SUBSTITUTIVE" else "SYNERGISTIC") +
                        " -- the difference against the out-of-sample reading is what the " +
                        "SELECTION costs, and the placement freedom carries a larger selection " +
                        "than the distribution freedom does"),
        "the split, placement FIRST" to
                ("placement " + split.placementTermAtTransferred.emittedDimensionless(4) +
                        " then distribution " +
                        split.distributionTermAtSearchedPlacement.emittedDimensionless(4)),
        "the split, distribution FIRST" to
                ("distribution " + split.distributionTermAtFixedPlacement.emittedDimensionless(4) +
                        " then placement " +
                        split.placementTermAtSearchedDistribution.emittedDimensionless(4)),
        "C-0063's ordering on THIS lattice" to
                ((if (split.placementMainEffectLarger)
                    "UPHELD -- the placement main effect is larger"
                else "REVERSED -- the DISTRIBUTION main effect is larger") +
                        ", at " + abs(split.placementTermAtTransferred).emittedDimensionless(4) +
                        " against " +
                        abs(split.distributionTermAtFixedPlacement).emittedDimensionless(4) +
                        " in log units"),
        "the interaction's share of the variation over the 5 x 3 grid" to
                (interactionRows.first().interactionShare.emittedDimensionless(4) +
                        " against a placement main effect of " +
                        interactionRows.first().placementShare.emittedDimensionless(4) +
                        " and a distribution main effect of " +
                        interactionRows.first().distributionShare.emittedDimensionless(4)),
        "is the joint optimum FLAT at the 90th percentile" to
                ((if (jointCorner.flatAtP90) "YES" else "NO") + " -- " +
                        jointCorner.p90OverStroke.emittedDimensionless(9) + " against T-5b's " +
                        T323_TOLERANCE.emitted(2)),
        "is it ADMISSIBLE -- flat AND inside C-0023's per-path allowable" to
                ((if (jointCorner.flatAndAdmissible) "YES" else "NO") + " -- peak " +
                        jointCorner.peakStiffness.emitted(9) + " pN/nm against " +
                        unzipCeiling.emitted(9)),
        "does it beat the UNCOUPLED tile" to
                ((if (jointCorner.beatsUncoupledAtP90) "YES" else "NO") +
                        " at the 90th percentile and " +
                        (if (jointCorner.beatsUncoupledAtZeroDefects) "YES" else "NO") +
                        " at zero defects -- " +
                        jointCorner.uncoupledP90.emittedDimensionless(9) + " uncoupled"),
        "the conjunction over every corner graded here" to
                (cornerRows.size.toString() + " corners, " + flatCorners + " flat, " +
                        admissibleCorners + " flat AND admissible, " + beatUncoupled +
                        " beating the uncoupled tile"),
        "the placement family, and what made the answer affordable" to
                (family.size.toString() + " placements at the deciding cell, enumerated " +
                        "EXHAUSTIVELY on a bank of " + family.stationCount +
                        " unit-point-load solves, with NO centro-symmetric member at any of " +
                        "them")
    )

    val findings = listOf(
        "THE PLACEMENT AND THE DISTRIBUTION HAVE NOW BEEN SEARCHED TOGETHER, and the answer is " +
                "a TOTAL of " + split.total.emittedDimensionless(4) + " log units with an " +
                "INTERACTION of " + split.interactionPerCent.emittedDimensionless(4) + " %. " +
                (if (split.substitutive)
                    "The interaction is POSITIVE, so the two freedoms are SUBSTITUTIVE: each " +
                            "is worth LESS when the other is free, and adding the two " +
                            "separately measured gains overstates the joint answer."
                else "The interaction is NEGATIVE, so the two freedoms are SYNERGISTIC: each " +
                        "is worth MORE when the other is free."),
        "THE SPLIT IS ORDER-DEPENDENT AND BOTH ORDERINGS ARE EMITTED. Placement first: " +
                split.placementTermAtTransferred.emittedDimensionless(4) + " then " +
                split.distributionTermAtSearchedPlacement.emittedDimensionless(4) + ". " +
                "Distribution first: " +
                split.distributionTermAtFixedPlacement.emittedDimensionless(4) + " then " +
                split.placementTermAtSearchedDistribution.emittedDimensionless(4) + ". " +
                "The two totals agree identically and their difference IS the interaction -- " +
                "the path disagreement is below " +
                split.pathDisagreementTolerance.emittedDimensionless(2) + " of a log unit.",
        "AND THE SAME 2 x 2 IS TAKEN IN SAMPLE, WHICH IS WHAT SEPARATES THE INTERACTION FROM " +
                "THE SELECTION. Out of sample every corner but (P0, D0) carries a selection and " +
                "the placement freedom carries a much larger one; in sample none of them does. " +
                "The interaction is " + split.interactionPerCent.emittedDimensionless(4) +
                " % out of sample and " + inSample.interactionPerCent.emittedDimensionless(4) +
                " % in sample, and the joint corner " +
                (if (inSample.jointBeatsBothAlone) "does" else "does NOT") +
                " beat both singles in sample against " +
                (if (split.jointBeatsBothAlone) "does" else "does NOT") + " out of it.",
        "C-0063's ORDERING, MEASURED ON THIS LATTICE: " +
                (if (split.placementMainEffectLarger) "UPHELD" else "REVERSED") +
                ". The placement main effect is " +
                abs(split.placementTermAtTransferred).emittedDimensionless(4) +
                " and the distribution main effect " +
                abs(split.distributionTermAtFixedPlacement).emittedDimensionless(4) +
                " in log units.",
        "THE CHEAP BOUND DECIDED THE METHOD AND IT NEEDED NO SOLVE: the determined ladder " +
                "carries " + ladderSizes.sum() + " stations, " + ladderSizes.joinToString(", ") +
                " by row, so at five columns the five-station rows are FORCED and the family " +
                "is 6^5 = " + family.size + " -- exhaustible. And it admits NO " +
                "centro-symmetric member at any of its " + shared.rasterRows / 2 + " row " +
                "pairs, so C-0063's own search strategy has no analogue here.",
        "THE INHERITED SENTENCE, MEASURED: " + transferredScreens.joinToString("; ") {
            it.screen + " ranks the placements at Spearman " +
                    it.spearmanAgainstSearched.emittedDimensionless(4) +
                    " against the searched objective, with a regret of " +
                    it.regretOfSelectingOnThisScreen.emittedDimensionless(6)
        } + ". CLAUDE.md's *selecting a placement on the EQUAL-SPRING objective is selecting " +
                "on the wrong quantity once a distribution is free* is a warning this corpus " +
                "asserts and had never measured on this lattice.",
        "EVERY THRESHOLD THE MOVING QUANTITIES FEED, AND THEIR CONJUNCTION -- which CH-0272 " +
                "records no verdict block here has ever stated: over " + cornerRows.size +
                " graded corners, " + flatCorners + " are flat at the 90th percentile, " +
                admissibleCorners + " are flat AND inside C-0023's per-path allowable, and " +
                beatUncoupled + " beat the uncoupled tile."
    )

    val validity = listOf(
        "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated. No such " +
                "coupling has been drawn, let alone folded.",
        "The PLACEMENT family is C-0141's determined station ladder on the rooting helices, at " +
                "the phase C-0148's +/-5 bp rule DETERMINES (" + T323_LADDER_PHASE + ") and the " +
                "forced " + T323_LADDER_OFFSET + " bp inter-row offset. The three unrealisable " +
                "members of C-0167's four placements are GRADED and not SEARCHED.",
        "The radial link constant is unsourceable (C-0208) and is carried at its bracket FLOOR " +
                "alone here; C-0212 measured the rung axis with the placement fixed and its " +
                "flat verdict moved at none of the five.",
        "CH-0242's common-mode spring is absent, so every bond and every tie is still missing " +
                "the stiffer of the two springs.",
        "The census is on ROUTE A, whose raster turns carry ZERO unpaired nucleotides " +
                "(C-0175's modelling choice). C-0193 and C-0200 establish that the only folded " +
                "block of this cross-section does otherwise.",
        "C-0060's " + FLAT_RATIO_FLOOR.emitted(2) + " to " + FLAT_RATIO_CEILING.emitted(2) +
                " is its FLAT ratio window, measured on C-0058's square-lattice 45-station " +
                "design, and it is NOT a buildability constraint (CH-0273). It is read here on " +
                "the TWO-LEVEL projection, which is the object C-0060 measured it on.",
        "BUILDABILITY is not established at any corner. What it costs to PLACE a distribution " +
                "spanning a large stiffness ratio is C-0060's own placement question, and " +
                "nothing in this corpus prices it.",
        "A DESCENT REPORTS THE BEST POINT IT FOUND. The five-column cell is exhaustive; 10, 20 " +
                "and 30 paths are a per-row descent whose slack is measured at the five-column " +
                "cell (F21) and is not zero by assumption.",
        "The COUNT is out of sample and WHICH placement is tightest is an order statistic over " +
                family.size + " of them. Quote the count as the result and the placement as an " +
                "identification.",
        "One load case (C-0022's solved collar at 2 mM, 10 nm, 0.192 V), one cross-section, one " +
                "raster, one dropout model, one composite fraction unless P8 ran.",
        "EVERY SEARCH DECISION IN THIS STUDY IS TAKEN AT SIX SIGNIFICANT DIGITS, ties broken on " +
                "the candidate's own label (T-328, C-0217), and the two identities are reported " +
                "as a tolerance and a boolean rather than as residuals whose true value is zero " +
                "(T-329). What that does NOT stabilise is C-0135's descent MANIFOLD: where the " +
                "active constraints are fewer than the free directions the optimal set is a " +
                "manifold and rounding fixes which BRANCH is taken, not the POINT."
    )

    val openQuestions = listOf(
        "The LADDER PHASE is determined by C-0148's rule on the drawable raster and is " +
                "therefore not a design variable here. Whether a raster that closes at another " +
                "phase would place better is a question about the RASTER, not the placement.",
        "A smoothed CVaR of a log-sum-exp -- convex in the sampled field, an upper bound on the " +
                "percentile, differentiable throughout -- would let the DISTRIBUTION half use " +
                "an adjoint gradient on the quantity the verdict is read on. C-0212 priced it " +
                "and this task inherits the pricing unchanged.",
        "The best design inside C-0060's TWO-LEVEL family, searched rather than projected into; " +
                "CLAUDE.md prices the projected-against-searched gap at 24.9 %.",
        "Whether the SHARED-BODY topology -- C-0017's mandate spent once in a rigid-body mode " +
                "rather than at every station -- moves this census. That is a change of " +
                "TOPOLOGY, and it is orthogonal to both factors measured here.",
        "What a joint search does on ROUTE B, whose turns carry 28 unpaired nucleotides.",
        "What it costs to PLACE the joint optimum: its stiffness ratio is " +
                jointCorner.ratio.emittedDimensionless(9) + ", and C-0060's own exponents turn " +
                "that into a member LENGTH nobody has priced on this lattice."
    )

    val result = T323Result(
        task = "T-323",
        claim = "C-0216",
        leaf = "A8.2",
        question = "Does a JOINT search over the station set and the per-path stiffness vector " +
                "reach what neither reaches alone -- and what is the INTERACTION of the two " +
                "factors, in both orderings?",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm",
            "pressure" to "pN/nm^2 = 1 MPa",
            "dishing" to "dimensionless, as a fraction of the free stroke",
            "ratio" to "dimensionless, max over min of a per-path stiffness vector",
            "split term" to "dimensionless, a natural logarithm of a ratio of two p90 readings"
        ),
        conventions = mapOf(
            "s" to "along the helices, origin at the face centre",
            "y" to "across the helices in the plane of the face, origin at the face centre",
            "W" to "positive DOWNWARD, toward the electrode (C-0006)",
            "dishing" to "the peak of |w - the best-fit rigid plane| over an 81 x 81 face grid, " +
                    "divided by the free-tile stroke",
            "placement" to "for each x-raster row, a set of `columns` DISTINCT stations of that " +
                    "row's own 21 bp ladder at the row's rooting-helix y; the key is the tuple " +
                    "of station indices, ascending within a row and row-major overall",
            "tie-break" to "at a tie in the searchDecision-rounded objective the SMALLER KEY " +
                    "wins, so the answer is a property of the family and not of a traversal",
            "resolvedLink" to "k_radial * unitZ^2 + k_transverse * unitY^2 per bond (C-0208), " +
                    "with k_transverse pinned at C-0205's ceiling",
            "transferred" to "a distribution RULE evaluated on the station set -- C-0058's " +
                    "equal springs and its rim-graded 5:1 at a 6.7 nm band",
            "searched" to "T-316's composition, unchanged: C-0135's smoothed minimax on the " +
                    "zero-defect peak, then a multi-start descent on the TRUE training " +
                    "percentile seeded from it and from both transferred rules",
            "outOfSample" to "every quoted verdict is graded on the " + T323_GRADING_SEED +
                    " stream; the distribution search sees " + T323_TRAINING_SEED +
                    " and the exhaustive placement census " + T323_SCREENING_SEED,
            "interaction" to "the gap between the two orderings of one 2 x 2, in log units; " +
                    "POSITIVE means SUBSTITUTIVE and NEGATIVE means SYNERGISTIC",
            "flatRatioWindow" to "C-0060's " + FLAT_RATIO_FLOOR.emitted(2) + " to " +
                    FLAT_RATIO_CEILING.emitted(2) + " is its FLAT ratio window, measured on " +
                    "C-0058's square-lattice 45-station design -- NOT a buildability " +
                    "constraint (CH-0273)"
        ),
        parameters = mapOf(
            "crossSection" to shared.crossSection,
            "rowBasePairs" to shared.rowBasePairs.toString(),
            "edgeX" to shared.edgeX.emitted(9),
            "edgeY" to shared.edgeY.emitted(9),
            "interhelicalDistance" to d.emitted(9),
            "hingeStiffness" to kTheta.emitted(9),
            "transverseConstant" to T323_SHEAR_CEILING.emitted(9),
            "radialBracketFloor" to radial.floor.emitted(9),
            "throughThicknessLinkAtTheFloor" to throughThickness(radial.floor).emitted(9),
            "foundationStiffness" to Gen1Tile.FOUNDATION_SECANT.emitted(9),
            "compositeFractionPrimary" to primaryFraction.emitted(3),
            "compositeFractionSecond" to secondFraction.emitted(3),
            "secondArmRan" to secondArmAdmitted.toString(),
            "mandate" to ("C-0017's " + mandate.emitted(9) + " pN/nm on the SUM, section 3's " +
                    "acceptable clause"),
            "ladderPhase" to T323_LADDER_PHASE.toString(),
            "ladderInterRowOffset" to T323_LADDER_OFFSET.toString(),
            "candidateStations" to family.stationCount.toString(),
            "rowLadderSizes" to ladderSizes.joinToString(", "),
            "familySizeAtFiveColumns" to family.size.toString(),
            "decidingColumns" to decidingColumns.toString(),
            "descentColumns" to descentColumns.joinToString(", "),
            "gradingRealisations" to t323Realisations.toString(),
            "gradingSeed" to T323_GRADING_SEED.toString(),
            "trainingRealisations" to t323TrainingRealisations.toString(),
            "trainingSeed" to T323_TRAINING_SEED.toString(),
            "coarseTrainingRealisations" to t323CoarseTrainingRealisations.toString(),
            "screeningRealisations" to t323ScreeningRealisations.toString(),
            "screeningSeed" to T323_SCREENING_SEED.toString(),
            "screeningConvergenceRealisations" to t323ScreeningConvergenceRealisations.toString(),
            "topPerScreen" to t323TopPerScreen.toString(),
            "finalists" to t323Finalists.toString(),
            "oracleSample" to t323OracleSample.toString(),
            "placementDescentSweeps" to t323PlacementDescentSweeps.toString(),
            "samples" to T323_SAMPLES.toString(),
            "searchSamples" to T323_SEARCH_SAMPLES.toString(),
            "percentileSweeps" to T323_PERCENTILE_SWEEPS.toString(),
            "coarsePercentileSweeps" to T323_COARSE_PERCENTILE_SWEEPS.toString(),
            "percentileScanPoints" to T323_SCAN_POINTS.toString(),
            "percentileRefinements" to T323_REFINEMENTS.toString(),
            "smoothingLevels" to T323_SMOOTHING_LEVELS.joinToString(", ") { it.emitted(3) },
            "smoothingIterationsPerLevel" to T323_SMOOTHING_ITERATIONS.toString(),
            "smoothedMinimaxPolishSweeps" to T323_POLISH_SWEEPS.toString(),
            "tolerance" to T323_TOLERANCE.emitted(2),
            "rimBand" to T323_RIM_BAND.emitted(3),
            "unzipCeiling" to unzipCeiling.emitted(9),
            "flatRatioWindow" to (FLAT_RATIO_FLOOR.emitted(2) + " to " +
                    FLAT_RATIO_CEILING.emitted(2) + " (C-0060, its FLAT window, CH-0273)"),
            "raster" to (T323_RECOMMENDED_ONE.toString() + " / " + T323_RECOMMENDED_TWO +
                    " (C-0151, drawable)")
        ),
        sources = listOf(
            ResultInputs.T_3B.path + " (C-0022's solved collar at 2 mM / 10 nm / 0.192 V)",
            ResultInputs.T_310.path + " (C-0208's coupled cells at the resolved link)",
            ResultInputs.T_316.path + " (C-0212's searched distributions at the same cells)"
        ),
        citedInputs = mapOf(
            "C-0063 placement axis on the SQUARE lattice" to "0.4156 to 0.0706, a factor of 5.9",
            "C-0063 distribution axis on its own winner" to "13.9 %",
            "C-0212 distribution axis on THIS lattice" to "1.10434917 to 1.70065256",
            "C-0208 tightest transferred p90 at the radial bracket floor" to "0.100198485",
            "C-0212 tightest searched p90" to "0.0647024141",
            "C-0108 interaction share against a phase main effect" to "9.79 % against 7.84 %",
            "C-0205 shear ceiling, the transverse constant" to "254.808095 pN/nm",
            "T-5b flatness tolerance" to "0.10 of the free stroke"
        ),
        cheapBound = cheapBound,
        family = familyRows,
        placementCensus = censusRows,
        screens = screenRows,
        corners = cornerRows,
        split = splitRows,
        grid = gridRows,
        interaction = interactionRows,
        fragility = fragilityRows,
        paired = pairedRows,
        verdict = verdict,
        convergence = convergence,
        identities = identities,
        reproductions = reproductions,
        falsifiers = falsifiers,
        dropped = dropped,
        findings = findings,
        validity = validity,
        openQuestions = openQuestions,
        proseFailure = "none"
    )

    val output = File("gpd/results/T-323-the-placement-and-the-distribution-together.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digits = 9,
                digitsByKey = mapOf(
                    "convergence/departure" to DEPARTURE_SIGNIFICANT_DIGITS,
                    "reproductions/relativeDeparture" to DEPARTURE_SIGNIFICANT_DIGITS,
                    "split/interaction" to DEPARTURE_SIGNIFICANT_DIGITS,
                    "split/interactionPerCent" to DEPARTURE_SIGNIFICANT_DIGITS,
                    "interaction/worstResidual" to DEPARTURE_SIGNIFICANT_DIGITS,
                    "interaction/worstResidualPerCent" to DEPARTURE_SIGNIFICANT_DIGITS,
                    "interaction/interactionSumOfSquares" to 4,
                    "interaction/interactionShare" to 4
                ),
                floor = 1e-12
            ).withEmissionHeader(LatticeTag.HONEYCOMB, null) as JsonObject)
        ) + "\n"
    )
    println("T-323 - wrote " + output.path + " at " + elapsedSeconds() + " s")
}
