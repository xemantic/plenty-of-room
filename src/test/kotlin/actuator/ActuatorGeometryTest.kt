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

package com.xemantic.nano.plentyofroom.actuator

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * The one thing `T-3` has to fix before it derives anything: which length the electrostatics
 * sees and which length the layer mechanics sees, and where the tile's faces are.
 */
class ActuatorGeometryTest {

    private val geometry = ActuatorGeometry()

    @Test
    fun `gate 1 dimensional consistency - the footprint should be the tile edge squared in nm2`() {
        assert(geometry.footprintArea.isCloseTo(1600.0))
        assert(ActuatorGeometry(tileEdge = 70.0, tileThickness = 10.0).footprintArea.isCloseTo(4900.0))
    }

    @Test
    fun `gate 1 dimensional consistency - the electrostatic gap should BE the layer height, not a separate length`() {
        // this is the relation T-3 must fix explicitly: the tile's bottom face rests on the
        // layer's outer surface, so the tile-electrode separation IS the layer height
        listOf(3.0, 5.0, 7.0, 10.0).forEach { height ->
            assert(geometry.electrostaticGap(height) == height)
            assert(geometry.tileBottomFace(height) == height)
        }
    }

    @Test
    fun `gate 2 limiting cases - the tile faces should be one thickness apart at every layer height`() {
        listOf(2.5, 5.0, 7.0, 10.0).forEach { height ->
            assert(
                (geometry.tileTopFace(height) - geometry.tileBottomFace(height))
                    .isCloseTo(geometry.tileThickness)
            )
        }
    }

    @Test
    fun `gate 5 cross-check - section 3's 20-25 nm effort point should be reproduced by the section 3 layer heights`() {
        // §3: tile ~10 nm thick, "effort point may sit ~20-25 nm above the electrode",
        // polymer layer 5 / 7 / 10 nm. With a 5 nm lever attachment above the top face the
        // three §3 layer heights land at exactly 20 / 22 / 25 nm — the §3 band, both ends.
        assert(geometry.effortPointHeight(5.0).isCloseTo(20.0))
        assert(geometry.effortPointHeight(7.0).isCloseTo(22.0))
        assert(geometry.effortPointHeight(10.0).isCloseTo(25.0))
        assert(geometry.effortPointHeight(5.0) >= 20.0)
        assert(geometry.effortPointHeight(10.0) <= 25.0)
    }

    @Test
    fun `gate 2 limiting cases - a lever bonded straight onto the top face should drop the effort point by its attachment height`() {
        val bonded = ActuatorGeometry(leverAttachmentHeight = 0.0)
        listOf(5.0, 7.0, 10.0).forEach { height ->
            assert(bonded.effortPointHeight(height).isCloseTo(geometry.effortPointHeight(height) - 5.0))
            assert(bonded.effortPointHeight(height).isCloseTo(bonded.tileTopFace(height)))
        }
    }

    @Test
    fun `gate 1 dimensional consistency - unphysical geometry should be rejected on construction`() {
        assertFailsWith<IllegalArgumentException> { ActuatorGeometry(tileEdge = 0.0) }
        assertFailsWith<IllegalArgumentException> { ActuatorGeometry(tileThickness = -1.0) }
        assertFailsWith<IllegalArgumentException> { ActuatorGeometry(leverAttachmentHeight = -1.0) }
        assertFailsWith<IllegalArgumentException> { geometry.electrostaticGap(0.0) }
    }

}
