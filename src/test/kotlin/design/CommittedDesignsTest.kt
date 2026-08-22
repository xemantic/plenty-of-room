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

package com.xemantic.nano.plentyofroom.design

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.tile.honeycombRasterProfile
import java.io.File
import kotlin.test.Test

/**
 * The committed design artifacts, and the one thing that keeps them from becoming fossils.
 *
 * A design in `gpd/designs/` is not a result file: no `Entry points` row re-runs it. What replaces
 * that guarantee is this test — the artifact must be **byte-identical** to what the writer produces
 * today, so a change to the writer, to the lattice or to the recommended raster fails the build
 * instead of leaving a stale file somebody could fold.
 *
 * `gpd/README.md`'s rule for a result file is that a re-run changing nothing produces no diff.
 * Here it is stronger: a re-run is not even needed, because the comparison is the test.
 */
class CommittedDesignsTest {

    private val sheetFile = File(SQUARE_SHEET_DESIGN)
    private val blockFile = File(HONEYCOMB_BLOCK_DESIGN)

    @Test
    fun `the committed square sheet is exactly what the writer emits today`() {
        assert(sheetFile.exists())
        assert(sheetFile.readText() == squareSheetDesign().toScadnanoText())
    }

    @Test
    fun `the committed honeycomb block is exactly what the writer emits today`() {
        assert(blockFile.exists())
        assert(blockFile.readText() == recommendedHoneycombBlockDesign().toScadnanoText())
    }

    @Test
    fun `the committed square sheet carries C-0086's tile, read back out of the artifact`() {
        val design = ScadnanoDesign.fromFile(sheetFile)
        assert(design.grid == "square")
        assert(design.helixCount == 15)
        assert(design.rowBasePairs() == 112)
        assert(design.edgeAlongHelicesNm().isCloseTo(38.08))
        assert(design.crossoverPhase() == 8)
        assert(design.crossoverColumns().size == 7)
        assert(design.crossoverCount() == 49)
        assert(design.scaffoldTurns().size == 14)
        assert(design.crossoversPerInterface().count { it == 4 } == 7)
        assert(design.crossoversPerInterface().count { it == 3 } == 7)
        assert(design.checkBuildability().verdict == BuildabilityVerdict.ADMISSIBLE)
    }

    @Test
    fun `the committed honeycomb block carries C-0151's raster, read back out of the artifact`() {
        val design = ScadnanoDesign.fromFile(blockFile)
        val profile = honeycombRasterProfile(
            RECOMMENDED_RASTER_ROWS, RECOMMENDED_HELICES_PER_ROW,
            RECOMMENDED_SENSE_ONE_BASE_PAIRS, RECOMMENDED_SENSE_TWO_BASE_PAIRS
        )
        assert(design.grid == "honeycomb")
        assert(design.helixCount == 60)
        assert(design.scaffold().domains.size == 60)
        assert(design.scaffoldTurns().size == 59)
        assert(design.rowBasePairs() == 116)
        assert(design.edgeAlongHelicesNm().isCloseTo(39.44))
        assert(design.scaffold().domains.map { it.length }.toSortedSet() == sortedSetOf(102, 109))
        // C-0151's own numbers for the pair, reproduced on the emitted design
        assert(profile.closes)
        assert(profile.offRuleCrossovers == 0)
        assert(profile.staggerBasePairs == 7)
        assert(profile.rowSpanBasePairs == 109)
        assert(profile.interfaceWindowBasePairs == 102)
        assert(profile.stationsOnFace == 55)
        assert(profile.ladderPhaseBasePairs == 16)
        assert(profile.interRowOffsetBasePairs == 14)
        assert(profile.classZeroResidue == 5)
        assert(profile.fitsM13)
        // T-270: graded on its OWN lattice, and the honeycomb branch now ANSWERS rather than
        // withholding -- `C-0148`'s closure, derived from the file the artifact is
        val report = design.checkBuildability()
        assert(report.lattice == "honeycomb")
        assert(report.honeycombRasterCloses == true)
        assert(report.honeycombForcedCrossovers == 0)
        // one b0, and it is the construction's own shifted onto the file's datum (T-270's tests
        // carry the shift; here what matters is that the file determines exactly one)
        assert(report.honeycombClassZeroResidues.size == 1)
        assert(report.verdict == BuildabilityVerdict.ADMISSIBLE)
    }

    @Test
    fun `the two path ends take ONE OF EACH row length, and the corpus's charge is exact`() {
        // The two path ends carry no derived window -- their turn sense is undefined -- and
        // `HoneycombRasterProfile` charges them `L1 + L2`, "C-0140's own accounting". Drawn, they
        // come out at exactly that: 102 and 109, so the profile's charge is not a convention that
        // happens to be safe, it is right, and the reason is the raster's row-PARITY symmetry.
        val profile = honeycombRasterProfile(
            RECOMMENDED_RASTER_ROWS, RECOMMENDED_HELICES_PER_ROW,
            RECOMMENDED_SENSE_ONE_BASE_PAIRS, RECOMMENDED_SENSE_TWO_BASE_PAIRS
        )
        val design = ScadnanoDesign.fromFile(blockFile)
        val lengths = design.scaffold().domains.map { it.length }
        assert(lengths.first() == RECOMMENDED_SENSE_ONE_BASE_PAIRS)
        assert(lengths.last() == RECOMMENDED_SENSE_TWO_BASE_PAIRS)
        assert(lengths.sum() == profile.scaffoldNucleotides)
        assert(lengths.sum() == 6330)
        // a row is NOT uniform: 30 helices at each length, which is what makes the ends' lengths a
        // symmetry statement rather than the row-span reading, and that reading costs 7 nt
        assert(lengths.count { it == RECOMMENDED_SENSE_ONE_BASE_PAIRS } == 30)
        assert(lengths.count { it == RECOMMENDED_SENSE_TWO_BASE_PAIRS } == 30)
        assert(profile.fitsM13)
        assert(profile.scaffoldSpareOnM13 == 7249 - 6330)
    }

    @Test
    fun `the designs directory carries the README that names which claim recommended what`() {
        val readme = File("$DESIGN_DIRECTORY/README.md")
        assert(readme.exists())
        val text = readme.readText()
        listOf("C-0151", "C-0086", "C-0157", "39.44", "38.08", "102", "109").forEach {
            assert(it in text)
        }
    }
}
