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

import com.xemantic.nano.plentyofroom.anchoring.upwardRootLattice
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrthotropicPlate
import com.xemantic.nano.plentyofroom.structure.roundForResult
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * `T-165` — the two axes `C-0093` left unspent on the shared-body topology: **where** the ties
 * land, and **how stiff each of them is**.
 *
 * ## What is a design variable here, and what is not
 *
 * `C-0093` prices a shared body at `C-0063`'s 34 upward roots and on `C-0015`'s abstract
 * `m × 15` equal-tributary grids, with **uniform** ties, and closes on a count: its own
 * redundancy fit demands **252 ties** against the **53** upward crossover sites `C-0066` counts.
 * Two variables were held fixed while that was measured:
 *
 * 1. **the placement** — which sites the ties land on, and at which **lattice phase**. A tie is an
 *    inter-layer crossover, so it must root on an `EAST` (`+z`) junction site; `C-0055`'s own
 *    construction says where those are, and [upwardTieCensus] counts them at every phase. **The
 *    arm footprint that capped `C-0063` at 34 does not apply** — a tie is not an arm — so the
 *    ceiling is the site inventory itself.
 * 2. **the distribution** — the tie stiffnesses. And here the topology changes the *constraint*
 *    and not only the value: under an array `C-0017`'s mandate is a **sum** over the paths, so a
 *    distribution redistributes a scarce budget; under a shared body the mandate lives in the
 *    body's **ground** ([PlacedSharedBody]), and what caps a tie is a **force**.
 *
 * ## The cheap bound that prices the second axis before it is searched
 *
 * Scaling every tie by `s` gives, for a rigid body,
 *
 * ```
 * K_c(s T) = s [T − TΦ(ΦᵀTΦ)⁻¹ΦᵀT] + O(1)
 * ```
 *
 * whose leading term is the **free** body's own projector and diverges in every non-affine
 * direction. The stations are therefore driven onto a **plane** — a *kinematic* constraint whose
 * three remaining freedoms the ground holds — and that limit is reached from **any** tie
 * distribution. [kinematicLimitDeparture] measures the approach, which is first order in `1/s`.
 *
 * So the distribution axis is bounded by the gap between a finite tie ladder and its own
 * kinematic limit. `C-0089` measured the same axis at **1.30–1.61×** on an array, where the
 * mandate is divided and a distribution really is a scarce resource; that number does not
 * transfer, and the reason it does not is the same division that makes the shared body flatter.
 *
 * ## Conventions, restated rather than inherited
 *
 * Lengths **nm**, forces **pN**, stiffness **pN/nm**. `x` runs **along** the helices, `y`
 * **across** them, `z` normal and positive **upward** — away from the grafted layer. `w` is
 * positive **downward**; the origin is the tile centre. A **dropout is a REMOVAL** and it removes
 * the tie; the body stays (`C-0093`).
 */

// ------------------------------------------------------------------ the upward tie lattice

/** The period of the crossover-column phase in base pairs — `C-0015`'s 32, not 16. */
const val UPWARD_TIE_PHASE_PERIOD: Int = 32

/**
 * The `EAST` (`+z`) junction sites of a [duplexes]-duplex sheet at one crossover phase, row by
 * row and ascending in `x` — the stations a shared body's ties may root on.
 *
 * Derived from `C-0055`'s own `upwardRootLattice`, so no second reading of the azimuth rule can
 * drift from it. **The arm footprint is deliberately absent**: `C-0063` had to fit an 8.164 nm
 * arm beside every root and that is what capped it at 34 of 53, while a tie is a crossover and
 * occupies a site and nothing more.
 */
class UpwardTieLattice internal constructor(

    /** The lattice phase in base pairs, in `0 until` [UPWARD_TIE_PHASE_PERIOD]. */
    val phaseBasePairs: Int,

    /** The site positions of each duplex row in nm, ascending. */
    val rows: List<List<Double>>
) {

    /** How many sites the whole lattice offers — the tie-count ceiling at this phase. */
    val siteCount: Int get() = rows.sumOf { it.size }

    /** How many sites each row offers. */
    val rowLengths: List<Int> get() = rows.map { it.size }

    /**
     * The smallest gap between consecutive sites of one row, in nm — the bare per-interface
     * crossover pitch, 32 bp = 10.88 nm (`C-0055`), which is what bounds the column count.
     */
    val rowPitch: Double
        get() = rows.filter { it.size >= 2 }
            .minOf { row -> row.zipWithNext().minOf { (a, b) -> b - a } }

    /** The stations `(x, y)` in nm, row by row and ascending in `x` within a row. */
    fun stations(
        interhelicalDistance: Double = Gen1Tile.INTERHELICAL_SHEET
    ): List<Pair<Double, Double>> = rows.indices.flatMap { row ->
        val y = (row - (rows.size - 1) / 2.0) * interhelicalDistance
        rows[row].map { it to y }
    }

    /**
     * Whether the site set is invariant under `(x, y) → (−x, −y)` — the symmetry a Rothemund
     * sheet has and a mirror one does not, asserted on the **set** rather than inferred.
     */
    fun isCentroSymmetric(tolerance: Double = 1e-9): Boolean = rows.indices.all { row ->
        val mine = rows[row]
        val partner = rows[rows.size - 1 - row].map { -it }.sorted()
        mine.size == partner.size && mine.zip(partner).all { (a, b) -> abs(a - b) <= tolerance }
    }

}

/** The [UpwardTieLattice] at [phaseBasePairs]. */
fun upwardTieLattice(
    phaseBasePairs: Int,
    edgeX: Double,
    duplexes: Int,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR
): UpwardTieLattice {
    require(phaseBasePairs >= 0) {
        "phaseBasePairs must not be negative, was: $phaseBasePairs"
    }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
    return UpwardTieLattice(
        phaseBasePairs,
        upwardRootLattice(phaseBasePairs, edgeX, duplexes, risePerBasePair)
    )
}

/** Every phase of the column lattice, in order — 32 of them, and the census is the cheap bound. */
fun upwardTieCensus(
    edgeX: Double,
    duplexes: Int,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR
): List<UpwardTieLattice> = (0 until UPWARD_TIE_PHASE_PERIOD).map {
    upwardTieLattice(it, edgeX, duplexes, risePerBasePair)
}

/**
 * `C-0089`'s [longestAbsenceRun] on rows that need not be the same length — which the **real**
 * upward lattice's are not: at phase 24 the rows carry 4, 3, 4, 3 … sites.
 *
 * A run is counted inside a row and never across the boundary, because the pitch a run opens up
 * is an along-helix pitch and the next row is a different beam. On a uniform split this is
 * `C-0089`'s own function, which is gate 2.
 */
fun longestAbsenceRunByRow(present: List<Boolean>, rowLengths: List<Int>): Int {
    require(present.isNotEmpty()) { "present must not be empty" }
    require(rowLengths.isNotEmpty()) { "rowLengths must not be empty" }
    require(rowLengths.all { it > 0 }) { "every row must carry at least one site: $rowLengths" }
    require(rowLengths.sum() == present.size) {
        "a presence vector of ${present.size} does not split into rows of $rowLengths"
    }
    var longest = 0
    var index = 0
    rowLengths.forEach { length ->
        var run = 0
        repeat(length) {
            run = if (present[index]) 0 else run + 1
            longest = max(longest, run)
            index++
        }
    }
    return longest
}

/**
 * What the real upward lattice's own pitch is worth against `C-0089`'s run-length demand — the
 * division that needs no solve and no topology at all.
 *
 * `C-0089`: *"a dropout IS an increase in the attachment pitch"*, so surviving `j` consecutive
 * absences in a row needs `columns ≥ (j + 1)·edgeX/ℓ`. The upward lattice's pitch is the bare
 * 32 bp, so a 40 nm row carries at most four columns whatever the phase.
 */
data class UpwardPitchLedger(
    val phaseBasePairs: Int,
    val columnsAvailable: Int,
    val sitePitch: Double,
    val pitchOverBendingLength: Double,
    val worstRunAtP90: Int,
    val survivingPitch: Double,
    val columnsDemanded: Int,
    val columnShortfall: Double,
    val clears: Boolean
)

/** [UpwardPitchLedger] for [lattice] at a 90th-percentile absence run of [worstRun]. */
fun upwardPitchLedger(
    lattice: UpwardTieLattice,
    edgeX: Double,
    bendingLength: Double,
    worstRun: Int
): UpwardPitchLedger {
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(bendingLength > 0.0) { "bendingLength must be positive, was: $bendingLength" }
    require(worstRun >= 0) { "worstRun must not be negative, was: $worstRun" }
    val columns = lattice.rowLengths.max()
    val pitch = lattice.rowPitch
    val demanded = columnsForRunRobustness(edgeX, bendingLength, worstRun)
    return UpwardPitchLedger(
        phaseBasePairs = lattice.phaseBasePairs,
        columnsAvailable = columns,
        sitePitch = pitch,
        pitchOverBendingLength = pitch / bendingLength,
        worstRunAtP90 = worstRun,
        survivingPitch = (worstRun + 1) * pitch,
        columnsDemanded = demanded,
        columnShortfall = demanded.toDouble() / columns,
        clears = demanded <= columns
    )
}

// ------------------------------------------------------------------ the redundancy fit

/**
 * A two-parameter log-log fit of a 90th-percentile dishing against a path count, and the count at
 * which it crosses a tolerance — `C-0093`'s Deliverable 3 as a reusable object.
 *
 * The **slope** is the measured quantity; the crossing is a reading of it, and quoting it beyond
 * the densest grid measured is an extrapolation that must be stated as one.
 */
data class RedundancyFit(
    val slope: Double,
    val intercept: Double,
    val tolerance: Double,
    val countAtTolerance: Double,
    val points: Int
) {

    /** The fitted 90th percentile at a path count of [count]. */
    fun predictedAt(count: Double): Double {
        require(count > 0.0) { "count must be positive, was: $count" }
        return exp(intercept + slope * ln(count))
    }

    /** How much a search must buy at [count] for the fit to reach the tolerance. */
    fun factorDemandedAt(count: Double): Double = predictedAt(count) / tolerance

}

/** [RedundancyFit] over `(path count, 90th percentile)` [points]. */
fun redundancyFit(points: List<Pair<Int, Double>>, tolerance: Double): RedundancyFit {
    require(points.size >= 2) { "a fit needs at least two points, had: ${points.size}" }
    require(tolerance > 0.0) { "tolerance must be positive, was: $tolerance" }
    require(points.all { it.first > 0 }) { "every path count must be positive: $points" }
    require(points.all { it.second > 0.0 }) { "every percentile must be positive: $points" }
    val meanX = points.sumOf { ln(it.first.toDouble()) } / points.size
    val meanY = points.sumOf { ln(it.second) } / points.size
    var covariance = 0.0
    var variance = 0.0
    points.forEach {
        val dx = ln(it.first.toDouble()) - meanX
        covariance += dx * (ln(it.second) - meanY)
        variance += dx * dx
    }
    require(variance > 0.0) {
        "every point sits at the same path count, so there is no slope to fit: $points"
    }
    val slope = covariance / variance
    val intercept = meanY - slope * meanX
    return RedundancyFit(
        slope = slope,
        intercept = intercept,
        tolerance = tolerance,
        countAtTolerance = exp((ln(tolerance) - intercept) / slope),
        points = points.size
    )
}

// ------------------------------------------------------------------ the placed shared body

/** A [SharedBody] with `C-0017`'s mandate placed on its ground, and what that placement cost. */
class PlacedSharedBodyState internal constructor(
    val body: SharedBody,
    val groundScale: Double,
    val heaveSecant: Double,
    val tieSecantCeiling: Double,
    val groundComplianceShare: Double
)

/**
 * A shared body of a stated footprint and rigidity whose **ground** carries `C-0017`'s
 * [mandate] — the public, testable form of `T-162`'s own private body.
 *
 * A [plate] of `null` is the **rigid limit**, which at Ritz degree 1 is exactly the three
 * rigid-body modes and carries no bending energy at all. `C-0093` measured a four-layer
 * honeycomb brick at 1.564× the rigid body's condensed station compliance and a single-layer
 * sheet at 2.311×, so the rigid limit is the optimistic end and is named as such.
 */
class PlacedSharedBody(
    val lengthX: Double,
    val lengthY: Double,
    val mandate: Double,
    val degree: Int = 1,
    val plate: OrthotropicPlate? = null
) {

    init {
        require(mandate > 0.0) { "mandate must be positive, was: $mandate" }
    }

    /** The Ritz modes — degree 1 is the three rigid-body modes, in the order `1`, `ξ`, `η`. */
    val modes: SharedBodyModes = sharedBodyModes(lengthX, lengthY, degree)

    /** The body's own bending stiffness in those coordinates — all zeros at the rigid limit. */
    val bending: Array<DoubleArray> =
        if (plate == null) Array(modes.modeCount) { DoubleArray(modes.modeCount) }
        else modes.bendingStiffness(plate)

    /** A completely **free** body at [stations] — no ground, and the rank statement's own case. */
    fun freeAt(stations: List<Pair<Double, Double>>): SharedBody =
        SharedBody(modes.shapesAt(stations), bending.copyDeep())

    /** The body at [stations] on a distributed ground of [totalStiffness] pN/nm. */
    fun groundedAt(
        stations: List<Pair<Double, Double>>,
        totalStiffness: Double
    ): SharedBody = sharedBody(
        modes.shapesAt(stations), bending, modes.distributedGroundStiffness(totalStiffness)
    )

    /**
     * The body at [stations] with its ground **placed** so that the whole coupling's heave secant
     * is exactly [mandate] — which is what makes this topology's mandate a property of the ground
     * rather than a budget shared between the ties.
     */
    fun placedAt(
        stations: List<Pair<Double, Double>>,
        ties: List<Double>
    ): PlacedSharedBodyState {
        val shapes = modes.shapesAt(stations)
        val unitGround = modes.distributedGroundStiffness(1.0)
        val placement = placeSharedBodyGround(ties, shapes, bending, unitGround, mandate)
        val ground = Array(modes.modeCount) { m ->
            DoubleArray(modes.modeCount) { n -> placement.groundScale * unitGround[m][n] }
        }
        return PlacedSharedBodyState(
            body = sharedBody(shapes, bending, ground),
            groundScale = placement.groundScale,
            heaveSecant = placement.heaveSecant,
            tieSecantCeiling = placement.tieSecantCeiling,
            groundComplianceShare = placement.groundComplianceShare
        )
    }

    private fun Array<DoubleArray>.copyDeep(): Array<DoubleArray> =
        Array(size) { this[it].copyOf() }

}

// ------------------------------------------------------------------ the kinematic limit

/**
 * How far `K_c(s·t)/s` is from the **free** body's own projector `T − TΦ(ΦᵀTΦ)⁻¹ΦᵀT` — the
 * measure of how close a tie ladder is to its own **kinematic** limit.
 *
 * The limit is *"the stations lie on the body's plane"* and it does not depend on the tie
 * distribution at all, so this number is a **ceiling on what a distribution search can buy** at
 * that tie scale, and it falls as `1/s` (gate 3, falsifier `F2`).
 */
fun kinematicLimitDeparture(
    tieShape: List<Double>,
    shapes: Array<DoubleArray>,
    ground: Array<DoubleArray>,
    scale: Double
): Double {
    require(tieShape.isNotEmpty()) { "tieShape must not be empty" }
    require(tieShape.all { it > 0.0 && it.isFinite() }) { "every tie must be positive and finite" }
    require(scale > 0.0) { "scale must be positive, was: $scale" }
    val modeCount = ground.size
    val projector = sharedBodyCouplingMatrix(
        tieShape, SharedBody(shapes, Array(modeCount) { DoubleArray(modeCount) })
    )
    val scaled = sharedBodyCouplingMatrix(
        tieShape.map { it * scale }, SharedBody(shapes, Array(modeCount) { m -> ground[m].copyOf() })
    )
    val reduced = Array(scaled.size) { j -> DoubleArray(scaled.size) { k -> scaled[j][k] / scale } }
    return matrixDeparture(projector, reduced)
}

// ------------------------------------------------------------------ the decision precision

/**
 * [roundForResult] at a **decision** precision, with an exact zero and a non-finite value passed
 * through.
 *
 * `roundForResult` takes `log10(|value|)` and rounds the mantissa, so at an absolute floor of zero
 * an exact `0.0` reaches `roundToLong(NaN)` and **throws** — which is not a numerical edge case
 * here but the case a falsifier test constructs deliberately (*"the objective is exactly zero at
 * the target"*). The floor cannot be used to cure it either, because a decision precision is a
 * statement about **relative** precision and any floor it carried would be a second, silent,
 * absolute one.
 */
private fun decidedAt(value: Double, digits: Int): Double =
    if (value == 0.0 || !value.isFinite()) value else roundForResult(value, digits, 0.0)

// ------------------------------------------------------------------ the distribution search

/** What a [optimiseTieDistribution] found. Nothing here counts a step (`CLAUDE.md`, `P-18`). */
data class TieDistributionOptimum(

    /** The tie stiffnesses in pN/nm, in the order of the stations. */
    val ties: List<Double>,

    /** The objective at those ties. */
    val objective: Double,

    /** The last sweep's relative improvement — the truncation, stated rather than hidden. */
    val lastImprovement: Double
)

/**
 * A deterministic cyclic coordinate descent over the **tie stiffnesses** of a shared body, under
 * a per-tie ceiling and floor and with **no sum constraint at all**.
 *
 * That absence is the whole difference from `C-0058`'s [optimiseStiffnessDistribution], which
 * holds `C-0017`'s mandate as a sum because under an array the mandate *is* the per-station
 * budget. Under a shared body the mandate lives in the body's ground and is re-placed inside the
 * [objective] at every candidate; what bounds a tie is the per-path **force**, which enters here
 * as [upperBound].
 *
 * Each coordinate is searched on its **logarithm** by a coarse scan followed by a golden-section
 * refinement inside the best bracket, exactly as `C-0058`'s descent is, because the peak of a
 * field is a maximum of smooth functions and is piecewise smooth rather than unimodal.
 *
 * A move is accepted only if it improves the objective **rounded to [decisionDigits]**, with the
 * earlier candidate winning ties — `CLAUDE.md`'s *"a decision must be rounded coarser than the
 * number it is taken on"*.
 */
@Suppress("LongParameterList")
fun optimiseTieDistribution(
    start: List<Double>,
    lowerBound: Double,
    upperBound: Double,
    sweeps: Int = 3,
    tolerance: Double = 1e-4,
    scanPoints: Int = 7,
    refinements: Int = 8,
    decisionDigits: Int = 6,
    objective: (List<Double>) -> Double
): TieDistributionOptimum {
    require(start.isNotEmpty()) { "start must not be empty" }
    require(lowerBound > 0.0) { "lowerBound must be positive, was: $lowerBound" }
    require(upperBound > lowerBound) {
        "upperBound must exceed lowerBound, were: $upperBound, $lowerBound"
    }
    require(start.all { it >= lowerBound && it <= upperBound }) {
        "every start must lie inside [$lowerBound, $upperBound], was: $start"
    }
    require(sweeps >= 1) { "sweeps must be at least 1, was: $sweeps" }
    require(scanPoints >= 3) { "scanPoints must be at least 3, was: $scanPoints" }
    require(refinements >= 1) { "refinements must be at least 1, was: $refinements" }
    val ties = start.toMutableList()
    fun decide(value: Double) = decidedAt(value, decisionDigits)
    var best = decide(objective(ties))
    var lastImprovement = 0.0
    val lowLog = ln(lowerBound)
    val highLog = ln(upperBound)
    for (sweep in 1..sweeps) {
        val before = best
        ties.indices.forEach { index ->
            val base = ties[index]
            fun at(logValue: Double): Double {
                ties[index] = exp(min(highLog, max(lowLog, logValue)))
                val value = decide(objective(ties))
                ties[index] = base
                return value
            }

            var bestLog = ln(base)
            var bestValue = best
            val step = (highLog - lowLog) / (scanPoints - 1)
            for (point in 0 until scanPoints) {
                val logValue = lowLog + point * step
                val value = at(logValue)
                if (value < bestValue) {
                    bestValue = value
                    bestLog = logValue
                }
            }
            var low = max(lowLog, bestLog - step)
            var high = min(highLog, bestLog + step)
            val phi = (sqrt(5.0) - 1.0) / 2.0
            var left = high - phi * (high - low)
            var right = low + phi * (high - low)
            var leftValue = at(left)
            var rightValue = at(right)
            repeat(refinements) {
                if (leftValue < rightValue) {
                    high = right
                    right = left
                    rightValue = leftValue
                    left = high - phi * (high - low)
                    leftValue = at(left)
                } else {
                    low = left
                    left = right
                    leftValue = rightValue
                    right = low + phi * (high - low)
                    rightValue = at(right)
                }
            }
            if (leftValue < bestValue) {
                bestValue = leftValue
                bestLog = left
            }
            if (rightValue < bestValue) {
                bestValue = rightValue
                bestLog = right
            }
            if (bestValue < best) {
                best = bestValue
                ties[index] = exp(min(highLog, max(lowLog, bestLog)))
            }
        }
        lastImprovement = if (before > 0.0) (before - best) / before else 0.0
        if (lastImprovement <= tolerance) break
    }
    return TieDistributionOptimum(ties.toList(), best, lastImprovement)
}

// ------------------------------------------------------------------ the placement search

/** What a [descendTieSubset] found — a set of station indices and nothing that counts a step. */
data class TieSubsetOptimum(
    val indices: List<Int>,
    val objective: Double,
    val lastImprovement: Double
)

/**
 * A deterministic swap descent over **which** of a lattice's sites carry ties, at a fixed count.
 *
 * One move class: exchange one chosen station for one unchosen one. It is a **first-improvement**
 * descent — the incumbent is replaced as soon as a swap improves it, and the scan continues from
 * there — so a sweep is not an exhaustive neighbourhood search and the result is reported as what
 * was found. A move is accepted only if it improves the objective **rounded to [decisionDigits]**,
 * so a tie between two placements is broken by the incumbent rather than by the order of
 * summation (`CLAUDE.md`'s argmin trap). Both the chosen set and the scan order are kept sorted,
 * so the whole search is deterministic.
 *
 * This is a **descent** and reports what it found; `InfluenceSurrogate.reachableDishingFloorAt`
 * is what says how much room may be left, and `CH-0104` is why that floor may not be read as a
 * licence.
 */
fun descendTieSubset(
    chosen: List<Int>,
    candidates: List<Int>,
    sweeps: Int = 3,
    decisionDigits: Int = 6,
    objective: (List<Int>) -> Double
): TieSubsetOptimum {
    require(chosen.isNotEmpty()) { "chosen must not be empty" }
    require(chosen.distinct().size == chosen.size) { "chosen must be distinct, was: $chosen" }
    require(candidates.distinct().size == candidates.size) { "candidates must be distinct" }
    require(chosen.all { it in candidates }) {
        "every chosen index must be a candidate, were: $chosen against $candidates"
    }
    require(sweeps >= 1) { "sweeps must be at least 1, was: $sweeps" }
    fun decide(value: Double) = decidedAt(value, decisionDigits)
    var current = chosen.sorted()
    var best = decide(objective(current))
    var lastImprovement = 0.0
    for (sweep in 1..sweeps) {
        val before = best
        val outside = candidates.filter { it !in current }
        if (outside.isEmpty()) break
        current.indices.forEach { slot ->
            outside.forEach { entrant ->
                if (entrant in current) return@forEach
                val candidate = current.toMutableList().also { it[slot] = entrant }.sorted()
                val value = decide(objective(candidate))
                if (value < best) {
                    best = value
                    current = candidate
                }
            }
        }
        lastImprovement = if (before > 0.0) (before - best) / before else 0.0
        if (lastImprovement <= 0.0) break
    }
    return TieSubsetOptimum(current, best, lastImprovement)
}
