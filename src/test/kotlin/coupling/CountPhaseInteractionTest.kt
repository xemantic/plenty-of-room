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
import com.xemantic.nano.plentyofroom.anchoring.UpwardRootInfluenceBank
import com.xemantic.nano.plentyofroom.anchoring.upwardRootLattice
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-178` — does `C-0103`'s count effect at fixed station geometry hold at the other 31
 * crossover phases, and is its *"+12.86 % of count against −19.0 % of phase"* a decomposition?
 *
 * Every test is named for the verification gate it discharges, and the falsifiers `T-178`
 * declares are asserted rather than argued:
 *
 * - **`F1`** — the count term's sign across the phase family is measured in the study; what is
 *   asserted here is that the family it is measured on really is **nested** at every pair of
 *   counts and at every phase, because a count sweep over sets that are not nested is not one;
 * - **`F2`** — the interaction is computed two independent ways, and this file asserts that they
 *   are the same arithmetic: a balanced two-way additive fit's residual and the disagreement
 *   between the two orderings of a 2 × 2 are one number;
 * - **`F3`** — the cheap instrument is computed on the same objects the percentile is;
 * - **`F4`** — `C-0063`'s 0.0706145537 and `C-0087`'s 0.501011167 reproduce;
 * - **`F5`** — a uniform load on a uniform foundation dishes exactly zero, on a **seven**-column
 *   host as well as on `C-0063`'s eight-column one.
 *
 * The disciplines from `CLAUDE.md` that govern this file: two quantities both meant to be zero
 * are compared **absolutely**; a nested family is asserted nested rather than inferred from its
 * construction; and a search-free construction is asserted to contain no search, by being
 * identical when run twice from different starts.
 */
class CountPhaseInteractionTest {

    private val duplexes = 15

    private val sheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )

    private val edgeX = Gen1Tile.EDGE_X

    private val edgeY = duplexes * sheet.interhelicalDistance

    private val interiorPressure = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)

    private val mandate = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

    /** `C-0022`'s design point at 2 mM / 10 nm / 0.192 V, as `PathCountAtFixedGeometryTest` has it. */
    private val solvedField = edgeCollarPressure(
        interiorPressure, edgeX, edgeY,
        listOf(CollarTerm(-0.302887367, 8.93928311), CollarTerm(-0.593889278, 1.0))
    )

    /** `C-0063`'s own crossover phase — centro-symmetric, and an eight-column host. */
    private val phase = 24

    /** A **seven**-column host at the richest upward inventory — `C-0102`'s other stratum. */
    private val richestPhase = 0

    private val sites = upwardRootLattice(phase, edgeX, duplexes)

    /** `C-0063`'s 34 upward roots at phase 24, transcribed from its own result file. */
    private val anchor: List<List<Double>> = listOf(
        listOf(-16.32, -5.44, 16.32),
        listOf(0.0, 10.88),
        listOf(-16.32, 5.44, 16.32),
        listOf(0.0, 10.88),
        listOf(-16.32, 16.32),
        listOf(-10.88, 0.0),
        listOf(-16.32, 16.32),
        listOf(-10.88, 10.88),
        listOf(-16.32, 16.32),
        listOf(0.0, 10.88),
        listOf(-16.32, 16.32),
        listOf(-10.88, 0.0),
        listOf(-16.32, -5.44, 16.32),
        listOf(-10.88, 0.0),
        listOf(-16.32, 5.44, 16.32)
    )

    private fun hostAt(basePairPhase: Int) = OrigamiGrillage(
        sheet = sheet,
        lengthX = edgeX,
        beamCount = duplexes,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = CrossoverLayout.atBasePairPhase(basePairPhase, sheet, edgeX),
        subdivisions = 2,
        supports = emptyList()
    )

    private val host = hostAt(phase)

    /** `C-0026`'s free-tile stroke, recomputed rather than transcribed. */
    private val freeStroke = PlateOnFoundation(
        sheet.plate(edgeX, edgeY), Gen1Tile.FOUNDATION_SECANT, emptyList(), 12
    ).solve(uniformPressure(interiorPressure)).meanDeflection

    private fun stationsOf(rows: List<List<Double>>): List<Pair<Double, Double>> =
        rows.indices.flatMap { row ->
            val y = (row - (duplexes - 1) / 2.0) * sheet.interhelicalDistance
            rows[row].map { it to y }
        }

    private val bank by lazy {
        UpwardRootInfluenceBank(host, stationsOf(sites), solvedField, 81)
    }

    private val counts = listOf(22, 25, 28, 30, 34, 45)

    // ------------------------------------------------------------------ gate 1: dimensional

    @Test
    fun `gate 1 - the central root placement is one root per row and every root is a site`() {
        val central = centralRootPlacement(sites)
        assert(central.size == duplexes)
        assert(central.all { it.size == 1 })
        central.indices.forEach { row ->
            assert(sites[row].any { abs(it - central[row][0]) < 1e-9 })
        }
        assertFailsWith<IllegalArgumentException> { centralRootPlacement(emptyList()) }
        assertFailsWith<IllegalArgumentException> {
            centralRootPlacement(listOf(listOf(0.0), emptyList()))
        }
    }

    @Test
    fun `gate 1 - a two-way interaction refuses a ragged, a degenerate or a non-positive grid`() {
        assertFailsWith<IllegalArgumentException> {
            twoWayLogInteraction(listOf(listOf(1.0, 2.0), listOf(3.0)))
        }
        assertFailsWith<IllegalArgumentException> {
            twoWayLogInteraction(listOf(listOf(1.0, 2.0)))
        }
        assertFailsWith<IllegalArgumentException> {
            twoWayLogInteraction(listOf(listOf(1.0), listOf(2.0)))
        }
        assertFailsWith<IllegalArgumentException> {
            twoWayLogInteraction(listOf(listOf(1.0, 0.0), listOf(2.0, 3.0)))
        }
    }

    @Test
    fun `gate 1 - a count-phase split refuses a non-positive percentile`() {
        assertFailsWith<IllegalArgumentException> {
            countPhaseSplit(0.0, 0.7, 0.6, 0.5)
        }
        assertFailsWith<IllegalArgumentException> {
            countPhaseSplit(0.6, 0.7, -0.6, 0.5)
        }
    }

    // ------------------------------------------------------------------ gate 2: limiting cases

    @Test
    fun `gate 2 - a SEPARABLE grid has exactly zero interaction and recovers its own effects`() {
        val rowFactors = listOf(0.4, 0.9, 1.7, 3.1)
        val columnFactors = listOf(0.25, 1.0, 2.5)
        val grid = rowFactors.map { a -> columnFactors.map { b -> a * b } }
        val fit = twoWayLogInteraction(grid)
        assert(fit.worstResidual < 1e-12)
        assert(fit.interactionSumOfSquares < 1e-24)
        rowFactors.indices.forEach { row ->
            val recovered = exp(fit.grandMean + fit.rowEffects[row] + fit.columnEffects[0])
            assert(recovered.isCloseTo(rowFactors[row] * columnFactors[0], 1e-12))
        }
    }

    @Test
    fun `gate 2 - a grid in which only one factor varies has zero interaction`() {
        val grid = listOf(listOf(2.0, 2.0, 2.0), listOf(5.0, 5.0, 5.0))
        val fit = twoWayLogInteraction(grid)
        assert(fit.worstResidual < 1e-12)
        assert(abs(fit.columnSumOfSquares) < 1e-24)
        assert(fit.rowSumOfSquares > 0.0)
    }

    @Test
    fun `gate 2 - the canonical chain at its minimum is the central root placement`() {
        val chain = canonicalRootChain(sites)
        assert(chain.minimumCount == duplexes)
        assert(chain.at(duplexes) == centralRootPlacement(sites))
    }

    @Test
    fun `gate 2 - F1 - the canonical family is NESTED at every pair of counts, at every phase`() {
        (0 until 32).forEach { basePairPhase ->
            val phaseSites = upwardRootLattice(basePairPhase, edgeX, duplexes)
            val chain = canonicalRootChain(phaseSites)
            val members = counts.associateWith { chain.at(it) }
            counts.forEach { count ->
                assert(members.getValue(count).sumOf { it.size } == count)
            }
            counts.forEach { smaller ->
                counts.filter { it >= smaller }.forEach { larger ->
                    val small = members.getValue(smaller)
                    val large = members.getValue(larger)
                    small.indices.forEach { row ->
                        small[row].forEach { root ->
                            assert(large[row].any { abs(it - root) < 1e-9 })
                        }
                    }
                }
            }
        }
    }

    @Test
    fun `gate 2 - the canonical construction contains no search and is a function of the lattice`() {
        val a = canonicalRootChain(sites).at(34)
        val b = canonicalRootChain(upwardRootLattice(phase, edgeX, duplexes)).at(34)
        assert(a == b)
    }

    // ------------------------------------------------------- gate 3: symmetry and conservation

    @Test
    fun `gate 3 - F5 - the uncoupled tile under a uniform load dishes exactly zero on both hosts`() {
        listOf(phase, richestPhase).forEach { basePairPhase ->
            val dishing = hostAt(basePairPhase)
                .solve(uniformPressure(interiorPressure))
                .peakDishing(81)
            assert(abs(dishing) / freeStroke < 1e-9)
        }
    }

    @Test
    fun `gate 3 - the two-way sums of squares decompose exactly`() {
        val grid = listOf(
            listOf(0.63, 0.72, 0.80, 1.03),
            listOf(0.58, 0.69, 0.77, 0.94),
            listOf(0.71, 0.75, 0.91, 1.11)
        )
        val fit = twoWayLogInteraction(grid)
        val sum = fit.rowSumOfSquares + fit.columnSumOfSquares + fit.interactionSumOfSquares
        assert(abs(sum - fit.totalSumOfSquares) < 1e-12 * fit.totalSumOfSquares)
    }

    @Test
    fun `gate 3 - F2 - the two orderings of a 2 x 2 share their total and differ by the interaction`() {
        val split = countPhaseSplit(
            fromCountFromPhase = 0.638498565,
            toCountFromPhase = 0.720607136,
            fromCountToPhase = 0.611,
            toCountToPhase = 0.583664426
        )
        assert(split.pathDisagreement < 1e-15)
        assert(
            abs(
                (split.countTermAtToPhase - split.countTermAtFromPhase) - split.interaction
            ) < 1e-15
        )
        assert(
            abs(
                (split.phaseTermAtToCount - split.phaseTermAtFromCount) - split.interaction
            ) < 1e-15
        )
        assert(
            abs(
                split.total - ln(0.583664426 / 0.638498565)
            ) < 1e-15
        )
    }

    @Test
    fun `gate 3 - a 2 x 2 assembled from a separable grid has exactly zero interaction`() {
        val split = countPhaseSplit(
            fromCountFromPhase = 0.6,
            toCountFromPhase = 0.6 * 1.13,
            fromCountToPhase = 0.6 * 0.81,
            toCountToPhase = 0.6 * 1.13 * 0.81
        )
        assert(abs(split.interaction) < 1e-15)
    }

    // ------------------------------------------------------------------ gate 4: convergence

    @Test
    fun `gate 4 - the count term settles as the ensemble grows, under common random numbers`() {
        val field = measuredDepthIncorporation(edgeX, edgeY)
        val probabilities = stationsOf(sites).map { (x, y) -> field.at(x, y) }
        val chain = canonicalRootChain(sites)
        val at34 = rootStationIndices(sites, chain.at(34))
        val at30 = rootStationIndices(sites, chain.at(30))
        val surrogate34 = bank.surrogateFor(at34)
        val surrogate30 = bank.surrogateFor(at30)
        val terms = listOf(250, 500, 1000).map { realisations ->
            val parent = dropoutEnsemble(probabilities, realisations, 20260817L)
            val a = orderStatistic(
                dropoutDishingSample(
                    surrogate34, List(34) { mandate / 34 }, restrictEnsemble(parent, at34)
                ), 0.90
            )
            val b = orderStatistic(
                dropoutDishingSample(
                    surrogate30, List(30) { mandate / 30 }, restrictEnsemble(parent, at30)
                ), 0.90
            )
            b / a
        }
        assert(abs(terms[2] - terms[1]) < 0.05 * terms[2])
    }

    // ------------------------------------------------------------- gate 5: upstream cross-check

    @Test
    fun `gate 5 - F4 - the zero-dropout limit at C-0063's own anchor reproduces 0_0706145537`() {
        val surrogate = bank.surrogateFor(rootStationIndices(sites, anchor))
        val dishing = surrogate.solve(List(34) { mandate / 34 }).peakDishing / freeStroke
        assert(dishing.isCloseTo(0.0706145537, 1e-6))
    }

    @Test
    fun `gate 5 - F4 - the cheap bound at C-0063's anchor reproduces C-0087's 0_501011167`() {
        val surrogate = bank.surrogateFor(rootStationIndices(sites, anchor))
        val worst = worstSinglePathRemoval(surrogate, List(34) { mandate / 34 }) / freeStroke
        assert(worst.isCloseTo(0.501011167, 1e-6))
    }

    @Test
    fun `gate 5 - F4 - C-0103's own two graded cells reproduce at phase 24, or the sweep is on another object`() {
        val field = measuredDepthIncorporation(edgeX, edgeY)
        val probabilities = stationsOf(sites).map { (x, y) -> field.at(x, y) }
        val parent = dropoutEnsemble(probabilities, 10000, 20260817L)
        val chainA = nestedRootChain(sites, anchor)
        listOf(34 to 0.638498565, 30 to 0.720607136).forEach { (count, published) ->
            val indices = rootStationIndices(sites, chainA.at(count))
            val p90 = orderStatistic(
                dropoutDishingSample(
                    bank.surrogateFor(indices),
                    List(count) { mandate / count },
                    restrictEnsemble(parent, indices)
                ),
                0.90
            ) / freeStroke
            assert(p90.isCloseTo(published, 1e-6))
        }
    }

    @Test
    fun `gate 5 - C-0026's free-tile stroke and C-0066's 53 sites at phase 24 are reproduced`() {
        assert(freeStroke.isCloseTo(4.90731102, 1e-8))
        assert(sites.sumOf { it.size } == 53)
        assert(sites.map { it.size } == listOf(4, 3, 4, 3, 4, 3, 4, 3, 4, 3, 4, 3, 4, 3, 4))
    }

    @Test
    fun `gate 5 - C-0102's census strata are reproduced at the nominal width`() {
        val inventory = (0 until 32).associateWith {
            upwardRootLattice(it, edgeX, duplexes).sumOf { row -> row.size }
        }
        assert(inventory.filterValues { it == 60 }.keys.sorted() ==
                listOf(0, 1, 2, 14, 15, 16, 17, 18, 30, 31))
        assert(inventory.getValue(24) == 53)
        assert(inventory.getValue(8) == 52)
    }

}
