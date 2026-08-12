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

package com.xemantic.nano.plentyofroom.structure

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.test.Test
import kotlin.test.assertFailsWith

class LoadPathsTest {

    // ---------------------------------------------------------------- gate 1

    @Test
    fun `gate 1 dimensional consistency - a total force over a path count should give a force per path`() {
        val path = LoadPath(
            name = "anchors", description = "four corner tethers", paths = 4.0, totalForce = 100.0
        )
        assert(path.forcePerPath.isCloseTo(25.0))
    }

    // ---------------------------------------------------------------- gate 2

    @Test
    fun `gate 2 limiting cases - the cited bands should be classified at and between their edges`() {
        assert(structuralBand(0.5) == StructuralBand.BELOW_ISOMERISATION)
        assert(structuralBand(9.99) == StructuralBand.BELOW_ISOMERISATION)
        assert(structuralBand(10.0) == StructuralBand.REVERSIBLE_ISOMERISATION)
        assert(structuralBand(34.9) == StructuralBand.REVERSIBLE_ISOMERISATION)
        assert(structuralBand(35.0) == StructuralBand.IRREVERSIBLE_DISASSEMBLY)
        assert(structuralBand(60.0) == StructuralBand.IRREVERSIBLE_DISASSEMBLY)
        assert(structuralBand(60.1) == StructuralBand.ABOVE_DISASSEMBLY)
    }

    @Test
    fun `gate 2 limiting cases - a single load path should carry the whole load`() {
        val single = LoadPath("lever", "one tether to the lever", 1.0, 100.0)
        assert(single.forcePerPath.isCloseTo(100.0))
        assert(single.band == StructuralBand.ABOVE_DISASSEMBLY)
    }

    // ---------------------------------------------------------------- gate 3

    @Test
    fun `gate 3 conservation - the per-path force times the path count should return the total`() {
        listOf(1.0, 3.0, 38.4, 512.0).forEach { paths ->
            val path = LoadPath("n", "n", paths, 100.0)
            assert((path.forcePerPath * paths).isCloseTo(100.0))
        }
    }

    // ---------------------------------------------------------------- the acceptance arithmetic

    /**
     * The number `T-5` exists to produce: not the force at an assumed attachment count,
     * but the attachment count the §3 force target demands. Strictly below the limit,
     * so a limit that divides the load exactly still costs one more path.
     */
    @Test
    fun `the minimum number of load paths should be the smallest count strictly under the limit`() {
        assert(minimumLoadPaths(totalForce = 100.0, limit = DISASSEMBLY_THRESHOLD) == 3)
        assert(minimumLoadPaths(totalForce = 100.0, limit = ISOMERISATION_THRESHOLD) == 11)
        assert(minimumLoadPaths(totalForce = 100.0, limit = 50.0) == 3)
        assert(minimumLoadPaths(totalForce = 100.0, limit = 100.0) == 2)
        assert(minimumLoadPaths(totalForce = 30.0, limit = DISASSEMBLY_THRESHOLD) == 1)
        listOf(3, 11).zip(listOf(DISASSEMBLY_THRESHOLD, ISOMERISATION_THRESHOLD))
            .forEach { (count, limit) ->
                assert(100.0 / count < limit)
                assert(100.0 / (count - 1) >= limit)
            }
    }

    // ---------------------------------------------------------------- validity

    @Test
    fun `a non-physical load path should be rejected`() {
        assertFailsWith<IllegalArgumentException> { minimumLoadPaths(0.0, 35.0) }
        assertFailsWith<IllegalArgumentException> { minimumLoadPaths(100.0, 0.0) }
    }

}
