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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.sqrt
import kotlin.test.Test

/**
 * `T-3b` gate tests for the **cheap bound** that runs before the 2-D solve, and for the
 * taper fit that turns a solved lateral profile into the `(depth, width)` pair
 * `C-0006`/`C-0009` consume.
 *
 * The cheap bound has two halves and they are one-sided in opposite directions:
 * the **width** comes from the transverse eigenvalue of the linearised slit, which is a
 * rigorous *upper* bound on the taper's decay length; the **depth** comes from the exact
 * half-plane superposition anchor, which ignores both the electrode image and the tile's
 * charge saturation. Neither is asserted here to be right — only to be what it claims.
 */
class TileEdgeFringingTest {

    private val lb = bjerrumLength()
    private val buffer = MagnesiumChlorideBuffer(2.0)
    private val kappa = buffer.inverseDebyeLength()
    private val ions = IonModel(buffer.magnesiumNumberDensity)
    private val freeBuffer = GapMedium()

    /** The Manning-renormalised tile charge facing the gap — `C-0005`/`C-0008`. */
    private val tileCharge = -1276.0 / 2.0 / 1600.0

    // gate 1 — dimensional consistency

    @Test
    fun `gate 1 should give the transverse decay bound the dimensions of an inverse length`() {
        // q0^2 >= kappa^2 + (pi/2h)^2 — the two terms must be commensurable, so quadrupling
        // both kappa and 1/h must double the bound exactly.
        val one = transverseDecayRateBound(kappa, 10.0)
        val four = transverseDecayRateBound(2.0 * kappa, 5.0)
        assert(four.isCloseTo(2.0 * one, 1e-12))
        assert(one.isCloseTo(sqrt(kappa * kappa + (PI / 20.0) * (PI / 20.0)), 1e-12))
    }

    @Test
    fun `gate 1 should return the bulk screening exactly at zero potential`() {
        // kappa_loc^2(y) = -4 pi l_B drho/dy, and at y = 0 that is 4 pi l_B (4c + 2c) = 24 pi l_B c,
        // which IS kappa^2 for a 2:1 electrolyte. Asserting it through the identity rather than
        // through the formula is what would catch a valency dropped in the Boltzmann factor.
        val screening = localScreening(0.0, ions, freeBuffer, lb)
        assert(screening.isCloseTo(kappa * kappa, 1e-12))
    }

    @Test
    fun `gate 1 should make the equivalent taper width a length through its own moment identity`() {
        // The fit is defined by two moments of the load deficit, so the zeroth moment of the
        // fitted raised cosine must return depth x interior x W/2 by construction.
        val width = 4.0
        val depth = 0.5
        val interior = 0.0625
        val fit = fitEdgeTaper(
            distanceFromEdge = DoubleArray(2001) { it * 20.0 / 2000.0 },
            load = DoubleArray(2001) {
                raisedCosineLoad(it * 20.0 / 2000.0, interior, depth, width)
            },
            interiorLoad = interior
        )
        assert(fit.loadDeficit.isCloseTo(depth * interior * width / 2.0, 1e-6))
    }

    // gate 2 — limiting cases

    @Test
    fun `gate 2 should reduce the transverse eigenvalue to its closed form on uniform screening`() {
        // With kappa_loc constant the eigenproblem -phi'' + kappa^2 phi = q^2 phi with
        // phi(0) = 0 and phi'(h) = 0 has the exact lowest eigenvalue kappa^2 + (pi/2h)^2.
        // This is the one case where the numerical eigensolver has an answer to be right about.
        val height = 10.0
        val nodes = 400
        val z = DoubleArray(nodes + 1) { height * it / nodes }
        val screening = DoubleArray(nodes + 1) { kappa * kappa }
        val rate = transverseDecayRate(z, screening)
        assert(rate.isCloseTo(transverseDecayRateBound(kappa, height), 1e-4))
    }

    @Test
    fun `gate 2 should approach the bulk inverse Debye length as the gap grows`() {
        val wide = transverseDecayRate(
            DoubleArray(801) { 200.0 * it / 800.0 },
            DoubleArray(801) { kappa * kappa }
        )
        assert(wide.isCloseTo(kappa, 2e-3))
    }

    @Test
    fun `gate 2 should approach the geometric mode as the screening vanishes`() {
        val height = 8.0
        val nodes = 800
        val z = DoubleArray(nodes + 1) { height * it / nodes }
        val rate = transverseDecayRate(z, DoubleArray(nodes + 1) { 1e-12 })
        assert(rate.isCloseTo(PI / (2.0 * height), 1e-4))
    }

    @Test
    fun `gate 2 should raise the transverse rate above its own bound when counterions accumulate`() {
        // kappa_loc^2 >= kappa^2 pointwise, because counterion accumulation only strengthens
        // screening — so the true eigenvalue can only exceed the closed-form bound, which is
        // exactly what makes the bound an UPPER bound on the taper width.
        val height = 10.0
        val nodes = 600
        val z = DoubleArray(nodes + 1) { height * it / nodes }
        val screening = DoubleArray(nodes + 1) {
            localScreening(-2.0 * exp(-kappa * (height - z[it])), ions, freeBuffer, lb)
        }
        assert(transverseDecayRate(z, screening) > transverseDecayRateBound(kappa, height))
    }

    @Test
    fun `gate 2 should make the half plane superposition depth vanish for an uncharged tile`() {
        // With no tile charge the rim has nothing to lose, so the cheap depth is exactly zero.
        val depth = halfPlaneSuperpositionDepth(
            gapHeight = 10.0,
            electrodeReducedPotential = 2.0,
            tileSurfaceChargeDensity = 0.0,
            inverseDebyeLength = kappa,
            bjerrumLength = lb
        )
        assert(depth.isCloseTo(0.0, 1e-12))
    }

    @Test
    fun `gate 2 should put the half plane superposition depth strictly inside zero and one`() {
        val depth = halfPlaneSuperpositionDepth(
            gapHeight = 10.0,
            electrodeReducedPotential = 4.0,
            tileSurfaceChargeDensity = tileCharge,
            inverseDebyeLength = kappa,
            bjerrumLength = lb
        )
        assert(depth > 0.0)
        assert(depth < 1.0)
    }

    // gate 3 — symmetry and conservation

    @Test
    fun `gate 3 should return zero depth and zero deficit for a uniform load`() {
        val interior = 0.0625
        val fit = fitEdgeTaper(
            distanceFromEdge = DoubleArray(101) { it * 0.2 },
            load = DoubleArray(101) { interior },
            interiorLoad = interior
        )
        assert(fit.loadDeficit.isCloseTo(0.0, 1e-12))
        assert(fit.depth.isCloseTo(0.0, 1e-12))
    }

    @Test
    fun `gate 3 should conserve the total load between the raw profile and the fitted taper`() {
        // The fit exists to be substituted into C-0006's plate, so what it must conserve is the
        // integral the plate reacts to. Asserted over the tile half-width, not over the taper.
        val halfWidth = 20.0
        val interior = 0.05
        val samples = 4001
        val s = DoubleArray(samples) { halfWidth * it / (samples - 1) }
        val load = DoubleArray(samples) { interior * (1.0 - 0.42 * exp(-s[it] / 2.6)) }
        val fit = fitEdgeTaper(s, load, interior)
        val fitted = fit.depth * interior * fit.equivalentWidth / 2.0
        assert(fitted.isCloseTo(fit.loadDeficit, 1e-6))
    }

    // gate 4 — numerical convergence

    @Test
    fun `gate 4 should round trip the taper fit on the very profile that C-0006 consumes`() {
        // Fed the raised cosine `edgeTaperedPressure` generates, the two-moment fit must return
        // that cosine's own (depth, width). A round trip, not a resemblance.
        val width = 5.5
        val depth = 0.37
        val interior = 0.0625
        val samples = 8001
        val s = DoubleArray(samples) { 20.0 * it / (samples - 1) }
        val fit = fitEdgeTaper(s, DoubleArray(samples) { raisedCosineLoad(s[it], interior, depth, width) }, interior)
        // Exact in the continuum; the residual here is the trapezoid rule's, and it falls as
        // the square of the sampling — which the next test asserts as sampling independence.
        assert(fit.depth.isCloseTo(depth, 1e-4))
        assert(fit.equivalentWidth.isCloseTo(width, 1e-4))
    }

    @Test
    fun `gate 4 should make the taper fit independent of the sampling of the profile`() {
        val interior = 0.05
        val exact = { s: Double -> interior * (1.0 - 0.4 * exp(-s / 3.0)) }
        val coarse = DoubleArray(801) { 20.0 * it / 800.0 }
        val fine = DoubleArray(6401) { 20.0 * it / 6400.0 }
        val a = fitEdgeTaper(coarse, DoubleArray(coarse.size) { exact(coarse[it]) }, interior)
        val b = fitEdgeTaper(fine, DoubleArray(fine.size) { exact(fine[it]) }, interior)
        assert(a.depth.isCloseTo(b.depth, 1e-4))
        assert(a.equivalentWidth.isCloseTo(b.equivalentWidth, 1e-4))
    }

    @Test
    fun `gate 4 should converge the transverse eigenvalue at second order in the mesh`() {
        val height = 10.0
        val exact = transverseDecayRateBound(kappa, height)
        fun error(n: Int): Double {
            val z = DoubleArray(n + 1) { height * it / n }
            return abs(transverseDecayRate(z, DoubleArray(n + 1) { kappa * kappa }) - exact)
        }
        val ratio = error(100) / error(200)
        assert(ratio > 3.0)
        assert(ratio < 5.0)
    }

    // gate 5 — upstream cross-check

    @Test
    fun `gate 5 should recover the local screening profile of a T-3a solve at its own walls`() {
        // The profile enters the cheap bound, so it must be the one T-3a's solver produced —
        // read through the PUBLIC surface of GapSolution rather than recomputed from a formula.
        val gap = PoissonBoltzmannGap(10.0, ions, uniformMedium(freeBuffer), lb, nodes = 1000)
        val solution = gap.solve(0.0, tileCharge)
        val screening = localScreeningProfile(solution, ions, freeBuffer, lb)
        assert(screening.size == solution.reducedPotential.size)
        // in the middle of the gap the potential is small and the screening is the bulk one
        val middle = screening[screening.size / 2]
        assert(middle > 0.9 * kappa * kappa)
        // at the negatively charged tile the counterions accumulate, so the screening is larger
        assert(screening[screening.size - 1] > middle)
    }

    @Test
    fun `gate 5 should place the cheap taper width below the Debye length C-0006 assumed`() {
        // The prediction the cheap bound is run to make, stated before the 2-D solve:
        // the geometric term (pi/2h)^2 is comparable to kappa^2 at every working gap, so the
        // taper cannot be as wide as the 4 nm C-0006 used, and it NARROWS as the gap closes.
        val ten = 1.0 / transverseDecayRateBound(kappa, 10.0)
        val five = 1.0 / transverseDecayRateBound(kappa, 5.0)
        assert(ten < 4.0)
        assert(five < ten)
    }

}

/** The raised cosine `edgeTaperedPressure` builds, as a function of distance from the rim. */
private fun raisedCosineLoad(
    distanceFromEdge: Double,
    interior: Double,
    depth: Double,
    width: Double
): Double = if (distanceFromEdge >= width) interior
else interior * (1.0 - depth * 0.5 * (1.0 + cos(PI * distanceFromEdge / width)))
