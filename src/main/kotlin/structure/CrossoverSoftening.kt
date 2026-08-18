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

package com.xemantic.nano.plentyofroom.structure

import kotlinx.serialization.Serializable

/**
 * `T-164` — what one crossover of an [OrigamiGrillage] retains of an interior crossover's two
 * elements.
 *
 * ## Why a crossover needs TWO factors and not one
 *
 * `CLAUDE.md`, from `C-0009`: *"A crossover is TWO elements and only one of them is `D_⊥`. The
 * dihedral spring `k_θ` carries the across-helix rigidity; the vertical link is a **constraint**
 * tying two duplex surfaces together and carries no rigidity at all."*
 *
 * `C-0090`'s two end-of-row readings move both at once — its *refused* reading deletes the
 * dihedral spring, the vertical link **and** the node — so they cannot express the state
 * `CH-0111` actually asks about: a row-end crossover whose torsional register is relaxed (which is
 * what Rothemund's *"one or two scaffold bases could be left unpaired and allowed to form a
 * hairpin"* does) while its backbone stays covalently continuous across the interface. That state
 * is [ofHinge], and it is neither of the two published readings.
 *
 * Both factors are **dimensionless**, and neither is capped at one here: a cap would be a physical
 * assertion inside a numerical type, which is exactly the mistake `CrossoverLayout.EDGE_MARGIN`
 * made at 38.08 nm. The counting ceiling `s ≤ 1` lives in the task that derives it.
 *
 * @param hinge the multiplier on the dihedral spring `k_θ`; `1.0` is an interior crossover.
 * @param link the multiplier on the vertical link's penalty stiffness; `1.0` is an interior
 *          crossover, and the link being a constraint rather than an elasticity is the reason
 *          this is normally left there.
 */
@Serializable
data class CrossoverSoftening(
    val hinge: Double,
    val link: Double
) {

    init {
        require(hinge >= 0.0 && hinge.isFinite()) {
            "hinge must be a non-negative finite multiplier, was: $hinge"
        }
        require(link >= 0.0 && link.isFinite()) {
            "link must be a non-negative finite multiplier, was: $link"
        }
    }

    companion object {

        /** An ordinary interior crossover — the default everywhere. */
        val FULL: CrossoverSoftening = CrossoverSoftening(1.0, 1.0)

        /**
         * No crossover at all: `C-0090`'s *refused* reading, minus the node.
         *
         * Asserted in `T-164`'s gate 2 to be bit-identical to
         * [OrigamiGrillage.consumedCrossovers] at the same sites, which is an independent code
         * path — one deletes the element, the other multiplies it by zero.
         */
        val ABSENT: CrossoverSoftening = CrossoverSoftening(0.0, 0.0)

        /** A crossover whose dihedral spring is scaled by [hinge] and whose link is intact. */
        fun ofHinge(hinge: Double): CrossoverSoftening = CrossoverSoftening(hinge, 1.0)

    }

}
