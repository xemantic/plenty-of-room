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
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-9`'s second deliverable — the crossover's vertical/axial compliance.
 *
 * The gates are named for the falsifiers and cheap bounds of `gpd/tasks/T-9-*.md` §5.
 */
class CrossoverVerticalComplianceTest {

    // -- G1: the construction is this repository's OWN, on a different axis -----------------------

    @Test
    fun `G1 the vertical construction is the in-plane one, on the orthogonal axis`() {
        // The whole traceability argument: the vertical link and the in-plane connector are the
        // SAME two phosphate bonds resisting a relative DISPLACEMENT of the SAME two duplexes, so
        // Chen et al.'s softened-bond construction applies to both unchanged.  If these two ever
        // part company, one of them has stopped being that construction.
        assert(
            abs(crossoverVerticalStiffness(1.0) - Gen1Tile.crossoverInPlaneStiffness(1.0)) < 1e-12
        )
        assert(
            abs(crossoverVerticalStiffness(0.6) - Gen1Tile.crossoverInPlaneStiffness(0.6)) < 1e-12
        )
    }

    @Test
    fun `G1 the vertical stiffness is 2 alpha S over 100 a, linear in alpha`() {
        val expected = 2.0 * Gen1Tile.DUPLEX_STRETCH_MODULUS /
                (100.0 * Gen1Tile.RISE_PER_BASE_PAIR)
        assert(abs(crossoverVerticalStiffness(1.0) - expected) < 1e-12)
        assert(abs(crossoverVerticalStiffness(2.0) - 2.0 * expected) < 1e-12)
        assert(abs(crossoverVerticalStiffness(0.5) - 0.5 * expected) < 1e-12)
    }

    @Test
    fun `G1 a non-positive alpha is refused`() {
        assertFailsWith<IllegalArgumentException> { crossoverVerticalStiffness(0.0) }
        assertFailsWith<IllegalArgumentException> { crossoverVerticalStiffness(-1.0) }
    }

    // -- G2: the hinge's own equivalent, and the penalty's own justification ----------------------

    @Test
    fun `G2 the hinge equivalent reproduces the RIGID_LINK_STIFFNESS KDoc's about 5000 times`() {
        // OrigamiGrillage.RIGID_LINK_STIFFNESS is documented as "about 5000x the hinge's own
        // equivalent vertical stiffness k_theta / d^2".  Asserted so that the justification the
        // penalty carries is executable rather than a sentence beside a constant.
        val equivalent = hingeEquivalentVerticalStiffness()
        val ratio = OrigamiGrillage.RIGID_LINK_STIFFNESS / equivalent
        assert(ratio > 4000.0)
        assert(ratio < 6000.0)
        assert(abs(equivalent - Gen1Tile.crossoverHingeStiffness(1.0) /
                (Gen1Tile.INTERHELICAL_SHEET * Gen1Tile.INTERHELICAL_SHEET)) < 1e-12)
    }

    @Test
    fun `G2 the penalty is 150 to 160 times the crossover's own displacement stiffness`() {
        // The other half of the same KDoc, which it does NOT state: the penalty is compared
        // against the duplex stretch modulus and against the hinge, never against the crossover's
        // own displacement stiffness -- which this repository derives 200 lines away.
        val ratio = OrigamiGrillage.RIGID_LINK_STIFFNESS / crossoverVerticalStiffness(1.0)
        assert(ratio > 150.0)
        assert(ratio < 160.0)
    }

    // -- G3: the cheap bound's central arithmetic, as an executable assertion ---------------------

    @Test
    fun `G3 the physical value lies inside C-0099's unresolved bisection bracket`() {
        // C-0099's channel B bisection returned [0, 0.015625] of the penalty and read it as a
        // discontinuity.  Falsifier F5 of the Plan: if this leaves that bracket, the framing goes.
        val fraction = penaltyFractionOf(crossoverVerticalStiffness(1.0))
        assert(fraction > 0.0)
        assert(fraction < C0099_UNRESOLVED_PENALTY_FRACTION)
    }

    @Test
    fun `G3 the alpha band keeps the physical value inside that bracket at both ends`() {
        // Chen et al.'s experimentally admissible alpha band is 0.6 - 1.2, so the conclusion must
        // not rest on alpha = 1 alone.
        listOf(Gen1Tile.CROSSOVER_ALPHA_MIN, 1.0, Gen1Tile.CROSSOVER_ALPHA_MAX).forEach { alpha ->
            assert(penaltyFractionOf(crossoverVerticalStiffness(alpha)) <
                    C0099_UNRESOLVED_PENALTY_FRACTION)
        }
    }

    // -- G4: the sweep -----------------------------------------------------------------------

    @Test
    fun `G4 the sweep is C-0020's own four decades, scaled`() {
        val sweep = crossoverVerticalStiffnessSweep(1.0)
        assert(sweep.size == Gen1Tile.CROSSOVER_IN_PLANE_SWEEP.size)
        sweep.indices.forEach {
            assert(
                abs(sweep[it] - Gen1Tile.CROSSOVER_IN_PLANE_SWEEP[it] *
                        crossoverVerticalStiffness(1.0)) < 1e-9
            )
        }
    }

    @Test
    fun `G4 the sweep is strictly increasing and brackets the hinge equivalent and the penalty`() {
        val sweep = crossoverVerticalStiffnessSweep(1.0)
        (0 until sweep.size - 1).forEach { assert(sweep[it] < sweep[it + 1]) }
        // below the hinge's own equivalent at the soft end ...
        assert(sweep.first() > hingeEquivalentVerticalStiffness())
        assert(sweep.first() < 10.0 * hingeEquivalentVerticalStiffness())
        // ... and past the penalty at the stiff end, so the rigid limit is inside the sweep
        assert(sweep.last() < OrigamiGrillage.RIGID_LINK_STIFFNESS)
        assert(sweep.last() > 0.5 * OrigamiGrillage.RIGID_LINK_STIFFNESS)
    }

    // -- G5: the ramp fraction, which is how V4 is decided ----------------------------------------

    @Test
    fun `G5 the ramp fraction is zero at the rigid limit and one at an absent link`() {
        assert(abs(rampFraction(atPhysical = 0.06, atRigid = 0.06, atAbsent = 0.17)) < 1e-12)
        assert(abs(rampFraction(atPhysical = 0.17, atRigid = 0.06, atAbsent = 0.17) - 1.0) < 1e-12)
        assert(abs(rampFraction(atPhysical = 0.115, atRigid = 0.06, atAbsent = 0.17) - 0.5) < 1e-9)
    }

    @Test
    fun `G5 a ramp fraction with no present-versus-absent movement is refused`() {
        assertFailsWith<IllegalArgumentException> {
            rampFraction(atPhysical = 0.06, atRigid = 0.06, atAbsent = 0.06)
        }
    }

    // -- G6: the four verdicts fire in BOTH directions --------------------------------------------

    @Test
    fun `G6 V1 to V4 do not fire on a link the lattice cannot tell from rigid`() {
        val verdict = verticalComplianceVerdict(
            dishingAtPhysical = 0.0621469105,
            dishingAtRigid = 0.0621469105,
            dishingAtAbsent = 0.168640591,
            peakForceAtPhysical = 1.0,
            peakForceAtRigid = 1.0
        )
        assert(!verdict.crossesFlatnessConvention)
        assert(!verdict.movesMoreThanTheRowEndUnknown)
        assert(!verdict.movesMoreThanTheRowEndUnknownAsFirstWritten)
        assert(!verdict.movesThePeakCrossoverForce)
        assert(!verdict.isARampNotAStep)
        assert(verdict.binaryReadingIsRight)
        assert(verdict.binaryReadingIsRightAsFirstWritten)
    }

    @Test
    fun `G6 V1 to V4 all fire on a link the lattice reads as absent`() {
        val verdict = verticalComplianceVerdict(
            dishingAtPhysical = 0.168640591,
            dishingAtRigid = 0.0621469105,
            dishingAtAbsent = 0.168640591,
            peakForceAtPhysical = 0.5,
            peakForceAtRigid = 1.0
        )
        assert(verdict.crossesFlatnessConvention)
        assert(verdict.movesMoreThanTheRowEndUnknown)
        assert(verdict.movesMoreThanTheRowEndUnknownAsFirstWritten)
        assert(verdict.movesThePeakCrossoverForce)
        assert(verdict.isARampNotAStep)
        assert(!verdict.binaryReadingIsRight)
        assert(!verdict.binaryReadingIsRightAsFirstWritten)
    }

    @Test
    fun `G6 each verdict has its own threshold and they are independent`() {
        // V2 alone: 3.1 points of the convention (0.031 of the stroke), inside T-5b, force unmoved,
        // and a ramp fraction below a twentieth -- so V2 fires and V1, V3, V4 do not.
        val onlyV2 = verticalComplianceVerdict(
            dishingAtPhysical = 0.0621469105 + 0.031,
            dishingAtRigid = 0.0621469105,
            dishingAtAbsent = 0.0621469105 + 1.0,
            peakForceAtPhysical = 1.0,
            peakForceAtRigid = 1.0
        )
        assert(!onlyV2.crossesFlatnessConvention)
        assert(onlyV2.movesMoreThanTheRowEndUnknown)
        assert(!onlyV2.movesThePeakCrossoverForce)
        assert(!onlyV2.isARampNotAStep)
        // V3 alone: a 20 % force movement with the dishing untouched.
        val onlyV3 = verticalComplianceVerdict(
            dishingAtPhysical = 0.0621469105,
            dishingAtRigid = 0.0621469105,
            dishingAtAbsent = 0.168640591,
            peakForceAtPhysical = 1.2,
            peakForceAtRigid = 1.0
        )
        assert(!onlyV3.crossesFlatnessConvention)
        assert(!onlyV3.movesMoreThanTheRowEndUnknown)
        assert(onlyV3.movesThePeakCrossoverForce)
        assert(!onlyV3.isARampNotAStep)
    }

    @Test
    fun `G6 the corrected V2 threshold is C-0099's own difference of two emitted readings`() {
        // C-0099 emits 0.0651753854 at s = 0 and 0.0621469105 at s = 1 and calls the gap
        // "3.0 percentage points of margin" against T-5b's 0.10 -- so the movement it prices the
        // whole row-end unknown at is 0.0030284749 of the stroke, not 0.030.  T-9's Plan wrote
        // the second, a factor of ten out; both are kept and both verdicts are emitted.
        assert(abs(ROW_END_UNKNOWN_MARGIN - (0.0651753854 - 0.0621469105)) < 1e-9)
        assert(abs(ROW_END_UNKNOWN_MARGIN_AS_FIRST_WRITTEN / ROW_END_UNKNOWN_MARGIN - 9.906) < 0.01)
    }

    @Test
    fun `G6 a movement between the two thresholds fires the corrected V2 and not the registered one`() {
        val verdict = verticalComplianceVerdict(
            dishingAtPhysical = 0.062146910 + 0.0034,
            dishingAtRigid = 0.062146910,
            dishingAtAbsent = 0.168640591,
            peakForceAtPhysical = 1.0,
            peakForceAtRigid = 1.0
        )
        assert(verdict.movesMoreThanTheRowEndUnknown)
        assert(!verdict.movesMoreThanTheRowEndUnknownAsFirstWritten)
        assert(!verdict.binaryReadingIsRight)
        assert(verdict.binaryReadingIsRightAsFirstWritten)
        assert(verdict.dishingMovementOverTheRowEndUnknown > 1.0)
    }

    @Test
    fun `G6 the thresholds are the ones the task file fixed`() {
        assert(abs(FLATNESS_CONVENTION - 0.10) < 1e-12)
        assert(abs(ROW_END_UNKNOWN_MARGIN_AS_FIRST_WRITTEN - 0.030) < 1e-12)
        assert(abs(REGISTRATION_FORCE_THRESHOLD - 0.19) < 1e-12)
        assert(abs(RAMP_FRACTION_THRESHOLD - 0.05) < 1e-12)
        assert(abs(C0099_UNRESOLVED_PENALTY_FRACTION - 0.015625) < 1e-12)
    }

    // -- G7: the knob is the one the corpus's default uses ----------------------------------------

    @Test
    fun `G7 an explicit rigid linkStiffness is the lattice the corpus already publishes`() {
        // The sweep varies OrigamiGrillage.linkStiffness globally.  Passing the default value
        // explicitly must be the SAME object, or the sweep's rigid rung is not the corpus's tile.
        // Taken on the load vector, which is a fixed-order scatter-add: CLAUDE.md records that a
        // solved field of this lattice is not bit-identical even between two identical solves.
        val sheet = origamiSheet(
            interhelicalDistance = Gen1Tile.INTERHELICAL_SHEET,
            crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_SHEET_BP
        )
        val columns = CrossoverLayout.centred(7, sheet.crossoverSpacing / 2.0)
        fun lattice(link: Double?) = if (link == null) OrigamiGrillage(
            sheet = sheet, lengthX = 38.08, beamCount = 15,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT, columns = columns
        ) else OrigamiGrillage(
            sheet = sheet, lengthX = 38.08, beamCount = 15,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT, columns = columns,
            linkStiffness = link
        )
        val defaulted = lattice(null)
        val explicit = lattice(OrigamiGrillage.RIGID_LINK_STIFFNESS)
        assert(abs(defaulted.linkStiffness - explicit.linkStiffness) < 1e-12)
        val field = uniformPressure(Gen1Tile.TARGET_FORCE / (38.08 * 15 * Gen1Tile.INTERHELICAL_SHEET))
        val a = defaulted.solve(field).peakDishing(41)
        val b = explicit.solve(field).peakDishing(41)
        assert(abs(a - b) < 1e-10)
    }

    @Test
    fun `G7 a softer link is a strictly softer lattice under a point load`() {
        // Limiting case, and the sign: a softer vertical tie cannot make the sheet stiffer.
        val sheet = origamiSheet(
            interhelicalDistance = Gen1Tile.INTERHELICAL_SHEET,
            crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_SHEET_BP
        )
        val columns = CrossoverLayout.centred(7, sheet.crossoverSpacing / 2.0)
        fun peak(link: Double): Double = OrigamiGrillage(
            sheet = sheet, lengthX = 38.08, beamCount = 15,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT, columns = columns,
            linkStiffness = link
        ).solve(uniformPressure(0.0), listOf(PointLoad(0.0, 0.0, 10.0))).peakDeflection(41)
        val soft = peak(crossoverVerticalStiffness(1.0))
        val rigid = peak(OrigamiGrillage.RIGID_LINK_STIFFNESS)
        assert(soft > rigid)
    }

}
