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
import com.xemantic.nano.plentyofroom.structure.OrigamiSheet
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

/**
 * `T-153` — the Gen-1 tile at the **buildable** seamless raster width, 112 bp = 38.08 nm.
 *
 * ## Which axis this is
 *
 * `C-0086`'s odd-half-turn rule binds the distance between **successive scaffold crossovers**, and
 * in a boustrophedon those are the two ends of one row — so the quantity it quantises is the
 * **along-helix** length, `Gen1Tile.EDGE_X`. The across-helix span is a *count of duplexes*
 * (`15 × 2.69 = 40.35 nm`) and the scaffold does not raster along it, so nothing across the
 * helices moves. `CLAUDE.md`: a discreteness criterion must pair lengths in the same direction.
 *
 * ## The three lattice facts this file exists to compute
 *
 * 1. **`38.08 = 7 × 5.44` exactly.** The tile is an integer number of column pitches, where the
 *    nominal 40.0 nm spans `7.35` of them. So the column census stops being a truncation with a
 *    remainder and becomes a *tangency*: a column lands **on the row end** at exactly the phases
 *    [endOfRowColumnPhases] names, and nowhere else.
 * 2. **Those phases are 8 and 24** — `C-0063`'s two centro-symmetric phases, one of which is its
 *    winner. In a seamless boustrophedon the row-end column **is** the scaffold crossover that
 *    turns the raster, so the routing and the phase are one choice.
 * 3. **The binding arm ceiling switches.** A three-arm row is bounded by its *inboard* pair,
 *    `pitch − d` = 8.19 nm (`C-0069`'s budget, which carries no tile width), **and** by its
 *    *outboard* arm, `edgeX/2 − pitch`, which carries nothing else. The two cross at
 *    [armCeilingCrossoverWidth] = 38.14 nm, and 38.08 nm falls **0.176 base pairs** below it.
 *
 * ## The end-of-row convention, named rather than assumed
 *
 * `CrossoverLayout.phased` keeps only the columns **strictly inside** the footprint, by
 * `CrossoverLayout.EDGE_MARGIN` = 0.05 nm, so that a column cannot seed a zero-length beam
 * element. At a width that is an exact multiple of the pitch that convention **deletes the row-end
 * crossover**, which in a seamless raster is the one crossover that certainly exists. Every
 * function here therefore takes `admitRowEnd`, and with `admitRowEnd = false` reproduces
 * `C-0015`'s and `C-0055`'s published lattices to the last bit — which is gate 2.
 *
 * Conventions: lengths **nm**; `x` along the helices, `y` across them, `z` normal and positive
 * **upward**, away from the grafted layer.
 */

/** The buildable seamless raster row length nearest §3's 40.0 nm, in base pairs — `C-0086`. */
const val BUILDABLE_RASTER_ROW_BASE_PAIRS: Int = 112

/** That row length as an along-helix width in nm — **38.08**, 4.8 % below §3's nominal 40.0. */
val BUILDABLE_RASTER_WIDTH: Double =
    BUILDABLE_RASTER_ROW_BASE_PAIRS * Gen1Tile.RISE_PER_BASE_PAIR

/** The sheet's own column lattice pitch in base pairs — half the 32 bp per-interface spacing. */
const val COLUMN_PITCH_BASE_PAIRS: Int = 16

/** How many base-pair rises [length] nm is. */
fun risesIn(length: Double, rise: Double = Gen1Tile.RISE_PER_BASE_PAIR): Double {
    require(rise > 0.0) { "rise must be positive, was: $rise" }
    return length / rise
}

/**
 * [length] rounded **down** to a whole number of base-pair rises.
 *
 * `C-0085`: a plan length in DNA is quantised at the rise, so a solved root that lands at
 * 24.0129 rises is not a length any duplex has. Rounding **down** is the conservative direction
 * for a member whose ceiling is a clearance and whose floor is a stiffness the design can trim
 * elsewhere by one base pair (`C-0075`).
 */
fun quantisedToRise(length: Double, rise: Double = Gen1Tile.RISE_PER_BASE_PAIR): Double {
    require(rise > 0.0) { "rise must be positive, was: $rise" }
    require(length >= 0.0) { "length must not be negative, was: $length" }
    return floor(length / rise + 1.0e-9) * rise
}

// ------------------------------------------------------------------ the width/phase congruences

/** Whether a row of [rowBasePairs] is an exact whole number of column pitches. */
fun isIntegerColumnPitches(
    rowBasePairs: Int,
    columnPitchBasePairs: Int = COLUMN_PITCH_BASE_PAIRS
): Boolean {
    require(rowBasePairs > 0) { "rowBasePairs must be positive, was: $rowBasePairs" }
    require(columnPitchBasePairs > 0) {
        "columnPitchBasePairs must be positive, was: $columnPitchBasePairs"
    }
    return rowBasePairs % columnPitchBasePairs == 0
}

/**
 * The column-lattice phases at which a crossover column lands **exactly on the row end**, i.e. at
 * `x = ±rowBasePairs/2` rises from the tile centre.
 *
 * A **closed-form congruence, not a search**: the columns are at `phase + m·pitch`, so a column
 * sits at `−rowBasePairs/2` exactly when `phase ≡ −rowBasePairs/2 (mod pitch)`, which has a
 * solution only when `rowBasePairs/2` is an integer number of half-pitches — and then the tile's
 * *other* end carries one too, because the row length is a whole number of pitches. Empty
 * whenever the row is not an integer pitch count, which is why §3's 117.6 bp has no such phase.
 *
 * The list runs over the **32 bp** period `CrossoverLayout.BASE_PAIRS_PER_PERIOD`, because a shift
 * by one pitch leaves the positions unchanged and hands every interface the other parity's columns.
 */
fun endOfRowColumnPhases(
    rowBasePairs: Int,
    columnPitchBasePairs: Int = COLUMN_PITCH_BASE_PAIRS,
    periodBasePairs: Int = CrossoverLayout.BASE_PAIRS_PER_PERIOD
): List<Int> {
    require(rowBasePairs > 0) { "rowBasePairs must be positive, was: $rowBasePairs" }
    require(columnPitchBasePairs > 0) {
        "columnPitchBasePairs must be positive, was: $columnPitchBasePairs"
    }
    require(periodBasePairs > 0) { "periodBasePairs must be positive, was: $periodBasePairs" }
    if (rowBasePairs % 2 != 0) return emptyList()
    if (!isIntegerColumnPitches(rowBasePairs, columnPitchBasePairs)) return emptyList()
    return (0 until periodBasePairs).filter {
        Math.floorMod(it + rowBasePairs / 2, columnPitchBasePairs) == 0
    }
}

/**
 * The width at which the **inboard** arm ceiling `pitch − width` and the **outboard** one
 * `edgeX/2 − pitch` cross, in nm — `edgeX = 2(2·pitch − width)`.
 *
 * Above it a three-arm row is bounded by `C-0069`'s plan budget and the tile edge is slack; below
 * it the tile edge binds and the budget is slack. It contains no arm length, so it is a property
 * of the lattice alone.
 */
fun armCeilingCrossoverWidth(
    rootPitch: Double,
    width: Double = OrigamiDuplex.INTERHELICAL
): Double {
    require(rootPitch > 0.0) { "rootPitch must be positive, was: $rootPitch" }
    require(width > 0.0) { "width must be positive, was: $width" }
    return 2.0 * (2.0 * rootPitch - width)
}

/**
 * The longest arm the **inboard** pair of a three-arm row admits — `C-0069`'s plan budget,
 * `pitch − d`, which carries no tile width at all.
 */
fun inboardArmCeiling(
    rootPitch: Double,
    width: Double = OrigamiDuplex.INTERHELICAL
): Double {
    require(rootPitch > 0.0) { "rootPitch must be positive, was: $rootPitch" }
    require(width > 0.0) { "width must be positive, was: $width" }
    return rootPitch - width
}

/**
 * The longest arm the **outboard** root of a row admits — `edgeX/2 − outermostSite`, which carries
 * no interhelical distance at all.
 */
fun outboardArmCeiling(outermostSite: Double, edgeX: Double): Double {
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(outermostSite >= 0.0) {
        "outermostSite must not be negative, was: $outermostSite"
    }
    return edgeX / 2.0 - outermostSite
}

// ------------------------------------------------------------------ the raster plane lattice

/** One crossover plane of the raster lattice, carrying whether it sits on the row end. */
data class RasterPlane(
    val planeIndex: Int,
    val x: Double,
    val atRowEnd: Boolean
)

/**
 * The 8 bp crossover planes of a sheet of edge [edgeX] at a column phase of [phaseBasePairs].
 *
 * With [admitRowEnd] `false` this is `junctionPlanes` exactly, to the last bit — the strictly
 * interior truncation `C-0015` and `C-0055` are written on. With [admitRowEnd] `true` a plane
 * lying **on** the row end is kept, inset by [inset] so it cannot seed a zero-length beam element;
 * 0.05 nm is 0.147 base pairs, i.e. below the resolution of the design language.
 */
fun rasterJunctionPlanes(
    phaseBasePairs: Int,
    edgeX: Double,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR,
    admitRowEnd: Boolean = false,
    inset: Double = CrossoverLayout.EDGE_MARGIN,
    tolerance: Double = 1.0e-9
): List<RasterPlane> {
    require(phaseBasePairs >= 0) { "phaseBasePairs must not be negative, was: $phaseBasePairs" }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(risePerBasePair > 0.0) {
        "risePerBasePair must be positive, was: $risePerBasePair"
    }
    require(inset > 0.0) { "inset must be positive, was: $inset" }
    val spacing = CROSSOVER_PLANE_BASE_PAIRS * risePerBasePair
    val phase = phaseBasePairs * risePerBasePair
    val half = edgeX / 2.0
    val bound = if (admitRowEnd) half else half - inset
    val first = ceil((-bound - phase) / spacing - tolerance).toInt()
    val last = floor((bound - phase) / spacing + tolerance).toInt()
    return (first..last).map { index ->
        val x = phase + index * spacing
        val onEnd = abs(abs(x) - half) <= tolerance
        RasterPlane(
            planeIndex = index,
            x = if (onEnd) (if (x < 0.0) -(half - inset) else half - inset) else x,
            atRowEnd = onEnd
        )
    }
}

/**
 * The sheet's own **columns** — the even-index planes — as a [CrossoverLayout].
 *
 * With [admitRowEnd] `false` this equals `CrossoverLayout.atBasePairPhase` to the last bit.
 */
fun rasterColumnLayout(
    phaseBasePairs: Int,
    sheet: OrigamiSheet,
    edgeX: Double,
    admitRowEnd: Boolean = false,
    inset: Double = CrossoverLayout.EDGE_MARGIN
): CrossoverLayout {
    // the sheet owns the pitch: `crossoverSpacing` is 32 bp, so the rise is a thirty-second of it
    val rise = sheet.crossoverSpacing / Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    val planes = rasterJunctionPlanes(phaseBasePairs, edgeX, rise, admitRowEnd, inset)
        .filter { it.planeIndex % 2 == 0 }
    require(planes.size >= 2) {
        "a lattice needs at least two crossover columns at phase $phaseBasePairs on $edgeX nm, " +
                "and it has ${planes.size}"
    }
    return CrossoverLayout(
        positions = planes.map { it.x },
        parities = planes.map { Math.floorMod(it.planeIndex / 2, 2) }
    )
}

/** [junctionSites]'s construction over the raster plane lattice, with the row-end convention. */
fun rasterJunctionSites(
    phaseBasePairs: Int,
    edgeX: Double,
    duplexes: Int,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR,
    admitRowEnd: Boolean = false,
    inset: Double = CrossoverLayout.EDGE_MARGIN
): List<T119JunctionSite> {
    require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
    val planes = rasterJunctionPlanes(phaseBasePairs, edgeX, risePerBasePair, admitRowEnd, inset)
    val azimuths = CrossoverAzimuth.entries
    return (0 until duplexes).flatMap { duplex ->
        planes.map { plane ->
            T119JunctionSite(
                duplex = duplex,
                planeIndex = plane.planeIndex,
                x = plane.x,
                azimuth = azimuths[
                    Math.floorMod(plane.planeIndex - 2 * duplex, CROSSOVER_PLANES_PER_PERIOD)
                ]
            )
        }
    }
}

/** [junctionSiteInventory] over [rasterJunctionSites]. */
fun rasterSiteInventory(
    phaseBasePairs: Int,
    edgeX: Double,
    duplexes: Int,
    admitRowEnd: Boolean = false,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR,
    inset: Double = CrossoverLayout.EDGE_MARGIN
): T119SiteInventory {
    val sites = rasterJunctionSites(
        phaseBasePairs, edgeX, duplexes, risePerBasePair, admitRowEnd, inset
    )
    val last = duplexes - 1
    return T119SiteInventory(
        phaseBasePairs = phaseBasePairs,
        duplexes = duplexes,
        planes = rasterJunctionPlanes(
            phaseBasePairs, edgeX, risePerBasePair, admitRowEnd, inset
        ).size,
        interfaceSites = sites.count {
            it.azimuth == CrossoverAzimuth.NORTH && it.duplex < last
        },
        outwardFacingSites = sites.count {
            (it.azimuth == CrossoverAzimuth.NORTH && it.duplex == last) ||
                    (it.azimuth == CrossoverAzimuth.SOUTH && it.duplex == 0)
        },
        upwardSites = sites.count { it.azimuth == CrossoverAzimuth.EAST },
        downwardSites = sites.count { it.azimuth == CrossoverAzimuth.WEST }
    )
}

/** The upward (`EAST`) site positions of every row, ascending — `upwardRootLattice`'s shape. */
fun rasterUpwardSites(
    phaseBasePairs: Int,
    edgeX: Double,
    duplexes: Int,
    admitRowEnd: Boolean = false,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR,
    inset: Double = CrossoverLayout.EDGE_MARGIN
): List<List<Double>> {
    val sites = rasterJunctionSites(
        phaseBasePairs, edgeX, duplexes, risePerBasePair, admitRowEnd, inset
    ).filter { it.azimuth == CrossoverAzimuth.EAST }
    return (0 until duplexes).map { row ->
        sites.filter { it.duplex == row }.map { it.x }.sorted()
    }
}

// ------------------------------------------------------------------ the edge clearance

/**
 * Every feasible direction assignment of the arms rooted at [roots], under `C-0053`'s footprint
 * convention — an arm occupies `[low, high]`, the next may start at `high + width`, and no arm may
 * leave `[−edgeX/2, edgeX/2]`.
 *
 * `armDirections` returns the *first* such assignment (`+x` first, so that a placement admitted
 * there is the one a greedy scheduler produces); this enumerates them all, which is what a
 * clearance — a maximum over assignments — needs.
 */
fun armAssignments(
    roots: List<Double>,
    arm: Double,
    edgeX: Double,
    width: Double = OrigamiDuplex.INTERHELICAL
): List<List<Boolean>> {
    require(roots.isNotEmpty()) { "roots must not be empty" }
    require(arm > 0.0) { "arm must be positive, was: $arm" }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(width > 0.0) { "width must be positive, was: $width" }
    require(roots.sorted() == roots) { "roots must ascend, were: $roots" }
    val half = edgeX / 2.0
    val found = ArrayList<List<Boolean>>()
    fun search(index: Int, frontier: Double, taken: List<Boolean>) {
        if (index == roots.size) {
            found += taken
            return
        }
        for (toward in listOf(true, false)) {
            val low = if (toward) roots[index] else roots[index] - arm
            val high = if (toward) roots[index] + arm else roots[index]
            if (low < -half - PLAN_TANGENCY_TOLERANCE) continue
            if (high > half + PLAN_TANGENCY_TOLERANCE) continue
            if (low < frontier - PLAN_TANGENCY_TOLERANCE) continue
            search(index + 1, high + width, taken + toward)
        }
    }
    search(0, Double.NEGATIVE_INFINITY, emptyList())
    return found
}

/**
 * The largest clearance to the tile edge any feasible assignment of [roots] attains, in nm, or
 * `null` where none is feasible.
 *
 * The clearance of one assignment is the **minimum** over its arms of the distance from an arm end
 * to the nearer tile edge; the returned value is the **maximum** of that over the assignments, so
 * it is the room a designer who is free to choose the senses actually has.
 */
fun bestEdgeClearance(
    roots: List<Double>,
    arm: Double,
    edgeX: Double,
    width: Double = OrigamiDuplex.INTERHELICAL
): Double? {
    val half = edgeX / 2.0
    return armAssignments(roots, arm, edgeX, width).maxOfOrNull { senses ->
        roots.zip(senses).minOf { (root, toward) ->
            val low = if (toward) root else root - arm
            val high = if (toward) root + arm else root
            minOf(half - high, low + half)
        }
    }
}

/**
 * The largest clearance any [count]-arm subset of [sites] attains, in nm, or `null` where the row
 * cannot carry [count] arms at all.
 */
fun rowEdgeClearance(
    sites: List<Double>,
    arm: Double,
    count: Int,
    edgeX: Double,
    width: Double = OrigamiDuplex.INTERHELICAL
): Double? {
    require(count >= 1) { "count must be at least one, was: $count" }
    if (count > sites.size) return null
    val ascending = sites.sorted()
    var best: Double? = null
    fun build(start: Int, taken: List<Double>) {
        if (taken.size == count) {
            val clearance = bestEdgeClearance(taken, arm, edgeX, width) ?: return
            val current = best
            if (current == null || clearance > current) best = clearance
            return
        }
        for (index in start until ascending.size) build(index + 1, taken + ascending[index])
    }
    build(0, emptyList())
    return best
}
