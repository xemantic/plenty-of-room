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
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-65` — the standoff's **2 × 2** tip flexibility, solved into `C-0025`'s beam instead of
 * split into two independent springs.
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 * The spine of the task is that `C-0025`'s `k_θ` and `C-0028`'s `k_sway` are the two DIAGONAL
 * entries of one flexibility matrix read with the other load zero, that the off-diagonal is not a
 * correction to the term they kept but larger than it, and that its **sign** is set by which body
 * carries the standoffs.
 */
class CoupledStandoffJointTest {

    private val ei = Gen1Tile.DUPLEX_BENDING_RIGIDITY

    private val stretch = Gen1Tile.DUPLEX_STRETCH_MODULUS

    /** `C-0028`'s recommended base: two crossovers laid ACROSS the flexure, 261.17 pN·nm/rad. */
    private val favourableBase = StandoffBase.crossovers(2, favourableOrientation = true)

    private val singleCrossoverBase = StandoffBase.crossovers(1)

    // ---------------------------------------------------------------- gate 1 — dimensional

    @Test
    fun `gate 1 dimensional consistency - the three flexibility entries carry three different powers of the length`() {
        // at a clamped base C11 = l^3/3EI, C12 = l^2/2EI, C22 = l/EI: doubling the length
        // multiplies them by exactly 8, 4 and 2
        val short = standoffTipFlexibility(ei, 4.0, Double.POSITIVE_INFINITY)
        val long = standoffTipFlexibility(ei, 8.0, Double.POSITIVE_INFINITY)
        assert((long.translationUnderForce / short.translationUnderForce).isCloseTo(8.0))
        assert((long.translationUnderMoment / short.translationUnderMoment).isCloseTo(4.0))
        assert((long.rotationUnderMoment / short.rotationUnderMoment).isCloseTo(2.0))
        assert(long.translationUnderForce.isCloseTo(8.0 * 8.0 * 8.0 / (3.0 * ei)))
        assert(long.translationUnderMoment.isCloseTo(8.0 * 8.0 / (2.0 * ei)))
        assert(long.rotationUnderMoment.isCloseTo(8.0 / ei))
    }

    @Test
    fun `gate 1 dimensional consistency - the correlation is dimensionless and the same at every length`() {
        val lengths = listOf(2.0, 5.0, 8.0, 13.0)
        lengths.forEach {
            assert(standoffTipFlexibility(ei, it, Double.POSITIVE_INFINITY)
                .correlation.isCloseTo(sqrt(3.0) / 2.0))
        }
        // and it does not move when the rigidity does, at a clamped base
        assert(standoffTipFlexibility(17.0, 8.0, Double.POSITIVE_INFINITY)
            .correlation.isCloseTo(sqrt(3.0) / 2.0))
    }

    @Test
    fun `gate 1 dimensional consistency - the coupling factor Phi is dimensionless and is a length ratio`() {
        val flexure = designFlexure(8.0, 31.0)
        // Phi = 24 EI C12/(L^2 A) — a supplied draw-in per unit midspan deflection, so pure number
        val a = 1.0 + 8.0 * ei * flexure.flexibility.rotationUnderMoment / flexure.span
        assert(
            flexure.couplingFactor.isCloseTo(
                24.0 * ei * flexure.flexibility.translationUnderMoment / (flexure.span * flexure.span * a)
            )
        )
        // and it is strictly positive for any real standoff: the head's tilt always draws it in
        assert(flexure.couplingFactor > 0.0)
    }

    @Test
    fun `gate 1 dimensional consistency - the braced buckling load is EI over a squared span`() {
        val short = bracedColumnBucklingLoad(ei, 15.0, 3.5)
        val long = bracedColumnBucklingLoad(ei, 30.0, 3.5)
        assert((short / long).isCloseTo(4.0))
    }

    @Test
    fun `gate 1 dimensional consistency - the favourable mounting's clearance is a length and never negative`() {
        // the midspan sags toward the body the bases stand on, so the standoff length IS the
        // clearance, less one measured interhelical distance
        assert(favourableStrokeClearance(8.0).isCloseTo(8.0 - Gen1Tile.INTERHELICAL_SHEET))
        assert(favourableStrokeClearance(10.0).isCloseTo(10.0 - Gen1Tile.INTERHELICAL_SHEET))
        // it cannot go negative, and a standoff shorter than a contact distance has none at all
        assert(favourableStrokeClearance(1.0) == 0.0)
        // §3's acceptable 3 nm fits inside C-0017's envelope and its desired 10 nm does not
        assert(favourableStrokeClearance(10.0) >= 3.0)
        assert(favourableStrokeClearance(10.0) < 10.0)
    }

    @Test
    fun `gate 1 dimensional consistency - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { standoffTipFlexibility(ei, -1.0, 100.0) }
        assertFailsWith<IllegalArgumentException> { standoffTipFlexibility(-1.0, 8.0, 100.0) }
        assertFailsWith<IllegalArgumentException> { standoffTipFlexibility(ei, 8.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { standoffTipFlexibilityByIntegration(ei, 8.0, 100.0, 7) }
        assertFailsWith<IllegalArgumentException> { bracedColumnWavenumber(-1.0) }
        assertFailsWith<IllegalArgumentException> { favourableStrokeClearance(0.0) }
    }

    // ---------------------------------------------------------------- gate 2 — limiting cases

    @Test
    fun `gate 2 limiting cases - a clamped base gives the textbook cantilever flexibility exactly`() {
        val clamped = standoffTipFlexibility(ei, 8.0, Double.POSITIVE_INFINITY)
        assert(clamped.translationUnderForce.isCloseTo(8.0 * 8.0 * 8.0 / (3.0 * ei)))
        assert(clamped.rotationUnderMoment.isCloseTo(8.0 / ei))
        // and its two "other load zero" diagonal readings ARE C-0025's two standoff constants
        assert(clamped.swayStiffness.isCloseTo(3.0 * ei / (8.0 * 8.0 * 8.0)))
        assert(clamped.headRotationalStiffness.isCloseTo(ei / 8.0))
    }

    @Test
    fun `gate 2 limiting cases - the diagonal readings reproduce C-0028's two series reductions`() {
        listOf(3.0, 8.0, 10.0).forEach { length ->
            val restraint = baseRestraintParameter(favourableBase.rotationalStiffness, ei, length)
            val flexibility =
                standoffTipFlexibility(ei, length, favourableBase.rotationalStiffness)
            assert(
                flexibility.headRotationalStiffness.isCloseTo(
                    standoffHeadRotationalStiffness(ei, length, restraint)
                )
            )
            assert(
                flexibility.swayStiffness.isCloseTo(
                    standoffSwayStiffness(ei, length, restraint)
                )
            )
        }
    }

    @Test
    fun `gate 2 limiting cases - the decoupled flexure reproduces C-0025's PartiallyRestrainedFlexure identically`() {
        listOf(3.0, 8.0, 10.0).forEach { length ->
            val base = favourableBase
            val joint = basedNormalStandoff(length, base)
            val flexibility = standoffTipFlexibility(ei, length, base.rotationalStiffness).decoupled()
            listOf(25.0, 31.0, 44.0).forEach { span ->
                val filed = PartiallyRestrainedFlexure(ei, span, joint, stretch)
                val here = CoupledJointFlexure(ei, span, flexibility, stretch)
                assert(here.bendingFactor.isCloseTo(filed.midspanFactor))
                assert(here.effectiveStretchModulus.isCloseTo(filed.effectiveStretchModulus))
                listOf(0.5, 3.0, 7.0, 10.0).forEach { d ->
                    assert(here.reaction(d).isCloseTo(filed.reaction(d)))
                    assert(here.tangentStiffness(d).isCloseTo(filed.tangentStiffness(d)))
                    assert(here.axialForce(d).isCloseTo(filed.axialTension(d)))
                    assert(here.reaction(-d).isCloseTo(filed.reaction(-d)))
                }
            }
        }
    }

    @Test
    fun `gate 2 limiting cases - the bending factor is C-0025's c of rho at every restraint, coupled or not`() {
        listOf(3.0, 8.0, 10.0).forEach { length ->
            listOf(singleCrossoverBase, favourableBase, StandoffBase.crossovers(3)).forEach { base ->
                val flexibility = standoffTipFlexibility(ei, length, base.rotationalStiffness)
                val coupled = CoupledJointFlexure(ei, 31.0, flexibility, stretch)
                val decoupled = CoupledJointFlexure(ei, 31.0, flexibility.decoupled(), stretch)
                assert(coupled.bendingFactor.isCloseTo(midspanFactor(coupled.restraint)))
                assert(coupled.bendingFactor.isCloseTo(decoupled.bendingFactor))
                assert(coupled.bendingFactor > 48.0 && coupled.bendingFactor < 192.0)
            }
        }
    }

    @Test
    fun `gate 2 limiting cases - the decoupled span root reproduces C-0025's flexureSpanForJoint`() {
        listOf(7.0, 8.0, 9.0).forEach { length ->
            val joint = basedNormalStandoff(length, favourableBase)
            val filed = flexureSpanForJoint(ei, joint, 45, 100.0 / 3.0, 3.0, stretch)
            val here = coupledFlexureSpan(
                ei,
                standoffTipFlexibility(ei, length, favourableBase.rotationalStiffness).decoupled(),
                45, 100.0 / 3.0, 3.0, FlexureOrientation.FAVOURABLE, stretch
            )
            assert(here.isCloseTo(filed))
        }
    }

    @Test
    fun `gate 2 limiting cases - the braced column eigenvalue has both textbook K factors as limits`() {
        // a pinned-pinned braced column is K = 1, u = pi; a clamped-clamped one is K = 0.5, u = 2pi
        assert(bracedColumnWavenumber(0.0).isCloseTo(PI))
        assert(bracedColumnWavenumber(Double.POSITIVE_INFINITY).isCloseTo(2.0 * PI))
        // and it is monotone in the restraint, strictly between them
        val values = listOf(0.5, 1.0, 3.5, 10.0, 100.0).map { bracedColumnWavenumber(it) }
        values.zipWithNext().forEach { (a, b) -> assert(b > a) }
        values.forEach { assert(it > PI && it < 2.0 * PI) }
    }

    @Test
    fun `gate 2 limiting cases - a rigid base leaves the correlation at root three over two and the factor at four`() {
        val clamped = standoffTipFlexibility(ei, 8.0, Double.POSITIVE_INFINITY)
        assert(clamped.correlation.isCloseTo(sqrt(3.0) / 2.0))
        assert(clamped.otherDisplacementFixedFactor.isCloseTo(4.0))
        assert(clamped.swayStiffnessRotationFixed.isCloseTo(4.0 * clamped.swayStiffness))
        assert(
            clamped.headRotationalStiffnessTranslationFixed
                .isCloseTo(4.0 * clamped.headRotationalStiffness)
        )
    }

    // ---------------------------------------------------------------- gate 3 — symmetry

    @Test
    fun `gate 3 symmetry - Maxwell-Betti holds between two independently integrated off-diagonals`() {
        // C12 is a DOUBLE integral of the deflection under a unit tip MOMENT; C21 is a SINGLE
        // integral of the curvature under a unit tip FORCE. Nothing in either construction
        // imposes that they agree — that they do is the reciprocal theorem.
        listOf(3.0, 8.0, 13.0).forEach { length ->
            listOf(13.53, 261.17, 1.0e9).forEach { base ->
                val integrated = standoffTipFlexibilityByIntegration(ei, length, base, 1024)
                assert(
                    abs(integrated.translationUnderMoment - integrated.rotationUnderForce) <=
                            1.0e-12 * integrated.translationUnderMoment
                )
            }
        }
    }

    @Test
    fun `gate 3 symmetry - the integrated flexibility reproduces the closed form at every entry`() {
        listOf(3.0, 8.0, 13.0).forEach { length ->
            listOf(13.53, 261.17).forEach { base ->
                val exact = standoffTipFlexibility(ei, length, base)
                val integrated = standoffTipFlexibilityByIntegration(ei, length, base, 1024)
                assert(integrated.translationUnderForce.isCloseTo(exact.translationUnderForce))
                assert(integrated.translationUnderMoment.isCloseTo(exact.translationUnderMoment))
                assert(integrated.rotationUnderForce.isCloseTo(exact.translationUnderMoment))
                assert(integrated.rotationUnderMoment.isCloseTo(exact.rotationUnderMoment))
            }
        }
    }

    @Test
    fun `gate 3 symmetry - the flexibility matrix is positive definite and the correlation is strictly below one`() {
        listOf(3.0, 8.0, 13.0).forEach { length ->
            listOf(1.0, 13.53, 261.17, Double.POSITIVE_INFINITY).forEach { base ->
                val flexibility = standoffTipFlexibility(ei, length, base)
                assert(flexibility.determinant > 0.0)
                assert(flexibility.correlation > 0.0 && flexibility.correlation < 1.0)
                assert(flexibility.otherDisplacementFixedFactor > 1.0)
            }
        }
    }

    @Test
    fun `gate 3 conservation - the axial compatibility closes from both sides`() {
        // the head's inward translation computed from the JOINT, C11 T + C12 M, must equal what
        // the beam's own geometry demands minus the beam's own stretch. The two are different
        // expressions and neither was used to derive the other.
        val flexure = designFlexure(8.0, 31.82)
        listOf(-7.0, -3.0, 1.0, 3.0, 7.0, 10.0).forEach { d ->
            val fromJoint = flexure.headDrawIn(d)
            val fromBeam = flexure.chordExtension(d) -
                    flexure.axialForce(d) * (flexure.span / 2.0) / stretch
            assert(abs(fromJoint - fromBeam) <= 1.0e-9 * (1.0 + abs(fromBeam)))
        }
    }

    @Test
    fun `gate 3 conservation - the reaction is exactly the sum of its bending and membrane parts`() {
        val flexure = designFlexure(8.0, 31.82)
        listOf(-7.0, -1.0, 3.0, 10.0).forEach { d ->
            assert(
                flexure.reaction(d).isCloseTo(flexure.bendingReaction(d) + flexure.membraneForce(d))
            )
        }
    }

    @Test
    fun `gate 3 symmetry - the coupled law is still SIGNED but it is no longer ODD`() {
        val flexure = designFlexure(8.0, 31.82)
        listOf(1.0, 3.0, 10.0).forEach { s ->
            // still two-sided: it pushes back when the tile rises (C-0023's own test)
            assert(carriesCompression(flexure, s))
            assert(flexure.reaction(s) > 0.0 && flexure.reaction(-s) < 0.0)
            // but the two limbs are NOT mirror images, and the adverse one is the stiffer
            assert(abs(flexure.reaction(-s)) > abs(flexure.reaction(s)))
        }
        // the decoupled reading, by contrast, is exactly odd
        val decoupled = CoupledJointFlexure(
            ei, 31.82,
            standoffTipFlexibility(ei, 8.0, favourableBase.rotationalStiffness).decoupled(),
            stretch
        )
        listOf(1.0, 3.0, 10.0).forEach { s ->
            assert(decoupled.reaction(-s).isCloseTo(-decoupled.reaction(s)))
        }
    }

    @Test
    fun `gate 3 symmetry - the standoff's sway and the flexure's draw-in are still one coordinate`() {
        // C-0028's identity, re-asserted through the 2x2: the head's translation under the beam's
        // inward pull alone is exactly the sway compliance, i.e. the reciprocal of C-0028's spring
        val length = 8.0
        val restraint = baseRestraintParameter(favourableBase.rotationalStiffness, ei, length)
        val flexibility = standoffTipFlexibility(ei, length, favourableBase.rotationalStiffness)
        assert(
            (1.0 / flexibility.translationUnderForce)
                .isCloseTo(standoffSwayStiffness(ei, length, restraint))
        )
    }

    // ---------------------------------------------------------------- gate 4 — convergence

    @Test
    fun `gate 4 convergence - the integrated flexibility converges on the closed form as the mesh refines`() {
        val exact = standoffTipFlexibility(ei, 8.0, 261.17)
        val departures = listOf(64, 128, 256, 512).map { steps ->
            val integrated = standoffTipFlexibilityByIntegration(ei, 8.0, 261.17, steps)
            abs(integrated.translationUnderForce - exact.translationUnderForce) /
                    exact.translationUnderForce
        }
        departures.forEach { assert(it < 1.0e-10) }
        departures.zipWithNext().forEach { (coarse, fine) -> assert(fine <= coarse + 1.0e-15) }
    }

    @Test
    fun `gate 4 convergence - the span root is exactly scan-step independent`() {
        val flexibility = standoffTipFlexibility(ei, 8.0, favourableBase.rotationalStiffness)
        val spans = listOf(64, 128, 256, 512, 2048).map {
            coupledFlexureSpan(
                ei, flexibility, 45, 100.0 / 3.0, 3.0,
                FlexureOrientation.FAVOURABLE, stretch, DrawInModel.CHORD, it
            )
        }
        spans.zipWithNext().forEach { (a, b) -> assert(abs(a - b) <= 1.0e-12 * a) }
    }

    @Test
    fun `gate 4 convergence - the analytic tangent matches a central difference and converges with the step`() {
        val flexure = designFlexure(8.0, 31.82)
        listOf(-6.0, 1.0, 3.0, 5.0, 10.0).forEach { d ->
            val departures = listOf(1.0e-3, 1.0e-4, 1.0e-5).map { h ->
                val numeric = (flexure.reaction(d + h) - flexure.reaction(d - h)) / (2.0 * h)
                abs(numeric - flexure.tangentStiffness(d))
            }
            departures.forEach { assert(it < 1.0e-6) }
            assert(departures.last() <= departures.first() + 1.0e-12)
        }
    }

    @Test
    fun `gate 4 convergence - the braced eigenvalue satisfies its own determinant and is scan-independent`() {
        listOf(0.5, 3.5, 25.0).forEach { restraint ->
            val u = bracedColumnWavenumber(restraint)
            assert(abs(bracedColumnDeterminant(u, restraint)) < 1.0e-9)
            listOf(64, 256, 1024).forEach {
                assert(abs(bracedColumnWavenumber(restraint, it) - u) <= 1.0e-12 * u)
            }
        }
    }

    @Test
    fun `gate 4 convergence - the placed span returns its own target secant`() {
        listOf(FlexureOrientation.FAVOURABLE, FlexureOrientation.ADVERSE).forEach { orientation ->
            val flexibility = standoffTipFlexibility(ei, 8.0, favourableBase.rotationalStiffness)
            val span = coupledFlexureSpan(
                ei, flexibility, 45, 100.0 / 3.0, 3.0, orientation, stretch
            )
            val flexure = CoupledJointFlexure(ei, span, flexibility, stretch)
            assert(
                (45.0 * flexure.strokeSecantStiffness(3.0, orientation))
                    .isCloseTo(100.0 / 3.0)
            )
        }
    }

    // ---------------------------------------------------------------- gate 5 — cross-check

    @Test
    fun `gate 5 cross-check - C-0028's B2 design at 8 nm is reproduced in the decoupled limit`() {
        val flexibility =
            standoffTipFlexibility(ei, 8.0, favourableBase.rotationalStiffness).decoupled()
        val span = coupledFlexureSpan(
            ei, flexibility, 45, 100.0 / 3.0, 3.0, FlexureOrientation.FAVOURABLE, stretch
        )
        val flexure = CoupledJointFlexure(ei, span, flexibility, stretch)
        // C-0028's filed row: span 31.06 nm, c = 91.8, tangent 36.51 pN/nm, T(10) = 2.94 pN,
        // duty(10) = 5.113 pN
        assert(abs(span - 31.06) < 5.0e-3)
        assert(abs(flexure.bendingFactor - 91.81) < 5.0e-2)
        assert(abs(45.0 * flexure.tangentStiffness(3.0) - 36.51) < 5.0e-3)
        assert(abs(flexure.axialForce(10.0) - 2.937) < 5.0e-3)
        assert(abs(flexure.endShear(10.0) - 5.113) < 5.0e-3)
    }

    @Test
    fun `gate 5 cross-check - C-0025's J5-8 design is reproduced in the decoupled clamped-base limit`() {
        val flexibility = standoffTipFlexibility(ei, 8.0, Double.POSITIVE_INFINITY).decoupled()
        val span = coupledFlexureSpan(
            ei, flexibility, 45, 100.0 / 3.0, 3.0, FlexureOrientation.FAVOURABLE, stretch
        )
        val flexure = CoupledJointFlexure(ei, span, flexibility, stretch)
        // C-0025's J5-8: 31.6403748 nm, c = 95.639, tangent 37.3911 pN/nm, T(10) = 3.82799
        assert(abs(span - 31.6403748) < 1.0e-6)
        assert(abs(flexure.bendingFactor - 95.6390226) < 1.0e-5)
        assert(abs(45.0 * flexure.tangentStiffness(3.0) - 37.3911226) < 1.0e-5)
        assert(abs(flexure.axialForce(10.0) - 3.82799407) < 1.0e-6)
    }

    @Test
    fun `gate 5 cross-check - C-0028's own off-diagonal bounds are reproduced by the new object`() {
        // C-0028 reports the correlation as root-three-over-two at a clamped base and 0.947 at a
        // crossover base, with the other-displacement-fixed factor exactly 4 and 9.70
        val clamped = standoffTipFlexibility(ei, 8.0, Double.POSITIVE_INFINITY)
        assert(abs(clamped.correlation - sqrt(3.0) / 2.0) < 1.0e-15)
        assert(abs(clamped.otherDisplacementFixedFactor - 4.0) < 1.0e-12)
        val crossover =
            standoffTipFlexibility(ei, 8.0, singleCrossoverBase.rotationalStiffness)
        assert(abs(crossover.correlation - 0.947) < 5.0e-4)
        assert(abs(crossover.otherDisplacementFixedFactor - 9.70) < 1.0e-2)
        // and they agree with C-0028's own two functions, which are a different construction
        assert(
            crossover.correlation.isCloseTo(
                offDiagonalCorrelation(ei, 8.0, singleCrossoverBase.rotationalStiffness)
            )
        )
        assert(
            crossover.otherDisplacementFixedFactor.isCloseTo(
                offDiagonalFactor(ei, 8.0, singleCrossoverBase.rotationalStiffness)
            )
        )
    }

    @Test
    fun `gate 5 cross-check - the cheap bound stands, the omitted supply exceeds the term that was kept`() {
        // the task's own cheap bound: the head's tilt supplies Phi*delta of draw-in per end while
        // the chord geometry demands only e(delta) — and at C-0028's design point the supply wins
        val flexure = designFlexure(8.0, 31.06)
        val supply = flexure.couplingFactor * 3.0
        val demand = flexure.chordExtension(3.0)
        assert(supply > 2.0 * demand)
        // which is why the coupled beam is in COMPRESSION at the placement point
        assert(flexure.axialForce(3.0) < 0.0)
    }

    @Test
    fun `gate 5 cross-check - the coupled flexure's own compression stays below its braced Euler load`() {
        val length = 8.0
        val flexibility = standoffTipFlexibility(ei, length, favourableBase.rotationalStiffness)
        val span = coupledFlexureSpan(
            ei, flexibility, 45, 100.0 / 3.0, 3.0, FlexureOrientation.FAVOURABLE, stretch
        )
        val flexure = CoupledJointFlexure(ei, span, flexibility, stretch)
        val critical = bracedColumnBucklingLoad(ei, span, flexure.restraint)
        val peak = (1..100).maxOf { -flexure.axialForce(it * 0.1) }
        assert(peak > 0.0)
        assert(critical > 2.0 * peak)
    }

    // ---------------------------------------------------------------- helpers

    private fun designFlexure(length: Double, span: Double) = CoupledJointFlexure(
        ei, span, standoffTipFlexibility(ei, length, favourableBase.rotationalStiffness), stretch
    )
}
