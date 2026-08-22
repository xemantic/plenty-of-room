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

package com.xemantic.nano.plentyofroom.design

import com.xemantic.nano.plentyofroom.lattice.HoneycombCrossoverLattice
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.honeycombXRasterPath
import com.xemantic.nano.plentyofroom.tile.AxialWindow
import com.xemantic.nano.plentyofroom.tile.HoneycombRasterResidues

/**
 * The recommended honeycomb block, as a scadnano design.
 *
 * `C-0151` selects the **drawable** two-length honeycomb raster: `102 / 109 bp` on the `10 × 6`
 * cross-section (60 helices, ten corrugated x-raster rows of six), closing on caDNAno's published
 * `±5 bp` scaffold rule at **zero** forced crossovers, `116 bp = 39.44 nm` of axial extent, the
 * station ladder determined at phase 16 with the 14 bp inter-row offset. Until this function that
 * recommendation was a pair of integers in a study literal.
 *
 * The path, the turn senses and the level walk are **not** re-derived here — they are read off
 * `HoneycombRasterResidues`, the object `C-0148` and `C-0151` computed their verdicts on, so the
 * emitted design and the graded design are the same object and the test can assert it at departure
 * `0.0` rather than at a tolerance.
 *
 * ## Two things this deliberately does not emit, and why
 *
 * **A staple set.** This corpus determines the block's row lengths, its turn senses, its closure,
 * its crossover columns and its station ladder; it has never determined a honeycomb **staple
 * routing**, and inventing one here would put design into an artifact that nothing graded. The
 * emitted file is therefore the scaffold routing and the lattice it is drawn on — openable,
 * inspectable and checkable, and not yet foldable.
 *
 * **A turn loopout.** Every raster turn is emitted as a direct scaffold crossover, no unpaired
 * nucleotides, which is exactly what `C-0148`'s closure rule certifies at zero forced crossovers
 * and what the corpus's own level walk models. `C-0147` prices an unpaired turn allowance
 * separately (6 nt on reach, 8 nt affordable on M13 at a 112 bp row, 28 nt as built); emitting one
 * needs scadnano's heterogeneous `domains` array, which this reader does not parse.
 */
fun honeycombBlockScaffoldDesign(
    rasterRows: Int,
    helicesPerRow: Int,
    senseOneBasePairs: Int,
    senseTwoBasePairs: Int,
    firstAxialSign: Int = 1,
    interhelicalDistance: Double = Gen1Tile.INTERHELICAL_HONEYCOMB,
    risePerBasePair: Double = Gen1Tile.RISE_PER_BASE_PAIR,
    scaffoldSequence: String? = null
): ScadnanoDesign {
    val residues = HoneycombRasterResidues(
        rasterRows = rasterRows,
        helicesPerRow = helicesPerRow,
        senseOneBasePairs = senseOneBasePairs,
        senseTwoBasePairs = senseTwoBasePairs,
        firstAxialSign = firstAxialSign
    )
    val path = honeycombXRasterPath(rasterRows, helicesPerRow)
    val helixCount = path.size
    val shift = -residues.blockWindow.lowBasePairs

    // The two path ends carry no derived window: their turn sense is undefined, so the corpus's
    // own level walk has nothing to give them. Their length is NOT undetermined, though, and the
    // thing that determines it is a symmetry rather than a choice: an x-raster row is a repeat of
    // the row two above it, so a row's per-position windows depend only on the row's PARITY. Every
    // same-parity row is required to agree position by position -- a tautological check here would
    // prove nothing, and this one has already refused one plausible construction -- and the missing
    // end then takes the window its own position carries in the rows where it is defined.
    //
    // A row is NOT uniform, which is what makes this necessary: at `102 / 109` a row's six helices
    // run 102, 109, 109, 109, 109, 102 on one parity and 109, 102, 102, 102, 102, 109 on the other.
    // `C-0140`'s "every x-raster row spans exactly one length" is a statement about the row's own
    // union WINDOW, not about its helices, and reading it as the latter costs the block 7 nt.
    val windowByParityAndPosition: Map<Pair<Int, Int>, AxialWindow> = buildMap {
        (0 until helicesPerRow).forEach { position ->
            (0..1).forEach { parity ->
                val seen = (0 until rasterRows).filter { it % 2 == parity }
                    .mapNotNull { residues.helixWindows[it * helicesPerRow + position] }
                    .map { it.lowBasePairs to it.highBasePairs }
                    .distinct()
                require(seen.size < 2) {
                    "position $position of the parity-$parity raster rows carries ${seen.size} " +
                        "distinct axial windows, so the raster does not repeat with row parity " +
                        "and a path end's length is not derivable from it: $seen"
                }
                if (seen.size == 1) {
                    put(parity to position, AxialWindow(seen.single().first, seen.single().second))
                }
            }
        }
    }
    val windows = (0 until helixCount).map { k ->
        residues.helixWindows[k] ?: requireNotNull(
            windowByParityAndPosition[(k / helicesPerRow) % 2 to k % helicesPerRow]
        ) {
            "path end $k is at a position no same-parity row defines, so its axial window is " +
                "not derivable and the block cannot be drawn"
        }
    }
    listOf(0, helixCount - 1).forEach { k ->
        val level = residues.crossoverLevels.getValue(if (k == 0) 0 else helixCount - 2)
        require(level == windows[k].lowBasePairs || level == windows[k].highBasePairs) {
            "path end $k does not touch the raster crossover that joins it to the block"
        }
    }

    // the scaffold's direction on helix k: +1 runs along increasing offsets. The rule is the level
    // walk's own -- `honeycombRasterTurns` alternates from `firstAxialSign` with the path index.
    fun axialSign(k: Int): Int = if (k % 2 == 0) firstAxialSign else -firstAxialSign

    val domains = (0 until helixCount).map { k ->
        ScadnanoDomain(
            helix = k,
            forward = axialSign(k) > 0,
            start = windows[k].lowBasePairs + shift,
            end = windows[k].highBasePairs + shift
        )
    }
    val scaffold = ScadnanoStrand(
        domains = domains,
        isScaffold = true,
        sequence = scaffoldSequence,
        color = "#0066cc"
    )

    // scadnano's honeycomb grid takes (h, v) = (column, raster row); its own grid_position ->
    // position map then lands on this corpus's honeycomb cross-section exactly, up to the sign of
    // the y axis (scadnano's y increases downward). Asserted in HoneycombBlockDesignTest.
    val helices = (0 until helixCount).map { k ->
        ScadnanoHelix(
            gridPosition = listOf(path[k].x, k / helicesPerRow),
            maxOffset = residues.blockWindow.basePairs
        )
    }

    return ScadnanoDesign(
        grid = HONEYCOMB_GRID,
        helixCount = helixCount,
        strands = listOf(scaffold),
        helices = helices,
        geometry = ScadnanoGeometry(
            risePerBasePair = risePerBasePair,
            helixRadius = DUPLEX_RADIUS_NM,
            basesPerTurn = HoneycombCrossoverLattice.designBasesPerTurn,
            interHelixGap = interhelicalDistance - 2.0 * DUPLEX_RADIUS_NM
        )
    )
}

/** scadnano's own name for the honeycomb grid. */
const val HONEYCOMB_GRID: String = "honeycomb"

/** The duplex radius scadnano's geometry block is written on, in nm. */
const val DUPLEX_RADIUS_NM: Double = 1.0
