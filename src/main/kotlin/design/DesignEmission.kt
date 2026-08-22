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

import com.xemantic.nano.plentyofroom.tile.HoneycombRasterResidues
import com.xemantic.nano.plentyofroom.tile.honeycombRasterProfile
import java.io.File

/**
 * Where a committed design artifact lives.
 *
 * Not `gpd/results/`: a result is the output of a computation and is re-run by an `Entry points`
 * row, and a **design** is an interchange artifact whose staleness is caught by a test that
 * rebuilds it and compares it byte for byte (`CommittedDesignsTest`), which is the stronger
 * guarantee of the two.
 */
const val DESIGN_DIRECTORY: String = "gpd/designs"

/** The single-layer square sheet `C-0157`'s oxDNA run simulated, round-tripped through the writer. */
const val SQUARE_SHEET_SOURCE: String = "src/test/resources/gen1-tile.sc"

/** The file the round-tripped square sheet is written to. */
const val SQUARE_SHEET_DESIGN: String = "$DESIGN_DIRECTORY/gen1-sheet-square-15x112.sc"

/** The file the recommended honeycomb block is written to. */
const val HONEYCOMB_BLOCK_DESIGN: String = "$DESIGN_DIRECTORY/gen1-block-honeycomb-10x6-102-109.sc"

/** `C-0151`'s recommended cross-section: ten corrugated x-raster rows of six helices. */
const val RECOMMENDED_RASTER_ROWS: Int = 10

/** `C-0151`'s recommended cross-section, thickness direction. */
const val RECOMMENDED_HELICES_PER_ROW: Int = 6

/** `C-0151`'s recommended row length at effective sense one. */
const val RECOMMENDED_SENSE_ONE_BASE_PAIRS: Int = 102

/** `C-0151`'s recommended row length at effective sense two. */
const val RECOMMENDED_SENSE_TWO_BASE_PAIRS: Int = 109

/** The square sheet, read from the file `C-0157` simulated and written back out by this writer. */
fun squareSheetDesign(source: File = File(SQUARE_SHEET_SOURCE)): ScadnanoDesign =
    ScadnanoDesign.fromFile(source)

/** The block `C-0151` recommends, built from the object `C-0151` graded it on. */
fun recommendedHoneycombBlockDesign(): ScadnanoDesign = honeycombBlockScaffoldDesign(
    rasterRows = RECOMMENDED_RASTER_ROWS,
    helicesPerRow = RECOMMENDED_HELICES_PER_ROW,
    senseOneBasePairs = RECOMMENDED_SENSE_ONE_BASE_PAIRS,
    senseTwoBasePairs = RECOMMENDED_SENSE_TWO_BASE_PAIRS
)

/**
 * Writes both committed design artifacts.
 *
 *     ./gradlew study -Pstudy=design.DesignEmissionKt
 *
 * Writes no result file: what it emits is a **design**, and `CommittedDesignsTest` is what keeps
 * the committed copies from going stale.
 */
fun main() {
    val sheet = squareSheetDesign()
    val sheetFile = sheet.writeTo(File(SQUARE_SHEET_DESIGN))
    println("T-266 - wrote " + sheetFile.path)
    println(
        "  square sheet: " + sheet.helixCount + " duplexes, " + sheet.rowBasePairs() +
            " bp, phase " + sheet.crossoverPhase() + ", " + sheet.crossoverColumns().size +
            " columns, " + sheet.crossoverCount() + " crossovers, " +
            sheet.scaffoldTurns().size + " raster turns"
    )
    val sheetReport = sheet.checkBuildability()
    println(
        "  buildability: " + sheetReport.verdict + " on the " + sheetReport.lattice +
            " lattice, " + sheetReport.violations.size + " violation(s), " +
            sheetReport.notApplicable.size + " rule(s) withheld"
    )

    val block = recommendedHoneycombBlockDesign()
    val blockFile = block.writeTo(File(HONEYCOMB_BLOCK_DESIGN))
    println("T-266 - wrote " + blockFile.path)
    val residues = HoneycombRasterResidues(
        RECOMMENDED_RASTER_ROWS, RECOMMENDED_HELICES_PER_ROW,
        RECOMMENDED_SENSE_ONE_BASE_PAIRS, RECOMMENDED_SENSE_TWO_BASE_PAIRS
    )
    val profile = honeycombRasterProfile(
        RECOMMENDED_RASTER_ROWS, RECOMMENDED_HELICES_PER_ROW,
        RECOMMENDED_SENSE_ONE_BASE_PAIRS, RECOMMENDED_SENSE_TWO_BASE_PAIRS
    )
    println(
        "  honeycomb block: " + block.helixCount + " helices, " + block.scaffoldTurns().size +
            " raster turns, extent " + block.rowBasePairs() + " bp"
    )
    println(
        "  closes=" + residues.closes + " offRule=" + residues.offRuleCrossovers +
            " stagger=" + profile.staggerBasePairs + " rowSpan=" + profile.rowSpanBasePairs +
            " interfaceWindow=" + profile.interfaceWindowBasePairs +
            " scaffoldNt=" + profile.scaffoldNucleotides + " fitsM13=" + profile.fitsM13
    )
    println(
        "  stations=" + profile.stationsOnFace + " ladderPhase=" + profile.ladderPhaseBasePairs +
            " interRowOffset=" + profile.interRowOffsetBasePairs +
            " classZeroResidue=" + profile.classZeroResidue
    )
    val blockReport = block.checkBuildability()
    println(
        "  buildability: " + blockReport.verdict + " on the " + blockReport.lattice +
            " lattice, " + blockReport.violations.size + " violation(s), " +
            blockReport.notApplicable.size + " rule(s) withheld; closes=" +
            blockReport.honeycombRasterCloses + " forced=" +
            blockReport.honeycombForcedCrossovers + " b0=" + blockReport.honeycombClassZeroResidues
    )
}
