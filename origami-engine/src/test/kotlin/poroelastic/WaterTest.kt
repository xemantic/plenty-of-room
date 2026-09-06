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

package com.xemantic.nano.plentyofroom.poroelastic

import com.xemantic.kotlin.test.assert
import com.xemantic.kotlin.test.have
import com.xemantic.kotlin.test.should
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * The solvent viscosity every drainage time in `T-7` is proportional to.
 *
 * It is a *cited correlation* evaluated in code rather than a number typed in,
 * because §7 of the problem definition asks for inherited numbers to be re-derived
 * and because the temperature dependence is steep enough (−2.3 %/K near 300 K)
 * that quoting a 20 °C handbook value at 300 K would be a 6 % error.
 */
class WaterTest {

    @Test
    fun `should express the viscosity in the locked pressure-time unit`() {
        // gate 1, dimensional consistency: viscosity is pressure x time, so in the locked
        // units it is pN*s/nm^2, and 1 Pa*s is exactly 1e-6 of it
        assert(PASCAL_SECOND.isCloseTo(1e-6))
        assert((waterViscosity(300.0) / PASCAL_SECOND).isCloseTo(8.5406e-4, 1e-4))
    }

    @Test
    fun `should reproduce the reference viscosity of water at twenty celsius`() {
        // gate 5, literature cross-check: the IAPWS reference value at 20 C is
        // 1.0016 mPa*s, and the correlation must land on it without being fitted to it
        val referenceAt20C = 1.0016e-3
        val predicted = waterViscosity(293.15) / PASCAL_SECOND
        assert(predicted.isCloseTo(referenceAt20C, relativeTolerance = 5e-3))
    }

    @Test
    fun `should fall monotonically with temperature across the liquid range`() {
        // gate 2, limiting cases: water thins as it warms, over the whole validity range
        val viscosities = (0..20).map { waterViscosity(275.0 + it * 4.5) }
        assert(viscosities.zipWithNext().all { (cold, warm) -> warm < cold })
    }

    @Test
    fun `should reject a temperature outside the liquid range of water`() {
        assertFailsWith<IllegalArgumentException> {
            waterViscosity(250.0)
        } should {
            have(message == "temperature must be within [273.15, 373.15] K, was: 250.0")
        }
    }

    @Test
    fun `should convert a mass density into the locked inertial unit`() {
        // gate 1: force = mass x acceleration, so in the locked units mass is pN*s^2/nm.
        // A 40 x 40 x 10 nm origami tile at 1.7 g/cm^3 is 2.72e-17 pN*s^2/nm,
        // which is 2.72e-20 kg — checked against the SI value computed independently.
        val tileMass = 1.7 * 16000.0 * GRAM_PER_CUBIC_CENTIMETRE
        assert(tileMass.isCloseTo(2.72e-17, relativeTolerance = 1e-3))
        assert((tileMass / KILOGRAM).isCloseTo(2.72e-20, relativeTolerance = 1e-3))
    }

}
