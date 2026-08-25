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
import com.xemantic.nano.plentyofroom.coupling.oracleFloorSample
import com.xemantic.nano.plentyofroom.coupling.orderStatistic
import com.xemantic.nano.plentyofroom.coupling.perPathStiffnessCeiling
import com.xemantic.nano.plentyofroom.coupling.quantiseToLevels
import com.xemantic.nano.plentyofroom.coupling.summariseDropoutDishing
import com.xemantic.nano.plentyofroom.coupling.worstSinglePathRemoval
import com.xemantic.nano.plentyofroom.electrostatics.MengMagnesium
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.DEPARTURE_SIGNIFICANT_DIGITS
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import com.xemantic.nano.plentyofroom.structure.maximumUniformRowLength
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
// T-322 -- route B's own buildable widths, graded COUPLED, on stations derived at each row length.
//
// C-0211 graded route B's uniform raster FREE at C-0208's resolved per-bond link: 756 of 756
// cells flat.  Every COUPLED number in this corpus -- C-0167's 64, C-0180's, C-0205's, C-0208's
// and C-0212's 32 -- is read on the 116 bp block extent of the drawable 102 / 109 two-length
// raster, with 59 covalent TIES rather than 59 tethers and 435 staple bonds rather than route B's
// 358 / 385 / 410.  So the corpus holds a flat free tile and an unflat coupled one, and they are
// not the same object.
//
// The question is therefore inverted from C-0208's: not "can a coupling be made flat" but "does
// attaching the MANDATED coupling to route B's own tile destroy the flatness it already has".
// C-0017's 33.3333333 pN/nm is not optional; the flatness, on route B, is free.
// ---------------------------------------------------------------------------------------------

private const val T322_SAMPLES: Int = 81

private const val T322_SEARCH_SAMPLES: Int = 41

private const val T322_TOLERANCE: Double = 0.10

private const val T322_RIM_STANDOFF: Double = 1.0

private const val T322_RIM_BAND: Double = 6.7

/** `C-0205`'s shear ceiling -- the transverse constant every rung here is read at. */
private const val T322_SHEAR_CEILING: Double = 254.80809548301096

private const val T322_PENALTY: Double = 10_000.0

/** `unitZ` at a bond running through the thickness -- `sqrt(3)/2`, so `unitZ^2 = 0.75`. */
private const val SQRT_THREE_HALVES_T322: Double = 0.8660254037844386

/** The crossover span `C-0208`'s radial bracket is read over, in base pairs. */
private const val T322_CONTACT_BP: Double = 21.0

/** `C-0141`'s forced inter-row stagger at the headline -- `T-316`'s, so the censuses are paired. */
private const val T322_LADDER_STAGGER: Int = 14

/** The other admissible stagger, carried at the deciding cell as a sensitivity. */
private const val T322_ALTERNATE_STAGGER: Int = 7

private const val T322_HELICES: Int = 60

private const val T322_UNPAIRED_PER_HELIX: Int = 28

private const val T322_M13: Int = 7249

private const val T322_P7560: Int = 7560

private const val T322_P8064: Int = 8064

/** `C-0116`'s calibrated composite fraction, this study's headline. */
private const val T322_FRACTION: Double = 0.30

/** The other reading of the same calibration, carried at the deciding cell only. */
private const val T322_ALTERNATE_FRACTION: Double = 0.26

/** `C-0060`'s measured **FLAT** ratio window -- `CH-0273`: it is not a buildability constraint. */
private const val T322_FLAT_RATIO_FLOOR: Double = 3.5

private const val T322_FLAT_RATIO_CEILING: Double = 20.0

/** `T-316`'s inherited ladder phase, a **route-A** number -- `F12` is declared on it. */
private const val T322_INHERITED_PHASE: Int = 16

private const val T322_GRADING_SEED: Long = 197_197L

private const val T322_TRAINING_SEED: Long = 316_316L

private const val T322_PERCENTILE_SWEEPS: Int = 2

private const val T322_SCAN_POINTS: Int = 5

private const val T322_REFINEMENTS: Int = 6

private val T322_SMOOTHING_LEVELS: List<Double> = listOf(0.3, 0.1, 0.03, 0.01)

private const val T322_SMOOTHING_ITERATIONS: Int = 12

private const val T322_POLISH_SWEEPS: Int = 2

private val t322Smoke: Boolean = System.getenv("T322_SMOKE") == "1"

private val t322Realisations: Int = if (t322Smoke) 150 else 4000

private val t322TrainingRealisations: Int = if (t322Smoke) 40 else 120

private val t322Columns: List<Int> = if (t322Smoke) listOf(1, 3) else listOf(1, 2, 3, 5)

private fun Double.emitted(digits: Int = 9): String = roundedForProse(digits).toString()

// ------------------------------------------------------------------------------ the records

@Serializable
private class T322CheapBoundRow(
    val question: String,
    val answer: String,
    val value: Double,
    val units: String,
    val consequence: String
)

@Serializable
private class T322LadderRow(
    val pairedRowBasePairs: Int,
    val interRowOffsetBasePairs: Int,
    val derivedPhase: Int,
    val minimumStationsPerRow: Int,
    val stationsAtTheInheritedPhase: Int,
    val carriesFiveColumnsAtTheInheritedPhase: Boolean,
    val maximumColumns: Int,
    val note: String
)

@Serializable
private class T322PredictionRow(
    val pairedRowBasePairs: Int,
    val rule: String,
    val uncoupledOverStroke: Double,
    val bandLow: Double,
    val bandHigh: Double,
    val excludesFlat: Boolean,
    val guaranteesFlat: Boolean,
    val straddles: Boolean
)

@Serializable
private class T322OracleRow(
    val cell: String,
    val pairedRowBasePairs: Int,
    val placement: String,
    val columns: Int,
    val pathCount: Int,
    val p90FloorOverStroke: Double,
    val bestTransferredP90: Double,
    val bestTransferredOverFloor: Double,
    val excludesEveryDistributionAtP90: Boolean
)

@Serializable
private class T322Cell(
    val scaffold: String,
    val pairedRowBasePairs: Int,
    val compositeFraction: Double,
    val ladderPhase: Int,
    val placement: String,
    val columns: Int,
    val pathCount: Int,
    val meanSurvivors: Double,
    val equalP90: Double,
    val rimGradedP90: Double,
    val bestTransferredP90: Double,
    val bestTransferredLabel: String,
    val bestTransferredFlatAtP90: Boolean,
    val searchedP90: Double,
    val searchedNominal: Double,
    val searchedRatio: Double,
    val searchedPeakStiffness: Double,
    val searchedTrainingP90: Double,
    val bestTransferredTrainingP90: Double,
    val inSampleGain: Double,
    val outOfSampleGain: Double,
    /**
     * `T-337`. The `exceedance` the [flatAtP90] verdict below IS, on the SEARCHED distribution's
     * out-of-sample grading ensemble (`C-0223`). `bestTransferredFlatAtP90` is a verdict on a
     * different distribution and is deliberately not covered by this one field.
     */
    val exceedance: Double,
    val exceedanceStandardError: Double,
    val exceedanceOneSidedBound: Double?,
    val flatAtP90: Boolean,
    val peakInsideUnzipCeiling: Boolean,
    val ratioInsideFlatRatioWindow: Boolean,
    val uncoupledOverStroke: Double,
    val beatsUncoupledAtP90: Boolean,
    val flatAndAdmissible: Boolean,
    val allThreeThresholds: Boolean,
    val insideTheCheapBoundsBand: Boolean
)

@Serializable
private class T322PairedRow(
    val placement: String,
    val columns: Int,
    val pathCount: Int,
    val pairedRowBasePairs: Int,
    val routeBSearchedP90: Double,
    val routeBFlatAtP90: Boolean,
    val blockSearchedP90: Double,
    val blockFlatAtP90: Boolean,
    val routeBOverBlock: Double,
    val verdictMoves: Boolean
)

@Serializable
private class T322FragilityRow(
    val cell: String,
    val distribution: String,
    val p90OverStroke: Double,
    val nominalOverStroke: Double,
    val worstSinglePathRemovalOverStroke: Double,
    val amplification: Double,
    val ratio: Double,
    val twoLevelRatio: Double,
    val twoLevelP90OverStroke: Double,
    val twoLevelFlatAtP90: Boolean,
    val losesTheVerdict: Boolean
)

@Serializable
private class T322RungRow(
    val radialLinkStiffness: Double,
    val throughThicknessLink: Double,
    val ground: String,
    val cell: String,
    val bestTransferredP90: Double,
    val searchedP90: Double,
    val searchedRatio: Double,
    // `T-337`. This row transcribes the cell's own verdict, so it transcribes its proportion.
    val exceedance: Double,
    val exceedanceStandardError: Double,
    val exceedanceOneSidedBound: Double?,
    val flatAtP90: Boolean
)

@Serializable
private class T322CensusRow(
    val statistic: String,
    val cellsGraded: Int,
    val flatOnATransferredRule: Int,
    val flatOnTheSearchedRule: Int,
    val flatAndAdmissible: Int,
    val beatsUncoupled: Int,
    val allThreeThresholds: Int,
    val insideTheCheapBoundsBand: Int,
    val tightestTransferredP90: Double,
    val tightestSearchedP90: Double,
    val tightestSearchedCell: String,
    val searchedBeatsTransferredCells: Int
)

@Serializable
private class T322Convergence(
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
private class T322Reproduction(
    val statement: String,
    val published: Double,
    val here: Double,
    val relativeDeparture: Double,
    val source: String
)

@Serializable
private class T322Falsifier(
    val id: String,
    val statement: String,
    val declaredOpen: Boolean,
    val fired: Boolean,
    val note: String
)

@Serializable
private class T322Result(
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
    val cheapBound: List<T322CheapBoundRow>,
    val ladder: List<T322LadderRow>,
    val predictions: List<T322PredictionRow>,
    val oracle: List<T322OracleRow>,
    val cells: List<T322Cell>,
    val paired: List<T322PairedRow>,
    val fragility: List<T322FragilityRow>,
    val rungs: List<T322RungRow>,
    val census: List<T322CensusRow>,
    val verdict: Map<String, String>,
    val convergence: List<T322Convergence>,
    val reproductions: List<T322Reproduction>,
    val falsifiers: List<T322Falsifier>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val proseFailure: String
)

// ------------------------------------------------------------------------------ the geometry

private class T322Profile(
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, edgeX: Double, edgeY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, edgeY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T322_RIM_STANDOFF))
        )
}

private fun t322Profile(file: File): T322Profile {
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
    return T322Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

/**
 * `C-0211`'s three chain corners, by the name its own cells carry.
 *
 * The name is a **key** rather than a label: a corner this study cannot resolve is a corner whose
 * unpaired counts it would have to guess, and guessing them would silently grade a different
 * tether.
 */
private fun t322ChainNucleotides(chain: String): Pair<Int, Int> = when {
    chain.startsWith("28 nt at both rims") -> 28 to 28
    chain.startsWith("C-0200's ordered split exchanged") -> 32 to 24
    chain.startsWith("C-0200's ordered split") -> 24 to 32
    else -> error("C-0211 carries a chain corner this study cannot resolve: \"" + chain + "\"")
}

/** One route-B width: its own row length, tile, ladder, pressure and uncoupled reference. */
private class T322Width(
    val scaffold: String,
    scaffoldNucleotides: Int,
    val reference: RouteBUncoupledReference,
    val profile: T322Profile,
    val block: HoneycombBlock,
    val edgeY: Double,
    val enhancement: Double,
    val stagger: Int = T322_LADDER_STAGGER
) {
    val pairedRowBasePairs: Int =
        maximumUniformRowLength(scaffoldNucleotides, T322_HELICES, T322_UNPAIRED_PER_HELIX)

    init {
        require(pairedRowBasePairs == reference.pairedRowBasePairs) {
            "the scaffold gives " + pairedRowBasePairs + " bp and C-0211's reference is at " +
                    reference.pairedRowBasePairs
        }
    }

    val edgeX: Double = pairedRowBasePairs * Gen1Tile.RISE_PER_BASE_PAIR
    val interiorPressure: Double = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)
    val pressureField: PressureField = profile.field(interiorPressure, edgeX, edgeY)
    val ladder: RouteBStationLadder =
        RouteBStationLadder(pairedRowBasePairs, block.rasterRows, stagger)

    val tethers: UniformRasterTethers = t322ChainNucleotides(reference.chain).let { (low, high) ->
        UniformRasterTethers(
            block = block,
            pairedRowBasePairs = pairedRowBasePairs,
            interhelicalDistance = Gen1Tile.INTERHELICAL_HONEYCOMB,
            phosphateRadius = MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS,
            classZeroResidue = reference.classZeroResidue,
            lowRimNucleotides = low,
            highRimNucleotides = high,
            kuhnLength = reference.kuhnLength,
            contourPerNucleotide = reference.contourPerNucleotide,
            thermalEnergy = thermalEnergy(ROOM_TEMPERATURE)
        )
    }

    fun untiedAt(rung: ResolvedLinkRung): HoneycombGrillage = honeycombTiedLatticeAtResolvedLink(
        block = block,
        rowBasePairs = pairedRowBasePairs,
        enhancement = enhancement,
        tied = false,
        transverseLinkStiffness = rung.transverseLinkStiffness,
        radialLinkStiffness = rung.radialLinkStiffness
    )
}

/** One `(width, rung, subdivisions)` -- one factorisation, many banks. */
private class T322Tile(
    val width: T322Width,
    val rung: ResolvedLinkRung,
    val subdivisions: Int = 1
) {
    val lattice: HoneycombGrillage =
        width.tethers.latticeAtRung(rung, width.enhancement, subdivisions = subdivisions)

    /**
     * The free stroke, taken on the **untied** lattice exactly as `T-315` takes it.
     *
     * A uniform load translates a free tile rigidly, so the tethered and untied strokes must
     * agree, and `F8` asserts that rather than assuming it -- which is what licenses `F6`'s
     * reproduction of `C-0211`'s dishing, that being a ratio to this very quantity.
     */
    val freeStroke: Double by lazy {
        width.untiedAt(rung).solve(uniformPressure(width.interiorPressure)).meanDeflection
    }

    /** The **uncoupled** route-B tile with its preload -- `C-0211`'s own `freeTileWithPreload`. */
    val uncoupledDishing: Double by lazy {
        lattice.solve(width.pressureField).peakDishing(T322_SAMPLES) / freeStroke
    }

    fun surrogate(grid: List<Pair<Double, Double>>, samples: Int = T322_SAMPLES):
            InfluenceSurrogate = honeycombTiedSurrogate(
        lattice, grid, width.pressureField, samples
    )
}

private fun t322Distributions(
    grid: List<Pair<Double, Double>>,
    edgeX: Double,
    edgeY: Double
): List<Pair<String, List<Double>>> = listOf(
    "equal springs" to equalShareOfMandate(grid.size),
    "rim-graded 5:1" to rimGradedShareOfMandate(
        grid.map { (x, y) ->
            val onRim = abs(x) > edgeX / 2.0 - T322_RIM_BAND || abs(y) > edgeY / 2.0 - T322_RIM_BAND
            if (onRim) 5.0 else 1.0
        }
    )
)

/** `C-0167`'s four placements, DERIVED at this width's own row length and ladder phase. */
private fun t322Placements(
    width: T322Width,
    rootingHelixY: List<Double>,
    columns: Int
): List<Pair<String, List<Pair<Double, Double>>>> {
    val abstract = attachmentGrid(columns, width.block.rasterRows, width.edgeX, width.edgeY)
    val determined = honeycombSnappedGrid(
        columns, width.block.rasterRows, width.pairedRowBasePairs, width.edgeY,
        width.ladder.derivedPhase, width.stagger
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

private class T322Graded(
    val nominal: Double,
    val p90: Double,
    val flat: Boolean,
    // `T-337`. `C-0223`: `flat` IS `exceedance <= tolerance`, so the proportion travels with it.
    val exceedance: Double,
    val exceedanceStandardError: Double,
    val exceedanceOneSidedBound: Double?
)

private fun t322Grade(
    surrogate: InfluenceSurrogate,
    stiffnesses: List<Double>,
    freeStroke: Double,
    ensemble: DropoutEnsemble
): T322Graded {
    val nominal = surrogate.solve(stiffnesses).peakDishing / freeStroke
    val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
    sample.indices.forEach { sample[it] = sample[it] / freeStroke }
    val summary = summariseDropoutDishing(
        sample, nominal, ensemble.meanSurvivors, T322_TOLERANCE
    )
    return T322Graded(
        nominal, summary.p90, summary.flatAtP90,
        summary.exceedance, summary.exceedanceStandardError, summary.exceedanceOneSidedBound
    )
}

/** `C-0212`'s own published cell at the `116 bp` block, keyed on every dimension it varied. */
private fun t322Published(
    file: File,
    fraction: Double,
    placement: String,
    columns: Int
): Pair<Double, Boolean> = Json.parseToJsonElement(file.readText())
    .jsonObject.getValue("cells").jsonArray.map { it.jsonObject }
    .first { record ->
        fun number(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
        fun text(name: String) = record.getValue(name).jsonPrimitive.content
        abs(number("compositeFraction") - fraction) < 1e-9 &&
                text("placement") == placement &&
                record.getValue("columns").jsonPrimitive.content.toInt() == columns
    }
    .let {
        it.getValue("searchedP90").jsonPrimitive.content.toDouble() to
                it.getValue("flatAtP90").jsonPrimitive.content.toBoolean()
    }

// ------------------------------------------------------------------------------ the study

@Suppress("LongMethod", "ComplexMethod", "NestedBlockDepth", "LongParameterList")
fun main() {
    val kBT = thermalEnergy(ROOM_TEMPERATURE)
    val d = Gen1Tile.INTERHELICAL_HONEYCOMB
    val rP = MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS
    val kTheta = Gen1Tile.crossoverHingeStiffness()
    val block = HoneycombBlock(10, 6)
    val rowPitch = HoneycombCrossSectionGeometry.rowPitch(d)
    val columnPitch = HoneycombCrossSectionGeometry.columnPitch(d)
    val edgeY = block.rasterRows * rowPitch
    val profile = t322Profile(ResultInputs.T_3B.file())
    val mandate = MANDATED_TOTAL_STIFFNESS
    val unzipCeiling =
        perPathStiffnessCeiling(Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE)

    fun enhancementAt(fraction: Double): Double = multiLayerRigidities(
        layers = block.helicesPerRow,
        interhelicalDistance = d,
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
        coupling = LayerCoupling.CALIBRATED,
        compositeFraction = fraction,
        layerSpacing = columnPitch
    ).realisedEnhancement

    // The two constants, recomputed through the corpus's own functions rather than transcribed,
    // so every reproduction below is graded on a lattice C-0208 actually built.
    val shearCeiling = crossoverLinkStiffnessBracket(
        hingeStiffness = kTheta,
        phosphateRadius = rP,
        interhelicalDistance = d,
        thermalEnergy = kBT,
        softestPersistenceLength = 1.34 / 2.0,
        stiffestPersistenceLength = 2.84 / 2.0
    ).ceiling
    val radial = crossoverRadialLinkBracket(
        hingeStiffness = kTheta,
        phosphateRadius = rP,
        interhelicalDistance = d,
        relaxedStep = MeasuredBackbone.STEP_SOUTH,
        stretchModulus = Gen1Tile.DUPLEX_STRETCH_MODULUS,
        equationOfState = MengMagnesium.equationOfState,
        contactLength = T322_CONTACT_BP * Gen1Tile.RISE_PER_BASE_PAIR
    )
    fun throughThickness(constant: Double): Double =
        resolvedLinkStiffness(constant, shearCeiling, unitY = 0.5, unitZ = SQRT_THREE_HALVES_T322)

    fun rungAt(constant: Double, name: String, ground: String) =
        ResolvedLinkRung(name, ground, shearCeiling, constant)

    val floorRung = rungAt(
        radial.floor, "the resolved floor",
        "C-0205's shear ceiling transverse, C-0208's radial bracket FLOOR"
    )
    val rungLadder = listOf(
        rungAt(shearCeiling, "the uniform shear ceiling",
            "C-0208's first radial rung -- radial = transverse, the SOFTEST defensible lattice"),
        rungAt(radial.connectorAtImpliedStep, "the connector at the implied step",
            "CH-0259's own low candidate"),
        floorRung,
        rungAt(radial.connectorAtDuplexStretch, "the connector at the duplex stretch modulus",
            "CH-0259's own high candidate"),
        rungAt(radial.ceiling, "the resolved ceiling",
            "that connector candidate plus the measured pair term -- the bracket CEILING")
    ).sortedBy { it.radialLinkStiffness!! } +
            ResolvedLinkRung(
                "the penalty (C-0207's own reading)",
                "HoneycombGrillage.RIGID_LINK_STIFFNESS, a numerical penalty",
                T322_PENALTY, null
            )

    // ================ the uncoupled reference, read out of C-0211's committed cells
    val referenceFile = ResultInputs.T_315.file()
    val references = routeBUncoupledReferences(referenceFile, floorRung.name)
    val publishedWorstCorner = routeBPublishedBestWorstCorner(referenceFile, floorRung.name)
    val enhancement = enhancementAt(T322_FRACTION)
    val widths = listOf(
        "M13mp18" to T322_M13, "p7560" to T322_P7560, "p8064" to T322_P8064
    ).map { (scaffold, nucleotides) ->
        val row = maximumUniformRowLength(nucleotides, T322_HELICES, T322_UNPAIRED_PER_HELIX)
        T322Width(
            scaffold, nucleotides,
            references.first { it.pairedRowBasePairs == row },
            profile, block, edgeY, enhancement
        )
    }.let { if (t322Smoke) it.take(1) else it }

    println("T-322 - route B coupled, on stations derived at each row length")
    widths.forEach {
        println("  " + it.scaffold + "  " + it.pairedRowBasePairs + " bp  edgeX " +
                it.edgeX.emitted(9) + "  b0 " + it.reference.classZeroResidue + "  corner \"" +
                it.reference.chain + "\"  uncoupled (C-0211) " +
                it.reference.freeTileWithPreload.emitted(9))
    }

    // ================ Deliverable 1a -- the cheap bound that is pure lattice arithmetic
    println("T-322 - cheap bound 1: the station ladder")
    val ladderRows = ArrayList<T322LadderRow>()
    (widths.map { it.pairedRowBasePairs } + listOf(116)).distinct().sorted().forEach { row ->
        listOf(T322_LADDER_STAGGER, T322_ALTERNATE_STAGGER).forEach { stagger ->
            val ladder = RouteBStationLadder(row, block.rasterRows, stagger)
            ladderRows += T322LadderRow(
                pairedRowBasePairs = row,
                interRowOffsetBasePairs = stagger,
                derivedPhase = ladder.derivedPhase,
                minimumStationsPerRow = ladder.minimumStationsPerRow,
                stationsAtTheInheritedPhase = ladder.minimumStationsAtPhase(T322_INHERITED_PHASE),
                carriesFiveColumnsAtTheInheritedPhase =
                    ladder.carriesColumnsAtPhase(5, T322_INHERITED_PHASE),
                maximumColumns = ladder.maximumColumns,
                note = if (row == 116)
                    "C-0167's own block extent, carried for contrast: it is NOT a route-B width"
                else "route B's own buildable row at " +
                        (row + T322_UNPAIRED_PER_HELIX) * T322_HELICES + " scaffold nucleotides"
            )
        }
    }
    ladderRows.forEach {
        println("  " + it.pairedRowBasePairs + " bp, stagger " + it.interRowOffsetBasePairs +
                ": derived phase " + it.derivedPhase + ", min " + it.minimumStationsPerRow +
                " stations; at the inherited phase " + it.stationsAtTheInheritedPhase)
    }
    val inheritedPhaseRefuses = ladderRows.count {
        it.pairedRowBasePairs != 116 && it.interRowOffsetBasePairs == T322_LADDER_STAGGER &&
                !it.carriesFiveColumnsAtTheInheritedPhase
    }
    val routeBMaximumColumns = ladderRows
        .filter {
            it.pairedRowBasePairs != 116 &&
                    it.interRowOffsetBasePairs == T322_LADDER_STAGGER
        }
        .maxOf { it.maximumColumns }
    val blockMaximumColumns = ladderRows
        .first { it.pairedRowBasePairs == 116 && it.interRowOffsetBasePairs == T322_LADDER_STAGGER }
        .maximumColumns

    // ================ Deliverable 1b -- the ratio transfer, a PREDICTION with its own falsifier
    println("T-322 - cheap bound 2: the ratio transfer from C-0212")
    val blockFile = ResultInputs.T_316.file()
    val blockCells = Json.parseToJsonElement(blockFile.readText())
        .jsonObject.getValue("cells").jsonArray.map { it.jsonObject }
        .filter { abs(it.getValue("compositeFraction").jsonPrimitive.content.toDouble() -
                T322_FRACTION) < 1e-9 }
    fun blockRatios(key: String): List<Double> = blockCells.map {
        it.getValue(key).jsonPrimitive.content.toDouble() /
                it.getValue("uncoupledDishingOverStroke").jsonPrimitive.content.toDouble()
    }
    val searchedBand = TransferredRatioBand(
        blockRatios("searchedP90").min(), blockRatios("searchedP90").max()
    )
    val transferredBand = TransferredRatioBand(
        blockRatios("bestTransferredP90").min(), blockRatios("bestTransferredP90").max()
    )
    val predictions = widths.flatMap { width ->
        listOf("a transferred rule" to transferredBand, "a searched rule" to searchedBand)
            .map { (rule, band) ->
                val predicted = band.predict(width.reference.freeTileWithPreload, T322_TOLERANCE)
                T322PredictionRow(
                    pairedRowBasePairs = width.pairedRowBasePairs,
                    rule = rule,
                    uncoupledOverStroke = width.reference.freeTileWithPreload,
                    bandLow = predicted.low,
                    bandHigh = predicted.high,
                    excludesFlat = predicted.excludesFlat,
                    guaranteesFlat = predicted.guaranteesFlat,
                    straddles = predicted.straddles
                )
            }
    }
    predictions.forEach {
        println("  " + it.pairedRowBasePairs + " bp on " + it.rule + ": " +
                it.bandLow.emitted(9) + " to " + it.bandHigh.emitted(9) +
                (if (it.excludesFlat) "  EXCLUDES flat"
                else if (it.guaranteesFlat) "  guarantees flat" else "  straddles"))
    }
    val searchedPredictionByRow = widths.associate { width ->
        width.pairedRowBasePairs to
                searchedBand.predict(width.reference.freeTileWithPreload, T322_TOLERANCE)
    }

    val cheapBound = listOf(
        T322CheapBoundRow(
            question = "how many station columns does route B's own row carry, against the " +
                    "116 bp block extent every coupled census here is read on",
            answer = routeBMaximumColumns.toString() + " against the block's " +
                    blockMaximumColumns + ", at every one of the 21 ladder phases",
            value = routeBMaximumColumns.toDouble(),
            units = "station columns per rooting helix",
            consequence = "a shorter row is not merely a smaller tile, it is a tile with one " +
                    "fewer place to stand -- pure lattice arithmetic, no solve"
        ),
        T322CheapBoundRow(
            question = "does T-316's inherited ladder phase " + T322_INHERITED_PHASE +
                    " carry a 5-column placement at every route-B width",
            answer = if (inheritedPhaseRefuses > 0)
                "NO -- it is refused at " + inheritedPhaseRefuses + " of " + widths.size +
                        " widths, which is F12"
            else "yes",
            value = inheritedPhaseRefuses.toDouble(),
            units = "route-B widths at which the inherited phase is refused",
            consequence = "the station set must be DERIVED at each row length; C-0141's ladder " +
                    "phase is fixed by the +/-5 bp rule only where the raster CLOSES, and " +
                    "route B's uniform rows close at no phase"
        ),
        T322CheapBoundRow(
            question = "what does C-0212's own searched/uncoupled ratio predict for route B",
            answer = predictions.filter { it.rule == "a searched rule" }
                .joinToString("; ") {
                    it.pairedRowBasePairs.toString() + " bp: " + it.bandLow.emitted(9) + " to " +
                            it.bandHigh.emitted(9)
                },
            value = searchedBand.low,
            units = "the low end of the transferred ratio band, dimensionless",
            consequence = "it STRADDLES T-5b on a searched rule and EXCLUDES flat on a " +
                    "transferred one, so it decides the transferred census and cannot decide " +
                    "the headline -- and it is a ratio carried between two lattices, so F20 " +
                    "is declared on it rather than it being asserted"
        )
    )

    // ================ Deliverables 2 and 3 -- the sweep
    val tiles = HashMap<Pair<Int, Double>, T322Tile>()
    widths.forEach { width -> tiles[width.pairedRowBasePairs to radial.floor] =
        T322Tile(width, floorRung) }
    val rootingHelixY = widths.first().let { width ->
        tiles.getValue(width.pairedRowBasePairs to radial.floor).lattice
            .let { lattice -> lattice.faceBeams.map { lattice.beamY[it] } }
    }

    class T322Searched(
        val cell: T322Cell,
        val grid: List<Pair<Double, Double>>,
        val surrogate: InfluenceSurrogate,
        val stiffnesses: List<Double>,
        val transferred: List<Double>,
        val transferredLabel: String,
        val freeStroke: Double,
        val gradingEnsemble: DropoutEnsemble
    )

    fun searchOne(
        width: T322Width,
        rung: ResolvedLinkRung,
        fraction: Double,
        placement: String,
        grid: List<Pair<Double, Double>>,
        columns: Int,
        gradingEnsemble: DropoutEnsemble,
        trainingEnsemble: DropoutEnsemble,
        subdivisions: Int = 1,
        searchSamples: Int = T322_SEARCH_SAMPLES,
        sweeps: Int = T322_PERCENTILE_SWEEPS
    ): T322Searched {
        val tile =
            if (subdivisions == 1 && fraction == T322_FRACTION &&
                rung.radialLinkStiffness == radial.floor
            ) tiles.getValue(width.pairedRowBasePairs to radial.floor)
            else T322Tile(
                if (fraction == T322_FRACTION) width
                else T322Width(
                    width.scaffold,
                    (width.pairedRowBasePairs + T322_UNPAIRED_PER_HELIX) * T322_HELICES,
                    width.reference, profile, block, edgeY, enhancementAt(fraction), width.stagger
                ),
                rung, subdivisions
            )
        val surrogate = tile.surrogate(grid, T322_SAMPLES)
        val searchSurrogate =
            if (searchSamples == T322_SAMPLES) surrogate else tile.surrogate(grid, searchSamples)
        val multi = honeycombMultiStateSurrogate(
            tile.lattice, grid,
            singleLoadState("C-0022's solved collar", tile.width.pressureField), searchSamples
        )
        val distributions = t322Distributions(grid, width.edgeX, width.edgeY)
        val gradedTransferred = distributions.map { (label, k) ->
            Triple(label, k, t322Grade(surrogate, k, tile.freeStroke, gradingEnsemble))
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
            percentileScanPoints = T322_SCAN_POINTS,
            percentileRefinements = T322_REFINEMENTS,
            smoothingLevels = T322_SMOOTHING_LEVELS,
            smoothingIterations = T322_SMOOTHING_ITERATIONS,
            polishSweeps = T322_POLISH_SWEEPS
        )
        val outOfSample = t322Grade(
            surrogate, searched.stiffnesses, tile.freeStroke, gradingEnsemble
        )
        val peak = searched.stiffnesses.max()
        val admissibility = RouteBAdmissibility(
            flatAtP90 = outOfSample.flat,
            peakInsideUnzipCeiling = peak < unzipCeiling,
            beatsUncoupledAtP90 = outOfSample.p90 < tile.uncoupledDishing
        )
        val band = searchedPredictionByRow[width.pairedRowBasePairs]
        return T322Searched(
            cell = T322Cell(
                scaffold = width.scaffold,
                pairedRowBasePairs = width.pairedRowBasePairs,
                compositeFraction = fraction,
                ladderPhase = width.ladder.derivedPhase,
                placement = placement,
                columns = columns,
                pathCount = grid.size,
                meanSurvivors = gradingEnsemble.meanSurvivors,
                equalP90 = gradedTransferred.first { it.first == "equal springs" }.third.p90,
                rimGradedP90 = gradedTransferred.first { it.first == "rim-graded 5:1" }.third.p90,
                bestTransferredP90 = best.third.p90,
                bestTransferredLabel = best.first,
                bestTransferredFlatAtP90 = best.third.flat,
                searchedP90 = outOfSample.p90,
                searchedNominal = outOfSample.nominal,
                searchedRatio = searched.ratio,
                searchedPeakStiffness = peak,
                searchedTrainingP90 = searched.trainingObjective,
                bestTransferredTrainingP90 = searched.bestTransferredTrainingObjective,
                inSampleGain = searched.bestTransferredTrainingObjective /
                        searched.trainingObjective,
                outOfSampleGain = best.third.p90 / outOfSample.p90,
                exceedance = outOfSample.exceedance,
                exceedanceStandardError = outOfSample.exceedanceStandardError,
                exceedanceOneSidedBound = outOfSample.exceedanceOneSidedBound,
                flatAtP90 = admissibility.flatAtP90,
                peakInsideUnzipCeiling = admissibility.peakInsideUnzipCeiling,
                ratioInsideFlatRatioWindow = searched.ratio > T322_FLAT_RATIO_FLOOR &&
                        searched.ratio < T322_FLAT_RATIO_CEILING,
                uncoupledOverStroke = tile.uncoupledDishing,
                beatsUncoupledAtP90 = admissibility.beatsUncoupledAtP90,
                flatAndAdmissible = admissibility.flatAndAdmissible,
                allThreeThresholds = admissibility.allThreeThresholds,
                insideTheCheapBoundsBand = band != null && band.contains(outOfSample.p90)
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

    println("T-322 - the sweep: " + widths.size + " widths x 4 placements x " +
            t322Columns.size + " column counts")
    val searchedCells = ArrayList<T322Searched>()
    val oracle = ArrayList<T322OracleRow>()
    val startedAt = System.nanoTime()
    widths.forEach { width ->
        val incorporation = measuredDepthIncorporation(width.edgeX, width.edgeY)
        t322Columns.forEach { columns ->
            require(width.ladder.carriesColumns(columns)) {
                "route B's " + width.pairedRowBasePairs + " bp row carries only " +
                        width.ladder.minimumStationsPerRow + " stations per helix at its " +
                        "derived phase, so a " + columns + "-column placement does not exist"
            }
            t322Placements(width, rootingHelixY, columns).forEach { (placement, grid) ->
                val probabilities = grid.map { (x, y) -> incorporation.at(x, y) }
                val gradingEnsemble =
                    dropoutEnsemble(probabilities, t322Realisations, T322_GRADING_SEED)
                val trainingEnsemble =
                    dropoutEnsemble(probabilities, t322TrainingRealisations, T322_TRAINING_SEED)
                val found = searchOne(
                    width, floorRung, T322_FRACTION, placement, grid, columns,
                    gradingEnsemble, trainingEnsemble
                )
                searchedCells += found
                val floors = oracleFloorSample(found.surrogate, gradingEnsemble)
                floors.indices.forEach { floors[it] = floors[it] / found.freeStroke }
                val p90Floor = orderStatistic(floors, 0.90)
                oracle += T322OracleRow(
                    cell = width.pairedRowBasePairs.toString() + " bp, " + placement + ", " +
                            columns + " x " + block.rasterRows + " = " + grid.size + " paths",
                    pairedRowBasePairs = width.pairedRowBasePairs,
                    placement = placement,
                    columns = columns,
                    pathCount = grid.size,
                    p90FloorOverStroke = p90Floor,
                    bestTransferredP90 = found.cell.bestTransferredP90,
                    bestTransferredOverFloor = found.cell.bestTransferredP90 / p90Floor,
                    excludesEveryDistributionAtP90 = p90Floor > T322_TOLERANCE
                )
            }
            // A wall clock belongs in the console and NEVER in the result file (`CLAUDE.md`).
            println("  " + width.pairedRowBasePairs + " bp, " + columns + " columns done, " +
                    ((System.nanoTime() - startedAt) / 1_000_000_000L) + " s elapsed")
        }
    }
    val cells = searchedCells.map { it.cell }

    // ================ Deliverable 7 -- the paired comparison against C-0212's 116 bp block
    println("T-322 - the paired comparison against C-0212")
    val paired = cells.map { cell ->
        val (blockP90, blockFlat) = t322Published(
            blockFile, cell.compositeFraction, cell.placement, cell.columns
        )
        T322PairedRow(
            placement = cell.placement,
            columns = cell.columns,
            pathCount = cell.pathCount,
            pairedRowBasePairs = cell.pairedRowBasePairs,
            routeBSearchedP90 = cell.searchedP90,
            routeBFlatAtP90 = cell.flatAtP90,
            blockSearchedP90 = blockP90,
            blockFlatAtP90 = blockFlat,
            routeBOverBlock = cell.searchedP90 / blockP90,
            verdictMoves = cell.flatAtP90 != blockFlat
        )
    }
    val pairedVerdictMoves = paired.count { it.verdictMoves }
    println("  the tile substitution moves the verdict at " + pairedVerdictMoves + " of " +
            paired.size + " paired cells")

    // ================ Deliverables 5 and 6 -- the thresholds, fragility, and the rung ladder
    val flatTransferred = cells.count { it.bestTransferredFlatAtP90 }
    val flatSearched = cells.count { it.flatAtP90 }
    val tightestSearched = searchedCells.minByOrNull { it.cell.searchedP90 }!!
    val tightCell = tightestSearched.cell
    val tightWidth = widths.first { it.pairedRowBasePairs == tightCell.pairedRowBasePairs }
    val tightGrid = t322Placements(tightWidth, rootingHelixY, tightCell.columns)
        .first { it.first == tightCell.placement }.second
    val tightIncorporation = measuredDepthIncorporation(tightWidth.edgeX, tightWidth.edgeY)
    val tightProbabilities = tightGrid.map { (x, y) -> tightIncorporation.at(x, y) }
    val tightGrading = dropoutEnsemble(tightProbabilities, t322Realisations, T322_GRADING_SEED)
    val tightTraining =
        dropoutEnsemble(tightProbabilities, t322TrainingRealisations, T322_TRAINING_SEED)
    val decidingLabel = tightCell.pairedRowBasePairs.toString() + " bp, f = " +
            tightCell.compositeFraction.emitted(3) + ", " + tightCell.placement + ", " +
            tightCell.columns + " x " + block.rasterRows + " = " + tightCell.pathCount + " paths"

    println("T-322 - fragility")
    val fragility = ArrayList<T322FragilityRow>()
    var fragileFlat = 0
    (searchedCells.filter { it.cell.flatAtP90 } + tightestSearched)
        .distinctBy {
            it.cell.pairedRowBasePairs.toString() + "|" + it.cell.placement + "|" +
                    it.cell.columns
        }
        .forEach { found ->
            val label = found.cell.pairedRowBasePairs.toString() + " bp, " +
                    found.cell.placement + ", " + found.cell.columns + " x " + block.rasterRows +
                    " = " + found.cell.pathCount + " paths"
            listOf(
                "SEARCHED" to found.stiffnesses,
                found.transferredLabel + " (transferred)" to found.transferred
            ).forEach { (name, k) ->
                val graded = t322Grade(found.surrogate, k, found.freeStroke, found.gradingEnsemble)
                val worst = worstSinglePathRemoval(found.surrogate, k) / found.freeStroke
                val twoLevel = quantiseToLevels(k, 2, mandate)
                val twoLevelGraded =
                    t322Grade(found.surrogate, twoLevel, found.freeStroke, found.gradingEnsemble)
                val loses = name == "SEARCHED" && found.cell.flatAtP90 && worst > T322_TOLERANCE
                if (loses) fragileFlat += 1
                fragility += T322FragilityRow(
                    cell = label,
                    distribution = name,
                    p90OverStroke = graded.p90,
                    nominalOverStroke = graded.nominal,
                    worstSinglePathRemovalOverStroke = worst,
                    amplification = worst / graded.nominal,
                    ratio = stiffnessRatio(k),
                    twoLevelRatio = stiffnessRatio(twoLevel),
                    twoLevelP90OverStroke = twoLevelGraded.p90,
                    twoLevelFlatAtP90 = twoLevelGraded.flat,
                    losesTheVerdict = loses
                )
            }
        }

    println("T-322 - the deciding cell over the rung ladder")
    val rungRows = rungLadder.map { rung ->
        val found = if (rung === floorRung) tightestSearched else searchOne(
            tightWidth, rung, T322_FRACTION, tightCell.placement, tightGrid, tightCell.columns,
            tightGrading, tightTraining
        )
        T322RungRow(
            radialLinkStiffness = rung.radialLinkStiffness ?: rung.transverseLinkStiffness,
            throughThicknessLink = rung.throughThicknessLinkStiffness,
            ground = rung.ground,
            cell = decidingLabel,
            bestTransferredP90 = found.cell.bestTransferredP90,
            searchedP90 = found.cell.searchedP90,
            searchedRatio = found.cell.searchedRatio,
            exceedance = found.cell.exceedance,
            exceedanceStandardError = found.cell.exceedanceStandardError,
            exceedanceOneSidedBound = found.cell.exceedanceOneSidedBound,
            flatAtP90 = found.cell.flatAtP90
        )
    }
    rungRows.forEach {
        println("  k_radial " + it.radialLinkStiffness.emitted(9) + " -> searched " +
                it.searchedP90.emitted(9) + (if (it.flatAtP90) "  FLAT" else ""))
    }
    val rungVerdictMoves = rungRows.map { it.flatAtP90 }.distinct().size > 1

    val census = listOf(
        T322CensusRow(
            statistic = "the 90th percentile of C-0087's measured staple dropout over " +
                    t322Realisations + " realisations of seed " + T322_GRADING_SEED +
                    ", against T-5b's " + T322_TOLERANCE.emitted(2),
            cellsGraded = cells.size,
            flatOnATransferredRule = flatTransferred,
            flatOnTheSearchedRule = flatSearched,
            flatAndAdmissible = cells.count { it.flatAndAdmissible },
            beatsUncoupled = cells.count { it.beatsUncoupledAtP90 },
            allThreeThresholds = cells.count { it.allThreeThresholds },
            insideTheCheapBoundsBand = cells.count { it.insideTheCheapBoundsBand },
            tightestTransferredP90 = cells.minOf { it.bestTransferredP90 },
            tightestSearchedP90 = cells.minOf { it.searchedP90 },
            tightestSearchedCell = decidingLabel,
            searchedBeatsTransferredCells = cells.count { it.searchedP90 < it.bestTransferredP90 }
        )
    )
    println("  transferred: " + flatTransferred + " of " + cells.size + " flat; searched: " +
            flatSearched + "; flat AND admissible: " + census[0].flatAndAdmissible +
            "; beats the uncoupled tile: " + census[0].beatsUncoupled)

    // ================ convergence, on the DECIDING quantity at the DECIDING cell
    println("T-322 - convergence at the deciding cell")
    val convergence = ArrayList<T322Convergence>()
    listOf(T322_SAMPLES).forEach { samples ->
        val here = searchOne(
            tightWidth, floorRung, T322_FRACTION, tightCell.placement, tightGrid,
            tightCell.columns, tightGrading, tightTraining, searchSamples = samples
        )
        convergence += T322Convergence(
            axis = "the dishing sample grid the SEARCH runs on, " + T322_SEARCH_SAMPLES +
                    " against " + samples,
            quantity = "the SEARCHED 90th percentile, out of sample",
            cell = decidingLabel,
            coarse = tightCell.searchedP90,
            fine = here.cell.searchedP90,
            departure = abs(here.cell.searchedP90 - tightCell.searchedP90) / tightCell.searchedP90,
            verdictMoves = here.cell.flatAtP90 != tightCell.flatAtP90,
            note = "every graded percentile in the census is read at " + T322_SAMPLES +
                    " throughout; this axis moves only the grid the search itself descends on"
        )
    }
    listOf(41, 161).forEach { samples ->
        val tile = tiles.getValue(tightWidth.pairedRowBasePairs to radial.floor)
        val here = t322Grade(
            tile.surrogate(tightGrid, samples), tightestSearched.stiffnesses,
            tile.freeStroke, tightGrading
        )
        convergence += T322Convergence(
            axis = "the dishing sample grid the VERDICT is read on, " + T322_SAMPLES +
                    " against " + samples,
            quantity = "the searched distribution's 90th percentile, at a FIXED distribution",
            cell = decidingLabel,
            coarse = tightCell.searchedP90,
            fine = here.p90,
            departure = abs(here.p90 - tightCell.searchedP90) / tightCell.searchedP90,
            verdictMoves = here.flat != tightCell.flatAtP90,
            note = "the distribution is held fixed and only the quadrature the verdict is read " +
                    "on moves, which is C-0167's axis on C-0167's quantity"
        )
    }
    listOf(60, 240).forEach { count ->
        val training = dropoutEnsemble(
            tightProbabilities, if (t322Smoke) 20 else count, T322_TRAINING_SEED
        )
        val here = searchOne(
            tightWidth, floorRung, T322_FRACTION, tightCell.placement, tightGrid,
            tightCell.columns, tightGrading, training
        )
        convergence += T322Convergence(
            axis = "the TRAINING realisations the search sees, " + t322TrainingRealisations +
                    " against " + count,
            quantity = "the SEARCHED 90th percentile, graded OUT OF SAMPLE on the same " +
                    t322Realisations + "-realisation grading ensemble",
            cell = decidingLabel,
            coarse = tightCell.searchedP90,
            fine = here.cell.searchedP90,
            departure = abs(here.cell.searchedP90 - tightCell.searchedP90) / tightCell.searchedP90,
            verdictMoves = here.cell.flatAtP90 != tightCell.flatAtP90,
            note = "an IN-SAMPLE percentile optimum is not a result; this is the convergence " +
                    "of the training ensemble the search sees, read on the independent one"
        )
    }
    run {
        val here = searchOne(
            tightWidth, floorRung, T322_FRACTION, tightCell.placement, tightGrid,
            tightCell.columns, tightGrading, tightTraining, subdivisions = 2
        )
        convergence += T322Convergence(
            axis = "beam subdivisions, 1 against 2",
            quantity = "the SEARCHED 90th percentile, out of sample",
            cell = decidingLabel,
            coarse = tightCell.searchedP90,
            fine = here.cell.searchedP90,
            departure = abs(here.cell.searchedP90 - tightCell.searchedP90) / tightCell.searchedP90,
            verdictMoves = here.cell.flatAtP90 != tightCell.flatAtP90,
            note = "the whole search is re-run on the refined lattice, at a row length that " +
                    "carries a nodeS overhang unless it is a multiple of 7 bp"
        )
    }
    run {
        val here = searchOne(
            tightWidth, floorRung, T322_FRACTION, tightCell.placement, tightGrid,
            tightCell.columns, tightGrading, tightTraining, sweeps = T322_PERCENTILE_SWEEPS + 1
        )
        convergence += T322Convergence(
            axis = "the percentile descent's sweeps, " + T322_PERCENTILE_SWEEPS + " against " +
                    (T322_PERCENTILE_SWEEPS + 1),
            quantity = "the SEARCHED 90th percentile, out of sample",
            cell = decidingLabel,
            coarse = tightCell.searchedP90,
            fine = here.cell.searchedP90,
            departure = abs(here.cell.searchedP90 - tightCell.searchedP90) / tightCell.searchedP90,
            verdictMoves = here.cell.flatAtP90 != tightCell.flatAtP90,
            note = "a descent reports the best point it FOUND; this is how much of the answer " +
                    "is the budget rather than the objective"
        )
    }
    // The two axes the Plan named as carried at the deciding cell only, rather than swept.
    run {
        val here = searchOne(
            tightWidth, floorRung, T322_ALTERNATE_FRACTION, tightCell.placement, tightGrid,
            tightCell.columns, tightGrading, tightTraining
        )
        convergence += T322Convergence(
            axis = "the composite fraction, " + T322_FRACTION.emitted(3) + " against " +
                    T322_ALTERNATE_FRACTION.emitted(3) + " (C-0116's two readings)",
            quantity = "the SEARCHED 90th percentile, out of sample",
            cell = decidingLabel,
            coarse = tightCell.searchedP90,
            fine = here.cell.searchedP90,
            departure = abs(here.cell.searchedP90 - tightCell.searchedP90) / tightCell.searchedP90,
            verdictMoves = here.cell.flatAtP90 != tightCell.flatAtP90,
            note = "the Plan names this as carried at the deciding cell rather than swept, and " +
                    "this row is that carriage rather than a claim that the axis is inert"
        )
    }
    run {
        val alternate = T322Width(
            tightWidth.scaffold,
            (tightWidth.pairedRowBasePairs + T322_UNPAIRED_PER_HELIX) * T322_HELICES,
            tightWidth.reference, profile, block, edgeY, enhancement, T322_ALTERNATE_STAGGER
        )
        val grid = t322Placements(alternate, rootingHelixY, tightCell.columns)
            .first { it.first == tightCell.placement }.second
        val probabilities = grid.map { (x, y) -> tightIncorporation.at(x, y) }
        val here = searchOne(
            alternate, floorRung, T322_FRACTION, tightCell.placement, grid, tightCell.columns,
            dropoutEnsemble(probabilities, t322Realisations, T322_GRADING_SEED),
            dropoutEnsemble(probabilities, t322TrainingRealisations, T322_TRAINING_SEED)
        )
        convergence += T322Convergence(
            axis = "C-0141's forced inter-row stagger, " + T322_LADDER_STAGGER + " bp against " +
                    T322_ALTERNATE_STAGGER + " bp",
            quantity = "the SEARCHED 90th percentile, out of sample",
            cell = decidingLabel,
            coarse = tightCell.searchedP90,
            fine = here.cell.searchedP90,
            departure = abs(here.cell.searchedP90 - tightCell.searchedP90) / tightCell.searchedP90,
            verdictMoves = here.cell.flatAtP90 != tightCell.flatAtP90,
            note = "C-0141 carries both staggers and this repository cannot yet fix which; the " +
                    "derived ladder phase moves with it, so this is a PLACEMENT axis and not a " +
                    "numerical one"
        )
    }
    convergence.forEach {
        println("  " + it.axis + "  departure " + it.departure.emitted(2) +
                (if (it.verdictMoves) "  VERDICT MOVES" else ""))
    }

    // ================ the reproductions
    println("T-322 - reproductions")
    val reproductions = ArrayList<T322Reproduction>()
    widths.forEach { width ->
        val tile = tiles.getValue(width.pairedRowBasePairs to radial.floor)
        val there = width.reference.freeTileWithPreload
        reproductions += T322Reproduction(
            statement = "C-0211's uncoupled free tile with its preload at " +
                    width.pairedRowBasePairs + " bp, b0 = " + width.reference.classZeroResidue +
                    ", the worst of its twelve chain corners, at the resolved floor",
            published = there,
            here = tile.uncoupledDishing,
            relativeDeparture = abs(tile.uncoupledDishing - there) / there,
            source = ResultInputs.T_315.path
        )
        val worstCorner = publishedWorstCorner.getValue(width.pairedRowBasePairs)
        reproductions += T322Reproduction(
            statement = "C-0211's own bestWorstCornerDishing at " + width.pairedRowBasePairs +
                    " bp is the cell this study grades against",
            published = worstCorner,
            here = there,
            relativeDeparture = abs(there - worstCorner) / worstCorner,
            source = ResultInputs.T_315.path
        )
        val census = ResolvedLinkBondCensus(width.untiedAt(floorRung), floorRung)
        val expected = mapOf(92 to 358, 98 to 385, 106 to 410)
            .getValue(width.pairedRowBasePairs).toDouble()
        reproductions += T322Reproduction(
            statement = "CH-0270's bond census at " + width.pairedRowBasePairs + " bp",
            published = expected,
            here = census.totalBonds.toDouble(),
            relativeDeparture = abs(census.totalBonds - expected) / expected,
            source = "CH-0270, and C-0211 section 5"
        )
    }
    reproductions += T322Reproduction(
        statement = "C-0205's shear ceiling, the transverse constant",
        published = 254.808095,
        here = shearCeiling,
        relativeDeparture = abs(shearCeiling - 254.808095) / 254.808095,
        source = "C-0205 section 1, recomputed through crossoverLinkStiffnessBracket"
    )
    reproductions += T322Reproduction(
        statement = "C-0208's radial bracket floor",
        published = 754.005141,
        here = radial.floor,
        relativeDeparture = abs(radial.floor - 754.005141) / 754.005141,
        source = "C-0208, recomputed through crossoverRadialLinkBracket"
    )
    reproductions += T322Reproduction(
        statement = "the through-thickness link at that floor",
        published = 629.20588,
        here = throughThickness(radial.floor),
        relativeDeparture = abs(throughThickness(radial.floor) - 629.20588) / 629.20588,
        source = "C-0208, recomputed through resolvedLinkStiffness"
    )
    reproductions += T322Reproduction(
        statement = "C-0017's mandate, on the SUM, at section 3's acceptable clause",
        published = 33.3333333,
        here = mandate,
        relativeDeparture = abs(mandate - 33.3333333) / 33.3333333,
        source = "C-0017"
    )
    reproductions += T322Reproduction(
        statement = "C-0023's per-path unzip allowable over section 3's acceptable stroke",
        published = 3.33333333,
        here = unzipCeiling,
        relativeDeparture = abs(unzipCeiling - 3.33333333) / 3.33333333,
        source = "C-0023, through perPathStiffnessCeiling"
    )
    val worstReproduction = reproductions.maxOf { it.relativeDeparture }
    println("  worst reproduction departure over " + reproductions.size + ": " +
            worstReproduction.emitted(2))

    // ================ the falsifiers
    println("T-322 - the falsifiers")
    // F1 -- per width, because two of the three rows carry a nodeS overhang and one does not.
    val uniformDishing = widths.maxOf { width ->
        val lattice = width.tethers.latticeAtRung(floorRung, enhancement, withPreload = false)
        val field = lattice.solve(uniformPressure(width.interiorPressure))
        field.peakDishing(T322_SAMPLES) / field.meanDeflection
    }
    // F2 -- the null-radial rung must be the standing single-scalar object, bit for bit.
    var loadIdentical = true
    var siteSetIdentical = true
    widths.forEach { width ->
        val nullRung = ResolvedLinkRung(
            "the standing scalar", "HoneycombGrillage's own default", T322_PENALTY, null
        )
        val here = width.tethers.latticeAtRung(nullRung, enhancement)
        val standing = width.tethers.lattice(enhancement, linkStiffness = T322_PENALTY)
        if (here.degreesOfFreedom != standing.degreesOfFreedom) loadIdentical = false
        else {
            val a = here.assembleLoad(width.pressureField)
            val b = standing.assembleLoad(width.pressureField)
            for (i in 0 until here.degreesOfFreedom) if (a[i] != b[i]) loadIdentical = false
        }
        if (here.bonds.map { it.site } != standing.bonds.map { it.site }) siteSetIdentical = false
    }
    // F3 -- the surrogate at full presence against the ASSEMBLED solve
    val assembledDeparture = run {
        val tile = tiles.getValue(tightWidth.pairedRowBasePairs to radial.floor)
        val response = tightestSearched.surrogate.solve(tightestSearched.stiffnesses)
        val assembled = tile.lattice.solve(
            tightWidth.pressureField,
            tightGrid.mapIndexed { index, (s, y) ->
                PointLoad(s, y, -response.supportForces[index])
            }
        ).peakDishing(T322_SAMPLES)
        abs(assembled - response.peakDishing) / abs(response.peakDishing)
    }
    // F5 -- the two surrogates must be the same object
    val surrogateDeparture = run {
        val tile = tiles.getValue(tightWidth.pairedRowBasePairs to radial.floor)
        val multi = honeycombMultiStateSurrogate(
            tile.lattice, tightGrid,
            singleLoadState("C-0022's solved collar", tightWidth.pressureField), T322_SAMPLES
        )
        val a = tightestSearched.surrogate.solve(tightestSearched.stiffnesses).peakDishing
        val b = multi.peakDishing(tightestSearched.stiffnesses)[0]
        abs(a - b) / abs(a)
    }
    // F8 -- the tethered and untied free strokes
    val strokeDeparture = widths.maxOf { width ->
        val tethered = width.tethers.latticeAtRung(floorRung, enhancement)
            .solve(uniformPressure(width.interiorPressure)).meanDeflection
        val untied = width.untiedAt(floorRung)
            .solve(uniformPressure(width.interiorPressure)).meanDeflection
        abs(tethered - untied) / untied
    }
    val uncoupledDeparture = reproductions
        .filter { it.statement.startsWith("C-0211's uncoupled free tile") }
        .maxOf { it.relativeDeparture }
    val censusDeparture = reproductions
        .filter { it.statement.startsWith("CH-0270's bond census") }
        .maxOf { it.relativeDeparture }
    val unitZInPlane = widths.maxOf {
        ResolvedLinkBondCensus(it.untiedAt(floorRung), floorRung).meanSquaredUnitZInPlane
    }
    val unitZThrough = widths.maxOf {
        abs(ResolvedLinkBondCensus(it.untiedAt(floorRung), floorRung)
            .meanSquaredUnitZThroughThickness - 0.75)
    }
    val floorViolations = oracle.count { row ->
        val here = cells.first {
            it.pairedRowBasePairs == row.pairedRowBasePairs && it.placement == row.placement &&
                    it.columns == row.columns
        }
        here.searchedP90 < row.p90FloorOverStroke * (1.0 - 1e-9)
    }
    val inSampleLosses = cells.count {
        it.searchedTrainingP90 > it.bestTransferredTrainingP90 * (1.0 + 1e-12)
    }
    val outOfSampleLosses = cells.count { it.searchedP90 > it.bestTransferredP90 }
    val outsideBand = cells.count { !it.insideTheCheapBoundsBand }
    // F19 -- the ranking of the three widths, coupled against C-0211's uncoupled
    val coupledOrder = widths
        .map { w -> w.pairedRowBasePairs to cells.filter {
            it.pairedRowBasePairs == w.pairedRowBasePairs
        }.minOf { it.searchedP90 } }
        .sortedBy { it.second }.map { it.first }
    val uncoupledOrder = widths
        .map { it.pairedRowBasePairs to it.reference.freeTileWithPreload }
        .sortedBy { it.second }.map { it.first }

    val falsifiers = listOf(
        T322Falsifier(
            "F1",
            "a uniform pressure on the FREE route-B tethered lattice at the resolved link, " +
                    "preload off, dishes more than 1e-9 of the stroke at any of the graded widths",
            false, uniformDishing > 1e-9,
            "worst over the widths: " + uniformDishing.emitted(2) + " of the stroke, taken per " +
                    "width because two of the three rows carry a nodeS overhang and one does not"
        ),
        T322Falsifier(
            "F2",
            "a route-B lattice at a null radial constant is not bit-identical to " +
                    "UniformRasterTethers.lattice's, on assembleLoad over every degree of " +
                    "freedom or on the crossover site set",
            false, !(loadIdentical && siteSetIdentical),
            "load vector identical: " + loadIdentical + ", site set identical: " +
                    siteSetIdentical + ", at all " + widths.size + " widths"
        ),
        T322Falsifier(
            "F3",
            "the surrogate at full presence does not reproduce the ASSEMBLED route-B solve " +
                    "with its own Woodbury support forces applied as point loads, at 1e-9 relative",
            false, assembledDeparture > 1e-9,
            "departure " + assembledDeparture.emitted(2) + " at the deciding cell"
        ),
        T322Falsifier(
            "F4",
            "two independent runs of the study do not produce a byte-identical result file",
            false, false,
            "discharged OUTSIDE the study by a second emission in a separate snapshot, diffed " +
                    "against the artifact; every search decision is rounded at six significant " +
                    "digits and no wall clock or step count is emitted"
        ),
        T322Falsifier(
            "F5",
            "the one-state MultiStateSurrogate and the InfluenceSurrogate disagree about the " +
                    "peak dishing of one distribution by more than 1e-10 relative",
            false, surrogateDeparture > 1e-10,
            "departure " + surrogateDeparture.emitted(2) + " at the deciding cell"
        ),
        T322Falsifier(
            "F6",
            "the uncoupled route-B reading here does not reproduce C-0211's committed " +
                    "freeTileWithPreload at every (width, b0, chain corner, rung) graded, to " +
                    "1e-8 relative",
            false, uncoupledDeparture > 1e-8,
            "worst departure " + uncoupledDeparture.emitted(2) + " over " + widths.size +
                    " widths"
        ),
        T322Falsifier(
            "F7",
            "the bond census is not 358 / 385 / 410, or mean unitZ^2 is not exactly 0 in plane " +
                    "and 0.75 through the thickness",
            false, censusDeparture > 0.0 || unitZInPlane != 0.0 || unitZThrough > 1e-12,
            "census departure " + censusDeparture.emitted(2) + ", in-plane mean unitZ^2 " +
                    unitZInPlane.emitted(2) + ", through-thickness departure from 0.75 " +
                    unitZThrough.emitted(2)
        ),
        T322Falsifier(
            "F8",
            "the free stroke of the tethered lattice and of the untied one differ by more than " +
                    "1e-9 relative at any width",
            false, strokeDeparture > 1e-9,
            "worst departure " + strokeDeparture.emitted(2) + "; a uniform load translates a " +
                    "free tile rigidly, so it is element-independent, and this is what " +
                    "licenses F6"
        ),
        T322Falsifier(
            "F9", "a searched p90 falls BELOW the oracle p90 floor at any cell",
            false, floorViolations > 0,
            floorViolations.toString() + " of " + cells.size + " cells; it is a theorem"
        ),
        T322Falsifier(
            "F10",
            "the searched distribution's IN-SAMPLE training objective is worse than the best " +
                    "of its own starts at any cell",
            false, inSampleLosses > 0,
            inSampleLosses.toString() + " of " + cells.size + " cells; the descent is seeded " +
                    "from its own comparands, so this is a property of the composition"
        ),
        T322Falsifier(
            "F11",
            "a distribution searched at the resolved per-bond link puts at least one ROUTE-B " +
                    "coupled cell inside T-5b's " + T322_TOLERANCE.emitted(2) +
                    " at the 90th percentile of the grading ensemble",
            true, flatSearched > 0,
            flatSearched.toString() + " of " + cells.size + " searched cells are flat, against " +
                    flatTransferred + " on a transferred rule; the tightest searched reading is " +
                    cells.minOf { it.searchedP90 }.emitted(9) + " at " + decidingLabel
        ),
        T322Falsifier(
            "F12",
            "T-316's inherited ladder phase " + T322_INHERITED_PHASE + " at the " +
                    T322_LADDER_STAGGER + " bp stagger cannot carry a 5-column placement at " +
                    "some route-B width",
            true, inheritedPhaseRefuses > 0,
            "refused at " + inheritedPhaseRefuses + " of " + widths.size + " widths; route B's " +
                    "rows carry at most " + routeBMaximumColumns + " station columns against " +
                    "the 116 bp block extent's " + blockMaximumColumns + ", at every phase"
        ),
        T322Falsifier(
            "F13",
            "no cell of the sweep is both flat at the 90th percentile AND inside C-0023's " +
                    "per-path allowable " + unzipCeiling.emitted(9) + " pN/nm",
            true, census[0].flatAndAdmissible == 0,
            "flat " + flatSearched + ", inside the allowable " +
                    cells.count { it.peakInsideUnzipCeiling } + ", both " +
                    census[0].flatAndAdmissible + " of " + cells.size
        ),
        T322Falsifier(
            "F14",
            "no coupled cell beats the UNCOUPLED route-B tile at the 90th percentile",
            true, census[0].beatsUncoupled == 0,
            census[0].beatsUncoupled.toString() + " of " + cells.size + " cells beat it; " +
                    "C-0211 shows the uncoupled tile is flat at 756 of 756, so the coupling is " +
                    "not there for flatness"
        ),
        T322Falsifier(
            "F15",
            "a cell that clears at the 90th percentile loses the verdict when its worst SINGLE " +
                    "path is removed",
            true, fragileFlat > 0,
            fragileFlat.toString() + " of " + flatSearched + " flat cells lose the verdict to " +
                    "one missing path"
        ),
        T322Falsifier(
            "F16",
            "the verdict at the deciding cell moves across C-0208's radial rungs, or between " +
                    "the resolved link and the 1e4 penalty",
            true, rungVerdictMoves,
            rungRows.joinToString("; ") {
                it.radialLinkStiffness.emitted(9) + " -> " + it.searchedP90.emitted(9)
            }
        ),
        T322Falsifier(
            "F17",
            "the searched distribution's OUT-OF-SAMPLE p90 is worse than the best transferred " +
                    "distribution's at any cell",
            true, outOfSampleLosses > 0,
            outOfSampleLosses.toString() + " of " + cells.size + " cells; the search wins out " +
                    "of sample at " + census[0].searchedBeatsTransferredCells
        ),
        T322Falsifier(
            "F18",
            "the flat verdict at a route-B width differs from C-0212's paired verdict at the " +
                    "same (placement, columns, fraction) on the 116 bp block",
            true, pairedVerdictMoves > 0,
            pairedVerdictMoves.toString() + " of " + paired.size + " paired cells move; route " +
                    "B over the block runs " + paired.minOf { it.routeBOverBlock }.emitted(9) +
                    " to " + paired.maxOf { it.routeBOverBlock }.emitted(9)
        ),
        T322Falsifier(
            "F19",
            "the three widths rank differently on the COUPLED p90 than on C-0211's UNCOUPLED " +
                    "reading",
            true, coupledOrder != uncoupledOrder,
            "uncoupled " + uncoupledOrder.joinToString(" < ") { it.toString() + " bp" } +
                    "; coupled " + coupledOrder.joinToString(" < ") { it.toString() + " bp" }
        ),
        T322Falsifier(
            "F20",
            "the searched p90 at some cell falls outside the cheap bound's own predicted band",
            true, outsideBand > 0,
            outsideBand.toString() + " of " + cells.size + " cells fall outside; the band is a " +
                    "ratio carried between two lattices and a miss measures how much of C-0212 " +
                    "is a property of its own tile"
        )
    )
    falsifiers.forEach {
        println("  " + it.id + (if (it.fired) "  FIRED" else "  did not fire") +
                (if (it.declaredOpen) "  (declared OPEN)" else ""))
    }

    // ================ the verdict, the findings, and the emission
    val verdict = mapOf(
        "does a SEARCHED distribution clear T-5b at the 90th percentile on ROUTE B's own tile" to
                (if (flatSearched > 0) "YES at " + flatSearched + " of " + cells.size + " cells"
                else "NO at any of " + cells.size + " cells") +
                ", against " + flatTransferred + " on a transferred rule",
        "is any flat cell ALSO inside C-0023's per-path allowable -- the CONJUNCTION" to
                (if (census[0].flatAndAdmissible > 0)
                    "YES at " + census[0].flatAndAdmissible + " of " + cells.size
                else "NO -- 0 of " + cells.size + ", flat " + flatSearched + " and admissible " +
                        cells.count { it.peakInsideUnzipCeiling } + " separately"),
        "does any coupled cell beat the UNCOUPLED route-B tile, which C-0211 shows is flat" to
                (if (census[0].beatsUncoupled > 0)
                    "YES at " + census[0].beatsUncoupled + " of " + cells.size
                else "NO -- 0 of " + cells.size + "; the uncoupled tile reads " +
                        widths.minOf { it.reference.freeTileWithPreload }.emitted(9) + " to " +
                        widths.maxOf { it.reference.freeTileWithPreload }.emitted(9) +
                        " and the best searched cell " + cells.minOf { it.searchedP90 }
                    .emitted(9)),
        "how many station columns does route B's own row carry" to
                (routeBMaximumColumns.toString() + " against the 116 bp block extent's " +
                        blockMaximumColumns + ", and T-316's inherited ladder phase " +
                        T322_INHERITED_PHASE + " carries a 5-column placement at " +
                        (widths.size - inheritedPhaseRefuses) + " of " + widths.size +
                        " route-B widths"),
        "does the TILE substitution move the verdict against C-0212's paired cells" to
                (if (pairedVerdictMoves > 0) "YES at " + pairedVerdictMoves + " of " +
                        paired.size + " paired cells" else "NO at any of " + paired.size),
        "the tightest searched reading, and where" to
                (cells.minOf { it.searchedP90 }.emitted(9) + " at " + decidingLabel),
        "the ratio the argmin demands at that cell, against C-0060's FLAT window" to
                (tightCell.searchedRatio.emitted(9) + ", " +
                        (if (tightCell.ratioInsideFlatRatioWindow) "inside" else "OUTSIDE") +
                        " [" + T322_FLAT_RATIO_FLOOR.emitted(2) + ", " +
                        T322_FLAT_RATIO_CEILING.emitted(2) + "] -- which CH-0273 establishes is " +
                        "a FLATNESS window measured on a square-lattice 45-station design and " +
                        "not a buildability constraint"),
        "does the cheap bound's transferred ratio band hold" to
                (if (outsideBand == 0) "YES at all " + cells.size + " cells"
                else "NO at " + outsideBand + " of " + cells.size + " cells"),
        "does the verdict move across the rung ladder" to
                (if (rungVerdictMoves) "YES" else "NO -- " + rungRows.joinToString("; ") {
                    it.radialLinkStiffness.emitted(9) + " -> " + it.searchedP90.emitted(9)
                })
    )

    val findings = listOf(
        "Route B's own buildable widths carry " + routeBMaximumColumns + " station columns per " +
                "rooting helix against the 116 bp block extent's " + blockMaximumColumns +
                ", at every one of the 21 ladder phases, and T-316's inherited phase " +
                T322_INHERITED_PHASE + " carries a 5-column placement at " +
                (widths.size - inheritedPhaseRefuses) + " of " + widths.size + " of them. The " +
                "station set a coupled census stands on is a function of the ROW LENGTH, and " +
                "it is pure lattice arithmetic -- no solve, no ensemble.",
        "Graded on route B's own tile, at stations derived at each row length, " + flatSearched +
                " of " + cells.size + " searched cells are flat at the 90th percentile and " +
                flatTransferred + " on a transferred rule. The tightest searched reading is " +
                cells.minOf { it.searchedP90 }.emitted(9) + " against the tightest transferred " +
                cells.minOf { it.bestTransferredP90 }.emitted(9) + ".",
        "The conjunction CH-0272 asks for: " + census[0].flatAndAdmissible + " of " + cells.size +
                " cells are flat AND inside C-0023's per-path allowable, and " +
                census[0].allThreeThresholds + " are also better than the uncoupled tile. " +
                "C-0211 shows the UNCOUPLED route-B tile is already flat at 756 of 756, so on " +
                "this tile the coupling is not a flatness remedy at all -- it is a mandate the " +
                "flatness has to survive.",
        "The uncoupled tile is beaten at " + census[0].beatsUncoupled + " of " + cells.size +
                " cells. C-0212 read 0 of 32 on the 116 bp block and the direction transfers; " +
                "what does not transfer is the LEVEL, because route B's uncoupled tile is " +
                "flatter than route A's and its coupled cells are read against a smaller tile, " +
                "a higher interior pressure and a different dropout field.",
        "Against C-0212's own paired cells the tile substitution moves the flat verdict at " +
                pairedVerdictMoves + " of " + paired.size + " cells, and route B over the " +
                "block runs " + paired.minOf { it.routeBOverBlock }.emitted(9) + " to " +
                paired.maxOf { it.routeBOverBlock }.emitted(9) + " on the deciding statistic. " +
                "That is the number the objection 'the stations belong to a different tile' " +
                "was worth."
    )

    val validity = listOf(
        "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated. No route-B " +
                "coupling has been drawn, let alone folded.",
        "The search is over the STIFFNESS vector alone, subject to C-0017's mandate on the SUM. " +
                "The placement family, the cross-section, the raster, the load case, the link " +
                "resolution and the radial bracket do not move.",
        "The composite fraction is C-0116's " + T322_FRACTION.emitted(3) + " at every cell of " +
                "the sweep and " + T322_ALTERNATE_FRACTION.emitted(3) + " is carried at the " +
                "deciding cell only, as a convergence row. C-0141's forced inter-row stagger is " +
                T322_LADDER_STAGGER + " bp at every cell and " + T322_ALTERNATE_STAGGER +
                " bp at the deciding cell only. Both are deliberate scalings, named in T-322's " +
                "own Plan before the run and not silent caps.",
        "T-315's 21 lattice phases and 12 chain corners are NOT re-swept. Each width is graded " +
                "at C-0211's own recommended b0 and at the WORST of its twelve corners at that " +
                "b0, which is the reading the recommendation is a minimax over -- so every " +
                "coupled cell here is judged against the free-tile number C-0211 publishes.",
        "The ladder PHASE is derived by a stated rule -- the smallest phase maximising the " +
                "minimum station count per rooting helix, ties to the earlier phase -- because " +
                "C-0141's +/-5 bp rule fixes it only where the raster CLOSES and route B's " +
                "uniform rows close at no phase. A different rule would give a different " +
                "placement, and the stagger row of the convergence block is how much that is " +
                "worth here.",
        "The percentile is an ORDER STATISTIC, so C-0135's log-sum-exp smoothing and its " +
                "adjoint do NOT transfer to it. They are applied to the zero-defect peak, which " +
                "IS a max of smooth functions, and the percentile is searched by a multi-start " +
                "coordinate descent with every decision rounded at six significant digits.",
        "The COUNT of flat cells is out of sample; WHICH cell is tightest is an order statistic " +
                "over " + cells.size + " cells read on the grading stream, so the tightest " +
                "value carries a selection the count does not.",
        "C-0060's [3.5, 20] is emitted beside every cell and is named a FLATNESS window, not a " +
                "buildability one (CH-0273). The one physical per-path threshold here is " +
                "C-0023's unzip allowable read over section 3's acceptable stroke.",
        "The transverse constant is pinned at C-0205's ceiling throughout, which is its " +
                "generous reading, and the radial constant is a bracket of constructions with " +
                "one measured term in it (C-0208), which is why the deciding cell is re-graded " +
                "over the whole ladder including the 1e4 penalty.",
        "Every bond of this lattice is still missing CH-0242's common-mode spring, the lattice " +
                "carries no steric floor between two duplexes, and k_theta is Gen1Tile's " +
                "square-lattice-fitted constant. The nine in-plane raster turns contribute " +
                "exactly zero preload because this model has no in-plane transverse coordinate, " +
                "and that zero is a property of the MODEL."
    )

    val openQuestions = listOf(
        "Whether a search over the PLACEMENT and the distribution together reaches what neither " +
                "reaches alone on route B's tile. C-0063 records that which stations a coupling " +
                "enters at is worth more than how its stiffness is distributed, and route B's " +
                "ladder is one column narrower than the block's, so the placement axis is " +
                "smaller here and the question is sharper.",
        "What the ladder phase is worth as a design variable. It is DERIVED here by one stated " +
                "rule; C-0207 measures the tether lattice constant b0 at up to 1.82562517x on " +
                "the free tile, and nothing has swept the STATION phase on a coupled cell.",
        "Whether a shared-body topology -- C-0017's mandate spent once in a rigid-body mode " +
                "rather than at every station -- changes the answer. It is a change of TOPOLOGY " +
                "and not of distribution, and the dishing projector annihilates a rigid-body " +
                "mode by construction.",
        "Whether route B should trade paired row length against tether span at all. These " +
                "three widths are the maximum each scaffold affords, and a shorter row buys a " +
                "smaller tile and spends a station column."
    )

    val result = T322Result(
        task = "T-322",
        claim = "C-0215",
        leaf = "A8.2",
        question = "Every coupled number in this corpus is read on the 116 bp block extent of " +
                "route A's drawable raster. Route B's own buildable widths -- 92 / 98 / 106 bp " +
                "-- have never been graded COUPLED, and C-0211 shows their FREE tile is flat. " +
                "Does the mandated coupling, on stations derived at each row length, destroy " +
                "that flatness?",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated. " +
                "Every number is a property of one lattice, one placement family, one raster, " +
                "one load case and one dropout model, read at a radial link constant C-0208 " +
                "records as unsourceable and carries as a bracket.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm",
            "pressure" to "pN/nm^2 = 1 MPa",
            "dishing" to "dimensionless, as a fraction of the free stroke",
            "ratio" to "dimensionless, max over min of a per-path stiffness vector",
            "basePairs" to "integer, at 0.34 nm per rise"
        ),
        conventions = mapOf(
            "s" to "along the helices, origin at the face centre",
            "y" to "across the helices in the plane of the face, origin at the face centre",
            "W" to "positive DOWNWARD, toward the electrode (C-0006)",
            "dishing" to "the peak of |w - the best-fit rigid plane| over an " + T322_SAMPLES +
                    " x " + T322_SAMPLES + " face grid, divided by the free-tile stroke of the " +
                    "UNTIED lattice at the same rung -- T-315's convention, and F8 asserts " +
                    "that the two agree",
            "pairedRowBasePairs" to "scaffoldNucleotides / 60 - 28, C-0193's built allowance",
            "resolvedLink" to "k_radial * unitZ^2 + k_transverse * unitY^2 per bond (C-0208), " +
                    "with k_transverse pinned at C-0205's ceiling",
            "ladderPhase" to "DERIVED per width: the smallest phase in [0, 21) maximising the " +
                    "minimum station count per rooting helix, ties to the earlier phase",
            "transferred" to "a distribution RULE evaluated on the station set -- C-0058's " +
                    "equal springs and its rim-graded 5:1 at a " + T322_RIM_BAND.emitted(3) +
                    " nm band",
            "searched" to "C-0135's smoothed minimax on the zero-defect peak composed with a " +
                    "multi-start coordinate descent on the TRUE training percentile",
            "outOfSample" to "graded on the " + T322_GRADING_SEED + " stream, which the search " +
                    "never sees; the search sees the " + T322_TRAINING_SEED + " stream"
        ),
        parameters = mapOf(
            "crossSection" to (block.rasterRows.toString() + " x " + block.helicesPerRow),
            "pairedRowBasePairs" to widths.joinToString(" / ") { it.pairedRowBasePairs.toString() },
            "scaffolds" to widths.joinToString(" / ") { it.scaffold },
            "edgeX" to widths.joinToString(" / ") { it.edgeX.emitted(9) },
            "edgeY" to edgeY.emitted(9),
            "ladderPhases" to widths.joinToString(" / ") { it.ladder.derivedPhase.toString() },
            "ladderStagger" to T322_LADDER_STAGGER.toString(),
            "classZeroResidues" to
                    widths.joinToString(" / ") { it.reference.classZeroResidue.toString() },
            "chainCorners" to widths.joinToString(" | ") { it.reference.chain },
            "kuhnLengths" to widths.joinToString(" / ") { it.reference.kuhnLength.emitted(3) },
            "contourPerNucleotide" to
                    widths.joinToString(" / ") { it.reference.contourPerNucleotide.emitted(3) },
            "interhelicalDistance" to d.emitted(9),
            "hingeStiffness" to kTheta.emitted(9),
            "hingeStiffnessEnhancement" to enhancement.emitted(9),
            "transverseConstant" to shearCeiling.emitted(9),
            "radialBracketFloor" to radial.floor.emitted(9),
            "radialBracketCeiling" to radial.ceiling.emitted(9),
            "throughThicknessLinkAtTheFloor" to throughThickness(radial.floor).emitted(9),
            "foundationStiffness" to Gen1Tile.FOUNDATION_SECANT.emitted(9),
            "compositeFraction" to (T322_FRACTION.emitted(3) + " (C-0116), with " +
                    T322_ALTERNATE_FRACTION.emitted(3) + " at the deciding cell only"),
            "mandate" to ("C-0017's " + mandate.emitted(9) + " pN/nm on the SUM, section 3's " +
                    "acceptable clause"),
            "perPathAllowable" to (unzipCeiling.emitted(9) + " pN/nm (C-0023's 10 pN unzip " +
                    "allowable over section 3's acceptable 3 nm stroke)"),
            "gradingRealisations" to t322Realisations.toString(),
            "gradingSeed" to T322_GRADING_SEED.toString(),
            "trainingRealisations" to t322TrainingRealisations.toString(),
            "trainingSeed" to T322_TRAINING_SEED.toString(),
            "columnCounts" to t322Columns.joinToString(", "),
            "samples" to T322_SAMPLES.toString(),
            "searchSamples" to T322_SEARCH_SAMPLES.toString(),
            "smoothingLevels" to T322_SMOOTHING_LEVELS.joinToString(", ") { it.emitted(3) },
            "smoothingIterationsPerLevel" to T322_SMOOTHING_ITERATIONS.toString(),
            "smoothedMinimaxPolishSweeps" to T322_POLISH_SWEEPS.toString(),
            "percentileSweeps" to T322_PERCENTILE_SWEEPS.toString(),
            "percentileScanPoints" to T322_SCAN_POINTS.toString(),
            "percentileRefinements" to T322_REFINEMENTS.toString(),
            "tolerance" to T322_TOLERANCE.emitted(2),
            "rimBand" to T322_RIM_BAND.emitted(3),
            "flatRatioWindow" to (T322_FLAT_RATIO_FLOOR.emitted(2) + " to " +
                    T322_FLAT_RATIO_CEILING.emitted(2) + " (C-0060, a FLATNESS window -- CH-0273)"),
            "smoke" to t322Smoke.toString()
        ),
        sources = listOf(
            ResultInputs.T_3B.path + " (C-0022's solved collar at 2 mM / 10 nm / 0.192 V)",
            ResultInputs.T_315.path + " (C-0211's uncoupled route-B free tile: the recommended " +
                    "b0, the worst chain corner and the reading every coupled cell is judged " +
                    "against)",
            ResultInputs.T_316.path + " (C-0212's own 116 bp block cells: the transferred " +
                    "ratio band that is this study's cheap bound, and the paired comparands)"
        ),
        citedInputs = mapOf(
            "C-0211 uncoupled route-B free tile, 756 of 756 flat" to
                    "0.048606444 to 0.0960647281 of the stroke",
            "C-0212 searched census on the 116 bp block" to "22 of 32 flat, 0 of 32 beating " +
                    "the uncoupled tile",
            "C-0208 census on the two transferred rules" to "0 of 64",
            "C-0205 shear ceiling, the transverse constant" to "254.808095 pN/nm",
            "C-0208 radial bracket floor" to "754.005141 pN/nm",
            "CH-0270 bond census at 92 / 98 / 106 bp" to "358 / 385 / 410",
            "C-0141 honeycomb station ladder" to "phase + 21k, with a forced 7 or 14 bp " +
                    "inter-row stagger",
            "T-5b flatness tolerance" to "0.10 of the free stroke"
        ),
        cheapBound = cheapBound,
        ladder = ladderRows,
        predictions = predictions,
        oracle = oracle,
        cells = cells,
        paired = paired,
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

    val output = File("gpd/results/T-322-route-b-coupled-on-its-own-stations.json")
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
    println("T-322 - wrote " + output.path)
}
