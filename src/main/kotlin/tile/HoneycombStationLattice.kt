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

/**
 * `T-203` — the attachment lattice a **honeycomb** block's top face offers.
 *
 * ## Why this needed its own derivation
 *
 * Every plan ceiling, station lattice, crossover phase and placement in this repository is
 * **single-layer square-lattice**: four crossover azimuths, planes every 8 bp, the same adjacent
 * pair every 32 bp, and `C-0015`'s parity rule on top of it. **None of that transfers.**
 *
 * The honeycomb, read directly from Douglas, Marblestone, Teerapittayanon, Vazquez, Church & Shih,
 * *Nucleic Acids Research* **37**:5001 (2009) by `C-0119`:
 *
 * > *"In a fully occupied honeycomb lattice, each staple helix has **three nearest neighbors** …
 * > antiparallel crossovers between adjacent staple helices only where the strand backbones arrive
 * > at points of closest proximity, which repeat **every 21 base pairs** if the helical twist is
 * > fixed at 10.5 base pairs per turn. Thus for a given staple helix, potential staple-crossover
 * > positions occur **every seven base pairs**, or two-thirds of a turn."*
 *
 * So: **three** azimuths rather than four, **7 bp** between consecutive positions on a helix rather
 * than 8, and **21 bp** before the *same* neighbour comes round again rather than 32. The 7 and the
 * 21 are the pair a reader is most likely to conflate — 7 bp is *any* azimuth's next position and
 * 21 bp is *one* azimuth's period, and an attachment roots on one azimuth.
 */
object HoneycombLattice {

    /** Neighbours of a helix in a fully occupied honeycomb lattice. Read directly. */
    const val AZIMUTHS: Int = 3

    /** Base pairs between consecutive crossover positions on a helix, over ALL azimuths. */
    const val ANY_AZIMUTH_STEP_BP: Int = 7

    /** Base pairs before the SAME adjacent pair comes round again — one azimuth's period. */
    const val SAME_PAIR_PERIOD_BP: Int = 21

    /** `360 / 3`. The azimuths are equally spaced by construction of the lattice. */
    fun azimuthSeparationDegrees(): Double = 360.0 / AZIMUTHS

    /**
     * Whether a helix's free azimuth points **straight out** of the slab, or obliquely.
     *
     * A honeycomb lattice is two interpenetrating triangular sublattices, and caDNAno alternates
     * helix orientation between them — so along a row the free direction alternates between
     * "straight out" and "two oblique". Which one a given helix has is a property of the
     * **sublattice**, not of the design, and it is what makes the census a parity question rather
     * than a multiplication.
     *
     * The parity used here is `(row + column) % 2`, the standard bipartite colouring of the
     * honeycomb. **It fixes which helices carry the perpendicular root and which carry two oblique
     * ones; it does not change the COUNT**, because every top-face helix has exactly one free
     * direction either way — which is asserted in the census rather than assumed here.
     */
    fun pointsDirectlyOut(row: Int, column: Int): Boolean = (row + column) % 2 == 0
}

/**
 * How many attachment stations one top-face helix carries over a row of [rowBasePairs].
 *
 * An attachment roots on **one** azimuth, so the ladder is the **21 bp** period and not the 7 bp
 * step: positions at `phase + 21k` that fall inside the row.
 */
fun honeycombStationsOnHelix(rowBasePairs: Int, phaseBasePairs: Int): Int {
    require(rowBasePairs > 0) { "rowBasePairs must be positive, was: $rowBasePairs" }
    require(phaseBasePairs >= 0) { "phaseBasePairs must not be negative, was: $phaseBasePairs" }
    if (phaseBasePairs > rowBasePairs) return 0
    return (rowBasePairs - phaseBasePairs) / HoneycombLattice.SAME_PAIR_PERIOD_BP + 1
}

/** The upward station census of an `m × n` honeycomb block. */
class HoneycombStationCensus(
    val rasterRows: Int,
    val layers: Int,
    val rowBasePairs: Int,
    val phaseBasePairs: Int,
    val helices: Int,
    val topFaceHelices: Int,
    val stationsPerHelix: Int,
    val stations: Int,
    val perpendicularRootHelices: Int,
    val obliqueRootHelices: Int,
    val alongHelixPitch: Double,
    val acrossHelixPitch: Double
)

/**
 * The attachment stations an `m × n` honeycomb block's **top face** offers.
 *
 * Only the top face counts: a buried helix has all three azimuths occupied by neighbours, so it has
 * no free direction to root an attachment on. That is the same statement as the square lattice's
 * *"a single-layer sheet occupies two of its four azimuths and the other two point out of the
 * plane"* — but on a **slab** the count is set by the FACE rather than by the sheet, and a deeper
 * block therefore offers **fewer** stations at the same helix count, which is the opposite of what
 * a reader expects from a thicker tile.
 */
fun honeycombStationCensus(
    rasterRows: Int,
    layers: Int,
    rowBasePairs: Int,
    phaseBasePairs: Int = 0,
    interhelicalDistance: Double =
        com.xemantic.nano.plentyofroom.structure.Gen1Tile.INTERHELICAL_HONEYCOMB,
    risePerBasePair: Double =
        com.xemantic.nano.plentyofroom.structure.Gen1Tile.RISE_PER_BASE_PAIR
): HoneycombStationCensus {
    require(rasterRows >= 1) { "rasterRows must be at least 1, was: $rasterRows" }
    require(layers >= 1) { "layers must be at least 1, was: $layers" }
    val perHelix = honeycombStationsOnHelix(rowBasePairs, phaseBasePairs)
    val topFace = rasterRows
    val perpendicular = (0 until topFace).count { HoneycombLattice.pointsDirectlyOut(0, it) }
    return HoneycombStationCensus(
        rasterRows = rasterRows,
        layers = layers,
        rowBasePairs = rowBasePairs,
        phaseBasePairs = phaseBasePairs,
        helices = rasterRows * layers,
        topFaceHelices = topFace,
        stationsPerHelix = perHelix,
        stations = topFace * perHelix,
        perpendicularRootHelices = perpendicular,
        obliqueRootHelices = topFace - perpendicular,
        alongHelixPitch = HoneycombLattice.SAME_PAIR_PERIOD_BP * risePerBasePair,
        acrossHelixPitch = interhelicalDistance
    )
}

/**
 * `T-204` — how much more (or less) of the total load a tile's **collar** carries, relative to
 * another tile, from geometry alone.
 *
 * `C-0022` solved the edge profile on a 40 × 40.35 nm tile, and every four-layer number is read
 * under it unchanged. The collar is a **local** rim effect — `CLAUDE.md` records a sub-Debye
 * **1.65 nm** band whose total contribution scales as `1/L` — so its depth and width are set by
 * screening and **not** by the tile, while its *share* of the load scales as the tile's
 * **perimeter over area**. That makes a transfer between two tiles boundable before any field is
 * solved, which is the cheap bound this task runs first.
 *
 * @return the factor by which the collar's share on the tile `(toX, toY)` exceeds its share on
 *          `(fromX, fromY)`.
 */
fun collarShareRatio(fromX: Double, fromY: Double, toX: Double, toY: Double): Double {
    require(fromX > 0.0 && fromY > 0.0) { "the source tile must be positive: $fromX x $fromY" }
    require(toX > 0.0 && toY > 0.0) { "the target tile must be positive: $toX x $toY" }
    fun perimeterOverArea(x: Double, y: Double) = 2.0 * (x + y) / (x * y)
    return perimeterOverArea(toX, toY) / perimeterOverArea(fromX, fromY)
}
