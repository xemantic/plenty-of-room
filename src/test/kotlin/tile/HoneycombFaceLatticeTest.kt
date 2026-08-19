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
import com.xemantic.nano.plentyofroom.anchoring.maximumPlanCeilingForCount
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.centroSymmetricPlacementsOn
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-219` — the honeycomb's own station lattice, plan ceiling and placement family.
 *
 * `C-0122` multiplies a census, `CH-0151` corrects the multiplication, and `C-0128` prices an
 * oblique root — and **none of the three derives the block's cross-section**, without which a free
 * azimuth is not defined at all. These tests pin the cross-section first and the census second.
 */
class HoneycombFaceLatticeTest {

    private val d = Gen1Tile.INTERHELICAL_HONEYCOMB

    // --- gate 1: the cheap bound, which is one multiplication -----------------------------------

    @Test
    fun `a honeycomb cell is the product of its two pitches`() {
        val row = HoneycombCrossSectionGeometry.rowPitch(d)
        val column = HoneycombCrossSectionGeometry.columnPitch(d)
        assert(row.isCloseTo(1.5 * d))
        assert(column.isCloseTo(sqrt(3.0) / 2.0 * d))
        assert((row * column).isCloseTo(HoneycombCrossSectionGeometry.perSiteArea(d)))
    }

    @Test
    fun `the assumed cross-section is denser than any honeycomb by exactly three root three over four`() {
        // F1: `C-0109`/`C-0120` write the cross-section as `interhelicalDistance x layerSpacing`
        // with both equal to `d`. A honeycomb lattice cannot pack that tightly.
        val assumed = d * d
        val honeycomb = HoneycombCrossSectionGeometry.perSiteArea(d)
        assert((honeycomb / assumed).isCloseTo(3.0 * sqrt(3.0) / 4.0))
        assert((honeycomb / assumed) > 1.29 && (honeycomb / assumed) < 1.30)
    }

    // --- gate 2: the lattice itself -------------------------------------------------------------

    @Test
    fun `every helix has three azimuths and the neighbour relation is symmetric`() {
        val block = HoneycombBlock(rasterRows = 6, helicesPerRow = 6, bondLength = d)
        block.sites.forEach { site ->
            val azimuths = honeycombAzimuthsOf(site)
            assert(azimuths.size == 3)
            azimuths.forEach { azimuth ->
                val r = site.rasterRow + azimuth.rasterRowStep
                val c = site.column + azimuth.columnStep
                if (block.contains(r, c)) {
                    val back = honeycombAzimuthsOf(HoneycombSite(r, c))
                    assert(back.any {
                        r + it.rasterRowStep == site.rasterRow && c + it.columnStep == site.column
                    })
                }
            }
        }
    }

    @Test
    fun `every bond is exactly one lattice constant long`() {
        val block = HoneycombBlock(rasterRows = 5, helicesPerRow = 5, bondLength = d)
        block.sites.forEach { site ->
            val (x, y) = block.position(site)
            honeycombAzimuthsOf(site).forEach { azimuth ->
                val r = site.rasterRow + azimuth.rasterRowStep
                val c = site.column + azimuth.columnStep
                if (block.contains(r, c)) {
                    val (nx, ny) = block.position(HoneycombSite(r, c))
                    val distance = sqrt((nx - x) * (nx - x) + (ny - y) * (ny - y))
                    assert(distance.isCloseTo(d))
                    // and the unit vector points at it
                    assert(((nx - x) / d).isCloseTo(azimuth.unitX))
                    assert(((ny - y) / d).isCloseTo(azimuth.unitY))
                }
            }
        }
    }

    // --- gate 3: the census, derived rather than multiplied -------------------------------------

    @Test
    fun `every face helix carries exactly ONE rooting azimuth`() {
        // F2: `CH-0151` asserts two on one sublattice. On a full `m x n` block the up-oblique
        // azimuths of that sublattice point at the OTHER sublattice's helices in its own row,
        // which are present, so it carries none at all -- and the face normal to the thin
        // direction gives every one of its helices exactly one.
        listOf(15 to 4, 10 to 6).forEach { (rows, perRow) ->
            val block = HoneycombBlock(rows, perRow, d)
            val rooting = block.rootingAzimuths(1.0, 0.0)
            assert(rooting.size == rows)
            assert(rooting.map { it.first.rasterRow }.toSet().size == rows)
            assert(rooting.all { it.first.column == perRow - 1 })
        }
    }

    @Test
    fun `the rooting azimuth is thirty degrees off the face normal and alternates`() {
        val block = HoneycombBlock(15, 4, d)
        val rooting = block.rootingAzimuths(1.0, 0.0).sortedBy { it.first.rasterRow }
        rooting.forEach { (_, azimuth) ->
            assert(azimuth.angleFromNormalDegrees(1.0, 0.0).isCloseTo(30.0))
        }
        // the sign alternates with the row parity, which is what makes the face stagger forced
        val signs = rooting.map { it.second.unitY > 0.0 }
        assert(signs.zipWithNext().all { (a, b) -> a != b })
    }

    @Test
    fun `no azimuth of a full block points straight out of a face`() {
        // The corollary of gate 3 and the correction to `C-0128`'s 60 degrees: on the face normal
        // to the thin direction there is no perpendicular root at all.
        val block = HoneycombBlock(10, 6, d)
        assert(block.rootingAzimuths(1.0, 0.0).none {
            it.second.angleFromNormalDegrees(1.0, 0.0) < 1.0
        })
    }

    @Test
    fun `a block's envelope is the honeycomb cell area per helix`() {
        val block = HoneycombBlock(15, 4, d)
        assert(block.latticeExtentX.isCloseTo(3.0 * HoneycombCrossSectionGeometry.columnPitch(d)))
        assert(block.latticeExtentY.isCloseTo(
            14.0 * HoneycombCrossSectionGeometry.rowPitch(d) + 0.5 * d
        ))
        // and the envelope area per helix lands on the honeycomb cell, within the edge allowance
        assert(abs(block.envelopeAreaPerHelix / HoneycombCrossSectionGeometry.perSiteArea(d) - 1.0) < 0.05)
    }

    @Test
    fun `the plate edgeY the block implies is exactly one and a half times the standing one`() {
        listOf(15 to 4, 10 to 6).forEach { (rows, perRow) ->
            val block = HoneycombBlock(rows, perRow, d)
            assert((block.plateEdgeY / (rows * d)).isCloseTo(1.5))
        }
        // and the corrected `10 x 6` is bit-close to the standing `15 x 4`, because 10 x 1.5 = 15
        assert(HoneycombBlock(10, 6, d).plateEdgeY.isCloseTo(15 * d))
    }

    // --- gate 4: the station ladder -------------------------------------------------------------

    @Test
    fun `the ladder is the 21 bp period and a 112 bp row carries six stations`() {
        assert(honeycombLadderIndices(112, 0).size == 6)
        assert(honeycombLadderIndices(112, 0) == listOf(0, 21, 42, 63, 84, 105))
        assert(honeycombLadderIndices(112, 7).size == 6)
        assert(honeycombLadderIndices(119, 7) == listOf(7, 28, 49, 70, 91, 112))
    }

    @Test
    fun `the census reproduces C-0122's 90 and 60 at one azimuth per helix`() {
        val fifteen = honeycombStationLattice(15, 112, 0, 0)
        val ten = honeycombStationLattice(10, 112, 0, 0)
        assert(fifteen.sumOf { it.size } == 90)
        assert(ten.sumOf { it.size } == 60)
    }

    @Test
    fun `adjacent station rows are staggered by the inter-row offset`() {
        val lattice = honeycombStationLattice(4, 112, 0, 7)
        val step = 7 * Gen1Tile.RISE_PER_BASE_PAIR
        assert((lattice[1][0] - lattice[0][0]).isCloseTo(step))
        assert((lattice[2][0] - lattice[1][0]).isCloseTo(-step))
    }

    @Test
    fun `an ODD rooting-helix count admits NO centro-symmetric station lattice under the forced stagger`() {
        // Rows r and (m - 1 - r) of a 15-row face have the SAME parity, so the reflection maps a
        // row onto one carrying the SAME ladder phase -- which the 7 bp stagger then cannot
        // satisfy. Enumerated over every phase and both admissible offsets.
        listOf(7, 14).forEach { offset ->
            listOf(112, 119).forEach { row ->
                assert((0 until HoneycombLattice.SAME_PAIR_PERIOD_BP).none {
                    latticeIsCentroSymmetric(honeycombStationLattice(15, row, it, offset))
                })
            }
        }
    }

    @Test
    fun `an EVEN rooting-helix count admits one at the FULL station count`() {
        // Rows r and (m - 1 - r) of a 10-row face have OPPOSITE parity, so the reflection swaps
        // the two ladder phases and the stagger is exactly what makes the symmetry available.
        val phases = (0 until HoneycombLattice.SAME_PAIR_PERIOD_BP).filter {
            latticeIsCentroSymmetric(honeycombStationLattice(10, 112, it, 7))
        }
        assert(phases == listOf(0))
        assert(honeycombStationLattice(10, 112, 0, 7).sumOf { it.size } == 60)
    }

    @Test
    fun `the unstaggered lattice would cost a station per row on a 112 bp row and none on 119`() {
        // The counterfactual, kept because it is what isolates the row LENGTH from the stagger.
        val onOneTwelve = (0 until HoneycombLattice.SAME_PAIR_PERIOD_BP).filter {
            latticeIsCentroSymmetric(honeycombStationLattice(15, 112, it, 0))
        }
        assert(onOneTwelve == listOf(14))
        assert(honeycombStationLattice(15, 112, 14, 0).sumOf { it.size } == 75)
        val onOneNineteen = (0 until HoneycombLattice.SAME_PAIR_PERIOD_BP).filter {
            latticeIsCentroSymmetric(honeycombStationLattice(15, 119, it, 0))
        }
        assert(onOneNineteen == listOf(7))
        assert(honeycombStationLattice(15, 119, 7, 0).sumOf { it.size } == 90)
    }

    @Test
    fun `the inter-row stagger is forced to seven or fourteen base pairs and never zero`() {
        // The two face sublattices carry their free azimuth on two DIFFERENT bond classes, and
        // caDNAno's rule puts consecutive azimuths of a helix 7 bp apart with the same pair every
        // 21 -- so the residues of two distinct classes differ by 7 or 14 mod 21, never 0.
        assert(HoneycombLattice.SAME_PAIR_PERIOD_BP ==
                HoneycombLattice.ANY_AZIMUTH_STEP_BP * HoneycombLattice.AZIMUTHS)
        val block = HoneycombBlock(4, 4, d)
        val classes = block.rootingAzimuths(1.0, 0.0).map { it.second.unitY > 0.0 }.toSet()
        assert(classes.size == 2)
    }

    // --- gate 5: the placement machinery is lattice-generic --------------------------------------

    @Test
    fun `the square-lattice plan ceiling machinery accepts the honeycomb lattice`() {
        // F5. `maximumPlanCeilingForCount` takes an explicit lattice, so nothing about it is
        // square-lattice-specific; what is square-lattice-specific is the lattice GENERATOR.
        val lattice = honeycombStationLattice(15, 112, 0, 7)
        val ceiling = maximumPlanCeilingForCount(
            lattice, count = 45, edgeX = 112 * Gen1Tile.RISE_PER_BASE_PAIR,
            width = d, maximumPerRow = 6
        )
        assert(ceiling != null)
        // F3 FIRES: at 45 of 90 stations a placement SKIPS stations, so the binding pitch is
        // 42 bp and not 21, and the ceiling is ABOVE the square lattice's 8.19 nm inboard bound.
        assert(ceiling!! > 8.19)
    }

    @Test
    fun `the inboard bound binds only at the SATURATED count`() {
        val lattice = honeycombStationLattice(15, 112, 0, 7)
        val inboard = HoneycombLattice.SAME_PAIR_PERIOD_BP * Gen1Tile.RISE_PER_BASE_PAIR - d
        val saturated = maximumPlanCeilingForCount(
            lattice, count = 90, edgeX = 112 * Gen1Tile.RISE_PER_BASE_PAIR,
            width = d, maximumPerRow = 6
        )
        assert(saturated != null)
        assert(saturated!! < inboard)
    }

    @Test
    fun `the collinear inboard bound is the ladder less one duplex`() {
        val inboard = HoneycombLattice.SAME_PAIR_PERIOD_BP * Gen1Tile.RISE_PER_BASE_PAIR - d
        assert(inboard.isCloseTo(4.604))
        val square = 32 * Gen1Tile.RISE_PER_BASE_PAIR - Gen1Tile.INTERHELICAL_SHEET
        assert((square / inboard) > 1.77 && (square / inboard) < 1.79)
    }

    @Test
    fun `a centro-symmetric placement family exists on the even-row honeycomb lattice`() {
        val lattice = honeycombStationLattice(10, 112, 0, 7)
        val found = centroSymmetricPlacementsOn(
            lattice, edgeX = 112 * Gen1Tile.RISE_PER_BASE_PAIR, arm = 3.0, count = 20,
            minimumPerRow = 2, maximumPerRow = 2, width = d
        ).take(3).toList()
        assert(found.isNotEmpty())
        assert(found.all { it.isCentroSymmetric(10) })
    }

    @Test
    fun `an odd-row honeycomb face supplies none, and the enumerator must not be asked`() {
        // `centroSymmetricPlacementsOn` mirrors a row's roots by NEGATING them; on a lattice that
        // is not itself centro-symmetric the mirror image is not a lattice point at all, so the
        // enumerator would return placements the sheet cannot carry. The guard is the lattice.
        val lattice = honeycombStationLattice(15, 112, 0, 7)
        assert(!latticeIsCentroSymmetric(lattice))
        val mirroredRow0 = lattice[0].map { -it }
        assert(mirroredRow0.none { candidate ->
            lattice[14].any { abs(it - candidate) < 1e-9 }
        })
    }

    // --- gate 6: the guards ----------------------------------------------------------------------

    @Test
    fun `a block refuses a non-positive size`() {
        assertFailsWith<IllegalArgumentException> { HoneycombBlock(0, 4, d) }
        assertFailsWith<IllegalArgumentException> { HoneycombBlock(4, 0, d) }
        assertFailsWith<IllegalArgumentException> { HoneycombBlock(4, 4, 0.0) }
    }

    @Test
    fun `a single row of one helix is free on all three azimuths`() {
        val block = HoneycombBlock(1, 1, d)
        assert(block.freeAzimuths(HoneycombSite(0, 0)).size == 3)
        assert(block.rootingAzimuths(1.0, 0.0).size == 1)
    }
}
