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
import com.xemantic.nano.plentyofroom.anchoring.rasterUpwardSites
import com.xemantic.nano.plentyofroom.anchoring.rasterColumnLayout
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-189` — can `C-0086`'s 112 bp seamless raster row be **twist-corrected**?
 *
 * `C-0086` quantises the row on **connectivity**: a boustrophedon's successive scaffold crossovers
 * are the two ends of one row, so Rothemund's *"odd number of half turns"* binds the row length.
 * A twist correction wants the **mean** inter-column domain to realise B-DNA's twist — Snodin's
 * 31/32 bp mixed sections, Rothemund's *"helical domain lengths … by single bases"*. This file
 * tests whether the two demands can be met together, and what is left when they cannot.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 */
class TwistCorrectedRasterTest {

    private val rise = Gen1Tile.RISE_PER_BASE_PAIR
    private val hinge = Gen1Tile.crossoverHingeStiffness()

    /** `C-0086`'s buildable row: seven 16 bp domains, 112 bp, 38.08 nm. */
    private val uniform = RasterRow(List(7) { 16 })

    /** The twist-corrected row this task constructs: five 16 bp domains and two 15 bp. */
    private val corrected = RasterRow(listOf(16, 16, 15, 16, 15, 16, 16))

    // ------------------------------------------------------------------ gate 1 — dimensional

    @Test
    fun `gate 1 dimensional a row length is a base pair count and its width is a rise times it`() {
        assert(uniform.basePairs == 112)
        assert(corrected.basePairs == 110)
        assert(uniform.width(rise).isCloseTo(38.08))
        assert(corrected.width(rise).isCloseTo(37.4))
        assert(uniform.width(2.0 * rise).isCloseTo(2.0 * uniform.width(rise)))
    }

    @Test
    fun `gate 1 dimensional the design twist is degrees per base pair and carries no length`() {
        assert(uniform.designTwistPerBase.isCloseTo(33.75))
        assert(corrected.designTwistPerBase.isCloseTo(180.0 * 21.0 / 110.0))
    }

    @Test
    fun `gate 1 dimensional an unphysical row is refused`() {
        assertFailsWith<IllegalArgumentException> { RasterRow(emptyList()) }
        assertFailsWith<IllegalArgumentException> { RasterRow(listOf(16, 0)) }
        assertFailsWith<IllegalArgumentException> { RasterRow(listOf(16), halfTurnsPerDomain = 0) }
    }

    // --------------------------------------------------------------- gate 2 — limiting cases

    @Test
    fun `gate 2 limiting THE THEOREM no integer row is an odd half turn count at B-DNA twist`() {
        // the cheap bound, and it is exact: N = 180 q / 34.2857... = 21 q / 4 with q odd,
        // and 21 q is odd, so the quotient is never an integer.
        val offending = (1..4001 step 2).filter { q ->
            val exact = exactHalfTurnBasePairs(q, B_DNA_TWIST_PER_BASE)
            abs(exact - Math.round(exact)) < 1.0e-9
        }
        assert(offending.isEmpty())
    }

    @Test
    fun `gate 2 limiting the residual is exactly a quarter base pair at every odd half turn`() {
        (1..401 step 2).forEach { q ->
            val exact = exactHalfTurnBasePairs(q, B_DNA_TWIST_PER_BASE)
            val nearest = Math.round(exact).toInt()
            assert(abs(abs(exact - nearest) - 0.25) < 1.0e-9)
        }
    }

    @Test
    fun `gate 2 limiting a row of equal domains is C-0086's own row and its mismatch is C-0107's`() {
        assert(uniform.seamlessAdmissible)
        assert(uniform.halfTurns == 21)
        val mismatch = uniform.twistRateMismatch(rise)
        assert(mismatch.isCloseTo(twistRateMismatch(33.75, B_DNA_TWIST_PER_BASE, rise)))
    }

    @Test
    fun `gate 2 limiting an even domain count is not seamless admissible`() {
        assert(!RasterRow(List(6) { 16 }).seamlessAdmissible)
        assert(RasterRow(List(5) { 16 }).seamlessAdmissible)
    }

    @Test
    fun `gate 2 limiting a rigid hinge holds the register at zero and a free one lets it run`() {
        val soft = columnRegisterField(uniform.domains, 460.0, 1.0e-6, 10.2, rise)
        val stiff = columnRegisterField(uniform.domains, 460.0, 1.0e12, 10.2, rise)
        assert(abs(stiff.last()) < 1.0e-6)
        // with no hinge at all the whole accumulation surfaces, odd about the row centre
        assert(soft.last().isCloseTo(uniform.totalMismatchRadians() / 2.0, 1e-5))
        assert(soft.first().isCloseTo(-soft.last(), 1e-7))
    }

    // ------------------------------------------------------ gate 3 — symmetry and reproduction

    @Test
    fun `gate 3 symmetry the uniform chain reproduces EdgeTwistRelief's own discrete solve`() {
        val model = EdgeTwistRelief(460.0, hinge, 10.2, uniform.width(rise))
        val mismatch = uniform.twistRateMismatch(rise)
        val reference = model.discreteEndResidual(mismatch, uniform.domainCount)
        val mine = columnRegisterField(uniform.domains, 460.0, hinge, 10.2, rise).last()
        assert(abs(mine - reference) / abs(reference) < 1.0e-10)
    }

    @Test
    fun `gate 3 symmetry a centro-symmetric domain sequence gives an odd register field`() {
        val u = columnRegisterField(corrected.domains, 460.0, hinge, 10.2, rise)
        u.indices.forEach { j ->
            assert(abs(u[j] + u[u.size - 1 - j]) < 1.0e-12)
        }
    }

    @Test
    fun `gate 3 symmetry the generalised column layout reproduces rasterColumnLayout at 112 bp`() {
        val sheet = origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)
        val reference = rasterColumnLayout(8, sheet, uniform.width(rise), true, CrossoverLayout.EDGE_MARGIN)
        val mine = twistCorrectedColumnLayout(uniform.domains, rise, CrossoverLayout.EDGE_MARGIN)
        assert(mine.positions.size == reference.positions.size)
        mine.positions.indices.forEach { assert(abs(mine.positions[it] - reference.positions[it]) < 1.0e-12) }
        assert(mine.parities == reference.parities)
    }

    @Test
    fun `gate 3 symmetry the generalised upward lattice reproduces rasterUpwardSites at 112 bp`() {
        val reference = rasterUpwardSites(8, uniform.width(rise), 15, true, rise, CrossoverLayout.EDGE_MARGIN)
        val mine = twistCorrectedUpwardSites(uniform.domains, 15, rise)
        assert(mine.size == reference.size)
        mine.indices.forEach { row ->
            assert(mine[row].size == reference[row].size)
            mine[row].indices.forEach { i ->
                assert(abs(mine[row][i] - reference[row][i]) < 1.0e-12)
            }
        }
    }

    // -------------------------------------------------------------------- gate 4 — exactness

    @Test
    fun `gate 4 exactness the register field is exactly linear in the twist mismatch`() {
        // doubling the natural-versus-design gap doubles the whole field, at every node
        val base = columnRegisterField(uniform.domains, 460.0, hinge, 10.2, rise)
        val doubled = columnRegisterField(
            uniform.domains, 460.0, hinge, 10.2, rise,
            naturalTwistPerBase = 2.0 * B_DNA_TWIST_PER_BASE - 33.75
        )
        base.indices.forEach { assert(abs(doubled[it] - 2.0 * base[it]) < 1.0e-12 * abs(base[it]).coerceAtLeast(1.0e-9)) }
    }

    @Test
    fun `gate 4 exactness the domain mix is the only one that sums to the row`() {
        val mix = evenDomainMix(110, 7)
        assert(mix.sum() == 110)
        assert(mix.count { it == 15 } == 2)
        assert(mix.count { it == 16 } == 5)
        assert(mix == mix.reversed())
        assert(mix == listOf(16, 16, 15, 16, 15, 16, 16))
        assert(evenDomainMix(112, 7).all { it == 16 })
    }

    @Test
    fun `gate 4 exactness the best seamless row near a target width is 110 bp`() {
        val rows = seamlessTwistCorrectedRows(maximumDomains = 13)
        val best = rows.minByOrNull { abs(it.width(rise) - 40.0) }!!
        assert(best.basePairs == 110)
        assert(best.domainCount == 7)
        assert(rows.all { it.seamlessAdmissible })
    }

    // ------------------------------------------------------ gate 5 — literature and upstream

    @Test
    fun `gate 5 literature Snodin's 31 bp per equivalent junction is what the mix delivers`() {
        // two consecutive domains are one interface's own crossover spacing: 31 or 32 bp
        val perInterface = corrected.domains.zipWithNext { a, b -> a + b }
        assert(perInterface.all { it == 31 || it == 32 })
        assert(perInterface.any { it == 31 })
    }

    @Test
    fun `gate 5 upstream the corrected row's total residual is exactly a quarter base pair`() {
        val residual = abs(corrected.basePairs - exactHalfTurnBasePairs(corrected.halfTurns, B_DNA_TWIST_PER_BASE))
        assert(residual.isCloseTo(0.25))
        // and C-0086's own row is seven times worse
        val theirs = abs(uniform.basePairs - exactHalfTurnBasePairs(uniform.halfTurns, B_DNA_TWIST_PER_BASE))
        assert(theirs.isCloseTo(1.75))
        assert((theirs / residual).isCloseTo(7.0))
    }

    @Test
    fun `gate 3 symmetry mirroring the out-of-plane offsets restores the station lattice symmetry`() {
        val plain = twistCorrectedUpwardSites(corrected.domains, 15, rise)
        val mirrored = twistCorrectedUpwardSites(corrected.domains, 15, rise, mirrorOffsets = true)
        fun symmetric(lattice: List<List<Double>>): Boolean = lattice.indices.all { row ->
            val mine = lattice[row]
            val partner = lattice[lattice.size - 1 - row].map { -it }.sorted()
            mine.size == partner.size && mine.zip(partner).all { (a, b) -> abs(a - b) < 1.0e-9 }
        }
        assert(!symmetric(plain))
        assert(symmetric(mirrored))
        // and on a row of equal domains the two are the same lattice
        assert(
            twistCorrectedUpwardSites(uniform.domains, 15, rise, mirrorOffsets = true) ==
                    twistCorrectedUpwardSites(uniform.domains, 15, rise)
        )
    }

    @Test
    fun `gate 1 dimensional the azimuth departure is 4 degrees at 8 bp and 30 at 7`() {
        assert(azimuthDeparture(8).isCloseTo(8.0 * B_DNA_TWIST_PER_BASE - 270.0))
        assert(azimuthDeparture(8).isCloseTo(4.2857142857, 1e-8))
        assert(azimuthDeparture(7).isCloseTo(-30.0, 1e-8))
    }
}
