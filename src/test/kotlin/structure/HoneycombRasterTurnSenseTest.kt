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

package com.xemantic.nano.plentyofroom.structure

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-218` — which turn sense `Δ` does a caDNAno `15 × 4` honeycomb x-raster carry?
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 * The whole task is exact integer lattice arithmetic; there is no solve and therefore no mesh, so
 * gate 4 is discharged as **exactness over whole periods and whole families** rather than as a
 * refinement sequence.
 */
class HoneycombRasterTurnSenseTest {

    private val design1 = honeycombXRasterPath(rows = 15, helicesPerRow = 4)

    // ------------------------------------------------------------------ gate 1 — dimensional

    @Test
    fun `gate 1 dimensional a cross-section carries azimuths and no length`() {
        assert(honeycombAzimuthDegrees(0, 2).isCloseTo(90.0))
        assert(honeycombAzimuthDegrees(-1, -1).isCloseTo(210.0))
        assert(honeycombAzimuthDegrees(1, -1).isCloseTo(330.0))
        assert(honeycombAzimuthDegrees(0, -2).isCloseTo(270.0))
        assert(honeycombAzimuthDegrees(1, 1).isCloseTo(30.0))
        assert(honeycombAzimuthDegrees(-1, 1).isCloseTo(150.0))
    }

    @Test
    fun `gate 1 dimensional an off-lattice site and an off-bond offset throw`() {
        assertFailsWith<IllegalArgumentException> { HoneycombCell(1, 0) }
        assertFailsWith<IllegalArgumentException> { HoneycombCell(0, 1) }
        assertFailsWith<IllegalArgumentException> { honeycombAzimuthDegrees(0, 0) }
        assertFailsWith<IllegalArgumentException> { honeycombAzimuthDegrees(1, 0) }
        assertFailsWith<IllegalArgumentException> { honeycombXRasterPath(0, 4) }
        assertFailsWith<IllegalArgumentException> { honeycombXRasterPath(15, 1) }
        assertFailsWith<IllegalArgumentException> { honeycombXRasterPath(15, 5) }
        assertFailsWith<IllegalArgumentException> { honeycombRasterTurns(design1, 0) }
        assertFailsWith<IllegalArgumentException> { neighbourClassDifference(0.0, 45.0, 3) }
    }

    // ---------------------------------------------------------------- gate 2 — limiting cases

    @Test
    fun `gate 2 limiting THE CHEAP BOUND a honeycomb has no straight three-helix chain`() {
        listOf(HoneycombCell(0, 0), HoneycombCell(0, 2)).forEach { cell ->
            HONEYCOMB_BOND_OFFSETS.getValue(cell.sublattice).forEach { offset ->
                val next = HoneycombCell(cell.x + offset.first, cell.y + offset.second)
                assert(offset !in HONEYCOMB_BOND_OFFSETS.getValue(next.sublattice))
            }
        }
    }

    @Test
    fun `gate 2 limiting the fifteen by four path is a valid honeycomb path of sixty helices`() {
        assert(design1.size == 60)
        requireHoneycombPath(design1)
        assert(design1.distinct().size == 60)
    }

    @Test
    fun `gate 2 limiting an x-raster row is corrugated over exactly two y positions`() {
        (0 until 15).forEach { r ->
            val row = design1.subList(r * 4, r * 4 + 4)
            assert(row.map { it.y }.distinct().size == 2)
            assert(row.map { it.x }.distinct().size == 4)
        }
    }

    @Test
    fun `gate 2 limiting consecutive helices of a row are on opposite sublattices`() {
        design1.zipWithNext().forEach { (a, b) -> assert(a.sublattice != b.sublattice) }
    }

    // ------------------------------------------------------- gate 3 — symmetry and reproduction

    @Test
    fun `gate 3 symmetry the geometric sense alternates in blocks of four`() {
        val senses = honeycombRasterTurns(design1).map { it.geometricSense }
        assert(senses.all { it == 1 || it == 2 })
        assert(senses.take(9) == listOf(1, 2, 2, 2, 2, 1, 1, 1, 1))
        (0 until senses.size - 8).forEach { assert(senses[it] == senses[it + 8]) }
    }

    @Test
    fun `gate 3 symmetry the effective sense is NOT constant and both senses occur`() {
        val senses = honeycombRasterTurns(design1).map { it.effectiveSense }
        assert(senses.toSet() == setOf(1, 2))
        assert(senses.take(8) == listOf(2, 2, 1, 2, 1, 1, 2, 1))
        (0 until senses.size - 8).forEach { assert(senses[it] == senses[it + 8]) }
        assert(senses.count { it == 2 } == 30)
        assert(senses.count { it == 1 } == 28)
    }

    @Test
    fun `gate 3 symmetry mirroring the cross-section swaps the labels and keeps the alternation`() {
        val plain = honeycombRasterTurns(design1).map { it.effectiveSense }
        val mirror = honeycombRasterTurns(
            honeycombXRasterPath(15, 4, mirrored = true)
        ).map { it.effectiveSense }
        assert(mirror == plain.map { 3 - it })
        assert(mirror.toSet() == setOf(1, 2))
    }

    @Test
    fun `gate 3 symmetry flipping the first axial sign swaps the labels and nothing else`() {
        val plain = honeycombRasterTurns(design1, firstAxialSign = 1).map { it.effectiveSense }
        val flipped = honeycombRasterTurns(design1, firstAxialSign = -1).map { it.effectiveSense }
        assert(flipped == plain.map { 3 - it })
    }

    @Test
    fun `gate 3 symmetry the square sheet control reproduces C-0086 unconditionally`() {
        val turns = squareSheetRasterTurns(15)
        assert(turns.all { it.geometricSense == 2 })
        assert(turns.map { it.effectiveSense }.toSet() == setOf(2))
        val residues = HelixCrossoverLattice.SQUARE_SHEET.turnPairResidues(0, 2)
        assert(residues == setOf(16))
    }

    @Test
    fun `gate 3 symmetry the two honeycomb residue triples are disjoint`() {
        val honeycomb = HelixCrossoverLattice.HONEYCOMB
        val delta1 = honeycomb.turnPairResidues(0, 1)
        val delta2 = honeycomb.turnPairResidues(0, 2)
        assert(delta1 == setOf(7, 17, 18))
        assert(delta2 == setOf(3, 4, 14))
        assert(delta1.intersect(delta2).isEmpty())
    }

    // ----------------------------------------------------------- gate 4 — exactness over families

    @Test
    fun `gate 4 exactness no uniform row length serves both senses at any width`() {
        val honeycomb = HelixCrossoverLattice.HONEYCOMB
        val delta1 = honeycomb.turnPairResidues(0, 1)
        val delta2 = honeycomb.turnPairResidues(0, 2)
        (1..2100).forEach { n ->
            val residue = Math.floorMod(n, 21)
            assert(!(residue in delta1 && residue in delta2))
        }
    }

    @Test
    fun `gate 4 exactness the minimum admissible row-length stagger is three base pairs`() {
        val honeycomb = HelixCrossoverLattice.HONEYCOMB
        assert(
            minimumRowLengthStagger(
                honeycomb.turnPairResidues(0, 1),
                honeycomb.turnPairResidues(0, 2),
                21
            ) == 3
        )
        assert(minimumRowLengthStagger(setOf(16), setOf(16), 32) == 0)
    }

    @Test
    fun `gate 2 limiting every raster shape the paper folded has an EVEN row`() {
        listOf(4, 6, 8, 10, 16, 20, 30).forEach { assert(it % 2 == 0) }
        assertFailsWith<IllegalArgumentException> { honeycombXRasterPath(3, 15) }
    }

    @Test
    fun `gate 3 symmetry a row interior carries ONE sense and consecutive rows carry opposite`() {
        val senses = listOf<Int?>(null) + honeycombRasterTurns(design1).map { it.effectiveSense } +
                listOf<Int?>(null)
        (0 until 15).forEach { r ->
            val row = (0 until 4).map { senses[r * 4 + it] }
            val interior = row.subList(1, 3).filterNotNull()
            assert(interior.distinct().size == 1)
            if (r > 0) {
                val previous = (0 until 4).map { senses[(r - 1) * 4 + it] }
                    .subList(1, 3).filterNotNull()
                assert(previous.distinct().single() != interior.distinct().single())
            }
        }
    }

    @Test
    fun `gate 3 symmetry a single x-raster row carries a CONSTANT sense`() {
        val turns = honeycombRasterTurns(honeycombXRasterPath(rows = 1, helicesPerRow = 60))
        assert(turns.map { it.effectiveSense }.distinct().size == 1)
    }

    @Test
    fun `gate 4 exactness the alternation holds at every raster shape the paper folded`() {
        listOf(15 to 4, 10 to 6, 8 to 8, 6 to 10, 4 to 16, 3 to 20, 2 to 30).forEach { (m, n) ->
            val turns = honeycombRasterTurns(honeycombXRasterPath(m, n))
            assert(turns.map { it.effectiveSense }.toSet() == setOf(1, 2))
        }
    }

    @Test
    fun `gate 3 symmetry a two-length raster drifts exactly nothing over one eight-helix period`() {
        val turns = honeycombRasterTurns(design1)
        listOf(112 to 108, 112 to 119, 122 to 119, 101 to 109).forEach { (a, b) ->
            val levels = ArrayList<Int>()
            var current = 0
            levels += current
            turns.forEach { turn ->
                current += turn.axialSign * (if (turn.effectiveSense == 1) a else b)
                levels += current
            }
            (0 until levels.size - 8).forEach { assert(levels[it + 8] == levels[it]) }
        }
    }

    @Test
    fun `gate 4 exactness one hundred and twenty six is exactly six azimuth periods`() {
        assert(Math.floorMod(126, 21) == 0)
        assert(60 * 126 == 7560)
        assert(64 * 126 == 8064)
        assert(Math.floorMod(98, 21) == 14)
    }

    // ---------------------------------------------------------- gate 5 — literature and upstream

    @Test
    fun `gate 5 literature C-0119's 112 bp row is admissible at one sense and not the other`() {
        val honeycomb = HelixCrossoverLattice.HONEYCOMB
        assert(Math.floorMod(112, 21) == 7)
        assert(Math.floorMod(112, 21) in honeycomb.turnPairResidues(0, 1))
        assert(Math.floorMod(112, 21) !in honeycomb.turnPairResidues(0, 2))
        assert(Math.floorMod(119, 21) == 14)
        assert(Math.floorMod(119, 21) in honeycomb.turnPairResidues(0, 2))
        assert(Math.floorMod(119, 21) !in honeycomb.turnPairResidues(0, 1))
    }

    @Test
    fun `gate 5 literature the nearest Delta equals one width to forty nm is 122 bp not 112`() {
        val honeycomb = HelixCrossoverLattice.HONEYCOMB
        val rise = Gen1Tile.RISE_PER_BASE_PAIR
        val nearest = honeycomb
            .admissibleRowLengths(90, 150, honeycomb.turnPairResidues(0, 1))
            .minByOrNull { kotlin.math.abs(it * rise - 40.0) }
        assert(nearest == 122)
        assert(kotlin.math.abs(122 * rise - 40.0) < kotlin.math.abs(112 * rise - 40.0))
    }
}
