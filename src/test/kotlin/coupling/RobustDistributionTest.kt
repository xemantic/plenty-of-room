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
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlin.math.abs
import kotlin.math.ln
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-123` — is **any** distribution flat at every one of `C-0022`'s solved states?
 *
 * Every test is named for the verification gate it discharges. What governs this file:
 *
 * - the standing free falsifier — **a uniform load on a free tile dishes exactly zero** —
 *   which is gate 2's first test here as it is in `C-0058`'s;
 * - `C-0058`'s [InfluenceSurrogate] is an **independent implementation** of the same
 *   superposition, so gate 5 asserts this task's multi-state surrogate against it rather than
 *   against itself;
 * - the whole conjugate-gradient search rests on an **analytic gradient through the Woodbury
 *   solve**, so gate 4's first test is that gradient against a central finite difference —
 *   without it every CG step is unverified;
 * - a Rothemund sheet is **centro-symmetric, not mirror-symmetric** (`C-0015`), so gate 3
 *   checks the symmetry the lattice actually has.
 */
class RobustDistributionTest {

    private val sheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )

    private val duplexes = 15

    private val lengthY = duplexes * sheet.interhelicalDistance

    private val interiorPressure = Gen1Tile.TARGET_FORCE / (Gen1Tile.EDGE_X * lengthY)

    private val mandate = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

    private fun lattice(subdivisions: Int = 2) = OrigamiGrillage(
        sheet = sheet,
        lengthX = Gen1Tile.EDGE_X,
        beamCount = duplexes,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = CrossoverLayout.centred(8, sheet.crossoverSpacing / 2.0),
        subdivisions = subdivisions,
        supports = emptyList()
    )

    private fun plate(basisDegree: Int = 12) = PlateOnFoundation(
        sheet.plate(Gen1Tile.EDGE_X, lengthY), Gen1Tile.FOUNDATION_SECANT, emptyList(), basisDegree
    )

    private fun field(depth: Double, width: Double, rim: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, Gen1Tile.EDGE_X, lengthY,
            listOf(CollarTerm(depth, width), CollarTerm(rim, 1.0))
        )

    /**
     * Three of `C-0022`'s solved states, transcribed from `gpd/results/T-3b-*.json` so the test
     * file needs no result file — the design point, the 10 mM state whose collar has the
     * **opposite sign**, and the 2 nm state `C-0058` names as the one its flat design breaks.
     */
    private val designPoint = field(-0.302887367, 8.93928311, -0.593889278)

    private val tenMillimolar = field(0.419998636, 2.39768412, -2.73316696)

    private val twoNanometreGap = field(-0.0514981261, 6.56393103, 1.08681801)

    private val states = listOf(
        LoadState("2 mM, 10 nm, 0.192 V", designPoint),
        LoadState("10 mM, 10 nm, 0.192 V", tenMillimolar),
        LoadState("2 mM, 2 nm, 0.368 V", twoNanometreGap)
    )

    private fun gridOf(columns: Int) =
        attachmentGrid(columns, duplexes, Gen1Tile.EDGE_X, lengthY)

    private fun surrogateOf(
        columns: Int,
        loads: List<LoadState> = states,
        samples: Int = 81,
        subdivisions: Int = 2
    ) = multiStateSurrogate(lattice(subdivisions), gridOf(columns), loads, samples)

    private fun uniform(paths: Int) = normalisedStiffnesses(List(paths) { 1.0 }, mandate)

    // ---------------------------------------------------------------- gate 1 — dimensional

    @Test
    fun `gate 1 dimensional - the worst dishing over a one-state set is that state's peak dishing`() {
        val surrogate = surrogateOf(3)
        val stiffnesses = uniform(45)
        val peaks = surrogate.peakDishing(stiffnesses)
        peaks.indices.forEach { state ->
            assert(surrogate.worstDishing(stiffnesses, listOf(state)).isCloseTo(peaks[state], 1e-15))
        }
        assert(surrogate.worstDishing(stiffnesses).isCloseTo(peaks.max(), 1e-15))
    }

    @Test
    fun `gate 1 dimensional - the dishing of every state is exactly linear in the applied pressure`() {
        val one = surrogateOf(3, states.map { LoadState(it.name, it.pressure) })
        val doubled = surrogateOf(
            3, states.map { state -> LoadState(state.name, PressureField { x, y -> 2.0 * state.pressure.at(x, y) }) }
        )
        val stiffnesses = uniform(45)
        one.peakDishing(stiffnesses).zip(doubled.peakDishing(stiffnesses)).forEach { (a, b) ->
            assert(b.isCloseTo(2.0 * a, 1e-10))
        }
    }

    @Test
    fun `gate 1 dimensional - quantisation to levels conserves the mandate and yields no more than the levels asked for`() {
        val stiffnesses = normalisedStiffnesses(
            List(45) { 0.2 + 0.05 * it }, mandate
        )
        listOf(2, 3, 4).forEach { levels ->
            val quantised = quantiseToLevels(stiffnesses, levels, mandate)
            assert(quantised.sum().isCloseTo(mandate, 1e-12))
            assert(quantised.distinct().size <= levels)
            assert(quantised.size == stiffnesses.size)
        }
    }

    @Test
    fun `gate 1 dimensional - unphysical arguments throw`() {
        val surrogate = surrogateOf(1)
        assertFailsWith<IllegalArgumentException> { surrogate.peakDishing(uniform(14)) }
        assertFailsWith<IllegalArgumentException> { surrogate.peakDishing(List(15) { 0.0 }) }
        assertFailsWith<IllegalArgumentException> { surrogate.worstDishing(uniform(15), emptyList()) }
        assertFailsWith<IllegalArgumentException> { surrogate.worstDishing(uniform(15), listOf(7)) }
        assertFailsWith<IllegalArgumentException> {
            multiStateSurrogate(lattice(), gridOf(1), emptyList(), 41)
        }
        assertFailsWith<IllegalArgumentException> { quantiseToLevels(uniform(15), 0, mandate) }
        assertFailsWith<IllegalArgumentException> {
            surrogate.smoothedObjective(uniform(15), 0.0, listOf(0))
        }
    }

    // ---------------------------------------------------------------- gate 2 — limiting cases

    @Test
    fun `gate 2 limiting - a uniform load on a free tile dishes exactly zero, lattice and plate`() {
        val uniformLoad = uniformPressure(interiorPressure)
        assert(lattice().solve(uniformLoad).peakDishing(81) < 1e-9)
        assert(plate().solve(uniformLoad).peakDishing(81) < 1e-9)
    }

    @Test
    fun `gate 2 limiting - the smoothed objective converges to the true maximum from above as the smoothing vanishes`() {
        val surrogate = surrogateOf(3)
        val stiffnesses = uniform(45)
        val truth = surrogate.worstDishing(stiffnesses)
        var previous = Double.POSITIVE_INFINITY
        listOf(1.0, 0.1, 0.01, 1e-3, 1e-4).forEach { smoothing ->
            val value = surrogate.smoothedObjective(stiffnesses, smoothing, listOf(0, 1, 2)).value
            assert(value >= truth)
            assert(value < previous)
            // the log-sum-exp bound: the excess is at most `mu ln(2 N)` over 2N signed samples
            assert(value - truth <= smoothing * ln(2.0 * 3 * 81 * 81) * (1.0 + 1e-9))
            previous = value
        }
    }

    @Test
    fun `gate 2 limiting - a minimax over a subset is never worse than the same distribution over a superset`() {
        val surrogate = surrogateOf(3)
        val stiffnesses = uniform(45)
        assert(
            surrogate.worstDishing(stiffnesses, listOf(0, 2)) <=
                    surrogate.worstDishing(stiffnesses, listOf(0, 1, 2)) * (1.0 + 1e-15)
        )
    }

    @Test
    fun `gate 2 limiting - the minimax search is a descent and never returns worse than its best start`() {
        val surrogate = surrogateOf(3)
        val start = uniform(45)
        val startValue = surrogate.worstDishing(start)
        val optimum = minimaxStiffnessDistribution(
            surrogate = surrogate,
            states = listOf(0, 1, 2),
            totalStiffness = mandate,
            starts = listOf(List(45) { 1.0 }),
            smoothingLevels = listOf(0.1, 0.01),
            iterationsPerLevel = 5,
            polishSweeps = 1
        )
        assert(optimum.worstDishing <= startValue * (1.0 + 1e-12))
        assert(optimum.stiffnesses.sum().isCloseTo(mandate, 1e-12))
        assert(optimum.perStateDishing.max().isCloseTo(optimum.worstDishing, 1e-12))
    }

    // ---------------------------------------------------------------- gate 3 — symmetry

    @Test
    fun `gate 3 symmetry - Maxwell-Betti reciprocity of the station influence matrix holds between two quadratures`() {
        assert(surrogateOf(3).reciprocityResidual < 1e-12)
    }

    @Test
    fun `gate 3 symmetry - a point-reflected distribution dishes identically on the centro-symmetric lattice`() {
        val surrogate = surrogateOf(3)
        val stiffnesses = normalisedStiffnesses(List(45) { 1.0 + 0.02 * it }, mandate)
        val reflected = stiffnesses.reversed()
        surrogate.peakDishing(stiffnesses).zip(surrogate.peakDishing(reflected)).forEach { (a, b) ->
            assert(b.isCloseTo(a, 1e-9))
        }
    }

    @Test
    fun `gate 3 conservation - the softmax parametrisation carries exactly the mandated total at every point`() {
        val surrogate = surrogateOf(1)
        val optimum = minimaxStiffnessDistribution(
            surrogate = surrogate,
            states = listOf(0, 1),
            totalStiffness = mandate,
            starts = listOf(List(15) { 1.0 }, List(15) { 1.0 + 0.1 * it }),
            smoothingLevels = listOf(0.1),
            iterationsPerLevel = 3,
            polishSweeps = 1
        )
        assert(optimum.stiffnesses.sum().isCloseTo(mandate, 1e-12))
        assert(optimum.stiffnesses.all { it > 0.0 })
    }

    // ---------------------------------------------------------------- gate 4 — convergence

    @Test
    fun `gate 4 convergence - the analytic gradient matches a central finite difference`() {
        val surrogate = surrogateOf(1)
        val stiffnesses = normalisedStiffnesses(List(15) { 1.0 + 0.13 * it }, mandate)
        val smoothing = 0.05
        val analytic = surrogate.smoothedObjective(stiffnesses, smoothing, listOf(0, 1, 2)).gradient
        stiffnesses.indices.forEach { index ->
            val step = 1e-6 * stiffnesses[index]
            val up = stiffnesses.toMutableList().also { it[index] += step }
            val down = stiffnesses.toMutableList().also { it[index] -= step }
            val numeric = (
                    surrogate.smoothedObjective(up, smoothing, listOf(0, 1, 2)).value -
                            surrogate.smoothedObjective(down, smoothing, listOf(0, 1, 2)).value
                    ) / (2.0 * step)
            assert(abs(analytic[index] - numeric) <= 1e-5 * (1.0 + abs(numeric)))
        }
    }

    @Test
    fun `gate 4 convergence - the reachable floor per state bounds every distribution from below`() {
        val surrogate = surrogateOf(3)
        val stiffnesses = uniform(45)
        val peaks = surrogate.peakDishing(stiffnesses)
        peaks.indices.forEach { state ->
            val floor = surrogate.reachableDishingFloor(state)
            assert(floor > 0.0)
            assert(floor <= peaks[state])
        }
    }

    // ---------------------------------------------------------------- gate 5 — cross-check

    @Test
    fun `gate 5 cross-check - the multi-state surrogate reproduces C-0058's single-state surrogate exactly`() {
        val grid = gridOf(3)
        val stiffnesses = normalisedStiffnesses(
            rimStiffenedWeights(grid, Gen1Tile.EDGE_X, lengthY, 6.7, 5.0), mandate
        )
        val published = latticeInfluenceSurrogate(lattice(), grid, designPoint, 81)
        val here = multiStateSurrogate(lattice(), grid, states, 81)
        assert(here.peakDishing(stiffnesses)[0].isCloseTo(published.solve(stiffnesses).peakDishing, 1e-12))
        here.supportForces(stiffnesses, 0)
            .zip(published.solve(stiffnesses).supportForces)
            .forEach { (a, b) -> assert(a.isCloseTo(b, 1e-10)) }
        assert(here.reachableDishingFloor(0).isCloseTo(published.reachableDishingFloor, 1e-10))
    }

    @Test
    fun `gate 5 cross-check - C-0058's uniform and rim x 5 numbers are reproduced at the design point`() {
        val stroke = 4.90731
        val grid = gridOf(3)
        val surrogate = multiStateSurrogate(lattice(), grid, states, 81)
        val uniformHere = surrogate.peakDishing(uniform(45))[0] / stroke
        val rim = normalisedStiffnesses(
            rimStiffenedWeights(grid, Gen1Tile.EDGE_X, lengthY, 6.7, 5.0), mandate
        )
        assert(uniformHere.isCloseTo(0.2182, 3e-3))
        assert((surrogate.peakDishing(rim)[0] / stroke).isCloseTo(0.0753, 3e-3))
        // and C-0058's own state finding: the same rim design is WORSE than uniform at 2 nm
        assert(surrogate.peakDishing(rim)[2] > surrogate.peakDishing(uniform(45))[2])
    }

    @Test
    fun `gate 5 cross-check - the real optimiser matches or beats C-0058's published minimax`() {
        val grid = gridOf(3)
        val surrogate = multiStateSurrogate(lattice(), grid, states, 81)
        val optimum = minimaxStiffnessDistribution(
            surrogate = surrogate,
            states = listOf(0, 1, 2),
            totalStiffness = mandate,
            starts = listOf(
                List(45) { 1.0 },
                rimStiffenedWeights(grid, Gen1Tile.EDGE_X, lengthY, 6.7, 5.0)
            ),
            smoothingLevels = listOf(0.3, 0.1, 0.03, 0.01),
            iterationsPerLevel = 12,
            polishSweeps = 2
        )
        // C-0058's five-state minimax reached 0.1587 of the 4.90731 nm stroke; this subset of
        // three contains both of the states that bind there, so a real optimiser must not do
        // worse than a coordinate descent did on a superset.
        assert(optimum.worstDishing / 4.90731 <= 0.1587)
        assert(optimum.bindingStates.isNotEmpty())
    }

    @Test
    fun `gate 5 cross-check - C-0060's flat ratio window and C-0049's ceiling are re-derived, not cited`() {
        // C-0049's per-path ceiling as a stiffness, and its 1/s tightening
        assert(
            perPathStiffnessCeiling(Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE)
                .isCloseTo(10.0 / 3.0, 1e-12)
        )
        assert(
            admissibleStiffnessRatio(
                Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE, mandate, 45
            ).isCloseTo(4.5, 1e-12)
        )
        // C-0060's two levels, re-derived from C-0058's own rim x 5 rule rather than tabulated
        val grid = gridOf(3)
        val levels = normalisedStiffnesses(
            rimStiffenedWeights(grid, Gen1Tile.EDGE_X, lengthY, 6.7, 5.0), mandate
        ).distinct().sorted()
        assert(levels.size == 2)
        assert(levels[0].isCloseTo(0.1842, 1e-3))
        assert(levels[1].isCloseTo(0.9208, 1e-3))
        assert((levels[1] / levels[0]).isCloseTo(5.0, 1e-9))
    }

}
