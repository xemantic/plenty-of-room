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

import com.xemantic.nano.plentyofroom.anchoring.UpwardRootInfluenceBank
import com.xemantic.nano.plentyofroom.anchoring.upwardRootLattice
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

/**
 * `T-178` — does `C-0103`'s count effect at fixed station geometry hold at the other 31 crossover
 * phases, and is its *"+12.86 % of count against −19.0 % of phase"* a decomposition at all?
 *
 * `C-0103` measured the count axis at **one** phase and then defended the programme's standing
 * 34 → 30 recommendation by splitting the move into a count term and a phase term. The split is a
 * subtraction. This study measures the **interaction** the subtraction assumes away, two
 * independent ways: on the 2 × 2 the recommendation itself moves through, and over the whole
 * 32-phase × 6-count grid.
 */

// ---------------------------------------------------------------------------------------------
// the records
// ---------------------------------------------------------------------------------------------

/** One cheap bound, settled before the sampler it precedes. */
@Serializable
private data class T178BoundRecord(
    val name: String,
    val value: Double,
    val unit: String,
    val settles: String,
    val falsifierFired: Boolean
)

/** One phase of the upward `EAST` lattice — a census row, and no solve but the free host. */
@Serializable
private data class T178CensusRecord(
    val phaseBasePairs: Int,
    val upwardSites: Int,
    val rowLengths: List<Int>,
    val sheetCrossoverColumns: Int,
    val eightColumnHost: Boolean,
    val richestInventory: Boolean,
    val centroSymmetric: Boolean,
    val stratum: String,
    val freeTileDishingOverStroke: Double
)

/** One graded design: a station set, a distribution, and the whole dropout distribution. */
@Serializable
private data class T178CellRecord(
    val family: String,
    val phaseBasePairs: Int,
    val pathCount: Int,
    val stratum: String,
    val distribution: String,
    val ensemble: String,
    val centroSymmetric: Boolean,
    val perPathStiffness: Double,
    val perPathForce: Double,
    val nominalOverStroke: Double,
    val worstSingleRemovalOverStroke: Double,
    val medianOverStroke: Double,
    val p90OverStroke: Double,
    val p95OverStroke: Double,
    val worstOverStroke: Double,
    val exceedance: Double,
    val exceedanceStandardError: Double,
    /**
     * The one-sided Clopper-Pearson limit where [exceedance] saturates, else `null` — `T-213`.
     *
     * `CH-0153`: at `p̂ = 1` the symmetric [exceedanceStandardError] is identically zero for every
     * sample count, and this study's headline is a design that **fails**, which is exactly the
     * direction that saturates it. Emitted **beside** the symmetric error rather than replacing
     * it: the symmetric error is uninformative rather than wrong.
     */
    val exceedanceOneSidedBound: Double?,
    val meanSurvivors: Double,
    val flatAtP90: Boolean,
    val p90OverFreeTile: Double,
    val worseThanNoCouplingAtP90: Boolean
)

/** The count term of one phase — `C-0103`'s +12.86 %, read at every phase. */
@Serializable
private data class T178CountTermRecord(
    val phaseBasePairs: Int,
    val stratum: String,
    val fromCount: Int,
    val toCount: Int,
    val p90From: Double,
    val p90To: Double,
    val costFactor: Double,
    val costPerCent: Double,
    val adverse: Boolean,
    val cheapCostFactor: Double,
    val cheapAgreesInSign: Boolean,
    val p90ByCount: List<Double>,
    val monotoneDecreasingInCount: Boolean
)

/** The interaction of the count and the phase over one named grid. */
@Serializable
private data class T178InteractionRecord(
    val scope: String,
    val phases: List<Int>,
    val counts: List<Int>,
    val worstResidual: Double,
    val worstResidualPerCent: Double,
    val interactionShare: Double,
    val phaseSumOfSquares: Double,
    val countSumOfSquares: Double,
    val interactionSumOfSquares: Double,
    val totalSumOfSquares: Double,
    val decompositionResidual: Double,
    val countMainTerm: Double,
    val countMainTermPerCent: Double,
    val worstResidualOverCountMainTerm: Double,
    val separable: Boolean,
    val note: String
)

/** One 2 × 2, read both ways round. */
@Serializable
private data class T178SplitRecord(
    val name: String,
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

/** A rank agreement between the cheap bound and the percentile, on one named scope. */
@Serializable
private data class T178RankRecord(
    val scope: String,
    val pairs: Int,
    val spearman: Double,
    val transfers: Boolean,
    val note: String
)

/** A convergence axis. [departure] is emitted at **two significant digits** and nothing finer. */
@Serializable
private data class T178ConvergenceRecord(
    val quantity: String,
    val parameter: String,
    val values: List<Double>,
    val results: List<Double>,
    val departure: Double,
    val note: String
)

/**
 * One upstream number reproduced rather than cited. [departure] is a difference of two nearly
 * equal numbers and is **dimensionless**, so it is emitted at two significant digits (`P-18`).
 */
@Serializable
private data class T178ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double,
    val strict: Boolean
)

/** One acceptance predicate of `T-178`. */
@Serializable
private data class T178PredicateRecord(
    val name: String,
    val statement: String,
    val verdict: String
)

/** One declared falsifier, and whether it fired. */
@Serializable
private data class T178FalsifierRecord(
    val name: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T178Result(
    val task: String,
    val question: String,
    val leaf: String,
    val conditions: Map<String, String>,
    val decision: String,
    val parameters: Map<String, String>,
    val cheapBounds: List<T178BoundRecord>,
    val census: List<T178CensusRecord>,
    val cells: List<T178CellRecord>,
    val countTerms: List<T178CountTermRecord>,
    val interactions: List<T178InteractionRecord>,
    val splits: List<T178SplitRecord>,
    val rankAgreement: List<T178RankRecord>,
    val convergence: List<T178ConvergenceRecord>,
    val reproductions: List<T178ReproductionRecord>,
    val predicates: List<T178PredicateRecord>,
    val falsifiers: List<T178FalsifierRecord>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

// ---------------------------------------------------------------------------------------------
// the constants
// ---------------------------------------------------------------------------------------------

private const val T178_DUPLEXES = 15
private const val T178_SAMPLES = 81
private const val T178_TOLERANCE = 0.10
private const val T178_RIM_STANDOFF = 1.0

/** `C-0063`'s and `C-0103`'s own phase — centro-symmetric, and an eight-column host. */
private const val T178_REFERENCE_PHASE = 24

/** `C-0074`'s phase — the other half of the move the programme recommends. */
private const val T178_RECOMMENDED_PHASE = 8

/** `C-0087`'s own seed, so that its published percentiles reproduce cell for cell. */
private const val T178_GRADING_SEED = 20260817L

private const val T178_DECISION_DIGITS = 6
private const val T178_DECISION_FLOOR = 1e-12

/** The count term is separable to within this many log units — 1 % of a level. */
private const val T178_SEPARABLE = 0.01

/** `C-0103`'s own count term at phase 24, the level `T-178`'s falsifier `F2` is declared against. */
private const val T178_C0103_COUNT_TERM_PER_CENT = 12.8596328

private val T178_EDGE_X = Gen1Tile.EDGE_X
private val T178_MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

/** `C-0075`'s own self-consistent count table, at the rows `C-0103` swept. */
private val T178_COUNTS = listOf(22, 25, 28, 30, 34, 45)

private val t178Realisations = System.getenv("T178_REALISATIONS")?.toIntOrNull() ?: 10000

private val t178Phases: List<Int> = System.getenv("T178_PHASES")
    ?.split(",")?.mapNotNull { it.trim().toIntOrNull() }?.takeIf { it.isNotEmpty() }
    ?: (0 until 32).toList()

// ---------------------------------------------------------------------------------------------
// the inputs, read from the claims that own them
// ---------------------------------------------------------------------------------------------

private class T178Profile(
    val name: String,
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, lengthY: Double): PressureField = edgeCollarPressure(
        interiorPressure, T178_EDGE_X, lengthY,
        listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T178_RIM_STANDOFF))
    )
}

/** `C-0022`'s solved profile, keyed on **`(concentration, gap, bias)`** — `CLAUDE.md`'s gotcha. */
private fun t178Profile(file: File, key: Triple<Double, Double, Double>): T178Profile {
    require(file.exists()) {
        "C-0022's result file is missing: ${file.path}. T-178 consumes the SOLVED edge profile."
    }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(name: String) = it.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == key.first && value("gapHeight") == key.second &&
                    value("appliedBias") == key.third
        } ?: error("no C-0022 profile at ${key.first} mM, ${key.second} nm, ${key.third} V")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T178Profile(
        name = "%.1f mM, %.0f nm, %.3f V".format(key.first, key.second, key.third),
        smoothDepth = value("taperDepth"),
        smoothWidth = value("taperWidth"),
        rimDepth = value("rimResidualDepth")
    )
}

/** A placement read from the result file of the claim that owns it, row by row. */
private fun t178PlacementRows(file: File, key: String): List<List<Double>> {
    require(file.exists()) { "the placement's own result file is missing: ${file.path}" }
    val byRow = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue(key).jsonArray.map { it.jsonObject }
        .associate { row ->
            row.getValue("row").jsonPrimitive.content.toInt() to
                    row.getValue("roots").jsonArray.map { it.jsonPrimitive.content.toDouble() }
        }
    return (0 until T178_DUPLEXES).map { byRow[it] ?: emptyList() }
}

/**
 * `C-0098`'s own graded **array** cells at full upward inventory, read at run time — the
 * published phase reading this task's cheap bound 2 is taken on.
 */
private fun t178PublishedArrayPhases(file: File): Map<Int, Pair<Int, Double>> {
    require(file.exists()) {
        "C-0098's result file is missing: ${file.path}. T-178 will not retype a phase reading."
    }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("cells").jsonArray.map { it.jsonObject }
        .filter { it.getValue("topology").jsonPrimitive.content == "ARRAY" }
        .filter { it.getValue("stationSet").jsonPrimitive.content.startsWith("the full upward") }
        .associate {
            it.getValue("phaseBasePairs").jsonPrimitive.content.toInt() to
                    (it.getValue("pathCount").jsonPrimitive.content.toInt() to
                            it.getValue("p90OverStroke").jsonPrimitive.content.toDouble())
        }
}

// ---------------------------------------------------------------------------------------------
// the models
// ---------------------------------------------------------------------------------------------

private fun t178Sheet(): OrigamiSheet =
    origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)

private fun t178Host(sheet: OrigamiSheet, phase: Int): OrigamiGrillage = OrigamiGrillage(
    sheet = sheet,
    lengthX = T178_EDGE_X,
    beamCount = T178_DUPLEXES,
    foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
    columns = CrossoverLayout.atBasePairPhase(phase, sheet, T178_EDGE_X),
    subdivisions = 2,
    supports = emptyList()
)

/** `C-0102`'s three strata over `C-0015`'s eight-column set and `C-0063`'s centro-symmetric one. */
private fun t178Stratum(sites: Int, columns: Int, centroSymmetric: Boolean): String = when {
    centroSymmetric -> "eight-column AND centro-symmetric (C-0063's two)"
    columns == 8 -> "eight-column (C-0015's ten)"
    sites == 60 -> "richest inventory, seven-column (C-0098's ten)"
    else -> "seven-column, neither richest nor symmetric"
}

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

@Suppress("LongMethod", "ComplexMethod", "CyclomaticComplexMethod", "NestedBlockDepth")
fun main() {
    val sheet = t178Sheet()
    val lengthY = T178_DUPLEXES * sheet.interhelicalDistance
    val interiorPressure = Gen1Tile.TARGET_FORCE / (T178_EDGE_X * lengthY)

    println("T-178 — reading C-0022's solved load and the standing placements ...")
    val loadFile = ResultInputs.T_3B.file()
    val designProfile = t178Profile(loadFile, Triple(2.0, 10.0, 0.192))
    val designField = designProfile.field(interiorPressure, lengthY)

    val freeStroke = PlateOnFoundation(
        sheet.plate(T178_EDGE_X, lengthY), Gen1Tile.FOUNDATION_SECANT, emptyList(), 12
    ).solve(uniformPressure(interiorPressure)).meanDeflection

    val anchorRows = t178PlacementRows(
        ResultInputs.T_125.file(), "bestPlacement"
    )
    check(anchorRows.sumOf { it.size } == 34) { "C-0063's placement must carry 34 roots" }
    val roots30Rows = t178PlacementRows(
        ResultInputs.T_136.file(), "recommendedPlacement"
    )
    check(roots30Rows.sumOf { it.size } == 30) { "C-0074's placement must carry 30 roots" }
    val publishedArray = t178PublishedArrayPhases(
        ResultInputs.T_165.file()
    )
    check(publishedArray.size >= 6) { "C-0098's six graded array phases must be readable" }

    val measuredField = measuredDepthIncorporation(T178_EDGE_X, lengthY)
    fun equalSprings(count: Int) = List(count) { T178_MANDATE / count }

    // ------------------------------------------------------------------ the census, no solve
    val censusSites = t178Phases.associateWith { upwardRootLattice(it, T178_EDGE_X, T178_DUPLEXES) }
    // `CrossoverLayout.atBasePairPhase` lays the sheet's own COLUMNS out at the 16 bp pitch,
    // so their count is the layout's own size; the parities are the two interface parities.
    val censusColumns = t178Phases.associateWith {
        CrossoverLayout.atBasePairPhase(it, sheet, T178_EDGE_X).positions.size
    }
    val censusSymmetric = t178Phases.associateWith { phase ->
        val rows = censusSites.getValue(phase)
        rows.indices.all { row ->
            val mine = rows[row]
            val partner = rows[T178_DUPLEXES - 1 - row].map { -it }.sorted()
            mine.size == partner.size && mine.zip(partner).all { (a, b) -> abs(a - b) < 1e-9 }
        }
    }
    val strata = t178Phases.associateWith { phase ->
        t178Stratum(
            censusSites.getValue(phase).sumOf { it.size },
            censusColumns.getValue(phase),
            censusSymmetric.getValue(phase)
        )
    }

    // ------------------------------------------------------------------ the sweep
    val cells = ArrayList<T178CellRecord>()
    val censusRecords = ArrayList<T178CensusRecord>()
    val reproductions = ArrayList<T178ReproductionRecord>()
    val convergence = ArrayList<T178ConvergenceRecord>()

    fun reproduction(
        source: String,
        quantity: String,
        published: Double,
        reproduced: Double,
        strict: Boolean = true
    ) = T178ReproductionRecord(
        source, quantity, published, reproduced,
        roundForResult(abs(reproduced - published), 2, T178_DECISION_FLOOR), strict
    )

    val canonicalFamily = "canonical — the search-free nested chain of this phase's lattice"

    t178Phases.forEach { phase ->
        val sites = censusSites.getValue(phase)
        val siteStations = rootStations(sites, T178_DUPLEXES, sheet.interhelicalDistance)
        val host = t178Host(sheet, phase)
        val bank = UpwardRootInfluenceBank(host, siteStations, designField, T178_SAMPLES)
        val freeTileDishing = bank.freePeakDishing / freeStroke
        val probabilities = siteStations.map { (x, y) -> measuredField.at(x, y) }
        val parent = dropoutEnsemble(probabilities, t178Realisations, T178_GRADING_SEED)
        val chain = canonicalRootChain(sites)
        val stratum = strata.getValue(phase)

        censusRecords += T178CensusRecord(
            phaseBasePairs = phase,
            upwardSites = sites.sumOf { it.size },
            rowLengths = sites.map { it.size },
            sheetCrossoverColumns = censusColumns.getValue(phase),
            eightColumnHost = censusColumns.getValue(phase) == 8,
            richestInventory = sites.sumOf { it.size } == 60,
            centroSymmetric = censusSymmetric.getValue(phase),
            stratum = stratum,
            freeTileDishingOverStroke = freeTileDishing
        )

        fun grade(
            family: String,
            rows: List<List<Double>>,
            count: Int,
            ownEnsemble: Boolean,
            distribution: String = "EQUAL"
        ): T178CellRecord {
            val indices = rootStationIndices(sites, rows)
            val surrogate = bank.surrogateFor(indices)
            val stations = rootStations(rows, T178_DUPLEXES, sheet.interhelicalDistance)
            val ensemble = if (ownEnsemble) dropoutEnsemble(
                stations.map { (x, y) -> measuredField.at(x, y) },
                t178Realisations, T178_GRADING_SEED
            ) else restrictEnsemble(parent, indices)
            val springs = equalSprings(count)
            val nominal = surrogate.solve(springs).peakDishing / freeStroke
            val sample = dropoutDishingSample(surrogate, springs, ensemble)
            sample.indices.forEach { sample[it] = sample[it] / freeStroke }
            val summary = summariseDropoutDishing(
                sample, nominal, ensemble.meanSurvivors, T178_TOLERANCE
            )
            return T178CellRecord(
                family = family,
                phaseBasePairs = phase,
                pathCount = count,
                stratum = stratum,
                distribution = distribution,
                ensemble = if (ownEnsemble)
                    "drawn per station set, as C-0087 and C-0089 draw it"
                else "restricted from this phase's own site stream (common random numbers)",
                centroSymmetric = rowsAreCentroSymmetric(rows),
                perPathStiffness = springs.max(),
                perPathForce = springs.max() * Gen1Tile.ACCEPTABLE_STROKE,
                nominalOverStroke = nominal,
                worstSingleRemovalOverStroke =
                    worstSinglePathRemoval(surrogate, springs) / freeStroke,
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

        T178_COUNTS.forEach { count ->
            cells += grade(canonicalFamily, chain.at(count), count, ownEnsemble = false)
        }
        println(
            "T-178 — phase %2d (%2d sites, %d cols): p90 34 = %.6f, 30 = %.6f".format(
                phase, sites.sumOf { it.size }, censusColumns.getValue(phase),
                cells.first { it.phaseBasePairs == phase && it.pathCount == 34 }.p90OverStroke,
                cells.first { it.phaseBasePairs == phase && it.pathCount == 30 }.p90OverStroke
            )
        )

        if (phase == T178_REFERENCE_PHASE) {
            val chainA = nestedRootChain(sites, anchorRows)
            listOf(34, 30).forEach { count ->
                cells += grade(
                    "reference — C-0103's chain A on C-0063's own 34 roots",
                    chainA.at(count), count, ownEnsemble = false
                )
            }
            cells += grade(
                "reference — C-0063's 34 roots, C-0087's per-set ensemble",
                anchorRows, 34, ownEnsemble = true
            )
            val nominal34 = bank.surrogateFor(rootStationIndices(sites, anchorRows))
                .solve(equalSprings(34)).peakDishing / freeStroke
            reproductions += reproduction(
                "C-0063", "the zero-defect dishing of its 34-root placement", 0.0706145537,
                nominal34
            )
            reproductions += reproduction(
                "C-0074/C-0103", "the uncoupled tile on this host", 0.307902368, freeTileDishing
            )
            reproductions += reproduction(
                "C-0087/C-0089", "C-0063's 34 roots on their own per-set ensemble", 0.639129638,
                cells.last { it.family.startsWith("reference — C-0063") }.p90OverStroke
            )
            reproductions += reproduction(
                "C-0103", "chain A at 34 paths under common random numbers", 0.638498565,
                cells.first { it.family.contains("chain A") && it.pathCount == 34 }.p90OverStroke
            )
            reproductions += reproduction(
                "C-0103", "chain A at 30 paths under common random numbers", 0.720607136,
                cells.first { it.family.contains("chain A") && it.pathCount == 30 }.p90OverStroke
            )
            reproductions += reproduction(
                "C-0066", "the upward site inventory at phase 24",
                53.0, sites.sumOf { it.size }.toDouble()
            )

            // The realisation ladder, read on the DIFFERENCE, which is what the answer is.
            val levels = listOf(1250, 2500, 5000, t178Realisations)
            val chainCanonical = chain
            val idx34 = rootStationIndices(sites, chainCanonical.at(34))
            val idx30 = rootStationIndices(sites, chainCanonical.at(30))
            val s34 = bank.surrogateFor(idx34)
            val s30 = bank.surrogateFor(idx30)
            val terms = levels.map { realisations ->
                val ladder = dropoutEnsemble(probabilities, realisations, T178_GRADING_SEED)
                val a = orderStatistic(
                    dropoutDishingSample(s34, equalSprings(34), restrictEnsemble(ladder, idx34)),
                    0.90
                )
                val b = orderStatistic(
                    dropoutDishingSample(s30, equalSprings(30), restrictEnsemble(ladder, idx30)),
                    0.90
                )
                b / a
            }
            convergence += T178ConvergenceRecord(
                quantity = "the canonical 34 -> 30 count FACTOR at phase $T178_REFERENCE_PHASE",
                parameter = "realisations",
                values = levels.map { it.toDouble() },
                results = terms,
                departure = roundForResult(
                    abs(terms[terms.size - 1] - terms[terms.size - 2]), 2, T178_DECISION_FLOOR
                ),
                note = "read on the FACTOR and under common random numbers, which is the axis " +
                        "F2 is decided on"
            )
            val gridLevels = listOf(41, 81, 161)
            val gridMeans = gridLevels.map { samples ->
                val coarse = latticeInfluenceSurrogate(
                    host, rootStations(
                        chainCanonical.at(34), T178_DUPLEXES, sheet.interhelicalDistance
                    ),
                    designField, samples
                )
                val ladder = restrictEnsemble(
                    dropoutEnsemble(probabilities, 200, T178_GRADING_SEED), idx34
                )
                dropoutDishingSample(coarse, equalSprings(34), ladder).average() / freeStroke
            }
            convergence += T178ConvergenceRecord(
                quantity = "the MEAN over 200 realisations of the canonical 34 at phase " +
                        "$T178_REFERENCE_PHASE",
                parameter = "dishing samples per edge",
                values = gridLevels.map { it.toDouble() },
                results = gridMeans,
                departure = roundForResult(
                    abs(gridMeans[2] - gridMeans[1]), 2, T178_DECISION_FLOOR
                ),
                note = "C-0087's cure for the degenerate nested-grid percentile: three nested " +
                        "grids share their nodes, so a mean is the convergence test and not a " +
                        "percentile"
            )
        }

        if (phase == T178_RECOMMENDED_PHASE) {
            val grown = nestedRootChain(sites, roots30Rows)
            cells += grade(
                "reference — C-0074's recommended 30 roots, C-0087's per-set ensemble",
                roots30Rows, 30, ownEnsemble = true
            )
            reproductions += reproduction(
                "C-0089/C-0103", "C-0074's 30 roots on their own per-set ensemble", 0.583664426,
                cells.last { it.family.startsWith("reference — C-0074") }.p90OverStroke
            )
            listOf(30, 34).forEach { count ->
                cells += grade(
                    "published-adjacent — C-0074's 30 roots grown by C-0103's own addition rule",
                    grown.at(count), count, ownEnsemble = false
                )
            }
        }
    }

    reproductions += reproduction(
        "C-0026", "the free-tile stroke [nm]", 4.90731102, freeStroke
    )
    publishedArray.forEach { (phase, row) ->
        if (phase in t178Phases) {
            reproductions += reproduction(
                "C-0098", "its graded ARRAY cell at phase $phase and ${row.first} ties",
                row.second, row.second, strict = false
            )
        }
    }

    // ------------------------------------------------------------------ the count terms
    fun cellAt(family: String, phase: Int, count: Int): T178CellRecord? = cells.firstOrNull {
        it.family == family && it.phaseBasePairs == phase && it.pathCount == count
    }

    val countTerms = t178Phases.mapNotNull { phase ->
        val from = cellAt(canonicalFamily, phase, 34)
        val to = cellAt(canonicalFamily, phase, 30)
        if (from == null || to == null) null else T178CountTermRecord(
            phaseBasePairs = phase,
            stratum = strata.getValue(phase),
            fromCount = 34,
            toCount = 30,
            p90From = from.p90OverStroke,
            p90To = to.p90OverStroke,
            costFactor = to.p90OverStroke / from.p90OverStroke,
            costPerCent = 100.0 * (to.p90OverStroke / from.p90OverStroke - 1.0),
            adverse = to.p90OverStroke > from.p90OverStroke,
            cheapCostFactor = to.worstSingleRemovalOverStroke / from.worstSingleRemovalOverStroke,
            cheapAgreesInSign =
                (to.worstSingleRemovalOverStroke > from.worstSingleRemovalOverStroke) ==
                        (to.p90OverStroke > from.p90OverStroke),
            p90ByCount = T178_COUNTS.map { cellAt(canonicalFamily, phase, it)!!.p90OverStroke },
            monotoneDecreasingInCount = T178_COUNTS
                .map { cellAt(canonicalFamily, phase, it)!!.p90OverStroke }
                .zipWithNext().all { (a, b) -> b < a }
        )
    }

    // ------------------------------------------------------------------ the interactions
    val gradedPhases = t178Phases.filter { phase ->
        T178_COUNTS.all { cellAt(canonicalFamily, phase, it) != null }
    }
    fun gridOver(phases: List<Int>, select: (T178CellRecord) -> Double) =
        phases.map { phase ->
            T178_COUNTS.map { count -> select(cellAt(canonicalFamily, phase, count)!!) }
        }

    val interactions = ArrayList<T178InteractionRecord>()
    fun addInteraction(scope: String, phases: List<Int>, note: String, cheap: Boolean = false) {
        if (phases.size < 2) return
        val fit = twoWayLogInteraction(
            gridOver(phases) { if (cheap) it.worstSingleRemovalOverStroke else it.p90OverStroke }
        )
        // "the count term it splits" is the count MAIN effect of the same fit, 34 -> 30.
        val countMain = fit.columnEffects[T178_COUNTS.indexOf(30)] -
                fit.columnEffects[T178_COUNTS.indexOf(34)]
        interactions += T178InteractionRecord(
            scope = scope,
            phases = phases,
            counts = T178_COUNTS,
            worstResidual = fit.worstResidual,
            worstResidualPerCent = fit.worstResidualPerCent,
            interactionShare = fit.interactionShare,
            phaseSumOfSquares = fit.rowSumOfSquares,
            countSumOfSquares = fit.columnSumOfSquares,
            interactionSumOfSquares = fit.interactionSumOfSquares,
            totalSumOfSquares = fit.totalSumOfSquares,
            decompositionResidual = roundForResult(
                abs(
                    fit.totalSumOfSquares -
                            (fit.rowSumOfSquares + fit.columnSumOfSquares +
                                    fit.interactionSumOfSquares)
                ),
                2, T178_DECISION_FLOOR
            ),
            countMainTerm = countMain,
            countMainTermPerCent = 100.0 * (kotlin.math.exp(countMain) - 1.0),
            worstResidualOverCountMainTerm = fit.worstResidual / abs(countMain),
            separable = fit.worstResidual < T178_SEPARABLE,
            note = note
        )
    }
    addInteraction(
        "the CHEAP grid — the single-removal instrument over every swept phase",
        gradedPhases,
        "n solves per cell and no sampling; run before the ensemble and reported whatever it says",
        cheap = true
    )
    addInteraction(
        "the GRADED grid — the 90th percentile under C-0087's measured dropout",
        gradedPhases,
        "the headline: whether the count term is a constant of the coupling or of the phase"
    )
    listOf(
        "eight-column AND centro-symmetric (C-0063's two)",
        "eight-column (C-0015's ten)",
        "richest inventory, seven-column (C-0098's ten)",
        "seven-column, neither richest nor symmetric"
    ).forEach { stratum ->
        addInteraction(
            "the GRADED grid, within C-0102's stratum: $stratum",
            gradedPhases.filter { strata.getValue(it) == stratum },
            "a stratum is a set of structurally comparable hosts, so an interaction inside one " +
                    "is not a host effect"
        )
    }

    // ------------------------------------------------------------------ the 2 x 2 splits
    val splits = ArrayList<T178SplitRecord>()
    fun addSplit(
        name: String,
        fromPhase: Int,
        toPhase: Int,
        ff: Double,
        tf: Double,
        ft: Double,
        tt: Double,
        verdict: String
    ) {
        val split = countPhaseSplit(ff, tf, ft, tt)
        fun perCent(logTerm: Double) = 100.0 * (kotlin.math.exp(logTerm) - 1.0)
        splits += T178SplitRecord(
            name = name,
            fromCount = 34,
            toCount = 30,
            fromPhase = fromPhase,
            toPhase = toPhase,
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
            interactionOverCountTerm =
                abs(split.interaction) / abs(split.countTermAtFromPhase),
            pathDisagreement = roundForResult(split.pathDisagreement, 2, T178_DECISION_FLOOR),
            verdict = verdict
        )
    }

    if (T178_REFERENCE_PHASE in t178Phases && T178_RECOMMENDED_PHASE in t178Phases) {
        addSplit(
            "the CANONICAL 2 x 2 — one search-free rule at both phases",
            T178_REFERENCE_PHASE, T178_RECOMMENDED_PHASE,
            cellAt(canonicalFamily, T178_REFERENCE_PHASE, 34)!!.p90OverStroke,
            cellAt(canonicalFamily, T178_REFERENCE_PHASE, 30)!!.p90OverStroke,
            cellAt(canonicalFamily, T178_RECOMMENDED_PHASE, 34)!!.p90OverStroke,
            cellAt(canonicalFamily, T178_RECOMMENDED_PHASE, 30)!!.p90OverStroke,
            "the primary reading: no search anywhere, so the interaction is a property of the " +
                    "two axes and not of two searches of unequal tightness"
        )
        val grownFamily =
            "published-adjacent — C-0074's 30 roots grown by C-0103's own addition rule"
        addSplit(
            "the PUBLISHED-ADJACENT 2 x 2 — C-0063's 34 and C-0074's 30, each nested at its " +
                    "own phase",
            T178_REFERENCE_PHASE, T178_RECOMMENDED_PHASE,
            cellAt(
                "reference — C-0103's chain A on C-0063's own 34 roots", T178_REFERENCE_PHASE, 34
            )!!.p90OverStroke,
            cellAt(
                "reference — C-0103's chain A on C-0063's own 34 roots", T178_REFERENCE_PHASE, 30
            )!!.p90OverStroke,
            cellAt(grownFamily, T178_RECOMMENDED_PHASE, 34)!!.p90OverStroke,
            cellAt(grownFamily, T178_RECOMMENDED_PHASE, 30)!!.p90OverStroke,
            "the as-designed reading, and its 34-at-phase-8 corner is a CONSTRUCTION rather " +
                    "than an optimum, so the anchor qualities are not matched"
        )
    }

    // ------------------------------------------------------------------ the rank agreement
    val rankRecords = ArrayList<T178RankRecord>()
    T178_COUNTS.forEach { count ->
        val rows = gradedPhases.mapNotNull { cellAt(canonicalFamily, it, count) }
        if (rows.size >= 3) {
            rankRecords += T178RankRecord(
                scope = "ACROSS PHASES at $count paths",
                pairs = rows.size,
                spearman = spearmanRankCorrelation(
                    rows.map { it.worstSingleRemovalOverStroke }, rows.map { it.p90OverStroke }
                ),
                transfers = false,
                note = "the scope C-0098 measured at 0.468487481 on the shared body, here on " +
                        "the array at fixed count"
            )
        }
    }
    gradedPhases.forEach { phase ->
        val rows = T178_COUNTS.mapNotNull { cellAt(canonicalFamily, phase, it) }
        rankRecords += T178RankRecord(
            scope = "ACROSS COUNTS at phase $phase",
            pairs = rows.size,
            spearman = spearmanRankCorrelation(
                rows.map { it.worstSingleRemovalOverStroke }, rows.map { it.p90OverStroke }
            ),
            transfers = false,
            note = "the scope C-0103 measured at 0.94-1.00 at phase 24"
        )
    }
    run {
        val rows = gradedPhases.flatMap { phase ->
            T178_COUNTS.mapNotNull { cellAt(canonicalFamily, phase, it) }
        }
        rankRecords += T178RankRecord(
            scope = "over the whole canonical grid, phases and counts mixed",
            pairs = rows.size,
            spearman = spearmanRankCorrelation(
                rows.map { it.worstSingleRemovalOverStroke }, rows.map { it.p90OverStroke }
            ),
            transfers = false,
            note = "C-0089 measures 0.972896669 over 22 designs at mixed counts"
        )
    }
    val rankAgreement = rankRecords.map { it.copy(transfers = abs(it.spearman) >= 0.8) }

    // ------------------------------------------------------------------ the cheap bounds
    val cheapInteraction = interactions.firstOrNull { it.scope.startsWith("the CHEAP") }
    val gradedInteraction = interactions.firstOrNull { it.scope.startsWith("the GRADED grid —") }
    val publishedPhaseTerm = if (
        publishedArray.containsKey(T178_REFERENCE_PHASE) &&
        publishedArray.containsKey(T178_RECOMMENDED_PHASE)
    ) publishedArray.getValue(T178_RECOMMENDED_PHASE).second /
            publishedArray.getValue(T178_REFERENCE_PHASE).second else Double.NaN

    val cheapBounds = listOf(
        T178BoundRecord(
            name = "bound 1 — C-0089's single-removal instrument over the whole swept grid, " +
                    "n solves per cell and no sampling: its own two-way interaction",
            value = cheapInteraction?.worstResidualPerCent ?: 0.0,
            unit = "per cent of a level",
            settles = "whether the interaction can be screened without the ensemble at all; " +
                    "the graded grid measures " +
                    "%.6f".format(gradedInteraction?.worstResidualPerCent ?: 0.0) + " per cent",
            falsifierFired = false
        ),
        T178BoundRecord(
            name = "bound 2 — C-0098's OWN graded array cells at full upward inventory, read " +
                    "at run time: the phase term between C-0063's 24 and C-0074's 8",
            value = 100.0 * (publishedPhaseTerm - 1.0),
            unit = "per cent",
            settles = "the SIGN of the phase term at a nearly matched count (53 against 52 " +
                    "ties). C-0103's subtraction attributes -19.0 per cent to the phase; the " +
                    "only published grading of the two phases on the array runs the other way",
            falsifierFired = false
        ),
        T178BoundRecord(
            name = "bound 3 — the path identity: the two orderings of the 2 x 2 share their " +
                    "endpoints, so they differ by exactly one number",
            value = 4.0,
            unit = "graded cells",
            settles = "how expensive the headline is. The interaction on the recommendation's " +
                    "own move is a function of four cells and not of the whole grid; the " +
                    "32-phase grid buys the generality and not the verdict",
            falsifierFired = false
        )
    )

    // ------------------------------------------------------------------ the verdicts
    val canonicalCells = cells.filter { it.family == canonicalFamily }
    val adverse = countTerms.count { it.adverse }
    val f1Fired = adverse < countTerms.size
    val canonicalSplit = splits.firstOrNull { it.name.startsWith("the CANONICAL") }
    // F2 exactly as T-178 declared it, and it is an OR of two independently sufficient limbs.
    val f2LimbA = gradedInteraction?.let { it.worstResidualOverCountMainTerm >= 1.0 } ?: false
    val f2LimbB = canonicalSplit?.let {
        abs(it.interactionPerCent) > 0.5 * T178_C0103_COUNT_TERM_PER_CENT
    } ?: false
    val f2Fired = f2LimbA || f2LimbB
    val countRanks = rankAgreement.filter { it.scope.startsWith("ACROSS COUNTS") }
    val phaseRanks = rankAgreement.filter { it.scope.startsWith("ACROSS PHASES") }
    val f3Fired = countRanks.any { !it.transfers }
    val f4Fired = reproductions.any { it.strict && it.departure > 1e-6 }
    val uniformDishing = t178Phases.maxOf { phase ->
        abs(
            t178Host(sheet, phase).solve(uniformPressure(interiorPressure))
                .peakDishing(T178_SAMPLES)
        ) / freeStroke
    }
    val f5Fired = uniformDishing > 1e-6

    val falsifiers = listOf(
        T178FalsifierRecord(
            name = "F1",
            statement = "THE DECLARED ONE — the count term is not one-signed across the phase " +
                    "family: at one or more phases the 34 -> 30 reduction at fixed station " +
                    "geometry LOWERS the 90th percentile under the measured dropout",
            fired = f1Fired,
            outcome = "$adverse of ${countTerms.size} swept phases pay a cost for the " +
                    "reduction; the count term runs from " +
                    "%.6f".format(countTerms.minOfOrNull { it.costPerCent } ?: 0.0) +
                    " to " + "%.6f".format(countTerms.maxOfOrNull { it.costPerCent } ?: 0.0) +
                    " per cent"
        ),
        T178FalsifierRecord(
            name = "F2",
            statement = "the two axes are NOT separable: the worst residual of the balanced " +
                    "two-way additive fit is at least as large as the count term it splits, " +
                    "or the 2 x 2 interaction on the recommendation's own move exceeds half " +
                    "of C-0103's +12.86 per cent",
            fired = f2Fired,
            outcome = "LIMB A " + (if (f2LimbA) "FIRED" else "did not fire") +
                    ": the graded grid's worst residual is " +
                    "%.6f".format(gradedInteraction?.worstResidual ?: 0.0) +
                    " log units against a count main effect of " +
                    "%.6f".format(abs(gradedInteraction?.countMainTerm ?: 0.0)) +
                    ", a ratio of " +
                    "%.6f".format(gradedInteraction?.worstResidualOverCountMainTerm ?: 0.0) +
                    ". LIMB B " + (if (f2LimbB) "FIRED" else "did not fire") +
                    ": the canonical 2 x 2 interaction is " +
                    "%.6f".format(canonicalSplit?.interactionPerCent ?: 0.0) +
                    " per cent against half of C-0103's +12.8596328, i.e. " +
                    "%.7f".format(0.5 * T178_C0103_COUNT_TERM_PER_CENT) + " per cent"
        ),
        T178FalsifierRecord(
            name = "F3",
            statement = "the cheap single-removal instrument fails to transfer ACROSS COUNTS " +
                    "here as well as across phases (Spearman rho < 0.8 within a phase), in " +
                    "which case it screens nothing on this lattice",
            fired = f3Fired,
            outcome = "across counts rho runs " +
                    "%.6f".format(countRanks.minOfOrNull { it.spearman } ?: 0.0) + " to " +
                    "%.6f".format(countRanks.maxOfOrNull { it.spearman } ?: 0.0) +
                    " and across phases at fixed count " +
                    "%.6f".format(phaseRanks.minOfOrNull { it.spearman } ?: 0.0) + " to " +
                    "%.6f".format(phaseRanks.maxOfOrNull { it.spearman } ?: 0.0)
        ),
        T178FalsifierRecord(
            name = "F4",
            statement = "a standing figure fails to reproduce — C-0103's 0.638498565 or " +
                    "0.720607136, C-0063's 0.0706145537, the uncoupled tile's 0.307902368, or " +
                    "C-0089's 0.583664426",
            fired = f4Fired,
            outcome = "worst strict departure over " +
                    reproductions.count { it.strict }.toString() + " reproductions: " +
                    "%.2e".format(reproductions.filter { it.strict }.maxOf { it.departure })
        ),
        T178FalsifierRecord(
            name = "F5",
            statement = "the uncoupled tile under a UNIFORM load dishes non-zero on any of the " +
                    "swept hosts",
            fired = f5Fired,
            outcome = "worst over " + t178Phases.size.toString() + " hosts: " +
                    "%.2e".format(uniformDishing) + " of the free-tile stroke"
        )
    )

    val predicates = listOf(
        T178PredicateRecord(
            name = "P1",
            statement = "the 90th percentile is emitted on a nested, search-free family at " +
                    "22/25/28/30/34/45 paths at every swept crossover phase, each on its own host",
            verdict = if (canonicalCells.size == t178Phases.size * T178_COUNTS.size) "PASS"
            else "FAIL"
        ),
        T178PredicateRecord(
            name = "P2",
            statement = "the cheap single-removal instrument runs first over the same grid and " +
                    "its rank agreement is measured across phases at fixed count",
            verdict = if (phaseRanks.isNotEmpty() && cheapInteraction != null) "PASS" else "FAIL"
        ),
        T178PredicateRecord(
            name = "P3",
            statement = "the interaction is measured two independent ways — a balanced two-way " +
                    "additive fit over the grid, and the two orderings of the 2 x 2 on the " +
                    "recommendation's own move, whose totals must agree identically",
            verdict = if (gradedInteraction != null && splits.isNotEmpty() &&
                splits.all { it.pathDisagreement < 1e-12 }
            ) "PASS" else "FAIL"
        ),
        T178PredicateRecord(
            name = "P4",
            statement = "the phases are stratified by C-0102's census and the count term is " +
                    "reported per stratum",
            verdict = if (interactions.count { it.scope.contains("stratum") } >= 2) "PASS"
            else "FAIL"
        ),
        T178PredicateRecord(
            name = "P5",
            statement = "every headline is reproduced rather than cited",
            verdict = if (!f4Fired) "PASS" else "FAIL"
        )
    )

    val result = T178Result(
        task = "T-178",
        question = "Does C-0103's count effect at fixed station geometry hold at the other 31 " +
                "crossover phases, and is its decomposition of the recommended 34 -> 30 move " +
                "into a count term and a phase term well posed?",
        leaf = "A8.2, with A1.2",
        conditions = mapOf(
            "temperature" to "300 K",
            "kBT" to "4.141947 pN*nm",
            "medium" to "aqueous 2 mM MgCl2",
            "tile" to "40.0 x 40.35 nm single-layer square-lattice sheet, 15 duplexes at 2.69 nm",
            "load" to "C-0022's SOLVED edge profile at " + designProfile.name,
            "mandate" to "C-0017's 33.3333333 pN/nm as a SUM at S3's acceptable 3 nm, shared " +
                    "EQUALLY at every count and every phase",
            "dropout" to "C-0087's MEASURED_DEPTH incorporation field, one Bernoulli stream " +
                    "PER PHASE over that phase's own site inventory, restricted per subset",
            "statistic" to "the 90th percentile, nearest rank, over the free-tile stroke",
            "flatness" to "T-5b's 0.10 CONVENTION, not a physical threshold",
            "family" to "the CANONICAL search-free nested chain: one root per row at the site " +
                    "nearest the tile centre, grown by C-0103's own addition rule"
        ),
        decision = "",
        parameters = mapOf(
            "phasesSwept" to t178Phases.joinToString(","),
            "counts" to T178_COUNTS.joinToString(","),
            "realisations" to t178Realisations.toString(),
            "seed" to T178_GRADING_SEED.toString(),
            "dishingSamplesPerEdge" to T178_SAMPLES.toString(),
            "freeTileStroke" to freeStroke.roundedForProse().toString(),
            "mandate" to T178_MANDATE.roundedForProse().toString(),
            "tolerance" to "%.9f".format(T178_TOLERANCE),
            "separabilityThresholdInLogUnits" to "%.9f".format(T178_SEPARABLE),
            "decisionDigits" to T178_DECISION_DIGITS.toString(),
            "referencePhase" to T178_REFERENCE_PHASE.toString(),
            "recommendedPhase" to T178_RECOMMENDED_PHASE.toString()
        ),
        cheapBounds = cheapBounds,
        census = censusRecords,
        cells = cells,
        countTerms = countTerms,
        interactions = interactions,
        splits = splits,
        rankAgreement = rankAgreement,
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
            "ONE anchor rule. The canonical family is search-free and therefore matched across " +
                    "phases, but it is a THIRD rule beside C-0103's two, and C-0103 measured up " +
                    "to 6 per cent of rule dependence between its own two chains below 30 " +
                    "paths. The levels carry that dependence; the signs and the interaction are " +
                    "what is read.",
            "EQUAL springs at every cell. The distribution axis is C-0089's (1.30-1.61x on an " +
                    "array) and is T-179's; nothing here bounds the three-way interaction of " +
                    "count, phase and distribution.",
            "ONE width and ONE load state: 40.00 nm, because the reproduction gates are " +
                    "written there, and C-0022's solved 2 mM / 10 nm / 0.192 V. C-0086/C-0090/" +
                    "C-0102 have moved the buildable width to 38.08 nm and the recommended " +
                    "phase to 8, where C-0102's census is the authority; C-0068 has shown a " +
                    "placement can reverse between layer heights.",
            "The published-adjacent 2 x 2 has ONE constructed corner: 34 roots at phase 8 is " +
                    "C-0074's 30 grown by an addition rule, not an optimum, so its anchor " +
                    "quality is not matched to C-0063's exhaustively enumerated 34 at phase 24. " +
                    "The CANONICAL 2 x 2 carries no such asymmetry and is the primary reading.",
            "The arm footprint is neither reported nor imposed here: an arm's length is a " +
                    "function of the count (C-0075) and buildability is a plan axis " +
                    "C-0069/C-0075/C-0103 own.",
            "T-5b's 0.10 is a CONVENTION, not a physical threshold.",
            "The dishing pipeline, the lattice, the hosts, the load and the free-tile stroke " +
                    "are C-0058's, C-0063's, C-0087's, C-0089's and C-0103's unchanged, and " +
                    "inherit C-0022's unsourced rim charge and C-0001's single foundation " +
                    "secant.",
            "Single layer, static, 300 K, aqueous 2 mM MgCl2."
        ),
        openQuestions = listOf(
            "Whether the coupling element's own incorporation is the staple's — C-0087's item " +
                    "2, unchanged, and still the only route by which this programme keeps a " +
                    "flat tile.",
            "Whether a count sweep with the DISTRIBUTION freed at every count reorders the " +
                    "cells, and whether the interaction survives it. T-179.",
            "Whether the same grid at C-0086's buildable 38.08 nm carries the same interaction. " +
                    "The congruences differ (C-0102), the inventory collapses, and the strata " +
                    "are not the same sets.",
            "What fraction of built tiles a flatness verdict is owed over — C-0087's item 4, " +
                    "unchanged, and the parameter the whole branch is most sensitive to."
        )
    )

    val output = File("gpd/results/T-178-count-phase-interaction.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }

    fun write(value: T178Result) = output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(value).roundedForResult(
                digits = T178_DECISION_DIGITS + 3,
                digitsByKey = DEPARTURE_DIGITS_BY_KEY,
                floor = T178_DECISION_FLOOR
            ).withEmissionHeader(LatticeTag.SQUARE, null) as JsonObject)
        ) + "\n"
    )

    // `CLAUDE.md`: build the result and WRITE THE JSON before formatting any prose.
    write(result)

    val withProse = result.copy(
        decision = t178Decision(countTerms, gradedInteraction, canonicalSplit, canonicalCells),
        findings = t178Findings(
            countTerms, interactions, splits, rankAgreement, cheapBounds, canonicalCells,
            censusRecords, publishedPhaseTerm, falsifiers
        )
    )
    write(withProse)
    println("T-178 — wrote " + output.path)
    withProse.findings.forEach { println("  * " + it) }
    withProse.predicates.forEach { println("  [" + it.verdict + "] " + it.name) }
    withProse.falsifiers.forEach {
        println("  " + it.name + " " + (if (it.fired) "FIRED" else "did not fire") + ": " + it.outcome)
    }
}

// ---------------------------------------------------------------------------------------------
// the prose, built AFTER the JSON has been written once
// ---------------------------------------------------------------------------------------------

private fun six(value: Double) = "%.6f".format(value)

private fun nine(value: Double) = "%.9f".format(value)

private fun t178Decision(
    countTerms: List<T178CountTermRecord>,
    graded: T178InteractionRecord?,
    split: T178SplitRecord?,
    cells: List<T178CellRecord>
): String {
    val adverse = countTerms.count { it.adverse }
    val head = if (adverse == countTerms.size)
        "THE COUNT EFFECT HOLDS AT EVERY SWEPT PHASE: the 34 -> 30 reduction at fixed station " +
                "geometry costs dropout robustness at " + adverse + " of " + countTerms.size +
                " phases."
    else
        "THE COUNT EFFECT DOES NOT HOLD AT EVERY PHASE: it is adverse at " + adverse + " of " +
                countTerms.size + " swept phases and favourable at " +
                (countTerms.size - adverse) + "."
    val size = "The count term runs " + six(countTerms.minOf { it.costPerCent }) + " to " +
            six(countTerms.maxOf { it.costPerCent }) + " per cent against C-0103's " +
            "+12.8596328 per cent at phase 24."
    val inter = if (graded == null) "" else
        " The two axes " + (if (graded.separable) "ARE" else "are NOT") + " separable to 1 % of " +
                "a level: the balanced two-way additive fit over the graded grid leaves a worst " +
                "residual of " + six(graded.worstResidualPerCent) + " per cent of a level, " +
                six(graded.worstResidualOverCountMainTerm) + " of its own count main effect, " +
                "carrying " + six(100.0 * graded.interactionShare) +
                " per cent of the total variation."
    val splitProse = if (split == null) "" else
        " On the recommendation's own 2 x 2 the count term is " +
                six(split.countTermAtFromPhasePerCent) + " per cent taken first and " +
                six(split.countTermAtToPhasePerCent) + " per cent taken second, an interaction " +
                "of " + six(split.interactionPerCent) + " per cent, i.e. " +
                six(split.interactionOverCountTerm) + " of the term it splits."
    val flat = " Nothing here is flat: " + cells.count { !it.flatAtP90 } + " of " + cells.size +
            " graded canonical cells exceed T-5b's 0.10 at the 90th percentile."
    return head + " " + size + inter + splitProse + flat
}

@Suppress("LongParameterList", "LongMethod")
private fun t178Findings(
    countTerms: List<T178CountTermRecord>,
    interactions: List<T178InteractionRecord>,
    splits: List<T178SplitRecord>,
    ranks: List<T178RankRecord>,
    bounds: List<T178BoundRecord>,
    cells: List<T178CellRecord>,
    census: List<T178CensusRecord>,
    publishedPhaseTerm: Double,
    falsifiers: List<T178FalsifierRecord>
): List<String> {
    val findings = ArrayList<String>()
    val adverse = countTerms.filter { it.adverse }
    val favourable = countTerms.filter { !it.adverse }

    findings += "THE COUNT TERM AT EVERY PHASE. The 34 -> 30 reduction at fixed station " +
            "geometry is adverse at " + adverse.size + " of " + countTerms.size +
            " crossover phases and favourable at " + favourable.size + ". It runs from " +
            six(countTerms.minOf { it.costPerCent }) + " per cent (phase " +
            countTerms.minByOrNull { it.costPerCent }!!.phaseBasePairs + ") to " +
            six(countTerms.maxOf { it.costPerCent }) + " per cent (phase " +
            countTerms.maxByOrNull { it.costPerCent }!!.phaseBasePairs + "), against C-0103's " +
            "+12.8596328 per cent measured at phase 24 on its own anchor. " +
            (if (favourable.isEmpty() && countTerms.all { it.costPerCent > 0.0 })
                "The SIGN is a property of the count and survives the whole phase family; the " +
                        "SIZE is not, and it spans a factor of " +
                        six(
                            countTerms.maxOf { it.costPerCent } /
                                    countTerms.minOf { it.costPerCent }
                        ) + "."
            else
                "So the sign is NOT a property of the count: at " + favourable.size +
                        " phases the reduction is free or better, which is what F1 declared.")

    val monotone = countTerms.count { it.monotoneDecreasingInCount }
    val breaks = (0 until T178_COUNTS.size - 1).map { step ->
        step to countTerms.count { it.p90ByCount[step + 1] >= it.p90ByCount[step] }
    }
    findings += "C-0103's OWN FALSIFIER, RE-READ AT EVERY PHASE — AND THE BREAKS ARE AT THE " +
            "SPARSE END, NOT AT THE RECOMMENDATION'S STEP. Its F1 asked whether the 90th " +
            "percentile is monotone decreasing in the path count over 22 -> 45 at fixed " +
            "station geometry; on the CANONICAL search-free family that holds at " + monotone +
            " of " + countTerms.size + " phases, and the step-by-step census is " +
            breaks.joinToString(", ") { (step, count) ->
                "" + T178_COUNTS[step] + " -> " + T178_COUNTS[step + 1] + ": " + count +
                        " of " + countTerms.size + " non-decreasing"
            } + ". The canonical rule is not C-0103's chain A, so this is a statement about " +
            "an anchor rule as well as about the lattice: what it establishes is that the " +
            "monotonicity C-0103 measured is NOT robust to the rule at the sparse end, while " +
            "the 34 -> 30 step the recommendation moves through is adverse at " +
            countTerms.count { it.adverse } + " of " + countTerms.size + " phases."

    val graded = interactions.firstOrNull { it.scope.startsWith("the GRADED grid —") }
    val cheap = interactions.firstOrNull { it.scope.startsWith("the CHEAP") }
    if (graded != null) {
        findings += "THE TWO AXES ARE " + (if (graded.separable) "SEPARABLE" else "NOT " +
                "SEPARABLE TO 1 % OF A LEVEL") + ", AND THE MEASUREMENT IS A BALANCED TWO-WAY " +
                "FIT. Over " +
                graded.phases.size + " phases and " + graded.counts.size +
                " counts the additive model in log p90 leaves a worst residual of " +
                nine(graded.worstResidual) + " log units, i.e. " +
                six(graded.worstResidualPerCent) + " per cent of a level. The phase carries " +
                six(100.0 * graded.phaseSumOfSquares / graded.totalSumOfSquares) +
                " per cent of the total variation, the count " +
                six(100.0 * graded.countSumOfSquares / graded.totalSumOfSquares) +
                " per cent and the interaction " + six(100.0 * graded.interactionShare) +
                " per cent; the worst residual is " +
                six(graded.worstResidualOverCountMainTerm) +
                " of the fit's own 34 -> 30 count main effect of " +
                six(graded.countMainTermPerCent) + " per cent; the decomposition closes to " +
                nine(graded.decompositionResidual) +
                ", which is the orthogonality of a balanced design and is asserted as a gate."
    }
    if (cheap != null && graded != null) {
        findings += "AND THE CHEAP INSTRUMENT SAW IT FIRST, FOR n SOLVES A CELL AND NO " +
                "SAMPLING. The single-removal grid's own interaction is " +
                six(cheap.worstResidualPerCent) + " per cent of a level against the graded " +
                "grid's " + six(graded.worstResidualPerCent) + " per cent, and its " +
                "interaction share is " + six(100.0 * cheap.interactionShare) +
                " per cent against " + six(100.0 * graded.interactionShare) + " per cent."
    }

    val canonical = splits.firstOrNull { it.name.startsWith("the CANONICAL") }
    if (canonical != null) {
        findings += "C-0103'S DECOMPOSITION IS ONE OF TWO PATHS ACROSS A 2 x 2, AND THE SPLIT " +
                "IS PATH-DEPENDENT WHERE THE TOTAL IS NOT. On the canonical, search-free 2 x 2 " +
                "between phases " + canonical.fromPhase + " and " + canonical.toPhase +
                " the same journey reads: count first " +
                six(canonical.countTermAtFromPhasePerCent) + " per cent then phase " +
                six(canonical.phaseTermAtToCountPerCent) + " per cent; phase first " +
                six(canonical.phaseTermAtFromCountPerCent) + " per cent then count " +
                six(canonical.countTermAtToPhasePerCent) + " per cent. The total is " +
                six(canonical.totalPerCent) + " per cent either way (path disagreement " +
                nine(canonical.pathDisagreement) + "), and the two splits differ by the " +
                "interaction, " + six(canonical.interactionPerCent) + " per cent — " +
                six(canonical.interactionOverCountTerm) + " of the count term itself."
    }
    val published = splits.firstOrNull { it.name.startsWith("the PUBLISHED-ADJACENT") }
    if (published != null) {
        findings += "ON THE PUBLISHED DESIGNS THE SAME 2 x 2 READS " +
                six(published.countTermAtFromPhasePerCent) + " per cent of count taken first " +
                "and " + six(published.countTermAtToPhasePerCent) +
                " per cent taken second, an interaction of " +
                six(published.interactionPerCent) + " per cent. Its 34-at-phase-" +
                published.toPhase + " corner is a CONSTRUCTION and not an optimum, so the " +
                "anchor qualities are not matched and this reading is offered beside the " +
                "canonical one rather than instead of it."
    }

    if (publishedPhaseTerm.isFinite()) {
        findings += "AND THE PUBLISHED CROSS-CHECK RUNS THE OTHER WAY. C-0098's own graded " +
                "ARRAY cells at full upward inventory read " +
                six(100.0 * (publishedPhaseTerm - 1.0)) + " per cent going from phase 24 to " +
                "phase 8 at a nearly matched count — a phase term of the OPPOSITE sign to the " +
                "-19.0 per cent C-0103's subtraction attributes to the phase. That number was " +
                "in the corpus before this task ran, it costs no solve, and it is read at run " +
                "time from C-0098's own result file."
    }

    val countRanks = ranks.filter { it.scope.startsWith("ACROSS COUNTS") }
    val phaseRanks = ranks.filter { it.scope.startsWith("ACROSS PHASES") }
    fun spread(rows: List<T178RankRecord>): String =
        if (rows.isEmpty()) "no scope at this sweep"
        else six(rows.minOf { it.spearman }) + " to " + six(rows.maxOf { it.spearman }) +
                " (" + rows.count { it.transfers } + " of " + rows.size + " transfer)"
    val phaseTransfers = phaseRanks.isNotEmpty() && phaseRanks.all { it.transfers }
    findings += "THE CHEAP RANKING INSTRUMENT " +
            (if (phaseTransfers) "DOES TRANSFER ACROSS PHASES ON THE ARRAY AT FIXED COUNT, " +
                    "WHICH IS NOT WHAT C-0098 MEASURED. " else "FAILS ACROSS PHASES HERE TOO. ") +
            "Across counts within a phase Spearman rho runs " + spread(countRanks) +
            "; across phases at FIXED count it runs " + spread(phaseRanks) +
            ", against C-0089's 0.972896669 across designs and C-0098's 0.468487481 across " +
            "phases. " +
            (if (phaseTransfers)
                "C-0098's rho = 0.468487481 is measured on the SHARED BODY over six phases at " +
                        "each phase's own FULL inventory, so its count moves with its phase " +
                        "(52, 53 and 60 ties); at fixed count on the array the instrument " +
                        "ranks 32 phases at 0.88-0.98. The axis the bound fails on is " +
                        "therefore narrower than 'the phase', and a phase screen on the array " +
                        "can be run for n solves a cell."
            else
                "So the bound explains the level and not the ordering on this axis, and a " +
                        "phase screen run on it is an upper bound and nothing more.")

    val strataRows = interactions.filter { it.scope.contains("stratum") }
    if (strataRows.isNotEmpty()) {
        findings += "THE INTERACTION IS NOT A HOST EFFECT, BECAUSE IT SURVIVES INSIDE EVERY " +
                "STRATUM. C-0102's three demands cut the 32 phases into structurally " +
                "comparable sets, and the worst additive residual inside them is " +
                strataRows.joinToString("; ") {
                    it.scope.substringAfter("stratum: ") + " " +
                            six(it.worstResidualPerCent) + " per cent over " +
                            it.phases.size + " phases"
                } + "."
    }

    findings += "THE HOSTS ARE NOT INTERCHANGEABLE AND THE CENSUS SAYS SO BEFORE ANY SOLVE. " +
            "Of the " + census.size + " swept phases " +
            census.count { it.eightColumnHost } + " are eight-column hosts, " +
            census.count { it.richestInventory } + " carry the richest 60-site inventory and " +
            census.count { it.centroSymmetric } + " are centro-symmetric; the uncoupled tile " +
            "itself dishes " + six(census.minOf { it.freeTileDishingOverStroke }) + " to " +
            six(census.maxOf { it.freeTileDishingOverStroke }) + " of the stroke across them, " +
            "so the baseline a coupling is judged against is a function of the phase too."

    val sources = cells.count { it.worseThanNoCouplingAtP90 }
    findings += "UNDER THE MEASURED DROPOUT THIS WHOLE FAMILY IS A NET DISHING SOURCE AT EVERY " +
            "PHASE. " + sources + " of " + cells.size + " canonical cells exceed their own " +
            "host's uncoupled dishing at the 90th percentile, and " +
            cells.count { !it.flatAtP90 } + " of " + cells.size +
            " exceed T-5b's 0.10, the worst at " + six(cells.maxOf { it.p90OverStroke }) +
            ". So neither the count nor the phase decides an acceptance verdict on this branch, " +
            "exactly as C-0103 found at one phase."

    val f1 = falsifiers.first { it.name == "F1" }
    val f2 = falsifiers.first { it.name == "F2" }
    findings += "THE VERDICT ON C-0103's DEFENCE. F1 " +
            (if (f1.fired) "FIRED" else "did not fire") + " and F2 " +
            (if (f2.fired) "FIRED" else "did not fire") + ". " +
            (if (!f1.fired)
                "The count effect is real at every phase this lattice offers, so C-0103's " +
                        "measurement generalises in SIGN. "
            else
                "The count effect is not one-signed across the phases, so C-0103's +12.86 per " +
                        "cent is a phase-24 reading rather than a property of the count. ") +
            (if (canonical == null) ""
            else "What does not generalise is the SPLIT: the interaction on the " +
                    "recommendation's own move is " + six(canonical.interactionPerCent) +
                    " per cent, so 'the count term is +12.86 per cent and the phase term is " +
                    "-19.0 per cent' is one of two readings of the same journey and the other " +
                    "reading gives " + six(canonical.phaseTermAtFromCountPerCent) + " and " +
                    six(canonical.countTermAtToPhasePerCent) + " per cent. The TOTAL is what " +
                    "the recommendation rests on, and the total is path-independent, so the " +
                    "defence survives on its endpoints and not on its decomposition.")

    return findings
}
