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

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.Test

/**
 * `T-50` gate tests for the beyond-mean-field arithmetic.
 *
 * The whole task rests on one identity — that at a force-pinned operating point a multiplier on
 * `|F_es|` enters the stability floor **only** through its logarithmic gradient — so the tests
 * that matter are the ones that pin down the *shape* of that dependence: exactly linear in `g`,
 * exactly zero at `g = 0`, and singular exactly at the unscreened-capacitor limit.
 */
class BeyondMeanFieldGapTest {

    /** `C-0017`'s binding state: 10 nm layer, 2 mM, alexander-box(two-body), held at 7 nm. */
    private val pinnedForce = 143.922
    private val effectiveStiffness = -27.913
    private val decayLength = 2.8619
    private val mandate = 100.0 / 3.0

    @Test
    fun `gate 2 - a vanishing gradient leaves the mean-field floor exactly unmoved`() {
        assert(
            stabilityFloorUnderGradient(effectiveStiffness, pinnedForce, 0.0)
                .isCloseTo(27.913)
        )
    }

    @Test
    fun `gate 1 - the gradient term has the units of a stiffness`() {
        // |F| in pN times g in nm^-1 is pN/nm, so a 0.01 nm^-1 gradient on 143.922 pN is 1.43922
        assert(
            (effectiveStiffnessUnderGradient(effectiveStiffness, pinnedForce, 0.01) - effectiveStiffness)
                .isCloseTo(1.43922)
        )
    }

    @Test
    fun `the floor is exactly linear in the gradient while it stays positive`() {
        val a = stabilityFloorUnderGradient(effectiveStiffness, pinnedForce, -0.01)
        val b = stabilityFloorUnderGradient(effectiveStiffness, pinnedForce, 0.0)
        val c = stabilityFloorUnderGradient(effectiveStiffness, pinnedForce, 0.01)
        assert((a + c).isCloseTo(2.0 * b))
    }

    @Test
    fun `the threshold gradient is exactly the gradient at which the floor equals the mandate`() {
        val threshold = thresholdGradient(effectiveStiffness, pinnedForce, mandate)
        assert(threshold < 0.0)
        assert(
            stabilityFloorUnderGradient(effectiveStiffness, pinnedForce, threshold)
                .isCloseTo(mandate)
        )
    }

    @Test
    fun `a favourable gradient lowers the floor and an adverse one raises it`() {
        assert(
            stabilityFloorUnderGradient(effectiveStiffness, pinnedForce, 0.02) <
                stabilityFloorUnderGradient(effectiveStiffness, pinnedForce, 0.0)
        )
        assert(
            stabilityFloorUnderGradient(effectiveStiffness, pinnedForce, -0.02) >
                stabilityFloorUnderGradient(effectiveStiffness, pinnedForce, 0.0)
        )
    }

    @Test
    fun `a large enough favourable gradient removes the floor entirely`() {
        assert(
            stabilityFloorUnderGradient(effectiveStiffness, pinnedForce, 1.0)
                .isCloseTo(0.0)
        )
    }

    @Test
    fun `gate 2 - a zero gradient leaves the decay length unchanged`() {
        assert(decayLengthUnderGradient(decayLength, 0.0)!!.isCloseTo(decayLength))
    }

    @Test
    fun `the threshold gradient corresponds to a shorter decay length`() {
        val threshold = thresholdGradient(effectiveStiffness, pinnedForce, mandate)
        val required = decayLengthUnderGradient(decayLength, threshold)!!
        assert(required < decayLength)
        assert((required / decayLength).isCloseTo(0.902702, 1e-5))
    }

    @Test
    fun `gate 2 - the unscreened capacitor limit is exactly where the decay length diverges`() {
        val ceiling = unscreenedGradientCeiling(decayLength)
        assert(ceiling.isCloseTo(1.0 / decayLength))
        assert(decayLengthUnderGradient(decayLength, ceiling) == null)
        assert(decayLengthUnderGradient(decayLength, ceiling * 0.999999)!! > 1.0e5)
    }

    @Test
    fun `gate 2 - the MSA screening parameter reduces to the Debye one for point ions`() {
        val kappa = 0.2546
        assert(msaInverseScreeningLength(kappa, 0.0).isCloseTo(kappa))
        assert(msaInverseScreeningLength(kappa, 1e-9).isCloseTo(kappa, 1e-9))
        assert(msaInverseScreeningLength(kappa, 1e-16).isCloseTo(kappa, 1e-15))
    }

    @Test
    fun `finite ion size always lengthens the screening length`() {
        val kappa = 0.2546
        var previous = kappa
        for (diameter in listOf(0.1, 0.3, 0.6, 0.9)) {
            val value = msaInverseScreeningLength(kappa, diameter)
            assert(value < previous)
            previous = value
        }
    }

    @Test
    fun `the MSA screening parameter satisfies its own defining relation`() {
        // 2G = k/(1 + G s) is the relation the closed form solves; assert the closed form, not it.
        val kappa = 0.2546
        val diameter = 0.6
        val twoGamma = msaInverseScreeningLength(kappa, diameter)
        val gamma = twoGamma / 2.0
        assert((2.0 * gamma * (1.0 + gamma * diameter)).isCloseTo(kappa))
    }

    @Test
    fun `gate 2 - Bjerrum association vanishes when the contact distance exceeds the critical one`() {
        val lb = 0.7141066106764419
        val critical = bjerrumCriticalSeparation(lb, 2)
        assert(critical.isCloseTo(lb))
        assert(bjerrumAssociationVolume(lb, 2, critical).isCloseTo(0.0))
        assert(bjerrumAssociationVolume(lb, 2, critical * 1.001).isCloseTo(0.0))
        assert(bjerrumAssociationVolume(lb, 2, critical * 0.999) > 0.0)
    }

    @Test
    fun `the Bjerrum association volume grows as the contact distance shrinks`() {
        val lb = 0.7141066106764419
        var previous = 0.0
        for (contact in listOf(0.6, 0.5, 0.4, 0.3)) {
            val value = bjerrumAssociationVolume(lb, 2, contact)
            assert(value > previous)
            previous = value
        }
    }

    @Test
    fun `gate 4 - the Bjerrum quadrature is converged in its own panel count`() {
        val lb = 0.7141066106764419
        val coarse = bjerrumAssociationVolume(lb, 2, 0.3, panels = 250)
        val fine = bjerrumAssociationVolume(lb, 2, 0.3, panels = 1000)
        val finer = bjerrumAssociationVolume(lb, 2, 0.3, panels = 4000)
        assert(abs(fine - finer) < abs(coarse - fine))
        assert(abs(finer - fine) / finer < 1e-9)
    }

    @Test
    fun `gate 2 - no association volume means no paired fraction`() {
        assert(pairedMagnesiumFraction(1.204e-3, 0.0).isCloseTo(0.0))
    }

    @Test
    fun `the paired fraction satisfies its own mass action law`() {
        val density = 1.204e-3
        val volume = 20.0
        val alpha = pairedMagnesiumFraction(density, volume)
        assert(alpha > 0.0)
        assert(alpha < 1.0)
        val paired = alpha * density
        assert((paired / ((density - paired) * (2.0 * density - paired))).isCloseTo(volume, 1e-8))
    }

    @Test
    fun `gate 3 - the unassociated ionic strength is exactly three times the concentration`() {
        assert(associatedIonicStrength(2.0, 0.0).isCloseTo(6.0))
        assert(associatedIonicStrength(2.0, 1.0).isCloseTo(2.0))
    }

    @Test
    fun `association lengthens the screening length, so its gradient contribution is favourable`() {
        val kappa = 0.2546
        val paired = associatedIonicStrength(2.0, 0.2)
        val unpaired = associatedIonicStrength(2.0, 0.0)
        val corrected = kappa * sqrt(paired / unpaired)
        assert(corrected < kappa)
        assert((kappa - corrected) > 0.0)
    }

    @Test
    fun `gate 5 - the dressed-ion second virial coefficient is a pure number in the valency`() {
        assert(dressedIonSecondVirialCoefficient(1).isCloseTo(0.25))
        assert(dressedIonSecondVirialCoefficient(2).isCloseTo(1.0 / 3.0))
        assert(dressedIonSecondVirialCoefficient(3).isCloseTo(0.375))
        assert(dressedIonSecondVirialCoefficient(4).isCloseTo(0.4))
    }

    @Test
    fun `gate 5 - the exponential integral reproduces its tabulated values`() {
        assert(exponentialIntegralEi(1.0).isCloseTo(1.8951178163559368, 1e-12))
        assert(exponentialIntegralEi(2.0).isCloseTo(4.954234356001890, 1e-12))
        assert(exponentialIntegralEi(0.5).isCloseTo(0.4542199048631736, 1e-12))
    }

    @Test
    fun `Ei is not E1, and confusing them is a factor of five here`() {
        assert(!exponentialIntegralEi(1.0).isCloseTo(exponentialIntegralE1(1.0), 1e-3))
    }

    @Test
    fun `gate 4 - the exponential integral series is converged in its own term count`() {
        val coarse = exponentialIntegralEi(2.0, terms = 20)
        val fine = exponentialIntegralEi(2.0, terms = 60)
        val finer = exponentialIntegralEi(2.0, terms = 400)
        assert(abs(fine - finer) < abs(coarse - finer) + 1e-18)
        assert(abs(fine - finer) / finer < 1e-14)
    }

    @Test
    fun `the dressed-ion validity constant grows exponentially as the wall charge rises`() {
        // A smaller reduced Gouy-Chapman length is a more strongly charged wall.
        val loose = dressedIonValidityConstant(0.5)
        val tight = dressedIonValidityConstant(0.0303)
        assert(loose > 1.0)
        assert(tight > 1e10)
        assert(loose.isCloseTo(16.26, 1e-3))
    }

    @Test
    fun `gate 5 - the weak-coupling criterion rises with the reduced separation`() {
        val near = weakCouplingValidityCoupling(20.0)
        val far = weakCouplingValidityCoupling(60.0)
        assert(far > near)
        // D/mu = 7 nm / 0.119 nm = 58.8 at the bare duplex wall: the bound is about 14.4
        assert(weakCouplingValidityCoupling(58.8).isCloseTo(14.43, 1e-3))
    }

    @Test
    fun `gate 5 - the limiting-law window and the Kirkwood crossover bracket the device`() {
        // 2 mM MgCl2, hydrated Mg2+ diameter: the reduced diameter is inside the window where
        // four independent methods agree the bulk decay length IS the Debye length.
        val kappa = MagnesiumChlorideBuffer(2.0).inverseDebyeLength()
        val reduced = kappa * 2.0 * HYDRATED_MAGNESIUM_RADIUS
        assert(reduced < LIMITING_LAW_REDUCED_DIAMETER)
        assert(reduced < KIRKWOOD_REDUCED_DIAMETER)
        assert(LIMITING_LAW_REDUCED_DIAMETER < KIRKWOOD_REDUCED_DIAMETER)
    }
}
