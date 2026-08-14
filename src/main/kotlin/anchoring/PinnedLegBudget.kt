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
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

/**
 * Task `T-132` — does the leg's own length budget survive the **pinned** base misalignment, at
 * **one** leg length shared by all 34 instances?
 *
 * ## What changes when the base is pinned
 *
 * `C-0052` established that a truss leg is **one body with two junctions**, so its base and cap
 * chords differ by `m × 33.74°` folded modulo `π` — only the **difference** is quantised, and
 * rotating the leg about its own axis trades the two misalignments one for one. `C-0062` composed
 * its design table on that budget by sweeping the rotation freely against two independent
 * misalignment **floors**.
 *
 * `C-0065` pinned the base. A leg's base sits only where the host's own backbone offers one, no row
 * pitch closes at the station itself, and no crossover phase can absorb the offset. So the leg's
 * rotation is **not** free: it is whatever the register's closing placement presents, and the cap
 * misalignment then **follows** from the base and the length.
 *
 * Three consequences, none of which the free composition has:
 *
 * 1. **A pinned base can only OVERSPEND the budget.** `ψ_base + ψ_cap ≥ |m τ − 90°|` for every
 *    pinned deviation, with equality only when the pinned deviation happens to oppose the budget's
 *    own sense. A free rotation always achieves equality; a pinned one pays up to `2|δ|` more.
 * 2. **A truss has TWO legs and they are pinned at two DIFFERENT axial positions of one duplex**,
 *    so their two base chords differ — and their two cap chords differ by exactly the same folded
 *    angle, **independently of the leg length**. That difference is a floor on the worst cap
 *    misalignment that no length can beat: `max(ψ_cap,A, ψ_cap,B) ≥ |fold(δ_A − δ_B)| / 2`.
 * 3. **All 34 caps sit at one height, so one length serves the array** — and because every station
 *    is the same helical phase class of its own duplex (`C-0065`'s bound 3), the 68 leg bases fall
 *    into exactly **two** classes of 34, one per leg of the pair. The array clause is therefore a
 *    **two-body** condition and not a thirty-four-body one.
 *
 * ## Geometry and sign conventions
 *
 * `C-0052`'s and `C-0065`'s, restated: `x` along the host's helices, `y` across, `z` normal and
 * positive upward. A **chord is a line**, so an azimuth folds modulo `π`; a **misalignment** is the
 * folded distance in `[0, π/2]` and a **signed deviation** lies in `[−π/2, π/2]`. The base chord
 * wants the flexure's own axis (`wantedChordAzimuth = π/2`) and the cap chord wants **across** it,
 * so the wanted separation is `π/2`. Twist `10.67 bp/turn` on the square lattice, `10.5` swept.
 *
 * ## What is modelled and what is not
 *
 * A torsion closure is a **necessary** condition and never a sufficient one, so every *"closes"*
 * here is an **upper bound on buildability**. The cap floor is imposed as
 * `max(cap_geometric, capFloor)` — `C-0062`'s own device and its own independence assumption, which
 * presumes the crossbar can be arranged to close at any misalignment at or above its floor. That
 * bounds the achievable design from the **favourable** side.
 */

// ---------------------------------------------------------------- the fold, made explicit

/** An angle folded onto the line `[−π/2, π/2]`, because a chord has no head and no tail. */
fun foldChordAngleToLine(angle: Double): Double {
    var folded = angle % PI
    if (folded > 0.5 * PI) folded -= PI
    if (folded < -0.5 * PI) folded += PI
    return folded
}

/**
 * The **signed** departure of a chord at [chordAzimuth] from the direction [wanted], in radians on
 * `[−π/2, π/2]`.
 *
 * `C-0042`'s [foldedChordMisalignment] is its magnitude, and that magnitude is all the free
 * composition ever needs — a free rotation can move the chord either way. A **pinned** base cannot,
 * so the sign is exactly the information this task turns on.
 */
fun signedChordDeviation(chordAzimuth: Double, wanted: Double = 0.5 * PI): Double =
    foldChordAngleToLine(chordAzimuth - wanted)

// ---------------------------------------------------------------- cheap bound 2: the chord floor

/**
 * **The floor no leg length can beat**, in radians.
 *
 * Two legs pinned at signed deviations [first] and [second] carry cap chords whose signed
 * deviations differ by `fold(first − second)` at **every** leg length, because the length rotates
 * both by the same `m τ`. The worst of the two is therefore at least half that difference, by the
 * triangle inequality on the folded line metric, and equality is reached when the two straddle.
 */
fun sharedCapFloor(first: Double, second: Double): Double =
    0.5 * abs(foldChordAngleToLine(first - second))

/**
 * The widest gap, in radians, between consecutive relative chord azimuths over a leg-length
 * [range] — how coarsely the envelope samples the chord circle.
 *
 * The design can only reach [sharedCapFloor] to within **half** of this, because the leg length is
 * an integer number of base pairs and the chord it delivers is quantised with it.
 */
fun chordSampleSpacing(range: IntRange, backbone: DuplexBackbone = DuplexBackbone()): Double {
    require(!range.isEmpty()) { "range must not be empty" }
    val values = range.map { relativeChordAzimuth(it, backbone) }.sorted()
    var widest = values.first() + PI - values.last()
    for (i in 0 until values.size - 1) widest = max(widest, values[i + 1] - values[i])
    return widest
}

// ---------------------------------------------------------------- cheap bound 1: the class count

/** How many distinct helical phase classes the **leg bases** of a truss array occupy. */
data class LegBaseClassCensus(
    val trusses: Int,
    val legBases: Int,
    val classes: Int,
    /** The distinct local axial coordinates, in base pairs from each duplex's own `NORTH` plane. */
    val localAxialBasePairs: List<Double>,
    val populations: List<Int>,
    /** How many distinct `(low leg, high leg)` class pairs the array presents. */
    val distinctPairs: Int
) {

    init {
        require(trusses > 0) { "trusses must be positive, was: $trusses" }
        require(populations.sum() == legBases) { "the class populations must sum to the leg bases" }
    }

    /**
     * **The cheap bound's verdict.** One leg length serves every instance exactly when every truss
     * presents the *same* pair of base classes — which reduces *"one length for 34 instances"* to
     * *"one length for two legs"*.
     */
    val oneLengthServesAll: Boolean get() = distinctPairs == 1
}

private fun foldToPeriod(value: Double, modulus: Double): Double {
    val remainder = value - modulus * floor(value / modulus)
    val snapped = if (abs(remainder - modulus) < 1.0e-9) 0.0 else remainder
    return (snapped * 1.0e6).roundToInt() / 1.0e6
}

/**
 * `C-0065`'s [stationPhaseClassCensus] carried from the **station** to the **two leg bases** a
 * truss actually roots at — the station's own local coordinate displaced by the register's
 * [centreOffsetBasePairs] and then by ∓ half the row pitch.
 */
fun legBaseClassCensus(
    stations: List<TrussStation>,
    separationBasePairs: Int,
    centreOffsetBasePairs: Double,
    phaseBasePairs: Int,
    periodBasePairs: Int = UPWARD_ROOT_PITCH_BASE_PAIRS,
    rowOffsetBasePairs: Int = ROW_AZIMUTH_OFFSET_BASE_PAIRS,
    rise: Double = Gen1Tile.RISE_PER_BASE_PAIR
): LegBaseClassCensus {
    require(stations.isNotEmpty()) { "stations must not be empty" }
    require(separationBasePairs >= 1) {
        "separationBasePairs must be positive, was: $separationBasePairs"
    }
    require(periodBasePairs >= 1) { "periodBasePairs must be positive, was: $periodBasePairs" }
    val period = periodBasePairs.toDouble()
    val half = 0.5 * separationBasePairs
    val pairs = stations.map { station ->
        val local = station.x / rise - phaseBasePairs - rowOffsetBasePairs * station.row +
                centreOffsetBasePairs
        foldToPeriod(local - half, period) to foldToPeriod(local + half, period)
    }
    val flat = pairs.flatMap { listOf(it.first, it.second) }
    val grouped = flat.groupingBy { it }.eachCount().toSortedMap()
    return LegBaseClassCensus(
        trusses = stations.size,
        legBases = flat.size,
        classes = grouped.size,
        localAxialBasePairs = grouped.keys.toList(),
        populations = grouped.values.toList(),
        distinctPairs = pairs.toSet().size
    )
}

// ---------------------------------------------------------------- the register of SIGNED azimuths

/** One closing base placement at a pinned axial position, with the **sign** of its chord kept. */
data class PinnedBaseCandidate(
    val axial: Double,
    val chordAzimuth: Double,
    val signedDeviation: Double,
    val closure: JunctionClosure
) {

    /** `C-0065`'s misalignment: the magnitude the free composition keeps. */
    val misalignment: Double get() = abs(signedDeviation)
}

/** One axial position of the register, with **every** closing candidate rather than the winner. */
data class PinnedRegisterPosition(
    val index: Int,
    val axial: Double,
    val candidates: Int,
    val closers: List<PinnedBaseCandidate>
) {

    val closes: Boolean get() = closers.isNotEmpty()

    /**
     * The best-aligned closer — **`C-0065`'s `BaseRegisterField` winner**, because the candidates
     * are ranked by misalignment before any of them is solved.
     */
    val winner: PinnedBaseCandidate? get() = closers.firstOrNull()
}

/** A pair of leg bases at one row pitch, with every admissible base azimuth at each. */
data class PinnedLegPair(
    val separationBasePairs: Int,
    val centre: Double,
    val offsetFromStation: Double,
    /** The **low** leg, at `centre − w/2`. */
    val legA: List<PinnedBaseCandidate>,
    /** The **high** leg, at `centre + w/2`. */
    val legB: List<PinnedBaseCandidate>
) {

    /** `C-0065`'s published number: the worse of the two winners' misalignments. */
    val worstMisalignment: Double
        get() = max(
            legA.firstOrNull()?.misalignment ?: 0.0,
            legB.firstOrNull()?.misalignment ?: 0.0
        )
}

/**
 * `C-0065`'s [BaseRegisterField] with the sign kept and **every** closing candidate retained.
 *
 * The candidate ranking is `C-0065`'s exactly — misalignment, then reach violation, then azimuth —
 * so [PinnedRegisterPosition.winner] **is** that field's winner, and the reproduction is a gate
 * test rather than an assumption. What is new is everything after the first closer: a pinned design
 * may prefer a worse-aligned base that buys a better cap, and the free composition cannot see it.
 */
class PinnedBaseRegister(
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
    val wantedChordAzimuth: Double = 0.5 * PI
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

    val step: Double get() = backbone.risePerBasePair / stepsPerBasePair

    val centreAxial: Double get() = eastSiteBasePairs * backbone.risePerBasePair

    val junctionSet: SingleJunctionFeasibleSet = SingleJunctionFeasibleSet(
        backbone = backbone,
        interhelical = interhelical,
        azimuthSteps = azimuthSteps,
        targetDuplexes = listOf(0),
        wantedChordAzimuth = wantedChordAzimuth
    )

    /** How many junction closure solves the register has consumed. */
    var solves: Int = 0
        private set

    private val steps: Int get() = halfWindowBasePairs * stepsPerBasePair

    private val cache = HashMap<Int, PinnedRegisterPosition>()

    private fun at(index: Int): PinnedRegisterPosition = cache.getOrPut(index) {
        val axial = centreAxial + index * step
        val candidates = junctionSet.feasibleAt(topology, axial, lateralSeat)
            .sortedWith(compareBy({ it.misalignment }, { it.reachViolation }, { it.closure.azimuth }))
            .take(candidatesPerPosition)
        val closers = candidates.filter { candidate ->
            solves++
            junctionClosesOnSomeAssignment(
                junctionLinks(backbone, candidate.closure, interhelical), gridSteps, refinements
            )
        }.map { candidate ->
            PinnedBaseCandidate(
                axial = axial,
                chordAzimuth = candidate.closure.chordAzimuth,
                signedDeviation = signedChordDeviation(
                    candidate.closure.chordAzimuth, wantedChordAzimuth
                ),
                closure = candidate.closure
            )
        }
        PinnedRegisterPosition(
            index = index,
            axial = axial,
            candidates = candidates.size,
            closers = closers
        )
    }

    /** Every position of the window, ascending in axial coordinate. */
    val positions: List<PinnedRegisterPosition> get() = (-steps..steps).map { at(it) }

    /** The position nearest [axial] on the register's own grid. */
    fun positionAt(axial: Double): PinnedRegisterPosition =
        at(((axial - centreAxial) / step).roundToInt())

    /**
     * Every centre at which **both** legs of a pair at [separationBasePairs] close with four
     * distinct sheet targets — `C-0065`'s own admission rule, on the two winners.
     */
    fun closingPairCentres(separationBasePairs: Int): List<PinnedLegPair> {
        require(separationBasePairs >= 1) {
            "separationBasePairs must be positive, was: $separationBasePairs"
        }
        val shift = separationBasePairs * stepsPerBasePair / 2
        require(2 * shift == separationBasePairs * stepsPerBasePair) {
            "a half row pitch must land on the grid; use an even stepsPerBasePair"
        }
        val out = ArrayList<PinnedLegPair>()
        for (index in (-steps + shift)..(steps - shift)) {
            val low = at(index - shift)
            val high = at(index + shift)
            val a = low.winner ?: continue
            val b = high.winner ?: continue
            if (!distinctSheetTargets(a.closure, b.closure)) continue
            out += PinnedLegPair(
                separationBasePairs = separationBasePairs,
                centre = centreAxial + index * step,
                offsetFromStation = index * step,
                legA = low.closers,
                legB = high.closers
            )
        }
        return out
    }

    /** The closing pair centre nearest the upward site, or `null` if the row pitch admits none. */
    fun nearestPair(separationBasePairs: Int): PinnedLegPair? =
        closingPairCentres(separationBasePairs).minByOrNull { abs(it.offsetFromStation) }
}

// ---------------------------------------------------------------- the pinned composition

/** `C-0048`'s design at a leg length, with **both** bases pinned by the register. */
data class PinnedTrussDesign(
    val separationBasePairs: Int,
    val legSteps: Int,
    val legLength: Double,
    val baseADegrees: Double,
    val baseBDegrees: Double,
    val baseDegrees: Double,
    val capASignedDegrees: Double,
    val capBSignedDegrees: Double,
    val capADegrees: Double,
    val capBDegrees: Double,
    /** What the pinned bases and this length deliver, before any chemistry floor. */
    val capGeometricDegrees: Double,
    val capFloorDegrees: Double,
    val capDegrees: Double,
    val flexureDegrees: Double,
    /** `C-0052`'s `chordPairMisalignment(m)` — the budget a **free** rotation would spend exactly. */
    val budgetDegrees: Double,
    val spentDegrees: Double,
    val overspendDegrees: Double,
    val representable: Boolean,
    val frameCouple: Double,
    val capBending: Double,
    val capTorsion: Double,
    val span: Double,
    val tangent: Double,
    val duty: Double,
    val criticalLoadCanDo: Double,
    val criticalLoadFields: Double,
    val marginCanDo: Double,
    val marginFields: Double,
    val governingPlane: String,
    val passes: Boolean,
    val verdict: String
)

private const val HALF_RIGHT_ANGLE = 0.25 * PI

/**
 * `C-0052`'s [chordPairMisalignment] re-composed against a **pinned** base — the two legs' signed
 * chord deviations [deviationA] and [deviationB] as the register delivers them — at one leg length
 * [legSteps] shared by both legs, hence by every instance of the array.
 */
fun pinnedTrussDesign(
    legSteps: Int,
    deviationA: Double,
    deviationB: Double,
    capFloor: Double,
    flexureFloor: Double,
    separationBasePairs: Int,
    backbone: DuplexBackbone = DuplexBackbone(),
    rise: Double = Gen1Tile.RISE_PER_BASE_PAIR
): PinnedTrussDesign {
    require(legSteps >= 1) { "legSteps must be positive, was: $legSteps" }
    require(capFloor >= 0.0 && flexureFloor >= 0.0) {
        "a misalignment floor must not be negative, was: $capFloor / $flexureFloor"
    }
    require(separationBasePairs >= 1) {
        "separationBasePairs must be positive, was: $separationBasePairs"
    }
    val degrees = 180.0 / PI
    val dA = foldChordAngleToLine(deviationA)
    val dB = foldChordAngleToLine(deviationB)
    val relative = relativeChordAzimuth(legSteps, backbone)
    val capSignedA = foldChordAngleToLine(0.5 * PI + dA + relative)
    val capSignedB = foldChordAngleToLine(0.5 * PI + dB + relative)
    val baseA = abs(dA)
    val baseB = abs(dB)
    val base = max(baseA, baseB)
    val capA = abs(capSignedA)
    val capB = abs(capSignedB)
    val capGeometric = max(capA, capB)
    val budget = chordPairMisalignment(legSteps, 0.5 * PI, backbone)
    val spent = max(baseA + capA, baseB + capB)
    val cap = max(capGeometric, capFloor)
    val legLength = legSteps * rise
    val row = separationBasePairs * rise
    val bending = capBendingStiffness(Gen1Tile.DUPLEX_BENDING_RIGIDITY, row, 12.0)
    val torsion = capTorsionalStiffness(Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY, row)
    if (base > HALF_RIGHT_ANGLE) {
        return PinnedTrussDesign(
            separationBasePairs = separationBasePairs,
            legSteps = legSteps,
            legLength = legLength,
            baseADegrees = baseA * degrees,
            baseBDegrees = baseB * degrees,
            baseDegrees = base * degrees,
            capASignedDegrees = capSignedA * degrees,
            capBSignedDegrees = capSignedB * degrees,
            capADegrees = capA * degrees,
            capBDegrees = capB * degrees,
            capGeometricDegrees = capGeometric * degrees,
            capFloorDegrees = capFloor * degrees,
            capDegrees = cap * degrees,
            flexureDegrees = flexureFloor * degrees,
            budgetDegrees = budget * degrees,
            spentDegrees = spent * degrees,
            overspendDegrees = (spent - budget) * degrees,
            representable = false,
            frameCouple = 0.0,
            capBending = bending,
            capTorsion = torsion,
            span = 0.0,
            tangent = 0.0,
            duty = 0.0,
            criticalLoadCanDo = 0.0,
            criticalLoadFields = 0.0,
            marginCanDo = 0.0,
            marginFields = 0.0,
            governingPlane = "none",
            passes = false,
            verdict = "NOT REPRESENTABLE — the PINNED base misalignment exceeds a half right " +
                    "angle, past which C-0037's TwoLinkBase invariant cannot represent the base"
        )
    }
    val design = capDesign(
        legLength = legLength,
        separationBasePairs = separationBasePairs,
        baseMisalignment = base,
        capMisalignment = cap,
        flexureMisalignment = flexureFloor,
        legSteps = legSteps,
        legRotation = dA
    )
    return PinnedTrussDesign(
        separationBasePairs = separationBasePairs,
        legSteps = legSteps,
        legLength = legLength,
        baseADegrees = baseA * degrees,
        baseBDegrees = baseB * degrees,
        baseDegrees = base * degrees,
        capASignedDegrees = capSignedA * degrees,
        capBSignedDegrees = capSignedB * degrees,
        capADegrees = capA * degrees,
        capBDegrees = capB * degrees,
        capGeometricDegrees = capGeometric * degrees,
        capFloorDegrees = capFloor * degrees,
        capDegrees = cap * degrees,
        flexureDegrees = flexureFloor * degrees,
        budgetDegrees = budget * degrees,
        spentDegrees = spent * degrees,
        overspendDegrees = (spent - budget) * degrees,
        representable = true,
        frameCouple = design.frameCouple,
        capBending = bending,
        capTorsion = torsion,
        span = design.span,
        tangent = design.tangent,
        duty = design.duty,
        criticalLoadCanDo = design.criticalLoad,
        criticalLoadFields = design.criticalLoadFields,
        marginCanDo = design.marginCanDo,
        marginFields = design.marginFields,
        governingPlane = design.governingPlane,
        passes = design.passes,
        verdict = design.verdict
    )
}

/** What one row pitch's pinned register delivers, over the whole shared leg-length envelope. */
data class PinnedRowOutcome(
    val separationBasePairs: Int,
    val offsetFromStation: Double,
    val candidatePairs: Int,
    val evaluated: Int,
    val sharedCapFloorDegrees: Double,
    val bestLegSteps: Int,
    val representableLengths: Int,
    val passingLengths: Int,
    val best: PinnedTrussDesign?,
    val verdict: String
)

/**
 * The **single** leg length that serves both legs of a pinned pair best — which, by
 * [legBaseClassCensus], is the single leg length that serves the whole array.
 *
 * Every admissible base combination is tried, not only the best-aligned one, because a pinned
 * design may prefer a worse base that buys a better cap. `C-0059`'s four-distinct-targets condition
 * is imposed on the combination, exactly as the register's own admission rule imposes it on the
 * winners.
 */
fun bestPinnedDesign(
    pair: PinnedLegPair,
    capFloor: Double,
    flexureFloor: Double,
    legRange: IntRange = 12..26,
    backbone: DuplexBackbone = DuplexBackbone(),
    rise: Double = Gen1Tile.RISE_PER_BASE_PAIR
): PinnedRowOutcome {
    require(!legRange.isEmpty()) { "legRange must not be empty" }
    val combinations = ArrayList<Pair<PinnedBaseCandidate, PinnedBaseCandidate>>()
    pair.legA.forEach { a ->
        pair.legB.forEach { b ->
            if (distinctSheetTargets(a.closure, b.closure)) combinations += a to b
        }
    }
    if (combinations.isEmpty()) {
        return PinnedRowOutcome(
            separationBasePairs = pair.separationBasePairs,
            offsetFromStation = pair.offsetFromStation,
            candidatePairs = 0,
            evaluated = 0,
            sharedCapFloorDegrees = 0.0,
            bestLegSteps = 0,
            representableLengths = 0,
            passingLengths = 0,
            best = null,
            verdict = "NO admissible base pair — the row does not register at this centre"
        )
    }
    var best: PinnedTrussDesign? = null
    var bestSteps = 0
    var evaluated = 0
    var floorDegrees = Double.MAX_VALUE
    val representable = HashSet<Int>()
    val passing = HashSet<Int>()
    combinations.forEach { (a, b) ->
        floorDegrees = min(
            floorDegrees, sharedCapFloor(a.signedDeviation, b.signedDeviation) * 180.0 / PI
        )
        legRange.forEach { steps ->
            evaluated++
            val design = pinnedTrussDesign(
                legSteps = steps,
                deviationA = a.signedDeviation,
                deviationB = b.signedDeviation,
                capFloor = capFloor,
                flexureFloor = flexureFloor,
                separationBasePairs = pair.separationBasePairs,
                backbone = backbone,
                rise = rise
            )
            if (design.representable) representable += steps
            if (design.passes) passing += steps
            val incumbent = best
            val better = incumbent == null ||
                    (design.passes && !incumbent.passes) ||
                    (design.passes == incumbent.passes &&
                            design.criticalLoadCanDo > incumbent.criticalLoadCanDo)
            if (design.representable && better) {
                best = design
                bestSteps = steps
            }
        }
    }
    val found = best
    return PinnedRowOutcome(
        separationBasePairs = pair.separationBasePairs,
        offsetFromStation = pair.offsetFromStation,
        candidatePairs = combinations.size,
        evaluated = evaluated,
        sharedCapFloorDegrees = if (floorDegrees == Double.MAX_VALUE) 0.0 else floorDegrees,
        bestLegSteps = bestSteps,
        representableLengths = representable.size,
        passingLengths = passing.size,
        best = found,
        verdict = when {
            found == null ->
                "NOT REPRESENTABLE at any leg length — the pinned base is past the half right angle"
            found.passes ->
                ("A SHARED LEG LENGTH SURVIVES — %d steps (%.2f nm), base %.1f°, cap %.1f°, " +
                        "margin %.2f on CanDo and %.2f on Fields").format(
                    bestSteps, found.legLength, found.baseDegrees, found.capDegrees,
                    found.marginCanDo, found.marginFields
                )
            else ->
                ("NO SHARED LEG LENGTH PASSES — the best of %d representable lengths is %d steps, " +
                        "%s").format(representable.size, bestSteps, found.verdict)
        }
    )
}
