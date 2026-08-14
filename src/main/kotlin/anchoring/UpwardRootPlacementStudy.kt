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
import com.xemantic.nano.plentyofroom.coupling.InfluenceSurrogate
import com.xemantic.nano.plentyofroom.coupling.admissibleStiffnessRatio
import com.xemantic.nano.plentyofroom.coupling.attachmentGrid
import com.xemantic.nano.plentyofroom.coupling.cappedStiffnesses
import com.xemantic.nano.plentyofroom.coupling.couplingSupports
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.coupling.normalisedStiffnesses
import com.xemantic.nano.plentyofroom.coupling.optimiseStiffnessDistribution
import com.xemantic.nano.plentyofroom.coupling.perPathStiffnessCeiling
import com.xemantic.nano.plentyofroom.coupling.perPathThermalForces
import com.xemantic.nano.plentyofroom.coupling.rimStiffenedWeights
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_COUNT
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_LENGTH
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.OrigamiSheet
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PointSupport
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.StackedArm
import com.xemantic.nano.plentyofroom.structure.StackedArmGrillage
import com.xemantic.nano.plentyofroom.structure.origamiSheet
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

/**
 * `T-125` — the row phases of `C-0055`'s upward arm array, swept under `C-0022`'s **solved**
 * load, against `T-5b`'s 10 % convention and `C-0058`'s 0.0753.
 *
 * Emits `gpd/results/T-125-upward-root-placement.json`.
 */

private const val DUPLEXES = 15
private const val FLATNESS_TOLERANCE = 0.10
private const val RIM_STANDOFF = 1.0
private const val C0058_COLLAR_WIDTH = 6.70
private val MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

// ---------------------------------------------------------------------------------------------
// the records — prefixed with the task, because study records are package scoped (CLAUDE.md)
// ---------------------------------------------------------------------------------------------

@Serializable
private data class T125BoundRecord(
    val name: String,
    val value: Double,
    val unit: String,
    val settles: String
)

@Serializable
private data class T125PhaseRecord(
    val phaseBasePairs: Int,
    val hostColumns: Int,
    val interfaceCrossovers: Int,
    val centroSymmetricCapable: Boolean,
    val freeDishingOverStroke: Double,
    val greedyDishingOverStroke: Double,
    val bestDishingOverStroke: Double,
    val bestCentroidX: Double,
    val bestCentroSymmetric: Boolean,
    val bestBeatsNoCoupling: Boolean,
    val evaluations: Int,
    val key: String
)

@Serializable
private data class T125SymmetricFamilyRecord(
    val phaseBasePairs: Int,
    val enumerated: Int,
    val bestDishingOverStroke: Double,
    val worstDishingOverStroke: Double,
    val medianDishingOverStroke: Double,
    val bestKey: String,
    val exhaustive: Boolean
)

@Serializable
private data class T125RowRecord(
    val phaseBasePairs: Int,
    val row: Int,
    val y: Double,
    val sites: List<Double>,
    val roots: List<Double>,
    val towardPositiveX: List<Boolean>
)

@Serializable
private data class T125FlatnessRecord(
    val placement: String,
    val phaseBasePairs: Int,
    val host: String,
    val stations: Int,
    val dishingOverStroke: Double,
    val flatAtTenPercent: Boolean,
    val beatsNoCoupling: Boolean,
    val peakPathForce: Double,
    val peakCrossoverForce: Double,
    val peakDuplexShear: Double
)

@Serializable
private data class T125DistributionRecord(
    val placement: String,
    val stations: Int,
    val rule: String,
    val ratio: Double,
    val dishingOverStroke: Double,
    val flatAtTenPercent: Boolean,
    val peakPathForce: Double,
    val peakSolvedPathForce: Double,
    val peakThermalForce: Double,
    val admissibleRatio: Double,
    val withinPerPathAllowable: Boolean
)

@Serializable
private data class T125LoadCaseRecord(
    val loadCase: String,
    val freeDishing: Double,
    val bestDishingOverStroke: Double,
    val bestKey: String,
    val note: String
)

@Serializable
private data class T125ConvergenceRecord(
    val quantity: String,
    val parameter: String,
    val values: List<Double>,
    val results: List<Double>,
    val departure: Double,
    val note: String
)

@Serializable
private data class T125ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double,
    val strict: Boolean
)

@Serializable
private data class T125PredicateRecord(
    val name: String,
    val statement: String,
    val verdict: String
)

@Serializable
private data class T125Result(
    val task: String,
    val leaf: String,
    val conditions: String,
    val decision: String,
    val bounds: List<T125BoundRecord>,
    val phases: List<T125PhaseRecord>,
    val symmetricFamily: List<T125SymmetricFamilyRecord>,
    val bestPlacement: List<T125RowRecord>,
    val flatness: List<T125FlatnessRecord>,
    val distributions: List<T125DistributionRecord>,
    val loadCases: List<T125LoadCaseRecord>,
    val convergence: List<T125ConvergenceRecord>,
    val reproductions: List<T125ReproductionRecord>,
    val predicates: List<T125PredicateRecord>,
    val findings: List<String>,
    val parameters: Map<String, Double>
)

// ---------------------------------------------------------------------------------------------
// the inputs, read from the claims that own them
// ---------------------------------------------------------------------------------------------

private fun sheet(): OrigamiSheet = origamiSheet(
    Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP
)

private fun lattice(
    sheet: OrigamiSheet,
    columns: CrossoverLayout,
    supports: List<PointSupport> = emptyList(),
    subdivisions: Int = 2
) = OrigamiGrillage(
    sheet = sheet,
    lengthX = Gen1Tile.EDGE_X,
    beamCount = DUPLEXES,
    foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
    columns = columns,
    subdivisions = subdivisions,
    supports = supports
)

/** `C-0022`'s solved edge profile, keyed on concentration, gap **and bias**. */
private fun solvedProfile(file: File): Pair<CollarTerm, CollarTerm> {
    require(file.exists()) {
        "C-0022's result file is missing: ${file.path}. T-125 sweeps a PLACEMENT under the " +
                "SOLVED load, because a uniform load makes a free plate dish exactly zero " +
                "whatever its rigidity, so a uniform-load sweep is vacuous."
    }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("profiles").jsonArray.map { it.jsonObject }
        .firstOrNull {
            fun value(key: String) = it.getValue(key).jsonPrimitive.content.toDouble()
            value("concentration") == 2.0 && value("gapHeight") == 10.0 &&
                    value("appliedBias") == 0.192
        } ?: error("no C-0022 profile at 2 mM, 10 nm, 0.192 V")
    fun value(key: String) = record.getValue(key).jsonPrimitive.content.toDouble()
    return CollarTerm(value("taperDepth"), value("taperWidth")) to
            CollarTerm(value("rimResidualDepth"), RIM_STANDOFF)
}

/** `C-0055`'s own 34 roots, read from its result file rather than retyped. */
private fun c0055Roots(file: File): List<Pair<Int, Double>> {
    require(file.exists()) {
        "C-0055's result file is missing: ${file.path}. T-125 sweeps AROUND its placement and " +
                "will not substitute an assumed one for it."
    }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("bestPhasePlacement").jsonArray.map { it.jsonObject }
        .map {
            it.getValue("row").jsonPrimitive.content.toInt() to
                    it.getValue("rootX").jsonPrimitive.content.toDouble()
        }
}

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

fun main() {
    val started = System.currentTimeMillis()
    val sheet = sheet()
    val edgeX = Gen1Tile.EDGE_X
    val lengthY = DUPLEXES * sheet.interhelicalDistance
    val area = edgeX * lengthY
    val interiorPressure = Gen1Tile.TARGET_FORCE / area
    val arm = C0055_ARM_LENGTH
    val count = C0055_ARM_COUNT
    val pitch = Gen1Tile.CROSSOVER_SPACING_SHEET_BP * Gen1Tile.RISE_PER_BASE_PAIR

    println("T-125 — reading C-0022's solved load and C-0055's own placement ...")
    val (smooth, rim) = solvedProfile(File("gpd/results/T-3b-tile-edge-load-profile.json"))
    val solvedField = edgeCollarPressure(interiorPressure, edgeX, lengthY, listOf(smooth, rim))
    val uniformField = uniformPressure(interiorPressure)
    val publishedRoots = c0055Roots(File("gpd/results/T-119-unused-junction-site.json"))
    check(publishedRoots.size == count) {
        "C-0055's placement must carry $count roots, carried ${publishedRoots.size}"
    }

    val freeStroke = PlateOnFoundation(
        sheet.plate(edgeX, lengthY), Gen1Tile.FOUNDATION_SECANT, emptyList(), basisDegree = 12
    ).solve(uniformField).meanDeflection

    val nominalColumns = CrossoverLayout.centred(8, sheet.crossoverSpacing / 2.0)

    // ------------------------------------------------------------------ the cheap bounds
    println("T-125 — the cheap bounds, which run before any sweep ...")
    val threeRows = rowsCarryingThreeArms(count, DUPLEXES, 3)
    val symmetricPhases = centroSymmetricUpwardPhases(edgeX, DUPLEXES)
    val greedyAtZero = greedyUpwardPlacement(0, edgeX, DUPLEXES, arm, count)

    // C-0055's own placement, reproduced from the lattice rather than read as coordinates
    val publishedByRow = publishedRoots.groupBy({ it.first }, { it.second })
        .mapValues { it.value.sorted() }
    val greedyDeparture = greedyAtZero.rows.maxOf { row ->
        val theirs = publishedByRow[row.row] ?: error("C-0055 places nothing in row ${row.row}")
        require(theirs.size == row.count) {
            "row ${row.row}: C-0055 places ${theirs.size} arms, the reconstruction ${row.count}"
        }
        row.roots.zip(theirs).maxOf { (a, b) -> abs(a - b) }
    }
    check(greedyDeparture < 1e-9) {
        "the reconstruction of C-0055's placement departs from its own result file by " +
                "$greedyDeparture nm"
    }

    // C-0061's mirrored set: is it on the upward lattice at all?
    val zeroSites = upwardRootLattice(0, edgeX, DUPLEXES)
    val downwardAtZero = junctionSites(0, edgeX, DUPLEXES)
        .filter { it.azimuth == CrossoverAzimuth.WEST }
    val mirroredRoots = greedyAtZero.rows.filter { it.row % 2 == 1 }
        .flatMap { row -> row.roots.map { row.row to -it } }
    val mirroredOffLattice = mirroredRoots.count { (row, x) ->
        zeroSites[row].none { abs(it - x) < 1e-9 }
    }
    val mirroredDownward = mirroredRoots.count { (row, x) ->
        downwardAtZero.any { it.duplex == row && abs(it.x - x) < 1e-9 }
    }

    val uniformShare = MANDATE / count
    val bounds = listOf(
        T125BoundRecord(
            "rows that must carry three arms, from 3a + 2(15 - a) = 34",
            threeRows.toDouble(), "rows",
            "the shape of the whole design space in one line: exactly four rows of three and " +
                    "eleven of two, so a placement is a choice of WHICH four and of where each " +
                    "row sits in its own 10.88 nm pitch"
        ),
        T125BoundRecord(
            "phases admitting a centro-symmetric placement, of 32",
            symmetricPhases.size.toDouble(), "phases",
            "the congruence 2c = 0 (mod p) holds at phases " + symmetricPhases.joinToString(", ") +
                    " — and at C-0055's own phase 0 it does not, so no placement there is " +
                    "centro-symmetric at all"
        ),
        T125BoundRecord(
            "C-0061's mirrored roots that are NOT upward sites",
            mirroredOffLattice.toDouble(), "roots",
            "of ${mirroredRoots.size} reflected odd-row roots; $mirroredDownward of them land " +
                    "on the WEST (downward) azimuth, which points INTO the grafted layer and is " +
                    "the half of the out-of-plane inventory C-0055 counted and refused"
        ),
        T125BoundRecord(
            "the uniform per-path force at 34 roots and the acceptable stroke",
            uniformShare * Gen1Tile.ACCEPTABLE_STROKE, "pN",
            "against the 10 pN unzip allowable, i.e. an admissible non-uniformity ratio of " +
                    "%.3f".format(
                        admissibleStiffnessRatio(
                            Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE,
                            MANDATE, count
                        )
                    ) + " — the force is not what binds at this count"
        ),
        T125BoundRecord(
            "the arm against its own root pitch", arm / pitch, "-",
            "C-0055's 8.164 nm against 10.88 nm: three arms per row and no fourth, which is " +
                    "why the count vector has the shape the first bound gives it"
        )
    )

    // ------------------------------------------------------------------ the phase sweep
    println("T-125 — sweeping all 32 phases, each on its OWN host ...")

    fun starts(phase: Int, sites: List<List<Double>>): List<UpwardArmPlacement> {
        val threes = setOf(0, 4, 10, 14)
        fun place(pick: (List<List<Double>>) -> List<Double>) = UpwardArmPlacement(
            phase,
            (0 until DUPLEXES).map { row ->
                val size = if (row in threes) 3 else 2
                val options = rowRootOptions(sites[row], size, arm, edgeX)
                val roots = pick(options)
                UpwardArmRow(row, roots, armDirections(roots, arm, edgeX)!!)
            }
        )
        return listOf(
            greedyUpwardPlacement(phase, edgeX, DUPLEXES, arm, count),
            place { options -> options.minBy { roots -> roots.sumOf { it * it } } },
            place { options -> options.maxBy { roots -> roots.sumOf { it * it } } },
            place { options -> options.minBy { roots -> abs(roots.sum()) } }
        )
    }

    class PhaseSolve(val phase: Int) {
        val columns = CrossoverLayout.atBasePairPhase(phase, sheet, edgeX)
        val host = lattice(sheet, columns)
        val sites = upwardRootLattice(phase, edgeX, DUPLEXES)
        val stations = sites.flatMapIndexed { row, xs ->
            xs.map { it to (row - (DUPLEXES - 1) / 2.0) * sheet.interhelicalDistance }
        }
        val bank = UpwardRootInfluenceBank(host, stations, solvedField)
        val uniform = List(count) { MANDATE / count }

        fun indices(placement: UpwardArmPlacement) =
            placement.stations(DUPLEXES).map { (x, y) ->
                val index = bank.indexOf(x, y)
                require(index >= 0) { "($x, $y) is not an upward site of phase $phase" }
                index
            }

        fun surrogate(placement: UpwardArmPlacement): InfluenceSurrogate =
            bank.surrogateFor(indices(placement))

        fun dishing(placement: UpwardArmPlacement): Double =
            surrogate(placement).solve(uniform).peakDishing / freeStroke

        val freeDishing = bank.freePeakDishing / freeStroke
    }

    var evaluationTotal = 0
    val phaseSolves = HashMap<Int, PhaseSolve>()
    val phases = (0 until 32).map { phase ->
        val solve = PhaseSolve(phase)
        if (phase in symmetricPhases || phase == 0 || phase == 6) phaseSolves[phase] = solve
        var evaluations = 0
        val descents = starts(phase, solve.sites).map { start ->
            descendPlacement(
                start, solve.sites, arm, edgeX, minimumPerRow = 1, maximumPerRow = 3
            ) { placement ->
                evaluations++
                solve.dishing(placement)
            }
        }
        val best = descents.minWith(
            compareBy({ it.objective }, { it.placement.key })
        )
        evaluationTotal += evaluations
        val greedy = solve.dishing(greedyUpwardPlacement(phase, edgeX, DUPLEXES, arm, count))
        println(
            ("  phase %2d  columns %d  free %6.4f  greedy %6.4f  best %6.4f  centroid %6.2f nm")
                .format(
                    phase, solve.columns.size, solve.freeDishing, greedy, best.objective,
                    best.placement.centroidX
                )
        )
        T125PhaseRecord(
            phaseBasePairs = phase,
            hostColumns = solve.columns.size,
            interfaceCrossovers = junctionSiteInventory(phase, edgeX, DUPLEXES).interfaceSites,
            centroSymmetricCapable = phase in symmetricPhases,
            freeDishingOverStroke = solve.freeDishing,
            greedyDishingOverStroke = greedy,
            bestDishingOverStroke = best.objective,
            bestCentroidX = best.placement.centroidX,
            bestCentroSymmetric = best.placement.isCentroSymmetric(DUPLEXES),
            bestBeatsNoCoupling = best.objective < solve.freeDishing,
            evaluations = evaluations,
            key = best.placement.key
        ) to best.placement
    }
    val phaseRecords = phases.map { it.first }
    val bestByPhase = phases.associate { it.first.phaseBasePairs to it.second }

    // -------------------------------------------- the symmetric family, enumerated exhaustively
    println("T-125 — enumerating the centro-symmetric family exhaustively ...")
    var symmetricBest: Pair<UpwardArmPlacement, Double>? = null
    val symmetricFamily = symmetricPhases.map { phase ->
        val solve = phaseSolves.getValue(phase)
        val values = ArrayList<Double>()
        var best: Pair<UpwardArmPlacement, Double>? = null
        centroSymmetricPlacements(
            phase, edgeX, DUPLEXES, arm, count, minimumPerRow = 2, maximumPerRow = 3
        ).forEach { placement ->
            val value = solve.dishing(placement)
            values += value
            val current = best
            if (current == null || value < current.second ||
                (value == current.second && placement.key < current.first.key)
            ) best = placement to value
        }
        val winner = best ?: error("the symmetric family at phase $phase is empty")
        val overall = symmetricBest
        if (overall == null || winner.second < overall.second) symmetricBest = winner
        values.sort()
        println(
            "  phase %2d  enumerated %6d  best %6.4f  worst %6.4f".format(
                phase, values.size, values.first(), values.last()
            )
        )
        T125SymmetricFamilyRecord(
            phaseBasePairs = phase,
            enumerated = values.size,
            bestDishingOverStroke = values.first(),
            worstDishingOverStroke = values.last(),
            medianDishingOverStroke = values[values.size / 2],
            bestKey = winner.first.key,
            exhaustive = true
        )
    }

    // ------------------------------------------------------------------ the best placement
    val descentWinner = phaseRecords.minWith(
        compareBy({ it.bestDishingOverStroke }, { it.key })
    )
    val symmetricWinner = symmetricBest ?: error("no symmetric placement was found")
    val best = if (
        symmetricWinner.second < descentWinner.bestDishingOverStroke
    ) symmetricWinner.first else bestByPhase.getValue(descentWinner.phaseBasePairs)
    val bestPhase = best.phaseBasePairs
    val bestSolve = phaseSolves[bestPhase] ?: PhaseSolve(bestPhase).also {
        phaseSolves[bestPhase] = it
    }
    val bestDishing = bestSolve.dishing(best)
    val bestSites = upwardRootLattice(bestPhase, edgeX, DUPLEXES)
    val bestRows = best.rows.map { row ->
        T125RowRecord(
            phaseBasePairs = bestPhase,
            row = row.row,
            y = (row.row - (DUPLEXES - 1) / 2.0) * sheet.interhelicalDistance,
            sites = bestSites[row.row],
            roots = row.roots,
            towardPositiveX = row.towardPositiveX
        )
    }

    // --------------------------------------------------- the flatness table, assembled not reduced
    println("T-125 — the flatness table, on assembled lattices ...")
    val gridStations = attachmentGrid(3, DUPLEXES, edgeX, lengthY)
    val columnStations = attachmentGrid(1, DUPLEXES, edgeX, lengthY)
    val zeroSolve = phaseSolves.getValue(0)

    fun flatness(
        name: String,
        phase: Int,
        columns: CrossoverLayout,
        hostName: String,
        stations: List<Pair<Double, Double>>,
        free: Double
    ): T125FlatnessRecord {
        val supports = if (stations.isEmpty()) emptyList() else couplingSupports(stations, MANDATE)
        val solution = lattice(sheet, columns, supports).solve(solvedField)
        val dishing = solution.peakDishing() / freeStroke
        return T125FlatnessRecord(
            placement = name,
            phaseBasePairs = phase,
            host = hostName,
            stations = stations.size,
            dishingOverStroke = dishing,
            flatAtTenPercent = dishing < FLATNESS_TOLERANCE,
            beatsNoCoupling = dishing < free,
            peakPathForce = if (stations.isEmpty()) 0.0 else solution.supportForces.maxOf { abs(it) },
            peakCrossoverForce = solution.peakCrossoverForce,
            peakDuplexShear = solution.peakDuplexShear
        )
    }

    val nominalFree = lattice(sheet, nominalColumns).solve(solvedField).peakDishing() / freeStroke
    val flatness = listOf(
        flatness(
            "NONE — free tile, C-0009's nominal eight-column host", -1, nominalColumns,
            "nominal 8 columns", emptyList(), Double.POSITIVE_INFINITY
        ),
        flatness(
            "ROOTS — C-0055's own 34, on C-0061's nominal host", 0, nominalColumns,
            "nominal 8 columns", greedyAtZero.stations(DUPLEXES), nominalFree
        ),
        flatness(
            "ROOTS — C-0055's own 34, on ITS OWN phase-0 host", 0,
            CrossoverLayout.atBasePairPhase(0, sheet, edgeX), "phase 0, seven columns",
            greedyAtZero.stations(DUPLEXES), zeroSolve.freeDishing
        ),
        flatness(
            "NONE — free tile, C-0055's own phase-0 host", 0,
            CrossoverLayout.atBasePairPhase(0, sheet, edgeX), "phase 0, seven columns",
            emptyList(), Double.POSITIVE_INFINITY
        ),
        flatness(
            "ROOTS-MIRRORED — C-0061's reflection, which is NOT on the upward lattice", 0,
            nominalColumns, "nominal 8 columns",
            greedyAtZero.rows.flatMap { row ->
                val y = (row.row - (DUPLEXES - 1) / 2.0) * sheet.interhelicalDistance
                if (row.row % 2 == 1) row.roots.map { -it to y } else row.roots.map { it to y }
            },
            nominalFree
        ),
        flatness(
            "BEST — T-125's swept placement, on its own host", bestPhase,
            CrossoverLayout.atBasePairPhase(bestPhase, sheet, edgeX),
            "phase $bestPhase", best.stations(DUPLEXES), bestSolve.freeDishing
        ),
        flatness(
            "NONE — free tile, on the best placement's own host", bestPhase,
            CrossoverLayout.atBasePairPhase(bestPhase, sheet, edgeX), "phase $bestPhase",
            emptyList(), Double.POSITIVE_INFINITY
        ),
        flatness(
            "GRID — C-0015's 3 x 15, which no placement supplies", -1, nominalColumns,
            "nominal 8 columns", gridStations, nominalFree
        ),
        flatness(
            "COLUMN — C-0041's 1 x 15", -1, nominalColumns, "nominal 8 columns",
            columnStations, nominalFree
        )
    )

    // ------------------------------------------------------------------ the distributions
    println("T-125 — C-0058's distribution family, on the placements that survive ...")
    val ceiling = perPathStiffnessCeiling(
        Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE
    )
    val admissible = admissibleStiffnessRatio(
        Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE, MANDATE, count
    )

    fun distributions(
        name: String,
        placement: UpwardArmPlacement,
        solve: PhaseSolve
    ): List<T125DistributionRecord> {
        val surrogate = solve.surrogate(placement)
        val stations = placement.stations(DUPLEXES)
        return listOf(1.0, 2.0, 3.0, 5.0, 8.0, 12.0, 20.0).map { ratio ->
            val weights = rimStiffenedWeights(stations, edgeX, lengthY, C0058_COLLAR_WIDTH, ratio)
            val stiffnesses = normalisedStiffnesses(weights, MANDATE)
            val solution = surrogate.solve(stiffnesses)
            val dishing = solution.peakDishing / freeStroke
            T125DistributionRecord(
                placement = name,
                stations = stations.size,
                rule = "rim x ratio over a %.2f nm collar (C-0058's one-parameter family)"
                    .format(C0058_COLLAR_WIDTH),
                ratio = ratio,
                dishingOverStroke = dishing,
                flatAtTenPercent = dishing < FLATNESS_TOLERANCE,
                peakPathForce = stiffnesses.max() * Gen1Tile.ACCEPTABLE_STROKE,
                peakSolvedPathForce = solution.supportForces.maxOf { abs(it) },
                peakThermalForce = perPathThermalForces(stiffnesses).max(),
                admissibleRatio = admissible,
                withinPerPathAllowable =
                    stiffnesses.max() * Gen1Tile.ACCEPTABLE_STROKE <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE
            )
        }
    }

    val bestSurrogate = bestSolve.surrogate(best)
    val greedySurrogate = zeroSolve.surrogate(greedyAtZero)
    val optimum = optimiseStiffnessDistribution(
        totalStiffness = MANDATE,
        starts = listOf(
            List(count) { 1.0 },
            rimStiffenedWeights(
                best.stations(DUPLEXES), edgeX, lengthY, C0058_COLLAR_WIDTH, 5.0
            ),
            rimStiffenedWeights(
                best.stations(DUPLEXES), edgeX, lengthY, C0058_COLLAR_WIDTH, 3.0
            )
        ),
        ceiling = ceiling
    ) { stiffnesses -> bestSurrogate.solve(stiffnesses).peakDishing / freeStroke }
    val optimisedForces = bestSurrogate.solve(optimum.stiffnesses)
    val distributions = distributions("BEST — T-125's swept placement", best, bestSolve) +
            distributions("ROOTS — C-0055's own greedy placement", greedyAtZero, zeroSolve) +
            T125DistributionRecord(
                placement = "BEST — T-125's swept placement",
                stations = count,
                rule = "C-0058's ${count}-parameter coordinate descent under C-0049's ceiling",
                ratio = optimum.stiffnesses.max() / (MANDATE / count),
                dishingOverStroke = optimum.objective,
                flatAtTenPercent = optimum.objective < FLATNESS_TOLERANCE,
                peakPathForce = optimum.stiffnesses.max() * Gen1Tile.ACCEPTABLE_STROKE,
                peakSolvedPathForce = optimisedForces.supportForces.maxOf { abs(it) },
                peakThermalForce = perPathThermalForces(optimum.stiffnesses).max(),
                admissibleRatio = admissible,
                withinPerPathAllowable = true
            )

    val bestFloor = bestSurrogate.reachableDishingFloor / freeStroke
    val greedyFloor = greedySurrogate.reachableDishingFloor / freeStroke

    // ------------------------------------------------------------------ the load-case falsifier
    println("T-125 — the load-case falsifier ...")
    val uniformFreeDishing = lattice(sheet, nominalColumns).solve(uniformField).peakDishing()
    check(abs(uniformFreeDishing) < 1e-9) {
        "a uniform load on a uniform Winkler foundation must dish EXACTLY zero on a free tile, " +
                "and it dished $uniformFreeDishing nm — the sweep would then be measuring the " +
                "load case and not the placement"
    }
    val uniformBank = UpwardRootInfluenceBank(
        lattice(sheet, CrossoverLayout.atBasePairPhase(bestPhase, sheet, edgeX)),
        bestSolve.stations, uniformField
    )
    val uniformDescent = descendPlacement(
        greedyUpwardPlacement(bestPhase, edgeX, DUPLEXES, arm, count),
        bestSites, arm, edgeX, minimumPerRow = 1, maximumPerRow = 3
    ) { placement ->
        uniformBank.surrogateFor(
            placement.stations(DUPLEXES).map { (x, y) -> uniformBank.indexOf(x, y) }
        ).solve(bestSolve.uniform).peakDishing / freeStroke
    }
    val loadCases = listOf(
        T125LoadCaseRecord(
            loadCase = "uniform (the load case a free plate cannot dish under)",
            freeDishing = uniformFreeDishing,
            bestDishingOverStroke = uniformDescent.objective,
            bestKey = uniformDescent.placement.key,
            note = "the free tile dishes EXACTLY zero here, so the whole of this number is the " +
                    "coupling's own sag; the argmin it selects is " +
                    (if (uniformDescent.placement.key == best.key) "the SAME as" else "NOT the same as") +
                    " the one C-0022's solved load selects, which is why a flatness result is " +
                    "quoted with its load case"
        ),
        T125LoadCaseRecord(
            loadCase = "C-0022 solved, 2 mM, 10 nm gap, 0.192 V",
            freeDishing = bestSolve.freeDishing * freeStroke,
            bestDishingOverStroke = bestDishing,
            bestKey = best.key,
            note = "the load case every number in this claim is quoted at"
        )
    )

    // ------------------------------------------------------------------ convergence
    println("T-125 — convergence ...")
    val bestSupports = couplingSupports(best.stations(DUPLEXES), MANDATE)
    val bestColumns = CrossoverLayout.atBasePairPhase(bestPhase, sheet, edgeX)
    val nested = listOf(1, 2, 4).map { subdivisions ->
        lattice(sheet, bestColumns, bestSupports, subdivisions).solve(solvedField)
            .peakDishing() / freeStroke
    }
    val samples = listOf(41, 81, 161).map { grid ->
        lattice(sheet, bestColumns, bestSupports).solve(solvedField).peakDishing(grid) / freeStroke
    }
    val assembled = lattice(sheet, bestColumns, bestSupports).solve(solvedField).peakDishing() /
            freeStroke
    val repeatDescent = descendPlacement(
        starts(bestPhase, bestSites).first(), bestSites, arm, edgeX,
        minimumPerRow = 1, maximumPerRow = 3
    ) { bestSolve.dishing(it) }
    val repeatAgain = descendPlacement(
        starts(bestPhase, bestSites).first(), bestSites, arm, edgeX,
        minimumPerRow = 1, maximumPerRow = 3
    ) { bestSolve.dishing(it) }

    // the arms themselves, attached — C-0061's exact zero, checked on the best placement
    val armed = StackedArmGrillage(
        lattice(sheet, bestColumns, bestSupports),
        best.rows.flatMap { row ->
            row.roots.zip(row.towardPositiveX).map { (x, toward) ->
                StackedArm(row.row, x, arm, toward)
            }
        }
    ).solve(solvedField).deflection.peakDishing() / freeStroke

    val convergence = listOf(
        T125ConvergenceRecord(
            "dishing/stroke of the best placement", "nested subdivisions 1 c 2 c 4",
            listOf(1.0, 2.0, 4.0), nested,
            abs(nested[2] - nested[1]) / nested[1],
            "nested, per CLAUDE.md — a subdivision of 3 moves a station off a node and is not a " +
                    "refinement of 2"
        ),
        T125ConvergenceRecord(
            "dishing/stroke of the best placement", "dishing sample grid",
            listOf(41.0, 81.0, 161.0), samples,
            abs(samples[2] - samples[1]) / samples[1],
            "81 is the grid every published dishing in this programme is read on"
        ),
        T125ConvergenceRecord(
            "dishing/stroke of the best placement", "Woodbury surrogate against the assembly",
            listOf(0.0, 1.0), listOf(bestDishing, assembled),
            abs(bestDishing - assembled) / assembled,
            "FALSIFIER 1: superposition is exact for a linear system, so any departure above " +
                    "round-off means the whole sweep was run on the wrong object"
        ),
        T125ConvergenceRecord(
            "dishing/stroke of the best placement", "34 arms ATTACHED (C-0061's exact zero)",
            listOf(0.0, 1.0), listOf(assembled, armed),
            abs(armed - assembled) / assembled,
            "the arms add exactly nothing at one tie, so the sweep may be run on the host with " +
                    "the coupling at the roots — asserted here rather than assumed"
        ),
        T125ConvergenceRecord(
            "the descent's own argmin", "repeat runs",
            listOf(1.0, 2.0), listOf(repeatDescent.objective, repeatAgain.objective),
            abs(repeatDescent.objective - repeatAgain.objective),
            if (repeatDescent.placement.key == repeatAgain.placement.key)
                "identical placement key — the argmin is rounded at the decision point and " +
                        "tie-broken on a canonical key, per CLAUDE.md"
            else "THE ARGMIN IS NOT DETERMINISTIC"
        )
    )

    // ------------------------------------------------------------------ upstream reproductions
    fun reproduction(
        source: String, quantity: String, published: Double, reproduced: Double, strict: Boolean
    ) = T125ReproductionRecord(
        source, quantity, published, reproduced,
        abs(reproduced - published) / abs(published), strict
    )

    val gridUniform = flatness.first { it.placement.startsWith("GRID") }
    val gridWeights = rimStiffenedWeights(gridStations, edgeX, lengthY, C0058_COLLAR_WIDTH, 5.0)
    val gridRimFive = lattice(
        sheet, nominalColumns,
        gridStations.mapIndexed { index, (x, y) ->
            PointSupport(x, y, normalisedStiffnesses(gridWeights, MANDATE)[index])
        }
    ).solve(solvedField).peakDishing() / freeStroke
    val reproductions = listOf(
        reproduction("C-0055", "arm length at 34 paths [nm]", 8.164, arm, false),
        reproduction("C-0055", "upward root pitch [nm]", 10.88, pitch, true),
        reproduction("C-0055", "arms placed", 34.0, greedyAtZero.count.toDouble(), true),
        reproduction(
            "C-0061", "coupling centroid of C-0055's placement [nm]", -8.80,
            greedyAtZero.centroidX, false
        ),
        reproduction(
            "C-0061", "dishing/stroke, C-0055's roots, uniform coupling", 0.4156,
            flatness.first { it.placement.startsWith("ROOTS") && it.host.startsWith("nominal") }
                .dishingOverStroke, false
        ),
        reproduction("C-0022", "dishing/stroke, free uncoupled tile", 0.3079, nominalFree, false),
        reproduction(
            "C-0058", "dishing/stroke, 3 x 15, uniform", 0.2182,
            gridUniform.dishingOverStroke, false
        ),
        reproduction("C-0058", "dishing/stroke, 3 x 15, rim x 5", 0.0753, gridRimFive, false),
        reproduction(
            "C-0058", "the admissible per-path ratio at 45 paths", 4.5,
            admissibleStiffnessRatio(
                Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE, MANDATE, 45
            ), true
        ),
        reproduction(
            "C-0047", "dishing/stroke, 1 x 15", 0.695,
            flatness.first { it.placement.startsWith("COLUMN") }.dishingOverStroke, false
        ),
        reproduction(
            "C-0015", "interface crossovers at an eight-column phase", 56.0,
            phaseRecords.first { it.hostColumns == 8 }.interfaceCrossovers.toDouble(), true
        ),
        reproduction(
            "C-0015", "interface crossovers at a seven-column phase", 49.0,
            phaseRecords.first { it.hostColumns == 7 }.interfaceCrossovers.toDouble(), true
        )
    )
    val worstStrict = reproductions.filter { it.strict }.maxOf { it.departure }
    check(worstStrict < 1e-6) { "a strict upstream reproduction departs by $worstStrict" }

    // ------------------------------------------------------------------ predicates and findings
    val beatsFree = bestDishing < bestSolve.freeDishing
    val predicates = listOf(
        T125PredicateRecord(
            "T-125 acceptance",
            "the flatness of the best 34-root placement under C-0022's solved load, against " +
                    "T-5b's 10 % convention",
            "%.4f of the stroke, %s T-5b's 0.10".format(
                bestDishing, if (bestDishing < FLATNESS_TOLERANCE) "INSIDE" else "OUTSIDE"
            )
        ),
        T125PredicateRecord(
            "the bar C-0047 sets",
            "a coupling must beat no coupling at all",
            "%.4f against the free tile's %.4f — %s".format(
                bestDishing, bestSolve.freeDishing,
                if (beatsFree) "CLEARED" else "NOT CLEARED"
            )
        ),
        T125PredicateRecord(
            "CH-0074",
            "C-0058's flat distribution lives on stations no placement supplies",
            if (optimum.objective < FLATNESS_TOLERANCE)
                ("RESOLVED — a placement the upward lattice supplies reaches %.4f under a " +
                        "distribution, inside T-5b's 0.10").format(optimum.objective)
            else ("STANDS, and hardens: the best distribution on the best placement reaches " +
                    "%.4f and the least-squares floor over ALL force vectors is %.4f")
                .format(optimum.objective, bestFloor)
        ),
        T125PredicateRecord(
            "the per-path force",
            "the peak per-path force against the 10 pN unzip allowable",
            ("%.3f pN uniform at 34 roots, %.3f pN at the optimised distribution, against " +
                    "10 pN — admissible ratio %.3f").format(
                uniformShare * Gen1Tile.ACCEPTABLE_STROKE,
                optimum.stiffnesses.max() * Gen1Tile.ACCEPTABLE_STROKE, admissible
            )
        ),
        T125PredicateRecord(
            "C-0014's thermal force",
            "over-stiffening is not free",
            "%.3f pN per path uniform, %.3f pN at the optimised distribution".format(
                perPathThermalForces(List(count) { MANDATE / count }).max(),
                perPathThermalForces(optimum.stiffnesses).max()
            )
        )
    )

    val findings = listOf(
        ("The best 34-root placement the upward lattice can supply dishes %.4f of the stroke " +
                "under C-0022's solved load, against the free tile's %.4f on the same host, " +
                "C-0055's own placement at %.4f and C-0058's 3 x 15 at 0.0753. It is %s T-5b's " +
                "0.10 and it %s no coupling at all.").format(
            bestDishing, bestSolve.freeDishing,
            flatness.first { it.placement.startsWith("ROOTS") && it.host.startsWith("nominal") }
                .dishingOverStroke,
            if (bestDishing < FLATNESS_TOLERANCE) "INSIDE" else "OUTSIDE",
            if (beatsFree) "beats" else "does NOT beat"
        ),
        ("The placement is at phase %d of 32, %s centro-symmetric, and its coupling centroid is " +
                "%.3f nm against C-0055's -8.80. Exactly %d of the 32 phases can supply a " +
                "centro-symmetric placement at all, and they are %s — which is a congruence, " +
                "2c = 0 (mod p), and not a search.").format(
            bestPhase, if (best.isCentroSymmetric(DUPLEXES)) "is" else "is NOT", best.centroidX,
            symmetricPhases.size, symmetricPhases.joinToString(" and ")
        ),
        ("C-0061's mirrored placement is NOT on the upward lattice: %d of its %d reflected " +
                "odd-row roots are not upward sites at all, and %d of them are WEST sites — the " +
                "DOWNWARD azimuth, which points into the grafted layer and which C-0055 counted " +
                "and refused. Its 0.3558 and 0.1649 are quoted on a station set the array " +
                "cannot supply.").format(
            mirroredOffLattice, mirroredRoots.size, mirroredDownward
        ),
        ("The distribution question, settled on a placement that exists: C-0058's one-parameter " +
                "rim family reaches %.4f on the best placement, its %d-parameter descent %.4f, " +
                "and the least-squares floor over ALL force vectors — a rigorous lower bound on " +
                "every distribution whatever — is %.4f. On C-0055's own placement the same " +
                "floor is %.4f.").format(
            distributions.filter { it.placement.startsWith("BEST") && it.rule.startsWith("rim") }
                .minOf { it.dishingOverStroke },
            count, optimum.objective, bestFloor, greedyFloor
        ),
        ("The host's phase and the array's phase are ONE variable. C-0061 read C-0055's phase-0 " +
                "roots on the nominal eight-column host, which is the phase-8 lattice; on its " +
                "own seven-column host the same 34 roots dish %.4f against %.4f, a departure of " +
                "%.1f %%.").format(
            flatness.first { it.placement.startsWith("ROOTS") && it.host.startsWith("phase 0") }
                .dishingOverStroke,
            flatness.first { it.placement.startsWith("ROOTS") && it.host.startsWith("nominal") }
                .dishingOverStroke,
            100.0 * abs(
                flatness.first {
                    it.placement.startsWith("ROOTS") && it.host.startsWith("phase 0")
                }.dishingOverStroke -
                        flatness.first {
                            it.placement.startsWith("ROOTS") && it.host.startsWith("nominal")
                        }.dishingOverStroke
            ) / flatness.first {
                it.placement.startsWith("ROOTS") && it.host.startsWith("nominal")
            }.dishingOverStroke
        )
    )

    val result = T125Result(
        task = "T-125 — sweep the row phases of C-0055's upward arm array",
        leaf = "A8.2",
        conditions = ("T = 300 K, k_BT = 4.141947 pN nm; aqueous 2 mM MgCl2; 40.0 x %.2f nm " +
                "single-layer square-lattice Rothemund sheet, %d duplexes at 2.69 nm; C-0039's " +
                "%.3f nm arm at C-0055's self-consistent %d roots; C-0017's %.4f pN/nm mandate; " +
                "C-0022's solved profile at 2 mM, a 10 nm gap and 0.192 V; C-0001's foundation " +
                "secant").format(lengthY, DUPLEXES, arm, count, MANDATE),
        decision = ("the best 34-root placement dishes %.4f of the stroke; T-5b's convention is " +
                "0.10 and the free tile is %.4f").format(bestDishing, bestSolve.freeDishing),
        bounds = bounds,
        phases = phaseRecords,
        symmetricFamily = symmetricFamily,
        bestPlacement = bestRows,
        flatness = flatness,
        distributions = distributions,
        loadCases = loadCases,
        convergence = convergence,
        reproductions = reproductions,
        predicates = predicates,
        findings = findings,
        parameters = mapOf(
            "armLength" to arm,
            "armCount" to count.toDouble(),
            "rootPitch" to pitch,
            "edgeX" to edgeX,
            "lengthY" to lengthY,
            "duplexes" to DUPLEXES.toDouble(),
            "mandate" to MANDATE,
            "freeStroke" to freeStroke,
            "flatnessTolerance" to FLATNESS_TOLERANCE,
            "collarWidth" to C0058_COLLAR_WIDTH,
            "unzipAllowable" to Gen1Tile.DUPLEX_UNZIP_ALLOWABLE,
            "perPathStiffnessCeiling" to ceiling,
            "admissibleRatio" to admissible,
            "bestPhase" to bestPhase.toDouble(),
            "bestDishingOverStroke" to bestDishing,
            "bestReachableFloor" to bestFloor,
            "greedyReachableFloor" to greedyFloor,
            "optimisedDishingOverStroke" to optimum.objective,
            "placementEvaluations" to evaluationTotal.toDouble(),
            "symmetricEnumerated" to symmetricFamily.sumOf { it.enumerated }.toDouble()
        )
    )

    val output = File("gpd/results/T-125-upward-root-placement.json")
    output.parentFile.mkdirs()
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult() as JsonObject)
        )
    )

    println()
    println("cheap bounds")
    result.bounds.forEach {
        println("  %-56s %12.4f %-6s".format(it.name, it.value, it.unit))
    }
    println()
    println("flatness under C-0022's solved load")
    result.flatness.forEach {
        println(
            "  %-52s %-24s %3d stations  dish/stroke %7.4f  flat %s".format(
                it.placement, it.host, it.stations, it.dishingOverStroke, it.flatAtTenPercent
            )
        )
    }
    println()
    println("distributions")
    result.distributions.forEach {
        println(
            "  %-38s ratio %6.2f  dish/stroke %7.4f  flat %-5s  path %5.2f pN".format(
                it.placement, it.ratio, it.dishingOverStroke, it.flatAtTenPercent, it.peakPathForce
            )
        )
    }
    println()
    println("upstream reproductions")
    result.reproductions.forEach {
        println(
            "  %-10s %-52s %12.6g vs %12.6g  %8.2e %s".format(
                it.source, it.quantity, it.published, it.reproduced, it.departure,
                if (it.strict) "" else "(non-strict)"
            )
        )
    }
    println()
    println("predicates")
    result.predicates.forEach { println("  ${it.name}: ${it.verdict}") }
    println()
    result.findings.forEach { println("  * $it"); println() }
    println("written to ${output.path} in ${(System.currentTimeMillis() - started) / 1000} s")
}
