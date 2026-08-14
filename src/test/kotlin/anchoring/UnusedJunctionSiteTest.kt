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
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-119`, leaf `A8.2` — whether a flexure hinge can be rooted on a junction site the
 * single-layer sheet does not use, which is `C-0054`'s own named falsifier.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 * The spine is that a square-lattice helix has **four** crossover azimuths at 8 bp intervals
 * (Ke et al., *JACS* **131**:15903, read directly) and a single-layer sheet occupies **two** of
 * them, so the sheet's own 56 crossovers are not the inventory a hinge must be drawn from.
 */
class UnusedJunctionSiteTest {

    private val edgeX = Gen1Tile.EDGE_X

    private val rows = 15

    /** `C-0039`'s `E5a1` arm at 45 paths, quoted; the study re-derives it from its own library. */
    private val arm = 9.131

    // --------------------------------------------------------------- gate 1: dimensional

    @Test
    fun `gate 1 should give the square lattice a twist of exactly 32 base pairs per 3 turns`() {
        assert(SQUARE_LATTICE_BASE_PAIRS_PER_TURN.isCloseTo(32.0 / 3.0, relativeTolerance = 1e-15))
        assert(SQUARE_LATTICE_DEGREES_PER_BASE_PAIR.isCloseTo(33.75, relativeTolerance = 1e-15))
    }

    @Test
    fun `gate 1 should make the azimuth a pure angle linear in the base-pair count`() {
        assert(azimuthDegrees(4.0).isCloseTo(2.0 * azimuthDegrees(2.0)))
        assert(azimuthDegrees(1.0, basePairsPerTurn = 21.0).isCloseTo(
            0.5 * azimuthDegrees(1.0, basePairsPerTurn = 10.5)
        ))
    }

    @Test
    fun `gate 1 should make the register departure linear in the offset and zero at the design twist`() {
        assert(registerDeparture(8).isCloseTo(4.285714285714286, relativeTolerance = 1e-12))
        assert(registerDeparture(16).isCloseTo(2.0 * registerDeparture(8), relativeTolerance = 1e-12))
        assert(
            registerDeparture(
                8,
                preferredBasePairsPerTurn = SQUARE_LATTICE_BASE_PAIRS_PER_TURN
            ).isCloseTo(0.0, relativeTolerance = 1e-12)
        )
        // the finding: the UNUSED site is off-register by half what the USED one is
        assert(registerDeparture(8) < registerDeparture(16))
    }

    @Test
    fun `gate 1 should make every site count a count and refuse unphysical arguments`() {
        assertFailsWith<IllegalArgumentException> { junctionSites(-1, edgeX, rows) }
        assertFailsWith<IllegalArgumentException> { junctionSites(0, -1.0, rows) }
        assertFailsWith<IllegalArgumentException> { junctionSites(0, edgeX, 1) }
        assertFailsWith<IllegalArgumentException> { registerDeparture(-1) }
        assertFailsWith<IllegalArgumentException> { azimuthDegrees(1.0, basePairsPerTurn = 0.0) }
        assertFailsWith<IllegalArgumentException> { placeUpwardArms(0, edgeX, rows, -1.0) }
        assertFailsWith<IllegalArgumentException> { scaffoldBasePairs(0, edgeX) }
        assertFailsWith<IllegalArgumentException> { junctionSiteInventory(0, edgeX, 1) }
    }

    // --------------------------------------------------------------- gate 2: limiting cases

    @Test
    fun `gate 2 should put the four azimuths at exactly the compass points of the square lattice`() {
        // Ke et al.: 0 bp north, 8 bp west (0.75 turns), 16 bp south (1.5), 24 bp east (2.25)
        assert(CrossoverAzimuth.NORTH.designAzimuthDegrees.isCloseTo(0.0, relativeTolerance = 1e-12))
        assert(CrossoverAzimuth.WEST.designAzimuthDegrees.isCloseTo(270.0, relativeTolerance = 1e-14))
        assert(CrossoverAzimuth.SOUTH.designAzimuthDegrees.isCloseTo(180.0, relativeTolerance = 1e-14))
        assert(CrossoverAzimuth.EAST.designAzimuthDegrees.isCloseTo(90.0, relativeTolerance = 1e-14))
    }

    @Test
    fun `gate 2 should make exactly two of the four azimuths out of the sheet plane`() {
        assert(CrossoverAzimuth.entries.count { it.outOfPlane } == 2)
        assert(CrossoverAzimuth.entries.count { !it.outOfPlane } == 2)
        // and the out-of-plane pair is exactly a quarter turn from the in-plane pair
        assert(
            abs(CrossoverAzimuth.EAST.designAzimuthDegrees -
                    CrossoverAzimuth.NORTH.designAzimuthDegrees).isCloseTo(90.0, relativeTolerance = 1e-14)
        )
    }

    @Test
    fun `gate 2 should reproduce caDNAno's honeycomb rule from the same arithmetic`() {
        // Douglas et al.: 7 bp is two-thirds of a turn at 10.5 bp per turn, so 21 bp per pair
        assert(azimuthDegrees(7.0, basePairsPerTurn = 10.5).isCloseTo(240.0, relativeTolerance = 1e-12))
        assert(azimuthDegrees(21.0, basePairsPerTurn = 10.5).isCloseTo(720.0, relativeTolerance = 1e-12))
    }

    @Test
    fun `gate 2 should leave a one-interface sheet with no interface site it can spare`() {
        val inventory = junctionSiteInventory(6, edgeX, 2)
        assert(inventory.interfaceSites > 0)
        assert(inventory.upwardSites > 0)
        // two duplexes, one interface: the pigeonhole leaves nothing in plane, and the
        // out-of-plane inventory is untouched by it
        assert(inventory.inPlaneHingeCeiling == inventory.interfaceSites - 1)
    }

    @Test
    fun `gate 2 should place no arm longer than the tile and every arm inside it`() {
        assert(placeUpwardArms(6, edgeX, rows, edgeX * 2.0).arms == 0)
        placeUpwardArms(6, edgeX, rows, arm).placements.forEach {
            assert(it.low >= -edgeX / 2.0 - 1e-9)
            assert(it.high <= edgeX / 2.0 + 1e-9)
        }
    }

    // --------------------------------------------------------------- gate 3: symmetry, conservation

    @Test
    fun `gate 3 should reproduce C-0015's own inventory from the azimuth arithmetic at every phase`() {
        (0 until 32).forEach { phase ->
            val fromAzimuth = junctionSites(phase, edgeX, rows)
                .filter { it.azimuth == CrossoverAzimuth.NORTH && it.duplex < rows - 1 }
                .map { it.duplex to Math.round(it.x * 1e6) }
                .toSet()
            val fromLayout = hingeSites(phase, edgeX, rows)
                .map { it.interfaceIndex to Math.round(it.x * 1e6) }
                .toSet()
            assert(fromAzimuth == fromLayout)
        }
    }

    @Test
    fun `gate 3 should conserve every junction site among the four azimuths at every phase`() {
        (0 until 32).forEach { phase ->
            val sites = junctionSites(phase, edgeX, rows)
            val byAzimuth = CrossoverAzimuth.entries.sumOf { a -> sites.count { it.azimuth == a } }
            assert(byAzimuth == sites.size)
            // every duplex sees every plane exactly once
            assert(sites.size == rows * junctionPlanes(phase, edgeX).size)
        }
    }

    @Test
    fun `gate 3 should make a 32 base-pair phase shift the identity and an 8 bp shift a rotation`() {
        fun sites(phase: Int, azimuth: CrossoverAzimuth) =
            junctionSites(phase, edgeX, rows)
                .filter { it.azimuth == azimuth }
                .map { it.duplex to Math.round(it.x * 1e6) }
                .toSet()
        (0 until 32).forEach { phase ->
            // a full period is the identity, azimuth class by azimuth class
            CrossoverAzimuth.entries.forEach { azimuth ->
                assert(sites(phase, azimuth) == sites(phase + 32, azimuth))
            }
            // one plane of shift advances every site by one azimuth class, at the same positions,
            // because the 8 bp plane lattice is invariant under its own pitch
            assert(sites(phase + 8, CrossoverAzimuth.NORTH) == sites(phase, CrossoverAzimuth.WEST))
            assert(sites(phase + 8, CrossoverAzimuth.EAST) == sites(phase, CrossoverAzimuth.NORTH))
        }
    }

    @Test
    fun `gate 3 should not share an out-of-plane site between two duplexes`() {
        val placement = placeUpwardArms(6, edgeX, rows, arm)
        val keys = placement.placements.map { it.row to Math.round(it.rootX * 1e6) }
        assert(keys.size == keys.toSet().size)
        // and because no site is shared, the greedy construction meets the independent bound
        assert(placement.arms == placement.independentRowBound)
    }

    @Test
    fun `gate 3 should leave the host sheet in one piece at every out-of-plane arm count`() {
        val ceiling = junctionSiteInventory(6, edgeX, rows).upwardSites
        (0..ceiling step 5).forEach { count ->
            val budget = outOfPlaneHingeBudget(6, edgeX, rows, count)
            assert(budget.components == 1)
            assert(budget.retainedInterfaceCrossovers == budget.sheetInventory)
        }
    }

    // --------------------------------------------------------------- gate 4: convergence

    @Test
    fun `gate 4 should make the 32 base-pair phase sweep complete`() {
        val coarse = (0 until 32).map { junctionSiteInventory(it, edgeX, rows).upwardSites }.toSet()
        val fine = (0 until 320).map {
            junctionSiteInventory(it, edgeX, rows).upwardSites
        }.toSet()
        assert(fine == coarse)
    }

    @Test
    fun `gate 4 should place deterministically`() {
        val a = placeUpwardArms(6, edgeX, rows, arm)
        val b = placeUpwardArms(6, edgeX, rows, arm)
        assert(a == b)
    }

    @Test
    fun `gate 4 should be insensitive to the edge margin convention`() {
        val here = junctionSiteInventory(6, edgeX, rows)
        val nudged = junctionSiteInventory(6, edgeX + 1e-6, rows)
        assert(here.upwardSites == nudged.upwardSites)
        assert(here.interfaceSites == nudged.interfaceSites)
    }

    // --------------------------------------------------------------- gate 5: literature, upstream

    @Test
    fun `gate 5 should reproduce Ke et al's own underwinding statement`() {
        // "33.75 degrees per bp average twist (or 32 bp per 3 turns)" against
        // "the preferred 34.3 degrees per bp or 10.5 bp per turn"
        assert(SQUARE_LATTICE_DEGREES_PER_BASE_PAIR.isCloseTo(33.75, relativeTolerance = 1e-14))
        assert(azimuthDegrees(1.0, basePairsPerTurn = 10.5).isCloseTo(34.3, relativeTolerance = 1e-3))
        assert(azimuthDegrees(8.0).isCloseTo(270.0, relativeTolerance = 1e-14))
        assert(azimuthDegrees(32.0).isCloseTo(1080.0, relativeTolerance = 1e-14))
    }

    @Test
    fun `gate 5 should reproduce C-0015's 56 and 49 crossover inventory`() {
        val eightColumn = listOf(6, 7, 8, 9, 10, 22, 23, 24, 25, 26)
        (0 until 32).forEach { phase ->
            val inventory = junctionSiteInventory(phase, edgeX, rows)
            assert(inventory.interfaceSites == if (phase in eightColumn) 56 else 49)
        }
    }

    @Test
    fun `gate 5 should reproduce C-0040's four crossovers per interface`() {
        val sites = junctionSites(6, edgeX, rows)
        (0 until rows - 1).forEach { interfaceIndex ->
            val count = sites.count {
                it.azimuth == CrossoverAzimuth.NORTH && it.duplex == interfaceIndex
            }
            assert(count == 4)
        }
    }

    @Test
    fun `gate 5 should reproduce C-0053's in-plane arm demand`() {
        assert((arm + OrigamiDuplex.INTERHELICAL).isCloseTo(11.821, relativeTolerance = 1e-4))
    }

    @Test
    fun `gate 5 should keep the whole design inside the M13 scaffold`() {
        val sheet = scaffoldBasePairs(rows, edgeX)
        assert(sheet == 15L * 118L)
        assert(sheet + armScaffoldBasePairs(45, arm) < M13_SCAFFOLD_NUCLEOTIDES)
    }

    // --------------------------------------------------------------- the finding itself

    @Test
    fun `should find an out-of-plane inventory larger than C-0054's whole in-plane ceiling`() {
        val best = (0 until 32).maxOf { junctionSiteInventory(it, edgeX, rows).upwardSites }
        assert(best > 42)
    }

    @Test
    fun `should use under a third of the junction sites its own lattice offers`() {
        (0 until 32).forEach { phase ->
            val inventory = junctionSiteInventory(phase, edgeX, rows)
            assert(inventory.usedFraction < 1.0 / 3.0)
        }
    }

    @Test
    fun `should beat C-0053's 25 arms without severing the host`() {
        val count = selfConsistentUpwardArmCount(edgeX, rows) { paths ->
            // C-0039's placed arm goes as the cube root of the path count
            arm * Math.cbrt(paths / 45.0)
        }
        assert(count > 25)
        assert(outOfPlaneHingeBudget(6, edgeX, rows, count).components == 1)
    }
}
