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

/**
 * `T-215` — the instrument for *"which part of a result file is a descent manifold, and how wide"*.
 *
 * `CLAUDE.md` records the phenomenon and names its amplifier:
 *
 * > A descent on an optimal MANIFOLD has no isolated answer to be reproducible about, and no
 * > amount of rounding supplies one. […] Report the residual rather than asserting byte-identity —
 * > and check what depends on the POINT rather than on the VALUE.
 *
 * What was missing was the second sentence's arithmetic. Two widths are needed, not one:
 *
 * - the **VALUE** width, the relative spread of the objective a descent reports;
 * - the **POINT** width, the relative spread of a functional of the argmin it reports it at.
 *
 * Their ratio is the **amplification**, and it is the number that says whether a file's
 * irreproducibility is an answer moving or only the place the answer was found. It is `null`,
 * never a large number, when the value width is exactly zero — that is a manifold in the strict
 * sense and a ratio to zero is not a reading.
 */
data class ManifoldWidth(
    val members: Int,
    /** How many *distinct* objective readings the ensemble carries — 1 means reproducible. */
    val distinctValues: Int,
    val valueWidth: Double,
    val pointWidth: Double,
    /** `pointWidth / valueWidth`, or `null` where the value width is exactly zero. */
    val amplification: Double?,
    val values: List<Double>,
    val points: List<Double>
)

/**
 * The relative spread of one quantity over an ensemble, `(max − min) / max|·|`.
 *
 * Relative, so it carries no units and two quantities in different units — a dimensionless
 * dishing and a stiffness in pN/nm — can be compared. Exactly `0.0` on an ensemble of identical
 * readings, which is what makes *"reproducible"* an assertion rather than a rounding.
 */
fun ensembleWidth(values: List<Double>): Double {
    require(values.isNotEmpty()) { "an ensemble must have at least one member" }
    require(values.all { it.isFinite() }) { "every member must be finite" }
    val scale = values.maxOf { abs(it) }
    if (scale == 0.0) return 0.0
    return (values.max() - values.min()) / scale
}

/** The two widths of one descent's ensemble, and the amplification between them. */
fun manifoldWidth(values: List<Double>, points: List<Double>): ManifoldWidth {
    require(values.size == points.size) {
        "values and points must be one ensemble, were: ${values.size} and ${points.size}"
    }
    val valueWidth = ensembleWidth(values)
    val pointWidth = ensembleWidth(points)
    return ManifoldWidth(
        members = values.size,
        distinctValues = values.distinct().size,
        valueWidth = valueWidth,
        pointWidth = pointWidth,
        amplification = if (valueWidth == 0.0) null else pointWidth / valueWidth,
        values = values,
        points = points
    )
}

/**
 * This ensemble's first member, with its first weight advanced by [ulps] units in the last place.
 *
 * The smallest perturbation a `Double` admits, so a descent that moves under it is degenerate at
 * the arithmetic's own resolution — which is exactly the size of the jitter `CLAUDE.md` attributes
 * a re-run's movement to (*"the JIT recompiling a hot reduction"*).
 */
fun List<Double>.perturbedByUlps(ulps: Int): List<Double> {
    require(isNotEmpty()) { "cannot perturb an empty start" }
    require(ulps > -1) { "ulps must not be negative, was: $ulps" }
    if (ulps == 0) return this
    var head = this[0]
    repeat(ulps) { head = Math.nextUp(head) }
    return listOf(head) + drop(1)
}

/**
 * [minimaxStiffnessDistribution] re-run from starts perturbed by whole ulps, measured.
 *
 * The perturbation is applied to the **first weight of the first start** only, which is the
 * smallest change that can be made to the problem at all. Everything else — the surrogate, the
 * states, the mandate, the ceiling, the other 23 starts — is held.
 */
fun descentDegeneracy(
    surrogate: MultiStateSurrogate,
    states: List<Int>,
    totalStiffness: Double,
    starts: List<List<Double>>,
    ceiling: Double = Double.POSITIVE_INFINITY,
    ulpOffsets: List<Int> = listOf(0, 1, 2, 4, 8)
): ManifoldWidth {
    require(starts.isNotEmpty()) { "starts must not be empty" }
    require(ulpOffsets.isNotEmpty()) { "at least one offset is needed" }
    val readings = ulpOffsets.map { offset ->
        val perturbed = listOf(starts[0].perturbedByUlps(offset)) + starts.drop(1)
        val optimum = minimaxStiffnessDistribution(
            surrogate = surrogate,
            states = states,
            totalStiffness = totalStiffness,
            starts = perturbed,
            ceiling = ceiling
        )
        optimum.worstDishing to optimum.stiffnesses.max()
    }
    return manifoldWidth(readings.map { it.first }, readings.map { it.second })
}

/**
 * One start's own terminal reading, before the ensemble picks a winner.
 *
 * This is the instrument the ulp probe above turns out to need. `descentDegeneracy` measures
 * whether the *input* is what a jitter perturbs, and on this optimiser it is not — a whole ulp
 * in a start weight moves the answer by `1e−15`. What a run-to-run jitter actually perturbs is
 * the *intermediate* arithmetic, and `minimaxStiffnessDistribution` funnels every start through
 * one strict comparison: `results.minWithOrNull(compareBy(searchDecision(objective), index))`.
 * So the movement a result file shows is a change of **which start won**, and its size is set by
 * how close the near-optimal starts are in VALUE against how far apart they are in POINT.
 */
data class StartReading(
    val index: Int,
    val objective: Double,
    val peakStiffness: Double
)

/**
 * The near-optimal set of an ensemble of starts, and the width of the answers inside it.
 *
 * Every start is run **on its own**, so the readings are the terminal points the winner is
 * selected from. [tolerance] is a relative band on the objective: a start inside it is one that
 * an ulp of arithmetic jitter could plausibly promote to winner.
 */
data class NearOptimalSpread(
    val startsUsed: Int,
    val startsWithinTolerance: Int,
    val tolerance: Double,
    /** Over the starts inside [tolerance] of the best. */
    val width: ManifoldWidth,
    /** Over **every** start, whose membership no threshold can make marginal. */
    val allStartsWidth: ManifoldWidth,
    val readings: List<StartReading>
)

/** [minimaxStiffnessDistribution] run one start at a time, and the spread of the near-optimal set. */
fun nearOptimalSpread(
    surrogate: MultiStateSurrogate,
    states: List<Int>,
    totalStiffness: Double,
    starts: List<List<Double>>,
    ceiling: Double = Double.POSITIVE_INFINITY,
    tolerance: Double = 1e-2
): NearOptimalSpread {
    require(starts.isNotEmpty()) { "starts must not be empty" }
    require(tolerance > 0.0) { "tolerance must be positive, was: $tolerance" }
    val readings = starts.mapIndexed { index, start ->
        val optimum = minimaxStiffnessDistribution(
            surrogate = surrogate,
            states = states,
            totalStiffness = totalStiffness,
            starts = listOf(start),
            ceiling = ceiling
        )
        StartReading(index, optimum.worstDishing, optimum.stiffnesses.max())
    }
    val best = readings.minOf { it.objective }
    // The band test is a DECISION, so it is taken at the decision precision and not at the
    // arithmetic's: two runs of this measurement put one start's reading either side of a bare
    // `<` and the reported membership changed with it, which is the very defect being measured.
    val edge = searchDecision(best * (1.0 + tolerance))
    val near = readings.filter { searchDecision(it.objective) < edge }
    return NearOptimalSpread(
        startsUsed = starts.size,
        startsWithinTolerance = near.size,
        tolerance = tolerance,
        width = manifoldWidth(near.map { it.objective }, near.map { it.peakStiffness }),
        allStartsWidth = manifoldWidth(
            readings.map { it.objective }, readings.map { it.peakStiffness }
        ),
        readings = readings
    )
}
