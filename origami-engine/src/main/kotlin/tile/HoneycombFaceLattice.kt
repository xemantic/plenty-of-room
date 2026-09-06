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
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * `T-219` — the honeycomb block's **cross-section**, and the attachment lattice its faces offer.
 *
 * ## Why the cross-section had to come first
 *
 * `C-0122` censuses the honeycomb's stations by multiplying top-face helices by stations per
 * ladder; `CH-0151` corrects that by asserting that a top-face helix of one sublattice carries
 * **two** free azimuths at `±60°`; `C-0128` prices an oblique root at *"the honeycomb's own 60°"*.
 * **A free azimuth is a lattice neighbour that is ABSENT**, so none of the three can be settled
 * without the site set — and the site set is fixed by the caDNAno paper in one sentence
 * (Douglas et al., *NAR* **37**:5001, PMC2731887, already in `gpd/data/T-151-sources/`):
 *
 * > *"as viewed down the helical axes, close-packing rows of helices were arrayed within the
 * > honeycomb framework in an x-raster pattern … **The x-raster rows within the honeycomb framework
 * > are corrugated; they stagger up and down and encompass helices that are actually at two
 * > different y-positions.** Similarly, virtual y-oriented layers can be defined that stagger left
 * > and right and encompass helices that are at two different x-positions."*
 *
 * and its Figure 2 caption:
 *
 * > *"The nomenclature of the designs is `m × n`, where `m` is the number of x-raster rows, and
 * > `n` is the number of helices per x-raster row."*
 *
 * So a block is `m` **corrugated rows** of `n` helices, site `(r, c)` with `0 ≤ r < m` and
 * `0 ≤ c < n`, and the neighbour rule is the standard brick-wall representation of the honeycomb:
 * `(r, c ± 1)` always, plus `(r + 1, c)` when `r + c` is even and `(r − 1, c)` when it is odd.
 *
 * ## Conventions, restated rather than inherited
 *
 * Cross-section coordinates: **`x` runs across an x-raster row** (the `n` direction) and **`y`
 * along the stack of rows** (the `m` direction). The helix axis is the third direction and carries
 * the row's base-pair span. Lengths **nm**, angles **degrees**.
 *
 * A **station** is a crossover position on a **free** azimuth whose direction has a positive
 * component along the outward normal of the face being counted. An attachment roots on **one**
 * azimuth, so its ladder is the **21 bp** period and never the 7 bp step (`C-0119`, `C-0122`).
 */
object HoneycombCrossSectionGeometry {

    /** `√3`, once. */
    val SQRT3: Double = sqrt(3.0)

    /**
     * The centre-to-centre spacing **between x-raster rows**, `3d/2`.
     *
     * This is the tile's **in-plane** duplex pitch, and it is where every four-layer number in this
     * corpus differs from a honeycomb: `C-0109` and `C-0120` use `d` itself.
     */
    fun rowPitch(bondLength: Double = Gen1Tile.INTERHELICAL_HONEYCOMB): Double = 1.5 * bondLength

    /**
     * The centre-to-centre spacing **between helices within an x-raster row**, `d√3/2`.
     *
     * This is the tile's **layer** spacing — `CLAUDE.md` already records that a honeycomb array
     * stacks at `d√3/2` rather than `d`, and [multiLayerRigidities] carries it as a parameter.
     */
    fun columnPitch(bondLength: Double = Gen1Tile.INTERHELICAL_HONEYCOMB): Double =
        SQRT3 / 2.0 * bondLength

    /**
     * The plan area a honeycomb lattice spends per helix, `3√3/4 · d²`.
     *
     * It is exactly [rowPitch] × [columnPitch], which is the cheap bound of `T-219`: a
     * cross-section quoted at `d × d` per helix is `3√3/4 = 1.299038…` times denser than **any**
     * honeycomb lattice of that bond length can be, and no choice of layer spacing alone repairs
     * it — the two pitches are wrong in opposite directions and only their product is the cell.
     */
    fun perSiteArea(bondLength: Double = Gen1Tile.INTERHELICAL_HONEYCOMB): Double =
        3.0 * SQRT3 / 4.0 * bondLength * bondLength
}

/** One helix of a honeycomb block: its x-raster row [rasterRow] and position [column] in it. */
data class HoneycombSite(val rasterRow: Int, val column: Int) {

    init {
        require(rasterRow >= 0) { "rasterRow must not be negative, was: $rasterRow" }
        require(column >= 0) { "column must not be negative, was: $column" }
    }

    /**
     * Whether this site's **vertical** bond points toward increasing `y`.
     *
     * The honeycomb's two interpenetrating sublattices; `(r + c)` even is the one whose vertical
     * bond runs *up*, and which therefore sits half a bond *above* its own row-neighbours.
     */
    val verticalBondUp: Boolean get() = (rasterRow + column) % 2 == 0
}

/** A lattice direction out of a site: the neighbour it points at, and its unit vector. */
data class HoneycombAzimuth(
    val rasterRowStep: Int,
    val columnStep: Int,
    val unitX: Double,
    val unitY: Double
) {

    /** The angle in degrees between this azimuth and the outward normal `(normalX, normalY)`. */
    fun angleFromNormalDegrees(normalX: Double, normalY: Double): Double {
        val dot = unitX * normalX + unitY * normalY
        val cross = unitX * normalY - unitY * normalX
        return abs(Math.toDegrees(atan2(cross, dot)))
    }
}

/** The three lattice azimuths of [site], in the order vertical, `−x`, `+x`. */
fun honeycombAzimuthsOf(site: HoneycombSite): List<HoneycombAzimuth> {
    val up = site.verticalBondUp
    val sign = if (up) -1.0 else 1.0
    val half = HoneycombCrossSectionGeometry.SQRT3 / 2.0
    return listOf(
        HoneycombAzimuth(if (up) 1 else -1, 0, 0.0, if (up) 1.0 else -1.0),
        HoneycombAzimuth(0, -1, -half, sign * 0.5),
        HoneycombAzimuth(0, 1, half, sign * 0.5)
    )
}

/**
 * A rectangular `m × n` honeycomb block — `m` corrugated x-raster rows of `n` helices.
 *
 * @param rasterRows `m`, the number of x-raster rows. The tile's **in-plane** direction.
 * @param helicesPerRow `n`, the helices in each row. The tile's **thickness** direction whenever
 *          `n < m`, which is true of both cross-sections this programme carries.
 * @param bondLength `d` in nm — the honeycomb's one lattice constant, measured 2.536 nm by SAXS.
 */
class HoneycombBlock(
    val rasterRows: Int,
    val helicesPerRow: Int,
    val bondLength: Double = Gen1Tile.INTERHELICAL_HONEYCOMB,
    val duplexDiameter: Double = 2.0
) {

    init {
        require(rasterRows >= 1) { "rasterRows must be at least 1, was: $rasterRows" }
        require(helicesPerRow >= 1) { "helicesPerRow must be at least 1, was: $helicesPerRow" }
        require(bondLength > 0.0) { "bondLength must be positive, was: $bondLength" }
        require(duplexDiameter > 0.0) { "duplexDiameter must be positive, was: $duplexDiameter" }
    }

    /** Every helix of the block, rows ascending and columns ascending within a row. */
    val sites: List<HoneycombSite> =
        (0 until rasterRows).flatMap { r -> (0 until helicesPerRow).map { c -> HoneycombSite(r, c) } }

    /** The number of helices — `m · n`. */
    val helices: Int get() = sites.size

    /** Whether the block contains the site at [rasterRow], [column]. */
    fun contains(rasterRow: Int, column: Int): Boolean =
        rasterRow in 0 until rasterRows && column in 0 until helicesPerRow

    /** The `(x, y)` of [site] in nm — `x` across the row, `y` along the stack. */
    fun position(site: HoneycombSite): Pair<Double, Double> {
        require(contains(site.rasterRow, site.column)) { "$site is not in this block" }
        val x = site.column * HoneycombCrossSectionGeometry.columnPitch(bondLength)
        val y = site.rasterRow * HoneycombCrossSectionGeometry.rowPitch(bondLength) +
                (if (site.verticalBondUp) 0.5 * bondLength else 0.0)
        return x to y
    }

    /** The azimuths of [site] whose neighbour is **absent** from the block. */
    fun freeAzimuths(site: HoneycombSite): List<HoneycombAzimuth> {
        require(contains(site.rasterRow, site.column)) { "$site is not in this block" }
        return honeycombAzimuthsOf(site).filter {
            !contains(site.rasterRow + it.rasterRowStep, site.column + it.columnStep)
        }
    }

    /**
     * Every **rooting** azimuth of the face whose outward normal is `(normalX, normalY)`: a free
     * azimuth with a strictly positive component along that normal.
     *
     * An attachment rooted on such an azimuth stands off the block on that side. A free azimuth
     * exactly perpendicular to the normal points **along** the face and roots nothing out of it,
     * so the strict inequality is the definition rather than a tolerance.
     */
    fun rootingAzimuths(
        normalX: Double,
        normalY: Double,
        tolerance: Double = 1e-12
    ): List<Pair<HoneycombSite, HoneycombAzimuth>> {
        require(abs(normalX * normalX + normalY * normalY - 1.0) < 1e-9) {
            "the normal must be a unit vector, was ($normalX, $normalY)"
        }
        return sites.flatMap { site ->
            freeAzimuths(site)
                .filter { it.unitX * normalX + it.unitY * normalY > tolerance }
                .map { site to it }
        }
    }

    /** The centre-to-centre extent along `y`, in nm — the tile's in-plane width less the duplex. */
    val latticeExtentY: Double
        get() {
            val ys = sites.map { position(it).second }
            return ys.max() - ys.min()
        }

    /** The centre-to-centre extent along `x`, in nm — the tile's thickness less the duplex. */
    val latticeExtentX: Double
        get() = (helicesPerRow - 1) * HoneycombCrossSectionGeometry.columnPitch(bondLength)

    /** The block's `y` envelope in nm, duplex diameter included. */
    val envelopeY: Double get() = latticeExtentY + duplexDiameter

    /** The block's `x` envelope in nm, duplex diameter included. */
    val envelopeX: Double get() = latticeExtentX + duplexDiameter

    /** The cross-section area per helix in nm², envelope over helix count. */
    val envelopeAreaPerHelix: Double get() = envelopeX * envelopeY / helices

    /**
     * The plate `edgeY` this block implies under the corpus's own convention — `rasterRows ×`
     * the in-plane pitch, which is what `C-0120` writes as `rasterRows × d`.
     *
     * It is **exactly 1.5×** the standing value at every `m`, because the honeycomb's in-plane
     * pitch is `3d/2` and not `d`.
     */
    val plateEdgeY: Double get() = rasterRows * HoneycombCrossSectionGeometry.rowPitch(bondLength)
}

/** The station ladder on one helix: base-pair indices `phase + 21k` inside `[0, rowBasePairs]`. */
fun honeycombLadderIndices(
    rowBasePairs: Int,
    phaseBasePairs: Int,
    periodBasePairs: Int = HoneycombLattice.SAME_PAIR_PERIOD_BP
): List<Int> {
    require(rowBasePairs > 0) { "rowBasePairs must be positive, was: $rowBasePairs" }
    require(periodBasePairs > 0) { "periodBasePairs must be positive, was: $periodBasePairs" }
    val start = Math.floorMod(phaseBasePairs, periodBasePairs)
    return generateSequence(start) { it + periodBasePairs }.takeWhile { it <= rowBasePairs }.toList()
}

/**
 * The honeycomb face's **station lattice** — one row of `x` positions per rooting helix, centred on
 * the tile, ascending.
 *
 * The two sublattices of a face carry their free azimuth on **two different bond classes**, and the
 * caDNAno rule puts consecutive azimuths of a helix 7 bp apart with the same pair recurring every
 * 21 bp — so **adjacent station rows are staggered along the helices by 7 or 14 bp**, which is a
 * convention this repository cannot yet fix and which is therefore swept as
 * [interRowOffsetBasePairs] rather than chosen. The stagger is **forced**: there is no honeycomb
 * face whose station rows are in register.
 */
fun honeycombStationLattice(
    rootingHelices: Int,
    rowBasePairs: Int,
    basePhaseBasePairs: Int = 0,
    interRowOffsetBasePairs: Int = 7,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR,
    periodBasePairs: Int = HoneycombLattice.SAME_PAIR_PERIOD_BP
): List<List<Double>> {
    require(rootingHelices >= 1) { "rootingHelices must be at least 1, was: $rootingHelices" }
    require(rowBasePairs > 0) { "rowBasePairs must be positive, was: $rowBasePairs" }
    require(risePerBasePair > 0.0) {
        "risePerBasePair must be positive, was: $risePerBasePair"
    }
    val half = rowBasePairs * risePerBasePair / 2.0
    return (0 until rootingHelices).map { row ->
        val phase = basePhaseBasePairs + (row % 2) * interRowOffsetBasePairs
        honeycombLadderIndices(rowBasePairs, phase, periodBasePairs)
            .map { it * risePerBasePair - half }
    }
}

/** Whether [lattice] maps onto itself under `(x, row) → (−x, last − row)` — `C-0063`'s symmetry. */
fun latticeIsCentroSymmetric(lattice: List<List<Double>>, tolerance: Double = 1e-9): Boolean {
    require(lattice.size >= 2) { "a lattice needs at least two rows, had ${lattice.size}" }
    val last = lattice.size - 1
    return lattice.indices.all { row ->
        val mine = lattice[row].sorted()
        val partner = lattice[last - row].map { -it }.sorted()
        mine.size == partner.size && mine.zip(partner).all { (a, b) -> abs(a - b) <= tolerance }
    }
}
