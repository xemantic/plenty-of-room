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
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * Task `T-65` / leaf `A8.2` — the standoff's **2 × 2** tip flexibility, solved into `C-0025`'s
 * beam instead of split into two independent springs.
 *
 * ```shell
 * tools/study.sh anchoring.CoupledStandoffJointStudyKt
 * ```
 *
 * Emits `gpd/results/T-65-coupled-standoff-joint.json`, deterministically: the file carries no
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
private const val DEAD_BAND_ALLOWED = 0.1
private const val DESIGN_LENGTH = 8.0

private val EI = Gen1Tile.DUPLEX_BENDING_RIGIDITY
private val STRETCH = Gen1Tile.DUPLEX_STRETCH_MODULUS
private val RISE = Gen1Tile.RISE_PER_BASE_PAIR
private val PER_PATH = MANDATE / PATH_COUNT

private val STANDOFF_LENGTHS = listOf(3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)

private val BASES: List<Pair<String, StandoffBase>> = listOf(
    "B0" to StandoffBase.idealClamp(),
    "B1" to StandoffBase.crossovers(1),
    "B2u" to StandoffBase.crossovers(2, favourableOrientation = false),
    "B2" to StandoffBase.crossovers(2, favourableOrientation = true),
    "B3" to StandoffBase.crossovers(3, favourableOrientation = true)
)

private val RECOMMENDED_BASE = StandoffBase.crossovers(2, favourableOrientation = true)

// ---------------------------------------------------------------------------------------------

@Serializable
data class StandoffFlexibilityRecord(
    val standoffLength: Double,
    val baseId: String,
    val baseRotationalStiffness: Double,
    val translationUnderForce: Double,
    val translationUnderMoment: Double,
    val rotationUnderForce: Double,
    val rotationUnderMoment: Double,
    val bettiDeparture: Double,
    val determinant: Double,
    val correlation: Double,
    val otherDisplacementFixedFactor: Double,
    val swayStiffnessOtherLoadZero: Double,
    val swayStiffnessRotationFixed: Double,
    val headRotationalOtherLoadZero: Double,
    val headRotationalTranslationFixed: Double,
    val reproducesC0028Sway: Boolean,
    val reproducesC0028Rotational: Boolean
)

@Serializable
data class CoupledDesignRecord(
    val baseId: String,
    val standoffLength: Double,
    val standoffBasePairs: Double,
    val model: String,
    val orientation: String,
    val span: Double,
    val spanBasePairs: Double,
    val restraint: Double,
    val bendingFactor: Double,
    val effectiveFactorAcceptable: Double,
    val effectiveFactorDesired: Double,
    val couplingFactor: Double,
    val suppliedDrawInAcceptable: Double,
    val demandedDrawInAcceptable: Double,
    val supplyToDemandAcceptable: Double,
    val effectiveStretchFraction: Double,
    val effectiveStretchFractionDecoupled: Double,
    val axialStiffeningRatio: Double,
    val secantAcceptable: Double,
    val tangentAcceptable: Double,
    val tangentToSecant: Double,
    val minimumTangent: Double,
    val minimumTangentStroke: Double,
    val secantDesired: Double,
    val axialForceAcceptable: Double,
    val axialForceDesired: Double,
    val axialSignAcceptable: String,
    val tensionReversalStroke: Double,
    val peakCompression: Double,
    val flexureBracedCritical: Double,
    val flexureCompressionMargin: Double,
    val dutyAcceptable: Double,
    val dutyDesiredMandate: Double,
    val dutyDesiredElement: Double,
    val dutyRatio: Double,
    val headRestraintRealised: Double,
    val bucklingFreeHead: Double,
    val bucklingRealisedHead: Double,
    val bucklingMarginFreeHead: Double,
    val bucklingMarginRealisedHead: Double,
    val bucklingMarginFreeHeadFields: Double,
    val bucklingStroke: Double,
    val headDeflectionDesired: Double,
    val headDeflectionRatio: Double,
    val headRotationDesired: Double,
    val strokeClearance: Double,
    val clearanceCoversDesiredStroke: Boolean,
    val clearanceCoversAcceptableStroke: Boolean,
    val supportStiffness: Double,
    val supportMargin: Double,
    val p1Supports: Boolean,
    val p2Placed: Boolean,
    val p3Compliant: Boolean,
    val p4Safe: Boolean,
    val p5Buildable: Boolean,
    val p6Stable: Boolean,
    val p7FlexureStable: Boolean,
    val allPredicatesPass: Boolean,
    val passesOnFieldsRigidity: Boolean,
    val verdict: String
)

@Serializable
data class CoupledSensitivityRecord(
    val axis: String,
    val label: String,
    val value: Double,
    val span: Double,
    val couplingFactor: Double,
    val tangentAcceptable: Double,
    val axialForceDesired: Double,
    val dutyDesiredElement: Double,
    val bucklingFreeHead: Double,
    val bucklingMarginFreeHead: Double,
    val bucklingMarginFreeHeadFields: Double,
    val allPredicatesPass: Boolean
)

@Serializable
data class CoupledConvergenceRecord(
    val quantity: String,
    val control: String,
    val level: Double,
    val value: Double,
    val departureFromFinest: Double
)

@Serializable
data class CoupledReproductionRecord(
    val quantity: String,
    val published: Double,
    val derived: Double,
    val relativeDeparture: Double,
    val source: String
)

@Serializable
data class CoupledStandoffJointResult(
    val task: String,
    val leaf: String,
    val conditions: Map<String, String>,
    val cheapBound: Map<String, Double>,
    val flexibilities: List<StandoffFlexibilityRecord>,
    val designs: List<CoupledDesignRecord>,
    val baseComparison: List<CoupledDesignRecord>,
    val sensitivities: List<CoupledSensitivityRecord>,
    val convergence: List<CoupledConvergenceRecord>,
    val reproductions: List<CoupledReproductionRecord>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val citedNumbers: List<String>
)

// ---------------------------------------------------------------------------------------------

private fun flexibilityRecord(id: String, base: StandoffBase, length: Double): StandoffFlexibilityRecord {
    val exact = standoffTipFlexibility(EI, length, base.rotationalStiffness)
    val integrated = standoffTipFlexibilityByIntegration(EI, length, base.rotationalStiffness, 1024)
    val restraint = baseRestraintParameter(base.rotationalStiffness, EI, length)
    return StandoffFlexibilityRecord(
        standoffLength = length,
        baseId = id,
        baseRotationalStiffness = finite(base.rotationalStiffness),
        translationUnderForce = exact.translationUnderForce,
        translationUnderMoment = exact.translationUnderMoment,
        rotationUnderForce = exact.rotationUnderForce,
        rotationUnderMoment = exact.rotationUnderMoment,
        bettiDeparture = abs(integrated.translationUnderMoment - integrated.rotationUnderForce) /
                integrated.translationUnderMoment,
        determinant = exact.determinant,
        correlation = exact.correlation,
        otherDisplacementFixedFactor = exact.otherDisplacementFixedFactor,
        swayStiffnessOtherLoadZero = exact.swayStiffness,
        swayStiffnessRotationFixed = exact.swayStiffnessRotationFixed,
        headRotationalOtherLoadZero = exact.headRotationalStiffness,
        headRotationalTranslationFixed = exact.headRotationalStiffnessTranslationFixed,
        reproducesC0028Sway = near(
            exact.swayStiffness, standoffSwayStiffness(EI, length, restraint), 1.0e-12
        ),
        reproducesC0028Rotational = near(
            exact.headRotationalStiffness,
            standoffHeadRotationalStiffness(EI, length, restraint), 1.0e-12
        )
    )
}

private fun designRecord(
    id: String,
    base: StandoffBase,
    length: Double,
    coupled: Boolean,
    orientation: FlexureOrientation,
    drawInModel: DrawInModel = DrawInModel.CHORD,
    bendingRigidity: Double = EI,
    stretchModulus: Double = STRETCH
): CoupledDesignRecord {
    val full = standoffTipFlexibility(bendingRigidity, length, base.rotationalStiffness)
    val flexibility = if (coupled) full else full.decoupled()
    val restraintBase = baseRestraintParameter(base.rotationalStiffness, bendingRigidity, length)
    val span = coupledFlexureSpan(
        bendingRigidity, flexibility, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE, orientation,
        stretchModulus, drawInModel
    )
    val flexure = CoupledJointFlexure(
        bendingRigidity, span, flexibility, stretchModulus, drawInModel
    )
    val decoupledFlexure = CoupledJointFlexure(
        bendingRigidity, span, full.decoupled(), stretchModulus, drawInModel
    )
    val secant = PATH_COUNT * flexure.strokeSecantStiffness(ACCEPTABLE_STROKE, orientation)
    val tangent = PATH_COUNT * flexure.strokeTangentStiffness(ACCEPTABLE_STROKE, orientation)
    // the tangent is no longer monotone in the stroke, so the minimum over 0-10 nm is scanned
    var minimumTangent = Double.MAX_VALUE
    var minimumStroke = 0.0
    for (i in 0..1000) {
        val s = i * DESIRED_STROKE / 1000.0
        val value = PATH_COUNT * flexure.strokeTangentStiffness(s, orientation)
        if (value < minimumTangent) {
            minimumTangent = value
            minimumStroke = s
        }
    }
    // the stroke at which the beam's axial force crosses back into tension, if it does
    var reversal = Double.POSITIVE_INFINITY
    if (flexure.strokeAxialForce(0.5, orientation) < 0.0) {
        for (i in 1..4000) {
            val s = i * 30.0 / 4000.0
            if (flexure.strokeAxialForce(s, orientation) >= 0.0) {
                reversal = s
                break
            }
        }
    } else {
        reversal = 0.0
    }
    val dutyElement = flexure.strokeEndShear(DESIRED_STROKE, orientation)
    val dutyMandate = MANDATE * DESIRED_STROKE / PATH_COUNT / 2.0
    val headRestraint = beamHeadRestraint(length, span, 2.0)
    val free = standoffBucklingLoad(bendingRigidity, length, restraintBase, 0.0)
    val realised = standoffBucklingLoad(bendingRigidity, length, restraintBase, headRestraint)
    val fieldsFree = free * FIELDS_BENDING_RIGIDITY / bendingRigidity
    val peak = peakFlexureCompression(flexure, orientation, DESIRED_STROKE)
    val braced = bracedColumnBucklingLoad(bendingRigidity, span, flexure.restraint)
    val support = seriesStiffness(stretchModulus / length, base.axialStiffness)
    // a geometric ceiling, reported beside the predicates and not adopted as one
    val clearance = if (orientation == FlexureOrientation.FAVOURABLE)
        favourableStrokeClearance(length) else Double.POSITIVE_INFINITY
    val tensionDesired = flexure.strokeAxialForce(DESIRED_STROKE, orientation)
    val shareDesired = MANDATE * DESIRED_STROKE / PATH_COUNT
    val p1 = base.buildable && base.transverseDeadBand <= DEAD_BAND_ALLOWED &&
            support >= SUPPORT_MARGIN_REQUIRED * PER_PATH
    val p2 = abs(secant - MANDATE) <= 1.0e-6 * MANDATE
    val p3 = tangent <= COMPLIANT_CEILING
    val p4 = tensionDesired <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE &&
            shareDesired <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE
    val p5 = base.buildable && length <= 10.0 && span <= 60.0
    val p6 = free >= dutyElement
    val p7 = peak <= braced
    val verdict = when {
        !base.buildable -> "FAIL — the motif does not exist as a 90 degree junction"
        !p1 -> "FAIL P1 — the base does not support the standoff"
        !p3 -> "FAIL P3 — tangent past the 40 pN/nm compliance ceiling"
        !p4 -> "FAIL P4 — beam tension past the 10 pN unzip allowable at 10 nm"
        !p5 -> "FAIL P5 — outside C-0017's buildable envelope"
        !p6 -> "FAIL P6 — the standoff buckles before the desired stroke"
        !p7 -> "FAIL P7 — the FLEXURE buckles under the compression the joint imposes"
        else -> "PASS"
    }
    return CoupledDesignRecord(
        baseId = id,
        standoffLength = length,
        standoffBasePairs = length / RISE,
        model = if (coupled) "coupled 2x2" else "decoupled (C-0025 / C-0028)",
        orientation = orientation.name.lowercase(),
        span = span,
        spanBasePairs = span / RISE,
        restraint = finite(flexure.restraint),
        bendingFactor = flexure.bendingFactor,
        effectiveFactorAcceptable =
            flexure.effectiveMidspanFactor(orientation.sense * ACCEPTABLE_STROKE),
        effectiveFactorDesired =
            flexure.effectiveMidspanFactor(orientation.sense * DESIRED_STROKE),
        couplingFactor = flexure.couplingFactor,
        suppliedDrawInAcceptable = flexure.couplingFactor * ACCEPTABLE_STROKE,
        demandedDrawInAcceptable = flexure.chordExtension(ACCEPTABLE_STROKE),
        supplyToDemandAcceptable = flexure.couplingFactor * ACCEPTABLE_STROKE /
                flexure.chordExtension(ACCEPTABLE_STROKE),
        effectiveStretchFraction = flexure.effectiveStretchModulus / stretchModulus,
        effectiveStretchFractionDecoupled =
            decoupledFlexure.effectiveStretchModulus / stretchModulus,
        axialStiffeningRatio =
            flexure.effectiveStretchModulus / decoupledFlexure.effectiveStretchModulus,
        secantAcceptable = secant,
        tangentAcceptable = tangent,
        tangentToSecant = tangent / secant,
        minimumTangent = minimumTangent,
        minimumTangentStroke = minimumStroke,
        secantDesired = PATH_COUNT * flexure.strokeSecantStiffness(DESIRED_STROKE, orientation),
        axialForceAcceptable = flexure.strokeAxialForce(ACCEPTABLE_STROKE, orientation),
        axialForceDesired = tensionDesired,
        axialSignAcceptable = signLabel(flexure.strokeAxialForce(ACCEPTABLE_STROKE, orientation)),
        tensionReversalStroke = finite(reversal),
        peakCompression = peak,
        flexureBracedCritical = braced,
        flexureCompressionMargin = if (peak <= 0.0) INFINITE_SENTINEL else braced / peak,
        dutyAcceptable = flexure.strokeEndShear(ACCEPTABLE_STROKE, orientation),
        dutyDesiredMandate = dutyMandate,
        dutyDesiredElement = dutyElement,
        dutyRatio = dutyElement / dutyMandate,
        headRestraintRealised = headRestraint,
        bucklingFreeHead = free,
        bucklingRealisedHead = realised,
        bucklingMarginFreeHead = free / dutyElement,
        bucklingMarginRealisedHead = realised / dutyElement,
        bucklingMarginFreeHeadFields = fieldsFree / dutyElement,
        bucklingStroke = finite(coupledBucklingStroke(flexure, orientation, free)),
        headDeflectionDesired = abs(flexure.headDrawIn(orientation.sense * DESIRED_STROKE)),
        headDeflectionRatio =
            abs(flexure.headDrawIn(orientation.sense * DESIRED_STROKE)) / length,
        headRotationDesired = abs(flexure.headRotation(orientation.sense * DESIRED_STROKE)),
        strokeClearance = finite(clearance),
        clearanceCoversDesiredStroke = clearance >= DESIRED_STROKE,
        clearanceCoversAcceptableStroke = clearance >= ACCEPTABLE_STROKE,
        supportStiffness = finite(support),
        supportMargin = finite(support / PER_PATH),
        p1Supports = p1,
        p2Placed = p2,
        p3Compliant = p3,
        p4Safe = p4,
        p5Buildable = p5,
        p6Stable = p6,
        p7FlexureStable = p7,
        allPredicatesPass = p1 && p2 && p3 && p4 && p5 && p6 && p7,
        passesOnFieldsRigidity = p1 && p2 && p3 && p4 && p5 && p7 && fieldsFree >= dutyElement,
        verdict = verdict
    )
}

private fun sensitivity(
    axis: String,
    label: String,
    value: Double,
    record: CoupledDesignRecord
): CoupledSensitivityRecord = CoupledSensitivityRecord(
    axis = axis,
    label = label,
    value = value,
    span = record.span,
    couplingFactor = record.couplingFactor,
    tangentAcceptable = record.tangentAcceptable,
    axialForceDesired = record.axialForceDesired,
    dutyDesiredElement = record.dutyDesiredElement,
    bucklingFreeHead = record.bucklingFreeHead,
    bucklingMarginFreeHead = record.bucklingMarginFreeHead,
    bucklingMarginFreeHeadFields = record.bucklingMarginFreeHeadFields,
    allPredicatesPass = record.allPredicatesPass
)

private fun reproduction(
    quantity: String,
    published: Double,
    derived: Double,
    source: String
) = CoupledReproductionRecord(
    quantity = quantity,
    published = published,
    derived = derived,
    relativeDeparture = abs(derived - published) / kotlin.math.max(1.0e-12, abs(published)),
    source = source
)

fun main() {
    // ------------------------------------------------------------------ the cheap bound
    val cheapFlexibility = standoffTipFlexibility(EI, DESIGN_LENGTH, RECOMMENDED_BASE.rotationalStiffness)
    val cheapSpan = 31.06
    val cheapFlexure = CoupledJointFlexure(EI, cheapSpan, cheapFlexibility, STRETCH)
    val cheapBound = mapOf(
        "standoffLength" to DESIGN_LENGTH,
        "spanUsed" to cheapSpan,
        "couplingFactorPhi" to cheapFlexure.couplingFactor,
        "suppliedPerEndAt3nm" to cheapFlexure.couplingFactor * ACCEPTABLE_STROKE,
        "demandedPerEndAt3nm" to cheapFlexure.chordExtension(ACCEPTABLE_STROKE),
        "supplyOverDemandAt3nm" to
                cheapFlexure.couplingFactor * ACCEPTABLE_STROKE /
                cheapFlexure.chordExtension(ACCEPTABLE_STROKE),
        "supplyOverDemandAt10nm" to
                cheapFlexure.couplingFactor * DESIRED_STROKE /
                cheapFlexure.chordExtension(DESIRED_STROKE)
    )

    // ------------------------------------------------------------------ the flexibilities
    val flexibilities = STANDOFF_LENGTHS.map { flexibilityRecord("B2", RECOMMENDED_BASE, it) } +
            BASES.filter { it.first != "B2" }.map { (id, base) ->
                flexibilityRecord(id, base, DESIGN_LENGTH)
            }

    // ------------------------------------------------------------------ the designs
    val designs = STANDOFF_LENGTHS.flatMap { length ->
        listOf(
            designRecord("B2", RECOMMENDED_BASE, length, false, FlexureOrientation.FAVOURABLE),
            designRecord("B2", RECOMMENDED_BASE, length, true, FlexureOrientation.FAVOURABLE),
            designRecord("B2", RECOMMENDED_BASE, length, true, FlexureOrientation.ADVERSE)
        )
    }

    val baseComparison = BASES.flatMap { (id, base) ->
        listOf(
            designRecord(id, base, DESIGN_LENGTH, false, FlexureOrientation.FAVOURABLE),
            designRecord(id, base, DESIGN_LENGTH, true, FlexureOrientation.FAVOURABLE),
            designRecord(id, base, DESIGN_LENGTH, true, FlexureOrientation.ADVERSE)
        )
    }

    // ------------------------------------------------------------------ the sensitivities
    val sensitivities = buildList {
        DrawInModel.entries.forEach { model ->
            add(
                sensitivity(
                    "draw-in model", "${model.name} demand (T-43's 1.13-1.20x debt)", 0.0,
                    designRecord(
                        "B2", RECOMMENDED_BASE, DESIGN_LENGTH, true,
                        FlexureOrientation.FAVOURABLE, model
                    )
                )
            )
        }
        listOf(1.0 / 32.0, 1.0 / 8.0, 1.0 / 2.0, 1.0, 8.0, 128.0).forEach { multiplier ->
            add(
                sensitivity(
                    "k_s multiplier", "C-0020's four decades", multiplier,
                    designRecord(
                        "B2", StandoffBase.crossovers(2, true, 1.0, multiplier), DESIGN_LENGTH,
                        true, FlexureOrientation.FAVOURABLE
                    )
                )
            )
        }
        listOf(0.6, 1.0, 1.2).forEach { alpha ->
            add(
                sensitivity(
                    "alpha", "Chen et al.'s own bracket", alpha,
                    designRecord(
                        "B2", StandoffBase.crossovers(2, true, alpha), DESIGN_LENGTH, true,
                        FlexureOrientation.FAVOURABLE
                    )
                )
            )
        }
        listOf(
            "CanDo model input" to EI,
            "Fields et al. measured buckling" to FIELDS_BENDING_RIGIDITY
        ).forEach { (label, rigidity) ->
            add(
                sensitivity(
                    "EI everywhere", label, rigidity,
                    designRecord(
                        "B2", StandoffBase.crossovers(2, true), DESIGN_LENGTH, true,
                        FlexureOrientation.FAVOURABLE, DrawInModel.CHORD, rigidity
                    )
                )
            )
        }
    }

    // ------------------------------------------------------------------ convergence
    val convergence = buildList {
        val exact = standoffTipFlexibility(EI, DESIGN_LENGTH, RECOMMENDED_BASE.rotationalStiffness)
        listOf(64, 128, 256, 512, 1024).forEach { steps ->
            val integrated = standoffTipFlexibilityByIntegration(
                EI, DESIGN_LENGTH, RECOMMENDED_BASE.rotationalStiffness, steps
            )
            add(
                CoupledConvergenceRecord(
                    quantity = "C11 by cumulative Simpson against the closed form",
                    control = "integration steps",
                    level = steps.toDouble(),
                    value = integrated.translationUnderForce,
                    departureFromFinest =
                        abs(integrated.translationUnderForce - exact.translationUnderForce) /
                                exact.translationUnderForce
                )
            )
        }
        val flexibility =
            standoffTipFlexibility(EI, DESIGN_LENGTH, RECOMMENDED_BASE.rotationalStiffness)
        val finest = coupledFlexureSpan(
            EI, flexibility, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE,
            FlexureOrientation.FAVOURABLE, STRETCH, DrawInModel.CHORD, 2048
        )
        listOf(64, 128, 256, 512, 2048).forEach { steps ->
            val span = coupledFlexureSpan(
                EI, flexibility, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE,
                FlexureOrientation.FAVOURABLE, STRETCH, DrawInModel.CHORD, steps
            )
            add(
                CoupledConvergenceRecord(
                    quantity = "the placed span",
                    control = "scan steps",
                    level = steps.toDouble(),
                    value = span,
                    departureFromFinest = abs(span - finest) / finest
                )
            )
        }
        val designFlexure = CoupledJointFlexure(EI, finest, flexibility, STRETCH)
        listOf(1.0e-2, 1.0e-3, 1.0e-4, 1.0e-5).forEach { h ->
            val numeric =
                (designFlexure.reaction(3.0 + h) - designFlexure.reaction(3.0 - h)) / (2.0 * h)
            add(
                CoupledConvergenceRecord(
                    quantity = "the analytic tangent against a central difference at 3 nm",
                    control = "difference step",
                    level = h,
                    value = numeric,
                    departureFromFinest =
                        abs(numeric - designFlexure.tangentStiffness(3.0)) /
                                designFlexure.tangentStiffness(3.0)
                )
            )
        }
        listOf(64, 256, 1024).forEach { steps ->
            val u = bracedColumnWavenumber(designFlexure.restraint, steps)
            add(
                CoupledConvergenceRecord(
                    quantity = "the braced column eigenvalue",
                    control = "scan steps",
                    level = steps.toDouble(),
                    value = u,
                    departureFromFinest = abs(bracedColumnDeterminant(u, designFlexure.restraint))
                )
            )
        }
    }

    // ------------------------------------------------------------------ reproductions
    val decoupledDesign =
        designs.first { it.standoffLength == DESIGN_LENGTH && it.model.startsWith("decoupled") }
    val clampedDecoupled =
        baseComparison.first { it.baseId == "B0" && it.model.startsWith("decoupled") }
    val crossoverFlexibility =
        standoffTipFlexibility(EI, DESIGN_LENGTH, StandoffBase.crossovers(1).rotationalStiffness)
    val clampFlexibility = standoffTipFlexibility(EI, DESIGN_LENGTH, Double.POSITIVE_INFINITY)
    val reproductions = listOf(
        reproduction("C-0028 B2 span at 8 nm [nm]", 31.06, decoupledDesign.span, "C-0028 design table"),
        reproduction("C-0028 B2 c at 8 nm", 91.8, decoupledDesign.bendingFactor, "C-0028 design table"),
        reproduction(
            "C-0028 B2 tangent at 8 nm [pN/nm]", 36.51, decoupledDesign.tangentAcceptable,
            "C-0028 design table"
        ),
        reproduction(
            "C-0028 B2 T(10) [pN]", 2.94, decoupledDesign.axialForceDesired, "C-0028 design table"
        ),
        reproduction(
            "C-0028 B2 duty(10) element [pN]", 5.113, decoupledDesign.dutyDesiredElement,
            "C-0028 design table, with CH-0037 applied"
        ),
        reproduction(
            "C-0028 B2 P_c free head [pN]", 7.21, decoupledDesign.bucklingFreeHead,
            "C-0028 design table"
        ),
        reproduction(
            "C-0028 B2 buckling margin at 8 nm", 1.41, decoupledDesign.bucklingMarginFreeHead,
            "C-0028 design table"
        ),
        reproduction(
            "C-0028 B2 S_eff/S at 8 nm", 0.0141, decoupledDesign.effectiveStretchFraction,
            "C-0028 nominal design"
        ),
        reproduction(
            "C-0025 J5-8 span [nm]", 31.6403748, clampedDecoupled.span, "C-0025 design table"
        ),
        reproduction("C-0025 J5-8 c", 95.6390226, clampedDecoupled.bendingFactor, "C-0025 design table"),
        reproduction(
            "C-0025 J5-8 tangent [pN/nm]", 37.3911226, clampedDecoupled.tangentAcceptable,
            "C-0025 design table"
        ),
        reproduction(
            "C-0025 J5-8 T(10) [pN]", 3.82799407, clampedDecoupled.axialForceDesired,
            "C-0025 design table"
        ),
        reproduction(
            "C-0025 pinned-head buckling at 8 nm [pN]", 8.8672227,
            clampedDecoupled.bucklingFreeHead, "C-0025 / C-0028"
        ),
        reproduction(
            "C-0028 off-diagonal correlation at a clamped base", sqrt(3.0) / 2.0,
            clampFlexibility.correlation, "C-0028 validity range"
        ),
        reproduction(
            "C-0028 off-diagonal factor at a clamped base", 4.0,
            clampFlexibility.otherDisplacementFixedFactor, "C-0028 validity range"
        ),
        reproduction(
            "C-0028 off-diagonal correlation at a crossover base", 0.947,
            crossoverFlexibility.correlation, "C-0028 validity range"
        ),
        reproduction(
            "C-0028 off-diagonal factor at a crossover base", 9.70,
            crossoverFlexibility.otherDisplacementFixedFactor, "C-0028 validity range"
        ),
        reproduction(
            "Fields et al.'s implied bending rigidity [pN nm^2]", 172.9,
            FIELDS_BENDING_RIGIDITY, "C-0028 literature table"
        ),
        reproduction(
            "the braced column's pinned-pinned effective length factor", 1.0,
            kotlin.math.PI / bracedColumnWavenumber(0.0), "textbook"
        ),
        reproduction(
            "the braced column's clamped-clamped effective length factor", 0.5,
            kotlin.math.PI / bracedColumnWavenumber(Double.POSITIVE_INFINITY), "textbook"
        )
    )

    val coupledWindow = designs.filter {
        it.model == "coupled 2x2" && it.orientation == "favourable" && it.allPredicatesPass
    }.map { it.standoffLength }
    val coupledWindowFields = designs.filter {
        it.model == "coupled 2x2" && it.orientation == "favourable" && it.passesOnFieldsRigidity
    }.map { it.standoffLength }
    val adverseWindow = designs.filter {
        it.model == "coupled 2x2" && it.orientation == "adverse" && it.allPredicatesPass
    }.map { it.standoffLength }
    val design = designs.first {
        it.standoffLength == DESIGN_LENGTH && it.model == "coupled 2x2" &&
                it.orientation == "favourable"
    }
    val adverse = designs.first {
        it.standoffLength == DESIGN_LENGTH && it.model == "coupled 2x2" &&
                it.orientation == "adverse"
    }

    val findings = mapOf(
        "the cheap bound" to ("The head's tilt under the beam's own end moment supplies " +
                "%.3f nm of draw-in per end at the 3 nm placement stroke against a chord demand " +
                "of %.3f nm — a ratio of %.2f. The term C-0025 and C-0028 dropped is not a " +
                "correction to the term they kept; it is larger than it, and it is FIRST order " +
                "in the deflection where the demand is second.").format(
            cheapBound["suppliedPerEndAt3nm"], cheapBound["demandedPerEndAt3nm"],
            cheapBound["supplyOverDemandAt3nm"]
        ),
        "the sign is a mounting choice" to ("Phi*delta is ODD and e(delta) is EVEN, so the " +
                "coupled law is signed but no longer odd. At 8 nm the favourable mounting gives " +
                "span %.2f nm, tangent %.2f pN/nm and duty %.3f pN; the adverse one gives " +
                "%.2f nm, %.2f pN/nm and %.3f pN — %.2fx the duty and %.2fx the tangent. Which " +
                "one the device sees is decided by which body carries the standoffs.").format(
            design.span, design.tangentAcceptable, design.dutyDesiredElement,
            adverse.span, adverse.tangentAcceptable, adverse.dutyDesiredElement,
            adverse.dutyDesiredElement / design.dutyDesiredElement,
            adverse.tangentAcceptable / design.tangentAcceptable
        ),
        "the buckling margin" to ("At C-0028's recommended design the free-head margin rises " +
                "from %.2fx decoupled to %.2fx coupled on CanDo's rigidity and %.2fx on Fields " +
                "et al.'s, because the duty falls from %.3f to %.3f pN. In the ADVERSE mounting " +
                "it falls to %.2fx and %.2fx. C-0028's feared 1.4x softening of the sway does " +
                "not happen: against a NET demand the coupled joint is %.2fx STIFFER, and what " +
                "moves the answer is the draw-in it supplies, not the compliance it has.").format(
            decoupledDesign.bucklingMarginFreeHead, design.bucklingMarginFreeHead,
            design.bucklingMarginFreeHeadFields, decoupledDesign.dutyDesiredElement,
            design.dutyDesiredElement, adverse.bucklingMarginFreeHead,
            adverse.bucklingMarginFreeHeadFields, design.axialStiffeningRatio
        ),
        "c is no longer a constant" to ("c_0 = 48(A+3)/A is C-0025's c(rho) to the last digit " +
                "even coupled — the off-diagonal does not change the bending COEFFICIENT. What " +
                "it adds is a term proportional to the axial force, so the EFFECTIVE end " +
                "condition becomes a function of the stroke: %.1f at 3 nm and %.1f at 10 nm " +
                "against a nominal %.1f. S_eff is worse than not-a-constant: the joint SUPPLIES " +
                "draw-in, so the effective membrane modulus is NEGATIVE below a %.2f nm " +
                "stroke.").format(
            design.effectiveFactorAcceptable, design.effectiveFactorDesired,
            design.bendingFactor, design.tensionReversalStroke
        ),
        "the window" to ("On the coupled model in the FAVOURABLE mounting the window is " +
                "%s nm on CanDo's rigidity and %s nm on Fields et al.'s — it survives and it " +
                "WIDENS from C-0028's 7-9 nm, closed below by the compliance ceiling and above " +
                "by C-0017's 10 nm envelope. In the ADVERSE mounting it is %s: P3 fails at " +
                "every length and P6 below one at the top of the range. The verdict on the " +
                "branch is therefore not 'survives' or 'closes' but 'survives conditionally on " +
                "a variable nobody had written down'.").format(
            if (coupledWindow.isEmpty()) "EMPTY" else "%.0f-%.0f".format(
                coupledWindow.min(), coupledWindow.max()
            ),
            if (coupledWindowFields.isEmpty()) "EMPTY" else "%.0f-%.0f".format(
                coupledWindowFields.min(), coupledWindowFields.max()
            ),
            if (adverseWindow.isEmpty()) "EMPTY" else "%.0f-%.0f nm".format(
                adverseWindow.min(), adverseWindow.max()
            )
        ),
        "the price of the favourable mounting" to ("The favourable sense is the one in which the " +
                "midspan sags TOWARD the body its standoff bases stand on, so the standoff length " +
                "is also a CLEARANCE: at 8 nm the midspan bottoms out after %.2f nm and at 10 nm " +
                "after %.2f nm, against C-0017's own 10 nm ceiling on the standoff. So the " +
                "favourable mounting delivers §3's ACCEPTABLE 3 nm stroke at every window length " +
                "and its DESIRED 10 nm stroke at NONE of them, while the adverse mounting has " +
                "unlimited clearance and no window. This is reported beside the predicates and " +
                "not adopted as one, because §3 does not say what the standoff-carrying body " +
                "is — a specification gap, not a modelling one.").format(
            favourableStrokeClearance(8.0), favourableStrokeClearance(10.0)
        ),
        "a new failure mode" to ("The coupled flexure carries axial COMPRESSION over " +
                "0 < s < %.2f nm, peaking at %.3f pN against its own braced Euler load of " +
                "%.2f pN — a %.2fx margin, and a predicate (P7) the decoupled reading cannot " +
                "even state, because it puts the beam in tension at every stroke.").format(
            design.tensionReversalStroke, design.peakCompression,
            design.flexureBracedCritical, design.flexureCompressionMargin
        ),
        "the element is now strain-SOFTENING" to ("tangent/secant falls from %.3f decoupled to " +
                "%.3f coupled, and the assembled tangent has an interior MINIMUM of %.2f pN/nm " +
                "at a %.2f nm stroke. C-0017's theorem says the excess of the tangent over the " +
                "secant is free stability margin; here it is a free stability DEBT of %.1f %%, " +
                "and the stability condition k_c > |k_eff| is now written on %.2f pN/nm rather " +
                "than %.2f. That is CH-0042 and it is not resolved here.").format(
            decoupledDesign.tangentToSecant, design.tangentToSecant, design.minimumTangent,
            design.minimumTangentStroke, 100.0 * (1.0 - design.tangentToSecant),
            design.minimumTangent, decoupledDesign.tangentAcceptable
        )
    )

    val result = CoupledStandoffJointResult(
        task = "T-65 — the standoff's 2x2 tip flexibility, solved into C-0025's beam",
        leaf = "A8.2 (with A1.2 for the anchoring scheme)",
        conditions = mapOf(
            "temperature" to "300 K, k_BT = 4.141947 pN nm",
            "medium" to "aqueous 2 mM MgCl2",
            "geometry" to "40 x 40 nm tile, 45 load paths on C-0015's 3 x 15 grid",
            "strokes" to "§3's acceptable 3 nm (the placement point) and desired 10 nm",
            "rigidity" to "EI = 230 pN nm^2 (CanDo MODEL INPUT) with every buckling margin " +
                    "reported ALSO on Fields et al.'s measured 172.9 pN nm^2",
            "sign convention" to "delta > 0 is the midspan moving AWAY from the plane the " +
                    "standoff bases stand on, which tilts the heads INWARD"
        ),
        cheapBound = cheapBound,
        flexibilities = flexibilities,
        designs = designs,
        baseComparison = baseComparison,
        sensitivities = sensitivities,
        convergence = convergence,
        reproductions = reproductions,
        findings = findings,
        validity = VALIDITY,
        openQuestions = OPEN_QUESTIONS,
        citedNumbers = CITED
    )

    val json = Json { prettyPrint = true; encodeDefaults = true }
    val file = File("gpd/results/T-65-coupled-standoff-joint.json")
    file.parentFile.mkdirs()
    file.writeText(json.encodeToString(json.encodeToJsonElement(result).roundedForResult()) + "\n")
    report(result, file)
}

private val VALIDITY = listOf(
    "TRL 1-3. Nothing here is measured, and the MOTIF is not demonstrated either (C-0028's " +
            "literature finding stands unchanged): no duplex has been built standing normal to a " +
            "single-layer sheet.",
    "SMALL DEFLECTION. The standoff head's rotation at §3's desired 10 nm stroke is 0.6-0.7 rad " +
            "and its translation 30-40 % of its own length, so every 10 nm column is a linear " +
            "-theory extrapolation, exactly as C-0023, C-0025 and C-0028 flag. The 3 nm columns " +
            "(head rotation 0.13 rad, translation under 4 % of the length) are inside it.",
    "The elastica correction to the head's inward translation under a pure end moment is " +
            "(1 - theta^2/12), i.e. -3.8 % at the 10 nm stroke and -0.15 % at 3 nm, so the " +
            "supplied draw-in is a slight OVERestimate at the desired stroke.",
    "The beam is solved with LINEAR bending kinematics plus C-0023's chord membrane term. The " +
            "geometric softening the compression produces is carried only through that chord " +
            "term, which puts the flexure's own critical load at 12EI/L^2 against the exact " +
            "pi^2 EI/L^2 for pinned ends — 22 % optimistic, and P7 is quoted against the exact " +
            "braced eigenvalue rather than the model's own.",
    "The two draw-in demands are still inconsistent by 1.13-1.20x (T-43). The chord demand is " +
            "used everywhere a number is quoted, because it is what produces the tension in " +
            "C-0023's own force law; the shape demand is carried as a sensitivity and it moves " +
            "the buckling margin by 10-12 %, never a verdict.",
    "The buckling head restraint is a BRACKET, not a number: free (adopted, conservative) and " +
            "the beam's own 2EI/L. C-0028's held-head reading remains unavailable, because the " +
            "sway is still the draw-in — the coupled model does not change that, it changes who " +
            "supplies it.",
    "The FAVOURABLE and ADVERSE mountings are both reported and NEITHER is asserted to be the " +
            "one §3 builds. §3 does not say which body carries the standoffs, and the two " +
            "answers differ by a whole window.",
    "k_s is C-0020's derived, unmeasured construction and the two-crossover couple rests on it " +
            "entirely. Coupled, P6 now crosses one between k_s/32 and k_s/8 rather than between " +
            "k_s/2 and k_s — the coupling BUYS about a factor of 4 in the constant T-9 would " +
            "settle, and does not remove the dependence.",
    "The sheet beyond the base crossovers is still treated as rigid (T-68), and a compliant " +
            "sheet lowers k_theta_base, which RAISES the correlation and therefore the coupling.",
    "One flexure per load path and 45 attachments, exactly as C-0023, C-0025 and C-0028 assume.",
    "The FAVOURABLE mounting's stroke clearance is computed against a solid body one interhelical " +
            "distance away. It is real if the standoff-carrying body is the 40 x 40 nm tile and it " +
            "is a design choice if it is C-0017's unspecified superstructure, so it is REPORTED " +
            "beside the predicates and not adopted as one — the same treatment C-0025 gave " +
            "buckling."
)

private val OPEN_QUESTIONS = listOf(
    "Which body carries the standoffs. It is a free choice to a builder and it is worth a whole " +
            "window; nothing upstream specifies it. T-75.",
    "Whether a strain-SOFTENING coupling still satisfies C-0017's stability condition. The " +
            "assembled tangent falls to 23-26 pN/nm against a secant placed at 33.333, and " +
            "C-0018's fold margins are 19-42 % in k_c/|k_eff|. CH-0042 / T-76.",
    "The flexure's own buckling under the compression the joint imposes, solved as a beam-column " +
            "rather than bounded by a braced eigenvalue. P7 passes here with 3-4x, but the model " +
            "carrying the compression is C-0023's chord term.",
    "A pre-bowed flexure (T-42) is now a different question: the built rise interacts with the " +
            "supplied draw-in linearly rather than quadratically.",
    "What the standoff-carrying body is, and therefore whether the favourable mounting's " +
            "clearance ceiling binds. T-78.",
    "Whether a 90 degree routing between a sheet duplex and a normal standoff exists at all " +
            "(T-67). Upstream of every number here, exactly as in C-0028."
)

private val CITED = listOf(
    "duplex EI = 230 pN nm^2 — CITED, a CanDo MODEL INPUT (Kim et al., NAR 40:2862, 2012), NOT " +
            "a measurement; every buckling margin is reported also on Fields et al.'s implied " +
            "172.9 pN nm^2",
    "duplex stretch modulus S = 1100 pN — CITED, MEASURED (Wang et al., Biophys. J. 72:1335, 1997)",
    "crossover hinge k_theta = 2*alpha*B/(100a) = 13.53 pN nm/rad — CITED, FITTED (Chen et al., " +
            "JACS 136:6995, 2014, SI S2), via C-0009; swept over alpha in [0.6, 1.2]",
    "crossover in-plane k_s = 2*alpha*S/(100a) = 64.71 pN/nm — DERIVED (C-0020), NOT measured; " +
            "swept over four decades",
    "interhelical distance 2.69 nm — CITED, MEASURED by SAXS (Fischer et al. 2016)",
    "duplex buckling at 40-41 bp under 9 pN — CITED, MEASURED (Fields, Meyer & Cohen, NAR " +
            "41:9881, 2013), used only to produce the second rigidity, never as an input",
    "C-0025's J5-8 design and C-0028's B2 design — CITED, and reproduced here as gate-5 tests",
    "§3 targets: 100 pN, 3 nm, 10 nm, 40 x 40 nm, 2 mM — CITED"
)

private fun report(result: CoupledStandoffJointResult, file: File) {
    println("T-65 — the standoff's 2 x 2 tip flexibility, solved")
    println()
    println("the cheap bound")
    result.cheapBound.forEach { (key, value) -> println("  %-24s %12.5f".format(key, value)) }
    println()
    println("flexibilities (l, base, C11, C12, C22, correlation, factor, Betti departure)")
    result.flexibilities.forEach {
        println(
            "  %5.1f %-5s %10.5f %10.5f %11.6f %8.5f %8.4f %10.2e %s%s".format(
                it.standoffLength, it.baseId, it.translationUnderForce,
                it.translationUnderMoment, it.rotationUnderMoment, it.correlation,
                it.otherDisplacementFixedFactor, it.bettiDeparture,
                if (it.reproducesC0028Sway) "sway=C-0028 " else "SWAY MISMATCH ",
                if (it.reproducesC0028Rotational) "rot=C-0028" else "ROT MISMATCH"
            )
        )
    }
    println()
    println("clearance in the favourable mounting (l, largest stroke that fits, covers 3 nm, covers 10 nm)")
    result.designs.filter { it.orientation == "favourable" && it.model == "coupled 2x2" }.forEach {
        println(
            "  %5.1f %8.2f %-6s %-6s".format(
                it.standoffLength, it.strokeClearance, it.clearanceCoversAcceptableStroke,
                it.clearanceCoversDesiredStroke
            )
        )
    }
    println()
    println("designs (l, model, orientation, span, c0, tangent, T10, duty10, Pc, margin, Fields, verdict)")
    result.designs.forEach {
        println(
            "  %5.1f %-26s %-10s %6.2f %6.1f %7.2f %+7.3f %7.3f %7.2f %6.2f %6.2f  %s".format(
                it.standoffLength, it.model, it.orientation, it.span, it.bendingFactor,
                it.tangentAcceptable, it.axialForceDesired, it.dutyDesiredElement,
                it.bucklingFreeHead, it.bucklingMarginFreeHead, it.bucklingMarginFreeHeadFields,
                it.verdict
            )
        )
    }
    println()
    println("base comparison at 8 nm")
    result.baseComparison.forEach {
        println(
            "  %-5s %-26s %-10s span=%6.2f tan=%6.2f duty=%6.3f Pc=%6.2f margin=%5.2f  %s".format(
                it.baseId, it.model, it.orientation, it.span, it.tangentAcceptable,
                it.dutyDesiredElement, it.bucklingFreeHead, it.bucklingMarginFreeHead, it.verdict
            )
        )
    }
    println()
    println("sensitivities (axis, label, value, span, Phi, tangent, duty, margin, Fields, pass)")
    result.sensitivities.forEach {
        println(
            "  %-16s %-40s %10.4f %6.2f %7.4f %6.2f %6.3f %5.2f %5.2f %s".format(
                it.axis, it.label.take(40), it.value, it.span, it.couplingFactor,
                it.tangentAcceptable, it.dutyDesiredElement, it.bucklingMarginFreeHead,
                it.bucklingMarginFreeHeadFields, it.allPredicatesPass
            )
        )
    }
    println()
    println("convergence")
    result.convergence.forEach {
        println(
            "  %-56s %-18s %10.4g %14.9f %10.2e".format(
                it.quantity.take(56), it.control, it.level, it.value, it.departureFromFinest
            )
        )
    }
    println()
    println("reproductions (published, derived, departure)")
    result.reproductions.forEach {
        println(
            "  %-56s %12.6f %12.6f %10.2e".format(
                it.quantity.take(56), it.published, it.derived, it.relativeDeparture
            )
        )
    }
    println()
    result.findings.forEach { (key, value) -> println("$key:\n  $value\n") }
    println("written to $file")
}
