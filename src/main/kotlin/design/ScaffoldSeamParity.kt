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

package com.xemantic.nano.plentyofroom.design

import com.xemantic.nano.plentyofroom.anchoring.SsDnaTether
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.HoneycombCell
import kotlin.math.ceil
import kotlin.math.max
import kotlin.math.sqrt

/**
 * `T-274` — whether a honeycomb block's scaffold needs a **seam**, derived on the block's own
 * cross-section adjacency rather than inherited from the single-layer sheet.
 *
 * ## The theorem has two premises, and only one of them is about geometry
 *
 * `CLAUDE.md`, in its own words:
 *
 * > A scaffold **seam** is a parity on a tree, not a fabrication convention. Crossovers join only
 * > *adjacent* duplexes, so a single-layer sheet's row-adjacency graph is the path `P_D`, a
 * > **tree** — and a closed walk on a tree traverses every edge an **even** number of times. A
 * > **fully folded circular** scaffold therefore gives every row **two** segments, which is
 * > exactly Rothemund's seam; a **linear** scaffold needs only a Hamiltonian path, and a circular
 * > scaffold left partly unfolded closes through its own remainder. **A seam needs BOTH premises
 * > and dropping either removes it.**
 *
 * So there is **(P1)** *the scaffold's graph is a tree* and **(P2)** *the scaffold is fully folded
 * and circular, so its closure is itself an edge of that graph*. `C-0119` §4 argues (P1) for a
 * honeycomb block by restricting the scaffold to Douglas et al.'s *"the path of the scaffold stays
 * within a 2D surface"*, and does not examine (P2). This file supplies both readings of the graph
 * and prices (P2)'s escape.
 *
 * ## Conventions, fixed before deriving
 *
 * Cells are `HoneycombRasterTurnSense`'s integer `HoneycombCell(x, y)`, `x` in units of `d√3/2`
 * and `y` in units of `d/2`. Lengths **nm**, energies **k_BT**, counts integers. A *domain* is one
 * contiguous run of the scaffold on one helix, which is one **visit** of the walk; a closed walk
 * of `m` edge traversals makes exactly `m` visits, so *domains = edge traversals*.
 */

/**
 * The graph of admissible scaffold crossovers on an ordered list of helices.
 *
 * @param cells the helices, in raster order — index `i` is helix `i` of the drawn design.
 * @param edges unordered index pairs; duplicates and self-loops are refused.
 */
data class ScaffoldGraph(
    val cells: List<HoneycombCell>,
    val edges: List<Pair<Int, Int>>
) {

    init {
        require(cells.size >= 2) { "a scaffold graph needs at least two helices, was: ${cells.size}" }
        edges.forEach { (a, b) ->
            require(a in cells.indices && b in cells.indices) {
                "edge ($a, $b) leaves the ${cells.size} helices it is drawn on"
            }
            require(a != b) { "a helix cannot cross over to itself: ($a, $b)" }
        }
        require(edges.map { setOf(it.first, it.second) }.toSet().size == edges.size) {
            "the edge list repeats a pair, which would double-count a crossover"
        }
    }

    /** Helices. */
    val order: Int get() = cells.size

    /** Admissible crossovers. */
    val size: Int get() = edges.size

    private val adjacency: List<ArrayList<Int>> = List(order) { ArrayList<Int>() }.also { lists ->
        edges.forEach { (a, b) ->
            lists[a].add(b)
            lists[b].add(a)
        }
    }

    /** Neighbours of helix [index] in this graph. */
    fun neighbours(index: Int): List<Int> = adjacency[index]

    /** The degree sequence, in helix order. */
    val degrees: List<Int> get() = adjacency.map { it.size }

    /** The helices of degree one, ascending — a vertex of degree one lies on no cycle. */
    val leaves: List<Int> get() = degrees.indices.filter { degrees[it] == 1 }

    /** Whether the graph is connected. */
    val isConnected: Boolean
        get() {
            val seen = BooleanArray(order)
            val stack = ArrayDeque(listOf(0))
            seen[0] = true
            var count = 1
            while (stack.isNotEmpty()) {
                adjacency[stack.removeLast()].forEach { next ->
                    if (!seen[next]) {
                        seen[next] = true
                        count++
                        stack.addLast(next)
                    }
                }
            }
            return count == order
        }

    /** `|E| − |V| + 1` for a connected graph: the number of independent cycles. */
    val cyclomaticNumber: Int
        get() {
            require(isConnected) { "the cyclomatic number of a disconnected graph is not one number" }
            return size - order + 1
        }

    /** A connected graph is a tree exactly when it has `|V| − 1` edges. */
    val isTree: Boolean get() = isConnected && size == order - 1

    /**
     * The bridges, as index pairs in the order they appear in [edges].
     *
     * A bridge is an edge whose removal disconnects the graph; a closed walk covering both sides
     * must therefore traverse it an **even** number of times, i.e. at least twice. That is the
     * whole of the seam argument, and it is why a tree — every edge a bridge — is the expensive
     * case.
     */
    val bridges: List<Pair<Int, Int>>
        get() {
            val discovery = IntArray(order) { -1 }
            val low = IntArray(order)
            val found = ArrayList<Pair<Int, Int>>()
            var timer = 0

            // an explicit stack: a 60-helix block is small, but a recursion limit is not a bound
            val incident = List(order) { ArrayList<Pair<Int, Int>>() }
            edges.forEachIndexed { edgeIndex, (a, b) ->
                incident[a].add(b to edgeIndex)
                incident[b].add(a to edgeIndex)
            }
            (0 until order).forEach { root ->
                if (discovery[root] >= 0) return@forEach
                val frame = ArrayDeque<Triple<Int, Int, Int>>()
                discovery[root] = timer
                low[root] = timer
                timer++
                frame.addLast(Triple(root, -1, 0))
                while (frame.isNotEmpty()) {
                    val (node, parentEdge, cursor) = frame.removeLast()
                    if (cursor < incident[node].size) {
                        frame.addLast(Triple(node, parentEdge, cursor + 1))
                        val (next, edgeIndex) = incident[node][cursor]
                        if (edgeIndex == parentEdge) continue
                        if (discovery[next] >= 0) {
                            low[node] = minOf(low[node], discovery[next])
                        } else {
                            discovery[next] = timer
                            low[next] = timer
                            timer++
                            frame.addLast(Triple(next, edgeIndex, 0))
                        }
                    } else if (parentEdge >= 0) {
                        val parent = frame.last().first
                        low[parent] = minOf(low[parent], low[node])
                        if (low[node] > discovery[parent]) found.add(edges[parentEdge])
                    }
                }
            }
            return found.sortedBy { edges.indexOf(it) }
        }

    /**
     * Whether a Hamiltonian cycle exists — `true`, `false`, or `null` where the search **refuses**.
     *
     * A helix of degree one lies on no cycle at all, so the answer is `false` with no search
     * wherever the graph has a leaf; that is what settles the honeycomb block. Beyond
     * [orderLimit] the exhaustive branch refuses rather than running a factorial, which is
     * `C-0119`'s own guard discipline — its brute force stops at order 9 for the same reason.
     */
    fun hasHamiltonianCycle(orderLimit: Int = HAMILTONIAN_ORDER_LIMIT): Boolean? {
        require(orderLimit >= 3) { "orderLimit must be at least three, was: $orderLimit" }
        if (order < 3) return false
        if (!isConnected) return false
        if (degrees.any { it < 2 }) return false
        if (order > orderLimit) return null
        val visited = BooleanArray(order)
        visited[0] = true

        fun search(current: Int, depth: Int): Boolean {
            if (depth == order) return adjacency[current].contains(0)
            adjacency[current].forEach { next ->
                if (!visited[next]) {
                    visited[next] = true
                    if (search(next, depth + 1)) return true
                    visited[next] = false
                }
            }
            return false
        }
        return search(0, 1)
    }

    /**
     * A lower bound on the **domains** a fully folded circular scaffold needs on this graph.
     *
     * Let `H` be the multigraph of the closed walk's traversals. `H` spans every helix, is
     * connected and has every degree even, so `|E(H)| = ½ Σ deg_H(v)`. Two independent bounds
     * follow and the answer is their maximum:
     *
     * - **the degree bound.** Every covered helix has `deg_H ≥ 2`. A **leaf**'s single edge must
     *   carry an even multiplicity, so `deg_H = ` that multiplicity `≥ 2`. A helix `u` carrying
     *   `k ≥ 1` leaves takes `≥ 2k` from them alone, **and two more only if there is a rest of the
     *   graph for it to reach** — `V ∖ ({u} ∪ its leaves)` non-empty. That last qualifier is not a
     *   nicety: on the three-vertex path the middle helix carries **both** leaves and has no rest,
     *   and asserting the `+2` there returns 5 against a true minimum of 4.
     * - **the bridge bound.** Every bridge is in `H` with multiplicity at least two, and `H`'s
     *   underlying simple graph is spanning and connected, so it carries at least `|V| − 1`
     *   distinct edges: `|E(H)| ≥ (|V| − 1) + bridges`.
     *
     * On a path — Douglas et al.'s 2-D-surface restriction — every edge is a bridge and the second
     * bound gives `2(|V| − 1)`, which is Rothemund's seam: two segments per helix. On a honeycomb
     * block's own induced adjacency only the two pendant edges are bridges, the first bound binds,
     * and it is far smaller.
     *
     * Both bounds are sound at every order, `|V| = 2` included, which is why there is no guard.
     */
    val minimumClosedCoveringWalk: Int
        get() {
            require(isConnected) { "a covering closed walk needs a connected graph" }
            val leafSet = leaves.toSet()
            var degreeSum = 0
            (0 until order).forEach { v ->
                degreeSum += if (v in leafSet) {
                    2
                } else {
                    val attachedLeaves = adjacency[v].count { it in leafSet }
                    val restOfGraph = order - 1 - attachedLeaves
                    when {
                        attachedLeaves == 0 -> 2
                        restOfGraph > 0 -> 2 * attachedLeaves + 2
                        else -> maxOf(2, 2 * attachedLeaves)
                    }
                }
            }
            return max(degreeSum / 2, order - 1 + bridges.size)
        }

    companion object {

        /**
         * The largest order the exhaustive Hamiltonian-cycle branch will attempt.
         *
         * Sixty-four covers every honeycomb block this programme has considered *and* is never
         * reached by it, because every such block has two leaves and is decided before the search.
         */
        const val HAMILTONIAN_ORDER_LIMIT: Int = 64
    }
}

/**
 * Douglas et al.'s reading: *"the path of the scaffold stays within a 2D surface"*, so the only
 * admissible scaffold crossovers are the raster's own consecutive pairs. This is `C-0119` §4's
 * graph, and it is a path by construction.
 */
fun rasterSurfaceScaffoldGraph(path: List<HoneycombCell>): ScaffoldGraph =
    ScaffoldGraph(path, (0 until path.size - 1).map { it to it + 1 })

/**
 * The honeycomb lattice's own adjacency, **induced** on the raster's cells — every pair of helices
 * the cross-section puts in contact, whether or not the raster runs between them.
 *
 * This is the permissive reading, and it is the one `C-0154` says a honeycomb block has: three
 * lattice neighbours per site rather than a path's two.
 */
fun inducedLatticeScaffoldGraph(path: List<HoneycombCell>): ScaffoldGraph {
    val index = path.withIndex().associate { (i, cell) -> cell to i }
    require(index.size == path.size) { "the raster path repeats a helix" }
    val edges = ArrayList<Pair<Int, Int>>()
    path.forEachIndexed { i, cell ->
        cell.neighbours.forEach { neighbour ->
            val j = index[neighbour]
            if (j != null && i < j) edges.add(i to j)
        }
    }
    return ScaffoldGraph(path, edges)
}

/** The cross-section position of [cell] in nm, `x` at `d√3/2` and `y` at `d/2`. */
fun honeycombCellPosition(
    cell: HoneycombCell,
    bondLength: Double = Gen1Tile.INTERHELICAL_HONEYCOMB
): Pair<Double, Double> {
    require(bondLength > 0.0) { "bondLength must be positive, was: $bondLength" }
    return cell.x * bondLength * sqrt(3.0) / 2.0 to cell.y * bondLength / 2.0
}

/**
 * The distance in nm an unpaired scaffold remainder must span to close the circle: the separation
 * of the raster path's two **termini**.
 *
 * @param axialSeparation the along-helix separation of the two strand ends in nm. It is **zero**
 *          for the committed block, whose 5' and 3' termini both sit at offset 7.
 */
fun rasterTerminusSeparation(
    path: List<HoneycombCell>,
    bondLength: Double = Gen1Tile.INTERHELICAL_HONEYCOMB,
    axialSeparation: Double = 0.0
): Double {
    require(path.size >= 2) { "a raster path needs at least two helices, was: ${path.size}" }
    val (x0, y0) = honeycombCellPosition(path.first(), bondLength)
    val (x1, y1) = honeycombCellPosition(path.last(), bondLength)
    return sqrt(
        (x1 - x0) * (x1 - x0) + (y1 - y0) * (y1 - y0) + axialSeparation * axialSeparation
    )
}

/**
 * One reading of ssDNA's elasticity. `CLAUDE.md`: the Kuhn length is a **2× method-systematic
 * bracket**, 1.34–1.41 nm from 10–40 pN force spectroscopy against 2.10–2.84 nm from zero-force
 * scattering, and *"the contour per nucleotide travels with the elastic model too … mixing them
 * double-counts the extension"*. So the two travel as a pair and never as four independent numbers.
 */
data class SsDnaConvention(
    val name: String,
    val kuhnLength: Double,
    val contourPerNucleotide: Double
) {
    init {
        require(kuhnLength > 0.0) { "kuhnLength must be positive, was: $kuhnLength" }
        require(contourPerNucleotide > 0.0) {
            "contourPerNucleotide must be positive, was: $contourPerNucleotide"
        }
    }
}

/** The four `(Kuhn, contour)` pairs `CLAUDE.md` licenses, never mixed across conventions. */
val SSDNA_CONVENTIONS: List<SsDnaConvention> = listOf(
    SsDnaConvention(
        "zero-force scattering, 2 mM MgCl2",
        SsDnaTether.KUHN_LENGTH_ZERO_FORCE_TWO_MILLIMOLAR,
        SsDnaTether.CONTOUR_PER_NUCLEOTIDE_MAX
    ),
    SsDnaConvention(
        "zero-force scattering, 10 mM MgCl2",
        SsDnaTether.KUHN_LENGTH_ZERO_FORCE,
        SsDnaTether.CONTOUR_PER_NUCLEOTIDE
    ),
    SsDnaConvention(
        "force spectroscopy, 2 mM MgCl2",
        SsDnaTether.KUHN_LENGTH_FORCE_SPECTROSCOPY_TWO_MILLIMOLAR,
        SsDnaTether.CONTOUR_PER_NUCLEOTIDE_MIN
    ),
    SsDnaConvention(
        "force spectroscopy, 10 mM MgCl2",
        SsDnaTether.KUHN_LENGTH_FORCE_SPECTROSCOPY,
        SsDnaTether.CONTOUR_PER_NUCLEOTIDE_MIN
    )
)

/** What it costs a circular scaffold's unpaired remainder to hold the two raster termini apart. */
data class RemainderClosure(
    val convention: String,
    val separation: Double,
    val nucleotides: Int,
    val contourLength: Double,
    val kuhnSegments: Double,
    val rootMeanSquareEndToEnd: Double,
    val extensionRatio: Double,
    val minimumNucleotidesToReach: Int,
    val stretchFreeEnergyKbt: Double
)

/**
 * The Gaussian-chain price of closing the circle through [nucleotides] of unpaired scaffold.
 *
 * `ΔF(r) = (3/2)·k_BT·r²/⟨R²⟩` with `⟨R²⟩ = N_K b²` — the free energy of holding an ideal chain's
 * two ends at separation `r`, measured from the free chain. [extensionRatio] is `r/L_c`, which is
 * the Gaussian's own validity statement: the formula is the small-extension limit of the
 * freely-jointed chain and is quoted with the ratio so a reader can see where it sits.
 *
 * [minimumNucleotidesToReach] is the **reach bound** and runs first: a contour shorter than the
 * separation cannot close at any force, whatever the elasticity.
 */
fun remainderClosure(
    separation: Double,
    nucleotides: Int,
    convention: SsDnaConvention
): RemainderClosure {
    require(separation >= 0.0) { "separation must not be negative, was: $separation" }
    require(nucleotides > 0) { "nucleotides must be positive, was: $nucleotides" }
    val contour = nucleotides * convention.contourPerNucleotide
    val kuhnSegments = contour / convention.kuhnLength
    val meanSquare = kuhnSegments * convention.kuhnLength * convention.kuhnLength
    return RemainderClosure(
        convention = convention.name,
        separation = separation,
        nucleotides = nucleotides,
        contourLength = contour,
        kuhnSegments = kuhnSegments,
        rootMeanSquareEndToEnd = sqrt(meanSquare),
        extensionRatio = separation / contour,
        minimumNucleotidesToReach =
            ceil(separation / convention.contourPerNucleotide).toInt(),
        stretchFreeEnergyKbt = 1.5 * separation * separation / meanSquare
    )
}

/**
 * The smallest remainder whose closure costs at most [costKbt] — the inverse of [remainderClosure],
 * so a falsifier can be quoted as a **threshold** rather than as a comparison.
 *
 * `ΔF ≤ c` is `n ≥ 3r²/(2 c b L_nt)`, and the reach bound is applied afterwards because a chain
 * shorter than the separation does not close at any cost.
 */
fun nucleotidesForClosureCost(
    separation: Double,
    costKbt: Double,
    convention: SsDnaConvention
): Int {
    require(separation >= 0.0) { "separation must not be negative, was: $separation" }
    require(costKbt > 0.0) { "costKbt must be positive, was: $costKbt" }
    val fromEnergy = 1.5 * separation * separation /
            (costKbt * convention.kuhnLength * convention.contourPerNucleotide)
    val fromReach = separation / convention.contourPerNucleotide
    return max(1, ceil(max(fromEnergy, fromReach)).toInt())
}
