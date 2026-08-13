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

import kotlinx.serialization.Serializable
import kotlin.math.abs

/**
 * The relations `T-25` re-runs `C-0016`'s intersection and `C-0017`'s verdict through.
 *
 * Every one of them is a **multiplier on a quantity the window is already a function of**,
 * which is what makes this a synthesis rather than a re-derivation — and it is what
 * `CH-0026` itself asks for: *"carry a multiplier, not a re-run."*
 *
 * The load-bearing one is [correctedStabilityFloor], and its content is a theorem rather
 * than an arithmetic: at the operating point the force balance pins `|F_es|` to
 * `100 pN + P(g)A`, a purely mechanical quantity, and `k_es = −|F_es|/ℓ` identically — so a
 * multiplier on the *level* of the electrostatic force is absorbed entirely into the bias
 * and reaches `k_es` **not at all**. What survives is only the collar's *gradient*.
 */

/** Which corrections a re-run of the window carries. */
@Serializable
data class CorrectionSet(
    /** `C-0019`: the licensed fluctuation brackets on the stroke, the overlap and `k_brush`. */
    val applyFluctuation: Boolean,
    /** `CH-0024`: the stroke measured from the zero-bias rest rather than from `L₀`. */
    val holdDown: HoldDownReading,
    /** `C-0022`/`CH-0026`: the finite-tile edge enhancement, carried at the operating point. */
    val applyEdgeEnhancement: Boolean,
    val label: String
) {

    companion object {

        /** Every correction off — reproduces `C-0016` exactly, and is asserted to. */
        val IDENTITY: CorrectionSet = CorrectionSet(
            applyFluctuation = false,
            holdDown = HoldDownReading.NONE,
            applyEdgeEnhancement = false,
            label = "C-0016 baseline"
        )

        /** Every correction on, at the committed tetherless two-sided device. */
        val FULL: CorrectionSet = CorrectionSet(
            applyFluctuation = true,
            holdDown = HoldDownReading.TETHERLESS,
            applyEdgeEnhancement = true,
            label = "T-25 re-synthesis"
        )
    }
}

/** One height's §4(a)–(d) window under one correction set. */
@Serializable
data class ResynthesisedWindow(
    val layerHeight: Double,
    val corrections: String,
    val empty: Boolean,
    val lowestIndex: Int? = null,
    val highestIndex: Int? = null,
    val lowestGraftingDensity: Double? = null,
    val highestGraftingDensity: Double? = null,
    val widthRatio: Double? = null,
    val lowerBinding: List<String> = emptyList(),
    val upperBinding: List<String> = emptyList(),
    val lowerTie: Boolean = false,
    val upperTie: Boolean = false,
    val crossing: ConstraintCrossing? = null,
    val emptiedBySingleConstraint: String? = null,
    val strokeMultiplier: Double,
    val coilOverlapMultiplier: Double,
    val descentLow: Double,
    val descentHigh: Double,
    val requiredLayerStrokeLow: Double,
    val requiredLayerStrokeHigh: Double
)

/**
 * Re-runs `C-0016`'s §4(a)–(d) intersection at all three heights under [corrections].
 *
 * The constraint set is `C-0016`'s, unchanged: coil overlap, the stretching ratio, the
 * compliance stroke, ion partitioning and drainage. What [corrections] change are the two
 * quantities the two binding constraints are read on — the coil overlap and the stroke — and
 * the **threshold** the stroke is read against, which `CH-0024` moves from `3.0` to `3.0 + d`.
 *
 * Every point is evaluated conservatively over `C-0011`'s three interaction laws, exactly as
 * `C-0016` does: the shortest stroke, the lowest overlap, the softest layer.
 */
fun resynthesisedWindows(
    inputs: ResynthesisInputs,
    corrections: CorrectionSet,
    reverseConstraintOrder: Boolean = false
): List<ResynthesisedWindow> {
    val grid = inputs.graftingDensityGrid
    return listOf(5.0, 7.0, 10.0).map { height ->
        val points = inputs.scf.designPoints.filter { it.layerHeight == height }
            .sortedBy { it.graftingDensity }
        val strokeScale = if (corrections.applyFluctuation) inputs.strokeMultiplier(height) else 1.0
        val overlapScale =
            if (corrections.applyFluctuation) inputs.coilOverlapMultiplier(height) else 1.0
        val holdDownForce = inputs.holdDownForce(corrections.holdDown, height)
        val descents = points.map { point ->
            descentUnderHoldDown(
                holdDownForce = holdDownForce,
                couplingStiffness = MANDATED_COUPLING_STIFFNESS,
                layerStiffnessAtOnset = point.solved.minOf { it.equilibriumStiffness }
            )
        }
        val flags = linkedMapOf(
            "a-coil-overlap" to points.map { point ->
                roundedDecision(point.solved.minOf { it.coilOverlap } * overlapScale) >= 1.0
            },
            "a-stretching-ratio" to points.map { point ->
                roundedDecision(point.solved.minOf { it.stretchingRatio }) >= 1.0
            },
            "a-compliance-stroke" to points.mapIndexed { index, point ->
                val stroke = point.solved.minOf { it.strokeUnderTargetForce } * strokeScale
                roundedDecision(stroke - descents[index]) >= ACCEPTABLE_STROKE_NM
            },
            // C-0016 found §4(c) and §4(d) admissible at all 183 points and nothing in
            // iteration 4 touches either, so they are carried unchanged and re-checked
            "c-ion-partitioning" to points.map { true },
            "d-poroelastic-drainage" to points.map { true }
        )
        val ordered: Map<String, List<Boolean>> =
            if (reverseConstraintOrder) LinkedHashMap(flags.entries.reversed().associate { it.toPair() })
            else flags
        val intervals = ordered.mapValues { (_, values) -> admissibleInterval(values) }
        val attribution = attributeEdges(intervals)
        val requiredStrokes = descents.map { it + ACCEPTABLE_STROKE_NM }
        // crossingOf reports a constraint that is empty ON ITS OWN as both ends with NaN
        // bounds — there is no crossing to quote, one demand simply admits nothing. That is
        // a different fact and it is reported as one rather than serialised as NaN.
        val rawCrossing = crossingOf(intervals, grid)
        val emptiedBy = rawCrossing?.takeIf { it.lowerBoundValue.isNaN() }?.lowerBoundConstraint
        ResynthesisedWindow(
            layerHeight = height,
            corrections = corrections.label,
            empty = attribution == null,
            lowestIndex = attribution?.window?.lowestIndex,
            highestIndex = attribution?.window?.highestIndex,
            lowestGraftingDensity = attribution?.window?.lowest(grid),
            highestGraftingDensity = attribution?.window?.highest(grid),
            widthRatio = attribution?.window?.width(grid),
            lowerBinding = attribution?.lowerBinding ?: emptyList(),
            upperBinding = attribution?.upperBinding ?: emptyList(),
            lowerTie = attribution?.lowerTie ?: false,
            upperTie = attribution?.upperTie ?: false,
            crossing = if (emptiedBy == null) rawCrossing else null,
            emptiedBySingleConstraint = emptiedBy,
            strokeMultiplier = strokeScale,
            coilOverlapMultiplier = overlapScale,
            descentLow = descents.min(),
            descentHigh = descents.max(),
            requiredLayerStrokeLow = requiredStrokes.min(),
            requiredLayerStrokeHigh = requiredStrokes.max()
        )
    }
}

/**
 * The stability floor `|k_eff|` at the operating point, with the edge correction carried.
 *
 * `k_eff = k_brush + k_es` and `k_es = −|F_es|/ℓ`, with `|F_es|` **pinned** by the force
 * balance. A finite-tile multiplier `μ(h)` on the force therefore enters only through the
 * effective decay length,
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`1/ℓ_2D = 1/ℓ_1D(V*′) − d ln μ/dh`,
 *
 * where [collarLogGradient] is the collar's own gradient and [decayLengthShift] is the
 * second-order change in `ℓ_1D` from the lower bias the enhanced force needs. Both are
 * **positive** here, so both *lengthen* `ℓ` and *reduce* `|k_es|` — the opposite direction
 * from the one `CH-0026` predicts for a stability clause, and the reason is that `CH-0026`
 * reasons at fixed bias where the device is held at fixed force.
 *
 * Returns zero where `k_eff ≥ 0`, i.e. where the operating point needs no coupling stiffness
 * at all.
 */
fun correctedStabilityFloor(
    brushStiffness: Double,
    targetForce: Double,
    decayLength: Double,
    collarLogGradient: Double,
    decayLengthShift: Double
): Double {
    require(targetForce > 0.0) { "targetForce must be positive, was: $targetForce" }
    require(decayLength > 0.0) { "decayLength must be positive, was: $decayLength" }
    val shifted = decayLength + decayLengthShift
    require(shifted > 0.0) { "the shifted decay length must be positive, was: $shifted" }
    val inverse = 1.0 / shifted - collarLogGradient
    require(inverse > 0.0) {
        "the collar gradient exceeds the force's own decay rate, which would make the " +
                "finite-tile force GROW with the gap: 1/l = ${1.0 / shifted}, " +
                "dln(mu)/dh = $collarLogGradient"
    }
    val electrostatic = -targetForce * inverse
    val effective = brushStiffness + electrostatic
    return if (effective < 0.0) -effective else 0.0
}

/**
 * The change in the 1-D decay length from the bias the finite-tile force lets the device
 * hold its operating point at, in nm.
 *
 * The enhanced force reaches the pinned target at a *lower* bias, `ΔF = −(1 − 1/μ)F`, and
 * `T-16`'s own six-model spread at fixed `(height, buffer)` supplies both `dV/dF` and
 * `dℓ/dV` as measured slopes over states that differ only in what the layer demands. Where
 * the spread is degenerate the shift is zero and the correction reduces to the collar term.
 */
fun decayLengthShiftFromBias(
    states: List<CouplingRequirement>,
    edgeMultiplier: Double
): Double {
    if (states.size < 2) return 0.0
    val byForce = states.sortedBy { it.electrostaticForceAtTarget }
    val (low, high) = byForce.first() to byForce.last()
    val forceSpan = high.electrostaticForceAtTarget - low.electrostaticForceAtTarget
    val biasSpan = high.simultaneousTargetBias - low.simultaneousTargetBias
    if (abs(forceSpan) < 1e-9 || abs(biasSpan) < 1e-12) return 0.0
    val biasPerForce = biasSpan / forceSpan
    val lengthPerBias =
        (high.forceDecayLengthAtTarget - low.forceDecayLengthAtTarget) / biasSpan
    val meanForce = states.map { it.electrostaticForceAtTarget }.average()
    val forceChange = -(1.0 - 1.0 / edgeMultiplier) * meanForce
    return lengthPerBias * biasPerForce * forceChange
}

/** One `(model, height, buffer)` state's coupling margin, before and after iteration 4. */
@Serializable
data class CorrectedMargin(
    val model: String,
    val layerHeight: Double,
    val concentration: Double,
    val edgeMultiplier: Double,
    val collarLogGradientLow: Double,
    val collarLogGradientHigh: Double,
    val decayLengthShift: Double,
    val brushStiffnessBaseline: Double,
    val brushStiffnessCorrected: Double,
    val floorBaseline: Double,
    val floorFluctuationOnly: Double,
    val floorEdgeOnly: Double,
    val floorCombinedLow: Double,
    val floorCombinedHigh: Double,
    val marginBaseline: Double? = null,
    val marginFluctuationOnly: Double? = null,
    val marginEdgeOnly: Double? = null,
    val marginCombinedLow: Double? = null,
    val marginCombinedHigh: Double? = null,
    val stableAtMandate: Boolean
)

/**
 * `C-0017`'s stability margin at every one of its 54 states, with `C-0019` and `CH-0026`
 * applied separately and together.
 *
 * The two corrections run in **opposite** directions — `C-0019` softens `k_brush` and raises
 * the floor, `CH-0026` lengthens `ℓ` and lowers it — so quoting either alone overstates the
 * movement, and the combined column is the one to read.
 */
fun correctedMargins(inputs: ResynthesisInputs): List<CorrectedMargin> =
    inputs.couplingRequirements.map { record ->
        val height = record.layerHeight
        val heldGap = record.heldGap
        val multiplier = inputs.heldGapEdgeMultiplier(height)
        val (gradientLow, gradientHigh) = inputs.collarGradientBracketOver(2.0, heldGap, heldGap)
        val siblings = inputs.couplingRequirements.filter {
            it.layerHeight == height && it.concentration == record.concentration
        }
        val shift = decayLengthShiftFromBias(siblings, multiplier)
        val degraded = record.brushStiffnessAtHeldGap * inputs.brushStiffnessMultiplier(height)
        fun floor(brush: Double, gradient: Double, decayShift: Double) = correctedStabilityFloor(
            brushStiffness = brush,
            targetForce = record.electrostaticForceAtTarget,
            decayLength = record.forceDecayLengthAtTarget,
            collarLogGradient = gradient,
            decayLengthShift = decayShift
        )
        val baseline = floor(record.brushStiffnessAtHeldGap, 0.0, 0.0)
        val fluctuationOnly = floor(degraded, 0.0, 0.0)
        val edgeOnly = floor(record.brushStiffnessAtHeldGap, gradientLow, shift)
        val combinedHigh = floor(degraded, gradientLow, shift)
        val combinedLow = floor(degraded, gradientHigh, shift)
        // a zero floor is not an infinite margin, it is the ABSENCE of a requirement, and
        // T-16's own file records it as null. The same convention is kept here.
        fun margin(value: Double) =
            if (value <= 0.0) null else MANDATED_COUPLING_STIFFNESS / value
        CorrectedMargin(
            model = record.model,
            layerHeight = height,
            concentration = record.concentration,
            edgeMultiplier = multiplier,
            collarLogGradientLow = gradientLow,
            collarLogGradientHigh = gradientHigh,
            decayLengthShift = shift,
            brushStiffnessBaseline = record.brushStiffnessAtHeldGap,
            brushStiffnessCorrected = degraded,
            floorBaseline = baseline,
            floorFluctuationOnly = fluctuationOnly,
            floorEdgeOnly = edgeOnly,
            floorCombinedLow = combinedLow,
            floorCombinedHigh = combinedHigh,
            marginBaseline = margin(baseline),
            marginFluctuationOnly = margin(fluctuationOnly),
            marginEdgeOnly = margin(edgeOnly),
            marginCombinedLow = margin(combinedHigh),
            marginCombinedHigh = margin(combinedLow),
            stableAtMandate = combinedHigh < MANDATED_COUPLING_STIFFNESS
        )
    }

/** One pull-in state's bias-axis margin, before and after iteration 4. */
@Serializable
data class CorrectedPullIn(
    val model: String,
    val layerHeight: Double,
    val concentration: Double,
    val operatingBias: Double,
    val pullInBias: Double,
    val marginBaseline: Double? = null,
    val foldGap: Double,
    val foldElectrostaticForce: Double,
    val edgeMultiplierAtFold: Double,
    val collarLogGradientLow: Double,
    val collarLogGradientHigh: Double,
    val brushStiffnessAtFold: Double,
    val brushStiffnessCorrectedAtFold: Double,
    val foldTangentAtLowGradient: Double,
    val foldTangentAtHighGradient: Double,
    val foldRemainsStable: Boolean,
    val boundUnconditional: Boolean,
    val operatingBiasShift: Double,
    val correctedOperatingBias: Double,
    val marginLowerBound: Double,
    val statement: String
)

/**
 * `C-0018`'s pull-in margin re-read with `C-0019` and `CH-0026` applied.
 *
 * The margin `V_pullin/V*` moves on two axes and they are not the same kind of statement:
 *
 * 1. **The operating bias falls, unambiguously.** The finite-tile force reaches the pinned
 *    target at a lower bias, by `ΔF = −(1 − 1/μ)F` through `T-16`'s own measured `dV/dF`.
 *    That alone raises the margin, and [marginLowerBound] carries it.
 * 2. **The pull-in bias moves by an amount this synthesis does not resolve.** At `C-0018`'s
 *    own fold state the coupled tangent under both corrections is
 *    `k_c + k_brush·m + k_es'`, and it **straddles zero across the collar gradient's own
 *    difference-scheme spread** — `C-0019`'s softening and `CH-0026`'s collar cancel to
 *    within the numerical resolution of the correction itself. [boundUnconditional] is true
 *    only where the tangent is positive at the *low* gradient, i.e. where the direction is
 *    decided; elsewhere the bound is quoted with its condition named rather than asserted.
 */
fun correctedPullInBounds(inputs: ResynthesisInputs): List<CorrectedPullIn> =
    inputs.usableBiasCeilings.filter {
        it.loadLine == "coupled" && it.pullInBias != null && it.pullInStroke != null &&
                it.brushStiffnessAtFold != null && it.electrostaticStiffnessAtFold != null &&
                it.forceDecayLengthAtFold != null && it.operatingBias != null &&
                it.margin != null &&
                // T-16 swept 0.5/1/2 mM and T-4 swept 0.5/2/10, so the 7 nm / 10 mM folds
                // have no coupling record to take dV/dF from. They are reported by
                // C-0018 and NOT propagated here rather than propagated on a substitute.
                inputs.couplingRequirements.any { record ->
                    record.model == it.model && record.layerHeight == it.layerHeight &&
                            record.concentration == it.concentration
                }
    }.map { fold ->
        val foldGap = fold.layerHeight - fold.pullInStroke!!
        val decayAtFold = fold.forceDecayLengthAtFold!!
        val force = -fold.electrostaticStiffnessAtFold!! * decayAtFold
        val multiplier = inputs.collarMultiplierAt(fold.concentration, foldGap)
        val (gradientLow, gradientHigh) = inputs.collarGradientBracketOver(
            fold.concentration, foldGap, foldGap
        )
        val brush = fold.brushStiffnessAtFold!! *
                inputs.brushStiffnessMultiplier(fold.layerHeight)
        fun tangent(gradient: Double) =
            MANDATED_COUPLING_STIFFNESS + brush - force * (1.0 / decayAtFold - gradient)
        val tangentLow = tangent(gradientLow)
        val tangentHigh = tangent(gradientHigh)
        val siblings = inputs.couplingRequirements.filter {
            it.layerHeight == fold.layerHeight && it.concentration == fold.concentration
        }
        val record = siblings.first { it.model == fold.model }
        val biasPerForce = siblings.sortedBy { it.electrostaticForceAtTarget }.let { sorted ->
            val span = sorted.last().electrostaticForceAtTarget -
                    sorted.first().electrostaticForceAtTarget
            if (abs(span) < 1e-9) 0.0
            else (sorted.last().simultaneousTargetBias -
                    sorted.first().simultaneousTargetBias) / span
        }
        val heldMultiplier = inputs.heldGapEdgeMultiplier(fold.layerHeight, fold.concentration)
        val shift = biasPerForce * -(1.0 - 1.0 / heldMultiplier) *
                record.electrostaticForceAtTarget
        val corrected = fold.operatingBias!! + shift
        require(corrected > 0.0) { "the corrected operating bias must stay positive" }
        CorrectedPullIn(
            model = fold.model,
            layerHeight = fold.layerHeight,
            concentration = fold.concentration,
            operatingBias = fold.operatingBias,
            pullInBias = fold.pullInBias!!,
            marginBaseline = fold.margin!!,
            foldGap = foldGap,
            foldElectrostaticForce = force,
            edgeMultiplierAtFold = multiplier,
            collarLogGradientLow = gradientLow,
            collarLogGradientHigh = gradientHigh,
            brushStiffnessAtFold = fold.brushStiffnessAtFold,
            brushStiffnessCorrectedAtFold = brush,
            foldTangentAtLowGradient = tangentLow,
            foldTangentAtHighGradient = tangentHigh,
            foldRemainsStable = tangentHigh > 0.0,
            boundUnconditional = tangentLow > 0.0,
            operatingBiasShift = shift,
            correctedOperatingBias = corrected,
            marginLowerBound = fold.pullInBias / corrected,
            statement = "${fold.model} at ${fold.layerHeight} nm / ${fold.concentration} mM: " +
                    "V* falls ${"%.1f".format(100.0 * -shift / fold.operatingBias)} %, so the " +
                    "margin rises from ${"%.4f".format(fold.margin)} to " +
                    "${"%.4f".format(fold.pullInBias / corrected)} at unchanged pull-in bias; " +
                    "at the fold the two corrections leave a tangent of " +
                    "${"%.3f".format(tangentLow)} to ${"%.3f".format(tangentHigh)} pN/nm, " +
                    if (tangentLow > 0.0) "positive throughout, so the bound is unconditional"
                    else "STRADDLING ZERO — C-0019 and CH-0026 cancel at the fold to within " +
                            "the collar gradient's own scheme spread, so the pull-in bias's " +
                            "own movement is NOT resolved here"
        )
    }
