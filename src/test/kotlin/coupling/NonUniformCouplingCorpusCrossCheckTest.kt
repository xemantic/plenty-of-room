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
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.synthesis.perPathSecantCeiling
import kotlin.test.Test

/**
 * The part of `NonUniformCouplingTest` that cross-checks the library against a **corpus** claim.
 *
 * It lives in the root project rather than in `origami-engine` because `perPathSecantCeiling` is
 * `synthesis`, which is corpus: the library must not depend on the corpus, so a test that reads
 * both sits on the side that already sees both.  See `LIBRARY.md`, "tests that stayed behind".
 */
class NonUniformCouplingCorpusCrossCheckTest {

    private val mandate = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

    @Test
    fun `gate 5 cross-check - C-0049's per-path secant ceiling is this task's admissible ratio times the mandate`() {
        listOf(15, 45).forEach { paths ->
            val ratio = admissibleStiffnessRatio(
                Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE, mandate, paths
            )
            val ceiling = perPathSecantCeiling(
                Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, paths, Gen1Tile.ACCEPTABLE_STROKE
            )
            assert((ratio * mandate).isCloseTo(ceiling, 1e-12))
        }
    }

}
