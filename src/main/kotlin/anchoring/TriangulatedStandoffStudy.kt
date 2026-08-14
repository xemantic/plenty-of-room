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

package com.xemantic.nano.plentyofroom.anchoring

import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.PI
import kotlin.math.abs

/**
 * Task `T-72`, covering `T-66` / leaf `A8.2` — the triangulated standoff priced as a **stability**
 * remedy rather than a rigidity one.
 *
 * ```shell
 * tools/study.sh anchoring.TriangulatedStandoffStudyKt
 * ```
 *
 * Emits `gpd/results/T-72-triangulated-standoff.json`, deterministically: the file carries no
 * timestamp and the whole tree is rounded at the **serialisation boundary**.
 */

private const val INFINITE_SENTINEL = -1.0

private fun finite(value: Double): Double =
    if (value.isInfinite() || value.isNaN()) INFINITE_SENTINEL else value

private const val TARGET_FORCE = 100.0
private const val ACCEPTABLE_STROKE = 3.0
private const val DESIRED_STROKE = 10.0
private const val MANDATE = TARGET_FORCE / ACCEPTABLE_STROKE
private const val PATH_COUNT = 45
private const val COMPLIANT_CEILING = 40.0
private const val SUPPORT_MARGIN_REQUIRED = 10.0
private const val DESIGN_LENGTH = 8.0

private val EI = Gen1Tile.DUPLEX_BENDING_RIGIDITY
private val STRETCH = Gen1Tile.DUPLEX_STRETCH_MODULUS
private val RISE = Gen1Tile.RISE_PER_BASE_PAIR
private val INTERHELICAL = Gen1Tile.INTERHELICAL_SHEET
private val PER_PATH = MANDATE / PATH_COUNT

private val STANDOFF_LENGTHS = listOf(5.0, 6.0, 7.0, 8.0, 9.0, 10.0)

/** `C-0029`'s realisable two-link base on the hard, convention-free 180° chord. */
private val HARD_BASE = TwoLinkBase.realisable()

/** The same base read at the nominal 120° groove, which is `C-0029`'s own nominal row. */
private val NOMINAL_BASE = TwoLinkBase.realisable(DuplexBackbone())

/**
 * The lattice the leg offsets live on.
 *
 * `C-0029`'s closure puts the base chord **across** the sheet helix, so laying the chord along the
 * flexure's axis makes the flexure's axis perpendicular to the sheet helices. Then a leg offset
 * **along** the flexure axis is a step across helices — quantised at the SAXS 2.69 nm — and an
 * offset **across** it is a step along one helix, quantised at the 0.34 nm rise with a steric
 * floor of one duplex diameter (2.0 nm, i.e. 6 bp).
 */
private const val ACROSS_QUANTUM_BASE_PAIRS = 6

private val ACROSS_6BP = ACROSS_QUANTUM_BASE_PAIRS * RISE
private val ACROSS_8BP = 8 * RISE
private val ACROSS_12BP = 12 * RISE

private data class Layout(val id: String, val layout: TrussLayout, val note: String)

private val LAYOUTS: List<Layout> = listOf(
    Layout("L1", TrussLayout.single(), "C-0029's single standoff — the object every limit reduces to"),
    Layout(
        "L2a6", TrussLayout.row(2, ACROSS_6BP, PI / 2.0, "two legs across, 6 bp"),
        "two legs ACROSS the flexure axis at the steric floor, 6 bp = 2.04 nm along one sheet helix"
    ),
    Layout(
        "L2a8", TrussLayout.row(2, ACROSS_8BP, PI / 2.0, "two legs across, 8 bp"),
        "two legs ACROSS at 8 bp = 2.72 nm — one base-pair step wider than the interhelical distance"
    ),
    Layout(
        "L2a12", TrussLayout.row(2, ACROSS_12BP, PI / 2.0, "two legs across, 12 bp"),
        "two legs ACROSS at 12 bp = 4.08 nm"
    ),
    Layout(
        "L2l", TrussLayout.row(2, INTERHELICAL, 0.0, "two legs along"),
        "two legs ALONG the flexure axis on adjacent sheet duplexes, 2.69 nm — the frame lands " +
                "entirely in the LOADED plane"
    ),
    Layout(
        "L3a", TrussLayout.row(3, ACROSS_8BP, PI / 2.0, "three legs across"),
        "three legs ACROSS at 8 bp pitch, spanning 5.44 nm of one sheet helix"
    ),
    Layout(
        "L3t", TrussLayout.triangle(ACROSS_8BP, INTERHELICAL, "triangle"),
        "the FULLY triangulated head: two legs across at 8 bp and one along at 2.69 nm"
    ),
    Layout(
        "L4", TrussLayout.rectangle(INTERHELICAL, ACROSS_8BP, "rectangle"),
        "the fully triangulated head at four legs, 2.69 x 2.72 nm"
    )
)

/**
 * What ties a leg head to the cap, as a **rotational** stiffness at the head.
 *
 * The physical path is each leg head's own resistance to axial displacement relative to the cap,
 * `k_link`, which enters the frame couple as `k_link Σd_i²` — so a tie is quoted here as that
 * product and the study reports the `k_link` it came from. The nominal reading is the one
 * `C-0029`'s counting theorem forces: **a leg's head is a duplex end too**, so it also has exactly
 * two termini, and its axial stiffness is `2 k_bond,s` = 64.71 pN/nm.
 */
private data class HeadTie(val id: String, val linkStiffness: Double, val note: String)

private val HEAD_TIES: List<HeadTie> = listOf(
    HeadTie("Hr", Double.POSITIVE_INFINITY, "a rigid cap — the idealisation, reported not adopted"),
    HeadTie(
        "H2", 2.0 * bondSlideStiffness(),
        "the NOMINAL reading: the leg's head is a duplex end too, so it has two termini and " +
                "2 k_bond,s = 64.71 pN/nm of axial stiffness (C-0029's counting theorem, applied " +
                "at the other end of the same leg)"
    ),
    HeadTie("H1", bondSlideStiffness(), "one link only at each leg head — a ball joint's axial path")
)

private fun tieRotational(tie: HeadTie, secondMoment: Double): Double =
    if (tie.linkStiffness.isInfinite()) Double.POSITIVE_INFINITY
    else tie.linkStiffness * secondMoment

// ---------------------------------------------------------------------------------------------

@Serializable
data class T72LayoutRecord(
    val layoutId: String,
    val name: String,
    val legCount: Int,
    val alongSecondMoment: Double,
    val acrossSecondMoment: Double,
    val totalSecondMoment: Double,
    val minimumLegSeparation: Double,
    val stericallyRealisable: Boolean,
    val footprintArea: Double,
    val note: String
)

@Serializable
data class T72DesignRecord(
    val layoutId: String,
    val headTieId: String,
    val standoffLength: Double,
    val standoffBasePairs: Double,
    val baseReading: String,
    val orientation: String,
    val legCount: Int,
    val legAxialStiffness: Double,
    val loadedFrameCouple: Double,
    val freeFrameCouple: Double,
    val span: Double,
    val spanBasePairs: Double,
    val bendingFactor: Double,
    val couplingFactor: Double,
    val suppliedDrawInAcceptable: Double,
    val demandedDrawInAcceptable: Double,
    val supplyToDemandAcceptable: Double,
    val supplyToDemandDesired: Double,
    val secantAcceptable: Double,
    val tangentAcceptable: Double,
    val tangentToSecant: Double,
    val minimumTangent: Double,
    val axialForceAcceptable: Double,
    val axialForceDesired: Double,
    val axialSignAcceptable: String,
    val dutyDesiredElement: Double,
    val headRotationDesired: Double,
    val loadedCriticalLoad: Double,
    val freeCriticalLoad: Double,
    val criticalLoad: Double,
    val criticalLoadFields: Double,
    val governingPlane: String,
    val bucklingMargin: Double,
    val bucklingMarginFields: Double,
    val peakLegCompression: Double,
    val perLegCriticalLoad: Double,
    val perLegMargin: Double,
    val bucklingStroke: Double,
    val transverseStiffness: Double,
    val supportMargin: Double,
    val strokeClearance: Double,
    val p1Supports: Boolean,
    val p2Placed: Boolean,
    val p3Compliant: Boolean,
    val p4Safe: Boolean,
    val p5Buildable: Boolean,
    val p6Stable: Boolean,
    val p7FlexureStable: Boolean,
    val p8DrawInSurvives: Boolean,
    val p9NoLegOverloaded: Boolean,
    val allPredicatesPass: Boolean,
    val passesOnFieldsRigidity: Boolean,
    val verdict: String
)

@Serializable
data class T72AzimuthRecord(
    val azimuthDegrees: Double,
    val alongSecondMoment: Double,
    val acrossSecondMoment: Double,
    val budget: Double,
    val loadedFrameCouple: Double,
    val freeFrameCouple: Double,
    val couplingFactor: Double,
    val span: Double,
    val tangentAcceptable: Double,
    val supplyToDemandAcceptable: Double,
    val loadedCriticalLoad: Double,
    val freeCriticalLoad: Double,
    val criticalLoad: Double,
    val governingPlane: String,
    val dutyDesiredElement: Double,
    val bucklingMargin: Double,
    val allPredicatesPass: Boolean
)

@Serializable
data class T72SensitivityRecord(
    val axis: String,
    val label: String,
    val value: Double,
    val span: Double,
    val couplingFactor: Double,
    val supplyToDemandAcceptable: Double,
    val tangentAcceptable: Double,
    val dutyDesiredElement: Double,
    val criticalLoad: Double,
    val governingPlane: String,
    val bucklingMargin: Double,
    val bucklingMarginFields: Double,
    val allPredicatesPass: Boolean,
    val verdict: String
)

@Serializable
data class T72ConvergenceRecord(
    val quantity: String,
    val control: String,
    val level: Double,
    val value: Double,
    val departureFromFinest: Double
)

@Serializable
data class T72ReproductionRecord(
    val quantity: String,
    val published: Double,
    val derived: Double,
    val relativeDeparture: Double
)

@Serializable
data class T72LiteratureRecord(
    val question: String,
    val answer: String,
    val flag: String,
    val source: String
)

@Serializable
data class TriangulatedStandoffResult(
    val task: String,
    val leaf: String,
    val conditions: Map<String, String>,
    val cheapBound: Map<String, Double>,
    val layouts: List<T72LayoutRecord>,
    val designs: List<T72DesignRecord>,
    val azimuthSweep: List<T72AzimuthRecord>,
    val sensitivities: List<T72SensitivityRecord>,
    val convergence: List<T72ConvergenceRecord>,
    val reproductions: List<T72ReproductionRecord>,
    val literature: List<T72LiteratureRecord>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val citedNumbers: List<String>
)

// ---------------------------------------------------------------------------------------------

private fun design(
    layoutId: String,
    layout: TrussLayout,
    tie: HeadTie,
    length: Double,
    base: TwoLinkBase = HARD_BASE,
    baseReading: String = "hard 180 chord",
    orientation: FlexureOrientation = FlexureOrientation.FAVOURABLE,
    drawInModel: DrawInModel = DrawInModel.CHORD,
    bendingRigidity: Double = EI,
    stretchModulus: Double = STRETCH,
    fieldsRigidity: Double = FIELDS_BENDING_RIGIDITY
): T72DesignRecord {
    val head = TriangulatedStandoff(
        layout, length, base,
        headTieStiffness = tieRotational(tie, layout.totalSecondMoment),
        bendingRigidity = bendingRigidity, stretchModulus = stretchModulus
    )
    val span = coupledFlexureSpan(
        bendingRigidity, head.flexibility, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE, orientation,
        stretchModulus, drawInModel
    )
    val flexure = CoupledJointFlexure(
        bendingRigidity, span, head.flexibility, stretchModulus, drawInModel
    )
    val secant = PATH_COUNT * flexure.strokeSecantStiffness(ACCEPTABLE_STROKE, orientation)
    val tangent = PATH_COUNT * flexure.strokeTangentStiffness(ACCEPTABLE_STROKE, orientation)
    var minimumTangent = Double.MAX_VALUE
    for (i in 0..1000) {
        val value = PATH_COUNT *
                flexure.strokeTangentStiffness(i * DESIRED_STROKE / 1000.0, orientation)
        if (value < minimumTangent) minimumTangent = value
    }
    val duty = flexure.strokeEndShear(DESIRED_STROKE, orientation)
    val rotation = abs(flexure.headRotation(orientation.sense * DESIRED_STROKE))
    val peakLeg = head.peakLegCompression(duty, rotation)
    val perLegCritical = head.criticalLoad / head.legCount
    val fields = head.criticalLoad * fieldsRigidity / bendingRigidity
    val tension = flexure.strokeAxialForce(DESIRED_STROKE, orientation)
    val share = MANDATE * DESIRED_STROKE / PATH_COUNT
    val supplied = flexure.couplingFactor * ACCEPTABLE_STROKE
    val demanded = flexure.chordExtension(ACCEPTABLE_STROKE)
    val braced = bracedColumnBucklingLoad(bendingRigidity, span, flexure.restraint)
    val peakFlexure = peakFlexureCompression(flexure, orientation, DESIRED_STROKE)
    val clearance = if (orientation == FlexureOrientation.FAVOURABLE)
        favourableStrokeClearance(length) else Double.POSITIVE_INFINITY

    val p1 = head.transverseStiffness >= SUPPORT_MARGIN_REQUIRED * PER_PATH &&
            layout.stericallyRealisable
    val p2 = abs(secant - MANDATE) <= 1.0e-6 * MANDATE
    val p3 = tangent <= COMPLIANT_CEILING
    val p4 = tension <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE &&
            share <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE
    val p5 = length <= 10.0 && span <= 60.0
    val p6 = head.criticalLoad >= duty
    val p7 = peakFlexure <= braced
    val p8 = supplied >= demanded
    val p9 = peakLeg <= perLegCritical
    val verdict = when {
        !p1 -> "FAIL P1 — the legs do not support the flexure, or they overlap"
        !p3 -> "FAIL P3 — tangent past the 40 pN/nm compliance ceiling"
        !p4 -> "FAIL P4 — beam tension past the 10 pN unzip allowable at 10 nm"
        !p5 -> "FAIL P5 — outside C-0017's buildable envelope"
        !p6 -> "FAIL P6 — the truss buckles before the desired stroke"
        !p7 -> "FAIL P7 — the FLEXURE buckles under the compression the joint imposes"
        !p8 -> "FAIL P8 — the joint no longer supplies the draw-in it is charged for"
        !p9 -> "FAIL P9 — the head moment overloads one leg"
        else -> "PASS"
    }
    return T72DesignRecord(
        layoutId = layoutId,
        headTieId = tie.id,
        standoffLength = length,
        standoffBasePairs = length / RISE,
        baseReading = baseReading,
        orientation = orientation.name.lowercase(),
        legCount = head.legCount,
        legAxialStiffness = head.legAxial,
        loadedFrameCouple = head.loadedFrameCouple,
        freeFrameCouple = head.freeFrameCouple,
        span = span,
        spanBasePairs = span / RISE,
        bendingFactor = flexure.bendingFactor,
        couplingFactor = flexure.couplingFactor,
        suppliedDrawInAcceptable = supplied,
        demandedDrawInAcceptable = demanded,
        supplyToDemandAcceptable = supplied / demanded,
        supplyToDemandDesired = flexure.couplingFactor * DESIRED_STROKE /
                flexure.chordExtension(DESIRED_STROKE),
        secantAcceptable = secant,
        tangentAcceptable = tangent,
        tangentToSecant = tangent / secant,
        minimumTangent = minimumTangent,
        axialForceAcceptable = flexure.strokeAxialForce(ACCEPTABLE_STROKE, orientation),
        axialForceDesired = tension,
        axialSignAcceptable = signLabel(flexure.strokeAxialForce(ACCEPTABLE_STROKE, orientation)),
        dutyDesiredElement = duty,
        headRotationDesired = rotation,
        loadedCriticalLoad = head.loadedCriticalLoad,
        freeCriticalLoad = head.freeCriticalLoad,
        criticalLoad = head.criticalLoad,
        criticalLoadFields = fields,
        governingPlane = head.governingPlane,
        bucklingMargin = head.criticalLoad / duty,
        bucklingMarginFields = fields / duty,
        peakLegCompression = peakLeg,
        perLegCriticalLoad = perLegCritical,
        perLegMargin = perLegCritical / peakLeg,
        bucklingStroke = finite(coupledBucklingStroke(flexure, orientation, head.criticalLoad)),
        transverseStiffness = head.transverseStiffness,
        supportMargin = head.transverseStiffness / PER_PATH,
        strokeClearance = finite(clearance),
        p1Supports = p1,
        p2Placed = p2,
        p3Compliant = p3,
        p4Safe = p4,
        p5Buildable = p5,
        p6Stable = p6,
        p7FlexureStable = p7,
        p8DrawInSurvives = p8,
        p9NoLegOverloaded = p9,
        allPredicatesPass = p1 && p2 && p3 && p4 && p5 && p6 && p7 && p8 && p9,
        passesOnFieldsRigidity = p1 && p2 && p3 && p4 && p5 && p7 && p8 && fields >= duty,
        verdict = verdict
    )
}

private fun sensitivity(
    axis: String,
    label: String,
    value: Double,
    record: T72DesignRecord
): T72SensitivityRecord = T72SensitivityRecord(
    axis = axis,
    label = label,
    value = value,
    span = record.span,
    couplingFactor = record.couplingFactor,
    supplyToDemandAcceptable = record.supplyToDemandAcceptable,
    tangentAcceptable = record.tangentAcceptable,
    dutyDesiredElement = record.dutyDesiredElement,
    criticalLoad = record.criticalLoad,
    governingPlane = record.governingPlane,
    bucklingMargin = record.bucklingMargin,
    bucklingMarginFields = record.bucklingMarginFields,
    allPredicatesPass = record.allPredicatesPass,
    verdict = record.verdict
)

// ---------------------------------------------------------------------------------------------

fun main() {
    val nominalTie = HEAD_TIES.first { it.id == "H2" }
    val recommendedLayout = LAYOUTS.first { it.id == "L2a8" }

    // ------------------------------------------------------------------ the cheap bound
    val legAxial = legAxialStiffness(DESIGN_LENGTH, HARD_BASE, STRETCH)
    val budget = recommendedLayout.layout.totalSecondMoment
    val rigidCouple = trussFrameCouple(budget, legAxial)
    val tiedCouple = trussFrameCouple(
        budget, legAxial, tieRotational(nominalTie, budget)
    )
    val cheapBound = mapOf(
        "standoffLength" to DESIGN_LENGTH,
        "legAxialStiffness" to legAxial,
        "secondMomentBudget" to budget,
        "frameCoupleRigidCap" to rigidCouple,
        "frameCoupleTwoLinkCap" to tiedCouple,
        "freeAxisBondCouple" to HARD_BASE.freeAxis,
        "frameOverBondCouple" to tiedCouple / HARD_BASE.freeAxis,
        "restrainedAxisCeiling" to HARD_BASE.restrainedAxis,
        "conservationResidual" to abs(
            recommendedLayout.layout.alongSecondMoment +
                    recommendedLayout.layout.acrossSecondMoment - budget
        )
    )

    // ------------------------------------------------------------------ the layouts
    val layouts = LAYOUTS.map {
        T72LayoutRecord(
            layoutId = it.id,
            name = it.layout.name,
            legCount = it.layout.legCount,
            alongSecondMoment = it.layout.alongSecondMoment,
            acrossSecondMoment = it.layout.acrossSecondMoment,
            totalSecondMoment = it.layout.totalSecondMoment,
            minimumLegSeparation = finite(it.layout.minimumLegSeparation),
            stericallyRealisable = it.layout.stericallyRealisable,
            footprintArea = it.layout.footprintArea,
            note = it.note
        )
    }

    // ------------------------------------------------------------------ the design table
    val designs = mutableListOf<T72DesignRecord>()
    // every layout at the design length, on the nominal two-link cap
    LAYOUTS.forEach { designs += design(it.id, it.layout, nominalTie, DESIGN_LENGTH) }
    // the recommended layout over the whole standoff-length envelope
    STANDOFF_LENGTHS.forEach {
        designs += design(recommendedLayout.id, recommendedLayout.layout, nominalTie, it)
    }
    // and the single standoff over the same envelope, for the comparison the task is written on
    STANDOFF_LENGTHS.forEach {
        designs += design("L1", TrussLayout.single(), nominalTie, it)
    }
    // the head tie, which is the assumption a truss adds and the one that binds
    HEAD_TIES.forEach {
        designs += design(recommendedLayout.id, recommendedLayout.layout, it, DESIGN_LENGTH)
    }
    // the adverse mounting, on the recommended layout
    designs += design(
        recommendedLayout.id, recommendedLayout.layout, nominalTie, DESIGN_LENGTH,
        orientation = FlexureOrientation.ADVERSE
    )

    // ------------------------------------------------------------------ the azimuth trade
    val azimuthSweep = (0..12).map { i ->
        val azimuth = i * PI / 24.0
        val layout = TrussLayout.row(2, ACROSS_8BP, azimuth, "sweep")
        val record = design("sweep", layout, nominalTie, DESIGN_LENGTH)
        T72AzimuthRecord(
            azimuthDegrees = azimuth * 180.0 / PI,
            alongSecondMoment = layout.alongSecondMoment,
            acrossSecondMoment = layout.acrossSecondMoment,
            budget = layout.totalSecondMoment,
            loadedFrameCouple = record.loadedFrameCouple,
            freeFrameCouple = record.freeFrameCouple,
            couplingFactor = record.couplingFactor,
            span = record.span,
            tangentAcceptable = record.tangentAcceptable,
            supplyToDemandAcceptable = record.supplyToDemandAcceptable,
            loadedCriticalLoad = record.loadedCriticalLoad,
            freeCriticalLoad = record.freeCriticalLoad,
            criticalLoad = record.criticalLoad,
            governingPlane = record.governingPlane,
            dutyDesiredElement = record.dutyDesiredElement,
            bucklingMargin = record.bucklingMargin,
            allPredicatesPass = record.allPredicatesPass
        )
    }

    // ------------------------------------------------------------------ the sensitivities
    val nominal = design(recommendedLayout.id, recommendedLayout.layout, nominalTie, DESIGN_LENGTH)
    val sensitivities = mutableListOf<T72SensitivityRecord>()
    sensitivities += sensitivity("reference", "L2a8, H2, 8 nm, favourable, hard chord", 1.0, nominal)
    HEAD_TIES.forEach {
        sensitivities += sensitivity(
            "head tie", it.note, finite(it.linkStiffness),
            design(recommendedLayout.id, recommendedLayout.layout, it, DESIGN_LENGTH)
        )
    }
    listOf(1.0 / 32.0, 1.0 / 8.0, 1.0 / 2.0, 1.0, 2.0, 8.0).forEach { multiplier ->
        val base = TwoLinkBase.realisable(
            DuplexBackbone(minorGrooveAngle = 180.0), inPlaneMultiplier = multiplier
        )
        sensitivities += sensitivity(
            "k_s (C-0020, DERIVED and unmeasured)", "k_s x $multiplier", multiplier,
            design(
                recommendedLayout.id, recommendedLayout.layout, nominalTie, DESIGN_LENGTH,
                base = base, baseReading = "hard chord, k_s x $multiplier"
            )
        )
    }
    listOf(0.6, 1.0, 1.2).forEach { alpha ->
        sensitivities += sensitivity(
            "alpha (Chen et al.'s own bracket)", "alpha = $alpha", alpha,
            design(
                recommendedLayout.id, recommendedLayout.layout, nominalTie, DESIGN_LENGTH,
                base = TwoLinkBase.realisable(DuplexBackbone(minorGrooveAngle = 180.0), alpha = alpha),
                baseReading = "hard chord, alpha = $alpha"
            )
        )
    }
    sensitivities += sensitivity(
        "chord convention", "nominal 120 degree groove, lever arm 0.866 nm", 120.0,
        design(
            recommendedLayout.id, recommendedLayout.layout, nominalTie, DESIGN_LENGTH,
            base = NOMINAL_BASE, baseReading = "nominal 120 groove"
        )
    )
    sensitivities += sensitivity(
        "EI everywhere", "Fields et al.'s measured 172.9 pN nm^2", FIELDS_BENDING_RIGIDITY,
        design(
            recommendedLayout.id, recommendedLayout.layout, nominalTie, DESIGN_LENGTH,
            bendingRigidity = FIELDS_BENDING_RIGIDITY, fieldsRigidity = FIELDS_BENDING_RIGIDITY
        )
    )
    sensitivities += sensitivity(
        "draw-in model (T-43)", "C-0025's deflected shape instead of C-0023's chord", 2.0,
        design(
            recommendedLayout.id, recommendedLayout.layout, nominalTie, DESIGN_LENGTH,
            drawInModel = DrawInModel.SHAPE
        )
    )
    sensitivities += sensitivity(
        "mounting (T-75)", "adverse — the flexure bends toward the standoff bases", -1.0,
        design(
            recommendedLayout.id, recommendedLayout.layout, nominalTie, DESIGN_LENGTH,
            orientation = FlexureOrientation.ADVERSE
        )
    )
    listOf("L1", "L2a6", "L2a8", "L2a12", "L2l", "L3a", "L3t", "L4").forEach { id ->
        val layout = LAYOUTS.first { it.id == id }
        sensitivities += sensitivity(
            "layout", "$id — ${layout.layout.name}", layout.layout.legCount.toDouble(),
            design(id, layout.layout, nominalTie, DESIGN_LENGTH)
        )
    }

    // ------------------------------------------------------------------ convergence
    val convergence = mutableListOf<T72ConvergenceRecord>()
    val head = TriangulatedStandoff(
        recommendedLayout.layout, DESIGN_LENGTH, HARD_BASE,
        headTieStiffness = tieRotational(nominalTie, recommendedLayout.layout.totalSecondMoment)
    )
    val finestCritical = trussBucklingLoad(
        EI, DESIGN_LENGTH, HARD_BASE.freeAxis, 2, head.freeFrameCouple, scanSteps = 8192
    )
    listOf(64, 256, 1024, 4096).forEach { steps ->
        val value = trussBucklingLoad(
            EI, DESIGN_LENGTH, HARD_BASE.freeAxis, 2, head.freeFrameCouple, scanSteps = steps
        )
        convergence += T72ConvergenceRecord(
            "free-plane critical load of the recommended truss", "sway eigenvalue scan steps",
            steps.toDouble(), value, abs(value - finestCritical) / finestCritical
        )
    }
    val finestSpan = coupledFlexureSpan(
        EI, head.flexibility, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE,
        FlexureOrientation.FAVOURABLE, STRETCH, DrawInModel.CHORD, scanSteps = 4096
    )
    listOf(64, 256, 1024).forEach { steps ->
        val value = coupledFlexureSpan(
            EI, head.flexibility, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE,
            FlexureOrientation.FAVOURABLE, STRETCH, DrawInModel.CHORD, scanSteps = steps
        )
        convergence += T72ConvergenceRecord(
            "placed span of the recommended truss", "secant root scan steps",
            steps.toDouble(), value, abs(value - finestSpan) / finestSpan
        )
    }
    val closed = trussTipFlexibility(
        EI, DESIGN_LENGTH, HARD_BASE.restrainedAxis, 2, head.loadedFrameCouple
    )
    listOf(64, 256, 1024).forEach { steps ->
        val quadrature = trussTipFlexibilityByIntegration(
            EI, DESIGN_LENGTH, HARD_BASE.restrainedAxis, 2, head.loadedFrameCouple, steps
        )
        convergence += T72ConvergenceRecord(
            "assembled C11 by quadrature against the closed form", "Simpson steps",
            steps.toDouble(), quadrature.translationUnderForce,
            abs(quadrature.translationUnderForce - closed.translationUnderForce) /
                    closed.translationUnderForce
        )
    }

    // ------------------------------------------------------------------ reproductions
    val singleAt8 = TriangulatedStandoff(TrussLayout.single(), DESIGN_LENGTH, HARD_BASE)
    val c0030Head = TriangulatedStandoff(
        TrussLayout.single(), DESIGN_LENGTH, TwoLinkBase.c0028TwoCrossovers()
    )
    val c0030Span = coupledFlexureSpan(
        EI, c0030Head.flexibility, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE,
        FlexureOrientation.FAVOURABLE, STRETCH
    )
    val c0030Flexure = CoupledJointFlexure(EI, c0030Span, c0030Head.flexibility, STRETCH)
    fun reproduction(name: String, published: Double, derived: Double) = T72ReproductionRecord(
        name, published, derived, abs(derived - published) / abs(published)
    )
    val reproductions = listOf(
        reproduction("C-0029 hard base ceiling [pN nm/rad]", 78.24, HARD_BASE.restrainedAxis),
        reproduction("C-0029 nominal 120 groove ceiling", 62.06, NOMINAL_BASE.restrainedAxis),
        reproduction("C-0029 free axis = C-0028's B1", 13.53, HARD_BASE.freeAxis),
        reproduction(
            "C-0029 T1 weak-axis P_c at 5 nm [pN]", 2.46,
            TriangulatedStandoff(TrussLayout.single(), 5.0, HARD_BASE).criticalLoad
        ),
        reproduction(
            "C-0029 T1 weak-axis P_c at 7 nm [pN]", 1.69,
            TriangulatedStandoff(TrussLayout.single(), 7.0, HARD_BASE).criticalLoad
        ),
        reproduction("C-0029 T1 weak-axis P_c at 8 nm [pN]", 1.46, singleAt8.criticalLoad),
        reproduction("C-0030 B2 coupled span at 8 nm [nm]", 31.82, c0030Span),
        reproduction(
            "C-0030 B2 coupled tangent at 3 nm [pN/nm]", 25.23,
            PATH_COUNT * c0030Flexure.strokeTangentStiffness(
                ACCEPTABLE_STROKE, FlexureOrientation.FAVOURABLE
            )
        ),
        reproduction(
            "C-0030 B2 coupled duty at 10 nm [pN]", 3.313,
            c0030Flexure.strokeEndShear(DESIRED_STROKE, FlexureOrientation.FAVOURABLE)
        ),
        reproduction("C-0030 B2 free-head P_c at 8 nm [pN]", 7.21, c0030Head.loadedCriticalLoad),
        reproduction("C-0028 B2 rotational stiffness", 261.17, TwoLinkBase.c0028TwoCrossovers().restrainedAxis),
        reproduction("Fields et al.'s implied EI [pN nm^2]", 172.906, FIELDS_BENDING_RIGIDITY),
        reproduction("SAXS interhelical distance [nm]", 2.69, INTERHELICAL),
        reproduction(
            "C-0025's standoff transverse support S/l at 8 nm [pN/nm]", 137.5,
            STRETCH / DESIGN_LENGTH
        )
    )

    val findings = mutableMapOf<String, String>()
    val recommended = designs.first {
        it.layoutId == "L2a8" && it.headTieId == "H2" && it.standoffLength == DESIGN_LENGTH &&
                it.orientation == "favourable"
    }
    val single = designs.first {
        it.layoutId == "L1" && it.headTieId == "H2" && it.standoffLength == DESIGN_LENGTH
    }
    findings["the answer"] = (
            "A two-leg row laid ACROSS the flexure axis takes the adopted critical load from " +
                    "%.2f pN to %.2f pN (%.2fx) and moves the governing plane from '%s' to '%s'. " +
                    "The draw-in that survives is %.3f nm supplied against %.3f nm demanded at " +
                    "the placement stroke — a ratio of %.2f, against %.2f for the single " +
                    "standoff — so the beam is still in compression and C-0030's favourable " +
                    "mounting is intact. The tangent moves %.2f -> %.2f pN/nm against a 40 pN/nm " +
                    "ceiling, and the margin is %.2fx on CanDo's rigidity and %.2fx on Fields'."
            ).format(
            single.criticalLoad, recommended.criticalLoad,
            recommended.criticalLoad / single.criticalLoad,
            single.governingPlane, recommended.governingPlane,
            recommended.suppliedDrawInAcceptable, recommended.demandedDrawInAcceptable,
            recommended.supplyToDemandAcceptable, single.supplyToDemandAcceptable,
            single.tangentAcceptable, recommended.tangentAcceptable,
            recommended.bucklingMargin, recommended.bucklingMarginFields
        )
    val alongRow = designs.first { it.layoutId == "L2l" }
    findings["the azimuth is the whole design"] = (
            "The same two legs laid ALONG the flexure axis leave the free plane at %.2f pN — " +
                    "still governing — while destroying the draw-in supply (%.2f against %.2f) " +
                    "and taking the tangent to %.2f pN/nm. Verdict: %s. The frame couple is a " +
                    "rank-one tensor on the leg offsets, so the truss has ONE budget of it and " +
                    "the azimuth decides which plane spends it."
            ).format(
            alongRow.freeCriticalLoad, alongRow.supplyToDemandAcceptable,
            recommended.supplyToDemandAcceptable, alongRow.tangentAcceptable, alongRow.verdict
        )
    val triangle = designs.first { it.layoutId == "L3t" }
    findings["full triangulation"] = (
            "The FULLY triangulated head — two legs across and one along — reaches %.2f pN of " +
                    "critical load but supplies only %.2f of the draw-in it is charged for and " +
                    "holds a tangent of %.2f pN/nm. Verdict: %s."
            ).format(
            triangle.criticalLoad, triangle.supplyToDemandAcceptable,
            triangle.tangentAcceptable, triangle.verdict
        )
    val rigidCap = designs.first { it.layoutId == "L2a8" && it.headTieId == "Hr" }
    val oneLink = designs.first { it.layoutId == "L2a8" && it.headTieId == "H1" }
    findings["the cap is not free"] = (
            "The head tie is the assumption a truss adds, and it is not negligible: a rigid cap " +
                    "gives %.2f pN, the NOMINAL two-link cap (a leg head is a duplex end too, so " +
                    "C-0029's counting theorem applies at BOTH ends of every leg) gives %.2f pN, " +
                    "and a one-link cap gives %.2f pN with the governing plane back at '%s'. " +
                    "Verdicts: %s / %s / %s."
            ).format(
            rigidCap.criticalLoad, recommended.criticalLoad, oneLink.criticalLoad,
            oneLink.governingPlane, rigidCap.verdict, recommended.verdict, oneLink.verdict
        )
    val windowLengths = designs.filter {
        it.layoutId == "L2a8" && it.headTieId == "H2" && it.orientation == "favourable" &&
                it.standoffLength in STANDOFF_LENGTHS
    }
    val passing = windowLengths.filter { it.allPredicatesPass }.map { it.standoffLength }
    val passingFields = windowLengths.filter { it.passesOnFieldsRigidity }.map { it.standoffLength }
    findings["the window"] = (
            "On CanDo's rigidity the recommended truss passes all nine predicates at %s nm; on " +
                    "Fields et al.'s measured rigidity at %s nm. The single standoff passes at %s nm."
            ).format(
            if (passing.isEmpty()) "no length" else passing.joinToString(", "),
            if (passingFields.isEmpty()) "no length" else passingFields.joinToString(", "),
            designs.filter {
                it.layoutId == "L1" && it.headTieId == "H2" && it.standoffLength in STANDOFF_LENGTHS
            }.filter { it.allPredicatesPass }.map { it.standoffLength }
                .let { if (it.isEmpty()) "no length" else it.joinToString(", ") }
        )
    findings["the cost, in plan"] = (
            "Two legs per flexure end is %d standoffs over 45 load paths and two ends — %.0f nm² " +
                    "of duplex cross-section against the 40 x 40 nm tile's 1600 nm², i.e. %.0f %% " +
                    "of a tile footprint. This is a SCALE, not a layout: T-96 owns the plan view."
            ).format(
            2 * 2 * PATH_COUNT,
            2 * 2 * PATH_COUNT * recommendedLayout.layout.footprintArea /
                    recommendedLayout.layout.legCount,
            100.0 * 2 * 2 * PATH_COUNT * recommendedLayout.layout.footprintArea /
                    recommendedLayout.layout.legCount / 1600.0
        )

    val result = TriangulatedStandoffResult(
        task = "T-72 (covering T-66) — the triangulated standoff as a stability remedy",
        leaf = "A8.2",
        conditions = mapOf(
            "temperature" to "300 K, k_BT = 4.141947 pN nm",
            "medium" to "aqueous 2 mM MgCl2",
            "geometry" to "40 x 40 nm tile, 45 load paths on C-0015's 3 x 15 grid",
            "strokes" to "§3's acceptable 3 nm (the placement point) and desired 10 nm",
            "rigidity" to "EI = 230 pN nm^2 (CanDo MODEL INPUT), every critical load reported " +
                    "ALSO on Fields et al.'s measured 172.9 pN nm^2",
            "base" to "C-0029's two-link junction on the HARD 180 degree chord (lever arm " +
                    "r_P = 1.0 nm), restrained axis 78.24 and free axis 13.53 pN nm/rad",
            "sign convention" to "x is the flexure's own axis, so the LOADED plane is x-z and " +
                    "the FREE plane is y-z; delta > 0 is C-0030's favourable sense, T positive " +
                    "in tension"
        ),
        cheapBound = cheapBound,
        layouts = layouts,
        designs = designs,
        azimuthSweep = azimuthSweep,
        sensitivities = sensitivities,
        convergence = convergence,
        reproductions = reproductions,
        literature = LITERATURE,
        findings = findings,
        validity = VALIDITY,
        openQuestions = OPEN_QUESTIONS,
        citedNumbers = CITED
    )

    val json = Json { prettyPrint = true; encodeDefaults = true }
    val file = File("gpd/results/T-72-triangulated-standoff.json")
    file.parentFile.mkdirs()
    file.writeText(json.encodeToString(json.encodeToJsonElement(result).roundedForResult()) + "\n")
    report(result, file)
}

private val LITERATURE = listOf(
    T72LiteratureRecord(
        question = "Is a rigid out-of-plane mounting in the literature triangulated?",
        answer = "YES, and it is the only rigid one: Pumm et al.'s inclined plates \"were held " +
                "rigidly at this angle with a set of double-helical spacers\" — a SET, never one " +
                "duplex on a stiff base.",
        flag = "read directly (C-0028, re-verified in this task)",
        source = "Pumm et al., Nature 607:492 (2022)"
    ),
    T72LiteratureRecord(
        question = "Is a TWO-legged normal standoff on a single-layer sheet an established motif?",
        answer = "NOT FOUND. C-0028's and C-0029's ~110 primary-source queries found no duplex " +
                "standing normal to a single-layer sheet at all, so a fortiori no pair of them " +
                "sharing a cap. This task's truss is a model of a motif nobody has built.",
        flag = "not found",
        source = "EuropePMC, arXiv, Crossref — query families recorded in C-0029"
    ),
    T72LiteratureRecord(
        question = "What holds every published out-of-plane element on an origami body?",
        answer = "A PIN — 1-5 unpaired nt, or one base pair with flanking ssDNA. That is this " +
                "programme's B5, and it fails P1 at every length.",
        flag = "read directly (C-0028)",
        source = "Marras PNAS 112:713; Lauback Nat Commun 9:1446; Kopperger Science 359:296"
    ),
    T72LiteratureRecord(
        question = "Is the buckling model validated at this length scale?",
        answer = "YES: Fields et al. find dsDNA under 41 bp resists 9 pN and longer strands bend, " +
                "which inverts to EI = 172.9 pN nm^2 and a persistence length of 41.7 nm — " +
                "inside the measured 40-47 nm band and 25 % BELOW CanDo's model input. Every " +
                "critical load here is therefore the optimistic end, and both are reported.",
        flag = "read directly (C-0028)",
        source = "Fields, Meyer & Cohen, NAR 41:9881 (2013)"
    )
)

private val VALIDITY = listOf(
    "TRL 1-3. Nothing here is measured, and the MOTIF is not demonstrated either — C-0028's and " +
            "C-0029's literature finding stands: no duplex has been built standing normal to a " +
            "single-layer sheet, so two of them sharing a cap is a model of a model.",
    "The legs are tied ONLY at the head. They are not crossovered to each other along their " +
            "length, which would make them a 2-helix bundle and a different object. For a CROSS " +
            "row that omission is conservative — a bundle would stiffen the free plane further " +
            "and leave the loaded plane's bending unchanged, because the offset is in y.",
    "The head cap is modelled as ONE rigid body of finite rotational stiffness in series with " +
            "the legs' axial couple. Its NOMINAL value is forced by C-0029's counting theorem " +
            "applied at the other end of the leg (a leg head is a duplex end too, so 2 k_bond,s), " +
            "and the rigid reading is reported beside it and NOT adopted.",
    "The axial load is assumed shared equally between legs under a centroidal shear, which is " +
            "exact for a symmetric layout; the head MOMENT's axial share is carried separately " +
            "as P9 and is exactly zero for a cross row.",
    "The frame couple is taken to be unaffected by the axial preload. It comes from the legs' " +
            "AXIAL stiffness, which the compression does not soften to first order, but the " +
            "coupling of frame action to the sway eigenvalue is modelled as a head spring rather " +
            "than solved as a two-degree-of-freedom frame eigenproblem.",
    "SMALL DEFLECTION, exactly as C-0025, C-0028 and C-0030 flag. The 10 nm columns are linear" +
            "-theory extrapolations; the 3 nm placement point is inside small deflection.",
    "The base chord is assumed laid ALONG the flexure axis, which is the orientation that puts " +
            "the strong axis in the loaded plane. C-0029 shows the chord azimuth is quantised at " +
            "33.74 degrees per base pair and that the worst misalignment costs cos^2(16.87) = " +
            "8.4 % of the couple; that projection is NOT applied here, so the restrained-axis " +
            "numbers are the best-phase ones.",
    "k_s is C-0020's DERIVED, unmeasured construction and BOTH the base couple and the frame " +
            "couple's cap rest on it. It is swept over four decades and the verdict is reported " +
            "across them — T-9.",
    "EI = 230 pN nm^2 is a CanDo MODEL INPUT; every critical load is reported also on Fields et " +
            "al.'s implied 172.9, which is the measured end and 25 % lower.",
    "One flexure per load path and 45 attachments, exactly as C-0023, C-0025, C-0028 and C-0030 " +
            "assume. Two legs per end doubles the standoff count to 180, and whether they fit in " +
            "plan is T-96's question, not this one's.",
    "The favourable mounting's stroke clearance (l - 2.69 nm) is reported beside the predicates " +
            "and not adopted as one, exactly as C-0030 does, because §3 does not say what the " +
            "standoff-carrying body is."
)

private val OPEN_QUESTIONS = listOf(
    "Whether TWO 90 degree junctions can close on one sheet duplex 6-8 bp apart. C-0029's " +
            "closure search places ONE junction; two of them share a seat duplex and their " +
            "scaffold excursions must not collide. This is the truss's own version of T-71 and " +
            "it is the largest open item under the recommended design.",
    "The head cap as a solved body rather than a series spring. Its nominal stiffness is forced " +
            "by the counting theorem, but its geometry — what physically joins two leg heads " +
            "2.72 nm apart to one flexure duplex — is asserted, not designed.",
    "k_s, which the base couple and the cap both rest on, and which moves a verdict here as it " +
            "does in C-0028 and C-0030. T-9.",
    "Whether the plan view admits 180 standoffs and 45 flexures on a 40 x 40 nm footprint. T-96.",
    "Whether the flexure branch is preferable to E5a16, which needs no 90 degree junction at all " +
            "and therefore no standoff, no truss and no cap. C-0034 / CH-0044."
)

private val CITED = listOf(
    "duplex EI = 230 pN nm^2 — CITED, a CanDo MODEL INPUT (Kim et al., NAR 40:2862, 2012), NOT " +
            "a measurement; every critical load reported also on Fields et al.'s implied 172.9",
    "duplex stretch modulus S = 1100 pN — CITED, MEASURED (Wang et al., Biophys. J. 72:1335, 1997)",
    "phosphate radius 1.00 nm — CITED, READ DIRECTLY (Hedley et al., Phys. Rev. X 14:031042, 2024), via C-0029",
    "softened-bond constants k_bond,theta and k_bond,s — CITED+FITTED (Chen et al., JACS " +
            "136:6995, 2014) and DERIVED (C-0020) respectively; the second is NOT measured",
    "interhelical distance 2.69 nm — CITED, MEASURED by SAXS (Fischer et al. 2016)",
    "rise per base pair 0.34 nm — CITED (Douglas et al. 2009)",
    "duplex buckling at 40-41 bp under 9 pN — CITED, MEASURED (Fields, Meyer & Cohen, NAR " +
            "41:9881, 2013), used only to produce the second rigidity",
    "C-0029's ceiling and weak-axis critical loads, and C-0030's B2 design — CITED, and " +
            "reproduced here as gate-5 tests",
    "per-path allowables 10 / 65 pN — CITED via C-0006",
    "§3 targets: 100 pN, 3 nm, 10 nm, 40 x 40 nm, 2 mM — CITED"
)

private fun report(result: TriangulatedStandoffResult, file: File) {
    println("T-72 (covering T-66) — the triangulated standoff, priced as a stability remedy")
    println()
    println("the cheap bound")
    result.cheapBound.forEach { (key, value) -> println("  %-28s %14.6f".format(key, value)) }
    println()
    println("layouts (id, n, Sigma x^2, Sigma y^2, budget, min separation, steric)")
    result.layouts.forEach {
        println(
            "  %-6s %2d %8.4f %8.4f %8.4f %8.3f %s".format(
                it.layoutId, it.legCount, it.alongSecondMoment, it.acrossSecondMoment,
                it.totalSecondMoment, it.minimumLegSeparation, it.stericallyRealisable
            )
        )
    }
    println()
    println("designs (layout, tie, l, span, tangent, supply/demand, duty, Pc loaded, Pc free, Pc, plane, margin, Fields, verdict)")
    result.designs.forEach {
        println(
            "  %-6s %-3s %5.1f %6.2f %6.2f %6.2f %6.3f %7.2f %7.2f %7.2f %-7s %5.2f %5.2f  %s".format(
                it.layoutId, it.headTieId, it.standoffLength, it.span, it.tangentAcceptable,
                it.supplyToDemandAcceptable, it.dutyDesiredElement, it.loadedCriticalLoad,
                it.freeCriticalLoad, it.criticalLoad, it.governingPlane, it.bucklingMargin,
                it.bucklingMarginFields, it.verdict
            )
        )
    }
    println()
    println("azimuth sweep (deg, Sigma x^2, Sigma y^2, Phi, tangent, supply/demand, Pc loaded, Pc free, plane, margin, pass)")
    result.azimuthSweep.forEach {
        println(
            "  %6.1f %8.4f %8.4f %7.4f %6.2f %6.2f %7.2f %7.2f %-7s %5.2f %s".format(
                it.azimuthDegrees, it.alongSecondMoment, it.acrossSecondMoment,
                it.couplingFactor, it.tangentAcceptable, it.supplyToDemandAcceptable,
                it.loadedCriticalLoad, it.freeCriticalLoad, it.governingPlane,
                it.bucklingMargin, it.allPredicatesPass
            )
        )
    }
    println()
    println("sensitivities (axis, label, span, Phi, supply/demand, tangent, Pc, plane, margin, Fields, pass)")
    result.sensitivities.forEach {
        println(
            "  %-34s %-44s %6.2f %7.4f %6.2f %6.2f %7.2f %-7s %5.2f %5.2f %s".format(
                it.axis.take(34), it.label.take(44), it.span, it.couplingFactor,
                it.supplyToDemandAcceptable, it.tangentAcceptable, it.criticalLoad,
                it.governingPlane, it.bucklingMargin, it.bucklingMarginFields,
                it.allPredicatesPass
            )
        )
    }
    println()
    println("convergence")
    result.convergence.forEach {
        println(
            "  %-52s %-28s %8.0f %14.9f %10.2e".format(
                it.quantity.take(52), it.control, it.level, it.value, it.departureFromFinest
            )
        )
    }
    println()
    println("reproductions (published, derived, departure)")
    result.reproductions.forEach {
        println(
            "  %-52s %12.6f %12.6f %10.2e".format(
                it.quantity.take(52), it.published, it.derived, it.relativeDeparture
            )
        )
    }
    println()
    result.findings.forEach { (key, value) -> println("$key:\n  $value\n") }
    println("written to $file")
}
