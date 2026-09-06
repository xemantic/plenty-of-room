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

package com.xemantic.nano.plentyofroom

import kotlin.math.sqrt

/**
 * The unit system locked for the whole programme, per §5 of the problem definition.
 *
 * Everything is expressed in nanometres, piconewtons, and kelvin, from which:
 * - energy is `pN·nm`,
 * - pressure is `pN/nm²`, which is exactly `1 MPa`,
 * - stiffness is `pN/nm`, which is exactly `1 mN/m`.
 *
 * These are SI, merely scaled: no conversion factor is hidden in any formula.
 * Energies are additionally reported in `k_BT` and in eV where a task calls for it,
 * via [ELECTRON_VOLT].
 */

/** The Boltzmann constant, `1.380649e-23 J/K`, expressed in `pN·nm/K`. */
const val BOLTZMANN_CONSTANT: Double = 1.380649e-2

/** One electronvolt, `1.602176634e-19 J`, expressed in `pN·nm`. */
const val ELECTRON_VOLT: Double = 160.2176634

/** The operating temperature of the Gen-1 stack, in kelvin, per §3 of the problem definition. */
const val ROOM_TEMPERATURE: Double = 300.0

/**
 * Returns the thermal energy `k_BT` in `pN·nm` at [temperature].
 *
 * At the locked 300 K this is `4.142 pN·nm`, the value the problem definition
 * and the NDI V&V matrix both quote.
 *
 * @throws IllegalArgumentException if [temperature] is not positive.
 */
fun thermalEnergy(temperature: Double = ROOM_TEMPERATURE): Double {
    require(temperature > 0.0) { "temperature must be positive, was: $temperature" }
    return BOLTZMANN_CONSTANT * temperature
}

/**
 * Returns the RMS positional fluctuation `sqrt(k_BT/k)` in nm
 * of a harmonic degree of freedom of [stiffness] in `pN/nm` at [temperature].
 *
 * This is equipartition, and it is the verification gate 3 hand-off:
 * the same stiffness that sets the actuation stroke sets the thermal position noise.
 *
 * @throws IllegalArgumentException if [stiffness] is not positive.
 */
fun equipartitionRms(
    stiffness: Double,
    temperature: Double = ROOM_TEMPERATURE
): Double {
    require(stiffness > 0.0) { "stiffness must be positive, was: $stiffness" }
    return sqrt(thermalEnergy(temperature) / stiffness)
}

/**
 * Returns the stiffness `k_BT/σ²` in `pN/nm` required to hold a degree of freedom
 * to [positionalRms] in nm at [temperature] — the inverse of [equipartitionRms].
 *
 * @throws IllegalArgumentException if [positionalRms] is not positive.
 */
fun equipartitionStiffness(
    positionalRms: Double,
    temperature: Double = ROOM_TEMPERATURE
): Double {
    require(positionalRms > 0.0) { "positionalRms must be positive, was: $positionalRms" }
    return thermalEnergy(temperature) / (positionalRms * positionalRms)
}
