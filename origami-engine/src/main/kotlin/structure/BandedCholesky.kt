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
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * The Cholesky factorisation `A = L Lᵀ` of a symmetric positive-definite matrix whose non-zeros
 * lie within [bandwidth] of the diagonal — `A[i, j] = 0` wherever `|i − j| > bandwidth`.
 *
 * ## Why this exists beside [CholeskyDecomposition]
 *
 * `T-253`'s honeycomb lattice is a **three-dimensional** grillage: 60 duplex beams with four
 * degrees of freedom per node, which is 4 080 unknowns at one beam subdivision and 7 920 at two.
 * A dense factorisation of the second is 490 MB of storage before the factor and `O(n³)` of work,
 * and `CLAUDE.md` already records what a few tens of megabytes of retained matrix does to a
 * Gradle test worker.
 *
 * Ordering the degrees of freedom **node-major** — every beam's unknowns at one node column
 * before any unknown at the next — makes every coupling either within a node column (bonds) or
 * between two consecutive ones (beam elements), so the half-bandwidth is `dofPerNode · beams`
 * plus the within-node offset. At 60 beams and four components that is 243, and the banded
 * factorisation stores `n·(b+1)` and costs about `n·b²/2`: 15 MB and `2.4e8` flops against 490 MB
 * and `8e10`. The arithmetic is done in the task file, before the code.
 *
 * The factor is stored lower-triangular in band form, `band[i·(bandwidth+1) + (j − i + bandwidth)]`
 * holding `L[i, j]` for `max(0, i − bandwidth) ≤ j ≤ i`.
 *
 * @param size the order of the matrix.
 * @param bandwidth the half-bandwidth; `0` is a diagonal matrix and anything at or above
 *          `size − 1` is the dense case, which this class then reproduces exactly.
 * @param entry `A[i, j]`, called only for `j ≤ i` and `i − j ≤ bandwidth`.
 * @throws IllegalArgumentException if the matrix is not positive-definite, which — exactly as for
 *          [CholeskyDecomposition] — is itself a check that the assembled matrix is what it is
 *          claimed to be.
 */
class BandedCholesky(
    val size: Int,
    bandwidth: Int,
    entry: (Int, Int) -> Double
) {

    /** The half-bandwidth actually used, never wider than `size − 1`. */
    val bandwidth: Int

    private val band: DoubleArray

    init {
        require(size > 0) { "size must be positive, was: $size" }
        require(bandwidth >= 0) { "bandwidth must not be negative, was: $bandwidth" }
        this.bandwidth = min(bandwidth, size - 1)
        val width = this.bandwidth + 1
        band = DoubleArray(size * width)
        for (i in 0 until size) {
            val low = max(0, i - this.bandwidth)
            for (j in low..i) {
                var product = 0.0
                val start = max(low, j - this.bandwidth)
                for (k in start until j) {
                    product += band[i * width + (k - i + this.bandwidth)] *
                            band[j * width + (k - j + this.bandwidth)]
                }
                val residual = entry(i, j) - product
                if (i == j) {
                    require(residual > 0.0) {
                        "matrix is not positive-definite: non-positive pivot at index $i"
                    }
                    band[i * width + this.bandwidth] = sqrt(residual)
                } else {
                    band[i * width + (j - i + this.bandwidth)] =
                        residual / band[j * width + this.bandwidth]
                }
            }
        }
    }

    /** `L[i, j]`, zero outside the stored band and above the diagonal. */
    fun lower(i: Int, j: Int): Double {
        require(i in 0 until size) { "i must be within 0 until $size, was: $i" }
        require(j in 0 until size) { "j must be within 0 until $size, was: $j" }
        if (j > i || i - j > bandwidth) return 0.0
        return band[i * (bandwidth + 1) + (j - i + bandwidth)]
    }

    /** The solution `x` of `A x = `[right]. */
    fun solve(right: F64Array): F64Array {
        require(right.nDim == 1 && right.length == size) {
            "right must be a vector of length $size, was: ${right.shape.toList()}"
        }
        val width = bandwidth + 1
        val x = DoubleArray(size) { right[it] }
        for (i in 0 until size) {
            var total = x[i]
            val low = max(0, i - bandwidth)
            for (j in low until i) total -= band[i * width + (j - i + bandwidth)] * x[j]
            x[i] = total / band[i * width + bandwidth]
        }
        for (i in size - 1 downTo 0) {
            var total = x[i]
            val high = min(size - 1, i + bandwidth)
            for (j in i + 1..high) total -= band[j * width + (i - j + bandwidth)] * x[j]
            x[i] = total / band[i * width + bandwidth]
        }
        return F64Array(size) { x[it] }
    }

}
