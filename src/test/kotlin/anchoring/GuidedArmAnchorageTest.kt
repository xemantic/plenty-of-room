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
import kotlin.math.cos
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-70` — what holds `E5g16`'s guided arm, and whether `C-0029`'s asserted `c = 12` survives its
 * own anchorage.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 * The spine of the task is that `c` is a **continuum** whose two textbook values (3, a cantilever;
 * 12, a guided arm) are its two limits, and that `ρ_far = k_far r/EI` carries the **arm length** —
 * so the cap is a fixed point rather than a formula evaluated at an asserted `c`.
 */
class GuidedArmAnchorageTest {

    private val ei = Gen1Tile.DUPLEX_BENDING_RIGIDITY

    private val mandate = 100.0 / 3.0

    private val paths = 45

    private val hinge = Gen1Tile.crossoverHingeStiffness()

    // ---------------------------------------------------------------- gate 1 — dimensional

    @Test
    fun `gate 1 dimensional consistency - the end restraint parameter is dimensionless and carries the span`() {
        // rho = k L/EI: (pN*nm/rad)*(nm)/(pN*nm^2) = 1
        assert(armRestraintParameter(78.235, 12.0, ei).isCloseTo(78.235 * 12.0 / 230.0))
        // the SAME joint on a twice longer arm is twice the restraint: C-0025's lesson, here
        val short = armRestraintParameter(78.235, 6.0, ei)
        val long = armRestraintParameter(78.235, 12.0, ei)
        assert((long / short).isCloseTo(2.0))
    }

    @Test
    fun `gate 1 dimensional consistency - the arm cap is a cube root of a rigidity over a stiffness`() {
        // eight times the rigidity doubles the cap, at any fixed anchorage-to-rigidity ratio
        val base = anchoredArmCeiling(0.0, paths, ei, mandate)
        val eight = anchoredArmCeiling(0.0, paths, 8.0 * ei, mandate)
        assert((eight / base).isCloseTo(2.0))
        // and eight times the path count likewise
        val many = anchoredArmCeiling(0.0, 8 * paths, ei, mandate)
        assert((many / base).isCloseTo(2.0))
    }

    @Test
    fun `gate 1 dimensional consistency - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { armRestraintParameter(-1.0, 12.0, ei) }
        assertFailsWith<IllegalArgumentException> { armRestraintParameter(1.0, 0.0, ei) }
        assertFailsWith<IllegalArgumentException> { guidedArmFactor(-0.1) }
        assertFailsWith<IllegalArgumentException> { twoSpringArmFactor(-1.0, 1.0) }
        assertFailsWith<IllegalArgumentException> { anchoredArmCeiling(-1.0, paths, ei, mandate) }
        assertFailsWith<IllegalArgumentException> { anchoredArmCeiling(0.0, 0, ei, mandate) }
        assertFailsWith<IllegalArgumentException> {
            TwoSpringArm(ei, -1.0, 100.0, 100.0)
        }
        assertFailsWith<IllegalArgumentException> { ArmAnchorage.twoTerminus(leverArm = -0.1) }
    }

    // ---------------------------------------------------------------- gate 2 — limiting cases

    @Test
    fun `gate 2 limiting cases - c is EXACTLY 3 at a free far end and EXACTLY 12 at a rigid one`() {
        assert(guidedArmFactor(0.0).isCloseTo(3.0))
        assert(guidedArmFactor(1.0e14).isCloseTo(12.0, 1e-12))
        // the two textbook values are a factor of exactly four apart
        assert((guidedArmFactor(1.0e14) / guidedArmFactor(0.0)).isCloseTo(4.0, 1e-12))
        // and the halfway point is at rho = 2, exactly
        assert(guidedArmFactor(2.0).isCloseTo(6.0))
    }

    @Test
    fun `gate 2 limiting cases - c is strictly monotone in the far restraint`() {
        val restraints = listOf(0.0, 0.25, 0.5, 1.0, 2.0, 4.0, 8.0, 16.0, 64.0, 256.0, 1024.0)
        val factors = restraints.map { guidedArmFactor(it) }
        factors.zipWithNext().forEach { (low, high) -> assert(high > low) }
        assert(factors.first().isCloseTo(3.0))
        assert(factors.last() < 12.0)
    }

    @Test
    fun `gate 2 limiting cases - the two-spring factor has all four textbook corners`() {
        val big = 1.0e14
        // both ends rigid: the guided arm, exactly 12
        assert(twoSpringArmFactor(big, big).isCloseTo(12.0, 1e-10))
        // one end rigid, the other free: a cantilever, exactly 3, in BOTH orders
        assert(twoSpringArmFactor(big, 0.0).isCloseTo(3.0, 1e-10))
        assert(twoSpringArmFactor(0.0, big).isCloseTo(3.0, 1e-10))
        // both ends free: a MECHANISM, exactly zero — not a weaker beam
        assert(twoSpringArmFactor(0.0, 0.0).isCloseTo(0.0))
    }

    @Test
    fun `gate 2 limiting cases - the two-spring factor is symmetric in its two joints`() {
        listOf(0.3 to 7.0, 1.0 to 40.0, 4.2 to 4.2, 11.5 to 0.0).forEach { (a, b) ->
            assert(twoSpringArmFactor(a, b).isCloseTo(twoSpringArmFactor(b, a)))
        }
    }

    @Test
    fun `gate 2 limiting cases - a rigid near end reduces the two-spring factor to c of the far one`() {
        listOf(0.0, 0.5, 2.0, 4.164, 37.1, 1000.0).forEach { far ->
            assert(twoSpringArmFactor(1.0e14, far).isCloseTo(guidedArmFactor(far), 1e-8))
        }
    }

    @Test
    fun `gate 2 limiting cases - the far end of a guided arm does not rotate and a free one does`() {
        val guided = TwoSpringArm(ei, 12.0, 216.5, 1.0e12)
        assert(abs(guided.farRotation(3.0)) < 1e-9)
        val free = TwoSpringArm(ei, 12.0, 216.5, 0.0)
        assert(free.farRotation(3.0) > 0.0)
        // and a free far end carries no moment while a guided one does
        assert(abs(free.farMoment(3.0)) < 1e-9)
        assert(guided.farMoment(3.0) > 0.0)
    }

    // ------------------------------------------------- gate 3 — symmetry and conservation

    @Test
    fun `gate 3 conservation - the series composition is EXACT at a free far end and WRONG at a held one`() {
        // C-0023's 1/k = r^2/(n k_theta) + r^3/(c EI) is the hinge's rigid rotation in series with
        // the arm's own bending. It is exact only when the far end carries no moment.
        val free = TwoSpringArm(ei, 12.0, 16.0 * hinge, 0.0)
        assert(free.seriesStiffness.isCloseTo(free.stiffness, 1e-12))
        val held = TwoSpringArm(ei, 12.0, 16.0 * hinge, 1.0e12)
        assert(held.seriesStiffness < held.stiffness)
        // the guide carries part of the moment and RELIEVES the hinge, so the series reading is soft
        assert(held.seriesDeparture < 1.0)
    }

    @Test
    fun `gate 3 conservation - the two end moments and the applied force are in equilibrium`() {
        listOf(
            TwoSpringArm(ei, 11.0, 216.5, 78.235),
            TwoSpringArm(ei, 14.0, 100.0, 683.0),
            TwoSpringArm(ei, 9.0, 40.0, 13.53)
        ).forEach { arm ->
            val delta = 2.0
            val shear = arm.stiffness * delta
            // the couple the two end moments make must equal the shear's moment about the far end
            assert(
                (arm.nearMoment(delta) + arm.farMoment(delta)).isCloseTo(shear * arm.length, 1e-9)
            )
        }
    }

    @Test
    fun `gate 3 conservation - the two-link couple has an axis and the orthogonal one is a crossover`() {
        // C-0029's counting theorem, applied at the arm's FAR end: two termini on a chord restrain
        // the perpendicular bisector only, and about the chord itself nothing is left but the
        // bonds' own hinges — which IS one antiparallel crossover, to the last digit.
        val anchorage = ArmAnchorage.twoTerminus(leverArm = 1.0)
        assert(anchorage.chordAxisStiffness.isCloseTo(2.0 * bondHingeStiffness()))
        assert(anchorage.chordAxisStiffness.isCloseTo(Gen1Tile.crossoverHingeStiffness()))
        assert(anchorage.rotationalStiffness > anchorage.chordAxisStiffness)
    }

    @Test
    fun `gate 3 conservation - the phase projection is even and costs less than a tenth of a quantum`() {
        val quantum = DuplexBackbone().azimuthQuantum
        assert(couplePhaseProjection(quantum / 2.0).isCloseTo(couplePhaseProjection(-quantum / 2.0)))
        assert(couplePhaseProjection(0.0).isCloseTo(1.0))
        val worst = couplePhaseProjection(quantum / 2.0)
        assert(worst > 0.9)
        assert(worst < 1.0)
        assert(worst.isCloseTo(cos(quantum / 2.0) * cos(quantum / 2.0)))
    }

    // ---------------------------------------------------- gate 4 — numerical convergence

    @Test
    fun `gate 4 convergence - the cap is a FIXED POINT and reproduces both textbook caps at its ends`() {
        // k_far = 0 gives the cantilever's cap exactly, and it is BELOW the desired 10 nm stroke
        val cantilever = anchoredArmCeiling(0.0, paths, ei, mandate)
        assert(cantilever.isCloseTo(hingeArmCeiling(3.0, paths, ei, mandate), 1e-9))
        assert(cantilever < Gen1Tile.DESIRED_STROKE)
        // an infinitely stiff anchorage gives the guided cap exactly
        val guided = anchoredArmCeiling(1.0e12, paths, ei, mandate)
        assert(guided.isCloseTo(hingeArmCeiling(12.0, paths, ei, mandate), 1e-6))
        // and the fixed point satisfies its own defining equation
        listOf(13.53, 78.235, 683.0, 3857.9).forEach { far ->
            val r = anchoredArmCeiling(far, paths, ei, mandate)
            val c = guidedArmFactor(armRestraintParameter(far, r, ei))
            assert(r.isCloseTo(Math.cbrt(c * paths * ei / mandate), 1e-9))
        }
    }

    @Test
    fun `gate 4 convergence - the placed arm reproduces its own target secant`() {
        listOf(13.53, 78.235, 683.0).forEach { far ->
            val r = anchoredArmForStiffness(hinge, 16, far, ei, paths, mandate, 3.0)
            val c = guidedArmFactor(armRestraintParameter(far, r, ei))
            val element = RotatingHingeArm(hinge, r, ei, 16, c)
            assert((paths * element.secantStiffness(3.0)).isCloseTo(mandate, 1e-7))
        }
    }

    @Test
    fun `gate 4 convergence - the placed arm is strictly below its own cap`() {
        listOf(13.53, 78.235, 683.0, 3857.9).forEach { far ->
            val r = anchoredArmForStiffness(hinge, 16, far, ei, paths, mandate, 3.0)
            assert(r < anchoredArmCeiling(far, paths, ei, mandate))
        }
    }

    @Test
    fun `gate 4 convergence - the two-spring placement solves its own equation and brackets the arm`() {
        val far = ArmAnchorage.twoTerminus(leverArm = 1.0).rotationalStiffness
        val r = twoSpringArmForStiffness(hinge, 16, far, ei, paths, mandate)
        val beam = TwoSpringArm(ei, r, 16.0 * hinge, far)
        assert((paths * beam.stiffness).isCloseTo(mandate, 1e-9))
        // in this reading the placement length and the cap are the SAME equation
        assert(r.isCloseTo(Math.cbrt(beam.armFactor * paths * ei / mandate), 1e-9))
        // and it lands ABOVE the large-rotation series reading, because the series one is soft
        assert(r > anchoredArmForStiffness(hinge, 16, far, ei, paths, mandate, 3.0))
        // both are below the ideal guide's cap, and both reach the desired stroke
        assert(r < hingeArmCeiling(12.0, paths, ei, mandate))
        assert(r > Gen1Tile.DESIRED_STROKE)
    }

    @Test
    fun `CH - C-0029's E5g16 is over-placed once its own c of twelve is taken as a boundary condition`() {
        // C-0029 solved 12.242 nm from the SERIES composition at c = 12. Read as the two-spring
        // beam its own c = 12 describes, the same geometry is far stiffer than the mandate.
        val beam = TwoSpringArm(ei, 12.2423721, 16.0 * hinge, 1.0e12)
        assert(paths * beam.stiffness > mandate)
        assert(paths * beam.stiffness > 40.0)
        // and the correctly placed arm on the REALISED anchorage still clears the desired stroke
        val far = ArmAnchorage.twoTerminus(leverArm = 1.0).rotationalStiffness
        assert(twoSpringArmForStiffness(hinge, 16, far, ei, paths, mandate) > Gen1Tile.DESIRED_STROKE)
    }

    // ---------------------------------------------- gate 5 — literature and upstream

    @Test
    fun `gate 5 upstream - C-0029's E5g16 reproduces at its asserted c of twelve`() {
        val arm = rotatingArmForStiffness(hinge, ei, paths, mandate, 3.0, 16, 12.0)
        assert(arm.isCloseTo(12.242, 1e-3))
        // C-0029's own result file carries 15.5029478 and 9.76624511; its gate-5 PROSE quotes
        // "15.5005", which is a transcription slip in the claim text — the number reproduces.
        assert(hingeArmCeiling(12.0, paths, ei, mandate).isCloseTo(15.5029478, 1e-7))
        assert(hingeArmCeiling(3.0, paths, ei, mandate).isCloseTo(9.76624511, 1e-7))
    }

    @Test
    fun `gate 5 upstream - the anchorage catalogue reproduces C-0025's and C-0029's joints`() {
        // C-0029's hard two-terminus ceiling, at the 180 degree convention-free reading
        assert(ArmAnchorage.twoTerminus(leverArm = 1.0).rotationalStiffness.isCloseTo(78.235, 1e-4))
        // C-0025's J2: a single-nicked continuation, one backbone intact — a clamp
        assert(
            ArmAnchorage.nickedContinuation().rotationalStiffness
                .isCloseTo(FlexureEndJoint.nickedContinuation().rotationalStiffness)
        )
        // C-0025's J4-2: a two-crossover clamp at the 32 bp pitch
        assert(
            ArmAnchorage.multiCrossoverClamp(2).rotationalStiffness
                .isCloseTo(FlexureEndJoint.multiCrossoverClamp(2).rotationalStiffness)
        )
        // C-0029's R3: one covalent link is a BALL JOINT and restrains nothing
        assert(ArmAnchorage.singleLink().rotationalStiffness.isCloseTo(0.0))
    }

    @Test
    fun `gate 5 upstream - a doubly nicked far end IS a crossover, exactly as C-0025 found`() {
        assert(
            ArmAnchorage.doublyNickedContinuation().chordAxisStiffness
                .isCloseTo(Gen1Tile.crossoverHingeStiffness())
        )
    }

    // ----------------------------------------------------------- the acceptance predicates

    @Test
    fun `P1 and P2 - the realised c is strictly inside the bracket, not at either textbook end`() {
        val far = ArmAnchorage.twoTerminus(leverArm = 1.0).rotationalStiffness
        val r = anchoredArmForStiffness(hinge, 16, far, ei, paths, mandate, 3.0)
        val c = guidedArmFactor(armRestraintParameter(far, r, ei))
        assert(c > 3.0)
        assert(c < 12.0)
        // and it is NOT within a per cent of either end — the continuum is doing real work
        assert(c > 3.5)
        assert(c < 11.0)
    }

    @Test
    fun `P3 - the cap on EVERY two-link anchorage clears the desired 10 nm stroke`() {
        listOf(
            ArmAnchorage.twoTerminus(leverArm = 1.0),
            ArmAnchorage.twoTerminus(leverArm = 0.866),
            ArmAnchorage.nickedContinuation(),
            ArmAnchorage.multiCrossoverClamp(2)
        ).forEach { anchorage ->
            assert(
                anchoredArmCeiling(anchorage.rotationalStiffness, paths, ei, mandate) >
                        Gen1Tile.DESIRED_STROKE
            )
        }
        // even the UNFAVOURABLE axis — the chord reading, C-0029's B1 — clears it
        val chord = ArmAnchorage.twoTerminus(leverArm = 1.0).chordAxisStiffness
        assert(anchoredArmCeiling(chord, paths, ei, mandate) > Gen1Tile.DESIRED_STROKE)
        // and a single covalent link does NOT: it is the cantilever, 9.77 nm
        assert(
            anchoredArmCeiling(ArmAnchorage.singleLink().rotationalStiffness, paths, ei, mandate) <
                    Gen1Tile.DESIRED_STROKE
        )
    }

    @Test
    fun `P4 - a placed design exists whose arm reaches the desired stroke inside the ceiling`() {
        val far = ArmAnchorage.twoTerminus(leverArm = 1.0).rotationalStiffness
        val r = anchoredArmForStiffness(hinge, 16, far, ei, paths, mandate, 3.0)
        assert(r > Gen1Tile.DESIRED_STROKE)
        val c = guidedArmFactor(armRestraintParameter(far, r, ei))
        val element = RotatingHingeArm(hinge, r, ei, 16, c)
        assert(paths * element.tangentStiffness(3.0) < 40.0)
        assert(paths * element.tangentStiffness(Gen1Tile.DESIRED_STROKE) < 40.0)
    }

    @Test
    fun `P5 - the anchorage restrains the axis the arm bends about, at a bounded phase cost`() {
        // the arm's working bending is about the y axis; the chord is a diameter of the duplex
        // cross-section, so laying it NORMAL to the sheet puts the couple on exactly that axis.
        val anchorage = ArmAnchorage.twoTerminus(leverArm = 1.0)
        val quantum = DuplexBackbone().azimuthQuantum
        val worst = anchorage.rotationalStiffness * couplePhaseProjection(quantum / 2.0) +
                anchorage.chordAxisStiffness * (1.0 - couplePhaseProjection(quantum / 2.0))
        // the base-pair quantum can cost at most ~8 % of the couple
        assert(worst / anchorage.rotationalStiffness > 0.9)
        // and even at the worst phase the cap still clears the desired stroke
        assert(anchoredArmCeiling(worst, paths, ei, mandate) > Gen1Tile.DESIRED_STROKE)
    }

    @Test
    fun `P6 - the couple the anchorage reacts demands a bonded length inside CH-0029's ladder`() {
        val far = ArmAnchorage.twoTerminus(leverArm = 1.0).rotationalStiffness
        val r = anchoredArmForStiffness(hinge, 16, far, ei, paths, mandate, 3.0)
        val c = guidedArmFactor(armRestraintParameter(far, r, ei))
        val element = RotatingHingeArm(hinge, r, ei, 16, c)
        listOf(3.0, Gen1Tile.DESIRED_STROKE).forEach { stroke ->
            val force = abs(element.reaction(stroke))
            val moment = farAnchorageMoment(force, r, far, ei)
            val link = farAnchorageLinkForce(moment, 1.0)
            // CH-0029's ladder, inverted: it throws if no bonded length carries the load at all
            assert(bondedLengthForTension(link) < 30.0)
        }
        // and the anchorage supports the arm transversely with orders to spare
        val support = ArmAnchorage.twoTerminus(leverArm = 1.0).transverseStiffness
        assert(support / (mandate / paths) > 10.0)
    }

    @Test
    fun `gate 2 limiting cases - the anchorage moment is zero at a ball joint and F L over two at a guide`() {
        assert(farAnchorageMoment(5.0, 11.0, 0.0, ei).isCloseTo(0.0))
        assert(farAnchorageMoment(5.0, 11.0, 1.0e12, ei).isCloseTo(0.5 * 5.0 * 11.0, 1e-8))
        assert(
            farAnchorageMoment(5.0, 11.0, Double.POSITIVE_INFINITY, ei)
                .isCloseTo(0.5 * 5.0 * 11.0)
        )
        // it is monotone in the restraint, so a stiffer guide relieves the hinge MORE and costs MORE
        val soft = farAnchorageMoment(5.0, 11.0, 13.53, ei)
        val stiff = farAnchorageMoment(5.0, 11.0, 683.0, ei)
        assert(stiff > soft)
        assertFailsWith<IllegalArgumentException> { farAnchorageLinkForce(1.0, 0.0) }
    }

    // ------------------------------------------------------------------- the cheap bounds

    @Test
    fun `cheap bound - EI over a 12 nm arm is 19 pN nm per rad, so rho_far lands in the interior`() {
        val perRadian = ei / 12.0
        assert(perRadian.isCloseTo(230.0 / 12.0))
        val rho = armRestraintParameter(78.235, 12.0, ei)
        assert(rho > 1.0)
        assert(rho < 10.0)
        // neither textbook value applies, which is the whole reason the continuum is needed
        assert(guidedArmFactor(rho) > 4.0)
        assert(guidedArmFactor(rho) < 9.0)
    }

    @Test
    fun `cheap bound - the arm factor is bounded above by 12 at every finite anchorage`() {
        listOf(1.0, 13.53, 78.235, 683.0, 3857.9, 1.0e6).forEach { far ->
            assert(guidedArmFactor(armRestraintParameter(far, 12.0, ei)) < 12.0)
        }
    }

    @Test
    fun `the arm's own bending is the DOMINANT compliance term, which is leaf A8-2's ask`() {
        val far = ArmAnchorage.twoTerminus(leverArm = 1.0).rotationalStiffness
        val r = anchoredArmForStiffness(hinge, 16, far, ei, paths, mandate, 3.0)
        val c = guidedArmFactor(armRestraintParameter(far, r, ei))
        val element = CrossoverHingeFlexure(hinge, r, ei, 16, c)
        // once the anchorage is realised rather than asserted, the arm carries more than the hinge
        assert(element.hingeComplianceShare < 0.5)
        assert(element.hingeComplianceShare > 0.0)
    }

    @Test
    fun `the two-spring rotation split shows the guide is the SOFTER of the two joints here`() {
        val far = ArmAnchorage.twoTerminus(leverArm = 1.0).rotationalStiffness
        val arm = TwoSpringArm(ei, 11.0, 16.0 * hinge, far)
        // the far anchorage is softer than sixteen crossovers, so it rotates more than the hinge
        assert(arm.farRotation(3.0) > arm.nearRotation(3.0))
        assert(arm.farRotation(3.0) * 180.0 / PI < 90.0)
    }
}
