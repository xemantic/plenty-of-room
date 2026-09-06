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

package com.xemantic.nano.plentyofroom.coupling

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.anchoring.AnchorMaterials
import com.xemantic.nano.plentyofroom.anchoring.FreelyJointedChain
import com.xemantic.nano.plentyofroom.anchoring.SsDnaTether
import com.xemantic.nano.plentyofroom.anchoring.rodAxialStiffness
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-16` — the **supply** side: composing DNA elements into one coupling, and finding which
 * of them is the compliance that matters. Leaf `A8.2`'s explicit ask.
 *
 * The elements themselves are `C-0014`'s and are consumed from `anchoring/` rather than
 * re-derived; what is new here is the *series* composition and the compliance share, because
 * a coupling is a chain from the tile to ground and `C-0014`'s `S5` already showed what a
 * single soft element in that chain does: a factor of 36.
 */
class CouplingElementTest {

    // ---------------------------------------------------------------- gate 1

    @Test
    fun `gate 1 dimensional consistency - series compliance adds and parallel stiffness adds`() {
        assert(seriesStiffness(listOf(2.0, 3.0)).isCloseTo(1.0 / (1.0 / 2.0 + 1.0 / 3.0)))
        assert(parallelStiffness(listOf(2.0, 3.0)).isCloseTo(5.0))
        // a single element reduces to itself under both
        assert(seriesStiffness(listOf(7.0)).isCloseTo(7.0))
        assert(parallelStiffness(listOf(7.0)).isCloseTo(7.0))
        // n identical elements in parallel are exactly n times one
        assert(parallelStiffness(List(45) { 0.74 }).isCloseTo(45 * 0.74))
    }

    @Test
    fun `gate 1 dimensional consistency - compliance shares are dimensionless and sum to one`() {
        val shares = complianceShares(listOf(55.0, 0.69, 0.91))
        assert(shares.sum().isCloseTo(1.0, 1e-12))
        shares.forEach { assert(it > 0.0 && it < 1.0) }
        // and the share is largest for the softest element, which is the whole point
        assert(shares[1] > shares[0])
    }

    @Test
    fun `gate 1 dimensional consistency - a lever reflects an output stiffness as the square of its ratio`() {
        assert(leverReflectedStiffness(10.0, 2.0).isCloseTo(40.0))
        assert(leverReflectedStiffness(10.0, 0.5).isCloseTo(2.5))
        // a rotational joint seen at a radius is a rotational stiffness over a squared length
        assert(jointStiffnessAtRadius(rotational = 400.0, radius = 20.0).isCloseTo(1.0))
    }

    @Test
    fun `gate 1 dimensional consistency - a yaw stiffness is a per-anchor stiffness times a summed squared radius`() {
        val grid = attachmentGrid(columns = 3, rows = 15, edgeX = 40.0, edgeY = 40.35)
        assert(grid.size == 45)
        val squared = grid.sumOf { (x, y) -> x * x + y * y }
        assert(yawStiffness(0.74, grid).isCloseTo(0.74 * squared))
    }

    @Test
    fun `gate 1 dimensional consistency - unphysical arguments throw`() {
        assertFailsWith<IllegalArgumentException> { seriesStiffness(emptyList()) }
        assertFailsWith<IllegalArgumentException> { seriesStiffness(listOf(1.0, 0.0)) }
        assertFailsWith<IllegalArgumentException> { parallelStiffness(listOf(-1.0)) }
        assertFailsWith<IllegalArgumentException> { attachmentGrid(0, 15, 40.0, 40.0) }
    }

    // ---------------------------------------------------------------- gate 2

    @Test
    fun `gate 2 limiting cases - a series chain is softer than its softest element and equals it in the rigid limit`() {
        val soft = 0.69
        assert(seriesStiffness(listOf(1e12, soft)).isCloseTo(soft, 1e-9))
        assert(seriesStiffness(listOf(soft, soft)).isCloseTo(soft / 2.0))
        assert(seriesStiffness(listOf(55.0, soft)) < soft)
    }

    @Test
    fun `gate 2 limiting cases - the dominant compliance is the softest element and ties break on order`() {
        val chain = listOf(
            "tether, axial" to 55.0,
            "single-duplex post" to 0.69,
            "ssDNA spacer" to 0.91
        )
        assert(dominantCompliance(chain).first == "single-duplex post")
        // an exact tie is resolved by position, never by floating-point order
        val tied = listOf("first" to 1.0, "second" to 1.0)
        assert(dominantCompliance(tied).first == "first")
    }

    @Test
    fun `gate 2 limiting cases - the ssDNA spacer reduces to its Gaussian spring at vanishing tension`() {
        val chain = FreelyJointedChain(8.0, SsDnaTether.KUHN_LENGTH_ZERO_FORCE)
        assert(chain.tangentStiffness(0.0).isCloseTo(chain.gaussianStiffness, 1e-9))
        assert(chain.gaussianStiffness.isCloseTo(3.0 * thermalEnergy() / (8.0 * 2.10), 1e-9))
        // and it stiffens without bound toward the contour length
        assert(chain.tangentStiffness(50.0) > 100.0 * chain.gaussianStiffness)
    }

    @Test
    fun `gate 2 limiting cases - the spacer contour that meets a force target at a stroke is exact in one evaluation`() {
        val contour = spacerContourForTarget(
            kuhnLength = SsDnaTether.KUHN_LENGTH_ZERO_FORCE,
            count = 45,
            targetForce = 100.0,
            targetStroke = 3.0
        )
        // the design is exact: the chain of that contour, at that tension, extends by the stroke
        val perPath = 100.0 / 45.0
        val chain = FreelyJointedChain(contour, SsDnaTether.KUHN_LENGTH_ZERO_FORCE)
        assert(chain.extension(perPath).isCloseTo(3.0, 1e-9))
        // the extension is exactly linear in the contour at fixed tension, which is why one
        // evaluation suffices and no root find is needed
        val doubled = FreelyJointedChain(2.0 * contour, SsDnaTether.KUHN_LENGTH_ZERO_FORCE)
        assert(doubled.extension(perPath).isCloseTo(6.0, 1e-9))
    }

    @Test
    fun `gate 2 limiting cases - a duplex in tension is far stiffer than the requirement and an ssDNA spacer far softer`() {
        // C-0014's own number, reproduced from S and L: a 10 nm duplex is 110 pN/nm axially
        val duplex = rodAxialStiffness(AnchorMaterials.DUPLEX_STRETCH_MODULUS, 10.0)
        assert(duplex.isCloseTo(110.0))
        // 45 of them is 150x the 33.3 pN/nm §3 mandates — the coupling's problem is being
        // too STIFF, not too soft
        assert(45 * duplex > 100.0 * mandatedCouplingStiffness(100.0, 3.0))
        // while 45 slack 80-nt spacers are 6.5x too soft, so the design target sits BETWEEN
        // the two element classes C-0014 evaluated and neither of its schemes hits it
        val slack = FreelyJointedChain(80 * SsDnaTether.CONTOUR_PER_NUCLEOTIDE, 2.10)
        assert(45 * slack.gaussianStiffness < 0.2 * mandatedCouplingStiffness(100.0, 3.0))
    }

    // ---------------------------------------------------------------- gate 3

    @Test
    fun `gate 3 symmetry - the convexity bound holds for every spacer state the design visits`() {
        val chain = FreelyJointedChain(10.0, SsDnaTether.KUHN_LENGTH_ZERO_FORCE)
        listOf(0.01, 0.1, 1.0, 2.22, 5.0, 20.0).forEach { force ->
            val secant = chain.transverseStiffness(force)
            val tangent = chain.tangentStiffness(force)
            // C-0014's theorem, in the direction T-16 needs it: lateral is at most normal
            assert(secant <= tangent * (1.0 + 1e-9))
        }
    }

    @Test
    fun `gate 3 symmetry - the per-anchor thermal force grows as the square root of the stiffness`() {
        val four = perAnchorThermalForce(0.460216, 4)
        val hundredFold = perAnchorThermalForce(100.0 * 0.460216, 4)
        assert((hundredFold / four).isCloseTo(10.0, 1e-9))
        // C-0014's own minimum-design figure, reproduced
        assert(four.isCloseTo(0.345, 1e-2))
    }
}
