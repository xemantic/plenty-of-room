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

import com.xemantic.nano.plentyofroom.anchoring.FreelyJointedChain
import kotlinx.serialization.Serializable
import kotlin.math.max
import kotlin.math.min

/**
 * Task `T-16` — the **requirement** an output coupling has to meet. Leaf `A8.2`.
 *
 * ## Two conditions, and `C-0012` states one of them
 *
 * `C-0012` gives *"the number an output coupling has to supply"* as `|k_eff|` at the held gap
 * `L₀ − 3 nm`. That is a **stability** threshold: below it the operating point is a maximum of
 * the potential rather than a minimum. It is necessary and it is not sufficient, because a
 * stiffness alone does not say *where* the operating point is.
 *
 * The coupling is a **load line** drawn across the actuator's characteristic. With
 * `R(s) = R₀ + k_c s` the reaction of the coupling at stroke `s` (positive **upward**, i.e.
 * resisting descent), the operating point is the first root of
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`W(s) = R(s)`, &nbsp; and it is stable iff &nbsp; `k_c > −dW/ds = |k_eff|`.
 *
 * ## The stiffness §3 fixes on its own, before any physics
 *
 * The force delivered *to the load* between the unbiased and the biased state is
 * `R(s₁) − R(s₀) = k_c (s₁ − s₀)` — **independent of the preload `R₀`**. So §3's own two
 * numbers, 100 pN and 3 nm, fix the coupling stiffness by arithmetic:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;**`k_c* = 100 pN / 3 nm = 33.333… pN/nm`.**
 *
 * That is the cheap bound this task runs first, and it inverts the question. A DNA duplex in
 * tension is `S/L = 110 pN/nm` at 10 nm and forty-five of them are 4950 — so the interesting
 * question is not whether a DNA-origami coupling can be **stiff enough** but whether it can be
 * made **compliant enough**, and whether 33.3 pN/nm is enough to stabilise.
 *
 * ## What is NOT done here
 *
 * A stroke is never a force divided by a stiffness (`C-0012`): three of `C-0003`'s six layer
 * models have exactly zero stiffness at `L₀`. Every stroke below is a **root**.
 */

// ---------------------------------------------------------------- the mandated stiffness

/**
 * §3's own coupling stiffness in `pN/nm`: [targetForce] delivered over [targetStroke].
 *
 * Preload-free, model-free and physics-free — the delivered force is `k_c Δs` whatever the
 * coupling already carries at zero stroke. 100 pN over 3 nm is **33.333… pN/nm**.
 */
fun mandatedCouplingStiffness(targetForce: Double, targetStroke: Double): Double {
    require(targetForce > 0.0) { "targetForce must be positive, was: $targetForce" }
    require(targetStroke > 0.0) { "targetStroke must be positive, was: $targetStroke" }
    return targetForce / targetStroke
}

/**
 * The preload in pN a coupling of [stiffness] must already carry at zero stroke for its
 * operating point to sit at [stroke], given the actuator's [outputForce] there:
 * `R₀ = k_c s − W(s)`.
 *
 * Positive means the coupling holds the tile **up** at zero stroke, which nothing in the §3
 * stack does; negative means it pulls the tile **down**, which is `T-13`'s open question and
 * is what a coupling stiffer than the placement value needs.
 */
fun placementPreload(stiffness: Double, stroke: Double, outputForce: Double): Double {
    require(stiffness >= 0.0) { "stiffness must not be negative, was: $stiffness" }
    require(stroke > 0.0) { "stroke must be positive, was: $stroke" }
    return stiffness * stroke - outputForce
}

// ---------------------------------------------------------------- the characteristic

/**
 * The actuator's force-displacement characteristic `W(s)` in pN at stroke `s` in nm —
 * `C-0012`'s own object, `W(s) = |F_es(L₀−s, V)| − P(L₀−s)·A`.
 *
 * Its value at zero is the blocking force, its root is the free stroke, and its slope is
 * `−k_eff` exactly.
 */
fun interface OutputCharacteristic {

    /** `W(s)` in pN. */
    fun outputForce(stroke: Double): Double
}

// ---------------------------------------------------------------- the load line

/**
 * Whatever the tile pushes against — the lever, its joints, and every anchor in parallel.
 *
 * The sign convention is fixed here and used everywhere below: the **reaction is positive
 * upward**, i.e. it resists the tile's motion toward the electrode. A coupling supplies
 * stabilising stiffness only through `dR/ds > 0`; an element that goes slack as the tile
 * descends has `dR/ds = 0` and supplies exactly nothing.
 */
interface CouplingReaction {

    /** `R(s)` in pN at stroke [stroke] nm. */
    fun reaction(stroke: Double): Double

    /** `dR/ds` in `pN/nm` at [stroke] — the quantity the stability condition is written on. */
    fun tangentStiffness(stroke: Double): Double

    /**
     * The largest stroke the coupling can accommodate at all, in nm.
     *
     * Infinite for a linear element; the contour length for an entropic one, which is a
     * **hard stop** rather than a modelling limit.
     */
    val maximumStroke: Double get() = Double.POSITIVE_INFINITY
}

/** A linear coupling of stiffness `k_c` carrying [preload] pN at zero stroke. */
@Serializable
data class LinearCoupling(
    val stiffness: Double,
    val preload: Double = 0.0
) : CouplingReaction {

    init {
        require(stiffness >= 0.0) { "stiffness must not be negative, was: $stiffness" }
    }

    override fun reaction(stroke: Double): Double = preload + stiffness * stroke

    override fun tangentStiffness(stroke: Double): Double = stiffness

    /**
     * The force delivered to the load between strokes [from] and [to], `k_c (s₁ − s₀)`.
     *
     * **Independent of the preload**, which is why §3's two numbers fix `k_c` without any
     * statement about where the tile sits at zero bias.
     */
    fun deliveredForce(from: Double, to: Double): Double = stiffness * (to - from)
}

/**
 * `n` parallel entropic (single-stranded DNA) spacers, each stretched by exactly the stroke.
 *
 * The reaction is `n f(s)` with `f` the chain's force-extension law, so it is **zero at zero
 * stroke** — an unpreloaded coupling — and strain-stiffens toward the contour length. Its
 * secant and its tangent are different numbers and the design uses both: the secant over the
 * stroke is what delivers §3's force, the tangent at the working point is what stabilises.
 */
class EntropicCoupling(
    val count: Int,
    val chain: FreelyJointedChain
) : CouplingReaction {

    init {
        require(count > 0) { "count must be positive, was: $count" }
    }

    override fun reaction(stroke: Double): Double =
        if (stroke <= 0.0) 0.0 else count * chain.tension(stroke)

    override fun tangentStiffness(stroke: Double): Double =
        count * chain.tangentStiffness(if (stroke <= 0.0) 0.0 else chain.tension(stroke))

    /** The contour length: the tile physically cannot descend past it. */
    override val maximumStroke: Double get() = chain.contourLength
}

/**
 * `n` parallel paths, each a linear element of stiffness [linearStiffness] `pN/nm` **in series
 * with** an entropic spacer — the buildable form of an ssDNA-tuned coupling, in which the
 * linear element is the hybridised standoff and the spacer is what sets the stiffness.
 *
 * The two elements share the stroke, so the reaction at [stroke] is `n f` where `f` solves
 * `f/k_lin + x(f) = s`. Bisected on the **bracket width**, never on a residual.
 */
class SeriesEntropicCoupling(
    val count: Int,
    val linearStiffness: Double,
    val chain: FreelyJointedChain
) : CouplingReaction {

    init {
        require(count > 0) { "count must be positive, was: $count" }
        require(linearStiffness > 0.0) {
            "linearStiffness must be positive, was: $linearStiffness"
        }
    }

    /** The per-path tension in pN at [stroke], the root of `f/k_lin + x(f) = s`. */
    fun tension(stroke: Double): Double {
        if (stroke <= 0.0) return 0.0
        var low = 0.0
        var high = 1.0
        while (extensionAt(high) < stroke) high *= 2.0
        repeat(200) {
            val middle = 0.5 * (low + high)
            if (extensionAt(middle) < stroke) low = middle else high = middle
            if (high - low <= 1e-14 * max(high, 1.0)) return 0.5 * (low + high)
        }
        return 0.5 * (low + high)
    }

    private fun extensionAt(force: Double): Double =
        force / linearStiffness + chain.extension(force)

    override fun reaction(stroke: Double): Double =
        if (stroke <= 0.0) 0.0 else count * tension(stroke)

    override fun tangentStiffness(stroke: Double): Double {
        val force = tension(stroke)
        return count * seriesStiffness(listOf(linearStiffness, chain.tangentStiffness(force)))
    }

    /** The linear element still extends past the chain's contour, so the stop is softened. */
    override val maximumStroke: Double get() = Double.POSITIVE_INFINITY
}

// ---------------------------------------------------------------- the operating point

/**
 * The first root of `R(s) = W(s)` in nm, scanning **downward from zero stroke** and bisecting
 * inside the first bracket — the same construction `C-0012` uses and for the same reason: the
 * characteristic is not monotone, so bisection over the whole interval would not be safe.
 *
 * Returns `null` when the load line never meets the characteristic, which is the physical
 * statement that the coupling is too soft to hold the tile anywhere: it runs away.
 *
 * Exits on the **bracket width**, never on a residual (`CLAUDE.md`).
 */
fun firstOperatingStroke(
    characteristic: OutputCharacteristic,
    coupling: CouplingReaction,
    maximumStroke: Double,
    scanSteps: Int = 4096
): Double? {
    require(maximumStroke > 0.0) { "maximumStroke must be positive, was: $maximumStroke" }
    require(scanSteps >= 16) { "scanSteps must be at least 16, was: $scanSteps" }
    val ceiling = min(maximumStroke, coupling.maximumStroke * (1.0 - 1e-9))
    if (ceiling <= 0.0) return null
    fun gap(stroke: Double): Double = coupling.reaction(stroke) - characteristic.outputForce(stroke)
    var low = 0.0
    var atLow = gap(0.0)
    if (atLow == 0.0) return 0.0
    val step = ceiling / scanSteps
    for (i in 1..scanSteps) {
        val high = if (i == scanSteps) ceiling else i * step
        val atHigh = gap(high)
        if (atLow < 0.0 && atHigh >= 0.0 || atLow > 0.0 && atHigh <= 0.0) {
            var left = low
            var right = high
            repeat(200) {
                val middle = 0.5 * (left + right)
                if (gap(middle) * atLow > 0.0) left = middle else right = middle
                if (right - left <= 1e-14 * max(right, 1.0)) return 0.5 * (left + right)
            }
            return 0.5 * (left + right)
        }
        low = high
        atLow = atHigh
    }
    return null
}

// ---------------------------------------------------------------- the window

/**
 * The coupling requirement at one solved state, in the two conditions it actually has.
 *
 * @property targetStroke §3's stroke, 3 nm.
 * @property outputForceAtTarget `W(s*)` in pN — what the actuator delivers at that stroke.
 * @property effectiveStiffnessAtTarget `k_eff(L₀ − s*)` in `pN/nm`, **signed**, per `C-0012`.
 * @property mandatedStiffness `k_c* = F/δ` in `pN/nm`, from §3 alone.
 */
@Serializable
data class CouplingWindow(
    val targetStroke: Double,
    val outputForceAtTarget: Double,
    val effectiveStiffnessAtTarget: Double,
    val mandatedStiffness: Double
) {

    /** `max(0, |k_eff|)` — `C-0012`'s number, and the lower bound on any coupling. */
    val stabilityFloor: Double get() = max(0.0, -effectiveStiffnessAtTarget)

    /**
     * The stiffness that places the operating point at [targetStroke] with **no preload**,
     * `W(s*)/s*` — the chord of the characteristic from the origin.
     */
    val unpreloadedPlacementStiffness: Double get() = outputForceAtTarget / targetStroke

    /**
     * True when the chord is flatter than the tangent, i.e. **no unpreloaded linear coupling
     * is simultaneously placed at the target stroke and stable there**.
     *
     * The escape is a coupling stiffer than the chord carrying a **downward** preload, which
     * moves the zero-bias rest position and is therefore `T-13`'s problem as well as this one's.
     */
    val unpreloadedWindowIsEmpty: Boolean
        get() = unpreloadedPlacementStiffness <= stabilityFloor

    /** Whether §3's own mandated stiffness clears the stability floor. */
    val mandatedStiffnessIsStable: Boolean get() = mandatedStiffness > stabilityFloor

    /**
     * `k_c*` over `|k_eff|` — how much margin §3's own coupling stiffness has, infinite where
     * the operating point is already stable.
     *
     * Written out rather than as a ratio with a slash: `CLAUDE.md` records that a KDoc
     * comment containing the two characters that close it terminates there.
     */
    val stabilityMargin: Double
        get() = if (stabilityFloor <= 0.0) Double.POSITIVE_INFINITY
        else mandatedStiffness / stabilityFloor

    /** The verdict: **empty** means §3's own mandated coupling does not stabilise this point. */
    val isEmpty: Boolean get() = !mandatedStiffnessIsStable
}
