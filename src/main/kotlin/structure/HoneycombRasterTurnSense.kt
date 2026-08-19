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

package com.xemantic.nano.plentyofroom.structure

import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.roundToInt
import kotlin.math.sqrt

/**
 * `T-218` — the **turn sense** a caDNAno honeycomb x-raster carries.
 *
 * `C-0136`/`CH-0165` derive the honeycomb's admissible raster row lengths as
 * `N ≡ 7Δ + {0, 10, 11} (mod 21)` with `Δ = (b − a) mod 3` the difference of the neighbour classes
 * the scaffold arrives from and leaves to, and the two senses **disjoint**. This file supplies the
 * missing coordinate: which `Δ` an actual `m × n` x-raster puts on each of its helices.
 *
 * ## Coordinates
 *
 * The cross-section is an **exact integer** lattice. A cell `(x, y)` sits at the physical point
 * `(x·d√3/2, y·d/2)` for interhelical distance `d`, which makes every honeycomb site an integer
 * pair and every bond one of six integer offsets — so nothing here rounds a lattice position.
 *
 * Sublattice **A** is `y ≡ 0 (mod 3)` with `x − y/3` even; its bonds are `(0, +2)`, `(−1, −1)`,
 * `(+1, −1)`, i.e. azimuths `90°`, `210°`, `330°`. Sublattice **B** is `y ≡ 2 (mod 3)` with
 * `x − (y − 2)/3` even; its bonds are `(0, −2)`, `(+1, +1)`, `(−1, +1)`, i.e. `270°`, `30°`,
 * `150°`. **No two consecutive bonds of a honeycomb path are parallel** — that is why a row of
 * three or more helices cannot be straight, and it is the whole of this task's cheap bound.
 *
 * ## Sign conventions, fixed before deriving
 *
 * All helices are parallel to a **global** `z`, positions in base pairs from one common origin
 * plane. B-DNA is right-handed, so viewed from `+z` the backbone azimuth **increases**
 * counter-clockwise with `z`; one azimuth step of the lattice (`+7 bp` on honeycomb) advances it by
 * `+240° ≡ −120°`. **Neighbour class therefore increases as the neighbour azimuth decreases by one
 * class step**, which is what [neighbourClassDifference] encodes.
 *
 * A raster runs the full length of every helix, so the scaffold's **axial direction alternates**
 * helix to helix; and the row *length* is `|z_out − z_in|`, positive by construction. So the sense
 * that enters the residue formula is `Δ_eff = (s·Δ_geom) mod c`, [RasterTurn.effectiveSense].
 */
enum class HoneycombSublattice { A, B }

/** The six honeycomb bond azimuths, in degrees — the odd multiples of 30°. */
val HONEYCOMB_BOND_AZIMUTHS: Set<Int> = setOf(30, 90, 150, 210, 270, 330)

/** The six honeycomb bonds as integer offsets, by sublattice. */
val HONEYCOMB_BOND_OFFSETS: Map<HoneycombSublattice, List<Pair<Int, Int>>> = mapOf(
    HoneycombSublattice.A to listOf(0 to 2, -1 to -1, 1 to -1),
    HoneycombSublattice.B to listOf(0 to -2, 1 to 1, -1 to 1)
)

/** One helix of the cross-section, at exact integer lattice coordinates. */
data class HoneycombCell(val x: Int, val y: Int) {

    /** Which honeycomb sublattice this cell is on; a non-lattice pair throws. */
    val sublattice: HoneycombSublattice = honeycombSublatticeOf(x, y)

    /** The three cells bonded to this one. */
    val neighbours: List<HoneycombCell>
        get() = HONEYCOMB_BOND_OFFSETS.getValue(sublattice).map { (dx, dy) ->
            HoneycombCell(x + dx, y + dy)
        }
}

/** [HoneycombSublattice] of an integer cross-section coordinate; throws off the lattice. */
fun honeycombSublatticeOf(x: Int, y: Int): HoneycombSublattice {
    if (Math.floorMod(y, 3) == 0 && Math.floorMod(x - y / 3, 2) == 0) {
        return HoneycombSublattice.A
    }
    if (Math.floorMod(y, 3) == 2 && Math.floorMod(x - (y - 2) / 3, 2) == 0) {
        return HoneycombSublattice.B
    }
    throw IllegalArgumentException("($x, $y) is not a honeycomb lattice site")
}

/**
 * The azimuth, in degrees on `[0, 360)`, of the integer cross-section offset `(dx, dy)` — with
 * `x` in units of `d√3/2` and `y` in units of `d/2`, so the six bonds land on multiples of 60°.
 */
fun honeycombAzimuthDegrees(dx: Int, dy: Int): Double {
    require(dx != 0 || dy != 0) { "a bond offset cannot be zero" }
    val raw = Math.toDegrees(atan2(dy / 2.0, dx * sqrt(3.0) / 2.0))
    val wrapped = if (raw < 0.0) raw + 360.0 else raw
    val nearest = wrapped.roundToInt()
    require(abs(wrapped - nearest) < 1e-9 && nearest in HONEYCOMB_BOND_AZIMUTHS) {
        "offset ($dx, $dy) is not on a honeycomb bond azimuth: $wrapped"
    }
    return Math.floorMod(nearest, 360).toDouble()
}

/**
 * The neighbour-class difference `(j(leave) − j(arrive)) mod classes` of a helix whose scaffold
 * arrives from azimuth [arriveAzimuthDegrees] and leaves to [leaveAzimuthDegrees].
 *
 * Class index increases as azimuth **decreases** by `360/classes`, because one azimuth step is a
 * positive number of base pairs and B-DNA is right-handed (see the file header).
 */
fun neighbourClassDifference(
    arriveAzimuthDegrees: Double,
    leaveAzimuthDegrees: Double,
    classes: Int
): Int {
    require(classes >= 2) { "classes must be at least two, was: $classes" }
    val perClass = 360.0 / classes
    val steps = (arriveAzimuthDegrees - leaveAzimuthDegrees) / perClass
    require(abs(steps - steps.roundToInt()) < 1e-9) {
        "azimuths $arriveAzimuthDegrees and $leaveAzimuthDegrees are not a whole number of " +
                "$perClass° class steps apart"
    }
    return Math.floorMod(steps.roundToInt(), classes)
}

/** One helix's contribution to the raster: where the scaffold enters, leaves, and in what sense. */
data class RasterTurn(
    val index: Int,
    val cell: HoneycombCell?,
    val sublattice: HoneycombSublattice?,
    val arriveAzimuthDegrees: Double,
    val leaveAzimuthDegrees: Double,
    val geometricSense: Int,
    val axialSign: Int,
    val effectiveSense: Int
)

/**
 * The x-raster path of an `m × n` honeycomb block: [rows] corrugated rows of [helicesPerRow]
 * helices, *"left to right, then down, then right to left, then down"* (Douglas et al.).
 *
 * The rows stagger between two `y` positions because the honeycomb has no straight chain at all;
 * the paper states the same fact as *"the x-raster rows … are corrugated"*. [mirrored] reflects the
 * cross-section, `x → −x`, which is the one free convention (which face it is viewed from).
 */
fun honeycombXRasterPath(
    rows: Int,
    helicesPerRow: Int,
    mirrored: Boolean = false
): List<HoneycombCell> {
    require(rows >= 1) { "rows must be at least one, was: $rows" }
    require(helicesPerRow >= 2) { "helicesPerRow must be at least two, was: $helicesPerRow" }
    require(helicesPerRow % 2 == 0) {
        "helicesPerRow must be EVEN: a row's two ends must both carry the DOWNWARD vertical " +
                "bond, and that bond points up on one sublattice and down on the other, " +
                "was: $helicesPerRow"
    }
    val path = ArrayList<HoneycombCell>(rows * helicesPerRow)
    (0 until rows).forEach { r ->
        val order = if (r % 2 == 0) (0 until helicesPerRow) else (helicesPerRow - 1 downTo 0)
        order.forEach { x ->
            val y = -3 * r - if (Math.floorMod(x + r, 2) == 0) 0 else 1
            path += HoneycombCell(if (mirrored) -x else x, y)
        }
    }
    return path
}

/** Every consecutive pair of a raster path must be a honeycomb bond; throws where it is not. */
fun requireHoneycombPath(path: List<HoneycombCell>) {
    require(path.size >= 3) { "a raster path needs at least three helices, had: ${path.size}" }
    path.zipWithNext().forEach { (from, to) ->
        require(to in from.neighbours) { "$from and $to are not honeycomb neighbours" }
    }
}

/**
 * The turn sense of every **interior** helix of [path]. The two path ends carry one raster
 * crossover each, so their sense is undefined and they are not returned.
 *
 * [firstAxialSign] is `+1` if the scaffold traverses `path[0]` in `+z`; the sign alternates from
 * there, because a raster runs the full length of every helix.
 */
fun honeycombRasterTurns(
    path: List<HoneycombCell>,
    firstAxialSign: Int = 1,
    classes: Int = 3
): List<RasterTurn> {
    requireHoneycombPath(path)
    require(firstAxialSign == 1 || firstAxialSign == -1) {
        "firstAxialSign must be +1 or -1, was: $firstAxialSign"
    }
    return (1 until path.size - 1).map { k ->
        val here = path[k]
        val arrive = honeycombAzimuthDegrees(path[k - 1].x - here.x, path[k - 1].y - here.y)
        val leave = honeycombAzimuthDegrees(path[k + 1].x - here.x, path[k + 1].y - here.y)
        val geometric = neighbourClassDifference(arrive, leave, classes)
        val sign = if (k % 2 == 0) firstAxialSign else -firstAxialSign
        RasterTurn(
            index = k,
            cell = here,
            sublattice = here.sublattice,
            arriveAzimuthDegrees = arrive,
            leaveAzimuthDegrees = leave,
            geometricSense = geometric,
            axialSign = sign,
            effectiveSense = Math.floorMod(sign * geometric, classes)
        )
    }
}

/**
 * The same construction on Rothemund's **single-layer square sheet**, whose cross-section is a
 * straight chain of [helices] and whose two in-plane neighbours are 180° apart.
 *
 * This is the control: `C-0086`'s width rule is unconditional, so this must return a **constant**
 * effective sense. It does, and the reason is arithmetic — `2` is its own negative modulo `4`.
 */
fun squareSheetRasterTurns(
    helices: Int,
    firstAxialSign: Int = 1,
    classes: Int = 4
): List<RasterTurn> {
    require(helices >= 3) { "a raster needs at least three helices, had: $helices" }
    require(firstAxialSign == 1 || firstAxialSign == -1) {
        "firstAxialSign must be +1 or -1, was: $firstAxialSign"
    }
    return (1 until helices - 1).map { k ->
        val geometric = neighbourClassDifference(180.0, 0.0, classes)
        val sign = if (k % 2 == 0) firstAxialSign else -firstAxialSign
        RasterTurn(
            index = k,
            cell = null,
            sublattice = null,
            arriveAzimuthDegrees = 180.0,
            leaveAzimuthDegrees = 0.0,
            geometricSense = geometric,
            axialSign = sign,
            effectiveSense = Math.floorMod(sign * geometric, classes)
        )
    }
}

/**
 * The smallest positive difference between a row length in [first] and one in [second], both read
 * modulo [period]. Zero means a **uniform** row length serves both senses.
 */
fun minimumRowLengthStagger(first: Set<Int>, second: Set<Int>, period: Int): Int {
    require(period >= 2) { "period must be at least two, was: $period" }
    require(first.isNotEmpty() && second.isNotEmpty()) { "both residue sets must be non-empty" }
    return first.minOf { a ->
        second.minOf { b ->
            val d = Math.floorMod(a - b, period)
            minOf(d, period - d)
        }
    }
}
