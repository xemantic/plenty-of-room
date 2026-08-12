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

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.material.ScalingEquationOfState
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertFailsWith

private val peg = PegWater()

/** Avogadro's number, used only to cross-check a published unit conversion. */
private const val AVOGADRO = 6.02214076e23

/** A chain length in the middle of the `T-1` design space. */
private const val CHAIN_LENGTH = 200.0

private val eos = peg.equationOfState(CHAIN_LENGTH)

private val desCloizeaux = desCloizeauxInteraction(
    crossoverIndex = peg.crossoverIndex,
    monomerVolume = peg.monomerVolume
)

/** `B = 2 α (α N)^(−1/5)`, the two-body coefficient that meets the des Cloizeaux limb at `φ#`. */
private val MATCHED_SECOND_VIRIAL = matchedSecondVirialCoefficient(
    crossoverIndex = peg.crossoverIndex,
    monomersPerChain = CHAIN_LENGTH
)

private val twoBody = twoBodyInteraction(
    secondVirialCoefficient = MATCHED_SECOND_VIRIAL,
    monomerVolume = peg.monomerVolume
)

private val crossover = additiveInteraction("two-body + des-Cloizeaux", listOf(twoBody, desCloizeaux))

private val interactions = listOf(twoBody, desCloizeaux, crossover)

class InteractionFreeEnergyTest {

    // ---------------------------------------------------------------- gate 1

    @Test
    fun `gate 1 dimensional consistency - the osmotic pressure should be the Legendre transform of the free energy density`() {
        // Pi = phi * df/dphi - f, and mu = v0 * df/dphi, so Pi = phi * mu / v0 - f,
        // which is the identity every model in this file has to satisfy by construction
        interactions.forEach { interaction ->
            listOf(0.001, 0.01, 0.05, 0.2, 0.6).forEach { phi ->
                val transform = phi * interaction.exchangeChemicalPotential(phi) /
                        interaction.monomerVolume - interaction.freeEnergyDensity(phi)
                assert(transform.isCloseTo(interaction.osmoticPressure(phi), 1e-12))
            }
        }
    }

    @Test
    fun `gate 1 dimensional consistency - the des Cloizeaux limb should equal the measured equation of state limb exactly`() {
        listOf(0.005, 0.03, 0.1, 0.4).forEach { phi ->
            assert(
                desCloizeaux.osmoticPressure(phi).isCloseTo(
                    eos.desCloizeauxPressure(phi), 1e-12
                )
            )
        }
    }

    @Test
    fun `gate 1 dimensional consistency - the exchange chemical potential should be an energy per monomer`() {
        // k_BT/v0 * v0 = k_BT: the two-body chemical potential at phi is exactly B k_BT phi
        val b = MATCHED_SECOND_VIRIAL
        listOf(0.01, 0.1).forEach { phi ->
            assert(
                twoBody.exchangeChemicalPotential(phi)
                    .isCloseTo(b * thermalEnergy() * phi, 1e-12)
            )
        }
    }

    // ---------------------------------------------------------------- gate 2

    @Test
    fun `gate 2 limiting cases - each limb should carry its own osmotic exponent exactly`() {
        val low = 0.02
        val high = 0.04
        assert(
            (twoBody.osmoticPressure(high) / twoBody.osmoticPressure(low))
                .isCloseTo(2.0.pow(2.0), 1e-12)
        )
        assert(
            (desCloizeaux.osmoticPressure(high) / desCloizeaux.osmoticPressure(low))
                .isCloseTo(2.0.pow(9.0 / 4.0), 1e-12)
        )
    }

    @Test
    fun `gate 2 limiting cases - the additive crossover should approach the two-body limb below and the des Cloizeaux limb above`() {
        // With the matched B the two limbs cross at phi# = 0.026, and phi <= 1 leaves too little
        // room above it to reach the des Cloizeaux asymptote — which is itself worth knowing.
        // The asymptotics are therefore exercised with limbs whose crossing is moved out of the
        // way, one in each direction.
        val twoBodyDominated = additiveInteraction(
            "two-body dominated", listOf(twoBodyInteraction(1e4, peg.monomerVolume), desCloizeaux)
        )
        val desCloizeauxDominated = additiveInteraction(
            "des-Cloizeaux dominated",
            listOf(twoBodyInteraction(1e-8, peg.monomerVolume), desCloizeaux)
        )
        assert(
            (twoBodyDominated.osmoticPressure(0.05) /
                    twoBodyInteraction(1e4, peg.monomerVolume).osmoticPressure(0.05))
                .isCloseTo(1.0, 1e-3)
        )
        assert(
            (desCloizeauxDominated.osmoticPressure(0.05) / desCloizeaux.osmoticPressure(0.05))
                .isCloseTo(1.0, 1e-3)
        )
        // and the matched pair does cross exactly where the measured crossover sits
        val crossoverPoint = (MATCHED_SECOND_VIRIAL / (2.0 * peg.crossoverIndex)).pow(4.0)
        assert(crossoverPoint.isCloseTo(eos.crossoverVolumeFraction, 1e-12))
    }

    @Test
    fun `gate 2 limiting cases - the matched second virial coefficient should equate the two limbs exactly at the crossover volume fraction`() {
        val crossoverVolumeFraction = eos.crossoverVolumeFraction
        assert(
            twoBody.osmoticPressure(crossoverVolumeFraction)
                .isCloseTo(desCloizeaux.osmoticPressure(crossoverVolumeFraction), 1e-12)
        )
    }

    // ---------------------------------------------------------------- gate 3

    @Test
    fun `gate 3 symmetry - inverting the exchange chemical potential should round-trip for every interaction`() {
        interactions.forEach { interaction ->
            listOf(0.002, 0.02, 0.09, 0.35).forEach { phi ->
                val potential = interaction.exchangeChemicalPotential(phi)
                assert(
                    interaction.volumeFractionAtChemicalPotential(potential)
                        .isCloseTo(phi, 1e-9)
                )
            }
        }
    }

    @Test
    fun `gate 3 symmetry - the ratio of the pressure slope to the potential slope should be phi over v0 for every interaction`() {
        // this identity holds for ANY local free energy: Pi' = phi f'' and mu' = v0 f''.
        // The generalised strong-stretching stiffness is written on it, so it is asserted
        // rather than assumed.
        interactions.forEach { interaction ->
            listOf(0.005, 0.05, 0.3).forEach { phi ->
                val ratio = interaction.osmoticPressureSlope(phi) /
                        interaction.exchangeChemicalPotentialSlope(phi)
                assert(ratio.isCloseTo(phi / interaction.monomerVolume, 1e-12))
            }
        }
    }

    // ---------------------------------------------------------------- gate 4

    @Test
    fun `gate 4 numerical convergence - analytic slopes should match a central difference of their own functions`() {
        interactions.forEach { interaction ->
            listOf(0.01, 0.05, 0.2).forEach { phi ->
                val step = phi * 1e-6
                val pressureSlope =
                    (interaction.osmoticPressure(phi + step) -
                            interaction.osmoticPressure(phi - step)) / (2.0 * step)
                assert(pressureSlope.isCloseTo(interaction.osmoticPressureSlope(phi), 1e-6))
                val potentialSlope =
                    (interaction.exchangeChemicalPotential(phi + step) -
                            interaction.exchangeChemicalPotential(phi - step)) / (2.0 * step)
                assert(
                    potentialSlope.isCloseTo(
                        interaction.exchangeChemicalPotentialSlope(phi), 1e-6
                    )
                )
            }
        }
    }

    // ---------------------------------------------------------------- gate 5

    @Test
    fun `gate 5 literature cross-check - integrating the measured equation of state should give the free energy the crossover is built from`() {
        // f(phi) = phi * integral of Pi/phi'^2 dphi' = (k_BT/v0)[phi ln phi / N + (4 alpha/5) phi^(9/4)]
        // Verified by differentiating BACK: Pi = phi f' - f must reproduce the fitted equation of state.
        listOf(0.001, 0.02, 0.15, 0.5).forEach { phi ->
            val step = phi * 1e-7
            val slope = (eos.freeEnergyDensity(phi + step) - eos.freeEnergyDensity(phi - step)) /
                    (2.0 * step)
            val pressure = phi * slope - eos.freeEnergyDensity(phi)
            assert(pressure.isCloseTo(eos.pressure(phi), 1e-6))
        }
    }

    @Test
    fun `gate 5 literature cross-check - removing the chain translational entropy should leave exactly the des Cloizeaux limb`() {
        // This is the whole of the T-1c step-1 argument, as an executable statement:
        // a GRAFTED layer has no chain translational entropy, and the fitted equation of state's
        // van't Hoff limb IS that entropy, so the layer's interaction pressure is the 9/4 limb
        // at EVERY density — the bulk crossover in Pi is, for this equation of state, an artefact
        // of a term a grafted layer does not have.
        listOf(0.001, 0.02, 0.15, 0.5).forEach { phi ->
            val translational = eos.freeEnergyDensity(phi) - eos.interactionFreeEnergyDensity(phi)
            val step = phi * 1e-7
            val slope = (eos.interactionFreeEnergyDensity(phi + step) -
                    eos.interactionFreeEnergyDensity(phi - step)) / (2.0 * step)
            val interactionPressure = phi * slope - eos.interactionFreeEnergyDensity(phi)
            assert(interactionPressure.isCloseTo(eos.desCloizeauxPressure(phi), 1e-6))
            // and what was removed is the van't Hoff limb, nothing else
            val translationalSlope =
                (eos.freeEnergyDensity(phi + step) - eos.interactionFreeEnergyDensity(phi + step) -
                        (eos.freeEnergyDensity(phi - step) -
                                eos.interactionFreeEnergyDensity(phi - step))) / (2.0 * step)
            assert(
                (phi * translationalSlope - translational)
                    .isCloseTo(eos.vanTHoffPressure(phi), 1e-6)
            )
        }
    }

    @Test
    fun `gate 5 literature cross-check - the matched second virial coefficient should carry the dilute-coil chain-length scaling`() {
        // B = 2 alpha (alpha N)^(-1/5): the N^(-1/5) is the known chain-length dependence of the
        // second virial coefficient of a swollen coil in monomer units, which the matching
        // procedure reproduces rather than assumes.
        val short = matchedSecondVirialCoefficient(peg.crossoverIndex, 100.0)
        val long = matchedSecondVirialCoefficient(peg.crossoverIndex, 3200.0)
        assert((short / long).isCloseTo(32.0.pow(1.0 / 5.0), 1e-12))
    }

    @Test
    fun `gate 5 literature cross-check - converting the measured A2 should reproduce the published excluded volume`() {
        // Shvets (arXiv:2010.08110) Eq. 2.24 gives the excluded volume per monomer directly as
        // v = 2 M0^2 A2 / N_A, and tabulates 12.2 A^3 for PEG/water at A2 = 1.9e-3.
        // Our conversion goes a different way — B = 2 A2 M0 / Vbar, then v = B v0 — and has to
        // land on the same number, or one of the two conventions is wrong.
        val osmoticSecondVirial = 1.9e-3
        val reduced = peg.reducedSecondVirialCoefficient(osmoticSecondVirial)
        val excludedVolume = reduced * peg.monomerVolume
        val published = 2.0 * peg.monomerMolarMass * peg.monomerMolarMass *
                osmoticSecondVirial / AVOGADRO * 1e21
        assert(excludedVolume.isCloseTo(published, 1e-9))
        assert(excludedVolume.isCloseTo(0.01225, 1e-3))
    }

    @Test
    fun `gate 5 literature cross-check - the measured A2 should give a marginal solvent, not a good one`() {
        // chi = 0.399 against 0.367 measured independently by SAXS on PEG-4600 in D2O
        // (Pedersen & Sommer 2005). Both say PEG/water at 300 K is MARGINAL: the excluded volume
        // is a fifth of the monomer volume, not of order it.
        val reduced = peg.reducedSecondVirialCoefficient(1.9e-3)
        assert(floryHugginsChi(reduced).isCloseTo(0.3985, 1e-3))
        assert(reduced < 0.25)
    }

    @Test
    fun `gate 5 literature cross-check - the matched second virial coefficient should exceed the measured one`() {
        // Matching the two limbs at phi# is a construction, and comparing it against a genuinely
        // independent measurement is the only way to find out what the construction costs.
        // It costs a factor of ~2, and it runs in the direction that makes the layer stiffer.
        val matched = matchedSecondVirialCoefficient(peg.crossoverIndex, CHAIN_LENGTH)
        val measured = peg.reducedSecondVirialCoefficient(1.9e-3)
        assert(matched / measured > 1.5)
        assert(matched / measured < 2.5)
    }

    @Test
    fun `gate 5 literature cross-check - the third virial term should not be negligible at the layer volume fraction`() {
        // If it were, dropping it would be safe. It is a third of the second virial term at
        // phi = 0.033, which is why the measured virial bracket carries both.
        val twoBody = twoBodyInteraction(
            peg.reducedSecondVirialCoefficient(1.9e-3), peg.monomerVolume
        )
        val threeBody = threeBodyInteraction(
            peg.reducedThirdVirialCoefficient(2.0e-2), peg.monomerVolume
        )
        val ratio = threeBody.osmoticPressure(0.033) / twoBody.osmoticPressure(0.033)
        assert(ratio > 0.2)
        assert(ratio < 0.7)
    }

    @Test
    fun `gate 5 literature cross-check - the des Cloizeaux limb should exceed the measured virial pressure at the layer volume fraction`() {
        // The two measured descriptions of the SAME material disagree by about a factor of 1.5
        // where the Gen-1 layer sits. That disagreement IS the uncertainty on the layer response,
        // and it is a factor, not an exponent.
        val virial = additiveInteraction(
            "virial",
            listOf(
                twoBodyInteraction(peg.reducedSecondVirialCoefficient(1.9e-3), peg.monomerVolume),
                threeBodyInteraction(peg.reducedThirdVirialCoefficient(2.0e-2), peg.monomerVolume)
            )
        )
        val ratio = desCloizeaux.osmoticPressure(0.033) / virial.osmoticPressure(0.033)
        assert(ratio > 1.0)
        assert(ratio < 2.0)
    }

    // ---------------------------------------------------------- validity range

    @Test
    fun `should reject volume fractions outside the physical range`() {
        interactions.forEach { interaction ->
            assertFailsWith<IllegalArgumentException> {
                interaction.osmoticPressure(-1e-9)
            }
            assertFailsWith<IllegalArgumentException> {
                interaction.osmoticPressure(1.5)
            }
        }
    }

    @Test
    fun `should evaluate to zero at zero volume fraction rather than throwing`() {
        // the outer edge of an uncompressed strong-stretching profile IS phi = 0,
        // so the free energy has to be defined there
        interactions.forEach { interaction ->
            assert(interaction.osmoticPressure(0.0) == 0.0)
            assert(interaction.freeEnergyDensity(0.0) == 0.0)
            assert(interaction.exchangeChemicalPotential(0.0) == 0.0)
            assert(interaction.volumeFractionAtChemicalPotential(0.0) == 0.0)
        }
    }

    @Test
    fun `should reject a power law with an exponent that has no free energy`() {
        assertFailsWith<IllegalArgumentException> {
            PowerLawInteraction(
                name = "degenerate",
                coefficient = 1.0,
                exponent = 1.0,
                monomerVolume = peg.monomerVolume
            )
        }
    }

    @Test
    fun `the des Cloizeaux exponent should be the one the equation of state was fitted with`() {
        assert(desCloizeaux.exponent == ScalingEquationOfState.DES_CLOIZEAUX_EXPONENT)
    }

}
