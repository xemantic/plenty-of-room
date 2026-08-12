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

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlin.math.PI
import kotlin.math.pow

/**
 * A law for the pressure a grafted layer exerts on a **rigid, non-adsorbing wall**
 * pressed into it — the asymmetric geometry of the Gen-1 stack, where the DNA-origami
 * tile is the wall and there is no second brush.
 *
 * §2 of the problem definition names this asymmetry as a documented source of prefactor
 * confusion, and names three functional forms in circulation. Two of them are implemented
 * here ([DeGennesScaling] and [MilnerWittenCates]) so the spread between them can be
 * reported rather than hidden inside a choice.
 *
 * ## Sign convention
 *
 * [disjoiningPressure] is positive when the layer pushes the wall away, along `+z`.
 * [stiffnessPerArea] is `−∂P/∂h`, positive for a restoring layer.
 * A tile resting on an unbiased layer sits at [equilibriumHeight], where the pressure vanishes.
 *
 * ## Validity
 *
 * Every model is defined on `0 < h ≤ L₀` only. Above `L₀` a non-adsorbing brush loses
 * contact with the wall and the true pressure is zero; the scaling form's negative branch
 * there is an artefact of the interpolation, so evaluating it is rejected rather than returned.
 */
sealed interface BrushCompressionModel {

    /** Stable identifier of the model, emitted with every machine-readable result. */
    val name: String

    /** The temperature in K at which this model is evaluated — stated, never implied. */
    val temperature: Double

    /** Returns the unperturbed height of [brush] in nm, as predicted by *this* model. */
    fun equilibriumHeight(brush: PolymerBrush): Double

    /**
     * Returns the pressure in `pN/nm²` that [brush] exerts on a rigid wall held at [height] nm.
     *
     * @throws IllegalArgumentException if [height] is outside `(0, L₀]`.
     */
    fun disjoiningPressure(brush: PolymerBrush, height: Double): Double

    /**
     * Returns `−∂P/∂h` in `pN/nm³`, the stiffness per unit of tile footprint,
     * for [brush] against a rigid wall at [height] nm.
     *
     * @throws IllegalArgumentException if [height] is outside `(0, L₀]`.
     */
    fun stiffnessPerArea(brush: PolymerBrush, height: Double): Double

}

/**
 * The de Gennes (1987) scaling form, mapped from two opposing brushes onto a rigid wall.
 *
 * The published form for two brushes whose grafting planes are separated by `D`,
 *
 * `P(D) = (k_BT/s³)[(2L₀/D)^(9/4) − (D/2L₀)^(3/4)]`,
 *
 * is mapped here by the mirror-plane argument: an impenetrable wall at height `h` imposes
 * exactly the boundary condition that the midplane of two identical non-interpenetrating
 * brushes imposes, so `D → 2h` and the factor of two cancels out of both ratios:
 *
 * `P(h) = (k_BT/s³)[(L₀/h)^(9/4) − (h/L₀)^(3/4)]`.
 *
 * This is the resolution of the prefactor confusion §2 flags: the pressure is the same
 * function *of the compression ratio*, and the error consists in keeping the factor of two
 * while reinterpreting `D` as the wall distance, which understates the pressure at contact
 * by `2^(9/4) ≈ 4.8`. The mapping is if anything cleaner in the wall case than in the
 * two-brush case it was derived for, because a rigid wall enforces the zero-interpenetration
 * assumption exactly, whereas real opposing brushes interdigitate.
 *
 * The elastic exponent `3/4` is an interpolation choice, not Gaussian chain elasticity,
 * which would give exponent 1 with an unrelated prefactor. What justifies it is that
 * integrating this pressure gives a free energy per chain of `(48/35) N/g` at `h = L₀`,
 * i.e. proportional to the Alexander-de Gennes blob count — an analytic consistency argument,
 * not yet an executed check: the free-energy functional itself is task `T-1b`, and task `T-4`
 * needs it anyway to look for pull-in. Under working compression the elastic term is small:
 * at `h/L₀ = 0.5` it is a 12.5% correction to the osmotic term, and shrinking.
 *
 * @param osmoticExponent the exponent `m` of `Π ∝ φ^m`. The default `9/4` is the good-solvent
 *          semidilute (des Cloizeaux) value; `2` is mean-field and `3` the concentrated/melt
 *          limit. §2 of the problem definition is explicit that which of these we are entitled
 *          to is undecided for this layer, so it is a parameter, not a constant.
 * @param elasticExponent the exponent `n` of the chain-elasticity term.
 * @param temperature in K.
 */
data class DeGennesScaling(
    val osmoticExponent: Double = 9.0 / 4.0,
    val elasticExponent: Double = 3.0 / 4.0,
    override val temperature: Double = ROOM_TEMPERATURE
) : BrushCompressionModel {

    init {
        require(osmoticExponent > 1.0) {
            "osmoticExponent must exceed 1, was: $osmoticExponent"
        }
        require(elasticExponent > 0.0) {
            "elasticExponent must be positive, was: $elasticExponent"
        }
        require(temperature > 0.0) { "temperature must be positive, was: $temperature" }
    }

    override val name: String get() = "de-gennes-scaling"

    /** The pressure scale `k_BT/s³` in `pN/nm²`, one thermal energy per grafting volume. */
    fun pressureScale(brush: PolymerBrush): Double =
        thermalEnergy(temperature) / brush.graftingSpacing.pow(3.0)

    override fun equilibriumHeight(brush: PolymerBrush): Double = brush.alexanderDeGennesHeight

    /** Returns the osmotic term of the disjoining pressure alone, in `pN/nm²`. */
    fun osmoticPressure(brush: PolymerBrush, height: Double): Double {
        val ratio = compressionRatio(brush, height)
        return pressureScale(brush) * ratio.pow(-osmoticExponent)
    }

    /** Returns the chain-elasticity term alone, in `pN/nm²`, negative because it opposes the osmotic term. */
    fun elasticPressure(brush: PolymerBrush, height: Double): Double {
        val ratio = compressionRatio(brush, height)
        return -pressureScale(brush) * ratio.pow(elasticExponent)
    }

    override fun disjoiningPressure(brush: PolymerBrush, height: Double): Double =
        osmoticPressure(brush, height) + elasticPressure(brush, height)

    override fun stiffnessPerArea(brush: PolymerBrush, height: Double): Double {
        val ratio = compressionRatio(brush, height)
        return pressureScale(brush) / equilibriumHeight(brush) * (
                osmoticExponent * ratio.pow(-(osmoticExponent + 1.0)) +
                        elasticExponent * ratio.pow(elasticExponent - 1.0)
                )
    }

}

/**
 * The Milner-Witten-Cates self-consistent-field brush, compressed by a rigid wall.
 *
 * SCF gives the layer a parabolic self-consistent potential `U(z) = A(L² − z²)` with
 * `A = 3π²/(8N²a²)`, hence — at second virial — a parabolic segment-density profile
 * `n(z) = U(z)/w`. Squeezing the layer to `h < L₀` truncates the parabola at a finite
 * wall concentration instead of at zero:
 *
 * `n(z) = Γ[1/h + (h² − 3z²)/(2L₀³)]`, `Γ = Nσ`,
 *
 * and the wall pressure follows from the mean-field contact-value theorem,
 * `P = ½ w k_BT n(h)²`, which reduces to
 *
 * `P(h) = ½ w k_BT (Γ/L₀)² (L₀/h − h²/L₀²)²`.
 *
 * §2 of the problem definition observes that this "does not reduce to the same thing"
 * as the scaling form, and it does not, in two ways that matter for the Gen-1 tile:
 * the strong-compression exponent is 2 rather than 9/4 because the construction is
 * mean-field, and the pressure vanishes **quadratically** at `L₀` rather than linearly,
 * so the layer has *zero* stiffness at first contact. A brush with a diffuse outer edge
 * offers no restoring force until it is meaningfully compressed.
 *
 * @param excludedVolume the monomer excluded volume `w = a³(1 − 2χ)` in nm³. Unlike the
 *          scaling form, SCF cannot be written without it, which is why the layer's
 *          solvent quality (§2, second caveat) enters the answer here explicitly.
 *          [alexanderDeGennesMatchedExcludedVolume] gives the value at which the two
 *          models agree on the unperturbed height.
 * @param temperature in K.
 */
data class MilnerWittenCates(
    val excludedVolume: Double,
    override val temperature: Double = ROOM_TEMPERATURE
) : BrushCompressionModel {

    init {
        require(excludedVolume > 0.0) {
            "excludedVolume must be positive, was: $excludedVolume"
        }
        require(temperature > 0.0) { "temperature must be positive, was: $temperature" }
    }

    override val name: String get() = "milner-witten-cates-scf"

    override fun equilibriumHeight(brush: PolymerBrush): Double =
        brush.monomersPerChain * (
                4.0 * excludedVolume * brush.graftingDensity *
                        brush.monomerSize.pow(2.0) / (PI * PI)
                ).pow(1.0 / 3.0)

    /** The grafted coverage `Γ = N σ` in monomers per nm². */
    fun coverage(brush: PolymerBrush): Double =
        brush.monomersPerChain * brush.graftingDensity

    /**
     * Returns the segment number density in `nm⁻³` at [z] nm above the electrode,
     * for [brush] compressed to [height] nm.
     *
     * @throws IllegalArgumentException if [height] is outside `(0, L₀]`, or [z] outside `[0, h]`.
     */
    fun segmentDensity(brush: PolymerBrush, height: Double, z: Double): Double {
        requireWithinLayer(brush, height)
        require(z in 0.0..height) { "z must be within [0.0, $height], was: $z" }
        val unperturbed = equilibriumHeight(brush)
        return coverage(brush) * (
                1.0 / height + (height * height - 3.0 * z * z) /
                        (2.0 * unperturbed.pow(3.0))
                )
    }

    override fun disjoiningPressure(brush: PolymerBrush, height: Double): Double {
        val wallDensity = segmentDensity(brush, height, height)
        return 0.5 * thermalEnergy(temperature) * excludedVolume * wallDensity * wallDensity
    }

    override fun stiffnessPerArea(brush: PolymerBrush, height: Double): Double {
        val ratio = compressionRatio(brush, height)
        val unperturbed = equilibriumHeight(brush)
        val scale = 0.5 * thermalEnergy(temperature) * excludedVolume *
                (coverage(brush) / unperturbed).pow(2.0)
        val profile = 1.0 / ratio - ratio * ratio
        return 2.0 * scale / unperturbed * profile * (1.0 / (ratio * ratio) + 2.0 * ratio)
    }

}

/**
 * Returns the monomer excluded volume at which [MilnerWittenCates] and [DeGennesScaling]
 * predict the same unperturbed height for the same chain, `w = π²a³/4 ≈ 2.47 a³`.
 *
 * The value is independent of chain length and of grafting density, which is what makes
 * it usable as a calibration: with it, any residual difference between the two compression
 * curves is functional form rather than prefactor. It is *not* a claim about PEG —
 * a real excluded volume `w = a³(1 − 2χ)` with `χ ≈ 0.45` is roughly 25 times smaller,
 * and pinning it down is task `P-3`.
 */
fun alexanderDeGennesMatchedExcludedVolume(monomerSize: Double): Double {
    require(monomerSize > 0.0) { "monomerSize must be positive, was: $monomerSize" }
    return PI * PI / 4.0 * monomerSize.pow(3.0)
}

/**
 * Returns the load in pN that [brush] carries when a tile of footprint [area] nm²
 * is held at [height] nm.
 */
fun BrushCompressionModel.load(
    brush: PolymerBrush,
    height: Double,
    area: Double
): Double {
    require(area > 0.0) { "area must be positive, was: $area" }
    return disjoiningPressure(brush, height) * area
}

/**
 * Returns the stiffness in `pN/nm` that a tile of footprint [area] nm² sees
 * from [brush] at [height] nm.
 */
fun BrushCompressionModel.stiffness(
    brush: PolymerBrush,
    height: Double,
    area: Double
): Double {
    require(area > 0.0) { "area must be positive, was: $area" }
    return stiffnessPerArea(brush, height) * area
}

/**
 * Returns the height in nm at which [brush] balances a compressive [load] in pN
 * applied over a tile of footprint [area] nm².
 *
 * Solved by bisection, which is justified rather than lazy here: the pressure is strictly
 * decreasing in the height for every model in this file, so bisection is unconditionally
 * convergent and needs no derivative, and the 100 halvings drive the bracket to machine
 * precision. A Newton iteration would be faster and would risk stepping out of the
 * validity range near `L₀`, where the pressure is flat.
 *
 * @throws IllegalArgumentException if [load] is tensile or [area] is not positive.
 */
fun BrushCompressionModel.heightUnderLoad(
    brush: PolymerBrush,
    load: Double,
    area: Double
): Double {
    require(load >= 0.0) { "load must not be tensile, was: $load" }
    require(area > 0.0) { "area must be positive, was: $area" }
    val unperturbed = equilibriumHeight(brush)
    if (load == 0.0) return unperturbed
    var low = unperturbed * 1e-12
    var high = unperturbed
    repeat(100) {
        val middle = 0.5 * (low + high)
        if (disjoiningPressure(brush, middle) * area > load) low = middle else high = middle
    }
    return 0.5 * (low + high)
}

/**
 * Returns `h/L₀` after checking that [height] is inside the model's validity range.
 */
private fun BrushCompressionModel.compressionRatio(
    brush: PolymerBrush,
    height: Double
): Double {
    requireWithinLayer(brush, height)
    return height / equilibriumHeight(brush)
}

private fun BrushCompressionModel.requireWithinLayer(
    brush: PolymerBrush,
    height: Double
) {
    val unperturbed = equilibriumHeight(brush)
    require(height > 0.0 && height <= unperturbed) {
        "height must be within (0.0, $unperturbed], was: $height"
    }
}
