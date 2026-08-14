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

package com.xemantic.nano.plentyofroom.structure

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.anchoring.ArmAnchorage
import com.xemantic.nano.plentyofroom.anchoring.BForm
import com.xemantic.nano.plentyofroom.anchoring.tradePoint
import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.couplingSupports
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-110` — what spending 80–100 % of the tile's crossovers on hinges does to the sheet.
 *
 * Every test is named for the verification gate it discharges. Three disciplines from
 * `CLAUDE.md` govern the numerics:
 *
 * - **a uniform load on a uniform Winkler foundation must dish exactly zero**, at *every*
 *   consumption level, because a free tile translates whatever its rigidity is — the strongest
 *   free falsifier available here and the one that would catch a broken removal;
 * - **zero consumption must reproduce `C-0009`'s lattice identically**, not approximately;
 * - **mesh monotonicity holds only on nested refinements**, so gate 4 sweeps 1 ⊂ 2 ⊂ 4.
 */
class ConsumedCrossoverSheetTest {

    private val sheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )

    private val duplexes = 15

    private val lengthY = duplexes * sheet.interhelicalDistance

    private val interiorPressure = Gen1Tile.TARGET_FORCE / (Gen1Tile.EDGE_X * lengthY)

    private fun lattice(
        consumed: Set<CrossoverSite> = emptySet(),
        supports: List<PointSupport> = emptyList(),
        subdivisions: Int = 2,
        linkStiffness: Double = OrigamiGrillage.RIGID_LINK_STIFFNESS
    ) = OrigamiGrillage(
        sheet = sheet,
        lengthX = Gen1Tile.EDGE_X,
        beamCount = duplexes,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = CrossoverLayout.centred(8, sheet.crossoverSpacing / 2.0),
        subdivisions = subdivisions,
        linkStiffness = linkStiffness,
        supports = supports,
        consumedCrossovers = consumed
    )

    private val inventory: List<CrossoverSite> = lattice().crossoverSites

    /** `C-0022`'s design point (2 mM, 10 nm, 0.192 V), transcribed as `C-0047`'s test does. */
    private val solvedField: PressureField = edgeCollarPressure(
        interiorPressure, Gen1Tile.EDGE_X, lengthY,
        listOf(CollarTerm(-0.302887367, 8.93928311), CollarTerm(-0.593889278, 1.0))
    )

    private fun supportsOf(columns: Int) = couplingSupports(
        attachmentGrid(columns, duplexes, Gen1Tile.EDGE_X, lengthY),
        Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE
    )

    // ---------------------------------------------------------------- gate 1 — dimensional

    @Test
    fun `gate 1 dimensional - the uniform-curvature rigidity is a moment and is linear in the retained count`() {
        val whole = uniformCurvatureRigidity(56, Gen1Tile.crossoverHingeStiffness(), 2.69, 1614.0)
        val half = uniformCurvatureRigidity(28, Gen1Tile.crossoverHingeStiffness(), 2.69, 1614.0)
        assert((whole / half).isCloseTo(2.0, 1e-14))
        // it is k_theta times a length: doubling the interhelical distance quadruples it
        val wide = uniformCurvatureRigidity(56, Gen1Tile.crossoverHingeStiffness(), 5.38, 1614.0)
        assert((wide / whole).isCloseTo(4.0, 1e-14))
    }

    @Test
    fun `gate 1 dimensional - the consumed fraction is a count over a count and the ceiling is a pure ratio`() {
        assert(maximumConsumedForConnectivity(56, 15) == 42)
        assert(
            maximumConsumedFractionForConnectivity(56, 15).isCloseTo(42.0 / 56.0, 1e-15)
        )
        assert(
            maximumConsumedFractionForConnectivity(49, 15).isCloseTo(35.0 / 49.0, 1e-15)
        )
    }

    @Test
    fun `gate 1 dimensional - the anisotropy is a pure ratio and reports a sentinel where it is unbounded`() {
        assert(bendingAnisotropy(85.5, 3.42).isCloseTo(25.0, 1e-12))
        assert(bendingAnisotropy(85.5, 1.71).isCloseTo(50.0, 1e-12))
        assert(bendingAnisotropy(85.5, 0.0) == ANISOTROPY_UNBOUNDED)
        assert(ANISOTROPY_UNBOUNDED < 0.0)
    }

    @Test
    fun `gate 1 dimensional - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { uniformCurvatureRigidity(-1, 1.0, 1.0, 1.0) }
        assertFailsWith<IllegalArgumentException> { uniformCurvatureRigidity(1, -1.0, 1.0, 1.0) }
        assertFailsWith<IllegalArgumentException> { uniformMomentRigidity(emptyList(), 1.0, 1.0, 1.0) }
        assertFailsWith<IllegalArgumentException> { maximumConsumedForConnectivity(56, 1) }
        assertFailsWith<IllegalArgumentException> {
            retainedSites(inventory, consumed = inventory.size + 1, pattern = ConsumptionPattern.SPREAD)
        }
        assertFailsWith<IllegalArgumentException> { sheetComponents(emptyList(), duplexes = 0) }
    }

    // ---------------------------------------------------------------- gate 2 — limiting cases

    /**
     * The free falsifier. A **free** tile on a **uniform** foundation under a **uniform** load
     * translates exactly, whatever its rigidity — so it must dish zero at *every* consumption
     * level, including the one where the sheet is in fifteen pieces. If this fails, the removal
     * is broken and nothing else in the file means anything.
     */
    @Test
    fun `gate 2 limiting - a uniform load dishes exactly zero at every consumption level`() {
        listOf(0, 14, 28, 42, 45, 56).forEach { consumed ->
            val model = lattice(
                retainedSites(inventory, consumed, ConsumptionPattern.SPREAD).let { retained ->
                    inventory.toSet() - retained
                }
            )
            assert(model.solve(uniformPressure(interiorPressure)).peakDishing() < 1e-9)
        }
    }

    @Test
    fun `gate 2 limiting - zero consumption reproduces C-0009's lattice identically`() {
        val standing = OrigamiGrillage(
            sheet = sheet,
            lengthX = Gen1Tile.EDGE_X,
            beamCount = duplexes,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            columns = CrossoverLayout.centred(8, sheet.crossoverSpacing / 2.0)
        )
        val consumedNone = lattice(emptySet())
        assert(consumedNone.crossovers.size == standing.crossovers.size)
        assert(consumedNone.crossovers.size == 56)
        val load = solvedField
        val a = standing.solve(load)
        val b = consumedNone.solve(load)
        assert(b.peakDishing().isCloseTo(a.peakDishing(), 1e-15))
        assert(b.peakCrossoverForce.isCloseTo(a.peakCrossoverForce, 1e-15))
    }

    @Test
    fun `gate 2 limiting - full consumption leaves no crossover, no rigidity and fifteen pieces`() {
        val all = inventory.toSet()
        val model = lattice(all)
        assert(model.crossovers.isEmpty())
        assert(sheetComponents(emptyList(), duplexes) == duplexes)
        assert(
            uniformCurvatureRigidity(0, Gen1Tile.crossoverHingeStiffness(), 2.69, 1614.0) == 0.0
        )
        assert(
            uniformMomentRigidity(
                List(duplexes - 1) { 0 }, Gen1Tile.crossoverHingeStiffness(), 40.0, lengthY
            ) == 0.0
        )
    }

    @Test
    fun `gate 2 limiting - the two rigidity conventions agree on a uniform lattice up to the duplex-count factor`() {
        val hinge = Gen1Tile.crossoverHingeStiffness()
        val voigt = uniformCurvatureRigidity(56, hinge, sheet.interhelicalDistance, 40.0 * lengthY)
        val reuss = uniformMomentRigidity(List(14) { 4 }, hinge, 40.0, lengthY)
        assert((reuss / voigt).isCloseTo((15.0 / 14.0) * (15.0 / 14.0), 1e-12))
    }

    @Test
    fun `gate 2 limiting - one empty interface annihilates the uniform-moment rigidity entirely`() {
        val hinge = Gen1Tile.crossoverHingeStiffness()
        val counts = MutableList(14) { 4 }
        assert(uniformMomentRigidity(counts, hinge, 40.0, lengthY) > 0.0)
        counts[7] = 0
        assert(uniformMomentRigidity(counts, hinge, 40.0, lengthY) == 0.0)
    }

    // ---------------------------------------------------------------- gate 3 — symmetry, conservation

    /**
     * The pigeonhole, from two independent routes: a union-find over the retained crossovers,
     * and the closed form `1 + (empty interfaces)` that holds because the interfaces of a sheet
     * form a **path** graph on its duplexes. Nothing in the union-find knows about the path.
     */
    @Test
    fun `gate 3 conservation - union-find and the empty-interface count agree at every consumption level`() {
        ConsumptionPattern.entries.forEach { pattern ->
            (0..inventory.size).forEach { consumed ->
                val retained = retainedSites(inventory, consumed, pattern)
                val perInterface = retainedPerInterface(retained, duplexes)
                assert(perInterface.sum() == inventory.size - consumed)
                assert(
                    sheetComponents(retained, duplexes) == 1 + perInterface.count { it == 0 }
                )
            }
        }
    }

    @Test
    fun `gate 3 conservation - the pigeonhole ceiling is exactly achieved by the spreading pattern and by no other`() {
        val ceiling = maximumConsumedForConnectivity(inventory.size, duplexes)
        assert(ceiling == 42)
        assert(sheetComponents(retainedSites(inventory, ceiling, ConsumptionPattern.SPREAD), duplexes) == 1)
        assert(sheetComponents(retainedSites(inventory, ceiling + 1, ConsumptionPattern.SPREAD), duplexes) > 1)
        // the structured patterns cannot even reach it
        assert(
            sheetComponents(
                retainedSites(inventory, ceiling, ConsumptionPattern.INTERFACE_FIRST), duplexes
            ) > 1
        )
    }

    @Test
    fun `gate 3 conservation - every one of C-0046's three surviving designs disconnects the sheet`() {
        listOf(45, 50, 56).forEach { consumed ->
            ConsumptionPattern.entries.forEach { pattern ->
                assert(sheetComponents(retainedSites(inventory, consumed, pattern), duplexes) > 1)
            }
        }
        // and the best of them leaves at least four pieces, by pigeonhole alone
        assert(
            sheetComponents(retainedSites(inventory, 45, ConsumptionPattern.SPREAD), duplexes) >= 4
        )
    }

    /**
     * Global force balance — the applied load equals what the foundation and the anchors carry —
     * at every consumption level and every pattern.
     *
     * This is the conservation law that survives severance. The *per-interface* cut identity
     * `C-0009` asserts does **not**: it integrates `k_f w` over a region whose panels straddle
     * the strip boundaries, and across a severed interface the reconstructed deflection field is
     * genuinely discontinuous, so a Gauss-Legendre panel spanning the jump misreads the
     * foundation reaction by ~0.06 pN. The diagnostic is degraded by the physics it is being
     * asked to measure, which is why the check that stands here is the global one.
     */
    @Test
    fun `gate 3 conservation - the applied load is carried in full at every consumption level`() {
        ConsumptionPattern.entries.forEach { pattern ->
            listOf(0, 20, 42, 45, 56).forEach { consumed ->
                val model = lattice(consumedSites(inventory, consumed, pattern), supportsOf(3))
                val solution = model.solve(
                    uniformPressure(interiorPressure), listOf(PointLoad(3.0, 4.0, 20.0))
                )
                val carried = solution.foundationForce + solution.supportForces.sum()
                assert(carried.isCloseTo(solution.appliedForce, 1e-9))
            }
        }
    }

    /**
     * `C-0009`'s own cut identity, unchanged where it is valid — the retained crossovers on one
     * interface carry exactly the shear crossing it — plus the statement the removal has to
     * earn: **an emptied interface transmits nothing at all**.
     */
    @Test
    fun `gate 3 conservation - an emptied interface transmits nothing and a retained one carries its cut`() {
        val intact = lattice(emptySet(), supportsOf(3)).solve(
            uniformPressure(interiorPressure), listOf(PointLoad(3.0, 4.0, 20.0))
        )
        val transmitted = intact.crossoverForces.filter { it.lowerBeam == 10 }
            .sumOf { it.verticalForce }
        assert(transmitted.isCloseTo(intact.shearAcrossInterface(10), 1e-6))

        val severed = lattice(
            consumedSites(inventory, 20, ConsumptionPattern.INTERFACE_FIRST), supportsOf(3)
        ).solve(uniformPressure(interiorPressure), listOf(PointLoad(3.0, 4.0, 20.0)))
        val counts = retainedPerInterface(
            retainedSites(inventory, 20, ConsumptionPattern.INTERFACE_FIRST), duplexes
        )
        assert(counts.count { it == 0 } == 5)
        counts.indices.filter { counts[it] == 0 }.forEach { interfaceIndex ->
            assert(
                severed.crossoverForces.none { it.lowerBeam == interfaceIndex }
            )
        }
    }

    @Test
    fun `gate 3 conservation - the imposed-curvature hinge energy is exactly linear in the retained count`() {
        val curvature = 1e-3
        val full = lattice(emptySet())
        val half = lattice(inventory.toSet() - retainedSites(inventory, 28, ConsumptionPattern.SPREAD))
        val energyFull = full.hingeEnergy(full.curvatureFieldAcrossHelices(curvature))
        val energyHalf = half.hingeEnergy(half.curvatureFieldAcrossHelices(curvature))
        assert((energyFull / energyHalf).isCloseTo(2.0, 1e-12))
    }

    // ---------------------------------------------------------------- gate 4 — convergence

    @Test
    fun `gate 4 convergence - the dishing is mesh converged over the nested refinements 1 2 4`() {
        val consumed = inventory.toSet() - retainedSites(inventory, 45, ConsumptionPattern.SPREAD)
        val values = listOf(1, 2, 4).map { subdivisions ->
            lattice(consumed, supportsOf(3), subdivisions = subdivisions)
                .solve(solvedField).peakDishing()
        }
        assert(abs(values[2] - values[1]) / values[2] < 1e-3)
    }

    @Test
    fun `gate 4 convergence - the peak crossover force is converged in the link penalty`() {
        val consumed = inventory.toSet() - retainedSites(inventory, 20, ConsumptionPattern.SPREAD)
        val soft = lattice(consumed, supportsOf(3), linkStiffness = 1e4)
            .solve(solvedField).peakCrossoverForce
        val stiff = lattice(consumed, supportsOf(3), linkStiffness = 1e6)
            .solve(solvedField).peakCrossoverForce
        assert(abs(stiff - soft) / stiff < 1e-2)
    }

    // ---------------------------------------------------------------- gate 5 — upstream

    @Test
    fun `gate 5 upstream - C-0009's across-helix rigidities and anisotropy reproduce at zero consumption`() {
        val hinge = Gen1Tile.crossoverHingeStiffness()
        val continuum = hinge * sheet.interhelicalDistance / sheet.crossoverSpacing
        assert(continuum.isCloseTo(3.3452, 1e-4))
        val voigt = uniformCurvatureRigidity(56, hinge, sheet.interhelicalDistance, 40.0 * lengthY)
        assert(voigt.isCloseTo(3.397, 1e-3))
        assert((voigt / continuum).isCloseTo(1.015467, 1e-5))
        val along = Gen1Tile.DUPLEX_BENDING_RIGIDITY / sheet.interhelicalDistance
        assert((along / continuum).isCloseTo(25.56, 1e-3))
    }

    @Test
    fun `gate 5 upstream - C-0047's 3 x 15 dishing under C-0022's solved load reproduces at zero consumption`() {
        val stroke = PlateOnFoundation(
            sheet.plate(Gen1Tile.EDGE_X, lengthY), Gen1Tile.FOUNDATION_SECANT,
            emptyList(), basisDegree = 12
        ).solve(uniformPressure(interiorPressure)).meanDeflection
        val dishing = lattice(emptySet(), supportsOf(3)).solve(solvedField).peakDishing()
        assert((dishing / stroke).isCloseTo(0.218, 5e-3))
    }

    @Test
    fun `gate 3 conservation - the consumed and retained sets are exact complements at every level`() {
        ConsumptionPattern.entries.forEach { pattern ->
            (0..inventory.size).forEach { consumed ->
                val retained = retainedSites(inventory, consumed, pattern)
                val spent = consumedSites(inventory, consumed, pattern)
                assert(retained.size + spent.size == inventory.size)
                assert((retained intersect spent).isEmpty())
                assert((retained + spent) == inventory.toSet())
            }
        }
    }

    /**
     * The spreading pattern is **optimal for connectivity**, which is what makes the pigeonhole a
     * ceiling on the design rather than an artefact of one arrangement: no other pattern leaves
     * fewer pieces at any consumption level.
     */
    @Test
    fun `gate 3 conservation - no pattern leaves fewer pieces than the spreading one`() {
        (0..inventory.size).forEach { consumed ->
            val best = sheetComponents(
                retainedSites(inventory, consumed, ConsumptionPattern.SPREAD), duplexes
            )
            ConsumptionPattern.entries.forEach { pattern ->
                assert(
                    sheetComponents(retainedSites(inventory, consumed, pattern), duplexes) >= best
                )
            }
        }
    }

    /**
     * `C-0046`'s own elastica, re-run as a library at the connectivity ceiling — the resolution
     * of the `34 < n ≤ 45` bracket its own claim leaves open.
     */
    @Test
    fun `gate 5 upstream - C-0046's design reproduces and the connected ceiling clears the acceptable stroke`() {
        val far = ArmAnchorage.twoTerminus(BForm.PHOSPHATE_RADIUS).rotationalStiffness
        fun place(paths: Int) = tradePoint(
            paths, 1, far, Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE,
            Gen1Tile.ACCEPTABLE_STROKE, Gen1Tile.DESIRED_STROKE
        )
        assert(place(45).armLength.isCloseTo(9.131, 1e-4))
        assert(place(45).usableStroke.isCloseTo(3.119, 1e-4))
        assert(place(56).usableStroke.isCloseTo(3.312, 1e-4))
        // the ceiling clears the acceptable stroke; the threshold is 39 and 38 does not
        assert(place(42).usableStroke >= Gen1Tile.ACCEPTABLE_STROKE)
        assert(place(39).usableStroke >= Gen1Tile.ACCEPTABLE_STROKE)
        assert(place(38).usableStroke < Gen1Tile.ACCEPTABLE_STROKE)
    }

    @Test
    fun `gate 5 upstream - C-0040's per-interface census reproduces on this lattice`() {
        val perInterface = retainedPerInterface(inventory, duplexes)
        assert(perInterface.size == 14)
        assert(perInterface.all { it == 4 })
        assert(inventory.size == 56)
    }
}
