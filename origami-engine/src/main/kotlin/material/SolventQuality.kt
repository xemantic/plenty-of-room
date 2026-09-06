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

package com.xemantic.nano.plentyofroom.material

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow

/**
 * Solvent quality for PEG in water — task `P-6`.
 *
 * ## The problem this file exists to solve
 *
 * `C-0002` closed the PEG parameter sheet but left `χ(T, salt)` open, and `C-0001` carried a
 * `χ ≈ 0.45` it could not source. §2 of the problem definition asks for something sharper still:
 * PEG/water is said to have *"an unusually mobile `χ`"*, and the Gen-1 buffer is 2–10 mM MgCl₂,
 * so the question is not "what is `χ`" but **"how far does the layer's mechanics move when the
 * buffer moves"**.
 *
 * ## Three currencies, and the conventions that make them convertible
 *
 * Solvent quality is quoted as `χ`, as a second virial coefficient `A₂`/`B₂`, and as an excluded
 * volume `v`. They are convertible, but only once two conventions are fixed, and **both of them
 * are places where a factor of two hides**:
 *
 * 1. **`χ` lives on a lattice, and the lattice site is not always the monomer.** The measured
 *    PEG/water `χ` of Pedersen & Sommer is defined on a **water-molecule** site — their
 *    `2A₂M = (V_PEG/V_H₂O)(1 − 2χ)`. The monomer excluded volume is then
 *    `v = v₀ · (v₀/v_site) · (1 − 2χ)`, which for PEG is **2.01×** the familiar `v₀(1 − 2χ)`.
 *    See [monomerExcludedVolume], which takes the site volume as an argument precisely so it
 *    cannot be forgotten.
 * 2. **`B₂` is per chain, `v` is per monomer pair**, and they differ by `N²/2`.
 *
 * This is the same class of trap as the three meanings of `a` in [PegWater], and it is handled
 * the same way: no function here takes a bare "monomer size" or a bare "χ" without also taking
 * the convention it belongs to.
 *
 * ## What is measured, and where
 *
 * Everything with a `Serializable` data class here is a *parameterisation of a measurement*,
 * carrying its own source string, not a model we invented.
 */

/** `1 mol/L` expressed in `nm⁻³` — `N_A / 10²⁴`. The only unit conversion in this task. */
const val PER_CUBIC_NANOMETRE_PER_MOLAR: Double = AVOGADRO_CONSTANT / 1e24

/** Mass density of water at 300 K in `g/cm³` — CITED, and only a 0.3% correction anyway. */
const val WATER_MASS_DENSITY_AT_300K: Double = 0.99656

/**
 * Returns the number density in `nm⁻³` of the mobile ions released by a salt of [molarity]
 * `mol/L` that dissociates into [ionsPerFormulaUnit] ions.
 *
 * `MgCl₂` gives three. The Gen-1 buffer's 10 mM is therefore 30 mM of ions, and that is the
 * number [excludedSaltFreeEnergyDensity] is fed.
 *
 * @throws IllegalArgumentException if [molarity] is negative or [ionsPerFormulaUnit] is not positive.
 */
fun ionNumberDensity(
    molarity: Double,
    ionsPerFormulaUnit: Int
): Double {
    require(molarity >= 0.0) { "molarity must not be negative, was: $molarity" }
    require(ionsPerFormulaUnit > 0) {
        "ionsPerFormulaUnit must be positive, was: $ionsPerFormulaUnit"
    }
    return molarity * ionsPerFormulaUnit * PER_CUBIC_NANOMETRE_PER_MOLAR
}

/**
 * Returns the volume in nm³ of one water molecule at [massDensity] `g/cm³` — **DERIVED**,
 * `0.0300 nm³` at 300 K.
 *
 * It is derived rather than cited because it is the Flory-Huggins **lattice site** of the
 * measured PEG/water `χ`, and a lattice site that is wrong by a factor is a `χ` that is wrong
 * by the same factor.
 */
fun waterMoleculeVolume(
    massDensity: Double = WATER_MASS_DENSITY_AT_300K
): Double {
    require(massDensity > 0.0) { "massDensity must be positive, was: $massDensity" }
    return monomerVolume(molarMass(hydrogen = 2, oxygen = 1), 1.0 / massDensity)
}

/**
 * The measured temperature dependence of the Flory-Huggins parameter, `χ = a + b/T`.
 *
 * The functional form is Venohr et al.'s; the parameters are Pedersen & Sommer's fit of it to
 * small-angle X-ray scattering from **PEG 4600 in D₂O**, 10–100 °C, 1–20 wt %
 * (*Progr. Colloid Polym. Sci.* **130**:70, 2005): `a = 1.156 ± 0.002`, `b = −235.3 ± 0.9 K`.
 *
 * Two things make this the right parameterisation to carry rather than a single number:
 *
 * - it is **measured across the whole 300–375 K span** this task has to reason over, which a
 *   single `χ` at one temperature is not;
 * - its own theta temperature is a *consequence* rather than an extra fitted parameter —
 *   [thetaTemperature] solves `χ = ½` and lands on 358.7 K against the 358.85 ± 1.1 K that the
 *   same paper obtains from an independent quadratic fit. That agreement is a verification gate,
 *   not a coincidence.
 *
 * **Convention:** this `χ` is defined on a **water-molecule** lattice site. Feed it to
 * [monomerExcludedVolume] with [waterMoleculeVolume], never with the monomer volume.
 *
 * **Medium:** D₂O, not H₂O. D₂O is the poorer solvent, so this is a lower bound on the true
 * solvent quality of PEG in H₂O, by a few kelvin of theta temperature. Stated, not hidden.
 */
@Serializable
data class ReciprocalTemperatureChi(
    val interceptA: Double = 1.156,
    val slopeB: Double = -235.3,
    val interceptUncertainty: Double = 0.002,
    val slopeUncertainty: Double = 0.9,
    val source: String = "Pedersen & Sommer, Progr. Colloid Polym. Sci. 130:70 (2005), " +
            "SAXS on PEG 4600 in D2O, 10-100 C; functional form from Venohr et al. (1998)"
) {

    init {
        require(slopeB < 0.0) {
            "slopeB must be negative for an LCST system, was: $slopeB"
        }
        require(interceptA > THETA_CHI) {
            "interceptA must exceed 1/2 for chi to cross the theta value, was: $interceptA"
        }
    }

    /** `χ(T)`. 0.372 at 300 K. */
    fun chi(temperature: Double = ROOM_TEMPERATURE): Double {
        require(temperature > 0.0) { "temperature must be positive, was: $temperature" }
        return interceptA + slopeB / temperature
    }

    /** `dχ/dT = −b/T²` in `K⁻¹`. `+2.61e-3` at 300 K, `+1.69e-3` at the cloud point. */
    fun chiTemperatureDerivative(temperature: Double = ROOM_TEMPERATURE): Double {
        require(temperature > 0.0) { "temperature must be positive, was: $temperature" }
        return -slopeB / (temperature * temperature)
    }

    /** The temperature at which `χ = ½`, in K — **DERIVED** from [interceptA] and [slopeB]. */
    val thetaTemperature: Double get() = slopeB / (THETA_CHI - interceptA)
}

/**
 * The same measurement in its second, independent parameterisation:
 * `χ = ½ + χ₁(T − θ) + χ₂(T − θ)²`.
 *
 * Pedersen & Sommer fit this form as well, obtaining `χ₁ = 0.00142 ± 0.00009`,
 * `χ₂ = −1.1e-5 ± 2e-6` and `θ = 85.7 ± 1.1 °C`. It is carried alongside
 * [ReciprocalTemperatureChi] for exactly one reason: **the two disagree, and by how much is the
 * honest uncertainty on `χ`.** They agree to 1.8% in `χ` at 300 K but only to 5.5% in
 * `1 − 2χ`, and to 53% in `dχ/dT` at the cloud point — which is where this task reads it.
 */
@Serializable
data class ThetaExpansionChi(
    val firstOrder: Double = 0.00142,
    val secondOrder: Double = -1.1e-5,
    val thetaTemperature: Double = 358.85,
    val source: String = "Pedersen & Sommer, Progr. Colloid Polym. Sci. 130:70 (2005), Eq. (8)"
) {

    init {
        require(thetaTemperature > 0.0) {
            "thetaTemperature must be positive, was: $thetaTemperature"
        }
    }

    /** `χ(T)`. */
    fun chi(temperature: Double = ROOM_TEMPERATURE): Double {
        val d = temperature - thetaTemperature
        return THETA_CHI + firstOrder * d + secondOrder * d * d
    }

    /** `dχ/dT` in `K⁻¹`. */
    fun chiTemperatureDerivative(temperature: Double = ROOM_TEMPERATURE): Double =
        firstOrder + 2.0 * secondOrder * (temperature - thetaTemperature)
}

/** The Flory-Huggins value of `χ` at theta conditions. */
const val THETA_CHI: Double = 0.5

/**
 * Returns the **monomer-pair excluded volume** in nm³ implied by a Flory-Huggins [chi]
 * that was defined on a lattice of [latticeSiteVolume] nm³:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`v = v₀ · (v₀ / v_site) · (1 − 2χ)`
 *
 * The middle factor is the one that gets dropped. It is 1 only when the lattice site *is* the
 * monomer; for the measured PEG/water `χ`, whose site is a water molecule, it is **2.01**,
 * and dropping it understates the excluded volume by that factor.
 *
 * Negative below theta conditions, by construction — a poor solvent has negative excluded volume,
 * and callers that need that to be an error should check it themselves.
 *
 * @throws IllegalArgumentException if either volume is not positive.
 */
fun monomerExcludedVolume(
    chi: Double,
    monomerVolume: Double,
    latticeSiteVolume: Double
): Double {
    require(monomerVolume > 0.0) { "monomerVolume must be positive, was: $monomerVolume" }
    require(latticeSiteVolume > 0.0) {
        "latticeSiteVolume must be positive, was: $latticeSiteVolume"
    }
    return monomerVolume * (monomerVolume / latticeSiteVolume) * (1.0 - 2.0 * chi)
}

/** The inverse of [monomerExcludedVolume] — the `χ` a given excluded volume corresponds to. */
fun chiFromMonomerExcludedVolume(
    excludedVolume: Double,
    monomerVolume: Double,
    latticeSiteVolume: Double
): Double {
    require(monomerVolume > 0.0) { "monomerVolume must be positive, was: $monomerVolume" }
    require(latticeSiteVolume > 0.0) {
        "latticeSiteVolume must be positive, was: $latticeSiteVolume"
    }
    return 0.5 * (1.0 - excludedVolume * latticeSiteVolume / (monomerVolume * monomerVolume))
}

/**
 * The measured chain-chain second virial coefficient of PEG in water, `B₂ = B_fit (θ − T)`,
 * in nm³ — linear in the distance from the theta temperature.
 *
 * `B_fit = 2.00 nm³/K` and `θ = 373.2 K` for `M = 4600 g/mol`, as quoted by
 * Chudoba, Heyda & Dzubiella (*J. Chem. Theory Comput.* **13**:6317, 2017, Eq. 9) from
 * Pedersen & Sommer's scattering data.
 *
 * This is carried because it **is the measurement that licenses the linear-in-`τ` treatment**
 * of solvent quality that this whole task rests on. Not an assumption — a fitted form.
 *
 * Note that its theta temperature (373.2 K, from the virial analysis) is **14 K above** the one
 * [ReciprocalTemperatureChi] implies (358.7 K, from the Flory-Huggins analysis of the *same*
 * scattering data). Both are in the paper; the difference is the finite-concentration correction.
 * "The theta temperature of PEG in water" is therefore itself a 14 K band, which is worth
 * remembering before attributing a one-kelvin shift to a salt.
 */
@Serializable
data class ChainSecondVirialCoefficient(
    val slope: Double = 2.00,
    val thetaTemperature: Double = 373.2,
    val chainMolarMass: Double = 4600.0,
    val source: String = "Chudoba, Heyda & Dzubiella, J. Chem. Theory Comput. 13:6317 (2017), " +
            "Eq. (9), from Pedersen & Sommer SAXS"
) {

    init {
        require(slope > 0.0) { "slope must be positive, was: $slope" }
        require(thetaTemperature > 0.0) {
            "thetaTemperature must be positive, was: $thetaTemperature"
        }
        require(chainMolarMass > 0.0) { "chainMolarMass must be positive, was: $chainMolarMass" }
    }

    /** `B₂(T)` in nm³. 146 nm³ at 300 K. */
    fun secondVirialCoefficient(temperature: Double = ROOM_TEMPERATURE): Double =
        slope * (thetaTemperature - temperature)

    /** How many monomers the fitted chain has, given a [monomerMolarMass] in `g/mol`. */
    fun monomersPerChain(monomerMolarMass: Double): Double {
        require(monomerMolarMass > 0.0) {
            "monomerMolarMass must be positive, was: $monomerMolarMass"
        }
        return chainMolarMass / monomerMolarMass
    }

    /**
     * Returns the monomer-pair excluded volume in nm³ implied by `B₂ = N² v / 2`.
     *
     * This is the *mean-field* relation between the two, valid while the chain is nearly
     * Gaussian. For PEG 4600 the excluded-volume parameter is `z ≈ 0.4`, so the chain is
     * mildly swollen and the relation **overestimates `B₂` for a given `v`**, i.e. it
     * **underestimates `v`** by perhaps a third. One-sided, and stated.
     */
    fun monomerExcludedVolume(monomerMolarMass: Double, temperature: Double = ROOM_TEMPERATURE): Double {
        val n = monomersPerChain(monomerMolarMass)
        return 2.0 * secondVirialCoefficient(temperature) / (n * n)
    }
}

/**
 * Returns the excluded volume in nm³ between two **Kuhn segments** of [monomersPerKuhnSegment]
 * monomers each, given the [monomerExcludedVolume].
 *
 * `v_K = n_K² v`. Trivial arithmetic that exists as a named function because the blob relations
 * below are in Kuhn units and everything else in this project is in monomer units, and mixing
 * the two is exactly the error `C-0002` was written to prevent.
 */
fun kuhnPairExcludedVolume(
    monomerExcludedVolume: Double,
    monomersPerKuhnSegment: Double
): Double {
    require(monomersPerKuhnSegment > 0.0) {
        "monomersPerKuhnSegment must be positive, was: $monomersPerKuhnSegment"
    }
    return monomerExcludedVolume * monomersPerKuhnSegment * monomersPerKuhnSegment
}

/**
 * Returns the des Cloizeaux crossover index `α` of the semidilute equation of state predicted
 * by the correlation-blob argument, up to an unknown O(1) [blobPrefactor]:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`α = C · v₀ · v_K^(3/4) · b^(3/2) · v_K,vol^(−9/4)`
 *
 * from `Π = C k_BT/ξ³` with `ξ = ξ_T (φ/φ_T)^(−3/4)`, `ξ_T = b⁴/v_K`, `φ_T = v_K v_K,vol/b⁶`.
 * Dimensionless, as it must be — `nm³ · nm^(9/4) · nm^(3/2) / nm^(27/4) = nm⁰`.
 *
 * **This function is not used to predict `α`; the measured `α = 0.49` is.** It is here for its
 * *logarithmic derivative*, `d ln α / d ln v = 3/4`, which is independent of `C` and is the
 * transfer function that carries a change in solvent quality into a change in the layer's
 * osmotic pressure. Evaluated at `C = 1` it returns 1.22 against the measured 0.49, i.e. the
 * real prefactor is 0.40 — order unity, which is all the argument claims and all it needs.
 */
fun desCloizeauxIndexFromExcludedVolume(
    kuhnPairExcludedVolume: Double,
    kuhnLength: Double,
    kuhnSegmentVolume: Double,
    monomerVolume: Double,
    blobPrefactor: Double = 1.0
): Double {
    require(kuhnPairExcludedVolume > 0.0) {
        "kuhnPairExcludedVolume must be positive, was: $kuhnPairExcludedVolume"
    }
    require(kuhnLength > 0.0) { "kuhnLength must be positive, was: $kuhnLength" }
    require(kuhnSegmentVolume > 0.0) {
        "kuhnSegmentVolume must be positive, was: $kuhnSegmentVolume"
    }
    require(monomerVolume > 0.0) { "monomerVolume must be positive, was: $monomerVolume" }
    require(blobPrefactor > 0.0) { "blobPrefactor must be positive, was: $blobPrefactor" }
    return blobPrefactor * monomerVolume *
            kuhnPairExcludedVolume.pow(DES_CLOIZEAUX_TRANSFER_EXPONENT) *
            kuhnLength.pow(1.5) / kuhnSegmentVolume.pow(2.25)
}

/** The inverse of [desCloizeauxIndexFromExcludedVolume]. */
fun kuhnPairExcludedVolumeFromDesCloizeauxIndex(
    crossoverIndex: Double,
    kuhnLength: Double,
    kuhnSegmentVolume: Double,
    monomerVolume: Double,
    blobPrefactor: Double = 1.0
): Double {
    require(crossoverIndex > 0.0) { "crossoverIndex must be positive, was: $crossoverIndex" }
    val scale = desCloizeauxIndexFromExcludedVolume(
        kuhnPairExcludedVolume = 1.0,
        kuhnLength = kuhnLength,
        kuhnSegmentVolume = kuhnSegmentVolume,
        monomerVolume = monomerVolume,
        blobPrefactor = blobPrefactor
    )
    return (crossoverIndex / scale).pow(1.0 / DES_CLOIZEAUX_TRANSFER_EXPONENT)
}

/**
 * `d ln Π / d ln v = 3/4` in the des Cloizeaux limb — the blob transfer exponent.
 *
 * `Π = k_BT/ξ³` and `ξ ∝ v^(−1/4)φ^(−3/4)`, so three quarters. It is the **smaller** of the two
 * admissible exponents, hence not the one to bound with.
 */
const val DES_CLOIZEAUX_TRANSFER_EXPONENT: Double = 0.75

/**
 * `d ln Π / d ln v = 1` in the mean-field limb, where `Π ⊃ (v/2v₀²)k_BT φ²`.
 *
 * The conservative choice: it is the larger of the two, and it is also the one that applies
 * *above* the thermal-blob volume fraction, which the Gen-1 layer approaches.
 */
const val MEAN_FIELD_TRANSFER_EXPONENT: Double = 1.0

/**
 * A linear cloud-point depression by salt, `T_cp(c) = T_cp(0) − k_s c` — the Setschenow-type
 * parameterisation every published PEG salt series is reported in.
 *
 * @param slope `k_s` in K per `mol/L`. **Positive means salting out** (the cloud point falls,
 *          the solvent gets worse). Negative is salting in, which for PEG with divalent cations
 *          is a live possibility rather than a formality — see `C-0007`.
 * @param saltFreeCloudPoint `T_cp(0)` in K.
 * @param fittedRangeLow the lowest salt molarity the fit was measured over.
 * @param fittedRangeHigh the highest.
 * @param source where `k_s` comes from.
 */
@Serializable
data class CloudPointDepression(
    val slope: Double,
    val saltFreeCloudPoint: Double = 375.0,
    val fittedRangeLow: Double,
    val fittedRangeHigh: Double,
    val source: String
) {

    init {
        require(saltFreeCloudPoint > 0.0) {
            "saltFreeCloudPoint must be positive, was: $saltFreeCloudPoint"
        }
        require(fittedRangeLow > 0.0) { "fittedRangeLow must be positive, was: $fittedRangeLow" }
        require(fittedRangeHigh >= fittedRangeLow) {
            "fittedRangeHigh must not be below fittedRangeLow, was: $fittedRangeHigh"
        }
    }

    /** The cloud point in K at [molarity] `mol/L`. */
    fun cloudPoint(molarity: Double): Double {
        require(molarity >= 0.0) { "molarity must not be negative, was: $molarity" }
        return saltFreeCloudPoint - slope * molarity
    }

    /** Whether [molarity] lies outside the range the slope was measured over. */
    fun isExtrapolatedAt(molarity: Double): Boolean =
        molarity < fittedRangeLow || molarity > fittedRangeHigh

    /**
     * How many decades of extrapolation [molarity] is from the fitted range — zero inside it.
     *
     * The Gen-1 buffer is 2–10 mM and the published PEG salt series sit at 0.1–1 M, so this
     * returns **1.0 to 1.7 decades**. That is the single largest uncertainty in this task and
     * it is reported as a number rather than as a caveat.
     */
    fun extrapolationDecades(molarity: Double): Double {
        require(molarity > 0.0) { "molarity must be positive, was: $molarity" }
        return when {
            molarity < fittedRangeLow -> log10(fittedRangeLow / molarity)
            molarity > fittedRangeHigh -> log10(molarity / fittedRangeHigh)
            else -> 0.0
        }
    }
}

/**
 * What a step in buffer salt concentration does to solvent quality and to the layer.
 *
 * Every field is a *difference between two buffers*, not an absolute value, because the
 * absolute value of `χ` for PEG in water is known far less well than its salt derivative —
 * which is itself the headline of `C-0007`.
 */
@Serializable
data class SolventQualityShift(
    val lowMolarity: Double,
    val highMolarity: Double,
    val cloudPointLow: Double,
    val cloudPointHigh: Double,
    val cloudPointShift: Double,
    val chiLow: Double,
    val chiHigh: Double,
    val chiShift: Double,
    val excludedVolumeLow: Double,
    val excludedVolumeHigh: Double,
    val excludedVolumeFractionalShift: Double,
    val crossoverIndexFractionalShift: Double,
    val equilibriumHeightFractionalShift: Double,
    val extrapolationDecadesLow: Double,
    val extrapolationDecadesHigh: Double
)

/**
 * Returns the [SolventQualityShift] between two buffer concentrations.
 *
 * ## The transfer function, stated so it can be attacked
 *
 * A salt that depresses the cloud point by `ΔT_cp` must be supplying, at the cloud point,
 * exactly the `Δχ` that the *temperature* would otherwise have had to supply:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;**`Δχ_salt = −(dχ/dT)|_{T_cp} · ΔT_cp`**
 *
 * because the phase boundary is a locus of constant total `χ`. The step from there to 300 K
 * carries **one assumption**: that `Δχ_salt` is itself temperature-independent, i.e. that the
 * salt contributes a constant to `χ` rather than a constant to `χ(T)`'s slope. That is the
 * standard Setschenow reading and it is what makes the number transferable; it is also the
 * first thing to attack if this result is ever contradicted.
 *
 * @param chiAtOperatingTemperature `χ` of the salt-free solvent at the temperature of interest.
 * @param chiTemperatureDerivativeAtCloudPoint `dχ/dT` evaluated **at the cloud point**, not at
 *          the operating temperature — they differ by 55% for PEG/water and using the wrong one
 *          is a silent 55% error.
 */
fun solventQualityShift(
    depression: CloudPointDepression,
    lowMolarity: Double,
    highMolarity: Double,
    chiAtOperatingTemperature: Double,
    chiTemperatureDerivativeAtCloudPoint: Double,
    monomerVolume: Double,
    latticeSiteVolume: Double
): SolventQualityShift {
    require(lowMolarity > 0.0) { "lowMolarity must be positive, was: $lowMolarity" }
    require(highMolarity >= lowMolarity) {
        "highMolarity must not be below lowMolarity, was: $highMolarity"
    }
    val cloudPointLow = depression.cloudPoint(lowMolarity)
    val cloudPointHigh = depression.cloudPoint(highMolarity)
    val cloudPointShift = cloudPointHigh - cloudPointLow
    val chiShift = -chiTemperatureDerivativeAtCloudPoint * cloudPointShift
    val chiLow = chiAtOperatingTemperature
    val chiHigh = chiAtOperatingTemperature + chiShift
    val excludedVolumeLow = monomerExcludedVolume(chiLow, monomerVolume, latticeSiteVolume)
    val excludedVolumeHigh = monomerExcludedVolume(chiHigh, monomerVolume, latticeSiteVolume)
    val fractional = excludedVolumeHigh / excludedVolumeLow - 1.0
    return SolventQualityShift(
        lowMolarity = lowMolarity,
        highMolarity = highMolarity,
        cloudPointLow = cloudPointLow,
        cloudPointHigh = cloudPointHigh,
        cloudPointShift = cloudPointShift,
        chiLow = chiLow,
        chiHigh = chiHigh,
        chiShift = chiShift,
        excludedVolumeLow = excludedVolumeLow,
        excludedVolumeHigh = excludedVolumeHigh,
        excludedVolumeFractionalShift = fractional,
        crossoverIndexFractionalShift =
            (1.0 + fractional).pow(DES_CLOIZEAUX_TRANSFER_EXPONENT) - 1.0,
        equilibriumHeightFractionalShift = (1.0 + fractional).pow(1.0 / 3.0) - 1.0,
        extrapolationDecadesLow = depression.extrapolationDecades(lowMolarity),
        extrapolationDecadesHigh = depression.extrapolationDecades(highMolarity)
    )
}

/**
 * `K = φ ∂Π/∂φ`, the osmotic modulus in `pN/nm²` — **the quantity the layer's stiffness is
 * proportional to**, since `k/A = K/h` for a grafted layer at fixed `N` and `σ`.
 *
 * In closed form for the adopted equation of state, `K = Π_vH + (9/4)Π_dC = Π · m_eff`.
 */
fun ScalingEquationOfState.osmoticModulus(volumeFraction: Double): Double =
    vanTHoffPressure(volumeFraction) +
            ScalingEquationOfState.DES_CLOIZEAUX_EXPONENT * desCloizeauxPressure(volumeFraction)

/** What fraction of [osmoticModulus] the solvent-quality-dependent des Cloizeaux limb supplies. */
fun ScalingEquationOfState.desCloizeauxModulusFraction(volumeFraction: Double): Double {
    val desCloizeaux =
        ScalingEquationOfState.DES_CLOIZEAUX_EXPONENT * desCloizeauxPressure(volumeFraction)
    return desCloizeaux / (vanTHoffPressure(volumeFraction) + desCloizeaux)
}

/**
 * Returns the fractional change in the layer's osmotic modulus produced by a
 * [fractionalExcludedVolumeShift] in solvent quality.
 *
 * Only the des Cloizeaux limb responds — the van't Hoff limb is chain translational entropy and
 * knows nothing about solvent quality — so the answer is always **smaller in magnitude** than
 * the shift that produced it, by [desCloizeauxModulusFraction].
 *
 * `k/A = K/h` at fixed geometry, so this is also the fractional change in the layer stiffness.
 */
fun ScalingEquationOfState.osmoticModulusResponse(
    volumeFraction: Double,
    fractionalExcludedVolumeShift: Double,
    transferExponent: Double = DES_CLOIZEAUX_TRANSFER_EXPONENT
): Double {
    require(fractionalExcludedVolumeShift > -1.0) {
        "fractionalExcludedVolumeShift must exceed -1, was: $fractionalExcludedVolumeShift"
    }
    val fraction = desCloizeauxModulusFraction(volumeFraction)
    return fraction * ((1.0 + fractionalExcludedVolumeShift).pow(transferExponent) - 1.0)
}

/**
 * The free-energy density in `pN/nm²` whose Legendre transform is the adopted equation of state:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`f(φ) = (k_BT/v₀)[ (φ ln φ)/N + (4/5) α φ^(9/4) ]`
 *
 * Checked by [osmoticPressureOfFreeEnergyDensity] rather than asserted.
 */
fun ScalingEquationOfState.freeEnergyDensity(volumeFraction: Double): Double {
    require(volumeFraction > 0.0 && volumeFraction <= 1.0) {
        "volumeFraction must be within (0.0, 1.0], was: $volumeFraction"
    }
    return pressureScale * (
            volumeFraction * ln(volumeFraction) / monomersPerChain +
                    (4.0 / 5.0) * crossoverIndex *
                    volumeFraction.pow(ScalingEquationOfState.DES_CLOIZEAUX_EXPONENT)
            )
}

/**
 * Returns `Π = φ f'(φ) − f(φ)` in `pN/nm²`, the osmotic pressure implied by a free-energy
 * density [freeEnergyDensity], by central difference.
 *
 * It exists so that a candidate contribution to the layer's free energy can be **tested** for
 * whether it exerts a pressure at all, rather than argued about. The mobile-ion term is the
 * case in point: it is large, and it exerts none.
 */
fun osmoticPressureOfFreeEnergyDensity(
    volumeFraction: Double,
    step: Double = 1e-6,
    freeEnergyDensity: (Double) -> Double
): Double {
    require(volumeFraction > 0.0) { "volumeFraction must be positive, was: $volumeFraction" }
    require(step > 0.0 && step < volumeFraction) {
        "step must be within (0.0, $volumeFraction), was: $step"
    }
    val derivative =
        (freeEnergyDensity(volumeFraction + step) - freeEnergyDensity(volumeFraction - step)) /
                (2.0 * step)
    return volumeFraction * derivative - freeEnergyDensity(volumeFraction)
}

/**
 * The free-energy density in `pN/nm²` contributed by ideal mobile salt of [ionNumberDensity]
 * `nm⁻³` that is excluded from the polymer's own volume: **`f_ion = k_BT n_s φ`**.
 *
 * Derivation, because the sign is not obvious: at fixed ion chemical potential the ions form an
 * ideal gas in the accessible fraction `1 − φ`, so their grand potential density is
 * `−k_BT n_s (1 − φ)`; measured against the reservoir's `−k_BT n_s`, the excess is `+k_BT n_s φ`.
 *
 * **It is exactly linear in `φ`, and a linear term carries no osmotic pressure.** For a *grafted*
 * layer the same statement is that `∫f_ion dV = k_BT n_s × (conserved polymer volume)`,
 * independent of the layer's height, hence no force on the tile. The 0.075 pN/nm² of ion
 * pressure at 10 mM MgCl₂ — three and a half times the layer's own — cancels rather than being
 * neglected. Everything the buffer does to the layer's mechanics is therefore beyond ideality,
 * which is to say it is a `χ`, which is what `P-6` went looking for.
 */
fun excludedSaltFreeEnergyDensity(
    ionNumberDensity: Double,
    volumeFraction: Double,
    temperature: Double = ROOM_TEMPERATURE
): Double {
    require(ionNumberDensity >= 0.0) {
        "ionNumberDensity must not be negative, was: $ionNumberDensity"
    }
    require(volumeFraction >= 0.0) { "volumeFraction must not be negative, was: $volumeFraction" }
    return thermalEnergy(temperature) * ionNumberDensity * volumeFraction
}

/**
 * Returns the gap-averaged counterion concentration in `mol/L` that a surface of
 * [surfaceChargeDensity] `e/nm²` releases into a gap of [gapHeight] nm bounded by it —
 * `c = σ_eff / (h N_A)` in the units of this project, for one charged surface.
 *
 * `C-0005` finds the tile-electrode gap is counterion-dominated by 3.3:1 to 33:1, so **the salt
 * concentration the PEG layer actually sits in is not the buffer's**. And because the counterion
 * inventory per unit area is fixed by the tile's charge while the gap shrinks under actuation,
 * this concentration goes as `1/h` — i.e. **the layer's ionic environment is a function of the
 * actuator's own stroke.** That is the coupling `T-3` has to know about.
 *
 * @throws IllegalArgumentException if either argument is not positive.
 */
fun gapAveragedCounterionMolarity(
    surfaceChargeDensity: Double,
    gapHeight: Double,
    valency: Int = 2
): Double {
    require(surfaceChargeDensity > 0.0) {
        "surfaceChargeDensity must be positive, was: $surfaceChargeDensity"
    }
    require(gapHeight > 0.0) { "gapHeight must be positive, was: $gapHeight" }
    require(valency > 0) { "valency must be positive, was: $valency" }
    return surfaceChargeDensity / (valency * gapHeight * PER_CUBIC_NANOMETRE_PER_MOLAR)
}

/** Returns `|a − b| / |b|`, the relative disagreement between two independent determinations. */
fun relativeDisagreement(a: Double, b: Double): Double = abs(a - b) / abs(b)
