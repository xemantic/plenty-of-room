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

import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs

// ---------------------------------------------------------------------------------------------
// T-243 -- the crossover-column count derived from the ROW spans, not from a bounding-box edgeX.
//
// CH-0185: a 116 bp = 39.44 nm block extent clears eleven honeycomb crossover pitches by 0.07 nm,
// one fifth of a base-pair rise, so CrossoverLayout.EDGE_MARGIN admits a TWELFTH column at 0.05 nm
// and refuses it at half a rise -- worth 6 flat cells of 8 against 3. The cheap bound is a floor
// division on windows C-0140's level walk already emits.
// ---------------------------------------------------------------------------------------------

private const val T243_SAMPLES: Int = 81
private const val T243_TOLERANCE: Double = 0.10
private const val T243_RIM_STANDOFF: Double = 1.0
private const val T243_BAND_LOW: Double = 0.26
private const val T243_BAND_MEASURED: Double = 0.30

private fun Double.emitted(digits: Int = 9): String =
    if (!isFinite() || this == 0.0) toString()
    else java.math.BigDecimal(this).round(java.math.MathContext(digits)).toDouble().toString()

@Serializable
private class T243Window(
    val crossSection: String,
    val senseOneBasePairs: Int,
    val senseTwoBasePairs: Int,
    val reading: String,
    val lowBasePairs: Int,
    val highBasePairs: Int,
    val basePairs: Int,
    val nanometres: Double,
    val edgeMargin: Double,
    val edgeMarginConvention: String,
    val crossoverColumns: Int,
    val slackBeyondLastPitch: Double
)

@Serializable
private class T243Guard(
    val crossSection: String,
    val senseOneBasePairs: Int,
    val senseTwoBasePairs: Int,
    val reading: String,
    val nanometres: Double,
    val columnsAtEachMargin: List<Int>,
    val inert: Boolean,
    val slackAtTightestMargin: Double,
    val slackInRises: Double
)

@Serializable
private class T243Dishing(
    val crossSection: String,
    val widthReading: String,
    val edgeX: Double,
    val crossoverColumns: Int,
    val compositeFraction: Double,
    val uncoupledDishingOverStroke: Double,
    val flat: Boolean
)

@Serializable
private class T243Cell(
    val columns: Int,
    val paths: Int,
    val distribution: String,
    val at112BpEleven: Double,
    val at116BpEleven: Double,
    val at116BpTwelve: Double,
    val selectedByRowDerivation: Double,
    val flatAtSelected: Boolean,
    val flatAtTwelve: Boolean,
    val decidedByTheGuard: Boolean
)

@Serializable
private class T243Reproduction(
    val what: String,
    val published: String,
    val here: String,
    val relativeDeparture: String,
    val reproduced: Boolean
)

@Serializable
private class T243Falsifier(val name: String, val statement: String, val fired: Boolean, val note: String)

@Serializable
private class T243Result(
    val task: String,
    val leaf: String,
    val units: Map<String, String>,
    val conventions: Map<String, String>,
    val parameters: Map<String, String>,
    val sources: List<String>,
    val citedInputs: List<String>,
    val cheapBound: Map<String, String>,
    val windows: List<T243Window>,
    val guards: List<T243Guard>,
    val dishing: List<T243Dishing>,
    val cells: List<T243Cell>,
    val reproductions: List<T243Reproduction>,
    val falsifiers: List<T243Falsifier>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

// ------------------------------------------------------------------------------- the tile

private class T243Profile(val smoothDepth: Double, val smoothWidth: Double, val rimDepth: Double) {
    fun field(interiorPressure: Double, edgeX: Double, lengthY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, lengthY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T243_RIM_STANDOFF))
        )
}

private fun t243Profile(file: File): T243Profile {
    require(file.exists()) { "C-0022's result file is missing: " + file.path }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(name: String) = it.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at the design state")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T243Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

/**
 * `C-0142`'s tile, with `edgeX` and the crossover-column count lifted out as parameters — which is
 * the whole of `T-243`: the count stops being a function of `edgeX` and becomes a function of the
 * **window** a row lattice offers.
 */
private class T243Tile(
    val rasterRows: Int,
    val layers: Int,
    val edgeX: Double,
    val crossoverColumns: Int,
    val compositeFraction: Double,
    private val profile: T243Profile
) {

    private val bondLength: Double = Gen1Tile.INTERHELICAL_HONEYCOMB

    private val inPlanePitch: Double = HoneycombCrossSectionGeometry.rowPitch(bondLength)

    private val layerSpacing: Double = HoneycombCrossSectionGeometry.columnPitch(bondLength)

    val edgeY: Double = rasterRows * inPlanePitch

    private val rigidities: MultiLayerRigidities = multiLayerRigidities(
        layers = layers,
        interhelicalDistance = inPlanePitch,
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
        coupling = LayerCoupling.CALIBRATED,
        compositeFraction = compositeFraction,
        layerSpacing = layerSpacing
    )

    private val sheet = equivalentSheet(rigidities)

    val interiorPressure: Double = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)

    val pressureField: PressureField = profile.field(interiorPressure, edgeX, edgeY)

    val freeStroke: Double = PlateOnFoundation(
        sheet.plate(edgeX, edgeY), Gen1Tile.FOUNDATION_SECANT, emptyList(), 12
    ).solve(uniformPressure(interiorPressure)).meanDeflection

    val uncoupledDishing: Double by lazy {
        val pitch = sheet.crossoverSpacing / 2.0
        OrigamiGrillage(
            sheet = sheet,
            lengthX = edgeX,
            beamCount = rasterRows,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            columns = CrossoverLayout.centred(crossoverColumns, pitch),
            subdivisions = 2
        ).solve(pressureField).peakDishing(T243_SAMPLES) / freeStroke
    }
}

fun main() {
    val rise = Gen1Tile.RISE_PER_BASE_PAIR
    val pitch = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP * rise / 2.0
    val margins = listOf(0.05, 0.17, 0.34)
    val marginNames = listOf("the standing numerical guard", "half a base-pair rise", "one base-pair rise")
    val profile = t243Profile(File("gpd/results/T-3b-tile-edge-load-profile.json"))
    val pairs = listOf(112 to 108, 102 to 109)
    val sections = listOf("10 x 6" to (10 to 6), "15 x 4" to (15 to 4))

    println("T-243 - the crossover-column count from the row spans")
    println("  CHEAP BOUND - pitch " + pitch.emitted(6) + " nm; a column serves an INTERFACE")

    // ------------------------------------------------------------------ the windows
    val windows = ArrayList<T243Window>()
    val guards = ArrayList<T243Guard>()
    pairs.forEach { (a, b) ->
        sections.forEach { (name, dims) ->
            val r = HoneycombRasterResidues(dims.first, dims.second, a, b)
            val readings = listOf(
                "bounding box" to r.blockWindow,
                "x-raster row span" to r.rowWindows.first(),
                "interface (two adjacent row spans intersected)" to r.interfaceWindows.first(),
                "every interior helix intersected" to r.allHelixWindow
            )
            readings.forEach { (reading, window) ->
                val nm = window.nm(rise)
                margins.forEachIndexed { i, margin ->
                    windows += T243Window(
                        name, a, b, reading, window.lowBasePairs, window.highBasePairs,
                        window.basePairs, nm, margin, marginNames[i],
                        crossoverColumnsIn(nm, pitch, margin), columnSlack(nm, pitch, margin)
                    )
                }
                guards += T243Guard(
                    name, a, b, reading, nm,
                    margins.map { crossoverColumnsIn(nm, pitch, it) },
                    guardIsInert(nm, pitch, margins),
                    columnSlack(nm, pitch, margins.first()),
                    columnSlack(nm, pitch, margins.first()) / rise
                )
            }
        }
    }

    val rowDerived = HoneycombRasterResidues(10, 6, 112, 108)
    val rowColumns = crossoverColumnsIn(rowDerived.rowWindows.first().nm(rise), pitch, margins.first())
    val interfaceColumns =
        crossoverColumnsIn(rowDerived.interfaceWindows.first().nm(rise), pitch, margins.first())
    val boxColumns = crossoverColumnsIn(rowDerived.blockWindow.nm(rise), pitch, margins.first())

    // ------------------------------------------------------------------ the dishing
    val states = listOf(
        Triple("row length 112 bp", 112 * rise, 11),
        Triple("block extent 116 bp", 116 * rise, 11),
        Triple("block extent 116 bp", 116 * rise, 12)
    )
    val dishing = sections.flatMap { (name, dims) ->
        states.flatMap { (reading, edgeX, columns) ->
            listOf(T243_BAND_MEASURED, T243_BAND_LOW).map { fraction ->
                val tile = T243Tile(dims.first, dims.second, edgeX, columns, fraction, profile)
                T243Dishing(
                    name, reading, edgeX, columns, fraction,
                    tile.uncoupledDishing, tile.uncoupledDishing < T243_TOLERANCE
                )
            }
        }
    }

    // ------------------------------------------------------------------ C-0146's eight cells
    //
    // Read out of C-0146's own result file rather than transcribed: a hand-copied table is a
    // number like any other, and this study's whole contribution is SELECTING among these three
    // columns, so getting them from the file that owns them is the difference between a
    // selection and an assertion.
    val c0146 = File("gpd/results/T-235-coupled-cells-at-the-two-length-raster.json")
    require(c0146.exists()) { "C-0146's result file is missing: " + c0146.path }
    val c0146Cells = Json.parseToJsonElement(c0146.readText())
        .jsonObject.getValue("cells").jsonArray.map { it.jsonObject }

    fun c0146P90(widthReading: String, columns: Int, cols: Int, distribution: String): Double {
        fun text(record: kotlinx.serialization.json.JsonObject, name: String) =
            record.getValue(name).jsonPrimitive.content
        val hits = c0146Cells.filter {
            text(it, "crossSection") == "10 x 6" && text(it, "widthReading") == widthReading &&
                    text(it, "crossoverColumns").toInt() == columns &&
                    text(it, "columns").toInt() == cols &&
                    text(it, "distribution") == distribution &&
                    text(it, "compositeFraction").toDouble() == T243_BAND_MEASURED &&
                    text(it, "placement") == "abstract grid"
        }
        require(hits.size == 1) {
            "C-0146 carries " + hits.size + " cells at " + widthReading + " / " + columns +
                    " columns / " + cols + " / " + distribution
        }
        return text(hits.single(), "p90OverStroke").toDouble()
    }

    val publishedFromFile = listOf(
        1 to "equal springs", 1 to "rim-graded 5:1", 2 to "equal springs", 2 to "rim-graded 5:1",
        3 to "equal springs", 3 to "rim-graded 5:1", 5 to "equal springs", 5 to "rim-graded 5:1"
    ).map { (cols, distribution) ->
        listOf(cols, cols * 10) to (distribution to Triple(
            c0146P90("row length 112 bp", 11, cols, distribution),
            c0146P90("block extent 116 bp", 11, cols, distribution),
            c0146P90("block extent 116 bp", 12, cols, distribution)
        ))
    }

    val published = listOf(
        listOf(1, 10) to ("equal springs" to Triple(0.0680677948, 0.0708759349, 0.0662801686)),
        listOf(1, 10) to ("rim-graded 5:1" to Triple(0.102582764, 0.104654401, 0.0998334915)),
        listOf(2, 20) to ("equal springs" to Triple(0.119502047, 0.125509341, 0.116688801)),
        listOf(2, 20) to ("rim-graded 5:1" to Triple(0.168817101, 0.174594445, 0.16373126)),
        listOf(3, 30) to ("equal springs" to Triple(0.101905503, 0.107278473, 0.0997830457)),
        listOf(3, 30) to ("rim-graded 5:1" to Triple(0.0954158305, 0.100357905, 0.0938556471)),
        listOf(5, 50) to ("equal springs" to Triple(0.0900369, 0.0946671181, 0.0880177483)),
        listOf(5, 50) to ("rim-graded 5:1" to Triple(0.0822611821, 0.0855380627, 0.0805842317))
    )
    require(published == publishedFromFile) {
        "the transcribed cells do not agree with C-0146's own result file"
    }

    val cells = publishedFromFile.map { (counts, entry) ->
        val (distribution, values) = entry
        val selected = values.second
        T243Cell(
            columns = counts[0], paths = counts[1], distribution = distribution,
            at112BpEleven = values.first, at116BpEleven = values.second,
            at116BpTwelve = values.third, selectedByRowDerivation = selected,
            flatAtSelected = selected < T243_TOLERANCE,
            flatAtTwelve = values.third < T243_TOLERANCE,
            decidedByTheGuard = (selected < T243_TOLERANCE) != (values.third < T243_TOLERANCE)
        )
    }

    // ------------------------------------------------------------------ reproductions
    fun departure(published: Double, here: Double): Double =
        if (published == 0.0) abs(here) else abs(here - published) / abs(published)

    fun reproduction(what: String, publishedValue: Double, here: Double, tolerance: Double = 1e-8) =
        T243Reproduction(
            what, publishedValue.emitted(9), here.emitted(9),
            departure(publishedValue, here).emitted(2), departure(publishedValue, here) < tolerance
        )

    fun dishingAt(section: String, reading: String, columns: Int, fraction: Double): Double =
        dishing.first {
            it.crossSection == section && it.widthReading == reading &&
                    it.crossoverColumns == columns && it.compositeFraction == fraction
        }.uncoupledDishingOverStroke

    val reproductions = listOf(
        reproduction("C-0146's 10 x 6 uncoupled dishing, 112 bp / 11 columns, f = 0.30",
            0.0240648102, dishingAt("10 x 6", "row length 112 bp", 11, 0.30)),
        reproduction("C-0146's 10 x 6 uncoupled dishing, 116 bp / 11 columns, f = 0.30",
            0.0252615047, dishingAt("10 x 6", "block extent 116 bp", 11, 0.30)),
        reproduction("C-0146's 10 x 6 uncoupled dishing, 116 bp / 12 columns, f = 0.30",
            0.0231299291, dishingAt("10 x 6", "block extent 116 bp", 12, 0.30)),
        reproduction("C-0146's 10 x 6 uncoupled dishing, 116 bp / 11 columns, f = 0.26",
            0.0268332278, dishingAt("10 x 6", "block extent 116 bp", 11, 0.26)),
        reproduction("C-0146's 15 x 4 uncoupled dishing, 112 bp / 11 columns, f = 0.30",
            0.0978155002, dishingAt("15 x 4", "row length 112 bp", 11, 0.30)),
        reproduction("C-0146's bounding-box column count at the standing guard",
            12.0, boxColumns.toDouble()),
        reproduction("C-0146's bounding-box slack past the eleventh pitch",
            0.07, columnSlack(rowDerived.blockWindow.nm(rise), pitch, 0.05), 2e-2)
    )

    // ------------------------------------------------------------------ falsifiers
    val rowGuards = guards.filter { it.reading != "bounding box" }
    val gradedRowGuards = rowGuards.filter { it.senseOneBasePairs == 112 }
    val f1 = rowGuards.any { !it.inert }
    val f2 = guards.filter { it.senseOneBasePairs == 112 && it.reading.startsWith("x-raster") }
        .zip(guards.filter { it.senseOneBasePairs == 112 && it.reading.startsWith("interface") })
        .any { (row, iface) -> row.columnsAtEachMargin != iface.columnsAtEachMargin }
    val f3 = guards.first { it.reading == "bounding box" && it.senseOneBasePairs == 112 }
        .columnsAtEachMargin != listOf(12, 11, 11)
    val f4 = reproductions.take(5).any { !it.reproduced }

    val falsifiers = listOf(
        T243Falsifier("F1", "a row-derived column count moves across the three EDGE_MARGIN conventions", f1,
            "FIRED at " + rowGuards.count { !it.inert } + " of " + rowGuards.size +
                    " row-derived readings, and all of them are the STRICTEST reading (every " +
                    "interior helix intersected) at the length pair 102 / 109, whose 32.30 nm " +
                    "window clears nine pitches by 0.07 nm - the same knife edge as the bounding " +
                    "box's, at a different window. At C-0140's own 112 / 108, which is the pair " +
                    "every graded cell is read at, all " + gradedRowGuards.size +
                    " row-derived readings are inert: " + gradedRowGuards.all { it.inert }),
        T243Falsifier("F2", "the row-span and interface readings disagree", f2,
            "both give " + rowColumns + " at 112 / 108"),
        T243Falsifier("F3", "the bounding-box reading does not reproduce C-0146's 12 / 11 / 11", f3,
            guards.first { it.reading == "bounding box" && it.senseOneBasePairs == 112 }
                .columnsAtEachMargin.toString()),
        T243Falsifier("F4", "the uncoupled dishing re-solved here does not reproduce C-0146's", f4,
            "five states re-solved, departures " +
                    reproductions.take(5).joinToString { it.relativeDeparture })
    )

    val findings = LinkedHashMap<String, String>()
    runCatching {
        findings["THE_TWELFTH_COLUMN"] = ("The twelfth crossover column is a property of the " +
                "bounding BOX and of no row. Every x-raster row of the 10 x 6 block spans 112 bp " +
                "= 38.08 nm and every INTERFACE -- two adjacent row spans intersected, which is " +
                "what a crossover column actually serves -- spans 108 bp = 36.72 nm. Both give " +
                rowColumns + " columns at all three EDGE_MARGIN conventions, with " +
                columnSlack(rowDerived.rowWindows.first().nm(rise), pitch, 0.05).emitted(3) +
                " nm and " +
                columnSlack(rowDerived.interfaceWindows.first().nm(rise), pitch, 0.05).emitted(3) +
                " nm of slack past the last pitch. The box gives " + boxColumns + " at 0.05 nm " +
                "on 0.07 nm of slack and 11 at half a rise. So the guard is INERT on every " +
                "row-derived reading and decisive only on the box.")
        findings["THE_VERDICT_IT_MOVES"] = ("Reading the count off the rows selects C-0146's " +
                "116 bp / 11-column column, which is " + cells.count { it.flatAtSelected } +
                " flat cells of 8 against the twelve-column reading's " +
                cells.count { it.flatAtTwelve } + ". " + cells.count { it.decidedByTheGuard } +
                " cells are decided by the guard alone, and the tightest is C-0142's own -- the " +
                "3-column rim-graded cell at " +
                cells.first { it.columns == 3 && it.distribution == "rim-graded 5:1" }
                    .at116BpEleven.emitted(9) + " against " +
                cells.first { it.columns == 3 && it.distribution == "rim-graded 5:1" }
                    .at116BpTwelve.emitted(9) + ", straddling T-5b's 0.10. The RECOMMENDED cell " +
                "-- one column, ten paths, equal springs -- is flat at every reading.")
        findings["THE_STRICTEST_READING"] = ("A fourth reading exists and it is not chosen here: " +
                "the window every interior helix of the block shares is 104 bp = 35.36 nm and " +
                "gives 10 columns, again at all three margins at 112 / 108 -- though NOT at " +
                "102 / 109, where its 32.30 nm clears nine pitches by the same 0.07 nm the " +
                "bounding box clears eleven by, and F1 duly fires. It is the right window for a " +
                "crossover that must exist on EVERY helix of an interface; the interface reading " +
                "is the right one for a column that must exist on the ROWS it ties, which is what " +
                "a smeared grillage models. Both are guard-inert, which is the point.")
        findings["EQUIVALENCE"] = ("No per-beam axial window is needed in OrigamiGrillage, and the " +
                "reason is an identity rather than an approximation: at a two-length raster every " +
                "interface is between an even row and an odd one, so ALL nine interfaces of the " +
                "10 x 6 block carry the IDENTICAL window " +
                rowDerived.interfaceWindows.first().lowBasePairs.toString() + " to " +
                rowDerived.interfaceWindows.first().highBasePairs.toString() + " bp. A uniform " +
                "column lattice is therefore exact for the crossovers; what a single-lengthX " +
                "plate still cannot represent is the 4 bp of free overhang at alternating row " +
                "ends, and that material carries no crossover at either reading.")
    }.getOrElse { failure -> findings["PROSE_FAILED"] = failure.toString() }

    val result = T243Result(
        task = "T-243 - the crossover-column count from the row spans, not from a bounding-box edgeX",
        leaf = "A8.2",
        units = mapOf(
            "length" to "nm", "axialPosition" to "base pairs on one global z",
            "dishing" to "dimensionless, as a fraction of the free stroke",
            "pressure" to "pN/nm^2 (= MPa)"
        ),
        conventions = mapOf(
            "temperature" to "300 K, aqueous 2 mM MgCl2, k_BT = 4.142 pN nm",
            "pitch" to "the honeycomb per-interface crossover spacing 21 bp, halved as every four-layer study here halves it: 3.57 nm",
            "columnConstruction" to "CrossoverLayout.centred(count, pitch), count = floor((window - 2 m)/pitch) + 1",
            "window" to "four readings are carried: the bounding box, an x-raster row span, an interface (two adjacent row spans intersected) and every interior helix intersected",
            "load" to "C-0022's solved collar at 2 mM / 10 nm / 0.192 V; C-0001's secant foundation",
            "tolerance" to "T-5b's 0.10 of the free stroke"
        ),
        parameters = mapOf(
            "pitchNm" to pitch.emitted(9),
            "edgeMarginsSwept" to margins.joinToString(),
            "risePerBasePair" to rise.emitted(9),
            "lengthPairs" to pairs.joinToString { it.first.toString() + " / " + it.second },
            "crossSections" to sections.joinToString { it.first },
            "compositeFractions" to (T243_BAND_LOW.toString() + ", " + T243_BAND_MEASURED),
            "dishingSamplesPerSide" to T243_SAMPLES.toString(),
            "beamSubdivisions" to "2",
            "sources" to "gpd/results/T-3b-tile-edge-load-profile.json, gpd/results/T-235-coupled-cells-at-the-two-length-raster.json"
        ),
        sources = listOf(
            "gpd/results/T-3b-tile-edge-load-profile.json - C-0022's solved collar at the design state, read at 2 mM / 10 nm / 0.192 V",
            "gpd/results/T-235-coupled-cells-at-the-two-length-raster.json - C-0146's eight graded 10 x 6 cells at the three width/column readings, READ rather than transcribed, and asserted equal to the literals this study also carries"
        ),
        citedInputs = listOf(
            "C-0140 - the two-length raster's level walk, which supplies every window here",
            "C-0146 / CH-0185 - the bounding-box column table and the eight graded cells this selects among",
            "C-0142 - the tile, the collar and the grading machinery, consumed unmodified",
            "C-0141 - the honeycomb cross-section geometry",
            "C-0116 - the measured composite-fraction band"
        ),
        cheapBound = mapOf(
            "whatItSaid" to ("A crossover column serves an INTERFACE between two rows, so its " +
                    "window is the intersection of two row spans, not the union of every row. " +
                    "At 112 / 108 that is 108 bp = 36.72 nm against the box's 116 bp = 39.44 nm, " +
                    "and eleven pitches need 39.27 nm -- so the twelfth column needs 115.5 bp of " +
                    "shared window and no row has more than 112. One floor division per reading, " +
                    "no solve."),
            "cost" to "integer windows and one floor division; the five dishing states are one linear plate solve each",
            "rowDerivedColumns" to rowColumns.toString(),
            "interfaceDerivedColumns" to interfaceColumns.toString(),
            "boundingBoxColumns" to boxColumns.toString(),
            "guardInertOnEveryRowReadingAt112Over108" to gradedRowGuards.all { it.inert }.toString(),
            "guardInertOnEveryRowReadingOverBothPairs" to rowGuards.all { it.inert }.toString()
        ),
        windows = windows,
        guards = guards,
        dishing = dishing,
        cells = cells,
        reproductions = reproductions,
        falsifiers = falsifiers,
        findings = findings,
        validity = listOf(
            "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated.",
            "The dishing rows re-solve the SMEARED equivalent sheet; the grillage is still single-layer and still square-lattice in its crossover combinatorics (C-0141's own caveat).",
            "The eight cells are C-0146's Monte Carlo grading, READ rather than re-run. What moves here is which of its three columns a row-faithful column count selects.",
            "The window readings are exact only for C-0140's own level walk. CH-0187 and T-244 both re-open the length pair, and every window here moves with it: at 102 / 109 the interface window is 102 bp and the count is 10.",
            "EDGE_MARGIN is a numerical guard against a zero-length beam element and is not a physical clearance. Its inertness is a property of the geometry it is read at, and this is the second geometry at which it is not inert on the standing reading."
        ),
        openQuestions = listOf(
            "Which width reading section 3 names is a specification question (T-242), and it is upstream of this one: the count derived here is row-faithful whatever edgeX the plate is given, but the plate's own extent is not.",
            "At 102 / 109 -- the one length pair that closes on the scaffold-crossover rule (T-244) -- the interface window falls to 102 bp and the row-derived count to 10. Nothing has been graded there."
        )
    )

    println("  row-derived columns " + rowColumns + ", interface " + interfaceColumns +
            ", bounding box " + boxColumns)
    println("  guard inert on every row-derived reading at 112/108: " + gradedRowGuards.all { it.inert })
    println("  ... over both length pairs: " + rowGuards.all { it.inert })
    falsifiers.forEach { println("  " + it.name + " fired=" + it.fired) }
    reproductions.forEach { println("  reproduced " + it.reproduced + " - " + it.what) }

    val output = File("gpd/results/T-243-columns-from-row-spans.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(digits = 9) as JsonObject)
        ) + "\n"
    )
    println("T-243 - wrote " + output.path)
}
