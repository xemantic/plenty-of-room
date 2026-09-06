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

package com.xemantic.nano.plentyofroom.actuator

import kotlinx.serialization.Serializable

/**
 * The Gen-1 stack's geometry, fixed before anything is derived — task `T-3`, leaf `A2.2`.
 *
 * ## The one relation that has to be stated rather than assumed
 *
 * §3 gives three lengths that are easy to confuse: a polymer layer of 5 / 7 / 10 nm, a tile
 * ~10 nm thick, and an effort point ~20–25 nm above the electrode. The electrostatic problem
 * `C-0008` solves runs between the electrode at `z = 0` and the tile's charged plane; the
 * mechanical problem `C-0003` solves runs between the grafting plane at `z = 0` and the rigid
 * non-adsorbing wall the tile presents. **Those two lengths are the same length**, because the
 * tile's bottom face rests on the layer's outer surface: the layer is grafted on the electrode
 * and the tile is what compresses it. There is no free buffer sliver between them — a tile
 * floating above an uncompressed layer feels no restoring force at all and simply falls until
 * it touches.
 *
 * So [electrostaticGap] is the identity on the layer height, and this class exists to say so
 * once, in code, rather than to have every downstream expression re-assume it.
 *
 * ## Sign conventions, restated from `C-0008` and `C-0003` because they must agree
 *
 * - `z` is normal to the electrode, **positive away from it**, origin at the electrode surface.
 * - The layer is grafted at `z = 0`; the tile's bottom face sits at `z = h`, its top face at
 *   `z = h + t`.
 * - The layer's disjoining pressure is **positive** when it pushes the tile along `+z`.
 * - The electrostatic force `F_es,z` is **negative** when it pulls the tile toward the electrode,
 *   which is what a positive electrode bias does to a net-negative tile (§1).
 * - Compression means `h < L₀`, and the **stroke** is `L₀ − h`, positive downward.
 *
 * ## Where the charge sits
 *
 * `C-0008` models the tile as a uniformly charged plane at `z = h` carrying the
 * Manning-renormalised charge of half the tile. That convention is inherited unchanged, so the
 * charged plane and the bottom face coincide. The tile's own 10 nm of thickness therefore
 * enters this task only through the effort point, never through the gap.
 *
 * @param tileEdge the square tile's edge in nm — 40 nm per §3, up to 70 × 100 nm for test tiles.
 * @param tileThickness the tile's thickness in nm; §3 says ~10 nm for a single-layer honeycomb.
 * @param leverAttachmentHeight how far above the tile's **top** face the output coupling takes
 *          its purchase, in nm. §3 says the effort point "may sit ~20–25 nm above the electrode";
 *          at 5 nm this places the three §3 layer heights at exactly 20 / 22 / 25 nm, i.e. it
 *          reproduces the §3 band at both ends. A lever bonded straight onto the tile gives 0.
 */
@Serializable
data class ActuatorGeometry(
    val tileEdge: Double = 40.0,
    val tileThickness: Double = 10.0,
    val leverAttachmentHeight: Double = 5.0
) {

    init {
        require(tileEdge > 0.0) { "tileEdge must be positive, was: $tileEdge" }
        require(tileThickness > 0.0) { "tileThickness must be positive, was: $tileThickness" }
        require(leverAttachmentHeight >= 0.0) {
            "leverAttachmentHeight must not be negative, was: $leverAttachmentHeight"
        }
    }

    /** The footprint `A` in nm² over which both the disjoining pressure and `F_es` are integrated. */
    val footprintArea: Double get() = tileEdge * tileEdge

    /**
     * The tile-electrode separation in nm that the electrostatics sees, at layer height [layerHeight].
     *
     * The identity, stated as a function so that it is auditable and so that any future
     * geometry in which it is *not* the identity — a dielectric spacer of §1, an adsorbed
     * tile, a tethered stand-off — has one place to change.
     */
    fun electrostaticGap(layerHeight: Double): Double {
        requirePositive(layerHeight)
        return layerHeight
    }

    /** Where the tile's bottom face sits, in nm above the electrode. */
    fun tileBottomFace(layerHeight: Double): Double {
        requirePositive(layerHeight)
        return layerHeight
    }

    /** Where the tile's top face sits, in nm above the electrode. */
    fun tileTopFace(layerHeight: Double): Double {
        requirePositive(layerHeight)
        return layerHeight + tileThickness
    }

    /** Where the output coupling takes its purchase, in nm above the electrode — §3's effort point. */
    fun effortPointHeight(layerHeight: Double): Double {
        requirePositive(layerHeight)
        return layerHeight + tileThickness + leverAttachmentHeight
    }

    private fun requirePositive(layerHeight: Double) {
        require(layerHeight > 0.0) { "layerHeight must be positive, was: $layerHeight" }
    }

}
