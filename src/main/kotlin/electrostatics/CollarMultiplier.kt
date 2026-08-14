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

package com.xemantic.nano.plentyofroom.electrostatics

import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.ln

/**
 * Task `T-60` — the **collar multiplier** `μ(h)` of `C-0022`'s finite tile, as a function of the
 * gap at **fixed applied bias**, and the logarithmic gradient that is the only part of it a
 * force-pinned actuator can feel. Leaf `A7.4`, consumed by `A2.2`.
 *
 * ## Why a gradient and not a multiplier
 *
 * `CH-0026` measures the finite-tile enhancement — `+4.9 %` to `+19.2 %` of total force over the
 * §3 box — and `CH-0035` shows what happens to it at an operating point the device is *held* at:
 * the balance fixes `|F_es| = R(s) + P(h)A`, and `k_es = −|F_es|/ℓ` identically, so a multiplier
 * on the **level** of the force is absorbed entirely into the bias and reaches the stiffness
 * **exactly not at all**. What survives is
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`1/ℓ_2D = 1/ℓ_1D − d ln μ/dh`,
 *
 * one number per state, and it lengthens the decay wherever the collar widens with the gap.
 *
 * `C-0027` could only difference it across gaps that `T-3b`'s sweep visited **at different
 * biases**, giving `0.0133–0.0226 nm⁻¹` over three schemes — a 1.7× spread that leaves the
 * coupled tangent at the fold straddling zero. This file is the apparatus for replacing that
 * difference with a derivative of one function.
 *
 * ## Conventions
 *
 * - `μ ≡ |F_es,2D| / (Π_1D · A)` with `A` the footprint: **dimensionless, and `μ > 1` is an
 *   enhancement.** `T-3b` emits a force *deficit* fraction, so `μ = 1 − fraction`.
 * - Two mappings of the straight-edge deficit onto a square tile bracket the unsolved corner:
 *   [minimumMarginCollarMultiplier] counts each corner once and understates it,
 *   [additiveCollarMultiplier] counts it twice and overstates it (`C-0022`).
 * - `d ln μ/dh` is in `nm⁻¹`, **positive when the collar widens with the gap**.
 */

/**
 * The collar multiplier on the **additive-deficit** mapping: `μ = 1 − 4·M₀/(L·Π)`.
 *
 * For a deficit `M₀ = −wΠ` — i.e. an effective collar of width `w` on every side — this is
 * exactly `1 + 4w/L`, which is the form `C-0022` quotes and the form the gate test asserts.
 * It counts each of the four corners **twice** and is therefore the upper of the two mappings
 * for an enhancement.
 *
 * @param totalDeficitPerUnitEdge `M₀` in `pN/nm` of edge; **negative** for an enhancement.
 * @param interiorLoad the 1-D load far from the rim in `pN/nm²`, which must not be zero.
 * @param edgeLength the tile's side `L` in nm.
 */
fun additiveCollarMultiplier(
    totalDeficitPerUnitEdge: Double,
    interiorLoad: Double,
    edgeLength: Double
): Double {
    require(edgeLength > 0.0) { "edgeLength must be positive, was: $edgeLength" }
    require(interiorLoad != 0.0) { "interiorLoad must not be zero" }
    return 1.0 - 4.0 * totalDeficitPerUnitEdge / (edgeLength * interiorLoad)
}

/**
 * The collar multiplier on the **minimum-margin** mapping,
 * `μ = 1 − (4L·M₀ − 8·M₁)/(L²·Π)`.
 *
 * This is the mapping [com.xemantic.nano.plentyofroom.structure.edgeTaperedPressure] realises:
 * the taper is a function of the *minimum* distance to the boundary, so a layer-cake integral
 * over level sets of perimeter `4(L − 2m)` gives the deficit as `4L·M₀ − 8·M₁` in the profile's
 * own first two moments, and each corner is counted **once**.
 *
 * @param firstMoment `M₁ = ∫s(Π − load)ds` in pN per unit length of edge.
 */
fun minimumMarginCollarMultiplier(
    totalDeficitPerUnitEdge: Double,
    firstMoment: Double,
    interiorLoad: Double,
    edgeLength: Double
): Double {
    require(edgeLength > 0.0) { "edgeLength must be positive, was: $edgeLength" }
    require(interiorLoad != 0.0) { "interiorLoad must not be zero" }
    return 1.0 - (4.0 * edgeLength * totalDeficitPerUnitEdge - 8.0 * firstMoment) /
            (edgeLength * edgeLength * interiorLoad)
}

/**
 * The **cheap estimate** of `d ln μ/dh` in `nm⁻¹`, from the transverse eigenvalue alone —
 * run before any 2-D solve, per §5.
 *
 * `C-0022`'s rigorous width ceiling is `w ≤ 1/q₀` with `q₀² = κ² + (π/2h)²`, because
 * `κ_loc² ≥ κ²` pointwise. Taking that ceiling *as* the collar and the additive mapping
 * `μ = 1 + 4w/L` gives, in closed form,
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`d ln μ/dh = (4/L)·(dw/dh)/μ`, &nbsp;&nbsp;
 * `dw/dh = (π²/4)·h⁻³/q₀³`.
 *
 * **This is an estimate, not a bound**, and the difference matters: the ceiling bounds `w`, not
 * `dw/dh`, and it enters the numerator and the denominator with opposite effect. The expected
 * error is stated in advance in `T-60`'s Plan as **about a factor of two, one-sided in neither
 * direction** — which is what `C-0022`'s own depth half of the cheap bound failed to be, having
 * got the sign wrong.
 *
 * Two limits are exact and are asserted as such: with no screening the collar is purely
 * geometric, `w = 2h/π`, so `dw/dh = 2/π`; with strong screening `w → 1/κ` and the gradient
 * vanishes.
 */
fun collarLogGradientEstimate(
    inverseDebyeLength: Double,
    gapHeight: Double,
    edgeLength: Double
): Double {
    require(inverseDebyeLength > 0.0) {
        "inverseDebyeLength must be positive, was: $inverseDebyeLength"
    }
    require(gapHeight > 0.0) { "gapHeight must be positive, was: $gapHeight" }
    require(edgeLength > 0.0) { "edgeLength must be positive, was: $edgeLength" }
    val rate = transverseDecayRateBound(inverseDebyeLength, gapHeight)
    val width = 1.0 / rate
    val widthSlope = 0.25 * PI * PI / (gapHeight * gapHeight * gapHeight * rate * rate * rate)
    val multiplier = 1.0 + 4.0 * width / edgeLength
    return 4.0 * widthSlope / (edgeLength * multiplier)
}

/**
 * A solved `μ(h)` at one buffer and one fixed applied bias, as a `C¹` curve the actuator's
 * field can be multiplied by.
 *
 * ## Why a cubic Hermite on `ln μ` with parabolic node slopes
 *
 * The answer this whole task delivers *is* a derivative, so the interpolant's derivative and
 * the reported finite difference must be **the same object**, not two numbers that happen to
 * be close. Parabolic node slopes make the interpolant's derivative at an interior node of a
 * uniform mesh **exactly** the central difference there, and reproduce a log-linear `μ` to
 * machine precision on any mesh.
 *
 * A spline was rejected: `ln μ` changes sign inside the sampled range (`C-0022` finds `μ < 1`
 * at a 2 nm gap and `μ > 1` above ~3 nm), and an oscillating interpolant would put structure
 * into the one quantity the task exists to measure.
 *
 * ## Why it clamps rather than extrapolates
 *
 * A collar that grows without bound is not a physical statement, and `C-0027`'s own reader
 * throws rather than extrapolate. Here the curve is consumed inside a fold search that
 * necessarily probes the whole admissible stroke, so throwing would kill the search; instead
 * the multiplier is **held** outside the solved range, the gradient is exactly zero there, and
 * every such evaluation is **counted** so that the study can assert the fold and the operating
 * point are interior. A clamp that is never reported is an extrapolation with extra steps.
 *
 * @param gaps strictly ascending gap heights in nm, at least two.
 * @param multipliers the solved `μ` at those gaps, all strictly positive.
 */
class CollarMultiplierCurve(
    gaps: DoubleArray,
    multipliers: DoubleArray
) {

    private val nodes: DoubleArray = gaps.copyOf()

    private val values: DoubleArray = multipliers.copyOf()

    private val logs: DoubleArray

    private val slopes: DoubleArray

    private var clamped = 0

    init {
        require(gaps.size >= 2) { "at least two gaps are needed, was: ${gaps.size}" }
        require(gaps.size == multipliers.size) {
            "gaps and multipliers must agree in length, were: ${gaps.size} and ${multipliers.size}"
        }
        for (i in 1 until gaps.size) {
            require(gaps[i] > gaps[i - 1]) {
                "gaps must strictly ascend — mu must be a FUNCTION of the gap; breaks at index $i"
            }
        }
        multipliers.forEachIndexed { i, it ->
            require(it > 0.0) { "multipliers must be positive, was: $it at index $i" }
        }
        logs = DoubleArray(multipliers.size) { ln(multipliers[it]) }
        slopes = nodeSlopes(nodes, logs)
    }

    /** The lowest gap solved, in nm — below it the curve is clamped. */
    val lowestGap: Double get() = nodes.first()

    /** The highest gap solved, in nm — above it the curve is clamped. */
    val highestGap: Double get() = nodes.last()

    /** How many evaluations fell outside the solved range and were clamped. */
    val clampedEvaluations: Int get() = clamped

    /** `μ` at [gap], clamped to the solved range. */
    fun multiplierAt(gap: Double): Double = when {
        gap < nodes.first() -> { clamped++; values.first() }
        gap > nodes.last() -> { clamped++; values.last() }
        else -> exp(hermite(gap))
    }

    /** `ln μ` at [gap], clamped to the solved range. */
    fun logMultiplierAt(gap: Double): Double = when {
        gap < nodes.first() -> { clamped++; logs.first() }
        gap > nodes.last() -> { clamped++; logs.last() }
        else -> hermite(gap)
    }

    /** `d ln μ/dh` at [gap] in `nm⁻¹`; **exactly zero** outside the solved range. */
    fun logGradientAt(gap: Double): Double = when {
        gap < nodes.first() || gap > nodes.last() -> { clamped++; 0.0 }
        else -> hermiteSlope(gap)
    }

    /** The interval index `i` with `nodes[i] <= gap <= nodes[i+1]`. */
    private fun interval(gap: Double): Int {
        var low = 0
        var high = nodes.size - 1
        while (high - low > 1) {
            val middle = (low + high) / 2
            if (nodes[middle] <= gap) low = middle else high = middle
        }
        return low
    }

    private fun hermite(gap: Double): Double {
        val i = interval(gap)
        val step = nodes[i + 1] - nodes[i]
        val t = (gap - nodes[i]) / step
        val t2 = t * t
        val t3 = t2 * t
        return (2.0 * t3 - 3.0 * t2 + 1.0) * logs[i] +
                (t3 - 2.0 * t2 + t) * step * slopes[i] +
                (-2.0 * t3 + 3.0 * t2) * logs[i + 1] +
                (t3 - t2) * step * slopes[i + 1]
    }

    private fun hermiteSlope(gap: Double): Double {
        val i = interval(gap)
        val step = nodes[i + 1] - nodes[i]
        val t = (gap - nodes[i]) / step
        val t2 = t * t
        return ((6.0 * t2 - 6.0 * t) * (logs[i] - logs[i + 1]) / step) +
                (3.0 * t2 - 4.0 * t + 1.0) * slopes[i] +
                (3.0 * t2 - 2.0 * t) * slopes[i + 1]
    }
}

/**
 * The parabolic node slopes of [values] on the mesh [nodes] — the derivative at each node of
 * the parabola through it and its two neighbours, one-sided at the ends.
 *
 * On a uniform mesh the interior slopes are **exactly** the central differences, which is what
 * makes the reported gradient and the used gradient one object; on any mesh a straight line is
 * reproduced exactly, which is what makes a log-linear `μ` come back with its own rate.
 */
internal fun nodeSlopes(nodes: DoubleArray, values: DoubleArray): DoubleArray {
    val count = nodes.size
    val step = DoubleArray(count - 1) { nodes[it + 1] - nodes[it] }
    val divided = DoubleArray(count - 1) { (values[it + 1] - values[it]) / step[it] }
    val slopes = DoubleArray(count)
    if (count == 2) {
        slopes[0] = divided[0]
        slopes[1] = divided[0]
        return slopes
    }
    for (i in 1 until count - 1) {
        slopes[i] = (step[i] * divided[i - 1] + step[i - 1] * divided[i]) / (step[i - 1] + step[i])
    }
    slopes[0] = ((2.0 * step[0] + step[1]) * divided[0] - step[0] * divided[1]) / (step[0] + step[1])
    val last = count - 1
    slopes[last] = ((2.0 * step[last - 1] + step[last - 2]) * divided[last - 1] -
            step[last - 1] * divided[last - 2]) / (step[last - 1] + step[last - 2])
    return slopes
}
