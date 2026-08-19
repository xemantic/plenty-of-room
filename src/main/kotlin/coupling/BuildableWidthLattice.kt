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

import com.xemantic.nano.plentyofroom.anchoring.rasterColumnLayout
import com.xemantic.nano.plentyofroom.anchoring.rasterUpwardSites
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiSheet
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import kotlin.math.abs

/**
 * `T-188` — the **cheap bound** of the count/phase grid at `C-0086`'s buildable 38.08 nm.
 *
 * ## What this file is for, and why it is arithmetic
 *
 * `C-0108`'s 32 × 6 count/phase grid is read at §3's nominal **40.00 nm**. Everything downstream
 * of `C-0086`, `C-0090` and `C-0102` is read at the only buildable seamless raster width near it,
 * **112 bp = 38.08 nm**. Moving the grid there is not a rescaling: 38.08 nm is `7 × 5.44`
 * **exactly**, so the column census stops being a truncation with a remainder and becomes a
 * *tangency*, and `CrossoverLayout.EDGE_MARGIN` — a numerical guard whose own KDoc certifies it
 * inert on a 40 nm tile — lands squarely on the lattice.
 *
 * `CLAUDE.md` says to **sweep that guard rather than assume it is inert at the geometry you are
 * running**. This file is what makes the sweep a *proof* instead of a study: a lattice
 * **signature** — the column positions and every row's upward station positions — is a function
 * of the phase, the width, the end-of-row convention and the inset alone, so whether the guard
 * moves anything is settled by comparing signatures, at a cost of no solves at all.
 *
 * The answer, which is the opposite of what the axis was expected to say, is `T-188`'s falsifier
 * `F3`: at the **buildable** width the guard's *value* is exactly inert over 0.05 nm, half a rise
 * and one rise, because a column that lands *on* the edge is deleted by any positive inset and
 * the next one in is a whole 16 bp pitch further; at the **nominal** width it is *not*, because
 * there the closest approach is 0.28 nm and one rise is 0.34. What is not inert at 38.08 nm is
 * the guard's **existence** — the binary `admitRowEnd` — and a constraint has only two physical
 * states, present and absent (`C-0100`).
 *
 * ## Conventions, restated rather than inherited
 *
 * Lengths **nm**; `x` runs **along** the helices and carries the width, `y` **across** them and
 * does not; the origin is the tile centre. A **row** is one duplex, a **plane** is `C-0055`'s
 * 8 bp crossover plane, a **column** is the sheet's own 16 bp column lattice, and a **station**
 * is an upward (`EAST`) plane site at which an arm may be rooted.
 */

/**
 * The complete geometric identity of one upward station lattice — everything a host and a
 * placement can differ in, and nothing else.
 *
 * Two signatures that agree position by position describe the **same** lattice, which is what
 * lets an inset sweep be settled without grading anything.
 */
data class UpwardLatticeSignature(

    /** The crossover phase in base pairs, in `[0, 32)`. */
    val phaseBasePairs: Int,

    /** The along-helix tile width in nm. */
    val edgeX: Double,

    /** Whether a plane lying **on** the row end was kept. */
    val admitRowEnd: Boolean,

    /** The inset in nm at which a kept row-end plane sits inside the edge. */
    val inset: Double,

    /** The sheet's own crossover columns, ascending, in nm. */
    val columnPositions: List<Double>,

    /** The upward station positions of every row, ascending, in nm. */
    val rows: List<List<Double>>
) {

    /** How many crossover columns the host carries. */
    val columns: Int get() = columnPositions.size

    /** How many upward stations the lattice offers in total. */
    val upwardSites: Int get() = rows.sumOf { it.size }

    /** Whether the lattice maps onto itself under `(x, row) -> (−x, D − 1 − row)`. */
    val centroSymmetric: Boolean get() = rowsAreCentroSymmetric(rows)

}

/**
 * The [UpwardLatticeSignature] of one phase, width and end-of-row convention.
 *
 * With [admitRowEnd] `false` this is `CrossoverLayout.atBasePairPhase` and `upwardRootLattice`
 * exactly, to the last bit — the truncation `C-0015`, `C-0055`, `C-0063` and `C-0108` are all
 * written on, which is gate 2 of `T-188`.
 *
 * @throws IllegalArgumentException if [edgeX] is not positive, [duplexes] is below two, or
 *   [inset] is not positive.
 */
fun upwardLatticeSignature(
    phaseBasePairs: Int,
    edgeX: Double,
    duplexes: Int,
    admitRowEnd: Boolean = false,
    inset: Double = CrossoverLayout.EDGE_MARGIN,
    sheet: OrigamiSheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )
): UpwardLatticeSignature {
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(duplexes >= 2) { "duplexes must be at least 2, was: $duplexes" }
    require(inset > 0.0) { "inset must be positive, was: $inset" }
    val rise = sheet.crossoverSpacing / Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    return UpwardLatticeSignature(
        phaseBasePairs = phaseBasePairs,
        edgeX = edgeX,
        admitRowEnd = admitRowEnd,
        inset = inset,
        columnPositions = rasterColumnLayout(
            phaseBasePairs, sheet, edgeX, admitRowEnd, inset
        ).positions,
        rows = rasterUpwardSites(
            phaseBasePairs, edgeX, duplexes, admitRowEnd, rise, inset
        )
    )
}

/**
 * Whether two signatures describe the same lattice to within [tolerance] nm, position by
 * position.
 *
 * With [columnsToo] `false` only the **stations** are compared, which is the comparison
 * `C-0090`'s *"the upward station set is identical to the 40 nm one"* is a statement about: at
 * phases 8 and 24 the stations coincide across the two widths and the **hosts** do not, and that
 * is exactly what makes the two widths' 2 × 2 a matched comparison of hosts and loads.
 */
fun latticesAgree(
    a: UpwardLatticeSignature,
    b: UpwardLatticeSignature,
    columnsToo: Boolean = true,
    tolerance: Double = 1.0e-12
): Boolean {
    require(tolerance >= 0.0) { "tolerance must not be negative, was: $tolerance" }
    fun same(x: List<Double>, y: List<Double>) =
        x.size == y.size && x.zip(y).all { (u, v) -> abs(u - v) <= tolerance }
    if (columnsToo && !same(a.columnPositions, b.columnPositions)) return false
    if (a.rows.size != b.rows.size) return false
    return a.rows.zip(b.rows).all { (x, y) -> same(x, y) }
}

/**
 * What a sweep of the edge guard's **value** does to a lattice, over the whole phase period.
 *
 * [distinctSignatures] of `1` is a **proof** that the guard's value decides nothing at this
 * width and convention: the station set, the column set and every position are identical at
 * every swept inset, so no grading can separate them. Anything above `1` is the signal that the
 * guard has become a physical assertion and has to be priced.
 */
data class InsetSensitivity(

    /** The along-helix tile width in nm. */
    val edgeX: Double,

    /** Whether a plane lying on the row end was kept. */
    val admitRowEnd: Boolean,

    /** The insets swept, in nm. */
    val insets: List<Double>,

    /** How many distinct lattices the sweep produced over the whole phase period. */
    val distinctSignatures: Int,

    /** The largest change in any phase's column **count** across the sweep. */
    val worstColumnCountChange: Int,

    /** The largest distance any surviving station moved across the sweep, in nm. */
    val worstStationDisplacement: Double,

    /** The phases at which any column count changed. */
    val phasesWhoseColumnCountMoves: List<Int>,

    /** The phases at which any station moved or appeared. */
    val phasesWhoseStationsMove: List<Int>
) {

    /** Whether the guard's value is provably inert here. */
    val inert: Boolean get() = distinctSignatures == 1

}

/**
 * [InsetSensitivity] over [insets] at [edgeX], swept over every phase of the column lattice.
 *
 * A station that exists at one inset and not at another counts as a moved station and its
 * displacement is not defined, so [InsetSensitivity.worstStationDisplacement] is measured over
 * the stations the two lattices **share** by index within a row, and the count change is reported
 * separately. That is the conservative reading: a lattice whose station *count* moves is not
 * inert whatever the displacement says.
 *
 * @throws IllegalArgumentException if fewer than two insets are swept.
 */
fun insetSensitivity(
    edgeX: Double,
    duplexes: Int,
    admitRowEnd: Boolean = false,
    insets: List<Double> = listOf(
        CrossoverLayout.EDGE_MARGIN,
        Gen1Tile.RISE_PER_BASE_PAIR / 2.0,
        Gen1Tile.RISE_PER_BASE_PAIR
    ),
    period: Int = CrossoverLayout.BASE_PAIRS_PER_PERIOD,
    sheet: OrigamiSheet = origamiSheet(
        Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
    )
): InsetSensitivity {
    require(insets.size >= 2) { "a sweep needs at least two insets, had: ${insets.size}" }
    require(period >= 1) { "period must be at least 1, was: $period" }
    val phases = 0 until period
    val byInset = insets.map { inset ->
        phases.map { upwardLatticeSignature(it, edgeX, duplexes, admitRowEnd, inset, sheet) }
    }
    val reference = byInset[0]
    var worstCount = 0
    var worstMove = 0.0
    val countMovers = sortedSetOf<Int>()
    val stationMovers = sortedSetOf<Int>()
    byInset.drop(1).forEach { level ->
        phases.forEach { phase ->
            val a = reference[phase]
            val b = level[phase]
            val countChange = abs(a.columns - b.columns)
            if (countChange > worstCount) worstCount = countChange
            if (countChange != 0) countMovers += phase
            if (!latticesAgree(a, b, columnsToo = false)) stationMovers += phase
            a.rows.zip(b.rows).forEach { (x, y) ->
                x.zip(y).forEach { (u, v) -> if (abs(u - v) > worstMove) worstMove = abs(u - v) }
            }
        }
    }
    val distinct = byInset.map { level ->
        level.joinToString(";") { signature ->
            signature.columnPositions.joinToString(",") { "%.12f".format(it) } + "|" +
                    signature.rows.joinToString("/") { row ->
                        row.joinToString(",") { "%.12f".format(it) }
                    }
        }
    }.distinct().size
    return InsetSensitivity(
        edgeX = edgeX,
        admitRowEnd = admitRowEnd,
        insets = insets,
        distinctSignatures = distinct,
        worstColumnCountChange = worstCount,
        worstStationDisplacement = worstMove,
        phasesWhoseColumnCountMoves = countMovers.toList(),
        phasesWhoseStationsMove = stationMovers.toList()
    )
}
