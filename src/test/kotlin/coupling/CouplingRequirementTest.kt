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

package com.xemantic.nano.plentyofroom.coupling

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-16` — the **requirement** side: the load line, and the two conditions a coupling has to
 * meet rather than the one `C-0012` states.
 *
 * The whole task turns on the difference between *"stiff enough not to fall over"* and
 * *"the right stiffness to put the operating point where §3 wants it"*, so every assertion
 * here is about keeping those two apart.
 */
class CouplingRequirementTest {

    /**
     * A synthetic characteristic with the shape `C-0012` reports: positive at zero stroke
     * (the blocking force), **rising** with stroke because `k_eff < 0`, and analytically
     * differentiable so that `dW/ds = −k_eff` can be checked rather than assumed.
     */
    private fun risingCharacteristic(
        blocking: Double,
        slope: Double
    ): OutputCharacteristic = OutputCharacteristic { blocking + slope * it }

    // ---------------------------------------------------------------- gate 1

    @Test
    fun `gate 1 dimensional consistency - the mandated coupling stiffness is a force over a length`() {
        // §3's own two numbers, and nothing else: 100 pN delivered over a 3 nm stroke
        assert(mandatedCouplingStiffness(100.0, 3.0).isCloseTo(100.0 / 3.0, 1e-12))
        // and it scales as a stiffness must: double the force, double the stiffness
        assert(mandatedCouplingStiffness(200.0, 3.0).isCloseTo(2.0 * 100.0 / 3.0, 1e-12))
        // half the stroke, double the stiffness
        assert(mandatedCouplingStiffness(100.0, 1.5).isCloseTo(2.0 * 100.0 / 3.0, 1e-12))
    }

    @Test
    fun `gate 1 dimensional consistency - a reaction minus an output force is a force and vanishes at the root`() {
        val characteristic = risingCharacteristic(blocking = 34.46, slope = 8.5)
        val coupling = LinearCoupling(stiffness = 33.0)
        val root = firstOperatingStroke(characteristic, coupling, 9.0)!!
        assert((coupling.reaction(root) - characteristic.outputForce(root)).isCloseTo(0.0, 1e-9))
    }

    @Test
    fun `gate 1 dimensional consistency - the placement preload is a force`() {
        // preload = k_c s − W(s): the force the coupling must already carry at zero stroke
        assert(placementPreload(stiffness = 50.0, stroke = 3.0, outputForce = 100.0).isCloseTo(50.0))
        // and it is exactly zero when the coupling is the unpreloaded placement stiffness
        val exact = 100.0 / 3.0
        assert(placementPreload(exact, 3.0, 100.0).isCloseTo(0.0, 1e-9))
    }

    @Test
    fun `gate 1 dimensional consistency - unphysical arguments throw rather than returning a number`() {
        assertFailsWith<IllegalArgumentException> { mandatedCouplingStiffness(100.0, 0.0) }
        assertFailsWith<IllegalArgumentException> { mandatedCouplingStiffness(-1.0, 3.0) }
        assertFailsWith<IllegalArgumentException> { LinearCoupling(stiffness = -1.0) }
    }

    // ---------------------------------------------------------------- gate 2

    @Test
    fun `gate 2 limiting cases - an infinitely stiff coupling delivers no stroke and a vanishing one delivers the free stroke`() {
        // free stroke: the root of W(s) = 0, here a FALLING characteristic so that one exists
        val characteristic = OutputCharacteristic { 34.46 - 10.0 * it }
        val free = firstOperatingStroke(characteristic, LinearCoupling(1e-12), 20.0)!!
        assert(free.isCloseTo(3.446, 1e-6))
        val stiff = firstOperatingStroke(characteristic, LinearCoupling(1e9), 20.0)!!
        assert(stiff.isCloseTo(0.0, 1e-6))
    }

    @Test
    fun `gate 2 limiting cases - the delivered stroke is monotone decreasing in the coupling stiffness`() {
        val characteristic = OutputCharacteristic { 34.46 + 8.5 * it - 0.4 * it * it }
        val strokes = listOf(1.0, 3.0, 10.0, 30.0, 100.0, 300.0).map {
            firstOperatingStroke(characteristic, LinearCoupling(it), 60.0)!!
        }
        strokes.zipWithNext { a, b -> assert(b < a) }
    }

    @Test
    fun `gate 2 limiting cases - a characteristic that never crosses the load line returns no operating point`() {
        // a rising characteristic against a load line softer than its own slope never crosses
        val characteristic = risingCharacteristic(blocking = 34.46, slope = 20.0)
        assert(firstOperatingStroke(characteristic, LinearCoupling(5.0), 9.0) == null)
    }

    @Test
    fun `gate 2 limiting cases - the unpreloaded placement stiffness puts the root exactly at the target`() {
        val characteristic = OutputCharacteristic { 34.46 + 8.5 * it - 0.4 * it * it }
        val target = 3.0
        val placement = characteristic.outputForce(target) / target
        val root = firstOperatingStroke(characteristic, LinearCoupling(placement), 20.0)!!
        assert(root.isCloseTo(target, 1e-7))
    }

    // ---------------------------------------------------------------- gate 3

    @Test
    fun `gate 3 symmetry - the force delivered over a stroke is independent of the preload`() {
        val stiffness = 33.3
        val from = 0.4
        val to = 3.4
        listOf(-200.0, -50.0, 0.0, 50.0, 200.0).forEach { preload ->
            val coupling = LinearCoupling(stiffness, preload)
            assert(
                (coupling.reaction(to) - coupling.reaction(from))
                    .isCloseTo(stiffness * (to - from), 1e-12)
            )
            assert(coupling.deliveredForce(from, to).isCloseTo(stiffness * (to - from), 1e-12))
        }
    }

    @Test
    fun `gate 3 symmetry - stability is the sign of the load line minus the characteristic slope`() {
        // k_eff = -dW/ds, so a coupling is stabilising exactly when k_c exceeds |k_eff|
        val window = CouplingWindow(
            targetStroke = 3.0,
            outputForceAtTarget = 100.0,
            effectiveStiffnessAtTarget = -25.0,
            mandatedStiffness = 100.0 / 3.0
        )
        assert(window.stabilityFloor.isCloseTo(25.0))
        assert(window.unpreloadedPlacementStiffness.isCloseTo(100.0 / 3.0, 1e-12))
        assert(window.mandatedStiffnessIsStable)
        assert(window.stabilityMargin.isCloseTo((100.0 / 3.0) / 25.0, 1e-12))
        assert(!window.isEmpty)
    }

    @Test
    fun `gate 3 symmetry - a positive effective stiffness needs no coupling at all`() {
        val window = CouplingWindow(
            targetStroke = 3.0,
            outputForceAtTarget = 100.0,
            effectiveStiffnessAtTarget = +42.6,
            mandatedStiffness = 100.0 / 3.0
        )
        assert(window.stabilityFloor.isCloseTo(0.0))
        assert(window.mandatedStiffnessIsStable)
        assert(!window.isEmpty)
    }

    @Test
    fun `gate 3 symmetry - the window is empty exactly when the chord is flatter than the tangent`() {
        // 10 nm, 0.25 V, alexander-box(two-body) as C-0012 reports it: W(3) = 170.76,
        // k_eff(3) = -60.87. The chord W(3)/3 = 56.92 is BELOW the tangent 60.87, so no
        // unpreloaded linear coupling is both placed at 3 nm and stable there.
        val empty = CouplingWindow(3.0, 170.76, -60.87, 100.0 / 3.0)
        assert(empty.unpreloadedPlacementStiffness < empty.stabilityFloor)
        assert(empty.unpreloadedWindowIsEmpty)
        // but a PRELOADED coupling escapes it: stiffness above the floor, preload made up
        val preloaded = 70.0
        assert(preloaded > empty.stabilityFloor)
        assert(placementPreload(preloaded, 3.0, 170.76).isCloseTo(70.0 * 3.0 - 170.76))
    }

    // ---------------------------------------------------------------- gate 4

    @Test
    fun `gate 4 convergence - the operating stroke exits on the bracket width and is scan-independent`() {
        val characteristic = OutputCharacteristic { 34.46 + 8.5 * it - 0.4 * it * it }
        val coarse = firstOperatingStroke(characteristic, LinearCoupling(33.0), 20.0, scanSteps = 64)!!
        val fine = firstOperatingStroke(characteristic, LinearCoupling(33.0), 20.0, scanSteps = 8192)!!
        assert(coarse.isCloseTo(fine, 1e-9))
    }
}
