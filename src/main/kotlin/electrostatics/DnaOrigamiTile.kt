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

import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.sqrt

/**
 * The charge of the Gen-1 DNA-origami tile, **derived from the §3 geometry** rather than
 * quoted, plus the Manning renormalisation that decides how much of it a downstream task
 * is entitled to use.
 *
 * ## Three surface charge densities, and which is which
 *
 * A block of packed duplexes does not have *one* surface charge density, and using the
 * wrong one moves the coupling parameter by a factor of seven:
 *
 * | quantity | value | where it belongs |
 * |---|---|---|
 * | [duplexSurfaceChargeDensity] | 0.936 e/nm² | the **local** coupling: what a condensed counterion sits on |
 * | [singleHelixLayerChargeDensity] | 2.26 e/nm² | one layer of helices, projected |
 * | [projectedChargeDensity] | 6.70 e/nm² | the **far field**: what a distant plane sees |
 *
 * The logic that selects between them is not arbitrary. The high projected densities are
 * the far-field description, and the far field is exactly where Poisson-Boltzmann works;
 * the region where mean field fails is within `a_⊥ ≈ 1.5 nm` of an actual phosphate, and
 * there the only surface in sight is the duplex's own cylinder. So `Ξ` is computed on
 * [duplexSurfaceChargeDensity], and the projected values are reported to show what the
 * answer would have been had the wrong one been used.
 *
 * ## The §3 geometry, and where it is inconsistent
 *
 * §3 gives "40 × 40 nm" and "~10 nm (single-layer honeycomb)". Those two cannot both hold:
 * a *single* layer of 2 nm duplexes at honeycomb spacing is ~2.6 nm thick. The thickness is
 * taken as given and the "single-layer" reading is carried alongside as
 * [singleHelixLayerChargeDensity]; the discrepancy is a factor of 3 in projected charge and
 * is reported rather than resolved by fiat.
 *
 * @param edge the tile edge in nm — §3, 40 nm.
 * @param thickness the tile thickness in nm — §3, ~10 nm.
 * @param interhelicalDistance the honeycomb-lattice centre-to-centre helix spacing in nm —
 *          **CITED**, ~2.6 nm for honeycomb-lattice origami. The single most uncertain
 *          input here; the charge density goes as its inverse square.
 * @param risePerBasePair the B-form axial rise in nm — **CITED**, 0.34 nm.
 * @param helixRadius the B-DNA duplex radius in nm — **CITED**, 1.0 nm.
 */
@Serializable
data class DnaOrigamiTile(
    val edge: Double = 40.0,
    val thickness: Double = 10.0,
    val interhelicalDistance: Double = 2.6,
    val risePerBasePair: Double = 0.34,
    val helixRadius: Double = 1.0
) {

    init {
        require(edge > 0.0) { "edge must be positive, was: $edge" }
        require(thickness > 0.0) { "thickness must be positive, was: $thickness" }
        require(interhelicalDistance > 0.0) {
            "interhelicalDistance must be positive, was: $interhelicalDistance"
        }
        require(risePerBasePair > 0.0) {
            "risePerBasePair must be positive, was: $risePerBasePair"
        }
        require(helixRadius > 0.0) { "helixRadius must be positive, was: $helixRadius" }
    }

    /** The tile footprint in nm² — 1600 nm² for the §3 tile. */
    val footprintArea: Double get() = edge * edge

    /**
     * The cross-sectional area one helix occupies in a honeycomb lattice, in nm².
     *
     * The honeycomb unit cell of nearest-neighbour distance `d` has area `3√3 d²/2` and
     * contains two sites, so the area per helix is `3√3 d²/4 = 1.299 d²` — 8.78 nm² at
     * `d = 2.6 nm`. Note this is **larger** than the duplex's own 3.14 nm² cross-section:
     * honeycomb packing is deliberately open, which is why it is used for addressability.
     */
    val areaPerHelixCrossSection: Double
        get() = 3.0 * sqrt(3.0) / 4.0 * interhelicalDistance * interhelicalDistance

    /** How many duplexes fit the `edge × thickness` cross-section — **DERIVED**, 45.6. */
    val helixCount: Double get() = edge * thickness / areaPerHelixCrossSection

    /** Total base pairs in the tile — **DERIVED**, 5359. */
    val basePairs: Double get() = helixCount * edge / risePerBasePair

    /**
     * Total nucleotides, i.e. total phosphate charges — **DERIVED**, 10718.
     *
     * A useful independent bound: this must fit a single M13mp18 scaffold (7249 nt) plus a
     * comparable mass of staples, so ≤ ~14500 nt. It does, with room; a factor-of-two error
     * in the packing model would break that bound.
     */
    val nucleotides: Double get() = 2.0 * basePairs

    /** `b = rise/2` in nm — **DERIVED**, 0.17 nm, the axial spacing of phosphate charges. */
    val axialChargeSpacing: Double get() = risePerBasePair / 2.0

    /** `τ = 1/b` in `e/nm` — **DERIVED**, 5.88 charges per nm of duplex. */
    val linearChargeDensity: Double get() = 1.0 / axialChargeSpacing

    /**
     * `σ = τ/(2πR)` in `e/nm²` — **DERIVED**, 0.936 e/nm² = 0.150 C/m².
     *
     * The duplex's own cylindrical surface charge density, and the one the coupling
     * parameter is computed on. That it lands on the textbook 0.15 C/m² for B-DNA to
     * three digits is a gate-5 cross-check, not a coincidence: both come from the same
     * two numbers, the rise and the radius.
     */
    val duplexSurfaceChargeDensity: Double get() = linearChargeDensity / (2.0 * PI * helixRadius)

    /** All the tile's charge smeared over its footprint, in `e/nm²` — **DERIVED**, 6.70. */
    val projectedChargeDensity: Double get() = nucleotides / footprintArea

    /**
     * Returns the surface charge density in `e/nm²` seen by a counterion of radius
     * [counterionRadius] nm that cannot penetrate the duplex — Naji Eq. (30).
     *
     * The hard core moves the effective cylinder radius to `R + σ_ci/2`, which lowers `σ`
     * and hence `Ξ`. For a hydrated `Mg²⁺` this takes `Ξ` from 24.0 to 16.8, i.e. from just
     * above the first-order unbinding threshold to just below it — the correction is not
     * cosmetic. Naji is explicit that the *Manning* parameter is unaffected by it.
     *
     * @throws IllegalArgumentException if [counterionRadius] is negative.
     */
    fun hardCoreSurfaceChargeDensity(counterionRadius: Double): Double {
        require(counterionRadius >= 0.0) {
            "counterionRadius must not be negative, was: $counterionRadius"
        }
        return linearChargeDensity / (2.0 * PI * (helixRadius + counterionRadius))
    }

    /**
     * Returns the Manning parameter `ξ_M = l_B/b` — 4.20 for B-DNA in water at 300 K.
     *
     * **Convention warning.** Manning's own `ξ` excludes the counterion valency, and the
     * condensation criterion is then `q ξ_M > 1`. Naji et al. Eq. (28) write `ξ = q l_B τ`,
     * *including* it, and their Table I accordingly lists 4.1 for `q = 1` and 8.2 for
     * `q = 2` on the same DNA. Both are "the Manning parameter". This property is the
     * valency-free one; multiply by `q` before comparing against Naji's table.
     */
    fun manningParameter(bjerrumLength: Double): Double = bjerrumLength / axialChargeSpacing

    /**
     * Returns the fraction of the bare phosphate charge that survives condensation of
     * counterions of valency [counterionValency].
     *
     * Manning's result: condensation proceeds until the *effective* Manning parameter
     * reaches `1/q`, so the surviving fraction is `1/(q ξ_M)` — and nothing at all condenses
     * when `q ξ_M ≤ 1`.
     *
     * For B-DNA: **23.8% survives with `Na⁺`, 11.9% with `Mg²⁺`** — exactly half, because
     * the surviving fraction goes as `1/q`. This is the charge `T-3` must use; the bare
     * charge would overstate the electrostatic force by 8.4×.
     */
    fun manningSurvivingFraction(counterionValency: Int, bjerrumLength: Double): Double {
        require(counterionValency > 0) {
            "counterionValency must be positive, was: $counterionValency"
        }
        val product = counterionValency * manningParameter(bjerrumLength)
        return if (product <= 1.0) 1.0 else 1.0 / product
    }

    /** The complement of [manningSurvivingFraction] — the condensed, neutralised fraction. */
    fun manningCondensedFraction(counterionValency: Int, bjerrumLength: Double): Double =
        1.0 - manningSurvivingFraction(counterionValency, bjerrumLength)

}

/**
 * Returns the projected charge density in `e/nm²` of a **single row** of duplexes lying
 * side by side at the tile's interhelical pitch — 2.26 e/nm².
 *
 * This is the other reading of §3's "single-layer honeycomb", and it is carried because
 * the two readings differ by a factor of three and §3 does not say which it means.
 */
fun singleHelixLayerChargeDensity(tile: DnaOrigamiTile): Double {
    val helices = tile.edge / tile.interhelicalDistance
    return 2.0 * helices * tile.edge / tile.risePerBasePair / tile.footprintArea
}
