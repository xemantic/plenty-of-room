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
import com.xemantic.nano.plentyofroom.equipartitionRms
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.test.Test
import kotlin.test.assertFailsWith

/** §3 of the problem definition: 40 × 40 nm tile, ≥ 100 pN target force, PEG. */
private const val TILE_AREA = 1600.0
private const val TARGET_FORCE = 100.0
private const val PEG_MONOMER_SIZE = 0.35

private fun point(
    layerHeight: Double = 10.0,
    graftingDensity: Double = 0.03
) = layerDesignPoint(
    layerHeight = layerHeight,
    graftingDensity = graftingDensity,
    monomerSize = PEG_MONOMER_SIZE,
    tileArea = TILE_AREA,
    targetForce = TARGET_FORCE
)

class LayerDesignPointTest {

    @Test
    fun `should build the design point at the requested layer height`() {
        point(layerHeight = 7.0) should {
            have(layerHeight == 7.0)
            have(graftingDensity == 0.03)
            have(monomersPerChain > 0.0)
        }
    }

    @Test
    fun `should report every model at the same layer height`() {
        val designPoint = point()
        assert(designPoint.responses.isNotEmpty())
        designPoint.responses.forEach { response ->
            assert(response.equilibriumHeight.isCloseTo(designPoint.layerHeight, 1e-9))
        }
    }

    @Test
    fun `should cover the three osmotic exponents and the SCF form`() {
        assert(
            point().responses.map { it.model } == listOf(
                "de-gennes-scaling(m=9/4, good-solvent semidilute)",
                "de-gennes-scaling(m=2, mean-field)",
                "de-gennes-scaling(m=3, concentrated/theta)",
                "milner-witten-cates-scf(height-matched)"
            )
        )
    }

    @Test
    fun `should close the stroke against the layer height`() {
        point().responses.forEach { response ->
            assert(
                (response.heightUnderTargetForce + response.strokeUnderTargetForce)
                    .isCloseTo(response.equilibriumHeight, 1e-9)
            )
        }
    }

    @Test
    fun `should close the secant stiffness against the target force and the stroke`() {
        point().responses.forEach { response ->
            assert(
                (response.secantStiffness * response.strokeUnderTargetForce)
                    .isCloseTo(TARGET_FORCE, 1e-9)
            )
        }
    }

    @Test
    fun `should report a tangent stiffness above the secant stiffness at the working point`() {
        // the layer stiffens under compression, so the tangent at the working point
        // must exceed the secant averaged over the approach to it
        point().responses.forEach { response ->
            assert(response.tangentStiffness > response.secantStiffness)
        }
    }

    @Test
    fun `should report the positional noise of the tile at the working point`() {
        point().responses.forEach { response ->
            assert(
                response.positionalRms.isCloseTo(equipartitionRms(response.tangentStiffness))
            )
        }
    }

    @Test
    fun `should shorten the stroke when the layer is grafted more densely`() {
        val sparse = point(graftingDensity = 0.02)
        val dense = point(graftingDensity = 0.20)
        sparse.responses.zip(dense.responses).forEach { (a, b) ->
            assert(b.strokeUnderTargetForce < a.strokeUnderTargetForce)
        }
    }

    /**
     * The acceptance predicate of task 1 requires the sensitivity to grafting density,
     * reported here as the local log-log slope `d ln k / d ln σ` of the secant stiffness.
     * For the scaling form deep in compression the osmotic term dominates and the slope
     * approaches the closed-form equilibrium value `7/6`; it is reported rather than
     * assumed, because at the working compression the elastic term still contributes.
     */
    @Test
    fun `should report a positive grafting-density sensitivity`() {
        point().responses.forEach { response ->
            assert(response.stiffnessSensitivity > 0.0)
        }
    }

    @Test
    fun `should classify the grafting regime`() {
        assert(point(graftingDensity = 0.30).regime == GraftingRegime.BRUSH.name)
    }

    @Test
    fun `should report the mean volume fraction of the unperturbed layer`() {
        val designPoint = point()
        val brush = brushOfHeight(10.0, 0.03, PEG_MONOMER_SIZE)
        assert(designPoint.meanVolumeFraction.isCloseTo(brush.meanVolumeFraction(10.0)))
    }

    @Test
    fun `should not build a design point without a target force`() {
        assertFailsWith<IllegalArgumentException> {
            layerDesignPoint(10.0, 0.03, PEG_MONOMER_SIZE, TILE_AREA, targetForce = 0.0)
        } should {
            have(message == "targetForce must be positive, was: 0.0")
        }
    }

}
