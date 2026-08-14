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

import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import org.openrndr.math.Vector2
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.cos
import kotlin.math.floor
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * `T-96`, leaf `A8.2` — the **plan view** of `C-0035`'s surviving flexure array.
 *
 * `C-0035` settles the mounting (`Su`: standoff bases on the output superstructure, standoffs
 * pointing away from the tile, the flexure outboard above the superstructure's plane, each midspan
 * tied back down through it) and prices its apertures as **areas**, saying so explicitly:
 * *"45 flexures of ~32 nm span do not lie side by side in a 40 × 40 nm footprint at a 2.69 nm
 * pitch, so every aperture-area fraction is a scale against the tile footprint and not a layout."*
 *
 * This file supplies the layout. It contains no new physics: it is exact plane geometry on the
 * lattice constants `C-0009` already carries, and the only quantity it takes from a solve is
 * `C-0030`'s placed span.
 *
 * **The conventions, restated (`SESSION-PROMPT.md`).**
 *
 * - `x` runs **along** the tile's helices, `y` **across** them, origin at the tile centre; `z` is
 *   positive **upward**, away from the electrode.
 * - **A duplex in plan is a rectangle of width `d = 2.69 nm`**, the SAXS-measured single-layer
 *   interhelical distance, so two parallel duplexes at exactly `d` are **tangent and admissible**.
 *   That is the lattice condition and it is the loosest defensible one — the steric diameter is
 *   2.0 nm and the lattice packs at 2.69.
 * - **A vertical member — a standoff or a tie — is a disc of radius `d/2`.** It is a duplex
 *   standing normal to the sheet, and a plane it passes through needs a duplex-omission hole.
 * - **A flexure occupies one rectangle `span × d` centred on its midspan, and owns three vertical
 *   members**: two standoffs at its ends and one tie at its midspan.
 * - **The midspans are pinned.** The tie is vertical, so a flexure's midspan sits over its own tile
 *   attachment — `C-0015`'s `m × 15` grid, which `C-0026` shows is one attachment row per duplex
 *   for every `m`.
 */

/** The tolerance in nm below which two plan features are called tangent rather than overlapping. */
const val PLAN_TANGENCY_TOLERANCE: Double = 1.0e-9

// ---------------------------------------------------------------- oriented rectangles

/**
 * An oriented rectangle in plan: a duplex of [length] nm running at [angle] radians to `x`,
 * [width] nm wide, centred at [centre].
 */
data class PlanRectangle(
    val centre: Vector2,
    val angle: Double,
    val length: Double,
    val width: Double
) {

    init {
        require(length > 0.0) { "length must be positive, was: $length" }
        require(width > 0.0) { "width must be positive, was: $width" }
    }

    /** The unit vector along the duplex's own axis. */
    val axis: Vector2 get() = Vector2(cos(angle), sin(angle))

    /** The unit vector across it. */
    val normal: Vector2 get() = Vector2(-sin(angle), cos(angle))

    /** The plan area in nm². */
    val area: Double get() = length * width

    /** The four corners, anticlockwise from the `(+along, +across)` one. */
    val corners: List<Vector2>
        get() {
            val a = axis * (length / 2.0)
            val n = normal * (width / 2.0)
            return listOf(centre + a + n, centre - a + n, centre - a - n, centre + a - n)
        }

    /** The shortest distance in nm from [point] to this rectangle — zero inside it. */
    fun distanceTo(point: Vector2): Double {
        val offset = point - centre
        val along = abs(offset.dot(axis)) - length / 2.0
        val across = abs(offset.dot(normal)) - width / 2.0
        return hypot(max(0.0, along), max(0.0, across))
    }

    /** This rectangle turned by [turn] radians about [pivot]. */
    fun rotatedAbout(pivot: Vector2, turn: Double): PlanRectangle = copy(
        centre = pivot + (centre - pivot).rotateBy(turn),
        angle = angle + turn
    )
}

private fun Vector2.rotateBy(turn: Double): Vector2 =
    Vector2(x * cos(turn) - y * sin(turn), x * sin(turn) + y * cos(turn))

/**
 * Whether two oriented rectangles overlap by more than [tolerance], by the separating-axis
 * theorem on their four face normals.
 *
 * **Tangency is not overlap**: two duplexes whose axes are exactly one interhelical distance apart
 * are what a lattice is made of, so the test is strict.
 */
fun rectanglesOverlap(
    first: PlanRectangle,
    second: PlanRectangle,
    tolerance: Double = PLAN_TANGENCY_TOLERANCE
): Boolean {
    val axes = listOf(first.axis, first.normal, second.axis, second.normal)
    val firstCorners = first.corners
    val secondCorners = second.corners
    axes.forEach { axis ->
        val firstLow = firstCorners.minOf { it.dot(axis) }
        val firstHigh = firstCorners.maxOf { it.dot(axis) }
        val secondLow = secondCorners.minOf { it.dot(axis) }
        val secondHigh = secondCorners.maxOf { it.dot(axis) }
        if (min(firstHigh, secondHigh) - max(firstLow, secondLow) <= tolerance) return false
    }
    return true
}

// ---------------------------------------------------------------- the flexure in plan

/**
 * One flexure of `C-0030`'s design, seen from above: a duplex beam of [span] nm at [angle] radians
 * whose midspan sits at [midspan], with two standoff feet at its ends and one tie at its midspan.
 */
data class PlanFlexure(
    val id: String,
    val midspan: Vector2,
    val angle: Double,
    val span: Double,
    val duplexWidth: Double = OrigamiDuplex.INTERHELICAL
) {

    init {
        require(span > 0.0) { "span must be positive, was: $span" }
        require(duplexWidth > 0.0) { "duplexWidth must be positive, was: $duplexWidth" }
    }

    /** The beam's own plan footprint. */
    val body: PlanRectangle get() = PlanRectangle(midspan, angle, span, duplexWidth)

    /** The tie descends from here, vertically, to the tile. */
    val tiePoint: Vector2 get() = midspan

    /** The two standoffs rise from here, vertically, out of the superstructure. */
    val standoffFeet: List<Vector2>
        get() {
            val half = Vector2(cos(angle), sin(angle)) * (span / 2.0)
            return listOf(midspan - half, midspan + half)
        }

    /** The three vertical members this flexure needs a clear column for. */
    val verticalMembers: List<Vector2> get() = standoffFeet + tiePoint

    /** The radius in nm of each vertical member — a duplex standing normal to the sheet. */
    val memberRadius: Double get() = duplexWidth / 2.0

    /** This flexure turned by [turn] radians about [pivot]. */
    fun rotatedAbout(pivot: Vector2, turn: Double): PlanFlexure = copy(
        midspan = pivot + (midspan - pivot).rotateBy(turn),
        angle = angle + turn
    )
}

/**
 * Whether [lower]'s beam covers any of [upper]'s three vertical members — and therefore whether
 * `level(lower) > level(upper)` is **required** rather than optional.
 *
 * This is the whole of why stacking is a decidable question rather than an area budget: an upper
 * flexure's two standoffs have to reach the superstructure and its tie has to reach the tile, so
 * both must pass through every beam plane below. Two flexures that block **each other** are
 * infeasible at any level count, on a body of any size.
 */
fun blocksVerticalMembers(
    lower: PlanFlexure,
    upper: PlanFlexure,
    tolerance: Double = PLAN_TANGENCY_TOLERANCE
): Boolean {
    val body = lower.body
    return upper.verticalMembers.any { body.distanceTo(it) < upper.memberRadius - tolerance }
}

/**
 * Whether any vertical member of [first] comes closer than one duplex pitch to any of [second]'s.
 *
 * **This is level-independent and therefore absolutely fatal.** Every standoff runs from the
 * superstructure up to its own beam plane and every tie runs from its beam plane down to the tile,
 * so any two vertical members of the array share a height range whatever levels their beams sit
 * at. Two beams whose members clash cannot be separated by stacking, by re-ordering, or by a
 * larger body.
 */
fun verticalMembersClash(
    first: PlanFlexure,
    second: PlanFlexure,
    tolerance: Double = PLAN_TANGENCY_TOLERANCE
): Boolean {
    val minimum = first.memberRadius + second.memberRadius
    return first.verticalMembers.any { a ->
        second.verticalMembers.any { b -> (a - b).length < minimum - tolerance }
    }
}

/**
 * Whether [lower]'s beam covers [upper]'s **tie** alone.
 *
 * Separated from [blocksVerticalMembers] because it carries a theorem the standoff case does not:
 * the tie sits at the centre of its own beam, and the beam's half-width **is** the tie's radius,
 * so covering a tie implies overlapping the beam that owns it. A standoff foot sits on its beam's
 * *end*, where half the tie's disc lies outboard, and coverage there does not.
 */
fun blocksTie(
    lower: PlanFlexure,
    upper: PlanFlexure,
    tolerance: Double = PLAN_TANGENCY_TOLERANCE
): Boolean = lower.body.distanceTo(upper.tiePoint) < upper.memberRadius - tolerance

// ---------------------------------------------------------------- the packing verdict

/** What a plan-view array of flexures costs in levels, and whether it is realisable at all. */
data class PackingVerdict(
    val count: Int,
    val overlappingPairs: Int,
    val blockingPairs: Int,
    val mutuallyBlockingPairs: Int,
    val memberClashPairs: Int,
    val feasibleAtAnyLevelCount: Boolean,
    val levelsRequired: Int,
    val levels: List<Int>
) {

    /** True when the whole array lies in one plane. */
    val singleLevel: Boolean get() = feasibleAtAnyLevelCount && levelsRequired == 1
}

/** The sentinel level count reported when no assignment exists at any number of levels. */
const val UNREALISABLE_LEVEL_COUNT: Int = -1

/**
 * Solves the level assignment for [flexures].
 *
 * Two relations are built:
 *
 * - **overlap** — two beams sharing plan area may not share a level;
 * - **blocking** — `Y` covers one of `X`'s vertical members, so `level(Y) > level(X)` strictly.
 *
 * A directed cycle in the blocking relation (of which a mutually blocking pair is the shortest
 * case) makes the array **infeasible at every level count**. Otherwise the flexures are visited in
 * topological order and each is put at the lowest level that clears both relations, which is
 * deterministic and terminates in at most [count] levels.
 */
fun packingVerdict(flexures: List<PlanFlexure>): PackingVerdict {
    require(flexures.isNotEmpty()) { "flexures must not be empty" }
    val n = flexures.size
    val overlaps = Array(n) { BooleanArray(n) }
    val blocks = Array(n) { BooleanArray(n) }
    var overlapping = 0
    var blocking = 0
    var mutual = 0
    var clashes = 0
    for (i in 0 until n) {
        for (j in 0 until n) {
            if (i == j) continue
            if (blocksVerticalMembers(flexures[i], flexures[j])) {
                blocks[i][j] = true
                blocking++
            }
        }
    }
    for (i in 0 until n) {
        for (j in i + 1 until n) {
            if (rectanglesOverlap(flexures[i].body, flexures[j].body)) {
                overlaps[i][j] = true
                overlaps[j][i] = true
                overlapping++
            }
            if (blocks[i][j] && blocks[j][i]) mutual++
            if (verticalMembersClash(flexures[i], flexures[j])) clashes++
        }
    }
    // `blocks[i][j]` demands level(i) > level(j); a cycle in that demand is unrealisable.
    val order = topologicalOrder(n) { from, to -> blocks[to][from] }
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

/**
 * A topological order of `0 until [count]`, or `null` if the relation [precedes] has a cycle.
 *
 * `precedes(from, to)` means `from` must be visited before `to`.
 */
private fun topologicalOrder(count: Int, precedes: (Int, Int) -> Boolean): List<Int>? {
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

// ---------------------------------------------------------------- arrays and areas

/** The plan area in nm² that [count] beams of [span] and [width] occupy, laid out or not. */
fun arrayPlanArea(count: Int, span: Double, width: Double = OrigamiDuplex.INTERHELICAL): Double {
    require(count > 0) { "count must be positive, was: $count" }
    require(span > 0.0) { "span must be positive, was: $span" }
    require(width > 0.0) { "width must be positive, was: $width" }
    return count * span * width
}

/**
 * The flexure array whose midspans are `C-0015`'s [columns] × [rows] attachment grid on an
 * [edgeX] × [edgeY] tile, every beam of [span] laid at [angle] to `x`.
 */
fun gridFlexureArray(
    columns: Int,
    rows: Int,
    edgeX: Double,
    edgeY: Double,
    span: Double,
    angle: Double,
    width: Double = OrigamiDuplex.INTERHELICAL
): List<PlanFlexure> = attachmentGrid(columns, rows, edgeX, edgeY).mapIndexed { index, (x, y) ->
    PlanFlexure("F$index", Vector2(x, y), angle, span, width)
}

/** What a whole orientation sweep found, over `[0, π)`. */
data class OrientationSweep(
    val samples: Int,
    val feasibleOrientations: Int,
    val singleLevelOrientations: Int,
    val minimumOverlappingPairs: Int,
    val minimumMutuallyBlockingPairs: Int,
    val minimumMemberClashPairs: Int,
    val bestAngleDegrees: Double
)

/**
 * Sweeps the beam orientation over `[0, π)` at [samples] steps, on a fixed set of [midspans].
 *
 * The angle is continuous — the beam is a free duplex between two standoff heads — but the two
 * conditions that bound it are lattice quantities, so the sweep reports a **window** rather than a
 * best value, exactly as `C-0015` insists.
 */
fun orientationSweep(
    midspans: List<Vector2>,
    span: Double,
    samples: Int = 720,
    width: Double = OrigamiDuplex.INTERHELICAL
): OrientationSweep {
    require(midspans.isNotEmpty()) { "midspans must not be empty" }
    require(samples > 0) { "samples must be positive, was: $samples" }
    var feasible = 0
    var single = 0
    var minimumOverlaps = Int.MAX_VALUE
    var minimumMutual = Int.MAX_VALUE
    var minimumClashes = Int.MAX_VALUE
    var bestAngle = 0.0
    (0 until samples).forEach { step ->
        val angle = step * PI / samples
        val verdict = packingVerdict(
            midspans.mapIndexed { index, point ->
                PlanFlexure("F$index", point, angle, span, width)
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
 * The largest number of attachment columns whose untilted beams of [span] lie side by side on a
 * tile of edge [edgeX] — `⌊edgeX/(span + d)⌋`, because the column pitch is `edgeX/m` and two
 * collinear beams need **one duplex more than their span**: their standoff feet sit on the beam
 * ends, so beams laid end to end put two standoffs in the same place.
 *
 * A single column is admitted whenever the beam merely fits, `span ≤ edgeX`, there being no
 * neighbour to clear. Quoted as a closed form and **cross-checked against the solved layout** in
 * [packingLimitedPathCount], which knows nothing of it.
 */
fun packingLimitedColumns(
    edgeX: Double,
    span: Double,
    width: Double = OrigamiDuplex.INTERHELICAL
): Int {
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(span > 0.0) { "span must be positive, was: $span" }
    require(width > 0.0) { "width must be positive, was: $width" }
    val many = floor(edgeX / (span + width)).toInt()
    return if (many >= 1) many else if (span <= edgeX) 1 else 0
}

/**
 * The largest path count that packs in **one** level on an [edgeX] × ([rows] · `d`) tile at a span
 * of [span], found by solving the layout at every column count rather than by a formula.
 */
fun packingLimitedPathCount(
    edgeX: Double,
    rows: Int,
    span: Double,
    width: Double = OrigamiDuplex.INTERHELICAL
): Int {
    require(rows > 0) { "rows must be positive, was: $rows" }
    var best = 0
    (1..rows).forEach { columns ->
        val array = gridFlexureArray(columns, rows, edgeX, rows * width, span, 0.0, width)
        if (packingVerdict(array).singleLevel && span <= edgeX / columns + PLAN_TANGENCY_TOLERANCE) {
            best = max(best, columns * rows)
        }
    }
    return best
}

// ---------------------------------------------------------------- levels in the height envelope

/**
 * The slot length in nm a beam of [span] and end restraint [restraint] needs in a plane
 * [clearance] nm below its own undeflected axis, when its midspan travels [stroke].
 *
 * `C-0035`'s [apertureLength] with the clearance supplied directly rather than computed from a
 * standoff length — because in a **stacked** array the plane below a beam is not always the
 * superstructure. It reproduces [apertureLength] identically at `clearance = ℓ − d`.
 */
fun slotLengthForClearance(
    span: Double,
    restraint: Double,
    stroke: Double,
    clearance: Double,
    scanSteps: Int = 256
): Double {
    require(span > 0.0) { "span must be positive, was: $span" }
    require(stroke >= 0.0) { "stroke must not be negative, was: $stroke" }
    require(clearance >= 0.0) { "clearance must not be negative, was: $clearance" }
    if (stroke <= clearance || stroke <= 0.0) return 0.0
    val level = clearance / stroke
    return span * (1.0 - 2.0 * apertureHalfPositionFraction(restraint, level, scanSteps))
}

/**
 * The beam-plane heights in nm available to a stacked array at [stroke], given
 *
 * - `C-0030`'s clearance condition `ℓ ≥ stroke + d` (the midspan must not reach the plane it
 *   stands on),
 * - the steric separation [stericSeparation] between beam planes,
 * - `C-0017`'s [maximumLength] envelope above,
 * - and the 0.34 nm rise, which quantises a standoff (`C-0023`: a preload is a LENGTH).
 *
 * Empty when §3's stroke leaves no admissible standoff at all — which is what happens at the
 * desired 10 nm stroke, and is `C-0030`'s `ℓ ≥ 12.69 nm` in another form.
 */
fun availableLevelHeights(
    stroke: Double,
    contactDistance: Double = OrigamiDuplex.INTERHELICAL,
    stericSeparation: Double = OrigamiDuplex.DIAMETER,
    maximumLength: Double = 10.0,
    quantum: Double = Gen1Tile.RISE_PER_BASE_PAIR
): List<Double> {
    require(stroke >= 0.0) { "stroke must not be negative, was: $stroke" }
    require(contactDistance > 0.0) { "contactDistance must be positive, was: $contactDistance" }
    require(stericSeparation > 0.0) { "stericSeparation must be positive, was: $stericSeparation" }
    require(quantum > 0.0) { "quantum must be positive, was: $quantum" }
    require(maximumLength > contactDistance) {
        "maximumLength must exceed the contact distance, was: $maximumLength"
    }
    val levels = ArrayList<Double>()
    var steps = ceil((stroke + contactDistance) / quantum - 1.0e-9).toInt()
    while (steps * quantum <= maximumLength + 1.0e-12) {
        val height = steps * quantum
        if (levels.isEmpty() || height - levels.last() >= stericSeparation - 1.0e-12) {
            levels += height
        }
        steps++
    }
    return levels
}

// ---------------------------------------------------------------- the superstructure as a sheet

/** What the tie apertures do to the superstructure **as a sheet** rather than as an area. */
data class SheetSeverance(
    val duplexes: Int,
    val holes: Int,
    val segments: Int,
    val crossovers: Int,
    val components: Int
) {

    /** True when the apertures leave the body in more than one piece. */
    val severed: Boolean get() = components > 1
}

/**
 * The connected components of a single-layer Rothemund superstructure of [edgeX] × [edgeY] once
 * the [holes] — one duplex-omission hole of [holeWidth] per tie — have been cut out of it.
 *
 * `C-0035` prices the tie apertures as **326 nm², 20.4 % of the footprint**. An area is not the
 * question a sheet asks. The holes sit on the attachment grid, whose across-helix pitch is
 * *exactly* one duplex, so a column of ties removes a whole line of material.
 *
 * The lattice is `C-0015`'s: a given interface is linked every **32 bp = 10.88 nm**, and adjacent
 * interfaces are offset by half of that because crossovers alternate between a helix's two
 * neighbours. A crossover survives only where it lies in intact material on **both** sides.
 */
fun superstructureSeverance(
    holes: List<Vector2>,
    edgeX: Double,
    edgeY: Double,
    helixAlongX: Boolean,
    holeWidth: Double = OrigamiDuplex.INTERHELICAL,
    pitch: Double = OrigamiDuplex.INTERHELICAL,
    crossoverSpacing: Double = Gen1Tile.CROSSOVER_SPACING_SHEET_BP * Gen1Tile.RISE_PER_BASE_PAIR,
    crossoverPhase: Double = 0.0
): SheetSeverance {
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(edgeY > 0.0) { "edgeY must be positive, was: $edgeY" }
    require(holeWidth > 0.0) { "holeWidth must be positive, was: $holeWidth" }
    require(pitch > 0.0) { "pitch must be positive, was: $pitch" }
    require(crossoverSpacing > 0.0) {
        "crossoverSpacing must be positive, was: $crossoverSpacing"
    }
    val alongEdge = if (helixAlongX) edgeX else edgeY
    val acrossEdge = if (helixAlongX) edgeY else edgeX
    val count = max(1, (acrossEdge / pitch).roundToInt())
    fun axisOf(index: Int) = (index + 0.5) * pitch - count * pitch / 2.0
    val cuts = Array(count) { ArrayList<Pair<Double, Double>>() }
    holes.forEach { hole ->
        val along = if (helixAlongX) hole.x else hole.y
        val across = if (helixAlongX) hole.y else hole.x
        val index = (0 until count).minByOrNull { abs(axisOf(it) - across) } ?: 0
        if (abs(axisOf(index) - across) < holeWidth / 2.0 + PLAN_TANGENCY_TOLERANCE) {
            cuts[index] += (along - holeWidth / 2.0) to (along + holeWidth / 2.0)
        }
    }
    // every duplex becomes an ordered list of intact segments
    val segments = Array(count) { index ->
        val merged = ArrayList<Pair<Double, Double>>()
        cuts[index].sortedBy { it.first }.forEach { cut ->
            val last = merged.lastOrNull()
            if (last != null && cut.first <= last.second + PLAN_TANGENCY_TOLERANCE) {
                merged[merged.size - 1] = last.first to max(last.second, cut.second)
            } else merged += cut
        }
        val pieces = ArrayList<Pair<Double, Double>>()
        var start = -alongEdge / 2.0
        merged.forEach { (low, high) ->
            if (low - start > PLAN_TANGENCY_TOLERANCE) pieces += start to min(low, alongEdge / 2.0)
            start = max(start, high)
        }
        if (alongEdge / 2.0 - start > PLAN_TANGENCY_TOLERANCE) pieces += start to alongEdge / 2.0
        pieces
    }
    val offsets = IntArray(count)
    var total = 0
    (0 until count).forEach { index ->
        offsets[index] = total
        total += segments[index].size
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
    fun segmentAt(index: Int, position: Double): Int? =
        segments[index].indexOfFirst { position >= it.first && position <= it.second }
            .takeIf { it >= 0 }?.let { offsets[index] + it }
    var crossovers = 0
    (0 until count - 1).forEach { interfaceIndex ->
        val offset = crossoverPhase + (interfaceIndex % 2) * crossoverSpacing / 2.0
        var step = ceil((-alongEdge / 2.0 - offset) / crossoverSpacing).toInt()
        while (offset + step * crossoverSpacing <= alongEdge / 2.0 + PLAN_TANGENCY_TOLERANCE) {
            val position = offset + step * crossoverSpacing
            val low = segmentAt(interfaceIndex, position)
            val high = segmentAt(interfaceIndex + 1, position)
            if (low != null && high != null) {
                crossovers++
                union(low, high)
            }
            step++
        }
    }
    val components = (0 until total).map { find(it) }.distinct().size
    return SheetSeverance(count, holes.size, total, crossovers, max(components, 1))
}

/**
 * A single column of [rows] tie holes, alternating `±stagger/2` along `x` — the cheapest way to
 * break the collinearity that severs the superstructure.
 *
 * `C-0026`'s finding fixes the attachment **rows** (one per duplex) and says nothing about where
 * along a row an attachment sits, so a stagger is free of every upstream claim.
 */
fun staggeredTieColumn(rows: Int, edgeY: Double, stagger: Double): List<Vector2> {
    require(rows > 0) { "rows must be positive, was: $rows" }
    require(edgeY > 0.0) { "edgeY must be positive, was: $edgeY" }
    require(stagger >= 0.0) { "stagger must not be negative, was: $stagger" }
    return (0 until rows).map { row ->
        Vector2(
            if (row % 2 == 0) -stagger / 2.0 else stagger / 2.0,
            edgeY * (row + 0.5) / rows - edgeY / 2.0
        )
    }
}

/**
 * The smallest stagger in nm, quantised to the rise, that leaves the superstructure in one piece
 * while every beam of [span] still lies inside [edgeX] — or `0.0` if none does.
 */
fun smallestConnectingStagger(
    rows: Int,
    edgeX: Double,
    edgeY: Double,
    span: Double,
    quantum: Double = Gen1Tile.RISE_PER_BASE_PAIR,
    crossoverPhase: Double = 0.0
): Double {
    require(rows > 0) { "rows must be positive, was: $rows" }
    require(span > 0.0) { "span must be positive, was: $span" }
    require(quantum > 0.0) { "quantum must be positive, was: $quantum" }
    val ceiling = edgeX - span
    if (ceiling <= 0.0) return 0.0
    var steps = 1
    while (steps * quantum <= ceiling + 1.0e-12) {
        val stagger = steps * quantum
        val holes = staggeredTieColumn(rows, edgeY, stagger)
        if (superstructureSeverance(
                holes, edgeX, edgeY, true, crossoverPhase = crossoverPhase
            ).components == 1
        ) return stagger
        steps++
    }
    return 0.0
}
