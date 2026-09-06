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

package com.xemantic.nano.plentyofroom.quantities

/**
 * A screening or decay length — **the** quantity this project has three correct values for.
 *
 * `CLAUDE.md`, verbatim: *"'The Debye length' is three different numbers in this project, and all
 * three are correct in their own place — 3.93 nm in the bulk buffer at 2 mM MgCl₂; 0.84–1.18 nm in
 * the tile-electrode gap, which is counterion-dominated 3–33× so its ion content is set by the
 * tile's charge and not by the buffer; and 4.5–5.5 nm inside the PEG layer, which excludes 23–48 %
 * of the salt. Substituting one for another is `CH-0004`."*
 *
 * There is a fourth: the **lateral** decay of a load perturbation in a slit, which is the transverse
 * eigenvalue `q₀² ≥ κ² + (π/2h)²` and not `λ_D` at all — 0.62–0.84 `λ_D` at 2 mM, and it *narrows*
 * as the gap closes. That one is on a different [axis], which is why [axis] is part of the state
 * rather than a comment.
 *
 * So a screening length carries [where] it was read and on which [axis], and [ratioOf] then refuses
 * the substitution outright. The legitimate comparison — `T-3a`'s own `decayOverBulkDebye` is one —
 * is [statedRatio], which renders both states beside the number.
 *
 * @param nanometres the length itself.
 * @param where which of the three (or four) this is; see the constants on the companion.
 * @param axis which direction it decays in. Defaults to [NORMAL], the walls' own axis.
 * @param readAt whatever else fixes the reading: concentration, gap, bias, layer height.
 */
data class ScreeningLength(
    val nanometres: Double,
    val where: String,
    val axis: String = NORMAL,
    val readAt: Map<String, String> = emptyMap()
) : StatedQuantity {

    init {
        require(nanometres > 0.0) {
            "a screening length must be positive, was: $nanometres. A non-positive local " +
                "logarithmic derivative means the response GROWS with separation, which is the " +
                "signature of a state near a sign change of the force rather than in its far " +
                "field; report that state, do not call it a length."
        }
        require(where.isNotBlank()) {
            "a screening length must say WHERE it was read; this project has three correct " +
                "values for it and substituting one for another is CH-0004"
        }
        require(axis.isNotBlank()) { "a screening length must say which axis it decays on" }
    }

    override val value: Double get() = nanometres

    override val unit: String get() = "nm"

    override val kind: String get() = "screening length"

    override val state: Map<String, String> = LinkedHashMap<String, String>().apply {
        put("where", where)
        put("axis", axis)
        putAll(readAt)
    }

    override fun scaledBy(factor: Double): ScreeningLength =
        copy(nanometres = nanometres * factor)

    companion object {

        /** The bulk reservoir the surfaces are in equilibrium with — Kjellander's decay length. */
        const val BULK_RESERVOIR: String = "the bulk reservoir"

        /**
         * Between two walls, where the ion content is set by the walls' counterions.
         *
         * `C-0110`: counterion dominance is a statement about ION CONTENT and never about a decay
         * length, and the standing shorthand *"the gap's Debye length is counterion-set"* is true
         * of the content and false of the decay. Reading this one where [BULK_RESERVOIR] belongs
         * is `CH-0004`.
         */
        const val CONFINED_GAP: String = "the confined gap"

        /** Inside the grafted layer, which excludes salt and therefore screens from further out. */
        const val GRAFTED_LAYER: String = "inside the grafted layer"

        /** Normal to the walls — the axis a disjoining pressure decays on. */
        const val NORMAL: String = "normal to the walls"

        /** Lateral, inward from a rim — a different eigenvalue, not `λ_D`. */
        const val LATERAL: String = "lateral, inward from the rim"
    }

}
