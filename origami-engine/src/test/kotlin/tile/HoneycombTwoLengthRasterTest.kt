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
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-235` — the coupled cells at `C-0140`'s two-length raster.
 *
 * `C-0142` graded at a uniform **112 bp** row; `C-0140` shows no uniform honeycomb row length
 * exists at all and recommends **112 / 108 bp**, whose block extent is 116 bp = 39.44 nm.
 *
 * What is new here is only the two-length raster's own axial geometry — which rooting helix
 * carries which length, where each one's window sits on the global `z`, and the station lattice
 * that follows. Everything else is consumed from `C-0140` (`honeycombXRasterPath`,
 * `honeycombRasterTurns`) and `C-0141` (`honeycombStationLattice`) unmodified.
 */
class HoneycombTwoLengthRasterTest {

    private val rise = Gen1Tile.RISE_PER_BASE_PAIR

    // --- gate 1: dimensional consistency -----------------------------------------------------

    @Test
    fun `a two-length raster carries base pairs and the lattice it makes carries nm`() {
        val raster = twoLengthRaster(10, 6, 112, 108)
        // Every window is an integer base-pair pair, and the extent is an integer count.
        assert(raster.blockExtentBasePairs == 116)
        raster.faceRows.forEach { row ->
            assert(row.lengthBasePairs == row.highBasePairs - row.lowBasePairs)
            assert(row.lengthBasePairs == 112 || row.lengthBasePairs == 108)
        }
        // The lattice is in nm and centred: its extreme stations straddle zero.
        val lattice = raster.stationLattice(basePhaseBasePairs = 0, interRowOffsetBasePairs = 14)
        val xs = lattice.flatten()
        assert(xs.min() < 0.0)
        assert(xs.max() > 0.0)
        assert(xs.max() - xs.min() < raster.blockExtentBasePairs * rise)
    }

    @Test
    fun `the raster refuses a non-positive length and an odd row width`() {
        assertFailsWith<IllegalArgumentException> { twoLengthRaster(10, 6, 0, 108) }
        assertFailsWith<IllegalArgumentException> { twoLengthRaster(10, 6, 112, -1) }
        assertFailsWith<IllegalArgumentException> { twoLengthRaster(10, 5, 112, 108) }
    }

    // --- gate 2: limiting cases --------------------------------------------------------------

    @Test
    fun `equal lengths reduce EXACTLY to C-0141's single-length station lattice`() {
        // The whole generalisation must be a generalisation: with both senses at 112 bp every
        // helix spans [-112, 0], the block extent is 112, and the lattice is position-for-position
        // `honeycombStationLattice`. That is `F2`.
        val raster = twoLengthRaster(10, 6, 112, 112)
        assert(raster.blockExtentBasePairs == 112)
        listOf(0, 3, 7, 14, 20).forEach { phase ->
            listOf(7, 14).forEach { offset ->
                val mine = raster.stationLattice(phase, offset)
                val theirs = honeycombStationLattice(10, 112, phase, offset)
                assert(mine.size == theirs.size)
                mine.indices.forEach { row ->
                    assert(mine[row].size == theirs[row].size)
                    mine[row].indices.forEach { k ->
                        assert(mine[row][k].isCloseTo(theirs[row][k]))
                    }
                }
            }
        }
    }

    @Test
    fun `a one-row raster has no row turn, one sense, and NO face helix with a defined sense`() {
        // `C-0140`'s only constant-sense raster is the one-row one, and its `+x` face helix is
        // the terminus of the scaffold path — so the row-parity extrapolation has nothing to
        // extrapolate from and refuses rather than inventing a length.
        val one = twoLengthRaster(1, 6, 112, 108)
        assert(one.senseCounts.toList().count { it > 0 } == 1)
        assert(one.blockExtentBasePairs == 112 || one.blockExtentBasePairs == 108)
        assertFailsWith<IllegalArgumentException> { one.faceRows }
    }

    // --- gate 3: symmetry and the lattice's own structure -------------------------------------

    @Test
    fun `the 10 x 6 face alternates sense with the row parity, so it is still a two-phase lattice`() {
        // This is the cheap bound that decides whether `C-0141`'s two-phase station lattice
        // survives at all: if the face's lengths did not alternate with the row index there
        // would be no phase variable left (`C-0136`'s finding, in a new place).
        val raster = twoLengthRaster(10, 6, 112, 108)
        // The `+x` face `C-0141` censuses is the LAST helix of an even row and the FIRST of an
        // odd one, because a raster runs left to right and then right to left. Pin the indices:
        // the two of a row turn are consecutive on the path, and they are the face pair.
        assert(raster.faceRows.map { it.pathIndex } == listOf(5, 6, 17, 18, 29, 30, 41, 42, 53, 54))
        raster.faceRows.forEachIndexed { row, it ->
            assert(it.effectiveSense == if (row % 2 == 0) 1 else 2)
            assert(it.lengthBasePairs == if (row % 2 == 0) 112 else 108)
            assert(it.lowBasePairs == -112)
            assert(it.highBasePairs == if (row % 2 == 0) 0 else -4)
        }
    }

    @Test
    fun `every raster ROW is still 112 bp and the block is 116 only because the rows are OFFSET`() {
        // The finding that splits the width question in two: the block's extent exceeds every
        // row's own span, so a smeared plate is owed either the bounding box or the row length
        // and they are not the same tile.
        val raster = twoLengthRaster(10, 6, 112, 108)
        assert(raster.blockExtentBasePairs == 116)
        raster.rowSpans.forEach { assert(it.second - it.first == 112) }
        val offsets = raster.rowSpans.map { it.first }.toSet()
        assert(offsets == setOf(-112, -116))
    }

    @Test
    fun `the station census is row-dependent and the two admissible offsets now DISAGREE`() {
        // `C-0141` records that "no answer here depends on the choice" of the 7 or 14 bp
        // inter-row offset. At a two-length raster that stops being true.
        val raster = twoLengthRaster(10, 6, 112, 108)
        val atSeven = raster.stationLattice(0, 7).map { it.size }
        val atFourteen = raster.stationLattice(0, 14).map { it.size }
        assert(atSeven == listOf(5, 6, 5, 6, 5, 6, 5, 6, 5, 6))
        assert(atFourteen == listOf(5, 5, 5, 5, 5, 5, 5, 5, 5, 5))
        assert(atSeven.sum() == 55)
        assert(atFourteen.sum() == 50)
        // Over the whole 21 x 2 sweep exactly ONE (phase, offset) pair keeps all sixty
        // stations, where the uniform 112 bp raster keeps them at C-0142's own phase 0 / 7 bp.
        val full = (0 until 21).flatMap { base ->
            listOf(7, 14).map { off -> Triple(base, off, raster.stationLattice(base, off)) }
        }.filter { (_, _, l) -> l.sumOf { it.size } == 60 }
        assert(full.size == 1)
        assert(full.single().first == 11 && full.single().second == 14)
    }

    @Test
    fun `snapping onto the two-length lattice moves a station by at most half the ladder pitch`() {
        val raster = twoLengthRaster(10, 6, 112, 108)
        val edgeY = HoneycombBlock(10, 6).plateEdgeY
        val half = 0.5 * HoneycombLattice.SAME_PAIR_PERIOD_BP * rise
        listOf(1, 2, 3, 5).forEach { columns ->
            val grid = twoLengthSnappedGrid(raster, columns, edgeY, 0, 7)
            assert(grid.size == columns * 10)
            val abstract = com.xemantic.nano.plentyofroom.coupling.attachmentGrid(
                columns, 10, raster.blockExtentBasePairs * rise, edgeY
            )
            assert(alongHelixDeparture(abstract, grid) < half + 1e-9)
            // and `y` is untouched: a station row IS a rooting helix.
            grid.indices.forEach { assert(abs(grid[it].second - abstract[it].second) < 1e-12) }
        }
    }

    @Test
    fun `a placement wider than the sparsest row's ladder is REFUSED, not snapped`() {
        // At the 7 bp offset half the rows carry five stations, so six columns is a change of
        // the path COUNT wearing a change of position. That is `F4`.
        val raster = twoLengthRaster(10, 6, 112, 108)
        val edgeY = HoneycombBlock(10, 6).plateEdgeY
        assertFailsWith<IllegalArgumentException> { twoLengthSnappedGrid(raster, 6, edgeY, 0, 7) }
        assertFailsWith<IllegalArgumentException> { twoLengthSnappedGrid(raster, 6, edgeY, 0, 14) }
        // and at the one station-saturating pair the same six columns are admitted
        assert(twoLengthSnappedGrid(raster, 6, edgeY, 11, 14).size == 60)
    }

    // --- gate 4: exactness over whole families -------------------------------------------------

    @Test
    fun `every raster row spans max of the two lengths, at EVERY pair C-0140 tabulates`() {
        // The generalisation that makes the width finding robust to which pair is selected:
        // a block's extent exceeds every one of its rows' own spans by exactly the STAGGER,
        // so no two-length raster lengthens a row at all.
        listOf(112 to 108, 101 to 109, 102 to 109, 112 to 109, 122 to 119).forEach { (a, b) ->
            listOf(10 to 6, 15 to 4).forEach { (m, n) ->
                val raster = twoLengthRaster(m, n, a, b)
                val spans = raster.rowSpans.map { it.second - it.first }.toSet()
                assert(spans.size == 1)
                assert(spans.single() == maxOf(a, b))
                assert(raster.blockExtentBasePairs == maxOf(a, b) + abs(a - b))
            }
        }
    }

    @Test
    fun `the block extent is 116 bp at BOTH published 60-helix cross-sections`() {
        assert(twoLengthRaster(15, 4, 112, 108).blockExtentBasePairs == 116)
        assert(twoLengthRaster(10, 6, 112, 108).blockExtentBasePairs == 116)
    }

    @Test
    fun `the sense census reproduces C-0140 at both cross-sections`() {
        assert(twoLengthRaster(15, 4, 112, 108).senseCounts == 28 to 30)
        assert(twoLengthRaster(10, 6, 112, 108).senseCounts == 29 to 29)
    }

    @Test
    fun `an ODD raster-row count puts a PATH END on the face, and it is reported`() {
        // `15 × 4`'s row 14 face helix is the terminus of the scaffold path, whose sense
        // `C-0140` leaves undefined. It must be flagged rather than silently assigned.
        val odd = twoLengthRaster(15, 4, 112, 108)
        assert(odd.faceRows.count { !it.senseIsDefined } == 1)
        assert(!odd.faceRows.last().senseIsDefined)
        assert(twoLengthRaster(10, 6, 112, 108).faceRows.all { it.senseIsDefined })
        // and it is filled in from a face row of its OWN parity, never from its neighbour:
        // row 14 is even, so it must carry exactly what row 0 carries.
        val even = odd.faceRows.first()
        val filled = odd.faceRows.last()
        assert(filled.effectiveSense == even.effectiveSense)
        assert(filled.lengthBasePairs == even.lengthBasePairs)
        assert(filled.lowBasePairs == even.lowBasePairs)
        assert(filled.highBasePairs == even.highBasePairs)
    }

    // --- gate 5: the crossover-column count against the numerical guard -------------------------

    @Test
    fun `the crossover column count at the wider edgeX sits within 0-07 nm of EDGE_MARGIN`() {
        // `CLAUDE.md`: a numerical guard becomes a physical assertion the moment the lattice
        // lands on it. At 116 bp the usable span clears eleven pitches by 0.07 nm, so the count
        // is 12 at the default guard and 11 at half a rise. That is `F5`, and it is a sweep.
        val pitch = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP * rise / 2.0
        assert(crossoverColumnCount(112 * rise, 0.05, pitch) == 11)
        assert(crossoverColumnCount(116 * rise, 0.05, pitch) == 12)
        assert(crossoverColumnCount(116 * rise, 0.5 * rise, pitch) == 11)
        assert(crossoverColumnCount(116 * rise, rise, pitch) == 11)
        val slack = 116 * rise - 2.0 * 0.05 - 11 * pitch
        assert(slack > 0.0 && slack < 0.08)
    }
}
