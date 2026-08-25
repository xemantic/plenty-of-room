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

import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.kotlin.test.assert
import org.jetbrains.bio.viktor.F64Array
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-330` — the face's three rigid modes, their Gram, and the parity that decides whether the
 * standing three-projection decomposition is the least-squares fit at all.
 *
 * `CH-0282`: `HoneycombDeflection` removes its best-fit rigid plane by three **independent**
 * projections, which is the least-squares fit **iff** the modes are mutually orthogonal.
 * `⟨piston, tiltY⟩ = ∫y dA` over the face's tributaries, and a honeycomb face is corrugated —
 * its gap sequence `d, 2d, d, 2d, …` — so that integral vanishes iff the raster-row count `m`
 * is EVEN.
 *
 * **The discriminating fixture is an ODD `m`.** Every grillage dishing test in this repository
 * before this one used `m = 4`, `6` or `10`, which is why a correct standing falsifier slept for
 * eleven iterations.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 */
class FaceRigidBasisTest {

    private val d = Gen1Tile.INTERHELICAL_HONEYCOMB

    /** A one-helix-per-row block: the face geometry of `m × n` at `1/n` of the unknowns. */
    private fun face(
        rows: Int,
        columns: Int = 1,
        rowBasePairs: Int = 42,
        faceColumn: Int = 0
    ) = HoneycombGrillage(
        block = HoneycombBlock(rows, columns),
        rowBasePairs = rowBasePairs,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        faceColumn = faceColumn
    )

    // ------------------------------------------------------------------ gate 1, dimensional

    @Test
    fun `gate 1 - dimensional - the Gram's leading entry is the face area in nm squared`() {
        val lattice = face(10)
        assert(abs(lattice.faceRigidGram[0][0] - lattice.area) < 1e-9 * lattice.area)
        // and the two tilt diagonals are the norms the standing projections already divide by
        assert(abs(lattice.faceRigidGram[1][1] - lattice.tiltSNorm) < 1e-9)
        assert(abs(lattice.faceRigidGram[2][2] - lattice.tiltYNorm) < 1e-9)
    }

    @Test
    fun `gate 1 - dimensional - the Gram is symmetric`() {
        val g = face(15).faceRigidGram
        for (i in 0..2) for (j in 0..2) assert(abs(g[i][j] - g[j][i]) < 1e-9 * (1.0 + abs(g[i][j])))
    }

    // ------------------------------------------------------------------ gate 3, symmetry

    @Test
    fun `gate 3 - symmetry - F7 the face basis is orthogonal exactly when m is even`() {
        for (m in 3..16) {
            for (column in 0..1) {
                val lattice = face(m, columns = 2, faceColumn = column)
                assert(lattice.faceRigidModesAreOrthogonal == (m % 2 == 0))
            }
        }
    }

    @Test
    fun `gate 3 - symmetry - P1 the face beam positions sum to zero at even m and to minus m minus one d over four at odd m`() {
        for (m in 3..16) {
            val lattice = face(m)
            val sum = lattice.faceBeams.sumOf { lattice.beamY[it] }
            val expected = if (m % 2 == 0) 0.0 else -(m - 1) * d / 4.0
            assert(abs(sum - expected) < 1e-12 * (1.0 + abs(expected)))
        }
    }

    @Test
    fun `gate 3 - symmetry - F8 the decomposition annihilates each of its own three basis modes`() {
        // The falsifier that needs NO SOLVE, and the one that would have caught CH-0282 at once.
        for (m in listOf(3, 4, 10, 11, 15)) {
            val lattice = face(m)
            lattice.faceRigidModes.forEachIndexed { index, mode ->
                val fitted = lattice.faceRigidCoefficients(mode)
                fitted.forEachIndexed { j, c ->
                    val target = if (j == index) 1.0 else 0.0
                    assert(abs(c - target) < 1e-9)
                }
            }
        }
    }

    @Test
    fun `gate 3 - symmetry - P2 the worst relative off-diagonal reproduces CH-0282's own two numbers`() {
        // `F2` FIRED: the quadrature Gram is NOT exactly diagonal at even `m` -- it is
        // `4.5e-16` here and `0.0` on `T-294`'s own `10 x 6` at 116 bp, so the exact zero is a
        // property of a particular lattice's roundoff and not of the parity. That is precisely
        // why the branch is taken on the INTEGER ladder and not on this number.
        assert(face(10).worstFaceNonOrthogonality < 1e-12)
        assert(face(14).worstFaceNonOrthogonality < 1e-12)
        assert(abs(face(15).worstFaceNonOrthogonality - 0.0358744468) < 5e-10)
        assert(abs(face(11).worstFaceNonOrthogonality - 0.0475958489) < 5e-10)
    }

    // ------------------------------------------------------------------ gate 2, limiting cases

    @Test
    fun `gate 2 - limiting - a single face beam is trivially orthogonal`() {
        assert(face(1).faceRigidModesAreOrthogonal)
        assert(face(1).worstFaceNonOrthogonality < 1e-12)
    }

    @Test
    fun `gate 2 - limiting - F1 at an orthogonal basis the corrected fit is bit-identical to the retained one`() {
        for (m in listOf(4, 6, 10)) {
            val lattice = face(m, columns = 2)
            val collar = PressureField { s, _ -> 0.05 * (1.0 + 0.4 * s / lattice.lengthS) }
            val cases = listOf(
                lattice.solve(uniformPressure(0.05)),
                lattice.solve(collar),
                lattice.solve(uniformPressure(0.0), listOf(PointLoad(0.0, 0.0, 1.0))),
                lattice.unitPrestrainResponse(lattice.bonds.first())
            )
            cases.forEach { field ->
                val corrected = field.dishingCoefficients
                val retained = field.independentProjectionDishingCoefficients
                for (i in 0 until lattice.degreesOfFreedom) {
                    assert(corrected[i] == retained[i])
                }
            }
        }
    }

    @Test
    fun `gate 2 - limiting - a uniform load dishes zero in the corrected convention at BOTH parities`() {
        for (m in 3..12) {
            val lattice = face(m, columns = 2)
            val solution = lattice.solve(uniformPressure(0.05))
            assert(solution.peakDishing(41) < 1e-9 * solution.meanDeflection)
        }
    }

    @Test
    fun `gate 2 - limiting - the RETAINED convention fails that falsifier at odd m and passes at even m`() {
        // The defect, kept measurable (C-0092): the retained reading is what C-0154 published.
        val odd = face(15, columns = 2).solve(uniformPressure(0.05))
        assert(odd.independentProjectionPeakDishing(41) > 1e-3 * odd.meanDeflection)
        val even = face(10, columns = 2).solve(uniformPressure(0.05))
        assert(even.independentProjectionPeakDishing(41) < 1e-9 * even.meanDeflection)
    }

    // ------------------------------------------------------------------ gate 4, numerics

    @Test
    fun `gate 4 - numerical - the three by three solve inverts a known symmetric system`() {
        val matrix = listOf(
            listOf(4.0, 1.0, 2.0),
            listOf(1.0, 5.0, 3.0),
            listOf(2.0, 3.0, 6.0)
        )
        val x = listOf(1.5, -2.0, 0.25)
        val rhs = (0..2).map { i -> (0..2).sumOf { j -> matrix[i][j] * x[j] } }
        val solved = solveSymmetricThreeByThree(matrix, rhs)
        solved.forEachIndexed { i, value -> assert(abs(value - x[i]) < 1e-12) }
    }

    @Test
    fun `gate 4 - numerical - the three by three solve refuses a singular matrix at its OWN column`() {
        // The column matters, and a bare `assertFailsWith` cannot see it. Weakening the guard to
        // `>= 0.0` still throws -- one column later, because the zero pivot makes every entry NaN
        // and `abs(NaN) >= 0.0` is false -- so the exception alone does not discriminate and the
        // mutation SURVIVED the first run of this fixture. `C-0176`: a surviving mutation is
        // usually a fixture that could not discriminate.
        val thrown = assertFailsWith<IllegalArgumentException> {
            solveSymmetricThreeByThree(
                listOf(listOf(0.0, 0.0, 0.0), listOf(0.0, 2.0, 0.0), listOf(0.0, 0.0, 3.0)),
                listOf(1.0, 1.0, 1.0)
            )
        }
        assert(thrown.message!!.contains("column 0"))
    }

    @Test
    fun `gate 4 - numerical - the Gram is built once and does not depend on the load case`() {
        val lattice = face(15, columns = 2)
        val first = lattice.faceRigidGram
        lattice.solve(uniformPressure(0.05))
        assert(lattice.faceRigidGram === first)
    }

    @Test
    fun `gate 4 - numerical - the worst off-diagonal is independent of the row length and the thickness`() {
        // it is a ratio of three integrals that all carry the axial span as a factor
        val a = face(15, columns = 2, rowBasePairs = 42).worstFaceNonOrthogonality
        val b = face(15, columns = 4, rowBasePairs = 105).worstFaceNonOrthogonality
        assert(abs(a - b) < 1e-12)
    }

    @Test
    fun `gate 4 - numerical - the face's own positions lie on the half-bond integer ladder`() {
        // the premise the orthogonality predicate rests on, asserted rather than assumed
        for (m in 3..16) {
            val lattice = face(m, columns = 2)
            lattice.faceBeams.forEach { beam ->
                val rungs = Math.round(lattice.beamY[beam] / (d / 2.0)).toDouble()
                assert(abs(lattice.beamY[beam] - rungs * d / 2.0) < 1e-9)
            }
        }
    }

    // ------------------------------------------------------------------ gate 5, cross-check

    @Test
    fun `gate 5 - cross-check - FaceRigidBasis delegates to the lattice rather than duplicating it`() {
        val lattice = face(15, columns = 2)
        val basis = FaceRigidBasis(lattice)
        assert(basis.modesAreOrthogonal == lattice.faceRigidModesAreOrthogonal)
        assert(basis.worstNonOrthogonality == lattice.worstFaceNonOrthogonality)
        val field = lattice.solve(uniformPressure(0.05))
        val viaBasis = basis.dishingOf(field)
        for (i in 0 until lattice.degreesOfFreedom) {
            assert(viaBasis.coefficients[i] == field.dishingCoefficients[i])
        }
    }

    @Test
    fun `gate 5 - cross-check - the fitted rigid coefficients reconstruct the field's own mean`() {
        val lattice = face(10, columns = 2)
        val field = lattice.solve(uniformPressure(0.05))
        // at an orthogonal basis the piston coefficient IS the area-averaged deflection
        assert(field.rigidPlaneCoefficients[0] == field.meanDeflection)
    }

    @Test
    fun `gate 5 - cross-check - a rigid plane is annihilated exactly at odd m too`() {
        val lattice = face(15, columns = 2)
        val plane = F64Array(lattice.degreesOfFreedom)
        plane += lattice.faceRigidModes[0] * 0.7
        plane += lattice.faceRigidModes[1] * 0.013
        plane += lattice.faceRigidModes[2] * -0.021
        val fitted = lattice.faceRigidCoefficients(plane)
        assert(abs(fitted[0] - 0.7) < 1e-9)
        assert(abs(fitted[1] - 0.013) < 1e-9)
        assert(abs(fitted[2] + 0.021) < 1e-9)
    }

}
