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

import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import org.openrndr.math.Vector2
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin

/**
 * `T-116`, leaf `A8.2` — the **plan view** of `C-0050`'s surviving `E5a1` hinge-arm array.
 *
 * `C-0050` reports `E5a1` — `C-0039`'s two-spring elastica at **one** crossover per flexure, 45
 * flexures, a **9.131 nm** arm — as one of only three catalogue rows clearing every predicate at
 * §3's acceptable stroke, and the only one computed on the exact element. Its rows carry
 * `packingAssessed = false`, because `C-0041`'s packer is written for a **standoff-and-tie**
 * flexure and does not cover a hinge-line arm.
 *
 * ## What does not transfer from `C-0041`, and why
 *
 * `C-0041`'s obstruction is a pair of lattice facts:
 *
 * - **Fact A** — the attachment grid's across-helix pitch **is** one duplex, so beams in adjacent
 *   rows are tangent at zero tilt and *mutually bury each other's ties* at any other. That relation
 *   is between **vertical members**, and `E5a1` has none: its near end is a crossover in the host
 *   sheet's own plane and its far end is `C-0034`'s two-link `A2` joint.
 * - **Fact B** — the along-helix pitch, 13.33 nm, is under the span plus a duplex, 34.51 nm.
 *   `E5a1`'s arm is 9.131 nm, so the same comparison is **11.821 against 13.333** and runs the
 *   other way.
 *
 * Neither is inherited here. Both are re-evaluated, and the packer is written as a
 * **generalisation** of `C-0041`'s so that its flexure is a *configuration of this code*: setting
 * [PlanElement.anchorFraction] to `0.5` and [PlanElement.verticalMemberFractions] to
 * `[0, ½, 1]` must reproduce `C-0041`'s verdict, its *"0 of 720"* and its fifteen. That is the free
 * limiting case, and it is asserted as a gate-2 test.
 *
 * ## The conventions, restated (`SESSION-PROMPT.md`)
 *
 * - `x` runs **along** the host sheet's helices, `y` **across** them, origin at the tile centre;
 *   `z` is positive **upward**.
 * - **A duplex in plan is a rectangle of width `d = 2.69 nm`** (SAXS), so two parallel duplexes at
 *   exactly `d` are **tangent and admissible** — `C-0041`'s convention verbatim, and the loosest
 *   defensible one.
 * - **A hinge-line arm occupies one rectangle `arm × d`, ROOTED at its hinge**, not centred on it:
 *   the load path enters the host sheet at the crossover, so that is where the grid anchor sits.
 * - **The hinge is one crossover of the host sheet's own lattice.** A crossover serves one
 *   *interface* every 32 bp = 10.88 nm and adjacent interfaces are offset by 16 bp, so a row's
 *   available roots are the union of its two bounding interfaces' columns — both parities for an
 *   interior row, **one** for an edge row.
 * - **At `n = 1` the hinge is a point joint.** `C-0009`'s interhelical dihedral constant and
 *   `C-0029`'s two-bond couple about their own chord are the *same* 13.5294 pN·nm/rad, so a single
 *   crossover restrains the arm's out-of-plane rotation whichever way the arm runs from it.
 *   `E5a1` is the one member of the family for which `C-0040`'s hinge-line **orientation** question
 *   does not arise, and that is why it is the row left open.
 */

// ---------------------------------------------------------------- the generalised plan element

/**
 * A coupling element seen from above: one duplex rectangle of [length] × [width] at [angle] to
 * `x`, positioned by an [anchor] point that sits a fraction [anchorFraction] of the way along it
 * from the near end, and owning a vertical member at each fraction in [verticalMemberFractions].
 *
 * `C-0041`'s standoff flexure is `anchorFraction = 0.5`, `verticalMemberFractions = [0, ½, 1]`
 * (two standoff feet and one midspan tie). `E5a1` is `anchorFraction = 0.0`, **no** vertical
 * members at all.
 */
data class PlanElement(
    val id: String,
    val anchor: Vector2,
    val angle: Double,
    val length: Double,
    val width: Double = OrigamiDuplex.INTERHELICAL,
    val anchorFraction: Double = 0.5,
    val verticalMemberFractions: List<Double> = emptyList()
) {

    init {
        require(length > 0.0) { "length must be positive, was: $length" }
        require(width > 0.0) { "width must be positive, was: $width" }
        require(anchorFraction in 0.0..1.0) {
            "anchorFraction must lie in [0, 1], was: $anchorFraction"
        }
        require(verticalMemberFractions.all { it in 0.0..1.0 }) {
            "every vertical member fraction must lie in [0, 1], were: $verticalMemberFractions"
        }
    }

    /** The unit vector along the element's own axis, from its near end to its far end. */
    val axis: Vector2 get() = Vector2(cos(angle), sin(angle))

    /** The near end — for a hinge arm, the **root**, where the crossover is. */
    val nearEnd: Vector2 get() = anchor - axis * (anchorFraction * length)

    /** The far end — for a hinge arm, the **tip**, where `C-0034`'s `A2` anchorage is. */
    val farEnd: Vector2 get() = nearEnd + axis * length

    /** The rectangle's own centre. */
    val centre: Vector2 get() = nearEnd + axis * (length / 2.0)

    /** The element's plan footprint. */
    val body: PlanRectangle get() = PlanRectangle(centre, angle, length, width)

    /** The vertical members this element needs a clear column for — possibly none. */
    val verticalMembers: List<Vector2>
        get() = verticalMemberFractions.map { nearEnd + axis * (it * length) }

    /** The radius in nm of each vertical member — a duplex standing normal to the sheet. */
    val memberRadius: Double get() = width / 2.0

    /** This element turned by [turn] radians about [pivot]. */
    fun rotatedAbout(pivot: Vector2, turn: Double): PlanElement = copy(
        anchor = pivot + (anchor - pivot).turnedBy(turn),
        angle = angle + turn
    )
}

private fun Vector2.turnedBy(turn: Double): Vector2 =
    Vector2(x * cos(turn) - y * sin(turn), x * sin(turn) + y * cos(turn))

/**
 * Whether [lower]'s body covers any of [upper]'s vertical members, so that
 * `level(upper) > level(lower)` is required rather than optional.
 *
 * `C-0041`'s [blocksVerticalMembers] on the generalised element. An element with **no** vertical
 * members can never be blocked, which is exactly the premise `E5a1` changes.
 */
fun elementBlocksVerticalMembers(
    lower: PlanElement,
    upper: PlanElement,
    tolerance: Double = PLAN_TANGENCY_TOLERANCE
): Boolean {
    val body = lower.body
    return upper.verticalMembers.any { body.distanceTo(it) < upper.memberRadius - tolerance }
}

/**
 * Whether any vertical member of [first] comes closer than one duplex pitch to any of [second]'s —
 * `C-0041`'s level-independent, and therefore fatal, clash.
 */
fun elementMembersClash(
    first: PlanElement,
    second: PlanElement,
    tolerance: Double = PLAN_TANGENCY_TOLERANCE
): Boolean {
    val minimum = first.memberRadius + second.memberRadius
    return first.verticalMembers.any { a ->
        second.verticalMembers.any { b -> (a - b).length < minimum - tolerance }
    }
}

/**
 * Solves the level assignment for [elements] — `C-0041`'s [packingVerdict] on the generalised
 * element, with the same two relations and the same cycle rule, so that its results are reproduced
 * rather than re-derived.
 */
fun elementPackingVerdict(elements: List<PlanElement>): PackingVerdict {
    require(elements.isNotEmpty()) { "elements must not be empty" }
    val n = elements.size
    val overlaps = Array(n) { BooleanArray(n) }
    val blocks = Array(n) { BooleanArray(n) }
    var overlapping = 0
    var blocking = 0
    var mutual = 0
    var clashes = 0
    for (i in 0 until n) {
        for (j in 0 until n) {
            if (i == j) continue
            if (elementBlocksVerticalMembers(elements[i], elements[j])) {
                blocks[i][j] = true
                blocking++
            }
        }
    }
    for (i in 0 until n) {
        for (j in i + 1 until n) {
            if (rectanglesOverlap(elements[i].body, elements[j].body)) {
                overlaps[i][j] = true
                overlaps[j][i] = true
                overlapping++
            }
            if (blocks[i][j] && blocks[j][i]) mutual++
            if (elementMembersClash(elements[i], elements[j])) clashes++
        }
    }
    val order = elementTopologicalOrder(n) { from, to -> blocks[to][from] }
    if (order == null || clashes > 0) {
        return PackingVerdict(
            n, overlapping, blocking, mutual, clashes, false, UNREALISABLE_LEVEL_COUNT, emptyList()
        )
    }
    val level = IntArray(n) { 0 }
    order.forEach { index ->
        var candidate = 1
        (0 until n).forEach { other ->
            if (level[other] > 0 && blocks[index][other]) {
                candidate = max(candidate, level[other] + 1)
            }
        }
        while ((0 until n).any { level[it] == candidate && overlaps[it][index] }) candidate++
        level[index] = candidate
    }
    return PackingVerdict(
        n, overlapping, blocking, mutual, clashes, true, level.max(), level.toList()
    )
}

private fun elementTopologicalOrder(count: Int, precedes: (Int, Int) -> Boolean): List<Int>? {
    val indegree = IntArray(count)
    for (from in 0 until count) {
        for (to in 0 until count) {
            if (from != to && precedes(from, to)) indegree[to]++
        }
    }
    val order = ArrayList<Int>(count)
    val ready = ArrayDeque((0 until count).filter { indegree[it] == 0 })
    while (ready.isNotEmpty()) {
        val next = ready.removeFirst()
        order += next
        for (to in 0 until count) {
            if (to != next && precedes(next, to)) {
                indegree[to]--
                if (indegree[to] == 0) ready.addLast(to)
            }
        }
    }
    return if (order.size == count) order else null
}

// ---------------------------------------------------------------- arrays on the attachment grid

/** The plan area in nm² that [count] arms of [arm] and [width] occupy, laid out or not. */
fun armArrayPlanArea(
    count: Int,
    arm: Double,
    width: Double = OrigamiDuplex.INTERHELICAL
): Double {
    require(count > 0) { "count must be positive, was: $count" }
    require(arm > 0.0) { "arm must be positive, was: $arm" }
    require(width > 0.0) { "width must be positive, was: $width" }
    return count * arm * width
}

/**
 * The array whose anchors are `C-0015`'s [columns] × [rows] attachment grid on an
 * [edgeX] × [edgeY] tile, every element of [length] laid at [angle] to `x`.
 */
fun elementArray(
    columns: Int,
    rows: Int,
    edgeX: Double,
    edgeY: Double,
    length: Double,
    angle: Double,
    width: Double = OrigamiDuplex.INTERHELICAL,
    anchorFraction: Double = 0.5,
    verticalMemberFractions: List<Double> = emptyList()
): List<PlanElement> =
    com.xemantic.nano.plentyofroom.coupling.attachmentGrid(columns, rows, edgeX, edgeY)
        .mapIndexed { index, (x, y) ->
            PlanElement(
                "E$index", Vector2(x, y), angle, length, width, anchorFraction,
                verticalMemberFractions
            )
        }

/** The `E5a1` array: rooted at its hinge, owning **no** vertical member. */
fun hingeArmArray(
    columns: Int,
    rows: Int,
    edgeX: Double,
    edgeY: Double,
    arm: Double,
    angle: Double,
    width: Double = OrigamiDuplex.INTERHELICAL
): List<PlanElement> = elementArray(
    columns, rows, edgeX, edgeY, arm, angle, width,
    anchorFraction = 0.0, verticalMemberFractions = emptyList()
)

/**
 * Sweeps the element orientation over `[0, angularSpan)` at [samples] steps on fixed [anchors].
 *
 * `C-0041` swept `[0, π)` because its beam is **centred** and therefore symmetric under a half
 * turn. A **rooted** arm is not: `θ` and `θ + π` are different designs, so the arm sweep runs over
 * the full circle and says so.
 */
fun elementOrientationSweep(
    anchors: List<Vector2>,
    length: Double,
    samples: Int = 720,
    width: Double = OrigamiDuplex.INTERHELICAL,
    anchorFraction: Double = 0.0,
    verticalMemberFractions: List<Double> = emptyList(),
    angularSpan: Double = PI
): OrientationSweep {
    require(anchors.isNotEmpty()) { "anchors must not be empty" }
    require(samples > 0) { "samples must be positive, was: $samples" }
    require(angularSpan > 0.0) { "angularSpan must be positive, was: $angularSpan" }
    var feasible = 0
    var single = 0
    var minimumOverlaps = Int.MAX_VALUE
    var minimumMutual = Int.MAX_VALUE
    var minimumClashes = Int.MAX_VALUE
    var bestAngle = 0.0
    (0 until samples).forEach { step ->
        val angle = step * angularSpan / samples
        val verdict = elementPackingVerdict(
            anchors.mapIndexed { index, point ->
                PlanElement(
                    "E$index", point, angle, length, width, anchorFraction,
                    verticalMemberFractions
                )
            }
        )
        if (verdict.feasibleAtAnyLevelCount) feasible++
        if (verdict.singleLevel) single++
        val obstruction = verdict.mutuallyBlockingPairs + verdict.memberClashPairs
        val best = minimumMutual + minimumClashes
        val better = minimumMutual == Int.MAX_VALUE || obstruction < best ||
                (obstruction == best && verdict.overlappingPairs < minimumOverlaps)
        if (better) bestAngle = angle
        minimumOverlaps = min(minimumOverlaps, verdict.overlappingPairs)
        minimumMutual = min(minimumMutual, verdict.mutuallyBlockingPairs)
        minimumClashes = min(minimumClashes, verdict.memberClashPairs)
    }
    return OrientationSweep(
        samples, feasible, single, minimumOverlaps, minimumMutual, minimumClashes,
        bestAngle * 180.0 / PI
    )
}

/**
 * The largest path count that packs in **one** level on an [edgeX] × ([rows] · `d`) tile, solved at
 * every column count rather than by a formula, with the element's length supplied by [lengthFor] so
 * that a self-consistently placed span (`L ∝ n^(1/3)`) can be swept as well as a fixed arm.
 *
 * Every body must lie inside the host sheet's own edge in `x` — a hinge arm is made of the host's
 * duplexes, so an arm hanging off the edge is not a placement.
 */
fun packingLimitedElementCount(
    edgeX: Double,
    rows: Int,
    lengthFor: (Int) -> Double,
    width: Double = OrigamiDuplex.INTERHELICAL,
    anchorFraction: Double = 0.5,
    verticalMemberFractions: List<Double> = emptyList(),
    angle: Double = 0.0
): Int {
    require(rows > 0) { "rows must be positive, was: $rows" }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    var best = 0
    (1..rows).forEach { columns ->
        val count = columns * rows
        val length = lengthFor(count)
        val array = elementArray(
            columns, rows, edgeX, rows * width, length, angle, width, anchorFraction,
            verticalMemberFractions
        )
        val contained = array.all { element ->
            element.body.corners.all { abs(it.x) <= edgeX / 2.0 + PLAN_TANGENCY_TOLERANCE }
        }
        if (contained && elementPackingVerdict(array).singleLevel) best = max(best, count)
    }
    return best
}

// ---------------------------------------------------------------- the hinge lattice

/**
 * One crossover of the host sheet's lattice, available as an arm's hinge.
 *
 * @param interfaceIndex the interface `b ∈ [0, D−2]` between duplex `b` and duplex `b+1`.
 * @param x the crossover's position along the helices, in nm from the tile centre.
 * @param parity `b mod 2` — interface `b` carries the columns of its own parity, because
 *          crossovers alternate between a helix's two neighbours (`C-0015`, `C-0040`).
 */
data class HingeSite(val interfaceIndex: Int, val x: Double, val parity: Int)

/**
 * Every crossover of a [duplexes]-duplex sheet of edge [edgeX] at a column-lattice phase of
 * [phaseBasePairs] base pairs, as hinge sites.
 *
 * Reproduces `C-0015`'s inventory — **56** at the ten eight-column phases and **49** at the other
 * twenty-two — and `C-0040`'s **four** per interface, from the same [CrossoverLayout] both of them
 * use, rather than from a count restated here.
 */
fun hingeSites(
    phaseBasePairs: Int,
    edgeX: Double,
    duplexes: Int,
    crossoverSpacingBasePairs: Double = Gen1Tile.CROSSOVER_SPACING_SHEET_BP,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR
): List<HingeSite> {
    require(phaseBasePairs >= 0) { "phaseBasePairs must not be negative, was: $phaseBasePairs" }
    require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    val layout = CrossoverLayout.phased(
        phase = phaseBasePairs * risePerBasePair,
        columnSpacing = crossoverSpacingBasePairs * risePerBasePair / 2.0,
        lengthX = edgeX
    )
    return (0 until duplexes - 1).flatMap { interfaceIndex ->
        val parity = interfaceIndex % 2
        layout.positions.indices
            .filter { layout.parities[it] == parity }
            .map { HingeSite(interfaceIndex, layout.positions[it], parity) }
    }
}

/**
 * The sites a given [row] may root an arm on — the crossovers of its two bounding interfaces.
 *
 * An **interior** row sees both parities, so its roots sit at a 16 bp = 5.44 nm pitch; an **edge**
 * row sees one interface only, so its roots sit at 32 bp = 10.88 nm. That asymmetry is a lattice
 * fact and is not imposed anywhere.
 */
fun rowHingeSites(row: Int, sites: List<HingeSite>): List<HingeSite> {
    require(row >= 0) { "row must not be negative, was: $row" }
    return sites.filter { it.interfaceIndex == row || it.interfaceIndex == row - 1 }
        .sortedWith(compareBy({ it.x }, { it.interfaceIndex }))
}

/** One arm placed on the lattice: rooted at [rootX] in [row], on the crossover of [interfaceIndex]. */
data class ArmPlacement(
    val row: Int,
    val rootX: Double,
    val towardPositiveX: Boolean,
    val interfaceIndex: Int,
    val arm: Double
) {

    /** The low end of the arm's footprint along `x`. */
    val low: Double get() = if (towardPositiveX) rootX else rootX - arm

    /** The high end of the arm's footprint along `x`. */
    val high: Double get() = if (towardPositiveX) rootX + arm else rootX
}

/**
 * The **maximum** number of arms of [arm] one row can carry, given its own [sites], solved exactly
 * by interval scheduling: an arm's effective footprint is `[low, high + d)`, two arms clear each
 * other exactly when their effective footprints are disjoint, and earliest-effective-end is optimal
 * for a maximum-cardinality disjoint set.
 *
 * Both root directions are candidates, and an arm must lie inside the host's own edge.
 */
fun maximumArmsInRow(
    sites: List<HingeSite>,
    arm: Double,
    edgeX: Double,
    width: Double = OrigamiDuplex.INTERHELICAL,
    row: Int = 0,
    forbidden: Set<Pair<Int, Long>> = emptySet()
): List<ArmPlacement> {
    require(arm > 0.0) { "arm must be positive, was: $arm" }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    val half = edgeX / 2.0
    val candidates = sites.flatMap { site ->
        listOf(true, false).map { toward -> ArmPlacement(row, site.x, toward, site.interfaceIndex, arm) }
    }.filter { placement ->
        placement.low >= -half - PLAN_TANGENCY_TOLERANCE &&
                placement.high <= half + PLAN_TANGENCY_TOLERANCE &&
                (placement.interfaceIndex to crossoverKey(placement.rootX)) !in forbidden
    }.sortedWith(compareBy({ it.high }, { it.low }, { it.interfaceIndex }))
    val chosen = ArrayList<ArmPlacement>()
    var frontier = Double.NEGATIVE_INFINITY
    candidates.forEach { placement ->
        if (placement.low >= frontier - PLAN_TANGENCY_TOLERANCE) {
            chosen += placement
            frontier = placement.high + width
        }
    }
    return chosen
}

/** The key a crossover is identified by, rounded so that a lattice position is exact. */
internal fun crossoverKey(x: Double): Long = Math.round(x * 1.0e6)

/** A whole array of arms placed on the host sheet's own crossover lattice. */
data class HingeArmPlacement(
    val phaseBasePairs: Int,
    val placements: List<ArmPlacement>,
    val independentRowBound: Int
) {

    /** The number of arms actually placed. */
    val arms: Int get() = placements.size

    /**
     * The same placement thinned to [target] arms, taken **round robin across the rows** so that a
     * design asking for fewer paths than the lattice can carry is spread over the host rather than
     * concentrated in its first rows.
     *
     * A count is a design variable (`C-0041`: the span is re-placed at every candidate count), so
     * what a smaller array costs the host has to be read on a smaller array, not on the maximal one.
     */
    fun truncatedTo(target: Int): HingeArmPlacement {
        require(target >= 0) { "target must not be negative, was: $target" }
        if (target >= arms) return this
        val byRow = placements.groupBy { it.row }.toSortedMap()
        val queues = byRow.values.map { it.sortedBy { placement -> placement.low }.toMutableList() }
        val taken = ArrayList<ArmPlacement>(target)
        var index = 0
        while (taken.size < target) {
            val queue = queues[index % queues.size]
            if (queue.isNotEmpty()) taken += queue.removeAt(0)
            index++
        }
        return copy(placements = taken.sortedWith(compareBy({ it.row }, { it.low })))
    }
}

/**
 * Places as many arms of [arm] as the lattice of a [duplexes]-duplex sheet of edge [edgeX] admits
 * at a column phase of [phaseBasePairs], **one distinct crossover each**.
 *
 * Rows are solved in order, each by the exact per-row interval schedule, with the crossovers the
 * previous row has already consumed removed from its candidate set — a crossover on interface `b`
 * joins duplexes `b` and `b+1`, so it can be the hinge of an arm in **one** of them, not both. The
 * per-row maxima solved **independently** are carried alongside as a strict **upper bound**, so
 * that a negative verdict is a proof and a positive one is a construction.
 */
fun placeHingeArms(
    phaseBasePairs: Int,
    edgeX: Double,
    duplexes: Int,
    arm: Double,
    width: Double = OrigamiDuplex.INTERHELICAL
): HingeArmPlacement {
    require(arm > 0.0) { "arm must be positive, was: $arm" }
    val sites = hingeSites(phaseBasePairs, edgeX, duplexes)
    val bound = (0 until duplexes).sumOf { row ->
        maximumArmsInRow(rowHingeSites(row, sites), arm, edgeX, width, row).size
    }
    var best: List<ArmPlacement> = emptyList()
    listOf(0 until duplexes, (duplexes - 1) downTo 0).forEach { order ->
        val used = HashSet<Pair<Int, Long>>()
        val placed = ArrayList<ArmPlacement>()
        order.forEach { row ->
            val inRow = maximumArmsInRow(rowHingeSites(row, sites), arm, edgeX, width, row, used)
            inRow.forEach { used += it.interfaceIndex to crossoverKey(it.rootX) }
            placed += inRow
        }
        if (placed.size > best.size) best = placed
    }
    return HingeArmPlacement(
        phaseBasePairs, best.sortedWith(compareBy({ it.row }, { it.low })), bound
    )
}

// ---------------------------------------------------------------- what the host sheet is left with

/** What an arm array costs the sheet that hosts its hinges. */
data class HostSheetVerdict(
    val duplexes: Int,
    val arms: Int,
    val armLengthFraction: Double,
    val hingeCrossovers: Int,
    val buriedCrossovers: Int,
    val crossoversDemanded: Int,
    val inventory: Int,
    val survivingCrossovers: Int,
    val segments: Int,
    val trimmedSegments: Int,
    val orphanSegments: Int,
    val components: Int,
    val largestComponentSegments: Int
) {

    /**
     * True when the arms leave the host in more than one piece.
     *
     * An **orphan** — a residual stub carrying no surviving crossover, of which the slivers left
     * beside an arm rooted near the edge are the common case — is a piece like any other, so it is
     * counted here even though it is excluded from [components], which is a statement about the
     * part of the host that is still bonded to something.
     */
    val severed: Boolean get() = components + orphanSegments > 1

    /** True when the arms need more crossovers than the host has. */
    val overSubscribed: Boolean get() = crossoversDemanded > inventory
}

/**
 * What [placement] does to the sheet that hosts it: how much of its own duplex length becomes arm,
 * how many crossovers the arms consume as hinges, how many more must be **deleted** because they
 * lie under an arm and would tie it back to the sheet, and how many connected components the
 * residual sheet falls into.
 *
 * An arm is a length of the host's own duplex cut free at both ends, so the host's row is severed
 * at the arm's two ends and the arm itself leaves the sheet's load path. A crossover strictly
 * inside an arm's footprint on either of that arm's two interfaces is **not available** — it would
 * make the arm a second hinge line rather than a free lever — so it is charged as buried.
 */
fun hostSheetAfterArms(
    placement: HingeArmPlacement,
    edgeX: Double,
    duplexes: Int,
    arm: Double,
    width: Double = OrigamiDuplex.INTERHELICAL,
    minimumSegment: Double = OrigamiDuplex.DIAMETER
): HostSheetVerdict {
    require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
    require(arm > 0.0) { "arm must be positive, was: $arm" }
    val sites = hingeSites(placement.phaseBasePairs, edgeX, duplexes)
    val hinges = placement.placements
        .map { it.interfaceIndex to crossoverKey(it.rootX) }
        .toHashSet()
    val buried = HashSet<Pair<Int, Long>>()
    placement.placements.forEach { armPlacement ->
        sites.filter {
            (it.interfaceIndex == armPlacement.row || it.interfaceIndex == armPlacement.row - 1) &&
                    it.x > armPlacement.low - PLAN_TANGENCY_TOLERANCE &&
                    it.x < armPlacement.high + PLAN_TANGENCY_TOLERANCE
        }.forEach { site ->
            val key = site.interfaceIndex to crossoverKey(site.x)
            if (key !in hinges) buried += key
        }
    }
    // every duplex becomes an ordered list of intact segments, the arms cut out of it.
    // A residual piece shorter than one duplex diameter is TRIM, not a component: it is a stub
    // left where an arm roots on the lattice site nearest the edge, and a design simply does not
    // build it. Counting such a sliver as a disconnected piece would report a placement artefact
    // as a structural failure.
    val half = edgeX / 2.0
    var trimmed = 0
    val segments = (0 until duplexes).map { row ->
        val cuts = placement.placements.filter { it.row == row }
            .map { it.low to it.high }
            .sortedBy { it.first }
        val pieces = ArrayList<Pair<Double, Double>>()
        var start = -half
        cuts.forEach { (low, high) ->
            if (low - start > PLAN_TANGENCY_TOLERANCE) pieces += start to min(low, half)
            start = max(start, high)
        }
        if (half - start > PLAN_TANGENCY_TOLERANCE) pieces += start to half
        val kept = pieces.filter { it.second - it.first > minimumSegment }
        trimmed += pieces.size - kept.size
        kept
    }
    val offsets = IntArray(duplexes)
    var total = 0
    (0 until duplexes).forEach { row ->
        offsets[row] = total
        total += segments[row].size
    }
    val parent = IntArray(total) { it }
    fun find(node: Int): Int {
        var root = node
        while (parent[root] != root) root = parent[root]
        var walk = node
        while (parent[walk] != walk) {
            val next = parent[walk]
            parent[walk] = root
            walk = next
        }
        return root
    }
    fun union(a: Int, b: Int) {
        val rootA = find(a)
        val rootB = find(b)
        if (rootA != rootB) parent[rootB] = rootA
    }
    fun segmentAt(row: Int, position: Double): Int? =
        segments[row].indexOfFirst {
            position >= it.first - PLAN_TANGENCY_TOLERANCE &&
                    position <= it.second + PLAN_TANGENCY_TOLERANCE
        }.takeIf { it >= 0 }?.let { offsets[row] + it }
    var surviving = 0
    val bonded = HashSet<Int>()
    sites.forEach { site ->
        val key = site.interfaceIndex to crossoverKey(site.x)
        if (key in hinges || key in buried) return@forEach
        val low = segmentAt(site.interfaceIndex, site.x)
        val high = segmentAt(site.interfaceIndex + 1, site.x)
        if (low != null && high != null) {
            surviving++
            bonded += low
            bonded += high
            union(low, high)
        }
    }
    val roots = bonded.map { find(it) }
    val components = roots.distinct().size
    val largest = roots.groupingBy { it }.eachCount().values.maxOrNull() ?: 0
    return HostSheetVerdict(
        duplexes = duplexes,
        arms = placement.arms,
        armLengthFraction = placement.arms * arm / (duplexes * edgeX),
        hingeCrossovers = hinges.size,
        buriedCrossovers = buried.size,
        crossoversDemanded = hinges.size + buried.size,
        inventory = sites.size,
        survivingCrossovers = surviving,
        segments = total,
        trimmedSegments = trimmed,
        orphanSegments = total - bonded.size,
        components = components,
        largestComponentSegments = largest
    )
}
