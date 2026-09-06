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

package com.xemantic.nano.plentyofroom

import com.xemantic.kotlin.test.assert
import com.xemantic.kotlin.test.have
import com.xemantic.kotlin.test.should
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * The locked unit system, and the two conversions everything downstream depends on.
 *
 * The problem definition fixes `k_BT = 4.142 pN·nm` at 300 K and asks for energies
 * in both `k_BT` and eV, so both routes are pinned here rather than in each study.
 */
class PhysicsTest {

    @Test
    fun `should reproduce the locked thermal energy at 300 K`() {
        // the value the problem definition and the NDI V&V matrix both quote
        assert(thermalEnergy(300.0).isCloseTo(4.142, relativeTolerance = 1e-4))
    }

    @Test
    fun `should express the thermal energy at 300 K in electronvolts`() {
        // 25.85 meV, the standard room-temperature figure
        assert((thermalEnergy(300.0) / ELECTRON_VOLT).isCloseTo(0.025852, relativeTolerance = 1e-4))
    }

    @Test
    fun `should default the thermal energy to room temperature`() {
        assert(thermalEnergy() == thermalEnergy(ROOM_TEMPERATURE))
    }

    @Test
    fun `should scale the thermal energy linearly with temperature`() {
        assert(thermalEnergy(600.0).isCloseTo(2.0 * thermalEnergy(300.0)))
    }

    @Test
    fun `should not return the thermal energy of a non-positive temperature`() {
        assertFailsWith<IllegalArgumentException> {
            thermalEnergy(0.0)
        } should {
            have(message == "temperature must be positive, was: 0.0")
        }
    }

    /**
     * Cross-check against leaf `A1.1` of `../simulation-task-map`, whose acceptance
     * predicate reads: "sigma=3 nm -> k>=~0.46 pN/nm; sigma=0.1 nm (prize) -> k>=~414 pN/nm".
     */
    @Test
    fun `should reproduce the A1_1 equipartition stiffness bounds`() {
        assert(equipartitionStiffness(positionalRms = 3.0).isCloseTo(0.46, relativeTolerance = 1e-2))
        assert(equipartitionStiffness(positionalRms = 0.1).isCloseTo(414.0, relativeTolerance = 1e-2))
    }

    @Test
    fun `should invert equipartition between stiffness and positional RMS`() {
        val stiffness = 12.5
        assert(equipartitionStiffness(equipartitionRms(stiffness)).isCloseTo(stiffness))
    }

    @Test
    fun `should not return the positional RMS of a non-positive stiffness`() {
        assertFailsWith<IllegalArgumentException> {
            equipartitionRms(0.0)
        } should {
            have(message == "stiffness must be positive, was: 0.0")
        }
    }

}
