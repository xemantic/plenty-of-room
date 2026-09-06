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

package com.xemantic.nano.plentyofroom.brush

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * The **expansion parameter** of the mean field every mechanical number in this project rests on —
 * task `T-1f`.
 *
 * ## What is being expanded about what
 *
 * `C-0011` solves the Edwards propagator exactly, but in a field `w(z) = μ(φ(z))/k_BT` built from
 * the *mean* local volume fraction. Fluctuations of the concentration field about that mean are
 * absent, and `C-0011`'s validity range says so in as many words. The leading correction is the
 * Gaussian (one-loop) one, and for the Edwards model it has the Debye-Hückel form:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`S⁻¹(q) = b²q²/(12c) + v` &nbsp;⟹&nbsp; `ξ = b/√(12 v c)`,
 * &nbsp;&nbsp;`Δf = −k_BT/(12π ξ³)`, &nbsp;&nbsp;`ΔΠ = c ∂Δf/∂c − Δf = ½ Δf`
 *
 * the last step because `Δf ∝ c^(3/2)` exactly. Against the mean-field two-body pressure
 * `Π_MF = ½ k_BT v c²` this gives a ratio which is a pure function of `c`,
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;**`Gi(φ) = |ΔΠ|/Π_MF = √(φ** over φ)`, &nbsp;&nbsp;`φ** = 12 v w/(π² b⁶)`**
 *
 * with `w` the *physical* volume of one segment. `Gi` is **independent of the chain length** — the
 * `1/N` Ginzburg number familiar from polymer *blends* comes from the chains' translational
 * entropy, which is the very term a grafted layer does not have (`C-0003`, `CLAUDE.md`).
 *
 * ## The convention trap this type exists to close
 *
 * The excluded volume `v` is a **pair** quantity and it does not coarse-grain linearly. Written on
 * monomers it is `v_m = B v₀`; written on Kuhn segments — which is what any formula containing `b`
 * requires — it is `v_K = n_K² v_m`, because the interaction is `(v/2)∫c²` and `c_K = c_m/n_K`.
 * Both readings are constructible here ([PegWater.edwardsCorrelation],
 * [PegWater.monomerEdwardsCorrelation]) and they agree to machine precision, which is the
 * executable form of `CH-0020`. `PegWater.thermalBlobKuhnSegments` does **not** agree: it
 * coarse-grains linearly and is `n_K² = 9.67` too large.
 *
 * ## Units
 *
 * `b`, `ξ` in nm; `v`, `w` in nm³; `c` in nm⁻³; `Δf`, `ΔΠ`, `Π_MF` in `pN/nm²` (= MPa exactly);
 * `Gi` and `φ` dimensionless. `φ` is always the **physical** volume fraction `c w`.
 *
 * @param segmentLength `b` in nm — the statistical segment the pair `v`/`w` is written on.
 * @param excludedVolume `v` in nm³, the **pair** excluded volume of two such segments.
 * @param segmentVolume `w` in nm³, the physical volume one such segment occupies.
 */
@Serializable
data class EdwardsCorrelation(
    val segmentLength: Double,
    val excludedVolume: Double,
    val segmentVolume: Double,
    val temperature: Double = ROOM_TEMPERATURE
) {

    init {
        require(segmentLength > 0.0) { "segmentLength must be positive, was: $segmentLength" }
        require(excludedVolume > 0.0) { "excludedVolume must be positive, was: $excludedVolume" }
        require(segmentVolume > 0.0) { "segmentVolume must be positive, was: $segmentVolume" }
        require(temperature > 0.0) { "temperature must be positive, was: $temperature" }
    }

    /** `c = φ/w` in nm⁻³ — the number density of statistical segments. */
    fun segmentDensity(volumeFraction: Double): Double {
        requirePositive(volumeFraction)
        return volumeFraction / segmentVolume
    }

    /** `ξ = b/√(12 v c)` in nm — the Edwards concentration screening length. */
    fun screeningLength(volumeFraction: Double): Double {
        requirePositive(volumeFraction)
        return segmentLength / sqrt(12.0 * excludedVolume * segmentDensity(volumeFraction))
    }

    /** `Δf = −k_BT/(12π ξ³)` in `pN/nm²` — the one-loop correction to the free-energy density. */
    fun oneLoopFreeEnergyDensity(volumeFraction: Double): Double {
        val screening = screeningLength(volumeFraction)
        return -thermalEnergy(temperature) / (12.0 * PI * screening * screening * screening)
    }

    /** `ΔΠ = ½Δf` in `pN/nm²`, negative — fluctuations *reduce* the osmotic pressure. */
    fun oneLoopPressure(volumeFraction: Double): Double =
        0.5 * oneLoopFreeEnergyDensity(volumeFraction)

    /** `Π_MF = ½ k_BT v c²` in `pN/nm²` — the mean-field two-body pressure the ratio is taken against. */
    fun meanFieldPressure(volumeFraction: Double): Double {
        val density = segmentDensity(volumeFraction)
        return 0.5 * thermalEnergy(temperature) * excludedVolume * density * density
    }

    /**
     * `Gz = √(v c)/(c b³) = √(v/(c b⁶))` — the **bare** Ginzburg parameter of the polymer
     * literature, Wittmer et al. (arXiv:1107.4454) Eq. (48), which they attribute to Doi &
     * Edwards Eq. (5.46).
     *
     * It is **not** the same number as [ginzburgNumber]: this is the small parameter of the
     * perturbation theory, and [ginzburgNumber] is the resulting *ratio of the pressure
     * correction to the leading pressure*, which is `(2√3/π) = 1.1027` times larger. Both are
     * reported, because reporting one under the other's name is worth 21 %.
     */
    fun ginzburgParameter(volumeFraction: Double): Double {
        requirePositive(volumeFraction)
        val density = segmentDensity(volumeFraction)
        return sqrt(excludedVolume / density) / (segmentLength * segmentLength * segmentLength)
    }

    /**
     * `δ(1/g)/(1/g)_MF = −(3√3/2π) Gz` — the one-loop correction to the **inverse osmotic
     * compressibility**, Wittmer et al. Eq. (99), reproduced here from [oneLoopFreeEnergyDensity]
     * rather than transcribed.
     *
     * It is the one published number of this family that is checked against simulation over three
     * decades, so it is this project's gate-5 handle on the whole construction.
     */
    fun oneLoopCompressibilityCorrection(volumeFraction: Double): Double =
        -1.5 * sqrt(3.0) / PI * ginzburgParameter(volumeFraction)

    /**
     * `Gi = |ΔΠ|/Π_MF = √(φ** over φ)` — the loop parameter of the expansion whose saddle point *is*
     * the self-consistent field.
     *
     * Read exactly as `C-0005` reads its electrostatic counterpart: **above 1 the expansion has
     * broken down**, and one cannot say from within the theory in which direction to correct it or
     * by how much.
     */
    fun ginzburgNumber(volumeFraction: Double): Double {
        requirePositive(volumeFraction)
        return sqrt(ginzburgVolumeFraction / volumeFraction)
    }

    /**
     * `φ** = 12 v w/(π² b⁶)` — the volume fraction at which `Gi = 1`.
     *
     * This is the thermal-blob concentration with a *computed* prefactor rather than a `~`, and it
     * is the boundary between the fluctuation-dominated regime below and the mean-field-controlled
     * one above.
     */
    val ginzburgVolumeFraction: Double
        get() = 12.0 * excludedVolume * segmentVolume / (PI * PI * segmentLength.pow(6.0))

    /** How many statistical segments of one chain lie inside one correlation volume, `(ξ/b)²`. */
    fun segmentsPerCorrelationBlob(volumeFraction: Double): Double {
        val ratio = screeningLength(volumeFraction) / segmentLength
        return ratio * ratio
    }

}

/**
 * The intrachain excluded-volume swelling a mean field cannot contain.
 *
 * A chain in a self-consistent field is Gaussian *in that field*: the correlation hole — a chain's
 * self-avoidance with itself — is exactly one of the things a mean field averages away. To first
 * order in the Fixman parameter,
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`z = (3/2πb²)^(3/2) v √n`, &nbsp;&nbsp; `α² = 1 + (4/3) z`
 *
 * and inside a layer the accumulation is cut off at the Edwards screening length, so the *screened*
 * value uses `n_ξ = (ξ/b)²` in place of `n` whenever that is smaller.
 *
 * This is the channel that acts on the term `C-0011` says is the **entire** disjoining pressure,
 * because it enters the solved layer as an effective segment length `b_eff = α b` and the Edwards
 * diffusion coefficient is `b²/6n_K`.
 *
 * @param kuhnLength `b` in nm.
 * @param kuhnExcludedVolume `v_K` in nm³ — the **pair** excluded volume of two Kuhn segments.
 */
@Serializable
data class ChainSwelling(
    val kuhnLength: Double,
    val kuhnExcludedVolume: Double
) {

    init {
        require(kuhnLength > 0.0) { "kuhnLength must be positive, was: $kuhnLength" }
        require(kuhnExcludedVolume > 0.0) {
            "kuhnExcludedVolume must be positive, was: $kuhnExcludedVolume"
        }
    }

    /**
     * `z = (3/2πb²)^(3/2) v √n`, dimensionless — Yamakawa, *Modern Theory of Polymer Solutions*
     * (1971) Eq. (13.32), with `b²` the mean-square displacement per counted segment and `v` that
     * same segment's **pair** excluded volume (his `β`, Eq. (13.3), which is `2B₂` and not `B₂`).
     *
     * The prefactor `(3/2π)^(3/2) = 0.32992` is the difference between this and the scaling
     * convention that writes `z ≈ (v/b³)√n`, and it is worth `9.185` in any blob count derived
     * from it.
     */
    fun fixmanParameter(kuhnSegments: Double): Double {
        require(kuhnSegments > 0.0) { "kuhnSegments must be positive, was: $kuhnSegments" }
        return FIXMAN_PREFACTOR * kuhnExcludedVolume * sqrt(kuhnSegments) /
                (kuhnLength * kuhnLength * kuhnLength)
    }

    /** `α = √(1 + 4z/3)` — the first-order expansion factor of a free chain. */
    fun expansionFactor(kuhnSegments: Double): Double =
        sqrt(1.0 + 4.0 * fixmanParameter(kuhnSegments) / 3.0)

    /**
     * The thermal blob in Kuhn segments **in Yamakawa's exact normalisation**, `z(g_T) = 1`, i.e.
     * `g_T = [b³/(0.32992 v)]²`.
     *
     * Carried beside the *scaling* normalisation `(b³/v)²` because the two differ by
     * `1/0.32992² = 9.185` and **the thermal blob's prefactor is a published convention bracket of
     * order thirty in `ξ_T`** (Schroeder, J. Rheol. 62, 371 (2018): `ξ_T ≡ c b⁴/v` with `c ≈ 0.1`
     * and `c ≈ 1` both in print for the same material). A blob count is therefore a convention;
     * [expansionFactor] is not, and it is what any conclusion about swelling has to be built on.
     */
    val thermalBlobKuhnSegments: Double
        get() {
            val ratio = kuhnLength.pow(3.0) /
                    (FIXMAN_PREFACTOR * kuhnExcludedVolume)
            return ratio * ratio
        }

    /**
     * [expansionFactor] with the accumulation cut off at [screeningLength]: swelling inside a
     * concentrated layer stops at the correlation blob, so `n → min(n, (ξ/b)²)`.
     */
    fun screenedExpansionFactor(kuhnSegments: Double, screeningLength: Double): Double {
        require(screeningLength > 0.0) {
            "screeningLength must be positive, was: $screeningLength"
        }
        val blob = (screeningLength / kuhnLength).pow(2.0)
        return expansionFactor(minOf(kuhnSegments, blob))
    }

}

/**
 * `v_K = n_K² v₀ B` in nm³ — the **pair** excluded volume of two Kuhn segments.
 *
 * The `n_K²` is the whole content of `CH-0020`: the interaction is `(v/2)∫c²` and the segment
 * density coarse-grains as `c_K = c_m/n_K`, so the excluded volume coarse-grains as the *square*.
 * `PegWater.thermalBlobKuhnSegments` uses `n_K` and is `n_K² = 9.67` too large as a result.
 */
fun PegWater.kuhnExcludedVolume(secondVirialCoefficient: Double): Double {
    require(secondVirialCoefficient > 0.0) {
        "secondVirialCoefficient must be positive, was: $secondVirialCoefficient"
    }
    val perKuhn = monomersPerKuhnSegment * monomersPerKuhnSegment
    return perKuhn * secondVirialCoefficient * monomerVolume
}

/**
 * `g_T = (b³/v_K)²` in Kuhn segments, with [kuhnExcludedVolume] — the corrected companion to
 * `PegWater.thermalBlobKuhnSegments`.
 *
 * Kept **beside** the incumbent rather than replacing it, per `SESSION-PROMPT.md`: a contradiction
 * raises a challenge, not an overwrite. The two differ by exactly `n_K²`, which is asserted as a
 * test.
 */
fun PegWater.thermalBlobKuhnSegmentsCorrected(secondVirialCoefficient: Double): Double {
    val ratio = kuhnLength.pow(3.0) / kuhnExcludedVolume(secondVirialCoefficient)
    return ratio * ratio
}

/** The correlation written on **Kuhn segments** — the reading every formula containing `b` needs. */
fun PegWater.edwardsCorrelation(
    secondVirialCoefficient: Double,
    temperature: Double = ROOM_TEMPERATURE
): EdwardsCorrelation = EdwardsCorrelation(
    segmentLength = kuhnLength,
    excludedVolume = kuhnExcludedVolume(secondVirialCoefficient),
    segmentVolume = kuhnSegmentVolume,
    temperature = temperature
)

/**
 * The same correlation written on **monomers**, with `b_m = b/√n_K` and `v_m = B v₀`.
 *
 * It exists only to be compared against [edwardsCorrelation]: a physical expansion parameter cannot
 * depend on how the chain is chopped into segments, and that identity is the gate that catches a
 * coarse-graining error.
 */
fun PegWater.monomerEdwardsCorrelation(
    secondVirialCoefficient: Double,
    temperature: Double = ROOM_TEMPERATURE
): EdwardsCorrelation {
    require(secondVirialCoefficient > 0.0) {
        "secondVirialCoefficient must be positive, was: $secondVirialCoefficient"
    }
    return EdwardsCorrelation(
        segmentLength = kuhnLength / sqrt(monomersPerKuhnSegment),
        excludedVolume = secondVirialCoefficient * monomerVolume,
        segmentVolume = monomerVolume,
        temperature = temperature
    )
}

/**
 * The same power law with its coefficient multiplied by [factor] — the handle the non-perturbative
 * bound is swept on.
 *
 * `C-0003` proves `k ∝ K^(1/(m+1))` and `N ∝ K^(−1/(m+1))` *exactly* for its two ansatz profiles.
 * Whether that transfers to a solved profile, whose disjoining pressure is conformational rather
 * than interactional, is a question this scaling makes measurable rather than assumable.
 */
fun PowerLawInteraction.scaled(factor: Double): PowerLawInteraction {
    require(factor > 0.0) { "factor must be positive, was: $factor" }
    return copy(name = "$name×$factor", coefficient = coefficient * factor)
}

/** `(3/2π)^(3/2) = 0.329918…` — Yamakawa Eq. (13.32)'s prefactor, quoted rather than absorbed. */
val FIXMAN_PREFACTOR: Double = (3.0 / (2.0 * PI)).pow(1.5)

private fun requirePositive(volumeFraction: Double) {
    require(volumeFraction > 0.0) {
        "volumeFraction must be positive, was: $volumeFraction"
    }
}
