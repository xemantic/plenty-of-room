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

import com.xemantic.nano.plentyofroom.coupling.CountPhaseSplit
import com.xemantic.nano.plentyofroom.coupling.InfluenceSurrogate
import com.xemantic.nano.plentyofroom.coupling.MultiStateSurrogate
import com.xemantic.nano.plentyofroom.coupling.countPhaseSplit
import com.xemantic.nano.plentyofroom.coupling.searchDecision
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlin.math.abs

/**
 * `T-323` — the **placement** and the **distribution** searched together, and the machinery that
 * makes a joint search an outer loop rather than a new problem.
 *
 * ## Why this file exists
 *
 * This corpus carries two claims about these two design variables and no measurement of their
 * interaction. `C-0063` searched the **placement** with the distribution fixed — 1 144 858
 * placements, `0.4156 → 0.0706` of the stroke on the square lattice, with **equal springs** —
 * and its sentence, which `CLAUDE.md` carries, is *"which stations a coupling enters at is worth
 * more than how its stiffness is distributed."* `C-0212` searched the **distribution** with the
 * placement fixed on this honeycomb lattice — `22 of 32` cells flat where two transferred rules
 * gave `0 of 32`. Neither moved the other variable, and `C-0072` already warns that *"selecting
 * a placement on the EQUAL-SPRING objective is selecting on the wrong quantity once a
 * distribution is free"* — a hypothesis inherited without a number.
 *
 * ## The three objects, and why each is small
 *
 * 1. [JointPlacementFamily] — the placement design space is a **product of row option sets**, so
 *    its size is one product and its symmetry one set intersection, both before any solve. On
 *    `C-0141`'s determined ladder the five-station rows are **forced** at five columns and the
 *    whole family is `6⁵ = 7 776`: **exhaustible**, which removes `C-0102`'s *a descent compared
 *    against an exhaustive enumeration is not a comparison* at the deciding cell.
 * 2. [HoneycombStationBank] — an influence bank is a property of the **structure**, a
 *    distribution enters the Woodbury system as a **diagonal**, and a *placement* is a **slice of
 *    the bank's index set**. So one bank of `stations.size` unit-point-load solves serves every
 *    placement and every distribution ever tried at that cell. This is `C-0063`'s
 *    `UpwardRootInfluenceBank` on the honeycomb, and the slice is asserted against a surrogate
 *    built on the placement alone rather than argued.
 * 3. [placementDistributionSplit] — the 2 × 2 in **both orderings**. Its arithmetic is
 *    `coupling/CountPhaseInteraction.kt`'s [countPhaseSplit], **reused unchanged**: that function
 *    is generic in its two factors and only its field names are `count`/`phase`, so mapping
 *    `count ↔ placement` and `phase ↔ distribution` at one call site is what keeps the rule
 *    from being written twice (`CLAUDE.md`: *a duplicated rule is invisible to a mutation test of
 *    either copy*).
 *
 * ## Conventions, restated rather than inherited
 *
 * `s` runs **along** the helices and `y` **across** them in the plane of the face, both in nm
 * from the face centre; a station is `(s, y)` and deflections are positive **downward**.
 * A **placement** is, for each raster row, a set of `columns` **distinct** stations of that
 * row's own `21 bp` ladder at the row's rooting-helix `y`. Its **key** is the tuple of station
 * indices, ascending within a row and row-major overall; the enumeration order is lexicographic
 * on that key and, at a tie in the objective, the **smaller key wins** — a rule that depends on
 * the family and not on the order a search happens to visit it in.
 */

// ------------------------------------------------------------------------------ the family

/**
 * One candidate placement of [family]: which stations of each row's own ladder are occupied.
 *
 * Construct through [JointPlacementFamily.placementAt] or [JointPlacementFamily.enumerate].
 */
class JointPlacement internal constructor(

    /** The family this placement belongs to. */
    val family: JointPlacementFamily,

    /** One ascending list of station indices per raster row, each `family.columns` long. */
    val key: List<List<Int>>
) {

    /** The indices of this placement's stations in [JointPlacementFamily.stations]. */
    val bankIndices: List<Int> = key.flatMapIndexed { row, chosen ->
        chosen.map { family.bankIndex(row, it) }
    }

    /** The stations themselves, `(s, y)` in nm, row-major and ascending within a row. */
    val grid: List<Pair<Double, Double>> = bankIndices.map { family.stations[it] }

    /**
     * A deterministic, order-independent name — the key rendered so that lexicographic order on
     * the string is lexicographic order on the key, which is what makes the tie-break a property
     * of the family rather than of a traversal.
     */
    val label: String = key.joinToString("|") { row ->
        row.joinToString("-") { it.toString().padStart(2, '0') }
    }

    /**
     * Whether this placement is invariant under `(s, y) → (−s, −y)`.
     *
     * `C-0063`'s square-lattice answer was found by an exhaustive enumeration of exactly this
     * family, so it is measured here rather than assumed — and on the honeycomb's determined
     * ladder the answer is that no such member exists at all
     * ([JointPlacementFamily.admitsCentroSymmetry]).
     */
    fun isCentroSymmetric(tolerance: Double = 1e-9): Boolean {
        val mirrored = grid.map { (s, y) -> -s to -y }
        return mirrored.all { (s, y) ->
            grid.any { abs(it.first - s) < tolerance && abs(it.second - y) < tolerance }
        }
    }

}

/**
 * The placement design space at one column count: for each raster row, every choice of [columns]
 * distinct stations of that row's own ladder.
 *
 * @param rowStations one strictly ascending list of `s` positions per raster row, in nm.
 * @param rowY that row's rooting-helix `y`, in nm.
 * @param columns how many stations each row carries — the path count is `rows × columns`.
 */
class JointPlacementFamily(

    /** The candidate `s` positions of each raster row, strictly ascending. */
    val rowStations: List<List<Double>>,

    /** The rooting-helix `y` of each raster row. */
    val rowY: List<Double>,

    /** How many stations each row carries. */
    val columns: Int
) {

    init {
        require(rowStations.isNotEmpty()) { "rowStations must not be empty" }
        require(rowY.size == rowStations.size) {
            "one y per raster row: ${rowY.size} against ${rowStations.size} rows"
        }
        require(columns > 0) { "columns must be positive, was: $columns" }
        rowStations.forEachIndexed { row, stations ->
            require(stations.isNotEmpty()) { "row $row carries no station" }
            require(stations.zipWithNext().all { (a, b) -> b > a }) {
                "row $row's ladder must be strictly ascending, was: $stations"
            }
            require(columns <= stations.size) {
                "a $columns-column placement cannot stand on row $row's ladder of " +
                        "${stations.size} stations — that is a change of the path COUNT, not " +
                        "of the position"
            }
        }
    }

    /** How many x-raster rows the face carries. */
    val rasterRows: Int = rowStations.size

    /** How many load paths a member of this family carries. */
    val pathCount: Int = rasterRows * columns

    /** Every candidate station of the face, row-major — the bank's own index set. */
    val stations: List<Pair<Double, Double>> = rowStations.flatMapIndexed { row, positions ->
        positions.map { it to rowY[row] }
    }

    /** How many candidate stations the face carries in total. */
    val stationCount: Int = stations.size

    private val rowOffset: IntArray = IntArray(rasterRows).also { offsets ->
        var running = 0
        for (row in 0 until rasterRows) {
            offsets[row] = running
            running += rowStations[row].size
        }
    }

    /** The index of row [row]'s station [station] in [stations]. */
    fun bankIndex(row: Int, station: Int): Int {
        require(row in 0 until rasterRows) { "no such raster row: $row" }
        require(station in rowStations[row].indices) {
            "row $row has ${rowStations[row].size} stations, asked for $station"
        }
        return rowOffset[row] + station
    }

    /** Row [row]'s option set: every ascending [columns]-subset of its ladder, lexicographic. */
    val rowOptions: List<List<List<Int>>> = rowStations.map { positions ->
        ascendingSubsets(positions.size, columns)
    }

    /** How many options each row has — `C(|L_r|, columns)`. */
    val rowOptionCounts: List<Int> = rowOptions.map { it.size }

    /**
     * How many placements the family holds — the **product** of [rowOptionCounts], exactly.
     *
     * This is the cheap bound the whole method rests on: on `C-0141`'s determined ladder it is
     * `7 776` at five columns and `3.2e11` at three, so one cell is exhaustible and the others
     * are not.
     */
    val size: Long = rowOptionCounts.fold(1L) { running, count ->
        Math.multiplyExact(running, count.toLong())
    }

    /**
     * How many row pairs `(r, rows − 1 − r)` admit **any** centro-symmetric station pair.
     *
     * A placement invariant under `(s, y) → (−s, −y)` needs, for every station `s` it puts on
     * row `r`, the station `−s` on row `rows − 1 − r`; so an **empty** intersection at even one
     * row pair makes the whole symmetric family empty, whatever the column count.
     */
    val centroSymmetricRowPairs: Int = (0 until rasterRows).count { row ->
        val partner = rasterRows - 1 - row
        abs(rowY[row] + rowY[partner]) < 1e-9 &&
                rowStations[row].any { s ->
                    rowStations[partner].any { abs(it + s) < 1e-9 }
                }
    }

    /** Whether the family contains a centro-symmetric member at all. */
    val admitsCentroSymmetry: Boolean = (0 until rasterRows).all { row ->
        val partner = rasterRows - 1 - row
        abs(rowY[row] + rowY[partner]) < 1e-9 &&
                rowStations[row].count { s ->
                    rowStations[partner].any { abs(it + s) < 1e-9 }
                } >= columns
    }

    /** The placement at [key], which must be one ascending [columns]-subset per row. */
    fun placementAt(key: List<List<Int>>): JointPlacement {
        require(key.size == rasterRows) {
            "a key carries one row per raster row: ${key.size} against $rasterRows"
        }
        key.forEachIndexed { row, chosen ->
            require(chosen.size == columns) {
                "row $row's key carries ${chosen.size} stations and the family has $columns"
            }
            require(chosen.zipWithNext().all { (a, b) -> b > a }) {
                "row $row's key must be strictly ascending, was: $chosen"
            }
            require(chosen.all { it in rowStations[row].indices }) {
                "row $row's key names a station its ladder does not have: $chosen"
            }
        }
        return JointPlacement(this, key)
    }

    /**
     * Every member of the family, in **lexicographic** key order — the order the tie-break is
     * written on, so a search over this sequence is a function of the family alone.
     */
    fun enumerate(): Sequence<JointPlacement> = sequence {
        val cursor = IntArray(rasterRows)
        while (true) {
            yield(placementAt((0 until rasterRows).map { rowOptions[it][cursor[it]] }))
            var row = rasterRows - 1
            while (row >= 0) {
                cursor[row]++
                if (cursor[row] < rowOptionCounts[row]) break
                cursor[row] = 0
                row--
            }
            if (row < 0) break
        }
    }

    /**
     * The member of the family whose stations are nearest, row by row, to [grid].
     *
     * This is how `C-0167`'s own *"determined station lattice on the rooting helices"* enters the
     * search as a **member** rather than as a comparand outside it — which is what makes
     * *"the searched placement is never worse than the fixed one"* a property of the composition.
     */
    fun nearest(grid: List<Pair<Double, Double>>): JointPlacement {
        require(grid.size == pathCount) {
            "expected $pathCount stations, one per path, was: ${grid.size}"
        }
        return placementAt((0 until rasterRows).map { row ->
            val wanted = (0 until columns).map { grid[row * columns + it].first }
            val chosen = wanted.map { target ->
                rowStations[row].indices.minByOrNull { abs(rowStations[row][it] - target) }!!
            }
            require(chosen.distinct().size == columns) {
                "row $row's nearest stations collide, which changes the path count rather " +
                        "than the placement: $chosen"
            }
            chosen.sorted()
        })
    }

}

/** Every ascending [choose]-subset of `0 until size`, in lexicographic order. */
internal fun ascendingSubsets(size: Int, choose: Int): List<List<Int>> {
    require(choose in 1..size) { "cannot choose $choose of $size" }
    val out = ArrayList<List<Int>>()
    val current = IntArray(choose) { it }
    while (true) {
        out += current.toList()
        var index = choose - 1
        while (index >= 0 && current[index] == size - choose + index) index--
        if (index < 0) break
        current[index]++
        for (next in index + 1 until choose) current[next] = current[next - 1] + 1
    }
    return out
}

// ------------------------------------------------------------------------------ the bank

/**
 * One free solution and one unit-point-load solution per **candidate** station of a honeycomb
 * face, sampled once, from which the response of any coupling at any **subset** of them follows
 * exactly.
 *
 * This is what makes a placement sweep affordable, and it is `C-0063`'s `UpwardRootInfluenceBank`
 * on the other lattice: the stations enter a [HoneycombGrillage] solve as **loads**, so the host
 * is factorised once and every candidate station costs one back-substitution, after which a whole
 * placement costs an `n × n` Cholesky instead of a 4 320-degree-of-freedom solve.
 *
 * The influences are taken on `withoutPrestrain` for the reason `C-0104` gives and
 * [honeycombTiedSurrogate] carries: a prestrain is a **load**, so an influence taken on the
 * prestrained lattice is that influence *plus* the prestrain's own response, and the Woodbury
 * matrix stops being a compliance.
 */
class HoneycombStationBank(
    lattice: HoneycombGrillage,

    /** Every candidate station, `(s, y)` in nm from the face centre. */
    val stations: List<Pair<Double, Double>>,
    pressure: PressureField,

    /** Samples per edge of the dishing grid. */
    val samples: Int = 81
) {

    init {
        require(stations.isNotEmpty()) { "stations must not be empty" }
        require(samples >= 2) { "samples must be at least 2, was: $samples" }
    }

    private val halfS = lattice.lengthS / 2.0

    private val halfY = lattice.lengthY / 2.0

    private val structure = lattice.withoutPrestrain

    private val free = lattice.solve(pressure)

    private val influence = stations.map { (s, y) ->
        structure.solve(uniformPressure(0.0), listOf(PointLoad(s, y, 1.0)))
    }

    /** The peak dishing in nm of the host under the load alone — the *no coupling at all* bar. */
    val freePeakDishing: Double = free.peakDishing(samples)

    private fun sample(dishing: (Double, Double) -> Double): DoubleArray {
        val field = DoubleArray(samples * samples)
        for (i in 0 until samples) {
            val s = -halfS + 2.0 * halfS * i / (samples - 1)
            for (j in 0 until samples) {
                val y = -halfY + 2.0 * halfY * j / (samples - 1)
                field[i * samples + j] = dishing(s, y)
            }
        }
        return field
    }

    private val stationFree = DoubleArray(stations.size) {
        free.deflection(stations[it].first, stations[it].second)
    }

    private val stationInfluence = Array(stations.size) { j ->
        DoubleArray(stations.size) { k ->
            influence[k].deflection(stations[j].first, stations[j].second)
        }
    }

    private val dishingFree = sample { s, y -> free.dishing(s, y) }

    private val dishingInfluence = Array(stations.size) { k ->
        sample { s, y -> influence[k].dishing(s, y) }
    }

    /**
     * `C-0135`'s one-state [MultiStateSurrogate] over the same subset — the object the **smoothed
     * minimax** runs on, sliced from the same bank rather than re-solved.
     *
     * [honeycombMultiStateSurrogate] builds this from `grid.size` fresh lattice solves; sliced,
     * it costs nothing beyond the bank that already exists, and the two are asserted equal.
     */
    fun multiStateFor(indices: List<Int>, stateName: String): MultiStateSurrogate {
        require(indices.isNotEmpty()) { "indices must not be empty" }
        require(indices.all { it in stations.indices }) {
            "every index must name a station of this bank, were: $indices"
        }
        require(indices.distinct().size == indices.size) { "the indices must be distinct" }
        return MultiStateSurrogate(
            grid = indices.map { stations[it] },
            samples = samples,
            stateNames = listOf(stateName),
            stationInfluence = Array(indices.size) { j ->
                DoubleArray(indices.size) { k -> stationInfluence[indices[j]][indices[k]] }
            },
            dishingInfluence = Array(indices.size) { dishingInfluence[indices[it]] },
            stationFree = arrayOf(DoubleArray(indices.size) { stationFree[indices[it]] }),
            dishingFree = arrayOf(dishingFree)
        )
    }

    /** `C-0058`'s surrogate over the subset of stations at [indices], in that order. */
    fun surrogateFor(indices: List<Int>): InfluenceSurrogate {
        require(indices.isNotEmpty()) { "indices must not be empty" }
        require(indices.all { it in stations.indices }) {
            "every index must name a station of this bank, were: $indices"
        }
        require(indices.distinct().size == indices.size) { "the indices must be distinct" }
        return InfluenceSurrogate(
            grid = indices.map { stations[it] },
            samples = samples,
            stationFree = DoubleArray(indices.size) { stationFree[indices[it]] },
            stationInfluence = Array(indices.size) { j ->
                DoubleArray(indices.size) { k -> stationInfluence[indices[j]][indices[k]] }
            },
            dishingFree = dishingFree,
            dishingInfluence = Array(indices.size) { dishingInfluence[indices[it]] }
        )
    }

}

// ------------------------------------------------------------------------------ the search

/**
 * [value] at the precision a **search decision** is taken at — six significant digits.
 *
 * `C-0135` and `C-0177` record that a descent takes `O(10³)` `Double` comparisons, that one ulp
 * of jitter in a hot reduction flips one of them, and that the cure is to decide **coarser** than
 * the number is emitted at. `CLAUDE.md` adds the half nobody applies: *a cure is a property of a
 * CALL SITE, not of a repository — grep for the call sites, not for the fix.* `T-323` wrote
 * fourteen selection sites and routed **five** of them through this rule, and `F23` duly fired
 * (`C-0216` §14); `T-328` routes the rest, and every one of them goes through **this** function,
 * [decidesBetter], [byDecisionThenLabel] or [decisionArgmin], so a mutation of the rule is
 * visible from every site rather than from one copy of it.
 *
 * It is **idempotent**, which is what makes routing an objective that is already rounded — the
 * two sites consuming `T-316`'s `percentileObjective` — provably inert rather than a second
 * rounding.
 */
fun searchDecisionKey(value: Double): Double = searchDecision(value)

/**
 * Whether [candidate] is strictly better than [incumbent] **at the decision precision**.
 *
 * The discriminating case is a candidate better by *less* than six significant digits: rounded it
 * ties and cannot move the incumbent. A rank is this same decision read as a **count**, which is
 * why `determinedRankFromBest` and `jointWinnerRankInThisScreen` call it rather than writing
 * a raw `<`.
 */
fun decidesBetter(candidate: Double, incumbent: Double): Boolean =
    searchDecisionKey(candidate) < searchDecisionKey(incumbent)

/**
 * Orders candidates by [key] at the decision precision, breaking ties on [label].
 *
 * The tie-break is a property of the **family** and not of a traversal, so the answer does not
 * depend on the order the candidates were visited in — which is strictly stronger than *the
 * earlier candidate wins*, and agrees with it wherever the labels are in enumeration order, as
 * every label in this study is.
 */
fun <T> byDecisionThenLabel(label: (T) -> String, key: (T) -> Double): Comparator<T> =
    compareBy({ searchDecisionKey(key(it)) }, { label(it) })

/**
 * The best of [candidates] under [byDecisionThenLabel], evaluating [key] **exactly once** per
 * candidate.
 *
 * The once-per-candidate contract is not a nicety: two of the sites this replaces have a whole
 * dropout ensemble behind their key, so a `sortedWith` would pay `O(n log n)` solves where an
 * argmin pays `n`.
 */
fun <T> decisionArgmin(candidates: List<T>, label: (T) -> String, key: (T) -> Double): T {
    require(candidates.isNotEmpty()) { "an argmin needs at least one candidate" }
    val keys = DoubleArray(candidates.size) { searchDecisionKey(key(candidates[it])) }
    var best = 0
    for (index in 1 until candidates.size) {
        val better = keys[index] < keys[best] ||
                (keys[index] == keys[best] &&
                        label(candidates[index]) < label(candidates[best]))
        if (better) best = index
    }
    return candidates[best]
}

/**
 * Whether a numerical identity whose true value is **zero** holds to [tolerance].
 *
 * `CLAUDE.md`: *a quantity that is nothing but ulp noise must be emitted as a THRESHOLD, never as
 * a value — rounding cannot save it*, because one such field makes a whole result file
 * permanently un-diffable, which is the check the rounding layer exists to enable. `T-323`
 * emitted two such residuals as numbers (`9.6E-16` against `3.8E-16`, `2.0E-14` against
 * `3.9E-14` between two runs of identical code) and `C-0216` §14(b) queued this as `T-329`.
 *
 * A non-finite residual is **not** a residual that holds: `abs(NaN) < tolerance` is `false`, and
 * that is the direction a report must fail in.
 */
fun identityHolds(residual: Double, tolerance: Double): Boolean {
    require(tolerance > 0.0 && tolerance.isFinite()) {
        "a tolerance must be positive and finite, was: $tolerance"
    }
    return abs(residual) < tolerance
}

/**
 * Whether a candidate beats an incumbent, at the **decision** precision and with a tie-break that
 * is a property of the family rather than of a traversal.
 *
 * One rule, one implementation: the comparison is [decidesBetter] and the tie-break the label, so
 * a mutation of either is invisible to neither.
 */
fun jointPlacementBetter(
    candidateValue: Double,
    candidateLabel: String,
    bestValue: Double,
    bestLabel: String
): Boolean {
    if (decidesBetter(candidateValue, bestValue)) return true
    if (decidesBetter(bestValue, candidateValue)) return false
    return candidateLabel < bestLabel
}

/**
 * The best member of [family] under [objective], by **exhaustive** enumeration.
 *
 * Affordable exactly where [JointPlacementFamily.size] is small, which on `C-0141`'s determined
 * ladder is the five-column cell — `6⁵ = 7 776`, because the five-station rows are forced.
 */
fun exhaustiveJointPlacement(
    family: JointPlacementFamily,
    objective: (JointPlacement) -> Double
): JointPlacement {
    var best: JointPlacement? = null
    var bestValue = Double.POSITIVE_INFINITY
    family.enumerate().forEach { candidate ->
        val value = objective(candidate)
        if (best == null || jointPlacementBetter(value, candidate.label, bestValue, best!!.label)) {
            best = candidate
            bestValue = value
        }
    }
    return best!!
}

/**
 * A deterministic **per-row** coordinate descent over a placement family: hold every row but one,
 * enumerate that row's options exhaustively, take the best under [jointPlacementBetter], and
 * sweep until no row moves.
 *
 * Used where the family cannot be enumerated — at 10, 20 and 30 paths on this lattice — and
 * **calibrated** at the 50-path cell, where the exhaustive optimum is known, so that its slack is
 * a measurement rather than an assumption (`C-0102`, `CH-0119`).
 *
 * @param start the family anchor; every member of [starts] must belong to the same family.
 * @param starts the descent's starts, defaulting to [start] alone.
 */
fun descendJointPlacement(
    start: JointPlacement,
    sweeps: Int,
    starts: List<JointPlacement> = listOf(start),
    objective: (JointPlacement) -> Double
): JointPlacement {
    require(sweeps >= 1) { "sweeps must be at least 1, was: $sweeps" }
    require(starts.isNotEmpty()) { "at least one start is required" }
    require(starts.all { it.family === start.family }) {
        "every start must belong to the same placement family as the anchor"
    }
    val family = start.family
    var best: JointPlacement? = null
    var bestValue = Double.POSITIVE_INFINITY
    starts.forEach { seed ->
        var current = seed
        var currentValue = objective(seed)
        for (sweep in 1..sweeps) {
            var moved = false
            for (row in 0 until family.rasterRows) {
                family.rowOptions[row].forEach { option ->
                    if (option != current.key[row]) {
                        val key = current.key.toMutableList().also { it[row] = option }
                        val candidate = family.placementAt(key)
                        val value = objective(candidate)
                        if (jointPlacementBetter(
                                value, candidate.label, currentValue, current.label
                            )
                        ) {
                            current = candidate
                            currentValue = value
                            moved = true
                        }
                    }
                }
            }
            if (!moved) break
        }
        if (best == null ||
            jointPlacementBetter(currentValue, current.label, bestValue, best!!.label)
        ) {
            best = current
            bestValue = currentValue
        }
    }
    return best!!
}

// ------------------------------------------------------------------------------ the 2 x 2

/**
 * The 2 × 2 of **placement freedom** against **distribution freedom**, in both orderings.
 *
 * This is [countPhaseSplit] **reused unchanged** under the mapping
 *
 * | this task | [CountPhaseSplit] |
 * |---|---|
 * | placement | count |
 * | distribution | phase |
 *
 * so `countTermAtFromPhase` is the **placement** term taken first at the transferred
 * distribution, `phaseTermAtFromCount` the **distribution** term taken first at the fixed
 * placement, and `interaction` their gap — identically the difference of the two placement terms
 * and of the two distribution terms. The arithmetic is not written twice, because a duplicated
 * rule is invisible to a mutation test of either copy.
 *
 * A **negative** interaction means the two freedoms are *synergistic* — each is worth more when
 * the other is free; a **positive** one means they are *substitutive*, so the two separately
 * measured gains **overstate** what a joint search buys. `T-323` declares the expectation as
 * substitutive, before the run, so that it can be wrong.
 */
fun placementDistributionSplit(
    fixedPlacementTransferred: Double,
    searchedPlacementTransferred: Double,
    fixedPlacementSearched: Double,
    searchedPlacementSearched: Double
): CountPhaseSplit = countPhaseSplit(
    fromCountFromPhase = fixedPlacementTransferred,
    toCountFromPhase = searchedPlacementTransferred,
    fromCountToPhase = fixedPlacementSearched,
    toCountToPhase = searchedPlacementSearched
)

/**
 * The **median of the per-realisation ratio** `a / b` over a paired sample.
 *
 * `CLAUDE.md`: *a ratio of two ORDER STATISTICS is not the order statistic of the ratio*, and
 * `C-0212`'s own headline had to be corrected for exactly that. Both ensembles here are drawn
 * from one seed at one path count, so `DropoutRandom` hands every design the **same** uniform
 * stream and the comparison is paired by construction — which is what makes this well posed.
 */
fun pairedMedianRatio(a: DoubleArray, b: DoubleArray): Double {
    require(a.isNotEmpty()) { "the sample must not be empty" }
    require(a.size == b.size) {
        "a paired ratio needs one pair per realisation: ${a.size} against ${b.size}"
    }
    require(b.all { it > 0.0 && it.isFinite() }) {
        "every denominator must be positive and finite"
    }
    val ratios = DoubleArray(a.size) { a[it] / b[it] }
    ratios.sort()
    val middle = ratios.size / 2
    return if (ratios.size % 2 == 1) ratios[middle]
    else 0.5 * (ratios[middle - 1] + ratios[middle])
}
