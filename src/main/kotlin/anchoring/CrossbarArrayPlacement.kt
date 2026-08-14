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
import org.openrndr.math.Vector2
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.roundToInt

/**
 * `T-130` — **the array question**: does a trio that closes on one lone crossbar survive being
 * repeated **34** times on `C-0063`'s placement?
 *
 * `C-0062` searched one crossbar at a time and found 609 closing trios. A Gen-1 device needs 34 of
 * them, at the 34 upward roots `C-0055` counts and `C-0063` places. Three things a single-crossbar
 * search cannot see, and this file is each of them:
 *
 * 1. **Register.** A trio is a property of the crossbar and the two leg *heads*. A leg also has a
 *    **base**, and a base can only sit where the host duplex's own backbone offers one.
 *    `C-0059`'s and `C-0062`'s base misalignment floors are **minima over the base's axial position
 *    on the host duplex**, and an array **pins** that position — at 34 stations at once.
 * 2. **Plan.** Each instance is two legs and a crossbar above them, and must clear its neighbours'
 *    at the pitch the placement actually has.
 * 3. **Sharing.** 34 instances draw on one sheet.
 *
 * ## Conventions, restated rather than inherited
 *
 * - Lengths **nm**, angles **degrees** in every reported number and radians in code;
 *   `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
 * - `x` runs **along** the host sheet's helices, `y` **across** them, `z` **normal and positive
 *   upward** — away from the grafted layer, which lies below the tile. Origin at the tile centre.
 * - **A duplex in plan is a rectangle of width `d = 2.69 nm`** (SAXS), so two parallel duplexes at
 *   exactly `d` are **tangent and admissible** — `C-0041`'s and `C-0053`'s convention verbatim.
 * - **One truss instance** is `C-0048`'s cap assembly: two legs standing along `+z` from **one**
 *   host duplex at `x_root ∓ w/2`, and one crossbar duplex along `x̂` one duplex radius above their
 *   heads, displaced by the trio's own `axialPhase` in `x` and `lateralSeat` in `y`. In plan it is
 *   **one rectangle with two vertical members** — `C-0053`'s [PlanElement] exactly.
 * - **The station is where the coupling enters.** For `C-0063` that is one upward crossover; for a
 *   truss it is **two leg bases**, and that difference is priced rather than assumed away.
 * - **The upward (`EAST`) azimuth is `C-0055`'s**: [EAST_SITE_BASE_PAIRS] from a duplex's own
 *   `NORTH` plane, with duplex `b`'s azimuth class at plane `k` equal to `(k − 2b) mod 4`. So every
 *   station of every row sits at the **identical** helical phase of its **own** host duplex, which
 *   is the fact that makes the register one question rather than thirty-four.
 */

/**
 * The `EAST` (upward) site's offset from its own duplex's `NORTH` crossover plane, in base pairs.
 *
 * `C-0055`'s azimuth table — `NORTH` 0, `WEST` 8, `SOUTH` 16, `EAST` 24 — at the square lattice's
 * designed 33.75°/bp, which puts `EAST` at exactly a quarter turn.
 */
const val EAST_SITE_BASE_PAIRS: Int = 24

/** How many crossover planes a duplex's azimuth advances per row of the sheet (`C-0055`). */
const val ROW_AZIMUTH_OFFSET_BASE_PAIRS: Int = 16

/** One station of a placement: which row it is on, and where. */
data class TrussStation(val row: Int, val x: Double, val y: Double) {

    init {
        require(row >= 0) { "row must not be negative, was: $row" }
    }
}

/**
 * One truss standoff instance in plan — `C-0048`'s cap assembly, rooted at [rootX] on row [row].
 *
 * The [planElement] carries the **crossbar** as its body and the **two legs** as its vertical
 * members, which is what makes a leg clash level-independent and therefore fatal (`C-0041`).
 */
data class TrussInstance(
    val id: String,
    val row: Int,
    val rootX: Double,
    val y: Double,
    val crossbarBasePairs: Int,
    val separationBasePairs: Int,
    val axialPhase: Double = 0.0,
    val lateralSeat: Double = 0.0,
    val rise: Double = Gen1Tile.RISE_PER_BASE_PAIR,
    val width: Double = OrigamiDuplex.INTERHELICAL
) {

    init {
        require(row >= 0) { "row must not be negative, was: $row" }
        require(separationBasePairs >= 1) {
            "separationBasePairs must be positive, was: $separationBasePairs"
        }
        require(crossbarBasePairs >= separationBasePairs) {
            "a crossbar of $crossbarBasePairs bp does not cover a $separationBasePairs bp row"
        }
        require(rise > 0.0) { "rise must be positive, was: $rise" }
        require(width > 0.0) { "width must be positive, was: $width" }
    }

    /** The crossbar's own length in nm. */
    val crossbarLength: Double get() = crossbarBasePairs * rise

    /** The row pitch `w` in nm — the legs' separation along the host duplex. */
    val legSeparation: Double get() = separationBasePairs * rise

    /** The crossbar's centre in `x`, which the trio's own axial phase displaces from the root. */
    val centreX: Double get() = rootX + axialPhase

    val low: Double get() = centreX - 0.5 * crossbarLength

    val high: Double get() = centreX + 0.5 * crossbarLength

    /** The instance's plan footprint in nm². */
    val planArea: Double get() = crossbarLength * width

    /** The two leg axes, on the host duplex's own line — **not** displaced by the lateral seat. */
    val legPositions: List<Vector2>
        get() = listOf(
            Vector2(rootX - 0.5 * legSeparation, y),
            Vector2(rootX + 0.5 * legSeparation, y)
        )

    /** `C-0053`'s generalised plan element: the crossbar's body, the legs as vertical members. */
    val planElement: PlanElement
        get() {
            val fractions = legPositions.map { (it.x - low) / crossbarLength }
            require(fractions.all { it in 0.0..1.0 }) {
                "the crossbar does not cover both legs at an axial phase of $axialPhase nm"
            }
            return PlanElement(
                id = id,
                anchor = Vector2(centreX, y + lateralSeat),
                angle = 0.0,
                length = crossbarLength,
                width = width,
                anchorFraction = 0.5,
                verticalMemberFractions = fractions
            )
        }
}

/** The plan span one instance demands along its row — `C-0053`'s `arm + d`, for a truss block. */
fun trussPlanDemand(
    crossbarBasePairs: Int,
    width: Double = OrigamiDuplex.INTERHELICAL,
    rise: Double = Gen1Tile.RISE_PER_BASE_PAIR
): Double {
    require(crossbarBasePairs >= 1) {
        "crossbarBasePairs must be positive, was: $crossbarBasePairs"
    }
    require(width > 0.0) { "width must be positive, was: $width" }
    return crossbarBasePairs * rise + width
}

/** One truss instance per station, all carrying the same trio, the array rigidly offset by [offset]. */
fun trussArray(
    stations: List<TrussStation>,
    crossbarBasePairs: Int,
    separationBasePairs: Int,
    offset: Double = 0.0,
    axialPhase: Double = 0.0,
    lateralSeat: Double = 0.0,
    width: Double = OrigamiDuplex.INTERHELICAL,
    rise: Double = Gen1Tile.RISE_PER_BASE_PAIR
): List<TrussInstance> {
    require(stations.isNotEmpty()) { "stations must not be empty" }
    return stations.mapIndexed { index, station ->
        TrussInstance(
            id = "truss $index (row ${station.row})",
            row = station.row,
            rootX = station.x + offset,
            y = station.y,
            crossbarBasePairs = crossbarBasePairs,
            separationBasePairs = separationBasePairs,
            axialPhase = axialPhase,
            lateralSeat = lateralSeat,
            rise = rise,
            width = width
        )
    }
}

/** `C-0053`'s packer on a truss array. */
fun trussArrayPackingVerdict(array: List<TrussInstance>): PackingVerdict {
    require(array.isNotEmpty()) { "array must not be empty" }
    return elementPackingVerdict(array.map { it.planElement })
}

/**
 * The leg clashes counted on the legs' **true** positions, rather than on the crossbar's axis.
 *
 * [PlanElement] puts an element's vertical members on its own body axis, which the trio's lateral
 * seat displaces by up to 0.4 nm; this counts the same relation on the host duplex's own line, and
 * the two are asserted equal — the seat is common to every instance, so it cancels exactly.
 */
fun exactLegClashPairs(
    array: List<TrussInstance>,
    tolerance: Double = PLAN_TANGENCY_TOLERANCE
): Int {
    require(array.isNotEmpty()) { "array must not be empty" }
    var clashes = 0
    for (i in array.indices) {
        for (j in i + 1 until array.size) {
            val minimum = 0.5 * array[i].width + 0.5 * array[j].width
            val clash = array[i].legPositions.any { a ->
                array[j].legPositions.any { b -> (a - b).length < minimum - tolerance }
            }
            if (clash) clashes++
        }
    }
    return clashes
}

/** The flexure each trio's third junction caps, as a plan body running along `−ŷ` from the cap. */
fun flexurePlanElement(
    truss: TrussInstance,
    span: Double,
    width: Double = OrigamiDuplex.INTERHELICAL
): PlanElement {
    require(span > 0.0) { "span must be positive, was: $span" }
    return PlanElement(
        id = "flexure of ${truss.id}",
        anchor = Vector2(truss.centreX, truss.y + truss.lateralSeat),
        angle = -0.5 * kotlin.math.PI,
        length = span,
        width = width,
        anchorFraction = 0.0
    )
}

// ---------------------------------------------------------------- the cheap bound: phase classes

/** How many distinct helical phase classes of their own host duplexes a station set occupies. */
data class PhaseClassCensus(
    val stations: Int,
    val classes: Int,
    /** The distinct local axial coordinates, in base pairs from each duplex's own `NORTH` plane. */
    val localAxialBasePairs: List<Double>,
    val populations: List<Int>
) {

    init {
        require(stations > 0) { "stations must be positive, was: $stations" }
        require(populations.sum() == stations) { "the class populations must sum to the stations" }
    }
}

private fun floorModDouble(value: Double, modulus: Double): Double {
    val remainder = value - modulus * floor(value / modulus)
    return if (abs(remainder - modulus) < 1.0e-9) 0.0 else remainder
}

/**
 * **The cheap bound that decides the shape of the whole answer.**
 *
 * A station's coordinate in its **own** host duplex's frame is `x − rise·(φ + 16·row)`, modulo the
 * 32 bp period: `φ` places the crossover planes and each row's duplex is phase-shifted by exactly
 * the [ROW_AZIMUTH_OFFSET_BASE_PAIRS] its own sites are offset by (`C-0055`'s `(k − 2b) mod 4`).
 * Every upward site therefore lands on the **same** local coordinate, [EAST_SITE_BASE_PAIRS], and
 * the register question is asked once rather than thirty-four times.
 */
fun stationPhaseClassCensus(
    stations: List<TrussStation>,
    phaseBasePairs: Int,
    rise: Double = Gen1Tile.RISE_PER_BASE_PAIR,
    periodBasePairs: Int = UPWARD_ROOT_PITCH_BASE_PAIRS,
    rowOffsetBasePairs: Int = ROW_AZIMUTH_OFFSET_BASE_PAIRS
): PhaseClassCensus {
    require(stations.isNotEmpty()) { "stations must not be empty" }
    require(periodBasePairs >= 1) { "periodBasePairs must be positive, was: $periodBasePairs" }
    val locals = stations.map { station ->
        val basePairs = station.x / rise - phaseBasePairs - rowOffsetBasePairs * station.row
        val folded = floorModDouble(basePairs, periodBasePairs.toDouble())
        (folded * 1.0e6).roundToInt() / 1.0e6
    }
    val grouped = locals.groupingBy { it }.eachCount().toSortedMap()
    return PhaseClassCensus(
        stations = stations.size,
        classes = grouped.size,
        localAxialBasePairs = grouped.keys.toList(),
        populations = grouped.values.toList()
    )
}

// ---------------------------------------------------------------- the register, on the host duplex

/** One axial position of a 90° base junction on the host duplex, and whether it closes. */
data class BaseRegisterPosition(
    val axial: Double,
    val candidates: Int,
    val closes: Boolean,
    val misalignmentDegrees: Double,
    val closure: JunctionClosure?
)

/** A pair of leg bases at one row pitch, centred at [centre]. */
data class BaseRegisterPair(
    val centre: Double,
    val separationBasePairs: Int,
    val worstMisalignmentDegrees: Double,
    val offsetFromStation: Double
)

/**
 * **Where on its host duplex a truss leg's base can actually sit.**
 *
 * `C-0059`'s [SingleJunctionFeasibleSet] and `C-0062`'s pruned [junctionClosesOnSomeAssignment],
 * evaluated on a window of axial positions around the upward site, so that the *station* becomes a
 * coordinate of the junction problem rather than a free minimisation. The field is memoised, pure
 * and deterministic; [solves] reports what it cost, because a register verdict names its budget.
 */
class BaseRegisterField(
    val backbone: DuplexBackbone = DuplexBackbone(),
    val interhelical: Double = Gen1Tile.INTERHELICAL_SHEET,
    val topology: RoutingTopology = RoutingTopology.INDEPENDENT_STAPLES,
    val azimuthSteps: Int = 120,
    val stepsPerBasePair: Int = 2,
    val halfWindowBasePairs: Int = 22,
    val lateralSeat: Double = 0.0,
    val candidatesPerPosition: Int = 4,
    val gridSteps: Int = 60,
    val refinements: Int = 4,
    val eastSiteBasePairs: Int = EAST_SITE_BASE_PAIRS,
    val wantedChordAzimuth: Double = 0.5 * kotlin.math.PI
) {

    init {
        require(azimuthSteps >= 1) { "azimuthSteps must be positive, was: $azimuthSteps" }
        require(stepsPerBasePair >= 2 && stepsPerBasePair % 2 == 0) {
            "stepsPerBasePair must be a positive even number so that a half row pitch lands on " +
                    "the grid, was: $stepsPerBasePair"
        }
        require(halfWindowBasePairs >= 1) {
            "halfWindowBasePairs must be positive, was: $halfWindowBasePairs"
        }
        require(candidatesPerPosition >= 1) {
            "candidatesPerPosition must be positive, was: $candidatesPerPosition"
        }
    }

    /** The axial grid step in nm — half a base pair at the default resolution. */
    val step: Double get() = backbone.risePerBasePair / stepsPerBasePair

    /**
     * The upward site's own axial coordinate on the host duplex, in nm.
     *
     * The **datum**, not a grid parameter: a second reading of *"which strand's backbone faces the
     * neighbour"* moves the station by `−Δ/twist` base pairs, and that is applied to the offsets
     * afterwards rather than to this coordinate — moving the grid would resample a set that lives
     * on a continuum and measure the grid instead of the convention.
     */
    val centreAxial: Double get() = eastSiteBasePairs * backbone.risePerBasePair

    val junctionSet: SingleJunctionFeasibleSet = SingleJunctionFeasibleSet(
        backbone = backbone,
        interhelical = interhelical,
        azimuthSteps = azimuthSteps,
        targetDuplexes = listOf(0),
        wantedChordAzimuth = wantedChordAzimuth
    )

    /** How many junction closure solves the field has consumed. */
    var solves: Int = 0
        private set

    private val steps: Int get() = halfWindowBasePairs * stepsPerBasePair

    private val cache = HashMap<Int, BaseRegisterPosition>()

    private fun at(index: Int): BaseRegisterPosition = cache.getOrPut(index) {
        val axial = centreAxial + index * step
        val candidates = junctionSet.feasibleAt(topology, axial, lateralSeat)
            .sortedWith(compareBy({ it.misalignment }, { it.reachViolation }, { it.closure.azimuth }))
            .take(candidatesPerPosition)
        val winner = candidates.firstOrNull { candidate ->
            solves++
            junctionClosesOnSomeAssignment(
                junctionLinks(backbone, candidate.closure, interhelical), gridSteps, refinements
            )
        }
        BaseRegisterPosition(
            axial = axial,
            candidates = candidates.size,
            closes = winner != null,
            misalignmentDegrees = (winner?.misalignment ?: 0.0) * 180.0 / kotlin.math.PI,
            closure = winner?.closure
        )
    }

    /** Every position of the window, ascending in axial coordinate. */
    val positions: List<BaseRegisterPosition> get() = (-steps..steps).map { at(it) }

    /** The position nearest [axial] on the field's own grid. */
    fun positionAt(axial: Double): BaseRegisterPosition =
        at(((axial - centreAxial) / step).roundToInt())

    /**
     * Every centre at which **both** legs of a pair at [separationBasePairs] close, with four
     * distinct sheet targets — `C-0059`'s own pair condition, read at a pinned centre.
     */
    fun closingPairCentres(separationBasePairs: Int): List<BaseRegisterPair> {
        require(separationBasePairs >= 1) {
            "separationBasePairs must be positive, was: $separationBasePairs"
        }
        val shift = separationBasePairs * stepsPerBasePair / 2
        require(2 * shift == separationBasePairs * stepsPerBasePair) {
            "a half row pitch must land on the grid; use an even stepsPerBasePair"
        }
        val out = ArrayList<BaseRegisterPair>()
        for (index in (-steps + shift)..(steps - shift)) {
            val first = at(index - shift)
            val second = at(index + shift)
            if (!first.closes || !second.closes) continue
            val a = first.closure ?: continue
            val b = second.closure ?: continue
            if (!distinctSheetTargets(a, b)) continue
            val centre = centreAxial + index * step
            out += BaseRegisterPair(
                centre = centre,
                separationBasePairs = separationBasePairs,
                worstMisalignmentDegrees = maxOf(first.misalignmentDegrees, second.misalignmentDegrees),
                offsetFromStation = centre - centreAxial
            )
        }
        return out
    }

    /** The closing pair centre nearest the upward site, or `null` if the row pitch admits none. */
    fun nearestPairCentre(separationBasePairs: Int): BaseRegisterPair? =
        closingPairCentres(separationBasePairs).minByOrNull { abs(it.offsetFromStation) }
}

/** `C-0059`'s four-distinct-targets condition on two base closures. */
fun distinctSheetTargets(first: JunctionClosure, second: JunctionClosure): Boolean {
    val targets = listOf(
        Triple(first.firstDuplex, first.firstStrand, first.firstIndex),
        Triple(first.secondDuplex, first.secondStrand, first.secondIndex),
        Triple(second.firstDuplex, second.firstStrand, second.firstIndex),
        Triple(second.secondDuplex, second.secondStrand, second.secondIndex)
    )
    return targets.toSet().size == 4
}

// ---------------------------------------------------------------- the array verdict

/**
 * Which part of an instance the tile edge is allowed to cut.
 *
 * `C-0053`'s arm is a length of the host's **own** duplex, so it must lie on the sheet; a truss's
 * crossbar is a **free** duplex one radius above the leg heads and nothing forbids it overhanging.
 * The two readings are reported side by side rather than chosen between.
 */
enum class ContainmentRule(val description: String) {

    /** The legs must stand on the sheet; the crossbar above them may overhang the rim. */
    LEGS_ON_SHEET("the legs on the sheet, the crossbar free to overhang"),

    /** `C-0053`'s rule: the whole plan footprint inside the tile. */
    WHOLE_BLOCK("C-0053's containment — the whole block inside the tile")
}

/** What placing one trio at every station of a placement returns. */
data class TrussArrayOutcome(
    val label: String,
    val crossbarBasePairs: Int,
    val separationBasePairs: Int,
    val offsetsOffered: List<Double>,
    val offsetsUsed: List<Double>,
    val demanded: Int,
    val legsOnSheet: Int,
    val insideFootprint: Int,
    val placed: Int,
    val placedWholeBlock: Int,
    val overlappingPairs: Int,
    val mutuallyBlockingPairs: Int,
    val memberClashPairs: Int,
    val levelsRequired: Int,
    val singleLevel: Boolean,
    val planAreaFraction: Double,
    val verdict: String
) {

    /** Whether the whole demanded array places, in one level. */
    val placesInFull: Boolean get() = placed == demanded && singleLevel
}

/**
 * Places one trio at every station of [stations], each instance choosing among the register's
 * [offsets] the one nearest its own station that its containment rule admits.
 *
 * The offsets are a property of the **sheet's** backbone and are the same on every row, but each
 * instance may take any of them: they sit on independent duplexes and 32 bp apart along one. The
 * choice is deterministic — smallest magnitude first — so the placement is reproducible.
 *
 * The surviving count is the largest conflict-free subset a deterministic greedy thinning finds,
 * a **lower** bound on the maximum, reported beside the conflict census that produced it.
 */
fun chooseTrussInstances(
    stations: List<TrussStation>,
    crossbarBasePairs: Int,
    separationBasePairs: Int,
    offsets: List<Double> = listOf(0.0),
    axialPhase: Double = 0.0,
    lateralSeat: Double = 0.0,
    edgeX: Double = Gen1Tile.EDGE_X,
    width: Double = OrigamiDuplex.INTERHELICAL,
    rule: ContainmentRule = ContainmentRule.LEGS_ON_SHEET
): List<TrussInstance> {
    require(stations.isNotEmpty()) { "stations must not be empty" }
    require(offsets.isNotEmpty()) { "offsets must not be empty" }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }

    fun admits(truss: TrussInstance): Boolean = when (rule) {
        ContainmentRule.LEGS_ON_SHEET -> truss.legPositions.all {
            it.x >= -0.5 * edgeX - 0.5 * width - PLAN_TANGENCY_TOLERANCE &&
                    it.x <= 0.5 * edgeX + 0.5 * width + PLAN_TANGENCY_TOLERANCE
        }

        ContainmentRule.WHOLE_BLOCK ->
            truss.low >= -0.5 * edgeX - PLAN_TANGENCY_TOLERANCE &&
                    truss.high <= 0.5 * edgeX + PLAN_TANGENCY_TOLERANCE
    }

    return stations.mapIndexedNotNull { index, station ->
        offsets.sortedBy { abs(it) }
            .map {
                TrussInstance(
                    id = "truss $index (row ${station.row})",
                    row = station.row,
                    rootX = station.x + it,
                    y = station.y,
                    crossbarBasePairs = crossbarBasePairs,
                    separationBasePairs = separationBasePairs,
                    axialPhase = axialPhase,
                    lateralSeat = lateralSeat,
                    width = width
                )
            }
            .firstOrNull { admits(it) }
    }
}

fun placeTrussArray(
    label: String,
    stations: List<TrussStation>,
    crossbarBasePairs: Int,
    separationBasePairs: Int,
    offsets: List<Double> = listOf(0.0),
    axialPhase: Double = 0.0,
    lateralSeat: Double = 0.0,
    edgeX: Double = Gen1Tile.EDGE_X,
    lengthY: Double = 15 * OrigamiDuplex.INTERHELICAL,
    width: Double = OrigamiDuplex.INTERHELICAL,
    flexureSpan: Double = 0.0
): TrussArrayOutcome {
    require(stations.isNotEmpty()) { "stations must not be empty" }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(offsets.isNotEmpty()) { "offsets must not be empty" }
    fun choose(rule: ContainmentRule) = chooseTrussInstances(
        stations, crossbarBasePairs, separationBasePairs, offsets, axialPhase, lateralSeat,
        edgeX, width, rule
    )

    val legsOn = choose(ContainmentRule.LEGS_ON_SHEET)
    val whole = choose(ContainmentRule.WHOLE_BLOCK)
    if (legsOn.isEmpty()) {
        return TrussArrayOutcome(
            label, crossbarBasePairs, separationBasePairs, offsets, emptyList(), stations.size,
            0, 0, 0, 0, 0, 0, 0, UNREALISABLE_LEVEL_COUNT, false, 0.0,
            "NO instance stands on the tile at $crossbarBasePairs bp"
        )
    }

    fun groups(array: List<TrussInstance>): List<List<PlanElement>> = array.map { truss ->
        if (flexureSpan > 0.0) {
            listOf(truss.planElement, flexurePlanElement(truss, flexureSpan, width))
        } else {
            listOf(truss.planElement)
        }
    }

    val elements = groups(legsOn).flatten()
    val verdict = elementPackingVerdict(elements)
    val placed = if (verdict.singleLevel && verdict.memberClashPairs == 0) {
        legsOn.size
    } else {
        greedyConflictFreeCount(groups(legsOn))
    }
    val placedWhole = if (whole.isEmpty()) 0 else greedyConflictFreeCount(groups(whole))
    val area = legsOn.sumOf { it.planArea } +
            (if (flexureSpan > 0.0) legsOn.size * flexureSpan * width else 0.0)
    return TrussArrayOutcome(
        label = label,
        crossbarBasePairs = crossbarBasePairs,
        separationBasePairs = separationBasePairs,
        offsetsOffered = offsets,
        offsetsUsed = legsOn.map { truss ->
            val nearest = stations.filter { it.row == truss.row }
                .minByOrNull { abs(truss.rootX - it.x) } ?: stations.first()
            truss.rootX - nearest.x
        }.map { (it * 1.0e6).roundToInt() / 1.0e6 }.distinct().sorted(),
        demanded = stations.size,
        legsOnSheet = legsOn.size,
        insideFootprint = whole.size,
        placed = placed,
        placedWholeBlock = placedWhole,
        overlappingPairs = verdict.overlappingPairs,
        mutuallyBlockingPairs = verdict.mutuallyBlockingPairs,
        memberClashPairs = verdict.memberClashPairs,
        levelsRequired = verdict.levelsRequired,
        singleLevel = verdict.singleLevel,
        planAreaFraction = area / (edgeX * lengthY),
        verdict = if (placed == stations.size && verdict.singleLevel) {
            "PLACES — all ${stations.size} instances, one level"
        } else {
            "DOES NOT PLACE — $placed of ${stations.size}"
        }
    )
}

/**
 * The largest conflict-free subset a deterministic greedy thinning finds over **groups** of plan
 * elements — one group per instance, because an instance places or does not place as a whole.
 *
 * The element of highest conflict degree is dropped first, ties broken by index, so the answer is
 * reproducible; it is a **lower** bound on the maximum independent set.
 */
fun greedyConflictFreeCount(groups: List<List<PlanElement>>): Int {
    require(groups.isNotEmpty()) { "groups must not be empty" }
    require(groups.all { it.isNotEmpty() }) { "every group must carry at least one element" }
    val n = groups.size
    val conflict = Array(n) { BooleanArray(n) }
    for (i in 0 until n) {
        for (j in i + 1 until n) {
            val clash = groups[i].any { first ->
                groups[j].any { second ->
                    rectanglesOverlap(first.body, second.body) ||
                            elementMembersClash(first, second) ||
                            (elementBlocksVerticalMembers(first, second) &&
                                    elementBlocksVerticalMembers(second, first))
                }
            }
            conflict[i][j] = clash
            conflict[j][i] = clash
        }
    }
    val alive = BooleanArray(n) { true }
    while (true) {
        var worst = -1
        var worstDegree = 0
        for (i in 0 until n) {
            if (!alive[i]) continue
            val degree = (0 until n).count { alive[it] && conflict[i][it] }
            if (degree > worstDegree) {
                worstDegree = degree
                worst = i
            }
        }
        if (worst < 0) break
        alive[worst] = false
    }
    return alive.count { it }
}

/** [greedyConflictFreeCount] over single-element groups. */
fun greedyConflictFreeElements(elements: List<PlanElement>): Int =
    greedyConflictFreeCount(elements.map { listOf(it) })
