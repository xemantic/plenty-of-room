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

import com.xemantic.kotlin.test.assert
import com.xemantic.kotlin.test.have
import com.xemantic.kotlin.test.should
import org.jetbrains.bio.viktor.asF64Array
import org.openrndr.math.Vector3
import kotlin.math.PI
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * The tolerance of the floating point comparisons which are not exact in binary.
 */
private const val EPSILON = 1e-12

class VectorsTest {

    @Test
    fun `should return the centroid of the points`() {
        centroid(
            listOf(
                Vector3(0.0, 0.0, 0.0),
                Vector3(2.0, 0.0, 6.0),
                Vector3(1.0, 3.0, 0.0)
            )
        ) should {
            have(x == 1.0)
            have(y == 1.0)
            have(z == 2.0)
        }
    }

    @Test
    fun `should return the single point as its own centroid`() {
        centroid(listOf(Vector3(1.5, -2.0, 0.25))) should {
            have(x == 1.5)
            have(y == -2.0)
            have(z == 0.25)
        }
    }

    @Test
    fun `should not return the centroid of no points`() {
        assertFailsWith<IllegalArgumentException> {
            centroid(emptyList())
        } should {
            have(message == "points cannot be empty")
        }
    }

    @Test
    fun `should return the right angle between perpendicular vectors`() {
        val angle = angleBetween(Vector3.UNIT_X, Vector3.UNIT_Y)
        assert(abs(angle - PI / 2) < EPSILON)
    }

    @Test
    fun `should return no angle between collinear vectors`() {
        val angle = angleBetween(Vector3.UNIT_Z, Vector3.UNIT_Z * 42.0)
        assert(angle == 0.0)
    }

    @Test
    fun `should return the straight angle between opposite vectors`() {
        val angle = angleBetween(Vector3.UNIT_X, -Vector3.UNIT_X)
        assert(abs(angle - PI) < EPSILON)
    }

    @Test
    fun `should not return the angle of a zero length vector`() {
        assertFailsWith<IllegalArgumentException> {
            angleBetween(Vector3.ZERO, Vector3.UNIT_X)
        } should {
            have(message == "vectors cannot be of zero length")
        }
    }

    @Test
    fun `should return the displacement of each point`() {
        displacements(
            from = listOf(
                Vector3(0.0, 0.0, 0.0),
                Vector3(1.0, 1.0, 1.0)
            ),
            to = listOf(
                Vector3(3.0, 4.0, 0.0),
                Vector3(1.0, 1.0, 1.0)
            )
        ) should {
            have(length == 2)
            have(this[0] == 5.0)
            have(this[1] == 0.0)
        }
    }

    @Test
    fun `should not return displacements of point lists of different size`() {
        assertFailsWith<IllegalArgumentException> {
            displacements(
                from = listOf(Vector3.ZERO),
                to = listOf(Vector3.ZERO, Vector3.UNIT_X)
            )
        } should {
            have(message == "from and to must be of equal size, was: 1 and 2")
        }
    }

    @Test
    fun `should return the root mean square of the values`() {
        val rms = rootMeanSquare(doubleArrayOf(3.0, 4.0, 0.0, 0.0).asF64Array())
        assert(rms == 2.5)
    }

}
