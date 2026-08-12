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
import com.xemantic.nano.plentyofroom.material.ScalingEquationOfState
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow

/**
 * A **local interaction** free energy of a polymer solution, as a function of the physical
 * volume fraction `φ` — the object task `T-1c` was raised to put underneath the layer response
 * in place of a fixed osmotic exponent.
 *
 * ## Why an interaction free energy and not the measured equation of state
 *
 * Integrating the adopted equation of state `Π(φ) = (k_BT/v₀)[φ/N + αφ^(9/4)]` through
 * `f(φ) = φ ∫ Π(φ')/φ'² dφ'` gives
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`f(φ) = (k_BT/v₀)[ (φ lnφ)/N + (4α/5) φ^(9/4) ]`
 *
 * whose first term is the **translational entropy of whole chains**. A grafted layer does not
 * have it: the chains cannot explore the volume, they are tethered. Removing it leaves an
 * interaction free energy whose osmotic pressure is the des Cloizeaux limb alone — exponent
 * exactly `9/4` at *every* density. So the crossover `C-0002` measures in the **bulk** pressure
 * is, for this equation of state, an artefact of a term the brush does not carry, and the
 * `m_eff = 1.66–1.92` that `CH-0001` carried into the brush pressure law does not belong there.
 * [ScalingEquationOfState.interactionFreeEnergyDensity] is that statement in code, and
 * `InteractionFreeEnergyTest` verifies it by differentiating back.
 *
 * ## What replaces the objection
 *
 * A narrower one, which is why this is an interface and not a constant. The fitted `αφ^(9/4)` is
 * a crossover *interpolation*: below `φ#` the measured pressure is dominated by the van't Hoff
 * limb, so the data constrain the interaction term only weakly there. Physically the interaction
 * free energy must cross over from an unscreened two-body form `~(B/2)φ²` to the screened
 * `φ^(9/4)` one, and **where** it crosses is set by `B`, which this project does not have from
 * an independent measurement. The two limbs are therefore carried as a bracket
 * ([twoBodyInteraction], [desCloizeauxInteraction]) with [additiveInteraction] as the
 * interpolation that has both asymptotes — the same construction the measured equation of state
 * itself uses, and the same one that overshoots each limb by 2× where they cross.
 *
 * ## Sign and unit conventions
 *
 * `f` is a free energy **per unit volume of solution**, in `pN/nm²`; `Π = φ f' − f` is in the same
 * units, which are `MPa` exactly. The exchange chemical potential `μ = v₀ df/dφ` is an energy
 * **per monomer**, in `pN·nm`, because that is what the strong-stretching potential is compared
 * against. `φ = 0` is a legal argument and every quantity vanishes there: the outer edge of an
 * uncompressed brush profile *is* `φ = 0`.
 *
 * [freeEnergyDensity], [osmoticPressure] and [osmoticPressureSlope] reject `φ > 1` as unphysical.
 * [exchangeChemicalPotential], its slope and its inverse do **not**, because the root finders that
 * locate a profile have to be able to bracket from the unphysical side.
 */
sealed interface InteractionFreeEnergy {

    /** Stable identifier, emitted with every machine-readable result. */
    val name: String

    /** `v₀` in nm³ — the monomer volume the volume fraction is measured against. */
    val monomerVolume: Double

    /** The temperature in K at which this free energy is evaluated — stated, never implied. */
    val temperature: Double

    /** `f(φ)` in `pN/nm²`, the interaction free energy per unit volume of solution. */
    fun freeEnergyDensity(volumeFraction: Double): Double

    /** `Π(φ) = φ f'(φ) − f(φ)` in `pN/nm²` (= MPa). */
    fun osmoticPressure(volumeFraction: Double): Double

    /** `dΠ/dφ` in `pN/nm²`. */
    fun osmoticPressureSlope(volumeFraction: Double): Double

    /** `μ(φ) = v₀ f'(φ)` in `pN·nm`, the exchange chemical potential per monomer. */
    fun exchangeChemicalPotential(volumeFraction: Double): Double

    /** `dμ/dφ` in `pN·nm`. */
    fun exchangeChemicalPotentialSlope(volumeFraction: Double): Double

    /**
     * The inverse of [exchangeChemicalPotential]: the `φ` at which the potential is
     * [chemicalPotential]. Zero for a non-positive argument, and may exceed 1, which the
     * caller has to recognise as unphysical.
     */
    fun volumeFractionAtChemicalPotential(chemicalPotential: Double): Double

}

/**
 * A single power-law limb `Π = K φ^m`, hence `f = K φ^m/(m − 1)` and `μ = v₀ K m φ^(m−1)/(m − 1)`.
 *
 * @param coefficient `K` in `pN/nm²`.
 * @param exponent `m`, strictly greater than 1 — at `m = 1` the free energy is purely
 *          translational, the Legendre transform this file rests on has no inverse, and a
 *          grafted layer has no such term anyway.
 */
@Serializable
data class PowerLawInteraction(
    override val name: String,
    val coefficient: Double,
    val exponent: Double,
    override val monomerVolume: Double,
    override val temperature: Double = ROOM_TEMPERATURE
) : InteractionFreeEnergy {

    init {
        require(coefficient > 0.0) { "coefficient must be positive, was: $coefficient" }
        require(exponent > 1.0) { "exponent must exceed 1, was: $exponent" }
        require(monomerVolume > 0.0) { "monomerVolume must be positive, was: $monomerVolume" }
        require(temperature > 0.0) { "temperature must be positive, was: $temperature" }
    }

    override fun freeEnergyDensity(volumeFraction: Double): Double {
        requirePhysical(volumeFraction)
        return coefficient * volumeFraction.pow(exponent) / (exponent - 1.0)
    }

    override fun osmoticPressure(volumeFraction: Double): Double {
        requirePhysical(volumeFraction)
        return coefficient * volumeFraction.pow(exponent)
    }

    override fun osmoticPressureSlope(volumeFraction: Double): Double {
        requirePhysical(volumeFraction)
        return exponent * coefficient * volumeFraction.pow(exponent - 1.0)
    }

    override fun exchangeChemicalPotential(volumeFraction: Double): Double {
        requireNonNegative(volumeFraction)
        return monomerVolume * coefficient * exponent *
                volumeFraction.pow(exponent - 1.0) / (exponent - 1.0)
    }

    override fun exchangeChemicalPotentialSlope(volumeFraction: Double): Double {
        requireNonNegative(volumeFraction)
        return monomerVolume * coefficient * exponent * volumeFraction.pow(exponent - 2.0)
    }

    override fun volumeFractionAtChemicalPotential(chemicalPotential: Double): Double =
        if (chemicalPotential <= 0.0) 0.0
        else (
                chemicalPotential * (exponent - 1.0) /
                        (monomerVolume * coefficient * exponent)
                ).pow(1.0 / (exponent - 1.0))

}

/**
 * A sum of power-law limbs — the interpolation that carries every asymptote of its terms,
 * built the same way the measured equation of state of `C-0002` is built.
 *
 * It is honest about its own weakness: where two limbs cross, the sum is twice either of them,
 * so it is an **upper** interpolation of the crossover shape, not a measurement of it.
 */
@Serializable
data class AdditiveInteraction(
    override val name: String,
    val terms: List<PowerLawInteraction>
) : InteractionFreeEnergy {

    init {
        require(terms.isNotEmpty()) { "terms must not be empty" }
        require(terms.all { it.monomerVolume == terms[0].monomerVolume }) {
            "every term must use the same monomerVolume, were: ${terms.map { it.monomerVolume }}"
        }
        require(terms.all { it.temperature == terms[0].temperature }) {
            "every term must use the same temperature, were: ${terms.map { it.temperature }}"
        }
    }

    override val monomerVolume: Double get() = terms[0].monomerVolume

    override val temperature: Double get() = terms[0].temperature

    override fun freeEnergyDensity(volumeFraction: Double): Double =
        terms.sumOf { it.freeEnergyDensity(volumeFraction) }

    override fun osmoticPressure(volumeFraction: Double): Double =
        terms.sumOf { it.osmoticPressure(volumeFraction) }

    override fun osmoticPressureSlope(volumeFraction: Double): Double =
        terms.sumOf { it.osmoticPressureSlope(volumeFraction) }

    override fun exchangeChemicalPotential(volumeFraction: Double): Double =
        terms.sumOf { it.exchangeChemicalPotential(volumeFraction) }

    override fun exchangeChemicalPotentialSlope(volumeFraction: Double): Double =
        terms.sumOf { it.exchangeChemicalPotentialSlope(volumeFraction) }

    /**
     * Inverted by a safeguarded Newton iteration rather than in closed form, because a sum of
     * powers has none. `μ` is strictly increasing, so the bracket is never lost and the
     * bisection fallback makes the iteration unconditionally convergent.
     */
    override fun volumeFractionAtChemicalPotential(chemicalPotential: Double): Double {
        if (chemicalPotential <= 0.0) return 0.0
        var low = 0.0
        // the sum dominates every single limb, so each limb alone reaches the target at a
        // LARGER volume fraction than the sum does: the smallest single-limb inverse brackets it
        var high = terms.minOf { it.volumeFractionAtChemicalPotential(chemicalPotential) }
        var guess = 0.5 * (low + high)
        repeat(NEWTON_ITERATIONS) {
            val residual = exchangeChemicalPotential(guess) - chemicalPotential
            if (abs(residual) <= CONVERGENCE * chemicalPotential) return guess
            if (residual > 0.0) high = guess else low = guess
            val slope = exchangeChemicalPotentialSlope(guess)
            val step = if (slope > 0.0) guess - residual / slope else Double.NaN
            guess = if (step > low && step < high) step else 0.5 * (low + high)
        }
        return guess
    }

}

/** The unscreened two-body limb `Π = (B/2)(k_BT/v₀) φ²`, `B` the dimensionless excluded volume `v/v₀`. */
fun twoBodyInteraction(
    secondVirialCoefficient: Double,
    monomerVolume: Double,
    temperature: Double = ROOM_TEMPERATURE
): PowerLawInteraction {
    require(secondVirialCoefficient > 0.0) {
        "secondVirialCoefficient must be positive, was: $secondVirialCoefficient"
    }
    return PowerLawInteraction(
        name = "two-body",
        coefficient = 0.5 * secondVirialCoefficient * thermalEnergy(temperature) / monomerVolume,
        exponent = 2.0,
        monomerVolume = monomerVolume,
        temperature = temperature
    )
}

/** The screened des Cloizeaux limb `Π = α (k_BT/v₀) φ^(9/4)` of the measured equation of state. */
fun desCloizeauxInteraction(
    crossoverIndex: Double,
    monomerVolume: Double,
    temperature: Double = ROOM_TEMPERATURE
): PowerLawInteraction {
    require(crossoverIndex > 0.0) { "crossoverIndex must be positive, was: $crossoverIndex" }
    return PowerLawInteraction(
        name = "des-Cloizeaux",
        coefficient = crossoverIndex * thermalEnergy(temperature) / monomerVolume,
        exponent = ScalingEquationOfState.DES_CLOIZEAUX_EXPONENT,
        monomerVolume = monomerVolume,
        temperature = temperature
    )
}

/**
 * The three-body limb `Π = C₃ (k_BT/v₀) φ³`, hence `f = C₃(k_BT/v₀)φ³/2`.
 *
 * Carried because the measured virial description of PEG/water needs it: at the Gen-1 layer's own
 * volume fraction the third virial term is a third of the second, not a correction.
 */
fun threeBodyInteraction(
    thirdVirialCoefficient: Double,
    monomerVolume: Double,
    temperature: Double = ROOM_TEMPERATURE
): PowerLawInteraction {
    require(thirdVirialCoefficient > 0.0) {
        "thirdVirialCoefficient must be positive, was: $thirdVirialCoefficient"
    }
    return PowerLawInteraction(
        name = "three-body",
        coefficient = thirdVirialCoefficient * thermalEnergy(temperature) / monomerVolume,
        exponent = 3.0,
        monomerVolume = monomerVolume,
        temperature = temperature
    )
}

/**
 * Converts a published osmotic second virial coefficient `A₂` into the dimensionless `B = v/v₀`
 * this project's free energies are written in.
 *
 * The conversion is `B = 2 A₂ M₀ / V̄` and it is **temperature-free**: the `RT` of the osmometry
 * convention and the `k_BT` of the free energy cancel through Avogadro's number. `A₂` must be in
 * the convention `Π/(RT) = c/M + A₂c² + A₃c³` with `c` in `g/cm³` and **no factor of two** —
 * the factor-of-two trap is real and the activity-coefficient convention `a₂₂ = 2 M A₂` is the
 * one to watch for.
 */
fun PegWater.reducedSecondVirialCoefficient(osmoticSecondVirial: Double): Double {
    require(osmoticSecondVirial > 0.0) {
        "osmoticSecondVirial must be positive, was: $osmoticSecondVirial"
    }
    return 2.0 * osmoticSecondVirial * monomerMolarMass / partialSpecificVolume
}

/** Converts a published osmotic third virial coefficient `A₃` into `C₃` of `Π = C₃(k_BT/v₀)φ³`. */
fun PegWater.reducedThirdVirialCoefficient(osmoticThirdVirial: Double): Double {
    require(osmoticThirdVirial > 0.0) {
        "osmoticThirdVirial must be positive, was: $osmoticThirdVirial"
    }
    return osmoticThirdVirial * monomerMolarMass /
            (partialSpecificVolume * partialSpecificVolume)
}

/**
 * Returns the Flory-Huggins `χ = (1 − B)/2` implied by a dimensionless second virial coefficient.
 *
 * This is the route `P-3` could not take — the adopted crossover equation of state is non-virial
 * by construction and yields no `A₂` — and it is available now only because a *separate*,
 * virial-convention measurement of the same material was read.
 */
fun floryHugginsChi(reducedSecondVirialCoefficient: Double): Double =
    0.5 * (1.0 - reducedSecondVirialCoefficient)

/** The sum of [terms], named [name]. */
fun additiveInteraction(
    name: String,
    terms: List<PowerLawInteraction>
): AdditiveInteraction = AdditiveInteraction(name, terms)

/**
 * Returns `B = 2 α (α N)^(−1/5)` — the two-body coefficient whose limb meets the des Cloizeaux
 * limb exactly at the measured crossover `φ# = (αN)^(−4/5)`.
 *
 * The `N^(−1/5)` is not put in: it falls out, and it is the known chain-length dependence of the
 * second virial coefficient of a swollen coil written in monomer units. That the matching
 * procedure reproduces it is the reason to trust it as a stand-in until an independently
 * measured `A₂` for PEG in water is in hand.
 */
fun matchedSecondVirialCoefficient(
    crossoverIndex: Double,
    monomersPerChain: Double
): Double {
    require(crossoverIndex > 0.0) { "crossoverIndex must be positive, was: $crossoverIndex" }
    require(monomersPerChain >= 1.0) {
        "monomersPerChain must be at least 1, was: $monomersPerChain"
    }
    return 2.0 * crossoverIndex * (crossoverIndex * monomersPerChain).pow(-1.0 / 5.0)
}

/**
 * Returns the `B` that makes a two-body [AlexanderBoxLayer] reproduce `L₀ = N a^(5/3) σ^(1/3)`
 * exactly: `B = 6 n_K a⁵ / (v₀ b²)`.
 *
 * This is what the Alexander-de Gennes unity prefactor is *worth* as a physical excluded volume,
 * once the elasticity is written on the measured Kuhn parameters rather than on `a`. Comparing it
 * against [matchedSecondVirialCoefficient] is how `T-1c` converts a convention into a number.
 */
fun alexanderDeGennesImpliedSecondVirialCoefficient(peg: PegWater): Double =
    6.0 * peg.monomersPerKuhnSegment * peg.effectiveMonomerLength.pow(5.0) /
            (peg.monomerVolume * peg.kuhnLength * peg.kuhnLength)

/**
 * Returns the total free-energy density in `pN/nm²` whose Legendre transform is
 * [ScalingEquationOfState.pressure], obtained by `f(φ) = φ ∫ Π(φ')/φ'² dφ'`.
 *
 * The integration constant is a term linear in `φ`, which contributes nothing to the pressure
 * and nothing to any difference taken here, and is therefore set to zero.
 */
fun ScalingEquationOfState.freeEnergyDensity(volumeFraction: Double): Double {
    require(volumeFraction > 0.0 && volumeFraction <= 1.0) {
        "volumeFraction must be within (0.0, 1.0], was: $volumeFraction"
    }
    return pressureScale * (
            volumeFraction * ln(volumeFraction) / monomersPerChain +
                    0.8 * crossoverIndex *
                    volumeFraction.pow(ScalingEquationOfState.DES_CLOIZEAUX_EXPONENT)
            )
}

/**
 * Returns [freeEnergyDensity] with the **chain translational entropy removed** — the free energy
 * of a *grafted* layer of the same material at the same volume fraction.
 *
 * This is the single substitution task `T-1c` turns on. Grafting removes the chains' ability to
 * explore the volume, and the `φ lnφ / N` term is exactly that ability. What is left has osmotic
 * pressure `α (k_BT/v₀) φ^(9/4)` at every density.
 */
fun ScalingEquationOfState.interactionFreeEnergyDensity(volumeFraction: Double): Double {
    require(volumeFraction > 0.0 && volumeFraction <= 1.0) {
        "volumeFraction must be within (0.0, 1.0], was: $volumeFraction"
    }
    return pressureScale * 0.8 * crossoverIndex *
            volumeFraction.pow(ScalingEquationOfState.DES_CLOIZEAUX_EXPONENT)
}

/**
 * Returns the thermal blob size in **Kuhn segments**, `g_T = (b³/v)²`, where
 * `v = B n_K v₀` is the excluded volume of one Kuhn segment.
 *
 * A chain shorter than `g_T` is not swollen: it is a Gaussian coil whose excluded-volume
 * interactions have not yet accumulated to `k_BT`. That is a checkable statement about *this*
 * material at *this* chain length, and it is what decides whether the layer's elasticity is
 * blob elasticity or Gaussian elasticity on the measured Kuhn parameters.
 */
fun PegWater.thermalBlobKuhnSegments(secondVirialCoefficient: Double): Double {
    require(secondVirialCoefficient > 0.0) {
        "secondVirialCoefficient must be positive, was: $secondVirialCoefficient"
    }
    val kuhnExcludedVolume = secondVirialCoefficient * kuhnSegmentVolume
    val ratio = kuhnLength.pow(3.0) / kuhnExcludedVolume
    return ratio * ratio
}

private const val NEWTON_ITERATIONS = 200

/** Relative residual at which the chemical-potential inversion is considered converged. */
private const val CONVERGENCE = 1e-15

private fun requirePhysical(volumeFraction: Double) {
    require(volumeFraction >= 0.0 && volumeFraction <= 1.0) {
        "volumeFraction must be within [0.0, 1.0], was: $volumeFraction"
    }
}

private fun requireNonNegative(volumeFraction: Double) {
    require(volumeFraction >= 0.0) {
        "volumeFraction must not be negative, was: $volumeFraction"
    }
}
