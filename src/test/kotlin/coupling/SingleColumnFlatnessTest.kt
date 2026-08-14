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
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-101` — is a 15-attachment scheme flat under the **solved** load?
 *
 * Every test is named for the verification gate it discharges. Two disciplines from
 * `CLAUDE.md` govern the numerics here:
 *
 * - **a uniform load on a uniform Winkler foundation must produce exactly zero dishing**,
 *   which is a free falsifier and is wired in as gate 2's first test;
 * - **mesh monotonicity holds only on nested refinements**, so gate 4 sweeps 1 ⊂ 2 ⊂ 4 and
 *   never 1/2/3/4 — a subdivision of 3 moves a point support off a node.
 */
class SingleColumnFlatnessTest {

    private val sheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )

    private val lengthY = DUPLEXES * sheet.interhelicalDistance

    private val interiorPressure = Gen1Tile.TARGET_FORCE / (Gen1Tile.EDGE_X * lengthY)

    private fun lattice(
        supports: List<PointSupport>,
        subdivisions: Int = 2
    ) = OrigamiGrillage(
        sheet = sheet,
        lengthX = Gen1Tile.EDGE_X,
        beamCount = DUPLEXES,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = CrossoverLayout.centred(8, sheet.crossoverSpacing / 2.0),
        subdivisions = subdivisions,
        supports = supports
    )

    private fun supportsOf(grid: List<Pair<Double, Double>>) =
        couplingSupports(grid, MANDATE)

    /** `C-0022`'s design point, transcribed here only so the test file needs no result file. */
    private val solvedField: PressureField = edgeCollarPressure(
        interiorPressure, Gen1Tile.EDGE_X, lengthY,
        listOf(CollarTerm(-0.302887367, 8.93928311), CollarTerm(-0.593889278, 1.0))
    )

    // ---------------------------------------------------------------- gate 1 — dimensional

    @Test
    fun `gate 1 dimensional - the Winkler bending length is a length and scales as the fourth root of the rigidity`() {
        val short = winklerBendingLength(rigidityPerLength = 230.0, foundationPerLength = 0.0339629)
        assert(short.isCloseTo(12.83, 1e-3))
        // sixteen times the rigidity is exactly twice the length
        val long = winklerBendingLength(
            rigidityPerLength = 16.0 * 230.0, foundationPerLength = 0.0339629
        )
        assert((long / short).isCloseTo(2.0, 1e-12))
    }

    @Test
    fun `gate 1 dimensional - the dishing is exactly linear in the applied pressure`() {
        val model = lattice(supportsOf(staggeredAttachmentGrid(1, DUPLEXES, Gen1Tile.EDGE_X, lengthY)))
        val single = model.solve(solvedField).peakDishing()
        val tripled = model.solve(
            PressureField { x, y -> 3.0 * solvedField.at(x, y) }
        ).peakDishing()
        assert((tripled / single).isCloseTo(3.0, 1e-10))
    }

    @Test
    fun `gate 1 dimensional - unphysical stagger arguments throw`() {
        assertFailsWith<IllegalArgumentException> {
            staggeredAttachmentGrid(1, DUPLEXES, Gen1Tile.EDGE_X, lengthY, stagger = -1.0)
        }
        assertFailsWith<IllegalArgumentException> {
            // half a stagger of 80 nm puts every attachment outside a 40 nm tile
            staggeredAttachmentGrid(1, DUPLEXES, Gen1Tile.EDGE_X, lengthY, stagger = 80.0)
        }
        assertFailsWith<IllegalArgumentException> {
            staggerOfBasePairs(-1.0)
        }
    }

    // ---------------------------------------------------------------- gate 2 — limiting cases

    /**
     * The free falsifier this project already uses: a **free** plate on a **uniform** foundation
     * under a **uniform** load translates exactly, whatever its flexural rigidity, because
     * `w = q/k_f` has zero fourth derivative and satisfies the free-edge conditions identically.
     * If this dishes, the solver is wrong and nothing else in the file means anything.
     */
    @Test
    fun `gate 2 limiting - a uniform load on a free tile dishes exactly zero, lattice and plate alike`() {
        val bare = lattice(emptyList())
        assert(bare.solve(uniformPressure(interiorPressure)).peakDishing() < 1e-9)
        val plate = PlateOnFoundation(
            sheet.plate(Gen1Tile.EDGE_X, lengthY), Gen1Tile.FOUNDATION_SECANT,
            emptyList(), basisDegree = 12
        )
        assert(plate.solve(uniformPressure(interiorPressure)).peakDishing() < 1e-9)
    }

    @Test
    fun `gate 2 limiting - a zero stagger reproduces the plain attachment grid identically`() {
        val plain = attachmentGrid(3, DUPLEXES, Gen1Tile.EDGE_X, lengthY)
        val staggered = staggeredAttachmentGrid(3, DUPLEXES, Gen1Tile.EDGE_X, lengthY, stagger = 0.0)
        assert(plain.size == staggered.size)
        plain.zip(staggered).forEach { (a, b) ->
            assert(abs(a.first - b.first) < 1e-15)
            assert(abs(a.second - b.second) < 1e-15)
        }
    }

    @Test
    fun `gate 2 limiting - a staggered grid is still one attachment row per duplex`() {
        val grid = staggeredAttachmentGrid(
            1, DUPLEXES, Gen1Tile.EDGE_X, lengthY, stagger = staggerOfBasePairs(8.0)
        )
        val rows = grid.map { it.second }.distinct().sorted()
        assert(rows.size == DUPLEXES)
        rows.forEachIndexed { index, y ->
            assert(abs(y - (index - 7) * Gen1Tile.INTERHELICAL_SHEET) < 1e-12)
        }
        // and the stagger lives entirely along the helices
        assert(grid.map { abs(it.first) }.distinct().size == 1)
    }

    @Test
    fun `gate 2 limiting - the 8 base pair stagger is 2 point 72 nm, quantised to the rise`() {
        assert(staggerOfBasePairs(8.0).isCloseTo(2.72, 1e-12))
        assert(staggerOfBasePairs(1.0).isCloseTo(Gen1Tile.RISE_PER_BASE_PAIR, 1e-12))
    }

    // ------------------------------------------------- gate 3 — symmetry and conservation

    /**
     * A load that varies smoothly **across** the helices, which Gauss-Legendre integrates to
     * machine precision — `C-0026`'s device, for the same reason: a conservation gate written on
     * `C-0022`'s collar would measure the **quadrature** and not the model, because the collar is
     * only `C⁰` where it meets the interior and the load vector's assembly and
     * `integrateOverFootprint` do not resolve that kink identically. That difference is 0.07 %
     * here, and it is reported as a convergence record rather than hidden inside a conservation
     * gate.
     */
    private val smoothAcrossHelices = PressureField { _, y ->
        interiorPressure * (1.0 + 0.3 * kotlin.math.cos(2.0 * Math.PI * y / lengthY))
    }

    @Test
    fun `gate 3 conservation - the support forces and the foundation carry the whole applied load`() {
        val model = lattice(supportsOf(attachmentGrid(1, DUPLEXES, Gen1Tile.EDGE_X, lengthY)))
        val solution = model.solve(smoothAcrossHelices)
        val carried = solution.foundationForce + solution.supportForces.sum()
        assert(abs(carried - solution.appliedForce) < 1e-6 * abs(solution.appliedForce))
    }

    @Test
    fun `gate 3 conservation - the collar's kink costs the equilibrium under a tenth of a per cent`() {
        val model = lattice(supportsOf(attachmentGrid(1, DUPLEXES, Gen1Tile.EDGE_X, lengthY)))
        val solution = model.solve(solvedField)
        val carried = solution.foundationForce + solution.supportForces.sum()
        assert(abs(carried - solution.appliedForce) < 1e-3 * abs(solution.appliedForce))
    }

    @Test
    fun `gate 3 symmetry - a stagger and its mirror image dish identically`() {
        val stagger = staggerOfBasePairs(32.0)
        val forward = lattice(
            supportsOf(staggeredAttachmentGrid(1, DUPLEXES, Gen1Tile.EDGE_X, lengthY, stagger))
        ).solve(solvedField).peakDishing()
        val mirrored = lattice(
            supportsOf(
                staggeredAttachmentGrid(1, DUPLEXES, Gen1Tile.EDGE_X, lengthY, stagger)
                    .map { (x, y) -> -x to y }
            )
        ).solve(solvedField).peakDishing()
        assert(abs(forward - mirrored) < 1e-9)
    }

    /**
     * `C-0015`'s exact zero survives the collinear single column — one spring per duplex at the
     * same station on every duplex, so a uniform load restores nothing. Compared **absolutely**
     * in pN, because both sides are meant to be zero and a relative test would compare noise.
     */
    @Test
    fun `gate 3 symmetry - the collinear single column keeps the exact zero under a uniform load`() {
        val model = lattice(supportsOf(attachmentGrid(1, DUPLEXES, Gen1Tile.EDGE_X, lengthY)))
        assert(model.solve(uniformPressure(interiorPressure)).peakCrossoverForce < 1e-9)
    }

    /**
     * The staggered column does **not** keep it, and the order in the stagger is the finding.
     *
     * This gate was written asserting a **second**-order response and it failed at once: the ratio
     * over a fourfold stagger is 3.58, not 16. The reason is that the restored force is a
     * **shape** effect and not a **reaction** effect. The reaction *is* second order — the tile's
     * bow is even about `x = 0`, so `w'(0) = 0` and moving a support by `±s/2` changes what it
     * carries only at `O(s²)`. But a crossover measures the **relative deflection of two adjacent
     * duplexes**, and two duplexes propped at `+s/2` and `−s/2` have mirror-image deflected
     * shapes whose difference is `O(s)` everywhere except at the centre. Alternating the support
     * *station* across the helices is therefore first order where alternating the support
     * *stiffness* is — `C-0026`'s worst scatter pattern reached in a second way.
     */
    @Test
    fun `gate 3 symmetry - the staggered column restores a force that is FIRST order in the stagger`() {
        fun restored(basePairs: Double): Double = lattice(
            supportsOf(
                staggeredAttachmentGrid(
                    1, DUPLEXES, Gen1Tile.EDGE_X, lengthY, staggerOfBasePairs(basePairs)
                )
            )
        ).solve(uniformPressure(interiorPressure)).peakCrossoverForce
        val small = restored(8.0)
        val large = restored(32.0)
        assert(small > 1e-12)
        // a fourfold stagger, and the force follows it to within a fifth of a power
        assert(large / small > 3.0)
        assert(large / small < 5.0)
    }

    // ---------------------------------------------------------------- gate 4 — convergence

    @Test
    fun `gate 4 convergence - the dishing tightens on NESTED subdivisions 1 2 4`() {
        val grid = staggeredAttachmentGrid(
            1, DUPLEXES, Gen1Tile.EDGE_X, lengthY, staggerOfBasePairs(8.0)
        )
        val values = listOf(1, 2, 4).map {
            lattice(supportsOf(grid), subdivisions = it).solve(solvedField).peakDishing()
        }
        val first = abs(values[1] - values[0]) / values[2]
        val second = abs(values[2] - values[1]) / values[2]
        assert(second < first)
        assert(second < 0.02)
    }

    @Test
    fun `gate 4 convergence - the peak dishing is insensitive to the sampling grid`() {
        val solution = lattice(
            supportsOf(attachmentGrid(1, DUPLEXES, Gen1Tile.EDGE_X, lengthY))
        ).solve(solvedField)
        val coarse = solution.peakDishing(41)
        val fine = solution.peakDishing(161)
        assert(abs(fine - coarse) / fine < 1e-3)
    }

    // ------------------------------------------------- gate 5 — upstream cross-check

    @Test
    fun `gate 5 upstream - C-0026's 1 x 15 and 3 x 15 dishing under the solved design point reproduce`() {
        val stroke = PlateOnFoundation(
            sheet.plate(Gen1Tile.EDGE_X, lengthY), Gen1Tile.FOUNDATION_SECANT,
            emptyList(), basisDegree = 12
        ).solve(uniformPressure(interiorPressure)).meanDeflection
        assert(stroke.isCloseTo(4.90731102, 1e-6))
        fun fraction(columns: Int) = lattice(
            supportsOf(attachmentGrid(columns, DUPLEXES, Gen1Tile.EDGE_X, lengthY))
        ).solve(solvedField).peakDishing() / stroke
        assert(fraction(1).isCloseTo(0.695201577, 1e-4))
        assert(fraction(3).isCloseTo(0.21821335, 1e-4))
    }

    @Test
    fun `gate 5 upstream - CH-0034's saturation floor of 0 point 149 reproduces at 15 x 15`() {
        val stroke = PlateOnFoundation(
            sheet.plate(Gen1Tile.EDGE_X, lengthY), Gen1Tile.FOUNDATION_SECANT,
            emptyList(), basisDegree = 12
        ).solve(uniformPressure(interiorPressure)).meanDeflection
        val fraction = lattice(
            supportsOf(attachmentGrid(15, DUPLEXES, Gen1Tile.EDGE_X, lengthY))
        ).solve(solvedField).peakDishing() / stroke
        assert(fraction.isCloseTo(0.149, 5e-3))
    }

    /**
     * The sheet is 25.6× stiffer along the helices than across them — `C-0015`'s reason for
     * searching grid *shapes* rather than counts, and the reason a single column has only one
     * feasible orientation. Read from the sheet's own rigidities, not transcribed.
     */
    @Test
    fun `gate 5 upstream - the sheet anisotropy is 25 point 6 times, from its own rigidities`() {
        assert((sheet.alongHelixRigidity / sheet.acrossHelixRigidity).isCloseTo(25.6, 2e-2))
    }

    @Test
    fun `gate 5 upstream - C-0022's free-tile dishing of 0 point 321 of the stroke reproduces`() {
        val plate = PlateOnFoundation(
            sheet.plate(Gen1Tile.EDGE_X, lengthY), Gen1Tile.FOUNDATION_SECANT,
            emptyList(), basisDegree = 12
        )
        val stroke = plate.solve(uniformPressure(interiorPressure)).meanDeflection
        assert((plate.solve(solvedField).peakDishing() / stroke).isCloseTo(0.32125378, 2e-2))
    }

    // ------------------------------------------------- the findings, as executable statements

    private fun dishingFraction(grid: List<Pair<Double, Double>>): Double {
        val stroke = PlateOnFoundation(
            sheet.plate(Gen1Tile.EDGE_X, lengthY), Gen1Tile.FOUNDATION_SECANT,
            emptyList(), basisDegree = 12
        ).solve(uniformPressure(interiorPressure)).meanDeflection
        return lattice(if (grid.isEmpty()) emptyList() else supportsOf(grid))
            .solve(solvedField).peakDishing() / stroke
    }

    /**
     * The finding `CH-0034`'s table could not see, because it starts at 45: below the break-even
     * a coupling adds more sag between its own attachments than it removes from the rim, so it is
     * a **net dishing source**. One column and two columns are both worse than no coupling at
     * all; three is better.
     */
    @Test
    fun `gate 2 limiting - a one and a two column coupling dish MORE than no coupling at all`() {
        val free = dishingFraction(emptyList())
        assert(free.isCloseTo(0.3079, 1e-2))
        assert(dishingFraction(attachmentGrid(1, DUPLEXES, Gen1Tile.EDGE_X, lengthY)) > free)
        assert(dishingFraction(attachmentGrid(2, DUPLEXES, Gen1Tile.EDGE_X, lengthY)) > free)
        assert(dishingFraction(attachmentGrid(3, DUPLEXES, Gen1Tile.EDGE_X, lengthY)) < free)
    }

    /**
     * The 25.6× anisotropy shows up in the **load path**, not in the dishing: fifteen attachments
     * along one helix leave the other fourteen duplexes to be carried across the hinges.
     */
    @Test
    fun `gate 3 symmetry - the wrong orientation costs an order of magnitude on the crossover path`() {
        fun crossover(grid: List<Pair<Double, Double>>) =
            lattice(supportsOf(grid)).solve(solvedField).peakCrossoverForce
        val across = crossover(attachmentGrid(1, DUPLEXES, Gen1Tile.EDGE_X, lengthY))
        val along = crossover(attachmentGrid(DUPLEXES, 1, Gen1Tile.EDGE_X, lengthY))
        assert(along / across > 10.0)
        // and both remain well below the 10 pN unzip allowable
        assert(along < Gen1Tile.DUPLEX_UNZIP_ALLOWABLE)
    }

    /**
     * The cheap bound predicts **where the best repair sits**, not only that the scheme fails:
     * the optimal half-stagger is the along-helix bending length, because that is the reach of
     * one attachment's influence patch.
     */
    @Test
    fun `gate 5 upstream - the best half-stagger is the along-helix bending length`() {
        val bendingLength = winklerBendingLength(
            rigidityPerLength = sheet.alongHelixRigidity * sheet.interhelicalDistance,
            foundationPerLength = Gen1Tile.FOUNDATION_SECANT * sheet.interhelicalDistance
        )
        val best = listOf(32.0, 48.0, 64.0, 80.0, 96.0, 112.0).minBy { basePairs ->
            dishingFraction(
                staggeredAttachmentGrid(
                    1, DUPLEXES, Gen1Tile.EDGE_X, lengthY, staggerOfBasePairs(basePairs)
                )
            )
        }
        val halfStagger = staggerOfBasePairs(best) / 2.0
        assert(abs(halfStagger / bendingLength - 1.0) < 0.10)
        // and it is still not flat, at any stagger
        assert(
            dishingFraction(
                staggeredAttachmentGrid(
                    1, DUPLEXES, Gen1Tile.EDGE_X, lengthY, staggerOfBasePairs(best)
                )
            ) > 3.0 * RIGID_PLATE_TOLERANCE
        )
    }

    /**
     * A staggered **attachment** only has to stay on the tile; a staggered **flexure** has to stay
     * on the body, and a flexure is a beam of `C-0041`'s span centred on its own midspan, which is
     * where the tie and therefore the attachment sits. So the cap is `edgeX − span` and not
     * `edgeX`, and it is what stops the unconstrained flatness optimum from being a design.
     */
    @Test
    fun `gate 2 limiting - the span caps the stagger, and the unconstrained optimum overhangs`() {
        val cap = maximumStaggerForSpan(Gen1Tile.EDGE_X, span = 21.44)
        assert(cap.isCloseTo(18.56, 1e-12))
        // C-0041's 8 bp remedy fits with room to spare; the 80 bp flatness optimum does not
        assert(staggerOfBasePairs(8.0) < cap)
        assert(staggerOfBasePairs(54.0) < cap)
        assert(staggerOfBasePairs(64.0) > cap)
        // a zero-length span is not a flexure, and a span longer than the edge does not fit
        assertFailsWith<IllegalArgumentException> {
            maximumStaggerForSpan(Gen1Tile.EDGE_X, span = 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            maximumStaggerForSpan(Gen1Tile.EDGE_X, span = 41.0)
        }
    }

    companion object {
        const val DUPLEXES: Int = 15
        const val RIGID_PLATE_TOLERANCE: Double = 0.10
        val MANDATE: Double = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE
    }

}
