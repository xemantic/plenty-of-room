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
import com.xemantic.nano.plentyofroom.coupling.couplingSupports
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.PI
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-121` — what 34 duplexes stacked **above** the tile do to it.
 *
 * Every test is named for the verification gate it discharges. The two free strong falsifiers
 * `T-121` declared are here as tests: **zero attached arms must reproduce `C-0009`'s grillage
 * exactly**, and **a uniform load on a uniform Winkler foundation must dish exactly zero**
 * whether or not the sheet carries arms.
 *
 * The lattice tests run at one element per interval. The exact-zero statement is a property of
 * the *assembly* — a free body attached at a single point has a zero Schur complement whatever
 * the host mesh is — and the nested `1 ⊂ 2 ⊂ 4` refinement lives in the study, per
 * `CLAUDE.md`'s rule that mesh monotonicity holds only on nested refinements.
 */
class StackedArmSheetTest {

    private val sheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )

    private val duplexes = 15

    private fun lattice(supports: List<PointSupport> = emptyList()) = OrigamiGrillage(
        sheet = sheet,
        lengthX = Gen1Tile.EDGE_X,
        beamCount = duplexes,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        crossoverColumns = 8,
        subdivisions = 1,
        supports = supports
    )

    /** Six of `C-0055`'s own 34 arm placements — enough rows to break every symmetry. */
    private fun someArms(length: Double = C0055_ARM_LENGTH): List<StackedArm> = listOf(
        StackedArm(0, -13.6, length), StackedArm(0, -2.72, length),
        StackedArm(1, -19.04, length), StackedArm(1, -8.16, length),
        StackedArm(7, -2.72, length), StackedArm(14, -13.6, length)
    )

    private fun thirtyFourArms(): List<StackedArm> =
        (0 until 34).map { StackedArm(it % duplexes, -13.6, C0055_ARM_LENGTH) }

    // ------------------------------------------------------------------ gate 1 — dimensional

    @Test
    fun `gate 1 - the plan footprint fraction is an area over an area, invariant under a common length rescaling`() {
        val arms = someArms()
        val plain = armPlanFootprintFraction(
            arms, Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.EDGE_X * 40.35
        )
        val scaled = armPlanFootprintFraction(
            arms.map { it.copy(rootX = 2.0 * it.rootX, length = 2.0 * it.length) },
            2.0 * Gen1Tile.INTERHELICAL_SHEET, 4.0 * Gen1Tile.EDGE_X * 40.35
        )
        assert(scaled.isCloseTo(plain, relativeTolerance = 1e-12))
    }

    @Test
    fun `gate 1 - the composite rigidity has the dimensions of a rigidity and scales as length to the fourth`() {
        val plain = compositeBendingRigidity(230.0, 1100.0, 2.69)
        val scaled = compositeBendingRigidity(230.0 * 16.0, 1100.0 * 4.0, 2.0 * 2.69)
        assert(scaled.isCloseTo(16.0 * plain, relativeTolerance = 1e-12))
    }

    @Test
    fun `gate 1 - the slender-body drag is a viscosity times a length and is linear in both`() {
        val one = slenderBodyTransverseDrag(1.0, 8.0, 1.0)
        val scaled = slenderBodyTransverseDrag(3.0, 16.0, 2.0)
        // doubling every length at fixed aspect ratio doubles the drag; tripling eta triples it
        assert(scaled.isCloseTo(6.0 * one, relativeTolerance = 1e-12))
    }

    @Test
    fun `gate 1 - unphysical arguments throw at every entry point`() {
        assertFailsWith<IllegalArgumentException> { StackedArm(0, 0.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { StackedArm(-1, 0.0, 1.0) }
        assertFailsWith<IllegalArgumentException> {
            armPlanFootprintFraction(emptyList(), 1.0, 1.0)
        }
        assertFailsWith<IllegalArgumentException> { armContourFraction(someArms(), 0.0) }
        assertFailsWith<IllegalArgumentException> { armRootY(15, 15, 2.69) }
        assertFailsWith<IllegalArgumentException> { compositeBendingRigidity(-1.0, 1.0, 1.0) }
        assertFailsWith<IllegalArgumentException> { duplexMassPerLength(0.0) }
        assertFailsWith<IllegalArgumentException> { secondRootReachable(1.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { slenderBodyTransverseDrag(1.0, 1.0, 0.0) }
        // a body no longer than its own diameter is not a slender body
        assertFailsWith<IllegalArgumentException> { slenderBodyTransverseDrag(1.0, 1.0, 1.0) }
        assertFailsWith<IllegalArgumentException> {
            armRootCondensation(230.0, 460.0, 8.16, 1e4, 13.5, 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            armRootCondensation(230.0, 460.0, 8.16, 1e4, 13.5, 1e-6, ties = 3)
        }
        assertFailsWith<IllegalArgumentException> {
            // a root that does not land on a host node is refused rather than snapped
            StackedArmGrillage(lattice(), listOf(StackedArm(0, 1.234, 8.16)))
        }
        assertFailsWith<IllegalArgumentException> {
            StackedArmGrillage(lattice(), someArms(), regularisation = 0.0)
        }
        assertFailsWith<IllegalArgumentException> {
            StackedArmGrillage(lattice(), listOf(StackedArm(99, -13.6, 8.16)))
        }
    }

    // ------------------------------------------------------------------ gate 2 — limiting cases

    @Test
    fun `gate 2 - zero attached arms reproduces C-0009's grillage exactly`() {
        val bare = lattice()
        val armed = StackedArmGrillage(bare, emptyList())
        assert(armed.degreesOfFreedom == bare.degreesOfFreedom)
        assert(armed.armDegreesOfFreedom == 0)
        val pressure = uniformPressure(Gen1Tile.TARGET_FORCE / bare.area)
        val reference = bare.solve(pressure)
        val carried = armed.solve(pressure)
        // the only difference between the two solves is that the augmented matrix was
        // re-factorised, so what is left is the factorisation's own round-off on a 4.9 nm
        // deflection — twelve orders below it
        assert(carried.hostDeparture(reference.coefficients) < 1e-9)
        // both dishings are meant to be zero, so they are compared ABSOLUTELY: comparing two
        // quantities that both vanish relatively compares their round-off (`CLAUDE.md`)
        assert(abs(carried.deflection.peakDishing() - reference.peakDishing()) < 1e-9)
        assert(carried.deflection.meanDeflection.isCloseTo(reference.meanDeflection, 1e-9))
    }

    @Test
    fun `gate 2 - a uniform load on a uniform Winkler foundation dishes exactly zero, with and without the arms`() {
        val bare = lattice()
        val pressure = uniformPressure(Gen1Tile.TARGET_FORCE / bare.area)
        assert(bare.solve(pressure).peakDishing() < 1e-9)
        // the arms' regularisation is a spring to ground, so the armed sheet's dishing is
        // O(epsilon) rather than round-off; at the default 1e-9 it is eight orders below the
        // 4.9 nm deflection it would have to be a feature of
        assert(
            StackedArmGrillage(bare, someArms()).solve(pressure).deflection.peakDishing() < 1e-7
        )
    }

    @Test
    fun `gate 2 - the armed sheet's dishing under a uniform load vanishes linearly with the regularisation`() {
        val bare = lattice()
        val pressure = uniformPressure(Gen1Tile.TARGET_FORCE / bare.area)
        fun dishing(epsilon: Double) =
            StackedArmGrillage(bare, someArms(), regularisation = epsilon)
                .solve(pressure).deflection.peakDishing()
        val coarse = dishing(1e-6)
        val fine = dishing(1e-8)
        assert(coarse > 0.0)
        assert((fine / coarse).isCloseTo(1e-2, relativeTolerance = 0.1))
    }

    @Test
    fun `gate 2 - a free body tied at ONE point adds exactly zero stiffness at its root`() {
        val condensation = armRootCondensation(
            bendingRigidity = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
            torsionalRigidity = Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY,
            length = C0055_ARM_LENGTH,
            linkStiffness = OrigamiGrillage.RIGID_LINK_STIFFNESS,
            hingeStiffness = sheet.crossoverHingeStiffness,
            regularisation = 1e-9
        )
        assert(condensation.ties == 1)
        assert(abs(condensation.addedStiffnessNorm) < 1e-5)
    }

    @Test
    fun `gate 2 - a body tied at TWO points does not`() {
        val two = armRootCondensation(
            Gen1Tile.DUPLEX_BENDING_RIGIDITY, Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY,
            C0055_ARM_LENGTH, OrigamiGrillage.RIGID_LINK_STIFFNESS,
            sheet.crossoverHingeStiffness, 1e-9, ties = 2
        )
        assert(two.addedStiffnessNorm > 1.0)
    }

    @Test
    fun `gate 2 - the arm is too short to reach a second upward root over the whole design range`() {
        val pitch = Gen1Tile.CROSSOVER_SPACING_SHEET_BP * Gen1Tile.RISE_PER_BASE_PAIR
        assert(pitch.isCloseTo(10.88, relativeTolerance = 1e-12))
        assert(!secondRootReachable(C0055_ARM_LENGTH, pitch))
        // C-0055's arm at §3's own 45 paths, the longest in its table
        assert(!secondRootReachable(9.1311565, pitch))
        assert(secondRootReachable(pitch, pitch))
    }

    // --------------------------------------------------- gate 3 — symmetry and conservation

    @Test
    fun `gate 3 - the dof layout this model reconstructs agrees with the lattice's own basis`() {
        val bare = lattice()
        val node = 4
        val beam = 3
        val basis = bare.basisAt(bare.nodeX[node], bare.beamY[beam])
        val index = (beam * bare.nodesPerBeam + node) * OrigamiGrillage.DOF_PER_NODE +
                OrigamiGrillage.W
        assert(basis[index].isCloseTo(1.0, relativeTolerance = 1e-12))
    }

    @Test
    fun `gate 3 - the arms hold no strain energy and their root crossovers transmit nothing`() {
        val bare = lattice()
        val pressure = uniformPressure(Gen1Tile.TARGET_FORCE / bare.area)
        val carried = StackedArmGrillage(bare, someArms(), regularisation = 1e-9).solve(pressure)
        assert(carried.armEnergy < 1e-9)
        assert(carried.peakRootLinkForce < 1e-6)
    }

    @Test
    fun `gate 3 - attaching the arms leaves every published grillage response identically unchanged`() {
        val supports = couplingSupports(
            listOf(-10.0 to 0.0, 10.0 to 0.0),
            Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE
        )
        val bare = lattice(supports)
        val pressure = uniformPressure(Gen1Tile.TARGET_FORCE / bare.area)
        val reference = bare.solve(pressure)
        val carried = StackedArmGrillage(bare, someArms(), regularisation = 1e-9).solve(pressure)
        assert(
            carried.deflection.peakCrossoverForce
                .isCloseTo(reference.peakCrossoverForce, relativeTolerance = 1e-6)
        )
        assert(
            carried.deflection.peakDuplexShear
                .isCloseTo(reference.peakDuplexShear, relativeTolerance = 1e-6)
        )
    }

    @Test
    fun `gate 3 - the thermal point fluctuation of the host is unchanged by the arms`() {
        val bare = lattice()
        val armed = StackedArmGrillage(bare, someArms(), regularisation = 1e-9)
        assert(
            armed.pointFluctuationRms(0.0, 0.0)
                .isCloseTo(bare.thermalFluctuation().centreRms, relativeTolerance = 1e-6)
        )
    }

    @Test
    fun `gate 3 - the exact zero is a property of the attachment and not of the arm's own elasticity`() {
        // a ten-fold stiffer and a ten-fold longer arm both add exactly nothing
        listOf(0.5 * C0055_ARM_LENGTH, 2.0 * C0055_ARM_LENGTH).forEach { length ->
            val condensation = armRootCondensation(
                10.0 * Gen1Tile.DUPLEX_BENDING_RIGIDITY,
                10.0 * Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY,
                length, OrigamiGrillage.RIGID_LINK_STIFFNESS,
                sheet.crossoverHingeStiffness, 1e-9
            )
            assert(abs(condensation.addedStiffnessNorm) < 1e-4)
        }
    }

    // ------------------------------------------------------------------ gate 4 — convergence

    @Test
    fun `gate 4 - the added root stiffness vanishes linearly in the regularisation`() {
        fun norm(epsilon: Double) = armRootCondensation(
            Gen1Tile.DUPLEX_BENDING_RIGIDITY, Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY,
            C0055_ARM_LENGTH, OrigamiGrillage.RIGID_LINK_STIFFNESS,
            sheet.crossoverHingeStiffness, epsilon
        ).addedStiffnessNorm
        val coarse = norm(1e-4)
        val fine = norm(1e-6)
        assert(coarse > 0.0)
        assert((fine / coarse).isCloseTo(1e-2, relativeTolerance = 0.1))
    }

    @Test
    fun `gate 4 - the host solution converges to the bare one linearly in the regularisation`() {
        val bare = lattice()
        val pressure = uniformPressure(Gen1Tile.TARGET_FORCE / bare.area)
        val reference = bare.solve(pressure).coefficients
        fun departure(epsilon: Double) =
            StackedArmGrillage(bare, someArms(), regularisation = epsilon)
                .solve(pressure).hostDeparture(reference)
        val coarse = departure(1e-6)
        val fine = departure(1e-8)
        assert(coarse > 0.0)
        assert((fine / coarse).isCloseTo(1e-2, relativeTolerance = 0.1))
        // extrapolated to the exact constraint, the arms move the host by nothing
        assert(departure(1e-9) < 1e-7)
    }

    // ------------------------------------------------------------------ gate 5 — upstream

    @Test
    fun `gate 5 - C-0055's plan footprint fraction, arm length and root pitch are reproduced`() {
        val fraction = armPlanFootprintFraction(
            thirtyFourArms(), Gen1Tile.INTERHELICAL_SHEET,
            Gen1Tile.EDGE_X * duplexes * Gen1Tile.INTERHELICAL_SHEET
        )
        assert(fraction.isCloseTo(0.46, relativeTolerance = 0.02))
        assert(C0055_ARM_LENGTH.isCloseTo(8.164, relativeTolerance = 1e-4))
        assert(C0055_ARM_COUNT == 34)
    }

    @Test
    fun `gate 5 - the slender-body transverse drag sits between its two bracketing denominators`() {
        val viscosity = 8.540578046518857e-10
        val drag = slenderBodyTransverseDrag(viscosity, C0055_ARM_LENGTH, 1.0)
        assert(drag > 4.0 * PI * viscosity * C0055_ARM_LENGTH / 3.0)
        assert(drag < 4.0 * PI * viscosity * C0055_ARM_LENGTH / 2.0)
    }

    @Test
    fun `gate 5 - the composite rigidity a genuine second layer would give is an order of magnitude above one duplex`() {
        val composite = compositeBendingRigidity(
            Gen1Tile.DUPLEX_BENDING_RIGIDITY, Gen1Tile.DUPLEX_STRETCH_MODULUS,
            Gen1Tile.INTERHELICAL_SHEET
        )
        val ratio = composite / Gen1Tile.DUPLEX_BENDING_RIGIDITY
        assert(ratio > 15.0)
        assert(ratio < 25.0)
    }

    @Test
    fun `gate 5 - the duplex mass per unit length reproduces the B-DNA linear density`() {
        val perNm = duplexMassPerLength(Gen1Tile.RISE_PER_BASE_PAIR)
        assert(perNm.isCloseTo(3.175e-21, relativeTolerance = 0.02))
    }

    @Test
    fun `gate 5 - the arm array adds under half the sheet's own duplex contour`() {
        val fraction = armContourFraction(thirtyFourArms(), duplexes * Gen1Tile.EDGE_X)
        assert(fraction.isCloseTo(0.4626, relativeTolerance = 1e-3))
    }

    @Test
    fun `gate 5 - C-0055's own arm roots land on the nominal column lattice`() {
        val bare = lattice()
        listOf(-19.04, -13.6, -8.16, -2.72, 2.72, 8.16).forEach { root ->
            assert(bare.columnX.any { abs(it - root) < 1e-6 })
        }
    }

    @Test
    fun `gate 5 - the duplex axis of a row reproduces the lattice's own beam ordinate`() {
        val bare = lattice()
        (0 until duplexes).forEach { row ->
            assert(
                armRootY(row, duplexes, Gen1Tile.INTERHELICAL_SHEET)
                    .isCloseTo(bare.beamY[row], relativeTolerance = 1e-12)
            )
        }
    }

}
