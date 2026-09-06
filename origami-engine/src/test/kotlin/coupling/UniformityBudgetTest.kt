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
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.ShearJointAllowable
import com.xemantic.nano.plentyofroom.structure.edgeTaperedPressure
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-17` — what the "one attachment row per duplex" exact zero costs, and what breaks it.
 *
 * Every test is named for the verification gate it discharges. The load path this task is
 * about is a **difference of two nearly equal nodal deflections multiplied by a penalty**
 * (`CLAUDE.md`), so wherever two quantities that are both meant to be zero are compared, they
 * are compared **absolutely** in pN and never relatively.
 */
class UniformityBudgetTest {

    private val sheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )

    private val lattice = OrigamiGrillage(
        sheet = sheet,
        lengthX = Gen1Tile.EDGE_X,
        beamCount = DUPLEXES,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = CrossoverLayout.centred(8, sheet.crossoverSpacing / 2.0)
    )

    private val interiorPressure = Gen1Tile.TARGET_FORCE / (Gen1Tile.EDGE_X * lattice.lengthY)

    // ---------------------------------------------------------------- gate 1 — dimensional

    /**
     * A load that varies smoothly **across** the helices — the one non-uniformity a
     * one-row-per-duplex grid can see, and one that Gauss-Legendre integrates to machine
     * precision, so that a conservation gate written on it measures the model and not the
     * quadrature. The collar of `C-0022` is only `C⁰` where it meets the interior, and that
     * kink is what gate 4 refines.
     */
    private val smoothAcrossHelices = PressureField { _, y ->
        interiorPressure * (1.0 + 0.3 * kotlin.math.cos(2.0 * Math.PI * y / lattice.lengthY))
    }

    @Test
    fun `gate 1 dimensional - the tributary strip loads sum to the footprint integral of the pressure`() {
        val loads = tributaryStripLoads(lattice, smoothAcrossHelices)
        assert(loads.size == DUPLEXES)
        assert(loads.sum().isCloseTo(footprintLoad(lattice, smoothAcrossHelices), 1e-12))
    }

    @Test
    fun `gate 4 convergence - the strip quadrature of the KINKED collar tightens with the panel count`() {
        val pressure = edgeCollarPressure(
            interiorPressure, Gen1Tile.EDGE_X, lattice.lengthY,
            listOf(CollarTerm(-0.303, 8.94))
        )
        val departures = listOf(12, 24, 48).map { panels ->
            val loads = tributaryStripLoads(lattice, pressure, panels)
            abs(loads.sum() - footprintLoad(lattice, pressure, panels)) / loads.sum()
        }
        assert(departures[1] < departures[0])
        assert(departures[2] < departures[1])
        assert(departures[2] < 1e-6)
    }

    @Test
    fun `gate 1 dimensional - a uniform pressure puts the same load on every strip and it is q times the area`() {
        val loads = tributaryStripLoads(lattice, uniformPressure(interiorPressure))
        val expected = interiorPressure * Gen1Tile.EDGE_X * sheet.interhelicalDistance
        loads.forEach { assert(it.isCloseTo(expected, 1e-9)) }
    }

    @Test
    fun `gate 1 dimensional - the crossover force functional reproduces the lattice's own vertical force`() {
        val solution = lattice.solve(uniformPressure(interiorPressure), pointLoads = emptyList())
        lattice.crossovers.forEachIndexed { index, crossover ->
            val functional = crossoverForceFunctional(lattice, crossover)
            val fromFunctional = functional.dot(solution.coefficients)
            // absolutely, in pN: both are meant to be zero here and a relative test would
            // compare their noise
            assert(abs(fromFunctional - solution.crossoverForces[index].verticalForce) < 1e-9)
        }
    }

    @Test
    fun `gate 1 dimensional - the crossover force functional reproduces a NON-zero vertical force too`() {
        val pressure = edgeCollarPressure(
            interiorPressure, Gen1Tile.EDGE_X, lattice.lengthY,
            listOf(CollarTerm(0.5, 8.0))
        )
        val solution = lattice.solve(pressure)
        val peak = solution.peakCrossoverForce
        assert(peak > 1e-3)
        lattice.crossovers.forEachIndexed { index, crossover ->
            val functional = crossoverForceFunctional(lattice, crossover)
            assert(
                abs(
                    functional.dot(solution.coefficients) -
                            solution.crossoverForces[index].verticalForce
                ) < 1e-9 * peak
            )
        }
    }

    @Test
    fun `gate 1 dimensional - unphysical collar arguments throw`() {
        assertFailsWith<IllegalArgumentException> {
            edgeCollarPressure(1.0, 40.0, 40.0, listOf(CollarTerm(0.5, 0.0)))
        }
        assertFailsWith<IllegalArgumentException> {
            edgeCollarPressure(1.0, 40.0, 40.0, listOf(CollarTerm(Double.NaN, 4.0)))
        }
        assertFailsWith<IllegalArgumentException> {
            edgeCollarPressure(1.0, 0.0, 40.0, listOf(CollarTerm(0.5, 4.0)))
        }
    }

    @Test
    fun `gate 2 limiting cases - a collar depth above one REVERSES the load at the rim, as C-0022 reports`() {
        // C-0022's rim residual runs from -3.52 to +1.60 and its solved profile changes sign
        // within about half a nanometre of the rim; structure's edgeTaperedPressure requires
        // depth in 0..1 and cannot represent it at all
        val collar = edgeCollarPressure(
            interiorPressure, Gen1Tile.EDGE_X, lattice.lengthY, listOf(CollarTerm(1.6, 1.0))
        )
        assert(collar.at(Gen1Tile.EDGE_X / 2.0, 0.0) < 0.0)
        assert(collar.at(0.0, 0.0).isCloseTo(interiorPressure, 1e-12))
    }

    // ---------------------------------------------------------------- gate 2 — limiting cases

    @Test
    fun `gate 2 limiting cases - a zero-depth collar is the uniform field, everywhere`() {
        val collar = edgeCollarPressure(
            interiorPressure, Gen1Tile.EDGE_X, lattice.lengthY, listOf(CollarTerm(0.0, 8.0))
        )
        listOf(0.0, 5.0, 19.5, -19.99).forEach { x ->
            listOf(0.0, 7.0, -20.1).forEach { y ->
                assert(collar.at(x, y).isCloseTo(interiorPressure, 1e-12))
            }
        }
    }

    @Test
    fun `gate 2 limiting cases - the collar field equals structure's edgeTaperedPressure where both are defined`() {
        val plate = sheet.plate(Gen1Tile.EDGE_X, lattice.lengthY)
        val theirs = edgeTaperedPressure(interiorPressure, plate, 4.0, 0.5)
        val ours = edgeCollarPressure(
            interiorPressure, Gen1Tile.EDGE_X, lattice.lengthY, listOf(CollarTerm(0.5, 4.0))
        )
        for (i in 0..40) {
            val x = Gen1Tile.EDGE_X * (i / 40.0 - 0.5)
            for (j in 0..40) {
                val y = lattice.lengthY * (j / 40.0 - 0.5)
                assert(ours.at(x, y).isCloseTo(theirs.at(x, y), 1e-12))
            }
        }
    }

    @Test
    fun `gate 2 limiting cases - a uniform load restores exactly zero on every one-row-per-duplex grid`() {
        listOf(1, 2, 3, 5, 8, 15).forEach { columns ->
            val supports = couplingSupports(
                attachmentGrid(columns, DUPLEXES, Gen1Tile.EDGE_X, lattice.lengthY),
                MANDATED_STIFFNESS
            )
            val supported = lattice.withSupports(supports)
            val solution = supported.solve(uniformPressure(interiorPressure))
            assert(solution.peakCrossoverForce < 1e-9)
        }
    }

    @Test
    fun `gate 2 limiting cases - a grid whose rows are NOT one per duplex restores a finite force`() {
        val supports = couplingSupports(
            attachmentGrid(3, 11, Gen1Tile.EDGE_X, lattice.lengthY), MANDATED_STIFFNESS
        )
        val solution = lattice.withSupports(supports).solve(uniformPressure(interiorPressure))
        assert(solution.peakCrossoverForce > 1e-3)
    }

    @Test
    fun `gate 2 limiting cases - a load varying only along the helices restores exactly zero`() {
        val supports = couplingSupports(
            attachmentGrid(3, DUPLEXES, Gen1Tile.EDGE_X, lattice.lengthY), MANDATED_STIFFNESS
        )
        val alongOnly = PressureField { x, _ ->
            interiorPressure * (1.0 + 0.4 * kotlin.math.cos(2.0 * Math.PI * x / Gen1Tile.EDGE_X))
        }
        val solution = lattice.withSupports(supports).solve(alongOnly)
        assert(solution.peakCrossoverForce < 1e-9)
        // and it is not a trivially small load case: the duplexes bend
        assert(solution.peakDuplexShear > 1e-3)
    }

    @Test
    fun `gate 2 limiting cases - the 3 x 15 flatness grid IS one attachment row per duplex`() {
        val grid = attachmentGrid(3, DUPLEXES, Gen1Tile.EDGE_X, lattice.lengthY)
        val rows = grid.map { it.second }.distinct().sorted()
        assert(rows.size == DUPLEXES)
        rows.zip(lattice.beamY).forEach { (row, axis) -> assert(abs(row - axis) < 1e-12) }
    }

    // ---------------------------------------------------------------- gate 3 — symmetry

    @Test
    fun `gate 3 conservation - the crossover forces on one interface sum to the lattice's own interface shear`() {
        val supports = couplingSupports(
            attachmentGrid(3, DUPLEXES, Gen1Tile.EDGE_X, lattice.lengthY), MANDATED_STIFFNESS
        )
        val supported = lattice.withSupports(supports)
        val solution = supported.solve(smoothAcrossHelices)
        val scale = solution.peakCrossoverForce
        assert(scale > 1e-3)
        (0 until DUPLEXES - 1).forEach { interfaceIndex ->
            val summed = solution.crossoverForces
                .filter { it.lowerBeam == interfaceIndex }
                .sumOf { it.verticalForce }
            assert(abs(summed - solution.shearAcrossInterface(interfaceIndex)) < 1e-2 * scale)
        }
    }

    @Test
    fun `gate 3 symmetry - the restored crossover force is exactly linear in the collar depth`() {
        val supports = couplingSupports(
            attachmentGrid(3, DUPLEXES, Gen1Tile.EDGE_X, lattice.lengthY), MANDATED_STIFFNESS
        )
        val supported = lattice.withSupports(supports)
        fun peak(depth: Double): Double = supported.solve(
            edgeCollarPressure(
                interiorPressure, Gen1Tile.EDGE_X, lattice.lengthY,
                listOf(CollarTerm(depth, 8.94))
            )
        ).peakCrossoverForce
        val reference = peak(0.1)
        assert(peak(0.5).isCloseTo(5.0 * reference, 1e-6))
        // and an enhancement is the same magnitude with the sign reversed
        assert(peak(-0.1).isCloseTo(reference, 1e-6))
    }

    @Test
    fun `gate 3 conservation - the rigid-tile identity is recovered as the SHEET stiffens`() {
        // the rigid limit is a stiff SHEET, not a stiff foundation: a stiff Winkler foundation
        // makes the tile CONFORM to the load, which is the opposite of rigid
        val identity = rigidTileInterfaceForces(tributaryStripLoads(lattice, smoothAcrossHelices))
        val scale = identity.maxOf { abs(it) }
        val departures = listOf(1.0, 1e2, 1e4, 1e6).map { rigidity ->
            val stiff = OrigamiGrillage(
                sheet = sheet.copy(
                    duplex = sheet.duplex.copy(
                        bendingRigidity = sheet.duplex.bendingRigidity * rigidity,
                        torsionalRigidity = sheet.duplex.torsionalRigidity * rigidity
                    ),
                    crossoverHingeStiffness = sheet.crossoverHingeStiffness * rigidity
                ),
                lengthX = Gen1Tile.EDGE_X,
                beamCount = DUPLEXES,
                foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
                columns = CrossoverLayout.centred(8, sheet.crossoverSpacing / 2.0),
                linkStiffness = OrigamiGrillage.RIGID_LINK_STIFFNESS * rigidity,
                supports = couplingSupports(
                    attachmentGrid(3, DUPLEXES, Gen1Tile.EDGE_X, lattice.lengthY),
                    MANDATED_STIFFNESS
                )
            )
            val solution = stiff.solve(smoothAcrossHelices)
            (0 until DUPLEXES - 1).maxOf {
                abs(solution.shearAcrossInterface(it) - identity[it])
            }
        }
        departures.zipWithNext { a, b -> assert(b < a) }
        assert(departures.last() < 0.01 * scale)
    }

    // ---------------------------------------------------------------- gate 4 — convergence

    @Test
    fun `gate 4 convergence - the restored force converges over the NESTED subdivisions 1 2 4`() {
        val pressure = edgeCollarPressure(
            interiorPressure, Gen1Tile.EDGE_X, lattice.lengthY, listOf(CollarTerm(0.5, 8.0))
        )
        val peaks = listOf(1, 2, 4).map { subdivisions ->
            val refined = OrigamiGrillage(
                sheet = sheet,
                lengthX = Gen1Tile.EDGE_X,
                beamCount = DUPLEXES,
                foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
                columns = CrossoverLayout.centred(8, sheet.crossoverSpacing / 2.0),
                subdivisions = subdivisions,
                supports = couplingSupports(
                    attachmentGrid(3, DUPLEXES, Gen1Tile.EDGE_X, DUPLEXES * sheet.interhelicalDistance),
                    MANDATED_STIFFNESS
                )
            )
            refined.solve(pressure).peakCrossoverForce
        }
        assert(abs(peaks[2] - peaks[1]) < 0.02 * peaks[2])
    }

    @Test
    fun `gate 4 convergence - the STATIC restored force does not depend on the link penalty`() {
        val pressure = edgeCollarPressure(
            interiorPressure, Gen1Tile.EDGE_X, lattice.lengthY, listOf(CollarTerm(0.5, 8.0))
        )
        val peaks = listOf(1e3, 1e4, 1e5).map { penalty ->
            OrigamiGrillage(
                sheet = sheet,
                lengthX = Gen1Tile.EDGE_X,
                beamCount = DUPLEXES,
                foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
                columns = CrossoverLayout.centred(8, sheet.crossoverSpacing / 2.0),
                linkStiffness = penalty
            ).solve(pressure).peakCrossoverForce
        }
        assert(abs(peaks[2] - peaks[1]) < 0.01 * peaks[2])
    }

    @Test
    fun `gate 4 convergence - the THERMAL crossover force does NOT converge in the link penalty, it grows as its square root`() {
        val ratios = listOf(1e3, 1e4, 1e5).map { penalty ->
            val model = OrigamiGrillage(
                sheet = sheet,
                lengthX = Gen1Tile.EDGE_X,
                beamCount = DUPLEXES,
                foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
                columns = CrossoverLayout.centred(8, sheet.crossoverSpacing / 2.0),
                linkStiffness = penalty
            )
            thermalCrossoverForceRms(model).max()
        }
        // a decade of penalty is a factor of sqrt(10) in the force — the quantity is a
        // property of the JOINT and not of the load path, and the rigid limit does not exist
        assert((ratios[1] / ratios[0]).isCloseTo(kotlin.math.sqrt(10.0), 0.05))
        assert((ratios[2] / ratios[1]).isCloseTo(kotlin.math.sqrt(10.0), 0.05))
    }

    // ---------------------------------------------------------------- gate 5 — cross-check

    @Test
    fun `gate 5 cross-check - C-0015's exact zero is reproduced under its OWN point-load case`() {
        // C-0015's load case: the 100 pN enters at the attachments and the foundation reacts
        val loads = attachmentGrid(3, DUPLEXES, Gen1Tile.EDGE_X, lattice.lengthY).map { (x, y) ->
            PointLoad(
                x, y, Gen1Tile.TARGET_FORCE / (3 * DUPLEXES)
            )
        }
        assert(lattice.solve(pointLoads = loads).peakCrossoverForce < 1e-9)
    }

    @Test
    fun `gate 5 cross-check - C-0017's lateral and yaw by-products are reproduced on this grid`() {
        val grid = attachmentGrid(3, DUPLEXES, Gen1Tile.EDGE_X, 40.35)
        val meanSquaredRadius = grid.sumOf { (x, y) -> x * x + y * y } / grid.size
        // C-0017: k_yaw = 8205 pN*nm/rad at k_lat = 32.36 pN/nm, i.e. <r^2> = 253.6 nm^2
        assert(meanSquaredRadius.isCloseTo(253.6, 1e-3))
        assert(yawStiffness(32.36 / grid.size, grid).isCloseTo(8205.0, 1e-3))
    }

    @Test
    fun `gate 5 cross-check - CH-0029's length-dependent shear ladder is reproduced`() {
        val joint = ShearJointAllowable()
        val rate = ShearJointAllowable.REFERENCE_LOADING_RATE
        assert(joint.ruptureForce(8.0, rate).isCloseTo(18.8, 0.01))
        assert(joint.ruptureForce(16.0, rate).isCloseTo(34.8, 0.01))
        assert(joint.ruptureForce(30.0, rate).isCloseTo(47.1, 0.01))
    }

    private companion object {

        const val DUPLEXES: Int = 15

        /** `C-0017`'s mandate, `100 pN / 3 nm`. */
        const val MANDATED_STIFFNESS: Double = 100.0 / 3.0
    }

}
