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
import kotlin.math.abs
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * The Ritz basis and its quadrature, which the plate solve of `T-5` / `T-5b` rests on.
 * Nothing here is physics; it is the numerical substrate, and it is checked exactly
 * because everything above it is checked only relatively.
 */
class LegendreTest {

    // ---------------------------------------------------------------- gate 1

    @Test
    fun `gate 1 dimensional consistency - the recurrence should reproduce the closed-form low-order polynomials`() {
        listOf(-1.0, -0.6, -0.1, 0.0, 0.25, 0.7, 1.0).forEach { x ->
            val jet = legendreJet(4, x)
            assert(jet.value[0].isCloseTo(1.0))
            assert(jet.value[1].isCloseTo(x, 1e-12))
            assert(jet.value[2].isCloseTo((3.0 * x * x - 1.0) / 2.0, 1e-12))
            assert(jet.value[3].isCloseTo((5.0 * x.pow(3) - 3.0 * x) / 2.0, 1e-12))
            assert(
                jet.value[4].isCloseTo((35.0 * x.pow(4) - 30.0 * x * x + 3.0) / 8.0, 1e-12)
            )
        }
    }

    @Test
    fun `gate 1 dimensional consistency - the derivative recurrences should match central differences`() {
        val step = 1e-5
        listOf(-0.7, -0.2, 0.35, 0.8).forEach { x ->
            val jet = legendreJet(8, x)
            val forward = legendreJet(8, x + step)
            val backward = legendreJet(8, x - step)
            for (n in 0..8) {
                val first = (forward.value[n] - backward.value[n]) / (2.0 * step)
                val second =
                    (forward.value[n] - 2.0 * jet.value[n] + backward.value[n]) / (step * step)
                assert(abs(jet.firstDerivative[n] - first) < 1e-6)
                assert(abs(jet.secondDerivative[n] - second) < 1e-3)
            }
        }
    }

    // ---------------------------------------------------------------- gate 3

    @Test
    fun `gate 3 symmetry - Legendre polynomials should have the parity of their degree`() {
        listOf(0.13, 0.5, 0.91).forEach { x ->
            val positive = legendreJet(10, x)
            val negative = legendreJet(10, -x)
            for (n in 0..10) {
                val parity = if (n % 2 == 0) 1.0 else -1.0
                assert(negative.value[n].isCloseTo(parity * positive.value[n], 1e-12))
            }
        }
    }

    @Test
    fun `gate 3 symmetry - the Gauss-Legendre nodes and weights should be symmetric about the origin`() {
        listOf(3, 8, 15).forEach { points ->
            val rule = gaussLegendreRule(points)
            for (i in 0 until points) {
                val mirrored = points - 1 - i
                // absolute, not relative: an odd rule has a node at the origin, and comparing
                // two numbers that are both meant to be zero relatively compares their noise
                assert(abs(rule.nodes[i] + rule.nodes[mirrored]) < 1e-14)
                assert(abs(rule.weights[i] - rule.weights[mirrored]) < 1e-14)
            }
        }
    }

    // ---------------------------------------------------------------- gate 4

    @Test
    fun `gate 4 numerical convergence - an n point Gauss rule should integrate degree 2n-1 exactly`() {
        listOf(2, 4, 7, 12).forEach { points ->
            val rule = gaussLegendreRule(points)
            for (power in 0..(2 * points - 1)) {
                val quadrature = (0 until points).sumOf { i ->
                    rule.weights[i] * rule.nodes[i].pow(power)
                }
                val exact = if (power % 2 == 0) 2.0 / (power + 1) else 0.0
                assert(abs(quadrature - exact) < 1e-11)
            }
        }
    }

    @Test
    fun `gate 4 numerical convergence - the Gauss rule should reproduce Legendre orthogonality`() {
        val rule = gaussLegendreRule(12)
        val jets = (0 until rule.points).map { legendreJet(6, rule.nodes[it]) }
        for (i in 0..6) {
            for (j in 0..6) {
                val integral = (0 until rule.points).sumOf { k ->
                    rule.weights[k] * jets[k].value[i] * jets[k].value[j]
                }
                val exact = if (i == j) 2.0 / (2 * i + 1) else 0.0
                assert(abs(integral - exact) < 1e-12)
            }
        }
    }

    @Test
    fun `gate 4 numerical convergence - scaled nodes and weights should integrate over the scaled interval`() {
        val rule = gaussLegendreRule(6)
        val half = 20.0
        val nodes = rule.scaledNodes(half)
        val weights = rule.scaledWeights(half)
        val area = (0 until rule.points).sumOf { weights[it] }
        assert(area.isCloseTo(2.0 * half, 1e-12))
        val secondMoment = (0 until rule.points).sumOf { weights[it] * nodes[it] * nodes[it] }
        assert(secondMoment.isCloseTo(2.0 * half.pow(3) / 3.0, 1e-12))
    }

    // ---------------------------------------------------------------- validity

    @Test
    fun `a negative degree and a non-positive point count should be rejected`() {
        assertFailsWith<IllegalArgumentException> { legendreJet(-1, 0.0) }
        assertFailsWith<IllegalArgumentException> { gaussLegendreRule(0) }
    }

}
