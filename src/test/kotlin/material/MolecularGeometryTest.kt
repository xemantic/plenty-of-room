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

package com.xemantic.nano.plentyofroom.material

import com.xemantic.kotlin.test.assert
import com.xemantic.kotlin.test.have
import com.xemantic.kotlin.test.should
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * The molecular arithmetic under the PEG parameter sheet (task `P-3`).
 *
 * Nothing here is fitted. These are the numbers that turn `cited` into `derived`,
 * which is the first bullet of §7 of the problem definition.
 */
class MolecularGeometryTest {

    @Test
    fun `should derive the molar mass of the ethylene oxide monomer from atomic masses`() {
        // C2H4O — the PEG repeat unit. 44.05 g/mol, the value the osmotic
        // equation of state of Cohen et al. (2009) quotes as "Mm = 44 Da".
        assert(
            molarMass(carbon = 2, hydrogen = 4, oxygen = 1)
                .isCloseTo(44.053, relativeTolerance = 1e-4)
        )
    }

    @Test
    fun `should derive the molar mass of the ethylene repeat unit from atomic masses`() {
        // C2H4 — polyethylene, carried because it validates the contour-length
        // function below against a crystallographic repeat that is known independently.
        assert(
            molarMass(carbon = 2, hydrogen = 4)
                .isCloseTo(28.054, relativeTolerance = 1e-4)
        )
    }

    @Test
    fun `should reject a molecule with no atoms in it`() {
        assertFailsWith<IllegalArgumentException> {
            molarMass()
        } should {
            have(message == "a molecule must contain at least one atom")
        }
    }

    @Test
    fun `should derive the monomer volume from the partial specific volume`() {
        // v0 = M * Vbar / N_A. With PEG's partial specific volume in water,
        // 0.825 mL/g, the ethylene oxide unit occupies 0.0604 nm^3.
        assert(
            monomerVolume(molarMass = 44.053, partialSpecificVolume = 0.825)
                .isCloseTo(0.060350, relativeTolerance = 1e-4)
        )
    }

    @Test
    fun `should invert the monomer volume back to the partial specific volume`() {
        // gate 1, dimensional consistency: the conversion is an involution
        val volume = monomerVolume(molarMass = 44.053, partialSpecificVolume = 0.825)
        assert(
            partialSpecificVolume(molarMass = 44.053, monomerVolume = volume)
                .isCloseTo(0.825)
        )
    }

    @Test
    fun `should reject a non-positive partial specific volume`() {
        assertFailsWith<IllegalArgumentException> {
            monomerVolume(molarMass = 44.053, partialSpecificVolume = 0.0)
        } should {
            have(message == "partialSpecificVolume must be positive, was: 0.0")
        }
    }

    @Test
    fun `should reproduce the polyethylene crystallographic repeat from bond geometry`() {
        // gate 5, literature cross-check on a material whose answer is known independently:
        // the all-trans zigzag repeat of polyethylene is 0.2534 nm along the crystal c axis.
        // Two C-C bonds of 0.153 nm at a backbone angle of 112 degrees give 0.2537 nm.
        assert(
            allTransContourLength(
                backboneBondLengths = listOf(0.153, 0.153),
                backboneBondAngle = 112.0
            ).isCloseTo(0.2534, relativeTolerance = 2e-3)
        )
    }

    @Test
    fun `should derive the all-trans contour length of the ethylene oxide unit`() {
        // -CH2-CH2-O- is three backbone bonds: one C-C at 0.153 nm and two C-O at 0.143 nm.
        // This is the number that decides whether the cited Alexander-de Gennes
        // effective monomer length a = 0.35 nm is a structural quantity or a free parameter.
        assert(
            allTransContourLength(
                backboneBondLengths = listOf(0.153, 0.143, 0.143),
                backboneBondAngle = 112.0
            ).isCloseTo(0.36395, relativeTolerance = 1e-4)
        )
    }

    @Test
    fun `should keep the contour length within one percent across the ether bond-angle spread`() {
        // gate 4, sensitivity: the ether backbone does not have a single bond angle
        // (C-O-C is near 111.5 degrees, O-C-C near 110), so the single-angle simplification
        // is only admissible if the spread it hides is small. It is: 1.2% over 110-112 degrees.
        val bonds = listOf(0.153, 0.143, 0.143)
        val low = allTransContourLength(bonds, backboneBondAngle = 110.0)
        val high = allTransContourLength(bonds, backboneBondAngle = 112.0)
        assert((high - low) / high < 0.02)
    }

    @Test
    fun `should reject a bond angle outside the chemically meaningful range`() {
        assertFailsWith<IllegalArgumentException> {
            allTransContourLength(listOf(0.153), backboneBondAngle = 0.0)
        } should {
            have(message == "backboneBondAngle must be within (0.0, 180.0), was: 0.0")
        }
    }

    @Test
    fun `should reject a chain with no backbone bonds`() {
        assertFailsWith<IllegalArgumentException> {
            allTransContourLength(emptyList(), backboneBondAngle = 112.0)
        } should {
            have(message == "backboneBondLengths must not be empty")
        }
    }

}
