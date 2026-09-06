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

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.isCloseTo
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * Deliberately round numbers, so that every assertion below is a closed form
 * rather than a regression against a previous run. The literature values live in the
 * study entry points, where they are logged with their provenance.
 */
private val duplex = DnaDuplex(
    bendingRigidity = 200.0,
    torsionalRigidity = 400.0,
    stretchModulus = 1000.0
)

private val sheet = OrigamiSheet(
    duplex = duplex,
    interhelicalDistance = 2.5,
    crossoverSpacing = 5.0,
    crossoverHingeStiffness = 100.0
)

class OrigamiSheetTest {

    // ---------------------------------------------------------------- gate 1

    @Test
    fun `gate 1 dimensional consistency - a persistence length times kT should give a bending rigidity`() {
        val fromPersistence = duplexOfPersistenceLengths(
            bendingPersistenceLength = 50.0,
            torsionalPersistenceLength = 100.0,
            stretchModulus = 1100.0
        )
        assert(fromPersistence.bendingRigidity.isCloseTo(50.0 * thermalEnergy()))
        assert(fromPersistence.torsionalRigidity.isCloseTo(100.0 * thermalEnergy()))
        assert(fromPersistence.stretchModulus.isCloseTo(1100.0))
    }

    @Test
    fun `gate 1 dimensional consistency - the along-helix rigidity should be EI per interhelical distance`() {
        assert(sheet.alongHelixRigidity.isCloseTo(200.0 / 2.5))
    }

    @Test
    fun `gate 1 dimensional consistency - the across-helix rigidity should be the hinge stiffness per crossover density`() {
        assert(sheet.acrossHelixRigidity.isCloseTo(100.0 * 2.5 / 5.0))
    }

    @Test
    fun `gate 1 dimensional consistency - the twisting rigidity should be a quarter of the torsion per unit width`() {
        assert(sheet.twistingRigidity.isCloseTo(400.0 / (4.0 * 2.5)))
    }

    // ---------------------------------------------------------------- gate 2

    @Test
    fun `gate 2 limiting cases - a stiffer crossover should stiffen only the across-helix direction`() {
        val stiffer = sheet.copy(crossoverHingeStiffness = sheet.crossoverHingeStiffness * 10.0)
        assert(stiffer.acrossHelixRigidity.isCloseTo(sheet.acrossHelixRigidity * 10.0))
        assert(stiffer.alongHelixRigidity.isCloseTo(sheet.alongHelixRigidity))
    }

    @Test
    fun `gate 2 limiting cases - sparser crossovers should soften the across-helix direction proportionally`() {
        val sparse = sheet.copy(crossoverSpacing = sheet.crossoverSpacing * 2.0)
        assert(sparse.acrossHelixRigidity.isCloseTo(sheet.acrossHelixRigidity / 2.0))
    }

    @Test
    fun `gate 2 limiting cases - uncoupled layers should add linearly and coupled layers by the parallel axis theorem`() {
        val uncoupled = sheet.copy(layers = 2, layerSpacing = 2.5)
        assert(uncoupled.alongHelixRigidity.isCloseTo(2.0 * sheet.alongHelixRigidity))
        val coupled = uncoupled.copy(interlayerCoupling = InterlayerCoupling.RIGID)
        // z = ±1.25 nm, so the parallel-axis term is S * 2 * 1.25^2 = 3125 pN*nm^2
        assert(coupled.alongHelixRigidity.isCloseTo((2.0 * 200.0 + 3125.0) / 2.5))
    }

    @Test
    fun `gate 2 limiting cases - a single layer should reduce to the duplex diameter in thickness`() {
        assert(sheet.thickness.isCloseTo(OrigamiSheet.DUPLEX_DIAMETER))
        assert(sheet.copy(layers = 4, layerSpacing = 2.5).thickness.isCloseTo(3.0 * 2.5 + 2.0))
    }

    // ---------------------------------------------------------------- gate 3

    @Test
    fun `gate 3 conservation - the counts on a cut should be the linear densities times its length`() {
        assert(sheet.crossoverLinearDensity.isCloseTo(1.0 / 5.0))
        assert(sheet.duplexLinearDensity.isCloseTo(1.0 / 2.5))
        assert(sheet.crossoversOnCut(40.0).isCloseTo(8.0))
        assert(sheet.duplexesOnCut(40.0).isCloseTo(16.0))
        assert(sheet.copy(layers = 3).crossoversOnCut(40.0).isCloseTo(24.0))
    }

    @Test
    fun `gate 3 symmetry - the plate should carry the sheet rigidities into the matching axes`() {
        val plate = sheet.plate(lengthX = 40.0, lengthY = 70.0)
        assert(plate.rigidityX.isCloseTo(sheet.alongHelixRigidity))
        assert(plate.rigidityY.isCloseTo(sheet.acrossHelixRigidity))
        assert(plate.twistingRigidity.isCloseTo(sheet.twistingRigidity))
        assert(plate.couplingRigidity.isCloseTo(0.0, 1e-15))
        assert(plate.area.isCloseTo(2800.0))
    }

    // ---------------------------------------------------------------- validity

    @Test
    fun `a non-physical sheet should be rejected on construction`() {
        assertFailsWith<IllegalArgumentException> { sheet.copy(interhelicalDistance = 0.0) }
        assertFailsWith<IllegalArgumentException> { sheet.copy(crossoverSpacing = -1.0) }
        assertFailsWith<IllegalArgumentException> { sheet.copy(crossoverHingeStiffness = 0.0) }
        assertFailsWith<IllegalArgumentException> { sheet.copy(layers = 0) }
        assertFailsWith<IllegalArgumentException> { duplex.copy(bendingRigidity = 0.0) }
    }

}
