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
import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.LoadState
import com.xemantic.nano.plentyofroom.coupling.couplingSupports
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.PointSupport
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-136`, leaf `A8.2` — is there a **flat 30-root placement**, and does it keep the plan margin
 * `C-0072` bought by dropping four arms?
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 *
 * The strong falsifiers this task declared are here as tests:
 * **a uniform load on a uniform Winkler foundation must dish exactly zero**;
 * **the sliced multi-state bank must equal an assembled `OrigamiGrillage` solve** at the same 30
 * stations; **the closed-form maximum plan ceiling must equal an exhaustive maximum** over a
 * lattice small enough to enumerate; and **`C-0069`'s 8.19 nm and `C-0072`'s 9.12 nm must both
 * reproduce**, since the whole claim is a comparison against them.
 */
class TwoPerRowPlacementTest {

    private val sheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )

    private val duplexes = 15

    private val edgeX = Gen1Tile.EDGE_X

    private val lengthY = duplexes * Gen1Tile.INTERHELICAL_SHEET

    private val interiorPressure = Gen1Tile.TARGET_FORCE / (edgeX * lengthY)

    private val mandate = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

    private val width = OrigamiDuplex.INTERHELICAL

    /** The phase-24 upward lattice — `C-0063`'s own host, and this task's. */
    private val lattice24 = upwardRootLattice(24, edgeX, duplexes)

    /** The phase-8 one, the other centro-symmetric phase (`C-0063`'s congruence). */
    private val lattice8 = upwardRootLattice(8, edgeX, duplexes)

    /**
     * `C-0072`'s own 30-root reduction — `C-0063`'s placement with the interior root of every row
     * of three removed, which is the placement its 9.12 nm ceiling and 1.3495 nm margin are read
     * on. Reproduced through `rowsWithoutInteriorRoots` rather than transcribed.
     */
    private val c0063Roots: List<Pair<Int, List<Double>>> = listOf(
        0 to listOf(-16.32, -5.44, 16.32),
        1 to listOf(0.0, 10.88),
        2 to listOf(-16.32, 5.44, 16.32),
        3 to listOf(0.0, 10.88),
        4 to listOf(-16.32, 16.32),
        5 to listOf(-10.88, 0.0),
        6 to listOf(-16.32, 16.32),
        7 to listOf(-10.88, 10.88),
        8 to listOf(-16.32, 16.32),
        9 to listOf(0.0, 10.88),
        10 to listOf(-16.32, 16.32),
        11 to listOf(-10.88, 0.0),
        12 to listOf(-16.32, -5.44, 16.32),
        13 to listOf(-10.88, 0.0),
        14 to listOf(-16.32, 5.44, 16.32)
    )

    private fun c0063Rows(): List<StationRow> = c0063Roots.map { (row, roots) ->
        StationRow(row, (row - (duplexes - 1) / 2.0) * Gen1Tile.INTERHELICAL_SHEET, roots.sorted())
    }

    /** A three-row toy lattice, small enough that the maximum over placements is enumerable. */
    private val toy = listOf(
        listOf(-6.0, 0.0, 6.0),
        listOf(-4.0, 4.0),
        listOf(-8.0, -2.0, 3.0, 9.0)
    )

    private val toyEdge = 24.0

    // ------------------------------------------------------------------ gate 1 — dimensional

    @Test
    fun `gate 1 — the forced per-row count is an identity at 30 on 15 rows and refuses nonsense`() {
        // C-0063's bound 1 in a new place: 2 x 15 = 30, so the count vector carries no freedom.
        assert(forcedUniformRootsPerRow(30, duplexes, 2) == 2)
        assert(forcedUniformRootsPerRow(15, duplexes, 1) == 1)
        assert(forcedUniformRootsPerRow(45, duplexes, 3) == 3)
        // 34 on 15 rows at three per row does NOT force a uniform vector — it forces four threes.
        assert(forcedUniformRootsPerRow(34, duplexes, 3) == null)
        assert(forcedUniformRootsPerRow(29, duplexes, 2) == null)
        assertFailsWith<IllegalArgumentException> { forcedUniformRootsPerRow(31, duplexes, 2) }
        assertFailsWith<IllegalArgumentException> { forcedUniformRootsPerRow(0, duplexes, 2) }
        assertFailsWith<IllegalArgumentException> { forcedUniformRootsPerRow(30, 1, 2) }
        assertFailsWith<IllegalArgumentException> { forcedUniformRootsPerRow(30, duplexes, 0) }
        // and it agrees with C-0063's own arithmetic, read at a cap of two
        assert(rowsCarryingThreeArms(30, duplexes, 2) == duplexes)
    }

    @Test
    fun `gate 1 — a plan ceiling is a LENGTH and scales with the tile`() {
        val plain = maximumPlanCeilingForCount(toy, 3, toyEdge, width, 1)
        val scaled = maximumPlanCeilingForCount(
            toy.map { row -> row.map { it * 10.0 } }, 3, toyEdge * 10.0, width * 10.0, 1
        )
        assert(plain != null && scaled != null)
        assert(scaled!!.isCloseTo(10.0 * plain!!, 1e-7))
        assertFailsWith<IllegalArgumentException> {
            maximumPlanCeilingForCount(emptyList(), 1, toyEdge, width, 1)
        }
        assertFailsWith<IllegalArgumentException> {
            maximumPlanCeilingForCount(toy, 0, toyEdge, width, 1)
        }
        assertFailsWith<IllegalArgumentException> {
            maximumPlanCeilingForCount(toy, 3, -1.0, width, 1)
        }
        assertFailsWith<IllegalArgumentException> {
            maximumPlanCeilingForCount(toy, 3, toyEdge, width, 0)
        }
        // more roots than the lattice can carry at ANY length is a null, not an exception
        assert(maximumPlanCeilingForCount(toy, 40, toyEdge, width, 3) == null)
    }

    @Test
    fun `gate 1 — the lattice capacity falls with the element length and is capped per row`() {
        assert(latticeRootCapacity(toy, 0.01, toyEdge, width, 3) == 8)
        assert(latticeRootCapacity(toy, 0.01, toyEdge, width, 2) == 6)
        assert(latticeRootCapacity(toy, 0.01, toyEdge, width, 1) == 3)
        val short = latticeRootCapacity(toy, 1.0, toyEdge, width, 3)
        val long = latticeRootCapacity(toy, 6.0, toyEdge, width, 3)
        assert(long <= short)
        assertFailsWith<IllegalArgumentException> {
            latticeRootCapacity(toy, -1.0, toyEdge, width, 3)
        }
    }

    // ------------------------------------------------------------------ gate 2 — limiting cases

    @Test
    fun `gate 2 — one root per row has the tile-edge ceiling and a row of three has exactly pitch minus d`() {
        // With one root per row, the arm may run the whole way to the far edge: 20 + |x|, minimised
        // over the rows, which on the phase-24 lattice is the 10.88 nm outermost site of a 3-site
        // row: 30.88 nm. `C-0072` reports 20.00 for 15 paths because it reads the ceiling on ITS
        // OWN reduced rows and not on the best placement.
        val one = maximumPlanCeilingForCount(lattice24, 15, edgeX, width, 1)!!
        assert(one.isCloseTo(30.88, 1e-6))
        // A row of three is the binding case and it is the closed form C-0069 derived.
        val three = maximumPlanCeilingForCount(lattice24, 45, edgeX, width, 3)!!
        assert(three.isCloseTo(rowOfThreeLengthCeiling(10.88, width), 1e-6))
        assert(three.isCloseTo(8.19, 1e-6))
        // and 31 already forces one, so the ceiling steps down there and not at 34
        assert(maximumPlanCeilingForCount(lattice24, 31, edgeX, width, 3)!!.isCloseTo(8.19, 1e-6))
    }

    @Test
    fun `gate 2 — at 30 roots the two-per-row family IS the whole family`() {
        // Capping at two and capping at three give the same maximum ceiling at 30, because a row of
        // three costs more ceiling than it buys count: the cap is not a restriction at this count.
        val cappedAtTwo = maximumPlanCeilingForCount(lattice24, 30, edgeX, width, 2)!!
        val cappedAtThree = maximumPlanCeilingForCount(lattice24, 30, edgeX, width, 3)!!
        assert(cappedAtTwo.isCloseTo(cappedAtThree, 1e-9))
        assert(cappedAtTwo.isCloseTo(9.535, 1e-6))
        // The same at phase 8, where the row site counts are the other parity.
        assert(maximumPlanCeilingForCount(lattice8, 30, edgeX, width, 2)!!.isCloseTo(9.535, 1e-6))
    }

    @Test
    fun `gate 2 — an equal-spring coupling of vanishing total stiffness reproduces the free tile`() {
        val stations = c0063Rows().flatMap { row -> row.roots.map { it to row.y } }
        val free = grillage().solve(designLoad()).peakDishing()
        val limp = grillage(couplingSupports(stations, 1e-9)).solve(designLoad()).peakDishing()
        assert(abs(limp - free) / free < 1e-6)
    }

    // ------------------------------------------------------- gate 3 — symmetry and conservation

    @Test
    fun `gate 3 — a uniform load on a free tile dishes exactly zero`() {
        val dishing = grillage().solve(uniformPressure(interiorPressure)).peakDishing()
        assert(dishing < 1e-9)
    }

    @Test
    fun `gate 3 — the closed-form maximum ceiling equals an EXHAUSTIVE maximum over placements`() {
        // On the toy lattice every two-per-row placement can be enumerated, so the bound is not
        // merely plausible: it is checked against the object it bounds.
        for (perRow in 1..2) {
            val closed = maximumPlanCeilingForCount(
                toy, perRow * toy.size, toyEdge, width, perRow
            )!!
            val exhaustive = toy.map { row ->
                subsetsOf(row, perRow).maxOf {
                    rootedLengthCeiling(listOf(StationRow(0, 0.0, it)), toyEdge, width)
                }
            }.min()
            assert(closed.isCloseTo(exhaustive, 1e-6))
        }
    }

    @Test
    fun `gate 3 — forbidding the CONVERGING pose returns exactly C-0072's own 9_12 nm`() {
        // `CH-0086` claims the whole 30-root disagreement is the value of `C-0053`'s converging
        // pose — two arms in one row pointing at each other. This is that claim, measured: with
        // the pose forbidden the maximum over all two-per-row placements is 9.12 nm at BOTH
        // centro-symmetric phases, which is the number `C-0072` reports.
        listOf(lattice8, lattice24).forEach { lattice ->
            val ceiling = lattice.minOf { row ->
                subsetsOf(row, 2).maxOf { divergingOnlyRowCeiling(it[0], it[1]) }
            }
            assert(ceiling.isCloseTo(9.12, 1e-9))
        }
        // and with the pose admitted the same construction gives C-0074's 9.535
        assert(
            maximumPlanCeilingForCount(lattice24, 30, edgeX, width, 2)!!.isCloseTo(9.535, 1e-6)
        )
    }

    @Test
    fun `gate 3 — no concrete placement beats the closed-form ceiling, and C-0072's does not reach it`() {
        val reduced = rowsWithoutInteriorRoots(c0063Rows(), 4)
        assert(reduced.sumOf { it.roots.size } == 30)
        val concrete = rootedLengthCeiling(reduced, edgeX, width)
        val bound = maximumPlanCeilingForCount(lattice24, 30, edgeX, width, 2)!!
        assert(concrete <= bound + 1e-9)
        // C-0072's own reduction reaches 9.12 where the lattice affords 9.535 — the ceiling is a
        // property of the PLACEMENT and not of the count, which is CH-0086.
        assert(concrete.isCloseTo(9.12, 1e-6))
        assert(bound > concrete + 0.4)
    }

    @Test
    fun `gate 3 — the sliced bank equals an assembled grillage solve at the same 30 stations`() {
        val reduced = rowsWithoutInteriorRoots(c0063Rows(), 4)
        val stations = reduced.flatMap { row -> row.roots.map { it to row.y } }
        val bank = MultiStateRootBank(
            grillage(), stations, listOf(LoadState("design", designLoad())), samples = 41
        )
        val stiffnesses = List(stations.size) { mandate / stations.size }
        val sliced = bank.surrogateFor(stations.indices.toList())
        val assembled = grillage(couplingSupports(stations, mandate)).solve(designLoad())
        assert(
            sliced.worstDishing(stiffnesses, listOf(0))
                .isCloseTo(assembled.peakDishing(41), 1e-7)
        )
        val slicedForces = sliced.supportForces(stiffnesses, 0)
        assembled.supportForces.forEachIndexed { index, force ->
            assert(slicedForces[index].isCloseTo(force, 1e-6))
        }
    }

    @Test
    fun `gate 3 — a placement's own rows reflect through the centre and keep their ceiling`() {
        val rows = c0063Rows()
        val mirrored = rows.reversed().mapIndexed { index, row ->
            StationRow(index, -row.y, row.roots.map { -it }.sorted())
        }
        assert(
            rootedLengthCeiling(mirrored, edgeX, width)
                .isCloseTo(rootedLengthCeiling(rows, edgeX, width), 1e-9)
        )
    }

    // ------------------------------------------------------- gate 4 — numerical convergence

    @Test
    fun `gate 4 — the ceiling bisection is resolution independent`() {
        val readings = listOf(1e-6, 1e-9, 1e-12).map {
            maximumPlanCeilingForCount(lattice24, 30, edgeX, width, 2, it)!!
        }
        assert(abs(readings[2] - readings[1]) < 1e-6)
        assert(abs(readings[1] - readings[0]) < 1e-5)
    }

    @Test
    fun `gate 4 — a placement's station rows round-trip and its ceiling is deterministic`() {
        val placement = UpwardArmPlacement(
            24,
            (0 until duplexes).map { row ->
                val roots = lattice24[row].take(2)
                UpwardArmRow(row, roots, armDirections(roots, 7.7, edgeX, width)!!)
            }
        )
        val rows = stationRowsOf(placement, duplexes, Gen1Tile.INTERHELICAL_SHEET)
        assert(rows.size == duplexes)
        assert(rows.sumOf { it.roots.size } == 30)
        assert(rows.map { it.y } == placement.stations(duplexes).map { it.second }.distinct())
        val first = placementLengthCeiling(placement, duplexes, edgeX, width)
        val again = placementLengthCeiling(placement, duplexes, edgeX, width)
        assert(first == again)
    }

    // ------------------------------------------------------- gate 5 — literature and upstream

    @Test
    fun `gate 5 — C-0055's pitch, C-0069's 8_19 nm and C-0072's 30-root reduction all reproduce`() {
        assert(
            (UPWARD_ROOT_PITCH_BASE_PAIRS * Gen1Tile.RISE_PER_BASE_PAIR).isCloseTo(10.88, 1e-12)
        )
        assert(rowOfThreeLengthCeiling(10.88, width).isCloseTo(8.19, 1e-12))
        assert(rootedLengthCeiling(c0063Rows(), edgeX, width).isCloseTo(8.19, 1e-6))
        val reduced = rowsWithoutInteriorRoots(c0063Rows(), 4)
        assert(reduced.count { it.roots.size == 2 } == duplexes)
        // C-0072's Deliverable 6: ceiling 9.12 nm, margin 1.3495 nm against its 7.77049 nm arm.
        val ceiling = rootedLengthCeiling(reduced, edgeX, width)
        assert(ceiling.isCloseTo(9.12, 1e-6))
        assert((ceiling - 7.77049).isCloseTo(1.3495, 1e-3))
    }

    @Test
    fun `gate 5 — every root of the phase-24 lattice is on C-0055's own 10_88 nm pitch`() {
        lattice24.forEach { row ->
            row.zipWithNext().forEach { (a, b) ->
                assert(abs((b - a) - 10.88) < 1e-9)
            }
        }
    }

    // ------------------------------------------------------------------ fixtures

    private fun grillage(supports: List<PointSupport> = emptyList()) = OrigamiGrillage(
        sheet = sheet,
        lengthX = edgeX,
        beamCount = duplexes,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = CrossoverLayout.atBasePairPhase(24, sheet, edgeX),
        subdivisions = 1,
        supports = supports
    )

    /** `C-0022`'s solved 2 mM / 10 nm / 0.192 V collar, transcribed from `T-3b`'s result file. */
    private fun designLoad() = edgeCollarPressure(
        interiorPressure, edgeX, lengthY,
        listOf(CollarTerm(-0.302887367, 8.93928311), CollarTerm(-0.593889278, 1.0))
    )

    /**
     * The longest rooted element a pair of roots at [low] and [high] admits when the two arms may
     * **not** converge — written out here rather than added to `armDirections`, which is a shared
     * main source another agent is working in.
     *
     * Three assignments remain of `C-0053`'s four: `(+, +)`, `(−, −)` and `(−, +)`. Each is a pair
     * of linear inequalities in `L`, so the maximum is a closed form and no bisection is needed.
     */
    private fun divergingOnlyRowCeiling(low: Double, high: Double): Double {
        val half = edgeX / 2.0
        val bothPositive = minOf(high - low - width, half - high)
        val bothNegative = minOf(high - low - width, low + half)
        val diverging = if (low + width <= high) minOf(low + half, half - high) else -1.0
        return maxOf(0.0, maxOf(bothPositive, maxOf(bothNegative, diverging)))
    }

    private fun subsetsOf(items: List<Double>, size: Int): List<List<Double>> {
        val out = ArrayList<List<Double>>()
        fun build(start: Int, taken: List<Double>) {
            if (taken.size == size) {
                out += taken
                return
            }
            for (index in start until items.size) build(index + 1, taken + items[index])
        }
        build(0, emptyList())
        return out
    }

}
