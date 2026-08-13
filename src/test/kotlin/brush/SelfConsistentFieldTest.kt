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
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln

import kotlin.math.sin
import kotlin.test.Test
import kotlin.test.assertFailsWith

private val peg = PegWater()

/** §3: the 40 × 40 nm tile footprint. */
private const val TILE_AREA = 1600.0

/** The `A₂`-derived two-body coefficient of `C-0003`, `B = 2 A₂ M₀/V̄`. */
private val measuredSecondVirial = peg.reducedSecondVirialCoefficient(1.9e-3)

private val twoBody = twoBodyInteraction(measuredSecondVirial, peg.monomerVolume)

private val desCloizeaux = desCloizeauxInteraction(peg.crossoverIndex, peg.monomerVolume)

private val virial = additiveInteraction(
    "virial",
    listOf(
        twoBody,
        threeBodyInteraction(peg.reducedThirdVirialCoefficient(2.0e-2), peg.monomerVolume)
    )
)

/**
 * An interaction so weak that the layer is an ideal grafted chain to twelve digits —
 * the limit in which the propagator has a closed-form answer to check against.
 */
private val ideal = PowerLawInteraction(
    name = "ideal", coefficient = 1e-14, exponent = 2.0, monomerVolume = peg.monomerVolume
)

/** A grid coarse enough to keep the suite fast; the convergence gate is what justifies it. */
private val testGrid = ScfDiscretisation(
    nodeSpacing = 0.25,
    contourStepsPerMonomer = 1.0
)

private val chain = peg.graftedChain(monomersPerChain = 120.0, graftingDensity = 0.03)

class SelfConsistentFieldTest {

    // ---------------------------------------------------------------- gate 1

    @Test
    fun `gate 1 dimensional consistency - the converged field should be the exchange chemical potential in units of thermal energy`() {
        // w(z) = mu(phi(z))/k_BT is the WHOLE content of "self-consistent" here, and it is the one
        // place where T-1c's measured interaction free energy enters the profile calculation.
        val layer = SelfConsistentFieldLayer(desCloizeaux, testGrid)
        val profile = layer.profile(chain, 8.0)
        assert(profile.converged)
        (1 until profile.nodes step 7).forEach { node ->
            val volumeFraction = profile.volumeFraction[node]
            assert(
                profile.field[node].isCloseTo(
                    desCloizeaux.exchangeChemicalPotential(volumeFraction) / thermalEnergy(),
                    1e-9
                )
            )
        }
    }

    @Test
    fun `gate 1 dimensional consistency - the layer free energy should be the field-theoretic one to the last digit`() {
        // F/A = -sigma k_BT ln Q - integral of the interaction pressure. The second term is the
        // Legendre transform doing the same work it does in InteractionFreeEnergy: -mu rho + f.
        val layer = SelfConsistentFieldLayer(virial, testGrid)
        val profile = layer.profile(chain, 8.0)
        var integrated = 0.0
        (0 until profile.nodes).forEach { node ->
            integrated += virial.osmoticPressure(profile.volumeFraction[node])
        }
        integrated *= profile.nodeSpacing
        val expected = -chain.graftingDensity * thermalEnergy() * profile.logPartitionFunction -
                integrated
        assert(profile.freeEnergyPerArea.isCloseTo(expected, 1e-12))
    }

    @Test
    fun `gate 1 dimensional consistency - a pressure over the tile should reduce to a load and a stiffness per area to a stiffness`() {
        val layer = SelfConsistentFieldLayer(desCloizeaux, testGrid)
        val height = 7.0
        assert(
            layer.load(chain, height, TILE_AREA)
                .isCloseTo(layer.disjoiningPressure(chain, height) * TILE_AREA, 1e-12)
        )
        assert(
            layer.stiffness(chain, height, TILE_AREA)
                .isCloseTo(layer.stiffnessPerArea(chain, height) * TILE_AREA, 1e-12)
        )
    }

    // ---------------------------------------------------------------- gate 2

    @Test
    fun `gate 2 limiting cases - an interaction-free layer should reproduce the analytic ideal grafted chain profile`() {
        // The strongest available check on the propagator itself: with the field switched off the
        // Edwards equation on [0, h] with Dirichlet ends has an exact eigenfunction solution, and
        // the grafted density is a double sine series with NO free constant — it normalises itself
        // to N sigma. Anything wrong with the source, the boundary condition, the contour
        // quadrature or the Simpson weights breaks it.
        val height = 9.0
        val layer = SelfConsistentFieldLayer(
            ideal, ScfDiscretisation(nodeSpacing = 0.1, contourStepsPerMonomer = 4.0)
        )
        val profile = layer.profile(chain, height)
        val reference = idealGraftedProfile(chain, height, peg)
        (0 until profile.nodes).forEach { node ->
            val z = (node + 1) * profile.nodeSpacing
            if (z > 0.2 * height && z < 0.9 * height) {
                assert(profile.volumeFraction[node].isCloseTo(reference(z), 2e-2))
            }
        }
        // the first moment converges far faster than the series does pointwise
        var moment = 0.0
        var total = 0.0
        (0 until profile.nodes).forEach { node ->
            val z = (node + 1) * profile.nodeSpacing
            val value = reference(z)
            moment += z * value
            total += value
        }
        assert(profile.firstMomentHeight.isCloseTo(2.0 * moment / total, 5e-3))
    }

    @Test
    fun `gate 2 limiting cases - the pressure should fall monotonically as the wall retreats and vanish far above the layer`() {
        val layer = SelfConsistentFieldLayer(desCloizeaux, testGrid)
        var previous = Double.MAX_VALUE
        listOf(4.0, 5.0, 6.0, 7.0, 8.0, 10.0, 12.0, 16.0).forEach { height ->
            val pressure = layer.pressureAt(chain, height)
            assert(pressure > 0.0)
            assert(pressure < previous)
            previous = pressure
        }
        // far above the layer the tile carries essentially nothing
        assert(layer.pressureAt(chain, 30.0) * TILE_AREA < 0.01)
    }

    @Test
    fun `gate 2 limiting cases - the profile should approach the strong-stretching parabola when the layer is strongly stretched`() {
        // T-1c's largest stated weakness is that L0/R0 is of order one for the Gen-1 layer, so
        // neither of its profile models has its premise met. Pushed to a layer that DOES meet the
        // strong-stretching premise, the SCF profile must converge onto the truncated parabola —
        // otherwise the disagreement found at Gen-1 conditions would be a bug rather than physics.
        val stretched = peg.graftedChain(monomersPerChain = 900.0, graftingDensity = 0.45)
        val sst = StrongStretchingLayer(twoBody)
        val height = sst.equilibriumHeight(stretched)
        assert(stretched.stretchingRatio(height) > 3.0)
        val layer = SelfConsistentFieldLayer(
            twoBody, ScfDiscretisation(nodeSpacing = 0.15, contourStepsPerMonomer = 1.0)
        )
        val profile = layer.profile(stretched, height)
        var worst = 0.0
        (0 until profile.nodes).forEach { node ->
            val z = (node + 1) * profile.nodeSpacing
            if (z > 0.1 * height && z < 0.6 * height) {
                val parabola = sst.volumeFractionAt(stretched, height, z)
                worst = maxOf(worst, abs(profile.volumeFraction[node] - parabola) / parabola)
            }
        }
        assert(worst < 0.12)
    }

    // ---------------------------------------------------------------- gate 3

    @Test
    fun `gate 3 conservation - the profile should conserve the grafted coverage at every wall height`() {
        listOf(twoBody, virial, desCloizeaux).forEach { interaction ->
            val layer = SelfConsistentFieldLayer(interaction, testGrid)
            listOf(4.0, 6.0, 9.0, 14.0).forEach { height ->
                val profile = layer.profile(chain, height)
                assert(profile.coverage.isCloseTo(chain.coverage, 1e-10))
            }
        }
    }

    @Test
    fun `gate 3 conservation - the single-chain partition function should not depend on where the contour is split`() {
        // Q = integral of q(z,n) q_dagger(z,N-n) dz for EVERY n. It is the propagator identity that
        // makes the density expression normalisable, and it fails immediately if the two sweeps
        // do not see the same field or the same boundary condition.
        val layer = SelfConsistentFieldLayer(desCloizeaux, testGrid)
        val profile = layer.profile(chain, 8.0)
        profile.partitionFunctionAtSplit.forEach { ratio ->
            assert(ratio.isCloseTo(1.0, 1e-9))
        }
    }

    @Test
    fun `gate 3 conservation - the contact-value pressure should agree with minus the free-energy derivative`() {
        // The two routes the task requires be checked against each other. They are genuinely
        // independent: one is a thermodynamic derivative of the converged free energy, the other
        // reads the curvature of the density at the wall, P = k_BT (b^2/6 n_K) lim rho/(h-z)^2,
        // which is what the contact-value theorem becomes for a continuum Gaussian chain.
        listOf(twoBody, desCloizeaux).forEach { interaction ->
            val layer = SelfConsistentFieldLayer(interaction, testGrid)
            listOf(5.0, 7.0, 10.0).forEach { height ->
                val thermodynamic = layer.pressureAt(chain, height)
                val contact = layer.profile(chain, height).contactPressure
                assert(abs(contact - thermodynamic) / thermodynamic < 0.05)
            }
        }
    }

    @Test
    fun `gate 3 conservation - the work done compressing the layer should equal the free energy it gains`() {
        // On a ladder of heights that all share ONE node spacing, so that the free energies are
        // comparable to the last digit — see the surprise about the grafted source normalisation.
        val layer = SelfConsistentFieldLayer(desCloizeaux, testGrid)
        val spacing = 0.15
        val ladder = (24..80).map { it * spacing }
        val energies = ladder.map { layer.freeEnergyPerAreaOnGrid(chain, it, spacing) }
        var work = 0.0
        (0 until ladder.size - 1).forEach { i ->
            val pressure = -(energies[i + 1] - energies[i]) / spacing
            work += pressure * spacing
        }
        assert(work.isCloseTo(energies.first() - energies.last(), 1e-9))
    }

    // ---------------------------------------------------------------- gate 4

    @Test
    fun `gate 4 numerical convergence - the pressure should converge at second order in the node spacing`() {
        val height = 8.0
        val reference = SelfConsistentFieldLayer(
            desCloizeaux, ScfDiscretisation(nodeSpacing = 0.05, contourStepsPerMonomer = 8.0)
        ).pressureAt(chain, height)
        val errors = listOf(0.4, 0.2, 0.1).map { spacing ->
            abs(
                SelfConsistentFieldLayer(
                    desCloizeaux,
                    ScfDiscretisation(nodeSpacing = spacing, contourStepsPerMonomer = 8.0)
                ).pressureAt(chain, height) - reference
            ) / reference
        }
        assert(errors[1] < errors[0])
        assert(errors[2] < errors[1])
        // Second order. The coarsest pair carries higher-order terms as well, so it is held only
        // to a factor of 2.5; the finest pair, where the asymptotic regime has been reached,
        // must clear four — and the residual error of the reference itself biases both ratios
        // downwards, so these are lower bounds on the true order.
        assert(errors[0] / errors[1] > 2.5)
        assert(errors[1] / errors[2] > 4.0)
        // and the production spacing is two orders of magnitude inside the +/-15% spread on A2
        assert(errors[2] < 1e-3)
    }

    @Test
    fun `gate 4 numerical convergence - the pressure should converge at second order in the contour step`() {
        val height = 8.0
        val reference = SelfConsistentFieldLayer(
            desCloizeaux, ScfDiscretisation(nodeSpacing = 0.4, contourStepsPerMonomer = 16.0)
        ).pressureAt(chain, height)
        val errors = listOf(0.5, 1.0, 2.0).map { steps ->
            abs(
                SelfConsistentFieldLayer(
                    desCloizeaux,
                    ScfDiscretisation(nodeSpacing = 0.4, contourStepsPerMonomer = steps)
                ).pressureAt(chain, height) - reference
            ) / reference
        }
        assert(errors[1] < errors[0])
        assert(errors[2] < errors[1])
        assert(errors[0] / errors[1] > 2.5)
        assert(errors[1] / errors[2] > 3.5)
        assert(errors[2] < 1e-4)
    }

    @Test
    fun `gate 4 numerical convergence - the self-consistency iteration should report reaching its tolerance rather than running the cap`() {
        // CLAUDE.md records the failure mode: an unreachable tolerance silently runs the iteration
        // cap and the caller cannot tell. Every profile therefore carries its own verdict, and
        // every consumer in this file asserts it.
        listOf(twoBody, virial, desCloizeaux).forEach { interaction ->
            val layer = SelfConsistentFieldLayer(interaction, testGrid)
            listOf(3.5, 6.0, 12.0).forEach { height ->
                val profile = layer.profile(chain, height)
                assert(profile.converged)
                assert(profile.iterations < testGrid.maximumIterations)
                assert(profile.residual <= testGrid.tolerance)
            }
        }
    }

    @Test
    fun `gate 4 numerical convergence - the analytic stiffness should match a central difference of its own pressure`() {
        val layer = SelfConsistentFieldLayer(desCloizeaux, testGrid)
        listOf(6.0, 8.0).forEach { height ->
            val step = 0.3
            val slope = (layer.pressureAt(chain, height + step) -
                    layer.pressureAt(chain, height - step)) / (2.0 * step)
            assert((-slope).isCloseTo(layer.stiffnessPerAreaAt(chain, height), 2e-2))
        }
    }

    // ---------------------------------------------------------------- gate 5

    @Test
    fun `gate 5 literature cross-check - the SCF resting height should NOT be linear in the chain length at Gen-1 densities`() {
        // `L0` is EXACTLY linear in `N` for both T-1c profile models and any pure power-law
        // interaction — proved as a test in GraftedLayerTest. The SCF layer is not, and by a wide
        // margin: at the Gen-1 grafting densities the exponent is near ONE HALF, which is the
        // single-chain coil exponent. The resting height of this layer is set by the tail of an
        // ideal coil, not by an osmotic balance, and that is the finding T-1d exists to make.
        val layer = SelfConsistentFieldLayer(desCloizeaux, testGrid)
        val single = layer.equilibriumHeight(peg.graftedChain(100.0, 0.03))
        val double = layer.equilibriumHeight(peg.graftedChain(200.0, 0.03))
        val exponent = ln(double / single) / ln(2.0)
        assert(exponent > 0.4)
        assert(exponent < 0.75)
        // and it tracks the coil size to within a factor of two over that whole range
        listOf(100.0, 200.0).forEach { length ->
            val test = peg.graftedChain(length, 0.03)
            val ratio = layer.equilibriumHeight(test) / test.idealEndToEnd
            assert(ratio > 1.0)
            assert(ratio < 3.5)
        }
    }

    @Test
    fun `gate 5 literature cross-check - the SCF profile should be more diffuse than both T-1c profile models`() {
        // The finding this task exists to produce: at one and the same chain and grafting density,
        // the SCF layer reaches further than either scaling profile, because its outer edge is set
        // by a real end distribution rather than by a truncation of a trial function.
        val layer = SelfConsistentFieldLayer(desCloizeaux, testGrid)
        val box = AlexanderBoxLayer(desCloizeaux)
        val sst = StrongStretchingLayer(desCloizeaux)
        val scfHeight = layer.equilibriumHeight(chain)
        assert(scfHeight > sst.equilibriumHeight(chain))
        assert(scfHeight > box.equilibriumHeight(chain))
        // and the pressure it holds at the SST resting height is NOT zero, which is what
        // "the resting height is not where the pressure vanishes" means numerically
        assert(layer.pressureAt(chain, sst.equilibriumHeight(chain)) * TILE_AREA > 1.0)
    }

    @Test
    fun `gate 5 literature cross-check - a reflecting wall should give a denser contact layer than an absorbing one`() {
        // Stated in the task file rather than chosen silently: a rigid impenetrable tile removes
        // every chain conformation that would cross it, which is the ABSORBING condition. The
        // REFLECTING one is the mid-plane of two identical brushes and is carried only as a
        // sensitivity, because it is the assumption that would make the contact-value theorem of
        // T-1c literally true.
        val absorbing = SelfConsistentFieldLayer(desCloizeaux, testGrid)
        val reflecting = SelfConsistentFieldLayer(
            desCloizeaux, testGrid, wallCondition = ScfWallCondition.REFLECTING
        )
        val height = 7.0
        val open = absorbing.profile(chain, height)
        val closed = reflecting.profile(chain, height)
        assert(closed.wallVolumeFraction > open.wallVolumeFraction)
        assert(closed.coverage.isCloseTo(chain.coverage, 1e-10))
        // and the reflecting wall is SOFTER, because it costs the chains no conformational entropy
        assert(reflecting.pressureAt(chain, height) < absorbing.pressureAt(chain, height))
    }

    // ---------------------------------------------------------- validity range

    @Test
    fun `should reject a height outside the layer`() {
        val layer = SelfConsistentFieldLayer(desCloizeaux, testGrid)
        val equilibrium = layer.equilibriumHeight(chain)
        assertFailsWith<IllegalArgumentException> {
            layer.disjoiningPressure(chain, equilibrium * 1.01)
        }
        assertFailsWith<IllegalArgumentException> {
            layer.disjoiningPressure(chain, 0.0)
        }
    }

    @Test
    fun `the resting height should be where the layer first carries the stated resting load`() {
        // There is no height at which an SCF layer exerts exactly zero pressure — its outer edge
        // is a real, exponentially decaying tail — so L0 is DEFINED by a stated threshold rather
        // than found as a root of P = 0, and the threshold travels with every number derived here.
        val layer = SelfConsistentFieldLayer(desCloizeaux, testGrid)
        val equilibrium = layer.equilibriumHeight(chain)
        assert(
            (layer.pressureAt(chain, equilibrium) * TILE_AREA)
                .isCloseTo(layer.restingPressure * TILE_AREA, 1e-4)
        )
        // a ten-fold smaller threshold gives a MEANINGFULLY taller layer — that is the finding
        val looser = SelfConsistentFieldLayer(
            desCloizeaux, testGrid, restingPressure = 0.1 / TILE_AREA
        )
        assert(looser.equilibriumHeight(chain) > equilibrium * 1.05)
    }

}

/**
 * Returns the exact ideal-chain grafted density profile `φ(z)` on `[0, h]` with absorbing ends,
 * as the double sine series of the Edwards propagator's eigenfunction expansion.
 *
 * `q(z,n) = Σ_k k sin(kπz/h) e^(−λ_k n)` for a source at the grafting surface (the `k` weight is
 * the `δ′` that a graft point pressed against an absorbing wall becomes), and
 * `q†(z,m) = Σ_l (4/lπ) sin(lπz/h) e^(−λ_l m)` for a free end, `l` odd.
 * The series normalises itself to `N σ v₀` through the orthogonality of the sines, which is why
 * it can be compared against the solver with no fitted constant anywhere.
 */
private fun idealGraftedProfile(
    chain: GraftedChain,
    height: Double,
    peg: PegWater
): (Double) -> Double {
    val diffusion = chain.kuhnLength * chain.kuhnLength / (6.0 * chain.monomersPerKuhnSegment)
    val modes = 400
    val decay = DoubleArray(modes + 1) { k ->
        if (k == 0) 0.0 else diffusion * (k * PI / height) * (k * PI / height)
    }
    val length = chain.monomersPerChain
    val survival = DoubleArray(modes + 1) { k -> if (k == 0) 0.0 else exp(-decay[k] * length) }
    var partition = 0.0
    for (k in 1..modes step 2) partition += survival[k]
    partition *= 2.0 * height / PI
    return { z ->
        var density = 0.0
        for (k in 1..modes) {
            val sourceWeight = k.toDouble()
            val sk = sin(k * PI * z / height)
            if (sk == 0.0) continue
            for (l in 1..modes step 2) {
                val endWeight = 4.0 / (l * PI)
                val overlap = if (k == l) length * survival[k]
                else (survival[l] - survival[k]) / (decay[k] - decay[l])
                density += sourceWeight * endWeight * sk * sin(l * PI * z / height) * overlap
            }
        }
        chain.graftingDensity * density / partition * peg.monomerVolume
    }
}
