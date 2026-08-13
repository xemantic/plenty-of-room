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

package com.xemantic.nano.plentyofroom.anchoring

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.PI
import kotlin.math.sqrt
import kotlin.test.Test

/**
 * `T-12` — assembling anchor links into the tile's four rigid-body coordinates.
 *
 * The assembly is three lines of algebra, and all three are places the answer could be
 * silently wrong: the axis projection (which decides whether an element resists in tension
 * or in bending), the yaw lever arm (which is what makes a *central* anchor useless), and
 * the sign convention on `theta`, measured here from the surface **normal**.
 */
class AnchorSchemeTest {

    /** Four isotropic links at the corners of the §3 footprint — the reference arrangement. */
    private fun cornerLinks(stiffness: Double): List<AnchorLink> = listOf(
        20.0 to 20.0, -20.0 to 20.0, -20.0 to -20.0, 20.0 to -20.0
    ).map { (x, y) ->
        AnchorLink(
            name = "isotropic",
            axialStiffness = stiffness,
            transverseStiffness = stiffness,
            polarAngle = 0.0,
            azimuth = 0.0,
            attachmentX = x,
            attachmentY = y
        )
    }

    // ---------------------------------------------------------------- gate 1

    @Test
    fun `gate 1 dimensional consistency - a yaw stiffness should be a lateral stiffness times a squared radius`() {
        val assembly = AnchorAssembly(cornerLinks(0.5))
        val radiusSquared = 20.0 * 20.0 + 20.0 * 20.0
        assert(assembly.yawStiffness.isCloseTo(assembly.lateralStiffnessX * radiusSquared))
        // and it scales as the square of the arrangement's radius, exactly
        val wide = AnchorAssembly(
            cornerLinks(0.5).map { it.copy(attachmentX = it.attachmentX * 2.0, attachmentY = it.attachmentY * 2.0) }
        )
        assert((wide.yawStiffness / assembly.yawStiffness).isCloseTo(4.0))
    }

    // ---------------------------------------------------------------- gate 2

    @Test
    fun `gate 2 limiting cases - a link along the normal should put its axial stiffness in z and its transverse in the plane`() {
        val vertical = AnchorLink(
            name = "vertical strut",
            axialStiffness = 110.0,
            transverseStiffness = 0.69,
            polarAngle = 0.0,
            azimuth = 0.0,
            attachmentX = 0.0,
            attachmentY = 0.0
        )
        val assembly = AnchorAssembly(listOf(vertical))
        assert(assembly.normalStiffness.isCloseTo(110.0))
        assert(assembly.lateralStiffnessX.isCloseTo(0.69))
        assert(assembly.lateralStiffnessY.isCloseTo(0.69))
    }

    @Test
    fun `gate 2 limiting cases - a link in the plane should swap them exactly`() {
        val horizontal = AnchorLink(
            name = "in-plane tether",
            axialStiffness = 55.0,
            transverseStiffness = 0.345,
            polarAngle = PI / 2.0,
            azimuth = 0.0,
            attachmentX = 20.0,
            attachmentY = 0.0
        )
        val assembly = AnchorAssembly(listOf(horizontal))
        assert(assembly.normalStiffness.isCloseTo(0.345))
        assert(assembly.lateralStiffnessX.isCloseTo(55.0))
        assert(assembly.lateralStiffnessY.isCloseTo(0.345))
    }

    @Test
    fun `gate 2 limiting cases - a single central anchor should have no yaw stiffness whatever its lateral stiffness`() {
        // the reason a scheme can pin translation and still leave the tile free to rotate
        val central = AnchorLink(
            name = "central",
            axialStiffness = 1e6,
            transverseStiffness = 1e6,
            polarAngle = 0.0,
            azimuth = 0.0,
            attachmentX = 0.0,
            attachmentY = 0.0
        )
        val assembly = AnchorAssembly(listOf(central))
        assert(assembly.lateralStiffnessX.isCloseTo(1e6))
        assert(assembly.yawStiffness.isCloseTo(0.0))
    }

    @Test
    fun `gate 2 limiting cases - purely radial in-plane links should give yaw only through their bending`() {
        // four radial struts at the corners: the tangential direction sees the TRANSVERSE
        // stiffness only, so the yaw lever works on the bending stiffness, not the axial one
        val radial = radialInPlaneLinks(
            axialStiffness = 55.0, transverseStiffness = 0.345, radius = 20.0 * sqrt(2.0)
        )
        val assembly = AnchorAssembly(radial)
        assert(assembly.yawStiffness.isCloseTo(4.0 * 0.345 * 800.0))
        // while translation gets the mean of axial and transverse, by symmetry
        assert(assembly.lateralStiffnessX.isCloseTo(2.0 * (55.0 + 0.345)))
    }

    // ---------------------------------------------------------------- gate 3

    @Test
    fun `gate 3 symmetry - a four-fold symmetric arrangement should have no lateral coupling and equal axes`() {
        val assembly = AnchorAssembly(
            radialInPlaneLinks(axialStiffness = 55.0, transverseStiffness = 0.345, radius = 28.284271247461902)
        )
        assert(assembly.lateralStiffnessX.isCloseTo(assembly.lateralStiffnessY))
        assert(assembly.lateralCoupling.isCloseTo(0.0))
    }

    @Test
    fun `gate 3 symmetry - the trace of a link stiffness should be invariant under its orientation`() {
        // k_xx + k_yy + k_zz = k_a + 2 k_t whatever the axis: an invariant of the projector,
        // and the strongest check available on the assembly, because it is orientation-free
        listOf(0.0, 0.3, PI / 4.0, 1.2, PI / 2.0).forEach { polar ->
            listOf(0.0, 0.7, 2.5).forEach { azimuth ->
                val link = AnchorLink(
                    name = "arbitrary",
                    axialStiffness = 55.0,
                    transverseStiffness = 0.345,
                    polarAngle = polar,
                    azimuth = azimuth,
                    attachmentX = 3.0,
                    attachmentY = -7.0
                )
                val assembly = AnchorAssembly(listOf(link))
                val trace = assembly.lateralStiffnessX + assembly.lateralStiffnessY +
                        assembly.normalStiffness
                assert(trace.isCloseTo(55.0 + 2.0 * 0.345))
            }
        }
    }

    @Test
    fun `gate 3 symmetry - adding links should add their stiffnesses in every coordinate`() {
        val first = cornerLinks(0.3)
        val second = cornerLinks(0.7)
        val combined = AnchorAssembly(first + second)
        assert(
            combined.lateralStiffnessX.isCloseTo(
                AnchorAssembly(first).lateralStiffnessX + AnchorAssembly(second).lateralStiffnessX
            )
        )
        assert(
            combined.yawStiffness.isCloseTo(
                AnchorAssembly(first).yawStiffness + AnchorAssembly(second).yawStiffness
            )
        )
    }
}
