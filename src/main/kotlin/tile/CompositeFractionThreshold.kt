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

package com.xemantic.nano.plentyofroom.tile

import kotlin.math.abs

/**
 * `T-196` — the algebra and the search behind *"where does the four-layer tile stop being flat?"*
 *
 * ## Why this is a one-dimensional inversion and not a plate sweep
 *
 * [multiLayerRigidities] admits the interlayer coupling only through
 * `realised = 1 + f (factor − 1)`, and that **one** number multiplies `D_par` **and** `D_perp`
 * alike — the identity `k_s/k_theta = S/B` documented on [MultiLayerRigidities]. So the composite
 * fraction is a pure **scale** on the plate, the free-tile dishing is a function of that single
 * scale, and the threshold in `f` follows from the threshold in the scale by
 * [fractionForEnhancement]. That is the cheap bound this task runs before any solve.
 *
 * ## Why the search scans before it bisects
 *
 * `CLAUDE.md` records two failures this file is written against. **A verdict that is not monotone
 * in a swept variable has no threshold**, and sweeping it finer finds more alternation rather than
 * less (`C-0070`'s lateral seat passed at 4 of 11 interleaved values because it was a *register*
 * and not a tolerance). And **a tolerance threshold is not a slope at the origin** — the two can
 * have opposite signs, so differentiating at `f = 0` is not a shortcut. [firstCrossing] therefore
 * scans the whole interval, counts **every** sign change, bisects only the **first** one, and
 * reports the count so a caller cannot mistake an alternating verdict for a threshold.
 */

/** `1 + f (factor − 1)`: the realised flexural enhancement at composite fraction [fraction]. */
fun enhancementForFraction(fraction: Double, factor: Double): Double {
    require(fraction in 0.0..1.0) { "fraction must lie in [0, 1], was: $fraction" }
    require(factor >= 1.0) { "factor must be at least 1, was: $factor" }
    return 1.0 + fraction * (factor - 1.0)
}

/**
 * The inverse of [enhancementForFraction].
 *
 * Refuses `factor == 1` rather than dividing by zero: a single layer has no parallel-axis excess,
 * so *"what fraction of the excess is realised"* is not a question about it. `C-0031`'s rule —
 * a root-finder handed a target it cannot reach must not manufacture an answer.
 */
fun fractionForEnhancement(enhancement: Double, factor: Double): Double {
    require(factor > 1.0) {
        "factor must exceed 1 for the fraction to be defined, was: $factor"
    }
    return (enhancement - 1.0) / (factor - 1.0)
}

/**
 * A located crossing of a swept quantity through a target.
 *
 * @param root the abscissa of the **first** crossing.
 * @param bracketLow the retained bracket's lower end; `root` lies inside `[bracketLow, bracketHigh]`.
 * @param bracketHigh the retained bracket's upper end.
 * @param signChanges how many sign changes the scan found over the WHOLE interval. One means the
 *          quantity crosses once and a threshold is well posed; more than one means it alternates,
 *          and the caller must report the alternation rather than the root.
 * @param monotone `signChanges == 1`, named so that a reader cannot miss it.
 * @param scanSteps the scan resolution the crossing was located in.
 */
data class SweptCrossing(
    val root: Double,
    val bracketLow: Double,
    val bracketHigh: Double,
    val signChanges: Int,
    val scanSteps: Int
) {
    val monotone: Boolean get() = signChanges == 1
}

/**
 * The first crossing of [quantity] through [target] on `[low, high]`, or `null` if there is none.
 *
 * A `null` is a **verdict**, not a failure: it means the swept quantity does not reach the target
 * anywhere in the interval, which for this task is *"no interlayer coupling in `[0, 1]` puts the
 * free tile on the other side of the tolerance"*. `C-0031` and `C-0027` both record that clamping
 * such a case into range turns a result into a crash — or worse, into a plausible number.
 *
 * @param scanSteps intervals the scan uses. Refining it must NOT move the root: the bisection owns
 *          the precision, and a scan that changes the answer is a scan that missed a feature.
 * @param tolerance the bracket width the bisection exits on, in the units of the abscissa.
 */
fun firstCrossing(
    low: Double,
    high: Double,
    scanSteps: Int,
    target: Double,
    tolerance: Double = 1e-9,
    quantity: (Double) -> Double
): SweptCrossing? {
    require(high > low) { "high must exceed low, was: $low..$high" }
    require(scanSteps >= 2) { "scanSteps must be at least 2, was: $scanSteps" }
    require(tolerance > 0.0) { "tolerance must be positive, was: $tolerance" }

    val step = (high - low) / scanSteps
    // Sampled once and retained: the residual is evaluated at exactly the abscissae that are
    // passed on, because `CLAUDE.md` records a bracket walk and its root-finder spelling one sign
    // test two ways and certifying a bracket the finder then rejected.
    val abscissae = (0..scanSteps).map { low + it * step }
    val residuals = abscissae.map { quantity(it) - target }

    var signChanges = 0
    var firstIndex = -1
    for (i in 0 until scanSteps) {
        val a = residuals[i]
        val b = residuals[i + 1]
        // Compared as SIGNS, never as a product: a product of two small residuals underflows,
        // which is `C-0031`'s repaired defect.
        val crosses = (a > 0.0 && b < 0.0) || (a < 0.0 && b > 0.0) || a == 0.0 || b == 0.0
        if (crosses) {
            signChanges++
            if (firstIndex < 0) firstIndex = i
        }
    }
    if (firstIndex < 0) return null

    var lo = abscissae[firstIndex]
    var hi = abscissae[firstIndex + 1]
    var loResidual = residuals[firstIndex]
    // An endpoint that is exactly on the target is the root; no bisection can improve it.
    if (loResidual == 0.0) {
        return SweptCrossing(lo, lo, lo, signChanges, scanSteps)
    }
    if (residuals[firstIndex + 1] == 0.0) {
        return SweptCrossing(hi, hi, hi, signChanges, scanSteps)
    }
    while (hi - lo > tolerance) {
        val mid = 0.5 * (lo + hi)
        val midResidual = quantity(mid) - target
        if (midResidual == 0.0) {
            lo = mid
            hi = mid
            break
        }
        // Sign comparison again, and the retained endpoint's sign is carried explicitly.
        if ((loResidual > 0.0) != (midResidual > 0.0)) {
            hi = mid
        } else {
            lo = mid
            loResidual = midResidual
        }
    }
    return SweptCrossing(0.5 * (lo + hi), lo, hi, signChanges, scanSteps)
}

/** True when [a] and [b] agree to [relative] of the larger magnitude. Used by the study's gates. */
internal fun agreesTo(a: Double, b: Double, relative: Double): Boolean =
    abs(a - b) <= relative * maxOf(abs(a), abs(b), Double.MIN_VALUE)

/**
 * The helices in an `m × n` honeycomb block — Douglas et al.'s nomenclature, `m` x-raster rows of
 * `n` helices each (*Nucleic Acids Research* **37**:5001, Figure 2 caption, read directly).
 *
 * `T-199` exists because two of that paper's seven designs — `15 × 4`, which is the tile this
 * programme recommends, and `10 × 6`, which the paper itself recommends — are **both 60 helices**,
 * so choosing between them costs no scaffold. Two of the seven (`8 × 8` and `4 × 16`) are 64, so
 * the family as a whole is *not* a fixed-budget comparison and only this pair is.
 */
fun crossSectionHelices(rows: Int, layers: Int): Int {
    require(rows >= 1) { "rows must be at least 1, was: $rows" }
    require(layers >= 1) { "layers must be at least 1, was: $layers" }
    return rows * layers
}
