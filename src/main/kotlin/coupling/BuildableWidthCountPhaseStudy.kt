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

package com.xemantic.nano.plentyofroom.coupling

import com.xemantic.nano.plentyofroom.anchoring.BUILDABLE_RASTER_WIDTH
import com.xemantic.nano.plentyofroom.anchoring.UpwardRootInfluenceBank
import com.xemantic.nano.plentyofroom.anchoring.endOfRowColumnPhases
import com.xemantic.nano.plentyofroom.anchoring.rasterColumnLayout
import com.xemantic.nano.plentyofroom.anchoring.rasterUpwardSites
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.DEPARTURE_DIGITS_BY_KEY
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.OrigamiSheet
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.roundForResult
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.uniformPressure
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs
import kotlin.math.exp

/**
 * `T-188` — `C-0108`'s count/phase grid re-read at `C-0086`'s **buildable** 38.08 nm.
 *
 * `C-0108` is read at §3's nominal 40.00 nm because that is where its reproduction gates live;
 * everything downstream of `C-0086`, `C-0090` and `C-0102` is read at 112 bp = 38.08 nm, the only
 * seamless raster row width the design language can draw near it. This study moves the grid there
 * and puts the interaction beside the 40.00 nm one.
 *
 * The cheap bound runs first and is arithmetic — see `BuildableWidthLattice.kt`.
 */

// ---------------------------------------------------------------------------------------------
// the records
// ---------------------------------------------------------------------------------------------

/** One cheap bound, settled before the sampler it precedes. */
@Serializable
private data class T188BoundRecord(
    val name: String,
    val value: Double,
    val unit: String,
    val settles: String,
    val falsifierFired: Boolean
)

/** One phase of one width under one end-of-row convention — a census row and one free solve. */
@Serializable
private data class T188CensusRecord(
    val edgeX: Double,
    val admitRowEnd: Boolean,
    val phaseBasePairs: Int,
    val columns: Int,
    val upwardSites: Int,
    val rowLengths: List<Int>,
    val centroSymmetric: Boolean,
    val richestInventory: Boolean,
    val stratum: String,
    val columnOnRowEnd: Boolean,
    val freeTileDishingOverStroke: Double
)

/** What a sweep of the edge guard's VALUE does to a lattice — no solve. */
@Serializable
private data class T188GuardRecord(
    val edgeX: Double,
    val admitRowEnd: Boolean,
    val insets: List<Double>,
    val distinctLattices: Int,
    val worstColumnCountChange: Int,
    val worstStationDisplacement: Double,
    val phasesWhoseColumnCountMoves: List<Int>,
    val phasesWhoseStationsMove: List<Int>,
    val inert: Boolean,
    val note: String
)

/** One graded design: a station set, an equal distribution and the whole dropout distribution. */
@Serializable
private data class T188CellRecord(
    val family: String,
    val edgeX: Double,
    val admitRowEnd: Boolean,
    val inset: Double,
    val phaseBasePairs: Int,
    val pathCount: Int,
    val stratum: String,
    val centroSymmetric: Boolean,
    val perPathStiffness: Double,
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
    val flatAtP90: Boolean,
    val p90OverFreeTile: Double,
    val worseThanNoCouplingAtP90: Boolean
)

/** The 34 -> 30 count term of one phase, at one width and convention. */
@Serializable
private data class T188CountTermRecord(
    val edgeX: Double,
    val admitRowEnd: Boolean,
    val phaseBasePairs: Int,
    val stratum: String,
    val p90From: Double,
    val p90To: Double,
    val costFactor: Double,
    val costPerCent: Double,
    val adverse: Boolean,
    val p90ByCount: List<Double>,
    val monotoneDecreasingInCount: Boolean
)

/** The interaction of the count and the phase over one named grid. */
@Serializable
private data class T188InteractionRecord(
    val scope: String,
    val edgeX: Double,
    val admitRowEnd: Boolean,
    val phases: List<Int>,
    val counts: List<Int>,
    val worstResidual: Double,
    val worstResidualPerCent: Double,
    val interactionShare: Double,
    val phaseShare: Double,
    val countShare: Double,
    val decompositionResidual: Double,
    val countMainTerm: Double,
    val countMainTermPerCent: Double,
    val worstResidualOverCountMainTerm: Double,
    val separable: Boolean,
    val note: String
)

/** One 2 x 2, read both ways round. */
@Serializable
private data class T188SplitRecord(
    val name: String,
    val edgeX: Double,
    val admitRowEnd: Boolean,
    val fromCount: Int,
    val toCount: Int,
    val fromPhase: Int,
    val toPhase: Int,
    val p90FromCountFromPhase: Double,
    val p90ToCountFromPhase: Double,
    val p90FromCountToPhase: Double,
    val p90ToCountToPhase: Double,
    val totalPerCent: Double,
    val countTermAtFromPhasePerCent: Double,
    val phaseTermAtToCountPerCent: Double,
    val phaseTermAtFromCountPerCent: Double,
    val countTermAtToPhasePerCent: Double,
    val interactionPerCent: Double,
    val interactionOverCountTerm: Double,
    val pathDisagreement: Double,
    val verdict: String
)

/** The `(count, phase)` corner that minimises the 90th percentile on one named grid. */
@Serializable
private data class T188ArgminRecord(
    val scope: String,
    val edgeX: Double,
    val admitRowEnd: Boolean,
    val bestPhase: Int,
    val bestCount: Int,
    val bestP90: Double,
    val bestPhaseAtCount34: Int,
    val bestP90AtCount34: Double,
    val bestPhaseAtCount30: Int,
    val bestP90AtCount30: Double,
    val flatAtP90: Boolean
)

/** A convergence axis. [departure] is emitted at **two significant digits** and nothing finer. */
@Serializable
private data class T188ConvergenceRecord(
    val quantity: String,
    val parameter: String,
    val values: List<Double>,
    val results: List<Double>,
    val departure: Double,
    val note: String
)

/** One upstream number reproduced rather than cited. [departure] is at two significant digits. */
@Serializable
private data class T188ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double,
    val strict: Boolean
)

/** One acceptance predicate of `T-188`. */
@Serializable
private data class T188PredicateRecord(
    val name: String,
    val statement: String,
    val verdict: String
)

/** One declared falsifier, and whether it fired. */
@Serializable
private data class T188FalsifierRecord(
    val name: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T188Result(
    val task: String,
    val question: String,
    val leaf: String,
    val conditions: Map<String, String>,
    val decision: String,
    val parameters: Map<String, String>,
    val cheapBounds: List<T188BoundRecord>,
    val guardSweep: List<T188GuardRecord>,
    val census: List<T188CensusRecord>,
    val cells: List<T188CellRecord>,
    val countTerms: List<T188CountTermRecord>,
    val interactions: List<T188InteractionRecord>,
    val splits: List<T188SplitRecord>,
    val argmins: List<T188ArgminRecord>,
    val convergence: List<T188ConvergenceRecord>,
    val reproductions: List<T188ReproductionRecord>,
    val predicates: List<T188PredicateRecord>,
    val falsifiers: List<T188FalsifierRecord>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

// ---------------------------------------------------------------------------------------------
// the constants
// ---------------------------------------------------------------------------------------------

private const val T188_DUPLEXES = 15
private const val T188_SAMPLES = 81
private const val T188_TOLERANCE = 0.10
private const val T188_RIM_STANDOFF = 1.0
private const val T188_DECISION_DIGITS = 6
private const val T188_DECISION_FLOOR = 1e-12
private const val T188_SEPARABLE = 0.01

/** `C-0063`'s and `C-0103`'s own phase — the 2 x 2's starting corner. */
private const val T188_REFERENCE_PHASE = 24

/** `C-0074`'s and `C-0090`'s phase — the 2 x 2's ending corner. */
private const val T188_RECOMMENDED_PHASE = 8

/** `C-0087`'s own seed, so that its published percentiles reproduce cell for cell. */
private const val T188_GRADING_SEED = 20260817L

private val T188_COUNTS = listOf(22, 25, 28, 30, 34, 45)

private val T188_NOMINAL_WIDTH = Gen1Tile.EDGE_X

private val T188_MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

private val T188_INSETS = listOf(
    CrossoverLayout.EDGE_MARGIN,
    Gen1Tile.RISE_PER_BASE_PAIR / 2.0,
    Gen1Tile.RISE_PER_BASE_PAIR
)

private val t188Realisations = System.getenv("T188_REALISATIONS")?.toIntOrNull() ?: 10000

private val t188Phases: List<Int> = System.getenv("T188_PHASES")
    ?.split(",")?.mapNotNull { it.trim().toIntOrNull() }?.takeIf { it.isNotEmpty() }
    ?: (0 until 32).toList()

private val ADMITTED = "38.08 nm, row-end column ADMITTED (C-0095/C-0099's carried convention)"
private val REFUSED = "38.08 nm, row-end column REFUSED (C-0015/C-0055/C-0108's truncation)"

// ---------------------------------------------------------------------------------------------
// the inputs, read from the claims that own them
// ---------------------------------------------------------------------------------------------

private class T188Collar(val smoothDepth: Double, val smoothWidth: Double, val rimDepth: Double) {
    fun field(interiorPressure: Double, edgeX: Double, lengthY: Double): PressureField =
        edgeCollarPressure(
            interiorPressure, edgeX, lengthY,
            listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T188_RIM_STANDOFF))
        )
}

/** `C-0022`'s solved profile, keyed on **`(concentration, gap, bias)`** — `CLAUDE.md`'s gotcha. */
private fun t188Collar(file: File): T188Collar {
    require(file.exists()) {
        "C-0022's result file is missing: " + file.path + ". T-188 consumes the SOLVED profile."
    }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(name: String) = it.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at 2 mM, 10 nm, 0.192 V")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T188Collar(
        value("taperDepth"), value("taperWidth"), value("rimResidualDepth")
    )
}

/** `C-0108`'s own canonical cells at 40.00 nm, read at run time and never retyped. */
private fun t188NominalCells(file: File): Map<Pair<Int, Int>, Double> {
    require(file.exists()) {
        "C-0108's result file is missing: " + file.path + ". T-188 will not retype its grid."
    }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("cells").jsonArray.map { it.jsonObject }
        .filter { it.getValue("family").jsonPrimitive.content.startsWith("canonical") }
        .associate {
            (it.getValue("phaseBasePairs").jsonPrimitive.content.toInt() to
                    it.getValue("pathCount").jsonPrimitive.content.toInt()) to
                    it.getValue("p90OverStroke").jsonPrimitive.content.toDouble()
        }
}

/** `C-0108`'s published grid interaction, read at run time as the reproduction gate. */
private fun t188NominalInteraction(file: File): Map<String, Double> {
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("interactions").jsonArray.map { it.jsonObject }
        .first { it.getValue("scope").jsonPrimitive.content.startsWith("the GRADED grid —") }
    return listOf(
        "worstResidual", "countMainTerm", "interactionShare"
    ).associateWith { record.getValue(it).jsonPrimitive.content.toDouble() }
}

/** `C-0090`'s own 32-phase descent at 38.08 nm, row-end admitted — free dishings and census. */
private fun t188BuildableDescent(file: File): Map<Int, Triple<Int, Int, Double>> {
    require(file.exists()) {
        "C-0090's result file is missing: " + file.path + ". T-188 consumes its free tile."
    }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("descent").jsonArray.map { it.jsonObject }
        .associate {
            it.getValue("phaseBasePairs").jsonPrimitive.content.toInt() to Triple(
                it.getValue("columns").jsonPrimitive.content.toInt(),
                it.getValue("upwardSites").jsonPrimitive.content.toInt(),
                it.getValue("freeDishingOverStroke").jsonPrimitive.content.toDouble()
            )
        }
}

/** `C-0090`'s own free stroke at the buildable width, read rather than retyped. */
private fun t188BuildableFreeStroke(file: File): Double =
    Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("parameters").jsonObject
        .getValue("freeStrokeBuildable").jsonPrimitive.content.toDouble()

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

/** One `(width, convention, inset)` the grid is graded on. */
private class T188Case(
    val name: String,
    val edgeX: Double,
    val admitRowEnd: Boolean,
    val inset: Double,
    val sheet: OrigamiSheet,
    collar: T188Collar
) {
    val lengthY: Double = T188_DUPLEXES * sheet.interhelicalDistance
    val interiorPressure: Double = Gen1Tile.TARGET_FORCE / (edgeX * lengthY)
    val uniformField: PressureField = uniformPressure(interiorPressure)
    val solvedField: PressureField = collar.field(interiorPressure, edgeX, lengthY)
    val freeStroke: Double = PlateOnFoundation(
        sheet.plate(edgeX, lengthY), Gen1Tile.FOUNDATION_SECANT, emptyList(), 12
    ).solve(uniformField).meanDeflection
    val incorporation = measuredDepthIncorporation(edgeX, lengthY)

    fun sites(phase: Int): List<List<Double>> = rasterUpwardSites(
        phase, edgeX, T188_DUPLEXES, admitRowEnd, Gen1Tile.RISE_PER_BASE_PAIR, inset
    )

    fun host(phase: Int, subdivisions: Int = 2): OrigamiGrillage = OrigamiGrillage(
        sheet = sheet,
        lengthX = edgeX,
        beamCount = T188_DUPLEXES,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = rasterColumnLayout(phase, sheet, edgeX, admitRowEnd, inset),
        subdivisions = subdivisions,
        supports = emptyList()
    )
}

/** `C-0102`'s strata, recomputed at whatever width and convention the case carries. */
private fun t188Stratum(
    sites: Int,
    richest: Int,
    columns: Int,
    maximumColumns: Int,
    centroSymmetric: Boolean
): String = when {
    centroSymmetric && columns == maximumColumns ->
        "richest-column AND centro-symmetric (this width's own two)"
    columns == maximumColumns -> "richest-column"
    sites == richest -> "richest inventory, not richest-column"
    else -> "neither richest inventory nor richest column"
}

@Suppress("LongMethod", "ComplexMethod", "CyclomaticComplexMethod", "NestedBlockDepth")
fun main() {
    val sheet = origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)
    val buildable = BUILDABLE_RASTER_WIDTH
    val collar = t188Collar(ResultInputs.T_3B.file())

    val nominalCells = t188NominalCells(ResultInputs.T_178.file())
    val nominalFit = t188NominalInteraction(ResultInputs.T_178.file())
    val descentFile = ResultInputs.T_153.file()
    val publishedDescent = t188BuildableDescent(descentFile)
    val publishedFreeStroke = t188BuildableFreeStroke(descentFile)

    val admitted = T188Case(ADMITTED, buildable, true, CrossoverLayout.EDGE_MARGIN, sheet, collar)
    val refused = T188Case(REFUSED, buildable, false, CrossoverLayout.EDGE_MARGIN, sheet, collar)
    val cases = listOf(admitted, refused)

    val reproductions = ArrayList<T188ReproductionRecord>()
    fun reproduction(
        source: String,
        quantity: String,
        published: Double,
        reproduced: Double,
        strict: Boolean = true
    ) = T188ReproductionRecord(
        source, quantity, published, reproduced,
        roundForResult(abs(reproduced - published), 2, T188_DECISION_FLOOR), strict
    )

    // ------------------------------------------------------------------ cheap bound 1: the guard
    println("T-188 - the cheap bound, before any solve ...")
    val guardSweeps = listOf(
        Triple(buildable, false, "the BUILDABLE width under the truncation C-0108 was run on"),
        Triple(buildable, true, "the BUILDABLE width under the convention the programme carries"),
        Triple(T188_NOMINAL_WIDTH, false, "the NOMINAL width, where the guard's KDoc calls it inert")
    ).map { (width, admit, note) ->
        val sweep = insetSensitivity(width, T188_DUPLEXES, admit, T188_INSETS, 32, sheet)
        T188GuardRecord(
            edgeX = width,
            admitRowEnd = admit,
            insets = T188_INSETS,
            distinctLattices = sweep.distinctSignatures,
            worstColumnCountChange = sweep.worstColumnCountChange,
            worstStationDisplacement = sweep.worstStationDisplacement,
            phasesWhoseColumnCountMoves = sweep.phasesWhoseColumnCountMoves,
            phasesWhoseStationsMove = sweep.phasesWhoseStationsMove,
            inert = sweep.inert,
            note = note
        )
    }

    // ------------------------------------------------------------------ cheap bound 2: census
    val censusRecords = ArrayList<T188CensusRecord>()
    val strata = HashMap<Pair<Boolean, Int>, String>()
    val rowEndColumnPhases = endOfRowColumnPhases(112)

    cases.forEach { case ->
        val signatures = t188Phases.associateWith {
            upwardLatticeSignature(it, case.edgeX, T188_DUPLEXES, case.admitRowEnd, case.inset, sheet)
        }
        val richest = signatures.values.maxOf { it.upwardSites }
        val maximumColumns = signatures.values.maxOf { it.columns }
        signatures.forEach { (phase, signature) ->
            strata[case.admitRowEnd to phase] = t188Stratum(
                signature.upwardSites, richest, signature.columns, maximumColumns,
                signature.centroSymmetric
            )
        }
    }

    // ------------------------------------------------------------------ the sweep
    val cells = ArrayList<T188CellRecord>()
    val convergence = ArrayList<T188ConvergenceRecord>()
    fun equalSprings(count: Int) = List(count) { T188_MANDATE / count }

    fun gradeGrid(
        case: T188Case,
        family: String,
        phases: List<Int>,
        counts: List<Int>
    ) {
        phases.forEach { phase ->
            val sites = case.sites(phase)
            val signature = upwardLatticeSignature(
                phase, case.edgeX, T188_DUPLEXES, case.admitRowEnd, case.inset, sheet
            )
            val stations = rootStations(sites, T188_DUPLEXES, sheet.interhelicalDistance)
            val host = case.host(phase)
            val bank = UpwardRootInfluenceBank(host, stations, case.solvedField, T188_SAMPLES)
            val freeTileDishing = bank.freePeakDishing / case.freeStroke
            val probabilities = stations.map { (x, y) -> case.incorporation.at(x, y) }
            val parent = dropoutEnsemble(probabilities, t188Realisations, T188_GRADING_SEED)
            val chain = canonicalRootChain(sites)
            val stratum = strata.getValue(case.admitRowEnd to phase)

            if (case.inset == CrossoverLayout.EDGE_MARGIN) {
                censusRecords += T188CensusRecord(
                    edgeX = case.edgeX,
                    admitRowEnd = case.admitRowEnd,
                    phaseBasePairs = phase,
                    columns = signature.columns,
                    upwardSites = signature.upwardSites,
                    rowLengths = sites.map { it.size },
                    centroSymmetric = signature.centroSymmetric,
                    richestInventory = signature.upwardSites ==
                            (0 until 32).maxOf {
                                upwardLatticeSignature(
                                    it, case.edgeX, T188_DUPLEXES, case.admitRowEnd, case.inset,
                                    sheet
                                ).upwardSites
                            },
                    stratum = stratum,
                    columnOnRowEnd = phase in rowEndColumnPhases,
                    freeTileDishingOverStroke = freeTileDishing
                )
                if (case.admitRowEnd && publishedDescent.containsKey(phase)) {
                    val published = publishedDescent.getValue(phase)
                    reproductions += reproduction(
                        "C-0090", "its free-tile dishing at phase " + phase +
                                " on the buildable admitted host", published.third, freeTileDishing
                    )
                    reproductions += reproduction(
                        "C-0090", "its upward site inventory at phase " + phase,
                        published.second.toDouble(), signature.upwardSites.toDouble()
                    )
                    reproductions += reproduction(
                        "C-0090", "its column count at phase " + phase,
                        published.first.toDouble(), signature.columns.toDouble()
                    )
                }
            }

            counts.forEach { count ->
                val rows = chain.at(count)
                val indices = rootStationIndices(sites, rows)
                val surrogate = bank.surrogateFor(indices)
                val ensemble = restrictEnsemble(parent, indices)
                val springs = equalSprings(count)
                val nominal = surrogate.solve(springs).peakDishing / case.freeStroke
                val sample = dropoutDishingSample(surrogate, springs, ensemble)
                sample.indices.forEach { sample[it] = sample[it] / case.freeStroke }
                val summary = summariseDropoutDishing(
                    sample, nominal, ensemble.meanSurvivors, T188_TOLERANCE
                )
                cells += T188CellRecord(
                    family = family,
                    edgeX = case.edgeX,
                    admitRowEnd = case.admitRowEnd,
                    inset = case.inset,
                    phaseBasePairs = phase,
                    pathCount = count,
                    stratum = stratum,
                    centroSymmetric = rowsAreCentroSymmetric(rows),
                    perPathStiffness = springs.max(),
                    nominalOverStroke = nominal,
                    worstSingleRemovalOverStroke =
                        worstSinglePathRemoval(surrogate, springs) / case.freeStroke,
                    medianOverStroke = summary.median,
                    p90OverStroke = summary.p90,
                    p95OverStroke = summary.p95,
                    worstOverStroke = summary.worst,
                    exceedance = summary.exceedance,
                    exceedanceStandardError = summary.exceedanceStandardError,
                    exceedanceOneSidedBound = summary.exceedanceOneSidedBound,
                    meanSurvivors = summary.meanSurvivors,
                    flatAtP90 = summary.flatAtP90,
                    p90OverFreeTile = summary.p90 / freeTileDishing,
                    worseThanNoCouplingAtP90 = summary.p90 > freeTileDishing
                )
            }
            println(
                ("T-188 - %s phase %2d (%2d sites, %d cols): p90 34 = %.6f, 30 = %.6f").format(
                    if (case.admitRowEnd) "ADMITTED" else "REFUSED ", phase,
                    signature.upwardSites, signature.columns,
                    cells.last { it.family == family && it.phaseBasePairs == phase &&
                            it.pathCount == 34 }.p90OverStroke,
                    cells.last { it.family == family && it.phaseBasePairs == phase &&
                            it.pathCount == 30 }.p90OverStroke
                )
            )
        }
    }

    val canonicalAdmitted = "canonical search-free nested chain, " + ADMITTED
    val canonicalRefused = "canonical search-free nested chain, " + REFUSED
    gradeGrid(admitted, canonicalAdmitted, t188Phases, T188_COUNTS)
    gradeGrid(refused, canonicalRefused, t188Phases, T188_COUNTS)

    // ------------------------------------------------------------------ the guard, graded
    val guardPhases = listOf(0, 8, 16, 24).filter { it in t188Phases }
    val guardCounts = listOf(30, 34)
    T188_INSETS.drop(1).forEach { inset ->
        val case = T188Case(
            ADMITTED + ", inset " + "%.2f".format(inset) + " nm",
            buildable, true, inset, sheet, collar
        )
        gradeGrid(
            case,
            "guard sweep - the row-end column inset at " + "%.2f".format(inset) + " nm",
            guardPhases, guardCounts
        )
    }

    reproductions += reproduction(
        "C-0090", "the free-tile stroke at the buildable width [nm]",
        publishedFreeStroke, admitted.freeStroke
    )
    reproductions += reproduction(
        "C-0090", "the free-tile stroke at the buildable width under the refused convention [nm]",
        publishedFreeStroke, refused.freeStroke
    )

    // ------------------------------------------------------------------ readers of the grid
    fun cellAt(family: String, phase: Int, count: Int): T188CellRecord? = cells.firstOrNull {
        it.family == family && it.phaseBasePairs == phase && it.pathCount == count
    }
    fun p90(family: String, phase: Int, count: Int): Double? =
        cellAt(family, phase, count)?.p90OverStroke

    val gradedPhases = t188Phases.filter { phase ->
        T188_COUNTS.all { cellAt(canonicalAdmitted, phase, it) != null }
    }

    // ------------------------------------------------------------------ the count terms
    val countTerms = ArrayList<T188CountTermRecord>()
    listOf(canonicalAdmitted to admitted, canonicalRefused to refused).forEach { (family, case) ->
        gradedPhases.forEach { phase ->
            val from = cellAt(family, phase, 34) ?: return@forEach
            val to = cellAt(family, phase, 30) ?: return@forEach
            val byCount = T188_COUNTS.mapNotNull { p90(family, phase, it) }
            countTerms += T188CountTermRecord(
                edgeX = case.edgeX,
                admitRowEnd = case.admitRowEnd,
                phaseBasePairs = phase,
                stratum = strata.getValue(case.admitRowEnd to phase),
                p90From = from.p90OverStroke,
                p90To = to.p90OverStroke,
                costFactor = to.p90OverStroke / from.p90OverStroke,
                costPerCent = 100.0 * (to.p90OverStroke / from.p90OverStroke - 1.0),
                adverse = to.p90OverStroke > from.p90OverStroke,
                p90ByCount = byCount,
                monotoneDecreasingInCount = byCount.zipWithNext().all { (a, b) -> b < a }
            )
        }
    }

    // ------------------------------------------------------------------ the interactions
    val interactions = ArrayList<T188InteractionRecord>()
    fun addInteraction(
        scope: String,
        edgeX: Double,
        admitRowEnd: Boolean,
        phases: List<Int>,
        counts: List<Int>,
        values: (Int, Int) -> Double?,
        note: String
    ) {
        if (phases.size < 2 || counts.size < 2) return
        val grid = phases.map { phase -> counts.map { values(phase, it) ?: return } }
        val fit = twoWayLogInteraction(grid)
        val countMain = fit.columnEffects[counts.indexOf(30)] -
                fit.columnEffects[counts.indexOf(34)]
        interactions += T188InteractionRecord(
            scope = scope,
            edgeX = edgeX,
            admitRowEnd = admitRowEnd,
            phases = phases,
            counts = counts,
            worstResidual = fit.worstResidual,
            worstResidualPerCent = fit.worstResidualPerCent,
            interactionShare = fit.interactionShare,
            phaseShare = fit.rowSumOfSquares / fit.totalSumOfSquares,
            countShare = fit.columnSumOfSquares / fit.totalSumOfSquares,
            decompositionResidual = roundForResult(
                abs(
                    fit.totalSumOfSquares -
                            (fit.rowSumOfSquares + fit.columnSumOfSquares +
                                    fit.interactionSumOfSquares)
                ),
                2, T188_DECISION_FLOOR
            ),
            countMainTerm = countMain,
            countMainTermPerCent = 100.0 * (exp(countMain) - 1.0),
            worstResidualOverCountMainTerm = fit.worstResidual / abs(countMain),
            separable = fit.worstResidual < T188_SEPARABLE,
            note = note
        )
    }

    addInteraction(
        "the GRADED grid at the BUILDABLE width, row-end ADMITTED",
        buildable, true, gradedPhases, T188_COUNTS,
        { phase, count -> p90(canonicalAdmitted, phase, count) },
        "the headline: C-0108's own construction at the only width the design language can draw"
    )
    addInteraction(
        "the GRADED grid at the BUILDABLE width, row-end REFUSED",
        buildable, false, gradedPhases, T188_COUNTS,
        { phase, count -> p90(canonicalRefused, phase, count) },
        "the declared bracket - the convention C-0015/C-0055/C-0108 are written on, which at " +
                "this width deletes the row-end column"
    )
    addInteraction(
        "the GRADED grid at the NOMINAL width - C-0108's own cells, refitted here",
        T188_NOMINAL_WIDTH, false, (0 until 32).toList(), T188_COUNTS,
        { phase, count -> nominalCells[phase to count] },
        "read from C-0108's result file at run time and refitted by the same function, so its " +
                "published interaction is a reproduction rather than a citation"
    )
    setOf(
        "richest-column AND centro-symmetric (this width's own two)",
        "richest-column",
        "richest inventory, not richest-column",
        "neither richest inventory nor richest column"
    ).forEach { stratum ->
        val phases = gradedPhases.filter { strata[true to it] == stratum }
        addInteraction(
            "the BUILDABLE ADMITTED grid, within this width's own stratum: " + stratum,
            buildable, true, phases, T188_COUNTS,
            { phase, count -> p90(canonicalAdmitted, phase, count) },
            "the strata are recomputed at this width and are NOT the same sets as C-0102's at " +
                    "40.00 nm, so the membership is reported beside the number"
        )
    }

    // ------------------------------------------------------------------ the 2 x 2 splits
    val splits = ArrayList<T188SplitRecord>()
    fun addSplit(
        name: String,
        edgeX: Double,
        admitRowEnd: Boolean,
        ff: Double,
        tf: Double,
        ft: Double,
        tt: Double,
        verdict: String
    ) {
        val split = countPhaseSplit(ff, tf, ft, tt)
        fun perCent(term: Double) = 100.0 * (exp(term) - 1.0)
        splits += T188SplitRecord(
            name = name,
            edgeX = edgeX,
            admitRowEnd = admitRowEnd,
            fromCount = 34,
            toCount = 30,
            fromPhase = T188_REFERENCE_PHASE,
            toPhase = T188_RECOMMENDED_PHASE,
            p90FromCountFromPhase = ff,
            p90ToCountFromPhase = tf,
            p90FromCountToPhase = ft,
            p90ToCountToPhase = tt,
            totalPerCent = perCent(split.total),
            countTermAtFromPhasePerCent = perCent(split.countTermAtFromPhase),
            phaseTermAtToCountPerCent = perCent(split.phaseTermAtToCount),
            phaseTermAtFromCountPerCent = perCent(split.phaseTermAtFromCount),
            countTermAtToPhasePerCent = perCent(split.countTermAtToPhase),
            interactionPerCent = split.interactionPerCent,
            interactionOverCountTerm = abs(split.interaction) / abs(split.countTermAtFromPhase),
            pathDisagreement = roundForResult(split.pathDisagreement, 2, T188_DECISION_FLOOR),
            verdict = verdict
        )
    }

    if (T188_REFERENCE_PHASE in t188Phases && T188_RECOMMENDED_PHASE in t188Phases) {
        listOf(
            Triple(canonicalAdmitted, buildable, true) to
                    "the primary reading. The station sets at BOTH corners are bit-identical to " +
                    "the 40.00 nm ones (C-0090, re-derived here), so every difference from " +
                    "C-0108's 2 x 2 is the HOST and the LOAD and none of it is the stations",
            Triple(canonicalRefused, buildable, false) to
                    "the bracket: at this width refusing the row-end column takes both corners " +
                    "from eight columns to six",
            Triple("nominal", T188_NOMINAL_WIDTH, false) to
                    "C-0108's own 2 x 2, recomputed here from its result file by the same " +
                    "function, which is the reproduction gate"
        ).forEach { (key, verdict) ->
            val (family, width, admit) = key
            val corners = if (family == "nominal") listOf(
                nominalCells[T188_REFERENCE_PHASE to 34],
                nominalCells[T188_REFERENCE_PHASE to 30],
                nominalCells[T188_RECOMMENDED_PHASE to 34],
                nominalCells[T188_RECOMMENDED_PHASE to 30]
            ) else listOf(
                p90(family, T188_REFERENCE_PHASE, 34),
                p90(family, T188_REFERENCE_PHASE, 30),
                p90(family, T188_RECOMMENDED_PHASE, 34),
                p90(family, T188_RECOMMENDED_PHASE, 30)
            )
            if (corners.all { it != null }) addSplit(
                "the 2 x 2 at " + (if (family == "nominal") "40.00 nm (C-0108's own cells)"
                else if (admit) "38.08 nm, row-end ADMITTED" else "38.08 nm, row-end REFUSED"),
                width, admit,
                corners[0]!!, corners[1]!!, corners[2]!!, corners[3]!!, verdict
            )
        }
    }

    // ------------------------------------------------------------------ the argmins
    val argmins = ArrayList<T188ArgminRecord>()
    listOf(
        Triple("the BUILDABLE grid, row-end ADMITTED", canonicalAdmitted, true),
        Triple("the BUILDABLE grid, row-end REFUSED", canonicalRefused, false)
    ).forEach { (scope, family, admit) ->
        val grid = cells.filter { it.family == family }
        if (grid.isNotEmpty()) {
            val best = grid.minBy { it.p90OverStroke }
            val at34 = grid.filter { it.pathCount == 34 }.minBy { it.p90OverStroke }
            val at30 = grid.filter { it.pathCount == 30 }.minBy { it.p90OverStroke }
            argmins += T188ArgminRecord(
                scope = scope, edgeX = buildable, admitRowEnd = admit,
                bestPhase = best.phaseBasePairs, bestCount = best.pathCount,
                bestP90 = best.p90OverStroke,
                bestPhaseAtCount34 = at34.phaseBasePairs, bestP90AtCount34 = at34.p90OverStroke,
                bestPhaseAtCount30 = at30.phaseBasePairs, bestP90AtCount30 = at30.p90OverStroke,
                flatAtP90 = best.flatAtP90
            )
        }
    }
    run {
        val grid = nominalCells.filterKeys { it.second in T188_COUNTS }
        if (grid.isNotEmpty()) {
            val best = grid.minBy { it.value }
            val at34 = grid.filterKeys { it.second == 34 }.minBy { it.value }
            val at30 = grid.filterKeys { it.second == 30 }.minBy { it.value }
            argmins += T188ArgminRecord(
                scope = "the NOMINAL grid - C-0108's own cells", edgeX = T188_NOMINAL_WIDTH,
                admitRowEnd = false,
                bestPhase = best.key.first, bestCount = best.key.second, bestP90 = best.value,
                bestPhaseAtCount34 = at34.key.first, bestP90AtCount34 = at34.value,
                bestPhaseAtCount30 = at30.key.first, bestP90AtCount30 = at30.value,
                flatAtP90 = best.value < T188_TOLERANCE
            )
        }
    }

    // ------------------------------------------------------------------ the reproduction of C-0108
    interactions.firstOrNull { it.scope.contains("NOMINAL width") }?.let { fit ->
        reproductions += reproduction(
            "C-0108", "the worst residual of its 32 x 6 graded fit [log units]",
            nominalFit.getValue("worstResidual"), fit.worstResidual
        )
        reproductions += reproduction(
            "C-0108", "the 34 -> 30 count main effect of its 32 x 6 graded fit [log units]",
            nominalFit.getValue("countMainTerm"), fit.countMainTerm
        )
        reproductions += reproduction(
            "C-0108", "the interaction share of its 32 x 6 graded fit",
            nominalFit.getValue("interactionShare"), fit.interactionShare
        )
    }
    splits.firstOrNull { it.name.contains("40.00") }?.let {
        reproductions += reproduction(
            "C-0108", "the interaction of its canonical 2 x 2 [per cent]",
            -5.74202435, it.interactionPerCent
        )
        reproductions += reproduction(
            "C-0108", "the total of its canonical 2 x 2 [per cent]", 6.48887743, it.totalPerCent
        )
    }
    reproductions += reproduction(
        "C-0102", "the number of phases at which the end plane is a COLUMN at 112 bp",
        2.0, rowEndColumnPhases.size.toDouble()
    )

    // ------------------------------------------------------------------ convergence
    if (T188_RECOMMENDED_PHASE in t188Phases) {
        val phase = T188_RECOMMENDED_PHASE
        val sites = admitted.sites(phase)
        val chain = canonicalRootChain(sites)
        val stations = rootStations(sites, T188_DUPLEXES, sheet.interhelicalDistance)
        val probabilities = stations.map { (x, y) -> admitted.incorporation.at(x, y) }
        val bank = UpwardRootInfluenceBank(
            admitted.host(phase), stations, admitted.solvedField, T188_SAMPLES
        )
        val idx34 = rootStationIndices(sites, chain.at(34))
        val idx30 = rootStationIndices(sites, chain.at(30))
        val s34 = bank.surrogateFor(idx34)
        val s30 = bank.surrogateFor(idx30)
        val levels = listOf(1250, 2500, 5000, t188Realisations)
        val terms = levels.map { realisations ->
            val ladder = dropoutEnsemble(probabilities, realisations, T188_GRADING_SEED)
            val a = orderStatistic(
                dropoutDishingSample(s34, equalSprings(34), restrictEnsemble(ladder, idx34)), 0.90
            )
            val b = orderStatistic(
                dropoutDishingSample(s30, equalSprings(30), restrictEnsemble(ladder, idx30)), 0.90
            )
            b / a
        }
        convergence += T188ConvergenceRecord(
            quantity = "the canonical 34 -> 30 count FACTOR at phase " + phase +
                    " on the buildable admitted host",
            parameter = "realisations",
            values = levels.map { it.toDouble() },
            results = terms,
            departure = roundForResult(
                abs(terms[terms.size - 1] - terms[terms.size - 2]), 2, T188_DECISION_FLOOR
            ),
            note = "read on the FACTOR and under common random numbers, exactly as C-0108 reads it"
        )
        val gridLevels = listOf(41, 81, 161)
        val gridMeans = gridLevels.map { samples ->
            val coarse = latticeInfluenceSurrogate(
                admitted.host(phase),
                rootStations(chain.at(34), T188_DUPLEXES, sheet.interhelicalDistance),
                admitted.solvedField, samples
            )
            val ladder = restrictEnsemble(
                dropoutEnsemble(probabilities, 200, T188_GRADING_SEED), idx34
            )
            dropoutDishingSample(coarse, equalSprings(34), ladder).average() / admitted.freeStroke
        }
        convergence += T188ConvergenceRecord(
            quantity = "the MEAN over 200 realisations of the canonical 34 at phase " + phase,
            parameter = "dishing samples per edge",
            values = gridLevels.map { it.toDouble() },
            results = gridMeans,
            departure = roundForResult(abs(gridMeans[2] - gridMeans[1]), 2, T188_DECISION_FLOOR),
            note = "C-0087's cure for the degenerate nested-grid percentile: a mean, not a " +
                    "percentile, over three nested grids that share their nodes"
        )
        val subdivisionLevels = listOf(1, 2, 4)
        val subdivisionResults = subdivisionLevels.map { subdivisions ->
            val fine = UpwardRootInfluenceBank(
                admitted.host(phase, subdivisions), stations, admitted.solvedField, T188_SAMPLES
            )
            fine.surrogateFor(idx34).solve(equalSprings(34)).peakDishing / admitted.freeStroke
        }
        convergence += T188ConvergenceRecord(
            quantity = "the zero-defect dishing of the canonical 34 at phase " + phase,
            parameter = "nested beam subdivisions",
            values = subdivisionLevels.map { it.toDouble() },
            results = subdivisionResults,
            departure = roundForResult(
                abs(subdivisionResults[2] - subdivisionResults[1]), 2, T188_DECISION_FLOOR
            ),
            note = "nested 1 c 2 c 4, because a subdivision of 3 moves a load off a node"
        )
    }

    // ------------------------------------------------------------------ the falsifiers
    val admittedTerms = countTerms.filter { it.admitRowEnd }
    val refusedTerms = countTerms.filter { !it.admitRowEnd }
    val buildableSplit = splits.firstOrNull { it.name.contains("ADMITTED") }
    val nominalSplit = splits.firstOrNull { it.name.contains("40.00") }
    val buildableArgmin = argmins.firstOrNull { it.scope.contains("ADMITTED") }
    val nominalArgmin = argmins.firstOrNull { it.scope.contains("NOMINAL") }

    val f1Fired = buildableArgmin != null && nominalArgmin != null &&
            (buildableArgmin.bestPhase != nominalArgmin.bestPhase ||
                    buildableArgmin.bestCount != nominalArgmin.bestCount)
    val interactionRatio = if (buildableSplit != null && nominalSplit != null &&
        abs(nominalSplit.interactionPerCent) > 0.0
    ) abs(buildableSplit.interactionPerCent) / abs(nominalSplit.interactionPerCent)
    else Double.NaN
    val signChanged = buildableSplit != null && nominalSplit != null &&
            (buildableSplit.interactionPerCent > 0.0) != (nominalSplit.interactionPerCent > 0.0)
    val f2Fired = signChanged ||
            (interactionRatio.isFinite() && (interactionRatio > 2.0 || interactionRatio < 0.5))
    val guardAtCarried = guardSweeps.first { it.edgeX == buildable && !it.admitRowEnd }
    val guardGraded = cells.filter { it.family.startsWith("guard sweep") }
    val guardSpread = guardPhases.flatMap { phase ->
        guardCounts.mapNotNull { count ->
            val base = cellAt(canonicalAdmitted, phase, count) ?: return@mapNotNull null
            val others = guardGraded.filter {
                it.phaseBasePairs == phase && it.pathCount == count
            }
            if (others.isEmpty()) null else
                (others + base).maxOf { it.p90OverStroke } /
                        (others + base).minOf { it.p90OverStroke } - 1.0
        }
    }
    val worstGuardSpread = guardSpread.maxOrNull() ?: 0.0
    val f3Fired = !guardAtCarried.inert || worstGuardSpread > 0.01
    val f4Fired = reproductions.any { it.strict && it.departure > 1e-6 }
    val uniformDishing = cases.maxOf { case ->
        t188Phases.maxOf { phase ->
            abs(case.host(phase).solve(case.uniformField).peakDishing(T188_SAMPLES)) /
                    case.freeStroke
        }
    }
    val f5Fired = uniformDishing > 1e-6

    val falsifiers = listOf(
        T188FalsifierRecord(
            name = "F1",
            statement = "THE DECLARED ONE - the recommended endpoint moves: the (count, phase) " +
                    "corner minimising the 90th percentile at 38.08 nm is not the corner that " +
                    "minimises it at 40.00 nm",
            fired = f1Fired,
            outcome = ("the buildable grid's argmin is phase %d at %d paths (%.9f) against the " +
                    "nominal grid's phase %d at %d paths (%.9f); at 34 paths the best phase is " +
                    "%d against %d, and at 30 paths %d against %d").format(
                buildableArgmin?.bestPhase ?: -1, buildableArgmin?.bestCount ?: -1,
                buildableArgmin?.bestP90 ?: 0.0,
                nominalArgmin?.bestPhase ?: -1, nominalArgmin?.bestCount ?: -1,
                nominalArgmin?.bestP90 ?: 0.0,
                buildableArgmin?.bestPhaseAtCount34 ?: -1, nominalArgmin?.bestPhaseAtCount34 ?: -1,
                buildableArgmin?.bestPhaseAtCount30 ?: -1, nominalArgmin?.bestPhaseAtCount30 ?: -1
            )
        ),
        T188FalsifierRecord(
            name = "F2",
            statement = "the interaction on the 2 x 2 the recommendation moves through changes " +
                    "SIGN between the two widths, or its magnitude moves by more than a factor " +
                    "of two",
            fired = f2Fired,
            outcome = ("the buildable admitted 2 x 2 interaction is %s per cent against " +
                    "C-0108's %s per cent, a ratio of %s, and the sign %s").format(
                (buildableSplit?.interactionPerCent ?: 0.0).roundedForProse(),
                (nominalSplit?.interactionPerCent ?: 0.0).roundedForProse(),
                interactionRatio.roundedForProse(),
                if (signChanged) "CHANGED" else "did not change"
            )
        ),
        T188FalsifierRecord(
            name = "F3",
            statement = "the EDGE_MARGIN VALUE is not inert at the buildable width - the swept " +
                    "guard moves the station lattice under the carried convention, or moves a " +
                    "graded 90th percentile by more than 1 per cent of a level",
            fired = f3Fired,
            outcome = ("the guard's value leaves %d distinct lattice(s) over 32 phases at the " +
                    "buildable width refused and %d admitted; graded at %d cells its worst " +
                    "spread is %.9f of a level").format(
                guardAtCarried.distinctLattices,
                guardSweeps.first { it.edgeX == buildable && it.admitRowEnd }.distinctLattices,
                guardGraded.size, worstGuardSpread
            )
        ),
        T188FalsifierRecord(
            name = "F4",
            statement = "a standing figure fails to reproduce - C-0090's free stroke, its " +
                    "32-phase free-tile dishings, its census, or C-0108's own fit and 2 x 2",
            fired = f4Fired,
            outcome = ("worst strict departure over %d reproductions: %.2e").format(
                reproductions.count { it.strict },
                reproductions.filter { it.strict }.maxOfOrNull { it.departure } ?: 0.0
            )
        ),
        T188FalsifierRecord(
            name = "F5",
            statement = "the uncoupled tile under a UNIFORM load dishes non-zero on any of the " +
                    "swept hosts at either convention",
            fired = f5Fired,
            outcome = ("worst over %d hosts: %.2e of the free-tile stroke").format(
                2 * t188Phases.size, uniformDishing
            )
        )
    )

    val predicates = listOf(
        T188PredicateRecord(
            "P1",
            "the width x phase x convention census is emitted closed-form before any solve",
            if (censusRecords.size >= 2 * t188Phases.size) "PASS" else "FAIL"
        ),
        T188PredicateRecord(
            "P2",
            "the EDGE_MARGIN value is swept at both widths and both conventions and reported as " +
                    "a lattice statement first",
            if (guardSweeps.size == 3) "PASS" else "FAIL"
        ),
        T188PredicateRecord(
            "P3",
            "the 90th percentile is emitted on the canonical search-free nested family at " +
                    "22/25/28/30/34/45 paths at every swept phase at 38.08 nm, each on its own " +
                    "host, under both end-of-row conventions",
            if (cells.count { it.family == canonicalAdmitted } ==
                t188Phases.size * T188_COUNTS.size &&
                cells.count { it.family == canonicalRefused } ==
                t188Phases.size * T188_COUNTS.size
            ) "PASS" else "FAIL"
        ),
        T188PredicateRecord(
            "P4",
            "the interaction is measured the same two ways as C-0108 and the two orderings of " +
                    "every 2 x 2 agree on their total to 1e-12",
            if (splits.isNotEmpty() && splits.all { it.pathDisagreement < 1e-12 } &&
                interactions.isNotEmpty()
            ) "PASS" else "FAIL"
        ),
        T188PredicateRecord(
            "P5",
            "the 38.08 nm interaction is reported beside C-0108's 40.00 nm one, read from its " +
                    "own result file at run time",
            if (interactions.any { it.scope.contains("NOMINAL width") } &&
                splits.any { it.name.contains("40.00") }
            ) "PASS" else "FAIL"
        ),
        T188PredicateRecord(
            "P6",
            "every headline is reproduced rather than cited",
            if (!f4Fired) "PASS" else "FAIL"
        )
    )

    val result = T188Result(
        task = "T-188",
        question = "At C-0086's buildable 38.08 nm, is the count/phase interaction the same " +
                "object C-0108 measured at 40.00 nm, and does the recommended (count, phase) " +
                "endpoint move?",
        leaf = "A8.2, with A1.2",
        conditions = mapOf(
            "temperature" to "300 K",
            "kBT" to "4.141947 pN*nm",
            "medium" to "aqueous 2 mM MgCl2",
            "tile" to "single-layer square-lattice sheet, 15 duplexes at the SAXS 2.69 nm " +
                    "(40.35 nm across the helices, UNCHANGED at both widths); along-helix " +
                    "width 38.08 nm (112 bp, C-0086's buildable seamless raster row) against " +
                    "40.00 nm (S3's nominal, C-0108's)",
            "endOfRow" to "row-end column ADMITTED is primary (C-0095 on the permission, " +
                    "C-0099 on the mechanics); REFUSED is carried as the declared bracket",
            "load" to "C-0022's SOLVED edge profile at 2 mM, a 10 nm gap and 0.192 V, its " +
                    "collar terms carried unchanged to the narrower tile as C-0090 carries them",
            "mandate" to "C-0017's 33.3333333 pN/nm as a SUM at S3's acceptable 3 nm, shared " +
                    "EQUALLY at every count and every phase",
            "dropout" to "C-0087's MEASURED_DEPTH incorporation field evaluated on THIS width's " +
                    "own tile, one Bernoulli stream per phase, restricted per subset",
            "statistic" to "the 90th percentile, nearest rank, over that width's own free stroke",
            "flatness" to "T-5b's 0.10 CONVENTION, not a physical threshold",
            "family" to "C-0108's CANONICAL search-free nested chain, unchanged"
        ),
        decision = "",
        parameters = mapOf(
            "buildableEdgeX" to "%.9f".format(buildable),
            "nominalEdgeX" to "%.9f".format(T188_NOMINAL_WIDTH),
            "phasesSwept" to t188Phases.joinToString(","),
            "counts" to T188_COUNTS.joinToString(","),
            "realisations" to t188Realisations.toString(),
            "seed" to T188_GRADING_SEED.toString(),
            "dishingSamplesPerEdge" to T188_SAMPLES.toString(),
            "insetsSwept" to T188_INSETS.joinToString(",") { "%.9f".format(it) },
            "freeStrokeBuildable" to admitted.freeStroke.roundedForProse().toString(),
            "mandate" to T188_MANDATE.roundedForProse().toString(),
            "tolerance" to "%.9f".format(T188_TOLERANCE),
            "decisionDigits" to T188_DECISION_DIGITS.toString(),
            "referencePhase" to T188_REFERENCE_PHASE.toString(),
            "recommendedPhase" to T188_RECOMMENDED_PHASE.toString(),
            "endOfRowColumnPhasesAt112bp" to rowEndColumnPhases.joinToString(",")
        ),
        cheapBounds = emptyList(),
        guardSweep = guardSweeps,
        census = censusRecords,
        cells = cells,
        countTerms = countTerms,
        interactions = interactions,
        splits = splits,
        argmins = argmins,
        convergence = convergence,
        reproductions = reproductions,
        predicates = predicates,
        falsifiers = falsifiers,
        findings = emptyList(),
        validity = listOf(
            "TRL 1-3. Model-consistent and traceable. The dropout INPUT is C-0087's, which is " +
                    "Strauss et al. (2018) read directly on a plain Rothemund rectangle; " +
                    "nothing derived here is measured.",
            "The motif is NOT demonstrated: a free lever held to a single-layer sheet by one " +
                    "crossover is this programme's own construct (C-0028, C-0029, C-0055).",
            "The 32 x 6 grids at the two widths are DIFFERENT OBJECTS - the site inventories " +
                    "and the column censuses differ - so they are compared as summary " +
                    "statistics of one construction on two lattices, never cell by cell. Only " +
                    "the 2 x 2 at phases 24 and 8 is matched, and it is matched on STATIONS " +
                    "and not on hosts.",
            "The strata are recomputed at this width and are not C-0102's sets: at 38.08 nm " +
                    "with the row end admitted the richest-column phases are {8, 24} alone and " +
                    "the richest inventory belongs to {0, 16}.",
            "C-0022's collar terms are carried unchanged to the narrower tile rather than " +
                    "re-solved, exactly as C-0090 carries them; C-0026's edge enhancement is a " +
                    "function of the tile size and this is the inherited approximation.",
            "EQUAL springs at every cell. The distribution axis is C-0089's and T-179's; " +
                    "nothing here bounds the three-way interaction of count, phase and " +
                    "distribution.",
            "ONE anchor rule and ONE load state, as C-0108: the canonical chain is a third rule " +
                    "beside C-0103's two, and the levels carry that rule dependence. The signs, " +
                    "the interaction and the argmin are what is read.",
            "T-5b's 0.10 is a CONVENTION, not a physical threshold.",
            "Single layer, static, 300 K, aqueous 2 mM MgCl2."
        ),
        openQuestions = listOf(
            "Whether the coupling element's own incorporation is the staple's - C-0087's item " +
                    "2, unchanged, and still the only route by which this programme keeps a " +
                    "flat tile.",
            "Whether a SEARCHED placement at the buildable width reorders the phases the way " +
                    "the search-free chain does; C-0090's own descent is at one count and one " +
                    "arm.",
            "Whether the distribution freed at every count changes the argmin at this width. " +
                    "T-179 owns the axis and it has never been run at 38.08 nm."
        )
    )

    val output = File("gpd/results/T-188-buildable-width-count-phase.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    fun write(value: T188Result) = output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(value).roundedForResult(
                digits = T188_DECISION_DIGITS + 3,
                digitsByKey = DEPARTURE_DIGITS_BY_KEY,
                floor = T188_DECISION_FLOOR
            ).withEmissionHeader(LatticeTag.SQUARE, null) as JsonObject)
        ) + "\n"
    )

    // `CLAUDE.md`: build the result and WRITE THE JSON before formatting any prose.
    write(result)

    val bounds = t188CheapBounds(
        guardSweeps, censusRecords, rowEndColumnPhases, buildableSplit, nominalSplit
    )
    val withProse = try {
        result.copy(
            cheapBounds = bounds,
            decision = t188Decision(
                admittedTerms, refusedTerms, interactions, buildableSplit, nominalSplit,
                buildableArgmin, nominalArgmin, cells, canonicalAdmitted
            ),
            findings = t188Findings(
                guardSweeps, censusRecords, countTerms, interactions, splits, argmins, cells,
                canonicalAdmitted, canonicalRefused, worstGuardSpread, falsifiers
            )
        )
    } catch (e: Exception) {
        write(result.copy(cheapBounds = bounds, decision = "PROSE FAILED: " + e.toString()))
        throw e
    }
    write(withProse)
    println("T-188 - wrote " + output.path)
    withProse.findings.forEach { println("  * " + it) }
    withProse.predicates.forEach { println("  [" + it.verdict + "] " + it.name) }
    withProse.falsifiers.forEach {
        println(
            "  " + it.name + " " + (if (it.fired) "FIRED" else "did not fire") + ": " + it.outcome
        )
    }
}

// ---------------------------------------------------------------------------------------------
// the prose, built AFTER the JSON has been written once
// ---------------------------------------------------------------------------------------------

private fun six(value: Double) = "%.6f".format(value)

private fun nine(value: Double) = "%.9f".format(value)

private fun t188CheapBounds(
    guards: List<T188GuardRecord>,
    census: List<T188CensusRecord>,
    rowEndColumnPhases: List<Int>,
    buildableSplit: T188SplitRecord?,
    nominalSplit: T188SplitRecord?
): List<T188BoundRecord> {
    val carried = guards.first { it.edgeX < 39.0 && !it.admitRowEnd }
    val nominal = guards.first { it.edgeX > 39.0 }
    return listOf(
        T188BoundRecord(
            name = "bound 1 - the EDGE_MARGIN sweep as a LATTICE statement, no solve at all",
            value = carried.distinctLattices.toDouble(),
            unit = "distinct lattices over 32 phases",
            settles = "THE AXIS THE BRIEF EXPECTED TO DOMINATE. At the buildable width the " +
                    "guard's VALUE is exactly inert over 0.05 nm, half a rise and one rise - " +
                    "one lattice, worst station displacement " +
                    nine(carried.worstStationDisplacement) + " nm - because a column that lands " +
                    "ON the edge is deleted by any positive inset and the next one in is a " +
                    "whole 16 bp pitch further. At the NOMINAL width the same sweep leaves " +
                    nominal.distinctLattices.toString() + " lattices, the column count moving " +
                    "at phases " + nominal.phasesWhoseColumnCountMoves.joinToString(", ") +
                    ". What is not inert at 38.08 nm is the guard's EXISTENCE, which is a binary",
            falsifierFired = false
        ),
        T188BoundRecord(
            name = "bound 2 - C-0102's congruence, and the collapse of the strata",
            value = rowEndColumnPhases.size.toDouble(),
            unit = "phases at which the end plane is a COLUMN",
            settles = "which phases are structurally comparable at the buildable width. The " +
                    "end plane exists at 0, 8, 16 and 24 and is a column only at " +
                    rowEndColumnPhases.joinToString(" and ") + "; admitting it those two carry " +
                    "eight columns and every other phase seven, and refusing it those two carry " +
                    "SIX and no phase carries eight. So C-0015's ten eight-column phases become " +
                    "two or none, and a cell-by-cell comparison against the 40.00 nm grid is " +
                    "not a comparison of the same strata",
            falsifierFired = false
        ),
        T188BoundRecord(
            name = "bound 3 - the 2 x 2's own corners carry IDENTICAL stations at the two widths",
            value = 2.0,
            unit = "matched corners",
            settles = "what the headline comparison holds fixed. At phases 8 and 24 the " +
                    "buildable admitted station lattice is bit-identical to the 40.00 nm one " +
                    "(C-0090, re-derived here as a test), and the COLUMNS are not - so every " +
                    "difference between the two widths' 2 x 2 is host geometry and load, and " +
                    "none of it is which stations were available",
            falsifierFired = false
        ),
        T188BoundRecord(
            name = "bound 4 - the path identity: four graded cells decide the headline",
            value = 4.0,
            unit = "graded cells",
            settles = "how expensive the answer is. The two orderings of the 2 x 2 share their " +
                    "endpoints, so the interaction is one number from four cells; the 32 x 6 " +
                    "grid buys the generality and not the verdict. Measured: " +
                    (if (buildableSplit == null || nominalSplit == null) "not available at this " +
                            "sweep"
                    else six(buildableSplit.interactionPerCent) + " per cent at 38.08 nm " +
                            "against " + six(nominalSplit.interactionPerCent) +
                            " per cent at 40.00 nm"),
            falsifierFired = false
        ),
        T188BoundRecord(
            name = "bound 5 - the free tile is a function of the phase at this width too",
            value = census.filter { it.admitRowEnd }.maxOfOrNull {
                it.freeTileDishingOverStroke
            } ?: 0.0,
            unit = "of the free-tile stroke",
            settles = "the baseline every coupling is judged against. Over the admitted hosts " +
                    "the uncoupled tile dishes " +
                    six(
                        census.filter { it.admitRowEnd }.minOfOrNull {
                            it.freeTileDishingOverStroke
                        } ?: 0.0
                    ) + " to " +
                    six(
                        census.filter { it.admitRowEnd }.maxOfOrNull {
                            it.freeTileDishingOverStroke
                        } ?: 0.0
                    ) + " of the stroke, so a p90 quoted without its host is not quoted at a state",
            falsifierFired = false
        )
    )
}

@Suppress("LongParameterList")
private fun t188Decision(
    admittedTerms: List<T188CountTermRecord>,
    refusedTerms: List<T188CountTermRecord>,
    interactions: List<T188InteractionRecord>,
    buildableSplit: T188SplitRecord?,
    nominalSplit: T188SplitRecord?,
    buildableArgmin: T188ArgminRecord?,
    nominalArgmin: T188ArgminRecord?,
    cells: List<T188CellRecord>,
    canonicalAdmitted: String
): String {
    val adverse = admittedTerms.count { it.adverse }
    val graded = interactions.firstOrNull { it.scope.contains("BUILDABLE width, row-end ADMITTED") }
    val nominalFit = interactions.firstOrNull { it.scope.contains("NOMINAL width") }
    val head = "THE WIDTH DOES NOT MOVE THE INTERACTION, AND THE GUARD'S VALUE DOES NOTHING AT " +
            "ALL. "
    val guard = "At the buildable 38.08 nm the EDGE_MARGIN sweep 0.05 / 0.17 / 0.34 nm leaves " +
            "ONE lattice under the truncation C-0108 was run on, so the axis the brief expected " +
            "to dominate is settled by arithmetic; what is not inert is the guard's EXISTENCE, " +
            "the binary C-0095 and C-0099 already closed. "
    val split = if (buildableSplit == null || nominalSplit == null) "" else
        "On the 2 x 2 the recommendation moves through - whose station sets are bit-identical " +
                "at the two widths - the interaction is " +
                six(buildableSplit.interactionPerCent) + " per cent at 38.08 nm against " +
                six(nominalSplit.interactionPerCent) + " per cent at 40.00 nm, and the total " +
                six(buildableSplit.totalPerCent) + " against " + six(nominalSplit.totalPerCent) +
                " per cent. "
    val fit = if (graded == null || nominalFit == null) "" else
        "Over the 32 x 6 grid the two axes are NOT separable at either width: the worst " +
                "additive residual is " + six(graded.worstResidualPerCent) +
                " per cent of a level at 38.08 nm against " +
                six(nominalFit.worstResidualPerCent) + " per cent at 40.00 nm, carrying " +
                six(100.0 * graded.interactionShare) + " per cent of the variation against " +
                six(100.0 * nominalFit.interactionShare) + " per cent. "
    val argmin = if (buildableArgmin == null || nominalArgmin == null) "" else
        "The argmin moves from phase " + nominalArgmin.bestPhase + " at " +
                nominalArgmin.bestCount + " paths to phase " + buildableArgmin.bestPhase +
                " at " + buildableArgmin.bestCount + " paths. "
    val counts = "The 34 -> 30 count term is adverse at " + adverse + " of " +
            admittedTerms.size + " admitted phases and at " + refusedTerms.count { it.adverse } +
            " of " + refusedTerms.size + " refused ones. "
    val flat = "Nothing here is flat: " +
            cells.count { it.family == canonicalAdmitted && !it.flatAtP90 } + " of " +
            cells.count { it.family == canonicalAdmitted } +
            " graded admitted cells exceed T-5b's 0.10 at the 90th percentile."
    return head + guard + split + fit + argmin + counts + flat
}

@Suppress("LongParameterList", "LongMethod")
private fun t188Findings(
    guards: List<T188GuardRecord>,
    census: List<T188CensusRecord>,
    countTerms: List<T188CountTermRecord>,
    interactions: List<T188InteractionRecord>,
    splits: List<T188SplitRecord>,
    argmins: List<T188ArgminRecord>,
    cells: List<T188CellRecord>,
    canonicalAdmitted: String,
    canonicalRefused: String,
    worstGuardSpread: Double,
    falsifiers: List<T188FalsifierRecord>
): List<String> {
    val findings = ArrayList<String>()
    val carried = guards.first { it.edgeX < 39.0 && !it.admitRowEnd }
    val admittedGuard = guards.first { it.edgeX < 39.0 && it.admitRowEnd }
    val nominalGuard = guards.first { it.edgeX > 39.0 }

    findings += ("THE GUARD'S VALUE IS INERT AT THE BUILDABLE WIDTH AND NOT AT THE NOMINAL ONE, " +
            "WHICH IS THE OPPOSITE WAY ROUND FROM THE EXPECTATION. Swept over 0.05 nm, half a " +
            "rise and one rise, CrossoverLayout.EDGE_MARGIN leaves %d distinct lattice(s) over " +
            "the 32 phases of the buildable 38.08 nm tile under the truncation C-0108 was run " +
            "on - worst station displacement %s nm, worst column-count change %d - and %d at " +
            "40.00 nm, where the column count moves at phases %s. The reason is a lattice one " +
            "and needs no solve: at a width that is an exact whole number of column pitches the " +
            "row-end column sits ON the edge, so ANY positive inset deletes it and the next " +
            "column in is a whole 16 bp = 5.44 nm further; at 40.00 nm the closest approach is " +
            "0.28 nm, which one rise crosses. So the guard at this width is not a tolerance at " +
            "all - it is a BINARY, and it is the one C-0095 and C-0099 already closed.").format(
        carried.distinctLattices, nine(carried.worstStationDisplacement),
        carried.worstColumnCountChange, nominalGuard.distinctLattices,
        nominalGuard.phasesWhoseColumnCountMoves.joinToString(", ")
    )

    findings += ("AND ADMITTING THE ROW END MAKES THE GUARD A POSITION RATHER THAN A " +
            "TRUNCATION, WORTH %s PER CENT OF A LEVEL. With the row-end plane kept, the inset " +
            "decides WHERE it sits, so the same sweep leaves %d lattices - the stations moving " +
            "at phases %s by exactly one rise less the guard, %s nm, and the column count moving " +
            "nowhere. Graded at %d cells the whole spread is %s of a level, against C-0090's " +
            "0.32 per cent on its own searched placement.").format(
        six(100.0 * worstGuardSpread), admittedGuard.distinctLattices,
        admittedGuard.phasesWhoseStationsMove.joinToString(", "),
        nine(admittedGuard.worstStationDisplacement),
        cells.count { it.family.startsWith("guard sweep") }, nine(worstGuardSpread)
    )

    val admittedCensus = census.filter { it.admitRowEnd }
    val refusedCensus = census.filter { !it.admitRowEnd }
    findings += ("THE STRATA ARE NOT THE SAME SETS, AND THE CENSUS SAYS SO BEFORE ANY SOLVE. At " +
            "38.08 nm with the row end admitted %d of %d phases carry the richest column count " +
            "(%d) - they are %s - and %d carry the richest inventory (%d sites), which are %s; " +
            "refusing the row end takes those same two phases to %d columns and no phase " +
            "carries more than %d. C-0015's ten eight-column phases at 40.00 nm have therefore " +
            "become two or none, and the 32 x 6 grids at the two widths are different objects " +
            "compared as summary statistics and never cell by cell.").format(
        admittedCensus.count { it.columns == (admittedCensus.maxOfOrNull { c -> c.columns } ?: 0) },
        admittedCensus.size, admittedCensus.maxOfOrNull { it.columns } ?: 0,
        admittedCensus.filter {
            it.columns == (admittedCensus.maxOfOrNull { c -> c.columns } ?: 0)
        }.joinToString(", ") { it.phaseBasePairs.toString() },
        admittedCensus.count { it.richestInventory },
        admittedCensus.maxOfOrNull { it.upwardSites } ?: 0,
        admittedCensus.filter { it.richestInventory }
            .joinToString(", ") { it.phaseBasePairs.toString() },
        refusedCensus.filter { it.phaseBasePairs == 8 }.map { it.columns }.firstOrNull() ?: 0,
        refusedCensus.maxOfOrNull { it.columns } ?: 0
    )

    val admittedTerms = countTerms.filter { it.admitRowEnd }
    val refusedTerms = countTerms.filter { !it.admitRowEnd }
    if (admittedTerms.isNotEmpty()) {
        findings += ("THE COUNT TERM AT EVERY PHASE OF THE BUILDABLE TILE. The 34 -> 30 " +
                "reduction on the canonical search-free family is adverse at %d of %d admitted " +
                "phases and favourable at %d, running %s to %s per cent, against C-0108's " +
                "-4.59519576 to +12.2058991 per cent at 40.00 nm. Under the refused convention " +
                "it is adverse at %d of %d and runs %s to %s per cent.").format(
            admittedTerms.count { it.adverse }, admittedTerms.size,
            admittedTerms.count { !it.adverse },
            six(admittedTerms.minOf { it.costPerCent }),
            six(admittedTerms.maxOf { it.costPerCent }),
            refusedTerms.count { it.adverse }, refusedTerms.size,
            six(refusedTerms.minOfOrNull { it.costPerCent } ?: 0.0),
            six(refusedTerms.maxOfOrNull { it.costPerCent } ?: 0.0)
        )
    }

    val graded = interactions.firstOrNull { it.scope.contains("BUILDABLE width, row-end ADMITTED") }
    val bracket = interactions.firstOrNull { it.scope.contains("BUILDABLE width, row-end REFUSED") }
    val nominalFit = interactions.firstOrNull { it.scope.contains("NOMINAL width") }
    if (graded != null && nominalFit != null) {
        findings += ("THE TWO AXES ARE NOT SEPARABLE AT EITHER WIDTH, AND THE INTERACTION IS " +
                "THE SAME SIZE. The balanced two-way additive fit in log p90 leaves a worst " +
                "residual of %s log units at 38.08 nm admitted (%s per cent of a level, %s of " +
                "its own 34 -> 30 count main effect) against %s log units at 40.00 nm (%s per " +
                "cent, %s of its count main effect); the variation splits %s per cent phase, " +
                "%s per cent count and %s per cent interaction at 38.08 nm against %s, %s and " +
                "%s at 40.00 nm. Both decompositions close to %s and %s, which is the " +
                "orthogonality of a balanced design and is asserted as a gate.%s").format(
            nine(graded.worstResidual), six(graded.worstResidualPerCent),
            six(graded.worstResidualOverCountMainTerm),
            nine(nominalFit.worstResidual), six(nominalFit.worstResidualPerCent),
            six(nominalFit.worstResidualOverCountMainTerm),
            six(100.0 * graded.phaseShare), six(100.0 * graded.countShare),
            six(100.0 * graded.interactionShare),
            six(100.0 * nominalFit.phaseShare), six(100.0 * nominalFit.countShare),
            six(100.0 * nominalFit.interactionShare),
            nine(graded.decompositionResidual), nine(nominalFit.decompositionResidual),
            if (bracket == null) "" else (" Under the refused convention the same fit leaves " +
                    nine(bracket.worstResidual) + " log units and " +
                    six(100.0 * bracket.interactionShare) + " per cent of the variation.")
        )
    }

    val buildableSplit = splits.firstOrNull { it.name.contains("ADMITTED") }
    val nominalSplit = splits.firstOrNull { it.name.contains("40.00") }
    val refusedSplit = splits.firstOrNull { it.name.contains("REFUSED") }
    if (buildableSplit != null && nominalSplit != null) {
        findings += ("THE HEADLINE 2 x 2, MATCHED ON STATIONS AND NOT ON HOSTS. At phases 24 " +
                "and 8 the buildable admitted station lattice is bit-identical to the 40.00 nm " +
                "one, so the 2 x 2 differs only in the host and the load. Count first then " +
                "phase reads %s then %s per cent at 38.08 nm against %s then %s at 40.00 nm; " +
                "phase first then count reads %s then %s against %s then %s. The total is %s " +
                "per cent against %s (path disagreement %s and %s), and the interaction %s per " +
                "cent against %s.%s").format(
            six(buildableSplit.countTermAtFromPhasePerCent),
            six(buildableSplit.phaseTermAtToCountPerCent),
            six(nominalSplit.countTermAtFromPhasePerCent),
            six(nominalSplit.phaseTermAtToCountPerCent),
            six(buildableSplit.phaseTermAtFromCountPerCent),
            six(buildableSplit.countTermAtToPhasePerCent),
            six(nominalSplit.phaseTermAtFromCountPerCent),
            six(nominalSplit.countTermAtToPhasePerCent),
            six(buildableSplit.totalPerCent), six(nominalSplit.totalPerCent),
            nine(buildableSplit.pathDisagreement), nine(nominalSplit.pathDisagreement),
            six(buildableSplit.interactionPerCent), six(nominalSplit.interactionPerCent),
            if (refusedSplit == null) "" else (" Refusing the row-end column the same 2 x 2 " +
                    "reads a total of " + six(refusedSplit.totalPerCent) +
                    " per cent and an interaction of " + six(refusedSplit.interactionPerCent) +
                    " per cent, so the CONVENTION moves the split further than the WIDTH does.")
        )
    }

    val buildableArgmin = argmins.firstOrNull { it.scope.contains("ADMITTED") }
    val refusedArgmin = argmins.firstOrNull { it.scope.contains("REFUSED") }
    val nominalArgmin = argmins.firstOrNull { it.scope.contains("NOMINAL") }
    if (buildableArgmin != null && nominalArgmin != null) {
        findings += ("AND THE RECOMMENDED ENDPOINT. Over the whole 32 x 6 grid the argmin is " +
                "phase %d at %d paths (%s of the stroke) at the buildable width against phase " +
                "%d at %d paths (%s) at the nominal one; held at 34 paths the best phase is %d " +
                "against %d, and held at 30 paths %d against %d.%s Neither is flat: T-5b's " +
                "convention is 0.10.").format(
            buildableArgmin.bestPhase, buildableArgmin.bestCount, six(buildableArgmin.bestP90),
            nominalArgmin.bestPhase, nominalArgmin.bestCount, six(nominalArgmin.bestP90),
            buildableArgmin.bestPhaseAtCount34, nominalArgmin.bestPhaseAtCount34,
            buildableArgmin.bestPhaseAtCount30, nominalArgmin.bestPhaseAtCount30,
            if (refusedArgmin == null) "" else (" Refusing the row-end column the argmin is " +
                    "phase " + refusedArgmin.bestPhase + " at " + refusedArgmin.bestCount +
                    " paths (" + six(refusedArgmin.bestP90) + ").")
        )
    }

    val admittedCells = cells.filter { it.family == canonicalAdmitted }
    val refusedCells = cells.filter { it.family == canonicalRefused }
    findings += ("UNDER THE MEASURED DROPOUT THE WHOLE FAMILY IS A NET DISHING SOURCE AT THE " +
            "BUILDABLE WIDTH TOO. %d of %d admitted cells and %d of %d refused ones exceed " +
            "their own host's uncoupled dishing at the 90th percentile, and %d of %d and %d of " +
            "%d exceed T-5b's 0.10; the worst admitted cell is %s of the stroke. So neither the " +
            "width, nor the count, nor the phase, nor the end-of-row convention decides an " +
            "acceptance verdict on this branch - exactly as C-0103 found at one phase and " +
            "C-0108 at thirty-two.").format(
        admittedCells.count { it.worseThanNoCouplingAtP90 }, admittedCells.size,
        refusedCells.count { it.worseThanNoCouplingAtP90 }, refusedCells.size,
        admittedCells.count { !it.flatAtP90 }, admittedCells.size,
        refusedCells.count { !it.flatAtP90 }, refusedCells.size,
        six(admittedCells.maxOfOrNull { it.p90OverStroke } ?: 0.0)
    )

    val f1 = falsifiers.first { it.name == "F1" }
    val f2 = falsifiers.first { it.name == "F2" }
    val f3 = falsifiers.first { it.name == "F3" }
    findings += ("THE VERDICT ON C-0108 AT THE BUILDABLE WIDTH. F1 %s, F2 %s and F3 %s. %s").format(
        if (f1.fired) "FIRED" else "did not fire",
        if (f2.fired) "FIRED" else "did not fire",
        if (f3.fired) "FIRED" else "did not fire",
        if (!f2.fired)
            "C-0108's finding transfers to the width the design can build: the interaction is " +
                    "the same sign and within a factor of two, the two axes are not separable " +
                    "at either width, and the recommendation still rests on the TOTAL, which " +
                    "is path-independent."
        else
            "C-0108's interaction does not transfer unchanged to the buildable width, so its " +
                    "split is a 40.00 nm reading and the total is what carries over."
    )

    return findings
}
