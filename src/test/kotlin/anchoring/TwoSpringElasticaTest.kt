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
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-79` — a **large-rotation two-spring elastica** for `E5`'s arm: the composition that is exact
 * in the rotation *and* in the end condition, which neither of the two readings that bracket the
 * arm at 11.03–12.50 nm is.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 * The spine of the task is that the elastica's **vanishing-load limit is `C-0034`'s closed-form
 * `c(ρ_n, ρ_f)`** — which pins the field equation, both boundary conditions and every sign at once
 * — and that everything the exact solve then adds is *geometric*.
 */
class TwoSpringElasticaTest {

    private val ei = Gen1Tile.DUPLEX_BENDING_RIGIDITY

    private val hinge = Gen1Tile.crossoverHingeStiffness()

    private val mandate = 100.0 / 3.0

    private val paths = 45

    /** `C-0029`'s two-terminus couple, the adopted anchorage — `C-0034`'s `A2`. */
    private val anchorage = maximumBaseRotationalStiffness(BForm.PHOSPHATE_RADIUS)

    private fun designArm(steps: Int = 200) = TwoSpringElastica(
        bendingRigidity = ei,
        length = 12.5,
        nearStiffness = 16 * hinge,
        farStiffness = anchorage,
        steps = steps
    )

    // ---------------------------------------------------------------- gate 1 — dimensional

    @Test
    fun `gate 1 dimensional consistency - the vanishing-load stiffness is a rigidity over a cubed length`() {
        val base = TwoSpringElastica(ei, 10.0, 16 * hinge, anchorage, steps = 200)
        // c(rho) is invariant if rho = kL/EI is: scale lengths by 2 and EI by 8, springs by 4
        val scaled = TwoSpringElastica(8 * ei, 20.0, 4 * (16 * hinge), 4 * anchorage, steps = 200)
        assert(scaled.nearRestraint.isCloseTo(base.nearRestraint))
        assert(scaled.farRestraint.isCloseTo(base.farRestraint))
        // k = c EI/L^3 is then unchanged: 8/2^3 = 1
        assert(scaled.smallRotationStiffness.isCloseTo(base.smallRotationStiffness))
    }

    @Test
    fun `gate 1 dimensional consistency - the NONLINEAR solve obeys the same similarity`() {
        // Under x -> lambda x, EI -> mu EI, k_spring -> (mu/lambda) k_spring, a displacement
        // lambda*delta needs a force (mu/lambda^2)*F. Nothing in the solver imposes this.
        val lambda = 2.0
        val mu = 8.0
        val base = TwoSpringElastica(ei, 12.0, 16 * hinge, anchorage, steps = 200)
        val scaled = TwoSpringElastica(
            mu * ei, lambda * 12.0, (mu / lambda) * 16 * hinge, (mu / lambda) * anchorage,
            steps = 200
        )
        val force = base.forceForDisplacement(4.0)
        val scaledForce = scaled.forceForDisplacement(lambda * 4.0)
        assert(scaledForce.isCloseTo(mu / (lambda * lambda) * force, 1e-8))
    }

    @Test
    fun `gate 1 dimensional consistency - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> {
            TwoSpringElastica(-1.0, 10.0, hinge, anchorage)
        }
        assertFailsWith<IllegalArgumentException> {
            TwoSpringElastica(ei, 0.0, hinge, anchorage)
        }
        assertFailsWith<IllegalArgumentException> {
            TwoSpringElastica(ei, 10.0, -1.0, anchorage)
        }
        assertFailsWith<IllegalArgumentException> {
            TwoSpringElastica(ei, 10.0, hinge, -1.0)
        }
        assertFailsWith<IllegalArgumentException> {
            TwoSpringElastica(ei, 10.0, hinge, anchorage, steps = 1)
        }
        // both springs free is a MECHANISM, not a soft beam: it carries no transverse load at all
        assertFailsWith<IllegalArgumentException> { TwoSpringElastica(ei, 10.0, 0.0, 0.0) }
        // the chord bound is undefined for a stroke past the arm's own contour
        assertFailsWith<IllegalArgumentException> { chordDrawInBound(10.0, 11.0) }
    }

    // ---------------------------------------------------------------- gate 2 — limiting cases

    @Test
    fun `gate 2 limiting cases - the vanishing-load limit reproduces C-0034's two-spring factor at all four corners`() {
        val corners = listOf(
            Triple(Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, 12.0),
            Triple(Double.POSITIVE_INFINITY, 0.0, 3.0),
            Triple(0.0, Double.POSITIVE_INFINITY, 3.0)
        )
        corners.forEach { (near, far, expected) ->
            val beam = TwoSpringElastica(
                ei, 12.0,
                if (near.isInfinite()) Double.POSITIVE_INFINITY else near,
                if (far.isInfinite()) Double.POSITIVE_INFINITY else far,
                steps = 400
            )
            val force = 1.0e-7
            val state = beam.stateAtForce(force)
            val factor = force / state.displacement * 12.0 * 12.0 * 12.0 / ei
            assert(factor.isCloseTo(expected, 1e-6))
            assert(beam.smallRotationArmFactor.isCloseTo(expected))
        }
        // and the fourth corner, (0,0) = 0, is a mechanism — it cannot be built at all
        assert(twoSpringArmFactor(0.0, 0.0).isCloseTo(0.0))
    }

    @Test
    fun `gate 2 limiting cases - the vanishing-load limit reproduces the two-spring factor over the INTERIOR`() {
        val length = 12.0
        listOf(0.1, 0.7, 3.75, 11.3, 60.0).forEach { rhoNear ->
            listOf(0.0, 0.25, 4.082, 32.76, 201.2).forEach { rhoFar ->
                val beam = TwoSpringElastica(
                    ei, length, rhoNear * ei / length, rhoFar * ei / length, steps = 400
                )
                val force = 1.0e-7
                val factor = force / beam.stateAtForce(force).displacement *
                        length * length * length / ei
                assert(factor.isCloseTo(twoSpringArmFactor(rhoNear, rhoFar), 1e-6))
            }
        }
    }

    @Test
    fun `gate 2 limiting cases - a rigid arm on a free far end is C-0029's exact rotation law`() {
        // EI -> infinity with k_far = 0 leaves k theta = F L cos theta and delta = L sin theta
        val length = 12.0
        val spring = 16 * hinge
        val beam = TwoSpringElastica(1.0e9, length, spring, 0.0, steps = 400)
        listOf(1.0, 3.0, 6.0).forEach { displacement ->
            val exact = RotatingHingeArm(hinge, length, 1.0e9, 16, 3.0)
            val force = beam.forceForDisplacement(displacement)
            assert(force.isCloseTo(abs(exact.reaction(displacement)), 1e-5))
        }
    }

    @Test
    fun `gate 2 limiting cases - a free far end at small load is C-0023's series composition`() {
        val length = 11.0
        val beam = TwoSpringElastica(ei, length, 16 * hinge, 0.0, steps = 400)
        val series = 1.0 / (
                length * length / (16 * hinge) + length * length * length / (3.0 * ei)
                )
        val force = 1.0e-7
        assert((force / beam.stateAtForce(force).displacement).isCloseTo(series, 1e-6))
    }

    @Test
    fun `gate 2 limiting cases - the draw-in is quadratic in the stroke and respects the chord bound`() {
        val beam = designArm(400)
        val small = beam.stateAtDisplacement(0.2).drawIn / 0.04
        val smaller = beam.stateAtDisplacement(0.1).drawIn / 0.01
        assert(small.isCloseTo(smaller, 1e-3))
        // and never below the inextensible chord bound, at any stroke
        listOf(1.0, 3.0, 6.0, 10.0).forEach { displacement ->
            val drawIn = beam.stateAtDisplacement(displacement).drawIn
            assert(drawIn >= chordDrawInBound(beam.length, displacement))
        }
    }

    @Test
    fun `gate 2 limiting cases - the chord bound is exact geometry and needs no elastica`() {
        assert(chordDrawInBound(12.5, 10.0).isCloseTo(12.5 - sqrt(12.5 * 12.5 - 100.0)))
        assert(chordDrawInBound(12.5, 0.0).isCloseTo(0.0))
        assert(chordDrawInBound(10.0, 10.0).isCloseTo(10.0))
    }

    @Test
    fun `gate 2 limiting cases - the axially HELD reading is a strain bound, not a stiffer beam`() {
        // Holding the ends at their original axial separation while offsetting them by delta puts
        // the chord at sqrt(L^2 + delta^2), which the contour must reach: the arm must STRETCH.
        assert(restrainedAxialStrainBound(12.5, 0.0).isCloseTo(0.0))
        assert(
            restrainedAxialStrainBound(12.5, 3.0)
                .isCloseTo(sqrt(1.0 + (3.0 / 12.5) * (3.0 / 12.5)) - 1.0)
        )
        val strain = restrainedAxialStrainBound(12.5, 10.0)
        val loose = restrainedAxialStrainBound(12.5, 3.0)
        assert(strain > loose)
        assert(
            restrainedTensionBound(12.5, 10.0)
                .isCloseTo(Gen1Tile.DUPLEX_STRETCH_MODULUS * strain)
        )
    }

    // ---------------------------------------------- gate 3 — symmetry and conservation

    @Test
    fun `gate 3 symmetry - the reaction is ODD and the draw-in EVEN`() {
        val beam = designArm(200)
        listOf(0.5, 3.0, 7.0).forEach { displacement ->
            assert(beam.reaction(-displacement).isCloseTo(-beam.reaction(displacement), 1e-9))
            assert(
                beam.stateAtDisplacement(-displacement).drawIn
                    .isCloseTo(beam.stateAtDisplacement(displacement).drawIn, 1e-9)
            )
        }
    }

    @Test
    fun `gate 3 conservation - the global moment balance holds, with and without an axial force`() {
        val beam = designArm(400)
        listOf(0.5, 3.0, 10.0).forEach { force ->
            listOf(0.0, 5.0, -3.0).forEach { axial ->
                val state = beam.stateAtForce(force, axial)
                // EI(phi'(0) - phi'(L)) = F x(L) - H z(L), the two sides independently computed
                val left = state.nearMoment - state.farMoment
                val right = force * state.tipAxial - axial * state.displacement
                assert(left.isCloseTo(right, 1e-10))
                assert(abs(state.momentBalanceResidual) <= 1e-9 * abs(right))
            }
        }
    }

    @Test
    fun `gate 3 conservation - the external work equals the stored strain energy`() {
        val beam = designArm(400)
        listOf(1.0, 3.0, 8.0).forEach { displacement ->
            val samples = 400
            var work = 0.0
            for (i in 1..samples) {
                val a = displacement * (i - 1.0) / samples
                val b = displacement * i.toDouble() / samples
                val mid = 0.5 * (a + b)
                work += beam.forceForDisplacement(mid) * (b - a)
            }
            assert(work.isCloseTo(beam.stateAtDisplacement(displacement).strainEnergy, 2e-5))
        }
    }

    @Test
    fun `gate 3 conservation - the first integral of the field equation is constant along the arm`() {
        val beam = designArm(400)
        listOf(0.5, 3.0, 12.0).forEach { force ->
            val state = beam.stateAtForce(force)
            assert(state.firstIntegralSpread <= 1e-8)
        }
    }

    @Test
    fun `gate 3 symmetry - Maxwell-Betti reciprocity between two independently integrated off-diagonals`() {
        // The tip TRANSLATION under a unit tip MOMENT (an integration of sin phi over the arm)
        // against the tip ROTATION under a unit tip FORCE (an endpoint of the shooting solve).
        // Two different quadratures; nothing in the construction forces them to agree.
        val beam = designArm(400)
        val probe = 1.0e-6
        val underMoment = beam.stateAtForce(0.0, 0.0, probe).displacement / probe
        val underForce = beam.stateAtForce(probe).farRotation / probe
        assert(underMoment.isCloseTo(underForce, 1e-7))
    }

    // ---------------------------------------------------------------- gate 4 — convergence

    @Test
    fun `gate 4 convergence - the solve is fourth order in the step count under NESTED refinement`() {
        val fine = TwoSpringElastica(ei, 12.5, 16 * hinge, anchorage, steps = 1600)
            .forceForDisplacement(10.0)
        val errors = listOf(100, 200, 400).map { steps ->
            abs(
                TwoSpringElastica(ei, 12.5, 16 * hinge, anchorage, steps = steps)
                    .forceForDisplacement(10.0) / fine - 1.0
            )
        }
        // strictly decreasing, and each doubling wins at least an order of magnitude
        assert(errors[1] < errors[0] / 8.0)
        assert(errors[2] < errors[1] / 8.0)
        assert(errors[2] < 1e-8)
    }

    @Test
    fun `gate 4 convergence - the placement discharges its own secant condition`() {
        val arm = elasticaArmForStiffness(
            hingeStiffness = hinge,
            hingeCount = 16,
            farStiffness = anchorage,
            bendingRigidity = ei,
            count = paths,
            targetStiffness = mandate,
            workingDisplacement = 3.0,
            steps = 200
        )
        val beam = TwoSpringElastica(ei, arm, 16 * hinge, anchorage, steps = 200)
        assert((paths * beam.secantStiffness(3.0)).isCloseTo(mandate, 1e-7))
    }

    @Test
    fun `gate 4 convergence - the cap is the placement at a RIGID hinge and bounds every placement`() {
        val cap = elasticaArmCeiling(
            farStiffness = anchorage,
            count = paths,
            bendingRigidity = ei,
            targetStiffness = mandate,
            workingDisplacement = 3.0,
            steps = 200
        )
        val rigid = TwoSpringElastica(
            ei, cap, Double.POSITIVE_INFINITY, anchorage, steps = 200
        )
        assert((paths * rigid.secantStiffness(3.0)).isCloseTo(mandate, 1e-7))
        listOf(8, 16, 32, 64).forEach { count ->
            val arm = elasticaArmForStiffness(
                hinge, count, anchorage, ei, paths, mandate, 3.0, steps = 200
            )
            assert(arm < cap)
        }
    }

    @Test
    fun `gate 4 convergence - the tangent agrees with a central difference at two step sizes`() {
        val beam = designArm(400)
        listOf(3.0, 8.0).forEach { displacement ->
            val coarse = (beam.reaction(displacement + 1e-3) - beam.reaction(displacement - 1e-3)) /
                    2e-3
            val fine = (beam.reaction(displacement + 1e-4) - beam.reaction(displacement - 1e-4)) /
                    2e-4
            assert(beam.tangentStiffness(displacement).isCloseTo(coarse, 1e-5))
            assert(beam.tangentStiffness(displacement).isCloseTo(fine, 1e-5))
        }
    }

    @Test
    fun `gate 4 convergence - an axial tension reduces the draw-in monotonically but never to zero`() {
        val beam = designArm(400)
        val free = beam.stateAtDisplacement(3.0).drawIn
        val pulled = beam.stateAtDisplacement(3.0, axialForce = 20.0).drawIn
        assert(pulled < free)
        // and no tension whatever removes it: an inextensible arm CANNOT be axially restrained
        assert(pulled >= chordDrawInBound(beam.length, 3.0))
        val harder = beam.stateAtDisplacement(3.0, axialForce = 50.0).drawIn
        assert(harder < pulled)
        assert(harder >= chordDrawInBound(beam.length, 3.0))
        // and the solver DECLARES where its own shooting stops being conditioned rather than
        // returning a wrong root: |H| L^2/EI is the amplification exponent squared
        assertFailsWith<IllegalArgumentException> {
            beam.stateAtDisplacement(3.0, axialForce = 200.0)
        }
    }

    @Test
    fun `gate 4 convergence - the maximum rotation is reported and stays inside the solver's branch`() {
        val beam = designArm(400)
        // monotone in the stroke, and below a right angle at both of §3's strokes: past pi/2 the
        // tip force's moment arm reverses and the elastica acquires branches this solver does not
        // enumerate, which is why it is carried as a field rather than assumed away
        val acceptable = beam.stateAtDisplacement(3.0).maximumRotation
        val desired = beam.stateAtDisplacement(10.0).maximumRotation
        assert(desired > acceptable)
        assert(desired < PI / 2.0)
        assert(desired >= abs(beam.stateAtDisplacement(10.0).farRotation))
    }

    @Test
    fun `gate 4 convergence - a stroke the arm cannot reach is REFUSED, not approximated`() {
        val beam = designArm(200)
        // a tip cannot rise past its own contour
        assertFailsWith<IllegalArgumentException> { beam.forceForDisplacement(beam.length) }
        assertFailsWith<IllegalArgumentException> { beam.forceForDisplacement(2.0 * beam.length) }
        assertFailsWith<IllegalArgumentException> { beam.forceForDisplacement(0.0) }
    }

    // ------------------------------------------------- gate 5 — literature and upstream

    @Test
    fun `gate 5 upstream - C-0034's two placements and their realised end factors reproduce`() {
        val seriesArm = anchoredArmForStiffness(
            hinge, 16, anchorage, ei, paths, mandate, 3.0
        )
        val bvpArm = twoSpringArmForStiffness(hinge, 16, anchorage, ei, paths, mandate)
        assert(seriesArm.isCloseTo(11.028, 1e-4))
        assert(bvpArm.isCloseTo(12.496, 1e-4))
        assert(
            guidedArmFactor(armRestraintParameter(anchorage, seriesArm, ei))
                .isCloseTo(7.356, 1e-3)
        )
        assert(
            TwoSpringArm(ei, bvpArm, 16 * hinge, anchorage).armFactor.isCloseTo(6.284, 1e-3)
        )
    }

    @Test
    fun `gate 5 upstream - C-0029's asserted design and both hinge-arm ceilings reproduce`() {
        assert(
            rotatingArmForStiffness(hinge, ei, paths, mandate, 3.0, 16, 12.0)
                .isCloseTo(12.2423721, 1e-6)
        )
        assert(hingeArmCeiling(3.0, paths, ei, mandate).isCloseTo(9.76624511, 1e-6))
        assert(hingeArmCeiling(12.0, paths, ei, mandate).isCloseTo(15.5029478, 1e-6))
        assert(anchorage.isCloseTo(78.2352941, 1e-6))
    }

    @Test
    fun `gate 5 upstream - CH-0044's over-placement reproduces on the elastica's own linear limit`() {
        // 45 guided-far-end BVP arms of C-0029's own 12.2424 nm assemble to 54.61 pN/nm
        val beam = TwoSpringElastica(
            ei, 12.2423721, 16 * hinge, Double.POSITIVE_INFINITY, steps = 400
        )
        assert((paths * beam.smallRotationStiffness).isCloseTo(54.61, 1e-3))
        val force = 1.0e-7
        assert(
            (paths * force / beam.stateAtForce(force).displacement).isCloseTo(54.61, 1e-3)
        )
    }

    // ------------------------------------------------- T-159 — the doubling ladder's repair

    /**
     * `C-0069`'s `Q5`, re-derived through `C-0039`'s own placement solve rather than transcribed —
     * the arm on which `C-0092` measured the doubling ladder losing the branch.
     */
    private val armRoot = Gen1Tile.crossoverHingeStiffness()

    private val armTip = ArmAnchorage.twoTerminus().rotationalStiffness

    private val recommendedArm: TwoSpringElastica by lazy {
        val length = elasticaArmForStiffness(
            hingeStiffness = armRoot,
            hingeCount = 1,
            farStiffness = armTip,
            bendingRigidity = ei,
            count = 34,
            targetStiffness = mandate,
            workingDisplacement = 3.0,
            steps = 400
        )
        TwoSpringElastica(ei, length, armRoot, armTip, steps = 400)
    }

    @Test
    fun `T-159 defect - the branch continues PAST the stroke the doubling ladder loses it at`() {
        // C-0092/CH-0107: C-0039's doubling force ladder refuses at 7.9196867 nm, and the branch
        // it has lost runs to 8.1610821 nm with max|phi| still below a right angle. A repair is a
        // solve at a stroke the ladder cannot reach, on the SAME arm and the same integrator.
        val arm = recommendedArm
        assert(arm.length.isCloseTo(8.16439083, 1e-8))
        val force = arm.forceForDisplacement(7.95)
        assert(force.isFinite() && force > 0.0)
        val state = arm.stateAtDisplacement(7.95)
        assert(state.displacement.isCloseTo(7.95, 1e-9))
        assert(state.maximumRotation < PI / 2.0)
        assert(state.firstIntegralSpread < 1e-9)
    }

    @Test
    fun `T-159 gate 3 conservation - the continued branch keeps the moment balance and the first integral`() {
        val arm = recommendedArm
        var previousStroke = 0.0
        var previousForce = 0.0
        for (stroke in listOf(1.0, 3.0, 5.0, 7.0, 7.5, 7.9, 7.95, 8.0)) {
            val state = arm.stateAtDisplacement(stroke)
            assert(state.displacement.isCloseTo(stroke, 1e-9))
            // the branch is ascending in BOTH coordinates, which is what makes it one branch
            assert(state.displacement > previousStroke)
            assert(state.force > previousForce)
            assert(state.maximumRotation < PI / 2.0)
            assert(state.firstIntegralSpread < 1e-9)
            assert(abs(state.momentBalanceResidual) < 1e-6 * (state.force * arm.length))
            previousStroke = state.displacement
            previousForce = state.force
        }
    }

    @Test
    fun `T-159 gate 1 dimensional - a stroke past the branch is REFUSED, and the refusal is below the contour`() {
        val arm = recommendedArm
        // the contour is a hard bound on every branch (C-0092) and is refused by argument check
        assertFailsWith<IllegalArgumentException> { arm.forceForDisplacement(arm.length) }
        // and a stroke inside the contour but past what the continuation resolves is refused too,
        // rather than answered off another branch
        val refused = assertFailsWith<IllegalArgumentException> {
            arm.forceForDisplacement(arm.length - 1e-9)
        }
        assert(refused.message!!.contains("branch"))
    }

    @Test
    fun `T-159 gate 4 convergence - the repair does not cost more sweeps than the ladder it replaces`() {
        // C-0031's precedent: a defect invisible in the answer is invisible to every check written
        // on the answer, so the count the strategy was chosen for is asserted as well as the root.
        val arm = TwoSpringElastica(ei, 12.5, 16 * hinge, anchorage, steps = 200)
        arm.resetSweepCount()
        val force = arm.forceForDisplacement(3.0)
        val sweeps = arm.sweepCount
        assert(force > 0.0)
        // the doubling ladder took 209 sweeps for this call, measured on this arm before the
        // repair; a continuation that needed materially more would be paying for its safety out
        // of the study budget, and a continuation that collapsed to bisection would need many
        // more while returning the same root
        assert(sweeps in 1..209)
    }

    @Test
    fun `T-159 gate 5 upstream - the retained ladder still measures the artefact C-0092 reported`() {
        // A repair that makes the defect it repairs unmeasurable replaces one unfalsifiable
        // number with another, so C-0039's blind doubling ladder is retained OPT-IN and
        // C-0092's measurement of it stays a measurement.
        val repaired = recommendedArm
        val ladder = TwoSpringElastica(
            ei, repaired.length, armRoot, armTip, 400, BranchStrategy.DOUBLING_LADDER
        )
        // where the far-end residual has one root the two strategies are the same solve
        assert(
            ladder.forceForDisplacement(3.0)
                .isCloseTo(repaired.forceForDisplacement(3.0), 1e-11)
        )
        assert(ladder.forceForDisplacement(7.0).isCloseTo(repaired.forceForDisplacement(7.0), 1e-9))
        // and they part company exactly where C-0092 said: the ladder loses the branch, the
        // continuation keeps it
        assertFailsWith<IllegalArgumentException> { ladder.forceForDisplacement(7.95) }
        assert(repaired.forceForDisplacement(7.95) > 0.0)
    }

    @Test
    fun `gate 5 upstream - the elastica's own trigonometry is the one CH-0040 wrote down`() {
        // delta = r sin theta and the restoring lever is r cos theta: recovered, not assumed,
        // in the rigid-arm limit of the elastica at a free far end.
        val beam = TwoSpringElastica(1.0e9, 4.11, hinge, 0.0, steps = 400)
        val force = beam.forceForDisplacement(3.0)
        val state = beam.stateAtForce(force)
        val theta = state.nearRotation
        assert((4.11 * sin(theta)).isCloseTo(3.0, 1e-6))
        assert((hinge * theta).isCloseTo(force * 4.11 * cos(theta), 1e-5))
    }
}
