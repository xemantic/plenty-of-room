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
import com.xemantic.kotlin.test.have
import com.xemantic.kotlin.test.should
import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.equipartitionRms
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.thermalEnergy
import org.jetbrains.bio.viktor.asF64Array
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertFailsWith

private val brush = PolymerBrush(
    monomerSize = 1.0,
    monomersPerChain = 100.0,
    graftingDensity = 0.125
)

/** The Alexander-de Gennes height of [brush], exactly 50 nm by construction. */
private const val HEIGHT = 50.0

/** The Gen-1 tile footprint of §3 of the problem definition, 40 × 40 nm. */
private const val TILE_AREA = 1600.0

private val deGennes = DeGennesScaling()

private val milner = MilnerWittenCates(
    excludedVolume = alexanderDeGennesMatchedExcludedVolume(brush.monomerSize)
)

private val models = listOf(deGennes, milner)

class BrushCompressionTest {

    // ---------------------------------------------------------------- gate 2

    @Test
    fun `gate 2 limiting cases - every model should exert no pressure at its own equilibrium height`() {
        models.forEach { model ->
            val equilibrium = model.equilibriumHeight(brush)
            assert(model.disjoiningPressure(brush, equilibrium).isCloseTo(0.0, 1e-12))
        }
    }

    @Test
    fun `gate 2 limiting cases - every model should push the wall away under compression`() {
        models.forEach { model ->
            val equilibrium = model.equilibriumHeight(brush)
            listOf(0.9, 0.7, 0.5, 0.25, 0.1).forEach { x ->
                assert(model.disjoiningPressure(brush, x * equilibrium) > 0.0)
            }
        }
    }

    @Test
    fun `gate 2 limiting cases - every model should stiffen monotonically as the layer is compressed`() {
        models.forEach { model ->
            val equilibrium = model.equilibriumHeight(brush)
            val stiffnesses = listOf(0.9, 0.7, 0.5, 0.25, 0.1).map { x ->
                model.stiffnessPerArea(brush, x * equilibrium)
            }
            assert(stiffnesses == stiffnesses.sortedBy { it })
        }
    }

    /**
     * The qualitative divergence between the two functional forms in circulation,
     * and the reason §2 of the problem definition flags MWC as "not the same thing":
     * the scaling form opens with a finite stiffness, the SCF form opens with none,
     * because its outer edge is diffuse. Which one is used decides whether "the stiffness
     * of the polymer layer" is even a well-posed single number at the resting height.
     */
    @Test
    fun `gate 2 limiting cases - the SCF brush should have zero stiffness at first contact`() {
        assert(milner.stiffnessPerArea(brush, HEIGHT).isCloseTo(0.0, 1e-12))
    }

    @Test
    fun `gate 2 limiting cases - the scaling brush should have finite stiffness at first contact`() {
        assert(deGennes.stiffnessPerArea(brush, HEIGHT) > 0.0)
    }

    // ---------------------------------------------------------------- gate 1

    /**
     * Gate 1 — dimensional consistency. The scaling-form stiffness at the equilibrium
     * height must reduce to `(m + n) k_BT / (s^3 L0)`, which for the de Gennes exponents
     * `9/4` and `3/4` is exactly `3 k_BT sigma^(3/2) / L0`. Asserted against that closed
     * form, which is arrived at differently from the implementation's derivative.
     */
    @Test
    fun `gate 1 dimensional consistency - scaling stiffness at equilibrium should reduce to 3 kT sigma to the 3 halves over L0`() {
        val expected = 3.0 * thermalEnergy() * brush.graftingDensity.pow(1.5) / HEIGHT
        assert(deGennes.stiffnessPerArea(brush, HEIGHT).isCloseTo(expected))
    }

    @Test
    fun `gate 1 dimensional consistency - stiffness times area should reduce to a force per length`() {
        val perArea = deGennes.stiffnessPerArea(brush, 0.5 * HEIGHT)
        val stiffness = deGennes.stiffness(brush, 0.5 * HEIGHT, TILE_AREA)
        assert(stiffness.isCloseTo(perArea * TILE_AREA))
    }

    @Test
    fun `gate 1 dimensional consistency - pressure times area should reduce to the load`() {
        val pressure = deGennes.disjoiningPressure(brush, 0.5 * HEIGHT)
        val load = deGennes.load(brush, 0.5 * HEIGHT, TILE_AREA)
        assert(load.isCloseTo(pressure * TILE_AREA))
    }

    /**
     * Gate 1, continued — the composite scaling of the equilibrium stiffness in the
     * grafting density. `k/A = 3 kT sigma^(3/2) / L0` with `L0 ∝ sigma^(1/3)` gives
     * `k/A ∝ sigma^(7/6)`, so doubling the grafting density at fixed chemistry and
     * chain length must raise it by `2^(7/6)`, not by `2^(3/2)`.
     */
    @Test
    fun `gate 1 dimensional consistency - equilibrium stiffness should scale as the grafting density to the 7 sixths`() {
        val denser = brush.copy(graftingDensity = 2.0 * brush.graftingDensity)
        val ratio = deGennes.stiffnessPerArea(denser, deGennes.equilibriumHeight(denser)) /
                deGennes.stiffnessPerArea(brush, HEIGHT)
        assert(ratio.isCloseTo(2.0.pow(7.0 / 6.0)))
    }

    // ---------------------------------------------------------------- gate 3

    /**
     * Gate 3 — conservation. The SCF segment-density profile is only meaningful if it
     * conserves the grafted material: integrating it over the compressed layer must return
     * the coverage `Gamma = N sigma`, at every compression. The profile is quadratic in z,
     * so Simpson's rule is exact and any discrepancy is a modelling error, not a quadrature one.
     */
    @Test
    fun `gate 3 conservation - the SCF density profile should conserve the grafted coverage`() {
        val coverage = brush.monomersPerChain * brush.graftingDensity
        listOf(1.0, 0.8, 0.5, 0.2).forEach { x ->
            val height = x * HEIGHT
            assert(simpson(0.0, height, 200) { z ->
                milner.segmentDensity(brush, height, z)
            }.isCloseTo(coverage, 1e-10))
        }
    }

    /**
     * Gate 3 — equipartition. The layer under the tile is the spring that sets the tile's
     * thermal position noise, so `sigma = sqrt(k_BT/k)` has to be reachable from the same
     * stiffness the actuation calculation uses. This is the hand-off to task `T-8`.
     */
    @Test
    fun `gate 3 conservation - equipartition should be reachable from the layer stiffness`() {
        val stiffness = deGennes.stiffness(brush, 0.5 * HEIGHT, TILE_AREA)
        val rms = equipartitionRms(stiffness)
        assert(rms.isCloseTo((thermalEnergy() / stiffness).pow(0.5)))
    }

    // ---------------------------------------------------------------- gate 4

    /**
     * Gate 4 — numerical convergence. Every model's analytic stiffness is checked against a
     * central difference of its own pressure, and the difference is required to shrink
     * quadratically as the step halves. This catches a derivative that is merely plausible.
     */
    @Test
    fun `gate 4 convergence - analytic stiffness should match a central difference of the pressure`() {
        models.forEach { model ->
            val equilibrium = model.equilibriumHeight(brush)
            listOf(0.8, 0.5, 0.3).forEach { x ->
                val height = x * equilibrium
                val step = 1e-5 * equilibrium
                val difference = -(model.disjoiningPressure(brush, height + step) -
                        model.disjoiningPressure(brush, height - step)) / (2.0 * step)
                assert(model.stiffnessPerArea(brush, height).isCloseTo(difference, 1e-7))
            }
        }
    }

    @Test
    fun `gate 4 convergence - the central difference error should fall quadratically with the step`() {
        val height = 0.5 * HEIGHT
        val analytic = deGennes.stiffnessPerArea(brush, height)
        fun errorAt(step: Double): Double {
            val difference = -(deGennes.disjoiningPressure(brush, height + step) -
                    deGennes.disjoiningPressure(brush, height - step)) / (2.0 * step)
            return abs(difference - analytic)
        }
        val coarse = errorAt(1e-3 * HEIGHT)
        val fine = errorAt(0.5e-3 * HEIGHT)
        assert((coarse / fine).isCloseTo(4.0, 1e-2))
    }

    // ---------------------------------------------------------------- gate 5

    /**
     * Gate 5 — the scaling law's own premise. Under strong compression the scaling form must
     * inherit the osmotic exponent of `Pi ∝ phi^m` unchanged, because `phi ∝ 1/h` at fixed
     * coverage. Halving the height must therefore multiply the pressure by `2^m`, and it is
     * this exponent that §2 says we may not be entitled to.
     */
    @Test
    fun `gate 5 premises - the scaling form should inherit the osmotic exponent under strong compression`() {
        listOf(9.0 / 4.0, 2.0, 3.0).forEach { exponent ->
            val model = DeGennesScaling(osmoticExponent = exponent)
            val ratio = model.disjoiningPressure(brush, 0.05 * HEIGHT) /
                    model.disjoiningPressure(brush, 0.10 * HEIGHT)
            assert(ratio.isCloseTo(2.0.pow(exponent), 1e-2))
        }
    }

    /**
     * Gate 5 — the SCF form is mean-field by construction: its wall pressure is the
     * second-virial osmotic pressure at the wall concentration, so it must approach `m = 2`
     * under strong compression regardless of what the scaling form is asked to use.
     */
    @Test
    fun `gate 5 premises - the SCF form should approach the mean-field exponent under strong compression`() {
        val ratio = milner.disjoiningPressure(brush, 0.05 * HEIGHT) /
                milner.disjoiningPressure(brush, 0.10 * HEIGHT)
        assert(ratio.isCloseTo(4.0, 1e-2))
    }

    /**
     * Gate 5 — the two forms are only comparable if they are made to agree on the one thing
     * both claim to predict, the unperturbed height. They do so at `w = pi^2 a^3 / 4`,
     * which is independent of N and of the grafting density; any residual difference in the
     * compression curves is then functional form, not calibration.
     */
    @Test
    fun `gate 5 premises - the matched excluded volume should equate the SCF and scaling heights`() {
        assert(milner.equilibriumHeight(brush).isCloseTo(brush.alexanderDeGennesHeight))
        assert(alexanderDeGennesMatchedExcludedVolume(1.0).isCloseTo(PI * PI / 4.0))
    }

    @Test
    fun `gate 5 premises - the matched excluded volume should not depend on chain length or grafting density`() {
        val other = PolymerBrush(monomerSize = 1.0, monomersPerChain = 500.0, graftingDensity = 0.02)
        assert(milner.equilibriumHeight(other).isCloseTo(other.alexanderDeGennesHeight))
    }

    // ------------------------------------------------------- the working point

    @Test
    fun `should return the equilibrium height under no load`() {
        models.forEach { model ->
            assert(
                model.heightUnderLoad(brush, load = 0.0, area = TILE_AREA)
                    .isCloseTo(model.equilibriumHeight(brush))
            )
        }
    }

    @Test
    fun `should return the height at which the layer carries the applied load`() {
        models.forEach { model ->
            val load = 100.0
            val height = model.heightUnderLoad(brush, load, TILE_AREA)
            assert(height < model.equilibriumHeight(brush))
            assert(model.load(brush, height, TILE_AREA).isCloseTo(load, 1e-9))
        }
    }

    @Test
    fun `should compress further under a heavier load`() {
        models.forEach { model ->
            assert(
                model.heightUnderLoad(brush, 200.0, TILE_AREA) <
                        model.heightUnderLoad(brush, 100.0, TILE_AREA)
            )
        }
    }

    @Test
    fun `should not return the height under a tensile load`() {
        assertFailsWith<IllegalArgumentException> {
            deGennes.heightUnderLoad(brush, load = -1.0, area = TILE_AREA)
        } should {
            have(message == "load must not be tensile, was: -1.0")
        }
    }

    // ---------------------------------------------------------- validity range

    /**
     * The validity range travels with the model, and is enforced rather than documented.
     * Above the equilibrium height a non-adsorbing brush simply loses contact with the wall
     * and the pressure is zero; the scaling form's negative branch there is an artefact of
     * the interpolation, and silently returning it is how an unphysical attraction reaches
     * a downstream task.
     */
    @Test
    fun `should not evaluate the pressure above the equilibrium height`() {
        assertFailsWith<IllegalArgumentException> {
            deGennes.disjoiningPressure(brush, 50.1)
        } should {
            have(message == "height must be within (0.0, ${deGennes.equilibriumHeight(brush)}], was: 50.1")
        }
    }

    @Test
    fun `should not evaluate the pressure at a non-positive height`() {
        assertFailsWith<IllegalArgumentException> {
            deGennes.disjoiningPressure(brush, 0.0)
        } should {
            have(message == "height must be within (0.0, ${deGennes.equilibriumHeight(brush)}], was: 0.0")
        }
    }

    @Test
    fun `should state the temperature of every model`() {
        assert(deGennes.temperature == ROOM_TEMPERATURE)
        assert(milner.temperature == ROOM_TEMPERATURE)
    }

    @Test
    fun `should not accept a non-positive excluded volume`() {
        assertFailsWith<IllegalArgumentException> {
            MilnerWittenCates(excludedVolume = 0.0)
        } should {
            have(message == "excludedVolume must be positive, was: 0.0")
        }
    }

}

/**
 * Composite Simpson's rule over [intervals] sub-intervals, exact for the quadratic
 * SCF density profile. The weighted samples are accumulated with viktor rather than
 * a hand-rolled loop, per the project convention.
 */
private fun simpson(
    from: Double,
    to: Double,
    intervals: Int,
    function: (Double) -> Double
): Double {
    require(intervals % 2 == 0) { "intervals must be even, was: $intervals" }
    val step = (to - from) / intervals
    val weights = DoubleArray(intervals + 1) { i ->
        when {
            i == 0 || i == intervals -> 1.0
            i % 2 == 1 -> 4.0
            else -> 2.0
        }
    }.asF64Array()
    val samples = DoubleArray(intervals + 1) { i -> function(from + i * step) }.asF64Array()
    return weights.dot(samples) * step / 3.0
}
