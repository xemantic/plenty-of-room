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

package com.xemantic.nano.plentyofroom.anchoring

import com.xemantic.nano.plentyofroom.coupling.CollarTerm
import com.xemantic.nano.plentyofroom.coupling.LoadState
import com.xemantic.nano.plentyofroom.coupling.MultiStateSurrogate
import com.xemantic.nano.plentyofroom.coupling.minimaxStiffnessDistribution
import com.xemantic.nano.plentyofroom.coupling.normalisedStiffnesses
import com.xemantic.nano.plentyofroom.coupling.perPathStiffnessCeiling
import com.xemantic.nano.plentyofroom.coupling.perPathThermalForces
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.DEPARTURE_DIGITS_BY_KEY
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_COUNT
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.OrigamiSheet
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.roundForResult
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
 * `T-136` — is there a **flat 30-root placement**, and does it keep the plan margin?
 *
 * Emits `gpd/results/T-136-two-per-row-placement.json`.
 */

private const val DUPLEXES = 15
private const val FLATNESS_TOLERANCE = 0.10
private const val RIM_STANDOFF = 1.0
private const val C0063_PHASE = 24
private const val TARGET_COUNT = 30
private val MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

/**
 * The precision every argmin here is DECIDED at (`CLAUDE.md`: a decision must be rounded coarser
 * than the number it is taken on, and nine significant digits is not coarse enough). The floor is
 * lowered from `RESULT_ABSOLUTE_FLOOR` because a dishing ratio is **dimensionless** and the default
 * is a claim about pN.
 */
private const val DECISION_DIGITS = 6
private const val DECISION_FLOOR = 1e-12

/** `C-0009`'s one-crossover hinge and `C-0034`'s `A2` duplex-end couple — `C-0069`'s `Q5`. */
private const val HINGE_COUPLE = 13.5294
private const val TIP_COUPLE = 78.2353

/** Descent starts per phase per objective, the first being the greedy placement. */
private const val DESCENT_STARTS = 12

// ---------------------------------------------------------------------------------------------
// the records — prefixed with the task, because study records are package scoped (CLAUDE.md)
// ---------------------------------------------------------------------------------------------

@Serializable
private data class T136BoundRecord(
    val name: String,
    val value: Double,
    val unit: String,
    val settles: String,
    val falsifierFired: Boolean
)

@Serializable
private data class T136CeilingRecord(
    val count: Int,
    val maximumPerRow: Int,
    val phaseBasePairs: Int,
    val latticeCeiling: Double,
    val armAtThisCount: Double,
    val marginAtBestPlacement: Double,
    val c0072Ceiling: Double,
    val c0072Margin: Double,
    val ceilingIsPlacementDependent: Boolean,
    val note: String
)

@Serializable
private data class T136FamilyRecord(
    val phaseBasePairs: Int,
    val crossoverColumns: Int,
    val family: String,
    val objective: String,
    val enumerated: Int,
    val bestOverStroke: Double,
    val medianOverStroke: Double,
    val worstOverStroke: Double,
    val bestKey: String,
    val bestFlatAtTenPercent: Boolean,
    val bestAtDesignStateOverStroke: Double,
    val bestPlanCeiling: Double,
    val bestPlanMargin: Double,
    val freeTileOverStroke: Double,
    val beatsNoCouplingAtAll: Boolean
)

@Serializable
private data class T136ParetoRecord(
    val phaseBasePairs: Int,
    val objective: String,
    val selection: String,
    val dishingOverStroke: Double,
    val planCeiling: Double,
    val planMargin: Double,
    val marginOverRise: Double,
    val flatAtTenPercent: Boolean,
    val key: String
)

@Serializable
private data class T136DescentRecord(
    val phaseBasePairs: Int,
    val crossoverColumns: Int,
    val objective: String,
    val feasible: Boolean,
    val greedyOverStroke: Double,
    val bestOverStroke: Double,
    val bestFlatAtTenPercent: Boolean,
    val bestPlanMargin: Double,
    val evaluations: Int,
    val freeTileOverStroke: Double
)

@Serializable
private data class T136DistributionRecord(
    val placement: String,
    val phaseBasePairs: Int,
    val stations: Int,
    val rule: String,
    val peakRatio: Double,
    val designStateOverStroke: Double,
    val rangeWorstOverStroke: Double,
    val reachableFloorOverStroke: Double,
    val flatAtTenPercent: Boolean,
    val planMargin: Double,
    val peakPathStiffness: Double,
    val peakPathForceAtAcceptableStroke: Double,
    val peakThermalForce: Double,
    val withinPerPathCeiling: Boolean,
    val startsUsed: Int,
    val startsWithinOnePartInAMillion: Int
)

@Serializable
private data class T136FloorRecord(
    val floor: String,
    val sigma: Double,
    val unit: String,
    val againstTheKnifeEdge: Double,
    val againstTheRecommendedMargin: Double,
    val clearedAtThirtyRoots: Boolean,
    val note: String
)

@Serializable
private data class T136CostRecord(
    val placement: String,
    val stations: Int,
    val perPathSecant: Double,
    val perPathForceAtAcceptableStroke: Double,
    val peakSolvedPathForce: Double,
    val peakThermalForce: Double,
    val withinUnzipAllowable: Boolean
)

@Serializable
private data class T136ConvergenceRecord(
    val quantity: String,
    val parameter: String,
    val values: List<Double>,
    val results: List<Double>,
    val departure: Double,
    val note: String
)

@Serializable
private data class T136ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double,
    val strict: Boolean
)

@Serializable
private data class T136PredicateRecord(
    val name: String,
    val statement: String,
    val verdict: String
)

@Serializable
private data class T136Result(
    val task: String,
    val leaf: String,
    val conditions: String,
    val decision: String,
    val bounds: List<T136BoundRecord>,
    val ceilings: List<T136CeilingRecord>,
    val families: List<T136FamilyRecord>,
    val pareto: List<T136ParetoRecord>,
    val distributions: List<T136DistributionRecord>,
    val floors: List<T136FloorRecord>,
    val descents: List<T136DescentRecord>,
    val costs: List<T136CostRecord>,
    val convergence: List<T136ConvergenceRecord>,
    val reproductions: List<T136ReproductionRecord>,
    val predicates: List<T136PredicateRecord>,
    val bestPlacement: List<T136RowRecord>,
    val recommendedPlacement: List<T136RowRecord>,
    val findings: List<String>,
    val parameters: Map<String, Double>
)

@Serializable
private data class T136RowRecord(
    val phaseBasePairs: Int,
    val row: Int,
    val roots: List<Double>,
    val towardPositiveX: List<Boolean>
)

// ---------------------------------------------------------------------------------------------
// the states, read from `C-0022`'s own result file
// ---------------------------------------------------------------------------------------------

private class T136Profile(
    val name: String,
    val concentration: Double,
    val gapHeight: Double,
    val appliedBias: Double,
    val smoothDepth: Double,
    val smoothWidth: Double,
    val rimDepth: Double
) {

    fun field(interiorPressure: Double, lengthY: Double): PressureField = edgeCollarPressure(
        interiorPressure, Gen1Tile.EDGE_X, lengthY,
        listOf(CollarTerm(smoothDepth, smoothWidth), CollarTerm(rimDepth, RIM_STANDOFF))
    )

}

/**
 * `C-0022`'s solved profiles keyed on **`(concentration, gapHeight, appliedBias)`** — the file
 * carries more than one profile per `(concentration, gap)`, one per operating bias (`CLAUDE.md`).
 */
private fun t136Profile(file: File, key: Triple<Double, Double, Double>): T136Profile {
    require(file.exists()) {
        "C-0022's result file is missing: ${file.path}. T-136 reads the SOLVED edge profiles and " +
                "will not substitute assumed ones for them."
    }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            it.getValue("concentration").jsonPrimitive.content.toDouble() == key.first &&
                    it.getValue("gapHeight").jsonPrimitive.content.toDouble() == key.second &&
                    it.getValue("appliedBias").jsonPrimitive.content.toDouble() == key.third
        } ?: error("no C-0022 profile at ${key.first} mM, ${key.second} nm, ${key.third} V")
    fun value(name: String) = record.getValue(name).jsonPrimitive.content.toDouble()
    return T136Profile(
        name = "%.1f mM, %.0f nm, %.3f V".format(key.first, key.second, key.third),
        concentration = key.first,
        gapHeight = key.second,
        appliedBias = key.third,
        smoothDepth = value("taperDepth"),
        smoothWidth = value("taperWidth"),
        rimDepth = value("rimResidualDepth")
    )
}

/** `C-0063`'s winning 34-root placement, read from `T-125`'s result file rather than retyped. */
private fun t136C0063Placement(file: File): UpwardArmPlacement {
    require(file.exists()) {
        "C-0063's result file is missing: ${file.path}. T-136 compares against ITS placement and " +
                "will not substitute a reconstruction for it."
    }
    val rows = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("bestPlacement").jsonArray.map { it.jsonObject }
    return UpwardArmPlacement(
        C0063_PHASE,
        rows.map { row ->
            UpwardArmRow(
                row = row.getValue("row").jsonPrimitive.content.toInt(),
                roots = row.getValue("roots").jsonArray
                    .map { it.jsonPrimitive.content.toDouble() },
                towardPositiveX = row.getValue("towardPositiveX").jsonArray
                    .map { it.jsonPrimitive.content.toBoolean() }
            )
        }.sortedBy { it.row }
    )
}

// ---------------------------------------------------------------------------------------------
// the models
// ---------------------------------------------------------------------------------------------

private fun t136Sheet(): OrigamiSheet =
    origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)

private fun t136Lattice(
    sheet: OrigamiSheet,
    columns: CrossoverLayout,
    subdivisions: Int = 2
): OrigamiGrillage = OrigamiGrillage(
    sheet = sheet,
    lengthX = Gen1Tile.EDGE_X,
    beamCount = DUPLEXES,
    foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
    columns = columns,
    subdivisions = subdivisions,
    supports = emptyList()
)

/** The decision precision, applied at the comparison and not at the serialisation boundary. */
private fun decide(value: Double) = roundForResult(value, DECISION_DIGITS, DECISION_FLOOR)

/** A deterministic pseudo-random choice of one feasible 2-subset per row. */
private fun t136RandomPlacement(
    phase: Int,
    options: List<List<List<Double>>>,
    arm: Double,
    edgeX: Double,
    seed: Long
): UpwardArmPlacement? {
    var state = seed
    fun next(bound: Int): Int {
        state = state * 6364136223846793005L + 1442695040888963407L
        return ((state ushr 33).toInt() and Int.MAX_VALUE) % bound
    }
    val rows = options.mapIndexed { row, choices ->
        if (choices.isEmpty()) return null
        val roots = choices[next(choices.size)]
        UpwardArmRow(row, roots, armDirections(roots, arm, edgeX)!!)
    }
    return UpwardArmPlacement(phase, rows)
}

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

fun main() {
    val started = System.currentTimeMillis()
    val sheet = t136Sheet()
    val edgeX = Gen1Tile.EDGE_X
    val lengthY = DUPLEXES * sheet.interhelicalDistance
    val interiorPressure = Gen1Tile.TARGET_FORCE / (edgeX * lengthY)
    val width = OrigamiDuplex.INTERHELICAL
    val rise = Gen1Tile.RISE_PER_BASE_PAIR

    println("T-136 — reading C-0022's solved loads and C-0063's placement ...")
    val loadFile = ResultInputs.T_3B.file()
    val designProfile = t136Profile(loadFile, Triple(2.0, 10.0, 0.192))
    val heldProfile = t136Profile(loadFile, Triple(2.0, 7.0, 0.192))
    val uniformProfile = T136Profile("uniform load (the falsifier case)", 0.0, 0.0, 0.0, 0.0, 1.0, 0.0)
    val profiles = listOf(designProfile, heldProfile, uniformProfile)
    val loadStates = profiles.map { LoadState(it.name, it.field(interiorPressure, lengthY)) }
    val designState = 0
    val heldState = 1

    val c0063 = t136C0063Placement(ResultInputs.T_125.file())
    require(c0063.count == C0055_ARM_COUNT) {
        "C-0063's placement must carry $C0055_ARM_COUNT roots, carried ${c0063.count}"
    }

    // ------------------------------------------------------------------ the elements
    println("T-136 — the arm C-0017's mandate demands at 34 and at 30 paths ...")
    val armCache = HashMap<Int, Double>()
    fun armAt(count: Int) = armCache.getOrPut(count) {
        elasticaArmForStiffness(
            hingeStiffness = HINGE_COUPLE,
            hingeCount = 1,
            farStiffness = TIP_COUPLE,
            bendingRigidity = Gen1Tile.DUPLEX_BENDING_RIGIDITY,
            count = count,
            targetStiffness = MANDATE,
            workingDisplacement = Gen1Tile.ACCEPTABLE_STROKE
        )
    }

    val armAt34 = armAt(C0055_ARM_COUNT)
    val armAt30 = armAt(TARGET_COUNT)
    println("  arm at 34 paths %.6f nm, at 30 paths %.6f nm".format(armAt34, armAt30))

    val freeStroke = PlateOnFoundation(
        sheet.plate(edgeX, lengthY), Gen1Tile.FOUNDATION_SECANT, emptyList(), basisDegree = 12
    ).solve(uniformPressure(interiorPressure)).meanDeflection

    // ------------------------------------------------------------------ the cheap bounds
    println("T-136 — the cheap bounds, which run before any placement is priced ...")
    val forced = forcedUniformRootsPerRow(TARGET_COUNT, DUPLEXES, 2)
    val lattice24 = upwardRootLattice(C0063_PHASE, edgeX, DUPLEXES)
    val symmetricPhases = centroSymmetricUpwardPhases(edgeX, DUPLEXES)

    val c0072Rows = rowsWithoutInteriorRoots(
        stationRowsOf(c0063, DUPLEXES, sheet.interhelicalDistance), C0055_ARM_COUNT - TARGET_COUNT
    )
    val c0072Ceiling = rootedLengthCeiling(c0072Rows, edgeX, width)
    val c0072Margin = c0072Ceiling - armAt30

    val ceilings = listOf(15 to 1, 22 to 2, TARGET_COUNT to 2, TARGET_COUNT to 3, 31 to 3, 34 to 3, 45 to 3)
        .flatMap { (count, cap) ->
            symmetricPhases.map { phase ->
                val lattice = upwardRootLattice(phase, edgeX, DUPLEXES)
                val bound = maximumPlanCeilingForCount(lattice, count, edgeX, width, cap)
                val arm = armAt(count)
                val reduced = runCatching {
                    rowsWithoutInteriorRoots(
                        stationRowsOf(c0063, DUPLEXES, sheet.interhelicalDistance),
                        C0055_ARM_COUNT - count
                    )
                }.getOrNull()
                val published = reduced?.let { rootedLengthCeiling(it, edgeX, width) } ?: 0.0
                T136CeilingRecord(
                    count = count,
                    maximumPerRow = cap,
                    phaseBasePairs = phase,
                    latticeCeiling = bound ?: 0.0,
                    armAtThisCount = arm,
                    marginAtBestPlacement = (bound ?: 0.0) - arm,
                    c0072Ceiling = published,
                    c0072Margin = if (published > 0.0) published - arm else 0.0,
                    ceilingIsPlacementDependent = published > 0.0 &&
                            abs((bound ?: 0.0) - published) > 1e-6,
                    note = if (bound == null)
                        "the lattice cannot carry $count roots at $cap per row at any length"
                    else "the largest element ANY placement of $count roots at <= $cap per row " +
                            "can keep, against the ceiling C-0072's own plan-rule reduction reaches"
                )
            }
        }

    // The reachable dishing floor over EVERY candidate root of phase 24 — a lower bound on what
    // any 30-root subset can reach with any distribution whatever, and it costs one Cholesky.
    val allSites24 = lattice24.flatMapIndexed { row, xs ->
        xs.map { it to (row - (DUPLEXES - 1) / 2.0) * sheet.interhelicalDistance }
    }
    val host24 = t136Lattice(sheet, CrossoverLayout.atBasePairPhase(C0063_PHASE, sheet, edgeX))
    val bank24 = MultiStateRootBank(host24, allSites24, loadStates)
    val fullSurrogate = bank24.surrogateFor(allSites24.indices.toList())
    val floorDesign = fullSurrogate.reachableDishingFloor(designState) / freeStroke
    val floorHeld = fullSurrogate.reachableDishingFloor(heldState) / freeStroke
    val freeDesign = bank24.freePeakDishing(designState) / freeStroke

    val maximumCeilingAt30 = maximumPlanCeilingForCount(lattice24, TARGET_COUNT, edgeX, width, 2)!!

    val bounds = listOf(
        T136BoundRecord(
            name = "the per-row count 30 roots on 15 rows FORCE at a cap of two",
            value = (forced ?: 0).toDouble(), unit = "roots per row",
            settles = "C-0063's bound 1 read at the other cap: 2 x 15 = 30 exactly, so every row " +
                    "carries two and the two-per-row CONSTRAINT is an IDENTITY — the design space " +
                    "is a product of per-row 2-subsets and nothing else, before any solve",
            falsifierFired = forced != 2
        ),
        T136BoundRecord(
            name = "the largest element ANY 30-root placement can keep, phase 24",
            value = maximumCeilingAt30, unit = "nm",
            settles = "a placement's plan ceiling is a MIN over its rows and the rows are " +
                    "independent, so the maximum over placements is a bisection on a monotone " +
                    "capacity — a proof, not a search; C-0072's own reduction reaches %.4f nm"
                        .format(c0072Ceiling),
            falsifierFired = maximumCeilingAt30 < armAt30
        ),
        T136BoundRecord(
            name = "the reachable dishing floor over EVERY phase-24 upward root, design state",
            value = floorDesign, unit = "of the free-tile stroke",
            settles = "dishing is affine in the attachment forces, so the least-squares residual " +
                    "over the FULL site set is a rigorous lower bound on every 30-root subset of " +
                    "it under every distribution; a floor above 0.10 fires the falsifier before " +
                    "a single placement is enumerated",
            falsifierFired = floorDesign > FLATNESS_TOLERANCE
        ),
        T136BoundRecord(
            name = "the same floor at the compressed end of the design device's range",
            value = floorHeld, unit = "of the free-tile stroke",
            settles = "C-0068's range objective takes the worst of the two ends, so the floor of " +
                    "the range is the larger of the two floors",
            falsifierFired = floorHeld > FLATNESS_TOLERANCE
        ),
        T136BoundRecord(
            name = "C-0072's standing upper bound on a 30-root placement's dishing",
            value = 0.26028, unit = "of the free-tile stroke",
            settles = "its own plan-rule reduction (drop the interior root of every row of three) " +
                    "is a member of the family this task searches, so the search cannot come out " +
                    "worse; the free tile on this phase dishes %.4f".format(freeDesign),
            falsifierFired = false
        )
    )
    bounds.forEach { println("  %-68s %10.4f %s".format(it.name, it.value, it.unit)) }

    // ------------------------------------------------------------------ the search
    class T136Objective(val name: String, val states: List<Int>)

    val objectives = listOf(
        T136Objective("the design state alone — 2 mM, 10 nm, 0.192 V", listOf(designState)),
        T136Objective(
            "the worst over the 2 mM / 10 nm / 0.192 V device's traversed range",
            listOf(designState, heldState)
        )
    )

    class T136Phase(val phase: Int) {
        val sites = upwardRootLattice(phase, edgeX, DUPLEXES)
        val stations = sites.flatMapIndexed { row, xs ->
            xs.map { it to (row - (DUPLEXES - 1) / 2.0) * sheet.interhelicalDistance }
        }
        val columns = CrossoverLayout.atBasePairPhase(phase, sheet, edgeX).positions.size
        val bank = if (phase == C0063_PHASE) bank24 else MultiStateRootBank(
            t136Lattice(sheet, CrossoverLayout.atBasePairPhase(phase, sheet, edgeX)),
            stations, loadStates
        )
        val options: List<List<List<Double>>> =
            sites.map { rowRootOptions(it, 2, armAt30, edgeX, width) }

        fun surrogate(candidate: UpwardArmPlacement) = bank.surrogateFor(
            candidate.stations(DUPLEXES, sheet.interhelicalDistance).map { (x, y) ->
                val index = bank.indexOf(x, y)
                require(index >= 0) { "($x, $y) is not an upward site of phase $phase" }
                index
            }
        )
    }

    val equal = List(TARGET_COUNT) { MANDATE / TARGET_COUNT }
    val families = ArrayList<T136FamilyRecord>()
    val pareto = ArrayList<T136ParetoRecord>()
    val descents = ArrayList<T136DescentRecord>()
    val paretoPlacements = LinkedHashMap<String, Pair<Int, UpwardArmPlacement>>()
    var overallBest: Triple<UpwardArmPlacement, Double, Int>? = null

    // ONE pass over the 32 phases: the exhaustive centro-symmetric family where the congruence
    // admits one, and a descent over the non-symmetric family everywhere. One bank is alive at a
    // time, which is what keeps a 32-phase sweep inside a study's memory budget.
    println("T-136 — one pass over all 32 phases: exhaustive where symmetric, a descent always ...")
    (0 until UPWARD_ROOT_PITCH_BASE_PAIRS).forEach { phase ->
        val sweep = T136Phase(phase)
        val free = objectives.map { objective ->
            objective.states.maxOf { sweep.bank.freePeakDishing(it) } / freeStroke
        }
        val feasible = sweep.options.none { it.isEmpty() }

        // ------------------------------------------------ the exhaustive centro-symmetric family
        if (phase in symmetricPhases && feasible) {
            val values = objectives.map { ArrayList<Double>() }
            val best = arrayOfNulls<Pair<UpwardArmPlacement, Double>>(objectives.size)
            val bestCeiling = arrayOfNulls<Pair<UpwardArmPlacement, Double>>(objectives.size)
            val bestFlatWithMargin = arrayOfNulls<Pair<UpwardArmPlacement, Double>>(objectives.size)
            val bestFlatAtMaxCeiling =
                arrayOfNulls<Pair<UpwardArmPlacement, Double>>(objectives.size)
            // known before the enumeration, and exactly: rows are independent and a placement's
            // ceiling is a MIN over them
            val phaseMaxCeiling = decide(
                maximumPlanCeilingForCount(sweep.sites, TARGET_COUNT, edgeX, width, 2)
                    ?: error("phase $phase carries no 30-root placement")
            )
            centroSymmetricPlacements(
                phase, edgeX, DUPLEXES, armAt30, TARGET_COUNT,
                minimumPerRow = 2, maximumPerRow = 2, width = width
            ).forEach { candidate ->
                val surrogate = sweep.surrogate(candidate)
                val ceiling = decide(
                    placementLengthCeiling(
                        candidate, DUPLEXES, edgeX, width, sheet.interhelicalDistance
                    )
                )
                objectives.forEachIndexed { index, objective ->
                    val value = decide(surrogate.worstDishing(equal, objective.states) / freeStroke)
                    values[index] += value
                    val current = best[index]
                    if (current == null || value < current.second ||
                        (value == current.second && candidate.key < current.first.key)
                    ) best[index] = candidate to value
                    val currentCeiling = bestCeiling[index]
                    if (currentCeiling == null || ceiling > currentCeiling.second ||
                        (ceiling == currentCeiling.second &&
                                candidate.key < currentCeiling.first.key)
                    ) bestCeiling[index] = candidate to ceiling
                    // the flattest placement whose margin clears one base-pair rise — C-0072's own
                    // quantum, and the threshold that makes a margin quotable at all
                    if (ceiling - armAt30 >= rise) {
                        val currentFlat = bestFlatWithMargin[index]
                        if (currentFlat == null || value < currentFlat.second ||
                            (value == currentFlat.second &&
                                    candidate.key < currentFlat.first.key)
                        ) bestFlatWithMargin[index] = candidate to value
                    }
                    // and the design point the two objectives share: the flattest placement that
                    // ALSO keeps every nanometre of plan margin the lattice affords at this count
                    if (ceiling >= phaseMaxCeiling - 1e-6) {
                        val currentMax = bestFlatAtMaxCeiling[index]
                        if (currentMax == null || value < currentMax.second ||
                            (value == currentMax.second &&
                                    candidate.key < currentMax.first.key)
                        ) bestFlatAtMaxCeiling[index] = candidate to value
                    }
                }
            }
            objectives.forEachIndexed { index, objective ->
                val winner = best[index]
                    ?: error("the two-per-row family at phase $phase is empty")
                val sorted = values[index].sorted()
                val atDesign = sweep.surrogate(winner.first)
                    .worstDishing(equal, listOf(designState)) / freeStroke
                val winnerCeiling = placementLengthCeiling(
                    winner.first, DUPLEXES, edgeX, width, sheet.interhelicalDistance
                )
                families += T136FamilyRecord(
                    phaseBasePairs = phase,
                    crossoverColumns = sweep.columns,
                    family = "centro-symmetric, EXACTLY two arms per row, exhaustive",
                    objective = objective.name,
                    enumerated = sorted.size,
                    bestOverStroke = winner.second,
                    medianOverStroke = sorted[sorted.size / 2],
                    worstOverStroke = sorted.last(),
                    bestKey = winner.first.key,
                    bestFlatAtTenPercent = winner.second < FLATNESS_TOLERANCE,
                    bestAtDesignStateOverStroke = atDesign,
                    bestPlanCeiling = winnerCeiling,
                    bestPlanMargin = winnerCeiling - armAt30,
                    freeTileOverStroke = free[index],
                    beatsNoCouplingAtAll = winner.second < free[index]
                )
                if (index == 1) {
                    val current = overallBest
                    if (current == null || winner.second < current.second) {
                        overallBest = Triple(winner.first, winner.second, phase)
                    }
                }
                fun paretoRow(
                    selection: String,
                    entry: Pair<UpwardArmPlacement, Double>?
                ): T136ParetoRecord? {
                    if (entry == null) return null
                    val ceiling = placementLengthCeiling(
                        entry.first, DUPLEXES, edgeX, width, sheet.interhelicalDistance
                    )
                    val dishing = sweep.surrogate(entry.first)
                        .worstDishing(equal, objective.states) / freeStroke
                    if (index == 1) paretoPlacements["phase $phase — $selection"] =
                        phase to entry.first
                    return T136ParetoRecord(
                        phaseBasePairs = phase,
                        objective = objective.name,
                        selection = selection,
                        dishingOverStroke = dishing,
                        planCeiling = ceiling,
                        planMargin = ceiling - armAt30,
                        marginOverRise = (ceiling - armAt30) / rise,
                        flatAtTenPercent = dishing < FLATNESS_TOLERANCE,
                        key = entry.first.key
                    )
                }
                listOfNotNull(
                    paretoRow("the FLATTEST placement, plan margin unconstrained", best[index]),
                    paretoRow(
                        "the LARGEST plan ceiling, flatness unconstrained", bestCeiling[index]
                    ),
                    paretoRow(
                        "the flattest placement whose margin clears one base-pair rise",
                        bestFlatWithMargin[index]
                    ),
                    paretoRow(
                        "the FLATTEST placement AT the largest plan ceiling the lattice affords",
                        bestFlatAtMaxCeiling[index]
                    )
                ).forEach { pareto += it }
                println(
                    ("  phase %2d  EXHAUSTIVE  %-52s %6d candidates  best %6.4f  median %6.4f  " +
                            "margin %6.4f").format(
                        phase, objective.name, sorted.size, winner.second,
                        sorted[sorted.size / 2], winnerCeiling - armAt30
                    )
                )
            }
        }

        // ------------------------------------------------------------------ the descent
        if (!feasible) {
            objectives.forEachIndexed { index, objective ->
                descents += T136DescentRecord(
                    phaseBasePairs = phase,
                    crossoverColumns = sweep.columns,
                    objective = objective.name,
                    feasible = false,
                    greedyOverStroke = 0.0,
                    bestOverStroke = 0.0,
                    bestFlatAtTenPercent = false,
                    bestPlanMargin = 0.0,
                    evaluations = 0,
                    freeTileOverStroke = free[index]
                )
            }
            println("  phase %2d  NO two-per-row placement exists at all".format(phase))
            return@forEach
        }
        val greedy = UpwardArmPlacement(
            phase,
            sweep.options.mapIndexed { row, choices ->
                UpwardArmRow(row, choices.first(), armDirections(choices.first(), armAt30, edgeX)!!)
            }
        )
        val starts = listOf(greedy) + (1 until DESCENT_STARTS).mapNotNull {
            t136RandomPlacement(phase, sweep.options, armAt30, edgeX, 20260817L + 7919L * it)
        }
        objectives.forEachIndexed { index, objective ->
            fun objectiveOf(candidate: UpwardArmPlacement) = decide(
                sweep.surrogate(candidate).worstDishing(equal, objective.states) / freeStroke
            )

            var evaluations = 0
            var winner: Pair<UpwardArmPlacement, Double>? = null
            starts.forEach { start ->
                val descent = descendPlacement(
                    start = start,
                    sites = sweep.sites,
                    arm = armAt30,
                    edgeX = edgeX,
                    width = width,
                    minimumPerRow = 2,
                    maximumPerRow = 2,
                    objective = ::objectiveOf
                )
                evaluations += descent.evaluations
                val current = winner
                if (current == null || descent.objective < current.second ||
                    (descent.objective == current.second &&
                            descent.placement.key < current.first.key)
                ) winner = descent.placement to descent.objective
            }
            val best = winner!!
            val ceiling = placementLengthCeiling(
                best.first, DUPLEXES, edgeX, width, sheet.interhelicalDistance
            )
            descents += T136DescentRecord(
                phaseBasePairs = phase,
                crossoverColumns = sweep.columns,
                objective = objective.name,
                feasible = true,
                greedyOverStroke = objectiveOf(greedy),
                bestOverStroke = best.second,
                bestFlatAtTenPercent = best.second < FLATNESS_TOLERANCE,
                bestPlanMargin = ceiling - armAt30,
                evaluations = evaluations,
                freeTileOverStroke = free[index]
            )
            if (index == 1) {
                val current = overallBest
                if (current == null || best.second < current.second) {
                    overallBest = Triple(best.first, best.second, phase)
                }
            }
        }
        val row = descents.last()
        println(
            "  phase %2d (%d columns)  DESCENT  range best %6.4f  flat %s  margin %6.4f".format(
                phase, row.crossoverColumns, row.bestOverStroke, row.bestFlatAtTenPercent,
                row.bestPlanMargin
            )
        )
    }

    // ------------------------------------------------------------------ the cost of the winner
    val (winnerPlacement, winnerObjective, winnerPhase) = overallBest!!
    println("T-136 — the winner is at phase $winnerPhase, range dishing %.4f".format(winnerObjective))
    val winnerSweep = T136Phase(winnerPhase)
    val winnerSurrogate = winnerSweep.surrogate(winnerPlacement)
    val winnerCeiling = placementLengthCeiling(
        winnerPlacement, DUPLEXES, edgeX, width, sheet.interhelicalDistance
    )

    fun costOf(
        name: String,
        surrogate: MultiStateSurrogate,
        count: Int,
        states: List<Int>
    ): T136CostRecord {
        val stiffnesses = List(count) { MANDATE / count }
        return T136CostRecord(
            placement = name,
            stations = count,
            perPathSecant = MANDATE / count,
            perPathForceAtAcceptableStroke = Gen1Tile.TARGET_FORCE / count,
            peakSolvedPathForce = states.maxOf { state ->
                surrogate.supportForces(stiffnesses, state).maxOf { abs(it) }
            },
            peakThermalForce = perPathThermalForces(stiffnesses).max(),
            withinUnzipAllowable =
                Gen1Tile.TARGET_FORCE / count <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE
        )
    }

    val c0063Indices = c0063.stations(DUPLEXES, sheet.interhelicalDistance).map { (x, y) ->
        bank24.indexOf(x, y)
    }
    require(c0063Indices.all { it >= 0 }) { "C-0063's stations must lie on the phase-24 lattice" }
    val c0063Surrogate = bank24.surrogateFor(c0063Indices)
    val c0072Indices = c0072Rows.flatMap { row -> row.roots.map { bank24.indexOf(it, row.y) } }
    require(c0072Indices.all { it >= 0 }) { "C-0072's reduction must lie on the phase-24 lattice" }
    val c0072Surrogate = bank24.surrogateFor(c0072Indices)

    val costs = listOf(
        costOf(
            "C-0063's 34 roots at phase 24 — the knife-edge design", c0063Surrogate,
            C0055_ARM_COUNT, listOf(designState, heldState)
        ),
        costOf(
            "C-0072's plan-rule 30-root reduction", c0072Surrogate, TARGET_COUNT,
            listOf(designState, heldState)
        ),
        costOf(
            "T-136's swept 30-root placement at phase $winnerPhase", winnerSurrogate,
            TARGET_COUNT, listOf(designState, heldState)
        )
    )

    // ------------------------------------------------- does a DISTRIBUTION recover the flatness?
    // C-0063's headline is that 34 EQUAL springs make the tile flat. At 30 the equal-spring
    // answer above is a negative, and the least-squares floor says nothing about whether that is
    // a property of the STATION SET or of the equal springs — so the minimax is run beside it,
    // at C-0017's unchanged total and under C-0049's per-path ceiling.
    println("T-136 — the 30-parameter minimax on each Pareto placement, at C-0017's total ...")
    val perPathCeiling = perPathStiffnessCeiling(
        Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE
    )

    fun minimaxStarts(count: Int): List<List<Double>> {
        var seed = 20260817L
        fun next(): Double {
            seed = seed * 6364136223846793005L + 1442695040888963407L
            return ((seed ushr 11).toDouble() / (1L shl 53).toDouble()) - 0.5
        }
        return listOf(List(count) { MANDATE / count }) + (1 until 12).map {
            normalisedStiffnesses(List(count) { kotlin.math.exp(0.35 * 2.0 * next()) }, MANDATE)
        }
    }

    val distributions = ArrayList<T136DistributionRecord>()

    fun distributionRows(
        name: String,
        phase: Int,
        surrogate: MultiStateSurrogate,
        count: Int,
        margin: Double
    ) {
        val equalHere = List(count) { MANDATE / count }
        val states = listOf(designState, heldState)
        val floor = states.maxOf { surrogate.reachableDishingFloor(it) } / freeStroke
        val equalRange = surrogate.worstDishing(equalHere, states) / freeStroke
        distributions += T136DistributionRecord(
            placement = name,
            phaseBasePairs = phase,
            stations = count,
            rule = "uniform — %d EQUAL springs".format(count),
            peakRatio = 1.0,
            designStateOverStroke =
                surrogate.worstDishing(equalHere, listOf(designState)) / freeStroke,
            rangeWorstOverStroke = equalRange,
            reachableFloorOverStroke = floor,
            flatAtTenPercent = equalRange < FLATNESS_TOLERANCE,
            planMargin = margin,
            peakPathStiffness = MANDATE / count,
            peakPathForceAtAcceptableStroke = MANDATE / count * Gen1Tile.ACCEPTABLE_STROKE,
            peakThermalForce = perPathThermalForces(equalHere).max(),
            withinPerPathCeiling = MANDATE / count <= perPathCeiling,
            startsUsed = 0,
            startsWithinOnePartInAMillion = 0
        )
        val optimum = minimaxStiffnessDistribution(
            surrogate = surrogate,
            states = states,
            totalStiffness = MANDATE,
            starts = minimaxStarts(count),
            ceiling = perPathCeiling
        )
        val peak = optimum.stiffnesses.max()
        distributions += T136DistributionRecord(
            placement = name,
            phaseBasePairs = phase,
            stations = count,
            rule = "the %d-parameter minimax over the design device's range".format(count),
            peakRatio = peak / (MANDATE / count),
            designStateOverStroke =
                surrogate.worstDishing(optimum.stiffnesses, listOf(designState)) / freeStroke,
            rangeWorstOverStroke = optimum.worstDishing / freeStroke,
            reachableFloorOverStroke = floor,
            flatAtTenPercent = optimum.worstDishing / freeStroke < FLATNESS_TOLERANCE,
            planMargin = margin,
            peakPathStiffness = peak,
            peakPathForceAtAcceptableStroke = peak * Gen1Tile.ACCEPTABLE_STROKE,
            peakThermalForce = perPathThermalForces(optimum.stiffnesses).max(),
            withinPerPathCeiling = peak <= perPathCeiling * (1.0 + 1e-9),
            startsUsed = optimum.startsUsed,
            startsWithinOnePartInAMillion = optimum.startsWithinOnePartInAMillion
        )
        println(
            "  %-58s equal %6.4f  minimax %6.4f  floor %6.4f  peak ratio %5.2f".format(
                name, equalRange, optimum.worstDishing / freeStroke, floor,
                peak / (MANDATE / count)
            )
        )
    }

    distributionRows(
        "C-0063's 34 roots at phase 24 — the knife-edge design", C0063_PHASE, c0063Surrogate,
        C0055_ARM_COUNT, rootedLengthCeiling(
            stationRowsOf(c0063, DUPLEXES, sheet.interhelicalDistance), edgeX, width
        ) - armAt34
    )
    distributionRows(
        "C-0072's plan-rule 30-root reduction", C0063_PHASE, c0072Surrogate, TARGET_COUNT,
        c0072Margin
    )
    paretoPlacements.forEach { (label, entry) ->
        val (phase, placement) = entry
        val sweep = T136Phase(phase)
        distributionRows(
            label, phase, sweep.surrogate(placement), TARGET_COUNT,
            placementLengthCeiling(
                placement, DUPLEXES, edgeX, width, sheet.interhelicalDistance
            ) - armAt30
        )
    }

    // ------------------------------------------------ the recommendation, chosen from the above
    // Among the placements priced under a distribution, the one that keeps every nanometre of
    // plan margin the lattice affords at 30 roots AND is flattest under C-0017's unchanged total.
    val recommendedRow = distributions
        .filter { it.rule.contains("minimax") && it.stations == TARGET_COUNT && it.flatAtTenPercent }
        .maxWithOrNull(
            compareBy<T136DistributionRecord> { decide(it.planMargin) }
                .thenBy { -decide(it.rangeWorstOverStroke) }
                .thenBy { it.placement }
        )
    val recommended = recommendedRow?.let { paretoPlacements[it.placement] }
    println(
        "T-136 — the recommendation: " + (recommendedRow?.let {
            "phase %d, margin %.4f nm, minimax %.4f at peak ratio %.2f".format(
                it.phaseBasePairs, it.planMargin, it.rangeWorstOverStroke, it.peakRatio
            )
        } ?: "none — no 30-root placement is flat even under a distribution")
    )

    // ---------------------------------- C-0072's four floors, re-read at the 30-path arm
    // C-0072 built them on the 34-path arm and on a 0.0256 nm margin. Two of the four scale with
    // the arm, so they are re-evaluated here rather than carried across.
    println("T-136 — C-0072's four floors, re-evaluated at the 30-path arm ...")
    val knifeEdge = 0.0256098233
    val recommendedMargin = recommendedRow?.planMargin ?: (maximumCeilingAt30 - armAt30)
    val armBasePairs30 = basePairsNearest(armAt30, rise)
    val axialSigma = quadrature(
        axialFluctuation(UPWARD_ROOT_PITCH_BASE_PAIRS * rise),
        axialFluctuation(armBasePairs30 * rise)
    )
    val tipSigma = cantileverTipFluctuation(armAt30, Gen1Tile.DUPLEX_BENDING_RIGIDITY)
    fun floorRow(name: String, sigma: Double, note: String) = T136FloorRecord(
        floor = name,
        sigma = sigma,
        unit = "nm",
        againstTheKnifeEdge = sigma / knifeEdge,
        againstTheRecommendedMargin = sigma / recommendedMargin,
        clearedAtThirtyRoots = recommendedMargin >= sigma,
        note = note
    )
    val floors = listOf(
        floorRow(
            "the base-pair rise — the design quantum", rise,
            "C-0072's sharpest floor: below it a margin cannot be specified in the design language"
        ),
        floorRow(
            "the two measured SAXS interhelical means, 2.73 - 2.69 nm",
            SQUARE_LATTICE_INTERHELICAL - OrigamiDuplex.INTERHELICAL,
            "the disagreement between two measurements of the same material"
        ),
        floorRow(
            "the thermal axial breathing of the two segments the margin differences", axialSigma,
            ("the host's 32 bp pitch and the element's %d bp length in quadrature, from the " +
                    "measured stretch modulus — re-read at the 30-path arm").format(armBasePairs30)
        ),
        floorRow(
            "the arm tip's own bending at a PERFECTLY RIGID root", tipSigma,
            "the floor of the transverse channel: no joint stiffening removes it. It SCALES with " +
                    "the arm as L^(3/2), so it is the one floor a shorter arm moves"
        ),
        floorRow(
            "Fischer et al.'s fitted single-layer lattice-constant width, w_a = 2.5 A", 0.25,
            "the measurement C-0072's survey found in a supplementary table nobody quotes — the " +
                    "only fabrication number in the list, and 9.1 % of a 27.41 A lattice constant"
        )
    )
    floors.forEach {
        println(
            "  %-62s sigma %7.5f nm  x knife edge %7.2f  x recommended margin %6.3f  cleared %s"
                .format(it.floor, it.sigma, it.againstTheKnifeEdge,
                    it.againstTheRecommendedMargin, it.clearedAtThirtyRoots)
        )
    }

    // ------------------------------------------------------------------ convergence
    println("T-136 — convergence: subdivisions, the sampling grid and the ceiling bisection ...")
    val winnerStations = winnerPlacement.stations(DUPLEXES, sheet.interhelicalDistance)
    fun reading(subdivisions: Int, samples: Int): Double {
        val bank = MultiStateRootBank(
            t136Lattice(
                sheet, CrossoverLayout.atBasePairPhase(winnerPhase, sheet, edgeX), subdivisions
            ),
            winnerStations, loadStates, samples
        )
        return bank.surrogateFor(winnerStations.indices.toList())
            .worstDishing(equal, listOf(designState, heldState)) / freeStroke
    }

    val subdivisionResults = listOf(1, 2, 4).map { reading(it, 81) }
    val samplingResults = listOf(41, 81, 161).map { reading(2, it) }
    val ceilingResults = listOf(1e-6, 1e-9, 1e-12).map {
        maximumPlanCeilingForCount(lattice24, TARGET_COUNT, edgeX, width, 2, it)!!
    }
    val armResults = listOf(200, 400, 800).map { steps ->
        elasticaArmForStiffness(
            hingeStiffness = HINGE_COUPLE, hingeCount = 1, farStiffness = TIP_COUPLE,
            bendingRigidity = Gen1Tile.DUPLEX_BENDING_RIGIDITY, count = TARGET_COUNT,
            targetStiffness = MANDATE, workingDisplacement = Gen1Tile.ACCEPTABLE_STROKE,
            steps = steps
        )
    }
    val convergence = listOf(
        T136ConvergenceRecord(
            "the winner's range dishing over the free stroke", "NESTED subdivisions 1 c 2 c 4",
            listOf(1.0, 2.0, 4.0), subdivisionResults,
            abs(subdivisionResults[2] - subdivisionResults[1]) / subdivisionResults[2],
            "nested only, per CLAUDE.md — a subdivision of 3 moves a station off a node"
        ),
        T136ConvergenceRecord(
            "the winner's range dishing over the free stroke", "dishing sample grid 41 / 81 / 161",
            listOf(41.0, 81.0, 161.0), samplingResults,
            abs(samplingResults[2] - samplingResults[1]) / samplingResults[2],
            "81 is the grid every published dishing in this programme is read on"
        ),
        T136ConvergenceRecord(
            "the maximum plan ceiling at 30 roots", "bisection resolution 1e-6 / 1e-9 / 1e-12",
            listOf(1e-6, 1e-9, 1e-12), ceilingResults,
            abs(ceilingResults[2] - ceilingResults[1]),
            "an ABSOLUTE departure in nm, because the quantity is a length and the bisection " +
                    "exits on its own bracket width"
        ),
        T136ConvergenceRecord(
            "the 30-path arm", "RK4 steps 200 / 400 / 800",
            listOf(200.0, 400.0, 800.0), armResults,
            abs(armResults[2] - armResults[1]) / armResults[2],
            "C-0039's exact elastica, re-run rather than tabulated"
        )
    )

    // ------------------------------------------------------------------ reproductions
    println("T-136 — reproducing what upstream published on these stations ...")
    val c0063Single = c0063Surrogate.worstDishing(
        List(C0055_ARM_COUNT) { MANDATE / C0055_ARM_COUNT }, listOf(designState)
    ) / freeStroke
    val c0063Range = c0063Surrogate.worstDishing(
        List(C0055_ARM_COUNT) { MANDATE / C0055_ARM_COUNT }, listOf(designState, heldState)
    ) / freeStroke
    val c0072Single = c0072Surrogate.worstDishing(equal, listOf(designState)) / freeStroke

    fun reproduction(
        source: String, quantity: String, published: Double, reproduced: Double, strict: Boolean
    ) = T136ReproductionRecord(
        source, quantity, published, reproduced,
        if (published == 0.0) abs(reproduced) else abs(reproduced - published) / abs(published),
        strict
    )

    val reproductions = listOf(
        reproduction("C-0063", "dishing/stroke, 34 roots, equal springs, design state", 0.0706145537, c0063Single, false),
        reproduction("C-0068", "dishing/stroke, 34 roots, over the 2 mM device's range", 0.0789, c0063Range, false),
        reproduction("C-0072", "dishing/stroke, its plan-rule 30 roots, design state", 0.26028, c0072Single, false),
        reproduction("C-0072", "the ceiling of its own 30-root reduction [nm]", 9.12, c0072Ceiling, false),
        reproduction("C-0072", "the margin of its own 30-root reduction [nm]", 1.3495, c0072Margin, false),
        reproduction("C-0072", "the 30-path arm [nm]", 7.77049, armAt30, false),
        reproduction("C-0069", "the 34-path arm [nm]", 8.16439083, armAt34, false),
        reproduction("C-0069", "the row-of-three length ceiling [nm]", 8.19, rowOfThreeLengthCeiling(10.88, width), false),
        reproduction("C-0055", "the upward root pitch [nm]", 10.88, UPWARD_ROOT_PITCH_BASE_PAIRS * rise, true),
        reproduction("C-0055", "roots in C-0063's placement", 34.0, c0063.count.toDouble(), true),
        reproduction("C-0026", "the free-tile stroke [nm]", 4.90731102, freeStroke, false),
        reproduction("C-0022", "dishing/stroke, free uncoupled tile, design state, phase 24", 0.3079, freeDesign, false),
        reproduction("C-0063", "centro-symmetric phases", 2.0, symmetricPhases.size.toDouble(), true)
    )

    // ------------------------------------------------------------------ the predicates
    val rangeFamilies = families.filter { it.objective == objectives[1].name }
    val designFamilies = families.filter { it.objective == objectives[0].name }
    val rangeDescents = descents.filter { it.objective == objectives[1].name && it.feasible }
    val flatDescents = rangeDescents.filter { it.bestFlatAtTenPercent }
    val anyFlat = rangeFamilies.any { it.bestFlatAtTenPercent } || flatDescents.isNotEmpty()
    val winnerAtDesign =
        winnerSurrogate.worstDishing(equal, listOf(designState)) / freeStroke
    val winnerMargin = winnerCeiling - armAt30

    val predicates = listOf(
        T136PredicateRecord(
            "P1 — is there a FLAT 30-root placement?",
            "the peak dishing of 30 EQUAL springs on the best two-per-row placement, at the " +
                    "design state and over the whole range the placed 2 mM device traverses, " +
                    "against T-5b's 0.10 of the free-tile stroke",
            ("the best of the exhaustive centro-symmetric family reaches %.4f at the design " +
                    "state and %.4f over the range; the best of the 32-phase descent reaches " +
                    "%.4f over the range — %s T-5b's 0.10, against C-0072's plan-rule %.4f and " +
                    "C-0063's 34-root %.4f").format(
                designFamilies.minOf { it.bestOverStroke },
                rangeFamilies.minOf { it.bestOverStroke },
                rangeDescents.minOf { it.bestOverStroke },
                if (anyFlat) "INSIDE" else "OUTSIDE", c0072Single, c0063Range
            )
        ),
        T136PredicateRecord(
            "P2 — does it keep the plan margin?",
            "the re-sized 30-path arm's margin `ceiling − arm` on the placement P1 returns, " +
                    "against C-0072's 1.3495 nm and against the 0.34 nm base-pair rise",
            ("the winner's own ceiling is %.4f nm and its margin %.4f nm (%.2f rises); the " +
                    "largest any 30-root placement can keep is %.4f nm, i.e. a margin of %.4f " +
                    "nm — C-0072's reduction reaches %.4f nm and %.4f nm").format(
                winnerCeiling, winnerMargin, winnerMargin / rise, maximumCeilingAt30,
                maximumCeilingAt30 - armAt30, c0072Ceiling, c0072Margin
            )
        ),
        T136PredicateRecord(
            "P3 — are the two satisfiable TOGETHER?",
            "the Pareto front of the exhaustive family: the flattest placement, the " +
                    "largest-ceiling placement, and the flattest placement whose margin clears " +
                    "one base-pair rise",
            (pareto.filter { it.objective == objectives[1].name }.joinToString("; ") {
                "phase %d, %s: equal springs %.4f, margin %.4f nm".format(
                    it.phaseBasePairs, it.selection, it.dishingOverStroke, it.planMargin
                )
            } + " — and under a distribution: " +
                    distributions.filter {
                        it.rule.contains("minimax") && it.stations == TARGET_COUNT
                    }.joinToString("; ") {
                        "%s: %.4f at margin %.4f nm".format(
                            it.placement, it.rangeWorstOverStroke, it.planMargin
                        )
                    })
        ),
        T136PredicateRecord(
            "P4 — does a DISTRIBUTION recover what equal springs lose?",
            "the 30-parameter minimax over the same range at C-0017's unchanged total and under " +
                    "C-0049's per-path ceiling, against the least-squares floor of the same " +
                    "station set",
            distributions.filter { it.rule.contains("minimax") }.joinToString("; ") {
                "%s: %.4f (floor %.4f, peak ratio %.2f, %s)".format(
                    it.placement, it.rangeWorstOverStroke, it.reachableFloorOverStroke,
                    it.peakRatio, if (it.flatAtTenPercent) "FLAT" else "not flat"
                )
            }
        ),
        T136PredicateRecord(
            "the recommendation",
            "the 30-root placement that keeps every nanometre of plan margin the lattice affords " +
                    "AND is flattest under C-0017's unchanged total",
            recommendedRow?.let {
                ("%s — plan margin %.4f nm (%.2f base-pair rises, %.1fx C-0069's 0.02561 nm " +
                        "knife edge and %.2fx C-0072's 1.3495 nm), dishing %.4f over the design " +
                        "device's whole range at a peak stiffness ratio of %.2f and a peak path " +
                        "force of %.3f pN against the 10 pN unzip allowable; with EQUAL springs " +
                        "the same placement dishes %.4f and is NOT flat").format(
                    it.placement, it.planMargin, it.planMargin / rise,
                    it.planMargin / 0.0256098233, it.planMargin / c0072Margin,
                    it.rangeWorstOverStroke, it.peakRatio, it.peakPathForceAtAcceptableStroke,
                    distributions.first { row ->
                        row.placement == it.placement && row.rule.startsWith("uniform")
                    }.rangeWorstOverStroke
                )
            } ?: "none exists"
        ),
        T136PredicateRecord(
            "the declared falsifier",
            "no two-per-row placement clears 0.10, at either objective and at any phase, with " +
                    "EQUAL springs",
            if (anyFlat)
                ("DID NOT FIRE — %d of %d phases reach a flat range placement in the descent " +
                        "and %d of %d symmetric families do").format(
                    flatDescents.size, rangeDescents.size,
                    rangeFamilies.count { it.bestFlatAtTenPercent }, rangeFamilies.size
                )
            else ("FIRED for equal springs — no two-per-row placement of any phase clears 0.10 " +
                    "with them, the best being %.4f; %s").format(
                minOf(
                    rangeFamilies.minOf { it.bestOverStroke },
                    rangeDescents.minOf { it.bestOverStroke }
                ),
                if (distributions.any { it.stations == TARGET_COUNT && it.flatAtTenPercent })
                    "a DISTRIBUTION recovers it, so the trade is between an equal-spring design " +
                            "and a quotable margin, not between flatness and margin"
                else "and no distribution recovers it either, so the branch must choose between " +
                        "a quotable margin and a flat tile"
            )
        )
    )

    // ------------------------------------------------------------------ the findings
    val findings = listOf(
        ("The two-per-row constraint at 30 roots on 15 duplexes is an IDENTITY and not a " +
                "constraint: 2 x 15 = 30 exactly, so every row carries two and the whole design " +
                "space is a product of per-row 2-subsets. That is C-0063's bound 1 read at the " +
                "other cap, it costs one line of arithmetic, and it shrinks the centro-symmetric " +
                "family from C-0063's 361 584 at 34 roots to %d at 30 — small enough to " +
                "enumerate EXHAUSTIVELY at both phases the congruence admits.").format(
            families.filter { it.objective == objectives[0].name }.sumOf { it.enumerated }
        ),
        ("A plan ceiling is a property of a PLACEMENT, not of a count. C-0072 reports 9.12 nm " +
                "at 30 roots and 20.00 nm at 15; the largest any placement of those counts can " +
                "keep on the same lattice is %.4f nm and %.4f nm. The difference is a bisection " +
                "on a monotone capacity — rows are independent and a placement's ceiling is a " +
                "MIN over them — so it is a proof and not a search, and it costs no solve at all."
                ).format(
            maximumCeilingAt30,
            maximumPlanCeilingForCount(lattice24, 15, edgeX, width, 1) ?: 0.0
        ),
        ("The flatness answer for EQUAL springs: %s. Over the range the placed 2 mM / 10 nm / " +
                "0.192 V device traverses, the best two-per-row placement dishes %.4f " +
                "(exhaustive centro-symmetric) and %.4f (32-phase descent) against T-5b's 0.10, " +
                "C-0072's plan-rule %.4f, C-0063's 34-root %.4f and a free tile at %.4f.").format(
            if (anyFlat) "a flat 30-root placement EXISTS"
            else "NO 30-root placement is flat with equal springs, at any of the 32 phases",
            rangeFamilies.minOf { it.bestOverStroke },
            rangeDescents.minOf { it.bestOverStroke },
            c0072Single, c0063Range, freeDesign
        ),
        ("The winner sits at phase %d, dishes %.4f at the design state and %.4f over the range, " +
                "and carries a plan margin of %.4f nm — %.2f base-pair rises against C-0072's " +
                "%.2f. Per path it carries %.3f pN of the 100 pN duty against the 10 pN unzip " +
                "allowable, %.3f pN solved and %.3f pN of C-0014 thermal force.").format(
            winnerPhase, winnerAtDesign, winnerObjective, winnerMargin, winnerMargin / rise,
            c0072Margin / rise, Gen1Tile.TARGET_FORCE / TARGET_COUNT,
            costs[2].peakSolvedPathForce, costs[2].peakThermalForce
        ),
        ("The cheap bound did not decide it: the least-squares floor over EVERY phase-24 upward " +
                "root is %.4f at the design state and %.4f at the compressed end, %.1fx below " +
                "T-5b's convention, so the falsifier could not fire before the enumeration and " +
                "the enumeration was necessary.").format(
            floorDesign, floorHeld, FLATNESS_TOLERANCE / maxOf(floorDesign, floorHeld)
        ),
        ("And the negative belongs to the EQUAL SPRINGS, not to the station set: the " +
                "30-parameter minimax at C-0017's unchanged total takes the same placements to " +
                "%s. %s C-0063's own advantage was that 34 roots need no distribution at all; " +
                "at 30 that advantage is what is spent, which is CH-0080's finding on a new " +
                "axis — the equal-spring advantage belongs to a COUNT as well as to a layer."
                ).format(
            distributions.filter { it.rule.contains("minimax") && it.stations == TARGET_COUNT }
                .joinToString(", ") { "%.4f".format(it.rangeWorstOverStroke) },
            if (distributions.any {
                    it.stations == TARGET_COUNT && it.rule.contains("minimax") &&
                            it.flatAtTenPercent
                }
            ) "So a flat 30-root design EXISTS, and it needs a distribution."
            else "So no 30-root design is flat at all, with or without a distribution."
        ),
        ("And the margin becomes QUOTABLE: all five of C-0072's floors — four of them needing no " +
                "fabrication measurement and the fifth being the only one there is — are cleared " +
                "at the recommended placement, %s, the weakest by %.2fx. Two of them scale with " +
                "the arm and are re-evaluated here rather than carried across: the arm tip's own " +
                "bending at a rigid root falls from 1.8074 to %.4f nm as the arm shortens from " +
                "8.164 to %.4f nm, which is the whole of that clearance.").format(
            floors.joinToString(", ") {
                "%s at %.4f nm".format(it.floor.take(34), it.sigma)
            },
            floors.minOf { recommendedMargin / it.sigma }, tipSigma, armAt30
        )
    )

    val result = T136Result(
        task = "T-136 — is there a FLAT 30-root placement?",
        leaf = "A8.2",
        conditions = ("T = 300 K, k_BT = 4.141947 pN nm; aqueous 2 mM MgCl2; 40.0 x %.2f nm " +
                "single-layer square-lattice Rothemund sheet, %d duplexes at 2.69 nm, 0.34 nm " +
                "rise; C-0022's SOLVED edge profiles at 10 nm and 7 nm, both at 0.192 V; " +
                "C-0017's %.4f pN/nm mandate as a SUM over %d equal paths; C-0039's exact " +
                "elastica arm %.5f nm on a one-crossover root and C-0034's A2 tip; C-0053's " +
                "footprint convention at d = %.2f nm; free-tile stroke %.5f nm").format(
            lengthY, DUPLEXES, MANDATE, TARGET_COUNT, armAt30, width, freeStroke
        ),
        decision = ("the best two-per-row 30-root placement dishes %.4f of the free-tile stroke " +
                "over the design device's whole range and %.4f at the design state, with a plan " +
                "margin of %.4f nm — against T-5b's 0.10 and C-0072's 0.2603 / 1.3495 nm").format(
            winnerObjective, winnerAtDesign, winnerMargin
        ),
        bounds = bounds,
        ceilings = ceilings,
        families = families,
        pareto = pareto,
        distributions = distributions,
        floors = floors,
        descents = descents,
        costs = costs,
        convergence = convergence,
        reproductions = reproductions,
        predicates = predicates,
        bestPlacement = winnerPlacement.rows.map {
            T136RowRecord(winnerPhase, it.row, it.roots, it.towardPositiveX)
        },
        recommendedPlacement = recommended?.let { (phase, placement) ->
            placement.rows.map { T136RowRecord(phase, it.row, it.roots, it.towardPositiveX) }
        } ?: emptyList(),
        findings = findings,
        parameters = mapOf(
            "targetCount" to TARGET_COUNT.toDouble(),
            "duplexes" to DUPLEXES.toDouble(),
            "edgeX" to edgeX,
            "lengthY" to lengthY,
            "mandate" to MANDATE,
            "armAt30" to armAt30,
            "armAt34" to armAt34,
            "exclusionWidth" to width,
            "risePerBasePair" to rise,
            "freeStroke" to freeStroke,
            "flatnessTolerance" to FLATNESS_TOLERANCE,
            "maximumPlanCeilingAt30" to maximumCeilingAt30,
            "c0072Ceiling" to c0072Ceiling,
            "c0072Margin" to c0072Margin,
            "winnerPhase" to winnerPhase.toDouble(),
            "winnerCeiling" to winnerCeiling,
            "winnerMargin" to winnerMargin,
            "winnerRangeOverStroke" to winnerObjective,
            "winnerDesignOverStroke" to winnerAtDesign,
            "reachableFloorDesign" to floorDesign,
            "reachableFloorHeld" to floorHeld,
            "freeTileDesignOverStroke" to freeDesign,
            "descentStarts" to DESCENT_STARTS.toDouble(),
            "decisionDigits" to DECISION_DIGITS.toDouble(),
            "recommendedPhase" to (recommended?.first?.toDouble() ?: -1.0),
            "recommendedCeiling" to (recommendedRow?.let { it.planMargin + armAt30 } ?: 0.0),
            "recommendedMargin" to (recommendedRow?.planMargin ?: 0.0),
            "recommendedEqualSpringOverStroke" to (recommendedRow?.let { row ->
                distributions.first {
                    it.placement == row.placement && it.rule.startsWith("uniform")
                }.rangeWorstOverStroke
            } ?: 0.0),
            "recommendedMinimaxOverStroke" to (recommendedRow?.rangeWorstOverStroke ?: 0.0),
            "recommendedPeakRatio" to (recommendedRow?.peakRatio ?: 0.0),
            "recommendedPeakPathForce" to (recommendedRow?.peakPathForceAtAcceptableStroke ?: 0.0)
        )
    )

    val output = File("gpd/results/T-136-two-per-row-placement.json")
    output.parentFile.mkdirs()
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            // `T-209`/`C-0129`: a DEPARTURE is a record type, not a file. `C-0093` cured the
            // trap on its own convergence axis and `C-0101` in the reproduction records of the
            // files it happened to be re-emitting; `C-0127` then found this file still carrying
            // `reproductions[2].departure` at nine significant digits. The rule now travels as
            // `DEPARTURE_DIGITS_BY_KEY` rather than as a `2` at an emission site.
            (json.encodeToJsonElement(result).roundedForResult(
                digitsByKey = DEPARTURE_DIGITS_BY_KEY
            ).withEmissionHeader(LatticeTag.SQUARE, null) as JsonObject)
        )
    )

    println()
    println("predicates")
    result.predicates.forEach { println("  ${it.name}: ${it.verdict}"); println() }
    println("upstream reproductions")
    result.reproductions.forEach {
        println(
            "  %-8s %-58s %12.6g vs %12.6g  %8.2e %s".format(
                it.source, it.quantity, it.published, it.reproduced, it.departure,
                if (it.strict) "" else "(non-strict)"
            )
        )
    }
    println()
    result.findings.forEach { println("  * $it"); println() }
    println("written to ${output.path} in ${(System.currentTimeMillis() - started) / 1000} s")
}
