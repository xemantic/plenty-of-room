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

package com.xemantic.nano.plentyofroom.poroelastic

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.brush.PolymerBrush
import com.xemantic.nano.plentyofroom.brush.brushOfHeight
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.material.PegWater
import kotlin.math.PI
import kotlin.math.pow
import kotlin.test.Test

/**
 * How many blobs deep the Gen-1 layer actually is — the structural twin of `T-7`'s
 * hydrodynamic question, and the ground of challenge `CH-0003`.
 *
 * `T-7` needs the layer's hydrodynamic screening length, and for a brush that length is
 * the Alexander blob, which *is* the grafting spacing. Asking how many screening lengths
 * fit across the layer turns out to be the same question as asking how many blobs the
 * Alexander-de Gennes chain is subdivided into across the layer height, and the answer —
 * one and a half — is not what a brush is supposed to look like.
 *
 * These tests are in the `poroelastic` package because `T-7` owns it this session and the
 * finding came out of `T-7`'s permeability work; the identity is about brush geometry and
 * belongs to `T-1c` once that lands.
 */
class BlobStackHeightTest {

    private val peg = PegWater()

    @Test
    fun `should make the blob stack height an exact function of the reduced grafting density`() {
        // gate 3, an exact symmetry rather than a numerical observation:
        //   L0/s = N a^(5/3) sigma^(5/6)   and   Sigma = pi a^2 N^(6/5) sigma
        // so    L0/s = (Sigma/pi)^(5/6)
        // identically — the monomer size, the chain length and the grafting density all
        // cancel. This is the same kind of statement C-0002 proved for phi/phi#, and it
        // is proved here rather than observed.
        listOf(
            PolymerBrush(monomerSize = 0.35, monomersPerChain = 199.44, graftingDensity = 0.024),
            PolymerBrush(monomerSize = 0.35, monomersPerChain = 64.0, graftingDensity = 0.092),
            PolymerBrush(monomerSize = 0.28, monomersPerChain = 1000.0, graftingDensity = 0.5),
            PolymerBrush(monomerSize = 0.55, monomersPerChain = 20.0, graftingDensity = 0.003)
        ).forEach { brush ->
            val stackHeight = brush.alexanderDeGennesHeight / brush.graftingSpacing
            val fromReducedDensity = (brush.reducedGraftingDensity / PI).pow(5.0 / 6.0)
            assert(stackHeight.isCloseTo(fromReducedDensity, relativeTolerance = 1e-12))
        }
    }

    @Test
    fun `should make the conventional brush onset exactly one point four seven blobs tall`() {
        // The finding. At the Sigma = 5 convention that C-0001 uses to set the lower edge
        // of its design window, the Alexander-de Gennes layer is (5/pi)^(5/6) = 1.473
        // grafting spacings tall — for every polymer, every chain length, every thickness.
        // A "stack of blobs" with 1.5 blobs in it is not a stack.
        val atOnset = (PolymerBrush.BRUSH_ONSET / PI).pow(5.0 / 6.0)
        assert(atOnset.isCloseTo(1.4729345, relativeTolerance = 1e-7))
    }

    @Test
    fun `should leave every surviving design point below two blobs tall`() {
        // gate 5, against our own standing claim rather than a textbook: the four C-0001
        // design points that survive into C-0002 are 1.47 to 1.73 blobs tall.
        val stackHeights = listOf(
            5.0 to 0.092,
            7.0 to 0.045,
            10.0 to 0.024,
            10.0 to 0.030
        ).map { (height, density) ->
            val brush = brushOfHeight(height, density, peg.effectiveMonomerLength)
            brush.alexanderDeGennesHeight / brush.graftingSpacing
        }
        assert(stackHeights.min().isCloseTo(1.4849, relativeTolerance = 1e-3))
        assert(stackHeights.max().isCloseTo(1.7321, relativeTolerance = 1e-3))
        assert(stackHeights.all { it < 2.0 })
    }

    @Test
    fun `should need a reduced grafting density of fifty for a ten-blob stack`() {
        // What it would take to make the blob stack a stack: inverting the identity,
        // Sigma = pi (L0/s)^(6/5), so ten blobs needs Sigma = 49.8, ten times the
        // conventional onset. C-0002 already shows that the densities which reach the
        // des Cloizeaux regime are ruled out by §4(a) for stiffness; this is the same
        // tension arriving from the geometry instead of from the thermodynamics.
        val required = PI * 10.0.pow(6.0 / 5.0)
        assert(required.isCloseTo(49.791, relativeTolerance = 1e-4))
        assert((required / PolymerBrush.BRUSH_ONSET).isCloseTo(9.958, relativeTolerance = 1e-3))
    }

    @Test
    fun `should identify the blob with the hydrodynamic screening length to within a sixth`() {
        // Why this belongs to T-7 as well as to T-1c: de Gennes argues that hydrodynamic
        // and excluded-volume screening share one length. Taking that literally, the
        // layer is 1.55 screening lengths tall at the design point on the blob picture —
        // and the independent correlation-length permeability model puts it at 1.79.
        // Two routes to "this layer is under two screening lengths thick".
        val brush = brushOfHeight(10.0, 0.024, peg.effectiveMonomerLength)
        val blobStack = brush.alexanderDeGennesHeight / brush.graftingSpacing
        val screening = CorrelationLengthScreening(peg.volumetricMonomerSize)
            .screeningLength(peg.volumeFraction(brush.monomersPerChain, 0.024, 10.0))
        val screeningStack = 10.0 / screening
        assert(blobStack.isCloseTo(1.5493, relativeTolerance = 1e-3))
        assert(screeningStack.isCloseTo(1.7864, relativeTolerance = 1e-3))
        assert(screeningStack / blobStack < 1.2)
    }

}
