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
import kotlin.math.floor

// ---------------------------------------------------------------------------------------------
// T-232 -- re-grade C-0118's sixteen coupled cells at the CORRECTED honeycomb cross-section.
//
// C-0118 is the only coupled tile in this programme that clears T-5b under the measured folding
// statistics. C-0141 has since shown that the cross-section it was graded on is not a honeycomb:
// the in-plane row pitch is 3d/2 and the layer pitch d*sqrt(3)/2, so every four-layer edgeY here
// is exactly 1.5x too small and the plan density is 1.299x too high.
//
// The machinery is unchanged -- C-0141 lifted edgeY, the in-plane pitch and the layer spacing out
// of C-0120's construction as parameters, and this study consumes that rather than re-deriving it.
// Everything geometric comes from HoneycombFaceLattice.
// ---------------------------------------------------------------------------------------------

private const val T232_SAMPLES: Int = 81
private const val T232_TOLERANCE: Double = 0.10
private const val T232_RIM_STANDOFF: Double = 1.0
private const val T232_RIM_BAND: Double = 6.7
private const val T232_ROW_BP: Int = 112
private const val T232_SEED: Long = 197_197L
private const val T232_BAND_LOW: Double = 0.26
private const val T232_BAND_MEASURED: Double = 0.30
private const val T232_LADDER_PHASE: Int = 0
private const val T232_ROW_OFFSET_BP: Int = 7

private const val T232_DECLARED_REALISATIONS: Int = 4000

/**
 * The realisation count, overridable **only** for a smoke run.
 *
 * `CLAUDE.md`: *"a toy-sample smoke run tests the plumbing and must NOT be read for a falsifier
 * verdict"* — so the emitted file declares `smokeRun` beside it rather than leaving a reader to
 * infer from a count whether the numbers are the ones the task promised.
 */
private val t232Realisations: Int =
    System.getenv("T232_REALISATIONS")?.toIntOrNull() ?: T232_DECLARED_REALISATIONS

private fun Double.emitted(digits: Int = 9): String =
    if (!isFinite() || this == 0.0) toString()
    else java.math.BigDecimal(this).round(java.math.MathContext(digits)).toDouble().toString()

// ------------------------------------------------------------------------------ the records

@Serializable
private class T232Geometry(
    val crossSection: String,
    val geometry: String,
    val rasterRows: Int,
    val layers: Int,
    val edgeX: Double,
    val edgeY: Double,
    val inPlaneRowPitch: Double,
    val layerSpacing: Double,
    val planAreaPerHelix: Double,
    val planAreaPerHelixOverHoneycomb: Double,
    val edgeYOverSpecification: Double,
    val alongHelixRigidity: Double,
    val acrossHelixRigidity: Double,
    val reachAlong: Double,
    val reachAcross: Double,
    val stationsPerRootingHelix: Int,
    val stationsOnFace: Int
)

@Serializable
private class T232Reference(
    val crossSection: String,
    val geometry: String,
    val compositeFraction: Double,
    val uncoupledDishingOverStroke: Double,
    val flat: Boolean
)

@Serializable
private class T232Cell(
    val crossSection: String,
    val geometry: String,
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
private class T232Paired(
    val crossSection: String,
    val columns: Int,
    val pathCount: Int,
    val distribution: String,
    val correctedP90: Double,
    val standingP90: Double,
    val ratioOfPercentiles: Double,
    val medianOfRatios: Double,
    val p90OfRatios: Double,
    val bestRatio: Double,
    val worstRatio: Double,
    val fractionAbove: Double
)

@Serializable
private class T232Convergence(
    val axis: String,
    val values: List<Double>,
    val results: List<Double>,
    val departure: Double,
    val note: String
)

@Serializable
private class T232Reproduction(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double,
    val strict: Boolean
)

@Serializable
private class T232Falsifier(
    val id: String,
    val statement: String,
    val fired: Boolean,
    val evidence: String
)

@Serializable
private class T232Result(
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
    val geometries: List<T232Geometry>,
    val references: List<T232Reference>,
    val cells: List<T232Cell>,
    val paired: List<T232Paired>,
    val verdict: Map<String, String>,
    val convergence: List<T232Convergence>,
    val reproductions: List<T232Reproduction>,
    val falsifiers: List<T232Falsifier>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

// ------------------------------------------------------------------------------ the load

private class T232Profile(
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, edgeX: Double, lengthY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, lengthY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T232_RIM_STANDOFF))
        )
}

private fun t232Profile(file: File): T232Profile {
    require(file.exists()) { "C-0022's result file is missing: " + file.path }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(name: String) = it.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at the design state")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T232Profile(value("taperDepth"), value("taperWidth"), value("rimResidualDepth"))
}

// ------------------------------------------------------------------------------ the tile

/**
 * A four-layer tile at a stated cross-section geometry — `C-0141`'s `T219Tile`, with the coupling
 * machinery `C-0118` grades on attached to it.
 *
 * The three geometric parameters are the whole of the correction: `edgeY`, the in-plane duplex
 * pitch and the layer spacing. Nothing else moves between the standing reading and the honeycomb.
 */
private class T232Tile(
    val label: String,
    val geometry: String,
    val rasterRows: Int,
    val layers: Int,
    val edgeY: Double,
    val inPlanePitch: Double,
    val layerSpacing: Double,
    val compositeFraction: Double,
    private val profile: T232Profile
) {

    val edgeX: Double = T232_ROW_BP * Gen1Tile.RISE_PER_BASE_PAIR

    init {
        // The grillage derives its own `lengthY = beamCount × interhelicalDistance`; the plate and
        // the pressure field are handed `edgeY`. They are the same tile only if these agree, and
        // that is an identity of the honeycomb geometry rather than a coincidence.
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
        val pitch = sheet.crossoverSpacing / 2.0
        val usable = edgeX - 2.0 * CrossoverLayout.EDGE_MARGIN
        OrigamiGrillage(
            sheet = sheet,
            lengthX = edgeX,
            beamCount = rasterRows,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            columns = CrossoverLayout.centred(floor(usable / pitch).toInt() + 1, pitch),
            subdivisions = 2
        )
    }

    val uncoupledDishing: Double by lazy {
        lattice.solve(pressureField).peakDishing(T232_SAMPLES) / freeStroke
    }
}

/** One graded cell, with the dishing sample retained so the comparison can be **paired**. */
private class T232Graded(val cell: T232Cell, val sample: DoubleArray)

@Suppress("LongParameterList")
private fun gradeCell(
    tile: T232Tile,
    columns: Int,
    grid: List<Pair<Double, Double>>,
    placement: String,
    snapDeparture: Double,
    distribution: String,
    stiffnesses: List<Double>,
    realisations: Int
): T232Graded {
    val surrogate = latticeInfluenceSurrogate(tile.lattice, grid, tile.pressureField, T232_SAMPLES)
    val incorporation = measuredDepthIncorporation(tile.edgeX, tile.edgeY)
    val ensemble = dropoutEnsemble(
        grid.map { (x, y) -> incorporation.at(x, y) }, realisations, T232_SEED
    )
    val nominal = surrogate.solve(stiffnesses).peakDishing / tile.freeStroke
    val sample = dropoutDishingSample(surrogate, stiffnesses, ensemble)
    sample.indices.forEach { sample[it] = sample[it] / tile.freeStroke }
    val summary = summariseDropoutDishing(
        sample, nominal, ensemble.meanSurvivors, T232_TOLERANCE
    )
    val pitchAlong = tile.edgeX / columns
    val pitchAcross = tile.edgeY / tile.rasterRows
    return T232Graded(
        T232Cell(
            crossSection = tile.label,
            geometry = tile.geometry,
            placement = placement,
            compositeFraction = tile.compositeFraction,
            columns = columns,
            rows = tile.rasterRows,
            pathCount = grid.size,
            distribution = distribution,
            perPathStiffness = stiffnesses.max(),
            totalStiffness = stiffnesses.sum(),
            attachmentPitchAlong = pitchAlong,
            attachmentPitchAcross = pitchAcross,
            pitchAlongOverReach = pitchAlong / tile.reachAlong,
            pitchAcrossOverReach = pitchAcross / tile.reachAcross,
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
            flatAtNominal = nominal < T232_TOLERANCE,
            flatAtP90 = summary.flatAtP90,
            beatsUncoupledAtP90 = summary.p90 < tile.uncoupledDishing
        ),
        sample
    )
}

private fun t232Distributions(
    grid: List<Pair<Double, Double>>,
    edgeX: Double,
    edgeY: Double
): List<Pair<String, List<Double>>> = listOf(
    "equal springs" to equalShareOfMandate(grid.size),
    "rim-graded 5:1" to rimGradedShareOfMandate(
        grid.map { (x, y) ->
            val onRim = abs(x) > edgeX / 2.0 - T232_RIM_BAND || abs(y) > edgeY / 2.0 - T232_RIM_BAND
            if (onRim) 5.0 else 1.0
        }
    )
)

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val d = Gen1Tile.INTERHELICAL_HONEYCOMB
    val rowPitch = HoneycombCrossSectionGeometry.rowPitch(d)
    val columnPitch = HoneycombCrossSectionGeometry.columnPitch(d)
    val honeycombCell = HoneycombCrossSectionGeometry.perSiteArea(d)
    val profile = t232Profile(File("gpd/results/T-3b-tile-edge-load-profile.json"))
    val specEdge = 40.35
    val stationsPerHelix = honeycombLadderIndices(T232_ROW_BP, T232_LADDER_PHASE).size

    println("T-232 - the cheap bound, before any Monte Carlo")
    println("  honeycomb in-plane row pitch 3d/2 = " + rowPitch.emitted(6) + " nm")
    println("  honeycomb layer pitch d*sqrt(3)/2 = " + columnPitch.emitted(6) + " nm")
    println("  honeycomb cell 3sqrt(3)/4 d^2     = " + honeycombCell.emitted(6) + " nm^2 per helix")
    println("  the standing d^2                  = " + (d * d).emitted(6) + ", ratio " +
            (honeycombCell / (d * d)).emitted(9))
    println("  stations per rooting helix at " + T232_ROW_BP + " bp on the 21 bp ladder: " +
            stationsPerHelix)
    println("  realisations: " + t232Realisations)

    // ------------------------------------------------------------------ the two cross-sections
    val designs = listOf(Triple("15 x 4", 15, 4), Triple("10 x 6", 10, 6))

    fun tileAt(design: Triple<String, Int, Int>, geometry: String, fraction: Double): T232Tile =
        if (geometry == "honeycomb")
            T232Tile(
                design.first, geometry, design.second, design.third,
                HoneycombBlock(design.second, design.third, d).plateEdgeY,
                rowPitch, columnPitch, fraction, profile
            )
        else
            T232Tile(
                design.first, geometry, design.second, design.third,
                design.second * d, d, d, fraction, profile
            )

    val geometries = ArrayList<T232Geometry>()
    val references = ArrayList<T232Reference>()
    designs.forEach { design ->
        listOf("standing", "honeycomb").forEach { geometry ->
            val tile = tileAt(design, geometry, T232_BAND_MEASURED)
            geometries += T232Geometry(
                crossSection = design.first,
                geometry = geometry,
                rasterRows = design.second,
                layers = design.third,
                edgeX = tile.edgeX,
                edgeY = tile.edgeY,
                inPlaneRowPitch = tile.inPlanePitch,
                layerSpacing = tile.layerSpacing,
                planAreaPerHelix = tile.inPlanePitch * tile.layerSpacing,
                planAreaPerHelixOverHoneycomb = tile.inPlanePitch * tile.layerSpacing / honeycombCell,
                edgeYOverSpecification = tile.edgeY / specEdge,
                alongHelixRigidity = tile.rigidities.alongHelixRigidity,
                acrossHelixRigidity = tile.rigidities.acrossHelixRigidity,
                reachAlong = tile.reachAlong,
                reachAcross = tile.reachAcross,
                stationsPerRootingHelix = stationsPerHelix,
                stationsOnFace = stationsPerHelix * design.second
            )
            listOf(T232_BAND_LOW, T232_BAND_MEASURED).forEach { fraction ->
                val at = tileAt(design, geometry, fraction)
                references += T232Reference(
                    crossSection = design.first,
                    geometry = geometry,
                    compositeFraction = fraction,
                    uncoupledDishingOverStroke = at.uncoupledDishing,
                    flat = at.uncoupledDishing < T232_TOLERANCE
                )
            }
        }
    }
    geometries.forEach {
        println("  " + it.crossSection + "  " + it.geometry + "  edgeY " + it.edgeY.emitted(6) +
                " nm (" + it.edgeYOverSpecification.emitted(6) + " of spec)  D_par " +
                it.alongHelixRigidity.emitted(6) + "  D_perp " + it.acrossHelixRigidity.emitted(6) +
                "  stations " + it.stationsOnFace)
    }
    references.forEach {
        println("  uncoupled " + it.crossSection + "  " + it.geometry + "  f = " +
                it.compositeFraction.emitted(3) + "  dishing " +
                it.uncoupledDishingOverStroke.emitted(9) +
                (if (it.flat) "  flat" else "  NOT FLAT"))
    }

    // ------------------------------------------------------------------ the sixteen cells
    val columnCounts = listOf(1, 2, 3, 5)
    val cells = ArrayList<T232Cell>()
    val samples = HashMap<String, DoubleArray>()

    fun key(design: String, geometry: String, placement: String, fraction: Double,
            columns: Int, distribution: String): String =
        design + "|" + geometry + "|" + placement + "|" + fraction + "|" + columns + "|" +
                distribution

    designs.forEach { design ->
        listOf(
            Triple("standing", "abstract grid", T232_BAND_MEASURED),
            Triple("honeycomb", "abstract grid", T232_BAND_MEASURED),
            Triple("honeycomb", "abstract grid", T232_BAND_LOW),
            Triple("honeycomb", "honeycomb station lattice", T232_BAND_MEASURED)
        ).forEach { (geometry, placement, fraction) ->
            val tile = tileAt(design, geometry, fraction)
            columnCounts.forEach { columns ->
                val abstractGrid = attachmentGrid(columns, tile.rasterRows, tile.edgeX, tile.edgeY)
                val grid = if (placement == "abstract grid") abstractGrid
                else honeycombSnappedGrid(
                    columns, tile.rasterRows, T232_ROW_BP, tile.edgeY,
                    T232_LADDER_PHASE, T232_ROW_OFFSET_BP
                )
                val snapDeparture = alongHelixDeparture(abstractGrid, grid)
                t232Distributions(grid, tile.edgeX, tile.edgeY).forEach { (label, stiffnesses) ->
                    val graded = gradeCell(
                        tile, columns, grid, placement, snapDeparture, label, stiffnesses,
                        t232Realisations
                    )
                    cells += graded.cell
                    samples[key(design.first, geometry, placement, fraction, columns, label)] =
                        graded.sample
                    println("  " + design.first + "  " + geometry + "  " + placement + "  f=" +
                            fraction.emitted(3) + "  " + columns + " col x " + tile.rasterRows +
                            " = " + grid.size + " paths, " + label + "  nominal " +
                            graded.cell.nominalOverStroke.emitted(9) + "  p90 " +
                            graded.cell.p90OverStroke.emitted(9) +
                            (if (graded.cell.flatAtP90) "  FLAT at p90" else "  not flat at p90"))
                }
            }
        }
    }

    // ------------------------------------------------------------------ the paired comparison
    val paired = ArrayList<T232Paired>()
    designs.forEach { design ->
        columnCounts.forEach { columns ->
            listOf("equal springs", "rim-graded 5:1").forEach { label ->
                val corrected = samples.getValue(
                    key(design.first, "honeycomb", "abstract grid", T232_BAND_MEASURED, columns, label)
                )
                val standing = samples.getValue(
                    key(design.first, "standing", "abstract grid", T232_BAND_MEASURED, columns, label)
                )
                val summary = pairedRatioSummary(corrected, standing)
                val correctedCell = cells.first {
                    it.crossSection == design.first && it.geometry == "honeycomb" &&
                            it.placement == "abstract grid" &&
                            it.compositeFraction == T232_BAND_MEASURED && it.columns == columns &&
                            it.distribution == label
                }
                val standingCell = cells.first {
                    it.crossSection == design.first && it.geometry == "standing" &&
                            it.columns == columns && it.distribution == label
                }
                paired += T232Paired(
                    crossSection = design.first,
                    columns = columns,
                    pathCount = correctedCell.pathCount,
                    distribution = label,
                    correctedP90 = correctedCell.p90OverStroke,
                    standingP90 = standingCell.p90OverStroke,
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

    // ------------------------------------------------------------------ the verdict
    val correctedCells = cells.filter {
        it.geometry == "honeycomb" && it.placement == "abstract grid" &&
                it.compositeFraction == T232_BAND_MEASURED
    }
    val standingCells = cells.filter { it.geometry == "standing" }
    val bandLowCells = cells.filter {
        it.geometry == "honeycomb" && it.compositeFraction == T232_BAND_LOW
    }
    val snappedCells = cells.filter { it.placement == "honeycomb station lattice" }
    val bestCorrected = correctedCells.minBy { it.p90OverStroke }
    val bestCorrected154 = correctedCells.filter { it.crossSection == "15 x 4" }
        .minBy { it.p90OverStroke }
    val bestCorrected106 = correctedCells.filter { it.crossSection == "10 x 6" }
        .minBy { it.p90OverStroke }
    val bestSnapped = snappedCells.minBy { it.p90OverStroke }

    // ------------------------------------------------------------------ convergence
    val bestDesign = designs.first { it.first == bestCorrected.crossSection }
    val realisationSweep = listOf(
        t232Realisations / 4, t232Realisations / 2, t232Realisations
    ).map { n ->
        val tile = tileAt(bestDesign, "honeycomb", T232_BAND_MEASURED)
        val grid = attachmentGrid(bestCorrected.columns, tile.rasterRows, tile.edgeX, tile.edgeY)
        val stiffnesses = t232Distributions(grid, tile.edgeX, tile.edgeY)
            .first { it.first == bestCorrected.distribution }.second
        gradeCell(
            tile, bestCorrected.columns, grid, "abstract grid", 0.0,
            bestCorrected.distribution, stiffnesses, n
        ).cell.p90OverStroke
    }
    val convergence = listOf(
        T232Convergence(
            axis = "dropout realisations, the best corrected cell's 90th percentile",
            values = listOf(
                t232Realisations / 4.0, t232Realisations / 2.0, t232Realisations.toDouble()
            ),
            results = realisationSweep,
            departure = abs(realisationSweep[2] - realisationSweep[1]) / abs(realisationSweep[2]),
            note = "one COMMON stream restricted, not three independent draws, so the departure " +
                    "is a convergence and not a variance"
        )
    )

    // ------------------------------------------------------------------ reproductions
    val publishedStanding = mapOf(
        "15 x 4|1|equal springs" to 0.131685589,
        "15 x 4|1|rim-graded 5:1" to 0.186011867,
        "15 x 4|2|equal springs" to 0.155081687,
        "15 x 4|2|rim-graded 5:1" to 0.206737248,
        "15 x 4|3|equal springs" to 0.133271547,
        "15 x 4|3|rim-graded 5:1" to 0.104871904,
        "15 x 4|5|equal springs" to 0.124585773,
        "15 x 4|5|rim-graded 5:1" to 0.0882933461,
        "10 x 6|1|equal springs" to 0.0278431488,
        "10 x 6|1|rim-graded 5:1" to 0.0306268096,
        "10 x 6|2|equal springs" to 0.0541089284,
        "10 x 6|2|rim-graded 5:1" to 0.0623145994,
        "10 x 6|3|equal springs" to 0.0461988976,
        "10 x 6|3|rim-graded 5:1" to 0.0441544716,
        "10 x 6|5|equal springs" to 0.0408747025,
        "10 x 6|5|rim-graded 5:1" to 0.0366559399
    )
    val reproductions = ArrayList<T232Reproduction>()
    standingCells.forEach { cell ->
        val id = cell.crossSection + "|" + cell.columns + "|" + cell.distribution
        val published = publishedStanding.getValue(id)
        reproductions += T232Reproduction(
            source = "C-0118",
            quantity = "standing-geometry 90th percentile, " + id,
            published = published,
            reproduced = cell.p90OverStroke,
            departure = abs(cell.p90OverStroke - published) / abs(published),
            strict = true
        )
    }
    listOf(
        Triple("C-0120 / C-0118", "15 x 4|standing|0.3", 0.0577199433),
        Triple("C-0120 / C-0118", "10 x 6|standing|0.3", 0.00874363524),
        Triple("C-0141", "15 x 4|honeycomb|0.3", 0.0978155002),
        Triple("C-0141", "10 x 6|honeycomb|0.3", 0.0240648102),
        Triple("C-0141", "15 x 4|honeycomb|0.26", 0.101759944),
        Triple("C-0141", "10 x 6|honeycomb|0.26", 0.0255589305)
    ).forEach { (source, id, published) ->
        val parts = id.split("|")
        val reference = references.first {
            it.crossSection == parts[0] && it.geometry == parts[1] &&
                    abs(it.compositeFraction - parts[2].toDouble()) < 1e-12
        }
        reproductions += T232Reproduction(
            source = source,
            quantity = "uncoupled free-tile dishing, " + id,
            published = published,
            reproduced = reference.uncoupledDishingOverStroke,
            departure = abs(reference.uncoupledDishingOverStroke - published) / abs(published),
            strict = true
        )
    }
    reproductions.forEach {
        println("  reproduce " + it.source + " " + it.quantity + ": " +
                it.reproduced.emitted(9) + " against " + it.published.emitted(9) +
                ", departure " + it.departure.emitted(2))
    }

    // ------------------------------------------------------------------ falsifiers
    val worstStandingDeparture = reproductions.filter { it.source == "C-0118" }.maxOf { it.departure }
    val worstGeometryDeparture = reproductions.filter { it.source == "C-0141" }.maxOf { it.departure }
    val f3Offenders = cells.filter { it.p90OverStroke < it.nominalOverStroke }
    val f4Offenders = paired.filter {
        (it.medianOfRatios - 1.0) * (it.ratioOfPercentiles - 1.0) < 0.0
    }
    val f5Flat106 = correctedCells.filter { it.crossSection == "10 x 6" }.all { it.flatAtP90 }
    val falsifiers = listOf(
        T232Falsifier(
            "F1",
            "the standing-geometry cells do NOT reproduce C-0118's sixteen numbers, in which " +
                    "case nothing here is a re-grading of that claim",
            worstStandingDeparture > 1e-6,
            "worst departure over the sixteen standing cells: " + worstStandingDeparture.emitted(2)
        ),
        T232Falsifier(
            "F2",
            "the corrected uncoupled references do not reproduce C-0141's numbers, in which " +
                    "case the geometry has not been transferred",
            worstGeometryDeparture > 1e-6,
            "worst departure over C-0141's four corrected references: " +
                    worstGeometryDeparture.emitted(2)
        ),
        T232Falsifier(
            "F3",
            "a cell's 90th percentile is BELOW its own zero-defect nominal, which a removal-only " +
                    "perturbation should not produce",
            f3Offenders.isNotEmpty(),
            f3Offenders.size.toString() + " of " + cells.size + " cells have p90 below nominal"
        ),
        T232Falsifier(
            "F4",
            "the median per-realisation ratio has the OPPOSITE sign to the ratio of the 90th " +
                    "percentiles, in which case the quoted summary does not describe its sample",
            f4Offenders.isNotEmpty(),
            f4Offenders.size.toString() + " of " + paired.size + " paired rows disagree in sign"
        ),
        T232Falsifier(
            "F5",
            "10 x 6 loses its flatness at the corrected geometry at the measured f = 0.30, in " +
                    "which case the programme has no flat coupled tile at all",
            !f5Flat106,
            correctedCells.count { it.crossSection == "10 x 6" && it.flatAtP90 }.toString() +
                    " of 8 corrected 10 x 6 cells are flat at the 90th percentile"
        )
    )
    falsifiers.forEach {
        println("  " + it.id + (if (it.fired) " FIRED   " else " did not fire   ") + it.evidence)
    }

    // ------------------------------------------------------------------ findings
    val findings = HashMap<String, String>()
    findings["theCheapBound"] =
        "A honeycomb spends " + honeycombCell.emitted(9) + " nm^2 of plan per helix against the " +
                "standing cross-section's " + (d * d).emitted(9) + ", a factor of " +
                (honeycombCell / (d * d)).emitted(9) + ", so every four-layer edgeY in this " +
                "corpus is exactly 1.5x too small. The across-helix ATTACHMENT pitch is edgeY " +
                "over rasterRows identically, so it is the in-plane row pitch and it moves by " +
                "that same 1.5x -- which is how the correction reaches the coupling. Every " +
                "requested path count is inside its own station inventory (" +
                (stationsPerHelix * 15) + " on 15 x 4, " + (stationsPerHelix * 10) +
                " on 10 x 6), so no cell fails on COUNT."
    findings["theVerdict"] =
        "Of " + correctedCells.size + " coupled cells re-graded at the corrected honeycomb " +
                "cross-section at the measured f = 0.30, " +
                correctedCells.count { it.flatAtP90 } + " are flat at the 90th percentile " +
                "against C-0118's " + standingCells.count { it.flatAtP90 } + " of " +
                standingCells.size + " at the geometry it was graded on. The best corrected cell " +
                "is " + bestCorrected.crossSection + ", " + bestCorrected.columns + " column(s), " +
                bestCorrected.distribution + ", p90 = " + bestCorrected.p90OverStroke.emitted() + "."
    findings["theCrossSections"] =
        "Best corrected cell on 15 x 4: " + bestCorrected154.p90OverStroke.emitted() + " (" +
                bestCorrected154.distribution + ", " + bestCorrected154.columns + " column(s)), " +
                (if (bestCorrected154.flatAtP90) "flat" else "NOT flat") +
                ". Best on 10 x 6: " + bestCorrected106.p90OverStroke.emitted() + " (" +
                bestCorrected106.distribution + ", " + bestCorrected106.columns + " column(s)), " +
                (if (bestCorrected106.flatAtP90) "flat" else "NOT flat") +
                ". The cross-section is worth " +
                (bestCorrected154.p90OverStroke / bestCorrected106.p90OverStroke).emitted(6) +
                "x on this statistic at the corrected geometry, against C-0118's 3.17109774x at " +
                "the uncorrected one."
    findings["theBandIsNotOnePoint"] =
        "C-0116's composite-fraction threshold for 15 x 4 moves INSIDE the measured 0.26-0.33 " +
                "band at the corrected geometry, so a single-f verdict is not a verdict. At the " +
                "band's adverse low end f = 0.26, " +
                bandLowCells.count { it.crossSection == "15 x 4" && it.flatAtP90 } +
                " of 8 corrected 15 x 4 cells and " +
                bandLowCells.count { it.crossSection == "10 x 6" && it.flatAtP90 } +
                " of 8 corrected 10 x 6 cells are flat at the 90th percentile."
    findings["thePlacementIsNowOnTheLattice"] =
        "C-0118 states its path counts are a REQUEST and not a demonstration that the stations " +
                "exist. Snapped onto C-0141's honeycomb station lattice at phase " +
                T232_LADDER_PHASE + " and the forced " + T232_ROW_OFFSET_BP +
                " bp inter-row stagger, every one of the " + snappedCells.size +
                " cells is realisable -- the along-helix snap moves a station by at most " +
                snappedCells.maxOf { it.alongHelixSnapDeparture }.emitted(6) +
                " nm, inside the " +
                (0.5 * HoneycombLattice.SAME_PAIR_PERIOD_BP * Gen1Tile.RISE_PER_BASE_PAIR)
                    .emitted(3) +
                " nm half-ladder-pitch ceiling the snap cannot exceed -- and " +
                snappedCells.count { it.flatAtP90 } + " of them are flat at the 90th percentile. " +
                "The best is " + bestSnapped.crossSection + ", " + bestSnapped.columns +
                " column(s), " + bestSnapped.distribution + ", p90 = " +
                bestSnapped.p90OverStroke.emitted() + "."
    findings["thePairedReading"] =
        "The cost of the geometry read PER REALISATION on the shared stream: the median " +
                "per-realisation ratio runs " +
                paired.minOf { it.medianOfRatios }.emitted(6) + " to " +
                paired.maxOf { it.medianOfRatios }.emitted(6) + " against a ratio of 90th " +
                "percentiles running " + paired.minOf { it.ratioOfPercentiles }.emitted(6) +
                " to " + paired.maxOf { it.ratioOfPercentiles }.emitted(6) +
                ". CLAUDE.md: a ratio of two ORDER STATISTICS is not the order statistic of the " +
                "ratio."

    val result = T232Result(
        task = "T-232",
        leaf = "A8.2",
        title = "C-0118's sixteen coupled cells, re-graded at the corrected honeycomb cross-section",
        verificationType = "in-silico (influence surrogate over the grillage, Monte Carlo dropout " +
                "on one common stream) + logical (the station lattice each placement stands on)",
        maturity = "TRL 1-3. Model-consistent and traceable. NOT empirically demonstrated. The " +
                "FOLDING statistics graded against are measured; the flatness is not.",
        units = mapOf(
            "length" to "nm",
            "stiffness" to "pN/nm",
            "rigidity" to "pN nm",
            "dishing" to "dimensionless, as a fraction of the free-tile stroke",
            "compositeFraction" to "dimensionless"
        ),
        conventions = mapOf(
            "crossSection" to "C-0141's honeycomb: in-plane row pitch 3d/2, layer pitch " +
                    "d sqrt(3)/2, cell 3 sqrt(3)/4 d^2; the standing reading is d and d",
            "mandate" to "C-0017's equality on the SUM, SS3's acceptable clause: 100 pN / 3 nm",
            "load" to "C-0022's solved collar at 2 mM / 10 nm / 0.192 V",
            "dropout" to "C-0087's measured per-site staple incorporation, depth convention",
            "flat" to "peak dishing below T-5b's 0.10, read at the 90th percentile",
            "span" to "112 bp along the helices for both cross-sections",
            "stationLattice" to "C-0141's 21 bp ladder with the FORCED 7 bp inter-row stagger",
            "axes" to "x along the helices, y across them, origin at the tile centre"
        ),
        parameters = mapOf(
            "realisations" to t232Realisations.toString(),
            "declaredRealisations" to T232_DECLARED_REALISATIONS.toString(),
            "smokeRun" to (t232Realisations != T232_DECLARED_REALISATIONS).toString(),
            "seed" to T232_SEED.toString(),
            "dishingSamplesPerSide" to T232_SAMPLES.toString(),
            "tolerance" to T232_TOLERANCE.toString(),
            "mandatedTotalStiffness" to MANDATED_TOTAL_STIFFNESS.emitted(),
            "rowBasePairs" to T232_ROW_BP.toString(),
            "bondLength" to d.emitted(),
            "ladderPhaseBasePairs" to T232_LADDER_PHASE.toString(),
            "interRowOffsetBasePairs" to T232_ROW_OFFSET_BP.toString(),
            "rimBandWidth" to T232_RIM_BAND.toString(),
            "compositeFractions" to (T232_BAND_LOW.toString() + ", " + T232_BAND_MEASURED),
            "beamSubdivisions" to "2",
            // `P-22`'s declaration convention: the task ids this study READS, so that
            // `tools/result-reader-census.py --check-declarations` can verify the derived graph
            // against what the study says of itself.
            "sources" to "gpd/results/T-3b-tile-edge-load-profile.json"
        ),
        sources = listOf(
            "gpd/results/T-3b-tile-edge-load-profile.json - C-0022's solved collar at the design " +
                    "state, read at 2 mM / 10 nm / 0.192 V"
        ),
        citedInputs = listOf(
            "C-0118 - the sixteen coupled cells this re-grades, and the standing recommendation",
            "C-0141 / CH-0174 - the corrected honeycomb cross-section and the station lattice",
            "C-0120 - the cross-section comparison and its uncoupled references",
            "C-0116 - the composite-fraction threshold and the measured 0.26-0.33 band",
            "C-0017 - the mandate, an equality on the SUM",
            "C-0087 - the measured per-site staple incorporation",
            "C-0058 - the rim grading",
            "C-0129 - the one-sided bound on a saturated proportion",
            "C-0103 - the common-random-number discipline"
        ),
        cheapBound = mapOf(
            "honeycombCellArea" to honeycombCell.emitted(),
            "standingCellArea" to (d * d).emitted(),
            "densityRatio" to (honeycombCell / (d * d)).emitted(),
            "inPlaneRowPitch" to rowPitch.emitted(),
            "layerSpacing" to columnPitch.emitted(),
            "edgeYRatio" to "1.5, exactly, at every raster-row count",
            "stationsPerRootingHelix" to stationsPerHelix.toString(),
            "whatItSaid" to findings["theCheapBound"]!!
        ),
        geometries = geometries,
        references = references,
        cells = cells,
        paired = paired,
        verdict = mapOf(
            "correctedCellsGraded" to correctedCells.size.toString(),
            "correctedFlatAtP90" to correctedCells.count { it.flatAtP90 }.toString(),
            "standingFlatAtP90" to standingCells.count { it.flatAtP90 }.toString(),
            "corrected15x4FlatAtP90" to
                    correctedCells.count { it.crossSection == "15 x 4" && it.flatAtP90 }.toString(),
            "corrected10x6FlatAtP90" to
                    correctedCells.count { it.crossSection == "10 x 6" && it.flatAtP90 }.toString(),
            "bandLow15x4FlatAtP90" to
                    bandLowCells.count { it.crossSection == "15 x 4" && it.flatAtP90 }.toString(),
            "bandLow10x6FlatAtP90" to
                    bandLowCells.count { it.crossSection == "10 x 6" && it.flatAtP90 }.toString(),
            "latticeSnappedFlatAtP90" to snappedCells.count { it.flatAtP90 }.toString(),
            "bestCorrectedCrossSection" to bestCorrected.crossSection,
            "bestCorrectedColumns" to bestCorrected.columns.toString(),
            "bestCorrectedDistribution" to bestCorrected.distribution,
            "bestCorrectedP90" to bestCorrected.p90OverStroke.emitted(),
            "bestCorrectedOn15x4" to bestCorrected154.p90OverStroke.emitted(),
            "bestCorrectedOn10x6" to bestCorrected106.p90OverStroke.emitted(),
            "bestLatticeSnappedP90" to bestSnapped.p90OverStroke.emitted(),
            "crossSectionWorth" to
                    (bestCorrected154.p90OverStroke / bestCorrected106.p90OverStroke).emitted()
        ),
        convergence = convergence,
        reproductions = reproductions,
        falsifiers = falsifiers,
        findings = findings,
        validity = listOf(
            "The cross-section is a LATTICE statement (C-0141): no folded object is measured, and " +
                    "the geometry follows from the honeycomb's own two pitches.",
            "The tile is a SMEARED equivalent sheet. OrigamiGrillage never reads `layers`, and " +
                    "its crossover combinatorics are square-lattice; only edgeY, the in-plane " +
                    "pitch and the layer spacing carry the honeycomb here.",
            "The dropout statistics are measured on a SINGLE-LAYER Rothemund rectangle; only the " +
                    "PROFILE transfers, in nm, and a four-layer tile has a different staple " +
                    "population and a different perimeter-to-area ratio. C-0109's assumption, " +
                    "inherited and named -- and the corrected edgeY changes that ratio again.",
            "The mandate is read at SS3's ACCEPTABLE clause (100 pN / 3 nm). The desired clause " +
                    "gives 10 pN/nm and a different device.",
            "Two distributions only -- equal and C-0058's rim rule. C-0089's percentile descent " +
                    "and C-0093's shared body are not run.",
            "C-0022's collar is read unchanged at both aspect ratios, and both edgeY values have " +
                    "now moved by 1.5x. C-0123's question is reopened rather than answered.",
            "The lattice-snapped placement is read at ONE ladder phase and ONE of the two " +
                    "admissible inter-row offsets. C-0141 carries both offsets and 21 phases.",
            "Kirchhoff is not safe at these thicknesses (C-0109, C-0120): every D_par is an upper " +
                    "bound, and more so on the thicker cross-section."
        ),
        openQuestions = listOf(
            "Whether C-0089's percentile descent or C-0093's shared body recovers any 15 x 4 cell " +
                    "at the corrected geometry -- the axes C-0118 also left unrun.",
            "Which ladder phase and which of the two inter-row offsets a caDNAno honeycomb " +
                    "carries, which decides which snapped placement is buildable.",
            "Whether C-0022's collar transfers to either corrected aspect ratio, which both " +
                    "C-0120 and C-0141 flag and neither answers.",
            "Whether 15 x 4 at 56.524 nm across is admissible at all against SS3's ~40 nm, which " +
                    "is a specification question and not a flatness one."
        )
    )

    val output = File("gpd/results/T-232-coupled-cells-at-the-honeycomb-cross-section.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digits = 9, floor = 1e-12
            ) as JsonObject)
        ) + "\n"
    )
    println("T-232 - wrote " + output.path)
}
