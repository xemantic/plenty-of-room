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
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlin.math.abs
import kotlin.test.Test

/**
 * `T-245` — the **closing** two-length honeycomb rasters, and the selection inside them.
 *
 * Written before the model and watched fail.
 */
class HoneycombClosingFamilyTest {

    private val rise = Gen1Tile.RISE_PER_BASE_PAIR

    // ------------------------------------------------------- gate 1: dimensional consistency

    @Test
    fun `a closing residue pair is a pair of residues modulo 21`() {
        closingResiduePairs(10, 6).forEach { (one, two) ->
            assert(one in 0..20)
            assert(two in 0..20)
        }
    }

    @Test
    fun `a length pair's block extent in nm is its base pairs at the rise`() {
        val profile = honeycombRasterProfile(10, 6, 102, 109)
        assert(abs(profile.blockExtentNm - profile.blockExtentBasePairs * rise) < 1e-12)
        assert(abs(profile.interfaceWindowNm - profile.interfaceWindowBasePairs * rise) < 1e-12)
    }

    // --------------------------------------------- gate 2: limiting cases and the cheap bound

    @Test
    fun `closure depends only on the two lengths MODULO 21, which is what makes the sweep exhaustive`() {
        listOf(10 to 6, 15 to 4).forEach { (rows, perRow) ->
            (0 until 21).forEach { one ->
                (0 until 21).forEach { two ->
                    val a = HoneycombRasterResidues(rows, perRow, 105 + one, 105 + two).closes
                    val b = HoneycombRasterResidues(rows, perRow, 84 + one, 147 + two).closes
                    assert(a == b)
                }
            }
        }
    }

    @Test
    fun `EVERY closing pair has the same length difference modulo 21, so the minimum stagger is 7`() {
        val differences = closingResiduePairs(10, 6)
            .map { (one, two) -> Math.floorMod(one - two, 21) }
            .toSet()
        assert(differences.size == 1)
        assert(differences.single() == 14)
        assert(minimumClosingStaggerBasePairs(10, 6) == 7)
    }

    @Test
    fun `the closing residue set is the same on both 60-helix cross-sections`() {
        assert(closingResiduePairs(10, 6).toSet() == closingResiduePairs(15, 4).toSet())
    }

    @Test
    fun `a UNIFORM row length never closes, which is C-0140's negative from the residue side`() {
        (0 until 21).forEach { residue ->
            assert(!HoneycombRasterResidues(10, 6, 105 + residue, 105 + residue).closes)
        }
    }

    @Test
    fun `the block extent of a two-length raster is twice the larger length less the smaller`() {
        listOf(112 to 108, 102 to 109, 101 to 108, 112 to 119, 122 to 119).forEach { (a, b) ->
            val profile = honeycombRasterProfile(10, 6, a, b)
            assert(profile.blockExtentBasePairs == 2 * maxOf(a, b) - minOf(a, b))
            assert(profile.rowSpanBasePairs == maxOf(a, b))
            assert(profile.staggerBasePairs == abs(a - b))
        }
    }

    @Test
    fun `an interface window is the row span less the stagger`() {
        listOf(112 to 108, 102 to 109, 101 to 108).forEach { (a, b) ->
            val profile = honeycombRasterProfile(10, 6, a, b)
            assert(
                profile.interfaceWindowBasePairs ==
                        profile.rowSpanBasePairs - profile.staggerBasePairs
            )
        }
    }

    // ------------------------------------------------ gate 3: symmetry, conservation, reproduction

    @Test
    fun `the closure verdict survives all four sign, mirror and datum conventions`() {
        listOf(112 to 108, 102 to 109, 101 to 108, 112 to 119).forEach { (a, b) ->
            val verdicts = listOf(1, -1).flatMap { sign ->
                listOf(false, true).flatMap { mirrored ->
                    listOf(false, true).map { reversed ->
                        HoneycombRasterResidues(10, 6, a, b, sign, mirrored, reversed).closes
                    }
                }
            }.toSet()
            assert(verdicts.size == 1)
        }
    }

    @Test
    fun `C-0148's five-pair verdict is reproduced exactly`() {
        assert(!HoneycombRasterResidues(10, 6, 112, 108).closes)
        assert(!HoneycombRasterResidues(10, 6, 101, 109).closes)
        assert(HoneycombRasterResidues(10, 6, 102, 109).closes)
        assert(!HoneycombRasterResidues(10, 6, 112, 109).closes)
        assert(!HoneycombRasterResidues(10, 6, 122, 119).closes)
    }

    @Test
    fun `C-0148's three closing classes on the 10 x 6 path are reproduced`() {
        assert(closingResiduePairs(10, 6).toSet() == setOf(7 to 14, 17 to 3, 18 to 4))
    }

    @Test
    fun `C-0140's minimum stagger of 3 bp is a PER-HELIX minimum and no closing pair reaches it`() {
        val closing = closingLengthPairs(10, 6, 80, 160, 21)
        assert(closing.isNotEmpty())
        assert(closing.none { (a, b) -> abs(a - b) < 7 })
        assert(closing.any { (a, b) -> abs(a - b) == 7 })
    }

    @Test
    fun `the scaffold budget reproduces C-0140's 6596 nt at its own pair and cross-section`() {
        assert(honeycombRasterProfile(15, 4, 112, 108).scaffoldNucleotides == 6596)
        assert(honeycombRasterProfile(15, 4, 101, 109).scaffoldNucleotides == 6308)
        assert(honeycombRasterProfile(15, 4, 102, 109).scaffoldNucleotides == 6337)
    }

    @Test
    fun `the ragged faces reproduce C-0140's 4 and 8 bp at 112 over 108`() {
        val profile = honeycombRasterProfile(15, 4, 112, 108)
        assert(profile.frontFaceRaggednessBasePairs == 4)
        assert(profile.rearFaceRaggednessBasePairs == 8)
    }

    @Test
    fun `a closing profile carries a determined b0, phase and station census`() {
        val profile = honeycombRasterProfile(10, 6, 102, 109)
        assert(profile.closes)
        assert(profile.classZeroResidue == 5)
        assert(profile.ladderPhaseBasePairs == 16)
        assert(profile.interRowOffsetBasePairs == 14)
        assert(profile.stationsOnFace == 55)
        assert(profile.sparsestRowStations == 5)
    }

    @Test
    fun `a non-closing profile has no determined phase and says so rather than guessing`() {
        val profile = honeycombRasterProfile(10, 6, 112, 108)
        assert(!profile.closes)
        assert(profile.classZeroResidue == null)
        assert(profile.ladderPhaseBasePairs == null)
        assert(profile.stationsOnFace == null)
    }

    // --------------------------------------------------- gate 4: the column count off the rows

    @Test
    fun `the recommended pair's row-derived column count is ten, as CH-0188 predicts`() {
        val profile = honeycombRasterProfile(10, 6, 102, 109)
        val pitch = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP * rise / 2.0
        listOf(0.05, 0.5 * rise, rise).forEach { margin ->
            assert(crossoverColumnsIn(profile.interfaceWindowNm, pitch, margin) == 10)
        }
    }

    @Test
    fun `112 over 108's interface window still gives eleven, which is C-0148's own reading`() {
        val profile = honeycombRasterProfile(10, 6, 112, 108)
        val pitch = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP * rise / 2.0
        listOf(0.05, 0.5 * rise, rise).forEach { margin ->
            assert(crossoverColumnsIn(profile.interfaceWindowNm, pitch, margin) == 11)
        }
    }

    // ---------------------------------------------------------- gate 5: the selection itself

    @Test
    fun `no closing pair inside M13 beats 116 bp on the axial extent axis`() {
        val nominal = 40.0
        val best = closingLengthPairs(10, 6, 60, 200, 42)
            .map { (a, b) -> honeycombRasterProfile(10, 6, a, b) }
            .filter { it.scaffoldNucleotides <= 7249 }
            .minBy { abs(it.blockExtentNm - nominal) }
        assert(abs(best.blockExtentBasePairs - 116) == 0)
    }

    @Test
    fun `the best extent is reached by several closing pairs and the tightest stagger is seven`() {
        val at116 = closingLengthPairs(10, 6, 60, 200, 42)
            .map { (a, b) -> honeycombRasterProfile(10, 6, a, b) }
            .filter { it.blockExtentBasePairs == 116 }
        assert(at116.size > 1)
        assert(at116.minOf { it.staggerBasePairs } == 7)
        val tightest = at116.minBy { it.staggerBasePairs }
        assert(tightest.senseOneBasePairs == 102)
        assert(tightest.senseTwoBasePairs == 109)
        // a wider stagger at the same extent costs the interface window, hence a column
        assert(at116.all { it.staggerBasePairs == 7 || it.interfaceWindowBasePairs < 102 })
    }

    @Test
    fun `the unpaired-nucleotide allowance is per HELIX and reproduces C-0147's 8 at a uniform 112 bp row`() {
        // C-0147: 60 x (112 + L) <= 7249 gives L <= 8, and it divides the allowance into front
        // and rear loop fragments at the ends of each HELIX -- so the denominator is 60, not 59.
        assert((7249 - 60 * 112) / 60 == 8)
        val profile = honeycombRasterProfile(10, 6, 102, 109)
        assert(profile.unpairedNucleotidesPerHelixOnM13 == profile.scaffoldSpareOnM13 / 60)
    }

    @Test
    fun `the closing family's own best pair is the same at both 60-helix cross-sections`() {
        fun best(rows: Int, perRow: Int): HoneycombRasterProfile =
            closingLengthPairs(rows, perRow, 60, 200, 42)
                .map { (a, b) -> honeycombRasterProfile(rows, perRow, a, b) }
                .filter { it.fitsM13 }
                .let { admissible ->
                    val target = admissible.minOf { abs(it.blockExtentNm - 40.0) }
                    admissible.filter { abs(it.blockExtentNm - 40.0) <= target + 1e-9 }
                        .minByOrNull { it.staggerBasePairs }!!
                }
        val a = best(10, 6)
        val b = best(15, 4)
        assert(a.senseOneBasePairs == b.senseOneBasePairs)
        assert(a.senseTwoBasePairs == b.senseTwoBasePairs)
        assert(a.senseOneBasePairs == 102)
        assert(a.senseTwoBasePairs == 109)
        assert(a.stationsOnFace == 55)
        assert(b.stationsOnFace == 82)
    }

    @Test
    fun `a raster refuses a non-positive row length rather than walking a degenerate lattice`() {
        var threw = false
        try {
            honeycombRasterProfile(10, 6, 0, 109)
        } catch (e: IllegalArgumentException) {
            threw = true
        }
        assert(threw)
    }
}
