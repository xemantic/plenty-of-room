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

package com.xemantic.nano.plentyofroom.anchoring

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-67` — whether a 90° routing between a sheet duplex and a normal standoff exists.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 * The spine of the task is a **counting theorem**: a duplex end has two backbones, therefore two
 * strand termini, therefore at most two covalent links, therefore a lever arm bounded by the
 * duplex's own radius — which is 1.49× smaller than the one `C-0028`'s recommended base assumes.
 */
class PerpendicularJunctionTest {

    private val ei = Gen1Tile.DUPLEX_BENDING_RIGIDITY

    private val stretch = Gen1Tile.DUPLEX_STRETCH_MODULUS

    private val rise = Gen1Tile.RISE_PER_BASE_PAIR

    private val backbone = DuplexBackbone()

    // ---------------------------------------------------------------- gate 1 — dimensional

    @Test
    fun `gate 1 dimensional consistency - a softened bond's two constants carry the duplex units`() {
        // k_bond,theta = alpha B/(100 a): (pN*nm^2)/(nm) = pN*nm/rad
        assert(bondHingeStiffness(1.0).isCloseTo(230.0 / (100.0 * 0.34)))
        // k_bond,s = alpha S/(100 a): (pN)/(nm) = pN/nm
        assert(bondSlideStiffness(1.0).isCloseTo(1100.0 / (100.0 * 0.34)))
        // and an antiparallel crossover is exactly two of each, which is C-0009's own constant
        assert((2.0 * bondHingeStiffness(1.0)).isCloseTo(Gen1Tile.crossoverHingeStiffness()))
        assert((2.0 * bondSlideStiffness(1.0)).isCloseTo(Gen1Tile.crossoverInPlaneStiffness()))
    }

    @Test
    fun `gate 1 dimensional consistency - a base couple is a slide stiffness times a squared lever arm`() {
        // k_theta = 2 k_hinge + 2 k_slide a^2 — doubling the lever arm quadruples the couple part
        val couple = { arm: Double ->
            maximumBaseRotationalStiffness(arm) - 2.0 * bondHingeStiffness(1.0)
        }
        assert((couple(1.0) / couple(0.5)).isCloseTo(4.0))
        assert(couple(0.0).isCloseTo(0.0))
    }

    @Test
    fun `gate 1 dimensional consistency - the azimuthal quantum is a full turn over the base pairs in it`() {
        assert(DuplexBackbone(basePairsPerTurn = 10.67).azimuthQuantum
            .isCloseTo(2.0 * PI / 10.67))
        assert(DuplexBackbone(basePairsPerTurn = 10.5).azimuthQuantum.isCloseTo(2.0 * PI / 10.5))
    }

    @Test
    fun `gate 1 dimensional consistency - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { DuplexBackbone(phosphateRadius = -0.1) }
        assertFailsWith<IllegalArgumentException> { DuplexBackbone(basePairsPerTurn = 0.0) }
        assertFailsWith<IllegalArgumentException> { maximumBaseRotationalStiffness(-1.0) }
        assertFailsWith<IllegalArgumentException> { unpairedNucleotidesForGap(-0.1) }
        assertFailsWith<IllegalArgumentException> {
            RotatingHingeArm(13.53, -1.0, ei)
        }
    }

    // ---------------------------------------------------------------- gate 2 — limiting cases

    @Test
    fun `gate 2 limiting cases - the terminal chord is bounded by the duplex diameter and vanishes at zero groove angle`() {
        // 2 r_P sin(Delta/2): 0 at Delta = 0, exactly 2 r_P at Delta = 180 degrees
        assert(DuplexBackbone(minorGrooveAngle = 0.0).terminalChord.isCloseTo(0.0))
        assert(
            DuplexBackbone(minorGrooveAngle = 180.0).terminalChord
                .isCloseTo(2.0 * BForm.PHOSPHATE_RADIUS)
        )
        // and no groove angle whatever puts a terminus outside the duplex's own radius
        listOf(60.0, 90.0, 120.0, 154.0, 180.0).forEach {
            assert(DuplexBackbone(minorGrooveAngle = it).leverArm <= BForm.DUPLEX_RADIUS)
        }
    }

    @Test
    fun `gate 2 limiting cases - the seat height is the sheet radius over a duplex and falls in the valley`() {
        val onDuplex = seatFaceHeight(0.0)
        val valley = seatFaceHeight(Gen1Tile.INTERHELICAL_SHEET / 2.0)
        assert(onDuplex.isCloseTo(BForm.DUPLEX_RADIUS))
        // in the valley the standoff's rim dips between its two neighbours, so the face sits lower
        assert(valley < onDuplex)
        assert(
            valley.isCloseTo(
                sqrt(
                    BForm.DUPLEX_RADIUS * BForm.DUPLEX_RADIUS -
                            (Gen1Tile.INTERHELICAL_SHEET / 2.0 - BForm.DUPLEX_RADIUS) *
                            (Gen1Tile.INTERHELICAL_SHEET / 2.0 - BForm.DUPLEX_RADIUS)
                )
            )
        )
        // the seat is symmetric about the valley and about the duplex
        assert(seatFaceHeight(0.1).isCloseTo(seatFaceHeight(-0.1)))
        assert(
            seatFaceHeight(Gen1Tile.INTERHELICAL_SHEET / 2.0 + 0.2)
                .isCloseTo(seatFaceHeight(Gen1Tile.INTERHELICAL_SHEET / 2.0 - 0.2))
        )
    }

    @Test
    fun `gate 2 limiting cases - a link needs no unpaired nucleotide inside the phosphodiester step and one beyond it`() {
        assert(unpairedNucleotidesForGap(0.0) == 0)
        assert(unpairedNucleotidesForGap(BForm.PHOSPHODIESTER_STEP) == 0)
        assert(unpairedNucleotidesForGap(BForm.PHOSPHODIESTER_STEP + 1.0e-9) == 1)
        assert(unpairedNucleotidesForGap(BForm.PHOSPHODIESTER_STEP + 0.60) == 1)
        assert(unpairedNucleotidesForGap(BForm.PHOSPHODIESTER_STEP + 0.70) == 2)
        // monotone
        var previous = 0
        var gap = 0.0
        while (gap < 5.0) {
            val current = unpairedNucleotidesForGap(gap)
            assert(current >= previous)
            previous = current
            gap += 0.05
        }
    }

    @Test
    fun `gate 2 limiting cases - the rotating arm reduces to C-0023's linear hinge law at small rotation`() {
        val arm = RotatingHingeArm(Gen1Tile.crossoverHingeStiffness(), 4.11, ei)
        val linear = CrossoverHingeFlexure(Gen1Tile.crossoverHingeStiffness(), 4.11, ei)
        // at 1/1000 of the arm the exact law and C-0023's are the same number
        val tiny = 4.11e-3
        assert((arm.reaction(tiny) / linear.reaction(tiny)).isCloseTo(1.0, 1e-4))
        assert(arm.smallRotationStiffness.isCloseTo(linear.stiffness))
    }

    @Test
    fun `gate 2 limiting cases - the hinge branch alone cannot lift its tip past the arm length`() {
        // the geometric ceiling, and it needs no constitutive law: delta_hinge = r sin(theta) < r
        val arm = RotatingHingeArm(Gen1Tile.crossoverHingeStiffness(), 4.11, ei)
        assert(arm.maximumHingeStroke.isCloseTo(4.11))
        listOf(1.0, 10.0, 100.0, 1.0e6).forEach {
            assert(arm.hingeDisplacement(it) < 4.11)
        }
    }

    @Test
    fun `gate 2 limiting cases - an infinitely stiff arm leaves the pure rotation law`() {
        val stiff = RotatingHingeArm(13.53, 6.0, 1.0e9)
        val force = 1.0
        val theta = stiff.rotationForForce(force)
        // k theta = F r cos theta, exactly
        assert((13.53 * theta).isCloseTo(force * 6.0 * cos(theta)))
        assert(stiff.displacement(force).isCloseTo(6.0 * sin(theta), 1e-6))
    }

    // ------------------------------------------------- gate 3 — symmetry and conservation

    @Test
    fun `gate 3 symmetry - the base couple has an axis and the orthogonal one keeps only the bonds' own hinges`() {
        // the two links lie on a chord: a couple restrains rotation about the chord's
        // perpendicular bisector and contributes NOTHING about the chord itself
        val favourable = realisablePerpendicularBase(backbone, favourable = true)
        val unfavourable = realisablePerpendicularBase(backbone, favourable = false)
        assert(unfavourable.rotationalStiffness.isCloseTo(2.0 * bondHingeStiffness(1.0)))
        assert(
            (favourable.rotationalStiffness - unfavourable.rotationalStiffness)
                .isCloseTo(2.0 * bondSlideStiffness(1.0) * backbone.leverArm * backbone.leverArm)
        )
        // and the axial support is the same either way — orientation moves the couple, not the pair
        assert(favourable.axialStiffness.isCloseTo(unfavourable.axialStiffness))
    }

    @Test
    fun `gate 3 symmetry - a two-link base is exactly C-0028's one-crossover base about the free axis`() {
        // two softened bonds on a chord, read about the chord, ARE an antiparallel crossover's
        // own k_theta — the same two bonds, and C-0028's B1 to the last digit
        val unfavourable = realisablePerpendicularBase(backbone, favourable = false)
        val b1 = StandoffBase.crossovers(1)
        assert(unfavourable.rotationalStiffness.isCloseTo(b1.rotationalStiffness))
        assert(unfavourable.axialStiffness.isCloseTo(b1.axialStiffness))
    }

    @Test
    fun `gate 3 symmetry - the couple projects as cosine squared and is worst at half a quantum`() {
        // a couple k a^2 about one axis contributes k a^2 cos^2(phi) about an axis phi away
        val quantum = backbone.azimuthQuantum
        assert(couplePhaseProjection(0.0).isCloseTo(1.0))
        assert(couplePhaseProjection(quantum / 2.0).isCloseTo(cos(quantum / 2.0) * cos(quantum / 2.0)))
        assert(couplePhaseProjection(PI / 2.0).isCloseTo(0.0))
        // symmetric in the sign of the misalignment
        assert(couplePhaseProjection(0.3).isCloseTo(couplePhaseProjection(-0.3)))
        // and the worst case over a base-pair quantum costs less than a tenth of the couple
        assert(couplePhaseProjection(quantum / 2.0) > 0.9)
    }

    @Test
    fun `gate 3 symmetry - the rotating arm's law is odd and its horizontal draw-in even`() {
        val arm = RotatingHingeArm(Gen1Tile.crossoverHingeStiffness(), 4.95, ei)
        listOf(0.5, 1.5, 3.0).forEach {
            assert(arm.reaction(-it).isCloseTo(-arm.reaction(it)))
            assert(arm.horizontalDrawIn(-it).isCloseTo(arm.horizontalDrawIn(it)))
        }
        assert(arm.reaction(0.0).isCloseTo(0.0))
        assert(arm.horizontalDrawIn(0.0).isCloseTo(0.0))
    }

    @Test
    fun `gate 3 conservation - the closure search never returns a configuration that interpenetrates`() {
        val closure = bestTwoLinkClosure(backbone)
        // the standoff's own face must clear every sheet duplex it is not part of
        assert(closure.faceHeight >= seatFaceHeight(closure.centreY) - 1.0e-9)
        // and both termini sit on the standoff's own backbone radius
        assert(closure.firstTerminusRadius.isCloseTo(backbone.phosphateRadius))
        assert(closure.secondTerminusRadius.isCloseTo(backbone.phosphateRadius))
        // and no phosphate pair is closer than van der Waals contact
        assert(closure.firstGap >= BForm.PHOSPHATE_HARD_SEPARATION - 1.0e-9)
        assert(closure.secondGap >= BForm.PHOSPHATE_HARD_SEPARATION - 1.0e-9)
    }

    @Test
    fun `gate 2 limiting cases - the link window residual is zero inside the measured step and grows outside it`() {
        assert(linkWindowResidual(BForm.PHOSPHODIESTER_STEP_MIN).isCloseTo(0.0))
        assert(linkWindowResidual(BForm.PHOSPHODIESTER_STEP).isCloseTo(0.0))
        assert(linkWindowResidual(0.65).isCloseTo(0.0))
        // a pair too CLOSE cannot be bonded either — which is what minimising a bare distance misses
        assert(linkWindowResidual(0.35).isCloseTo(BForm.PHOSPHODIESTER_STEP_MIN - 0.35))
        assert(linkWindowResidual(1.20).isCloseTo(1.20 - BForm.PHOSPHODIESTER_STEP))
    }

    @Test
    fun `gate 1 dimensional consistency - the hinge arm ceiling is a cube root of a rigidity over a stiffness`() {
        // r_max = (c n EI/k)^(1/3): eight times the rigidity doubles the ceiling exactly
        assert(
            (hingeArmCeiling(bendingRigidity = 8.0 * ei) / hingeArmCeiling(bendingRigidity = ei))
                .isCloseTo(2.0)
        )
        assert((hingeArmCeiling(armFactor = 24.0) / hingeArmCeiling(armFactor = 3.0)).isCloseTo(2.0))
        assertFailsWith<IllegalArgumentException> { hingeArmCeiling(count = 0) }
    }

    @Test
    fun `gate 5 cross-check - a single-duplex cantilever arm cannot reach the desired stroke at any hinge count`() {
        // the arm's own bending is in SERIES with the hinge, so the placement condition caps the
        // arm at (c n EI/k)^(1/3) = 9.77 nm — below section 3's desired 10 nm stroke
        val ceiling = hingeArmCeiling()
        assert(ceiling.isCloseTo(9.7666, 1e-4))
        assert(ceiling < Gen1Tile.DESIRED_STROKE)
        // and every solved arm, at every hinge count, lands below that ceiling
        listOf(1, 2, 4, 8, 64, 1024).forEach { hinges ->
            val arm = rotatingArmForStiffness(
                Gen1Tile.crossoverHingeStiffness(), ei, 45, 100.0 / 3.0, 3.0, hinges
            )
            assert(arm < ceiling)
        }
        // a GUIDED arm lifts the ceiling past the desired stroke, and that is the redesign
        assert(hingeArmCeiling(armFactor = 12.0) > Gen1Tile.DESIRED_STROKE)
    }

    // ---------------------------------------------------------------- gate 4 — convergence

    @Test
    fun `gate 4 convergence - the closure search converges and is deterministic`() {
        val coarse = bestTwoLinkClosure(backbone, azimuthSteps = 90, axialSteps = 48)
        val fine = bestTwoLinkClosure(backbone, azimuthSteps = 270, axialSteps = 144)
        // three times the grid in both continuous directions moves the objective by very little
        assert(abs(fine.worstGap - coarse.worstGap) < 0.05)
        // and the same arguments return the identical configuration, not merely the same value
        val again = bestTwoLinkClosure(backbone, azimuthSteps = 270, axialSteps = 144)
        assert(again.worstGap.isCloseTo(fine.worstGap, 0.0))
        assert(again.azimuth.isCloseTo(fine.azimuth, 0.0))
        assert(again.centreX.isCloseTo(fine.centreX, 0.0))
    }

    @Test
    fun `gate 4 convergence - the rotating arm's tangent matches a central difference`() {
        val arm = RotatingHingeArm(Gen1Tile.crossoverHingeStiffness(), 4.95, ei)
        listOf(0.5, 1.5, 3.0).forEach { displacement ->
            val step = 1.0e-5
            val difference =
                (arm.reaction(displacement + step) - arm.reaction(displacement - step)) /
                        (2.0 * step)
            assert(arm.tangentStiffness(displacement).isCloseTo(difference, 1e-5))
        }
    }

    @Test
    fun `gate 4 convergence - the rotating arm's reaction inverts its own displacement`() {
        val arm = RotatingHingeArm(Gen1Tile.crossoverHingeStiffness(), 4.95, ei)
        listOf(0.2, 1.0, 2.5, 4.0).forEach { force ->
            assert(arm.reaction(arm.displacement(force)).isCloseTo(force, 1e-8))
        }
    }

    @Test
    fun `gate 4 convergence - the arm solved for placement reproduces the mandate secant`() {
        val solved = rotatingArmForStiffness(
            hingeStiffness = Gen1Tile.crossoverHingeStiffness(),
            armBendingRigidity = ei,
            count = 45,
            targetStiffness = 100.0 / 3.0,
            workingDisplacement = 3.0
        )
        val arm = RotatingHingeArm(Gen1Tile.crossoverHingeStiffness(), solved, ei)
        assert((45.0 * arm.secantStiffness(3.0)).isCloseTo(100.0 / 3.0, 1e-8))
    }

    // ------------------------------------------------- gate 5 — literature and upstream

    @Test
    fun `gate 5 cross-check - C-0028's B1 and B2 are reproduced from this task's own bond constants`() {
        assert(StandoffBase.crossovers(1).rotationalStiffness.isCloseTo(13.5294117647, 1e-9))
        assert(StandoffBase.crossovers(1).axialStiffness.isCloseTo(64.7058823529, 1e-9))
        assert(
            StandoffBase.crossovers(2, favourableOrientation = true)
                .rotationalStiffness.isCloseTo(261.1679411765, 1e-9)
        )
    }

    @Test
    fun `gate 5 cross-check - C-0028's B2 asks for a lever arm larger than a duplex's own radius`() {
        // B2's couple is 2 k_s (d/2)^2 with d/2 = 1.345 nm, against a backbone radius of 0.9 nm
        val b2Arm = Gen1Tile.INTERHELICAL_SHEET / 2.0
        assert(b2Arm > BForm.DUPLEX_RADIUS)
        assert((b2Arm / backbone.phosphateRadius).isCloseTo(1.345, 1e-9))
        assert((b2Arm / BForm.PHOSPHATE_RADIUS_NARROW).isCloseTo(1.4944444444, 1e-9))
        // and the couple goes as the square, so B2 is over the hard ceiling by more than three
        val ceiling = maximumBaseRotationalStiffness(BForm.DUPLEX_RADIUS)
        assert(
            StandoffBase.crossovers(2, favourableOrientation = true).rotationalStiffness / ceiling
                    > 3.0
        )
    }

    @Test
    fun `gate 5 cross-check - the realisable ceiling is read against C-0028's own threshold ladder`() {
        // C-0028's published threshold at its own 8 nm design length
        val threshold = baseRotationalStiffnessThreshold(8.0, 10.0)
        assert(threshold.isCloseTo(68.8, 0.01))
        // the hard ceiling — both termini diametrically opposite — clears it by a hair
        assert(maximumBaseRotationalStiffness(BForm.DUPLEX_RADIUS) > threshold)
        assert(maximumBaseRotationalStiffness(BForm.DUPLEX_RADIUS) / threshold < 1.2)
        // the nominal 120 degree backbone separation does not
        assert(maximumBaseRotationalStiffness(backbone.leverArm) < threshold)
        // and C-0028's threshold at 10 nm is out of reach at every reading
        assert(
            maximumBaseRotationalStiffness(BForm.DUPLEX_RADIUS) <
                    baseRotationalStiffnessThreshold(10.0, 10.0)
        )
    }

    @Test
    fun `gate 5 cross-check - C-0023's E5 arm and hinge share are reproduced`() {
        val solved = hingeArmForStiffness(
            hingeStiffness = Gen1Tile.crossoverHingeStiffness(),
            armBendingRigidity = ei,
            count = 45,
            targetStiffness = 100.0 / 3.0
        )
        assert(solved.isCloseTo(4.11, 0.005))
        val e5 = CrossoverHingeFlexure(Gen1Tile.crossoverHingeStiffness(), solved, ei)
        assert(e5.hingeComplianceShare.isCloseTo(0.925, 0.005))
    }

    @Test
    fun `gate 5 cross-check - E5 as filed cannot reach the desired stroke, on geometry alone`() {
        // the tip of an arm of length r cannot rise more than r, whatever the hinge constant
        val solved = hingeArmForStiffness(
            hingeStiffness = Gen1Tile.crossoverHingeStiffness(),
            armBendingRigidity = ei,
            count = 45,
            targetStiffness = 100.0 / 3.0
        )
        assert(solved < Gen1Tile.DESIRED_STROKE)
        // and even at C-0023's own 3 nm working point the rotation is far past small angle
        assert(asin(3.0 / solved) > 40.0 * PI / 180.0)
    }

    @Test
    fun `gate 3 symmetry - a column buckles about its softest axis, and the free axis is the softer one`() {
        // the two-link base restrains ONE axis; the eigenvalue about the other is strictly lower,
        // and it is the one a strut actually fails in
        listOf(3.0, 5.0, 8.0, 10.0).forEach { length ->
            val restrained = realisablePerpendicularBase(backbone, favourable = true)
            val free = realisablePerpendicularBase(backbone, favourable = false)
            val strong = standoffBucklingLoad(
                ei, length, baseRestraintParameter(restrained.rotationalStiffness, ei, length), 0.0
            )
            val weak = standoffBucklingLoad(
                ei, length, baseRestraintParameter(free.rotationalStiffness, ei, length), 0.0
            )
            assert(weak < strong)
        }
    }

    @Test
    fun `gate 3 conservation - the one-sided contact's moment capacity is a force times a half-width`() {
        assert(contactMomentCapacity(4.0, 1.0).isCloseTo(4.0))
        assert((contactMomentCapacity(4.0, 2.0) / contactMomentCapacity(4.0, 1.0)).isCloseTo(2.0))
        assert(contactMomentCapacity(0.0).isCloseTo(0.0))
        // a rocking freedom of asin(R_s/2R_h) — exactly 30 degrees for equal radii
        assert(stericTiltFreedom(1.0, 1.0).isCloseTo(PI / 6.0))
        assert(stericTiltFreedom(2.0, 1.0).isCloseTo(PI / 2.0))
    }

    @Test
    fun `gate 5 cross-check - the two-terminus ceiling is bracketed by the only measured multi-arm junction constant`() {
        // Pan et al., Nat Commun 5:5578 (2014): "a rotational stiffness of k_twist = 135 pN nm
        // rad^-1 of the scissor-like interhelical angle J_twist" — FITTED to MD, cross-validated
        // against FRET, and the only number of its kind. It restrains a DIFFERENT degree of
        // freedom, so it is used only to check the order of magnitude.
        val fourWayJunction = 135.0
        val hard = maximumBaseRotationalStiffness(BForm.DUPLEX_RADIUS)
        val nominal = maximumBaseRotationalStiffness(backbone.leverArm)
        assert(fourWayJunction / hard < 2.0)
        assert(fourWayJunction / nominal < 2.5)
        assert(fourWayJunction > hard)
    }

    @Test
    fun `gate 5 cross-check - the honeycomb and square lattices give the two quantised azimuths`() {
        assert(
            (DuplexBackbone(basePairsPerTurn = BForm.BASE_PAIRS_PER_TURN_SQUARE).azimuthQuantum *
                    180.0 / PI).isCloseTo(33.7394564, 1e-6)
        )
        assert(
            (DuplexBackbone(basePairsPerTurn = BForm.BASE_PAIRS_PER_TURN_HONEYCOMB).azimuthQuantum *
                    180.0 / PI).isCloseTo(34.2857142857, 1e-6)
        )
        // C-0015's 32 bp interface phase is exactly three turns of the square lattice
        assert((32.0 / BForm.BASE_PAIRS_PER_TURN_SQUARE).isCloseTo(2.9990627, 1e-6))
    }
}
