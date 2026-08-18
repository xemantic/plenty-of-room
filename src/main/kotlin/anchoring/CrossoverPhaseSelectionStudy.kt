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
import com.xemantic.nano.plentyofroom.coupling.couplingSupports
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.coupling.perPathThermalForces
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_COUNT
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_LENGTH
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.OrigamiSheet
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PointSupport
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.uniformCurvatureRigidity
import com.xemantic.nano.plentyofroom.structure.uniformMomentRigidity
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
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow

/**
 * `T-171` — the crossover phase is ONE variable and three claims want different values of it.
 *
 * Emits `gpd/results/T-171-crossover-phase-selection.json`.
 *
 * Reads `gpd/results/T-3b-tile-edge-load-profile.json` (`C-0022`'s solved edge profile),
 * `gpd/results/T-125-upward-root-placement.json` (`C-0063`'s published optimum, as a gate) and
 * `gpd/results/T-153-buildable-raster-width.json` (`C-0090`'s published optimum and its
 * placement keys, as a gate).
 */

private const val DUPLEXES = 15
private const val FLATNESS_TOLERANCE = 0.10
private const val RIM_STANDOFF = 1.0
private val MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

/** `C-0093`'s redundancy slope on its abstract grids, `d ln p90 / d ln n` — **CITED**. */
private const val C0093_REDUNDANCY_SLOPE = -0.784357442

/** `C-0098`'s redundancy slope measured on the **real** upward lattice — **CITED**. */
private const val C0098_REDUNDANCY_SLOPE = -0.376769756

/** `C-0098`'s graded 90th-percentile dishing at the richest 40 nm phase, 60 ties — **CITED**. */
private const val C0098_RICHEST_PHASE_P90 = 0.487309625

/** `C-0098`'s graded 90th-percentile dishing at phase 24, 53 ties — **CITED**. */
private const val C0098_PHASE_24_P90 = 0.385192562

/** `C-0087`'s measured staple incorporation: edge, mean, centre — **CITED, MEASURED**. */
private val C0087_INCORPORATION = listOf(0.48, 0.84, 0.95)

/**
 * [value] rounded to two significant digits.
 *
 * A **departure** is a difference of two nearly equal numbers and it is **dimensionless**, so
 * `RESULT_ABSOLUTE_FLOOR` — a magnitude in the locked units — cannot reach it, and the JIT
 * recompiling a hot reduction mid-run moves its ninth digit (`P-18`, `C-0098`). `CLAUDE.md`:
 * *emit the answer and a two-significant-digit convergence measure*. The gates are taken on the
 * unrounded value, before this is applied.
 */
private fun twoSignificantDigits(value: Double): Double {
    if (value == 0.0 || !value.isFinite()) return value
    val scale = 10.0.pow(floor(log10(abs(value))) - 1.0)
    return Math.round(value / scale) * scale
}

// ---------------------------------------------------------------------------------------------
// the records — prefixed with the task, because study records are package scoped (CLAUDE.md)
// ---------------------------------------------------------------------------------------------

@Serializable
private data class T171CensusRecord(
    val convention: String,
    val edgeX: Double,
    val admitRowEnd: Boolean,
    val phaseBasePairs: Int,
    val columns: Int,
    val interfaceCrossovers: Int,
    val crossoversPerInterface: List<Int>,
    val upwardSites: Int,
    val centroSymmetric: Boolean,
    val columnOnRowEnd: Boolean,
    val planeOnRowEnd: Boolean,
    val rowEndUpwardStations: Int,
    val rowEndColumns: Int
)

@Serializable
private data class T171DemandRecord(
    val convention: String,
    val edgeX: Double,
    val admitRowEnd: Boolean,
    val maximumUpwardSites: Int,
    val maximumColumns: Int,
    val richestUpwardInventory: List<Int>,
    val mostColumns: List<Int>,
    val centroSymmetric: List<Int>,
    val richestAndColumns: List<Int>,
    val richestAndSymmetry: List<Int>,
    val columnsAndSymmetry: List<Int>,
    val allThree: List<Int>,
    val note: String
)

@Serializable
private data class T171SheetPriceRecord(
    val quantity: String,
    val owner: String,
    val atSevenColumns: Double,
    val atEightColumns: Double,
    val sevenOverEight: Double,
    val closedForm: Boolean,
    val note: String
)

@Serializable
private data class T171PhaseRecord(
    val phaseBasePairs: Int,
    val role: String,
    val columns: Int,
    val interfaceCrossovers: Int,
    val upwardSites: Int,
    val centroSymmetric: Boolean,
    val freeDishingOverStroke: Double,
    val descentDishingOverStroke: Double,
    val flatAtTenPercent: Boolean,
    val descentEvaluations: Int,
    val freePeakCrossoverForce: Double,
    val freePeakDuplexShear: Double,
    val coupledPeakCrossoverForce: Double,
    val coupledPeakDuplexShear: Double,
    val thermalDishingRms: Double,
    val thermalCentreRms: Double,
    val peakPathForce: Double,
    val peakThermalPathForce: Double,
    val key: String
)

@Serializable
private data class T171TieCountRecord(
    val statement: String,
    val slope: Double,
    val slopeOwner: String,
    val tiesAtRichest: Int,
    val tiesAtEightColumn: Int,
    val factorTheInventoryBuys: Double,
    val factorThePhaseCosts: Double,
    val worthIt: Boolean
)

@Serializable
private data class T171ConvergenceRecord(
    val quantity: String,
    val parameter: String,
    val values: List<Double>,
    val results: List<Double>,
    val departure: Double,
    val note: String
)

@Serializable
private data class T171ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double,
    val strict: Boolean
)

@Serializable
private data class T171PredicateRecord(
    val name: String,
    val statement: String,
    val verdict: String
)

@Serializable
private data class T171FalsifierRecord(
    val name: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T171RecommendationRecord(
    val edgeX: Double,
    val phaseBasePairs: Int,
    val columns: Int,
    val upwardSites: Int,
    val centroSymmetric: Boolean,
    val demandDropped: String,
    val costOfDroppingIt: String,
    val dishingOverStroke: Double,
    val flatAtTenPercent: Boolean,
    val statement: String
)

@Serializable
private data class T171Result(
    val task: String,
    val leaf: String,
    val conditions: String,
    val decision: String,
    val census: List<T171CensusRecord>,
    val demands: List<T171DemandRecord>,
    val sheetPrice: List<T171SheetPriceRecord>,
    val phases: List<T171PhaseRecord>,
    val tieCount: List<T171TieCountRecord>,
    val recommendation: T171RecommendationRecord,
    val convergence: List<T171ConvergenceRecord>,
    val reproductions: List<T171ReproductionRecord>,
    val predicates: List<T171PredicateRecord>,
    val falsifiers: List<T171FalsifierRecord>,
    val findings: List<String>,
    val parameters: Map<String, Double>
)

// ---------------------------------------------------------------------------------------------
// the inputs, read from the claims that own them
// ---------------------------------------------------------------------------------------------

/** `C-0022`'s solved edge profile, keyed on concentration, gap **and bias** (`CLAUDE.md`). */
private fun solvedProfile(file: File): Pair<CollarTerm, CollarTerm> {
    require(file.exists()) { "C-0022's result file is missing: ${file.path}" }
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

/** `C-0063`'s published centro-symmetric optimum at one phase, and its placement key. */
private fun c0063Optimum(file: File, phase: Int): Pair<Double, String> {
    require(file.exists()) { "C-0063's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("symmetricFamily").jsonArray.map { it.jsonObject }
        .firstOrNull { it.getValue("phaseBasePairs").jsonPrimitive.content.toInt() == phase }
        ?: error("no C-0063 symmetric family record at phase $phase")
    return record.getValue("bestDishingOverStroke").jsonPrimitive.content.toDouble() to
            record.getValue("bestKey").jsonPrimitive.content
}

/** `C-0090`'s published optimum at the buildable width, keyed on its case and phase. */
private fun c0090Optimum(file: File, casePrefix: String, phase: Int): Pair<Double, String> {
    require(file.exists()) { "C-0090's result file is missing: ${file.path}" }
    val record = Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("placements").jsonArray.map { it.jsonObject }
        .firstOrNull {
            it.getValue("case").jsonPrimitive.content.startsWith(casePrefix) &&
                    it.getValue("phaseBasePairs").jsonPrimitive.content.toInt() == phase
        } ?: error("no C-0090 placement record for '$casePrefix' at phase $phase")
    return record.getValue("bestDishingOverStroke").jsonPrimitive.content.toDouble() to
            record.getValue("bestKey").jsonPrimitive.content
}

// ---------------------------------------------------------------------------------------------
// one width, one arm reading, one end-of-row convention — C-0090's own case, re-run
// ---------------------------------------------------------------------------------------------

private class T171Case(
    val name: String,
    val edgeX: Double,
    val arm: Double,
    val admitRowEnd: Boolean,
    val sheet: OrigamiSheet,
    smooth: CollarTerm,
    rim: CollarTerm,
    val inset: Double = CrossoverLayout.EDGE_MARGIN
) {

    val lengthY: Double = DUPLEXES * sheet.interhelicalDistance

    val area: Double = edgeX * lengthY

    val interiorPressure: Double = Gen1Tile.TARGET_FORCE / area

    val uniformField: PressureField = uniformPressure(interiorPressure)

    val solvedField: PressureField =
        edgeCollarPressure(interiorPressure, edgeX, lengthY, listOf(smooth, rim))

    val freeStroke: Double = PlateOnFoundation(
        sheet.plate(edgeX, lengthY), Gen1Tile.FOUNDATION_SECANT, emptyList(), basisDegree = 12
    ).solve(uniformField).meanDeflection

    fun sites(phase: Int): List<List<Double>> = rasterUpwardSites(
        phase, edgeX, DUPLEXES, admitRowEnd, Gen1Tile.RISE_PER_BASE_PAIR, inset
    )

    fun host(phase: Int, supports: List<PointSupport> = emptyList(), subdivisions: Int = 2) =
        OrigamiGrillage(
            sheet = sheet,
            lengthX = edgeX,
            beamCount = DUPLEXES,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            columns = rasterColumnLayout(phase, sheet, edgeX, admitRowEnd, inset),
            subdivisions = subdivisions,
            supports = supports
        )

    inner class PhaseSolve(val phase: Int) {
        val sites = this@T171Case.sites(phase)
        val stations = sites.flatMapIndexed { row, xs ->
            xs.map { it to (row - (DUPLEXES - 1) / 2.0) * sheet.interhelicalDistance }
        }
        val bank = UpwardRootInfluenceBank(host(phase), stations, solvedField)
        val uniform = List(C0055_ARM_COUNT) { MANDATE / C0055_ARM_COUNT }
        val freeDishing = bank.freePeakDishing / freeStroke

        fun surrogate(placement: UpwardArmPlacement): InfluenceSurrogate =
            bank.surrogateFor(
                placement.stations(DUPLEXES, sheet.interhelicalDistance).map { (x, y) ->
                    val index = bank.indexOf(x, y)
                    require(index >= 0) { "($x, $y) is not an upward site of phase $phase" }
                    index
                }
            )

        fun dishing(placement: UpwardArmPlacement): Double =
            surrogate(placement).solve(uniform).peakDishing / freeStroke
    }
}

/** `C-0090`'s own four descent starts, re-run so the search effort is matched across phases. */
private fun descentStarts(
    case: T171Case,
    phase: Int,
    sites: List<List<Double>>
): List<UpwardArmPlacement> {
    val threes = setOf(0, 4, 10, 14)
    fun place(pick: (List<List<Double>>) -> List<Double>) = UpwardArmPlacement(
        phase,
        (0 until DUPLEXES).map { row ->
            val size = if (row in threes) 3 else 2
            val options = rowRootOptions(sites[row], size, case.arm, case.edgeX)
            require(options.isNotEmpty()) {
                "row $row at phase $phase carries no $size-arm option on ${case.name}"
            }
            val roots = pick(options)
            UpwardArmRow(row, roots, armDirections(roots, case.arm, case.edgeX)!!)
        }
    )
    return listOf(
        greedyUpwardPlacement(phase, case.edgeX, DUPLEXES, case.arm, C0055_ARM_COUNT),
        place { options -> options.minBy { roots -> roots.sumOf { it * it } } },
        place { options -> options.maxBy { roots -> roots.sumOf { it * it } } },
        place { options -> options.minBy { roots -> abs(roots.sum()) } }
    )
}

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

fun main() {
    val started = System.currentTimeMillis()
    val sheet = origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)
    val nominal = Gen1Tile.EDGE_X
    val buildable = BUILDABLE_RASTER_WIDTH
    val elasticaArm = C0055_ARM_LENGTH
    val buildableArm = quantisedToRise(elasticaArm)
    val lengthY = DUPLEXES * Gen1Tile.INTERHELICAL_SHEET
    val hinge = sheet.crossoverHingeStiffness

    println("T-171 — reading C-0022's solved load and the two published optima ...")
    val (smooth, rim) = solvedProfile(File("gpd/results/T-3b-tile-edge-load-profile.json"))
    val (c0063Published, c0063Key) =
        c0063Optimum(File("gpd/results/T-125-upward-root-placement.json"), 24)
    val (c0090Published, c0090Key) =
        c0090Optimum(File("gpd/results/T-153-buildable-raster-width.json"), "RECOMMENDED", 8)

    // ---------------------------------------------------- the cheap bound: the census, no solve
    println("T-171 — the census over one integer, at both widths, with no solve at all ...")

    val conventions = listOf(
        Triple("40.00 nm, interior (C-0015's, C-0055's and C-0098's reading)", nominal, false),
        Triple("38.08 nm, row-end admitted (the programme's carried reading)", buildable, true),
        Triple("38.08 nm, row-end refused (C-0090's bracket)", buildable, false)
    )
    val censusByConvention = conventions.associate { (name, edgeX, admit) ->
        name to crossoverPhaseCensus(edgeX, DUPLEXES, sheet, admit)
    }
    val census = conventions.flatMap { (name, edgeX, admit) ->
        censusByConvention.getValue(name).map { row ->
            T171CensusRecord(
                convention = name,
                edgeX = edgeX,
                admitRowEnd = admit,
                phaseBasePairs = row.phaseBasePairs,
                columns = row.columns,
                interfaceCrossovers = row.interfaceCrossovers,
                crossoversPerInterface = row.crossoversPerInterface,
                upwardSites = row.upwardSites,
                centroSymmetric = row.centroSymmetric,
                columnOnRowEnd = row.columnOnRowEnd,
                planeOnRowEnd = row.planeOnRowEnd,
                rowEndUpwardStations = rowEndUpwardStations(row.phaseBasePairs, edgeX, DUPLEXES),
                rowEndColumns = rowEndColumns(row.phaseBasePairs, edgeX, DUPLEXES, sheet)
            )
        }
    }

    val demands = conventions.map { (name, edgeX, admit) ->
        val ledger = phaseDemandLedger(censusByConvention.getValue(name))
        T171DemandRecord(
            convention = name,
            edgeX = edgeX,
            admitRowEnd = admit,
            maximumUpwardSites = ledger.maximumUpwardSites,
            maximumColumns = ledger.maximumColumns,
            richestUpwardInventory = ledger.richestUpwardInventory,
            mostColumns = ledger.eightColumnHost,
            centroSymmetric = ledger.centroSymmetric,
            richestAndColumns = ledger.richestAndColumns,
            richestAndSymmetry = ledger.richestAndSymmetry,
            columnsAndSymmetry = ledger.columnsAndSymmetry,
            allThree = ledger.allThree,
            note = if (ledger.allThree.isEmpty())
                "the three demands do NOT coincide at this width and convention"
            else "a phase satisfies all three demands at once"
        )
    }
    val buildableLedger = phaseDemandLedger(
        censusByConvention.getValue(conventions[1].first)
    )
    val nominalLedger = phaseDemandLedger(censusByConvention.getValue(conventions[0].first))

    // ------------------------------------------- the sheet-side price, three of it closed form
    println("T-171 — the sheet-side price of a seven-column host ...")

    val buildableCensus = censusByConvention.getValue(conventions[1].first)
    val sevenRow = buildableCensus.single { it.phaseBasePairs == buildableLedger.richestUpwardInventory.first() }
    val eightRow = buildableCensus.single { it.phaseBasePairs == buildableLedger.centroSymmetric.first() }
    val sevenPerInterface = sevenRow.crossoversPerInterface
    val eightPerInterface = eightRow.crossoversPerInterface
    val area = buildable * lengthY

    fun smeared(perInterface: List<Int>) = uniformCurvatureRigidity(
        perInterface.sum(), hinge, Gen1Tile.INTERHELICAL_SHEET, area
    )

    fun series(perInterface: List<Int>) =
        uniformMomentRigidity(perInterface, hinge, buildable, lengthY)

    fun price(
        quantity: String,
        owner: String,
        seven: Double,
        eight: Double,
        closedForm: Boolean,
        note: String
    ) = T171SheetPriceRecord(
        quantity, owner, seven, eight, if (eight != 0.0) seven / eight else -1.0, closedForm, note
    )

    val sheetPriceClosed = buildList {
        add(
            price(
                "interface crossovers", "C-0015",
                sevenRow.interfaceCrossovers.toDouble(), eightRow.interfaceCrossovers.toDouble(),
                true,
                "49 against 56 — the seven-column host keeps 7/8 of the sheet's in-plane paths"
            )
        )
        add(
            price(
                "minimum crossovers on any one interface", "C-0054",
                sevenPerInterface.min().toDouble(), eightPerInterface.min().toDouble(), true,
                "a seven-column sheet splits its columns 4/3 between the two parities, so half " +
                        "its interfaces carry THREE paths and half carry four; an eight-column " +
                        "sheet carries four everywhere"
            )
        )
        add(
            price(
                "D_perp, smeared (uniform curvature) [pN nm]", "C-0054 / C-0009",
                smeared(sevenPerInterface), smeared(eightPerInterface), true,
                "linear in the retained count, so exactly 49/56 = 7/8 = 0.875 — what a " +
                        "continuum plate can express"
            )
        )
        add(
            price(
                "D_perp, series (uniform moment) [pN nm]", "C-0054",
                series(sevenPerInterface), series(eightPerInterface), true,
                "a HARMONIC mean over the fourteen interfaces: 7/4 + 7/3 against 14/4, so " +
                        "exactly 42/49 = 6/7 = 0.857142857. The two readings differ by 48/49 — " +
                        "the smeared one understates the loss because the sheet bends across " +
                        "its interfaces in SERIES and the thin ones dominate"
            )
        )
        add(
            price(
                "bending anisotropy D_par / D_perp, series", "C-0009",
                sheet.alongHelixRigidity / series(sevenPerInterface),
                sheet.alongHelixRigidity / series(eightPerInterface), true,
                "the sheet gets 16.7 % more anisotropic, which is the axis C-0015's " +
                        "'shapes, not counts' rule is written on"
            )
        )
        add(
            price(
                "spendable crossovers under C-0054's connectivity theorem", "C-0054",
                (sevenRow.interfaceCrossovers - (DUPLEXES - 1)).toDouble(),
                (eightRow.interfaceCrossovers - (DUPLEXES - 1)).toDouble(), true,
                "a connected sheet needs one retained crossover on each of its D - 1 " +
                        "interfaces, so the hinge budget is 35 of 49 (71.4 %) against 42 of 56 " +
                        "(75.0 %) — C-0054's 75 % is an EIGHT-column number"
            )
        )
        C0087_INCORPORATION.forEach { incorporation ->
            add(
                price(
                    ("P(some interface loses every crossover) at C-0087's " +
                            incorporation + " incorporation"),
                    "C-0087 / C-0054",
                    severanceProbability(sevenPerInterface, incorporation),
                    severanceProbability(eightPerInterface, incorporation), true,
                    "(1 - p)^3 on a three-crossover interface against (1 - p)^4 on a " +
                            "four-crossover one, over fourteen interfaces. An empty interface " +
                            "takes the SERIES D_perp to exactly zero, so this is not a " +
                            "correction to a rigidity, it is the chance there is none"
                )
            )
        }
    }

    // ------------------------------------------------- the measured comparison, four phases
    println("T-171 — the four candidate phases, measured on C-0009's grillage ...")

    val buildableCase = T171Case(
        "38.08 nm, 24 bp arm, row-end admitted", buildable, buildableArm, true, sheet, smooth, rim
    )
    val nominalCase = T171Case(
        "40.00 nm, C-0039's elastica arm, interior", nominal, elasticaArm, false,
        sheet, smooth, rim
    )

    val candidates = (buildableLedger.richestUpwardInventory + buildableLedger.centroSymmetric)
        .distinct().sorted()

    data class T171Solved(
        val phase: Int,
        val placement: UpwardArmPlacement,
        val dishing: Double,
        val evaluations: Int,
        val free: Double
    )

    val solved = candidates.map { phase ->
        val solve = buildableCase.PhaseSolve(phase)
        var evaluations = 0
        val found = descentStarts(buildableCase, phase, solve.sites).map { start ->
            descendPlacement(
                start, solve.sites, buildableCase.arm, buildableCase.edgeX,
                minimumPerRow = 1, maximumPerRow = 3
            ) { placement ->
                evaluations++
                solve.dishing(placement)
            }
        }.minWith(compareBy({ it.objective }, { it.placement.key }))
        println(
            ("  phase %2d  cols %d  sites %2d  free %7.4f  descent %7.4f  evals %6d")
                .format(
                    phase, rasterColumnLayout(phase, sheet, buildable, true).size,
                    solve.sites.sumOf { it.size }, solve.freeDishing, found.objective, evaluations
                )
        )
        T171Solved(phase, found.placement, found.objective, evaluations, solve.freeDishing)
    }

    val phaseRecords = solved.map { record ->
        val row = buildableCensus.single { it.phaseBasePairs == record.phase }
        val stations = record.placement.stations(DUPLEXES, sheet.interhelicalDistance)
        val supports = couplingSupports(stations, MANDATE)
        val coupled = buildableCase.host(record.phase, supports).solve(buildableCase.solvedField)
        val free = buildableCase.host(record.phase).solve(buildableCase.solvedField)
        val thermal = buildableCase.host(record.phase).thermalFluctuation()
        val perPath = MANDATE / stations.size
        T171PhaseRecord(
            phaseBasePairs = record.phase,
            role = when {
                row.upwardSites == buildableLedger.maximumUpwardSites && row.centroSymmetric ->
                    "richest AND centro-symmetric"
                row.upwardSites == buildableLedger.maximumUpwardSites -> "richest upward inventory"
                row.centroSymmetric -> "eight-column host and centro-symmetric"
                else -> "neither"
            },
            columns = row.columns,
            interfaceCrossovers = row.interfaceCrossovers,
            upwardSites = row.upwardSites,
            centroSymmetric = row.centroSymmetric,
            freeDishingOverStroke = record.free,
            descentDishingOverStroke = record.dishing,
            flatAtTenPercent = record.dishing < FLATNESS_TOLERANCE,
            descentEvaluations = record.evaluations,
            freePeakCrossoverForce = free.peakCrossoverForce,
            freePeakDuplexShear = free.peakDuplexShear,
            coupledPeakCrossoverForce = coupled.peakCrossoverForce,
            coupledPeakDuplexShear = coupled.peakDuplexShear,
            thermalDishingRms = thermal.dishingRms,
            thermalCentreRms = thermal.centreRms,
            peakPathForce = perPath * Gen1Tile.ACCEPTABLE_STROKE,
            peakThermalPathForce = perPathThermalForces(List(stations.size) { perPath }).max(),
            key = record.placement.key
        )
    }

    val bestEightColumn = phaseRecords
        .filter { it.phaseBasePairs in buildableLedger.centroSymmetric }
        .minBy { it.descentDishingOverStroke }
    val bestRichest = phaseRecords
        .filter { it.phaseBasePairs in buildableLedger.richestUpwardInventory }
        .minBy { it.descentDishingOverStroke }

    // add the measured channels to the sheet price, at matched search effort
    val sevenMeasured = phaseRecords.single { it.phaseBasePairs == bestRichest.phaseBasePairs }
    val eightMeasured = phaseRecords.single { it.phaseBasePairs == bestEightColumn.phaseBasePairs }
    val sheetPrice = sheetPriceClosed + listOf(
        price(
            "free-tile peak crossover force under C-0022's solved load [pN]", "C-0016 / C-0009",
            sevenMeasured.freePeakCrossoverForce, eightMeasured.freePeakCrossoverForce, false,
            "the host and the load only, with NO coupling — a pure sheet-side reading of " +
                    "C-0015's per-load-path lever, and the first time it has been read at a phase"
        ),
        price(
            "free-tile peak duplex shear under the same load [pN]", "C-0016 / C-0009",
            sevenMeasured.freePeakDuplexShear, eightMeasured.freePeakDuplexShear, false,
            "C-0015: the crossover and the duplex run in opposite directions across the cell"
        ),
        price(
            "thermal dishing RMS of the free host [nm]", "C-0010",
            sevenMeasured.thermalDishingRms, eightMeasured.thermalDishingRms, false,
            "equipartition on the assembled stiffness matrix, exact for the harmonic model; " +
                    "C-0010's channel, re-read at a phase"
        ),
        price(
            "thermal centre RMS of the free host [nm]", "C-0010",
            sevenMeasured.thermalCentreRms, eightMeasured.thermalCentreRms, false,
            "the quietest point on the tile (CLAUDE.md), quoted with its point"
        ),
        price(
            "best 34-root dishing over the free stroke, matched descent effort", "C-0063 / C-0090",
            sevenMeasured.descentDishingOverStroke, eightMeasured.descentDishingOverStroke, false,
            "the same four starts and the same descent at both phases, because a comparison " +
                    "between an exhaustive enumeration and a descent is not a comparison"
        )
    )

    // ------------------------------------------- the tie-count arithmetic, which decides it
    val tiesRichest = buildableLedger.maximumUpwardSites
    val tiesEight = eightMeasured.upwardSites
    fun inventoryFactor(slope: Double) =
        Math.exp(-slope * ln(tiesRichest.toDouble() / tiesEight.toDouble()))
    val phaseCostMeasured =
        sevenMeasured.descentDishingOverStroke / eightMeasured.descentDishingOverStroke
    val phaseCostC0098 = C0098_RICHEST_PHASE_P90 / C0098_PHASE_24_P90
    val tieCount = listOf(
        T171TieCountRecord(
            statement = "what the richest inventory buys on C-0093's abstract-grid slope",
            slope = C0093_REDUNDANCY_SLOPE,
            slopeOwner = "C-0093",
            tiesAtRichest = tiesRichest,
            tiesAtEightColumn = tiesEight,
            factorTheInventoryBuys = inventoryFactor(C0093_REDUNDANCY_SLOPE),
            factorThePhaseCosts = phaseCostMeasured,
            worthIt = inventoryFactor(C0093_REDUNDANCY_SLOPE) > phaseCostMeasured
        ),
        T171TieCountRecord(
            statement = "the same on C-0098's slope measured on the REAL upward lattice",
            slope = C0098_REDUNDANCY_SLOPE,
            slopeOwner = "C-0098",
            tiesAtRichest = tiesRichest,
            tiesAtEightColumn = tiesEight,
            factorTheInventoryBuys = inventoryFactor(C0098_REDUNDANCY_SLOPE),
            factorThePhaseCosts = phaseCostMeasured,
            worthIt = inventoryFactor(C0098_REDUNDANCY_SLOPE) > phaseCostMeasured
        ),
        T171TieCountRecord(
            statement = "C-0098's own MEASURED comparison at 40 nm, 60 ties against 53, " +
                    "10 000 dropout realisations",
            slope = C0098_REDUNDANCY_SLOPE,
            slopeOwner = "C-0098",
            tiesAtRichest = 60,
            tiesAtEightColumn = 53,
            factorTheInventoryBuys = 1.0 / phaseCostC0098,
            factorThePhaseCosts = phaseCostC0098,
            worthIt = false
        )
    )

    // ------------------------------------------------------------------ the two gate solves
    println("T-171 — reproducing the two published optima from their own placement keys ...")
    val nominalSolve = nominalCase.PhaseSolve(24)
    val c0063Reproduced = nominalSolve.dishing(
        upwardPlacementFromKey(c0063Key, 24, elasticaArm, nominal, nominalSolve.sites)
    )
    val buildableSolveEight = buildableCase.PhaseSolve(8)
    val c0090Reproduced = buildableSolveEight.dishing(
        upwardPlacementFromKey(c0090Key, 8, buildableArm, buildable, buildableSolveEight.sites)
    )

    // ------------------------------------------------------------------ the recommendation
    val recommendedPhase = bestEightColumn.phaseBasePairs
    val recommendedRow = buildableCensus.single { it.phaseBasePairs == recommendedPhase }
    val recommendation = T171RecommendationRecord(
        edgeX = buildable,
        phaseBasePairs = recommendedPhase,
        columns = recommendedRow.columns,
        upwardSites = recommendedRow.upwardSites,
        centroSymmetric = recommendedRow.centroSymmetric,
        demandDropped = "the richest upward inventory (C-0098 / CH-0113)",
        costOfDroppingIt = (recommendedRow.upwardSites.toString() + " upward stations against " +
                tiesRichest + " — a " +
                "%.1f %% smaller tie inventory, worth at most %.3fx on C-0093's own redundancy " +
                "slope and %.3fx on C-0098's measured one").format(
                    100.0 * (tiesRichest - recommendedRow.upwardSites) / tiesRichest,
                    inventoryFactor(C0093_REDUNDANCY_SLOPE),
                    inventoryFactor(C0098_REDUNDANCY_SLOPE)
                ),
        dishingOverStroke = bestEightColumn.descentDishingOverStroke,
        flatAtTenPercent = bestEightColumn.flatAtTenPercent,
        statement = ("phase " + recommendedPhase + " at the buildable 38.08 nm width: eight " +
                "columns, centro-symmetric, and the flattest tile of the four candidates. The " +
                "three demands are irreconcilable at every width and the one to drop is the " +
                "inventory, because it is the only one whose owner has already measured it " +
                "losing")
    )

    // ------------------------------------------------------------------ convergence
    println("T-171 — convergence ...")
    val recommendedPlacement = solved.single { it.phase == recommendedPhase }.placement
    val recommendedSupports = couplingSupports(
        recommendedPlacement.stations(DUPLEXES, sheet.interhelicalDistance), MANDATE
    )
    val nested = listOf(1, 2, 4).map { subdivisions ->
        buildableCase.host(recommendedPhase, recommendedSupports, subdivisions)
            .solve(buildableCase.solvedField).peakDishing() / buildableCase.freeStroke
    }
    val grids = listOf(41, 81, 161).map { grid ->
        buildableCase.host(recommendedPhase, recommendedSupports)
            .solve(buildableCase.solvedField).peakDishing(grid) / buildableCase.freeStroke
    }
    val assembled = buildableCase.host(recommendedPhase, recommendedSupports)
        .solve(buildableCase.solvedField).peakDishing() / buildableCase.freeStroke
    val surrogateDishing = buildableCase.PhaseSolve(recommendedPhase).dishing(recommendedPlacement)

    val uniformResiduals = candidates.map { phase ->
        abs(
            buildableCase.host(phase).solve(buildableCase.uniformField).peakDishing()
        ) / buildableCase.freeStroke
    }
    check(uniformResiduals.max() < 1.0e-6) {
        "a uniform load on a uniform Winkler foundation must dish exactly zero on a free tile, " +
                "and the worst host here dished ${uniformResiduals.max()} of the free stroke"
    }

    val repeatSolve = buildableCase.PhaseSolve(recommendedPhase)
    val repeats = (1..2).map {
        descendPlacement(
            descentStarts(buildableCase, recommendedPhase, repeatSolve.sites).first(),
            repeatSolve.sites, buildableCase.arm, buildableCase.edgeX,
            minimumPerRow = 1, maximumPerRow = 3
        ) { repeatSolve.dishing(it) }
    }

    val convergence = listOf(
        T171ConvergenceRecord(
            "dishing/stroke of the recommended placement", "nested subdivisions 1 c 2 c 4",
            listOf(1.0, 2.0, 4.0), nested,
            twoSignificantDigits(abs(nested[2] - nested[1]) / nested[1]),
            "nested only, per CLAUDE.md — a subdivision of 3 moves a station off a node"
        ),
        T171ConvergenceRecord(
            "dishing/stroke of the recommended placement", "dishing sample grid",
            listOf(41.0, 81.0, 161.0), grids,
            twoSignificantDigits(abs(grids[2] - grids[1]) / grids[1]),
            "81 is the grid every published dishing in this programme is read on"
        ),
        T171ConvergenceRecord(
            "dishing/stroke of the recommended placement", "Woodbury surrogate vs the assembly",
            listOf(0.0, 1.0), listOf(surrogateDishing, assembled),
            twoSignificantDigits(abs(surrogateDishing - assembled) / assembled),
            "FALSIFIER: superposition is exact for a linear system, so any departure above " +
                    "round-off means the sweep was run on the wrong object"
        ),
        T171ConvergenceRecord(
            "the descent's own argmin", "repeat runs",
            listOf(1.0, 2.0), repeats.map { it.objective },
            twoSignificantDigits(abs(repeats[0].objective - repeats[1].objective)),
            if (repeats[0].placement.key == repeats[1].placement.key)
                "identical placement key — the argmin is rounded at the decision point"
            else "THE ARGMIN IS NOT DETERMINISTIC"
        ),
        T171ConvergenceRecord(
            "the uniform-load falsifier over every host swept",
            "the candidate phases", candidates.map { it.toDouble() }, uniformResiduals,
            twoSignificantDigits(uniformResiduals.max()),
            "a uniform load on a uniform Winkler foundation dishes EXACTLY zero on a free tile; " +
                    "the residual is the conditioning of the short end element the row-end " +
                    "inset creates, and it is asserted relative to the free stroke"
        )
    )

    // ------------------------------------------------------------------ reproductions
    fun reproduction(
        source: String, quantity: String, publishedValue: Double, reproduced: Double,
        strict: Boolean
    ) = T171ReproductionRecord(
        source, quantity, publishedValue, reproduced,
        twoSignificantDigits(abs(reproduced - publishedValue) / abs(publishedValue)), strict
    )

    val nominalCensusRows = censusByConvention.getValue(conventions[0].first)
    val reproductions = listOf(
        reproduction(
            "C-0063", "dishing/stroke at 40.00 nm, phase 24, from its own placement key",
            c0063Published, c0063Reproduced, true
        ),
        reproduction(
            "C-0090", "dishing/stroke at 38.08 nm, phase 8, from its own placement key",
            c0090Published, c0090Reproduced, true
        ),
        reproduction(
            "C-0098", "the lattice's richest upward inventory at 40.00 nm", 60.0,
            nominalLedger.maximumUpwardSites.toDouble(), true
        ),
        reproduction(
            "C-0098", "the number of phases attaining it at 40.00 nm", 10.0,
            nominalLedger.richestUpwardInventory.size.toDouble(), true
        ),
        reproduction(
            "C-0066 / C-0093", "the upward inventory at phase 24, 40.00 nm", 53.0,
            nominalCensusRows.single { it.phaseBasePairs == 24 }.upwardSites.toDouble(), true
        ),
        reproduction(
            "C-0015", "interface crossovers at an eight-column phase, 40.00 nm", 56.0,
            nominalCensusRows.first { it.columns == 8 }.interfaceCrossovers.toDouble(), true
        ),
        reproduction(
            "C-0015", "interface crossovers at a seven-column phase, 40.00 nm", 49.0,
            nominalCensusRows.first { it.columns == 7 }.interfaceCrossovers.toDouble(), true
        ),
        reproduction(
            "C-0015", "eight-column phases at 40.00 nm", 10.0,
            nominalLedger.eightColumnHost.size.toDouble(), true
        ),
        reproduction(
            "C-0063", "centro-symmetric phases at 40.00 nm", 2.0,
            nominalLedger.centroSymmetric.size.toDouble(), true
        ),
        reproduction(
            "C-0090", "eight-column phases at 38.08 nm with the row end admitted", 2.0,
            buildableLedger.eightColumnHost.size.toDouble(), true
        ),
        reproduction(
            "C-0055", "the self-consistent upward arm count", 34.0, C0055_ARM_COUNT.toDouble(),
            true
        ),
        reproduction("C-0086", "the buildable width [nm]", 38.08, buildable, false),
        reproduction(
            "C-0054", "the series/smeared D_perp ratio on a uniform lattice",
            (DUPLEXES.toDouble() / (DUPLEXES - 1)) * (DUPLEXES.toDouble() / (DUPLEXES - 1)),
            series(eightPerInterface) / smeared(eightPerInterface), true
        )
    )
    val worstStrict = reproductions.filter { it.strict }.maxOf { it.departure }
    check(worstStrict < 1.0e-8) {
        "a strict upstream reproduction departs by $worstStrict — the sweep is on the wrong object"
    }

    // ------------------------------------------------------------------ predicates, falsifiers
    val predicates = listOf(
        T171PredicateRecord(
            "P1 — the census is complete and the intersections are emitted",
            "32 phases at each of three width/convention readings, with all three demand sets " +
                    "and every pairwise intersection",
            (census.size.toString() + " census rows over " + demands.size +
                    " conventions; the three demands coincide at " +
                    demands.sumOf { it.allThree.size } + " phases in total")
        ),
        T171PredicateRecord(
            "P2 — the seven-column host is priced on the sheet side",
            "at least four channels that are not the coupling's dishing",
            (sheetPrice.size.toString() + " channels, " + sheetPrice.count { it.closedForm } +
                    " of them closed form: interface count, minimum interface redundancy, both " +
                    "D_perp readings, the anisotropy, C-0054's spendable budget, three " +
                    "severance probabilities, the free-tile crossover force and duplex shear, " +
                    "and two thermal amplitudes")
        ),
        T171PredicateRecord(
            "P3 — one phase is recommended, or the irreconcilability is stated",
            "a phase, or the demand to drop and the cost of dropping it",
            recommendation.statement
        ),
        T171PredicateRecord(
            "P4 — the consequences for the standing claims are stated",
            "C-0063, C-0090, C-0098 and C-0071's recommended element",
            "stated in C-0102's 'What this does to the standing claims', with CH-0118 raised " +
                    "against C-0090's row-end reading rather than written over it"
        ),
        T171PredicateRecord(
            "P5 — the two published optima reproduce",
            "C-0063's 0.0706145537 at 40.00 nm and C-0090's 0.0621469105 at 38.08 nm",
            ("departures " + "%.2e".format(reproductions[0].departure) + " and " +
                    "%.2e".format(reproductions[1].departure))
        )
    )

    val f1Fired = demands.any { it.allThree.isNotEmpty() }
    val f2Fired = abs(
        series(sevenPerInterface) / series(eightPerInterface) -
                smeared(sevenPerInterface) / smeared(eightPerInterface)
    ) < 1.0e-9
    val f3Fired = sevenMeasured.descentDishingOverStroke <
            eightMeasured.descentDishingOverStroke
    val f4Fired = reproductions.take(2).any { it.departure > 1.0e-8 }
    val f5Fired = uniformResiduals.max() >= 1.0e-6
    val rowEndStations = candidates.associateWith { rowEndUpwardStations(it, buildable, DUPLEXES) }
    val f6Fired = rowEndStations.values.all { it == 0 }

    val falsifiers = listOf(
        T171FalsifierRecord(
            "F1",
            "at the buildable width a phase is simultaneously richest, eight-column and " +
                    "centro-symmetric — there is no conflict to price",
            f1Fired,
            ("the richest set is " + buildableLedger.richestUpwardInventory +
                    " and the eight-column/centro-symmetric set is " +
                    buildableLedger.centroSymmetric + " — still disjoint, 2 against 2")
        ),
        T171FalsifierRecord(
            "F2",
            "the two D_perp readings of a seven-column sheet agree, so C-0054's harmonic-mean " +
                    "discipline says nothing here",
            f2Fired,
            ("smeared " + "%.9f".format(smeared(sevenPerInterface) / smeared(eightPerInterface)) +
                    " against series " +
                    "%.9f".format(series(sevenPerInterface) / series(eightPerInterface)) +
                    " — exactly 7/8 against 6/7, which is 48/49 apart")
        ),
        T171FalsifierRecord(
            "F3",
            "at matched search effort the richest phase is flatter than the eight-column one",
            f3Fired,
            ("phase " + eightMeasured.phaseBasePairs + " reads " +
                    "%.9f".format(eightMeasured.descentDishingOverStroke) + " against phase " +
                    sevenMeasured.phaseBasePairs + "'s " +
                    "%.9f".format(sevenMeasured.descentDishingOverStroke) + ", the same four " +
                    "starts and the same descent at both")
        ),
        T171FalsifierRecord(
            "F4",
            "the pipeline fails to reproduce C-0063's 0.0706145537 or C-0090's 0.0621469105",
            f4Fired,
            ("departures " + "%.2e".format(reproductions[0].departure) + " and " +
                    "%.2e".format(reproductions[1].departure) + ", each from the claim's own " +
                    "published placement key")
        ),
        T171FalsifierRecord(
            "F5",
            "a uniform load on a uniform Winkler foundation dishes non-zero on a free host",
            f5Fired,
            ("worst residual " + "%.2e".format(uniformResiduals.max()) +
                    " of the free stroke over the four candidate hosts")
        ),
        T171FalsifierRecord(
            "F6",
            "C-0090's 'the row-end crossover can never be an upward site, AT ANY PHASE' holds — " +
                    "admitting the row end changes no upward count anywhere",
            f6Fired,
            ("row-end upward stations by phase: " + rowEndStations +
                    ". At phases 8 and 24 the end plane's index is EVEN and it is a column; at " +
                    "0 and 16 it is ODD and it is fifteen upward stations, adding no column at " +
                    "all. CH-0118")
        )
    )

    val findings = listOf(
        ("THE CENSUS IS THE ANSWER AND IT COSTS NO SOLVE. The phase is one integer with 32 " +
                "values and each of the three demands is a count over it. At 40.00 nm the sets " +
                "are 10 / 10 / 2 and the richest is disjoint from the other two; at C-0086's " +
                "buildable 38.08 nm every one of them collapses to TWO — richest " +
                buildableLedger.richestUpwardInventory + ", eight-column " +
                buildableLedger.eightColumnHost + ", centro-symmetric " +
                buildableLedger.centroSymmetric + " — and the disjointness survives. " +
                "C-0090's collapse does NOT close the question; it sharpens it from ten against " +
                "ten to two against two."),
        ("THE SEVEN-COLUMN HOST'S SHEET-SIDE PRICE IS LARGER THAN THE SMEARED READING SAYS, AND " +
                "THE EXCESS IS EXACT. A seven-column sheet splits its columns 4/3 between the " +
                "two parities, so seven of its fourteen interfaces carry THREE crossovers. The " +
                "smeared D_perp is linear in the count and loses 7/8; the SERIES D_perp is a " +
                "harmonic mean and loses 6/7. The two differ by exactly 48/49, and the series " +
                "one is the one that describes a sheet bending across its interfaces."),
        ("AND THE THIN INTERFACES ARE WHERE FOLDING FAILS. Under C-0087's measured incorporation " +
                "the chance that SOME interface loses every crossover is " +
                "%.4g".format(
                    severanceProbability(eightPerInterface, 0.84)
                ) + " on an eight-column sheet and " +
                "%.4g".format(severanceProbability(sevenPerInterface, 0.84)) +
                " on a seven-column one at the mean 84 %, and " +
                "%.3g".format(severanceProbability(eightPerInterface, 0.48)) + " against " +
                "%.3g".format(severanceProbability(sevenPerInterface, 0.48)) +
                " at the 48 % C-0087 measures at the tile edge. An empty interface takes the " +
                "series D_perp to EXACTLY zero, so this is the probability that the sheet has " +
                "no across-helix rigidity at all, not a correction to it."),
        ("THE INVENTORY DEMAND IS WORTH LESS THAN THE HOST IT DEMANDS COSTS, ON EVERY SLOPE IN " +
                "THE CORPUS. Going from " + tiesEight + " ties to " + tiesRichest + " buys " +
                "%.3fx".format(inventoryFactor(C0093_REDUNDANCY_SLOPE)) +
                " on C-0093's abstract-grid redundancy slope and " +
                "%.3fx".format(inventoryFactor(C0098_REDUNDANCY_SLOPE)) +
                " on C-0098's slope measured on the real lattice. The phase it demands costs " +
                "%.3fx".format(phaseCostMeasured) + " in the tile's own flatness at matched " +
                "search effort, and C-0098's own 10 000-realisation dropout grading at 40 nm " +
                "measured " + "%.3fx".format(phaseCostC0098) + ". The demand loses by a factor " +
                "of " + "%.1f".format(
                    phaseCostMeasured / inventoryFactor(C0098_REDUNDANCY_SLOPE)
                ) + "."),
        ("C-0090'S ROW-END READING IS A STATEMENT ABOUT TWO PHASES AND IT IS QUOTED AS A " +
                "THEOREM. Its Deliverable 3 says an end plane has an even index, so 'the row-end " +
                "crossover can never be an upward site, at any phase' and admitting it 'adds " +
                "in-plane inventory and no stations'. A plane lands on the row end whenever " +
                "phase = -56 (mod 8), i.e. at 0, 8, 16 and 24; its INDEX is even only when the " +
                "congruence also holds modulo 16, i.e. at 8 and 24. At phases 0 and 16 the end " +
                "plane is odd, so it is not a column at all: admitting it adds FIFTEEN upward " +
                "stations and ZERO columns, taking the inventory 45 to 60. The claim's own code " +
                "computes this correctly and its prose inverts it — and the sentence is exactly " +
                "the one that would have dismissed this task's whole question. CH-0118."),
        ("THE RECOMMENDATION IS PHASE " + recommendedPhase + " AND THE DEMAND TO DROP IS THE " +
                "INVENTORY. It is the only one of the three whose owner has already measured it " +
                "losing (C-0098, 0.487309625 against 0.385192562 with seven more ties), the " +
                "only one that is not also a structural property of the host, and the only one " +
                "whose loss is bounded by a published slope. Dropping either of the other two " +
                "costs an eight-column host — 12.5 % of the smeared and 14.3 % of the series " +
                "D_perp, 7 of C-0054's 42 spendable crossovers and a " +
                "%.1fx".format(
                    severanceProbability(sevenPerInterface, 0.84) /
                            severanceProbability(eightPerInterface, 0.84)
                ) + " severance probability — or the exhaustive centro-symmetric family that " +
                "supplies the best placement anybody has found."),
        ("A FLATNESS VERDICT NEEDS ITS LOAD CASE AND ITS STATE, AND SO DOES A PHASE. Every " +
                "number here is read at C-0022's SOLVED 2 mM / 10 nm / 0.192 V profile, carried " +
                "to 38.08 nm exactly as C-0090 carries it and measured by C-0100 to be worth " +
                "0.0712 % of the flatness — three decades below the " +
                "%.2fx".format(phaseCostMeasured) + " this task is about.")
    )

    val result = T171Result(
        task = "T-171",
        leaf = "A8.2",
        conditions = "T = 300 K, k_BT = 4.141947 pN nm; aqueous 2 mM MgCl2; single-layer " +
                "square-lattice Rothemund sheet, 15 duplexes at the SAXS 2.69 nm (40.35 nm " +
                "across the helices, unchanged), 0.34 nm rise, 32/3 bp per turn, 16 bp column " +
                "pitch; along-helix width 40.00 nm (§3) and 38.08 nm (C-0086's 112 bp); " +
                "C-0055's 34 upward roots on C-0039's arm quantised to 24 rises; C-0017's " +
                "33.3333333 pN/nm mandate as a SUM; C-0022's solved edge profile at 2 mM, a " +
                "10 nm gap and 0.192 V, carried unchanged (C-0100: worth 0.0712 %); C-0001's " +
                "foundation secant; dishing on an 81 x 81 grid over the free-tile stroke; flat " +
                "means below T-5b's 0.10 CONVENTION",
        decision = ("The three demands on the one phase variable are IRRECONCILABLE at both " +
                "widths, and the one to drop is the richest upward inventory. At C-0086's " +
                "buildable 38.08 nm the three sets collapse from 10 / 10 / 2 to 2 / 2 / 2 and " +
                "stay disjoint: richest " + buildableLedger.richestUpwardInventory +
                ", eight-column and centro-symmetric " + buildableLedger.centroSymmetric +
                ". PHASE " + recommendedPhase + " IS RECOMMENDED, at " +
                "%.9f".format(bestEightColumn.descentDishingOverStroke) + " of the free stroke " +
                "on the same descent that reads " +
                "%.9f".format(sevenMeasured.descentDishingOverStroke) + " at the richest phase " +
                sevenMeasured.phaseBasePairs + "."),
        census = census,
        demands = demands,
        sheetPrice = sheetPrice,
        phases = phaseRecords,
        tieCount = tieCount,
        recommendation = recommendation,
        convergence = convergence,
        reproductions = reproductions,
        predicates = predicates,
        falsifiers = falsifiers,
        findings = findings,
        parameters = mapOf(
            "nominalEdgeX" to nominal,
            "buildableEdgeX" to buildable,
            "lengthY" to lengthY,
            "duplexes" to DUPLEXES.toDouble(),
            "armCount" to C0055_ARM_COUNT.toDouble(),
            "elasticaArm" to elasticaArm,
            "buildableArm" to buildableArm,
            "mandate" to MANDATE,
            "flatnessTolerance" to FLATNESS_TOLERANCE,
            "freeStrokeNominal" to nominalCase.freeStroke,
            "freeStrokeBuildable" to buildableCase.freeStroke,
            "crossoverHingeStiffness" to hinge,
            "alongHelixRigidity" to sheet.alongHelixRigidity,
            "recommendedPhase" to recommendedPhase.toDouble(),
            "recommendedDishingOverStroke" to bestEightColumn.descentDishingOverStroke,
            "richestPhase" to sevenMeasured.phaseBasePairs.toDouble(),
            "richestPhaseDishingOverStroke" to sevenMeasured.descentDishingOverStroke,
            "phaseCostMeasured" to phaseCostMeasured,
            "phaseCostC0098AtFortyNanometres" to phaseCostC0098,
            "inventoryFactorOnC0093Slope" to inventoryFactor(C0093_REDUNDANCY_SLOPE),
            "inventoryFactorOnC0098Slope" to inventoryFactor(C0098_REDUNDANCY_SLOPE),
            "tiesAtRichestPhase" to tiesRichest.toDouble(),
            "tiesAtRecommendedPhase" to tiesEight.toDouble(),
            "smearedRigidityRatio" to smeared(sevenPerInterface) / smeared(eightPerInterface),
            "seriesRigidityRatio" to series(sevenPerInterface) / series(eightPerInterface),
            "severanceSevenAtMeanIncorporation" to severanceProbability(sevenPerInterface, 0.84),
            "severanceEightAtMeanIncorporation" to severanceProbability(eightPerInterface, 0.84),
            "rowEndUpwardStationsAtRichestPhase" to
                    rowEndUpwardStations(sevenMeasured.phaseBasePairs, buildable, DUPLEXES)
                        .toDouble(),
            "c0063Published" to c0063Published,
            "c0063Reproduced" to c0063Reproduced,
            "c0090Published" to c0090Published,
            "c0090Reproduced" to c0090Reproduced,
            "descentEvaluations" to phaseRecords.sumOf { it.descentEvaluations }.toDouble()
        )
    )

    val output = File("gpd/results/T-171-crossover-phase-selection.json")
    output.parentFile.mkdirs()
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult() as JsonObject)
        )
    )

    println()
    println("the demand ledger")
    result.demands.forEach {
        println("  ${it.convention}")
        println("    richest ${it.maximumUpwardSites}: ${it.richestUpwardInventory}")
        println("    most columns ${it.maximumColumns}: ${it.mostColumns}")
        println("    centro-symmetric: ${it.centroSymmetric}")
        println("    all three: ${it.allThree}")
    }
    println()
    println("the sheet-side price of a seven-column host")
    result.sheetPrice.forEach {
        println(
            "  %-62s 7col %12.6g  8col %12.6g  ratio %8.5f %s".format(
                it.quantity.take(62), it.atSevenColumns, it.atEightColumns, it.sevenOverEight,
                if (it.closedForm) "(closed form)" else ""
            )
        )
    }
    println()
    println("the candidate phases")
    result.phases.forEach {
        println(
            "  phase %2d  %-34s cols %d  sites %2d  descent %8.5f  flat %s".format(
                it.phaseBasePairs, it.role, it.columns, it.upwardSites,
                it.descentDishingOverStroke, it.flatAtTenPercent
            )
        )
    }
    println()
    println("upstream reproductions")
    result.reproductions.forEach {
        println(
            "  %-16s %-64s %14.9g vs %14.9g  %8.2e %s".format(
                it.source, it.quantity.take(64), it.published, it.reproduced, it.departure,
                if (it.strict) "" else "(non-strict)"
            )
        )
    }
    println()
    println("falsifiers")
    result.falsifiers.forEach {
        println("  %s %-5s %s".format(it.name, if (it.fired) "FIRED" else "no", it.outcome))
    }
    println()
    result.predicates.forEach { println("  ${it.name}: ${it.verdict}") }
    println()
    result.findings.forEach { println("  * $it"); println() }
    println("written to ${output.path} in ${(System.currentTimeMillis() - started) / 1000} s")
}
