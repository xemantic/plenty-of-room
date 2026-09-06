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
import com.xemantic.nano.plentyofroom.anchoring.maximumPlanCeilingForCount
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.centroSymmetricPlacementsOn
import kotlin.test.Test

/**
 * The part of `HoneycombFaceLatticeTest` that runs the **corpus's** placement machinery over the
 * library's honeycomb station lattice.
 *
 * `maximumPlanCeilingForCount` is `anchoring` and `centroSymmetricPlacementsOn` is
 * `TwistCorrectedRaster`; both are corpus, so these tests sit on the side that sees both.  What
 * they assert is unchanged: the placement machinery is lattice-GENERIC, and what is
 * square-lattice-specific is the lattice generator.  See `LIBRARY.md`, "tests that stayed behind".
 */
class HoneycombFaceLatticeCorpusCrossCheckTest {

    private val d = Gen1Tile.INTERHELICAL_HONEYCOMB

    @Test
    fun `the square-lattice plan ceiling machinery accepts the honeycomb lattice`() {
        // F5. `maximumPlanCeilingForCount` takes an explicit lattice, so nothing about it is
        // square-lattice-specific; what is square-lattice-specific is the lattice GENERATOR.
        val lattice = honeycombStationLattice(15, 112, 0, 7)
        val ceiling = maximumPlanCeilingForCount(
            lattice, count = 45, edgeX = 112 * Gen1Tile.RISE_PER_BASE_PAIR,
            width = d, maximumPerRow = 6
        )
        assert(ceiling != null)
        // F3 FIRES: at 45 of 90 stations a placement SKIPS stations, so the binding pitch is
        // 42 bp and not 21, and the ceiling is ABOVE the square lattice's 8.19 nm inboard bound.
        assert(ceiling!! > 8.19)
    }

    @Test
    fun `the inboard bound binds only at the SATURATED count`() {
        val lattice = honeycombStationLattice(15, 112, 0, 7)
        val inboard = HoneycombLattice.SAME_PAIR_PERIOD_BP * Gen1Tile.RISE_PER_BASE_PAIR - d
        val saturated = maximumPlanCeilingForCount(
            lattice, count = 90, edgeX = 112 * Gen1Tile.RISE_PER_BASE_PAIR,
            width = d, maximumPerRow = 6
        )
        assert(saturated != null)
        assert(saturated!! < inboard)
    }

    @Test
    fun `a centro-symmetric placement family exists on the even-row honeycomb lattice`() {
        val lattice = honeycombStationLattice(10, 112, 0, 7)
        val found = centroSymmetricPlacementsOn(
            lattice, edgeX = 112 * Gen1Tile.RISE_PER_BASE_PAIR, arm = 3.0, count = 20,
            minimumPerRow = 2, maximumPerRow = 2, width = d
        ).take(3).toList()
        assert(found.isNotEmpty())
        assert(found.all { it.isCentroSymmetric(10) })
    }

}
