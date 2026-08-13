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

package com.xemantic.nano.plentyofroom.anchoring

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.equipartitionStiffness
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.sqrt

/**
 * The requirement `T-12` has to meet, in its three inequivalent readings.
 *
 * §6 task 8 says "σ_RMS ≤ 3.0 nm" without saying of what, and `C-0010` had to declare a
 * reading for the *normal* coordinate. The lateral coordinate has exactly the same ambiguity
 * and it is declared here rather than chosen silently — which, per `CH-0009`, is the failure
 * mode this programme has already made once.
 *
 * The **declared acceptance quantity is the per-coordinate reading**, because that is the one
 * leaf `A1.1` tabulates and the one `C-0010` handed down. The other two are reported with
 * their margins.
 */
object LateralRequirement {

    /** §6 task 8's bound in nm. */
    const val POSITIONAL_BOUND: Double = 3.0

    /**
     * `k ≥ k_BT/σ²` for **one Cartesian coordinate** — leaf `A1.1`'s own bound table, and the
     * declared acceptance quantity. `0.460216 pN/nm` at `σ = 3.0 nm`, 300 K.
     */
    fun translationalStiffness(
        bound: Double = POSITIONAL_BOUND,
        temperature: Double = ROOM_TEMPERATURE
    ): Double = equipartitionStiffness(bound, temperature)

    /**
     * The stiffness that holds the **radial** in-plane excursion `√(σ_x² + σ_y²)` to [bound] —
     * exactly twice [translationalStiffness], because there are two equal coordinates.
     */
    fun radialStiffness(
        bound: Double = POSITIONAL_BOUND,
        temperature: Double = ROOM_TEMPERATURE
    ): Double = 2.0 * translationalStiffness(bound, temperature)

    /**
     * The stiffness that holds the in-plane excursion of the tile's **corner** to [bound] when
     * the variance is split equally between the two translations and the yaw — exactly three
     * times [translationalStiffness].
     *
     * A design sitting exactly on leaf `A1.1`'s bound in all three coordinates puts
     * `√3 × 3.0 = 5.196 nm` on the corner, which is `CH-0009`'s point restated in the plane.
     */
    fun worstPointTranslationalStiffness(
        bound: Double = POSITIONAL_BOUND,
        temperature: Double = ROOM_TEMPERATURE
    ): Double = 3.0 * translationalStiffness(bound, temperature)

    /**
     * The yaw stiffness in `pN·nm/rad` that holds the in-plane displacement of a point at
     * [radius] nm to [bound] nm: `k_ψ = k_BT r²/σ²`.
     *
     * The currency is declared in `T-12`'s Formulate section: **the worst material point, the
     * corner**, so that yaw and translation are commensurable and can be added.
     */
    fun yawStiffness(
        bound: Double = POSITIONAL_BOUND,
        radius: Double,
        temperature: Double = ROOM_TEMPERATURE
    ): Double {
        require(radius > 0.0) { "radius must be positive, was: $radius" }
        return equipartitionStiffness(bound, temperature) * radius * radius
    }

    /** The half-diagonal of the footprint — the tile's worst material point. */
    fun cornerRadius(edgeX: Double, edgeY: Double): Double {
        require(edgeX > 0.0 && edgeY > 0.0) { "the footprint must be positive" }
        return 0.5 * sqrt(edgeX * edgeX + edgeY * edgeY)
    }

    /** The footprint-RMS radius `√((L_x² + L_y²)/12)` — the weaker reading, reported not used. */
    fun footprintRadius(edgeX: Double, edgeY: Double): Double {
        require(edgeX > 0.0 && edgeY > 0.0) { "the footprint must be positive" }
        return sqrt((edgeX * edgeX + edgeY * edgeY) / 12.0)
    }

    /**
     * The in-plane RMS displacement in nm of a point at [radius] under a scheme of
     * translational stiffness [lateral] and yaw stiffness [yaw]:
     * `√(2k_BT/k_lat + k_BT r²/k_ψ)`.
     *
     * Both translations contribute, so this is a **radial** amplitude and it is `√2` above the
     * per-coordinate reading before yaw is added at all.
     */
    fun pointRms(
        lateral: Double,
        yaw: Double,
        radius: Double,
        temperature: Double = ROOM_TEMPERATURE
    ): Double {
        require(lateral > 0.0) { "lateral must be positive, was: $lateral" }
        require(yaw > 0.0) { "yaw must be positive, was: $yaw" }
        val energy = thermalEnergy(temperature)
        return sqrt(2.0 * energy / lateral + energy * radius * radius / yaw)
    }
}

/**
 * The per-load-path allowables, **cited** from `C-0006`'s literature trace, with `C-0009`'s
 * concentration factor.
 *
 * §4(f)'s 35–60 pN band is deliberately **absent**: `C-0006` traced it to Shrestha et al.
 * (2016) and showed it is the failure force of an entire 6–8 helix cross-section, not the
 * capacity of one load path.
 */
object PerPathAllowables {

    /** Single hybridised domain in **shear**, quasi-static — Strunz et al. (1999), 48 ± 2 pN. */
    const val SHEAR: Double = 48.0

    /** The same domain in **unzip** geometry — Essevaz-Roulet et al. (1997), 10–15 pN. */
    const val UNZIP: Double = 10.0

    /** Hard ceiling on any nicked duplex — van Mameren et al. (2009), rate-independent. */
    const val OVERSTRETCHING_CEILING: Double = 65.0

    /**
     * The factor by which a rigid anchor's load concentrates onto its **two nearest
     * crossovers** rather than spreading over the `ℓ`-contour — **CITED**, `C-0009`, which
     * measures it at 2.3–7.6× across every anchored case, anchor count and foundation
     * stiffness in its sweep. The worst value is used here.
     */
    const val CONCENTRATION_FACTOR_MAX: Double = 7.6

    /** The mild end of the same range, reported alongside. */
    const val CONCENTRATION_FACTOR_MIN: Double = 2.3
}

/**
 * The peak force in pN on a single crossover under an anchor carrying [anchorForce], with
 * `C-0009`'s [concentrationFactor] applied to the equal-sharing figure.
 *
 * `C-0006`'s equal-sharing number understates this by the factor, and `C-0009` shows the
 * understatement reaches the unzip allowable in the normal direction. Applied here to the
 * lateral load as well, which is **conservative**: an in-plane load on the tile is a membrane
 * load carried by the duplexes in tension (`S = 1100 pN` each, 14.9 of them across the tile),
 * not a flexural one confined to an `ℓ`-sized patch, so it spreads further than `C-0009`'s
 * out-of-plane case. Using the out-of-plane factor is the safe direction and it is stated.
 */
fun peakPathForce(anchorForce: Double, concentrationFactor: Double): Double {
    require(anchorForce >= 0.0) { "anchorForce must not be negative, was: $anchorForce" }
    require(concentrationFactor >= 1.0) {
        "concentrationFactor must be at least one, was: $concentrationFactor"
    }
    return anchorForce * concentrationFactor
}

/**
 * The fraction of the actuator's stroke that survives once anchors of total normal stiffness
 * [anchorNormalStiffness] are added in parallel with a layer of [layerStiffness]:
 * `k_layer/(k_layer + k_anchors)`, at fixed applied force.
 */
fun strokeRetainedFraction(anchorNormalStiffness: Double, layerStiffness: Double): Double {
    require(anchorNormalStiffness >= 0.0) {
        "anchorNormalStiffness must not be negative, was: $anchorNormalStiffness"
    }
    require(layerStiffness > 0.0) { "layerStiffness must be positive, was: $layerStiffness" }
    return layerStiffness / (layerStiffness + anchorNormalStiffness)
}

/**
 * The **anisotropy ratio** `k_lat/k_norm` of an anchor — the figure of merit of the whole task.
 *
 * A scheme wants this **large**: lateral stiffness is the requirement and normal stiffness is
 * the cost. A strut standing under the tile has it of order `3EI/(S L²) ≈ 0.006`; a tether
 * lying in the plane has it of order `S L²/(12 EI) ≈ 160`. Four orders of magnitude separate
 * two arrangements of the same duplex.
 */
fun anisotropyRatio(lateralStiffness: Double, normalStiffness: Double): Double {
    require(normalStiffness > 0.0) { "normalStiffness must be positive, was: $normalStiffness" }
    return lateralStiffness / normalStiffness
}

/**
 * The secant-to-tangent ratio of a chain at tension [force] — the **anisotropy theorem**,
 * executable.
 *
 * For any link crossing the layer, the lateral stiffness is the tension over the span (`f/x`)
 * and the normal stiffness is the tangent (`df/dx`). With `f(0) = 0` and `f` convex — which
 * every polymer and every duplex is — `f(x) = ∫₀ˣ f′ ≤ x f′(x)`, so this ratio never exceeds
 * one and equals one only for a linear spring.
 *
 * **No through-layer load path can buy lateral stiffness more cheaply than one-for-one in
 * normal stiffness.** That is the cheap bound `T-12` runs before any number, and it is what
 * sends the answer into the plane of the surface.
 */
fun secantToTangentRatio(chain: FreelyJointedChain, force: Double): Double =
    chain.transverseStiffness(force) / chain.tangentStiffness(force)

/**
 * The contour length in nm at which [count] entropic tethers, each spanning [span] nm across
 * the layer with Kuhn length [kuhnLength], together deliver exactly [requiredStiffness] of
 * lateral stiffness.
 *
 * Solved by bisection on the contour length, which the lateral stiffness is monotone
 * decreasing in: a longer chain is a softer spring. Exits on the **bracket width**.
 *
 * The Gaussian closed form `L_c = 3 N k_BT/(k b)` is the `span → 0` limit of this and is
 * asserted against it as a gate-4 test.
 */
fun entropicTetherContourLength(
    count: Int,
    span: Double,
    kuhnLength: Double,
    requiredStiffness: Double,
    temperature: Double = ROOM_TEMPERATURE
): Double {
    require(count > 0) { "count must be positive, was: $count" }
    require(span > 0.0) { "span must be positive, was: $span" }
    require(requiredStiffness > 0.0) {
        "requiredStiffness must be positive, was: $requiredStiffness"
    }
    fun stiffness(contour: Double): Double {
        val chain = FreelyJointedChain(contour, kuhnLength, temperature)
        return count * chain.transverseStiffness(chain.tension(span))
    }
    var low = span * 1.000001
    var high = low
    while (stiffness(high) > requiredStiffness) {
        high *= 2.0
        require(high < 1e9) { "no contour length delivers $requiredStiffness pN/nm" }
    }
    repeat(300) {
        val middle = 0.5 * (low + high)
        if (stiffness(middle) > requiredStiffness) low = middle else high = middle
        if (high - low <= 1e-13 * high) return 0.5 * (low + high)
    }
    return 0.5 * (low + high)
}
