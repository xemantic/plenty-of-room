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
import com.xemantic.nano.plentyofroom.equipartitionRms
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-12` / leaf `A1.1` — the requirement, the anisotropy theorem, and the cost.
 *
 * Gate 3 here is deliberately not a restatement of equipartition, which is the construction
 * of the requirement itself. It checks two independent things: the exact `r²` cancellation
 * between the yaw and translation requirements, which is an algebraic claim about the
 * *placement* of anchors and not about their stiffness; and the anisotropy theorem, which is
 * a convexity statement checked against the freely-jointed chain it is applied to.
 */
class LateralConfinementBudgetTest {

    // ---------------------------------------------------------------- gate 1

    @Test
    fun `gate 1 dimensional consistency - the requirement should round-trip through equipartition`() {
        val required = LateralRequirement.translationalStiffness(3.0)
        assert(equipartitionRms(required).isCloseTo(3.0))
        assert(required.isCloseTo(thermalEnergy() / 9.0))
    }

    @Test
    fun `gate 1 dimensional consistency - a yaw stiffness requirement should be an energy over a squared angle`() {
        val radius = LateralRequirement.cornerRadius(40.0, 40.0)
        val required = LateralRequirement.yawStiffness(3.0, radius)
        val angle = 3.0 / radius
        assert(required.isCloseTo(thermalEnergy() / (angle * angle)))
        // and in pN.nm/rad it is a translational stiffness times a squared radius
        assert(required.isCloseTo(LateralRequirement.translationalStiffness(3.0) * radius * radius))
    }

    // ---------------------------------------------------------------- gate 2

    @Test
    fun `gate 2 limiting cases - leaf A1_1's bound table should be reproduced from k_BT alone`() {
        // "sigma=3 nm -> k>=~0.46 pN/nm; sigma=0.1 nm (prize) -> k>=~414 pN/nm;
        //  sigma=0.03 nm -> k>=~4.6 N/m" — the NDI task map's own table, re-derived not accepted
        assert(LateralRequirement.translationalStiffness(3.0).isCloseTo(0.46, 6e-3))
        assert(LateralRequirement.translationalStiffness(0.1).isCloseTo(414.0, 1e-3))
        assert(LateralRequirement.translationalStiffness(0.03).isCloseTo(4600.0, 1e-3))
    }

    @Test
    fun `gate 2 limiting cases - the three readings of the bound should stand in exact ratios`() {
        val perCoordinate = LateralRequirement.translationalStiffness(3.0)
        val radial = LateralRequirement.radialStiffness(3.0)
        val worstPoint = LateralRequirement.worstPointTranslationalStiffness(3.0)
        assert((radial / perCoordinate).isCloseTo(2.0))
        assert((worstPoint / perCoordinate).isCloseTo(3.0))
    }

    @Test
    fun `gate 2 limiting cases - an anchor that adds nothing should cost no stroke`() {
        assert(strokeRetainedFraction(0.0, 20.0).isCloseTo(1.0))
        assert(strokeRetainedFraction(20.0, 20.0).isCloseTo(0.5))
        assert(strokeRetainedFraction(180.0, 20.0).isCloseTo(0.1))
    }

    // ---------------------------------------------------------------- gate 3

    @Test
    fun `gate 3 symmetry - anchors on the budget radius should make yaw and translation the same condition`() {
        // If the anchors sit at radius r_a and the yaw budget is written at radius r_b, the yaw
        // requirement is the translation requirement scaled by (r_b/r_a)^2 — so for r_a = r_b
        // the two are IDENTICALLY the same condition, whatever the radius is. Asserted over a
        // range of radii, because the claim is that the radius cancels exactly.
        listOf(5.0, 20.0, 28.284271247461902, 100.0).forEach { radius ->
            val links = radialInPlaneLinks(
                axialStiffness = 1.0, transverseStiffness = 1.0, radius = radius
            )
            val assembly = AnchorAssembly(links)
            val translationMargin =
                assembly.lateralStiffnessX / LateralRequirement.translationalStiffness(3.0)
            val yawMargin = assembly.yawStiffness / LateralRequirement.yawStiffness(3.0, radius)
            assert(translationMargin.isCloseTo(yawMargin))
        }
    }

    @Test
    fun `gate 3 symmetry - anchors inside the budget radius should make yaw the binding condition`() {
        // four anchors at the edge midpoints, budget written at the corner: the yaw margin must
        // fall short of the translation margin by exactly (r_corner/r_edge)^2 = 2
        val corner = LateralRequirement.cornerRadius(40.0, 40.0)
        val assembly = AnchorAssembly(
            radialInPlaneLinks(axialStiffness = 1.0, transverseStiffness = 1.0, radius = 20.0)
        )
        val translationMargin =
            assembly.lateralStiffnessX / LateralRequirement.translationalStiffness(3.0)
        val yawMargin = assembly.yawStiffness / LateralRequirement.yawStiffness(3.0, corner)
        assert((translationMargin / yawMargin).isCloseTo(2.0))
    }

    @Test
    fun `gate 3 the anisotropy theorem - a through-layer chain should never be stiffer across than along`() {
        // f(0) = 0 and f convex give f(h) <= h f'(h), so secant/tangent <= 1 with equality only
        // for a linear spring. This is the cheap bound the whole task turns on, and it is
        // checked against the chain it is applied to rather than asserted.
        listOf(10.0, 40.0, 100.0).forEach { contour ->
            val chain = FreelyJointedChain(contourLength = contour, kuhnLength = 1.5)
            listOf(1e-4, 0.01, 0.5, 2.0, 10.0, 50.0).forEach { force ->
                val ratio = chain.transverseStiffness(force) / chain.tangentStiffness(force)
                assert(ratio <= 1.0 + 1e-12)
                assert(ratio > 0.0)
            }
        }
    }

    @Test
    fun `gate 3 the anisotropy theorem - the equality case should be the linear spring and nothing else`() {
        val chain = FreelyJointedChain(contourLength = 100.0, kuhnLength = 1.5)
        // at vanishing force the chain IS a linear spring, so the ratio reaches one
        assert(
            (chain.transverseStiffness(1e-8) / chain.tangentStiffness(1e-8)).isCloseTo(1.0, 1e-9)
        )
        // and it falls monotonically away from one as the chain strain-stiffens
        val ratios = listOf(0.1, 1.0, 5.0, 20.0).map {
            chain.transverseStiffness(it) / chain.tangentStiffness(it)
        }
        ratios.zipWithNext { first, second -> assert(second < first) }
        assert(ratios.last() < 0.2)
    }

    @Test
    fun `gate 3 the anisotropy theorem - a rigid rod should do far worse than the floor a chain reaches`() {
        // The theorem bounds a FLEXIBLE through-layer link at one-for-one. A rigid rod is not
        // covered by it — its lateral stiffness is bending, not tension — and it does not merely
        // fail to beat the floor, it misses it by two orders of magnitude: 3EI/(S L^2).
        val span = 10.0
        val rodLateral = beamTransverseStiffness(230.0, span, BeamEndCondition.PINNED_HEAD)
        val rodNormal = rodAxialStiffness(1100.0, span)
        val rodRatio = anisotropyRatio(rodLateral, rodNormal)
        assert(rodRatio.isCloseTo(3.0 * 230.0 / (1100.0 * span * span)))
        assert(rodRatio < 0.01)
        // the same span crossed by an entropic tether reaches the floor to within a few per cent
        val chain = FreelyJointedChain(contourLength = 60.0, kuhnLength = 2.1)
        val tension = chain.tension(span)
        val chainRatio = anisotropyRatio(
            chain.transverseStiffness(tension), chain.tangentStiffness(tension)
        )
        assert(chainRatio > 0.9)
        assert(chainRatio / rodRatio > 100.0)
    }

    // ---------------------------------------------------------------- gate 4

    @Test
    fun `gate 4 numerical convergence - the tether design solve should reproduce its own requirement`() {
        // solved for the contour length at which N tethers spanning h deliver exactly k_req
        listOf(4, 8).forEach { count ->
            listOf(5.0, 7.0, 10.0).forEach { span ->
                val contour = entropicTetherContourLength(
                    count = count,
                    span = span,
                    kuhnLength = 1.5,
                    requiredStiffness = 0.460216333
                )
                val chain = FreelyJointedChain(contourLength = contour, kuhnLength = 1.5)
                val force = chain.tension(span)
                assert((count * chain.transverseStiffness(force)).isCloseTo(0.460216333, 1e-7))
            }
        }
    }

    @Test
    fun `gate 4 numerical convergence - the tether solve should approach its Gaussian closed form for a slack chain`() {
        // for h^2 << L_c b the chain is Gaussian and the answer is L_c = 3 N k_BT/(k_req b)
        val span = 0.05
        val contour = entropicTetherContourLength(
            count = 4, span = span, kuhnLength = 1.5, requiredStiffness = 0.460216333
        )
        val gaussian = 3.0 * 4 * thermalEnergy() / (0.460216333 * 1.5)
        assert(contour.isCloseTo(gaussian, 1e-3))
    }

    // ---------------------------------------------------------------- gate 5

    @Test
    fun `gate 5 literature cross-check - the C-0010 requirement should be reproduced to its last digit`() {
        assert(LateralRequirement.translationalStiffness(3.0).isCloseTo(0.460216333, 1e-8))
    }

    @Test
    fun `gate 5 literature cross-check - the C-0009 concentration factor should be applied not the equal share`() {
        // C-0009: a rigid anchor is carried by its two nearest crossovers, so the equal-sharing
        // figure understates the peak by 2.3-7.6x. The worst factor is used here.
        assert(peakPathForce(1.0, PerPathAllowables.CONCENTRATION_FACTOR_MAX).isCloseTo(7.6))
        assert(PerPathAllowables.UNZIP < PerPathAllowables.SHEAR)
        assert(PerPathAllowables.SHEAR < PerPathAllowables.OVERSTRETCHING_CEILING)
    }

    // ---------------------------------------------------------------- guards

    @Test
    fun `a zero positional bound should be rejected rather than returning an infinity`() {
        assertFailsWith<IllegalArgumentException> {
            LateralRequirement.translationalStiffness(0.0)
        }
        assertFailsWith<IllegalArgumentException> { strokeRetainedFraction(-1.0, 20.0) }
    }

    @Test
    fun `the corner radius should be the half diagonal and the footprint radius the RMS one`() {
        assert(LateralRequirement.cornerRadius(40.0, 40.0).isCloseTo(20.0 * sqrt(2.0)))
        assert(LateralRequirement.footprintRadius(40.0, 40.0).isCloseTo(sqrt(1600.0 / 6.0)))
    }
}
