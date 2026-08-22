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
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.grillageImport
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-275` — the census that says which lattice a measured constant was measured on.
 *
 * Every test here is named for the gate or the predicate it decides, and every one of them is
 * an integer, a ratio of integers or a base-pair offset. Nothing is solved.
 */
class SimulatedTileCensusTest {

    private val sheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )
    private val design = ScadnanoDesign.fromFile(File(SQUARE_SHEET_DESIGN))

    // --- P1, the cheap bound: two phase data, one integer ------------------------------------

    @Test
    fun `P1 the phase data differ by exactly eight base pairs on a 112 bp row`() {
        assert(phaseDatumOffsetBasePairs(112, 16) == 8)
    }

    @Test
    fun `P1 the offset is a row length modulo the column pitch, so an even pitch count is zero`() {
        assert(phaseDatumOffsetBasePairs(96, 16) == 0)
        assert(phaseDatumOffsetBasePairs(128, 16) == 0)
        assert(phaseDatumOffsetBasePairs(80, 16) == 8)
    }

    @Test
    fun `P1 an odd row length has no centre base pair and is refused`() {
        assertFailsWith<IllegalArgumentException> { phaseDatumOffsetBasePairs(111, 16) }
    }

    // --- gate 1, dimensional: the rigidity ledger is dimensionless ---------------------------

    @Test
    fun `gate a uniform interface count makes the smeared and series fractions exactly equal`() {
        val uniform = List(14) { 4 }
        assert(smearedRigidityFraction(uniform, 4) == 1.0)
        assert(seriesRigidityFraction(uniform, 4) == 1.0)
        assert(smearedRigidityFraction(uniform, 4) == seriesRigidityFraction(uniform, 4))
    }

    @Test
    fun `gate the four three split is 49 over 56 smeared and 6 over 7 in series`() {
        val split = List(14) { if (it % 2 == 0) 4 else 3 }
        assert(smearedRigidityFraction(split, 4).isCloseTo(49.0 / 56.0))
        assert(seriesRigidityFraction(split, 4).isCloseTo(6.0 / 7.0))
        // the series reading is the pessimistic one, and CLAUDE.md prices the gap at 48/49
        assert(
            (seriesRigidityFraction(split, 4) / smearedRigidityFraction(split, 4))
                .isCloseTo(48.0 / 49.0)
        )
    }

    @Test
    fun `gate an empty interface annihilates the series fraction and not the smeared one`() {
        val depleted = listOf(4, 4, 0, 4)
        assert(seriesRigidityFraction(depleted, 4) == 0.0)
        assert(smearedRigidityFraction(depleted, 4) > 0.0)
    }

    // --- P4, the lattice the corpus grades on ------------------------------------------------

    @Test
    fun `P4 tile-centre phase 8 with the row end admitted is eight columns and 56 crossovers`() {
        val row = latticeCensusRow(8, sheet, BUILDABLE_EDGE_X, 15, admitRowEnd = true)
        assert(row.columnCount == 8)
        assert(row.crossoverCount == 56)
        assert(row.crossoversPerInterface.all { it == 4 })
        assert(row.columnOnRowEnd)
    }

    @Test
    fun `P4 tile-centre phase 8 with the row end refused is six columns and 42 crossovers`() {
        val row = latticeCensusRow(8, sheet, BUILDABLE_EDGE_X, 15, admitRowEnd = false)
        assert(row.columnCount == 6)
        assert(row.crossoverCount == 42)
        assert(row.crossoversPerInterface.all { it == 3 })
    }

    @Test
    fun `P4 neither reading at phase 8 is the 49 the simulated tile carries`() {
        assert(latticeCensusRow(8, sheet, BUILDABLE_EDGE_X, 15, true).crossoverCount != 49)
        assert(latticeCensusRow(8, sheet, BUILDABLE_EDGE_X, 15, false).crossoverCount != 49)
    }

    // --- P2, the design's own census, filtered by strand role ---------------------------------

    @Test
    fun `P2 the emitted sheet carries 49 staple crossovers and 14 raster turns`() {
        val census = design.crossoverCensus()
        assert(census.stapleCrossovers == 49)
        assert(census.scaffoldTurns == 14)
        assert(census.allStrandCrossings == 63)
    }

    @Test
    fun `P2 a bare crossovers count on this design is ambiguous by 28 point 57 percent`() {
        val census = design.crossoverCensus()
        assert(census.roleAmbiguity.isCloseTo(63.0 / 49.0 - 1.0))
        assert(census.roleAmbiguity.isCloseTo(0.2857142857, relativeTolerance = 1e-8))
    }

    @Test
    fun `P2 the design's columns are the row-start lattice 8 plus 16k`() {
        val census = design.crossoverCensus()
        assert(census.columnOffsetsBasePairs == listOf(8, 24, 40, 56, 72, 88, 104))
        assert(census.crossoversPerInterface == List(14) { if (it % 2 == 0) 4 else 3 })
    }

    // --- P3, which corpus phase the simulated tile is -----------------------------------------

    @Test
    fun `P3 the design matches exactly one tile-centre phase on positions and parities`() {
        val matches = matchDesignToPhases(design, sheet)
        val exact = matches.filter { it.positionsMatch && it.paritiesMatch }
        assert(exact.map { it.phaseBasePairs }.distinct() == listOf(16))
    }

    @Test
    fun `P3 the row-end setting is immaterial at the matching phase, which is the guard inert`() {
        val exact = matchDesignToPhases(design, sheet)
            .filter { it.positionsMatch && it.paritiesMatch }
        assert(exact.size == 2)
        assert(exact.map { it.admitRowEnd }.toSet() == setOf(true, false))
    }

    @Test
    fun `P3 the matching phase is NOT 8, which is the whole finding`() {
        val exact = matchDesignToPhases(design, sheet)
            .filter { it.positionsMatch && it.paritiesMatch }
        assert(exact.none { it.phaseBasePairs == 8 })
    }

    @Test
    fun `P3 phase 0 matches the positions and inverts every parity`() {
        val zero = matchDesignToPhases(design, sheet)
            .filter { it.phaseBasePairs == 0 && it.positionsMatch }
        assert(zero.isNotEmpty())
        assert(zero.all { !it.paritiesMatch })
    }

    @Test
    fun `P3 the cheap bound predicts the matching phase, which is F4`() {
        val predicted = Math.floorMod(
            design.crossoverPhase() + phaseDatumOffsetBasePairs(design.rowBasePairs(), 16), 16
        )
        val exact = matchDesignToPhases(design, sheet)
            .first { it.positionsMatch && it.paritiesMatch }
        assert(Math.floorMod(exact.phaseBasePairs, 16) == predicted)
    }

    // --- P5, the whole 32-phase census ---------------------------------------------------------

    @Test
    fun `P5 the sweep covers all 32 phases at both row-end settings`() {
        val sweep = latticeCensusSweep(sheet, BUILDABLE_EDGE_X, 15)
        assert(sweep.size == 64)
        assert(sweep.map { it.phaseBasePairs }.distinct().size == 32)
    }

    @Test
    fun `P5 thirty of the 32 phases carry seven columns and 49, and only 8 and 24 do not`() {
        // The predicate was first written as "exactly the phases 0 and 16", and the census
        // corrected it: 7/49 is the GENERIC reading of this row and 8/56 the exceptional one.
        val sweep = latticeCensusSweep(sheet, BUILDABLE_EDGE_X, 15)
        val notFortyNine = sweep.filter { it.crossoverCount != 49 }
            .map { it.phaseBasePairs }.distinct().sorted()
        assert(notFortyNine == listOf(8, 24))
        val fortyNine = sweep.filter { it.crossoverCount == 49 }
            .map { it.phaseBasePairs }.distinct()
        assert(fortyNine.size == 30)
    }

    // --- the tie accounting: what "admit the row-end column" actually is ----------------------

    @Test
    fun `gate at phase 8 admitted every row-end tie is a raster turn, not a staple crossover`() {
        val row = latticeCensusRow(8, sheet, BUILDABLE_EDGE_X, 15, admitRowEnd = true)
        assert(row.modelledRasterTurns == 14)
        assert(row.stapleCrossovers == 42)
        assert(row.physicalTies == 56)
    }

    @Test
    fun `gate refusing the row end removes the modelled turns and not the object's own`() {
        val refused = latticeCensusRow(8, sheet, BUILDABLE_EDGE_X, 15, admitRowEnd = false)
        assert(refused.modelledRasterTurns == 0)
        assert(refused.stapleCrossovers == 42)
        assert(refused.physicalTies == 56)
        // so the binary is worth 14 modelled ties on a 56-tie object and no staple crossover
        val admitted = latticeCensusRow(8, sheet, BUILDABLE_EDGE_X, 15, admitRowEnd = true)
        assert(admitted.stapleCrossovers == refused.stapleCrossovers)
        assert(admitted.physicalTies == refused.physicalTies)
    }

    @Test
    fun `gate the simulated tile carries SEVEN more inter-duplex ties than the graded one`() {
        val simulated = latticeCensusRow(16, sheet, BUILDABLE_EDGE_X, 15, admitRowEnd = false)
        val graded = latticeCensusRow(8, sheet, BUILDABLE_EDGE_X, 15, admitRowEnd = true)
        assert(simulated.physicalTies == 63)
        assert(graded.physicalTies == 56)
        assert(simulated.physicalTies - graded.physicalTies == 7)
        assert(design.crossoverCensus().let { it.stapleCrossovers + it.scaffoldTurns } == 63)
    }

    @Test
    fun `gate the two lattices share NOT ONE staple crossover column`() {
        val graded = latticeCensusRow(8, sheet, BUILDABLE_EDGE_X, 15, admitRowEnd = true)
        val window = design.axialWindowBasePairs()
        val centre = (window.first + window.last + 1) / 2.0
        val gradedBp = graded.columnPositionsNm.map { it / Gen1Tile.RISE_PER_BASE_PAIR + centre }
        val designBp = design.crossoverCensus().columnOffsetsBasePairs.map { it.toDouble() }
        assert(gradedBp.none { g -> designBp.any { kotlin.math.abs(it - g) < 0.5 } })
    }

    @Test
    fun `P5 a column lands on the row end only at the phases C-0134 names`() {
        val sweep = latticeCensusSweep(sheet, BUILDABLE_EDGE_X, 15)
        val onEnd = sweep.filter { it.columnOnRowEnd }.map { it.phaseBasePairs }
            .distinct().sorted()
        assert(onEnd == listOf(8, 24))
    }

    // --- gate 3, symmetry ----------------------------------------------------------------------

    @Test
    fun `gate the design's column set is invariant under reflection about the row centre`() {
        val census = design.crossoverCensus()
        val reflected = census.columnOffsetsBasePairs.map { design.rowBasePairs() - it }.sorted()
        assert(reflected == census.columnOffsetsBasePairs)
    }

    @Test
    fun `gate the census of a phase reproduces the importer's own columns at zero departure`() {
        val import = design.grillageImport("T-275 census")
        val exact = matchDesignToPhases(design, sheet)
            .first { it.positionsMatch && it.paritiesMatch }
        assert(exact.worstColumnDepartureNm < 1e-9)
        assert(import.columnsAsJunctions == 7)
    }

    // --- D2, what the count is worth --------------------------------------------------------

    @Test
    fun `D2 the smeared D perpendicular density is a 49-crossover statement on this row`() {
        // D_perp = k_theta * d / p is a LINEAR DENSITY 1/p, so over one 112 bp interface it is
        // 112/32 = 3.5 crossovers, and over fourteen interfaces exactly 49.
        assert(smearedCrossoverCountOfContinuum(sheet, BUILDABLE_EDGE_X, 15).isCloseTo(49.0))
    }

    @Test
    fun `D2 the phase 8 lattice carries eight sevenths of the continuum density`() {
        val row = latticeCensusRow(8, sheet, BUILDABLE_EDGE_X, 15, admitRowEnd = true)
        val continuum = smearedCrossoverCountOfContinuum(sheet, BUILDABLE_EDGE_X, 15)
        assert((row.crossoverCount / continuum).isCloseTo(8.0 / 7.0))
    }
}
