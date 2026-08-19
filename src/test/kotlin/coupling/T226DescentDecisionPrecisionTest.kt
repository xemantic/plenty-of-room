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
import kotlin.math.abs
import kotlin.math.max
import kotlin.test.Test

/**
 * `T-226` — the decision precision of `C-0058`'s coordinate descent.
 *
 * `C-0138` §8 measured that two runs of **identical code** on **identical inputs** emit
 * `gpd/results/T-113-non-uniform-coupling.json` with 217 fields different, and that the moving
 * block is always one descent record and its transfers.
 *
 * [optimiseStiffnessDistribution] compares raw `Double`s at every decision it takes — the coarse
 * scan's `value < bestValue`, the golden section's `leftValue < rightValue`, the acceptance
 * `bestValue < best - 1e-15` and the start ranking — so an arithmetic difference far below the
 * precision the answer has can flip a branch and move the terminal point.  `CLAUDE.md`:
 * *"a DECISION must be rounded COARSER than the number it is taken on"*.
 *
 * [minimaxStiffnessDistribution] already wraps its own objective in [searchDecision] at its polish
 * call; `coupling.NonUniformCouplingStudy` does not, at any of its three call sites.  These tests
 * pin the mechanism and the cure, on a jitter whose size is chosen to sit **between** the two:
 * `1e-14` relative is far above a last-ulp difference and far below the `1e-9` relative cell of a
 * six-significant-digit decision, so it must reach an unwrapped comparison and must not reach a
 * wrapped one.
 */
class T226DescentDecisionPrecisionTest {

    /**
     * A deterministic per-evaluation perturbation of relative size [jitter], standing in for the
     * JIT recompiling a hot reduction part-way through a run (`CLAUDE.md`), wrapped around a
     * [base] objective.
     */
    private fun jittered(
        jitter: Double,
        phase: Int,
        base: (List<Double>) -> Double
    ): (List<Double>) -> Double {
        var evaluations = 0
        return { stiffnesses ->
            evaluations++
            base(stiffnesses) * (1.0 + jitter * (((evaluations * 7 + phase * 3) % 5) - 2))
        }
    }

    /**
     * The **perfect manifold**: an objective every admissible distribution attains equally.
     *
     * `cappedStiffnesses` conserves the mandate exactly, so an objective that is a function of the
     * SUM alone is constant over the whole feasible set — which is `C-0135`'s *"a descent on an
     * optimal MANIFOLD has no isolated answer to be reproducible about"* in its limiting case, and
     * the case in which the arithmetic is the ONLY thing left to decide the terminal point.
     */
    private val plateau: (List<Double>) -> Double = { stiffnesses -> stiffnesses.sum() * 0.01 }

    /** A minimax of the same shape as `T-113`'s: a max of smooth functions, hence a kinked one. */
    private val minimax: (List<Double>) -> Double = { stiffnesses ->
        listOf(
            listOf(1.0, 0.7, 0.4, 0.9, 0.6),
            listOf(0.5, 1.0, 0.8, 0.3, 0.7),
            listOf(0.8, 0.4, 1.0, 0.6, 0.5)
        ).maxOf { target ->
            var sum = 0.0
            for (i in stiffnesses.indices) {
                val d = stiffnesses[i] - target[i % target.size]
                sum += d * d
            }
            sum
        }
    }

    private fun run(
        base: (List<Double>) -> Double,
        jitter: Double,
        phase: Int,
        round: Boolean
    ): List<Double> {
        val raw = jittered(jitter, phase, base)
        return optimiseStiffnessDistribution(
            totalStiffness = 5.0,
            starts = listOf(List(5) { 1.0 }, listOf(2.0, 1.0, 0.5, 1.0, 1.5)),
            sweeps = 12,
            tolerance = 1e-5,
            searchHalfWidth = 2.0,
            scanPoints = 7,
            refinements = 8,
            objective = if (round) { ks -> searchDecision(raw(ks)) } else raw
        ).stiffnesses
    }

    private fun spread(a: List<Double>, b: List<Double>): Double {
        var worst = 0.0
        for (i in a.indices) {
            worst = max(worst, abs(a[i] - b[i]) / max(abs(a[i]), 1e-30))
        }
        return worst
    }

    @Test
    fun `the descent is deterministic when nothing perturbs it`() {
        assert(spread(run(minimax, 0.0, 0, false), run(minimax, 0.0, 0, false)) == 0.0)
        assert(spread(run(minimax, 0.0, 0, true), run(minimax, 0.0, 0, true)) == 0.0)
        assert(spread(run(plateau, 0.0, 0, false), run(plateau, 0.0, 0, false)) == 0.0)
    }

    @Test
    fun `a sub-decision-precision perturbation MOVES the unwrapped descent on a manifold`() {
        // the defect: `1e-14` relative is eight decades below the four digits this study reports,
        // and on a manifold it still moves the ANSWER, because no comparison in the descent has a
        // tolerance -- neither the coarse scan's `value < bestValue`, nor the golden section's
        // `leftValue < rightValue`, nor the start ranking.
        assert(spread(run(plateau, 1e-14, 0, false), run(plateau, 1e-14, 1, false)) > 1e-6)
    }

    @Test
    fun `the same perturbation does NOT move the descent decided at six significant digits`() {
        assert(spread(run(plateau, 1e-14, 0, true), run(plateau, 1e-14, 1, true)) == 0.0)
        assert(spread(run(minimax, 1e-14, 0, true), run(minimax, 1e-14, 1, true)) == 0.0)
    }

    @Test
    fun `rounding the objective does not degrade the optimum it finds`() {
        val rounded = run(minimax, 0.0, 0, true)
        val raw = run(minimax, 0.0, 0, false)
        // the rounded descent must not be worse than the raw one by more than the cell it decides on
        assert(minimax(rounded) < minimax(raw) * (1.0 + 1e-5))
    }

    @Test
    fun `searchDecision quantises at six significant digits and leaves the extremes alone`() {
        assert(searchDecision(0.12345678) == 0.123457)
        assert(searchDecision(-0.12345678) == -0.123457)
        assert(searchDecision(0.0) == 0.0)
        assert(searchDecision(Double.POSITIVE_INFINITY) == Double.POSITIVE_INFINITY)
        // two values differing by 1e-14 relative land on the same decision
        assert(searchDecision(0.1234567) == searchDecision(0.1234567 * (1.0 + 1e-14)))
    }
}
