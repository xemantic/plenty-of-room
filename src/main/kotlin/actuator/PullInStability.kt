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

package com.xemantic.nano.plentyofroom.actuator

import kotlinx.serialization.Serializable
import kotlin.math.abs
import kotlin.math.max

/**
 * Task `T-4` — electrostatic softening and pull-in: the **maximum usable bias**, located as the
 * fold of the equilibrium path rather than as a sign change of `k_eff` at one held point.
 *
 * ## The one idea in this file
 *
 * Pull-in is a **discontinuity in the bias**: as the bias rises past it the equilibrium jumps
 * from the shallow branch to near-contact, and a discontinuity is exactly what a bisection on
 * the bias cannot find. Parametrised by the **stroke** the same object is smooth: at every
 * stroke `s` there is one bias that puts an equilibrium there,
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`R(s) + P(L₀−s)·A = |F_es(L₀−s, V_eq(s))|`,
 *
 * and the fold is `max_s V_eq(s)`. Differentiating the balance along the path at `V'(s) = 0`
 * gives `k_c + k_eff = 0` exactly, so **the argmax is the tangency point** and the two routes to
 * it — a maximum of the path and a vanishing coupled tangent — are numerically independent.
 * [EquilibriumPath.fold] takes the first, and the study grades it against the second by a finite
 * difference of the field at fixed applied bias.
 *
 * ## Why the path is parametrised by the DIFFUSE-layer drop, not by the applied bias
 *
 * `C-0008`'s applied bias is the diffuse-layer drop **plus** the compact-layer drop, and
 * recovering the drop from the bias costs 34 Poisson-Boltzmann solves of Stern-series bisection
 * per force evaluation (`diffusePotentialOfAppliedBias`). Run the other way it is free: **one**
 * solve at a given diffuse drop yields the force *and*, through the electrode's own surface
 * charge, the applied bias that produced it. Since the path needs a bias per stroke rather than
 * a force per bias, the inversion is not needed at all — which is a factor of ~35 in the cost of
 * this task, and the reason a 162-fold sweep fits in minutes.
 *
 * ## The sign conventions, restated because three claims have now been quoted without them
 *
 * - `z` is normal to the electrode, positive **away** from it; the gap **is** the layer height.
 * - The **stroke** `s = L₀ − h` is positive **downward**, toward the electrode.
 * - `F_es,z < 0` is attraction toward the electrode; [FieldSample.attraction] is `−F_z`.
 * - The **load line** `R(s)` is positive **upward**: `R = 0` is the free tile, `R = const` a dead
 *   load, `R = k_c s` an unpreloaded output coupling. **A ceiling belongs to a `(bias, load line)`
 *   pair, never to the bias alone** — `CH-0015`, made executable.
 * - `k_es = −∂F_z/∂h`, **negative above the force maximum and positive below it** (`CH-0011`).
 */

// ---------------------------------------------------------------- the field

/** One Poisson-Boltzmann solve, read at a gap and a diffuse-layer drop. */
@Serializable
data class FieldSample(

    /** The tile-electrode gap in nm — the layer height, exactly. */
    val gap: Double,

    /** The diffuse-layer drop in V, which is what the solve is actually parametrised by. */
    val diffusePotential: Double,

    /** The applied bias in V — the diffuse drop **plus** the compact-layer drop. */
    val appliedBias: Double,

    /** The **signed** force in pN over the footprint; negative means toward the electrode. */
    val force: Double
) {

    /** `|F_es|` in pN when the force is attractive, negative when it is repulsive. */
    val attraction: Double get() = -force
}

/**
 * `F_es` at a gap and a **diffuse-layer drop**, which is the cheap direction (see the file KDoc).
 */
fun interface DiffuseParametrisedField {

    /** One solve at [gap] nm and diffuse drop [diffusePotential] V. */
    fun sample(gap: Double, diffusePotential: Double): FieldSample
}

/**
 * The applied bias in V that holds the tile at [gap] against [load] pN of upward force, or
 * `null` when the field cannot supply that much attraction at or below [diffuseCeiling].
 *
 * Bisected on the diffuse drop, exiting on the **bracket width** and never on a residual
 * (`CLAUDE.md`: an unreachable residual tolerance is silent — it returns the right answer having
 * burnt its whole iteration cap). `null` is a physical statement and not a failure: it is where
 * the layer's osmotic divergence, or the force's own maximum, has outrun the field.
 */
fun holdingBias(
    field: DiffuseParametrisedField,
    gap: Double,
    load: Double,
    diffuseFloor: Double = DEFAULT_DIFFUSE_FLOOR,
    diffuseCeiling: Double = DEFAULT_DIFFUSE_CEILING,
    bracketTolerance: Double = DEFAULT_DIFFUSE_TOLERANCE
): FieldSample? {
    require(gap > 0.0) { "gap must be positive, was: $gap" }
    require(load > 0.0) { "load must be positive, was: $load" }
    require(diffuseFloor > 0.0 && diffuseCeiling > diffuseFloor) {
        "the diffuse bracket must be positive and ascending, was: [$diffuseFloor, $diffuseCeiling]"
    }
    require(bracketTolerance > 0.0) {
        "bracketTolerance must be positive, was: $bracketTolerance"
    }
    val atFloor = field.sample(gap, diffuseFloor)
    if (atFloor.attraction >= load) return atFloor
    val atCeiling = field.sample(gap, diffuseCeiling)
    if (atCeiling.attraction < load) return null
    var low = diffuseFloor
    var high = diffuseCeiling
    repeat(MAXIMUM_BISECTIONS) {
        val middle = 0.5 * (low + high)
        if (field.sample(gap, middle).attraction < load) low = middle else high = middle
        if (high - low <= bracketTolerance * max(high, 1.0)) return field.sample(gap, 0.5 * (low + high))
    }
    return field.sample(gap, 0.5 * (low + high))
}

// ---------------------------------------------------------------- the equilibrium path

/** One point of the equilibrium path: the bias that holds the tile at a given stroke. */
@Serializable
data class BranchPoint(

    /** `s = L₀ − h` in nm, positive downward. */
    val stroke: Double,

    /** `h` in nm — the layer height, which is the electrostatic gap. */
    val gap: Double,

    /** The applied bias in V that puts the equilibrium here. */
    val appliedBias: Double,

    /** The diffuse-layer drop in V at that bias — the parameter the solve was run on. */
    val diffusePotential: Double,

    /** `|F_es|` in pN there, equal to [load] by construction. */
    val attraction: Double,

    /** `R(s) + P(L₀−s)·A` in pN — everything holding the tile up at this stroke. */
    val load: Double,

    /** True when no bias was needed: the field already exceeds the load at the search floor. */
    val atDiffuseFloor: Boolean
)

/** What the fold search found — a pull-in, or a demonstration that there is none. */
@Serializable
data class FoldSearch(

    /** The fold, i.e. the largest bias the branch reaches, or `null` when it has no maximum. */
    val fold: BranchPoint?,

    /**
     * True when the branch is descending from its very start, so the ceiling is the bias that
     * holds the tile at **zero** stroke — for a dead load that is the blocking-force bias, and
     * it means no compressed equilibrium under that load is stable at any bias.
     */
    val foldAtBranchStart: Boolean,

    /** The deepest stroke the branch reaches at all. */
    val branchEnd: BranchPoint?,

    /** True when the branch ended because the field ran out, not because the scan did. */
    val reachedDiffuseCeiling: Boolean,

    /** The coarse scan resolution the bracket came from. */
    val coarseSteps: Int,

    /** The golden-section bracket width in nm at exit — the search's own exit criterion. */
    val bracketWidth: Double,

    /** How many biases were located: the cost of the search, reported rather than assumed. */
    val evaluations: Int
) {

    /** The pull-in bias in V, or `null` where the branch has no fold below the model's floor. */
    val pullInBias: Double? get() = fold?.appliedBias
}

/**
 * The equilibrium path of the tile under one load line, parametrised by the stroke.
 *
 * @param restingHeight `L₀` in nm — a **force-onset** height (`C-0011`, `CH-0010`).
 * @param strokeCeiling the deepest stroke the model is allowed to be asked about, in nm.
 * @param field the electrostatics, parametrised by the diffuse drop.
 * @param load everything holding the tile up at a stroke, in pN: `R(s) + P(L₀−s)·A`.
 */
class EquilibriumPath(
    val restingHeight: Double,
    val strokeCeiling: Double,
    val field: DiffuseParametrisedField,
    val diffuseFloor: Double = DEFAULT_DIFFUSE_FLOOR,
    val diffuseCeiling: Double = DEFAULT_DIFFUSE_CEILING,
    val bracketTolerance: Double = DEFAULT_DIFFUSE_TOLERANCE,
    val load: (Double) -> Double
) {

    init {
        require(restingHeight > 0.0) { "restingHeight must be positive, was: $restingHeight" }
        require(strokeCeiling > 0.0 && strokeCeiling < restingHeight) {
            "strokeCeiling must lie in (0, $restingHeight), was: $strokeCeiling"
        }
    }

    private var located = 0

    /** How many biases this path has located so far — the search's cost, not a physical number. */
    val evaluations: Int get() = located

    /** The point of the path at [stroke] nm, or `null` where no bias holds the tile there. */
    fun at(stroke: Double): BranchPoint? {
        require(stroke >= 0.0 && stroke <= strokeCeiling) {
            "stroke must lie in [0, $strokeCeiling], was: $stroke"
        }
        val gap = restingHeight - stroke
        val holding = load(stroke)
        located++
        val sample = if (holding <= 0.0) field.sample(gap, diffuseFloor)
        else holdingBias(field, gap, holding, diffuseFloor, diffuseCeiling, bracketTolerance)
        return sample?.let {
            BranchPoint(
                stroke = stroke,
                gap = gap,
                appliedBias = it.appliedBias,
                diffusePotential = it.diffusePotential,
                attraction = it.attraction,
                load = holding,
                atDiffuseFloor = it.diffusePotential <= diffuseFloor * (1.0 + 1e-12)
            )
        }
    }

    /** The point of the path at the gap [gap] nm, i.e. at a stroke of `L₀ − gap`. */
    fun atGap(gap: Double): BranchPoint? = at(restingHeight - gap)

    /**
     * Locates the fold: a coarse scan for the **first** descent, then golden section inside that
     * bracket, exiting on the bracket width.
     *
     * The coarse scan is what `CLAUDE.md` requires of a non-monotone coupled problem — *"scan for
     * the first sign change and bisect on that bracket"* — and the first descent is the right one
     * to take because the tile descends through the branch in order: a later maximum is a state
     * the device reaches only after having already folded.
     */
    fun fold(
        coarseSteps: Int = DEFAULT_COARSE_STEPS,
        strokeTolerance: Double = DEFAULT_STROKE_TOLERANCE
    ): FoldSearch {
        require(coarseSteps >= 4) { "coarseSteps must be at least 4, was: $coarseSteps" }
        require(strokeTolerance > 0.0) {
            "strokeTolerance must be positive, was: $strokeTolerance"
        }
        val start = located
        val step = strokeCeiling / coarseSteps
        val coarse = mutableListOf<BranchPoint>()
        var exhausted = false
        for (i in 0..coarseSteps) {
            // `i * (ceiling/steps)` at `i == steps` need NOT equal `ceiling` in floating point —
            // it landed three ulp ABOVE it on `T-192`'s 25.144662445344164 nm ceiling, and `at`'s
            // own range `require` then killed a nine-minute sweep three quarters of the way
            // through. The clamp is a no-op wherever the product lands at or below the ceiling,
            // which is every case that did not previously throw, so no emitted number moves.
            val point = at(minOf(i * step, strokeCeiling))
            if (point == null) {
                exhausted = true
                break
            }
            coarse += point
        }
        val end = coarse.lastOrNull()
        var descent = -1
        for (i in 1 until coarse.size) {
            if (coarse[i].appliedBias < coarse[i - 1].appliedBias) {
                descent = i
                break
            }
        }
        if (descent < 0) {
            return FoldSearch(
                fold = null,
                foldAtBranchStart = false,
                branchEnd = end,
                reachedDiffuseCeiling = exhausted,
                coarseSteps = coarseSteps,
                bracketWidth = step,
                evaluations = located - start
            )
        }
        // A descent at the very first step is the branch start unless the path rose and fell
        // inside it, which one interior probe settles — and settling it keeps the endpoint answer
        // exact, which is what a dead load needs.
        if (descent == 1) {
            val probe = at(0.5 * step)
            if (probe == null || probe.appliedBias <= coarse[0].appliedBias) {
                return FoldSearch(
                    fold = coarse[0],
                    foldAtBranchStart = true,
                    branchEnd = end,
                    reachedDiffuseCeiling = exhausted,
                    coarseSteps = coarseSteps,
                    bracketWidth = step,
                    evaluations = located - start
                )
            }
        }
        var low = (descent - 2).coerceAtLeast(0) * step
        var high = minOf(descent * step, strokeCeiling)
        var candidate = coarse[descent - 1]
        val golden = 0.6180339887498949
        var left = high - golden * (high - low)
        var right = low + golden * (high - low)
        var atLeft = at(left)
        var atRight = at(right)
        while (high - low > strokeTolerance) {
            val leftBias = atLeft?.appliedBias ?: Double.NEGATIVE_INFINITY
            val rightBias = atRight?.appliedBias ?: Double.NEGATIVE_INFINITY
            if (leftBias >= rightBias) {
                high = right
                right = left
                atRight = atLeft
                left = high - golden * (high - low)
                atLeft = at(left)
            } else {
                low = left
                left = right
                atLeft = atRight
                right = low + golden * (high - low)
                atRight = at(right)
            }
            listOfNotNull(atLeft, atRight).forEach {
                if (it.appliedBias > candidate.appliedBias) candidate = it
            }
        }
        return FoldSearch(
            fold = candidate,
            foldAtBranchStart = candidate.stroke <= strokeTolerance,
            branchEnd = end,
            reachedDiffuseCeiling = exhausted,
            coarseSteps = coarseSteps,
            bracketWidth = high - low,
            evaluations = located - start
        )
    }
}

// ---------------------------------------------------------------- the small-gap diagnostics

/**
 * The gap in nm at which [attraction] is maximal inside `(low, high)`, or `null` when the
 * maximum sits at an end of the interval — which for a decaying force means it does not turn.
 *
 * This is the gap at which **`k_es` changes sign**: above it the force decays with the gap and
 * the electrostatics softens the layer (§1, `C-0008`); below it the force falls as the gap closes
 * and the electrostatics **stiffens** it (`CH-0011`). Quoting the sign without this gap is the
 * error the programme has now made three times.
 */
fun forceMaximumGap(
    low: Double,
    high: Double,
    coarseSteps: Int = 64,
    tolerance: Double = 1e-6,
    attraction: (Double) -> Double
): Double? {
    require(low > 0.0 && high > low) { "the bracket must be positive and ascending" }
    require(coarseSteps >= 4) { "coarseSteps must be at least 4, was: $coarseSteps" }
    val step = (high - low) / coarseSteps
    var best = 0
    var bestValue = Double.NEGATIVE_INFINITY
    for (i in 0..coarseSteps) {
        val value = attraction(low + i * step)
        if (value > bestValue) {
            bestValue = value
            best = i
        }
    }
    if (best == 0 || best == coarseSteps) return null
    return goldenSectionMaximum(low + (best - 1) * step, low + (best + 1) * step, tolerance, attraction)
}

/**
 * The gap in nm below which the signed [force] is **repulsive**, bisected on the bracket width,
 * or `null` when it never changes sign inside `(low, high)`.
 *
 * `C-0008` reports this sign change at zero bias between 4 and 5 nm; under bias it moves to
 * smaller separation, and `C-0012` puts it at 0.55–1.58 nm. It is an **electrostatic stopper**:
 * the field cannot drive the tile below it, whatever the layer does.
 */
fun repulsionOnsetGap(
    low: Double,
    high: Double,
    coarseSteps: Int = 64,
    tolerance: Double = 1e-9,
    force: (Double) -> Double
): Double? {
    require(low > 0.0 && high > low) { "the bracket must be positive and ascending" }
    require(coarseSteps >= 4) { "coarseSteps must be at least 4, was: $coarseSteps" }
    val step = (high - low) / coarseSteps
    var previous = force(low)
    for (i in 1..coarseSteps) {
        val gap = low + i * step
        val value = force(gap)
        if (previous >= 0.0 && value < 0.0) {
            var left = gap - step
            var right = gap
            while (right - left > tolerance) {
                val middle = 0.5 * (left + right)
                if (force(middle) >= 0.0) left = middle else right = middle
            }
            return 0.5 * (left + right)
        }
        previous = value
    }
    return null
}

/** Golden-section maximisation inside a bracket, exiting on the **bracket width**. */
internal inline fun goldenSectionMaximum(
    low: Double,
    high: Double,
    tolerance: Double,
    f: (Double) -> Double
): Double {
    var left = low
    var right = high
    val golden = 0.6180339887498949
    var a = right - golden * (right - left)
    var b = left + golden * (right - left)
    var fa = f(a)
    var fb = f(b)
    while (right - left > tolerance) {
        if (fa >= fb) {
            right = b
            b = a
            fb = fa
            a = right - golden * (right - left)
            fa = f(a)
        } else {
            left = a
            a = b
            fa = fb
            b = left + golden * (right - left)
            fb = f(b)
        }
    }
    return 0.5 * (left + right)
}

// ---------------------------------------------------------------- the ceilings

/**
 * One bias ceiling in V, with the name of what imposes it. `null` means it does not bind at all
 * — the branch folds, or the model floor is reached, before that constraint is met.
 */
@Serializable
data class BiasCeiling(val name: String, val bias: Double?)

/**
 * The binding ceiling: the smallest one, with ties broken by **declaration order**.
 *
 * The tie-break is not cosmetic. `CLAUDE.md` records that rounding at the serialisation boundary
 * does not make a result file reproducible if it contains an argmin, because two entries can tie
 * to the last unit in the last place; so the comparison is made on the **rounded** values, with
 * the first index winning, and the decision is therefore the same on every re-run.
 */
fun bindingCeiling(ceilings: List<BiasCeiling>): BiasCeiling? {
    var best: BiasCeiling? = null
    var bestValue = Double.POSITIVE_INFINITY
    ceilings.forEach { candidate ->
        val value = candidate.bias?.let { roundActuatorResult(it) } ?: return@forEach
        if (value < bestValue) {
            bestValue = value
            best = candidate
        }
    }
    return best
}

/** How much room a ceiling leaves above an operating bias — the "with margin" of §6 task 4. */
fun biasMargin(ceiling: Double?, operating: Double?): Double? =
    if (ceiling == null || operating == null || operating <= 0.0) null
    else ceiling / operating

/** The smallest diffuse-layer drop the search starts from, in V. */
const val DEFAULT_DIFFUSE_FLOOR: Double = 1e-6

/**
 * The largest diffuse-layer drop the search admits, in V.
 *
 * `C-0008`: the compact layer takes 88 % of 2 V, so the diffuse layer never sees more than
 * ~0.235 V at §3's own ceiling, and the far field saturates above that. 0.35 V is the same
 * bracket `diffusePotentialOfAppliedBias` uses, so nothing here can ask the field for a state
 * `C-0008`'s own inversion would not have found.
 */
const val DEFAULT_DIFFUSE_CEILING: Double = 0.35

/** The relative bracket width at which the diffuse-drop bisection exits. */
const val DEFAULT_DIFFUSE_TOLERANCE: Double = 1e-10

/** The default coarse resolution of the fold scan, in steps across the admissible stroke. */
const val DEFAULT_COARSE_STEPS: Int = 12

/**
 * The bracket width in nm at which the golden-section fold search exits.
 *
 * The bias is quadratic in the stroke near the fold, so 1e−4 nm of stroke is ~1e−9 relative in
 * the located bias — and the study asserts that by re-running the search at 1e−3, 1e−4 and 1e−6.
 */
const val DEFAULT_STROKE_TOLERANCE: Double = 1e-4

/** The iteration cap on the diffuse-drop bisection; the exit is the bracket width, not this. */
private const val MAXIMUM_BISECTIONS: Int = 200
