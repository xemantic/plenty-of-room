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
import com.xemantic.nano.plentyofroom.lattice.HoneycombCrossoverLattice
import com.xemantic.nano.plentyofroom.lattice.SquareCrossoverLattice
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.honeycombXRasterPath
import com.xemantic.nano.plentyofroom.tile.HoneycombRasterResidues
import com.xemantic.nano.plentyofroom.tile.honeycombRasterProfile
import kotlin.math.sqrt
import kotlin.test.Test

/**
 * The recommended block, written out.
 *
 * `C-0151` selects the **drawable** two-length honeycomb raster — `102 / 109 bp` on the `10 × 6`
 * cross-section, closing on caDNAno's own `±5 bp` scaffold rule at **zero** forced crossovers,
 * `116 bp = 39.44 nm` of axial extent. Until this file that recommendation was a pair of integers
 * in a study literal; here it is a scadnano design, and every lattice fact it carries is derived
 * back **out of the design** and checked against the object `C-0151` computed it on.
 *
 * The scaffold is emitted; the staple set is **not**, and that is stated rather than invented —
 * this corpus determines the block's row lengths, turn senses, closure and station ladder, and it
 * has never determined a honeycomb staple routing. See [honeycombBlockScaffoldDesign].
 */
class HoneycombBlockDesignTest {

    private val rows = 10
    private val perRow = 6
    private val senseOne = 102
    private val senseTwo = 109

    private val design = honeycombBlockScaffoldDesign(rows, perRow, senseOne, senseTwo)
    private val residues = HoneycombRasterResidues(rows, perRow, senseOne, senseTwo)
    private val reread = ScadnanoDesign.fromText(design.toScadnanoText())

    // --- H1: the block is the one C-0151 recommends --------------------------------------------

    @Test
    fun `the block carries 60 helices on the honeycomb lattice`() {
        assert(design.grid == "honeycomb")
        assert(design.lattice() === HoneycombCrossoverLattice)
        assert(design.helixCount == rows * perRow)
        assert(design.helixCount == 60)
    }

    @Test
    fun `the scaffold is one strand of 60 domains making 59 raster turns`() {
        assert(design.scaffold().domains.size == 60)
        assert(design.scaffoldTurns().size == 59)
        assert(design.scaffoldTurns().size == residues.rasterCrossovers)
        // the staple set is NOT emitted, and the design says so rather than implying one
        assert(design.staples().isEmpty())
        assert(design.crossovers().isEmpty())
    }

    @Test
    fun `every helix carries one of the two recommended row lengths`() {
        val lengths = design.scaffold().domains.map { it.length }.toSortedSet()
        assert(lengths == sortedSetOf(senseOne, senseTwo))
    }

    @Test
    fun `the block extent is C-0151's 116 bp, which is 39_44 nm`() {
        val profile = honeycombRasterProfile(rows, perRow, senseOne, senseTwo)
        assert(profile.blockExtentBasePairs == 116)
        assert(design.rowBasePairs() == profile.blockExtentBasePairs)
        assert(design.edgeAlongHelicesNm().isCloseTo(116 * Gen1Tile.RISE_PER_BASE_PAIR))
        assert(design.edgeAlongHelicesNm().isCloseTo(39.44))
    }

    @Test
    fun `the raster closes on caDNAno's own rule at zero forced crossovers`() {
        assert(residues.closes)
        assert(residues.offRuleCrossovers == 0)
    }

    // --- H2: the design reproduces the object it was built from, at departure 0.0 ---------------

    @Test
    fun `every interior helix's span read BACK out of the design is the corpus's own window`() {
        val shift = -residues.blockWindow.lowBasePairs
        val domains = reread.scaffold().domains
        residues.helixWindows.forEach { (pathIndex, window) ->
            val domain = domains[pathIndex]
            assert(domain.start == window.lowBasePairs + shift)
            assert(domain.end == window.highBasePairs + shift)
        }
        // and the two path ends, which the corpus's own windows do not cover, take the window
        // their own POSITION carries in the rows of their own PARITY -- a row is not uniform, so
        // the row's span is the wrong reading and costs the block 7 nt
        val fromParity = { pathIndex: Int, sameParityRow: Int ->
            residues.helixWindows.getValue(sameParityRow * perRow + pathIndex % perRow)
        }
        assert(domains.first().start == fromParity(0, 2).lowBasePairs + shift)
        assert(domains.first().end == fromParity(0, 2).highBasePairs + shift)
        assert(domains.last().start == fromParity(rows * perRow - 1, 7).lowBasePairs + shift)
        assert(domains.last().end == fromParity(rows * perRow - 1, 7).highBasePairs + shift)
        // which is one of each row length -- exactly what HoneycombRasterProfile charges them
        assert(domains.first().length == senseOne)
        assert(domains.last().length == senseTwo)
    }

    @Test
    fun `every raster turn sits at the level the corpus's level walk puts it at`() {
        val shift = -residues.blockWindow.lowBasePairs
        val turns = reread.scaffoldTurns()
        assert(turns.size == residues.crossoverLevels.size)
        val domains = reread.scaffold().domains
        turns.forEachIndexed { k, turn ->
            // a turn occupies ONE offset -- the last base of helix k. A helix run in the +z sense
            // ends one base SHORT of its own upper level, because a window is half-open; one run
            // in -z ends ON its lower level.
            val level = residues.crossoverLevels.getValue(k) + shift
            assert(turn.offset == if (domains[k].forward) level - 1 else level)
            assert(turn.lowerHelix == k)
            assert(turn.upperHelix == k + 1)
        }
    }

    @Test
    fun `the round trip reproduces the block exactly`() {
        assert(reread.strands.map { it.domains } == design.strands.map { it.domains })
        assert(reread.helices == design.helices)
        assert(reread.toScadnanoText() == design.toScadnanoText())
    }

    // --- H3: scadnano's own honeycomb grid reproduces this corpus's cross-section ---------------

    @Test
    fun `scadnano's honeycomb grid position map reproduces this corpus's cross-section`() {
        // an independent implementation cross-check: scadnano's grid_position -> position map
        // (scadnano.py, grid_position_to_position) evaluated at the (column, raster row) this
        // writer emits must land on the corpus's own honeycomb cell, up to the y-axis sign
        val d = Gen1Tile.INTERHELICAL_HONEYCOMB
        val path = honeycombXRasterPath(rows, perRow)
        path.forEachIndexed { k, cell ->
            val row = k / perRow
            val position = design.helices[k].gridPosition
            assert(position == listOf(cell.x, row))
            val h = position[0]
            val v = position[1]
            val scadnanoX = h * sqrt(3.0) / 2.0 * d
            val scadnanoY = (
                if (h % 2 == 0) (v * 3 + Math.floorMod(v, 2)) / 2.0
                else (v * 3 - Math.floorMod(v, 2) + 1) / 2.0
                ) * d
            assert(scadnanoX.isCloseTo(cell.x * d * sqrt(3.0) / 2.0))
            assert(scadnanoY.isCloseTo(-cell.y * d / 2.0))
        }
    }

    // --- H4: the finding -- and its repair, with C-0160's pinning test explicitly RETIRED -------

    @Test
    fun `the SQUARE-lattice width rule is no longer applied to this honeycomb design`() {
        // RETIREMENT, recorded rather than deleted. `C-0160` left `checkBuildability()`
        // byte-identical and pinned its lattice-blind behaviour with a named test:
        //
        //     `checkBuildability applies a SQUARE-lattice width rule to a honeycomb design`
        //         val report = design.checkBuildability()
        //         assert(!report.seamlessRowWidthIsAdmissible)
        //         assert(report.violations.size == 1)
        //         assert("16 bp" in report.violations.single())
        //
        // That was a pin on a defect, held so `T-266`'s fired falsifier `F2` stayed measurable
        // until it was repaired. `T-270` repaired it, so the pin is retired here and replaced by
        // its negation: the design's OWN lattice answers, and no sentence of a honeycomb report
        // may name the square sheet's 16 bp ladder.
        val report = design.checkBuildability()
        assert(report.lattice == "honeycomb")
        assert(report.seamlessRowWidthIsAdmissible == null)
        assert((report.violations + report.notApplicable).none { "16 bp" in it })
        assert(design.lattice().samePairPeriodBasePairs == 21)
        assert(SquareCrossoverLattice.samePairPeriodBasePairs == 32)
    }

    @Test
    fun `the honeycomb branch ANSWERS the rule it used to withhold, and the block closes`() {
        val report = design.checkBuildability()
        assert(report.honeycombRasterCloses == true)
        assert(report.honeycombForcedCrossovers == 0)
        assert(report.everyScaffoldRunIsAdmissible == true)
        assert(report.everyStrandCrossingJoinsLatticeNeighbours == true)
        assert(report.noSiteIsCrossedTwice)
        assert(!report.carriesInsertionsOrDeletions)
        assert(report.violations.isEmpty())
        assert(report.notApplicable.isEmpty())
        assert(report.verdict == BuildabilityVerdict.ADMISSIBLE)
    }

    @Test
    fun `on a SQUARE design the check reproduces C-0160's report field for field`() {
        // `C-0160` asserted the lattice-aware check against the lattice-blind one at run time;
        // the lattice-blind one is gone, so its four fields are pinned here as LITERALS -- which
        // is the stronger pin, because it survives the function it was taken from.
        val tile = ScadnanoDesign.fromResource("/gen1-tile.sc")
        val report = tile.checkBuildability()
        assert(report.lattice == "square")
        assert(report.rowBasePairs == 112)
        assert(report.seamlessRowWidthIsAdmissible == true)
        assert(report.everyStrandCrossingJoinsLatticeNeighbours == true)
        assert(report.noSiteIsCrossedTwice)
        assert(!report.carriesInsertionsOrDeletions)
        assert(report.violations.isEmpty())
        assert(report.notApplicable.isEmpty())
    }
}
