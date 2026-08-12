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

package com.xemantic.nano.plentyofroom.brush

import com.xemantic.kotlin.test.assert
import com.xemantic.kotlin.test.have
import com.xemantic.kotlin.test.should
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * A brush whose Alexander-de Gennes height is exactly 50 nm,
 * chosen so the height, spacing and volume-fraction assertions are exact in decimal
 * rather than being a re-implementation of the formula under test:
 * `L0 = N a^(5/3) sigma^(1/3) = 100 * 1 * 0.125^(1/3) = 100 * 0.5 = 50`.
 */
private val roundBrush = PolymerBrush(
    monomerSize = 1.0,
    monomersPerChain = 100.0,
    graftingDensity = 0.125
)

class PolymerBrushTest {

    @Test
    fun `should return the grafting spacing as the inverse square root of the grafting density`() {
        assert(PolymerBrush(1.0, 100.0, 0.25).graftingSpacing.isCloseTo(2.0))
    }

    @Test
    fun `should return the Flory radius of the free chain`() {
        // 32^0.6 = 2^3 = 8, exactly
        assert(PolymerBrush(1.0, 32.0, 0.25).floryRadius.isCloseTo(8.0))
    }

    @Test
    fun `should return the Alexander-de Gennes equilibrium height`() {
        assert(roundBrush.alexanderDeGennesHeight.isCloseTo(50.0))
    }

    @Test
    fun `should return the mean volume fraction at the equilibrium height`() {
        // N sigma a^3 / L0 = 100 * 0.125 * 1 / 50
        assert(roundBrush.meanVolumeFraction(50.0).isCloseTo(0.25))
    }

    @Test
    fun `should double the mean volume fraction when the layer is compressed by half`() {
        assert(roundBrush.meanVolumeFraction(25.0).isCloseTo(0.5))
    }

    @Test
    fun `should invert the equilibrium height back into the chain length`() {
        val brush = brushOfHeight(
            height = 7.0,
            graftingDensity = 0.05,
            monomerSize = 0.35
        )
        assert(brush.alexanderDeGennesHeight.isCloseTo(7.0))
        assert(brush.graftingDensity == 0.05)
        assert(brush.monomerSize == 0.35)
    }

    /**
     * Gate 2 — limiting cases. The regime boundary is the reduced grafting density
     * `Sigma = sigma * pi * R_F^2`: chains overlap, and the layer is a brush, when `Sigma > 1`.
     * §4(a) of the problem definition asks for exactly this window, so the classification
     * is part of the answer rather than a convenience.
     */
    @Test
    fun `should classify a sparsely grafted layer as a mushroom`() {
        // R_F = 8, so Sigma = 1 at sigma = 1/(pi*64) = 0.004974
        assert(PolymerBrush(1.0, 32.0, 0.004).regime == GraftingRegime.MUSHROOM)
    }

    @Test
    fun `should classify a marginally overlapping layer as a crossover`() {
        assert(PolymerBrush(1.0, 32.0, 0.01).regime == GraftingRegime.CROSSOVER)
    }

    @Test
    fun `should classify a densely grafted layer as a brush`() {
        assert(PolymerBrush(1.0, 32.0, 0.05).regime == GraftingRegime.BRUSH)
    }

    @Test
    fun `should place the regime boundary at unit reduced grafting density`() {
        assert(PolymerBrush(1.0, 32.0, 0.004974).reducedGraftingDensity.isCloseTo(1.0, 1e-3))
    }

    /**
     * Gate 5 — literature cross-check, PROVISIONAL pending task `P-3`.
     *
     * Dense PEG 5 kDa brushes (N ≈ 113 ethylene-oxide units) at antifouling-grade
     * grafting densities are reported in the 10–16 nm height range. The monomer size
     * a = 0.35 nm used here is itself a number `P-3` has to source and defend;
     * until then this test pins the order of magnitude, not the value.
     */
    @Test
    fun `should reproduce the reported height range of a dense PEG 5 kDa brush`() {
        val height = PolymerBrush(
            monomerSize = 0.35,
            monomersPerChain = 113.0,
            graftingDensity = 0.3
        ).alexanderDeGennesHeight
        assert(height > 10.0)
        assert(height < 16.0)
    }

    @Test
    fun `should not accept a non-positive monomer size`() {
        assertFailsWith<IllegalArgumentException> {
            PolymerBrush(0.0, 100.0, 0.1)
        } should {
            have(message == "monomerSize must be positive, was: 0.0")
        }
    }

    @Test
    fun `should not accept a chain shorter than one monomer`() {
        assertFailsWith<IllegalArgumentException> {
            PolymerBrush(1.0, 0.5, 0.1)
        } should {
            have(message == "monomersPerChain must be at least 1, was: 0.5")
        }
    }

    @Test
    fun `should not accept a non-positive grafting density`() {
        assertFailsWith<IllegalArgumentException> {
            PolymerBrush(1.0, 100.0, 0.0)
        } should {
            have(message == "graftingDensity must be positive, was: 0.0")
        }
    }

    @Test
    fun `should not return the volume fraction at a non-positive height`() {
        assertFailsWith<IllegalArgumentException> {
            roundBrush.meanVolumeFraction(0.0)
        } should {
            have(message == "height must be positive, was: 0.0")
        }
    }

}
