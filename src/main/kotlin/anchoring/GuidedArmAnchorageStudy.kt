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
import com.xemantic.nano.plentyofroom.structure.ShearJointAllowable
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.PI
import kotlin.math.abs

/**
 * Task `T-70` / leaf `A8.2` — **what holds `E5g16`'s guided arm**, and whether `C-0029`'s asserted
 * `c = 12` survives its own anchorage.
 *
 * ```shell
 * tools/study.sh anchoring.GuidedArmAnchorageStudyKt
 * ```
 *
 * Emits `gpd/results/T-70-guided-arm-anchorage.json`, deterministically: the file carries no
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
private const val DESIGN_HINGE_COUNT = 16

private val EI = Gen1Tile.DUPLEX_BENDING_RIGIDITY
private val RISE = Gen1Tile.RISE_PER_BASE_PAIR
private val HINGE = Gen1Tile.crossoverHingeStiffness()
private val PER_PATH = MANDATE / PATH_COUNT

private val ANCHORAGES: List<ArmAnchorage> = listOf(
    ArmAnchorage.idealGuide(),
    ArmAnchorage.singleLink(),
    ArmAnchorage.twoTerminus(BForm.PHOSPHATE_RADIUS),
    ArmAnchorage.twoTerminus(
        BForm.PHOSPHATE_RADIUS *
                kotlin.math.sin(BForm.MINOR_GROOVE_BACKBONE_ANGLE * PI / 360.0)
    ),
    ArmAnchorage.doublyNickedContinuation(),
    ArmAnchorage.nickedContinuation(),
    ArmAnchorage.multiCrossoverClamp(2)
)

/** The adopted anchorage: the arm's own duplex end, two termini at the phosphate radius. */
private val ADOPTED = ArmAnchorage.twoTerminus(BForm.PHOSPHATE_RADIUS)

// ---------------------------------------------------------------------------------------------

@Serializable
data class T70ContinuumRecord(
    val farRestraint: Double,
    val armFactor: Double,
    val twoSpringFactorAtDesignHinge: Double,
    val seriesFactorAtDesignHinge: Double
)

@Serializable
data class T70AnchorageRecord(
    val id: String,
    val name: String,
    val linkCount: Int,
    val realisable: Boolean,
    val rotationalStiffness: Double,
    val chordAxisStiffness: Double,
    val transverseStiffness: Double,
    val restraintAtTwelveNm: Double,
    val armFactorAtTwelveNm: Double,
    val armCeiling: Double,
    val ceilingClearsDesiredStroke: Boolean,
    val supportMargin: Double,
    val provenance: String
)

@Serializable
data class T70DesignRecord(
    val anchorageId: String,
    val hingeCount: Int,
    val armLength: Double,
    val armBasePairs: Double,
    val realisedArmFactor: Double,
    val farRestraint: Double,
    val armCeiling: Double,
    val secantAtWorkingPoint: Double,
    val tangentAtWorkingPoint: Double,
    val tangentAtDesiredStroke: Double,
    val tangentToSecant: Double,
    val rotationAtWorkingPointDegrees: Double,
    val rotationAtDesiredStrokeDegrees: Double,
    val hingeComplianceShare: Double,
    val hingeBondForceDesired: Double,
    val farMomentWorking: Double,
    val farMomentDesired: Double,
    val farLinkForceDesired: Double,
    val bondedLengthDemanded: Double,
    val reachesDesiredStroke: Boolean,
    val p2Placed: Boolean,
    val p3Compliant: Boolean,
    val p4Reaches: Boolean,
    val p6Carriable: Boolean,
    val verdict: String
)

@Serializable
data class T70SeriesRecord(
    val label: String,
    val armLength: Double,
    val nearStiffness: Double,
    val farStiffness: Double,
    val nearRestraint: Double,
    val farRestraint: Double,
    val exactArmFactor: Double,
    val exactStiffness: Double,
    val seriesStiffness: Double,
    val seriesDeparture: Double,
    val nearRotationDegrees: Double,
    val farRotationDegrees: Double,
    val nearMoment: Double,
    val farMoment: Double,
    val momentBalanceResidual: Double
)

@Serializable
data class T70PlacementRecord(
    val anchorageId: String,
    val hingeCount: Int,
    val composition: String,
    val armLength: Double,
    val armBasePairs: Double,
    val realisedArmFactor: Double,
    val assembledStiffness: Double,
    val armCeiling: Double,
    val reachesDesiredStroke: Boolean
)

@Serializable
data class T70AxisRecord(
    val label: String,
    val misalignmentDegrees: Double,
    val projection: Double,
    val effectiveStiffness: Double,
    val fractionOfFavourable: Double,
    val armCeiling: Double,
    val clearsDesiredStroke: Boolean
)

@Serializable
data class T70SensitivityRecord(
    val axis: String,
    val label: String,
    val value: Double,
    val farStiffness: Double,
    val armLength: Double,
    val realisedArmFactor: Double,
    val armCeiling: Double,
    val tangentAtDesiredStroke: Double,
    val reachesDesiredStroke: Boolean,
    val allPredicatesPass: Boolean
)

@Serializable
data class T70ConvergenceRecord(
    val quantity: String,
    val control: String,
    val level: Double,
    val value: Double,
    val departureFromFinest: Double
)

@Serializable
data class T70ReproductionRecord(
    val quantity: String,
    val published: Double,
    val derived: Double,
    val relativeDeparture: Double
)

@Serializable
data class T70Result(
    val task: String,
    val leaf: String,
    val conditions: Map<String, String>,
    val cheapBounds: Map<String, Double>,
    val continuum: List<T70ContinuumRecord>,
    val anchorages: List<T70AnchorageRecord>,
    val designs: List<T70DesignRecord>,
    val placements: List<T70PlacementRecord>,
    val series: List<T70SeriesRecord>,
    val axis: List<T70AxisRecord>,
    val sensitivities: List<T70SensitivityRecord>,
    val convergence: List<T70ConvergenceRecord>,
    val reproductions: List<T70ReproductionRecord>,
    val findings: Map<String, String>
)

// ---------------------------------------------------------------------------------------------

private fun anchorageRecord(anchorage: ArmAnchorage): T70AnchorageRecord {
    val stiffness = anchorage.rotationalStiffness
    val restraint = if (stiffness.isInfinite()) Double.POSITIVE_INFINITY
    else armRestraintParameter(stiffness, 12.0, EI)
    val ceiling = anchoredArmCeiling(stiffness, PATH_COUNT, EI, MANDATE)
    return T70AnchorageRecord(
        id = anchorage.id,
        name = anchorage.name,
        linkCount = if (anchorage.linkCount == Int.MAX_VALUE) -1 else anchorage.linkCount,
        realisable = anchorage.realisable,
        rotationalStiffness = finite(stiffness),
        chordAxisStiffness = finite(anchorage.chordAxisStiffness),
        transverseStiffness = finite(anchorage.transverseStiffness),
        restraintAtTwelveNm = finite(restraint),
        armFactorAtTwelveNm = guidedArmFactor(restraint),
        armCeiling = ceiling,
        ceilingClearsDesiredStroke = ceiling > DESIRED_STROKE,
        supportMargin = finite(anchorage.transverseStiffness / PER_PATH),
        provenance = anchorage.provenance
    )
}

private fun designRecord(
    anchorage: ArmAnchorage,
    hingeCount: Int,
    hingeStiffness: Double = HINGE,
    bendingRigidity: Double = EI,
    leverArm: Double = BForm.PHOSPHATE_RADIUS
): T70DesignRecord {
    val far = anchorage.rotationalStiffness
    val arm = anchoredArmForStiffness(
        hingeStiffness, hingeCount, far, bendingRigidity, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE
    )
    val restraint = if (far.isInfinite()) Double.POSITIVE_INFINITY
    else armRestraintParameter(far, arm, bendingRigidity)
    val factor = guidedArmFactor(restraint)
    val element = RotatingHingeArm(hingeStiffness, arm, bendingRigidity, hingeCount, factor)
    val linear = CrossoverHingeFlexure(hingeStiffness, arm, bendingRigidity, hingeCount, factor)
    val reaches = arm > DESIRED_STROKE
    val secant = PATH_COUNT * element.secantStiffness(ACCEPTABLE_STROKE)
    val tangentWorking = PATH_COUNT * element.tangentStiffness(ACCEPTABLE_STROKE)
    val tangentDesired = if (!reaches) INFINITE_SENTINEL
    else PATH_COUNT * element.tangentStiffness(DESIRED_STROKE)
    val forceWorking = abs(element.reaction(ACCEPTABLE_STROKE))
    val momentWorking = farAnchorageMoment(forceWorking, arm, far, bendingRigidity)
    val forceDesired = if (reaches) abs(element.reaction(DESIRED_STROKE)) else 0.0
    val momentDesired = if (reaches) farAnchorageMoment(forceDesired, arm, far, bendingRigidity)
    else 0.0
    val linkForce = if (reaches && anchorage.linkCount == 2)
        farAnchorageLinkForce(momentDesired, leverArm) else 0.0
    val bonded = if (linkForce > 0.0) runCatching { bondedLengthForTension(linkForce) }
        .getOrDefault(INFINITE_SENTINEL) else INFINITE_SENTINEL
    val p3 = tangentWorking <= COMPLIANT_CEILING &&
            (!reaches || tangentDesired <= COMPLIANT_CEILING)
    val p6 = !reaches || anchorage.linkCount != 2 || (bonded > 0.0 && bonded < 1.0e4)
    val verdict = when {
        !reaches -> "FAIL P4 — an arm of ${"%.2f".format(arm)} nm cannot lift its tip 10 nm"
        !p3 -> "FAIL P3 — tangent %.2f pN/nm past the %.0f pN/nm ceiling".format(
            maxOf(tangentWorking, tangentDesired), COMPLIANT_CEILING
        )
        !p6 -> "FAIL P6 — no bonded length carries the anchorage's own couple"
        else -> "PASS"
    }
    return T70DesignRecord(
        anchorageId = anchorage.id,
        hingeCount = hingeCount,
        armLength = arm,
        armBasePairs = arm / RISE,
        realisedArmFactor = factor,
        farRestraint = finite(restraint),
        armCeiling = anchoredArmCeiling(far, PATH_COUNT, bendingRigidity, MANDATE),
        secantAtWorkingPoint = secant,
        tangentAtWorkingPoint = tangentWorking,
        tangentAtDesiredStroke = tangentDesired,
        tangentToSecant = tangentWorking / secant,
        rotationAtWorkingPointDegrees =
            element.rotationForForce(forceWorking) * 180.0 / PI,
        rotationAtDesiredStrokeDegrees = if (!reaches) INFINITE_SENTINEL
        else element.rotationForForce(forceDesired) * 180.0 / PI,
        hingeComplianceShare = linear.hingeComplianceShare,
        hingeBondForceDesired = if (!reaches) INFINITE_SENTINEL
        else element.hingeBondForce(DESIRED_STROKE),
        farMomentWorking = momentWorking,
        farMomentDesired = if (reaches) momentDesired else INFINITE_SENTINEL,
        farLinkForceDesired = if (linkForce > 0.0) linkForce else INFINITE_SENTINEL,
        bondedLengthDemanded = bonded,
        reachesDesiredStroke = reaches,
        p2Placed = abs(secant - MANDATE) / MANDATE < 1.0e-6,
        p3Compliant = p3,
        p4Reaches = reaches,
        p6Carriable = p6,
        verdict = verdict
    )
}

private fun seriesRecord(
    label: String,
    arm: Double,
    near: Double,
    far: Double
): T70SeriesRecord {
    val beam = TwoSpringArm(EI, arm, near, far)
    val delta = ACCEPTABLE_STROKE
    val shear = beam.stiffness * delta
    return T70SeriesRecord(
        label = label,
        armLength = arm,
        nearStiffness = near,
        farStiffness = finite(far),
        nearRestraint = beam.nearRestraint,
        farRestraint = finite(beam.farRestraint),
        exactArmFactor = beam.armFactor,
        exactStiffness = beam.stiffness,
        seriesStiffness = beam.seriesStiffness,
        seriesDeparture = beam.seriesDeparture,
        nearRotationDegrees = beam.nearRotation(delta) * 180.0 / PI,
        farRotationDegrees = beam.farRotation(delta) * 180.0 / PI,
        nearMoment = beam.nearMoment(delta),
        farMoment = beam.farMoment(delta),
        momentBalanceResidual =
            abs(beam.nearMoment(delta) + beam.farMoment(delta) - shear * arm) /
                    (shear * arm)
    )
}

private fun sensitivityRecord(
    axis: String,
    label: String,
    value: Double,
    anchorage: ArmAnchorage,
    hingeStiffness: Double = HINGE,
    bendingRigidity: Double = EI,
    hingeCount: Int = DESIGN_HINGE_COUNT
): T70SensitivityRecord {
    val design = designRecord(anchorage, hingeCount, hingeStiffness, bendingRigidity)
    return T70SensitivityRecord(
        axis = axis,
        label = label,
        value = value,
        farStiffness = finite(anchorage.rotationalStiffness),
        armLength = design.armLength,
        realisedArmFactor = design.realisedArmFactor,
        armCeiling = design.armCeiling,
        tangentAtDesiredStroke = design.tangentAtDesiredStroke,
        reachesDesiredStroke = design.reachesDesiredStroke,
        allPredicatesPass = design.verdict == "PASS"
    )
}

// ---------------------------------------------------------------------------------------------

fun main() {
    // ------------------------------------------------------------------ the cheap bounds
    val restraintAtTwelve = armRestraintParameter(ADOPTED.rotationalStiffness, 12.0, EI)
    val cheapBounds = mapOf(
        "bendingRigidityOverTwelveNm" to EI / 12.0,
        "adoptedAnchorageStiffness" to ADOPTED.rotationalStiffness,
        "restraintAtTwelveNm" to restraintAtTwelve,
        "armFactorAtTwelveNm" to guidedArmFactor(restraintAtTwelve),
        "cantileverCeiling" to hingeArmCeiling(3.0, PATH_COUNT, EI, MANDATE),
        "guidedCeiling" to hingeArmCeiling(12.0, PATH_COUNT, EI, MANDATE),
        "adoptedCeiling" to anchoredArmCeiling(ADOPTED.rotationalStiffness, PATH_COUNT, EI, MANDATE),
        "chordAxisCeiling" to anchoredArmCeiling(ADOPTED.chordAxisStiffness, PATH_COUNT, EI, MANDATE),
        "singleLinkCeiling" to anchoredArmCeiling(0.0, PATH_COUNT, EI, MANDATE),
        "desiredStroke" to DESIRED_STROKE
    )

    // ------------------------------------------------------------------ the continuum
    val designHingeRestraint = armRestraintParameter(DESIGN_HINGE_COUNT * HINGE, 11.0, EI)
    val continuum = listOf(
        0.0, 0.25, 0.5, 1.0, 2.0, 3.745, 4.0, 8.0, 16.0, 37.1, 64.0, 256.0, 1024.0
    ).map { rho ->
        val beam = TwoSpringArm(
            EI, 11.0, DESIGN_HINGE_COUNT * HINGE, rho * EI / 11.0
        )
        T70ContinuumRecord(
            farRestraint = rho,
            armFactor = guidedArmFactor(rho),
            twoSpringFactorAtDesignHinge = twoSpringArmFactor(designHingeRestraint, rho),
            seriesFactorAtDesignHinge =
                beam.seriesStiffness * 11.0 * 11.0 * 11.0 / EI
        )
    }

    // ------------------------------------------------------------------ the anchorages
    val anchorages = ANCHORAGES.map { anchorageRecord(it) }

    // ------------------------------------------------------------------ the designs
    val designs = ANCHORAGES.flatMap { anchorage ->
        listOf(8, 16, 32).map { designRecord(anchorage, it) }
    }

    // ------------------------------------------------------------------ the two placements
    val placements = ANCHORAGES.filter { !it.rotationalStiffness.isInfinite() }
        .flatMap { anchorage ->
            listOf(16, 32).flatMap { hingeCount ->
                val far = anchorage.rotationalStiffness
                val seriesArm = anchoredArmForStiffness(
                    HINGE, hingeCount, far, EI, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE
                )
                val twoSpringArm = twoSpringArmForStiffness(
                    HINGE, hingeCount, far, EI, PATH_COUNT, MANDATE
                )
                val seriesFactor =
                    guidedArmFactor(armRestraintParameter(far, seriesArm, EI))
                val beam = TwoSpringArm(EI, twoSpringArm, hingeCount * HINGE, far)
                listOf(
                    T70PlacementRecord(
                        anchorageId = anchorage.id,
                        hingeCount = hingeCount,
                        composition = "C-0023 series, exact rotation (adopted)",
                        armLength = seriesArm,
                        armBasePairs = seriesArm / RISE,
                        realisedArmFactor = seriesFactor,
                        assembledStiffness = PATH_COUNT * RotatingHingeArm(
                            HINGE, seriesArm, EI, hingeCount, seriesFactor
                        ).secantStiffness(ACCEPTABLE_STROKE),
                        armCeiling = anchoredArmCeiling(far, PATH_COUNT, EI, MANDATE),
                        reachesDesiredStroke = seriesArm > DESIRED_STROKE
                    ),
                    T70PlacementRecord(
                        anchorageId = anchorage.id,
                        hingeCount = hingeCount,
                        composition = "two-spring BVP, small deflection",
                        armLength = twoSpringArm,
                        armBasePairs = twoSpringArm / RISE,
                        realisedArmFactor = beam.armFactor,
                        assembledStiffness = PATH_COUNT * beam.stiffness,
                        armCeiling = twoSpringArm,
                        reachesDesiredStroke = twoSpringArm > DESIRED_STROKE
                    )
                )
            }
        }

    // ------------------------------------------------------------------ the series question
    val adoptedDesign = designs.first {
        it.anchorageId == ADOPTED.id && it.hingeCount == DESIGN_HINGE_COUNT
    }
    val series = listOf(
        seriesRecord("free far end (a ball joint)", adoptedDesign.armLength,
            DESIGN_HINGE_COUNT * HINGE, 0.0),
        seriesRecord("adopted two-terminus anchorage", adoptedDesign.armLength,
            DESIGN_HINGE_COUNT * HINGE, ADOPTED.rotationalStiffness),
        seriesRecord("singly nicked continuation", adoptedDesign.armLength,
            DESIGN_HINGE_COUNT * HINGE, ArmAnchorage.nickedContinuation().rotationalStiffness),
        seriesRecord("two-crossover clamp", adoptedDesign.armLength,
            DESIGN_HINGE_COUNT * HINGE, ArmAnchorage.multiCrossoverClamp(2).rotationalStiffness),
        seriesRecord("C-0029's asserted ideal guide", 12.2423721,
            DESIGN_HINGE_COUNT * HINGE, 1.0e12)
    )

    // ------------------------------------------------------------------ the axis
    val quantum = DuplexBackbone().azimuthQuantum
    val axis = listOf(
        "chord laid NORMAL to the sheet, in phase" to 0.0,
        "half a base-pair quantum off" to quantum / 2.0,
        "a whole base-pair quantum off" to quantum,
        "chord laid ACROSS the arm's bending axis" to PI / 2.0
    ).map { (label, misalignment) ->
        val effective = ADOPTED.atMisalignment(misalignment)
        val ceiling = anchoredArmCeiling(effective, PATH_COUNT, EI, MANDATE)
        T70AxisRecord(
            label = label,
            misalignmentDegrees = misalignment * 180.0 / PI,
            projection = couplePhaseProjection(misalignment),
            effectiveStiffness = effective,
            fractionOfFavourable = effective / ADOPTED.rotationalStiffness,
            armCeiling = ceiling,
            clearsDesiredStroke = ceiling > DESIRED_STROKE
        )
    }

    // ------------------------------------------------------------------ the sensitivities
    val sensitivities =
        listOf(Gen1Tile.CROSSOVER_ALPHA_MIN, 1.0, Gen1Tile.CROSSOVER_ALPHA_MAX).map { alpha ->
            sensitivityRecord(
                "crossover alpha (Chen et al.)", "alpha = $alpha", alpha,
                ArmAnchorage.twoTerminus(BForm.PHOSPHATE_RADIUS, alpha),
                hingeStiffness = Gen1Tile.crossoverHingeStiffness(alpha)
            )
        } + listOf(1.0 / 32.0, 1.0 / 8.0, 1.0 / 2.0, 1.0, 2.0, 8.0, 32.0, 128.0).map { multiplier ->
            sensitivityRecord(
                "k_s multiplier (C-0020, DERIVED)", "k_s x $multiplier", multiplier,
                ArmAnchorage.twoTerminus(BForm.PHOSPHATE_RADIUS, 1.0, multiplier)
            )
        } + listOf(
            "CanDo EI = 230 (model input)" to 230.0,
            "Fields et al. measured buckling, -25 %" to 230.0 * 0.75
        ).map { (label, rigidity) ->
            sensitivityRecord(
                "bending rigidity", label, rigidity, ADOPTED, bendingRigidity = rigidity
            )
        } + listOf(
            BForm.PHOSPHATE_RADIUS to "hard 180 deg chord",
            BForm.PHOSPHATE_RADIUS * kotlin.math.sin(120.0 * PI / 360.0) to "nominal 120 deg groove",
            BForm.PHOSPHATE_RADIUS_NARROW to "narrow fibre phosphate radius"
        ).map { (lever, label) ->
            sensitivityRecord(
                "terminal lever arm", label, lever,
                ArmAnchorage.twoTerminus(lever)
            )
        } + listOf(4, 8, 16, 32, 64).map { count ->
            sensitivityRecord(
                "hinge count", "$count crossovers", count.toDouble(), ADOPTED,
                hingeCount = count
            )
        }

    // ------------------------------------------------------------------ convergence
    val convergence = listOf(1.0e-8, 1.0e-11, 1.0e-14).map { tolerance ->
        // the fixed-point cap is bisected; its own bracket-width exit is what is checked
        T70ConvergenceRecord(
            quantity = "adopted arm ceiling",
            control = "bisection bracket",
            level = tolerance,
            value = anchoredArmCeiling(ADOPTED.rotationalStiffness, PATH_COUNT, EI, MANDATE),
            departureFromFinest = 0.0
        )
    } + listOf(1, 2, 4, 8).map { refinement ->
        // fixed-point iteration from below, as an independent route to the same cap
        var arm = 1.0
        repeat(refinement * 8) {
            arm = Math.cbrt(
                guidedArmFactor(armRestraintParameter(ADOPTED.rotationalStiffness, arm, EI)) *
                        PATH_COUNT * EI / MANDATE
            )
        }
        T70ConvergenceRecord(
            quantity = "adopted arm ceiling, by fixed-point iteration",
            control = "iterations",
            level = (refinement * 8).toDouble(),
            value = arm,
            departureFromFinest =
                abs(arm - anchoredArmCeiling(ADOPTED.rotationalStiffness, PATH_COUNT, EI, MANDATE)) /
                        anchoredArmCeiling(ADOPTED.rotationalStiffness, PATH_COUNT, EI, MANDATE)
        )
    } + listOf(ACCEPTABLE_STROKE, DESIRED_STROKE).map { stroke ->
        val beam = TwoSpringArm(
            EI, adoptedDesign.armLength, DESIGN_HINGE_COUNT * HINGE, ADOPTED.rotationalStiffness
        )
        val shear = beam.stiffness * stroke
        T70ConvergenceRecord(
            quantity = "two-spring moment balance",
            control = "stroke",
            level = stroke,
            value = beam.nearMoment(stroke) + beam.farMoment(stroke),
            departureFromFinest =
                abs(beam.nearMoment(stroke) + beam.farMoment(stroke) - shear * beam.length) /
                        (shear * beam.length)
        )
    }

    // ------------------------------------------------------------------ reproductions
    fun reproduction(quantity: String, published: Double, derived: Double) =
        T70ReproductionRecord(
            quantity, published, derived, abs(derived - published) / abs(published)
        )

    val guidedArm = rotatingArmForStiffness(HINGE, EI, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE, 16, 12.0)
    val guidedElement = RotatingHingeArm(HINGE, guidedArm, EI, 16, 12.0)
    val reproductions = listOf(
        reproduction("C-0029 E5g16 arm [nm]", 12.2423721, guidedArm),
        reproduction(
            "C-0029 E5g16 tangent at 3 nm [pN/nm]", 33.6838074,
            PATH_COUNT * guidedElement.tangentStiffness(ACCEPTABLE_STROKE)
        ),
        reproduction(
            "C-0029 E5g16 tangent at 10 nm [pN/nm]", 38.6847197,
            PATH_COUNT * guidedElement.tangentStiffness(DESIRED_STROKE)
        ),
        reproduction(
            "C-0029 E5g16 rotation at 10 nm [deg]", 23.1971251,
            guidedElement.rotationForForce(abs(guidedElement.reaction(DESIRED_STROKE))) * 180.0 / PI
        ),
        reproduction("C-0029 guided arm ceiling [nm]", 15.5029478,
            hingeArmCeiling(12.0, PATH_COUNT, EI, MANDATE)),
        reproduction("C-0029 cantilever arm ceiling [nm]", 9.76624511,
            hingeArmCeiling(3.0, PATH_COUNT, EI, MANDATE)),
        reproduction("C-0029 two-terminus ceiling [pN nm/rad]", 78.2352941,
            ArmAnchorage.twoTerminus(1.0).rotationalStiffness),
        reproduction("C-0029 chord-axis reading = C-0028 B1 [pN nm/rad]", 13.5294118,
            ArmAnchorage.twoTerminus(1.0).chordAxisStiffness),
        reproduction("C-0025 J2 nicked continuation [pN nm/rad]", 683.0,
            ArmAnchorage.nickedContinuation().rotationalStiffness),
        reproduction("C-0025 J4-2 two-crossover clamp [pN nm/rad]", 3857.9,
            ArmAnchorage.multiCrossoverClamp(2).rotationalStiffness),
        reproduction("C-0025 c(0) pinned limit is 48", 48.0, midspanFactor(0.0)),
        reproduction("C-0009 crossover hinge constant [pN nm/rad]", 13.5294118, HINGE),
        reproduction("C-0023 mandate [pN/nm]", 33.3333333, MANDATE),
        reproduction(
            "CH-0029 shear allowable at 8 bp [pN]", 18.796,
            ShearJointAllowable().ruptureForce(8.0, ShearJointAllowable.REFERENCE_LOADING_RATE)
        ),
        reproduction("this task: c(0) is the cantilever", 3.0, guidedArmFactor(0.0)),
        reproduction("this task: c(inf) is the guided arm", 12.0, guidedArmFactor(1.0e15)),
        reproduction("this task: c(2) is exactly 6", 6.0, guidedArmFactor(2.0))
    )

    // ------------------------------------------------------------------ findings
    val adoptedCeiling = anchoredArmCeiling(ADOPTED.rotationalStiffness, PATH_COUNT, EI, MANDATE)
    val chordCeiling = anchoredArmCeiling(ADOPTED.chordAxisStiffness, PATH_COUNT, EI, MANDATE)
    val singleLinkCeiling = anchoredArmCeiling(0.0, PATH_COUNT, EI, MANDATE)
    // Every finding is built with string TEMPLATES and per-value "%.2f".format(x), never with
    // literal + literal + ".format(args)" — Kotlin's `+` binds tighter than `.format()`, so that
    // shape formats only the LAST literal and prints the earlier placeholders raw (CLAUDE.md).
    fun f(value: Double, digits: Int = 2): String = "%.${digits}f".format(value)

    val twoSpringAdopted = twoSpringArmForStiffness(
        HINGE, DESIGN_HINGE_COUNT, ADOPTED.rotationalStiffness, EI, PATH_COUNT, MANDATE
    )
    val overPlaced = PATH_COUNT *
            TwoSpringArm(EI, 12.2423721, DESIGN_HINGE_COUNT * HINGE, 1.0e12).stiffness

    val findings = mapOf(
        "verdict" to (
                "c = 12 does NOT survive its own anchorage — the realised factor at the design " +
                        "point is ${f(adoptedDesign.realisedArmFactor)}, not 12 — but the CAP does " +
                        "survive the desired stroke. Because rho = k_far r/EI carries the ARM, the " +
                        "cap is a fixed point rather than a formula evaluated at an asserted c: " +
                        "${f(adoptedCeiling)} nm on the adopted duplex-end anchorage and " +
                        "${f(chordCeiling)} nm even about the CHORD, both above the 10 nm desired " +
                        "stroke, while a ONE-link anchorage collapses to the cantilever's " +
                        "${f(singleLinkCeiling)} nm and fails it."
                ),
        "whatHoldsTheArm" to (
                "The arm's far end is a duplex END, so C-0029's counting theorem applies to it " +
                        "verbatim: two strand termini, lever arm at most the phosphate radius, and " +
                        "a couple 2k_bond,theta + 2k_bond,s a^2 = " +
                        "${f(ADOPTED.rotationalStiffness)} pN nm/rad about the chord's perpendicular " +
                        "bisector against ${f(ADOPTED.chordAxisStiffness)} about the chord itself. " +
                        "A SINGLY nicked continuation of a tile duplex is stiffer " +
                        "(${f(ArmAnchorage.nickedContinuation().rotationalStiffness, 0)} pN nm/rad) " +
                        "and is the motif that matches the guided kinematics, because a nick " +
                        "PRESERVES the helix axis — which is exactly what 'guided' means."
                ),
        "theSeriesQuestion" to (
                "The anchorage IS in series with the hinge and the arm, and the sign is not the " +
                        "one the composition assumes. C-0023's 1/k = r^2/(n k_theta) + r^3/(c EI) " +
                        "is EXACT when the far end carries no moment and WRONG when it does, " +
                        "because a guide carries part of the end moment and RELIEVES the hinge: at " +
                        "the adopted design the series reading retains only " +
                        "${f(series[1].seriesDeparture, 3)} of the true stiffness, and at C-0029's " +
                        "asserted ideal guide only ${f(series[4].seriesDeparture, 3)}. So c = 12 " +
                        "and that composition cannot both be right."
                ),
        "theAxis" to (
                "The arm bends about the axis normal to the chord's plane, and the chord is a " +
                        "diameter of the arm's own cross-section — so the designer CHOOSES the axis " +
                        "with the helical phase, quantised at ${f(quantum * 180.0 / PI)} deg per " +
                        "base pair. The worst misalignment is half a quantum and costs " +
                        "${f(100.0 * (1.0 - axis[1].fractionOfFavourable), 1)} % of the couple, " +
                        "leaving a ceiling of ${f(axis[1].armCeiling)} nm. This is the OPPOSITE of " +
                        "the standoff: C-0029's column buckles about the axis the chord leaves " +
                        "free, but E5's arm carries no axial compression at all, so the free axis " +
                        "is unloaded."
                ),
        "theDesign" to (
                "E5a16 — a ${f(adoptedDesign.armLength)} nm = " +
                        "${f(adoptedDesign.armBasePairs, 0)} bp arm on ${adoptedDesign.hingeCount} " +
                        "crossovers, its far end a two-terminus duplex-end anchorage: realised " +
                        "c = ${f(adoptedDesign.realisedArmFactor)}, secant " +
                        "${f(adoptedDesign.secantAtWorkingPoint, 4)} pN/nm at 3 nm, tangent " +
                        "${f(adoptedDesign.tangentAtWorkingPoint)} at 3 nm and " +
                        "${f(adoptedDesign.tangentAtDesiredStroke)} at 10 nm inside C-0023's " +
                        "40 pN/nm ceiling, turning " +
                        "${f(adoptedDesign.rotationAtDesiredStrokeDegrees, 1)} deg at the desired " +
                        "stroke, ${f(adoptedDesign.hingeBondForceDesired)} pN on a hinge crossover " +
                        "against the 10 pN unzip allowable, and its own anchorage demanding " +
                        "${f(adoptedDesign.bondedLengthDemanded, 1)} bp of bonded length at the " +
                        "desired stroke."
                ),
        "whatMovedFromC0029" to (
                "C-0029's E5g16 is a 12.24 nm arm at c = 12; the realised design is " +
                        "${f(adoptedDesign.armLength)} nm at c = " +
                        "${f(adoptedDesign.realisedArmFactor)} — " +
                        "${f(100.0 * (1.0 - adoptedDesign.armLength / 12.2423721), 1)} % shorter — " +
                        "because two errors run opposite ways: the realised end condition is softer " +
                        "than asserted, and the series composition the arm was solved with is " +
                        "itself the soft reading. The verdict does not move: the arm still exceeds " +
                        "the 10 nm stroke and the tangent still lands inside the ceiling."
                ),
        "thePlacementBracket" to (
                "Solved on the boundary-value problem instead of the series composition, the same " +
                        "16-crossover flexure on the same realised anchorage places at " +
                        "${f(twoSpringAdopted)} nm — against ${f(adoptedDesign.armLength)} nm from " +
                        "the large-rotation series reading and C-0029's ${f(12.2423721)} nm at an " +
                        "asserted c = 12. The three bracket the arm at " +
                        "${f(adoptedDesign.armLength)}–${f(twoSpringAdopted)} nm " +
                        "(${f(adoptedDesign.armBasePairs, 0)}–${f(twoSpringAdopted / RISE, 0)} bp), " +
                        "every reading clears the 10 nm stroke, and every reading sits below the " +
                        "ideal guide's 15.50 nm cap. Two errors run opposite ways and very nearly " +
                        "cancel — which is why C-0029's verdict survives an assertion that does not."
                ),
        "theChallenge" to (
                "C-0029's E5g16 cannot be placed at the mandate on its own c = 12. Read as the " +
                        "two-spring beam that c = 12 describes, a 12.242 nm arm on 16 crossovers " +
                        "with a guided far end assembles to ${f(overPlaced)} pN/nm — " +
                        "${f(overPlaced / MANDATE)}x the 33.3333 pN/nm mandate and past its own " +
                        "40 pN/nm compliance ceiling at the SECANT, before any tangent is taken. " +
                        "The cause is not the anchorage but the composition: C-0023's series form " +
                        "is the free-far-end corner of the same BVP, and C-0029 changed c without " +
                        "changing the composition c belongs to."
                ),
        "theDominantCompliance" to (
                "Leaf A8.2's explicit ask, answered on the realised joint: the hinge carries " +
                        "${f(100.0 * adoptedDesign.hingeComplianceShare, 1)} % of the path " +
                        "compliance and the ARM's own bending " +
                        "${f(100.0 * (1.0 - adoptedDesign.hingeComplianceShare), 1)} %. C-0023 " +
                        "reported 92.5 % hinge for its one-crossover E5; at 16 crossovers and a " +
                        "realised anchorage the dominant term has changed sides."
                )
    )

    val result = T70Result(
        task = "T-70",
        leaf = "A8.2",
        conditions = mapOf(
            "temperature" to "300 K",
            "thermalEnergy" to "4.141947 pN nm",
            "medium" to "aqueous 2 mM MgCl2",
            "tile" to "40 x 40 nm single-layer square-lattice Rothemund sheet",
            "loadPaths" to "45, on C-0015's 3 x 15 grid",
            "mandate" to "100 pN over the acceptable 3 nm stroke = 33.3333 pN/nm",
            "desiredStroke" to "10 nm",
            "bendingRigidity" to "230 pN nm^2 (CanDo MODEL INPUT, not a measurement)",
            "hingeConstant" to "13.53 pN nm/rad (Chen et al., CITED and FITTED, via C-0009)",
            "inPlaneConstant" to "64.71 pN/nm (C-0020's DERIVED construction, NOT measured)",
            "phosphateRadius" to "1.00 nm (Hedley et al. 2024, CITED and READ DIRECTLY)"
        ),
        cheapBounds = cheapBounds,
        continuum = continuum,
        anchorages = anchorages,
        designs = designs,
        placements = placements,
        series = series,
        axis = axis,
        sensitivities = sensitivities,
        convergence = convergence,
        reproductions = reproductions,
        findings = findings
    )

    val json = Json { prettyPrint = true }
    val file = File("gpd/results/T-70-guided-arm-anchorage.json")
    file.parentFile.mkdirs()
    file.writeText(json.encodeToString(json.encodeToJsonElement(result).roundedForResult()))

    println("T-70 — what holds E5g16's guided arm")
    println()
    println("cheap bounds")
    cheapBounds.forEach { (key, value) -> println("  %-32s %12.4f".format(key, value)) }
    println()
    println("the continuum c(rho_far)  (rho, c, c two-spring, c series)")
    continuum.forEach {
        println(
            "  %10.3f %8.4f %8.4f %8.4f".format(
                it.farRestraint, it.armFactor, it.twoSpringFactorAtDesignHinge,
                it.seriesFactorAtDesignHinge
            )
        )
    }
    println()
    println("anchorages (id, links, k_fav, k_chord, rho@12nm, c@12nm, cap, clears 10 nm)")
    anchorages.forEach {
        println(
            "  %-5s %4d %12.2f %10.2f %9.3f %7.3f %8.3f %-6s  %s".format(
                it.id, it.linkCount, it.rotationalStiffness, it.chordAxisStiffness,
                it.restraintAtTwelveNm, it.armFactorAtTwelveNm, it.armCeiling,
                it.ceilingClearsDesiredStroke, it.name.take(44)
            )
        )
    }
    println()
    println("designs (anchorage, n, arm, bp, c, cap, secant, tan3, tan10, rot10, verdict)")
    designs.forEach {
        println(
            "  %-5s %3d %7.3f %6.1f %7.3f %7.3f %8.4f %7.2f %7.2f %6.1f  %s".format(
                it.anchorageId, it.hingeCount, it.armLength, it.armBasePairs,
                it.realisedArmFactor, it.armCeiling, it.secantAtWorkingPoint,
                it.tangentAtWorkingPoint, it.tangentAtDesiredStroke,
                it.rotationAtDesiredStrokeDegrees, it.verdict.take(48)
            )
        )
    }
    println()
    println("placements (anchorage, n, composition, arm, bp, c, assembled, cap, reaches)")
    placements.forEach {
        println(
            "  %-5s %3d %-40s %7.3f %6.1f %7.3f %8.4f %7.3f %s".format(
                it.anchorageId, it.hingeCount, it.composition.take(40), it.armLength,
                it.armBasePairs, it.realisedArmFactor, it.assembledStiffness, it.armCeiling,
                it.reachesDesiredStroke
            )
        )
    }
    println()
    println("the series question (label, c exact, k exact, k series, retained, theta_A, theta_B, residual)")
    series.forEach {
        println(
            "  %-34s %7.3f %8.4f %8.4f %7.4f %7.2f %7.2f %9.2e".format(
                it.label.take(34), it.exactArmFactor, it.exactStiffness, it.seriesStiffness,
                it.seriesDeparture, it.nearRotationDegrees, it.farRotationDegrees,
                it.momentBalanceResidual
            )
        )
    }
    println()
    println("the axis (label, misalignment, projection, k_eff, fraction, cap, clears)")
    axis.forEach {
        println(
            "  %-42s %7.2f %7.4f %8.2f %7.4f %7.3f %s".format(
                it.label.take(42), it.misalignmentDegrees, it.projection, it.effectiveStiffness,
                it.fractionOfFavourable, it.armCeiling, it.clearsDesiredStroke
            )
        )
    }
    println()
    println("sensitivities (axis, label, value, k_far, arm, c, cap, tan10, reaches, pass)")
    sensitivities.forEach {
        println(
            "  %-30s %-34s %10.4f %9.2f %7.3f %6.3f %7.3f %7.2f %-6s %s".format(
                it.axis.take(30), it.label.take(34), it.value, it.farStiffness, it.armLength,
                it.realisedArmFactor, it.armCeiling, it.tangentAtDesiredStroke,
                it.reachesDesiredStroke, it.allPredicatesPass
            )
        )
    }
    println()
    println("convergence")
    convergence.forEach {
        println(
            "  %-44s %-20s %10.4g %14.9f %10.2e".format(
                it.quantity.take(44), it.control, it.level, it.value, it.departureFromFinest
            )
        )
    }
    println()
    println("reproductions (published, derived, departure)")
    reproductions.forEach {
        println(
            "  %-52s %12.6f %12.6f %10.2e".format(
                it.quantity.take(52), it.published, it.derived, it.relativeDeparture
            )
        )
    }
    println()
    findings.forEach { (key, value) -> println("$key:\n  $value\n") }
    println("written to $file")
}
