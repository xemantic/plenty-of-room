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

package com.xemantic.nano.plentyofroom.anchoring

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.coupling.couplingSupports
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_COUNT
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_LENGTH
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PointSupport
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-126`, leaf `A8.2` — whether the arm slab clears `C-0035`'s tie-down path.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 *
 * The **strong free limiting case** this task declared is here as a test: **zero arms must
 * reproduce `C-0035`'s clearance ledger exactly** — no clash at any tie count, the full tie
 * capacity of every row, and `C-0035`'s own 326 nm² aperture floor and 5.31 nm clearance
 * unchanged. If that fails, this is not the geometry `C-0035` wrote its ledger on.
 */
class ArmSlabClearanceTest {

    private val arm = C0055_ARM_LENGTH

    private val edgeX = Gen1Tile.EDGE_X

    private val width = Gen1Tile.INTERHELICAL_SHEET

    private val duplexes = 15

    private val pitch = Gen1Tile.CROSSOVER_SPACING_SHEET_BP * Gen1Tile.RISE_PER_BASE_PAIR

    /** `C-0063`'s phase-24 placement, its roots read from that claim's own table. */
    private val phase24: List<List<Double>> = listOf(
        listOf(-16.32, -5.44, 16.32),
        listOf(0.0, 10.88),
        listOf(-16.32, 5.44, 16.32),
        listOf(0.0, 10.88),
        listOf(-16.32, 16.32),
        listOf(-10.88, 0.0),
        listOf(-16.32, 16.32),
        listOf(-10.88, 10.88),
        listOf(-16.32, 16.32),
        listOf(0.0, 10.88),
        listOf(-16.32, 16.32),
        listOf(-10.88, 0.0),
        listOf(-16.32, -5.44, 16.32),
        listOf(-10.88, 0.0),
        listOf(-16.32, 5.44, 16.32)
    )

    // ------------------------------------------------------------------ gate 1 — dimensional

    @Test
    fun `gate 1 - the arm slab is 1_69 to 3_69 above the sheet's own mid-plane`() {
        val slab = armSlabBand(0.0)
        assert(slab.low.isCloseTo(1.69))
        assert(slab.high.isCloseTo(3.69))
        assert(slab.thickness.isCloseTo(2.0))
        assert(sheetBand().low.isCloseTo(-1.0))
        assert(sheetBand().high.isCloseTo(1.0))
    }

    @Test
    fun `gate 1 - the whole plan verdict is invariant under a common rescaling of every length`() {
        val columns = gridColumns(3, edgeX)
        val plain = interleaveRow(0, phase24[0], arm, edgeX, width, columns)
        val scale = 10.0
        val scaled = interleaveRow(
            0, phase24[0].map { it * scale }, arm * scale, edgeX * scale, width * scale,
            columns.map { it * scale }
        )
        assert(scaled.gridClashes == plain.gridClashes)
        assert(scaled.maximumTies == plain.maximumTies)
        assert(scaled.freeIntervals.size == plain.freeIntervals.size)
        assert(scaled.freeLength.isCloseTo(plain.freeLength * scale))
    }

    @Test
    fun `gate 1 - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { SectionBand(3.0, 1.0) }
        assertFailsWith<IllegalArgumentException> { PlanInterval(3.0, 1.0) }
        assertFailsWith<IllegalArgumentException> { armSlabBand(-1.0) }
        assertFailsWith<IllegalArgumentException> { armPlanReach(-1.0, 0.0) }
        // an arm cannot deliver a stroke longer than itself: it is a rotation, not a translation
        assertFailsWith<IllegalArgumentException> { armPlanReach(arm, 10.0) }
        assertFailsWith<IllegalArgumentException> { tieClearColumn(0.5) }
        assertFailsWith<IllegalArgumentException> { gridColumns(0, edgeX) }
        assertFailsWith<IllegalArgumentException> { tiesFitting(emptyList(), 0.0) }
        // a root outside the tile, and a row whose arms cannot be given any direction at all
        assertFailsWith<IllegalArgumentException> {
            interleaveRow(0, listOf(0.0, 1.0, 2.0), arm, edgeX, width, gridColumns(3, edgeX))
        }
    }

    // ------------------------------------------------------------------ gate 2 — limiting cases

    @Test
    fun `gate 2 - ZERO ARMS reproduces C-0035's clearance ledger exactly`() {
        listOf(1, 2, 3).forEach { m ->
            val row = interleaveRow(0, emptyList(), arm, edgeX, width, gridColumns(m, edgeX))
            assert(row.gridClashes == 0)
            assert(row.freeLength.isCloseTo(edgeX))
            assert(row.maximumTies == 14)
        }
        // and C-0035's own ledger, recomputed through its own library rather than retyped
        assert(tieApertureArea(45).isCloseTo(325.6245))
        assert(midspanClearance(8.0).isCloseTo(5.31))
        assert(midspanPenetration(3.0, 8.0).isCloseTo(0.0))
        assert(midspanPenetration(10.0, 8.0).isCloseTo(4.69))
    }

    @Test
    fun `gate 2 - the swept plan envelope IS the rest footprint - the projection is a cosine`() {
        val a = SlabArm(0, -16.32, true)
        val rest = armInterval(a, arm, 0.0)
        listOf(0.5, 1.0, 2.0, 3.0).forEach { stroke ->
            val swept = sweptArmInterval(a, arm, stroke)
            assert(swept.low.isCloseTo(rest.low))
            assert(swept.high.isCloseTo(rest.high))
            assert(armInterval(a, arm, stroke).length < rest.length)
        }
        assert(armPlanReach(arm, 3.0).isCloseTo(sqrt(arm * arm - 9.0)))
        assert(armPlanReach(arm, 0.0).isCloseTo(arm))
    }

    @Test
    fun `gate 2 - the interleave is a per-row problem - different rows never interact`() {
        val here = interleaveRow(0, listOf(0.0), arm, edgeX, width, listOf(0.0))
        assert(here.gridClashes == 1)
        val elsewhere = interleaveRow(1, listOf(0.0), arm, edgeX, width, listOf(-15.0))
        assert(elsewhere.gridClashes == 0)
    }

    @Test
    fun `gate 2 - a gap shorter than a duplex admits no tie and a double one admits two`() {
        assert(tiesFitting(listOf(PlanInterval(0.0, 2.0)), width) == 0)
        assert(tiesFitting(listOf(PlanInterval(0.0, width)), width) == 1)
        assert(tiesFitting(listOf(PlanInterval(0.0, 2.0 * width)), width) == 2)
    }

    // ---------------------------------------------- gate 3 — symmetry, conservation and section

    @Test
    fun `gate 3 - the section makes a plan overlap LEVEL-INDEPENDENT`() {
        listOf(0.0, 1.0, 3.0).forEach { stroke ->
            val slab = armSlabBand(stroke)
            val column = tieClearColumn(slab.high + 1.0)
            assert(column.contains(slab))
            assert(!tieMayPassOverSlab(slab, column))
        }
        // the theorem's own falsifier: a body ABOVE the column can be passed under
        assert(tieMayPassOverSlab(SectionBand(20.0, 22.0), tieClearColumn(6.0)))
    }

    @Test
    fun `gate 3 - the free intervals and the arms partition the row exactly`() {
        phase24.forEachIndexed { row, roots ->
            val interleave = interleaveRow(row, roots, arm, edgeX, width, gridColumns(3, edgeX))
            val covered = interleave.armIntervals.sumOf { it.length }
            assert((covered + interleave.freeLength).isCloseTo(edgeX))
            assert(interleave.armIntervals.size == roots.size)
        }
    }

    @Test
    fun `gate 3 - every arm lies inside the footprint and no two arms of a row overlap`() {
        phase24.forEachIndexed { row, roots ->
            val interleave = interleaveRow(row, roots, arm, edgeX, width, gridColumns(3, edgeX))
            interleave.armIntervals.forEach {
                assert(it.low >= -edgeX / 2.0 - 1e-9)
                assert(it.high <= edgeX / 2.0 + 1e-9)
            }
            interleave.armIntervals.sortedBy { it.low }.zipWithNext().forEach { (a, b) ->
                assert(b.low >= a.high - 1e-9)
            }
        }
    }

    @Test
    fun `gate 3 - the direction enumeration agrees with C-0063's own armDirections`() {
        phase24.forEach { roots ->
            val greedy = armDirections(roots, arm, edgeX, width)
            assert(greedy != null)
            assert(feasibleRowDirections(roots, arm, edgeX, width).contains(greedy))
        }
    }

    // ------------------------------------------------------------------ gate 4 — convergence

    @Test
    fun `gate 4 - the swept envelope is sample-count independent`() {
        val a = SlabArm(0, -16.32, true)
        val coarse = sweptArmInterval(a, arm, 3.0, 8)
        val fine = sweptArmInterval(a, arm, 3.0, 4096)
        assert(coarse.low.isCloseTo(fine.low))
        assert(coarse.high.isCloseTo(fine.high))
    }

    @Test
    fun `gate 4 - the clearing-offset sweep converges in measure`() {
        val coarse = clearingGridOffsets(phase24, arm, edgeX, width, 1, 4001).sumOf { it.length }
        val fine = clearingGridOffsets(phase24, arm, edgeX, width, 1, 40001).sumOf { it.length }
        assert(fine > 0.0)
        assert(abs(coarse - fine) / fine < 0.05)
    }

    // --------------------------------------------- gate 5 — literature and upstream cross-check

    @Test
    fun `gate 5 - the root pitch minus the arm is the only gap the lattice offers`() {
        assert(pitch.isCloseTo(10.88))
        assert(arm.isCloseTo(8.16439083))
        val gap = pitch - arm
        assert(gap.isCloseTo(2.71560917))
        // it clears a 2.69 nm duplex and does NOT clear the 2.73 nm square-lattice one
        assert(gap > Gen1Tile.INTERHELICAL_SHEET)
        assert(gap < 2.73)
    }

    @Test
    fun `gate 5 - C-0063's placement carries 34 arms at 46 percent of the plan`() {
        assert(phase24.sumOf { it.size } == C0055_ARM_COUNT)
        val fraction = phase24.sumOf { it.size } * arm * width / (edgeX * duplexes * width)
        assert(fraction.isCloseTo(0.4626, relativeTolerance = 1e-3))
    }

    @Test
    fun `gate 5 - C-0015's three-column grid sits at minus 13_333, 0 and plus 13_333`() {
        val columns = gridColumns(3, edgeX)
        assert(columns[0].isCloseTo(-40.0 / 3.0))
        assert(columns[1].isCloseTo(0.0))
        assert(columns[2].isCloseTo(40.0 / 3.0))
    }

    // ------------------------------------------------------------------ the findings themselves

    @Test
    fun `the tie-down path does NOT clear the arm slab on C-0015's own grid`() {
        val greedy = totalGridClashes(phase24, arm, edgeX, width, gridColumns(3, edgeX), false)
        val best = totalGridClashes(phase24, arm, edgeX, width, gridColumns(3, edgeX), true)
        assert(greedy == 30)
        assert(best == 26)
    }

    @Test
    fun `a one and a two column grid fare no better - 10 of 15 and 24 of 30`() {
        assert(totalGridClashes(phase24, arm, edgeX, width, gridColumns(1, edgeX), true) == 10)
        assert(totalGridClashes(phase24, arm, edgeX, width, gridColumns(2, edgeX), true) == 24)
    }

    @Test
    fun `no rigid offset of a two or three column grid clears every row`() {
        assert(clearingGridOffsets(phase24, arm, edgeX, width, 3, 40001).isEmpty())
        assert(clearingGridOffsets(phase24, arm, edgeX, width, 2, 40001).isEmpty())
        val one = clearingGridOffsets(phase24, arm, edgeX, width, 1, 400001)
        assert(one.size == 4)
        assert(one.minOf { minOf(abs(it.low), abs(it.high) ) }.isCloseTo(6.785, 1e-3))
        assert(one.maxOf { it.length }.isCloseTo(0.99, 1e-3))
    }

    @Test
    fun `the room is there - the free tie capacity is far above the demand`() {
        val capacity = phase24.mapIndexed { row, roots ->
            interleaveRow(row, roots, arm, edgeX, width, gridColumns(3, edgeX), true).maximumTies
        }
        assert(capacity.sum() == 108)
        assert(capacity.min() >= 3)
    }

    @Test
    fun `the arms' OWN tip links clear every other arm in their row`() {
        val worst = worstTipClearance(phase24, arm, edgeX, width)
        assert(worst > width / 2.0)
        assert(worst.isCloseTo(pitch - arm))
    }

    @Test
    fun `the escape exists - 45 snapped tie stations clear every arm`() {
        val stations = snappedTieStations(
            phase24, arm, edgeX, width, gridColumns(3, edgeX), Gen1Tile.INTERHELICAL_SHEET, true
        )
        assert(stations != null)
        assert(stations!!.size == 45)
        phase24.forEachIndexed { row, roots ->
            val y = (row - (duplexes - 1) / 2.0) * Gen1Tile.INTERHELICAL_SHEET
            val interleave =
                interleaveRow(row, roots, arm, edgeX, width, gridColumns(3, edgeX), true)
            stations.filter { abs(it.second - y) < 1e-9 }.forEach { (x, _) ->
                val tie = PlanInterval(x - width / 2.0, x + width / 2.0)
                assert(interleave.armIntervals.none { it.overlaps(tie) })
                assert(abs(x) <= edgeX / 2.0 + 1e-9)
            }
        }
        // and they are displaced from C-0015's own columns, which is the whole price
        val displacement = stations.mapIndexed { index, (x, _) ->
            abs(x - gridColumns(3, edgeX)[index % 3])
        }.max()
        assert(displacement.isCloseTo(4.331667, relativeTolerance = 1e-5))
    }

    // ---------------------------------------- gate 5 — the flatness pipeline, reproduced

    private val sheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )

    private val lengthY = duplexes * Gen1Tile.INTERHELICAL_SHEET

    private val mandate = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

    /** `C-0022`'s solved edge profile at 2 mM, a 10 nm gap and 0.192 V — its own numbers. */
    private val solvedLoad = edgeCollarPressure(
        Gen1Tile.TARGET_FORCE / (edgeX * lengthY), edgeX, lengthY,
        listOf(CollarTerm(-0.302930, 8.939), CollarTerm(-0.593880, 1.0))
    )

    private val freeStroke = PlateOnFoundation(
        sheet.plate(edgeX, lengthY), Gen1Tile.FOUNDATION_SECANT, emptyList(), basisDegree = 12
    ).solve(uniformPressure(Gen1Tile.TARGET_FORCE / (edgeX * lengthY))).meanDeflection

    private fun lattice(
        supports: List<PointSupport> = emptyList(),
        columns: CrossoverLayout = CrossoverLayout.centred(8, sheet.crossoverSpacing / 2.0)
    ) = OrigamiGrillage(
        sheet = sheet,
        lengthX = edgeX,
        beamCount = duplexes,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = columns,
        subdivisions = 2,
        supports = supports
    )

    @Test
    fun `gate 5 - C-0063's own 34 roots dish 0_0706 of the stroke on their phase-24 host`() {
        val stations = phase24.flatMapIndexed { row, roots ->
            val y = (row - (duplexes - 1) / 2.0) * Gen1Tile.INTERHELICAL_SHEET
            roots.map { it to y }
        }
        assert(stations.size == 34)
        val dishing = lattice(
            couplingSupports(stations, mandate),
            CrossoverLayout.atBasePairPhase(24, sheet, edgeX)
        ).solve(solvedLoad).peakDishing() / freeStroke
        assert(dishing.isCloseTo(0.0706, relativeTolerance = 3e-3))
    }

    @Test
    fun `the escape is nearly free in flatness - the snapped 45 dish 0_2219 against 0_2182`() {
        val snapped = snappedTieStations(
            phase24, arm, edgeX, width, gridColumns(3, edgeX), Gen1Tile.INTERHELICAL_SHEET, true
        )!!
        val dishing = lattice(
            couplingSupports(snapped, mandate),
            CrossoverLayout.atBasePairPhase(24, sheet, edgeX)
        ).solve(solvedLoad).peakDishing() / freeStroke
        assert(dishing.isCloseTo(0.221863, relativeTolerance = 1e-4))
        // 1.7 % worse than the grid it was displaced from, and neither is flat
        val grid = lattice(couplingSupports(attachmentGrid(3, duplexes, edgeX, lengthY), mandate))
            .solve(solvedLoad).peakDishing() / freeStroke
        assert(dishing / grid < 1.02)
        assert(dishing > 0.10)
        assert(grid > 0.10)
    }

    @Test
    fun `gate 5 - C-0058's 3 x 15 grid dishes 0_2182 and the free tile 0_3079`() {
        val grid = attachmentGrid(3, duplexes, edgeX, lengthY)
        val dishing = lattice(couplingSupports(grid, mandate))
            .solve(solvedLoad).peakDishing() / freeStroke
        assert(dishing.isCloseTo(0.2182, relativeTolerance = 3e-3))
        val free = lattice().solve(solvedLoad).peakDishing() / freeStroke
        assert(free.isCloseTo(0.3079, relativeTolerance = 3e-3))
    }
}
