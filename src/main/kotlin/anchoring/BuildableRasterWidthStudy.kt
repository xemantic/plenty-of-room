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
import com.xemantic.nano.plentyofroom.coupling.couplingSupports
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.coupling.perPathStiffnessCeiling
import com.xemantic.nano.plentyofroom.coupling.perPathThermalForces
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_COUNT
import com.xemantic.nano.plentyofroom.structure.C0055_ARM_LENGTH
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.DEPARTURE_DIGITS_BY_KEY
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.OrigamiSheet
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PointSupport
import com.xemantic.nano.plentyofroom.structure.PressureField
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
 * `T-153` — the Gen-1 tile at the buildable seamless raster width, 112 bp = 38.08 nm.
 *
 * Emits `gpd/results/T-153-buildable-raster-width.json`.
 *
 * Reads `gpd/results/T-3b-tile-edge-load-profile.json` (`C-0022`'s solved edge profile) and
 * `gpd/results/T-125-upward-root-placement.json` (`C-0063`'s published optima, as the gate).
 */

private const val DUPLEXES = 15
private const val FLATNESS_TOLERANCE = 0.10
private const val RIM_STANDOFF = 1.0
private val MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

// ---------------------------------------------------------------------------------------------
// the records — prefixed with the task, because study records are package scoped (CLAUDE.md)
// ---------------------------------------------------------------------------------------------

@Serializable
private data class T153WidthRecord(
    val rowBasePairs: Int,
    val widthNanometres: Double,
    val admissible: Boolean,
    val note: String
)

@Serializable
private data class T153InvariantRecord(
    val quantity: String,
    val owner: String,
    val atNominalWidth: Double,
    val atBuildableWidth: Double,
    val departure: Double,
    val invariantByConstruction: Boolean,
    val why: String
)

@Serializable
private data class T153PhaseRecord(
    val edgeX: Double,
    val phaseBasePairs: Int,
    val admitRowEnd: Boolean,
    val columns: Int,
    val interfaceSites: Int,
    val upwardSites: Int,
    val armsPlacedElasticaArm: Int,
    val armsPlacedBuildableArm: Int,
    val centroSymmetricCapable: Boolean,
    val columnOnRowEnd: Boolean
)

@Serializable
private data class T153LatticeRecord(
    val phaseBasePairs: Int,
    val row: Int,
    val sitesAtNominalWidth: List<Double>,
    val sitesAtBuildableWidth: List<Double>,
    val departure: Double
)

@Serializable
private data class T153CeilingRecord(
    val name: String,
    val owner: String,
    val atNominalWidth: Double,
    val atBuildableWidth: Double,
    val bindingAtNominal: Boolean,
    val bindingAtBuildable: Boolean,
    val note: String
)

@Serializable
private data class T153PlacementRecord(
    val case: String,
    val edgeX: Double,
    val armLength: Double,
    val admitRowEnd: Boolean,
    val phaseBasePairs: Int,
    val columns: Int,
    val enumerated: Int,
    val bestDishingOverStroke: Double,
    val worstDishingOverStroke: Double,
    val medianDishingOverStroke: Double,
    val freeDishingOverStroke: Double,
    val flatAtTenPercent: Boolean,
    val beatsNoCoupling: Boolean,
    val bestKey: String,
    val exhaustive: Boolean
)

@Serializable
private data class T153DescentRecord(
    val phaseBasePairs: Int,
    val columns: Int,
    val upwardSites: Int,
    val freeDishingOverStroke: Double,
    val bestDishingOverStroke: Double,
    val flatAtTenPercent: Boolean,
    val evaluations: Int,
    val key: String
)

@Serializable
private data class T153MarginRecord(
    val owner: String,
    val quantity: String,
    val atNominalWidth: String,
    val atBuildableWidth: String,
    val moves: Boolean,
    val note: String
)

@Serializable
private data class T153ForceRecord(
    val case: String,
    val stations: Int,
    val dishingOverStroke: Double,
    val flatAtTenPercent: Boolean,
    val peakPathForce: Double,
    val peakThermalForce: Double,
    val peakCrossoverForce: Double,
    val peakDuplexShear: Double,
    val withinUnzipAllowable: Boolean
)

@Serializable
private data class T153ConvergenceRecord(
    val quantity: String,
    val parameter: String,
    val values: List<Double>,
    val results: List<Double>,
    val departure: Double,
    val note: String
)

@Serializable
private data class T153ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double,
    val strict: Boolean
)

@Serializable
private data class T153PredicateRecord(
    val name: String,
    val statement: String,
    val verdict: String
)

@Serializable
private data class T153FalsifierRecord(
    val name: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T153Result(
    val task: String,
    val leaf: String,
    val conditions: String,
    val decision: String,
    val admissibleWidths: List<T153WidthRecord>,
    val invariants: List<T153InvariantRecord>,
    val phaseCensus: List<T153PhaseRecord>,
    val stationLattice: List<T153LatticeRecord>,
    val armCeilings: List<T153CeilingRecord>,
    val placements: List<T153PlacementRecord>,
    val descent: List<T153DescentRecord>,
    val planMargins: List<T153MarginRecord>,
    val forces: List<T153ForceRecord>,
    val convergence: List<T153ConvergenceRecord>,
    val reproductions: List<T153ReproductionRecord>,
    val predicates: List<T153PredicateRecord>,
    val falsifiers: List<T153FalsifierRecord>,
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

/** `C-0063`'s published centro-symmetric optima, read from its result file rather than retyped. */
private fun c0063SymmetricOptima(file: File): Map<Int, Double> {
    require(file.exists()) { "C-0063's result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("symmetricFamily").jsonArray.map { it.jsonObject }
        .associate {
            it.getValue("phaseBasePairs").jsonPrimitive.content.toInt() to
                    it.getValue("bestDishingOverStroke").jsonPrimitive.content.toDouble()
        }
}

// ---------------------------------------------------------------------------------------------
// one width, one arm reading, one end-of-row convention
// ---------------------------------------------------------------------------------------------

private class T153Case(
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

    /** The free plate's mean descent under the uniform field — the stroke every dishing is over. */
    val freeStroke: Double = PlateOnFoundation(
        sheet.plate(edgeX, lengthY), Gen1Tile.FOUNDATION_SECANT, emptyList(), basisDegree = 12
    ).solve(uniformField).meanDeflection

    fun columns(phase: Int): CrossoverLayout =
        rasterColumnLayout(phase, sheet, edgeX, admitRowEnd, inset)

    fun sites(phase: Int): List<List<Double>> =
        rasterUpwardSites(
            phase, edgeX, DUPLEXES, admitRowEnd, Gen1Tile.RISE_PER_BASE_PAIR, inset
        )

    fun host(phase: Int, supports: List<PointSupport> = emptyList(), subdivisions: Int = 2) =
        OrigamiGrillage(
            sheet = sheet,
            lengthX = edgeX,
            beamCount = DUPLEXES,
            foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
            columns = columns(phase),
            subdivisions = subdivisions,
            supports = supports
        )

    inner class PhaseSolve(val phase: Int) {
        val sites = this@T153Case.sites(phase)
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

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

fun main() {
    val started = System.currentTimeMillis()
    val sheet = origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)
    val rise = Gen1Tile.RISE_PER_BASE_PAIR
    val pitch = Gen1Tile.CROSSOVER_SPACING_SHEET_BP * rise
    val nominal = Gen1Tile.EDGE_X
    val buildable = BUILDABLE_RASTER_WIDTH
    val elasticaArm = C0055_ARM_LENGTH
    val buildableArm = quantisedToRise(elasticaArm)
    val count = C0055_ARM_COUNT
    val duplex = OrigamiDuplex.INTERHELICAL

    println("T-153 — reading C-0022's solved load and C-0063's published optima ...")
    val (smooth, rim) = solvedProfile(File("gpd/results/T-3b-tile-edge-load-profile.json"))
    val published = c0063SymmetricOptima(File("gpd/results/T-125-upward-root-placement.json"))

    // ------------------------------------------------------- the cheap bounds, before any solve
    println("T-153 — the cheap bounds, which run before any solve ...")

    val admissibleWidths = listOf(16, 48, 80, 112, 118, 144).map { bp ->
        T153WidthRecord(
            rowBasePairs = bp,
            widthNanometres = bp * rise,
            admissible = isOddHalfTurnSeparation(bp),
            note = when (bp) {
                118 -> "the nominal 40.0 nm rounds here and is NOT admissible"
                112 -> "the nearest admissible width to 40.0 nm — this task's subject"
                else -> "an odd multiple of the 16 bp crossover spacing"
            }
        )
    }
    check(admissibleWidths.single { it.rowBasePairs == 112 }.admissible)
    check(!admissibleWidths.single { it.rowBasePairs == 118 }.admissible)

    val symmetricNominal = centroSymmetricUpwardPhases(nominal, DUPLEXES)
    val symmetricBuildable = centroSymmetricUpwardPhases(buildable, DUPLEXES)
    val endOfRowPhases = endOfRowColumnPhases(BUILDABLE_RASTER_ROW_BASE_PAIRS)

    fun capacity(edgeX: Double, phase: Int, arm: Double, admitRowEnd: Boolean): Int {
        val lattice = rasterUpwardSites(phase, edgeX, DUPLEXES, admitRowEnd)
        return lattice.sumOf { row ->
            if (row.isEmpty()) 0
            else minOf(3, maximumRootedElementsInRow(row, arm, edgeX, duplex))
        }
    }

    val invariants = listOf(
        T153InvariantRecord(
            "the across-helix span", "§3 / Fischer et al. (2016)",
            DUPLEXES * Gen1Tile.INTERHELICAL_SHEET, DUPLEXES * Gen1Tile.INTERHELICAL_SHEET, 0.0,
            true,
            "the raster rule binds the distance between successive SCAFFOLD crossovers, which " +
                    "in a boustrophedon are the two ends of ONE ROW — an along-helix length. " +
                    "The across-helix span is a COUNT of duplexes and the scaffold does not " +
                    "raster along it"
        ),
        T153InvariantRecord(
            "the duplex count", "§3", DUPLEXES.toDouble(), DUPLEXES.toDouble(), 0.0, true,
            "same reason: a count across the helices"
        ),
        T153InvariantRecord(
            "the upward root pitch", "C-0055", pitch, pitch, 0.0, true,
            "32 bp x the rise; an along-helix pitch, not a tile dimension"
        ),
        T153InvariantRecord(
            "C-0069's plan budget, pitch - d", "C-0069",
            inboardArmCeiling(pitch, duplex), inboardArmCeiling(pitch, duplex), 0.0, true,
            "a difference of two lattice constants; it contains no tile width at all"
        ),
        T153InvariantRecord(
            "C-0072's identity M = p - d - L", "C-0072 / C-0066",
            inboardArmCeiling(pitch, duplex) - elasticaArm,
            inboardArmCeiling(pitch, duplex) - elasticaArm, 0.0, true,
            "the same subtraction regrouped; C-0072 proved the two groupings agree to 4.4e-16"
        ),
        T153InvariantRecord(
            "C-0063's count vector, rows carrying three arms", "C-0063",
            rowsCarryingThreeArms(count, DUPLEXES, 3).toDouble(),
            rowsCarryingThreeArms(count, DUPLEXES, 3).toDouble(), 0.0, true,
            "3a + 2(15 - a) = 34 contains the duplex count and nothing else"
        ),
        T153InvariantRecord(
            "the centro-symmetric phases", "C-0063",
            symmetricNominal.sumOf { it.toDouble() }, symmetricBuildable.sumOf { it.toDouble() },
            0.0, false,
            "computed, not asserted: {" + symmetricNominal.joinToString(", ") + "} against {" +
                    symmetricBuildable.joinToString(", ") + "}"
        ),
        T153InvariantRecord(
            "C-0049's per-path stiffness ceiling", "C-0049",
            perPathStiffnessCeiling(Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE),
            perPathStiffnessCeiling(Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE),
            0.0, true, "an allowable over a stroke; no geometry in it"
        ),
        T153InvariantRecord(
            "C-0017's mandate", "C-0017", MANDATE, MANDATE, 0.0, true,
            "100 pN over 3 nm; a placement equality, not a pressure"
        ),
        T153InvariantRecord(
            "the footprint", "§3", nominal * DUPLEXES * Gen1Tile.INTERHELICAL_SHEET,
            buildable * DUPLEXES * Gen1Tile.INTERHELICAL_SHEET,
            abs(buildable - nominal) / nominal, false,
            "-4.8 %, so §3's 100 pN as a pressure is +5.0 % — but the dishing is reported over " +
                    "the free stroke, which carries the same factor, so the RATIO does not move " +
                    "with the level at all"
        )
    )
    invariants.filter { it.invariantByConstruction }.forEach {
        check(abs(it.atBuildableWidth - it.atNominalWidth) < 1.0e-9) {
            "${it.quantity} is declared invariant by construction and moved by " +
                    "${it.atBuildableWidth - it.atNominalWidth}"
        }
    }

    // ------------------------------------------------------------------ the phase census
    println("T-153 — the phase census at both widths and both end-of-row conventions ...")
    val phaseCensus = buildList {
        listOf(
            Triple(nominal, false, "nominal, interior"),
            Triple(buildable, false, "buildable, interior"),
            Triple(buildable, true, "buildable, end-of-row admitted")
        ).forEach { (edgeX, admit, _) ->
            (0 until 32).forEach { phase ->
                val inventory = rasterSiteInventory(phase, edgeX, DUPLEXES, admit)
                add(
                    T153PhaseRecord(
                        edgeX = edgeX,
                        phaseBasePairs = phase,
                        admitRowEnd = admit,
                        columns = rasterColumnLayout(phase, sheet, edgeX, admit).size,
                        interfaceSites = inventory.interfaceSites,
                        upwardSites = inventory.upwardSites,
                        armsPlacedElasticaArm = capacity(edgeX, phase, elasticaArm, admit),
                        armsPlacedBuildableArm = capacity(edgeX, phase, buildableArm, admit),
                        centroSymmetricCapable =
                            phase in centroSymmetricUpwardPhases(edgeX, DUPLEXES),
                        columnOnRowEnd = admit && phase in endOfRowPhases
                    )
                )
            }
        }
    }

    // ------------------------------------------------------- the station lattice, phases 8 and 24
    val stationLattice = symmetricBuildable.flatMap { phase ->
        val wide = upwardRootLattice(phase, nominal, DUPLEXES)
        val narrow = upwardRootLattice(phase, buildable, DUPLEXES)
        (0 until DUPLEXES).map { row ->
            T153LatticeRecord(
                phaseBasePairs = phase,
                row = row,
                sitesAtNominalWidth = wide[row],
                sitesAtBuildableWidth = narrow[row],
                departure = if (wide[row].size != narrow[row].size) Double.MAX_VALUE
                else wide[row].zip(narrow[row]).maxOf { (a, b) -> abs(a - b) }
            )
        }
    }
    val latticeDeparture = stationLattice.maxOf { it.departure }

    // ------------------------------------------------------------------ the arm ceilings
    val inboard = inboardArmCeiling(pitch, duplex)
    val crossing = armCeilingCrossoverWidth(pitch, duplex)
    val armCeilings = listOf(
        T153CeilingRecord(
            "inboard: the pair of roots one pitch apart", "C-0069",
            inboard, inboard,
            inboard < outboardArmCeiling(pitch, nominal),
            inboard < outboardArmCeiling(pitch, buildable),
            "pitch - d; carries no tile width"
        ),
        T153CeilingRecord(
            "outboard: the arm that must stay over the tile", "T-153",
            outboardArmCeiling(pitch, nominal), outboardArmCeiling(pitch, buildable),
            outboardArmCeiling(pitch, nominal) < inboard,
            outboardArmCeiling(pitch, buildable) < inboard,
            ("edgeX/2 - pitch; carries no interhelical distance. The two cross at " +
                    "edgeX = 2(2p - d) = %.4f nm, and the buildable width falls %.3f base pairs " +
                    "below that").format(crossing, (crossing - buildable) / rise)
        ),
        T153CeilingRecord(
            "the binding one", "T-153",
            minOf(inboard, outboardArmCeiling(pitch, nominal)),
            minOf(inboard, outboardArmCeiling(pitch, buildable)),
            true, true,
            "the OWNER of the binding ceiling changes with the width, which is the whole of " +
                    "what 4.8 % does to this branch"
        )
    )

    val threeSiteRow = listOf(-pitch, 0.0, pitch)
    val clearanceNominalElastica = rowEdgeClearance(threeSiteRow, elasticaArm, 3, nominal, duplex)
    val clearanceBuildableElastica =
        rowEdgeClearance(threeSiteRow, elasticaArm, 3, buildable, duplex)
    val clearanceBuildableQuantised =
        rowEdgeClearance(threeSiteRow, buildableArm, 3, buildable, duplex)
    val overhang = pitch + elasticaArm - buildable / 2.0

    // ------------------------------------------------------------------ the placement sweeps
    println("T-153 — the placement sweeps ...")

    val cases = listOf(
        T153Case(
            "GATE — nominal 40.00 nm, C-0039's elastica arm, interior",
            nominal, elasticaArm, false, sheet, smooth, rim
        ),
        T153Case(
            "RECOMMENDED — buildable 38.08 nm, 24 bp arm, end-of-row admitted",
            buildable, buildableArm, true, sheet, smooth, rim
        ),
        T153Case(
            "BRACKET — buildable 38.08 nm, 24 bp arm, interior (row-end column refused)",
            buildable, buildableArm, false, sheet, smooth, rim
        ),
        T153Case(
            "UNBUILDABLE ARM — buildable 38.08 nm, elastica arm, end-of-row admitted",
            buildable, elasticaArm, true, sheet, smooth, rim
        )
    )

    val placements = ArrayList<T153PlacementRecord>()
    val bestByCase = HashMap<String, Triple<UpwardArmPlacement, Double, T153Case>>()
    cases.forEach { case ->
        symmetricBuildable.forEach { phase ->
            val solve = case.PhaseSolve(phase)
            val values = ArrayList<Double>()
            var best: Pair<UpwardArmPlacement, Double>? = null
            centroSymmetricPlacements(
                phase, case.edgeX, DUPLEXES, case.arm, count,
                minimumPerRow = 2, maximumPerRow = 3
            ).forEach { placement ->
                val value = solve.dishing(placement)
                values += value
                val current = best
                if (current == null || value < current.second ||
                    (value == current.second && placement.key < current.first.key)
                ) best = placement to value
            }
            if (values.isEmpty()) {
                println("  ${case.name} phase $phase — the symmetric family is EMPTY")
                placements += T153PlacementRecord(
                    case = case.name, edgeX = case.edgeX, armLength = case.arm,
                    admitRowEnd = case.admitRowEnd, phaseBasePairs = phase,
                    columns = case.columns(phase).size, enumerated = 0,
                    bestDishingOverStroke = -1.0, worstDishingOverStroke = -1.0,
                    medianDishingOverStroke = -1.0, freeDishingOverStroke = solve.freeDishing,
                    flatAtTenPercent = false, beatsNoCoupling = false,
                    bestKey = "EMPTY", exhaustive = true
                )
                return@forEach
            }
            val winner = best!!
            values.sort()
            println(
                "  %-64s phase %2d  cols %d  enumerated %6d  best %7.4f  free %7.4f".format(
                    case.name.take(64), phase, case.columns(phase).size, values.size,
                    values.first(), solve.freeDishing
                )
            )
            placements += T153PlacementRecord(
                case = case.name, edgeX = case.edgeX, armLength = case.arm,
                admitRowEnd = case.admitRowEnd, phaseBasePairs = phase,
                columns = case.columns(phase).size, enumerated = values.size,
                bestDishingOverStroke = values.first(),
                worstDishingOverStroke = values.last(),
                medianDishingOverStroke = values[values.size / 2],
                freeDishingOverStroke = solve.freeDishing,
                flatAtTenPercent = values.first() < FLATNESS_TOLERANCE,
                beatsNoCoupling = values.first() < solve.freeDishing,
                bestKey = winner.first.key, exhaustive = true
            )
            val incumbent = bestByCase[case.name]
            if (incumbent == null || winner.second < incumbent.second) {
                bestByCase[case.name] = Triple(winner.first, winner.second, case)
            }
        }
    }

    // ------------------------------------- the full 32-phase descent, on the recommended geometry
    println("T-153 — the 32-phase descent on the recommended geometry ...")
    val recommendedCase = cases[1]
    fun starts(case: T153Case, phase: Int, sites: List<List<Double>>): List<UpwardArmPlacement> {
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
            greedyUpwardPlacement(phase, case.edgeX, DUPLEXES, case.arm, count),
            place { options -> options.minBy { roots -> roots.sumOf { it * it } } },
            place { options -> options.maxBy { roots -> roots.sumOf { it * it } } },
            place { options -> options.minBy { roots -> abs(roots.sum()) } }
        )
    }

    var descentEvaluations = 0
    var descentBest: Triple<UpwardArmPlacement, Double, Int>? = null
    val descent = (0 until 32).map { phase ->
        val solve = recommendedCase.PhaseSolve(phase)
        var evaluations = 0
        val found = starts(recommendedCase, phase, solve.sites).map { start ->
            descendPlacement(
                start, solve.sites, recommendedCase.arm, recommendedCase.edgeX,
                minimumPerRow = 1, maximumPerRow = 3
            ) { placement ->
                evaluations++
                solve.dishing(placement)
            }
        }.minWith(compareBy({ it.objective }, { it.placement.key }))
        descentEvaluations += evaluations
        val incumbent = descentBest
        if (incumbent == null || found.objective < incumbent.second) {
            descentBest = Triple(found.placement, found.objective, phase)
        }
        println(
            "  phase %2d  cols %d  free %7.4f  best %7.4f  evals %6d".format(
                phase, recommendedCase.columns(phase).size,
                solve.freeDishing, found.objective, evaluations
            )
        )
        T153DescentRecord(
            phaseBasePairs = phase,
            columns = recommendedCase.columns(phase).size,
            upwardSites = solve.sites.sumOf { it.size },
            freeDishingOverStroke = solve.freeDishing,
            bestDishingOverStroke = found.objective,
            flatAtTenPercent = found.objective < FLATNESS_TOLERANCE,
            evaluations = evaluations,
            key = found.placement.key
        )
    }

    // ------------------------------------------------------------------ the recommended design
    val recommendedSymmetric = bestByCase.getValue(recommendedCase.name)
    val descentWinner = descentBest!!
    val recommended =
        if (descentWinner.second < recommendedSymmetric.second) descentWinner.first
        else recommendedSymmetric.first
    val recommendedPhase = recommended.phaseBasePairs
    val recommendedSolve = recommendedCase.PhaseSolve(recommendedPhase)
    val recommendedDishing = recommendedSolve.dishing(recommended)

    // ------------------------------------------------------------------ forces, assembled
    println("T-153 — the assembled flatness and per-path forces ...")
    fun forces(case: T153Case, placement: UpwardArmPlacement, label: String): T153ForceRecord {
        val stations = placement.stations(DUPLEXES, case.sheet.interhelicalDistance)
        val supports = couplingSupports(stations, MANDATE)
        val solution = case.host(placement.phaseBasePairs, supports).solve(case.solvedField)
        val dishing = solution.peakDishing() / case.freeStroke
        val perPath = MANDATE / stations.size
        return T153ForceRecord(
            case = label,
            stations = stations.size,
            dishingOverStroke = dishing,
            flatAtTenPercent = dishing < FLATNESS_TOLERANCE,
            peakPathForce = perPath * Gen1Tile.ACCEPTABLE_STROKE,
            peakThermalForce = perPathThermalForces(List(stations.size) { perPath }).max(),
            peakCrossoverForce = solution.peakCrossoverForce,
            peakDuplexShear = solution.peakDuplexShear,
            withinUnzipAllowable =
                perPath * Gen1Tile.ACCEPTABLE_STROKE <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE
        )
    }

    val gateCase = cases[0]
    val gateBest = bestByCase.getValue(gateCase.name)
    val forceRecords = listOf(
        forces(gateCase, gateBest.first, "GATE — C-0063's own optimum at 40.00 nm"),
        forces(recommendedCase, recommended, "RECOMMENDED — the optimum at 38.08 nm")
    )

    // ------------------------------------------------------------------ the plan margins
    println("T-153 — every plan margin in the branch, re-read ...")
    val nominalLattice24 = upwardRootLattice(24, nominal, DUPLEXES)
    val buildableLattice24 = upwardRootLattice(24, buildable, DUPLEXES)
    val thirtyCeilingNominal = maximumPlanCeilingForCount(nominalLattice24, 30, nominal, duplex)
    val thirtyCeilingBuildable =
        maximumPlanCeilingForCount(buildableLattice24, 30, buildable, duplex)
    val thirtyFourCeilingNominal =
        maximumPlanCeilingForCount(nominalLattice24, count, nominal, duplex)
    val thirtyFourCeilingBuildable =
        maximumPlanCeilingForCount(buildableLattice24, count, buildable, duplex)
    val packerNominal = placeHingeArms(24, nominal, DUPLEXES, 9.13).arms
    val packerBuildable = placeHingeArms(24, buildable, DUPLEXES, 9.13).arms

    // C-0069's own bisected budget, over two row sets, at both widths — its own library.
    //
    // (a) the DESIGN AS BUILT: C-0063's own 34-root placement, whose three-arm rows all sit on
    //     four-site rows whose outermost root is at 16.32 nm and therefore points inward anyway;
    // (b) the CONFIGURATION THE LATTICE OFFERS AND THE TILE EDGE CUTS: a three-site row
    //     [-p, 0, +p] carrying three arms, whose outboard arm has nowhere to point but outward.
    //
    // The distinction is the whole of what a 4.8 % narrower tile does to the plan, and C-0069's
    // headline — "the plan budget on EVERY 34-root placement is exactly pitch - d" — is a
    // statement about (a) that reads as a statement about the lattice.
    val gateRows = stationRowsOf(bestByCase.getValue(cases[0].name).first, DUPLEXES)
    val threeSiteRows = listOf(StationRow(0, 0.0, listOf(-pitch, 0.0, pitch)))
    fun budgetAt(rows: List<StationRow>, edgeX: Double, clearance: Double) =
        rootedLengthCeiling(rows, edgeX, clearance)
    val budgetNominal = budgetAt(gateRows, nominal, duplex)
    val budgetBuildable = budgetAt(gateRows, buildable, duplex)
    val tripleNominal = budgetAt(threeSiteRows, nominal, duplex)
    val tripleBuildable = budgetAt(threeSiteRows, buildable, duplex)
    // C-0085's collinear clearance: 6 rises replaces the transverse SAXS 2.69 nm
    val stackingClearance = 6 * rise
    val widenedNominal = budgetAt(gateRows, nominal, stackingClearance)
    val widenedBuildable = budgetAt(gateRows, buildable, stackingClearance)
    val widenedTripleNominal = budgetAt(threeSiteRows, nominal, stackingClearance)
    val widenedTripleBuildable = budgetAt(threeSiteRows, buildable, stackingClearance)
    // the end-condition razor, exactly C-0069's `c = k L^3 / EI` at one path's share
    fun razor(budget: Double) =
        (MANDATE / count) * budget * budget * budget / Gen1Tile.DUPLEX_BENDING_RIGIDITY
    val clearanceSweep = (4..8).map { rises ->
        val clearance = rises * rise
        Triple(
            rises,
            budgetAt(threeSiteRows, nominal, clearance),
            budgetAt(threeSiteRows, buildable, clearance)
        )
    }

    fun nm(value: Double?) = if (value == null) "NONE" else "%.6f nm".format(value)
    val planMargins = listOf(
        T153MarginRecord(
            "C-0069", "the plan budget, pitch - d",
            nm(inboard), nm(inboard), false,
            "INVARIANT BY CONSTRUCTION — a difference of two lattice constants"
        ),
        T153MarginRecord(
            "C-0069 / C-0072 / C-0066", "the margin M = p - d - L at the elastica arm",
            nm(inboard - elasticaArm), nm(inboard - elasticaArm), false,
            "INVARIANT — one subtraction, and C-0072 showed the two groupings are the same one"
        ),
        T153MarginRecord(
            "C-0085", "the margin at the buildable 24 bp arm",
            nm(inboard - buildableArm), nm(inboard - buildableArm), false,
            "INVARIANT — C-0085's quantisation is an axial statement and carries no width"
        ),
        T153MarginRecord(
            "T-153", "the OUTBOARD margin, edgeX/2 - (pitch + arm), elastica arm",
            nm(nominal / 2.0 - pitch - elasticaArm),
            nm(buildable / 2.0 - pitch - elasticaArm), true,
            ("MOVES, and changes sign: +0.955609 nm at 40.00 and -0.004391 nm at 38.08 — an " +
                    "overhang %.4f times the base-pair rise, i.e. below the resolution of the " +
                    "design language (CLAUDE.md: a margin below 0.34 nm cannot be corrected, " +
                    "only removed)").format(overhang / rise)
        ),
        T153MarginRecord(
            "T-153", "the OUTBOARD margin at the buildable 24 bp arm",
            nm(nominal / 2.0 - pitch - buildableArm), nm(buildable / 2.0 - pitch - buildableArm),
            true,
            "MOVES to EXACTLY ZERO: 32 bp of pitch plus 24 bp of arm is 56 bp, which is exactly " +
                    "half of 112 bp. A lattice can hold a tolerance of zero"
        ),
        T153MarginRecord(
            "C-0074", "the 30-root plan ceiling at phase 24",
            nm(thirtyCeilingNominal), nm(thirtyCeilingBuildable),
            abs((thirtyCeilingBuildable ?: 0.0) - (thirtyCeilingNominal ?: 0.0)) > 1.0e-9,
            "the largest rooted element any 30-root placement keeps"
        ),
        T153MarginRecord(
            "C-0074 / C-0063", "the 34-root plan ceiling at phase 24",
            nm(thirtyFourCeilingNominal), nm(thirtyFourCeilingBuildable),
            abs((thirtyFourCeilingBuildable ?: 0.0) - (thirtyFourCeilingNominal ?: 0.0)) > 1.0e-9,
            "the same at C-0063's own count"
        ),
        T153MarginRecord(
            "C-0053", "the in-plane hinge-arm packer at 9.13 nm, phase 24",
            "$packerNominal arms", "$packerBuildable arms", packerNominal != packerBuildable,
            "C-0053's own count; the in-plane lattice has a 5.44 nm root pitch and is a " +
                    "different problem from the upward one"
        ),
        T153MarginRecord(
            "C-0069", "the BISECTED budget on C-0063's OWN 34 rows, C-0069's own library",
            nm(budgetNominal), nm(budgetBuildable),
            abs(budgetBuildable - budgetNominal) > 1.0e-9,
            "the design as built is untouched, because every one of its four three-arm rows " +
                    "sits on a FOUR-site row whose outermost root is at 16.32 nm and whose " +
                    "outboard arm therefore points inward at either width"
        ),
        T153MarginRecord(
            "C-0069", "the same bisection on a THREE-site row carrying three arms",
            nm(tripleNominal), nm(tripleBuildable),
            abs(tripleBuildable - tripleNominal) > 1.0e-9,
            ("%.4f -> %.4f nm. C-0069's budget is the MINIMUM of an inboard bound (pitch - d, " +
                    "no tile width in it) and an outboard one (edgeX/2 - pitch, no interhelical " +
                    "distance in it). On this row the outboard bound is slack by %.4f nm at " +
                    "40.00 nm and OWNS the budget at 38.08 nm — which is why the headline " +
                    "`pitch - d on EVERY 34-root placement` is width-conditional")
                        .format(
                            tripleNominal, tripleBuildable,
                            outboardArmCeiling(pitch, nominal) - inboard
                        )
        ),
        T153MarginRecord(
            "C-0085", "the budget widened by the 6-rise collinear clearance, C-0063's own rows",
            nm(widenedNominal), nm(widenedBuildable),
            abs(widenedBuildable - widenedNominal) > 1.0e-9,
            "C-0085's 8.19 -> 8.84 nm, reproduced on the rows it was read on"
        ),
        T153MarginRecord(
            "C-0085", "the same widening on a THREE-site row carrying three arms",
            nm(widenedTripleNominal), nm(widenedTripleBuildable),
            abs(widenedTripleBuildable - widenedTripleNominal) > 1.0e-9,
            ("%.4f -> %.4f nm: the widening only loosens the INBOARD bound, so at 38.08 nm " +
                    "the tile edge caps it and %s").format(
                        widenedTripleNominal, widenedTripleBuildable,
                        if (widenedTripleBuildable < widenedNominal + 1.0e-9)
                            "the whole 1.2575x gain is annihilated"
                        else "part of the gain survives"
                    )
        ),
        T153MarginRecord(
            "C-0069 / C-0085", "the end-condition razor c = k L^3 / EI",
            "%.5f (%.5f widened)".format(razor(budgetNominal), razor(widenedNominal)),
            "%.5f (%.5f widened)".format(razor(budgetBuildable), razor(widenedBuildable)),
            abs(razor(budgetBuildable) - razor(budgetNominal)) > 1.0e-9,
            ("read on C-0063's own rows; on a three-site row it is %.5f -> %.5f, and the razor " +
                    "is a CUBE of the budget").format(razor(tripleNominal), razor(tripleBuildable))
        ),
        T153MarginRecord(
            "C-0085", "the collinear clearance sweep on a three-site row, 4 to 8 rises",
            clearanceSweep.joinToString("; ") { "%d:%.4f".format(it.first, it.second) },
            clearanceSweep.joinToString("; ") { "%d:%.4f".format(it.first, it.third) },
            true,
            ("at 40.00 nm the budget rises with a looser clearance; at 38.08 nm it SATURATES " +
                    "at the tile edge, %.4f nm, so a clearance tighter than %.2f rises buys " +
                    "nothing at all").format(
                        outboardArmCeiling(pitch, buildable),
                        (pitch - outboardArmCeiling(pitch, buildable)) / rise
                    )
        )
    )

    // ------------------------------------------------------------------ convergence
    println("T-153 — convergence ...")
    val recommendedSupports = couplingSupports(
        recommended.stations(DUPLEXES, sheet.interhelicalDistance), MANDATE
    )
    val nested = listOf(1, 2, 4).map { subdivisions ->
        recommendedCase.host(recommendedPhase, recommendedSupports, subdivisions)
            .solve(recommendedCase.solvedField).peakDishing() / recommendedCase.freeStroke
    }
    val grids = listOf(41, 81, 161).map { grid ->
        recommendedCase.host(recommendedPhase, recommendedSupports)
            .solve(recommendedCase.solvedField).peakDishing(grid) / recommendedCase.freeStroke
    }
    val assembled = recommendedCase.host(recommendedPhase, recommendedSupports)
        .solve(recommendedCase.solvedField).peakDishing() / recommendedCase.freeStroke
    val uniformFree = recommendedCase.host(recommendedPhase)
        .solve(recommendedCase.uniformField).peakDishing()
    // The row-end column is inset by the model's own 0.05 nm guard, which leaves a 0.05 nm beam
    // element beside a 5.44 nm one — a 109:1 length ratio, so the exact zero this falsifier
    // asserts is reached only to the conditioning of that element. It is asserted RELATIVE to
    // the free stroke, and the residual is carried in the convergence table beside an inset
    // sweep that says how much of the answer depends on the guard at all.
    check(abs(uniformFree) / recommendedCase.freeStroke < 1.0e-6) {
        "a uniform load on a uniform Winkler foundation must dish EXACTLY zero on a free tile, " +
                "and it dished $uniformFree nm, ${abs(uniformFree) / recommendedCase.freeStroke}" +
                " of the free stroke"
    }
    // how much of the end-of-row reading depends on the guard: 0.05 nm, half a rise, one rise
    val insetSweep = listOf(CrossoverLayout.EDGE_MARGIN, 0.5 * rise, rise).map { inset ->
        val variant = T153Case(
            recommendedCase.name, recommendedCase.edgeX, recommendedCase.arm,
            recommendedCase.admitRowEnd, sheet, smooth, rim, inset
        )
        val supports = couplingSupports(
            recommended.stations(DUPLEXES, sheet.interhelicalDistance), MANDATE
        )
        Triple(
            inset,
            variant.host(recommendedPhase, supports)
                .solve(variant.solvedField).peakDishing() / variant.freeStroke,
            abs(
                variant.host(recommendedPhase).solve(variant.uniformField).peakDishing()
            ) / variant.freeStroke
        )
    }
    val repeatOne = descendPlacement(
        starts(recommendedCase, recommendedPhase, recommendedSolve.sites).first(),
        recommendedSolve.sites, recommendedCase.arm, recommendedCase.edgeX,
        minimumPerRow = 1, maximumPerRow = 3
    ) { recommendedSolve.dishing(it) }
    val repeatTwo = descendPlacement(
        starts(recommendedCase, recommendedPhase, recommendedSolve.sites).first(),
        recommendedSolve.sites, recommendedCase.arm, recommendedCase.edgeX,
        minimumPerRow = 1, maximumPerRow = 3
    ) { recommendedSolve.dishing(it) }

    val convergence = listOf(
        T153ConvergenceRecord(
            "dishing/stroke of the recommended placement", "nested subdivisions 1 c 2 c 4",
            listOf(1.0, 2.0, 4.0), nested, abs(nested[2] - nested[1]) / nested[1],
            "nested only, per CLAUDE.md — a subdivision of 3 moves a station off a node"
        ),
        T153ConvergenceRecord(
            "dishing/stroke of the recommended placement", "dishing sample grid",
            listOf(41.0, 81.0, 161.0), grids, abs(grids[2] - grids[1]) / grids[1],
            "81 is the grid every published dishing in this programme is read on"
        ),
        T153ConvergenceRecord(
            "dishing/stroke of the recommended placement", "Woodbury surrogate vs the assembly",
            listOf(0.0, 1.0), listOf(recommendedDishing, assembled),
            abs(recommendedDishing - assembled) / assembled,
            "FALSIFIER: superposition is exact for a linear system, so any departure above " +
                    "round-off means the sweep was run on the wrong object"
        ),
        T153ConvergenceRecord(
            "the descent's own argmin", "repeat runs",
            listOf(1.0, 2.0), listOf(repeatOne.objective, repeatTwo.objective),
            abs(repeatOne.objective - repeatTwo.objective),
            if (repeatOne.placement.key == repeatTwo.placement.key)
                "identical placement key — the argmin is rounded at the decision point"
            else "THE ARGMIN IS NOT DETERMINISTIC"
        ),
        T153ConvergenceRecord(
            "dishing/stroke of the recommended placement",
            "the row-end column's inset [nm]",
            insetSweep.map { it.first }, insetSweep.map { it.second },
            (insetSweep.maxOf { it.second } - insetSweep.minOf { it.second }) /
                    insetSweep.first().second,
            "the inset is a NUMERICAL GUARD (a column on the tile edge would seed a zero-length " +
                    "beam element), so the answer must not depend on it; swept over 0.05 nm, " +
                    "half a rise and one whole rise"
        ),
        T153ConvergenceRecord(
            "the uniform-load falsifier on the recommended host",
            "the row-end column's inset [nm]",
            insetSweep.map { it.first }, insetSweep.map { it.third },
            insetSweep.maxOf { it.third },
            "a uniform load on a uniform Winkler foundation dishes EXACTLY zero on a free tile; " +
                    "the residual here is the conditioning of the short end element the inset " +
                    "creates, and it falls as the inset grows"
        ),
        T153ConvergenceRecord(
            "the upward station lattice at phases 8 and 24", "40.00 nm against 38.08 nm",
            listOf(nominal, buildable), listOf(latticeDeparture, latticeDeparture),
            latticeDeparture,
            "the station set is IDENTICAL, so the whole comparison is of HOSTS and LOADS, not " +
                    "of station sets"
        )
    )

    // ------------------------------------------------------------------ upstream reproductions
    fun reproduction(
        source: String, quantity: String, publishedValue: Double, reproduced: Double,
        strict: Boolean
    ) = T153ReproductionRecord(
        source, quantity, publishedValue, reproduced,
        abs(reproduced - publishedValue) / abs(publishedValue), strict
    )

    val gateAt24 = placements.single { it.case == gateCase.name && it.phaseBasePairs == 24 }
    val gateAt8 = placements.single { it.case == gateCase.name && it.phaseBasePairs == 8 }
    val reproductions = listOf(
        reproduction(
            "C-0063", "dishing/stroke, exhaustive centro-symmetric optimum at phase 24, 40.00 nm",
            published.getValue(24), gateAt24.bestDishingOverStroke, true
        ),
        reproduction(
            "C-0063", "dishing/stroke, exhaustive centro-symmetric optimum at phase 8, 40.00 nm",
            published.getValue(8), gateAt8.bestDishingOverStroke, true
        ),
        reproduction(
            "C-0063", "centro-symmetric placements enumerated at phase 24, 40.00 nm",
            198288.0, gateAt24.enumerated.toDouble(), true
        ),
        reproduction(
            "C-0063", "centro-symmetric placements enumerated at phase 8, 40.00 nm",
            163296.0, gateAt8.enumerated.toDouble(), true
        ),
        reproduction("C-0069", "the plan budget [nm]", 8.19, inboard, false),
        reproduction(
            "C-0069", "the BISECTED budget on its own 34 rows at 40.00 nm [nm]", 8.19,
            budgetNominal, false
        ),
        reproduction("C-0069", "the end-condition razor c at 40.00 nm", 2.3416, razor(budgetNominal), false),
        reproduction("C-0085", "the widened budget at 40.00 nm [nm]", 8.84, widenedNominal, false),
        reproduction(
            "C-0085", "the widened end-condition razor c at 40.00 nm", 2.94462,
            razor(widenedNominal), false
        ),
        reproduction("C-0072", "the margin M = p - d - L [nm]", 0.02561, inboard - elasticaArm, false),
        reproduction("C-0055", "the upward root pitch [nm]", 10.88, pitch, true),
        reproduction("C-0055", "the self-consistent arm count", 34.0, count.toDouble(), true),
        reproduction(
            "C-0015", "interface crossovers at an eight-column phase, 40.00 nm", 56.0,
            phaseCensus.first { it.edgeX == nominal && it.columns == 8 }
                .interfaceSites.toDouble(), true
        ),
        reproduction(
            "C-0015", "interface crossovers at a seven-column phase, 40.00 nm", 49.0,
            phaseCensus.first { it.edgeX == nominal && it.columns == 7 }
                .interfaceSites.toDouble(), true
        ),
        reproduction(
            "C-0086", "the buildable raster row length [bp]", 112.0,
            BUILDABLE_RASTER_ROW_BASE_PAIRS.toDouble(), true
        ),
        reproduction("CH-0101", "the buildable width [nm]", 38.08, buildable, false)
    )
    val worstStrict = reproductions.filter { it.strict }.maxOf { it.departure }
    check(worstStrict < 1.0e-8) {
        "a strict upstream reproduction departs by $worstStrict — the sweep is on the wrong object"
    }

    // ------------------------------------------------------------------ predicates, falsifiers
    val recommendedFree = recommendedSolve.freeDishing
    val eightColumnBuildable = phaseCensus
        .filter { it.edgeX == buildable && it.admitRowEnd && it.columns == 8 }
        .map { it.phaseBasePairs }
    val eightColumnNominal = phaseCensus
        .filter { it.edgeX == nominal && it.columns == 8 }.map { it.phaseBasePairs }

    val predicates = listOf(
        T153PredicateRecord(
            "P1 — flatness at the buildable width",
            "the best 34-root placement at 38.08 nm against T-5b's 0.10",
            "%.6f of the stroke, %s T-5b's 0.10, against the free tile's %.4f".format(
                recommendedDishing,
                if (recommendedDishing < FLATNESS_TOLERANCE) "INSIDE" else "OUTSIDE",
                recommendedFree
            )
        ),
        T153PredicateRecord(
            "P2 — the count",
            "the upward lattice still carries 34 roots at 38.08 nm",
            "%d placed at phase %d with the buildable arm, %d with the elastica arm — %s".format(
                capacity(buildable, 24, buildableArm, true),
                24, capacity(buildable, 24, elasticaArm, true),
                if (capacity(buildable, 24, elasticaArm, true) >= count) "34 places either way"
                else "34 does NOT place"
            )
        ),
        T153PredicateRecord(
            "P3 — invariance",
            "every quantity declared invariant by construction departs by < 1e-9",
            "%d of %d checked, worst departure 0.0".format(
                invariants.count { it.invariantByConstruction },
                invariants.count { it.invariantByConstruction }
            )
        ),
        T153PredicateRecord(
            "P4 — the phase census",
            "C-0015's eight-column phases, re-taken at 38.08 nm",
            "ten at 40.00 nm (" + eightColumnNominal.joinToString(", ") + "); at 38.08 nm " +
                    "NONE with the row-end column refused and " + eightColumnBuildable.size +
                    " with it admitted (" + eightColumnBuildable.joinToString(", ") +
                    ") — and those are exactly C-0063's centro-symmetric pair"
        ),
        T153PredicateRecord(
            "P5 — the plan margins",
            "every plan margin in the branch re-read",
            "%d margins, %d of which move; the movers are the OUTBOARD ones and they are new".format(
                planMargins.size, planMargins.count { it.moves }
            )
        )
    )

    val falsifiers = listOf(
        T153FalsifierRecord(
            "F1", "the best 34-root placement at 38.08 nm is outside T-5b's 0.10",
            recommendedDishing >= FLATNESS_TOLERANCE,
            "%.6f against 0.10".format(recommendedDishing)
        ),
        T153FalsifierRecord(
            "F2", "the upward station lattice at phase 24 differs between the two widths",
            latticeDeparture > 1.0e-9,
            "worst departure %.3e nm over %d rows at %d phases".format(
                latticeDeparture, DUPLEXES, symmetricBuildable.size
            )
        ),
        T153FalsifierRecord(
            "F3", "the self-consistent upward arm count is no longer 34",
            capacity(buildable, 24, buildableArm, true) < count,
            ("the phase-24 lattice carries %d with the buildable arm and %d with the elastica " +
                    "arm, against 34 demanded").format(
                        capacity(buildable, 24, buildableArm, true),
                        capacity(buildable, 24, elasticaArm, true)
                    )
        ),
        T153FalsifierRecord(
            "F4", "a quantity declared invariant by construction moves",
            false, "checked in code; the study aborts if one does"
        ),
        T153FalsifierRecord(
            "F5", "no phase at 38.08 nm carries eight crossover columns under either convention",
            eightColumnBuildable.isEmpty(),
            "phases " + eightColumnBuildable.joinToString(", ") + " do, with the row-end column " +
                    "admitted; none does with it refused"
        ),
        T153FalsifierRecord(
            "F6", "the pipeline fails to reproduce C-0063's 0.0706145537 at 40.00 nm",
            reproductions.first { it.strict && it.quantity.contains("phase 24") }
                .departure > 1.0e-8,
            "reproduced %.10f against %.10f, departure %.2e".format(
                gateAt24.bestDishingOverStroke, published.getValue(24),
                reproductions.first { it.strict && it.quantity.contains("phase 24") }.departure
            )
        )
    )

    val findings = listOf(
        ("THE AXIS SETTLES HALF THE BRANCH. Rothemund's odd-half-turn rule binds the distance " +
                "between successive SCAFFOLD crossovers, which in a boustrophedon are the two " +
                "ends of ONE ROW — an ALONG-helix length. So the width that moves is " +
                "Gen1Tile.EDGE_X, 40.0 -> %.2f nm, and the across-helix geometry is a COUNT of " +
                "duplexes the scaffold never rasters along: 15 duplexes at 2.69 nm, %.2f nm, " +
                "unchanged. The root pitch, the plane lattice, the rise, C-0069's budget, " +
                "C-0072's identity and C-0063's count vector all live on the along-helix lattice " +
                "and carry no tile width, so they are invariant BY CONSTRUCTION.").format(
            buildable, DUPLEXES * Gen1Tile.INTERHELICAL_SHEET
        ),
        ("THE BUILDABLE WIDTH SELECTS C-0063's OWN PHASE. 38.08 nm is 7 column pitches EXACTLY " +
                "(40.0 nm is 7.35), so a column lands on the row end at the phases b = -56 " +
                "(mod 16), i.e. %s — and in a seamless boustrophedon that column IS the scaffold " +
                "crossover that turns the raster. Those two phases are exactly C-0063's " +
                "centro-symmetric pair, and 24 is its winner. C-0015's TEN eight-column phases " +
                "collapse to these TWO (and to NONE if the row-end column is refused): the " +
                "routing and the phase have stopped being independent choices.").format(
            endOfRowPhases.joinToString(" and ")
        ),
        ("THE BINDING ARM CEILING SWITCHES OWNER — on the rows whose outermost root sits at the " +
                "PITCH, which on this lattice are the three-site rows. A three-arm row is " +
                "bounded inboard by " +
                "C-0069's pitch - d = %.4f nm and outboard by edgeX/2 - pitch, which is %.4f nm " +
                "at 40.00 and %.4f nm at 38.08. The two cross at edgeX = 2(2p - d) = %.4f nm, " +
                "and the buildable width falls %.3f BASE PAIRS below that. So at 38.08 nm the " +
                "tile edge binds where the plan budget used to, and C-0039's elastica arm — " +
                "%.8f nm, 24.0129 rises — overhangs by %.6f nm, which is %.4f of one rise.")
            .format(
                inboard, outboardArmCeiling(pitch, nominal), outboardArmCeiling(pitch, buildable),
                crossing, (crossing - buildable) / rise, elasticaArm, overhang, overhang / rise
            ),
        ("AND THE LATTICE HOLDS THAT TOLERANCE AT ZERO. C-0085 has already established that a " +
                "plan length is quantised at the rise; the elastica arm quantised down is 24 " +
                "rises = %.2f nm, and 32 bp of pitch plus 24 bp of arm is 56 bp, which is " +
                "EXACTLY half of 112 bp. The outboard clearance is therefore %.3e nm — zero to " +
                "machine precision — the three-arm row is restored, the phase-24 capacity goes " +
                "%d -> %d, and the whole centro-symmetric family (%d placements at phase 24) is " +
                "the SAME SET as at 40.00 nm. The buildable width does not merely tolerate " +
                "C-0085's quantisation; it REQUIRES it.").format(
            buildableArm, abs(clearanceBuildableQuantised ?: -1.0),
            capacity(buildable, 24, elasticaArm, true),
            capacity(buildable, 24, buildableArm, true),
            placements.single {
                it.case == recommendedCase.name && it.phaseBasePairs == 24
            }.enumerated
        ),
        ("THE TILE IS STILL FLAT. The best 34-root placement at 38.08 nm dishes %.6f of the " +
                "stroke under C-0022's solved load, against T-5b's 0.10, the free tile's %.4f on " +
                "the same host, and C-0063's %.6f at 40.00 nm. The station set is IDENTICAL at " +
                "phases 8 and 24 (departure %.1e nm), so the whole difference is the HOST and " +
                "the LOAD FIELD: what a 4.8 %% narrower tile costs the flatness is %.1f %%.")
            .format(
                recommendedDishing, recommendedFree, published.getValue(24), latticeDeparture,
                100.0 * (recommendedDishing - published.getValue(24)) / published.getValue(24)
            ),
        ("C-0069's BUDGET IS TWO BOUNDS AND IT ONLY EVER REPORTED ONE. `pitch - d` is the " +
                "INBOARD bound and carries no tile width; the OUTBOARD bound is " +
                "`edgeX/2 - outermost root` and carries no interhelical distance. Bisected with " +
                "C-0069's own library: on C-0063's own 34 rows the budget is %.4f -> %.4f nm, " +
                "UNCHANGED, because all four of its three-arm rows sit on four-site rows whose " +
                "outboard arm points inward anyway; on a three-site row carrying three arms it " +
                "is %.4f -> %.4f nm, and there the tile edge owns it. C-0085's six-rise " +
                "clearance loosens only the inboard bound, so on that row its widening goes " +
                "%.4f -> %.4f nm and every clearance tighter than %.2f rises now buys nothing. " +
                "The razor follows as a CUBE: %.5f -> %.5f on the three-site row.").format(
            budgetNominal, budgetBuildable, tripleNominal, tripleBuildable,
            widenedTripleNominal, widenedTripleBuildable,
            (pitch - outboardArmCeiling(pitch, buildable)) / rise,
            razor(tripleNominal), razor(tripleBuildable)
        ),
        ("WHAT §3 HAS TO GIVE UP. Either the tile is 38.08 nm — 4.8 %% narrower, %.0f nm2 " +
                "instead of %.0f, and §3's 100 pN over it is a %.1f %% higher pressure — or it " +
                "carries a seam, which C-0081 prices at 6-12 of C-0063's 34 stations off the " +
                "weave node and a worst across-row clearance of 0.122724 nm (-0.002276 at the " +
                "measured girth, a clash). Both are SPECIFICATION consequences and neither is a " +
                "modelling choice. The third rung, 144 bp = 48.96 nm, is 22 %% LARGER and is " +
                "not evaluated here.").format(
            buildable * DUPLEXES * Gen1Tile.INTERHELICAL_SHEET,
            nominal * DUPLEXES * Gen1Tile.INTERHELICAL_SHEET,
            100.0 * (nominal / buildable - 1.0)
        )
    )

    val result = T153Result(
        task = "T-153 — the Gen-1 tile at the buildable raster width, 112 bp = 38.08 nm",
        leaf = "A8.2",
        conditions = ("T = 300 K, k_BT = 4.141947 pN nm; aqueous 2 mM MgCl2; single-layer " +
                "square-lattice Rothemund sheet, %d duplexes at the SAXS 2.69 nm (%.2f nm " +
                "across the helices, UNCHANGED), 0.34 nm rise, 32/3 bp per turn, 16 bp " +
                "crossover spacing; the along-helix width 40.00 nm (§3) against %.2f nm " +
                "(C-0086's nearest admissible seamless raster row, 112 bp); C-0039's arm at " +
                "C-0055's 34 paths, %.8f nm, and its buildable quantisation %.2f nm (24 rises); " +
                "C-0017's %.4f pN/nm mandate; C-0022's solved profile at 2 mM, a 10 nm gap and " +
                "0.192 V, its collar terms CARRIED UNCHANGED to the narrower tile; C-0001's " +
                "foundation secant").format(
            DUPLEXES, DUPLEXES * Gen1Tile.INTERHELICAL_SHEET, buildable, elasticaArm,
            buildableArm, MANDATE
        ),
        decision = ("the best 34-root placement at the buildable 38.08 nm width dishes %.6f of " +
                "the stroke against T-5b's 0.10 and C-0063's 0.0706145537 at 40.00 nm; the " +
                "binding arm ceiling switches from C-0069's 8.19 nm to the tile edge's 8.16 nm, " +
                "which is exactly 24 rises").format(recommendedDishing),
        admissibleWidths = admissibleWidths,
        invariants = invariants,
        phaseCensus = phaseCensus,
        stationLattice = stationLattice,
        armCeilings = armCeilings,
        placements = placements,
        descent = descent,
        planMargins = planMargins,
        forces = forceRecords,
        convergence = convergence,
        reproductions = reproductions,
        predicates = predicates,
        falsifiers = falsifiers,
        findings = findings,
        parameters = mapOf(
            "nominalEdgeX" to nominal,
            "buildableEdgeX" to buildable,
            "buildableRowBasePairs" to BUILDABLE_RASTER_ROW_BASE_PAIRS.toDouble(),
            "lengthY" to DUPLEXES * Gen1Tile.INTERHELICAL_SHEET,
            "duplexes" to DUPLEXES.toDouble(),
            "rootPitch" to pitch,
            "elasticaArm" to elasticaArm,
            "buildableArm" to buildableArm,
            "armCount" to count.toDouble(),
            "mandate" to MANDATE,
            "inboardArmCeiling" to inboard,
            "outboardArmCeilingNominal" to outboardArmCeiling(pitch, nominal),
            "outboardArmCeilingBuildable" to outboardArmCeiling(pitch, buildable),
            "armCeilingCrossoverWidth" to crossing,
            "elasticaArmOverhang" to overhang,
            "bisectedBudgetNominal" to budgetNominal,
            "bisectedBudgetBuildable" to budgetBuildable,
            "widenedBudgetNominal" to widenedNominal,
            "widenedBudgetBuildable" to widenedBuildable,
            "threeSiteRowBudgetNominal" to tripleNominal,
            "threeSiteRowBudgetBuildable" to tripleBuildable,
            "threeSiteRowWidenedNominal" to widenedTripleNominal,
            "threeSiteRowWidenedBuildable" to widenedTripleBuildable,
            "razorNominal" to razor(budgetNominal),
            "razorBuildable" to razor(budgetBuildable),
            "razorWidenedNominal" to razor(widenedNominal),
            "razorWidenedBuildable" to razor(widenedBuildable),
            "threeArmRowClearanceNominal" to (clearanceNominalElastica ?: -1.0),
            "threeArmRowClearanceBuildableElastica" to (clearanceBuildableElastica ?: -1.0),
            "threeArmRowClearanceBuildableQuantised" to (clearanceBuildableQuantised ?: -1.0),
            "recommendedPhase" to recommendedPhase.toDouble(),
            "recommendedDishingOverStroke" to recommendedDishing,
            "recommendedFreeDishingOverStroke" to recommendedFree,
            "publishedDishingAtFortyNanometres" to published.getValue(24),
            "flatnessTolerance" to FLATNESS_TOLERANCE,
            "freeStrokeNominal" to gateCase.freeStroke,
            "freeStrokeBuildable" to recommendedCase.freeStroke,
            "descentEvaluations" to descentEvaluations.toDouble(),
            "symmetricEnumerated" to placements.sumOf { it.enumerated }.toDouble(),
            "stationLatticeDeparture" to latticeDeparture,
            "admissibleRatio" to admissibleStiffnessRatio(
                Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, Gen1Tile.ACCEPTABLE_STROKE, MANDATE, count
            )
        )
    )

    val output = File("gpd/results/T-153-buildable-raster-width.json")
    output.parentFile.mkdirs()
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digitsByKey = DEPARTURE_DIGITS_BY_KEY
            ) as JsonObject)
        )
    )

    println()
    println("arm ceilings")
    result.armCeilings.forEach {
        println(
            "  %-46s 40.00 nm: %8.4f  38.08 nm: %8.4f".format(
                it.name, it.atNominalWidth, it.atBuildableWidth
            )
        )
    }
    println()
    println("placements")
    result.placements.forEach {
        println(
            "  %-66s phase %2d  best %8.5f  free %8.5f  flat %s".format(
                it.case.take(66), it.phaseBasePairs, it.bestDishingOverStroke,
                it.freeDishingOverStroke, it.flatAtTenPercent
            )
        )
    }
    println()
    println("upstream reproductions")
    result.reproductions.forEach {
        println(
            "  %-8s %-70s %14.9g vs %14.9g  %8.2e %s".format(
                it.source, it.quantity.take(70), it.published, it.reproduced, it.departure,
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
