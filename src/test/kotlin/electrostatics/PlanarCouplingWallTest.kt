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

package com.xemantic.nano.plentyofroom.electrostatics

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.E
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.test.Test

/**
 * `T-221` gate tests for the planar wall-wall coupling criterion.
 *
 * The whole task rests on one identity — that `Ξ` and `D̃` are both linear in the wall's charge
 * density, so their ratio is a property of the **gap** and not of the wall — and the tests that
 * matter are the ones that pin that down, pin the closed-form threshold it produces, and pin the
 * two branches of Kanduč et al.'s criterion against each other where they must meet.
 */
class PlanarCouplingWallTest {

    private val bjerrumLength = bjerrumLength(300.0, 78.0)
    private val valency = 2

    /** `C-0005`'s duplex cylinder: `σ = (1/b)/(2πR)` with `b = 0.17 nm`, `R = 1.0 nm`. */
    private val duplexCylinder = 1.0 / 0.17 / (2.0 * PI)

    /** `C-0005`'s saturated far-field tile charge at 2 mM. */
    private val saturated = 0.0568

    // ---------------------------------------------------------------- gate 1, dimensional

    @Test
    fun `gate 1 - the pair Bjerrum length is a length and is q squared times l_B`() {
        assert(counterionPairBjerrumLength(valency, bjerrumLength).isCloseTo(4.0 * bjerrumLength))
        assert(abs(bjerrumLength - 0.7141) < 1e-4)
    }

    @Test
    fun `gate 1 - the coupling over the reduced gap is dimensionless and is a pair Bjerrum length over the gap`() {
        assert(
            couplingOverReducedGap(7.0, valency, bjerrumLength)
                .isCloseTo(4.0 * bjerrumLength / 7.0)
        )
    }

    // ------------------------------------------------- gate 3, the scale-covariance identity

    @Test
    fun `the ratio of the coupling to the reduced gap carries no wall convention at all`() {
        val gap = 7.0
        listOf(duplexCylinder, saturated, 0.01, 100.0).forEach { sigma ->
            val surface = ChargedSurface(sigma, valency)
            val mu = surface.gouyChapmanLength(bjerrumLength)
            val ratio = surface.couplingParameter(bjerrumLength) / (gap / mu)
            assert(abs(ratio - couplingOverReducedGap(gap, valency, bjerrumLength)) < 1e-13)
        }
    }

    // ------------------------------------------------------- the criterion in its log form

    @Test
    fun `the log residual is positive exactly when Kanduc Eq 64 is satisfied`() {
        listOf(duplexCylinder, saturated).forEach { sigma ->
            val surface = ChargedSurface(sigma, valency)
            val mu = surface.gouyChapmanLength(bjerrumLength)
            val coupling = surface.couplingParameter(bjerrumLength)
            val bound = weakCouplingValidityCoupling(7.0 / mu)
            val residual = repulsiveBranchLogResidual(7.0, mu, valency, bjerrumLength)
            assert((residual > 0.0) == (coupling < bound))
        }
    }

    @Test
    fun `the whole wall convention enters the criterion as the logarithm of the charge density`() {
        val gap = 7.0
        val a = repulsiveBranchLogResidual(
            gap, ChargedSurface(duplexCylinder, valency).gouyChapmanLength(bjerrumLength),
            valency, bjerrumLength
        )
        val b = repulsiveBranchLogResidual(
            gap, ChargedSurface(saturated, valency).gouyChapmanLength(bjerrumLength),
            valency, bjerrumLength
        )
        assert((b - a).isCloseTo(ln(duplexCylinder / saturated)))
    }

    // ------------------------------------------------------ gate 3, the closed-form threshold

    @Test
    fun `the closed-form threshold coupling reproduces a bisection on the criterion itself`() {
        listOf(5.0, 7.0, 10.0, 20.0).forEach { gap ->
            val closed = repulsiveBranchThresholdCoupling(gap, valency, bjerrumLength)
            var low = E * 1.0000001
            var high = 1e12
            repeat(300) {
                val middle = sqrt(low * high)
                // the reduced gap at which the bound equals the coupling implied by that same wall
                if (middle / ln(middle) - couplingOverReducedGap(gap, valency, bjerrumLength) * middle < 0.0) {
                    high = middle
                } else low = middle
            }
            val reduced = sqrt(low * high)
            assert((couplingOverReducedGap(gap, valency, bjerrumLength) * reduced).isCloseTo(closed))
        }
    }

    @Test
    fun `the threshold Gouy-Chapman length and charge density are each other's inverse map`() {
        val gap = 7.0
        val mu = repulsiveBranchThresholdGouyChapmanLength(gap, valency, bjerrumLength)
        val sigma = repulsiveBranchThresholdChargeDensity(gap, valency, bjerrumLength)
        assert(
            ChargedSurface(sigma, valency).gouyChapmanLength(bjerrumLength).isCloseTo(mu)
        )
    }

    @Test
    fun `a wall exactly at the threshold charge density sits exactly on the criterion`() {
        val gap = 7.0
        val sigma = repulsiveBranchThresholdChargeDensity(gap, valency, bjerrumLength)
        val surface = ChargedSurface(sigma, valency)
        val reduced = gap / surface.gouyChapmanLength(bjerrumLength)
        assert(
            abs(surface.couplingParameter(bjerrumLength) - weakCouplingValidityCoupling(reduced)) < 1e-9
        )
    }

    // --------------------------------------------------------------- gate 2, limiting cases

    @Test
    fun `the criterion's own bound is minimal at a reduced gap of e and equals e there`() {
        assert(weakCouplingValidityCoupling(E).isCloseTo(E))
        listOf(1.5, 2.0, 2.5, 3.0, 4.0, 10.0).forEach { reduced ->
            assert(weakCouplingValidityCoupling(reduced) > E - 1e-12)
        }
    }

    @Test
    fun `the asymmetry function is continuous across its own branch point`() {
        val branch = -sqrt(0.5)
        val below = asymmetryFunction(branch - 1e-6)
        val above = asymmetryFunction(branch + 1e-6)
        assert(abs(below - above) < 1e-5)
    }

    @Test
    fun `the asymmetry function vanishes as the second wall's charge does`() {
        assert(abs(asymmetryFunction(-1e-4)) < 1e-9)
    }

    @Test
    fun `the mean-field pressure changes sign at a reduced gap of one plus zeta over its magnitude`() {
        assert(meanFieldPressureSignChangeReducedGap(-0.5).isCloseTo(1.0))
        assert(meanFieldPressureSignChangeReducedGap(-0.25).isCloseTo(3.0))
        assert(meanFieldPressureSignChangeReducedGap(-0.125).isCloseTo(7.0))
    }

    @Test
    fun `the sign-change locus solves Kanduc Eq 18 in the vanishing-alpha limit`() {
        // tan(2 alpha a) = alpha (zeta+1) mu / (alpha^2 mu^2 - zeta): as alpha -> 0 both sides are
        // linear in alpha and the equality fixes D/mu. Check the residual vanishes like alpha^3.
        listOf(-0.5, -0.25, -0.1).forEach { zeta ->
            val reduced = meanFieldPressureSignChangeReducedGap(zeta)
            val residuals = listOf(1e-3, 1e-4).map { alpha ->
                abs(kotlin.math.tan(alpha * reduced) - alpha * (zeta + 1.0) / (alpha * alpha - zeta))
            }
            assert(residuals[0] / residuals[1] > 500.0)
        }
    }

    @Test
    fun `the asymmetry admitting attraction tends to zero as the gap opens and to one as it closes`() {
        assert(attractiveBranchAsymmetryCeiling(0.0).isCloseTo(-1.0))
        assert(abs(attractiveBranchAsymmetryCeiling(1e6)) < 1e-5)
        assert(attractiveBranchAsymmetryCeiling(7.0).isCloseTo(-0.125))
    }

    // ------------------------------------------------------------- the attractive branch

    @Test
    fun `Kanduc Eq 65's bound is the exponential of twice the reduced gap times the asymmetry`() {
        val zeta = -0.5
        val reduced = 10.0
        assert(
            attractiveBranchValidityCoupling(zeta, reduced).isCloseTo(
                zeta * zeta / abs(asymmetryFunction(zeta)) * exp(-2.0 * zeta * reduced)
            )
        )
    }

    @Test
    fun `the attractive branch's infimum is attained at its own boundary`() {
        listOf(2.6, 3.568, 7.0, 58.8082, 210.4).forEach { reduced ->
            val scanned = attractiveBranchScannedInfimumCoupling(reduced, 200_000)
            val closed = attractiveBranchInfimumCoupling(reduced)
            assert(abs(scanned / closed - 1.0) < 1e-4)
        }
    }

    @Test
    fun `gate 4 - the scan reproduces the closed form at every sample count, and the argmin is the boundary`() {
        val reduced = 58.8082
        val closed = attractiveBranchInfimumCoupling(reduced)
        listOf(5_000, 20_000, 80_000).forEach {
            assert(abs(attractiveBranchScannedInfimumCoupling(reduced, it) / closed - 1.0) < 1e-12)
        }
        // the boundary is the argmin and not merely a sampled point: every interior point is above
        val ceiling = attractiveBranchAsymmetryCeiling(reduced)
        listOf(1e-6, 1e-4, 1e-2, 0.1, 0.5).forEach { back ->
            val zeta = -1.0 + (ceiling + 1.0) * (1.0 - back)
            assert(attractiveBranchValidityCoupling(zeta, reduced) > closed)
        }
    }

    @Test
    fun `the two branches agree to within a sixth at the boundary where they must meet`() {
        listOf(2.0, 2.6, 3.568, 5.0, 7.0, 42.0, 58.8, 210.4).forEach { reduced ->
            val ratio = attractiveBranchInfimumCoupling(reduced) /
                weakCouplingValidityCoupling(reduced)
            assert(ratio > 0.83)
            assert(ratio < 1.17)
        }
    }

    @Test
    fun `Kanduc Eq 65's bound rises monotonically away from the branch boundary`() {
        val reduced = 58.8082
        val ceiling = attractiveBranchAsymmetryCeiling(reduced)
        var previous = attractiveBranchValidityCoupling(ceiling, reduced)
        var zeta = ceiling
        repeat(200) {
            zeta = -1.0 + (zeta + 1.0) * 0.95
            val bound = attractiveBranchValidityCoupling(zeta, reduced)
            assert(bound > previous)
            previous = bound
        }
    }

    @Test
    fun `the excluded sliver next to the branch boundary is a fraction of a per cent wide`() {
        val bare = ChargedSurface(duplexCylinder, valency)
        val reduced = 7.0 / bare.gouyChapmanLength(bjerrumLength)
        val threshold = attractiveBranchAsymmetryThreshold(
            bare.couplingParameter(bjerrumLength), reduced
        )
        assert(threshold != null)
        val ceiling = attractiveBranchAsymmetryCeiling(reduced)
        val width = (ceiling - threshold!!) / (1.0 + ceiling)
        assert(width > 0.0)
        assert(width < 0.01)
    }

    @Test
    fun `a renormalised wall has no excluded sliver at all`() {
        val wall = ChargedSurface(saturated, valency)
        val reduced = 7.0 / wall.gouyChapmanLength(bjerrumLength)
        assert(
            attractiveBranchAsymmetryThreshold(wall.couplingParameter(bjerrumLength), reduced)
                == null
        )
    }

    // --------------------------------------------------- gate 5, the literature cross-check

    @Test
    fun `the bare duplex cylinder reproduces C-0005's published one-loop deviations`() {
        val surface = ChargedSurface(duplexCylinder, valency)
        val mu = surface.gouyChapmanLength(bjerrumLength)
        val coupling = surface.couplingParameter(bjerrumLength)
        val published = listOf(2.14, 1.63, 1.23, 0.89, 0.70)
        listOf(5.0, 7.0, 10.0, 15.0, 20.0).forEachIndexed { index, gap ->
            val deviation = meanFieldDeviation(coupling, gap / mu)
            assert(abs(deviation - published[index]) < 0.005)
        }
    }

    @Test
    fun `T-6's emitted loop expansion validity gap is this criterion at the bare duplex wall`() {
        val surface = ChargedSurface(duplexCylinder, valency)
        val mu = surface.gouyChapmanLength(bjerrumLength)
        val coupling = surface.couplingParameter(bjerrumLength)
        val gap = loopExpansionValidityGap(coupling, mu)!!
        assert(abs(gap - 13.517697558570946) < 1e-6)
        assert(abs(repulsiveBranchLogResidual(gap, mu, valency, bjerrumLength)) < 1e-6)
    }

    // ------------------------------------------- CH-0178: a criterion with no root at all

    @Test
    fun `a wall below the criterion's global minimum has NO validity gap and says so`() {
        // D/ln D has a global minimum of e, so for Xi <= e the criterion holds at every
        // separation and there is no root. Before T-221 the bisection returned e times mu.
        val wall = ChargedSurface(saturated, valency)
        val mu = wall.gouyChapmanLength(bjerrumLength)
        val coupling = wall.couplingParameter(bjerrumLength)
        assert(coupling < E)
        assert(loopExpansionValidityGap(coupling, mu) == null)
    }

    @Test
    fun `the withdrawn bracket floor was exactly e times the Gouy-Chapman length`() {
        // T-6 emitted 0.9241 nm for the hydrated-hard-core wall with Na+, Xi = 2.1006:
        // that is the bisection's own low bracket, e * mu, and not a separation.
        val hardCore = DnaOrigamiTile().hardCoreSurfaceChargeDensity(0.428)
        val wall = ChargedSurface(hardCore, 1)
        val mu = wall.gouyChapmanLength(bjerrumLength)
        assert(wall.couplingParameter(bjerrumLength) < E)
        assert(abs(E * mu - 0.9241) < 5e-4)
        assert(loopExpansionValidityGap(wall.couplingParameter(bjerrumLength), mu) == null)
    }

    @Test
    fun `a wall just above the global minimum still has a root, and it is a real one`() {
        val wall = ChargedSurface(duplexCylinder * 0.119, valency)
        val mu = wall.gouyChapmanLength(bjerrumLength)
        val coupling = wall.couplingParameter(bjerrumLength)
        assert(coupling > E)
        val gap = loopExpansionValidityGap(coupling, mu)!!
        assert(abs(repulsiveBranchLogResidual(gap, mu, valency, bjerrumLength)) < 1e-9)
    }

    @Test
    fun `the full one-loop boundary also returns null where the deviation never reaches one`() {
        val wall = ChargedSurface(0.002, valency)
        val mu = wall.gouyChapmanLength(bjerrumLength)
        assert(meanFieldValidityGap(wall.couplingParameter(bjerrumLength), mu) == null)
    }

    // ------------------------------------------------- the Manning-renormalised identity

    @Test
    fun `a Manning-renormalised cylinder's Gouy-Chapman length is exactly the helix radius`() {
        listOf(0.5, 0.7141, 1.3).forEach { lb ->
            listOf(1, 2, 3).forEach { q ->
                listOf(0.34, 0.30).forEach { rise ->
                    listOf(1.0, 0.9086).forEach { radius ->
                        val tile = DnaOrigamiTile(risePerBasePair = rise, helixRadius = radius)
                        val bare = tile.duplexSurfaceChargeDensity
                        val surviving = tile.manningSurvivingFraction(q, lb)
                        val sigma = bare * surviving
                        val mu = ChargedSurface(sigma, q).gouyChapmanLength(lb)
                        // the identity holds wherever anything condenses at all
                        if (surviving < 1.0) assert(mu.isCloseTo(radius))
                    }
                }
            }
        }
    }
}
