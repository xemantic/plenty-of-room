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

package com.xemantic.nano.plentyofroom.window

import com.xemantic.nano.plentyofroom.electrostatics.LayerPartitioning
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.poroelastic.CorrelationLengthScreening
import com.xemantic.nano.plentyofroom.poroelastic.FiberArrayPermeability
import com.xemantic.nano.plentyofroom.poroelastic.FreeDrainingSegments
import com.xemantic.nano.plentyofroom.poroelastic.LayerPermeability
import com.xemantic.nano.plentyofroom.poroelastic.RectangularFootprint
import com.xemantic.nano.plentyofroom.poroelastic.drainageResponse
import com.xemantic.nano.plentyofroom.poroelastic.waterViscosity
import kotlinx.serialization.Serializable
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.sqrt

/**
 * The four relations `T-2` evaluates at points its sources did not tabulate.
 *
 * Every one of them is an upstream claim's **own model, called as a library** — nothing
 * here is a new physical model, and nothing here edits another package. Where a relation
 * needed a number no claim supplies, `T-2` says so in its report rather than inventing it.
 */

/** §4(c) at one point of the window — `C-0005`'s partitioning evaluated at the layer's own `φ`. */
@Serializable
data class SaltPartitioning(
    val polymerVolumeFraction: Double,
    val magnesiumPartitionCoefficient: Double,
    val chloridePartitionCoefficient: Double,
    val saltPartitionCoefficient: Double,
    val debyeLengthRatio: Double,
    val localDebyeLengthAtTwoMillimolar: Double,
    val effectivePermittivity: Double
)

/**
 * The bulk Debye length in nm at 2 mM `MgCl₂` — `C-0005`'s derived 3.927 nm.
 *
 * **CITED from `C-0005`.** It enters only as the multiplier that turns the layer's Debye
 * *ratio* into a layer-local length, so a per-cent error in it moves no verdict.
 */
const val BULK_DEBYE_LENGTH_AT_TWO_MILLIMOLAR: Double = 3.92677096

/**
 * Evaluates `C-0005`'s ion partitioning at [polymerVolumeFraction].
 *
 * §4(c) asks how much hydrated-ion inclusion the layer gives "as a function of the layer's
 * structure". `C-0005` answered it at five labelled volume fractions; the window needs it
 * at every one of its own points, and the layer's `φ` is what varies across the window.
 * The sign of the answer is `C-0005`'s and it is the opposite of §4(c)'s premise: the
 * layer **excludes** salt, so `K < 1` and the local screening length is **longer**.
 */
fun saltPartitioning(polymerVolumeFraction: Double, peg: PegWater = PegWater()): SaltPartitioning {
    val partitioning = LayerPartitioning(
        polymerVolumeFraction = polymerVolumeFraction,
        fibreRadius = peg.kuhnSegmentDiameter / 2.0
    )
    val ratio = 1.0 / sqrt(partitioning.saltPartitionCoefficient)
    return SaltPartitioning(
        polymerVolumeFraction = polymerVolumeFraction,
        magnesiumPartitionCoefficient = partitioning.magnesiumPartitionCoefficient,
        chloridePartitionCoefficient = partitioning.chloridePartitionCoefficient,
        saltPartitionCoefficient = partitioning.saltPartitionCoefficient,
        debyeLengthRatio = ratio,
        localDebyeLengthAtTwoMillimolar = ratio * BULK_DEBYE_LENGTH_AT_TWO_MILLIMOLAR,
        effectivePermittivity = partitioning.effectivePermittivity
    )
}

/** §4(d) at one point of the window — the slowest of `C-0004`'s three permeability models. */
@Serializable
data class DrainageBound(
    val slowestPermeabilityModel: String,
    val cornerFrequency: Double,
    val marginAtOneKilohertz: Double,
    val screeningLengthOverThickness: Double
)

/**
 * Evaluates `C-0004`'s drainage corner at one design point, quoting the **slowest** of its
 * three permeability models — which is the direction a bound on a drainage *time* must err.
 *
 * `C-0004` verifies `τ ∝ 1/k_layer` exactly, so the stiffness is an argument. `T-2` passes
 * the **secant** stiffness of the solved layer, because the tile traverses the whole stroke
 * and it is the secant that carries it (`C-0010`: the secant sets the stroke, the tangent
 * sets the fluctuation).
 */
fun drainageBound(
    layerHeight: Double,
    polymerVolumeFraction: Double,
    layerStiffness: Double,
    footprint: RectangularFootprint,
    peg: PegWater = PegWater()
): DrainageBound {
    val models: List<LayerPermeability> = listOf(
        FreeDrainingSegments(
            segmentLength = peg.kuhnLength,
            segmentDiameter = peg.kuhnSegmentDiameter,
            segmentVolume = peg.kuhnSegmentVolume
        ),
        FiberArrayPermeability(fiberRadius = peg.kuhnSegmentDiameter / 2.0),
        CorrelationLengthScreening(volumetricMonomerSize = peg.volumetricMonomerSize)
    )
    val slowest = models.map { model ->
        drainageResponse(
            footprint = footprint,
            thickness = layerHeight,
            volumeFraction = polymerVolumeFraction,
            permeabilityModel = model,
            layerStiffness = layerStiffness,
            viscosity = waterViscosity()
        )
    }.minBy { it.cornerFrequency }
    return DrainageBound(
        slowestPermeabilityModel = slowest.permeabilityModel,
        cornerFrequency = slowest.cornerFrequency,
        marginAtOneKilohertz = slowest.marginAtOneKilohertz,
        screeningLengthOverThickness = slowest.screeningLengthOverThickness
    )
}

/** `C-0015`'s peak per-load-path force at a foundation stiffness it did not itself sample. */
@Serializable
data class LoadPathForce(
    val loadCase: String,
    val foundationMultiplier: Double,
    val bestLayoutForce: Double,
    val worstLayoutForce: Double,
    val insideSweptRange: Boolean
)

/**
 * Interpolates `C-0015`'s complete layout sweep onto the foundation stiffness the solved
 * layer actually has at a design point.
 *
 * `C-0015` reports the peak force scaling "roughly as `k_f^(−1/2)`" over a `×[0.25, 4]`
 * sweep about `C-0001`'s secant. Rather than assume that exponent, this interpolates
 * **log-log between the two states that bracket the requested multiplier** — which
 * reproduces the tabulated states exactly and never extrapolates silently: outside the
 * swept range [insideSweptRange] is `false` and the caller reports it as an extrapolation.
 */
fun loadPathForce(
    states: List<LayoutFoundationState>,
    loadCase: String,
    foundationMultiplier: Double
): LoadPathForce {
    require(foundationMultiplier > 0.0) {
        "foundationMultiplier must be positive, was: $foundationMultiplier"
    }
    val ordered = states.sortedBy { it.foundationMultiplier }
    val best = ordered.map { state ->
        state.foundationMultiplier to state.loadClasses.first { it.loadCase == loadCase }
    }
    val lowest = best.first().first
    val highest = best.last().first
    val inside = foundationMultiplier in lowest..highest
    val clamped = foundationMultiplier.coerceIn(lowest, highest)
    val upperIndex = best.indexOfFirst { it.first >= clamped }.coerceAtLeast(1)
    val (lowMultiplier, lowClass) = best[upperIndex - 1]
    val (highMultiplier, highClass) = best[upperIndex]
    fun interpolate(low: Double, high: Double): Double {
        val fraction = ln(foundationMultiplier / lowMultiplier) / ln(highMultiplier / lowMultiplier)
        return exp(ln(low) + fraction * (ln(high) - ln(low)))
    }
    return LoadPathForce(
        loadCase = loadCase,
        foundationMultiplier = foundationMultiplier,
        bestLayoutForce = interpolate(lowClass.jointBestForce, highClass.jointBestForce),
        worstLayoutForce = interpolate(lowClass.jointWorstForce, highClass.jointWorstForce),
        insideSweptRange = inside
    )
}

/**
 * The minimum surface-parallel tether length in nm that keeps the cable tension a stroke of
 * [stroke] induces below [allowableTension] — `C-0014`'s `L_min = δ √(S/(2A))`.
 *
 * The tether cannot let the tile descend without stretching: the chord between its fixed
 * ends grows to `√(L² + δ²)`, so `T ≈ S δ²/(2L²)`, and the design rule is **linear in the
 * stroke**. `C-0014`'s own table is reproduced from this expression as a gate-5 test.
 *
 * @param stretchModulus the duplex stretch modulus `S` in pN — 1100 pN, cited from
 *          Wang et al. (1997) via `C-0014`.
 */
fun minimumTetherLength(
    stroke: Double,
    allowableTension: Double,
    stretchModulus: Double = DUPLEX_STRETCH_MODULUS
): Double {
    require(stroke > 0.0) { "stroke must be positive, was: $stroke" }
    require(allowableTension > 0.0) {
        "allowableTension must be positive, was: $allowableTension"
    }
    require(stretchModulus > 0.0) {
        "stretchModulus must be positive, was: $stretchModulus"
    }
    return stroke * sqrt(stretchModulus / (2.0 * allowableTension))
}

/**
 * The `T-14` load class a distributed, discretely anchored output coupling belongs to.
 *
 * `C-0015` reports three: a discrete anchor at the foundation's own stiffness, the same at
 * ten times it, and a single concentrated lever. A 45-point attachment grid is the middle
 * one — stiffer than the layer under it, but not a point coupling.
 */
const val ANCHOR_LOAD_CASE: String = "anchored, k_a = 10 k_f A"

/** The duplex stretch modulus in pN. **CITED, MEASURED**, Wang et al. (1997), via `C-0014`. */
const val DUPLEX_STRETCH_MODULUS: Double = 1100.0

/**
 * `C-0009`'s out-of-plane load concentration factor at its worst value, applied by `C-0014`
 * to an in-plane load as a conservative stand-in. **CITED**; `T-15` would replace it.
 */
const val LOAD_CONCENTRATION_FACTOR: Double = 7.6

/**
 * The mean polymer volume fraction when the layer of resting height [restingHeight] and
 * resting mean fraction [restingVolumeFraction] is held at a gap of [gap].
 *
 * Mass conservation, and nothing else: `φ h = N σ v₀` is a constant of the compression, so
 * `φ(h) = φ(L₀) L₀/h`. It is what decides whether the **held** operating point — the one an
 * output coupling puts the tile at — is inside `C-0002`'s `φ ≈ 0.2` concentrated crossover,
 * which the **free** operating point is not.
 */
fun heldVolumeFraction(
    restingVolumeFraction: Double,
    restingHeight: Double,
    gap: Double
): Double {
    require(gap > 0.0) { "gap must be positive, was: $gap" }
    require(restingHeight > 0.0) { "restingHeight must be positive, was: $restingHeight" }
    require(restingVolumeFraction > 0.0) {
        "restingVolumeFraction must be positive, was: $restingVolumeFraction"
    }
    return restingVolumeFraction * restingHeight / gap
}
