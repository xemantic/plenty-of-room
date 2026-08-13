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
import kotlin.math.abs
import kotlin.math.ln
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-15`'s in-plane lattice, gate by gate.
 *
 * The gate-2 tests are the ones that license the model at all: a lattice whose
 * long-wavelength limit does not reproduce the membrane stiffnesses `S/d`, `k_s d/p` and
 * `k_n d/p` is not a discretisation of the shear-lag membrane it is compared against, and
 * any difference it then reports would be parameterisation rather than functional form —
 * exactly the discipline `C-0009` applied to `C-0006`'s plate.
 */

private val nominalSheet = origamiSheet(
    Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
)

private val NOMINAL_SHEAR = Gen1Tile.crossoverInPlaneStiffness()

private fun membrane(
    beamCount: Int = 15,
    columns: Int = 8,
    subdivisions: Int = 2,
    crossoverShearStiffness: Double = NOMINAL_SHEAR,
    crossoverNormalStiffness: Double = NOMINAL_SHEAR,
    connectorArm: Double = Gen1Tile.INTERHELICAL_SHEET / 2.0,
    regularisation: Double = OrigamiMembrane.DEFAULT_REGULARISATION,
    supports: List<InPlanePointSupport> = emptyList(),
    lengthX: Double = Gen1Tile.EDGE_X,
    sheet: OrigamiSheet = nominalSheet
): OrigamiMembrane = OrigamiMembrane(
    sheet = sheet,
    lengthX = lengthX,
    beamCount = beamCount,
    columns = CrossoverLayout.centred(columns, sheet.crossoverSpacing / 2.0),
    crossoverShearStiffness = crossoverShearStiffness,
    crossoverNormalStiffness = crossoverNormalStiffness,
    subdivisions = subdivisions,
    connectorArm = connectorArm,
    regularisation = regularisation,
    supports = supports
)

/** The along-helix tether chord: one pN in at one edge and out at the opposite one. */
private fun chordLoads(
    lattice: OrigamiMembrane,
    y: Double = 0.0,
    force: Double = 1.0
): List<InPlanePointLoad> = listOf(
    InPlanePointLoad(-lattice.lengthX / 2.0, y, -force, 0.0),
    InPlanePointLoad(lattice.lengthX / 2.0, y, force, 0.0)
)

class OrigamiMembraneTest {

    // ------------------------------------------------------------------ gate 1

    @Test
    fun `gate 1 the lattice has three in-plane degrees of freedom per node`() {
        val lattice = membrane()
        assert(lattice.degreesOfFreedom == lattice.beamCount * lattice.nodesPerBeam * 3)
    }

    @Test
    fun `gate 1 the in-plane lattice is the same sheet as the out-of-plane one`() {
        // the two models must agree on the topology exactly, or no comparison between the
        // in-plane and out-of-plane concentration factors means anything
        val layout = CrossoverLayout.centred(8, nominalSheet.crossoverSpacing / 2.0)
        val inPlane = OrigamiMembrane(
            sheet = nominalSheet,
            lengthX = Gen1Tile.EDGE_X,
            beamCount = 15,
            columns = layout,
            crossoverShearStiffness = NOMINAL_SHEAR,
            crossoverNormalStiffness = NOMINAL_SHEAR
        )
        val outOfPlane = OrigamiGrillage(
            sheet = nominalSheet,
            lengthX = Gen1Tile.EDGE_X,
            beamCount = 15,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            columns = layout
        )
        assert(inPlane.crossovers.size == outOfPlane.crossovers.size)
        assert(inPlane.crossovers.size == 56)
        inPlane.crossovers.zip(outOfPlane.crossovers).forEach { (a, b) ->
            assert(a.lowerBeam == b.lowerBeam)
            assert(a.column == b.column)
            assert(a.x.isCloseTo(b.x))
            assert(a.y.isCloseTo(b.y))
        }
        assert(inPlane.nodeX.size == outOfPlane.nodeX.size)
    }

    @Test
    fun `gate 1 a uniform axial strain costs exactly the membrane stiffness times the area`() {
        val lattice = membrane()
        val strain = 1e-3
        val field = lattice.nodalField({ x, _ -> strain * x }, { _, _ -> 0.0 }, { _, _ -> 0.0 })
        val expected = 0.5 * (nominalSheet.duplex.stretchModulus /
                nominalSheet.interhelicalDistance) * strain * strain * lattice.area
        assert(lattice.axialEnergy(field).isCloseTo(expected))
        // and nothing else stores anything: a uniform stretch slides no crossover
        assert(lattice.bendingEnergy(field).isCloseTo(0.0, 1e-12))
        assert(lattice.crossoverShearEnergy(field).isCloseTo(0.0, 1e-12))
        assert(lattice.crossoverNormalEnergy(field).isCloseTo(0.0, 1e-12))
    }

    @Test
    fun `gate 1 arguments are validated`() {
        assertFailsWith<IllegalArgumentException> { membrane(crossoverShearStiffness = 0.0) }
        assertFailsWith<IllegalArgumentException> { membrane(crossoverNormalStiffness = -1.0) }
        assertFailsWith<IllegalArgumentException> { membrane(regularisation = 0.0) }
        assertFailsWith<IllegalArgumentException> { membrane(beamCount = 1) }
    }

    // ------------------------------------------------------------------ gate 2

    @Test
    fun `gate 2 the in-plane shear rigidity carries the same discretisation excess as D perp`() {
        // `u = gamma y` slides every interface by exactly gamma d and nothing else, so the
        // lattice energy is n_c times half k_s (gamma d)^2 against the continuum's
        // half (k_s d / p) gamma^2 A. The ratio is the integer crossover count over the
        // continuum areal density — the *identical* 56/55.147 that C-0009 found for D_perp.
        val lattice = membrane()
        val shear = 1e-3
        val field = lattice.nodalField({ _, y -> shear * y }, { _, _ -> 0.0 }, { _, _ -> 0.0 })
        val continuum = 0.5 * (NOMINAL_SHEAR * nominalSheet.interhelicalDistance /
                nominalSheet.crossoverSpacing) * shear * shear * lattice.area
        val ratio = lattice.crossoverShearEnergy(field) / continuum
        val counted = lattice.crossovers.size /
                ((lattice.lengthX / nominalSheet.crossoverSpacing) * lattice.beamCount)
        assert(ratio.isCloseTo(counted))
        assert(ratio.isCloseTo(56.0 / 55.1470588235294, 1e-9))
    }

    @Test
    fun `gate 2 the across-helix membrane stiffness carries the same excess`() {
        val lattice = membrane()
        val strain = 1e-3
        val field = lattice.nodalField({ _, _ -> 0.0 }, { _, y -> strain * y }, { _, _ -> 0.0 })
        val continuum = 0.5 * (NOMINAL_SHEAR * nominalSheet.interhelicalDistance /
                nominalSheet.crossoverSpacing) * strain * strain * lattice.area
        assert(
            (lattice.crossoverNormalEnergy(field) / continuum)
                .isCloseTo(56.0 / 55.1470588235294, 1e-9)
        )
    }

    @Test
    fun `gate 2 in-plane bending along the helices reproduces D parallel exactly`() {
        val lattice = membrane()
        val curvature = 1e-4
        val field = lattice.nodalField(
            { _, _ -> 0.0 },
            { x, _ -> 0.5 * curvature * x * x },
            { x, _ -> curvature * x }
        )
        val expected = 0.5 * (nominalSheet.duplex.bendingRigidity /
                nominalSheet.interhelicalDistance) * curvature * curvature * lattice.area
        assert(lattice.bendingEnergy(field).isCloseTo(expected, 1e-9))
    }

    @Test
    fun `gate 2 a rigid in-plane translation stores nothing structural`() {
        val lattice = membrane()
        val field = lattice.nodalField({ _, _ -> 1.0 }, { _, _ -> 2.0 }, { _, _ -> 0.0 })
        assert(lattice.structuralEnergy(field).isCloseTo(0.0, 1e-12))
    }

    @Test
    fun `gate 2 a rigid in-plane rotation stores nothing structural`() {
        // u = -omega y, v = omega x, rotation = omega — the third nullspace vector, and the
        // one the connector arm could break if the interface kinematics were wrong
        val lattice = membrane()
        val omega = 1e-3
        val field = lattice.nodalField(
            { _, y -> -omega * y }, { x, _ -> omega * x }, { _, _ -> omega }
        )
        assert(lattice.structuralEnergy(field).isCloseTo(0.0, 1e-12))
    }

    @Test
    fun `gate 2 doubling the stretch modulus doubles the axial energy and nothing else`() {
        val stiff = nominalSheet.copy(
            duplex = nominalSheet.duplex.copy(
                stretchModulus = 2.0 * nominalSheet.duplex.stretchModulus
            )
        )
        val soft = membrane()
        val hard = membrane(sheet = stiff)
        val strain = 1e-3
        val field = soft.nodalField({ x, _ -> strain * x }, { _, _ -> 0.0 }, { _, _ -> 0.0 })
        assert((hard.axialEnergy(field) / soft.axialEnergy(field)).isCloseTo(2.0))
    }

    @Test
    fun `gate 2 a rigid crossover on a long strip forces an equal share`() {
        // the shear-lag limiting case, and it needs both premises: a crossover rigid enough
        // that no interface slides, *and* a strip long enough for the load to have become
        // uniform — which the 40 nm tile is not, and that is a result rather than a defect.
        // The connector arm is zero here because the equal-share limit belongs to the
        // shear-lag kinematics, in which the shear strain is du/dy alone.
        val length = 200.0
        val columns = 2 * (length / nominalSheet.crossoverSpacing).toInt() - 1
        val lattice = OrigamiMembrane(
            sheet = nominalSheet,
            lengthX = length,
            beamCount = 3,
            columns = CrossoverLayout.centred(columns, nominalSheet.crossoverSpacing / 2.0),
            crossoverShearStiffness = 1e5,
            crossoverNormalStiffness = 1e5,
            subdivisions = 1,
            connectorArm = 0.0
        )
        val solution = lattice.solve(
            listOf(
                InPlanePointLoad(-length / 2.0, lattice.duplexY(1), -1.0, 0.0),
                InPlanePointLoad(length / 2.0, lattice.duplexY(1), 1.0, 0.0)
            )
        )
        (0 until 3).forEach {
            assert(lattice.axialForceAt(solution, it, 0.0).isCloseTo(1.0 / 3.0, 1e-3))
        }
    }

    @Test
    fun `gate 2 frame indifference fixes the connector arm at exactly half the duplex spacing`() {
        // the arm is not a free parameter. A crossover joins two material points that
        // coincide on the interface line, so the two arms must sum to d — and only then does
        // a rigid in-plane rotation of the whole sheet cost nothing. Classical shear lag,
        // which drops dv/dx from the shear strain, is the arm = 0 case and is therefore
        // *not* frame-indifferent: it charges energy to a rigid rotation.
        val omega = 1e-3
        val energies = listOf(0.0, 0.5, 1.0, Gen1Tile.INTERHELICAL_SHEET / 2.0, 2.0).map { arm ->
            val lattice = membrane(connectorArm = arm)
            lattice.structuralEnergy(
                lattice.nodalField(
                    { _, y -> -omega * y }, { x, _ -> omega * x }, { _, _ -> omega }
                )
            )
        }
        assert(energies[3].isCloseTo(0.0, 1e-12))
        listOf(0, 1, 2, 4).forEach { assert(energies[it] > 1e-6) }
    }

    // ------------------------------------------------------------------ gate 3

    @Test
    fun `gate 3 the transfer ratio never exceeds one`() {
        // the equilibrium bound, which is the whole qualitative answer of T-15: a lateral
        // tether collects nothing from the layer, so no internal path can carry more than
        // the tether's own tension
        listOf(0.5, 5.0, NOMINAL_SHEAR, 1000.0).forEach { shear ->
            val lattice = membrane(
                crossoverShearStiffness = shear, crossoverNormalStiffness = shear
            )
            listOf(0.0, nominalSheet.interhelicalDistance * 7.0).forEach { y ->
                val solution = lattice.solve(chordLoads(lattice, y = y))
                assert(solution.peakDuplexAxialForce <= 1.0 + 1e-6)
                assert(solution.peakCrossoverForce <= 1.0 + 1e-6)
                assert(solution.peakDuplexInPlaneShear <= 1.0 + 1e-6)
            }
        }
    }

    @Test
    fun `gate 3 in-plane force balance closes on the applied load`() {
        val lattice = membrane(
            supports = listOf(InPlanePointSupport(-20.0, 0.0, 50.0, 50.0))
        )
        val solution = lattice.solve(
            listOf(InPlanePointLoad(20.0, 0.0, 1.0, 0.0))
        )
        val reacted = solution.supportForcesAlong.sum() + solution.regularisationForceAlong
        assert(reacted.isCloseTo(-1.0, 1e-8))
    }

    @Test
    fun `gate 3 the crossovers on one interface carry exactly the force crossing it`() {
        val lattice = membrane(
            supports = listOf(InPlanePointSupport(-15.0, -nominalSheet.interhelicalDistance * 5.0, 200.0, 200.0))
        )
        val solution = lattice.solve(
            listOf(
                InPlanePointLoad(
                    15.0, nominalSheet.interhelicalDistance * 5.0, 0.7, 0.3
                )
            )
        )
        (0 until lattice.beamCount - 1).forEach { interface_ ->
            val shear = solution.crossoverForces
                .filter { it.lowerBeam == interface_ }
                .sumOf { it.shearForce }
            val normal = solution.crossoverForces
                .filter { it.lowerBeam == interface_ }
                .sumOf { it.normalForce }
            assert(shear.isCloseTo(solution.shearAcrossInterface(interface_), 1e-6))
            assert(normal.isCloseTo(solution.normalAcrossInterface(interface_), 1e-6))
        }
    }

    @Test
    fun `gate 3 a chord on the mid duplex produces a symmetric force distribution`() {
        val lattice = membrane()
        val solution = lattice.solve(chordLoads(lattice, y = 0.0))
        (0..5).forEach { offset ->
            val lower = solution.crossoverForces
                .filter { it.lowerBeam == 6 - offset }.sumOf { abs(it.shearForce) }
            val upper = solution.crossoverForces
                .filter { it.lowerBeam == 7 + offset }.sumOf { abs(it.shearForce) }
            assert(lower.isCloseTo(upper, 1e-6))
        }
    }

    // ------------------------------------------------------------------ gate 4

    @Test
    fun `gate 4 the peak forces converge under nested mesh refinement`() {
        // nested refinements only, 1 subset 2 subset 4 — the monotonicity caveat C-0009 records
        val forces = listOf(1, 2, 4).map { subdivisions ->
            val lattice = membrane(subdivisions = subdivisions)
            val solution = lattice.solve(chordLoads(lattice))
            solution.peakCrossoverForce
        }
        assert(abs(forces[2] - forces[1]) / forces[2] < 0.01)
    }

    @Test
    fun `gate 4 the regularisation carries none of the load and does not move the answer`() {
        // there is no in-plane foundation in the physics — C-0010's exact zero — so the bed
        // exists only to remove the three rigid-body modes, and it must be invisible
        val forces = listOf(1e-6, 1e-4, 1e-2).map { regularisation ->
            val lattice = membrane(regularisation = regularisation)
            val solution = lattice.solve(chordLoads(lattice))
            assert(abs(solution.regularisationForceAlong) < 1e-9)
            assert(abs(solution.regularisationForceAcross) < 1e-9)
            solution.peakCrossoverForce
        }
        assert((forces[0] / forces[1]).isCloseTo(1.0, 1e-5))
        assert((forces[0] / forces[2]).isCloseTo(1.0, 1e-3))
    }

    // ------------------------------------------------------------------ gate 5

    @Test
    fun `gate 5 the lattice reproduces the shear-lag neighbour exchange length`() {
        // the derivation checked against the thing it describes: a two-duplex strip with a
        // chord on one of them sheds load to the other over Lambda_nn = sqrt(S p / 2 k_s).
        // Run at a soft crossover so that Lambda_nn is well above the crossover spacing,
        // which is the premise of the continuum-in-x reduction the formula rests on.
        val shear = 1.0
        val length = 800.0
        val columns = 2 * (length / nominalSheet.crossoverSpacing).toInt() - 1
        val lattice = OrigamiMembrane(
            sheet = nominalSheet,
            lengthX = length,
            beamCount = 2,
            columns = CrossoverLayout.centred(columns, nominalSheet.crossoverSpacing / 2.0),
            crossoverShearStiffness = shear,
            crossoverNormalStiffness = shear,
            subdivisions = 1,
            connectorArm = 0.0
        )
        val solution = lattice.solve(
            listOf(
                InPlanePointLoad(-length / 2.0, lattice.duplexY(0), -1.0, 0.0),
                InPlanePointLoad(length / 2.0, lattice.duplexY(0), 1.0, 0.0)
            )
        )
        val first = length / 2.0 - 100.0
        val second = length / 2.0 - 200.0
        val excessFirst = lattice.axialForceAt(solution, 0, first) - 0.5
        val excessSecond = lattice.axialForceAt(solution, 0, second) - 0.5
        val measured = (first - second) / ln(excessFirst / excessSecond)
        val predicted = shearLagNeighbourLength(
            nominalSheet.duplex.stretchModulus, nominalSheet.crossoverSpacing, shear
        )
        assert(measured.isCloseTo(predicted, 0.05))
        assert(lattice.degreesOfFreedom < 1200)
    }

    @Test
    fun `gate 5 the lattice agrees with the continuum membrane where discreteness is small`() {
        // where the continuum's own premise holds — a transfer length well above the
        // crossover spacing — the two must agree, and that is what licenses attributing the
        // disagreement at the Gen-1 crossover stiffness to discreteness
        val shear = 2.0
        val lattice = membrane(
            crossoverShearStiffness = shear, crossoverNormalStiffness = shear,
            connectorArm = 0.0
        )
        val solution = lattice.solve(chordLoads(lattice))
        val continuum = ShearLagMembrane(
            stretchModulus = nominalSheet.duplex.stretchModulus,
            interhelicalDistance = nominalSheet.interhelicalDistance,
            crossoverSpacing = nominalSheet.crossoverSpacing,
            crossoverShearStiffness = shear,
            lengthX = Gen1Tile.EDGE_X,
            duplexes = 15,
            modes = 1200
        )
        val load = ChordLoad(-20.0, 0.0, 20.0, 0.0, 1.0)
        listOf(0.0, 5.0, -8.0).forEach { x ->
            listOf(0, 4, 7).forEach { beam ->
                val latticeForce = lattice.axialForceAt(solution, beam, x)
                val continuumForce = continuum.duplexAxialForce(load, x, beam)
                // compared ABSOLUTELY against the 1 pN applied: two quantities that are both
                // meant to be near zero compare their noise if compared relatively
                assert(abs(latticeForce - continuumForce) < 0.02)
            }
        }
    }

    @Test
    fun `gate 5 pulling across the helices loads a crossover and pulling along one does not`() {
        // the direction result, as a limiting statement rather than a number: an along-helix
        // chord runs down a duplex, an across-helix chord has to cross every interface
        val lattice = membrane()
        val along = lattice.solve(chordLoads(lattice))
        val across = lattice.solve(
            listOf(
                InPlanePointLoad(0.0, lattice.duplexY(0), 0.0, -1.0),
                InPlanePointLoad(0.0, lattice.duplexY(lattice.beamCount - 1), 0.0, 1.0)
            )
        )
        assert(across.peakCrossoverForce > along.peakCrossoverForce)
        assert(along.peakDuplexAxialForce > across.peakDuplexAxialForce)
    }

}
