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

/**
 * A contiguous run of admissible indices on an ascending grid.
 *
 * `T-2` intersects constraints on the 61-point logarithmic grafting-density grid `T-1d`
 * ran, so every window edge is a **grid point** and its resolution is one grid ratio
 * (1.107 here). That is stated as [edgeResolution] rather than left implicit, because a
 * design window read at a bench is exactly the artifact whose edges get quoted to more
 * digits than they have.
 */
@Serializable
data class GridInterval(
    val lowestIndex: Int,
    val highestIndex: Int
) {

    init {
        require(lowestIndex >= 0) { "lowestIndex must not be negative, was: $lowestIndex" }
        require(highestIndex >= lowestIndex) {
            "highestIndex must not precede lowestIndex, was: $highestIndex < $lowestIndex"
        }
    }

    /** The number of grid points inside the interval — at least one. */
    val count: Int get() = highestIndex - lowestIndex + 1

    /** The grid value at the lower edge. */
    fun lowest(grid: List<Double>): Double = grid[lowestIndex]

    /** The grid value at the upper edge. */
    fun highest(grid: List<Double>): Double = grid[highestIndex]

    /**
     * The window's width as a **ratio** of its edges, not a difference.
     *
     * A grafting-density window spans decades, and "22.4× wide" is the statement a
     * process engineer can act on; "0.249 nm⁻² wide" is not.
     */
    fun width(grid: List<Double>): Double = grid[highestIndex] / grid[lowestIndex]

    /**
     * The ratio between neighbouring grid points at the lower edge — the resolution to
     * which the edge is located, and the honest error bar on it.
     */
    fun edgeResolution(grid: List<Double>): Double =
        if (lowestIndex + 1 < grid.size) grid[lowestIndex + 1] / grid[lowestIndex]
        else grid[lowestIndex] / grid[lowestIndex - 1]

}

/**
 * Returns the single contiguous run of `true` in [flags], or `null` if there is none.
 *
 * @throws IllegalStateException if the admissible set has a **hole**. This is declared
 * falsifier 2 of `T-2`'s Plan: the deliverable is an interval, so a non-contiguous
 * admissible set is not a narrower answer but the wrong object, and reporting an interval
 * would hide the hole. It throws rather than warning.
 */
fun admissibleInterval(flags: List<Boolean>): GridInterval? {
    val first = flags.indexOfFirst { it }
    if (first < 0) return null
    val last = flags.indexOfLast { it }
    for (index in first..last) {
        check(flags[index]) {
            "the admissible set is not contiguous: index $index is inadmissible " +
                    "inside the run [$first, $last]. An interval is the wrong object for it."
        }
    }
    return GridInterval(first, last)
}

/** Returns the intersection of two admissible intervals, or `null` if they are disjoint. */
fun intersect(first: GridInterval?, second: GridInterval?): GridInterval? {
    if (first == null || second == null) return null
    val lowest = maxOf(first.lowestIndex, second.lowestIndex)
    val highest = minOf(first.highestIndex, second.highestIndex)
    return if (lowest > highest) null else GridInterval(lowest, highest)
}

/**
 * The window and, for each of its two edges, the constraint or constraints that close it.
 *
 * [lowerTie] and [upperTie] are `true` when more than one constraint sits on the edge.
 * `T-2`'s Plan declares that case a falsifier of the *attribution*, not of the window:
 * the method promises a binding constraint per edge, and where two coincide the honest
 * report is a tie rather than a name.
 */
@Serializable
data class EdgeAttribution(
    val window: GridInterval,
    val lowerBinding: List<String>,
    val upperBinding: List<String>,
    val lowerTie: Boolean,
    val upperTie: Boolean
)

/**
 * Intersects [intervals] and attributes each edge of the result, or returns `null` if the
 * intersection is empty.
 *
 * The attribution is an **index** comparison, not a comparison of doubles, so it is exact
 * and cannot move between runs. That matters here: `CLAUDE.md` records that rounding at
 * the serialisation boundary does not make a file reproducible if it contains an argmin.
 */
fun attributeEdges(intervals: Map<String, GridInterval?>): EdgeAttribution? {
    require(intervals.isNotEmpty()) { "intervals must not be empty" }
    val window = intervals.values.reduce(::intersect) ?: return null
    val lower = intervals.filterValues { it != null && it.lowestIndex == window.lowestIndex }
    val upper = intervals.filterValues { it != null && it.highestIndex == window.highestIndex }
    return EdgeAttribution(
        window = window,
        lowerBinding = lower.keys.toList(),
        upperBinding = upper.keys.toList(),
        lowerTie = lower.size > 1,
        upperTie = upper.size > 1
    )
}

/**
 * The two constraints whose admissible intervals fail to overlap, and by how much.
 *
 * This is the *proof of emptiness* half of §6 task 2's acceptance predicate: an empty
 * window is only an answer if it names what closed it. The [crossingRatio] is the factor
 * in grafting density by which the two demands miss each other.
 */
@Serializable
data class ConstraintCrossing(
    val lowerBoundConstraint: String,
    val upperBoundConstraint: String,
    val lowerBoundValue: Double,
    val upperBoundValue: Double,
    val crossingRatio: Double
)

/**
 * Returns the crossing that empties [intervals], or `null` if their intersection is not
 * empty. A constraint that is empty on its own closes the window by itself and is
 * reported as both ends of the crossing, with a ratio of `NaN`-free unity.
 */
fun crossingOf(
    intervals: Map<String, GridInterval?>,
    grid: List<Double>
): ConstraintCrossing? {
    require(intervals.isNotEmpty()) { "intervals must not be empty" }
    if (intervals.values.reduce(::intersect) != null) return null
    intervals.entries.firstOrNull { it.value == null }?.let { entry ->
        return ConstraintCrossing(
            lowerBoundConstraint = entry.key,
            upperBoundConstraint = entry.key,
            lowerBoundValue = Double.NaN,
            upperBoundValue = Double.NaN,
            crossingRatio = 1.0
        )
    }
    // the demand furthest to the right, against the demand furthest to the left: those
    // two are the pair that cannot be satisfied together, whatever the others do
    val lower = intervals.entries.maxBy { it.value!!.lowestIndex }
    val upper = intervals.entries.minBy { it.value!!.highestIndex }
    val lowerValue = grid[lower.value!!.lowestIndex]
    val upperValue = grid[upper.value!!.highestIndex]
    return ConstraintCrossing(
        lowerBoundConstraint = lower.key,
        upperBoundConstraint = upper.key,
        lowerBoundValue = lowerValue,
        upperBoundValue = upperValue,
        crossingRatio = lowerValue / upperValue
    )
}
