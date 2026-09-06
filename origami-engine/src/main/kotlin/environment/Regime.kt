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

package com.xemantic.nano.plentyofroom.environment

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import kotlinx.serialization.Serializable

/**
 * The tuple a downstream consumer has to be **refused** on.
 *
 * A validity range written in prose is respected by whoever reads it, and this repository has paid
 * for the difference repeatedly: `CH-0004` substituted one of three correct Debye lengths for
 * another, `CH-0007` compared a diffuse-layer drop against an applied bias, `CH-0080` carried a
 * ceiling quoted at a state the device does not occupy. Every one of those is a *reading* where a
 * *gate* was wanted.
 *
 * So a [Regime] is data. It answers two questions mechanically:
 *
 *  * **may this environment be asked about this state** — [admitsHeight], [admitsBias],
 *    [requireAdmits];
 *  * **may a number solved over there be consumed here** — [reasonToRefuse], which returns a
 *    *reason* rather than a boolean, because a bare boolean is what a reader ignores.
 *
 * ## Why this repository's moat is a regime rather than an ingredient
 *
 * oxDNA2 carries salt-dependent electrostatics and says its parameterisation is *"restricted to
 * salt concentrations of 0.1 M of monovalent salt or greater"*, with magnesium *"not included in
 * the oxDNA model"*. This device runs at **0.5–10 mM MgCl₂** — below the floor, in the ion that is
 * excluded. That is not a detail of provenance, it is the whole reason `brush/` and
 * `electrostatics/` are worth handing to anybody, and it is exactly what [bufferMillimolar] and
 * [counterionValency] say.
 *
 * @param name how this regime is named in a message and in a result file.
 * @param bufferMillimolar the salt molarity, or `null` where no electrolyte enters the model at
 *          all. `null` is a **claim**, not an omission: `CLAUDE.md` establishes that ideal mobile
 *          salt exerts exactly no osmotic pressure on a neutral grafted layer, because
 *          `Π = φf′ − f` annihilates a term linear in `φ`.
 * @param electrolyte the salt and its stoichiometry, in words — `"MgCl₂ (2:1)"`.
 * @param counterionValency the valency of the ion that screens the wall in question. `Ξ ∝ q³` and
 *          `μ_GC ∝ 1/q`, so divalent at a surface is a **different problem** from monovalent at
 *          the same surface, never a rescaling of it.
 * @param temperatureKelvin the temperature every constant here was evaluated at.
 * @param lowestHeightNm the smallest separation the model is solved for, inclusive.
 * @param highestHeightNm the largest, inclusive.
 * @param lowestBiasVolts the smallest applied bias, inclusive; `0.0` where bias does not enter.
 * @param highestBiasVolts the largest, inclusive.
 * @param bandwidthHz the band a fluctuation read from this environment would be quoted in, or
 *          `null` for a quasi-static response that states no band. `CLAUDE.md`: a broadband RMS is
 *          the `f → ∞` limit and is not the measured quantity; the Gen-1 tile puts 0.55–3.1 % of
 *          its variance below 1 kHz.
 */
@Serializable
data class Regime(
    val name: String,
    val bufferMillimolar: Double?,
    val electrolyte: String,
    val counterionValency: Int,
    val temperatureKelvin: Double,
    val lowestHeightNm: Double,
    val highestHeightNm: Double,
    val lowestBiasVolts: Double,
    val highestBiasVolts: Double,
    val bandwidthHz: Double?
) {

    init {
        require(name.isNotBlank()) { "a regime must be named" }
        require(bufferMillimolar == null || bufferMillimolar > 0.0) {
            "bufferMillimolar must be positive where it is stated, was: $bufferMillimolar"
        }
        require(counterionValency > 0) {
            "counterionValency must be positive, was: $counterionValency"
        }
        require(temperatureKelvin > 0.0) {
            "temperatureKelvin must be positive, was: $temperatureKelvin"
        }
        require(lowestHeightNm > 0.0) { "lowestHeightNm must be positive, was: $lowestHeightNm" }
        require(highestHeightNm > lowestHeightNm) {
            "highestHeightNm must exceed lowestHeightNm, were: " +
                "$highestHeightNm and $lowestHeightNm"
        }
        require(highestBiasVolts >= lowestBiasVolts) {
            "highestBiasVolts must not be below lowestBiasVolts, were: " +
                "$highestBiasVolts and $lowestBiasVolts"
        }
        require(bandwidthHz == null || bandwidthHz > 0.0) {
            "bandwidthHz must be positive where it is stated, was: $bandwidthHz"
        }
    }

    /** Whether [heightNm] lies inside the declared, closed separation range. */
    fun admitsHeight(heightNm: Double): Boolean =
        heightNm >= lowestHeightNm && heightNm <= highestHeightNm

    /** Whether [biasVolts] lies inside the declared, closed bias range. */
    fun admitsBias(biasVolts: Double): Boolean =
        biasVolts >= lowestBiasVolts && biasVolts <= highestBiasVolts

    /** Throws naming the height bound, which is the half of the message worth having. */
    fun requireAdmitsHeight(heightNm: Double) {
        require(admitsHeight(heightNm)) {
            "$name: a height of $heightNm nm is outside the regime this environment is solved " +
                "in, [$lowestHeightNm, $highestHeightNm] nm"
        }
    }

    /** Throws naming the bias bound. */
    fun requireAdmitsBias(biasVolts: Double) {
        require(admitsBias(biasVolts)) {
            "$name: a bias of $biasVolts V is outside the regime this environment is solved in, " +
                "[$lowestBiasVolts, $highestBiasVolts] V"
        }
    }

    /** Both bounds at once. */
    fun requireAdmits(heightNm: Double, biasVolts: Double) {
        requireAdmitsHeight(heightNm)
        requireAdmitsBias(biasVolts)
    }

    /**
     * Why a number solved in [source] may not be consumed here, or `null` if it may.
     *
     * The order of the checks is the order the corpus has been bitten in: the electrolyte first,
     * because that is `CH-0004`; then the valency, because `Ξ ∝ q³`; then the temperature, the
     * ranges, and the band.
     */
    fun reasonToRefuse(source: Regime): String? {
        if (bufferMillimolar != source.bufferMillimolar) {
            return "$name is solved at ${describeBuffer()} and ${source.name} at " +
                "${source.describeBuffer()}; a screening length, an effective charge and a " +
                "force level are all functions of the salt, and substituting one buffer's for " +
                "another's is CH-0004"
        }
        if (electrolyte != source.electrolyte || counterionValency != source.counterionValency) {
            return "$name screens with a valency-$counterionValency counterion ($electrolyte) " +
                "and ${source.name} with a valency-${source.counterionValency} one " +
                "(${source.electrolyte}); the coupling parameter goes as q^3, so these are " +
                "different problems and not a rescaling"
        }
        if (temperatureKelvin != source.temperatureKelvin) {
            return "$name is at $temperatureKelvin K and ${source.name} at " +
                "${source.temperatureKelvin} K"
        }
        if (source.lowestHeightNm < lowestHeightNm || source.highestHeightNm > highestHeightNm) {
            return "${source.name} is solved over [${source.lowestHeightNm}, " +
                "${source.highestHeightNm}] nm, which leaves [$lowestHeightNm, " +
                "$highestHeightNm] nm"
        }
        if (source.lowestBiasVolts < lowestBiasVolts || source.highestBiasVolts > highestBiasVolts) {
            return "${source.name} is solved over [${source.lowestBiasVolts}, " +
                "${source.highestBiasVolts}] V, which leaves [$lowestBiasVolts, " +
                "$highestBiasVolts] V"
        }
        if (bandwidthHz != source.bandwidthHz) {
            return "$name declares a band of ${describeBand()} and ${source.name} " +
                "${source.describeBand()}; a variance quoted in one band is not a variance in " +
                "the other, and the two differ here by 13x in amplitude"
        }
        return null
    }

    private fun describeBuffer(): String =
        if (bufferMillimolar == null) "no electrolyte at all" else "$bufferMillimolar mM"

    private fun describeBand(): String =
        if (bandwidthHz == null) "no band (quasi-static)" else "$bandwidthHz Hz"

    companion object {

        /**
         * A regime in the Gen-1 buffer: `MgCl₂`, 2:1, `Mg²⁺` the screening counterion.
         *
         * The concentration is not defaulted. §3 names 2, 5 and 10 mM and `C-0012` recommends
         * 0.5 mM; there is no *the* buffer here, and a default would be one.
         */
        fun magnesiumChloride(
            name: String,
            concentrationMillimolar: Double,
            lowestHeightNm: Double,
            highestHeightNm: Double,
            lowestBiasVolts: Double,
            highestBiasVolts: Double,
            temperatureKelvin: Double = ROOM_TEMPERATURE,
            bandwidthHz: Double? = null
        ): Regime = Regime(
            name = name,
            bufferMillimolar = concentrationMillimolar,
            electrolyte = "MgCl2 (2:1)",
            counterionValency = 2,
            temperatureKelvin = temperatureKelvin,
            lowestHeightNm = lowestHeightNm,
            highestHeightNm = highestHeightNm,
            lowestBiasVolts = lowestBiasVolts,
            highestBiasVolts = highestBiasVolts,
            bandwidthHz = bandwidthHz
        )

        /**
         * A regime in which no electrolyte enters the model, and that is a **result**.
         *
         * Ideal mobile salt contributes `f = k_BT n_s φ`, strictly linear in `φ`, and
         * `Π = φf′ − f` annihilates a linear term — so the salt's osmotic contribution to a
         * neutral grafted layer is not small, it is exactly zero. At 10 mM MgCl₂ that cancels a
         * term 3.5× the layer's own osmotic pressure. Everything a buffer does to a neutral layer's
         * mechanics is therefore a `χ`, never an ion count.
         *
         * The valency is still stated, because it is the valency the *cancellation* was taken at,
         * and the bias range is `[0, 0]` because the model **contains no bias**. That is not a
         * refusal: an [Environment] whose `respondsToBias` is `false` never consults the bias
         * range, because the answer does not depend on the coordinate the range would bound.
         */
        fun neutralLayer(
            name: String,
            lowestHeightNm: Double,
            highestHeightNm: Double,
            temperatureKelvin: Double = ROOM_TEMPERATURE,
            bandwidthHz: Double? = null
        ): Regime = Regime(
            name = name,
            bufferMillimolar = null,
            electrolyte = "none — an ideal mobile salt cancels out of a neutral layer exactly",
            counterionValency = 2,
            temperatureKelvin = temperatureKelvin,
            lowestHeightNm = lowestHeightNm,
            highestHeightNm = highestHeightNm,
            lowestBiasVolts = 0.0,
            highestBiasVolts = 0.0,
            bandwidthHz = bandwidthHz
        )
    }

}
