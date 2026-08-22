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

package com.xemantic.nano.plentyofroom.structure

import com.xemantic.nano.plentyofroom.coupling.winklerBendingLength
import com.xemantic.nano.plentyofroom.tile.HoneycombCrossSectionGeometry
import com.xemantic.nano.plentyofroom.tile.LayerCoupling
import com.xemantic.nano.plentyofroom.tile.multiLayerRigidities
import com.xemantic.kotlin.test.assert
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-258` — the ragged face at the relief the DRAWABLE raster carries.
 *
 * Written before `structure/DrawableRaggedFace.kt` existed and watched fail.
 */
class DrawableRaggedFaceTest {

    private val rise = Gen1Tile.RISE_PER_BASE_PAIR

    // --------------------------------------------------------------- gate 5, upstream

    @Test
    fun `gate 5 - the relief reproduces C-0147's 4 and 8 bp at the undrawable 112 - 108 pair`() {
        val relief = raggedFaceRelief(rasterRows = 15, helicesPerRow = 4, senseOne = 112, senseTwo = 108)
        assert(relief.frontBasePairs == 4)
        assert(relief.rearBasePairs == 8)
        assert(relief.axialExtentBasePairs == 116)
    }

    @Test
    fun `gate 5 - the identical 4 and 8 appear on the other 60-helix cross-section`() {
        val relief = raggedFaceRelief(rasterRows = 10, helicesPerRow = 6, senseOne = 112, senseTwo = 108)
        assert(relief.frontBasePairs == 4)
        assert(relief.rearBasePairs == 8)
    }

    // --------------------------------------------------------------- gate 2, the drawable pair

    @Test
    fun `gate 2 - the drawable 102 - 109 pair carries 7 and 14 bp of relief`() {
        listOf(15 to 4, 10 to 6).forEach { (rows, perRow) ->
            val relief = raggedFaceRelief(rows, perRow, senseOne = 102, senseTwo = 109)
            assert(relief.frontBasePairs == 7)
            assert(relief.rearBasePairs == 14)
            assert(relief.axialExtentBasePairs == 116)
        }
    }

    @Test
    fun `gate 3 - both length-to-sense assignments give the same spreads and the same extent`() {
        val forward = raggedFaceRelief(10, 6, senseOne = 102, senseTwo = 109)
        val reverse = raggedFaceRelief(10, 6, senseOne = 109, senseTwo = 102)
        assert(forward.frontBasePairs == reverse.frontBasePairs)
        assert(forward.rearBasePairs == reverse.rearBasePairs)
        assert(forward.axialExtentBasePairs == reverse.axialExtentBasePairs)
    }

    @Test
    fun `gate 2 - a uniform row length leaves both faces flat`() {
        val relief = raggedFaceRelief(10, 6, senseOne = 112, senseTwo = 112)
        assert(relief.frontBasePairs == 0)
        assert(relief.rearBasePairs == 0)
        assert(relief.axialExtentBasePairs == 112)
    }

    // --------------------------------------------------------------- gate 1, dimensional

    @Test
    fun `gate 1 - a relief in nm is its base-pair count times the rise and above one rise`() {
        val relief = raggedFaceRelief(10, 6, senseOne = 102, senseTwo = 109)
        assert(abs(relief.frontNm - 7 * rise) < 1e-12)
        assert(abs(relief.rearNm - 14 * rise) < 1e-12)
        assert(relief.frontNm > rise)
    }

    @Test
    fun `gate 1 - the relief and the modulation are DIFFERENT quantities and only one scales`() {
        val four = raggedFaceRelief(10, 6, 112, 108)
        val seven = raggedFaceRelief(10, 6, 102, 109)
        assert(seven.gapFacingRimPeriodRows == four.gapFacingRimPeriodRows)
        assert(seven.frontBasePairs > four.frontBasePairs)
    }

    // --------------------------------------------------------------- gate 2, the axis

    @Test
    fun `gate 2 - every column of the cross-section carries the same ragged rim`() {
        val relief = raggedFaceRelief(10, 6, senseOne = 102, senseTwo = 109)
        assert(relief.spreadByColumn.size == 6)
        assert(relief.spreadByColumn.all { it <= relief.frontBasePairs + relief.rearBasePairs })
        assert(relief.spreadByColumn.max() > 0)
    }

    // --------------------------------------------------------------- gate 1 and 3, the bound

    @Test
    fun `gate 1 - the rim modulation bound is exactly linear in the relief`() {
        val ell = 20.0
        val small = rimModulationBound(1.36, 19.04, ell, 7.608, 50.0)
        val large = rimModulationBound(2.38, 19.04, ell, 7.608, 50.0)
        assert(abs(large / small - 7.0 / 4.0) < 1e-12)
    }

    @Test
    fun `gate 3 - the bound reproduces C-0147's own product at 4 bp on both cross-sections`() {
        val rowPitch = HoneycombCrossSectionGeometry.rowPitch()
        val columnPitch = HoneycombCrossSectionGeometry.columnPitch()
        listOf(4 to 5.54399427e-5, 6 to 1.68371917e-5).forEach { (layers, published) ->
            val rigidities = multiLayerRigidities(
                layers = layers,
                interhelicalDistance = rowPitch,
                crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
                coupling = LayerCoupling.CALIBRATED,
                compositeFraction = 0.30,
                layerSpacing = columnPitch
            )
            val ell = winklerBendingLength(
                rigidities.acrossHelixRigidity, Gen1Tile.FOUNDATION_SECANT
            )
            val bound = rimModulationBound(4 * rise, 112 * rise / 2.0, ell, 7.608, 50.0)
            assert(abs(bound - published) / published < 1e-6)
        }
    }

    @Test
    fun `gate 1 - the bound refuses a non-positive relief span or wavelength`() {
        assertFailsWith<IllegalArgumentException> { rimModulationBound(1.0, 0.0, 20.0, 7.6, 50.0) }
        assertFailsWith<IllegalArgumentException> { rimModulationBound(-1.0, 19.0, 20.0, 7.6, 50.0) }
    }

    @Test
    fun `gate 2 - a zero relief bounds the flatness move at exactly zero`() {
        assert(rimModulationBound(0.0, 19.04, 20.0, 7.608, 50.0) == 0.0)
    }

    // --------------------------------------------------------------- gate 3, guards

    @Test
    fun `gate 1 - the relief refuses a non-positive row length and an odd helix count`() {
        assertFailsWith<IllegalArgumentException> { raggedFaceRelief(10, 6, 0, 109) }
        assertFailsWith<IllegalArgumentException> { raggedFaceRelief(10, 5, 102, 109) }
    }
}
