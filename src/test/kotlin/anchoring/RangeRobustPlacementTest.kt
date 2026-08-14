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
import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.coupling.couplingSupports
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.coupling.minimaxStiffnessDistribution
import com.xemantic.nano.plentyofroom.coupling.multiStateSurrogate
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PointSupport
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-129`, leaf `A8.2` — is `C-0063`'s flat placement flat over the range a device **traverses**?
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 *
 * The three free strong falsifiers this task declared are here as tests:
 * **a uniform load on a uniform Winkler foundation must dish exactly zero**,
 * **the sliced multi-state bank must equal a surrogate built over that subset alone and an
 * assembled `OrigamiGrillage` solve at the same stations**,
 * and **`C-0063`'s own 0.0706 must reproduce** — the only published number on these 34 stations,
 * and the one that would say the pipeline had moved under the task.
 */
class RangeRobustPlacementTest {

    private val sheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )

    private val duplexes = 15

    private val edgeX = Gen1Tile.EDGE_X

    private val lengthY = duplexes * Gen1Tile.INTERHELICAL_SHEET

    private val interiorPressure = Gen1Tile.TARGET_FORCE / (edgeX * lengthY)

    private val mandate = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

    /**
     * Two of `C-0022`'s solved states, transcribed from `gpd/results/T-3b-*.json` so the test does
     * not depend on a file it does not own — the **rest** and **held** ends of `C-0018`'s placed
     * device: 2 mM, `L₀` = 10 nm, 0.192 V, gaps 10 → 7 nm.
     */
    private val restState = LoadState(
        "2 mM, 10 nm, 0.192 V",
        edgeCollarPressure(
            interiorPressure, edgeX, lengthY,
            listOf(CollarTerm(-0.302887367, 8.93928311), CollarTerm(-0.593889278, 1.0))
        )
    )

    private val heldState = LoadState(
        "2 mM, 7 nm, 0.192 V",
        edgeCollarPressure(
            interiorPressure, edgeX, lengthY,
            listOf(CollarTerm(-0.307225808, 7.63667082), CollarTerm(0.0149184449, 1.0))
        )
    )

    private val uniformState = LoadState("uniform", uniformPressure(interiorPressure))

    private fun lattice(
        supports: List<PointSupport> = emptyList(),
        columns: CrossoverLayout = CrossoverLayout.centred(8, sheet.crossoverSpacing / 2.0),
        subdivisions: Int = 1
    ) = OrigamiGrillage(
        sheet = sheet,
        lengthX = edgeX,
        beamCount = duplexes,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = columns,
        subdivisions = subdivisions,
        supports = supports
    )

    /** `C-0006`'s free-tile stroke, which every dishing in this programme is quoted over. */
    private val freeStroke = PlateOnFoundation(
        sheet.plate(edgeX, lengthY), Gen1Tile.FOUNDATION_SECANT, emptyList(), basisDegree = 12
    ).solve(uniformPressure(interiorPressure)).meanDeflection

    /** A cheap station set for the algebraic gates — deliberately NOT the 34 roots. */
    private val stations = attachmentGrid(3, 5, edgeX, lengthY)

    private val bank by lazy {
        MultiStateRootBank(
            lattice(), stations, listOf(restState, heldState, uniformState)
        )
    }

    private val all = stations.indices.toList()

    private fun equal(count: Int) = List(count) { mandate / count }

    /**
     * `C-0063`'s winning placement — phase 24, 34 roots, centro-symmetric — transcribed from
     * `gpd/results/T-125-upward-root-placement.json`. **CITED**, and checked against the phase-24
     * upward lattice by the gate-5 test below rather than trusted.
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

    private fun c0063Stations(): List<Pair<Double, Double>> = c0063Roots.flatMap { (row, roots) ->
        val y = (row - (duplexes - 1) / 2.0) * sheet.interhelicalDistance
        roots.map { it to y }
    }

    // ------------------------------------------------------------------ gate 1 — dimensional

    @Test
    fun `gate 1 — a bank refuses a supported host, empty stations, an unknown station and a state out of range`() {
        assertFailsWith<IllegalArgumentException> {
            MultiStateRootBank(
                lattice(couplingSupports(stations, mandate)), stations, listOf(restState)
            )
        }
        assertFailsWith<IllegalArgumentException> {
            MultiStateRootBank(lattice(), emptyList(), listOf(restState))
        }
        assertFailsWith<IllegalArgumentException> {
            MultiStateRootBank(lattice(), stations, emptyList())
        }
        assertFailsWith<IllegalArgumentException> { bank.surrogateFor(emptyList()) }
        assertFailsWith<IllegalArgumentException> { bank.surrogateFor(listOf(0, 0)) }
        assertFailsWith<IllegalArgumentException> { bank.surrogateFor(listOf(stations.size)) }
        assertFailsWith<IllegalArgumentException> { bank.freePeakDishing(bank.stateNames.size) }
        assert(bank.indexOf(1e6, 1e6) == -1)
    }

    @Test
    fun `gate 1 — the stroke a state demands is L0 minus the gap, and a 10 nm device cannot occupy 2 nm`() {
        assert(strokeToOccupy(10.0, 7.0).isCloseTo(3.0))
        assert(strokeToOccupy(10.0, 2.0).isCloseTo(8.0))
        assert(strokeToOccupy(5.0, 2.0).isCloseTo(3.0))
        // C-0050's dead-load stroke at §3's 100 pN, 10 nm layer at sigma = 0.024 nm^-2, the
        // largest of its six layer models: 6.01348358 nm. Eight nanometres is outside it and
        // three is not — which is what makes the exclusion of the 2 nm state PHYSICAL.
        assert(!gapOccupiable(10.0, 2.0, 6.01348358))
        assert(gapOccupiable(10.0, 7.0, 6.01348358))
        assert(gapOccupiable(5.0, 2.0, 6.01348358))
        assertFailsWith<IllegalArgumentException> { strokeToOccupy(10.0, 12.0) }
        assertFailsWith<IllegalArgumentException> { strokeToOccupy(-1.0, 0.5) }
        assertFailsWith<IllegalArgumentException> { gapOccupiable(10.0, 7.0, -1.0) }
    }

    @Test
    fun `gate 1 — the worst dishing over a range is exactly linear in the applied pressure`() {
        val doubled = MultiStateRootBank(
            lattice(), stations,
            listOf(
                LoadState(restState.name, doubled(restState)),
                LoadState(heldState.name, doubled(heldState))
            )
        )
        val single = bank.surrogateFor(all).worstDishing(equal(all.size), listOf(0, 1))
        val twice = doubled.surrogateFor(all).worstDishing(equal(all.size), listOf(0, 1))
        assert(twice.isCloseTo(2.0 * single, 1e-10))
    }

    private fun doubled(state: LoadState) =
        PressureField { x, y -> 2.0 * state.pressure.at(x, y) }

    // ------------------------------------------------------------------ gate 2 — limiting cases

    @Test
    fun `gate 2 — a uniform load on a uniform Winkler foundation dishes exactly zero on the free tile`() {
        assert(bank.freePeakDishing(2) < 1e-9)
        assert(bank.freePeakDishing(0) > 1e-3)
    }

    @Test
    fun `gate 2 — the worst over a subset never exceeds the worst over a superset`() {
        val surrogate = bank.surrogateFor(all)
        val stiffnesses = equal(all.size)
        val peaks = surrogate.peakDishing(stiffnesses)
        assert(surrogate.worstDishing(stiffnesses, listOf(0)).isCloseTo(peaks[0]))
        assert(surrogate.worstDishing(stiffnesses, listOf(1)).isCloseTo(peaks[1]))
        val range = surrogate.worstDishing(stiffnesses, listOf(0, 1))
        assert(range >= surrogate.worstDishing(stiffnesses, listOf(0)) - 1e-15)
        assert(range.isCloseTo(maxOf(peaks[0], peaks[1])))
    }

    @Test
    fun `gate 2 — the range minimax is a descent and conserves the mandate`() {
        val surrogate = bank.surrogateFor(all)
        val start = equal(all.size)
        val optimum = minimaxStiffnessDistribution(
            surrogate = surrogate,
            states = listOf(0, 1),
            totalStiffness = mandate,
            starts = listOf(start),
            smoothingLevels = listOf(0.1, 0.01),
            iterationsPerLevel = 4,
            polishSweeps = 1
        )
        assert(optimum.worstDishing <= surrogate.worstDishing(start, listOf(0, 1)) * (1.0 + 1e-9))
        assert(optimum.stiffnesses.sum().isCloseTo(mandate, 1e-12))
        assert(optimum.stiffnesses.all { it > 0.0 })
        assert(optimum.bindingStates.isNotEmpty())
    }

    @Test
    fun `gate 2 — the per-state least-squares floor never exceeds what any distribution reaches`() {
        val surrogate = bank.surrogateFor(all)
        val peaks = surrogate.peakDishing(equal(all.size))
        (0..1).forEach { state ->
            assert(surrogate.reachableDishingFloor(state) <= peaks[state])
        }
    }

    // --------------------------------------------------------- gate 3 — symmetry and conservation

    @Test
    fun `gate 3 — Maxwell-Betti reciprocity of the sliced station influence matrix`() {
        assert(bank.surrogateFor(all).reciprocityResidual < 1e-9)
        assert(bank.surrogateFor(listOf(0, 4, 9)).reciprocityResidual < 1e-9)
    }

    @Test
    fun `gate 3 — a point-reflected station set dishes identically at every state`() {
        val chosen = listOf(-14.0 to -8.07, -3.0 to 0.0, 9.0 to 5.38, 14.0 to 13.45)
        val reflected = chosen.map { (x, y) -> -x to -y }
        val here = MultiStateRootBank(lattice(), chosen, listOf(restState, heldState))
        val there = MultiStateRootBank(lattice(), reflected, listOf(restState, heldState))
        val stiffnesses = equal(chosen.size)
        val mine = here.surrogateFor(chosen.indices.toList()).peakDishing(stiffnesses)
        val theirs = there.surrogateFor(chosen.indices.toList()).peakDishing(stiffnesses)
        mine.zip(theirs).forEach { (a, b) -> assert(a.isCloseTo(b, 1e-9)) }
    }

    // ------------------------------------------------------------------ gate 4 — convergence

    @Test
    fun `gate 4 — a sliced bank equals a surrogate built over that subset alone`() {
        val indices = listOf(1, 3, 7, 8, 12)
        val sliced = bank.surrogateFor(indices)
        val direct = multiStateSurrogate(
            lattice(), indices.map { stations[it] },
            listOf(restState, heldState, uniformState)
        )
        val stiffnesses = equal(indices.size)
        sliced.peakDishing(stiffnesses).zip(direct.peakDishing(stiffnesses))
            .forEach { (a, b) -> assert(a.isCloseTo(b, 1e-12)) }
        sliced.supportForces(stiffnesses, 0).zip(direct.supportForces(stiffnesses, 0))
            .forEach { (a, b) -> assert(a.isCloseTo(b, 1e-12)) }
        assert(sliced.reachableDishingFloor(0).isCloseTo(direct.reachableDishingFloor(0), 1e-10))
        assert(sliced.freeFieldCosine(0, 1).isCloseTo(direct.freeFieldCosine(0, 1), 1e-12))
    }

    @Test
    fun `gate 4 — the equal-spring range reading equals an assembled grillage solve at the same stations`() {
        val indices = listOf(0, 2, 5, 9, 11, 14)
        val chosen = indices.map { stations[it] }
        val stiffnesses = equal(indices.size)
        val surrogate = bank.surrogateFor(indices)
        val assembled = listOf(restState, heldState).map { state ->
            lattice(couplingSupports(chosen, mandate)).solve(state.pressure).peakDishing()
        }
        surrogate.peakDishing(stiffnesses).take(2).zip(assembled)
            .forEach { (a, b) -> assert(a.isCloseTo(b, 1e-9)) }
        assert(surrogate.worstDishing(stiffnesses, listOf(0, 1)).isCloseTo(assembled.max(), 1e-9))
    }

    // ------------------------------------------------------------------ gate 5 — upstream

    @Test
    fun `gate 5 — C-0063's 34 roots are on the phase-24 upward lattice and are centro-symmetric`() {
        val sites = upwardRootLattice(24, edgeX, duplexes)
        assert(c0063Roots.sumOf { it.second.size } == 34)
        c0063Roots.forEach { (row, roots) ->
            roots.forEach { root ->
                assert(sites[row].any { abs(it - root) < 1e-9 })
            }
        }
        val byRow = c0063Roots.toMap()
        (0 until duplexes).forEach { row ->
            val mine = byRow.getValue(row).sorted()
            val partner = byRow.getValue(duplexes - 1 - row).map { -it }.sorted()
            assert(mine.size == partner.size)
            mine.zip(partner).forEach { (a, b) -> assert(abs(a - b) < 1e-9) }
        }
    }

    @Test
    fun `gate 5 — C-0063's 0_0706 reproduces at its own state on its own host`() {
        val chosen = c0063Stations()
        val host = lattice(
            columns = CrossoverLayout.atBasePairPhase(24, sheet, edgeX), subdivisions = 2
        )
        val roots = MultiStateRootBank(host, chosen, listOf(restState))
        val dishing = roots.surrogateFor(chosen.indices.toList())
            .worstDishing(equal(chosen.size), listOf(0)) / freeStroke
        assert(dishing.isCloseTo(0.0706145537, 1e-3))
        assert(dishing < 0.10)
        assert(freeStroke.isCloseTo(4.90731102, 1e-6))
    }

}
