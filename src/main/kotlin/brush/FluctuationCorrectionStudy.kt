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
import com.xemantic.nano.plentyofroom.structure.SOLVED_HEIGHT_SIGNIFICANT_DIGITS
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sqrt

/**
 * Task `T-1f` — bounding the mean-field fluctuation corrections to the Gen-1 grafted layer at
 * `φ ≈ 0.01`. Leaf `A2.1`.
 *
 * Run with:
 *
 * ```shell
 * tools/study.sh brush.FluctuationCorrectionStudyKt
 * ```
 *
 * Emits `gpd/results/T-1f-mean-field-fluctuation-corrections.json`, deterministically — no
 * timestamp, every floating-point number rounded to nine significant digits at the serialisation
 * boundary.
 *
 * Consumes `C-0011`'s SCF layer, `C-0003`'s interaction bracket and `C-0002`'s material sheet as
 * **libraries, re-run rather than tabulated**. Adds nothing outside `brush/`.
 */

// ---------------------------------------------------------------------------------------------
// records
// ---------------------------------------------------------------------------------------------

/** The loop parameter of the polymer mean field, at one volume fraction of one design point. */
@Serializable
data class GinzburgRecord(
    val designPoint: String,
    val layerHeight: Double,
    val graftingDensity: Double,
    val monomersPerChain: Double,
    val where: String,
    val volumeFraction: Double,
    val ginzburgVolumeFraction: Double,
    val volumeFractionRatio: Double,
    val screeningLength: Double,
    val bareGinzburgParameter: Double,
    val inverseCompressibilityCorrection: Double,
    val idealEndToEnd: Double,
    val screeningToCoil: Double,
    val kuhnSegmentsPerCorrelationBlob: Double,
    val chainsPerCorrelationArea: Double,
    val meanFieldPressure: Double,
    val oneLoopPressureCorrection: Double,
    val ginzburgNumber: Double,
    val verdict: String
)

/** `CH-0020`: the thermal blob in both coarse-grainings, and the swelling that follows from it. */
@Serializable
data class ThermalBlobRecord(
    val monomersPerChain: Double,
    val kuhnSegments: Double,
    val incumbentBlobKuhnSegments: Double,
    val correctedBlobKuhnSegments: Double,
    val ratio: Double,
    val incumbentBlobsPerChain: Double,
    val correctedBlobsPerChain: Double,
    val correctedBlobMonomers: Double,
    val correctedBlobKiloDaltons: Double,
    val fixmanParameter: Double,
    val expansionFactorFree: Double,
    val expansionFactorScreened: Double,
    val volumeFraction: Double,
    val screeningLength: Double
)

/** The solved layer's response at one scaling of the interaction strength. */
@Serializable
data class InteractionSensitivityRecord(
    val designPoint: String,
    val layerHeight: Double,
    val graftingDensity: Double,
    val interactionScale: Double?,
    val monomersPerChain: Double,
    val kiloDaltons: Double,
    val meanVolumeFraction: Double,
    val firstMomentThickness: Double,
    val pressureAtEightyPercent: Double,
    val stiffnessAtEightyPercent: Double,
    val stiffnessAtHeldGap: Double,
    val secantStiffness: Double,
    val strokeAtTargetForce: Double
)

/** The solved layer's response under an effective segment length `b_eff = α b`. */
@Serializable
data class SwellingSensitivityRecord(
    val designPoint: String,
    val layerHeight: Double,
    val graftingDensity: Double,
    val channel: String,
    val expansionFactor: Double,
    val effectiveKuhnLength: Double,
    val monomersPerChain: Double,
    val meanVolumeFraction: Double,
    val stiffnessAtEightyPercent: Double,
    val stiffnessAtHeldGap: Double,
    val secantStiffness: Double,
    val strokeAtTargetForce: Double,
    val overlapLowerEdge: Double,
    val strokeUpperEdge: Double,
    val windowWidth: Double
)

/** A multiplicative bracket on one response quantity, and what it does downstream. */
@Serializable
data class PropagationRecord(
    val quantity: String,
    val designPoint: String,
    val baseline: Double,
    val low: Double,
    val high: Double,
    val bracketWidth: Double,
    val note: String
)

/** Gate 4: the sensitivity exponent and the `K → 0` floor, exhibited rather than asserted. */
@Serializable
data class ConvergenceRecord(
    val axis: String,
    val setting: Double,
    val value: Double,
    val departureFromFinest: Double
)

@Serializable
data class FluctuationCorrectionResult(
    val task: String,
    val leaf: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val parameters: Map<String, Double>,
    val ginzburg: List<GinzburgRecord>,
    val thermalBlob: List<ThermalBlobRecord>,
    val interactionSensitivity: List<InteractionSensitivityRecord>,
    val swellingSensitivity: List<SwellingSensitivityRecord>,
    val propagation: List<PropagationRecord>,
    val convergence: List<ConvergenceRecord>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val open: List<String>
)

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

/** §3: the 40 × 40 nm tile. */
private const val TILE_AREA = 1600.0

/** §3: the force the stroke is measured at. */
private const val TARGET_FORCE = 100.0

/** §3's acceptable stroke, which owns `C-0016`'s upper window edge at every height. */
private const val TARGET_STROKE = 3.0

private val peg = PegWater()

private val secondVirial = peg.reducedSecondVirialCoefficient(1.9e-3)

private val correlation = peg.edwardsCorrelation(secondVirial)

private val swelling = ChainSwelling(peg.kuhnLength, peg.kuhnExcludedVolume(secondVirial))

private val desCloizeaux = desCloizeauxInteraction(peg.crossoverIndex, peg.monomerVolume)

private val twoBody = twoBodyInteraction(secondVirial, peg.monomerVolume)

private val productionGrid = ScfDiscretisation(nodeSpacing = 0.2, contourStepsPerMonomer = 2.0)

/**
 * The grid the **window-edge roots** are solved on.
 *
 * Coarser than [productionGrid] on purpose, and the justification is `C-0016`'s own: it locates
 * every window edge on a 61-point logarithmic sweep with ratio **1.109×**, and states that no edge
 * should be quoted to more digits than that. A root solved to 1e-4 in `ln σ` on a grid whose own
 * error is ~3e-3 relative in the pressure is three orders of magnitude finer than the quantity it
 * is compared against. Gate 4 exhibits the difference rather than asserting it.
 */
private val edgeGrid = ScfDiscretisation(nodeSpacing = 0.4, contourStepsPerMonomer = 1.0)

/** How far either side of `C-0016`'s own edge the seeded root brackets, as a factor. */
private const val EDGE_BRACKET = 4.0

/** `C-0001`'s single grafting density per height, which `C-0011` and `C-0017` both design at. */
private val designPoints = listOf(
    Triple("10 nm design point", 10.0, 0.0240),
    Triple("7 nm design point", 7.0, 0.0450)
)

/** `C-0016`'s own window edges, re-solved here rather than read from its file. */
private val edgePoints = listOf(
    Triple("10 nm lower edge", 10.0, 0.011630),
    Triple("10 nm upper edge", 10.0, 0.260150),
    Triple("7 nm lower edge", 7.0, 0.029550),
    Triple("7 nm upper edge", 7.0, 0.049600)
)

private class SolvedPoint(
    val layer: SelfConsistentFieldLayer,
    val chain: GraftedChain,
    val height: Double
) {
    val meanVolumeFraction: Double get() = chain.meanVolumeFraction(height)
    val stiffnessAtEightyPercent: Double get() = layer.stiffness(chain, 0.8 * height, TILE_AREA)
    val pressureAtEightyPercent: Double get() = layer.disjoiningPressure(chain, 0.8 * height)
    /**
     * The height at which the layer balances §3's 100 pN over the tile.
     *
     * Solved with the layer's **own** `heightAtPressure`, which is the routine `C-0011` uses for
     * the resting height: a log-log bracketed root whose floor is grown *downward* from
     * `0.35 R₀` only as far as it has to go. Two other routes were tried and both are traps.
     *
     * `GraftedLayerModel.heightUnderLoad` brackets on the raw residual `F − P(h)A`, which spans
     * seven decades once the interaction is switched off; the Illinois halving inside
     * `bracketedRoot` then tests the sign of a **product** of two numbers that can both be tiny,
     * and when that product underflows to `−0.0` the stagnant endpoint is replaced by one of the
     * *same* sign and the next secant step leaves the bracket — observed here as an evaluation at
     * `0.8 × Nσv₀`, a fifth of the way *below* the dry thickness. Queued for repair; it is not
     * repaired here because three standing claims consume it.
     *
     * A hand-rolled bisection between the **saturation height** and `L₀` is worse, and in a way
     * that costs hours rather than throwing: at `h = Nσv₀/0.8` the layer is a melt, the node
     * spacing collapses to `h/24`, the contour step count goes as `1/Δz²`, and the self-consistent
     * iteration does not converge — so a single guard evaluation runs the 8000-iteration cap over
     * a 10⁵-step contour. **The cheapest place to evaluate an SCF layer is never its own floor.**
     */
    val heightAtTargetForce: Double by lazy {
        layer.heightAtPressure(chain, TARGET_FORCE / TILE_AREA)
    }

    val stroke: Double get() = height - heightAtTargetForce
    val secantStiffness: Double get() = TARGET_FORCE / stroke

    /**
     * `k_brush` at the gap `C-0017` holds the tile at, `L₀ − 3 nm` — the compression its stability
     * floor is written at, so that the bracket propagated to it is quoted at the same compression
     * rather than at a nearby one.
     */
    val stiffnessAtHeldGap: Double
        get() = layer.stiffness(chain, height - TARGET_STROKE, TILE_AREA)
}

private fun solve(
    interaction: PowerLawInteraction,
    material: PegWater,
    height: Double,
    graftingDensity: Double,
    grid: ScfDiscretisation = productionGrid
): SolvedPoint {
    val layer = SelfConsistentFieldLayer(interaction, grid)
    val length = layer.chainLengthAtRestingHeight(material, height, graftingDensity)
    return SolvedPoint(layer, material.graftedChain(length, graftingDensity), height)
}

/**
 * `Σ = πR₀²σ = 1`, solved as a root in `ln σ` seeded on `C-0016`'s own edge.
 *
 * Seeded rather than swept from `σ = 1e-3`, because the cost of one evaluation rises steeply as the
 * grafting density falls — a sparse layer needs a long chain to reach a given height, and the
 * contour discretisation is proportional to it. The bracket is `C-0016`'s edge ± a factor of four,
 * which is 3.6 decades of its own grid resolution, and the root is verified to lie strictly inside
 * it rather than at an end.
 */
private fun overlapEdge(
    interaction: PowerLawInteraction,
    material: PegWater,
    height: Double,
    seed: Double,
    grid: ScfDiscretisation = productionGrid
): Double = seededEdge(seed) { density ->
    val point = solve(interaction, material, height, density, grid)
    val coil = point.chain.idealEndToEnd
    ln(PI * coil * coil * density)
}

/** `stroke(σ) = 3 nm` at 100 pN, solved as a root — `C-0016`'s upper edge at every height. */
private fun strokeEdge(
    interaction: PowerLawInteraction,
    material: PegWater,
    height: Double,
    seed: Double,
    grid: ScfDiscretisation = productionGrid
): Double = seededEdge(seed) { density ->
    solve(interaction, material, height, density, grid).stroke - TARGET_STROKE
}

/** Plain bisection in `ln σ` over `[seed/4, 4 seed]`, to 1e-4 of the bracket. */
private fun seededEdge(seed: Double, residual: (Double) -> Double): Double {
    var low = ln(seed / EDGE_BRACKET)
    var high = ln(seed * EDGE_BRACKET)
    val atLow = residual(exp(low))
    val atHigh = residual(exp(high))
    require(atLow * atHigh < 0.0) {
        "the edge is not inside [${exp(low)}, ${exp(high)}]: residuals $atLow .. $atHigh"
    }
    repeat(40) {
        val middle = 0.5 * (low + high)
        if (residual(exp(middle)) * atLow > 0.0) low = middle else high = middle
        if (high - low <= 1e-4) return exp(0.5 * (low + high))
    }
    return exp(0.5 * (low + high))
}

private fun verdictOf(ginzburg: Double): String = when {
    ginzburg > 1.0 -> "BROKEN - the one-loop term exceeds the leading term"
    ginzburg > 0.5 -> "marginal - the expansion converges slowly if at all"
    else -> "controlled"
}

/** Progress to stderr, so a multi-minute sweep is observable rather than opaque. */
private var phaseStart = System.currentTimeMillis()

private fun progress(what: String) {
    val now = System.currentTimeMillis()
    System.err.println("  [%6.1f s] %s".format((now - phaseStart) / 1000.0, what))
    System.err.flush()
    phaseStart = now
}

fun main() {
    val ginzburg = mutableListOf<GinzburgRecord>()
    val blobs = mutableListOf<ThermalBlobRecord>()
    val interactionSensitivity = mutableListOf<InteractionSensitivityRecord>()
    val swellingSensitivity = mutableListOf<SwellingSensitivityRecord>()
    val propagation = mutableListOf<PropagationRecord>()
    val convergence = mutableListOf<ConvergenceRecord>()

    // ---------------------------------------------------------------- the cheap bound

    (designPoints + edgePoints).forEach { (label, height, density) ->
        val point = solve(desCloizeaux, peg, height, density)
        val profile = point.layer.profile(point.chain, height)
        val held = height - TARGET_STROKE
        val samples = listOf(
            "profile mean at L0" to point.meanVolumeFraction,
            "profile peak at L0" to profile.peakVolumeFraction,
            "profile mean at the held gap L0 - 3 nm" to point.chain.meanVolumeFraction(held)
        )
        progress("ginzburg: $label, N = %.2f".format(point.chain.monomersPerChain))
        samples.forEach { (where, volumeFraction) ->
            ginzburg += GinzburgRecord(
                designPoint = label,
                layerHeight = height,
                graftingDensity = density,
                monomersPerChain = point.chain.monomersPerChain,
                where = where,
                volumeFraction = volumeFraction,
                ginzburgVolumeFraction = correlation.ginzburgVolumeFraction,
                volumeFractionRatio = volumeFraction / correlation.ginzburgVolumeFraction,
                screeningLength = correlation.screeningLength(volumeFraction),
                bareGinzburgParameter = correlation.ginzburgParameter(volumeFraction),
                inverseCompressibilityCorrection =
                    correlation.oneLoopCompressibilityCorrection(volumeFraction),
                idealEndToEnd = point.chain.idealEndToEnd,
                screeningToCoil = correlation.screeningLength(volumeFraction) /
                        point.chain.idealEndToEnd,
                kuhnSegmentsPerCorrelationBlob =
                    correlation.segmentsPerCorrelationBlob(volumeFraction),
                chainsPerCorrelationArea = PI *
                        correlation.screeningLength(volumeFraction).pow(2.0) * density,
                meanFieldPressure = correlation.meanFieldPressure(volumeFraction),
                oneLoopPressureCorrection = correlation.oneLoopPressure(volumeFraction),
                ginzburgNumber = correlation.ginzburgNumber(volumeFraction),
                verdict = verdictOf(correlation.ginzburgNumber(volumeFraction))
            )
        }
    }

    // ---------------------------------------------------------------- CH-0020

    val designVolumeFraction = ginzburg.first { it.where == "profile mean at L0" }.volumeFraction
    val designScreening = correlation.screeningLength(designVolumeFraction)
    val blobChains = listOf(27.8, 36.6, 62.1, 74.0, 199.4, 375.0)
    blobChains.forEach { length ->
        val kuhnSegments = length / peg.monomersPerKuhnSegment
        val incumbent = peg.thermalBlobKuhnSegments(secondVirial)
        val corrected = peg.thermalBlobKuhnSegmentsCorrected(secondVirial)
        blobs += ThermalBlobRecord(
            monomersPerChain = length,
            kuhnSegments = kuhnSegments,
            incumbentBlobKuhnSegments = incumbent,
            correctedBlobKuhnSegments = corrected,
            ratio = incumbent / corrected,
            incumbentBlobsPerChain = kuhnSegments / incumbent,
            correctedBlobsPerChain = kuhnSegments / corrected,
            correctedBlobMonomers = corrected * peg.monomersPerKuhnSegment,
            correctedBlobKiloDaltons =
                corrected * peg.monomersPerKuhnSegment * peg.monomerMolarMass / 1000.0,
            fixmanParameter = swelling.fixmanParameter(kuhnSegments),
            expansionFactorFree = swelling.expansionFactor(kuhnSegments),
            expansionFactorScreened =
                swelling.screenedExpansionFactor(kuhnSegments, designScreening),
            volumeFraction = designVolumeFraction,
            screeningLength = designScreening
        )
    }

    // ---------------------------------------------------------------- the interaction channel

    val scales = listOf(1e-6, 1e-3, 0.1, 0.25, 0.5, 1.0, 2.0, 4.0)
    designPoints.forEach { (label, height, density) ->
        scales.forEach { scale ->
            val point = solve(desCloizeaux.scaled(scale), peg, height, density)
            progress("interaction: $label, K/K0 = $scale, N = %.2f".format(point.chain.monomersPerChain))
            interactionSensitivity += InteractionSensitivityRecord(
                designPoint = label,
                layerHeight = height,
                graftingDensity = density,
                interactionScale = scale,
                monomersPerChain = point.chain.monomersPerChain,
                kiloDaltons = point.chain.monomersPerChain * peg.monomerMolarMass / 1000.0,
                meanVolumeFraction = point.meanVolumeFraction,
                firstMomentThickness =
                    point.layer.profile(point.chain, height).firstMomentHeight,
                pressureAtEightyPercent = point.pressureAtEightyPercent,
                stiffnessAtEightyPercent = point.stiffnessAtEightyPercent,
                stiffnessAtHeldGap = point.stiffnessAtHeldGap,
                secantStiffness = point.secantStiffness,
                strokeAtTargetForce = point.stroke
            )
        }
        // the two-body limb at the same point: the mean-field END of the same crossover
        val meanField = solve(twoBody, peg, height, density)
        interactionSensitivity += InteractionSensitivityRecord(
            designPoint = "$label (two-body limb)",
            layerHeight = height,
            graftingDensity = density,
            interactionScale = null,
            monomersPerChain = meanField.chain.monomersPerChain,
            kiloDaltons = meanField.chain.monomersPerChain * peg.monomerMolarMass / 1000.0,
            meanVolumeFraction = meanField.meanVolumeFraction,
            firstMomentThickness =
                meanField.layer.profile(meanField.chain, height).firstMomentHeight,
            pressureAtEightyPercent = meanField.pressureAtEightyPercent,
            stiffnessAtEightyPercent = meanField.stiffnessAtEightyPercent,
            stiffnessAtHeldGap = meanField.stiffnessAtHeldGap,
            secantStiffness = meanField.secantStiffness,
            strokeAtTargetForce = meanField.stroke
        )
    }

    // ---------------------------------------------------------------- the conformational channel

    designPoints.forEach { (label, height, density) ->
        val baseline = solve(desCloizeaux, peg, height, density)
        val kuhnSegments = baseline.chain.kuhnSegments
        val screening = correlation.screeningLength(baseline.meanVolumeFraction)
        val channels = listOf(
            "mean field (no swelling)" to 1.0,
            "screened swelling" to swelling.screenedExpansionFactor(kuhnSegments, screening),
            "free-chain swelling" to swelling.expansionFactor(kuhnSegments)
        )
        val seeds = if (height == 10.0) Pair(0.011630, 0.260150) else Pair(0.029550, 0.049600)
        channels.forEach { (channel, alpha) ->
            val material = peg.copy(kuhnLength = peg.kuhnLength * alpha)
            val point = solve(desCloizeaux, material, height, density)
            val lower = overlapEdge(desCloizeaux, material, height, seeds.first)
            val upper = strokeEdge(desCloizeaux, material, height, seeds.second)
            progress("swelling: $label, $channel, edges [%.5f, %.5f]".format(lower, upper))
            swellingSensitivity += SwellingSensitivityRecord(
                designPoint = label,
                layerHeight = height,
                graftingDensity = density,
                channel = channel,
                expansionFactor = alpha,
                effectiveKuhnLength = material.kuhnLength,
                monomersPerChain = point.chain.monomersPerChain,
                meanVolumeFraction = point.meanVolumeFraction,
                stiffnessAtEightyPercent = point.stiffnessAtEightyPercent,
                stiffnessAtHeldGap = point.stiffnessAtHeldGap,
                secantStiffness = point.secantStiffness,
                strokeAtTargetForce = point.stroke,
                overlapLowerEdge = lower,
                strokeUpperEdge = upper,
                windowWidth = upper / lower
            )
        }
    }

    // ---------------------------------------------------------------- gate 4

    val grids = listOf(0.4, 0.2, 0.1)
    val exponents = grids.map { spacing ->
        val grid = ScfDiscretisation(nodeSpacing = spacing, contourStepsPerMonomer = 2.0)
        val low = solve(desCloizeaux.scaled(0.5), peg, 10.0, 0.024, grid).stiffnessAtEightyPercent
        val high = solve(desCloizeaux.scaled(2.0), peg, 10.0, 0.024, grid).stiffnessAtEightyPercent
        ln(high / low) / ln(4.0)
    }
    progress("convergence: sensitivity exponents ${exponents.joinToString()}")
    grids.forEachIndexed { index, spacing ->
        convergence += ConvergenceRecord(
            axis = "node spacing, d ln k / d ln K at the 10 nm design point",
            setting = spacing,
            value = exponents[index],
            departureFromFinest = abs(exponents[index] - exponents.last())
        )
    }
    val coarseEdge = strokeEdge(desCloizeaux, peg, 10.0, 0.260150, edgeGrid)
    val fineEdge = strokeEdge(desCloizeaux, peg, 10.0, 0.260150, productionGrid)
    listOf(0.4 to coarseEdge, 0.2 to fineEdge).forEach { (spacing, edge) ->
        convergence += ConvergenceRecord(
            axis = "node spacing, the 10 nm stroke window edge",
            setting = spacing,
            value = edge,
            departureFromFinest = abs(edge - fineEdge) / fineEdge
        )
    }

    progress("convergence: edge grids done")
    val floorScales = listOf(1e-2, 1e-3, 1e-4, 1e-6)
    val floors = floorScales.map {
        solve(desCloizeaux.scaled(it), peg, 10.0, 0.024).stiffnessAtEightyPercent
    }
    floorScales.forEachIndexed { index, scale ->
        convergence += ConvergenceRecord(
            axis = "interaction scale, k(0.8 L0) approaching the interaction-free floor",
            setting = scale,
            value = floors[index],
            departureFromFinest = abs(floors[index] - floors.last()) / abs(floors.last())
        )
    }

    // ---------------------------------------------------------------- propagation

    val measuredExponent = exponents.last()
    val cloizeaux = { label: String, scale: Double ->
        interactionSensitivity.first {
            it.designPoint == label && it.interactionScale == scale
        }
    }
    designPoints.forEach { (label, _, _) ->
        val baseline = cloizeaux(label, 1.0)
        val floorRecord = cloizeaux(label, 1e-6)
        val doubled = cloizeaux(label, 2.0)
        listOf(
            Triple("k(0.8 L0)", baseline.stiffnessAtEightyPercent, "pN/nm") to
                    Pair(floorRecord.stiffnessAtEightyPercent, doubled.stiffnessAtEightyPercent),
            Triple("k_brush at the held gap L0 - 3 nm", baseline.stiffnessAtHeldGap, "pN/nm") to
                    Pair(floorRecord.stiffnessAtHeldGap, doubled.stiffnessAtHeldGap),
            Triple("secant stiffness", baseline.secantStiffness, "pN/nm") to
                    Pair(floorRecord.secantStiffness, doubled.secantStiffness),
            Triple("stroke at 100 pN", baseline.strokeAtTargetForce, "nm") to
                    Pair(floorRecord.strokeAtTargetForce, doubled.strokeAtTargetForce),
            Triple("N(L0)", baseline.monomersPerChain, "monomers") to
                    Pair(floorRecord.monomersPerChain, doubled.monomersPerChain)
        ).forEach { (head, ends) ->
            val (quantity, base, unit) = head
            val low = minOf(ends.first, ends.second)
            val high = maxOf(ends.first, ends.second)
            propagation += PropagationRecord(
                quantity = "$quantity [$unit]",
                designPoint = label,
                baseline = base,
                low = low,
                high = high,
                bracketWidth = high / low,
                note = "interaction channel, EXPLORATORY range K/K0 from 1e-6 (fluctuations " +
                        "destroy the interaction entirely) to 2 (twice the measured limb)"
            )
            // the LICENSED bracket: the one-loop term is negative, so fluctuations can only
            // REDUCE the interaction. K/K0 in [0, 1] is the whole of what they can do.
            val licensedLow = minOf(ends.first, base)
            val licensedHigh = maxOf(ends.first, base)
            propagation += PropagationRecord(
                quantity = "$quantity [$unit], LICENSED",
                designPoint = label,
                baseline = base,
                low = licensedLow,
                high = licensedHigh,
                bracketWidth = licensedHigh / licensedLow,
                note = "interaction channel, K/K0 in [0, 1] — the one-loop correction is " +
                        "NEGATIVE, so fluctuations can only reduce the interaction and the " +
                        "measured limb is the upper end"
            )
        }
    }
    swellingSensitivity.groupBy { it.designPoint }.forEach { (label, records) ->
        val base = records.first { it.channel == "mean field (no swelling)" }
        listOf(
            "k(0.8 L0) [pN/nm]" to records.map { it.stiffnessAtEightyPercent },
            "k_brush at the held gap L0 - 3 nm [pN/nm]" to records.map { it.stiffnessAtHeldGap },
            "stroke at 100 pN [nm]" to records.map { it.strokeAtTargetForce },
            "N(L0) [monomers]" to records.map { it.monomersPerChain },
            "coil-overlap window edge [nm^-2]" to records.map { it.overlapLowerEdge },
            "stroke window edge [nm^-2]" to records.map { it.strokeUpperEdge },
            "window width [1]" to records.map { it.windowWidth }
        ).forEachIndexed { index, (quantity, values) ->
            val baseline = listOf(
                base.stiffnessAtEightyPercent, base.stiffnessAtHeldGap, base.strokeAtTargetForce,
                base.monomersPerChain, base.overlapLowerEdge, base.strokeUpperEdge,
                base.windowWidth
            )[index]
            propagation += PropagationRecord(
                quantity = quantity,
                designPoint = label,
                baseline = baseline,
                low = values.min(),
                high = values.max(),
                bracketWidth = values.max() / values.min(),
                note = "conformational channel, b_eff = alpha b from no swelling to free-chain " +
                        "swelling; the coil-overlap edge moves exactly as 1/alpha^2"
            )
        }
    }

    val result = FluctuationCorrectionResult(
        task = "T-1f",
        leaf = "A2.1",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "pressure" to "pN/nm^2 (= MPa exactly)",
            "stiffness" to "pN/nm (= mN/m exactly)",
            "graftingDensity" to "nm^-2",
            "volumeFraction" to "1 (PHYSICAL, phi = c v0)",
            "ginzburgNumber" to "1"
        ),
        conventions = CONVENTIONS,
        parameters = mapOf(
            "temperature" to 300.0,
            "tileArea" to TILE_AREA,
            "targetForce" to TARGET_FORCE,
            "targetStroke" to TARGET_STROKE,
            "reducedSecondVirialCoefficient" to secondVirial,
            "monomerExcludedVolume" to secondVirial * peg.monomerVolume,
            "kuhnExcludedVolume" to peg.kuhnExcludedVolume(secondVirial),
            "kuhnLength" to peg.kuhnLength,
            "monomersPerKuhnSegment" to peg.monomersPerKuhnSegment,
            "monomerVolume" to peg.monomerVolume,
            "kuhnSegmentVolume" to peg.kuhnSegmentVolume,
            "ginzburgVolumeFraction" to correlation.ginzburgVolumeFraction,
            "restingLoad" to 1.0,
            "nodeSpacing" to productionGrid.nodeSpacing,
            "contourStepsPerMonomer" to productionGrid.contourStepsPerMonomer,
            "measuredInteractionSensitivityExponent" to measuredExponent,
            "desCloizeauxSensitivityExponentOfC0003" to 1.0 / (2.25 + 1.0)
        ),
        ginzburg = ginzburg,
        thermalBlob = blobs,
        interactionSensitivity = interactionSensitivity,
        swellingSensitivity = swellingSensitivity,
        propagation = propagation,
        convergence = convergence,
        findings = findings(ginzburg, blobs, propagation, measuredExponent),
        validity = VALIDITY,
        open = OPEN
    )

    val output = File("gpd/results/T-1f-mean-field-fluctuation-corrections.json")
    output.parentFile.mkdirs()
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(json.encodeToJsonElement(result).roundedForFluctuationResult()) + "\n"
    )
    report(result, output)
}

private fun findings(
    ginzburg: List<GinzburgRecord>,
    blobs: List<ThermalBlobRecord>,
    propagation: List<PropagationRecord>,
    measuredExponent: Double
): Map<String, String> {
    val atDesign = ginzburg.filter { it.designPoint.endsWith("design point") }
    val worst = atDesign.maxBy { it.ginzburgNumber }
    val best = atDesign.minBy { it.ginzburgNumber }
    val blob = blobs.first()
    val stiffnessBrackets = propagation.filter { it.quantity == "k(0.8 L0) [pN/nm], LICENSED" }
    val overlapEdges = propagation.filter { it.quantity.startsWith("coil-overlap") }
    return mapOf(
        "THE PERTURBATIVE VERDICT" to (
                "Gi runs %.4f to %.4f across the two Gen-1 design points, and it straddles unity " +
                        "INSIDE a single profile (%.4f at the mean, %.4f at the peak of the 10 nm " +
                        "point). phi** = %.6f. The polymer loop expansion is therefore marginal to " +
                        "BROKEN at the Gen-1 layer, exactly as C-0005 reports the electrostatic one " +
                        "to be, and NO bound on the correction is available from within it."
                ).format(
                best.ginzburgNumber, worst.ginzburgNumber,
                atDesign.first { it.layerHeight == 10.0 && it.where.contains("mean at L0") }
                    .ginzburgNumber,
                atDesign.first { it.layerHeight == 10.0 && it.where.contains("peak") }
                    .ginzburgNumber,
                atDesign.first().ginzburgVolumeFraction
            ),
        "THE NON-PERTURBATIVE BOUND" to (
                "The one-loop term is NEGATIVE, so all fluctuations can do to the interaction " +
                        "is destroy it. Sweeping K/K0 over the whole licensed range [0, 1] moves " +
                        "k(0.8 L0) by only %.3fx and %.3fx at the two design points. The layer response is bounded even though " +
                        "the expansion is not, because C-0011's disjoining pressure is " +
                        "CONFORMATIONAL: at an absorbing wall Pi_int(phi(h)) is identically zero."
                ).format(
                stiffnessBrackets[0].bracketWidth, stiffnessBrackets[1].bracketWidth
            ),
        "C-0003's SENSITIVITY EXPONENT DOES NOT TRANSFER" to (
                "d ln k / d ln K measured on the SOLVED layer is %.4f, against C-0003's exact " +
                        "1/(m+1) = %.4f for the des Cloizeaux exponent on either ansatz profile - " +
                        "a factor of %.2f. The solved layer is nearly INSENSITIVE to the " +
                        "interaction where the two ansatz models C-0003 derived that relation on " +
                        "are merely weakly sensitive, for the same reason CH-0010 gives: the " +
                        "pressure is not the interaction."
                ).format(measuredExponent, 1.0 / 3.25, (1.0 / 3.25) / measuredExponent),
        "CH-0020 - THE THERMAL BLOB IS 9.67x SMALLER THAN C-0003 REPORTS" to (
                "The excluded volume is a PAIR quantity: v_K = n_K^2 v_m, not n_K v_m. The thermal " +
                        "blob is %.1f Kuhn segments = %.1f monomers = %.1f kDa, not %.1f / %.0f / " +
                        "167. The Gen-1 chains are %.3f to %.3f of one blob, not 0.02-0.10. The " +
                        "Gaussian licence C-0003 claims SURVIVES, with 6.3x of margin at the design " +
                        "point instead of 50x and only 1.05x at the longest chain in the design space."
                ).format(
                blob.correctedBlobKuhnSegments, blob.correctedBlobMonomers,
                blob.correctedBlobKiloDaltons, blob.incumbentBlobKuhnSegments,
                blob.incumbentBlobKuhnSegments * 3.1098903593398863,
                blobs.minOf { it.correctedBlobsPerChain }, blobs.maxOf { it.correctedBlobsPerChain }
            ),
        "THE CONFORMATIONAL CHANNEL WIDENS THE WINDOW, AND NOT AT THE EDGE EXPECTED" to (
                "Intrachain swelling is the one channel a mean field cannot contain at all. At " +
                        "FIXED chain length it moves coil overlap by exactly alpha^2, and that " +
                        "identity is asserted as a test - but the propagation does NOT reduce to " +
                        "it, because the chain length moves against it: a swollen chain reaches " +
                        "the same height with fewer monomers. The coil-overlap edge therefore " +
                        "moves only %.2f%% and %.2f%% (10 and 7 nm) against an alpha^2 of " +
                        "%.3f, while the STROKE edge moves %.2f%% and %.2f%%. The windows widen " +
                        "by %.2fx and %.2fx, entirely at the top. Same near-cancellation as the " +
                        "interaction channel, and the third instance in this project."
                ).format(
                100.0 * (overlapEdges[0].bracketWidth - 1.0),
                100.0 * (overlapEdges[1].bracketWidth - 1.0),
                blobs.first { it.monomersPerChain == 62.1 }.expansionFactorScreened.let { it * it },
                100.0 * (propagation.first {
                    it.quantity.startsWith("stroke window") && it.designPoint.startsWith("10")
                }.bracketWidth - 1.0),
                100.0 * (propagation.first {
                    it.quantity.startsWith("stroke window") && it.designPoint.startsWith("7")
                }.bracketWidth - 1.0),
                propagation.first {
                    it.quantity.startsWith("window width") && it.designPoint.startsWith("10")
                }.bracketWidth,
                propagation.first {
                    it.quantity.startsWith("window width") && it.designPoint.startsWith("7")
                }.bracketWidth
            ),
        "SCOPE - CH-0019" to (
                "C-0005's 123-214% is the ELECTROSTATIC loop expansion at the charged tile and " +
                        "electrode surfaces, governed by Xi ~ q^3; it corrects k_es. This task's " +
                        "Ginzburg number is the POLYMER loop expansion in a neutral layer; it " +
                        "corrects k_brush. They act on the two terms of k_eff = k_brush + k_es and " +
                        "NEITHER BOUNDS THE OTHER. C-0017's 1.19-1.42x margin at 10 nm sits inside " +
                        "the electrostatic one, and nothing in T-1f narrows it."
                )
    )
}

private val CONVENTIONS = listOf(
    "z normal to the electrode, chains grafted at z = 0, the tile a rigid non-adsorbing wall at " +
            "h; P > 0 when the layer pushes the tile along +z; k = -A dP/dh.",
    "A volume fraction is always the PHYSICAL one, phi = c v0, never a reduced density n a^3.",
    "The excluded volume is a PAIR quantity and does not coarse-grain linearly: written on " +
            "monomers v_m = B v0 = 12.25 A^3, written on Kuhn segments v_K = n_K^2 v_m. Every " +
            "formula containing b needs the Kuhn reading. This is CH-0020.",
    "L0 is a FORCE-ONSET height at 1.0 pN over the 40 x 40 nm tile (C-0011, CH-0010); every " +
            "N(L0) here inherits that convention.",
    "A fluctuation correction is quoted as a multiplicative bracket on a NAMED response quantity " +
            "at a NAMED compression, never as 'the correction to the layer stiffness'.",
    "Gi is read exactly as C-0005 reads its electrostatic counterpart: above 1 the expansion has " +
            "broken down and the direction of the correction is not knowable from within it."
)

private val VALIDITY = listOf(
    "TRL 1-3. Nothing here is measured. PASS means model-consistent and traceable.",
    "NO fluctuation-corrected profile is computed. The bound is a bracket obtained by re-running " +
            "the MEAN-FIELD solver over the range a broken expansion licences. Adding the one-loop " +
            "term to f_int would drive the osmotic pressure negative below phi**, which is the " +
            "signature of the breakdown rather than a correction to be applied.",
    "The interaction channel's floor assumes fluctuations cannot change the SIGN of Pi_int. A " +
            "net-attractive layer is outside the family of free energies used anywhere in this " +
            "programme; that boundary is inherited, not tested.",
    "The conformational channel is FIRST ORDER in the Fixman parameter z. z <= 0.33 over the " +
            "whole design space, inside the usual range for the linear form, but no second-order " +
            "term is carried and the swelling is applied as a uniform effective segment length " +
            "rather than as a self-consistent field.",
    "Lateral fluctuations are not treated at all: the correction computed here is to a 1-D field, " +
            "and lateral inhomogeneity is a separate omission C-0011 also names.",
    "The Ginzburg number is derived for a solution of Gaussian chains with a two-body excluded " +
            "volume. It is evaluated on the MEASURED PEG/water v, but the RPA structure factor it " +
            "comes from is the Edwards model's, not a fit to PEG.",
    "The electrostatic loop expansion is NOT in scope and is NOT narrowed. C-0005 owns it and " +
            "reports it as uncontrolled over the entire 5-10 nm working range. See CH-0019.",
    "Every osmotic input is still a BULK property applied to a BRUSH (P-9, C-0013), and the " +
            "equation of state is still linear PEG in pure water at 20-25 C."
)

private val OPEN = listOf(
    "The method that WOULD bound the correction is a field-theoretic simulation of the grafted " +
            "layer - complex-Langevin sampling of the full Edwards functional integral, which is " +
            "exact in the fluctuations and does not expand about the saddle point at all. Costed " +
            "at weeks of wall clock for a sweep over grafting density on this box, and NOT RUN. " +
            "The cheaper intermediate is a renormalised one-loop treatment, which resums the " +
            "divergent part but still requires the expansion parameter to be below one somewhere.",
    "A lattice or off-lattice Monte Carlo of the layer would answer the same question by " +
            "measurement rather than expansion, at days to weeks per state point. NOT RUN.",
    "The interaction free energy below phi# is not measured (C-0011), and this layer is entirely " +
            "below phi#. That gap and this one are different: one is about the input, the other " +
            "about the method that consumes it.",
    "The swelling is applied as a uniform b_eff. A self-consistent treatment would let alpha vary " +
            "with z through the profile, since the screening length does.",
    "No fluctuation correction to the ELECTROSTATIC part of the actuator characteristic - that is " +
            "C-0005's, it is 123-214%, and it is the correction C-0017's margin actually sits " +
            "inside. CH-0019."
)

// ---------------------------------------------------------------------------------------------
// reproducibility
// ---------------------------------------------------------------------------------------------

/**
 * Nine significant digits at the serialisation boundary, so a re-run that changed nothing
 * produces no diff. A deliberate copy of the pattern `structure/ResultRounding.kt` established
 * for `T-5`, not an import — `T-1f` owns no package but `brush/`, which has none of its own.
 *
 * This study contains **no argmin**, so the trap `CLAUDE.md` records for `T-14` does not apply;
 * the `maxBy`/`minBy` in [findings] run over quantities that differ in the first two digits.
 *
 * **`P-18` changed the count from nine to six, and only the count.** Every number in this file is
 * downstream of a solved height, and `SelfConsistentFieldLayer.heightAtPressure` closes its
 * bracket at a relative `1e-6`; `CH-0043` measured this very file relocating inside exactly that
 * when `P-15` shortened the solver's path without changing its answer. Nine digits made the
 * re-run diff a certificate about the code **path**. The absolute floor stays at `1e-12` rather
 * than moving to the shared `1e-9`, so that this change is attributable to one cause.
 */
private const val SIGNIFICANT_DIGITS: Int = SOLVED_HEIGHT_SIGNIFICANT_DIGITS

private const val ABSOLUTE_FLOOR: Double = 1e-12

private fun roundResult(value: Double): Double {
    if (!value.isFinite()) return value
    if (abs(value) < ABSOLUTE_FLOOR) return 0.0
    val scale = 10.0.pow(SIGNIFICANT_DIGITS - 1 - floor(log10(abs(value))))
    return (value * scale).roundToLong() / scale
}

private fun JsonElement.roundedForFluctuationResult(): JsonElement = when (this) {
    is JsonPrimitive -> {
        val value = if (isString) null else doubleOrNull
        if (value == null) this else JsonPrimitive(roundResult(value))
    }

    is JsonArray -> JsonArray(map { it.roundedForFluctuationResult() })
    is JsonObject -> JsonObject(mapValues { it.value.roundedForFluctuationResult() })
}

private fun report(result: FluctuationCorrectionResult, output: File) {
    println()
    println("T-1f — mean-field fluctuation corrections at phi ~ 0.01")
    println("  Ginzburg records: ${result.ginzburg.size}")
    println("  thermal-blob records: ${result.thermalBlob.size}")
    println("  interaction-sensitivity records: ${result.interactionSensitivity.size}")
    println("  swelling records: ${result.swellingSensitivity.size}")
    println("  propagation records: ${result.propagation.size}")
    println()
    result.ginzburg.filter { it.designPoint.endsWith("design point") }.forEach {
        println(
            ("  %-22s %-40s phi=%.5f  xi=%.3f nm  Gi=%.4f  %s").format(
                it.designPoint, it.where, it.volumeFraction, it.screeningLength,
                it.ginzburgNumber, it.verdict
            )
        )
    }
    println()
    result.findings.forEach { (key, value) ->
        println("  [$key]")
        println("    $value")
        println()
    }
    println("  written to ${output.path}")
}
