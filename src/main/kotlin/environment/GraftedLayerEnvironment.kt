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
import com.xemantic.nano.plentyofroom.brush.GraftedChain
import com.xemantic.nano.plentyofroom.brush.GraftedLayerModel
import com.xemantic.nano.plentyofroom.brush.load
import com.xemantic.nano.plentyofroom.brush.SelfConsistentFieldLayer
import com.xemantic.nano.plentyofroom.quantities.ScreeningLength

/**
 * The grafted layer as an [Environment] — one wrapper over all three of this repository's profile
 * models, the numerical self-consistent field included.
 *
 * [SelfConsistentFieldLayer] implements the same `GraftedLayerModel` contract as the Alexander box
 * and the strong-stretching parabola, so this class needs no branch: the model with no counterpart
 * in the field and the two cheap bounds it is checked against arrive here through one type.
 *
 * ## What the regime says, and why the electrolyte is `null`
 *
 * A neutral grafted layer's regime carries **no buffer**, and that is a derived result rather than
 * an omission. Ideal mobile salt contributes `f = k_BT n_s φ`, strictly linear in `φ`, and
 * `Π = φ f′ − f` annihilates a linear term — so the ions' osmotic contribution is not small, it is
 * exactly zero, cancelling a term 3.5× the layer's own pressure at 10 mM MgCl₂. Everything a buffer
 * does to a neutral layer's mechanics is therefore a `χ`, never an ion count, and `χ` enters
 * through the [GraftedLayerModel]'s own interaction free energy rather than through this class.
 *
 * The **height range** is derived from the model, not asserted: the floor is the layer's own dry
 * thickness `N σ v₀`, below which the volume fraction would exceed one, and the ceiling is the
 * model's resting height. For an SCF layer that ceiling is a *definition* carrying the resting load
 * it was read at, which is why [regime] is lazy — asking for it costs a bracket of solves.
 *
 * @param model the profile model. Anything satisfying `GraftedLayerModel`.
 * @param chain the grafted chain, which is what fixes the height range.
 * @param referenceHeightNm the separation [decayLength] is read at.
 * @param referenceArea the footprint [force] is quoted over; one square nanometre by default,
 *          because a layer has no footprint of its own.
 * @param decayDifferenceStepNm the central-difference step `−P/(dP/dh)` is taken with.
 * @param temperatureKelvin the temperature the regime declares.
 * @param bandwidthHz the band this environment's fluctuations would be quoted in, if any.
 */
class GraftedLayerEnvironment(
    val model: GraftedLayerModel,
    val chain: GraftedChain,
    override val referenceHeightNm: Double,
    override val referenceArea: Double = 1.0,
    val decayDifferenceStepNm: Double = DEFAULT_DECAY_DIFFERENCE_STEP,
    val temperatureKelvin: Double = ROOM_TEMPERATURE,
    val bandwidthHz: Double? = null
) : Environment {

    init {
        require(referenceHeightNm > 0.0) {
            "referenceHeightNm must be positive, was: $referenceHeightNm"
        }
        require(referenceArea > 0.0) { "referenceArea must be positive, was: $referenceArea" }
        require(decayDifferenceStepNm > 0.0) {
            "decayDifferenceStepNm must be positive, was: $decayDifferenceStepNm"
        }
    }

    override val name: String get() = "grafted layer: ${model.name}"

    /** Exactly zero, and that is the derivation in the class KDoc rather than an approximation. */
    override val respondsToBias: Boolean get() = false

    override val regime: Regime by lazy {
        Regime.neutralLayer(
            name = name,
            lowestHeightNm = chain.occupiedThickness,
            highestHeightNm = model.equilibriumHeight(chain),
            temperatureKelvin = temperatureKelvin,
            bandwidthHz = bandwidthHz
        )
    }

    /**
     * `Π(h)` in `pN/nm²` — the model's own disjoining pressure, unchanged.
     *
     * The model's validity range is enforced by the model, which is why this does not consult
     * [regime]: a re-expression that added a second, differently-worded guard would be a change of
     * behaviour, and `T-265` is additive.
     */
    override fun pressure(heightNm: Double): Double = model.disjoiningPressure(chain, heightNm)

    /**
     * `Π(h)·A` in `pN`, with the bias **ignored** — see [respondsToBias].
     *
     * The bias is not clamped to the regime's `[0, 0]` range, because refusing on a coordinate the
     * model does not contain would assert a dependence that is exactly zero.
     */
    override fun force(heightNm: Double, biasVolts: Double): Double =
        model.load(chain, heightNm, referenceArea)

    override val decayLength: ScreeningLength by lazy {
        val step = decayDifferenceStepNm
        val here = pressure(referenceHeightNm)
        val slope =
            (pressure(referenceHeightNm + step) - pressure(referenceHeightNm - step)) /
                (2.0 * step)
        ScreeningLength(
            nanometres = -here / slope,
            where = ScreeningLength.GRAFTED_LAYER,
            readAt = linkedMapOf(
                "heightNm" to referenceHeightNm.toString(),
                "model" to model.name,
                "differenceStepNm" to step.toString()
            )
        )
    }

}

/**
 * The central-difference step the local decay length is taken with, in nm.
 *
 * `T-3a`'s own `STIFFNESS_STEP`, so that this class and `ElectrodeGapEnvironment` differentiate on
 * one convention and a decay length from either can be compared with the other's without a
 * second-order term standing between them.
 */
const val DEFAULT_DECAY_DIFFERENCE_STEP: Double = 0.02
