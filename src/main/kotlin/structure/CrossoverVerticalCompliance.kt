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

package com.xemantic.nano.plentyofroom.structure

import kotlinx.serialization.Serializable
import kotlin.math.abs

/**
 * `T-9`'s **second** deliverable — the crossover's **vertical/axial** compliance.
 *
 * ## The coordinate, which is not the one `C-0157` measured
 *
 * [OrigamiGrillage.linkExtension] is
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`e = (w_b + (d/2)φ_b) − (w_{b+1} − (d/2)φ_{b+1})`
 *
 * — the relative **out-of-plane** displacement of the two duplex **surfaces** that face each other
 * across an interface, in nm.  It is *not* the interduplex **roll** (`C-0157`'s dihedral) and *not*
 * the **interhelical distance** (`C-0157`'s reproduced sawtooth, an in-plane separation).
 * `k_z` is the stiffness conjugate to `e`, in **pN/nm**.
 *
 * ## Why there is a constant here at all, and why it is a construction rather than a measurement
 *
 * `C-0009` carries the link as a **penalty**, [OrigamiGrillage.RIGID_LINK_STIFFNESS] `= 1e4 pN/nm`,
 * whose KDoc justifies the value against the **duplex stretch modulus per nm** and against the
 * hinge's own equivalent [hingeEquivalentVerticalStiffness] — and against nothing else.
 * `Gen1Tile.crossoverInPlaneStiffness` meanwhile *derives* a crossover's **displacement** stiffness
 * two hundred lines away, by applying Chen et al.'s softened-bond construction to the duplex
 * constant that describes displacement rather than rotation:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`k = 2αS/(100a) = 64.7 pN/nm` at `α = 1`.
 *
 * The vertical link and the in-plane connector are **the same two phosphate bonds resisting a
 * relative displacement of the same two duplexes**, on orthogonal axes.  So the construction
 * applies to the vertical axis unchanged — [crossoverVerticalStiffness] is asserted equal to
 * `Gen1Tile.crossoverInPlaneStiffness` by gate `G1`, which is the whole traceability argument —
 * and, exactly as `C-0020` does for the in-plane axis, **every result is reported over
 * [crossoverVerticalStiffnessSweep], four decades wide**.
 *
 * **Nothing about a crossover's vertical stiffness has been measured anywhere**, and this file
 * does not pretend otherwise: it supplies the *criterion* a measurement would be read against.
 *
 * Conventions: `x` along the helices, `y` across them, `z` normal and positive upward;
 * lengths **nm**, forces **pN**, stiffness **pN/nm**; `k_BT = 4.141947 pN·nm` at 300 K.
 */

/** `T-5b`'s flatness convention: peak dishing as a fraction of the free stroke. */
const val FLATNESS_CONVENTION: Double = 0.10

/**
 * `C-0099`'s whole row-end unknown, as a movement of the dishing **over the free stroke**:
 * `0.0651753854 − 0.0621469105`, its own two emitted readings, which it describes as
 * **3.0 percentage points of margin** against `T-5b`'s [FLATNESS_CONVENTION].
 * A movement below this is smaller than a question the corpus has already measured and closed.
 *
 * **CORRECTED, and the correction is published rather than applied silently.**
 * `T-9`'s Plan first wrote this threshold as `0.030` — a factor of ten out against the quantity
 * its own sentence names, because three percentage points *of* `0.10` is `0.0030`, not `0.030`.
 * The mis-transcribed value is retained as [ROW_END_UNKNOWN_MARGIN_AS_FIRST_WRITTEN] and **both**
 * verdicts are emitted, because a threshold corrected after a sweep has to stay checkable against
 * the one registered before it. `C-0169` reports both and neither moves `V1`, `V3` or `V4`.
 */
const val ROW_END_UNKNOWN_MARGIN: Double = 0.0030284749

/**
 * The factor-of-ten mis-transcription of [ROW_END_UNKNOWN_MARGIN] that `T-9`'s Plan registered
 * before the sweep, retained so that the pre-registered verdict stays computable from the code
 * that reports the corrected one.
 */
const val ROW_END_UNKNOWN_MARGIN_AS_FIRST_WRITTEN: Double = 0.030

/**
 * `C-0015`'s own count effect — *"seven crossover columns instead of eight moves the peak
 * per-load-path force by 19 %"* — the smallest movement in that quantity the claim which owns the
 * registration design rule reports as material.
 */
const val REGISTRATION_FORCE_THRESHOLD: Double = 0.19

/**
 * The fraction of the whole present-versus-absent movement above which the response is a **ramp**
 * rather than a **step**, and therefore above which the binary reading of a constraint fails.
 *
 * `R = 0` is the binary reading exactly; `R = 1` is a link that does not exist.
 */
const val RAMP_FRACTION_THRESHOLD: Double = 0.05

/**
 * The lower end of `C-0099`'s channel-B bisection bracket, as a fraction of the penalty.
 *
 * Its sweep returned `[0, 0.015625]` and read it as locating a **discontinuity** rather than a
 * threshold — so this is the one interval of the penalty axis the corpus has never resolved.
 */
const val C0099_UNRESOLVED_PENALTY_FRACTION: Double = 0.015625

/**
 * The crossover's **vertical** displacement stiffness in `pN/nm` — `2αS/(100a)`.
 *
 * Chen et al.'s softened-bond construction (*JACS* **136**:6995) applied to the duplex **stretch**
 * modulus, which is `Gen1Tile.crossoverInPlaneStiffness` on the orthogonal axis and is asserted
 * equal to it.  **DERIVED, not measured**, and swept over [crossoverVerticalStiffnessSweep].
 */
fun crossoverVerticalStiffness(alpha: Double = 1.0): Double {
    require(alpha > 0.0) { "alpha must be positive, was: $alpha" }
    return 2.0 * alpha * Gen1Tile.DUPLEX_STRETCH_MODULUS / (100.0 * Gen1Tile.RISE_PER_BASE_PAIR)
}

/**
 * The hinge's own equivalent vertical stiffness `k_θ/d²` in `pN/nm` — the *other* element of the
 * crossover, converted onto the vertical coordinate by its own `d/2` lever on each side.
 *
 * This is the quantity [OrigamiGrillage.RIGID_LINK_STIFFNESS]'s KDoc compares itself against.
 */
fun hingeEquivalentVerticalStiffness(
    interhelicalDistance: Double = Gen1Tile.INTERHELICAL_SHEET,
    alpha: Double = 1.0
): Double {
    require(interhelicalDistance > 0.0) {
        "interhelicalDistance must be positive, was: $interhelicalDistance"
    }
    return Gen1Tile.crossoverHingeStiffness(alpha) / (interhelicalDistance * interhelicalDistance)
}

/** What fraction of [OrigamiGrillage.RIGID_LINK_STIFFNESS] a vertical stiffness [k] is. */
fun penaltyFractionOf(k: Double): Double {
    require(k > 0.0) { "k must be positive, was: $k" }
    return k / OrigamiGrillage.RIGID_LINK_STIFFNESS
}

/**
 * The four-decade sweep, in absolute `pN/nm` — `Gen1Tile.CROSSOVER_IN_PLANE_SWEEP` scaled by
 * [crossoverVerticalStiffness], so the vertical axis is reported exactly as `C-0020` reports the
 * in-plane one: as a function of a constant nobody has measured.
 */
fun crossoverVerticalStiffnessSweep(alpha: Double = 1.0): List<Double> =
    Gen1Tile.CROSSOVER_IN_PLANE_SWEEP.map { it * crossoverVerticalStiffness(alpha) }

/**
 * The **ramp fraction** `R = (D_phys − D_rigid)/(D_absent − D_rigid)` — how much of the whole
 * present-versus-absent movement the physical value already carries.
 *
 * The binary reading of a constraint asserts `R = 0`: the only two physical states are present and
 * absent, and everything between them is the flat top of a step.
 */
fun rampFraction(atPhysical: Double, atRigid: Double, atAbsent: Double): Double {
    val span = atAbsent - atRigid
    require(abs(span) > 1.0e-12) {
        "the present-versus-absent movement is zero, so a ramp fraction is not defined: " +
                "atRigid = $atRigid, atAbsent = $atAbsent"
    }
    return (atPhysical - atRigid) / span
}

/**
 * The four verdicts `T-9`'s Plan fixes **before** the sweep, so that none can be read off
 * afterwards.
 *
 * @property crossesFlatnessConvention `V1` — the rigid model is wrong for the flatness verdict.
 * @property movesMoreThanTheRowEndUnknown `V2` — material even if `V1` holds.
 * @property movesThePeakCrossoverForce `V3` — `C-0015`'s registration lever moves.
 * @property isARampNotAStep `V4` — the binary reading of the constraint is wrong.
 * @property binaryReadingIsRight the conjunction none of the four fired.
 */
@Serializable
data class VerticalComplianceVerdict(
    val crossesFlatnessConvention: Boolean,
    val movesMoreThanTheRowEndUnknown: Boolean,
    val movesMoreThanTheRowEndUnknownAsFirstWritten: Boolean,
    val movesThePeakCrossoverForce: Boolean,
    val isARampNotAStep: Boolean,
    val rampFraction: Double,
    val dishingMovement: Double,
    val dishingMovementOverTheRowEndUnknown: Double,
    val relativeForceMovement: Double
) {

    /** The conjunction none of the four fired, at the **corrected** `V2` threshold. */
    val binaryReadingIsRight: Boolean
        get() = !crossesFlatnessConvention && !movesMoreThanTheRowEndUnknown &&
                !movesThePeakCrossoverForce && !isARampNotAStep

    /** The same, at the threshold `T-9`'s Plan registered before the sweep. */
    val binaryReadingIsRightAsFirstWritten: Boolean
        get() = !crossesFlatnessConvention && !movesMoreThanTheRowEndUnknownAsFirstWritten &&
                !movesThePeakCrossoverForce && !isARampNotAStep

}

/** Evaluates `V1`–`V4` against the thresholds `T-9`'s Plan fixed. */
fun verticalComplianceVerdict(
    dishingAtPhysical: Double,
    dishingAtRigid: Double,
    dishingAtAbsent: Double,
    peakForceAtPhysical: Double,
    peakForceAtRigid: Double
): VerticalComplianceVerdict {
    require(peakForceAtRigid > 0.0) {
        "peakForceAtRigid must be positive, was: $peakForceAtRigid"
    }
    val ramp = rampFraction(dishingAtPhysical, dishingAtRigid, dishingAtAbsent)
    val movement = abs(dishingAtPhysical - dishingAtRigid)
    val force = abs(peakForceAtPhysical - peakForceAtRigid) / peakForceAtRigid
    return VerticalComplianceVerdict(
        crossesFlatnessConvention = dishingAtPhysical > FLATNESS_CONVENTION,
        movesMoreThanTheRowEndUnknown = movement > ROW_END_UNKNOWN_MARGIN,
        movesMoreThanTheRowEndUnknownAsFirstWritten =
            movement > ROW_END_UNKNOWN_MARGIN_AS_FIRST_WRITTEN,
        movesThePeakCrossoverForce = force > REGISTRATION_FORCE_THRESHOLD,
        isARampNotAStep = ramp > RAMP_FRACTION_THRESHOLD,
        rampFraction = ramp,
        dishingMovement = movement,
        dishingMovementOverTheRowEndUnknown = movement / ROW_END_UNKNOWN_MARGIN,
        relativeForceMovement = force
    )
}
