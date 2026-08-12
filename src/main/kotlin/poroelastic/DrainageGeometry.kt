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

import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.math.tanh

/**
 * Where the water has to go when the tile comes down, and how far.
 *
 * ## Geometry and sign conventions, restated
 *
 * `z` is normal to the electrode, positive away from it, origin at the electrode surface.
 * The grafted layer occupies `0 < z < h`; the tile is a rigid, **impermeable**,
 * non-adsorbing plate at `z = h`, and the electrode below is impermeable too. Descent of
 * the tile is `−ż > 0`, and the water it displaces therefore has nowhere to go but
 * **sideways**, out through the perimeter of the footprint, where the pore pressure is
 * taken as zero because beyond the tile edge the layer's upper surface is open to bulk
 * buffer within about one layer thickness.
 *
 * ## The two drainage lengths, defined so they are comparable
 *
 * Both are defined so that the drainage time is `ℓ²/D_p` with the *same* poroelastic
 * diffusivity, which is the only way the comparison the task demanded means anything:
 *
 * - lateral, [RectangularFootprint.effectiveDrainageLength] `= √G`;
 * - vertical, [verticalDrainageLength] `= 2h/π`, the first consolidation mode of a layer
 *   drained on one face — the path that would exist if the origami tile were permeable.
 */
@Serializable
data class RectangularFootprint(
    val length: Double,
    val width: Double,
    val harmonics: Int = DEFAULT_HARMONICS
) {

    init {
        require(length > 0.0) { "length must be positive, was: $length" }
        require(width > 0.0) { "width must be positive, was: $width" }
        require(harmonics > 0) { "harmonics must be positive, was: $harmonics" }
        require(harmonics % 2 == 1) { "harmonics must be odd, was: $harmonics" }
    }

    /** The footprint area in nm². */
    val area: Double get() = length * width

    /**
     * `G` in nm² — the footprint average of the solution of `∇²u = −1` with `u = 0` on
     * the perimeter, which is the Saint-Venant torsion function of the same section.
     *
     * It appears because squeeze-out through a thin layer obeys
     * `∇²p = η v̇ / T` with `p = 0` at the edge, so the mean pore pressure is
     * `p̄ = (η v̇ / T) · G` and the drag coefficient is `η G A / T`.
     *
     * Evaluated from the double Fourier series
     * `G = Σ_{m,n odd} 64 / (π⁶ m² n² (m²/L² + n²/W²))`, truncated at [harmonics].
     * For a square it is `0.0351443 L²`; for a long strip it tends to `W²/12`.
     */
    val drainageFactor: Double
        get() {
            var sum = 0.0
            var m = 1
            while (m <= harmonics) {
                var n = 1
                while (n <= harmonics) {
                    val eigenvalue = m * m / (length * length) + n * n / (width * width)
                    sum += 64.0 / (PI.pow(6.0) * m * m * n * n * eigenvalue)
                    n += 2
                }
                m += 2
            }
            return sum
        }

    /** `√G` in nm — the lateral drainage length, defined so that `τ = ℓ²/D_p`. */
    val effectiveDrainageLength: Double get() = sqrt(drainageFactor)

    companion object {

        /**
         * The default truncation of the Fourier series.
         *
         * 201 odd harmonics converge a square to 1e-7 relative (see `DrainageGeometryTest`);
         * strongly elongated rectangles need more, so it is a constructor parameter.
         */
        const val DEFAULT_HARMONICS: Int = 201
    }

}

/**
 * Returns `2h/π` in nm — the drainage length of a layer of [thickness] consolidating
 * through **one** open face, defined so that the first-mode relaxation time is `ℓ²/D_p`.
 *
 * This path does not exist in the Gen-1 stack as drawn, because both the electrode and
 * the origami tile are impermeable. It is computed anyway because a DNA-origami sheet is
 * not obviously impermeable — a honeycomb lattice has interhelical solvent channels —
 * and `T-7` needs to know whether making the tile permeable would buy anything.
 *
 * @throws IllegalArgumentException if [thickness] is not positive.
 */
fun verticalDrainageLength(thickness: Double): Double {
    require(thickness > 0.0) { "thickness must be positive, was: $thickness" }
    return 2.0 * thickness / PI
}

/**
 * Returns the depth-integrated transmissivity `T` in nm³ of a Brinkman layer of
 * [thickness] and Darcy [permeability], bounded by two no-slip walls:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`T = k h [ 1 − (2√k/h) tanh(h / 2√k) ]`
 *
 * obtained by integrating the Brinkman velocity profile
 * `u(z) ∝ 1 − cosh((z − h/2)/√k) / cosh(h/2√k)` across the channel, with the effective
 * viscosity taken equal to the solvent viscosity.
 *
 * This form rather than plain Darcy is what makes `T-7` answerable at all. The Gen-1
 * layer's screening length is 0.09 of the thickness on the segment-scale permeability
 * models and 0.56 of it on the blob-scale one — i.e. **Darcy is valid for two of the
 * three and badly invalid for the third**. The Brinkman transmissivity contains both
 * ends: it reduces to [darcyTransmissivity] when `√k ≪ h`, and to
 * [poiseuilleTransmissivity] — a free water film — when `√k ≫ h`, so the answer degrades
 * gracefully instead of diverging where the premise fails.
 *
 * @throws IllegalArgumentException if either argument is not positive.
 */
fun brinkmanTransmissivity(permeability: Double, thickness: Double): Double {
    require(permeability > 0.0) { "permeability must be positive, was: $permeability" }
    require(thickness > 0.0) { "thickness must be positive, was: $thickness" }
    val halfRatio = thickness / (2.0 * sqrt(permeability))
    // 1 - tanh(x)/x loses all its significant digits to cancellation for small x,
    // so the series 1 - tanh(x)/x = x^2/3 - 2x^4/15 + 17x^6/315 is used there instead
    val screeningDeficit = if (halfRatio < SERIES_CROSSOVER) {
        val square = halfRatio * halfRatio
        square / 3.0 - 2.0 * square * square / 15.0 + 17.0 * square * square * square / 315.0
    } else {
        1.0 - tanh(halfRatio) / halfRatio
    }
    return permeability * thickness * screeningDeficit
}

/** Returns the Darcy limit `k h` in nm³ — the `√k ≪ h` end of [brinkmanTransmissivity]. */
fun darcyTransmissivity(permeability: Double, thickness: Double): Double {
    require(permeability > 0.0) { "permeability must be positive, was: $permeability" }
    require(thickness > 0.0) { "thickness must be positive, was: $thickness" }
    return permeability * thickness
}

/**
 * Returns the free-film limit `h³/12` in nm³ — the `√k ≫ h` end of
 * [brinkmanTransmissivity], i.e. plane Poiseuille flow with no polymer in the way.
 */
fun poiseuilleTransmissivity(thickness: Double): Double {
    require(thickness > 0.0) { "thickness must be positive, was: $thickness" }
    return thickness.pow(3.0) / 12.0
}

/** Below this `h/2√k` the series expansion is used instead of the closed form. */
private const val SERIES_CROSSOVER: Double = 1e-2
