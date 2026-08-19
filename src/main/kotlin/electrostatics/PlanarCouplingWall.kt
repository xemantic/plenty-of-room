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
import kotlin.math.abs
import kotlin.math.atan
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * `T-221` — which wall a **planar wall-wall** coupling criterion is owed at, in closed form.
 *
 * ## The one identity
 *
 * Kanduč, Trulsson, Naji, Burak, Forsman & Podgornik (arXiv:0905.3851) Eq. (64) reads
 * `Ξ < D̃/ln D̃` with `D̃ = D/μ`, and **both** of its variables are linear in the wall's charge
 * density: `Ξ = 2π q³ l_B² σ` and `D̃ = 2π q l_B σ D`. Their ratio therefore carries no wall
 * convention whatever,
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`Ξ/D̃ = q² l_B/D`,
 *
 * and the criterion is **equivalent** to `ln(D/μ) < D/(q² l_B)` — a statement in which the whole
 * disputed convention appears once, as `ln σ`. That is what makes a `16.5×` disagreement in `Ξ`
 * worth `ln 16.5 = 2.80` in the variable that decides, and it is one line of algebra rather than
 * a solve.
 *
 * ## What is here and what is not
 *
 * The **repulsive** branch's bound is already implemented as [weakCouplingValidityCoupling] and
 * its root as [loopExpansionValidityGap] — the same closed form as Naji Eq. (20), which is why
 * `T-6` has been emitting it since iteration 3. What this file adds is the log form, its
 * closed-form threshold, and the **attractive** branch (Eq. 65), which `C-0137` and
 * [weakCouplingValidityCoupling]'s own KDoc dispose of as *"exponentially large"* without
 * evaluating it. The exponential is at fixed `ζ`; over the branch's own admissible `ζ` the
 * right-hand side has a finite infimum, and locating it needs the branch boundary, which follows
 * from Eq. (18) in the `α → 0` limit and costs no solve either.
 */

/**
 * `q² l_B` in nm — the separation at which two **counterions** interact with `k_BT`.
 *
 * Kanduč §I states it in as many words: the Bjerrum length is for unit charges and *"if the
 * charge valency of the counterions is q then the aforementioned distance scales as q² l_B"*.
 * It is 2.8564 nm for `Mg²⁺` at 300 K, and it is the only material quantity surviving in the
 * criterion once the wall convention has cancelled.
 */
fun counterionPairBjerrumLength(counterionValency: Int, bjerrumLength: Double): Double {
    require(counterionValency > 0) { "valency must be positive: $counterionValency" }
    require(bjerrumLength > 0.0) { "bjerrumLength must be positive: $bjerrumLength" }
    return counterionValency * counterionValency * bjerrumLength
}

/**
 * `Ξ/D̃ = q² l_B/D`, exactly — the criterion's scale-covariance identity.
 *
 * A property of the **gap**, not of the wall. Every candidate reading of the wall's charge
 * density returns this same number, which is the cheap bound `T-221` runs before anything else.
 */
fun couplingOverReducedGap(
    gap: Double,
    counterionValency: Int,
    bjerrumLength: Double
): Double {
    require(gap > 0.0) { "gap must be positive: $gap" }
    return counterionPairBjerrumLength(counterionValency, bjerrumLength) / gap
}

/**
 * `D/(q² l_B) − ln(D/μ)` — Kanduč Eq. (64) restated so the wall convention appears once.
 *
 * Positive means the loop expansion about mean field is controlled at this gap and this wall;
 * negative means it is not. The **difference** between two wall conventions is exactly the
 * logarithm of the ratio of their charge densities, with no gap dependence at all.
 */
fun repulsiveBranchLogResidual(
    gap: Double,
    gouyChapmanLength: Double,
    counterionValency: Int,
    bjerrumLength: Double
): Double {
    require(gouyChapmanLength > 0.0) { "gouyChapmanLength must be positive: $gouyChapmanLength" }
    return gap / counterionPairBjerrumLength(counterionValency, bjerrumLength) -
            ln(gap / gouyChapmanLength)
}

/**
 * `μ*(D) = D e^(−D/(q² l_B))` in nm — the Gouy-Chapman length at which Eq. (64) is an equality.
 *
 * Closed form, from setting [repulsiveBranchLogResidual] to zero. A wall with a **longer** `μ`
 * than this satisfies the criterion; a wall with a shorter one does not.
 */
fun repulsiveBranchThresholdGouyChapmanLength(
    gap: Double,
    counterionValency: Int,
    bjerrumLength: Double
): Double {
    require(gap > 0.0) { "gap must be positive: $gap" }
    return gap * exp(-gap / counterionPairBjerrumLength(counterionValency, bjerrumLength))
}

/**
 * `Ξ*(D) = (q² l_B/D) e^(D/(q² l_B))` — the largest coupling parameter Eq. (64) admits at [gap].
 *
 * 4.7317 at a 7 nm gap for `Mg²⁺`. Note it is **not** monotone in the gap: it has a minimum of
 * `e` at `D = q² l_B`, which is the same minimum [weakCouplingValidityCoupling] has at `D̃ = e`.
 */
fun repulsiveBranchThresholdCoupling(
    gap: Double,
    counterionValency: Int,
    bjerrumLength: Double
): Double = counterionPairBjerrumLength(counterionValency, bjerrumLength) /
        repulsiveBranchThresholdGouyChapmanLength(gap, counterionValency, bjerrumLength)

/**
 * `σ*(D)` in `e/nm²` — the largest wall charge density Eq. (64) admits at [gap].
 *
 * 0.1846 `e/nm²` at 7 nm for `Mg²⁺`, which is **below every bare reading of the Gen-1 tile's
 * gap-facing wall** and **above both renormalised ones**. That single number is what makes the
 * verdict a property of the bare/renormalised axis and of nothing else.
 */
fun repulsiveBranchThresholdChargeDensity(
    gap: Double,
    counterionValency: Int,
    bjerrumLength: Double
): Double = 1.0 / (2.0 * PI * counterionValency * bjerrumLength *
        repulsiveBranchThresholdGouyChapmanLength(gap, counterionValency, bjerrumLength))

/**
 * Kanduč Eqs. (61)/(62): `f(ζ)`, the amplitude of the asymptotic fluctuation pressure
 * `p̃₂(D̃) ≃ Ξ f(ζ) e^(2ζD̃)` between two **oppositely charged** walls, `−1 < ζ < 0`.
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`f(ζ) = ζ³ [3^(1+ζ)/(1−ζ)] [ 2 arctan(√(1−2ζ²))/√(1−2ζ²) + ln((1−ζ²)/(2ζ²)) ]`
 *
 * for `−√2/2 < ζ < 0`, and the same expression with `2 artanh(√(2ζ²−1))/√(2ζ²−1)` for
 * `ζ < −√2/2`. The two are each other's **analytic continuation** — `arctan(iy)/(iy) = artanh(y)/y`
 * — which is why the paper prints two forms and why continuity across `ζ = −√2/2` is the gate
 * that says the transcription is right. `f < 0` throughout, and `f → 0` as `ζ → 0⁻`.
 */
fun asymmetryFunction(asymmetry: Double): Double {
    require(asymmetry > -1.0 && asymmetry < 0.0) {
        "the asymmetry parameter must lie in (-1, 0) for oppositely charged walls: $asymmetry"
    }
    val z = asymmetry
    val prefactor = z * z * z * Math.pow(3.0, 1.0 + z) / (1.0 - z)
    val discriminant = 1.0 - 2.0 * z * z
    val inverseTrigonometric = if (discriminant > 0.0) {
        val root = sqrt(discriminant)
        2.0 * atan(root) / root
    } else if (discriminant < 0.0) {
        val root = sqrt(-discriminant)
        2.0 * (0.5 * ln((1.0 + root) / (1.0 - root))) / root
    } else 2.0
    return prefactor * (inverseTrigonometric + ln((1.0 - z * z) / (2.0 * z * z)))
}

/**
 * Kanduč Eq. (65): `Ξ < (ζ²/|f(ζ)|) e^(−2ζD̃)`, the weak-coupling validity criterion where the
 * mean-field pressure is **attractive**, which for planar walls requires opposite signs.
 *
 * The paper calls the right-hand side *"exponentially large"* and that is a statement at **fixed
 * `ζ`**; it diverges as `ζ → 0⁻` too, through the prefactor. What it does **not** do is stay
 * large over the branch's own domain: attraction itself requires `D̃ > (1+ζ)/|ζ|`
 * ([meanFieldPressureSignChangeReducedGap]), and the bound's infimum on that domain is attained
 * at its boundary — see [attractiveBranchInfimumCoupling].
 */
fun attractiveBranchValidityCoupling(asymmetry: Double, reducedGap: Double): Double {
    require(reducedGap > 0.0) { "reducedGap must be positive: $reducedGap" }
    return asymmetry * asymmetry / abs(asymmetryFunction(asymmetry)) *
            exp(-2.0 * asymmetry * reducedGap)
}

/**
 * `D̃*(ζ) = (1+ζ)/|ζ|` — the reduced separation at which the mean-field pressure between two
 * oppositely charged walls changes sign.
 *
 * **Derived** from Kanduč Eq. (18), `tan(2αa) = α(ζ+1)μ/(α²μ² − ζ)`, in the `α → 0` limit where
 * `p̃₀ = α̃² → 0`: both sides are then linear in `α` and the equality fixes `2a/μ = −(1+ζ)/ζ`.
 * Below it the walls repel (Eq. 64's branch), above it they attract (Eq. 65's), and **at** it the
 * paper is explicit that neither criterion applies — *"the leading order term is zero and the
 * fluctuations are dominant at any finite value of Ξ"*.
 */
fun meanFieldPressureSignChangeReducedGap(asymmetry: Double): Double {
    require(asymmetry > -1.0 && asymmetry < 0.0) {
        "the asymmetry parameter must lie in (-1, 0): $asymmetry"
    }
    return (1.0 + asymmetry) / (-asymmetry)
}

/**
 * The asymmetry parameter closest to zero that still admits an **attractive** mean-field pressure
 * at [reducedGap] — the inverse of [meanFieldPressureSignChangeReducedGap], `ζ = −1/(1 + D̃)`.
 *
 * A wall convention therefore constrains the electrode: because the Gen-1 gap force **is** an
 * attraction (`C-0008` solves it), the reading that gives the smaller `D̃` demands the larger
 * `|ζ|`.
 */
fun attractiveBranchAsymmetryCeiling(reducedGap: Double): Double {
    require(reducedGap >= 0.0) { "reducedGap must not be negative: $reducedGap" }
    return -1.0 / (1.0 + reducedGap)
}

/**
 * The infimum of Kanduč Eq. (65)'s bound over every asymmetry that admits attraction at
 * [reducedGap] — attained **at the branch boundary**, `ζ = −1/(1 + D̃)`, so it is a closed form.
 *
 * The bound diverges at both ends of the branch — as `ζ → 0⁻` through the prefactor and as
 * `ζ → −1⁻` because `f(ζ) → 0` there — and it is monotone in between, so the smallest value it
 * takes on the branch sits exactly where the branch begins. That the scan
 * [attractiveBranchScannedInfimumCoupling] reproduces it is the gate; that it lands within
 * `0.84–1.14×` of [weakCouplingValidityCoupling] at every reduced separation from 2 to 210 is the
 * finding, and it is what *"the right hand side here is exponentially large"* is **not**: the
 * exponential is at fixed interior `ζ`, and at the branch's own boundary the two criteria agree
 * to within 16 %, as continuity of the physics across `p₀ = 0` requires.
 */
fun attractiveBranchInfimumCoupling(reducedGap: Double): Double =
    attractiveBranchValidityCoupling(attractiveBranchAsymmetryCeiling(reducedGap), reducedGap)

/**
 * The same infimum by a scan on `ζ ∈ (−1, ζ_ceiling]` with [samples] points — the gate on
 * [attractiveBranchInfimumCoupling]'s closed form, and the only place the branch's shape is
 * measured rather than asserted.
 */
fun attractiveBranchScannedInfimumCoupling(reducedGap: Double, samples: Int): Double {
    require(samples > 1) { "samples must exceed one: $samples" }
    val ceiling = attractiveBranchAsymmetryCeiling(reducedGap)
    val floor = -1.0 + 1e-9
    var best = Double.MAX_VALUE
    for (index in 0 until samples) {
        val zeta = floor + (ceiling - floor) * index / (samples - 1.0)
        if (zeta <= -1.0 || zeta >= 0.0) continue
        val bound = attractiveBranchValidityCoupling(zeta, reducedGap)
        if (bound < best) best = bound
    }
    return best
}

/**
 * The asymmetry parameter at which Kanduč Eq. (65) becomes an equality for [coupling] at
 * [reducedGap], or `null` where the criterion holds over the whole attractive branch.
 *
 * Because the bound is monotone in `|ζ|` away from the branch boundary, the criterion is
 * satisfied for **every** `ζ` more negative than this and violated between it and the boundary.
 * The width of that excluded sliver — 0.50 % of the branch at the bare duplex reading and 0.23 %
 * at the smeared bare face, and **zero** at both renormalised readings — is what the whole
 * `16.5×` wall disagreement is worth once the device's own branch is used.
 */
fun attractiveBranchAsymmetryThreshold(coupling: Double, reducedGap: Double): Double? {
    require(coupling > 0.0) { "coupling must be positive: $coupling" }
    val ceiling = attractiveBranchAsymmetryCeiling(reducedGap)
    if (attractiveBranchValidityCoupling(ceiling, reducedGap) > coupling) return null
    var low = -1.0 + 1e-12
    var high = ceiling
    repeat(200) {
        val middle = 0.5 * (low + high)
        if (attractiveBranchValidityCoupling(middle, reducedGap) > coupling) low = middle
        else high = middle
    }
    return 0.5 * (low + high)
}
