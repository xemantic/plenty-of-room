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
import com.xemantic.nano.plentyofroom.electrostatics.MagnesiumChlorideBuffer
import com.xemantic.nano.plentyofroom.electrostatics.bjerrumLength
import com.xemantic.nano.plentyofroom.electrostatics.thermalVoltage
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-193` — the electrode's potential of zero charge, and the rational potential the Gen-1
 * gap model is silently parametrised by.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem
 * definition. The load-bearing one is **gate 5**: the measured `E_pzc` values of
 * `GoldPotentialOfZeroCharge` are transcribed from a figure-free passage of one paper, and
 * the paper prints each of them **twice**, on the RHE and the SHE scale. The Nernst
 * conversion between those two scales is therefore an internal identity of the source that
 * a transcription error would break — `CLAUDE.md`'s *"a transcription from a figure is
 * checkable three ways"*, applied to a transcription from prose.
 */
class ElectrodePotentialOfZeroChargeTest {

    // ---------------------------------------------------------------- gate 1

    @Test
    fun `gate 1 dimensional consistency - the Nernst slope is a thermal voltage times ln 10`() {
        assert(nernstSlope().isCloseTo(thermalVoltage() * ln(10.0)))
        // 25.85200 mV x 2.302585 = 59.52643 mV per decade at 300 K
        assert(nernstSlope().isCloseTo(0.05952643, 1e-6))
        // and it is linear in the temperature, exactly
        assert((nernstSlope(600.0) / nernstSlope(300.0)).isCloseTo(2.0))
    }

    @Test
    fun `gate 1 dimensional consistency - a rational potential is a difference of two potentials`() {
        assert(rationalPotential(0.0, 0.497).isCloseTo(-0.497))
        assert(rationalPotential(0.497, 0.497).isCloseTo(0.0))
        assert(rationalPotential(1.0, 0.497).isCloseTo(0.503))
    }

    // ---------------------------------------------------------------- gate 2

    @Test
    fun `gate 2 limiting case - at pH zero the RHE and the SHE are the same electrode`() {
        assert(reversibleHydrogenToStandardHydrogen(0.674, 0.0).isCloseTo(0.674))
        // and one pH unit is exactly one Nernst slope
        assert(
            (reversibleHydrogenToStandardHydrogen(0.674, 0.0) -
                    reversibleHydrogenToStandardHydrogen(0.674, 1.0)).isCloseTo(nernstSlope())
        )
    }

    @Test
    fun `gate 2 limiting case - a strong monoprotic acid has pH equal to minus log of its molarity`() {
        assert(strongAcidPh(1.0e-3).isCloseTo(3.0))
        assert(strongAcidPh(1.0e-2).isCloseTo(2.0))
        assertFailsWith<IllegalArgumentException> { strongAcidPh(0.0) }
    }

    // ---------------------------------------------------------------- gate 3

    @Test
    fun `gate 3 conservation - the electrode at its own PZC carries no rational potential at all`() {
        GoldPotentialOfZeroCharge.readingsVersusStandardHydrogen.forEach { (_, value) ->
            assert(rationalPotential(value, value).isCloseTo(0.0))
        }
    }

    // ---------------------------------------------------------------- gate 5

    @Test
    fun `gate 5 literature cross-check - the source prints each PZC on two scales and Nernst joins them`() {
        // Adnan et al. PCCP 26:21419 (2024), 1 mM HClO4: pH 3.0 on a strong monoprotic acid
        val pH = strongAcidPh(GoldPotentialOfZeroCharge.ELECTROLYTE_MOLARITY)
        assert(pH.isCloseTo(3.0))
        GoldPotentialOfZeroCharge.readingsVersusReversibleHydrogen.forEach { (surface, versusRhe) ->
            val derived = reversibleHydrogenToStandardHydrogen(versusRhe, pH)
            val published = GoldPotentialOfZeroCharge.readingsVersusStandardHydrogen.getValue(surface)
            // the paper's own rounding is to the millivolt and it was taken at 298 K, not 300;
            // 5 mV is the band that leaves, and a mis-transcribed digit is 10x outside it
            assert(abs(derived - published) < 5.0e-3)
        }
    }

    @Test
    fun `gate 5 literature cross-check - an independent paper's Au(111) statement contains the measurement`() {
        // Liu, Doblhoff-Dier & Koper, ACS Electrochem. 2:995 (2026): "E_pzc values in the
        // literature for Au(111) are around 0.5 V vs. SHE ... while for Au(110) they are
        // around 0.2 V vs. SHE"
        GoldPotentialOfZeroCharge.readingsVersusStandardHydrogen.forEach { (_, value) ->
            assert(abs(value - GoldPotentialOfZeroCharge.LITERATURE_AU111_VERSUS_SHE) < 0.05)
        }
        // and the facet spread of a polycrystalline film is not a rounding error
        assert(
            (GoldPotentialOfZeroCharge.LITERATURE_AU111_VERSUS_SHE -
                    GoldPotentialOfZeroCharge.LITERATURE_AU110_VERSUS_SHE) > 0.25
        )
    }

    @Test
    fun `gate 5 literature cross-check - the measured PZC dwarfs C-0021's hold-down thresholds`() {
        // C-0021's contact-potential thresholds, in volt, at 5 / 7 / 10 nm
        val thresholds = listOf(0.000885908166, 0.00184292351, 0.00510177542)
        val pzc = GoldPotentialOfZeroCharge.readingsVersusStandardHydrogen.values
        // an electrode held at 0 V on ANY of the common aqueous scales sits this far from
        // zero charge; the smallest multiple in the box is the 10 nm gap at the lowest PZC
        val smallest = pzc.min() / thresholds.max()
        assert(smallest > 80.0)
        assert(smallest < 100.0)
        // and the largest is the 5 nm gap at the highest PZC
        val largest = pzc.max() / thresholds.min()
        assert(largest > 500.0)
    }

    // ------------------------------------------------------------ gate 5, the screening audit

    @Test
    fun `gate 5 literature cross-check - the buffer's own Debye length is only reached by the DEFAULT call`() {
        val buffer = MagnesiumChlorideBuffer(2.0)
        // CLAUDE.md and MagnesiumChlorideBuffer's own KDoc: 3.93 nm at 2 mM, 300 K, eps_r 78
        assert(buffer.debyeLength().isCloseTo(3.93, 3e-3))
        // C-0021 and C-0023 both write `buffer.inverseDebyeLength(lb)`, and the FIRST parameter
        // of that method is a TEMPERATURE. Passing 0.714 nm as 0.714 K inflates the Bjerrum
        // length by 300/0.714 and kappa by the square root of that.
        val asCalled = buffer.inverseDebyeLength(bjerrumLength())
        assert(asCalled / buffer.inverseDebyeLength() > 20.0)
        assert(asCalled / buffer.inverseDebyeLength() < 21.0)
        // the consequence, and the whole of it: the zero-frequency term is annihilated rather
        // than screened, which lands the low end of the bracket exactly on "fully screened"
        assert(zeroFrequencyScreeningFactor(5.0, asCalled) < 1e-20)
        assert(zeroFrequencyScreeningFactor(5.0, buffer.inverseDebyeLength()) > 0.07)
        assert(zeroFrequencyScreeningFactor(5.0, buffer.inverseDebyeLength()) < 0.08)
    }

    @Test
    fun `gate 2 limiting case - repairing the screening moves the gold low end by under one per cent`() {
        val buffer = MagnesiumChlorideBuffer(2.0)
        val combined = combinedHamakerAcrossWater(
            HamakerConstants.DNA_ACROSS_WATER_LOW, HamakerConstants.GOLD_ACROSS_WATER
        )
        val zero = sqrt(
            HamakerConstants.ZERO_FREQUENCY_TERM_LOW_DIELECTRIC * HamakerConstants.ZERO_FREQUENCY_TERM
        )
        listOf(5.0, 7.0, 10.0).forEach { gap ->
            val published = screenedHamakerConstant(
                zero, combined - zero, gap, buffer.inverseDebyeLength(bjerrumLength())
            )
            val repaired = screenedHamakerConstant(
                zero, combined - zero, gap, buffer.inverseDebyeLength()
            )
            // the pressure is linear in the Hamaker constant, so this IS the force ratio
            assert(repaired > published)
            assert((repaired - published) / published < 0.01)
        }
    }

}
