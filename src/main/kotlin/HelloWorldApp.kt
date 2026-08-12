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

import org.openrndr.math.Vector3

private val relaxed = listOf(
    Vector3(0.0, 0.0, 0.0),
    Vector3(4.0, 0.0, 0.0),
    Vector3(0.0, 4.0, 0.0)
)

private val actuated = listOf(
    Vector3(0.0, 0.0, 0.0),
    Vector3(4.0, 0.0, 0.0),
    Vector3(0.0, 0.0, 4.0)
)

fun main() {
    println("Hello World!")
    println("centroid, relaxed: ${centroid(relaxed)}")
    println("centroid, actuated: ${centroid(actuated)}")
    println("hinge angle, relaxed: ${angleBetween(relaxed[1], relaxed[2])}")
    println("hinge angle, actuated: ${angleBetween(actuated[1], actuated[2])}")
    println("displacement RMS: ${rootMeanSquare(displacements(relaxed, actuated))}")
}
