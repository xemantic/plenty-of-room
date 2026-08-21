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
import com.xemantic.nano.plentyofroom.design.HONEYCOMB_BLOCK_DESIGN
import com.xemantic.nano.plentyofroom.design.SQUARE_SHEET_DESIGN
import com.xemantic.nano.plentyofroom.design.ScadnanoDesign
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import java.io.File
import kotlin.test.Test

/**
 * `T-267` — [HoneycombGrillage] built from a **design file** instead of from a pair of integers in
 * a study literal.
 *
 * The reproduction is taken on `assembleLoad`, per `CLAUDE.md`: a fixed-order scatter-add is
 * bit-comparable and a solved field is not. The `60`-helix block's solve is a study's expense, not
 * a test's, and it is taken there.
 */

/** `C-0141`'s recommended cross-section, and `C-0151`'s row lengths, as drawn. */
private const val T267_HC_ROWS = 10
private const val T267_HC_COLUMNS = 6
private const val T267_HC_EXTENT_BP = 116

private const val T267_HC_FOUNDATION = 0.012625625

class DesignedHoneycombGrillageTest {

    private val blockDesign = ScadnanoDesign.fromFile(File(HONEYCOMB_BLOCK_DESIGN))

    @Test
    fun `P1 - the committed block imports its own cross-section out of the file`() {
        val import = blockDesign.honeycombBlockImport("gen1-block-honeycomb-10x6-102-109")
        assert(import.refusals.isEmpty())
        assert(import.rasterRows == T267_HC_ROWS)
        assert(import.helicesPerRow == T267_HC_COLUMNS)
        assert(import.helices == 60)
        assert(import.axialWindowBasePairs == T267_HC_EXTENT_BP)
        assert(import.bondLength.isCloseTo(Gen1Tile.INTERHELICAL_HONEYCOMB))
        assert(import.rowPitch.isCloseTo(1.5 * Gen1Tile.INTERHELICAL_HONEYCOMB))
        assert(import.lengthS.isCloseTo(T267_HC_EXTENT_BP * Gen1Tile.RISE_PER_BASE_PAIR))
    }

    @Test
    fun `P3 - the imported block's load vector is BIT-IDENTICAL to the constants-built one`() {
        val imported = blockDesign
            .honeycombBlockImport("gen1-block-honeycomb-10x6-102-109")
            .grillage(foundationStiffness = T267_HC_FOUNDATION)
        val constants = HoneycombGrillage(
            block = HoneycombBlock(T267_HC_ROWS, T267_HC_COLUMNS),
            rowBasePairs = T267_HC_EXTENT_BP,
            foundationStiffness = T267_HC_FOUNDATION
        )
        assert(imported.degreesOfFreedom == constants.degreesOfFreedom)
        assert(imported.bonds.size == constants.bonds.size)
        val field = uniformPressure(0.0619578686)
        val a = constants.assembleLoad(field)
        val b = imported.assembleLoad(field)
        assert(a.length == b.length)
        assert((0 until a.length).all { a[it] == b[it] })
    }

    @Test
    fun `a SQUARE design is refused by the honeycomb import, rather than reshaped`() {
        val sheet = ScadnanoDesign.fromFile(File(SQUARE_SHEET_DESIGN))
        val import = sheet.honeycombBlockImport("gen1-sheet-square-15x112")
        assert(import.block == null)
        assert(import.refusals.any { "honeycomb" in it })
    }
}
