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
import com.xemantic.nano.plentyofroom.anchoring.rasterColumnLayout
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-216` — the crossover **phase lattice** of a mixed-domain row —
 * and `T-217` — can the four-layer **honeycomb** tile be twist-corrected?
 *
 * The two tasks share one arithmetic: `10.5 = 21/2`, so an **odd** number of half turns is never
 * an integer number of base pairs and an **even** one may be. `C-0133`'s square-lattice
 * incompatibility and the honeycomb's freedom from it are the two readings of that one fact.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 */
class LatticePhaseCensusTest {

    private val rise = Gen1Tile.RISE_PER_BASE_PAIR
    private val corrected = listOf(16, 15, 16, 16, 16, 15, 16)
    private val uniform = List(7) { 16 }

    // ------------------------------------------------------------------ gate 1 — dimensional

    @Test
    fun `gate 1 dimensional a crossover lattice carries degrees per base and no length`() {
        assert(HelixCrossoverLattice.SQUARE_SHEET.azimuthPeriodBasePairs == 32)
        assert(HelixCrossoverLattice.HONEYCOMB.azimuthPeriodBasePairs == 21)
        assert(HelixCrossoverLattice.SQUARE_SHEET.designTwistPerBase.isCloseTo(33.75))
        assert(HelixCrossoverLattice.HONEYCOMB.designTwistPerBase.isCloseTo(360.0 / 10.5))
        assert(HelixCrossoverLattice.SQUARE_SHEET.azimuthStepDegrees.isCloseTo(270.0))
        assert(HelixCrossoverLattice.HONEYCOMB.azimuthStepDegrees.isCloseTo(240.0))
    }

    @Test
    fun `gate 1 dimensional unphysical lattices and rows throw`() {
        assertFailsWith<IllegalArgumentException> {
            HelixCrossoverLattice("bad", 0, 7, 2, 5, listOf(0, 1))
        }
        assertFailsWith<IllegalArgumentException> {
            HelixCrossoverLattice("bad", 3, 7, 2, 5, listOf(0))
        }
        assertFailsWith<IllegalArgumentException> {
            HelixCrossoverLattice("bad", 3, 7, 2, 5, listOf(0, 3))
        }
        assertFailsWith<IllegalArgumentException> { domainArrangements(110, 0, 15, 16) }
        assertFailsWith<IllegalArgumentException> { domainArrangements(110, 7, 17, 16) }
        assertFailsWith<IllegalArgumentException> { halfTurnBasePairs(0, 10.5) }
        assertFailsWith<IllegalArgumentException> { halfTurnBasePairs(3, 0.0) }
    }

    // ------------------------------------------------------------------ gate 2 — limiting cases

    /**
     * `T-217`'s cheap bound and the unification of both tasks: at 10.5 bp/turn a half turn is
     * 5.25 bp, so `h` half turns is an integer number of base pairs **iff `h ≡ 0 (mod 4)`**, and
     * the distance to the nearest integer is exactly `0.25` for odd `h` and exactly `0.5` for
     * `h ≡ 2 (mod 4)`. `C-0133`'s theorem is the odd case; the honeycomb's 21 bp azimuth period
     * is the `h = 4` case.
     */
    @Test
    fun `gate 2 limiting an odd half turn is never integral and a quadruple one always is`() {
        (1..2001).forEach { h ->
            val distance = distanceToNearestInteger(halfTurnBasePairs(h, 10.5))
            when (h % 4) {
                0 -> assert(distance.isCloseTo(0.0))
                2 -> assert(distance.isCloseTo(0.5))
                else -> assert(distance.isCloseTo(0.25))
            }
        }
    }

    @Test
    fun `gate 2 limiting the honeycomb azimuth period is exactly four half turns of B-DNA`() {
        val honeycomb = HelixCrossoverLattice.HONEYCOMB
        assert(honeycomb.halfTurnsPerAzimuthPeriod == 4)
        assert(halfTurnBasePairs(4, 10.5).isCloseTo(honeycomb.azimuthPeriodBasePairs.toDouble()))
        assert(honeycomb.mismatchPerBase(B_DNA_TWIST_PER_BASE).isCloseTo(0.0))
        assert(honeycomb.accumulatedMismatchDegrees(112, B_DNA_TWIST_PER_BASE).isCloseTo(0.0))
    }

    /** `C-0086`'s 112 bp row is 60.0° out on its own lattice; the honeycomb's is exactly zero. */
    @Test
    fun `gate 2 limiting the square sheet carries C-0086's sixty degrees and honeycomb none`() {
        val square = HelixCrossoverLattice.SQUARE_SHEET
        assert(abs(square.accumulatedMismatchDegrees(112, B_DNA_TWIST_PER_BASE)).isCloseTo(60.0))
        assert(HelixCrossoverLattice.HONEYCOMB
            .accumulatedMismatchDegrees(112, B_DNA_TWIST_PER_BASE).isCloseTo(0.0))
    }

    /** A lattice at its own natural twist has no mismatch, whatever the row length. */
    @Test
    fun `gate 2 limiting a lattice read at its own design twist has zero mismatch everywhere`() {
        listOf(HelixCrossoverLattice.SQUARE_SHEET, HelixCrossoverLattice.HONEYCOMB).forEach {
            (1..300).forEach { n ->
                assert(it.accumulatedMismatchDegrees(n, it.designTwistPerBase).isCloseTo(0.0))
            }
        }
    }

    // ------------------------------------------- gate 3 — symmetry, reproduction, conservation

    /**
     * **The gate that makes `T-217` a derivation and not a second rule.** Run on the square
     * sheet's own azimuths — four classes, 8 bp apart, the two in-plane ones two classes apart —
     * the neighbour-azimuth construction must return `C-0086`'s *"odd multiples of 16 bp"*
     * exactly.
     */
    @Test
    fun `gate 3 reproduction the construction returns C-0086's odd multiples of sixteen`() {
        val square = HelixCrossoverLattice.SQUARE_SHEET
        assert(square.admissibleRowResidues() == setOf(16))
        assert(square.admissibleRowLengths(1, 200) == listOf(16, 48, 80, 112, 144, 176))
    }

    @Test
    fun `gate 3 reproduction the honeycomb residues are six of twenty one and disjoint by turn`() {
        val honeycomb = HelixCrossoverLattice.HONEYCOMB
        assert(honeycomb.scaffoldCrossoverResidues(0) == setOf(5, 16))
        assert(honeycomb.scaffoldCrossoverResidues(1) == setOf(2, 12))
        assert(honeycomb.scaffoldCrossoverResidues(2) == setOf(9, 19))
        assert(honeycomb.turnPairResidues(0, 1) == setOf(7, 17, 18))
        assert(honeycomb.turnPairResidues(0, 2) == setOf(3, 4, 14))
        assert(honeycomb.turnPairResidues(0, 0) == setOf(0, 10, 11))
        // the two turn senses are DISJOINT: no row length serves both
        assert(honeycomb.turnPairResidues(0, 1).intersect(honeycomb.turnPairResidues(0, 2))
            .isEmpty())
        assert(honeycomb.admissibleRowResidues() == setOf(3, 4, 7, 14, 17, 18))
    }

    /** The residue set depends only on `Δ = (b − a) mod 3`, not on which pair realises it. */
    @Test
    fun `gate 3 symmetry the honeycomb turn pair residues depend only on the class difference`() {
        val honeycomb = HelixCrossoverLattice.HONEYCOMB
        (0..2).forEach { a ->
            (0..2).forEach { b ->
                val delta = Math.floorMod(b - a, 3)
                assert(honeycomb.turnPairResidues(a, b) ==
                        honeycomb.turnPairResidues(0, delta))
            }
        }
    }

    /**
     * `C-0119`'s own 112 bp row is admissible — but at exactly one of the two turn senses.
     * `C-0119` checks that the scaffold lattice is **integral** and does not check the azimuth.
     */
    @Test
    fun `gate 3 reproduction C-0119's 112 bp honeycomb row is admissible at one turn sense`() {
        val honeycomb = HelixCrossoverLattice.HONEYCOMB
        assert(112 % honeycomb.azimuthPeriodBasePairs == 7)
        assert(honeycomb.turnPairResidues(0, 1).contains(7))
        assert(!honeycomb.turnPairResidues(0, 2).contains(7))
    }

    // ------------------------------------------------------- gate 2/3 — the phase lattice, T-216

    /**
     * **`T-216`'s cheap bound.** A seamless raster row's two ends *are* the tile edges and both
     * carry a scaffold crossover, so a rigid translation of the column pattern by any non-zero
     * amount takes an end column off the edge. The admissible translation group is therefore
     * **trivial** — on the mixed-domain row and on `C-0086`'s uniform one alike.
     */
    @Test
    fun `gate 2 limiting a seamless row admits exactly one rigid column translation`() {
        assert(admissibleColumnTranslations(corrected) == listOf(0))
        assert(admissibleColumnTranslations(uniform) == listOf(0))
        assert(admissibleColumnTranslations(listOf(16)) == listOf(0))
    }

    /**
     * `C-0090`'s *"two phases"* is **one column lattice and a parity binary**: at 38.08 nm phases
     * 8 and 24 give the **same** column positions to the last bit and the **opposite** parities.
     */
    @Test
    fun `gate 3 reproduction C-0090's phases 8 and 24 are one lattice and two parities`() {
        val sheet = origamiSheet(
            Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
        )
        val eight = rasterColumnLayout(8, sheet, 38.08, true)
        val twentyFour = rasterColumnLayout(24, sheet, 38.08, true)
        assert(eight.positions.size == 8)
        assert(twentyFour.positions.size == 8)
        eight.positions.indices.forEach {
            assert(twentyFour.positions[it].isCloseTo(eight.positions[it], 1e-12))
        }
        eight.parities.indices.forEach {
            assert(twentyFour.parities[it] == 1 - eight.parities[it])
        }
    }

    /** The census of the 110 bp twist-corrected row's arrangement family. */
    @Test
    fun `gate 2 limiting the 110 bp arrangement family is 21 with 3 centro-symmetric`() {
        val family = domainArrangements(110, 7, 15, 16)
        assert(family.size == 21)
        assert(family.all { it.sum() == 110 })
        assert(family.all { it.size == 7 })
        // the column count is an IDENTITY, not a function of anything
        assert(family.all { rasterColumnPositions(it, rise).size == 8 })
        assert(family.count { isCentroSymmetricDomains(it) } == 3)
        assert(reflectionClassCount(family) == 12)
        assert(family.contains(corrected))
    }

    /** A uniform row has exactly one arrangement, so the arrangement axis is new at 110 bp. */
    @Test
    fun `gate 2 limiting a uniform row has exactly one arrangement`() {
        val family = domainArrangements(112, 7, 16, 16)
        assert(family.size == 1)
        assert(family.single() == uniform)
        assert(isCentroSymmetricDomains(family.single()))
        assert(reflectionClassCount(family) == 1)
    }

    /** Centro-symmetry of the domain sequence is exactly centro-symmetry of the column set. */
    @Test
    fun `gate 3 symmetry a palindromic domain sequence gives a centro-symmetric column set`() {
        domainArrangements(110, 7, 15, 16).forEach { domains ->
            val columns = rasterColumnPositions(domains, rise)
            val mirrored = columns.map { -it }.sorted()
            val symmetric = columns.zip(mirrored).all { (a, b) -> abs(a - b) < 1e-12 }
            assert(symmetric == isCentroSymmetricDomains(domains))
        }
    }

    /**
     * The parity binary transfers to the mixed-domain row, and parity 0 reproduces
     * `twistCorrectedUpwardSites` to the last bit — which is what makes this a generalisation
     * rather than a second station lattice.
     */
    @Test
    fun `gate 3 reproduction parity zero reproduces twistCorrectedUpwardSites exactly`() {
        listOf(uniform, corrected).forEach { domains ->
            val reference = twistCorrectedUpwardSites(domains, 15, rise)
            val mine = mixedDomainUpwardSites(domains, 15, rise, parity = 0)
            assert(mine.size == reference.size)
            mine.indices.forEach { row ->
                assert(mine[row].size == reference[row].size)
                mine[row].indices.forEach {
                    assert(mine[row][it].isCloseTo(reference[row][it], 1e-12))
                }
            }
        }
    }

    /**
     * The other parity is a **different sheet**: on a uniform row its stations are the sites the
     * first parity leaves empty, so the two partition the plane lattice and neither is a subset
     * of the other.
     */
    @Test
    fun `gate 3 symmetry the two parities partition the upward station lattice`() {
        val zero = mixedDomainUpwardSites(uniform, 15, rise, parity = 0)
        val one = mixedDomainUpwardSites(uniform, 15, rise, parity = 1)
        assert(zero.sumOf { it.size } + one.sumOf { it.size } == 15 * 7)
        zero.indices.forEach { row ->
            zero[row].forEach { site ->
                assert(one[row].none { abs(it - site) < 1e-9 })
            }
        }
    }

    // ------------------------------------------------------------------ gate 4 — exactness

    /** `C-0133`'s quarter base pair, recovered as the honeycomb's scaffold half-turn residual. */
    @Test
    fun `gate 4 exactness the honeycomb scaffold offset is a quarter base pair short`() {
        val honeycomb = HelixCrossoverLattice.HONEYCOMB
        assert(honeycomb.scaffoldOffsetResidualBasePairs(10.5).isCloseTo(0.25))
        assert(honeycomb.scaffoldOffsetResidualDegrees(10.5).isCloseTo(0.25 * 360.0 / 10.5))
        assert(honeycomb.scaffoldOffsetResidualDegrees(10.5).isCloseTo(8.571428571428571))
    }

    /** The admissible-width density: 3 residues per 21 bp against the square sheet's 1 per 32. */
    @Test
    fun `gate 4 exactness the honeycomb width list is denser than the square sheet's`() {
        val square = HelixCrossoverLattice.SQUARE_SHEET
        val honeycomb = HelixCrossoverLattice.HONEYCOMB
        val squareDensity = square.admissibleRowResidues().size.toDouble() /
                square.azimuthPeriodBasePairs
        val perTurnSense = honeycomb.turnPairResidues(0, 1).size.toDouble() /
                honeycomb.azimuthPeriodBasePairs
        assert(squareDensity.isCloseTo(1.0 / 32.0))
        assert(perTurnSense.isCloseTo(3.0 / 21.0))
        assert((perTurnSense / squareDensity).isCloseTo(32.0 * 3.0 / 21.0))
    }

    /** The sensitivity the whole favourable honeycomb result rests on: one constant. */
    @Test
    fun `gate 5 literature the honeycomb advantage is exactly as good as the 10 point 5 constant`() {
        val honeycomb = HelixCrossoverLattice.HONEYCOMB
        assert(honeycomb.accumulatedMismatchDegrees(112, 360.0 / 10.5).isCloseTo(0.0))
        val atTenFourFour = honeycomb.accumulatedMismatchDegrees(112, 360.0 / 10.44)
        assert(abs(atTenFourFour) > 20.0)
        assert(abs(atTenFourFour) < 25.0)
    }
}
