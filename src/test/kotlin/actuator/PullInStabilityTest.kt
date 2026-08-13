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
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.abs
import kotlin.math.exp
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-4` — the fold of the equilibrium path, located as the **maximum of the bias along it**.
 *
 * ## Why the path is parametrised by the stroke and not by the bias
 *
 * The pull-in bias is the largest bias at which a stable equilibrium exists under a given load
 * line. Sought by *scanning the bias* it is a discontinuity — the equilibrium jumps from the
 * shallow branch to near-contact — and a discontinuity is exactly what a bisection cannot find.
 * Sought by *scanning the stroke* it is a smooth maximum: at every stroke there is one bias that
 * puts the equilibrium there, `V_eq(s)`, and the fold is `max_s V_eq(s)`. Differentiating
 * `R(s) + P(L₀−s)A = |F_es(L₀−s, V(s))|` at `V'(s) = 0` gives `k_c + k_eff = 0` exactly, so the
 * argmax **is** the tangency point, and the two routes to it are independent.
 *
 * The closed form these tests are graded against: for a field `|F| = ψ e^{−h/λ}` and a linear
 * coupling `R = k_c s` against **no** layer, `V_eq(s) = k_c s e^{(L₀−s)/λ}`, whose maximum is at
 * a stroke of **exactly one decay length**, `s = λ`, whatever `k_c` is, and whose value there is
 * `k_c λ e^{(L₀−λ)/λ}`.
 */
class PullInStabilityTest {

    private val restingHeight = 10.0

    private val decayLength = 2.5

    /** `|F_es| = ψ e^{−h/λ}`, with the diffuse drop as the bias — no Stern series. */
    private fun exponentialField(lambda: Double = decayLength) =
        DiffuseParametrisedField { gap, diffuse ->
            FieldSample(
                gap = gap,
                diffusePotential = diffuse,
                appliedBias = diffuse,
                force = -diffuse * exp(-gap / lambda)
            )
        }

    private fun path(
        stiffness: Double,
        preload: Double = 0.0,
        layer: (Double) -> Double = { 0.0 },
        ceiling: Double = 9.0,
        lambda: Double = decayLength,
        bracketTolerance: Double = DEFAULT_DIFFUSE_TOLERANCE
    ) = EquilibriumPath(
        restingHeight = restingHeight,
        strokeCeiling = ceiling,
        field = exponentialField(lambda),
        diffuseCeiling = 1e12,
        bracketTolerance = bracketTolerance
    ) { stroke -> preload + stiffness * stroke + layer(restingHeight - stroke) }

    // ------------------------------------------------------------------ gate 1

    @Test
    fun `gate 1 dimensional consistency - a holding bias balances the load exactly, in pN against pN`() {
        val field = exponentialField()
        val sample = holdingBias(field, gap = 6.0, load = 25.0, diffuseCeiling = 1e12)
        assert(sample != null)
        // the bisection exits on its bracket width, so the balance is struck to that width and
        // not to machine precision — which is the honest number to assert
        assert(sample!!.attraction.isCloseTo(25.0, 1e-9))
        // and the bias it took is the closed form
        assert(sample.appliedBias.isCloseTo(25.0 * exp(6.0 / decayLength), 1e-9))
        assert(sample.force.isCloseTo(-sample.attraction, 1e-15))
    }

    @Test
    fun `gate 1 dimensional consistency - a branch point pairs a stroke with the gap it leaves, in nm`() {
        val point = path(stiffness = 10.0).at(3.0)
        assert(point != null)
        assert(point!!.stroke.isCloseTo(3.0, 1e-12))
        assert(point.gap.isCloseTo(restingHeight - 3.0, 1e-12))
        assert(point.load.isCloseTo(30.0, 1e-12))
        assert(point.attraction.isCloseTo(30.0, 1e-9))
    }

    @Test
    fun `gate 1 dimensional consistency - unphysical arguments throw rather than returning a number`() {
        val field = exponentialField()
        assertFailsWith<IllegalArgumentException> { holdingBias(field, gap = -1.0, load = 1.0) }
        assertFailsWith<IllegalArgumentException> { holdingBias(field, gap = 1.0, load = -1.0) }
        assertFailsWith<IllegalArgumentException> {
            EquilibriumPath(restingHeight = 10.0, strokeCeiling = 11.0, field = field) { 1.0 }
        }
        assertFailsWith<IllegalArgumentException> { path(stiffness = 10.0).fold(coarseSteps = 2) }
    }

    // ------------------------------------------------------------------ gate 2

    @Test
    fun `gate 2 limiting cases - the fold of an exponential field against a linear coupling is at one decay length`() {
        val search = path(stiffness = 20.0).fold()
        assert(search.fold != null)
        // the stroke is located to the search's own bracket width, 1e-4 nm …
        assert(abs(search.fold!!.stroke - decayLength) < 1e-4)
        // … and the bias, being quadratic in the stroke there, to seven orders better
        assert(
            search.fold.appliedBias.isCloseTo(
                20.0 * decayLength * exp((restingHeight - decayLength) / decayLength), 1e-9
            )
        )
        assert(!search.foldAtBranchStart)
    }

    @Test
    fun `gate 2 limiting cases - the fold stroke does not move with the coupling stiffness over a decade`() {
        val strokes = listOf(5.0, 20.0, 80.0).map { path(stiffness = it).fold().fold!!.stroke }
        strokes.forEach { assert(abs(it - decayLength) < 1e-4) }
        // and the pull-in bias is exactly proportional to the stiffness, which the closed form says
        val biases = listOf(5.0, 20.0, 80.0).map { path(stiffness = it).fold().fold!!.appliedBias }
        assert((biases[1] / biases[0]).isCloseTo(4.0, 1e-9))
        assert((biases[2] / biases[1]).isCloseTo(4.0, 1e-9))
    }

    @Test
    fun `gate 2 limiting cases - the fold stroke follows the decay length, which is what sets it`() {
        listOf(1.0, 2.5, 4.0).forEach { lambda ->
            val search = path(stiffness = 20.0, lambda = lambda).fold()
            assert(abs(search.fold!!.stroke - lambda) < 1e-4)
        }
    }

    @Test
    fun `gate 2 limiting cases - a dead load against a decaying field is unstable from the branch start`() {
        val search = path(stiffness = 0.0, preload = 100.0).fold()
        assert(search.foldAtBranchStart)
        // the ceiling is then the bias that delivers the dead load at zero stroke — the blocking bias
        assert(search.fold!!.stroke.isCloseTo(0.0, 1e-12))
        assert(search.fold.appliedBias.isCloseTo(100.0 * exp(restingHeight / decayLength), 1e-9))
    }

    @Test
    fun `gate 2 limiting cases - a fold beyond the stroke ceiling is reported as no fold, with the branch end quoted`() {
        val search = path(stiffness = 20.0, ceiling = 1.5).fold()
        assert(search.fold == null)
        assert(search.branchEnd != null)
        assert(search.branchEnd!!.stroke.isCloseTo(1.5, 1e-9))
    }

    @Test
    fun `gate 2 limiting cases - a load the field cannot reach ends the branch, and that is the osmotic wall`() {
        // a layer whose load diverges at 6 nm of stroke, against a field capped by its own ceiling
        val search = EquilibriumPath(
            restingHeight = restingHeight,
            strokeCeiling = 9.0,
            field = exponentialField(),
            diffuseCeiling = 1e6
        ) { stroke -> 10.0 / (6.0 - stroke).coerceAtLeast(1e-9) }.fold()
        assert(search.branchEnd != null)
        assert(search.branchEnd!!.stroke < 6.0)
        assert(search.reachedDiffuseCeiling)
    }

    @Test
    fun `gate 2 limiting cases - a load beyond what the field can ever supply has no holding bias`() {
        val field = exponentialField()
        assert(holdingBias(field, gap = 6.0, load = 1e9, diffuseCeiling = 0.35) == null)
    }

    // ------------------------------------------------------------------ gate 3

    @Test
    fun `gate 3 symmetry - at the located fold the coupled tangent vanishes, k_c plus k_eff is zero`() {
        val stiffness = 20.0
        // The tangency residual is FIRST ORDER in the located stroke, and near the maximum the
        // bias is flat, so what floors the located stroke is not the golden-section bracket but
        // the noise of the bias bisection underneath it: at a relative bracket `t` the stroke is
        // resolvable only to about `λ√(2t)`. Both brackets are therefore tightened here, and the
        // residual falls with them — which is the statement that the identity is exact.
        val fold = path(stiffness = stiffness, bracketTolerance = 1e-15)
            .fold(strokeTolerance = 1e-7).fold!!
        // k_es by central difference of the field at the fold's own applied bias, independent of
        // the path that located it
        val field = exponentialField()
        val delta = 1e-5
        val above = field.sample(fold.gap + delta, fold.appliedBias).force
        val below = field.sample(fold.gap - delta, fold.appliedBias).force
        val electrostatic = -(above - below) / (2.0 * delta)
        assert(abs(stiffness + electrostatic) < 1e-5 * stiffness)
        // and the analytic value: k_es = -|F|/λ with |F| = k_c λ at the fold
        assert(electrostatic.isCloseTo(-stiffness, 1e-5))
        // the loose search — the study's own settings — is an order worse, and no better than
        // its own bracket noise allows
        val loose = path(stiffness = stiffness).fold().fold!!
        val looseStiffness = -(field.sample(loose.gap + delta, loose.appliedBias).force -
                field.sample(loose.gap - delta, loose.appliedBias).force) / (2.0 * delta)
        assert(abs(stiffness + looseStiffness) < 1e-4 * stiffness)
    }

    @Test
    fun `gate 3 symmetry - the binding ceiling is the smallest one, and ties break on order`() {
        val ceilings = listOf(
            BiasCeiling("pull-in", 0.42),
            BiasCeiling("correlation band", 0.19),
            BiasCeiling("point ions", 1.0),
            BiasCeiling("concentrated crossover", null)
        )
        val binding = bindingCeiling(ceilings)
        assert(binding != null)
        assert(binding.name == "correlation band")
        assert(binding.bias!!.isCloseTo(0.19, 1e-12))
        // a tie is broken by the order the ceilings are declared in, so the file is reproducible
        val tied = bindingCeiling(listOf(BiasCeiling("a", 0.5), BiasCeiling("b", 0.5)))
        assert(tied!!.name == "a")
        assert(bindingCeiling(listOf(BiasCeiling("a", null))) == null)
    }

    @Test
    fun `gate 3 symmetry - the fold's own bias is the largest along the whole branch`() {
        val branch = path(stiffness = 20.0)
        val fold = branch.fold().fold!!
        (1..60).forEach { i ->
            val point = branch.at(i * 9.0 / 61.0)
            if (point != null) assert(point.appliedBias <= fold.appliedBias * (1.0 + 1e-9))
        }
    }

    // ------------------------------------------------------------------ gate 4

    @Test
    fun `gate 4 convergence - the located fold is independent of the coarse scan and of the tolerance`() {
        val branch = path(stiffness = 20.0)
        val reference = branch.fold(coarseSteps = 48, strokeTolerance = 1e-9)
        listOf(8, 12, 24).forEach { steps ->
            val search = branch.fold(coarseSteps = steps)
            assert(search.fold!!.appliedBias.isCloseTo(reference.fold!!.appliedBias, 1e-7))
        }
        listOf(1e-3, 1e-4, 1e-6).forEach { tolerance ->
            val search = branch.fold(strokeTolerance = tolerance)
            assert(search.fold!!.appliedBias.isCloseTo(reference.fold!!.appliedBias, 1e-6))
        }
    }

    @Test
    fun `gate 4 convergence - the golden-section search exits on the bracket width, never on a residual`() {
        val branch = path(stiffness = 20.0)
        val search = branch.fold(coarseSteps = 12, strokeTolerance = 1e-6)
        assert(search.bracketWidth <= 1e-6)
        assert(search.evaluations < 60)
    }

    // ------------------------------------------------------------------ the small-gap diagnostics

    @Test
    fun `gate 2 limiting cases - the force maximum of a non-monotone attraction is located, and a monotone one has none`() {
        // an attraction that rises as the gap closes, peaks at 1.2 nm and falls below it
        val peak = forceMaximumGap(low = 0.4, high = 6.0) { gap ->
            exp(-gap / 2.0) * (1.0 - exp(-(gap - 0.2) / 0.5))
        }
        assert(peak != null)
        assert(peak > 0.4 && peak < 6.0)
        // the derivative vanishes there
        val delta = 1e-6
        fun f(gap: Double) = exp(-gap / 2.0) * (1.0 - exp(-(gap - 0.2) / 0.5))
        assert(abs(f(peak + delta) - f(peak - delta)) / (2.0 * delta) < 1e-5)
        // a strictly decaying attraction has its maximum at the low end, which is not a maximum
        assert(forceMaximumGap(low = 0.4, high = 6.0) { exp(-it / 2.0) } == null)
    }

    @Test
    fun `gate 2 limiting cases - the repulsion onset is the gap where the signed force changes sign`() {
        // a Maxwell attraction on 2 nm against a confined-counterion repulsion on 0.4 nm: the
        // shorter-ranged term wins at small gap, which is the mechanism `C-0008` reports at V = 0
        fun signed(gap: Double) = -(exp(-gap / 2.0) - 3.0 * exp(-gap / 0.4))
        val onset = repulsionOnsetGap(low = 0.2, high = 6.0) { signed(it) }
        assert(onset != null)
        // below it the force is repulsive and above it attractive, which is the whole statement
        assert(signed(onset - 0.05) > 0.0)
        assert(signed(onset + 0.05) < 0.0)
        assert(repulsionOnsetGap(low = 0.2, high = 6.0) { -exp(-it / 2.0) } == null)
    }
}
