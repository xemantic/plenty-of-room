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
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * `T-126` — whether `C-0055`'s upward arm slab and `C-0035`'s tie-down path can share the tile's
 * `+z` face.
 *
 * ## What the question is, and what it is not
 *
 * `C-0035` found exactly one buildable flexure mounting — standoff bases on the **output
 * superstructure**, standoffs pointing **away** from the tile, the flexure **outboard**, each
 * midspan tied back **down** through that ground to the tile — and recorded that *"the tile now
 * carries no out-of-plane element at all"*. `C-0055` then bought the programme's escape by
 * rooting the flexure hinges on the **unused** `EAST` azimuth, which puts 34 duplexes in a slab
 * above the sheet over 46.3 % of the plan, on exactly the face those ties cross.
 *
 * `C-0041` established that an **area** bound is what invites *"stack it in three levels"*, and
 * that stacking buys nothing when two vertical members share a height range. This file therefore
 * answers in a **plan and a section**, and the section is what makes the plan decisive:
 * [tieMayPassOverSlab] is `false` for every mounting `C-0035` admits, so a plan overlap cannot be
 * relieved at any level count, on a body of any size.
 *
 * ## Conventions, restated rather than inherited
 *
 * Lengths **nm**, areas **nm²**. `x` runs **along** the host sheet's helices, `y` **across** them,
 * origin at the tile centre; `z` is normal and positive **upward**, with `z = 0` on the sheet's
 * own **mid-plane** and the grafted layer below. A duplex in plan is a rectangle of width
 * `d = 2.69 nm` (`C-0041`, `C-0053`); in section it is a cylinder of radius **1.0 nm**, the B-DNA
 * phosphate radius, which **is** the steric surface (`CLAUDE.md`).
 *
 * An **arm** is `C-0055`'s: a duplex lying parallel to its host one interhelical distance above
 * it, rooted at its hinge and not centred on it, so `+x` and `−x` are different designs
 * (`C-0053`). A **tie-down** is `C-0035`'s: a duplex standing **normal** to the sheet, descending
 * from a flexure midspan through the superstructure onto the tile. `C-0029` makes its landing a
 * two-link joint at a duplex end — quantised at the 0.34 nm rise along `x` and **not** on any
 * crossover lattice — which is why the tie's `x` is a design variable and the arm's root is not.
 *
 * **The arm's rotation axis is taken ACROSS its own length at the root**, so a stroke lifts the
 * tip out of the sheet plane. That is `C-0055`'s deliberately unadjudicated open item 2, stated
 * here as a convention: under the other reading the arm spins about its own axis and delivers no
 * stroke at all.
 */

// ------------------------------------------------------------------------------- the section

/**
 * A band on the `z` axis in nm, measured from the sheet's own mid-plane.
 *
 * The section exists in this file for one purpose: to decide whether a plan overlap between an
 * arm and a tie is **level-independent**. `C-0041` needed the same distinction and reached it
 * through [verticalMembersClash]; here it is a containment of two bands.
 */
data class SectionBand(val low: Double, val high: Double) {

    init {
        require(high > low) { "a band must have a positive thickness, was: [$low, $high]" }
    }

    /** The thickness in nm. */
    val thickness: Double get() = high - low

    /** Whether this band and [other] share more than [tolerance] of height. */
    fun overlaps(other: SectionBand, tolerance: Double = PLAN_TANGENCY_TOLERANCE): Boolean =
        min(high, other.high) - max(low, other.low) > tolerance

    /** Whether [other] lies wholly inside this band. */
    fun contains(other: SectionBand, tolerance: Double = PLAN_TANGENCY_TOLERANCE): Boolean =
        other.low >= low - tolerance && other.high <= high + tolerance
}

/** The band the host sheet itself occupies — one duplex radius either side of its mid-plane. */
fun sheetBand(radius: Double = OrigamiDuplex.DIAMETER / 2.0): SectionBand {
    require(radius > 0.0) { "radius must be positive, was: $radius" }
    return SectionBand(-radius, radius)
}

/**
 * The band `C-0055`'s arm slab occupies at [stroke].
 *
 * At rest the arm's axis is one interhelical distance above the sheet's, so the slab is
 * `[d − r, d + r]` — **1.69 to 3.69 nm**, which is `C-0061`'s own figure re-derived. Under a
 * stroke the tip rises **relative to the tile** (the tile descends and the driven body does not),
 * so the slab's ceiling rises by the stroke and its floor does not move: the root end stays put.
 */
fun armSlabBand(
    stroke: Double,
    interhelicalDistance: Double = Gen1Tile.INTERHELICAL_SHEET,
    radius: Double = OrigamiDuplex.DIAMETER / 2.0
): SectionBand {
    require(stroke >= 0.0) { "stroke must not be negative, was: $stroke" }
    require(interhelicalDistance > 0.0) {
        "interhelicalDistance must be positive, was: $interhelicalDistance"
    }
    require(radius > 0.0) { "radius must be positive, was: $radius" }
    return SectionBand(interhelicalDistance - radius, interhelicalDistance + radius + stroke)
}

/**
 * The clear column a tie-down needs: from the tile's own top face up to the plane its flexure's
 * standoffs stand on, at [standoffBasePlane].
 *
 * The tie has to reach the **tile**, so its column starts at the tile's top face whatever else is
 * in the way. That single sentence is what makes the whole question a plan one.
 */
fun tieClearColumn(
    standoffBasePlane: Double,
    radius: Double = OrigamiDuplex.DIAMETER / 2.0
): SectionBand {
    require(radius > 0.0) { "radius must be positive, was: $radius" }
    require(standoffBasePlane > radius) {
        "the standoff base plane must lie above the tile's top face, was: $standoffBasePlane"
    }
    return SectionBand(radius, standoffBasePlane)
}

/**
 * Whether a tie could pass **over** the arm slab — `false` whenever the two share height, which
 * for `C-0035`'s `Su` mounting is always, because its base plane is above the arms by
 * construction and its tie descends past them to the tile.
 *
 * **This is the theorem that makes the plan view decisive**, and it is `C-0041`'s Fact A in a new
 * place: a clash that cannot be relieved by a level is a clash on a body of any size.
 */
fun tieMayPassOverSlab(slab: SectionBand, column: SectionBand): Boolean = !slab.overlaps(column)

// ---------------------------------------------------------------------------------- the plan

/** A closed interval along `x` in nm. */
data class PlanInterval(val low: Double, val high: Double) {

    init {
        require(high >= low) { "an interval must not run backwards, was: [$low, $high]" }
    }

    /** The length in nm. */
    val length: Double get() = high - low

    /** Whether this interval and [other] share more than [tolerance] of length. */
    fun overlaps(other: PlanInterval, tolerance: Double = PLAN_TANGENCY_TOLERANCE): Boolean =
        min(high, other.high) - max(low, other.low) > tolerance
}

/** One arm of the slab in plan: which row it is on, where it roots, and which way it runs. */
data class SlabArm(val row: Int, val rootX: Double, val towardPositiveX: Boolean)

/**
 * The plan reach in nm of an arm of [arm] whose tip has risen by [stroke] — `√(L² − s²)`.
 *
 * The arm rotates about its root, so its projection on the sheet is a **cosine** and **shortens**
 * with the stroke. That is why [sweptArmInterval] is the rest footprint identically, and it is
 * the one thing in this geometry that runs the favourable way.
 */
fun armPlanReach(arm: Double, stroke: Double): Double {
    require(arm > 0.0) { "arm must be positive, was: $arm" }
    require(stroke >= 0.0) { "stroke must not be negative, was: $stroke" }
    require(stroke <= arm) {
        "an arm of $arm nm cannot deliver a stroke of $stroke nm: a lever is a rotation, not a " +
                "translation, and this is C-0050's kinematic ceiling in a plan view"
    }
    return sqrt(arm * arm - stroke * stroke)
}

/** Whether an arm of [arm] can reach [stroke] at all. */
fun armDeliversStroke(arm: Double, stroke: Double): Boolean = stroke <= arm

/** The interval along `x` an arm covers at [stroke]. */
fun armInterval(slabArm: SlabArm, arm: Double, stroke: Double = 0.0): PlanInterval {
    val reach = armPlanReach(arm, stroke)
    return if (slabArm.towardPositiveX) PlanInterval(slabArm.rootX, slabArm.rootX + reach)
    else PlanInterval(slabArm.rootX - reach, slabArm.rootX)
}

/**
 * The **swept** envelope of an arm over the whole stroke `[0, stroke]`, sampled at [samples]
 * positions and unioned rather than asserted.
 *
 * It equals the rest footprint, because the reach is monotone decreasing in the stroke — but it
 * is computed as a union so that the statement is a result and not a definition.
 */
fun sweptArmInterval(
    slabArm: SlabArm,
    arm: Double,
    stroke: Double,
    samples: Int = 64
): PlanInterval {
    require(samples >= 2) { "samples must be at least 2, was: $samples" }
    var low = Double.POSITIVE_INFINITY
    var high = Double.NEGATIVE_INFINITY
    (0 until samples).forEach { step ->
        val interval = armInterval(slabArm, arm, stroke * step / (samples - 1))
        low = min(low, interval.low)
        high = max(high, interval.high)
    }
    return PlanInterval(low, high)
}

/** `C-0015`'s [columns] × 15 attachment grid, as the `x` of its columns on an [edgeX] tile. */
fun gridColumns(columns: Int, edgeX: Double): List<Double> {
    require(columns > 0) { "columns must be positive, was: $columns" }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    return (0 until columns).map { edgeX * (it + 0.5) / columns - edgeX / 2.0 }
}

/** The parts of a row of edge [edgeX] that [occupied] leaves free, ascending. */
fun freeIntervals(occupied: List<PlanInterval>, edgeX: Double): List<PlanInterval> {
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    val half = edgeX / 2.0
    val free = ArrayList<PlanInterval>()
    var start = -half
    occupied.sortedBy { it.low }.forEach { interval ->
        if (interval.low - start > PLAN_TANGENCY_TOLERANCE) {
            free += PlanInterval(start, min(interval.low, half))
        }
        start = max(start, interval.high)
    }
    if (half - start > PLAN_TANGENCY_TOLERANCE) free += PlanInterval(start, half)
    return free
}

/** How many ties of [width] fit side by side in [intervals]. */
fun tiesFitting(intervals: List<PlanInterval>, width: Double): Int {
    require(width > 0.0) { "width must be positive, was: $width" }
    return intervals.sumOf { floor(it.length / width + PLAN_TANGENCY_TOLERANCE).toInt() }
}

/** How many of [columns] land on [arms], a tie being [width] wide. */
fun columnClashes(arms: List<PlanInterval>, columns: List<Double>, width: Double): Int {
    require(width > 0.0) { "width must be positive, was: $width" }
    return columns.count { column ->
        val tie = PlanInterval(column - width / 2.0, column + width / 2.0)
        arms.any { it.overlaps(tie) }
    }
}

/**
 * Every direction assignment under which a row's [roots] clear one another and the tile edge —
 * **exhaustively**, `+x` first, so that the first entry is the one `C-0063`'s own [armDirections]
 * returns and the enumeration is a superset of it rather than a second reading of it.
 *
 * The rows are independent (`C-0055`'s gate 3), which is what makes an exhaustive treatment of
 * this free variable cost `2^3` and not `2^34`.
 */
fun feasibleRowDirections(
    roots: List<Double>,
    arm: Double,
    edgeX: Double,
    width: Double = OrigamiDuplex.INTERHELICAL
): List<List<Boolean>> {
    require(arm > 0.0) { "arm must be positive, was: $arm" }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(width > 0.0) { "width must be positive, was: $width" }
    if (roots.isEmpty()) return listOf(emptyList())
    require(roots.sorted() == roots) { "roots must ascend, were: $roots" }
    val half = edgeX / 2.0
    val found = ArrayList<List<Boolean>>()
    fun build(index: Int, taken: List<Boolean>) {
        if (index == roots.size) {
            val intervals = roots.zip(taken).map { (root, toward) ->
                if (toward) PlanInterval(root, root + arm) else PlanInterval(root - arm, root)
            }
            val sorted = intervals.sortedBy { it.low }
            if (sorted.zipWithNext().any { (a, b) -> b.low < a.high - PLAN_TANGENCY_TOLERANCE }) {
                return
            }
            found += taken
            return
        }
        listOf(true, false).forEach { toward ->
            val low = if (toward) roots[index] else roots[index] - arm
            val high = if (toward) roots[index] + arm else roots[index]
            if (low >= -half - PLAN_TANGENCY_TOLERANCE && high <= half + PLAN_TANGENCY_TOLERANCE) {
                build(index + 1, taken + toward)
            }
        }
    }
    build(0, emptyList())
    return found
}

/** What one row's arms leave for one row's ties. */
data class RowInterleave(
    val row: Int,
    val arms: Int,
    val towardPositiveX: List<Boolean>,
    val armIntervals: List<PlanInterval>,
    val freeIntervals: List<PlanInterval>,
    val maximumTies: Int,
    val gridClashes: Int
) {

    /** The length in nm of the row a tie may stand in. */
    val freeLength: Double get() = freeIntervals.sumOf { it.length }
}

/**
 * The interleave of one row: its arms as placed, the room they leave, and how many of [columns]
 * they refuse.
 *
 * With [optimiseDirections] the arm senses are chosen to minimise the clash count and then to
 * maximise the free tie capacity — the free variable `C-0063` left open in its own item 4, which
 * does not enter the flatness (the coupling enters at the root) but decides this question.
 * Without it the row carries `C-0063`'s own greedy assignment, which is the array as published.
 */
fun interleaveRow(
    row: Int,
    roots: List<Double>,
    arm: Double,
    edgeX: Double,
    width: Double = OrigamiDuplex.INTERHELICAL,
    columns: List<Double> = emptyList(),
    optimiseDirections: Boolean = false
): RowInterleave {
    require(row >= 0) { "row must not be negative, was: $row" }
    val feasible = feasibleRowDirections(roots, arm, edgeX, width)
    require(feasible.isNotEmpty()) {
        "no direction assignment places ${roots.size} arms of $arm nm at $roots on a $edgeX nm " +
                "row — this is not a placement the upward lattice supplies"
    }
    fun of(directions: List<Boolean>): RowInterleave {
        val intervals = roots.zip(directions).map { (root, toward) ->
            armInterval(SlabArm(row, root, toward), arm)
        }
        val free = freeIntervals(intervals, edgeX)
        return RowInterleave(
            row = row,
            arms = roots.size,
            towardPositiveX = directions,
            armIntervals = intervals,
            freeIntervals = free,
            maximumTies = tiesFitting(free, width),
            gridClashes = columnClashes(intervals, columns, width)
        )
    }
    if (!optimiseDirections) {
        val greedy = requireNotNull(armDirections0(roots, arm, edgeX, width)) {
            "the greedy assignment must exist wherever a feasible one does"
        }
        return of(greedy)
    }
    return feasible.map { of(it) }
        .reduce { best, candidate ->
            when {
                candidate.gridClashes < best.gridClashes -> candidate
                candidate.gridClashes > best.gridClashes -> best
                candidate.maximumTies > best.maximumTies -> candidate
                else -> best
            }
        }
}

/** `C-0063`'s [armDirections], with the empty row admitted — the zero-arm limiting case. */
private fun armDirections0(
    roots: List<Double>,
    arm: Double,
    edgeX: Double,
    width: Double
): List<Boolean>? = if (roots.isEmpty()) emptyList()
else armDirections(roots, arm, edgeX, width)

/** The total number of [columns] the whole array refuses, over [rowRoots]. */
fun totalGridClashes(
    rowRoots: List<List<Double>>,
    arm: Double,
    edgeX: Double,
    width: Double,
    columns: List<Double>,
    optimiseDirections: Boolean
): Int = rowRoots.mapIndexed { row, roots ->
    interleaveRow(row, roots, arm, edgeX, width, columns, optimiseDirections).gridClashes
}.sum()

/**
 * The intervals of **rigid translation** of a [columns]-column grid at which not one row refuses
 * a tie, swept at [samples] offsets over one column pitch.
 *
 * The sweep is over a translation and not over the individual columns because a coupling grid is
 * a **registration**: `C-0015`'s `m × 15`, `C-0026`'s one row per duplex, and every flatness
 * number in this programme are written on a regular one. What this function measures is whether
 * a regular grid exists at all.
 */
fun clearingGridOffsets(
    rowRoots: List<List<Double>>,
    arm: Double,
    edgeX: Double,
    width: Double,
    columns: Int,
    samples: Int = 40001
): List<PlanInterval> {
    require(columns > 0) { "columns must be positive, was: $columns" }
    require(samples >= 2) { "samples must be at least 2, was: $samples" }
    val pitch = edgeX / columns
    val nominal = gridColumns(columns, edgeX)
    val limit = edgeX / 2.0 - width / 2.0
    val feasible = rowRoots.map { roots ->
        feasibleRowDirections(roots, arm, edgeX, width).map { directions ->
            roots.zip(directions).map { (root, toward) ->
                if (toward) PlanInterval(root, root + arm) else PlanInterval(root - arm, root)
            }
        }
    }
    val clearing = ArrayList<PlanInterval>()
    var runStart: Double? = null
    var previous = 0.0
    (0 until samples).forEach { step ->
        val offset = -pitch / 2.0 + pitch * step / (samples - 1)
        val shifted = nominal.map { it + offset }
        val inside = shifted.all { abs(it) <= limit + PLAN_TANGENCY_TOLERANCE }
        val clear = inside && feasible.all { options ->
            options.any { intervals -> columnClashes(intervals, shifted, width) == 0 }
        }
        if (clear) {
            if (runStart == null) runStart = offset
        } else {
            runStart?.let { clearing += PlanInterval(it, previous) }
            runStart = null
        }
        previous = offset
    }
    runStart?.let { clearing += PlanInterval(it, previous) }
    return clearing
}

/**
 * The worst clearance in nm between an arm's own **tip link** and any *other* arm of its row.
 *
 * This is the composition in which the tie-down lands on the arm rather than on the tile — the
 * arm's far end is `C-0034`'s two-link `A2` joint, and whatever stands on it is a duplex on the
 * `+z` face like any other. The tip sits at the end of its own arm, so half of its own disc lies
 * over that arm by construction and only the neighbours are at issue.
 */
fun worstTipClearance(
    rowRoots: List<List<Double>>,
    arm: Double,
    edgeX: Double,
    width: Double = OrigamiDuplex.INTERHELICAL
): Double {
    var worst = Double.POSITIVE_INFINITY
    rowRoots.forEachIndexed { row, roots ->
        val interleave = interleaveRow(row, roots, arm, edgeX, width)
        val tips = roots.zip(interleave.towardPositiveX).map { (root, toward) ->
            if (toward) root + arm else root - arm
        }
        tips.forEachIndexed { index, tip ->
            interleave.armIntervals.forEachIndexed { other, interval ->
                if (other != index) {
                    worst = min(worst, max(0.0, max(interval.low - tip, tip - interval.high)))
                }
            }
        }
    }
    return worst
}

/**
 * The tie stations of a [columns]-column grid **snapped** into the room the arms leave, row by
 * row: the feasible `x` nearest each nominal column, assigned in column order with the room each
 * one takes removed, or `null` where a row cannot carry them all.
 *
 * This is the escape the plan view offers, and its price is what it names: the coupling no longer
 * enters on a grid, so every flatness number written on one has to be re-solved.
 */
fun snappedTieStations(
    rowRoots: List<List<Double>>,
    arm: Double,
    edgeX: Double,
    width: Double,
    columns: List<Double>,
    interhelicalDistance: Double = Gen1Tile.INTERHELICAL_SHEET,
    optimiseDirections: Boolean = false
): List<Pair<Double, Double>>? {
    val duplexes = rowRoots.size
    val stations = ArrayList<Pair<Double, Double>>()
    rowRoots.forEachIndexed { row, roots ->
        val y = (row - (duplexes - 1) / 2.0) * interhelicalDistance
        var free = interleaveRow(
            row, roots, arm, edgeX, width, columns, optimiseDirections
        ).freeIntervals
        columns.forEach { target ->
            val candidates = free.filter { it.length >= width - PLAN_TANGENCY_TOLERANCE }
                .map { min(max(target, it.low + width / 2.0), it.high - width / 2.0) }
            val chosen = candidates.minWithOrNull(
                compareBy({ abs(it - target) }, { it })
            ) ?: return null
            stations += chosen to y
            free = free.flatMap { interval ->
                buildList {
                    if (chosen - width / 2.0 > interval.low + PLAN_TANGENCY_TOLERANCE) {
                        add(PlanInterval(interval.low, min(interval.high, chosen - width / 2.0)))
                    }
                    if (interval.high > chosen + width / 2.0 + PLAN_TANGENCY_TOLERANCE) {
                        add(PlanInterval(max(interval.low, chosen + width / 2.0), interval.high))
                    }
                }
            }.filter { it.length > PLAN_TANGENCY_TOLERANCE }
        }
    }
    return stations
}
