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
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.anchoring.MeasuredBackbone
import com.xemantic.nano.plentyofroom.structure.minimumTurnPhosphateSpan
import com.xemantic.nano.plentyofroom.structure.turnPhosphateSpan
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-291` — the allowed departure is **common-mode**, and what replaces it is a per-beam **twist**.
 *
 * Written before `tile/RasterTurnTwistEigenstrain.kt` and before
 * `HoneycombGrillage.beamTwistResponse`, and watched fail.
 *
 * The gates each test names are `T-291`'s own `F1`–`F11`. The solve-based ones run on a small
 * block here; the recommended `10 × 6` one lives in the study, where the reproductions against
 * `C-0175` §8 and `C-0187` are taken.
 */
class RasterTurnTwistEigenstrainTest {

    private val d = 2.536
    private val rP = MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS
    private val delta = allowedScaffoldCrossoverDepartureDegrees()

    private val block = HoneycombBlock(4, 2)

    private val signs = HoneycombRasterTurnSigns(block, 102, 109)

    private val recommended = HoneycombRasterTurnSigns(HoneycombBlock(10, 6), 102, 109)

    private fun lattice(
        ties: List<HoneycombScaffoldTurnTie> = emptyList(),
        foundation: Double = Gen1Tile.FOUNDATION_SECANT,
        subdivisions: Int = 1
    ) = HoneycombGrillage(
        block = block,
        rowBasePairs = 116,
        foundationStiffness = foundation,
        subdivisions = subdivisions,
        scaffoldTurnTies = ties
    )

    // ------------------------------------------------- F1: the relative azimuth is level-free

    @Test
    fun `F1 - a level displacement leaves the two backbones exactly antipodal`() {
        // ForcedCrossoverPrice's own construction, read as the algebra CH-0240 section 2 states:
        // the corpus's span is taken at (theta, 180 + theta), so the RELATIVE azimuth is exactly
        // 180 degrees at every displacement and its derivative in the level is identically zero.
        (-21..21).forEach { k ->
            val theta = azimuthalDepartureDegrees(k)
            // the algebra is exact and its floating-point evaluation is not: `(180 + x) - x`
            // loses the last ulp of x, which is why this is a tolerance and not an equality
            assert(abs(honeycombBondRelativeAzimuthDegrees(k) - 180.0) < 1e-12)
            assert(
                abs(
                    forcedCrossoverSpan(d, rP, theta) -
                            turnPhosphateSpan(d, rP, theta, 180.0 + theta)
                ) < 1e-15
            )
        }
    }

    @Test
    fun `F1 - the common-mode relief reaches the minimum span exactly`() {
        // rolling BOTH duplexes by minus the departure puts both backbones back on the line of
        // centres, which is the minimum of the span over every azimuth pair.
        val relieved = turnPhosphateSpan(d, rP, delta - delta, 180.0 + delta - delta)
        assert(abs(relieved - minimumTurnPhosphateSpan(d, rP)) < 1e-15)
    }

    // ------------------------------------------------- F3: the relative channel cannot do it

    @Test
    fun `F3 - no relative roll reaches the span the common-mode relief reaches`() {
        val floor = minimumTurnPhosphateSpan(d, rP)
        var best = Double.MAX_VALUE
        (-3600..3600).forEach { i ->
            // a pure relative roll: the two duplexes counter-rotate about their own axes, so
            // the MEAN azimuth is unmoved and only the difference changes.
            val r = i / 100.0
            val span = turnPhosphateSpan(d, rP, delta + r, 180.0 + delta - r)
            best = minOf(best, span)
        }
        assert(best > floor + 1e-6)
        assert(relativeRollSpanShortfall(d, rP, delta) > 1e-6)
    }

    @Test
    fun `F3 - the shortfall vanishes exactly when the departure does`() {
        assert(relativeRollSpanShortfall(d, rP, 0.0) < 1e-12)
    }

    // ------------------------------------------------- F2: the applied load is orthogonal to it

    @Test
    fun `F2 - the tie prestrain load is orthogonal to the demanded common-mode roll at every tie`() {
        val nodes = lattice().nodesPerBeam
        val armed = lattice(signs.ties(nodes, phase = 1))
        val bare = armed.withoutPrestrain
        val pressure = uniformPressure(0.0)
        val a = armed.assembleLoad(pressure)
        val b = bare.assembleLoad(pressure)
        val load = DoubleArray(armed.degreesOfFreedom) { a[it] - b[it] }
        // the load must not be vacuously zero, or the orthogonality says nothing
        assert(load.any { abs(it) > 1e-12 })
        armed.turnElements.forEach { element ->
            val upper = commonModeDof(armed, element.node, element.tie.upperBeam)
            val lower = commonModeDof(armed, element.node, element.tie.lowerBeam)
            // the demanded kinematics at a tie is a roll of BOTH beams the same way
            assert(load[upper] + load[lower] == 0.0)
        }
    }

    @Test
    fun `F2 - and it is exactly the relative direction, which the demand does not contain`() {
        val nodes = lattice().nodesPerBeam
        val armed = lattice(signs.ties(nodes, phase = 1))
        val bare = armed.withoutPrestrain
        val pressure = uniformPressure(0.0)
        val a = armed.assembleLoad(pressure)
        val b = bare.assembleLoad(pressure)
        val load = DoubleArray(armed.degreesOfFreedom) { a[it] - b[it] }
        val phiOnly = load.indices.all {
            it % HoneycombGrillage.DOF_PER_NODE == HoneycombGrillage.PHI || load[it] == 0.0
        }
        assert(phiOnly)
        armed.turnElements.forEachIndexed { k, element ->
            val upper = commonModeDof(armed, element.node, element.tie.upperBeam)
            val couple = armed.hingeStiffness * element.tie.prestrainRadians
            assert(abs(load[upper] - couple) < 1e-12)
            assert(k >= 0)
        }
    }

    // ------------------------------------------------- F4: the derived twist demand

    @Test
    fun `F4 - the demanded relief roll is the negation of the departure at BOTH duplexes`() {
        val demands = honeycombTurnRollDemands(recommended, phase = 1)
        assert(demands.size == 59)
        recommended.signs.forEachIndexed { k, sign ->
            assert(abs(demands[k].rollDegrees + sign.departureDegrees) < 1e-12)
            assert(demands[k].lowerBeam < demands[k].upperBeam)
        }
    }

    @Test
    fun `F4 - every interior beam carries the same twist and the two raster termini carry none`() {
        val twists = honeycombBeamTwistDemands(recommended, phase = 1)
        assert(twists.size == 60)
        val free = twists.filter { it.twistDegrees == 0.0 }
        assert(free.size == 2)
        val interior = twists.filterNot { it.twistDegrees == 0.0 }
        assert(interior.size == 58)
        interior.forEach {
            assert(abs(abs(it.twistDegrees) - 2.0 * delta) < 1e-12)
            assert(it.twistDegrees == interior.first().twistDegrees)
        }
    }

    @Test
    fun `F4 - the twist magnitude is invariant over all eight readings of the free conventions`() {
        listOf(1, -1).forEach { first ->
            listOf(false, true).forEach { mirrored ->
                listOf(false, true).forEach { reversed ->
                    val s = HoneycombRasterTurnSigns(
                        HoneycombBlock(10, 6), 102, 109, first, mirrored, reversed
                    )
                    val twists = honeycombBeamTwistDemands(s, phase = 1)
                    val interior = twists.filterNot { it.twistDegrees == 0.0 }
                    assert(interior.size == 58)
                    assert(interior.all { abs(abs(it.twistDegrees) - 2.0 * delta) < 1e-12 })
                    assert(interior.all { it.twistDegrees == interior.first().twistDegrees })
                }
            }
        }
    }

    @Test
    fun `F4 - the global phase negates every beam's twist and nothing else`() {
        val plus = honeycombBeamTwistDemands(recommended, phase = 1)
        val minus = honeycombBeamTwistDemands(recommended, phase = -1)
        plus.indices.forEach { assert(plus[it].twistDegrees == -minus[it].twistDegrees) }
        assertFailsWith<IllegalArgumentException> {
            honeycombBeamTwistDemands(recommended, phase = 0)
        }
    }

    @Test
    fun `the restrained energy is the rigid-duplex ceiling and it falls with the beam length`() {
        val theta = Math.toRadians(2.0 * delta)
        val short = beamTwistRestrainedEnergy(
            Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY, 102 * Gen1Tile.RISE_PER_BASE_PAIR, theta
        )
        val long = beamTwistRestrainedEnergy(
            Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY, 109 * Gen1Tile.RISE_PER_BASE_PAIR, theta
        )
        assert(short > long)
        assert(abs(short - 0.5 * (460.0 / 34.68) * theta * theta) < 1e-12)
        assert(beamTwistRestrainedEnergy(460.0, 34.68, 0.0) == 0.0)
        assertFailsWith<IllegalArgumentException> { beamTwistRestrainedEnergy(0.0, 34.68, theta) }
    }

    // ------------------------------------------------- F5 and the load placement

    @Test
    fun `F5 - the twist response is exactly linear in the eigenstrain`() {
        val grid = lattice()
        val one = grid.beamTwistResponse(mapOf(1 to 0.01, 3 to -0.02))
        val two = grid.beamTwistResponse(mapOf(1 to 0.02, 3 to -0.04))
        val zero = grid.beamTwistResponse(emptyMap())
        var worst = 0.0
        (0..12).forEach { i ->
            (0..12).forEach { j ->
                val s = grid.lengthS * (i / 12.0 - 0.5)
                val y = grid.lengthY * (j / 12.0 - 0.5)
                worst = maxOf(worst, abs(two.deflection(s, y) - 2.0 * one.deflection(s, y)))
                worst = maxOf(worst, abs(zero.deflection(s, y)))
            }
        }
        assert(worst < 1e-9)
    }

    @Test
    fun `the eigenstrain load telescopes to one couple pair at the beam's two ends`() {
        listOf(1, 2).forEach { subdivisions ->
            val grid = lattice(subdivisions = subdivisions)
            val twist = 0.05
            val beam = 2
            val response = grid.beamTwistResponse(mapOf(beam to twist))
            val reconstructed = stiffnessTimes(grid, response.coefficients)
            val expected = Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY * twist /
                    (grid.nodeS.last() - grid.nodeS.first())
            val high = commonModeDof(grid, grid.nodesPerBeam - 1, beam)
            val low = commonModeDof(grid, 0, beam)
            assert(abs(reconstructed[high] - expected) < 1e-6)
            assert(abs(reconstructed[low] + expected) < 1e-6)
            var elsewhere = 0.0
            // the axial rigid mode is pinned at `dof(0, axialPinBeam, U)`, whose row the
            // factorisation replaces by the identity, so K u there is not the applied load
            val pinned = HoneycombGrillage.U
            reconstructed.indices.forEach {
                if (it != high && it != low && it != pinned) {
                    elsewhere = maxOf(elsewhere, abs(reconstructed[it]))
                }
            }
            assert(elsewhere < 1e-6)
        }
    }

    @Test
    fun `beamTwistResponse refuses a beam outside the block and a non-finite twist`() {
        val grid = lattice()
        assertFailsWith<IllegalArgumentException> { grid.beamTwistResponse(mapOf(-1 to 0.01)) }
        assertFailsWith<IllegalArgumentException> {
            grid.beamTwistResponse(mapOf(grid.beamCount to 0.01))
        }
        assertFailsWith<IllegalArgumentException> {
            grid.beamTwistResponse(mapOf(0 to Double.NaN))
        }
    }

    // ------------------------------------------------- F6: the limiting case, and the sign

    @Test
    fun `F6 - a uniform twist relaxes into the twisted ribbon as the foundation vanishes`() {
        val twist = 0.02
        val residuals = listOf(1e-3, 1e-5, 1e-7).map { scale ->
            val grid = lattice(foundation = Gen1Tile.FOUNDATION_SECANT * scale)
            val response = grid.beamTwistResponse(
                (0 until grid.beamCount).associateWith { twist }
            )
            var worst = 0.0
            (0..12).forEach { i ->
                (0..12).forEach { j ->
                    val s = grid.lengthS * (i / 12.0 - 0.5)
                    val y = grid.lengthY * (j / 12.0 - 0.5)
                    val ribbon = y * twist * s / (grid.nodeS.last() - grid.nodeS.first())
                    worst = maxOf(worst, abs(response.dishing(s, y) - ribbon))
                }
            }
            worst
        }
        // the sign is fixed by the comparison, not merely the magnitude
        assert(residuals[0] > residuals[1])
        assert(residuals[1] > residuals[2])
        assert(residuals[2] < 1e-4)
    }

    // ------------------------------------------------- F7: the standing falsifier, both ways

    @Test
    fun `F7 - a uniform pressure on the tied zero-eigenstrain lattice dishes exactly zero`() {
        val nodes = lattice().nodesPerBeam
        val tied = lattice(honeycombScaffoldTurnTies(block, nodes))
        assert(tied.solve(uniformPressure(0.02)).peakDishing(41) < 1e-9)
    }

    @Test
    fun `F7 - and a uniform TWIST eigenstrain does NOT inherit it`() {
        // CLAUDE.md: a uniform eigenstrain is not a uniform load. Here the state that relaxes
        // every hinge and every link at once is a twisted RIBBON, which is a saddle and not a
        // rigid mode, so the dishing is bounded away from zero rather than exactly zero.
        val grid = lattice()
        val response = grid.beamTwistResponse((0 until grid.beamCount).associateWith { 0.02 })
        assert(response.peakDishing(41) > 1e-3)
    }

    // ------------------------------------------------- helpers

    private fun commonModeDof(grid: HoneycombGrillage, node: Int, beam: Int): Int =
        (node * grid.beamCount + beam) * HoneycombGrillage.DOF_PER_NODE + HoneycombGrillage.PHI

    @Test
    fun `the reconstructed degree-of-freedom layout is the lattice's own`() {
        // CLAUDE.md: a private DOF layout is reconstructible from the public API and must be
        // asserted against the class's own basis vector rather than assumed.
        val grid = lattice()
        val piston = grid.pistonMode
        (0 until grid.nodesPerBeam).forEach { node ->
            (0 until grid.beamCount).forEach { beam ->
                val phi = commonModeDof(grid, node, beam)
                assert(piston[phi] == 0.0)
                assert(piston[phi - HoneycombGrillage.PHI + HoneycombGrillage.W] == 1.0)
            }
        }
        assert(grid.degreesOfFreedom == grid.nodesPerBeam * grid.beamCount * 4)
    }

    private fun stiffnessTimes(grid: HoneycombGrillage, field: org.jetbrains.bio.viktor.F64Array):
            DoubleArray {
        val n = grid.degreesOfFreedom
        val out = DoubleArray(n)
        for (i in 0 until n) {
            var total = 0.0
            for (j in maxOf(0, i - grid.bandwidth)..minOf(n - 1, i + grid.bandwidth)) {
                val entry =
                    if (j <= i) grid.stiffnessEntry(i, j) else grid.stiffnessEntry(j, i)
                total += entry * field[j]
            }
            out[i] = total
        }
        return out
    }
}
