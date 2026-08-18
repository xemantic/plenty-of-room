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
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-163` — the path-count sweep at **fixed station geometry** on the upward lattice, which is
 * what settles [`CH-0103`].
 *
 * Every test is named for the verification gate it discharges, and the falsifiers `T-163`
 * declares are asserted rather than argued:
 *
 * - **`F1`** — the count axis at fixed geometry; the monotonicity itself is measured in the
 *   study, and what is asserted here is that the family the study sweeps really is **nested**,
 *   because a count sweep over sets that are not nested is not a count sweep at all;
 * - **`F2`** — the cheap single-removal bound is computed on the same objects the percentile is,
 *   so a rank correlation between them is a statement about the bound and not about two
 *   pipelines;
 * - **`F3`** — the uncoupled tile under a **uniform** load dishes exactly zero;
 * - **`F4`** — `C-0063`'s 0.0706145537 and `C-0087`'s 0.501011167 reproduce.
 *
 * The disciplines from `CLAUDE.md` that govern this file: **common random numbers** must be
 * literally common (a restricted ensemble is the parent's own stream, realisation for
 * realisation, not a re-draw); two quantities both meant to be zero are compared **absolutely**;
 * and a nested family must be asserted nested rather than assumed from its construction.
 */
class PathCountAtFixedGeometryTest {

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

    /** `C-0063`'s own crossover phase — centro-symmetric, and an eight-column host. */
    private val phase = 24

    private val sites = upwardRootLattice(phase, edgeX, duplexes)

    /**
     * `C-0063`'s 34 upward roots at phase 24, **transcribed** from
     * `gpd/results/T-125-upward-root-placement.json` and checked against the lattice below, so a
     * transcription error is a test failure rather than a silent different design.
     */
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

    private val host = OrigamiGrillage(
        sheet = sheet,
        lengthX = edgeX,
        beamCount = duplexes,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = CrossoverLayout.atBasePairPhase(phase, sheet, edgeX),
        subdivisions = 2,
        supports = emptyList()
    )

    /** `C-0026`'s free-tile stroke, recomputed rather than transcribed. */
    private val freeStroke = PlateOnFoundation(
        sheet.plate(edgeX, edgeY), Gen1Tile.FOUNDATION_SECANT, emptyList(), 12
    ).solve(uniformPressure(interiorPressure)).meanDeflection

    private fun stationsOf(rows: List<List<Double>>): List<Pair<Double, Double>> =
        rows.indices.flatMap { row ->
            val y = (row - (duplexes - 1) / 2.0) * sheet.interhelicalDistance
            rows[row].map { it to y }
        }

    private val anchorStations = stationsOf(anchor)

    private val bank by lazy {
        UpwardRootInfluenceBank(host, stationsOf(sites), solvedField, 81)
    }

    private val anchorSurrogate by lazy {
        bank.surrogateFor(rootStationIndices(sites, anchor))
    }

    private val equal34 by lazy { List(34) { mandate / 34 } }

    // ------------------------------------------------------------------ gate 1: dimensional

    @Test
    fun `gate 1 - a restricted ensemble carries one probability per retained path and refuses anything else`() {
        val parent = dropoutEnsemble(List(8) { 0.8 }, 16, 3L)
        assertFailsWith<IllegalArgumentException> { restrictEnsemble(parent, emptyList()) }
        assertFailsWith<IllegalArgumentException> { restrictEnsemble(parent, listOf(0, 0)) }
        assertFailsWith<IllegalArgumentException> { restrictEnsemble(parent, listOf(-1)) }
        assertFailsWith<IllegalArgumentException> { restrictEnsemble(parent, listOf(8)) }
        val child = restrictEnsemble(parent, listOf(1, 3, 5))
        assert(child.pathCount == 3)
        assert(child.realisations == parent.realisations)
        assert(child.seed == parent.seed)
    }

    @Test
    fun `gate 1 - a nested chain refuses a count outside its own range and an anchor off the lattice`() {
        val chain = nestedRootChain(sites, anchor)
        assert(chain.anchorCount == 34)
        assert(chain.minimumCount == duplexes)
        assert(chain.maximumCount == 45)
        assertFailsWith<IllegalArgumentException> { chain.at(duplexes - 1) }
        assertFailsWith<IllegalArgumentException> { chain.at(46) }
        // A root that is not a site of its own row is not a placement on this lattice.
        val offLattice = anchor.toMutableList().also { it[1] = listOf(1.0, 10.88) }
        assertFailsWith<IllegalArgumentException> { nestedRootChain(sites, offLattice) }
        // A row carrying more than the maximum is not a placement either.
        val overfull = anchor.toMutableList().also { it[0] = sites[0] }
        assertFailsWith<IllegalArgumentException> { nestedRootChain(sites, overfull) }
    }

    @Test
    fun `gate 1 - the station index map is a permutation into the flattened lattice`() {
        val indices = rootStationIndices(sites, anchor)
        assert(indices.size == 34)
        assert(indices.distinct().size == 34)
        assert(indices.all { it in 0 until sites.sumOf { row -> row.size } })
        assert(indices == indices.sorted())
        val stations = stationsOf(sites)
        indices.indices.forEach {
            assert(stations[indices[it]].first.isCloseTo(anchorStations[it].first, 1e-9))
            assert(stations[indices[it]].second.isCloseTo(anchorStations[it].second, 1e-9))
        }
        assertFailsWith<IllegalArgumentException> {
            rootStationIndices(sites, anchor.toMutableList().also { it[0] = listOf(1.0) })
        }
    }

    @Test
    fun `gate 1 - an arm admissibility test is a length question and refuses a bad length`() {
        assertFailsWith<IllegalArgumentException> { rowsAdmitArm(anchor, 0.0, edgeX) }
        assertFailsWith<IllegalArgumentException> { rowsAdmitArm(anchor, 8.0, 0.0) }
        // C-0075: the 34-path arm is 8.16439018 nm and C-0063's own placement carries it.
        assert(rowsAdmitArm(anchor, 8.16439018, edgeX))
        // And an arm as long as the tile cannot be placed three to a row.
        assert(!rowsAdmitArm(anchor, 20.0, edgeX))
    }

    // ------------------------------------------------------------------ gate 2: limiting cases

    @Test
    fun `gate 2 - a chain at its own anchor count returns the anchor exactly`() {
        listOf(false, true).forEach { symmetric ->
            val chain = nestedRootChain(sites, anchor, symmetric)
            assert(chain.at(34) == anchor)
        }
    }

    @Test
    fun `gate 2 - a restriction to every index reproduces the parent stream exactly`() {
        val parent = dropoutEnsemble(List(6) { 0.7 }, 32, 20260817L)
        val child = restrictEnsemble(parent, (0 until 6).toList())
        (0 until 32).forEach { assert(child.presenceAt(it) == parent.presenceAt(it)) }
        assert(child.probabilities == parent.probabilities)
    }

    @Test
    fun `gate 2 - F1 - the swept family is NESTED at every pair of counts, on both chains`() {
        val counts = listOf(22, 25, 28, 30, 34, 45)
        listOf(false, true).forEach { symmetric ->
            val chain = nestedRootChain(sites, anchor, symmetric)
            val sets = counts.associateWith { chain.at(it).toStationSet() }
            counts.forEach { small ->
                counts.filter { it > small }.forEach { large ->
                    assert(sets.getValue(large).containsAll(sets.getValue(small)))
                }
            }
            counts.forEach { assert(sets.getValue(it).size == it) }
        }
    }

    @Test
    fun `gate 2 - the chain at 30 is C-0072's own interior-root reduction of C-0063's 34`() {
        // C-0072's rule dissolves the four rows of three, so 30 is two per row at every row.
        val thirty = nestedRootChain(sites, anchor).at(30)
        assert(thirty.all { it.size == 2 })
        assert(thirty.sumOf { it.size } == 30)
    }

    @Test
    fun `gate 2 - the sliced bank reproduces an independently assembled surrogate`() {
        val direct = latticeInfluenceSurrogate(host, anchorStations, solvedField, 81)
        val sliced = anchorSurrogate
        assert(sliced.pathCount == direct.pathCount)
        val a = direct.solve(equal34).peakDishing
        val b = sliced.solve(equal34).peakDishing
        assert(abs(a - b) < 1e-9)
    }

    @Test
    fun `gate 2 - a restricted ensemble at unit incorporation keeps every retained path`() {
        val parent = dropoutEnsemble(List(53) { 1.0 }, 8, 5L)
        val child = restrictEnsemble(parent, rootStationIndices(sites, anchor))
        assert(child.meanSurvivors == 34.0)
        val sample = dropoutDishingSample(anchorSurrogate, equal34, child)
        val nominal = anchorSurrogate.solve(equal34).peakDishing
        assert(sample.all { it.isCloseTo(nominal, 1e-12) })
    }

    // ------------------------------------------------------------ gate 3: symmetry, conservation

    @Test
    fun `gate 3 - F3 - the uncoupled tile under a uniform load dishes exactly zero`() {
        val uniform = latticeInfluenceSurrogate(
            host, anchorStations, uniformPressure(interiorPressure), 81
        )
        val none = uniform.solveWithDropout(equal34, List(34) { false })
        assert(abs(none.peakDishing) < 1e-9)
        assert(abs(uniform.reachableDishingFloorAt(List(34) { false })) < 1e-9)
    }

    @Test
    fun `gate 3 - the restriction is COMMON RANDOM NUMBERS, realisation for realisation`() {
        val parent = dropoutEnsemble(List(53) { 0.8 }, 64, 20260817L)
        val indices = rootStationIndices(sites, anchor)
        val child = restrictEnsemble(parent, indices)
        (0 until 64).forEach { realisation ->
            val expected = indices.map { parent.presenceAt(realisation)[it] }
            assert(child.presenceAt(realisation) == expected)
        }
        // And a nested pair sees the SAME outcome at every station it shares, which is the whole
        // reason a few per cent between two counts is readable at all.
        val chain = nestedRootChain(sites, anchor)
        val small = restrictEnsemble(parent, rootStationIndices(sites, chain.at(30)))
        val shared = rootStationIndices(sites, chain.at(30))
        val inLarge = indices.withIndex().filter { it.value in shared }.map { it.index }
        (0 until 64).forEach { realisation ->
            val fromLarge = inLarge.map { child.presenceAt(realisation)[it] }
            assert(small.presenceAt(realisation) == fromLarge)
        }
    }

    @Test
    fun `gate 3 - the phase 24 upward lattice is centro-symmetric, computed independently`() {
        sites.indices.forEach { row ->
            val mine = sites[row]
            val partner = sites[duplexes - 1 - row].map { -it }.sorted()
            assert(mine.size == partner.size)
            mine.indices.forEach { assert(mine[it].isCloseTo(partner[it], 1e-9)) }
        }
    }

    @Test
    fun `gate 3 - the symmetric chain is centro-symmetric wherever the parity admits it`() {
        val chain = nestedRootChain(sites, anchor, symmetric = true)
        listOf(22, 28, 30, 34, 45).forEach { count ->
            val rows = chain.at(count)
            rows.indices.forEach { row ->
                val mine = rows[row].sorted()
                val partner = rows[duplexes - 1 - row].map { -it }.sorted()
                assert(mine.size == partner.size)
                mine.indices.forEach { assert(mine[it].isCloseTo(partner[it], 1e-9)) }
            }
        }
    }

    // ------------------------------------------------------------------ gate 4: convergence

    @Test
    fun `gate 4 - the ninetieth percentile at fixed geometry settles as the ensemble grows`() {
        val indices = rootStationIndices(sites, anchor)
        val values = listOf(500, 1000, 2000).map { realisations ->
            val parent = dropoutEnsemble(List(53) { 0.8 }, realisations, 20260817L)
            orderStatistic(
                dropoutDishingSample(
                    anchorSurrogate, equal34, restrictEnsemble(parent, indices)
                ),
                0.90
            )
        }
        assert(abs(values[2] - values[1]) / values[2] < 0.05)
    }

    @Test
    fun `gate 4 - a redundancy fit over the swept counts recovers an exact power law`() {
        val exact = listOf(22, 25, 28, 30, 34, 45).map { it to 0.7 * Math.pow(it / 34.0, -0.3) }
        val fit = redundancyFit(exact, 0.10)
        assert(fit.slope.isCloseTo(-0.3, 1e-9))
        assert(fit.predictedAt(34.0).isCloseTo(0.7, 1e-9))
    }

    // ------------------------------------------------------------- gate 5: upstream cross-check

    @Test
    fun `gate 5 - F4 - the zero-dropout limit reproduces C-0063's 0_0706145537`() {
        assert(
            (anchorSurrogate.solve(equal34).peakDishing / freeStroke)
                .isCloseTo(0.0706145537, 1e-6)
        )
    }

    @Test
    fun `gate 5 - F4 - the cheap bound reproduces C-0087's single-removal 0_501011167`() {
        val worst = worstSinglePathRemoval(anchorSurrogate, equal34) / freeStroke
        assert(worst.isCloseTo(0.501011167, 1e-5))
    }

    @Test
    fun `gate 5 - C-0026's free-tile stroke and C-0047's bending length are reproduced`() {
        assert(freeStroke.isCloseTo(4.90731102, 1e-7))
        assert(
            winklerBendingLength(sheet.alongHelixRigidity, Gen1Tile.FOUNDATION_SECANT)
                .isCloseTo(12.8290845, 1e-6)
        )
    }

    @Test
    fun `gate 5 - the lattice at phase 24 carries C-0066's 53 upward sites in rows of 4 and 3`() {
        assert(sites.sumOf { it.size } == 53)
        assert(sites.map { it.size } == listOf(4, 3, 4, 3, 4, 3, 4, 3, 4, 3, 4, 3, 4, 3, 4))
    }
}

/** The `(row, x)` set of a placement — a set membership test, so nestedness is a set statement. */
private fun List<List<Double>>.toStationSet(): Set<Pair<Int, Long>> =
    indices.flatMap { row -> this[row].map { row to Math.round(it * 1.0e6) } }.toSet()
