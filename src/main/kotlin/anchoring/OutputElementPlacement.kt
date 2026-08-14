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

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.thermalEnergy
import org.openrndr.math.Vector2
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sqrt

/**
 * `T-133` — **is there an output element that does not lie in the plan?**
 *
 * `C-0065` places all 44 of `C-0062`'s truss trios 34 times as **standoffs** and then finds that
 * the **flexure they cap** does not follow: at `C-0030`'s 34-path span of 27.41 nm the same array
 * covers 1.84× the footprint, needs 7 levels and places **12 of 34**. Every coupling element this
 * programme has priced lies in the plan — `C-0023`'s `E3` and `E5`, `C-0030`'s coupled flexure,
 * `C-0039`'s elastica arm — so the branch's remaining question is whether the element space
 * contains anything else.
 *
 * ## The search is closed form, and this file is the closed form
 *
 * `C-0023` establishes that DNA's compliance comes in exactly two kinds — **entropic**, which
 * only pulls, and **bending**, which is signed — and that an element loaded along its own axis
 * must choose. There are therefore only five ways to obtain a normal-direction compliance at all,
 * and each of them has a closed-form length at a stated stiffness:
 *
 * | mechanism | length at stiffness `k` | sidedness |
 * |---|---|---|
 * | axial stretch of a duplex | [axialLengthForStiffness], `S/k` | two-sided |
 * | an entropic strand | [entropicContourForStiffness], `3k_BT/(k b)` | **one-sided** |
 * | rotation at a hinge on a lever | [hingeLeverForStiffness], `√(k_θ/k)` | two-sided |
 * | bending of a duplex | [bendingLengthForStiffness], `(c EI/k)^(1/3)` | two-sided |
 * | torsion of a duplex on a lever | [torsionalLengthForStiffness], `GJ/(k r²)` | two-sided |
 *
 * The **whole element space** is those five, and the only free parameter in the fourth is the
 * end-condition factor `c`, which is decided by the element's **topology**: `c ∈ (0, 12]` for a
 * beam supported once and loaded at its far end (`C-0034`, `C-0039`), `c ∈ [48, 192]` for one
 * supported twice and loaded at its midspan (`C-0025`). So the plan length of a bending element is
 * not a free design choice — it is a cube root of the topology.
 *
 * ## And the plan budget is closed form too
 *
 * `C-0063`'s bound 1 forces **four rows of three** roots (`3a + 2(15 − a) = 34`), and three roots
 * on the upward lattice's 10.88 nm pitch cap a **rooted** element at [rowOfThreeLengthCeiling] —
 * `pitch − d = 8.19 nm` — under `C-0053`'s footprint convention. That bound holds over **every**
 * 34-root placement on the lattice and not only over `C-0063`'s, so a family whose shortest member
 * exceeds it is refused at every span and on every placement.
 *
 * ## Conventions, restated rather than inherited
 *
 * - Lengths **nm**, forces **pN**, stiffness **pN/nm**, rotational stiffness **pN·nm/rad**;
 *   `k_BT = 4.141947 pN·nm` at **300 K** in aqueous **2 mM MgCl₂**.
 * - `x` runs **along** the host sheet's helices, `y` **across** them, `z` **normal and positive
 *   upward**. Origin at the tile centre.
 * - **A duplex in plan is a rectangle of width `d = 2.69 nm`** (SAXS), so two parallel duplexes at
 *   exactly `d` are **tangent and admissible** — `C-0041`'s and `C-0053`'s convention verbatim.
 * - **A rooted element occupies `[root, root ± L]`** and the next along the same row may start at
 *   `high + d` — `C-0053`'s convention, carried unchanged through [armDirections].
 * - **One load path is one element**, and `C-0017`'s 33.3333 pN/nm is a **sum**, so the per-path
 *   secant is [perPathStiffness] at §3's acceptable 3 nm stroke.
 */

// ---------------------------------------------------------------- the per-path budget

/**
 * The secant stiffness in `pN/nm` one of [pathCount] parallel paths must present so that they
 * together discharge [totalStiffness] — `C-0017`'s mandate read as a **sum**.
 */
fun perPathStiffness(totalStiffness: Double, pathCount: Int): Double {
    require(totalStiffness > 0.0) { "totalStiffness must be positive, was: $totalStiffness" }
    require(pathCount > 0) { "pathCount must be positive, was: $pathCount" }
    return totalStiffness / pathCount
}

// ---------------------------------------------------------------- the five mechanism lengths

/** `ℓ = S/k` — the length of a duplex loaded **along its own axis** at stiffness [stiffness]. */
fun axialLengthForStiffness(stretchModulus: Double, stiffness: Double): Double {
    require(stretchModulus > 0.0) { "stretchModulus must be positive, was: $stretchModulus" }
    require(stiffness > 0.0) { "stiffness must be positive, was: $stiffness" }
    return stretchModulus / stiffness
}

/**
 * `L_c = 3k_BT/(k b)` — the contour length of a freely jointed strand whose *unstretched* spring
 * constant is [stiffness]. It is the shortest compliant path DNA offers and it is **one-sided**
 * (`C-0023`'s `E2`, whose reaction and tangent are both exactly zero at negative argument).
 */
fun entropicContourForStiffness(
    stiffness: Double,
    kuhnLength: Double,
    temperature: Double = ROOM_TEMPERATURE
): Double {
    require(stiffness > 0.0) { "stiffness must be positive, was: $stiffness" }
    require(kuhnLength > 0.0) { "kuhnLength must be positive, was: $kuhnLength" }
    return 3.0 * thermalEnergy(temperature) / (stiffness * kuhnLength)
}

/** `r = √(k_θ/k)` — the lever a rotational spring of [hingeStiffness] needs to present [stiffness]. */
fun hingeLeverForStiffness(hingeStiffness: Double, stiffness: Double): Double {
    require(hingeStiffness > 0.0) { "hingeStiffness must be positive, was: $hingeStiffness" }
    require(stiffness > 0.0) { "stiffness must be positive, was: $stiffness" }
    return sqrt(hingeStiffness / stiffness)
}

/**
 * `L = (c EI/k)^(1/3)` — the span a bending element of end-condition factor [endFactor] needs.
 *
 * `c` is a property of the element's **topology and end joints**, never of the designer's
 * intention: `c(ρ_n, ρ_f) ∈ (0, 12]` for a beam supported once and loaded at its far end
 * (`C-0034`'s [twoSpringArmFactor], exactly 3 at a clamped root with a pinned tip and exactly 12
 * at a guided pair), and `c(ρ) = 192(ρ+2)/(ρ+8) ∈ [48, 192]` for one supported twice and loaded
 * at its midspan (`C-0025`).
 */
fun bendingLengthForStiffness(
    endFactor: Double,
    bendingRigidity: Double,
    stiffness: Double
): Double {
    require(endFactor > 0.0) { "endFactor must be positive, was: $endFactor" }
    require(bendingRigidity > 0.0) { "bendingRigidity must be positive, was: $bendingRigidity" }
    require(stiffness > 0.0) { "stiffness must be positive, was: $stiffness" }
    return Math.cbrt(endFactor * bendingRigidity / stiffness)
}

/** The exact inverse of [bendingLengthForStiffness]: the `c` a given plan length would buy. */
fun bendingFactorForLength(
    length: Double,
    bendingRigidity: Double,
    stiffness: Double
): Double {
    require(length > 0.0) { "length must be positive, was: $length" }
    require(bendingRigidity > 0.0) { "bendingRigidity must be positive, was: $bendingRigidity" }
    require(stiffness > 0.0) { "stiffness must be positive, was: $stiffness" }
    return stiffness * length * length * length / bendingRigidity
}

/**
 * `L = GJ/(k r²)` — the length of a duplex twisted about its **own** axis and driven through a
 * lever [lever], the one remaining way a duplex can be loaded that is neither axial nor bending.
 */
fun torsionalLengthForStiffness(
    torsionalRigidity: Double,
    lever: Double,
    stiffness: Double
): Double {
    require(torsionalRigidity > 0.0) {
        "torsionalRigidity must be positive, was: $torsionalRigidity"
    }
    require(lever > 0.0) { "lever must be positive, was: $lever" }
    require(stiffness > 0.0) { "stiffness must be positive, was: $stiffness" }
    return torsionalRigidity / (stiffness * lever * lever)
}

// ---------------------------------------------------------------- the plan budget

/**
 * **The plan length ceiling a row of three roots imposes, in closed form: `pitch − d`.**
 *
 * Three roots on one row of the upward lattice sit at gaps of `pitch` or `2·pitch`, and at most
 * one of the two adjacent pairs can be made to *diverge*; every other pair is either same-sense
 * (needing `L + d ≤ gap`) or converging (needing `2L + d ≤ gap`, which is worse). So the binding
 * case is a same-sense pair at the bare pitch, and the ceiling is `pitch − d` — **8.19 nm** on
 * the Gen-1 sheet, independently of the phase and of which placement is chosen.
 *
 * `C-0063`'s bound 1 (`3a + 2(15 − a) = 34`) forces four rows of three, so **every** 34-root
 * placement on this lattice carries this ceiling.
 */
fun rowOfThreeLengthCeiling(
    pitch: Double,
    width: Double = OrigamiDuplex.INTERHELICAL
): Double {
    require(width > 0.0) { "width must be positive, was: $width" }
    require(pitch > width) {
        "a root pitch of $pitch nm cannot hold an element one duplex ($width nm) wide"
    }
    return pitch - width
}

/** One row of a station set: its roots, ascending, at its own `y`. */
data class StationRow(val row: Int, val y: Double, val roots: List<Double>) {

    init {
        require(row >= 0) { "row must not be negative, was: $row" }
        require(roots.isNotEmpty()) { "a row must carry at least one station" }
        require(roots.sorted() == roots) { "roots must ascend, were: $roots" }
    }

    val count: Int get() = roots.size
}

/** [TrussStation]s grouped into rows, rows ascending and roots ascending within a row. */
fun stationRows(stations: List<TrussStation>): List<StationRow> {
    require(stations.isNotEmpty()) { "stations must not be empty" }
    return stations.groupBy { it.row }.toSortedMap().map { (row, group) ->
        StationRow(row, group.first().y, group.map { it.x }.sorted())
    }
}

/**
 * The largest number of [roots] that can carry a rooted element of [length] at once — **exact**,
 * by enumerating the subsets in descending size and testing each with [armDirections].
 *
 * A row carries at most four upward sites, so this is at most fifteen feasibility tests, and it is
 * a maximum rather than a greedy lower bound.
 */
fun maximumRootedElementsInRow(
    roots: List<Double>,
    length: Double,
    edgeX: Double,
    width: Double = OrigamiDuplex.INTERHELICAL
): Int {
    require(roots.isNotEmpty()) { "roots must not be empty" }
    require(length > 0.0) { "length must be positive, was: $length" }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(width > 0.0) { "width must be positive, was: $width" }
    val ascending = roots.sorted()
    for (size in ascending.size downTo 1) {
        val found = subsetsOfSize(ascending, size).any {
            armDirections(it, length, edgeX, width) != null
        }
        if (found) return size
    }
    return 0
}

private fun subsetsOfSize(items: List<Double>, size: Int): List<List<Double>> {
    if (size == 0) return listOf(emptyList())
    if (size > items.size) return emptyList()
    val out = ArrayList<List<Double>>()
    fun build(start: Int, taken: List<Double>) {
        if (taken.size == size) {
            out += taken
            return
        }
        for (index in start until items.size) build(index + 1, taken + items[index])
    }
    build(0, emptyList())
    return out
}

/**
 * The 34 rooted elements of [length], one per station, with each row's directions taken from
 * [armDirections] — or `null` if some row admits no assignment at all.
 *
 * Directions are searched `+x` first, so the array is deterministic (`C-0053`).
 */
fun rootedElementArray(
    rows: List<StationRow>,
    length: Double,
    edgeX: Double,
    width: Double = OrigamiDuplex.INTERHELICAL,
    verticalMemberFractions: List<Double> = emptyList()
): List<PlanElement>? {
    require(rows.isNotEmpty()) { "rows must not be empty" }
    require(length > 0.0) { "length must be positive, was: $length" }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(width > 0.0) { "width must be positive, was: $width" }
    val out = ArrayList<PlanElement>()
    rows.forEach { row ->
        val directions = armDirections(row.roots, length, edgeX, width) ?: return null
        row.roots.forEachIndexed { index, root ->
            out += PlanElement(
                id = "row ${row.row} station $index",
                anchor = Vector2(root, row.y),
                angle = if (directions[index]) 0.0 else PI,
                length = length,
                width = width,
                anchorFraction = 0.0,
                verticalMemberFractions = verticalMemberFractions
            )
        }
    }
    return out
}

/** The same array when no assignment exists: `+x` where it fits, `−x` where it does not. */
private fun fallbackRootedArray(
    rows: List<StationRow>,
    length: Double,
    edgeX: Double,
    width: Double,
    verticalMemberFractions: List<Double>
): List<PlanElement> = rows.flatMap { row ->
    val directions = armDirections(row.roots, length, edgeX, width)
    row.roots.mapIndexed { index, root ->
        val toward = directions?.get(index)
            ?: (root + length <= 0.5 * edgeX + PLAN_TANGENCY_TOLERANCE ||
                    root - length < -0.5 * edgeX - PLAN_TANGENCY_TOLERANCE)
        PlanElement(
            id = "row ${row.row} station $index",
            anchor = Vector2(root, row.y),
            angle = if (toward) 0.0 else PI,
            length = length,
            width = width,
            anchorFraction = 0.0,
            verticalMemberFractions = verticalMemberFractions
        )
    }
}

/**
 * The largest rooted element every row of [rows] admits, by bisection on [armDirections].
 *
 * Feasibility is monotone decreasing in the length — lengthening an element only tightens every
 * one of `armDirections`' inequalities — so a bisection is exact rather than a search. It exits on
 * the **bracket width** and never on a residual (`CLAUDE.md`).
 */
fun rootedLengthCeiling(
    rows: List<StationRow>,
    edgeX: Double,
    width: Double = OrigamiDuplex.INTERHELICAL,
    resolution: Double = 1.0e-9
): Double {
    require(rows.isNotEmpty()) { "rows must not be empty" }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(width > 0.0) { "width must be positive, was: $width" }
    require(resolution > 0.0) { "resolution must be positive, was: $resolution" }
    fun feasible(length: Double): Boolean =
        rows.all { armDirections(it.roots, length, edgeX, width) != null }
    var low = 1.0e-6
    require(feasible(low)) { "no rooted element of any length places on these rows" }
    var high = edgeX
    var grown = 0
    while (feasible(high) && grown < 40) {
        high *= 2.0
        grown++
    }
    require(!feasible(high)) { "every length up to $high nm places; the rows are unbounded" }
    while (high - low > resolution) {
        val middle = 0.5 * (low + high)
        if (feasible(middle)) low = middle else high = middle
    }
    return low
}

// ---------------------------------------------------------------- the placement outcome

/** What placing one candidate output element at every station of a placement returns. */
data class OutputElementOutcome(
    val label: String,
    val length: Double,
    val demanded: Int,
    val placed: Int,
    val directionsFound: Boolean,
    val overlappingPairs: Int,
    val mutuallyBlockingPairs: Int,
    val memberClashPairs: Int,
    val levelsRequired: Int,
    val singleLevel: Boolean,
    val planAreaFraction: Double,
    val rowsIndependent: Boolean,
    val verdict: String
) {

    /** Whether the whole demanded array places, in one level — `T-133`'s acceptance predicate. */
    val placesInFull: Boolean get() = placed == demanded && singleLevel && directionsFound
}

private fun outcome(
    label: String,
    length: Double,
    demanded: Int,
    placed: Int,
    directionsFound: Boolean,
    elements: List<PlanElement>,
    edgeX: Double,
    lengthY: Double,
    width: Double,
    rowsIndependent: Boolean = true
): OutputElementOutcome {
    val verdict = elementPackingVerdict(elements)
    return OutputElementOutcome(
        label = label,
        length = length,
        demanded = demanded,
        placed = placed,
        directionsFound = directionsFound,
        overlappingPairs = verdict.overlappingPairs,
        mutuallyBlockingPairs = verdict.mutuallyBlockingPairs,
        memberClashPairs = verdict.memberClashPairs,
        levelsRequired = verdict.levelsRequired,
        singleLevel = verdict.singleLevel,
        planAreaFraction = demanded * length * width / (edgeX * lengthY),
        rowsIndependent = rowsIndependent,
        verdict = if (placed == demanded && directionsFound && verdict.singleLevel) {
            "PLACES — all $demanded instances, one level"
        } else {
            "DOES NOT PLACE — $placed of $demanded"
        }
    )
}

/**
 * Places a **rooted** element of [length] at every station, running along its own row.
 *
 * The placed count is the sum of the per-row exact maxima ([maximumRootedElementsInRow]), which is
 * a maximum and not a greedy bound: rows are two duplexes apart in `y` and the elements are one
 * duplex wide, so adjacent rows are **tangent and admissible** and the rows are independent.
 */
fun placeRootedOutputElement(
    label: String,
    rows: List<StationRow>,
    length: Double,
    edgeX: Double,
    lengthY: Double,
    width: Double = OrigamiDuplex.INTERHELICAL,
    verticalMemberFractions: List<Double> = emptyList()
): OutputElementOutcome {
    require(rows.isNotEmpty()) { "rows must not be empty" }
    require(length > 0.0) { "length must be positive, was: $length" }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(lengthY > 0.0) { "lengthY must be positive, was: $lengthY" }
    require(width > 0.0) { "width must be positive, was: $width" }
    val demanded = rows.sumOf { it.count }
    val exact = rootedElementArray(rows, length, edgeX, width, verticalMemberFractions)
    val elements = exact ?: fallbackRootedArray(rows, length, edgeX, width, verticalMemberFractions)
    // rows may be summed independently only while a body of this width cannot reach the next row
    val independent = rows.size < 2 || rows.zipWithNext().all { (below, above) ->
        abs(above.y - below.y) >= width - PLAN_TANGENCY_TOLERANCE
    }
    val placed = if (independent) {
        rows.sumOf { maximumRootedElementsInRow(it.roots, length, edgeX, width) }
    } else {
        greedyConflictFreeElements(elements)
    }
    return outcome(
        label, length, demanded, placed, exact != null && independent, elements, edgeX, lengthY,
        width, independent
    )
}

/**
 * Places an element of [length] at every station at a **fixed** [angle] — `C-0065`'s reading of
 * the flexure, which runs along `−ŷ` from its cap and therefore across the rows.
 *
 * Here the placed count is the greedy conflict-free subset (`C-0053`'s), which is a **lower**
 * bound on the maximum independent set and is exact wherever the array packs cleanly.
 */
fun placeCappedOutputElement(
    label: String,
    rows: List<StationRow>,
    length: Double,
    angle: Double,
    edgeX: Double,
    lengthY: Double,
    width: Double = OrigamiDuplex.INTERHELICAL,
    verticalMemberFractions: List<Double> = emptyList()
): OutputElementOutcome {
    require(rows.isNotEmpty()) { "rows must not be empty" }
    require(length > 0.0) { "length must be positive, was: $length" }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(lengthY > 0.0) { "lengthY must be positive, was: $lengthY" }
    require(width > 0.0) { "width must be positive, was: $width" }
    val elements = rows.flatMap { row ->
        row.roots.mapIndexed { index, root ->
            PlanElement(
                id = "row ${row.row} station $index",
                anchor = Vector2(root, row.y),
                angle = angle,
                length = length,
                width = width,
                anchorFraction = 0.0,
                verticalMemberFractions = verticalMemberFractions
            )
        }
    }
    val verdict = elementPackingVerdict(elements)
    val demanded = elements.size
    val placed = if (verdict.singleLevel && verdict.memberClashPairs == 0) {
        demanded
    } else {
        greedyConflictFreeElements(elements)
    }
    return outcome(label, length, demanded, placed, true, elements, edgeX, lengthY, width)
}

// ------------------------------------------------- the admissible end-restraint window

/**
 * The largest **far**-end rotational restraint in `pN·nm/rad` an end-loaded arm may carry and
 * still place inside [lengthCeiling], or `null` if even a **free** tip already exceeds it.
 *
 * `C-0039`'s exact elastica is monotone increasing in either end restraint — a stiffer end makes a
 * stiffer beam at fixed length, so a longer one is needed to reach the same secant — which is what
 * makes a bisection exact. The search is capped at [maximumRestraint], and reaching the cap means
 * *"a perfect guide would still fit"*, which is reported as the cap and not as an infinity
 * (`CLAUDE.md`: `kotlinx.serialization` refuses `Infinity`).
 */
fun farRestraintCeiling(
    nearStiffness: Double,
    lengthCeiling: Double,
    bendingRigidity: Double = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
    count: Int = 34,
    targetStiffness: Double = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE,
    workingDisplacement: Double = Gen1Tile.ACCEPTABLE_STROKE,
    steps: Int = 400,
    maximumRestraint: Double = 1.0e6,
    resolution: Double = 1.0e-6
): Double? = restraintCeiling(
    lengthCeiling, maximumRestraint, resolution
) { far ->
    elasticaArmForStiffness(
        hingeStiffness = nearStiffness,
        hingeCount = 1,
        farStiffness = far,
        bendingRigidity = bendingRigidity,
        count = count,
        targetStiffness = targetStiffness,
        workingDisplacement = workingDisplacement,
        steps = steps
    )
}

/**
 * The largest **near**-end (root) rotational restraint an end-loaded arm may carry and still place
 * inside [lengthCeiling], at a stated far restraint — or `null` if the softest admissible root
 * already exceeds it.
 *
 * The near restraint cannot be taken to zero at a free tip (a beam free to rotate at both ends is
 * a mechanism, `C-0034`'s `c(0, 0) = 0`), so the search starts at [minimumRestraint].
 */
fun nearRestraintCeiling(
    farStiffness: Double,
    lengthCeiling: Double,
    bendingRigidity: Double = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
    count: Int = 34,
    targetStiffness: Double = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE,
    workingDisplacement: Double = Gen1Tile.ACCEPTABLE_STROKE,
    steps: Int = 400,
    minimumRestraint: Double = 1.0e-3,
    maximumRestraint: Double = 1.0e6,
    resolution: Double = 1.0e-6
): Double? = restraintCeiling(
    lengthCeiling, maximumRestraint, resolution, minimumRestraint
) { near ->
    elasticaArmForStiffness(
        hingeStiffness = near,
        hingeCount = 1,
        farStiffness = farStiffness,
        bendingRigidity = bendingRigidity,
        count = count,
        targetStiffness = targetStiffness,
        workingDisplacement = workingDisplacement,
        steps = steps
    )
}

/**
 * `C-0039`'s exact solver refuses an arm below `1.5 ×` the working stroke, where the tip rotation
 * passes 42° and the chord draw-in is a large fraction of the arm. A refusal is therefore not an
 * error here — it is the statement *"this end restraint asks for an arm shorter than the element
 * model's own floor"*, which is the **floor** of the same window whose ceiling the plan sets.
 */
private fun restraintCeiling(
    lengthCeiling: Double,
    maximumRestraint: Double,
    resolution: Double,
    minimumRestraint: Double = 0.0,
    arm: (Double) -> Double
): Double? {
    require(lengthCeiling > 0.0) { "lengthCeiling must be positive, was: $lengthCeiling" }
    require(maximumRestraint > 0.0) {
        "maximumRestraint must be positive, was: $maximumRestraint"
    }
    require(resolution > 0.0) { "resolution must be positive, was: $resolution" }
    // an arm the exact solver refuses is one that is TOO SHORT, so it is inside the plan ceiling
    fun inside(restraint: Double): Boolean =
        runCatching { arm(restraint) }.getOrNull()?.let { it <= lengthCeiling } ?: true
    if (!inside(minimumRestraint)) return null
    if (inside(maximumRestraint)) return maximumRestraint
    var low = minimumRestraint
    var high = maximumRestraint
    while (high - low > resolution * (1.0 + high)) {
        val middle = 0.5 * (low + high)
        if (inside(middle)) low = middle else high = middle
    }
    return low
}
