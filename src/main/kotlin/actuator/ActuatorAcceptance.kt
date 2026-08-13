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

/**
 * The two small pieces of arithmetic `T-3`'s acceptance verdict needs, kept apart from the
 * sweep so that both can be tested.
 */

/**
 * Returns the smallest [abscissa] at which [ordinate] first reaches [target], by linear
 * interpolation inside the first bracketing pair, or `null` if it never does.
 *
 * Linear rather than the shape-preserving cubic used for the force curve, and deliberately:
 * the abscissa here is the **applied bias**, sampled at nine points across two decades, and the
 * quantity being crossed saturates hard above ~0.5 V (`C-0008`: a factor of 8 in bias buys
 * 1.9× in force). A cubic through saturating data can place a crossing outside the bracket that
 * contains it; a linear reading cannot, and its error is bounded by the sample spacing, which is
 * reported alongside the answer as [BiasThreshold.bracket].
 */
fun firstCrossing(
    abscissa: DoubleArray,
    ordinate: DoubleArray,
    target: Double
): BiasThreshold? {
    require(abscissa.size == ordinate.size) {
        "abscissa and ordinate must have equal size, were: ${abscissa.size} and ${ordinate.size}"
    }
    require(abscissa.size >= 2) { "at least two samples are needed, were: ${abscissa.size}" }
    if (ordinate[0] >= target) return BiasThreshold(abscissa[0], abscissa[0] to abscissa[0], true)
    for (i in 1 until abscissa.size) {
        if (ordinate[i] >= target) {
            val span = ordinate[i] - ordinate[i - 1]
            val fraction = if (span > 0.0) (target - ordinate[i - 1]) / span else 1.0
            return BiasThreshold(
                abscissa[i - 1] + fraction * (abscissa[i] - abscissa[i - 1]),
                abscissa[i - 1] to abscissa[i],
                false
            )
        }
    }
    return null
}

/** Where a swept quantity first reaches a target, with the bracket that pins the answer. */
data class BiasThreshold(
    /** The interpolated abscissa in volt. */
    val value: Double,
    /** The sampled pair the crossing lies inside — the interpolation's own error bar. */
    val bracket: Pair<Double, Double>,
    /** True when the target was already met at the lowest sampled bias, so this is a ceiling. */
    val metAtLowestSample: Boolean
)

/**
 * Returns the drainage corner frequency in Hz at an operating point of stiffness [stiffness],
 * scaled from `C-0004`'s [referenceCorner] at [referenceStiffness].
 *
 * **This consumes `C-0004`; it does not re-derive it.** `C-0004` establishes `τ = γ/k_layer` with
 * `γ = ηGA/T` a purely hydrodynamic drag, and verifies `τ ∝ 1/k_layer` **exactly** — which is
 * what licenses substituting a different restoring stiffness into the same drag. The stiffness
 * to substitute is `k_eff = k_brush + k_es`, because that, not `k_brush`, is what restores the
 * tile at a biased operating point, and `k_es < 0` everywhere (§1).
 *
 * Two things make the substitution conservative rather than optimistic:
 *
 * 1. `γ` is evaluated by `C-0004` at the **unperturbed** volume fraction, and `C-0004` shows a
 *    denser layer drains **faster** (`kM ∝ φ^(5/4)` or `φ^(3/4)`), so the compressed operating
 *    point has less drag than the reference, not more;
 * 2. `C-0004`'s reference permeability is taken from the slow end of a factor-of-40 spread.
 *
 * So the number returned is a lower bound on the corner within `C-0004`'s own model, and the
 * electrostatic softening is the only term that pushes it down.
 */
fun drainageCornerFrequency(
    referenceCorner: Double,
    referenceStiffness: Double,
    stiffness: Double
): Double {
    require(referenceCorner > 0.0) { "referenceCorner must be positive, was: $referenceCorner" }
    require(referenceStiffness > 0.0) {
        "referenceStiffness must be positive, was: $referenceStiffness"
    }
    require(stiffness >= 0.0) { "stiffness must not be negative, was: $stiffness" }
    return referenceCorner * stiffness / referenceStiffness
}
