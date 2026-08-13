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
 * The interpolant that lets a force balance call `F_es(h)` thousands of times without
 * re-solving Poisson-Boltzmann each time.
 *
 * The construction is chosen so that its own error is checkable rather than assumed:
 * shape-preserving cubic Hermite on `ln|F_es|` against `h` is **exact** — value and
 * derivative — whenever the force is a pure exponential, which is the one case where the
 * answer is known in closed form and the one `C-0008` reports the force approaching in the
 * far field.
 */
class ElectrostaticForceCurveTest {

    private val gaps = DoubleArray(41) { 1.0 + it * 0.25 }

    /** `F_z = −F0 exp(−h/ell)`, negative meaning toward the electrode. */
    private fun exponential(amplitude: Double, decayLength: Double) =
        electrostaticForceCurve(gaps) { -amplitude * exp(-it / decayLength) }

    @Test
    fun `gate 2 limiting cases - a pure exponential force should be reproduced exactly, because ln F is then linear in h`() {
        val curve = exponential(900.0, 2.4)
        listOf(1.0, 1.13, 2.7, 5.0, 6.66, 9.0, 11.0).forEach { gap ->
            assert(curve.forceAt(gap).isCloseTo(-900.0 * exp(-gap / 2.4), 1e-12))
        }
    }

    @Test
    fun `gate 2 limiting cases - the decay length of a pure exponential should come back as its own decay length`() {
        val curve = exponential(900.0, 2.4)
        listOf(1.5, 3.0, 5.0, 7.0, 10.0).forEach { gap ->
            assert(curve.decayLengthAt(gap).isCloseTo(2.4, 1e-10))
        }
    }

    @Test
    fun `gate 3 symmetry and conservation - the identity ell = F_es over k_es should hold at every gap`() {
        // C-0008 defines ell = -1/(d ln|F_es|/dh) = F_es/k_es. Both are computed here from the
        // same interpolant, so the identity is a check that the two readings agree, not a tautology
        // of one formula: the force comes from the Hermite value, the stiffness from its derivative.
        val curve = electrostaticForceCurve(gaps) { -50.0 * exp(-it / 3.0) - 800.0 * exp(-it / 1.6) }
        listOf(1.4, 2.0, 4.0, 6.5, 9.5).forEach { gap ->
            val ratio = curve.forceAt(gap) / curve.stiffnessAt(gap)
            assert(ratio.isCloseTo(curve.decayLengthAt(gap), 1e-12))
            assert(ratio > 0.0)
        }
    }

    @Test
    fun `gate 3 symmetry and conservation - k_es must be negative everywhere, as section 1 of the problem definition requires`() {
        val curve = electrostaticForceCurve(gaps) { -50.0 * exp(-it / 3.0) - 800.0 * exp(-it / 1.6) }
        listOf(1.1, 2.0, 3.3, 5.0, 8.0, 10.9).forEach { gap ->
            assert(curve.stiffnessAt(gap) < 0.0)
            assert(curve.forceAt(gap) < 0.0)
            assert(curve.decayLengthAt(gap) > 0.0)
        }
    }

    @Test
    fun `gate 3 symmetry and conservation - past a force maximum k_es must change sign, and the interpolant must not hide it`() {
        // the real F_es(h) is non-monotone at small separation: it rises to a maximum and then
        // falls toward the sign change. Above the maximum k_es < 0 (§1's softening); below it
        // k_es > 0 and the electrostatics STIFFENS the layer. C-0008's "k_es < 0 everywhere" is
        // true everywhere it looked — its smallest gap is 3 nm — and this is where it stops being.
        // |F| = 900 e^(-h/2.4)(1 - e^(-h/1.5)), which peaks at h = 1.5 ln(2.6) = 1.433 nm —
        // inside the sampled range, and the same shape the solved F_es(h) has near its stopper
        val peaked = electrostaticForceCurve(gaps) { -900.0 * exp(-it / 2.4) * (1.0 - exp(-it / 1.5)) }
        assert(peaked.stiffnessAt(1.2) > 0.0)
        assert(peaked.decayLengthAt(1.2) < 0.0)
        assert(peaked.stiffnessAt(8.0) < 0.0)
        assert(peaked.decayLengthAt(8.0) > 0.0)
        // and the identity survives the sign change, because both readings come from one interpolant
        listOf(1.2, 8.0).forEach { gap ->
            assert(
                (peaked.forceAt(gap) / peaked.stiffnessAt(gap)).isCloseTo(
                    peaked.decayLengthAt(gap), 1e-12
                )
            )
        }
    }

    @Test
    fun `gate 2 limiting cases - the interpolant must be monotone where the samples are, with no overshoot`() {
        // a shape-preserving scheme, not a natural spline: |F_es| is monotone in the gap and an
        // interpolant that overshoots would invent a non-monotone force and with it a spurious
        // sign change in k_es
        val curve = electrostaticForceCurve(gaps) { -900.0 * exp(-it / 2.4) - 3.0 }
        var previous = curve.magnitudeAt(1.0)
        var gap = 1.0
        while (gap <= 11.0) {
            val current = curve.magnitudeAt(gap)
            assert(current <= previous + 1e-12)
            previous = current
            gap += 0.01
        }
    }

    @Test
    fun `gate 4 numerical convergence - halving the sample spacing should not move the interpolated force`() {
        val coarse = electrostaticForceCurve(DoubleArray(21) { 1.0 + it * 0.5 }) { field(it) }
        val fine = electrostaticForceCurve(DoubleArray(41) { 1.0 + it * 0.25 }) { field(it) }
        val finer = electrostaticForceCurve(DoubleArray(81) { 1.0 + it * 0.125 }) { field(it) }
        // stated as an ORDER over the whole range, not pointwise: at 1e-9 the pointwise
        // comparison is measuring double-precision roundoff rather than the interpolation
        val probes = listOf(1.7, 3.3, 5.0, 6.1, 8.8, 10.4)
        fun worst(curve: ElectrostaticForceCurve) =
            probes.maxOf { abs(curve.forceAt(it) / field(it) - 1.0) }
        val coarsest = worst(coarse)
        val middle = worst(fine)
        val finest = worst(finer)
        assert(middle < 0.2 * coarsest)
        assert(finest < 0.2 * middle)
        assert(finest < 1e-6)
    }

    @Test
    fun `gate 1 dimensional consistency - a curve should refuse gaps outside the sampled range and refuse bad samples`() {
        val curve = exponential(900.0, 2.4)
        assertFailsWith<IllegalArgumentException> { curve.forceAt(0.5) }
        assertFailsWith<IllegalArgumentException> { curve.forceAt(11.5) }
        assertFailsWith<IllegalArgumentException> {
            electrostaticForceCurve(doubleArrayOf(1.0, 2.0)) { 0.0 }
        }
        assertFailsWith<IllegalArgumentException> {
            electrostaticForceCurve(doubleArrayOf(3.0, 2.0, 1.0)) { -1.0 }
        }
    }

    @Test
    fun `gate 2 limiting cases - a force that turns repulsive at small gap should be trimmed, and the stopper reported`() {
        // C-0008 reports the V = 0 force changing sign between 4 and 5 nm; the same mechanism
        // operates under bias at small enough separation, and the gap where it happens is an
        // electrostatic STOPPER — a physical result, not a sampling artefact
        val sampling = attractiveForceCurve(gaps) {
            -900.0 * exp(-it / 2.4) + 5000.0 * exp(-it / 0.6)
        }
        assert(sampling.repulsiveBelow != null)
        assert(sampling.curve.minimumGap > sampling.repulsiveBelow!!)
        assert(sampling.curve.forceAt(sampling.curve.minimumGap) < 0.0)
        // and a curve that is attractive throughout reports no stopper and is trimmed nowhere
        val clean = attractiveForceCurve(gaps) { -900.0 * exp(-it / 2.4) }
        assert(clean.repulsiveBelow == null)
        assert(clean.curve.minimumGap == gaps[0])
    }

    @Test
    fun `gate 1 dimensional consistency - a curve with too few attractive samples should be rejected`() {
        assertFailsWith<IllegalArgumentException> {
            attractiveForceCurve(gaps) { 1.0 + it }
        }
    }

    /** A two-exponential test field: not a pure exponential, so the scheme has to work for it. */
    private fun field(gap: Double): Double = -50.0 * exp(-gap / 3.0) - 800.0 * exp(-gap / 1.6)

}
