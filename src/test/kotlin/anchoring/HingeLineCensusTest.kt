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
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-81` — whether a 16-crossover hinge line exists on a 40 nm tile.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 * The spine of the task is that a hinge line is a set of **collinear** crossovers on **one**
 * interface, that the interface pitch is `32 bp` and not `16`, and that a count on a lattice is
 * not something a model can move.
 */
class HingeLineCensusTest {

    private val pitch = perInterfacePitch()

    private val tile = Gen1Tile.EDGE_X

    private val hinge = Gen1Tile.crossoverHingeStiffness()

    private val ei = Gen1Tile.DUPLEX_BENDING_RIGIDITY

    private val mandate = 100.0 / 3.0

    private val paths = 45

    /** `C-0034`'s adopted `A2` anchorage — the arm's own duplex end, two strand termini. */
    private val farStiffness = 78.2352941176

    // ---------------------------------------------------------------- gate 1 — dimensional

    @Test
    fun `gate 1 dimensional consistency - the per-interface pitch is a base-pair count times a rise`() {
        // 32 bp x 0.34 nm/bp = 10.88 nm, a length
        assert(pitch.isCloseTo(32.0 * 0.34))
        // and it is linear in both factors
        assert(perInterfacePitch(64.0, 0.34).isCloseTo(2.0 * pitch))
        assert(perInterfacePitch(32.0, 0.68).isCloseTo(2.0 * pitch))
    }

    @Test
    fun `gate 1 dimensional consistency - a hinge line of n crossovers demands n minus one pitches`() {
        assert(hingeLineLengthForCount(1, pitch).isCloseTo(0.0))
        assert(hingeLineLengthForCount(2, pitch).isCloseTo(pitch))
        assert(hingeLineLengthForCount(16, pitch).isCloseTo(15.0 * pitch))
        // and the demand and the census are exact inverses of each other
        (1..24).forEach { n ->
            assert(maximumHingeCount(hingeLineLengthForCount(n, pitch), pitch) == n)
        }
    }

    @Test
    fun `gate 1 dimensional consistency - the count is a length over a pitch, so doubling the pitch halves it`() {
        val long = maximumHingeCount(163.2, pitch)
        val short = maximumHingeCount(163.2, 2.0 * pitch)
        assert(long == 16)
        assert(short == 8)
    }

    @Test
    fun `gate 1 dimensional consistency - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { perInterfacePitch(0.0, 0.34) }
        assertFailsWith<IllegalArgumentException> { perInterfacePitch(32.0, -0.1) }
        assertFailsWith<IllegalArgumentException> { hingeLineLengthForCount(0, pitch) }
        assertFailsWith<IllegalArgumentException> { maximumHingeCount(-1.0, pitch) }
        assertFailsWith<IllegalArgumentException> { fanEffectiveHingeCount(0, 4) }
        assertFailsWith<IllegalArgumentException> { fanEffectiveHingeCount(4, 0) }
        assertFailsWith<IllegalArgumentException> { transverseHingeCount(0, 0) }
        assertFailsWith<IllegalArgumentException> { transverseHingeCount(15, 2) }
        assertFailsWith<IllegalArgumentException> {
            generalFanEffectiveCount(10.0, listOf(1.0), listOf(1, 2))
        }
    }

    // ---------------------------------------------------------------- gate 2 — limiting cases

    @Test
    fun `gate 2 limiting cases - a line shorter than one pitch holds exactly one crossover`() {
        assert(maximumHingeCount(0.0, pitch) == 1)
        assert(maximumHingeCount(0.99 * pitch, pitch) == 1)
        assert(maximumHingeCount(1.01 * pitch, pitch) == 2)
    }

    @Test
    fun `gate 2 limiting cases - one interface is one hinge, so the fan reduces to its own count at m equals one`() {
        (1..8).forEach { n -> assert(fanEffectiveHingeCount(1, n).isCloseTo(n.toDouble())) }
    }

    @Test
    fun `gate 2 limiting cases - a transverse line serves one parity, so it needs two n plus one duplexes`() {
        // 14 interfaces on 15 duplexes, 7 of each parity
        assert(transverseHingeCount(15, 0) == 7)
        assert(transverseHingeCount(15, 1) == 7)
        assert(transverseHingeCount(2, 0) == 1)
        assert(transverseHingeCount(2, 1) == 0)
        assert(duplexesForTransverseCount(16) == 33)
        assert(transverseHingeCount(duplexesForTransverseCount(16), 0) == 16)
    }

    @Test
    fun `gate 2 limiting cases - the fan effective count falls strictly with the number of interfaces`() {
        val counts = (1..8).map { fanEffectiveHingeCount(it, 4) }
        assert(counts.zipWithNext().all { (a, b) -> b < a })
        // 4 crossovers on each of 4 interfaces is SIXTEEN crossovers and 2.333 of hinge
        assert(fanEffectiveHingeCount(4, 4).isCloseTo(4.0 * 21.0 / 36.0))
        assert(fanEffectiveHingeCount(4, 4).isCloseTo(2.3333333333333335))
        // the lever sum is m(4m^2 - 1)/12 and the lever is (m - 1/2) d
        assert(fanLeverSum(4).isCloseTo(21.0))
        assert(fanLever(4).isCloseTo(3.5 * Gen1Tile.INTERHELICAL_SHEET))
    }

    // ------------------------------------------------- gate 3 — symmetry and conservation

    @Test
    fun `gate 3 conservation - the two parities' counts sum to the column count at every phase`() {
        hingeLineCensus(tile).forEach { record ->
            assert(record.evenInterfaces + record.oddInterfaces == record.columns)
        }
    }

    @Test
    fun `gate 3 symmetry - centro-symmetry holds exactly when columns plus duplexes is odd, and at 10 of 32 phases`() {
        val census = hingeLineCensus(tile)
        assert(census.size == 32)
        census.forEach { record ->
            assert(isCentroSymmetric(record.columns, 15) == ((record.columns + 15) % 2 == 1))
        }
        assert(census.count { isCentroSymmetric(it.columns, 15) } == 10)
    }

    @Test
    fun `gate 3 symmetry - the phase period is 32 bp and a 16 bp shift swaps the two parities`() {
        val census = hingeLineCensus(tile)
        (0 until 32).forEach { phase ->
            val shifted = census[(phase + 16) % 32]
            assert(census[phase].evenInterfaces == shifted.oddInterfaces)
            assert(census[phase].oddInterfaces == shifted.evenInterfaces)
        }
        // and a full period is the identity, which is what makes the sweep complete
        (0 until 32).forEach { phase ->
            assert(
                crossoversInLine(tile, phase * Gen1Tile.RISE_PER_BASE_PAIR, pitch) ==
                        crossoversInLine(tile, (phase + 32) * Gen1Tile.RISE_PER_BASE_PAIR, pitch)
            )
        }
    }

    @Test
    fun `gate 3 conservation - the tile inventory is the interface count times the per-interface count`() {
        hingeLineCensus(tile).forEach { record ->
            val inventory = tileCrossoverInventory(15, record.evenInterfaces, record.oddInterfaces)
            assert(inventory == 7 * record.evenInterfaces + 7 * record.oddInterfaces)
        }
    }

    @Test
    fun `gate 3 symmetry - the general fan reproduces the uniform one and is exact at a single hinge`() {
        val d = Gen1Tile.INTERHELICAL_SHEET
        (1..6).forEach { m ->
            val positions = (0 until m).map { it * d }
            val general = generalFanEffectiveCount(fanLever(m, d), positions, List(m) { 4 })
            assert(general.isCloseTo(fanEffectiveHingeCount(m, 4)))
        }
        // a single hinge at the root of a lever is its own count, exactly
        assert(generalFanEffectiveCount(10.0, listOf(0.0), listOf(9)).isCloseTo(9.0))
    }

    // ---------------------------------------------------------- gate 4 — convergence

    @Test
    fun `gate 4 convergence - the lattice fan converges to the continuum strip from above as three over two m`() {
        val ratios = (1..64).map { fanOverContinuum(it) }
        // monotone DOWN to one, and always above it: the lattice fan is the softer of the two
        assert(ratios.zipWithNext().all { (a, b) -> b < a })
        assert(ratios.all { it > 1.0 })
        assert(fanOverContinuum(1).isCloseTo(6.0))
        assert(fanOverContinuum(2).isCloseTo(20.0 / 9.0))
        assert(fanOverContinuum(4).isCloseTo(72.0 / 49.0))
        // the asymptote is 1 + 3/(2m), which at m = 64 is 2.3 % and at m = 256 is 0.59 %
        assert((fanOverContinuum(64) - 1.0).isCloseTo(3.0 / 128.0, relativeTolerance = 2e-2))
        assert((fanOverContinuum(256) - 1.0).isCloseTo(3.0 / 512.0, relativeTolerance = 5e-3))
    }

    @Test
    fun `gate 4 convergence - the base-pair sweep is complete, so refining the phase grid adds nothing`() {
        val coarse = (0 until 32).map {
            crossoversInLine(tile, it * Gen1Tile.RISE_PER_BASE_PAIR, pitch)
        }.toSet()
        val fine = (0 until 3200).map {
            crossoversInLine(tile, it * Gen1Tile.RISE_PER_BASE_PAIR / 100.0, pitch)
        }.toSet()
        assert(fine == coarse)
    }

    @Test
    fun `gate 4 convergence - the re-priced arm reproduces its own target secant at every hinge count`() {
        (1..16).forEach { n ->
            val arm = anchoredArmForStiffness(
                hingeStiffness = hinge,
                hingeCount = n,
                farStiffness = farStiffness,
                count = paths,
                targetStiffness = mandate,
                workingDisplacement = 3.0
            )
            val factor = guidedArmFactor(armRestraintParameter(farStiffness, arm, ei))
            val assembled = paths * RotatingHingeArm(hinge, arm, ei, n, factor).secantStiffness(3.0)
            assert((assembled / mandate - 1.0).isCloseTo(0.0, relativeTolerance = 1e-7))
        }
    }

    // ------------------------------------------------- gate 5 — literature and upstream

    @Test
    fun `gate 5 upstream - the census reproduces C-0015's crossover inventory exactly`() {
        val census = hingeLineCensus(tile)
        val inventories = census.map { tileCrossoverInventory(15, it.evenInterfaces, it.oddInterfaces) }
        assert(inventories.count { it == 56 } == 10)
        assert(inventories.count { it == 49 } == 22)
        assert(census.count { it.columns == 8 } == 10)
        assert(census.count { it.columns == 7 } == 22)
    }

    @Test
    fun `gate 5 upstream - the census agrees with C-0015's own CrossoverLayout at every phase`() {
        val sheet = origamiSheet(
            Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
        )
        hingeLineCensus(tile).forEach { record ->
            val layout = CrossoverLayout.atBasePairPhase(record.phaseBasePairs, sheet, tile)
            assert(record.columns == layout.size)
            assert(record.evenInterfaces == layout.countOfParity(0))
            assert(record.oddInterfaces == layout.countOfParity(1))
        }
    }

    @Test
    fun `gate 5 upstream - the continuum strip is C-0009's own across-helix rigidity`() {
        val sheet = origamiSheet(
            Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
        )
        // D_perp per unit width = k_theta d/p, and a strip of n_i crossovers is n_i pitches wide
        val perUnitWidth = hinge * Gen1Tile.INTERHELICAL_SHEET / pitch
        assert(sheet.acrossHelixRigidity.isCloseTo(perUnitWidth))
        assert(continuumStripRigidity(4, hinge, Gen1Tile.INTERHELICAL_SHEET).isCloseTo(
            4.0 * hinge * Gen1Tile.INTERHELICAL_SHEET
        ))
    }

    @Test
    fun `gate 5 upstream - C-0029's guided arms reproduce at 8, 16 and 32 crossovers`() {
        fun guided(n: Int): Double = rotatingArmForStiffness(
            hingeStiffness = hinge,
            armBendingRigidity = ei,
            count = paths,
            targetStiffness = mandate,
            workingDisplacement = 3.0,
            hingeCount = n,
            armFactor = 12.0
        )
        assert(guided(8).isCloseTo(10.3056070, relativeTolerance = 1e-6))
        assert(guided(16).isCloseTo(12.2423721, relativeTolerance = 1e-6))
        assert(guided(32).isCloseTo(13.6483006, relativeTolerance = 1e-6))
    }

    @Test
    fun `gate 5 upstream - C-0034's E5a16 arm and tangents reproduce, and its 8-crossover failure too`() {
        val sixteen = anchoredArmForStiffness(
            hinge, 16, farStiffness, ei, paths, mandate, 3.0
        )
        assert(sixteen.isCloseTo(11.028, relativeTolerance = 1e-4))
        val eight = anchoredArmForStiffness(
            hinge, 8, farStiffness, ei, paths, mandate, 3.0
        )
        assert(eight.isCloseTo(9.52, relativeTolerance = 1e-3))
        // C-0034's own tangents at the adopted design, 33.56 and 36.78 pN/nm
        val factor = guidedArmFactor(armRestraintParameter(farStiffness, sixteen, ei))
        val element = RotatingHingeArm(hinge, sixteen, ei, 16, factor)
        assert((paths * element.tangentStiffness(3.0)).isCloseTo(33.56, relativeTolerance = 3e-4))
        assert((paths * element.tangentStiffness(10.0)).isCloseTo(36.78, relativeTolerance = 3e-4))
    }

    @Test
    fun `gate 5 literature - 32 bp is three turns of the square lattice and 16 bp is one and a half`() {
        assert((32.0 / 10.67).isCloseTo(3.0, relativeTolerance = 1e-3))
        assert((16.0 / 10.67).isCloseTo(1.5, relativeTolerance = 1e-3))
    }

    // ---------------------------------------------------------------- the verdict itself

    @Test
    fun `P2 the predicate under test - sixteen crossovers are not reachable in one hinge line at any phase`() {
        val census = hingeLineCensus(tile)
        assert(census.all { it.largest == 4 })
        assert(census.count { it.smallest == 3 } == 22)
        assert(census.count { it.smallest == 4 } == 10)
        assert(census.none { it.largest >= 16 })
        // and the length it would take, against the tile it has to fit on
        assert(hingeLineLengthForCount(16, pitch).isCloseTo(163.2))
        assert((hingeLineLengthForCount(16, pitch) / tile).isCloseTo(4.08, relativeTolerance = 1e-9))
    }

    @Test
    fun `P2 the predicate under test - not even the per-helix mis-reading of the pitch reaches sixteen`() {
        // the exact error CLAUDE.md warns about: 16 bp per interface instead of 32
        val optimistic = maximumHingeCount(tile, pitch / 2.0)
        assert(optimistic == 8)
        assert(optimistic < 16)
    }

    @Test
    fun `P4 the design at the count that exists - the desired stroke needs ten crossovers and the ceiling needs three`() {
        fun arm(n: Int): Double =
            anchoredArmForStiffness(hinge, n, farStiffness, ei, paths, mandate, 3.0)
        fun tangent(n: Int): Double {
            val r = arm(n)
            val factor = guidedArmFactor(armRestraintParameter(farStiffness, r, ei))
            return paths * RotatingHingeArm(hinge, r, ei, n, factor).tangentStiffness(3.0)
        }
        // the arm is strictly increasing in the hinge count, so both thresholds are well posed
        assert((1..16).map { arm(it) }.zipWithNext().all { (a, b) -> b > a })
        assert(arm(9) < Gen1Tile.DESIRED_STROKE)
        assert(arm(10) > Gen1Tile.DESIRED_STROKE)
        assert(tangent(2) > 40.0)
        assert(tangent(3) < 40.0)
        // and four — the most any hinge line on this tile carries — reaches neither
        assert(arm(4) < Gen1Tile.DESIRED_STROKE)
        assert(arm(4) > Gen1Tile.ACCEPTABLE_STROKE)
    }

    @Test
    fun `P5 the inventory conflict - 45 paths at sixteen crossovers demand more than twelve tiles`() {
        val census = hingeLineCensus(tile)
        val richest = census.maxOf { tileCrossoverInventory(15, it.evenInterfaces, it.oddInterfaces) }
        assert(richest == 56)
        assert(paths * 16 == 720)
        assert((720.0 / richest).isCloseTo(12.857142857, relativeTolerance = 1e-9))
        // even ONE flexure at 16 would take 29 % of every crossover the tile has
        assert((16.0 / richest).isCloseTo(0.2857142857, relativeTolerance = 1e-9))
    }

    @Test
    fun `P6 the continuum control - the phase moves the count by a quarter where the answer is out fourfold`() {
        val census = hingeLineCensus(tile)
        // the continuum reading is the crossover LINE DENSITY times the length, 1/p per nm,
        // with no endpoint: it is what C-0009's sheet carries and it is not an integer
        val continuum = tile / pitch
        val best = census.maxOf { it.largest }.toDouble()
        val worst = census.minOf { it.smallest }.toDouble()
        // the lattice brackets the continuum reading, and narrowly
        assert(worst < continuum && continuum < best)
        assert((best / worst).isCloseTo(4.0 / 3.0))
        // 3.676 crossovers of continuum, so the quantisation is worth -18 % to +9 %
        assert(continuum.isCloseTo(40.0 / 10.88))
        assert((worst / continuum).isCloseTo(0.816, relativeTolerance = 1e-3))
        assert((best / continuum).isCloseTo(1.088, relativeTolerance = 1e-3))
        // while sixteen is out by more than four against a continuum line of the same length
        assert(16.0 / continuum > 4.0)
    }
}
