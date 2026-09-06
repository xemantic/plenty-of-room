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
import com.xemantic.nano.plentyofroom.anchoring.ArmAnchorage
import com.xemantic.nano.plentyofroom.anchoring.BForm
import com.xemantic.nano.plentyofroom.anchoring.tradePoint
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.test.Test

/**
 * The part of `ConsumedCrossoverSheetTest` that cross-checks the library against a **corpus**
 * claim.
 *
 * It lives in the root project rather than in `origami-engine` because `ArmAnchorage`, `BForm`
 * and `tradePoint` are `anchoring`, which is corpus.  See `LIBRARY.md`, "tests that stayed
 * behind".
 */
class ConsumedCrossoverSheetCorpusCrossCheckTest {

    /**
     * `C-0046`'s own elastica, re-run as a library at the connectivity ceiling — the resolution
     * of the `34 < n ≤ 45` bracket its own claim leaves open.
     */
    @Test
    fun `gate 5 upstream - C-0046's design reproduces and the connected ceiling clears the acceptable stroke`() {
        val far = ArmAnchorage.twoTerminus(BForm.PHOSPHATE_RADIUS).rotationalStiffness
        fun place(paths: Int) = tradePoint(
            paths, 1, far, Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE,
            Gen1Tile.ACCEPTABLE_STROKE, Gen1Tile.DESIRED_STROKE
        )
        assert(place(45).armLength.isCloseTo(9.131, 1e-4))
        assert(place(45).usableStroke.isCloseTo(3.119, 1e-4))
        assert(place(56).usableStroke.isCloseTo(3.312, 1e-4))
        // the ceiling clears the acceptable stroke; the threshold is 39 and 38 does not
        assert(place(42).usableStroke >= Gen1Tile.ACCEPTABLE_STROKE)
        assert(place(39).usableStroke >= Gen1Tile.ACCEPTABLE_STROKE)
        assert(place(38).usableStroke < Gen1Tile.ACCEPTABLE_STROKE)
    }

}
