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

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * PEG in water — the material parameter sheet of task `P-3`.
 *
 * ## The problem this type exists to solve
 *
 * The brush literature writes **three different quantities with the letter `a`**, and
 * `C-0001` inherited one of them without knowing which:
 *
 * | quantity | value | where it belongs |
 * |---|---|---|
 * | [effectiveMonomerLength] `a` | 0.35 nm | inside Alexander-de Gennes expressions only |
 * | [volumetricMonomerSize] `v₀^(1/3)` | 0.392 nm | anywhere a *volume* is meant |
 * | [kuhnLength] `b` | 1.1 nm | Gaussian chain elasticity |
 *
 * They differ by up to a factor of three, and their cubes — which is how they enter a
 * volume fraction — by a factor of 39. Substituting one for another is the single
 * most likely way to get this material wrong, so they are separate properties with
 * separate names and none of them is called `a`.
 *
 * **Convention, fixed and enforced downstream:** a volume fraction in this project is
 * always the *physical* one, computed with [monomerVolume]. Where a source defines `φ`
 * as a reduced density `n a³`, it is converted on the way in; [volumeFractionCorrection]
 * is that conversion.
 *
 * ## Provenance
 *
 * Everything computed from the constructor arguments is `DERIVED`. The arguments themselves
 * are `MEASURED` or `CITED` and each says which. Task `P-3` and claim `C-0002` carry the sheet
 * with its sources; this class carries the arithmetic.
 *
 * @param partialSpecificVolume `V̄` in `mL/g` — **CITED**, and load-bearing: it is the value
 *          the adopted equation of state's volume fractions were computed with, so it must
 *          not be swapped for a bulk density without re-fitting `α`.
 * @param crossoverIndex `α` — **MEASURED**, `0.49 ± 0.01`, fitted by Cohen et al. (2009) to
 *          osmometry on twelve PEG molecular weights in water at 20 °C, `r² = 0.9926`.
 * @param crossoverIndexUncertainty the `± 0.01` of that fit.
 * @param effectiveMonomerLength `a` in nm — **CITED** (Kenworthy et al. 1995) and
 *          **corroborated twice**: by [allTransContourLength] derived here from bond geometry,
 *          and by unconstrained Alexander-de Gennes fits to PEG-brush compression giving
 *          `0.356 ± 0.07` and `0.330 ± 0.15` nm (Hansen et al. 2003).
 * @param kuhnLength `b` in nm — **CITED**, Rubinstein & Colby Table 2.1 for PEO.
 * @param kuhnMolarMass the molar mass of one Kuhn segment in `g/mol` — **CITED**, same source.
 * @param thetaTemperature in K — **CITED**, PEO/water phase-separates near 102 °C.
 * @param backboneBondLengths the `-CH₂-CH₂-O-` backbone, in nm — **CITED** standard bond lengths.
 * @param backboneBondAngle the backbone bond angle in degrees — **CITED**, tetrahedral.
 */
@Serializable
data class PegWater(
    val partialSpecificVolume: Double = 0.825,
    val crossoverIndex: Double = 0.49,
    val crossoverIndexUncertainty: Double = 0.01,
    val effectiveMonomerLength: Double = 0.35,
    val kuhnLength: Double = 1.1,
    val kuhnMolarMass: Double = 137.0,
    val thetaTemperature: Double = 375.0,
    val backboneBondLengths: List<Double> = listOf(0.153, 0.143, 0.143),
    val backboneBondAngle: Double = 112.0
) {

    init {
        require(crossoverIndex > 0.0) { "crossoverIndex must be positive, was: $crossoverIndex" }
        require(effectiveMonomerLength > 0.0) {
            "effectiveMonomerLength must be positive, was: $effectiveMonomerLength"
        }
        require(kuhnLength > 0.0) { "kuhnLength must be positive, was: $kuhnLength" }
        require(kuhnMolarMass > 0.0) { "kuhnMolarMass must be positive, was: $kuhnMolarMass" }
        require(thetaTemperature > 0.0) {
            "thetaTemperature must be positive, was: $thetaTemperature"
        }
    }

    /** `M₀` in `g/mol` for the ethylene oxide repeat unit `C₂H₄O` — **DERIVED**. */
    val monomerMolarMass: Double get() = molarMass(carbon = 2, hydrogen = 4, oxygen = 1)

    /** `v₀ = M₀ V̄ / N_A` in nm³ — **DERIVED**. 0.0604 nm³ per ethylene oxide unit. */
    val monomerVolume: Double
        get() = monomerVolume(monomerMolarMass, partialSpecificVolume)

    /**
     * `v₀^(1/3)` in nm — **DERIVED**. The size of a cube of one monomer's volume, 0.392 nm.
     *
     * This is what "monomer size" means in a volume fraction, and it is *not*
     * [effectiveMonomerLength].
     */
    val volumetricMonomerSize: Double get() = monomerVolume.pow(1.0 / 3.0)

    /**
     * The mass density in `g/cm³` implied by the partial specific volume — **DERIVED**.
     *
     * 1.212 g/cm³, which is *above* bulk amorphous PEO at 1.12–1.13 g/cm³.
     * The difference is real: PEG contracts on hydration. It matters because a volume
     * fraction computed from the bulk density would be 8% too large.
     */
    val massDensity: Double get() = 1.0 / partialSpecificVolume

    /**
     * The all-trans contour length per monomer in nm — **DERIVED** from bond geometry.
     *
     * 0.364 nm, which lands within 4% of the cited [effectiveMonomerLength] and inside the
     * band that independent brush fits return. That agreement is what identifies `a` as a
     * *contour* length rather than a volumetric one, and is the whole reason the two must
     * not be interchanged.
     */
    val allTransContourLength: Double
        get() = allTransContourLength(backboneBondLengths, backboneBondAngle)

    /**
     * `v₀ / a³` — **DERIVED**, 1.408.
     *
     * The factor by which an Alexander-de Gennes *reduced* density `n a³` understates the
     * physical volume fraction. `C-0001` reported volume fractions in reduced units without
     * knowing it; multiplying by this recovers the physical ones.
     */
    val volumeFractionCorrection: Double
        get() = monomerVolume / effectiveMonomerLength.pow(3.0)

    /** How many ethylene oxide units make up one Kuhn segment — **DERIVED**, 3.11. */
    val monomersPerKuhnSegment: Double get() = kuhnMolarMass / monomerMolarMass

    /** The volume of one Kuhn segment in nm³ — **DERIVED**, 0.188 nm³. */
    val kuhnSegmentVolume: Double get() = monomersPerKuhnSegment * monomerVolume

    /**
     * `b³ / v_K` — **DERIVED**, 7.09.
     *
     * How far the Kuhn segment is from space-filling. For a hypothetical polymer whose
     * statistical segment is a sphere of its own volume this would be 1; for PEG it is 7,
     * i.e. the segment is a thin rod. Any scaling law that uses one length for both the
     * segment's extent and its volume is therefore wrong about PEG by a factor of this size
     * *somewhere*, and the sheet's job is to know where.
     */
    val kuhnSegmentAspectRatio: Double get() = kuhnLength.pow(3.0) / kuhnSegmentVolume

    /** The diameter in nm of a cylinder of length `b` and volume `v_K` — **DERIVED**, 0.466 nm. */
    val kuhnSegmentDiameter: Double
        get() = sqrt(4.0 * kuhnSegmentVolume / (PI * kuhnLength))

    /**
     * Returns `1 − T/θ`, the reduced distance from the theta temperature — **DERIVED**.
     *
     * 0.20 at 300 K. Positive means good solvent. It is small enough that PEG/water's
     * excluded volume is weak, which is the physical reason the dilute→semidilute crossover
     * reaches as high a volume fraction as it does.
     */
    fun reducedTemperature(temperature: Double = ROOM_TEMPERATURE): Double =
        1.0 - temperature / thetaTemperature

    /**
     * Returns the **physical** polymer volume fraction `φ = N σ v₀ / h` of a layer of
     * [monomersPerChain] chains grafted at [graftingDensity] `nm⁻²` and held at [layerHeight] nm.
     *
     * @throws IllegalArgumentException if any argument is not positive.
     */
    fun volumeFraction(
        monomersPerChain: Double,
        graftingDensity: Double,
        layerHeight: Double
    ): Double {
        require(monomersPerChain > 0.0) {
            "monomersPerChain must be positive, was: $monomersPerChain"
        }
        require(graftingDensity > 0.0) {
            "graftingDensity must be positive, was: $graftingDensity"
        }
        require(layerHeight > 0.0) { "layerHeight must be positive, was: $layerHeight" }
        return monomersPerChain * graftingDensity * monomerVolume / layerHeight
    }

    /** Returns the measured equation of state for a chain of [monomersPerChain] monomers. */
    fun equationOfState(
        monomersPerChain: Double,
        temperature: Double = ROOM_TEMPERATURE
    ): ScalingEquationOfState = ScalingEquationOfState(
        crossoverIndex = crossoverIndex,
        monomerVolume = monomerVolume,
        monomersPerChain = monomersPerChain,
        temperature = temperature
    )

    /**
     * Returns the tension in pN carried by one Gaussian chain of [monomersPerChain] monomers
     * stretched to an end-to-end [extension] in nm: `f = 3 k_BT L / (N_K b²)`.
     *
     * This is the brush's **own** stretching tension, which §2 of the problem definition
     * asks be compared against the ~30 pN at which chain tension is reported to degrade
     * PEG's solvent quality. The comparison is worth making because the answer is not obvious
     * from the outside — see the tests, where it comes out an order of magnitude below.
     *
     * @throws IllegalArgumentException if either argument is not positive.
     */
    fun stretchingTension(
        monomersPerChain: Double,
        extension: Double,
        temperature: Double = ROOM_TEMPERATURE
    ): Double {
        require(monomersPerChain > 0.0) {
            "monomersPerChain must be positive, was: $monomersPerChain"
        }
        require(extension > 0.0) { "extension must be positive, was: $extension" }
        val kuhnSegments = monomersPerChain / monomersPerKuhnSegment
        return 3.0 * thermalEnergy(temperature) * extension /
                (kuhnSegments * kuhnLength * kuhnLength)
    }

}

/**
 * Returns the tension in pN carried by each grafted chain when a [force] in pN is applied
 * to a tile of footprint [area] nm² resting on a layer grafted at [graftingDensity] `nm⁻²`.
 *
 * The load is shared equally, which is the rigid-tile assumption; if the tile dishes
 * (`T-5b`) it is not shared equally and this is a lower bound on the peak.
 *
 * @throws IllegalArgumentException if any argument is not positive.
 */
fun tensionPerChain(
    force: Double,
    graftingDensity: Double,
    area: Double
): Double {
    require(force > 0.0) { "force must be positive, was: $force" }
    require(graftingDensity > 0.0) { "graftingDensity must be positive, was: $graftingDensity" }
    require(area > 0.0) { "area must be positive, was: $area" }
    return force / (graftingDensity * area)
}
