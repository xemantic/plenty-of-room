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

import com.xemantic.nano.plentyofroom.equipartitionRms
import kotlinx.serialization.Serializable
import kotlin.math.ln

/**
 * The response of one compression model at one point of the design space,
 * all quantities in the locked units: nm, pN, `pN/nm`.
 */
@Serializable
data class LayerResponse(

    /** The model, and the premise that distinguishes it from the others. */
    val model: String,

    /** `L₀`, the unperturbed layer height in nm. Equal across models by construction. */
    val equilibriumHeight: Double,

    /**
     * `k(L₀)` in `pN/nm`, the stiffness the tile sees at first contact.
     *
     * Zero for the SCF form, whose outer edge is diffuse — which is a result, not a defect.
     */
    val equilibriumStiffness: Double,

    /** The height in nm at which the layer carries the target force. */
    val heightUnderTargetForce: Double,

    /** `L₀ − h`, the stroke in nm delivered by the target force. */
    val strokeUnderTargetForce: Double,

    /** `F/(L₀ − h)` in `pN/nm` — the stiffness that actually governs the stroke. */
    val secantStiffness: Double,

    /** `k(h)` in `pN/nm` at the working height — the stiffness that governs the noise. */
    val tangentStiffness: Double,

    /** `sqrt(k_BT/k)` in nm at the working point, from the tangent stiffness. */
    val positionalRms: Double,

    /**
     * `d ln k_secant / d ln σ` at fixed layer height, the sensitivity to grafting density
     * that the task 1 acceptance predicate requires.
     *
     * Taken at **fixed layer height**, so the chain length moves with the grafting density.
     * This is the sensitivity the design faces, because §3 of the problem definition specifies
     * the layer by its height and leaves the grafting density open; the alternative reading,
     * at fixed chain length, lets `L₀` float and answers a different question.
     */
    val stiffnessSensitivity: Double,

    /** The mean polymer volume fraction at the working height — the input to the exponent question. */
    val volumeFractionAtWorkingPoint: Double

)

/** One point of the (grafting density, height) design space, evaluated by every model. */
@Serializable
data class LayerDesignPoint(
    val layerHeight: Double,
    val graftingDensity: Double,
    val graftingSpacing: Double,
    val monomersPerChain: Double,
    val floryRadius: Double,
    val reducedGraftingDensity: Double,
    val regime: String,
    val meanVolumeFraction: Double,
    val responses: List<LayerResponse>
)

/**
 * The set of compression laws every design point is evaluated against.
 *
 * The three scaling variants span the exponent ambiguity §2 of the problem definition
 * refuses to inherit: `9/4` is the good-solvent semidilute (des Cloizeaux) value we start
 * from, `2` is mean-field, and `3` is where the exponent goes in the concentrated/melt limit
 * irrespective of solvent quality. The SCF form is carried alongside because it is a
 * genuinely different functional form, not a reparameterisation.
 */
private fun compressionModels(monomerSize: Double): List<Pair<String, BrushCompressionModel>> = listOf(
    "de-gennes-scaling(m=9/4, good-solvent semidilute)" to DeGennesScaling(osmoticExponent = 9.0 / 4.0),
    "de-gennes-scaling(m=2, mean-field)" to DeGennesScaling(osmoticExponent = 2.0),
    "de-gennes-scaling(m=3, concentrated/theta)" to DeGennesScaling(osmoticExponent = 3.0),
    "milner-witten-cates-scf(height-matched)" to MilnerWittenCates(
        excludedVolume = alexanderDeGennesMatchedExcludedVolume(monomerSize)
    )
)

/**
 * Evaluates one point of the design space: a layer of [layerHeight] nm grafted at
 * [graftingDensity] chains per nm², carrying a tile of footprint [tileArea] nm²
 * pressed by [targetForce] pN.
 *
 * The layer height is the independent variable and the chain length is derived from it,
 * matching the parameterisation of §3 of the problem definition. Both compression models
 * are held to the same `L₀`, so the spread between them is functional form rather than
 * calibration.
 *
 * @throws IllegalArgumentException if any argument is not positive.
 */
fun layerDesignPoint(
    layerHeight: Double,
    graftingDensity: Double,
    monomerSize: Double,
    tileArea: Double,
    targetForce: Double
): LayerDesignPoint {
    require(targetForce > 0.0) { "targetForce must be positive, was: $targetForce" }
    require(tileArea > 0.0) { "tileArea must be positive, was: $tileArea" }
    val brush = brushOfHeight(layerHeight, graftingDensity, monomerSize)
    return LayerDesignPoint(
        layerHeight = layerHeight,
        graftingDensity = graftingDensity,
        graftingSpacing = brush.graftingSpacing,
        monomersPerChain = brush.monomersPerChain,
        floryRadius = brush.floryRadius,
        reducedGraftingDensity = brush.reducedGraftingDensity,
        regime = brush.regime.name,
        meanVolumeFraction = brush.meanVolumeFraction(layerHeight),
        responses = compressionModels(monomerSize).map { (label, model) ->
            layerResponse(label, model, brush, tileArea, targetForce, monomerSize)
        }
    )
}

private fun layerResponse(
    label: String,
    model: BrushCompressionModel,
    brush: PolymerBrush,
    tileArea: Double,
    targetForce: Double,
    monomerSize: Double
): LayerResponse {
    val equilibrium = model.equilibriumHeight(brush)
    val working = model.heightUnderLoad(brush, targetForce, tileArea)
    val stroke = equilibrium - working
    return LayerResponse(
        model = label,
        equilibriumHeight = equilibrium,
        equilibriumStiffness = model.stiffness(brush, equilibrium, tileArea),
        heightUnderTargetForce = working,
        strokeUnderTargetForce = stroke,
        secantStiffness = targetForce / stroke,
        tangentStiffness = model.stiffness(brush, working, tileArea),
        positionalRms = equipartitionRms(model.stiffness(brush, working, tileArea)),
        stiffnessSensitivity = secantStiffnessSensitivity(
            model, brush, tileArea, targetForce, monomerSize
        ),
        volumeFractionAtWorkingPoint = brush.meanVolumeFraction(working)
    )
}

/**
 * Returns `d ln k_secant / d ln σ` by central difference over a ±1% perturbation of the
 * grafting density at fixed layer height.
 *
 * A finite difference rather than a closed form on purpose: the secant stiffness has no
 * closed form once the elastic term and the SCF profile are in play, and a numerical slope
 * that all four models share is comparable across them, whereas four separate analytic
 * derivatives would not be checkable against each other.
 */
private fun secantStiffnessSensitivity(
    model: BrushCompressionModel,
    brush: PolymerBrush,
    tileArea: Double,
    targetForce: Double,
    monomerSize: Double
): Double {
    val height = model.equilibriumHeight(brush)
    val perturbation = 0.01
    fun secantAt(factor: Double): Double {
        val perturbed = brushOfHeight(height, brush.graftingDensity * factor, monomerSize)
        val stroke = model.equilibriumHeight(perturbed) -
                model.heightUnderLoad(perturbed, targetForce, tileArea)
        return targetForce / stroke
    }
    return (ln(secantAt(1.0 + perturbation)) - ln(secantAt(1.0 - perturbation))) /
            (ln(1.0 + perturbation) - ln(1.0 - perturbation))
}
