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
import kotlin.math.abs
import kotlin.math.roundToLong

/**
 * The junction sites a square-lattice helix offers, and the ones a **single-layer** sheet leaves
 * unoccupied — `T-119`, the falsifier `C-0054` names against its own hinge budget.
 *
 * ## The published rule this is built on
 *
 * Ke, Douglas, Liedl and Shih (*JACS* **131**:15903, 2009), read directly:
 *
 * > *"In the square lattice, each double helix has up to four nearest neighbors and is designed to
 * > link to each with antiparallel strand crossovers. … Every 8 bp, the staple strand of a given
 * > double helix completes a rotation of 8 bp/(10.67 bp/turn) = 0.75 turns. Thus every 8 bp, that
 * > staple strand is positioned to cross over to one of its four neighbors; that is, starting from
 * > 0 bp as "north", then moving away from the viewer by 8 bp gives a clockwise rotation of 0.75
 * > turns to "west", moving 16 bp away gives a rotation of 1.5 turns to "south", moving 24 bp away
 * > gives a rotation of 2.25 turns to "east", and moving 32 bp away gives a rotation of 3.0 turns
 * > back to "north". Thus adjacent helices share crossovers every 32 bp, and the positions of the
 * > crossovers are restricted to periodic intersection or "crossover" planes, labeled from i to iv,
 * > spaced at 8 bp intervals."*
 *
 * and, of the same lattice's four planes:
 *
 * > *"The crossovers in i and iii sectional slices are parallel to the xz-plane, while the
 * > crossovers in ii and iv sectional slices are parallel to the yz-plane."*
 *
 * A **single-layer** sheet is one row of that lattice, so it can occupy only the two azimuths that
 * point at its in-plane neighbours. **The other two point out of the sheet plane and are empty** —
 * and they are the whole of `T-119`.
 *
 * ## Why this is not a restatement of [CrossoverLayout]
 *
 * `C-0015`'s lattice is the **column** lattice: the 16 bp pitch at which a helix crosses over at
 * all, with the two parities alternating between its two neighbours. That construction is a
 * complete description of what the sheet *builds* and says nothing about what the lattice *offers*.
 * This one is built from the azimuth instead — an angle per base pair — and the in-plane half of it
 * is then **required** to reproduce `CrossoverLayout` exactly, at every one of the 32 phases. Two
 * constructions, one already published, agreeing on the used half is what makes the count of the
 * unused half worth quoting.
 *
 * ## Conventions
 *
 * `x` runs along the helices, `y` across them in the sheet plane, `z` normal to the sheet and
 * **positive upward** — away from the grafted layer, which lies below the tile. [CrossoverAzimuth]
 * `NORTH`/`SOUTH` are the in-plane neighbours `±y`; `EAST`/`WEST` are `+z`/`−z`.
 */

/** The square lattice's designed twist, `32 bp per 3 turns` — Ke et al. (2009), **CITED**. */
const val SQUARE_LATTICE_BASE_PAIRS_PER_TURN: Double = 32.0 / 3.0

/** The same as an angle per base pair: **33.75°**, quoted verbatim by Ke et al. (2009). */
const val SQUARE_LATTICE_DEGREES_PER_BASE_PAIR: Double =
    360.0 / SQUARE_LATTICE_BASE_PAIRS_PER_TURN

/**
 * B-DNA's **preferred** twist, `10.5 bp/turn` — Ke et al.'s own comparison value, against which the
 * square lattice is underwound and from which a global right-handed twist follows.
 */
const val PREFERRED_BASE_PAIRS_PER_TURN: Double = 10.5

/** The crossover-plane spacing of the square lattice, in base pairs. */
const val CROSSOVER_PLANE_BASE_PAIRS: Int = 8

/** The number of crossover planes in one period, one per neighbour of the square lattice. */
const val CROSSOVER_PLANES_PER_PERIOD: Int = 4

/**
 * The M13mp18 scaffold, **7249 nt** — Rothemund, *Nature* **440**:297 (2006), **CITED**.
 */
const val M13_SCAFFOLD_NUCLEOTIDES: Long = 7249L

/**
 * The four crossover azimuths of a square-lattice helix, in Ke et al.'s own compass naming, at the
 * base-pair offsets they name.
 *
 * @param basePairOffset the offset from the `NORTH` reference, in base pairs.
 * @param outOfPlane whether the azimuth points out of a **single-layer** sheet's plane.
 */
enum class CrossoverAzimuth(
    val basePairOffset: Int,
    val outOfPlane: Boolean
) {

    /** `+y` — an in-plane neighbour. */
    NORTH(0, false),

    /** `−z` — **unoccupied** by a single-layer sheet, and pointing at the grafted layer. */
    WEST(8, true),

    /** `−y` — an in-plane neighbour. */
    SOUTH(16, false),

    /** `+z` — **unoccupied** by a single-layer sheet, and pointing away from the layer. */
    EAST(24, true);

    /**
     * The azimuth the design geometry puts this site at, in `[0, 360)` degrees, measured from
     * `NORTH`.
     *
     * Exactly `0`, `270`, `180`, `90` — a quarter turn apart — because `8 × 33.75 = 270` is exact.
     * That exactness is the whole of `T-119`'s cheap bound.
     */
    val designAzimuthDegrees: Double
        get() {
            val turned = basePairOffset * SQUARE_LATTICE_DEGREES_PER_BASE_PAIR
            return turned - 360.0 * kotlin.math.floor(turned / 360.0)
        }
}

/** The azimuth a strand's backbone has advanced to after [basePairs], in degrees, unwrapped. */
fun azimuthDegrees(
    basePairs: Double,
    basePairsPerTurn: Double = SQUARE_LATTICE_BASE_PAIRS_PER_TURN
): Double {
    require(basePairsPerTurn > 0.0) {
        "basePairsPerTurn must be positive, was: $basePairsPerTurn"
    }
    return 360.0 * basePairs / basePairsPerTurn
}

/**
 * How far off its designed azimuth a site [offsetBasePairs] from a reference crossover sits, if the
 * duplex adopts its **preferred** twist rather than the lattice's designed one.
 *
 * `|Δφ| = 360 · n · (1/preferred − 1/design)`, **linear in the offset** — which is the whole
 * argument: the unoccupied out-of-plane site is 8 bp from the sheet's own crossover and the next
 * in-plane one is 16 bp away, so the unused site is off-register by **half** what the used one is.
 */
fun registerDeparture(
    offsetBasePairs: Int,
    designBasePairsPerTurn: Double = SQUARE_LATTICE_BASE_PAIRS_PER_TURN,
    preferredBasePairsPerTurn: Double = PREFERRED_BASE_PAIRS_PER_TURN
): Double {
    require(offsetBasePairs >= 0) {
        "offsetBasePairs must not be negative, was: $offsetBasePairs"
    }
    return abs(
        azimuthDegrees(offsetBasePairs.toDouble(), preferredBasePairsPerTurn) -
                azimuthDegrees(offsetBasePairs.toDouble(), designBasePairsPerTurn)
    )
}

/** One junction site of the lattice: a base pair of one duplex facing one neighbour position. */
data class T119JunctionSite(
    val duplex: Int,
    val planeIndex: Int,
    val x: Double,
    val azimuth: CrossoverAzimuth
)

/**
 * The crossover planes of a sheet of edge [edgeX] at a column phase of [phaseBasePairs], as `x`
 * positions in nm — the 8 bp lattice, of which the sheet's own 16 bp column lattice is every other
 * member.
 *
 * Built through [CrossoverLayout.phased] so that the footprint truncation is `C-0015`'s own, to the
 * last digit, and a plane can never be admitted here that a column would not be.
 */
fun junctionPlanes(
    phaseBasePairs: Int,
    edgeX: Double,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR
): List<Double> {
    require(phaseBasePairs >= 0) {
        "phaseBasePairs must not be negative, was: $phaseBasePairs"
    }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    return CrossoverLayout.phased(
        phase = phaseBasePairs * risePerBasePair,
        columnSpacing = CROSSOVER_PLANE_BASE_PAIRS * risePerBasePair,
        lengthX = edgeX
    ).positions
}

/**
 * Every junction site of a [duplexes]-duplex single-layer sheet of edge [edgeX] at a column phase
 * of [phaseBasePairs] — **all four azimuths**, whether the sheet occupies them or not.
 *
 * Adjacent duplexes are offset by 16 bp — a crossover between duplex `b` and `b+1` needs the first
 * facing `NORTH` and the second facing `SOUTH` at the same plane — so duplex `b`'s azimuth at plane
 * `k` is entry `(k − 2b) mod 4`. That offset is not imposed: it is the only one that lets an
 * interface exist at all, and `C-0015`'s parity rule is its consequence.
 */
fun junctionSites(
    phaseBasePairs: Int,
    edgeX: Double,
    duplexes: Int,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR
): List<T119JunctionSite> {
    require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
    val planes = junctionPlanes(phaseBasePairs, edgeX, risePerBasePair)
    val spacing = CROSSOVER_PLANE_BASE_PAIRS * risePerBasePair
    val phase = phaseBasePairs * risePerBasePair
    val azimuths = CrossoverAzimuth.entries
    return (0 until duplexes).flatMap { duplex ->
        planes.map { x ->
            val plane = Math.round((x - phase) / spacing).toInt()
            T119JunctionSite(
                duplex = duplex,
                planeIndex = plane,
                x = x,
                azimuth = azimuths[Math.floorMod(plane - 2 * duplex, CROSSOVER_PLANES_PER_PERIOD)]
            )
        }
    }
}

/**
 * The census of a sheet's junction sites: what it uses, and what its own lattice leaves empty.
 *
 * @param interfaceSites the in-plane crossovers the sheet **builds** — `C-0015`'s 56 or 49.
 * @param outwardFacingSites in-plane azimuths of the two edge duplexes that face off the sheet and
 *          therefore have no partner; unoccupied, and not usable for a hinge on the tile.
 * @param upwardSites `EAST` sites, one duplex each, on the side **away** from the grafted layer.
 * @param downwardSites `WEST` sites, which point into the layer.
 */
data class T119SiteInventory(
    val phaseBasePairs: Int,
    val duplexes: Int,
    val planes: Int,
    val interfaceSites: Int,
    val outwardFacingSites: Int,
    val upwardSites: Int,
    val downwardSites: Int
) {

    /** Every site the lattice offers, counting a shared in-plane site once. */
    val totalSites: Int get() =
        interfaceSites + outwardFacingSites + upwardSites + downwardSites

    /** Every site the sheet leaves empty. */
    val unusedSites: Int get() = totalSites - interfaceSites

    /** The fraction of its own lattice the sheet occupies. */
    val usedFraction: Double get() = interfaceSites.toDouble() / totalSites

    /**
     * `C-0054`'s ceiling — the in-plane crossovers that may be spent as hinges while every one of
     * the `duplexes − 1` interfaces keeps at least one.
     */
    val inPlaneHingeCeiling: Int get() = interfaceSites - (duplexes - 1)

    /** The hinges the **unoccupied upward** azimuth supplies at no cost to the sheet at all. */
    val outOfPlaneHingeCeiling: Int get() = upwardSites
}

/** The census of [junctionSites], reduced. */
fun junctionSiteInventory(
    phaseBasePairs: Int,
    edgeX: Double,
    duplexes: Int,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR
): T119SiteInventory {
    val sites = junctionSites(phaseBasePairs, edgeX, duplexes, risePerBasePair)
    val last = duplexes - 1
    return T119SiteInventory(
        phaseBasePairs = phaseBasePairs,
        duplexes = duplexes,
        planes = junctionPlanes(phaseBasePairs, edgeX, risePerBasePair).size,
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

/**
 * The **upward** junction sites, as [HingeSite]s, so that `C-0053`'s exact per-row interval
 * scheduler can be re-run on them unchanged.
 *
 * `HingeSite.interfaceIndex` carries the **duplex** here rather than an interface, and that is the
 * whole structural difference: an in-plane crossover belongs to *two* rows and only one of them may
 * root an arm on it, while an upward site belongs to **one**. The same fact both removes the
 * connectivity cost and **halves the root density** — an upward line has the 32 bp pitch of one
 * interface, where an interior row sees two interfaces at 16 bp.
 */
fun upwardHingeSites(
    phaseBasePairs: Int,
    edgeX: Double,
    duplexes: Int,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR
): List<HingeSite> = junctionSites(phaseBasePairs, edgeX, duplexes, risePerBasePair)
    .filter { it.azimuth == CrossoverAzimuth.EAST }
    .map { HingeSite(it.duplex, it.x, Math.floorMod(it.planeIndex, 2)) }
    .sortedWith(compareBy({ it.interfaceIndex }, { it.x }))

/**
 * Places as many arms of [arm] as the **upward** lattice admits, one distinct site each.
 *
 * No site is shared between two rows, so the per-row schedules are independent and the greedy
 * construction meets the independent per-row bound **identically** — which is asserted rather than
 * assumed, and is the cleanest statement of what changes against `C-0053`'s in-plane placement.
 */
fun placeUpwardArms(
    phaseBasePairs: Int,
    edgeX: Double,
    duplexes: Int,
    arm: Double,
    width: Double = OrigamiDuplex.INTERHELICAL,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR
): HingeArmPlacement {
    require(arm > 0.0) { "arm must be positive, was: $arm" }
    val sites = upwardHingeSites(phaseBasePairs, edgeX, duplexes, risePerBasePair)
    val perRow = (0 until duplexes).map { row ->
        maximumArmsInRow(sites.filter { it.interfaceIndex == row }, arm, edgeX, width, row)
    }
    return HingeArmPlacement(
        phaseBasePairs = phaseBasePairs,
        placements = perRow.flatten().sortedWith(compareBy({ it.row }, { it.low })),
        independentRowBound = perRow.sumOf { it.size }
    )
}

/**
 * The largest path count whose own placed arm still places, over all 32 phases — the fixed point
 * `C-0041` and `C-0053` both solve, on the upward lattice.
 *
 * @param armFor the placed arm at a given path count, `C-0039`'s elastica in the study.
 */
fun selfConsistentUpwardArmCount(
    edgeX: Double,
    duplexes: Int,
    maximumCount: Int = 60,
    minimumCount: Int = 1,
    width: Double = OrigamiDuplex.INTERHELICAL,
    armFor: (Int) -> Double
): Int {
    require(maximumCount > 0) { "maximumCount must be positive, was: $maximumCount" }
    require(minimumCount in 1..maximumCount) {
        "minimumCount must lie in 1..$maximumCount, was: $minimumCount"
    }
    // the predicate is monotone — the placed arm grows with the count and the arms the lattice
    // carries can only fall — so the scan runs downward and the smallest counts, at which
    // `C-0039`'s elastica has no arm short enough to reach the mandate at all, are never reached
    return (maximumCount downTo minimumCount).firstOrNull { count ->
        // the arm is solved ONCE per count and not once per phase — `armFor` is a nested root
        // solve over an RK integration, and calling it inside the phase sweep costs 32x
        val arm = armFor(count)
        (0 until 32).maxOf { placeUpwardArms(it, edgeX, duplexes, arm, width).arms } >= count
    } ?: 0
}

/**
 * What an array of [hinges] rooted on the **upward** azimuth costs the host sheet.
 *
 * Nothing: the arms are added above the sheet rather than cut out of it, so no interface crossover
 * is consumed, nothing is buried under an arm on its own interface, and the residual sheet is the
 * sheet. The type exists so that the statement is **computed on the same inventory** `C-0054`'s
 * pigeonhole is computed on, rather than asserted in prose.
 */
data class T119HingeBudget(
    val phaseBasePairs: Int,
    val duplexes: Int,
    val hinges: Int,
    val sheetInventory: Int,
    val retainedInterfaceCrossovers: Int,
    val emptyInterfaces: Int,
    val components: Int,
    val inPlaneCeiling: Int,
    val outOfPlaneCeiling: Int
)

/** [T119HingeBudget] for [hinges] arms on the upward lattice. */
fun outOfPlaneHingeBudget(
    phaseBasePairs: Int,
    edgeX: Double,
    duplexes: Int,
    hinges: Int,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR
): T119HingeBudget {
    require(hinges >= 0) { "hinges must not be negative, was: $hinges" }
    val inventory = junctionSiteInventory(phaseBasePairs, edgeX, duplexes, risePerBasePair)
    require(hinges <= inventory.upwardSites) {
        "hinges must not exceed the upward inventory ${inventory.upwardSites}, was: $hinges"
    }
    return T119HingeBudget(
        phaseBasePairs = phaseBasePairs,
        duplexes = duplexes,
        hinges = hinges,
        sheetInventory = inventory.interfaceSites,
        retainedInterfaceCrossovers = inventory.interfaceSites,
        emptyInterfaces = 0,
        components = 1,
        inPlaneCeiling = inventory.inPlaneHingeCeiling,
        outOfPlaneCeiling = inventory.outOfPlaneHingeCeiling
    )
}

/**
 * The **8 bp staple domains** an upward hinge creates.
 *
 * A single-layer sheet's staple crosses over every 16 bp; an upward crossover sits exactly 8 bp from
 * its neighbours on the same helix, so building one splits a 16 bp domain into **two of 8**. Ke et
 * al. report of precisely this pattern that *"some staple breaks must be implemented between
 * crossovers 8 bp apart … Introducing these breaks may be destabilizing for the structure"*, and
 * that omitting them raised the folding yield of their 8 × 8 block. **This is the published cost of
 * the escape, and it is a yield cost, not a geometric one.**
 */
fun eightBasePairDomains(hinges: Int): Int {
    require(hinges >= 0) { "hinges must not be negative, was: $hinges" }
    return 2 * hinges
}

/** The scaffold a [duplexes]-duplex sheet of edge [edgeX] consumes, in base pairs. */
fun scaffoldBasePairs(
    duplexes: Int,
    edgeX: Double,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR
): Long {
    require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    return duplexes * (edgeX / risePerBasePair).roundToLong()
}

/** The scaffold [arms] arms of length [arm] add, in base pairs. */
fun armScaffoldBasePairs(
    arms: Int,
    arm: Double,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR
): Long {
    require(arms >= 0) { "arms must not be negative, was: $arms" }
    require(arm > 0.0) { "arm must be positive, was: $arm" }
    return arms * (arm / risePerBasePair).roundToLong()
}
