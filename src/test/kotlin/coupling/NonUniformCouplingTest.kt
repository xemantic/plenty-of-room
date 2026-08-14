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
import com.xemantic.nano.plentyofroom.structure.PointSupport
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.nano.plentyofroom.synthesis.perPathSecantCeiling
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-113` — can a **non-uniform** coupling stiffness buy back the edge dishing?
 *
 * Every test is named for the verification gate it discharges. The disciplines from
 * `CLAUDE.md` that govern this file:
 *
 * - **a uniform load on a uniform Winkler foundation must produce exactly zero dishing** on a
 *   free tile — the free falsifier, wired in as gate 2's first test;
 * - **mesh monotonicity holds only on nested refinements**, so gate 4 sweeps `1 ⊂ 2 ⊂ 4`;
 * - a Rothemund sheet is **centro-symmetric, not mirror-symmetric**, so gate 3 checks the
 *   symmetry the lattice actually has (reversal of the whole distribution, a point reflection)
 *   and checks mirror symmetry on the **plate**, which does have the full rectangular group.
 */
class NonUniformCouplingTest {

    private val sheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )

    private val duplexes = 15

    private val lengthY = duplexes * sheet.interhelicalDistance

    private val interiorPressure = Gen1Tile.TARGET_FORCE / (Gen1Tile.EDGE_X * lengthY)

    private val mandate = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

    private fun lattice(
        supports: List<PointSupport> = emptyList(),
        subdivisions: Int = 2
    ) = OrigamiGrillage(
        sheet = sheet,
        lengthX = Gen1Tile.EDGE_X,
        beamCount = duplexes,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = CrossoverLayout.centred(8, sheet.crossoverSpacing / 2.0),
        subdivisions = subdivisions,
        supports = supports
    )

    private fun plate(supports: List<PointSupport> = emptyList(), basisDegree: Int = 12) =
        PlateOnFoundation(
            sheet.plate(Gen1Tile.EDGE_X, lengthY),
            Gen1Tile.FOUNDATION_SECANT,
            supports,
            basisDegree
        )

    /** `C-0022`'s design point, transcribed so the test file needs no result file. */
    private val solvedField: PressureField = edgeCollarPressure(
        interiorPressure, Gen1Tile.EDGE_X, lengthY,
        listOf(CollarTerm(-0.302887367, 8.93928311), CollarTerm(-0.593889278, 1.0))
    )

    private fun gridOf(columns: Int) =
        attachmentGrid(columns, duplexes, Gen1Tile.EDGE_X, lengthY)

    private fun latticeSurrogate(
        columns: Int,
        field: PressureField = solvedField,
        subdivisions: Int = 2,
        samples: Int = 81
    ) = latticeInfluenceSurrogate(lattice(subdivisions = subdivisions), gridOf(columns), field, samples)

    // ---------------------------------------------------------------- gate 1 — dimensional

    @Test
    fun `gate 1 dimensional - a normalised distribution carries exactly the mandated total stiffness`() {
        val weights = listOf(1.0, 3.0, 0.5, 7.25, 0.125)
        val stiffnesses = normalisedStiffnesses(weights, mandate)
        assert(stiffnesses.sum().isCloseTo(mandate, 1e-12))
        // and the ratios of the weights are preserved
        assert((stiffnesses[1] / stiffnesses[0]).isCloseTo(3.0, 1e-12))
    }

    @Test
    fun `gate 1 dimensional - the per-path stiffness ceiling is an allowable over a stroke and tightens as one over the stroke`() {
        val atThree = perPathStiffnessCeiling(Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, 3.0)
        val atTen = perPathStiffnessCeiling(Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, 10.0)
        assert(atThree.isCloseTo(10.0 / 3.0, 1e-12))
        assert((atThree / atTen).isCloseTo(10.0 / 3.0, 1e-12))
    }

    @Test
    fun `gate 1 dimensional - the admissible stiffness ratio is dimensionless and is 1_5 at fifteen paths and 4_5 at forty-five`() {
        val atFifteen = admissibleStiffnessRatio(
            Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE, mandate, 15
        )
        val atFortyFive = admissibleStiffnessRatio(
            Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE, mandate, 45
        )
        assert(atFifteen.isCloseTo(1.5, 1e-12))
        assert(atFortyFive.isCloseTo(4.5, 1e-12))
        // at the DESIRED stroke fifteen paths admit no distribution at all — not even the uniform one
        val desired = admissibleStiffnessRatio(
            Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.DESIRED_STROKE, mandate, 15
        )
        assert(desired.isCloseTo(0.45, 1e-12))
        assert(desired < 1.0)
    }

    @Test
    fun `gate 1 dimensional - the dishing is exactly linear in the applied pressure`() {
        val single = latticeSurrogate(1)
        val tripled = latticeInfluenceSurrogate(
            lattice(),
            gridOf(1),
            edgeCollarPressure(
                3.0 * interiorPressure, Gen1Tile.EDGE_X, lengthY,
                listOf(CollarTerm(-0.302887367, 8.93928311), CollarTerm(-0.593889278, 1.0))
            ),
            81
        )
        val stiffnesses = normalisedStiffnesses(List(duplexes) { 1.0 }, mandate)
        assert(
            tripled.solve(stiffnesses).peakDishing
                .isCloseTo(3.0 * single.solve(stiffnesses).peakDishing, 1e-10)
        )
    }

    @Test
    fun `gate 1 dimensional - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { normalisedStiffnesses(listOf(1.0, -1.0), 1.0) }
        assertFailsWith<IllegalArgumentException> { normalisedStiffnesses(emptyList(), 1.0) }
        assertFailsWith<IllegalArgumentException> { normalisedStiffnesses(listOf(1.0), 0.0) }
        assertFailsWith<IllegalArgumentException> { perPathStiffnessCeiling(10.0, 0.0) }
        // a ceiling below the uniform share admits no distribution at all
        assertFailsWith<IllegalArgumentException> {
            cappedStiffnesses(List(4) { 1.0 }, totalStiffness = 8.0, ceiling = 1.0)
        }
        assertFailsWith<IllegalArgumentException> {
            latticeSurrogate(1).solve(List(3) { 1.0 })
        }
    }

    // ---------------------------------------------------------------- gate 2 — limiting cases

    @Test
    fun `gate 2 limiting - a uniform load on a free tile dishes exactly zero, lattice and plate`() {
        val uniform = uniformPressure(interiorPressure)
        assert(abs(lattice().solve(uniform).peakDishing(81)) < 1e-9)
        assert(abs(plate().solve(uniform).peakDishing(81)) < 1e-9)
    }

    @Test
    fun `gate 2 limiting - a stiffening ratio of one is the uniform distribution identically`() {
        val grid = gridOf(3)
        val weights = rimStiffenedWeights(grid, Gen1Tile.EDGE_X, lengthY, collarWidth = 8.939, ratio = 1.0)
        assert(weights.all { it.isCloseTo(1.0, 1e-15) })
        val stiffnesses = normalisedStiffnesses(weights, mandate)
        assert(stiffnesses.all { it.isCloseTo(mandate / grid.size, 1e-12) })
    }

    @Test
    fun `gate 2 limiting - a ceiling at the uniform share returns the uniform distribution exactly`() {
        val weights = listOf(1.0, 4.0, 9.0, 16.0)
        val capped = cappedStiffnesses(weights, totalStiffness = mandate, ceiling = mandate / 4.0)
        assert(capped.all { it.isCloseTo(mandate / 4.0, 1e-12) })
        // and an unbounded ceiling is the unconstrained normalisation
        val free = cappedStiffnesses(weights, mandate, ceiling = Double.POSITIVE_INFINITY)
        val plain = normalisedStiffnesses(weights, mandate)
        free.indices.forEach { assert(free[it].isCloseTo(plain[it], 1e-12)) }
    }

    @Test
    fun `gate 2 limiting - a load-matched distribution under a uniform load is the uniform one`() {
        val grid = gridOf(3)
        val flat = loadMatchedWeights(grid, uniformPressure(interiorPressure))
        assert(flat.all { it.isCloseTo(interiorPressure, 1e-15) })
        // and under C-0022's solved collar the rim stations carry more, by the collar's own depth
        val matched = loadMatchedWeights(grid, solvedField)
        val corner = matched.first()
        val centre = matched[grid.indices.minByOrNull { i ->
            abs(grid[i].first) + abs(grid[i].second)
        }!!]
        assert(corner > centre)
    }

    @Test
    fun `gate 2 limiting - the optimiser is a descent and never returns worse than its start`() {
        val surrogate = latticeSurrogate(1)
        val start = List(duplexes) { 1.0 }
        val startObjective = surrogate.solve(normalisedStiffnesses(start, mandate)).peakDishing
        val optimum = optimiseStiffnessDistribution(
            totalStiffness = mandate,
            starts = listOf(start),
            sweeps = 2
        ) { surrogate.solve(it).peakDishing }
        assert(optimum.objective <= startObjective + 1e-12)
        assert(optimum.stiffnesses.sum().isCloseTo(mandate, 1e-12))
    }

    // ---------------------------------------------------------------- gate 3 — symmetry

    @Test
    fun `gate 3 symmetry - the capped projection conserves the mandate exactly with the cap active`() {
        val weights = listOf(1.0, 2.0, 30.0, 4.0, 5.0)
        val ceiling = 1.5 * mandate / weights.size
        val capped = cappedStiffnesses(weights, mandate, ceiling)
        assert(capped.sum().isCloseTo(mandate, 1e-12))
        assert(capped.max() <= ceiling * (1.0 + 1e-12))
        // the cap is genuinely active — the third path would otherwise take most of the total
        assert(capped[2].isCloseTo(ceiling, 1e-12))
    }

    /**
     * A smooth non-uniform field, for the conservation gate.
     *
     * `C-0047` established that `C-0022`'s collar has a `C⁰` kink at the standoff which costs the
     * lattice's own load quadrature under a tenth of a per cent; that is a property of the field
     * and is checked separately, immediately below, rather than hidden inside a conservation gate.
     */
    private val smoothAcrossHelices = PressureField { _, y ->
        interiorPressure * (1.0 + 0.3 * kotlin.math.cos(2.0 * Math.PI * y / lengthY))
    }

    private fun rimStiffenedSupports(columns: Int, ratio: Double): List<PointSupport> {
        val grid = gridOf(columns)
        val stiffnesses = normalisedStiffnesses(
            rimStiffenedWeights(grid, Gen1Tile.EDGE_X, lengthY, 8.939, ratio), mandate
        )
        return grid.mapIndexed { i, (x, y) -> PointSupport(x, y, stiffnesses[i]) }
    }

    @Test
    fun `gate 3 symmetry - support forces plus the foundation carry the whole applied load`() {
        val solution = lattice(rimStiffenedSupports(3, 3.0)).solve(smoothAcrossHelices)
        val carried = solution.supportForces.sum() + solution.foundationForce
        assert(carried.isCloseTo(solution.appliedForce, 1e-6))
    }

    @Test
    fun `gate 3 conservation - the collar's kink costs a non-uniform coupling under a tenth of a per cent`() {
        val solution = lattice(rimStiffenedSupports(3, 3.0)).solve(solvedField)
        val carried = solution.supportForces.sum() + solution.foundationForce
        assert(carried.isCloseTo(solution.appliedForce, 1e-3))
    }

    @Test
    fun `gate 3 symmetry - a point-reflected distribution dishes identically on the centro-symmetric lattice`() {
        val surrogate = latticeSurrogate(3)
        val weights = gridOf(3).indices.map { 1.0 + 0.1 * it }
        val forward = surrogate.solve(normalisedStiffnesses(weights, mandate)).peakDishing
        val reversed = surrogate.solve(normalisedStiffnesses(weights.reversed(), mandate)).peakDishing
        assert(reversed.isCloseTo(forward, 1e-9))
    }

    @Test
    fun `gate 3 symmetry - a mirrored distribution dishes identically on the plate, which has the full rectangular group`() {
        val grid = gridOf(3)
        val surrogate = plateInfluenceSurrogate(plate(), grid, solvedField, 81)
        // mirror in x: column index c -> (columns-1-c), rows unchanged
        val weights = grid.indices.map { 1.0 + 0.1 * (it % 3) }
        val mirrored = grid.indices.map { 1.0 + 0.1 * (2 - it % 3) }
        val forward = surrogate.solve(normalisedStiffnesses(weights, mandate)).peakDishing
        val flipped = surrogate.solve(normalisedStiffnesses(mirrored, mandate)).peakDishing
        assert(flipped.isCloseTo(forward, 1e-9))
    }

    // ---------------------------------------------------------------- gate 4 — convergence

    @Test
    fun `gate 4 convergence - nested subdivisions one two four tighten monotonically`() {
        val stiffnesses = normalisedStiffnesses(
            rimStiffenedWeights(gridOf(3), Gen1Tile.EDGE_X, lengthY, 8.939, 3.0), mandate
        )
        val values = listOf(1, 2, 4).map {
            latticeSurrogate(3, subdivisions = it).solve(stiffnesses).peakDishing
        }
        val coarse = abs(values[0] - values[2]) / values[2]
        val fine = abs(values[1] - values[2]) / values[2]
        assert(fine < coarse)
        assert(fine < 2e-3)
    }

    @Test
    fun `gate 4 convergence - the dishing sampling grid is converged at 81 points`() {
        val stiffnesses = normalisedStiffnesses(
            rimStiffenedWeights(gridOf(3), Gen1Tile.EDGE_X, lengthY, 8.939, 3.0), mandate
        )
        val values = listOf(41, 81, 161).map {
            latticeSurrogate(3, samples = it).solve(stiffnesses).peakDishing
        }
        assert(abs(values[1] - values[2]) / values[2] < 1e-2)
    }

    // ---------------------------------------------------------------- gate 5 — cross-check

    @Test
    fun `gate 5 cross-check - the Woodbury surrogate reproduces the assembled solve for a non-uniform distribution`() {
        val grid = gridOf(3)
        val stiffnesses = normalisedStiffnesses(
            rimStiffenedWeights(grid, Gen1Tile.EDGE_X, lengthY, 8.939, 4.0), mandate
        )
        val supports = grid.mapIndexed { i, (x, y) -> PointSupport(x, y, stiffnesses[i]) }
        val assembled = lattice(supports).solve(solvedField)
        val surrogate = latticeSurrogate(3).solve(stiffnesses)
        assert(surrogate.peakDishing.isCloseTo(assembled.peakDishing(81), 1e-9))
        assembled.supportForces.indices.forEach {
            assert(surrogate.supportForces[it].isCloseTo(assembled.supportForces[it], 1e-9))
        }
    }

    @Test
    fun `gate 5 cross-check - the surrogate reproduces the same on the plate`() {
        val grid = gridOf(3)
        val stiffnesses = normalisedStiffnesses(
            rimStiffenedWeights(grid, Gen1Tile.EDGE_X, lengthY, 8.939, 4.0), mandate
        )
        val supports = grid.mapIndexed { i, (x, y) -> PointSupport(x, y, stiffnesses[i]) }
        val assembled = plate(supports).solve(solvedField)
        val surrogate = plateInfluenceSurrogate(plate(), grid, solvedField, 81).solve(stiffnesses)
        assert(surrogate.peakDishing.isCloseTo(assembled.peakDishing(81), 1e-8))
    }

    @Test
    fun `gate 5 cross-check - C-0047's uniform 1x15 and 3x15 dishings are reproduced as the limiting case`() {
        val uniformOne = latticeSurrogate(1)
            .solve(normalisedStiffnesses(List(duplexes) { 1.0 }, mandate)).peakDishing
        val uniformThree = latticeSurrogate(3)
            .solve(normalisedStiffnesses(List(3 * duplexes) { 1.0 }, mandate)).peakDishing
        assert(uniformOne.isCloseTo(3.412, 2e-3))
        assert(uniformThree.isCloseTo(1.071, 2e-3))
        // and C-0047's free tile, which is the bar the 1 x 15 coupling fails
        assert(lattice().solve(solvedField).peakDishing(81).isCloseTo(1.511, 2e-3))
    }

    @Test
    fun `gate 5 cross-check - C-0049's per-path secant ceiling is this task's admissible ratio times the mandate`() {
        listOf(15, 45).forEach { paths ->
            val ratio = admissibleStiffnessRatio(
                Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE, mandate, paths
            )
            val ceiling = perPathSecantCeiling(
                Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, paths, Gen1Tile.ACCEPTABLE_STROKE
            )
            assert((ratio * mandate).isCloseTo(ceiling, 1e-12))
        }
    }

    @Test
    fun `gate 5 cross-check - C-0014's per-anchor thermal force is the equal-path limit of the unequal-path one`() {
        val equal = List(45) { mandate / 45.0 }
        val forces = perPathThermalForces(equal)
        val published = perAnchorThermalForce(mandate, 45)
        forces.forEach { assert(it.isCloseTo(published, 1e-12)) }
        // and a path carrying twice the share carries twice the thermal force, not the square root of it
        val unequal = perPathThermalForces(listOf(2.0, 1.0, 1.0))
        assert((unequal[0] / unequal[1]).isCloseTo(2.0, 1e-12))
    }

    @Test
    fun `gate 5 cross-check - the reachable dishing floor bounds every distribution from below`() {
        val surrogate = latticeSurrogate(3)
        val floor = surrogate.reachableDishingFloor
        val uniform = surrogate.solve(normalisedStiffnesses(List(45) { 1.0 }, mandate)).peakDishing
        assert(floor > 0.0)
        assert(floor <= uniform)
        val optimum = optimiseStiffnessDistribution(
            totalStiffness = mandate,
            starts = listOf(List(45) { 1.0 }),
            sweeps = 2
        ) { surrogate.solve(it).peakDishing }
        assert(floor <= optimum.objective)
    }

}
