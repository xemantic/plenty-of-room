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

import com.xemantic.nano.plentyofroom.material.PegWater
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sin

/**
 * Task `T-1e` / leaf `A2.1` — the **first-moment** height convention, and the chain-length
 * inversion that goes with it.
 *
 * ## Why this file exists
 *
 * `C-0011` inverts `N` on a **force-onset** height: `L₀` is the height at which the layer first
 * carries a stated load. `C-0003` inverts it on the **edge of a trial function**. They are not the
 * same quantity, and `CH-0010` says so — but it prices the difference by *scaling* the solved
 * layer's first moment with an exponent read off a different quantity, and calls that an
 * *"extrapolation, not a computed design point"*. This file removes the extrapolation.
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`2⟨z⟩ = 2 ∫ z φ(z) dz / ∫ φ(z) dz`
 *
 * is a *functional of the profile*, so it is available for every one of the three models on equal
 * terms, and `N` can be inverted on it by the same bracketed root that inverts it on the
 * force-onset height. The two inversions then differ in the **height functional** and in nothing
 * else — the same discipline `T-1d` used to make its answer and `T-1c`'s differ in the profile and
 * in nothing else.
 *
 * ## Why the first moment, and not some other thickness
 *
 * Because it is the one functional under which an **Alexander box is quoting its own height
 * honestly**: for a box profile `⟨z⟩ = L/2` identically, at every chain length and every grafting
 * density, so `2⟨z⟩ = L`. Any comparison of the box against a solved profile that does not use it
 * is comparing two definitions and attributing the difference to physics.
 *
 * It is also the quantity a **measurement** would return. An ellipsometric or neutron-reflectivity
 * thickness is a moment of the profile, not the height at which a probe first feels a force.
 *
 * ## What this file does NOT do
 *
 * It does not edit [SelfConsistentField] or `ScfDensityProfileStudy`. Everything here is additive,
 * and [firstMomentThickness] on a [SelfConsistentFieldLayer] returns
 * [ScfProfile.firstMomentHeight] **unchanged** — asserted as a departure of exactly `0.0`, because
 * the two studies have to be reading the same number for their conventions to be comparable field
 * by field.
 */

/**
 * `2⟨z⟩` in nm — the first-moment thickness of [model]'s profile for [chain] against a wall at
 * [height] nm.
 *
 * Exactly [height] for a box, a Beta-function multiple of it for a strong-stretching profile under
 * a pure power law, and a quadrature over the solved nodes for an SCF layer.
 */
fun GraftedLayerModel.firstMomentThickness(
    chain: GraftedChain,
    height: Double
): Double {
    require(height > 0.0) { "height must be positive, was: $height" }
    return when (this) {
        // <z> = L/2 identically for a uniform profile — no quadrature can improve on that, and
        // rounding it through one would put a discretisation error into the exact case
        is AlexanderBoxLayer -> height
        is StrongStretchingLayer -> strongStretchingFirstMoment(chain, height)
        is SelfConsistentFieldLayer -> profile(chain, height).firstMomentHeight
    }
}

/**
 * `2⟨z⟩` in nm of the layer at **its own resting height**.
 *
 * This is the definition `C-0011` already emits as `firstMomentHeight`: the moment is taken on the
 * profile confined by a wall at the model's own `L₀`. For the two trial functions that wall is
 * where the profile terminates anyway; for the SCF layer it is a threshold-defined height, and how
 * much of that threshold survives into the moment is measured rather than assumed — `T-1e`'s
 * result file carries it.
 */
fun GraftedLayerModel.restingFirstMomentThickness(chain: GraftedChain): Double =
    firstMomentThickness(chain, equilibriumHeight(chain))

/**
 * The chain length whose **resting height** is [height] nm at [graftingDensity], through whichever
 * root finder the model needs.
 *
 * The same dispatch `ScfDensityProfileStudy` makes privately, lifted here so that `T-1e` reports
 * both conventions through one code path and a difference between them cannot be an artefact of
 * two different inverters. `L₀ ∝ N` exactly for the two analytic profiles, so their fixed point
 * lands in one pass; the SCF resting height goes as roughly `N^0.5`, so it is bracketed.
 */
fun GraftedLayerModel.chainLengthAtHeight(
    peg: PegWater,
    height: Double,
    graftingDensity: Double
): Double = if (this is SelfConsistentFieldLayer) {
    chainLengthAtRestingHeight(peg, height, graftingDensity)
} else chainLengthForHeight(peg, height, graftingDensity)

/**
 * The chain length whose resting-state first-moment thickness is [thickness] nm at
 * [graftingDensity] — the inversion `CH-0010` queued and `C-0011` could only estimate.
 *
 * Bracketed in `ln N` against `ln 2⟨z⟩`, for exactly the reason
 * [SelfConsistentFieldLayer.chainLengthAtRestingHeight] brackets the force-onset inversion: the
 * relation is very nearly a straight line in log-log, while the fixed point
 * `N ← N · target/achieved` contracts by only a factor of two per pass when `2⟨z⟩ ∝ N^0.5`.
 *
 * @param seed where the bracket starts. The default is the model's own **force-onset** inversion at
 *          the same target, which is a guaranteed lower bound for any profile whose bulk sits below
 *          the height at which it first resists — and it is a number `T-1e` wants reported anyway,
 *          so passing it in avoids solving the same root twice. For a box it is the answer.
 * @param tolerance the relative width at which the `ln N` bracket is closed. The default matches
 *          the SCF's own height tolerance: every evaluation here contains a whole resting-height
 *          solve, so a tighter outer bracket would sharpen a number the inner one has already
 *          blurred.
 *
 * ## The bracket walk and the root finder share ONE residual, and they must
 *
 * Written the obvious way — the walk as `achieved(N) < thickness` and the root as
 * `ln(achieved(N)/thickness)` — this is a **sign test spelled two ways**, and at the last unit in
 * the last place the two spellings disagree: `achieved` can exceed `thickness` by one ulp while the
 * quotient rounds to `1.0` or below and its logarithm comes out `−3.3e−16`. The walk then certifies
 * a bracket whose upper endpoint the root finder reads as the *same* sign as the lower one, and
 * `bracketedRoot`'s entry `require` fires on a bracket that was just constructed for it. It is the
 * same family as `C-0031`'s *"never write a sign test as a product"*, and it is reachable at every
 * scale rather than only below `1.5e−154`: the box profile has `2⟨z⟩ = L` exactly, so its seed **is**
 * its answer and every one of its inversions lands on the tie. Everything below therefore runs on
 * one [residual] in `ln N`, evaluated at exactly the abscissae that are passed on.
 */
fun GraftedLayerModel.chainLengthForFirstMomentThickness(
    peg: PegWater,
    thickness: Double,
    graftingDensity: Double,
    seed: Double = chainLengthAtHeight(peg, thickness, graftingDensity),
    tolerance: Double = FIRST_MOMENT_TOLERANCE
): Double {
    require(thickness > 0.0) { "thickness must be positive, was: $thickness" }
    require(graftingDensity > 0.0) {
        "graftingDensity must be positive, was: $graftingDensity"
    }
    require(seed >= 1.0) { "seed must be at least one monomer, was: $seed" }
    require(tolerance > 0.0) { "tolerance must be positive, was: $tolerance" }
    val logThickness = ln(thickness)
    fun residual(logLength: Double): Double =
        ln(restingFirstMomentThickness(peg.graftedChain(exp(logLength), graftingDensity))) -
                logThickness
    val logStep = ln(LENGTH_BRACKET_STEP)
    var low = ln(seed)
    var high = low
    if (residual(high) == 0.0) return exp(high)
    var guard = 0
    // Walking outward from the force-onset answer rather than from a fixed constant keeps every
    // evaluation near the design point. The SCF's own guard rail is the saturation height, and it
    // is approached from ABOVE here — the walk is upward for any profile whose moment is short of
    // its onset height — so no evaluation lands where the layer would be a melt.
    while (residual(high) < 0.0) {
        high += logStep
        require(guard++ < LENGTH_BRACKET_LIMIT) {
            "no chain long enough reaches a first-moment thickness of $thickness nm"
        }
    }
    guard = 0
    while (residual(low) > 0.0) {
        low -= logStep
        require(low >= 0.0 && guard++ < LENGTH_BRACKET_LIMIT) {
            "no chain short enough reaches a first-moment thickness of $thickness nm"
        }
    }
    if (high == low) return exp(high)
    return exp(
        bracketedRoot(low, high, tolerance = tolerance, iterations = 60) { residual(it) }
    )
}

/**
 * `2⟨z⟩/L` of an uncompressed strong-stretching profile under a pure power-law interaction of
 * osmotic exponent [osmoticExponent] — the closed form, with no quadrature.
 *
 * The uncompressed profile is `φ(z) = φ₀(1 − z²/L²)^p` with `p = 1/(m − 1)`, because
 * `μ(φ) ∝ φ^(m−1)` inverts the parabolic potential `λ − Az²` into exactly that power, and the
 * uncompressed case is the one where `λ = AL²`. Then
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`∫₀^L z(1−z²/L²)^p dz = L²/(2(p+1))`, &nbsp;
 * `∫₀^L (1−z²/L²)^p dz = L · halfCircleMoment(p)`
 *
 * so `2⟨z⟩/L = 1/[(p+1) halfCircleMoment(p)]`. It is **3/4 exactly** for the mean-field parabola
 * (`m = 2`, `p = 1`) and **0.783596** for des Cloizeaux (`m = 9/4`, `p = 4/5`).
 *
 * The value of having it in closed form is that it is what the quadrature is checked against: the
 * outer edge behaves as `(L² − z²)^0.8`, whose derivative is infinite there, and a uniform-`z`
 * Simpson rule converges on it at less than second order.
 */
fun strongStretchingFirstMomentRatio(osmoticExponent: Double): Double {
    require(osmoticExponent > 1.0) {
        "osmoticExponent must exceed 1, was: $osmoticExponent"
    }
    val p = 1.0 / (osmoticExponent - 1.0)
    return 1.0 / ((p + 1.0) * halfCircleMoment(p))
}

/**
 * `2⟨z⟩` of a strong-stretching profile, by Simpson's rule in the `θ` of `z = h sinθ`.
 *
 * The substitution is the one [StrongStretchingLayer] uses for its own quadratures, and for the
 * same reason: it turns the outer-edge behaviour `(L²−z²)^p` into `cos^(2p+1)θ` and removes the
 * endpoint singularity that makes a uniform-`z` rule converge slowly. Both integrals are taken in
 * one pass over the same samples, so the ratio is a ratio of two quadratures of identical support
 * — which is what makes it exact for the uniform profile and self-similar for a power law.
 */
private fun StrongStretchingLayer.strongStretchingFirstMoment(
    chain: GraftedChain,
    height: Double
): Double {
    // lambda is recovered from ONE evaluation of the public profile rather than from 512 of them.
    // The strong-stretching potential is `lambda - A z^2`, so at `z = 0` the potential IS lambda
    // and `mu(phi(0))` inverts straight back to it — `exchangeChemicalPotential` and
    // `volumeFractionAtChemicalPotential` being each other's inverse is the whole content of that
    // pair. Calling `volumeFractionAt` per sample instead re-runs the coverage-conserving Newton
    // solve for lambda at every one of them, which is a factor of ~500 and turns a millisecond
    // quadrature into a second.
    val curvature = chain.parabolicCurvature(interaction.temperature)
    val apex = volumeFractionAt(chain, height, 0.0)
    require(apex > 0.0) { "the profile carries no polymer at a height of $height nm" }
    val lambda = interaction.exchangeChemicalPotential(apex)
    val step = 0.5 * PI / FIRST_MOMENT_PANELS
    var moment = 0.0
    var total = 0.0
    for (i in 0..FIRST_MOMENT_PANELS) {
        val weight = when {
            i == 0 || i == FIRST_MOMENT_PANELS -> 1.0
            i % 2 == 1 -> 4.0
            else -> 2.0
        }
        val angle = i * step
        val z = height * sin(angle)
        val measure = weight * height * cos(angle)
        val value = interaction.volumeFractionAtChemicalPotential(lambda - curvature * z * z)
        if (value > 0.0) {
            moment += measure * z * value
            total += measure * value
        }
    }
    require(total > 0.0) { "the profile carries no polymer at a height of $height nm" }
    return 2.0 * moment / total
}

/**
 * Simpson panels in `θ` for the strong-stretching first moment.
 *
 * The substituted integrand is smooth, so this is far more than the ratio needs — the closed-form
 * gate in `FirstMomentThicknessTest` passes at `1e-6` — and with `lambda` hoisted out of the loop
 * each sample is two closed-form evaluations.
 */
private const val FIRST_MOMENT_PANELS = 512

/**
 * The relative width at which the `ln N` bracket of [chainLengthForFirstMomentThickness] closes.
 *
 * Matched to `SelfConsistentFieldLayer.HEIGHT_TOLERANCE`, which every evaluation of the outer root
 * contains one of: a tighter outer bracket sharpens a number the inner solve has already blurred,
 * and `C-0073` measured that blur at exactly this size.
 */
private const val FIRST_MOMENT_TOLERANCE = 1e-6

/** How far each step of the length bracket reaches. */
private const val LENGTH_BRACKET_STEP = 1.5

private const val LENGTH_BRACKET_LIMIT = 24
