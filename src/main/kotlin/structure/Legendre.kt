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

import org.jetbrains.bio.viktor.F64Array

/**
 * The Legendre polynomials `P₀ … P_degree` and their first two derivatives, all at one point.
 *
 * The Rayleigh-Ritz plate solve of [PlateOnFoundation] needs all three at every quadrature
 * node, and the three-term recurrences that produce them share their state, so they are
 * evaluated together rather than three times over.
 */
data class LegendreJet(
    val value: F64Array,
    val firstDerivative: F64Array,
    val secondDerivative: F64Array
)

/**
 * Returns the Legendre polynomials up to [degree] and their first two derivatives at [x].
 *
 * From the standard recurrences
 * `(n+1)P_{n+1} = (2n+1)xP_n − nP_{n−1}` and `P'_{n+1} = (2n+1)P_n + P'_{n−1}`,
 * the second differentiated once more. Valid for any real [x]; the plate solve only ever
 * evaluates it on `[−1, 1]`, but the Newton iteration of [gaussLegendreRule] may step outside.
 *
 * @throws IllegalArgumentException if [degree] is negative.
 */
fun legendreJet(degree: Int, x: Double): LegendreJet {
    require(degree >= 0) { "degree cannot be negative, was: $degree" }
    val value = F64Array(degree + 1)
    val first = F64Array(degree + 1)
    val second = F64Array(degree + 1)
    value[0] = 1.0
    if (degree >= 1) {
        value[1] = x
        first[1] = 1.0
    }
    for (n in 1 until degree) {
        value[n + 1] = ((2 * n + 1) * x * value[n] - n * value[n - 1]) / (n + 1)
        first[n + 1] = (2 * n + 1) * value[n] + first[n - 1]
        second[n + 1] = (2 * n + 1) * first[n] + second[n - 1]
    }
    return LegendreJet(value, first, second)
}

/** A Gauss-Legendre quadrature rule on `[−1, 1]`: [nodes] and their [weights]. */
data class GaussLegendreRule(
    val nodes: F64Array,
    val weights: F64Array
) {

    /** The number of nodes. */
    val points: Int get() = nodes.length

    /**
     * Returns the nodes of this rule mapped onto `[−half, half]`,
     * the coordinate range the plate is written in.
     */
    fun scaledNodes(half: Double): F64Array = F64Array(points) { nodes[it] * half }

    /** Returns the weights of this rule scaled for an interval of half-width [half]. */
    fun scaledWeights(half: Double): F64Array = F64Array(points) { weights[it] * half }

    companion object {

        /**
         * The number of Newton iterations per node.
         *
         * Newton on a Legendre root converges quadratically from the Chebyshev-like starting
         * guess below, so this is an upper bound that is never reached; the loop exits on
         * the step size.
         */
        const val MAX_NEWTON_ITERATIONS: Int = 100
    }
}

/**
 * Returns the [points]-node Gauss-Legendre rule on `[−1, 1]`,
 * which integrates polynomials up to degree `2·points − 1` exactly.
 *
 * The nodes are the roots of `P_points`, found by Newton iteration from the
 * standard `cos(π(i + ¾)/(points + ½))` starting guess, and the weights are
 * `2/((1−x²)P'_points(x)²)`.
 *
 * Exactness is what the plate solve needs: the Ritz stiffness matrix entries are
 * polynomials of known degree, so with enough nodes the quadrature contributes no error
 * at all and the only approximation left is the truncation of the basis — which is the
 * thing gate 4 then has to converge.
 *
 * @throws IllegalArgumentException if [points] is not positive.
 */
fun gaussLegendreRule(points: Int): GaussLegendreRule {
    require(points >= 1) { "points must be positive, was: $points" }
    val nodes = F64Array(points)
    val weights = F64Array(points)
    for (i in 0 until points) {
        var x = kotlin.math.cos(kotlin.math.PI * (i + 0.75) / (points + 0.5))
        var iteration = 0
        while (iteration < GaussLegendreRule.MAX_NEWTON_ITERATIONS) {
            val jet = legendreJet(points, x)
            val step = jet.value[points] / jet.firstDerivative[points]
            x -= step
            if (kotlin.math.abs(step) < 1e-16) break
            iteration++
        }
        val jet = legendreJet(points, x)
        nodes[i] = x
        weights[i] = 2.0 / ((1.0 - x * x) * jet.firstDerivative[points] * jet.firstDerivative[points])
    }
    return GaussLegendreRule(nodes, weights)
}
