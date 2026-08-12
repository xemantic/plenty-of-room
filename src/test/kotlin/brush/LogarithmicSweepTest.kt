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
import com.xemantic.kotlin.test.have
import com.xemantic.kotlin.test.should
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * The grafting density is swept logarithmically because it is the free variable of §4(a)
 * and it spans more than two decades between the mushroom boundary and antifouling grade;
 * a linear sweep would spend most of its samples where nothing happens.
 */
class LogarithmicSweepTest {

    @Test
    fun `should include both endpoints`() {
        val sweep = logarithmicSweep(0.01, 10.0, 4)
        assert(sweep.first().isCloseTo(0.01))
        assert(sweep.last().isCloseTo(10.0))
    }

    @Test
    fun `should space the samples evenly in the logarithm`() {
        val sweep = logarithmicSweep(0.001, 1.0, 4)
        assert(sweep.size == 4)
        listOf(0.001, 0.01, 0.1, 1.0).forEachIndexed { i, expected ->
            assert(sweep[i].isCloseTo(expected))
        }
    }

    @Test
    fun `should not sweep from a non-positive value`() {
        assertFailsWith<IllegalArgumentException> {
            logarithmicSweep(0.0, 1.0, 4)
        } should {
            have(message == "from must be positive, was: 0.0")
        }
    }

    @Test
    fun `should not sweep to a value below the start`() {
        assertFailsWith<IllegalArgumentException> {
            logarithmicSweep(1.0, 0.5, 4)
        } should {
            have(message == "to must exceed from, was: 0.5")
        }
    }

    @Test
    fun `should not sweep with fewer than two samples`() {
        assertFailsWith<IllegalArgumentException> {
            logarithmicSweep(1.0, 2.0, 1)
        } should {
            have(message == "samples must be at least 2, was: 1")
        }
    }

}
