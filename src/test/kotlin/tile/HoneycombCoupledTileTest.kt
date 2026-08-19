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
import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-232` — re-grading `C-0118`'s coupled cells at the corrected honeycomb cross-section.
 *
 * Two things are new here and both are small. The **lattice-snapped** placement, which is what
 * turns a path count from a *request* into a set of stations the honeycomb face actually
 * carries; and a **paired per-realisation** comparator, because `CLAUDE.md` records that a
 * ratio of two order statistics is not the order statistic of the ratio.
 *
 * Everything geometric is consumed from `C-0141`'s `HoneycombFaceLattice` rather than
 * re-derived — re-deriving it is how two claims end up disagreeing.
 */
class HoneycombCoupledTileTest {

    private val d = Gen1Tile.INTERHELICAL_HONEYCOMB

    // --- gate 1: the cheap bound, as an identity rather than as prose -------------------------

    @Test
    fun `the attachment pitch across the helices IS the in-plane row pitch, at every m`() {
        // `attachmentGrid` spreads `rows` stations over `edgeY`, so the across-helix attachment
        // pitch is `edgeY / rasterRows` identically. At the honeycomb geometry that is `3d/2`
        // and at the standing one it is `d` — the 1.5× is the whole of the correction, and it
        // reaches the coupling through this one quotient.
        for (m in listOf(3, 10, 15, 20)) {
            val block = HoneycombBlock(m, 4, d)
            assert((block.plateEdgeY / m).isCloseTo(HoneycombCrossSectionGeometry.rowPitch(d)))
            assert((block.plateEdgeY / m).isCloseTo(1.5 * d))
        }
    }

    @Test
    fun `the plate edgeY and the block ENVELOPE are two different numbers`() {
        // `C-0141` quotes both, and they are 2.0 nm — one duplex diameter — apart. The plate this
        // study solves is `rasterRows × the in-plane pitch`, which is the CENTRE-to-centre
        // convention the grillage's own `lengthY = beamCount × interhelicalDistance` uses; the
        // envelope is the physical body. Substituting one for the other is a whole duplex.
        val fifteenByFour = HoneycombBlock(15, 4, d)
        val tenBySix = HoneycombBlock(10, 6, d)
        assert(fifteenByFour.plateEdgeY.isCloseTo(57.06))
        assert(tenBySix.plateEdgeY.isCloseTo(38.04))
        assert(fifteenByFour.envelopeY.isCloseTo(56.524))
        assert(tenBySix.envelopeY.isCloseTo(37.504))
        // And the corrected `10 × 6` carries EXACTLY the plate `edgeY` the corpus attributed to
        // `15 × 4` at the standing geometry, because `10 × 1.5 = 15`. That is why the footprint
        // ordering reverses rather than merely shifting.
        assert(tenBySix.plateEdgeY.isCloseTo(15 * d))
    }

    // --- gate 2: the snapped grid stands on the honeycomb face's own stations -----------------

    @Test
    fun `every snapped station is a station of its own row's ladder`() {
        val rows = 10
        val rowBp = 112
        val edgeY = HoneycombBlock(rows, 6, d).plateEdgeY
        val lattice = honeycombStationLattice(rows, rowBp, 0, 7)
        val grid = honeycombSnappedGrid(3, rows, rowBp, edgeY, 0, 7)
        assert(grid.size == 3 * rows)
        grid.forEachIndexed { index, (x, _) ->
            val row = index / 3
            assert(lattice[row].any { abs(it - x) < 1e-9 })
        }
    }

    @Test
    fun `a snapped grid carries the same row positions as the abstract grid`() {
        val rows = 15
        val edgeY = HoneycombBlock(rows, 4, d).plateEdgeY
        val abstract = attachmentGrid(2, rows, 112 * Gen1Tile.RISE_PER_BASE_PAIR, edgeY)
        val snapped = honeycombSnappedGrid(2, rows, 112, edgeY, 0, 7)
        assert(abstract.size == snapped.size)
        // Only the ALONG-helix coordinate is snapped: the rows ARE the rooting helices, so the
        // across-helix coordinate is fixed by the lattice and there is nothing to choose.
        abstract.indices.forEach { assert(abstract[it].second.isCloseTo(snapped[it].second)) }
        // And the stations of one row stay distinct — a snap that collided two paths would be a
        // change of path COUNT wearing a change of position.
        (0 until rows).forEach { row ->
            val xs = (0 until 2).map { snapped[row * 2 + it].first }
            assert(xs.toSet().size == 2)
        }
    }

    @Test
    fun `the abstract grid is NOT on the lattice, and the departure is a length`() {
        val rows = 10
        val edgeY = HoneycombBlock(rows, 6, d).plateEdgeY
        val edgeX = 112 * Gen1Tile.RISE_PER_BASE_PAIR
        val abstract = attachmentGrid(1, rows, edgeX, edgeY)
        val snapped = honeycombSnappedGrid(1, rows, 112, edgeY, 0, 7)
        val departure = alongHelixDeparture(abstract, snapped)
        // A single-column abstract grid stands at x = 0 on every row; the 21 bp ladder at phase 0
        // has no station there, so the departure is strictly positive and is a real distance —
        // and it can never exceed half the ladder pitch, which is what makes it a bound.
        assert(departure > 0.1)
        assert(departure < 21 * Gen1Tile.RISE_PER_BASE_PAIR / 2.0 + 1e-9)
        assert(departure.isCloseTo(7.0 * Gen1Tile.RISE_PER_BASE_PAIR))
        assert(alongHelixDeparture(snapped, snapped).isCloseTo(0.0))
    }

    @Test
    fun `a placement wider than the ladder cannot be snapped and is refused`() {
        val rows = 10
        val edgeY = HoneycombBlock(rows, 6, d).plateEdgeY
        // Seven columns on a six-station ladder must collide, whatever the phase.
        assertFailsWith<IllegalArgumentException> {
            honeycombSnappedGrid(7, rows, 112, edgeY, 0, 7)
        }
        assertFailsWith<IllegalArgumentException> {
            honeycombSnappedGrid(0, rows, 112, edgeY, 0, 7)
        }
    }

    // --- gate 3: the paired comparator ---------------------------------------------------------

    @Test
    fun `the median of the ratios is not the ratio of the medians`() {
        // Constructed so the two disagree: the pairing matters, and the whole point of a common
        // stream is that it is available.
        val corrected = doubleArrayOf(1.0, 8.0, 3.0, 4.0, 5.0)
        val standing = doubleArrayOf(1.0, 1.0, 3.0, 4.0, 1.0)
        val paired = pairedRatioSummary(corrected, standing)
        // ratios: 1, 8, 1, 1, 5 -> sorted 1, 1, 1, 5, 8 -> nearest-rank median is 1.0
        assert(paired.median.isCloseTo(1.0))
        // medians: 4.0 and 3.0 -> 1.3333..., which is a different number entirely.
        assert(!paired.median.isCloseTo(4.0 / 3.0))
        assert(paired.realisations == 5)
    }

    @Test
    fun `a sample compared against itself is exactly one at every percentile`() {
        val sample = doubleArrayOf(0.7, 3.1, 0.02, 11.0, 5.5, 0.9)
        val paired = pairedRatioSummary(sample, sample)
        assert(paired.median == 1.0)
        assert(paired.p90 == 1.0)
        assert(paired.worst == 1.0)
        assert(paired.best == 1.0)
    }

    @Test
    fun `a paired comparison refuses mismatched samples and a zero denominator`() {
        assertFailsWith<IllegalArgumentException> {
            pairedRatioSummary(doubleArrayOf(1.0, 2.0), doubleArrayOf(1.0))
        }
        assertFailsWith<IllegalArgumentException> {
            pairedRatioSummary(doubleArrayOf(1.0), doubleArrayOf(0.0))
        }
        assertFailsWith<IllegalArgumentException> {
            pairedRatioSummary(DoubleArray(0), DoubleArray(0))
        }
    }

    // --- gate 4: the station inventory, which decides whether a cell exists at all -------------

    @Test
    fun `a 112 bp row carries six stations per rooting helix on the 21 bp ladder`() {
        assert(honeycombLadderIndices(112, 0).size == 6)
        assert(honeycombStationLattice(15, 112, 0, 7).sumOf { it.size } == 90)
        assert(honeycombStationLattice(10, 112, 0, 7).sumOf { it.size } == 60)
    }
}
