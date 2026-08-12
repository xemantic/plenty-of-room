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
 * The Cholesky factorisation `A = L Lᵀ` of a symmetric positive-definite matrix.
 *
 * The Rayleigh-Ritz stiffness matrix of a plate on an elastic foundation is symmetric
 * positive-definite by construction — the foundation term alone is, and the bending term
 * is positive semi-definite — so Cholesky is the right factorisation and its success is
 * itself a check that the assembled matrix is what it is claimed to be.
 *
 * Two things are wanted from it: the solution of `A x = b` for the deflection, and the
 * **diagonal of the inverse**, which is what equipartition turns into the thermal
 * fluctuation amplitude of each Ritz mode.
 *
 * @throws IllegalArgumentException if [matrix] is not square and two-dimensional,
 *          or if it is not positive-definite.
 */
class CholeskyDecomposition(matrix: F64Array) {

    /** The order of the matrix. */
    val size: Int

    private val lower: F64Array

    init {
        require(matrix.nDim == 2) { "matrix must be two-dimensional, was: ${matrix.nDim}" }
        require(matrix.shape[0] == matrix.shape[1]) {
            "matrix must be square, was: ${matrix.shape[0]} x ${matrix.shape[1]}"
        }
        size = matrix.shape[0]
        lower = F64Array(size, size)
        for (i in 0 until size) {
            for (j in 0..i) {
                val product = if (j == 0) 0.0
                else lower.V[i].slice(0, j).dot(lower.V[j].slice(0, j))
                if (i == j) {
                    val residual = matrix[i, i] - product
                    require(residual > 0.0) {
                        "matrix is not positive-definite: non-positive pivot at index $i"
                    }
                    lower[i, j] = kotlin.math.sqrt(residual)
                } else {
                    lower[i, j] = (matrix[i, j] - product) / lower[j, j]
                }
            }
        }
    }

    /**
     * Returns the solution `x` of `A x = b` for the right-hand side [b].
     *
     * @throws IllegalArgumentException if [b] is not a vector of length [size].
     */
    fun solve(b: F64Array): F64Array {
        require(b.nDim == 1 && b.length == size) {
            "b must be a vector of length $size, was: ${b.shape.toList()}"
        }
        val x = b.copy()
        for (i in 0 until size) {
            val product = if (i == 0) 0.0 else lower.V[i].slice(0, i).dot(x.slice(0, i))
            x[i] = (x[i] - product) / lower[i, i]
        }
        for (i in size - 1 downTo 0) {
            var product = 0.0
            for (k in i + 1 until size) product += lower[k, i] * x[k]
            x[i] = (x[i] - product) / lower[i, i]
        }
        return x
    }

    /**
     * The diagonal of `A⁻¹`, obtained by solving against each unit vector in turn.
     *
     * `O(n³)`, the same order as the factorisation itself, and at the Ritz basis sizes
     * this problem needs (a few hundred unknowns) it costs milliseconds.
     */
    val inverseDiagonal: F64Array by lazy {
        F64Array(size) { i ->
            val unit = F64Array(size)
            unit[i] = 1.0
            solve(unit)[i]
        }
    }

}
