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

package com.xemantic.nano.plentyofroom.material

import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.pow

/**
 * The effective `χ` of a **grafted** PEG layer — task `P-9`.
 *
 * ## Why this file exists
 *
 * Every osmotic number in this programme (`C-0001`, `C-0002`, `C-0003`, `C-0004`) is a
 * **bulk-solution** property applied to a **brush**. `C-0007` flagged that as the largest
 * un-discharged premise in the material sheet, on the strength of a report that a grafted PEO
 * layer sits at an effective `χ ≈ 0.60` — above θ, formally poor solvent — against `0.372`
 * measured in bulk.
 *
 * `P-9` is the task that reads the two sources instead of their abstracts, and the answer turns
 * on a distinction this file makes structural: **a `χ` fitted inside a model and a `χ` inferred
 * from a compression isotherm are not the same object.**
 *
 * ## Two kinds of fitted `χ`, and only one of them travels
 *
 * [ScfBrushChiFit] is the first kind. Lee et al. fitted `χ` as one of two adjustable parameters
 * of their own continuum self-consistent field model, which uses **unequal monomer and solvent
 * volumes** (`v_PEO = 59.2 Å³`, `v_water = 29.9 Å³`) and therefore does **not** put its own theta
 * point at `½`. They located it, in that model, at `χ ≈ 0.696`, and say in as many words that
 * *"simply setting the `χ` value to 0.5 in our model … would not be able to produce results that
 * precisely correspond to the behavior under the so-called θ condition"*. The `≈ 0.60` in
 * circulation is the paper's `χ/χ_θ ≈ 1.2` multiplied by the Flory-Huggins `½` — a transfer the
 * paper's own text forbids. [floryHugginsByRatio] and [floryHugginsByOffset] carry the two
 * defensible linear transfers side by side precisely so the gap between them is visible.
 *
 * [AlexanderDeGennesBrushFit] is the second kind. Hansen et al. fitted the Alexander-de Gennes
 * compression law to *measured* osmotic-stress isotherms of PEG-grafted bilayers, holding the
 * des Cloizeaux amplitude at the value they had fitted to **bulk** osmometry in the same paper
 * and letting the effective monomer length float. That fit is an excluded-volume measurement of
 * a grafted layer on the same convention as the bulk one, so its ratio to bulk *is* transferable
 * — and it is the cheap bound `§5` asks for before any modelling.
 *
 * ## The limiting case that decides the task
 *
 * A des Cloizeaux amplitude is a **positive power of a positive excluded volume**. There is no
 * effective monomer length, however small, that represents `χ ≥ ½`. So a grafted `χ` at or above
 * theta is not a large correction to the free energies `C-0003` uses — it is outside the family
 * altogether, and [interactionRatioFromEffectiveChi] refuses it rather than returning a number.
 *
 * ## Units
 *
 * Lengths in **Å** where a source reports Å (the two fits both do) and in **nm** everywhere a
 * quantity leaves this file. Grafting densities are always `nm⁻²`, volume fractions are always
 * **physical** (`N σ v₀ / h`), and `χ` is always on the **water-molecule** lattice site of
 * `C-0007`. Nothing here takes a bare "monomer size".
 */

/** Ångström per nanometre. The only unit conversion in this task, and it is squared and cubed. */
const val ANGSTROM_PER_NANOMETRE: Double = 10.0

/** The Gen-1 grafting window's lower edge in `nm⁻²`, from `C-0003`'s 10 nm design window. */
const val GEN1_GRAFTING_DENSITY_LOW: Double = 0.018

/** The Gen-1 grafting window's upper edge in `nm⁻²`, from `C-0003`'s 10 nm design window. */
const val GEN1_GRAFTING_DENSITY_HIGH: Double = 0.092

/**
 * The power of the effective monomer length in the des Cloizeaux osmotic amplitude, **15/4**.
 *
 * In the Alexander-de Gennes convention one length does all three jobs, so with `φ = n a³`
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`Π = α (k_BT/a³) φ^(9/4) = α k_BT n^(9/4) a^(15/4)`
 *
 * and at fixed **physical** monomer number density the interaction strength goes as `a^(15/4)`.
 * That steepness is why an unconstrained fit pins `a` to a few percent, and it is why a few
 * percent in `a` is only a few tens of percent in the interaction.
 */
const val DES_CLOIZEAUX_AMPLITUDE_EXPONENT: Double = 15.0 / 4.0

/**
 * The power of the monomer excluded volume in the des Cloizeaux osmotic amplitude, **3/4**.
 *
 * From the space-filling self-avoiding correlation blob: `ξ = b (v/b³)^(−1/4) φ^(−3/4)` and
 * `Π = k_BT/ξ³`. Derived in [desCloizeauxPressureFromBlob] and checked as a log-slope, not quoted.
 */
const val DES_CLOIZEAUX_EXCLUDED_VOLUME_EXPONENT: Double = 0.75

/**
 * The mean-field alternative, **1** — `Π_int = (v/2v₀²) k_BT φ²` is linear in the excluded volume.
 *
 * Carried as the other end of a bracket, exactly as `C-0007` brackets its transfer function
 * between the blob exponent and the mean-field one. It is the **less** forgiving of the two:
 * a given change in interaction strength implies a smaller change in excluded volume under 3/4
 * than under 1, so the mean-field reading gives the tighter `χ`.
 */
const val MEAN_FIELD_EXCLUDED_VOLUME_EXPONENT: Double = 1.0

/**
 * `1/(m+1) = 4/13` for the des Cloizeaux exponent `m = 9/4` — `C-0003`'s exact result that the
 * layer stiffness goes as `k ∝ K^(1/(m+1))` at fixed height, grafting density and compression.
 *
 * The chain length a specified height demands moves *against* the interaction and very nearly
 * cancels it. This is why `P-9`'s answer matters less than its size suggests.
 */
const val DES_CLOIZEAUX_STIFFNESS_EXPONENT: Double = 4.0 / 13.0

/**
 * `d ln(stroke) / d ln K = −0.1019`, defined by `C-0003`'s own 16-fold sensitivity study at the
 * 10 nm design point, where the stroke moved from **5.81 nm to 4.38 nm**.
 *
 * It is *defined* by that pair rather than re-derived, and flagged as such: it is the one number
 * in this file inherited from a standing claim instead of computed here.
 */
val C0003_STROKE_LOG_SLOPE: Double =
    kotlin.math.ln(4.38 / 5.81) / kotlin.math.ln(16.0)

/**
 * The correlation blob size `ξ` in nm of a semidilute solution at [volumeFraction], for a chain
 * of Kuhn length [kuhnLength] with monomer-pair excluded volume [excludedVolume] — **DERIVED**.
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`ξ = b (v/b³)^(−1/4) φ^(−3/4)`
 *
 * The blob is space filling and the chain is self-avoiding *inside* it, which is what fixes the
 * `−1/4`. At `φ = v/b³` it reduces to the thermal blob `b(b³/v)`, which is the limiting case the
 * test uses to check it.
 *
 * @throws IllegalArgumentException if any argument is not positive.
 */
fun correlationBlobSize(
    volumeFraction: Double,
    kuhnLength: Double,
    excludedVolume: Double
): Double {
    require(volumeFraction > 0.0) { "volumeFraction must be positive, was: $volumeFraction" }
    require(kuhnLength > 0.0) { "kuhnLength must be positive, was: $kuhnLength" }
    require(excludedVolume > 0.0) { "excludedVolume must be positive, was: $excludedVolume" }
    val reduced = excludedVolume / kuhnLength.pow(3.0)
    return kuhnLength * reduced.pow(-0.25) * volumeFraction.pow(-0.75)
}

/**
 * The des Cloizeaux osmotic pressure `Π = k_BT/ξ³` in pN/nm², built from [correlationBlobSize]
 * rather than from a fitted amplitude — **DERIVED**.
 *
 * Exists so that [DES_CLOIZEAUX_EXCLUDED_VOLUME_EXPONENT] can be measured off it as a log-slope
 * instead of asserted, which is the difference between a checked exponent and a remembered one.
 */
fun desCloizeauxPressureFromBlob(
    volumeFraction: Double,
    kuhnLength: Double,
    excludedVolume: Double,
    thermalEnergy: Double = thermalEnergy()
): Double {
    val blob = correlationBlobSize(volumeFraction, kuhnLength, excludedVolume)
    return thermalEnergy / blob.pow(3.0)
}

/**
 * Converts a **brush/bulk interaction-strength ratio** into the brush's effective `χ`, on the
 * same lattice convention as [bulkChi].
 *
 * `K ∝ v^p` with `p` = [excludedVolumeExponent], and `v ∝ (1 − 2χ)`, so
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`(1 − 2χ_brush) = (1 − 2χ_bulk) · (K_brush/K_bulk)^(1/p)`
 *
 * Note what this **cannot** produce: `χ ≥ ½` for any positive ratio. The theta point is the
 * `K → 0` limit and is approached, never reached.
 *
 * @throws IllegalArgumentException if [interactionRatio] is not positive, or if [bulkChi] is not
 *          below the theta point — a bulk solvent already at theta leaves nothing to scale.
 */
fun effectiveChiFromInteractionRatio(
    interactionRatio: Double,
    bulkChi: Double,
    excludedVolumeExponent: Double = DES_CLOIZEAUX_EXCLUDED_VOLUME_EXPONENT
): Double {
    require(interactionRatio > 0.0) {
        "interactionRatio must be positive, was: $interactionRatio"
    }
    require(bulkChi < THETA_CHI) {
        "bulkChi must be below the theta point $THETA_CHI, was: $bulkChi"
    }
    require(excludedVolumeExponent > 0.0) {
        "excludedVolumeExponent must be positive, was: $excludedVolumeExponent"
    }
    val volumeRatio = interactionRatio.pow(1.0 / excludedVolumeExponent)
    return 0.5 * (1.0 - (1.0 - 2.0 * bulkChi) * volumeRatio)
}

/**
 * The exact inverse of [effectiveChiFromInteractionRatio] — the brush/bulk interaction ratio an
 * effective `χ` would require.
 *
 * **It throws for `χ ≥ ½`, and that refusal is the substance of `P-9`.** A des Cloizeaux or
 * Alexander-de Gennes free energy has no representation of a poor solvent; a grafted `χ` above
 * theta is not a correction to it but a statement that a different free energy is needed.
 *
 * @throws IllegalArgumentException if [effectiveChi] is at or above the theta point, or if
 *          [bulkChi] is not below it.
 */
fun interactionRatioFromEffectiveChi(
    effectiveChi: Double,
    bulkChi: Double,
    excludedVolumeExponent: Double = DES_CLOIZEAUX_EXCLUDED_VOLUME_EXPONENT
): Double {
    require(bulkChi < THETA_CHI) {
        "bulkChi must be below the theta point $THETA_CHI, was: $bulkChi"
    }
    require(effectiveChi < THETA_CHI) {
        "effectiveChi must be below the theta point $THETA_CHI to have any representation as a " +
                "des Cloizeaux interaction strength — a positive amplitude is a positive power " +
                "of a positive excluded volume. Was: $effectiveChi"
    }
    require(excludedVolumeExponent > 0.0) {
        "excludedVolumeExponent must be positive, was: $excludedVolumeExponent"
    }
    val volumeRatio = (1.0 - 2.0 * effectiveChi) / (1.0 - 2.0 * bulkChi)
    return volumeRatio.pow(excludedVolumeExponent)
}

/**
 * `k_brush/k_bulk` for a change of interaction strength, at fixed layer height, grafting density
 * and compression ratio — `C-0003`'s exact `k ∝ K^(1/(m+1))`.
 */
fun stiffnessRatioFromInteractionRatio(
    interactionRatio: Double,
    stiffnessExponent: Double = DES_CLOIZEAUX_STIFFNESS_EXPONENT
): Double {
    require(interactionRatio > 0.0) {
        "interactionRatio must be positive, was: $interactionRatio"
    }
    return interactionRatio.pow(stiffnessExponent)
}

/**
 * `stroke_brush/stroke_bulk` for a change of interaction strength, at the 10 nm design point,
 * from [C0003_STROKE_LOG_SLOPE] — **INHERITED from `C-0003`**, not derived here.
 */
fun strokeRatioFromInteractionRatio(
    interactionRatio: Double,
    strokeLogSlope: Double = C0003_STROKE_LOG_SLOPE
): Double {
    require(interactionRatio > 0.0) {
        "interactionRatio must be positive, was: $interactionRatio"
    }
    return interactionRatio.pow(strokeLogSlope)
}

/**
 * A `χ` obtained as an **adjustable parameter of a self-consistent field model**, together with
 * the theta point **of that same model**.
 *
 * The defaults are Lee, Kim, Witte, Ohn, Choi, Akgun, Satija & Won,
 * *J. Phys. Chem. B* **116**:7367 (2012), §3.3 and Figure 4, read from the PDF.
 * The system is a **poly(ethylene oxide)-poly(n-butyl acrylate) diblock Langmuir monolayer at the
 * air/D₂O interface**, not a solid-grafted brush: the grafting plane is a single-monomer-thick
 * PnBA film, fitted as *indifferent* (`κ⁻¹ = ∞`), and the reported surface pressure is a
 * **lateral** one measured by Wilhelmy plate in a trough, not the normal disjoining pressure a
 * Gen-1 layer has to supply against the tile.
 *
 * @param areaPerChain `α` in **Å²/chain**, as reported.
 * @param fittedChi the best-fit `χ` of that model — **CITED**.
 * @param modelThetaChi the `χ` at which *this model* reproduces Gaussian single-chain statistics
 *          — **CITED**, 0.696, and not `½`, which is the whole point.
 * @param radiusOfGyration `R_G` of the PEO block in **Å**, self-avoiding — **CITED**, 25.7 Å.
 * @param monomerSiteVolume the model's own `v_PEO` in **Å³** — **CITED**, 59.2.
 * @param solventSiteVolume the model's own `v_water` in **Å³** — **CITED**, 29.9.
 */
@Serializable
data class ScfBrushChiFit(
    val label: String,
    val areaPerChain: Double,
    val fittedChi: Double,
    val modelThetaChi: Double,
    val fittedChiUncertainty: Double = 0.0,
    val monomersPerChain: Double = 113.0,
    val radiusOfGyration: Double = 25.7,
    val monomerSiteVolume: Double = 59.2,
    val solventSiteVolume: Double = 29.9,
    val geometry: String = "air/D2O Langmuir monolayer of a PEO-PnBA diblock; grafting plane is " +
            "a single-monomer-thick PnBA film fitted as indifferent (kappa^-1 = infinity); the " +
            "reported pressure is LATERAL surface pressure, not a normal disjoining pressure",
    val source: String = "Lee, Kim, Witte, Ohn, Choi, Akgun, Satija & Won, " +
            "J. Phys. Chem. B 116:7367 (2012), doi:10.1021/jp301817e, section 3.3, read from PDF"
) {

    init {
        require(areaPerChain > 0.0) { "areaPerChain must be positive, was: $areaPerChain" }
        require(fittedChi > 0.0) { "fittedChi must be positive, was: $fittedChi" }
        require(modelThetaChi > 0.0) { "modelThetaChi must be positive, was: $modelThetaChi" }
        require(fittedChiUncertainty >= 0.0) {
            "fittedChiUncertainty must not be negative, was: $fittedChiUncertainty"
        }
        require(monomersPerChain > 0.0) {
            "monomersPerChain must be positive, was: $monomersPerChain"
        }
        require(radiusOfGyration > 0.0) {
            "radiusOfGyration must be positive, was: $radiusOfGyration"
        }
        require(monomerSiteVolume > 0.0) {
            "monomerSiteVolume must be positive, was: $monomerSiteVolume"
        }
        require(solventSiteVolume > 0.0) {
            "solventSiteVolume must be positive, was: $solventSiteVolume"
        }
    }

    /** `σ = 1/α` in `nm⁻²` — **DERIVED**. */
    val graftingDensity: Double
        get() = ANGSTROM_PER_NANOMETRE.pow(2.0) / areaPerChain

    /** `Σ = σ π R_G²` — **DERIVED**, dimensionless, the paper's own reduced measure. */
    val reducedGraftingDensity: Double
        get() = PI * radiusOfGyration * radiusOfGyration / areaPerChain

    /** `χ − χ_θ(model)` — **DERIVED**. How far past *its own* theta point the fit sits. */
    val chiPastModelTheta: Double get() = fittedChi - modelThetaChi

    /** `χ / χ_θ(model)` — **DERIVED**. This is the `≈ 1.2` the abstract reports. */
    val chiRatioToModelTheta: Double get() = fittedChi / modelThetaChi

    /**
     * The first of two linear transfers onto the Flory-Huggins axis: preserve the **ratio** to
     * theta, `χ_FH = (χ/χ_θ) · ½`.
     *
     * This is the transfer that produces the `≈ 0.60` in circulation. It is a **construction**,
     * not a value reported anywhere in the source.
     */
    val floryHugginsByRatio: Double get() = chiRatioToModelTheta * THETA_CHI

    /**
     * The second: preserve the **distance** past theta, `χ_FH = ½ + (χ − χ_θ)`.
     *
     * Equally defensible a priori, and it disagrees. The disagreement is [transferSpread].
     */
    val floryHugginsByOffset: Double get() = THETA_CHI + chiPastModelTheta

    /** `|ratio transfer − offset transfer|` — **DERIVED**. The size of the non-transferability. */
    val transferSpread: Double
        get() = kotlin.math.abs(floryHugginsByRatio - floryHugginsByOffset)

    /**
     * `v_monomer / v_solvent` of the model's own sites — **DERIVED**, 1.980.
     *
     * `C-0007` derives 2.010 for the same ratio from the partial specific volume of PEG and the
     * mass density of water, without reference to this paper. Agreement to 1.5 % is what
     * establishes the `0.696` as a **convention offset** rather than a fitting artefact: it is
     * the same unequal-site trap `C-0007` names, inside a different model.
     */
    val modelSiteVolumeRatio: Double get() = monomerSiteVolume / solventSiteVolume
}

/**
 * A two-parameter **unconstrained** Alexander-de Gennes fit to a *measured* compression isotherm
 * of a grafted polymer layer.
 *
 * The defaults are Hansen, Cohen, Podgornik & Parsegian, *Biophys. J.* **84**:350 (2003), Fig. 3
 * — DSPC:PEG-5000 multilamellar liposomes under osmotic stress, the data of Kenworthy et al.
 * (1995), and the only data in that literature that met Hansen et al.'s own brush criterion.
 *
 * **Why this one transfers where the SCF fit does not.** The des Cloizeaux amplitude `α = 0.8`
 * was fitted in the same paper to **bulk** osmometry across seven PEG molecular weights, then
 * held fixed while `a` and `L₀` floated against the **grafted** isotherms. So `(a_fit/a_bulk)`
 * is a brush-versus-bulk excluded-volume comparison **inside one convention and one dataset
 * family** — and a convention that cancels out of the ratio, which matters because `C-0003`
 * records that the Alexander-de Gennes unity prefactor is worth 6.6× in excluded volume.
 *
 * @param fittedMonomerLength `a` in **Å** — **CITED**, 3.56 ± 0.07 and 3.30 ± 0.15.
 * @param restingHeight `L₀` in **Å** — **CITED**, 105 and 109.
 * @param bulkMonomerLength the `a` at which the same paper fitted the **bulk** des Cloizeaux
 *          limb — **CITED**, 3.5 Å (Kenworthy et al.'s structural value).
 */
@Serializable
data class AlexanderDeGennesBrushFit(
    val label: String,
    val fittedMonomerLength: Double,
    val restingHeight: Double,
    val fittedMonomerLengthUncertainty: Double = 0.0,
    val monomersPerChain: Double = 113.0,
    val bulkMonomerLength: Double = 3.5,
    val desCloizeauxAmplitude: Double = 0.8,
    val geometry: String = "PEG-lipid grafted to DSPC bilayers in multilamellar liposomes, " +
            "compressed NORMALLY by osmotic stress; the fitted quantity is a disjoining pressure",
    val source: String = "Hansen, Cohen, Podgornik & Parsegian, Biophys. J. 84:350 (2003), " +
            "Fig. 3, fitting the osmotic-stress data of Kenworthy et al. (1995a), read from " +
            "the PMC full text"
) {

    init {
        require(fittedMonomerLength > 0.0) {
            "fittedMonomerLength must be positive, was: $fittedMonomerLength"
        }
        require(restingHeight > 0.0) { "restingHeight must be positive, was: $restingHeight" }
        require(fittedMonomerLengthUncertainty >= 0.0) {
            "fittedMonomerLengthUncertainty must not be negative, " +
                    "was: $fittedMonomerLengthUncertainty"
        }
        require(fittedMonomerLengthUncertainty < fittedMonomerLength) {
            "fittedMonomerLengthUncertainty must be smaller than the value it qualifies, " +
                    "was: $fittedMonomerLengthUncertainty against $fittedMonomerLength"
        }
        require(monomersPerChain > 0.0) {
            "monomersPerChain must be positive, was: $monomersPerChain"
        }
        require(bulkMonomerLength > 0.0) {
            "bulkMonomerLength must be positive, was: $bulkMonomerLength"
        }
        require(desCloizeauxAmplitude > 0.0) {
            "desCloizeauxAmplitude must be positive, was: $desCloizeauxAmplitude"
        }
    }

    /**
     * `D = (N a^(5/3) / L₀)^(3/2)` in **Å** — **DERIVED**, the inversion of the Alexander-de
     * Gennes height relation the fit uses to eliminate the grafting spacing.
     *
     * `C-0003` **replaced** this relation for the Gen-1 layer, on the grounds that PEG's chains
     * hold 0.02–0.10 of a thermal blob and are not swollen. It is used here anyway, and must be:
     * the question is what Hansen et al.'s fit *means*, and it means what their form says it
     * means. The consequence is carried in the validity range, not hidden.
     */
    val graftingSpacing: Double
        get() = (monomersPerChain * fittedMonomerLength.pow(5.0 / 3.0) / restingHeight).pow(1.5)

    /** `σ = 1/D²` in `nm⁻²` — **DERIVED**. */
    val graftingDensity: Double
        get() = ANGSTROM_PER_NANOMETRE.pow(2.0) / (graftingSpacing * graftingSpacing)

    /** `n = N/(D² L₀)` in `nm⁻³` — **DERIVED**, the physical monomer number density. */
    val monomerNumberDensity: Double
        get() = monomersPerChain * ANGSTROM_PER_NANOMETRE.pow(3.0) /
                (graftingSpacing * graftingSpacing * restingHeight)

    /**
     * The **physical** volume fraction `φ = n v₀` — **DERIVED**.
     *
     * Physical, per `CLAUDE.md`: this is *not* the Alexander-de Gennes reduced density `n a³`,
     * which for PEG is 1.408× smaller.
     */
    fun physicalVolumeFraction(monomerVolume: Double): Double {
        require(monomerVolume > 0.0) { "monomerVolume must be positive, was: $monomerVolume" }
        return monomerNumberDensity * monomerVolume
    }

    /**
     * `K_brush / K_bulk = (a_fit / a_bulk)^(15/4)` — **DERIVED**.
     *
     * At fixed physical monomer density and fixed des Cloizeaux amplitude, the interaction
     * strength goes as [DES_CLOIZEAUX_AMPLITUDE_EXPONENT] in the effective monomer length.
     */
    fun interactionStrengthRatio(monomerLength: Double = fittedMonomerLength): Double {
        require(monomerLength > 0.0) { "monomerLength must be positive, was: $monomerLength" }
        return (monomerLength / bulkMonomerLength).pow(DES_CLOIZEAUX_AMPLITUDE_EXPONENT)
    }

    /** The interaction ratio at `a − δa`, `a` and `a + δa` — **DERIVED**. */
    fun interactionStrengthRatioBand(): List<Double> = listOf(
        fittedMonomerLength - fittedMonomerLengthUncertainty,
        fittedMonomerLength,
        fittedMonomerLength + fittedMonomerLengthUncertainty
    ).map { interactionStrengthRatio(it) }

    /** The effective `χ` of this grafted layer, on [bulkChi]'s convention — **DERIVED**. */
    fun effectiveChi(
        bulkChi: Double,
        excludedVolumeExponent: Double = DES_CLOIZEAUX_EXCLUDED_VOLUME_EXPONENT
    ): Double = effectiveChiFromInteractionRatio(
        interactionRatio = interactionStrengthRatio(),
        bulkChi = bulkChi,
        excludedVolumeExponent = excludedVolumeExponent
    )

    /** The effective `χ` across the fit's own 1σ band — **DERIVED**. */
    fun effectiveChiBand(
        bulkChi: Double,
        excludedVolumeExponent: Double = DES_CLOIZEAUX_EXCLUDED_VOLUME_EXPONENT
    ): List<Double> = interactionStrengthRatioBand().map {
        effectiveChiFromInteractionRatio(it, bulkChi, excludedVolumeExponent)
    }
}
