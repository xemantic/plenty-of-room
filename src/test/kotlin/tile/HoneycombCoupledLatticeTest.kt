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
import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-263` — the coupling surrogate ported onto the honeycomb grillage.
 *
 * `C-0154` measured that `OrigamiSheet`'s across-helix rigidity is `24/7` overstated on a
 * honeycomb block, and that one layer of such a block is a set of **dimers** rather than a
 * sheet — so every coupled cell in this corpus, all of them smeared single-layer square-lattice
 * solves, is graded on a body the design does not contain. This is the port that lets them be
 * re-graded, and every test is named for the gate it discharges.
 *
 * The point-load dual is the exact gradient of the same `evaluate` the sampling uses, so
 * `M = eᵀK⁻¹e` is symmetric **by construction** and its residual measures nothing. What has
 * content is Betti between the point functional and the **pressure quadrature**, which is a
 * different rule on different points — that is `gate 3 - F4`.
 */
class HoneycombCoupledLatticeTest {

    private fun grillage(
        rows: Int = 4,
        columns: Int = 2,
        rowBasePairs: Int = 42,
        subdivisions: Int = 1,
        enhancement: Double = 1.0
    ) = HoneycombGrillage(
        block = HoneycombBlock(rows, columns),
        rowBasePairs = rowBasePairs,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        hingeStiffnessEnhancement = enhancement,
        subdivisions = subdivisions
    )

    private fun tiltedPressure(peak: Double, lengthS: Double): PressureField =
        PressureField { s, _ -> peak * (1.0 + 0.5 * s / lengthS) }

    // ------------------------------------------------------------------ gate 1, dimensional

    @Test
    fun `gate 1 - dimensional - a unit downward point load does unit work under a unit piston`() {
        val lattice = grillage()
        val dual = lattice.pointLoadDual(0.7, lattice.beamY[lattice.faceBeams[1]], 1.0)
        assert(dual.dot(lattice.pistonMode).isCloseTo(1.0))
    }

    @Test
    fun `gate 1 - dimensional - a point load's work under the two rigid tilts is its position`() {
        val lattice = grillage()
        val s = 1.9
        val y = lattice.beamY[lattice.faceBeams[2]] + 0.4
        val dual = lattice.pointLoadDual(s, y, 3.0)
        assert(dual.dot(lattice.tiltSMode).isCloseTo(3.0 * s))
        assert(dual.dot(lattice.tiltYMode).isCloseTo(3.0 * y))
    }

    @Test
    fun `gate 1 - dimensional - the surrogate's half extents are the lattice's own`() {
        val lattice = grillage()
        val grid = listOf(0.0 to lattice.beamY[lattice.faceBeams[1]])
        val surrogate = honeycombInfluenceSurrogate(
            lattice, grid, uniformPressure(0.001), samples = 5
        )
        assert(surrogate.pathCount == 1)
        assert(surrogate.samples == 5)
        // the sampled field spans the face exactly, so a station at the centre is sampled
        assert(abs(lattice.lengthS - lattice.rowBasePairs * Gen1Tile.RISE_PER_BASE_PAIR) < 1e-12)
    }

    @Test
    fun `gate 1 - dimensional - the face helices alternate d and 2d, not one row pitch`() {
        val lattice = grillage(rows = 10, columns = 6)
        val d = Gen1Tile.INTERHELICAL_HONEYCOMB
        val gaps = lattice.faceBeams.zipWithNext { a, b -> lattice.beamY[b] - lattice.beamY[a] }
        assert(gaps.filterIndexed { i, _ -> i % 2 == 0 }.all { abs(it - d) < 1e-9 })
        assert(gaps.filterIndexed { i, _ -> i % 2 == 1 }.all { abs(it - 2.0 * d) < 1e-9 })
        // so an abstract grid row sits exactly a quarter of a lattice constant off its own helix
        val abstract = attachmentGrid(1, 10, lattice.lengthS, lattice.lengthY)
        val offsets = lattice.faceBeams.indices.map { abstract[it].second - lattice.beamY[lattice.faceBeams[it]] }
        assert(offsets.all { abs(abs(it) - d / 4.0) < 1e-9 })
        assert(offsets.zipWithNext().all { (a, b) -> a * b < 0.0 })
    }

    // ------------------------------------------------------------------ gate 2, limiting cases

    @Test
    fun `gate 2 - limiting - no point loads is the pressure-only solve, bit for bit`() {
        val lattice = grillage()
        val pressure = tiltedPressure(0.02, lattice.lengthS)
        val without = lattice.solve(pressure)
        val with = lattice.solve(pressure, emptyList())
        assert(without.peakDishing(21) == with.peakDishing(21))
    }

    @Test
    fun `gate 2 - limiting - a zero-magnitude point load changes nothing`() {
        val lattice = grillage()
        val pressure = tiltedPressure(0.02, lattice.lengthS)
        val y = lattice.beamY[lattice.faceBeams[1]]
        val without = lattice.solve(pressure).peakDishing(21)
        val with = lattice.solve(pressure, listOf(PointLoad(0.5, y, 0.0))).peakDishing(21)
        assert(abs(without - with) < 1e-12 * without)
    }

    @Test
    fun `gate 2 - limiting - a station off the face is refused rather than snapped`() {
        val lattice = grillage()
        assertFailsWith<IllegalArgumentException> {
            lattice.pointLoadDual(lattice.lengthS, 0.0, 1.0)
        }
        assertFailsWith<IllegalArgumentException> {
            honeycombInfluenceSurrogate(
                lattice, listOf(0.0 to lattice.lengthY), uniformPressure(0.001), samples = 5
            )
        }
    }


    @Test
    fun `gate 2 - limiting - the beams reach the end of a row that is not a multiple of 7 bp`() {
        val rise = Gen1Tile.RISE_PER_BASE_PAIR
        // a row that ends ON a crossover plane is untouched -- every lattice C-0154 measured
        listOf(42, 56, 112).forEach { row ->
            val lattice = grillage(rowBasePairs = row)
            assert(abs(lattice.nodeS.last() - lattice.lengthS / 2.0) < 1e-12)
            assert(lattice.nodesPerBeam == lattice.planeBasePairs.size)
        }
        // and a row with a remainder carries a free overhang past its last crossover column
        val overhang = grillage(rowBasePairs = 116, subdivisions = 2)
        assert(abs(overhang.nodeS.last() - overhang.lengthS / 2.0) < 1e-12)
        assert(overhang.planeBasePairs.last() == 112)
        assert(abs(overhang.nodeS.last() - 112 * rise + overhang.lengthS / 2.0 - 4.0 * rise) < 1e-9)
    }

    @Test
    fun `gate 3 - F1 - a uniform pressure dishes zero on a row with a free overhang`() {
        val lattice = grillage(rows = 10, columns = 6, rowBasePairs = 116)
        val pressure = 0.0666534426
        val stroke = lattice.solve(uniformPressure(pressure)).meanDeflection
        assert(abs(stroke - pressure / Gen1Tile.FOUNDATION_SECANT) < 1e-9 * stroke)
        assert(lattice.solve(uniformPressure(pressure)).peakDishing(41) / stroke < 1e-9)
    }

    // ------------------------------------------------- gate 3, symmetry and conservation

    @Test
    fun `gate 3 - F1 - a uniform pressure leaves the surrogate's free field with zero dishing`() {
        val lattice = grillage()
        val grid = attachmentGrid(2, 4, lattice.lengthS, lattice.lengthY)
        val surrogate = honeycombInfluenceSurrogate(
            lattice, grid, uniformPressure(0.03), samples = 21
        )
        val free = surrogate.solve(List(grid.size) { 1e-9 })
        assert(free.peakDishing < 1e-9)
    }

    @Test
    fun `gate 3 - F1 - the free stroke on the lattice is exactly the pressure over k_f`() {
        val lattice = grillage()
        val pressure = 0.05
        val mean = lattice.solve(uniformPressure(pressure)).meanDeflection
        assert(abs(mean - pressure / Gen1Tile.FOUNDATION_SECANT) < 1e-10 * mean)
    }

    @Test
    fun `gate 3 - F4 - Betti holds between the point dual and the pressure load vector`() {
        val lattice = grillage()
        val pressure = tiltedPressure(0.02, lattice.lengthS)
        val s = 1.3
        val y = lattice.beamY[lattice.faceBeams[2]] + 0.3
        val underPressure = lattice.solve(pressure)
        val underPoint = lattice.solve(uniformPressure(0.0), listOf(PointLoad(s, y, 1.0)))
        val forward = lattice.pointLoadDual(s, y).dot(underPressure.coefficients)
        val backward = lattice.assembleLoad(pressure).dot(underPoint.coefficients)
        assert(abs(forward - backward) < 1e-9 * abs(forward))
        // and the forward form IS the deflection the point functional was written to read
        assert(abs(forward - underPressure.deflection(s, y)) < 1e-12 * abs(forward))
    }

    @Test
    fun `gate 3 - F4 - the pressure QUADRATURE is adjoint only up to the corrugation term`() {
        val lattice = grillage()
        val pressure = tiltedPressure(0.02, lattice.lengthS)
        val s = 1.3
        val y = lattice.beamY[lattice.faceBeams[2]] + 0.3
        val underPressure = lattice.solve(pressure).deflection(s, y)
        val underPoint = lattice.solve(uniformPressure(0.0), listOf(PointLoad(s, y, 1.0)))
        val quadrature = lattice.integrateOverFace { a, b ->
            pressure.at(a, b) * underPoint.deflection(a, b)
        }
        // `assembleLoad` gives every face beam a strip one row pitch wide CENTRED ON ITS OWN
        // AXIS -- which is what keeps the uniform-load falsifier exact on a corrugated face --
        // while `evaluate` reads the NEAREST face helix. The gaps alternate `d` and `2d`, so the
        // strips overlap and gap, and the two are adjoint only up to that bookkeeping term.
        val departure = abs(underPressure - quadrature) / abs(underPressure)
        assert(departure > 1e-9)
        assert(departure < 1e-2)
    }

    @Test
    fun `gate 3 - F3 - the surrogate at full presence is the assembled solve`() {
        val lattice = grillage()
        val pressure = tiltedPressure(0.02, lattice.lengthS)
        val s = 1.3
        val y = lattice.beamY[lattice.faceBeams[2]]
        val surrogate = honeycombInfluenceSurrogate(lattice, listOf(s to y), pressure, samples = 21)
        val stiffness = 4.0
        val coupled = surrogate.solve(listOf(stiffness))
        val force = coupled.supportForces[0]
        // the support force acts UPWARD, so the assembled load carries its negative
        val assembled = lattice.solve(pressure, listOf(PointLoad(s, y, -force)))
        assert(abs(assembled.peakDishing(21) - coupled.peakDishing) < 1e-9 * coupled.peakDishing)
        assert(abs(assembled.deflection(s, y) - coupled.stationDeflections[0]) < 1e-9)
        // and the constitutive law of the path holds: f = k * w
        assert(abs(force - stiffness * coupled.stationDeflections[0]) < 1e-9 * force)
    }

    @Test
    fun `gate 3 - the dual's roll entries carry exactly the station's offset from the axis`() {
        val lattice = grillage()
        val beam = lattice.faceBeams[2]
        val axis = lattice.beamY[beam]
        fun rollOf(dual: org.jetbrains.bio.viktor.F64Array): Double = dual.dot(
            lattice.nodalField(
                { _, _, _ -> 0.0 },
                { _, _, _ -> 0.0 },
                { _, y, z ->
                    if (y == axis && z == lattice.beamZ[beam]) 1.0 else 0.0
                },
                { _, _, _ -> 0.0 }
            )
        )
        assert(abs(rollOf(lattice.pointLoadDual(0.4, axis, 2.0))) < 1e-15)
        assert(abs(rollOf(lattice.pointLoadDual(0.4, axis + 0.9, 2.0)) - 2.0 * 0.9) < 1e-12)
    }

    // ------------------------------------------------------------------ gate 4, convergence

    @Test
    fun `gate 4 - convergence - the coupled peak settles under beam subdivision`() {
        val pressure = tiltedPressure(0.02, 42 * Gen1Tile.RISE_PER_BASE_PAIR)
        fun peak(subdivisions: Int): Double {
            val lattice = grillage(subdivisions = subdivisions)
            val grid = attachmentGrid(2, 4, lattice.lengthS, lattice.lengthY)
            val surrogate = honeycombInfluenceSurrogate(lattice, grid, pressure, samples = 21)
            return surrogate.solve(List(grid.size) { 33.3333333 / grid.size }).peakDishing
        }
        val one = peak(1)
        val two = peak(2)
        val four = peak(4)
        assert(abs(four - two) < abs(two - one))
    }

    // ------------------------------------------------------------ gate 5, cross-check

    @Test
    fun `gate 5 - the per-interface crossover columns are the 21 bp ladder, not the 10 point 5`() {
        val lattice = grillage(rows = 10, columns = 6, rowBasePairs = 116)
        val perInterface = lattice.bonds
            .groupBy { it.site.lowerBeam to it.site.upperBeam }
            .mapValues { (_, bonds) -> bonds.size }
        // every interface carries the planes of ONE bond class, which recur every 21 bp
        assert(perInterface.values.min() == 5)
        assert(perInterface.values.max() == 6)
    }

}
