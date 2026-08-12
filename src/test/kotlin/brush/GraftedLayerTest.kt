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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertFailsWith

private val peg = PegWater()

/** §3: the 40 × 40 nm tile footprint. */
private const val TILE_AREA = 1600.0

/** §3: the target force. */
private const val TARGET_FORCE = 100.0

private const val CHAIN_LENGTH = 220.0
private const val GRAFTING_DENSITY = 0.024

private val chain = peg.graftedChain(CHAIN_LENGTH, GRAFTING_DENSITY)

private val twoBody = twoBodyInteraction(
    secondVirialCoefficient = matchedSecondVirialCoefficient(peg.crossoverIndex, CHAIN_LENGTH),
    monomerVolume = peg.monomerVolume
)

private val desCloizeaux = desCloizeauxInteraction(
    crossoverIndex = peg.crossoverIndex,
    monomerVolume = peg.monomerVolume
)

private val crossover = additiveInteraction(
    "two-body + des-Cloizeaux", listOf(twoBody, desCloizeaux)
)

private val models: List<GraftedLayerModel> = listOf(
    AlexanderBoxLayer(twoBody),
    AlexanderBoxLayer(desCloizeaux),
    AlexanderBoxLayer(crossover),
    StrongStretchingLayer(twoBody),
    StrongStretchingLayer(desCloizeaux),
    StrongStretchingLayer(crossover)
)

class GraftedLayerTest {

    // ---------------------------------------------------------------- gate 1

    @Test
    fun `gate 1 dimensional consistency - the equilibrium height should be exactly linear in the chain length`() {
        // h ~ N holds for BOTH profile models and ANY pure power-law interaction, because the
        // interaction free energy per chain and the parabolic curvature carry compensating powers
        // of N. It is what makes N(L0) an exact inversion rather than an iteration.
        listOf(
            AlexanderBoxLayer(twoBody), AlexanderBoxLayer(desCloizeaux),
            StrongStretchingLayer(twoBody), StrongStretchingLayer(desCloizeaux)
        ).forEach { model ->
            val single = model.equilibriumHeight(peg.graftedChain(150.0, GRAFTING_DENSITY))
            val double = model.equilibriumHeight(peg.graftedChain(300.0, GRAFTING_DENSITY))
            assert((double / single).isCloseTo(2.0, 1e-9))
        }
    }

    @Test
    fun `gate 1 dimensional consistency - a pressure over a tile should reduce to a force and a stiffness per area to a stiffness`() {
        models.forEach { model ->
            val height = model.equilibriumHeight(chain) * 0.7
            assert(
                model.load(chain, height, TILE_AREA)
                    .isCloseTo(model.disjoiningPressure(chain, height) * TILE_AREA, 1e-12)
            )
            assert(
                model.stiffness(chain, height, TILE_AREA)
                    .isCloseTo(model.stiffnessPerArea(chain, height) * TILE_AREA, 1e-12)
            )
        }
    }

    @Test
    fun `gate 1 dimensional consistency - the three disputed height exponents should each be reproduced by the free energy that implies it`() {
        // The heart of T-1c: L0 ~ sigma^(1/3) and L0 ~ sigma^(5/13) are NOT alternative readings
        // of one calculation. The first is what a two-body interaction gives, the second what the
        // des Cloizeaux interaction gives, both minimised against the SAME Gaussian chain
        // elasticity. The disagreement CH-0001 points at is an interaction-law disagreement.
        val factor = 8.0
        listOf(AlexanderBoxLayer(twoBody), StrongStretchingLayer(twoBody)).forEach { model ->
            val low = model.equilibriumHeight(peg.graftedChain(CHAIN_LENGTH, 0.01))
            val high = model.equilibriumHeight(peg.graftedChain(CHAIN_LENGTH, 0.01 * factor))
            assert((high / low).isCloseTo(factor.pow(1.0 / 3.0), 1e-9))
        }
        listOf(AlexanderBoxLayer(desCloizeaux), StrongStretchingLayer(desCloizeaux)).forEach { model ->
            val low = model.equilibriumHeight(peg.graftedChain(CHAIN_LENGTH, 0.01))
            val high = model.equilibriumHeight(peg.graftedChain(CHAIN_LENGTH, 0.01 * factor))
            assert((high / low).isCloseTo(factor.pow(5.0 / 13.0), 1e-9))
        }
    }

    // ---------------------------------------------------------------- gate 2

    @Test
    fun `gate 2 limiting cases - every model should exert no pressure at its own equilibrium height`() {
        models.forEach { model ->
            val equilibrium = model.equilibriumHeight(chain)
            assert(model.disjoiningPressure(chain, equilibrium).isCloseTo(0.0, 1e-9))
        }
    }

    @Test
    fun `gate 2 limiting cases - every model should push the tile away and stiffen monotonically under compression`() {
        models.forEach { model ->
            val equilibrium = model.equilibriumHeight(chain)
            var previousPressure = 0.0
            var previousStiffness = -1.0
            (9 downTo 2).forEach { tenth ->
                val height = equilibrium * tenth / 10.0
                val pressure = model.disjoiningPressure(chain, height)
                val stiffness = model.stiffnessPerArea(chain, height)
                assert(pressure > previousPressure)
                assert(stiffness > previousStiffness)
                previousPressure = pressure
                previousStiffness = stiffness
            }
        }
    }

    @Test
    fun `gate 2 limiting cases - the box profile should open with finite stiffness and the strong-stretching profile with none`() {
        // The C-0001 finding S-1 survives the change of free energy: a diffuse outer edge means
        // the layer offers no restoring force until it is meaningfully compressed, so
        // "the stiffness of the layer" is only well posed at a stated compression.
        listOf(twoBody, desCloizeaux, crossover).forEach { interaction ->
            val box = AlexanderBoxLayer(interaction)
            val sst = StrongStretchingLayer(interaction)
            assert(box.stiffnessPerArea(chain, box.equilibriumHeight(chain)) > 0.0)
            assert(
                sst.stiffnessPerArea(chain, sst.equilibriumHeight(chain))
                    .isCloseTo(0.0, 1e-9)
            )
        }
    }

    @Test
    fun `gate 2 limiting cases - the generalised strong stretching should reproduce the standing Milner-Witten-Cates model in the two-body limit`() {
        // The strongest available cross-check: with a two-body interaction and the Kuhn parameters
        // collapsed onto the old single monomer size, the general solver must reproduce the
        // C-0001 implementation exactly — height, wall density, pressure and stiffness.
        val monomerSize = 1.0
        val legacyBrush = PolymerBrush(
            monomerSize = monomerSize,
            monomersPerChain = 100.0,
            graftingDensity = 0.125
        )
        val excludedVolume = alexanderDeGennesMatchedExcludedVolume(monomerSize)
        val legacy = MilnerWittenCates(excludedVolume = excludedVolume)
        val monomerVolume = peg.monomerVolume
        val general = StrongStretchingLayer(
            twoBodyInteraction(
                secondVirialCoefficient = excludedVolume / monomerVolume,
                monomerVolume = monomerVolume
            )
        )
        val generalChain = GraftedChain(
            monomersPerChain = legacyBrush.monomersPerChain,
            graftingDensity = legacyBrush.graftingDensity,
            monomerVolume = monomerVolume,
            kuhnLength = monomerSize,
            monomersPerKuhnSegment = 1.0
        )
        val legacyHeight = legacy.equilibriumHeight(legacyBrush)
        assert(general.equilibriumHeight(generalChain).isCloseTo(legacyHeight, 1e-9))
        listOf(0.95, 0.8, 0.6, 0.35).forEach { ratio ->
            val height = legacyHeight * ratio
            assert(
                general.wallVolumeFraction(generalChain, height).isCloseTo(
                    legacy.segmentDensity(legacyBrush, height, height) * monomerVolume, 1e-8
                )
            )
            assert(
                general.disjoiningPressure(generalChain, height)
                    .isCloseTo(legacy.disjoiningPressure(legacyBrush, height), 1e-8)
            )
            assert(
                general.stiffnessPerArea(generalChain, height)
                    .isCloseTo(legacy.stiffnessPerArea(legacyBrush, height), 1e-7)
            )
        }
    }

    // ---------------------------------------------------------------- gate 3

    @Test
    fun `gate 3 conservation - the strong-stretching profile should conserve the grafted coverage at every compression`() {
        listOf(twoBody, desCloizeaux, crossover).forEach { interaction ->
            val sst = StrongStretchingLayer(interaction)
            val equilibrium = sst.equilibriumHeight(chain)
            listOf(1.0, 0.9, 0.7, 0.5, 0.3).forEach { ratio ->
                assert(
                    sst.coverage(chain, equilibrium * ratio)
                        .isCloseTo(chain.coverage, 1e-8)
                )
            }
        }
    }

    @Test
    fun `gate 3 conservation - the disjoining pressure should be minus the derivative of the layer free energy for every model`() {
        // The contact-value theorem P(h) = Pi_int(phi(h)) is a THEOREM for the strong-stretching
        // profile, not a definition: at the wall every chain present has its free end there and
        // therefore carries no tension. It is verified thermodynamically here, against the free
        // energy assembled independently from the profile.
        models.forEach { model ->
            val equilibrium = model.equilibriumHeight(chain)
            listOf(0.8, 0.6, 0.4).forEach { ratio ->
                val height = equilibrium * ratio
                val step = height * 1e-5
                val slope = (model.freeEnergyPerArea(chain, height + step) -
                        model.freeEnergyPerArea(chain, height - step)) / (2.0 * step)
                assert((-slope).isCloseTo(model.disjoiningPressure(chain, height), 1e-5))
            }
        }
    }

    @Test
    fun `gate 3 conservation - the work done compressing the layer should equal the free energy it gains`() {
        models.forEach { model ->
            val equilibrium = model.equilibriumHeight(chain)
            val target = equilibrium * 0.5
            val panels = 2000
            val step = (equilibrium - target) / panels
            var work = 0.0
            repeat(panels) { i ->
                val height = target + (i + 0.5) * step
                work += model.disjoiningPressure(chain, height) * step
            }
            val gained = model.freeEnergyPerArea(chain, target) -
                    model.freeEnergyPerArea(chain, equilibrium)
            assert(work.isCloseTo(gained, 1e-4))
        }
    }

    @Test
    fun `gate 3 symmetry - the response should scale as the interaction strength to the power one over m plus one`() {
        // At fixed layer height, grafting density and compression ratio the whole response scales
        // as a single power of the interaction coefficient, because the chain length the height
        // demands moves against it: N ~ K^(-1/(m+1)) and k ~ K^(+1/(m+1)).
        // For the des Cloizeaux exponent that is 4/13 = 0.31, so a factor of two in how strong the
        // interaction really is inside a brush — which is what C-0007 puts at issue — moves the
        // stiffness by 24%, not by 100%. This is the sensitivity statement, as an identity.
        val factor = 16.0
        listOf(twoBody, desCloizeaux).forEach { base ->
            val scaled = base.copy(coefficient = base.coefficient * factor)
            val expected = factor.pow(1.0 / (base.exponent + 1.0))
            listOf<(InteractionFreeEnergy) -> GraftedLayerModel>(
                { AlexanderBoxLayer(it) }, { StrongStretchingLayer(it) }
            ).forEach { build ->
                val plain = build(base)
                val strong = build(scaled)
                val plainLength = plain.chainLengthForHeight(peg, 10.0, GRAFTING_DENSITY)
                val strongLength = strong.chainLengthForHeight(peg, 10.0, GRAFTING_DENSITY)
                assert((plainLength / strongLength).isCloseTo(expected, 1e-6))
                val plainStiffness = plain.stiffness(
                    peg.graftedChain(plainLength, GRAFTING_DENSITY), 8.0, TILE_AREA
                )
                val strongStiffness = strong.stiffness(
                    peg.graftedChain(strongLength, GRAFTING_DENSITY), 8.0, TILE_AREA
                )
                assert((strongStiffness / plainStiffness).isCloseTo(expected, 1e-6))
            }
        }
    }

    // ---------------------------------------------------------------- gate 4

    @Test
    fun `gate 4 numerical convergence - the analytic stiffness should match a central difference of its own pressure`() {
        models.forEach { model ->
            val equilibrium = model.equilibriumHeight(chain)
            listOf(0.8, 0.6, 0.4).forEach { ratio ->
                val height = equilibrium * ratio
                val step = height * 1e-5
                val slope = (model.disjoiningPressure(chain, height + step) -
                        model.disjoiningPressure(chain, height - step)) / (2.0 * step)
                assert((-slope).isCloseTo(model.stiffnessPerArea(chain, height), 1e-5))
            }
        }
    }

    @Test
    fun `gate 4 numerical convergence - the closed-form equilibrium height should match the height solved from the coverage constraint`() {
        // The power-law closed form is what the study runs, because it makes N(L0) exact.
        // Here it is checked against the same constraint solved numerically, which is the only
        // route available to the additive crossover interaction.
        listOf(twoBody, desCloizeaux).forEach { interaction ->
            val sst = StrongStretchingLayer(interaction)
            assert(
                sst.equilibriumHeightByCoverage(chain)
                    .isCloseTo(sst.equilibriumHeight(chain), 1e-8)
            )
            val box = AlexanderBoxLayer(interaction)
            assert(
                box.equilibriumHeightByRoot(chain)
                    .isCloseTo(box.equilibriumHeight(chain), 1e-8)
            )
        }
    }

    @Test
    fun `gate 4 numerical convergence - the profile quadrature should converge with the panel count`() {
        val sst = StrongStretchingLayer(desCloizeaux)
        val height = sst.equilibriumHeight(chain) * 0.6
        val reference = StrongStretchingLayer(desCloizeaux, panels = 8192)
            .disjoiningPressure(chain, height)
        val coarse = StrongStretchingLayer(desCloizeaux, panels = 64)
            .disjoiningPressure(chain, height)
        val fine = StrongStretchingLayer(desCloizeaux, panels = 128)
            .disjoiningPressure(chain, height)
        val coarseError = abs(coarse - reference)
        val fineError = abs(fine - reference)
        assert(fineError < coarseError)
        // and the production setting is already converged to well below the fit uncertainty on alpha
        assert(abs(sst.disjoiningPressure(chain, height) - reference) / reference < 1e-8)
    }

    @Test
    fun `gate 4 numerical convergence - the height under a load should invert the pressure law`() {
        models.forEach { model ->
            val height = model.heightUnderLoad(chain, TARGET_FORCE, TILE_AREA)
            assert(
                (model.disjoiningPressure(chain, height) * TILE_AREA)
                    .isCloseTo(TARGET_FORCE, 1e-8)
            )
        }
    }

    // ---------------------------------------------------------------- gate 5

    @Test
    fun `gate 5 literature cross-check - the Alexander-de Gennes height should be recovered by a two-body box layer at a stated excluded volume`() {
        // L0 = N a^(5/3) sigma^(1/3) is exactly what a two-body Alexander free energy gives.
        // The excluded volume it implies, B = 6 n_K a^5 / (v0 b^2), is derived here rather than
        // asserted, and comparing it against the measurement-consistent B is what says by how much
        // the unity prefactor of the Alexander-de Gennes convention overstates the layer.
        val implied = alexanderDeGennesImpliedSecondVirialCoefficient(peg)
        val box = AlexanderBoxLayer(
            twoBodyInteraction(implied, peg.monomerVolume)
        )
        listOf(0.005, 0.024, 0.3).forEach { density ->
            val test = peg.graftedChain(CHAIN_LENGTH, density)
            val alexanderDeGennes = CHAIN_LENGTH *
                    peg.effectiveMonomerLength.pow(5.0 / 3.0) * density.pow(1.0 / 3.0)
            assert(box.equilibriumHeight(test).isCloseTo(alexanderDeGennes, 1e-9))
        }
    }

    @Test
    fun `gate 5 literature cross-check - the layer should be only marginally stretched across the whole design space`() {
        // The premise the strong-stretching theory needs is L0 >> R0, and it is not met anywhere
        // in the 5-10 nm x realisable-sigma box. Reported as a validity bound, not hidden.
        val sst = StrongStretchingLayer(desCloizeaux)
        listOf(5.0, 7.0, 10.0).forEach { layerHeight ->
            listOf(0.01, 0.1, 1.0).forEach { density ->
                val length = sst.chainLengthForHeight(peg, layerHeight, density)
                val test = peg.graftedChain(length, density)
                // nowhere in the box does the layer reach even a factor of three,
                // and sigma = 1 nm^-2 is a melt-like density §4(a) rules out anyway
                assert(test.stretchingRatio(layerHeight) < 2.5)
                if (density <= 0.1) {
                    // across the REALISABLE window the chains are barely stretched at all
                    assert(test.stretchingRatio(layerHeight) < 1.5)
                }
            }
        }
    }

    @Test
    fun `gate 5 literature cross-check - the chain should sit inside its own thermal blob for every design point`() {
        // If the chain is shorter than a thermal blob it is not swollen, and Gaussian elasticity
        // on the MEASURED Kuhn parameters — not blob elasticity — is the correct one. That is what
        // licenses the free-energy minimisation used here in place of the blob construction.
        val blob = peg.thermalBlobKuhnSegments(peg.reducedSecondVirialCoefficient(1.9e-3))
        // and not marginally: the blob is over a thousand Kuhn segments — about 167 kDa —
        // while the whole Gen-1 design space is 60 to 300 monomers, i.e. 3 to 14 kDa
        assert(blob > 1000.0)
        listOf(60.0, 200.0, 300.0).forEach { length ->
            assert(length / peg.monomersPerKuhnSegment < 0.1 * blob)
        }
    }

    // ---------------------------------------------------------- validity range

    @Test
    fun `should reject a height outside the layer`() {
        models.forEach { model ->
            val equilibrium = model.equilibriumHeight(chain)
            assertFailsWith<IllegalArgumentException> {
                model.disjoiningPressure(chain, equilibrium * 1.001)
            }
            assertFailsWith<IllegalArgumentException> {
                model.disjoiningPressure(chain, 0.0)
            }
        }
    }

    @Test
    fun `the grafted chain should carry the measured Kuhn parameters rather than a single monomer size`() {
        assert(chain.kuhnSegments.isCloseTo(CHAIN_LENGTH / peg.monomersPerKuhnSegment, 1e-12))
        assert(
            chain.idealEndToEnd.isCloseTo(
                peg.kuhnLength * (CHAIN_LENGTH / peg.monomersPerKuhnSegment).pow(0.5), 1e-12
            )
        )
        assert(chain.occupiedThickness.isCloseTo(
            CHAIN_LENGTH * GRAFTING_DENSITY * peg.monomerVolume, 1e-12
        ))
    }

    @Test
    fun `the parabolic curvature should be the Milner-Witten-Cates one written in Kuhn segments`() {
        val expected = 3.0 * PI * PI * com.xemantic.nano.plentyofroom.thermalEnergy() *
                peg.monomersPerKuhnSegment /
                (8.0 * CHAIN_LENGTH * CHAIN_LENGTH * peg.kuhnLength * peg.kuhnLength)
        assert(chain.parabolicCurvature().isCloseTo(expected, 1e-12))
    }

    @Test
    fun `inverting the height relation should return the chain length that produces it`() {
        models.forEach { model ->
            listOf(5.0, 7.0, 10.0).forEach { layerHeight ->
                val length = model.chainLengthForHeight(peg, layerHeight, GRAFTING_DENSITY)
                assert(
                    model.equilibriumHeight(peg.graftedChain(length, GRAFTING_DENSITY))
                        .isCloseTo(layerHeight, 1e-8)
                )
            }
        }
    }

}
