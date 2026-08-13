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
import com.xemantic.nano.plentyofroom.coupling.CouplingReaction
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sign
import kotlin.math.sqrt

/**
 * Task `T-23` — a **two-sided** compliant DNA coupling: an element that carries load in both
 * directions, so that `T-16`'s coupling stiffness and `T-13`'s hold-down become one part
 * instead of two.
 *
 * ## The currency, before any element
 *
 * `C-0021` derives the hold-down requirement as a **force**, `F ≥ k_BT/σ = 1.3806 pN`, and its
 * derivation says why: above `L₀` a non-adsorbing layer contributes nothing, so a constant
 * hold-down confines the tile through a **linear** potential and the upward excursion is
 * exponentially distributed. **That is a property of a one-sided stack, not of the problem.**
 * A two-sided coupling of stiffness `k` makes the same potential **quadratic** there, and the
 * requirement becomes `k ≥ k_BT/σ² = 0.4602 pN/nm` — leaf `A1.1`'s own bound.
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;**`F_req = k_req·σ` identically: two-sidedness is worth exactly one
 * power of the position bound.**
 *
 * See [holdDownForceScale] (the one-sided reading, `C-0021`'s) against
 * [com.xemantic.nano.plentyofroom.equipartitionStiffness] (the two-sided one), and
 * [oneSidedExcursionRms] against [twoSidedExcursionRms].
 *
 * ## The sidedness argument, which decides the catalogue before any arithmetic
 *
 * > **An element loaded along its own axis must choose.** A hybridised duplex is `S/L` —
 * > 220 pN/nm at 5 nm, two-sided and 297× too stiff (`C-0017`'s `K1`). A single strand is
 * > compliant and carries no compression at all. There is nothing in between **on that axis**,
 * > because axial compliance in DNA is entropic, and entropy only pulls.
 * >
 * > **An element loaded transverse to its axis, or through a hinge, does not have to choose**,
 * > because its compliance is *bending* and a bending moment is signed. `c EI/L³` and `k_θ/r²`
 * > are as small as the designer makes `L` and `r`.
 *
 * ## The convention this file adds
 *
 * Every element below is a law in a **signed** displacement `δ`, the tile's displacement from
 * the element's own unstressed configuration, **positive downward**; the reaction is positive
 * **upward**, i.e. resisting descent, exactly as in `C-0017`. A one-sided element has
 * `R(δ) = 0` for all `δ ≤ 0`; a two-sided one has `R(δ) < 0` there. **Sidedness is therefore
 * tested by evaluating the law at negative argument** ([carriesCompression]) and never by
 * inspecting the geometry — which is what makes `E1`'s pass and `E2`'s failure results rather
 * than assumptions.
 */

// ---------------------------------------------------------------- the signed interface

/**
 * One coupling element, as a **signed** force-extension law.
 *
 * This is deliberately *not* `C-0017`'s [CouplingReaction], whose argument is the actuator
 * stroke and which is therefore only ever evaluated at `s ≥ 0`. The whole question of `T-23`
 * lives at negative argument.
 */
interface SignedCouplingElement {

    /** `R(δ)` in pN, positive **upward** (resisting descent), at signed displacement [displacement] nm. */
    fun reaction(displacement: Double): Double

    /** `dR/dδ` in `pN/nm` at [displacement] — the quantity the stability condition is written on. */
    fun tangentStiffness(displacement: Double): Double

    /**
     * `R(δ)/δ` in `pN/nm` — the quantity the **placement** condition is written on (`C-0017`).
     *
     * For a strain-stiffening element this is strictly below [tangentStiffness], and the whole
     * `tangent/secant` ratio is free stability margin at zero placement cost.
     */
    fun secantStiffness(displacement: Double): Double {
        require(displacement != 0.0) { "a secant stiffness is undefined at zero displacement" }
        return reaction(displacement) / displacement
    }
}

/**
 * Whether [element] pushes back when the tile rises by [probe] nm — the **operational
 * definition of two-sidedness**, and the one test that separates `E2` from everything else.
 */
fun carriesCompression(element: SignedCouplingElement, probe: Double): Boolean {
    require(probe > 0.0) { "probe must be positive, was: $probe" }
    return element.reaction(-probe) < 0.0
}

// ---------------------------------------------------------------- E1, the axial standoff

/**
 * `E1` — a hybridised duplex standoff of stretch modulus [stretchModulus] pN and [length] nm,
 * loaded **along its axis**: `k = S/L`, two-sided and linear.
 *
 * It is in the catalogue as the null hypothesis, and it passes the sidedness test: DNA's
 * stiffest element *is* two-sided. It is excluded on **stiffness** — `C-0017`'s `K1`, 45 of
 * them at 5 nm are 9900 pN/nm, 297× §3's mandate — and that is exactly the point. Sidedness
 * and compliance are not scarce separately; they are scarce **together, on the axial axis**.
 *
 * In compression it is a column, so [eulerBucklingLoad] bounds how much push it can carry;
 * at the loads a two-sided coupling actually sees (the thermal `√(k_BT k)/n`) that bound is
 * two to three orders away, which is checked rather than assumed.
 */
class AxialDuplexStandoff(
    val stretchModulus: Double,
    val length: Double
) : SignedCouplingElement {

    init {
        require(stretchModulus > 0.0) { "stretchModulus must be positive, was: $stretchModulus" }
        require(length > 0.0) { "length must be positive, was: $length" }
    }

    val stiffness: Double get() = rodAxialStiffness(stretchModulus, length)

    override fun reaction(displacement: Double): Double = stiffness * displacement

    override fun tangentStiffness(displacement: Double): Double = stiffness
}

// ---------------------------------------------------------------- E2, the one-sided spacer

/**
 * `E2` — any of `C-0017`'s tension-only paths (`K2`'s standoff-plus-spacer chain), wrapped in
 * the signed interface so that its **one-sidedness is evaluated rather than asserted**.
 *
 * `R(δ) = 0` and `dR/dδ = 0` for every `δ ≤ 0`: a single strand carries no compression, and a
 * slack chain supplies exactly zero tangent — `C-0017`'s own sentence, *"an element that goes
 * slack as the tile descends supplies exactly nothing"*, read in the other direction.
 */
class OneSidedSpacer(
    val path: CouplingReaction
) : SignedCouplingElement {

    override fun reaction(displacement: Double): Double =
        if (displacement <= 0.0) 0.0 else path.reaction(displacement)

    override fun tangentStiffness(displacement: Double): Double =
        if (displacement <= 0.0) 0.0 else path.tangentStiffness(displacement)
}

// ---------------------------------------------------------------- E3, the transverse flexure

/**
 * The two end conditions a flexure spanning between two lever posts can plausibly have, and the
 * `c` in `k = c EI/L³` for a load applied at midspan.
 *
 * Carried as a **set** rather than chosen, for `C-0014`'s reason: an origami-to-superstructure
 * joint is not obviously either, and here they differ by **exactly 4** — which is asserted as
 * a limiting case rather than trusted.
 *
 * @property midspanFactor the `c` in `k = c EI/L³`.
 * @property drawInFactor the `g` in `Δ = g δ²/L`, the total axial draw-in of the two ends.
 *   It is **2.4 for both**, which is not obvious: the pinned cubic shape and the clamped
 *   cubic-Hermite shape have different curvature distributions and the same arc-length excess.
 */
enum class FlexureEndCondition(
    val midspanFactor: Double,
    val drawInFactor: Double,
    val description: String
) {

    /** Simply supported at both ends, load at midspan: `48 EI/L³`. */
    PINNED_ENDS(48.0, 2.4, "simply supported at both ends, load at midspan"),

    /** Built in at both ends, load at midspan: `192 EI/L³` — exactly 4× the pinned case. */
    CLAMPED_ENDS(192.0, 2.4, "built in at both ends, load at midspan")
}

/**
 * `E3` — a duplex of bending rigidity [bendingRigidity] `pN·nm²` spanning [span] nm between two
 * posts of the lever, tied to the tile at its **midspan** and therefore loaded **transverse to
 * its own axis**.
 *
 * Two-sided by construction: a beam bends either way and its law is **odd**, which is asserted
 * rather than assumed. Its compliance is `L³/(c EI)`, so the span is the design variable and it
 * can be made as compliant as required — which is the whole escape from the axial trade-off.
 *
 * ## The membrane term, and why it is a bracket rather than a correction
 *
 * If the two ends are held **axially**, the beam cannot deflect without stretching, and the
 * membrane (cable) term `C-0014` found for in-plane tethers reappears in the normal direction:
 * each half-span behaves as a tie of length `L/2` with a transverse offset `δ`, so
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`T = S(√((L/2)² + δ²) − L/2)/(L/2)`, &nbsp;&nbsp;
 * `F_m = 2 T δ/√((L/2)² + δ²)`,
 *
 * built from `C-0014`'s own [cableTension] and [cableNormalForce] rather than re-derived. It is
 * cubic in `δ`, so it is invisible in a linearised budget and dominant at §3's 3 nm — and it
 * makes the element **convex**, `tangent/secant > 1`, which `C-0017` shows is free stability
 * margin.
 *
 * If instead the ends can **draw in** by [endDrawIn] — `2.4 δ²/L`, of order one nanometre at
 * the working point — the term is absent and the element is exactly linear. **Which of the two
 * applies is a design choice about how the ends are built, not a quantity to be measured**, so
 * both are carried and the difference is reported.
 */
class TransverseDuplexFlexure(
    val bendingRigidity: Double,
    val span: Double,
    val endCondition: FlexureEndCondition,
    val axiallyRestrained: Boolean,
    val stretchModulus: Double = AnchorMaterials.DUPLEX_STRETCH_MODULUS
) : SignedCouplingElement {

    init {
        require(bendingRigidity > 0.0) {
            "bendingRigidity must be positive, was: $bendingRigidity"
        }
        require(span > 0.0) { "span must be positive, was: $span" }
        require(stretchModulus > 0.0) { "stretchModulus must be positive, was: $stretchModulus" }
    }

    /** Half the span — the length of the tie each half of the beam becomes in the membrane term. */
    private val half: Double get() = span / 2.0

    /** `c EI/L³` in `pN/nm`, the linear part, and the whole of the element when it can draw in. */
    val bendingStiffness: Double
        get() = endCondition.midspanFactor * bendingRigidity / (span * span * span)

    /** The membrane part of the reaction in pN at signed [displacement], odd and cubic. */
    fun membraneForce(displacement: Double): Double {
        if (!axiallyRestrained || displacement == 0.0) return 0.0
        return sign(displacement) * 2.0 *
                cableNormalForce(stretchModulus, half, abs(displacement))
    }

    /**
     * The axial tension in pN the beam carries at [displacement] — **even**, and the quantity
     * that must be judged against `C-0006`'s allowables, because it is what a joint at the
     * beam's end actually feels. Zero when the ends can draw in.
     */
    fun axialTension(displacement: Double): Double =
        if (!axiallyRestrained) 0.0 else cableTension(stretchModulus, half, abs(displacement))

    /**
     * The total axial draw-in of the two ends in nm, `g δ²/L` — the demand a **free** flexure
     * places on its end joints, and the thing that has to be accommodated for the membrane term
     * to be absent. Quoted in nm here; the study quotes it in base pairs, which is the unit a
     * design has.
     */
    fun endDrawIn(displacement: Double): Double =
        endCondition.drawInFactor * displacement * displacement / span

    override fun reaction(displacement: Double): Double =
        bendingStiffness * displacement + membraneForce(displacement)

    /**
     * `dR/dδ`, analytic: `c EI/L³ + (2S/a)(1 − a³/r³)` with `a = L/2`, `r = √(a² + δ²)`.
     *
     * The membrane part is **zero at zero deflection**, which is why an axially restrained beam
     * is indistinguishable from a free one in any linearised budget — and 6.8× stiffer at §3's
     * working point.
     */
    override fun tangentStiffness(displacement: Double): Double {
        if (!axiallyRestrained) return bendingStiffness
        val a = half
        val r = sqrt(a * a + displacement * displacement)
        return bendingStiffness + 2.0 * stretchModulus / a * (1.0 - a * a * a / (r * r * r))
    }
}

// ---------------------------------------------------------------- E5, the hinge flexure

/**
 * `E5` — [hingeCount] antiparallel crossovers acting as a torsional spring of constant
 * [hingeStiffness] `pN·nm/rad` each, on an arm of [armLength] nm reaching the tile.
 *
 * Two-sided and linear: a hinge resists rotation either way. Its compliances add,
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`1/k = r²/(n k_θ) + r³/(c EI)`,
 *
 * the first term being the hinge and the second the arm's own bending, so the design variable
 * is the arm and the answer scales as `r ∝ √(k_θ)` — **a 2× uncertainty in the hinge constant
 * is 1.41× in a length the designer chooses anyway**, which is why this element does not have
 * to wait for `T-9`.
 *
 * `k_θ = 2αB/(100a)` is `C-0009`'s **cited, fitted** constant (Chen et al., *JACS* **136**:6995,
 * 2014, SI §S2) with its own experimental bracket `α ∈ [0.6, 1.2]`. It is the **only** crossover
 * elastic constant anyone has fitted, and this element is the one place in the programme where
 * that is an advantage rather than an exposure: it is used here as a *spring*, which is what it
 * was fitted as.
 */
class CrossoverHingeFlexure(
    val hingeStiffness: Double,
    val armLength: Double,
    val armBendingRigidity: Double,
    val hingeCount: Int = 1,
    val armFactor: Double = 3.0
) : SignedCouplingElement {

    init {
        require(hingeStiffness > 0.0) { "hingeStiffness must be positive, was: $hingeStiffness" }
        require(armLength > 0.0) { "armLength must be positive, was: $armLength" }
        require(armBendingRigidity > 0.0) {
            "armBendingRigidity must be positive, was: $armBendingRigidity"
        }
        require(hingeCount > 0) { "hingeCount must be positive, was: $hingeCount" }
        require(armFactor > 0.0) { "armFactor must be positive, was: $armFactor" }
    }

    /** `n k_θ/r²` in `pN/nm` — the hinge's own contribution, before the arm is put in series. */
    val hingeTermStiffness: Double
        get() = hingeCount * hingeStiffness / (armLength * armLength)

    /** `c EI/r³` in `pN/nm` — the arm's own bending, the other half of the series chain. */
    val armTermStiffness: Double
        get() = armFactor * armBendingRigidity / (armLength * armLength * armLength)

    /** The series stiffness in `pN/nm`, and leaf `A8.2`'s question for this element. */
    val stiffness: Double
        get() = 1.0 / (1.0 / hingeTermStiffness + 1.0 / armTermStiffness)

    /** The hinge's share of the path compliance, dimensionless — `A8.2`'s explicit ask. */
    val hingeComplianceShare: Double
        get() = (1.0 / hingeTermStiffness) / (1.0 / hingeTermStiffness + 1.0 / armTermStiffness)

    override fun reaction(displacement: Double): Double = stiffness * displacement

    override fun tangentStiffness(displacement: Double): Double = stiffness

    /** The moment in `pN·nm` carried by the hinge at [displacement] — `R·r`, distributed over [hingeCount]. */
    fun hingeMoment(displacement: Double): Double =
        abs(reaction(displacement)) * armLength / hingeCount

    /**
     * The force in pN on one crossover's backbone bonds, resolving the hinge moment into a
     * couple over [leverSeparation] nm — the duplex diameter or the interhelical distance,
     * carried as a bracket because the two differ by 1.35× and neither is obviously right.
     */
    fun hingeBondForce(displacement: Double, leverSeparation: Double): Double {
        require(leverSeparation > 0.0) {
            "leverSeparation must be positive, was: $leverSeparation"
        }
        return hingeMoment(displacement) / leverSeparation
    }
}

// ---------------------------------------------------------------- E4, the antagonistic pair

/**
 * `E4` — [upCount] tension-only spacers reaching a lever **above** the tile, pre-extended by
 * [upPreExtension] nm, opposed by [downCount] tension-only tethers reaching ground **below** it
 * across a span of [downSpan] nm.
 *
 * **Two-sided as a pair, though neither part is** — and the structural reason is worth stating,
 * because it is what makes the topology usable at all:
 *
 * > **the pair's preload is the DIFFERENCE of its two tensions and its stiffness is their SUM**,
 * > so the hold-down and the coupling stiffness are independent design variables even though
 * > each part supplies both.
 *
 * The cost is that the difference is taken between two quantities that are each carried by real
 * load paths: a pair delivering a 1.4 pN preload from two 50 pN limbs has put 50 pN on every
 * path to deliver 1.4 pN of hold-down. That **circulating tension** is the price of the
 * topology and the study reports it.
 */
class AntagonisticSpacerPair(
    val upCount: Int,
    val upChain: FreelyJointedChain,
    val upPreExtension: Double,
    val downCount: Int,
    val downChain: FreelyJointedChain,
    val downSpan: Double
) : SignedCouplingElement {

    init {
        require(upCount > 0) { "upCount must be positive, was: $upCount" }
        require(downCount > 0) { "downCount must be positive, was: $downCount" }
        require(upPreExtension >= 0.0) {
            "upPreExtension must not be negative, was: $upPreExtension"
        }
        require(downSpan > 0.0) { "downSpan must be positive, was: $downSpan" }
    }

    /** The tension in pN in one up-spacer at [displacement], which **extends** as the tile descends. */
    fun upTension(displacement: Double): Double =
        upChain.tension(max(0.0, upPreExtension + displacement))

    /** The tension in pN in one down-tether, whose span **shortens** as the tile descends. */
    fun downTension(displacement: Double): Double =
        downChain.tension(max(0.0, downSpan - displacement))

    /** The larger of the two limb tensions in pN — the per-path force the pair actually costs. */
    fun circulatingTension(displacement: Double): Double =
        max(upTension(displacement), downTension(displacement))

    override fun reaction(displacement: Double): Double =
        upCount * upTension(displacement) - downCount * downTension(displacement)

    override fun tangentStiffness(displacement: Double): Double =
        upCount * upChain.tangentStiffness(upTension(displacement)) +
                downCount * downChain.tangentStiffness(downTension(displacement))
}

// ---------------------------------------------------------------- the assembled coupling

/**
 * [count] parallel copies of a signed [element], mounted so that the element is unstressed when
 * the tile sits [unstressedOffset] nm **below** `L₀`.
 *
 * The offset is the design's **mounting preload**, and it is a *length*, which is what makes it
 * quantised: the smallest one a DNA design can set is a base-pair rise. `R(s) = n f(s − q)`,
 * so `R(0) = n f(−q) < 0` — a downward preload — and `R(q) = 0` exactly.
 *
 * Implements `C-0017`'s [CouplingReaction] so that it can be dropped into that task's own
 * operating-point solver unchanged, and [SignedCouplingElement] so that its sidedness can be
 * probed at negative stroke, which [CouplingReaction] alone never is.
 */
class TwoSidedCoupling(
    val count: Int,
    val element: SignedCouplingElement,
    val unstressedOffset: Double = 0.0
) : CouplingReaction, SignedCouplingElement {

    init {
        require(count > 0) { "count must be positive, was: $count" }
        require(unstressedOffset >= 0.0) {
            "unstressedOffset must not be negative, was: $unstressedOffset"
        }
    }

    override fun reaction(stroke: Double): Double =
        count * element.reaction(stroke - unstressedOffset)

    override fun tangentStiffness(stroke: Double): Double =
        count * element.tangentStiffness(stroke - unstressedOffset)

    /** The downward preload in pN the coupling carries at zero stroke — `C-0021`'s `F_down`. */
    val preload: Double get() = -reaction(0.0)
}

// ---------------------------------------------------------------- the design roots

/**
 * The span in nm at which [count] transverse flexures present a **secant** stiffness of
 * [targetStiffness] `pN/nm` at [workingDisplacement] — §3's placement condition, solved as a
 * root and never as a stiffness read off a formula.
 *
 * The per-path secant is `(c EI δ + membrane(δ))/(L³ δ)` up to the membrane term's own weak
 * dependence, i.e. strictly decreasing in the span, so a bracketing scan followed by bisection
 * is safe. Exits on the **bracket width**, never on a residual (`CLAUDE.md`).
 */
fun flexureSpanForStiffness(
    bendingRigidity: Double,
    endCondition: FlexureEndCondition,
    axiallyRestrained: Boolean,
    stretchModulus: Double,
    count: Int,
    targetStiffness: Double,
    workingDisplacement: Double,
    scanSteps: Int = 256
): Double {
    require(count > 0) { "count must be positive, was: $count" }
    require(targetStiffness > 0.0) { "targetStiffness must be positive, was: $targetStiffness" }
    require(workingDisplacement > 0.0) {
        "workingDisplacement must be positive, was: $workingDisplacement"
    }
    require(scanSteps >= 16) { "scanSteps must be at least 16, was: $scanSteps" }
    fun assembled(span: Double): Double = count * TransverseDuplexFlexure(
        bendingRigidity, span, endCondition, axiallyRestrained, stretchModulus
    ).secantStiffness(workingDisplacement)
    var low = max(workingDisplacement * 1.0e-3, 1.0e-3)
    var high = low
    // the stiffness falls with the span, so grow the span until it is below the target
    var grown = 0
    while (assembled(high) >= targetStiffness && grown < 400) {
        high *= 1.5
        grown++
    }
    require(assembled(high) < targetStiffness) {
        "no span reaches a stiffness as low as $targetStiffness"
    }
    // scan inward first, so that the bisection starts inside a bracket rather than assuming one
    val step = (high - low) / scanSteps
    var scan = low
    for (i in 1..scanSteps) {
        val next = scan + step
        if (assembled(next) < targetStiffness) {
            low = scan
            high = next
            break
        }
        scan = next
    }
    repeat(400) {
        val middle = 0.5 * (low + high)
        if (assembled(middle) > targetStiffness) low = middle else high = middle
        if (high - low <= 1.0e-15 * high) return 0.5 * (low + high)
    }
    return 0.5 * (low + high)
}

/**
 * The arm length in nm at which [count] crossover-hinge flexures present [targetStiffness]
 * `pN/nm`. Linear, so the secant and the tangent are the same number and the root is unique.
 */
fun hingeArmForStiffness(
    hingeStiffness: Double,
    armBendingRigidity: Double,
    count: Int,
    targetStiffness: Double,
    hingeCount: Int = 1,
    armFactor: Double = 3.0
): Double {
    require(count > 0) { "count must be positive, was: $count" }
    require(targetStiffness > 0.0) { "targetStiffness must be positive, was: $targetStiffness" }
    fun assembled(arm: Double): Double = count * CrossoverHingeFlexure(
        hingeStiffness, arm, armBendingRigidity, hingeCount, armFactor
    ).stiffness
    var low = 1.0e-3
    var high = 1.0e-3
    var grown = 0
    while (assembled(high) >= targetStiffness && grown < 400) {
        high *= 1.5
        grown++
    }
    require(assembled(high) < targetStiffness) {
        "no arm reaches a stiffness as low as $targetStiffness"
    }
    repeat(400) {
        val middle = 0.5 * (low + high)
        if (assembled(middle) > targetStiffness) low = middle else high = middle
        if (high - low <= 1.0e-15 * high) return 0.5 * (low + high)
    }
    return 0.5 * (low + high)
}

// ---------------------------------------------------------------- the mounting offset

/**
 * The downward preload in pN a two-sided coupling carries at zero stroke when it is mounted
 * unstressed [offset] nm below `L₀`, given that its operating point must still sit at
 * [targetStroke] with [targetForce] delivered there:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`k = F/(δ − q)` and **`F_down = F q/(δ − q)`**.
 *
 * Algebraically identical to `C-0021`'s [couplingPreloadForStiffness] — asserted equal to it as
 * a gate-3 test, **absolutely and in pN**, because near the mandate the two are a catastrophic
 * cancellation of each other — but written on the variable a *design* has. `q` is a length, and
 * the smallest non-zero length a DNA design can set is a base-pair rise.
 */
fun mountingOffsetPreload(offset: Double, targetForce: Double, targetStroke: Double): Double {
    require(offset >= 0.0) { "offset must not be negative, was: $offset" }
    require(targetStroke > offset) {
        "offset $offset must be below the target stroke $targetStroke"
    }
    require(targetForce > 0.0) { "targetForce must be positive, was: $targetForce" }
    return targetForce * offset / (targetStroke - offset)
}

/** The coupling stiffness in `pN/nm` that mounting offset implies: `k = F/(δ − q)`. */
fun mountingOffsetStiffness(offset: Double, targetForce: Double, targetStroke: Double): Double {
    require(offset >= 0.0) { "offset must not be negative, was: $offset" }
    require(targetStroke > offset) {
        "offset $offset must be below the target stroke $targetStroke"
    }
    require(targetForce > 0.0) { "targetForce must be positive, was: $targetForce" }
    return targetForce / (targetStroke - offset)
}

/** The mounting offset in nm that delivers [preload] pN — the exact inverse of [mountingOffsetPreload]. */
fun offsetForPreload(preload: Double, targetForce: Double, targetStroke: Double): Double {
    require(preload >= 0.0) { "preload must not be negative, was: $preload" }
    require(targetForce > 0.0) { "targetForce must be positive, was: $targetForce" }
    require(targetStroke > 0.0) { "targetStroke must be positive, was: $targetStroke" }
    return targetStroke * preload / (targetForce + preload)
}

// ---------------------------------------------------------------- the two readings

/**
 * The RMS upward excursion in nm of a tile held above `L₀` by a **constant** hold-down [force],
 * i.e. by a one-sided stack: `√2 k_BT/F`, exactly, because the potential is linear there and
 * the distribution exponential (`C-0021`).
 */
fun oneSidedExcursionRms(force: Double, temperature: Double = ROOM_TEMPERATURE): Double {
    require(force > 0.0) { "force must be positive, was: $force" }
    return sqrt(2.0) * thermalEnergy(temperature) / force
}

/**
 * The RMS excursion in nm of a tile held by a **two-sided** coupling of [stiffness]:
 * `√(k_BT/k)`, equipartition, because a two-sided element makes the potential quadratic on
 * *both* sides of the rest position rather than only below it.
 *
 * The whole of `T-23`'s cheap bound is the comparison of this with [oneSidedExcursionRms], and
 * the requirement it implies with `C-0021`'s: `F_req = k_req·σ`.
 */
fun twoSidedExcursionRms(stiffness: Double, temperature: Double = ROOM_TEMPERATURE): Double {
    require(stiffness > 0.0) { "stiffness must be positive, was: $stiffness" }
    return sqrt(thermalEnergy(temperature) / stiffness)
}
