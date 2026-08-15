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

package com.xemantic.nano.plentyofroom.synthesis

import com.xemantic.nano.plentyofroom.structure.C0055_ARM_COUNT
import kotlinx.serialization.Serializable
import kotlin.math.abs

/**
 * Task `T-135` — **which output element does the Gen-1 programme recommend, and on what
 * premises?** Leaf `A8.2`, with `A1.2` for the anchoring scheme and `A2.2` for the clause.
 *
 * ## Why a recommendation needs a type system at all
 *
 * A recommendation is a claim, and in this project a claim carries its provenance, its validity
 * range and its failure routes. What is different here is that the *content* of the answer is
 * mostly bookkeeping over other claims — so the bookkeeping is where the errors live, and it is
 * made executable rather than written out in prose:
 *
 * - a **[RecommendationMargin]** knows its sense, so a ceiling and a floor cannot be compared the
 *   wrong way round, and its classification is a pure function of the ratio;
 * - a **[RecommendationPremise]** carries *how it was obtained* and never how confident anyone
 *   is, so [PremiseStatus.UNDEMONSTRATED] cannot be softened into a citation;
 * - a **[RecommendationFailureRoute]** carries what would decide it *and* whether the reversal is
 *   already inside a published bracket, which is the distinction between an audit and a worry;
 * - a **[RecommendationDecidability]** bound decides whether a recommendation may be made **at
 *   all** — and reports its own declared falsifier having fired or not.
 *
 * ## The cheap bound, and it is the whole of the decision
 *
 * `C-0069` funnels an 11-row element catalogue to 3 that place all 34 instances at one level and
 * **2 that survive every clause**. Two survivors is not a recommendation. [decidability] ranks
 * the survivors on three axes, each of which is an **integer count owned by a standing claim**
 * and not a new result — undemonstrated motifs, `C-0017`'s stability floors cleared, and
 * compression members — and reports a winner **only if all three agree**.
 *
 * **The declared falsifier is that they disagree**, in which case the honest answer to `T-135` is
 * *"the programme cannot yet recommend"*, and the axes name what would decide it.
 */

/** `C-0055`'s self-consistent upward-root count, which is also this recommendation's path count. */
const val ARM_COUNT: Int = C0055_ARM_COUNT

// ---------------------------------------------------------------------------- margins

/**
 * Which way a requirement points.
 *
 * A **[CEILING]** is a bound the design must stay below (the plan budget, the end-condition
 * factor, the per-path unzip allowable); a **[FLOOR]** is one it must stay above (`C-0017`'s
 * stability floors). Recording the sense is what stops the two being divided the wrong way round
 * — the fifth-plus instance of this project's *"quote a quantity with the state it is read at"*
 * discipline, applied to a requirement rather than to a result.
 */
enum class MarginSense { CEILING, FLOOR }

/**
 * How much room a requirement leaves, in four bands.
 *
 * Ordinals ascend with the margin, which is what makes [classifyMargin] monotone and testable.
 */
enum class MarginClass { VIOLATED, NONE, THIN, COMFORTABLE }

/**
 * Below this ratio the design has **no** margin.
 *
 * A **declared convention**, not a measurement: it is chosen so that anything smaller than the
 * coarsest quantum this programme can build — one base-pair rise, 0.34 nm, on the recommended
 * 8.16 nm member, i.e. 4.2 % — counts as none. `C-0023`'s *"a preload that cannot be set is an
 * argument for not needing one"*, read as a tolerance on a margin.
 */
const val NO_MARGIN_THRESHOLD: Double = 1.05

/** Below this ratio a margin is thin: it survives its own model but not a model change. */
const val THIN_MARGIN_THRESHOLD: Double = 1.5

/**
 * The margin a requirement leaves, as a dimensionless ratio ≥ 1 when it clears.
 *
 * Against a [MarginSense.CEILING] it is `limit/value`; against a [MarginSense.FLOOR] it is
 * `value/limit`. Both are invariant under a common rescaling of the two, which is the gate-1
 * check.
 */
fun marginRatio(value: Double, limit: Double, sense: MarginSense): Double {
    require(value > 0.0) { "value must be positive, was: $value" }
    require(limit > 0.0) { "limit must be positive, was: $limit" }
    return when (sense) {
        MarginSense.CEILING -> limit / value
        MarginSense.FLOOR -> value / limit
    }
}

/** [margin] placed in its band. Exactly at the limit is [MarginClass.NONE], never `VIOLATED`. */
fun classifyMargin(margin: Double): MarginClass = when {
    margin < 1.0 -> MarginClass.VIOLATED
    margin < NO_MARGIN_THRESHOLD -> MarginClass.NONE
    margin < THIN_MARGIN_THRESHOLD -> MarginClass.THIN
    else -> MarginClass.COMFORTABLE
}

/** One requirement the recommendation is graded against, with the room it leaves. */
@Serializable
data class RecommendationMargin(
    val quantity: String,
    val owner: String,
    val axis: String,
    val value: Double,
    val limit: Double,
    val sense: MarginSense,
    val margin: Double,
    val classification: MarginClass,
    val note: String
)

/** Builds a [RecommendationMargin], computing its ratio and band rather than accepting them. */
@Suppress("LongParameterList")
fun recommendationMargin(
    quantity: String,
    owner: String,
    axis: String,
    value: Double,
    limit: Double,
    sense: MarginSense,
    note: String
): RecommendationMargin {
    require(quantity.isNotBlank()) { "quantity must be named" }
    require(owner.isNotBlank()) { "owner must be named" }
    val ratio = marginRatio(value, limit, sense)
    return RecommendationMargin(
        quantity = quantity, owner = owner, axis = axis, value = value, limit = limit,
        sense = sense, margin = ratio, classification = classifyMargin(ratio), note = note
    )
}

// ---------------------------------------------------------------------------- premises

/**
 * **How a premise was obtained** — never how confident anyone is about it.
 *
 * [UNDEMONSTRATED] is the one that matters and it is a *literature* verdict, not a modelling one:
 * a motif this programme constructed and a recorded search did not find in print.
 */
enum class PremiseStatus {
    DERIVED,
    CITED_MEASURED,
    CITED_MODEL_INPUT,
    CITED_FITTED,
    SPECIFICATION,
    UNDEMONSTRATED
}

/** One thing the recommendation would lose if the statement were withdrawn. */
@Serializable
data class RecommendationPremise(
    val id: String,
    val statement: String,
    val owner: String,
    val status: PremiseStatus,
    val worth: String
)

// ---------------------------------------------------------------------------- failure routes

/** What a reversed result does to the recommendation. */
enum class RouteEffect { REMOVES_THE_ELEMENT, REMOVES_A_PREMISE, CHANGES_UNIQUENESS }

/** What would settle a route — and only one of these can be spent inside this repository. */
enum class RouteDecider { CALCULATION, MEASUREMENT, SPECIFICATION, EXPERIMENT }

/** One standing result which, reversed, changes the recommendation. */
@Serializable
data class RecommendationFailureRoute(
    val id: String,
    val statement: String,
    val owner: String,
    val effect: RouteEffect,
    val decidedBy: RouteDecider,
    val insidePublishedBracket: Boolean,
    val consequence: String
)

/** Builds a [RecommendationFailureRoute]; exists so that the study cannot omit a field. */
@Suppress("LongParameterList")
fun failureRoute(
    id: String,
    statement: String,
    owner: String,
    effect: RouteEffect,
    decidedBy: RouteDecider,
    insidePublishedBracket: Boolean,
    consequence: String
): RecommendationFailureRoute {
    require(id.isNotBlank()) { "id must be named" }
    return RecommendationFailureRoute(
        id = id, statement = statement, owner = owner, effect = effect, decidedBy = decidedBy,
        insidePublishedBracket = insidePublishedBracket, consequence = consequence
    )
}

// ------------------------------------------------------------------ specification conditionals

/** Whether an unanswered specification question still binds **this** element. */
enum class ConditionalStatus { BINDING, DISCHARGED_BY_THIS_ELEMENT }

/**
 * One specification question the programme has accumulated, checked against the recommendation.
 *
 * `CLAUDE.md`: *"a window gains an axis when a constraint is discovered and loses one when a
 * constraint is DISCHARGED, and an intersection records neither"* — so the discharge check runs
 * here explicitly, question by question, rather than being inherited from the branch that raised
 * it.
 */
@Serializable
data class RecommendationConditional(
    val id: String,
    val question: String,
    val owner: String,
    val status: ConditionalStatus,
    val why: String,
    val ifAnsweredOtherwise: String
)

// ---------------------------------------------------------------------------- decidability

/** One element of `C-0069`'s catalogue, reduced to what the decision needs. */
@Serializable
data class RecommendationCandidate(
    val id: String,
    val name: String,
    val undemonstratedMotifs: Int,
    val stabilityFloorsCleared: Int,
    val compressionMembers: Int,
    val placesInFull: Boolean,
    val singleLevel: Boolean,
    val twoSided: Boolean
) {

    init {
        require(undemonstratedMotifs >= 0) {
            "undemonstratedMotifs must not be negative, was: $undemonstratedMotifs"
        }
        require(stabilityFloorsCleared >= 0) {
            "stabilityFloorsCleared must not be negative, was: $stabilityFloorsCleared"
        }
        require(compressionMembers >= 0) {
            "compressionMembers must not be negative, was: $compressionMembers"
        }
    }

    /** `C-0069`'s clause funnel, in one predicate. */
    val survivesEveryClause: Boolean get() = placesInFull && singleLevel && twoSided
}

/** One tie-break axis, its values over the survivors, and the unique winner if there is one. */
@Serializable
data class RecommendationTieBreak(
    val axis: String,
    val betterIsLower: Boolean,
    val values: Map<String, Int>,
    val winner: String?
)

/**
 * The winner on one axis — `null` where the best value is shared, which is what makes a tie
 * report *"no winner"* rather than an arbitrary first entry (`CLAUDE.md`'s argmin rule).
 */
fun tieBreakAxis(
    axis: String,
    betterIsLower: Boolean,
    values: Map<String, Int>
): RecommendationTieBreak {
    require(values.isNotEmpty()) { "an axis needs at least one candidate" }
    val best = if (betterIsLower) values.values.min() else values.values.max()
    val holders = values.filterValues { it == best }.keys
    return RecommendationTieBreak(
        axis = axis, betterIsLower = betterIsLower, values = values.toSortedMap(),
        winner = holders.singleOrNull()
    )
}

/** The cheap bound: whether the programme can recommend at all, and on what. */
@Serializable
data class RecommendationDecidability(
    val catalogueSize: Int,
    val placeAtOneLevel: Int,
    val survivors: Int,
    val rejected: Int,
    val axes: List<RecommendationTieBreak>,
    val unanimous: Boolean,
    val winner: String?,
    val falsifierFired: Boolean,
    val note: String
)

/**
 * Ranks the survivors of [catalogue] on three axes **in declaration order**, and returns a winner
 * only where all three agree.
 *
 * The axes are fixed here rather than passed in, because they are the ones the standing claims
 * already carry as integer counts; inventing a fourth would be a new result, which is exactly
 * what a cheap bound may not be.
 */
fun decidability(
    catalogue: List<RecommendationCandidate>,
    placeAtOneLevel: Int
): RecommendationDecidability {
    require(catalogue.isNotEmpty()) { "catalogue must not be empty" }
    require(placeAtOneLevel >= 0) { "placeAtOneLevel must not be negative, was: $placeAtOneLevel" }
    val survivors = catalogue.filter { it.survivesEveryClause }
    if (survivors.isEmpty()) {
        return RecommendationDecidability(
            catalogueSize = catalogue.size, placeAtOneLevel = placeAtOneLevel, survivors = 0,
            rejected = catalogue.size, axes = emptyList(), unanimous = false, winner = null,
            falsifierFired = true,
            note = "no element survives every clause — the programme cannot recommend one"
        )
    }
    if (survivors.size == 1) {
        return RecommendationDecidability(
            catalogueSize = catalogue.size, placeAtOneLevel = placeAtOneLevel, survivors = 1,
            rejected = catalogue.size - 1, axes = emptyList(), unanimous = true,
            winner = survivors.single().id, falsifierFired = false,
            note = "exactly one element survives every clause — no tie-break is needed"
        )
    }
    val axes = listOf(
        tieBreakAxis(
            "undemonstrated motifs required (C-0055, C-0028)", betterIsLower = true,
            values = survivors.associate { it.id to it.undemonstratedMotifs }
        ),
        tieBreakAxis(
            "C-0017's six 2 mM stability floors cleared (C-0069)", betterIsLower = false,
            values = survivors.associate { it.id to it.stabilityFloorsCleared }
        ),
        tieBreakAxis(
            "compression members (C-0023, C-0028)", betterIsLower = true,
            values = survivors.associate { it.id to it.compressionMembers }
        )
    )
    val winners = axes.map { it.winner }
    val unanimous = winners.all { it != null } && winners.distinct().size == 1
    return RecommendationDecidability(
        catalogueSize = catalogue.size, placeAtOneLevel = placeAtOneLevel,
        survivors = survivors.size, rejected = catalogue.size - survivors.size, axes = axes,
        unanimous = unanimous, winner = if (unanimous) winners.first() else null,
        falsifierFired = !unanimous,
        note = if (unanimous) {
            "all three axes agree, and none of them is a new result"
        } else {
            "the axes disagree — the declared falsifier has fired, and the honest answer is " +
                    "that the programme cannot yet recommend"
        }
    )
}

// ---------------------------------------------------------------------------- §6 and provenance

/** One row of §6's own acceptance table, read against the recommendation. */
@Serializable
data class RecommendationSectionSixVerdict(
    val task: String,
    val acceptance: String,
    val verdict: String,
    val claims: String,
    val whatTheRecommendationChanges: String
)

/** One upstream number, re-derived here from the library that owns it. */
@Serializable
data class RecommendationReproduction(
    val owner: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double
)

/** Builds a [RecommendationReproduction], computing the relative departure. */
fun recommendationReproduction(
    owner: String,
    quantity: String,
    published: Double,
    reproduced: Double
): RecommendationReproduction = RecommendationReproduction(
    owner = owner, quantity = quantity, published = published, reproduced = reproduced,
    departure = if (published == 0.0) abs(reproduced)
    else abs(reproduced - published) / abs(published)
)

/** One convergence record — a quantity read at two resolutions of the same axis. */
@Serializable
data class RecommendationConvergence(
    val quantity: String,
    val axis: String,
    val coarse: Double,
    val fine: Double,
    val departure: Double
)

/** Builds a [RecommendationConvergence], computing the relative departure. */
fun recommendationConvergence(
    quantity: String,
    axis: String,
    coarse: Double,
    fine: Double
): RecommendationConvergence = RecommendationConvergence(
    quantity = quantity, axis = axis, coarse = coarse, fine = fine,
    departure = if (fine == 0.0) abs(coarse) else abs(fine - coarse) / abs(fine)
)
