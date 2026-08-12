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
import kotlin.math.cosh
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt
import kotlin.test.Test

/**
 * `T-3a` gate tests for the **asymmetric 2:1** Gouy-Chapman closed forms.
 *
 * The whole point of this file is that `MgCl₂` is not a `z:z` electrolyte, so none of the
 * textbook symmetric results may be used. Every expression here is derived from the 2:1
 * first integral, and the symmetric forms are carried alongside **only** as a cross-check
 * that the machinery reproduces them when the electrolyte is made symmetric.
 */
class AsymmetricGouyChapmanTest {

    private val lb = bjerrumLength()
    private val buffer = MagnesiumChlorideBuffer(2.0)
    private val kappa = buffer.inverseDebyeLength()

    // gate 1 — dimensional consistency and internal identities

    @Test
    fun `gate 1 should make the 2 to 1 first integral vanish at zero potential and be quadratic around it`() {
        // f(y) = (e^-2y + 2e^y - 3)/3 is the reduced (y'/kappa)^2 of the 2:1 planar problem.
        // It must vanish at y = 0 (bulk) and reduce to y^2 there, which is what pins kappa^2 = 24 pi l_B c.
        assert(asymmetricFirstIntegral(0.0).isCloseTo(0.0, 1e-14))
        assert((asymmetricFirstIntegral(1e-4) / 1e-8).isCloseTo(1.0, 1e-3))
        assert((asymmetricFirstIntegral(-1e-4) / 1e-8).isCloseTo(1.0, 1e-3))
        // and it is NOT symmetric in y — that asymmetry is the physics this task exists for
        assert(!asymmetricFirstIntegral(2.0).isCloseTo(asymmetricFirstIntegral(-2.0), 1e-3))
    }

    @Test
    fun `gate 1 should invert the surface charge to surface potential relation exactly`() {
        listOf(-2.0, -0.399, -0.05, 0.05, 0.399, 2.2).forEach { sigma ->
            val y0 = asymmetricReducedSurfacePotential(sigma, kappa, lb)
            assert(asymmetricSurfaceChargeDensity(y0, kappa, lb).isCloseTo(sigma, 1e-9))
        }
    }

    @Test
    fun `gate 1 should give the effective charge density the dimensions of a charge density`() {
        // sigma_eff = kappa A / (4 pi l_B): [1/nm] * [1] / [nm] = [1/nm^2]. Asserted through the
        // identity rather than through the formula, so a transposed factor cannot hide.
        val y0 = asymmetricReducedSurfacePotential(-0.399, kappa, lb)
        val amplitude = asymmetricFarFieldAmplitude(y0)
        assert(
            asymmetricEffectiveChargeDensity(y0, kappa, lb)
                .isCloseTo(kappa * amplitude / (4.0 * PI * lb), 1e-12)
        )
    }

    // gate 2 — limiting cases

    @Test
    fun `gate 2 should return the bare charge as the effective charge in the Debye Huckel limit`() {
        // As the surface charge goes to zero the double layer linearises and saturation cannot bite,
        // so sigma_eff must return sigma itself. This is the limit in which "effective charge" is empty.
        listOf(1e-5, 1e-4, 1e-3).forEach { sigma ->
            val y0 = asymmetricReducedSurfacePotential(-sigma, kappa, lb)
            assert(asymmetricEffectiveChargeDensity(y0, kappa, lb).isCloseTo(-sigma, 1.5e-2))
        }
        // and the approach is first order in the charge: halving sigma halves the error
        fun error(sigma: Double): Double {
            val y0 = asymmetricReducedSurfacePotential(-sigma, kappa, lb)
            return abs(asymmetricEffectiveChargeDensity(y0, kappa, lb) / -sigma - 1.0)
        }
        assert((error(1e-3) / error(5e-4)).isCloseTo(2.0, 5e-2))
    }

    @Test
    fun `gate 2 should saturate the far field amplitude at 12 minus 6 root 3 for a divalent counterion`() {
        // The 2:1 saturation constants, derived rather than cited:
        //   negative wall (Mg2+ counterion): A -> -(12 - 6 sqrt3) = -1.60770
        //   positive wall (Cl- counterion):  A -> +6
        assert(SATURATED_AMPLITUDE_DIVALENT_COUNTERION.isCloseTo(12.0 - 6.0 * sqrt(3.0), 1e-14))
        assert(SATURATED_AMPLITUDE_MONOVALENT_COUNTERION.isCloseTo(6.0, 1e-14))
        assert(asymmetricFarFieldAmplitude(-40.0).isCloseTo(-SATURATED_AMPLITUDE_DIVALENT_COUNTERION, 1e-9))
        assert(asymmetricFarFieldAmplitude(80.0).isCloseTo(SATURATED_AMPLITUDE_MONOVALENT_COUNTERION, 1e-9))
        // the two saturations differ by exactly 2 + sqrt(3) — the asymmetry, in closed form
        assert(
            (SATURATED_AMPLITUDE_MONOVALENT_COUNTERION / SATURATED_AMPLITUDE_DIVALENT_COUNTERION)
                .isCloseTo(2.0 + sqrt(3.0), 1e-12)
        )
    }

    @Test
    fun `gate 2 should approach the saturated amplitude monotonically from below`() {
        val amplitudes = listOf(-1.0, -2.0, -3.0, -5.0, -10.0).map { abs(asymmetricFarFieldAmplitude(it)) }
        amplitudes.zipWithNext().forEach { (low, high) -> assert(high > low) }
        assert(amplitudes.last() < SATURATED_AMPLITUDE_DIVALENT_COUNTERION)
    }

    @Test
    fun `gate 2 should reproduce the symmetric z to z closed form when the electrolyte is made symmetric`() {
        // 4/z tanh(z y0 / 4), the textbook Gouy-Chapman amplitude in the valency-free reduced potential.
        assert(symmetricFarFieldAmplitude(1e-5, 1).isCloseTo(1e-5, 1e-6))
        assert(symmetricFarFieldAmplitude(-40.0, 2).isCloseTo(-2.0, 1e-9))
        assert(symmetricFarFieldAmplitude(100.0, 1).isCloseTo(4.0, 1e-9))
    }

    // gate 3 — symmetry and conservation

    @Test
    fun `gate 3 should conserve charge between the wall and its diffuse layer`() {
        // Electroneutrality of a single plate: the integrated space charge must cancel the wall's.
        // Integrated analytically through the closed-form profile, by Simpson on a long domain
        // plus the analytic exponential tail — the same construction T-6 used for Naji Eq. (9).
        val sigma = -0.399
        val y0 = asymmetricReducedSurfacePotential(sigma, kappa, lb)
        val far = 200.0
        val steps = 40000
        val step = far / steps
        val magnesium = buffer.magnesiumNumberDensity
        val chloride = buffer.chlorideNumberDensity
        fun density(z: Double): Double {
            val y = asymmetricPotentialProfile(z, y0, kappa)
            return 2.0 * magnesium * exp(-2.0 * y) - chloride * exp(y)
        }
        var integral = density(0.0) + density(far)
        for (i in 1 until steps) {
            integral += (if (i % 2 == 0) 2.0 else 4.0) * density(i * step)
        }
        integral *= step / 3.0
        // the tail beyond `far`, where y = A e^{-kappa z} and rho = -kappa^2 y / (4 pi l_B)
        val amplitude = asymmetricFarFieldAmplitude(y0)
        val tail = -kappa * amplitude * exp(-kappa * far) / (4.0 * PI * lb)
        assert((integral + tail).isCloseTo(-sigma, 1e-6))
    }

    @Test
    fun `gate 3 should keep the sign of the effective charge equal to the sign of the bare charge`() {
        // Saturation reduces the magnitude; it can never invert the sign. Charge inversion is a
        // correlation effect and mean-field PB cannot produce it — C-0005 says so and this locks it.
        listOf(-3.0, -0.399, -1e-3, 1e-3, 0.399, 3.0).forEach { sigma ->
            val y0 = asymmetricReducedSurfacePotential(sigma, kappa, lb)
            assert(asymmetricEffectiveChargeDensity(y0, kappa, lb) * sigma > 0.0)
        }
        // The NEGATIVE surface — the tile — is always screened DOWN, at every charge.
        listOf(-1e-3, -0.05, -0.399, -3.0).forEach { sigma ->
            val y0 = asymmetricReducedSurfacePotential(sigma, kappa, lb)
            assert(abs(asymmetricEffectiveChargeDensity(y0, kappa, lb)) < abs(sigma))
        }
    }

    @Test
    fun `gate 2 should enhance the effective charge of a positive surface above its bare value`() {
        // Not a bug, and not true of any symmetric electrolyte: at a POSITIVE wall in MgCl2 the
        // divalent COION is expelled harder than a monovalent one would be, so screening is weaker
        // than Debye-Huckel and sigma_eff EXCEEDS sigma over a whole range of charge. The expansion
        // A = y0 + y0^2/6 holds for both branches, and only for y0 > 0 does it run upward.
        val enhancement = (1..2000).map { it * 6e-3 }.map { y0 ->
            asymmetricEffectiveChargeDensity(y0, kappa, lb) /
                    asymmetricSurfaceChargeDensity(y0, kappa, lb)
        }
        assert(enhancement.max().isCloseTo(1.2378, relativeTolerance = 1e-3))
        // and it must still saturate: at large charge the ratio falls back below one
        val y0 = asymmetricReducedSurfacePotential(2.2, kappa, lb)
        assert(asymmetricEffectiveChargeDensity(y0, kappa, lb) < 2.2)
    }

    // gate 4 — numerical convergence

    @Test
    fun `gate 4 should satisfy the 2 to 1 Poisson Boltzmann equation to second order in the step`() {
        // The closed-form profile is verified BY SUBSTITUTION into y'' = -(kappa^2/3)(e^-2y - e^y),
        // and the residual is checked as a convergence ORDER, not against a tolerance — which is
        // what would catch a formula that happens to be numerically close but structurally wrong.
        val y0 = asymmetricReducedSurfacePotential(-0.399, kappa, lb)
        fun residual(step: Double): Double {
            val z = 1.5
            val second = (asymmetricPotentialProfile(z + step, y0, kappa) -
                    2.0 * asymmetricPotentialProfile(z, y0, kappa) +
                    asymmetricPotentialProfile(z - step, y0, kappa)) / (step * step)
            val y = asymmetricPotentialProfile(z, y0, kappa)
            val expected = -(kappa * kappa / 3.0) * (exp(-2.0 * y) - exp(y))
            return abs(second - expected)
        }
        assert((residual(1e-2) / residual(5e-3)).isCloseTo(4.0, 0.1))
    }

    @Test
    fun `gate 4 should reach the far field amplitude as the profile decays`() {
        val y0 = asymmetricReducedSurfacePotential(-0.399, kappa, lb)
        val amplitude = asymmetricFarFieldAmplitude(y0)
        listOf(60.0, 80.0, 100.0).forEach { z ->
            assert(
                (asymmetricPotentialProfile(z, y0, kappa) * exp(kappa * z))
                    .isCloseTo(amplitude, 1e-6)
            )
        }
        // and the approach is itself exponential: the residual falls by e^{-kappa dz} per step
        fun residual(z: Double) =
            abs(asymmetricPotentialProfile(z, y0, kappa) * exp(kappa * z) / amplitude - 1.0)
        assert((residual(20.0) / residual(30.0)).isCloseTo(exp(kappa * 10.0), 1e-2))
    }

    // gate 5 — literature and upstream cross-check

    @Test
    fun `gate 5 should reproduce C-0005 symmetric ceiling and undercut it by the 2 to 1 asymmetry`() {
        // C-0005 quotes sigma_eff = kappa/(pi l_B q) = 0.0568 e/nm^2 at 2 mM and flags it as an
        // order-of-magnitude ceiling read from the SYMMETRIC z:z form. Both halves are asserted:
        // the symmetric route reproduces its number, and the asymmetric solve lands below it.
        val symmetric = saturatedEffectiveChargeDensity(kappa, 2, lb)
        assert(symmetric.isCloseTo(0.05675572, relativeTolerance = 1e-6))
        assert(symmetric.isCloseTo(kappa * symmetricFarFieldAmplitude(-40.0, 2) / (4.0 * PI * lb) * -1.0, 1e-9))
        val asymmetric = asymmetricSaturatedEffectiveChargeDensity(kappa, lb, negativeSurface = true)
        assert(asymmetric.isCloseTo(0.04562295, relativeTolerance = 1e-6))
        // the ceiling is a ceiling: exactly 6 - 3 sqrt(3) = 0.8038 of it
        assert((asymmetric / symmetric).isCloseTo(6.0 - 3.0 * sqrt(3.0), 1e-9))
        // and the POSITIVE electrode saturates 3.73x higher than the negative tile, in the same buffer
        val positive = asymmetricSaturatedEffectiveChargeDensity(kappa, lb, negativeSurface = false)
        assert((positive / asymmetric).isCloseTo(2.0 + sqrt(3.0), 1e-9))
    }

    @Test
    fun `gate 5 should reproduce the Debye length of the 2 to 1 buffer through the first integral`() {
        // kappa in the first integral must be the one T-6 derived from I = 3c, not a monovalent one.
        assert((1.0 / kappa).isCloseTo(3.92688, relativeTolerance = 1e-5))
        assert(kappa.isCloseTo(sqrt(24.0 * PI * lb * buffer.magnesiumNumberDensity), 1e-12))
    }

    // the cheap bound: the linearised mixed boundary-value problem

    @Test
    fun `gate 2 should decay the zero bias linear pressure with half the Debye length`() {
        // The cheap bound in closed form. At zero electrode potential the tile interacts with its own
        // IMAGE in the grounded conductor, so the pressure carries e^{-2 kappa h} and the decay length
        // is lambda_D / 2 — not lambda_D. This is a limiting case AND the reason CH-0004 cannot be
        // settled by naming one length.
        fun pressure(h: Double) = linearMixedDisjoiningPressure(h, 0.0, -0.399, kappa, lb)
        assert(pressure(10.0) < 0.0)
        val decay = -(40.0 - 30.0) / ln(pressure(40.0) / pressure(30.0))
        assert(decay.isCloseTo(0.5 / kappa, 1e-5))
    }

    @Test
    fun `gate 2 should decay the biased linear pressure with the full Debye length`() {
        fun pressure(h: Double) = linearMixedDisjoiningPressure(h, 4.0, -0.399, kappa, lb)
        assert(pressure(20.0) < 0.0)
        val decay = -(60.0 - 40.0) / ln(pressure(60.0) / pressure(40.0))
        assert(decay.isCloseTo(1.0 / kappa, 1e-4))
    }

    @Test
    fun `gate 1 should make the linear mixed pressure the difference of two squares`() {
        // P = kT kappa^2 (y_d^2 - B^2) / (8 pi l_B) with B the sinh coefficient. Asserted against an
        // independent evaluation of the osmotic-minus-Maxwell form at an arbitrary interior point,
        // which is the same statement as "the first integral is constant" in the linear theory.
        val h = 6.0
        val yd = 2.0
        val sigma = -0.399
        val s = 4.0 * PI * lb * sigma
        val b = (s / kappa - yd * kotlin.math.sinh(kappa * h)) / cosh(kappa * h)
        val z = 2.3
        val y = yd * cosh(kappa * z) + b * kotlin.math.sinh(kappa * z)
        val slope = kappa * (yd * kotlin.math.sinh(kappa * z) + b * cosh(kappa * z))
        val direct = (kappa * kappa * y * y - slope * slope) / (8.0 * PI * lb)
        assert(linearMixedDisjoiningPressure(h, yd, sigma, kappa, lb).isCloseTo(direct, 1e-9))
    }

}
