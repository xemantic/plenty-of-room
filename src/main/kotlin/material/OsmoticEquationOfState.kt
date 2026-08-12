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
import kotlin.math.pow

/**
 * Where a polymer solution sits relative to the dilute→semidilute crossover.
 *
 * The boundaries are **measured**, not conventional: Cohen et al. (2009) show the crossover
 * of the fitted equation of state spans a 25-fold range of concentration centred on `φ#`,
 * so the two asymptotic laws own only what lies outside it.
 * This is the distinction §2 of the problem definition asks to have decided for our layer.
 */
enum class SolutionRegime {

    /** `φ ≲ 0.2 φ#` — an ideal gas of whole chains. No blobs, no brush, `Π ∝ φ`. */
    VAN_T_HOFF,

    /**
     * `0.2 φ# < φ < 5 φ#` — neither law holds.
     *
     * Quoting either asymptote here is the error this enum exists to make visible.
     * The honest quantity in this range is the *local* exponent, which is between 1 and 9/4.
     */
    CROSSOVER,

    /** `φ ≳ 5 φ#` — the des Cloizeaux semidilute regime, `Π ∝ φ^(9/4)`, chain length forgotten. */
    DES_CLOIZEAUX
}

/**
 * The measured osmotic equation of state of a neutral flexible polymer in a good solvent,
 * as a one-parameter non-virial interpolation between the van't Hoff and des Cloizeaux limbs:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`Π(φ) = (k_BT/v₀) · [ φ/N + α φ^(9/4) ]`
 *
 * with `φ` the **physical** polymer volume fraction, `v₀` the monomer volume, `N` the number
 * of monomers per chain, and `α` the fitted *crossover index*.
 *
 * ## Why this and not a virial expansion
 *
 * The two limbs are the exact asymptotes: the first is van't Hoff for an ideal solution of
 * whole chains, the second is des Cloizeaux's semidilute result, which is chain-length
 * independent — a property this class reproduces exactly rather than approximately.
 * The sum is deliberately **not** a virial series; a second-virial term would be a third
 * parameter fitted to the same data and would not improve it. Cohen, Podgornik, Hansen and
 * Parsegian (J. Phys. Chem. B 113:3709, 2009) fit this single `α` to Rand's osmometry on
 * **twelve** PEG molecular weights spanning 0–50 wt %, obtaining `r² = 0.9926`, and show the
 * result coincides with the renormalisation-group equation of state of Ohta and Oono.
 *
 * For the Gen-1 layer the point of carrying the whole crossover rather than an exponent is
 * that the layer turns out to sit **inside** it, where neither asymptote is available and
 * [localExponent] is the only defensible answer.
 *
 * ## What this is not
 *
 * It is a **bulk solution** equation of state. A grafted layer has no chain translational
 * entropy, so the van't Hoff limb is not the brush's restoring pressure; what the limb does
 * is locate the *density* at which the solution's semidilute structure — blobs, and with them
 * the entire Alexander-de Gennes picture — is actually established. That is how
 * Hansen et al. (Biophys. J. 84:350, 2003) use it, and it is how it is used here.
 *
 * @param crossoverIndex the fitted `α`. `0.49 ± 0.01` for PEG in water, `0.162 ± 0.002`
 *          for poly(α-methylstyrene) in toluene — it is strongly material-specific,
 *          which is exactly why it may not be inherited from a textbook.
 * @param monomerVolume `v₀` in nm³, the volume the volume fraction is measured against.
 *          It must be the same `v₀` the fit's volume fractions were computed with.
 * @param monomersPerChain `N`, continuous because chain length is a design variable.
 * @param temperature in K.
 */
@Serializable
data class ScalingEquationOfState(
    val crossoverIndex: Double,
    val monomerVolume: Double,
    val monomersPerChain: Double,
    val temperature: Double = ROOM_TEMPERATURE
) {

    init {
        require(crossoverIndex > 0.0) { "crossoverIndex must be positive, was: $crossoverIndex" }
        require(monomerVolume > 0.0) { "monomerVolume must be positive, was: $monomerVolume" }
        require(monomersPerChain >= 1.0) {
            "monomersPerChain must be at least 1, was: $monomersPerChain"
        }
        require(temperature > 0.0) { "temperature must be positive, was: $temperature" }
    }

    /**
     * The pressure scale `k_BT/v₀` in `pN/nm²`, one thermal energy per monomer volume.
     *
     * For PEG in water at 300 K this is 68.6 pN/nm², i.e. 68.6 MPa — the number every
     * osmotic pressure in this project is a small fraction of.
     */
    val pressureScale: Double get() = thermalEnergy(temperature) / monomerVolume

    /**
     * The crossover volume fraction `φ# = (α N)^(−4/5)`, where the two limbs are equal.
     *
     * Note this is **not** the overlap concentration `φ* = N^(−4/5)`: it carries the fitted
     * prefactor, and for PEG the two differ by `α^(−4/5) ≈ 1.77`. Hansen et al. (2003) make
     * the point sharply — the chain-overlap condition "does not provide a sufficient criterion"
     * for semidilute behaviour, and a layer can be well past overlap while its solution
     * thermodynamics is still that of separate chains.
     */
    val crossoverVolumeFraction: Double
        get() = (crossoverIndex * monomersPerChain).pow(-4.0 / 5.0)

    /** The van't Hoff limb alone, `(k_BT/v₀) φ/N`, in `pN/nm²`. */
    fun vanTHoffPressure(volumeFraction: Double): Double {
        requirePhysical(volumeFraction)
        return pressureScale * volumeFraction / monomersPerChain
    }

    /** The des Cloizeaux limb alone, `α (k_BT/v₀) φ^(9/4)`, in `pN/nm²`. */
    fun desCloizeauxPressure(volumeFraction: Double): Double {
        requirePhysical(volumeFraction)
        return pressureScale * crossoverIndex * volumeFraction.pow(DES_CLOIZEAUX_EXPONENT)
    }

    /** The osmotic pressure in `pN/nm²` (= MPa) at [volumeFraction]. */
    fun pressure(volumeFraction: Double): Double =
        vanTHoffPressure(volumeFraction) + desCloizeauxPressure(volumeFraction)

    /**
     * Returns `d lnΠ / d lnφ` at [volumeFraction] — the exponent the layer *actually* has.
     *
     * In closed form, with `x = α N φ^(5/4)` the ratio of the two limbs:
     *
     * &nbsp;&nbsp;&nbsp;&nbsp;`m(φ) = 1 + (5/4) · x/(1 + x)`
     *
     * which runs monotonically from 1 to 9/4 and equals `13/8 = 1.625` exactly at `φ#`.
     * This replaces the `{9/4, 2, 3}` three-way spread that `C-0001` had to carry:
     * the exponent is not a modelling choice, it is a function of where the layer sits.
     */
    fun localExponent(volumeFraction: Double): Double {
        requirePhysical(volumeFraction)
        val limbRatio = crossoverIndex * monomersPerChain *
                volumeFraction.pow(DES_CLOIZEAUX_EXPONENT - 1.0)
        return 1.0 + (DES_CLOIZEAUX_EXPONENT - 1.0) * limbRatio / (1.0 + limbRatio)
    }

    /** Which of the three domains [volumeFraction] falls in, by the measured crossover width. */
    fun regime(volumeFraction: Double): SolutionRegime {
        requirePhysical(volumeFraction)
        val reduced = volumeFraction / crossoverVolumeFraction
        return when {
            reduced <= VAN_T_HOFF_DOMAIN -> SolutionRegime.VAN_T_HOFF
            reduced >= DES_CLOIZEAUX_DOMAIN -> SolutionRegime.DES_CLOIZEAUX
            else -> SolutionRegime.CROSSOVER
        }
    }

    private fun requirePhysical(volumeFraction: Double) {
        require(volumeFraction > 0.0 && volumeFraction <= 1.0) {
            "volumeFraction must be within (0.0, 1.0], was: $volumeFraction"
        }
    }

    companion object {

        /** The des Cloizeaux exponent `9/4`, from the correlation length `ξ ∝ φ^(−3/4)`. */
        const val DES_CLOIZEAUX_EXPONENT: Double = 9.0 / 4.0

        /**
         * `φ/φ# ≤ 0.2` — the upper edge of the van't Hoff domain.
         *
         * Measured, not chosen: Cohen et al. (2009) obtain the crossover width by an
         * extrapolated-tangent construction and report it as `8/(5 ln 10)` decades either
         * side of `φ#`, i.e. a factor of 5 down and 5 up.
         */
        const val VAN_T_HOFF_DOMAIN: Double = 0.2

        /** `φ/φ# ≥ 5` — the lower edge of the des Cloizeaux domain. See [VAN_T_HOFF_DOMAIN]. */
        const val DES_CLOIZEAUX_DOMAIN: Double = 5.0
    }

}
