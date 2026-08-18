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

package com.xemantic.nano.plentyofroom.stability

import com.xemantic.nano.plentyofroom.anchoring.ArmAnchorage
import com.xemantic.nano.plentyofroom.anchoring.BranchStrategy
import com.xemantic.nano.plentyofroom.anchoring.TwoSpringElastica
import com.xemantic.nano.plentyofroom.anchoring.elasticaArmForStiffness
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlinx.serialization.Serializable
import kotlin.math.abs

/**
 * Task `T-149` — the **recommended** element's pull-in fold, which has never been searched.
 *
 * ## The one idea in this file
 *
 * `C-0018` located §6 task 4's fold for the **affine** mandate `R = 33.3333 s` and `C-0032` for
 * `C-0030`'s strain-**softening** coupled flexure, and `C-0032` is itself the proof that a fold does
 * not transfer between load lines: one substitution moved the fold's own stroke from 3.41–4.13 nm
 * back to 2.80–3.17 nm, **through §3's 3 nm target**. `C-0071` recommends a **third** law —
 * `C-0069`'s `Q5`, an end-loaded elastica arm rooted on one antiparallel crossover — and `CH-0083`
 * is that nobody has looked.
 *
 * What this file adds to `stability/SofteningCouplingStability.kt` is therefore small and precise:
 *
 * 1. [ElasticaArmLoadLine] — `C-0039`'s exact two-spring elastica, `n` in parallel, wearing
 *    [StrokeLoadLine] so that `C-0018`'s `EquilibriumPath` consumes it **unchanged**;
 * 2. [loadLineStrokeCeiling] — the largest stroke a load line's own law will answer at, which for an
 *    **inextensible** arm is a real boundary and not a solver nuisance;
 * 3. [foldPerturbation] — the **cheap bound**, which is the whole methodological point of the task.
 *
 * ## The cheap bound, and why it is exact rather than first order
 *
 * `CLAUDE.md`: *"at a fold the composition of two corrections is EXACT, not first order, because the
 * baseline coupled tangent vanishes there by construction."* Substituting one placed load line for
 * another is exactly two corrections on `C-0018`'s baseline:
 *
 * - a **slope** change `Δk_c = k_new(s*) − k_old(s*)` at the baseline fold stroke `s*`, which needs
 *   one evaluation of each law and no field solve at all;
 * - a **level** change `ΔR = R_new(s*) − R_old(s*)`, which moves the bias the path needs there and so
 *   moves `k_es` with it, and which needs a re-solve.
 *
 * At `s*` the baseline satisfies `k_old(s*) + k_eff(s*) = 0` **exactly** — that identity is what
 * located it — so the new coupled tangent at the same stroke is `Δk_c + Δk_eff`, and the sign of the
 * first term is available before anything is compiled into a sweep. `Δk_c > 0` predicts a **deeper**
 * fold, `Δk_c < 0` a shallower one. It is a prediction and not a proof, because `Δk_eff` is not
 * bounded here; the sweep grades it, and the disagreement between them is this task's declared
 * falsifier.
 *
 * ## Conventions, restated because this is where three claims' conventions meet
 *
 * - The **stroke** `s = L₀ − h` is positive **downward**; a **load line** `R(s)` is positive
 *   **upward**, in pN over the **whole array**. `C-0017`'s 33.3333 pN/nm is a **sum**, so the path
 *   count does not enter the load line at all — it enters the per-path allowables.
 * - **A requirement on a coupling law is owed over `[0, s*]`** (`C-0049`), and `C-0071` places at
 *   §3's **acceptable** clause, so `s* = 3 nm`.
 * - **An inextensible arm has a kinematic ceiling on the stroke**, and `C-0039`'s shooting solve
 *   enumerates only the **small-rotation branch**, so it refuses below the contour. A branch that
 *   ends there has been shown to have no fold *inside the element model's own range* — a **model**
 *   boundary, reported as one.
 */

// ---------------------------------------------------------------- the recommended element

/** `C-0055`'s one antiparallel crossover at the unused out-of-plane azimuth, in pN·nm/rad. */
val GEN1_ARM_ROOT_STIFFNESS: Double = Gen1Tile.crossoverHingeStiffness()

/** `C-0034`'s `A2` — the arm's own duplex end, two strand termini — in pN·nm/rad. */
val GEN1_ARM_TIP_STIFFNESS: Double = ArmAnchorage.twoTerminus().rotationalStiffness

/** `C-0055`/`C-0063`'s self-consistent upward-root count, which is `C-0071`'s path count. */
const val GEN1_RECOMMENDED_PATH_COUNT: Int = 34

/**
 * [count] end-loaded **elasticas** in parallel — `C-0071`'s recommended output element as a load
 * line, and the object `C-0018`'s equilibrium-path solver consumes.
 *
 * The element's own law is `C-0039`'s and is not re-derived here; what this class adds is the
 * assembly (parallel paths add reaction) and the [StrokeLoadLine] interface.
 */
class ElasticaArmLoadLine(
    override val name: String,
    val arm: TwoSpringElastica,
    val count: Int = GEN1_RECOMMENDED_PATH_COUNT
) : StrokeLoadLine {

    init {
        require(count > 0) { "count must be positive, was: $count" }
    }

    /** The arm's contour in nm — a **hard** ceiling on the stroke, by inextensibility. */
    val length: Double get() = arm.length

    /** `n c(ρ_n, ρ_f) EI/L³` in pN/nm — the assembled law's own vanishing-stroke limit. */
    val smallRotationStiffness: Double get() = count * arm.smallRotationStiffness

    override fun reaction(stroke: Double): Double {
        require(stroke >= 0.0) { "stroke must not be negative, was: $stroke" }
        return count * arm.reaction(stroke)
    }

    override fun tangent(stroke: Double): Double {
        require(stroke >= 0.0) { "stroke must not be negative, was: $stroke" }
        return count * arm.tangentStiffness(stroke)
    }

    /** The same array with a different path count — `C-0017`'s mandate is a **sum**. */
    fun withCount(count: Int): ElasticaArmLoadLine = ElasticaArmLoadLine(name, arm, count)

    /** `max_s |φ(s)|` in radians at [stroke] — the validity flag of `C-0039`'s own solve. */
    fun maximumRotation(stroke: Double): Double =
        arm.stateAtDisplacement(stroke).maximumRotation
}

/**
 * `C-0071`'s recommended element, **placed**: the arm length is solved so that [count] of them
 * present [targetStiffness] as a **secant** at [placementStroke].
 *
 * Nothing is read from a result file — `C-0069`'s 8.16439 nm is re-derived here through the same
 * `C-0039` solver that produced it, and the study asserts the reproduction.
 */
@Suppress("LongParameterList")
fun recommendedArmLine(
    name: String,
    count: Int = GEN1_RECOMMENDED_PATH_COUNT,
    rootStiffness: Double = GEN1_ARM_ROOT_STIFFNESS,
    tipStiffness: Double = GEN1_ARM_TIP_STIFFNESS,
    bendingRigidity: Double = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
    targetStiffness: Double = GEN1_MANDATE_STIFFNESS,
    placementStroke: Double = GEN1_ACCEPTABLE_STROKE,
    steps: Int = 400,
    strategy: BranchStrategy = BranchStrategy.CONTINUATION
): ElasticaArmLoadLine {
    val length = elasticaArmForStiffness(
        hingeStiffness = rootStiffness,
        hingeCount = 1,
        farStiffness = tipStiffness,
        bendingRigidity = bendingRigidity,
        count = count,
        targetStiffness = targetStiffness,
        workingDisplacement = placementStroke,
        steps = steps
    )
    return ElasticaArmLoadLine(
        name = name,
        arm = TwoSpringElastica(
            bendingRigidity, length, rootStiffness, tipStiffness, steps, strategy
        ),
        count = count
    )
}

// ---------------------------------------------------------------- the element model's own ceiling

/**
 * True when [line] answers at [stroke] at all: both its reaction and its tangent return finite
 * numbers rather than throwing.
 *
 * A refusal is a statement about the **element model**, not about the device: `C-0039`'s shooting
 * solve enumerates only the small-rotation branch, and an inextensible arm cannot be asked for a
 * stroke past its own contour under any constitutive assumption whatever.
 */
fun StrokeLoadLine.answersAt(stroke: Double): Boolean {
    if (stroke < 0.0) return false
    val reaction = runCatching { reaction(stroke) }.getOrNull() ?: return false
    if (!reaction.isFinite()) return false
    val tangent = runCatching { tangent(stroke) }.getOrNull() ?: return false
    return tangent.isFinite()
}

/**
 * The largest stroke in nm inside `[low, high]` at which [line] still [answersAt], bisected on the
 * **bracket width** and never on a residual (`CLAUDE.md`).
 *
 * Refusal is assumed **monotone** in the stroke — a longer stroke turns the arm further, and the
 * branch is lost once and not repeatedly — and that assumption is *checked* rather than asserted:
 * [strokeCeilingIsMonotone] scans a grid and reports whether any sample above the located ceiling
 * still answers.
 */
fun loadLineStrokeCeiling(
    line: StrokeLoadLine,
    low: Double,
    high: Double,
    resolution: Double = 1.0e-6
): Double {
    require(low >= 0.0) { "low must not be negative, was: $low" }
    require(high > low) { "the bracket must ascend, was: [$low, $high]" }
    require(resolution > 0.0) { "resolution must be positive, was: $resolution" }
    require(line.answersAt(low)) { "${line.name} does not even answer at a stroke of $low nm" }
    if (line.answersAt(high)) return high
    var lower = low
    var upper = high
    while (upper - lower > resolution) {
        val middle = 0.5 * (lower + upper)
        if (line.answersAt(middle)) lower = middle else upper = middle
    }
    return lower
}

/**
 * Whether refusal is monotone in the stroke: `true` when **no** sample of a [samples]-point grid
 * above [ceiling] answers. A `false` here invalidates [loadLineStrokeCeiling]'s bisection and is a
 * result rather than a failure — `CLAUDE.md`'s *"a verdict that is not MONOTONE in a swept variable
 * has no threshold"*.
 */
fun strokeCeilingIsMonotone(
    line: StrokeLoadLine,
    ceiling: Double,
    high: Double,
    samples: Int = 64
): Boolean {
    require(ceiling >= 0.0) { "ceiling must not be negative, was: $ceiling" }
    require(samples >= 2) { "samples must be at least 2, was: $samples" }
    if (high <= ceiling) return true
    val step = (high - ceiling) / samples
    return (1..samples).none { line.answersAt(ceiling + it * step) }
}

/**
 * **The branch-validity ceiling**: the largest stroke in nm inside `[low, high]` at which [line]'s
 * own **reaction** solve returns and keeps `max_s |φ(s)|` below [limit] radians, bisected on the
 * **bracket width**.
 *
 * It is read on the reaction alone and is therefore **not** bounded above by
 * [loadLineStrokeCeiling], which also requires the **tangent** — a forward difference of the same
 * law, and so the first of the two to refuse. The gap between them is a difference step and is
 * measured rather than assumed; a path's ceiling should take the **smaller** of the two.
 *
 * `C-0039`: *"past `π/2` the tip force's moment arm reverses, the shooting residual stops being
 * monotone in the near-end rotation, and the elastica acquires branches this solver does not
 * enumerate."* A refusal is therefore treated here as *"the limit has been reached"* — the two are
 * the same statement about the same branch, and which of them binds first is a **result** rather
 * than a convention: read `line.maximumRotation` at the returned stroke to see which. Returns
 * [high] when neither is reached inside the bracket.
 */
fun rotationLimitStroke(
    line: ElasticaArmLoadLine,
    low: Double,
    high: Double,
    limit: Double = 0.5 * Math.PI,
    resolution: Double = 1.0e-9
): Double {
    require(low >= 0.0) { "low must not be negative, was: $low" }
    require(high > low) { "the bracket must ascend, was: [$low, $high]" }
    require(limit > 0.0) { "limit must be positive, was: $limit" }
    require(resolution > 0.0) { "resolution must be positive, was: $resolution" }
    fun inside(stroke: Double): Boolean {
        val rotation =
            runCatching { line.maximumRotation(stroke) }.getOrNull() ?: return false
        return rotation.isFinite() && rotation < limit
    }
    require(inside(low)) { "${line.name} is already past $limit rad at $low nm" }
    if (inside(high)) return high
    var lower = low
    var upper = high
    while (upper - lower > resolution) {
        val middle = 0.5 * (lower + upper)
        if (inside(middle)) lower = middle else upper = middle
    }
    return lower
}

// ---------------------------------------------------------------- the cheap bound

/** Which way a substitution moves a fold, decided on the **slope** term alone. */
enum class FoldDirection {

    /** `Δk_c > 0`: the coupled tangent at the baseline fold is now positive, so the fold is deeper. */
    DEEPER,

    /** `Δk_c < 0`: the coupled tangent there is now negative, so the fold has already happened. */
    SHALLOWER,

    /** `Δk_c` is zero to the decision precision: the slope term predicts nothing. */
    UNMOVED
}

/**
 * The cheap bound: the two corrections a load-line substitution applies at a baseline fold.
 *
 * @property tangentChange `Δk_c` in pN/nm — the term that is **free**, and whose sign is the
 *   prediction.
 * @property reactionChange `ΔR` in pN — the term that needs a re-solve, reported beside it so that
 *   a reader can see what the prediction omits rather than having to infer it.
 */
@Serializable
data class FoldPerturbation(
    val baselineLine: String,
    val substitutedLine: String,
    val baselineFoldStroke: Double,
    val baselineTangent: Double,
    val substitutedTangent: Double,
    val tangentChange: Double,
    val relativeTangentChange: Double,
    val baselineReaction: Double,
    val substitutedReaction: Double,
    val reactionChange: Double,
    val relativeReactionChange: Double,
    val predictedDirection: String
)

/**
 * [FoldPerturbation] at [baselineFoldStroke], for substituting [substituted] into a path whose fold
 * was located under [baseline].
 *
 * The direction is decided at a **relative** tolerance of [tolerance] on the tangent, and never on a
 * bare `>` — `CLAUDE.md`: *"a strict `>` between two quantities that can be EQUAL BY CONSTRUCTION
 * reports a floating-point tie as a finding"*, and two placed load lines are equal by construction
 * at the placement stroke.
 */
fun foldPerturbation(
    baseline: StrokeLoadLine,
    substituted: StrokeLoadLine,
    baselineFoldStroke: Double,
    tolerance: Double = 1.0e-6
): FoldPerturbation {
    require(baselineFoldStroke >= 0.0) {
        "baselineFoldStroke must not be negative, was: $baselineFoldStroke"
    }
    require(tolerance > 0.0) { "tolerance must be positive, was: $tolerance" }
    val baseTangent = baseline.tangent(baselineFoldStroke)
    val newTangent = substituted.tangent(baselineFoldStroke)
    val baseReaction = baseline.reaction(baselineFoldStroke)
    val newReaction = substituted.reaction(baselineFoldStroke)
    val tangentChange = newTangent - baseTangent
    val relative = if (baseTangent == 0.0) 0.0 else tangentChange / baseTangent
    val relativeReaction = if (baseReaction == 0.0) 0.0 else (newReaction - baseReaction) / baseReaction
    val direction = when {
        abs(relative) <= tolerance -> FoldDirection.UNMOVED
        relative > 0.0 -> FoldDirection.DEEPER
        else -> FoldDirection.SHALLOWER
    }
    return FoldPerturbation(
        baselineLine = baseline.name,
        substitutedLine = substituted.name,
        baselineFoldStroke = baselineFoldStroke,
        baselineTangent = baseTangent,
        substitutedTangent = newTangent,
        tangentChange = tangentChange,
        relativeTangentChange = relative,
        baselineReaction = baseReaction,
        substitutedReaction = newReaction,
        reactionChange = newReaction - baseReaction,
        relativeReactionChange = relativeReaction,
        predictedDirection = direction.name
    )
}
