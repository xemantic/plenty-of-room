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

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/**
 * One anchor link between a substrate-fixed point and a point on the tile.
 *
 * The element mechanics gave two numbers — an [axialStiffness] along the link and a
 * [transverseStiffness] across it. **Which of the two ends up resisting the tile's lateral
 * motion is decided entirely by the orientation**, and that is the whole content of `T-12`.
 *
 * @property polarAngle `θ`, measured **from the surface normal**: `0` is a strut standing on
 *   the substrate under the tile, `π/2` a tether lying in the plane of the surface.
 * @property azimuth `φ`, the in-plane bearing of the link's axis, measured from `+x`.
 * @property attachmentX the tile-side attachment point, nm from the footprint centre.
 * @property attachmentY the same, across the helices.
 */
data class AnchorLink(
    val name: String,
    val axialStiffness: Double,
    val transverseStiffness: Double,
    val polarAngle: Double,
    val azimuth: Double,
    val attachmentX: Double,
    val attachmentY: Double
) {

    init {
        require(axialStiffness >= 0.0) { "axialStiffness must not be negative, was: $axialStiffness" }
        require(transverseStiffness >= 0.0) {
            "transverseStiffness must not be negative, was: $transverseStiffness"
        }
    }

    /** The link axis as a unit vector, `(sinθ cosφ, sinθ sinφ, cosθ)`. */
    val axis: Triple<Double, Double, Double>
        get() = Triple(
            sin(polarAngle) * cos(azimuth),
            sin(polarAngle) * sin(azimuth),
            cos(polarAngle)
        )

    /**
     * The link's contribution to the tile's translational stiffness along a direction whose
     * cosine with the link axis is [cosine]: `k_t + (k_a − k_t) cos²`.
     *
     * The projector `K = k_a n̂n̂ᵀ + k_t(I − n̂n̂ᵀ)` written one component at a time. Its trace
     * is `k_a + 2k_t` whatever the orientation, which is the orientation-free invariant the
     * gate-3 test uses.
     */
    fun stiffnessAlong(cosine: Double): Double =
        transverseStiffness + (axialStiffness - transverseStiffness) * cosine * cosine

    /** `k_xx` of this link. */
    val stiffnessXX: Double get() = stiffnessAlong(axis.first)

    /** `k_yy` of this link. */
    val stiffnessYY: Double get() = stiffnessAlong(axis.second)

    /** `k_zz` of this link — the part the actuator has to fight. */
    val stiffnessZZ: Double get() = stiffnessAlong(axis.third)

    /** The off-diagonal in-plane term `k_xy = (k_a − k_t) n_x n_y`. */
    val stiffnessXY: Double
        get() = (axialStiffness - transverseStiffness) * axis.first * axis.second

    /**
     * The link's contribution to the tile's **yaw** stiffness in `pN·nm/rad`.
     *
     * A yaw of `ψ` about the footprint centre moves this link's attachment point by
     * `u = ψ(−y, x)`, so the energy is `½ψ²[y²k_xx − 2xy k_xy + x²k_yy]`.
     * A link at the centre contributes **nothing**, whatever its stiffness — which is why a
     * single central anchor pins translation and leaves the tile free to rotate.
     */
    val yawStiffness: Double
        get() = attachmentY * attachmentY * stiffnessXX -
                2.0 * attachmentX * attachmentY * stiffnessXY +
                attachmentX * attachmentX * stiffnessYY
}

/**
 * A set of anchor links, reduced to the four rigid-body coordinates that matter here.
 *
 * The tile's shape modes are `C-0006`/`C-0009`'s, not this task's; what `T-12` owns is the
 * two in-plane translations and the yaw, with the normal direction carried alongside because
 * it is the **cost**.
 */
data class AnchorAssembly(val links: List<AnchorLink>) {

    /** `k_xx` summed over the links, in `pN/nm`. */
    val lateralStiffnessX: Double get() = links.sumOf { it.stiffnessXX }

    /** `k_yy` summed over the links, in `pN/nm`. */
    val lateralStiffnessY: Double get() = links.sumOf { it.stiffnessYY }

    /** `k_zz` summed over the links, in `pN/nm` — subtracted from the actuator, never added. */
    val normalStiffness: Double get() = links.sumOf { it.stiffnessZZ }

    /** `k_xy` summed over the links; zero for any arrangement with four-fold symmetry. */
    val lateralCoupling: Double get() = links.sumOf { it.stiffnessXY }

    /** The yaw stiffness in `pN·nm/rad`. */
    val yawStiffness: Double get() = links.sumOf { it.yawStiffness }

    /** The weaker of the two in-plane axes, which is the one the predicate is read against. */
    val weakestLateralStiffness: Double get() = minOf(lateralStiffnessX, lateralStiffnessY)
}

/**
 * `count` links lying **in the surface plane**, arranged radially at [radius] nm from the
 * footprint centre and evenly spaced in bearing, starting at [firstAzimuth].
 *
 * The default of four links starting at 45° puts them on the corners of a square footprint,
 * which is the arrangement the exact yaw/translation equivalence is stated for.
 */
fun radialInPlaneLinks(
    axialStiffness: Double,
    transverseStiffness: Double,
    radius: Double,
    count: Int = 4,
    firstAzimuth: Double = PI / 4.0,
    name: String = "in-plane radial tether"
): List<AnchorLink> {
    require(count > 0) { "count must be positive, was: $count" }
    require(radius >= 0.0) { "radius must not be negative, was: $radius" }
    return (0 until count).map { index ->
        val azimuth = firstAzimuth + 2.0 * PI * index / count
        AnchorLink(
            name = name,
            axialStiffness = axialStiffness,
            transverseStiffness = transverseStiffness,
            polarAngle = PI / 2.0,
            azimuth = azimuth,
            attachmentX = radius * cos(azimuth),
            attachmentY = radius * sin(azimuth)
        )
    }
}

/**
 * `count` links standing **along the surface normal**, at [radius] nm from the footprint
 * centre — a strut or a tether that crosses the polymer layer.
 *
 * Their azimuth is immaterial to the stiffness (the projection is on the normal) but not to
 * the yaw, which is why the attachment points are still spread.
 */
fun verticalLinks(
    axialStiffness: Double,
    transverseStiffness: Double,
    radius: Double,
    count: Int = 4,
    firstAzimuth: Double = PI / 4.0,
    name: String = "through-layer strut"
): List<AnchorLink> {
    require(count > 0) { "count must be positive, was: $count" }
    require(radius >= 0.0) { "radius must not be negative, was: $radius" }
    return (0 until count).map { index ->
        val azimuth = firstAzimuth + 2.0 * PI * index / count
        AnchorLink(
            name = name,
            axialStiffness = axialStiffness,
            transverseStiffness = transverseStiffness,
            polarAngle = 0.0,
            azimuth = azimuth,
            attachmentX = radius * cos(azimuth),
            attachmentY = radius * sin(azimuth)
        )
    }
}

/**
 * Two stiffnesses in series, `1/(1/a + 1/b)` — the compliance budget leaf `A8.2` asks for.
 *
 * Returns zero if either is zero: a single compliant element in the load path sets the whole
 * stiffness, and that is the point of computing it.
 */
fun seriesStiffness(vararg stiffnesses: Double): Double {
    require(stiffnesses.isNotEmpty()) { "at least one stiffness is required" }
    require(stiffnesses.all { it >= 0.0 }) { "stiffnesses must not be negative" }
    if (stiffnesses.any { it == 0.0 }) return 0.0
    return 1.0 / stiffnesses.sumOf { 1.0 / it }
}
