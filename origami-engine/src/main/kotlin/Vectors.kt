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

package com.xemantic.nano.plentyofroom

import org.jetbrains.bio.viktor.F64Array
import org.jetbrains.bio.viktor.asF64Array
import org.openrndr.math.Vector3
import kotlin.math.acos
import kotlin.math.sqrt

/**
 * Returns the centroid of [points], the arithmetic mean of their coordinates.
 *
 * @throws IllegalArgumentException if [points] is empty.
 */
fun centroid(points: List<Vector3>): Vector3 {
    require(points.isNotEmpty()) { "points cannot be empty" }
    return points.reduce(Vector3::plus) / points.size.toDouble()
}

/**
 * Returns the angle between [a] and [b], in radians, from the `0..PI` range.
 *
 * @throws IllegalArgumentException if either vector has zero length,
 *          in which case the angle is undefined.
 */
fun angleBetween(a: Vector3, b: Vector3): Double {
    require(a.length > 0 && b.length > 0) { "vectors cannot be of zero length" }
    // the dot product of two unit vectors can leave the acos domain due to rounding
    return acos(a.normalized.dot(b.normalized).coerceIn(-1.0, 1.0))
}

/**
 * Returns the distance between each point of [from] and the corresponding point of [to].
 *
 * @throws IllegalArgumentException if the two lists differ in size.
 */
fun displacements(from: List<Vector3>, to: List<Vector3>): F64Array {
    require(from.size == to.size) {
        "from and to must be of equal size, was: ${from.size} and ${to.size}"
    }
    return DoubleArray(from.size) { from[it].distanceTo(to[it]) }.asF64Array()
}

/**
 * Returns the root mean square of [values],
 * which are never empty, as viktor rejects empty arrays already on construction.
 */
fun rootMeanSquare(values: F64Array): Double =
    sqrt(values.dot(values) / values.length)
