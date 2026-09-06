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

import kotlin.math.PI
import kotlin.math.sin

/**
 * The molecular arithmetic that turns a polymer's chemistry into the lengths and volumes
 * a scaling law needs — derived, never transcribed.
 *
 * §7 of the problem definition asks that inherited numbers be re-derived. For a polymer
 * material sheet that means the monomer's molar mass comes from its formula, its volume
 * from a measured specific volume, and its extended length from bond geometry.
 * Only quantities that genuinely require a measurement are allowed to be cited.
 */

/** The Avogadro constant, in `mol⁻¹`. Exact, by the 2019 SI redefinition. */
const val AVOGADRO_CONSTANT: Double = 6.02214076e23

/** Standard atomic weight of carbon, in `g/mol`. */
const val CARBON_MOLAR_MASS: Double = 12.011

/** Standard atomic weight of hydrogen, in `g/mol`. */
const val HYDROGEN_MOLAR_MASS: Double = 1.008

/** Standard atomic weight of oxygen, in `g/mol`. */
const val OXYGEN_MOLAR_MASS: Double = 15.999

/**
 * Returns the molar mass in `g/mol` of a molecule of the given atomic composition.
 *
 * Only the three elements the Gen-1 polymers are built from are supported,
 * because supporting more would be scope the project does not have.
 *
 * @throws IllegalArgumentException if any count is negative, or all are zero.
 */
fun molarMass(
    carbon: Int = 0,
    hydrogen: Int = 0,
    oxygen: Int = 0
): Double {
    require(carbon >= 0) { "carbon count must not be negative, was: $carbon" }
    require(hydrogen >= 0) { "hydrogen count must not be negative, was: $hydrogen" }
    require(oxygen >= 0) { "oxygen count must not be negative, was: $oxygen" }
    require(carbon + hydrogen + oxygen > 0) { "a molecule must contain at least one atom" }
    return carbon * CARBON_MOLAR_MASS +
            hydrogen * HYDROGEN_MOLAR_MASS +
            oxygen * OXYGEN_MOLAR_MASS
}

/**
 * Returns the volume in nm³ occupied by one monomer of [molarMass] `g/mol`
 * in a medium where the polymer's [partialSpecificVolume] is `mL/g`.
 *
 * `v₀ = M V̄ / N_A`. The partial specific volume — rather than the bulk mass density —
 * is what a **dissolved** chain displaces, and the two differ for PEG in water,
 * which contracts on hydration. Since the layer is under aqueous buffer, this is the
 * conversion that belongs in a volume fraction.
 *
 * @throws IllegalArgumentException if either argument is not positive.
 */
fun monomerVolume(
    molarMass: Double,
    partialSpecificVolume: Double
): Double {
    require(molarMass > 0.0) { "molarMass must be positive, was: $molarMass" }
    require(partialSpecificVolume > 0.0) {
        "partialSpecificVolume must be positive, was: $partialSpecificVolume"
    }
    return molarMass * partialSpecificVolume / AVOGADRO_CONSTANT * CUBIC_NANOMETRES_PER_MILLILITRE
}

/**
 * Returns the partial specific volume in `mL/g` implied by a [monomerVolume] in nm³ —
 * the inverse of [monomerVolume], carried so the conversion can be checked in both directions.
 *
 * @throws IllegalArgumentException if either argument is not positive.
 */
fun partialSpecificVolume(
    molarMass: Double,
    monomerVolume: Double
): Double {
    require(molarMass > 0.0) { "molarMass must be positive, was: $molarMass" }
    require(monomerVolume > 0.0) { "monomerVolume must be positive, was: $monomerVolume" }
    return monomerVolume * AVOGADRO_CONSTANT / (molarMass * CUBIC_NANOMETRES_PER_MILLILITRE)
}

/**
 * Returns the length in nm that one repeat unit contributes to a fully extended
 * all-trans backbone of [backboneBondLengths] nm, at [backboneBondAngle] degrees.
 *
 * Each bond projects `l·sin(θ/2)` onto the chain axis of a planar zigzag.
 * This is the **contour** length per monomer, which is the quantity the Alexander-de Gennes
 * effective monomer length turns out to be — and it is *not* the cube root of the
 * monomer volume, which is what makes carrying both worthwhile.
 *
 * A single bond angle is a simplification for an ether backbone, where `C-O-C` and `O-C-C`
 * differ by a couple of degrees. The error that hides is bounded and small, and is
 * pinned by a test rather than asserted here.
 *
 * @throws IllegalArgumentException if there are no bonds, any bond is not positive,
 *          or the angle is outside `(0, 180)`.
 */
fun allTransContourLength(
    backboneBondLengths: List<Double>,
    backboneBondAngle: Double
): Double {
    require(backboneBondLengths.isNotEmpty()) { "backboneBondLengths must not be empty" }
    require(backboneBondLengths.all { it > 0.0 }) {
        "every backbone bond length must be positive, was: $backboneBondLengths"
    }
    require(backboneBondAngle > 0.0 && backboneBondAngle < 180.0) {
        "backboneBondAngle must be within (0.0, 180.0), was: $backboneBondAngle"
    }
    return backboneBondLengths.sum() * sin(backboneBondAngle / 2.0 * PI / 180.0)
}

/** `1 mL = 10²¹ nm³`, the only unit conversion in the sheet, kept in one place. */
private const val CUBIC_NANOMETRES_PER_MILLILITRE: Double = 1e21
