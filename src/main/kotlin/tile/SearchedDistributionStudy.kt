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
import com.xemantic.nano.plentyofroom.coupling.perPathStiffnessCeiling
import com.xemantic.nano.plentyofroom.coupling.orderStatistic
import com.xemantic.nano.plentyofroom.coupling.quantiseToLevels
import com.xemantic.nano.plentyofroom.coupling.summariseDropoutDishing
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

// ---------------------------------------------------------------------------------------------
// T-316 -- a distribution SEARCHED at the resolved per-bond link.
//
// Every coupled cell of every census in this corpus is graded on C-0058's equal springs and its
// rim-graded 5:1, both of them rules TRANSFERRED onto the lattice rather than optima OF it.
// C-0208's tightest cell misses T-5b's 0.10 by 0.198 %.
//
// The bank is the whole economy: an influence surrogate is a property of the STRUCTURE, so one
// per (placement, columns, fraction, rung) serves every distribution ever tried at that cell.
// ---------------------------------------------------------------------------------------------

private const val T316_SAMPLES: Int = 81

/**
 * The dishing grid the SEARCH runs on, against [T316_SAMPLES] for the grading.
 *
 * `C-0167` measures the free field's own departure over 41 / 81 / 161 at exactly `0.0`, because
 * the 41-grid is a subset of the 81-grid and the peak lands on a shared node. A **searched**
 * answer need not inherit that — a descent carries an argmin — so the search grid is a declared
 * convergence axis here and not an inherited one, and the deciding cell is re-searched at 81 and
 * at 161. It buys a factor of `(81/41)^2 = 3.9` on the term that dominates the whole study.
 */
private const val T316_SEARCH_SAMPLES: Int = 41

/** The smoothing ladder the nominal minimax is run on, and the iterations at each level. */
private val T316_SMOOTHING_LEVELS: List<Double> = listOf(0.3, 0.1, 0.03, 0.01)

private const val T316_SMOOTHING_ITERATIONS: Int = 12

private const val T316_POLISH_SWEEPS: Int = 2

/** The coarse scan and the golden-section refinements the percentile descent takes per coordinate. */
private const val T316_SCAN_POINTS: Int = 5

private const val T316_REFINEMENTS: Int = 6
private const val T316_TOLERANCE: Double = 0.10
private const val T316_RIM_STANDOFF: Double = 1.0
private const val T316_RIM_BAND: Double = 6.7

/** `C-0208`'s own grading seed, so its published cells reproduce here bit for bit. */
private const val T316_GRADING_SEED: Long = 197_197L

/** A different seed for the ensemble the SEARCH sees, so every graded percentile is out of sample. */
private const val T316_TRAINING_SEED: Long = 316_316L

private const val T316_BLOCK_EXTENT_BP: Int = 116
private const val T316_LADDER_PHASE: Int = 16
private const val T316_LADDER_OFFSET: Int = 14
private const val T316_RECOMMENDED_ONE: Int = 102
private const val T316_RECOMMENDED_TWO: Int = 109

/** `C-0205`'s own ceiling, in pN/nm — the TRANSVERSE constant every resolution is read at. */
private const val T316_SHEAR_CEILING: Double = 254.80809548301096

/** The interface one honeycomb crossover owns, in base pairs — the lattice's own period. */
private const val T316_CONTACT_BP: Double = 21.0

/** The sweeps the percentile coordinate descent is allowed, and its price is emitted beside it. */
private const val T316_PERCENTILE_SWEEPS: Int = 2

/** `C-0060`'s MEASURED buildable stiffness-ratio window, re-swept at 21 ratios by `T-122`. */
private const val BUILDABLE_RATIO_FLOOR: Double = 3.5

/** The other edge of the same measured window. */
private const val BUILDABLE_RATIO_CEILING: Double = 20.0

/** `unitZ` at a bond running through the thickness — `sqrt(3)/2`, so `unitZ^2 = 0.75`. */
private const val SQRT_THREE_HALVES: Double = 0.8660254037844386

private val t316Realisations: Int =
    if (System.getenv("T316_SMOKE") == "1") 150 else 4000

/**
 * The realisations the SEARCH sees.
 *
 * `C-0089` used 200 and swept 100 / 200 / 400; 120 is inside that swept range and the axis is
 * **re-taken here** at 60 / 120 / 240 on this lattice's own deciding cell, because the term it
 * multiplies — `sweeps x paths x evaluations x realisations x paths x samples^2` — is 60 % of the
 * whole study and a budget nobody measures is a budget nobody chose.
 */
private val t316TrainingRealisations: Int =
    if (System.getenv("T316_SMOKE") == "1") 40 else 120

private fun Double.emitted(digits: Int = 9): String = roundedForProse(digits).toString()

// ------------------------------------------------------------------------------ the records

@Serializable
private class T316CheapBoundRow(
    val question: String,
    val answer: String,
    val value: Double,
    val units: String,
    val consequence: String
)

@Serializable
private class T316OracleRow(
    val cell: String,
    val placement: String,
    val columns: Int,
    val pathCount: Int,
    val compositeFraction: Double,
    val p90FloorOverStroke: Double,
    val bestTransferredP90: Double,
    val bestTransferredOverFloor: Double,
    val excludesEveryDistributionAtP90: Boolean
)

@Serializable
private class T316Cell(
    val radialLinkStiffness: Double,
    val throughThicknessLink: Double,
    val compositeFraction: Double,
    val placement: String,
    val columns: Int,
    val pathCount: Int,
    /** `C-0058`'s equal springs, graded on the GRADING ensemble — a transferred rule. */
    val equalP90: Double,
    /** `C-0058`'s rim-graded 5:1, graded the same way — the other transferred rule. */
    val rimGradedP90: Double,
    val bestTransferredP90: Double,
    val bestTransferredLabel: String,
    /** The smoothed minimax on the ZERO-DEFECT peak, graded out of sample at the 90th percentile. */
    val nominalSearchP90: Double,
    val nominalSearchNominal: Double,
    val nominalSearchRatio: Double,
    /** The percentile descent's answer, graded OUT OF SAMPLE on the grading ensemble. */
    val searchedP90: Double,
    val searchedNominal: Double,
    val searchedRatio: Double,
    /** The largest single-path stiffness the argmin demands, in pN/nm. */
    val searchedPeakStiffness: Double,
    /** Whether that peak is inside `C-0023`'s unzip allowable read over §3's acceptable stroke. */
    val peakInsideUnzipCeiling: Boolean,
    /** The same distribution's objective on the TRAINING ensemble — in sample, by construction. */
    val searchedTrainingP90: Double,
    val bestTransferredTrainingP90: Double,
    /** `in sample gain / out of sample gain` — how much of the search survived the change of stream. */
    val inSampleGain: Double,
    val outOfSampleGain: Double,
    val flatAtP90: Boolean,
    val bestTransferredFlatAtP90: Boolean,
    val ratioInsideBuildableWindow: Boolean,
    val uncoupledDishingOverStroke: Double,
    val beatsUncoupledAtP90: Boolean
)

@Serializable
private class T316FragilityRow(
    val cell: String,
    val distribution: String,
    val p90OverStroke: Double,
    val nominalOverStroke: Double,
    val worstSinglePathRemovalOverStroke: Double,
    val amplification: Double,
    val ratio: Double,
    val twoLevelRatio: Double,
    val twoLevelP90OverStroke: Double,
    val twoLevelFlatAtP90: Boolean
)

@Serializable
private class T316RungRow(
    val radialLinkStiffness: Double,
    val throughThicknessLink: Double,
    val ground: String,
    val cell: String,
    val bestTransferredP90: Double,
    val searchedP90: Double,
    val searchedRatio: Double,
    val flatAtP90: Boolean
)

@Serializable
private class T316CensusRow(
    val statistic: String,
    val cellsGraded: Int,
    val flatOnATransferredRule: Int,
    val flatOnTheSearchedRule: Int,
    val tightestTransferredP90: Double,
    val tightestSearchedP90: Double,
    val tightestSearchedCell: String,
    val searchedBeatsTransferredCells: Int,
    val worstOutOfSampleLoss: Double
)

@Serializable
private class T316Convergence(
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
private class T316Reproduction(
    val statement: String,
    val published: Double,
    val here: Double,
    val relativeDeparture: Double,
    val source: String
)

@Serializable
private class T316Falsifier(
    val id: String,
    val statement: String,
    val declaredOpen: Boolean,
    val fired: Boolean,
    val note: String
)

@Serializable
private class T316Result(
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
    val cheapBound: List<T316CheapBoundRow>,
    val oracle: List<T316OracleRow>,
    val cells: List<T316Cell>,
    val fragility: List<T316FragilityRow>,
    val rungs: List<T316RungRow>,
    val census: List<T316CensusRow>,
    val verdict: Map<String, String>,
    val convergence: List<T316Convergence>,
    val reproductions: List<T316Reproduction>,
    val falsifiers: List<T316Falsifier>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val proseFailure: String
)

// ------------------------------------------------------------------------------ the geometry

private class T316Profile(
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, edgeX: Double, edgeY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, edgeY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T316_RIM_STANDOFF))
        )
}

private fun t316Profile(file: File): T316Profile {
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
    return T316Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

/** `C-0167`/`C-0180`/`C-0205`/`C-0208`'s geometry, unchanged — only the distribution moves. */
private class T316Shared(val profile: T316Profile) {
    val rasterRows: Int = 10
    val helicesPerRow: Int = 6
    val rowBasePairs: Int = T316_BLOCK_EXTENT_BP
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

/** One `(composite fraction, radial constant, subdivisions)` — one factorisation, many banks. */
private class T316Tile(
    val shared: T316Shared,
    val enhancement: Double,
    val radial: Double,
    val subdivisions: Int = 1
) {
    val lattice: HoneycombGrillage = honeycombTiedLatticeAtResolvedLink(
        block = shared.block,
        rowBasePairs = shared.rowBasePairs,
        enhancement = enhancement,
        tied = true,
        transverseLinkStiffness = T316_SHEAR_CEILING,
        radialLinkStiffness = radial,
        subdivisions = subdivisions
    )

    val freeStroke: Double by lazy {
        lattice.solve(uniformPressure(shared.interiorPressure)).meanDeflection
    }

    val uncoupledDishing: Double by lazy {
        lattice.solve(shared.pressureField).peakDishing(T316_SAMPLES) / freeStroke
    }

    fun surrogate(grid: List<Pair<Double, Double>>, samples: Int = T316_SAMPLES):
            InfluenceSurrogate = honeycombTiedSurrogate(
        lattice, grid, shared.pressureField, samples
    )
}

private fun t316Distributions(
    grid: List<Pair<Double, Double>>,
    edgeX: Double,
    edgeY: Double
): List<Pair<String, List<Double>>> = listOf(
    "equal springs" to equalShareOfMandate(grid.size),
    "rim-graded 5:1" to rimGradedShareOfMandate(
        grid.map { (x, y) ->
            val onRim = abs(x) > edgeX / 2.0 - T316_RIM_BAND || abs(y) > edgeY / 2.0 - T316_RIM_BAND
            if (onRim) 5.0 else 1.0
        }
    )
)

/** `C-0167`'s four placements, unchanged. */
private fun t316Placements(
    shared: T316Shared,
    rootingHelixY: List<Double>,
    columns: Int
): List<Pair<String, List<Pair<Double, Double>>>> {
    val abstract = attachmentGrid(columns, shared.rasterRows, shared.edgeX, shared.edgeY)
    val raster = twoLengthRaster(
        shared.rasterRows, shared.helicesPerRow, T316_RECOMMENDED_ONE, T316_RECOMMENDED_TWO
    )
    val determined = twoLengthSnappedGrid(
        raster, columns, shared.edgeY, T316_LADDER_PHASE, T316_LADDER_OFFSET
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

private class T316Graded(val nominal: Double, val p90: Double, val flat: Boolean)

private fun t316Grade(
    surrogate: InfluenceSurrogate,
    stiffnesses: List<Double>,
    freeStroke: Double,
    ensemble: DropoutEnsemble
): T316Graded {
    val nominal = surrogate.solve(stiffnesses).peakDishing / freeStroke
    val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
    sample.indices.forEach { sample[it] = sample[it] / freeStroke }
    val summary = summariseDropoutDishing(
        sample, nominal, ensemble.meanSurvivors, T316_TOLERANCE
    )
    return T316Graded(nominal, summary.p90, summary.flatAtP90)
}

/** `C-0208`'s own published cell, keyed on every dimension its sweep varied. */
private fun t316Published(
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

// ------------------------------------------------------------------------------ the study

@Suppress("LongMethod", "ComplexMethod", "NestedBlockDepth")
fun main() {
    val shared = T316Shared(t316Profile(ResultInputs.T_3B.file()))
    val fractions = listOf(0.30, 0.26)
    val gradedColumns = listOf(1, 2, 3, 5)
    val d = Gen1Tile.INTERHELICAL_HONEYCOMB
    val rP = MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS
    val kTheta = Gen1Tile.crossoverHingeStiffness()
    val contact = T316_CONTACT_BP * Gen1Tile.RISE_PER_BASE_PAIR

    // The radial bracket is recomputed rather than transcribed, so the rungs are C-0208's own
    // Doubles and not its emitted nine-digit renderings -- otherwise every reproduction below
    // would be graded on a lattice C-0208 never built.
    val radial = crossoverRadialLinkBracket(
        hingeStiffness = kTheta,
        phosphateRadius = rP,
        interhelicalDistance = d,
        relaxedStep = MeasuredBackbone.STEP_SOUTH,
        stretchModulus = Gen1Tile.DUPLEX_STRETCH_MODULUS,
        equationOfState = MengMagnesium.equationOfState,
        contactLength = contact
    )
    val rungs = listOf(
        T316_SHEAR_CEILING to
                "the CONTROL -- radial = transverse = C-0205's own ceiling",
        radial.connectorAtImpliedStep to
                "CH-0259's own low candidate -- the connector at C-0194's implied step stiffness",
        radial.floor to "that connector candidate PLUS the measured pair term -- the BRACKET " +
                "FLOOR, and C-0208's tightest cell lives here",
        radial.connectorAtDuplexStretch to
                "CH-0259's own high candidate -- the duplex stretch modulus over the span",
        radial.ceiling to "the bracket CEILING -- that connector candidate plus the pair term"
    ).sortedBy { it.first }
    val floorRung = radial.floor
    fun throughThickness(constant: Double): Double =
        resolvedLinkStiffness(constant, T316_SHEAR_CEILING, unitY = 0.5, unitZ = SQRT_THREE_HALVES)

    val probe = T316Tile(shared, shared.enhancementAt(0.30), floorRung)
    val rootingHelixY = probe.lattice.faceBeams.map { probe.lattice.beamY[it] }
    val incorporation = measuredDepthIncorporation(shared.edgeX, shared.edgeY)
    val mandate = MANDATED_TOTAL_STIFFNESS

    /**
     * The largest stiffness one load path may carry — `C-0023`'s 10 pN unzip allowable over §3's
     * **acceptable** 3 nm stroke, which is the clause `C-0017`'s mandate is itself read at.
     *
     * A free distribution can meet a mandate on a SUM by putting almost all of it on a few paths,
     * and `CLAUDE.md` records that a mandate on a sum is not a mandate on each term. This is the
     * threshold that says so, and it is a THIRD one beside the flatness and the ratio.
     */
    val unzipCeiling = perPathStiffnessCeiling(
        Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE
    )

    // ================ Deliverable 1 -- the cheap bounds, before any search
    println("T-316 - the cheap bounds")
    val published = ResultInputs.T_310.file()
    val publishedEqual = t316Published(
        published, floorRung, 0.30, "abstract grid on the rooting helices", 5, "equal springs"
    )
    val publishedRim = t316Published(
        published, floorRung, 0.30, "abstract grid on the rooting helices", 5, "rim-graded 5:1"
    )
    val cheapBound = ArrayList<T316CheapBoundRow>()
    cheapBound += T316CheapBoundRow(
        question = "how far apart are C-0058's TWO transferred distributions at C-0208's " +
                "tightest cell, before anything is searched",
        answer = "the equal-spring reading is " + publishedEqual.emitted(9) + " and the " +
                "rim-graded one " + publishedRim.emitted(9) + " of the free stroke, both read " +
                "out of C-0208's own committed result file",
        value = publishedEqual / publishedRim,
        units = "dimensionless, a ratio of two 90th percentiles",
        consequence = "the one-dimensional family the corpus has always graded on is not flat, " +
                "and the " + ((publishedEqual / publishedRim - 1.0) * 100.0).emitted(3) + " % " +
                "between its own two members is larger than the 0.198 % the tightest cell " +
                "misses T-5b by. That is a COST bound and not an exclusion: it says the search " +
                "is worth running, and it is one division on a committed file."
    )
    cheapBound += T316CheapBoundRow(
        question = "what does one influence bank cost, and how many distributions does it serve",
        answer = "an InfluenceSurrogate is a property of the STRUCTURE -- the lattice, the " +
                "station set and the load -- and a distribution enters the Woodbury system as a " +
                "DIAGONAL, so one bank per (placement, columns, fraction, rung) serves every " +
                "distribution ever tried at that cell at one n x n Cholesky each",
        value = gradedColumns.sumOf { it * shared.rasterRows }.toDouble(),
        units = "point-load lattice solves per (fraction, rung) and per placement",
        consequence = "the search costs no new factorisation of the 4 080-degree-of-freedom " +
                "lattice, which is what makes this a study rather than a proposal"
    )
    cheapBound.forEach { println("  " + it.question + " -> " + it.value.emitted(6)) }

    // ================ Deliverable 2 -- the census, searched, at the bracket floor
    println("T-316 - the 32-cell searched census at the radial bracket floor, " +
            t316Realisations + " grading and " + t316TrainingRealisations + " training " +
            "realisations")
    val tiles = HashMap<Pair<Double, Double>, T316Tile>()
    rungs.forEach { (constant, _) ->
        fractions.forEach { fraction ->
            tiles[constant to fraction] =
                T316Tile(shared, shared.enhancementAt(fraction), constant)
        }
    }

    class T316Searched(
        val cell: T316Cell,
        val grid: List<Pair<Double, Double>>,
        val surrogate: InfluenceSurrogate,
        val stiffnesses: List<Double>,
        val transferred: List<Double>,
        val transferredLabel: String,
        val freeStroke: Double,
        val gradingEnsemble: DropoutEnsemble
    )

    fun searchOne(
        constant: Double,
        fraction: Double,
        placement: String,
        grid: List<Pair<Double, Double>>,
        columns: Int,
        gradingEnsemble: DropoutEnsemble,
        trainingEnsemble: DropoutEnsemble,
        subdivisions: Int = 1,
        searchSamples: Int = T316_SEARCH_SAMPLES,
        sweeps: Int = T316_PERCENTILE_SWEEPS
    ): T316Searched {
        val tile = if (subdivisions == 1) tiles.getValue(constant to fraction)
        else T316Tile(shared, shared.enhancementAt(fraction), constant, subdivisions)
        // Two banks and one lattice: the SEARCH runs on the coarse grid and every graded
        // percentile in the census is read on the 81 x 81 one C-0167, C-0180 and C-0208 use.
        val surrogate = tile.surrogate(grid, T316_SAMPLES)
        val searchSurrogate =
            if (searchSamples == T316_SAMPLES) surrogate else tile.surrogate(grid, searchSamples)
        val multi = honeycombMultiStateSurrogate(
            tile.lattice, grid, singleLoadState("C-0022's solved collar", shared.pressureField),
            searchSamples
        )
        val distributions = t316Distributions(grid, shared.edgeX, shared.edgeY)
        val gradedTransferred = distributions.map { (label, k) ->
            Triple(label, k, t316Grade(surrogate, k, tile.freeStroke, gradingEnsemble))
        }
        val best = gradedTransferred.minByOrNull { it.third.p90 }!!
        val searched = searchedStiffnessDistribution(
            smooth = multi,
            percentile = searchSurrogate,
            training = trainingEnsemble,
            freeStroke = tile.freeStroke,
            totalStiffness = mandate,
            transferred = distributions.map { it.second },
            percentileSweeps = sweeps,
            percentileScanPoints = T316_SCAN_POINTS,
            percentileRefinements = T316_REFINEMENTS,
            smoothingLevels = T316_SMOOTHING_LEVELS,
            smoothingIterations = T316_SMOOTHING_ITERATIONS,
            polishSweeps = T316_POLISH_SWEEPS
        )
        val outOfSample = t316Grade(
            surrogate, searched.stiffnesses, tile.freeStroke, gradingEnsemble
        )
        val nominalOutOfSample = t316Grade(
            surrogate, searched.nominalStiffnesses, tile.freeStroke, gradingEnsemble
        )
        val equal = gradedTransferred.first { it.first == "equal springs" }.third.p90
        val rim = gradedTransferred.first { it.first == "rim-graded 5:1" }.third.p90
        return T316Searched(
            cell = T316Cell(
                radialLinkStiffness = constant,
                throughThicknessLink = throughThickness(constant),
                compositeFraction = fraction,
                placement = placement,
                columns = columns,
                pathCount = grid.size,
                equalP90 = equal,
                rimGradedP90 = rim,
                bestTransferredP90 = best.third.p90,
                bestTransferredLabel = best.first,
                nominalSearchP90 = nominalOutOfSample.p90,
                nominalSearchNominal = searched.nominalObjective,
                nominalSearchRatio = searched.nominalRatio,
                searchedP90 = outOfSample.p90,
                searchedNominal = outOfSample.nominal,
                searchedRatio = searched.ratio,
                searchedPeakStiffness = searched.stiffnesses.max(),
                peakInsideUnzipCeiling = searched.stiffnesses.max() < unzipCeiling,
                searchedTrainingP90 = searched.trainingObjective,
                bestTransferredTrainingP90 = searched.bestTransferredTrainingObjective,
                inSampleGain = searched.bestTransferredTrainingObjective /
                        searched.trainingObjective,
                outOfSampleGain = best.third.p90 / outOfSample.p90,
                flatAtP90 = outOfSample.flat,
                bestTransferredFlatAtP90 = best.third.flat,
                ratioInsideBuildableWindow =
                    searched.ratio > BUILDABLE_RATIO_FLOOR && searched.ratio < BUILDABLE_RATIO_CEILING,
                uncoupledDishingOverStroke = tile.uncoupledDishing,
                beatsUncoupledAtP90 = outOfSample.p90 < tile.uncoupledDishing
            ),
            grid = grid,
            surrogate = surrogate,
            stiffnesses = searched.stiffnesses,
            transferred = best.second,
            transferredLabel = best.first,
            freeStroke = tile.freeStroke,
            gradingEnsemble = gradingEnsemble
        )
    }

    val searchedCells = ArrayList<T316Searched>()
    val oracle = ArrayList<T316OracleRow>()
    val startedAt = System.nanoTime()
    gradedColumns.forEach { columns ->
        t316Placements(shared, rootingHelixY, columns).forEach { (placement, grid) ->
            val probabilities = grid.map { (x, y) -> incorporation.at(x, y) }
            val gradingEnsemble =
                dropoutEnsemble(probabilities, t316Realisations, T316_GRADING_SEED)
            val trainingEnsemble =
                dropoutEnsemble(probabilities, t316TrainingRealisations, T316_TRAINING_SEED)
            fractions.forEach { fraction ->
                val found = searchOne(
                    floorRung, fraction, placement, grid, columns,
                    gradingEnsemble, trainingEnsemble
                )
                searchedCells += found
                // The oracle floor: a POINTWISE lower bound over every force vector, therefore
                // over every stiffness distribution whatever. It can EXCLUDE and never ADMIT.
                val floors = oracleFloorSample(found.surrogate, gradingEnsemble)
                floors.indices.forEach { floors[it] = floors[it] / found.freeStroke }
                val p90Floor = orderStatistic(floors, 0.90)
                oracle += T316OracleRow(
                    cell = "f = " + fraction.emitted(3) + ", " + placement + ", " + columns +
                            " x " + shared.rasterRows + " = " + grid.size + " paths",
                    placement = placement,
                    columns = columns,
                    pathCount = grid.size,
                    compositeFraction = fraction,
                    p90FloorOverStroke = p90Floor,
                    bestTransferredP90 = found.cell.bestTransferredP90,
                    bestTransferredOverFloor = found.cell.bestTransferredP90 / p90Floor,
                    excludesEveryDistributionAtP90 = p90Floor > T316_TOLERANCE
                )
            }
            // A wall clock belongs in the console and NEVER in the result file (`CLAUDE.md`).
            println("  " + columns + " x " + shared.rasterRows + "  " + placement.take(44) +
                    "  done, " + ((System.nanoTime() - startedAt) / 1_000_000_000L) + " s elapsed")
        }
    }
    val cells = searchedCells.map { it.cell }

    // ================ Deliverable 3 -- fragility and buildability, at the cells that matter
    println("T-316 - fragility and buildability")
    val tightestSearched = searchedCells.minByOrNull { it.cell.searchedP90 }!!
    val fragilityOf = (searchedCells.filter { it.cell.flatAtP90 } + tightestSearched)
        .distinctBy { it.cell.placement + "|" + it.cell.columns + "|" + it.cell.compositeFraction }
    val fragility = ArrayList<T316FragilityRow>()
    // The flat cells whose searched distribution loses the verdict to ONE missing path --
    // computed from the cell records themselves, never by matching a prose label, because
    // "abstract grid" is a prefix of "abstract grid on the rooting helices".
    var fragileFlat = 0
    fragilityOf.forEach { found ->
        val label = "f = " + found.cell.compositeFraction.emitted(3) + ", " +
                found.cell.placement + ", " + found.cell.columns + " x " + shared.rasterRows +
                " = " + found.cell.pathCount + " paths"
        listOf(
            "SEARCHED" to found.stiffnesses,
            found.transferredLabel + " (transferred)" to found.transferred
        ).forEach { (name, k) ->
            val graded = t316Grade(found.surrogate, k, found.freeStroke, found.gradingEnsemble)
            val worst = worstSinglePathRemoval(found.surrogate, k) / found.freeStroke
            val twoLevel = quantiseToLevels(k, 2, mandate)
            val twoLevelGraded =
                t316Grade(found.surrogate, twoLevel, found.freeStroke, found.gradingEnsemble)
            if (name == "SEARCHED" && found.cell.flatAtP90 && worst > T316_TOLERANCE) {
                fragileFlat += 1
            }
            fragility += T316FragilityRow(
                cell = label,
                distribution = name,
                p90OverStroke = graded.p90,
                nominalOverStroke = graded.nominal,
                worstSinglePathRemovalOverStroke = worst,
                amplification = worst / graded.nominal,
                ratio = stiffnessRatio(k),
                twoLevelRatio = stiffnessRatio(twoLevel),
                twoLevelP90OverStroke = twoLevelGraded.p90,
                twoLevelFlatAtP90 = twoLevelGraded.flat
            )
        }
    }

    // ================ Deliverable 4 -- the tightest cell at every radial rung
    println("T-316 - the tightest cell over C-0208's five radial rungs")
    val tightCell = tightestSearched.cell
    val tightGrid = t316Placements(shared, rootingHelixY, tightCell.columns)
        .first { it.first == tightCell.placement }.second
    val tightProbabilities = tightGrid.map { (x, y) -> incorporation.at(x, y) }
    val tightGrading = dropoutEnsemble(tightProbabilities, t316Realisations, T316_GRADING_SEED)
    val tightTraining =
        dropoutEnsemble(tightProbabilities, t316TrainingRealisations, T316_TRAINING_SEED)
    val rungRows = rungs.map { (constant, ground) ->
        val found = if (constant == floorRung) tightestSearched else searchOne(
            constant, tightCell.compositeFraction, tightCell.placement, tightGrid,
            tightCell.columns, tightGrading, tightTraining
        )
        T316RungRow(
            radialLinkStiffness = constant,
            throughThicknessLink = throughThickness(constant),
            ground = ground,
            cell = "f = " + tightCell.compositeFraction.emitted(3) + ", " + tightCell.placement +
                    ", " + tightCell.columns + " x " + shared.rasterRows + " = " +
                    tightCell.pathCount + " paths",
            bestTransferredP90 = found.cell.bestTransferredP90,
            searchedP90 = found.cell.searchedP90,
            searchedRatio = found.cell.searchedRatio,
            flatAtP90 = found.cell.flatAtP90
        )
    }
    rungRows.forEach {
        println("  k_radial " + it.radialLinkStiffness.emitted(9) + " -> searched " +
                it.searchedP90.emitted(9) + (if (it.flatAtP90) "  FLAT" else ""))
    }

    // ================ the census
    val flatTransferred = cells.count { it.bestTransferredFlatAtP90 }
    val flatSearched = cells.count { it.flatAtP90 }
    val census = listOf(
        T316CensusRow(
            statistic = "the 90th percentile of C-0087's measured staple dropout over " +
                    t316Realisations + " realisations of seed " + T316_GRADING_SEED +
                    ", against T-5b's " + T316_TOLERANCE.emitted(2),
            cellsGraded = cells.size,
            flatOnATransferredRule = flatTransferred,
            flatOnTheSearchedRule = flatSearched,
            tightestTransferredP90 = cells.minOf { it.bestTransferredP90 },
            tightestSearchedP90 = cells.minOf { it.searchedP90 },
            tightestSearchedCell = "f = " + tightCell.compositeFraction.emitted(3) + ", " +
                    tightCell.placement + ", " + tightCell.columns + " x " + shared.rasterRows +
                    " = " + tightCell.pathCount + " paths",
            searchedBeatsTransferredCells = cells.count { it.searchedP90 < it.bestTransferredP90 },
            worstOutOfSampleLoss = cells.maxOf { it.searchedP90 / it.bestTransferredP90 }
        )
    )
    println("  transferred: " + flatTransferred + " of " + cells.size + " flat; searched: " +
            flatSearched + " of " + cells.size)

    // ================ convergence, re-taken on the DECIDING quantity at the DECIDING cell
    println("T-316 - convergence at the deciding cell")
    val convergence = ArrayList<T316Convergence>()
    val decidingLabel = census[0].tightestSearchedCell
    listOf(T316_SAMPLES).forEach { samples ->
        val here = searchOne(
            floorRung, tightCell.compositeFraction, tightCell.placement, tightGrid,
            tightCell.columns, tightGrading, tightTraining, searchSamples = samples
        )
        convergence += T316Convergence(
            axis = "the dishing sample grid the SEARCH runs on, " + T316_SEARCH_SAMPLES +
                    " against " + samples + " (every graded percentile is read at " +
                    T316_SAMPLES + " throughout)",
            quantity = "the SEARCHED 90th percentile, out of sample",
            cell = decidingLabel,
            coarse = tightCell.searchedP90,
            fine = here.cell.searchedP90,
            departure = abs(here.cell.searchedP90 - tightCell.searchedP90) / tightCell.searchedP90,
            verdictMoves = here.cell.flatAtP90 != tightCell.flatAtP90,
            note = "C-0167 measures the free FIELD's own grid departure at 0.0 over 41/81/161; " +
                    "this is the same axis re-taken on the quantity a SEARCH returns, which " +
                    "carries an argmin and need not inherit it"
        )
    }
    listOf(41, 161).forEach { samples ->
        val tile = tiles.getValue(floorRung to tightCell.compositeFraction)
        val here = t316Grade(
            tile.surrogate(tightGrid, samples), tightestSearched.stiffnesses,
            tile.freeStroke, tightGrading
        )
        convergence += T316Convergence(
            axis = "the dishing sample grid the VERDICT is read on, " + T316_SAMPLES +
                    " against " + samples,
            quantity = "the searched distribution's 90th percentile, at a FIXED distribution",
            cell = decidingLabel,
            coarse = tightCell.searchedP90,
            fine = here.p90,
            departure = abs(here.p90 - tightCell.searchedP90) / tightCell.searchedP90,
            verdictMoves = here.flat != tightCell.flatAtP90,
            note = "the search's own grid is a separate axis above; this one holds the " +
                    "distribution fixed and moves only the quadrature the verdict is read on, " +
                    "which is C-0167's axis on C-0167's quantity"
        )
    }
    listOf(60, 240).forEach { count ->
        val training = dropoutEnsemble(
            tightProbabilities, if (System.getenv("T316_SMOKE") == "1") 20 else count,
            T316_TRAINING_SEED
        )
        val here = searchOne(
            floorRung, tightCell.compositeFraction, tightCell.placement, tightGrid,
            tightCell.columns, tightGrading, training
        )
        convergence += T316Convergence(
            axis = "the TRAINING realisations the search sees, " + t316TrainingRealisations +
                    " against " + count,
            quantity = "the SEARCHED 90th percentile, graded OUT OF SAMPLE on the same " +
                    t316Realisations + "-realisation grading ensemble",
            cell = decidingLabel,
            coarse = tightCell.searchedP90,
            fine = here.cell.searchedP90,
            departure = abs(here.cell.searchedP90 - tightCell.searchedP90) / tightCell.searchedP90,
            verdictMoves = here.cell.flatAtP90 != tightCell.flatAtP90,
            note = "an IN-SAMPLE percentile optimum is not a result; this is the convergence of " +
                    "the training ensemble the search sees, read on the independent one"
        )
    }
    run {
        val here = searchOne(
            floorRung, tightCell.compositeFraction, tightCell.placement, tightGrid,
            tightCell.columns, tightGrading, tightTraining, subdivisions = 2
        )
        convergence += T316Convergence(
            axis = "beam subdivisions, 1 against 2",
            quantity = "the SEARCHED 90th percentile, out of sample",
            cell = decidingLabel,
            coarse = tightCell.searchedP90,
            fine = here.cell.searchedP90,
            departure = abs(here.cell.searchedP90 - tightCell.searchedP90) / tightCell.searchedP90,
            verdictMoves = here.cell.flatAtP90 != tightCell.flatAtP90,
            note = "C-0205's own F8 fired on this axis at 21 % of a THRESHOLD and at nothing " +
                    "in the verdict; here the whole search is re-run on the refined lattice"
        )
    }
    run {
        val sweeps = searchOne(
            floorRung, tightCell.compositeFraction, tightCell.placement, tightGrid,
            tightCell.columns, tightGrading, tightTraining, sweeps = T316_PERCENTILE_SWEEPS + 1
        )
        convergence += T316Convergence(
            axis = "the percentile descent's sweeps, " + T316_PERCENTILE_SWEEPS + " against " +
                    (T316_PERCENTILE_SWEEPS + 1),
            quantity = "the SEARCHED 90th percentile, out of sample",
            cell = decidingLabel,
            coarse = tightCell.searchedP90,
            fine = sweeps.cell.searchedP90,
            departure = abs(sweeps.cell.searchedP90 - tightCell.searchedP90) /
                    tightCell.searchedP90,
            verdictMoves = sweeps.cell.flatAtP90 != tightCell.flatAtP90,
            note = "a descent reports the best point it FOUND; this is how much of the answer " +
                    "is the budget rather than the objective"
        )
    }
    convergence.forEach {
        println("  " + it.axis + "  departure " + it.departure.emitted(2) +
                (if (it.verdictMoves) "  VERDICT MOVES" else ""))
    }

    // ================ the reproductions: C-0208's own published cells, at every graded cell
    println("T-316 - reproducing C-0208's transferred cells")
    val reproductions = ArrayList<T316Reproduction>()
    cells.forEach { cell ->
        listOf(
            "equal springs" to cell.equalP90,
            "rim-graded 5:1" to cell.rimGradedP90
        ).forEach { (label, here) ->
            val there = t316Published(
                published, floorRung, cell.compositeFraction, cell.placement, cell.columns, label
            )
            reproductions += T316Reproduction(
                statement = "C-0208's p90 at f = " + cell.compositeFraction.emitted(3) + ", " +
                        cell.placement + ", " + cell.columns + " x " + shared.rasterRows +
                        ", " + label,
                published = there,
                here = here,
                relativeDeparture = abs(here - there) / there,
                source = ResultInputs.T_310.path
            )
        }
    }
    val worstReproduction = reproductions.maxOf { it.relativeDeparture }
    println("  worst reproduction departure over " + reproductions.size + ": " +
            worstReproduction.emitted(2))

    // ================ the falsifiers
    val uniformField = probe.lattice.solve(uniformPressure(shared.interiorPressure))
    val uniformDishing = uniformField.peakDishing(T316_SAMPLES) / uniformField.meanDeflection
    val standing = HoneycombGrillage(shared.block, shared.rowBasePairs, Gen1Tile.FOUNDATION_SECANT)
    val defaulted = HoneycombGrillage(
        shared.block, shared.rowBasePairs, Gen1Tile.FOUNDATION_SECANT, radialLinkStiffness = null
    )
    val standingLoad = standing.assembleLoad(uniformPressure(0.01))
    val defaultedLoad = defaulted.assembleLoad(uniformPressure(0.01))
    var loadIdentical = standing.degreesOfFreedom == defaulted.degreesOfFreedom
    for (i in 0 until standing.degreesOfFreedom) {
        if (standingLoad[i] != defaultedLoad[i]) loadIdentical = false
    }
    val siteSetIdentical = standing.bonds.map { it.site } == defaulted.bonds.map { it.site }

    // the Woodbury reproduction, at the deciding cell, taken on the ASSEMBLED lattice
    val assembledDeparture = run {
        val tile = tiles.getValue(floorRung to tightCell.compositeFraction)
        val response = tightestSearched.surrogate.solve(tightestSearched.stiffnesses)
        val assembled = tile.lattice.solve(
            shared.pressureField,
            tightGrid.mapIndexed { index, (s, y) ->
                com.xemantic.nano.plentyofroom.structure.PointLoad(
                    s, y, -response.supportForces[index]
                )
            }
        ).peakDishing(T316_SAMPLES)
        abs(assembled - response.peakDishing) / abs(response.peakDishing)
    }

    // the two surrogates must be the same object, or the two halves search two lattices
    val surrogateDeparture = run {
        val tile = tiles.getValue(floorRung to tightCell.compositeFraction)
        val multi = honeycombMultiStateSurrogate(
            tile.lattice, tightGrid,
            singleLoadState("C-0022's solved collar", shared.pressureField), T316_SAMPLES
        )
        val a = tightestSearched.surrogate.solve(tightestSearched.stiffnesses).peakDishing
        val b = multi.peakDishing(tightestSearched.stiffnesses)[0]
        abs(a - b) / abs(a)
    }

    val floorViolations = oracle.count { row ->
        val here = cells.first {
            it.placement == row.placement && it.columns == row.columns &&
                    it.compositeFraction == row.compositeFraction
        }
        here.searchedP90 < row.p90FloorOverStroke * (1.0 - 1e-9)
    }
    val inSampleLosses = cells.count {
        it.searchedTrainingP90 > it.bestTransferredTrainingP90 * (1.0 + 1e-12)
    }
    val outOfSampleLosses = cells.count { it.searchedP90 > it.bestTransferredP90 }
    val flatOutsideWindow = cells.count { it.flatAtP90 && !it.ratioInsideBuildableWindow }
    val rungVerdictMoves = rungRows.map { it.flatAtP90 }.distinct().size > 1

    val falsifiers = listOf(
        T316Falsifier(
            id = "F1",
            statement = "a distribution SEARCHED at the resolved per-bond link puts at least " +
                    "one coupled cell inside T-5b's " + T316_TOLERANCE.emitted(2) +
                    " at the 90th percentile of the grading ensemble",
            declaredOpen = true,
            fired = flatSearched > 0,
            note = flatSearched.toString() + " of " + cells.size + " searched cells are flat " +
                    "at the 90th percentile, against " + flatTransferred + " on a transferred " +
                    "rule; the tightest searched reading is " +
                    census[0].tightestSearchedP90.emitted(9) + " against C-0208's " +
                    census[0].tightestTransferredP90.emitted(9)
        ),
        T316Falsifier(
            id = "F2",
            statement = "a cell that clears T-5b does so at a max/min stiffness ratio OUTSIDE " +
                    "C-0060's measured buildable window [" + BUILDABLE_RATIO_FLOOR.emitted(2) +
                    ", " + BUILDABLE_RATIO_CEILING.emitted(2) + "]",
            declaredOpen = true,
            fired = flatOutsideWindow > 0,
            note = if (flatSearched == 0) "vacuous: no searched cell clears T-5b, so the " +
                    "window has nothing to refuse. The tightest searched cell's own ratio is " +
                    tightCell.searchedRatio.emitted(9) +
                    (if (tightCell.ratioInsideBuildableWindow) ", inside the window"
                    else ", OUTSIDE the window") +
                    " -- reported because a ratio is a property of the argmin whether or not " +
                    "the flatness verdict reads it"
            else flatOutsideWindow.toString() + " of " + flatSearched + " flat cells sit " +
                    "outside C-0060's window"
        ),
        T316Falsifier(
            id = "F3",
            statement = "the searched distribution's OUT-OF-SAMPLE p90 is worse than the best " +
                    "transferred distribution's at any cell -- an over-fit",
            declaredOpen = true,
            fired = outOfSampleLosses > 0,
            note = outOfSampleLosses.toString() + " of " + cells.size + " cells lose out of " +
                    "sample, worst by " + census[0].worstOutOfSampleLoss.emitted(9) + " times; " +
                    "in sample the search wins at " + (cells.size - inSampleLosses) + " of " +
                    cells.size + " by construction"
        ),
        T316Falsifier(
            id = "F4",
            statement = "the searched distribution's IN-SAMPLE training objective is worse " +
                    "than the best of its own starts at any cell",
            declaredOpen = false,
            fired = inSampleLosses > 0,
            note = "the percentile descent is seeded from the transferred distributions AND " +
                    "from the smoothed minimax's answer, and evaluates every start before " +
                    "moving from it, so this is a property of the composition; " +
                    inSampleLosses.toString() + " losses"
        ),
        T316Falsifier(
            id = "F5",
            statement = "the searched p90 falls BELOW the oracle p90 floor at any cell",
            declaredOpen = false,
            fired = floorViolations > 0,
            note = floorViolations.toString() + " violations of a pointwise theorem over " +
                    oracle.size + " cells"
        ),
        T316Falsifier(
            id = "F6",
            statement = "a uniform pressure on the free honeycomb lattice at the resolved link " +
                    "does not dish exactly zero, at < 1e-9 of the free stroke",
            declaredOpen = false,
            fired = uniformDishing > 1e-9,
            note = "peak dishing over the free stroke is " + uniformDishing.emitted(2)
        ),
        T316Falsifier(
            id = "F7",
            statement = "the default lattice is not bit-identical to the standing object at " +
                    "assembleLoad over every degree of freedom, or its crossover site set differs",
            declaredOpen = false,
            fired = !loadIdentical || !siteSetIdentical,
            note = "this task edits no shared source and asserts it anyway: " +
                    standing.degreesOfFreedom + " degrees of freedom compared, " +
                    standing.bonds.size + " bond sites"
        ),
        T316Falsifier(
            id = "F8",
            statement = "the surrogate at full presence does not reproduce the ASSEMBLED solve " +
                    "with its own Woodbury support forces applied as point loads, at < 1e-9",
            declaredOpen = false,
            fired = assembledDeparture > 1e-9,
            note = "departure " + assembledDeparture.emitted(2) + " at the deciding cell, on " +
                    "the SEARCHED distribution rather than on a transferred one"
        ),
        T316Falsifier(
            id = "F9",
            statement = "the one-state MultiStateSurrogate and the InfluenceSurrogate disagree " +
                    "about the peak dishing of one distribution by more than 1e-10 relative",
            declaredOpen = false,
            fired = surrogateDeparture > 1e-10,
            note = "departure " + surrogateDeparture.emitted(2) + "; the smoothed search and " +
                    "the grading must read the same bank"
        ),
        T316Falsifier(
            id = "F10",
            statement = "the two transferred distributions do not reproduce C-0208's own " +
                    "published p90 at every cell of the bracket-floor rung",
            declaredOpen = false,
            fired = worstReproduction > 1e-6,
            note = "worst of " + reproductions.size + " reproductions is " +
                    worstReproduction.emitted(2)
        ),
        T316Falsifier(
            id = "F11",
            statement = "a cell that clears at the 90th percentile still clears when its worst " +
                    "SINGLE path is removed",
            declaredOpen = true,
            fired = flatSearched > 0 && fragileFlat == 0,
            note = if (flatSearched == 0) "vacuous: no searched cell clears. The tightest " +
                    "searched cell's worst single-path removal is " +
                    (fragility.firstOrNull { it.distribution == "SEARCHED" }
                        ?.worstSinglePathRemovalOverStroke?.emitted(9) ?: "not graded") +
                    " of the stroke against a nominal of " +
                    tightCell.searchedNominal.emitted(9)
            else "of the flat cells, " + fragileFlat + " lose the verdict to one missing path"
        ),
        T316Falsifier(
            id = "F12",
            statement = "two independent runs of the study do not produce a byte-identical " +
                    "result file",
            declaredOpen = false,
            fired = false,
            note = "asserted OUTSIDE the run, by diffing two emissions; every search decision " +
                    "is taken through searchDecision at six significant digits and no field of " +
                    "this file counts a step or a second"
        ),
        T316Falsifier(
            id = "F14",
            statement = "a cell that clears T-5b does so with a single-path stiffness above " +
                    "C-0023's unzip allowable read over section 3's acceptable stroke, " +
                    unzipCeiling.emitted(9) + " pN/nm -- a THIRD threshold the moving quantity " +
                    "feeds, and one a mandate on a SUM does not constrain",
            declaredOpen = true,
            fired = cells.count { it.flatAtP90 && !it.peakInsideUnzipCeiling } > 0,
            note = cells.count { it.flatAtP90 && !it.peakInsideUnzipCeiling }.toString() +
                    " of " + flatSearched + " flat cells demand more than " +
                    unzipCeiling.emitted(9) + " pN/nm on one path; the tightest searched cell's " +
                    "peak is " + tightCell.searchedPeakStiffness.emitted(9) + " pN/nm against a " +
                    "uniform share of " + (mandate / tightCell.pathCount).emitted(9)
        ),
        T316Falsifier(
            id = "F13",
            statement = "the verdict at the tightest cell moves across C-0208's five radial " +
                    "rungs",
            declaredOpen = true,
            fired = rungVerdictMoves,
            note = rungRows.joinToString("; ") {
                it.radialLinkStiffness.emitted(9) + " -> " + it.searchedP90.emitted(9) +
                        (if (it.flatAtP90) " FLAT" else " not flat")
            }
        )
    )
    falsifiers.forEach { println("  " + it.id + (if (it.fired) "  FIRED" else "  did not fire")) }

    // ================ the verdict, the findings and the emission
    val tightestGainPercent =
        (census[0].tightestTransferredP90 / census[0].tightestSearchedP90 - 1.0) * 100.0
    val missPercent = (census[0].tightestSearchedP90 / T316_TOLERANCE - 1.0) * 100.0

    val verdict = mapOf(
        "does a SEARCHED distribution clear T-5b at the 90th percentile" to
                (if (flatSearched > 0) "YES at " + flatSearched + " of " + cells.size + " cells"
                else "NO -- " + flatSearched + " of " + cells.size + ", and the tightest " +
                        "searched cell reads " + census[0].tightestSearchedP90.emitted(9) +
                        ", which misses T-5b's " + T316_TOLERANCE.emitted(2) + " by " +
                        missPercent.emitted(3) + " %"),
        "what the search is worth at the tightest cell" to
                census[0].tightestTransferredP90.emitted(9) + " transferred against " +
                census[0].tightestSearchedP90.emitted(9) + " searched, a gain of " +
                tightestGainPercent.emitted(3) + " % against the " +
                ((census[0].tightestTransferredP90 / T316_TOLERANCE - 1.0) * 100.0).emitted(3) +
                " % C-0208's tightest cell had to close",
        "the stiffness ratio the argmin demands at the tightest cell" to
                tightCell.searchedRatio.emitted(9) + " against C-0060's measured buildable " +
                "window [" + BUILDABLE_RATIO_FLOOR.emitted(2) + ", " +
                BUILDABLE_RATIO_CEILING.emitted(2) + "], " +
                (if (tightCell.ratioInsideBuildableWindow) "INSIDE" else "OUTSIDE") + " it",
        "the in-sample and out-of-sample gain" to
                "in sample the search wins at " + (cells.size - inSampleLosses) + " of " +
                cells.size + " cells by construction and out of sample at " +
                census[0].searchedBeatsTransferredCells + "; the worst out-of-sample reading is " +
                census[0].worstOutOfSampleLoss.emitted(9) + " times the transferred one",
        "the oracle floor" to "the p90 dishing floor over EVERY force vector runs " +
                oracle.minOf { it.p90FloorOverStroke }.emitted(9) + " to " +
                oracle.maxOf { it.p90FloorOverStroke }.emitted(9) + " of the stroke and " +
                "excludes " + oracle.count { it.excludesEveryDistributionAtP90 } + " of " +
                oracle.size + " cells outright; the best transferred distribution sits " +
                oracle.minOf { it.bestTransferredOverFloor }.emitted(9) + " to " +
                oracle.maxOf { it.bestTransferredOverFloor }.emitted(9) + " times above it, " +
                "which is why a floor can exclude and can never admit",
        "does the verdict move across the five radial rungs" to
                (if (rungVerdictMoves) "YES" else "NO -- " + rungRows.joinToString("; ") {
                    it.radialLinkStiffness.emitted(9) + " -> " + it.searchedP90.emitted(9)
                }),
        "does C-0208's 0 of 64 stand once the distribution is searched" to
                (if (flatSearched > 0) "NO -- it reverses at " + flatSearched + " cells"
                else "YES -- the last axis the corpus had not opened is open now and it does " +
                        "not close the " + ((0.100198485 / T316_TOLERANCE - 1.0) * 100.0)
                    .emitted(3) + " % C-0208 left")
    )

    val findings = listOf(
        "A distribution SEARCHED at the resolved per-bond link reads " +
                census[0].tightestSearchedP90.emitted(9) + " at its tightest cell against the " +
                "transferred " + census[0].tightestTransferredP90.emitted(9) + " -- a gain of " +
                tightestGainPercent.emitted(3) + " % on the deciding statistic -- and " +
                flatSearched + " of " + cells.size + " cells are flat at the 90th percentile.",
        "The search is worth its cost IN SAMPLE at " + (cells.size - inSampleLosses) + " of " +
                cells.size + " cells by construction and OUT OF SAMPLE at " +
                census[0].searchedBeatsTransferredCells + " of " + cells.size + ". The gap " +
                "between the two is what a percentile optimised on a finite training stream " +
                "costs, and it is emitted per cell rather than argued.",
        "The oracle p90 floor -- a rigorous lower bound over every force vector and therefore " +
                "over every distribution whatever -- excludes " +
                oracle.count { it.excludesEveryDistributionAtP90 } + " of " + oracle.size +
                " cells before any search runs, and the best transferred distribution sits " +
                oracle.minOf { it.bestTransferredOverFloor }.emitted(9) + " to " +
                oracle.maxOf { it.bestTransferredOverFloor }.emitted(9) + " times above it. " +
                "CLAUDE.md's statement that such a floor can exclude and can never admit is " +
                "reproduced here on a second lattice.",
        "The ratio the argmin demands is a threshold the flatness falsifier cannot see. At the " +
                "tightest searched cell it is " + tightCell.searchedRatio.emitted(9) +
                " against C-0060's measured buildable [" + BUILDABLE_RATIO_FLOOR.emitted(2) +
                ", " + BUILDABLE_RATIO_CEILING.emitted(2) + "], " +
                (if (tightCell.ratioInsideBuildableWindow) "inside" else "OUTSIDE") + " it -- " +
                "and quantised onto C-0060's own two levels the same distribution reads " +
                (fragility.firstOrNull { it.distribution == "SEARCHED" }
                    ?.twoLevelP90OverStroke?.emitted(9) ?: "not graded") + ".",
        "The verdict is a property of the question and not of one radial rung: over C-0208's " +
                "five rungs the searched tightest cell reads " + rungRows.joinToString("; ") {
            it.searchedP90.emitted(9)
        } + ", and the flat count moves " + (if (rungVerdictMoves) "" else "not at all") + "."
    )

    val validity = listOf(
        "The search is over the STIFFNESS vector alone. The placement, the cross-section, the " +
                "raster, the load case, the link resolution and the radial bracket are all " +
                "C-0208's and none of them moves here.",
        "C-0017's mandate is an EQUALITY ON THE SUM, so every distribution graded here spends " +
                "the same total. A search that were allowed to spend more would not be " +
                "answering this question, and CLAUDE.md records that a mandate on a sum is not " +
                "a mandate on each term.",
        "The percentile is an ORDER STATISTIC, so C-0135's log-sum-exp smoothing and its " +
                "adjoint gradient do NOT transfer to it. They are applied here to the " +
                "zero-defect peak, which IS a max of smooth functions, and the percentile is " +
                "searched by a multi-start coordinate descent with every decision rounded at " +
                "six significant digits. A smoothed CVaR of a smoothed peak would be " +
                "differentiable throughout and is named as an open question, not attempted.",
        "A descent reports the best point it FOUND. The sweep budget is emitted as a " +
                "convergence axis so a reader can see how much the search still had left, and " +
                "the oracle floor is emitted beside every cell as the honest statement of how " +
                "much room any distribution could still have.",
        "The training ensemble is " + t316TrainingRealisations + " realisations of seed " +
                T316_TRAINING_SEED + " and the grading one " + t316Realisations + " of seed " +
                T316_GRADING_SEED + ". Every percentile in the census is read on the grading " +
                "stream and is therefore out of sample; the training reading is emitted beside " +
                "it and is in sample by construction.",
        "The census is taken on ROUTE A, whose raster turns carry ZERO unpaired nucleotides " +
                "(C-0175's modelling choice). C-0193 and C-0200 establish that the only folded " +
                "block of this cross-section does otherwise, so the whole census is a " +
                "statement about a design nobody has folded.",
        "The transverse constant is pinned at C-0205's ceiling throughout, which is its " +
                "generous reading, and C-0208 records that the measured pair term would lower " +
                "it.",
        "The COUNT of flat cells is out of sample; WHICH cell is tightest is an order statistic " +
                "over 32 cells read on the grading stream, so the tightest value carries a " +
                "selection the count does not. Quote the count as the result and the tightest " +
                "cell as an identification.",
        "A mandate on a SUM does not bound a single path, so the peak per-path stiffness and " +
                "the max/min ratio are emitted at every cell against C-0023's unzip allowable " +
                "over section 3's acceptable stroke and against C-0060's measured window. " +
                "C-0060's window is itself a SQUARE-LATTICE, 45-station, one-parameter-rim-rule " +
                "measurement and its transfer to a free distribution on a honeycomb face is a " +
                "transfer nobody has checked."
    )

    val openQuestions = listOf(
        "Whether a smoothed CVaR of a log-sum-exp -- convex in the sampled field, an upper " +
                "bound on the percentile, and differentiable throughout -- reaches a better " +
                "point than the multi-start coordinate descent used here. Its adjoint needs one " +
                "triangular solve per realisation, so it is affordable and it is a study.",
        "Whether a search over the PLACEMENT and the distribution together reaches what neither " +
                "reaches alone. C-0063 records that which stations a coupling enters at is " +
                "worth more than how its stiffness is distributed, and the two have never been " +
                "searched jointly on this lattice.",
        "What a searched distribution does on ROUTE B, whose turns carry 28 unpaired " +
                "nucleotides and whose link C-0207 reads at the standing penalty (T-315).",
        "Whether the shared-body topology -- C-0017's mandate spent once in a rigid-body mode " +
                "rather than at every station -- moves the census the way it moved the " +
                "square-lattice one, which is a change of TOPOLOGY and not of distribution."
    )

    val result = T316Result(
        task = "T-316",
        claim = "C-0212",
        leaf = "A8.2",
        question = "Every coupled cell of every census in this corpus is graded on C-0058's " +
                "equal springs and its rim-graded 5:1, both of them rules TRANSFERRED onto the " +
                "lattice rather than optima OF it. Does a distribution SEARCHED at C-0208's " +
                "resolved per-bond link close the 0.198 % its tightest cell misses T-5b by?",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated. " +
                "Every number here is a property of one lattice, one placement family, one " +
                "raster, one load case and one dropout model, and the radial constant it is " +
                "read at is a bracket of constructions with one measured term in it (C-0208).",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm",
            "pressure" to "pN/nm^2 = 1 MPa",
            "dishing" to "dimensionless, as a fraction of the free stroke",
            "ratio" to "dimensionless, max over min of a per-path stiffness vector"
        ),
        conventions = mapOf(
            "s" to "along the helices, origin at the face centre",
            "y" to "across the helices in the plane of the face, origin at the face centre",
            "W" to "positive DOWNWARD, toward the electrode (C-0006)",
            "dishing" to "the peak of |w - the best-fit rigid plane| over an 81 x 81 face grid, " +
                    "divided by the free-tile stroke",
            "resolvedLink" to "k_radial * unitZ^2 + k_transverse * unitY^2 per bond (C-0208), " +
                    "with k_transverse pinned at C-0205's ceiling",
            "transferred" to "a distribution RULE evaluated on the station set -- C-0058's " +
                    "equal springs and its rim-graded 5:1 at a 6.7 nm band",
            "searched" to "the composition of C-0135's smoothed minimax on the zero-defect peak " +
                    "and a multi-start coordinate descent on the TRUE training percentile, " +
                    "every decision rounded at six significant digits",
            "outOfSample" to "graded on the " + T316_GRADING_SEED + " stream, which the search " +
                    "never sees; the search sees the " + T316_TRAINING_SEED + " stream",
            "ratio" to "max/min over the per-path stiffnesses, the axis C-0060 measures a " +
                    "buildable window on"
        ),
        parameters = mapOf(
            "crossSection" to shared.crossSection,
            "rowBasePairs" to shared.rowBasePairs.toString(),
            "edgeX" to shared.edgeX.emitted(9),
            "edgeY" to shared.edgeY.emitted(9),
            "interhelicalDistance" to d.emitted(9),
            "hingeStiffness" to kTheta.emitted(9),
            "transverseConstant" to T316_SHEAR_CEILING.emitted(9),
            "radialBracketFloor" to radial.floor.emitted(9),
            "radialBracketCeiling" to radial.ceiling.emitted(9),
            "radialPairTerm" to radial.pairRadial.emitted(9),
            "throughThicknessLinkAtTheFloor" to throughThickness(radial.floor).emitted(9),
            "inPlaneLink" to T316_SHEAR_CEILING.emitted(9),
            "foundationStiffness" to Gen1Tile.FOUNDATION_SECANT.emitted(9),
            "compositeFractions" to "0.30 and 0.26 (C-0116)",
            "mandate" to ("C-0017's " + mandate.emitted(9) + " pN/nm on the SUM, section 3's " +
                    "acceptable clause"),
            "gradingRealisations" to t316Realisations.toString(),
            "gradingSeed" to T316_GRADING_SEED.toString(),
            "trainingRealisations" to t316TrainingRealisations.toString(),
            "trainingSeed" to T316_TRAINING_SEED.toString(),
            "samples" to T316_SAMPLES.toString(),
            "searchSamples" to T316_SEARCH_SAMPLES.toString(),
            "smoothingLevels" to T316_SMOOTHING_LEVELS.joinToString(", ") { it.emitted(3) },
            "smoothingIterationsPerLevel" to T316_SMOOTHING_ITERATIONS.toString(),
            "smoothedMinimaxPolishSweeps" to T316_POLISH_SWEEPS.toString(),
            "percentileScanPoints" to T316_SCAN_POINTS.toString(),
            "percentileRefinements" to T316_REFINEMENTS.toString(),
            "tolerance" to T316_TOLERANCE.emitted(2),
            "rimBand" to T316_RIM_BAND.emitted(3),
            "raster" to (T316_RECOMMENDED_ONE.toString() + " / " + T316_RECOMMENDED_TWO +
                    " (C-0151, drawable)"),
            "percentileSweeps" to T316_PERCENTILE_SWEEPS.toString(),
            "buildableRatioWindow" to (BUILDABLE_RATIO_FLOOR.emitted(2) + " to " +
                    BUILDABLE_RATIO_CEILING.emitted(2) + " (C-0060, MEASURED)")
        ),
        sources = listOf(
            ResultInputs.T_3B.path + " (C-0022's solved collar at 2 mM / 10 nm / 0.192 V)",
            ResultInputs.T_310.path + " (C-0208's own coupled cells at the resolved link, " +
                    "read and reproduced at every graded cell)"
        ),
        citedInputs = mapOf(
            "C-0208 tightest p90 at the radial bracket floor" to "0.100198485",
            "C-0208 census at every radial rung" to "0 of 64",
            "C-0205 shear ceiling, the transverse constant" to "254.808095 pN/nm",
            "C-0060 measured buildable stiffness-ratio window" to "3.5 to 20",
            "C-0058 measured gap between a projected and a searched design" to "24.9 %",
            "T-5b flatness tolerance" to "0.10 of the free stroke"
        ),
        cheapBound = cheapBound,
        oracle = oracle,
        cells = cells,
        fragility = fragility,
        rungs = rungRows,
        census = census,
        verdict = verdict,
        convergence = convergence,
        reproductions = reproductions,
        falsifiers = falsifiers,
        findings = findings,
        validity = validity,
        openQuestions = openQuestions,
        proseFailure = "none"
    )

    val output = File("gpd/results/T-316-a-searched-distribution-at-the-resolved-link.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digits = 9,
                digitsByKey = mapOf(
                    "convergence/departure" to DEPARTURE_SIGNIFICANT_DIGITS,
                    "reproductions/relativeDeparture" to DEPARTURE_SIGNIFICANT_DIGITS
                ),
                floor = 1e-12
            ).withEmissionHeader(LatticeTag.HONEYCOMB, null) as JsonObject)
        ) + "\n"
    )
    println("T-316 - wrote " + output.path)
}
