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
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-19`'s entry topologies, gate by gate.
 *
 * `C-0020`'s headline — the in-plane transfer ratio is exactly 1 for a tether aligned with the
 * helices — holds **because** its model gives the tether one point of one duplex to enter
 * through, which makes the attachment the most loaded member by construction. These tests
 * check the generalisation of that load introduction to the entry topologies an origami
 * attachment actually has, and the two bounds that settle most of the answer before any
 * matrix is assembled: the cut-equilibrium pigeonhole `η ≥ 1/D`, and the short-bond limit
 * `η → 1/m` for a bond spanning `m` duplexes.
 */

private val entrySheet = origamiSheet(
    Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
)

private val ENTRY_SHEAR = Gen1Tile.crossoverInPlaneStiffness()

private const val ENTRY_BEAMS = 15

private fun entryMembrane(
    beamCount: Int = ENTRY_BEAMS,
    phase: Int = 8,
    subdivisions: Int = 2,
    crossoverShearStiffness: Double = ENTRY_SHEAR,
    regularisation: Double = OrigamiMembrane.DEFAULT_REGULARISATION,
    extraStations: List<Double> = emptyList()
): OrigamiMembrane = OrigamiMembrane(
    sheet = entrySheet,
    lengthX = Gen1Tile.EDGE_X,
    beamCount = beamCount,
    columns = CrossoverLayout.atBasePairPhase(phase, entrySheet, Gen1Tile.EDGE_X),
    crossoverShearStiffness = crossoverShearStiffness,
    crossoverNormalStiffness = crossoverShearStiffness,
    subdivisions = subdivisions,
    regularisation = regularisation,
    extraStations = extraStations
)

class EntryTopologyTest {

    // ------------------------------------------------------------------ gate 1

    @Test
    fun `gate 1 an entry topology's shares are a partition of the tether tension`() {
        val band = EntryTopology.duplexBand("band", 6, 3, -20.0)
        assert(band.bonds.size == 3)
        assert(band.bonds.sumOf { it.share }.isCloseTo(1.0))
        assert(band.duplexSpan == 3)
        assert(band.stations.size == 1)
        assertFailsWith<IllegalArgumentException> {
            EntryTopology("bad", listOf(EntryBond(0, 0.0, 0.4), EntryBond(1, 0.0, 0.4)))
        }
        assertFailsWith<IllegalArgumentException> { EntryTopology("empty", emptyList()) }
        assertFailsWith<IllegalArgumentException> {
            EntryTopology("negative", listOf(EntryBond(0, 0.0, 1.4), EntryBond(1, 0.0, -0.4)))
        }
    }

    @Test
    fun `gate 1 the tether load introduction applies exactly the tension and no net force`() {
        val lattice = entryMembrane()
        val near = EntryTopology.duplexBand("near", 6, 2, -Gen1Tile.EDGE_X / 2.0)
        val far = EntryTopology.duplexBand("far", 6, 2, Gen1Tile.EDGE_X / 2.0)
        val loads = lattice.tetherLoads(near, far, force = 1.0)
        assert(loads.size == 4)
        assert(loads.sumOf { it.forceAlong }.isCloseTo(0.0, 1e-12))
        assert(loads.sumOf { it.forceAcross }.isCloseTo(0.0, 1e-12))
        assert(loads.filter { it.x > 0.0 }.sumOf { it.forceAlong }.isCloseTo(1.0))
    }

    @Test
    fun `gate 1 a base-pair footprint lands its bonds on exact base-pair stations`() {
        val footprint = EntryTopology.baseFootprint(
            "footprint", duplex = 7, from = -Gen1Tile.EDGE_X / 2.0, bases = 12,
            rise = Gen1Tile.RISE_PER_BASE_PAIR, inward = true
        )
        assert(footprint.bonds.size == 12)
        assert(footprint.duplexSpan == 1)
        footprint.bonds.forEachIndexed { index, bond ->
            assert(
                bond.x.isCloseTo(-Gen1Tile.EDGE_X / 2.0 + index * Gen1Tile.RISE_PER_BASE_PAIR)
            )
            assert(bond.share.isCloseTo(1.0 / 12.0))
        }
        // and every station is a node of a lattice built with them, so that the axial force
        // the bond introduces is resolved rather than averaged across an element
        val lattice = entryMembrane(extraStations = footprint.stations)
        footprint.stations.forEach { station ->
            assert(lattice.nodeX.any { abs(it - station) < 1e-9 })
        }
    }

    // ------------------------------------------------------------------ gate 2

    @Test
    fun `gate 2 one point on one duplex reproduces C-0020's transfer ratio of exactly one`() {
        // the control. If this moves, nothing else in T-19 is a comparison with C-0020
        val lattice = entryMembrane()
        val near = EntryTopology.singlePoint("near", 7, -Gen1Tile.EDGE_X / 2.0)
        val far = EntryTopology.singlePoint("far", 7, Gen1Tile.EDGE_X / 2.0)
        val solution = lattice.solve(lattice.tetherLoads(near, far))
        assert(solution.peakDuplexAxialForce.isCloseTo(1.0, 1e-5))
    }

    @Test
    fun `gate 2 an m-duplex bond with an equal split enters at exactly one over m`() {
        // The exact statement: no crossover sits on the rim, so the entry element of each
        // bonded duplex carries its own share and nothing else. The residual is the
        // regularising bed's pull on the entry node — softening the bed by four decades
        // tightens the agreement by four decades, which makes that attribution a measurement
        // rather than an excuse.
        listOf(
            OrigamiMembrane.DEFAULT_REGULARISATION to 1e-4,
            OrigamiMembrane.DEFAULT_REGULARISATION * 1e-4 to 1e-8
        ).forEach { (bed, tolerance) ->
            val lattice = entryMembrane(regularisation = bed)
            listOf(1, 2, 3, 5).forEach { m ->
                val near = EntryTopology.duplexBand("near", 5, m, -Gen1Tile.EDGE_X / 2.0)
                val far = EntryTopology.duplexBand("far", 5, m, Gen1Tile.EDGE_X / 2.0)
                val solution = lattice.solve(lattice.tetherLoads(near, far))
                (0 until m).forEach { i ->
                    val entry = lattice.axialForceAt(
                        solution, 5 + i, -Gen1Tile.EDGE_X / 2.0 + 1e-6
                    )
                    assert(abs(entry).isCloseTo(1.0 / m, tolerance))
                }
            }
        }
    }

    @Test
    fun `gate 2 the peak of an m-duplex bond sits just above one over m and not below`() {
        // and the *peak* is not exactly the entered share: the connector arm couples each
        // duplex's in-plane rotation into the interface sliding, so a duplex inside the band
        // can pick up a little from its bonded neighbours. It is a few per cent, it is one
        // sided, and the study reports it rather than assuming it away
        val lattice = entryMembrane()
        listOf(1, 2, 3, 5).forEach { m ->
            val near = EntryTopology.duplexBand("near", 5, m, -Gen1Tile.EDGE_X / 2.0)
            val far = EntryTopology.duplexBand("far", 5, m, Gen1Tile.EDGE_X / 2.0)
            val peak = lattice.solve(lattice.tetherLoads(near, far)).peakDuplexAxialForce
            assert(peak >= 1.0 / m - 1e-5)
            assert(peak <= 1.05 / m)
        }
    }

    @Test
    fun `gate 2 a bond to every duplex attains the pigeonhole floor of one over D`() {
        val lattice = entryMembrane()
        val near = EntryTopology.duplexBand("near", 0, ENTRY_BEAMS, -Gen1Tile.EDGE_X / 2.0)
        val far = EntryTopology.duplexBand("far", 0, ENTRY_BEAMS, Gen1Tile.EDGE_X / 2.0)
        val solution = lattice.solve(lattice.tetherLoads(near, far))
        assert(solution.peakDuplexAxialForce.isCloseTo(1.0 / ENTRY_BEAMS, 1e-4))
        // and it stores nothing in any crossover: every duplex is strained identically
        assert(solution.peakCrossoverForce < 1e-9)
    }

    @Test
    fun `gate 2 a mirror-symmetric strip splits a rigid bond exactly in half`() {
        // two duplexes that are exchanged by a symmetry of the lattice must take equal shares
        // whatever the compliance matrix is, and that is a property of the solver rather than
        // of the tile
        val lattice = OrigamiMembrane(
            sheet = entrySheet,
            lengthX = Gen1Tile.EDGE_X,
            beamCount = 2,
            columns = CrossoverLayout.centred(8, entrySheet.crossoverSpacing / 2.0),
            crossoverShearStiffness = ENTRY_SHEAR,
            crossoverNormalStiffness = ENTRY_SHEAR
        )
        val near = EntryTopology.duplexBand("near", 0, 2, -Gen1Tile.EDGE_X / 2.0)
        val far = EntryTopology.duplexBand("far", 0, 2, Gen1Tile.EDGE_X / 2.0)
        val shares = lattice.compatibleShares(near, far)
        assert(shares[0].isCloseTo(0.5, 1e-9))
        assert(shares[1].isCloseTo(0.5, 1e-9))
    }

    @Test
    fun `gate 2 a centred three-duplex bond splits symmetrically about the middle duplex`() {
        val lattice = entryMembrane()
        val near = EntryTopology.duplexBand("near", 6, 3, -Gen1Tile.EDGE_X / 2.0)
        val far = EntryTopology.duplexBand("far", 6, 3, Gen1Tile.EDGE_X / 2.0)
        val shares = lattice.compatibleShares(near, far)
        assert(shares[0].isCloseTo(shares[2], 1e-9))
        assert(shares.sum().isCloseTo(1.0))
    }

    // ------------------------------------------------------------------ gate 3

    @Test
    fun `gate 3 the duplex axial forces on a cut sum to the applied force for every topology`() {
        val lattice = entryMembrane()
        listOf(
            EntryTopology.singlePoint("p", 7, -Gen1Tile.EDGE_X / 2.0) to
                    EntryTopology.singlePoint("p", 7, Gen1Tile.EDGE_X / 2.0),
            EntryTopology.duplexBand("b", 3, 4, -Gen1Tile.EDGE_X / 2.0) to
                    EntryTopology.duplexBand("b", 3, 4, Gen1Tile.EDGE_X / 2.0)
        ).forEach { (near, far) ->
            val solution = lattice.solve(lattice.tetherLoads(near, far))
            val cut = (0 until ENTRY_BEAMS).sumOf { lattice.axialForceAt(solution, it, 0.0) }
            assert(cut.isCloseTo(1.0, 1e-4))
        }
    }

    @Test
    fun `gate 3 no entry topology can go below the pigeonhole floor`() {
        // the cheap bound, asserted rather than asserted-about: on a D-duplex tile the axial
        // forces on a cut sum to the applied force, so some duplex carries at least 1/D
        val lattice = entryMembrane()
        listOf(1, 2, 4, 8, ENTRY_BEAMS).forEach { m ->
            val near = EntryTopology.duplexBand("near", 0, m, -Gen1Tile.EDGE_X / 2.0)
            val far = EntryTopology.duplexBand("far", 0, m, Gen1Tile.EDGE_X / 2.0)
            val solution = lattice.solve(lattice.tetherLoads(near, far))
            // the slack is the regularising bed's own pull on the entry node, measured in
            // the gate-2 test above and four orders below the floor it qualifies
            assert(solution.peakDuplexAxialForce >= 1.0 / ENTRY_BEAMS - 1e-5)
        }
    }

    @Test
    fun `gate 3 the compatible split equalises the extension of every bonded path`() {
        // the defining property of a rigid staple: all its bonds move together along the pull
        val lattice = entryMembrane()
        val near = EntryTopology.duplexBand("near", 2, 4, -Gen1Tile.EDGE_X / 2.0)
        val far = EntryTopology.duplexBand("far", 2, 4, Gen1Tile.EDGE_X / 2.0)
        val shares = lattice.compatibleShares(near, far)
        val solution = lattice.solve(
            lattice.tetherLoads(near.withShares(shares), far.withShares(shares))
        )
        val extensions = (0 until 4).map { i ->
            val duplex = 2 + i
            solution.displacementAlong(Gen1Tile.EDGE_X / 2.0, lattice.duplexY(duplex)) -
                    solution.displacementAlong(-Gen1Tile.EDGE_X / 2.0, lattice.duplexY(duplex))
        }
        extensions.forEach { assert(it.isCloseTo(extensions[0], 1e-6)) }
    }

    @Test
    fun `gate 3 a rigid bond never splits better than an equal one`() {
        // the stiffest path takes more than its share, so the compatible split can only push
        // the peak up — the halving is exact only in the compliant limit
        val lattice = entryMembrane()
        listOf(2, 3, 4).forEach { m ->
            val near = EntryTopology.duplexBand("near", 0, m, -Gen1Tile.EDGE_X / 2.0)
            val far = EntryTopology.duplexBand("far", 0, m, Gen1Tile.EDGE_X / 2.0)
            val shares = lattice.compatibleShares(near, far)
            assert(shares.max() >= 1.0 / m - 1e-12)
            assert(shares.sum().isCloseTo(1.0))
        }
    }

    // ------------------------------------------------------------------ gate 4

    @Test
    fun `gate 4 a base-pair footprint converges under nested mesh refinement`() {
        // nested refinements only, 1 subset 2 subset 4
        val footprint = EntryTopology.baseFootprint(
            "near", 7, -Gen1Tile.EDGE_X / 2.0, 4, Gen1Tile.RISE_PER_BASE_PAIR, inward = true
        )
        val far = EntryTopology.baseFootprint(
            "far", 7, Gen1Tile.EDGE_X / 2.0, 4, Gen1Tile.RISE_PER_BASE_PAIR, inward = false
        )
        val peaks = listOf(1, 2, 4).map { subdivisions ->
            val lattice = entryMembrane(
                subdivisions = subdivisions,
                extraStations = footprint.stations + far.stations
            )
            val solution = lattice.solve(lattice.tetherLoads(footprint, far))
            assert(abs(solution.regularisationForceAlong) < 1e-9)
            solution.peakDuplexAxialForce
        }
        assert(abs(peaks[2] - peaks[1]) / peaks[2] < 0.01)
    }

    // ------------------------------------------------------------------ gate 5

    @Test
    fun `gate 5 spreading a bond along one duplex cannot relieve it`() {
        // the m = 1 bound: the duplex must carry the whole tension somewhere inboard of the
        // footprint, so a footprint buys at most the load shed over its own length — which is
        // why whatever a footprint is worth is worth on the JOINT and not on the sheet
        val far = EntryTopology.singlePoint("far", 7, Gen1Tile.EDGE_X / 2.0)
        val peaks = listOf(1, 8, 20).map { bases ->
            val near = EntryTopology.baseFootprint(
                "near", 7, -Gen1Tile.EDGE_X / 2.0, bases, Gen1Tile.RISE_PER_BASE_PAIR,
                inward = true
            )
            val lattice = entryMembrane(subdivisions = 1, extraStations = near.stations)
            lattice.solve(lattice.tetherLoads(near, far)).peakDuplexAxialForce
        }
        // the far end is a single point, so the peak there is one whatever the near end does
        peaks.forEach { assert(it.isCloseTo(1.0, 1e-4)) }
    }

    @Test
    fun `gate 5 a two-duplex bond relieves the crossovers rather than loading them`() {
        // both bonded duplexes move together, so the interface between them slides less, not
        // more — the two-duplex bond's cost cannot be a crossover force at the attachment
        val lattice = entryMembrane()
        val single = lattice.solve(
            lattice.tetherLoads(
                EntryTopology.singlePoint("near", 7, -Gen1Tile.EDGE_X / 2.0),
                EntryTopology.singlePoint("far", 7, Gen1Tile.EDGE_X / 2.0)
            )
        )
        val paired = lattice.solve(
            lattice.tetherLoads(
                EntryTopology.duplexBand("near", 7, 2, -Gen1Tile.EDGE_X / 2.0),
                EntryTopology.duplexBand("far", 7, 2, Gen1Tile.EDGE_X / 2.0)
            )
        )
        assert(paired.peakCrossoverForce < single.peakCrossoverForce)
        assert(paired.peakDuplexAxialForce < single.peakDuplexAxialForce)
    }

}
