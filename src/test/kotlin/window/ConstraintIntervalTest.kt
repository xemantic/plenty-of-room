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

package com.xemantic.nano.plentyofroom.window

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * The interval algebra `T-2` intersects its constraints with, and the two things it has
 * to refuse to do: report an interval where the admissible set has a hole, and name a
 * single binding constraint where two coincide.
 *
 * Every test is named for the verification gate it discharges.
 */
class ConstraintIntervalTest {

    private val grid = listOf(1.0, 2.0, 4.0, 8.0, 16.0, 32.0)

    @Test
    fun `gate 1 - an interval on a grid carries the grid's own units and its width is a ratio`() {
        val interval = admissibleInterval(listOf(false, true, true, true, false, false))!!
        assert(interval.lowestIndex == 1)
        assert(interval.highestIndex == 3)
        assert(interval.count == 3)
        // the grid is a grafting density in nm^-2, so the edges are too and the width is
        // dimensionless — a design window is quoted as a ratio, never as a difference
        assert(interval.lowest(grid).isCloseTo(2.0))
        assert(interval.highest(grid).isCloseTo(8.0))
        assert(interval.width(grid).isCloseTo(4.0))
    }

    @Test
    fun `gate 2 - an always-true constraint gives the whole grid and an always-false one gives nothing`() {
        assert(admissibleInterval(List(6) { true })!! == GridInterval(0, 5))
        assertNull(admissibleInterval(List(6) { false }))
    }

    @Test
    fun `gate 2 - a single admissible point is an interval of width one`() {
        val interval = admissibleInterval(listOf(false, false, true, false, false, false))!!
        assert(interval.count == 1)
        assert(interval.width(grid).isCloseTo(1.0))
    }

    @Test
    fun `gate 2 - a hole in the admissible set throws rather than being reported as an interval`() {
        // declared falsifier 2 of T-2's Plan: the method reports an interval, so a
        // non-contiguous admissible set is not a narrower answer, it is the wrong object
        val failure = assertFailsWith<IllegalStateException> {
            admissibleInterval(listOf(true, true, false, true, false, false))
        }
        assert(failure.message!!.contains("contiguous"))
    }

    @Test
    fun `gate 3 - the intersection is commutative and associative`() {
        val a = GridInterval(0, 4)
        val b = GridInterval(2, 5)
        val c = GridInterval(1, 3)
        assert(intersect(a, b) == intersect(b, a))
        assert(intersect(intersect(a, b), c) == intersect(a, intersect(b, c)))
        // an order-independent answer is what licenses reporting one window rather than
        // one window per order in which the constraints happened to be applied
        assert(intersect(a, b) == GridInterval(2, 4))
    }

    @Test
    fun `gate 3 - intersecting a constraint with itself changes nothing, and with nothing gives nothing`() {
        val a = GridInterval(1, 4)
        assert(intersect(a, a) == a)
        assertNull(intersect(a, null))
        assertNull(intersect(null, a))
    }

    @Test
    fun `gate 2 - disjoint constraints intersect to nothing`() {
        assertNull(intersect(GridInterval(0, 1), GridInterval(3, 5)))
    }

    @Test
    fun `gate 3 - the binding constraint at an edge is the one whose own edge the window sits on`() {
        val intervals = mapOf(
            "overlap" to GridInterval(2, 5),
            "stroke" to GridInterval(0, 3),
            "drainage" to GridInterval(0, 5)
        )
        val attribution = attributeEdges(intervals)!!
        assert(attribution.window == GridInterval(2, 3))
        assert(attribution.lowerBinding == listOf("overlap"))
        assert(attribution.upperBinding == listOf("stroke"))
        assert(!attribution.lowerTie)
        assert(!attribution.upperTie)
    }

    @Test
    fun `gate 3 - two constraints coinciding at an edge are reported as a tie, not as a name`() {
        // declared falsifier 1 of T-2's Plan
        val intervals = mapOf(
            "overlap" to GridInterval(2, 5),
            "regime" to GridInterval(1, 4),
            "stroke" to GridInterval(0, 4)
        )
        val attribution = attributeEdges(intervals)!!
        assert(attribution.lowerBinding == listOf("overlap"))
        assert(!attribution.lowerTie)
        assert(attribution.upperBinding == listOf("regime", "stroke"))
        assert(attribution.upperTie)
    }

    @Test
    fun `gate 2 - an empty intersection names the two crossing constraints and their crossing ratio`() {
        val intervals = mapOf(
            "overlap" to GridInterval(4, 5),
            "stroke" to GridInterval(0, 2),
            "drainage" to GridInterval(0, 5)
        )
        assertNull(attributeEdges(intervals))
        val crossing = crossingOf(intervals, grid)!!
        assert(crossing.lowerBoundConstraint == "overlap")
        assert(crossing.upperBoundConstraint == "stroke")
        // grid[4] = 16 against grid[2] = 4
        assert(crossing.crossingRatio.isCloseTo(4.0))
    }

    @Test
    fun `gate 2 - a non-empty intersection has no crossing`() {
        assertNull(
            crossingOf(
                mapOf("a" to GridInterval(0, 3), "b" to GridInterval(2, 5)),
                grid
            )
        )
    }

    @Test
    fun `gate 4 - a window edge is a grid point, so its resolution is one grid ratio`() {
        // the sigma grid T-1d ran is logarithmic with a fixed ratio; an edge located on it
        // is known to that ratio and no better, which is what the convergence claim is
        val ratios = grid.zipWithNext { low, high -> high / low }
        ratios.forEach { assert(it.isCloseTo(2.0)) }
        val interval = admissibleInterval(listOf(false, true, true, true, false, false))!!
        assert(interval.edgeResolution(grid).isCloseTo(2.0))
    }

    @Test
    fun `gate 4 - halving the grid moves an edge by at most one coarse step`() {
        // the convergence test the sweep itself cannot run twice: drop every other grid
        // point and check the located edge does not move further than the coarse spacing
        val fine = List(6) { it >= 2 }
        val fineInterval = admissibleInterval(fine)!!
        val coarse = fine.filterIndexed { index, _ -> index % 2 == 0 }
        val coarseGrid = grid.filterIndexed { index, _ -> index % 2 == 0 }
        val coarseInterval = admissibleInterval(coarse)!!
        val shift = coarseInterval.lowest(coarseGrid) / fineInterval.lowest(grid)
        assert(shift >= 1.0 / 4.0 && shift <= 4.0)
    }
}
