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
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * `T-151` — **can the Gen-1 tile be raster-folded without a scaffold seam?**
 *
 * `C-0081` shows a seam takes **6–12** of `C-0063`'s 34 stations off `C-0076`'s weave node and
 * restores the amplitude bracket `C-0076` annihilated, driving the worst across-row clearance
 * **negative** at the cryo-EM amplitude. A seamless routing buys all of that back — if one exists.
 *
 * ## The whole answer is a parity on a tree, and this file computes it
 *
 * A crossover joins only **adjacent** duplexes, so the row-adjacency graph of a single-layer sheet
 * is the **path** `P_D` — a tree. A **closed** walk on a tree traverses every edge an **even**
 * number of times, so:
 *
 * - a **fully folded circular** scaffold gives **every** row at least **two** segments, i.e. a seam
 *   crossing every row — [minimumSegmentsPerRow] returns 2 for every `D ≥ 3`;
 * - a **linear** scaffold needs a Hamiltonian **path**, which `P_D` has (uniquely, up to direction)
 *   for every `D` — [hamiltonianRowPathCount];
 * - a **circular** scaffold that is **not** fully folded closes through its own unpaired remainder,
 *   which is an extra edge, and is seamless too.
 *
 * Rothemund's own record is the check and it agrees: his 26-helix square *"had no vertical
 * reversals in raster direction, **required a linear scaffold**"*, while every rectangle — folded
 * from the whole circular M13 — carries a seam.
 *
 * ## Conventions, restated rather than inherited
 *
 * - Lengths **nm**; the sheet is `D` duplexes at the SAXS **2.69 nm**, rise **0.34 nm**, square
 *   lattice at **32/3 bp per turn**; `x` runs **along** the helices and `y` **across** them.
 * - **A row** is one duplex; **a scaffold segment** is a maximal run of scaffold inside one row.
 * - **A seam** is Rothemund's *"a contour which the path does not cross"*, i.e. the locus where a
 *   row carries two scaffold segments meeting end to end. **Seamless** means one segment per row.
 * - A **plane** is `C-0055`'s 8 bp crossover plane, the coordinate `C-0076` and `C-0081` use.
 */

// ---------------------------------------------------------------- the scaffold specification

/** What a scaffold is, as far as a routing is concerned. */
enum class ScaffoldTopology(val closed: Boolean, val fullyFolded: Boolean) {

    /** A linearised or synthetic scaffold with two free ends — Rothemund's square and star. */
    LINEAR(false, true),

    /** A circular scaffold with every nucleotide paired into the sheet — his rectangles. */
    CIRCULAR_FULLY_FOLDED(true, true),

    /**
     * A circular scaffold whose unused length is left as an unpaired remainder — his first
     * experiment, the 8-helix third-square, *"a circular M13mp18 scaffold DNA was used … because
     * the corners of the rectangle were close enough that the unfolded portion … could easily
     * bridge the corners without deforming the rectangle"*.
     */
    CIRCULAR_WITH_REMAINDER(true, false)
}

/** The row-adjacency graph of a [duplexes]-duplex single-layer sheet: the path `P_D`. */
fun rowAdjacency(duplexes: Int): List<List<Int>> {
    require(duplexes >= 1) { "duplexes must be at least one, was: $duplexes" }
    return (0 until duplexes).map { row ->
        listOf(row - 1, row + 1).filter { it in 0 until duplexes }
    }
}

/** How many Hamiltonian **paths** the row graph carries — brute-forced, not asserted. */
fun hamiltonianRowPathCount(duplexes: Int): Int = hamiltonianCount(duplexes, closed = false)

/** How many Hamiltonian **cycles** the row graph carries — brute-forced, not asserted. */
fun hamiltonianRowCycleCount(duplexes: Int): Int = hamiltonianCount(duplexes, closed = true)

private fun hamiltonianCount(duplexes: Int, closed: Boolean): Int {
    require(duplexes in 1..12) { "the brute force is capped at twelve rows, was: $duplexes" }
    val adjacency = rowAdjacency(duplexes)
    if (duplexes == 1) return if (closed) 0 else 1
    var found = 0
    val visited = BooleanArray(duplexes)
    fun walk(current: Int, depth: Int, start: Int) {
        if (depth == duplexes) {
            if (!closed || current in adjacency[start]) found++
            return
        }
        adjacency[current].forEach { next ->
            if (!visited[next]) {
                visited[next] = true
                walk(next, depth + 1, start)
                visited[next] = false
            }
        }
    }
    (0 until duplexes).forEach { start ->
        visited.fill(false)
        visited[start] = true
        walk(start, 1, start)
    }
    return found
}

/**
 * **The minimum number of scaffold segments every row must carry** under [topology].
 *
 * A closed walk on a **tree** traverses every edge an even number of times, and every edge of
 * `P_D` must be traversed at least once for the scaffold to reach every row — so under a fully
 * folded circular scaffold every row is entered at least twice. Under a linear scaffold, or a
 * circular one closing through an unpaired remainder, one segment per row suffices.
 */
fun minimumSegmentsPerRow(topology: ScaffoldTopology, duplexes: Int): Int {
    require(duplexes >= 1) { "duplexes must be at least one, was: $duplexes" }
    if (!topology.closed || !topology.fullyFolded) return 1
    // D <= 2 the row graph is a complete graph and a Hamiltonian cycle exists
    return if (duplexes <= 2) 1 else 2
}

/** What a scaffold specification implies for a [duplexes]-duplex single-layer sheet. */
data class SeamlessVerdict(
    val topology: ScaffoldTopology,
    val duplexes: Int,
    val segmentsPerRow: Int,
    val seamsRequired: Int,
    val scaffoldCrossovers: Int,
    val seamless: Boolean,
    val reason: String
)

/** [SeamlessVerdict] for [topology] on a [duplexes]-duplex sheet. */
fun seamlessRoutingVerdict(topology: ScaffoldTopology, duplexes: Int): SeamlessVerdict {
    val segments = minimumSegmentsPerRow(topology, duplexes)
    return SeamlessVerdict(
        topology = topology,
        duplexes = duplexes,
        segmentsPerRow = segments,
        seamsRequired = segments - 1,
        scaffoldCrossovers = duplexes * segments - if (topology.closed) 0 else 1,
        seamless = segments == 1,
        reason = if (segments == 1) {
            if (topology.closed) {
                "a circular scaffold that is not fully folded closes through its own unpaired " +
                        "remainder, which is an edge the row graph does not have"
            } else {
                "a linear scaffold needs a Hamiltonian PATH, and the path graph P_$duplexes has " +
                        "exactly ${hamiltonianRowPathCount(minOf(duplexes, 12))} of them"
            }
        } else {
            "a fully folded circular scaffold needs a closed walk on a TREE, which traverses " +
                    "every edge an even number of times, so every one of the $duplexes rows " +
                    "carries at least two segments"
        }
    )
}

// ---------------------------------------------------------------- the constructed routes

/** One maximal run of scaffold inside one row. */
data class RasterSegment(val row: Int, val order: Int, val towardPositiveX: Boolean, val half: Int)

/** A scaffold routing over the rows of a single-layer sheet. */
data class RasterRoute(
    val label: String,
    val duplexes: Int,
    val topology: ScaffoldTopology,
    val segments: List<RasterSegment>
) {

    /** Segments per row, ascending by row. */
    val segmentsPerRow: List<Int>
        get() = (0 until duplexes).map { row -> segments.count { it.row == row } }

    /** Whether every row carries exactly one segment. */
    val seamless: Boolean get() = segmentsPerRow.all { it == 1 }

    /** Whether consecutive segments sit on adjacent rows or share a row (a seam crossing). */
    val connected: Boolean
        get() = segments.zipWithNext().all { (a, b) ->
            abs(a.row - b.row) == 1 || (a.row == b.row && a.half != b.half)
        }

    /** Whether the route closes on itself — required of a circular scaffold with no remainder. */
    val closes: Boolean
        get() = segments.size >= 2 && segments.first().let { first ->
            segments.last().let { last ->
                abs(last.row - first.row) == 1 || (last.row == first.row && last.half != first.half)
            }
        }
}

/**
 * **The seamless route**: a plain boustrophedon, one segment per row, entering each row at the end
 * the previous one left. Rothemund's 26-helix square, which *"had no vertical reversals in raster
 * direction"*.
 */
fun boustrophedonRoute(duplexes: Int, topology: ScaffoldTopology = ScaffoldTopology.LINEAR):
        RasterRoute {
    require(duplexes >= 1) { "duplexes must be at least one, was: $duplexes" }
    require(minimumSegmentsPerRow(topology, duplexes) == 1) {
        "a $topology scaffold cannot carry one segment per row on $duplexes duplexes"
    }
    return RasterRoute(
        "boustrophedon", duplexes, topology,
        (0 until duplexes).map { RasterSegment(it, it, it % 2 == 0, 0) }
    )
}

/**
 * **The seamed route**: Rothemund's double raster — down the left halves, across at the far row,
 * back up the right halves — which is what a fully folded circular scaffold is forced into.
 */
fun doubleRasterRoute(duplexes: Int): RasterRoute {
    require(duplexes >= 2) { "duplexes must be at least two, was: $duplexes" }
    val down = (0 until duplexes).map { RasterSegment(it, it, false, 0) }
    val up = (duplexes - 1 downTo 0).mapIndexed { index, row ->
        RasterSegment(row, duplexes + index, true, 1)
    }
    return RasterRoute(
        "double raster", duplexes, ScaffoldTopology.CIRCULAR_FULLY_FOLDED, down + up
    )
}

// ------------------------------------------- Rothemund's odd half-turn constraint on the width

/**
 * Rothemund's **fundamental constraint**, read directly: *"for the scaffold to raster progressively
 * from one helix to another and onto a third, the distance between successive scaffold crossovers
 * must be an odd number of half turns."*
 *
 * A boustrophedon has **only** progressive crossovers, and its successive scaffold crossovers are
 * the two ends of one row — so the row **length** is the quantity the constraint binds.
 */
fun isOddHalfTurnSeparation(
    basePairs: Int,
    basePairsPerTurn: Double = SQUARE_LATTICE_BASE_PAIRS_PER_TURN,
    tolerance: Double = 1.0e-9
): Boolean {
    require(basePairs > 0) { "basePairs must be positive, was: $basePairs" }
    require(basePairsPerTurn > 0.0) {
        "basePairsPerTurn must be positive, was: $basePairsPerTurn"
    }
    val halfTurns = basePairs / (basePairsPerTurn / 2.0)
    val rounded = halfTurns.roundToInt()
    return abs(halfTurns - rounded) < tolerance && rounded % 2 == 1
}

/** Every row length up to [maximumBasePairs] that [isOddHalfTurnSeparation] admits. */
fun admissibleRasterRowLengths(
    maximumBasePairs: Int,
    basePairsPerTurn: Double = SQUARE_LATTICE_BASE_PAIRS_PER_TURN
): List<Int> = (1..maximumBasePairs).filter { isOddHalfTurnSeparation(it, basePairsPerTurn) }

/** The admissible row length nearest [target] nm, or `null` where none exists below the cap. */
fun nearestAdmissibleWidth(
    target: Double,
    maximumBasePairs: Int = 400,
    basePairsPerTurn: Double = SQUARE_LATTICE_BASE_PAIRS_PER_TURN,
    rise: Double = Gen1Tile.RISE_PER_BASE_PAIR
): Int? = admissibleRasterRowLengths(maximumBasePairs, basePairsPerTurn)
    .minByOrNull { abs(it * rise - target) }

// ---------------------------------------------------------------- the scaffold budget

/** The nucleotides a [duplexes]-row sheet of [basePairsPerRow] takes out of a scaffold. */
fun sheetScaffoldNucleotides(duplexes: Int, basePairsPerRow: Int): Int {
    require(duplexes >= 1) { "duplexes must be at least one, was: $duplexes" }
    require(basePairsPerRow >= 1) {
        "basePairsPerRow must be at least one, was: $basePairsPerRow"
    }
    return duplexes * basePairsPerRow
}

/**
 * The nucleotides an unpaired **return loop** needs to carry a circular scaffold from the last row
 * back to the first, outside the sheet: the across-sheet span over the contour per nucleotide.
 */
fun returnLoopNucleotides(
    duplexes: Int,
    interhelicalDistance: Double = Gen1Tile.INTERHELICAL_SHEET,
    contourPerNucleotide: Double = SsDnaTether.CONTOUR_PER_NUCLEOTIDE_MIN
): Int {
    require(duplexes >= 2) { "duplexes must be at least two, was: $duplexes" }
    require(contourPerNucleotide > 0.0) {
        "contourPerNucleotide must be positive, was: $contourPerNucleotide"
    }
    return ceil((duplexes - 1) * interhelicalDistance / contourPerNucleotide).toInt()
}

/** The ideal-chain radius of gyration of an unpaired scaffold remainder, in nm. */
fun singleStrandedRadiusOfGyration(
    nucleotides: Int,
    kuhnLength: Double = SsDnaTether.KUHN_LENGTH_ZERO_FORCE,
    contourPerNucleotide: Double = SsDnaTether.CONTOUR_PER_NUCLEOTIDE_MIN
): Double {
    require(nucleotides >= 1) { "nucleotides must be at least one, was: $nucleotides" }
    require(kuhnLength > 0.0) { "kuhnLength must be positive, was: $kuhnLength" }
    require(contourPerNucleotide > 0.0) {
        "contourPerNucleotide must be positive, was: $contourPerNucleotide"
    }
    val kuhnSegments = nucleotides * contourPerNucleotide / kuhnLength
    return kuhnLength * sqrt(kuhnSegments / 6.0)
}

/**
 * What is left of M13mp18 after BsrBI digestion, in nucleotides — **CITED, READ DIRECTLY**:
 * *"While 7,176 nt remained available for folding, most designs did not fold all 7,176 nt"*
 * (Rothemund 2006, main text). The circular length itself is `C-0055`'s
 * [M13_SCAFFOLD_NUCLEOTIDES].
 */
const val M13_LINEARISED_NUCLEOTIDES: Int = 7176

// ---------------------------------------------------------------- the staggered seam

/** One staggered-seam assignment: a split plane per row, and what it costs in stations. */
data class StaggeredSeam(val planes: List<Int>, val affectedStations: Int)

/**
 * **The staggered seam that costs the fewest stations** — Rothemund's own alternative, read
 * directly: *"it is possible to create staggered seams (as E. Winfree has suggested) so that
 * staple strands naturally cross and bridge the seam vertically"*. A seam is *"a contour"*, and
 * nothing makes a contour straight.
 *
 * `C-0081`'s cost mechanism is that the junctions absent at a plane straighten the duplexes and pin
 * the **interfaces** at an extremum. A station on row `b` sits between interfaces `b−1` and `b`,
 * which are moved by duplexes `b−1`, `b` and `b+1` — so it survives only if **none of those three
 * rows** puts its own split within [window] planes of it. That three-row coupling is solved exactly
 * by a dynamic program over the [candidatePlanes] of *consecutive row pairs*; the state space is
 * `|candidatePlanes|²` and the answer is a minimum, not a search.
 */
fun bestStaggeredSeam(
    stationPlanesByRow: List<List<Int>>,
    candidatePlanes: List<Int>,
    window: Int = 2
): StaggeredSeam {
    require(stationPlanesByRow.isNotEmpty()) { "stationPlanesByRow must not be empty" }
    require(candidatePlanes.isNotEmpty()) { "candidatePlanes must not be empty" }
    require(window >= 1) { "window must be at least one, was: $window" }
    val rows = stationPlanesByRow.size

    /** How many of row [row]'s stations the splits at [below], [here] and [above] take off node. */
    fun rowCost(row: Int, below: Int?, here: Int, above: Int?): Int =
        stationPlanesByRow[row].count { plane ->
            abs(plane - here) < window ||
                    (below != null && abs(plane - below) < window) ||
                    (above != null && abs(plane - above) < window)
        }

    if (rows == 1) {
        val best = candidatePlanes.minBy { rowCost(0, null, it, null) }
        return StaggeredSeam(listOf(best), rowCost(0, null, best, null))
    }
    // states are (previous row's split, this row's split); a transition closes the middle row
    var reachable: Map<Pair<Int, Int>, Pair<Int, List<Int>>> = HashMap<
            Pair<Int, Int>, Pair<Int, List<Int>>>().apply {
        candidatePlanes.forEach { first ->
            candidatePlanes.forEach { second ->
                put(first to second, rowCost(0, null, first, second) to listOf(first, second))
            }
        }
    }
    for (index in 1 until rows - 1) {
        val next = HashMap<Pair<Int, Int>, Pair<Int, List<Int>>>()
        reachable.forEach { (state, carried) ->
            val (below, here) = state
            val (cost, path) = carried
            candidatePlanes.forEach { above ->
                val total = cost + rowCost(index, below, here, above)
                val key = here to above
                val incumbent = next[key]
                if (incumbent == null || total < incumbent.first) {
                    next[key] = total to (path + above)
                }
            }
        }
        reachable = next
    }
    val best = reachable.entries.minBy { (state, carried) ->
        carried.first + rowCost(rows - 1, state.first, state.second, null)
    }
    return StaggeredSeam(
        best.value.second,
        best.value.first + rowCost(rows - 1, best.key.first, best.key.second, null)
    )
}

/** [bestStaggeredSeam]'s planes when it costs **no** station at all, and `null` otherwise. */
fun staggeredSeamAssignment(
    stationPlanesByRow: List<List<Int>>,
    candidatePlanes: List<Int>,
    window: Int = 2
): List<Int>? = bestStaggeredSeam(stationPlanesByRow, candidatePlanes, window)
    .takeIf { it.affectedStations == 0 }?.planes

/** How many stations a **straight** seam at [seamPlane] takes off their node. */
fun straightSeamCost(
    stationPlanesByRow: List<List<Int>>,
    seamPlane: Int,
    window: Int = 2
): Int {
    require(stationPlanesByRow.isNotEmpty()) { "stationPlanesByRow must not be empty" }
    require(window >= 1) { "window must be at least one, was: $window" }
    return stationPlanesByRow.sumOf { row -> row.count { abs(it - seamPlane) < window } }
}
