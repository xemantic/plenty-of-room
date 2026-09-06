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

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * The confined gap and the electrode.
 *
 * Everything in this file is **Poisson-Boltzmann**, deliberately. `T-6` has to separate two
 * different findings that are easy to conflate:
 *
 * - *"PB says something a bulk-buffer intuition does not expect"* — charge saturation, and
 *   counterion domination of the gap. These are mean-field results and they stand.
 * - *"PB is wrong here"* — the coupling-parameter analysis in `ChargedSurface.kt`.
 *
 * Only the second is a validity statement. The first is what the validity statement has to
 * be measured *against*, and getting it wrong would make the deviation look larger or
 * smaller than it is.
 */

/**
 * The gap between the tile and the electrode, counted as an ion inventory.
 *
 * ## The finding
 *
 * The tile's own counterions outnumber the bulk buffer's contribution to the gap by 3 to
 * 33× across the whole §3 box. So the gap is **not** at the bulk composition, and two
 * things follow:
 *
 * 1. Netz's counterion-only criteria are the *appropriate* tool here, not merely the
 *    available one — the approximation they make is the one this geometry actually is.
 * 2. §3's "Debye length ~4 nm" is a bulk-reservoir number and is **not** the decay length
 *    of the field in the gap. [localScreeningLength] is 0.84–1.18 nm instead.
 *
 * @param gapHeight the tile-electrode separation in nm.
 * @param chargeFraction the fraction of the tile's bare charge that is *not* condensed —
 *          pass [DnaOrigamiTile.manningSurvivingFraction], or 1.0 for the bare bound.
 */
@Serializable
data class CounterionDominatedGap(
    val tile: DnaOrigamiTile,
    val buffer: MagnesiumChlorideBuffer,
    val gapHeight: Double,
    val counterionValency: Int,
    val chargeFraction: Double
) {

    init {
        require(gapHeight > 0.0) { "gapHeight must be positive, was: $gapHeight" }
        require(counterionValency > 0) {
            "counterionValency must be positive, was: $counterionValency"
        }
        require(chargeFraction > 0.0 && chargeFraction <= 1.0) {
            "chargeFraction must be in (0, 1], was: $chargeFraction"
        }
    }

    /** The gap volume in nm³. */
    val volume: Double get() = tile.footprintArea * gapHeight

    /**
     * How many counterions the gap must hold, by electroneutrality.
     *
     * Half the tile's effective charge faces the gap — the tile is a slab and screens on
     * both sides — and each counterion carries `q` charges.
     */
    val counterionsRequired: Double
        get() = tile.nucleotides * chargeFraction / 2.0 / counterionValency

    /** How many `Mg²⁺` the bulk buffer would put in that volume at its own concentration. */
    val bulkCounterionsAvailable: Double get() = buffer.magnesiumNumberDensity * volume

    /** The ratio of the two. Above 1 means the gap is counterion-dominated. */
    val dominanceRatio: Double get() = counterionsRequired / bulkCounterionsAvailable

    /** The counterion number density in the gap in `nm⁻³`, if spread uniformly. */
    val counterionNumberDensity: Double get() = counterionsRequired / volume

    /**
     * Returns the Debye-Hückel screening length in nm of the counterion population itself,
     * `1/sqrt(4π l_B n q²)`.
     *
     * A uniform-density estimate, and therefore an **upper** bound on the true local
     * screening length: the counterions are concentrated near the surfaces, where they
     * screen harder still. Even so it comes out three to five times shorter than the bulk
     * Debye length, which is the number `T-3` needs and the one §3 does not supply.
     */
    fun localScreeningLength(bjerrumLength: Double): Double = 1.0 / sqrt(
        4.0 * PI * bjerrumLength * counterionNumberDensity *
                counterionValency * counterionValency
    )

}

/** The close-packing fraction of equal spheres in an fcc lattice. */
private const val CLOSE_PACKING_FRACTION: Double = 0.74048048969306

/**
 * Returns the close-packed number density in `nm⁻³` of hard spheres of radius [radius] nm.
 *
 * The ceiling a point-ion theory has no way of respecting, and therefore the sharpest
 * statement of where a point-ion theory stops being physical.
 */
fun closePackedNumberDensity(radius: Double): Double {
    require(radius > 0.0) { "radius must be positive, was: $radius" }
    return CLOSE_PACKING_FRACTION / (4.0 / 3.0 * PI * radius * radius * radius)
}

/**
 * Returns the diffuse-layer potential in **volt** at which point-ion Poisson-Boltzmann
 * first predicts a contact density above close packing:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`ψ_max = (k_BT/ze) ln(n_max/n_bulk)`
 *
 * Beyond it the Boltzmann factor is asking for more ions than fit, the compact (Stern)
 * layer takes over the potential drop, and the electrode's charge is no longer given by
 * Gouy-Chapman. 0.197 V for `Cl⁻` at a positive electrode in 2 mM `MgCl₂`; 0.097 V for
 * `Mg²⁺` at a negative one. **The §3 bias target of ≤ 2 V is ten times that.**
 *
 * The dependence on concentration is logarithmic, so no buffer in the §3 range rescues it.
 */
fun stericSaturationPotential(
    valency: Int,
    bulkNumberDensity: Double,
    ionRadius: Double,
    temperature: Double = ROOM_TEMPERATURE
): Double {
    require(valency > 0) { "valency must be positive, was: $valency" }
    require(bulkNumberDensity > 0.0) {
        "bulkNumberDensity must be positive, was: $bulkNumberDensity"
    }
    return thermalVoltage(temperature) / valency *
            ln(closePackedNumberDensity(ionRadius) / bulkNumberDensity)
}

/**
 * Returns the surface charge density in `e/nm²` per volt implied by a compact-layer
 * capacitance of [microFaradPerSquareCentimetre].
 *
 * The replacement for Gouy-Chapman above [stericSaturationPotential]: a typical aqueous
 * Stern capacitance of 20 µF/cm² gives 1.25 e/nm² per volt, essentially independent of
 * the buffer. This is the model `T-3` should use to convert a bias into an electrode charge.
 */
fun sternChargeDensityPerVolt(microFaradPerSquareCentimetre: Double): Double {
    require(microFaradPerSquareCentimetre > 0.0) {
        "microFaradPerSquareCentimetre must be positive, was: $microFaradPerSquareCentimetre"
    }
    // uF/cm^2 -> F/m^2 is x1e-2; F/m^2 -> C/nm^2 is x1e-18; C -> e is /e
    return microFaradPerSquareCentimetre * 1e-2 * 1e-18 / ELEMENTARY_CHARGE
}

/**
 * Returns the saturated far-field effective surface charge density in `e/nm²`,
 * `σ_eff = κ/(π l_B z)`.
 *
 * Gouy-Chapman charge saturation: the far field of a planar double layer is
 * `ψ → (4k_BT/ze)γ e^{−κx}` with `γ = tanh(zeψ₀/4k_BT)`, and `γ → 1` for any strongly
 * charged surface. So **the far field stops responding to the bare charge entirely**, and
 * the effective charge is set by the salt, not by the surface.
 *
 * At 2 mM with `Mg²⁺` counterions this is 0.057 e/nm² — 118× below the tile's bare
 * projected charge and 14× below its Manning-renormalised one. It is a *mean-field* result,
 * so it constrains what a correct PB calculation may return, not the accuracy of PB.
 *
 * **Premise, stated rather than inherited:** the Gouy-Chapman closed form this is read from is
 * the **symmetric `z:z`** one. `MgCl₂` is 2:1, and its exact planar solution is not this
 * expression. The *saturation* itself is generic — it comes from `tanh` bounding the effective
 * surface potential, which happens in any electrolyte — but the prefactor carries a
 * 2:1-versus-2:2 error of order tens of per cent. Treated accordingly: as an order-of-magnitude
 * ceiling that rules out using the bare charge, not as a number to compute a force from.
 */
fun saturatedEffectiveChargeDensity(
    inverseDebyeLength: Double,
    valency: Int,
    bjerrumLength: Double
): Double {
    require(valency > 0) { "valency must be positive, was: $valency" }
    return inverseDebyeLength / (PI * bjerrumLength * valency)
}

/** Returns `4k_BT/(ze)` in volt — the saturated apparent surface potential, 51.7 mV at `z = 2`. */
fun saturatedSurfacePotential(
    valency: Int,
    temperature: Double = ROOM_TEMPERATURE
): Double {
    require(valency > 0) { "valency must be positive, was: $valency" }
    return 4.0 * thermalVoltage(temperature) / valency
}
