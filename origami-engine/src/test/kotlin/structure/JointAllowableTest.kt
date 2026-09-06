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

package com.xemantic.nano.plentyofroom.structure

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.abs
import kotlin.math.ln
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * The joint's own allowable as a function of the bonded length, from the primary source
 * `C-0006` already traces the 48 pN to.
 *
 * `T-19` needs it because the entry topology's third question — a bond spread over `k`
 * consecutive bases — turns out not to be a sheet question at all: the loaded duplex must
 * carry the whole tension somewhere inboard of the footprint whatever the footprint is, so
 * everything a footprint buys, it buys on the **joint**.
 */

/** Strunz et al.'s slowest measured loading rate, in pN/s — 8 nm/s at 2 pN/nm. */
private const val SLOW_RATE = 16.0

/** The loading rate the 48 pN was measured at, in pN/s — 50 nm/s at 2 pN/nm. */
private const val REFERENCE_RATE = 100.0

/** Strunz et al.'s fastest measured loading rate, in pN/s. */
private const val FAST_RATE = 4000.0

class JointAllowableTest {

    // ------------------------------------------------------------------ gate 1

    @Test
    fun `gate 1 arguments are validated`() {
        val joint = ShearJointAllowable()
        assertFailsWith<IllegalArgumentException> { joint.ruptureForce(0.0, REFERENCE_RATE) }
        assertFailsWith<IllegalArgumentException> { joint.ruptureForce(30.0, 0.0) }
        assertFailsWith<IllegalArgumentException> {
            ShearJointAllowable(separationPerBasePair = 0.0)
        }
    }

    @Test
    fun `gate 1 the force rises by exactly kT over x per e-fold of loading rate`() {
        // dimensional: the single-barrier form is logarithmic in the loading rate with a
        // slope that is a force, and it is the thermal energy over the barrier separation
        val joint = ShearJointAllowable()
        val bases = 20.0
        val step = joint.ruptureForce(bases, 2.0 * REFERENCE_RATE) -
                joint.ruptureForce(bases, REFERENCE_RATE)
        assert(step.isCloseTo(thermalEnergy() * ln(2.0) / joint.barrierSeparation(bases)))
    }

    // ------------------------------------------------------------------ gate 2

    @Test
    fun `gate 2 the rupture force rises with length and with loading rate`() {
        val joint = ShearJointAllowable()
        val lengths = (5..40).map { joint.ruptureForce(it.toDouble(), REFERENCE_RATE) }
        lengths.zipWithNext().forEach { (low, high) -> assert(high > low) }
        assert(
            joint.ruptureForce(30.0, FAST_RATE) > joint.ruptureForce(30.0, SLOW_RATE)
        )
    }

    @Test
    fun `gate 2 the rupture force saturates with length at a loading-rate-free value`() {
        val joint = ShearJointAllowable()
        // the limit is kT beta ln10 / (per-base-pair separation), and the log term dies as 1/n
        assert(joint.ruptureForce(100000.0, REFERENCE_RATE).isCloseTo(joint.saturationForce, 1e-3))
        assert(joint.ruptureForce(100000.0, FAST_RATE).isCloseTo(joint.saturationForce, 1e-3))
        assert(joint.ruptureForce(30.0, REFERENCE_RATE) < joint.saturationForce)
    }

    @Test
    fun `gate 2 splitting a bond has a break-even length and it is not zero`() {
        // the joint side of the two-duplex bond: m domains of n/m against one of n. The
        // saturation makes the allowable concave at large n, so splitting wins there; the
        // barrier separation's own offset makes it convex at small n, so splitting loses
        // there. Both are properties of Strunz's own fitted form, not assumptions.
        val joint = ShearJointAllowable()
        assert(joint.splitGain(32.0, 2, REFERENCE_RATE) > 1.0)
        assert(joint.splitGain(8.0, 2, REFERENCE_RATE) < 1.0)
        val breakEven = joint.splitBreakEven(2, REFERENCE_RATE)
        assert(breakEven > 8.0 && breakEven < 32.0)
        assert(joint.splitGain(breakEven, 2, REFERENCE_RATE).isCloseTo(1.0, 1e-6))
    }

    // ------------------------------------------------------------------ gate 5

    @Test
    fun `gate 5 the model reproduces Strunz's own 48 pN at 30 base pairs`() {
        // built from the paper's published scaling constants alone — alpha = 3, beta = 0.5
        // (Eq. 2) and x = 0.7 nm + 0.07 nm per base pair (Eq. 3) — and asked for the one
        // number C-0006 carries into this programme
        val joint = ShearJointAllowable()
        assert(abs(joint.ruptureForce(30.0, REFERENCE_RATE) - 48.0) <= 2.0)
    }

    @Test
    fun `gate 5 the model reproduces Strunz's own quoted saturation of about 70 pN`() {
        val joint = ShearJointAllowable()
        assert(joint.saturationForce > 60.0 && joint.saturationForce < 75.0)
    }

    @Test
    fun `gate 5 the model puts the ten base-pair duplex in the measured twenty to fifty band`() {
        // the abstract: "Depending on the loading rate and sequence length, the unbinding
        // forces of single duplexes varied from 20 to 50 pN" over 16-4000 pN/s and 10-30 bp
        val joint = ShearJointAllowable()
        val extremes = listOf(
            joint.ruptureForce(10.0, SLOW_RATE), joint.ruptureForce(30.0, FAST_RATE)
        )
        assert(extremes[0] > 10.0 && extremes[0] < 25.0)
        assert(extremes[1] > 45.0 && extremes[1] < 60.0)
    }

    @Test
    fun `gate 5 the fitted parameter bracket moves the answer but not the break-even sign`() {
        // alpha = 3 +/- 1 and beta = 0.5 +/- 0.1 are the paper's own error bars
        val soft = ShearJointAllowable(
            offRateExponentIntercept = 4.0, offRateExponentSlope = 0.4
        )
        val stiff = ShearJointAllowable(
            offRateExponentIntercept = 2.0, offRateExponentSlope = 0.6
        )
        listOf(soft, stiff).forEach { joint ->
            assert(joint.splitGain(32.0, 2, REFERENCE_RATE) > 1.0)
            assert(joint.saturationForce > 40.0)
        }
    }

}
