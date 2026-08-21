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

package com.xemantic.nano.plentyofroom.tile

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlin.math.abs
import kotlin.math.hypot
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-253` — the honeycomb grillage the corpus did not have.
 *
 * `CLAUDE.md`: *"`OrigamiGrillage` NEVER READS `layers` OR `interlayerCoupling`"* and
 * *"`CrossoverLayout`'s two-parity alternation makes its crossover combinatorics
 * SQUARE-LATTICE"*. The cheap bound in the task file says why that cannot be adapted rather
 * than replaced: a honeycomb site has **three** lattice neighbours, and `OrigamiGrillage`'s
 * interfaces form a **path graph**, which is maximum degree two.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 */
class HoneycombGrillageTest {

    private val d = Gen1Tile.INTERHELICAL_HONEYCOMB

    private fun grillage(
        rows: Int = 4,
        columns: Int = 2,
        rowBasePairs: Int = 42,
        subdivisions: Int = 1,
        prestrains: Map<HoneycombBondSite, Double> = emptyMap()
    ) = HoneycombGrillage(
        block = HoneycombBlock(rows, columns),
        rowBasePairs = rowBasePairs,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        subdivisions = subdivisions,
        bondPrestrains = prestrains
    )

    // ------------------------------------------------------------------ gate 1, dimensional

    @Test
    fun `gate 1 - dimensional - every bond is exactly one lattice constant long`() {
        val lattice = grillage(rows = 5, columns = 4)
        assert(lattice.bonds.isNotEmpty())
        lattice.bonds.forEach { bond ->
            val dy = lattice.beamY[bond.site.upperBeam] - lattice.beamY[bond.site.lowerBeam]
            val dz = lattice.beamZ[bond.site.upperBeam] - lattice.beamZ[bond.site.lowerBeam]
            assert(hypot(dy, dz).isCloseTo(d))
            assert((bond.unitY * d).isCloseTo(dy))
            assert((bond.unitZ * d).isCloseTo(dz))
        }
    }

    @Test
    fun `gate 1 - dimensional - the face tributaries are one row pitch wide and sum to the width`() {
        val lattice = grillage(rows = 6, columns = 3)
        assert(lattice.lengthY.isCloseTo(6.0 * HoneycombCrossSectionGeometry.rowPitch(d)))
        val strips = lattice.faceBeams.indices.map { lattice.tributary(it) }
        // Each face beam owns a strip CENTRED on its own axis: the face is corrugated, so a
        // tiling strip would be off-centre and a uniform pressure would then apply a rolling
        // moment at the row pitch. The widths still sum to the in-plane width exactly.
        strips.forEachIndexed { index, (low, high) ->
            assert((high - low).isCloseTo(HoneycombCrossSectionGeometry.rowPitch(d)))
            assert(((low + high) / 2.0).isCloseTo(lattice.beamY[lattice.faceBeams[index]]))
        }
        assert(strips.sumOf { it.second - it.first }.isCloseTo(lattice.lengthY))
    }

    @Test
    fun `gate 1 - dimensional - a non-positive parameter is refused`() {
        assertFailsWith<IllegalArgumentException> { grillage(rowBasePairs = 0) }
        assertFailsWith<IllegalArgumentException> { grillage(subdivisions = 0) }
        assertFailsWith<IllegalArgumentException> {
            HoneycombGrillage(HoneycombBlock(3, 2), 42, foundationStiffness = 0.0)
        }
    }

    // ------------------------------------------------------------------ gate 2, limiting cases

    @Test
    fun `gate 2 - limiting case - a one-by-two block carries one interface and no in-plane bond`() {
        val lattice = grillage(rows = 1, columns = 2, rowBasePairs = 21)
        assert(lattice.beamCount == 2)
        assert(lattice.bonds.all { !it.inPlane })
        assert(lattice.bonds.map { it.site.lowerBeam to it.site.upperBeam }.toSet().size == 1)
        assert(lattice.bonds.all { it.bondClass == 0 })
    }

    @Test
    fun `gate 2 - limiting case - F3 no single layer of a honeycomb block is a connected sheet`() {
        (2..12).forEach { rows ->
            val block = HoneycombBlock(rows, 1)
            assert(honeycombBondGraphComponents(block) == (rows + 1) / 2)
        }
    }

    @Test
    fun `gate 2 - limiting case - F4 the bond graph is not a path at any relabelling`() {
        // an OrigamiGrillage bonds beam i to beam i+1 only, so its interface graph is a PATH
        // and has maximum degree two; a honeycomb interior site has three neighbours.
        assert(honeycombMaximumDegree(HoneycombBlock(10, 6)) == 3)
        assert(honeycombMaximumDegree(HoneycombBlock(15, 4)) == 3)
        assert(honeycombMaximumDegree(HoneycombBlock(6, 3)) == 3)
        // and the boundary is exact: at TWO helices per row every site has at most two
        // neighbours, so a `m x 2` block is a path and IS representable as an OrigamiGrillage
        // in its connectivity. The corpus's four-layer blocks are not.
        assert(honeycombMaximumDegree(HoneycombBlock(4, 2)) == 2)
        assert(honeycombMaximumDegree(HoneycombBlock(2, 1)) == 1)
    }

    @Test
    fun `gate 2 - limiting case - the along-helix curvature field costs the parallel-axis energy`() {
        val lattice = grillage(rows = 4, columns = 4, rowBasePairs = 42)
        val curvature = 1e-4
        val field = lattice.alongHelixCurvatureField(curvature)
        val expected = 0.5 * curvature * curvature * lattice.lengthS *
                (lattice.beamCount * lattice.duplex.bendingRigidity +
                        lattice.duplex.stretchModulus * lattice.beamZ.sumOf { it * it })
        assert((lattice.beamEnergy(field) + lattice.axialEnergy(field)).isCloseTo(expected))
        assert(abs(lattice.slipEnergy(field)) < 1e-18)
        assert(abs(lattice.linkEnergy(field)) < 1e-18)
        assert(abs(lattice.hingeEnergy(field)) < 1e-18)
    }

    @Test
    fun `gate 2 - limiting case - the across-helix curvature field is carried by the hinges alone`() {
        val lattice = grillage(rows = 5, columns = 4, rowBasePairs = 42)
        val curvature = 1e-4
        val field = lattice.acrossHelixCurvatureField(curvature)
        val expected = 0.5 * lattice.hingeStiffness * curvature * curvature * d * d *
                lattice.bonds.sumOf { it.unitY * it.unitY }
        assert(lattice.hingeEnergy(field).isCloseTo(expected))
        assert(abs(lattice.beamEnergy(field)) < 1e-18)
        assert(abs(lattice.axialEnergy(field)) < 1e-18)
        assert(abs(lattice.slipEnergy(field)) < 1e-18)
        assert(abs(lattice.linkEnergy(field)) < 1e-18)
    }

    // ------------------------------------------------------------------ gate 3, symmetry

    @Test
    fun `gate 3 - symmetry - F1 a uniform pressure on a uniform foundation dishes zero`() {
        val lattice = grillage(rows = 6, columns = 4, rowBasePairs = 42)
        val solution = lattice.solve(uniformPressure(0.05))
        assert(solution.peakDishing(41) < 1e-6 * solution.meanDeflection)
    }

    @Test
    fun `gate 3 - conservation - the foundation carries the whole applied load`() {
        val lattice = grillage(rows = 6, columns = 4, rowBasePairs = 42)
        val solution = lattice.solve(uniformPressure(0.05))
        assert(
            abs(solution.foundationForce - solution.appliedForce) <
                    1e-7 * abs(solution.appliedForce)
        )
    }

    @Test
    fun `gate 3 - symmetry - the axial pin removes a rigid mode and nothing else`() {
        val a = grillage(rows = 5, columns = 4, rowBasePairs = 42)
        val b = HoneycombGrillage(
            block = HoneycombBlock(5, 4),
            rowBasePairs = 42,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            subdivisions = 1,
            axialPinBeam = 7
        )
        val load = uniformPressure(0.05)
        assert(a.solve(load).peakDishing(41).isCloseTo(b.solve(load).peakDishing(41)))
        assert(a.solve(load).meanDeflection.isCloseTo(b.solve(load).meanDeflection))
    }

    @Test
    fun `gate 3 - symmetry - a prestrain changes no entry of the stiffness matrix`() {
        val sites = grillage().bonds.take(3).map { it.site }
        val strained = grillage(prestrains = sites.associateWith { 0.3 })
        val free = grillage()
        for (i in 0 until free.degreesOfFreedom step 37) {
            for (j in maxOf(0, i - free.bandwidth)..i step 11) {
                assert(abs(strained.stiffnessEntry(i, j) - free.stiffnessEntry(i, j)) < 1e-15)
            }
        }
    }

    @Test
    fun `gate 3 - symmetry - the prestrain response is exactly linear in the angle`() {
        val sites = grillage().bonds.take(4).map { it.site }
        val one = grillage(prestrains = sites.associateWith { 0.01 }).solve()
        val two = grillage(prestrains = sites.associateWith { 0.02 }).solve()
        assert(two.peakDishing(41).isCloseTo(2.0 * one.peakDishing(41)))
    }

    @Test
    fun `gate 3 - symmetry - an all-zero prestrain map is the unstrained lattice`() {
        val sites = grillage().bonds.take(5).map { it.site }
        val zero = grillage(prestrains = sites.associateWith { 0.0 }).solve(uniformPressure(0.05))
        val bare = grillage().solve(uniformPressure(0.05))
        assert(abs(zero.peakDishing(41) - bare.peakDishing(41)) < 1e-12)
    }

    @Test
    fun `gate 2 - limiting case - the realised rigidity is bracketed by its two limits`() {
        val curvature = 1e-4
        val soft = HoneycombGrillage(
            block = HoneycombBlock(4, 4), rowBasePairs = 42,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            slipStiffness = Gen1Tile.crossoverInPlaneStiffness() * 1e-8
        )
        val stiff = HoneycombGrillage(
            block = HoneycombBlock(4, 4), rowBasePairs = 42,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            slipStiffness = Gen1Tile.crossoverInPlaneStiffness() * 1e8
        )
        val independent = 4.0 * stiff.duplex.bendingRigidity / stiff.rowPitch
        val composite = 2.0 *
                (stiff.beamEnergy(stiff.alongHelixCurvatureField(curvature)) +
                        stiff.axialEnergy(stiff.alongHelixCurvatureField(curvature))) /
                (curvature * curvature * stiff.area)
        assert(composite > independent)
        // a vanishing slip spring leaves the layers bending independently
        assert(soft.realisedAlongHelixRigidity(curvature).isCloseTo(independent, 1e-4))
        // and a rigid one cannot exceed the parallel-axis closed form
        assert(stiff.realisedAlongHelixRigidity(curvature) < composite * (1.0 + 1e-9))
        assert(stiff.realisedAlongHelixRigidity(curvature) > independent)
    }

    @Test
    fun `gate 3 - symmetry - relaxing the axial coordinates cannot raise the energy`() {
        val lattice = grillage(rows = 4, columns = 4, rowBasePairs = 42)
        val curvature = 1e-4
        val imposed = lattice.alongHelixCurvatureField(curvature)
        val before = lattice.beamEnergy(imposed) + lattice.axialEnergy(imposed) +
                lattice.slipEnergy(imposed) + lattice.hingeEnergy(imposed) +
                lattice.linkEnergy(imposed)
        val relaxed = lattice.axialRelaxed(imposed)
        val after = lattice.beamEnergy(relaxed) + lattice.axialEnergy(relaxed) +
                lattice.slipEnergy(relaxed) + lattice.hingeEnergy(relaxed) +
                lattice.linkEnergy(relaxed)
        assert(after < before)
        // and the bending kinematics are untouched by the relaxation
        for (i in 0 until lattice.degreesOfFreedom) {
            if (i % HoneycombGrillage.DOF_PER_NODE != HoneycombGrillage.U) {
                assert(abs(relaxed[i] - imposed[i]) < 1e-15)
            }
        }
    }

    @Test
    fun `gate 3 - conservation - the rigid-mode duals reproduce the two-field quadrature`() {
        val lattice = grillage(rows = 5, columns = 3, rowBasePairs = 42)
        val solution = lattice.solve(uniformPressure(0.05))
        val direct = lattice.areaInnerProduct(lattice.pistonMode, solution.coefficients)
        assert(solution.meanDeflection.isCloseTo(direct))
        val tilt = lattice.areaInnerProduct(lattice.tiltSMode, lattice.tiltSMode)
        assert(lattice.tiltSNorm.isCloseTo(tilt * lattice.area))
    }

    // ------------------------------------------------------------------ gate 4, convergence

    @Test
    fun `gate 4 - convergence - nested beam subdivisions settle`() {
        // NOT under a uniform pressure: a free lattice on a uniform foundation answers that one
        // with an exact rigid translation at every mesh, so the departure is identically zero and
        // the test would measure nothing. `CLAUDE.md`: choose a WELL-CONDITIONED load case.
        val lattice = { subdivisions: Int ->
            grillage(rows = 4, columns = 3, rowBasePairs = 42, subdivisions = subdivisions)
        }
        val readings = listOf(1, 2, 4).map { subdivisions ->
            val one = lattice(subdivisions)
            val length = one.lengthS
            one.solve(
                com.xemantic.nano.plentyofroom.structure.PressureField { s, _ ->
                    0.05 * (1.0 + 2.0 * (s / length) * (s / length))
                }
            ).peakDishing(41)
        }
        val first = abs(readings[1] - readings[0])
        val second = abs(readings[2] - readings[1])
        assert(second < first)
    }

    // ------------------------------------------------------------------ gate 5, the lattice rule

    @Test
    fun `gate 5 - literature - the in-plane bonds are all class two and the interlayer ones are not`() {
        val lattice = grillage(rows = 6, columns = 4, rowBasePairs = 42)
        assert(lattice.bonds.filter { it.inPlane }.all { it.bondClass == 2 })
        assert(lattice.bonds.filterNot { it.inPlane }.all { it.bondClass != 2 })
        assert(lattice.bonds.filterNot { it.inPlane }.map { it.bondClass }.toSet() == setOf(0, 1))
    }

    @Test
    fun `gate 5 - literature - a class recurs every twenty-one base pairs on one interface`() {
        val lattice = grillage(rows = 4, columns = 2, rowBasePairs = 84)
        val interface0 = lattice.bonds.filter {
            it.site.lowerBeam == 0 && it.site.upperBeam == 1
        }.map { it.plane }.sorted()
        assert(interface0.isNotEmpty())
        for (i in 0 until interface0.size - 1) assert(interface0[i + 1] - interface0[i] == 3)
    }

}
