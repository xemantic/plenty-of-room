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

package com.xemantic.nano.plentyofroom.anchoring

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.PI
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-12` — the two anchorless branches, as **ceilings with thresholds**, never as designs.
 *
 * `C-0010` names lateral patterning of the grafting as *"a design lever nobody has costed"*,
 * and §1 says the electrode is patterned. Neither can be *solved* here — the first needs a
 * laterally resolved layer free energy and the second is `T-3b`'s 2-D Poisson-Boltzmann
 * solve — so each is closed the way `P-6` closed: the largest value the mechanism can reach,
 * and the value it would have to reach for the answer to change.
 */
class LayerCorrugationTest {

    // ---------------------------------------------------------------- gate 1

    @Test
    fun `gate 1 dimensional consistency - a corrugation stiffness should be an energy over a squared length`() {
        // U/(W h) has units pN.nm/nm^2 = pN/nm
        val ceiling = graftingPadStiffnessCeiling(storedEnergy = 200.0, tileEdge = 40.0, healingLength = 10.0)
        assert(ceiling.isCloseTo(200.0 / 400.0))
        assert(
            graftingPadStiffnessCeiling(400.0, 40.0, 10.0).isCloseTo(2.0 * ceiling)
        )
    }

    @Test
    fun `gate 1 dimensional consistency - the pad threshold should invert its own ceiling`() {
        val energy = graftingPadEnergyThreshold(
            tileEdge = 40.0, healingLength = 10.0, requiredStiffness = 0.460216333
        )
        assert(
            graftingPadStiffnessCeiling(energy, 40.0, 10.0).isCloseTo(0.460216333)
        )
    }

    // ---------------------------------------------------------------- gate 2

    @Test
    fun `gate 2 limiting cases - a corrugation that is long compared with the tile should do nothing`() {
        // the tile averages the pattern over its own footprint, so as the period grows the
        // lateral force vanishes as q^2, and as the period shrinks the gap attenuates it
        // as exp(-(sqrt(kappa^2+q^2) - kappa) z), which is what makes the optimum interior
        val long = patternedElectrodeStiffness(140.0, 1e5, 40.0, 1.0, 10.0)
        val short = patternedElectrodeStiffness(140.0, 1.0, 40.0, 1.0, 10.0)
        val matched = patternedElectrodeStiffness(140.0, 60.0, 40.0, 1.0, 10.0)
        assert(long < 1e-6)
        assert(short < matched)
        assert(matched > long)
    }

    @Test
    fun `gate 2 limiting cases - the form factor should be unity for a point tile and vanish at commensurate periods`() {
        // a tile much smaller than the period samples one phase; a tile exactly one period wide
        // samples the whole pattern and feels nothing at all
        assert(tileFormFactor(period = 1000.0, tileEdge = 1e-6).isCloseTo(1.0, 1e-9))
        assert(tileFormFactor(period = 40.0, tileEdge = 40.0).isCloseTo(0.0))
        assert(tileFormFactor(period = 20.0, tileEdge = 40.0).isCloseTo(0.0))
    }

    @Test
    fun `gate 2 limiting cases - the pad ceiling should vanish with the load the tile carries`() {
        // the corrugation is proportional to the compression energy already stored in the layer,
        // so at zero bias there is no stored energy and no lateral confinement at all
        assert(graftingPadStiffnessCeiling(0.0, 40.0, 10.0).isCloseTo(0.0))
    }

    // ---------------------------------------------------------------- gate 3

    @Test
    fun `gate 3 conservation - the corrugation ceiling should never exceed the energy the tile has`() {
        // the tile cannot gain more by sliding off the pad than the whole energy it stored in
        // the layer, so F <= U/W and k <= U/(W h) are energy conservation, not a fit
        val energy = 250.0
        val force = graftingPadLateralForceCeiling(energy, tileEdge = 40.0)
        assert((force * 40.0).isCloseTo(energy))
        assert(graftingPadStiffnessCeiling(energy, 40.0, 10.0).isCloseTo(force / 10.0))
    }

    // ---------------------------------------------------------------- gate 4

    @Test
    fun `gate 4 numerical convergence - the optimal electrode period should be a true interior maximum`() {
        val best = optimalElectrodePeriod(tileEdge = 40.0, decayLength = 1.0, height = 10.0)
        val peak = patternedElectrodeStiffness(140.0, best, 40.0, 1.0, 10.0)
        // probed only inside the domain the expression is valid on, period >= the tile edge
        listOf(0.7, 0.85, 0.95, 1.05, 1.25, 2.0).forEach { factor ->
            assert(
                patternedElectrodeStiffness(140.0, best * factor, 40.0, 1.0, 10.0)
                        <= peak * (1.0 + 1e-9)
            )
        }
        // and it is of the order of the tile itself, not of the Debye length
        assert(best > 40.0 && best < 200.0)
    }

    // ---------------------------------------------------------------- gate 5

    @Test
    fun `gate 5 literature cross-check - the C-0006 dishing coefficient should be linear in the modulation depth`() {
        // C-0006 demonstrated dishing to be EXACTLY linear in the load non-uniformity:
        // 0.2651 nm at 10 % against 1.3256 nm at 50 %, a ratio of exactly 5. So any lateral
        // corrugation depth converts to a dishing cost by one multiplication.
        assert(dishingFromModulation(0.5).isCloseTo(5.0 * dishingFromModulation(0.1)))
        assert(dishingFromModulation(0.5).isCloseTo(0.268, 2e-3))
    }

    @Test
    fun `gate 5 literature cross-check - the ripple transfer should be exactly one half at two pi bending lengths`() {
        // C-0006 gate 2: "a plate is a low-pass filter, never a band-pass one" — unity at long
        // wavelength, zero at short, and exactly one half at lambda = 2 pi l
        val bendingLength = 4.03
        assert(rippleTransfer(bendingLength, 2.0 * PI * bendingLength).isCloseTo(0.5))
        assert(rippleTransfer(bendingLength, 1e6) > 0.999999)
        assert(rippleTransfer(bendingLength, 0.1) < 1e-6)
        // and at the ~60 nm wavelength a lateral electrode pattern needs, the tile follows it
        assert(rippleTransfer(bendingLength, 60.0) > 0.95)
    }

    // ---------------------------------------------------------------- guards

    @Test
    fun `a negative stored energy should be rejected`() {
        assertFailsWith<IllegalArgumentException> { graftingPadStiffnessCeiling(-1.0, 40.0, 10.0) }
        assertFailsWith<IllegalArgumentException> {
            patternedElectrodeStiffness(140.0, 0.0, 40.0, 1.0, 10.0)
        }
        assertFailsWith<IllegalArgumentException> { dishingFromModulation(1.5) }
    }

    @Test
    fun `the wavevector convention should be two pi over the period`() {
        // at zero height there is no gap attenuation and a point tile has no form factor,
        // so the whole expression reduces to U q^2 with q = 2 pi / period
        val period = 60.0
        val wavevector = 2.0 * PI / period
        assert(
            patternedElectrodeStiffness(1.0, period, 1e-9, 1.0, 0.0)
                .isCloseTo(wavevector * wavevector, 1e-6)
        )
    }
}
