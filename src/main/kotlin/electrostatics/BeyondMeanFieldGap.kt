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
import kotlin.math.max
import kotlin.math.sqrt

/**
 * `T-50` — the arithmetic that turns a **beyond-mean-field multiplier** into a movement of
 * `C-0017`'s stability margin, and the two bulk screening-length corrections that bound its
 * far-field gradient.
 *
 * ## The one object
 *
 * Write the true electrostatic force on the tile as a multiplier on the mean-field one,
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`|F_true(h, V)| = μ(h, V) · |F_PB(h, V)|`,
 *
 * dimensionless, `μ > 1` an enhancement. `C-0005`'s 123–214 % one-loop correction is a statement
 * about the **level** of `μ − 1` at a gap. `C-0017`'s margin is a statement about a **stiffness**.
 * `CH-0035` shows those are not the same quantity: at a **force-pinned** operating point the level
 * is absorbed entirely into the bias, so only
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`g ≡ d ln μ/dh` (nm⁻¹, at fixed applied bias)
 *
 * survives into `k_es`. This file is that arithmetic and nothing else; the solves that measure `g`
 * for individual members of the family live in `BeyondMeanFieldGapStudy`.
 *
 * ## Sign conventions
 *
 * Inherited unchanged from `C-0017` and `C-0033`. `k_es = |F_es| d ln|F_es|/dh` and
 * `ℓ = −1/(d ln|F_es|/dh)`, so `k_es = −|F_es|/ℓ` identically and `k_es < 0` above the force
 * maximum. `g > 0` means the true force decays **more slowly** than the mean-field one, which
 * makes `|k_es|` **smaller** and the actuator **more** stable.
 */

/**
 * The effective stiffness under a multiplier gradient [gradient], at a **force-pinned** operating
 * point where `|F_es|` is fixed at [pinnedForce] by the balance against the load line.
 *
 * `k_eff = k_brush + k_es` and `k_es = |F|(g − 1/ℓ)`, so the whole effect of the multiplier is the
 * single additive term `|F| g`. Exactly linear in `g`, with no re-solve of anything.
 */
fun effectiveStiffnessUnderGradient(
    meanFieldEffectiveStiffness: Double,
    pinnedForce: Double,
    gradient: Double
): Double = meanFieldEffectiveStiffness + pinnedForce * gradient

/**
 * `C-0017`'s stability floor under a multiplier gradient — `max(0, −k_eff)`, because a positive
 * `k_eff` needs no coupling stiffness at all to be stable.
 */
fun stabilityFloorUnderGradient(
    meanFieldEffectiveStiffness: Double,
    pinnedForce: Double,
    gradient: Double
): Double = max(0.0, -effectiveStiffnessUnderGradient(meanFieldEffectiveStiffness, pinnedForce, gradient))

/**
 * The gradient at which `C-0017`'s margin reaches exactly one — the **threshold** `T-50` owes.
 *
 * `floor(g) = mandate` gives `g* = (floor(0) − mandate)/|F|`, one division per state and no field
 * solve. It is **negative** wherever the margin is above one, i.e. the true force would have to
 * decay *faster* than the mean-field one for the verdict to change.
 *
 * Returns `null` where the mean-field floor is already zero *and* stays zero — there is no
 * finite gradient at which such a state acquires a floor of exactly the mandate from below without
 * first passing through the sign change, which the caller handles by evaluating the floor directly.
 */
fun thresholdGradient(
    meanFieldEffectiveStiffness: Double,
    pinnedForce: Double,
    mandatedStiffness: Double
): Double = (-meanFieldEffectiveStiffness - mandatedStiffness) / pinnedForce

/**
 * The decay length the true force must have to realise a multiplier gradient [gradient], from
 * `1/ℓ_true = 1/ℓ_PB − g`.
 *
 * Returns `null` when `g ≥ 1/ℓ_PB`, where the true force no longer decays at all — the
 * unscreened-capacitor limit and beyond.
 */
fun decayLengthUnderGradient(meanFieldDecayLength: Double, gradient: Double): Double? {
    val inverse = 1.0 / meanFieldDecayLength - gradient
    return if (inverse > 0.0) 1.0 / inverse else null
}

/**
 * The rigorous **favourable** ceiling on the gradient: `g < 1/ℓ_PB`.
 *
 * At `g = 1/ℓ_PB` the true force is gap-independent, which is the bare-capacitor limit of two
 * oppositely charged plates with no mobile ions between them at all. Nothing can screen *less*
 * than nothing, so no correction of any kind reaches a larger positive gradient.
 */
fun unscreenedGradientCeiling(meanFieldDecayLength: Double): Double = 1.0 / meanFieldDecayLength

/**
 * The mean-spherical-approximation screening parameter `2Γ` of a restricted primitive model
 * electrolyte of ion diameter [ionDiameter] whose point-ion Debye value is [inverseDebyeLength].
 *
 * `Γ = (√(1 + 2κσ) − 1)/(2σ)`, the standard closed form; `2Γ → κ` as `σ → 0` and `2Γ < κ` for
 * every positive diameter, so **finite ion size lengthens the screening length**. The asymptotic
 * decay of the pair correlation — and therefore of the double-layer interaction — is `1/(2Γ)`
 * rather than `1/κ`, so this is the *bulk* channel through which any correction can change the
 * far-field gradient.
 *
 * **CITED formula, DERIVED evaluation**, and its `σ → 0` limit is asserted as a gate rather than
 * assumed.
 */
fun msaInverseScreeningLength(inverseDebyeLength: Double, ionDiameter: Double): Double {
    require(inverseDebyeLength > 0.0) { "inverseDebyeLength must be positive: $inverseDebyeLength" }
    require(ionDiameter >= 0.0) { "ionDiameter must not be negative: $ionDiameter" }
    // The textbook form is (sqrt(1 + 2 k s) - 1)/s, which loses every significant digit to
    // cancellation as s -> 0 - exactly the limit gate 2 asserts. Rationalised, it is exact,
    // needs no special case at s = 0, and returns kappa there by construction.
    return 2.0 * inverseDebyeLength / (1.0 + sqrt(1.0 + 2.0 * inverseDebyeLength * ionDiameter))
}

/**
 * Bjerrum's critical separation `q = |z₊z₋| l_B/2` — the distance at which the pair's Coulomb
 * energy is `2 k_BT` and below which the integrand of the association integral turns upward.
 */
fun bjerrumCriticalSeparation(bjerrumLength: Double, valencyProduct: Int): Double =
    valencyProduct * bjerrumLength / 2.0

/**
 * Bjerrum's association volume `K_A = 4π ∫_a^q r² exp(|z₊z₋| l_B/r) dr` in nm³, for a contact
 * distance [contactDistance] and Bjerrum cut-off `q`.
 *
 * **Zero when `a ≥ q`** — the pair never gets close enough to bind, which is not a degenerate case
 * here but the answer for *hydrated* Mg²⁺ and Cl⁻, whose contact distance is larger than `q`.
 * Simpson quadrature on [panels] intervals; the integrand is smooth and positive on `[a, q]`.
 */
fun bjerrumAssociationVolume(
    bjerrumLength: Double,
    valencyProduct: Int,
    contactDistance: Double,
    panels: Int = 2000
): Double {
    require(panels > 0 && panels % 2 == 0) { "panels must be positive and even: $panels" }
    val critical = bjerrumCriticalSeparation(bjerrumLength, valencyProduct)
    if (contactDistance >= critical) return 0.0
    val step = (critical - contactDistance) / panels
    fun integrand(r: Double) = r * r * exp(valencyProduct * bjerrumLength / r)
    var total = integrand(contactDistance) + integrand(critical)
    for (i in 1 until panels) {
        total += (if (i % 2 == 1) 4.0 else 2.0) * integrand(contactDistance + i * step)
    }
    return 4.0 * PI * total * step / 3.0
}

/**
 * The fraction of the Mg²⁺ that is bound into `MgCl⁺` pairs at mass-action equilibrium, for a
 * MgCl₂ solution of number density [magnesiumNumberDensity] (nm⁻³) and association volume
 * [associationVolume] (nm³).
 *
 * `p = K (M − p)(2M − p)` with `M` the total Mg²⁺ and `2M` the total Cl⁻; the physical root is the
 * smaller one, and the fraction returned is `p/M ∈ [0, 1)`.
 */
fun pairedMagnesiumFraction(magnesiumNumberDensity: Double, associationVolume: Double): Double {
    require(magnesiumNumberDensity > 0.0) { "density must be positive: $magnesiumNumberDensity" }
    if (associationVolume <= 0.0) return 0.0
    val m = magnesiumNumberDensity
    // K p² − (1 + 3 K M) p + 2 K M² = 0
    val a = associationVolume
    val b = -(1.0 + 3.0 * associationVolume * m)
    val c = 2.0 * associationVolume * m * m
    val discriminant = b * b - 4.0 * a * c
    val root = (-b - sqrt(discriminant)) / (2.0 * a)
    return root / m
}

/**
 * The ionic strength in mM of a MgCl₂ solution of concentration [concentration] mM in which a
 * fraction [pairedFraction] of the Mg²⁺ has associated into the **singly charged** `MgCl⁺`.
 *
 * `I = ½ Σ c_i z_i²` over `Mg²⁺`, `MgCl⁺` and `Cl⁻` gives `I = c(6 − 4α)`, which is `3c` at
 * `α = 0` — `CLAUDE.md`'s 2:1 rule — and `c` at full association, where the solution is a 1:1
 * electrolyte of `MgCl⁺` and `Cl⁻`.
 */
fun associatedIonicStrength(concentration: Double, pairedFraction: Double): Double {
    require(pairedFraction in 0.0..1.0) { "paired fraction out of range: $pairedFraction" }
    return concentration * (6.0 - 4.0 * pairedFraction) / 2.0
}

// ------------------------------------------------------------------ what the literature supplies

/**
 * The reduced ion diameter `d κ_D` below which the bulk charge decay length of a primitive-model
 * electrolyte **is** the Debye length.
 *
 * Cats, Evans, Härtel & van Roij (*J. Chem. Phys.* **154** (2021) 124504, §V, read directly):
 * *"At low concentrations, `dκ_D < 0.5`, all the approaches we consider agree that `ξ_Z` is close
 * to `κ_D⁻¹`…"* — MSA integral-equation theory, two classical DFTs and molecular dynamics. Above
 * it, `ξ_Z` is found **smaller** than the Debye length, which is the adverse direction, so this
 * constant is the edge of the window in which `d ln μ/dh` has no bulk contribution at all.
 */
const val LIMITING_LAW_REDUCED_DIAMETER: Double = 0.5

/**
 * The Kirkwood crossover of the restricted primitive model in the same reduced variable — the
 * concentration above which the decay stops being monotone and becomes damped-oscillatory.
 *
 * `x_K^IET ≈ 1.229` (Cats et al. 2021, §IV B 2, read directly); the same paper's two DFTs give
 * 1.24 and 0.7004 and its MD ≈ 1.37, and Lee & Fisher's generalised Debye-Hückel gives 1.17832
 * against Kirkwood's own 1.03. **CITED**, and the spread is the reason this is quoted as a
 * distance rather than as a boundary.
 */
const val KIRKWOOD_REDUCED_DIAMETER: Double = 1.229

/**
 * Kanduč, Trulsson, Naji, Burak, Forsman & Podgornik (arXiv:0905.3851) Eq. (64): the loop
 * expansion about mean field is controlled, for a **repulsive** mean-field pressure at large
 * reduced separation, when `Ξ < D̃/ln D̃` with `D̃ = D/μ_GC`.
 *
 * Their Eq. (65) is the **attractive** branch — the oppositely charged case, which is this
 * device's — and its right-hand side is *"exponentially large"*, so it is not implemented as a
 * number: what it says is that the criterion below is the **conservative** one here.
 */
fun weakCouplingValidityCoupling(reducedGap: Double): Double {
    require(reducedGap > 1.0) { "the criterion is asymptotic in D/mu_GC: $reducedGap" }
    return reducedGap / kotlin.math.ln(reducedGap)
}

/**
 * The dressed-ion second-virial coefficient `b = c₀ 2π l_B q²/κ²` of Kanduč, Moazzami-Gudarzi,
 * Valmacco, Podgornik & Trefalt (arXiv:1701.08989) Eq. (20), for a **pure `q:1` salt**.
 *
 * With `κ² = 4π l_B c₀ q(q+1)` the concentration, the Bjerrum length and the temperature all
 * cancel and `b = q/(2(q+1))` **exactly** — 1/4, **1/3**, 3/8, 2/5 at `q = 1, 2, 3, 4`. It is the
 * closed form for the one quantity `T-50` wants, and the cancellation is why it is worth having:
 * the correction it predicts to the decay rate is a pure number times `κ`.
 */
fun dressedIonSecondVirialCoefficient(counterionValency: Int): Double {
    require(counterionValency > 0) { "valency must be positive: $counterionValency" }
    val q = counterionValency.toDouble()
    return q / (2.0 * (q + 1.0))
}

/**
 * The exponential integral `Ei(x)` for `x > 0`, by its convergent series
 * `Ei(x) = γ + ln x + Σ_{k≥1} x^k/(k·k!)`.
 *
 * Not [exponentialIntegralE1], which is a different function: `E₁` is the integral outward from
 * `x` and `Ei` the principal value inward, and the dressed-ion constant below needs the second.
 */
private const val EULER_GAMMA: Double = 0.5772156649015329

fun exponentialIntegralEi(x: Double, terms: Int = 400): Double {
    require(x > 0.0) { "Ei is implemented for positive argument only: $x" }
    var term = 1.0
    var total = 0.0
    for (k in 1..terms) {
        term *= x / k
        total += term / k
    }
    return EULER_GAMMA + kotlin.math.ln(x) + total
}

/**
 * The constant `C` of Kanduč et al. (arXiv:1701.08989) Eq. (21),
 * `C = 3/2 − 2γ + 2 ln(κμ) + 2κμ e^(1/κμ) + 2 Ei(1/κμ)`, in the reduced Gouy-Chapman length `κμ`.
 *
 * It is the whole of that theory's validity statement: the modulating factor is
 * `K(h) = 1 − b(C + κh)`, so `bC` must be small against one for the second-virial expansion to
 * mean anything, and `C` grows like `e^(1/κμ)` as the wall gets more strongly charged.
 */
fun dressedIonValidityConstant(reducedGouyChapmanLength: Double): Double {
    require(reducedGouyChapmanLength > 0.0) { "must be positive: $reducedGouyChapmanLength" }
    val inverse = 1.0 / reducedGouyChapmanLength
    return 1.5 - 2.0 * EULER_GAMMA + 2.0 * kotlin.math.ln(reducedGouyChapmanLength) +
        2.0 * reducedGouyChapmanLength * exp(inverse) + 2.0 * exponentialIntegralEi(inverse)
}
