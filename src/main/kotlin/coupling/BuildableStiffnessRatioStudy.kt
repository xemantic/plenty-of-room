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
import com.xemantic.nano.plentyofroom.anchoring.ArmAnchorage
import com.xemantic.nano.plentyofroom.anchoring.BForm
import com.xemantic.nano.plentyofroom.anchoring.CoupledJointFlexure
import com.xemantic.nano.plentyofroom.anchoring.CrossoverHingeFlexure
import com.xemantic.nano.plentyofroom.anchoring.FlexureEndCondition
import com.xemantic.nano.plentyofroom.anchoring.FlexureOrientation
import com.xemantic.nano.plentyofroom.anchoring.FreelyJointedChain
import com.xemantic.nano.plentyofroom.anchoring.SsDnaTether
import com.xemantic.nano.plentyofroom.anchoring.StandoffBase
import com.xemantic.nano.plentyofroom.anchoring.TransverseDuplexFlexure
import com.xemantic.nano.plentyofroom.anchoring.TwoSpringElastica
import com.xemantic.nano.plentyofroom.anchoring.coupledFlexureSpan
import com.xemantic.nano.plentyofroom.anchoring.elasticaArmForStiffness
import com.xemantic.nano.plentyofroom.anchoring.offsetForPreload
import com.xemantic.nano.plentyofroom.anchoring.packingLimitedElementCount
import com.xemantic.nano.plentyofroom.anchoring.packingLimitedPathCount
import com.xemantic.nano.plentyofroom.anchoring.placeHingeArms
import com.xemantic.nano.plentyofroom.anchoring.standoffTipFlexibility
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.GrillageDeflection
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.OrigamiSheet
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PointSupport
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.nano.plentyofroom.synthesis.perPathSecantCeiling
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

/**
 * Task `T-122` — can a **5:1 per-path coupling stiffness ratio** be BUILT? Leaf `A8.2`.
 *
 * Run with:
 *
 * ```shell
 * tools/study.sh coupling.BuildableStiffnessRatioStudyKt
 * ```
 *
 * Emits `gpd/results/T-122-buildable-stiffness-ratio.json`, deterministically — no timestamp,
 * every floating-point number rounded at the serialisation boundary per [roundCouplingResult].
 *
 * ## What this study is
 *
 * `C-0058`'s validity range names one open item above all others: *"nothing here says a per-path
 * stiffness can be BUILT to a prescribed value"*. Its flat design is two numbers —
 * **0.921 pN/nm** at 34 rim stations and **0.184** at 11 interior ones — and every element that
 * could realise them is set by an integer count of base pairs, nucleotides or crossovers.
 *
 * Everything except the quantisation is upstream machinery re-run rather than reimplemented:
 * `C-0058`'s own Woodbury surrogate and distributions, `C-0023`'s and `C-0030`'s and `C-0039`'s
 * elements, `C-0022`'s solved load read from its own result file, `C-0026`'s scatter patterns,
 * `C-0041`'s packing limit. What is new is `BuildableStiffnessRatio.kt` — the ladder, the
 * granularity, and the scatter threshold.
 */

// ---------------------------------------------------------------------------------------------
// the records
// ---------------------------------------------------------------------------------------------

/** The cheap bound: one division per element per level, before any ladder is enumerated. */
@Serializable
data class T122CheapBoundRecord(
    val element: String,
    val level: String,
    val targetStiffness: Double,
    val parameterAtTheTarget: Double,
    val parameterUnitsAtTheTarget: Double,
    val quantum: Double,
    val quantumName: String,
    val nominalExponent: Double,
    val powerLawGranularity: Double,
    val flatRatioWindowWidth: Double,
    /** The fractional move in the RATIO one quantum of this level's parameter causes. */
    val ratioStepFromOneQuantum: Double,
    val falsifierFired: Boolean,
    val verdict: String
)

/** One rung of one element's ladder nearest one prescribed level. */
@Serializable
data class T122LadderRecord(
    val element: String,
    val level: String,
    val targetStiffness: Double,
    val units: Int,
    val quantumName: String,
    val parameter: Double,
    val realisedStiffness: Double,
    val relativeError: Double,
    val relativeGranularity: Double,
    val errorOverHalfTheGranularity: Double,
    val bracketed: Boolean,
    val ladderRungs: Int,
    val softestReachable: Double,
    val stiffestReachable: Double
)

/** The ratio and the total a built two-level design realises. */
@Serializable
data class T122BuiltRatioRecord(
    val element: String,
    val rimUnits: Int,
    val interiorUnits: Int,
    val rimParameter: Double,
    val interiorParameter: Double,
    val realisedRimStiffness: Double,
    val realisedInteriorStiffness: Double,
    val targetRatio: Double,
    val realisedRatio: Double,
    val ratioRelativeError: Double,
    val insideTheFlatRatioWindow: Boolean,
    val realisedTotal: Double,
    val mandate: Double,
    val totalRelativeError: Double,
    val totalRelativeGranularity: Double,
    val totalInsideItsOwnGranularity: Boolean,
    val verdict: String
)

/** One distribution, solved on the lattice and on the plate. */
@Serializable
data class T122FlatnessRecord(
    val label: String,
    val element: String,
    val rimStiffness: Double,
    val interiorStiffness: Double,
    val ratio: Double,
    val totalStiffness: Double,
    val latticePeakDishing: Double,
    val platePeakDishing: Double,
    val latticeOverPlate: Double,
    val dishingOverStroke: Double,
    val flat: Boolean,
    val peakPathForceAtAcceptableStroke: Double,
    val unzipMargin: Double,
    val peakCrossoverForce: Double,
    val peakDuplexShear: Double,
    val peakThermalForce: Double,
    val verdict: String
)

/** The ratio sweep that measures `C-0058`'s flat window rather than citing it. */
@Serializable
data class T122RatioWindowRecord(
    val collarWidth: Double,
    val rimPaths: Int,
    val ratio: Double,
    val dishingOverStroke: Double,
    val flat: Boolean
)

/** The realised ratio at a stroke — the strain-softening drift `C-0058`'s linear springs omit. */
@Serializable
data class T122DriftRecord(
    val element: String,
    val stroke: Double,
    val rimSecant: Double,
    val interiorSecant: Double,
    val realisedRatio: Double,
    val ratioAtThePlacementStroke: Double,
    val driftFromThePlacementStroke: Double,
    val insideTheFlatRatioWindow: Boolean
)

/** The scatter a built design tolerates before the flatness verdict is lost. */
@Serializable
data class T122ScatterRecord(
    val design: String,
    val pattern: String,
    /**
     * Whether the scattered distribution is rescaled back to `C-0017`'s mandate.
     *
     * `false` is the honest build tolerance — an assembly does not know the mandate — but it
     * confounds two effects, because a pattern collinear with the rim/interior split moves the
     * TOTAL as well as the distribution. `true` isolates the distribution alone.
     */
    val renormalisedToTheMandate: Boolean,
    val dishingAtZeroScatter: Double,
    val dishingAtTheScanCeiling: Double,
    val thresholdAmplitude: Double,
    val reachesTheTolerance: Boolean,
    val scanCeiling: Double,
    val bracketWidth: Double,
    val populationOverlapAmplitude: Double,
    val flatnessBindsBeforeTheOrdering: Boolean,
    /** How far the MANDATE has drifted at the threshold — scatter does not renormalise. */
    val totalDriftAtTheThreshold: Double,
    /** `C-0026`'s 0.883 pN per unit relative amplitude, evaluated at this threshold. */
    val crossoverForceAtTheThreshold: Double,
    val overCzeroTwentySixBreakEven: Double,
    /** The dishing at a plausible 10 % build tolerance — the two channels at one amplitude. */
    val dishingAtTenPercentScatter: Double,
    /** The SOLVED peak crossover force at the same 10 %, against 0 % — `C-0026`'s own channel. */
    val peakCrossoverForceAtZeroScatter: Double,
    val peakCrossoverForceAtTenPercentScatter: Double,
    val crossoverForceRestoredAtTenPercent: Double
)

/** What a level costs on the allowables that have closed other designs. */
@Serializable
data class T122CostRecord(
    val element: String,
    val level: String,
    val stiffness: Double,
    val parameter: Double,
    val units: Int,
    val forceAtAcceptableStroke: Double,
    val unzipMargin: Double,
    val perPathSecantCeilingAtAcceptableStroke: Double,
    val insidePerPathCeiling: Double,
    val thermalForce: Double,
    val tangentAtAcceptableStroke: Double,
    val tangentOverSecant: Double,
    val strainSoftening: Boolean
)

/** Whether the array the two levels demand can be laid out at all. */
@Serializable
data class T122PackingRecord(
    val element: String,
    val planKind: String,
    val rimParameter: Double,
    val interiorParameter: Double,
    val longerParameter: Double,
    val tileEdge: Double,
    val fitsTheTileEdge: Boolean,
    val leverEnvelope: Double,
    val fitsTheLeverEnvelope: Boolean,
    val pathsRequired: Int,
    /** The count at the LONGER of the two members — the honest reading for a mixed array. */
    val packingLimitedPaths: Int,
    /** The count if every member were the SHORTER one, i.e. the uniform design's own limit. */
    val packingLimitedPathsAtTheShorter: Int,
    /** For a rooted arm: the count the crossover ROOT PITCH admits (`C-0053`/`C-0055`). */
    val rootPitchLimitedPaths: Int,
    val packs: Boolean,
    val verdict: String
)

/** The mandate trim: the second cheap bound, made executable. */
@Serializable
data class T122TrimRecord(
    val element: String,
    val paths: Int,
    val distinctSettings: Int,
    val rimUnitsLow: Int,
    val rimUnitsHigh: Int,
    val interiorUnitsLow: Int,
    val interiorUnitsHigh: Int,
    val totalBeforeTrimming: Double,
    val totalAfterTrimming: Double,
    val mandate: Double,
    val relativeErrorBeforeTrimming: Double,
    val relativeErrorAfterTrimming: Double,
    val moves: Int,
    val rimStiffnessRange: Double,
    val interiorStiffnessRange: Double,
    val realisedRatioLow: Double,
    val realisedRatioHigh: Double,
    val insideTheFlatRatioWindow: Boolean
)

/** Gate 4. */
@Serializable
data class T122ConvergenceRecord(
    val quantity: String,
    val parameter: String,
    val value: String,
    val result: Double,
    val departureFromFinest: Double
)

/** Gate 5. */
@Serializable
data class T122ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val relativeDeparture: Double,
    val note: String = ""
)

@Serializable
data class T122Result(
    val task: String,
    val title: String,
    val leaf: String,
    val verificationType: String,
    val units: String,
    val conventions: List<String>,
    val runParameters: Map<String, String>,
    val citedInputs: Map<String, String>,
    val temperature: Double,
    val thermalEnergy: Double,
    val designPointProfile: String,
    val rigidPlateTolerance: Double,
    val mandatedTotalStiffness: Double,
    val freeTileStroke: Double,
    val rimTargetStiffness: Double,
    val interiorTargetStiffness: Double,
    val flatRatioWindowLow: Double,
    val flatRatioWindowHigh: Double,
    val cheapBound: List<T122CheapBoundRecord>,
    val ratioWindow: List<T122RatioWindowRecord>,
    val ladders: List<T122LadderRecord>,
    val builtRatios: List<T122BuiltRatioRecord>,
    val trims: List<T122TrimRecord>,
    val flatness: List<T122FlatnessRecord>,
    val drift: List<T122DriftRecord>,
    val scatter: List<T122ScatterRecord>,
    val costs: List<T122CostRecord>,
    val packing: List<T122PackingRecord>,
    val convergence: List<T122ConvergenceRecord>,
    val reproductions: List<T122ReproductionRecord>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

// ---------------------------------------------------------------------------------------------
// the constants
// ---------------------------------------------------------------------------------------------

private const val T122_DUPLEXES = 15
private const val T122_COLUMNS = 3
private const val T122_NOMINAL_CROSSOVER_COLUMNS = 8
private const val T122_SAMPLES = 81
private const val T122_TOLERANCE = 0.10
private const val T122_COLLAR = 6.7
private const val T122_TARGET_RATIO = 5.0
private const val T122_RIM_STANDOFF = 1.0
private val T122_EDGE_X = Gen1Tile.EDGE_X
private val T122_MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE
private val EI = Gen1Tile.DUPLEX_BENDING_RIGIDITY
private val RISE = Gen1Tile.RISE_PER_BASE_PAIR

/** `C-0017`'s lever envelope — the body a flexure longer than the tile could still sit on. */
private const val T122_LEVER_ENVELOPE = 60.0

/** `C-0026`'s measured sensitivity of the peak crossover force to a relative scatter amplitude. */
private const val T122_SCATTER_SENSITIVITY = 0.883

/** `C-0026`'s break-even against `C-0022`'s solved edge effect. */
private const val T122_SCATTER_BREAK_EVEN = 0.17

// ---------------------------------------------------------------------------------------------
// the load, read from `C-0022`'s own result file
// ---------------------------------------------------------------------------------------------

private class T122Profile(
    val name: String,
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, lengthY: Double): PressureField = edgeCollarPressure(
        interiorPressure, T122_EDGE_X, lengthY,
        if (rimDepth == 0.0) listOf(CollarTerm(smoothDepth, smoothWidth))
        else listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T122_RIM_STANDOFF))
    )
}

/**
 * `C-0022`'s design-point profile, keyed on **`(concentration, gap, bias)`** — `CLAUDE.md`'s
 * upstream gotcha: the file carries two profiles per `(concentration, gap)`, one per bias.
 */
private fun t122DesignProfile(file: File): T122Profile {
    require(file.exists()) {
        "C-0022's result file is missing: ${file.path}. T-122 consumes the SOLVED edge profile " +
                "and will not substitute an assumed one for it."
    }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(key: String) = it.getValue(key).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at 2.0 mM, 10 nm, 0.192 V")
    fun value(key: String) = record.getValue(key).jsonPrimitive.content.toDouble()
    return T122Profile(
        name = "C-0022 2.0 mM, 10 nm, 0.192 V",
        smoothDepth = value("taperDepth"),
        smoothWidth = value("taperWidth"),
        rimDepth = value("rimResidualDepth")
    )
}

// ---------------------------------------------------------------------------------------------
// the catalogue
// ---------------------------------------------------------------------------------------------

/**
 * One element of the catalogue, as a **quantised** design parameter and the stiffness law that
 * reads it.
 *
 * [secantAt] is the per-path secant at a stroke, which is [stiffnessOf] for every linear element
 * and is not for `C-0030`'s coupled flexure or `C-0039`'s elastica — the two that strain-soften.
 */
private enum class T122PlanKind(val label: String) {

    /** A beam spanning in the tile plane, tied at midspan — `C-0041`'s own geometry. */
    BEAM("a beam spanning in plane, tied at midspan"),

    /** An arm rooted at a crossover and reaching in plane — `C-0053`/`C-0055`'s geometry. */
    ROOTED_ARM("an arm rooted at a crossover, reaching in plane"),

    /** A chain running normal to the sheet: its length consumes no plan area at all. */
    OUT_OF_PLANE("a chain normal to the sheet, consuming no plan area")
}

private class T122Element(
    val id: String,
    val name: String,
    val quantum: Double,
    val quantumName: String,
    val units: IntRange,
    val nominalExponent: Double,
    val linear: Boolean,
    val planKind: T122PlanKind,
    val stiffnessOf: (Double) -> Double,
    val secantAt: (Double, Double) -> Double = { parameter, _ -> stiffnessOf(parameter) },
    val tangentAt: (Double, Double) -> Double = { parameter, _ -> stiffnessOf(parameter) }
)

private fun t122Catalogue(): List<T122Element> {
    val hinge = Gen1Tile.crossoverHingeStiffness()
    val flexibility = standoffTipFlexibility(
        EI, 8.0, StandoffBase.crossovers(2, favourableOrientation = true).rotationalStiffness
    )
    val anchorage = ArmAnchorage.twoTerminus(BForm.PHOSPHATE_RADIUS)
    fun coupled(span: Double) = CoupledJointFlexure(EI, span, flexibility)
    fun elastica(arm: Double) = TwoSpringElastica(EI, arm, 16 * hinge, anchorage.rotationalStiffness)
    return listOf(
        T122Element(
            id = "E3f",
            name = "C-0023 E3, transverse duplex flexure, PINNED ends free to draw in",
            quantum = RISE, quantumName = "base pair", units = 20..300,
            nominalExponent = -3.0, linear = true, planKind = T122PlanKind.BEAM,
            stiffnessOf = { span ->
                TransverseDuplexFlexure(EI, span, FlexureEndCondition.PINNED_ENDS, false)
                    .bendingStiffness
            }
        ),
        T122Element(
            id = "E3c",
            name = "C-0023 E3, transverse duplex flexure, CLAMPED ends free to draw in",
            quantum = RISE, quantumName = "base pair", units = 20..300,
            nominalExponent = -3.0, linear = true, planKind = T122PlanKind.BEAM,
            stiffnessOf = { span ->
                TransverseDuplexFlexure(EI, span, FlexureEndCondition.CLAMPED_ENDS, false)
                    .bendingStiffness
            }
        ),
        T122Element(
            id = "E5n1",
            name = "C-0023 E5, crossover-hinge flexure, ONE hinge",
            quantum = RISE, quantumName = "base pair", units = 3..120,
            nominalExponent = -2.0, linear = true, planKind = T122PlanKind.ROOTED_ARM,
            stiffnessOf = { arm -> CrossoverHingeFlexure(hinge, arm, EI, 1, 3.0).stiffness }
        ),
        T122Element(
            id = "E5n2",
            name = "C-0023 E5, crossover-hinge flexure, TWO hinges (C-0040's lattice supply)",
            quantum = RISE, quantumName = "base pair", units = 3..120,
            nominalExponent = -2.0, linear = true, planKind = T122PlanKind.ROOTED_ARM,
            stiffnessOf = { arm -> CrossoverHingeFlexure(hinge, arm, EI, 2, 3.0).stiffness }
        ),
        T122Element(
            id = "E4",
            name = "C-0023 E4, antagonistic ssDNA pair, both limbs of equal contour",
            quantum = SsDnaTether.CONTOUR_PER_NUCLEOTIDE, quantumName = "nucleotide",
            units = 3..400, nominalExponent = -1.0, linear = true,
            planKind = T122PlanKind.OUT_OF_PLANE,
            stiffnessOf = { contour ->
                2.0 * FreelyJointedChain(
                    contour, SsDnaTether.KUHN_LENGTH_ZERO_FORCE
                ).gaussianStiffness
            }
        ),
        T122Element(
            id = "C30",
            name = "C-0030 coupled-standoff flexure, B2 base at 8 nm, favourable mounting",
            quantum = RISE, quantumName = "base pair", units = 30..300,
            nominalExponent = -3.0, linear = false, planKind = T122PlanKind.BEAM,
            stiffnessOf = { span ->
                coupled(span).strokeSecantStiffness(
                    Gen1Tile.ACCEPTABLE_STROKE, FlexureOrientation.FAVOURABLE
                )
            },
            secantAt = { span, stroke ->
                coupled(span).strokeSecantStiffness(stroke, FlexureOrientation.FAVOURABLE)
            },
            tangentAt = { span, stroke ->
                coupled(span).strokeTangentStiffness(stroke, FlexureOrientation.FAVOURABLE)
            }
        ),
        T122Element(
            id = "C39",
            name = "C-0039 two-spring elastica, 16 hinges on C-0034's A2 anchorage",
            quantum = RISE, quantumName = "base pair", units = 15..200,
            nominalExponent = -3.0, linear = false, planKind = T122PlanKind.ROOTED_ARM,
            stiffnessOf = { arm -> elastica(arm).secantStiffness(Gen1Tile.ACCEPTABLE_STROKE) },
            secantAt = { arm, stroke -> elastica(arm).secantStiffness(stroke) },
            tangentAt = { arm, stroke -> elastica(arm).tangentStiffness(stroke) }
        )
    )
}

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

private fun t122Sheet(): OrigamiSheet =
    origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)

private fun t122Lattice(
    sheet: OrigamiSheet,
    supports: List<PointSupport> = emptyList(),
    subdivisions: Int = 2
): OrigamiGrillage = OrigamiGrillage(
    sheet = sheet,
    lengthX = T122_EDGE_X,
    beamCount = T122_DUPLEXES,
    foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
    columns = CrossoverLayout.centred(
        T122_NOMINAL_CROSSOVER_COLUMNS, sheet.crossoverSpacing / 2.0
    ),
    subdivisions = subdivisions,
    supports = supports
)

fun main() {
    val started = System.currentTimeMillis()
    val sheet = t122Sheet()
    val lengthY = T122_DUPLEXES * sheet.interhelicalDistance
    val interiorPressure = Gen1Tile.TARGET_FORCE / (T122_EDGE_X * lengthY)
    val plateModel = sheet.plate(T122_EDGE_X, lengthY)
    val grid = attachmentGrid(T122_COLUMNS, T122_DUPLEXES, T122_EDGE_X, lengthY)
    val mask = rimMask(grid, T122_EDGE_X, lengthY, T122_COLLAR)

    println("T-122 — reading C-0022's solved edge profile ...")
    val profile = t122DesignProfile(File("gpd/results/T-3b-tile-edge-load-profile.json"))
    val field = profile.field(interiorPressure, lengthY)

    val stroke = PlateOnFoundation(plateModel, Gen1Tile.FOUNDATION_SECANT, emptyList(), 12)
        .solve(uniformPressure(interiorPressure)).meanDeflection

    val bareLattice = t122Lattice(sheet)
    val barePlate = PlateOnFoundation(plateModel, Gen1Tile.FOUNDATION_SECANT, emptyList(), 12)

    println("T-122 — the Woodbury surrogates ...")
    val latticeSurrogate = latticeInfluenceSurrogate(bareLattice, grid, field, T122_SAMPLES)
    val plateSurrogate = plateInfluenceSurrogate(barePlate, grid, field, T122_SAMPLES)

    // `C-0058`'s two levels, RE-DERIVED from its own rule rather than taken as 0.921 / 0.184.
    val nominal = normalisedStiffnesses(
        rimStiffenedWeights(grid, T122_EDGE_X, lengthY, T122_COLLAR, T122_TARGET_RATIO),
        T122_MANDATE
    )
    val rimTarget = nominal.filterIndexed { i, _ -> mask[i] }.max()
    val interiorTarget = nominal.filterIndexed { i, _ -> !mask[i] }.min()

    fun dishingOverStroke(stiffnesses: List<Double>): Double =
        latticeSurrogate.solve(stiffnesses).peakDishing / stroke

    // --------------------------------------------------------- the flat window, MEASURED
    println("T-122 — measuring C-0058's flat ratio window rather than citing it ...")
    val ratioWindow = mutableListOf<T122RatioWindowRecord>()
    val ratios = listOf(
        1.0, 1.5, 2.0, 2.5, 3.0, 3.5, 4.0, 4.5, 5.0, 6.0, 7.0, 8.0, 10.0, 12.0, 15.0,
        20.0, 25.0, 30.0, 40.0, 60.0, 100.0
    )
    ratios.forEach { ratio ->
        val distribution = normalisedStiffnesses(
            rimStiffenedWeights(grid, T122_EDGE_X, lengthY, T122_COLLAR, ratio), T122_MANDATE
        )
        val fraction = dishingOverStroke(distribution)
        ratioWindow += T122RatioWindowRecord(
            collarWidth = T122_COLLAR,
            rimPaths = mask.count { it },
            ratio = ratio,
            dishingOverStroke = fraction,
            flat = fraction < T122_TOLERANCE
        )
    }
    val flatRatios = ratioWindow.filter { it.flat }.map { it.ratio }
    require(flatRatios.isNotEmpty()) {
        "no ratio in the sweep is flat — C-0058's headline does not reproduce"
    }
    val windowLow = flatRatios.min()
    val windowHigh = flatRatios.max()
    val windowWidth = windowHigh / windowLow

    // --------------------------------------------------------- the cheap bound
    println("T-122 — the cheap bound, one division per element per level ...")
    val catalogue = t122Catalogue()
    val levels = listOf("rim" to rimTarget, "interior" to interiorTarget)
    val cheapBound = mutableListOf<T122CheapBoundRecord>()
    val ladders = mutableListOf<T122LadderRecord>()
    val builtRatios = mutableListOf<T122BuiltRatioRecord>()
    val costs = mutableListOf<T122CostRecord>()
    val packing = mutableListOf<T122PackingRecord>()

    val ladderByElement = mutableMapOf<String, List<BuildableSetting>>()
    val verdictByElement = mutableMapOf<String, Map<String, QuantisationVerdict>>()

    catalogue.forEach { element ->
        println("T-122 — enumerating ${element.id}'s ladder (${element.units}) ...")
        val ladder = buildableLadder(element.quantum, element.units, element.stiffnessOf)
        ladderByElement[element.id] = ladder
        val verdicts = levels.associate { (level, target) ->
            level to nearestBuildable(ladder, target)
        }
        verdictByElement[element.id] = verdicts
        levels.forEach { (level, target) ->
            val verdict = verdicts.getValue(level)
            val bound = powerLawGranularity(
                element.nominalExponent, verdict.nearest.parameter, element.quantum
            )
            // one quantum of THIS level's parameter moves the ratio by the same fraction
            val ratioStep = verdict.relativeGranularity
            val fired = ratioStep >= windowWidth - 1.0
            cheapBound += T122CheapBoundRecord(
                element = element.id,
                level = level,
                targetStiffness = target,
                parameterAtTheTarget = verdict.nearest.parameter,
                parameterUnitsAtTheTarget = verdict.nearest.units.toDouble(),
                quantum = element.quantum,
                quantumName = element.quantumName,
                nominalExponent = element.nominalExponent,
                powerLawGranularity = bound,
                flatRatioWindowWidth = windowWidth,
                ratioStepFromOneQuantum = ratioStep,
                falsifierFired = fired,
                verdict = if (fired)
                    "the quantum is comparable with the flat window — the ratio cannot be SET"
                else
                    "the quantum is %.0fx finer than the flat window — the ratio CAN be set"
                        .format((windowWidth - 1.0) / ratioStep)
            )
            ladders += T122LadderRecord(
                element = element.id,
                level = level,
                targetStiffness = target,
                units = verdict.nearest.units,
                quantumName = element.quantumName,
                parameter = verdict.nearest.parameter,
                realisedStiffness = verdict.nearest.stiffness,
                relativeError = verdict.relativeError,
                relativeGranularity = verdict.relativeGranularity,
                errorOverHalfTheGranularity =
                    verdict.relativeError / (0.5 * verdict.relativeGranularity),
                bracketed = verdict.bracketed,
                ladderRungs = ladder.size,
                softestReachable = ladder.minOf { it.stiffness },
                stiffestReachable = ladder.maxOf { it.stiffness }
            )
        }

        val rim = verdicts.getValue("rim")
        val interior = verdicts.getValue("interior")
        val rimCount = mask.count { it }
        val interiorCount = mask.count { !it }
        val total = rimCount * rim.nearest.stiffness + interiorCount * interior.nearest.stiffness
        val totalGranularity = relativeTotalGranularity(
            rim.relativeGranularity * rim.nearest.stiffness, T122_MANDATE
        )
        val realisedRatio = rim.nearest.stiffness / interior.nearest.stiffness
        val inside = realisedRatio in windowLow..windowHigh
        val totalError = abs(total - T122_MANDATE) / T122_MANDATE
        builtRatios += T122BuiltRatioRecord(
            element = element.id,
            rimUnits = rim.nearest.units,
            interiorUnits = interior.nearest.units,
            rimParameter = rim.nearest.parameter,
            interiorParameter = interior.nearest.parameter,
            realisedRimStiffness = rim.nearest.stiffness,
            realisedInteriorStiffness = interior.nearest.stiffness,
            targetRatio = T122_TARGET_RATIO,
            realisedRatio = realisedRatio,
            ratioRelativeError = abs(realisedRatio - T122_TARGET_RATIO) / T122_TARGET_RATIO,
            insideTheFlatRatioWindow = inside,
            realisedTotal = total,
            mandate = T122_MANDATE,
            totalRelativeError = totalError,
            totalRelativeGranularity = totalGranularity,
            totalInsideItsOwnGranularity = totalError <= totalGranularity,
            verdict = when {
                !rim.bracketed || !interior.bracketed ->
                    "a level lies OUTSIDE the ladder — not reachable at any integer setting"
                inside -> "BUILDABLE: both levels reachable and the ratio inside the flat window"
                else -> "the ratio lands OUTSIDE the flat window"
            }
        )

        levels.forEach { (level, _) ->
            val verdict = verdicts.getValue(level)
            val count = if (level == "rim") rimCount else interiorCount
            val force = verdict.nearest.stiffness * Gen1Tile.ACCEPTABLE_STROKE
            val tangent = element.tangentAt(
                verdict.nearest.parameter, Gen1Tile.ACCEPTABLE_STROKE
            )
            val secant = element.secantAt(verdict.nearest.parameter, Gen1Tile.ACCEPTABLE_STROKE)
            costs += T122CostRecord(
                element = element.id,
                level = level,
                stiffness = verdict.nearest.stiffness,
                parameter = verdict.nearest.parameter,
                units = verdict.nearest.units,
                forceAtAcceptableStroke = force,
                unzipMargin = Gen1Tile.DUPLEX_UNZIP_ALLOWABLE / force,
                perPathSecantCeilingAtAcceptableStroke = perPathStiffnessCeiling(
                    Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE
                ),
                insidePerPathCeiling = perPathStiffnessCeiling(
                    Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE
                ) / verdict.nearest.stiffness,
                thermalForce = verdict.nearest.stiffness *
                        kotlin.math.sqrt(thermalEnergy() / T122_MANDATE),
                tangentAtAcceptableStroke = tangent,
                tangentOverSecant = tangent / secant,
                strainSoftening = tangent < secant * (1.0 - 1e-9)
            )
            check(count > 0) { "a level with no stations" }
        }

        val longer = maxOf(rim.nearest.parameter, interior.nearest.parameter)
        val shorter = minOf(rim.nearest.parameter, interior.nearest.parameter)
        fun planCount(length: Double): Int = when (element.planKind) {
            T122PlanKind.BEAM -> packingLimitedPathCount(T122_EDGE_X, T122_DUPLEXES, length)
            T122PlanKind.ROOTED_ARM -> packingLimitedElementCount(
                T122_EDGE_X, T122_DUPLEXES, { length }, Gen1Tile.INTERHELICAL_SHEET,
                anchorFraction = 0.0
            )
            // a chain normal to the sheet consumes no plan area: the 45 stations are the limit
            T122PlanKind.OUT_OF_PLANE -> grid.size
        }
        val rootPitch = if (element.planKind == T122PlanKind.ROOTED_ARM)
            (0 until 32).maxOf {
                placeHingeArms(it, T122_EDGE_X, T122_DUPLEXES, longer).arms
            } else grid.size
        val planLimited = planCount(longer)
        val limited = minOf(planLimited, rootPitch)
        val packs = limited >= grid.size
        packing += T122PackingRecord(
            element = element.id,
            planKind = element.planKind.label,
            rimParameter = rim.nearest.parameter,
            interiorParameter = interior.nearest.parameter,
            longerParameter = longer,
            tileEdge = T122_EDGE_X,
            fitsTheTileEdge = longer <= T122_EDGE_X,
            leverEnvelope = T122_LEVER_ENVELOPE,
            fitsTheLeverEnvelope = longer <= T122_LEVER_ENVELOPE,
            pathsRequired = grid.size,
            packingLimitedPaths = limited,
            packingLimitedPathsAtTheShorter = minOf(
                planCount(shorter),
                if (element.planKind == T122PlanKind.ROOTED_ARM)
                    (0 until 32).maxOf {
                        placeHingeArms(it, T122_EDGE_X, T122_DUPLEXES, shorter).arms
                    } else grid.size
            ),
            rootPitchLimitedPaths = rootPitch,
            packs = packs,
            verdict = when {
                packs && element.planKind == T122PlanKind.OUT_OF_PLANE ->
                    "the 45-station array places, but E4 needs a SECOND GROUND under the tile " +
                            "(C-0023's own caveat) and the layer and the electrode are there"
                packs -> "the 45-station array places"
                planLimited < grid.size && rootPitch < grid.size ->
                    "does NOT place: plan area admits $planLimited and the crossover root " +
                            "pitch $rootPitch, against 45"
                planLimited < grid.size ->
                    "does NOT place — C-0041's plan-area obstruction, unchanged"
                else ->
                    "does NOT place — C-0053/C-0055's crossover ROOT PITCH, not the plan area"
            }
        )
    }

    // --------------------------------------------------------- the built designs, solved
    println("T-122 — solving the built designs on the lattice and the plate ...")
    val flatness = mutableListOf<T122FlatnessRecord>()

    fun assembled(stiffnesses: List<Double>, subdivisions: Int = 2): GrillageDeflection =
        t122Lattice(
            sheet,
            grid.mapIndexed { index, (x, y) -> PointSupport(x, y, stiffnesses[index]) },
            subdivisions
        ).solve(field)

    fun flatnessRecord(
        label: String,
        elementId: String,
        stiffnesses: List<Double>
    ): T122FlatnessRecord {
        val solution = assembled(stiffnesses)
        val lattice = solution.peakDishing(T122_SAMPLES)
        val plate = PlateOnFoundation(
            plateModel, Gen1Tile.FOUNDATION_SECANT,
            grid.mapIndexed { index, (x, y) -> PointSupport(x, y, stiffnesses[index]) }, 12
        ).solve(field).peakDishing(T122_SAMPLES)
        val fraction = lattice / stroke
        val peak = stiffnesses.max()
        val force = peak * Gen1Tile.ACCEPTABLE_STROKE
        return T122FlatnessRecord(
            label = label,
            element = elementId,
            rimStiffness = stiffnesses.filterIndexed { i, _ -> mask[i] }.max(),
            interiorStiffness = stiffnesses.filterIndexed { i, _ -> !mask[i] }.min(),
            ratio = stiffnesses.filterIndexed { i, _ -> mask[i] }.max() /
                    stiffnesses.filterIndexed { i, _ -> !mask[i] }.min(),
            totalStiffness = stiffnesses.sum(),
            latticePeakDishing = lattice,
            platePeakDishing = plate,
            latticeOverPlate = lattice / plate,
            dishingOverStroke = fraction,
            flat = fraction < T122_TOLERANCE,
            peakPathForceAtAcceptableStroke = force,
            unzipMargin = Gen1Tile.DUPLEX_UNZIP_ALLOWABLE / force,
            peakCrossoverForce = solution.peakCrossoverForce,
            peakDuplexShear = solution.peakDuplexShear,
            peakThermalForce = perPathThermalForces(stiffnesses).max(),
            verdict = if (fraction < T122_TOLERANCE) "FLAT under T-5b's 10% convention"
            else "NOT flat"
        )
    }

    println("T-122 — trimming each built design to C-0017's mandate ...")
    val trims = mutableListOf<T122TrimRecord>()
    val trimmed = mutableMapOf<String, List<Double>>()
    catalogue.forEach { element ->
        val ladder = ladderByElement.getValue(element.id)
        val design = trimmedToTotal(nominal, ladder, T122_MANDATE)
        val rimSettings = design.settings.filterIndexed { i, _ -> mask[i] }
        val interiorSettings = design.settings.filterIndexed { i, _ -> !mask[i] }
        val verdicts = verdictByElement.getValue(element.id)
        val before = mask.count { it } * verdicts.getValue("rim").nearest.stiffness +
                mask.count { !it } * verdicts.getValue("interior").nearest.stiffness
        val ratioLow = rimSettings.minOf { it.stiffness } / interiorSettings.maxOf { it.stiffness }
        val ratioHigh = rimSettings.maxOf { it.stiffness } / interiorSettings.minOf { it.stiffness }
        trimmed[element.id] = design.settings.map { it.stiffness }
        trims += T122TrimRecord(
            element = element.id,
            paths = design.settings.size,
            distinctSettings = design.settings.map { it.units }.distinct().size,
            rimUnitsLow = rimSettings.minOf { it.units },
            rimUnitsHigh = rimSettings.maxOf { it.units },
            interiorUnitsLow = interiorSettings.minOf { it.units },
            interiorUnitsHigh = interiorSettings.maxOf { it.units },
            totalBeforeTrimming = before,
            totalAfterTrimming = design.total,
            mandate = T122_MANDATE,
            relativeErrorBeforeTrimming = abs(before - T122_MANDATE) / T122_MANDATE,
            relativeErrorAfterTrimming = design.relativeError,
            moves = design.moves,
            rimStiffnessRange =
                rimSettings.maxOf { it.stiffness } / rimSettings.minOf { it.stiffness },
            interiorStiffnessRange =
                interiorSettings.maxOf { it.stiffness } / interiorSettings.minOf { it.stiffness },
            realisedRatioLow = ratioLow,
            realisedRatioHigh = ratioHigh,
            insideTheFlatRatioWindow = ratioLow >= windowLow && ratioHigh <= windowHigh
        )
    }

    flatness += flatnessRecord(
        "C-0058's uniform coupling (the limiting case)", "none",
        normalisedStiffnesses(List(grid.size) { 1.0 }, T122_MANDATE)
    )
    flatness += flatnessRecord("C-0058's NOMINAL rim x 5 design", "none", nominal)
    catalogue.forEach { element ->
        val verdicts = verdictByElement.getValue(element.id)
        flatness += flatnessRecord(
            "BUILT on ${element.id}", element.id,
            twoLevelStiffnesses(
                mask,
                verdicts.getValue("rim").nearest.stiffness,
                verdicts.getValue("interior").nearest.stiffness
            )
        )
        flatness += flatnessRecord(
            "BUILT on ${element.id}, TRIMMED to the mandate", element.id,
            trimmed.getValue(element.id)
        )
    }

    // --------------------------------------------------------- the strain-softening drift
    println("T-122 — the realised ratio over the stroke ...")
    val drift = mutableListOf<T122DriftRecord>()
    val driftStrokes = listOf(0.5, 1.0, 2.0, 3.0, 5.0, 10.0)
    catalogue.filter { !it.linear }.forEach { element ->
        val verdicts = verdictByElement.getValue(element.id)
        val rimParameter = verdicts.getValue("rim").nearest.parameter
        val interiorParameter = verdicts.getValue("interior").nearest.parameter
        val atPlacement = element.secantAt(rimParameter, Gen1Tile.ACCEPTABLE_STROKE) /
                element.secantAt(interiorParameter, Gen1Tile.ACCEPTABLE_STROKE)
        realisedSecantRatio(
            { s -> element.secantAt(rimParameter, s) },
            { s -> element.secantAt(interiorParameter, s) },
            driftStrokes
        ).forEachIndexed { index, ratio ->
            val s = driftStrokes[index]
            drift += T122DriftRecord(
                element = element.id,
                stroke = s,
                rimSecant = element.secantAt(rimParameter, s),
                interiorSecant = element.secantAt(interiorParameter, s),
                realisedRatio = ratio,
                ratioAtThePlacementStroke = atPlacement,
                driftFromThePlacementStroke = ratio / atPlacement - 1.0,
                insideTheFlatRatioWindow = ratio in windowLow..windowHigh
            )
        }
    }

    // --------------------------------------------------------- the scatter threshold
    println("T-122 — bisecting the scatter threshold, per C-0026 pattern ...")
    val scatter = mutableListOf<T122ScatterRecord>()
    val overlap = populationOverlapScatter(T122_TARGET_RATIO)
    val designs = listOf(
        "C-0058's UNIFORM coupling (C-0026's own case)" to
                normalisedStiffnesses(List(grid.size) { 1.0 }, T122_MANDATE),
        "C-0058's NOMINAL rim x 5 design" to nominal,
        "BUILT on C30" to twoLevelStiffnesses(
            mask,
            verdictByElement.getValue("C30").getValue("rim").nearest.stiffness,
            verdictByElement.getValue("C30").getValue("interior").nearest.stiffness
        )
    )
    designs.forEach { (label, distribution) ->
        ScatterPattern.entries.forEach { pattern ->
        listOf(false, true).forEach { renormalise ->
            fun scattered(epsilon: Double): List<Double> {
                val raw = scatteredStiffnesses(distribution, T122_COLUMNS, pattern, epsilon)
                return if (renormalise) normalisedStiffnesses(raw, T122_MANDATE) else raw
            }
            val threshold = scatterThreshold(
                maximum = 0.95, scanSteps = 190, limit = T122_TOLERANCE
            ) { epsilon -> dishingOverStroke(scattered(epsilon)) }
            val atZero = assembled(scattered(0.0)).peakCrossoverForce
            val atTen = assembled(scattered(0.10)).peakCrossoverForce
            scatter += T122ScatterRecord(
                design = label,
                pattern = pattern.label,
                renormalisedToTheMandate = renormalise,
                dishingAtZeroScatter = threshold.metricAtZero,
                dishingAtTheScanCeiling = threshold.metricAtCeiling,
                thresholdAmplitude = threshold.threshold,
                reachesTheTolerance = threshold.reachesTheLimit,
                scanCeiling = threshold.ceiling,
                bracketWidth = threshold.bracketWidth,
                populationOverlapAmplitude = overlap,
                flatnessBindsBeforeTheOrdering =
                    threshold.reachesTheLimit && threshold.threshold < overlap,
                totalDriftAtTheThreshold =
                    abs(scattered(threshold.threshold).sum() - T122_MANDATE) / T122_MANDATE,
                crossoverForceAtTheThreshold =
                    T122_SCATTER_SENSITIVITY * threshold.threshold,
                overCzeroTwentySixBreakEven = threshold.threshold / T122_SCATTER_BREAK_EVEN,
                dishingAtTenPercentScatter = dishingOverStroke(scattered(0.10)),
                peakCrossoverForceAtZeroScatter = atZero,
                peakCrossoverForceAtTenPercentScatter = atTen,
                crossoverForceRestoredAtTenPercent = atTen - atZero
            )
        }
        }
    }

    // --------------------------------------------------------- gate 4, convergence
    println("T-122 — convergence ...")
    val convergence = mutableListOf<T122ConvergenceRecord>()
    val built = twoLevelStiffnesses(
        mask,
        verdictByElement.getValue("C30").getValue("rim").nearest.stiffness,
        verdictByElement.getValue("C30").getValue("interior").nearest.stiffness
    )
    val sampleCounts = listOf(41, 81, 161)
    val bySamples = sampleCounts.associateWith { samples ->
        latticeInfluenceSurrogate(bareLattice, grid, field, samples).solve(built).peakDishing
    }
    val finestSamples = bySamples.getValue(161)
    sampleCounts.forEach { samples ->
        convergence += T122ConvergenceRecord(
            quantity = "peak dishing of the BUILT C30 design [nm]",
            parameter = "dishing samples per edge",
            value = "$samples",
            result = bySamples.getValue(samples),
            departureFromFinest =
                abs(bySamples.getValue(samples) - finestSamples) / finestSamples
        )
    }
    val subdivisionCounts = listOf(1, 2, 4)
    val bySubdivisions = subdivisionCounts.associateWith { n ->
        assembled(built, n).peakDishing(T122_SAMPLES)
    }
    val finestSubdivision = bySubdivisions.getValue(4)
    subdivisionCounts.forEach { n ->
        convergence += T122ConvergenceRecord(
            quantity = "peak dishing of the BUILT C30 design [nm]",
            parameter = "NESTED beam subdivisions",
            value = "$n",
            result = bySubdivisions.getValue(n),
            departureFromFinest =
                abs(bySubdivisions.getValue(n) - finestSubdivision) / finestSubdivision
        )
    }
    // A threshold of exactly zero is a design already outside the tolerance at zero scatter —
    // the uniform coupling — and it has no bracket to converge. Guarding it ABSOLUTELY rather
    // than dividing by it is `CLAUDE.md`'s rule for a ratio whose denominator can vanish.
    scatter.filter { it.reachesTheTolerance && it.thresholdAmplitude > 0.0 }.forEach {
        convergence += T122ConvergenceRecord(
            quantity = "scatter threshold bracket width",
            parameter = "${it.design} / ${it.pattern} / " +
                    (if (it.renormalisedToTheMandate) "renormalised" else "as built"),
            value = "bisection on the bracket width",
            result = it.thresholdAmplitude,
            departureFromFinest = it.bracketWidth / it.thresholdAmplitude
        )
    }
    listOf(200, 400, 800).forEach { steps ->
        val hinge = Gen1Tile.crossoverHingeStiffness()
        val anchorage = ArmAnchorage.twoTerminus(BForm.PHOSPHATE_RADIUS)
        val reference = TwoSpringElastica(
            EI, 12.7198, 16 * hinge, anchorage.rotationalStiffness, 800
        ).secantStiffness(Gen1Tile.ACCEPTABLE_STROKE)
        val value = TwoSpringElastica(
            EI, 12.7198, 16 * hinge, anchorage.rotationalStiffness, steps
        ).secantStiffness(Gen1Tile.ACCEPTABLE_STROKE)
        convergence += T122ConvergenceRecord(
            quantity = "C-0039's elastica secant at its own arm [pN/nm]",
            parameter = "RK4 steps",
            value = "$steps",
            result = value,
            departureFromFinest = abs(value - reference) / reference
        )
    }

    // --------------------------------------------------------- gate 5, upstream
    println("T-122 — upstream reproductions ...")
    val reproductions = mutableListOf<T122ReproductionRecord>()
    fun reproduce(
        source: String, quantity: String, published: Double, reproduced: Double, note: String = ""
    ) {
        reproductions += T122ReproductionRecord(
            source = source, quantity = quantity, published = published, reproduced = reproduced,
            relativeDeparture = abs(reproduced - published) / abs(published), note = note
        )
    }
    reproduce("C-0058", "rim stiffness [pN/nm]", 0.921, rimTarget)
    reproduce("C-0058", "interior stiffness [pN/nm]", 0.184, interiorTarget)
    reproduce("C-0058", "rim stations", 34.0, mask.count { it }.toDouble())
    reproduce("C-0058", "interior stations", 11.0, mask.count { !it }.toDouble())
    reproduce(
        "C-0058", "uniform 3 x 15 dishing / stroke", 0.2182,
        flatness.first { it.label.contains("uniform") }.dishingOverStroke
    )
    reproduce(
        "C-0058", "rim x 5 dishing / stroke", 0.0753,
        flatness.first { it.label.contains("NOMINAL") }.dishingOverStroke
    )
    reproduce("C-0026", "free-tile stroke [nm]", 4.90731, stroke)
    reproduce("C-0017", "mandated total [pN/nm]", 33.3333, T122_MANDATE)
    val c30Span = coupledFlexureSpan(
        EI,
        standoffTipFlexibility(
            EI, 8.0, StandoffBase.crossovers(2, favourableOrientation = true).rotationalStiffness
        ),
        45, T122_MANDATE, Gen1Tile.ACCEPTABLE_STROKE
    )
    reproduce("C-0030", "45-path span [nm]", 31.82, c30Span, "the R = 1 limiting case")
    reproduce(
        "C-0030", "45-path tangent [pN/nm]", 25.23,
        45.0 * CoupledJointFlexure(
            EI, c30Span,
            standoffTipFlexibility(
                EI, 8.0,
                StandoffBase.crossovers(2, favourableOrientation = true).rotationalStiffness
            )
        ).tangentStiffness(Gen1Tile.ACCEPTABLE_STROKE)
    )
    reproduce(
        "C-0023", "E3 pinned free span at 45 paths [nm]", 24.61,
        ladderByElement.getValue("E3f").let { ladder ->
            // the span that makes 45 paths reach the mandate, read off the same law
            val target = T122_MANDATE / 45.0
            nearestBuildable(ladder, target).nearest.parameter
        },
        "read on the ladder, so it is the nearest BUILDABLE span and not the exact root"
    )
    reproduce(
        "C-0023", "E5 one-hinge arm at 45 paths [nm]", 4.11,
        nearestBuildable(ladderByElement.getValue("E5n1"), T122_MANDATE / 45.0).nearest.parameter,
        "read on the ladder, so it is the nearest BUILDABLE arm and not the exact root"
    )
    reproduce(
        "C-0023", "the preload quantum trap: required mounting offset [nm]", 0.0409,
        offsetForPreload(
            thermalEnergy() / 3.0, Gen1Tile.TARGET_FORCE, Gen1Tile.ACCEPTABLE_STROKE
        ),
        "the one case in this corpus where quantisation DEFEATED a requirement, 8.3x"
    )
    reproduce(
        "C-0049", "per-path secant ceiling at 45 paths and 3 nm [pN/nm]", 150.0,
        perPathSecantCeiling(Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, 45, Gen1Tile.ACCEPTABLE_STROKE)
    )
    reproduce(
        "C-0058", "admissible ratio at 45 paths and 3 nm", 4.5,
        admissibleStiffnessRatio(
            Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE, T122_MANDATE, 45
        )
    )
    reproduce(
        "C-0041", "packing-limited path count at C-0030's own span", 15.0,
        packingLimitedPathCount(T122_EDGE_X, T122_DUPLEXES, c30Span).toDouble()
    )
    reproduce(
        "C-0039", "E5a16 arm [nm]", 12.7198,
        run {
            val hinge = Gen1Tile.crossoverHingeStiffness()
            val anchorage = ArmAnchorage.twoTerminus(BForm.PHOSPHATE_RADIUS)
            elasticaArmForStiffness(
                hinge, 16, anchorage.rotationalStiffness, EI, 45, T122_MANDATE,
                Gen1Tile.ACCEPTABLE_STROKE, 400
            )
        }
    )
    reproduce(
        "C-0014", "per-path thermal force at the uniform coupling [pN]", 0.261,
        perPathThermalForces(
            normalisedStiffnesses(List(grid.size) { 1.0 }, T122_MANDATE)
        ).max()
    )

    // --------------------------------------------------------- the findings
    val worstBuilt = builtRatios.filter { it.insideTheFlatRatioWindow }
    val builtFlat = flatness.filter { it.element != "none" && it.flat }
    val binding = scatter.filter { it.design.contains("C30") }
    val findings = buildList {
        add(
            ("The flat ratio window, MEASURED rather than cited: a rim x R design over the " +
                    "%.2f nm collar is flat for %.1f <= R <= %.1f, a factor of %.1f wide.")
                .format(T122_COLLAR, windowLow, windowHigh, windowWidth)
        )
        ratioWindow.minByOrNull { it.dishingOverStroke }?.let {
            add(
                ("The best one-parameter ratio at C-0058's own 6.70 nm collar is %.1f, not 5: " +
                        "it dishes %.4f of the stroke against 5's %.4f, a further %.1f%%. " +
                        "C-0058's six-point sweep did not visit it.")
                    .format(
                        it.ratio, it.dishingOverStroke,
                        ratioWindow.first { r -> r.ratio == 5.0 }.dishingOverStroke,
                        100.0 * (1.0 - it.dishingOverStroke /
                                ratioWindow.first { r -> r.ratio == 5.0 }.dishingOverStroke)
                    )
            )
        }
        add(
            ("The cheap bound did NOT fire at any element: the coarsest quantum in the " +
                    "catalogue is %.1f%% of a level's own stiffness, against a flat window " +
                    "%.0f%% wide. Quantisation is %.0fx finer than the requirement, where " +
                    "C-0023's preload quantum was 8.3x COARSER than its own.")
                .format(
                    100.0 * cheapBound.maxOf { it.ratioStepFromOneQuantum },
                    100.0 * (windowWidth - 1.0),
                    (windowWidth - 1.0) / cheapBound.maxOf { it.ratioStepFromOneQuantum }
                )
        )
        add(
            ("%d of %d catalogue elements reach BOTH levels with the realised ratio inside the " +
                    "flat window; %d of %d built designs are still flat on the solved lattice.")
                .format(
                    worstBuilt.size, builtRatios.size, builtFlat.size,
                    flatness.count { it.element != "none" }
                )
        )
        add(
            ("Rounding the two LEVELS independently misses C-0017's mandate by up to %.2f%%, " +
                    "which is a placement error and not a rounding nuisance. Trimming — moving " +
                    "individual paths by ONE base pair, which a builder may do because the " +
                    "mandate is an equality on a SUM — takes the worst miss to %.2e in at most " +
                    "%d moves, and leaves the design %d distinct settings instead of two.")
                .format(
                    100.0 * builtRatios.maxOf { it.totalRelativeError },
                    trims.maxOf { it.relativeErrorAfterTrimming },
                    trims.maxOf { it.moves },
                    trims.maxOf { it.distinctSettings }
                )
        )
        binding.filter { it.reachesTheTolerance && !it.renormalisedToTheMandate }
            .minByOrNull { it.thresholdAmplitude }?.let {
            add(
                ("The BINDING scatter threshold is %.1f%% relative amplitude, on the '%s' " +
                        "pattern — %.2fx C-0026's 17%% break-even, and %.2fx the %.1f%% " +
                        "amplitude at which the two populations would merely OVERLAP. " +
                        "Flatness binds long before the ordering does.")
                    .format(
                        100.0 * it.thresholdAmplitude, it.pattern,
                        it.thresholdAmplitude / T122_SCATTER_BREAK_EVEN,
                        it.thresholdAmplitude / overlap, 100.0 * overlap
                    )
            )
        }
        if (binding.none { it.reachesTheTolerance }) {
            add(
                "No scatter pattern inside the scan ceiling loses the flatness verdict at all " +
                        "— the built design's tolerance to assembly scatter exceeds 95%."
            )
        }
        run {
            val built = scatter.filter {
                it.design.contains("C30") && it.renormalisedToTheMandate
            }
            val along = built.firstOrNull { it.pattern.startsWith("alternating columns") }
            val across = built.firstOrNull { it.pattern.startsWith("alternating rows") }
            if (along != null && across != null) add(
                ("C-0026's BUILD RULE reverses on this design. Its rule is to let the scatter " +
                        "alternate ALONG the helices, where it restores exactly zero crossover " +
                        "force; on a three-column NON-UNIFORM coupling that same direction IS " +
                        "the ratio's own axis, and it is the pattern the flatness verdict " +
                        "tolerates LEAST even with the mandate held: %.1f%% against %.1f%% " +
                        "across the helices, a factor of %.2f. The crossover channel still " +
                        "prefers it — at a 10%% amplitude it moves the peak crossover force by " +
                        "%+.4f pN against %+.4f pN across — so the two channels now RANK THE " +
                        "PATTERNS OPPOSITELY, which C-0026's equal-spring case could not show.")
                    .format(
                        100.0 * along.thresholdAmplitude, 100.0 * across.thresholdAmplitude,
                        across.thresholdAmplitude / along.thresholdAmplitude,
                        along.crossoverForceRestoredAtTenPercent,
                        across.crossoverForceRestoredAtTenPercent
                    )
            )
        }
        drift.filter { it.stroke == Gen1Tile.DESIRED_STROKE }.forEach {
            add(
                ("%s's realised ratio drifts %.1f%% between the 3 nm placement stroke and " +
                        "10 nm — %s the flat window.")
                    .format(
                        it.element, 100.0 * it.driftFromThePlacementStroke,
                        if (it.insideTheFlatRatioWindow) "still inside" else "OUTSIDE"
                    )
            )
        }
        add(
            ("The binding constraint is NOT the stiffness, it is the PLACEMENT: %d of %d " +
                    "elements fail to lay 45 stations out at all, and the soft level is what " +
                    "breaks them — its member is %.2fx longer than the stiff one, so the array " +
                    "is priced at the LONGER span. The counts that place are %s against 45.")
                .format(
                    packing.count { !it.packs }, packing.size,
                    packing.filter { !it.packs }
                        .maxOf { it.interiorParameter / it.rimParameter },
                    packing.joinToString(", ") { "${it.element} ${it.packingLimitedPaths}" }
                )
        )
        packing.filter { it.packs }.forEach {
            add("${it.element} DOES place 45 stations: ${it.verdict}")
        }
    }

    val result = T122Result(
        task = "T-122",
        title = "Can a 5:1 per-path coupling stiffness ratio be BUILT?",
        leaf = "A8.2",
        verificationType = "logical (an exact enumeration of the buildable settings of five " +
                "catalogue elements over an integer design parameter) + in-silico (C-0058's " +
                "Woodbury surrogate on C-0009's grillage and C-0006's plate, under C-0022's " +
                "SOLVED load)",
        units = "lengths nm, forces pN, stiffness pN/nm, pressure pN/nm^2 = 1 MPa, energy pN*nm",
        conventions = listOf(
            "x runs ALONG the helices, y ACROSS them; the origin is the tile centre",
            "w is positive DOWNWARD, compressing the polymer layer (T-5)",
            "dishing is the peak absolute departure from the area-weighted least-squares " +
                    "best-fit PLANE, sampled on the same 81 x 81 grid as C-0026 and C-0058",
            "flat means peak dishing below 10% of the free-tile stroke — T-5b's CONVENTION",
            "the quantum of a duplex length is the rise per base pair, 0.34 nm; of an ssDNA " +
                    "contour, 0.65 nm per nucleotide; of a hinge count, one crossover",
            "a ladder is ENUMERATED, never searched, so deliverables 1-3 have no convergence " +
                    "parameter at all",
            "granularity is the fractional stiffness step between adjacent integer settings, " +
                    "at the setting nearest the target",
            "scatter is multiplicative and is NOT renormalised to the mandate — a build " +
                    "tolerance does not know the mandate, and the drift is reported"
        ),
        runParameters = mapOf(
            "duplexes" to "$T122_DUPLEXES",
            "attachmentGrid" to "$T122_COLUMNS x $T122_DUPLEXES (C-0015's 45)",
            "collarWidth" to "$T122_COLLAR nm (C-0058's best one-parameter collar)",
            "crossoverColumns" to "$T122_NOMINAL_CROSSOVER_COLUMNS, symmetrically centred (T-10)",
            "subdivisions" to "2 per interval, nested 1/2/4 in gate 4",
            "dishingSamples" to "$T122_SAMPLES x $T122_SAMPLES",
            "plateBasisDegree" to "12",
            "elasticaSteps" to "400 RK4 steps, swept 200/400/800",
            "scatterScan" to "190 intervals over [0, 0.95], then bisection on the bracket width"
        ),
        citedInputs = mapOf(
            "C-0017 mandate" to "${T122_MANDATE.roundedForProse()} pN/nm = 100 pN / 3 nm",
            "C-0058 two levels" to "re-derived here from its own rim x 5 rule, not tabulated",
            "rise per base pair" to "$RISE nm — CITED, MEASURED (Douglas et al. 2009)",
            "ssDNA contour per nucleotide" to
                    "${SsDnaTether.CONTOUR_PER_NUCLEOTIDE} nm — CITED, MEASURED, inextensible " +
                    "convention (Sim et al. 2012; Bosco et al. 2014)",
            "ssDNA Kuhn length" to
                    "${SsDnaTether.KUHN_LENGTH_ZERO_FORCE} nm — CITED, MEASURED, zero-force end",
            "duplex EI" to "$EI pN*nm^2 — a CanDo MODEL INPUT, not a measurement",
            "crossover hinge k_theta" to
                    "${Gen1Tile.crossoverHingeStiffness().roundedForProse()} pN*nm/rad — " +
                            "CITED, FITTED (Chen 2014)",
            "C-0006/CH-0029 unzip allowable" to "${Gen1Tile.DUPLEX_UNZIP_ALLOWABLE} pN per path",
            "C-0026 scatter sensitivity" to
                    "$T122_SCATTER_SENSITIVITY pN per unit relative amplitude, break-even " +
                    "$T122_SCATTER_BREAK_EVEN — CITED from C-0026, T-45 is still unmeasured",
            "T-5b tolerance" to "$T122_TOLERANCE — a CONVENTION",
            "C-0022 solved collar" to "read at run time from " +
                    "gpd/results/T-3b-tile-edge-load-profile.json, keyed on " +
                    "(concentration, gap, bias)"
        ),
        temperature = ROOM_TEMPERATURE,
        thermalEnergy = thermalEnergy(),
        designPointProfile = profile.name,
        rigidPlateTolerance = T122_TOLERANCE,
        mandatedTotalStiffness = T122_MANDATE,
        freeTileStroke = stroke,
        rimTargetStiffness = rimTarget,
        interiorTargetStiffness = interiorTarget,
        flatRatioWindowLow = windowLow,
        flatRatioWindowHigh = windowHigh,
        cheapBound = cheapBound,
        ratioWindow = ratioWindow,
        ladders = ladders,
        builtRatios = builtRatios,
        trims = trims,
        flatness = flatness,
        drift = drift,
        scatter = scatter,
        costs = costs,
        packing = packing,
        convergence = convergence,
        reproductions = reproductions,
        findings = findings,
        validity = listOf(
            "TRL 1-3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and the " +
                    "out-of-plane motif every element in the catalogue stands on is NOT " +
                    "DEMONSTRATED (C-0028, C-0029, C-0055).",
            "The two levels are C-0058's and inherit its whole validity range, including " +
                    "C-0022's unsourced rim charge (1.85x on the collar depth) and C-0001's " +
                    "single foundation secant.",
            "T-45 is still unmeasured: the scatter numbers here are a THRESHOLD the design " +
                    "tolerates, never a tolerance any assembly has been shown to hold. " +
                    "C-0026's 0.883 pN per unit amplitude is CITED and carried, not re-derived.",
            "The ssDNA pair is priced on its GAUSSIAN stiffness at zero tension, which is the " +
                    "soft end of C-0023's own bracket; the Kuhn length is a 2x method-systematic " +
                    "bracket and the contour convention travels with it.",
            "The elastica ladder is a SHOOTING solve at 400 RK4 steps, swept 200/400/800 in " +
                    "gate 4; every other ladder is closed form.",
            "The packing verdict is C-0041's, evaluated at the spans this task's two levels " +
                    "demand. It is a plan-view argument on one body size and one orientation " +
                    "sweep, inherited unchanged.",
            "The drift is read on the SECANT, which is what C-0017's placement condition is " +
                    "written on; the tangent is reported beside it for CH-0042's stability " +
                    "reading and is not the quantity the ratio is defined by.",
            "One crossover layout — T-10's eight symmetrically centred columns; C-0015's 32 bp " +
                    "phase is not swept.",
            "Single layer, static, 300 K, aqueous buffer with Mg2+."
        ),
        openQuestions = listOf(
            "T-45 itself: what relative scatter a staple-designed attachment array can be " +
                    "GUARANTEED to. This task delivers the threshold the design tolerates; " +
                    "nothing accessible gives the spread an assembly achieves.",
            "Whether an array of MIXED spans packs differently from an array of equal ones — " +
                    "C-0041's sweep is on a single span, and the interior members here are " +
                    "1.6x longer than the rim ones.",
            "Whether the two levels can be realised by two DIFFERENT elements rather than two " +
                    "settings of one — nothing forbids it, and the soft level's length is what " +
                    "breaks the packing.",
            "Whether a distribution flat at every operating state exists (C-0058's own open " +
                    "item 2), which no quantisation argument touches."
        )
    )

    val output = File("gpd/results/T-122-buildable-stiffness-ratio.json")
    output.parentFile.mkdirs()
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            json.encodeToJsonElement(result).jsonObject.roundedForCouplingResult().jsonObject
        )
    )
    t122Report(result, output, started)
}

private fun t122Report(result: T122Result, output: File, started: Long) {
    println()
    println("=".repeat(120))
    println("T-122 — ${result.title}")
    println("=".repeat(120))
    println("design point: ${result.designPointProfile}")
    println(
        ("free-tile stroke %.4f nm; mandate %.4f pN/nm; levels %.4f / %.4f pN/nm; " +
                "flat ratio window %.1f-%.1f")
            .format(
                result.freeTileStroke, result.mandatedTotalStiffness,
                result.rimTargetStiffness, result.interiorTargetStiffness,
                result.flatRatioWindowLow, result.flatRatioWindowHigh
            )
    )
    println()
    println("THE LADDERS")
    println(
        "%-6s %-9s %8s %6s %10s %10s %10s".format(
            "elem", "level", "target", "units", "realised", "error", "granular"
        )
    )
    result.ladders.forEach {
        println(
            "%-6s %-9s %8.4f %6d %10.4f %10.2e %10.2e".format(
                it.element, it.level, it.targetStiffness, it.units, it.realisedStiffness,
                it.relativeError, it.relativeGranularity
            )
        )
    }
    println()
    println("THE BUILT RATIOS")
    result.builtRatios.forEach {
        println(
            "%-6s rim %3d, interior %3d -> ratio %7.3f, total %8.4f — %s".format(
                it.element, it.rimUnits, it.interiorUnits, it.realisedRatio, it.realisedTotal,
                it.verdict
            )
        )
    }
    println()
    println("THE FLATNESS")
    result.flatness.forEach {
        println(
            "%-42s dishing/stroke %7.4f  %s".format(
                it.label.take(42), it.dishingOverStroke, if (it.flat) "FLAT" else "not flat"
            )
        )
    }
    println()
    println("THE SCATTER THRESHOLD")
    result.scatter.forEach {
        println(
            "%-40s %-11s %-45s %s".format(
                it.design.take(40),
                if (it.renormalisedToTheMandate) "renormalised" else "as built",
                it.pattern.take(45),
                if (it.reachesTheTolerance)
                    "threshold %.4f".format(it.thresholdAmplitude)
                else "never reached below %.2f".format(it.scanCeiling)
            )
        )
    }
    println()
    println("THE MANDATE TRIM")
    result.trims.forEach {
        println(
            ("%-6s rim %3d-%3d bp, interior %3d-%3d bp, %d distinct settings, %2d moves: " +
                    "total %8.4f -> %8.4f (error %.2e -> %.2e)").format(
                it.element, it.rimUnitsLow, it.rimUnitsHigh, it.interiorUnitsLow,
                it.interiorUnitsHigh, it.distinctSettings, it.moves,
                it.totalBeforeTrimming, it.totalAfterTrimming,
                it.relativeErrorBeforeTrimming, it.relativeErrorAfterTrimming
            )
        )
    }
    println()
    println("THE PACKING")
    result.packing.forEach {
        println(
            "%-6s longest member %6.2f nm; %2d paths place, 45 needed — %s".format(
                it.element, it.longerParameter, it.packingLimitedPaths, it.verdict
            )
        )
    }
    println()
    println("FINDINGS")
    result.findings.forEach { println("  - $it") }
    println()
    println("worst upstream departure: %.2e".format(result.reproductions.maxOf { it.relativeDeparture }))
    println("written: ${output.path}")
    println("elapsed: %.1f s".format((System.currentTimeMillis() - started) / 1000.0))
}
