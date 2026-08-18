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
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs

/**
 * `T-161` — **can a crossover be drawn at the LAST base pair of a duplex?**
 *
 * ## Why this is a question at all
 *
 * At `C-0086`'s buildable seamless raster width the tile is **exactly seven column pitches**
 * (`38.08 = 7 × 5.44` nm, where §3's nominal 40.0 nm is 7.35), so at the phases
 * [endOfRowColumnPhases] names — 8 and 24, `C-0063`'s own centro-symmetric pair — a crossover
 * column lands **on the row end**. `CrossoverLayout.EDGE_MARGIN` deletes it. That constant is
 * documented as a *numerical* guard against a zero-length beam element, it is inert at 40.0 nm,
 * and at 38.08 nm it silently becomes a **physical assertion** worth the whole flatness verdict:
 * 0.0621469105 with the row-end column admitted against 0.168371808 with it refused.
 *
 * ## What this file contains, and what it deliberately does not
 *
 * The answer is a **reading** (`C-0095`), so nothing here computes a crossover's chemistry. What
 * is here is the part a verdict must not be allowed to assert without proof:
 *
 * 1. **The covalent count at a duplex end.** `CLAUDE.md` records that a duplex END has exactly
 *    two strand termini and that no force field adds a third; the 33.74°/bp azimuthal quantum
 *    then fixes *which* neighbour one base pair can reach, so a terminal base pair offers
 *    [crossoverBudgetOfDuplexEnd] = **one** crossover. [rasterTurns] says what a boustrophedon
 *    **demands** there, and [maximumTurnsPerRowEnd] compares the two.
 * 2. **The parity congruence.** A boustrophedon's raster turns at one edge join interfaces of one
 *    parity and at the other edge the complementary parity; a column serves the interfaces whose
 *    index parity matches its own (`CrossoverLayout`). So the row-end columns can *be* the raster
 *    turns only if they carry **opposite** parity — [rowEndColumnsAreComplementary] — which holds
 *    exactly when the row is an **odd** number of column pitches. `C-0086`'s odd-half-turn rule
 *    says it always is, and the two congruences turn out to be the **same statement**
 *    (asserted over `1 … 400` bp as a gate).
 * 3. **`C-0090`'s two readings**, recomputed from its own result file rather than transcribed.
 *
 * Conventions: lengths **nm**; `x` along the helices, `y` across them; an **interface** is the
 * boundary between duplexes `b` and `b+1`, indexed `b = 0 … D−2`; a **row end** is `x = ±edgeX/2`.
 */

/**
 * The strand termini a duplex **end** carries — **two**, one 3′ and one 5′.
 *
 * A count, not a model: it is the same statement `C-0029` uses to cap a duplex-end joint at two
 * covalent links, read here on the *other* load path.
 */
const val STRAND_TERMINI_AT_DUPLEX_END: Int = 2

/** `T-5b`'s flatness convention — a coupling is flat when it dishes below this of the stroke. */
const val FLATNESS_CONVENTION: Double = 0.10

/**
 * The row length of the 24-helix rectangle Rothemund folded, in base pairs.
 *
 * **READ DIRECTLY** from Rothemund 2006 Supplementary Figure S19: *"27 turns wide at 10.666 bases
 * / turn -> 288 nt"*, *"24 helices tall"*. `288 = 18 × 16`, an exact whole number of column
 * pitches, so both vertical edges of that structure lie **on** the crossover column lattice.
 */
const val ROTHEMUND_RECTANGLE_ROW_BASE_PAIRS: Int = 288

/**
 * How many crossovers one **terminal base pair** of a duplex can carry.
 *
 * Two strand termini are available, but the crossover azimuth is quantised: on the square lattice
 * a base pair's backbone points at exactly one of [azimuthsPerPeriod] directions, of which a
 * single-layer sheet's two in-plane ones reach its two neighbours. One base pair therefore reaches
 * **one** neighbour, whatever the terminus count — which is why this is `1` and not `2`.
 *
 * @throws IllegalArgumentException if [azimuthsPerPeriod] is not positive.
 */
fun crossoverBudgetOfDuplexEnd(azimuthsPerPeriod: Int = 4): Int {
    require(azimuthsPerPeriod > 0) {
        "azimuthsPerPeriod must be positive, was: $azimuthsPerPeriod"
    }
    return 1
}

// ------------------------------------------------------------------ what a boustrophedon demands

/**
 * Which interfaces a seamless boustrophedon turns at, at each of the tile's two vertical edges.
 *
 * The scaffold enters row 0 at one edge and leaves at the other, so the turn between rows `r` and
 * `r+1` sits at the edge row `r` **exits** — which alternates. Exactly **two** row ends are left
 * free, the scaffold's own termini (or, on a circular scaffold, the two ends of its unpaired
 * remainder): row 0's entry end and the last row's exit end. On an **odd** row count they fall on
 * opposite edges, one each; on an even one they fall on the **same** edge, which is not assumed
 * here but computed.
 *
 * @throws IllegalArgumentException if [duplexes] is below two.
 */
data class RasterTurnCensus(
    val duplexes: Int,
    val entryAtNegativeX: Boolean,
    val negativeEdgeInterfaces: List<Int>,
    val positiveEdgeInterfaces: List<Int>,
    val freeEndRowsAtNegativeX: List<Int>,
    val freeEndRowsAtPositiveX: List<Int>
)

/** [RasterTurnCensus] for a boustrophedon on [duplexes] rows. */
fun rasterTurns(duplexes: Int, entryAtNegativeX: Boolean = true): RasterTurnCensus {
    require(duplexes >= 2) { "duplexes must be at least two, was: $duplexes" }
    // row r is traversed toward +x when (r even) == entryAtNegativeX, and exits at that side
    fun exitsAtPositiveX(row: Int): Boolean = (row % 2 == 0) == entryAtNegativeX
    val positive = (0..duplexes - 2).filter { exitsAtPositiveX(it) }
    val negative = (0..duplexes - 2).filter { !exitsAtPositiveX(it) }
    // row 0's ENTRY end is free; the last row's EXIT end is free
    val free = listOf(
        0 to !entryAtNegativeX,
        (duplexes - 1) to exitsAtPositiveX(duplexes - 1)
    )
    return RasterTurnCensus(
        duplexes = duplexes,
        entryAtNegativeX = entryAtNegativeX,
        negativeEdgeInterfaces = negative,
        positiveEdgeInterfaces = positive,
        freeEndRowsAtNegativeX = free.filter { !it.second }.map { it.first },
        freeEndRowsAtPositiveX = free.filter { it.second }.map { it.first }
    )
}

/**
 * The interfaces a **seamed double raster** — Rothemund's own rectangle folding path — turns at,
 * at each of the two **outer** edges, `(negative x, positive x)`.
 *
 * The left half rasters down, turning alternately at the left edge and the seam; the right half
 * rasters back up, turning alternately at the seam and the right edge. On an even row count the
 * two outer edges therefore serve the **same** interface parity — which is exactly what an
 * **even** column-pitch count supplies, and a doubled odd-half-turn row length is always even.
 * That is the cross-check against a structure that was actually folded.
 *
 * @throws IllegalArgumentException if [duplexes] is below two or odd (a double raster closes on
 *         an even row count; Rothemund's rectangle is 24 helices tall).
 */
fun doubleRasterOuterEdgeInterfaces(duplexes: Int): Pair<List<Int>, List<Int>> {
    require(duplexes >= 2) { "duplexes must be at least two, was: $duplexes" }
    require(duplexes % 2 == 0) {
        "a double raster closes on an even row count, was: $duplexes"
    }
    val left = (0..duplexes - 2).filter { it % 2 == 0 }
    val right = (0..duplexes - 2).filter { (duplexes - 2 - it) % 2 == 0 }
    return left to right
}

/**
 * The largest number of raster turns incident on any **one** row end, over both edges.
 *
 * This is the quantity the covalent budget bounds: a turn at interface `b` occupies the row end of
 * duplex `b` *and* that of duplex `b+1`, so a value above [crossoverBudgetOfDuplexEnd] would mean
 * one terminal base pair had to carry two crossovers, which the azimuth forbids.
 */
fun maximumTurnsPerRowEnd(census: RasterTurnCensus): Int {
    fun peak(interfaces: List<Int>): Int =
        (0 until census.duplexes).maxOf { row ->
            interfaces.count { it == row || it == row - 1 }
        }
    return maxOf(peak(census.negativeEdgeInterfaces), peak(census.positiveEdgeInterfaces))
}

// ------------------------------------------------------------------ what the lattice supplies

/**
 * The interfaces a column of parity [parity] serves on a sheet of [duplexes] rows.
 *
 * `CrossoverLayout`: *"interface `b` carries the columns whose parity matches `b mod 2`"* —
 * because crossovers recur every 16 bp along a helix but alternate between its two neighbours.
 *
 * @throws IllegalArgumentException if [parity] is not 0 or 1, or [duplexes] is below two.
 */
fun interfacesServedByColumnParity(parity: Int, duplexes: Int): List<Int> {
    require(parity == 0 || parity == 1) { "parity must be 0 or 1, was: $parity" }
    require(duplexes >= 2) { "duplexes must be at least two, was: $duplexes" }
    return (0..duplexes - 2).filter { it % 2 == parity }
}

/**
 * Whether the two columns sitting on the two row ends carry **opposite** parity.
 *
 * They are `rowBasePairs / pitch` columns apart, so their parities differ exactly when that count
 * is **odd**. A row that is not a whole number of pitches has no row-end column at all and the
 * answer is `false`.
 *
 * @throws IllegalArgumentException if either argument is not positive.
 */
fun rowEndColumnsAreComplementary(
    rowBasePairs: Int,
    columnPitchBasePairs: Int = COLUMN_PITCH_BASE_PAIRS
): Boolean {
    require(rowBasePairs > 0) { "rowBasePairs must be positive, was: $rowBasePairs" }
    require(columnPitchBasePairs > 0) {
        "columnPitchBasePairs must be positive, was: $columnPitchBasePairs"
    }
    if (rowBasePairs % columnPitchBasePairs != 0) return false
    return (rowBasePairs / columnPitchBasePairs) % 2 == 1
}

/**
 * The parities of the two row-end columns, `(negative x, positive x)`, **read off the lattice**
 * `C-0090` builds rather than derived — so that [rowEndColumnsAreComplementary] has something
 * independent to agree with.
 *
 * @throws IllegalStateException if the phase puts no column on either row end.
 */
fun rowEndColumnParities(
    phaseBasePairs: Int,
    sheet: OrigamiSheet,
    edgeX: Double,
    inset: Double = CrossoverLayout.EDGE_MARGIN
): Pair<Int, Int> {
    val layout = rasterColumnLayout(phaseBasePairs, sheet, edgeX, admitRowEnd = true, inset = inset)
    val half = edgeX / 2.0
    val tolerance = inset + 1.0e-9
    check(abs(layout.positions.first() + half) <= tolerance) {
        "no column sits on the negative row end at phase $phaseBasePairs"
    }
    check(abs(layout.positions.last() - half) <= tolerance) {
        "no column sits on the positive row end at phase $phaseBasePairs"
    }
    return layout.parities.first() to layout.parities.last()
}

/**
 * Whether some boustrophedon sense lands its raster turns **exactly** on the two row-end columns,
 * and if so which.
 *
 * Both senses are free design choices, so the question is whether *either* matches. It does
 * whenever the two end columns are complementary, and never otherwise — that is the whole content
 * of the congruence, restated as a search over the two senses so that it is checked rather than
 * assumed.
 */
data class RowEndMatch(
    val matches: Boolean,
    val entryAtNegativeX: Boolean?,
    val negativeEdgeParity: Int,
    val positiveEdgeParity: Int,
    val turnsAtNegativeEdge: Int,
    val turnsAtPositiveEdge: Int
)

/** [RowEndMatch] for [duplexes] rows at column phase [phaseBasePairs] on a tile of [edgeX] nm. */
fun rasterTurnsOnRowEndColumns(
    duplexes: Int,
    phaseBasePairs: Int,
    sheet: OrigamiSheet,
    edgeX: Double,
    inset: Double = CrossoverLayout.EDGE_MARGIN
): RowEndMatch {
    val (negativeParity, positiveParity) =
        rowEndColumnParities(phaseBasePairs, sheet, edgeX, inset)
    val negativeServes = interfacesServedByColumnParity(negativeParity, duplexes)
    val positiveServes = interfacesServedByColumnParity(positiveParity, duplexes)
    val sense = listOf(true, false).firstOrNull { entryAtNegativeX ->
        val census = rasterTurns(duplexes, entryAtNegativeX)
        census.negativeEdgeInterfaces == negativeServes &&
                census.positiveEdgeInterfaces == positiveServes
    }
    return RowEndMatch(
        matches = sense != null,
        entryAtNegativeX = sense,
        negativeEdgeParity = negativeParity,
        positiveEdgeParity = positiveParity,
        turnsAtNegativeEdge = negativeServes.size,
        turnsAtPositiveEdge = positiveServes.size
    )
}

/**
 * How the interface crossovers of a row-end-admitting lattice split into **scaffold** turns and
 * **staple** crossovers.
 *
 * The two row-end columns are the raster turns, so they are scaffold; every other column is
 * staple. On a seamless boustrophedon the scaffold contributes exactly `duplexes − 1` crossovers,
 * which is `C-0086`'s own count for the LINEAR topology, recovered here from the lattice instead.
 */
data class RowEndInventory(
    val columns: Int,
    val interfaceCrossovers: Int,
    val scaffoldCrossovers: Int,
    val stapleCrossovers: Int
)

/** [RowEndInventory] at column phase [phaseBasePairs] on a tile of [edgeX] nm. */
fun rowEndInventory(
    duplexes: Int,
    phaseBasePairs: Int,
    sheet: OrigamiSheet,
    edgeX: Double,
    inset: Double = CrossoverLayout.EDGE_MARGIN
): RowEndInventory {
    val layout = rasterColumnLayout(phaseBasePairs, sheet, edgeX, admitRowEnd = true, inset = inset)
    val total = layout.parities.sumOf { interfacesServedByColumnParity(it, duplexes).size }
    val match = rasterTurnsOnRowEndColumns(duplexes, phaseBasePairs, sheet, edgeX, inset)
    val scaffold =
        if (match.matches) match.turnsAtNegativeEdge + match.turnsAtPositiveEdge else 0
    return RowEndInventory(
        columns = layout.size,
        interfaceCrossovers = total,
        scaffoldCrossovers = scaffold,
        stapleCrossovers = total - scaffold
    )
}

// ------------------------------------------------------------------ C-0090's two readings

/** One of `C-0090`'s flatness readings at the buildable width, read out of its result file. */
data class RowEndReading(
    val case: String,
    val edgeX: Double,
    val armLength: Double,
    val admitRowEnd: Boolean,
    val phaseBasePairs: Int,
    val columns: Int,
    val bestDishingOverStroke: Double,
    val flatAtTenPercent: Boolean
)

/**
 * `C-0090`'s buildable-width readings at its own recommended arm, read from
 * `gpd/results/T-153-buildable-raster-width.json`.
 *
 * Keyed on **every** dimension its sweep varied — width, arm length, convention and phase —
 * because `CLAUDE.md` records that an upstream result file may hold more than one record per
 * state and a `firstOrNull` on two of four keys silently takes the wrong one.
 *
 * @throws IllegalArgumentException if the file is missing.
 */
fun c0090RowEndReadings(
    file: File,
    edgeX: Double = BUILDABLE_RASTER_WIDTH,
    armLength: Double = 8.16,
    tolerance: Double = 1.0e-9
): List<RowEndReading> {
    require(file.exists()) {
        "C-0090's result file is missing: ${file.path}. T-161's verdict is its two readings and " +
                "they are RECOMPUTED from that file, never transcribed."
    }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("placements").jsonArray
        .map { it.jsonObject }
        .filter {
            abs(it.getValue("edgeX").jsonPrimitive.content.toDouble() - edgeX) < tolerance &&
                    abs(
                        it.getValue("armLength").jsonPrimitive.content.toDouble() - armLength
                    ) < tolerance
        }
        .map { row ->
            RowEndReading(
                case = row.getValue("case").jsonPrimitive.content,
                edgeX = row.getValue("edgeX").jsonPrimitive.content.toDouble(),
                armLength = row.getValue("armLength").jsonPrimitive.content.toDouble(),
                admitRowEnd = row.getValue("admitRowEnd").jsonPrimitive.content.toBoolean(),
                phaseBasePairs = row.getValue("phaseBasePairs").jsonPrimitive.content.toInt(),
                columns = row.getValue("columns").jsonPrimitive.content.toInt(),
                bestDishingOverStroke =
                    row.getValue("bestDishingOverStroke").jsonPrimitive.content.toDouble(),
                flatAtTenPercent =
                    row.getValue("flatAtTenPercent").jsonPrimitive.content.toBoolean()
            )
        }
}

/** The verdict `T-161` decides: which of the two conventions the programme should carry. */
data class RowEndVerdict(
    val admitRowEnd: Boolean,
    val phaseBasePairs: Int,
    val columns: Int,
    val dishingOverStroke: Double,
    val rejectedColumns: Int,
    val rejectedDishingOverStroke: Double,
    val ratio: Double,
    val rejectedAtSamePhase: Double,
    val ratioAtSamePhase: Double,
    val insideFlatnessConvention: Boolean
)

/**
 * The verdict, taken over [readings] at the convention `T-161` decides — **admit**.
 *
 * The best phase is chosen inside each convention, so the two numbers compared are each that
 * convention's own optimum and neither is handicapped by a phase the other would not pick.
 * `C-0090`'s own headline pair is the **same-phase** one, which is carried beside it: at phase 8
 * the refused reading is 0.168371808 where the best refused reading anywhere is 0.156510532, so
 * the two framings differ and neither may be quoted as the other.
 */
fun rowEndVerdict(
    readings: List<RowEndReading>,
    admit: Boolean = true
): RowEndVerdict {
    require(readings.isNotEmpty()) { "readings must not be empty" }
    val chosen = readings.filter { it.admitRowEnd == admit }
        .minByOrNull { it.bestDishingOverStroke }
    val rejected = readings.filter { it.admitRowEnd != admit }
        .minByOrNull { it.bestDishingOverStroke }
    requireNotNull(chosen) { "no reading under the chosen convention" }
    requireNotNull(rejected) { "no reading under the rejected convention" }
    val samePhase = readings.single {
        it.admitRowEnd != admit && it.phaseBasePairs == chosen.phaseBasePairs
    }
    return RowEndVerdict(
        admitRowEnd = chosen.admitRowEnd,
        phaseBasePairs = chosen.phaseBasePairs,
        columns = chosen.columns,
        dishingOverStroke = chosen.bestDishingOverStroke,
        rejectedColumns = rejected.columns,
        rejectedDishingOverStroke = rejected.bestDishingOverStroke,
        ratio = rejected.bestDishingOverStroke / chosen.bestDishingOverStroke,
        rejectedAtSamePhase = samePhase.bestDishingOverStroke,
        ratioAtSamePhase = samePhase.bestDishingOverStroke / chosen.bestDishingOverStroke,
        insideFlatnessConvention = chosen.bestDishingOverStroke < FLATNESS_CONVENTION
    )
}

/** The nominal §3 width in base pairs, `40.0 / 0.34` rounded — 118, and not an admissible row. */
fun nominalRowBasePairs(
    edgeX: Double = Gen1Tile.EDGE_X,
    rise: Double = Gen1Tile.RISE_PER_BASE_PAIR
): Int {
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(rise > 0.0) { "rise must be positive, was: $rise" }
    return Math.round(edgeX / rise).toInt()
}
