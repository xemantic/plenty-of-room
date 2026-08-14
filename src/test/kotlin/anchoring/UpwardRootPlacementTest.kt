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
import com.xemantic.nano.plentyofroom.coupling.couplingSupports
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_LENGTH
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PointSupport
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-125`, leaf `A8.2` — the row phases of `C-0055`'s upward arm array, swept.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem
 * definition. The two free strong falsifiers `T-125` declared are here as tests: **the
 * Woodbury surrogate must reproduce an assembled `OrigamiGrillage` solve at the same
 * stations**, and **a uniform load on a uniform Winkler foundation must dish exactly zero**,
 * which is what makes the whole sweep a statement about `C-0022`'s solved load rather than
 * about a load case that cannot dish anything.
 *
 * The third anchor is `C-0061`'s own **0.4156** on `C-0055`'s placement: the only published
 * number on these stations, and the one that would say the pipeline had moved under the task.
 */
class UpwardRootPlacementTest {

    private val sheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )

    private val duplexes = 15

    private val edgeX = Gen1Tile.EDGE_X

    private val lengthY = duplexes * Gen1Tile.INTERHELICAL_SHEET

    private val arm = C0055_ARM_LENGTH

    private val mandate = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

    /** `C-0022`'s solved edge profile at 2 mM, a 10 nm gap and 0.192 V — its own numbers. */
    private val solvedLoad = edgeCollarPressure(
        Gen1Tile.TARGET_FORCE / (edgeX * lengthY), edgeX, lengthY,
        listOf(CollarTerm(-0.302930, 8.939), CollarTerm(-0.593880, 1.0))
    )

    private val uniformLoad = uniformPressure(Gen1Tile.TARGET_FORCE / (edgeX * lengthY))

    private fun lattice(
        supports: List<PointSupport> = emptyList(),
        columns: CrossoverLayout = CrossoverLayout.centred(8, sheet.crossoverSpacing / 2.0),
        subdivisions: Int = 2
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
    ).solve(uniformLoad).meanDeflection

    // ------------------------------------------------------------------ gate 1 — dimensional

    @Test
    fun `gate 1 - the upward root pitch is exactly 32 base pairs and adjacent rows are offset by 16`() {
        val rows = upwardRootLattice(0, edgeX, duplexes)
        assert(rows.size == duplexes)
        rows.forEach { row ->
            row.zipWithNext().forEach { (a, b) ->
                assert((b - a).isCloseTo(32.0 * Gen1Tile.RISE_PER_BASE_PAIR, relativeTolerance = 1e-12))
            }
        }
        // adjacent rows: the same lattice shifted by exactly 16 bp, which is half the pitch
        val offset = rows[1][0] - rows[0][0]
        val pitch = 32.0 * Gen1Tile.RISE_PER_BASE_PAIR
        val reduced = offset - pitch * Math.round(offset / pitch)
        assert(abs(abs(reduced) - 16.0 * Gen1Tile.RISE_PER_BASE_PAIR) < 1e-12)
    }

    @Test
    fun `gate 1 - every root lies inside the footprint and every station carries its own row's y`() {
        val placement = greedyUpwardPlacement(0, edgeX, duplexes, arm, 34)
        placement.rows.forEach { row ->
            row.roots.forEach { assert(abs(it) <= edgeX / 2.0) }
        }
        val stations = placement.stations(duplexes)
        assert(stations.size == 34)
        stations.zip(placement.rows.flatMap { row -> row.roots.map { row.row } })
            .forEach { (station, row) ->
                assert(
                    station.second.isCloseTo(
                        (row - (duplexes - 1) / 2.0) * Gen1Tile.INTERHELICAL_SHEET,
                        relativeTolerance = 1e-12
                    ) || abs(station.second) < 1e-12
                )
            }
    }

    @Test
    fun `gate 1 - unphysical arguments are refused rather than absorbed`() {
        assertFailsWith<IllegalArgumentException> { upwardRootLattice(0, -1.0, duplexes) }
        assertFailsWith<IllegalArgumentException> { upwardRootLattice(0, edgeX, 1) }
        assertFailsWith<IllegalArgumentException> { armDirections(listOf(0.0), -1.0, edgeX) }
        assertFailsWith<IllegalArgumentException> {
            rowRootOptions(listOf(0.0, 10.88), 0, arm, edgeX)
        }
        assertFailsWith<IllegalArgumentException> {
            UpwardArmRow(0, listOf(0.0, -10.88), listOf(true, true))
        }
        assertFailsWith<IllegalArgumentException> { UpwardArmRow(0, listOf(0.0), listOf(true, true)) }
        assertFailsWith<IllegalArgumentException> { UpwardArmRow(-1, listOf(0.0), listOf(true)) }
        assertFailsWith<IllegalArgumentException> {
            UpwardArmPlacement(0, listOf(UpwardArmRow(0, listOf(0.0), listOf(true)))).stations(0)
        }
    }

    // ------------------------------------------------------------------ gate 2 — limiting cases

    @Test
    fun `gate 2 - a row admits exactly the arm count C-0053's own exact scheduler places`() {
        (0 until 32).forEach { phase ->
            val sites = upwardRootLattice(phase, edgeX, duplexes)
            sites.forEachIndexed { row, xs ->
                val scheduled = maximumArmsInRow(
                    xs.map { HingeSite(row, it, 0) }, arm, edgeX, OrigamiDuplex.INTERHELICAL, row
                ).size
                val enumerated = (1..xs.size).last { rowRootOptions(xs, it, arm, edgeX).isNotEmpty() }
                assert(enumerated == scheduled)
            }
        }
    }

    @Test
    fun `gate 2 - an arm array too long for its row admits no direction assignment at all`() {
        assert(armDirections(listOf(-10.88, 0.0, 10.88), arm, edgeX) != null)
        assert(armDirections(listOf(-10.88, 0.0, 10.88), 12.0, edgeX) == null)
        // a single arm at the very edge can still point inward
        assert(armDirections(listOf(19.04), arm, edgeX) == listOf(false))
    }

    @Test
    fun `gate 2 - a uniform load on a uniform foundation dishes exactly zero on the free tile`() {
        val dishing = lattice().solve(uniformLoad).peakDishing()
        assert(abs(dishing) < 1e-9)
    }

    @Test
    fun `gate 2 - the free tile dishes C-0022's own 0_308 of the stroke under the solved load`() {
        val dishing = lattice().solve(solvedLoad).peakDishing() / freeStroke
        assert(dishing.isCloseTo(0.3079, relativeTolerance = 2e-3))
    }

    // ------------------------------------------------------------- gate 3 — symmetry, conservation

    @Test
    fun `gate 3 - the Woodbury surrogate reproduces an assembled lattice solve at the same stations`() {
        val placement = greedyUpwardPlacement(0, edgeX, duplexes, arm, 34)
        val stations = placement.stations(duplexes)
        val bank = UpwardRootInfluenceBank(lattice(), stations, solvedLoad)
        val surrogate = bank.surrogateFor(stations.indices.toList())
        val reduced = surrogate.solve(List(stations.size) { mandate / stations.size }).peakDishing
        val assembled = lattice(couplingSupports(stations, mandate)).solve(solvedLoad).peakDishing()
        assert(reduced.isCloseTo(assembled, relativeTolerance = 1e-9))
    }

    @Test
    fun `gate 3 - the influence matrix is symmetric, which is Maxwell-Betti and is not imposed`() {
        val placement = greedyUpwardPlacement(0, edgeX, duplexes, arm, 34)
        val stations = placement.stations(duplexes)
        val bank = UpwardRootInfluenceBank(lattice(), stations, solvedLoad)
        assert(bank.surrogateFor(stations.indices.toList()).reciprocityResidual < 1e-9)
    }

    /**
     * The load case here is the **uniform** one, not `C-0022`'s solved profile: the applied
     * force of a collar profile is an integral of a cosine taper over a 1 nm rim, and the
     * quadrature that assembles the load vector is not the quadrature that reports
     * `appliedForce`, so the two agree only to their own integration error. Conservation is a
     * property of the assembly, and the uniform load is where it can be read exactly.
     */
    @Test
    fun `gate 3 - the support forces and the foundation carry the whole applied load`() {
        val placement = greedyUpwardPlacement(0, edgeX, duplexes, arm, 34)
        val supports = couplingSupports(placement.stations(duplexes), mandate)
        val solution = lattice(supports).solve(uniformLoad)
        val carried = solution.supportForces.sum() + solution.foundationForce
        assert(carried.isCloseTo(solution.appliedForce, relativeTolerance = 1e-8))
        assert(solution.appliedForce.isCloseTo(Gen1Tile.TARGET_FORCE, relativeTolerance = 1e-10))
    }

    @Test
    fun `gate 3 - the phase period is 32 base pairs and not 16`() {
        val base = upwardRootLattice(3, edgeX, duplexes)
        val half = upwardRootLattice(3 + 16, edgeX, duplexes)
        val full = upwardRootLattice(3 + 32, edgeX, duplexes)
        assert(base.zip(full).all { (a, b) -> a.size == b.size && a.zip(b).all { (p, q) -> abs(p - q) < 1e-9 } })
        assert(base.zip(half).any { (a, b) -> a.size != b.size || a.zip(b).any { (p, q) -> abs(p - q) > 1e-9 } })
    }

    @Test
    fun `gate 3 - exactly two of the 32 phases can supply a centro-symmetric placement`() {
        val phases = centroSymmetricUpwardPhases(edgeX, duplexes)
        assert(phases == listOf(8, 24))
        phases.forEach { phase ->
            val placement = centroSymmetricPlacements(phase, edgeX, duplexes, arm, 34).first()
            assert(placement.count == 34)
            assert(placement.isCentroSymmetric(duplexes))
            assert(abs(placement.centroidX) < 1e-9)
        }
        // and at a phase the congruence excludes, C-0055's own, no placement can be symmetric
        assert(!greedyUpwardPlacement(0, edgeX, duplexes, arm, 34).isCentroSymmetric(duplexes))
    }

    @Test
    fun `gate 3 - C-0061's mirrored roots are NOT upward sites - they are the downward azimuth`() {
        val placement = greedyUpwardPlacement(0, edgeX, duplexes, arm, 34)
        val sites = upwardRootLattice(0, edgeX, duplexes)
        val mirroredOffLattice = placement.rows.filter { it.row % 2 == 1 }.sumOf { row ->
            row.roots.count { root -> sites[row.row].none { abs(it + root) < 1e-9 } }
        }
        // every reflected root of an odd row lands on that row's WEST (downward) azimuth
        assert(mirroredOffLattice == placement.rows.filter { it.row % 2 == 1 }.sumOf { it.count })
        val downward = junctionSites(0, edgeX, duplexes)
            .filter { it.azimuth == CrossoverAzimuth.WEST }
        placement.rows.filter { it.row % 2 == 1 }.forEach { row ->
            row.roots.forEach { root ->
                assert(downward.any { it.duplex == row.row && abs(it.x + root) < 1e-9 })
            }
        }
    }

    // ------------------------------------------------------- gate 4 — convergence and determinism

    @Test
    fun `gate 4 - the descent is deterministic and never worsens its own objective`() {
        val sites = upwardRootLattice(8, edgeX, duplexes)
        val start = greedyUpwardPlacement(8, edgeX, duplexes, arm, 34)
        val objective: (UpwardArmPlacement) -> Double = { abs(it.centroidX) }
        val first = descendPlacement(start, sites, arm, edgeX, objective = objective)
        val second = descendPlacement(start, sites, arm, edgeX, objective = objective)
        assert(first.placement.key == second.placement.key)
        assert(first.objective <= objective(start))
        // at a centro-symmetric phase the centroid can be driven to zero exactly
        assert(abs(first.objective) < 1e-9)
        assert(first.placement.count == 34)
    }

    @Test
    fun `gate 4 - the surrogate is independent of which stations the bank was built over`() {
        val placement = greedyUpwardPlacement(0, edgeX, duplexes, arm, 34)
        val stations = placement.stations(duplexes)
        val everySite = upwardRootLattice(0, edgeX, duplexes).flatMapIndexed { row, xs ->
            xs.map { it to (row - (duplexes - 1) / 2.0) * Gen1Tile.INTERHELICAL_SHEET }
        }
        val wide = UpwardRootInfluenceBank(lattice(), everySite, solvedLoad)
        val indices = stations.map { station ->
            everySite.indexOfFirst {
                abs(it.first - station.first) < 1e-9 && abs(it.second - station.second) < 1e-9
            }
        }
        assert(indices.none { it < 0 })
        val sliced = wide.surrogateFor(indices).solve(List(34) { mandate / 34 }).peakDishing
        val narrow = UpwardRootInfluenceBank(lattice(), stations, solvedLoad)
            .surrogateFor(stations.indices.toList())
            .solve(List(34) { mandate / 34 }).peakDishing
        assert(sliced.isCloseTo(narrow, relativeTolerance = 1e-12))
    }

    // ------------------------------------------------------------------ gate 5 — upstream

    @Test
    fun `gate 5 - C-0055's own placement is reproduced from the lattice, 34 arms on 15 rows`() {
        val placement = greedyUpwardPlacement(0, edgeX, duplexes, arm, 34)
        assert(placement.count == 34)
        assert(placement.rows.size == duplexes)
        assert(placement.rows.all { it.count >= 2 })
        assert(placement.rows.count { it.count == 3 } == 4)
        // C-0055's published roots for row 0 and row 1, read from its own result file
        assert(placement.rows[0].roots.zip(listOf(-13.6, -2.72, 8.16))
            .all { (a, b) -> abs(a - b) < 1e-9 })
        assert(placement.rows[1].roots.zip(listOf(-19.04, -8.16, 2.72))
            .all { (a, b) -> abs(a - b) < 1e-9 })
        assert(placement.centroidX.isCloseTo(-8.80, relativeTolerance = 2e-2))
    }

    @Test
    fun `gate 5 - C-0061's 0_4156 reproduces on C-0055's stations at its own configuration`() {
        val placement = greedyUpwardPlacement(0, edgeX, duplexes, arm, 34)
        val supports = couplingSupports(placement.stations(duplexes), mandate)
        val dishing = lattice(supports).solve(solvedLoad).peakDishing() / freeStroke
        assert(dishing.isCloseTo(0.4156, relativeTolerance = 3e-3))
    }

    @Test
    fun `gate 5 - the count arithmetic admits exactly four rows of three and eleven of two`() {
        assert(rowsCarryingThreeArms(34, duplexes, 3) == 4)
        assert(rowsCarryingThreeArms(45, duplexes, 3) == 15)
        assertFailsWith<IllegalArgumentException> { rowsCarryingThreeArms(46, duplexes, 3) }
    }
}
