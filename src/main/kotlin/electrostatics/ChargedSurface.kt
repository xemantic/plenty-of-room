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

import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * The electrostatic coupling parameter and the boundary of Poisson-Boltzmann validity.
 *
 * ## The one source
 *
 * Every expression in this file comes from
 * **A. Naji, S. Jungblut, A. G. Moreira, R. R. Netz,
 * *Electrostatic interactions in strongly-coupled soft matter*, Physica A 352:131 (2005)**
 * (arXiv:cond-mat/0508767), read rather than recalled, with the paper's equation numbers
 * quoted at each use. `CLAUDE.md`'s research-practice rule earned its keep here: four
 * arXiv identifiers recalled from memory all resolved to unrelated papers.
 *
 * ## The framing
 *
 * Poisson-Boltzmann is the `Ξ → 0` saddle point. `Ξ` is the loop parameter of the expansion
 * about it, so "how wrong is mean field" has a literal answer — the ratio of the one-loop
 * term to the leading term — and that ratio is what [meanFieldDeviation] returns.
 * `Ξ ≳ 1` is where correlations start to matter; `Ξ ≫ 1` is the strong-coupling regime;
 * Naji et al. put the practical boundaries at `Ξ ∼ 1` and `Ξ ∼ 10²`.
 *
 * ## What is deliberately not here
 *
 * Salt. The Naji/Netz results are derived for the **counterion-only** problem.
 * That is not a defect for the Gen-1 gap — see [CounterionDominatedGap], which shows the
 * gap holds 3 to 33 times more of the tile's own counterions than the bulk buffer supplies,
 * so the counterion-only limit is the *appropriate* one there and not merely the available
 * one. Where salt would matter it is named in the claim.
 */

/** Naji Fig. 5b: like-charge attraction between two walls sets in above this coupling. */
const val ATTRACTION_ONSET_COUPLING: Double = 12.0

/** Naji §IV C: the first-order unbinding transition of two like-charged walls. */
const val UNBINDING_TRANSITION_COUPLING: Double = 17.0

/** Naji §V: with a dielectric jump at the walls the attraction onset moves here. */
const val IMAGE_CHARGE_ATTRACTION_ONSET_COUPLING: Double = 30.0

/** Naji §II: Wigner crystallisation of the 2D one-component plasma, `Γ_c ≈ 125`. */
const val WIGNER_CRYSTAL_PLASMA_PARAMETER: Double = 125.0

/** `ζ(3)`, Apéry's constant, as it appears in Naji Eq. (19). */
private const val APERY_CONSTANT: Double = 1.2020569031595943

/**
 * A uniformly charged planar surface with its neutralising counterions.
 *
 * @param surfaceChargeDensity `σ_s` in `e/nm²`, the magnitude of the areal charge.
 * @param counterionValency `q`, the valency of the neutralising counterion — 2 for `Mg²⁺`.
 */
@Serializable
data class ChargedSurface(
    val surfaceChargeDensity: Double,
    val counterionValency: Int
) {

    init {
        require(surfaceChargeDensity > 0.0) {
            "surfaceChargeDensity must be positive, was: $surfaceChargeDensity"
        }
        require(counterionValency > 0) {
            "counterionValency must be positive, was: $counterionValency"
        }
    }

    /** The same charge density in `C/m²`, the unit the electrochemistry literature uses. */
    val surfaceChargeDensityInCoulombPerSquareMetre: Double
        get() = surfaceChargeDensity * ELEMENTARY_CHARGE * 1e18

    /**
     * Returns the Gouy-Chapman length `μ = 1/(2π q l_B σ_s)` in nm — Naji Eq. (3).
     *
     * The distance at which the counterion-wall attraction equals `k_BT`, and the thickness
     * of the counterion layer. For the Gen-1 tile it is 0.12 nm, which is **smaller than the
     * radius of a hydrated `Mg²⁺` ion** — the continuum picture has no room to be right
     * inside it, which is a conclusion about the model rather than about the system.
     */
    fun gouyChapmanLength(bjerrumLength: Double): Double =
        1.0 / (2.0 * PI * counterionValency * bjerrumLength * surfaceChargeDensity)

    /**
     * Returns the electrostatic coupling parameter `Ξ = 2π q³ l_B² σ_s` — Naji Eq. (4).
     *
     * Equivalently `Ξ = q² l_B / μ`. The cube in the valency is the whole story of this
     * task: the same DNA surface has `Ξ = 3.0` with `Na⁺` and `Ξ = 24.0` with `Mg²⁺`.
     */
    fun couplingParameter(bjerrumLength: Double): Double =
        2.0 * PI * counterionValency * counterionValency * counterionValency *
                bjerrumLength * bjerrumLength * surfaceChargeDensity

    /**
     * `a_⊥ = sqrt(q/σ_s)` in nm — Naji Eq. (5), the lateral spacing of surface counterions.
     *
     * This is the convention Naji's Rouzina-Bloomfield attraction criterion Eq. (24)
     * (`Δ < a_⊥`) is stated in, so it is the one [rouzinaBloomfieldRange] uses.
     */
    val lateralCounterionSpacing: Double get() = sqrt(counterionValency / surfaceChargeDensity)

    /**
     * `a_WS = sqrt(q/(π σ_s))` in nm — the Wigner-Seitz radius of the same 2D layer.
     *
     * Differs from [lateralCounterionSpacing] by `√π = 1.772`. Both are "the lateral
     * spacing"; Naji's Eq. (5) is deliberately written with a `∼` because of it. The
     * Wigner-Seitz convention is the one that makes `Γ = sqrt(Ξ/2)` **exact** and thereby
     * reproduces the paper's own `Γ_c = 125 ↔ Ξ_c ≈ 3.1e4` correspondence, which is how the
     * prefactor gets pinned instead of guessed.
     */
    val wignerSeitzRadius: Double get() = sqrt(counterionValency / (PI * surfaceChargeDensity))

    /**
     * Returns the plasma parameter `Γ = q² l_B / a_WS` — Naji Eq. (7), with the prefactor fixed.
     *
     * Measures the mutual Coulomb repulsion within the surface counterion layer.
     * `Γ > 125` would mean a Wigner crystal; the Gen-1 tile sits at 3.5, a strongly
     * correlated *liquid*.
     */
    fun plasmaParameter(bjerrumLength: Double): Double =
        counterionValency * counterionValency * bjerrumLength / wignerSeitzRadius

    /**
     * Returns the contact counterion density `ρ(0) = 2π l_B σ_s²` in `nm⁻³`.
     *
     * Naji, after Eq. (9), is explicit that this is **exact within the model and valid
     * beyond mean field** — it follows from the contact-value theorem, not from PB.
     * That matters for `T-6`: comparing it against the close-packed density of a hydrated
     * ion is therefore a statement about the *point-ion* assumption, which PB and
     * strong-coupling theory share, rather than about mean field specifically.
     *
     * Note it does not depend on the counterion valency.
     */
    fun contactDensity(bjerrumLength: Double): Double =
        2.0 * PI * bjerrumLength * surfaceChargeDensity * surfaceChargeDensity

    /**
     * Returns the mean-field counterion density at height [height] nm — Naji Eq. (9),
     * `ρ(z) = 2π l_B σ_s² / (z/μ + 1)²`, in `nm⁻³`.
     *
     * @throws IllegalArgumentException if [height] is negative.
     */
    fun meanFieldDensity(height: Double, bjerrumLength: Double): Double {
        require(height >= 0.0) { "height must not be negative, was: $height" }
        val reduced = height / gouyChapmanLength(bjerrumLength) + 1.0
        return contactDensity(bjerrumLength) / (reduced * reduced)
    }

    /**
     * Returns the wall separation in nm below which strong-coupling attraction is possible —
     * the Rouzina-Bloomfield criterion `Δ < a_⊥`, Naji Eq. (24).
     *
     * For the Gen-1 tile this is 1.46 nm, while the polymer layer holds the tile 5-10 nm
     * off the electrode. **The qualitative failure mode of mean field is out of geometric
     * range**, however large `Ξ` is — which is the single most useful thing this task can
     * tell `T-3` and `T-4`.
     */
    val rouzinaBloomfieldRange: Double get() = lateralCounterionSpacing

}

/** Returns the coupling parameter corresponding to a plasma parameter, `Ξ = 2Γ²`. */
fun couplingParameterOfPlasmaParameter(plasmaParameter: Double): Double =
    2.0 * plasmaParameter * plasmaParameter

/**
 * Returns `Λ = βP_PB/(2π l_B σ_s²)`, the mean-field pressure between two like-charged walls
 * at reduced separation [reducedGap] `= Δ/μ` — Naji Eq. (13).
 *
 * `Λ` solves `√Λ tan(√Λ Δ/(2μ)) = 1` on the principal branch. Bisection is used because on
 * `(0, (π/r)²)` the left-hand side increases monotonically from 0 to `+∞`, so bisection is
 * unconditionally convergent and cannot be thrown out of the branch — which Newton can,
 * the tangent's pole being right at the upper bracket.
 *
 * @throws IllegalArgumentException if [reducedGap] is not positive.
 */
fun poissonBoltzmannPressureCoefficient(reducedGap: Double): Double {
    require(reducedGap > 0.0) { "reducedGap must be positive, was: $reducedGap" }
    var low = 0.0
    var high = (PI / reducedGap) * (PI / reducedGap)
    repeat(200) {
        val middle = 0.5 * (low + high)
        val root = sqrt(middle)
        if (root * tan(root * reducedGap / 2.0) - 1.0 < 0.0) low = middle else high = middle
    }
    return 0.5 * (low + high)
}

/**
 * Returns the **magnitude** of the one-loop correction coefficient
 * `|βP⁽¹⁾|/(2π l_B σ_s²)` at reduced separation [reducedGap] — Naji Eq. (19):
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`βP⁽¹⁾/(2π l_B σ_s²) ≈ −(μ/Δ)³ [ ζ(3)/4 + π³/4 + π² ln(Δ/πμ) ]`
 *
 * The sign is negative — the Gaussian fluctuation correction is **attractive**, because
 * the counterion clouds at the two walls polarise each other. The magnitude is returned so
 * that callers cannot accidentally cancel it against the repulsive leading term.
 *
 * @throws IllegalArgumentException if [reducedGap] is not above `π`, where the logarithm
 *          changes sign and the expansion is meaningless anyway.
 */
fun oneLoopPressureCoefficientMagnitude(reducedGap: Double): Double {
    require(reducedGap > PI) { "reducedGap must exceed PI, was: $reducedGap" }
    val inverse = 1.0 / reducedGap
    return inverse * inverse * inverse *
            (APERY_CONSTANT / 4.0 + PI * PI * PI / 4.0 + PI * PI * ln(reducedGap / PI))
}

/**
 * Returns the fractional deviation of the true pressure from the mean-field one,
 * `Ξ|P⁽¹⁾| / P_PB`, at [coupling] `= Ξ` and [reducedGap] `= Δ/μ`.
 *
 * **This is the number the `T-6` acceptance predicate asks for.** Naji Eq. (18) expands
 * `P = P_PB + Ξ P⁽¹⁾ + O(Ξ²)`, so the ratio of the second term to the first is literally
 * "how wrong is mean field, and by how much". A value below ~0.1 means PB is quantitatively
 * usable; a value at 1 means the expansion has broken down and PB is not merely inaccurate
 * but **uncontrolled** — one cannot even say in which direction to correct it.
 */
fun meanFieldDeviation(coupling: Double, reducedGap: Double): Double =
    coupling * oneLoopPressureCoefficientMagnitude(reducedGap) /
            poissonBoltzmannPressureCoefficient(reducedGap)

/**
 * Returns the wall separation in nm at which [meanFieldDeviation] reaches one — the
 * boundary above which the loop expansion about mean field is still controlled.
 *
 * Found by bisection, which is safe because the deviation decreases monotonically with
 * separation over the whole range where the expansion is meaningful.
 */
fun meanFieldValidityGap(coupling: Double, gouyChapmanLength: Double): Double {
    require(coupling > 0.0) { "coupling must be positive, was: $coupling" }
    require(gouyChapmanLength > 0.0) {
        "gouyChapmanLength must be positive, was: $gouyChapmanLength"
    }
    var low = 4.0 * gouyChapmanLength
    var high = 1e6 * gouyChapmanLength
    repeat(200) {
        val middle = 0.5 * (low + high)
        if (meanFieldDeviation(coupling, middle / gouyChapmanLength) > 1.0) low = middle
        else high = middle
    }
    return 0.5 * (low + high)
}

/**
 * Returns the same boundary from Naji's own closed-form criterion Eq. (20),
 * `(Δ/μ)/ln(Δ/μ) > Ξ`, in nm.
 *
 * Kept alongside [meanFieldValidityGap] deliberately: Eq. (20) drops the sub-leading terms
 * of Eq. (19), so agreement between the two is a check that Eq. (19) was transcribed
 * correctly, and disagreement would be a transcription bug rather than physics.
 */
fun loopExpansionValidityGap(coupling: Double, gouyChapmanLength: Double): Double {
    require(coupling > 0.0) { "coupling must be positive, was: $coupling" }
    var low = kotlin.math.E * 1.000001
    var high = 1e12
    repeat(300) {
        val middle = sqrt(low * high)
        if (middle / ln(middle) - coupling < 0.0) low = middle else high = middle
    }
    return sqrt(low * high) * gouyChapmanLength
}

/**
 * Returns `βP_SC/(2π l_B σ_s²) = −1 + 2μ/Δ`, the strong-coupling pressure between two
 * like-charged walls at reduced separation [reducedGap] — Naji Eq. (15).
 *
 * Negative means attraction. It changes sign at `Δ* = 2μ`, Eq. (16), and saturates at `−1`.
 * Carried here so that the `Ξ → ∞` limiting case is executable rather than asserted.
 */
fun strongCouplingPressureCoefficient(reducedGap: Double): Double {
    require(reducedGap > 0.0) { "reducedGap must be positive, was: $reducedGap" }
    return -1.0 + 2.0 / reducedGap
}
