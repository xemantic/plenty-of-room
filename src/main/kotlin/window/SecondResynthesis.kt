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

import com.xemantic.nano.plentyofroom.anchoring.CoupledJointFlexure
import com.xemantic.nano.plentyofroom.anchoring.FlexureOrientation
import com.xemantic.nano.plentyofroom.anchoring.StandoffBase
import com.xemantic.nano.plentyofroom.anchoring.coupledFlexureSpan
import com.xemantic.nano.plentyofroom.anchoring.standoffTipFlexibility
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.io.File
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.roundToInt

/**
 * The relations `T-118` re-runs `C-0027`'s window and verdicts through, against iterations 5–7.
 *
 * Two of them are new physics rather than transfers, and both exist because a *synthesis* is the
 * only place they can be written:
 *
 * 1. [strokeCeilingsAcrossTheWindow] puts `C-0050`'s kinematic and validity stroke ceilings —
 *    derived there on `C-0003`'s six trial-function models at one grafting density per height —
 *    onto the **solved** layer the window is actually drawn on, at all 61 grid points. `φ` is
 *    `Nσv₀/h` identically, and `T-1d` emits it, so both ceilings cost nothing.
 * 2. [foldTangentIncrement] composes the **three** corrections that now stand against `C-0018`'s
 *    pull-in margin. At `C-0018`'s own fold the baseline coupled tangent vanishes by
 *    construction, `k_c + k_brush + k_es = 0`, so every perturbation enters as an increment and
 *    the *sign* of the sum is the direction the fold moves. `C-0033` carried the first two;
 *    nothing has ever carried the third beside them.
 */

/** §3's desired stroke in nm — quoted, never adopted (`C-0050`). */
const val DESIRED_STROKE_NM: Double = 10.0

/** `C-0006`/`CH-0029`'s per-path unzip allowable in pN. */
const val UNZIP_ALLOWABLE_PN: Double = 10.0

/** `C-0023`'s declared linearity tolerance on the placement discharge — `C-0049`'s `1.2`. */
const val DECLARED_CEILING_FACTOR: Double = 1.2

/** `C-0030`'s recommended standoff length in nm. */
const val STANDOFF_LENGTH_NM: Double = 8.0

// --- the two sigma-resolved candidates iterations 5-7 produced ------------------------------

/**
 * `C-0050`'s **kinematic** stroke ceiling in nm: the layer cannot be compressed past its own dry
 * thickness, and the dry thickness is `Nσv₀ = φ L₀` identically.
 *
 * Homogeneous of degree one in the height at fixed volume fraction, which is gate 1.
 */
fun kinematicStrokeCeiling(layerHeight: Double, meanVolumeFraction: Double): Double {
    require(layerHeight > 0.0) { "layerHeight must be positive, was: $layerHeight" }
    require(meanVolumeFraction in 0.0..1.0) {
        "meanVolumeFraction must be a fraction, was: $meanVolumeFraction"
    }
    return layerHeight * (1.0 - meanVolumeFraction)
}

/**
 * `C-0002`/`C-0036`'s **validity** stroke ceiling in nm, or `null` where the layer already sits
 * past the crossover at rest and the ceiling does not exist at all.
 *
 * `C-0050` records exactly this `null` at the top of `C-0027`'s 10 nm window **on `C-0003`'s
 * models**; whether it also happens on the solved layer is what [crossoverLicenceChecks] asks.
 */
fun validityStrokeCeiling(
    layerHeight: Double,
    meanVolumeFraction: Double,
    crossoverFraction: Double
): Double? {
    require(layerHeight > 0.0) { "layerHeight must be positive, was: $layerHeight" }
    require(meanVolumeFraction in 0.0..1.0) {
        "meanVolumeFraction must be a fraction, was: $meanVolumeFraction"
    }
    require(crossoverFraction > 0.0 && crossoverFraction <= 1.0) {
        "crossoverFraction must be in (0, 1], was: $crossoverFraction"
    }
    if (meanVolumeFraction > crossoverFraction) return null
    return layerHeight * (1.0 - meanVolumeFraction / crossoverFraction)
}

/** The layer's mean volume fraction after a compression of [stroke] — polymer volume is conserved. */
fun compressedVolumeFraction(
    meanVolumeFraction: Double,
    layerHeight: Double,
    stroke: Double
): Double {
    require(layerHeight > 0.0) { "layerHeight must be positive, was: $layerHeight" }
    require(stroke >= 0.0) { "stroke must not be negative, was: $stroke" }
    require(stroke < layerHeight) {
        "stroke must be shorter than the layer: s = $stroke against L0 = $layerHeight"
    }
    return meanVolumeFraction * layerHeight / (layerHeight - stroke)
}

/**
 * `C-0049`'s per-path secant ceiling in pN/nm: a bound on a **force** divided by the stroke.
 *
 * `perPathSecantCeiling(a, n, s) · s = n·a` identically — the same `F = k σ` identity as
 * `C-0023`'s, one power of the **stroke** apart instead of one power of a position bound.
 */
fun perPathSecantCeiling(allowable: Double, pathCount: Int, stroke: Double): Double {
    require(allowable > 0.0) { "allowable must be positive, was: $allowable" }
    require(pathCount > 0) { "pathCount must be positive, was: $pathCount" }
    require(stroke > 0.0) { "stroke must be positive, was: $stroke" }
    return pathCount * allowable / stroke
}

/** `C-0049`'s reading of `C-0023`'s declared ceiling: `1.2 ×` the mandate, which carries a stroke. */
fun declaredComplianceCeilingFromMandate(force: Double, stroke: Double): Double {
    require(force > 0.0) { "force must be positive, was: $force" }
    require(stroke > 0.0) { "stroke must be positive, was: $stroke" }
    return DECLARED_CEILING_FACTOR * force / stroke
}

// --- the three-channel fold tangent -----------------------------------------------------------

/** The three channels' contributions to the coupled tangent at `C-0018`'s own fold, in pN/nm. */
@Serializable
data class ChannelIncrement(
    /** `C-0033`: `|F_es| d ln μ/dh`, strictly positive because the gradient is. */
    val collar: Double,
    /** `C-0019`: `k_brush(m − 1)`, negative because the one-loop correction softens the layer. */
    val fluctuation: Double,
    /** `C-0032`/`C-0030`: `k_c(s_fold) − k_c,mandate`, negative for a strain-softening element. */
    val softening: Double,
    val total: Double
)

/**
 * The increment to the coupled tangent at `C-0018`'s **own** fold under all three corrections.
 *
 * At that fold the baseline tangent is zero by construction — `k_c + k_brush + k_es = 0` is what
 * located it — so the perturbed tangent **is** the increment, exactly and not to first order in
 * anything. The force is pinned by the balance, so a multiplier on its *level* reaches `k_es`
 * only through the decay rate, `1/ℓ → 1/ℓ − d ln μ/dh` (`CH-0035`, measured by `C-0033`).
 *
 * A **positive** total means the equilibrium path is still ascending at the old fold, so the fold
 * moves to a **deeper** stroke and the pull-in margin rises. A negative total means it moves
 * **shallower**, toward and possibly through §3's own 3 nm target.
 */
fun foldTangentIncrement(
    electrostaticForce: Double,
    collarLogGradient: Double,
    brushStiffnessAtFold: Double,
    brushMultiplier: Double,
    couplingTangentAtFold: Double,
    mandatedStiffness: Double
): ChannelIncrement {
    require(electrostaticForce > 0.0) {
        "electrostaticForce must be positive, was: $electrostaticForce"
    }
    require(brushStiffnessAtFold >= 0.0) {
        "brushStiffnessAtFold must not be negative, was: $brushStiffnessAtFold"
    }
    require(brushMultiplier > 0.0) { "brushMultiplier must be positive, was: $brushMultiplier" }
    require(couplingTangentAtFold >= 0.0) {
        "couplingTangentAtFold must not be negative, was: $couplingTangentAtFold"
    }
    require(mandatedStiffness > 0.0) {
        "mandatedStiffness must be positive, was: $mandatedStiffness"
    }
    val collar = electrostaticForce * collarLogGradient
    val fluctuation = brushStiffnessAtFold * (brushMultiplier - 1.0)
    val softening = couplingTangentAtFold - mandatedStiffness
    return ChannelIncrement(collar, fluctuation, softening, collar + fluctuation + softening)
}

// --- C-0030's realised element, re-run as a library rather than tabulated ---------------------

/** `C-0030`'s coupled-standoff flexure, placed on §3's acceptable clause at [pathCount] paths. */
class RealisedCouplingLaw(val pathCount: Int, val span: Double, private val beam: CoupledJointFlexure) {

    /** The assembled tangent `dR/ds` in pN/nm at an unsigned [stroke]. */
    fun assembledTangent(stroke: Double): Double =
        pathCount * beam.strokeTangentStiffness(stroke, FlexureOrientation.FAVOURABLE)

    /** The assembled secant `R(s)/s` in pN/nm at an unsigned [stroke]. */
    fun assembledSecant(stroke: Double): Double =
        pathCount * beam.strokeSecantStiffness(stroke, FlexureOrientation.FAVOURABLE)
}

/**
 * `C-0030`'s recommended element — favourable mounting, 8 nm standoff, two-crossover base —
 * placed at §3's mandate over §3's acceptable stroke, re-derived here rather than transcribed.
 */
fun realisedCouplingLaw(pathCount: Int): RealisedCouplingLaw {
    val ei = Gen1Tile.DUPLEX_BENDING_RIGIDITY
    val flexibility = standoffTipFlexibility(
        ei, STANDOFF_LENGTH_NM, StandoffBase.crossovers(2).rotationalStiffness
    )
    val span = coupledFlexureSpan(
        ei, flexibility, pathCount, MANDATED_COUPLING_STIFFNESS, ACCEPTABLE_STROKE_NM,
        FlexureOrientation.FAVOURABLE
    )
    return RealisedCouplingLaw(pathCount, span, CoupledJointFlexure(ei, span, flexibility))
}

// --- the grid, and what a movement on it means ------------------------------------------------

/**
 * The signed number of grid steps between two grafting densities on the window's own grid.
 *
 * A movement smaller than one grid ratio is **zero steps**, and that is reported as *"does not
 * move at this resolution"* rather than as *"does not move"* — `C-0027`'s declared falsifier 3,
 * which fired at three of its four non-empty edges.
 */
fun edgeMovementInGridSteps(from: Double, to: Double, grid: List<Double>): Int {
    require(grid.size >= 2) { "the grid must have at least two points" }
    require(from > 0.0 && to > 0.0) { "grafting densities must be positive" }
    fun nearest(value: Double): Int =
        grid.indices.minBy { abs(ln(grid[it] / value)) }
    return nearest(to) - nearest(from)
}

// --- the sigma-resolved sweep -----------------------------------------------------------------

/** One `(height, σ, crossover reading)` evaluation of `C-0050`'s ceilings on the SOLVED layer. */
@Serializable
data class StrokeCeilingRecord(
    val layerHeight: Double,
    val graftingDensity: Double,
    val crossoverReading: String,
    val crossoverFraction: Double,
    val meanVolumeFraction: Double,
    val kinematicCeiling: Double,
    val validityCeiling: Double? = null,
    val volumeFractionAtAcceptableStroke: Double,
    val acceptableStrokeInsideKinematic: Boolean,
    val acceptableStrokeInsideValidity: Boolean,
    val desiredStrokeInsideKinematic: Boolean
)

/**
 * `C-0050`'s two ceilings, evaluated on `C-0011`'s solved layer at every point of the window's
 * own grid and at every crossover reading `C-0036` leaves standing.
 *
 * The volume fraction taken is the **largest** of the three solved interaction laws, which is the
 * conservative reading of a ceiling: a denser layer has less room to be compressed.
 */
fun strokeCeilingsAcrossTheWindow(
    inputs: ResynthesisInputs,
    crossoverFractions: List<CrossoverReading>
): List<StrokeCeilingRecord> =
    listOf(5.0, 7.0, 10.0).flatMap { height ->
        inputs.scf.designPoints.filter { it.layerHeight == height }
            .sortedBy { it.graftingDensity }
            .flatMap { point ->
                val phi = point.solved.maxOf { it.meanVolumeFraction }
                crossoverFractions.map { reading ->
                    val kinematic = kinematicStrokeCeiling(height, phi)
                    val validity = validityStrokeCeiling(height, phi, reading.fraction)
                    StrokeCeilingRecord(
                        layerHeight = height,
                        graftingDensity = point.graftingDensity,
                        crossoverReading = reading.name,
                        crossoverFraction = reading.fraction,
                        meanVolumeFraction = phi,
                        kinematicCeiling = kinematic,
                        validityCeiling = validity,
                        volumeFractionAtAcceptableStroke =
                            compressedVolumeFraction(phi, height, ACCEPTABLE_STROKE_NM),
                        acceptableStrokeInsideKinematic = kinematic >= ACCEPTABLE_STROKE_NM,
                        acceptableStrokeInsideValidity =
                            validity != null && validity >= ACCEPTABLE_STROKE_NM,
                        desiredStrokeInsideKinematic = kinematic >= DESIRED_STROKE_NM
                    )
                }
            }
    }

/** One reading of `C-0002`'s upper crossover — `C-0036` makes it a one-parameter family. */
@Serializable
data class CrossoverReading(val name: String, val fraction: Double, val source: String)

// --- the licence check at the shared design point ----------------------------------------------

/** Whether `C-0050`'s ceiling verdicts transfer onto the layer the window is drawn on. */
@Serializable
data class CrossoverLicence(
    val layerHeight: Double,
    val upperEdgeGraftingDensity: Double,
    val solvedVolumeFractionAtUpperEdge: Double,
    val trialFunctionVolumeFractionLow: Double,
    val trialFunctionVolumeFractionHigh: Double,
    val ratioHigh: Double,
    val crossoverFraction: Double,
    val solvedCeilingExists: Boolean,
    val trialFunctionCeilingsMissing: Int,
    val trialFunctionModels: Int,
    val licensed: Boolean,
    val statement: String
)

/**
 * `C-0050`'s validity ceiling, re-read at the design point the **window** puts its upper edge at.
 *
 * `CLAUDE.md`: *an upstream bracket upheld at one design point is not upheld at all of them.*
 * `C-0016`'s own falsifier 3 fired at 5 nm and `C-0027` carried it forward; nobody has checked
 * the *upper edge* of the 10 nm window, which is where `C-0050` reports the ceiling as absent.
 */
fun crossoverLicenceChecks(
    inputs: ResynthesisInputs,
    second: SecondResynthesisInputs
): List<CrossoverLicence> =
    second.publishedWindows.filter { it.corrections == "T-25 re-synthesis" && !it.empty }
        .mapNotNull { window ->
            val height = window.layerHeight
            val edge = window.highestGraftingDensity!!
            val point = inputs.scf.designPoints.filter { it.layerHeight == height }
                .minBy { abs(ln(it.graftingDensity / edge)) }
            val solvedPhi = point.solved.maxOf { it.meanVolumeFraction }
            val trial = second.reachRecords.filter {
                it.nominalHeight == height && abs(ln(it.graftingDensity / edge)) < 0.05
            }
            // T-108 swept C-0027's 10 nm window and §3's three nominal densities; where it
            // has no record at a window EDGE the licence is not checkable and is reported as
            // unchecked by [uncheckedLicenceHeights] rather than asserted on a substitute
            if (trial.isEmpty()) return@mapNotNull null
            val crossover = second.crossoverFractions.first().fraction
            val solvedExists = validityStrokeCeiling(height, solvedPhi, crossover) != null
            val missing = trial.count { it.validityCeiling == null }
            val licensed = solvedExists == (missing == 0)
            CrossoverLicence(
                layerHeight = height,
                upperEdgeGraftingDensity = point.graftingDensity,
                solvedVolumeFractionAtUpperEdge = solvedPhi,
                trialFunctionVolumeFractionLow = trial.minOf { it.restingVolumeFraction },
                trialFunctionVolumeFractionHigh = trial.maxOf { it.restingVolumeFraction },
                ratioHigh = trial.maxOf { it.restingVolumeFraction } / solvedPhi,
                crossoverFraction = crossover,
                solvedCeilingExists = solvedExists,
                trialFunctionCeilingsMissing = missing,
                trialFunctionModels = trial.size,
                licensed = licensed,
                statement = "L0 = $height nm, upper edge sigma = " +
                        "${"%.5g".format(point.graftingDensity)} nm^-2: the SOLVED layer sits at " +
                        "phi = ${"%.4f".format(solvedPhi)} while C-0003's six trial-function " +
                        "models sit at ${"%.4f".format(trial.minOf { it.restingVolumeFraction })}-" +
                        "${"%.4f".format(trial.maxOf { it.restingVolumeFraction })}, a factor of " +
                        "${"%.2f".format(trial.maxOf { it.restingVolumeFraction } / solvedPhi)}x — " +
                        if (licensed) "and both agree about whether the validity ceiling exists"
                        else "and they DISAGREE about whether the validity ceiling exists at all " +
                                "($missing of ${trial.size} trial-function models have none, the " +
                                "solved layer has one). C-0050's bound 3 is NOT licensed at this " +
                                "design point; its bound 2 is, and bound 2 settles its question."
            )
        }

/** The heights whose upper edge `T-108` never sampled, so the licence cannot be checked there. */
fun uncheckedLicenceHeights(
    inputs: ResynthesisInputs,
    second: SecondResynthesisInputs
): List<Double> {
    val checked = crossoverLicenceChecks(inputs, second).map { it.layerHeight }.toSet()
    return second.publishedWindows
        .filter { it.corrections == "T-25 re-synthesis" && !it.empty }
        .map { it.layerHeight }
        .filter { it !in checked }
        .sorted()
}

// --- the window re-run, against C-0027's own published edges -----------------------------------

/** One height's window under one correction set, re-run and compared with `C-0027`'s own file. */
@Serializable
data class WindowEdgeReproduction(
    val layerHeight: Double,
    val corrections: String,
    val publishedEmpty: Boolean,
    val rerunEmpty: Boolean,
    val publishedLow: Double? = null,
    val publishedHigh: Double? = null,
    val rerunLow: Double? = null,
    val rerunHigh: Double? = null,
    val lowerDeparture: Double,
    val upperDeparture: Double,
    val movedGridSteps: Int,
    val ownerChanged: Boolean,
    val statement: String
)

/** `C-0027`'s six published windows, re-run on the current — post-`C-0031` — result files. */
fun windowEdgeReproductions(
    inputs: ResynthesisInputs,
    second: SecondResynthesisInputs
): List<WindowEdgeReproduction> {
    val grid = inputs.graftingDensityGrid
    val sets = listOf(CorrectionSet.IDENTITY, CorrectionSet.FULL)
    return sets.flatMap { corrections ->
        val rerun = resynthesisedWindows(inputs, corrections)
        rerun.map { window ->
            val published = second.publishedWindow(window.layerHeight, corrections.label)
            val lowerDeparture = departure(published.lowestGraftingDensity, window.lowestGraftingDensity)
            val upperDeparture = departure(published.highestGraftingDensity, window.highestGraftingDensity)
            val steps = if (published.empty || window.empty) 0
            else maxOf(
                abs(edgeMovementInGridSteps(published.lowestGraftingDensity!!, window.lowestGraftingDensity!!, grid)),
                abs(edgeMovementInGridSteps(published.highestGraftingDensity!!, window.highestGraftingDensity!!, grid))
            )
            val ownerChanged = published.lowerBinding.toSet() != window.lowerBinding.toSet() ||
                    published.upperBinding.toSet() != window.upperBinding.toSet()
            WindowEdgeReproduction(
                layerHeight = window.layerHeight,
                corrections = corrections.label,
                publishedEmpty = published.empty,
                rerunEmpty = window.empty,
                publishedLow = published.lowestGraftingDensity,
                publishedHigh = published.highestGraftingDensity,
                rerunLow = window.lowestGraftingDensity,
                rerunHigh = window.highestGraftingDensity,
                lowerDeparture = lowerDeparture,
                upperDeparture = upperDeparture,
                movedGridSteps = steps,
                ownerChanged = ownerChanged,
                statement = "L0 = ${window.layerHeight} nm, '${corrections.label}': " +
                        if (published.empty && window.empty) "EMPTY then and EMPTY now"
                        else "$steps grid step(s), worst edge departure " +
                                "${"%.3g".format(maxOf(lowerDeparture, upperDeparture))}"
            )
        }
    }
}

private fun departure(published: Double?, rerun: Double?): Double =
    if (published == null || rerun == null) 0.0
    else abs(rerun - published) / maxOf(abs(published), 1e-30)

// --- the fold channels --------------------------------------------------------------------------

/** One `(model)` fold at 10 nm / 2 mM, with all three corrections composed on one tangent. */
@Serializable
data class FoldChannelRecord(
    val model: String,
    val layerHeight: Double,
    val concentration: Double,
    val baselineFoldStroke: Double,
    val baselineMargin: Double,
    val electrostaticForceAtFold: Double,
    val collarLogGradientAtFold: Double,
    val brushStiffnessAtFold: Double,
    val brushMultiplier: Double,
    val couplingTangentAtFold: Double,
    val couplingTangentToMandate: Double,
    val increment: ChannelIncrement,
    val foldMovesDeeper: Boolean,
    val collarRecoversFractionOfTheSoftening: Double,
    val statement: String
)

/**
 * The three corrections standing against `C-0018`'s 10 nm / 2 mM pull-in margin, composed.
 *
 * `C-0033` (`T-60`) measured channels one and two and reported the margin **rising**;
 * `C-0032` (`T-76`) measured channel three and reported it **collapsing to 1.0000–1.0019**.
 * They were filed in consecutive iterations, on the same margin, and **neither carries the
 * other** — which is `CLAUDE.md`'s own recorded trap, in a third instance.
 */
fun foldChannels(second: SecondResynthesisInputs): List<FoldChannelRecord> {
    val element = realisedCouplingLaw(pathCount = 45)
    return second.collarDecompositions
        .filter {
            it.state == "10 nm / 2 mM" && it.loadLine == "coupled" &&
                    it.collarLogGradientAtBaselineFold != null && it.baselineMargin != null
        }
        .sortedBy { it.model }
        .map { decomposition ->
            val fold = second.baselineFold(decomposition.model, "10 nm / 2 mM")
            val stroke = fold.pullInStroke
            val force = -fold.electrostaticStiffnessAtFold * fold.forceDecayLengthAtFold
            val tangent = element.assembledTangent(stroke)
            val increment = foldTangentIncrement(
                electrostaticForce = force,
                collarLogGradient = decomposition.collarLogGradientAtBaselineFold!!,
                brushStiffnessAtFold = fold.brushStiffnessAtFold,
                brushMultiplier = decomposition.fluctuationBrushMultiplier,
                couplingTangentAtFold = tangent,
                mandatedStiffness = MANDATED_COUPLING_STIFFNESS
            )
            FoldChannelRecord(
                model = decomposition.model,
                layerHeight = 10.0,
                concentration = 2.0,
                baselineFoldStroke = stroke,
                baselineMargin = decomposition.baselineMargin!!,
                electrostaticForceAtFold = force,
                collarLogGradientAtFold = decomposition.collarLogGradientAtBaselineFold!!,
                brushStiffnessAtFold = fold.brushStiffnessAtFold,
                brushMultiplier = decomposition.fluctuationBrushMultiplier,
                couplingTangentAtFold = tangent,
                couplingTangentToMandate = tangent / MANDATED_COUPLING_STIFFNESS,
                increment = increment,
                foldMovesDeeper = increment.total > 0.0,
                collarRecoversFractionOfTheSoftening =
                    increment.collar / abs(increment.softening),
                statement = "${decomposition.model}: C-0033's collar adds " +
                        "${"%+.3f".format(increment.collar)}, C-0019's fluctuation " +
                        "${"%+.3f".format(increment.fluctuation)} and C-0030's realised " +
                        "softening ${"%+.3f".format(increment.softening)} pN/nm, so the fold " +
                        "tangent moves ${"%+.3f".format(increment.total)} pN/nm and the fold " +
                        (if (increment.total > 0.0) "moves DEEPER (the margin rises)"
                        else "moves SHALLOWER, toward SS3's own 3 nm target (the margin falls)")
            )
        }
}

// --- the inputs ------------------------------------------------------------------------------

private val reader = Json { ignoreUnknownKeys = true }

/** One window record as `C-0027` published it. */
@Serializable
data class PublishedWindow(
    val layerHeight: Double,
    val corrections: String,
    val empty: Boolean,
    val lowestGraftingDensity: Double? = null,
    val highestGraftingDensity: Double? = null,
    val widthRatio: Double? = null,
    val lowerBinding: List<String> = emptyList(),
    val upperBinding: List<String> = emptyList()
)

/** One `T-60` decomposition record — `(state, model, load line)` identifies it. */
@Serializable
data class CollarDecomposition(
    val state: String,
    val model: String,
    val loadLine: String,
    val baselineMargin: Double? = null,
    val levelOnlyMargin: Double? = null,
    val fullMargin: Double? = null,
    val collarLogGradientAtBaselineFold: Double? = null,
    val foldTangentCollarOnly: Double? = null,
    val foldTangentCollarAndFluctuation: Double? = null,
    val fluctuationBrushMultiplier: Double
)

/** One `T-60` fold record at the `μ ≡ 1` variant — `C-0018` reproduced. */
@Serializable
data class BaselineFold(
    val state: String,
    val model: String,
    val loadLine: String,
    val variant: String,
    val pullInStroke: Double,
    val brushStiffnessAtFold: Double,
    val electrostaticStiffnessAtFold: Double,
    val forceDecayLengthAtFold: Double,
    val margin: Double
)

/** One `T-108` reach record — `(model, nominal height, grafting density)` identifies it. */
@Serializable
data class ReachRecord(
    val model: String,
    val nominalHeight: Double,
    val graftingDensity: Double,
    val restingVolumeFraction: Double,
    val kinematicCeiling: Double,
    val validityCeiling: Double? = null,
    val deadLoadStroke: Double
)

/** One `T-108` reach bound. */
@Serializable
data class ReachBound(val name: String, val value: Double, val shortfall: Double)

/** Everything `T-118` reads beyond what `T-25` already reads. */
class SecondResynthesisInputs(
    val publishedWindows: List<PublishedWindow>,
    val collarDecompositions: List<CollarDecomposition>,
    val baselineFolds: List<BaselineFold>,
    val reachRecords: List<ReachRecord>,
    val reachBounds: List<ReachBound>,
    val crossoverFractions: List<CrossoverReading>,
    val packingLimitedPaths: Int
) {

    /** `C-0027`'s own record at exactly one `(height, correction set)`, or a throw. */
    fun publishedWindow(layerHeight: Double, corrections: String): PublishedWindow {
        val matches = publishedWindows.filter {
            it.layerHeight == layerHeight && it.corrections == corrections
        }
        require(matches.size == 1) {
            "the key ($layerHeight nm, '$corrections') identifies ${matches.size} T-25 " +
                    "windows, not one"
        }
        return matches.single()
    }

    /** `T-60`'s `μ ≡ 1` coupled fold at exactly one `(model, state)`, or a throw. */
    fun baselineFold(model: String, state: String): BaselineFold {
        val matches = baselineFolds.filter {
            it.model == model && it.state == state && it.loadLine == "coupled" &&
                    it.variant.startsWith("mu = 1")
        }
        require(matches.size == 1) {
            "the key ('$model', '$state') identifies ${matches.size} T-60 baseline folds, not one"
        }
        return matches.single()
    }

    companion object {

        fun read(directory: File): SecondResynthesisInputs {
            val windows = ResultInputs.T_25.file(directory).array("windows")
                .map { reader.decodeFromJsonElement(PublishedWindow.serializer(), it) }
            val collar = ResultInputs.T_60.file(directory)
            val decompositions = collar.array("decomposition")
                .map { reader.decodeFromJsonElement(CollarDecomposition.serializer(), it) }
            val folds = collar.array("folds")
                .filter {
                    it.jsonObject["pullInStroke"]?.toString()?.trim('"') !in listOf(null, "null")
                }
                .map { reader.decodeFromJsonElement(BaselineFold.serializer(), it) }
            val reachFile = ResultInputs.T_108.file(directory)
            val reach = reachFile.array("reach")
                .map { reader.decodeFromJsonElement(ReachRecord.serializer(), it) }
            val bounds = reachFile.array("reachBounds")
                .map { reader.decodeFromJsonElement(ReachBound.serializer(), it) }
            // C-0041's buildable path count, read from its own design table rather than
            // transcribed: the largest count whose plan view actually fits the Gen-1 tile
            val packing = ResultInputs.T_96.file(directory).array("designs")
                .map { it.jsonObject }
                .filter { it.getValue("packsOnGen1Tile").toString().trim('"').toBoolean() }
                .maxOf { it.getValue("pathCount").toString().trim('"').toDouble() }
                .toInt()
            return SecondResynthesisInputs(
                publishedWindows = windows,
                collarDecompositions = decompositions,
                baselineFolds = folds,
                reachRecords = reach,
                reachBounds = bounds,
                crossoverFractions = CROSSOVER_READINGS,
                packingLimitedPaths = packing
            )
        }
    }
}

/**
 * `C-0002`'s upper crossover, and `C-0036`'s finding that it is a **one-parameter family**.
 *
 * The first entry is the reading every upstream claim in this programme used and the one
 * `C-0050`'s bound 3 is written on; the others are `C-0036`'s own readings, carried so that the
 * ceiling's dependence on the convention is visible rather than assumed away.
 */
private val CROSSOVER_READINGS: List<CrossoverReading> = listOf(
    CrossoverReading("C-0002 / C-0018 / C-0050 as used", 0.2, "C-0002, CITED — CH-0049 disputes it"),
    CrossoverReading("C-0036 regime reading", 0.141, "C-0036, phi_c = b/xi family at xi = b"),
    CrossoverReading("C-0036 support reading", 0.49, "C-0036, the low end of its support bracket")
)

private fun File.array(name: String) =
    reader.parseToJsonElement(readText()).jsonObject.getValue(name).jsonArray

/** Rounds a grid ratio to the nearest whole step — used only in reporting. */
internal fun gridSteps(ratio: Double, gridRatio: Double): Int =
    (ln(ratio) / ln(gridRatio)).roundToInt()
