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

package com.xemantic.nano.plentyofroom.poroelastic

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import kotlin.math.pow

/**
 * The solvent, in the units this project locked.
 *
 * Task `T-7` is linear in the viscosity — every drainage time in it is `η × geometry`
 * — so the viscosity is evaluated from a correlation at the stated temperature rather
 * than typed in from a handbook row. Near 300 K water thins by 2.3 % per kelvin, so
 * quoting the familiar 20 °C value at 300 K would be a 17 % error in the answer.
 *
 * These three constants and [waterViscosity] are generic enough that they may be worth
 * promoting into `Physics.kt` once another task needs them; they live here because
 * `T-7` owns this package and `Physics.kt` belongs to another agent this session.
 */

/** One `Pa·s`, expressed in the locked viscosity unit `pN·s/nm²`. */
const val PASCAL_SECOND: Double = 1e-6

/**
 * One kilogram, expressed in the locked mass unit `pN·s²/nm`.
 *
 * Mass is not part of the `P-2` unit table because nothing before `T-7` needed it;
 * it follows from `F = m a` and the locked force and length: `1 pN·s²/nm = 1e-3 kg`.
 */
const val KILOGRAM: Double = 1e3

/** One `g/cm³` of mass density, expressed in `pN·s²/nm⁴`. */
const val GRAM_PER_CUBIC_CENTIMETRE: Double = 1e-21

/**
 * Returns the dynamic viscosity of liquid water at [temperature] K, in `pN·s/nm²`.
 *
 * Evaluated from the Vogel-type correlation `η = 2.414e-5 · 10^(247.8/(T − 140))` Pa·s,
 * which is the standard engineering fit over the whole liquid range. **CITED**, and
 * cross-checked in `WaterTest` against the IAPWS reference value at 20 °C, which it
 * reproduces to 0.02 % without having been fitted to it here.
 *
 * At the locked 300 K this is `8.541e-4 Pa·s = 8.541e-10 pN·s/nm²`.
 *
 * The Gen-1 medium is 2–10 mM MgCl₂, not pure water. At that ionic strength the
 * viscosity increment is below 0.1 %, far under the fit's own accuracy, so the
 * distinction is stated rather than modelled. What is **not** covered is the viscosity
 * *inside* the polymer layer: Darcy's law is written with the bulk solvent viscosity
 * and an effective permeability, which is the convention every permeability model in
 * [LayerPermeability] was constructed under, and mixing the two would double-count.
 *
 * @throws IllegalArgumentException if [temperature] is outside the liquid range.
 */
fun waterViscosity(temperature: Double = ROOM_TEMPERATURE): Double {
    require(temperature in FREEZING_POINT..BOILING_POINT) {
        "temperature must be within [$FREEZING_POINT, $BOILING_POINT] K, was: $temperature"
    }
    return 2.414e-5 * 10.0.pow(247.8 / (temperature - 140.0)) * PASCAL_SECOND
}

/** The lower end of the correlation's validity, 0 °C. */
const val FREEZING_POINT: Double = 273.15

/** The upper end of the correlation's validity, 100 °C. */
const val BOILING_POINT: Double = 373.15
