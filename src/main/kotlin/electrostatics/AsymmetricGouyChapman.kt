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
import kotlin.math.cosh
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.ln1p
import kotlin.math.sign
import kotlin.math.sinh
import kotlin.math.sqrt
import kotlin.math.tanh

/**
 * The **asymmetric 2:1** planar double layer, in closed form — task `T-3a`, leaf `A7.4`.
 *
 * ## Why this file exists at all
 *
 * `C-0005` had to quote its charge-saturation ceiling `σ_eff = κ/(π l_B q)` from the
 * **symmetric `z:z`** Gouy-Chapman solution, and flagged it explicitly as an
 * order-of-magnitude ceiling because `MgCl₂` is 2:1 and asymmetric. This file replaces it
 * with the 2:1 result, derived rather than adapted.
 *
 * ## The derivation, so the constants are not magic
 *
 * With `y = eψ/k_BT` (the **valency-free** reduced potential — the valencies live in the
 * Boltzmann factors, not in `y`), the 2:1 Poisson-Boltzmann equation is
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`y'' = −4π l_B (2c e^{−2y} − 2c e^{y}) = −(κ²/3)(e^{−2y} − e^{y})`
 *
 * with `κ² = 24π l_B c = 8π l_B I` and `I = 3c` — the 2:1 ionic strength, not `c`.
 * Its first integral, with `y → 0` and `y' → 0` in the bulk, is
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`(y'/κ)² = f(y) ≡ (e^{−2y} + 2e^{y} − 3)/3`
 *
 * — the **replacement for the symmetric `sinh` form**, and it is not even in `y`.
 * Substituting `u = e^y` factorises it, `3u² f = (u−1)²(2u+1)`, and the quadrature is
 * elementary in `w = sqrt(2u+1)`:
 *
 * - **negative wall** (`Mg²⁺` counterion): `w = √3 tanh((κz + ln G)/2)`, `G = (√3+w₀)/(√3−w₀)`,
 *   far field `y → −(6/G) e^{−κz}`, saturating at `A = −(12 − 6√3) = −1.6077`;
 * - **positive wall** (`Cl⁻` counterion): `w = √3(1+S)/(1−S)`, `S = H e^{−κz}`,
 *   `H = (w₀−√3)/(w₀+√3)`, far field `y → 6H e^{−κz}`, saturating at `A = +6`.
 *
 * The two saturations differ by exactly `2 + √3 = 3.732`. **A 2:1 electrolyte does not
 * screen the two signs of surface charge equally**, and that single fact is why the
 * symmetric closed form cannot be used for this device: the tile is negative and the
 * electrode is positive, so both branches are needed and they are not each other's mirror.
 *
 * ## The effective charge
 *
 * `σ_eff` is defined here as the charge a *linearised* (Debye-Hückel) plate would need to
 * reproduce the true far field: `y → (4π l_B σ_eff/κ) e^{−κz}`, i.e. `σ_eff = κ A/(4π l_B)`.
 * Applied to the symmetric case this definition returns `κ/(π l_B z)` identically, which is
 * how it is checked against `C-0005`.
 */

/**
 * `12 − 6√3 = 1.60770` — the saturated far-field amplitude, in units of `k_BT/e`, of a
 * **negatively** charged plane in a 2:1 electrolyte, where the counterion is the divalent one.
 *
 * The symmetric 2:2 form would give 2 and the symmetric 1:1 form 4. **DERIVED** above.
 */
const val SATURATED_AMPLITUDE_DIVALENT_COUNTERION: Double = 1.6076951545867364

/**
 * `6` — the saturated far-field amplitude of a **positively** charged plane in a 2:1
 * electrolyte, where the counterion is the monovalent one and the coion is divalent.
 *
 * Larger than the symmetric 1:1 value of 4, because the divalent coion is expelled harder
 * than a monovalent one and therefore contributes less screening. **DERIVED** above.
 */
const val SATURATED_AMPLITUDE_MONOVALENT_COUNTERION: Double = 6.0

/** `√3`, which is `w` in the bulk and appears in every branch below. */
private val ROOT_THREE: Double = sqrt(3.0)

/**
 * Returns the first integral `f(y) = (y'/κ)² = (e^{−2y} + 2e^{y} − 3)/3` of the 2:1
 * Poisson-Boltzmann equation.
 *
 * Vanishes at `y = 0` and equals `y²` to leading order there — which is what fixes `κ` as
 * the 2:1 Debye length and not a monovalent one. It is **not** even in `y`: `f(2) = 5.19`
 * against `f(−2) = 20.6`. The symmetric `2 sinh²(y/2)`-type form is simply not available here.
 */
fun asymmetricFirstIntegral(reducedPotential: Double): Double =
    (exp(-2.0 * reducedPotential) + 2.0 * exp(reducedPotential) - 3.0) / 3.0

/**
 * Returns the **signed** surface charge density in `e/nm²` that holds a planar wall at
 * reduced surface potential [reducedSurfacePotential] in a 2:1 electrolyte —
 * the 2:1 replacement for the Grahame equation.
 *
 * `σ = sign(y₀) κ sqrt(f(y₀)) / (4π l_B)`.
 */
fun asymmetricSurfaceChargeDensity(
    reducedSurfacePotential: Double,
    inverseDebyeLength: Double,
    bjerrumLength: Double
): Double = sign(reducedSurfacePotential) * inverseDebyeLength *
        sqrt(asymmetricFirstIntegral(reducedSurfacePotential)) / (4.0 * PI * bjerrumLength)

/**
 * Returns the **signed** reduced surface potential of a wall carrying
 * [surfaceChargeDensity] `e/nm²` — the inverse of [asymmetricSurfaceChargeDensity].
 *
 * Bisected rather than solved in closed form: `f` is monotone away from `y = 0` on each
 * branch, so bisection is unconditionally convergent, and the inverse of the 2:1 Grahame
 * relation is a cubic in `e^{y/1}` whose closed form is longer than the bracket.
 */
fun asymmetricReducedSurfacePotential(
    surfaceChargeDensity: Double,
    inverseDebyeLength: Double,
    bjerrumLength: Double
): Double {
    if (surfaceChargeDensity == 0.0) return 0.0
    val target = abs(surfaceChargeDensity)
    val direction = sign(surfaceChargeDensity)
    var low = 0.0
    var high = 80.0
    repeat(200) {
        val middle = 0.5 * (low + high)
        val charge = inverseDebyeLength * sqrt(asymmetricFirstIntegral(direction * middle)) /
                (4.0 * PI * bjerrumLength)
        if (charge < target) low = middle else high = middle
    }
    return direction * 0.5 * (low + high)
}

/**
 * Returns the **signed** far-field amplitude `A` of the reduced potential,
 * `y(z) → A e^{−κz}`, for a 2:1 electrolyte at surface potential [reducedSurfacePotential].
 *
 * Reduces to `y₀` itself in the Debye-Hückel limit and saturates at
 * `∓`[SATURATED_AMPLITUDE_DIVALENT_COUNTERION] / `±`[SATURATED_AMPLITUDE_MONOVALENT_COUNTERION].
 */
fun asymmetricFarFieldAmplitude(reducedSurfacePotential: Double): Double {
    if (reducedSurfacePotential == 0.0) return 0.0
    val w0 = sqrt(2.0 * exp(reducedSurfacePotential) + 1.0)
    return if (reducedSurfacePotential < 0.0) {
        -6.0 * (ROOT_THREE - w0) / (ROOT_THREE + w0)
    } else {
        6.0 * (w0 - ROOT_THREE) / (w0 + ROOT_THREE)
    }
}

/**
 * Returns the reduced potential at height [height] nm above a planar wall held at
 * [reducedSurfacePotential] in a 2:1 electrolyte — the exact profile, both branches.
 *
 * ## Written in `ln1p` form deliberately
 *
 * The profile is `y = ln((w² − 1)/2)` with `w → √3` in the bulk, so the naive expression
 * subtracts two nearly equal numbers and then takes a logarithm of something near 1. At
 * `κz ≈ 25` that costs **every significant digit** — the same failure mode `CLAUDE.md`
 * records for `1 − tanh(x)/x` in the Brinkman transmissivity. Both branches are therefore
 * rearranged so the small quantity is formed directly:
 * `u − 1 = −3δ(2 − δ)/2` with `δ = 1 − tanh X` on the negative branch, and
 * `u − 1 = 6S/(1 − S)²` on the positive one.
 *
 * @throws IllegalArgumentException if [height] is negative.
 */
fun asymmetricPotentialProfile(
    height: Double,
    reducedSurfacePotential: Double,
    inverseDebyeLength: Double
): Double {
    require(height >= 0.0) { "height must not be negative, was: $height" }
    if (reducedSurfacePotential == 0.0) return 0.0
    val w0 = sqrt(2.0 * exp(reducedSurfacePotential) + 1.0)
    return if (reducedSurfacePotential < 0.0) {
        val g = (ROOT_THREE + w0) / (ROOT_THREE - w0)
        val exponential = exp(-(inverseDebyeLength * height + ln(g)))
        val complement = 2.0 * exponential / (1.0 + exponential) // 1 - tanh(X), formed directly
        ln1p(-1.5 * complement * (2.0 - complement))
    } else {
        val s = (w0 - ROOT_THREE) / (w0 + ROOT_THREE) * exp(-inverseDebyeLength * height)
        ln1p(6.0 * s / ((1.0 - s) * (1.0 - s)))
    }
}

/**
 * Returns the **signed** effective (far-field) surface charge density in `e/nm²`,
 * `σ_eff = κ A/(4π l_B)` — the charge a linearised plate would need to reproduce the true
 * far field of a plate at [reducedSurfacePotential].
 *
 * This is the quantity `C-0005` could only bound. It equals the bare charge in the
 * Debye-Hückel limit and saturates at [asymmetricSaturatedEffectiveChargeDensity].
 */
fun asymmetricEffectiveChargeDensity(
    reducedSurfacePotential: Double,
    inverseDebyeLength: Double,
    bjerrumLength: Double
): Double = inverseDebyeLength * asymmetricFarFieldAmplitude(reducedSurfacePotential) /
        (4.0 * PI * bjerrumLength)

/**
 * Returns the **magnitude** of the saturated effective charge density in `e/nm²` of a 2:1
 * electrolyte, for a [negativeSurface] (divalent counterion) or a positive one.
 *
 * 0.0456 e/nm² and 0.1703 e/nm² at 2 mM `MgCl₂`, 300 K. The symmetric `z:z` reading that
 * `C-0005` had to use, [saturatedEffectiveChargeDensity], gives 0.0568 e/nm² for `q = 2`:
 * a ceiling, exactly `6 − 3√3 = 0.804` of which the asymmetric solve actually uses.
 */
fun asymmetricSaturatedEffectiveChargeDensity(
    inverseDebyeLength: Double,
    bjerrumLength: Double,
    negativeSurface: Boolean
): Double {
    val amplitude = if (negativeSurface) SATURATED_AMPLITUDE_DIVALENT_COUNTERION
    else SATURATED_AMPLITUDE_MONOVALENT_COUNTERION
    return inverseDebyeLength * amplitude / (4.0 * PI * bjerrumLength)
}

/**
 * Returns the far-field amplitude of a **symmetric** `z:z` electrolyte,
 * `A = (4/z) tanh(z y₀/4)`, in the same valency-free reduced potential.
 *
 * Carried only as a cross-check: applying [asymmetricEffectiveChargeDensity]'s definition of
 * `σ_eff` to this amplitude returns `κ/(π l_B z)`, which is the expression `C-0005` quotes.
 * Keeping both in one place is what makes the 2:1 correction auditable rather than asserted.
 */
fun symmetricFarFieldAmplitude(reducedSurfacePotential: Double, valency: Int): Double {
    require(valency > 0) { "valency must be positive, was: $valency" }
    return 4.0 / valency * tanh(valency * reducedSurfacePotential / 4.0)
}

/**
 * Returns the **linearised** (Debye-Hückel) disjoining pressure in `k_BT/nm³` between an
 * electrode held at reduced potential [electrodeReducedPotential] and a tile carrying a
 * fixed [tileSurfaceChargeDensity] `e/nm²`, separated by [gapHeight] nm.
 *
 * ## The cheap bound, and why it is the right one to run first
 *
 * The mixed boundary-value problem — **constant potential** at the electrode, **constant
 * charge** at the tile — has a two-line closed form in the linear theory:
 * `y = y_d cosh κz + B sinh κz` with `B = (s/κ − y_d sinh κh)/cosh κh` and `s = 4π l_B σ_t`,
 * and the osmotic-minus-Maxwell first integral collapses to a difference of two squares:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`P = k_BT κ² (y_d² − B²) / (8π l_B)`
 *
 * exactly constant in `z`, as it must be. Two limits are worth naming because they are the
 * whole of `CH-0004`:
 *
 * - at `y_d = 0` the pressure is `−k_BT s²/(8π l_B cosh²κh)`, decaying as **`e^{−2κh}`** —
 *   the tile interacting with its own image in the grounded conductor, decay length `λ_D/2`;
 * - at finite bias the leading term is `4 κ y_d s e^{−κh}/(8π l_B)`, decaying as **`e^{−κh}`** —
 *   decay length `λ_D`.
 *
 * So "the" decay length of the electrostatic force is not one number even in the linear
 * theory, and it is bias-dependent. The nonlinear solve in `PoissonBoltzmannGap` is what
 * says how far from these two the working range actually sits.
 *
 * @throws IllegalArgumentException if [gapHeight] is not positive.
 */
fun linearMixedDisjoiningPressure(
    gapHeight: Double,
    electrodeReducedPotential: Double,
    tileSurfaceChargeDensity: Double,
    inverseDebyeLength: Double,
    bjerrumLength: Double
): Double {
    require(gapHeight > 0.0) { "gapHeight must be positive, was: $gapHeight" }
    val reduced = inverseDebyeLength * gapHeight
    val slope = 4.0 * PI * bjerrumLength * tileSurfaceChargeDensity
    val b = (slope / inverseDebyeLength - electrodeReducedPotential * sinh(reduced)) / cosh(reduced)
    return inverseDebyeLength * inverseDebyeLength *
            (electrodeReducedPotential * electrodeReducedPotential - b * b) /
            (8.0 * PI * bjerrumLength)
}
