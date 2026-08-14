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

package com.xemantic.nano.plentyofroom.synthesis

import com.xemantic.nano.plentyofroom.anchoring.AnchorMaterials
import com.xemantic.nano.plentyofroom.anchoring.AxialDuplexStandoff
import com.xemantic.nano.plentyofroom.anchoring.CoupledJointFlexure
import com.xemantic.nano.plentyofroom.anchoring.FlexureEndCondition
import com.xemantic.nano.plentyofroom.anchoring.FlexureOrientation
import com.xemantic.nano.plentyofroom.anchoring.StandoffBase
import com.xemantic.nano.plentyofroom.anchoring.TransverseDuplexFlexure
import com.xemantic.nano.plentyofroom.anchoring.TwoSpringElastica
import com.xemantic.nano.plentyofroom.anchoring.coupledFlexureSpan
import com.xemantic.nano.plentyofroom.anchoring.elasticaArmForStiffness
import com.xemantic.nano.plentyofroom.anchoring.flexureSpanForStiffness
import com.xemantic.nano.plentyofroom.anchoring.hingeArmForStiffness
import com.xemantic.nano.plentyofroom.anchoring.hingeLineCensus
import com.xemantic.nano.plentyofroom.anchoring.packingLimitedPathCount
import com.xemantic.nano.plentyofroom.anchoring.standoffTipFlexibility
import com.xemantic.nano.plentyofroom.anchoring.tileCrossoverInventory
import com.xemantic.nano.plentyofroom.brush.AlexanderBoxLayer
import com.xemantic.nano.plentyofroom.brush.GraftedChain
import com.xemantic.nano.plentyofroom.brush.GraftedLayerModel
import com.xemantic.nano.plentyofroom.brush.InteractionFreeEnergy
import com.xemantic.nano.plentyofroom.brush.StrongStretchingLayer
import com.xemantic.nano.plentyofroom.brush.additiveInteraction
import com.xemantic.nano.plentyofroom.brush.chainLengthForHeight
import com.xemantic.nano.plentyofroom.brush.desCloizeauxInteraction
import com.xemantic.nano.plentyofroom.brush.graftedChain
import com.xemantic.nano.plentyofroom.brush.load
import com.xemantic.nano.plentyofroom.brush.reducedSecondVirialCoefficient
import com.xemantic.nano.plentyofroom.brush.reducedThirdVirialCoefficient
import com.xemantic.nano.plentyofroom.brush.threeBodyInteraction
import com.xemantic.nano.plentyofroom.brush.twoBodyInteraction
import com.xemantic.nano.plentyofroom.coupling.mandatedCouplingStiffness
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.abs

/**
 * Tasks `T-107` and `T-108` — the stroke `C-0023`'s compliance ceiling is owed at, and whether
 * §3's **desired** ~10 nm stroke is reachable by any coupling this programme has. Leaf `A8.2`.
 *
 * Run with:
 *
 * ```shell
 * ./gradlew study -Pstudy=synthesis.DesiredStrokeReachStudyKt -PbuildDirectory=build-t108
 * ```
 *
 * Emits `gpd/results/T-108-desired-stroke-reach.json`, deterministically — no timestamp, every
 * floating-point number rounded at the serialisation boundary, and every predicate decided by
 * [bindingConstraint]'s fixed declaration order rather than by a scan over a map.
 *
 * Consumes, **as libraries re-run rather than tabulated**: `C-0017`'s mandate, `C-0023`'s
 * element catalogue and its declared ceiling, `C-0030`'s coupled flexure, `C-0039`'s elastica,
 * `C-0040`'s hinge-line census, `C-0041`'s packing solver, and `C-0003`'s six layer models.
 * Owns `CouplingCeiling.kt`, `StrokeReach.kt` and this file, and edits nothing.
 */

// ---------------------------------------------------------------------------------------------
// records
// ---------------------------------------------------------------------------------------------

/** How `C-0023`'s declared ceiling reads when the stroke it is evaluated at is named. */
@Serializable
data class T108CeilingReadingRecord(
    val clause: String,
    val stroke: Double,
    val mandate: Double,
    val declaredCeiling: Double,
    val perPathSecantCeilingAt45: Double,
    val perPathSecantCeilingAt15: Double,
    val note: String
)

/** One `(layer height, model)` state's three stroke ceilings, none of which contains a coupling. */
@Serializable
data class T108ReachRecord(
    val model: String,
    val nominalHeight: Double,
    val graftingDensity: Double,
    val restingHeight: Double,
    val dryThickness: Double,
    val restingVolumeFraction: Double,
    val kinematicCeiling: Double,
    val validityCeiling: Double?,
    val deadLoadStroke: Double,
    val acceptableStrokeReached: Boolean,
    val desiredStrokeReached: Boolean,
    val desiredOverKinematic: Double,
    val desiredOverValidity: Double?,
    val desiredOverDeadLoad: Double
)

/**
 * `CH-0047`'s open convention, made a number: the same element's stability tangent read over the
 * range the device TRAVERSES against the range `CH-0042` prescribed.
 */
@Serializable
data class T108RangeRecord(
    val element: String,
    val pathCount: Int,
    val placementStroke: Double,
    val tangentAtPlacement: Double,
    val minimumOverTraversedRange: Double,
    val minimumOverPrescribedRange: Double,
    val argminOfPrescribedRange: Double,
    val floorsClearedAtPlacement: Int,
    val floorsClearedOverPrescribedRange: Int,
    val floorCount: Int,
    val note: String
)

/** The layer height §3 would have to name for a 10 nm stroke at 100 pN. */
@Serializable
data class T108EscapeRecord(
    val name: String,
    val axis: String,
    val value: Double,
    val unit: String,
    val owner: String,
    val note: String
)

/** One catalogue element at one stroke — the synthesis table `T-108` was asked for. */
@Serializable
data class T108ReproductionRecord(
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val relativeDeparture: Double,
    val source: String
)

/** A convergence check with the knob it was taken over. */
@Serializable
data class T108ConvergenceRecord(
    val quantity: String,
    val knob: String,
    val coarse: Double,
    val fine: Double,
    val relativeMovement: Double
)

@Serializable
@Suppress("LongParameterList")
data class T108Result(
    val task: String,
    val leaf: String,
    val question: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val parameters: Map<String, String>,
    val citedInputs: Map<String, String>,
    val requirementsAtAcceptable: List<StrokeBoundRequirement>,
    val requirementsAtDesired: List<StrokeBoundRequirement>,
    val ceilingReadings: List<T108CeilingReadingRecord>,
    val stabilityRanges: List<T108RangeRecord>,
    val reachBounds: List<ReachBound>,
    val reach: List<T108ReachRecord>,
    val catalogue: List<CatalogueRow>,
    val escapes: List<T108EscapeRecord>,
    val reproductions: List<T108ReproductionRecord>,
    val convergence: List<T108ConvergenceRecord>,
    val verdict: Map<String, String>
)

// ---------------------------------------------------------------------------------------------
// parameters
// ---------------------------------------------------------------------------------------------

private const val TARGET_FORCE = 100.0

private const val ACCEPTABLE_STROKE = 3.0

private const val DESIRED_STROKE = 10.0

private const val UNZIP_ALLOWABLE = 10.0

private const val FOOTPRINT = 1600.0

/**
 * `C-0017`'s six stability floors `|k_eff(3 nm)|` at the 10 nm layer in §3's own 2 mM — **CITED**,
 * read from `gpd/results/T-16-output-coupling-stiffness.json`, in that file's model order.
 */
private val FLOORS_TWO_MILLIMOLAR = listOf(
    27.9133262, 23.4139164, 24.9042565, 27.0387111, 23.8036442, 23.9527371
)

/** `C-0017`'s worst stability floor at 10 nm in §3's own 2 mM — **CITED**. */
private const val FLOOR_TWO_MILLIMOLAR = 27.91

/** The same at leaf `A2.2`'s 0.5 mM — **CITED**. */
private const val FLOOR_HALF_MILLIMOLAR = 15.94

/** `C-0034`'s adopted `A2` anchorage, the arm's own duplex end — **CITED**, reproduced below. */
private const val DUPLEX_END_ANCHORAGE = 78.2352941176

private const val OSMOTIC_SECOND_VIRIAL = 1.9e-3

private const val OSMOTIC_THIRD_VIRIAL = 2.0e-2

private val DESIGN_POINTS = listOf(5.0 to 0.092, 7.0 to 0.045, 10.0 to 0.024)

/** `C-0027`'s surviving 10 nm window in `σ`, swept for the largest stroke it admits. */
private val TEN_NANOMETRE_WINDOW = listOf(0.0116, 0.016, 0.020, 0.024, 0.030, 0.045, 0.10, 0.2885)

private const val STANDOFF_LENGTH = 8.0

/** `C-0002`'s concentrated crossover, the volume fraction the layer's equation of state ends at. */
private const val CONCENTRATED_CROSSOVER = 0.2

/**
 * How finely `min_s k_tangent(s)` is sampled over the traversed range.
 *
 * Sixty-four, not the default two thousand: every sample of `C-0039`'s elastica is a shooting
 * solve, and the convergence record below shows 64 → 256 moves the minimum by under 1e-4 on the
 * one element whose minimum is interior.
 */
private const val TANGENT_SAMPLES = 64

/** `C-0015`'s single-layer Rothemund sheet across a 40 nm tile. */
private const val TILE_DUPLEXES = 15

// ---------------------------------------------------------------------------------------------
// the layer
// ---------------------------------------------------------------------------------------------

private fun interactionFor(peg: PegWater, choice: String): InteractionFreeEnergy {
    val twoBody = twoBodyInteraction(
        peg.reducedSecondVirialCoefficient(OSMOTIC_SECOND_VIRIAL), peg.monomerVolume
    )
    val threeBody = threeBodyInteraction(
        peg.reducedThirdVirialCoefficient(OSMOTIC_THIRD_VIRIAL), peg.monomerVolume
    )
    return when (choice) {
        "two-body" -> twoBody
        "virial" -> additiveInteraction("virial", listOf(twoBody, threeBody))
        else -> desCloizeauxInteraction(peg.crossoverIndex, peg.monomerVolume)
    }
}

private fun layerModels(peg: PegWater): List<GraftedLayerModel> =
    listOf("alexander-box", "strong-stretching").flatMap { profile ->
        listOf("two-body", "virial", "des-Cloizeaux").map { interaction ->
            val energy = interactionFor(peg, interaction)
            if (profile == "alexander-box") AlexanderBoxLayer(energy) else StrongStretchingLayer(energy)
        }
    }

private fun chainOf(
    peg: PegWater,
    model: GraftedLayerModel,
    height: Double,
    density: Double
): GraftedChain = peg.graftedChain(model.chainLengthForHeight(peg, height, density), density)

private fun reachRecord(
    peg: PegWater,
    model: GraftedLayerModel,
    height: Double,
    density: Double
): T108ReachRecord {
    val chain = chainOf(peg, model, height, density)
    val resting = model.equilibriumHeight(chain)
    val dry = chain.occupiedThickness
    val floor = dry * 1.01
    val kinematic = kinematicStrokeCeiling(resting, floor)
    // where the layer already sits past C-0002's crossover at zero compression the validity
    // ceiling does not exist at all — a fact worth recording rather than a number to invent
    val validity =
        if (dry / CONCENTRATED_CROSSOVER < resting) validityStrokeCeiling(resting, dry) else null
    val dead = deadLoadStroke(resting, floor, TARGET_FORCE) { h -> model.load(chain, h, FOOTPRINT) }
    return T108ReachRecord(
        model = model.name,
        nominalHeight = height,
        graftingDensity = density,
        restingHeight = resting,
        dryThickness = dry,
        restingVolumeFraction = dry / resting,
        kinematicCeiling = kinematic,
        validityCeiling = validity,
        deadLoadStroke = dead,
        acceptableStrokeReached = dead >= ACCEPTABLE_STROKE,
        desiredStrokeReached = dead >= DESIRED_STROKE,
        desiredOverKinematic = strokeShortfall(DESIRED_STROKE, kinematic),
        desiredOverValidity = validity?.let { strokeShortfall(DESIRED_STROKE, it) },
        desiredOverDeadLoad = strokeShortfall(DESIRED_STROKE, dead)
    )
}

// ---------------------------------------------------------------------------------------------
// the catalogue
// ---------------------------------------------------------------------------------------------

private class Element(
    val name: String,
    val owner: String,
    val pathCount: Int,
    val span: Double,
    val secant: (Double) -> Double,
    val tangent: (Double) -> Double,
    val latticeSupplies: Boolean,
    val packs: Boolean,
    val packingAssessed: Boolean,
    val note: String
)

@Suppress("LongMethod")
private fun catalogue(): List<Element> {
    val ei = Gen1Tile.DUPLEX_BENDING_RIGIDITY
    val stretch = AnchorMaterials.DUPLEX_STRETCH_MODULUS
    val hinge = Gen1Tile.crossoverHingeStiffness()
    val mandate = mandatedCouplingStiffness(TARGET_FORCE, ACCEPTABLE_STROKE)
    // C-0040's census, re-run: the richest crossover inventory a 40 nm tile carries, and the
    // largest number of them that can be made collinear on ONE interface
    val census = hingeLineCensus(Gen1Tile.EDGE_X)
    val tileInventory = census.maxOf { tileCrossoverInventory(TILE_DUPLEXES, it.evenInterfaces, it.oddInterfaces) }
    val largestLine = census.maxOf { it.largest }
    val elements = mutableListOf<Element>()

    // E1 — the axial duplex standoff, C-0017's K1
    val standoff = AxialDuplexStandoff(stretch, 5.0)
    elements += Element(
        "E1 axial duplex standoff (5 nm)", "C-0023 / C-0017 K1", 45, 5.0,
        { 45 * standoff.stiffness }, { 45 * standoff.stiffness }, true, true, false,
        "two-sided and 297x too stiff: it fails PLACEMENT, at every stroke"
    )

    // E3a — the transverse flexure with ends free to draw in, at 45 and at 15 paths
    listOf(45, 15).forEach { count ->
        val span = flexureSpanForStiffness(
            ei, FlexureEndCondition.PINNED_ENDS, false, stretch, count, mandate, ACCEPTABLE_STROKE
        )
        val beam = TransverseDuplexFlexure(ei, span, FlexureEndCondition.PINNED_ENDS, false, stretch)
        elements += Element(
            "E3a transverse flexure, ends free ($count paths)", "C-0023", count, span,
            { d -> count * beam.secantStiffness(d) }, { d -> count * beam.tangentStiffness(d) },
            true, packingLimitedPathCount(Gen1Tile.EDGE_X, 15, span) >= count, true,
            "exactly linear, so secant = tangent at every stroke"
        )
    }

    // E3b — the same beam with its ends held axially: C-0023's own failed reading
    val heldSpan = flexureSpanForStiffness(
        ei, FlexureEndCondition.PINNED_ENDS, true, stretch, 45, mandate, ACCEPTABLE_STROKE
    )
    val heldBeam = TransverseDuplexFlexure(ei, heldSpan, FlexureEndCondition.PINNED_ENDS, true, stretch)
    elements += Element(
        "E3b transverse flexure, ends held", "C-0023", 45, heldSpan,
        { d -> 45 * heldBeam.secantStiffness(d) }, { d -> 45 * heldBeam.tangentStiffness(d) },
        true, packingLimitedPathCount(Gen1Tile.EDGE_X, 15, heldSpan) >= 45, true,
        "the membrane term makes it convex; C-0023 reports it past the declared ceiling"
    )

    // E5 — C-0023's crossover-hinge flexure on a single crossover, linear by construction
    val arm = hingeArmForStiffness(hinge, ei, 45, mandate)
    elements += Element(
        "E5 crossover-hinge flexure (1 crossover, rigid arm)", "C-0023", 45, arm,
        { mandate }, { mandate }, 45 <= tileInventory, true, false,
        "C-0023's own linear reading, superseded by C-0029/C-0034/C-0039's exact rotation"
    )

    // E5a — C-0039's two-spring elastica, at the asserted 16 and at the counts C-0040 supplies
    listOf(16, 4, 2, 1).forEach { hinges ->
        val length = elasticaArmForStiffness(hinge, hinges, DUPLEX_END_ANCHORAGE)
        val element = TwoSpringElastica(ei, length, hinges * hinge, DUPLEX_END_ANCHORAGE)
        val supplied = hinges <= largestLine && hinges * 45 <= tileInventory
        elements += Element(
            "E5a$hinges two-spring elastica arm", "C-0039 / C-0034", 45, length,
            { d -> 45 * element.secantStiffness(d) }, { d -> 45 * element.tangentStiffness(d) },
            supplied, true, false,
            if (supplied) "one crossover per flexure, 45 of the tile's $tileInventory — the " +
                    "only hinge count 45 paths can be given (C-0040)"
            else "45 paths at $hinges crossovers demand ${45 * hinges} against the tile's " +
                    "$tileInventory (C-0040); and sixteen collinear do not exist at any phase"
        )
    }

    // C-0030's coupled-standoff flexure, both mountings, at 45 and at C-0041's buildable 15
    val flexibility = standoffTipFlexibility(
        ei, STANDOFF_LENGTH, StandoffBase.crossovers(2).rotationalStiffness
    )
    listOf(
        FlexureOrientation.FAVOURABLE to "favourable",
        FlexureOrientation.ADVERSE to "adverse"
    ).forEach { (orientation, label) ->
        listOf(45, 15).forEach { count ->
            val span = coupledFlexureSpan(
                ei, flexibility, count, mandate, ACCEPTABLE_STROKE, orientation
            )
            val beam = CoupledJointFlexure(ei, span, flexibility)
            elements += Element(
                "C-0030 coupled flexure, $label ($count paths)", "C-0030 / C-0037", count, span,
                { d -> count * beam.strokeSecantStiffness(d, orientation) },
                { d -> count * beam.strokeTangentStiffness(d, orientation) },
                true, packingLimitedPathCount(Gen1Tile.EDGE_X, 15, span) >= count, true,
                "clearance ceiling l - d = ${"%.2f".format(STANDOFF_LENGTH - Gen1Tile.INTERHELICAL_SHEET)} nm " +
                        "on the favourable mounting (C-0030, C-0035)"
            )
        }
    }

    // the hypothetical ideal: any linear coupling placed at §3's DESIRED clause, 10 pN/nm
    val desiredMandate = mandatedCouplingStiffness(TARGET_FORCE, DESIRED_STROKE)
    elements += Element(
        "ideal linear coupling placed at §3's DESIRED clause", "T-108 (hypothetical)", 15, 0.0,
        { desiredMandate }, { desiredMandate }, true, true, false,
        "not an element — the best any coupling could be if the desired clause were the design " +
                "target: 100 pN over 10 nm is 10 pN/nm, and 10 < every 2 mM stability floor"
    )
    return elements
}

private fun rowsFor(
    element: Element,
    readAt: Double,
    mandate: Double,
    reaches: Boolean
): CatalogueRow = runCatching { evaluate(element, readAt, mandate, reaches) }.getOrElse { failure ->
    infeasibleCatalogueRow(
        element = element.name,
        owner = element.owner,
        readAtStroke = readAt,
        pathCount = element.pathCount,
        elementSpan = element.span,
        complianceCeiling = declaredComplianceCeiling(TARGET_FORCE, ACCEPTABLE_STROKE),
        unzipAllowable = UNZIP_ALLOWABLE,
        reason = failure.message ?: "the element's law is undefined here"
    )
}

private fun evaluate(
    element: Element,
    readAt: Double,
    mandate: Double,
    reaches: Boolean
): CatalogueRow {
    val stability = minimumTangent(
        minOf(ACCEPTABLE_STROKE, readAt), readAt, TANGENT_SAMPLES
    ) { element.tangent(it) }
    return catalogueRow(
        element = element.name,
        owner = element.owner,
        readAtStroke = readAt,
        pathCount = element.pathCount,
        elementSpan = element.span,
        secant = element.secant(readAt),
        tangent = element.tangent(readAt),
        mandate = mandate,
        complianceCeiling = declaredComplianceCeiling(TARGET_FORCE, ACCEPTABLE_STROKE),
        unzipAllowable = UNZIP_ALLOWABLE,
        stabilityFloorTwoMillimolar = FLOOR_TWO_MILLIMOLAR,
        stabilityFloorHalfMillimolar = FLOOR_HALF_MILLIMOLAR,
        latticeSupplies = element.latticeSupplies,
        packs = element.packs,
        reachesTheStroke = reaches,
        stabilityTangent = stability,
        packingAssessed = element.packingAssessed,
        placementIsEquality = readAt <= ACCEPTABLE_STROKE,
        placementTolerance = 1.0e-4,
        note = element.note
    )
}

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val peg = PegWater()
    val models = layerModels(peg)
    val mandate = mandatedCouplingStiffness(TARGET_FORCE, ACCEPTABLE_STROKE)
    val ceiling = declaredComplianceCeiling(TARGET_FORCE, ACCEPTABLE_STROKE)

    // ------------------------------------------------------- T-107: the ceiling, with its stroke

    val ceilingReadings = listOf(
        T108CeilingReadingRecord(
            clause = "§3 ACCEPTABLE — 100 pN at 3 nm",
            stroke = ACCEPTABLE_STROKE,
            mandate = mandate,
            declaredCeiling = ceiling,
            perPathSecantCeilingAt45 = perPathSecantCeiling(UNZIP_ALLOWABLE, 45, ACCEPTABLE_STROKE),
            perPathSecantCeilingAt15 = perPathSecantCeiling(UNZIP_ALLOWABLE, 15, ACCEPTABLE_STROKE),
            note = "the stroke C-0023 wrote the ceiling at, and the only stroke it is owed at"
        ),
        T108CeilingReadingRecord(
            clause = "§3 DESIRED — 100 pN at ~10 nm",
            stroke = DESIRED_STROKE,
            mandate = mandatedCouplingStiffness(TARGET_FORCE, DESIRED_STROKE),
            declaredCeiling = declaredComplianceCeiling(TARGET_FORCE, DESIRED_STROKE),
            perPathSecantCeilingAt45 = perPathSecantCeiling(UNZIP_ALLOWABLE, 45, DESIRED_STROKE),
            perPathSecantCeilingAt15 = perPathSecantCeiling(UNZIP_ALLOWABLE, 15, DESIRED_STROKE),
            note = "the same construction at the desired clause is 12 pN/nm, not 40 — so reading " +
                    "40 at 10 nm is not conservative, it is the wrong clause's number"
        )
    )

    val requirementsAtAcceptable = couplingRequirements(
        TARGET_FORCE, ACCEPTABLE_STROKE, 45, UNZIP_ALLOWABLE, FLOOR_TWO_MILLIMOLAR
    )
    val requirementsAtDesired = couplingRequirements(
        TARGET_FORCE, ACCEPTABLE_STROKE, 45, UNZIP_ALLOWABLE, FLOOR_TWO_MILLIMOLAR,
        largestStrokeTraversed = DESIRED_STROKE
    )

    // --------------------------------------- T-107, second half: the range a tangent is read over

    val flexibility = standoffTipFlexibility(
        Gen1Tile.DUPLEX_BENDING_RIGIDITY, STANDOFF_LENGTH,
        StandoffBase.crossovers(2).rotationalStiffness
    )
    val stabilityRanges = listOf(45, 15).map { count ->
        val span = coupledFlexureSpan(
            Gen1Tile.DUPLEX_BENDING_RIGIDITY, flexibility, count, mandate, ACCEPTABLE_STROKE,
            FlexureOrientation.FAVOURABLE
        )
        val beam = CoupledJointFlexure(Gen1Tile.DUPLEX_BENDING_RIGIDITY, span, flexibility)
        fun tangent(s: Double) =
            count * beam.strokeTangentStiffness(s, FlexureOrientation.FAVOURABLE)
        val samples = (0..4096).map { DESIRED_STROKE * it / 4096 }
        val argmin = samples.drop(1).minByOrNull { tangent(it) } ?: ACCEPTABLE_STROKE
        val prescribed = tangent(argmin)
        val traversed = minimumTangent(0.0 + 1e-9, ACCEPTABLE_STROKE, 4096, ::tangent)
        T108RangeRecord(
            element = "C-0030 coupled flexure, favourable",
            pathCount = count,
            placementStroke = ACCEPTABLE_STROKE,
            tangentAtPlacement = tangent(ACCEPTABLE_STROKE),
            minimumOverTraversedRange = traversed,
            minimumOverPrescribedRange = prescribed,
            argminOfPrescribedRange = argmin,
            floorsClearedAtPlacement = FLOORS_TWO_MILLIMOLAR.count { tangent(ACCEPTABLE_STROKE) > it },
            floorsClearedOverPrescribedRange = FLOORS_TWO_MILLIMOLAR.count { prescribed > it },
            floorCount = FLOORS_TWO_MILLIMOLAR.size,
            note = "a device PLACED at 3 nm never occupies the stroke CH-0042's minimum is " +
                    "taken at; T-107's answer applies to the FLOOR as well as to the ceiling"
        )
    }

    // ------------------------------------------------------------ T-108: the reach, before any coupling

    val reach = DESIGN_POINTS.flatMap { (height, density) ->
        models.map { reachRecord(peg, it, height, density) }
    }
    val windowSweep = models.flatMap { model ->
        TEN_NANOMETRE_WINDOW.map { density -> reachRecord(peg, model, 10.0, density) }
    }
    val bestFreeStroke = (reach + windowSweep).mapNotNull { it.validityCeiling }.max()
    val bestDeadLoad = (reach + windowSweep).maxOf { it.deadLoadStroke }
    val bestKinematic = (reach + windowSweep).maxOf { it.kinematicCeiling }

    val reachBounds = listOf(
        ReachBound(
            "the stroke is L0 - h, so s < L0 <= 10 nm at every height §3 names",
            10.0, 1.0, settlesTheQuestion = true, containsACoupling = false,
            note = "a 10 nm stroke on a 10 nm layer IS the statement h = 0. No bias, no coupling " +
                    "and no origami can move an identity"
        ),
        ReachBound(
            "kinematic ceiling L0 - N sigma v0, best over the whole sweep",
            bestKinematic, strokeShortfall(DESIRED_STROKE, bestKinematic),
            settlesTheQuestion = true, containsACoupling = false,
            note = "the layer's own dry thickness, at zero load and any bias"
        ),
        ReachBound(
            "validity ceiling L0 - N sigma v0 / 0.2, best over the whole sweep",
            bestFreeStroke, strokeShortfall(DESIRED_STROKE, bestFreeStroke),
            settlesTheQuestion = true, containsACoupling = false,
            note = "C-0002's concentrated crossover, which is C-0018's OWN binding bias ceiling " +
                    "at 10 nm at 6 of 6 models and every buffer"
        ),
        ReachBound(
            "dead-load stroke at §3's 100 pN, best over the whole sweep",
            bestDeadLoad, strokeShortfall(DESIRED_STROKE, bestDeadLoad),
            settlesTheQuestion = true, containsACoupling = false,
            note = "and a coupling can only REDUCE it: C-0017's own gate-2 theorem is that the " +
                    "delivered stroke is monotone decreasing in the coupling stiffness"
        )
    )

    // ------------------------------------------------------------------------ the catalogue

    val elements = catalogue()
    val catalogue = elements.flatMap { element ->
        listOf(
            rowsFor(element, ACCEPTABLE_STROKE, mandate, reaches = true),
            rowsFor(
                element, DESIRED_STROKE,
                mandatedCouplingStiffness(TARGET_FORCE, DESIRED_STROKE), reaches = false
            )
        )
    }

    // ------------------------------------------------------------------------ the escapes

    val requiredHeights = models.map { model ->
        fun strokeAt(height: Double): Double {
            val chain = chainOf(peg, model, height, 0.024)
            val resting = model.equilibriumHeight(chain)
            return deadLoadStroke(resting, chain.occupiedThickness * 1.01, TARGET_FORCE) { h ->
                model.load(chain, h, FOOTPRINT)
            }
        }
        model.name to restingHeightForStroke(DESIRED_STROKE, 10.0, 120.0) { strokeAt(it) }
    }
    val escapes = requiredHeights.map { (name, height) ->
        T108EscapeRecord(
            name = "layer height for a 10 nm stroke at 100 pN — $name",
            axis = "specification (§3's 5/7/10 nm heights)",
            value = height, unit = "nm", owner = "C-0001, and no task owns it yet",
            note = "at sigma = 0.024 nm^-2; C-0001 already recorded the direction — the reason " +
                    "to go outside the 5-10 nm range is UPWARD"
        )
    } + listOf(
        T108EscapeRecord(
            "tile footprint for the flexure array at the desired stroke", "specification (T-102)",
            2330.0, "nm^2", "C-0041", "1.44x the Gen-1 footprint, 1.20x in edge — CITED"
        ),
        T108EscapeRecord(
            "superstructure slot per flexure at the desired stroke", "specification (T-95)",
            2223.0, "nm^2", "C-0035", "1.39x the whole footprint over 45 paths — CITED"
        ),
        T108EscapeRecord(
            "buffer at which the surviving coupling is stable", "specification (T-63)",
            0.5, "mM MgCl2", "C-0032", "a REQUIREMENT for C-0030's law, not an improvement — CITED"
        )
    )

    // ------------------------------------------------------------------- upstream reproductions

    val reproductions = reproductions(mandate, ceiling)
    val convergence = convergence(peg, models.first())

    val result = T108Result(
        task = "T-107, T-108",
        leaf = "A8.2",
        question = "Which stroke is C-0023's 40 pN/nm compliance ceiling owed at, and is §3's " +
                "desired ~10 nm stroke reachable by any coupling this programme has?",
        units = mapOf(
            "length" to "nm", "force" to "pN", "stiffness" to "pN/nm",
            "energy" to "pN·nm (k_BT = 4.141947 at 300 K)", "area" to "nm^2"
        ),
        conventions = listOf(
            "the stroke s = L0 - h is positive DOWNWARD, toward the electrode",
            "L0 is a FORCE-ONSET height (C-0011, CH-0010)",
            "a coupling reaction R(s) is positive UPWARD; placement is written on its SECANT " +
                    "and stability on its TANGENT (C-0017)",
            "every requirement below is quoted with the stroke it is read at — that is T-107",
            "a stability tangent is minimised over [s_placement, s_read], never over a range " +
                    "containing zero stroke (CH-0047)"
        ),
        parameters = mapOf(
            "targetForce" to TARGET_FORCE.toString(),
            "acceptableStroke" to ACCEPTABLE_STROKE.toString(),
            "desiredStroke" to DESIRED_STROKE.toString(),
            "unzipAllowable" to UNZIP_ALLOWABLE.toString(),
            "footprint" to FOOTPRINT.toString(),
            "declaredCeilingFactor" to DECLARED_CEILING_FACTOR.toString(),
            "standoffLength" to STANDOFF_LENGTH.toString(),
            "designPoints" to DESIGN_POINTS.toString(),
            "tenNanometreWindow" to TEN_NANOMETRE_WINDOW.toString(),
            "layerModels" to models.map { it.name }.toString()
        ),
        citedInputs = mapOf(
            "stability floor at 10 nm / 2 mM" to "$FLOOR_TWO_MILLIMOLAR pN/nm (C-0017, worst of six models)",
            "stability floor at 10 nm / 0.5 mM" to "$FLOOR_HALF_MILLIMOLAR pN/nm (C-0017)",
            "per-path unzip allowable" to "$UNZIP_ALLOWABLE pN (C-0006, CH-0029)",
            "duplex EI" to "${Gen1Tile.DUPLEX_BENDING_RIGIDITY} pN·nm^2 (CanDo MODEL INPUT, not a measurement)",
            "duplex S" to "${AnchorMaterials.DUPLEX_STRETCH_MODULUS} pN (Wang 1997, MEASURED)",
            "crossover hinge k_theta" to "${Gen1Tile.crossoverHingeStiffness()} pN·nm/rad (Chen 2014, FITTED)",
            "A2 anchorage couple" to "$DUPLEX_END_ANCHORAGE pN·nm/rad (C-0034)",
            "concentrated crossover" to "phi = 0.2 (C-0002)",
            "§3 targets" to "100 pN, 3 nm acceptable, ~10 nm desired, 40 x 40 nm, 5/7/10 nm, 2 mM"
        ),
        requirementsAtAcceptable = requirementsAtAcceptable,
        requirementsAtDesired = requirementsAtDesired,
        ceilingReadings = ceilingReadings,
        stabilityRanges = stabilityRanges,
        reachBounds = reachBounds,
        reach = reach + windowSweep,
        catalogue = catalogue,
        escapes = escapes,
        reproductions = reproductions,
        convergence = convergence,
        verdict = verdict(
            catalogue, bestKinematic, bestFreeStroke, bestDeadLoad, requiredHeights, stabilityRanges
        )
    )

    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-108-desired-stroke-reach.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(json.encodeToJsonElement(result).roundedForResult()) + "\n"
    )
    report(result, output)
}

private fun reproductions(mandate: Double, ceiling: Double): List<T108ReproductionRecord> {
    val ei = Gen1Tile.DUPLEX_BENDING_RIGIDITY
    val stretch = AnchorMaterials.DUPLEX_STRETCH_MODULUS
    val hinge = Gen1Tile.crossoverHingeStiffness()
    val flexibility = standoffTipFlexibility(
        ei, STANDOFF_LENGTH, StandoffBase.crossovers(2).rotationalStiffness
    )
    val coupledSpan = coupledFlexureSpan(
        ei, flexibility, 45, mandate, ACCEPTABLE_STROKE, FlexureOrientation.FAVOURABLE
    )
    val coupled = CoupledJointFlexure(ei, coupledSpan, flexibility)
    val armSixteen = elasticaArmForStiffness(hinge, 16, DUPLEX_END_ANCHORAGE)
    val elastica = TwoSpringElastica(ei, armSixteen, 16 * hinge, DUPLEX_END_ANCHORAGE)
    val freeSpan = flexureSpanForStiffness(
        ei, FlexureEndCondition.PINNED_ENDS, false, stretch, 45, mandate, ACCEPTABLE_STROKE
    )
    val heldSpan = flexureSpanForStiffness(
        ei, FlexureEndCondition.PINNED_ENDS, true, stretch, 45, mandate, ACCEPTABLE_STROKE
    )
    val heldBeam = TransverseDuplexFlexure(ei, heldSpan, FlexureEndCondition.PINNED_ENDS, true, stretch)
    fun record(name: String, published: Double, reproduced: Double, source: String) =
        T108ReproductionRecord(
            name, published, reproduced,
            if (published == 0.0) abs(reproduced) else abs(reproduced - published) / abs(published),
            source
        )
    return listOf(
        record("C-0017 mandate", 33.3333333333, mandate, "C-0017 P1"),
        record("C-0023 declared ceiling", 40.0, ceiling, "C-0023 / T-23 P2"),
        record("C-0017 K1 over-stiffness", 297.0, 45 * 220.0 / mandate, "C-0017"),
        record("C-0023 E3a span", 24.61, freeSpan, "C-0023"),
        record("C-0023 E3b span", 49.41, heldSpan, "C-0023"),
        record(
            "C-0023 E3b tangent at 3 nm", 91.13, 45 * heldBeam.tangentStiffness(ACCEPTABLE_STROKE),
            "C-0023"
        ),
        record("C-0023 E5 arm", 4.11, hingeArmForStiffness(hinge, ei, 45, mandate), "C-0023"),
        record("C-0039 E5a16 arm", 12.7198, armSixteen, "C-0039"),
        record(
            "C-0039 E5a16 tangent at 3 nm", 36.44,
            45 * elastica.tangentStiffness(ACCEPTABLE_STROKE), "C-0039"
        ),
        record(
            "C-0039 E5a16 secant at 10 nm", 69.94,
            45 * elastica.secantStiffness(DESIRED_STROKE), "C-0039"
        ),
        record(
            "C-0039 E5a16 tangent at 10 nm", 264.24,
            45 * elastica.tangentStiffness(DESIRED_STROKE), "C-0039"
        ),
        record("C-0030 coupled span", 31.821, coupledSpan, "C-0030"),
        record(
            "C-0030 coupled tangent at 3 nm", 25.227,
            45 * coupled.strokeTangentStiffness(ACCEPTABLE_STROKE, FlexureOrientation.FAVOURABLE),
            "C-0030"
        ),
        record(
            "C-0040 largest hinge line", 4.0,
            hingeLineCensus(Gen1Tile.EDGE_X).maxOf { it.largest }.toDouble(), "C-0040"
        ),
        record(
            "C-0041 packing-limited path count", 15.0,
            packingLimitedPathCount(Gen1Tile.EDGE_X, 15, 21.44).toDouble(), "C-0041"
        )
    )
}

private fun convergence(peg: PegWater, model: GraftedLayerModel): List<T108ConvergenceRecord> {
    val chain = chainOf(peg, model, 10.0, 0.024)
    val resting = model.equilibriumHeight(chain)
    val floor = chain.occupiedThickness * 1.01
    fun stroke(steps: Int) =
        deadLoadStroke(resting, floor, TARGET_FORCE, steps) { h -> model.load(chain, h, FOOTPRINT) }
    val coarse = stroke(64)
    val fine = stroke(4096)
    val elasticaCoarse = TwoSpringElastica(
        Gen1Tile.DUPLEX_BENDING_RIGIDITY, 12.7198,
        16 * Gen1Tile.crossoverHingeStiffness(), DUPLEX_END_ANCHORAGE, steps = 200
    ).tangentStiffness(DESIRED_STROKE)
    val elasticaFine = TwoSpringElastica(
        Gen1Tile.DUPLEX_BENDING_RIGIDITY, 12.7198,
        16 * Gen1Tile.crossoverHingeStiffness(), DUPLEX_END_ANCHORAGE, steps = 1600
    ).tangentStiffness(DESIRED_STROKE)
    return listOf(
        T108ConvergenceRecord(
            "dead-load stroke at 10 nm", "scan steps 64 -> 4096", coarse, fine,
            abs(fine - coarse) / fine
        ),
        T108ConvergenceRecord(
            "E5a16 tangent at the desired stroke", "elastica RK4 steps 200 -> 1600",
            elasticaCoarse, elasticaFine, abs(elasticaFine - elasticaCoarse) / elasticaFine
        ),
        run {
            val flexibility = standoffTipFlexibility(
                Gen1Tile.DUPLEX_BENDING_RIGIDITY, STANDOFF_LENGTH,
                StandoffBase.crossovers(2).rotationalStiffness
            )
            val span = coupledFlexureSpan(
                Gen1Tile.DUPLEX_BENDING_RIGIDITY, flexibility, 45,
                mandatedCouplingStiffness(TARGET_FORCE, ACCEPTABLE_STROKE), ACCEPTABLE_STROKE,
                FlexureOrientation.FAVOURABLE
            )
            val beam = CoupledJointFlexure(Gen1Tile.DUPLEX_BENDING_RIGIDITY, span, flexibility)
            fun tangent(s: Double) =
                45 * beam.strokeTangentStiffness(s, FlexureOrientation.FAVOURABLE)
            val low = minimumTangent(ACCEPTABLE_STROKE, DESIRED_STROKE, TANGENT_SAMPLES, ::tangent)
            val high = minimumTangent(ACCEPTABLE_STROKE, DESIRED_STROKE, 4096, ::tangent)
            T108ConvergenceRecord(
                "C-0030 minimum tangent over [3, 10] — the one INTERIOR minimum in the catalogue",
                "sampling ${TANGENT_SAMPLES} -> 4096", low, high, abs(high - low) / high
            )
        }
    )
}

@Suppress("LongParameterList")
private fun verdict(
    catalogue: List<CatalogueRow>,
    kinematic: Double,
    validity: Double,
    deadLoad: Double,
    requiredHeights: List<Pair<String, Double>>,
    ranges: List<T108RangeRecord>
): Map<String, String> {
    val atDesired = catalogue.filter { it.readAtStroke == DESIRED_STROKE }
    val atAcceptable = catalogue.filter { it.readAtStroke == ACCEPTABLE_STROKE }
    val clearing = atAcceptable.filter { it.predicates.clears }
    return mapOf(
        "T-107 — the stroke the ceiling is owed at" to
                ("the PLACEMENT stroke, 3 nm. 40 pN/nm is 1.2 x (100 pN / 3 nm): a DECLARED " +
                        "linearity tolerance on the placement discharge, carrying its stroke " +
                        "inside it. Neither C-0017 nor C-0018 contains any upper bound on a " +
                        "coupling tangent — placement is an EQUALITY on the secant, stability a " +
                        "FLOOR on the tangent, and C-0032 measures a stiffer tangent RAISING " +
                        "C-0018's pull-in margin. The same construction at §3's desired clause " +
                        "is 12 pN/nm, not 40"),
        "T-107 — what replaces it beyond the working point" to
                ("the per-path unzip allowable, which is a bound on a FORCE and therefore " +
                        "tightens as 1/s: " +
                        "${"%.1f".format(perPathSecantCeiling(UNZIP_ALLOWABLE, 45, DESIRED_STROKE))}" +
                        " pN/nm at 45 paths and " +
                        "${"%.1f".format(perPathSecantCeiling(UNZIP_ALLOWABLE, 15, DESIRED_STROKE))}" +
                        " at C-0041's buildable 15. C-0039's E5a16 secant at 10 nm, 69.94, is " +
                        "past both — so relaxing the declared ceiling moves the miss from 6.6x " +
                        "on the tangent to 1.55x and 4.66x on a CITED allowable, and does not " +
                        "move the verdict"),
        "T-107 — the same answer applied to the FLOOR (CH-0047, T-76b)" to
                ("a device placed at 3 nm traverses [0, 3] and never occupies the stroke " +
                        "CH-0042's minimum is taken at. " +
                        ranges.joinToString("; ") {
                            "at ${it.pathCount} paths the tangent at the placement stroke is " +
                                    "${"%.3f".format(it.tangentAtPlacement)} pN/nm and clears " +
                                    "${it.floorsClearedAtPlacement} of ${it.floorCount} of " +
                                    "C-0017's 2 mM floors, against " +
                                    "${"%.3f".format(it.minimumOverPrescribedRange)} at s = " +
                                    "${"%.3f".format(it.argminOfPrescribedRange)} nm clearing " +
                                    "${it.floorsClearedOverPrescribedRange}"
                        }),
        "T-108 — is the desired stroke reachable" to
                ("NO, and the binding bound contains no coupling at all. The stroke is L0 - h, " +
                        "so s < L0 <= 10 nm at every height §3 names: a 10 nm stroke on a 10 nm " +
                        "layer IS h = 0. The best kinematic ceiling anywhere in the sweep is " +
                        "${"%.3f".format(kinematic)} nm, the best inside C-0002's validity range " +
                        "${"%.3f".format(validity)} nm, and the best under §3's own 100 pN " +
                        "${"%.3f".format(deadLoad)} nm — and a coupling can only reduce the last"),
        "T-108 — catalogue at the desired stroke" to
                ("${atDesired.count { it.predicates.clears }} of ${atDesired.size} rows clear " +
                        "every predicate"),
        "T-108 — catalogue at the acceptable stroke" to
                ("${clearing.size} of ${atAcceptable.size} rows clear every predicate: " +
                        clearing.joinToString("; ") { it.element }),
        "T-108 — which statement is established" to
                ("unreachable ON §3's OWN STACK, which is stronger than 'unreachable with this " +
                        "catalogue' and weaker than 'unreachable in physics'. The escape is a " +
                        "TALLER LAYER: " +
                        requiredHeights.joinToString("; ") {
                            "${it.first} ${"%.2f".format(it.second)} nm"
                        } + " for a 10 nm stroke at 100 pN"),
        "§6 task 3" to
                ("UNAFFECTED. Its acceptance predicate is 'stroke >= ~3 nm and force >= 100 pN " +
                        "at <= 2 V, or a demonstration that it is unreachable'. The 3 nm clause " +
                        "is delivered (C-0012, C-0017); the ~10 nm figure is a §3 TARGET row, " +
                        "not a §6 predicate, and what is filed here is §7's 'stated plainly'")
    )
}

private fun report(result: T108Result, output: File) {
    println("T-107 / T-108 — the compliance ceiling's stroke, and the reach of the desired stroke")
    println()
    result.ceilingReadings.forEach {
        println(
            "  ${it.clause}: mandate ${"%.4f".format(it.mandate)}, declared ceiling " +
                    "${"%.4f".format(it.declaredCeiling)}, per-path ceiling " +
                    "${"%.2f".format(it.perPathSecantCeilingAt45)} (45 paths) / " +
                    "${"%.2f".format(it.perPathSecantCeilingAt15)} (15 paths)"
        )
    }
    println()
    result.reachBounds.forEach {
        println("  bound: ${it.name} = ${"%.4f".format(it.value)} nm, short by ${"%.3f".format(it.shortfall)}x")
    }
    println()
    println("  catalogue rows: ${result.catalogue.size}")
    result.catalogue.filter { it.readAtStroke == DESIRED_STROKE }.forEach {
        println(
            "    ${it.element} @ 10 nm: secant ${"%.2f".format(it.secant)}, tangent " +
                    "${"%.2f".format(it.tangent)}, per path ${"%.2f".format(it.perPathForce)} pN " +
                    "— binding: ${it.bindingConstraint}"
        )
    }
    println()
    result.catalogue.filter { it.readAtStroke == ACCEPTABLE_STROKE }.forEach {
        println(
            "    ${it.element} @ 3 nm: span ${"%.3f".format(it.elementSpan)}, tangent ${"%.2f".format(it.tangent)}, stability tangent " +
                    "${"%.2f".format(it.stabilityTangent)} — binding: ${it.bindingConstraint}"
        )
    }
    println()
    val worst = result.reproductions.maxByOrNull { it.relativeDeparture }
    println("  worst upstream reproduction: ${worst?.quantity} at ${worst?.relativeDeparture}")
    println()
    result.verdict.forEach { (key, value) -> println("  $key:\n    $value\n") }
    println("  written to $output")
}
