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
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * §4(c) — ion partitioning into the neutral PEG layer, as a **cheap bound**.
 *
 * ## What is bounded, and what is not
 *
 * Two exclusion mechanisms are computed:
 *
 * 1. **Steric.** A hydrated ion cannot occupy the volume the chains occupy, nor come within
 *    its own radius of a chain's axis. Ogston's expression for a random fibre network,
 *    `K = exp(−φ (1 + R_ion/r_fibre)²)`, is the standard cheap form.
 * 2. **Dielectric (Born).** The layer's effective permittivity is below water's, so moving
 *    an ion in costs Born self-energy.
 *
 * Both **exclude**, so their product is an upper bound on depletion and hence a *lower*
 * bound on the partition coefficient. And that is the honest limit of the method:
 * a mechanism running the other way — PEG's ether oxygens coordinating cations, as they
 * famously do in polymer electrolytes — would raise `K` and is **not** bounded here.
 * The claim says so plainly rather than presenting the bound as an answer.
 *
 * @param polymerVolumeFraction `φ`, the physical volume fraction from `C-0002`.
 * @param fibreRadius the chain's effective radius in nm — pass `PegWater.kuhnSegmentDiameter/2`.
 */
@Serializable
data class LayerPartitioning(
    val polymerVolumeFraction: Double,
    val fibreRadius: Double,
    val magnesiumRadius: Double = HYDRATED_MAGNESIUM_RADIUS,
    val chlorideRadius: Double = HYDRATED_CHLORIDE_RADIUS
) {

    init {
        require(polymerVolumeFraction > 0.0 && polymerVolumeFraction < 1.0) {
            "polymerVolumeFraction must be in (0, 1), was: $polymerVolumeFraction"
        }
        require(fibreRadius > 0.0) { "fibreRadius must be positive, was: $fibreRadius" }
        require(magnesiumRadius > 0.0) {
            "magnesiumRadius must be positive, was: $magnesiumRadius"
        }
        require(chlorideRadius > 0.0) { "chlorideRadius must be positive, was: $chlorideRadius" }
    }

    /** The layer's effective relative permittivity by Maxwell-Garnett — 75.0 at `φ = 0.029`. */
    val effectivePermittivity: Double get() = maxwellGarnettPermittivity(polymerVolumeFraction)

    /** The Born transfer penalty for `Mg²⁺` in `k_BT` — 0.135 at the design point. */
    val magnesiumBornEnergy: Double
        get() = bornTransferEnergy(2, magnesiumRadius, effectivePermittivity)

    /** The Born transfer penalty for `Cl⁻` in `k_BT` — 0.045 at the design point. */
    val chlorideBornEnergy: Double
        get() = bornTransferEnergy(1, chlorideRadius, effectivePermittivity)

    /** The Born-only partition coefficient for `Mg²⁺`. */
    val magnesiumBornPartitionCoefficient: Double get() = exp(-magnesiumBornEnergy)

    /** The steric-only partition coefficient for `Mg²⁺` (Ogston). */
    val magnesiumStericPartitionCoefficient: Double
        get() = ogstonPartitionCoefficient(polymerVolumeFraction, magnesiumRadius, fibreRadius)

    /** Steric × Born for `Mg²⁺` — 0.69 at the design point. */
    val magnesiumPartitionCoefficient: Double
        get() = magnesiumStericPartitionCoefficient * magnesiumBornPartitionCoefficient

    /** Steric × Born for `Cl⁻` — 0.81 at the design point. */
    val chloridePartitionCoefficient: Double
        get() = ogstonPartitionCoefficient(polymerVolumeFraction, chlorideRadius, fibreRadius) *
                exp(-chlorideBornEnergy)

    /**
     * The `MgCl₂` salt partition coefficient, `K = (K₊ K₋²)^(1/3)`.
     *
     * The stoichiometric geometric mean is Donnan equilibrium for a layer with **no fixed
     * charge**: the layer must stay electroneutral, so the two ions cannot partition
     * independently, and the Donnan potential that enforces it is exactly what the geometric
     * mean encodes. The PEG layer is neutral, so this is the right combination rule; a
     * charged layer would need the full Donnan equation instead.
     */
    val saltPartitionCoefficient: Double
        get() = (magnesiumPartitionCoefficient * chloridePartitionCoefficient.pow(2.0))
            .pow(1.0 / 3.0)

    /**
     * `λ_in/λ_bulk = 1/√K_salt` — 1.14 to 1.39 across the design window.
     *
     * The sign of this is the interesting part: screening **inside** the layer is *weaker*
     * than in bulk, so the field survives the layer better than a bulk-Debye estimate says.
     * §4(c) is phrased as though ion inclusion were the risk ("mobile ions inside the
     * polymer layer screen the field exactly where we need it"); on this bound the layer
     * partially *protects* the field. That is a change of sign, not of magnitude.
     */
    val debyeLengthRatio: Double get() = 1.0 / sqrt(saltPartitionCoefficient)

}

/** The Nightingale (1959) hydrated radius of `Mg²⁺`, 4.28 Å — **CITED**. */
const val HYDRATED_MAGNESIUM_RADIUS: Double = 0.428

/** The Nightingale (1959) hydrated radius of `Cl⁻`, 3.32 Å — **CITED**. */
const val HYDRATED_CHLORIDE_RADIUS: Double = 0.332

/**
 * Returns the Maxwell-Garnett effective permittivity of polymer inclusions at volume
 * fraction [volumeFraction] in water.
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`ε = ε_w [1 + 3φβ/(1 − φβ)]`, `β = (ε_p − ε_w)/(ε_p + 2ε_w)`
 *
 * Exact in both limits: `ε_w` at `φ = 0` and `ε_p` at `φ = 1`. Chosen over Bruggeman
 * because at `φ ≈ 0.03` the two differ by less than the uncertainty in `ε_p` itself, and
 * Maxwell-Garnett is the dilute-inclusion form, which is the regime this layer is in.
 */
fun maxwellGarnettPermittivity(
    volumeFraction: Double,
    polymerPermittivity: Double = PEG_RELATIVE_PERMITTIVITY,
    waterPermittivity: Double = WATER_RELATIVE_PERMITTIVITY
): Double {
    val beta = (polymerPermittivity - waterPermittivity) /
            (polymerPermittivity + 2.0 * waterPermittivity)
    return waterPermittivity * (1.0 + 3.0 * volumeFraction * beta / (1.0 - volumeFraction * beta))
}

/**
 * Returns the Born self-energy cost in `k_BT` of moving an ion of valency [valency] and
 * radius [ionRadius] nm from a medium of [permittivityOutside] into one of
 * [permittivityInside].
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`ΔG/k_BT = (z² l_B ε_w / 2R)(1/ε_in − 1/ε_out)`
 *
 * where `l_B` is evaluated at the reference permittivity, so that the combination
 * `l_B ε_w` is permittivity-independent and the expression depends only on the ratio.
 */
fun bornTransferEnergy(
    valency: Int,
    ionRadius: Double,
    permittivityInside: Double,
    permittivityOutside: Double = WATER_RELATIVE_PERMITTIVITY
): Double {
    require(ionRadius > 0.0) { "ionRadius must be positive, was: $ionRadius" }
    require(permittivityInside > 0.0) {
        "permittivityInside must be positive, was: $permittivityInside"
    }
    val reference = bjerrumLength(relativePermittivity = permittivityOutside) * permittivityOutside
    return valency * valency * reference / (2.0 * ionRadius) *
            (1.0 / permittivityInside - 1.0 / permittivityOutside)
}

/**
 * Returns Ogston's partition coefficient for a sphere of radius [ionRadius] in a random
 * network of fibres of radius [fibreRadius] at volume fraction [volumeFraction]:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`K = exp[−φ (1 + R/r_f)²]`
 *
 * A hard-sphere/hard-cylinder geometric result — no adjustable parameter — which is why it
 * is the right cheap bound to run before anything expensive.
 */
fun ogstonPartitionCoefficient(
    volumeFraction: Double,
    ionRadius: Double,
    fibreRadius: Double
): Double {
    require(fibreRadius > 0.0) { "fibreRadius must be positive, was: $fibreRadius" }
    val ratio = 1.0 + ionRadius / fibreRadius
    return exp(-volumeFraction * ratio * ratio)
}
