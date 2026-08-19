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
import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.coupling.dropoutDishingSample
import com.xemantic.nano.plentyofroom.coupling.dropoutEnsemble
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.coupling.latticeInfluenceSurrogate
import com.xemantic.nano.plentyofroom.coupling.measuredDepthIncorporation
import com.xemantic.nano.plentyofroom.coupling.summariseDropoutDishing
import com.xemantic.nano.plentyofroom.coupling.winklerBendingLength
import com.xemantic.nano.plentyofroom.coupling.worstSinglePathRemoval
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
// T-235 -- re-grade the corrected 10 x 6 coupled cells at C-0140's TWO-LENGTH raster.
//
// C-0142 graded at a uniform 112 bp row and said so: "the ROW LENGTH is carried unchanged at
// 112 bp, and that is a fourth moved input this claim does NOT move". C-0140 moves it: a
// honeycomb x-raster carries BOTH turn senses, so no uniform row length exists, and its
// recommendation is 112 / 108 bp with a block extent of 116 bp = 39.44 nm, +3.57 % on the width
// C-0142 graded.
//
// The geometry is derived from C-0140's own path and level machinery (TwoLengthRaster); the
// grading machinery is C-0142's, unmodified.
// ---------------------------------------------------------------------------------------------

private const val T235_SAMPLES: Int = 81
private const val T235_TOLERANCE: Double = 0.10
private const val T235_RIM_STANDOFF: Double = 1.0
private const val T235_RIM_BAND: Double = 6.7
private const val T235_SENSE_ONE_BP: Int = 112
private const val T235_SENSE_TWO_BP: Int = 108
private const val T235_SEED: Long = 197_197L
private const val T235_BAND_LOW: Double = 0.26
private const val T235_BAND_MEASURED: Double = 0.30
private const val T235_LADDER_PHASE: Int = 0
private const val T235_ROW_OFFSET_BP: Int = 7
private const val T235_DECLARED_REALISATIONS: Int = 4000

/** The row-faithful width reading: every raster row is still 112 bp — `C-0142`'s own tile. */
private const val T235_ROW_WIDTH: String = "row length 112 bp"

/** The bounding-box width reading: the block's own axial extent, the dimension SS3 is owed. */
private const val T235_BLOCK_WIDTH: String = "block extent 116 bp"

/**
 * The realisation count, overridable **only** for a smoke run.
 *
 * `CLAUDE.md`: *"a toy-sample smoke run tests the plumbing and must NOT be read for a falsifier
 * verdict"*, so the emitted file declares `smokeRun` beside the count.
 */
private val t235Realisations: Int =
    System.getenv("T235_REALISATIONS")?.toIntOrNull() ?: T235_DECLARED_REALISATIONS

private fun Double.emitted(digits: Int = 9): String =
    if (!isFinite() || this == 0.0) toString()
    else java.math.BigDecimal(this).round(java.math.MathContext(digits)).toDouble().toString()

// ------------------------------------------------------------------------------ the records

@Serializable
private class T235RasterRow(
    val crossSection: String,
    val rasterRow: Int,
    val facePathIndex: Int,
    val senseIsDefined: Boolean,
    val effectiveSense: Int,
    val faceLengthBasePairs: Int,
    val faceLowBasePairs: Int,
    val faceHighBasePairs: Int,
    val rowSpanLowBasePairs: Int,
    val rowSpanHighBasePairs: Int,
    val rowSpanBasePairs: Int
)

@Serializable
private class T235Family(
    val crossSection: String,
    val senseOneBasePairs: Int,
    val senseTwoBasePairs: Int,
    val rowSpanBasePairs: Int,
    val blockExtentBasePairs: Int,
    val staggerBasePairs: Int,
    val blockExtentNm: Double,
    val rowSpanNm: Double
)

@Serializable
private class T235Census(
    val crossSection: String,
    val interRowOffsetBasePairs: Int,
    val basePhaseBasePairs: Int,
    val stationsPerRow: List<Int>,
    val sparsestRow: Int,
    val stationsOnFace: Int
)

@Serializable
private class T235ColumnCount(
    val widthReading: String,
    val edgeX: Double,
    val edgeMargin: Double,
    val edgeMarginConvention: String,
    val crossoverColumns: Int,
    val slackBeyondLastPitch: Double
)

@Serializable
private class T235Geometry(
    val crossSection: String,
    val widthReading: String,
    val rasterRows: Int,
    val layers: Int,
    val compositeFraction: Double,
    val edgeX: Double,
    val edgeY: Double,
    val edgeMargin: Double,
    val crossoverColumns: Int,
    val interiorPressure: Double,
    val freeStroke: Double,
    val reachAlong: Double,
    val reachAcross: Double,
    val edgeXOverReachAlong: Double
)

@Serializable
private class T235Reference(
    val crossSection: String,
    val widthReading: String,
    val edgeMargin: Double,
    val compositeFraction: Double,
    val uncoupledDishingOverStroke: Double,
    val flat: Boolean
)

@Serializable
private class T235Cell(
    val crossSection: String,
    val widthReading: String,
    val edgeX: Double,
    val edgeMargin: Double,
    val crossoverColumns: Int,
    val placement: String,
    val compositeFraction: Double,
    val columns: Int,
    val rows: Int,
    val pathCount: Int,
    val distribution: String,
    val perPathStiffness: Double,
    val totalStiffness: Double,
    val attachmentPitchAlong: Double,
    val attachmentPitchAcross: Double,
    val pitchAlongOverReach: Double,
    val pitchAcrossOverReach: Double,
    val alongHelixSnapDeparture: Double,
    val nominalOverStroke: Double,
    val worstSingleRemovalOverStroke: Double,
    val medianOverStroke: Double,
    val p90OverStroke: Double,
    val p95OverStroke: Double,
    val worstOverStroke: Double,
    val exceedance: Double,
    val exceedanceStandardError: Double,
    val exceedanceOneSidedBound: Double?,
    val meanSurvivors: Double,
    val flatAtNominal: Boolean,
    val flatAtP90: Boolean,
    val beatsUncoupledAtP90: Boolean
)

@Serializable
private class T235Paired(
    val crossSection: String,
    val placement: String,
    val columns: Int,
    val pathCount: Int,
    val distribution: String,
    val blockWidthP90: Double,
    val rowWidthP90: Double,
    val ratioOfPercentiles: Double,
    val medianOfRatios: Double,
    val p90OfRatios: Double,
    val bestRatio: Double,
    val worstRatio: Double,
    val fractionAbove: Double
)

@Serializable
private class T235Convergence(
    val axis: String,
    val values: List<Double>,
    val results: List<Double>,
    val departure: Double,
    val note: String
)

@Serializable
private class T235Reproduction(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double,
    val strict: Boolean
)

@Serializable
private class T235Falsifier(
    val id: String,
    val statement: String,
    val fired: Boolean,
    val evidence: String
)

@Serializable
private class T235Result(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: Map<String, String>,
    val parameters: Map<String, String>,
    val sources: List<String>,
    val citedInputs: List<String>,
    val cheapBound: Map<String, String>,
    val rasterRows: List<T235RasterRow>,
    val lengthPairFamily: List<T235Family>,
    val stationCensus: List<T235Census>,
    val columnCounts: List<T235ColumnCount>,
    val geometries: List<T235Geometry>,
    val references: List<T235Reference>,
    val cells: List<T235Cell>,
    val paired: List<T235Paired>,
    val verdict: Map<String, String>,
    val convergence: List<T235Convergence>,
    val reproductions: List<T235Reproduction>,
    val falsifiers: List<T235Falsifier>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

// ------------------------------------------------------------------------------ the load

private class T235Profile(
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, edgeX: Double, lengthY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, lengthY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T235_RIM_STANDOFF))
        )
}

private fun t235Profile(file: File): T235Profile {
    require(file.exists()) { "C-0022's result file is missing: " + file.path }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray
        .map { it.jsonObject }
        .firstOrNull { record ->
            fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at the design state")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T235Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

// ------------------------------------------------------------------------------ the tile

/**
 * A four-layer honeycomb tile at a stated axial width and a stated crossover-column guard.
 *
 * `C-0142`'s tile with two things lifted to parameters: `edgeX`, which is the whole of this
 * task, and `CrossoverLayout.EDGE_MARGIN`, because a `+3.57 %` width lands the derived column
 * count within 0.07 nm of that guard.
 */
private class T235Tile(
    val label: String,
    val widthReading: String,
    val rasterRows: Int,
    val layers: Int,
    val edgeX: Double,
    val edgeY: Double,
    val inPlanePitch: Double,
    val layerSpacing: Double,
    val compositeFraction: Double,
    val edgeMargin: Double,
    private val profile: T235Profile
) {

    init {
        require(abs(rasterRows * inPlanePitch - edgeY) < 1e-9 * edgeY) {
            "edgeY " + edgeY + " is not " + rasterRows + " rows at pitch " + inPlanePitch
        }
    }

    val rigidities: MultiLayerRigidities = multiLayerRigidities(
        layers = layers,
        interhelicalDistance = inPlanePitch,
        crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
        coupling = LayerCoupling.CALIBRATED,
        compositeFraction = compositeFraction,
        layerSpacing = layerSpacing
    )

    private val sheet = equivalentSheet(rigidities)

    val crossoverColumns: Int =
        crossoverColumnCount(edgeX, edgeMargin, sheet.crossoverSpacing / 2.0)

    val interiorPressure: Double = Gen1Tile.TARGET_FORCE / (edgeX * edgeY)

    val pressureField: PressureField = profile.field(interiorPressure, edgeX, edgeY)

    val freeStroke: Double = PlateOnFoundation(
        sheet.plate(edgeX, edgeY), Gen1Tile.FOUNDATION_SECANT, emptyList(), 12
    ).solve(uniformPressure(interiorPressure)).meanDeflection

    val reachAlong: Double =
        winklerBendingLength(rigidities.alongHelixRigidity, Gen1Tile.FOUNDATION_SECANT)

    val reachAcross: Double =
        winklerBendingLength(rigidities.acrossHelixRigidity, Gen1Tile.FOUNDATION_SECANT)

    val lattice: OrigamiGrillage by lazy {
        OrigamiGrillage(
            sheet = sheet,
            lengthX = edgeX,
            beamCount = rasterRows,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            columns = CrossoverLayout.centred(crossoverColumns, sheet.crossoverSpacing / 2.0),
            subdivisions = 2
        )
    }

    val uncoupledDishing: Double by lazy {
        lattice.solve(pressureField).peakDishing(T235_SAMPLES) / freeStroke
    }
}

/** One graded cell, with the dishing sample retained so the comparison can be **paired**. */
private class T235Graded(val cell: T235Cell, val sample: DoubleArray)

@Suppress("LongParameterList")
private fun gradeT235Cell(
    tile: T235Tile,
    columns: Int,
    grid: List<Pair<Double, Double>>,
    placement: String,
    snapDeparture: Double,
    distribution: String,
    stiffnesses: List<Double>,
    realisations: Int
): T235Graded {
    val surrogate = latticeInfluenceSurrogate(tile.lattice, grid, tile.pressureField, T235_SAMPLES)
    val incorporation = measuredDepthIncorporation(tile.edgeX, tile.edgeY)
    val ensemble = dropoutEnsemble(
        grid.map { (x, y) -> incorporation.at(x, y) }, realisations, T235_SEED
    )
    val nominal = surrogate.solve(stiffnesses).peakDishing / tile.freeStroke
    val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
    sample.indices.forEach { sample[it] = sample[it] / tile.freeStroke }
    val summary = summariseDropoutDishing(
        sample, nominal, ensemble.meanSurvivors, T235_TOLERANCE
    )
    return T235Graded(
        T235Cell(
            crossSection = tile.label,
            widthReading = tile.widthReading,
            edgeX = tile.edgeX,
            edgeMargin = tile.edgeMargin,
            crossoverColumns = tile.crossoverColumns,
            placement = placement,
            compositeFraction = tile.compositeFraction,
            columns = columns,
            rows = tile.rasterRows,
            pathCount = grid.size,
            distribution = distribution,
            perPathStiffness = stiffnesses.max(),
            totalStiffness = stiffnesses.sum(),
            attachmentPitchAlong = tile.edgeX / columns,
            attachmentPitchAcross = tile.edgeY / tile.rasterRows,
            pitchAlongOverReach = tile.edgeX / columns / tile.reachAlong,
            pitchAcrossOverReach = tile.edgeY / tile.rasterRows / tile.reachAcross,
            alongHelixSnapDeparture = snapDeparture,
            nominalOverStroke = nominal,
            worstSingleRemovalOverStroke =
                worstSinglePathRemoval(surrogate, stiffnesses) / tile.freeStroke,
            medianOverStroke = summary.median,
            p90OverStroke = summary.p90,
            p95OverStroke = summary.p95,
            worstOverStroke = summary.worst,
            exceedance = summary.exceedance,
            exceedanceStandardError = summary.exceedanceStandardError,
            exceedanceOneSidedBound = summary.exceedanceOneSidedBound,
            meanSurvivors = summary.meanSurvivors,
            flatAtNominal = nominal < T235_TOLERANCE,
            flatAtP90 = summary.flatAtP90,
            beatsUncoupledAtP90 = summary.p90 < tile.uncoupledDishing
        ),
        sample
    )
}

private fun t235Distributions(
    grid: List<Pair<Double, Double>>,
    edgeX: Double,
    edgeY: Double
): List<Pair<String, List<Double>>> = listOf(
    "equal springs" to equalShareOfMandate(grid.size),
    "rim-graded 5:1" to rimGradedShareOfMandate(
        grid.map { (x, y) ->
            val onRim = abs(x) > edgeX / 2.0 - T235_RIM_BAND || abs(y) > edgeY / 2.0 - T235_RIM_BAND
            if (onRim) 5.0 else 1.0
        }
    )
)

/** One graded configuration: a cross-section, a width, a guard, a placement and a fraction. */
private class T235Config(
    val crossSection: String,
    val rasterRows: Int,
    val layers: Int,
    val widthReading: String,
    val edgeMargin: Double,
    val placement: String,
    val compositeFraction: Double,
    val ladderPhase: Int = T235_LADDER_PHASE,
    val ladderOffset: Int = T235_ROW_OFFSET_BP
) {
    val key: String get() = crossSection + "|" + widthReading + "|" + edgeMargin + "|" +
            placement + "|" + compositeFraction
}

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val d = Gen1Tile.INTERHELICAL_HONEYCOMB
    val rise = Gen1Tile.RISE_PER_BASE_PAIR
    val rowPitch = HoneycombCrossSectionGeometry.rowPitch(d)
    val columnPitch = HoneycombCrossSectionGeometry.columnPitch(d)
    val profile = t235Profile(File("gpd/results/T-3b-tile-edge-load-profile.json"))
    val ladderPitch = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP * rise / 2.0

    // ------------------------------------------------------- the cheap bound, exact arithmetic
    val designs = listOf(Triple("15 x 4", 15, 4), Triple("10 x 6", 10, 6))
    val rasters = designs.associate {
        it.first to twoLengthRaster(it.second, it.third, T235_SENSE_ONE_BP, T235_SENSE_TWO_BP)
    }
    val rasterRowRecords = ArrayList<T235RasterRow>()
    println("T-235 - the cheap bound, before any Monte Carlo")
    designs.forEach { (name, m, _) ->
        val raster = rasters.getValue(name)
        println("  " + name + ": block extent " + raster.blockExtentBasePairs + " bp = " +
                raster.blockExtent().emitted(6) + " nm, senses " + raster.senseCounts.first +
                " / " + raster.senseCounts.second)
        (0 until m).forEach { row ->
            val face = raster.faceRows[row]
            val span = raster.rowSpans[row]
            rasterRowRecords += T235RasterRow(
                crossSection = name,
                rasterRow = row,
                facePathIndex = face.pathIndex,
                senseIsDefined = face.senseIsDefined,
                effectiveSense = face.effectiveSense,
                faceLengthBasePairs = face.lengthBasePairs,
                faceLowBasePairs = face.lowBasePairs,
                faceHighBasePairs = face.highBasePairs,
                rowSpanLowBasePairs = span.first,
                rowSpanHighBasePairs = span.second,
                rowSpanBasePairs = span.second - span.first
            )
        }
    }
    val rowSpansAllEqual = rasterRowRecords.filter { it.crossSection == "10 x 6" }
        .map { it.rowSpanBasePairs }.toSet()

    // C-0140's whole candidate family, so that the width finding does not rest on which pair
    // its own selection rule returns -- CH-0187 is challenging exactly that selection.
    val family = ArrayList<T235Family>()
    listOf(112 to 108, 101 to 109, 102 to 109, 112 to 109, 122 to 119).forEach { (a, b) ->
        designs.forEach { (name, m, n) ->
            val candidate = twoLengthRaster(m, n, a, b)
            val spans = candidate.rowSpans.map { it.second - it.first }.toSet()
            require(spans.size == 1) {
                "the " + a + " / " + b + " raster does not give every row one span: " + spans
            }
            family += T235Family(
                crossSection = name,
                senseOneBasePairs = a,
                senseTwoBasePairs = b,
                rowSpanBasePairs = spans.single(),
                blockExtentBasePairs = candidate.blockExtentBasePairs,
                staggerBasePairs = candidate.blockExtentBasePairs - spans.single(),
                blockExtentNm = candidate.blockExtent(),
                rowSpanNm = spans.single() * rise
            )
        }
    }
    println("  C-0140's candidate family, row span against block extent:")
    family.filter { it.crossSection == "10 x 6" }.forEach {
        println("    " + it.senseOneBasePairs + " / " + it.senseTwoBasePairs + " bp: every row " +
                it.rowSpanBasePairs + " bp, block " + it.blockExtentBasePairs + " bp, stagger " +
                it.staggerBasePairs + " bp")
    }

    val census = ArrayList<T235Census>()
    designs.forEach { (name, _, _) ->
        val raster = rasters.getValue(name)
        listOf(7, 14).forEach { offset ->
            (0 until HoneycombLattice.SAME_PAIR_PERIOD_BP).forEach { phase ->
                val sizes = raster.stationLattice(phase, offset).map { it.size }
                census += T235Census(name, offset, phase, sizes, sizes.min(), sizes.sum())
            }
        }
    }
    val saturating = census.filter { it.crossSection == "10 x 6" && it.stationsOnFace == 60 }
    println("  10 x 6 station census over 21 phases x 2 offsets: best " +
            census.filter { it.crossSection == "10 x 6" }.maxOf { it.stationsOnFace } +
            " of 60, saturating pairs " + saturating.size +
            (if (saturating.isEmpty()) "" else " at phase " + saturating.first().basePhaseBasePairs +
                    " / offset " + saturating.first().interRowOffsetBasePairs))

    val columnCounts = ArrayList<T235ColumnCount>()
    listOf(
        T235_ROW_WIDTH to T235_SENSE_ONE_BP,
        T235_BLOCK_WIDTH to rasters.getValue("10 x 6").blockExtentBasePairs
    ).forEach { (reading, bp) ->
        listOf(
            CrossoverLayout.EDGE_MARGIN to "the standing numerical guard",
            0.5 * rise to "half a base-pair rise",
            rise to "one base-pair rise"
        ).forEach { (margin, convention) ->
            val edgeX = bp * rise
            val count = crossoverColumnCount(edgeX, margin, ladderPitch)
            columnCounts += T235ColumnCount(
                widthReading = reading,
                edgeX = edgeX,
                edgeMargin = margin,
                edgeMarginConvention = convention,
                crossoverColumns = count,
                slackBeyondLastPitch = edgeX - 2.0 * margin - (count - 1) * ladderPitch
            )
        }
    }
    columnCounts.forEach {
        println("  columns at " + it.widthReading + ", guard " + it.edgeMargin.emitted(3) +
                " nm: " + it.crossoverColumns + " (slack " +
                it.slackBeyondLastPitch.emitted(3) + " nm)")
    }
    val guardMovesTheCount = columnCounts.filter { it.widthReading == T235_BLOCK_WIDTH }
        .map { it.crossoverColumns }.toSet().size > 1

    // the equal-length reduction, measured rather than asserted
    val reduction = run {
        val flat = twoLengthRaster(10, 6, T235_SENSE_ONE_BP, T235_SENSE_ONE_BP)
        (0 until HoneycombLattice.SAME_PAIR_PERIOD_BP).flatMap { phase ->
            listOf(7, 14).map { offset ->
                val mine = flat.stationLattice(phase, offset)
                val theirs = honeycombStationLattice(10, T235_SENSE_ONE_BP, phase, offset)
                if (mine.size != theirs.size ||
                    mine.indices.any { mine[it].size != theirs[it].size }
                ) Double.POSITIVE_INFINITY
                else mine.indices.maxOf { r ->
                    mine[r].indices.maxOf { abs(mine[r][it] - theirs[r][it]) }
                }
            }
        }.max()
    }
    println("  equal-length reduction to C-0141's lattice: worst departure " +
            reduction.emitted(2) + " nm over 42 (phase, offset) pairs")

    // ------------------------------------------------------------------ the graded configurations
    val halfRise = 0.5 * rise
    val configs = listOf(
        T235Config("10 x 6", 10, 6, T235_ROW_WIDTH, CrossoverLayout.EDGE_MARGIN,
            "abstract grid", T235_BAND_MEASURED),
        T235Config("10 x 6", 10, 6, T235_ROW_WIDTH, CrossoverLayout.EDGE_MARGIN,
            "abstract grid", T235_BAND_LOW),
        T235Config("10 x 6", 10, 6, T235_BLOCK_WIDTH, CrossoverLayout.EDGE_MARGIN,
            "abstract grid", T235_BAND_MEASURED),
        T235Config("10 x 6", 10, 6, T235_BLOCK_WIDTH, CrossoverLayout.EDGE_MARGIN,
            "abstract grid", T235_BAND_LOW),
        T235Config("10 x 6", 10, 6, T235_BLOCK_WIDTH, halfRise,
            "abstract grid", T235_BAND_MEASURED),
        T235Config("10 x 6", 10, 6, T235_BLOCK_WIDTH, halfRise,
            "abstract grid", T235_BAND_LOW),
        T235Config("10 x 6", 10, 6, T235_BLOCK_WIDTH, halfRise,
            "two-length station lattice", T235_BAND_MEASURED),
        T235Config("10 x 6", 10, 6, T235_ROW_WIDTH, CrossoverLayout.EDGE_MARGIN,
            "single-length station lattice", T235_BAND_MEASURED),
        T235Config("10 x 6", 10, 6, T235_BLOCK_WIDTH, CrossoverLayout.EDGE_MARGIN,
            "two-length station lattice", T235_BAND_MEASURED),
        T235Config("10 x 6", 10, 6, T235_BLOCK_WIDTH, CrossoverLayout.EDGE_MARGIN,
            "two-length station lattice, saturating phase", T235_BAND_MEASURED, 11, 14),
        T235Config("15 x 4", 15, 4, T235_ROW_WIDTH, CrossoverLayout.EDGE_MARGIN,
            "abstract grid", T235_BAND_MEASURED),
        T235Config("15 x 4", 15, 4, T235_BLOCK_WIDTH, CrossoverLayout.EDGE_MARGIN,
            "abstract grid", T235_BAND_MEASURED)
    )

    fun tileOf(config: T235Config): T235Tile {
        val bp = if (config.widthReading == T235_ROW_WIDTH) T235_SENSE_ONE_BP
        else rasters.getValue(config.crossSection).blockExtentBasePairs
        return T235Tile(
            label = config.crossSection,
            widthReading = config.widthReading,
            rasterRows = config.rasterRows,
            layers = config.layers,
            edgeX = bp * rise,
            edgeY = HoneycombBlock(config.rasterRows, config.layers, d).plateEdgeY,
            inPlanePitch = rowPitch,
            layerSpacing = columnPitch,
            compositeFraction = config.compositeFraction,
            edgeMargin = config.edgeMargin,
            profile = profile
        )
    }

    val geometries = ArrayList<T235Geometry>()
    val references = ArrayList<T235Reference>()
    val seenGeometry = HashSet<String>()
    configs.forEach { config ->
        val tile = tileOf(config)
        val id = config.crossSection + "|" + config.widthReading + "|" + config.edgeMargin +
                "|" + config.compositeFraction
        if (seenGeometry.add(id)) {
            geometries += T235Geometry(
                crossSection = config.crossSection,
                widthReading = config.widthReading,
                rasterRows = config.rasterRows,
                layers = config.layers,
                compositeFraction = config.compositeFraction,
                edgeX = tile.edgeX,
                edgeY = tile.edgeY,
                edgeMargin = tile.edgeMargin,
                crossoverColumns = tile.crossoverColumns,
                interiorPressure = tile.interiorPressure,
                freeStroke = tile.freeStroke,
                reachAlong = tile.reachAlong,
                reachAcross = tile.reachAcross,
                edgeXOverReachAlong = tile.edgeX / tile.reachAlong
            )
            references += T235Reference(
                crossSection = config.crossSection,
                widthReading = config.widthReading,
                edgeMargin = tile.edgeMargin,
                compositeFraction = config.compositeFraction,
                uncoupledDishingOverStroke = tile.uncoupledDishing,
                flat = tile.uncoupledDishing < T235_TOLERANCE
            )
        }
    }
    references.forEach {
        println("  uncoupled " + it.crossSection + "  " + it.widthReading + "  guard " +
                it.edgeMargin.emitted(3) + "  f = " + it.compositeFraction.emitted(3) +
                "  dishing " + it.uncoupledDishingOverStroke.emitted(9) +
                (if (it.flat) "  flat" else "  NOT FLAT"))
    }

    // ------------------------------------------------------------------ the grading
    val columnCountsGraded = listOf(1, 2, 3, 5)
    val cells = ArrayList<T235Cell>()
    val samples = HashMap<String, DoubleArray>()
    val refusals = ArrayList<String>()
    configs.forEach { config ->
        val tile = tileOf(config)
        val raster = rasters.getValue(config.crossSection)
        columnCountsGraded.forEach { columns ->
            val abstractGrid = attachmentGrid(columns, tile.rasterRows, tile.edgeX, tile.edgeY)
            val grid = try {
                when (config.placement) {
                    "abstract grid" -> abstractGrid
                    "single-length station lattice" -> honeycombSnappedGrid(
                        columns, tile.rasterRows, T235_SENSE_ONE_BP, tile.edgeY,
                        config.ladderPhase, config.ladderOffset
                    )
                    else -> twoLengthSnappedGrid(
                        raster, columns, tile.edgeY, config.ladderPhase, config.ladderOffset
                    )
                }
            } catch (e: IllegalArgumentException) {
                refusals += config.key + "|" + columns + " columns: " + e.message
                null
            } ?: return@forEach
            val snapDeparture = alongHelixDeparture(abstractGrid, grid)
            t235Distributions(grid, tile.edgeX, tile.edgeY).forEach { (label, stiffnesses) ->
                val graded = gradeT235Cell(
                    tile, columns, grid, config.placement, snapDeparture, label, stiffnesses,
                    t235Realisations
                )
                cells += graded.cell
                samples[config.key + "|" + columns + "|" + label] = graded.sample
                println("  " + config.crossSection + "  " + config.widthReading + "  guard " +
                        config.edgeMargin.emitted(3) + "  " + config.placement + "  f=" +
                        config.compositeFraction.emitted(3) + "  " + columns + " col x " +
                        tile.rasterRows + " = " + grid.size + " paths, " + label + "  p90 " +
                        graded.cell.p90OverStroke.emitted(9) +
                        (if (graded.cell.flatAtP90) "  FLAT at p90" else "  not flat at p90"))
            }
        }
    }

    // ------------------------------------------------------------------ the paired comparison
    fun configFor(
        crossSection: String,
        widthReading: String,
        placement: String,
        fraction: Double,
        margin: Double = CrossoverLayout.EDGE_MARGIN
    ): T235Config = configs.first {
        it.crossSection == crossSection && it.widthReading == widthReading &&
                it.placement == placement && it.compositeFraction == fraction &&
                it.edgeMargin == margin
    }

    val paired = ArrayList<T235Paired>()
    listOf(
        Triple("10 x 6", "abstract grid", "abstract grid"),
        Triple("15 x 4", "abstract grid", "abstract grid"),
        Triple("10 x 6", "two-length station lattice", "single-length station lattice")
    ).forEach { (crossSection, blockPlacement, rowPlacement) ->
        val blockConfig = configFor(crossSection, T235_BLOCK_WIDTH, blockPlacement, T235_BAND_MEASURED)
        val rowConfig = configFor(crossSection, T235_ROW_WIDTH, rowPlacement, T235_BAND_MEASURED)
        columnCountsGraded.forEach { columns ->
            listOf("equal springs", "rim-graded 5:1").forEach { label ->
                val wide = samples[blockConfig.key + "|" + columns + "|" + label]
                val narrow = samples[rowConfig.key + "|" + columns + "|" + label]
                if (wide == null || narrow == null) return@forEach
                val summary = pairedRatioSummary(wide, narrow)
                val wideCell = cells.first {
                    it.crossSection == crossSection && it.widthReading == T235_BLOCK_WIDTH &&
                            it.placement == blockPlacement && it.columns == columns &&
                            it.distribution == label &&
                            it.compositeFraction == T235_BAND_MEASURED &&
                            it.edgeMargin == CrossoverLayout.EDGE_MARGIN
                }
                val narrowCell = cells.first {
                    it.crossSection == crossSection && it.widthReading == T235_ROW_WIDTH &&
                            it.placement == rowPlacement && it.columns == columns &&
                            it.distribution == label &&
                            it.compositeFraction == T235_BAND_MEASURED
                }
                paired += T235Paired(
                    crossSection = crossSection,
                    placement = blockPlacement,
                    columns = columns,
                    pathCount = wideCell.pathCount,
                    distribution = label,
                    blockWidthP90 = wideCell.p90OverStroke,
                    rowWidthP90 = narrowCell.p90OverStroke,
                    ratioOfPercentiles = summary.ratioOfPercentiles,
                    medianOfRatios = summary.median,
                    p90OfRatios = summary.p90,
                    bestRatio = summary.best,
                    worstRatio = summary.worst,
                    fractionAbove = summary.fractionAbove
                )
            }
        }
    }

    // ------------------------------------------------------------------ the verdict sets
    val blockCells = cells.filter {
        it.crossSection == "10 x 6" && it.widthReading == T235_BLOCK_WIDTH &&
                it.placement == "abstract grid" &&
                it.compositeFraction == T235_BAND_MEASURED &&
                it.edgeMargin == CrossoverLayout.EDGE_MARGIN
    }
    val rowCells = cells.filter {
        it.crossSection == "10 x 6" && it.widthReading == T235_ROW_WIDTH &&
                it.placement == "abstract grid" && it.compositeFraction == T235_BAND_MEASURED
    }
    val blockLowCells = cells.filter {
        it.crossSection == "10 x 6" && it.widthReading == T235_BLOCK_WIDTH &&
                it.placement == "abstract grid" && it.compositeFraction == T235_BAND_LOW &&
                it.edgeMargin == CrossoverLayout.EDGE_MARGIN
    }
    val guardCells = cells.filter {
        it.crossSection == "10 x 6" && it.widthReading == T235_BLOCK_WIDTH &&
                it.edgeMargin == halfRise && it.placement == "abstract grid" &&
                it.compositeFraction == T235_BAND_MEASURED
    }
    val guardLowCells = cells.filter {
        it.crossSection == "10 x 6" && it.widthReading == T235_BLOCK_WIDTH &&
                it.edgeMargin == halfRise && it.placement == "abstract grid" &&
                it.compositeFraction == T235_BAND_LOW
    }
    val guardLatticeCells = cells.filter {
        it.widthReading == T235_BLOCK_WIDTH && it.edgeMargin == halfRise &&
                it.placement == "two-length station lattice"
    }
    val twoLengthCells = cells.filter {
        it.placement.startsWith("two-length station lattice") &&
                it.edgeMargin == CrossoverLayout.EDGE_MARGIN
    }
    val twoLengthAtPhaseZero = twoLengthCells.filter { it.placement == "two-length station lattice" }
    val twoLengthAtSaturating = twoLengthCells.filter { it.placement.endsWith("saturating phase") }
    val singleLengthCells = cells.filter { it.placement == "single-length station lattice" }
    val block154Cells = cells.filter {
        it.crossSection == "15 x 4" && it.widthReading == T235_BLOCK_WIDTH
    }
    val bestBlock = blockCells.minBy { it.p90OverStroke }
    val bestRow = rowCells.minBy { it.p90OverStroke }
    val bestTwoLength = twoLengthCells.minBy { it.p90OverStroke }

    // ------------------------------------------------------------------ convergence
    val convergenceConfig = configFor("10 x 6", T235_BLOCK_WIDTH, "abstract grid", T235_BAND_MEASURED)
    val convergenceTile = tileOf(convergenceConfig)
    val realisationSweep = listOf(
        t235Realisations / 4, t235Realisations / 2, t235Realisations
    ).map { n ->
        val grid = attachmentGrid(
            bestBlock.columns, convergenceTile.rasterRows, convergenceTile.edgeX,
            convergenceTile.edgeY
        )
        val stiffnesses = t235Distributions(grid, convergenceTile.edgeX, convergenceTile.edgeY)
            .first { it.first == bestBlock.distribution }.second
        gradeT235Cell(
            convergenceTile, bestBlock.columns, grid, "abstract grid", 0.0,
            bestBlock.distribution, stiffnesses, n
        ).cell.p90OverStroke
    }
    val convergence = listOf(
        T235Convergence(
            axis = "dropout realisations, the best block-width cell's 90th percentile",
            values = listOf(
                t235Realisations / 4.0, t235Realisations / 2.0, t235Realisations.toDouble()
            ),
            results = realisationSweep,
            departure = abs(realisationSweep[2] - realisationSweep[1]) / abs(realisationSweep[2]),
            note = "one COMMON stream restricted, not three independent draws, so the departure " +
                    "is a convergence and not a variance"
        ),
        T235Convergence(
            axis = "CrossoverLayout.EDGE_MARGIN, the crossover-column count at the block width",
            values = columnCounts.filter { it.widthReading == T235_BLOCK_WIDTH }
                .map { it.edgeMargin },
            results = columnCounts.filter { it.widthReading == T235_BLOCK_WIDTH }
                .map { it.crossoverColumns.toDouble() },
            departure = if (guardCells.isEmpty() || blockCells.isEmpty()) 0.0
            else abs(guardCells.maxOf { it.p90OverStroke } - blockCells.maxOf { it.p90OverStroke }) /
                    blockCells.maxOf { it.p90OverStroke },
            note = "a numerical guard is not a convergence parameter, and this axis is here " +
                    "because the 116 bp extent clears eleven honeycomb pitches by 0.07 nm"
        )
    )

    // ------------------------------------------------------------------ reproductions
    val reproductions = ArrayList<T235Reproduction>()
    fun reproduceCells(
        source: String,
        label: String,
        set: List<T235Cell>,
        published: Map<String, Double>
    ) {
        set.forEach { cell ->
            val id = cell.columns.toString() + "|" + cell.distribution
            val value = published[id] ?: return@forEach
            reproductions += T235Reproduction(
                source = source,
                quantity = label + ", " + id,
                published = value,
                reproduced = cell.p90OverStroke,
                departure = abs(cell.p90OverStroke - value) / abs(value),
                strict = true
            )
        }
    }
    reproduceCells(
        "C-0142", "10 x 6 abstract grid 90th percentile at f = 0.30", rowCells,
        mapOf(
            "1|equal springs" to 0.0680677948,
            "1|rim-graded 5:1" to 0.102582764,
            "2|equal springs" to 0.119502047,
            "2|rim-graded 5:1" to 0.168817101,
            "3|equal springs" to 0.101905503,
            "3|rim-graded 5:1" to 0.0954158305,
            "5|equal springs" to 0.0900369,
            "5|rim-graded 5:1" to 0.0822611821
        )
    )
    reproduceCells(
        "C-0142", "10 x 6 abstract grid 90th percentile at f = 0.26",
        cells.filter {
            it.crossSection == "10 x 6" && it.widthReading == T235_ROW_WIDTH &&
                    it.placement == "abstract grid" && it.compositeFraction == T235_BAND_LOW
        },
        mapOf(
            "1|equal springs" to 0.072431426,
            "3|rim-graded 5:1" to 0.0968178426,
            "5|equal springs" to 0.0923901454,
            "5|rim-graded 5:1" to 0.0832291872
        )
    )
    reproduceCells(
        "C-0142", "10 x 6 single-length lattice 90th percentile at f = 0.30", singleLengthCells,
        mapOf(
            "1|equal springs" to 0.0863028445,
            "1|rim-graded 5:1" to 0.111376749,
            "2|equal springs" to 0.125476912,
            "2|rim-graded 5:1" to 0.183045719,
            "3|equal springs" to 0.0973238201,
            "3|rim-graded 5:1" to 0.14299002,
            "5|equal springs" to 0.0868937148,
            "5|rim-graded 5:1" to 0.108415983
        )
    )
    reproduceCells(
        "C-0142", "15 x 4 abstract grid 90th percentile at f = 0.30",
        cells.filter {
            it.crossSection == "15 x 4" && it.widthReading == T235_ROW_WIDTH
        },
        mapOf(
            "1|equal springs" to 0.213735801,
            "1|rim-graded 5:1" to 0.304635002,
            "2|equal springs" to 0.250904784,
            "2|rim-graded 5:1" to 0.336722611,
            "3|equal springs" to 0.219381554,
            "3|rim-graded 5:1" to 0.178613247,
            "5|equal springs" to 0.198234404,
            "5|rim-graded 5:1" to 0.145354102
        )
    )
    listOf(
        Triple("C-0141 / C-0142", "10 x 6|0.3", 0.0240648102),
        Triple("C-0141 / C-0142", "10 x 6|0.26", 0.0255589305),
        Triple("C-0141 / C-0142", "15 x 4|0.3", 0.0978155002)
    ).forEach { (source, id, published) ->
        val parts = id.split("|")
        val reference = references.firstOrNull {
            it.crossSection == parts[0] && it.widthReading == T235_ROW_WIDTH &&
                    it.edgeMargin == CrossoverLayout.EDGE_MARGIN &&
                    abs(it.compositeFraction - parts[1].toDouble()) < 1e-12
        } ?: return@forEach
        reproductions += T235Reproduction(
            source = source,
            quantity = "uncoupled free-tile dishing at the 112 bp width, " + id,
            published = published,
            reproduced = reference.uncoupledDishingOverStroke,
            departure = abs(reference.uncoupledDishingOverStroke - published) / abs(published),
            strict = true
        )
    }
    listOf(
        Triple("C-0140", "block axial extent of the 112 / 108 raster, base pairs", 116.0),
        Triple("C-0140", "sense-1 helices of design (i), 15 x 4", 28.0),
        Triple("C-0140", "sense-2 helices of design (i), 15 x 4", 30.0),
        Triple("C-0140", "sense-1 helices of design (ii), 10 x 6", 29.0)
    ).forEach { (source, quantity, published) ->
        val reproduced = when {
            quantity.startsWith("block") ->
                rasters.getValue("10 x 6").blockExtentBasePairs.toDouble()
            quantity.contains("15 x 4") && quantity.startsWith("sense-1") ->
                rasters.getValue("15 x 4").senseCounts.first.toDouble()
            quantity.contains("15 x 4") -> rasters.getValue("15 x 4").senseCounts.second.toDouble()
            else -> rasters.getValue("10 x 6").senseCounts.first.toDouble()
        }
        reproductions += T235Reproduction(
            source = source, quantity = quantity, published = published,
            reproduced = reproduced, departure = abs(reproduced - published) / abs(published),
            strict = true
        )
    }
    reproductions.forEach {
        println("  reproduce " + it.source + " " + it.quantity + ": " +
                it.reproduced.emitted(9) + " against " + it.published.emitted(9) +
                ", departure " + it.departure.emitted(2))
    }

    // ------------------------------------------------------------------ falsifiers
    val worstC0142 = reproductions.filter { it.source == "C-0142" }.maxOf { it.departure }
    val flatBlockSet = blockCells.filter { it.flatAtP90 }
        .map { it.columns.toString() + "|" + it.distribution }.toSet()
    val flatGuardSet = guardCells.filter { it.flatAtP90 }
        .map { it.columns.toString() + "|" + it.distribution }.toSet()
    val f7Offenders = paired.filter {
        (it.medianOfRatios - 1.0) * (it.ratioOfPercentiles - 1.0) < 0.0
    }
    val flatTwoLengthEqual = twoLengthCells.filter { it.flatAtP90 }
        .all { it.distribution == "equal springs" }
    val falsifiers = listOf(
        T235Falsifier(
            "F1",
            "the 112 bp row-length reading does NOT reproduce C-0142's published cells, in " +
                    "which case nothing here is a re-reading of that claim",
            worstC0142 > 1e-6,
            "worst departure over " + reproductions.count { it.source == "C-0142" } +
                    " reproduced C-0142 cells: " + worstC0142.emitted(2)
        ),
        T235Falsifier(
            "F2",
            "the two-length station lattice does not reduce to C-0141's honeycombStationLattice " +
                    "when both lengths are equal, in which case the generalisation is not one",
            reduction > 1e-12,
            "worst position departure over 21 phases x 2 offsets: " + reduction.emitted(2) + " nm"
        ),
        T235Falsifier(
            "F3",
            "the recommended design loses T-5b's 0.10 at the two-length raster at either end of " +
                    "the measured band, in which case C-0142's recommendation does not survive " +
                    "its own buildable width -- DECLARED OPEN",
            !(bestBlock.flatAtP90 && blockLowCells.minBy { it.p90OverStroke }.flatAtP90),
            "best block-width cell at f = 0.30: " + bestBlock.p90OverStroke.emitted(9) +
                    (if (bestBlock.flatAtP90) " FLAT" else " NOT FLAT") + "; at f = 0.26: " +
                    blockLowCells.minBy { it.p90OverStroke }.p90OverStroke.emitted(9) +
                    (if (blockLowCells.minBy { it.p90OverStroke }.flatAtP90) " FLAT"
                    else " NOT FLAT")
        ),
        T235Falsifier(
            "F4",
            "a graded column count is REFUSED by the two-length station lattice, in which case " +
                    "the two-length raster costs a path count and not only a width",
            refusals.isNotEmpty(),
            refusals.size.toString() + " of " + (configs.size * columnCountsGraded.size) +
                    " (configuration, column count) pairs were refused" +
                    (if (refusals.isEmpty()) "" else ": " + refusals.joinToString("; "))
        ),
        T235Falsifier(
            "F5",
            "the flatness verdict set moves with CrossoverLayout.EDGE_MARGIN, in which case a " +
                    "numerical guard is deciding a flatness reading -- DECLARED OPEN",
            flatBlockSet != flatGuardSet,
            "the guard moves the column count: " + guardMovesTheCount + "; flat at the standing " +
                    "guard " + flatBlockSet.size + " of " + blockCells.size + ", at half a rise " +
                    flatGuardSet.size + " of " + guardCells.size +
                    (if (flatBlockSet == flatGuardSet) " -- the SAME cells"
                    else " -- flat only at twelve columns: {" +
                            (flatBlockSet - flatGuardSet).joinToString(", ") +
                            "}, flat only at eleven: {" +
                            (flatGuardSet - flatBlockSet).joinToString(", ") + "}")
        ),
        T235Falsifier(
            "F6",
            "the equal-spring advantage C-0142 measured on its lattice-snapped cells does not " +
                    "persist at the two-length lattice -- DECLARED OPEN",
            !flatTwoLengthEqual,
            twoLengthCells.count { it.flatAtP90 }.toString() + " of " + twoLengthCells.size +
                    " two-length lattice cells are flat, of which " +
                    twoLengthCells.count { it.flatAtP90 && it.distribution == "equal springs" } +
                    " are equal springs"
        ),
        T235Falsifier(
            "F7",
            "the paired and unpaired readings of the width's cost disagree in SIGN, in which " +
                    "case the quoted summary does not describe its sample",
            f7Offenders.isNotEmpty(),
            f7Offenders.size.toString() + " of " + paired.size + " paired rows disagree in sign"
        )
    )
    falsifiers.forEach {
        println("  " + it.id + (if (it.fired) " FIRED   " else " did not fire   ") + it.evidence)
    }

    // ------------------------------------------------------------------ findings
    val findings = HashMap<String, String>()
    findings["theCheapBound"] =
        "C-0140's 112 / 108 bp raster puts EVERY x-raster row of the 10 x 6 block at " +
                rowSpansAllEqual.joinToString(", ") + " base pairs and offsets consecutive rows " +
                "axially by " +
                (rasterRowRecords.filter { it.crossSection == "10 x 6" }
                    .map { it.rowSpanLowBasePairs }.distinct()
                    .let { abs(it.max() - it.min()) }) + " bp. The block's extent is " +
                rasters.getValue("10 x 6").blockExtentBasePairs + " bp = " +
                rasters.getValue("10 x 6").blockExtent().emitted(6) + " nm only BECAUSE the rows " +
                "are staggered, not because any row is longer -- so the +3.57 % width question " +
                "splits into two readings, a row-faithful 112 bp and a bounding-box " +
                rasters.getValue("10 x 6").blockExtentBasePairs + " bp, and the row-faithful one " +
                "is C-0142's own tile. The face's rooting helices alternate sense EXACTLY with " +
                "the raster row parity, so the station lattice is still a two-phase object. " +
                "And it is not a property of the pair C-0140 selects: over all five pairs its " +
                "own table carries, and at both cross-sections, every raster row spans the " +
                "LARGER of the two lengths exactly and the block extent exceeds it by exactly " +
                "the stagger -- so no two-length raster lengthens a row at all."
    findings["theStationCensus"] =
        "The 21 bp ladder becomes ROW-DEPENDENT: at C-0142's own phase 0 / 7 bp offset the " +
                "two-length face carries " +
                census.first {
                    it.crossSection == "10 x 6" && it.basePhaseBasePairs == 0 &&
                            it.interRowOffsetBasePairs == 7
                }.stationsOnFace + " stations of 60 against the uniform raster's 60, and over " +
                "the whole 21-phase x 2-offset sweep exactly " + saturating.size +
                " pair(s) keep all sixty" +
                (if (saturating.isEmpty()) "" else " -- phase " +
                        saturating.first().basePhaseBasePairs + " at the " +
                        saturating.first().interRowOffsetBasePairs + " bp offset") +
                ". C-0141 records that no answer of its own depends on the 7-or-14 bp offset " +
                "choice; at a two-length raster that stops being true. Every graded column " +
                "count (1, 2, 3, 5) is still realisable, so the raster costs stations and not " +
                "paths."
    findings["theVerdict"] =
        "Of " + blockCells.size + " 10 x 6 coupled cells re-read at the two-length raster's " +
                "bounding-box width at the measured f = 0.30, " +
                blockCells.count { it.flatAtP90 } + " are flat at the 90th percentile against " +
                rowCells.count { it.flatAtP90 } + " of " + rowCells.size +
                " at C-0142's 112 bp width. At the band's adverse low end f = 0.26 it is " +
                blockLowCells.count { it.flatAtP90 } + " of " + blockLowCells.size +
                ". The best block-width cell is " + bestBlock.columns + " column(s), " +
                bestBlock.distribution + ", p90 = " + bestBlock.p90OverStroke.emitted() +
                " against C-0142's " + bestRow.p90OverStroke.emitted() + " at the same cell."
    findings["theWidthCost"] =
        "Read PER REALISATION on the shared stream, the block width costs the 10 x 6 tile a " +
                "median ratio of " +
                paired.filter { it.crossSection == "10 x 6" }.minOf { it.medianOfRatios }
                    .emitted(6) + " to " +
                paired.filter { it.crossSection == "10 x 6" }.maxOf { it.medianOfRatios }
                    .emitted(6) + ", against a ratio of 90th percentiles of " +
                paired.filter { it.crossSection == "10 x 6" }.minOf { it.ratioOfPercentiles }
                    .emitted(6) + " to " +
                paired.filter { it.crossSection == "10 x 6" }.maxOf { it.ratioOfPercentiles }
                    .emitted(6) + ". CLAUDE.md: a ratio of two ORDER STATISTICS is not the order " +
                "statistic of the ratio."
    findings["theGuard"] =
        "A +3.57 % axial extent clears eleven honeycomb crossover pitches by " +
                columnCounts.first {
                    it.widthReading == T235_BLOCK_WIDTH &&
                            it.edgeMargin == CrossoverLayout.EDGE_MARGIN
                }.slackBeyondLastPitch.emitted(3) + " nm, so the column count is " +
                columnCounts.first {
                    it.widthReading == T235_BLOCK_WIDTH &&
                            it.edgeMargin == CrossoverLayout.EDGE_MARGIN
                }.crossoverColumns + " at the standing guard and " +
                columnCounts.first {
                    it.widthReading == T235_BLOCK_WIDTH && it.edgeMargin == halfRise
                }.crossoverColumns + " at half a base-pair rise. Swept: the flat cell set is " +
                (if (flatBlockSet == flatGuardSet) "IDENTICAL" else "DIFFERENT") +
                " at the two guards, and the worst cell moves " +
                (if (guardCells.isEmpty()) "n/a"
                else (abs(guardCells.maxOf { c -> c.p90OverStroke } -
                        blockCells.maxOf { c -> c.p90OverStroke }) /
                        blockCells.maxOf { c -> c.p90OverStroke }).emitted(2)) + " relative."
    findings["theLatticePlacement"] =
        "On the two-length station lattice at C-0142's own phase 0 / 7 bp offset, " +
                twoLengthAtPhaseZero.count { it.flatAtP90 } + " of " +
                twoLengthAtPhaseZero.size + " cells are flat, and at the one station-saturating " +
                "phase (11 / 14 bp) " + twoLengthAtSaturating.count { it.flatAtP90 } + " of " +
                twoLengthAtSaturating.size + ", against C-0142's " +
                singleLengthCells.count { it.flatAtP90 } + " of " + singleLengthCells.size +
                " on the single-length lattice. The best is " + bestTwoLength.columns +
                " column(s), " + bestTwoLength.distribution + ", p90 = " +
                bestTwoLength.p90OverStroke.emitted() + "."
    findings["theDecomposition"] =
        "The two effects of a +3.57 % axial extent are OPPOSED and the numerical guard picks " +
                "which wins. At a MATCHED eleven crossover columns the wider tile is worse -- " +
                "the uncoupled reference goes " +
                references.first {
                    it.crossSection == "10 x 6" && it.widthReading == T235_ROW_WIDTH &&
                            it.compositeFraction == T235_BAND_MEASURED
                }.uncoupledDishingOverStroke.emitted() + " to " +
                references.first {
                    it.crossSection == "10 x 6" && it.widthReading == T235_BLOCK_WIDTH &&
                            it.edgeMargin == halfRise &&
                            it.compositeFraction == T235_BAND_MEASURED
                }.uncoupledDishingOverStroke.emitted() + " -- and the TWELFTH crossover column " +
                "the standing guard admits takes it to " +
                references.first {
                    it.crossSection == "10 x 6" && it.widthReading == T235_BLOCK_WIDTH &&
                            it.edgeMargin == CrossoverLayout.EDGE_MARGIN &&
                            it.compositeFraction == T235_BAND_MEASURED
                }.uncoupledDishingOverStroke.emitted() + ", below the 112 bp reading. The " +
                "coupled flat count runs 4 of 8 at 112 bp, " + guardCells.count { it.flatAtP90 } +
                " of 8 at 116 bp on eleven columns and " + blockCells.count { it.flatAtP90 } +
                " of 8 on twelve -- and every raster ROW is 112 bp, so eleven is the count a " +
                "row can carry and twelve is the count the BOUNDING BOX admits."
    findings["theOtherCrossSection"] =
        "15 x 4 is " + block154Cells.count { it.flatAtP90 } + " of " + block154Cells.size +
                " at the two-length raster's width, against 0 of 8 at C-0142's -- the direction " +
                "the task asked for, and it does not move."

    val result = T235Result(
        task = "T-235",
        leaf = "A8.2",
        title = "C-0142's corrected 10 x 6 coupled cells, re-read at C-0140's two-length raster",
        verificationType = "logical (an exact integer construction of the two-length raster's " +
                "axial levels, station windows and census, derived from C-0140's own machinery) " +
                "+ in-silico (C-0142's influence surrogate and Monte Carlo dropout grading, on " +
                "one common stream)",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated. The " +
                "FOLDING statistics graded against are measured; the flatness is not, and the " +
                "raster is a LATTICE statement.",
        units = mapOf(
            "length" to "nm",
            "axialPosition" to "base pairs on one global z, C-0140's convention",
            "stiffness" to "pN/nm",
            "dishing" to "dimensionless, as a fraction of the free-tile stroke",
            "compositeFraction" to "dimensionless"
        ),
        conventions = mapOf(
            "raster" to "C-0140's two-length x-raster: 112 bp at effective sense 1, 108 bp at " +
                    "effective sense 2, first axial sign +1, unmirrored",
            "widthReadings" to "row length 112 bp (every raster row's own span, C-0142's tile) " +
                    "and block extent 116 bp (the block's axial bounding box, the dimension SS3 " +
                    "is owed)",
            "crossSection" to "C-0141's honeycomb: in-plane row pitch 3d/2, layer pitch " +
                    "d sqrt(3)/2, cell 3 sqrt(3)/4 d^2",
            "mandate" to "C-0017's equality on the SUM, SS3's acceptable clause: 100 pN / 3 nm",
            "load" to "C-0022's solved collar at 2 mM / 10 nm / 0.192 V",
            "dropout" to "C-0087's measured per-site staple incorporation, depth convention",
            "flat" to "peak dishing below T-5b's 0.10, read at the 90th percentile",
            "stationLattice" to "the 21 bp ladder on one global z, phase measured from the " +
                    "block's own low plane, with C-0141's FORCED 7 or 14 bp inter-row stagger",
            "axes" to "x along the helices, y across them, origin at the tile axial centre"
        ),
        parameters = mapOf(
            "realisations" to t235Realisations.toString(),
            "declaredRealisations" to T235_DECLARED_REALISATIONS.toString(),
            "smokeRun" to (t235Realisations != T235_DECLARED_REALISATIONS).toString(),
            "seed" to T235_SEED.toString(),
            "dishingSamplesPerSide" to T235_SAMPLES.toString(),
            "tolerance" to T235_TOLERANCE.toString(),
            "mandatedTotalStiffness" to MANDATED_TOTAL_STIFFNESS.emitted(),
            "senseOneBasePairs" to T235_SENSE_ONE_BP.toString(),
            "senseTwoBasePairs" to T235_SENSE_TWO_BP.toString(),
            "blockExtentBasePairs" to
                    rasters.getValue("10 x 6").blockExtentBasePairs.toString(),
            "bondLength" to d.emitted(),
            "ladderPhaseBasePairs" to T235_LADDER_PHASE.toString(),
            "interRowOffsetBasePairs" to T235_ROW_OFFSET_BP.toString(),
            "saturatingPhaseAndOffset" to
                    (if (saturating.isEmpty()) "none"
                    else saturating.first().basePhaseBasePairs.toString() + " / " +
                            saturating.first().interRowOffsetBasePairs),
            "rimBandWidth" to T235_RIM_BAND.toString(),
            "compositeFractions" to (T235_BAND_LOW.toString() + ", " + T235_BAND_MEASURED),
            "beamSubdivisions" to "2",
            "edgeMarginsSwept" to columnCounts.filter { it.widthReading == T235_BLOCK_WIDTH }
                .joinToString(", ") { it.edgeMargin.emitted(3) },
            // `P-22`'s declaration convention: the task ids this study READS.
            "sources" to "gpd/results/T-3b-tile-edge-load-profile.json"
        ),
        sources = listOf(
            "gpd/results/T-3b-tile-edge-load-profile.json - C-0022's solved collar at the design " +
                    "state, read at 2 mM / 10 nm / 0.192 V"
        ),
        citedInputs = listOf(
            "C-0142 - the eight 10 x 6 coupled cells this re-reads, and its own 15 x 4 controls",
            "C-0140 - the two-length raster, its turn senses and its 116 bp block extent",
            "C-0141 - the honeycomb cross-section and the single-length station lattice",
            "C-0116 - the composite-fraction threshold and the measured 0.26-0.33 band",
            "C-0017 - the mandate, an equality on the SUM",
            "C-0087 - the measured per-site staple incorporation",
            "C-0058 - the rim grading",
            "C-0129 - the one-sided bound on a saturated proportion",
            "C-0103 - the common-random-number discipline"
        ),
        cheapBound = mapOf(
            "blockExtentBasePairs" to rasters.getValue("10 x 6").blockExtentBasePairs.toString(),
            "blockExtentNm" to rasters.getValue("10 x 6").blockExtent().emitted(),
            "everyRasterRowBasePairs" to rowSpansAllEqual.joinToString(", "),
            "attachmentPitchAcross" to "edgeY / rasterRows, UNCHANGED by a row-length move",
            "stationsAtC0142Phase" to census.first {
                it.crossSection == "10 x 6" && it.basePhaseBasePairs == 0 &&
                        it.interRowOffsetBasePairs == 7
            }.stationsOnFace.toString(),
            "saturatingPairs" to saturating.size.toString(),
            "columnCountAtStandingGuard" to columnCounts.first {
                it.widthReading == T235_BLOCK_WIDTH &&
                        it.edgeMargin == CrossoverLayout.EDGE_MARGIN
            }.crossoverColumns.toString(),
            "whatItSaid" to findings["theCheapBound"]!!
        ),
        rasterRows = rasterRowRecords,
        lengthPairFamily = family,
        stationCensus = census,
        columnCounts = columnCounts,
        geometries = geometries,
        references = references,
        cells = cells,
        paired = paired,
        verdict = mapOf(
            "blockWidthCellsGraded" to blockCells.size.toString(),
            "blockWidthFlatAtP90" to blockCells.count { it.flatAtP90 }.toString(),
            "rowWidthFlatAtP90" to rowCells.count { it.flatAtP90 }.toString(),
            "blockWidthFlatAtBandLow" to blockLowCells.count { it.flatAtP90 }.toString(),
            "guardSweepFlatAtP90" to guardCells.count { it.flatAtP90 }.toString(),
            "guardSweepFlatAtBandLow" to guardLowCells.count { it.flatAtP90 }.toString(),
            "guardSweepLatticeFlatAtP90" to guardLatticeCells.count { it.flatAtP90 }.toString(),
            "twoLengthLatticeFlatAtP90" to twoLengthCells.count { it.flatAtP90 }.toString(),
            "singleLengthLatticeFlatAtP90" to singleLengthCells.count { it.flatAtP90 }.toString(),
            "crossSection15x4FlatAtP90" to block154Cells.count { it.flatAtP90 }.toString(),
            "bestBlockWidthColumns" to bestBlock.columns.toString(),
            "bestBlockWidthDistribution" to bestBlock.distribution,
            "bestBlockWidthP90" to bestBlock.p90OverStroke.emitted(),
            "bestRowWidthP90" to bestRow.p90OverStroke.emitted(),
            "bestTwoLengthLatticeP90" to bestTwoLength.p90OverStroke.emitted(),
            "bestBlockWidthAtElevenColumnsP90" to
                    (if (guardCells.isEmpty()) "n/a"
                    else guardCells.minBy { it.p90OverStroke }.p90OverStroke.emitted()),
            "recommendationSurvivesItsBuildableWidth" to
                    (bestBlock.flatAtP90 && blockLowCells.any { it.flatAtP90 } &&
                            guardCells.minBy { it.p90OverStroke }.flatAtP90 &&
                            guardLowCells.minBy { it.p90OverStroke }.flatAtP90).toString()
        ),
        convergence = convergence,
        reproductions = reproductions,
        falsifiers = falsifiers,
        findings = findings,
        validity = listOf(
            "The raster is a LATTICE statement (C-0140): no folded object is measured, and the " +
                    "two-length assignment is a design this repository derives rather than one " +
                    "anybody has built.",
            "The two ENDS of the scaffold path carry no defined turn sense (C-0140), so their " +
                    "length is free. 10 x 6's face is clear of both; 15 x 4's row 14 face helix " +
                    "IS the path terminus and its sense is extrapolated from the row parity.",
            "The block extent is taken over the INTERIOR helices, which is C-0140's own " +
                    "convention; the two terminal helices could extend it further.",
            "The tile is a SMEARED equivalent sheet with ONE lengthX, so the 4 bp axial STAGGER " +
                    "between consecutive raster rows is not representable at all -- which is " +
                    "exactly why both width readings are carried and neither is preferred.",
            "The dropout statistics are measured on a SINGLE-LAYER Rothemund rectangle; only the " +
                    "PROFILE transfers, in nm, and the wider tile changes the " +
                    "perimeter-to-area ratio again. C-0109's assumption, inherited.",
            "The mandate is read at SS3's ACCEPTABLE clause (100 pN / 3 nm).",
            "Two distributions only -- equal and C-0058's rim rule.",
            "C-0022's collar is read unchanged at the wider aspect ratio; C-0123's question is " +
                    "reopened again rather than answered.",
            "Kirchhoff is not safe at these thicknesses (C-0109, C-0120): every D_par is an " +
                    "upper bound."
        ),
        openQuestions = listOf(
            "Which of the two width readings a folded block is owed against SS3 -- the bounding " +
                    "box the object occupies or the row length its beams carry. Nothing here " +
                    "settles it, and the two differ by 3.57 %.",
            "What the 4 bp inter-row axial STAGGER costs a flatness model that can represent it; " +
                    "a single-lengthX grillage cannot.",
            "Which ladder phase and which of the two inter-row offsets a caDNAno honeycomb " +
                    "carries, which now decides a station COUNT and not only a position.",
            "Whether C-0089's percentile descent recovers any cell the two-length raster loses."
        )
    )

    val output = File("gpd/results/T-235-coupled-cells-at-the-two-length-raster.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digits = 9, floor = 1e-12
            ) as JsonObject)
        ) + "\n"
    )
    println("T-235 - wrote " + output.path)
}
