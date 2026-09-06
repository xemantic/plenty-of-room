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
import kotlin.math.ln
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * How hard it is to push water through the grafted layer, as a Darcy permeability `k`
 * in nm², whose square root is the Brinkman hydrodynamic screening length.
 *
 * ## Why there are three of these and not one
 *
 * `T-7` was told to source the permeability prefactor rather than guess it. Sourcing it
 * produced a disagreement instead of a number, and the disagreement is the result:
 *
 * | model | what it resolves | `√k` at `φ = 0.029` |
 * |---|---|---|
 * | [FreeDrainingSegments] | individual Kuhn rods, no hydrodynamic interaction | 0.99 nm |
 * | [FiberArrayPermeability] | a random array of fibres of the Kuhn radius | 0.86 nm |
 * | [CorrelationLengthScreening] | correlation blobs | 5.60 nm |
 *
 * A factor of 6.4 in length, 40 in permeability. This is not a defect of the sources:
 * it is what `C-0002` and `CH-0001` say should happen. The layer sits at `φ/φ# ≈ 1.1`,
 * where the correlation blob is two thirds of the whole coil, so "monomer scale" and
 * "blob scale" are not separated and no single-length picture can be right about both.
 *
 * The consequence for `T-7` is procedural rather than fatal: the drainage-time bound is
 * quoted from the **slowest** (least permeable) model, so the verdict does not depend on
 * which of them is right — only the size of the margin does.
 */
sealed interface LayerPermeability {

    /** Stable identifier, emitted with every machine-readable result. */
    val name: String

    /** Whether the model is derived here or taken from a source, and which. */
    val provenance: String

    /**
     * Returns the Darcy permeability in nm² at polymer volume fraction [volumeFraction].
     *
     * @throws IllegalArgumentException if [volumeFraction] is outside the model's range.
     */
    fun permeability(volumeFraction: Double): Double

    /**
     * Returns `√k` in nm — the Brinkman screening length, i.e. the distance over which
     * a shear flow is damped inside the layer.
     *
     * This is the number that decides whether the Darcy/Brinkman continuum is being used
     * inside its own domain: it has to be small against the layer thickness, and `T-7`
     * finds that it is for two of the three models and is not for the third.
     */
    fun screeningLength(volumeFraction: Double): Double = sqrt(permeability(volumeFraction))

}

/**
 * The segment-scale permeability with hydrodynamic interaction switched off:
 * every Kuhn segment feels the Stokes friction it would feel alone.
 *
 * **DERIVED.** The drag per unit volume of a suspension of `n` independent segments of
 * friction `ζ̄` is `n ζ̄ u`, and matching that to Darcy's `(η/k) u` gives
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`k = η / (n ζ̄) = v_K / (φ ζ̄/η)`
 *
 * with `n = φ/v_K`. The segment is treated as the thin rod `C-0002` shows it to be
 * (`b³/v_K = 7.09`), so its friction comes from slender-body theory,
 * `ζ_∥ = 2πηb/(ln(2b/d) − ½)` and `ζ_⊥ = 4πηb/(ln(2b/d) + ½)`, orientation-averaged as
 * `ζ̄ = (ζ_∥ + 2ζ_⊥)/3` because a grafted layer at `Σ ≈ 5` is not strongly aligned.
 *
 * Because it omits the shielding that segments give one another, this is expected to
 * **overstate** the drag and so understate the permeability — which is the direction a
 * bound on a drainage *time* has to err in. It is not offered as a rigorous inequality:
 * for a fixed bed the interactions can also raise the drag, and the only honest claim is
 * that this and [FiberArrayPermeability], built on different assumptions, agree to a
 * factor of 1.3.
 *
 * @param segmentLength the Kuhn length `b` in nm.
 * @param segmentDiameter the Kuhn segment's effective diameter `d_K` in nm.
 * @param segmentVolume the Kuhn segment volume `v_K` in nm³.
 */
@Serializable
data class FreeDrainingSegments(
    val segmentLength: Double,
    val segmentDiameter: Double,
    val segmentVolume: Double
) : LayerPermeability {

    init {
        require(segmentLength > 0.0) { "segmentLength must be positive, was: $segmentLength" }
        require(segmentDiameter > 0.0) {
            "segmentDiameter must be positive, was: $segmentDiameter"
        }
        require(segmentVolume > 0.0) { "segmentVolume must be positive, was: $segmentVolume" }
        require(segmentLength > segmentDiameter) {
            "slender-body friction needs a rod, so segmentLength must exceed " +
                    "segmentDiameter, was: $segmentLength vs $segmentDiameter"
        }
    }

    override val name: String get() = "free-draining-kuhn-segments"

    override val provenance: String
        get() = "DERIVED — slender-body friction of a Kuhn rod, no hydrodynamic interaction"

    /** The slenderness logarithm `ln(2b/d)` of one Kuhn segment. */
    val slendernessLogarithm: Double get() = ln(2.0 * segmentLength / segmentDiameter)

    /**
     * The orientation-averaged friction of one Kuhn segment, in units of `η · nm`.
     *
     * For PEG this is 6.68 nm, i.e. a Kuhn rod drags like a sphere of radius 0.355 nm —
     * reassuringly close to the segment's own 0.233 nm radius, and the reason the two
     * segment-scale models land so near each other.
     */
    val segmentFriction: Double
        get() {
            val parallel = 2.0 * PI * segmentLength / (slendernessLogarithm - 0.5)
            val perpendicular = 4.0 * PI * segmentLength / (slendernessLogarithm + 0.5)
            return (parallel + 2.0 * perpendicular) / 3.0
        }

    override fun permeability(volumeFraction: Double): Double {
        requirePhysical(volumeFraction)
        return segmentVolume / (volumeFraction * segmentFriction)
    }

}

/**
 * The permeability of a random three-dimensional array of fibres of radius `r`,
 * from the Jackson–James (1986) correlation:
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`k = r² · (3 / 20φ) · (−ln φ − 0.931)`
 *
 * **CITED, AND NOT VERIFIED AGAINST THE PRIMARY SOURCE.** Jackson, G. W. and James, D. F.,
 * *Can. J. Chem. Eng.* **64**:364 (1986) is paywalled and was not obtained this iteration;
 * the constants above come from secondary literature, which `CLAUDE.md`'s research-practice
 * rule says is not good enough to act on. It is therefore used **only** as a corroborating
 * cross-check on [FreeDrainingSegments], which is derived here in full, and nothing in
 * `T-7` changes if the `0.931` is wrong: the two agree to a factor of 1.3, and the `T-7`
 * bound is quoted from whichever is slower.
 *
 * @param fiberRadius the fibre radius in nm — for PEG, half the Kuhn segment diameter.
 */
@Serializable
data class FiberArrayPermeability(
    val fiberRadius: Double
) : LayerPermeability {

    init {
        require(fiberRadius > 0.0) { "fiberRadius must be positive, was: $fiberRadius" }
    }

    override val name: String get() = "jackson-james-fibre-array"

    override val provenance: String
        get() = "CITED (Jackson & James 1986) — PRIMARY SOURCE NOT OBTAINED; cross-check only"

    override fun permeability(volumeFraction: Double): Double {
        requirePhysical(volumeFraction)
        require(volumeFraction < DILUTE_LIMIT) {
            "volumeFraction must be below $DILUTE_LIMIT for the fibre correlation, " +
                    "was: $volumeFraction"
        }
        return fiberRadius * fiberRadius * (3.0 / (20.0 * volumeFraction)) *
                (-ln(volumeFraction) - LOGARITHMIC_OFFSET)
    }

    companion object {

        /** The `0.931` of the correlation. Cited, unverified — see the class documentation. */
        const val LOGARITHMIC_OFFSET: Double = 0.931

        /**
         * `exp(−0.931) = 0.394`, above which the correlation returns a negative
         * permeability. It is a hard boundary of the *formula*, not of the physics,
         * and it sits far above every volume fraction in the Gen-1 design space.
         */
        val DILUTE_LIMIT: Double = kotlin.math.exp(-LOGARITHMIC_OFFSET)
    }

}

/**
 * The blob-scale permeability, `k = (c ξ)²` with `ξ = v₀^(1/3) φ^(−3/4)` the semidilute
 * correlation length and `c` a prefactor taken as unity.
 *
 * **CITED, and validated on this material.** Two things justify the unit prefactor:
 *
 * - Offeddu, Axpe, Harley & Oyen, *AIP Adv.* **8**:105006 (2018), measure the intrinsic
 *   permeability of PEG hydrogels by poroelastic indentation and state that "the square
 *   root of the intrinsic permeability approximates the size of the fluid path, in this
 *   case corresponding with ξ", finding `k ∝ ξ²` at `r² = 0.92` and `k ∝ φ^(−3/2)` at
 *   `r² = 0.96`. That exponent is reproduced *exactly* by this class, not fitted.
 * - The construction is self-consistent at coil overlap: at `φ* = N^(−4/5)` it returns
 *   `ξ = v₀^(1/3) N^(3/5)`, which is the Flory radius up to `v₀^(1/3)/a = 1.12`.
 *
 * **Its validity is exactly what `CH-0001` puts in doubt.** `ξ ∝ φ^(−3/4)` is a semidilute
 * result, and the Gen-1 layer is at `φ/φ# ≈ 1.1`, inside the crossover. Below overlap the
 * expression keeps growing without bound, which is unphysical — the screening length of a
 * dilute solution is set by the chain concentration, not by a blob — so it is capped at
 * [coilSizeCap] when one is supplied.
 *
 * @param volumetricMonomerSize `v₀^(1/3)` in nm. **Not** the Alexander-de Gennes `a`:
 *          `C-0002` forbids substituting one for the other, and here a volume is meant.
 * @param prefactor the `c` above. Unity by the convention the cited validation was
 *          performed under; carried as a parameter so that a sensitivity on it is possible.
 * @param coilSizeCap the Flory radius in nm, above which `ξ` is clamped, or `null`
 *          to leave the semidilute form unclamped.
 */
@Serializable
data class CorrelationLengthScreening(
    val volumetricMonomerSize: Double,
    val prefactor: Double = 1.0,
    val coilSizeCap: Double? = null
) : LayerPermeability {

    init {
        require(volumetricMonomerSize > 0.0) {
            "volumetricMonomerSize must be positive, was: $volumetricMonomerSize"
        }
        require(prefactor > 0.0) { "prefactor must be positive, was: $prefactor" }
        require(coilSizeCap == null || coilSizeCap > 0.0) {
            "coilSizeCap must be positive when given, was: $coilSizeCap"
        }
    }

    override val name: String get() = "correlation-length-screening"

    override val provenance: String
        get() = "CITED (Offeddu et al. 2018, measured on PEG hydrogels) — k = xi^2, " +
                "prefactor unity, exponent -3/2 reproduced exactly"

    /** `d ln k / d ln φ = −3/2`, exactly, wherever the semidilute form is unclamped. */
    val volumeFractionExponent: Double get() = -1.5

    /** The semidilute correlation length `ξ` in nm at [volumeFraction], before clamping. */
    fun correlationLength(volumeFraction: Double): Double {
        requirePhysical(volumeFraction)
        return volumetricMonomerSize * volumeFraction.pow(-0.75)
    }

    override fun permeability(volumeFraction: Double): Double {
        val unclamped = prefactor * correlationLength(volumeFraction)
        val clamped = if (coilSizeCap == null) unclamped else minOf(unclamped, coilSizeCap)
        return clamped * clamped
    }

}

private fun requirePhysical(volumeFraction: Double) {
    require(volumeFraction > 0.0 && volumeFraction <= 1.0) {
        "volumeFraction must be within (0.0, 1.0], was: $volumeFraction"
    }
}
