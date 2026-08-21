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

import com.xemantic.nano.plentyofroom.design.DUPLEX_RADIUS_NM
import com.xemantic.nano.plentyofroom.design.ScadnanoDesign
import com.xemantic.nano.plentyofroom.lattice.HoneycombCrossoverLattice
import com.xemantic.nano.plentyofroom.lattice.crossoverLatticeOfGrid
import com.xemantic.nano.plentyofroom.structure.DuplexMechanics
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import kotlin.math.abs

/**
 * `T-267` — [HoneycombGrillage] on an **imported** design.
 *
 * `C-0151`'s recommended `10 × 6` block was *"a pair of integers in a study literal"* until
 * `C-0160` drew it; this is the other end of that seam. The cross-section, the row pitch and the
 * axial extent come out of the file, and the only thing the caller still supplies is the
 * elasticity — which no design file carries and none ever will.
 *
 * The refusals are the content. A **square** design is refused rather than reshaped: this
 * repository has one honeycomb cross-section object and one square one, and the whole cost of the
 * iteration-33 correction was a lattice statement travelling onto a body it did not describe.
 */
data class HoneycombBlockImport(
    val designName: String,
    val lattice: String,
    val rasterRows: Int,
    val helicesPerRow: Int,
    val helices: Int,
    val axialWindowBasePairs: Int,
    val bondLength: Double,
    val rowPitch: Double,
    val columnPitch: Double,
    val lengthS: Double,
    val lengthY: Double,
    val refusals: List<String>,
    val notes: List<String>,
    val block: HoneycombBlock?
) {

    /**
     * The lattice this import describes, at a stated foundation.
     *
     * The environment is never part of an import: a foundation stiffness is a property of the
     * grafted layer and of the state it is read at, and `environment/Regime` is where it is
     * declared.
     */
    fun grillage(
        foundationStiffness: Double,
        mechanics: DuplexMechanics = DuplexMechanics.gen1(),
        slipStiffness: Double = Gen1Tile.crossoverInPlaneStiffness(),
        hingeStiffnessEnhancement: Double = 1.0,
        subdivisions: Int = 1,
        linkStiffness: Double = HoneycombGrillage.RIGID_LINK_STIFFNESS,
        faceColumn: Int = 0,
        axialPinBeam: Int = 0,
        bondPrestrains: Map<HoneycombBondSite, Double> = emptyMap()
    ): HoneycombGrillage {
        val block = requireNotNull(block) {
            "this design is not a honeycomb block this repository can grade: " +
                refusals.joinToString("; ")
        }
        return HoneycombGrillage(
            block = block,
            rowBasePairs = axialWindowBasePairs,
            foundationStiffness = foundationStiffness,
            hingeStiffness = mechanics.crossoverHingeStiffness,
            hingeStiffnessEnhancement = hingeStiffnessEnhancement,
            slipStiffness = slipStiffness,
            duplex = mechanics.duplex,
            subdivisions = subdivisions,
            linkStiffness = linkStiffness,
            faceColumn = faceColumn,
            axialPinBeam = axialPinBeam,
            bondPrestrains = bondPrestrains
        )
    }
}

/**
 * The honeycomb block an imported [ScadnanoDesign] is, or the reasons it is not one.
 *
 * The cross-section is read from the helices' own **grid positions**, which is the one place a
 * scadnano file states it: `grid_position = (column, raster row)` on the honeycomb grid, which
 * `C-0160` asserts lands on this corpus's cross-section exactly. A design whose helices do not
 * fill an `m × n` rectangle of that grid is refused, because [HoneycombBlock] is that rectangle.
 *
 * @param bondLength supplied only where the file states no interhelical gap; a file that states
 *          one and a caller that supplies a different one is a refusal, not an override.
 */
fun ScadnanoDesign.honeycombBlockImport(
    name: String,
    bondLength: Double? = null,
    duplexDiameter: Double = 2.0 * DUPLEX_RADIUS_NM,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR
): HoneycombBlockImport {
    val refusals = mutableListOf<String>()
    val notes = mutableListOf<String>()

    val lattice = crossoverLatticeOfGrid(grid)
    if (lattice !== HoneycombCrossoverLattice) {
        refusals += "this design is drawn on the '$grid' grid and HoneycombGrillage's object is " +
            "a honeycomb block: a square sheet's interfaces are a path graph and a honeycomb " +
            "block's are not (C-0154), so the two are different lattices and not two " +
            "parameterisations of one"
    }

    val positions = helices.map { it.gridPosition }
    if (positions.size != helixCount || positions.any { it.size != 2 }) {
        refusals += "a honeycomb cross-section is read from the helices' own grid positions, and " +
            "this design carries ${positions.count { it.size == 2 }} two-dimensional position(s) " +
            "for $helixCount helices"
    }

    var rows = 0
    var columns = 0
    if (positions.size == helixCount && positions.all { it.size == 2 }) {
        val columnValues = positions.map { it[0] }.distinct().sorted()
        val rowValues = positions.map { it[1] }.distinct().sorted()
        columns = columnValues.size
        rows = rowValues.size
        if (columnValues != (0 until columns).toList() || rowValues != (0 until rows).toList()) {
            refusals += "the helices' grid positions are not a 0-based rectangle: columns " +
                "$columnValues, raster rows $rowValues"
        } else if (positions.map { it[0] to it[1] }.distinct().size != helixCount) {
            refusals += "two helices share a grid position, so the design is not an m x n block"
        } else if (rows * columns != helixCount) {
            refusals += "the grid positions span $rows x $columns = ${rows * columns} sites and " +
                "the design has $helixCount helices"
        }
    }

    val fileBond = interhelicalDistanceNm()
    val bond = when {
        fileBond != null && bondLength != null && abs(fileBond - bondLength) > 1e-12 -> {
            refusals += "the file states an interhelical distance of " +
                "${fileBond.roundedForProse()} nm and the caller supplied " +
                "${bondLength.roundedForProse()} nm"
            fileBond
        }
        fileBond != null -> fileBond
        bondLength != null -> {
            notes += "the file states no interhelical distance; the caller's " +
                "${bondLength.roundedForProse()} nm is " +
                "used and is declared here rather than defaulted"
            bondLength
        }
        else -> {
            refusals += "the file states no interhelical distance and none was supplied"
            Double.NaN
        }
    }

    if (strands.any { s -> s.domains.any { it.deletions.isNotEmpty() || it.insertions.isNotEmpty() } }) {
        refusals += "the design carries insertions or deletions, so an offset is not a base pair"
    }

    val span = axialSpanBasePairs()
    val block = if (refusals.isEmpty()) HoneycombBlock(
        rasterRows = rows,
        helicesPerRow = columns,
        bondLength = bond,
        duplexDiameter = duplexDiameter
    ) else null

    return HoneycombBlockImport(
        designName = name,
        lattice = lattice?.name ?: grid,
        rasterRows = rows,
        helicesPerRow = columns,
        helices = helixCount,
        axialWindowBasePairs = span,
        bondLength = bond,
        rowPitch = HoneycombCrossSectionGeometry.rowPitch(bond),
        columnPitch = HoneycombCrossSectionGeometry.columnPitch(bond),
        lengthS = span * risePerBasePair,
        lengthY = rows * HoneycombCrossSectionGeometry.rowPitch(bond),
        refusals = refusals,
        notes = notes,
        block = block
    )
}
