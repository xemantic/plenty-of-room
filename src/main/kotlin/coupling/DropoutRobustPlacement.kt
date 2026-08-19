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

package com.xemantic.nano.plentyofroom.coupling

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.sqrt

/**
 * `T-155` — is there a coupling placement and distribution that is flat **under** `C-0087`'s
 * measured staple dropout?
 *
 * ## What is new here, and it is one word
 *
 * `C-0087` grades four standing designs under the dropout and every one fails. Everything it
 * measures is a **value** — the dishing of a design whose stiffnesses were chosen at zero
 * defects. This file moves the objective to a **percentile of a distribution**, which is a
 * different optimisation and the one `C-0087`'s own open item 1 asks for:
 *
 * > *"the direction the reversal points is denser and more regular, and the objective would be a
 * > percentile rather than a value — a different optimisation from any run so far."*
 *
 * ## Three cheap bounds, in the order they run
 *
 * 1. [singlePathRemovalDishing] / [worstSinglePathRemoval] — `C-0087`'s own bound, `n` surrogate
 *    solves and no sampling, generalised into a **cheap objective** whose rank agreement with
 *    the percentile is measured ([spearmanRankCorrelation]) rather than assumed.
 * 2. [oracleFloorSample] — `InfluenceSurrogate.reachableDishingFloorAt` over a realisation's
 *    survivors, a **rigorous lower bound over every distribution whatever**. Above `T-5b`'s
 *    tolerance at the 90th percentile it settles a station set with no search at all.
 * 3. [longestAbsenceRun] / [columnsForRunRobustness] — the run-length pitch arithmetic, with no
 *    solve at all. `CLAUDE.md`: *"an attachment coupling can be a NET DISHING SOURCE, and the
 *    sign flips at an attachment pitch of one Winkler bending length"*, and **a dropout IS an
 *    increase in the attachment pitch**. So the density a dropout demands is a division.
 *
 * ## Conventions, restated rather than inherited
 *
 * Lengths **nm**, forces **pN**, stiffness **pN/nm**; probabilities and dishing-over-stroke
 * ratios are dimensionless. `x` runs **along** the helices, `y` **across** them; the origin is
 * the tile centre. A **dropout is a removal**, not a perturbation: an absent path is solved as
 * an absent station (`C-0087`, `InfluenceSurrogate.solveWithDropout`). Realisations are
 * **independent** across stations — `C-0087`'s convention, carried with its limitation.
 * Percentiles are **nearest-rank order statistics** ([orderStatistic]), so a reported value is
 * one the sample took.
 */

// ------------------------------------------------------------------ the ensemble

/**
 * A fixed bank of Bernoulli presence patterns — **common random numbers** across every candidate
 * design, so that two designs are compared on the *same* fabrication outcomes and their
 * difference is not a sampling difference.
 *
 * Construct through [dropoutEnsemble]. The stream is `C-0087`'s [DropoutRandom] drawn in exactly
 * its order — one deviate per station per realisation, stations in grid order — so an ensemble
 * built at `C-0087`'s own seed reproduces `C-0087`'s own realisations, which is gate 3 and is
 * what makes this task's percentiles comparable with that claim's cell for cell.
 */
class DropoutEnsemble internal constructor(

    /** The incorporation probability of each path, in the order of the station set. */
    val probabilities: List<Double>,

    /** The seed the stream was drawn from — emitted with every result. */
    val seed: Long,

    private val patterns: List<List<Boolean>>
) {

    /** The number of realisations. */
    val realisations: Int get() = patterns.size

    /** The number of paths. */
    val pathCount: Int get() = probabilities.size

    /** The presence flags of realisation [index]. */
    fun presenceAt(index: Int): List<Boolean> = patterns[index]

    /** How many paths survived in realisation [index]. */
    fun survivorsAt(index: Int): Int = patterns[index].count { it }

    /** The mean number of surviving paths over the ensemble. */
    val meanSurvivors: Double by lazy {
        patterns.sumOf { pattern -> pattern.count { it } }.toDouble() / patterns.size
    }

    /** The **fewest** survivors any realisation had — the tail of the support-set size. */
    val fewestSurvivors: Int by lazy { patterns.minOf { pattern -> pattern.count { it } } }

}

/** [realisations] independent Bernoulli draws per path at [probabilities], from [seed]. */
fun dropoutEnsemble(
    probabilities: List<Double>,
    realisations: Int,
    seed: Long
): DropoutEnsemble {
    require(probabilities.isNotEmpty()) { "probabilities must not be empty" }
    require(realisations > 0) { "realisations must be positive, was: $realisations" }
    require(probabilities.all { it >= 0.0 && it <= 1.0 }) {
        "every probability must lie in [0, 1], were: $probabilities"
    }
    val random = DropoutRandom(seed)
    return DropoutEnsemble(
        probabilities, seed,
        (1..realisations).map { bernoulliPresence(probabilities, random) }
    )
}

// ------------------------------------------------------------------ the distribution

/** The dishing distribution of one design over one ensemble, as ratios of the free-tile stroke. */
data class DropoutDishing(

    /** How many realisations the summary is over. */
    val realisations: Int,

    /** The design's own dishing with **no** path missing. */
    val nominal: Double,

    /** The mean over realisations — reported, never optimised: the distribution is skewed. */
    val mean: Double,

    /** The 50th percentile. */
    val median: Double,

    /** **The verdict statistic.** */
    val p90: Double,

    /** The 95th percentile. */
    val p95: Double,

    /** The worst realisation in the sample. */
    val worst: Double,

    /** The fraction of realisations above the flatness tolerance. */
    val exceedance: Double,

    /** `√(p(1 − p)/n)` on that fraction — a probability without one is not a result. */
    val exceedanceStandardError: Double,

    /**
     * The exact one-sided Clopper-Pearson limit where [exceedance] is **saturated**, else `null`.
     *
     * `T-213`/`CH-0153`. At `p̂ = 1` or `p̂ = 0` the symmetric [exceedanceStandardError] is
     * `√(p̂(1 − p̂)/n)` with a zero numerator, so it is **identically zero for every sample
     * count** and cannot distinguish 1 250 draws from 20 000. [saturatedProportionBound] can:
     * `p > (1 − c)^(1/n)`, whose large-`n` form is the rule of three.
     *
     * It lives on the **summary** rather than at six emission sites because that is the defect
     * `C-0129` measured — its own repair was applied to one file, and six more carried the same
     * degenerate statistic. `null` at an unsaturated proportion is deliberate: there the
     * symmetric error is the right instrument and a one-sided bound is the wrong question.
     */
    val exceedanceOneSidedBound: Double?,

    /** The mean number of surviving paths. */
    val meanSurvivors: Double,

    /** Whether the 90th percentile is inside the tolerance. */
    val flatAtP90: Boolean,

    /** Whether the median is inside the tolerance. */
    val flatAtMedian: Boolean
)

/**
 * The peak dishing in nm of [stiffnesses] at every realisation of [ensemble], in the ensemble's
 * own order.
 *
 * A realisation in which no path survives is the free tile, which
 * [InfluenceSurrogate.solveWithDropout] returns without a special case.
 */
fun dropoutDishingSample(
    surrogate: InfluenceSurrogate,
    stiffnesses: List<Double>,
    ensemble: DropoutEnsemble
): DoubleArray {
    require(stiffnesses.size == surrogate.pathCount) {
        "expected ${surrogate.pathCount} stiffnesses, was: ${stiffnesses.size}"
    }
    require(ensemble.pathCount == surrogate.pathCount) {
        "the ensemble carries ${ensemble.pathCount} paths and the surrogate " +
                "${surrogate.pathCount}"
    }
    return DoubleArray(ensemble.realisations) {
        surrogate.solveWithDropout(stiffnesses, ensemble.presenceAt(it)).peakDishing
    }
}

/** [sample] and [nominal] summarised against a flatness [tolerance], all as ratios of a stroke. */
fun summariseDropoutDishing(
    sample: DoubleArray,
    nominal: Double,
    meanSurvivors: Double,
    tolerance: Double
): DropoutDishing {
    require(sample.isNotEmpty()) { "sample must not be empty" }
    require(tolerance > 0.0) { "tolerance must be positive, was: $tolerance" }
    val exceedance = sample.count { it > tolerance }.toDouble() / sample.size
    val median = orderStatistic(sample, 0.50)
    val p90 = orderStatistic(sample, 0.90)
    return DropoutDishing(
        realisations = sample.size,
        nominal = nominal,
        mean = sample.average(),
        median = median,
        p90 = p90,
        p95 = orderStatistic(sample, 0.95),
        worst = sample.max(),
        exceedance = exceedance,
        exceedanceStandardError = binomialStandardError(exceedance, sample.size),
        exceedanceOneSidedBound =
            if (exceedance == 0.0 || exceedance == 1.0)
                saturatedProportionBound(exceedance, sample.size) else null,
        meanSurvivors = meanSurvivors,
        flatAtP90 = p90 < tolerance,
        flatAtMedian = median < tolerance
    )
}

// ------------------------------------------------------------------ bound 1: one missing path

/**
 * The peak dishing in nm with **exactly one** path absent, one entry per absent station —
 * `C-0087`'s cheap bound as a **profile** rather than a maximum, so that a design's fragility can
 * be read station by station.
 *
 * `n` surrogate solves and no sampling.
 */
fun singlePathRemovalDishing(
    surrogate: InfluenceSurrogate,
    stiffnesses: List<Double>
): List<Double> {
    require(stiffnesses.size == surrogate.pathCount) {
        "expected ${surrogate.pathCount} stiffnesses, was: ${stiffnesses.size}"
    }
    require(surrogate.pathCount >= 2) {
        "a single-removal profile needs at least two paths, had: ${surrogate.pathCount}"
    }
    val indices = 0 until surrogate.pathCount
    return indices.map { absent ->
        surrogate.solveWithDropout(stiffnesses, indices.map { it != absent }).peakDishing
    }
}

/** The worst entry of [singlePathRemovalDishing] — the cheap objective `T-155` tests. */
fun worstSinglePathRemoval(
    surrogate: InfluenceSurrogate,
    stiffnesses: List<Double>
): Double = singlePathRemovalDishing(surrogate, stiffnesses).max()

// ------------------------------------------------------------------ bound 2: the oracle floor

/**
 * `InfluenceSurrogate.reachableDishingFloorAt` at every realisation of [ensemble] — a **pointwise
 * lower bound** on the peak dishing of every stiffness distribution whatever.
 */
fun oracleFloorSample(
    surrogate: InfluenceSurrogate,
    ensemble: DropoutEnsemble
): DoubleArray {
    require(ensemble.pathCount == surrogate.pathCount) {
        "the ensemble carries ${ensemble.pathCount} paths and the surrogate " +
                "${surrogate.pathCount}"
    }
    return DoubleArray(ensemble.realisations) {
        surrogate.reachableDishingFloorAt(ensemble.presenceAt(it))
    }
}

// ------------------------------------------------------------------ bound 3: the run length

/**
 * The longest run of consecutive absent stations **within one row** of [rowLength], over a
 * presence vector laid out row by row.
 *
 * A run is counted inside a row and never across the row boundary, because the pitch a run opens
 * up is an along-helix pitch and the next row is a different beam.
 */
fun longestAbsenceRun(present: List<Boolean>, rowLength: Int): Int {
    require(present.isNotEmpty()) { "present must not be empty" }
    require(rowLength > 0) { "rowLength must be positive, was: $rowLength" }
    require(present.size % rowLength == 0) {
        "a presence vector of ${present.size} does not split into rows of $rowLength"
    }
    var longest = 0
    var run = 0
    present.indices.forEach { index ->
        if (index % rowLength == 0) run = 0
        run = if (present[index]) 0 else run + 1
        longest = max(longest, run)
    }
    return longest
}

/**
 * The number of attachment columns a tile of extent [edgeX] needs for the surviving pitch to stay
 * inside one Winkler bending length [bendingLength] after [run] consecutive absences —
 * `⌈(run + 1)·edgeX/ℓ⌉`.
 *
 * `CLAUDE.md`'s sign rule for a coupling that is a **net dishing source** read backwards: the
 * pitch a design is entitled to is `ℓ`, a run of `run` absences multiplies the local pitch by
 * `run + 1`, and the column count that keeps the product inside `ℓ` is one division.
 */
fun columnsForRunRobustness(edgeX: Double, bendingLength: Double, run: Int): Int {
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(bendingLength > 0.0) { "bendingLength must be positive, was: $bendingLength" }
    require(run >= 0) { "run must not be negative, was: $run" }
    return ceil((run + 1) * edgeX / bendingLength).toInt()
}

// ------------------------------------------------------------------ the rank agreement

/**
 * Spearman's rank correlation between [a] and [b], with tied ranks averaged.
 *
 * Used for one question only: does the **cheap** single-removal bound rank candidate designs the
 * way the **expensive** percentile does? A high correlation licenses a placement search on the
 * cheap objective; a low one says the bound explains the level and not the ordering, and that is
 * a result rather than a disappointment.
 */
fun spearmanRankCorrelation(a: List<Double>, b: List<Double>): Double {
    require(a.size == b.size) { "the two samples must be paired: ${a.size} against ${b.size}" }
    require(a.size >= 2) { "a rank correlation needs at least two pairs, had: ${a.size}" }
    fun ranks(values: List<Double>): DoubleArray {
        val order = values.indices.sortedBy { values[it] }
        val rank = DoubleArray(values.size)
        var index = 0
        while (index < order.size) {
            var last = index
            while (last + 1 < order.size && values[order[last + 1]] == values[order[index]]) last++
            val mean = (index + last) / 2.0 + 1.0
            for (tied in index..last) rank[order[tied]] = mean
            index = last + 1
        }
        return rank
    }

    val rankA = ranks(a)
    val rankB = ranks(b)
    val meanA = rankA.average()
    val meanB = rankB.average()
    var covariance = 0.0
    var varianceA = 0.0
    var varianceB = 0.0
    a.indices.forEach {
        val da = rankA[it] - meanA
        val db = rankB[it] - meanB
        covariance += da * db
        varianceA += da * da
        varianceB += db * db
    }
    if (varianceA == 0.0 || varianceB == 0.0) return 0.0
    return covariance / sqrt(varianceA * varianceB)
}

// ------------------------------------------------------------------ the redundancy arithmetic

/**
 * What a path count buys and what it costs, with **no solve at all** — the redundancy axis
 * `C-0087` names and nothing in this corpus has priced.
 *
 * @param pathCount the number of attachments.
 * @param perPathStiffness `K/n`, the uniform share of `C-0017`'s mandate.
 * @param perPathForce the force one path carries at the stroke the mandate is placed at.
 * @param allowableRatio that force over the per-path allowable — **falls as `1/n`**, so a
 *   redundant coupling is *less* force-limited, not more.
 * @param survivorFractionAtP10 the 10th percentile of the surviving fraction, i.e. the tail a
 *   design must still work at.
 */
data class RedundancyLedger(
    val pathCount: Int,
    val perPathStiffness: Double,
    val perPathForce: Double,
    val allowableRatio: Double,
    val expectedSurvivors: Double,
    val survivorFractionAtP10: Double,
    val nominalPitch: Double,
    val pitchOverBendingLength: Double,
    val worstRunAtP90: Int,
    val survivingPitchAtP90: Double,
    val survivingPitchInsideBendingLength: Boolean
)

/**
 * [RedundancyLedger] for a `columns × rows` attachment grid under [ensemble].
 *
 * Everything here is arithmetic on the ensemble and the mandate — no lattice, no load, no solve —
 * which is why it runs before the sweep it predicts.
 */
fun redundancyLedger(
    columns: Int,
    rows: Int,
    edgeX: Double,
    totalStiffness: Double,
    stroke: Double,
    allowable: Double,
    bendingLength: Double,
    ensemble: DropoutEnsemble
): RedundancyLedger {
    require(columns > 0) { "columns must be positive, was: $columns" }
    require(rows > 0) { "rows must be positive, was: $rows" }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(totalStiffness > 0.0) { "totalStiffness must be positive, was: $totalStiffness" }
    require(stroke > 0.0) { "stroke must be positive, was: $stroke" }
    require(allowable > 0.0) { "allowable must be positive, was: $allowable" }
    require(bendingLength > 0.0) { "bendingLength must be positive, was: $bendingLength" }
    val pathCount = columns * rows
    require(ensemble.pathCount == pathCount) {
        "the ensemble carries ${ensemble.pathCount} paths and the grid $pathCount"
    }
    val perPath = totalStiffness / pathCount
    val survivorFractions = DoubleArray(ensemble.realisations) {
        ensemble.survivorsAt(it).toDouble() / pathCount
    }
    val runs = DoubleArray(ensemble.realisations) {
        longestAbsenceRun(ensemble.presenceAt(it), columns).toDouble()
    }
    val worstRun = orderStatistic(runs, 0.90).toInt()
    val pitch = edgeX / columns
    val survivingPitch = (worstRun + 1) * pitch
    return RedundancyLedger(
        pathCount = pathCount,
        perPathStiffness = perPath,
        perPathForce = perPath * stroke,
        allowableRatio = perPath * stroke / allowable,
        expectedSurvivors = ensemble.meanSurvivors,
        survivorFractionAtP10 = orderStatistic(survivorFractions, 0.10),
        nominalPitch = pitch,
        pitchOverBendingLength = pitch / bendingLength,
        worstRunAtP90 = worstRun,
        survivingPitchAtP90 = survivingPitch,
        survivingPitchInsideBendingLength = survivingPitch <= bendingLength
    )
}

/**
 * `1/p` compensation clipped so that no path exceeds [ceiling] pN/nm, renormalised to
 * [totalStiffness] — the buildable form of `C-0087`'s compensated reading.
 *
 * `C-0087` compensates and does **not** renormalise, so its expected total *is* the mandate and
 * its nominal total is above it. Here the mandate is held as a sum at nominal, which is the
 * reading a builder can actually specify, and the two are reported side by side.
 */
fun inverseIncorporationWeights(probabilities: List<Double>, exponent: Double): List<Double> {
    require(probabilities.isNotEmpty()) { "probabilities must not be empty" }
    require(probabilities.all { it > 0.0 && it <= 1.0 }) {
        "every probability must lie in (0, 1]"
    }
    require(exponent.isFinite()) { "exponent must be finite, was: $exponent" }
    return probabilities.map { Math.pow(1.0 / it, exponent) }
}

/** The largest absolute departure between two dishing samples — a convergence reading. */
fun sampleDeparture(a: DoubleArray, b: DoubleArray): Double {
    require(a.size == b.size) { "the two samples must be paired: ${a.size} against ${b.size}" }
    require(a.isNotEmpty()) { "the samples must not be empty" }
    var worst = 0.0
    a.indices.forEach { worst = max(worst, abs(a[it] - b[it])) }
    return worst
}
