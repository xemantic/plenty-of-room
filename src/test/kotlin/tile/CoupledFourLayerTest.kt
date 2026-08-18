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

package com.xemantic.nano.plentyofroom.tile

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-197` — is a COUPLED four-layer tile flat under the measured staple dropout?
 *
 * The framing these tests pin down is the one the task turns on. `C-0109` reports every coupled
 * cell as worse than the **uncoupled** tile, and that comparison is only decisive if the uncoupled
 * tile is a design the device could have. **It is not**: `C-0017`'s mandate is an *equality* on the
 * SUM of the coupling stiffnesses, because §3 requires the actuator to deliver 100 pN to a load. So
 * the coupling total is fixed and non-zero by specification, and the real question is whether the
 * four-layer tile is flat **at the mandated total** under `C-0087`'s measured dropout.
 */
class CoupledFourLayerTest {

    // --- gate 1: the mandate is a SUM, and that is what makes zero unavailable ------------------

    @Test
    fun `an equal-spring distribution sums to the mandate whatever the path count`() {
        for (paths in listOf(15, 30, 45, 60, 90)) {
            val stiffnesses = equalShareOfMandate(paths, MANDATED_TOTAL_STIFFNESS)
            assert(stiffnesses.size == paths)
            assert(stiffnesses.sum().isCloseTo(MANDATED_TOTAL_STIFFNESS))
        }
    }

    @Test
    fun `the mandate cannot be met by no coupling at all`() {
        assertFailsWith<IllegalArgumentException> { equalShareOfMandate(0, MANDATED_TOTAL_STIFFNESS) }
        // And a zero total is refused rather than silently producing the uncoupled tile: the
        // uncoupled tile is a REFERENCE, never a design, because §3 requires 100 pN to reach a load.
        assertFailsWith<IllegalArgumentException> { equalShareOfMandate(45, 0.0) }
    }

    // --- gate 2: a graded distribution still spends exactly the mandate ------------------------

    @Test
    fun `a rim-graded distribution sums to the same mandate`() {
        val paths = 45
        val graded = rimGradedShareOfMandate(
            weights = List(paths) { if (it % 3 == 0) 5.0 else 1.0 },
            total = MANDATED_TOTAL_STIFFNESS
        )
        assert(graded.size == paths)
        assert(graded.sum().isCloseTo(MANDATED_TOTAL_STIFFNESS))
        // The ratio between the two levels is preserved exactly -- the grading is a redistribution
        // of a fixed budget, which is `C-0017`'s own reading of its mandate.
        assert(abs(graded[0] / graded[1] - 5.0) < 1e-12)
    }

    @Test
    fun `a distribution with a non-positive weight is refused`() {
        assertFailsWith<IllegalArgumentException> {
            rimGradedShareOfMandate(listOf(1.0, 0.0, 1.0), MANDATED_TOTAL_STIFFNESS)
        }
        assertFailsWith<IllegalArgumentException> {
            rimGradedShareOfMandate(listOf(1.0, -1.0), MANDATED_TOTAL_STIFFNESS)
        }
    }

    // --- gate 3: the mandate is the one C-0017 states --------------------------------------------

    @Test
    fun `the mandated total is section 3's 100 pN over its acceptable 3 nm stroke`() {
        assert(MANDATED_TOTAL_STIFFNESS.isCloseTo(100.0 / 3.0))
    }
}
