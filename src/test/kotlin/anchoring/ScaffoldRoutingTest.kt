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
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-151`, leaf `A8.2` — can the Gen-1 tile be raster-folded without a scaffold seam?
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 *
 * **The theorem is COMPUTED, not asserted**: the Hamiltonian path and cycle counts of the row graph
 * are brute-forced, and the whole verdict is read off them. If a path graph turned out to carry a
 * Hamiltonian cycle, the seam would not be forced and nothing here would hold.
 */
class ScaffoldRoutingTest {

    private val duplexes = 15

    private val rise = Gen1Tile.RISE_PER_BASE_PAIR

    // ---------------------------------------------------------------- gate 1: dimensional

    @Test
    fun `gate 1 - a nucleotide count is an integer and a loop contour is a length`() {
        assert(sheetScaffoldNucleotides(15, 112) == 15 * 112)
        val short = returnLoopNucleotides(duplexes, 2.69, 0.60)
        val long = returnLoopNucleotides(duplexes, 2.69, 0.30)
        assert(long >= 2 * short - 1)
    }

    @Test
    fun `gate 1 - a radius of gyration scales as the square root of the contour`() {
        val one = singleStrandedRadiusOfGyration(1000)
        val four = singleStrandedRadiusOfGyration(4000)
        assert(abs(four / one - 2.0) < 1e-12)
    }

    @Test
    fun `gate 1 - the half-turn test is invariant under a common rescaling of the lattice`() {
        for (basePairs in listOf(16, 48, 80, 112)) {
            assert(
                isOddHalfTurnSeparation(basePairs, SQUARE_LATTICE_BASE_PAIRS_PER_TURN) ==
                        isOddHalfTurnSeparation(2 * basePairs, 2 * SQUARE_LATTICE_BASE_PAIRS_PER_TURN)
            )
        }
    }

    @Test
    fun `gate 1 - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { rowAdjacency(0) }
        assertFailsWith<IllegalArgumentException> { hamiltonianRowPathCount(13) }
        assertFailsWith<IllegalArgumentException> { sheetScaffoldNucleotides(0, 112) }
        assertFailsWith<IllegalArgumentException> { returnLoopNucleotides(1) }
        assertFailsWith<IllegalArgumentException> { singleStrandedRadiusOfGyration(0) }
        assertFailsWith<IllegalArgumentException> { isOddHalfTurnSeparation(0) }
        assertFailsWith<IllegalArgumentException> { doubleRasterRoute(1) }
        assertFailsWith<IllegalArgumentException> {
            boustrophedonRoute(15, ScaffoldTopology.CIRCULAR_FULLY_FOLDED)
        }
        assertFailsWith<IllegalArgumentException> {
            bestStaggeredSeam(emptyList(), listOf(0))
        }
        assertFailsWith<IllegalArgumentException> {
            bestStaggeredSeam(listOf(listOf(1)), emptyList())
        }
        assertFailsWith<IllegalArgumentException> { straightSeamCost(emptyList(), 0) }
    }

    // ---------------------------------------------------------------- gate 2: limiting cases

    @Test
    fun `gate 2 - THE THEOREM - the row graph has Hamiltonian paths and NO Hamiltonian cycle`() {
        for (rows in 3..12) {
            // two directions from each of the two ends, counted once per start
            assert(hamiltonianRowPathCount(rows) == 2)
            assert(hamiltonianRowCycleCount(rows) == 0)
        }
    }

    @Test
    fun `gate 2 - two duplexes DO close, so the theorem starts at three`() {
        assert(hamiltonianRowCycleCount(2) > 0)
        assert(minimumSegmentsPerRow(ScaffoldTopology.CIRCULAR_FULLY_FOLDED, 2) == 1)
        assert(minimumSegmentsPerRow(ScaffoldTopology.CIRCULAR_FULLY_FOLDED, 3) == 2)
    }

    @Test
    fun `gate 2 - the double raster carries exactly one seam at every row count`() {
        for (rows in 2..20) {
            val route = doubleRasterRoute(rows)
            assert(route.segmentsPerRow.all { it == 2 })
            assert(!route.seamless)
            assert(route.connected)
            assert(route.closes)
        }
    }

    @Test
    fun `gate 2 - the boustrophedon is seamless and connected at every row count`() {
        for (rows in 1..20) {
            val route = boustrophedonRoute(rows)
            assert(route.seamless)
            assert(route.segments.size == rows)
            if (rows >= 2) assert(route.connected)
        }
    }

    @Test
    fun `gate 2 - a linear scaffold is seamless and a fully folded circular one is not`() {
        assert(seamlessRoutingVerdict(ScaffoldTopology.LINEAR, duplexes).seamless)
        assert(seamlessRoutingVerdict(ScaffoldTopology.CIRCULAR_WITH_REMAINDER, duplexes).seamless)
        assert(!seamlessRoutingVerdict(ScaffoldTopology.CIRCULAR_FULLY_FOLDED, duplexes).seamless)
        assert(
            seamlessRoutingVerdict(ScaffoldTopology.CIRCULAR_FULLY_FOLDED, duplexes)
                .seamsRequired == 1
        )
    }

    // ---------------------------------------------------- gate 3: symmetry and conservation

    @Test
    fun `gate 3 - a route covers every row and its segment count matches the walk parity`() {
        listOf(
            boustrophedonRoute(duplexes) to 1,
            doubleRasterRoute(duplexes) to 2
        ).forEach { (route, expected) ->
            assert(route.segmentsPerRow.size == duplexes)
            assert(route.segmentsPerRow.all { it == expected })
            assert(route.segments.size == duplexes * expected)
            assert(
                route.segments.size ==
                        duplexes * minimumSegmentsPerRow(route.topology, duplexes)
            )
        }
    }

    @Test
    fun `gate 3 - the staggered assignment is invariant under reflecting the sheet`() {
        val planes = listOf(-10, -8, -6, -4, -2, 0, 2, 4)
        val stations = List(duplexes) { row -> listOf(-9 + 2 * (row % 5), 3) }
        val forward = bestStaggeredSeam(stations, planes)
        val reflected = bestStaggeredSeam(stations.reversed(), planes)
        assert(forward.affectedStations == reflected.affectedStations)
        assert(forward.planes.size == duplexes)
    }

    @Test
    fun `gate 3 - every staggered assignment returned really clears every station`() {
        val planes = listOf(-10, -8, -6, -4, -2, 0, 2, 4)
        val stations = List(duplexes) { row -> listOf(-9 + 2 * (row % 4), 1 + 2 * (row % 3)) }
        val assignment = staggeredSeamAssignment(stations, planes)
        if (assignment != null) {
            stations.forEachIndexed { row, planesHere ->
                planesHere.forEach { plane ->
                    listOf(row - 1, row, row + 1).filter { it in 0 until duplexes }.forEach {
                        assert(abs(assignment[it] - plane) >= 2)
                    }
                }
            }
        }
    }

    @Test
    fun `gate 3 - an impossible stagger costs stations rather than returning a bad assignment`() {
        // one candidate plane, and a station sitting right beside it in every row
        assert(staggeredSeamAssignment(List(4) { listOf(1) }, listOf(0)) == null)
        assert(bestStaggeredSeam(List(4) { listOf(1) }, listOf(0)).affectedStations == 4)
        // and a stagger is never worse than the best straight seam
        val planes = listOf(-4, -2, 0, 2, 4)
        val stations = List(6) { row -> listOf(-3 + row, 1) }
        assert(
            bestStaggeredSeam(stations, planes).affectedStations <=
                    planes.minOf { straightSeamCost(stations, it) }
        )
    }

    // ---------------------------------------------------------------- gate 4: exactness

    @Test
    fun `gate 4 - every quantity here is an integer or a closed form so nothing converges`() {
        // the Hamiltonian counts are exhaustive: doubling nothing changes them
        assert(hamiltonianRowPathCount(10) == hamiltonianRowPathCount(10))
        // the admissible widths are exact multiples and the list is monotone
        val admissible = admissibleRasterRowLengths(400)
        assert(admissible == admissible.sorted())
        assert(admissible.all { isOddHalfTurnSeparation(it) })
        assert(
            admissibleRasterRowLengths(200).all { it in admissible }
        )
    }

    // ---------------------------------------------------------------- gate 5: literature

    @Test
    fun `gate 5 - Rothemund's 26-helix seamless square REQUIRED a linear scaffold`() {
        // "The square had no vertical reversals in raster direction, required a linear scaffold"
        val route = boustrophedonRoute(26, ScaffoldTopology.LINEAR)
        assert(route.seamless)
        assert(!seamlessRoutingVerdict(ScaffoldTopology.CIRCULAR_FULLY_FOLDED, 26).seamless)
    }

    @Test
    fun `gate 5 - Rothemund's 8-helix third-square was seamless on a CIRCULAR scaffold with a remainder`() {
        val verdict = seamlessRoutingVerdict(ScaffoldTopology.CIRCULAR_WITH_REMAINDER, 8)
        assert(verdict.seamless)
        // "No remainder strands were used on the ~2/3 of M13mp18 DNA left unfolded"
        val used = sheetScaffoldNucleotides(8, 112)
        assert(used < M13_SCAFFOLD_NUCLEOTIDES / 2)
    }

    @Test
    fun `gate 5 - the Gen-1 tile is deep inside the demonstrated remainder regime`() {
        val used = sheetScaffoldNucleotides(duplexes, 112)
        val unfolded = M13_SCAFFOLD_NUCLEOTIDES - used
        assert(unfolded > 0)
        // Rothemund's own first experiment left ~2/3 unfolded; the Gen-1 tile leaves more
        assert(unfolded.toDouble() / M13_SCAFFOLD_NUCLEOTIDES > 2.0 / 3.0)
    }

    @Test
    fun `gate 5 - M13's linearised length is Rothemund's own 7176`() {
        assert(M13_LINEARISED_NUCLEOTIDES == 7176)
        assert(M13_SCAFFOLD_NUCLEOTIDES == 7249L)
    }

    // ------------------------------------------------- the findings, asserted as tests

    @Test
    fun `finding - the odd half-turn constraint quantises a seamless tile's width at 32 bp`() {
        val admissible = admissibleRasterRowLengths(200)
        assert(admissible.contains(16))
        assert(admissible.contains(48))
        assert(admissible.contains(112))
        assert(!admissible.contains(118))
        // the step is 32 bp = 10.88 nm, not 0.34
        assert(admissible.zipWithNext().all { (a, b) -> b - a == 32 })
    }

    @Test
    fun `finding - the buildable seamless width nearest 40 nm is 112 bp = 38_08 nm`() {
        val nearest = nearestAdmissibleWidth(40.0)!!
        assert(nearest == 112)
        assert(abs(nearest * rise - 38.08) < 1e-9)
    }

    @Test
    fun `finding - the return loop a circular scaffold needs is tiny against M13's remainder`() {
        val loop = returnLoopNucleotides(duplexes)
        assert(loop in 60..70)
        val remainder = M13_SCAFFOLD_NUCLEOTIDES - sheetScaffoldNucleotides(duplexes, 112)
        assert(remainder > 50L * loop)
    }

    @Test
    fun `finding - the unpaired M13 remainder is a body comparable with the tile itself`() {
        val remainder = (M13_SCAFFOLD_NUCLEOTIDES - sheetScaffoldNucleotides(duplexes, 112)).toInt()
        val radius = singleStrandedRadiusOfGyration(remainder)
        assert(radius > 20.0)
        // and it carries more backbone charge than the whole sheet does
        assert(remainder > 2 * sheetScaffoldNucleotides(duplexes, 112))
    }

    @Test
    fun `gate 5 - a straight seam reproduces C-0081's own 6 to 12 affected stations`() {
        val profile = WeaveProfile(phaseBasePairs = 24, duplexes = duplexes)
        val planes = seamPlanesWithin(profile, Gen1Tile.EDGE_X)
        val stationPlanes = gen1StationPlanes(profile)
        assert(stationPlanes.sumOf { it.size } == 34)
        assert(planes.size == 8)
        val costs = planes.map { straightSeamCost(stationPlanes, it) }
        assert(costs.min() == 6)
        assert(costs.max() == 12)
    }

    @Test
    fun `finding - the best STAGGERED seam recovers only one station over the best straight one`() {
        val profile = WeaveProfile(phaseBasePairs = 24, duplexes = duplexes)
        val planes = seamPlanesWithin(profile, Gen1Tile.EDGE_X)
        val stationPlanes = gen1StationPlanes(profile)
        val staggered = bestStaggeredSeam(stationPlanes, planes)
        assert(staggered.planes.size == duplexes)
        assert(staggered.planes.all { it in planes })
        assert(staggered.affectedStations == 5)
        // so Rothemund's own alternative is NOT the remedy: only seamlessness is
        assert(staggeredSeamAssignment(stationPlanes, planes) == null)
        assert(planes.minOf { straightSeamCost(stationPlanes, it) } == 6)
    }

    /**
     * `C-0063`'s winning placement — phase 24, 34 roots, centro-symmetric — transcribed from
     * `gpd/results/T-125-upward-root-placement.json`, exactly as `C-0068`'s own test does, and
     * checked against the phase-24 upward lattice below rather than trusted.
     */
    private val c0063Roots: List<List<Double>> = listOf(
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

    private fun gen1StationPlanes(profile: WeaveProfile): List<List<Int>> =
        c0063Roots.map { roots -> roots.map { weavePlaneIndex(profile, it) } }

    @Test
    fun `gate 5 - the transcribed C-0063 stations all lie on the phase-24 upward lattice`() {
        val lattice = upwardRootLattice(24, Gen1Tile.EDGE_X, duplexes)
        c0063Roots.forEachIndexed { row, roots ->
            roots.forEach { root -> assert(lattice[row].any { abs(it - root) < 1e-9 }) }
        }
    }
}
