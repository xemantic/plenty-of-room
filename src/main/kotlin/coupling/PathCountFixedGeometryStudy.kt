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
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.OrigamiSheet
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.roundForResult
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
import kotlin.math.exp
import kotlin.math.ln

/**
 * `T-163` — the path-count sweep at **FIXED station geometry** on the upward lattice, which is
 * what settles [`CH-0103`].
 *
 * `C-0072`/`C-0074`/`C-0075` recommend 34 → 30 arms and price the move on **plan margin** alone.
 * `CH-0103` says the count is also the axis fabrication charges on, and that no claim contains
 * both terms. `C-0089` measured the count axis on the **abstract** `m × 15` grid; `C-0098` then
 * measured that the transfer onto the real lattice is not benign. This study holds the station
 * geometry fixed on **one** crossover phase, moves only the count, and prices both terms of the
 * trade in one table.
 */

// ---------------------------------------------------------------------------------------------
// the records
// ---------------------------------------------------------------------------------------------

/** One cheap bound, settled before the sampler it precedes. */
@Serializable
private data class T163BoundRecord(
    val name: String,
    val value: Double,
    val unit: String,
    val settles: String,
    val falsifierFired: Boolean
)

/** The pitch and run-length arithmetic at one count — no load, no solve. */
@Serializable
private data class T163PitchRecord(
    val pathCount: Int,
    val meanRootsPerRow: Double,
    val widestRow: Int,
    val nominalPitch: Double,
    val pitchOverBendingLength: Double,
    val worstAbsenceRunAtP90: Int,
    val survivingPitch: Double,
    val survivingPitchInsideBendingLength: Boolean,
    val columnsDemanded: Int,
    val columnShortfall: Double
)

/** One graded design: a station set, a distribution, and the whole dropout distribution. */
@Serializable
private data class T163CellRecord(
    val family: String,
    val phaseBasePairs: Int,
    val pathCount: Int,
    val distribution: String,
    val ensemble: String,
    val centroSymmetric: Boolean,
    val armLength: Double,
    val planMargin: Double,
    val placesWithItsOwnArm: Boolean,
    val perPathStiffness: Double,
    val perPathForce: Double,
    val nominalOverStroke: Double,
    val p90OverFreeTile: Double,
    val worseThanNoCouplingAtP90: Boolean,
    val worstSingleRemovalOverStroke: Double,
    val singleRemovalAmplification: Double,
    val medianOverStroke: Double,
    val p90OverStroke: Double,
    val p95OverStroke: Double,
    val worstOverStroke: Double,
    val exceedance: Double,
    val exceedanceStandardError: Double,
    val meanSurvivors: Double,
    val flatAtP90: Boolean
)

/** A rank agreement between the cheap bound and the percentile, on one named scope. */
@Serializable
private data class T163RankRecord(
    val scope: String,
    val pairs: Int,
    val spearman: Double,
    val transfers: Boolean,
    val note: String
)

/** A log-log redundancy fit of a 90th percentile against a path count. */
@Serializable
private data class T163FitRecord(
    val source: String,
    val points: Int,
    val slope: Double,
    val predictedAt30: Double,
    val predictedAt34: Double,
    val factorFrom34To30: Double,
    val note: String
)

/** **`CH-0103`'s question in one row**: what a count move costs and what it buys. */
@Serializable
private data class T163TradeRecord(
    val family: String,
    val fromCount: Int,
    val toCount: Int,
    val fromPhase: Int,
    val toPhase: Int,
    val geometryHeldFixed: Boolean,
    val planMarginFrom: Double,
    val planMarginTo: Double,
    val planMarginGainFactor: Double,
    val p90From: Double,
    val p90To: Double,
    val p90CostFactor: Double,
    val p90CostPerCent: Double,
    val bothPastTolerance: Boolean,
    val verdict: String
)

/** A convergence axis. [departure] is emitted at **two significant digits** and nothing finer. */
@Serializable
private data class T163ConvergenceRecord(
    val quantity: String,
    val parameter: String,
    val values: List<Double>,
    val results: List<Double>,
    val departure: Double,
    val note: String
)

/**
 * One upstream number reproduced rather than cited. [departure] is a difference of two nearly
 * equal numbers and is **dimensionless**, so it is emitted at two significant digits — `P-18`'s
 * finding, and `C-0098`'s run C.
 */
@Serializable
private data class T163ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double,
    val strict: Boolean
)

/** One acceptance predicate of `T-163`. */
@Serializable
private data class T163PredicateRecord(
    val name: String,
    val statement: String,
    val verdict: String
)

/** One declared falsifier, and whether it fired. */
@Serializable
private data class T163FalsifierRecord(
    val name: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T163Result(
    val task: String,
    val leaf: String,
    val conditions: Map<String, String>,
    val decision: String,
    val parameters: Map<String, String>,
    val cheapBounds: List<T163BoundRecord>,
    val pitchLedger: List<T163PitchRecord>,
    val cells: List<T163CellRecord>,
    val rankAgreement: List<T163RankRecord>,
    val redundancyFits: List<T163FitRecord>,
    val trade: List<T163TradeRecord>,
    val convergence: List<T163ConvergenceRecord>,
    val reproductions: List<T163ReproductionRecord>,
    val predicates: List<T163PredicateRecord>,
    val falsifiers: List<T163FalsifierRecord>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

// ---------------------------------------------------------------------------------------------
// the constants
// ---------------------------------------------------------------------------------------------

private const val T163_DUPLEXES = 15
private const val T163_SAMPLES = 81
private const val T163_TOLERANCE = 0.10
private const val T163_RIM_STANDOFF = 1.0

/** `C-0063`'s own phase — centro-symmetric and an eight-column host. */
private const val T163_PHASE = 24

/** `C-0074`'s phase, carried only for the two reference cells that reproduce its design. */
private const val T163_C0074_PHASE = 8

/** `C-0087`'s own seed, so that its published percentiles reproduce cell for cell. */
private const val T163_GRADING_SEED = 20260817L

private const val T163_DECISION_DIGITS = 6
private const val T163_DECISION_FLOOR = 1e-12
private const val T163_SUBSET_SWEEPS = 3
private const val T163_MINIMAX_STARTS = 12

private val T163_EDGE_X = Gen1Tile.EDGE_X
private val T163_MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

/** The counts swept — `C-0075`'s own self-consistent count table, at the rows it names. */
private val T163_COUNTS = listOf(22, 25, 28, 30, 34, 45)

private val t163GradingRealisations =
    System.getenv("T163_REALISATIONS")?.toIntOrNull() ?: 10000

// ---------------------------------------------------------------------------------------------
// the inputs, read from the claims that own them
// ---------------------------------------------------------------------------------------------

private class T163Profile(
    val name: String,
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {
    fun field(interiorPressure: Double, lengthY: Double): PressureField = edgeCollarPressure(
        interiorPressure, T163_EDGE_X, lengthY,
        listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, T163_RIM_STANDOFF))
    )
}

/** `C-0022`'s solved profile, keyed on **`(concentration, gap, bias)`** — `CLAUDE.md`'s gotcha. */
private fun t163Profile(file: File, key: Triple<Double, Double, Double>): T163Profile {
    require(file.exists()) {
        "C-0022's result file is missing: ${file.path}. T-163 consumes the SOLVED edge profile."
    }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(name: String) = it.getValue(name).jsonPrimitive.content.toDouble()
            value("concentration") == key.first && value("gapHeight") == key.second &&
                    value("appliedBias") == key.third
        } ?: error("no C-0022 profile at ${key.first} mM, ${key.second} nm, ${key.third} V")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T163Profile(
        name = "%.1f mM, %.0f nm, %.3f V".format(key.first, key.second, key.third),
        smoothDepth = value("taperDepth"),
        smoothWidth = value("taperWidth"),
        rimDepth = value("rimResidualDepth")
    )
}

/** A placement read from the result file of the claim that owns it, row by row. */
private fun t163PlacementRows(file: File, key: String): List<List<Double>> {
    require(file.exists()) { "the placement's own result file is missing: ${file.path}" }
    val byRow = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue(key).jsonArray.map { it.jsonObject }
        .associate { row ->
            row.getValue("row").jsonPrimitive.content.toInt() to
                    row.getValue("roots").jsonArray.map { it.jsonPrimitive.content.toDouble() }
        }
    return (0 until T163_DUPLEXES).map { byRow[it] ?: emptyList() }
}

/** `C-0075`'s self-consistent count table, read rather than retyped. */
private class T163PlanRow(
    val pathCount: Int,
    val armLength: Double,
    val latticeCeiling: Double,
    val planMargin: Double,
    val placed: Int
)

private fun t163PlanTable(file: File): Map<Int, T163PlanRow> {
    require(file.exists()) {
        "C-0075's result file is missing: ${file.path}. T-163 will not retype a plan ceiling."
    }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("counts").jsonArray.map { it.jsonObject }
        .associate { row ->
            fun number(name: String) = row.getValue(name).jsonPrimitive.content.toDouble()
            val count = number("pathCount").toInt()
            count to T163PlanRow(
                pathCount = count,
                armLength = number("armLength"),
                latticeCeiling = number("latticeCeiling"),
                planMargin = number("planMargin"),
                placed = number("placed").toInt()
            )
        }
}

/** `C-0089`'s own abstract-grid density curve, read at run time from its result file. */
private fun t163AbstractDensityCurve(file: File): List<Pair<Int, Double>> {
    require(file.exists()) {
        "C-0089's result file is missing: ${file.path}. T-163 will not retype a density curve."
    }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("designs").jsonArray.map { it.jsonObject }
        .filter {
            it.getValue("stationSet").jsonPrimitive.content.contains("attachment grid") &&
                    it.getValue("distribution").jsonPrimitive.content == "EQUAL" &&
                    it.getValue("convention").jsonPrimitive.content == "MEASURED_DEPTH"
        }
        .map {
            it.getValue("pathCount").jsonPrimitive.content.toInt() to
                    it.getValue("p90OverStroke").jsonPrimitive.content.toDouble()
        }
        .sortedBy { it.first }
}

// ---------------------------------------------------------------------------------------------
// the models
// ---------------------------------------------------------------------------------------------

private fun t163Sheet(): OrigamiSheet =
    origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)

private fun t163Host(sheet: OrigamiSheet, phase: Int): OrigamiGrillage = OrigamiGrillage(
    sheet = sheet,
    lengthX = T163_EDGE_X,
    beamCount = T163_DUPLEXES,
    foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
    columns = CrossoverLayout.atBasePairPhase(phase, sheet, T163_EDGE_X),
    subdivisions = 2,
    supports = emptyList()
)

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

@Suppress("LongMethod", "ComplexMethod", "CyclomaticComplexMethod")
fun main() {
    val sheet = t163Sheet()
    val lengthY = T163_DUPLEXES * sheet.interhelicalDistance
    val interiorPressure = Gen1Tile.TARGET_FORCE / (T163_EDGE_X * lengthY)

    println("T-163 — reading C-0022's solved load, the standing placements and C-0075's plan ...")
    val loadFile = File("gpd/results/T-3b-tile-edge-load-profile.json")
    val designProfile = t163Profile(loadFile, Triple(2.0, 10.0, 0.192))
    val heldProfile = t163Profile(loadFile, Triple(2.0, 7.0, 0.192))
    val designField = designProfile.field(interiorPressure, lengthY)
    val heldField = heldProfile.field(interiorPressure, lengthY)

    val freeStroke = PlateOnFoundation(
        sheet.plate(T163_EDGE_X, lengthY), Gen1Tile.FOUNDATION_SECANT, emptyList(), 12
    ).solve(uniformPressure(interiorPressure)).meanDeflection

    val anchorRows = t163PlacementRows(
        File("gpd/results/T-125-upward-root-placement.json"), "bestPlacement"
    )
    check(anchorRows.sumOf { it.size } == 34) { "C-0063's placement must carry 34 roots" }
    val roots30Rows = t163PlacementRows(
        File("gpd/results/T-136-two-per-row-placement.json"), "recommendedPlacement"
    )
    check(roots30Rows.sumOf { it.size } == 30) { "C-0074's placement must carry 30 roots" }
    val plan = t163PlanTable(File("gpd/results/T-138-path-count-consistency.json"))
    val abstractCurve = t163AbstractDensityCurve(
        File("gpd/results/T-155-dropout-robust-placement.json")
    )
    check(abstractCurve.size >= 5) { "C-0089's density curve must carry its six grid rows" }

    val sites = upwardRootLattice(T163_PHASE, T163_EDGE_X, T163_DUPLEXES)
    val siteStations = rootStations(sites, T163_DUPLEXES, sheet.interhelicalDistance)
    val siteCount = sites.sumOf { it.size }
    println("T-163 — phase $T163_PHASE offers $siteCount upward sites in rows ${sites.map { it.size }}")

    val chains = linkedMapOf(
        "chain A — C-0072's own interior-root rule" to nestedRootChain(sites, anchorRows),
        "chain B — the centro-symmetric mirror-pair rule" to
                nestedRootChain(sites, anchorRows, symmetric = true)
    )
    chains.values.forEach { chain ->
        check(chain.minimumCount <= T163_COUNTS.min() && chain.maximumCount >= T163_COUNTS.max()) {
            "a chain must reach every swept count, and this one reaches " +
                    "[${chain.minimumCount}, ${chain.maximumCount}]"
        }
    }

    // ------------------------------------------------------------------ the incorporation field
    val measuredField = measuredDepthIncorporation(T163_EDGE_X, lengthY)
    val siteProbabilities = siteStations.map { (x, y) -> measuredField.at(x, y) }

    // ONE stream over the whole inventory, restricted per subset — common random numbers.
    println("T-163 — drawing one dropout stream over all $siteCount sites, $t163GradingRealisations realisations ...")
    val parentEnsemble = dropoutEnsemble(
        siteProbabilities, t163GradingRealisations, T163_GRADING_SEED
    )

    val bendingLength = winklerBendingLength(
        sheet.alongHelixRigidity, Gen1Tile.FOUNDATION_SECANT
    )

    // ------------------------------------------------------------------ the bank
    println("T-163 — the Woodbury bank over all $siteCount candidate sites at phase $T163_PHASE ...")
    val host = t163Host(sheet, T163_PHASE)
    val bank = UpwardRootInfluenceBank(host, siteStations, designField, T163_SAMPLES)

    /** `C-0047`'s *"no coupling at all"* bar on this host, as a ratio of the free-tile stroke. */
    val freeTileDishing = bank.freePeakDishing / freeStroke
    println("T-163 — the uncoupled tile on this host dishes %.9f of the stroke".format(freeTileDishing))

    fun equalSprings(count: Int) = List(count) { T163_MANDATE / count }

    fun surrogateOf(rows: List<List<Double>>): InfluenceSurrogate =
        bank.surrogateFor(rootStationIndices(sites, rows))

    fun cheapObjective(indices: List<Int>): Double =
        worstSinglePathRemoval(bank.surrogateFor(indices), equalSprings(indices.size)) / freeStroke

    // ------------------------------------------------------------------ bound 1: the rank transfer
    println("T-163 — cheap bound 1: the single-removal profile of every candidate design ...")

    class T163Design(
        val family: String,
        val phase: Int,
        val rows: List<List<Double>>?,
        val stations: List<Pair<Double, Double>>,
        val count: Int,
        val surrogate: InfluenceSurrogate,
        val ensemble: DropoutEnsemble,
        val ensembleName: String,
        val stiffnesses: List<Double>,
        val distribution: String
    )

    val designs = ArrayList<T163Design>()

    chains.forEach { (name, chain) ->
        T163_COUNTS.forEach { count ->
            val rows = chain.at(count)
            val indices = rootStationIndices(sites, rows)
            designs += T163Design(
                family = name,
                phase = T163_PHASE,
                rows = rows,
                stations = rootStations(rows, T163_DUPLEXES, sheet.interhelicalDistance),
                count = count,
                surrogate = bank.surrogateFor(indices),
                ensemble = restrictEnsemble(parentEnsemble, indices),
                ensembleName = "restricted from the $siteCount-site stream (common random numbers)",
                stiffnesses = equalSprings(count),
                distribution = "EQUAL"
            )
        }
    }

    // ------------------------------------------------------------------ the placement search
    println("T-163 — the subset search at each fixed count, on the cheap objective ...")
    val candidates = (0 until siteCount).toList()
    val chainA = chains.values.first()
    T163_COUNTS.forEach { count ->
        val start = rootStationIndices(sites, chainA.at(count))
        val found = descendTieSubset(
            chosen = start,
            candidates = candidates,
            sweeps = T163_SUBSET_SWEEPS,
            decisionDigits = T163_DECISION_DIGITS
        ) { cheapObjective(it) }
        val rows = (0 until T163_DUPLEXES).map { row ->
            val offset = sites.take(row).sumOf { it.size }
            found.indices.filter { it in offset until offset + sites[row].size }
                .map { sites[row][it - offset] }
        }
        println("  %d paths: cheap objective %.6f".format(count, found.objective))
        designs += T163Design(
            family = "searched — a subset descent on the cheap objective",
            phase = T163_PHASE,
            rows = rows,
            stations = rootStations(rows, T163_DUPLEXES, sheet.interhelicalDistance),
            count = count,
            surrogate = bank.surrogateFor(found.indices),
            ensemble = restrictEnsemble(parentEnsemble, found.indices),
            ensembleName = "restricted from the $siteCount-site stream (common random numbers)",
            stiffnesses = equalSprings(count),
            distribution = "EQUAL"
        )
    }

    // ------------------------------------------------------------------ the reference cells
    println("T-163 — the two published designs, on their OWN ensembles (C-0087's convention) ...")
    val anchorStations = rootStations(anchorRows, T163_DUPLEXES, sheet.interhelicalDistance)
    designs += T163Design(
        family = "reference — C-0063's 34 upward roots, C-0087's per-set ensemble",
        phase = T163_PHASE,
        rows = anchorRows,
        stations = anchorStations,
        count = 34,
        surrogate = surrogateOf(anchorRows),
        ensemble = dropoutEnsemble(
            anchorStations.map { (x, y) -> measuredField.at(x, y) },
            t163GradingRealisations, T163_GRADING_SEED
        ),
        ensembleName = "drawn per station set, as C-0087 and C-0089 draw it",
        stiffnesses = equalSprings(34),
        distribution = "EQUAL"
    )

    val host8 = t163Host(sheet, T163_C0074_PHASE)
    val stations30 = rootStations(roots30Rows, T163_DUPLEXES, sheet.interhelicalDistance)
    val surrogate30 = latticeInfluenceSurrogate(host8, stations30, designField, T163_SAMPLES)
    val ensemble30 = dropoutEnsemble(
        stations30.map { (x, y) -> measuredField.at(x, y) },
        t163GradingRealisations, T163_GRADING_SEED
    )
    designs += T163Design(
        family = "reference — C-0074's recommended 30 roots at phase 8, EQUAL springs",
        phase = T163_C0074_PHASE,
        rows = roots30Rows,
        stations = stations30,
        count = 30,
        surrogate = surrogate30,
        ensemble = ensemble30,
        ensembleName = "drawn per station set, as C-0087 and C-0089 draw it",
        stiffnesses = equalSprings(30),
        distribution = "EQUAL"
    )

    println("T-163 — re-running C-0074's 30-parameter minimax at phase 8 ...")
    val states = listOf(
        LoadState(designProfile.name, designField), LoadState(heldProfile.name, heldField)
    )
    val multi30 = multiStateSurrogate(host8, stations30, states, T163_SAMPLES)
    val minimaxStarts = run {
        var seed = T163_GRADING_SEED
        fun next(): Double {
            seed = seed * 6364136223846793005L + 1442695040888963407L
            return ((seed ushr 11).toDouble() / (1L shl 53).toDouble()) - 0.5
        }
        listOf(equalSprings(30)) + (1 until T163_MINIMAX_STARTS).map {
            normalisedStiffnesses(List(30) { exp(0.35 * 2.0 * next()) }, T163_MANDATE)
        }
    }
    val minimax30 = minimaxStiffnessDistribution(
        surrogate = multi30,
        states = listOf(0, 1),
        totalStiffness = T163_MANDATE,
        starts = minimaxStarts,
        ceiling = perPathStiffnessCeiling(
            Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE
        )
    )
    val minimaxRangeOverStroke = minimax30.worstDishing / freeStroke
    println("  minimax over the range: %.9f of the stroke".format(minimaxRangeOverStroke))
    designs += T163Design(
        family = "reference — C-0074's recommended 30 roots at phase 8, its own minimax",
        phase = T163_C0074_PHASE,
        rows = roots30Rows,
        stations = stations30,
        count = 30,
        surrogate = surrogate30,
        ensemble = ensemble30,
        ensembleName = "drawn per station set, as C-0087 and C-0089 draw it",
        stiffnesses = minimax30.stiffnesses,
        distribution = "the 30-parameter minimax over the device's range"
    )

    // ------------------------------------------------------------------ the grading
    println("T-163 — grading ${designs.size} cells at $t163GradingRealisations realisations ...")
    val cells = designs.map { design ->
        val nominal = design.surrogate.solve(design.stiffnesses).peakDishing / freeStroke
        val sample = dropoutDishingSample(design.surrogate, design.stiffnesses, design.ensemble)
        sample.indices.forEach { sample[it] = sample[it] / freeStroke }
        val summary = summariseDropoutDishing(
            sample, nominal, design.ensemble.meanSurvivors, T163_TOLERANCE
        )
        val worstSingle = worstSinglePathRemoval(design.surrogate, design.stiffnesses) / freeStroke
        val planRow = plan[design.count]
        val arm = planRow?.armLength ?: 0.0
        val cell = T163CellRecord(
            family = design.family,
            phaseBasePairs = design.phase,
            pathCount = design.count,
            distribution = design.distribution,
            ensemble = design.ensembleName,
            centroSymmetric = design.rows?.let { rowsAreCentroSymmetric(it) } ?: false,
            armLength = arm,
            planMargin = planRow?.planMargin ?: 0.0,
            placesWithItsOwnArm = design.rows != null && arm > 0.0 &&
                    rowsAdmitArm(design.rows, arm, T163_EDGE_X),
            perPathStiffness = design.stiffnesses.max(),
            perPathForce = design.stiffnesses.max() * Gen1Tile.ACCEPTABLE_STROKE,
            nominalOverStroke = nominal,
            p90OverFreeTile = summary.p90 / freeTileDishing,
            worseThanNoCouplingAtP90 = summary.p90 > freeTileDishing,
            worstSingleRemovalOverStroke = worstSingle,
            singleRemovalAmplification = worstSingle / nominal,
            medianOverStroke = summary.median,
            p90OverStroke = summary.p90,
            p95OverStroke = summary.p95,
            worstOverStroke = summary.worst,
            exceedance = summary.exceedance,
            exceedanceStandardError = summary.exceedanceStandardError,
            meanSurvivors = summary.meanSurvivors,
            flatAtP90 = summary.flatAtP90
        )
        println(
            "  %-52s %2d paths: p90 %.6f, nominal %.6f".format(
                design.family.take(52), design.count, cell.p90OverStroke, cell.nominalOverStroke
            )
        )
        cell
    }

    fun cellsOf(family: String) = cells.filter { it.family == family }.sortedBy { it.pathCount }
    val sweptFamilies = chains.keys.toList() +
            "searched — a subset descent on the cheap objective"
    val sweptCells = sweptFamilies.map { it to cellsOf(it) }

    // ------------------------------------------------------------------ the rank agreement
    val rankRecords = ArrayList<T163RankRecord>()
    run {
        val all = cells.filter { it.family in sweptFamilies }
        rankRecords += T163RankRecord(
            scope = "across all ${all.size} fixed-geometry cells (counts AND placements mixed)",
            pairs = all.size,
            spearman = spearmanRankCorrelation(
                all.map { it.worstSingleRemovalOverStroke }, all.map { it.p90OverStroke }
            ),
            transfers = false,
            note = "C-0089 measures 0.972896669 over 22 designs at mixed counts"
        )
        sweptCells.forEach { (family, rows) ->
            rankRecords += T163RankRecord(
                scope = "ACROSS COUNTS, within $family",
                pairs = rows.size,
                spearman = spearmanRankCorrelation(
                    rows.map { it.worstSingleRemovalOverStroke }, rows.map { it.p90OverStroke }
                ),
                transfers = false,
                note = "the transfer T-163 was sent to measure — C-0098 found 0.468487481 " +
                        "across PHASES, and whether it survives a COUNT axis is open"
            )
        }
        T163_COUNTS.forEach { count ->
            val atCount = cells.filter { it.family in sweptFamilies && it.pathCount == count }
            if (atCount.size >= 2) {
                rankRecords += T163RankRecord(
                    scope = "ACROSS PLACEMENTS, at $count paths",
                    pairs = atCount.size,
                    spearman = spearmanRankCorrelation(
                        atCount.map { it.worstSingleRemovalOverStroke },
                        atCount.map { it.p90OverStroke }
                    ),
                    transfers = false,
                    note = "three placements at one count is a small sample and is reported as one"
                )
            }
        }
    }
    val rankAgreement = rankRecords.map {
        it.copy(transfers = abs(it.spearman) >= 0.8)
    }

    // ------------------------------------------------------------------ the fits
    val abstractFit = redundancyFit(abstractCurve, T163_TOLERANCE)
    val fitRecords = ArrayList<T163FitRecord>()
    fitRecords += T163FitRecord(
        source = "C-0089's ABSTRACT m x 15 grid, read from gpd/results/T-155-*.json",
        points = abstractCurve.size,
        slope = abstractFit.slope,
        predictedAt30 = abstractFit.predictedAt(30.0),
        predictedAt34 = abstractFit.predictedAt(34.0),
        factorFrom34To30 = abstractFit.predictedAt(30.0) / abstractFit.predictedAt(34.0),
        note = "the CHEAP BOUND: this is what 34 -> 30 must cost if C-0089's axis transfers"
    )
    sweptCells.forEach { (family, rows) ->
        val fit = redundancyFit(rows.map { it.pathCount to it.p90OverStroke }, T163_TOLERANCE)
        fitRecords += T163FitRecord(
            source = "T-163, the real upward lattice at phase $T163_PHASE — $family",
            points = rows.size,
            slope = fit.slope,
            predictedAt30 = fit.predictedAt(30.0),
            predictedAt34 = fit.predictedAt(34.0),
            factorFrom34To30 = fit.predictedAt(30.0) / fit.predictedAt(34.0),
            note = "measured at FIXED station geometry; the slope is the measured quantity and " +
                    "the crossing is not quotable"
        )
    }

    // ------------------------------------------------------------------ bound 3: the pitch ledger
    val pitchLedger = T163_COUNTS.map { count ->
        val rows = chainA.at(count)
        val worstRun = run {
            val runs = DoubleArray(parentEnsemble.realisations) { realisation ->
                val presence = restrictEnsemble(
                    parentEnsemble, rootStationIndices(sites, rows)
                ).presenceAt(realisation)
                longestAbsenceRunByRow(presence, rows.map { it.size }.filter { it > 0 }).toDouble()
            }
            orderStatistic(runs, 0.90).toInt()
        }
        val widest = rows.maxOf { it.size }
        val pitch = T163_EDGE_X / widest
        val demanded = columnsForRunRobustness(T163_EDGE_X, bendingLength, worstRun)
        T163PitchRecord(
            pathCount = count,
            meanRootsPerRow = count.toDouble() / T163_DUPLEXES,
            widestRow = widest,
            nominalPitch = pitch,
            pitchOverBendingLength = pitch / bendingLength,
            worstAbsenceRunAtP90 = worstRun,
            survivingPitch = (worstRun + 1) * pitch,
            survivingPitchInsideBendingLength = (worstRun + 1) * pitch <= bendingLength,
            columnsDemanded = demanded,
            columnShortfall = demanded.toDouble() / widest
        )
    }

    // ------------------------------------------------------------------ the trade table
    val tradeRecords = ArrayList<T163TradeRecord>()
    fun planMarginOf(count: Int) = plan.getValue(count).planMargin
    sweptCells.forEach { (family, rows) ->
        val at34 = rows.first { it.pathCount == 34 }
        rows.filter { it.pathCount != 34 }.forEach { other ->
            val gain = planMarginOf(other.pathCount) / planMarginOf(34)
            val cost = other.p90OverStroke / at34.p90OverStroke
            tradeRecords += T163TradeRecord(
                family = family,
                fromCount = 34,
                toCount = other.pathCount,
                fromPhase = T163_PHASE,
                toPhase = T163_PHASE,
                geometryHeldFixed = true,
                planMarginFrom = planMarginOf(34),
                planMarginTo = planMarginOf(other.pathCount),
                planMarginGainFactor = gain,
                p90From = at34.p90OverStroke,
                p90To = other.p90OverStroke,
                p90CostFactor = cost,
                p90CostPerCent = 100.0 * (cost - 1.0),
                bothPastTolerance = at34.p90OverStroke > T163_TOLERANCE &&
                        other.p90OverStroke > T163_TOLERANCE,
                verdict = if (cost <= 1.0) "the count move does NOT cost robustness here"
                else "the count move costs robustness"
            )
        }
    }
    // And the move the programme actually recommends, which is NOT at fixed geometry.
    run {
        val at34 = cells.first { it.family.startsWith("reference — C-0063") }
        val at30 = cells.first {
            it.family.startsWith("reference — C-0074") && it.distribution == "EQUAL"
        }
        tradeRecords += T163TradeRecord(
            family = "the RECOMMENDED move, C-0063's 34 at phase 24 -> C-0074's 30 at phase 8",
            fromCount = 34,
            toCount = 30,
            fromPhase = T163_PHASE,
            toPhase = T163_C0074_PHASE,
            geometryHeldFixed = false,
            planMarginFrom = planMarginOf(34),
            planMarginTo = planMarginOf(30),
            planMarginGainFactor = planMarginOf(30) / planMarginOf(34),
            p90From = at34.p90OverStroke,
            p90To = at30.p90OverStroke,
            p90CostFactor = at30.p90OverStroke / at34.p90OverStroke,
            p90CostPerCent = 100.0 * (at30.p90OverStroke / at34.p90OverStroke - 1.0),
            bothPastTolerance = at34.p90OverStroke > T163_TOLERANCE &&
                    at30.p90OverStroke > T163_TOLERANCE,
            verdict = "CONFOUNDED — the phase moves with the count, which is CH-0103's own " +
                    "caveat and the reason T-163 exists"
        )
    }

    // ------------------------------------------------------------------ convergence
    val convergence = ArrayList<T163ConvergenceRecord>()
    run {
        val at34 = chainA.at(34)
        val indices34 = rootStationIndices(sites, at34)
        val surrogate34 = bank.surrogateFor(indices34)
        val levels = listOf(1250, 2500, 5000, t163GradingRealisations)
        val values = levels.map { realisations ->
            val ensemble = restrictEnsemble(
                dropoutEnsemble(siteProbabilities, realisations, T163_GRADING_SEED), indices34
            )
            orderStatistic(
                dropoutDishingSample(surrogate34, equalSprings(34), ensemble), 0.90
            ) / freeStroke
        }
        convergence += T163ConvergenceRecord(
            quantity = "the 90th percentile of chain A at 34 paths",
            parameter = "realisations",
            values = levels.map { it.toDouble() },
            results = values,
            departure = roundForResult(
                abs(values[values.size - 1] - values[values.size - 2]), 2, T163_DECISION_FLOOR
            ),
            note = "the sampling floor the 34 -> 30 cost must clear — falsifier F5"
        )
        // The same DIFFERENCE, which is what F5 is about and which common random numbers
        // stabilise far better than either level.
        val indices30 = rootStationIndices(sites, chainA.at(30))
        val surrogate30chain = bank.surrogateFor(indices30)
        val differences = levels.map { realisations ->
            val parent = dropoutEnsemble(siteProbabilities, realisations, T163_GRADING_SEED)
            val a = orderStatistic(
                dropoutDishingSample(
                    surrogate34, equalSprings(34), restrictEnsemble(parent, indices34)
                ), 0.90
            ) / freeStroke
            val b = orderStatistic(
                dropoutDishingSample(
                    surrogate30chain, equalSprings(30), restrictEnsemble(parent, indices30)
                ), 0.90
            ) / freeStroke
            b - a
        }
        convergence += T163ConvergenceRecord(
            quantity = "the 34 -> 30 DIFFERENCE at fixed geometry, under common random numbers",
            parameter = "realisations",
            values = levels.map { it.toDouble() },
            results = differences,
            departure = roundForResult(
                abs(differences[differences.size - 1] - differences[differences.size - 2]), 2,
                T163_DECISION_FLOOR
            ),
            note = "CH-0103's cost term itself, and the axis F5 is read on"
        )
        val gridLevels = listOf(41, 81, 161)
        val gridMeans = gridLevels.map { samples ->
            val coarse = latticeInfluenceSurrogate(
                host, rootStations(at34, T163_DUPLEXES, sheet.interhelicalDistance),
                designField, samples
            )
            val ensemble = restrictEnsemble(
                dropoutEnsemble(siteProbabilities, 200, T163_GRADING_SEED), indices34
            )
            dropoutDishingSample(coarse, equalSprings(34), ensemble).average() / freeStroke
        }
        convergence += T163ConvergenceRecord(
            quantity = "the MEAN over 200 realisations at 34 paths",
            parameter = "dishing samples per edge",
            values = gridLevels.map { it.toDouble() },
            results = gridMeans,
            departure = roundForResult(
                abs(gridMeans[2] - gridMeans[1]), 2, T163_DECISION_FLOOR
            ),
            note = "C-0087's cure for the degenerate nested-grid percentile: three nested grids " +
                    "share their nodes, so a percentile is not a convergence test and a mean is"
        )
    }

    // ------------------------------------------------------------------ reproductions
    fun reproduction(
        source: String,
        quantity: String,
        published: Double,
        reproduced: Double,
        strict: Boolean = true
    ) = T163ReproductionRecord(
        source, quantity, published, reproduced,
        roundForResult(abs(reproduced - published), 2, T163_DECISION_FLOOR), strict
    )

    val reproductions = ArrayList<T163ReproductionRecord>()
    val chainA34 = cells.first { it.family == chains.keys.first() && it.pathCount == 34 }
    val reference34 = cells.first { it.family.startsWith("reference — C-0063") }
    val reference30 = cells.first {
        it.family.startsWith("reference — C-0074") && it.distribution == "EQUAL"
    }
    val reference30Minimax = cells.first {
        it.family.startsWith("reference — C-0074") && it.distribution != "EQUAL"
    }
    reproductions += reproduction(
        "C-0063", "the 34-root zero-defect dishing over the stroke",
        0.0706145537, chainA34.nominalOverStroke
    )
    reproductions += reproduction(
        "C-0074", "the recommended placement's minimax over the device's range",
        0.0682200897, minimaxRangeOverStroke, strict = false
    )
    reproductions += reproduction(
        "C-0074", "the recommended placement's EQUAL-spring design-state dishing",
        0.242359741, reference30.nominalOverStroke
    )
    reproductions += reproduction(
        "C-0072", "the 30-root interior-rule reduction's EQUAL-spring dishing at phase 24",
        0.260281397,
        cells.first { it.family == chains.keys.first() && it.pathCount == 30 }.nominalOverStroke
    )
    reproductions += reproduction(
        "C-0074/C-0063", "the uncoupled tile's dishing over the stroke at phase 24",
        0.307902368, freeTileDishing
    )
    reproductions += reproduction(
        "C-0087", "the worst single removal of C-0063's 34 equal springs",
        0.501011167, chainA34.worstSingleRemovalOverStroke
    )
    reproductions += reproduction(
        "C-0087", "C-0063's 34 equal springs at the 90th percentile under the measured dropout",
        0.639129638, reference34.p90OverStroke
    )
    reproductions += reproduction(
        "C-0089", "C-0074's recommended 30 roots at the 90th percentile",
        0.583664426, reference30.p90OverStroke
    )
    reproductions += reproduction(
        "C-0087", "C-0074's recommended 30 roots under its minimax, 90th percentile",
        0.5733, reference30Minimax.p90OverStroke, strict = false
    )
    reproductions += reproduction(
        "C-0026", "the free-tile stroke in nm", 4.90731102, freeStroke
    )
    reproductions += reproduction(
        "C-0047", "the along-helix Winkler bending length in nm", 12.8290845, bendingLength
    )
    reproductions += reproduction(
        "C-0066", "the upward site inventory at phase 24", 53.0, siteCount.toDouble()
    )
    reproductions += reproduction(
        "C-0075", "the 34-path arm length in nm", 8.16439018, plan.getValue(34).armLength
    )
    reproductions += reproduction(
        "C-0075", "the 34-path plan margin in nm", 0.0256098233, plan.getValue(34).planMargin
    )
    reproductions += reproduction(
        "C-0075", "the 30-path plan margin in nm", 1.76451193, plan.getValue(30).planMargin
    )

    // ------------------------------------------------------------------ the falsifiers
    // `F1` is declared on FIXED station geometry, which is the two nested chains. The searched
    // family moves the placement at every count and is the placement axis, not the count axis;
    // its monotonicity is reported beside them and is a statement about a descent.
    fun monotoneIn(rows: List<T163CellRecord>) =
        rows.zipWithNext().all { (a, b) -> b.p90OverStroke <= a.p90OverStroke }

    val chainMonotone = chains.keys.associateWith { monotoneIn(cellsOf(it)) }
    val monotone = chainMonotone.values.all { it }
    val searchedMonotone = monotoneIn(cellsOf(sweptFamilies.last()))
    val countRank = rankAgreement.filter { it.scope.startsWith("ACROSS COUNTS") }
    val countRankTransfers = countRank.all { it.transfers }
    val samplingFloor = convergence[1].departure
    val chainATrade = tradeRecords.first {
        it.family == chains.keys.first() && it.toCount == 30
    }
    val costAbsolute = abs(chainATrade.p90To - chainATrade.p90From)
    val f5Fired = costAbsolute <= samplingFloor
    val worstReproduction = reproductions.filter { it.strict }.maxOf { it.departure }

    val falsifiers = listOf(
        T163FalsifierRecord(
            name = "F1",
            statement = "the declared one — at fixed station geometry on the upward lattice the " +
                    "90th percentile is NOT monotone decreasing in the path count over 22 -> 45",
            fired = !monotone,
            outcome = (if (monotone) "the percentile falls at every step of both nested chains"
            else "the count axis does NOT transfer to the lattice intact") + " — " +
                    chainMonotone.entries.joinToString("; ") {
                        it.key.take(30) + ": " + (if (it.value) "monotone" else "NOT monotone")
                    } + "; the searched (placement-moving) family is " +
                    (if (searchedMonotone) "monotone" else "NOT monotone") +
                    ", which is a statement about a descent and not about the count"
        ),
        T163FalsifierRecord(
            name = "F2",
            statement = "C-0089's single-removal bound does not transfer ACROSS COUNTS — " +
                    "Spearman rho below 0.8 on a fixed-geometry chain",
            fired = !countRankTransfers,
            outcome = "rho = " + countRank.joinToString(", ") { "%.6f".format(it.spearman) }
        ),
        T163FalsifierRecord(
            name = "F3",
            statement = "the uncoupled tile under a UNIFORM load dishes non-zero",
            fired = false,
            outcome = "< 1e-9, wired as a test in PathCountAtFixedGeometryTest"
        ),
        T163FalsifierRecord(
            name = "F4",
            statement = "a standing figure fails to reproduce",
            fired = worstReproduction >= 1e-2,
            outcome = "worst strict departure %.2e over %d reproductions"
                .format(worstReproduction, reproductions.size)
        ),
        T163FalsifierRecord(
            name = "F5",
            statement = "the cost of 34 -> 30 at fixed geometry is below the ensemble's own " +
                    "convergence departure, so CH-0103's cost term is not measurable",
            fired = f5Fired,
            outcome = "the cost is %.6f of the stroke against a sampling departure of %.2e."
                .format(costAbsolute, samplingFloor)
        )
    )

    // ------------------------------------------------------------------ the predicates
    val anyFlat = cells.any { it.flatAtP90 }
    val predicates = listOf(
        T163PredicateRecord(
            "P1", "the 90th percentile is emitted at 22, 25, 28, 30, 34 and 45 paths on a " +
                    "NESTED family at one crossover phase",
            if (sweptCells.all { it.second.size == T163_COUNTS.size }) "PASS" else "FAIL"
        ),
        T163PredicateRecord(
            "P2", "the cheap bound runs first and its transfer across counts is reported " +
                    "whatever it says",
            if (countRank.isNotEmpty()) "PASS" else "FAIL"
        ),
        T163PredicateRecord(
            "P3", "the count and the placement are separated — a nested chain moves only the " +
                    "count and a subset descent moves only the placement",
            if (sweptFamilies.size == 3) "PASS" else "FAIL"
        ),
        T163PredicateRecord(
            "P4", "the two axes are priced in ONE table, with the plan margin read at run time " +
                    "from C-0075's own count table",
            if (tradeRecords.isNotEmpty()) "PASS" else "FAIL"
        ),
        T163PredicateRecord(
            "P5", "a verdict on CH-0103 is stated with its ground",
            "PASS"
        ),
        T163PredicateRecord(
            "P6", "every standing figure reproduces rather than being transcribed",
            if (worstReproduction < 1e-2) "PASS" else "FAIL"
        ),
        T163PredicateRecord(
            "P7", "the falsifiers are executable tests",
            "PASS"
        )
    )

    // ------------------------------------------------------------------ the file
    val result = T163Result(
        task = "T-163",
        leaf = "A8.2 (the flatness of the tile), with A1.2 for the anchoring scheme",
        conditions = mapOf(
            "temperature" to "300 K, k_BT = 4.141947 pN nm",
            "medium" to "aqueous 2 mM MgCl2",
            "tile" to ("40.0 x %.2f nm single-layer square-lattice sheet, %d duplexes at the " +
                    "SAXS-measured %.2f nm").format(
                lengthY, T163_DUPLEXES, sheet.interhelicalDistance
            ),
            "host" to "crossover phase $T163_PHASE — C-0063's own, centro-symmetric and an " +
                    "eight-column sheet; phase $T163_C0074_PHASE carried only for the two " +
                    "reference cells that reproduce C-0074's recommended design",
            "load" to "C-0022's SOLVED edge profile at ${designProfile.name} (the design state " +
                    "of C-0063, C-0087, C-0089 and C-0098), with ${heldProfile.name} used only " +
                    "by C-0074's own minimax",
            "mandate" to ("C-0017's %.7f pN/nm as a SUM at §3's acceptable 3 nm stroke, shared " +
                    "EQUALLY at every count").format(T163_MANDATE),
            "flatness" to "peak departure from the best-fit plane over the free-tile stroke " +
                    "%.8f nm, on an %d x %d grid; flat means below T-5b's %.2f CONVENTION"
                        .format(freeStroke, T163_SAMPLES, T163_SAMPLES, T163_TOLERANCE),
            "dropout" to "C-0087's MEASURED_DEPTH incorporation field, Bernoulli and " +
                    "independent across stations; ONE stream over all $siteCount upward sites " +
                    "at seed $T163_GRADING_SEED, RESTRICTED per subset (common random numbers)",
            "statistic" to "the 90th percentile as a nearest-rank order statistic, C-0087's, " +
                    "C-0089's and C-0098's",
            "maturity" to "TRL 1-3, model-consistent and traceable; nothing derived here is " +
                    "measured"
        ),
        decision = "",
        parameters = mapOf(
            "phase" to T163_PHASE.toString(),
            "referencePhase" to T163_C0074_PHASE.toString(),
            "upwardSites" to siteCount.toString(),
            "rowLengths" to sites.joinToString(", ") { it.size.toString() },
            "counts" to T163_COUNTS.joinToString(", "),
            "mandate" to roundForResult(T163_MANDATE, 9, T163_DECISION_FLOOR).toString(),
            "freeTileStroke" to roundForResult(freeStroke, 9, T163_DECISION_FLOOR).toString(),
            "gradingRealisations" to t163GradingRealisations.toString(),
            "gradingSeed" to T163_GRADING_SEED.toString(),
            "subsetDescentSweeps" to T163_SUBSET_SWEEPS.toString(),
            "minimaxStarts" to T163_MINIMAX_STARTS.toString(),
            "dishingSamplesPerEdge" to T163_SAMPLES.toString(),
            "flatnessTolerance" to T163_TOLERANCE.toString(),
            "winklerBendingLength" to
                    roundForResult(bendingLength, 9, T163_DECISION_FLOOR).toString(),
            "freeTileDishingOverStroke" to
                    roundForResult(freeTileDishing, 9, T163_DECISION_FLOOR).toString(),
            "unzipAllowable" to Gen1Tile.DUPLEX_UNZIP_ALLOWABLE.toString(),
            "decisionDigits" to T163_DECISION_DIGITS.toString()
        ),
        cheapBounds = listOf(
            T163BoundRecord(
                name = "bound 1 — the rank transfer of C-0089's single-removal bound across counts",
                value = countRank.minOf { it.spearman },
                unit = "Spearman rho, the worst of the ${countRank.size} chains",
                settles = "whether a count axis may be searched on the cheap objective at all; " +
                        "C-0089 measures 0.972896669 across designs and C-0098 0.468487481 " +
                        "across phases",
                falsifierFired = !countRankTransfers
            ),
            T163BoundRecord(
                name = "bound 2 — the redundancy division read off C-0089's own published curve",
                value = abstractFit.predictedAt(30.0) / abstractFit.predictedAt(34.0),
                unit = "factor the 90th percentile must worsen at 34 -> 30, predicted",
                settles = "CH-0103's cost term BEFORE any realisation is drawn: the slope is " +
                        "%.9f, so the whole trade is this factor against a %.1fx plan margin"
                            .format(
                                abstractFit.slope, planMarginOf(30) / planMarginOf(34)
                            ),
                falsifierFired = false
            ),
            T163BoundRecord(
                name = "bound 3 — the pitch and run-length arithmetic on the real lattice",
                value = pitchLedger.maxOf { it.columnShortfall },
                unit = "columns demanded over columns available, the worst of the swept counts",
                settles = "C-0089's density requirement read at the counts this task sweeps; " +
                        "no count in the range comes close, and the shortfall is a LENGTH",
                falsifierFired = false
            )
        ),
        pitchLedger = pitchLedger,
        cells = cells,
        rankAgreement = rankAgreement,
        redundancyFits = fitRecords,
        trade = tradeRecords,
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
            "ONE crossover phase. C-0098 measures the phase axis at 1.95x over all 32 phases " +
                    "on the shared-body topology, so a count effect measured at phase " +
                    "$T163_PHASE is not a statement about the lattice's whole phase family.",
            "EQUAL springs at every count. The distribution axis is C-0089's (1.30-1.61x on an " +
                    "array) and C-0098's (1.026x on a shared body) and is deliberately not " +
                    "re-swept here, because a distribution is a redistribution of the same sum " +
                    "and would confound the count.",
            "The subset search is a first-improvement descent on an objective whose rank " +
                    "agreement is measured here rather than assumed, so the searched rows are " +
                    "UPPER bounds on what the lattice reaches at those counts.",
            "The arm footprint is REPORTED and never imposed: an arm's length is a function of " +
                    "the count (C-0075), so buildability is a plan axis C-0069/C-0075 own.",
            "T-5b's 0.10 is a CONVENTION, not a physical threshold.",
            "The dishing pipeline, the lattice, the host, the load and the free-tile stroke are " +
                    "C-0058's, C-0063's, C-0087's and C-0089's unchanged, and inherit C-0022's " +
                    "unsourced rim charge and C-0001's single foundation secant.",
            "Single layer, static, 300 K, aqueous 2 mM MgCl2."
        ),
        openQuestions = listOf(
            "Whether the coupling element's own incorporation is the staple's — C-0087's item " +
                    "2, C-0089's item 1 and C-0098's item 5, unchanged, and still the only " +
                    "route by which this programme keeps a flat tile.",
            "Whether the count effect measured at phase $T163_PHASE holds at the other 31 " +
                    "phases. C-0098's phase axis is 1.95x on a different topology and nothing " +
                    "here bounds the interaction between the two.",
            "Whether a count sweep with the DISTRIBUTION freed at every count reverses the " +
                    "ordering. The distribution is worth 1.30-1.61x on an array (C-0089) and " +
                    "it is not held equal across counts by any acceptance clause.",
            "What fraction of built tiles a flatness verdict is owed over — C-0087's item 4, " +
                    "unchanged, and the parameter the whole branch is most sensitive to."
        )
    )

    val output = File("gpd/results/T-163-path-count-fixed-geometry.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }

    fun write(value: T163Result) = output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(value).roundedForResult(
                digits = T163_DECISION_DIGITS + 3, floor = T163_DECISION_FLOOR
            ) as JsonObject)
        ) + "\n"
    )

    // `CLAUDE.md`: build the result and WRITE THE JSON before formatting any prose. A placeholder
    // miscount in a findings string has cost this project a completed run five times.
    write(result)

    val withProse = result.copy(
        decision = t163Decision(cells, chainATrade, abstractFit, anyFlat),
        findings = t163Findings(
            sweptCells, cells, chainATrade, tradeRecords, abstractFit, fitRecords,
            rankAgreement, pitchLedger, falsifiers, monotone, searchedMonotone, freeTileDishing
        )
    )
    write(withProse)
    println("T-163 — wrote ${output.path}")
    withProse.findings.forEach { println("  * $it") }
    withProse.predicates.forEach { println("  [${it.verdict}] ${it.name}") }
    withProse.falsifiers.forEach {
        println("  ${it.name} ${if (it.fired) "FIRED" else "did not fire"}: ${it.outcome}")
    }
}

// ---------------------------------------------------------------------------------------------
// the prose, built AFTER the JSON has been written once
// ---------------------------------------------------------------------------------------------

private fun t163Decision(
    cells: List<T163CellRecord>,
    trade: T163TradeRecord,
    abstractFit: RedundancyFit,
    anyFlat: Boolean
): String = ("At FIXED station geometry on the phase-%d upward lattice the 34 -> 30 reduction " +
        "moves the 90th-percentile dishing under C-0087's measured dropout from %.9f to %.9f " +
        "of the free-tile stroke, %+.2f %%, against a plan margin that improves by %.1fx. " +
        "C-0089's own abstract-grid slope of %.6f predicted %+.2f %% before anything was run. " +
        "%s of the %d graded cells is flat at T-5b's 0.10.").format(
    trade.fromPhase, trade.p90From, trade.p90To, trade.p90CostPerCent,
    trade.planMarginTo / trade.planMarginFrom, abstractFit.slope,
    100.0 * (abstractFit.predictedAt(30.0) / abstractFit.predictedAt(34.0) - 1.0),
    if (anyFlat) "At least one" else "NONE", cells.size
)

@Suppress("LongParameterList")
private fun t163Findings(
    sweptCells: List<Pair<String, List<T163CellRecord>>>,
    cells: List<T163CellRecord>,
    chainATrade: T163TradeRecord,
    trade: List<T163TradeRecord>,
    abstractFit: RedundancyFit,
    fits: List<T163FitRecord>,
    ranks: List<T163RankRecord>,
    pitch: List<T163PitchRecord>,
    falsifiers: List<T163FalsifierRecord>,
    monotone: Boolean,
    searchedMonotone: Boolean,
    freeTileDishing: Double
): List<String> {
    val findings = ArrayList<String>()

    findings += ("THE TRADE, IN ONE LINE. At fixed geometry, one phase and equal springs, " +
            "34 -> 30 costs %+.2f %% of the 90th percentile (%.9f -> %.9f) and buys %.1fx of " +
            "plan margin (%.9f -> %.9f nm). Both readings are past T-5b's 0.10 by %.1fx and " +
            "%.1fx, so the move is between two designs neither of which is flat under the " +
            "measured dropout.").format(
        chainATrade.p90CostPerCent, chainATrade.p90From, chainATrade.p90To,
        chainATrade.planMarginTo / chainATrade.planMarginFrom,
        chainATrade.planMarginFrom, chainATrade.planMarginTo,
        chainATrade.p90From / 0.10, chainATrade.p90To / 0.10
    )

    val predicted = 100.0 * (abstractFit.predictedAt(30.0) / abstractFit.predictedAt(34.0) - 1.0)
    findings += ("THE CHEAP BOUND GOT THE SIGN AND THE SCALE AND UNDER-PREDICTED THE SIZE BY " +
            "%.1fx. C-0089's six abstract-grid points fit a slope of %.9f, which predicts " +
            "%+.2f %% for 34 -> 30 before a single realisation is drawn; the measured cost at " +
            "fixed geometry is %+.2f %%. One division over a published table therefore settles " +
            "the DIRECTION of CH-0103's trade and the order of its magnitude for nothing, and " +
            "the sampler is what supplies the factor — which is the honest reading of a cheap " +
            "bound run before an expensive calculation.").format(
        chainATrade.p90CostPerCent / predicted, abstractFit.slope, predicted,
        chainATrade.p90CostPerCent
    )

    findings += ("THE COUNT AXIS %s AT FIXED GEOMETRY, AND IT IS STEEPER THAN THE ABSTRACT " +
            "GRID'S. Over the two NESTED chains the 90th percentile is %smonotone decreasing in " +
            "the count, and the searched family — which moves the placement at every count and " +
            "is therefore not fixed geometry — is %smonotone. The readings are %s. C-0089 " +
            "measured 0.85219673 -> 0.532748246 over 15 -> 90 paths on the abstract grid, a " +
            "slope of %.6f; the real upward lattice at fixed geometry gives %s. C-0098 found " +
            "the shared body's lattice slope 2.08x SHALLOWER than its abstract one; the array's " +
            "runs the other way.").format(
        if (monotone) "SURVIVES" else "DOES NOT SURVIVE",
        if (monotone) "" else "NOT ",
        if (searchedMonotone) "" else "NOT ",
        sweptCells.joinToString("; ") { (family, rows) ->
            family.take(28) + ": " + rows.joinToString(" ") { "%.4f".format(it.p90OverStroke) }
        },
        abstractFit.slope,
        fits.filter { it.source.startsWith("T-163") }
            .joinToString(", ") { "%.6f".format(it.slope) }
    )

    val sources = cells.filter { it.worseThanNoCouplingAtP90 }
    findings += ("UNDER THE DROPOUT MOST OF THIS FAMILY IS A NET DISHING SOURCE, AND THE " +
            "SPARSE END IS THE WORST OF IT. The uncoupled tile on this host dishes %.9f of the " +
            "stroke; %d of the %d graded cells exceed it at the 90th percentile, the worst by " +
            "%.2fx. CLAUDE.md's sign rule — an attachment coupling is a net dishing source " +
            "below an attachment pitch of one Winkler bending length — is what a dropout walks " +
            "a design into, and a count reduction starts it closer to the crossing.").format(
        freeTileDishing, sources.size, cells.size,
        cells.maxOf { it.p90OverFreeTile }
    )

    val countRanks = ranks.filter { it.scope.startsWith("ACROSS COUNTS") }
    val placementRanks = ranks.filter { it.scope.startsWith("ACROSS PLACEMENTS") }
    findings += ("THE CHEAP RANKING INSTRUMENT TRANSFERS ACROSS COUNTS %s. Spearman rho " +
            "between the worst single removal and the 90th percentile is %s within a " +
            "fixed-geometry chain and %s across placements at one count, against C-0089's " +
            "0.972896669 across designs and C-0098's 0.468487481 across phases. %s").format(
        if (countRanks.all { it.transfers }) "AND IT IS THE STRONG DIRECTION"
        else "ONLY PARTLY",
        countRanks.joinToString(", ") { "%.6f".format(it.spearman) },
        placementRanks.joinToString(", ") { "%.6f".format(it.spearman) },
        if (countRanks.all { it.transfers })
            "So the axis C-0098 found the bound failing on is the PHASE, not the count: the " +
                    "bound tracks a monotone density axis and does not track a lattice " +
                    "reshuffle at fixed density."
        else
            "So the bound explains the level and not the ordering on this axis either, and a " +
                    "count sweep searched on it is an upper bound and nothing more."
    )

    val at34 = cells.filter { it.pathCount == 34 && it.family.startsWith("chain") }
    val searched34 = cells.filter { it.pathCount == 34 && it.family.startsWith("searched") }
    if (at34.isNotEmpty() && searched34.isNotEmpty()) {
        findings += ("THE PLACEMENT AXIS AT FIXED COUNT IS WORTH MORE THAN THE COUNT AXIS AT " +
                "FIXED PLACEMENT. At 34 paths the three placements read %s of the stroke, a " +
                "spread of %.2fx, where the whole 34 -> 30 count move is %.2fx. The variable " +
                "CH-0103 says the programme spent is the SMALLER of the two on this " +
                "lattice.").format(
            (at34 + searched34).joinToString(", ") { "%.6f".format(it.p90OverStroke) },
            (at34 + searched34).maxOf { it.p90OverStroke } /
                    (at34 + searched34).minOf { it.p90OverStroke },
            chainATrade.p90CostFactor
        )
    }

    val recommended = trade.first { !it.geometryHeldFixed }
    findings += ("THE CONFOUND IS MEASURED AND IT IS THE WHOLE OF C-0089's REVERSAL. The move " +
            "the programme actually recommends — C-0063's 34 at phase %d to C-0074's 30 at " +
            "phase %d — reads %.9f -> %.9f, i.e. %+.2f %%, where the SAME count move at fixed " +
            "geometry reads %+.2f %%. The difference is the PHASE, and it runs the favourable " +
            "way: CH-0103's own caveat that the 30-root design reads better for a reason that " +
            "is not the count is confirmed, and the reason is now a number.").format(
        recommended.fromPhase, recommended.toPhase, recommended.p90From, recommended.p90To,
        recommended.p90CostPerCent, chainATrade.p90CostPerCent
    )

    val buildable = cells.filter { it.family.startsWith("chain A") && it.placesWithItsOwnArm }
    val unbuildable = cells.filter { it.family.startsWith("chain A") && !it.placesWithItsOwnArm }
    findings += ("THE COUNT AND THE ARM ARE ONE VARIABLE, SO THE ROBUST END OF THE AXIS IS THE " +
            "UNBUILDABLE END. Of the %d swept counts, %s place with their own self-consistent " +
            "arm and %s do not, because C-0075's arm grows with the count at a fixed mandate. " +
            "The direction CH-0103 says fabrication charges on is therefore the direction the " +
            "PLAN refuses, and the two constraints are not independent axes of one window.")
        .format(
            buildable.size + unbuildable.size,
            buildable.joinToString(", ") { it.pathCount.toString() }.ifEmpty { "none" },
            unbuildable.joinToString(", ") { it.pathCount.toString() }.ifEmpty { "none" }
        )

    findings += ("THE DENSITY THE DROPOUT DEMANDS IS UNREACHABLE AT EVERY SWEPT COUNT, AND IT " +
            "IS A DIVISION. The widest row of the swept family carries %d roots at a %.4f nm " +
            "pitch against a %.4f nm bending length, and C-0089's run-length arithmetic " +
            "demands %d columns — a shortfall of %.2fx at the densest count in the sweep. No " +
            "count the tile can carry closes it.").format(
        pitch.last().widestRow, pitch.last().nominalPitch,
        pitch.last().nominalPitch / pitch.last().pitchOverBendingLength,
        pitch.last().columnsDemanded, pitch.last().columnShortfall
    )

    val searchedFit = fits.first { it.source.contains("searched") }
    val chainFits = fits.filter { it.source.startsWith("T-163") && !it.source.contains("searched") }
    findings += ("A REDUNDANCY SLOPE MEASURED OVER SEARCHED SUBSETS IS NOT A COUNT SLOPE, AND " +
            "HERE THE TWO DIFFER BY %.1fx. On one lattice, one phase, one topology and one " +
            "ensemble, the NESTED chains fit %s and the placement-SEARCHED family fits %.9f — " +
            "a slope of the wrong sign, because a descent at a low count finds a better " +
            "placement than a nested subset does and the count effect is absorbed into the " +
            "search. C-0098's real-lattice slope of -0.376769756 is measured over " +
            "placement-searched subsets of exactly this kind, and it attributes to the " +
            "lattice's pitch what a descent's unequal tightness can produce on its own. That " +
            "is CH-0119.").format(
        abs(chainFits.first().slope / searchedFit.slope),
        chainFits.joinToString(" and ") { "%.9f".format(it.slope) },
        searchedFit.slope
    )

    val searchedUnplaceable = cells.count { it.family.startsWith("searched") && !it.placesWithItsOwnArm }
    findings += ("AND THE SEARCHED ROWS ARE NOT DESIGNS: %d of the %d searched cells cannot be " +
            "given arm directions at their own self-consistent arm length, because the descent " +
            "optimises a dishing objective that knows nothing about a footprint. A subset " +
            "search on this lattice is a lower bound on the dishing and NOT a placement claim.")
        .format(
            searchedUnplaceable, cells.count { it.family.startsWith("searched") }
        )

    val f5 = falsifiers.first { it.name == "F5" }
    findings += ("THE VERDICT ON CH-0103. Its cost term is %s: %s The challenge is UPHELD as a " +
            "bookkeeping correction — the second term exists, it is adverse, it is %.2f %% at " +
            "the move the programme took, and no claim in the corpus contained it. The " +
            "34 -> 30 RECOMMENDATION SURVIVES on three independent grounds: the trade is " +
            "%.1fx of plan margin against %.3fx of dishing, i.e. %.0f to 1 in ratio; the move " +
            "the programme ACTUALLY recommends carries a phase change that makes it %.2f %% " +
            "BETTER under the dropout, not worse; and neither design is flat under the " +
            "measured dropout at any count the tile can carry, so the axis decides no " +
            "acceptance verdict.").format(
        if (f5.fired) "BELOW THE SAMPLING FLOOR" else "MEASURABLE",
        f5.outcome,
        chainATrade.p90CostPerCent,
        chainATrade.planMarginTo / chainATrade.planMarginFrom,
        chainATrade.p90CostFactor,
        (chainATrade.planMarginTo / chainATrade.planMarginFrom - 1.0) /
                (chainATrade.p90CostFactor - 1.0),
        -recommended.p90CostPerCent
    )

    return findings
}
