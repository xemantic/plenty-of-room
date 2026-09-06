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
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-203` — what attachment lattice does a honeycomb block's top face offer?
 *
 * Every plan ceiling, station lattice, crossover phase and placement in this corpus is
 * **single-layer square-lattice**: four azimuths at 8 bp, the same pair every 32 bp. The honeycomb
 * is **three azimuths at 7 bp**, the same pair every **21 bp** — read directly from Douglas et al.
 * by `C-0119` — and nobody had counted what that offers.
 *
 * These tests pin the lattice arithmetic before any census is run.
 */
class HoneycombStationLatticeTest {

    // --- gate 1: the azimuths, and the sublattice that decides which one points out -------------

    @Test
    fun `a honeycomb helix has three azimuths at 120 degrees`() {
        assert(HoneycombLattice.AZIMUTHS == 3)
        assert(HoneycombLattice.azimuthSeparationDegrees().isCloseTo(120.0))
    }

    @Test
    fun `the two sublattices point their free azimuth opposite ways`() {
        // caDNAno's honeycomb alternates helix orientation, so a top-row helix either has an
        // azimuth pointing straight OUT of the slab or two pointing obliquely out of it. Which one
        // is a property of the sublattice, not of the design.
        assert(HoneycombLattice.pointsDirectlyOut(row = 0, column = 0) !=
                HoneycombLattice.pointsDirectlyOut(row = 0, column = 1))
        // And it alternates along the row, which is what makes the census a parity question.
        assert(HoneycombLattice.pointsDirectlyOut(row = 0, column = 0) ==
                HoneycombLattice.pointsDirectlyOut(row = 0, column = 2))
    }

    // --- gate 2: the along-helix period -------------------------------------------------------

    @Test
    fun `crossover positions for ONE azimuth recur every 21 base pairs`() {
        assert(HoneycombLattice.SAME_PAIR_PERIOD_BP == 21)
        // And the three azimuths together give a position every 7 bp, which is the number the
        // source states and the one a reader is likely to mistake for the per-azimuth period.
        assert(HoneycombLattice.ANY_AZIMUTH_STEP_BP == 7)
        assert(HoneycombLattice.SAME_PAIR_PERIOD_BP ==
                HoneycombLattice.ANY_AZIMUTH_STEP_BP * HoneycombLattice.AZIMUTHS)
    }

    @Test
    fun `the stations on one helix are the 21 bp ladder inside the row`() {
        // A 112 bp row: positions at phase + 21k that fall inside it.
        assert(honeycombStationsOnHelix(rowBasePairs = 112, phaseBasePairs = 0) == 6)
        assert(honeycombStationsOnHelix(rowBasePairs = 112, phaseBasePairs = 20) == 5)
        // A row shorter than one period carries at most one.
        assert(honeycombStationsOnHelix(rowBasePairs = 20, phaseBasePairs = 0) == 1)
        assert(honeycombStationsOnHelix(rowBasePairs = 5, phaseBasePairs = 10) == 0)
    }

    @Test
    fun `a negative or zero row is refused`() {
        assertFailsWith<IllegalArgumentException> { honeycombStationsOnHelix(0, 0) }
        assertFailsWith<IllegalArgumentException> { honeycombStationsOnHelix(112, -1) }
    }

    // --- gate 3: the census, and the conservation it must satisfy -------------------------------

    @Test
    fun `the census counts only the TOP layer, because a buried helix has no free azimuth`() {
        val four = honeycombStationCensus(rasterRows = 15, layers = 4, rowBasePairs = 112)
        val six = honeycombStationCensus(rasterRows = 10, layers = 6, rowBasePairs = 112)
        // Deeper blocks have FEWER top-face helices at the same 60 total, so fewer stations.
        assert(four.topFaceHelices == 15)
        assert(six.topFaceHelices == 10)
        assert(six.stations < four.stations)
    }

    @Test
    fun `the station count is the top-face helices times the per-helix ladder`() {
        val census = honeycombStationCensus(rasterRows = 15, layers = 4, rowBasePairs = 112)
        assert(census.stations == census.topFaceHelices * census.stationsPerHelix)
    }

    @Test
    fun `a single-layer block has every helix on the top face`() {
        val census = honeycombStationCensus(rasterRows = 10, layers = 1, rowBasePairs = 112)
        assert(census.topFaceHelices == 10)
    }
}
