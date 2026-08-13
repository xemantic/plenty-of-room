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

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.ShearJointAllowable
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.abs

/**
 * Task `T-30` / leaf `A8.2` — the origami joint at a transverse flexure's end.
 *
 * ```shell
 * tools/study.sh anchoring.FlexureEndJointStudyKt
 * ```
 *
 * Emits `gpd/results/T-30-flexure-end-joint.json`, deterministically: the file carries no
 * timestamp and the whole tree is rounded at the **serialisation boundary**, so a re-run that
 * changes nothing produces no diff.
 *
 * Infinities are a modelling idealisation (an ideal pin, an ideal clamp, a held end) and
 * `kotlinx.serialization` refuses them, so every stiffness field emits [INFINITE_SENTINEL] where
 * the model holds `+∞`. The sentinel is negative and no physical stiffness here is.
 */

/** What an ideal (infinite) stiffness is written as in the result file. */
private const val INFINITE_SENTINEL = -1.0

private fun finite(value: Double): Double =
    if (value.isInfinite() || value.isNaN()) INFINITE_SENTINEL else value

@Serializable
data class EndJointRecord(
    val id: String,
    val name: String,
    val rotationalStiffness: Double,
    val axialStiffness: Double,
    val transverseStiffness: Double,
    val anisotropy: Double,
    val transverseDeadBand: Double,
    val restraintAtReferenceSpan: Double,
    val midspanFactorAtReferenceSpan: Double,
    val drawInFactorAtReferenceSpan: Double,
    val effectiveStretchFractionAtReferenceSpan: Double,
    val supportsBeam: Boolean,
    val supportReason: String,
    val provenance: String
)

@Serializable
data class RestraintContinuumRecord(
    val restraint: Double,
    val midspanFactor: Double,
    val endMomentFraction: Double,
    val drawInFactor: Double,
    val note: String
)

@Serializable
data class FlexureDesignRecord(
    val jointId: String,
    val jointName: String,
    val pathCount: Int,
    val span: Double,
    val spanBasePairs: Double,
    val restraint: Double,
    val midspanFactor: Double,
    val effectiveStretchModulus: Double,
    val effectiveStretchFraction: Double,
    val secantStiffness: Double,
    val tangentStiffness: Double,
    val tangentToSecant: Double,
    val compliantCeilingPass: Boolean,
    val drawInDemandAcceptable: Double,
    val drawInDemandAcceptableBasePairs: Double,
    val drawInDemandDesired: Double,
    val jointExtensionAcceptable: Double,
    val axialTensionAcceptable: Double,
    val axialTensionDesired: Double,
    val endShearAcceptable: Double,
    val endShearDesired: Double,
    val perPathShareAcceptable: Double,
    val perPathShareDesired: Double,
    val unzipPassAcceptable: Boolean,
    val unzipPassDesired: Boolean,
    val nickedCeilingPassDesired: Boolean,
    val bondedLengthForAxialAcceptable: Double,
    val bondedLengthForAxialDesired: Double,
    val deflectionRatioDesired: Double,
    val supportsBeam: Boolean,
    val verdict: String
)

@Serializable
data class JointSensitivityRecord(
    val jointId: String,
    val axis: String,
    val value: Double,
    val label: String,
    val axialStiffness: Double,
    val span: Double,
    val spanBasePairs: Double,
    val midspanFactor: Double,
    val effectiveStretchFraction: Double,
    val tangentStiffness: Double,
    val axialTensionDesired: Double,
    val compliantCeilingPass: Boolean
)

@Serializable
data class StandoffDesignRecord(
    val standoffLength: Double,
    val standoffBasePairs: Double,
    val rotationalStiffness: Double,
    val axialStiffness: Double,
    val transverseStiffness: Double,
    val anisotropy: Double,
    val span: Double,
    val spanBasePairs: Double,
    val midspanFactor: Double,
    val tangentStiffness: Double,
    val tangentToSecant: Double,
    val axialTensionAcceptable: Double,
    val axialTensionDesired: Double,
    val standoffDeflectionDesired: Double,
    val standoffDeflectionRatioDesired: Double,
    val bucklingLoadPinnedHead: Double,
    val bucklingLoadGuidedHead: Double,
    val bucklingMarginDesired: Double,
    val supportMargin: Double,
    val compliantCeilingPass: Boolean,
    val allPredicatesPass: Boolean
)

@Serializable
data class JointConvergenceRecord(
    val quantity: String,
    val control: String,
    val level: Double,
    val value: Double,
    val departure: Double
)

@Serializable
data class JointReproductionRecord(
    val quantity: String,
    val source: String,
    val published: Double,
    val derived: Double,
    val relativeDeparture: Double
)

@Serializable
data class FlexureEndJointResult(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val parameters: Map<String, String>,
    val joints: List<EndJointRecord>,
    val restraintContinuum: List<RestraintContinuumRecord>,
    val designs: List<FlexureDesignRecord>,
    val sensitivities: List<JointSensitivityRecord>,
    val standoffWindow: List<StandoffDesignRecord>,
    val convergence: List<JointConvergenceRecord>,
    val reproductions: List<JointReproductionRecord>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val citedNumbers: List<String>
)

// ---------------------------------------------------------------------------------------------

private const val TARGET_FORCE = 100.0

/** §3's acceptable stroke, and the point the placement condition is written at. */
private const val ACCEPTABLE_STROKE = 3.0

/** §3's **desired** stroke, at which every cable term in this programme is judged. */
private const val DESIRED_STROKE = 10.0

private const val MANDATE = TARGET_FORCE / ACCEPTABLE_STROKE

/** `C-0015`'s flatness grid, and `C-0023`'s path count: 45 attachments as 3 x 15. */
private const val PATH_COUNT = 45

private val PATH_COUNTS = listOf(8, 15, 45)

/** `C-0023`'s declared compliance ceiling in `pN/nm`. */
private const val COMPLIANT_CEILING = 40.0

/** The span the joint constants are tabulated at, so that the three cheap bounds are comparable. */
private const val REFERENCE_SPAN = 30.0

/** `P1`: a support has to be this many times stiffer than the beam it supports. */
private const val SUPPORT_MARGIN_REQUIRED = 10.0

/** `P1`: and it has to react at once — this much free play is already 3 % of §3's stroke. */
private const val DEAD_BAND_ALLOWED = 0.1

private val EI = Gen1Tile.DUPLEX_BENDING_RIGIDITY

private val STRETCH = Gen1Tile.DUPLEX_STRETCH_MODULUS

private val RISE = Gen1Tile.RISE_PER_BASE_PAIR

private val ALLOWABLE = ShearJointAllowable()

private val STANDOFF_LENGTHS = listOf(3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)

private val HINGE_NUCLEOTIDES = listOf(2, 5, 10, 20)

private fun bondedLengthOrSentinel(tension: Double): Double =
    if (tension <= 0.0) 0.0
    else if (tension >= ALLOWABLE.saturationForce) INFINITE_SENTINEL
    else bondedLengthForTension(tension, ALLOWABLE)

private fun supportMargin(joint: FlexureEndJoint, perPathStiffness: Double): Double =
    if (joint.transverseStiffness.isInfinite()) INFINITE_SENTINEL
    else joint.transverseStiffness / perPathStiffness

private fun supports(joint: FlexureEndJoint, perPathStiffness: Double): Boolean =
    joint.transverseDeadBand <= DEAD_BAND_ALLOWED &&
            (joint.transverseStiffness.isInfinite() ||
                    joint.transverseStiffness >= SUPPORT_MARGIN_REQUIRED * perPathStiffness)

private fun designFor(
    id: String,
    joint: FlexureEndJoint,
    paths: Int
): FlexureDesignRecord {
    val span = flexureSpanForJoint(EI, joint, paths, MANDATE, ACCEPTABLE_STROKE)
    val flexure = PartiallyRestrainedFlexure(EI, span, joint, STRETCH)
    val secant = paths * flexure.secantStiffness(ACCEPTABLE_STROKE)
    val tangent = paths * flexure.tangentStiffness(ACCEPTABLE_STROKE)
    val perPath = secant / paths
    val tensionAcceptable = flexure.axialTension(ACCEPTABLE_STROKE)
    val tensionDesired = flexure.axialTension(DESIRED_STROKE)
    val shareAcceptable = TARGET_FORCE / paths
    val shareDesired = MANDATE * DESIRED_STROKE / paths
    val supportsIt = supports(joint, perPath)
    val ceilingPass = tangent <= COMPLIANT_CEILING
    val unzipDesired = tensionDesired <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE
    // C-0023's own falsifier 3: at 8 paths the STATIC share alone is 12.5 pN, past the 10 pN
    // unzip allowable, for every element and before any joint is chosen. Read at the DESIRED
    // stroke as well, it is what puts a floor under the path count.
    val sharePass = shareDesired <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE
    val verdict = when {
        !supportsIt -> "FAIL P1 — the joint does not support the beam"
        !ceilingPass -> "FAIL P3 — tangent past the 40 pN/nm compliance ceiling"
        !sharePass -> "FAIL P4 — the static share alone is past the 10 pN unzip allowable"
        !unzipDesired -> "FAIL P4 — beam tension past the 10 pN unzip allowable at 10 nm"
        else -> "PASS"
    }
    return FlexureDesignRecord(
        jointId = id,
        jointName = joint.name,
        pathCount = paths,
        span = span,
        spanBasePairs = span / RISE,
        restraint = finite(flexure.restraint),
        midspanFactor = flexure.midspanFactor,
        effectiveStretchModulus = flexure.effectiveStretchModulus,
        effectiveStretchFraction = flexure.effectiveStretchModulus / STRETCH,
        secantStiffness = secant,
        tangentStiffness = tangent,
        tangentToSecant = tangent / secant,
        compliantCeilingPass = ceilingPass,
        drawInDemandAcceptable = flexure.drawInDemand(ACCEPTABLE_STROKE),
        drawInDemandAcceptableBasePairs = flexure.drawInDemand(ACCEPTABLE_STROKE) / RISE,
        drawInDemandDesired = flexure.drawInDemand(DESIRED_STROKE),
        jointExtensionAcceptable = flexure.jointExtension(ACCEPTABLE_STROKE),
        axialTensionAcceptable = tensionAcceptable,
        axialTensionDesired = tensionDesired,
        endShearAcceptable = flexure.endShear(ACCEPTABLE_STROKE),
        endShearDesired = shareDesired / 2.0,
        perPathShareAcceptable = shareAcceptable,
        perPathShareDesired = shareDesired,
        unzipPassAcceptable = tensionAcceptable <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE,
        unzipPassDesired = unzipDesired,
        nickedCeilingPassDesired = tensionDesired <= Gen1Tile.OVERSTRETCHING_CEILING,
        bondedLengthForAxialAcceptable = bondedLengthOrSentinel(tensionAcceptable),
        bondedLengthForAxialDesired = bondedLengthOrSentinel(tensionDesired),
        deflectionRatioDesired = DESIRED_STROKE / span,
        supportsBeam = supportsIt,
        verdict = verdict
    )
}

private fun jointRecord(id: String, joint: FlexureEndJoint): EndJointRecord {
    val restraint = endRestraintParameter(joint.rotationalStiffness, EI, REFERENCE_SPAN)
    val perPath = MANDATE / PATH_COUNT
    return EndJointRecord(
        id = id,
        name = joint.name,
        rotationalStiffness = finite(joint.rotationalStiffness),
        axialStiffness = finite(joint.axialStiffness),
        transverseStiffness = finite(joint.transverseStiffness),
        anisotropy = finite(joint.anisotropy),
        transverseDeadBand = joint.transverseDeadBand,
        restraintAtReferenceSpan = finite(restraint),
        midspanFactorAtReferenceSpan = midspanFactor(restraint),
        drawInFactorAtReferenceSpan = drawInFactor(restraint),
        effectiveStretchFractionAtReferenceSpan =
            effectiveStretchModulus(STRETCH, joint.axialStiffness, REFERENCE_SPAN) / STRETCH,
        supportsBeam = supports(joint, perPath),
        supportReason = when {
            joint.transverseDeadBand > DEAD_BAND_ALLOWED ->
                ("a dead band of %.2f nm, %.0f %% of the 3 nm stroke, before it reacts at all"
                    .format(joint.transverseDeadBand, 100.0 * joint.transverseDeadBand / 3.0))
            !joint.transverseStiffness.isInfinite() &&
                    joint.transverseStiffness < SUPPORT_MARGIN_REQUIRED * perPath ->
                ("transverse stiffness %.3f pN/nm against the beam's own %.3f — a support that moves"
                    .format(joint.transverseStiffness, perPath))
            else -> "covalent and stiff across the beam: a support"
        },
        provenance = joint.provenance
    )
}

fun main() {
    // ------------------------------------------------------------------ the joint catalogue
    val catalogue = buildList {
        add("I1" to FlexureEndJoint.pinnedAndFree())
        add("I2" to FlexureEndJoint.pinnedAndHeld())
        add("I3" to FlexureEndJoint.clamped())
        add("I4" to FlexureEndJoint.clampedAndHeld())
        add("J1" to FlexureEndJoint.crossover())
        add("J2" to FlexureEndJoint.nickedContinuation())
        add("J2b" to FlexureEndJoint.doublyNickedContinuation())
        HINGE_NUCLEOTIDES.forEach { n ->
            add("J3-$n" to FlexureEndJoint.singleStrandedHinge(n))
        }
        add("J4-2" to FlexureEndJoint.multiCrossoverClamp(2))
        add("J4-3" to FlexureEndJoint.multiCrossoverClamp(3))
        listOf(5.0, 8.0, 10.0).forEach { length ->
            add("J5-${length.toInt()}" to FlexureEndJoint.normalStandoff(length))
        }
    }
    val joints = catalogue.map { (id, joint) -> jointRecord(id, joint) }

    // ------------------------------------------------------------------ the restraint continuum
    val continuum = listOf(0.0, 0.5, 1.0, 2.0, 4.0, 8.0, 16.0, 64.0, 256.0, 4096.0).map { rho ->
        RestraintContinuumRecord(
            restraint = rho,
            midspanFactor = midspanFactor(rho),
            endMomentFraction = endMomentFraction(rho),
            drawInFactor = drawInFactor(rho),
            note = when (rho) {
                0.0 -> "the pinned limit — c = 48 and g = 2.4 exactly"
                8.0 -> "the interior MINIMUM of the draw-in factor: g = 9/4 exactly, at c = 120"
                4096.0 -> "the clamped limit — c -> 192 and g -> 2.4 again"
                else -> ""
            }
        )
    }

    // ------------------------------------------------------------------ the designs
    val designs = buildList {
        catalogue.forEach { (id, joint) -> add(designFor(id, joint, PATH_COUNT)) }
        // the path-count axis, for the four joints the verdict turns on
        listOf(
            "I1" to FlexureEndJoint.pinnedAndFree(),
            "I2" to FlexureEndJoint.pinnedAndHeld(),
            "J1" to FlexureEndJoint.crossover(),
            "J5-8" to FlexureEndJoint.normalStandoff(8.0)
        ).forEach { (id, joint) ->
            PATH_COUNTS.filter { it != PATH_COUNT }.forEach { paths ->
                add(designFor(id, joint, paths))
            }
        }
    }

    // ------------------------------------------------------------------ sensitivities
    val sensitivities = buildList {
        // J1 over Chen et al.'s own alpha bracket
        listOf(
            Gen1Tile.CROSSOVER_ALPHA_MIN, 1.0, Gen1Tile.CROSSOVER_ALPHA_MAX
        ).forEach { alpha ->
            val joint = FlexureEndJoint.crossover(alpha)
            add(sensitivity("J1", "alpha", alpha, "alpha = $alpha", joint))
        }
        // J1 over C-0020's four-decade sweep of the DERIVED in-plane constant
        Gen1Tile.CROSSOVER_IN_PLANE_SWEEP.forEach { multiplier ->
            val joint = FlexureEndJoint.crossover(inPlaneMultiplier = multiplier)
            add(
                sensitivity(
                    "J1", "k_s multiplier", multiplier, "k_s x $multiplier (DERIVED, not measured)",
                    joint
                )
            )
        }
        // J3 over the method-systematic Kuhn bracket, at C-0023's own two nucleotides
        listOf(
            SsDnaTether.KUHN_LENGTH_FORCE_SPECTROSCOPY to "1.34 nm — 10-40 pN spectroscopy, NOT applicable at ~1 pN",
            SsDnaTether.KUHN_LENGTH_ZERO_FORCE to "2.10 nm — zero-force scattering, the applicable end",
            SsDnaTether.KUHN_LENGTH_ZERO_FORCE_TWO_MILLIMOLAR to "2.84 nm — zero force at 2 mM MgCl2"
        ).forEach { (kuhn, label) ->
            val joint = FlexureEndJoint.singleStrandedHinge(2, kuhnLength = kuhn)
            add(sensitivity("J3-2", "Kuhn length", kuhn, label, joint))
        }
    }

    // ------------------------------------------------------------------ the standoff window
    val standoffWindow = STANDOFF_LENGTHS.map { length ->
        val joint = FlexureEndJoint.normalStandoff(length)
        val span = flexureSpanForJoint(EI, joint, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE)
        val flexure = PartiallyRestrainedFlexure(EI, span, joint, STRETCH)
        val secant = PATH_COUNT * flexure.secantStiffness(ACCEPTABLE_STROKE)
        val tangent = PATH_COUNT * flexure.tangentStiffness(ACCEPTABLE_STROKE)
        val tensionDesired = flexure.axialTension(DESIRED_STROKE)
        val deflection = tensionDesired / joint.axialStiffness
        val pinned = eulerBucklingLoad(EI, length, BeamEndCondition.PINNED_HEAD)
        val guided = eulerBucklingLoad(EI, length, BeamEndCondition.GUIDED_HEAD)
        val shearDesired = MANDATE * DESIRED_STROKE / PATH_COUNT / 2.0
        val margin = pinned / shearDesired
        val support = joint.transverseStiffness / (secant / PATH_COUNT)
        val ceiling = tangent <= COMPLIANT_CEILING
        StandoffDesignRecord(
            standoffLength = length,
            standoffBasePairs = length / RISE,
            rotationalStiffness = joint.rotationalStiffness,
            axialStiffness = joint.axialStiffness,
            transverseStiffness = joint.transverseStiffness,
            anisotropy = joint.anisotropy,
            span = span,
            spanBasePairs = span / RISE,
            midspanFactor = flexure.midspanFactor,
            tangentStiffness = tangent,
            tangentToSecant = tangent / secant,
            axialTensionAcceptable = flexure.axialTension(ACCEPTABLE_STROKE),
            axialTensionDesired = tensionDesired,
            standoffDeflectionDesired = deflection,
            standoffDeflectionRatioDesired = deflection / length,
            bucklingLoadPinnedHead = pinned,
            bucklingLoadGuidedHead = guided,
            bucklingMarginDesired = margin,
            supportMargin = support,
            compliantCeilingPass = ceiling,
            // exactly the predicates P1, P3, P4 and P5 declared in the task file before the run
            // — the buckling margin is REPORTED beside them, at both end conditions, and is not
            // one of them: adding a sixth predicate after seeing the numbers is how a window
            // gets tuned rather than found
            allPredicatesPass = ceiling &&
                    support >= SUPPORT_MARGIN_REQUIRED &&
                    tensionDesired <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE &&
                    length <= 10.0
        )
    }

    // ------------------------------------------------------------------ convergence
    val referenceSpan = flexureSpanForJoint(
        EI, FlexureEndJoint.crossover(), PATH_COUNT, MANDATE, ACCEPTABLE_STROKE, scanSteps = 4096
    )
    val convergence = buildList {
        listOf(32, 128, 512, 2048).forEach { steps ->
            val span = flexureSpanForJoint(
                EI, FlexureEndJoint.crossover(), PATH_COUNT, MANDATE, ACCEPTABLE_STROKE,
                scanSteps = steps
            )
            add(
                JointConvergenceRecord(
                    quantity = "J1 design span",
                    control = "bracketing scan steps",
                    level = steps.toDouble(),
                    value = span,
                    departure = abs(span - referenceSpan) / referenceSpan
                )
            )
        }
        // the analytic tangent against a central difference, refined
        val flexure = PartiallyRestrainedFlexure(EI, 32.0, FlexureEndJoint.crossover(), STRETCH)
        listOf(1e-3, 1e-4, 1e-5).forEach { h ->
            val numeric = (flexure.reaction(3.0 + h) - flexure.reaction(3.0 - h)) / (2.0 * h)
            add(
                JointConvergenceRecord(
                    quantity = "J1 tangent at 3 nm",
                    control = "central-difference step",
                    level = h,
                    value = numeric,
                    departure = abs(numeric - flexure.tangentStiffness(3.0)) /
                            flexure.tangentStiffness(3.0)
                )
            )
        }
        // the inverted allowable, against its own forward evaluation
        listOf(5.0, 18.8, 34.81, 47.11).forEach { force ->
            val bp = bondedLengthForTension(force, ALLOWABLE)
            add(
                JointConvergenceRecord(
                    quantity = "bonded length inversion",
                    control = "target tension",
                    level = force,
                    value = bp,
                    departure = abs(ALLOWABLE.ruptureForce(bp, 100.0) - force) / force
                )
            )
        }
    }

    // ------------------------------------------------------------------ reproductions
    val freeSpan = designs.first { it.jointId == "I1" && it.pathCount == PATH_COUNT }
    val heldSpan = designs.first { it.jointId == "I2" && it.pathCount == PATH_COUNT }
    val clampedFree = designs.first { it.jointId == "I3" && it.pathCount == PATH_COUNT }
    val clampedHeld = designs.first { it.jointId == "I4" && it.pathCount == PATH_COUNT }
    val reproductions = listOf(
        reproduction("C-0023 E3a span (pinned, free to draw in)", "C-0023", 24.61, freeSpan.span),
        reproduction("C-0023 E3b span (pinned, held axially)", "C-0023", 49.41, heldSpan.span),
        reproduction("C-0023 clamped/free span", "C-0023", 39.07, clampedFree.span),
        reproduction("C-0023 clamped/held span", "C-0023", 54.91, clampedHeld.span),
        reproduction("C-0023 E3a tangent", "C-0023", 33.333, freeSpan.tangentStiffness),
        reproduction("C-0023 E3b tangent", "C-0023", 91.13, heldSpan.tangentStiffness),
        reproduction(
            "C-0023 E3b axial tension at 3 nm", "C-0023", 8.08, heldSpan.axialTensionAcceptable
        ),
        reproduction(
            "C-0023 E3b axial tension at 10 nm", "C-0023", 86.7, heldSpan.axialTensionDesired
        ),
        reproduction(
            "C-0023 end draw-in demand at the free span", "C-0023",
            0.8776, freeSpan.drawInDemandAcceptable
        ),
        reproduction(
            "C-0023 end draw-in in base pairs", "C-0023",
            2.58, freeSpan.drawInDemandAcceptableBasePairs
        ),
        reproduction(
            "Gen1Tile crossover hinge constant", "C-0009 / Chen et al. 2014",
            13.5294, Gen1Tile.crossoverHingeStiffness()
        ),
        reproduction(
            "Gen1Tile crossover in-plane construction", "C-0020",
            64.7059, Gen1Tile.crossoverInPlaneStiffness()
        ),
        reproduction(
            "CH-0029 shear allowable at 8 bp", "C-0024", 18.80, ALLOWABLE.ruptureForce(8.0, 100.0)
        ),
        reproduction(
            "CH-0029 shear allowable at 16 bp", "C-0024", 34.81, ALLOWABLE.ruptureForce(16.0, 100.0)
        ),
        reproduction(
            "CH-0029 shear allowable at 30 bp", "C-0024", 47.11, ALLOWABLE.ruptureForce(30.0, 100.0)
        ),
        reproduction(
            "CH-0029 loading-rate-free saturation", "C-0024", 68.12, ALLOWABLE.saturationForce
        ),
        reproduction("C-0017 mandate", "C-0017", 33.3333, MANDATE)
    )

    val crossoverDesign = designs.first { it.jointId == "J1" && it.pathCount == PATH_COUNT }
    val standoffDesign = designs.first { it.jointId == "J5-8" && it.pathCount == PATH_COUNT }
    val nominal = standoffWindow.first { it.standoffLength == 8.0 }
    val nominalFlexure =
        PartiallyRestrainedFlexure(EI, nominal.span, FlexureEndJoint.normalStandoff(8.0), STRETCH)
    val hinge = designs.first { it.jointId == "J3-2" && it.pathCount == PATH_COUNT }
    val hingeLong = designs.first { it.jointId == "J3-10" && it.pathCount == PATH_COUNT }
    val hingeJoint = FlexureEndJoint.singleStrandedHinge(2)
    val passing = standoffWindow.filter { it.allPredicatesPass }
    /** the joints that actually support the beam — the only ones whose `c` is a design number. */
    val supporting = designs.filter { it.pathCount == PATH_COUNT && it.supportsBeam }
        .filter { !it.jointId.startsWith("I") }

    val result = FlexureEndJointResult(
        task = "T-30",
        leaf = "A8.2",
        title = "The origami joint at a transverse flexure's end: does it draw in, and does it clamp?",
        verificationType = "in-silico (a partial-restraint flexure whose two end brackets are the " +
                "two limits of one two-parameter joint) + logical (an anisotropy argument that " +
                "decides which joints can exist before any of them is evaluated)",
        acceptance = "P1 the joint supports the beam; P2 the span places 33.3333 pN/nm at 3 nm; " +
                "P3 the tangent stays at or below 40 pN/nm; P4 every per-path force below the " +
                "10 pN unzip allowable at the DESIRED 10 nm stroke and below the 65 pN nicked " +
                "ceiling, judged against CH-0029's length-dependent shear ladder and never a " +
                "flat 48 pN; P5 every length inside C-0017's envelope",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated. " +
                "No joint here has been built and none is a sequence design; base pairs and " +
                "nucleotides make the design statement concrete, they do not specify a staple",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm (= mN/m)",
            "rotational stiffness" to "pN*nm/rad",
            "bending rigidity" to "pN*nm^2",
            "energy" to "pN*nm",
            "temperature" to "K",
            "sentinel" to "$INFINITE_SENTINEL means an ideal (infinite) stiffness"
        ),
        conventions = listOf(
            "x along the beam's own axis, w its transverse deflection (the tile's normal " +
                    "coordinate); the beam is symmetric about midspan",
            "the element displacement d is signed and positive downward; the reaction is " +
                    "positive upward (C-0017, C-0023)",
            "the draw-in is the TOTAL inward motion of the two ends, positive when they approach",
            "a joint is (k_theta, k_a, k_transverse, dead band): rotation about the bending axis, " +
                    "translation along the beam, translation across it, and the free play before " +
                    "it reacts transversely at all",
            "rho = k_theta L/EI, so c depends on the SPAN as well as on the joint",
            "base pairs at the 0.34 nm rise; nucleotides at 0.65 nm of ssDNA contour, the " +
                    "inextensible convention that travels with the zero-force Kuhn length"
        ),
        parameters = mapOf(
            "temperature" to "$ROOM_TEMPERATURE K",
            "thermalEnergy" to "${thermalEnergy()} pN*nm",
            "medium" to "aqueous 2 mM MgCl2",
            "targetForce" to "$TARGET_FORCE pN (section 3)",
            "acceptableStroke" to "$ACCEPTABLE_STROKE nm (section 3)",
            "desiredStroke" to "$DESIRED_STROKE nm (section 3)",
            "mandate" to "$MANDATE pN/nm (C-0017)",
            "pathCount" to "$PATH_COUNT (C-0015's 3 x 15 flatness grid, via C-0023)",
            "compliantCeiling" to "$COMPLIANT_CEILING pN/nm (C-0023's own declared ceiling)",
            "bendingRigidity" to "$EI pN*nm^2 — CITED, a CanDo MODEL INPUT, not a measurement",
            "stretchModulus" to "$STRETCH pN — CITED, MEASURED (Wang et al. 1997)",
            "supportMarginRequired" to "$SUPPORT_MARGIN_REQUIRED x the beam's own per-path stiffness",
            "deadBandAllowed" to "$DEAD_BAND_ALLOWED nm",
            "loadingRate" to "${ShearJointAllowable.REFERENCE_LOADING_RATE} pN/s — the rate the " +
                    "48 pN was measured at (Strunz et al. 1999, via C-0024)"
        ),
        joints = joints,
        restraintContinuum = continuum,
        designs = designs,
        sensitivities = sensitivities,
        standoffWindow = standoffWindow,
        convergence = convergence,
        reproductions = reproductions,
        findings = mapOf(
            "theEndConditionBracketIsNotFourFold" to (
                    "c(rho) = 192(rho+2)/(rho+8) is exactly 48 at rho = 0 and exactly 192 as " +
                            "rho -> infinity, so C-0023's 4x bracket is the interior of ONE " +
                            "function rather than a choice of two. At the crossover constant it " +
                            "is %.1f on a %.2f nm span — neither end. And the joints that " +
                            "actually SUPPORT the beam span c = %.1f to %.1f, the upper %.0f %% " +
                            "of the bracket: nothing that supports a beam is anywhere near pinned, " +
                            "and the near-pinned joints are exactly the ones that fail P1."
                    ).format(
                        crossoverDesign.midspanFactor, crossoverDesign.span,
                        supporting.minOf { it.midspanFactor },
                        supporting.maxOf { it.midspanFactor },
                        100.0 * (192.0 - supporting.minOf { it.midspanFactor }) / (192.0 - 48.0)
                    ),
            "theDrawInFactorIsNot2point4" to (
                    "g(0) = g(infinity) = 2.4 exactly, and the interior MINIMUM is exactly 9/4 " +
                            "at rho = 8, i.e. c = 120. C-0023's 'the same 2.4 for both end " +
                            "conditions, which is not obvious' is right at the endpoints and up " +
                            "to 6.25 %% high between them, so 2.4 is a CEILING on the draw-in " +
                            "demand over the whole continuum rather than a constant. At the " +
                            "design point it is %.4f."
                    ).format(drawInFactor(nominalFlexure.restraint)),
            "anIsotropicJointCannotDoBoth" to (
                    "A joint has to be stiff ACROSS the beam (it reacts the end shear, in both " +
                            "directions) and soft ALONG it (it releases the draw-in), and for any " +
                            "FLEXIBLE LINK those are the same number — anisotropy exactly 1. " +
                            "C-0023's proposed two-nucleotide single-stranded hinge therefore " +
                            "buys %.2f nm of axial release at the price of %.2f nm of transverse " +
                            "DEAD BAND, %.0f %% of section 3's own stroke, and supplies only " +
                            "%.2f pN/nm across the beam against the beam's own %.4f — a support " +
                            "that moves. It fails P1, and P1 is a requirement C-0023 never wrote " +
                            "down. This is C-0014's convexity theorem in a new place."
                    ).format(
                        hingeJoint.contourLength, hingeJoint.transverseDeadBand,
                        100.0 * hingeJoint.transverseDeadBand / ACCEPTABLE_STROKE,
                        hingeJoint.transverseStiffness, MANDATE / PATH_COUNT
                    ),
            "everyCovalentJointIsIsotropicToo" to
                    ("J1, J2, J2b and J4 are all covalent ties on Chen et al.'s softened bond, " +
                            "which has no direction either, so their anisotropy is 1 as well: " +
                            "they support the beam AND hold it. The escape is C-0023's own — " +
                            "bending is signed and HAS a direction — applied one level down, to " +
                            "the joint instead of the element: a duplex standing NORMAL to the " +
                            "sheet carries the end shear along its own axis (S/l) and releases " +
                            "the draw-in by bending (3EI/l^3), an anisotropy of S l^2/(3EI) that " +
                            "the designer sets with a length and that grows as l^2."),
            "theCrossoverJointLandsInTheMiddleAndFails" to (
                    "A direct crossover gives span %.2f nm (%.0f bp), c = %.1f, S_eff/S = %.3f " +
                            "and a tangent of %.2f pN/nm — %.2fx past C-0023's own 40 pN/nm " +
                            "compliance ceiling — with %.2f pN of beam tension at section 3's " +
                            "DESIRED 10 nm stroke, %.1fx the 10 pN unzip allowable though still " +
                            "under the 65 pN nicked ceiling. C-0023's RESTRAINED verdict is the " +
                            "one that survives for every covalent isotropic joint."
                    ).format(
                        crossoverDesign.span, crossoverDesign.spanBasePairs,
                        crossoverDesign.midspanFactor, crossoverDesign.effectiveStretchFraction,
                        crossoverDesign.tangentStiffness,
                        crossoverDesign.tangentStiffness / COMPLIANT_CEILING,
                        crossoverDesign.axialTensionDesired,
                        crossoverDesign.axialTensionDesired / Gen1Tile.DUPLEX_UNZIP_ALLOWABLE
                    ),
            "aDoubleNickIsACrossover" to
                    ("A nicked continuation keeps one intact backbone, which is not a softened " +
                            "bond, so it is effectively clamped AND effectively held — the worst " +
                            "corner of both of C-0023's brackets. Cut the second backbone at the " +
                            "same base pair and nothing continuous is left: J2b reproduces J1 to " +
                            "the last digit. A double nick IS a crossover, and that is a result " +
                            "of the construction rather than an assumption put into it."),
            "theDesignThatResults" to (
                    "A normal duplex standoff of %.0f nm (%.0f bp) under each end of the beam: " +
                            "span %.2f nm (%.0f bp), c = %.1f, secant %.4f pN/nm by construction, " +
                            "tangent %.2f pN/nm (t/s = %.3f, inside the 40 pN/nm ceiling with " +
                            "%.0f %% to spare), beam tension %.3f pN at 3 nm and %.2f pN at 10 nm " +
                            "— both under the 10 pN unzip allowable — a transverse support margin " +
                            "of %.0fx, and an Euler buckling margin at the desired stroke of " +
                            "%.1fx pinned-head / %.1fx guided-head."
                    ).format(
                        nominal.standoffLength, nominal.standoffBasePairs, nominal.span,
                        nominal.spanBasePairs, nominal.midspanFactor,
                        MANDATE, nominal.tangentStiffness, nominal.tangentToSecant,
                        100.0 * (COMPLIANT_CEILING - nominal.tangentStiffness) / COMPLIANT_CEILING,
                        nominal.axialTensionAcceptable, nominal.axialTensionDesired,
                        nominal.supportMargin, nominal.bucklingMarginDesired,
                        4.0 * nominal.bucklingMarginDesired
                    ),
            "theStandoffWindow" to (
                    "%d of %d standoff lengths pass P1, P3, P4 and P5 — %.0f to %.0f nm (%.0f to " +
                            "%.0f bp). It is closed BELOW by the compliance ceiling (a short " +
                            "standoff is axially stiff, the membrane term returns) and ABOVE by " +
                            "C-0017's own 10 nm standoff envelope, and across it the Euler " +
                            "buckling margin at the desired stroke falls from %.1fx to %.1fx, " +
                            "which is why the design point sits at the SHORT end of a window " +
                            "whose short end is the binding one for compliance. The two closures " +
                            "are different mechanisms and neither is the other's slack."
                    ).format(
                        passing.size, standoffWindow.size,
                        passing.minOf { it.standoffLength }, passing.maxOf { it.standoffLength },
                        passing.minOf { it.standoffBasePairs }, passing.maxOf { it.standoffBasePairs },
                        passing.maxOf { it.bucklingMarginDesired },
                        passing.minOf { it.bucklingMarginDesired }
                    ),
            "theHingeVerdictOnItsOwnAxis" to (
                    "Sized purely on stiffness the 2 nt hinge would give span %.2f nm and " +
                            "tangent %.2f pN/nm, which fails P3 as well; a 10 nt hinge would give " +
                            "%.2f nm and %.2f pN/nm, which PASSES P3. Both fail P1. So the hinge " +
                            "is not excluded by the axis C-0023 was reasoning on — it is excluded " +
                            "by the one it did not have."
                    ).format(
                        hinge.span, hinge.tangentStiffness, hingeLong.span, hingeLong.tangentStiffness
                    ),
            "CH0029Applied" to (
                    "Judged on CH-0029's ladder rather than the flat 48 pN, the beam's own axial " +
                            "tension at the desired stroke asks for a hybridised bonded length of " +
                            "%.1f bp at the crossover joint and %.1f bp at the standoff design, " +
                            "against Strunz's 18.8 pN at 8 bp, 34.8 at 16 and 47.1 at 30. " +
                            "C-0023's fully restrained reading asks %.1f pN, which is PAST the " +
                            "68.1 pN loading-rate-free saturation: no bonded length of any size " +
                            "carries it, at any rate inside the measured range."
                    ).format(
                        crossoverDesign.bondedLengthForAxialDesired,
                        standoffDesign.bondedLengthForAxialDesired,
                        heldSpan.axialTensionDesired
                    ),
            "theDesiredStrokePutsAFloorUnderThePathCount" to (
                    "C-0023 read the per-path static share at section 3's ACCEPTABLE 3 nm point, " +
                            "where 45 paths give 2.22 pN. At the DESIRED 10 nm stroke the same " +
                            "coupling delivers %.1f pN and the share is %.2f pN, so the 10 pN " +
                            "unzip allowable puts a floor of %.1f — i.e. %d — load paths under " +
                            "the design, independently of the joint, the element and the layer. " +
                            "C-0015's flatness grid of 45 clears it by only %.2fx, which is a " +
                            "FOURTH independent route to the same count and the tightest of them."
                    ).format(
                        MANDATE * DESIRED_STROKE,
                        MANDATE * DESIRED_STROKE / PATH_COUNT,
                        MANDATE * DESIRED_STROKE / Gen1Tile.DUPLEX_UNZIP_ALLOWABLE,
                        Math.ceil(MANDATE * DESIRED_STROKE / Gen1Tile.DUPLEX_UNZIP_ALLOWABLE)
                            .toInt(),
                        Gen1Tile.DUPLEX_UNZIP_ALLOWABLE / (MANDATE * DESIRED_STROKE / PATH_COUNT)
                    ),
            "T13StillCloses" to
                    ("The coupling stays two-sided under every joint — the flexure's law is odd " +
                            "at every restraint, asserted at negative argument — and the " +
                            "placement condition is met by construction at every one, so " +
                            "C-0023's zero-bias verdict (k >= k_BT/sigma^2 = 0.4602 pN/nm, " +
                            "supplied 72.4x over, tetherless) is untouched and T-13 still closes. " +
                            "What moves is the SPAN and the TANGENT, i.e. C-0023's own compliance " +
                            "ceiling and its per-path forces at the desired stroke.")
        ),
        validity = listOf(
            "TRL 1-3. Nothing here is measured. No joint has been built and none is a sequence " +
                    "design; base pairs and nucleotides make the design statement concrete",
            "Euler-Bernoulli with a two-spring joint. The two springs are treated as INDEPENDENT; " +
                    "a real cantilever standoff has an off-diagonal compliance (a tip force also " +
                    "rotates the tip) which is not modelled and which softens the joint further, " +
                    "so J5's numbers are the STIFF reading of it",
            "the membrane term is C-0023's two-term large-deflection model, unchanged, with " +
                    "S_eff in place of S. At 3 nm on a 25-50 nm span the deflection ratio is " +
                    "5-12 %, inside its range; at the DESIRED 10 nm stroke it is 18-40 % and " +
                    "the model UNDERSTATES the stiffening, so every 10 nm column is a lower bound",
            "the cable geometry charges 2 d^2/L of draw-in while the beam's own deflected shape " +
                    "demands 2.25-2.40 d^2/L — a 1.13-1.20x gap between the term that produces " +
                    "the tension and the term that measures the demand. Both are reported and " +
                    "the larger is used wherever a joint is sized",
            "k_theta = 2 alpha B/(100 a) is C-0009's CITED, FITTED constant with Chen et al.'s " +
                    "own alpha in [0.6, 1.2], a factor of exactly two, and it is swept",
            "k_s = 2 alpha S/(100 a) is C-0020's DERIVED construction and is NOT measured; it is " +
                    "swept over C-0020's own four decades and no verdict moves across them",
            "EI = 230 pN*nm^2 is a CanDo MODEL INPUT, not a measurement; its implied persistence " +
                    "length is 55.5 nm against 40-47 measured, so it is the STIFF end and every " +
                    "span here is correspondingly long",
            "the ssDNA hinge uses the ZERO-FORCE end of the method-systematic Kuhn bracket, " +
                    "2.10 nm, because these elements carry ~1 pN; the 1.34-1.41 nm from 10-40 pN " +
                    "force spectroscopy is reported beside it and does not change the P1 verdict",
            "the standoff's buckling load is quoted at BOTH end conditions (K = 2 and K = 1), a " +
                    "factor of exactly 4, and the binding margin is read at the pinned-head one",
            "the joint is treated as loaded only by the beam's end shear and axial tension; the " +
                    "flexure array on a common superstructure is T-31's, and the lever's own " +
                    "ability to react a downward push is T-33's",
            "one beam per load path, exactly as C-0023 assumes, and the same 45 attachments",
            "no zero-bias re-solve: the element's SIDEDNESS is unchanged by the joint, so " +
                    "C-0023's confinement verdict is inherited rather than recomputed"
        ),
        openQuestions = listOf(
            "The off-diagonal compliance of a cantilever standoff, which couples the end's " +
                    "rotation to its axial motion and is not modelled here",
            "Whether a duplex standing normal to a single-layer sheet can be built with a " +
                    "rotationally stiff base at all — the base joint is itself one of J1-J4",
            "k_s, C-0020's derived crossover in-plane constant, which is the one input that " +
                    "moves the crossover joint's span by more than the design tolerance. T-9",
            "Whether the beam can be built PRE-BOWED, which would make the draw-in demand " +
                    "relative to the built shape and is worth up to 2x in the peak tension",
            "The interaction between neighbouring flexures' standoffs on a shared superstructure " +
                    "(T-31), which the independent-leaf-spring reading does not cover"
        ),
        citedNumbers = listOf(
            "EI = 230 pN*nm^2, GJ = 460 — CITED, CanDo MODEL INPUTS (Kim et al., NAR 40:2862, " +
                    "2012), NOT measurements",
            "S = 1100 pN — CITED, MEASURED (Wang et al., Biophys. J. 72:1335, 1997), in Mg2+",
            "k_theta = 2 alpha B/(100 a), alpha in [0.6, 1.2] — CITED, FITTED (Chen et al., " +
                    "JACS 136:6995, 2014, SI S2), via C-0009/Gen1Tile",
            "k_s = 2 alpha S/(100 a) — DERIVED from the same construction (C-0020), NOT measured",
            "ssDNA Kuhn length 2.10 nm at the zero-force end — CITED, MEASURED (Chen et al., " +
                    "PNAS 109:799, 2012); 1.34-1.41 nm from 10-40 pN spectroscopy (Bosco et al., " +
                    "NAR 42:2064, 2014) reported beside it",
            "ssDNA contour 0.65 nm/nt, inextensible convention — CITED, MEASURED (Sim et al. " +
                    "2012; Bosco et al. 2014). The convention travels with the number",
            "rise per base pair 0.34 nm, crossover interface pitch 32 bp — CITED (Douglas et al. " +
                    "2009; Rothemund, Nature 440:297, 2006)",
            "the shear allowable's three constants — CITED, MEASURED (Strunz et al., PNAS " +
                    "96:11277, 1999), via C-0024/CH-0029, used inside their measured " +
                    "16-4000 pN/s and never extrapolated to zero rate",
            "10 pN unzip, 65 pN nicked ceiling — CITED, MEASURED, via C-0006",
            "section 3 targets 100 pN, 3 nm, 10 nm, 40 x 40 nm, 2 mM — CITED",
            "C-0023's four spans (24.61, 39.07, 49.41, 54.91 nm) and two tangents (33.333, " +
                    "91.13 pN/nm) — CITED, and reproduced here as gate-5 tests"
        )
    )

    val json = Json { prettyPrint = true; encodeDefaults = true }
    val file = File("gpd/results/T-30-flexure-end-joint.json")
    file.parentFile.mkdirs()
    file.writeText(json.encodeToString(json.encodeToJsonElement(result).roundedForResult()) + "\n")
    report(result, file)
}

private fun sensitivity(
    id: String,
    axis: String,
    value: Double,
    label: String,
    joint: FlexureEndJoint
): JointSensitivityRecord {
    val span = flexureSpanForJoint(EI, joint, PATH_COUNT, MANDATE, ACCEPTABLE_STROKE)
    val flexure = PartiallyRestrainedFlexure(EI, span, joint, STRETCH)
    val tangent = PATH_COUNT * flexure.tangentStiffness(ACCEPTABLE_STROKE)
    return JointSensitivityRecord(
        jointId = id,
        axis = axis,
        value = value,
        label = label,
        axialStiffness = joint.axialStiffness,
        span = span,
        spanBasePairs = span / RISE,
        midspanFactor = flexure.midspanFactor,
        effectiveStretchFraction = flexure.effectiveStretchModulus / STRETCH,
        tangentStiffness = tangent,
        axialTensionDesired = flexure.axialTension(DESIRED_STROKE),
        compliantCeilingPass = tangent <= COMPLIANT_CEILING
    )
}

private fun reproduction(
    quantity: String,
    source: String,
    published: Double,
    derived: Double
): JointReproductionRecord = JointReproductionRecord(
    quantity = quantity,
    source = source,
    published = published,
    derived = derived,
    relativeDeparture = abs(derived - published) / abs(published)
)

private fun report(result: FlexureEndJointResult, file: File) {
    println("T-30 — the origami joint at a transverse flexure's end")
    println()
    println("joints (k_theta, k_a, k_transverse, anisotropy, dead band, supports?)")
    result.joints.forEach {
        println(
            "  %-6s %-42s %10.3f %10.3f %10.3f %8.1f %6.2f  %s".format(
                it.id, it.name.take(42), it.rotationalStiffness, it.axialStiffness,
                it.transverseStiffness, it.anisotropy, it.transverseDeadBand,
                if (it.supportsBeam) "supports" else "NO SUPPORT"
            )
        )
    }
    println()
    println("designs at 45 paths (span, bp, c, S_eff/S, secant, tangent, T(3), T(10), verdict)")
    result.designs.filter { it.pathCount == 45 }.forEach {
        println(
            "  %-6s %7.2f %6.1f %6.1f %6.3f %8.3f %8.2f %8.3f %8.2f  %s".format(
                it.jointId, it.span, it.spanBasePairs, it.midspanFactor,
                it.effectiveStretchFraction, it.secantStiffness, it.tangentStiffness,
                it.axialTensionAcceptable, it.axialTensionDesired, it.verdict
            )
        )
    }
    println()
    println("the standoff window (l, bp, span, c, tangent, T(10), buckling margin, all pass?)")
    result.standoffWindow.forEach {
        println(
            "  %5.1f %5.1f %7.2f %6.1f %8.2f %7.2f %8.2f  %s".format(
                it.standoffLength, it.standoffBasePairs, it.span, it.midspanFactor,
                it.tangentStiffness, it.axialTensionDesired, it.bucklingMarginDesired,
                if (it.allPredicatesPass) "PASS" else "fail"
            )
        )
    }
    println()
    println("reproductions (published, derived, departure)")
    result.reproductions.forEach {
        println(
            "  %-46s %12.4f %12.4f %10.2e".format(
                it.quantity.take(46), it.published, it.derived, it.relativeDeparture
            )
        )
    }
    println()
    result.findings.forEach { (key, value) -> println("$key:\n  $value\n") }
    println("written to $file")
}
