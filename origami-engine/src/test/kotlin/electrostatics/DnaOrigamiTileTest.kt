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
import com.xemantic.nano.plentyofroom.isCloseTo
import kotlin.math.PI
import kotlin.math.abs
import kotlin.test.Test

/**
 * The surface charge density of the Gen-1 tile, derived from the §3 geometry rather than
 * cited, and the Manning renormalisation that decides how much of it survives.
 */
class DnaOrigamiTileTest {

    private val lb = bjerrumLength()
    private val tile = DnaOrigamiTile()

    // gate 1 — dimensional consistency

    @Test
    fun `gate 1 should derive the axial charge spacing of B-DNA from the rise per base pair`() {
        // Two phosphates per base pair over a 0.34 nm rise gives b = 0.17 nm.
        // This is a DERIVED number: the only input is the B-form rise.
        assert(tile.axialChargeSpacing.isCloseTo(0.17))
        assert(tile.linearChargeDensity.isCloseTo(1.0 / 0.17, relativeTolerance = 1e-12))
    }

    @Test
    fun `gate 1 should derive the duplex surface charge density from the same geometry`() {
        // sigma = tau / (2 pi R) with tau = 2/rise and R = 1.0 nm.
        assert(tile.duplexSurfaceChargeDensity.isCloseTo(0.93621, relativeTolerance = 1e-4))
        assert(
            tile.duplexSurfaceChargeDensity
                .isCloseTo(tile.linearChargeDensity / (2.0 * PI * tile.helixRadius), 1e-12)
        )
    }

    @Test
    fun `gate 1 should conserve nucleotide count between the two ways of counting it`() {
        // helices x length / rise x 2 must equal base pairs x 2, or the packing model
        // and the charge model disagree about the same object.
        assert(tile.nucleotides.isCloseTo(2.0 * tile.basePairs, relativeTolerance = 1e-12))
        assert(
            tile.projectedChargeDensity.isCloseTo(tile.nucleotides / tile.footprintArea, 1e-12)
        )
    }

    // gate 2 — limiting cases and sensitivity

    @Test
    fun `gate 2 should scale the projected charge density linearly with tile thickness`() {
        // The honeycomb block is a bulk lattice, so doubling the thickness doubles the
        // helices and hence the projected charge. The DUPLEX surface charge density,
        // by contrast, is thickness independent — which is the whole point of carrying both.
        val thick = DnaOrigamiTile(thickness = 20.0)
        assert((thick.projectedChargeDensity / tile.projectedChargeDensity).isCloseTo(2.0, 1e-12))
        assert(thick.duplexSurfaceChargeDensity.isCloseTo(tile.duplexSurfaceChargeDensity, 1e-12))
    }

    @Test
    fun `gate 2 should place a single layer of helices an order of magnitude below the block`() {
        // §3 says "~10 nm (single-layer honeycomb)", which is internally inconsistent:
        // one layer of 2 nm duplexes at a 2.6 nm pitch is 2.6 nm thick, not 10 nm.
        // Both readings are carried; the 10 nm block is the one §3's thickness implies.
        assert(singleHelixLayerChargeDensity(tile).isCloseTo(2.26244, relativeTolerance = 1e-4))
        assert(tile.projectedChargeDensity.isCloseTo(6.69857, relativeTolerance = 1e-4))
    }

    @Test
    fun `gate 2 should keep the tile inside a single M13 scaffold origami`() {
        // A sanity bound that is independent of the packing model: the derived nucleotide
        // count must fit a standard 7249-nt M13mp18 scaffold plus a comparable mass of
        // staples, i.e. <= ~14500 nt total. 10718 nt does; a factor-of-two error would not.
        assert(tile.nucleotides < 14_500.0)
        assert(tile.nucleotides > 5_000.0)
        assert(tile.nucleotides.isCloseTo(10717.9, relativeTolerance = 1e-3))
    }

    // gate 3 — charge conservation under Manning renormalisation

    @Test
    fun `gate 3 should conserve charge across the Manning condensation split`() {
        // condensed + free = 1 exactly, for every valency. Manning's own statement is
        // z*theta = 1 - 1/(z xi), so the surviving fraction is 1/(z xi).
        listOf(1, 2, 3).forEach { valency ->
            val free = tile.manningSurvivingFraction(valency, lb)
            val condensed = tile.manningCondensedFraction(valency, lb)
            assert((free + condensed).isCloseTo(1.0, relativeTolerance = 1e-12))
        }
    }

    @Test
    fun `gate 3 should leave the effective Manning parameter at exactly one over the valency`() {
        // The fixed point of the theory: condensation proceeds until xi_eff = xi * (1 - z theta)
        // equals 1/z, at which point it stops. Asserted as an identity, not observed.
        listOf(1, 2, 3, 4).forEach { valency ->
            val effective = tile.manningParameter(lb) * tile.manningSurvivingFraction(valency, lb)
            assert(effective.isCloseTo(1.0 / valency, relativeTolerance = 1e-12))
        }
    }

    @Test
    fun `gate 2 should leave the charge untouched below the condensation threshold`() {
        // For z*xi <= 1 there is no condensation and the bare charge survives whole.
        // Realised here by a hypothetical duplex stretched to a 1 nm charge spacing.
        val sparse = DnaOrigamiTile(risePerBasePair = 2.0)
        assert(sparse.manningParameter(lb) < 1.0)
        assert(sparse.manningSurvivingFraction(1, lb).isCloseTo(1.0))
        assert(sparse.manningCondensedFraction(1, lb).isCloseTo(0.0))
    }

    // gate 5 — literature cross-check

    @Test
    fun `gate 5 should reproduce the textbook Manning parameter of B-DNA`() {
        // xi_M = l_B / b = 0.7141 / 0.17 = 4.20, the canonical value for B-DNA in water.
        // Naji et al. Table I list xi = 4.1 for q = 1 and 8.2 for q = 2 — note THEIR xi
        // absorbs the valency (their Eq. 28, xi = q l_B tau), so the comparison is
        // q * xi_M against their column, and it agrees to 2%.
        assert(tile.manningParameter(lb).isCloseTo(4.2006, relativeTolerance = 1e-4))
        assert(abs(1 * tile.manningParameter(lb) - 4.1) / 4.1 < 0.03)
        assert(abs(2 * tile.manningParameter(lb) - 8.2) / 8.2 < 0.03)
    }

    @Test
    fun `gate 5 should leave only twelve percent of the bare charge under divalent condensation`() {
        // The number T-3 must use instead of the bare charge.
        // Monovalent Na+ leaves 23.8%; divalent Mg2+ leaves 11.9%, i.e. exactly half of it.
        assert(tile.manningSurvivingFraction(1, lb).isCloseTo(0.238059, relativeTolerance = 1e-4))
        assert(tile.manningSurvivingFraction(2, lb).isCloseTo(0.119030, relativeTolerance = 1e-4))
        assert(
            (tile.manningSurvivingFraction(1, lb) / tile.manningSurvivingFraction(2, lb))
                .isCloseTo(2.0, relativeTolerance = 1e-12)
        )
        // in absolute terms, 10718 e bare becomes 1276 e effective
        assert((tile.nucleotides * tile.manningSurvivingFraction(2, lb)).isCloseTo(1275.8, 1e-3))
    }

    @Test
    fun `gate 5 should lower the coupling parameter when the counterion cannot reach the surface`() {
        // Naji Eq. (30): with hard-core counterions the cylinder radius to use is R + sigma_ci/2,
        // which lowers sigma and hence Xi. A hydrated Mg2+ (Nightingale radius 4.28 A) cannot
        // approach the phosphates closer than that, and Xi falls from 24.0 to 16.8 — from
        // above the first-order unbinding threshold of 17 to just below it.
        val bare = ChargedSurface(tile.duplexSurfaceChargeDensity, 2)
        val hydrated = ChargedSurface(tile.hardCoreSurfaceChargeDensity(0.428), 2)
        assert(tile.hardCoreSurfaceChargeDensity(0.428).isCloseTo(0.655637, relativeTolerance = 1e-4))
        assert(bare.couplingParameter(lb).isCloseTo(24.00, relativeTolerance = 1e-3))
        assert(hydrated.couplingParameter(lb).isCloseTo(16.809, relativeTolerance = 1e-3))
        // the Manning parameter is NOT changed by this — Naji says so explicitly
        assert(tile.manningParameter(lb).isCloseTo(4.2006, relativeTolerance = 1e-4))
    }

    @Test
    fun `gate 1 should reject an unphysical tile`() {
        try {
            DnaOrigamiTile(thickness = 0.0)
            throw AssertionError("should have rejected a zero thickness")
        } catch (e: IllegalArgumentException) {
            assert(e.message!!.contains("thickness"))
        }
        try {
            tile.hardCoreSurfaceChargeDensity(-1.0)
            throw AssertionError("should have rejected a negative counterion radius")
        } catch (e: IllegalArgumentException) {
            assert(e.message!!.contains("counterionRadius"))
        }
    }
}
