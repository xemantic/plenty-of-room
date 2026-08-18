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
import kotlin.math.pow

/**
 * `T-171` — the crossover phase is **one integer** and three standing claims want different values
 * of it.
 *
 * ## The conflict, in one table
 *
 * `CLAUDE.md` records that *a placement's own phase and its HOST's phase are ONE variable*: the
 * same 8 bp plane lattice carries the sheet's own crossover columns (the planes at even index) and
 * the coupling's upward stations (the planes at `k ≡ 2r + 3 (mod 4)`). `C-0098`'s 32-phase census
 * turns that into a conflict — the phases that maximise the **upward inventory**, the phases that
 * carry an **eight-column host** (`C-0015`) and the phases that admit a **centro-symmetric** root
 * lattice (`C-0063`) do not coincide.
 *
 * Every one of the three demands is a **census over a single quantised integer**, so the whole
 * comparison is a table with no solve in it. That is what this file computes, and it computes it at
 * both widths the programme carries — §3's nominal 40.00 nm and `C-0086`'s buildable 38.08 nm —
 * because `C-0090` has shown that the width **selects** the phase.
 *
 * ## What is new here beside the census
 *
 * Nobody has priced a **seven-column host on the sheet side**. Three of the four channels are
 * closed forms and live here:
 *
 * 1. the **per-interface** crossover counts, which a seven-column sheet splits 4/3 between the two
 *    parities and an eight-column one splits 4/4 — the input `C-0054`'s two `D_⊥` readings need,
 *    and the reason they disagree;
 * 2. [severanceProbability], the chance that `C-0087`'s measured staple dropout empties **some**
 *    interface, which is `(1 − p)³` on a three-crossover interface against `(1 − p)⁴` on a
 *    four-crossover one — and an empty interface takes the series `D_⊥` to exactly zero;
 * 3. [rowEndUpwardStations], which is what admitting the row-end crossover is worth in **stations**
 *    rather than in columns.
 *
 * ## Conventions, restated rather than inherited
 *
 * Lengths **nm**; `x` runs **along** the helices, `y` **across** them, `z` normal and positive
 * **upward** — away from the grafted layer, which lies below the tile. The phase is quantised to
 * base pairs and its period is **32, not 16** (`C-0015`).
 */

/** The number of distinct crossover phases — `C-0015`'s period, 32 base pairs. */
const val CROSSOVER_PHASE_COUNT: Int = 32

/**
 * One phase of the 8 bp plane lattice, censused — a row of counts and two booleans, and **no
 * solve**.
 *
 * @param phaseBasePairs the phase, in base pairs.
 * @param columns the sheet's own crossover columns inside the footprint.
 * @param interfaceCrossovers the total number of in-plane crossovers on the `D − 1` interfaces.
 * @param crossoversPerInterface how those are distributed, in interface order — the input the
 *          **series** reading of `D_⊥` needs, and the one a total cannot supply.
 * @param upwardSites the unoccupied `EAST` azimuth's inventory, `C-0055`'s upward stations.
 * @param centroSymmetric whether the upward root lattice is invariant under `(x, y) → (−x, −y)`.
 * @param columnOnRowEnd whether a **column** sits exactly on the row end at this phase.
 * @param planeOnRowEnd whether any **plane** does — which is a weaker condition, and the whole of
 *          `CH-0118`: at a phase where the end plane's index is *odd* it is not a column at all,
 *          it is an upward station.
 */
data class CrossoverPhaseRow(
    val phaseBasePairs: Int,
    val columns: Int,
    val interfaceCrossovers: Int,
    val crossoversPerInterface: List<Int>,
    val upwardSites: Int,
    val centroSymmetric: Boolean,
    val columnOnRowEnd: Boolean,
    val planeOnRowEnd: Boolean
)

/**
 * The three demands the corpus places on the one phase variable, and every pairwise intersection.
 *
 * Emitted rather than asserted: `C-0098` states the disjointness at 40.00 nm in prose, and the
 * whole question `T-171` exists to answer is whether it survives at the buildable width.
 */
data class PhaseDemandLedger(
    val maximumUpwardSites: Int,
    val maximumColumns: Int,
    val richestUpwardInventory: List<Int>,
    val eightColumnHost: List<Int>,
    val centroSymmetric: List<Int>,
    val richestAndColumns: List<Int>,
    val richestAndSymmetry: List<Int>,
    val columnsAndSymmetry: List<Int>,
    val allThree: List<Int>
)

/**
 * How many in-plane crossovers each of the `duplexes − 1` interfaces carries at this phase, in
 * interface order.
 *
 * Counted from `C-0055`'s own azimuth construction — an interface crossover is a `NORTH` site of
 * the lower duplex of the pair — rather than from the column parities, so that the two independent
 * readings of the same lattice can be asserted equal.
 */
fun crossoversPerInterface(
    phaseBasePairs: Int,
    edgeX: Double,
    duplexes: Int,
    admitRowEnd: Boolean = false,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR,
    inset: Double = CrossoverLayout.EDGE_MARGIN
): List<Int> {
    require(phaseBasePairs >= 0) { "phaseBasePairs must not be negative, was: $phaseBasePairs" }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
    val sites = rasterJunctionSites(
        phaseBasePairs, edgeX, duplexes, risePerBasePair, admitRowEnd, inset
    ).filter { it.azimuth == CrossoverAzimuth.NORTH && it.duplex < duplexes - 1 }
    return (0 until duplexes - 1).map { interfaceIndex ->
        sites.count { it.duplex == interfaceIndex }
    }
}

/**
 * Whether the upward root lattice at this phase is invariant under `(x, y) → (−x, −y)`.
 *
 * The same condition [centroSymmetricUpwardPhases] applies, evaluated at one phase and on the
 * **row-end-aware** lattice, so that a station admitted by the end-of-row convention cannot be
 * waved through a symmetry it does not have.
 */
fun isCentroSymmetricUpwardLattice(
    phaseBasePairs: Int,
    edgeX: Double,
    duplexes: Int,
    admitRowEnd: Boolean = false,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR,
    inset: Double = CrossoverLayout.EDGE_MARGIN,
    tolerance: Double = 1e-9
): Boolean {
    val lattice = rasterUpwardSites(
        phaseBasePairs, edgeX, duplexes, admitRowEnd, risePerBasePair, inset
    )
    return (0 until duplexes).all { row ->
        val mine = lattice[row]
        val partner = lattice[duplexes - 1 - row].map { -it }.sorted()
        mine.size == partner.size && mine.zip(partner).all { (a, b) -> abs(a - b) <= tolerance }
    }
}

/** Whether any crossover **plane** sits exactly on the row end at this phase. */
fun planeOnRowEnd(
    phaseBasePairs: Int,
    edgeX: Double,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR
): Boolean = rasterJunctionPlanes(
    phaseBasePairs, edgeX, risePerBasePair, admitRowEnd = true
).any { it.atRowEnd }

/** The census row of one phase. */
fun crossoverPhaseRow(
    phaseBasePairs: Int,
    edgeX: Double,
    duplexes: Int,
    sheet: OrigamiSheet,
    admitRowEnd: Boolean = false,
    inset: Double = CrossoverLayout.EDGE_MARGIN
): CrossoverPhaseRow {
    require(phaseBasePairs >= 0) { "phaseBasePairs must not be negative, was: $phaseBasePairs" }
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
    val rise = sheet.crossoverSpacing / Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    val perInterface = crossoversPerInterface(
        phaseBasePairs, edgeX, duplexes, admitRowEnd, rise, inset
    )
    val layout = rasterColumnLayout(phaseBasePairs, sheet, edgeX, admitRowEnd, inset)
    val planes = rasterJunctionPlanes(phaseBasePairs, edgeX, rise, admitRowEnd, inset)
    return CrossoverPhaseRow(
        phaseBasePairs = phaseBasePairs,
        columns = layout.size,
        interfaceCrossovers = perInterface.sum(),
        crossoversPerInterface = perInterface,
        upwardSites = rasterSiteInventory(
            phaseBasePairs, edgeX, duplexes, admitRowEnd, rise, inset
        ).upwardSites,
        centroSymmetric = isCentroSymmetricUpwardLattice(
            phaseBasePairs, edgeX, duplexes, admitRowEnd, rise, inset
        ),
        columnOnRowEnd = planes.any { it.atRowEnd && it.planeIndex % 2 == 0 },
        planeOnRowEnd = planes.any { it.atRowEnd }
    )
}

/** The complete census over the phase — 32 rows, one integer, no solve. */
fun crossoverPhaseCensus(
    edgeX: Double,
    duplexes: Int,
    sheet: OrigamiSheet,
    admitRowEnd: Boolean = false,
    inset: Double = CrossoverLayout.EDGE_MARGIN
): List<CrossoverPhaseRow> = (0 until CROSSOVER_PHASE_COUNT).map {
    crossoverPhaseRow(it, edgeX, duplexes, sheet, admitRowEnd, inset)
}

/** The three demands and their intersections, over a [crossoverPhaseCensus]. */
fun phaseDemandLedger(census: List<CrossoverPhaseRow>): PhaseDemandLedger {
    require(census.isNotEmpty()) { "a census must not be empty" }
    val maximumUpward = census.maxOf { it.upwardSites }
    val maximumColumns = census.maxOf { it.columns }
    val richest = census.filter { it.upwardSites == maximumUpward }.map { it.phaseBasePairs }
    val columns = census.filter { it.columns == maximumColumns }.map { it.phaseBasePairs }
    val symmetric = census.filter { it.centroSymmetric }.map { it.phaseBasePairs }
    return PhaseDemandLedger(
        maximumUpwardSites = maximumUpward,
        maximumColumns = maximumColumns,
        richestUpwardInventory = richest,
        eightColumnHost = columns,
        centroSymmetric = symmetric,
        richestAndColumns = richest.filter { it in columns },
        richestAndSymmetry = richest.filter { it in symmetric },
        columnsAndSymmetry = columns.filter { it in symmetric },
        allThree = richest.filter { it in columns && it in symmetric }
    )
}

// -------------------------------------------------------------------- what the row end is worth

/**
 * How many **upward stations** admitting the row-end crossover adds at this phase.
 *
 * `C-0090` states that *"an end plane has an even index, and the upward azimuth needs
 * `k ≡ 2b + 3 (mod 4)`, which is odd — the row-end crossover can never be an upward site, at any
 * phase"*. The first clause is a property of the phases `C-0090` examined and not of the lattice:
 * a plane lands on the row end whenever `phase ≡ −rowBasePairs/2 (mod 8)`, and its **index** is
 * even only when that congruence also holds modulo 16. At 112 bp that is phases 8 and 24; at
 * phases 0 and 16 the end plane's index is **odd**, so it is not a column at all and it **is** an
 * upward station on every row whose azimuth it matches.
 *
 * Computed as a difference of two inventories rather than by re-deriving the azimuth rule, so that
 * it cannot drift from `C-0055`'s own construction.
 */
fun rowEndUpwardStations(
    phaseBasePairs: Int,
    edgeX: Double,
    duplexes: Int,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR,
    inset: Double = CrossoverLayout.EDGE_MARGIN
): Int {
    require(phaseBasePairs >= 0) { "phaseBasePairs must not be negative, was: $phaseBasePairs" }
    fun inventory(admit: Boolean) = rasterSiteInventory(
        phaseBasePairs, edgeX, duplexes, admit, risePerBasePair, inset
    ).upwardSites
    return inventory(true) - inventory(false)
}

/** How many **columns** admitting the row-end crossover adds at this phase. */
fun rowEndColumns(
    phaseBasePairs: Int,
    edgeX: Double,
    duplexes: Int,
    sheet: OrigamiSheet,
    inset: Double = CrossoverLayout.EDGE_MARGIN
): Int {
    require(phaseBasePairs >= 0) { "phaseBasePairs must not be negative, was: $phaseBasePairs" }
    require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
    return rasterColumnLayout(phaseBasePairs, sheet, edgeX, true, inset).size -
            rasterColumnLayout(phaseBasePairs, sheet, edgeX, false, inset).size
}

// ------------------------------------------------------------------------- the severance bound

/**
 * The probability that **some** interface of the sheet loses every one of its crossovers, under
 * independent Bernoulli incorporation at rate [incorporation].
 *
 * &nbsp;&nbsp;&nbsp;&nbsp;`P = 1 − Π_i (1 − (1 − p)^{n_i})`
 *
 * A crossover is a staple, and `C-0087` reads Strauss et al.'s measured incorporation map at
 * single-staple resolution: **48 % at the edges to 95 % in the centre, mean 84 %**. `C-0054`'s
 * theorem is that a connected sheet needs one retained crossover on **each** of its `D − 1`
 * interfaces, and `C-0031`'s that the **series** `D_⊥` of a sheet with one empty interface is
 * exactly zero — so this is not a small correction to a rigidity, it is the probability that the
 * across-helix rigidity is not there at all.
 *
 * An interface with no crossovers at all is severed with probability one, which is what the
 * `n_i = 0` term gives.
 */
fun severanceProbability(
    crossoversPerInterface: List<Int>,
    incorporation: Double
): Double {
    require(crossoversPerInterface.isNotEmpty()) {
        "crossoversPerInterface must not be empty"
    }
    require(crossoversPerInterface.all { it >= 0 }) {
        "a crossover count must not be negative, were: $crossoversPerInterface"
    }
    require(incorporation in 0.0..1.0) {
        "incorporation must lie in [0, 1], was: $incorporation"
    }
    val missing = 1.0 - incorporation
    val intact = crossoversPerInterface.fold(1.0) { product, count ->
        product * (1.0 - missing.pow(count))
    }
    return 1.0 - intact
}

// ------------------------------------------------------------------------- placements from keys

/**
 * The placement a published [UpwardArmPlacement.key] names, snapped onto [sites].
 *
 * A key carries its root positions rounded to `1e-6 nm` — enough to identify a placement and **not**
 * enough to index an influence bank, whose station lookup is exact to `1e-9 nm`. Each parsed value
 * is therefore snapped to the nearest site of its own row and the departure is required to be below
 * half the key's own quantum, so a key that names a station this lattice does not have throws
 * rather than silently landing on a neighbour.
 *
 * This is what makes reproducing `C-0063`'s **0.0706145537** and `C-0090`'s **0.0621469105** cost
 * one solve each instead of 360 000: a gate on the *pipeline* rather than on the *search*.
 */
fun upwardPlacementFromKey(
    key: String,
    phaseBasePairs: Int,
    arm: Double,
    edgeX: Double,
    sites: List<List<Double>>,
    width: Double = OrigamiDuplex.INTERHELICAL,
    tolerance: Double = 5.0e-7
): UpwardArmPlacement {
    require(key.isNotBlank()) { "a placement key must not be blank" }
    require(sites.isNotEmpty()) { "sites must not be empty" }
    val rows = key.split(";").map { entry ->
        val parts = entry.split(":")
        require(parts.size == 2) { "a key entry must be 'row:x,x,...', was: '$entry'" }
        val row = parts[0].toIntOrNull()
            ?: throw IllegalArgumentException("a key entry's row must be an integer: '$entry'")
        require(row in sites.indices) { "row $row is not a row of this lattice" }
        val roots = parts[1].split(",").map { token ->
            val scaled = token.toLongOrNull()
                ?: throw IllegalArgumentException("a key root must be an integer: '$token'")
            val parsed = scaled / 1.0e6
            val nearest = sites[row].minByOrNull { abs(it - parsed) }
                ?: throw IllegalArgumentException("row $row carries no upward site")
            require(abs(nearest - parsed) <= tolerance) {
                "the key names a root at $parsed nm on row $row, whose nearest upward site is " +
                        "$nearest nm — $key is not a placement on this lattice"
            }
            nearest
        }
        val directions = armDirections(roots, arm, edgeX, width)
            ?: throw IllegalArgumentException(
                "the roots $roots of row $row admit no arm direction assignment"
            )
        UpwardArmRow(row, roots, directions)
    }
    return UpwardArmPlacement(phaseBasePairs, rows.sortedBy { it.row })
}
