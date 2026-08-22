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

package com.xemantic.nano.plentyofroom.design

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.anchoring.SsDnaTether
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.honeycombXRasterPath
import java.io.File
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-274` — does the recommended `10 × 6` honeycomb block need a scaffold **seam**?
 *
 * `CLAUDE.md` states the theorem with **two** premises: the graph the scaffold may use is a
 * **tree** (so every edge is a bridge and a closed walk crosses each an even number of times),
 * **and** the scaffold is a **fully folded circular** strand. `C-0119` §4 asserts the first for a
 * honeycomb block and does not examine the second; `C-0154` has since shown a honeycomb block's
 * interfaces are not a path graph, so the first must be re-derived on the block's own adjacency.
 *
 * Everything here is integer arithmetic on the corpus's own `HoneycombCell` lattice, plus one
 * Gaussian-chain closure price over `CLAUDE.md`'s 2× ssDNA Kuhn bracket.
 */
class ScaffoldSeamParityTest {

    private val rows = 10
    private val perRow = 6
    private val path = honeycombXRasterPath(rows, perRow)
    private val surface = rasterSurfaceScaffoldGraph(path)
    private val induced = inducedLatticeScaffoldGraph(path)

    // --- G1: the surface reading is a path, and the seam is what a closed walk on it costs -----

    @Test
    fun `the raster-surface graph is a path on sixty vertices`() {
        assert(surface.order == 60)
        assert(surface.size == 59)
        assert(surface.isTree)
        assert(surface.bridges.size == 59)
        assert(surface.leaves.size == 2)
        assert(surface.degrees.max() == 2)
    }

    @Test
    fun `a fully folded circular scaffold on the surface graph needs 118 domains`() {
        // every edge of a tree is a bridge, so a closed walk crosses each at least twice
        assert(surface.minimumClosedCoveringWalk == 2 * (surface.order - 1))
        assert(surface.minimumClosedCoveringWalk == 118)
    }

    // --- G2: the block's own lattice adjacency is NOT a tree ------------------------------------

    @Test
    fun `the induced lattice adjacency of the block carries seventy-seven edges`() {
        assert(induced.order == 60)
        assert(induced.size == 77)
        assert(!induced.isTree)
        assert(induced.cyclomaticNumber == 18)
        assert(induced.degrees.max() == 3)
    }

    @Test
    fun `the raster path is a subgraph of the induced lattice adjacency`() {
        val inducedEdges = induced.edges.map { setOf(it.first, it.second) }.toSet()
        assert(surface.edges.all { setOf(it.first, it.second) in inducedEdges })
    }

    // --- G3: the two raster termini are the only degree-one vertices ----------------------------

    @Test
    fun `the only degree-one vertices are the two raster termini`() {
        assert(induced.leaves == listOf(0, induced.order - 1))
        assert(induced.bridges.size == 2)
    }

    @Test
    fun `the induced adjacency is a path if and only if the row carries two helices`() {
        (2..6).forEach { m ->
            listOf(2, 4, 6).forEach { n ->
                val g = inducedLatticeScaffoldGraph(honeycombXRasterPath(m, n))
                assert(g.isTree == (n == 2))
            }
        }
    }

    // --- G4: therefore no Hamiltonian cycle, decided rather than searched -----------------------

    @Test
    fun `the block admits no Hamiltonian cycle`() {
        assert(induced.hasHamiltonianCycle() == false)
    }

    @Test
    fun `a hexagon admits a Hamiltonian cycle, which is the control on the search`() {
        val hexagon = ScaffoldGraph(
            cells = honeycombXRasterPath(3, 2),
            edges = (0 until 6).map { it to (it + 1) % 6 }
        )
        assert(hexagon.hasHamiltonianCycle() == true)
    }

    @Test
    fun `the search refuses beyond its declared order limit rather than running a factorial`() {
        val big = ScaffoldGraph(
            cells = honeycombXRasterPath(6, 6),
            edges = (0 until 36).map { it to (it + 1) % 36 }
        )
        assert(big.hasHamiltonianCycle(orderLimit = 12) == null)
    }

    // --- G5: the domain-count bound, and both readings exceed sixty -----------------------------

    @Test
    fun `a fully folded circular scaffold on the induced adjacency needs 62 domains`() {
        assert(induced.minimumClosedCoveringWalk == 62)
        assert(induced.minimumClosedCoveringWalk > induced.order)
        assert(surface.minimumClosedCoveringWalk > induced.order)
    }

    // --- G6/G7: the committed artifact, read back out of the file -------------------------------

    private val committed = ScadnanoDesign.fromFile(
        File("gpd/designs/gen1-block-honeycomb-10x6-102-109.sc")
    )

    @Test
    fun `the committed block carries one scaffold strand of sixty domains on sixty helices`() {
        assert(committed.helixCount == 60)
        assert(committed.strands.count { it.isScaffold } == 1)
        assert(committed.scaffold().domains.size == 60)
        assert(committed.staples().isEmpty())
        assert(committed.scaffold().domains.sumOf { it.end - it.start } == 6330)
    }

    @Test
    fun `the drawn routing is a Hamiltonian path, one domain per helix in raster order`() {
        assert(committed.scaffold().domains.map { it.helix } == (0 until 60).toList())
        assert(committed.scaffoldTurns().size == 59)
    }

    @Test
    fun `both scaffold termini sit at the same axial offset`() {
        val domains = committed.scaffold().domains
        assert(domains.first().entryOffset == 7)
        assert(domains.last().exitOffset == 7)
    }

    // --- G8: CH-0212's second reading is unavailable, and the rectangle is the control ----------

    @Test
    fun `the block carries no staple crossing, so it has no column parity sequence to read`() {
        assert(committed.crossovers().isEmpty())
        assert(committed.crossoverColumns().isEmpty())
        assertFailsWith<NoSuchElementException> { committed.crossoverPhase() }
    }

    @Test
    fun `the reference rectangle does carry one, which is what makes the block's absence a fact`() {
        val rectangle = ScadnanoDesign.fromFile(
            File("gpd/designs/third-party/scadnano-origami-rectangle-16x8.sc")
        )
        assert(rectangle.scaffold().domains.size == 31)
        assert(rectangle.crossovers().isNotEmpty())
    }

    // --- G9: the remainder closure -------------------------------------------------------------

    @Test
    fun `the two termini are fourteen bond lengths apart across the cross-section`() {
        val d = Gen1Tile.INTERHELICAL_HONEYCOMB
        val separation = rasterTerminusSeparation(path, d)
        assert(separation.isCloseTo(14.0 * d))
        assert(separation.isCloseTo(35.504))
    }

    @Test
    fun `every ssDNA convention reaches, and none costs more than one crossover column`() {
        val separation = rasterTerminusSeparation(path, Gen1Tile.INTERHELICAL_HONEYCOMB)
        SSDNA_CONVENTIONS.forEach { convention ->
            val closure = remainderClosure(separation, 919, convention)
            assert(closure.minimumNucleotidesToReach < 919)
            assert(closure.stretchFreeEnergyKbt < 8.0)
            assert(closure.extensionRatio < 0.25)
        }
    }

    @Test
    fun `a zero separation costs nothing and the price rises with the separation`() {
        val convention = SSDNA_CONVENTIONS.first()
        assert(remainderClosure(0.0, 919, convention).stretchFreeEnergyKbt == 0.0)
        val near = remainderClosure(10.0, 919, convention).stretchFreeEnergyKbt
        val far = remainderClosure(20.0, 919, convention).stretchFreeEnergyKbt
        assert(far > near)
        // Gaussian: the price is quadratic in the separation
        assert((far / near).isCloseTo(4.0))
    }

    @Test
    fun `the closure price is the corpus's own Gaussian chain, term by term`() {
        val convention = SsDnaConvention(
            "control", SsDnaTether.KUHN_LENGTH_ZERO_FORCE, SsDnaTether.CONTOUR_PER_NUCLEOTIDE
        )
        val closure = remainderClosure(35.504, 919, convention)
        assert(closure.contourLength.isCloseTo(919 * 0.65))
        assert(closure.kuhnSegments.isCloseTo(919 * 0.65 / 2.10))
        assert(closure.rootMeanSquareEndToEnd.isCloseTo(2.10 * kotlin.math.sqrt(919 * 0.65 / 2.10)))
        assert(
            closure.stretchFreeEnergyKbt.isCloseTo(
                1.5 * 35.504 * 35.504 /
                        (closure.rootMeanSquareEndToEnd * closure.rootMeanSquareEndToEnd)
            )
        )
    }

    // --- G10: the 15 x 4 control, which the corpus once recommended -----------------------------

    @Test
    fun `the 15 x 4 block's remainder is shorter and its termini further apart`() {
        val d = Gen1Tile.INTERHELICAL_HONEYCOMB
        val fifteen = rasterTerminusSeparation(honeycombXRasterPath(15, 4), d)
        val ten = rasterTerminusSeparation(path, d)
        assert(fifteen > ten)
        val convention = SSDNA_CONVENTIONS.first()
        assert(
            remainderClosure(fifteen, 529, convention).stretchFreeEnergyKbt >
                    remainderClosure(ten, 919, convention).stretchFreeEnergyKbt
        )
    }

    // --- G11: the falsifier's own threshold, so F4 is quantitative ------------------------------

    @Test
    fun `the seam would be forced below a nameable remainder, and 919 is above it`() {
        val separation = rasterTerminusSeparation(path, Gen1Tile.INTERHELICAL_HONEYCOMB)
        SSDNA_CONVENTIONS.forEach { convention ->
            val threshold = nucleotidesForClosureCost(separation, 8.0, convention)
            assert(threshold < 919)
            assert(remainderClosure(separation, threshold, convention).stretchFreeEnergyKbt < 8.0)
        }
    }

    // --- G12: guards ----------------------------------------------------------------------------

    @Test
    fun `a graph with an out-of-range edge is refused`() {
        assertFailsWith<IllegalArgumentException> {
            ScaffoldGraph(cells = honeycombXRasterPath(2, 2), edges = listOf(0 to 9))
        }
    }

    @Test
    fun `the domain bound is exact on the smallest paths, where a naive leaf bound is not`() {
        // two helices: the walk crosses the one edge twice, so 2 domains — and the naive
        // "|V| + leaves" would say 4
        val pair = ScaffoldGraph(cells = honeycombXRasterPath(1, 2), edges = listOf(0 to 1))
        assert(pair.leaves == listOf(0, 1))
        assert(pair.minimumClosedCoveringWalk == 2)
        // three in a row: 0 -> 1 -> 2 -> 1 -> 0 is 4 domains, and the middle helix carries BOTH
        // leaves with no rest of the graph to reach, which is where the naive bound says 5
        val triple = ScaffoldGraph(
            cells = honeycombXRasterPath(2, 2).take(3), edges = listOf(0 to 1, 1 to 2)
        )
        assert(triple.leaves == listOf(0, 2))
        assert(triple.minimumClosedCoveringWalk == 4)
        assert(triple.minimumClosedCoveringWalk == 2 * (triple.order - 1))
    }

    @Test
    fun `a closure with a non-positive nucleotide count is refused`() {
        assertFailsWith<IllegalArgumentException> {
            remainderClosure(10.0, 0, SSDNA_CONVENTIONS.first())
        }
    }
}
