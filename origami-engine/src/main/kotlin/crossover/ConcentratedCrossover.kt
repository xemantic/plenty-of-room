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

package com.xemantic.nano.plentyofroom.crossover

import com.xemantic.nano.plentyofroom.brush.FIXMAN_PREFACTOR
import com.xemantic.nano.plentyofroom.brush.kuhnExcludedVolume
import com.xemantic.nano.plentyofroom.brush.reducedSecondVirialCoefficient
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.material.kuhnPairExcludedVolume
import kotlinx.serialization.Serializable
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * `T-21` — the **upper** boundary of the semidilute regime, derived rather than cited.
 *
 * ## What this replaces, and why it had to be replaced
 *
 * `C-0002` carried a **cited** `φ ≈ 0.2–0.3` band for the semidilute→concentrated crossover.
 * `C-0018` then read the floor of that band as an operational ceiling on the usable bias, where
 * it turned out to bind at **121 of 162** states — so the usable bias of the whole Gen-1 device
 * came to rest on a number nobody in this project had derived.
 *
 * ## The convention, stated before any number
 *
 * A volume fraction here is always the **physical** one, `φ = c v₀ = c_K v_K`. Two reduced
 * densities are in circulation and both are smaller or larger by a fixed factor:
 *
 * | convention | relation | factor for PEG |
 * |---|---|---|
 * | physical, `c v₀` | this class | 1 |
 * | Alexander-de Gennes reduced, `n a³` | `C-0002.volumeFractionCorrection` | 1.408 smaller |
 * | Kuhn reduced, `c_K b³` | [kuhnAspectRatio] | **7.09 larger** |
 *
 * The textbook statements about this crossover are written in the *Kuhn reduced* convention with
 * the further assumption `b³ = v_K` — a space-filling statistical segment — which is exactly why
 * the textbook says "the semidilute regime extends to `φ ≈ 1`". PEG's Kuhn segment is a thin rod
 * (`b³/v_K = 7.09`, `C-0002`), and **that ratio is the whole of the departure**.
 *
 * ## The family, and the one number that names a member of it
 *
 * Writing the correlation blob as space filling, `n = φ ξ³ / v_K`, and its internal statistics as
 * Gaussian, `ξ = b √n` (which is the correct branch for this material — see below), eliminating
 * `ξ` gives, with no free parameter,
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;**`n(φ) = (v_K / (b³ φ))²`** &nbsp; and &nbsp; **`φ_c(n) = (v_K/b³) n^(−1/2)`**
 *
 * so the *entire* family of "upper crossovers" is the choice of how many Kuhn segments the
 * correlation blob is required to keep:
 *
 * | `n` | what the criterion says | PEG/water, 300 K |
 * |---|---|---|
 * | `g_T = 1160` (Yamakawa exact) | the blob stops being swollen | **0.00414** |
 * | `g_T = 126` (scaling) | the blob stops being swollen | **0.01255** |
 * | `1` | the blob holds one statistical segment; `ξ = b` | **0.1410** |
 *
 * beside two members that are *not* of this family and are quoted for contrast:
 * [monomerLevelCrossover], the same criterion mis-coarse-grained onto monomers (0.203 — which is
 * where the cited band's floor comes from), and [monomerScaleCorrelation], where `ξ` falls to the
 * volumetric monomer size (0.396).
 *
 * **Naming `n` is naming the convention**, which is what `CLAUDE.md` demands of every crossover
 * in this project after `Σ ≥ 5`, `L₀/R₀ ≥ 1` and the blob stack each failed for want of it.
 *
 * ## Why the Gaussian branch is the right one, and what follows
 *
 * The des Cloizeaux `φ^(9/4)` law needs a **swollen** correlation blob, i.e. `n > g_T`; the
 * solution is semidilute at all only when `n < N_K`. Both at once needs `N_K > g_T`. For PEG in
 * water at 300 K the measured excluded volume gives `g_T = 126–1160` Kuhn segments against
 * `N_K = 32–72` for every Gen-1 chain, so [desCloizeauxWindow] is **empty**: the layer is not
 * entitled to the des Cloizeaux exponent at any volume fraction whatever, and the question "at
 * which `φ` does it stop" has no interior answer. That is the same fact `CLAUDE.md` records as
 * "blob arguments do not apply to Gen-1 chains at all", now in the form that answers `T-21`.
 *
 * @param kuhnLength `b` in nm.
 * @param kuhnSegmentVolume `v_K` in nm³ — the volume a Kuhn segment *occupies*, not `b³`.
 * @param kuhnPairExcludedVolume `v` in nm³ — the **pair** excluded volume of two Kuhn segments,
 *          `n_K² v_m` and never `n_K v_m` (`CH-0020`).
 * @param monomerVolume `v₀` in nm³, for the monomer-scale members of the family only.
 * @param fixmanPrefactor the thermal-blob normalisation: `1` is the scaling convention
 *          `g_T = (b³/v)²`, [FIXMAN_PREFACTOR] `= (3/2π)^(3/2)` is Yamakawa's exact `z(g_T) = 1`.
 *          The two differ by 9.19 in `g_T` and the difference is a **published convention
 *          bracket**, not an error; both are carried.
 */
@Serializable
data class SemidiluteCorrelation(
    val kuhnLength: Double,
    val kuhnSegmentVolume: Double,
    val kuhnPairExcludedVolume: Double,
    val monomerVolume: Double,
    val fixmanPrefactor: Double = 1.0
) {

    init {
        require(kuhnLength > 0.0) { "kuhnLength must be positive, was: $kuhnLength" }
        require(kuhnSegmentVolume > 0.0) {
            "kuhnSegmentVolume must be positive, was: $kuhnSegmentVolume"
        }
        require(kuhnPairExcludedVolume > 0.0) {
            "kuhnPairExcludedVolume must be positive, was: $kuhnPairExcludedVolume"
        }
        require(monomerVolume > 0.0) { "monomerVolume must be positive, was: $monomerVolume" }
        require(fixmanPrefactor > 0.0) {
            "fixmanPrefactor must be positive, was: $fixmanPrefactor"
        }
    }

    /** `b³ / v_K` — how far the statistical segment is from space filling. 7.09 for PEG. */
    val kuhnAspectRatio: Double get() = kuhnLength.pow(3.0) / kuhnSegmentVolume

    /** Converts a physical volume fraction into the Kuhn-reduced one the textbooks use. */
    fun reducedFromPhysical(volumeFraction: Double): Double =
        volumeFraction * kuhnAspectRatio

    /** The inverse of [reducedFromPhysical]. */
    fun physicalFromReduced(reducedDensity: Double): Double =
        reducedDensity / kuhnAspectRatio

    /**
     * The correlation length in nm of an **unswollen** semidilute solution at [volumeFraction],
     * `ξ_θ = v_K / (b² φ)`.
     *
     * Derived, not quoted: a space-filling blob has `n = φ ξ³/v_K` segments and Gaussian
     * statistics give `ξ² = b² n`; eliminating `n` leaves one power of `ξ`.
     * The textbook `ξ = b/φ` is this expression with `v_K = b³`.
     */
    fun idealBlobSize(volumeFraction: Double): Double {
        requirePhysical(volumeFraction)
        return kuhnSegmentVolume / (kuhnLength * kuhnLength * volumeFraction)
    }

    /**
     * The correlation length in nm of a **swollen** semidilute solution at [volumeFraction],
     * `ξ_EV ∝ φ^(−3/4)` — the des Cloizeaux branch.
     *
     * Same construction with `ξ = b n^(3/5)(v/b³)^(1/5)` instead of `ξ = b√n`, giving
     * `ξ = [v_K b^(−5/3)(b³/v)^(1/3)]^(3/4) φ^(−3/4)`. Written in this form deliberately: it is
     * algebraically identical to `material.correlationBlobSize` evaluated in the *reduced*
     * convention, and the two routes are asserted equal as a gate-5 cross-check.
     */
    fun swollenBlobSize(volumeFraction: Double): Double {
        requirePhysical(volumeFraction)
        val amplitude = kuhnSegmentVolume * kuhnLength.pow(-5.0 / 3.0) *
                (kuhnLength.pow(3.0) / kuhnPairExcludedVolume).pow(1.0 / 3.0)
        return amplitude.pow(0.75) * volumeFraction.pow(-0.75)
    }

    /** The number of Kuhn segments in the correlation blob at [volumeFraction] — `(v_K/(b³φ))²`. */
    fun segmentsPerBlob(volumeFraction: Double): Double {
        requirePhysical(volumeFraction)
        val ratio = kuhnSegmentVolume / (kuhnLength.pow(3.0) * volumeFraction)
        return ratio * ratio
    }

    /** The inverse of [segmentsPerBlob]: `φ_c(n) = (v_K/b³) n^(−1/2)`. */
    fun volumeFractionAtSegmentsPerBlob(segments: Double): Double {
        require(segments > 0.0) { "segments must be positive, was: $segments" }
        return kuhnSegmentVolume / (kuhnLength.pow(3.0) * sqrt(segments))
    }

    /** `g_T` in Kuhn segments, `[b³/(κ v)]²` with `κ` the [fixmanPrefactor]. */
    val thermalBlobSegments: Double
        get() {
            val ratio = kuhnLength.pow(3.0) / (fixmanPrefactor * kuhnPairExcludedVolume)
            return ratio * ratio
        }

    /** The thermal blob size `ξ_T = b √g_T` in nm. */
    val thermalBlobSize: Double get() = kuhnLength * sqrt(thermalBlobSegments)

    /**
     * **The crossover this task was asked for**, on the excluded-volume criterion: the volume
     * fraction at which the correlation blob stops being swollen, `φ** = φ_c(g_T)`.
     *
     * In the scaling normalisation this is exactly `v_K v / b⁶`.
     */
    val excludedVolumeCrossover: Double
        get() = volumeFractionAtSegmentsPerBlob(thermalBlobSegments)

    /**
     * The crossover on the **one-segment-per-blob** criterion, `φ_c(1) = v_K/b³ = 0.1410` —
     * the reciprocal of `C-0002`'s Kuhn aspect ratio and nothing else.
     *
     * This is the member of the family the cited `0.2–0.3` band was *trying* to be: the point at
     * which the blob construction has no degrees of freedom left to describe and the solution is
     * concentrated. It is **below** the cited floor.
     */
    val segmentPerBlobCrossover: Double get() = volumeFractionAtSegmentsPerBlob(1.0)

    /** Where the correlation length falls to the **volumetric monomer size**, `v_K/(b² v₀^(1/3))`. */
    val monomerScaleCorrelation: Double
        get() = kuhnSegmentVolume /
                (kuhnLength * kuhnLength * monomerVolume.pow(1.0 / 3.0))

    /**
     * The same excluded-volume criterion **mis-coarse-grained onto monomers**, `v_m/v₀`.
     *
     * Not a member of the family above: it identifies the statistical segment with the monomer,
     * which for PEG is wrong by `n_K` in length and `n_K²` in excluded volume (`CH-0020`).
     * It is carried because it is **0.203** — the floor of the cited `0.2–0.3` band, to three
     * digits — and because `C-0007`'s parameter sheet reports it as "the thermal blob volume
     * fraction". It is 16.2× the Kuhn reading.
     */
    val monomerLevelCrossover: Double
        get() = kuhnPairExcludedVolume /
                (monomerVolume * monomersPerKuhnSegmentSquared)

    /**
     * `n_K²`, recovered from the two volumes this class already carries: `v_K = n_K v₀`.
     * Kept private-by-convention as a derived helper so that [monomerLevelCrossover] does not
     * need a chain parameter it otherwise has no use for.
     */
    private val monomersPerKuhnSegmentSquared: Double
        get() = (kuhnSegmentVolume / monomerVolume).pow(2.0)

    /** The same correlation in Yamakawa's exact thermal-blob normalisation. */
    val exact: SemidiluteCorrelation get() = copy(fixmanPrefactor = FIXMAN_PREFACTOR)

    /**
     * The interval of volume fraction over which a chain of [kuhnSegments] Kuhn segments is
     * entitled to the des Cloizeaux exponent: above overlap and below the thermal-blob crossover.
     *
     * Both edges are `φ_c(n)` at a different `n` — overlap is "the blob is the whole chain",
     * `n = N_K` — so the material prefactor cancels between them and the width is
     * `√(N_K/g_T)` exactly. That is [DesCloizeauxWindow.widthRatio], and it is below one
     * precisely when the chain is shorter than a thermal blob.
     */
    fun desCloizeauxWindow(kuhnSegments: Double): DesCloizeauxWindow {
        require(kuhnSegments > 0.0) { "kuhnSegments must be positive, was: $kuhnSegments" }
        val lower = volumeFractionAtSegmentsPerBlob(kuhnSegments)
        val upper = excludedVolumeCrossover
        return DesCloizeauxWindow(
            lower = lower,
            upper = upper,
            widthRatio = upper / lower,
            kuhnSegments = kuhnSegments,
            thermalBlobSegments = thermalBlobSegments
        )
    }

    private fun requirePhysical(volumeFraction: Double) {
        require(volumeFraction > 0.0 && volumeFraction <= 1.0) {
            "volumeFraction must be within (0.0, 1.0], was: $volumeFraction"
        }
    }
}

/**
 * The window `(φ*, φ**)` in which the des Cloizeaux exponent is the one a solution is entitled to.
 *
 * @property lower the ideal-coil overlap fraction `φ* = (v_K/b³) N_K^(−1/2)`.
 * @property upper the thermal-blob crossover `φ** = (v_K/b³) g_T^(−1/2)`.
 * @property widthRatio `φ** over φ* = √(N_K/g_T)`, **exactly** — below one means the window is empty.
 */
@Serializable
data class DesCloizeauxWindow(
    val lower: Double,
    val upper: Double,
    val widthRatio: Double,
    val kuhnSegments: Double,
    val thermalBlobSegments: Double
) {
    /** True when the chain is shorter than a thermal blob and the window does not exist. */
    val isEmpty: Boolean get() = lower >= upper
}

/**
 * The Kuhn-segment correlation of PEG in water, built from `C-0002`'s parameter sheet and the
 * **measured osmotic second virial coefficient** [osmoticSecondVirialCoefficient],
 * `A₂ = 1.9e−3 mol·cm³/g²`.
 *
 * `A₂` is reduced to `B = v_m/v₀ = 2 A₂ M₀/V̄` by `brush.reducedSecondVirialCoefficient` — the
 * factor-of-two trap `CLAUDE.md` records — and coarse-grained to the Kuhn pair by
 * `brush.kuhnExcludedVolume`, which applies `CH-0020`'s `n_K²` rather than `n_K`.
 * This class therefore carries **no** material number of its own.
 */
fun PegWater.semidiluteCorrelation(
    osmoticSecondVirialCoefficient: Double
): SemidiluteCorrelation = SemidiluteCorrelation(
    kuhnLength = kuhnLength,
    kuhnSegmentVolume = kuhnSegmentVolume,
    kuhnPairExcludedVolume = kuhnExcludedVolume(
        reducedSecondVirialCoefficient(osmoticSecondVirialCoefficient)
    ),
    monomerVolume = monomerVolume
)

/**
 * The same correlation built from an explicit **monomer-pair** excluded volume in nm³.
 *
 * Exists because this project has **two independent routes** to that number and they differ by
 * 2.5×: `C-0003`'s osmometry route (`v_m = B v₀ = 0.01225 nm³`, from the measured `A₂`) and
 * `C-0007`'s Flory-Huggins route (`v_m = v₀(v₀/v_water)(1 − 2χ) = 0.03114 nm³`). Every crossover
 * that carries `v` must therefore be quoted on **both**, and the coarse-graining to the Kuhn pair
 * is `n_K² v_m` on each (`CH-0020`).
 */
fun PegWater.semidiluteCorrelationFromExcludedVolume(
    monomerExcludedVolume: Double
): SemidiluteCorrelation = SemidiluteCorrelation(
    kuhnLength = kuhnLength,
    kuhnSegmentVolume = kuhnSegmentVolume,
    kuhnPairExcludedVolume = kuhnPairExcludedVolume(
        monomerExcludedVolume, monomersPerKuhnSegment
    ),
    monomerVolume = monomerVolume
)

/**
 * Converts a polymer **weight** fraction into the **physical volume** fraction this project uses.
 *
 * Needed because the equation of state `C-0002` adopts is fitted over a range quoted in weight
 * percent, and the ceiling `C-0018` consumes is a volume fraction. Ideal volume additivity, which
 * is the same assumption the partial specific volume already encodes.
 *
 * @param weightFraction polymer mass over total mass, in `[0, 1]`.
 * @param polymerDensity the hydrated polymer mass density in g/cm³ (`1/V̄`).
 * @param solventDensity the solvent mass density in g/cm³.
 */
fun weightToVolumeFraction(
    weightFraction: Double,
    polymerDensity: Double,
    solventDensity: Double
): Double {
    require(weightFraction in 0.0..1.0) {
        "weightFraction must be within [0.0, 1.0], was: $weightFraction"
    }
    require(polymerDensity > 0.0) { "polymerDensity must be positive, was: $polymerDensity" }
    require(solventDensity > 0.0) { "solventDensity must be positive, was: $solventDensity" }
    val polymer = weightFraction / polymerDensity
    val solvent = (1.0 - weightFraction) / solventDensity
    return polymer / (polymer + solvent)
}

/** The inverse of [weightToVolumeFraction]. */
fun volumeToWeightFraction(
    volumeFraction: Double,
    polymerDensity: Double,
    solventDensity: Double
): Double {
    require(volumeFraction in 0.0..1.0) {
        "volumeFraction must be within [0.0, 1.0], was: $volumeFraction"
    }
    require(polymerDensity > 0.0) { "polymerDensity must be positive, was: $polymerDensity" }
    require(solventDensity > 0.0) { "solventDensity must be positive, was: $solventDensity" }
    val polymer = volumeFraction * polymerDensity
    val solvent = (1.0 - volumeFraction) * solventDensity
    return polymer / (polymer + solvent)
}

/**
 * The gap in nm at which a layer of [dryThickness] `= N σ v₀` reaches [volumeFraction].
 *
 * This is the **only** channel through which the crossover reaches `C-0018`: the ceiling is a
 * bias read on the equilibrium path at this gap, and nothing else about the crossover enters.
 */
fun gapAtVolumeFraction(dryThickness: Double, volumeFraction: Double): Double {
    require(dryThickness > 0.0) { "dryThickness must be positive, was: $dryThickness" }
    require(volumeFraction > 0.0 && volumeFraction <= 1.0) {
        "volumeFraction must be within (0.0, 1.0], was: $volumeFraction"
    }
    return dryThickness / volumeFraction
}
