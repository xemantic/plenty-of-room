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
 * `T-204` — does `C-0022`'s collar transfer to a different aspect ratio?
 *
 * The collar is a **local** rim effect: `CLAUDE.md` records it as a sub-Debye **1.65 nm** band whose
 * total contribution scales as `1/L`, i.e. with the tile's **perimeter over area**. So a transfer
 * between two tiles is bounded by that ratio before any field is solved, which is the cheap bound
 * this task runs first.
 */
class CollarAspectRatioTest {

    @Test
    fun `the collar share scales as perimeter over area`() {
        // A square of side L has P/A = 4/L, so halving the side doubles the share.
        assert(collarShareRatio(40.0, 40.0, 20.0, 20.0).isCloseTo(2.0))
        // And a tile compared with itself transfers exactly.
        assert(collarShareRatio(38.08, 25.36, 38.08, 25.36).isCloseTo(1.0))
    }

    @Test
    fun `the two cross-sections differ by a quarter, and both exceed the solved tile`() {
        val solvedX = 40.0
        val solvedY = 40.35
        val ours = collarShareRatio(solvedX, solvedY, 38.08, 38.04)
        val theirs = collarShareRatio(solvedX, solvedY, 38.08, 25.36)
        assert(ours > 1.0)
        assert(theirs > ours)
        assert(abs(theirs / ours - collarShareRatio(38.08, 38.04, 38.08, 25.36)) < 1e-12)
    }

    @Test
    fun `a degenerate tile is refused rather than dividing by zero`() {
        assertFailsWith<IllegalArgumentException> { collarShareRatio(40.0, 40.0, 0.0, 10.0) }
        assertFailsWith<IllegalArgumentException> { collarShareRatio(0.0, 40.0, 10.0, 10.0) }
    }
}
