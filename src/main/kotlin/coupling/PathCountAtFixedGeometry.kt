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

import com.xemantic.nano.plentyofroom.anchoring.armDirections
import com.xemantic.nano.plentyofroom.anchoring.OrigamiDuplex
import kotlin.math.abs
import kotlin.math.min

/**
 * `T-163` — the path-count sweep at **fixed station geometry** on the upward lattice, which is
 * what settles [`CH-0103`](../../../../../gpd/challenges/CH-0103-the-path-count-recommendation-runs-against-fabrication.md).
 *
 * ## What is new here, and it is two words
 *
 * `C-0089` sweeps the attachment count under `C-0087`'s measured dropout and finds the 90th
 * percentile monotone in it — **on the abstract `m × 15` grid**. On the upward lattice the count
 * and the placement are confounded, and `C-0089`'s own numbers show it: `C-0074`'s 30 roots read
 * better than `C-0063`'s 34 because they sit at a different phase. `C-0098` then measured that
 * the transfer onto the real lattice is not benign — the redundancy slope is 2.08× shallower and
 * the cheap ranking instrument falls from ρ = 0.97 across designs to 0.47 across phases.
 *
 * So this file supplies two things `C-0089`'s pipeline does not have:
 *
 * 1. **A NESTED family** ([nestedRootChain]) — station sets that are literally subsets of one
 *    another, anchored on `C-0063`'s own 34 roots and passing through `C-0072`'s own 30-root
 *    reduction of them, so that a count sweep moves the count and nothing else. The construction
 *    is `C-0072`'s `rowsWithoutInteriorRoots` rule and its exact inverse, so the two members that
 *    carry standing verdicts are the published designs and not reconstructions of them.
 * 2. **COMMON RANDOM NUMBERS** ([restrictEnsemble]) — one Bernoulli stream over the whole site
 *    inventory, restricted to each subset, so two nested designs see the same staple present or
 *    absent at every station they share. `C-0089` draws a separate ensemble per station set,
 *    which is correct and which buries a few-per-cent difference between two counts inside the
 *    sampling noise of two independent samples.
 *
 * ## Conventions, restated rather than inherited
 *
 * Lengths **nm**, forces **pN**, stiffness **pN/nm**; probabilities and dishing-over-stroke
 * ratios are dimensionless. `x` runs **along** the helices, `y` **across** them; the origin is
 * the tile centre. A **dropout is a removal**, not a perturbation (`C-0087`). A **root** is the
 * crossover that ties one arm to its host duplex, and a placement is a set of roots, one list
 * per duplex row, ascending in `x`.
 */

// ------------------------------------------------------------------ common random numbers

/**
 * [parent] restricted to the stations at [indices], **sharing its stream** — the presence flag of
 * a retained station is the parent's own flag for that station, realisation for realisation.
 *
 * This is what makes two nested designs comparable: the difference between their percentiles is
 * then a difference of designs and not of samples. It is the standard common-random-numbers
 * variance reduction, and it is exact here because a dropout is drawn per station and the
 * stations of a subset are stations of the parent.
 *
 * @throws IllegalArgumentException if [indices] is empty, repeats, or names a station the parent
 *   does not carry.
 */
fun restrictEnsemble(parent: DropoutEnsemble, indices: List<Int>): DropoutEnsemble {
    require(indices.isNotEmpty()) { "indices must not be empty" }
    require(indices.distinct().size == indices.size) { "the indices must be distinct: $indices" }
    require(indices.all { it in 0 until parent.pathCount }) {
        "every index must name one of the parent's ${parent.pathCount} stations, were: $indices"
    }
    return DropoutEnsemble(
        probabilities = indices.map { parent.probabilities[it] },
        seed = parent.seed,
        patterns = (0 until parent.realisations).map { realisation ->
            val pattern = parent.presenceAt(realisation)
            indices.map { pattern[it] }
        }
    )
}

// ------------------------------------------------------------------ the nested count chain

/**
 * A family of root placements on one crossover phase, **totally ordered by inclusion**, anchored
 * on a published placement.
 *
 * [at] returns the member of the family carrying a given count, and the members satisfy
 * `at(a) ⊆ at(b)` for every `a ≤ b` — asserted as a gate rather than inferred from the
 * construction, because a "count sweep" over sets that are not nested is not a count sweep.
 */
class NestedRootChain internal constructor(

    /** The lattice's own sites, row by row and ascending in `x`. */
    val sites: List<List<Double>>,

    /** The placement the family is anchored on — `C-0063`'s 34 roots, at [anchorCount]. */
    val anchor: List<List<Double>>,

    /** Whether the family is built in mirror row pairs, preserving centro-symmetry. */
    val symmetric: Boolean,

    /** The smallest count the family reaches — one root in every row. */
    val minimumCount: Int,

    /** The largest count the family reaches — `maximumPerRow` roots wherever the lattice has them. */
    val maximumCount: Int,

    private val removals: List<Pair<Int, Double>>,
    private val additions: List<Pair<Int, Double>>,
    private val tolerance: Double
) {

    /** The count of the anchor placement. */
    val anchorCount: Int = anchor.sumOf { it.size }

    /**
     * The member of the family carrying [count] roots.
     *
     * @throws IllegalArgumentException if [count] is outside `[minimumCount, maximumCount]`.
     */
    fun at(count: Int): List<List<Double>> {
        require(count in minimumCount..maximumCount) {
            "count must lie in [$minimumCount, $maximumCount], was: $count"
        }
        val rows = anchor.map { it.toMutableList() }
        when {
            count < anchorCount -> repeat(anchorCount - count) { step ->
                val (row, x) = removals[step]
                val index = rows[row].indexOfFirst { abs(it - x) <= tolerance }
                check(index >= 0) { "the removal order names a root row $row does not carry: $x" }
                rows[row].removeAt(index)
            }

            count > anchorCount -> repeat(count - anchorCount) { step ->
                val (row, x) = additions[step]
                rows[row].add(x)
                rows[row].sort()
            }
        }
        return rows.map { it.toList() }
    }

}

/**
 * The [NestedRootChain] anchored on [anchor], built by `C-0072`'s own interior-root rule and its
 * exact inverse.
 *
 * **Removal** (`C-0072`'s `rowsWithoutInteriorRoots`, restated so that no second reading of it can
 * drift): take from the row that currently carries the most roots — lowest row index breaks the
 * tie — and within it the root nearest the row's own mean, lowest `x` breaking that tie. It is
 * the interior root of a row of three, whose removal is what dissolves the row of three that
 * `C-0069`'s 8.19 nm plan ceiling is bought by.
 *
 * **Addition** is its mirror image: give to the row that currently carries the fewest and has an
 * unused site — lowest row index breaks the tie — and within it the unused site nearest the row's
 * own mean, lowest `x` breaking that tie.
 *
 * With [symmetric] the same two rules run on **mirror row pairs** `(r, D − 1 − r)`, the partner
 * taking the negated position, so every even-length prefix of the order is centro-symmetric. A
 * middle row is served singly and last, which is where the parity of an odd count is spent.
 *
 * @throws IllegalArgumentException if a root of [anchor] is not a site of its own row, if a row
 *   carries more than [maximumPerRow] or fewer than [minimumPerRow], or if [symmetric] is asked
 *   of a lattice that is not itself centro-symmetric.
 */
@Suppress("LongParameterList", "CyclomaticComplexMethod")
fun nestedRootChain(
    sites: List<List<Double>>,
    anchor: List<List<Double>>,
    symmetric: Boolean = false,
    maximumPerRow: Int = 3,
    minimumPerRow: Int = 1,
    tolerance: Double = 1e-9
): NestedRootChain {
    require(sites.isNotEmpty()) { "sites must not be empty" }
    require(anchor.size == sites.size) {
        "the anchor must carry one row per lattice row: ${anchor.size} against ${sites.size}"
    }
    require(minimumPerRow >= 1) { "minimumPerRow must be at least one, was: $minimumPerRow" }
    require(maximumPerRow >= minimumPerRow) {
        "maximumPerRow must be at least minimumPerRow, were: $maximumPerRow, $minimumPerRow"
    }
    anchor.indices.forEach { row ->
        require(anchor[row].size in minimumPerRow..maximumPerRow) {
            "row $row carries ${anchor[row].size} roots, outside " +
                    "[$minimumPerRow, $maximumPerRow]"
        }
        require(anchor[row].sorted() == anchor[row]) {
            "row $row's roots must ascend, were: ${anchor[row]}"
        }
        anchor[row].forEach { root ->
            require(sites[row].any { abs(it - root) <= tolerance }) {
                "root $root is not a site of row $row, whose sites are ${sites[row]}"
            }
        }
    }
    val duplexes = sites.size
    if (symmetric) {
        require(
            sites.indices.all { row ->
                val mine = sites[row]
                val partner = sites[duplexes - 1 - row].map { -it }.sorted()
                mine.size == partner.size &&
                        mine.zip(partner).all { (a, b) -> abs(a - b) <= tolerance }
            }
        ) { "a symmetric chain needs a centro-symmetric lattice, and this one is not" }
    }

    fun interiorOf(roots: List<Double>): Double {
        val mean = roots.average()
        return roots.minByOrNull { abs(it - mean) * 1000.0 + (it + 1000.0) * 1e-9 }!!
    }

    fun exteriorSiteFor(roots: List<Double>, unused: List<Double>): Double {
        val mean = if (roots.isEmpty()) 0.0 else roots.average()
        return unused.minByOrNull { abs(it - mean) * 1000.0 + (it + 1000.0) * 1e-9 }!!
    }

    fun unusedOf(row: Int, current: List<MutableList<Double>>): List<Double> =
        sites[row].filter { site -> current[row].none { abs(it - site) <= tolerance } }

    fun mirrorOf(row: Int) = duplexes - 1 - row
    val middle = if (duplexes % 2 == 1) duplexes / 2 else -1

    // ------------------------------------------------------------------ the removal order
    val removals = ArrayList<Pair<Int, Double>>()
    run {
        val current = anchor.map { it.toMutableList() }
        fun take(row: Int, x: Double) {
            val index = current[row].indexOfFirst { abs(it - x) <= tolerance }
            check(index >= 0) { "row $row does not carry $x" }
            current[row].removeAt(index)
            removals += row to x
        }
        if (symmetric) {
            while (true) {
                val target = (0 until duplexes / 2)
                    .filter {
                        current[it].size > minimumPerRow &&
                                current[mirrorOf(it)].size > minimumPerRow
                    }
                    .maxByOrNull { current[it].size * 1000 - it } ?: break
                val victim = interiorOf(current[target])
                take(target, victim)
                take(mirrorOf(target), -victim)
            }
        }
        while (true) {
            val target = current.indices
                .filter { current[it].size > minimumPerRow }
                .filter { !symmetric || it == middle }
                .maxByOrNull { current[it].size * 1000 - it } ?: break
            take(target, interiorOf(current[target]))
        }
    }

    // ------------------------------------------------------------------ the addition order
    val additions = ArrayList<Pair<Int, Double>>()
    run {
        val current = anchor.map { it.toMutableList() }
        fun give(row: Int, x: Double) {
            current[row].add(x)
            current[row].sort()
            additions += row to x
        }
        if (symmetric) {
            while (true) {
                val target = (0 until duplexes / 2)
                    .filter {
                        current[it].size < maximumPerRow && unusedOf(it, current).isNotEmpty() &&
                                current[mirrorOf(it)].size < maximumPerRow &&
                                unusedOf(mirrorOf(it), current).isNotEmpty()
                    }
                    .minByOrNull { current[it].size * 1000 + it } ?: break
                val newcomer = exteriorSiteFor(current[target], unusedOf(target, current))
                give(target, newcomer)
                give(mirrorOf(target), -newcomer)
            }
        }
        while (true) {
            val target = current.indices
                .filter { current[it].size < maximumPerRow && unusedOf(it, current).isNotEmpty() }
                .filter { !symmetric || it == middle }
                .minByOrNull { current[it].size * 1000 + it } ?: break
            give(target, exteriorSiteFor(current[target], unusedOf(target, current)))
        }
    }

    val anchorCount = anchor.sumOf { it.size }
    return NestedRootChain(
        sites = sites,
        anchor = anchor.map { it.toList() },
        symmetric = symmetric,
        minimumCount = anchorCount - removals.size,
        maximumCount = anchorCount + additions.size,
        removals = removals,
        additions = additions,
        tolerance = tolerance
    )
}

/**
 * The theoretical extreme counts of a lattice at [minimumPerRow] and [maximumPerRow] — a count
 * and no solve, used to check that a chain reaches the whole family the lattice can carry.
 */
fun latticeCountRange(
    sites: List<List<Double>>,
    minimumPerRow: Int = 1,
    maximumPerRow: Int = 3
): IntRange {
    require(sites.isNotEmpty()) { "sites must not be empty" }
    require(sites.all { it.isNotEmpty() }) { "every row must carry at least one site" }
    require(minimumPerRow >= 1) { "minimumPerRow must be at least one, was: $minimumPerRow" }
    require(maximumPerRow >= minimumPerRow) {
        "maximumPerRow must be at least minimumPerRow, were: $maximumPerRow, $minimumPerRow"
    }
    return sites.sumOf { min(minimumPerRow, it.size) }..sites.sumOf { min(maximumPerRow, it.size) }
}

// ------------------------------------------------------------------ the lattice index map

/**
 * The indices of [rows]' roots in the flattened site list of [sites] — row-major and ascending
 * in `x` within a row, which is the order `UpwardRootInfluenceBank` lays its stations out in.
 *
 * @throws IllegalArgumentException if a root is not a site of its own row.
 */
fun rootStationIndices(
    sites: List<List<Double>>,
    rows: List<List<Double>>,
    tolerance: Double = 1e-9
): List<Int> {
    require(rows.size == sites.size) {
        "one row of roots per lattice row: ${rows.size} against ${sites.size}"
    }
    val indices = ArrayList<Int>()
    var offset = 0
    sites.indices.forEach { row ->
        rows[row].forEach { root ->
            val local = sites[row].indexOfFirst { abs(it - root) <= tolerance }
            require(local >= 0) {
                "root $root is not a site of row $row, whose sites are ${sites[row]}"
            }
            indices += offset + local
        }
        offset += sites[row].size
    }
    return indices.sorted()
}

/** The `(x, y)` stations of [rows] on a [duplexes]-duplex sheet at [interhelicalDistance]. */
fun rootStations(
    rows: List<List<Double>>,
    duplexes: Int,
    interhelicalDistance: Double
): List<Pair<Double, Double>> {
    require(rows.size == duplexes) {
        "one row of roots per duplex: ${rows.size} against $duplexes"
    }
    return rows.indices.flatMap { row ->
        val y = (row - (duplexes - 1) / 2.0) * interhelicalDistance
        rows[row].map { it to y }
    }
}

// ------------------------------------------------------------------ the plan axis, reported

/**
 * Whether every row of [rows] can point its arms of length [arm] so that they clear one another
 * and the tile edge — `C-0053`'s footprint convention through `C-0055`/`C-0063`'s own
 * `armDirections`, so that a *places* verdict here is the one those claims give.
 *
 * This is **reported and never imposed** in `T-163`. An arm's length is a function of the path
 * count (`C-0075`), so buildability is a *plan* axis that `C-0069`/`C-0075` own; folding it into
 * the station set is what made `C-0089`'s 45-path row unreadable as a count.
 *
 * @throws IllegalArgumentException if [arm] or [edgeX] is not positive.
 */
fun rowsAdmitArm(
    rows: List<List<Double>>,
    arm: Double,
    edgeX: Double,
    width: Double = OrigamiDuplex.INTERHELICAL
): Boolean {
    require(arm > 0.0) { "arm must be positive, was: $arm" }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(width > 0.0) { "width must be positive, was: $width" }
    return rows.all { it.isEmpty() || armDirections(it, arm, edgeX, width) != null }
}

/** Whether [rows] is invariant under `(x, y) → (−x, −y)`, asserted on the set. */
fun rowsAreCentroSymmetric(rows: List<List<Double>>, tolerance: Double = 1e-9): Boolean =
    rows.indices.all { row ->
        val mine = rows[row].sorted()
        val partner = rows[rows.size - 1 - row].map { -it }.sorted()
        mine.size == partner.size && mine.zip(partner).all { (a, b) -> abs(a - b) <= tolerance }
    }
