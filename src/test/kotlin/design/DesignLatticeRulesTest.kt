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
import com.xemantic.nano.plentyofroom.lattice.HoneycombCrossoverLattice
import com.xemantic.nano.plentyofroom.lattice.SquareCrossoverLattice
import com.xemantic.nano.plentyofroom.structure.honeycombXRasterPath
import com.xemantic.nano.plentyofroom.tile.HoneycombRasterResidues
import java.io.File
import kotlin.test.Test

/**
 * `T-270` — the width rule, per lattice, derived from an imported **file**.
 *
 * `C-0160`'s falsifier `F2` fired: `checkBuildability()` applied `C-0086`'s square-lattice
 * odd-multiple-of-16-bp rule to a honeycomb design, twelve lines below the `lattice()` that
 * refuses to guess. These are the tests of the repair, and the two that matter most are the ones
 * that make the predicate refusable: the withdrawn `112 / 108` pair must **fail** the closure it
 * was withdrawn for, and the per-element rule must **pass** on the same pair, which is
 * `CLAUDE.md`'s *a per-element rule that is NECESSARY is not SUFFICIENT once the elements share a
 * boundary*, executable.
 */
class DesignLatticeRulesTest {

    private val rows = 10
    private val perRow = 6
    private val block = honeycombBlockScaffoldDesign(rows, perRow, 102, 109)
    private val residues = HoneycombRasterResidues(rows, perRow, 102, 109)
    private val shift = -residues.blockWindow.lowBasePairs
    private val sheet = ScadnanoDesign.fromResource("/gen1-tile.sc")

    // --- G1: scadnano's honeycomb grid position inverts to this corpus's own cell --------------

    @Test
    fun `scadnano's honeycomb grid position inverts to this corpus's cross-section cell`() {
        val path = honeycombXRasterPath(rows, perRow)
        path.forEachIndexed { k, cell ->
            val position = block.helices[k].gridPosition
            assert(honeycombCellOfGridPosition(position[0], position[1]) == cell)
        }
    }

    @Test
    fun `every scadnano honeycomb grid position is a site of this corpus's lattice`() {
        // not an accident of the emitted block: the map lands on the lattice at every (h, v),
        // which is what makes the neighbour class derivable from a design nobody here drew
        (0 until 12).forEach { h ->
            (0 until 12).forEach { v ->
                val cell = honeycombCellOfGridPosition(h, v)
                assert(cell.x == h)
                assert(cell.neighbours.size == 3)
            }
        }
    }

    // --- G2: the level convention, and both sides of a crossover agree on it -------------------

    @Test
    fun `a raster crossover sits on the edge of the axial window the helix turns at`() {
        val derived = block.importedRasterCrossovers()
        assert(derived.size == residues.rasterCrossovers)
        derived.forEachIndexed { k, crossover ->
            assert(crossover.levelBasePairs == residues.crossoverLevels.getValue(k) + shift)
        }
    }

    @Test
    fun `the OFFSET the file records is not the level, and differs on half the crossovers`() {
        // the trap this convention exists to avoid: a forward domain's exit offset is one BELOW
        // the level and a reverse domain's is ON it, so reading offsets perturbs half the reduced
        // residues by one and is not a datum at all
        val turns = block.scaffoldTurns()
        val derived = block.importedRasterCrossovers()
        val differing = derived.indices.count { turns[it].offset != derived[it].levelBasePairs }
        assert(differing > 0)
        assert(differing < derived.size)
    }

    // --- G3: the closure, from the file --------------------------------------------------------

    @Test
    fun `the recommended block's closure is derived from the FILE and reproduces the construction`() {
        val closure = block.honeycombClosure()
        assert(closure.closes)
        assert(closure.forcedCrossovers == 0)
        assert(closure.forcedCrossovers == residues.offRuleCrossovers)
        // the file's datum is the corpus's own z shifted by the emission; a shift moves every
        // reduced residue alike, which is what makes closure convention-free
        val shifted = residues.distinctReducedResidues.map { Math.floorMod(it + shift, 21) }.sorted()
        assert(closure.distinctReducedResidues == shifted)
        assert(closure.classZeroResidueCandidates ==
            residues.classZeroResidueCandidates.map { Math.floorMod(it + shift, 21) }.sorted())
    }

    @Test
    fun `the withdrawn 112 by 108 pair does NOT close, and the file says so at ten forced crossovers`() {
        val withdrawn = honeycombBlockScaffoldDesign(rows, perRow, 112, 108)
        val closure = withdrawn.honeycombClosure()
        assert(!closure.closes)
        assert(closure.forcedCrossovers == 10)
        assert(closure.classZeroResidueCandidates.isEmpty())
        assert(withdrawn.checkBuildability().verdict == BuildabilityVerdict.VIOLATIONS)
    }

    // --- G4: the per-element rule, and what it cannot enforce -----------------------------------

    @Test
    fun `the per-element rule PASSES on the pair the global rule refuses`() {
        // `C-0148` / `CH-0194`: `C-0136`'s admissible row lengths are the ±5 rule read on ONE
        // helix, and what they cannot enforce is that the two helices sharing a crossover agree
        // about which of the two positions it occupies. `CLAUDE.md` states it in as many words.
        val withdrawn = honeycombBlockScaffoldDesign(rows, perRow, 112, 108)
        assert(withdrawn.inadmissibleScaffoldRuns().isEmpty())
        assert(!withdrawn.honeycombClosure().closes)
        assert(block.inadmissibleScaffoldRuns().isEmpty())
    }

    @Test
    fun `on a seamless square raster the run rule IS C-0086's row-width rule`() {
        // the square branch is the same statement as the honeycomb's -- a run's length must carry
        // the azimuth its two ends need -- and on a boustrophedon, whose two ends go to OPPOSITE
        // neighbours, it reduces to the odd multiples of 16 bp exactly
        assert(sheet.inadmissibleScaffoldRuns().isEmpty())
        assert(squareSeamlessRowWidthIsAdmissible(sheet.rowBasePairs()))
        assert(!squareSeamlessRowWidthIsAdmissible(116))
        assert(admissibleRunResidues(SquareCrossoverLattice, 2) == setOf(16))
        assert(admissibleRunResidues(SquareCrossoverLattice, 0) == setOf(0))
    }

    @Test
    fun `the square rule is unconditional and the honeycomb's is not, because 2 is self-inverse mod 4`() {
        // `CLAUDE.md`: a lattice rule transfers between lattices only if its class difference is
        // self-inverse in the new modulus. A raster's axial sign alternates, so the rule survives
        // it exactly when `-d = d`.
        assert(Math.floorMod(-2, 4) == 2)
        assert(Math.floorMod(-1, 3) != 1)
        assert(Math.floorMod(-2, 3) != 2)
        assert(admissibleRunResidues(HoneycombCrossoverLattice, 1) !=
            admissibleRunResidues(HoneycombCrossoverLattice, 2))
        assert(admissibleRunResidues(HoneycombCrossoverLattice, 1) == setOf(7, 17, 18))
        assert(admissibleRunResidues(HoneycombCrossoverLattice, 2) == setOf(14, 3, 4))
    }

    // --- G5: the default is lattice-aware, in BOTH directions ----------------------------------

    @Test
    fun `a honeycomb design gets the honeycomb rule and NOT the square one`() {
        val report = block.checkBuildability()
        assert(report.lattice == "honeycomb")
        assert(report.honeycombRasterCloses == true)
        assert(report.honeycombForcedCrossovers == 0)
        assert(report.seamlessRowWidthIsAdmissible == null)
        assert(report.isSeamlessRaster)
        // the whole finding of `C-0160`'s F2: no sentence of a honeycomb report may name the
        // square sheet's 16 bp ladder
        assert((report.violations + report.notApplicable).none { "16 bp" in it })
        assert(report.verdict == BuildabilityVerdict.ADMISSIBLE)
    }

    @Test
    fun `a square design still gets C-0086's row-width rule, at the same verdict as before`() {
        val report = sheet.checkBuildability()
        assert(report.lattice == "square")
        assert(report.seamlessRowWidthIsAdmissible == true)
        assert(report.isSeamlessRaster)
        assert(report.honeycombRasterCloses == null)
        assert(report.rowBasePairs == 112)
        assert(report.everyStrandCrossingJoinsLatticeNeighbours == true)
        assert(report.noSiteIsCrossedTwice)
        assert(!report.carriesInsertionsOrDeletions)
        assert(report.violations.isEmpty())
        assert(report.notApplicable.isEmpty())
        assert(report.verdict == BuildabilityVerdict.ADMISSIBLE)
    }

    // --- G6: a design whose lattice cannot be derived ------------------------------------------

    @Test
    fun `an underivable lattice is INCONCLUSIVE, and the rules that do not need one are answered`() {
        val design = ScadnanoDesign(
            grid = "none",
            helixCount = 2,
            strands = listOf(
                ScadnanoStrand(
                    domains = listOf(
                        ScadnanoDomain(helix = 0, forward = true, start = 0, end = 10),
                        ScadnanoDomain(helix = 1, forward = false, start = 0, end = 10)
                    ),
                    isScaffold = true
                )
            )
        )
        val report = design.checkBuildability()
        assert(report.lattice == null)
        assert(report.verdict == BuildabilityVerdict.INCONCLUSIVE)
        assert(report.seamlessRowWidthIsAdmissible == null)
        assert(report.honeycombRasterCloses == null)
        assert(report.everyStrandCrossingJoinsLatticeNeighbours == null)
        // and the two rules that are statements about strands rather than about a lattice ARE
        // answered: a report with nothing in it would be indistinguishable from a clean one
        assert(report.noSiteIsCrossedTwice)
        assert(!report.carriesInsertionsOrDeletions)
        assert(report.notApplicable.isNotEmpty())
    }

    @Test
    fun `an underivable lattice still REPORTS a violation it can see`() {
        val doubled = ScadnanoStrand(
            domains = listOf(
                ScadnanoDomain(helix = 0, forward = true, start = 0, end = 10),
                ScadnanoDomain(helix = 1, forward = false, start = 0, end = 10)
            )
        )
        val design = ScadnanoDesign(
            grid = "none",
            helixCount = 2,
            strands = listOf(doubled, doubled)
        )
        val report = design.checkBuildability()
        assert(report.lattice == null)
        assert(!report.noSiteIsCrossedTwice)
        assert(report.verdict == BuildabilityVerdict.VIOLATIONS)
    }

    // --- G7: the one design in this tree that nobody here drew ---------------------------------

    @Test
    fun `the reference implementation's own rectangle is graded, seam and all`() {
        val rectangle = ScadnanoDesign.fromFile(
            File("gpd/designs/third-party/scadnano-origami-rectangle-16x8.sc")
        )
        val report = rectangle.checkBuildability()
        assert(report.lattice == "square")
        // it has a SEAM, so its scaffold runs are not its rows: 48 and 80 bp against a 128 bp
        // span, and the row-width reading is taken on 144 bp, which is neither
        assert(report.rowBasePairs == 144)
        assert(rectangle.axialSpanBasePairs() == 128)
        assert(!report.isSeamlessRaster)
        // so C-0086's SEAMLESS row-width rule is not the rule this design is owed, and the
        // report says `null` rather than answering a question about a premise it does not meet
        assert(report.seamlessRowWidthIsAdmissible == null)
        val runs = rectangle.scaffoldRuns().map { it.lengthBasePairs }.distinct().sorted()
        assert(runs == listOf(48, 80, 128))
        // and the generalised rule -- which the field's own generator must satisfy -- passes
        assert(rectangle.inadmissibleScaffoldRuns().isEmpty())
        assert(report.verdict == BuildabilityVerdict.ADMISSIBLE)
    }
}
