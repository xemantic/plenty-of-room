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
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_LENGTH
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-153`, leaf `A8.2` — the Gen-1 tile at the buildable seamless raster width, 112 bp = 38.08 nm.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 *
 * The load-bearing gates are **gate 2** (at `edgeX = 40.0` every construction here reproduces the
 * published number, so the comparison is on the right object) and **gate 3** (the upward station
 * lattice at `C-0063`'s two centro-symmetric phases is *identical* between the two widths, which is
 * what makes the whole branch a comparison of hosts rather than of station sets).
 */
class BuildableRasterWidthTest {

    private val rise = Gen1Tile.RISE_PER_BASE_PAIR
    private val duplexes = 15
    private val sheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )
    private val nominal = Gen1Tile.EDGE_X
    private val buildable = BUILDABLE_RASTER_WIDTH

    // ------------------------------------------------------------------ gate 1 — dimensional

    @Test
    fun `gate 1 - the buildable width is a base-pair count times the rise`() {
        assert(BUILDABLE_RASTER_ROW_BASE_PAIRS == 112)
        assert(buildable.isCloseTo(112 * rise))
        assert(risesIn(buildable).isCloseTo(112.0))
        // 4.8 % narrower than the nominal, and the step to the next rung is 32 bp
        assert(((nominal - buildable) / nominal).isCloseTo(0.048, 1e-2))
    }

    @Test
    fun `gate 1 - quantising to the rise is a floor and is idempotent`() {
        val quantised = quantisedToRise(C0055_ARM_LENGTH)
        assert(quantised.isCloseTo(24 * rise))
        assert(quantised <= C0055_ARM_LENGTH)
        assert(quantisedToRise(quantised).isCloseTo(quantised))
        assert(risesIn(quantised).isCloseTo(24.0))
    }

    @Test
    fun `gate 1 - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { risesIn(1.0, rise = 0.0) }
        assertFailsWith<IllegalArgumentException> { quantisedToRise(-1.0) }
        assertFailsWith<IllegalArgumentException> { endOfRowColumnPhases(0) }
        assertFailsWith<IllegalArgumentException> { inboardArmCeiling(-1.0, 1.0) }
        assertFailsWith<IllegalArgumentException> { outboardArmCeiling(1.0, -1.0) }
        assertFailsWith<IllegalArgumentException> {
            rasterJunctionPlanes(0, -1.0)
        }
        assertFailsWith<IllegalArgumentException> {
            bestEdgeClearance(emptyList(), 1.0, 10.0)
        }
    }

    // ------------------------------------------------------------------ gate 2 — limiting cases

    @Test
    fun `gate 2 - with the row end refused the raster lattice IS C-0015's lattice, at both widths`() {
        for (edgeX in listOf(nominal, buildable)) {
            for (phase in 0 until 32) {
                val mine = rasterColumnLayout(phase, sheet, edgeX, admitRowEnd = false)
                val theirs = CrossoverLayout.atBasePairPhase(phase, sheet, edgeX)
                assert(mine.positions.size == theirs.positions.size)
                assert(mine.positions.zip(theirs.positions).all { (a, b) -> abs(a - b) < 1e-12 })
                assert(mine.parities == theirs.parities)
                val census = rasterSiteInventory(phase, edgeX, duplexes, admitRowEnd = false)
                val published = junctionSiteInventory(phase, edgeX, duplexes)
                assert(census.interfaceSites == published.interfaceSites)
                assert(census.upwardSites == published.upwardSites)
                assert(census.downwardSites == published.downwardSites)
                assert(census.outwardFacingSites == published.outwardFacingSites)
                val sites = rasterUpwardSites(phase, edgeX, duplexes, admitRowEnd = false)
                assert(sites == upwardRootLattice(phase, edgeX, duplexes))
            }
        }
    }

    @Test
    fun `gate 2 - 112 bp is an integer number of column pitches and 118 bp is not`() {
        assert(isIntegerColumnPitches(112))
        assert(!isIntegerColumnPitches(118))
        assert(isIntegerColumnPitches(144))
        // C-0086's own admissible list, re-derived: 112 is on it, 118 is not
        assert(112 in admissibleRasterRowLengths(200))
        assert(118 !in admissibleRasterRowLengths(200))
    }

    @Test
    fun `gate 2 - a column lands on the row end at exactly two phases and they are 8 and 24`() {
        assert(endOfRowColumnPhases(112) == listOf(8, 24))
        // at the nominal width no phase does, because 117.6 bp is not an integer pitch count
        assert(endOfRowColumnPhases(118).isEmpty())
        assert(endOfRowColumnPhases(144) == listOf(8, 24))
    }

    @Test
    fun `gate 2 - the eight-column phases collapse from ten to two`() {
        val nominalEight = (0 until 32).filter {
            CrossoverLayout.atBasePairPhase(it, sheet, nominal).size == 8
        }
        assert(nominalEight.size == 10)
        val interior = (0 until 32).filter {
            rasterColumnLayout(it, sheet, buildable, admitRowEnd = false).size == 8
        }
        assert(interior.isEmpty())
        val admitted = (0 until 32).filter {
            rasterColumnLayout(it, sheet, buildable, admitRowEnd = true).size == 8
        }
        assert(admitted == listOf(8, 24))
        // and the eight-column inventory is C-0015's 56, at both of them
        assert(admitted.all { rasterSiteInventory(it, buildable, duplexes, true).interfaceSites == 56 })
        assert(
            (0 until 32).filter {
                rasterColumnLayout(it, sheet, buildable, admitRowEnd = false).size == 6
            } == listOf(8, 24)
        )
    }

    @Test
    fun `gate 2 - a zero stroke has no overhang and a long arm never places`() {
        val sites = listOf(-10.88, 0.0, 10.88)
        assert(bestEdgeClearance(sites, 1.0e-9, buildable) != null)
        assert(rowEdgeClearance(sites, 40.0, 2, buildable) == null)
    }

    // ------------------------------------------------------------ gate 3 — symmetry, invariance

    @Test
    fun `gate 3 - the upward station lattice at phases 8 and 24 is IDENTICAL at both widths`() {
        for (phase in listOf(8, 24)) {
            val wide = upwardRootLattice(phase, nominal, duplexes)
            val narrow = upwardRootLattice(phase, buildable, duplexes)
            assert(wide.size == narrow.size)
            wide.zip(narrow).forEach { (a, b) ->
                assert(a.size == b.size)
                assert(a.zip(b).all { (x, y) -> abs(x - y) < 1e-12 })
            }
        }
        // and it is NOT identical at phase 0, where the end-of-row plane carries upward sites
        assert(
            upwardRootLattice(0, nominal, duplexes).sumOf { it.size } !=
                    upwardRootLattice(0, buildable, duplexes).sumOf { it.size }
        )
    }

    @Test
    fun `gate 3 - the centro-symmetry congruence still selects 8 and 24 at the buildable width`() {
        assert(centroSymmetricUpwardPhases(nominal, duplexes) == listOf(8, 24))
        assert(centroSymmetricUpwardPhases(buildable, duplexes) == listOf(8, 24))
    }

    @Test
    fun `gate 3 - the end-of-row plane lattice is symmetric about the tile centre`() {
        for (phase in listOf(8, 24)) {
            val planes = rasterJunctionPlanes(phase, buildable, admitRowEnd = true).map { it.x }
            val reflected = planes.map { -it }.sorted()
            assert(planes.zip(reflected).all { (a, b) -> abs(a - b) < 1e-9 })
        }
    }

    // ------------------------------------------- gate 5 — upstream reproduction, and the finding

    @Test
    fun `gate 5 - C-0069's plan budget is pitch minus d and carries no tile width`() {
        val pitch = Gen1Tile.CROSSOVER_SPACING_SHEET_BP * rise
        assert(pitch.isCloseTo(10.88))
        val budget = inboardArmCeiling(pitch, OrigamiDuplex.INTERHELICAL)
        assert(budget.isCloseTo(8.19))
        // C-0072's identity, and C-0069's 0.0256 nm knife edge
        assert((budget - C0055_ARM_LENGTH).isCloseTo(0.02560917, 1e-6))
    }

    @Test
    fun `gate 5 - the binding arm ceiling SWITCHES from the inboard one to the outboard one`() {
        val pitch = Gen1Tile.CROSSOVER_SPACING_SHEET_BP * rise
        val inboard = inboardArmCeiling(pitch, OrigamiDuplex.INTERHELICAL)
        assert(outboardArmCeiling(pitch, nominal).isCloseTo(9.12))
        assert(outboardArmCeiling(pitch, nominal) > inboard)
        assert(outboardArmCeiling(pitch, buildable).isCloseTo(8.16))
        assert(outboardArmCeiling(pitch, buildable) < inboard)
        // the crossing is a width, and the buildable one falls just below it
        val crossing = armCeilingCrossoverWidth(pitch, OrigamiDuplex.INTERHELICAL)
        assert(crossing.isCloseTo(38.14))
        assert(buildable < crossing)
        assert(((crossing - buildable) / rise).isCloseTo(0.176, 1e-2))
    }

    @Test
    fun `gate 5 - the elastica arm overhangs the buildable tile and the quantised one is tangent`() {
        val sites = listOf(-10.88, 0.0, 10.88)
        assert(rowEdgeClearance(sites, C0055_ARM_LENGTH, 3, nominal)!!.isCloseTo(0.95560917, 1e-6))
        assert(rowEdgeClearance(sites, C0055_ARM_LENGTH, 3, buildable) == null)
        val quantised = quantisedToRise(C0055_ARM_LENGTH)
        assert(abs(rowEdgeClearance(sites, quantised, 3, buildable)!!) < 1e-9)
        // the overhang the elastica arm asks for is 77 times below the rise, so it is unbuildable
        val overhang = 10.88 + C0055_ARM_LENGTH - buildable / 2.0
        assert(overhang.isCloseTo(0.00439083, 1e-6))
        assert(overhang / rise < 0.02)
    }

    @Test
    fun `gate 5 - the buildable arm restores C-0063's row capacity and the elastica arm does not`() {
        val quantised = quantisedToRise(C0055_ARM_LENGTH)
        fun capacity(edgeX: Double, phase: Int, arm: Double) = (0 until duplexes).sumOf { row ->
            maximumArmsInRow(
                upwardHingeSites(phase, edgeX, duplexes).filter { it.interfaceIndex == row },
                arm, edgeX, OrigamiDuplex.INTERHELICAL, row
            ).size
        }
        assert(capacity(nominal, 24, C0055_ARM_LENGTH) == 45)
        assert(capacity(buildable, 24, C0055_ARM_LENGTH) == 38)
        assert(capacity(buildable, 24, quantised) == 45)
        assert(capacity(nominal, 8, C0055_ARM_LENGTH) == 45)
        assert(capacity(buildable, 8, C0055_ARM_LENGTH) == 37)
        assert(capacity(buildable, 8, quantised) == 45)
        // 34 still places at both widths and under both readings of the arm
        assert(capacity(buildable, 24, C0055_ARM_LENGTH) >= 34)
    }

    @Test
    fun `gate 5 - C-0063's count vector contains the duplex count and not the width`() {
        assert(rowsCarryingThreeArms(34, duplexes, 3) == 4)
    }
}
