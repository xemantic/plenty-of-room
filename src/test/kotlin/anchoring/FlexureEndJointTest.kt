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
import com.xemantic.nano.plentyofroom.structure.ShearJointAllowable
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-30` — the origami joint at a transverse flexure's end: does it draw in, and does it clamp?
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 * The spine of the task is that `C-0023`'s two brackets — pinned/clamped and free/held — are the
 * **two limits of one two-parameter joint**, so the limit tests below are not decoration: they are
 * what makes every intermediate number comparable with the filed claim.
 */
class FlexureEndJointTest {

    // ---------------------------------------------------------------- gate 1 — dimensional

    @Test
    fun `gate 1 dimensional consistency - the end restraint parameter is dimensionless`() {
        // rho = k_theta L / EI: (pN*nm/rad)(nm)/(pN*nm^2) = 1
        assert(endRestraintParameter(13.53, 230.0, 25.0).isCloseTo(13.53 * 25.0 / 230.0))
        // and it is linear in the span, which is what makes c depend on the span too
        assert(
            (endRestraintParameter(13.53, 230.0, 50.0) /
                    endRestraintParameter(13.53, 230.0, 25.0)).isCloseTo(2.0)
        )
    }

    @Test
    fun `gate 1 dimensional consistency - an effective stretch modulus is a force`() {
        // S_eff = S/(1 + 2S/(k_a L)); doubling both k_a and 1/L leaves it unchanged
        assert(
            effectiveStretchModulus(1100.0, 64.7, 30.0)
                .isCloseTo(1100.0 / (1.0 + 2.0 * 1100.0 / (64.7 * 30.0)))
        )
        assert(
            effectiveStretchModulus(1100.0, 129.4, 15.0)
                .isCloseTo(effectiveStretchModulus(1100.0, 64.7, 30.0))
        )
    }

    @Test
    fun `gate 1 dimensional consistency - a partially restrained flexure stiffness is EI over a cubed length`() {
        // at a FIXED c (clamped, rho infinite) doubling the span divides the stiffness by 8
        val short = PartiallyRestrainedFlexure(230.0, 20.0, FlexureEndJoint.clamped())
        val long = PartiallyRestrainedFlexure(230.0, 40.0, FlexureEndJoint.clamped())
        assert((short.bendingStiffness / long.bendingStiffness).isCloseTo(8.0))
        assert(short.bendingStiffness.isCloseTo(192.0 * 230.0 / (20.0 * 20.0 * 20.0)))
    }

    @Test
    fun `gate 1 dimensional consistency - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { endRestraintParameter(13.53, 230.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { effectiveStretchModulus(1100.0, -1.0, 30.0) }
        assertFailsWith<IllegalArgumentException> { midspanFactor(-0.1) }
        assertFailsWith<IllegalArgumentException> {
            PartiallyRestrainedFlexure(230.0, -1.0, FlexureEndJoint.pinnedAndFree())
        }
        assertFailsWith<IllegalArgumentException> { drawInFactor(-1.0) }
    }

    // ---------------------------------------------------------------- gate 2 — limiting cases

    @Test
    fun `gate 2 limiting cases - the end condition factor is exactly 48 at zero restraint and 192 at infinite`() {
        assert(midspanFactor(0.0).isCloseTo(48.0))
        assert(midspanFactor(Double.POSITIVE_INFINITY).isCloseTo(192.0))
        // and it is monotone, so the whole of C-0023's 4x bracket is the interior of this function
        val ladder = listOf(0.0, 0.5, 1.0, 2.0, 4.0, 8.0, 16.0, 64.0, 1024.0)
        ladder.zipWithNext().forEach { (low, high) ->
            assert(midspanFactor(high) > midspanFactor(low))
        }
        // the half-way value is at rho = 4 exactly, which is a property of the algebra
        assert(midspanFactor(4.0).isCloseTo(96.0))
    }

    @Test
    fun `gate 2 limiting cases - the effective stretch modulus recovers both of C-0023's readings`() {
        // k_a -> infinity is "ends held axially"; k_a -> 0 is "ends free to draw in"
        assert(effectiveStretchModulus(1100.0, Double.POSITIVE_INFINITY, 30.0).isCloseTo(1100.0))
        assert(effectiveStretchModulus(1100.0, 0.0, 30.0).isCloseTo(0.0))
        assert(effectiveStretchModulus(1100.0, 1e-9, 30.0) < 1e-4)
    }

    @Test
    fun `gate 2 limiting cases - the partial model reproduces C-0023's pinned free flexure exactly`() {
        listOf(10.0, 25.0, 49.41).forEach { span ->
            val filed = TransverseDuplexFlexure(
                230.0, span, FlexureEndCondition.PINNED_ENDS, axiallyRestrained = false
            )
            val partial = PartiallyRestrainedFlexure(230.0, span, FlexureEndJoint.pinnedAndFree())
            listOf(-2.0, -0.5, 0.5, 3.0).forEach { d ->
                assert(partial.reaction(d).isCloseTo(filed.reaction(d)))
                assert(partial.tangentStiffness(d).isCloseTo(filed.tangentStiffness(d)))
            }
        }
    }

    @Test
    fun `gate 2 limiting cases - the partial model reproduces C-0023's pinned held flexure exactly`() {
        listOf(25.0, 49.41).forEach { span ->
            val filed = TransverseDuplexFlexure(
                230.0, span, FlexureEndCondition.PINNED_ENDS, axiallyRestrained = true
            )
            val partial = PartiallyRestrainedFlexure(230.0, span, FlexureEndJoint.pinnedAndHeld())
            listOf(-2.0, -0.5, 0.5, 3.0).forEach { d ->
                assert(partial.reaction(d).isCloseTo(filed.reaction(d)))
                assert(partial.tangentStiffness(d).isCloseTo(filed.tangentStiffness(d)))
                assert(partial.axialTension(d).isCloseTo(filed.axialTension(d)))
            }
        }
    }

    @Test
    fun `gate 2 limiting cases - the partial model reproduces C-0023's clamped readings exactly`() {
        listOf(false, true).forEach { held ->
            val filed = TransverseDuplexFlexure(
                230.0, 39.07, FlexureEndCondition.CLAMPED_ENDS, axiallyRestrained = held
            )
            val partial = PartiallyRestrainedFlexure(
                230.0, 39.07,
                if (held) FlexureEndJoint.clampedAndHeld() else FlexureEndJoint.clamped()
            )
            listOf(-1.0, 0.5, 3.0).forEach { d ->
                assert(partial.reaction(d).isCloseTo(filed.reaction(d)))
                assert(partial.tangentStiffness(d).isCloseTo(filed.tangentStiffness(d)))
            }
        }
    }

    @Test
    fun `gate 2 limiting cases - a partially restrained flexure is still two-sided and its law still odd`() {
        val joint = FlexureEndJoint.crossover()
        listOf(20.0, 35.0).forEach { span ->
            val flexure = PartiallyRestrainedFlexure(230.0, span, joint)
            assert(carriesCompression(flexure, 0.5))
            listOf(0.1, 1.0, 3.0).forEach { d ->
                assert(flexure.reaction(-d).isCloseTo(-flexure.reaction(d)))
                assert(flexure.tangentStiffness(-d).isCloseTo(flexure.tangentStiffness(d)))
            }
        }
    }

    @Test
    fun `gate 2 limiting cases - the draw-in factor is 2point4 at both ends and exactly 9 over 4 in between`() {
        // C-0023 records 2.4 for BOTH end conditions; that is right at the endpoints
        assert(drawInFactor(0.0).isCloseTo(2.4))
        assert(drawInFactor(Double.POSITIVE_INFINITY).isCloseTo(2.4))
        // ...and wrong in between: the interior minimum is exactly 9/4, at rho = 8 (c = 120)
        assert(drawInFactor(8.0).isCloseTo(2.25))
        assert(midspanFactor(8.0).isCloseTo(120.0))
        // it is a minimum, so nothing on the continuum exceeds 2.4
        listOf(0.25, 0.5, 1.0, 2.0, 4.0, 8.0, 16.0, 64.0, 256.0).forEach { rho ->
            assert(drawInFactor(rho) <= 2.4 + 1e-12)
            assert(drawInFactor(rho) >= 2.25 - 1e-12)
        }
    }

    @Test
    fun `gate 2 limiting cases - the draw-in vanishes as the square of the deflection`() {
        val flexure = PartiallyRestrainedFlexure(230.0, 30.0, FlexureEndJoint.crossover())
        assert(flexure.drawInDemand(0.0).isCloseTo(0.0))
        assert((flexure.drawInDemand(2.0) / flexure.drawInDemand(1.0)).isCloseTo(4.0))
        // and it is even
        assert(flexure.drawInDemand(-3.0).isCloseTo(flexure.drawInDemand(3.0)))
    }

    @Test
    fun `gate 2 limiting cases - the membrane term is cubic and vanishes at zero deflection`() {
        val flexure = PartiallyRestrainedFlexure(230.0, 30.0, FlexureEndJoint.crossover())
        assert(flexure.membraneForce(0.0).isCloseTo(0.0))
        val ratio = flexure.membraneForce(0.02) / flexure.membraneForce(0.01)
        assert(ratio.isCloseTo(8.0, 1e-3))
    }

    // ---------------------------------------------------------------- gate 3 — symmetry, conservation

    @Test
    fun `gate 3 conservation - the analytic tangent matches a central difference of the law`() {
        listOf(
            FlexureEndJoint.crossover(),
            FlexureEndJoint.nickedContinuation(),
            FlexureEndJoint.normalStandoff(10.0)
        ).forEach { joint ->
            val flexure = PartiallyRestrainedFlexure(230.0, 32.0, joint)
            listOf(0.5, 1.5, 3.0).forEach { d ->
                val h = 1e-5
                val numeric = (flexure.reaction(d + h) - flexure.reaction(d - h)) / (2.0 * h)
                assert(flexure.tangentStiffness(d).isCloseTo(numeric, 1e-6))
            }
        }
    }

    @Test
    fun `gate 3 conservation - the draw-in demand is taken up by the beam and the two joints together`() {
        // S_eff is defined by exactly this series statement, so it has to close on it:
        // the beam's own stretch plus twice the joint's extension is the demand the cable
        // geometry itself charges for, 2 d^2/L, to first order in the deflection.
        listOf(FlexureEndJoint.crossover(), FlexureEndJoint.normalStandoff(10.0)).forEach { joint ->
            val span = 32.0
            val flexure = PartiallyRestrainedFlexure(230.0, span, joint)
            listOf(0.5, 1.0).forEach { d ->
                val tension = flexure.axialTension(d)
                val beamStretch = tension * span / AnchorMaterials.DUPLEX_STRETCH_MODULUS
                val jointStretch = 2.0 * tension / joint.axialStiffness
                val cableDemand = d * d / (span / 2.0)
                assert((beamStretch + jointStretch).isCloseTo(cableDemand, 1e-3))
            }
        }
    }

    @Test
    fun `gate 3 conservation - a flexible link's transverse stiffness equals its axial one exactly`() {
        // C-0014's convexity theorem in a new place, and the whole reason J3 fails P1:
        // a tension-only link has no direction of its own, so a joint built from one cannot be
        // stiff across and soft along. Asserted over a range of hinge lengths.
        listOf(2, 5, 10, 20).forEach { nucleotides ->
            val joint = FlexureEndJoint.singleStrandedHinge(nucleotides)
            assert(joint.transverseStiffness.isCloseTo(joint.axialStiffness))
            assert(joint.anisotropy.isCloseTo(1.0))
        }
        // and the escape is bending, which has a direction: a normal standoff's anisotropy is
        // S l^2/(3 EI) and grows as the square of its length
        val shortStandoff = FlexureEndJoint.normalStandoff(5.0)
        val longStandoff = FlexureEndJoint.normalStandoff(10.0)
        assert((longStandoff.anisotropy / shortStandoff.anisotropy).isCloseTo(4.0))
        assert(longStandoff.anisotropy > 100.0)
    }

    @Test
    fun `gate 3 conservation - a covalent joint has no dead band and a slack one has exactly its contour`() {
        listOf(
            FlexureEndJoint.crossover(),
            FlexureEndJoint.nickedContinuation(),
            FlexureEndJoint.multiCrossoverClamp(2),
            FlexureEndJoint.normalStandoff(10.0)
        ).forEach { joint -> assert(joint.transverseDeadBand.isCloseTo(0.0)) }
        assert(
            FlexureEndJoint.singleStrandedHinge(2).transverseDeadBand
                .isCloseTo(2 * SsDnaTether.CONTOUR_PER_NUCLEOTIDE)
        )
    }

    // ---------------------------------------------------------------- gate 4 — convergence

    @Test
    fun `gate 4 convergence - the solved span reproduces its own target secant`() {
        listOf(
            FlexureEndJoint.pinnedAndFree(),
            FlexureEndJoint.crossover(),
            FlexureEndJoint.normalStandoff(10.0),
            FlexureEndJoint.pinnedAndHeld()
        ).forEach { joint ->
            val span = flexureSpanForJoint(230.0, joint, 45, 33.3333333333, 3.0)
            val assembled = 45 * PartiallyRestrainedFlexure(230.0, span, joint)
                .secantStiffness(3.0)
            assert(assembled.isCloseTo(33.3333333333, 1e-8))
        }
    }

    @Test
    fun `gate 4 convergence - the span root is independent of the bracketing scan`() {
        val joint = FlexureEndJoint.crossover()
        val reference = flexureSpanForJoint(230.0, joint, 45, 33.3333333333, 3.0, scanSteps = 4096)
        listOf(32, 128, 512, 2048).forEach { steps ->
            val span = flexureSpanForJoint(230.0, joint, 45, 33.3333333333, 3.0, scanSteps = steps)
            assert(abs(span - reference) / reference < 1e-12)
        }
    }

    // ---------------------------------------------------------------- gate 5 — literature

    @Test
    fun `gate 5 literature - the crossover joint's constants are Gen1Tile's own`() {
        val joint = FlexureEndJoint.crossover()
        assert(joint.rotationalStiffness.isCloseTo(Gen1Tile.crossoverHingeStiffness()))
        assert(joint.axialStiffness.isCloseTo(Gen1Tile.crossoverInPlaneStiffness()))
        assert(joint.rotationalStiffness.isCloseTo(13.5294, 1e-4))
        assert(joint.axialStiffness.isCloseTo(64.7059, 1e-4))
        // Chen et al.'s own experimental bracket is a factor of exactly two
        assert(
            (FlexureEndJoint.crossover(Gen1Tile.CROSSOVER_ALPHA_MAX).rotationalStiffness /
                    FlexureEndJoint.crossover(Gen1Tile.CROSSOVER_ALPHA_MIN).rotationalStiffness)
                .isCloseTo(2.0)
        )
    }

    @Test
    fun `gate 5 literature - the free and held spans reproduce C-0023's own two numbers`() {
        val free = flexureSpanForJoint(230.0, FlexureEndJoint.pinnedAndFree(), 45, 33.3333333333, 3.0)
        val held = flexureSpanForJoint(230.0, FlexureEndJoint.pinnedAndHeld(), 45, 33.3333333333, 3.0)
        assert(free.isCloseTo(24.61, 1e-3))
        assert(held.isCloseTo(49.41, 1e-3))
        // ...and C-0023's two tangents: 33.333 free, 91.13 held
        assert(
            (45 * PartiallyRestrainedFlexure(230.0, free, FlexureEndJoint.pinnedAndFree())
                .tangentStiffness(3.0)).isCloseTo(33.3333, 1e-4)
        )
        assert(
            (45 * PartiallyRestrainedFlexure(230.0, held, FlexureEndJoint.pinnedAndHeld())
                .tangentStiffness(3.0)).isCloseTo(91.13, 2e-3)
        )
        // and C-0023's 0.88 nm = 2.6 bp draw-in demand at the free span
        val demand = PartiallyRestrainedFlexure(230.0, free, FlexureEndJoint.pinnedAndFree())
            .drawInDemand(3.0)
        assert(demand.isCloseTo(0.8776, 1e-3))
        assert((demand / AnchorMaterials.RISE_PER_BASE_PAIR).isCloseTo(2.58, 1e-2))
    }

    @Test
    fun `gate 5 literature - the ssDNA hinge is built on the zero-force end of the Kuhn bracket`() {
        val joint = FlexureEndJoint.singleStrandedHinge(2)
        assert(joint.kuhnLength.isCloseTo(SsDnaTether.KUHN_LENGTH_ZERO_FORCE))
        assert(joint.contourLength.isCloseTo(2 * SsDnaTether.CONTOUR_PER_NUCLEOTIDE))
        // its axial stiffness is the chain's own Gaussian constant, 3 k_BT/(L_c b)
        assert(
            joint.axialStiffness.isCloseTo(
                FreelyJointedChain(
                    2 * SsDnaTether.CONTOUR_PER_NUCLEOTIDE, SsDnaTether.KUHN_LENGTH_ZERO_FORCE
                ).gaussianStiffness
            )
        )
        // ...and its rotational restraint is l_p k_BT/L_c with l_p = b/2, i.e. near-pinned
        assert(endRestraintParameter(joint.rotationalStiffness, 230.0, 28.0) < 0.5)
    }

    @Test
    fun `gate 5 literature - the crossover joint fails the compliance ceiling over the whole k_s sweep`() {
        // k_s is C-0020's DERIVED construction, not a measurement, and it sweeps four decades
        // there. The headline of this task is that no verdict moves across it: the crossover
        // joint is past C-0023's 40 pN/nm ceiling at every multiplier, and at every alpha.
        Gen1Tile.CROSSOVER_IN_PLANE_SWEEP.forEach { multiplier ->
            val joint = FlexureEndJoint.crossover(inPlaneMultiplier = multiplier)
            val span = flexureSpanForJoint(230.0, joint, 45, 33.3333333333, 3.0)
            val tangent = 45 * PartiallyRestrainedFlexure(230.0, span, joint)
                .tangentStiffness(3.0)
            assert(tangent > 40.0)
        }
        listOf(Gen1Tile.CROSSOVER_ALPHA_MIN, 1.0, Gen1Tile.CROSSOVER_ALPHA_MAX).forEach { alpha ->
            val joint = FlexureEndJoint.crossover(alpha)
            val span = flexureSpanForJoint(230.0, joint, 45, 33.3333333333, 3.0)
            assert(45 * PartiallyRestrainedFlexure(230.0, span, joint).tangentStiffness(3.0) > 40.0)
        }
    }

    @Test
    fun `gate 5 literature - a normal standoff clears the ceiling and an isotropic joint cannot`() {
        // the design statement, as an executable one: the anisotropic joint passes where every
        // isotropic one fails, and the anisotropy is the only thing that differs
        listOf(7.0, 8.0, 9.0, 10.0).forEach { length ->
            val joint = FlexureEndJoint.normalStandoff(length)
            val span = flexureSpanForJoint(230.0, joint, 45, 33.3333333333, 3.0)
            val flexure = PartiallyRestrainedFlexure(230.0, span, joint)
            assert(45 * flexure.tangentStiffness(3.0) <= 40.0)
            // ...and it still supports the beam: transverse stiffness far above the beam's own
            assert(joint.transverseStiffness > 10.0 * 33.3333333333 / 45.0)
            assert(joint.transverseDeadBand.isCloseTo(0.0))
            // ...and the beam's own tension stays under the 10 pN unzip allowable at 10 nm
            assert(flexure.axialTension(10.0) < Gen1Tile.DUPLEX_UNZIP_ALLOWABLE)
        }
    }

    @Test
    fun `gate 1 dimensional consistency - the desired stroke puts a floor under the path count`() {
        // a coupling at the mandate delivers 33.3333 x 10 = 333.33 pN at section 3's DESIRED
        // stroke, so the 10 pN unzip allowable needs at least 34 paths — pure arithmetic, no
        // joint, no element, no layer
        val delivered = (100.0 / 3.0) * 10.0
        assert(delivered.isCloseTo(333.3333333, 1e-6))
        val floor = delivered / Gen1Tile.DUPLEX_UNZIP_ALLOWABLE
        assert(floor.isCloseTo(33.33333333, 1e-6))
        assert(45 > floor)
        assert(15 < floor)
        assert(8 < floor)
    }

    @Test
    fun `gate 5 literature - CH-0029's ladder is reproduced and inverted consistently`() {
        val allowable = ShearJointAllowable()
        assert(allowable.ruptureForce(8.0, 100.0).isCloseTo(18.80, 1e-3))
        assert(allowable.ruptureForce(16.0, 100.0).isCloseTo(34.81, 1e-3))
        assert(allowable.ruptureForce(30.0, 100.0).isCloseTo(47.11, 1e-3))
        // the inversion this task needs: the bonded length a given tension demands
        listOf(5.0, 18.8, 34.81, 47.11).forEach { force ->
            val bp = bondedLengthForTension(force, allowable, 100.0)
            assert(allowable.ruptureForce(bp, 100.0).isCloseTo(force, 1e-6))
        }
        // and it is unreachable above the saturation, which is the honest failure
        assertFailsWith<IllegalArgumentException> {
            bondedLengthForTension(allowable.saturationForce * 1.01, allowable, 100.0)
        }
    }
}
