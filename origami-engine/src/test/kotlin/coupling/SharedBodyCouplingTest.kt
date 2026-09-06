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
import com.xemantic.nano.plentyofroom.structure.InterlayerCoupling
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-162` — does a coupling that is **not an array** escape `C-0089`'s count argument?
 *
 * Every test is named for the verification gate it discharges, and the falsifiers `T-162`
 * declares are asserted rather than argued:
 *
 * - **`F3`** — a **free** shared body under a **uniform** load must apply exactly zero tie force
 *   and therefore leave the tile exactly flat, which is the topology's own version of
 *   `CLAUDE.md`'s *"a uniform load on a uniform foundation dishes exactly zero"*;
 * - **`F4`** — at a rigidly grounded body the new code path must be the array's, and must
 *   reproduce `C-0017`'s 0.2182 and `C-0058`'s 0.0753.
 *
 * The closed form this file exists to pin down: a **free** rigid body tied at `n` stations
 * contributes `K_c = T − T A (AᵀTA)⁻¹ AᵀT` with `A = [1, x, y]`, of rank `max(n − 3, 0)`, so it
 * annihilates every affine tile motion and is **exactly zero** at `n = 1, 2, 3` — `CLAUDE.md`'s
 * *"a body attached at ONE point adds exactly zero"*, *"two points determine a line"* and
 * *"the arm's own EI is engaged only at three ties"* as one formula.
 */
class SharedBodyCouplingTest {

    private val duplexes = 15

    private val sheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )

    private val edgeX = Gen1Tile.EDGE_X

    private val edgeY = duplexes * sheet.interhelicalDistance

    private val interiorPressure = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)

    private val mandate = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

    /** `C-0022`'s design point at 2 mM / 10 nm / 0.192 V, as `DropoutRobustPlacementTest` has it. */
    private val solvedField = edgeCollarPressure(
        interiorPressure, edgeX, edgeY,
        listOf(CollarTerm(-0.302887367, 8.93928311), CollarTerm(-0.593889278, 1.0))
    )

    private fun lattice() = OrigamiGrillage(
        sheet = sheet,
        lengthX = edgeX,
        beamCount = duplexes,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = CrossoverLayout.centred(8, sheet.crossoverSpacing / 2.0),
        subdivisions = 2,
        supports = emptyList()
    )

    private val grid = attachmentGrid(3, duplexes, edgeX, edgeY)

    private val equal45 = List(grid.size) { mandate / grid.size }

    private val freeStroke = PlateOnFoundation(
        sheet.plate(edgeX, edgeY), Gen1Tile.FOUNDATION_SECANT, emptyList(), 12
    ).solve(uniformPressure(interiorPressure)).meanDeflection

    private val surrogate45 by lazy { latticeInfluenceSurrogate(lattice(), grid, solvedField, 81) }

    private val rigidModes = sharedBodyModes(edgeX, edgeY, 1)

    /** Four stations, deliberately not collinear, for the closed-form checks. */
    private val four = listOf(-8.0 to -6.0, 9.0 to -5.0, -7.0 to 6.5, 10.0 to 7.0)

    private fun zeros(size: Int) = Array(size) { DoubleArray(size) }

    /** A completely free body — no ground and, at the rigid modes, no bending either. */
    private fun freeRigidBody(stations: List<Pair<Double, Double>>) =
        SharedBody(rigidModes.shapesAt(stations), zeros(3))

    private fun groundedRigidBody(
        stations: List<Pair<Double, Double>>,
        total: Double = mandate
    ) = sharedBody(
        rigidModes.shapesAt(stations), zeros(3), rigidModes.distributedGroundStiffness(total)
    )

    // ------------------------------------------------------------------ gate 1: dimensional

    @Test
    fun `gate 1 - a mode set carries one shape per station per mode and refuses a bad footprint`() {
        assertFailsWith<IllegalArgumentException> { sharedBodyModes(0.0, edgeY, 1) }
        assertFailsWith<IllegalArgumentException> { sharedBodyModes(edgeX, -1.0, 1) }
        assertFailsWith<IllegalArgumentException> { sharedBodyModes(edgeX, edgeY, 0) }
        assert(rigidModes.modeCount == 3)
        assert(sharedBodyModes(edgeX, edgeY, 2).modeCount == 6)
        assert(sharedBodyModes(edgeX, edgeY, 3).modeCount == 10)
        val shapes = rigidModes.shapesAt(four)
        assert(shapes.size == four.size)
        assert(shapes.all { it.size == 3 })
        // The heave mode is dimensionless unity everywhere; the tilts are the reduced coordinates.
        assert(shapes.all { it[0] == 1.0 })
        assert(shapes[1][1].isCloseTo(2.0 * 9.0 / edgeX, 1e-15))
        assert(shapes[1][2].isCloseTo(2.0 * -5.0 / edgeY, 1e-15))
        assertFailsWith<IllegalArgumentException> { rigidModes.shapesAt(emptyList()) }
    }

    @Test
    fun `gate 1 - the modal ground of a distributed element is the mandate over the odd numbers`() {
        val modes = sharedBodyModes(edgeX, edgeY, 2)
        val ground = modes.distributedGroundStiffness(mandate)
        assertFailsWith<IllegalArgumentException> { modes.distributedGroundStiffness(0.0) }
        // `∫P_a P_b = 2δ/(2a+1)`, so a Winkler ground of total K is `K/((2a+1)(2b+1))`, diagonal.
        assert(ground[0][0].isCloseTo(mandate, 1e-12))
        for (m in 0 until modes.modeCount) for (n in 0 until modes.modeCount) {
            if (m != n) assert(abs(ground[m][n]) < 1e-12 * mandate)
        }
        assert(ground[1][1].isCloseTo(mandate / 3.0, 1e-12))
        assert(ground[2][2].isCloseTo(mandate / 3.0, 1e-12))
        assert(ground[0][0] > ground[1][1])
    }

    @Test
    fun `gate 1 - a shared-body compliance is a symmetric nm per pN matrix at the stations`() {
        val compliance = sharedBodyCompliance(
            rigidModes.shapesAt(four), rigidModes.distributedGroundStiffness(mandate)
        )
        assert(compliance.size == four.size)
        assert(compliance.all { it.size == four.size })
        for (i in four.indices) for (j in four.indices) {
            assert(abs(compliance[i][j] - compliance[j][i]) < 1e-12 * abs(compliance[i][i]))
        }
        // A rigid body on a distributed ground of total K has heave compliance exactly 1/K at
        // the footprint centre, and that is a unit check as well as a limit.
        val centre = sharedBodyCompliance(
            rigidModes.shapesAt(listOf(0.0 to 0.0)),
            rigidModes.distributedGroundStiffness(mandate)
        )
        assert(centre[0][0].isCloseTo(1.0 / mandate, 1e-9))
    }

    @Test
    fun `gate 1 - a condensation refuses a mismatched tie set or presence vector`() {
        val body = groundedRigidBody(four)
        assertFailsWith<IllegalArgumentException> {
            sharedBodyCouplingMatrix(List(3) { 1.0 }, body, List(3) { true })
        }
        assertFailsWith<IllegalArgumentException> {
            sharedBodyCouplingMatrix(List(4) { 1.0 }, body, List(3) { true })
        }
        assertFailsWith<IllegalArgumentException> {
            sharedBodyCouplingMatrix(List(4) { 0.0 }, body, List(4) { true })
        }
        assertFailsWith<IllegalArgumentException> {
            couplingStiffnessMatrix(List(4) { 1.0 }, zeros(3), List(4) { true })
        }
    }

    @Test
    fun `gate 1 - the mandate arithmetic is a stiffness over a stiffness and needs no solve`() {
        val arithmetic = mandatePlacementArithmetic(
            34, mandate, Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE
        )
        assert(arithmetic.arrayPerStation.isCloseTo(mandate / 34.0, 1e-12))
        assert(arithmetic.sharedBodyPerStation.isCloseTo(10.0 / 3.0, 1e-12))
        assert(arithmetic.ratio.isCloseTo(arithmetic.sharedBodyPerStation / (mandate / 34.0), 1e-12))
        assertFailsWith<IllegalArgumentException> {
            mandatePlacementArithmetic(0, mandate, 10.0, 3.0)
        }
    }

    // ------------------------------------------------------------------ gate 2: limiting cases

    @Test
    fun `gate 2 - F4 - an infinitely well grounded body is the array, in the limit`() {
        val ties = List(four.size) { 3.0 + it }
        var previous = Double.MAX_VALUE
        val array = Array(four.size) { i -> DoubleArray(four.size) { j ->
            if (i == j) ties[i] else 0.0
        } }
        listOf(1e3, 1e6, 1e9).forEach { scale ->
            val matrix = sharedBodyCouplingMatrix(ties, groundedRigidBody(four, mandate * scale))
            val departure = matrixDeparture(matrix, array)
            assert(departure < previous)
            previous = departure
        }
        assert(previous < 1e-6)
    }

    @Test
    fun `gate 2 - P1 - a FREE rigid body adds exactly zero at one, two and three ties`() {
        val stations = listOf(-8.0 to -6.0, 9.0 to -5.0, -7.0 to 6.5)
        (1..3).forEach { count ->
            val used = stations.take(count)
            val ties = List(count) { 12.0 }
            val matrix = sharedBodyCouplingMatrix(ties, freeRigidBody(used))
            val worst = matrix.flatMap { row -> row.map { abs(it) } }.max()
            assert(worst < 1e-9 * ties.max())
        }
    }

    @Test
    fun `gate 2 - P1 - a FREE rigid body annihilates every affine tile motion at four ties`() {
        val ties = List(four.size) { 12.0 + 3.0 * it }
        val matrix = sharedBodyCouplingMatrix(ties, freeRigidBody(four))
        val scale = ties.max()
        listOf(
            DoubleArray(four.size) { 1.0 },
            DoubleArray(four.size) { four[it].first },
            DoubleArray(four.size) { four[it].second }
        ).forEach { motion ->
            four.indices.forEach { i ->
                var force = 0.0
                four.indices.forEach { j -> force += matrix[i][j] * motion[j] }
                assert(abs(force) < 1e-7 * scale * edgeX)
            }
        }
        // And it is NOT the zero matrix: rank n − 3 = 1 here, so something survives.
        assert(matrix.flatMap { row -> row.map { abs(it) } }.max() > 0.1 * scale)
    }

    @Test
    fun `gate 2 - P1 - the free rigid body reproduces its own closed form at four ties`() {
        val ties = List(four.size) { 12.0 + 3.0 * it }
        val matrix = sharedBodyCouplingMatrix(ties, freeRigidBody(four))
        // `K = T − T A (AᵀTA)⁻¹ AᵀT`, assembled here by hand on a 3 × 3 so that nothing in the
        // production path is reused to check itself.
        val a = Array(four.size) { doubleArrayOf(1.0, four[it].first, four[it].second) }
        val ata = Array(3) { p ->
            DoubleArray(3) { q -> four.indices.sumOf { i -> a[i][p] * ties[i] * a[i][q] } }
        }
        val inverse = invertThreeByThree(ata)
        val expected = Array(four.size) { i ->
            DoubleArray(four.size) { j ->
                var correction = 0.0
                for (p in 0..2) for (q in 0..2) {
                    correction += ties[i] * a[i][p] * inverse[p][q] * a[j][q] * ties[j]
                }
                (if (i == j) ties[i] else 0.0) - correction
            }
        }
        assert(matrixDeparture(matrix, expected) < 1e-7)
    }

    @Test
    fun `gate 2 - an absent tie leaves the coupling matrix and the body's own load path`() {
        val ties = List(four.size) { 12.0 }
        val present = listOf(true, false, true, true)
        val matrix = sharedBodyCouplingMatrix(ties, groundedRigidBody(four), present)
        four.indices.forEach { i ->
            assert(matrix[1][i] == 0.0)
            assert(matrix[i][1] == 0.0)
        }
        // What survives is the three-station coupling, not a submatrix of the four-station one:
        // the body redistributes, which is the whole claim of the topology.
        val threeStations = listOf(four[0], four[2], four[3])
        val three = sharedBodyCouplingMatrix(List(3) { 12.0 }, groundedRigidBody(threeStations))
        val map = listOf(0, 2, 3)
        for (i in 0..2) for (j in 0..2) {
            assert(abs(matrix[map[i]][map[j]] - three[i][j]) < 1e-9 * 12.0)
        }
    }

    @Test
    fun `gate 2 - a shared body's coupling is never stiffer than its own ties`() {
        val ties = List(four.size) { 12.0 + 3.0 * it }
        val matrix = sharedBodyCouplingMatrix(ties, groundedRigidBody(four))
        four.indices.forEach { assert(matrix[it][it] <= ties[it] + 1e-9) }
        assert(couplingHeaveSecant(matrix) < ties.sum())
    }

    // ------------------------------------------------------- gate 3: symmetry and conservation

    @Test
    fun `gate 3 - F3 - a FREE shared body under a UNIFORM load applies exactly zero tie force`() {
        val uniform = latticeInfluenceSurrogate(
            lattice(), grid, uniformPressure(interiorPressure), 41
        )
        val coupled = uniform.solveWithSharedBody(
            List(grid.size) { 50.0 }, freeRigidBody(grid), List(grid.size) { true }
        )
        val scale = Gen1Tile.TARGET_FORCE
        assert(coupled.supportForces.all { abs(it) < 1e-9 * scale })
        assert(coupled.peakDishing < 1e-9)
        // And the uncoupled tile is flat too, which is the standing falsifier it rests on.
        assert(uniform.solveWithSharedBody(List(grid.size) { 1e-30 }, null, List(grid.size) { true })
            .peakDishing < 1e-9)
    }

    @Test
    fun `gate 3 - the two condensation routes agree where both are defined`() {
        val ties = List(four.size) { 7.0 + 2.0 * it }
        val modes = sharedBodyModes(edgeX, edgeY, 3)
        val body = sharedBody(
            modes.shapesAt(four),
            modes.bendingStiffness(sheet.plate(edgeX, edgeY)),
            modes.distributedGroundStiffness(mandate)
        )
        val condensed = sharedBodyCouplingMatrix(ties, body)
        val series = couplingStiffnessMatrix(
            ties, sharedBodyCompliance(body.shapes, body.modalStiffness)
        )
        assert(matrixDeparture(condensed, series) < 1e-8)
    }

    @Test
    fun `gate 3 - the modal stiffness and the condensed compliance are both reciprocal`() {
        val modes = sharedBodyModes(edgeX, edgeY, 3)
        val body = sharedBody(
            modes.shapesAt(four),
            modes.bendingStiffness(sheet.plate(edgeX, edgeY)),
            modes.distributedGroundStiffness(mandate)
        )
        for (m in 0 until modes.modeCount) for (n in 0 until modes.modeCount) {
            val scale = maxOf(
                abs(body.modalStiffness[m][m]), abs(body.modalStiffness[n][n]), 1e-30
            )
            assert(
                abs(body.modalStiffness[m][n] - body.modalStiffness[n][m]) < 1e-10 * scale
            )
        }
        val compliance = sharedBodyCompliance(body.shapes, body.modalStiffness)
        for (i in four.indices) for (j in four.indices) {
            assert(abs(compliance[i][j] - compliance[j][i]) < 1e-9 * abs(compliance[i][i]))
        }
    }

    @Test
    fun `gate 3 - the mandate is placed on the body's ground and the ties are left free`() {
        val ties = List(grid.size) { 50.0 }
        val placement = placeSharedBodyGround(
            ties, rigidModes.shapesAt(grid), zeros(3),
            rigidModes.distributedGroundStiffness(1.0), mandate
        )
        assert(placement.heaveSecant.isCloseTo(mandate, 1e-9))
        assert(placement.tieSecantCeiling.isCloseTo(50.0 * grid.size, 1e-12))
        // Almost the whole coupling compliance lives in the ground, which is the escape in a line.
        assert(placement.groundComplianceShare > 0.95)
        assertFailsWith<IllegalArgumentException> {
            placeSharedBodyGround(
                List(grid.size) { 0.1 }, rigidModes.shapesAt(grid), zeros(3),
                rigidModes.distributedGroundStiffness(1.0), mandate
            )
        }
    }

    @Test
    fun `gate 3 - a softer body is a larger compliance and never a smaller one`() {
        val modes = sharedBodyModes(edgeX, edgeY, 3)
        val ground = modes.distributedGroundStiffness(mandate)
        fun trace(multiplier: Double): Double {
            val bending = modes.bendingStiffness(sheet.plate(edgeX, edgeY))
            val total = Array(modes.modeCount) { m ->
                DoubleArray(modes.modeCount) { n -> multiplier * bending[m][n] + ground[m][n] }
            }
            val compliance = sharedBodyCompliance(modes.shapesAt(four), total)
            return four.indices.sumOf { compliance[it][it] }
        }
        assert(trace(1.0) > trace(10.0))
        assert(trace(10.0) > trace(100.0))
    }

    // -------------------------------------------------------------------- gate 4: convergence

    @Test
    fun `gate 4 - the condensed compliance settles as the Ritz basis is enriched`() {
        val values = listOf(2, 3, 4, 5).map { degree ->
            val modes = sharedBodyModes(edgeX, edgeY, degree)
            val body = sharedBody(
                modes.shapesAt(four),
                modes.bendingStiffness(sheet.plate(edgeX, edgeY)),
                modes.distributedGroundStiffness(mandate)
            )
            sharedBodyCompliance(body.shapes, body.modalStiffness)[0][0]
        }
        // A richer Ritz space can only soften the body: `φᵀH⁻¹φ` is a maximum over the retained
        // coordinates, and enlarging the space can only raise it.
        (1 until values.size).forEach { assert(values[it] >= values[it - 1] - 1e-12) }
        val first = abs(values[1] - values[0])
        val last = abs(values[values.size - 1] - values[values.size - 2])
        assert(last < first)
    }

    @Test
    fun `gate 4 - a four-layer body is stiffer than a single-layer one at every non-rigid mode`() {
        val modes = sharedBodyModes(edgeX, edgeY, 3)
        val single = modes.bendingStiffness(sheet.plate(edgeX, edgeY))
        val brick = origamiSheet(
            Gen1Tile.INTERHELICAL_HONEYCOMB, Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
            layers = 4, interlayerCoupling = InterlayerCoupling.RIGID
        )
        val stack = modes.bendingStiffness(brick.plate(edgeX, edgeY))
        var stiffer = 0
        for (m in 0 until modes.modeCount) if (stack[m][m] > single[m][m]) stiffer++
        assert(stiffer == modes.modeCount - 3)
        // The three rigid modes carry no bending energy at all, in either body.
        for (m in 0..2) {
            assert(abs(single[m][m]) < 1e-9)
            assert(abs(stack[m][m]) < 1e-9)
        }
    }

    // ------------------------------------------------------------- gate 5: upstream cross-check

    @Test
    fun `gate 5 - F4 - the array corner still reproduces C-0017's 0_2182 and C-0058's 0_0753`() {
        val all = List(grid.size) { true }
        assert(
            (surrogate45.solveWithSharedBody(equal45, null, all).peakDishing / freeStroke)
                .isCloseTo(0.2182, 1e-3)
        )
        val twoLevel = normalisedStiffnesses(
            rimStiffenedWeights(grid, edgeX, edgeY, 6.7, 5.0), mandate
        )
        assert(
            (surrogate45.solveWithSharedBody(twoLevel, null, all).peakDishing / freeStroke)
                .isCloseTo(0.0753, 1e-3)
        )
    }

    @Test
    fun `gate 5 - F4 - a rigidly grounded body reproduces the array's own dropout solve`() {
        val present = List(grid.size) { it % 7 != 3 }
        val standing = surrogate45.solveWithDropout(equal45, present)
        val rigid = surrogate45.solveWithSharedBody(
            equal45, groundedRigidBody(grid, mandate * 1e12), present
        )
        assert(abs(rigid.peakDishing - standing.peakDishing) < 1e-6 * standing.peakDishing)
    }

    @Test
    fun `gate 5 - C-0026's free-tile stroke is reproduced rather than transcribed`() {
        assert(freeStroke.isCloseTo(4.90731, 1e-5))
    }

    /** A 3 × 3 inverse written out, so that the closed-form check reuses no production code. */
    private fun invertThreeByThree(m: Array<DoubleArray>): Array<DoubleArray> {
        val determinant =
            m[0][0] * (m[1][1] * m[2][2] - m[1][2] * m[2][1]) -
                    m[0][1] * (m[1][0] * m[2][2] - m[1][2] * m[2][0]) +
                    m[0][2] * (m[1][0] * m[2][1] - m[1][1] * m[2][0])
        val cofactor = arrayOf(
            doubleArrayOf(
                m[1][1] * m[2][2] - m[1][2] * m[2][1],
                m[0][2] * m[2][1] - m[0][1] * m[2][2],
                m[0][1] * m[1][2] - m[0][2] * m[1][1]
            ),
            doubleArrayOf(
                m[1][2] * m[2][0] - m[1][0] * m[2][2],
                m[0][0] * m[2][2] - m[0][2] * m[2][0],
                m[0][2] * m[1][0] - m[0][0] * m[1][2]
            ),
            doubleArrayOf(
                m[1][0] * m[2][1] - m[1][1] * m[2][0],
                m[0][1] * m[2][0] - m[0][0] * m[2][1],
                m[0][0] * m[1][1] - m[0][1] * m[1][0]
            )
        )
        return Array(3) { i -> DoubleArray(3) { j -> cofactor[i][j] / determinant } }
    }
}
