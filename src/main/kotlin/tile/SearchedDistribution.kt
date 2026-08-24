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

package com.xemantic.nano.plentyofroom.tile

import com.xemantic.nano.plentyofroom.coupling.DropoutEnsemble
import com.xemantic.nano.plentyofroom.coupling.InfluenceSurrogate
import com.xemantic.nano.plentyofroom.coupling.LoadState
import com.xemantic.nano.plentyofroom.coupling.MultiStateSurrogate
import com.xemantic.nano.plentyofroom.coupling.dropoutDishingSample
import com.xemantic.nano.plentyofroom.coupling.minimaxStiffnessDistribution
import com.xemantic.nano.plentyofroom.coupling.optimiseStiffnessDistribution
import com.xemantic.nano.plentyofroom.coupling.orderStatistic
import com.xemantic.nano.plentyofroom.coupling.searchDecision
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.uniformPressure

/**
 * `T-316` — a stiffness distribution **searched** on the honeycomb lattice, rather than a rule
 * transferred onto it.
 *
 * ## Why this file exists
 *
 * Every coupled cell of every census in this repository — `C-0167`'s 64, `C-0180`'s, `C-0205`'s,
 * `C-0208`'s — is graded on exactly two distributions: `C-0058`'s **equal springs** and its
 * **rim-graded 5:1**. Both are rules *transferred onto* the lattice; neither is an optimum *of*
 * it, and `CLAUDE.md` records that *"projection onto a constrained family is not optimisation
 * within it"* at a measured gap of 24.9 %. `C-0208`'s tightest cell misses `T-5b`'s `0.10` by
 * **0.198 %**, so the difference between a transferred rule and a searched one is, for once,
 * larger than the question.
 *
 * ## The search, and where `C-0135`'s cure does and does not reach
 *
 * `C-0135` records that cyclic coordinate descent **on a max** stalls on the kink, at a cost of a
 * factor of 2.5, and its cure — log-sum-exp smoothing with continuation, an exact adjoint
 * gradient, Polak-Ribière with restarts, a lattice snap on the iterate, and every decision taken
 * through `searchDecision` — is [minimaxStiffnessDistribution]. It applies to a **maximum of
 * smooth functions**.
 *
 * The zero-defect peak dishing is such a maximum, so [searchedStiffnessDistribution] runs that
 * cure on it verbatim, over a one-state surrogate built by [honeycombMultiStateSurrogate].
 *
 * A **90th percentile of a dropout ensemble is not**: an order statistic *selects* a realisation
 * rather than maximising over a smooth family, so neither the smoothing nor its adjoint
 * transfers. What is used there instead is `C-0089`'s instrument — a multi-start coordinate
 * descent on the **true** training percentile — with every acceptance and tie-break rounded at
 * six significant digits, which is the half of `C-0135`'s cure that does transfer.
 *
 * The two are **composed, not compared**: the smoothed nominal optimum becomes one of the starts
 * of the percentile descent, beside the transferred distributions, so the composition cannot
 * report a worse in-sample objective than the best of its own comparands.
 */

/**
 * The `max/min` of a per-path stiffness distribution — the axis `C-0060` prices, whose measured
 * buildable window is `3.5 ≤ R ≤ 20`.
 *
 * A design reads the **point** a descent reached and not only its value, so this is emitted
 * beside every searched objective: a falsifier declared on the flatness threshold alone cannot
 * see a buildability threshold being crossed (`CLAUDE.md`).
 */
fun stiffnessRatio(stiffnesses: List<Double>): Double {
    require(stiffnesses.isNotEmpty()) { "stiffnesses must not be empty" }
    require(stiffnesses.all { it > 0.0 && it.isFinite() }) {
        "every path stiffness must be positive and finite, were: $stiffnesses"
    }
    return stiffnesses.max() / stiffnesses.min()
}

/**
 * [MultiStateSurrogate] over a [HoneycombGrillage], with the influence bank taken on
 * `withoutPrestrain`.
 *
 * This is [honeycombTiedSurrogate]'s twin for the smoothed search, and it carries the same
 * discipline for the same reason: a prestrain is a **load**, so an influence taken on the
 * prestrained lattice is that influence *plus* the prestrain's own response and the Woodbury
 * matrix stops being a compliance (`C-0104`). Where there is no prestrain `withoutPrestrain`
 * returns the same object, so the two surrogates are then the same bank — which is asserted as a
 * test rather than argued.
 *
 * A station is `(s, y)` in nm from the face centre, `s` **along** the helices, and deflections are
 * positive **downward**.
 */
fun honeycombMultiStateSurrogate(
    lattice: HoneycombGrillage,
    grid: List<Pair<Double, Double>>,
    states: List<LoadState>,
    samples: Int = 81
): MultiStateSurrogate {
    require(grid.isNotEmpty()) { "grid must not be empty" }
    require(states.isNotEmpty()) { "at least one load state is required" }
    require(samples >= 2) { "samples must be at least 2, was: $samples" }
    val structure = lattice.withoutPrestrain
    val halfS = lattice.lengthS / 2.0
    val halfY = lattice.lengthY / 2.0
    fun sampled(dishingAt: (Double, Double) -> Double): DoubleArray {
        val field = DoubleArray(samples * samples)
        for (i in 0 until samples) {
            val s = -halfS + 2.0 * halfS * i / (samples - 1)
            for (j in 0 until samples) {
                val y = -halfY + 2.0 * halfY * j / (samples - 1)
                field[i * samples + j] = dishingAt(s, y)
            }
        }
        return field
    }
    val influence = grid.map { (s, y) ->
        structure.solve(uniformPressure(0.0), listOf(PointLoad(s, y, 1.0)))
    }
    val free = states.map { lattice.solve(it.pressure) }
    return MultiStateSurrogate(
        grid = grid,
        samples = samples,
        stateNames = states.map { it.name },
        stationInfluence = Array(grid.size) { j ->
            DoubleArray(grid.size) { k -> influence[k].deflection(grid[j].first, grid[j].second) }
        },
        dishingInfluence = Array(grid.size) { k ->
            sampled { s, y -> influence[k].dishing(s, y) }
        },
        stationFree = Array(states.size) { s ->
            DoubleArray(grid.size) { j -> free[s].deflection(grid[j].first, grid[j].second) }
        },
        dishingFree = Array(states.size) { s ->
            sampled { x, y -> free[s].dishing(x, y) }
        }
    )
}

/**
 * The objective the percentile descent minimises: the [fraction] order statistic of [surrogate]'s
 * dishing over [ensemble], as a ratio of [freeStroke], **rounded at the decision precision**.
 *
 * The rounding is not cosmetic. A descent takes `O(10³)` `Double` comparisons and an ulp of
 * jitter in a hot reduction flips one of them, which moves the iteration into a neighbouring
 * basin; `C-0177` records that `T-113` moved 217 fields that way with identical inputs.
 * `searchDecision` quantises at six significant digits, and every tie-break downstream keeps the
 * **earlier** candidate.
 *
 * The ensemble the search sees must be **disjoint in seed** from the one the verdict is read on,
 * or the reported percentile is in sample and is not a result.
 */
fun percentileObjective(
    surrogate: InfluenceSurrogate,
    ensemble: DropoutEnsemble,
    freeStroke: Double,
    fraction: Double = 0.90
): (List<Double>) -> Double {
    require(freeStroke > 0.0 && freeStroke.isFinite()) {
        "freeStroke must be positive and finite, was: $freeStroke"
    }
    require(fraction > 0.0 && fraction <= 1.0) {
        "fraction must lie in (0, 1], was: $fraction"
    }
    require(ensemble.pathCount == surrogate.pathCount) {
        "the ensemble carries ${ensemble.pathCount} paths and the surrogate " +
                "${surrogate.pathCount}"
    }
    return { stiffnesses ->
        val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
        searchDecision(orderStatistic(sample, fraction) / freeStroke)
    }
}

/** What a search found, with no property of the search **path** in it. */
class SearchedDistribution(

    /** The searched distribution — the percentile descent's answer. */
    val stiffnesses: List<Double>,

    /** Its objective on the **training** ensemble, which is in sample by construction. */
    val trainingObjective: Double,

    /** Its `max/min` ratio, the quantity `C-0060`'s window is written on. */
    val ratio: Double,

    /** The smoothed-minimax answer on the **zero-defect** peak dishing. */
    val nominalStiffnesses: List<Double>,

    /** That answer's zero-defect peak dishing, as a ratio of the free stroke. */
    val nominalObjective: Double,

    /** Its own `max/min` ratio. */
    val nominalRatio: Double,

    /** The best training objective any of the transferred starts reached, for the in-sample gap. */
    val bestTransferredTrainingObjective: Double

)

/**
 * The composition: `C-0135`'s smoothed minimax on the zero-defect peak, then `C-0089`'s
 * percentile descent on the true training percentile, seeded from the smoothed answer **and**
 * from the [transferred] distributions the corpus already grades on.
 *
 * Seeding the percentile descent from its own comparands is what makes *"the search is never
 * worse in sample than the best transferred rule"* a property of the composition rather than a
 * hope about it — `optimiseStiffnessDistribution` evaluates every start before moving from it.
 *
 * @param smooth the one-state surrogate the smoothed minimax runs on.
 * @param percentile the surrogate the percentile objective and the grading both read.
 * @param training the ensemble the search sees, disjoint in seed from the grading one.
 */
fun searchedStiffnessDistribution(
    smooth: MultiStateSurrogate,
    percentile: InfluenceSurrogate,
    training: DropoutEnsemble,
    freeStroke: Double,
    totalStiffness: Double,
    transferred: List<List<Double>>,
    fraction: Double = 0.90,
    percentileSweeps: Int = 2,
    percentileScanPoints: Int = 5,
    percentileRefinements: Int = 6,
    smoothingLevels: List<Double> = listOf(0.3, 0.1, 0.03, 0.01, 3e-3, 1e-3),
    smoothingIterations: Int = 25,
    polishSweeps: Int = 4
): SearchedDistribution {
    require(transferred.isNotEmpty()) { "at least one transferred distribution is required" }
    require(totalStiffness > 0.0) {
        "the mandate is an EQUALITY on the sum, was: $totalStiffness"
    }
    require(percentileSweeps >= 1) {
        "percentileSweeps must be at least 1, was: $percentileSweeps"
    }
    val paths = percentile.pathCount
    require(smooth.pathCount == paths) {
        "the two surrogates carry ${smooth.pathCount} and $paths paths"
    }
    require(transferred.all { it.size == paths }) {
        "every transferred distribution must carry one stiffness per path, $paths"
    }
    val objective = percentileObjective(percentile, training, freeStroke, fraction)

    // 1. C-0135's cure, on the quantity it is written for: a MAX over the sampled field.
    val nominal = minimaxStiffnessDistribution(
        surrogate = smooth,
        states = listOf(0),
        totalStiffness = totalStiffness,
        starts = transferred,
        smoothingLevels = smoothingLevels,
        iterationsPerLevel = smoothingIterations,
        polishSweeps = polishSweeps
    )

    // 2. C-0089's instrument, on the quantity the verdict is read on: a training PERCENTILE.
    val searched = optimiseStiffnessDistribution(
        totalStiffness = totalStiffness,
        starts = transferred + listOf(nominal.stiffnesses),
        sweeps = percentileSweeps,
        tolerance = 1e-4,
        searchHalfWidth = 1.5,
        scanPoints = percentileScanPoints,
        refinements = percentileRefinements,
        objective = objective
    )
    return SearchedDistribution(
        stiffnesses = searched.stiffnesses,
        trainingObjective = objective(searched.stiffnesses),
        ratio = stiffnessRatio(searched.stiffnesses),
        nominalStiffnesses = nominal.stiffnesses,
        nominalObjective = nominal.worstDishing / freeStroke,
        nominalRatio = stiffnessRatio(nominal.stiffnesses),
        bestTransferredTrainingObjective = transferred.minOf { objective(it) }
    )
}

/** A one-state [LoadState] over [pressure], for the smoothed search on a single load case. */
fun singleLoadState(name: String, pressure: PressureField): List<LoadState> =
    listOf(LoadState(name, pressure))
