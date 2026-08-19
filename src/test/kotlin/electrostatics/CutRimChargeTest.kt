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

package com.xemantic.nano.plentyofroom.electrostatics

import com.xemantic.kotlin.test.assert
import com.xemantic.nano.plentyofroom.anchoring.MeasuredBackbone
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.PI
import kotlin.math.abs
import kotlin.test.Test
import kotlin.test.assertFailsWith

/**
 * `P-14` gate tests for the charge presented by the cut rim of a DNA-origami sheet.
 *
 * `C-0022` swept the rim between **uncharged** and the **face** areal density and reported a
 * 1.845× bracket on its collar depth, with neither endpoint sourced. What is tested here is
 * that the rim density is not a free parameter at all: it is fixed by a conservation identity
 * (the tile's own charge, smeared onto the tile's own boundary) plus a geometric partition,
 * and the family that satisfies both is one-parameter.
 */
class CutRimChargeTest {

    private val tile = DnaOrigamiTile()
    private val faceCharge = -0.3986652379247042
    private val rho = tileVolumetricChargeDensity(faceCharge, tile.thickness)

    // ------------------------------------------------ gate 1 — dimensional consistency

    @Test
    fun `gate 1 should recover the face density from the volumetric one and back`() {
        // sigma_face = rho t / 2 is EXACT for a slab: Gauss's law on a uniformly charged slab
        // gives the same exterior field as two sheets of rho t / 2.
        assert(rho.isCloseTo(2.0 * faceCharge / tile.thickness))
        assert((rho * tile.thickness / 2.0).isCloseTo(faceCharge))
        // and it is the tile's own charge over its bounding-box volume
        val bare = -tile.nucleotides / (tile.footprintArea * tile.thickness)
        val survived = bare * tile.manningSurvivingFraction(2, bjerrumLength())
        assert(rho.isCloseTo(survived, 1e-6))
    }

    @Test
    fun `gate 1 should scale the rim density with the volumetric density and the taper length`() {
        val one = CutRimSmearing.taperedFace(rho, tile.thickness, 20.0, 2.5)
        val double = CutRimSmearing.taperedFace(2.0 * rho, tile.thickness, 20.0, 2.5)
        val longer = CutRimSmearing.taperedFace(rho, tile.thickness, 20.0, 5.0)
        assert(double.rimChargeDensity.isCloseTo(2.0 * one.rimChargeDensity))
        assert(longer.rimChargeDensity.isCloseTo(2.0 * one.rimChargeDensity))
    }

    @Test
    fun `gate 1 should refuse a taper longer than the tile half width`() {
        assertFailsWith<IllegalArgumentException> {
            CutRimSmearing.taperedFace(rho, 10.0, 20.0, 20.1)
        }
        assertFailsWith<IllegalArgumentException> {
            CutRimSmearing.taperedFace(rho, 10.0, 20.0, -1.0)
        }
        assertFailsWith<IllegalArgumentException> {
            CutRimSmearing.taperedFace(rho, 0.0, 20.0, 1.0)
        }
    }

    // ------------------------------------------------ gate 2 — limiting cases

    @Test
    fun `gate 2 should reduce to C-0022's headline at zero taper`() {
        val zero = CutRimSmearing.taperedFace(rho, tile.thickness, 20.0, 0.0)
        assert(zero.rimChargeDensity == 0.0)
        assert(zero.faceChargeDensityAt(0.0).isCloseTo(faceCharge))
        assert(zero.faceChargeDensityAt(20.0).isCloseTo(faceCharge))
    }

    @Test
    fun `gate 2 should put the medial rim at exactly half the face density`() {
        val medial = CutRimSmearing.medial(rho, tile.thickness, 20.0)
        assert(medial.rimChargeDensity.isCloseTo(0.5 * faceCharge))
        assert(medial.taperLength.isCloseTo(tile.thickness / 2.0))
        // and the ratio is PURE GEOMETRY: no rho, no thickness, no buffer, no Manning fraction
        for (t in listOf(2.0, 8.589, 10.0, 25.0)) {
            for (r in listOf(-1.0, -0.079, 3.3)) {
                val other = CutRimSmearing.medial(r, t, 20.0)
                assert((other.rimChargeDensity / other.interiorFaceChargeDensity).isCloseTo(0.5))
            }
        }
    }

    @Test
    fun `gate 2 should make the medial rim profile triangular and the uniform one flat`() {
        val medial = CutRimSmearing.medial(rho, tile.thickness, 20.0)
        // peak at mid-height is the full face density, zero at both corners
        assert(medial.rimChargeDensityAt(tile.thickness / 2.0).isCloseTo(faceCharge))
        assert(abs(medial.rimChargeDensityAt(0.0)) < 1e-15)
        assert(abs(medial.rimChargeDensityAt(tile.thickness)) < 1e-15)
        val uniform = CutRimSmearing.taperedFace(rho, tile.thickness, 20.0, tile.thickness / 2.0)
        assert(uniform.rimChargeDensityAt(0.0).isCloseTo(uniform.rimChargeDensity))
        assert(uniform.rimChargeDensityAt(tile.thickness).isCloseTo(uniform.rimChargeDensity))
        // the two share a MEAN, which is what makes them a convention pair rather than a range
        assert(medial.rimChargeDensity.isCloseTo(uniform.rimChargeDensity))
    }

    @Test
    fun `gate 2 should reach C-0022's falsifier density only at a full-thickness taper`() {
        val full = CutRimSmearing.taperedFace(rho, tile.thickness, 20.0, tile.thickness)
        assert(full.rimChargeDensity.isCloseTo(faceCharge))
        // and it costs a face taper 10 nm deep, which C-0022's falsifier did NOT apply
        assert(full.faceChargeDensityAt(0.0).isCloseTo(0.0))
        assert(full.faceChargeDensityAt(5.0).isCloseTo(0.5 * faceCharge))
    }

    // ------------------------------------------------ gate 3 — conservation

    @Test
    fun `gate 3 should conserve the tile's charge at every member of the family`() {
        for (taper in listOf(0.0, 1.0, 2.5, 5.0, 7.5, 10.0, 19.0)) {
            val smearing = CutRimSmearing.taperedFace(rho, tile.thickness, 20.0, taper)
            assert(smearing.boundaryChargeRatioTwoDimensional.isCloseTo(1.0, 1e-12))
            assert(smearing.boundaryChargeRatioThreeDimensional.isCloseTo(1.0, 1e-12))
        }
        val medial = CutRimSmearing.medial(rho, tile.thickness, 20.0)
        assert(medial.boundaryChargeRatioTwoDimensional.isCloseTo(1.0, 1e-12))
        assert(medial.boundaryChargeRatioThreeDimensional.isCloseTo(1.0, 1e-12))
    }

    @Test
    fun `gate 3 should measure C-0022's falsifier as a 25 and 50 per cent charge excess`() {
        val excess = uniformRimBoundaryChargeRatio(faceCharge, faceCharge, tile.thickness, 20.0)
        assert(excess.twoDimensional.isCloseTo(1.25))
        assert(excess.threeDimensional.isCloseTo(1.5))
        // the geometric density is a quarter of that excess in 2-D and half in 3-D
        val geometric = uniformRimBoundaryChargeRatio(
            faceCharge, 0.5 * faceCharge, tile.thickness, 20.0
        )
        assert(geometric.twoDimensional.isCloseTo(1.125))
        assert(geometric.threeDimensional.isCloseTo(1.25))
        // and the uncharged rim conserves exactly
        val uncharged = uniformRimBoundaryChargeRatio(faceCharge, 0.0, tile.thickness, 20.0)
        assert(uncharged.twoDimensional == 1.0)
        assert(uncharged.threeDimensional == 1.0)
    }

    @Test
    fun `gate 3 should make the face deficit equal the rim gain identically`() {
        for (taper in listOf(0.5, 2.5, 5.0, 9.0)) {
            val smearing = CutRimSmearing.taperedFace(rho, tile.thickness, 20.0, taper)
            assert(smearing.faceDeficitPerUnitEdge.isCloseTo(smearing.rimGainPerUnitEdge, 1e-12))
        }
        val medial = CutRimSmearing.medial(rho, tile.thickness, 20.0)
        assert(medial.faceDeficitPerUnitEdge.isCloseTo(medial.rimGainPerUnitEdge, 1e-12))
    }

    // ------------------------------------------------ gate 4 — the DNA census

    @Test
    fun `gate 4 should count one duplex end per honeycomb cross-section on the across-helix rim`() {
        val census = cutRimCensus(tile, MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS)
        assert(census.duplexEndsPerRimArea.isCloseTo(1.0 / tile.areaPerHelixCrossSection))
        assert(
            census.endFaceCoverage.isCloseTo(
                PI * tile.helixRadius * tile.helixRadius / tile.areaPerHelixCrossSection
            )
        )
        // the end plane carries the terminal phosphate itself
        assert(census.endRimNearestChargeDepth == 0.0)
        // the sidewall's nearest phosphate is the measured backbone radius inside the steric one
        assert(
            census.sidewallRimNearestChargeDepth
                .isCloseTo(tile.helixRadius - MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS)
        )
    }

    @Test
    fun `gate 4 should keep both rim charge depths inside C-0022's discarded standoff`() {
        val census = cutRimCensus(tile, MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS)
        assert(census.chargeDepthDifference < DEFAULT_RIM_STANDOFF)
        assert(census.chargeDepthDifference > 0.0)
    }

    // ------------------------------------------------ gate 5 — the tile ledger

    @Test
    fun `gate 5 should reproduce C-0022's own face charge from the tile geometry`() {
        val fromTile = -tile.projectedChargeDensity *
                tile.manningSurvivingFraction(2, bjerrumLength()) / 2.0
        assert(fromTile.isCloseTo(faceCharge, 1e-12))
    }
}
