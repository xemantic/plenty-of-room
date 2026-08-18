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

package com.xemantic.nano.plentyofroom.tile

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-199` — is `10 × 6` a better tile than `15 × 4`?
 *
 * Douglas et al.'s seven honeycomb blocks are `m × n`: `m` x-raster rows of `n` helices. This
 * programme's tile is design (i), `15 × 4`, and the paper's own conclusion is that `10 × 6` folds
 * to the greatest fraction of defect-free objects. Both are **60** helices, so the comparison is
 * at fixed scaffold length — which is what makes it a free design choice and worth testing.
 *
 * These tests are the part that must hold before any plate is solved: that the two cross-sections
 * are the same body count, that the second moment and therefore the parallel-axis factor move
 * with the layer count, and that `C-0116`'s threshold — a function of that factor — moves with
 * them rather than being a constant of the material.
 */
class CrossSectionComparisonTest {

    // --- gate 1: dimensional consistency and the invariant that makes this a fair comparison ---

    @Test
    fun `both cross-sections are sixty helices, so the scaffold budget is unchanged`() {
        assert(crossSectionHelices(rows = 15, layers = 4) == 60)
        assert(crossSectionHelices(rows = 10, layers = 6) == 60)
        // And two of the paper's seven are NOT, which is why the family is folded from two
        // scaffolds and why only THIS pair is a fixed-budget comparison.
        assert(crossSectionHelices(rows = 8, layers = 8) == 64)
        assert(crossSectionHelices(rows = 4, layers = 16) == 64)
    }

    @Test
    fun `a cross-section needs at least one row and one layer`() {
        assertFailsWith<IllegalArgumentException> { crossSectionHelices(0, 4) }
        assertFailsWith<IllegalArgumentException> { crossSectionHelices(15, 0) }
    }

    // --- gate 2: limiting cases -----------------------------------------------------------------

    @Test
    fun `one layer has no parallel-axis excess at all`() {
        val single = multiLayerRigidities(
            layers = 1,
            interhelicalDistance = Gen1Tile.INTERHELICAL_HONEYCOMB,
            crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
            coupling = LayerCoupling.CALIBRATED
        )
        assert(single.parallelAxisFactor.isCloseTo(1.0))
        // And the threshold is then undefined rather than infinite -- `C-0031`'s rule.
        assertFailsWith<IllegalArgumentException> {
            fractionForEnhancement(1.5, single.parallelAxisFactor)
        }
    }

    // --- gate 3: the parallel-axis factor moves with the LAYER count, not the helix count -------

    @Test
    fun `six layers have a larger parallel-axis factor than four at the same helix count`() {
        fun factor(layers: Int) = multiLayerRigidities(
            layers = layers,
            interhelicalDistance = Gen1Tile.INTERHELICAL_HONEYCOMB,
            crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
            coupling = LayerCoupling.CALIBRATED
        ).parallelAxisFactor
        val four = factor(4)
        val six = factor(6)
        assert(six > four)
        // `Sigma y^2` for `n` layers at unit spacing is `n(n^2 - 1)/12`, so the factor's excess
        // scales as `(n^2 - 1)/12` -- the ratio is a pure integer function of the layer count and
        // contains no material constant at all.
        val expected = (6.0 * 6.0 - 1.0) / (4.0 * 4.0 - 1.0)
        assert(abs((six - 1.0) / (four - 1.0) - expected) < 1e-9)
    }

    @Test
    fun `so C-0116's threshold is a function of the cross-section, not a constant`() {
        // The enhancement needed to reach a given plate stiffness is the same number; the FRACTION
        // that delivers it is not, because the factor differs. A larger factor means a SMALLER
        // fraction suffices -- which is the direction that matters for the flatness margin.
        val fourFactor = 39.4479652
        val sixFactor = 100.0
        val enhancementNeeded = 4.03207885
        val fourFraction = fractionForEnhancement(enhancementNeeded, fourFactor)
        val sixFraction = fractionForEnhancement(enhancementNeeded, sixFactor)
        assert(sixFraction < fourFraction)
    }
}
