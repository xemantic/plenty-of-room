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
import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.test.Test

/**
 * The electrolyte of task `T-6`: the Bjerrum length, the ionic strength of a 2:1 salt,
 * and the Debye length §3 quotes as "~4 nm at 2 mM Mg²⁺" without deriving it.
 */
class ElectrolyteTest {

    // gate 1 — dimensional consistency

    @Test
    fun `gate 1 should derive the Bjerrum length in nm from SI constants alone`() {
        // l_B = e^2 / (4 pi eps0 eps_r k_B T). Nothing here is cited: the four constants
        // are SI definitions and eps_r is the one stated parameter.
        assert(bjerrumLength().isCloseTo(0.71411, relativeTolerance = 1e-4))
    }

    @Test
    fun `gate 1 should make the Bjerrum length the distance at which two unit charges cost one k_BT`() {
        // the defining property, asserted rather than the formula: at z = l_B the Coulomb
        // energy of two elementary charges in the medium equals k_BT exactly.
        val lb = bjerrumLength()
        assert(coulombEnergy(charge1 = 1, charge2 = 1, separation = lb).isCloseTo(1.0))
        assert(coulombEnergy(charge1 = 1, charge2 = 1, separation = 2.0 * lb).isCloseTo(0.5))
        // and a divalent pair at l_B costs four times as much
        assert(coulombEnergy(charge1 = 2, charge2 = 2, separation = lb).isCloseTo(4.0))
    }

    @Test
    fun `gate 1 should derive the thermal voltage as 25 85 mV at 300 K`() {
        assert(thermalVoltage().isCloseTo(0.0258520, relativeTolerance = 1e-5))
    }

    @Test
    fun `gate 1 should convert number density to molarity reversibly`() {
        val perCubicNanometre = millimolarToPerCubicNanometre(2.0)
        assert(perCubicNanometre.isCloseTo(1.20443e-3, relativeTolerance = 1e-4))
        assert(perCubicNanometreToMillimolar(perCubicNanometre).isCloseTo(2.0))
    }

    // gate 2 — limiting cases

    @Test
    fun `gate 2 should scale the Debye length as the inverse square root of concentration`() {
        val two = MagnesiumChlorideBuffer(2.0).debyeLength()
        val eight = MagnesiumChlorideBuffer(8.0).debyeLength()
        assert((two / eight).isCloseTo(2.0, relativeTolerance = 1e-12))
    }

    @Test
    fun `gate 2 should give a 2 to 1 salt three times the ionic strength of its molarity`() {
        // I = 1/2 sum c_i z_i^2 = 1/2 (c*4 + 2c*1) = 3c. The factor of three is the whole
        // reason a divalent buffer screens as hard as it does at low molarity, and it is
        // the first place a monovalent intuition goes wrong.
        assert(MagnesiumChlorideBuffer(2.0).ionicStrength.isCloseTo(6.0))
        assert(MagnesiumChlorideBuffer(10.0).ionicStrength.isCloseTo(30.0))
    }

    @Test
    fun `gate 2 should keep the buffer electroneutral at every concentration`() {
        // gate 3 in substance: charge conservation of the bulk reservoir.
        listOf(2.0, 5.0, 10.0).forEach { concentration ->
            val buffer = MagnesiumChlorideBuffer(concentration)
            assert(
                (2.0 * buffer.magnesiumNumberDensity - buffer.chlorideNumberDensity)
                    .isCloseTo(0.0, relativeTolerance = 1e-18)
            )
        }
    }

    // gate 4 — the identity between the two ways of writing kappa

    @Test
    fun `gate 4 should reproduce the Debye length from the ionic strength in two independent ways`() {
        val buffer = MagnesiumChlorideBuffer(2.0)
        // route A: kappa^2 = 8 pi l_B I_number  (I in number density)
        val routeA = 1.0 / sqrt(
            8.0 * PI * bjerrumLength() * millimolarToPerCubicNanometre(buffer.ionicStrength)
        )
        // route B: kappa^2 = 4 pi l_B sum_i n_i z_i^2, species by species
        val routeB = 1.0 / sqrt(
            4.0 * PI * bjerrumLength() *
                    (buffer.magnesiumNumberDensity * 4.0 + buffer.chlorideNumberDensity)
        )
        assert(routeA.isCloseTo(routeB, relativeTolerance = 1e-12))
        assert(buffer.debyeLength().isCloseTo(routeA, relativeTolerance = 1e-12))
    }

    // gate 5 — literature cross-check

    @Test
    fun `gate 5 should reproduce the Debye length that section 3 quotes at 2 mM Mg2 plus`() {
        // §3 of the problem definition states "~4 nm at 2 mM Mg2+" without derivation.
        // Re-derived at 300 K with eps_r = 78: 3.927 nm, i.e. 1.8% below the quoted 4 nm.
        // The inherited number is therefore CONFIRMED, and it is now derived, not cited.
        val lambda = MagnesiumChlorideBuffer(2.0).debyeLength()
        assert(lambda.isCloseTo(3.9269, relativeTolerance = 1e-3))
        assert(kotlin.math.abs(lambda - 4.0) / 4.0 < 0.02)
    }

    @Test
    fun `gate 5 should emit the whole section 3 buffer sweep`() {
        assert(MagnesiumChlorideBuffer(2.0).debyeLength().isCloseTo(3.9269, relativeTolerance = 1e-3))
        assert(MagnesiumChlorideBuffer(5.0).debyeLength().isCloseTo(2.4836, relativeTolerance = 1e-3))
        assert(MagnesiumChlorideBuffer(10.0).debyeLength().isCloseTo(1.7562, relativeTolerance = 1e-3))
    }

    @Test
    fun `gate 5 should agree with the Bjerrum length Naji et al use for water`() {
        // Naji, Jungblut, Moreira & Netz, Physica A 352:131 (2005), Table I caption:
        // "The Bjerrum length is taken here as l_B = 7.1 A corresponding to an aqueous
        // medium of dielectric constant eps = 80 at room temperature."
        // At eps_r = 80 our derivation gives 0.6963 nm, within 2% of their rounded 0.71 nm;
        // at our stated eps_r = 78 it is 0.7141 nm. Both are reported so the 2.5%
        // dielectric-constant sensitivity is visible rather than buried.
        assert(bjerrumLength(relativePermittivity = 80.0).isCloseTo(0.69625, relativeTolerance = 1e-4))
        assert(bjerrumLength(relativePermittivity = 78.0).isCloseTo(0.71411, relativeTolerance = 1e-4))
    }

    @Test
    fun `gate 2 should raise the Bjerrum length as the medium is made less polar`() {
        // the direction that matters for the polymer layer: lowering eps raises l_B,
        // which raises the coupling parameter as l_B^2 and worsens mean field.
        assert(bjerrumLength(relativePermittivity = 39.0) > bjerrumLength())
        assert(
            bjerrumLength(relativePermittivity = 39.0)
                .isCloseTo(2.0 * bjerrumLength(relativePermittivity = 78.0), relativeTolerance = 1e-12)
        )
    }

    @Test
    fun `gate 1 should reject an unphysical buffer`() {
        listOf(0.0, -1.0).forEach { bad ->
            try {
                MagnesiumChlorideBuffer(bad).debyeLength()
                throw AssertionError("should have rejected concentration $bad")
            } catch (e: IllegalArgumentException) {
                assert(e.message!!.contains("concentration"))
            }
        }
    }

    @Test
    fun `gate 2 should hold the temperature dependence of the Bjerrum length`() {
        // l_B ∝ 1/(eps_r T). Reported because the dielectric constant of water itself
        // falls with temperature, so the two effects compound rather than cancel.
        assert(
            bjerrumLength(temperature = 2.0 * ROOM_TEMPERATURE)
                .isCloseTo(0.5 * bjerrumLength(), relativeTolerance = 1e-12)
        )
    }
}
