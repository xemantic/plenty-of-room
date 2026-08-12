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
import kotlin.test.Test
import kotlin.test.assertFailsWith

private val symmetricPositiveDefinite = F64Array(3, 3) { i, j ->
    listOf(
        listOf(4.0, 2.0, 1.0),
        listOf(2.0, 5.0, 3.0),
        listOf(1.0, 3.0, 6.0)
    )[i][j]
}

/**
 * The dense solver the Ritz plate solve depends on.
 * The Ritz stiffness matrix is symmetric positive-definite by construction,
 * so the factorisation succeeding is itself a statement about the assembled matrix.
 */
class CholeskyTest {

    // ---------------------------------------------------------------- gate 1

    @Test
    fun `gate 1 dimensional consistency - the solution should reproduce the right-hand side`() {
        val cholesky = CholeskyDecomposition(symmetricPositiveDefinite)
        val rightHandSide = F64Array(3) { listOf(7.0, -3.0, 11.0)[it] }
        val solution = cholesky.solve(rightHandSide)
        for (i in 0 until 3) {
            val product = (0 until 3).sumOf { j -> symmetricPositiveDefinite[i, j] * solution[j] }
            assert(product.isCloseTo(rightHandSide[i], 1e-12))
        }
    }

    // ---------------------------------------------------------------- gate 2

    @Test
    fun `gate 2 limiting cases - a diagonal matrix should invert element by element`() {
        val diagonal = F64Array(2, 2) { i, j -> if (i == j) listOf(2.0, 8.0)[i] else 0.0 }
        val inverse = CholeskyDecomposition(diagonal).inverseDiagonal
        assert(inverse[0].isCloseTo(0.5, 1e-12))
        assert(inverse[1].isCloseTo(0.125, 1e-12))
    }

    @Test
    fun `gate 2 limiting cases - the inverse diagonal should match the closed-form two by two inverse`() {
        val a = 3.0
        val b = 1.0
        val c = 5.0
        val matrix = F64Array(2, 2) { i, j -> if (i == j) listOf(a, c)[i] else b }
        val determinant = a * c - b * b
        val inverse = CholeskyDecomposition(matrix).inverseDiagonal
        assert(inverse[0].isCloseTo(c / determinant, 1e-12))
        assert(inverse[1].isCloseTo(a / determinant, 1e-12))
    }

    // ---------------------------------------------------------------- gate 3

    @Test
    fun `gate 3 symmetry - the inverse diagonal should be positive for a positive-definite matrix`() {
        val inverse = CholeskyDecomposition(symmetricPositiveDefinite).inverseDiagonal
        for (i in 0 until 3) assert(inverse[i] > 0.0)
    }

    // ---------------------------------------------------------------- validity

    @Test
    fun `a matrix that is not positive-definite should be rejected rather than silently factorised`() {
        val indefinite = F64Array(2, 2) { i, j -> if (i == j) listOf(1.0, 1.0)[i] else 2.0 }
        assertFailsWith<IllegalArgumentException> { CholeskyDecomposition(indefinite) }
    }

    @Test
    fun `a non-square or non-matrix argument should be rejected`() {
        assertFailsWith<IllegalArgumentException> { CholeskyDecomposition(F64Array(3)) }
        assertFailsWith<IllegalArgumentException> { CholeskyDecomposition(F64Array(2, 3)) }
    }

}
