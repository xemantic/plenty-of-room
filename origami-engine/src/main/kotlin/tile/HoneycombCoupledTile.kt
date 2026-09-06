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

import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.coupling.orderStatistic
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import kotlin.math.abs

/**
 * `T-232` — re-grading `C-0118`'s coupled cells at the corrected honeycomb cross-section.
 *
 * ## What this file is, and what it deliberately is not
 *
 * `C-0141` established the cross-section: a honeycomb's in-plane row pitch is `3d/2`, its layer
 * pitch is `d√3/2`, and only their **product** is the cell `3√3/4·d²`. Every geometric quantity
 * this task needs is therefore consumed from [HoneycombCrossSectionGeometry], [HoneycombBlock]
 * and [honeycombStationLattice] rather than re-derived here — re-deriving it is how two claims
 * end up disagreeing about the same lattice.
 *
 * What is genuinely new is two small things:
 *
 * 1. **[honeycombSnappedGrid]** — the abstract `columns × rows` attachment grid every four-layer
 *    claim in this corpus is graded on, *realised on the honeycomb face's own station lattice*.
 *    `C-0118` states plainly that its path counts are a **request** and not a demonstration that
 *    the stations exist; `C-0141` supplies the stations, and this is the one step between them.
 * 2. **[pairedRatioSummary]** — the cost of the geometry read **per realisation** on the shared
 *    dropout stream. `CLAUDE.md`: *"a ratio of two ORDER STATISTICS is not the order statistic of
 *    the ratio, and here it is 5× too big."*
 *
 * ## Conventions
 *
 * Lengths **nm**, stiffness **pN/nm**, dishing dimensionless as a fraction of the free-tile
 * stroke. `x` runs **along** the helices, `y` **across** them; the origin is the tile centre.
 * A station row **is** a rooting helix, so the across-helix coordinate is fixed by the lattice
 * and only the along-helix coordinate is ever chosen.
 */

/**
 * The abstract `columns × rows` attachment grid, snapped onto the honeycomb face's own station
 * lattice — each station moved along its helix to the **nearest** ladder position of its row.
 *
 * The rows are the rooting helices, so `y` is untouched and is exactly [attachmentGrid]'s. The
 * along-helix ladder has period 21 bp with the adjacent-row stagger `C-0141` shows is **forced**,
 * so two rows' stations are never in register and the snap is per row.
 *
 * A snap that collided two of one row's stations would be a change of the path **count** wearing
 * a change of position, so it is refused rather than silently returned.
 *
 * @param columns the along-helix stations per rooting helix.
 * @param rootingHelices the face helices carrying a station row — `m` for an `m × n` block.
 * @param rowBasePairs the row length along the helices.
 * @param edgeY the plate's across-helix extent in nm — [HoneycombBlock.plateEdgeY].
 * @param basePhaseBasePairs the ladder phase of the even rows.
 * @param interRowOffsetBasePairs the forced stagger, 7 or 14 bp (`C-0141` carries both).
 */
fun honeycombSnappedGrid(
    columns: Int,
    rootingHelices: Int,
    rowBasePairs: Int,
    edgeY: Double,
    basePhaseBasePairs: Int = 0,
    interRowOffsetBasePairs: Int = 7,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR
): List<Pair<Double, Double>> {
    require(columns > 0) { "columns must be positive, was: $columns" }
    require(rootingHelices >= 1) { "rootingHelices must be at least 1, was: $rootingHelices" }
    require(edgeY > 0.0) { "edgeY must be positive, was: $edgeY" }
    val lattice = honeycombStationLattice(
        rootingHelices, rowBasePairs, basePhaseBasePairs, interRowOffsetBasePairs, risePerBasePair
    )
    val edgeX = rowBasePairs * risePerBasePair
    val abstract = attachmentGrid(columns, rootingHelices, edgeX, edgeY)
    return abstract.mapIndexed { index, (x, y) ->
        val row = index / columns
        val stations = lattice[row]
        require(columns <= stations.size) {
            "a $columns-column placement cannot stand on a ladder of ${stations.size} stations " +
                    "— that is a change of the path COUNT, not of the position"
        }
        stations.minBy { abs(it - x) } to y
    }.also { snapped ->
        (0 until rootingHelices).forEach { row ->
            val xs = (0 until columns).map { snapped[row * columns + it].first }
            require(xs.toSet().size == columns) {
                "snapping row $row collided two of its $columns stations onto one ladder " +
                        "position, which changes the path count rather than the placement"
            }
        }
    }
}

/**
 * The largest distance **along the helices** any station of [from] had to move to become the
 * corresponding station of [to], in nm.
 *
 * It is bounded above by half the ladder pitch by construction whenever [to] is a nearest-station
 * snap, which is what makes it quotable as a placement cost rather than as a residual.
 */
fun alongHelixDeparture(
    from: List<Pair<Double, Double>>,
    to: List<Pair<Double, Double>>
): Double {
    require(from.isNotEmpty()) { "from must not be empty" }
    require(from.size == to.size) {
        "the two placements carry ${from.size} and ${to.size} stations"
    }
    return from.indices.maxOf { abs(from[it].first - to[it].first) }
}

/**
 * The distribution of the **per-realisation** ratio of two samples drawn on one common stream.
 *
 * Every field is an order statistic **of the ratio**, never a ratio of order statistics.
 */
data class PairedRatioSummary(

    /** How many paired realisations the summary is over. */
    val realisations: Int,

    /** The 50th percentile of the per-realisation ratio. */
    val median: Double,

    /** The 90th percentile of the per-realisation ratio. */
    val p90: Double,

    /** The largest per-realisation ratio. */
    val worst: Double,

    /** The smallest per-realisation ratio. */
    val best: Double,

    /** The fraction of realisations in which the numerator exceeded the denominator. */
    val fractionAbove: Double,

    /** The ratio of the two samples' 90th percentiles — the **unpaired** reading, for contrast. */
    val ratioOfPercentiles: Double
)

/**
 * [numerator] over [denominator], realisation by realisation.
 *
 * The two samples must be the same length and must have been drawn on the **same** stream, which
 * is what makes the pairing meaningful; a zero denominator is refused rather than producing an
 * infinity, because a dishing of exactly zero is a degenerate solve and not a small number.
 */
fun pairedRatioSummary(
    numerator: DoubleArray,
    denominator: DoubleArray
): PairedRatioSummary {
    require(numerator.isNotEmpty()) { "the sample must not be empty" }
    require(numerator.size == denominator.size) {
        "the two samples carry ${numerator.size} and ${denominator.size} realisations"
    }
    require(denominator.all { it != 0.0 }) {
        "a zero denominator has no ratio — the paired reading is undefined there"
    }
    val ratios = DoubleArray(numerator.size) { numerator[it] / denominator[it] }
    return PairedRatioSummary(
        realisations = ratios.size,
        median = orderStatistic(ratios, 0.50),
        p90 = orderStatistic(ratios, 0.90),
        worst = ratios.max(),
        best = ratios.min(),
        fractionAbove = ratios.count { it > 1.0 }.toDouble() / ratios.size,
        ratioOfPercentiles = orderStatistic(numerator, 0.90) / orderStatistic(denominator, 0.90)
    )
}
