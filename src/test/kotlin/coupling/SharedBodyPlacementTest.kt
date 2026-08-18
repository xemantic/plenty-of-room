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

package com.xemantic.nano.plentyofroom.coupling

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.anchoring.centroSymmetricUpwardPhases
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-165` — a **distribution and placement** search on `C-0093`'s shared-body topology, on the
 * stations the **upward crossover lattice actually supplies**.
 *
 * Every test is named for the verification gate it discharges, and the falsifiers `T-165`
 * declares are asserted rather than argued:
 *
 * - **`F2`** — the **stiff-tie kinematic bound**. Scaling every tie by `s` drives the tile's
 *   stations onto a **plane**, a constraint that does not depend on the tie *distribution* at
 *   all, and the departure from that limit falls as `1/s`. If it did not, the cheap bound that
 *   prices the whole distribution axis would have to be withdrawn rather than quoted.
 * - **`F3`** — a uniform load on a uniform Winkler foundation dishes exactly zero, uncoupled and
 *   under a **free** shared body.
 * - **`F4`** — the pipeline reproduces the upstream figures on the identical stations.
 * - **`F5`** — a body grounded far above the mandate **is** the array.
 */
class SharedBodyPlacementTest {

    private val duplexes = 15

    private val sheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )

    private val edgeX = Gen1Tile.EDGE_X

    private val edgeY = duplexes * sheet.interhelicalDistance

    private val interiorPressure = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)

    private val mandate = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

    /** `C-0022`'s design point at 2 mM / 10 nm / 0.192 V, as `SharedBodyCouplingTest` has it. */
    private val solvedField = edgeCollarPressure(
        interiorPressure, edgeX, edgeY,
        listOf(CollarTerm(-0.302887367, 8.93928311), CollarTerm(-0.593889278, 1.0))
    )

    private fun host(phase: Int) = OrigamiGrillage(
        sheet = sheet,
        lengthX = edgeX,
        beamCount = duplexes,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = CrossoverLayout.atBasePairPhase(phase, sheet, edgeX),
        subdivisions = 2,
        supports = emptyList()
    )

    private val freeStroke = PlateOnFoundation(
        sheet.plate(edgeX, edgeY), Gen1Tile.FOUNDATION_SECANT, emptyList(), 12
    ).solve(uniformPressure(interiorPressure)).meanDeflection

    private val rigidBody = PlacedSharedBody(edgeX, edgeY, mandate)

    /** Four stations, deliberately not collinear. */
    private val four = listOf(-8.0 to -6.0, 9.0 to -5.0, -7.0 to 6.5, 10.0 to 7.0)

    // ------------------------------------------------------------------ gate 1: dimensional

    @Test
    fun `gate 1 - an upward tie lattice is a count and refuses an impossible sheet`() {
        assertFailsWith<IllegalArgumentException> { upwardTieLattice(24, 0.0, duplexes) }
        assertFailsWith<IllegalArgumentException> { upwardTieLattice(24, edgeX, 1) }
        assertFailsWith<IllegalArgumentException> { upwardTieLattice(-1, edgeX, duplexes) }
        val lattice = upwardTieLattice(24, edgeX, duplexes)
        assert(lattice.rows.size == duplexes)
        assert(lattice.siteCount == lattice.rows.sumOf { it.size })
        assert(lattice.rowLengths.sum() == lattice.siteCount)
        assert(lattice.stations(sheet.interhelicalDistance).size == lattice.siteCount)
        lattice.stations(sheet.interhelicalDistance).forEach { (x, y) ->
            assert(abs(x) <= edgeX / 2.0)
            assert(abs(y) <= edgeY / 2.0)
        }
        lattice.rows.forEach { row -> assert(row.sorted() == row) }
    }

    @Test
    fun `gate 1 - a redundancy fit is a slope in a log-log plane and refuses a degenerate sample`() {
        assertFailsWith<IllegalArgumentException> { redundancyFit(listOf(90 to 0.24), 0.10) }
        assertFailsWith<IllegalArgumentException> {
            redundancyFit(listOf(90 to 0.24, 180 to 0.12), 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            redundancyFit(listOf(90 to 0.24, 0 to 0.12), 0.10)
        }
        assertFailsWith<IllegalArgumentException> {
            redundancyFit(listOf(90 to 0.24, 180 to 0.0), 0.10)
        }
        assertFailsWith<IllegalArgumentException> {
            redundancyFit(listOf(90 to 0.24, 90 to 0.12), 0.10)
        }
    }

    @Test
    fun `gate 1 - a tie descent refuses bounds that admit nothing`() {
        val start = List(4) { 1.0 }
        assertFailsWith<IllegalArgumentException> {
            optimiseTieDistribution(emptyList(), 0.1, 10.0) { 1.0 }
        }
        assertFailsWith<IllegalArgumentException> {
            optimiseTieDistribution(start, 10.0, 1.0) { 1.0 }
        }
        assertFailsWith<IllegalArgumentException> {
            optimiseTieDistribution(start, 0.0, 10.0) { 1.0 }
        }
        assertFailsWith<IllegalArgumentException> {
            optimiseTieDistribution(start, 2.0, 10.0) { 1.0 }
        }
        assertFailsWith<IllegalArgumentException> {
            optimiseTieDistribution(start, 0.1, 10.0, sweeps = 0) { 1.0 }
        }
    }

    @Test
    fun `gate 1 - a subset descent refuses a choice its candidate set cannot make`() {
        assertFailsWith<IllegalArgumentException> {
            descendTieSubset(emptyList(), listOf(0, 1, 2)) { 1.0 }
        }
        assertFailsWith<IllegalArgumentException> {
            descendTieSubset(listOf(0, 0), listOf(0, 1, 2)) { 1.0 }
        }
        assertFailsWith<IllegalArgumentException> {
            descendTieSubset(listOf(0, 7), listOf(0, 1, 2)) { 1.0 }
        }
    }

    @Test
    fun `gate 1 - a run counted by row refuses a presence vector that does not split`() {
        assertFailsWith<IllegalArgumentException> {
            longestAbsenceRunByRow(List(5) { true }, listOf(2, 2))
        }
        assertFailsWith<IllegalArgumentException> {
            longestAbsenceRunByRow(emptyList(), emptyList())
        }
        assertFailsWith<IllegalArgumentException> {
            longestAbsenceRunByRow(List(4) { true }, listOf(2, 0, 2))
        }
    }

    // ------------------------------------------------------------------ gate 2: limiting cases

    @Test
    fun `gate 2 - the upward census reproduces C-0066's 53 sites at C-0063's phase 24`() {
        val census = upwardTieCensus(edgeX, duplexes)
        assert(census.size == 32)
        val phase24 = census.first { it.phaseBasePairs == 24 }
        assert(phase24.siteCount == 53)
        assert(phase24.rowLengths == listOf(4, 3, 4, 3, 4, 3, 4, 3, 4, 3, 4, 3, 4, 3, 4))
        assert(phase24.rowPitch.isCloseTo(32.0 * Gen1Tile.RISE_PER_BASE_PAIR))
    }

    @Test
    fun `gate 2 - a run counted by row is C-0089's run on a uniform split`() {
        val present = listOf(true, false, false, true, false, true, true, false)
        assert(longestAbsenceRunByRow(present, listOf(4, 4)) == longestAbsenceRun(present, 4))
        assert(longestAbsenceRunByRow(listOf(true, false, false, true), listOf(2, 2)) == 1)
        assert(longestAbsenceRunByRow(List(6) { true }, listOf(4, 2)) == 0)
        assert(longestAbsenceRunByRow(List(6) { false }, listOf(4, 2)) == 4)
    }

    @Test
    fun `gate 2 - a redundancy fit recovers an exact power law and inverts it`() {
        val slope = -0.75
        val points = listOf(15, 30, 45, 90, 180).map { it to 2.0 * it.toDouble().pow(slope) }
        val fit = redundancyFit(points, 0.10)
        assert(fit.slope.isCloseTo(slope, 1e-12))
        assert(fit.predictedAt(90.0).isCloseTo(2.0 * 90.0.pow(slope), 1e-12))
        assert(fit.predictedAt(fit.countAtTolerance).isCloseTo(0.10, 1e-12))
        assert(fit.factorDemandedAt(90.0).isCloseTo(fit.predictedAt(90.0) / 0.10, 1e-12))
    }

    @Test
    fun `gate 2 - a tie descent whose objective is flat returns its own start`() {
        val start = listOf(1.0, 2.0, 3.0)
        val found = optimiseTieDistribution(start, 0.5, 10.0) { 7.0 }
        assert(found.ties == start)
        assert(found.objective.isCloseTo(7.0))
        assert(found.lastImprovement.isCloseTo(0.0))
    }

    @Test
    fun `gate 2 - a tie descent finds a separable optimum inside its own bounds`() {
        val targets = listOf(0.7, 4.0, 40.0)
        val found = optimiseTieDistribution(List(3) { 1.0 }, 0.1, 10.0, sweeps = 6) { ties ->
            ties.indices.sumOf { ln(ties[it] / targets[it]).pow(2) }
        }
        assert(found.ties[0].isCloseTo(targets[0], 1e-3))
        assert(found.ties[1].isCloseTo(targets[1], 1e-3))
        assert(found.ties[2].isCloseTo(10.0, 1e-9))
    }

    @Test
    fun `gate 2 - a subset descent at full inventory has nothing to choose`() {
        val found = descendTieSubset(listOf(0, 1, 2), listOf(0, 1, 2)) { it.size.toDouble() }
        assert(found.indices == listOf(0, 1, 2))
        assert(found.lastImprovement.isCloseTo(0.0))
        // One swap is the only move class, so a target one swap away is reachable and one two
        // swaps away across a flat plateau is NOT — which is what makes this a descent and is
        // stated here rather than discovered later.
        val oneAway = descendTieSubset(listOf(0, 2), (0..3).toList()) { chosen ->
            if (chosen.toSet() == setOf(1, 2)) 0.0 else 1.0
        }
        assert(oneAway.indices.toSet() == setOf(1, 2))
        val twoAway = descendTieSubset(listOf(0, 2), (0..3).toList()) { chosen ->
            if (chosen.toSet() == setOf(1, 3)) 0.0 else 1.0
        }
        assert(twoAway.indices.toSet() == setOf(0, 2))
    }

    @Test
    fun `gate 2 - F5 - a body grounded far above the mandate is the array`() {
        val ties = List(four.size) { 25.0 }
        val body = rigidBody.groundedAt(four, 1e9 * mandate)
        val condensed = sharedBodyCouplingMatrix(ties, body)
        val array = Array(four.size) { j ->
            DoubleArray(four.size) { k -> if (j == k) ties[j] else 0.0 }
        }
        assert(matrixDeparture(condensed, array) < 1e-6)
    }

    // ------------------------------------------------------- gate 3: symmetry and conservation

    @Test
    fun `gate 3 - F2 - the kinematic limit is approached as one over the tie scale`() {
        // On the REAL phase-24 stations and a deliberately spread tie shape, so that the
        // statement is made at the size and the conditioning the study reads it at.
        val stations = upwardTieLattice(24, edgeX, duplexes).stations(sheet.interhelicalDistance)
        val shapes = rigidBody.modes.shapesAt(stations)
        val ground = rigidBody.modes.distributedGroundStiffness(mandate)
        val shape = stations.indices.map { 0.2 + 3.0 * ((it * 7) % 11) / 11.0 }
        val scales = listOf(1e3, 1e4, 1e5, 1e6)
        val departures = scales.map { kinematicLimitDeparture(shape, shapes, ground, it) }
        departures.zipWithNext().forEach { (coarse, fine) -> assert(fine < coarse) }
        // FIRST ORDER in `1/s`: the product `s x departure` settles rather than the departure
        // falling by a fixed factor from the first rung — the prefactor is set by the
        // conditioning of the body's tilt block and is not small.
        val products = scales.indices.map { scales[it] * departures[it] }
        val settling = abs(products.last() - products[products.size - 2]) / products.last()
        assert(settling < 0.05)
        assert(departures.last() < 1e-4)
    }

    @Test
    fun `gate 3 - the centro-symmetric phases of the census are C-0063's own two`() {
        val census = upwardTieCensus(edgeX, duplexes)
        val symmetric = census.filter { it.isCentroSymmetric() }.map { it.phaseBasePairs }
        assert(symmetric == centroSymmetricUpwardPhases(edgeX, duplexes))
        assert(symmetric == listOf(8, 24))
    }

    @Test
    fun `gate 3 - F3 - a uniform load dishes exactly zero, uncoupled and under a free body`() {
        val lattice = upwardTieLattice(24, edgeX, duplexes)
        val stations = lattice.stations(sheet.interhelicalDistance)
        val uniform = latticeInfluenceSurrogate(
            host(24), stations, uniformPressure(interiorPressure), 41
        )
        val ties = List(stations.size) { 50.0 }
        val coupled = uniform.solveWithSharedBody(
            ties, rigidBody.freeAt(stations), List(stations.size) { true }
        )
        assert(coupled.supportForces.all { abs(it) < 1e-9 * Gen1Tile.TARGET_FORCE })
        assert(coupled.peakDishing < 1e-9)
        assert(
            uniform.solveWithSharedBody(
                List(stations.size) { 1e-30 }, null, List(stations.size) { true }
            ).peakDishing < 1e-9
        )
    }

    // ------------------------------------------- gate 4: numerical convergence and descent power

    @Test
    fun `gate 4 - a tie descent never returns a point worse than its start`() {
        val objective = { ties: List<Double> -> ties.sumOf { (it - 2.5) * (it - 2.5) } + 1.0 }
        val start = List(5) { 1.0 + it }
        val found = optimiseTieDistribution(start, 0.5, 8.0, sweeps = 4, objective = objective)
        assert(found.objective <= objective(start))
        assert(found.ties.all { it >= 0.5 && it <= 8.0 })
    }

    @Test
    fun `gate 4 - a subset descent never returns a point worse than its start`() {
        val values = listOf(5.0, 1.0, 4.0, 2.0, 3.0)
        val objective = { chosen: List<Int> -> chosen.sumOf { values[it] } }
        val start = listOf(0, 2)
        val found = descendTieSubset(start, values.indices.toList(), objective = objective)
        assert(found.objective <= objective(start))
        assert(found.indices.toSet() == setOf(1, 3))
    }

    // ---------------------------------------------------------- gate 5: upstream cross-check

    @Test
    fun `gate 5 - F4 - the shared body beats the array on the identical real stations`() {
        val stations = upwardTieLattice(24, edgeX, duplexes)
            .stations(sheet.interhelicalDistance)
        assert(stations.size == 53)
        val surrogate = latticeInfluenceSurrogate(host(24), stations, solvedField, 81)
        val equal = List(stations.size) { mandate / stations.size }
        val arrayDishing = surrogate.solve(equal).peakDishing / freeStroke
        val ties = List(stations.size) { 1000.0 }
        val state = rigidBody.placedAt(stations, ties)
        val shared = surrogate.solveWithSharedBody(
            ties, state.body, List(stations.size) { true }
        ).peakDishing / freeStroke
        assert(shared < arrayDishing)
        assert(state.heaveSecant.isCloseTo(mandate, 1e-6))
        assert(state.groundComplianceShare > 0.9)
    }

    @Test
    fun `gate 5 - the free-tile stroke is C-0026's 4_90731102 nm`() {
        assert(freeStroke.isCloseTo(4.90731102, 1e-6))
    }

}
