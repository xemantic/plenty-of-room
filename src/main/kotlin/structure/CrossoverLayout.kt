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

package com.xemantic.nano.plentyofroom.structure

import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor
import kotlin.math.roundToLong

/**
 * Where the crossover columns of a single-layer sheet sit along the helices, and which
 * parity of interface each of them serves.
 *
 * ## Why this is a type and not an integer
 *
 * `T-10` parameterised the lattice by a **count** of crossover columns, symmetrically
 * centred on the footprint, and discovered that moving from eight columns to seven moved the
 * peak per-load-path force by 19 %. But a count is not the design variable. The column
 * lattice has a fixed pitch — `p/2`, half the per-interface crossover spacing, because
 * crossovers alternate between a helix's two neighbours — and what a staple layout actually
 * chooses is the **phase** of that lattice relative to the tile edge. The count is a
 * *consequence*: a 40 nm tile spans `40/5.44 = 7.35` column pitches, so a phase that lets
 * eight columns fit inside the footprint gives eight, and a phase that pushes one off the
 * edge gives seven. Sweeping the phase sweeps both effects at once, continuously, and
 * without ever comparing two lattices that differ in more than one thing.
 *
 * ## The period is `p`, not `p/2`
 *
 * This is the trap. Shifting the column lattice by **one column pitch** `p/2` leaves the set
 * of column *positions* inside the footprint unchanged, so the geometry looks identical —
 * but it hands every interface the other parity's columns, which is a physically different
 * sheet. Only a shift by `p = 32 bp`, one full per-interface spacing, is the identity.
 * A phase sweep over `[0, p/2)` therefore covers **half** the design space, and it is the
 * exact analogue of the per-helix / per-interface confusion that doubles the across-helix
 * rigidity if it goes unnoticed.
 *
 * ## The phase is quantised to base pairs
 *
 * A staple can only cross over at a base pair, so the phase is not a continuous variable:
 * it takes exactly [BASE_PAIRS_PER_PERIOD] values. That makes a *complete* sweep possible
 * rather than a sampled one.
 *
 * @param positions the `x` of each column in nm, strictly ascending, centred on the footprint.
 * @param parities the parity of each column's index in the underlying infinite column
 *          lattice — `0` or `1`. Interface `b` carries the columns whose parity matches
 *          `b mod 2`. Held explicitly rather than taken from the position in this list,
 *          because a column dropping off the tile edge must not silently swap every
 *          interface's parity.
 */
data class CrossoverLayout(
    val positions: List<Double>,
    val parities: List<Int>
) {

    init {
        require(positions.size >= 2) {
            "a lattice needs at least two crossover columns, was: ${positions.size}"
        }
        require(positions.size == parities.size) {
            "positions and parities must have the same size, were: " +
                    "${positions.size} and ${parities.size}"
        }
        require(parities.all { it == 0 || it == 1 }) {
            "a column parity must be 0 or 1, were: $parities"
        }
        require(positions.zipWithNext().all { (a, b) -> b > a }) {
            "the column positions must strictly ascend, were: $positions"
        }
    }

    /** The number of columns. */
    val size: Int get() = positions.size

    /** The number of columns of parity [parity]. */
    fun countOfParity(parity: Int): Int = parities.count { it == parity }

    companion object {

        /**
         * The number of distinct phases the column lattice has, which is the per-interface
         * crossover spacing in base pairs — `32` for a Rothemund single-layer sheet.
         *
         * Not `16`. See the class documentation.
         */
        val BASE_PAIRS_PER_PERIOD: Int = Gen1Tile.CROSSOVER_SPACING_SHEET_BP.toInt()

        /**
         * The margin in nm inside the footprint edge within which a column is not placed.
         *
         * A column exactly on the edge would seed a zero-length beam element.
         *
         * **The guard is inert exactly where the slack past the last pitch,
         * `(lengthX − 2·margin) mod columnSpacing`, stays clear of zero by more than the range
         * of margins a design might use — and it is NOT inert everywhere.** It was documented
         * here as *"far below the 0.28 nm closest approach any base-pair phase makes on a 40 nm
         * tile"*, which was a statement about **one** geometry and has since failed at two
         * others: `C-0134` found it deleting two of eight columns at the square lattice's
         * buildable 38.08 nm, and `C-0146`/`C-0148` found it admitting a **twelfth** honeycomb
         * crossover column on **0.07 nm** of slack — one fifth of a base-pair rise — at the
         * four-layer block's 39.44 nm bounding box, worth six flat coupled cells of eight
         * against three.
         *
         * So quote the slack beside any column count read at a new extent.
         * `tile.columnSlack` and `tile.guardIsInert` compute the condition, and
         * `tile.crossoverColumnsIn` takes the **window** as its parameter rather than a tile
         * dimension — because on a staggered row lattice the count belongs to the window two
         * adjacent rows share and not to the block's bounding box (`C-0148`).
         */
        const val EDGE_MARGIN: Double = 0.05

        /**
         * The symmetrically centred layout of [count] columns at pitch [columnSpacing] —
         * `T-10`'s construction, reproduced exactly so that nothing already published moves.
         */
        fun centred(count: Int, columnSpacing: Double): CrossoverLayout {
            require(count >= 2) { "count must be at least 2, was: $count" }
            require(columnSpacing > 0.0) {
                "columnSpacing must be positive, was: $columnSpacing"
            }
            return CrossoverLayout(
                positions = (0 until count).map { (it - (count - 1) / 2.0) * columnSpacing },
                parities = (0 until count).map { it % 2 }
            )
        }

        /**
         * The layout of the infinite column lattice `x = phase + k · columnSpacing`,
         * truncated to the columns lying strictly inside a footprint of length [lengthX]
         * centred on the origin.
         *
         * The parity carried is `k mod 2`, the index in the *infinite* lattice, so that a
         * column leaving the footprint changes the count without changing which interface
         * any surviving column serves.
         */
        fun phased(
            phase: Double,
            columnSpacing: Double,
            lengthX: Double
        ): CrossoverLayout {
            require(columnSpacing > 0.0) {
                "columnSpacing must be positive, was: $columnSpacing"
            }
            require(lengthX > 0.0) { "lengthX must be positive, was: $lengthX" }
            val half = lengthX / 2.0 - EDGE_MARGIN
            require(half > 0.0) { "lengthX must exceed twice the edge margin, was: $lengthX" }
            val first = ceil((-half - phase) / columnSpacing).toInt()
            val last = floor((half - phase) / columnSpacing).toInt()
            val indices = first..last
            return CrossoverLayout(
                positions = indices.map { phase + it * columnSpacing },
                parities = indices.map { Math.floorMod(it, 2) }
            )
        }

        /**
         * The layout at a phase of [basePairs] base pairs, for [sheet] on a footprint of
         * [lengthX] nm — the design variable as a staple layout can actually set it.
         */
        fun atBasePairPhase(
            basePairs: Int,
            sheet: OrigamiSheet,
            lengthX: Double
        ): CrossoverLayout = phased(
            phase = basePairs * Gen1Tile.RISE_PER_BASE_PAIR,
            columnSpacing = sheet.crossoverSpacing / 2.0,
            lengthX = lengthX
        )
    }

}

/**
 * The offsets, in units of the interhelical distance, of the rows of an inset
 * [rows] × [rows] attachment grid from the nearest duplex axis.
 *
 * The grid `insetGrid` lays down is `y_j = −L_y/2 + L_y (j + ½)/rows` over a footprint of
 * `L_y = duplexes × d`, so in units of `d` measured from the first duplex axis the `j`-th
 * row sits at `duplexes (j + ½)/rows − ½`, and its offset from the nearest axis is the
 * distance of that from the nearest integer. Every quantity here is therefore **pure
 * arithmetic in `rows` and `duplexes`** — no elasticity, no foundation stiffness, no solve.
 * That is what makes the commensurability explanation independent of everything `T-1c` and
 * `T-9` are still moving.
 *
 * @throws IllegalArgumentException if either argument is not positive.
 */
fun attachmentRowOffsets(rows: Int, duplexes: Int): List<Double> {
    require(rows > 0) { "rows must be positive, was: $rows" }
    require(duplexes > 0) { "duplexes must be positive, was: $duplexes" }
    return (0 until rows).map { j ->
        val position = duplexes * (j + 0.5) / rows - 0.5
        position - Math.round(position)
    }
}

/**
 * The spread of the attachment row offsets, `(max|o| − min|o|)/½`, in `0..1`.
 *
 * Zero when every attachment sits at the same distance from a duplex axis — the
 * **commensurate** case, in which every attachment loads the sheet identically. One when
 * the offsets run from an attachment sitting exactly on an axis to one sitting exactly at
 * an interface, which is the largest possible disparity in how the load enters the lattice.
 *
 * This is the metric that explains the non-monotone flatness curve `C-0009` reported.
 */
fun attachmentOffsetSpread(rows: Int, duplexes: Int): Double {
    val offsets = attachmentRowOffsets(rows, duplexes).map { abs(it) }
    return (offsets.max() - offsets.min()) / 0.5
}

/**
 * The number of distinct row offsets, which is `rows / gcd(rows, duplexes)`.
 *
 * Counted rather than asserted from the arithmetic identity, so the identity itself stays
 * falsifiable by the tests.
 */
fun distinctAttachmentOffsets(rows: Int, duplexes: Int): Int =
    attachmentRowOffsets(rows, duplexes)
        .map { (it * 1e9).roundToLong() }
        .distinct()
        .size
