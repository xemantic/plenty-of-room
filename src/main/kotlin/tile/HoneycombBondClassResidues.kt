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

import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.HoneycombCell
import com.xemantic.nano.plentyofroom.structure.HoneycombSublattice
import com.xemantic.nano.plentyofroom.structure.RasterTurn
import com.xemantic.nano.plentyofroom.structure.honeycombAzimuthDegrees
import com.xemantic.nano.plentyofroom.structure.honeycombRasterTurns
import com.xemantic.nano.plentyofroom.structure.honeycombXRasterPath
import kotlin.math.floor
import kotlin.math.sign

/**
 * `T-244` / `T-243` — the honeycomb face's crossover **bond-class residues**, and the axial
 * **windows** a two-length raster leaves for a crossover column.
 *
 * ## The rule, read rather than recalled
 *
 * Douglas, Marblestone, Teerapittayanon, Vazquez, Church & Shih, *Nucleic Acids Res.* **37**:5001
 * (caDNAno; `PMC2731887`, in `gpd/data/T-151-sources/`), **read directly**:
 *
 * > *"Our default rules allow antiparallel crossovers between adjacent staple helices only where
 * > the strand backbones arrive at points of closest proximity, which repeat every 21 base pairs
 * > if the helical twist is fixed at 10.5 base pairs per turn. Thus for a given staple helix,
 * > potential staple-crossover positions occur every seven base pairs, or two-thirds of a turn.
 * > Our default rules allow antiparallel crossovers between adjacent scaffold helices to occur
 * > five base pairs, or half a turn, upstream or downstream of allowed crossover positions for
 * > the associated staple helices."*
 *
 * Three numbers, and all three are needed here: **21** per pair, **7** per class step, **±5** for
 * the scaffold. `C-0141` and `C-0119` already carry the first two; the third has never been used
 * in this repository, and it is what turns a swept convention into a derived one.
 *
 * ## The map
 *
 * `HoneycombRasterTurnSense`'s own sign convention — *"viewed from `+z` the backbone azimuth
 * increases counter-clockwise with `z`; one azimuth step of the lattice (`+7 bp` on honeycomb)
 * advances it by `+240° ≡ −120°`"* — fixes the map completely once one constant is named. Write
 * `b₀` for the base-pair residue, modulo 21, of the **class-zero** bond; then a bond of class `c`
 * carries its staple crossovers at `b₀ + 7c` and its scaffold crossovers at `b₀ + 7c ± 5`.
 *
 * The two sublattices are **not** independent: a bond is one object seen from both of its ends, so
 * `R_B(φ + 180°) = R_A(φ)` identically, which is why [honeycombBondClass] reads the class off
 * `330°` on `A` and `150°` on `B` — the **same** bond. Both sublattices carry **three** azimuths
 * and **one** residue each, so nothing here asks a parity to justify a count (`CH-0151`).
 *
 * ## Conventions
 *
 * Axial positions are **integer base pairs on one global `z`**, `C-0140`'s convention, with the
 * datum at the first raster crossover. Residues are `mod 21`, non-negative. `x` runs across an
 * x-raster row, `y` along the stack of rows; a **face normal** is `+1` or `−1` on `x`.
 */
object HoneycombCrossoverRule {

    /** Base pairs between consecutive crossover positions of one helix, over all azimuths. */
    const val ANY_AZIMUTH_STEP_BP: Int = 7

    /** Base pairs between consecutive crossover positions of one *pair* of helices. */
    const val SAME_PAIR_PERIOD_BP: Int = 21

    /** How far a scaffold crossover sits from its pair's staple position — *"five base pairs"*. */
    const val SCAFFOLD_OFFSET_BP: Int = 5

    /** The neighbour classes of a honeycomb site. */
    const val CLASSES: Int = 3

    /** The azimuth this file calls class zero, per sublattice. They are the **same** bond. */
    fun classZeroAzimuthDegrees(sublattice: HoneycombSublattice): Double =
        if (sublattice == HoneycombSublattice.A) 330.0 else 150.0
}

/**
 * The neighbour class of the bond leaving [sublattice] at [azimuthDegrees].
 *
 * Class **increases as the azimuth decreases** by 120°, because one class step is `+7 bp` and
 * B-DNA is right-handed — the convention `HoneycombRasterTurnSense` states and
 * `neighbourClassDifference` already encodes.
 */
fun honeycombBondClass(sublattice: HoneycombSublattice, azimuthDegrees: Double): Int {
    val reference = HoneycombCrossoverRule.classZeroAzimuthDegrees(sublattice)
    val perClass = 360.0 / HoneycombCrossoverRule.CLASSES
    val steps = (reference - azimuthDegrees) / perClass
    val nearest = Math.round(steps).toInt()
    require(kotlin.math.abs(steps - nearest) < 1e-9) {
        "azimuth $azimuthDegrees is not a whole class step from $reference on $sublattice"
    }
    return Math.floorMod(nearest, HoneycombCrossoverRule.CLASSES)
}

/** The staple-crossover residue of that bond, given the class-zero residue [classZeroResidue]. */
fun honeycombStapleResidue(
    sublattice: HoneycombSublattice,
    azimuthDegrees: Double,
    classZeroResidue: Int
): Int = Math.floorMod(
    classZeroResidue +
            HoneycombCrossoverRule.ANY_AZIMUTH_STEP_BP * honeycombBondClass(sublattice, azimuthDegrees),
    HoneycombCrossoverRule.SAME_PAIR_PERIOD_BP
)

/** The two scaffold-crossover residues of that bond — the staple position `± 5 bp`. */
fun honeycombScaffoldResidues(
    sublattice: HoneycombSublattice,
    azimuthDegrees: Double,
    classZeroResidue: Int
): Set<Int> {
    val staple = honeycombStapleResidue(sublattice, azimuthDegrees, classZeroResidue)
    val period = HoneycombCrossoverRule.SAME_PAIR_PERIOD_BP
    return setOf(
        Math.floorMod(staple + HoneycombCrossoverRule.SCAFFOLD_OFFSET_BP, period),
        Math.floorMod(staple - HoneycombCrossoverRule.SCAFFOLD_OFFSET_BP, period)
    )
}

/**
 * `C-0136`'s admissible row-length residues at a stated effective sense, **re-derived here**.
 *
 * A helix's length is the difference of its two scaffold-crossover positions, so it is
 * `7Δ + (e_leave − e_arrive)` with each `e` in `{+5, −5}` — and `{0, +10, −10}` is exactly
 * `{0, 10, 11}` modulo 21. That the two constructions agree is the map's own cross-check.
 */
fun admissibleRowLengthResidues(effectiveSense: Int): Set<Int> {
    val period = HoneycombCrossoverRule.SAME_PAIR_PERIOD_BP
    val offset = HoneycombCrossoverRule.SCAFFOLD_OFFSET_BP
    return setOf(0, 2 * offset, -2 * offset).map {
        Math.floorMod(HoneycombCrossoverRule.ANY_AZIMUTH_STEP_BP * effectiveSense + it, period)
    }.toSet()
}

/** A window on the global axial `z`, in whole base pairs. */
data class AxialWindow(val lowBasePairs: Int, val highBasePairs: Int) {

    init {
        require(highBasePairs >= lowBasePairs) {
            "an axial window cannot run backwards, was: [$lowBasePairs, $highBasePairs]"
        }
    }

    /** Its extent in base pairs. */
    val basePairs: Int get() = highBasePairs - lowBasePairs

    /** Its extent in nm at a stated rise. */
    fun nm(risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR): Double =
        basePairs * risePerBasePair

    /** The intersection with [other], or `null` where they do not overlap. */
    fun intersect(other: AxialWindow): AxialWindow? {
        val low = maxOf(lowBasePairs, other.lowBasePairs)
        val high = minOf(highBasePairs, other.highBasePairs)
        return if (high >= low) AxialWindow(low, high) else null
    }

    /** This window with the axial datum reversed, `z → −z`. */
    fun reversed(): AxialWindow = AxialWindow(-highBasePairs, -lowBasePairs)
}

/**
 * The crossover columns a centred lattice of pitch [pitch] fits into a window of [windowNm],
 * with [edgeMargin] kept clear at each end — the construction every four-layer study here uses,
 * lifted out of them so that the **window** becomes the parameter it always was.
 */
fun crossoverColumnsIn(windowNm: Double, pitch: Double, edgeMargin: Double): Int {
    require(pitch > 0.0) { "pitch must be positive, was: $pitch" }
    require(edgeMargin >= 0.0) { "edgeMargin must not be negative, was: $edgeMargin" }
    val usable = windowNm - 2.0 * edgeMargin
    require(usable > 0.0) { "the window must exceed twice the margin, was: $windowNm" }
    return floor(usable / pitch).toInt() + 1
}

/**
 * The slack past the last pitch a window of [windowNm] leaves at margin [edgeMargin] — the
 * quantity that decides whether the guard is inert, and the one `EDGE_MARGIN`'s own KDoc is
 * missing.
 */
fun columnSlack(windowNm: Double, pitch: Double, edgeMargin: Double): Double {
    val count = crossoverColumnsIn(windowNm, pitch, edgeMargin)
    return (windowNm - 2.0 * edgeMargin) - (count - 1) * pitch
}

/** Whether every margin in [edgeMargins] returns the **same** column count at this window. */
fun guardIsInert(windowNm: Double, pitch: Double, edgeMargins: List<Double>): Boolean {
    require(edgeMargins.isNotEmpty()) { "at least one margin is needed" }
    return edgeMargins.map { crossoverColumnsIn(windowNm, pitch, it) }.toSet().size == 1
}

/**
 * `C-0140`'s two-length x-raster, read on the **crossover residue lattice** rather than only on
 * its levels.
 *
 * @param axialReversed whether to negate the axial datum, `z → −z`. Composed with [mirrored] it is
 *          the **proper** rotation about the in-plane `y` axis — the only way to look at the other
 *          face of a chiral object without mirroring the object.
 */
class HoneycombRasterResidues(
    val rasterRows: Int,
    val helicesPerRow: Int,
    val senseOneBasePairs: Int,
    val senseTwoBasePairs: Int,
    val firstAxialSign: Int = 1,
    val mirrored: Boolean = false,
    val axialReversed: Boolean = false
) {

    private val path: List<HoneycombCell> =
        honeycombXRasterPath(rasterRows, helicesPerRow, mirrored)

    private val turns: List<RasterTurn> = honeycombRasterTurns(path, firstAxialSign)

    private val cells: Set<HoneycombCell> = path.toSet()

    private fun lengthOf(sense: Int): Int =
        if (sense == 1) senseOneBasePairs else senseTwoBasePairs

    private val sign: Int = if (axialReversed) -1 else 1

    /**
     * How much a class step advances a residue **on this object's own axial datum**.
     *
     * Reversing `z` reverses the handedness a residue is read with, so `+7` becomes `−7`. It is
     * the one thing that must travel with [axialReversed], and leaving it behind is exactly the
     * improper transformation this class exists to keep apart from a proper one.
     */
    private val classStepBasePairs: Int = sign * HoneycombCrossoverRule.ANY_AZIMUTH_STEP_BP

    /** The staple residue of a bond, on this object's own axial datum. */
    private fun stapleResidueHere(
        sublattice: HoneycombSublattice,
        azimuthDegrees: Double,
        classZeroResidue: Int
    ): Int = Math.floorMod(
        classZeroResidue + classStepBasePairs * honeycombBondClass(sublattice, azimuthDegrees),
        HoneycombCrossoverRule.SAME_PAIR_PERIOD_BP
    )

    /** The axial level of the raster crossover **after** path helix `k`, keyed by `k`. */
    val crossoverLevels: Map<Int, Int> = buildMap {
        put(turns.first().index - 1, 0)
        var current = 0
        turns.forEach { turn ->
            current += turn.axialSign * lengthOf(turn.effectiveSense)
            put(turn.index, sign * current)
        }
    }

    /** How many raster crossovers the path carries — one per consecutive pair. */
    val rasterCrossovers: Int get() = path.size - 1

    /** The axial window of every **interior** helix, keyed by path index. */
    val helixWindows: Map<Int, AxialWindow> = turns.associate { turn ->
        val a = crossoverLevels.getValue(turn.index - 1)
        val b = crossoverLevels.getValue(turn.index)
        turn.index to AxialWindow(minOf(a, b), maxOf(a, b))
    }

    /** The neighbour class of the raster crossover after helix `k`. */
    private fun crossoverClass(k: Int): Int {
        val here = path[k]
        val next = path[k + 1]
        return honeycombBondClass(
            here.sublattice, honeycombAzimuthDegrees(next.x - here.x, next.y - here.y)
        )
    }

    /**
     * `(level − 7·class) mod 21` at every raster crossover.
     *
     * Every one of these must equal `b₀ ± 5` for **one** `b₀`, because `b₀` is a property of the
     * lattice and not of a crossover. So the set has at most two members and they are 10 apart —
     * which is the closure condition, and it is convention-free: shifting the datum shifts every
     * member alike.
     */
    val reducedResidues: List<Int> = (0 until rasterCrossovers).map { k ->
        Math.floorMod(
            crossoverLevels.getValue(k) - classStepBasePairs * crossoverClass(k),
            HoneycombCrossoverRule.SAME_PAIR_PERIOD_BP
        )
    }

    /** The distinct values of [reducedResidues], ascending. */
    val distinctReducedResidues: List<Int> = reducedResidues.distinct().sorted()

    /** Every class-zero residue `b₀` that admits **all** the raster's crossovers. */
    val classZeroResidueCandidates: List<Int> =
        (0 until HoneycombCrossoverRule.SAME_PAIR_PERIOD_BP).filter { b0 ->
            val allowed = setOf(
                Math.floorMod(b0 + HoneycombCrossoverRule.SCAFFOLD_OFFSET_BP, 21),
                Math.floorMod(b0 - HoneycombCrossoverRule.SCAFFOLD_OFFSET_BP, 21)
            )
            distinctReducedResidues.all { it in allowed }
        }

    /** Whether the raster closes on caDNAno's default scaffold-crossover rule. */
    val closes: Boolean get() = classZeroResidueCandidates.isNotEmpty()

    /** The fewest raster crossovers that must be **forced** — the minimum over every `b₀`. */
    val offRuleCrossovers: Int = (0 until HoneycombCrossoverRule.SAME_PAIR_PERIOD_BP).minOf { b0 ->
        val allowed = setOf(
            Math.floorMod(b0 + HoneycombCrossoverRule.SCAFFOLD_OFFSET_BP, 21),
            Math.floorMod(b0 - HoneycombCrossoverRule.SCAFFOLD_OFFSET_BP, 21)
        )
        reducedResidues.count { it !in allowed }
    }

    // -------------------------------------------------------------------------- the face

    /** The path index of the helix of raster row [row] at row position [column]. */
    private fun pathIndex(row: Int, column: Int): Int =
        row * helicesPerRow + (if (row % 2 == 0) column else helicesPerRow - 1 - column)

    /** The helix of raster row [row] on the face whose outward normal is [faceNormalX]. */
    fun faceHelix(row: Int, faceNormalX: Int): HoneycombCell {
        require(faceNormalX == 1 || faceNormalX == -1) {
            "faceNormalX must be +1 or -1, was: $faceNormalX"
        }
        val outward = if (mirrored) -faceNormalX else faceNormalX
        return path[pathIndex(row, if (outward > 0) helicesPerRow - 1 else 0)]
    }

    /** The free azimuth of that face helix — its one absent neighbour pointing out of the face. */
    fun faceAzimuthDegrees(row: Int, faceNormalX: Int): Double {
        val here = faceHelix(row, faceNormalX)
        val free = here.neighbours.filter { it !in cells }
            .filter { sign((it.x - here.x).toDouble()).toInt() == faceNormalX }
        require(free.size == 1) {
            "row $row's face helix has ${free.size} free azimuths on normal $faceNormalX"
        }
        return honeycombAzimuthDegrees(free.first().x - here.x, free.first().y - here.y)
    }

    /** The station residue of raster row [row] on that face, given [classZeroResidue]. */
    fun faceStationResidue(row: Int, faceNormalX: Int, classZeroResidue: Int): Int =
        stapleResidueHere(
            faceHelix(row, faceNormalX).sublattice,
            faceAzimuthDegrees(row, faceNormalX),
            classZeroResidue
        )

    /**
     * The inter-row ladder offset — the residue of an **odd** row's stations less that of an
     * **even** row's, which is `TwoLengthRaster.stationLattice`'s own parameterisation.
     *
     * It contains no `b₀`, so it needs none: it is a difference of two classes.
     */
    fun interRowOffsetBasePairs(faceNormalX: Int): Int {
        val offsets = (0 until rasterRows - 1).map { row ->
            val here = faceStationResidue(row, faceNormalX, 0)
            val next = faceStationResidue(row + 1, faceNormalX, 0)
            if (row % 2 == 0) Math.floorMod(next - here, 21) else Math.floorMod(here - next, 21)
        }.distinct()
        require(offsets.size == 1) {
            "the face's inter-row offset is not one value: $offsets"
        }
        return offsets.first()
    }

    /** The row windows of the face's rooting helices, extrapolated over a path end by parity. */
    val faceWindows: Map<Int, AxialWindow> by lazy { faceWindowsOn(1) }

    private fun faceWindowsOn(faceNormalX: Int): Map<Int, AxialWindow> {
        val defined = (0 until rasterRows).mapNotNull { row ->
            val here = faceHelix(row, faceNormalX)
            val index = path.indexOf(here)
            helixWindows[index]?.let { row to it }
        }.toMap()
        require(defined.isNotEmpty()) { "no face helix has a defined window" }
        return (0 until rasterRows).associateWith { row ->
            defined[row] ?: defined.entries.first { it.key % 2 == row % 2 }.value
        }
    }

    /** How many stations each face row carries at the determined phase. */
    fun stationsPerRow(faceNormalX: Int): List<Int> {
        val b0 = classZeroResidueCandidates.firstOrNull()
        require(b0 != null) { "this raster does not close, so no phase is determined" }
        val windows = faceWindowsOn(faceNormalX)
        return (0 until rasterRows).map { row ->
            val residue = faceStationResidue(row, faceNormalX, b0)
            val window = windows.getValue(row)
            val first = window.lowBasePairs + Math.floorMod(residue - window.lowBasePairs, 21)
            if (first > window.highBasePairs) 0 else (window.highBasePairs - first) / 21 + 1
        }
    }

    /** The face's station count at the determined phase. */
    fun stationsOnFace(faceNormalX: Int): Int = stationsPerRow(faceNormalX).sum()

    /**
     * The ladder phase `TwoLengthRaster.stationLattice` would have to be given to reproduce the
     * determined lattice — measured, as that function measures it, from the block's low plane.
     */
    fun determinedLadderPhaseBasePairs(faceNormalX: Int): Int {
        val b0 = classZeroResidueCandidates.firstOrNull()
        require(b0 != null) { "this raster does not close, so no phase is determined" }
        return Math.floorMod(faceStationResidue(0, faceNormalX, b0) - blockWindow.lowBasePairs, 21)
    }

    // -------------------------------------------------------------------------- the windows

    /** The union window of each x-raster row over its own interior helices. */
    val rowWindows: List<AxialWindow> = (0 until rasterRows).map { row ->
        val own = (0 until helicesPerRow).mapNotNull { helixWindows[row * helicesPerRow + it] }
        require(own.isNotEmpty()) { "raster row $row carries no interior helix" }
        AxialWindow(own.minOf { it.lowBasePairs }, own.maxOf { it.highBasePairs })
    }

    /** The window each **interface** offers a crossover column — two adjacent rows intersected. */
    val interfaceWindows: List<AxialWindow> = (0 until rasterRows - 1).map { row ->
        rowWindows[row].intersect(rowWindows[row + 1])
            ?: error("raster rows $row and ${row + 1} do not overlap axially")
    }

    /** The window every interior helix of the block shares — the strictest reading. */
    val allHelixWindow: AxialWindow = AxialWindow(
        helixWindows.values.maxOf { it.lowBasePairs },
        helixWindows.values.minOf { it.highBasePairs }
    )

    /** The block's own bounding window — the one every four-layer study has been reading. */
    val blockWindow: AxialWindow = AxialWindow(
        helixWindows.values.minOf { it.lowBasePairs },
        helixWindows.values.maxOf { it.highBasePairs }
    )
}
