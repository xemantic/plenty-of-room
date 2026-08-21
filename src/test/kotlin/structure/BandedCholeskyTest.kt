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
import org.jetbrains.bio.viktor.F64Array
import kotlin.math.abs
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-253` — a **banded** symmetric factorisation, because the honeycomb grillage is 4 080 degrees
 * of freedom at one subdivision and 7 920 at two.
 *
 * The cheap bound that justifies it is in the task file: a dense factorisation of the second is
 * 490 MB of storage and `O(n³)` of work, and the same matrix ordered **node-major** has a
 * half-bandwidth of `4·beams + 3 = 243`, which is 15 MB and `n·b²/2 ≈ 2.4e8`.
 *
 * Every test is named for the verification gate it discharges.
 */
class BandedCholeskyTest {

    private fun symmetricBanded(size: Int, bandwidth: Int, seed: Int): F64Array {
        val random = Random(seed)
        val matrix = F64Array(size, size)
        for (i in 0 until size) {
            for (j in maxOf(0, i - bandwidth) until i) {
                val value = random.nextDouble(-1.0, 1.0)
                matrix[i, j] = value
                matrix[j, i] = value
            }
        }
        // strict diagonal dominance makes it positive-definite by Gershgorin
        for (i in 0 until size) {
            var row = 0.0
            for (j in 0 until size) if (j != i) row += abs(matrix[i, j])
            matrix[i, i] = row + 1.0
        }
        return matrix
    }

    @Test
    fun `gate 1 - dimensional - the factor reconstructs the matrix it was given`() {
        val size = 40
        val bandwidth = 7
        val matrix = symmetricBanded(size, bandwidth, 11)
        val factor = BandedCholesky(size, bandwidth) { i, j -> matrix[i, j] }
        for (i in 0 until size) {
            for (j in maxOf(0, i - bandwidth)..i) {
                var product = 0.0
                for (k in 0..j) product += factor.lower(i, k) * factor.lower(j, k)
                assert(product.isCloseTo(matrix[i, j]))
            }
        }
    }

    @Test
    fun `gate 2 - limiting case - a diagonal matrix factorises to its square roots`() {
        val factor = BandedCholesky(4, 2) { i, j -> if (i == j) (i + 1.0) * (i + 1.0) else 0.0 }
        for (i in 0 until 4) assert(factor.lower(i, i).isCloseTo(i + 1.0))
    }

    @Test
    fun `gate 2 - limiting case - bandwidth zero is a diagonal solve`() {
        val factor = BandedCholesky(3, 0) { i, _ -> (i + 2.0) }
        val solution = factor.solve(F64Array(3) { 1.0 })
        for (i in 0 until 3) assert(solution[i].isCloseTo(1.0 / (i + 2.0)))
    }

    @Test
    fun `gate 3 - conservation - the solve reproduces the dense Cholesky to the last digit`() {
        val size = 60
        val bandwidth = 9
        val matrix = symmetricBanded(size, bandwidth, 23)
        val right = F64Array(size) { Random(7).nextDouble() + it * 0.01 }
        val dense = CholeskyDecomposition(matrix).solve(right)
        val banded = BandedCholesky(size, bandwidth) { i, j -> matrix[i, j] }.solve(right)
        for (i in 0 until size) assert(abs(dense[i] - banded[i]) < 1e-12 * (1.0 + abs(dense[i])))
    }

    @Test
    fun `gate 3 - conservation - the residual of the solve is at machine precision`() {
        val size = 50
        val bandwidth = 6
        val matrix = symmetricBanded(size, bandwidth, 5)
        val right = F64Array(size) { 1.0 + 0.5 * it }
        val solution = BandedCholesky(size, bandwidth) { i, j -> matrix[i, j] }.solve(right)
        for (i in 0 until size) {
            var product = 0.0
            for (j in 0 until size) product += matrix[i, j] * solution[j]
            assert(abs(product - right[i]) < 1e-9)
        }
    }

    @Test
    fun `gate 3 - conservation - a bandwidth wider than the matrix is the dense case`() {
        val size = 12
        val matrix = symmetricBanded(size, size - 1, 3)
        val right = F64Array(size) { 1.0 }
        val wide = BandedCholesky(size, size + 5) { i, j -> matrix[i, j] }.solve(right)
        val dense = CholeskyDecomposition(matrix).solve(right)
        for (i in 0 until size) assert(abs(wide[i] - dense[i]) < 1e-12)
    }

    @Test
    fun `gate 3 - conservation - many right hand sides share one factorisation`() {
        val size = 30
        val bandwidth = 4
        val matrix = symmetricBanded(size, bandwidth, 31)
        val factor = BandedCholesky(size, bandwidth) { i, j -> matrix[i, j] }
        repeat(3) { case ->
            val right = F64Array(size) { (it + case + 1.0) }
            val solution = factor.solve(right)
            for (i in 0 until size) {
                var product = 0.0
                for (j in 0 until size) product += matrix[i, j] * solution[j]
                assert(abs(product - right[i]) < 1e-9)
            }
        }
    }

    @Test
    fun `gate 2 - limiting case - a singular matrix is refused rather than returned`() {
        assertFailsWith<IllegalArgumentException> {
            BandedCholesky(2, 1) { i, j -> if (i == j) 1.0 else 1.0 }
        }
    }

    @Test
    fun `gate 1 - dimensional - the constructor refuses a non-positive size or a negative band`() {
        assertFailsWith<IllegalArgumentException> { BandedCholesky(0, 1) { _, _ -> 1.0 } }
        assertFailsWith<IllegalArgumentException> { BandedCholesky(3, -1) { _, _ -> 1.0 } }
    }

    @Test
    fun `gate 1 - dimensional - a right hand side of the wrong length is refused`() {
        val factor = BandedCholesky(3, 1) { i, j -> if (i == j) 2.0 else 0.0 }
        assertFailsWith<IllegalArgumentException> { factor.solve(F64Array(4) { 1.0 }) }
    }

}
