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

import com.xemantic.nano.plentyofroom.coupling.InfluenceSurrogate
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.PointLoad
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.roundForResult
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlin.math.abs

/**
 * `T-125` — **where** on the upward azimuth the 34 arm roots of `C-0055`'s array are put, and
 * what that is worth in flatness.
 *
 * ## What is a free variable here, and what is not
 *
 * `C-0055` fixed the *count* (34, self-consistently with `C-0039`'s arm) and the *lattice* (an
 * upward site belongs to one duplex, so its pitch is the bare 32 bp = 10.88 nm and adjacent
 * rows are offset by 16 bp). It did **not** fix the placement: its scheduler fills every row
 * greedily from the low-`x` end, which is one member of a large family, and `C-0061` found that
 * a uniform coupling on that member dishes **1.35× worse than no coupling at all** while
 * observing in one line that the array's centroid sits at `x = −8.80 nm`.
 *
 * The free variables this file exposes are exactly two, and they are the same variable twice:
 *
 * 1. **the lattice phase `φ`**, quantised to base pairs with a period of **32** (`C-0015`) —
 *    which sets the sheet's own crossover columns *and* the arm roots together, because the
 *    upward sites are the crossover planes at `k ≡ 2r + 3 (mod 4)` and the sheet's columns are
 *    the planes at `k` even;
 * 2. **which of its own row's sites each row uses**, subject to the arms fitting.
 *
 * A *reflection* of a row — `C-0061`'s one-line improvement — is a member of (2) only where the
 * row's own site lattice is symmetric about the tile centre, and [centroSymmetricUpwardPhases]
 * is the congruence that says at which phases that happens. **At `C-0055`'s own phase it does
 * not**: reflecting an odd row lands every one of its roots on the `WEST` azimuth, which points
 * *into* the grafted layer and is the half of the out-of-plane inventory `C-0055` counted and
 * refused.
 *
 * ## Conventions, restated rather than inherited
 *
 * Lengths **nm**, forces **pN**, stiffness **pN/nm**, pressure **pN/nm²** (= 1 MPa exactly).
 * `x` runs **along** the helices, `y` **across** them, `z` **normal** and positive **upward** —
 * away from the grafted layer, which lies below the tile. `w` is positive **downward**.
 * A **root** is the crossover that ties one arm to its host duplex; the coupling enters the
 * sheet there, which is the whole reason a placement has a flatness at all.
 */

/** The upward root pitch in base pairs — the bare per-interface crossover spacing (`C-0055`). */
const val UPWARD_ROOT_PITCH_BASE_PAIRS: Int = 32

/**
 * The upward (`EAST`) site positions of every row, ascending, at a lattice phase of
 * [phaseBasePairs] base pairs on a sheet of edge [edgeX] with [duplexes] duplexes.
 *
 * Derived from [upwardHingeSites], which is `C-0055`'s own construction, so that no second
 * reading of the azimuth rule can drift from it.
 */
fun upwardRootLattice(
    phaseBasePairs: Int,
    edgeX: Double,
    duplexes: Int,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR
): List<List<Double>> {
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
    val sites = upwardHingeSites(phaseBasePairs, edgeX, duplexes, risePerBasePair)
    return (0 until duplexes).map { row ->
        sites.filter { it.interfaceIndex == row }.map { it.x }.sorted()
    }
}

/**
 * A direction for each of [roots] under which the arms clear one another and the tile edge, or
 * `null` if no assignment does.
 *
 * The footprint convention is `C-0053`'s exactly — an arm occupies `[low, high]` and the next
 * one may start at `high + width` — so that a placement admitted here is admitted by
 * [maximumArmsInRow] as well, which is gate 2.
 *
 * Directions are searched **`+x` first**, so the returned assignment is the one a greedy
 * scheduler would produce and the choice is deterministic rather than incidental.
 */
fun armDirections(
    roots: List<Double>,
    arm: Double,
    edgeX: Double,
    width: Double = OrigamiDuplex.INTERHELICAL
): List<Boolean>? {
    require(roots.isNotEmpty()) { "roots must not be empty" }
    require(arm > 0.0) { "arm must be positive, was: $arm" }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(width > 0.0) { "width must be positive, was: $width" }
    val half = edgeX / 2.0
    val sorted = roots.sorted()
    require(sorted == roots) { "roots must be given in ascending order, were: $roots" }

    fun search(index: Int, frontier: Double, taken: List<Boolean>): List<Boolean>? {
        if (index == roots.size) return taken
        for (toward in listOf(true, false)) {
            val low = if (toward) roots[index] else roots[index] - arm
            val high = if (toward) roots[index] + arm else roots[index]
            if (low < -half - PLAN_TANGENCY_TOLERANCE) continue
            if (high > half + PLAN_TANGENCY_TOLERANCE) continue
            if (low < frontier - PLAN_TANGENCY_TOLERANCE) continue
            val found = search(index + 1, high + width, taken + toward)
            if (found != null) return found
        }
        return null
    }
    return search(0, Double.NEGATIVE_INFINITY, emptyList())
}

/**
 * Every set of [count] roots one row can take from its own [sites], in ascending order of the
 * root positions and of the subsets themselves.
 *
 * A subset is admitted when [armDirections] finds it a direction assignment — nothing else is
 * imposed, and in particular no row is required to be maximal: `C-0055`'s 34 is a
 * *self-consistent* count and eleven of the fifteen rows carry two arms where three would fit.
 */
fun rowRootOptions(
    sites: List<Double>,
    count: Int,
    arm: Double,
    edgeX: Double,
    width: Double = OrigamiDuplex.INTERHELICAL
): List<List<Double>> {
    require(count >= 1) { "count must be at least one, was: $count" }
    if (count > sites.size) return emptyList()
    val ascending = sites.sorted()
    val chosen = ArrayList<List<Double>>()
    fun build(start: Int, taken: List<Double>) {
        if (taken.size == count) {
            if (armDirections(taken, arm, edgeX, width) != null) chosen += taken
            return
        }
        for (index in start until ascending.size) build(index + 1, taken + ascending[index])
    }
    build(0, emptyList())
    return chosen
}

/**
 * How many rows must carry [maximumPerRow] arms for [count] arms to be placed on [duplexes]
 * rows carrying at least `maximumPerRow − 1` each — the **cheap bound that fixes the shape of
 * the whole design space**, `3a + 2(D − a) = n`, before any solve.
 */
fun rowsCarryingThreeArms(count: Int, duplexes: Int, maximumPerRow: Int): Int {
    require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
    require(maximumPerRow >= 2) { "maximumPerRow must be at least 2, was: $maximumPerRow" }
    val excess = count - (maximumPerRow - 1) * duplexes
    require(excess in 0..duplexes) {
        "$count arms cannot be placed on $duplexes rows carrying " +
                "${maximumPerRow - 1} or $maximumPerRow each"
    }
    return excess
}

/** One row of a placement: its roots, ascending, and which way each of its arms points. */
data class UpwardArmRow(
    val row: Int,
    val roots: List<Double>,
    val towardPositiveX: List<Boolean>
) {

    init {
        require(row >= 0) { "row must not be negative, was: $row" }
        require(roots.isNotEmpty()) { "a row must carry at least one arm" }
        require(roots.size == towardPositiveX.size) {
            "one direction per root: ${roots.size} roots against ${towardPositiveX.size}"
        }
        require(roots.sorted() == roots) { "roots must ascend, were: $roots" }
    }

    /** The number of arms this row carries. */
    val count: Int get() = roots.size

}

/** A whole placement of arm roots on the upward lattice at one phase. */
data class UpwardArmPlacement(
    val phaseBasePairs: Int,
    val rows: List<UpwardArmRow>
) {

    init {
        require(rows.isNotEmpty()) { "a placement must carry at least one row" }
    }

    /** The number of arms placed. */
    val count: Int get() = rows.sumOf { it.count }

    /** The `x` of the coupling centroid in nm — `C-0061`'s `−8.80` on `C-0055`'s own array. */
    val centroidX: Double get() = rows.sumOf { row -> row.roots.sum() } / count

    /**
     * A canonical string identifying this placement, used as the **tie-break at the decision
     * point** of every argmin here.
     *
     * `CLAUDE.md`: rounding a result file at the serialisation boundary does not make a sweep
     * reproducible if what it reports is an argmin, because two placements can tie to the last
     * unit in the last place and which one is returned then depends on the order of summation.
     */
    val key: String
        get() = rows.joinToString(";") { row ->
            row.row.toString() + ":" + row.roots.joinToString(",") {
                Math.round(it * 1.0e6).toString()
            }
        }

    /** The coupling stations `(x, y)` in nm, row by row and ascending in `x` within a row. */
    fun stations(
        duplexes: Int,
        interhelicalDistance: Double = Gen1Tile.INTERHELICAL_SHEET
    ): List<Pair<Double, Double>> {
        require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
        require(rows.all { it.row < duplexes }) {
            "every row must lie inside a $duplexes-duplex sheet"
        }
        return rows.flatMap { row ->
            val y = (row.row - (duplexes - 1) / 2.0) * interhelicalDistance
            row.roots.map { it to y }
        }
    }

    /**
     * Whether the root set is invariant under `(x, y) → (−x, −y)` — the symmetry a Rothemund
     * sheet has and a mirror one does not (`CLAUDE.md`), asserted on the **set** rather than
     * inferred from the construction.
     */
    fun isCentroSymmetric(duplexes: Int, tolerance: Double = 1e-9): Boolean {
        val byRow = rows.associateBy { it.row }
        return (0 until duplexes).all { row ->
            val mine = byRow[row]?.roots ?: emptyList()
            val partner = byRow[duplexes - 1 - row]?.roots ?: emptyList()
            mine.size == partner.size &&
                    mine.sorted().zip(partner.map { -it }.sorted())
                        .all { (a, b) -> abs(a - b) <= tolerance }
        }
    }

}

/**
 * The phases at which the upward lattice can supply a **centro-symmetric** placement at all.
 *
 * The condition is a congruence and not a search: row `r`'s roots reflect onto row
 * `D − 1 − r`'s lattice only if that lattice is the negation of this one, which for a
 * 15-duplex sheet (where `r` and `14 − r` have the **same** parity and therefore the same
 * sub-lattice) reduces to `2c ≡ 0 (mod p)`. It is checked here on the truncated position sets,
 * so a site lost off the tile edge cannot be waved through.
 *
 * **The cheap bound of `T-125`**, and it costs one pass over 32 phases.
 */
fun centroSymmetricUpwardPhases(
    edgeX: Double,
    duplexes: Int,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR,
    tolerance: Double = 1e-9
): List<Int> = (0 until UPWARD_ROOT_PITCH_BASE_PAIRS).filter { phase ->
    val lattice = upwardRootLattice(phase, edgeX, duplexes, risePerBasePair)
    (0 until duplexes).all { row ->
        val mine = lattice[row]
        val partner = lattice[duplexes - 1 - row].map { -it }.sorted()
        mine.size == partner.size && mine.zip(partner).all { (a, b) -> abs(a - b) <= tolerance }
    }
}

/**
 * Every centro-symmetric placement of [count] roots at [phaseBasePairs], streamed.
 *
 * Rows `0 …` below the middle are free; the rows above them are their reflections; a middle row,
 * where the sheet has one, must be self-symmetric. The count constraint is
 * `2 Σ n_r + n_middle = count`, which is a partition and not a search — so this is an
 * **exhaustive enumeration of the symmetric family**, not a sample of it.
 */
fun centroSymmetricPlacements(
    phaseBasePairs: Int,
    edgeX: Double,
    duplexes: Int,
    arm: Double,
    count: Int,
    minimumPerRow: Int = 1,
    maximumPerRow: Int = 3,
    width: Double = OrigamiDuplex.INTERHELICAL,
    tolerance: Double = 1e-9
): Sequence<UpwardArmPlacement> {
    require(count >= 1) { "count must be at least one, was: $count" }
    val lattice = upwardRootLattice(phaseBasePairs, edgeX, duplexes)
    val half = duplexes / 2
    val middle = if (duplexes % 2 == 1) half else -1
    val options: List<Map<Int, List<List<Double>>>> = (0 until duplexes).map { row ->
        (minimumPerRow..maximumPerRow).associateWith { size ->
            val all = rowRootOptions(lattice[row], size, arm, edgeX, width)
            if (row == middle) {
                all.filter { roots ->
                    roots.zip(roots.map { -it }.sorted()).all { (a, b) -> abs(a - b) <= tolerance }
                }
            } else all
        }
    }
    val free = (0 until half).toList()

    fun mirrored(row: UpwardArmRow, partner: Int): UpwardArmRow {
        val roots = row.roots.map { -it }.sorted()
        val directions = requireNotNull(armDirections(roots, arm, edgeX, width)) {
            "the reflection of a feasible row must itself be feasible on a symmetric lattice, " +
                    "and $roots in row $partner is not — the enumeration is not on the lattice " +
                    "it claims to be on"
        }
        return UpwardArmRow(partner, roots, directions)
    }

    fun expand(index: Int, remaining: Int, taken: List<UpwardArmRow>): Sequence<UpwardArmPlacement> {
        if (index == free.size) {
            val middleCount = remaining
            if (middle < 0) {
                return if (middleCount != 0) emptySequence()
                else sequenceOf(
                    UpwardArmPlacement(
                        phaseBasePairs,
                        (taken + taken.map { mirrored(it, duplexes - 1 - it.row) })
                            .sortedBy { it.row }
                    )
                )
            }
            val candidates = options[middle][middleCount] ?: return emptySequence()
            return candidates.asSequence().map { roots ->
                val directions = armDirections(roots, arm, edgeX, width)!!
                UpwardArmPlacement(
                    phaseBasePairs,
                    (taken + taken.map { mirrored(it, duplexes - 1 - it.row) } +
                            UpwardArmRow(middle, roots, directions)).sortedBy { it.row }
                )
            }
        }
        val row = free[index]
        return (minimumPerRow..maximumPerRow).asSequence().flatMap { size ->
            val left = remaining - 2 * size
            if (left < 0) emptySequence()
            else (options[row][size] ?: emptyList()).asSequence().flatMap { roots ->
                val directions = armDirections(roots, arm, edgeX, width)!!
                expand(index + 1, left, taken + UpwardArmRow(row, roots, directions))
            }
        }
    }
    return expand(0, count, emptyList())
}

/**
 * `C-0055`'s **own** placement at [phaseBasePairs], truncated round-robin to [count] —
 * [placeUpwardArms] and [HingeArmPlacement.truncatedTo], re-run as libraries rather than
 * retyped, so that this is the array `C-0061` measured and not a reconstruction of it.
 */
fun greedyUpwardPlacement(
    phaseBasePairs: Int,
    edgeX: Double,
    duplexes: Int,
    arm: Double,
    count: Int,
    width: Double = OrigamiDuplex.INTERHELICAL
): UpwardArmPlacement {
    val placed = placeUpwardArms(phaseBasePairs, edgeX, duplexes, arm, width).truncatedTo(count)
    val byRow = placed.placements.groupBy { it.row }.toSortedMap()
    return UpwardArmPlacement(
        phaseBasePairs,
        byRow.map { (row, arms) ->
            val sorted = arms.sortedBy { it.rootX }
            UpwardArmRow(row, sorted.map { it.rootX }, sorted.map { it.towardPositiveX })
        }
    )
}

// ------------------------------------------------------------------------ the influence bank

/**
 * One free solution and one unit-point-load solution per candidate root, sampled once, from
 * which the response of **any** coupling at **any** subset of those roots follows exactly.
 *
 * This is what makes a placement sweep affordable. `C-0058`'s [InfluenceSurrogate] is an exact
 * Woodbury reduction of a linear system, and the stations enter an [OrigamiGrillage] solve as
 * *loads* — so the host is factorised **once** and every candidate root costs one
 * back-substitution, after which a whole 34-root placement costs a 34 × 34 Cholesky instead of
 * an 855-degree-of-freedom one.
 *
 * @param lattice the host, which must carry **no** supports: the coupling is carried here.
 */
class UpwardRootInfluenceBank(
    val lattice: OrigamiGrillage,
    val stations: List<Pair<Double, Double>>,
    pressure: PressureField,
    val samples: Int = 81
) {

    init {
        require(lattice.supports.isEmpty()) {
            "the bank carries the coupling itself, so the host must be assembled without any " +
                    "supports: it had ${lattice.supports.size}"
        }
        require(stations.isNotEmpty()) { "stations must not be empty" }
        require(samples >= 2) { "samples must be at least 2, was: $samples" }
    }

    private val halfX = lattice.lengthX / 2.0

    private val halfY = lattice.lengthY / 2.0

    private val free = lattice.solve(pressure)

    private val influence = stations.map { (x, y) ->
        lattice.solve(uniformPressure(0.0), listOf(PointLoad(x, y, 1.0)))
    }

    /** The peak dishing in nm of the host under the load alone — the *no coupling at all* bar. */
    val freePeakDishing: Double = free.peakDishing(samples)

    private fun sample(dishing: (Double, Double) -> Double): DoubleArray {
        val field = DoubleArray(samples * samples)
        for (i in 0 until samples) {
            val x = -halfX + 2.0 * halfX * i / (samples - 1)
            for (j in 0 until samples) {
                val y = -halfY + 2.0 * halfY * j / (samples - 1)
                field[i * samples + j] = dishing(x, y)
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

    private val dishingFree = sample { x, y -> free.dishing(x, y) }

    private val dishingInfluence = Array(stations.size) { k ->
        sample { x, y -> influence[k].dishing(x, y) }
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

    /** The index of the station at ([x], [y]), or `−1`. */
    fun indexOf(x: Double, y: Double, tolerance: Double = 1e-9): Int =
        stations.indexOfFirst { abs(it.first - x) <= tolerance && abs(it.second - y) <= tolerance }

}

// ------------------------------------------------------------------------ the descent

/** What a [descendPlacement] found, and what it cost. */
data class PlacementDescent(
    val placement: UpwardArmPlacement,
    val objective: Double,
    val evaluations: Int,
    val sweeps: Int
)

/**
 * A deterministic cyclic descent over the row options of a placement, at fixed total count.
 *
 * Two move classes, in this order and both exhaustive within themselves:
 *
 * 1. **row options at fixed count** — every set of roots the row could take instead;
 * 2. **promote and demote** — one arm moved from a row carrying more than [minimumPerRow] to a
 *    row carrying fewer than [maximumPerRow], over every option of both rows, which is what
 *    lets the *count vector* move and not only the positions.
 *
 * A move is accepted only if it improves the objective **rounded at the decision point**
 * (`CLAUDE.md`: a sweep that reports an argmin is not reproducible unless the comparison is
 * rounded too), with the placement's own canonical [UpwardArmPlacement.key] as the tie-break.
 *
 * This is a **descent** and reports what it found; `InfluenceSurrogate.reachableDishingFloor` is
 * what says how much room may be left.
 */
fun descendPlacement(
    start: UpwardArmPlacement,
    sites: List<List<Double>>,
    arm: Double,
    edgeX: Double,
    width: Double = OrigamiDuplex.INTERHELICAL,
    minimumPerRow: Int = 2,
    maximumPerRow: Int = 3,
    sweeps: Int = 8,
    objective: (UpwardArmPlacement) -> Double
): PlacementDescent {
    require(sweeps >= 1) { "sweeps must be at least one, was: $sweeps" }
    require(minimumPerRow >= 1) { "minimumPerRow must be at least one, was: $minimumPerRow" }
    require(maximumPerRow >= minimumPerRow) {
        "maximumPerRow must be at least minimumPerRow, were: $maximumPerRow, $minimumPerRow"
    }
    val options: List<Map<Int, List<List<Double>>>> = sites.indices.map { row ->
        (minimumPerRow..maximumPerRow).associateWith {
            rowRootOptions(sites[row], it, arm, edgeX, width)
        }
    }

    fun rowOf(roots: List<Double>, row: Int) =
        UpwardArmRow(row, roots, armDirections(roots, arm, edgeX, width)!!)

    var current = start
    var best = roundForResult(objective(current))
    var evaluations = 1
    var used = 0
    for (sweep in 1..sweeps) {
        val before = best
        // (1) row options at fixed count
        current.rows.indices.forEach { index ->
            val row = current.rows[index]
            (options[row.row][row.count] ?: emptyList()).forEach { roots ->
                val candidate = current.copy(
                    rows = current.rows.toMutableList().also { it[index] = rowOf(roots, row.row) }
                )
                val value = roundForResult(objective(candidate))
                evaluations++
                if (value < best || (value == best && candidate.key < current.key)) {
                    best = value
                    current = candidate
                }
            }
        }
        // (2) promote and demote, which moves the count vector
        current.rows.indices.forEach { donor ->
            current.rows.indices.forEach { receiver ->
                val from = current.rows[donor]
                val to = current.rows[receiver]
                if (donor == receiver) return@forEach
                if (from.count - 1 < minimumPerRow || to.count + 1 > maximumPerRow) return@forEach
                (options[from.row][from.count - 1] ?: emptyList()).forEach { donorRoots ->
                    (options[to.row][to.count + 1] ?: emptyList()).forEach { receiverRoots ->
                        val rows = current.rows.toMutableList()
                        rows[donor] = rowOf(donorRoots, from.row)
                        rows[receiver] = rowOf(receiverRoots, to.row)
                        val candidate = current.copy(rows = rows)
                        val value = roundForResult(objective(candidate))
                        evaluations++
                        if (value < best || (value == best && candidate.key < current.key)) {
                            best = value
                            current = candidate
                        }
                    }
                }
            }
        }
        used = sweep
        if (best >= before) break
    }
    return PlacementDescent(current, best, evaluations, used)
}
