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

package com.xemantic.nano.plentyofroom.actuator

import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sign

/**
 * `F_es(h)` at a fixed applied bias, buffer and medium, sampled once and then interpolated —
 * the object that makes the `T-3` force balance affordable.
 *
 * ## Why an interpolant at all
 *
 * The force balance is a root find in `h`, and every evaluation of `F_es(h)` costs a Stern-series
 * bisection wrapped around a nonlinear Poisson-Boltzmann solve — `C-0008`'s pipeline, some tens
 * of milliseconds. A root find takes tens of evaluations, and `T-3` needs one root find per
 * (layer model × design point × buffer × bias), which is hundreds. Sampling the curve once per
 * (buffer, bias, medium) and interpolating turns an intractable sweep into a minute.
 *
 * ## Why `ln|F|` against `h`, and shape-preserving cubic Hermite
 *
 * `C-0008` establishes that `|F_es|` decays on its own length `ℓ = −1/(d ln|F_es|/dh)`, which is
 * 1.8–2.8 nm at the working gap and rises to the bulk `λ_D` in the far field. In the far field
 * the force is therefore *exactly* an exponential and `ln|F|` is *exactly* linear in `h`. A
 * scheme exact on linear data is then exact there in both value and derivative — the one case
 * where a closed form exists to check against, and `ElectrostaticForceCurveTest` checks it at 1e−12.
 *
 * The nodal derivatives are limited by the Fritsch-Carlson rule, so the interpolant is
 * **shape-preserving**: it cannot overshoot into a non-monotone `|F_es|`, and therefore cannot
 * invent a sign change in `k_es`, which would read as a spurious pull-in. A natural cubic spline
 * would give a smoother curve and exactly that hazard.
 *
 * ## Sign conventions, and the one place they reverse
 *
 * Inherited unchanged from `C-0008`: `F_z < 0` means toward the electrode, and `ℓ = F_es/k_es`.
 *
 * `C-0008` reports `k_es < 0` everywhere, and that is true **everywhere it looked** — its smallest
 * gap is 3 nm. It is a property of the force *decaying* with the gap, not an axiom: `|F_es|` is
 * non-monotone at small separation, rising to a maximum and then falling toward the sign change
 * described below. Above that maximum `k_es < 0` and `ℓ > 0`, which is §1's softening; **below it
 * `k_es > 0` and `ℓ < 0`, and the electrostatics stiffens the layer instead of softening it**.
 * Nothing here forbids either branch, and the study reports which branch each operating point is on.
 *
 * Only attractive (`F_z < 0`) sampling is admitted. That is not a convenience: `C-0008` shows the
 * **zero-bias** force is a sign-changing near-cancellation under 4 pN for which "no single number
 * is defensible", so a curve through it would be interpolating a quantity that does not exist.
 */
class ElectrostaticForceCurve internal constructor(

    /** The sampled gaps in nm, strictly ascending. */
    val gapHeights: DoubleArray,

    /** The **signed** force in pN at each sampled gap; negative means toward the electrode. */
    val forces: DoubleArray

) {

    private val logMagnitude = DoubleArray(gapHeights.size) { ln(-forces[it]) }

    /** `d ln|F|/dh` at each node, Fritsch-Carlson limited. */
    private val logSlopes = shapePreservingSlopes(gapHeights, logMagnitude)

    /** The smallest sampled gap in nm. */
    val minimumGap: Double get() = gapHeights[0]

    /** The largest sampled gap in nm. */
    val maximumGap: Double get() = gapHeights[gapHeights.size - 1]

    /** The **signed** force in pN at [gap] nm — negative, toward the electrode. */
    fun forceAt(gap: Double): Double = -magnitudeAt(gap)

    /** `|F_es|` in pN at [gap] nm. */
    fun magnitudeAt(gap: Double): Double = exp(interpolate(gap, derivative = false))

    /** `d ln|F_es|/dh` in `nm⁻¹` at [gap] nm — strictly negative for a decaying force. */
    fun logSlopeAt(gap: Double): Double = interpolate(gap, derivative = true)

    /**
     * `k_es = −∂F_es,z/∂h` in `pN/nm` at [gap] nm.
     *
     * `F_z = −|F|`, so `∂F_z/∂h = −|F| d ln|F|/dh` and `k_es = |F| d ln|F|/dh`, which carries the
     * sign of the log-slope: **negative wherever the force decays with the gap**, which is §1's
     * softening and everything `C-0008` sampled, and **positive past the force maximum at small
     * separation**, where the electrostatics stiffens the layer instead.
     */
    fun stiffnessAt(gap: Double): Double = magnitudeAt(gap) * logSlopeAt(gap)

    /** `ℓ = −1/(d ln|F_es|/dh)` in nm at [gap] nm — the force's own decay length, `C-0008`'s. */
    fun decayLengthAt(gap: Double): Double = -1.0 / logSlopeAt(gap)

    /**
     * The cubic Hermite value or derivative of `ln|F|` at [gap].
     *
     * On the standard basis `h00 = 2t³−3t²+1`, `h10 = t³−2t²+t`, `h01 = −2t³+3t²`, `h11 = t³−t²`,
     * with `t = (h − h_i)/Δ`. On linear data the limited slopes equal the secant and the four
     * terms collapse to the secant line exactly, in both readings.
     */
    private fun interpolate(gap: Double, derivative: Boolean): Double {
        val i = intervalAt(gap)
        val width = gapHeights[i + 1] - gapHeights[i]
        val t = (gap - gapHeights[i]) / width
        val t2 = t * t
        val t3 = t2 * t
        val left = logMagnitude[i]
        val right = logMagnitude[i + 1]
        val leftSlope = logSlopes[i]
        val rightSlope = logSlopes[i + 1]
        return if (derivative) {
            (
                    (6.0 * t2 - 6.0 * t) * (left - right) +
                            width * ((3.0 * t2 - 4.0 * t + 1.0) * leftSlope +
                            (3.0 * t2 - 2.0 * t) * rightSlope)
                    ) / width
        } else {
            (2.0 * t3 - 3.0 * t2 + 1.0) * left +
                    width * (t3 - 2.0 * t2 + t) * leftSlope +
                    (-2.0 * t3 + 3.0 * t2) * right +
                    width * (t3 - t2) * rightSlope
        }
    }

    private fun intervalAt(gap: Double): Int {
        require(gap >= minimumGap && gap <= maximumGap) {
            "gap must be within [$minimumGap, $maximumGap], was: $gap"
        }
        var low = 0
        var high = gapHeights.size - 1
        while (high - low > 1) {
            val middle = (low + high) / 2
            if (gapHeights[middle] <= gap) low = middle else high = middle
        }
        return low
    }

}

/**
 * Samples [force] at [gapHeights] and returns the interpolant.
 *
 * @throws IllegalArgumentException if fewer than three samples are given, if they are not
 *          strictly ascending, or if any sampled force is not strictly attractive and finite.
 */
fun electrostaticForceCurve(
    gapHeights: DoubleArray,
    force: (Double) -> Double
): ElectrostaticForceCurve {
    require(gapHeights.size >= 3) {
        "at least three samples are needed, were: ${gapHeights.size}"
    }
    for (i in 1 until gapHeights.size) {
        require(gapHeights[i] > gapHeights[i - 1]) {
            "gapHeights must be strictly ascending, broke at index $i: " +
                    "${gapHeights[i - 1]} then ${gapHeights[i]}"
        }
    }
    val forces = DoubleArray(gapHeights.size) { force(gapHeights[it]) }
    forces.forEachIndexed { i, value ->
        require(value < 0.0 && value.isFinite()) {
            "every sampled force must be strictly attractive and finite, was $value " +
                    "at gap ${gapHeights[i]}"
        }
    }
    return ElectrostaticForceCurve(gapHeights.copyOf(), forces)
}

/**
 * The attractive part of a sampled force curve, and where it stops being attractive.
 *
 * `C-0008` reports the force at `V = 0` changing sign between 4 and 5 nm, and the same mechanism
 * operates under bias at small enough separation: the tile's own counterion cloud is confined
 * between two walls and its osmotic pressure eventually beats the Maxwell attraction. The gap at
 * which that happens is an **electrostatic stopper** — the actuator cannot be driven below it by
 * the field alone, whatever the layer does — and it is a physical result, not a sampling artefact,
 * so it is returned alongside the curve rather than swallowed.
 */
data class AttractiveSampling(

    /** The interpolant over the strictly attractive part of the sampled range. */
    val curve: ElectrostaticForceCurve,

    /** The largest sampled gap at which the force was **not** attractive, or `null` if none was. */
    val repulsiveBelow: Double?
)

/**
 * Samples [force] at [gapHeights] and returns the interpolant over its strictly attractive tail.
 *
 * Trimming rather than throwing, because a sign change at small gap is physics (see
 * [AttractiveSampling]) while a curve through a sign change is not interpolable on `ln|F|` at all.
 * The trim point is reported so that a downstream operating point can be checked against it.
 *
 * @throws IllegalArgumentException if fewer than three attractive samples remain.
 */
fun attractiveForceCurve(
    gapHeights: DoubleArray,
    force: (Double) -> Double
): AttractiveSampling {
    require(gapHeights.size >= 3) {
        "at least three samples are needed, were: ${gapHeights.size}"
    }
    val sampled = DoubleArray(gapHeights.size) { force(gapHeights[it]) }
    var first = 0
    for (i in sampled.indices) if (sampled[i] >= 0.0 || !sampled[i].isFinite()) first = i + 1
    require(gapHeights.size - first >= 3) {
        "at least three attractive samples are needed, were: ${gapHeights.size - first} " +
                "over [${gapHeights[0]}, ${gapHeights[gapHeights.size - 1]}]"
    }
    return AttractiveSampling(
        curve = ElectrostaticForceCurve(
            gapHeights.copyOfRange(first, gapHeights.size),
            sampled.copyOfRange(first, sampled.size)
        ),
        repulsiveBelow = if (first == 0) null else gapHeights[first - 1]
    )
}

/**
 * Returns a geometrically graded sample grid of [count] gaps from [lowest] to [highest] nm.
 *
 * Geometric rather than uniform because `ℓ` shortens as the gap closes — 1.51 nm at 3 nm against
 * 3.90 nm at 30 nm, per `C-0008` — so the curvature of `ln|F|` in `h` is concentrated at small
 * gaps and that is where the samples have to be.
 */
fun gradedGapGrid(lowest: Double, highest: Double, count: Int): DoubleArray {
    require(lowest > 0.0) { "lowest must be positive, was: $lowest" }
    require(highest > lowest) { "highest must exceed lowest, was: $highest vs $lowest" }
    require(count >= 3) { "count must be at least 3, was: $count" }
    val ratio = highest / lowest
    return DoubleArray(count) { lowest * Math.pow(ratio, it.toDouble() / (count - 1)) }
}

/**
 * Returns the Fritsch-Carlson shape-preserving derivatives of [value] against [node].
 *
 * Exactly the secant slope wherever the data are linear — which is why a pure exponential force
 * is reproduced to machine precision — and limited to three times the smaller neighbouring
 * secant wherever they are not, which is what forbids overshoot.
 */
internal fun shapePreservingSlopes(node: DoubleArray, value: DoubleArray): DoubleArray {
    val count = node.size
    val secant = DoubleArray(count - 1) { (value[it + 1] - value[it]) / (node[it + 1] - node[it]) }
    val slope = DoubleArray(count)
    slope[0] = secant[0]
    slope[count - 1] = secant[count - 2]
    for (i in 1 until count - 1) {
        slope[i] = if (secant[i - 1] * secant[i] <= 0.0) 0.0
        else {
            // the weighted harmonic mean — the standard monotone choice, exact on linear data
            val left = node[i] - node[i - 1]
            val right = node[i + 1] - node[i]
            val total = left + right
            (total * 3.0) / ((total + right) / secant[i - 1] + (total + left) / secant[i])
        }
    }
    for (i in 0 until count - 1) {
        val limit = 3.0 * abs(secant[i])
        if (abs(slope[i]) > limit) slope[i] = sign(slope[i]) * limit
        if (abs(slope[i + 1]) > limit) slope[i + 1] = sign(slope[i + 1]) * limit
    }
    return slope
}
