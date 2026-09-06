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

package com.xemantic.nano.plentyofroom.tile

import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.honeycombRasterTurns
import com.xemantic.nano.plentyofroom.structure.honeycombXRasterPath
import kotlin.math.abs
import kotlin.math.floor

/**
 * `T-235` — the axial geometry of `C-0140`'s **two-length** honeycomb x-raster, and the station
 * lattice it makes.
 *
 * ## Why a row length is not a constant
 *
 * `C-0142` graded the corrected four-layer coupled cells at a uniform **112 bp** row, and said
 * so: *"the ROW LENGTH is carried unchanged at 112 bp, and that is a fourth moved input this
 * claim does NOT move."* `C-0140` is what moves it — a honeycomb x-raster carries **both** turn
 * senses, so no uniform row length exists at all, and its recommendation is **112 bp at
 * effective sense 1 and 108 bp at effective sense 2**, whose block extent is 116 bp = 39.44 nm.
 *
 * Two lengths make the 21 bp station ladder **row-dependent**: each rooting helix carries its
 * own length *and* its own axial window on the global `z`, so `C-0141`'s single-`rowBasePairs`
 * [honeycombStationLattice] is no longer the lattice. This file supplies the generalisation, and
 * its limiting case is exact — **equal lengths return `honeycombStationLattice` position for
 * position**.
 *
 * ## What is consumed rather than re-derived
 *
 * The path, the turn senses and the level walk are `C-0140`'s
 * ([honeycombXRasterPath], [honeycombRasterTurns]); the cross-section, the plate `edgeY` and the
 * ladder period are `C-0141`'s ([HoneycombBlock], [HoneycombLattice]). Nothing geometric is
 * written twice.
 *
 * ## Conventions
 *
 * Axial positions are **integer base pairs on one global `z`**, which is `C-0140`'s own
 * convention (all helices parallel to a global `z`, every crossover position absolute). The
 * ladder **phase is measured from the block's own low plane**, which is what makes the
 * equal-length reduction exact. Lengths in nm, `x` **along** the helices with the origin at the
 * block's axial centre, `y` **across** them.
 */

/** One rooting helix of a face: its raster row, its length, and its window on the global `z`. */
data class TwoLengthFaceRow(

    /** The x-raster row this rooting helix belongs to. */
    val rasterRow: Int,

    /** Its index on the scaffold's raster path. */
    val pathIndex: Int,

    /**
     * Whether `C-0140`'s turn sense is **defined** here.
     *
     * The two ends of the raster path carry one raster crossover each, so their sense is
     * undefined; an **odd** raster-row count puts one of them on the counted face, and it is
     * flagged rather than silently assigned.
     */
    val senseIsDefined: Boolean,

    /** `C-0140`'s effective sense, 1 or 2 — extrapolated from the row parity where undefined. */
    val effectiveSense: Int,

    /** The row length this sense is assigned, in base pairs. */
    val lengthBasePairs: Int,

    /** The low end of this helix's window on the global `z`, in base pairs. */
    val lowBasePairs: Int,

    /** The high end of this helix's window on the global `z`, in base pairs. */
    val highBasePairs: Int
)

/**
 * The two-length x-raster of an `m × n` honeycomb block.
 *
 * @param rasterRows `m`.
 * @param helicesPerRow `n` — must be even, which the honeycomb forces (`C-0140`).
 * @param senseOneBasePairs the row length assigned to effective sense 1 (`C-0140`: 112 bp).
 * @param senseTwoBasePairs the row length assigned to effective sense 2 (`C-0140`: 108 bp).
 */
class TwoLengthRaster(
    val rasterRows: Int,
    val helicesPerRow: Int,
    val senseOneBasePairs: Int,
    val senseTwoBasePairs: Int,
    val firstAxialSign: Int = 1,
    val mirrored: Boolean = false
) {

    init {
        require(senseOneBasePairs > 0) {
            "senseOneBasePairs must be positive, was: $senseOneBasePairs"
        }
        require(senseTwoBasePairs > 0) {
            "senseTwoBasePairs must be positive, was: $senseTwoBasePairs"
        }
    }

    private val path = honeycombXRasterPath(rasterRows, helicesPerRow, mirrored)

    private val turns = honeycombRasterTurns(path, firstAxialSign)

    private fun lengthOf(sense: Int): Int =
        if (sense == 1) senseOneBasePairs else senseTwoBasePairs

    /** The level walk of `C-0140`, keyed by path index: the crossover level **after** helix `k`. */
    private val levels: Map<Int, Int> = buildMap {
        put(turns.first().index - 1, 0)
        var current = 0
        turns.forEach { turn ->
            current += turn.axialSign * lengthOf(turn.effectiveSense)
            put(turn.index, current)
        }
    }

    /** The window `[low, high]` of every **interior** helix, keyed by path index. */
    val helixSpans: Map<Int, Pair<Int, Int>> = turns.associate { turn ->
        val a = levels.getValue(turn.index - 1)
        val b = levels.getValue(turn.index)
        turn.index to (minOf(a, b) to maxOf(a, b))
    }

    /** How many interior helices carry each effective sense — `C-0140`'s census. */
    val senseCounts: Pair<Int, Int> =
        turns.count { it.effectiveSense == 1 } to turns.count { it.effectiveSense == 2 }

    /** The block's low axial plane, over its interior helices, in base pairs. */
    val blockLowBasePairs: Int = helixSpans.values.minOf { it.first }

    /** The block's high axial plane, over its interior helices, in base pairs. */
    val blockHighBasePairs: Int = helixSpans.values.maxOf { it.second }

    /** The block's own axial extent in base pairs — `C-0140`'s 116 at both 60-helix designs. */
    val blockExtentBasePairs: Int = blockHighBasePairs - blockLowBasePairs

    /** The block's axial extent in nm. */
    fun blockExtent(risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR): Double =
        blockExtentBasePairs * risePerBasePair

    /** The union window of each x-raster row over its own interior helices, in base pairs. */
    val rowSpans: List<Pair<Int, Int>> = (0 until rasterRows).map { r ->
        val own = (0 until helicesPerRow)
            .map { c -> r * helicesPerRow + c }
            .mapNotNull { helixSpans[it] }
        require(own.isNotEmpty()) { "raster row $r carries no interior helix" }
        own.minOf { it.first } to own.maxOf { it.second }
    }

    /**
     * The path index of the rooting helix of raster row [row] on the `+x` face — the face
     * `C-0141` censuses, whose outward normal is `(1, 0)`.
     *
     * A raster runs left to right and then right to left, so the `+x` face helix is the **last**
     * of an even row and the **first** of an odd one.
     */
    fun faceHelixPathIndex(row: Int): Int =
        row * helicesPerRow + (if (row % 2 == 0) helicesPerRow - 1 else 0)

    /**
     * The rooting helix of every raster row, with its own length and window.
     *
     * A raster path END on the counted face has no defined sense (`C-0140`), so it is filled in
     * from the first **defined** face row of its own **parity** — which is exact whenever the
     * face alternates, and the alternation is asserted rather than assumed.
     */
    val faceRows: List<TwoLengthFaceRow> by lazy {
        val defined = (0 until rasterRows).mapNotNull { row ->
            turns.firstOrNull { it.index == faceHelixPathIndex(row) }?.let { turn ->
                val (low, high) = helixSpans.getValue(turn.index)
                row to TwoLengthFaceRow(
                    row, turn.index, true, turn.effectiveSense, high - low, low, high
                )
            }
        }
        require(defined.isNotEmpty()) {
            "no face helix of this block has a defined turn sense, so nothing can be " +
                    "extrapolated from the row parity"
        }
        (0 until rasterRows).map { row ->
            defined.firstOrNull { it.first == row }?.second ?: run {
                val sameParity = defined.filter { it.first % 2 == row % 2 }.map { it.second }
                require(sameParity.isNotEmpty()) {
                    "raster row $row's face helix is a path end and no defined face row shares " +
                            "its parity"
                }
                require(sameParity.map { it.effectiveSense }.toSet().size == 1) {
                    "the face's parity-$row rows do not carry ONE sense, so the row parity " +
                            "cannot extrapolate the undefined end"
                }
                val model = sameParity.first()
                TwoLengthFaceRow(
                    row, faceHelixPathIndex(row), false, model.effectiveSense,
                    model.lengthBasePairs, model.lowBasePairs, model.highBasePairs
                )
            }
        }
    }

    /**
     * The face's station lattice: one row of `x` positions per rooting helix, in nm, centred on
     * the block's own axial centre and ascending.
     *
     * A station sits at a global `z` congruent to that row's ladder phase modulo the 21 bp
     * period, **inside that row's own window**. The phase is `basePhaseBasePairs` on the even
     * rows and `basePhaseBasePairs + interRowOffsetBasePairs` on the odd ones — `C-0141`'s
     * **forced** inter-row stagger, which it carries at both 7 and 14 bp.
     *
     * With `senseOneBasePairs == senseTwoBasePairs` every window is the same and this returns
     * [honeycombStationLattice] exactly.
     */
    fun stationLattice(
        basePhaseBasePairs: Int = 0,
        interRowOffsetBasePairs: Int = 7,
        risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR,
        periodBasePairs: Int = HoneycombLattice.SAME_PAIR_PERIOD_BP
    ): List<List<Double>> {
        require(risePerBasePair > 0.0) {
            "risePerBasePair must be positive, was: $risePerBasePair"
        }
        require(periodBasePairs > 0) {
            "periodBasePairs must be positive, was: $periodBasePairs"
        }
        val centre = (blockLowBasePairs + blockHighBasePairs) / 2.0
        return faceRows.map { row ->
            val phase = Math.floorMod(
                basePhaseBasePairs + (row.rasterRow % 2) * interRowOffsetBasePairs, periodBasePairs
            )
            val low = row.lowBasePairs - blockLowBasePairs
            val high = row.highBasePairs - blockLowBasePairs
            val first = low + Math.floorMod(phase - low, periodBasePairs)
            generateSequence(first) { it + periodBasePairs }
                .takeWhile { it <= high }
                .map { (it + blockLowBasePairs - centre) * risePerBasePair }
                .toList()
        }
    }
}

/** `C-0140`'s two-length raster of an `m × n` block, at [senseOne] / [senseTwo] base pairs. */
fun twoLengthRaster(
    rasterRows: Int,
    helicesPerRow: Int,
    senseOne: Int,
    senseTwo: Int,
    firstAxialSign: Int = 1,
    mirrored: Boolean = false
): TwoLengthRaster =
    TwoLengthRaster(rasterRows, helicesPerRow, senseOne, senseTwo, firstAxialSign, mirrored)

/**
 * The abstract `columns × rasterRows` attachment grid, snapped onto [raster]'s own row-dependent
 * station lattice — each station moved along its helix to the **nearest** ladder position of its
 * own row.
 *
 * This is [honeycombSnappedGrid] with the row length lifted from a constant to a per-row
 * quantity, and it refuses the same thing for the same reason: a placement wider than a row's
 * ladder, or one that collides two of a row's stations, is a change of the path **count**
 * wearing a change of position.
 */
fun twoLengthSnappedGrid(
    raster: TwoLengthRaster,
    columns: Int,
    edgeY: Double,
    basePhaseBasePairs: Int = 0,
    interRowOffsetBasePairs: Int = 7,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR
): List<Pair<Double, Double>> {
    require(columns > 0) { "columns must be positive, was: $columns" }
    require(edgeY > 0.0) { "edgeY must be positive, was: $edgeY" }
    val lattice = raster.stationLattice(
        basePhaseBasePairs, interRowOffsetBasePairs, risePerBasePair
    )
    val edgeX = raster.blockExtent(risePerBasePair)
    val abstract = attachmentGrid(columns, raster.rasterRows, edgeX, edgeY)
    return abstract.mapIndexed { index, (x, y) ->
        val row = index / columns
        val stations = lattice[row]
        require(columns <= stations.size) {
            "a $columns-column placement cannot stand on row $row's ladder of ${stations.size} " +
                    "stations — that is a change of the path COUNT, not of the position"
        }
        stations.minBy { abs(it - x) } to y
    }.also { snapped ->
        (0 until raster.rasterRows).forEach { row ->
            val xs = (0 until columns).map { snapped[row * columns + it].first }
            require(xs.toSet().size == columns) {
                "snapping row $row collided two of its $columns stations onto one ladder " +
                        "position, which changes the path count rather than the placement"
            }
        }
    }
}

/**
 * The crossover-column count the grillage derives from an axial extent — `floor((edgeX − 2 m)/p)
 * + 1`, exactly as every four-layer study in this corpus assembles it.
 *
 * It is quoted as a function of the guard [edgeMargin] because `CLAUDE.md` records that *"a
 * numerical guard becomes a physical assertion the moment the lattice lands on it"*, and a
 * 116 bp extent clears eleven honeycomb pitches by **0.07 nm**.
 */
fun crossoverColumnCount(
    edgeX: Double,
    edgeMargin: Double,
    pitch: Double
): Int {
    require(edgeX > 0.0) { "edgeX must be positive, was: $edgeX" }
    require(edgeMargin >= 0.0) { "edgeMargin must not be negative, was: $edgeMargin" }
    require(pitch > 0.0) { "pitch must be positive, was: $pitch" }
    val usable = edgeX - 2.0 * edgeMargin
    require(usable > 0.0) { "the guard $edgeMargin leaves no usable span of $edgeX" }
    return floor(usable / pitch).toInt() + 1
}
