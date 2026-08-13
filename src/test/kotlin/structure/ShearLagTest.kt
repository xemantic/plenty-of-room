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
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-15`'s continuum control, gate by gate.
 *
 * The orthotropic shear-lag membrane is what the in-plane lattice discretises, and
 * `CLAUDE.md`'s standing rule is that a lattice effect is claimed only with the continuum
 * run beside it. Its `n = 0` mode **is** the equal share, which is what makes the
 * concentration factor readable off the series rather than asserted.
 */

private const val STRETCH_MODULUS = Gen1Tile.DUPLEX_STRETCH_MODULUS

private const val INTERHELICAL = Gen1Tile.INTERHELICAL_SHEET

private val CROSSOVER_SPACING =
    Gen1Tile.CROSSOVER_SPACING_SHEET_BP * Gen1Tile.RISE_PER_BASE_PAIR

private const val DUPLEXES = 15

private val NOMINAL_SHEAR = Gen1Tile.crossoverInPlaneStiffness()

private fun membrane(
    crossoverShearStiffness: Double = NOMINAL_SHEAR,
    lengthX: Double = Gen1Tile.EDGE_X,
    modes: Int = 600
) = ShearLagMembrane(
    stretchModulus = STRETCH_MODULUS,
    interhelicalDistance = INTERHELICAL,
    crossoverSpacing = CROSSOVER_SPACING,
    crossoverShearStiffness = crossoverShearStiffness,
    lengthX = lengthX,
    duplexes = DUPLEXES,
    modes = modes
)

private fun edgeChord(force: Double = 1.0) = ChordLoad(
    fromX = -Gen1Tile.EDGE_X / 2.0, fromY = 0.0,
    toX = Gen1Tile.EDGE_X / 2.0, toY = 0.0, force = force
)

class ShearLagTest {

    // ------------------------------------------------------------------ gate 1

    @Test
    fun `gate 1 the transfer length is a length and scales as the inverse root of the coupling`() {
        val short = shearLagTransferLength(STRETCH_MODULUS, CROSSOVER_SPACING, 400.0)
        val long = shearLagTransferLength(STRETCH_MODULUS, CROSSOVER_SPACING, 100.0)
        // quartering the crossover stiffness doubles the transfer length, exactly
        assert((long / short).isCloseTo(2.0))
        assert(long.isCloseTo(sqrt(STRETCH_MODULUS * CROSSOVER_SPACING / 100.0)))
    }

    @Test
    fun `gate 1 the neighbour exchange length is the transfer length over root two`() {
        val transfer = shearLagTransferLength(STRETCH_MODULUS, CROSSOVER_SPACING, 64.0)
        val neighbour = shearLagNeighbourLength(STRETCH_MODULUS, CROSSOVER_SPACING, 64.0)
        assert((transfer / neighbour).isCloseTo(sqrt(2.0)))
    }

    @Test
    fun `gate 1 the shear lag aspect ratio is dimensionless and is the transfer length in duplexes`() {
        val ratio = shearLagAspectRatio(
            STRETCH_MODULUS, CROSSOVER_SPACING, 64.0, INTERHELICAL
        )
        assert(
            ratio.isCloseTo(
                shearLagTransferLength(STRETCH_MODULUS, CROSSOVER_SPACING, 64.0) / INTERHELICAL
            )
        )
    }

    @Test
    fun `gate 1 arguments must be positive`() {
        assertFailsWith<IllegalArgumentException> {
            shearLagTransferLength(0.0, CROSSOVER_SPACING, 64.0)
        }
        assertFailsWith<IllegalArgumentException> {
            shearLagModeDecayLength(STRETCH_MODULUS, CROSSOVER_SPACING, 64.0, INTERHELICAL, 0.0)
        }
    }

    // ------------------------------------------------------------------ gate 2

    @Test
    fun `gate 2 the lattice and continuum decay lengths agree at long wavelength`() {
        val q = 1e-4
        val lattice = shearLagModeDecayLength(
            STRETCH_MODULUS, CROSSOVER_SPACING, 64.0, INTERHELICAL, q
        )
        val continuum = shearLagContinuumModeDecayLength(
            STRETCH_MODULUS, CROSSOVER_SPACING, 64.0, INTERHELICAL, q
        )
        assert(lattice.isCloseTo(continuum, 1e-8))
        // and both diverge as 1/q
        assert(
            (continuum / shearLagContinuumModeDecayLength(
                STRETCH_MODULUS, CROSSOVER_SPACING, 64.0, INTERHELICAL, 2.0 * q
            )).isCloseTo(2.0)
        )
    }

    @Test
    fun `gate 2 at the zone boundary the lattice decay length is exactly pi over two of the continuum`() {
        // q d = pi is the shortest wavelength a duplex lattice carries. The continuum does
        // not know the lattice stops there, and the ratio of the two is an exact identity,
        // not a tolerance: sqrt(S p / 4 k_s) against (d/pi) sqrt(S p /(k_s d^2)).
        val q = PI / INTERHELICAL
        val lattice = shearLagModeDecayLength(
            STRETCH_MODULUS, CROSSOVER_SPACING, 64.0, INTERHELICAL, q
        )
        val continuum = shearLagContinuumModeDecayLength(
            STRETCH_MODULUS, CROSSOVER_SPACING, 64.0, INTERHELICAL, q
        )
        assert((lattice / continuum).isCloseTo(PI / 2.0))
    }

    @Test
    fun `gate 2 a rigid crossover shares the load instantly and a vanishing one never does`() {
        val rigid = shearLagSharingLength(
            STRETCH_MODULUS, CROSSOVER_SPACING, 1e8, INTERHELICAL, DUPLEXES
        )
        val slack = shearLagSharingLength(
            STRETCH_MODULUS, CROSSOVER_SPACING, 1e-8, INTERHELICAL, DUPLEXES
        )
        assert(rigid < 0.1)
        assert(slack > 1e6)
    }

    @Test
    fun `gate 2 a rigid crossover makes every duplex carry exactly the equal share`() {
        // the continuum's own limiting case, and the one the lattice reproduces only when
        // its connector arm is set to zero — i.e. only in the shear-lag kinematics
        val sheet = membrane(crossoverShearStiffness = 1e6, modes = 1200)
        (0 until DUPLEXES).forEach { beam ->
            assert(
                sheet.duplexAxialForce(edgeChord(), 0.0, beam)
                    .isCloseTo(1.0 / DUPLEXES, 1e-6)
            )
        }
    }

    @Test
    fun `gate 2 the Gen-1 sharing length exceeds the tile it has to share across`() {
        // the cheap bound that decides the regime before any lattice is assembled
        val sharing = shearLagSharingLength(
            STRETCH_MODULUS, CROSSOVER_SPACING, NOMINAL_SHEAR, INTERHELICAL, DUPLEXES
        )
        assert(sharing > Gen1Tile.EDGE_X)
    }

    // ------------------------------------------------------------------ gate 3

    @Test
    fun `gate 3 the uniform mode of the continuum is exactly the equal share`() {
        val sheet = membrane()
        assert(sheet.equalShareDuplexAxialForce(1.0).isCloseTo(1.0 / DUPLEXES))
    }

    @Test
    fun `gate 3 the far duplex carries less than the equal share and still carries tension`() {
        val sheet = membrane()
        val far = sheet.duplexAxialForce(edgeChord(), x = 0.0, beam = 0)
        assert(far > 0.0)
        assert(far < 1.0 / DUPLEXES)
    }

    @Test
    fun `gate 3 the axial force sums across the strip to the applied force at every cut`() {
        val sheet = membrane()
        val load = edgeChord()
        listOf(-10.0, 0.0, 7.0).forEach { x ->
            val total = (0 until DUPLEXES).sumOf { beam ->
                sheet.duplexAxialForce(load, x, beam)
            }
            // exact, not a tolerance: every non-uniform mode integrates to zero over the
            // whole width, so the sum rule holds mode by mode
            assert(total.isCloseTo(1.0, 1e-9))
        }
    }

    @Test
    fun `gate 3 reversing the chord reverses every internal force`() {
        val sheet = membrane()
        val forward = ChordLoad(-20.0, 0.0, 20.0, 0.0, 1.0)
        val reversed = ChordLoad(20.0, 0.0, -20.0, 0.0, 1.0)
        assert(
            sheet.duplexAxialForce(forward, 0.0, 7)
                .isCloseTo(-sheet.duplexAxialForce(reversed, 0.0, 7), 1e-6)
        )
    }

    // ------------------------------------------------------------------ gate 4

    @Test
    fun `gate 4 the mode series converges away from the load point`() {
        val load = edgeChord()
        val coarse = membrane(modes = 150).duplexAxialForce(load, 0.0, 7)
        val fine = membrane(modes = 600).duplexAxialForce(load, 0.0, 7)
        val finest = membrane(modes = 2400).duplexAxialForce(load, 0.0, 7)
        assert(abs(fine - finest) / abs(finest) < 1e-6)
        assert(abs(fine - finest) <= abs(coarse - finest))
    }

    @Test
    fun `gate 4 the hyperbolic kernel does not overflow at a large argument`() {
        // cosh over sinh above u = 20 is the trap CLAUDE.md records three times over; the
        // stable form must stay finite for a crossover stiff enough to make kappa L huge
        val stiff = membrane(crossoverShearStiffness = 1e6, modes = 600)
        val value = stiff.duplexAxialForce(edgeChord(), 0.0, 7)
        assert(value.isFinite())
        // a very stiff crossover shares the load completely: the answer is the equal share
        assert(value.isCloseTo(1.0 / DUPLEXES, 5e-2))
    }

    // ------------------------------------------------------------------ gate 5

    @Test
    fun `gate 5 the continuum converges only logarithmically at the load point`() {
        // and it converges *to the applied force*, which is the equilibrium bound this task
        // exists to establish, reached independently by the continuum
        val near = membrane(modes = 2400).duplexAxialForce(edgeChord(), -19.99, 7)
        val nearer = membrane(modes = 9600).duplexAxialForce(edgeChord(), -19.99, 7)
        assert(nearer > near)
        assert(nearer < 1.0)
        assert(nearer > 0.99)
    }

    @Test
    fun `gate 5 the membrane stiffnesses are the lattice densities`() {
        val sheet = membrane(crossoverShearStiffness = 64.0)
        assert(sheet.alongHelixMembraneStiffness.isCloseTo(STRETCH_MODULUS / INTERHELICAL))
        assert(sheet.shearMembraneStiffness.isCloseTo(64.0 * INTERHELICAL / CROSSOVER_SPACING))
    }

    @Test
    fun `gate 5 the in-plane load ellipse is far from circular for this sheet`() {
        // the sheet is anisotropic in the membrane problem too, which is what makes a tether
        // pulling along the helices a different problem from one pulling across them
        val ratio = shearLagAspectRatio(
            STRETCH_MODULUS, CROSSOVER_SPACING, NOMINAL_SHEAR, INTERHELICAL
        )
        assert(ratio > 4.0)
    }

}
