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

import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.honeycombXRasterPath
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.kotlin.test.assert
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-254` — does a raster TURN sit on the flatness axis at all?
 *
 * Written before `tile/HoneycombRasterTurnTies.kt` and before `HoneycombGrillage`'s scaffold-tie
 * extension, and watched fail.
 */
class HoneycombRasterTurnTiesTest {

    private fun block(rows: Int, perRow: Int) = HoneycombBlock(rows, perRow)

    private fun lattice(
        rows: Int = 10,
        perRow: Int = 6,
        rowBasePairs: Int = 116,
        ties: List<HoneycombScaffoldTurnTie> = emptyList()
    ) = HoneycombGrillage(
        block = block(rows, perRow),
        rowBasePairs = rowBasePairs,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        scaffoldTurnTies = ties
    )

    // ------------------------------------------------------ gate 5 and 3: the census is derived

    @Test
    fun `gate 5 - the raster order reproduces honeycombXRasterPath`() {
        listOf(10 to 6, 15 to 4).forEach { (rows, perRow) ->
            val order = honeycombRasterOrder(block(rows, perRow))
            val path = honeycombXRasterPath(rows, perRow)
            assert(order.size == path.size)
            order.indices.forEach { k ->
                // the column MIRROR is not optional: HoneycombBlock and HoneycombCell use
                // opposite vertical-bond parities, so c = n - 1 - x (CLAUDE.md).
                assert(order[k].column == perRow - 1 - path[k].x)
                assert(order[k].rasterRow == (-path[k].y) / 3)
            }
        }
    }

    @Test
    fun `gate 3 - every consecutive pair of the raster order is a honeycomb bond of the block`() {
        listOf(10 to 6, 15 to 4).forEach { (rows, perRow) ->
            val b = block(rows, perRow)
            val bonded = honeycombBondPairs(b).map { minOf(it.first, it.second) to maxOf(it.first, it.second) }.toSet()
            honeycombRasterTurnList(b).forEach { turn ->
                assert(bonded.contains(turn.lowerBeam to turn.upperBeam))
            }
        }
    }

    @Test
    fun `gate 2 - a 10 by 6 block has 59 turns, 50 through-thickness and 9 in plane`() {
        val turns = honeycombRasterTurnList(block(10, 6))
        assert(turns.size == 59)
        assert(turns.count { it.inPlane } == 9)
        assert(turns.count { !it.inPlane } == 50)
    }

    @Test
    fun `gate 2 - a 15 by 4 block has the same 59 turns split 14 and 45`() {
        val turns = honeycombRasterTurnList(block(15, 4))
        assert(turns.size == 59)
        assert(turns.count { it.inPlane } == 14)
        assert(turns.count { !it.inPlane } == 45)
    }

    @Test
    fun `gate 3 - the turns alternate axial ends and the first axial sign swaps them`() {
        val forward = honeycombRasterTurnList(block(10, 6), firstAxialSign = 1)
        val reverse = honeycombRasterTurnList(block(10, 6), firstAxialSign = -1)
        assert(forward.count { it.atHighEnd } == 30)
        assert(reverse.count { it.atHighEnd } == 29)
        forward.indices.forEach { assert(forward[it].atHighEnd != reverse[it].atHighEnd) }
    }

    // ------------------------------------------------------ gate 1: the cheap bound, a lever arm

    @Test
    fun `gate 1 - no honeycomb bond has a zero in-plane lever arm`() {
        val arms = lattice().bonds
            .map { Math.round(abs(it.unitY) * 1e6) / 1e6 }.distinct().sorted()
        assert(arms.size == 2)
        assert(abs(arms[0] - 0.5) < 1e-6)
        assert(abs(arms[1] - 1.0) < 1e-6)
    }

    @Test
    fun `gate 1 - and no raster turn tie has one either`() {
        val l = lattice()
        val ties = honeycombScaffoldTurnTies(l.block, l.nodesPerBeam)
        val armed = lattice(ties = ties)
        armed.turnElements.forEach { assert(abs(it.unitY) > 0.49) }
    }

    @Test
    fun `gate 1 - a turn tie joins two beams exactly one lattice constant apart`() {
        val l = lattice()
        val armed = lattice(ties = honeycombScaffoldTurnTies(l.block, l.nodesPerBeam))
        armed.turnElements.forEach {
            assert(abs(it.unitY * it.unitY + it.unitZ * it.unitZ - 1.0) < 1e-12)
        }
    }

    @Test
    fun `gate 1 - a tie refuses a non-adjacent beam pair and a node outside the beam`() {
        assertFailsWith<IllegalArgumentException> {
            lattice(ties = listOf(HoneycombScaffoldTurnTie(0, 40, 0))).turnElements
        }
        assertFailsWith<IllegalArgumentException> {
            lattice(ties = listOf(HoneycombScaffoldTurnTie(0, 1, 9999))).turnElements
        }
    }

    // ------------------------------------------------------ gate 2: the empty list is a no-op

    @Test
    fun `gate 2 - an empty tie list leaves the lattice bit-identical`() {
        val plain = lattice()
        val armed = lattice(ties = emptyList())
        assert(armed.turnElements.isEmpty())
        // CLAUDE.md: a load-vector identity is NOT a lattice identity, so the crossover SITE
        // SET is asserted beside it - two grillages with inverted parities have bit-identical
        // load vectors and different solved fields.
        assert(armed.bonds.map { it.site } == plain.bonds.map { it.site })
        // and bit-identity is assertable on assembleLoad, which is a fixed-order scatter-add,
        // never on a solved field: two identically constructed lattices differ by a few ulp.
        val pressure = uniformPressure(0.02)
        val loadPlain = plain.assembleLoad(pressure)
        val loadArmed = armed.assembleLoad(pressure)
        for (i in 0 until plain.degreesOfFreedom) {
            assert(loadPlain[i] == loadArmed[i])
        }
        var worst = 0.0
        for (i in 0 until plain.degreesOfFreedom step 29) {
            for (j in maxOf(0, i - plain.bandwidth)..i step 13) {
                worst = maxOf(worst, abs(plain.stiffnessEntry(i, j) - armed.stiffnessEntry(i, j)))
            }
        }
        assert(worst == 0.0)
        val a = plain.solve(pressure)
        val b = armed.solve(pressure)
        assert(abs(a.peakDishing(41) - b.peakDishing(41)) < 1e-10)
    }

    @Test
    fun `gate 3 - adding the ties leaves the crossover SITE SET and the load vector untouched`() {
        val plain = lattice()
        val armed = lattice(ties = honeycombScaffoldTurnTies(plain.block, plain.nodesPerBeam))
        assert(armed.bonds.map { it.site } == plain.bonds.map { it.site })
        val pressure = uniformPressure(0.02)
        val loadPlain = plain.assembleLoad(pressure)
        val loadArmed = armed.assembleLoad(pressure)
        for (i in 0 until plain.degreesOfFreedom) {
            assert(loadPlain[i] == loadArmed[i])
        }
        // the ties are a STIFFNESS at zero prestrain, so only the matrix may move
        var moved = 0
        for (i in 0 until plain.degreesOfFreedom step 29) {
            for (j in maxOf(0, i - plain.bandwidth)..i step 13) {
                if (plain.stiffnessEntry(i, j) != armed.stiffnessEntry(i, j)) moved++
            }
        }
        assert(moved > 0)
    }

    // ------------------------------------------------------ gate 3: falsifiers and symmetry

    @Test
    fun `gate 3 - a uniform pressure on the tied lattice still dishes exactly zero`() {
        val l = lattice()
        val armed = lattice(ties = honeycombScaffoldTurnTies(l.block, l.nodesPerBeam))
        assert(armed.solve(uniformPressure(0.02)).peakDishing(41) < 1e-9)
    }

    @Test
    fun `gate 3 - a tie prestrain changes no entry of the stiffness matrix`() {
        val l = lattice()
        val ties = honeycombScaffoldTurnTies(l.block, l.nodesPerBeam)
        val cold = lattice(ties = ties)
        val hot = lattice(ties = ties.map { it.copy(prestrainRadians = 0.3) })
        assert(cold.degreesOfFreedom == hot.degreesOfFreedom)
        var worst = 0.0
        for (i in 0 until cold.degreesOfFreedom step 37) {
            for (j in maxOf(0, i - cold.bandwidth)..i step 11) {
                worst = maxOf(worst, abs(cold.stiffnessEntry(i, j) - hot.stiffnessEntry(i, j)))
            }
        }
        assert(worst == 0.0)
    }

    @Test
    fun `gate 3 - the field is exactly linear in the tie prestrain`() {
        val l = lattice()
        val ties = honeycombScaffoldTurnTies(l.block, l.nodesPerBeam)
        val one = lattice(ties = ties.map { it.copy(prestrainRadians = 0.01) })
            .solve().peakDishing(41)
        val two = lattice(ties = ties.map { it.copy(prestrainRadians = 0.02) })
            .solve().peakDishing(41)
        assert(one > 0.0)
        assert(abs(two / one - 2.0) < 1e-9)
    }

    @Test
    fun `gate 2 - a unit tie response is the field of one radian at that tie alone`() {
        val l = lattice()
        val ties = honeycombScaffoldTurnTies(l.block, l.nodesPerBeam)
        val armed = lattice(ties = ties)
        val element = armed.turnElements[7]
        val direct = lattice(
            ties = ties.mapIndexed { i, t ->
                if (i == 7) t.copy(prestrainRadians = 1.0) else t
            }
        ).solve().peakDishing(41)
        val unit = armed.unitTurnResponse(element).peakDishing(41)
        assert(abs(unit - direct) / direct < 1e-9)
    }

    @Test
    fun `gate 3 - adding the ties cannot soften the block under a fixed load`() {
        val l = lattice()
        val armed = lattice(ties = honeycombScaffoldTurnTies(l.block, l.nodesPerBeam))
        val load = listOf(PointLoad(0.0, 0.0, 10.0))
        val plainField = abs(l.solve(pointLoads = load).deflection(0.0, 0.0))
        val armedField = abs(armed.solve(pointLoads = load).deflection(0.0, 0.0))
        assert(armedField < plainField * (1.0 + 1e-9))
    }

    // ------------------------------------------------------ gate 5: the departure a turn carries

    @Test
    fun `gate 5 - an allowed scaffold crossover carries a quarter of a base pair of azimuth`() {
        val perBasePair = 240.0 / 7.0
        assert(abs(allowedScaffoldCrossoverDepartureDegrees() - perBasePair / 4.0) < 1e-12)
        assert(abs(allowedScaffoldCrossoverDepartureDegrees() - 8.571428571428571) < 1e-9)
    }
}
