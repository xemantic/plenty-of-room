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
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.InterlayerCoupling
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.nano.plentyofroom.anchoring.M13_SCAFFOLD_NUCLEOTIDES
import com.xemantic.nano.plentyofroom.coupling.winklerBendingLength
import kotlin.math.abs
import kotlin.math.pow
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `T-191` — the tile §3 actually specifies: a four-layer, ~10 nm body.
 *
 * Every test is named for the gate it discharges, and `T-191`'s declared falsifiers are
 * asserted rather than argued:
 *
 * - **`F1`** — a uniform load on the four-layer lattice must dish exactly zero;
 * - **`F2`** — the **smeared equivalent sheet** must reproduce the multi-layer sheet's three
 *   flexural rigidities exactly, or no lattice number in this task means anything.
 *
 * The identity this file exists to pin down: under Chen et al.'s construction the crossover's
 * in-plane spring and its hinge spring stand in the ratio `k_s/k_θ = S/B` of the duplex's own
 * stretch modulus to its bending rigidity, so the parallel-axis enhancement of a multi-layer
 * body is **the same factor along and across the helices** — and the anisotropy of a
 * four-layer sheet is therefore independent of how strongly its layers are coupled.
 */
class FourLayerTileTest {

    private val singleLayer = multiLayerRigidities(
        layers = 1,
        interhelicalDistance = Gen1Tile.INTERHELICAL_SHEET,
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_SHEET_BP,
        coupling = LayerCoupling.INDEPENDENT
    )

    private val fourLayerIndependent = multiLayerRigidities(
        layers = 4,
        interhelicalDistance = Gen1Tile.INTERHELICAL_HONEYCOMB,
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
        coupling = LayerCoupling.INDEPENDENT
    )

    private val fourLayerAlongOnly = multiLayerRigidities(
        layers = 4,
        interhelicalDistance = Gen1Tile.INTERHELICAL_HONEYCOMB,
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
        coupling = LayerCoupling.ALONG_HELICES_ONLY
    )

    private val fourLayerComposite = multiLayerRigidities(
        layers = 4,
        interhelicalDistance = Gen1Tile.INTERHELICAL_HONEYCOMB,
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
        coupling = LayerCoupling.COMPOSITE
    )

    // ------------------------------------------------------------------ gate 1: dimensions

    @Test
    fun `gate 1 - a flexural rigidity is a force times a length and a reach is a length`() {
        // D = EI/d : pN*nm^2 / nm = pN*nm
        assert(singleLayer.alongHelixRigidity.isCloseTo(230.0 / Gen1Tile.INTERHELICAL_SHEET))
        // ell = (4 D / k_f)^(1/4) : (pN*nm / (pN/nm^3))^(1/4) = (nm^4)^(1/4) = nm
        val reach = winklerBendingLength(
            singleLayer.alongHelixRigidity, Gen1Tile.FOUNDATION_SECANT
        )
        assert(reach > 12.0 && reach < 13.5)
    }

    @Test
    fun `gate 1 - the geometric thickness of four honeycomb layers is the stated 9 point 608 nm`() {
        assert(fourLayerComposite.thickness.isCloseTo(9.608))
        assert(singleLayer.thickness.isCloseTo(2.0))
    }

    // ------------------------------------------------------------------ gate 2: limiting cases

    @Test
    fun `gate 2 - one layer has no parallel-axis term at any coupling`() {
        LayerCoupling.entries.forEach { coupling ->
            val one = multiLayerRigidities(
                layers = 1,
                interhelicalDistance = Gen1Tile.INTERHELICAL_SHEET,
                crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_SHEET_BP,
                coupling = coupling
            )
            assert(one.parallelAxisFactor.isCloseTo(1.0))
            assert(one.alongHelixRigidity.isCloseTo(singleLayer.alongHelixRigidity))
            assert(one.acrossHelixRigidity.isCloseTo(singleLayer.acrossHelixRigidity))
        }
    }

    @Test
    fun `gate 2 - the independent four-layer sheet is exactly four single honeycomb layers`() {
        val oneHoneycomb = multiLayerRigidities(
            layers = 1,
            interhelicalDistance = Gen1Tile.INTERHELICAL_HONEYCOMB,
            crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
            coupling = LayerCoupling.INDEPENDENT
        )
        assert(fourLayerIndependent.alongHelixRigidity.isCloseTo(4.0 * oneHoneycomb.alongHelixRigidity))
        assert(fourLayerIndependent.acrossHelixRigidity.isCloseTo(4.0 * oneHoneycomb.acrossHelixRigidity))
        assert(fourLayerIndependent.twistingRigidity.isCloseTo(4.0 * oneHoneycomb.twistingRigidity))
    }

    @Test
    fun `gate 2 - F2 the smeared equivalent sheet reproduces the multi-layer rigidities exactly`() {
        listOf(singleLayer, fourLayerIndependent, fourLayerAlongOnly, fourLayerComposite)
            .forEach { rigidities ->
                val sheet = equivalentSheet(rigidities)
                assert(sheet.layers == 1)
                assert(
                    abs(sheet.alongHelixRigidity / rigidities.alongHelixRigidity - 1.0) < 1e-12
                )
                assert(
                    abs(sheet.acrossHelixRigidity / rigidities.acrossHelixRigidity - 1.0) < 1e-12
                )
                assert(
                    abs(sheet.twistingRigidity / rigidities.twistingRigidity - 1.0) < 1e-12
                )
                assert(sheet.interhelicalDistance.isCloseTo(rigidities.interhelicalDistance))
                assert(sheet.crossoverSpacing.isCloseTo(rigidities.crossoverSpacing))
            }
    }

    @Test
    fun `gate 2 - the ALONG_HELICES_ONLY reading reproduces the standing C-0006 variant`() {
        val standing = origamiSheet(
            Gen1Tile.INTERHELICAL_HONEYCOMB,
            Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
            layers = 4,
            interlayerCoupling = InterlayerCoupling.RIGID
        )
        assert(fourLayerAlongOnly.alongHelixRigidity.isCloseTo(standing.alongHelixRigidity))
        assert(fourLayerAlongOnly.acrossHelixRigidity.isCloseTo(standing.acrossHelixRigidity))
        assert(fourLayerAlongOnly.twistingRigidity.isCloseTo(standing.twistingRigidity))
        // and it reproduces C-0006's published table row
        assert(abs(fourLayerAlongOnly.alongHelixRigidity - 14310.78) < 0.01)
        assert(abs(fourLayerAlongOnly.acrossHelixRigidity - 19.222) < 0.001)
        assert(abs(fourLayerAlongOnly.twistingRigidity - 181.39) < 0.01)
    }

    // ------------------------------------------------------------------ gate 3: symmetry

    @Test
    fun `gate 3 - the parallel-axis enhancement is the SAME factor along and across the helices`() {
        // The identity: k_s/k_theta = S/B under Chen et al.'s construction, so the composite
        // excess over the independent reading is one number for both directions.
        val alongRatio = fourLayerComposite.alongHelixRigidity /
                fourLayerIndependent.alongHelixRigidity
        val acrossRatio = fourLayerComposite.acrossHelixRigidity /
                fourLayerIndependent.acrossHelixRigidity
        assert(abs(alongRatio / acrossRatio - 1.0) < 1e-12)
        assert(abs(alongRatio / fourLayerComposite.parallelAxisFactor - 1.0) < 1e-12)
    }

    @Test
    fun `gate 3 - therefore the anisotropy of a four-layer sheet does not depend on the coupling`() {
        val independent = fourLayerIndependent.alongHelixRigidity /
                fourLayerIndependent.acrossHelixRigidity
        val composite = fourLayerComposite.alongHelixRigidity /
                fourLayerComposite.acrossHelixRigidity
        assert(abs(independent / composite - 1.0) < 1e-12)
        // and the standing ALONG_HELICES_ONLY variant is NOT on that line — it is a mixed state
        val mixed = fourLayerAlongOnly.alongHelixRigidity / fourLayerAlongOnly.acrossHelixRigidity
        assert(mixed > 30.0 * independent)
    }

    @Test
    fun `gate 3 - F1 a uniform load on the four-layer lattice dishes exactly zero`() {
        val sheet = equivalentSheet(fourLayerComposite)
        val lattice = OrigamiGrillage(
            sheet = sheet,
            lengthX = 38.08,
            beamCount = 15,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            columns = CrossoverLayout.centred(8, sheet.crossoverSpacing / 2.0),
            subdivisions = 2
        )
        val solved = lattice.solve(uniformPressure(0.0625))
        assert(solved.dishingRms < 1e-9 * solved.meanDeflection)
    }

    // ------------------------------------------------------------------ gate 4: convergence

    @Test
    fun `gate 4 - the reach scales as the fourth root of the rigidity, exactly`() {
        val single = winklerBendingLength(
            singleLayer.alongHelixRigidity, Gen1Tile.FOUNDATION_SECANT
        )
        val four = winklerBendingLength(
            fourLayerComposite.alongHelixRigidity, Gen1Tile.FOUNDATION_SECANT
        )
        val ratio = fourLayerComposite.alongHelixRigidity / singleLayer.alongHelixRigidity
        assert(abs(four / single - ratio.pow(0.25)) < 1e-12)
    }

    // ------------------------------------------------------------------ gate 5: literature

    private val fourLayerCalibrated = multiLayerRigidities(
        layers = 4,
        interhelicalDistance = Gen1Tile.INTERHELICAL_HONEYCOMB,
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
        coupling = LayerCoupling.CALIBRATED
    )

    @Test
    fun `gate 5 - Kauert's measured 6HB honeycomb sits inside the declared composite band`() {
        // Six helices on a hexagon of circumradius d: sum(y^2) = 6 (d/2 ... ) = 3 d^2.
        val d = Gen1Tile.INTERHELICAL_HONEYCOMB
        val fraction = MeasuredBundleRigidity.compositeFraction(
            helices = 6, secondMoment = 3.0 * d * d, persistenceLength = 1880.0
        )
        assert(abs(fraction - 0.3019) < 5e-4)
        assert(fraction > MeasuredBundleRigidity.COMPOSITE_FRACTION_MIN)
        assert(fraction < MeasuredBundleRigidity.COMPOSITE_FRACTION_MAX)
    }

    @Test
    fun `gate 5 - Kauert's measured 4HB square lattice agrees with the 6HB honeycomb one`() {
        // Four helices at the corners of a square of side d: sum(y^2) = 4 (d/2)^2 = d^2.
        val d = 2.60
        val fraction = MeasuredBundleRigidity.compositeFraction(
            helices = 4, secondMoment = d * d, persistenceLength = 740.0
        )
        assert(abs(fraction - 0.2885) < 5e-4)
        assert(fraction > MeasuredBundleRigidity.COMPOSITE_FRACTION_MIN)
    }

    @Test
    fun `gate 5 - Pfitzner's independently measured 6HB agrees to the same band`() {
        val d = Gen1Tile.INTERHELICAL_HONEYCOMB
        val fraction = MeasuredBundleRigidity.compositeFraction(
            helices = 6, secondMoment = 3.0 * d * d, persistenceLength = 2000.0
        )
        assert(abs(fraction - 0.3253) < 5e-4)
        assert(fraction < MeasuredBundleRigidity.COMPOSITE_FRACTION_MAX)
    }

    @Test
    fun `gate 5 - the adopted fraction lies inside the measured band`() {
        assert(
            MeasuredBundleRigidity.COMPOSITE_FRACTION >
                    MeasuredBundleRigidity.COMPOSITE_FRACTION_MIN
        )
        assert(
            MeasuredBundleRigidity.COMPOSITE_FRACTION <
                    MeasuredBundleRigidity.COMPOSITE_FRACTION_MAX
        )
    }

    @Test
    fun `gate 5 - the calibrated tile lies strictly between the two limits in BOTH directions`() {
        assert(fourLayerCalibrated.alongHelixRigidity > fourLayerIndependent.alongHelixRigidity)
        assert(fourLayerCalibrated.alongHelixRigidity < fourLayerComposite.alongHelixRigidity)
        assert(fourLayerCalibrated.acrossHelixRigidity > fourLayerIndependent.acrossHelixRigidity)
        assert(fourLayerCalibrated.acrossHelixRigidity < fourLayerComposite.acrossHelixRigidity)
        // and the anisotropy is invariant along the whole calibration axis
        assert(
            abs(
                fourLayerCalibrated.anisotropy / fourLayerIndependent.anisotropy - 1.0
            ) < 1e-12
        )
    }

    @Test
    fun `a composite fraction outside the unit interval is refused`() {
        assertFailsWith<IllegalArgumentException> {
            multiLayerRigidities(
                layers = 4,
                interhelicalDistance = Gen1Tile.INTERHELICAL_HONEYCOMB,
                crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
                coupling = LayerCoupling.CALIBRATED,
                compositeFraction = 1.5
            )
        }
    }

    // ------------------------------------------------------------------ gate 5: the scaffold

    @Test
    fun `gate 5 - four honeycomb layers at the buildable width fit one circular M13`() {
        val used = scaffoldNucleotides(layers = 4, rows = 15, basePairsPerRow = 112)
        assert(used == 6720L)
        assert(used < M13_SCAFFOLD_NUCLEOTIDES)
        assert(layersAffordable(M13_SCAFFOLD_NUCLEOTIDES, rows = 15, basePairsPerRow = 112) == 4)
        // and five layers do not
        assert(scaffoldNucleotides(layers = 5, rows = 15, basePairsPerRow = 112) > M13_SCAFFOLD_NUCLEOTIDES)
    }

    @Test
    fun `gate 5 - C-0086's single-layer count is reproduced`() {
        assert(scaffoldNucleotides(layers = 1, rows = 15, basePairsPerRow = 112) == 1680L)
        assert(
            abs(
                M13_SCAFFOLD_NUCLEOTIDES.toDouble() /
                        scaffoldNucleotides(1, 15, 112).toDouble() - 4.31488095
            ) < 1e-6
        )
    }

    // ------------------------------------------------------------------ guards

    @Test
    fun `a body needs at least one layer`() {
        assertFailsWith<IllegalArgumentException> {
            multiLayerRigidities(
                layers = 0,
                interhelicalDistance = Gen1Tile.INTERHELICAL_HONEYCOMB,
                crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
                coupling = LayerCoupling.COMPOSITE
            )
        }
    }

    @Test
    fun `a scaffold cannot pay for a fractional layer`() {
        assertFailsWith<IllegalArgumentException> { scaffoldNucleotides(0, 15, 112) }
        assertFailsWith<IllegalArgumentException> { layersAffordable(0L, 15, 112) }
    }
}
