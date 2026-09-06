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

import com.xemantic.kotlin.test.assert
import com.xemantic.kotlin.test.have
import com.xemantic.kotlin.test.should
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * The measured osmotic equation of state of PEG in water, and the five verification gates on it.
 *
 * This is the object that replaces the undecided `{9/4, 2, 3}` exponent spread `C-0001` carried:
 * a one-parameter interpolation fitted to osmometry on twelve PEG molecular weights,
 * whose *local* exponent is a computable number at any volume fraction rather than a choice.
 */
class OsmoticEquationOfStateTest {

    /** PEG-8800, the chain length of the surviving `T-1` design window. */
    private val peg = ScalingEquationOfState(
        crossoverIndex = 0.49,
        monomerVolume = 0.0603502,
        monomersPerChain = 199.44
    )

    @Test
    fun `should reduce the pressure scale to one thermal energy per monomer volume`() {
        // gate 1, dimensional consistency: k_BT / v0 has units of pressure,
        // and in the locked units it is pN/nm^2, which is exactly MPa.
        assert(peg.pressureScale.isCloseTo(thermalEnergy() / 0.0603502))
        assert(peg.pressureScale.isCloseTo(68.632, relativeTolerance = 1e-4))
    }

    @Test
    fun `should sum exactly to its two limbs`() {
        // the equation of state is a non-virial linear combination and nothing else;
        // there is no third term hiding in the implementation
        val phi = 0.03
        assert(
            peg.pressure(phi).isCloseTo(
                peg.vanTHoffPressure(phi) + peg.desCloizeauxPressure(phi)
            )
        )
    }

    @Test
    fun `should approach the van't Hoff limit in the dilute limit`() {
        // gate 2, limiting case: as the solution is diluted, the osmotic pressure
        // becomes that of an ideal gas of whole chains
        val dilute = 1e-6 * peg.crossoverVolumeFraction
        assert((peg.pressure(dilute) / peg.vanTHoffPressure(dilute)).isCloseTo(1.0, 1e-6))
    }

    @Test
    fun `should approach the des Cloizeaux limit in the concentrated limit`() {
        // gate 2, limiting case: deep in the semidilute regime the pressure forgets
        // the chains entirely and depends on monomer density alone.
        // A volume fraction cannot exceed 1, so the limit is approached by lengthening
        // the chain rather than by concentrating past the physical range.
        val long = peg.copy(monomersPerChain = 1e6)
        assert((long.pressure(1.0) / long.desCloizeauxPressure(1.0)).isCloseTo(1.0, 1e-4))
    }

    @Test
    fun `should carry the local exponent from one to nine quarters across the crossover`() {
        // gate 2, limiting cases on the derivative rather than on the value:
        // van't Hoff is linear in the volume fraction, des Cloizeaux is 9/4
        assert(peg.localExponent(1e-8 * peg.crossoverVolumeFraction).isCloseTo(1.0, 1e-6))
        assert(peg.copy(monomersPerChain = 1e6).localExponent(1.0).isCloseTo(2.25, 1e-5))
    }

    @Test
    fun `should increase the local exponent monotonically with the volume fraction`() {
        // there is no re-entrant softening in this equation of state; a layer that is
        // squeezed only ever stiffens. Downstream stability analysis (T-4) depends on this.
        val exponents = (0..40).map { peg.localExponent(1e-6 * 1.4.pow(it.toDouble())) }
        assert(exponents.zipWithNext().all { (low, high) -> high > low })
    }

    @Test
    fun `should give the local exponent exactly thirteen eighths at the crossover`() {
        // gate 3, an exact symmetry rather than a numeric coincidence: at phi# the two limbs
        // are equal by construction, so the limb ratio is 1 and m = 1 + (5/4)(1/2) = 13/8.
        // It holds for every chain length and every material, which is what makes it a check
        // on the implementation rather than on the fit.
        assert(peg.localExponent(peg.crossoverVolumeFraction).isCloseTo(13.0 / 8.0))
        val other = peg.copy(monomersPerChain = 45.0, crossoverIndex = 0.162)
        assert(other.localExponent(other.crossoverVolumeFraction).isCloseTo(13.0 / 8.0))
    }

    @Test
    fun `should make the des Cloizeaux limb independent of chain length`() {
        // gate 3, symmetry: this molecular-weight independence *is* des Cloizeaux's result.
        // If the implementation broke it, the semidilute limb would not be des Cloizeaux at all.
        val short = peg.copy(monomersPerChain = 45.0)
        val long = peg.copy(monomersPerChain = 20000.0)
        assert(short.desCloizeauxPressure(0.2).isCloseTo(long.desCloizeauxPressure(0.2)))
    }

    @Test
    fun `should make the van't Hoff limb inversely proportional to chain length`() {
        // gate 3, symmetry: the dilute limb counts chains, so doubling the chain length
        // at fixed monomer content must halve the pressure
        val short = peg.copy(monomersPerChain = 100.0)
        val long = peg.copy(monomersPerChain = 200.0)
        assert(short.vanTHoffPressure(0.01).isCloseTo(2.0 * long.vanTHoffPressure(0.01)))
    }

    @Test
    fun `should locate the crossover where the two limbs cross`() {
        // gate 4, numerical convergence: the closed form phi# = (alpha*N)^(-4/5) is checked
        // against a bisection on the actual limbs rather than being asserted against itself
        var low = 1e-9
        var high = 1.0
        repeat(200) {
            val middle = 0.5 * (low + high)
            if (peg.desCloizeauxPressure(middle) < peg.vanTHoffPressure(middle)) low = middle
            else high = middle
        }
        assert(peg.crossoverVolumeFraction.isCloseTo(0.5 * (low + high), relativeTolerance = 1e-9))
    }

    @Test
    fun `should place the crossover at the reduced concentration the fit reports`() {
        // gate 5, literature cross-check: Cohen et al. (2009) report the crossover at
        // C/C*_N = 1.78 +/- 0.03 for PEG/water, which is alpha^(-4/5) with alpha = 0.49.
        // C*_N is the overlap scale N^(-4/5), so this is a check on the ratio alone.
        val overlapScale = peg.monomersPerChain.pow(-4.0 / 5.0)
        assert((peg.crossoverVolumeFraction / overlapScale).isCloseTo(1.78, relativeTolerance = 6e-3))
    }

    @Test
    fun `should agree with the independent brush-derived fit of the same material`() {
        // gate 5, and acceptance predicate (c) of the task: two independent fits of PEG/water,
        // with different volume-fraction conventions and different data reductions, must agree.
        //
        //   Cohen et al. (2009):  Pi = alpha * (k_BT/v0) * phi^(9/4),  alpha = 0.49,
        //                         phi = Vbar * C, Vbar = 0.825 mL/g
        //   Hansen et al. (2003): Pi = 0.8 * (k_BT/a^3) * phi_H^(9/4), a = 0.35 nm,
        //                         phi_H = n * a^3 = 0.586 * w
        //
        // Both are evaluated here at the same physical weight fraction w, in the des Cloizeaux
        // limb where both are asserted to hold, and compared. Acceptance is 10%.
        val weightFraction = 0.2
        val cohen = peg.desCloizeauxPressure(0.825 * weightFraction)
        val hansenScale = thermalEnergy() / 0.35.pow(3.0)
        val hansen = 0.8 * hansenScale * (0.58614 * weightFraction).pow(9.0 / 4.0)
        assert(abs(cohen - hansen) / hansen < 0.10)
        // and, so that a regression is visible rather than merely inside the band:
        assert((cohen / hansen).isCloseTo(0.939, relativeTolerance = 5e-3))
    }

    @Test
    fun `should converge the local exponent to its closed form under mesh refinement`() {
        // gate 4, numerical convergence: the closed-form logarithmic derivative is checked
        // against a central difference in log space, and the error must fall as the step shrinks
        val phi = 0.03
        val closedForm = peg.localExponent(phi)
        fun finiteDifference(step: Double): Double =
            (ln(peg.pressure(phi * (1.0 + step))) - ln(peg.pressure(phi * (1.0 - step)))) /
                    (ln(1.0 + step) - ln(1.0 - step))
        val coarse = abs(finiteDifference(1e-2) - closedForm)
        val fine = abs(finiteDifference(1e-3) - closedForm)
        assert(fine < coarse / 50.0)
        assert(fine < 1e-6)
    }

    @Test
    fun `should classify the three domains by the width the fit reports`() {
        // Cohen et al. (2009) give the crossover width explicitly: the van't Hoff domain is
        // phi <~ 0.2 phi#, the des Cloizeaux domain phi >~ 5 phi#, and the 25-fold range
        // between them is neither. That is a measured width, not a convention of ours.
        val crossover = peg.crossoverVolumeFraction
        assert(peg.regime(0.1 * crossover) == SolutionRegime.VAN_T_HOFF)
        assert(peg.regime(crossover) == SolutionRegime.CROSSOVER)
        assert(peg.regime(2.0 * crossover) == SolutionRegime.CROSSOVER)
        assert(peg.regime(10.0 * crossover) == SolutionRegime.DES_CLOIZEAUX)
    }

    @Test
    fun `should put the surviving T-1 design window inside the crossover`() {
        // The finding this task exists to establish. At L0 = 10 nm and sigma = 0.024 nm^-2
        // the layer stands at phi = 0.0289 and its crossover sits at phi# = 0.0256:
        // the layer is 1.13 crossover units up, an order of magnitude short of the
        // des Cloizeaux domain it was assumed to be in.
        val phi = 0.0288872
        assert(peg.crossoverVolumeFraction.isCloseTo(0.025585, relativeTolerance = 1e-3))
        assert((phi / peg.crossoverVolumeFraction).isCloseTo(1.1291, relativeTolerance = 1e-3))
        assert(peg.regime(phi) == SolutionRegime.CROSSOVER)
        assert(peg.localExponent(phi).isCloseTo(1.6726, relativeTolerance = 1e-3))
    }

    @Test
    fun `should keep the design window inside the crossover under working compression`() {
        // and it does not escape by being squeezed: at the working height under the
        // 100 pN target force the layer is still only 2.2 crossover units up.
        val working = 0.0288872 * 10.0 / 5.05
        assert((working / peg.crossoverVolumeFraction).isCloseTo(2.236, relativeTolerance = 1e-3))
        assert(peg.regime(working) == SolutionRegime.CROSSOVER)
        assert(peg.localExponent(working).isCloseTo(1.9155, relativeTolerance = 1e-3))
    }

    @Test
    fun `should reject a volume fraction outside the physical range`() {
        assertFailsWith<IllegalArgumentException> {
            peg.pressure(0.0)
        } should {
            have(message == "volumeFraction must be within (0.0, 1.0], was: 0.0")
        }
        assertFailsWith<IllegalArgumentException> {
            peg.pressure(1.5)
        } should {
            have(message == "volumeFraction must be within (0.0, 1.0], was: 1.5")
        }
    }

    @Test
    fun `should reject a non-positive crossover index`() {
        assertFailsWith<IllegalArgumentException> {
            peg.copy(crossoverIndex = 0.0)
        } should {
            have(message == "crossoverIndex must be positive, was: 0.0")
        }
    }

    @Test
    fun `should reject a chain shorter than one monomer`() {
        assertFailsWith<IllegalArgumentException> {
            peg.copy(monomersPerChain = 0.5)
        } should {
            have(message == "monomersPerChain must be at least 1, was: 0.5")
        }
    }

}
