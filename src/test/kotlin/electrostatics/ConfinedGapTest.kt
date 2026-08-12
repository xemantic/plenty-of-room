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
import kotlin.test.Test

/**
 * The confined gap and the electrode — the two places where the *mean-field* answer
 * itself, before any correlation correction, is not what a bulk-buffer intuition expects.
 *
 * Everything here is Poisson-Boltzmann. That is deliberate: `T-6` has to separate
 * "PB says something surprising" from "PB is wrong", and they are different findings.
 */
class ConfinedGapTest {

    private val lb = bjerrumLength()
    private val tile = DnaOrigamiTile()
    private val buffer = MagnesiumChlorideBuffer(2.0)

    // gate 1 — dimensional consistency

    @Test
    fun `gate 1 should convert a Stern capacitance into a surface charge density per volt`() {
        // 20 uF/cm^2 = 0.2 C/(V m^2) = 1.25 e/(V nm^2). Pure unit algebra, no physics.
        assert(sternChargeDensityPerVolt(20.0).isCloseTo(1.24837, relativeTolerance = 1e-4))
        assert((sternChargeDensityPerVolt(40.0) / sternChargeDensityPerVolt(20.0)).isCloseTo(2.0, 1e-12))
    }

    @Test
    fun `gate 1 should derive the close packed density of a hydrated ion`() {
        // fcc packing fraction 0.74 over the hydrated sphere volume.
        assert(closePackedNumberDensity(0.428).isCloseTo(2.25473, relativeTolerance = 1e-4))
        assert(closePackedNumberDensity(0.332).isCloseTo(4.83072, relativeTolerance = 1e-4))
        // and it is an inverse cube in the radius
        assert((closePackedNumberDensity(0.2) / closePackedNumberDensity(0.4)).isCloseTo(8.0, 1e-12))
    }

    // gate 2 — limiting cases

    @Test
    fun `gate 2 should lower the steric saturation potential for a higher valency counterion`() {
        // psi_max = (k_BT/ze) ln(n_max/n_bulk): both the 1/z prefactor and the larger hydrated
        // radius of Mg2+ push the divalent threshold below the monovalent one.
        val magnesium = stericSaturationPotential(
            valency = 2, bulkNumberDensity = buffer.magnesiumNumberDensity, ionRadius = 0.428
        )
        val chloride = stericSaturationPotential(
            valency = 1, bulkNumberDensity = buffer.chlorideNumberDensity, ionRadius = 0.332
        )
        assert(magnesium < chloride)
        assert(magnesium.isCloseTo(0.097371, relativeTolerance = 1e-3))
        assert(chloride.isCloseTo(0.196626, relativeTolerance = 1e-3))
    }

    @Test
    fun `gate 2 should raise the steric saturation potential only logarithmically with dilution`() {
        // a five-fold dilution buys 42 mV. There is no buffer in the §3 range that rescues
        // point-ion PB at a volt, and the logarithm is why.
        val dilute = stericSaturationPotential(1, MagnesiumChlorideBuffer(2.0).chlorideNumberDensity, 0.332)
        val concentrated = stericSaturationPotential(1, MagnesiumChlorideBuffer(10.0).chlorideNumberDensity, 0.332)
        assert(dilute > concentrated)
        assert(abs(dilute - concentrated) < 0.05)
    }

    @Test
    fun `gate 2 should saturate the far-field effective surface charge independently of the bare one`() {
        // Gouy-Chapman charge saturation: with gamma = tanh(z e psi_0 / 4 k_BT) -> 1, the
        // far field of ANY sufficiently charged wall is that of sigma_eff = kappa/(pi l_B z).
        // At 2 mM with divalent counterions that is 0.0568 e/nm^2 — 118x below the tile's
        // bare projected charge and 14x below its Manning-renormalised one.
        val saturated = saturatedEffectiveChargeDensity(buffer.inverseDebyeLength(), 2, lb)
        assert(saturated.isCloseTo(0.0567557, relativeTolerance = 1e-4))
        assert(saturated < tile.projectedChargeDensity / 100.0)
        // it grows with salt, not with the surface charge
        val salty = saturatedEffectiveChargeDensity(MagnesiumChlorideBuffer(10.0).inverseDebyeLength(), 2, lb)
        assert((salty / saturated).isCloseTo(kotlin.math.sqrt(5.0), relativeTolerance = 1e-12))
    }

    @Test
    fun `gate 2 should cap the saturated surface potential at four thermal volts over the valency`() {
        assert(saturatedSurfacePotential(1).isCloseTo(0.103408, relativeTolerance = 1e-4))
        assert(saturatedSurfacePotential(2).isCloseTo(0.051704, relativeTolerance = 1e-4))
    }

    // gate 3 — charge conservation in the gap

    @Test
    fun `gate 3 should require far more counterions in the gap than the bulk buffer supplies`() {
        // Electroneutrality, counted rather than assumed. The tile's Manning-renormalised
        // charge facing the gap needs 319 Mg2+ in a 5 nm gap under a 40x40 nm tile;
        // 2 mM of bulk buffer puts 9.6 there. The gap is counterion-dominated by 33x.
        val gap = CounterionDominatedGap(
            tile = tile, buffer = buffer, gapHeight = 5.0,
            counterionValency = 2, chargeFraction = tile.manningSurvivingFraction(2, lb)
        )
        assert(gap.counterionsRequired.isCloseTo(318.94, relativeTolerance = 1e-3))
        assert(gap.bulkCounterionsAvailable.isCloseTo(9.6354, relativeTolerance = 1e-3))
        assert(gap.dominanceRatio.isCloseTo(33.101, relativeTolerance = 1e-3))
        assert(gap.dominanceRatio > 1.0)
    }

    @Test
    fun `gate 3 should stay counterion dominated across the whole section 3 buffer and height range`() {
        // If this ever fell below 1 the salt-free Netz criteria would be the wrong tool.
        // It does not: the smallest dominance in the §3 box is at 10 mM and 10 nm, and is 3.3.
        listOf(2.0, 5.0, 10.0).forEach { molarity ->
            listOf(5.0, 7.0, 10.0).forEach { height ->
                val gap = CounterionDominatedGap(
                    tile, MagnesiumChlorideBuffer(molarity), height, 2,
                    tile.manningSurvivingFraction(2, lb)
                )
                assert(gap.dominanceRatio > 3.0)
            }
        }
        val worst = CounterionDominatedGap(
            tile, MagnesiumChlorideBuffer(10.0), 10.0, 2, tile.manningSurvivingFraction(2, lb)
        )
        assert(worst.dominanceRatio.isCloseTo(3.3101, relativeTolerance = 1e-3))
    }

    @Test
    fun `gate 3 should make the counterion population independent of the gap height`() {
        // The number of counterions is set by the tile's charge, not by the volume they
        // occupy — which is exactly why the dominance ratio falls as 1/h while the required
        // count stays put. Charge conservation, stated as an invariance.
        val short = CounterionDominatedGap(tile, buffer, 5.0, 2, 1.0)
        val tall = CounterionDominatedGap(tile, buffer, 10.0, 2, 1.0)
        assert(short.counterionsRequired.isCloseTo(tall.counterionsRequired, relativeTolerance = 1e-12))
        assert((short.dominanceRatio / tall.dominanceRatio).isCloseTo(2.0, relativeTolerance = 1e-12))
    }

    // gate 5 — the numbers that go downstream

    @Test
    fun `gate 5 should shorten the local screening length in the gap far below the bulk Debye length`() {
        // §3 quotes "Debye length ~4 nm at 2 mM Mg2+". That is the BULK value. The
        // counterion population the tile's own charge drags into the gap screens on
        // 0.84-1.18 nm instead — a factor of 3-5 shorter. T-3 must not use exp(-h/4 nm).
        listOf(5.0 to 0.83585, 7.0 to 0.98876, 10.0 to 1.18180).forEach { (height, expected) ->
            val gap = CounterionDominatedGap(
                tile, buffer, height, 2, tile.manningSurvivingFraction(2, lb)
            )
            assert(gap.localScreeningLength(lb).isCloseTo(expected, relativeTolerance = 1e-3))
            assert(gap.localScreeningLength(lb) < buffer.debyeLength() / 3.0)
        }
    }

    @Test
    fun `gate 5 should overlap the double layers across the whole working range at 2 mM`() {
        // kappa*h between 1.27 and 2.55 at 2 mM: the two double layers are not independent,
        // so isolated-surface criteria are being applied outside the geometry they were
        // derived for. At 10 mM the gap opens to kappa*h = 2.8-5.7 and the overlap relaxes.
        assert((5.0 / buffer.debyeLength()).isCloseTo(1.27321, relativeTolerance = 1e-4))
        assert((10.0 / buffer.debyeLength()).isCloseTo(2.54643, relativeTolerance = 1e-4))
        assert((5.0 / MagnesiumChlorideBuffer(10.0).debyeLength()).isCloseTo(2.84702, relativeTolerance = 1e-4))
    }

    @Test
    fun `gate 5 should break point-ion PB at the electrode an order of magnitude below the target bias`() {
        // §3's target is <= 2 V. Point-ion PB at a positive electrode in 2 mM MgCl2 stops
        // being physical at 0.197 V of diffuse-layer drop — a factor of 10 below.
        // Above that the compact layer carries the potential and Gouy-Chapman is not the
        // model that sets the electrode charge.
        val threshold = stericSaturationPotential(1, buffer.chlorideNumberDensity, 0.332)
        assert(2.0 / threshold > 10.0)
        // the Stern-limited charge that replaces it: ~1.25 e/nm^2 per volt at 20 uF/cm^2
        assert(sternChargeDensityPerVolt(20.0).isCloseTo(1.24837, relativeTolerance = 1e-4))
    }

    @Test
    fun `gate 5 should exceed the steric limit at the DNA surface even at zero applied bias`() {
        // The contact-value theorem is exact, so this is not a mean-field artefact:
        // 2 pi l_B sigma^2 = 3.93 /nm^3 = 6.53 M of Mg2+ at contact, against a close-packed
        // hydrated limit of 2.25 /nm^3 = 3.74 M. The tile's own charge, with no bias at all,
        // already puts PB's point-ion assumption 1.75x past physical possibility.
        val contact = ChargedSurface(tile.duplexSurfaceChargeDensity, 2).contactDensity(lb)
        val limit = closePackedNumberDensity(0.428)
        assert(contact.isCloseTo(3.93269, relativeTolerance = 1e-4))
        assert((contact / limit).isCloseTo(1.74524, relativeTolerance = 1e-3))
        assert(contact > limit)
        // the hard-core-corrected surface, where the hydrated ion can actually sit, is just under
        val corrected = ChargedSurface(tile.hardCoreSurfaceChargeDensity(0.428), 2).contactDensity(lb)
        assert(corrected / limit < 1.0)
    }
}
