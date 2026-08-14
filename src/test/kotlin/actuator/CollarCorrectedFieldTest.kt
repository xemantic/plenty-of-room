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
import kotlin.math.ln
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-60` gate tests for the collar-corrected field and for the decomposition it exists to
 * measure — `CH-0035`'s claim that a multiplier on the **level** of `|F_es|` is absorbed
 * entirely into the bias at a force-pinned operating point, and that only `d ln μ/dh`
 * survives into `k_es`.
 *
 * The analytic field used below is deliberately a **caricature** —
 * `|F| = F₀ (V/V₀)² e^{−h/ℓ}` — chosen because every quantity the actuator reads off it
 * (the holding bias, the fold, `k_es`, the decay length) has a closed form, so the
 * decomposition can be asserted against arithmetic rather than against another solve.
 */
class CollarCorrectedFieldTest {

    /** `|F| = amplitude · ψ² · exp(−gap/decay)`, with the applied bias `ψ + ψ/4`. */
    private fun analyticField(
        amplitude: Double = 1.0e5,
        decay: Double = 3.0
    ) = DiffuseParametrisedField { gap, psi ->
        FieldSample(
            gap = gap,
            diffusePotential = psi,
            appliedBias = 1.25 * psi,
            force = -amplitude * psi * psi * exp(-gap / decay)
        )
    }

    // gate 1 — dimensional consistency and the plain algebra of a multiplier

    @Test
    fun `gate 1 should scale only the force and leave every potential untouched`() {
        val base = analyticField()
        val corrected = base.withCollar { 1.0 + 0.02 * it }
        val plain = base.sample(7.0, 0.1)
        val scaled = corrected.sample(7.0, 0.1)
        assert(scaled.gap == plain.gap)
        assert(scaled.diffusePotential == plain.diffusePotential)
        assert(scaled.appliedBias == plain.appliedBias)
        assert(scaled.attraction.isCloseTo(1.14 * plain.attraction, 1e-12))
    }

    @Test
    fun `gate 2 should make a unit multiplier the identity, sample for sample`() {
        // The whole method rests on this: the corrected field with mu = 1 must BE the
        // uncorrected field, so that the mu = 1 variant of the study reproduces C-0018.
        val base = analyticField()
        val corrected = base.withCollar { 1.0 }
        listOf(2.0, 5.0, 7.0, 10.0).forEach { gap ->
            listOf(0.01, 0.05, 0.2).forEach { psi ->
                val plain = base.sample(gap, psi)
                val same = corrected.sample(gap, psi)
                assert(same.force == plain.force)
                assert(same.appliedBias == plain.appliedBias)
            }
        }
    }

    @Test
    fun `gate 2 should reject a non-positive multiplier rather than flip the force`() {
        val base = analyticField()
        assertFailsWith<IllegalArgumentException> { base.withCollar { 0.0 }.sample(7.0, 0.1) }
        assertFailsWith<IllegalArgumentException> { base.withCollar { -1.1 }.sample(7.0, 0.1) }
    }

    // gate 3 — the decomposition: level cancels at a pinned force, gradient does not

    @Test
    fun `gate 3 should absorb a CONSTANT multiplier entirely into the holding bias`() {
        // At a pinned load the balance fixes |F_es|; a constant multiplier changes only the
        // bias that delivers it. Asserted on the caricature, where the bias moves by
        // exactly 1/sqrt(mu) because the force is quadratic in the drop.
        val base = analyticField()
        val mu = 1.147
        val corrected = base.withCollar { mu }
        val load = 100.0
        val plain = holdingBias(base, 7.0, load)!!
        val scaled = holdingBias(corrected, 7.0, load)!!
        assert(plain.attraction.isCloseTo(load, 1e-6))
        assert(scaled.attraction.isCloseTo(load, 1e-6))
        assert(
            (scaled.diffusePotential / plain.diffusePotential)
                .isCloseTo(1.0 / kotlin.math.sqrt(mu), 1e-6)
        )
    }

    @Test
    fun `gate 3 should leave k_es untouched by a constant multiplier at a pinned force`() {
        // CH-0035's identity, made executable: k_es = -|F|/l, |F| pinned, so a constant mu
        // reaches k_es not at all. Here `l` is exactly the caricature's decay length at
        // every bias, so the pinned k_es must be identical.
        val decay = 3.0
        val base = analyticField(decay = decay)
        val load = 100.0
        val plainStiffness = pinnedElectrostaticStiffness(base, 7.0, load)
        val scaledStiffness = pinnedElectrostaticStiffness(base.withCollar { 1.147 }, 7.0, load)
        assert(plainStiffness.isCloseTo(-load / decay, 1e-4))
        assert(scaledStiffness.isCloseTo(plainStiffness, 1e-9))
    }

    @Test
    fun `gate 3 should let only the GRADIENT of the multiplier reach k_es`() {
        // With mu = exp(g h) the corrected force decays as exp(-h(1/l - g)), so the pinned
        // stiffness must be exactly -load(1/l - g): the gradient LENGTHENS the decay and
        // REDUCES |k_es|, which is the direction CH-0035 asserts and CH-0026 gets backwards.
        val decay = 3.0
        val gradient = 0.018
        val base = analyticField(decay = decay)
        val load = 100.0
        val corrected = base.withCollar { exp(gradient * it) }
        val stiffness = pinnedElectrostaticStiffness(corrected, 7.0, load)
        assert(stiffness.isCloseTo(-load * (1.0 / decay - gradient), 1e-4))
        assert(stiffness > pinnedElectrostaticStiffness(base, 7.0, load))
        assert(abs(stiffness) < load / decay)
    }

    @Test
    fun `gate 4 should separate the level and the gradient at the fold of an equilibrium path`() {
        // The whole three-variant construction of the T-60 study, asserted on the
        // caricature where the fold has a closed form. `load'/load = 1/l - g` at the fold,
        // so a CONSTANT multiplier leaves the fold STROKE exactly where it was and lowers
        // the whole path by 1/sqrt(mu); the GRADIENT moves the stroke and RAISES the bias.
        val base = analyticField()
        val resting = 10.0
        val gradient = 0.018
        val level = 1.147
        val line = { s: Double -> 33.333 * s + 4.0 * s * s }
        fun fold(field: DiffuseParametrisedField): BranchPoint =
            EquilibriumPath(resting, 9.0, field, load = line).fold().fold!!
        val plain = fold(base)
        val levelOnly = fold(base.withCollar { level })
        val full = fold(base.withCollar { level * exp(gradient * (it - resting)) })
        // the closed-form fold strokes: 4s^2 + 9.3333s - 100 = 0 without the gradient
        assert(plain.stroke.isCloseTo(3.9679, 1e-3))
        assert(levelOnly.stroke.isCloseTo(plain.stroke, 1e-3))
        assert((levelOnly.appliedBias / plain.appliedBias).isCloseTo(1.0 / kotlin.math.sqrt(level), 1e-3))
        // the gradient lengthens the decay, so the fold moves DEEPER and the pull-in bias
        // goes UP — the favourable direction, and the one CH-0026 predicts backwards
        assert(full.stroke > levelOnly.stroke)
        assert(full.appliedBias > levelOnly.appliedBias)
        assert(abs(full.appliedBias / levelOnly.appliedBias - 1.0) > 0.01)
    }

    /** `k_es` at a **pinned** load: the field is re-solved at the bias that holds it there. */
    private fun pinnedElectrostaticStiffness(
        field: DiffuseParametrisedField,
        gap: Double,
        load: Double,
        delta: Double = 1e-3
    ): Double {
        val psi = holdingBias(field, gap, load)!!.diffusePotential
        val above = field.sample(gap + delta, psi).attraction
        val below = field.sample(gap - delta, psi).attraction
        return load * (ln(above) - ln(below)) / (2.0 * delta)
    }
}
