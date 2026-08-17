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

package com.xemantic.nano.plentyofroom.anchoring

import com.xemantic.nano.plentyofroom.structure.Gen1Tile

/**
 * `T-136` — is there a **flat 30-root placement**, and does it keep the plan margin `C-0072`
 * bought by dissolving `C-0063`'s four forced rows of three?
 *
 * ## What is new here, and what is re-used
 *
 * The search itself is `C-0063`'s: [centroSymmetricPlacements] and [descendPlacement] both take a
 * `minimumPerRow`/`maximumPerRow` pair, so *"two per row"* is a parameter and not a new algorithm,
 * and `C-0068`'s [MultiStateRootBank] prices any subset of a phase's roots at any subset of the
 * load states. What did not exist is the arithmetic that says what such a search can possibly
 * return:
 *
 * 1. **[forcedUniformRootsPerRow]** — `C-0063`'s bound 1 (`3a + 2(D − a) = n`) read at a cap of
 *    two. At 30 roots on 15 rows it returns **2**: the count vector is not merely constrained, it
 *    is *forced*, so the design space is a product of per-row 2-subsets and the "constraint" this
 *    task imposes is an **identity**.
 * 2. **[maximumPlanCeilingForCount]** — the largest rooted element **any** placement of a given
 *    count can keep. A placement's plan ceiling is a `min` over its rows and the rows are
 *    independent, so
 *
 *    &nbsp;&nbsp;&nbsp;&nbsp;`max over placements of min over rows = sup{ L : Σ_r cap_r(L) ≥ n }`,
 *
 *    with `cap_r(L) = min(maximumPerRow, maximumRootedElementsInRow(r, L))`. The capacity is
 *    monotone in `L`, so this is a **bisection on a proof** rather than a search — and it is what
 *    shows that `C-0069`'s 8.19 nm and `C-0072`'s 9.12 nm are properties of a *placement*, not of
 *    a *count*: at 30 roots the lattice affords **9.535 nm** and at 15 it affords **30.88**.
 *
 * ## Conventions, restated rather than inherited
 *
 * Lengths **nm**, forces **pN**, stiffness **pN/nm**. `x` runs **along** the helices, `y`
 * **across** them, `z` **normal and positive upward** — away from the grafted layer. A **root** is
 * the crossover that ties one arm to its host duplex, on the unoccupied `EAST` azimuth. A rooted
 * element occupies `[root, root ± L]` and the next along the same row may start at `high + d` —
 * `C-0053`'s footprint convention, carried unchanged through [armDirections].
 */

/**
 * The per-row root count that [count] roots on [duplexes] rows **force** when no row may carry more
 * than [maximumPerRow] — or `null` where the count vector still has freedom.
 *
 * The vector is forced exactly when the count saturates the cap, `count = duplexes · maximumPerRow`,
 * because then every row must be full. At 30 roots on the Gen-1 sheet's 15 duplexes with two per
 * row that is `2 × 15 = 30`: `T-136`'s whole search space is a product of per-row 2-subsets, and
 * the two-per-row *constraint* is an *identity*. `C-0063`'s bound 1, read at the other cap.
 *
 * @throws IllegalArgumentException if the count cannot be placed on these rows at all.
 */
fun forcedUniformRootsPerRow(count: Int, duplexes: Int, maximumPerRow: Int): Int? {
    require(count >= 1) { "count must be at least one, was: $count" }
    require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
    require(maximumPerRow >= 1) { "maximumPerRow must be at least one, was: $maximumPerRow" }
    require(count <= duplexes * maximumPerRow) {
        "$count roots cannot be placed on $duplexes rows carrying at most $maximumPerRow each"
    }
    return if (count == duplexes * maximumPerRow) maximumPerRow else null
}

/**
 * The balanced count vector — `count` roots spread over [duplexes] rows as evenly as the lattice
 * allows, the larger counts first.
 *
 * At 34 on 15 rows this is `C-0063`'s bound 1 verbatim: **four** rows of three and eleven of two.
 * At 30 it is fifteen rows of two, at 45 fifteen of three, at 15 fifteen of one.
 *
 * @throws IllegalArgumentException if the count exceeds `duplexes · maximumPerRow`.
 */
fun balancedRowCounts(count: Int, duplexes: Int, maximumPerRow: Int): List<Int> {
    require(count >= 1) { "count must be at least one, was: $count" }
    require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
    require(maximumPerRow >= 1) { "maximumPerRow must be at least one, was: $maximumPerRow" }
    require(count <= duplexes * maximumPerRow) {
        "$count roots cannot be placed on $duplexes rows carrying at most $maximumPerRow each"
    }
    val base = count / duplexes
    val excess = count % duplexes
    return (0 until duplexes).map { if (it < excess) base + 1 else base }
}

/**
 * How many rooted elements of [length] the whole [lattice] can carry, at most [maximumPerRow] to a
 * row — the monotone capacity [maximumPlanCeilingForCount] bisects on.
 *
 * Exact per row, because [maximumRootedElementsInRow] enumerates the subsets in descending size
 * rather than filling greedily.
 */
fun latticeRootCapacity(
    lattice: List<List<Double>>,
    length: Double,
    edgeX: Double,
    width: Double = OrigamiDuplex.INTERHELICAL,
    maximumPerRow: Int = 3
): Int {
    require(lattice.isNotEmpty()) { "lattice must not be empty" }
    require(length > 0.0) { "length must be positive, was: $length" }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(width > 0.0) { "width must be positive, was: $width" }
    require(maximumPerRow >= 1) { "maximumPerRow must be at least one, was: $maximumPerRow" }
    return lattice.sumOf { row ->
        if (row.isEmpty()) 0
        else minOf(maximumPerRow, maximumRootedElementsInRow(row, length, edgeX, width))
    }
}

/**
 * **The largest rooted element any placement of [count] roots on [lattice] can keep** — the plan
 * ceiling as a property of the *count* and the *lattice*, with the placement maximised out.
 *
 * A placement's ceiling is the smallest of its rows' ceilings and the rows are independent, so the
 * maximum over placements is the largest `L` whose capacity still reaches the count. The capacity
 * is non-increasing in `L`, so the bisection is exact; it exits on the **bracket width** and never
 * on a residual (`CLAUDE.md`).
 *
 * Returns `null` where the lattice cannot carry [count] roots at any positive length at all.
 */
fun maximumPlanCeilingForCount(
    lattice: List<List<Double>>,
    count: Int,
    edgeX: Double,
    width: Double = OrigamiDuplex.INTERHELICAL,
    maximumPerRow: Int = 3,
    resolution: Double = 1.0e-9
): Double? {
    require(lattice.isNotEmpty()) { "lattice must not be empty" }
    require(count >= 1) { "count must be at least one, was: $count" }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(width > 0.0) { "width must be positive, was: $width" }
    require(maximumPerRow >= 1) { "maximumPerRow must be at least one, was: $maximumPerRow" }
    require(resolution > 0.0) { "resolution must be positive, was: $resolution" }
    var low = 1.0e-9 * edgeX
    if (latticeRootCapacity(lattice, low, edgeX, width, maximumPerRow) < count) return null
    var high = edgeX
    var grown = 0
    while (latticeRootCapacity(lattice, high, edgeX, width, maximumPerRow) >= count && grown < 40) {
        high *= 2.0
        grown++
    }
    require(latticeRootCapacity(lattice, high, edgeX, width, maximumPerRow) < count) {
        "every length up to $high nm carries $count roots; the lattice is unbounded"
    }
    while (high - low > resolution) {
        val middle = 0.5 * (low + high)
        if (latticeRootCapacity(lattice, middle, edgeX, width, maximumPerRow) >= count) low = middle
        else high = middle
    }
    return low
}

/** [placement] as [StationRow]s, so `C-0069`'s plan library reads it without a second convention. */
fun stationRowsOf(
    placement: UpwardArmPlacement,
    duplexes: Int,
    interhelicalDistance: Double = Gen1Tile.INTERHELICAL_SHEET
): List<StationRow> {
    require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
    require(placement.rows.all { it.row < duplexes }) {
        "every row must lie inside a $duplexes-duplex sheet"
    }
    return placement.rows.sortedBy { it.row }.map { row ->
        StationRow(row.row, (row.row - (duplexes - 1) / 2.0) * interhelicalDistance, row.roots)
    }
}

/**
 * The plan length ceiling of [placement] **itself** — what `C-0069`'s [rootedLengthCeiling] returns
 * on the rows this placement actually occupies.
 *
 * Never above [maximumPlanCeilingForCount] at the same count, and equal to it only on a
 * ceiling-optimal placement. `C-0072`'s own 30-root reduction reaches 9.12 nm where the phase-24
 * lattice affords 9.535.
 */
fun placementLengthCeiling(
    placement: UpwardArmPlacement,
    duplexes: Int,
    edgeX: Double,
    width: Double = OrigamiDuplex.INTERHELICAL,
    interhelicalDistance: Double = Gen1Tile.INTERHELICAL_SHEET,
    resolution: Double = 1.0e-9
): Double = rootedLengthCeiling(
    stationRowsOf(placement, duplexes, interhelicalDistance), edgeX, width, resolution
)
