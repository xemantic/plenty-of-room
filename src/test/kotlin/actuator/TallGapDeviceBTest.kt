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

package com.xemantic.nano.plentyofroom.actuator

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.electrostatics.MagnesiumChlorideBuffer
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.test.Test
import kotlin.test.assertFailsWith
import kotlin.test.assertNull

/**
 * `T-192` — device B in the corner NDI's answers to decisions 2 and 4 point at: a 10 pN/nm
 * coupling on a 17–26 nm layer at 0.5 mM.
 *
 * The five gates, named. Every closed form these tests grade against is written here rather
 * than imported, so a regression in the library cannot silently agree with itself.
 *
 * The synthetic field used throughout is `|F| = ψ e^{−h/λ}` with the diffuse drop standing in
 * for the bias — no Stern series — which makes every quantity in `P1` and `P2` exact:
 * the bias for a target force is `ψ = F* e^{h/λ}`, the decay length `ℓ = |F|/|k_es|` is `λ` at
 * every gap, and the fold of `R = k_c s` against no layer sits at a stroke of exactly one `λ`.
 */
class TallGapDeviceBTest {

    // ------------------------------------------------------------------ gate 1: dimensions

    @Test
    fun `gate 1 the density rule is a power law and the fit recovers an exact one`() {
        val exact = listOf(5.0 to 2.0 * 5.0.pow(-2.0), 7.0 to 2.0 * 7.0.pow(-2.0), 10.0 to 0.02)
        val rule = tallGapPowerLawFit("exact", exact)
        assert(rule.exponent.isCloseTo(-2.0, 1e-12))
        assert(rule.amplitude.isCloseTo(2.0, 1e-12))
        // sigma is nm^-2 and the height nm, so the amplitude carries nm^(-2-p) = nm^0 here
        assert(rule.densityAt(20.0).isCloseTo(2.0 / 400.0, 1e-12))
    }

    @Test
    fun `gate 1 the held-density rule is constant in the height`() {
        val rule = tallGapHeldDensityRule(0.024)
        assert(rule.densityAt(10.0).isCloseTo(0.024, 1e-15))
        assert(rule.densityAt(26.0).isCloseTo(0.024, 1e-15))
        assert(rule.exponent.isCloseTo(0.0, 1e-15))
    }

    @Test
    fun `gate 1 the stroke cap is a force over a stiffness and lands in nm`() {
        // C-0046's composed bound: placement k_c = F/delta and stability k_c > |k_eff|
        assert(tallGapStrokeCap(100.0, -25.0)!!.isCloseTo(4.0, 1e-15))
        assert(tallGapStabilityFloor(-25.0).isCloseTo(25.0, 1e-15))
    }

    @Test
    fun `gate 1 a stable state has no stability floor and therefore no cap`() {
        // A margin of infinity is not a margin, it is the absence of a requirement (CLAUDE.md).
        assert(tallGapStabilityFloor(12.0).isCloseTo(0.0, 1e-15))
        assertNull(tallGapStrokeCap(100.0, 12.0))
        assertNull(tallGapStrokeCap(100.0, 0.0))
    }

    @Test
    fun `gate 1 the fit refuses fewer than two points`() {
        assertFailsWith<IllegalArgumentException> {
            tallGapPowerLawFit("one", listOf(10.0 to 0.024))
        }
    }

    // ------------------------------------------------------------------ gate 2: limits

    @Test
    fun `gate 2 the trend rule reproduces the section 3 design points it was fitted through`() {
        val rule = tallGapPowerLawFit("section-3 trend", TALL_GAP_SECTION_3_DESIGN_POINTS)
        // the three points are not exactly collinear in log-log, so this is a fit residual
        TALL_GAP_SECTION_3_DESIGN_POINTS.forEach { (height, density) ->
            assert(abs(rule.densityAt(height) / density - 1.0) < 0.06)
        }
        // and it extrapolates downward in density, which is the direction a taller layer needs
        assert(rule.densityAt(26.0) < rule.densityAt(17.0))
        assert(rule.exponent < 0.0)
    }

    @Test
    fun `gate 2 the bias for a target force is exact on an exponential field`() {
        val lambda = 4.0
        val field = tallGapExponentialField(lambda)
        val sample = holdingBias(field, gap = 20.0, load = 100.0)
        // holdingBias exits on a 1e-10 ABSOLUTE bracket in the diffuse drop, and the drop this
        // synthetic field needs is ~0.015 V, so the relative floor of the answer is ~7e-9
        assert(sample!!.attraction.isCloseTo(100.0, 1e-7))
        assert(sample.appliedBias.isCloseTo(100.0 * exp(20.0 / lambda) / 1e6, 1e-7))
    }

    @Test
    fun `gate 2 an unreachable force returns null and not a clamped number`() {
        val field = tallGapExponentialField(4.0)
        // the diffuse ceiling caps psi, so a large enough load at a large enough gap is refused
        assertNull(holdingBias(field, gap = 26.0, load = 1e6))
    }

    @Test
    fun `gate 2 the measured decay length is the field's own decay length at every gap`() {
        val lambda = 7.85
        val field = tallGapExponentialField(lambda)
        listOf(17.0, 20.0, 23.0, 26.0).forEach { gap ->
            val measured = tallGapDecayLength(gap) { field.sample(it, 0.05).attraction }
            assert(measured.isCloseTo(lambda, 1e-4))
        }
    }

    @Test
    fun `gate 2 the counterion dominance ratio falls as one over the gap`() {
        val at17 = tallGapCounterionDominance(gap = 17.0, concentration = 2.0)
        val at34 = tallGapCounterionDominance(gap = 34.0, concentration = 2.0)
        assert((at17 / at34).isCloseTo(2.0, 1e-12))
        // and it rises as the buffer is diluted, in exact proportion
        val dilute = tallGapCounterionDominance(gap = 17.0, concentration = 0.5)
        assert((dilute / at17).isCloseTo(4.0, 1e-12))
    }

    @Test
    fun `gate 2 the deepest reachable gap is exact on an exponential field`() {
        // |F| = psi A e^{-h/lambda} reaches the target at h = lambda ln(psi A / target), exactly
        val lambda = 7.85
        val amplitude = SYNTHETIC_AMPLITUDE
        val drop = 0.2
        val deepest = tallGapDeepestReachableGap(load = 100.0, low = 1.0, high = 200.0) { gap ->
            drop * amplitude * exp(-gap / lambda)
        }
        assert(deepest!!.isCloseTo(lambda * ln(drop * amplitude / 100.0), 1e-8))
    }

    @Test
    fun `gate 2 a target the field never reaches returns null and not a clamped edge`() {
        // CLAUDE.md: a root-finder handed a target the function never reaches must return null,
        // and the null is a VERDICT — here "no gap at all carries this load".
        assertNull(
            tallGapDeepestReachableGap(load = 1e9, low = 1.0, high = 200.0) { gap ->
                0.2 * SYNTHETIC_AMPLITUDE * exp(-gap / 7.85)
            }
        )
    }

    // ------------------------------------------------------------------ gate 3: conservation

    @Test
    fun `gate 3 every located branch point balances its own load exactly`() {
        val field = tallGapExponentialField(7.85)
        val path = EquilibriumPath(
            restingHeight = 20.0,
            strokeCeiling = 15.0,
            field = field
        ) { stroke -> TALL_GAP_DEVICE_B_STIFFNESS * stroke + 1.0 }
        listOf(1.0, 5.0, 10.0, 14.0).forEach { stroke ->
            val point = path.at(stroke)!!
            assert(abs(point.attraction - point.load) < 1e-6 * point.load)
        }
    }

    @Test
    fun `gate 3 the device-B fold of a pure exponential sits at one decay length`() {
        // V_eq(s) = k_c s e^{(L0-s)/lambda} with no layer, whose maximum is at s = lambda
        // exactly, whatever k_c is — an independent closed form for the tangency point.
        val lambda = 4.0
        val field = tallGapExponentialField(lambda)
        val path = EquilibriumPath(
            restingHeight = 26.0,
            strokeCeiling = 20.0,
            field = field
        ) { stroke -> TALL_GAP_DEVICE_B_STIFFNESS * stroke }
        val fold = path.fold(coarseSteps = 24, strokeTolerance = 1e-7).fold
        // CLAUDE.md: a golden-section maximum is floored by the noise of the search UNDERNEATH
        // it, not by its own bracket. With the diffuse-drop bisection at relative bracket
        // t = 1e-10, the stroke at a fold is resolvable only to about lambda*sqrt(2t) = 5.7e-5 nm
        // here, so a tolerance below that would be testing the bisection and not the fold. The
        // BIAS, being quadratic in the stroke there, is pinned far harder.
        assert(fold!!.stroke.isCloseTo(lambda, 2e-4))
        assert(
            fold.appliedBias.isCloseTo(
                TALL_GAP_DEVICE_B_STIFFNESS * lambda * exp((26.0 - lambda) / lambda) / 1e6, 1e-6
            )
        )
    }

    @Test
    fun `gate 3 the volume fraction identity is recovered from the chain it was built from`() {
        val layer = tallGapLayerCensus(
            modelName = "alexander-box(two-body)",
            height = 20.0,
            graftingDensity = 0.024
        )
        assert(
            layer.volumeFractionAtRest.isCloseTo(
                layer.monomersPerChain * layer.graftingDensity * layer.monomerVolume /
                        layer.restingHeight,
                1e-9
            )
        )
        assert(layer.restingHeight.isCloseTo(20.0, 1e-6))
    }

    // ------------------------------------------------------------------ gate 4: convergence

    @Test
    fun `gate 4 a convergence departure is emitted at two significant digits`() {
        assert(tallGapTwoSignificantDigits(3.19469867e-11).isCloseTo(3.2e-11, 1e-15))
        assert(tallGapTwoSignificantDigits(1.06411397e-9).isCloseTo(1.1e-9, 1e-15))
        assert(tallGapTwoSignificantDigits(0.0).isCloseTo(0.0, 1e-15))
    }

    @Test
    fun `gate 4 the fold is insensitive to the stroke tolerance it exits on`() {
        val field = tallGapExponentialField(4.0)
        fun foldAt(tolerance: Double): Double {
            val path = EquilibriumPath(
                restingHeight = 26.0, strokeCeiling = 20.0, field = field
            ) { stroke -> TALL_GAP_DEVICE_B_STIFFNESS * stroke }
            return path.fold(coarseSteps = 24, strokeTolerance = tolerance).fold!!.appliedBias
        }
        val fine = foldAt(1e-7)
        assert(abs(foldAt(1e-3) / fine - 1.0) < 1e-4)
        assert(abs(foldAt(1e-5) / fine - 1.0) < 1e-6)
    }

    @Test
    fun `gate 4 the coarse scan's last point cannot overshoot its own stroke ceiling`() {
        // `i * (ceiling/steps)` at `i == steps` need NOT equal `ceiling` in floating point, and
        // when it lands ABOVE it `EquilibriumPath.at` throws its own range require — which is how
        // T-192's sweep died three quarters of the way through a nine-minute run. The ceiling
        // below is the exact value that failed: 25.144662445344164, whose twelfth coarse step is
        // 25.144662445344167, three ulp out.
        val field = tallGapExponentialField(4.0)
        val ceiling = 25.144662445344164
        val path = EquilibriumPath(
            restingHeight = 26.0, strokeCeiling = ceiling, field = field
        ) { stroke -> TALL_GAP_DEVICE_B_STIFFNESS * stroke }
        // no exception, and the branch end is AT the ceiling rather than past it
        val search = path.fold(coarseSteps = 12, strokeTolerance = 1e-4)
        // the repaired scan lands EXACTLY on the ceiling, and an additive 1e-15 is below the ulp
        // at 25 nm (3.6e-15), so the slack has to be relative
        assert(search.branchEnd!!.stroke < ceiling * (1.0 + 1e-12))
        // the overshoot is real, and this is the arithmetic that makes it so
        assert(12 * (ceiling / 12) > ceiling)
    }

    // ------------------------------------------------------------------ gate 5: cross-check

    @Test
    fun `gate 5 the bulk Debye length reproduces C-0008 and doubles when the buffer is quartered`() {
        // debyeLength's first positional parameter is the TEMPERATURE and its second the relative
        // permittivity — NOT a Bjerrum length, which it computes for itself. Passing one gives
        // 0.19 nm and no unit check catches it, both being lengths of a sort.
        val atTwo = MagnesiumChlorideBuffer(2.0).debyeLength()
        assert(atTwo.isCloseTo(3.9269, 1e-4))
        val atHalf = MagnesiumChlorideBuffer(0.5).debyeLength()
        assert((atHalf / atTwo).isCloseTo(2.0, 1e-12))
        // NDI's objection, in its own units: 17-26 nm is 4.3-6.6 bulk Debye lengths at 2 mM.
        // The tolerance is the published values' OWN rounding — three significant figures is
        // +-0.005 on 2.16, i.e. 2.3e-3 — and not a number chosen because it passes.
        assert((17.0 / atTwo).isCloseTo(4.33, 3e-3))
        assert((26.0 / atTwo).isCloseTo(6.62, 3e-3))
        // and 2.2-3.3 of them at 0.5 mM, which is the whole of what the reserve buys
        assert((17.0 / atHalf).isCloseTo(2.16, 3e-3))
        assert((26.0 / atHalf).isCloseTo(3.31, 3e-3))
    }

    @Test
    fun `gate 5 the tall layer is still below its own thermal blob so no blob argument applies`() {
        val premises = tallGapScalingPremises(height = 26.0, graftingDensity = 0.024)
        // C-0002: any PEG chain below ~40 kDa is unswollen; the tall layer's chains are longer
        // than Gen-1's and still far short of the thermal blob, so the des Cloizeaux window
        // sqrt(N_K/g_T) is empty and the 9/4 exponent never starts.
        assert(premises.kuhnSegments < premises.thermalBlobKuhnSegments)
        assert(premises.desCloizeauxWindowRatio < 1.0)
        assert(
            premises.desCloizeauxWindowRatio.isCloseTo(
                sqrt(premises.kuhnSegments / premises.thermalBlobKuhnSegments), 1e-12
            )
        )
        // and coil overlap, the ONLY criterion CLAUDE.md says bounds anything, still holds
        assert(premises.coilOverlap > 1.0)
    }

    // ------------------------------------------------------------------ the closed-form field

    /**
     * `|F| = ψ e^{−h/λ}` with the diffuse drop standing in for `ψ` and no Stern series, so the
     * applied bias **is** the diffuse drop. Every quantity these tests grade against is then
     * exact in closed form, which is the only case where the library can be checked rather than
     * compared against itself.
     */
    private fun tallGapExponentialField(
        lambda: Double,
        amplitude: Double = SYNTHETIC_AMPLITUDE
    ) = DiffuseParametrisedField { gap, diffuse ->
        FieldSample(
            gap = gap,
            diffusePotential = diffuse,
            appliedBias = diffuse,
            force = -diffuse * amplitude * exp(-gap / lambda)
        )
    }

    private companion object {

        /**
         * `ψ` per volt of diffuse drop, in pN. Chosen so that every bias these tests locate
         * falls strictly inside `holdingBias`'s own `[1e−6, 0.35]` diffuse bracket — a synthetic
         * field that saturates its search bracket would be testing the bracket.
         */
        const val SYNTHETIC_AMPLITUDE: Double = 1e6
    }
}
