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
import kotlin.math.exp
import kotlin.math.ln
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-60` gate tests for the collar multiplier `μ(h)`, its logarithmic gradient, and the
 * closed-form cheap estimate that runs before any 2-D solve.
 *
 * The whole of `T-60` rests on one distinction: a multiplier on the **level** of the
 * electrostatic force cancels at a force-pinned operating point, and only `d ln μ/dh`
 * survives (`CH-0035`). So the object under test is a *derivative*, and every gate below is
 * written about the derivative rather than about `μ` itself.
 */
class CollarMultiplierTest {

    private val edge = 40.0

    // gate 1 — dimensional consistency

    @Test
    fun `gate 1 should make the collar multiplier dimensionless and equal to one plus four collars over the edge`() {
        // T-3b emits a force DEFICIT per unit length of edge, so a NEGATIVE deficit is an
        // enhancement and the effective collar width is -deficit/interior. The additive
        // mapping is then exactly `1 + 4w/L` — asserted through the collar, which is the
        // form C-0022 quotes, rather than through the expression under test.
        val interior = 0.039
        val collar = 1.65
        val deficit = -collar * interior
        val multiplier = additiveCollarMultiplier(deficit, interior, edge)
        assert(multiplier.isCloseTo(1.0 + 4.0 * collar / edge, 1e-12))
        // dimensionless: scaling the load scale leaves it untouched
        assert(
            additiveCollarMultiplier(1000.0 * deficit, 1000.0 * interior, edge)
                .isCloseTo(multiplier, 1e-12)
        )
    }

    @Test
    fun `gate 1 should reproduce C-0022's two published mappings at the design point`() {
        // C-0022's design point: 2 mM, 10 nm, 0.192 V — read from T-3b's own result file,
        // not from its prose: interior 0.0390316 pN/nm^2, deficit -0.0644311 pN/nm, and the
        // first moment that its two published fractions imply. +14.71 % on the
        // minimum-margin mapping and +16.51 % on the additive one, collar 1.6507 nm.
        val interior = 0.0390316
        val deficit = -0.0644311
        val firstMoment = -0.14046488
        assert((-deficit / interior).isCloseTo(1.6507, 1e-4))
        assert(
            additiveCollarMultiplier(deficit, interior, edge).isCloseTo(1.16507426, 1e-5)
        )
        assert(
            minimumMarginCollarMultiplier(deficit, firstMoment, interior, edge)
                .isCloseTo(1.147080774, 1e-5)
        )
        // the minimum-margin mapping counts a corner once and the additive one twice, so
        // the first is always the SMALLER correction for an enhancement
        assert(
            minimumMarginCollarMultiplier(deficit, firstMoment, interior, edge) <
                    additiveCollarMultiplier(deficit, interior, edge)
        )
    }

    @Test
    fun `gate 1 should give the log gradient the dimensions of an inverse length`() {
        // `d ln mu/dh` is asserted through its own DEFINITION — the log of a ratio over a
        // length — and not through any expression: halving every gap doubles the gradient.
        val gaps = doubleArrayOf(4.0, 6.0, 8.0, 10.0)
        val curve = CollarMultiplierCurve(gaps, DoubleArray(gaps.size) { exp(0.02 * gaps[it]) })
        val halved = CollarMultiplierCurve(
            DoubleArray(gaps.size) { 0.5 * gaps[it] },
            DoubleArray(gaps.size) { exp(0.02 * gaps[it]) }
        )
        assert(curve.logGradientAt(7.0).isCloseTo(0.02, 1e-9))
        assert(halved.logGradientAt(3.5).isCloseTo(0.04, 1e-9))
    }

    @Test
    fun `gate 1 should divide a central difference by the SEPARATION of its two gaps`() {
        // The factor-of-two trap that actually fired here: a symmetric difference of
        // half-step d has denominator 2d, which IS the separation of the two gaps — and a
        // hand-written `/(2*step)` is half the right answer whenever `step` is the
        // separation rather than the half-step. Neither reading fails a dimensional check.
        val rate = 0.0181
        assert(
            centralLogGradient(exp(rate * 6.0), exp(rate * 7.0), 6.0, 7.0)
                .isCloseTo(rate, 1e-12)
        )
        assert(
            centralLogGradient(exp(rate * 6.0), exp(rate * 7.0), 6.0, 7.0)
                .isCloseTo(ln(exp(rate * 7.0) / exp(rate * 6.0)) / 1.0, 1e-12)
        )
        // and it agrees with the interpolant's own node slope on a uniform mesh
        val gaps = doubleArrayOf(6.0, 6.5, 7.0)
        val curve = CollarMultiplierCurve(gaps, doubleArrayOf(1.0866, 1.0969, 1.1063))
        assert(
            curve.logGradientAt(6.5)
                .isCloseTo(centralLogGradient(1.0866, 1.1063, 6.0, 7.0), 1e-12)
        )
        assertFailsWith<IllegalArgumentException> {
            centralLogGradient(1.0, 1.1, 7.0, 6.0)
        }
    }

    // gate 2 — limiting cases

    @Test
    fun `gate 2 should send the multiplier to one as the tile edge grows, as one over L`() {
        val interior = 0.039
        val deficit = -1.65 * interior
        val small = additiveCollarMultiplier(deficit, interior, 20.0)
        val large = additiveCollarMultiplier(deficit, interior, 100.0)
        assert(small > large)
        assert(large > 1.0)
        // the excess scales as 1/L exactly
        assert(((small - 1.0) / (large - 1.0)).isCloseTo(5.0, 1e-12))
        assert(additiveCollarMultiplier(deficit, interior, 1e12).isCloseTo(1.0, 1e-9))
    }

    @Test
    fun `gate 2 should give a zero collar a multiplier of exactly one and a gradient of exactly zero`() {
        assert(additiveCollarMultiplier(0.0, 0.039, edge) == 1.0)
        assert(minimumMarginCollarMultiplier(0.0, 0.0, 0.039, edge) == 1.0)
        val flat = CollarMultiplierCurve(
            doubleArrayOf(2.0, 4.0, 6.0, 8.0), doubleArrayOf(1.07, 1.07, 1.07, 1.07)
        )
        listOf(2.0, 3.3, 5.0, 6.7, 8.0).forEach {
            assert(flat.logGradientAt(it) == 0.0)
            assert(flat.multiplierAt(it).isCloseTo(1.07, 1e-12))
        }
    }

    @Test
    fun `gate 2 should reproduce a log-linear multiplier exactly, gradient and all`() {
        // The interpolant is a cubic Hermite on ln(mu) with parabolic node slopes, so a
        // straight line in ln(mu) must be reproduced to machine precision everywhere —
        // which is what makes the reported gradient and the used gradient one object.
        val rate = 0.0181
        val gaps = doubleArrayOf(3.0, 4.0, 5.5, 7.0, 9.0, 11.0)
        val curve = CollarMultiplierCurve(gaps, DoubleArray(gaps.size) { exp(rate * gaps[it]) })
        var probe = 3.0
        while (probe <= 11.0) {
            assert(curve.multiplierAt(probe).isCloseTo(exp(rate * probe), 1e-10))
            assert(curve.logGradientAt(probe).isCloseTo(rate, 1e-10))
            probe += 0.37
        }
    }

    @Test
    fun `gate 2 should clamp outside the solved range rather than extrapolate a collar`() {
        // CLAUDE.md and C-0027's own reader: a collar that grows without bound is not a
        // physical statement. Outside the range the multiplier is held and the gradient is
        // exactly zero, and every such evaluation is COUNTED so the study can assert that
        // the fold and the operating point are interior.
        val curve = CollarMultiplierCurve(
            doubleArrayOf(4.0, 6.0, 8.0), doubleArrayOf(1.04, 1.08, 1.12)
        )
        assert(curve.multiplierAt(2.0) == 1.04)
        assert(curve.multiplierAt(20.0) == 1.12)
        assert(curve.logGradientAt(2.0) == 0.0)
        assert(curve.logGradientAt(20.0) == 0.0)
        assert(curve.clampedEvaluations == 4)
        curve.multiplierAt(5.0)
        assert(curve.clampedEvaluations == 4)
        assert(curve.lowestGap == 4.0)
        assert(curve.highestGap == 8.0)
    }

    @Test
    fun `gate 2 should reject a curve that is not a function of the gap`() {
        assertFailsWith<IllegalArgumentException> {
            CollarMultiplierCurve(doubleArrayOf(4.0, 4.0), doubleArrayOf(1.0, 1.1))
        }
        assertFailsWith<IllegalArgumentException> {
            CollarMultiplierCurve(doubleArrayOf(6.0, 4.0), doubleArrayOf(1.0, 1.1))
        }
        assertFailsWith<IllegalArgumentException> {
            CollarMultiplierCurve(doubleArrayOf(4.0), doubleArrayOf(1.0))
        }
        assertFailsWith<IllegalArgumentException> {
            CollarMultiplierCurve(doubleArrayOf(4.0, 6.0), doubleArrayOf(1.0, -0.1))
        }
    }

    // gate 3 — the interpolant's derivative IS the central difference at a node

    @Test
    fun `gate 3 should make the interpolant's node derivative the central difference there`() {
        // The number reported and the number used must be the same object. On a uniform
        // mesh the parabolic node slope IS the central difference of ln(mu); asserted
        // against a difference computed here, from the samples, by a different expression.
        val gaps = doubleArrayOf(4.0, 5.0, 6.0, 7.0, 8.0)
        val values = doubleArrayOf(1.030, 1.058, 1.079, 1.096, 1.109)
        val curve = CollarMultiplierCurve(gaps, values)
        for (i in 1 until gaps.size - 1) {
            val central = (ln(values[i + 1]) - ln(values[i - 1])) / (gaps[i + 1] - gaps[i - 1])
            assert(curve.logGradientAt(gaps[i]).isCloseTo(central, 1e-12))
        }
    }

    @Test
    fun `gate 3 should keep the interpolated log gradient continuous across a node`() {
        val gaps = doubleArrayOf(4.0, 5.0, 6.0, 7.0, 8.0)
        val values = doubleArrayOf(1.030, 1.058, 1.079, 1.096, 1.109)
        val curve = CollarMultiplierCurve(gaps, values)
        listOf(5.0, 6.0, 7.0).forEach { node ->
            val below = curve.logGradientAt(node - 1e-7)
            val above = curve.logGradientAt(node + 1e-7)
            assert(abs(below - above) < 1e-5)
        }
    }

    // gate 4 — the cheap estimate

    @Test
    fun `gate 4 should give the cheap collar gradient estimate from the transverse eigenvalue alone`() {
        // w <= 1/q0 with q0^2 = kappa^2 + (pi/2h)^2 (C-0022's rigorous width ceiling), so
        // on the additive mapping mu = 1 + 4w/L and
        //     d ln mu/dh = (4/L)(dw/dh)/mu,  dw/dh = (pi^2/4) h^-3 / q0^3.
        // Asserted against the derivative of the ceiling computed here by finite difference
        // of `transverseDecayRateBound`, which shares no code with the expression.
        val kappa = MagnesiumChlorideBuffer(2.0).inverseDebyeLength()
        val h = 7.0
        val delta = 1e-5
        val width = { g: Double -> 1.0 / transverseDecayRateBound(kappa, g) }
        val numeric = (width(h + delta) - width(h - delta)) / (2.0 * delta)
        val mu = 1.0 + 4.0 * width(h) / edge
        val expected = (4.0 / edge) * numeric / mu
        assert(collarLogGradientEstimate(kappa, h, edge).isCloseTo(expected, 1e-6))
        // it is positive — the collar WIDENS with the gap, which is the sign the whole
        // stability consequence hangs on
        assert(collarLogGradientEstimate(kappa, h, edge) > 0.0)
    }

    @Test
    fun `gate 4 should send the cheap estimate to zero as the tile grows and as screening strengthens`() {
        val kappa = MagnesiumChlorideBuffer(2.0).inverseDebyeLength()
        assert(collarLogGradientEstimate(kappa, 7.0, 1e7) < 1e-6)
        // strong screening pins the collar at 1/kappa and kills its gap dependence
        assert(collarLogGradientEstimate(50.0, 7.0, edge) < 1e-6)
        // and with no screening at all the collar is purely geometric, w = 2h/pi, so
        // dw/dh = 2/pi exactly and the gradient is (4/L)(2/pi)/mu
        val free = 1e-9
        val mu = 1.0 + 4.0 * (2.0 * 7.0 / PI) / edge
        assert(
            collarLogGradientEstimate(free, 7.0, edge)
                .isCloseTo((4.0 / edge) * (2.0 / PI) / mu, 1e-6)
        )
    }

    @Test
    fun `gate 4 should reject unphysical arguments rather than return a number`() {
        val kappa = MagnesiumChlorideBuffer(2.0).inverseDebyeLength()
        assertFailsWith<IllegalArgumentException> { collarLogGradientEstimate(kappa, 0.0, edge) }
        assertFailsWith<IllegalArgumentException> { collarLogGradientEstimate(kappa, 7.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { collarLogGradientEstimate(-1.0, 7.0, edge) }
    }

    // gate 5 — the inherited difference-scheme spread, reproduced from C-0027's own numbers

    @Test
    fun `gate 5 should reproduce C-0027's inherited difference-scheme spread at 7 nm`() {
        // T-3b's five sampled gaps at 2 mM, each at ITS OWN bias: mu = 0.961 at 2 nm,
        // 1.036 at 4, 1.056 at 5, 1.105 at 7, 1.150 at 10. C-0027 differences them three
        // ways at 7 nm and reports 0.0133-0.0226 /nm. Reproduced here as the object T-60
        // exists to replace — a difference between three schemes, not a derivative.
        val backward = ln(1.105 / 1.056) / (7.0 - 5.0)
        val forward = ln(1.150 / 1.105) / (10.0 - 7.0)
        val central = ln(1.150 / 1.056) / (10.0 - 5.0)
        val low = minOf(backward, forward, central)
        val high = maxOf(backward, forward, central)
        assert(low.isCloseTo(0.0133, 1e-2))
        assert(high.isCloseTo(0.0226, 1e-2))
        // and the spread is 1.7x, which is what leaves the coupled tangent straddling zero
        assert((high / low) > 1.6)
    }

    @Test
    fun `gate 5 should put the cheap estimate inside a factor of two of C-0027's band`() {
        // The Plan predicts "about a factor of two, one-sided in neither direction". This
        // asserts the prediction rather than the outcome: if it fails, the Plan was wrong
        // and the claim must say so.
        val kappa = MagnesiumChlorideBuffer(2.0).inverseDebyeLength()
        val estimate = collarLogGradientEstimate(kappa, 7.0, edge)
        assert(estimate > 0.0133 / 2.0)
        assert(estimate < 0.0226 * 2.0)
    }

    @Test
    fun `gate 5 should hold the rigorous width ceiling that the cheap estimate is built on`() {
        // C-0022: the solved collar is 1.65 nm at the design point and the ceiling 1/q0 is
        // above it, everywhere. If this fails the cheap estimate is not merely imprecise,
        // it is built on a broken bound.
        val kappa = MagnesiumChlorideBuffer(2.0).inverseDebyeLength()
        assert(1.0 / transverseDecayRateBound(kappa, 10.0) > 1.6507)
        assert(1.0 / transverseDecayRateBound(kappa, 7.0) > 1.1964)
        assert(1.0 / transverseDecayRateBound(kappa, 5.0) > 0.7013)
        // and it is a ceiling, not an equality — the solved collar is 1.8x below it
        assert(1.0 / transverseDecayRateBound(kappa, 10.0) < 4.0)
    }
}
