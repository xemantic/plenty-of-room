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
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.sqrt

/**
 * The electrolyte side of task `T-6`.
 *
 * ## Fundamental constants
 *
 * `Physics.kt` locks the unit system and carries `k_B` and the electronvolt.
 * Electrostatics needs three more SI constants — the elementary charge, the vacuum
 * permittivity and the Avogadro constant — which live here for now because `T-6` is the
 * first task to need them. **They are candidates for promotion into `Physics.kt`** once a
 * second task uses them; until then keeping them local avoids editing a file three agents
 * share this session.
 *
 * All three are exact by SI definition except the permittivity, which is CODATA-2018.
 */

/** The elementary charge `e`, exact by SI definition, in coulomb. */
const val ELEMENTARY_CHARGE: Double = 1.602176634e-19

/** The vacuum permittivity `ε₀` in `F/m` — CODATA 2018, `8.8541878128(13)e-12`. */
const val VACUUM_PERMITTIVITY: Double = 8.8541878128e-12

/** The Avogadro constant `N_A`, exact by SI definition, in `1/mol`. */
const val AVOGADRO_CONSTANT: Double = 6.02214076e23

/**
 * The relative permittivity of liquid water at 300 K — **CITED**, `78.0`.
 *
 * The literature value at 298.15 K is 78.3 and at 300 K it is 77.7; `78` is the round
 * number the polyelectrolyte literature uses and the one every cross-check in this task is
 * run against. The sensitivity is carried explicitly: `l_B ∝ 1/ε` and the coupling
 * parameter goes as `l_B²`, so the 3% spread in `ε` is a 6% spread in `Ξ`, which is smaller
 * than the spread between the surface-charge models and does not move any verdict.
 */
const val WATER_RELATIVE_PERMITTIVITY: Double = 78.0

/**
 * The relative permittivity of bulk poly(ethylene oxide) — **CITED**, `5.0`.
 *
 * Only ever used inside a mixing rule at `φ ≈ 0.03`, where the whole polymer contribution
 * to the mixture is a 4% decrement, so the difference between 4 and 6 is invisible.
 */
const val PEG_RELATIVE_PERMITTIVITY: Double = 5.0

/**
 * Returns the Bjerrum length `l_B = e²/(4πε₀ε_r k_BT)` in **nm**.
 *
 * The distance at which the Coulomb energy of two elementary charges equals `k_BT`;
 * 0.714 nm in water at 300 K. Everything in this task is measured against it, so it is
 * derived from SI constants here rather than cited as "0.7 nm".
 *
 * @throws IllegalArgumentException if [temperature] or [relativePermittivity] is not positive.
 */
fun bjerrumLength(
    temperature: Double = ROOM_TEMPERATURE,
    relativePermittivity: Double = WATER_RELATIVE_PERMITTIVITY
): Double {
    require(relativePermittivity > 0.0) {
        "relativePermittivity must be positive, was: $relativePermittivity"
    }
    // thermalEnergy is pN*nm; 1 pN*nm = 1e-21 J
    val thermalEnergyJoule = thermalEnergy(temperature) * 1e-21
    val metres = ELEMENTARY_CHARGE * ELEMENTARY_CHARGE /
            (4.0 * PI * VACUUM_PERMITTIVITY * relativePermittivity * thermalEnergyJoule)
    return metres * 1e9
}

/**
 * Returns the thermal voltage `k_BT/e` in **volt** — 25.85 mV at 300 K.
 *
 * The natural unit of every electrode potential in this task: a bias is "large" or "small"
 * only relative to this.
 */
fun thermalVoltage(temperature: Double = ROOM_TEMPERATURE): Double =
    thermalEnergy(temperature) * 1e-21 / ELEMENTARY_CHARGE

/**
 * Returns the Coulomb interaction energy of two point charges of valency [charge1] and
 * [charge2] at [separation] nm, in units of `k_BT`.
 *
 * @throws IllegalArgumentException if [separation] is not positive.
 */
fun coulombEnergy(
    charge1: Int,
    charge2: Int,
    separation: Double,
    temperature: Double = ROOM_TEMPERATURE,
    relativePermittivity: Double = WATER_RELATIVE_PERMITTIVITY
): Double {
    require(separation > 0.0) { "separation must be positive, was: $separation" }
    return charge1 * charge2 * bjerrumLength(temperature, relativePermittivity) / separation
}

/** Converts a molar concentration in `mM` to a number density in `nm⁻³`. */
fun millimolarToPerCubicNanometre(millimolar: Double): Double =
    millimolar * 1e-3 * AVOGADRO_CONSTANT * 1e-24

/** Converts a number density in `nm⁻³` to a molar concentration in `mM`. */
fun perCubicNanometreToMillimolar(perCubicNanometre: Double): Double =
    perCubicNanometre / (1e-3 * AVOGADRO_CONSTANT * 1e-24)

/**
 * The Gen-1 buffer: `MgCl₂` in water at the §3 concentrations of 2, 5 or 10 mM.
 *
 * ## Why the salt type is in the type
 *
 * `MgCl₂` is a **2:1** electrolyte, so its ionic strength is `3c`, not `c`:
 * `I = ½ Σ c_i z_i² = ½(4c + 2c)`. A monovalent intuition understates the screening by a
 * factor of three, i.e. the Debye length by `√3`. §3 quotes "~4 nm at 2 mM Mg²⁺" without
 * derivation, and the point of this class is that the quoted number is reproduced from the
 * stoichiometry rather than carried forward.
 *
 * @param concentration the `MgCl₂` molarity in `mM`.
 */
@Serializable
data class MagnesiumChlorideBuffer(
    val concentration: Double
) {

    init {
        require(concentration > 0.0) { "concentration must be positive, was: $concentration" }
    }

    /** `I = ½ Σ c_i z_i² = 3c` in `mM` — **DERIVED** from the 2:1 stoichiometry. */
    val ionicStrength: Double get() = 3.0 * concentration

    /** The `Mg²⁺` number density in `nm⁻³`. */
    val magnesiumNumberDensity: Double get() = millimolarToPerCubicNanometre(concentration)

    /** The `Cl⁻` number density in `nm⁻³` — twice the magnesium, by electroneutrality. */
    val chlorideNumberDensity: Double get() = 2.0 * magnesiumNumberDensity

    /**
     * Returns the inverse Debye length `κ = sqrt(8π l_B I)` in `nm⁻¹`,
     * with `I` the ionic strength as a number density.
     */
    fun inverseDebyeLength(
        temperature: Double = ROOM_TEMPERATURE,
        relativePermittivity: Double = WATER_RELATIVE_PERMITTIVITY
    ): Double = sqrt(
        8.0 * PI * bjerrumLength(temperature, relativePermittivity) *
                millimolarToPerCubicNanometre(ionicStrength)
    )

    /**
     * Returns the Debye screening length `λ_D = 1/κ` in nm.
     *
     * 3.93 / 2.48 / 1.76 nm at 2 / 5 / 10 mM, 300 K, `ε_r = 78`.
     *
     * **This is a bulk-reservoir quantity.** It is *not* the decay length of the field in
     * the gap under the tile — see [CounterionDominatedGap], where the tile's own
     * counterions shorten it by a factor of three to five.
     */
    fun debyeLength(
        temperature: Double = ROOM_TEMPERATURE,
        relativePermittivity: Double = WATER_RELATIVE_PERMITTIVITY
    ): Double = 1.0 / inverseDebyeLength(temperature, relativePermittivity)

}
