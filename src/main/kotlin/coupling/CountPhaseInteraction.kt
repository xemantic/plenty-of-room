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
import kotlin.math.ln

/**
 * `T-178` — the **count/phase interaction** on the upward crossover lattice.
 *
 * ## What is new here, and it is three small objects
 *
 * `C-0103` measured the count axis at fixed station geometry at **one** crossover phase and then
 * defended a standing recommendation by splitting the move it recommends into *"a count term of
 * +12.86 % and a phase term of −19.0 %"*. That split is a **subtraction**, and a subtraction is a
 * decomposition only if the two axes do not interact. Nothing in the corpus bounds the
 * interaction: `C-0098` measures the phase axis on a different topology and `C-0089` the count
 * axis on an abstract grid.
 *
 * So this file supplies three things no upstream source has:
 *
 * 1. [centralRootPlacement] and [canonicalRootChain] — a **search-free** nested family that is
 *    the *same construction* at every crossover phase. `CH-0119` is this programme's own finding
 *    that a placement-**searched** family measures the search rather than the count, and `C-0102`
 *    that a descent compared against an exhaustive enumeration is not a comparison; a
 *    construction that contains no search at all is immune to both.
 * 2. [twoWayLogInteraction] — the balanced two-way additive fit of `log p90` over a
 *    phase × count grid, whose **residual** is the interaction. On a separable grid it is exactly
 *    zero, which is what makes it a falsifier rather than a description.
 * 3. [countPhaseSplit] — the same statement on the 2 × 2 the recommendation actually moves
 *    through. The two orderings (count then phase, phase then count) share their endpoints, so
 *    their **totals agree identically** and their **splits differ by exactly the interaction** —
 *    which is why the headline question costs **four** graded cells and not the whole grid.
 *
 * ## Conventions, restated rather than inherited
 *
 * Lengths **nm**; a 90th-percentile dishing is a **ratio of the free-tile stroke** and therefore
 * dimensionless, and so is every term of a split, which is a log ratio. `x` runs **along** the
 * helices and the origin is the tile centre. A **root** is the crossover that ties one arm to its
 * host duplex; a placement is a set of roots, one list per duplex row, ascending in `x`.
 */

// ------------------------------------------------------------------ the search-free family

/**
 * One root per row of [sites]: the site nearest the tile centre, the lower `x` breaking a tie.
 *
 * This is the anchor of the **canonical** family, and its whole purpose is that it is a function
 * of the lattice and of nothing else — no load, no objective, no descent — so that the same
 * construction can be run at all 32 crossover phases and a difference between two of its members
 * is a difference of phase or of count.
 *
 * @throws IllegalArgumentException if [sites] is empty or any row carries no site.
 */
fun centralRootPlacement(sites: List<List<Double>>): List<List<Double>> {
    require(sites.isNotEmpty()) { "sites must not be empty" }
    require(sites.all { it.isNotEmpty() }) {
        "every row must carry at least one site, and row " +
                "${sites.indexOfFirst { it.isEmpty() }} carries none"
    }
    return sites.map { row ->
        listOf(row.minWithOrNull(compareBy({ abs(it) }, { it }))!!)
    }
}

/**
 * The **canonical, search-free** [NestedRootChain] of a lattice: `C-0103`'s own addition rule run
 * from [centralRootPlacement], so that every count from one root per row up to [maximumPerRow]
 * per row is a member of one totally ordered family.
 *
 * Nestedness is `C-0103`'s gate and is re-asserted here at every phase, because a count sweep
 * over sets that are not nested is not a count sweep.
 */
fun canonicalRootChain(
    sites: List<List<Double>>,
    maximumPerRow: Int = 3
): NestedRootChain = nestedRootChain(
    sites = sites,
    anchor = centralRootPlacement(sites),
    symmetric = false,
    maximumPerRow = maximumPerRow,
    minimumPerRow = 1
)

// ------------------------------------------------------------------ the two-way additive fit

/**
 * A balanced two-way additive fit of `log(value)` over a complete `rows × columns` grid — here
 * **phase × count** — whose residual is the interaction of the two factors.
 *
 * The decomposition is orthogonal on a balanced complete design, so
 * `total = rows + columns + interaction` holds **identically** and is asserted as a gate rather
 * than reported as a fit quality.
 */
data class TwoWayInteraction(

    /** How many levels the first factor has. */
    val rows: Int,

    /** How many levels the second factor has. */
    val columns: Int,

    /** The mean of `log(value)` over the whole grid. */
    val grandMean: Double,

    /** The first factor's main effects, summing to zero. */
    val rowEffects: List<Double>,

    /** The second factor's main effects, summing to zero. */
    val columnEffects: List<Double>,

    /** `log(value) − grandMean − rowEffect − columnEffect`, cell by cell. */
    val residuals: List<List<Double>>,

    /** The largest absolute residual — **the interaction, in log units**. */
    val worstResidual: Double,

    /** `columns × Σ rowEffect²`. */
    val rowSumOfSquares: Double,

    /** `rows × Σ columnEffect²`. */
    val columnSumOfSquares: Double,

    /** `Σ residual²`. */
    val interactionSumOfSquares: Double,

    /** `Σ (log(value) − grandMean)²`. */
    val totalSumOfSquares: Double
) {

    /** The share of the total variation the interaction carries. */
    val interactionShare: Double
        get() = if (totalSumOfSquares > 0.0) interactionSumOfSquares / totalSumOfSquares else 0.0

    /** The largest absolute residual as a **per cent** of the level it multiplies. */
    val worstResidualPerCent: Double
        get() = 100.0 * (kotlin.math.exp(worstResidual) - 1.0)

}

/**
 * [TwoWayInteraction] over [values], indexed `[row][column]` and strictly positive.
 *
 * @throws IllegalArgumentException if the grid is ragged, has fewer than two levels of either
 *   factor, or carries a value that is not strictly positive.
 */
fun twoWayLogInteraction(values: List<List<Double>>): TwoWayInteraction {
    require(values.size >= 2) { "a two-way fit needs at least two rows, had: ${values.size}" }
    val columns = values[0].size
    require(columns >= 2) { "a two-way fit needs at least two columns, had: $columns" }
    require(values.all { it.size == columns }) {
        "the grid must be complete and rectangular, and its rows are ${values.map { it.size }}"
    }
    require(values.all { row -> row.all { it > 0.0 } }) {
        "every value must be strictly positive, because the fit is on its logarithm"
    }
    val rows = values.size
    val logs = values.map { row -> row.map { ln(it) } }
    val cells = (rows * columns).toDouble()
    val grandMean = logs.sumOf { it.sum() } / cells
    val rowEffects = logs.map { it.sum() / columns - grandMean }
    val columnEffects = (0 until columns).map { column ->
        logs.sumOf { it[column] } / rows - grandMean
    }
    val residuals = (0 until rows).map { row ->
        (0 until columns).map { column ->
            logs[row][column] - grandMean - rowEffects[row] - columnEffects[column]
        }
    }
    return TwoWayInteraction(
        rows = rows,
        columns = columns,
        grandMean = grandMean,
        rowEffects = rowEffects,
        columnEffects = columnEffects,
        residuals = residuals,
        worstResidual = residuals.maxOf { row -> row.maxOf { abs(it) } },
        rowSumOfSquares = columns * rowEffects.sumOf { it * it },
        columnSumOfSquares = rows * columnEffects.sumOf { it * it },
        interactionSumOfSquares = residuals.sumOf { row -> row.sumOf { it * it } },
        totalSumOfSquares = logs.sumOf { row ->
            row.sumOf { (it - grandMean) * (it - grandMean) }
        }
    )
}

// ------------------------------------------------------------------ the 2 x 2 path split

/**
 * The two orderings of one `(count, phase)` move, in log units.
 *
 * `C-0103` reads the programme's 34 → 30 recommendation as *"a count term and a phase term"*.
 * There are two such readings — take the count first, or take the phase first — and they are the
 * same journey between the same two designs. So [total] is identical for both, while
 * [countTermAtFromPhase] and [countTermAtToPhase] need not be: their difference **is** the
 * interaction, and it is the same number as the difference of the two phase terms.
 *
 * A `pathDisagreement` above the floating-point floor is an arithmetic error, never a result.
 */
data class CountPhaseSplit(

    /** `p90` at the starting count and the starting phase. */
    val fromCountFromPhase: Double,

    /** `p90` at the ending count and the starting phase. */
    val toCountFromPhase: Double,

    /** `p90` at the starting count and the ending phase. */
    val fromCountToPhase: Double,

    /** `p90` at the ending count and the ending phase — the design recommended. */
    val toCountToPhase: Double,

    /** `ln(end/start)`, which both orderings must reproduce. */
    val total: Double,

    /** The count term taken **first**, at the starting phase — `C-0103`'s +12.86 %. */
    val countTermAtFromPhase: Double,

    /** The phase term taken **second**, at the ending count — `C-0103`'s −19.0 %. */
    val phaseTermAtToCount: Double,

    /** The phase term taken **first**, at the starting count. */
    val phaseTermAtFromCount: Double,

    /** The count term taken **second**, at the ending phase. */
    val countTermAtToPhase: Double,

    /** `countTermAtToPhase − countTermAtFromPhase`, identically the difference of phase terms. */
    val interaction: Double,

    /** `|(count first + phase second) − (phase first + count second)|` — an arithmetic check. */
    val pathDisagreement: Double
) {

    /** The interaction as a **per cent** of the level it multiplies. */
    val interactionPerCent: Double
        get() = 100.0 * (kotlin.math.exp(interaction) - 1.0)

    /** Whether the split is well posed to within [tolerance] of a log unit. */
    fun separableWithin(tolerance: Double): Boolean = abs(interaction) < tolerance

}

/** [CountPhaseSplit] of the four corners of one `(count, phase)` 2 × 2. */
fun countPhaseSplit(
    fromCountFromPhase: Double,
    toCountFromPhase: Double,
    fromCountToPhase: Double,
    toCountToPhase: Double
): CountPhaseSplit {
    val corners = listOf(
        fromCountFromPhase, toCountFromPhase, fromCountToPhase, toCountToPhase
    )
    require(corners.all { it > 0.0 }) {
        "every corner of the 2 x 2 must be a strictly positive percentile, were: $corners"
    }
    val countFirst = ln(toCountFromPhase / fromCountFromPhase)
    val phaseSecond = ln(toCountToPhase / toCountFromPhase)
    val phaseFirst = ln(fromCountToPhase / fromCountFromPhase)
    val countSecond = ln(toCountToPhase / fromCountToPhase)
    return CountPhaseSplit(
        fromCountFromPhase = fromCountFromPhase,
        toCountFromPhase = toCountFromPhase,
        fromCountToPhase = fromCountToPhase,
        toCountToPhase = toCountToPhase,
        total = ln(toCountToPhase / fromCountFromPhase),
        countTermAtFromPhase = countFirst,
        phaseTermAtToCount = phaseSecond,
        phaseTermAtFromCount = phaseFirst,
        countTermAtToPhase = countSecond,
        interaction = countSecond - countFirst,
        pathDisagreement = abs((countFirst + phaseSecond) - (phaseFirst + countSecond))
    )
}
