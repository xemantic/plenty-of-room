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
import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/** The §3 Gen-1 tile footprint. */
private const val TILE_EDGE = 40.0
private const val TILE_AREA = TILE_EDGE * TILE_EDGE

/** §3 target force, and the pressure it becomes over the nominal footprint. */
private const val TARGET_FORCE = 100.0
private const val TARGET_PRESSURE = TARGET_FORCE / TILE_AREA

/** Round rigidities, so every assertion below is a closed form and not a regression. */
private val anisotropicTile = OrthotropicPlate(
    lengthX = TILE_EDGE,
    lengthY = TILE_EDGE,
    rigidityX = 80.0,
    rigidityY = 50.0,
    twistingRigidity = 40.0
)

class PlateOnFoundationTest {

    // ---------------------------------------------------------------- gate 1

    @Test
    fun `gate 1 dimensional consistency - the Winkler length should be a fourth root of rigidity over foundation`() {
        assert(winklerLength(rigidity = 80.0, foundationStiffness = 0.005).isCloseTo(16000.0.pow(0.25)))
        // a rigidity in pN*nm over a stiffness in pN/nm^3 is nm^4, so quadrupling the
        // rigidity must lengthen ell by exactly sqrt(2)
        assert(
            winklerLength(320.0, 0.005).isCloseTo(sqrt(2.0) * winklerLength(80.0, 0.005))
        )
    }

    @Test
    fun `gate 1 dimensional consistency - the Hertz point-load deflection should reduce to force over eight root D kf`() {
        val deflection = pointLoadDeflection(
            force = TARGET_FORCE, rigidity = 80.0, foundationStiffness = 0.01
        )
        assert(deflection.isCloseTo(TARGET_FORCE / (8.0 * sqrt(80.0 * 0.01))))
    }

    // ---------------------------------------------------------------- gate 2

    /**
     * The single most important limiting case in `T-5b`, and a result rather than a check:
     * a **uniform** load on a **uniform** foundation makes a free plate translate, exactly,
     * whatever its rigidity. `w = q/k_f` has zero fourth derivative and satisfies the free-edge
     * conditions identically. So dishing cannot come from the load being carried at all —
     * it has to come from a specific asymmetry, and naming that asymmetry is the task.
     */
    @Test
    fun `gate 2 limiting cases - a uniform load on a free plate should produce no dishing at all`() {
        listOf(1e-3, 1.0, 1e6).forEach { rigidityScale ->
            val solver = PlateOnFoundation(
                plate = anisotropicTile.copy(
                    rigidityX = 80.0 * rigidityScale,
                    rigidityY = 50.0 * rigidityScale,
                    twistingRigidity = 40.0 * rigidityScale
                ),
                foundationStiffness = 0.01
            )
            val deflection = solver.solve(uniformPressure(TARGET_PRESSURE))
            assert(deflection.meanDeflection.isCloseTo(TARGET_PRESSURE / 0.01, 1e-10))
            assert(deflection.peakDishing() < 1e-9)
            assert(deflection.dishingRms < 1e-9)
        }
    }

    @Test
    fun `gate 2 limiting cases - an infinitely rigid plate should translate under a point load too`() {
        val solver = PlateOnFoundation(
            plate = anisotropicTile.copy(
                rigidityX = 1e12, rigidityY = 1e12, twistingRigidity = 1e12
            ),
            foundationStiffness = 0.01
        )
        val deflection = solver.solve(
            pointLoads = listOf(PointLoad(x = 15.0, y = -10.0, force = TARGET_FORCE))
        )
        assert(deflection.meanDeflection.isCloseTo(TARGET_FORCE / (0.01 * TILE_AREA), 1e-6))
        assert(deflection.peakDishing() < 1e-6 * deflection.meanDeflection)
    }

    @Test
    fun `gate 2 limiting cases - a load ripple should be transmitted at long wavelength and blocked at short`() {
        val ell = 10.0
        assert(loadRippleTransmission(ell, 1e6).isCloseTo(1.0, 1e-9))
        assert(loadRippleTransmission(ell, 2.0 * PI * ell).isCloseTo(0.5, 1e-12))
        assert(loadRippleTransmission(ell, 1.0) < 1e-6)
        // monotone in the wavelength: a plate is a low-pass filter, never a band-pass one
        val transmissions = listOf(2.0, 5.0, 10.0, 40.0, 200.0).map {
            loadRippleTransmission(ell, it)
        }
        assert(transmissions == transmissions.sorted())
    }

    @Test
    fun `gate 2 limiting cases - a stiffer foundation should reduce the deflection proportionally`() {
        val soft = PlateOnFoundation(anisotropicTile, 0.01).solve(uniformPressure(TARGET_PRESSURE))
        val stiff = PlateOnFoundation(anisotropicTile, 0.04).solve(uniformPressure(TARGET_PRESSURE))
        assert(stiff.meanDeflection.isCloseTo(soft.meanDeflection / 4.0, 1e-9))
    }

    // ---------------------------------------------------------------- gate 3

    @Test
    fun `gate 3 conservation - the foundation and the supports should carry exactly the applied force`() {
        val supports = listOf(
            PointSupport(-15.0, -15.0, 2.0),
            PointSupport(15.0, -15.0, 2.0),
            PointSupport(-15.0, 15.0, 2.0),
            PointSupport(15.0, 15.0, 2.0)
        )
        val solver = PlateOnFoundation(anisotropicTile, 0.01, supports)
        val deflection = solver.solve(
            pressure = uniformPressure(TARGET_PRESSURE),
            pointLoads = listOf(PointLoad(0.0, 0.0, 25.0))
        )
        val carried = deflection.foundationForce + deflection.supportForces.sum()
        assert(deflection.appliedForce.isCloseTo(TARGET_FORCE + 25.0, 1e-12))
        assert(carried.isCloseTo(deflection.appliedForce, 1e-8))
    }

    @Test
    fun `gate 3 symmetry - a symmetric load case should produce a symmetric deflected shape`() {
        val supports = listOf(
            PointSupport(-12.0, -12.0, 5.0),
            PointSupport(12.0, -12.0, 5.0),
            PointSupport(-12.0, 12.0, 5.0),
            PointSupport(12.0, 12.0, 5.0)
        )
        val deflection = PlateOnFoundation(anisotropicTile, 0.01, supports)
            .solve(uniformPressure(TARGET_PRESSURE))
        listOf(0.0 to 7.0, 3.0 to 11.0, 18.0 to 4.0).forEach { (x, y) ->
            assert(deflection.deflection(x, y).isCloseTo(deflection.deflection(-x, y), 1e-9))
            assert(deflection.deflection(x, y).isCloseTo(deflection.deflection(x, -y), 1e-9))
        }
        deflection.supportForces.forEach {
            assert(it.isCloseTo(deflection.supportForces[0], 1e-9))
        }
    }

    @Test
    fun `gate 3 symmetry - the shear crossing a cut should vanish at the free edges and by symmetry at the centre`() {
        val deflection = PlateOnFoundation(anisotropicTile, 0.01)
            .solve(uniformPressure(TARGET_PRESSURE))
        assert(abs(deflection.shearAcrossCrossoverLine(-TILE_EDGE / 2.0)) < 1e-9)
        assert(abs(deflection.shearAcrossCrossoverLine(TILE_EDGE / 2.0)) < 1e-9)
        assert(abs(deflection.shearAcrossCrossoverLine(0.0)) < 1e-9)
    }

    /**
     * Equipartition, gate 3, in the form the problem definition names: `σ² = k_BT/k`.
     * A plate stiff enough to be rigid has exactly one soft degree of freedom left,
     * the piston mode against the foundation, whose stiffness is `k_f A`.
     */
    @Test
    fun `gate 3 equipartition - a rigid plate should fluctuate exactly as a single piston mode`() {
        val solver = PlateOnFoundation(
            plate = anisotropicTile.copy(
                rigidityX = 1e12, rigidityY = 1e12, twistingRigidity = 1e12
            ),
            foundationStiffness = 0.01
        )
        val fluctuation = solver.thermalFluctuation(ROOM_TEMPERATURE)
        val expected = sqrt(thermalEnergy() / (0.01 * TILE_AREA))
        // exact, not approximate: the piston mode is P0P0, whose second derivatives vanish,
        // so it carries no bending energy and its row of the stiffness matrix is k_f A alone
        assert(fluctuation.pistonRms.isCloseTo(expected, 1e-12))
        // the two rigid tilts are equally free of bending energy, and each has stiffness
        // k_f A / 3, so their area-averaged contribution is exactly sqrt(2) pistons
        assert(fluctuation.tiltRms.isCloseTo(sqrt(2.0) * expected, 1e-12))
        assert(fluctuation.centreRms.isCloseTo(expected, 1e-6))
        assert(fluctuation.dishingRms < 1e-4 * expected)
    }

    @Test
    fun `gate 3 equipartition - a softer foundation should raise the piston fluctuation as its inverse square root`() {
        val stiff = PlateOnFoundation(anisotropicTile, 0.04).thermalFluctuation()
        val soft = PlateOnFoundation(anisotropicTile, 0.01).thermalFluctuation()
        assert(soft.pistonRms.isCloseTo(2.0 * stiff.pistonRms, 1e-9))
    }

    // ---------------------------------------------------------------- gate 4

    @Test
    fun `gate 4 numerical convergence - the point-load deflection should converge in the basis degree`() {
        val deflections = listOf(8, 12, 16, 20).map { degree ->
            PlateOnFoundation(anisotropicTile, 0.01, basisDegree = degree)
                .solve(pointLoads = listOf(PointLoad(0.0, 0.0, TARGET_FORCE)))
                .deflection(0.0, 0.0)
        }
        // Ritz restricts the trial space, so the compliance can only grow with the basis
        assert(deflections == deflections.sorted())
        val last = deflections.last()
        val previous = deflections[deflections.size - 2]
        assert(abs(last - previous) / last < 0.05)
    }

    @Test
    fun `gate 4 numerical convergence - the uniform-load answer should be exact at every basis degree`() {
        listOf(2, 6, 14).forEach { degree ->
            val deflection = PlateOnFoundation(anisotropicTile, 0.01, basisDegree = degree)
                .solve(uniformPressure(TARGET_PRESSURE))
            assert(deflection.meanDeflection.isCloseTo(TARGET_PRESSURE / 0.01, 1e-11))
        }
    }

    @Test
    fun `gate 4 numerical convergence - the thermal dishing amplitude should converge in the basis degree`() {
        val amplitudes = listOf(6, 10, 14).map {
            PlateOnFoundation(anisotropicTile, 0.01, basisDegree = it).thermalFluctuation().dishingRms
        }
        assert(amplitudes == amplitudes.sorted())
        assert(abs(amplitudes[2] - amplitudes[1]) / amplitudes[2] < 0.10)
    }

    // ---------------------------------------------------------------- gate 5

    /**
     * Gate 5 — the literature cross-check. The classical Hertz–Westergaard result for a
     * point load on an infinite plate on a Winkler foundation is `w(0) = P/(8√(D k_f))`.
     * Reproduced here on a plate wide enough for the finite size to matter little
     * (half-width ≈ 3.6 ℓ, so the edge influence is `e^(−2.5)`), which is as close to
     * "infinite" as a 40 nm tile study ever needs to get.
     */
    @Test
    fun `gate 5 literature cross-check - a wide isotropic plate should reproduce the Hertz point-load deflection`() {
        val rigidity = 8000.0
        val foundation = 0.1
        val plate = isotropicPlate(lengthX = 120.0, lengthY = 120.0, rigidity = rigidity)
        val computed = PlateOnFoundation(plate, foundation, basisDegree = 24)
            .solve(pointLoads = listOf(PointLoad(0.0, 0.0, TARGET_FORCE)))
            .deflection(0.0, 0.0)
        val hertz = pointLoadDeflection(TARGET_FORCE, rigidity, foundation)
        assert(abs(computed - hertz) / hertz < 0.30)
    }

    @Test
    fun `gate 5 literature cross-check - the isotropic plate should reduce to the Huber form with the right Poisson terms`() {
        val plate = isotropicPlate(10.0, 10.0, rigidity = 7.0, poissonRatio = 0.25)
        assert(plate.rigidityX.isCloseTo(7.0))
        assert(plate.rigidityY.isCloseTo(7.0))
        assert(plate.couplingRigidity.isCloseTo(0.25 * 7.0))
        assert(plate.twistingRigidity.isCloseTo(7.0 * 0.75 / 2.0))
    }

    // ---------------------------------------------------------------- the physics under test

    @Test
    fun `discrete supports should dish a plate that a uniform reaction leaves flat`() {
        val pressure = uniformPressure(TARGET_PRESSURE)
        val free = PlateOnFoundation(anisotropicTile, 0.01).solve(pressure)
        val anchored = PlateOnFoundation(
            anisotropicTile, 0.01,
            supports = listOf(
                PointSupport(-15.0, -15.0, 5.0),
                PointSupport(15.0, -15.0, 5.0),
                PointSupport(-15.0, 15.0, 5.0),
                PointSupport(15.0, 15.0, 5.0)
            )
        ).solve(pressure)
        assert(free.peakDishing() < 1e-9)
        assert(anchored.peakDishing() > 1e-3)
        assert(anchored.meanDeflection < free.meanDeflection)
    }

    @Test
    fun `an edge-tapered pressure should be a bounded perturbation on the uniform answer`() {
        val solver = PlateOnFoundation(anisotropicTile, 0.01)
        val uniform = solver.solve(uniformPressure(TARGET_PRESSURE))
        val tapered = solver.solve(
            edgeTaperedPressure(TARGET_PRESSURE, anisotropicTile, edgeWidth = 4.0, depth = 0.5)
        )
        assert(tapered.appliedForce < uniform.appliedForce)
        // the taper removes load, so it can only lift the tile, never push it further down
        assert(tapered.meanDeflection < uniform.meanDeflection)
        // and the dishing it produces is bounded by the removed pressure over the foundation
        assert(tapered.peakDishing() < 0.5 * TARGET_PRESSURE / 0.01)
    }

    // ---------------------------------------------------------------- validity

    @Test
    fun `a non-physical plate or foundation should be rejected on construction`() {
        assertFailsWith<IllegalArgumentException> { anisotropicTile.copy(rigidityX = 0.0) }
        assertFailsWith<IllegalArgumentException> { anisotropicTile.copy(lengthY = -1.0) }
        assertFailsWith<IllegalArgumentException> { PlateOnFoundation(anisotropicTile, 0.0) }
        assertFailsWith<IllegalArgumentException> {
            PlateOnFoundation(anisotropicTile, 0.01, basisDegree = 0)
        }
    }

}
