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

package com.xemantic.nano.plentyofroom.brush

import kotlin.math.PI
import kotlin.math.pow

/**
 * The grafted polymer layer of the Gen-1 stack, as three numbers.
 *
 * ## Geometry and sign conventions
 *
 * The `z` axis is normal to the electrode, positive away from it,
 * with the origin at the top of the electrode (or of the thin high-k dielectric, if present).
 * Chains are grafted at `z = 0` and the layer occupies `0 < z < L`.
 * The DNA-origami tile is a wall at height `h`, and compression means `h < L₀`.
 *
 * @param monomerSize the monomer size `a`, in nm.
 * @param monomersPerChain the number of monomers per chain `N`.
 *          Continuous rather than integral, because chain length is swept as a design variable
 *          and the layer height is inverted for it in [brushOfHeight].
 * @param graftingDensity the areal density of grafted chains `σ`, in `nm⁻²`.
 */
data class PolymerBrush(
    val monomerSize: Double,
    val monomersPerChain: Double,
    val graftingDensity: Double
) {

    init {
        require(monomerSize > 0.0) { "monomerSize must be positive, was: $monomerSize" }
        require(monomersPerChain >= 1.0) {
            "monomersPerChain must be at least 1, was: $monomersPerChain"
        }
        require(graftingDensity > 0.0) {
            "graftingDensity must be positive, was: $graftingDensity"
        }
    }

    /** The mean distance between grafting points, `s = σ^(−1/2)`, in nm. */
    val graftingSpacing: Double get() = 1.0 / graftingDensity.pow(0.5)

    /**
     * The Flory radius of the same chain free in a good solvent, `R_F = a N^(3/5)`, in nm.
     *
     * This is the length the grafting spacing has to be compared against
     * to decide whether the layer is a brush at all.
     */
    val floryRadius: Double get() = monomerSize * monomersPerChain.pow(3.0 / 5.0)

    /**
     * The reduced grafting density `Σ = σ π R_F²`, the number of chains
     * whose unperturbed coils would occupy one coil's footprint.
     *
     * `Σ < 1` means the coils do not touch, and there is no brush.
     */
    val reducedGraftingDensity: Double
        get() = graftingDensity * PI * floryRadius * floryRadius

    /**
     * The grafting regime, which §4(a) of the problem definition asks us to locate a window in.
     *
     * The boundary at `Σ = 1` is unambiguous — it is the onset of coil overlap.
     * The upper boundary of [GraftingRegime.CROSSOVER] is a convention, taken here as `Σ = 5`,
     * above which the Alexander-de Gennes stretching picture is normally applied without apology.
     * Between the two the layer is a weak brush and every scaling result carries a large prefactor
     * uncertainty; results landing there must say so.
     */
    val regime: GraftingRegime
        get() = when {
            reducedGraftingDensity < 1.0 -> GraftingRegime.MUSHROOM
            reducedGraftingDensity < BRUSH_ONSET -> GraftingRegime.CROSSOVER
            else -> GraftingRegime.BRUSH
        }

    /**
     * The Alexander-de Gennes equilibrium height `L₀ ≃ N a (a/s)^(2/3) = N a^(5/3) σ^(1/3)`, in nm.
     *
     * Scaling result: the prefactor is unity by convention, not by derivation,
     * and this is one of the places §2 of the problem definition warns about.
     */
    val alexanderDeGennesHeight: Double
        get() = monomersPerChain * monomerSize.pow(5.0 / 3.0) * graftingDensity.pow(1.0 / 3.0)

    /**
     * Returns the mean polymer volume fraction `φ = N σ a³ / h` when the layer is held at [height].
     *
     * The grafted material is conserved, so `φ ∝ 1/h`, which is what makes the osmotic
     * exponent of `Π(φ)` appear directly in the compression law.
     *
     * @throws IllegalArgumentException if [height] is not positive.
     */
    fun meanVolumeFraction(height: Double): Double {
        require(height > 0.0) { "height must be positive, was: $height" }
        return monomersPerChain * graftingDensity * monomerSize.pow(3.0) / height
    }

    companion object {
        /** The conventional reduced grafting density above which the layer is treated as a brush. */
        const val BRUSH_ONSET: Double = 5.0
    }

}

/** Whether the grafted chains overlap, and how strongly. */
enum class GraftingRegime {

    /** `Σ < 1` — the coils do not touch. Compliant, but not a brush; §4(a) rules this out. */
    MUSHROOM,

    /** `1 ≤ Σ < 5` — the coils overlap but the chains are barely stretched. Scaling results are weak here. */
    CROSSOVER,

    /** `Σ ≥ 5` — a brush, and the regime in which the Alexander-de Gennes picture is applied. */
    BRUSH
}

/**
 * Returns the brush whose Alexander-de Gennes height is [height],
 * by inverting `L₀ = N a^(5/3) σ^(1/3)` for the chain length.
 *
 * §3 of the problem definition specifies the layer by its height (5 / 7 / 10 nm)
 * and leaves the grafting density open, which is the opposite parameterisation
 * from the one the scaling law is written in.
 *
 * @throws IllegalArgumentException if [height] is not positive,
 *          or if it demands a chain shorter than one monomer.
 */
fun brushOfHeight(
    height: Double,
    graftingDensity: Double,
    monomerSize: Double
): PolymerBrush {
    require(height > 0.0) { "height must be positive, was: $height" }
    require(graftingDensity > 0.0) { "graftingDensity must be positive, was: $graftingDensity" }
    require(monomerSize > 0.0) { "monomerSize must be positive, was: $monomerSize" }
    return PolymerBrush(
        monomerSize = monomerSize,
        monomersPerChain = height /
                (monomerSize.pow(5.0 / 3.0) * graftingDensity.pow(1.0 / 3.0)),
        graftingDensity = graftingDensity
    )
}
