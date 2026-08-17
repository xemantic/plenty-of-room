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
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-155` — is there a placement that is flat **under** the measured staple dropout?
 *
 * Every test is named for the verification gate it discharges, and the falsifiers `T-155`
 * declares are asserted rather than argued:
 *
 * - **`F3`** — the uncoupled tile under a **uniform** load must dish exactly zero;
 * - **`F4`** — the zero-dropout limit must reproduce `C-0017`'s 0.2182 and `C-0058`'s 0.0753,
 *   and the dropout pipeline must reproduce `C-0087`'s own single-removal bound;
 * - **`F5`** — the oracle floor under dropout is a **pointwise** lower bound on every
 *   realisation's peak dishing, whatever distribution is asked for, and that is the property
 *   that makes it able to settle the question with no search at all.
 *
 * The disciplines from `CLAUDE.md` that govern this file: a random stream a result file depends
 * on must be **bit-reproducible from its seed**; a percentile is an **order statistic**; and two
 * quantities both meant to be zero are compared **absolutely**.
 */
class DropoutRobustPlacementTest {

    private val duplexes = 15

    private val sheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )

    private val edgeX = Gen1Tile.EDGE_X

    private val edgeY = duplexes * sheet.interhelicalDistance

    private val interiorPressure = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)

    private val mandate = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

    /** `C-0022`'s design point at 2 mM / 10 nm / 0.192 V, as `StapleDropoutTest` transcribes it. */
    private val solvedField = edgeCollarPressure(
        interiorPressure, edgeX, edgeY,
        listOf(CollarTerm(-0.302887367, 8.93928311), CollarTerm(-0.593889278, 1.0))
    )

    private fun lattice(subdivisions: Int = 2) = OrigamiGrillage(
        sheet = sheet,
        lengthX = edgeX,
        beamCount = duplexes,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = CrossoverLayout.centred(8, sheet.crossoverSpacing / 2.0),
        subdivisions = subdivisions,
        supports = emptyList()
    )

    private val grid = attachmentGrid(3, duplexes, edgeX, edgeY)

    private val equal45 = List(grid.size) { mandate / grid.size }

    /** `C-0026`'s free-tile stroke, recomputed rather than transcribed. */
    private val freeStroke = PlateOnFoundation(
        sheet.plate(edgeX, edgeY), Gen1Tile.FOUNDATION_SECANT, emptyList(), 12
    ).solve(uniformPressure(interiorPressure)).meanDeflection

    private val surrogate45 by lazy { latticeInfluenceSurrogate(lattice(), grid, solvedField, 81) }

    // ------------------------------------------------------------------ gate 1: dimensional

    @Test
    fun `gate 1 - an ensemble carries one dimensionless probability per path and refuses anything else`() {
        assertFailsWith<IllegalArgumentException> { dropoutEnsemble(emptyList(), 10, 1L) }
        assertFailsWith<IllegalArgumentException> { dropoutEnsemble(listOf(0.5), 0, 1L) }
        assertFailsWith<IllegalArgumentException> { dropoutEnsemble(listOf(1.5), 10, 1L) }
        assertFailsWith<IllegalArgumentException> { dropoutEnsemble(listOf(-0.1), 10, 1L) }
        val ensemble = dropoutEnsemble(List(6) { 0.8 }, 32, 7L)
        assert(ensemble.pathCount == 6)
        assert(ensemble.realisations == 32)
        assert(ensemble.meanSurvivors in 0.0..6.0)
    }

    @Test
    fun `gate 1 - the run-length pitch arithmetic is a length over a length and refuses a bad one`() {
        assertFailsWith<IllegalArgumentException> { columnsForRunRobustness(0.0, 12.0, 0) }
        assertFailsWith<IllegalArgumentException> { columnsForRunRobustness(40.0, 0.0, 0) }
        assertFailsWith<IllegalArgumentException> { columnsForRunRobustness(40.0, 12.0, -1) }
        assertFailsWith<IllegalArgumentException> { longestAbsenceRun(listOf(true, false), 0) }
        assertFailsWith<IllegalArgumentException> { longestAbsenceRun(listOf(true, false, true), 2) }
    }

    @Test
    fun `gate 1 - a removal profile carries one dishing per path and refuses a mismatched design`() {
        assertFailsWith<IllegalArgumentException> {
            singlePathRemovalDishing(surrogate45, List(3) { 1.0 })
        }
        val profile = singlePathRemovalDishing(surrogate45, equal45)
        assert(profile.size == grid.size)
        assert(profile.all { it > 0.0 && it.isFinite() })
    }

    @Test
    fun `gate 1 - a rank correlation is dimensionless and refuses unpaired samples`() {
        assertFailsWith<IllegalArgumentException> {
            spearmanRankCorrelation(listOf(1.0, 2.0), listOf(1.0))
        }
        assertFailsWith<IllegalArgumentException> {
            spearmanRankCorrelation(listOf(1.0), listOf(1.0))
        }
        assert(
            spearmanRankCorrelation(listOf(1.0, 2.0, 3.0), listOf(10.0, 20.0, 30.0))
                .isCloseTo(1.0, 1e-12)
        )
        assert(
            spearmanRankCorrelation(listOf(1.0, 2.0, 3.0), listOf(30.0, 20.0, 10.0))
                .isCloseTo(-1.0, 1e-12)
        )
    }

    // ------------------------------------------------------------------ gate 2: limiting cases

    @Test
    fun `gate 2 - an ensemble at unit incorporation keeps every path and reproduces the nominal`() {
        val ensemble = dropoutEnsemble(List(grid.size) { 1.0 }, 16, 11L)
        assert(ensemble.meanSurvivors == grid.size.toDouble())
        val sample = dropoutDishingSample(surrogate45, equal45, ensemble)
        val nominal = surrogate45.solve(equal45).peakDishing
        assert(sample.all { it.isCloseTo(nominal, 1e-12) })
    }

    @Test
    fun `gate 2 - an ensemble at zero incorporation returns the free tile at every realisation`() {
        val ensemble = dropoutEnsemble(List(grid.size) { 0.0 }, 8, 13L)
        assert(ensemble.meanSurvivors == 0.0)
        val free = lattice().solve(solvedField).peakDishing(81)
        val sample = dropoutDishingSample(surrogate45, equal45, ensemble)
        assert(sample.all { it.isCloseTo(free, 1e-12) })
    }

    @Test
    fun `gate 2 - the oracle floor at full presence is the standing reachable floor`() {
        val full = surrogate45.reachableDishingFloor
        val viaSubset = surrogate45.reachableDishingFloorAt(List(grid.size) { true })
        assert(viaSubset.isCloseTo(full, 1e-8))
    }

    @Test
    fun `gate 2 - the oracle floor at no presence is the free field's own root mean square`() {
        val none = surrogate45.reachableDishingFloorAt(List(grid.size) { false })
        val freeRms = surrogate45.solveWithDropout(equal45, List(grid.size) { false }).rmsDishing
        assert(none.isCloseTo(freeRms, 1e-12))
    }

    @Test
    fun `gate 2 - removing a station can only raise the oracle floor`() {
        val full = surrogate45.reachableDishingFloorAt(List(grid.size) { true })
        (0 until grid.size step 7).forEach { absent ->
            val reduced = surrogate45.reachableDishingFloorAt(grid.indices.map { it != absent })
            assert(reduced >= full * (1.0 - 1e-9))
        }
    }

    @Test
    fun `gate 2 - the worst single removal is the maximum of the removal profile`() {
        val profile = singlePathRemovalDishing(surrogate45, equal45)
        assert(worstSinglePathRemoval(surrogate45, equal45).isCloseTo(profile.max(), 1e-12))
    }

    @Test
    fun `gate 2 - a run robustness of zero losses is the bare pitch requirement`() {
        val bendingLength = 12.0
        assert(
            columnsForRunRobustness(40.0, bendingLength, 0) ==
                    ceil(40.0 / bendingLength).toInt()
        )
        assert(
            columnsForRunRobustness(40.0, bendingLength, 1) ==
                    ceil(2.0 * 40.0 / bendingLength).toInt()
        )
    }

    // ------------------------------------------------------------ gate 3: symmetry, conservation

    @Test
    fun `gate 3 - F3 - the uncoupled tile under a uniform load dishes exactly zero`() {
        val uniform = latticeInfluenceSurrogate(
            lattice(), grid, uniformPressure(interiorPressure), 81
        )
        val none = uniform.solveWithDropout(equal45, List(grid.size) { false })
        assert(abs(none.peakDishing) < 1e-9)
        assert(abs(uniform.reachableDishingFloorAt(List(grid.size) { false })) < 1e-9)
    }

    @Test
    fun `gate 3 - F5 - the oracle floor bounds every realisation's peak dishing from below`() {
        val ensemble = dropoutEnsemble(List(grid.size) { 0.8 }, 40, 20260817L)
        val peaks = dropoutDishingSample(surrogate45, equal45, ensemble)
        val floors = oracleFloorSample(surrogate45, ensemble)
        assert(peaks.size == floors.size)
        // Not a tautology: the floor is a least-squares RMS over ALL force vectors and the peak
        // is the maximum of one particular field, so nothing in the assembly forces the order.
        peaks.indices.forEach { assert(floors[it] <= peaks[it] * (1.0 + 1e-9)) }
    }

    @Test
    fun `gate 3 - the ensemble is bit reproducible from its seed and differs between seeds`() {
        val probabilities = List(20) { 0.8 }
        val a = dropoutEnsemble(probabilities, 50, 20260817L)
        val b = dropoutEnsemble(probabilities, 50, 20260817L)
        val c = dropoutEnsemble(probabilities, 50, 20260818L)
        (0 until 50).forEach { assert(a.presenceAt(it) == b.presenceAt(it)) }
        assert((0 until 50).any { a.presenceAt(it) != c.presenceAt(it) })
    }

    @Test
    fun `gate 3 - the ensemble draws the same stream C-0087's own sampler draws`() {
        val probabilities = List(12) { 0.7 }
        val random = DropoutRandom(20260817L)
        val expected = (1..5).map { bernoulliPresence(probabilities, random) }
        val ensemble = dropoutEnsemble(probabilities, 5, 20260817L)
        expected.indices.forEach { assert(ensemble.presenceAt(it) == expected[it]) }
    }

    @Test
    fun `gate 3 - the longest absence run is counted within a row and never across rows`() {
        // Two rows of four: the first row ends absent and the second begins absent, which is a
        // run of one at each end and not a run of three.
        val present = listOf(true, true, false, false, false, true, true, true)
        assert(longestAbsenceRun(present, 4) == 2)
        assert(longestAbsenceRun(List(8) { false }, 4) == 4)
        assert(longestAbsenceRun(List(8) { true }, 4) == 0)
    }

    @Test
    fun `gate 3 - the percentile summary is monotone and lands on the sample`() {
        val ensemble = dropoutEnsemble(List(grid.size) { 0.8 }, 200, 20260817L)
        val sample = dropoutDishingSample(surrogate45, equal45, ensemble)
        val summary = summariseDropoutDishing(sample, 0.2182, ensemble.meanSurvivors, 0.10)
        assert(summary.median <= summary.p90)
        assert(summary.p90 <= summary.p95)
        assert(summary.p95 <= summary.worst)
        assert(sample.any { it == summary.p90 })
        assert(summary.exceedance in 0.0..1.0)
    }

    // ------------------------------------------------------------------ gate 4: convergence

    @Test
    fun `gate 4 - the ninetieth percentile settles as the ensemble grows`() {
        val counts = listOf(250, 500, 1000, 2000)
        val values = counts.map {
            val ensemble = dropoutEnsemble(List(grid.size) { 0.8 }, it, 20260817L)
            orderStatistic(dropoutDishingSample(surrogate45, equal45, ensemble), 0.90)
        }
        // The ensembles are NESTED — a 250-realisation bank is the prefix of a 500-realisation
        // one drawn from the same seed — and a percentile of a nested sample still need not
        // move monotonically, because the rank the order statistic reads moves with the sample
        // size. So the settling is asserted as a *magnitude*, not as a monotone sequence.
        val relativeStep = abs(values[3] - values[2]) / values[3]
        assert(relativeStep < 0.01)
        assert((values.max() - values.min()) / values.max() < 0.05)
    }

    @Test
    fun `gate 4 - the oracle floor at a coarser dishing grid tracks the standing one`() {
        val coarse = latticeInfluenceSurrogate(lattice(), grid, solvedField, 41)
        val ensemble = dropoutEnsemble(List(grid.size) { 0.8 }, 40, 20260817L)
        val fine = oracleFloorSample(surrogate45, ensemble).average()
        val rough = oracleFloorSample(coarse, ensemble).average()
        assert(rough.isCloseTo(fine, 5e-2))
    }

    // ------------------------------------------------------------- gate 5: upstream cross-check

    @Test
    fun `gate 5 - F4 - the zero-dropout limit reproduces C-0017's 0_2182 and C-0058's 0_0753`() {
        assert((surrogate45.solve(equal45).peakDishing / freeStroke).isCloseTo(0.2182, 1e-3))
        val twoLevel = normalisedStiffnesses(
            rimStiffenedWeights(grid, edgeX, edgeY, 6.7, 5.0), mandate
        )
        assert((surrogate45.solve(twoLevel).peakDishing / freeStroke).isCloseTo(0.0753, 1e-3))
    }

    @Test
    fun `gate 5 - F4 - the cheap bound reproduces C-0087's single-removal 0_3060 on the two-level design`() {
        val twoLevel = normalisedStiffnesses(
            rimStiffenedWeights(grid, edgeX, edgeY, 6.7, 5.0), mandate
        )
        val worst = worstSinglePathRemoval(surrogate45, twoLevel) / freeStroke
        assert(worst.isCloseTo(0.3060, 2e-3))
    }

    @Test
    fun `gate 5 - C-0026's free-tile stroke is reproduced rather than transcribed`() {
        assert(freeStroke.isCloseTo(4.90731, 1e-5))
    }

    @Test
    fun `gate 5 - the Winkler bending length along the helices reproduces C-0047's 12_83 nm`() {
        val along = winklerBendingLength(
            sheet.alongHelixRigidity, Gen1Tile.FOUNDATION_SECANT
        )
        assert(along.isCloseTo(12.83, 2e-2))
        // The density a single missing neighbour demands, from CLAUDE.md's own sign rule.
        assert(columnsForRunRobustness(edgeX, along, 1) == 7)
    }
}
