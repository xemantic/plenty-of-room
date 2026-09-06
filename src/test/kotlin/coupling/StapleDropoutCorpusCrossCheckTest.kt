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

package com.xemantic.nano.plentyofroom.coupling

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import kotlin.test.Test

/**
 * The part of `StapleDropoutTest` that cross-checks the library against a **corpus** claim.
 *
 * It lives in the root project rather than in `origami-engine` because `rimMask` is
 * `BuildableStiffnessRatio`, which reads `anchoring` and is therefore corpus.  See `LIBRARY.md`,
 * "tests that stayed behind".
 */
class StapleDropoutCorpusCrossCheckTest {

    private val duplexes = 15

    private val sheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )

    private val edgeX = Gen1Tile.EDGE_X

    private val edgeY = duplexes * sheet.interhelicalDistance

    private val grid = attachmentGrid(3, duplexes, edgeX, edgeY)

    @Test
    fun `gate 5 - the 3 x 15 grid's rim mask is C-0058's 34 and 11`() {
        val mask = rimMask(grid, edgeX, edgeY, 6.7)
        assert(mask.count { it } == 34)
        assert(mask.count { !it } == 11)
    }

}
