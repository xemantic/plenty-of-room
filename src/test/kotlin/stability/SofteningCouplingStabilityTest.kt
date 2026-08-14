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

package com.xemantic.nano.plentyofroom.stability

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.actuator.DiffuseParametrisedField
import com.xemantic.nano.plentyofroom.actuator.EquilibriumPath
import com.xemantic.nano.plentyofroom.actuator.FieldSample
import com.xemantic.nano.plentyofroom.anchoring.FlexureOrientation
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.abs
import kotlin.math.exp
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-76` — does a strain-**softening** coupling still satisfy `C-0017`'s stability condition?
 *
 * Every test is named for the verification gate it discharges, per §5 of the problem definition.
 *
 * The spine of the task is that `C-0017`'s theorem — *placement on the secant, stability on the
 * tangent* — has a **sign**, and `C-0030` has flipped it: the assembled tangent of the coupled
 * flexure falls **below** its placed secant and is not even monotone in the stroke. So the
 * stability condition must be read on `min_s k_tangent(s)` rather than at the working point, and
 * the pull-in fold must be re-located under a load line that is **nonlinear in two ways at once** —
 * a different slope *and* a different level away from the placement point.
 *
 * The synthetic field below is a one-line exponential whose fold obeys `k_c(s) = R(s)/λ` in closed
 * form, which makes the tangency identity an assertion about the solver rather than a restatement
 * of it. The real Poisson-Boltzmann field is the study's business, not this suite's: a fold search
 * is ~1700 solves and belongs in `tools/study.sh`.
 */
class SofteningCouplingStabilityTest {

    private val favourable = gen1CouplingLine(
        name = "coupled favourable", coupled = true,
        orientation = FlexureOrientation.FAVOURABLE
    )

    private val adverse = gen1CouplingLine(
        name = "coupled adverse", coupled = true,
        orientation = FlexureOrientation.ADVERSE
    )

    private val decoupled = gen1CouplingLine(
        name = "decoupled", coupled = false,
        orientation = FlexureOrientation.FAVOURABLE
    )

    private val mandate = AffineLoadLine("linear mandate", GEN1_MANDATE_STIFFNESS)

    // ---------------------------------------------------------------- gate 1 — dimensional

    @Test
    fun `gate 1 dimensional consistency - a reaction over a stroke IS the secant, identically`() {
        listOf(0.5, 1.0, 3.0, 7.0, 10.0).forEach { stroke ->
            listOf(mandate, favourable, adverse, decoupled).forEach { line ->
                assert(line.secant(stroke).isCloseTo(line.reaction(stroke) / stroke))
            }
        }
    }

    @Test
    fun `gate 1 dimensional consistency - the path count multiplies reaction secant and tangent alike`() {
        val single = gen1CouplingLine(
            name = "one path", coupled = true,
            orientation = FlexureOrientation.FAVOURABLE, count = 45
        )
        val doubled = single.withCount(90)
        listOf(1.0, 3.0, 8.0).forEach { stroke ->
            assert((doubled.reaction(stroke) / single.reaction(stroke)).isCloseTo(2.0))
            assert((doubled.secant(stroke) / single.secant(stroke)).isCloseTo(2.0))
            assert((doubled.tangent(stroke) / single.tangent(stroke)).isCloseTo(2.0))
        }
    }

    @Test
    fun `gate 1 dimensional consistency - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { favourable.secant(0.0) }
        assertFailsWith<IllegalArgumentException> { favourable.reaction(-1.0) }
        assertFailsWith<IllegalArgumentException> { AffineLoadLine("negative", -1.0) }
        assertFailsWith<IllegalArgumentException> { favourable.withCount(0) }
        assertFailsWith<IllegalArgumentException> {
            favourable.tangentMinimum(low = 3.0, high = 1.0)
        }
    }

    // ---------------------------------------------------------------- gate 2 — limiting cases

    @Test
    fun `gate 2 limiting cases - an affine line has secant equal to tangent equal to its slope everywhere`() {
        listOf(0.1, 3.0, 10.0).forEach { stroke ->
            assert(mandate.tangent(stroke).isCloseTo(GEN1_MANDATE_STIFFNESS))
            assert(mandate.secant(stroke).isCloseTo(GEN1_MANDATE_STIFFNESS))
        }
        val minimum = mandate.tangentMinimum(low = 0.0, high = 10.0)
        assert(minimum.stiffness.isCloseTo(GEN1_MANDATE_STIFFNESS))
        assert(!minimum.interior)
    }

    @Test
    fun `gate 2 limiting cases - a preloaded affine line is NOT its own secant`() {
        val preloaded = AffineLoadLine("preloaded", 20.0, preload = 40.0)
        assert(preloaded.tangent(3.0).isCloseTo(20.0))
        assert(preloaded.secant(3.0).isCloseTo(20.0 + 40.0 / 3.0))
    }

    @Test
    fun `gate 2 limiting cases - every line is PLACED, so all four deliver 100 pN at 3 nm`() {
        listOf(mandate, favourable, adverse, decoupled).forEach { line ->
            assert(line.secant(GEN1_ACCEPTABLE_STROKE).isCloseTo(GEN1_MANDATE_STIFFNESS, 1e-6))
            assert(line.reaction(GEN1_ACCEPTABLE_STROKE).isCloseTo(100.0, 1e-6))
        }
    }

    @Test
    fun `gate 2 limiting cases - the SIGN of CH-0042's debt, asserted on the same design`() {
        // the decoupled element strain-STIFFENS, which is the premise C-0017's theorem was
        // stated under; the coupled favourable one strain-SOFTENS, which is CH-0042
        assert(decoupled.tangentToSecant(GEN1_ACCEPTABLE_STROKE) > 1.0)
        assert(favourable.tangentToSecant(GEN1_ACCEPTABLE_STROKE) < 1.0)
        assert(adverse.tangentToSecant(GEN1_ACCEPTABLE_STROKE) > 1.0)
    }

    @Test
    fun `gate 2 limiting cases - the softening line's tangent minimum is INTERIOR and the stiffening one's is not`() {
        val softening = favourable.tangentMinimum(low = 0.0, high = GEN1_DESIRED_STROKE)
        assert(softening.interior)
        assert(softening.stroke > 0.0 && softening.stroke < GEN1_DESIRED_STROKE)
        val stiffening = decoupled.tangentMinimum(low = 0.0, high = GEN1_DESIRED_STROKE)
        assert(!stiffening.interior)
        assert(stiffening.stroke.isCloseTo(0.0, 1e-6))
    }

    @Test
    fun `gate 2 limiting cases - a dead load has no interior fold and folds at the branch start`() {
        val path = syntheticPath(AffineLoadLine("dead", 0.0, preload = 40.0))
        val search = path.fold()
        assert(search.foldAtBranchStart)
    }

    @Test
    fun `gate 2 limiting cases - an affine line through the origin folds at the field's own decay length`() {
        // on the synthetic field the fold condition R'(s)/R(s) = 1/lambda gives s = lambda for
        // any line through the origin, INDEPENDENT of its stiffness
        listOf(5.0, 33.3333, 300.0).forEach { stiffness ->
            val search = syntheticPath(AffineLoadLine("linear", stiffness)).fold()
            assert(search.fold!!.stroke.isCloseTo(SYNTHETIC_DECAY, 1e-3))
        }
    }

    // ------------------------------------------------- gate 3 — symmetry and conservation

    @Test
    fun `gate 3 conservation - the tangency identity holds at the fold of a NONLINEAR load line`() {
        // On the synthetic field k_es = -attraction/lambda and k_brush = 0, so the coupled
        // tangent at the fold is k_c(s) - R(s)/lambda. The path search never sees that
        // expression: it maximises V_eq(s). The two routes share no code.
        listOf(favourable, adverse, decoupled).forEach { line ->
            val fold = syntheticPath(line).fold().fold
            assert(fold != null)
            val stroke = fold!!.stroke
            val residual = line.tangent(stroke) - line.reaction(stroke) / SYNTHETIC_DECAY
            val scale = line.tangent(stroke) + line.reaction(stroke) / SYNTHETIC_DECAY
            assert(abs(residual) / scale < 1e-4)
        }
    }

    @Test
    fun `gate 3 conservation - PLACEMENT is an identity, so all four lines locate the same operating bias`() {
        // every line delivers exactly 100 pN at 3 nm, so the bias that holds the tile there is the
        // same number for all four — which is what makes a state-by-state comparison of their
        // FOLDS a comparison of one device rather than of four
        val biases = listOf(mandate, favourable, adverse, decoupled).map {
            syntheticPath(it).at(GEN1_ACCEPTABLE_STROKE)!!.appliedBias
        }
        biases.forEach { assert(it.isCloseTo(biases.first(), 1e-9)) }
    }

    @Test
    fun `gate 3 conservation - the SHAPE of the load line moves the fold, not only its slope`() {
        // the mandate folds at exactly the field's decay length whatever its stiffness (gate 2),
        // so any departure from that stroke is the nonlinearity and nothing else
        val softening = syntheticPath(favourable).fold().fold!!
        val stiffening = syntheticPath(adverse).fold().fold!!
        assert(abs(softening.stroke - SYNTHETIC_DECAY) > 1e-2)
        assert(abs(stiffening.stroke - SYNTHETIC_DECAY) > 1e-2)
        // and they move in OPPOSITE directions, which is the sign of CH-0042's debt again
        assert((softening.stroke - SYNTHETIC_DECAY) * (stiffening.stroke - SYNTHETIC_DECAY) < 0.0)
    }

    @Test
    fun `gate 3 conservation - a stability margin is null where there is no requirement`() {
        assert(stabilityMargin(couplingTangent = 25.0, effectiveStiffness = +4.0) == null)
        assert(stabilityMargin(couplingTangent = 25.0, effectiveStiffness = 0.0) == null)
        assert(stabilityMargin(couplingTangent = 25.0, effectiveStiffness = -20.0)!!.isCloseTo(1.25))
    }

    // ---------------------------------------------------------------- gate 4 — convergence

    @Test
    fun `gate 4 numerical convergence - the tangent minimum is scan-step and bracket independent`() {
        val finest = favourable.tangentMinimum(
            low = 0.0, high = GEN1_DESIRED_STROKE, coarseSteps = 4096, tolerance = 1e-10
        )
        listOf(64, 256, 1024).forEach { steps ->
            val coarse = favourable.tangentMinimum(
                low = 0.0, high = GEN1_DESIRED_STROKE, coarseSteps = steps, tolerance = 1e-8
            )
            assert(coarse.stiffness.isCloseTo(finest.stiffness, 1e-8))
            assert(abs(coarse.stroke - finest.stroke) < 1e-4)
        }
    }

    @Test
    fun `gate 4 numerical convergence - the located fold is coarse-scan independent on the synthetic field`() {
        val finest = syntheticPath(favourable).fold(coarseSteps = 48, strokeTolerance = 1e-8)
        listOf(8, 12, 24).forEach { steps ->
            val coarse = syntheticPath(favourable).fold(coarseSteps = steps, strokeTolerance = 1e-6)
            assert(coarse.fold!!.appliedBias.isCloseTo(finest.fold!!.appliedBias, 1e-6))
        }
    }

    // ---------------------------------------------- gate 5 — upstream cross-check

    @Test
    fun `gate 5 upstream cross-check - C-0030's recommended design reproduced`() {
        assert(favourable.span.isCloseTo(31.82, 1e-3))
        assert(favourable.tangent(GEN1_ACCEPTABLE_STROKE).isCloseTo(25.23, 1e-3))
        assert(favourable.tangentToSecant(GEN1_ACCEPTABLE_STROKE).isCloseTo(0.757, 2e-3))
        assert(favourable.secant(GEN1_DESIRED_STROKE).isCloseTo(29.81, 1e-3))
        assert(favourable.reaction(GEN1_DESIRED_STROKE).isCloseTo(298.0, 2e-3))
        val minimum = favourable.tangentMinimum(low = 0.0, high = GEN1_DESIRED_STROKE)
        assert(minimum.stiffness.isCloseTo(22.88, 1e-3))
        assert(minimum.stroke.isCloseTo(4.55, 5e-3))
    }

    @Test
    fun `gate 5 upstream cross-check - C-0028's decoupled design and C-0030's adverse mounting reproduced`() {
        assert(decoupled.span.isCloseTo(31.06, 1e-3))
        assert(decoupled.tangent(GEN1_ACCEPTABLE_STROKE).isCloseTo(36.51, 1e-3))
        assert(decoupled.tangentToSecant(GEN1_ACCEPTABLE_STROKE).isCloseTo(1.095, 2e-3))
        assert(adverse.span.isCloseTo(40.14, 1e-3))
        assert(adverse.tangent(GEN1_ACCEPTABLE_STROKE).isCloseTo(44.82, 1e-3))
    }

    @Test
    fun `gate 5 upstream cross-check - the cheap bound, asserted as a test`() {
        // C-0017's stability floor |k_eff(3 nm)| at the 10 nm design point: 23.41-27.91 at 2 mM
        // and 3.86-15.94 at 0.5 mM. The cheap bound this task ran before the sweep.
        val minimum = favourable.tangentMinimum(low = 0.0, high = GEN1_DESIRED_STROKE).stiffness
        assert(minimum < 23.41)
        assert(minimum > 15.94)
    }

    // ---------------------------------------------------------------- the synthetic field

    private fun syntheticPath(line: StrokeLoadLine): EquilibriumPath = EquilibriumPath(
        restingHeight = SYNTHETIC_RESTING,
        strokeCeiling = SYNTHETIC_RESTING - 0.5,
        field = SYNTHETIC_FIELD
    ) { stroke -> line.reaction(stroke) }

    private companion object {

        const val SYNTHETIC_DECAY = 3.0

        const val SYNTHETIC_RESTING = 10.0

        /**
         * `|F_es| = A ψ² exp(−h/λ)`, with the applied bias taken equal to the diffuse drop.
         *
         * Its equilibrium path is `V_eq(s) = √(R(s) e^{(L₀−s)/λ}/A)`, whose stationary point is
         * `R'(s)/R(s) = 1/λ` — i.e. `k_c + k_es = 0` with `k_es = −|F|/λ`. Closed form, so the
         * fold search can be graded rather than described.
         */
        val SYNTHETIC_FIELD = DiffuseParametrisedField { gap, psi ->
            val attraction = 4.0e5 * psi * psi * exp(-gap / SYNTHETIC_DECAY)
            FieldSample(gap, psi, psi, -attraction)
        }
    }
}
