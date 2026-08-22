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
import com.xemantic.nano.plentyofroom.lattice.SquareCrossoverLattice
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * The boundary this repository did not have, and the one every other tool in the field does.
 *
 * caDNAno, scadnano, ENSnano, Adenita, MagicDNA and DNAforge all read and write an interchange
 * format. This corpus's tile is a set of Kotlin constants in `Gen1Tile`, so its buildability
 * proofs — the quantised seamless width, the crossover phase census, the station ladder — cannot
 * be run against anybody's design, and its own recommended tile cannot be handed to anybody
 * without a human redrawing it.
 *
 * The fixture is not a hand-written stub: it is `src/test/resources/gen1-tile.sc`, the scadnano
 * file the oxDNA run (`C-0157`, `tools/oxdna/gen1_tile_design.py`) actually simulated. So these
 * tests assert that a design **read from a file** reproduces the lattice facts this corpus derived
 * on its own — 15 duplexes, 112 bp, phase 8, seven columns, the 4/3 parity split, 49 crossovers —
 * which is a reproduction across two independent implementations in two languages, not a restatement.
 */
class ScadnanoDesignTest {

    private val design = ScadnanoDesign.fromResource("/gen1-tile.sc")

    // --- gate 1: the file parses into the objects the lattice layer needs ------------------------

    @Test
    fun `the design declares its grid, and it is the square lattice`() {
        assert(design.grid == "square")
        assert(design.lattice() === SquareCrossoverLattice)
    }

    @Test
    fun `a design whose grid this repository has no lattice for is refused, not guessed`() {
        val failure = assertFailsWith<IllegalArgumentException> {
            ScadnanoDesign(grid = "hex", helixCount = 2, strands = emptyList()).lattice()
        }
        assert("hex" in failure.message!!)
    }

    @Test
    fun `the helices and strands are read`() {
        assert(design.helixCount == 15)
        assert(design.strands.size == 65)
        assert(design.scaffold().domains.size == 15)
        assert(design.staples().size == 64)
    }

    // --- gate 2: the raster, derived from the file rather than from a constant --------------------

    @Test
    fun `the scaffold is a boustrophedon of 112 base pairs per row`() {
        assert(design.rowBasePairs() == 112)
        val directions = design.scaffold().domains.map { it.forward }
        // a raster alternates direction every row, which is what makes it seamless
        directions.forEachIndexed { row, forward -> assert(forward == (row % 2 == 0)) }
    }

    @Test
    fun `the tile edge along the helices comes out at C-0086's buildable 38_08 nm`() {
        assert(design.edgeAlongHelicesNm().isCloseTo(38.08))
    }

    // --- gate 3: the crossovers, counted from the strands -----------------------------------------

    @Test
    fun `the staple crossovers land on seven columns at phase 8`() {
        assert(design.crossoverColumns() == listOf(8, 24, 40, 56, 72, 88, 104))
        assert(design.crossoverPhase() == 8)
        // and the columns are the lattice's own plane ladder: phase + 16k
        design.crossoverColumns().forEachIndexed { index, column ->
            assert(column == 8 + index * (SquareCrossoverLattice.samePairPeriodBasePairs / 2))
        }
    }

    @Test
    fun `the sheet builds 49 crossovers, split 4-3 between the two parities`() {
        assert(design.crossoverCount() == 49)
        val perInterface = design.crossoversPerInterface()
        assert(perInterface.size == 14)
        assert(perInterface.sum() == 49)
        assert(perInterface.count { it == 4 } == 7)
        assert(perInterface.count { it == 3 } == 7)
        // the split alternates, which is what makes D_perp a HARMONIC mean and not a smeared one
        perInterface.forEachIndexed { interfaceIndex, count ->
            assert(count == if (interfaceIndex % 2 == 0) 4 else 3)
        }
    }

    @Test
    fun `every crossover joins ADJACENT duplexes, which is what makes the graph a path`() {
        design.crossovers().forEach { crossover ->
            assert(crossover.upperHelix - crossover.lowerHelix == 1)
        }
    }

    @Test
    fun `a crossover is a SINGLE strand crossing, so no site is registered twice`() {
        val sites = design.crossovers().map { it.lowerHelix to it.offset }
        assert(sites.size == sites.toSet().size)
    }

    // --- gate 4: the register the design carries, from the lattice layer ---------------------------

    @Test
    fun `the imported raster carries C-0086's 60 degrees of accumulated register error`() {
        // the design is drawn at caDNAno's square-lattice twist, so the row accumulates against
        // B-DNA -- and this is the strain the oxDNA run of the same file relaxed against.
        assert(design.accumulatedRegisterDepartureDegrees().isCloseTo(-60.0))
    }

    // --- gate 5: a design is checkable against the rules this corpus derived ------------------------

    @Test
    fun `the imported design passes this repository's own buildability rules`() {
        val report = design.checkBuildability()
        assert(report.violations.isEmpty())
        assert(report.seamlessRowWidthIsAdmissible == true)
        assert(report.everyStrandCrossingJoinsLatticeNeighbours == true)
        assert(report.noSiteIsCrossedTwice)
    }

    @Test
    fun `a row width off the seamless ladder is REPORTED rather than silently accepted`() {
        // 118 bp is not an odd multiple of 16, so a boustrophedon cannot turn there: C-0086.
        // `T-270` renamed the rule to carry the lattice it belongs to; `buildabilityOfRowWidth`,
        // which answered it for any design that had not been drawn yet, is retired with it.
        assert(!squareSeamlessRowWidthIsAdmissible(118))
        assert(squareSeamlessRowWidthIsAdmissible(112))
    }
}
