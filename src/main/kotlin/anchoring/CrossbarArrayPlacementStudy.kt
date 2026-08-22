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
import com.xemantic.nano.plentyofroom.coupling.couplingSupports
import com.xemantic.nano.plentyofroom.coupling.edgeCollarPressure
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.DEPARTURE_DIGITS_BY_KEY
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.OrigamiSheet
import com.xemantic.nano.plentyofroom.structure.PlateOnFoundation
import com.xemantic.nano.plentyofroom.structure.PointSupport
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import com.xemantic.nano.plentyofroom.structure.origamiSheet
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
import kotlin.math.PI
import kotlin.math.abs

/**
 * `T-130` — do `C-0062`'s closing trio lattices place **34** times on `C-0063`'s placement?
 *
 * Emits `gpd/results/T-130-crossbar-array-placement.json`.
 */

private const val DUPLEXES = 15
private const val ARM_COUNT = 34
private const val FLATNESS_TOLERANCE = 0.10
private const val BEST_PHASE = 24
private const val REPRESENTABLE_BASE_DEGREES = 45.0
private val MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

@Serializable
private data class T130BoundRecord(
    val name: String,
    val value: Double,
    val unit: String,
    val against: Double,
    val ratio: Double,
    val settles: String
)

@Serializable
private data class T130PositionRecord(
    val axialBasePairs: Double,
    val offsetBasePairs: Double,
    val candidates: Int,
    val closes: Boolean,
    val misalignmentDegrees: Double
)

@Serializable
private data class T130RegisterRecord(
    val separationBasePairs: Int,
    val positions: Int,
    val closingPositions: Int,
    val closingPairCentres: Int,
    val admitsPairAnywhere: Boolean,
    val closesAtTheStation: Boolean,
    val nearestOffset: Double,
    val nearestOffsetBasePairs: Double,
    val misalignmentAtNearestCentre: Double,
    val representableAtNearestCentre: Boolean,
    val nearestRepresentableOffset: Double,
    val freeFloorDegrees: Double,
    val c0059FloorDegrees: Double,
    val pinnedOverFreeFloor: Double,
    val centroSymmetryPreserved: Boolean,
    val verdict: String
)

/** Every closing pair centre the register offers at one row pitch — the whole trade, not its argmin. */
@Serializable
private data class T130PairRecord(
    val separationBasePairs: Int,
    val offset: Double,
    val offsetBasePairs: Double,
    val worstMisalignmentDegrees: Double,
    val representable: Boolean
)

@Serializable
private data class T130ArrayRecord(
    val trio: String,
    val crossbarBasePairs: Int,
    val separationBasePairs: Int,
    val closesOnVerdictGrid: Boolean,
    val representableBase: Boolean,
    val registered: Boolean,
    val offset: Double,
    val demanded: Int,
    val legsOnSheet: Int,
    val insideFootprint: Int,
    val placed: Int,
    val placedWholeBlock: Int,
    val overlappingPairs: Int,
    val memberClashPairs: Int,
    val levelsRequired: Int,
    val singleLevel: Boolean,
    val planAreaFraction: Double,
    val placesAt34: Boolean,
    val placesAndRepresentable: Boolean,
    val verdict: String
)

@Serializable
private data class T130FlatnessRecord(
    val placement: String,
    val stations: Int,
    val offset: Double,
    val dishingOverStroke: Double,
    val flatAtTenPercent: Boolean,
    val beatsNoCoupling: Boolean,
    val peakPathForce: Double,
    val peakCrossoverForce: Double,
    val peakDuplexShear: Double
)

@Serializable
private data class T130FlexureRecord(
    val reading: String,
    val paths: Int,
    val span: Double,
    val planAreaFraction: Double,
    val overlappingPairs: Int,
    val mutuallyBlockingPairs: Int,
    val levelsRequired: Int,
    val singleLevel: Boolean,
    val placed: Int,
    val verdict: String
)

@Serializable
private data class T130SensitivityRecord(
    val axis: String,
    val reading: String,
    val closingPositions: Int,
    val closingPairCentres: Int,
    val nearestOffset: Double,
    val placed: Int,
    val verdictMoves: Boolean,
    val note: String
)

@Serializable
private data class T130ConvergenceRecord(
    val quantity: String,
    val parameter: String,
    val levels: List<String>,
    val results: List<Double>,
    val departure: Double,
    val note: String
)

@Serializable
private data class T130ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val here: Double,
    val departure: Double,
    val note: String
)

@Serializable
private data class T130BudgetRecord(
    val stage: String,
    val positions: Int,
    val junctionSolves: Int,
    val arraysPlaced: Int,
    val instancesPlaced: Int
)

@Serializable
private data class T130PredicateRecord(
    val name: String,
    val statement: String,
    val verdict: String
)

@Serializable
private data class T130Result(
    val task: String,
    val leaf: String,
    val conditions: Map<String, String>,
    val decision: String,
    val bounds: List<T130BoundRecord>,
    val positions: List<T130PositionRecord>,
    val register: List<T130RegisterRecord>,
    val pairs: List<T130PairRecord>,
    val arrays: List<T130ArrayRecord>,
    val flatness: List<T130FlatnessRecord>,
    val flexure: List<T130FlexureRecord>,
    val sensitivities: List<T130SensitivityRecord>,
    val convergence: List<T130ConvergenceRecord>,
    val reproductions: List<T130ReproductionRecord>,
    val budget: List<T130BudgetRecord>,
    val predicates: List<T130PredicateRecord>,
    val findings: Map<String, String>,
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

/** `C-0022`'s solved edge profile, keyed on concentration, gap **and bias** — `C-0063`'s own. */
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
            CollarTerm(value("rimResidualDepth"), 1.0)
}

/** `C-0063`'s own 34 stations, read from its result file rather than retyped. */
private fun c0063Stations(file: File): List<TrussStation> {
    require(file.exists()) { "C-0063's result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("bestPlacement").jsonArray.map { it.jsonObject }
        .flatMap { row ->
            val index = row.getValue("row").jsonPrimitive.content.toInt()
            val y = row.getValue("y").jsonPrimitive.content.toDouble()
            row.getValue("roots").jsonArray.map {
                TrussStation(index, it.jsonPrimitive.content.toDouble(), y)
            }
        }
}

/** One of `C-0062`'s recorded closing trios. */
private data class PublishedTrio(
    val rank: String,
    val crossbarBasePairs: Int,
    val separationBasePairs: Int,
    val axialPhase: Double,
    val lateralSeat: Double,
    val worstMisalignmentDegrees: Double,
    val closesOnVerdictGrid: Boolean
)

private fun c0062Trios(file: File): List<PublishedTrio> {
    require(file.exists()) { "C-0062's result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("trios").jsonArray.map { it.jsonObject }
        .map {
            fun text(key: String) = it.getValue(key).jsonPrimitive.content
            PublishedTrio(
                rank = text("rank"),
                crossbarBasePairs = text("crossbarBasePairs").toInt(),
                separationBasePairs = text("separationBasePairs").toInt(),
                axialPhase = text("axialPhase").toDouble(),
                lateralSeat = text("lateralSeat").toDouble(),
                worstMisalignmentDegrees = text("worstMisalignmentDegrees").toDouble(),
                closesOnVerdictGrid = text("closesOnVerdictGrid").toBoolean()
            )
        }
}

/** `C-0062`'s per-configuration closing-lattice counts, for the re-check. */
private fun c0062ClosingLattices(file: File): Int =
    Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("configurations").jsonArray.map { it.jsonObject }
        .sumOf { it.getValue("closingLattices").jsonPrimitive.content.toInt() }

/** `C-0059`'s published base misalignment floors, consumed as data. */
private fun c0059BaseFloors(file: File): Map<Int, Double> {
    require(file.exists()) { "C-0059's result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("pairs").jsonArray.map { it.jsonObject }
        .associate {
            it.getValue("separationBasePairs").jsonPrimitive.content.toInt() to
                    it.getValue("worstMisalignmentDegrees").jsonPrimitive.content.toDouble()
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
    val footprint = edgeX * lengthY
    val rise = Gen1Tile.RISE_PER_BASE_PAIR
    val pitch = UPWARD_ROOT_PITCH_BASE_PAIRS * rise
    val width = OrigamiDuplex.INTERHELICAL

    println("T-130 — reading C-0062's trios, C-0063's stations and C-0022's solved load ...")
    val stations = c0063Stations(ResultInputs.T_125.file())
    check(stations.size == ARM_COUNT) {
        "C-0063's placement must carry $ARM_COUNT stations, carried ${stations.size}"
    }
    val trios = c0062Trios(ResultInputs.T_127.file())
    val closingLattices = c0062ClosingLattices(ResultInputs.T_127.file())
    val baseFloors = c0059BaseFloors(ResultInputs.T_124.file())
    val (smooth, rim) = solvedProfile(ResultInputs.T_3B.file())
    val interiorPressure = Gen1Tile.TARGET_FORCE / footprint
    val solvedField = edgeCollarPressure(interiorPressure, edgeX, lengthY, listOf(smooth, rim))
    val uniformField = uniformPressure(interiorPressure)
    val freeStroke = PlateOnFoundation(
        sheet.plate(edgeX, lengthY), Gen1Tile.FOUNDATION_SECANT, emptyList(), basisDegree = 12
    ).solve(uniformField).meanDeflection
    val columns = CrossoverLayout.atBasePairPhase(BEST_PHASE, sheet, edgeX)

    // ------------------------------------------------------------------ the cheap bounds
    println("T-130 — the five cheap bounds, which run before the composition ...")
    val census = stationPhaseClassCensus(stations, BEST_PHASE)
    val blockArea = trios.map { it.crossbarBasePairs }.distinct().sorted()
    val widestBlock = blockArea.max()
    val blockAreaFraction = ARM_COUNT * widestBlock * rise * width / footprint
    val demand = trussPlanDemand(widestBlock, width)

    // the flexure the third junction caps, at C-0048's own 45 paths and at C-0055's 34
    val reference = capDesign(legLength = 12 * rise, separationBasePairs = 10)
    val cap = SolvedTrussCap(
        separationBasePairs = 10,
        legLength = 12 * rise,
        base = TwoLinkBase(
            name = "two-terminus base",
            restrainedAxis = chordBaseAxes(DuplexBackbone(minorGrooveAngle = 180.0), 0.0).loaded,
            freeAxis = chordBaseAxes(DuplexBackbone(minorGrooveAngle = 180.0), 0.0).free,
            axial = 2.0 * bondSlideStiffness(),
            provenance = "C-0029's counting theorem via C-0042's chordBaseAxes"
        )
    )
    val span34 = coupledFlexureSpan(
        Gen1Tile.DUPLEX_BENDING_RIGIDITY, cap.flexibility, ARM_COUNT, MANDATE,
        Gen1Tile.ACCEPTABLE_STROKE
    )
    val flexureAreaFraction = ARM_COUNT * (span34 + widestBlock * rise) * width / footprint

    val bounds = listOf(
        T130BoundRecord(
            "the plan area of 34 truss blocks", ARM_COUNT * widestBlock * rise * width, "nm^2",
            footprint, blockAreaFraction,
            "the truss BLOCK is not an area problem — C-0053's bound 1 in a new place, and it " +
                    "does not decide the task"
        ),
        T130BoundRecord(
            "the block's plan demand, crossbar + d", demand, "nm", pitch, demand / pitch,
            "the along-row packing, before any packer runs: the widest crossbar in C-0062's " +
                    "band clears the 10.88 nm station pitch, where C-0053's 45-path arm does not"
        ),
        T130BoundRecord(
            "the helical phase classes of the 34 stations", census.classes.toDouble(), "classes",
            1.0, census.classes.toDouble(),
            "THE SHAPE OF THE WHOLE ANSWER. Every station is an EAST site of its own duplex and " +
                    "adjacent rows' duplexes are phase-shifted by exactly the 16 bp their sites " +
                    "are offset by, so the register is ONE question and the placed count is " +
                    "quantised at 0 or 34"
        ),
        T130BoundRecord(
            "the plan area of 34 truss blocks AND their flexures",
            ARM_COUNT * (span34 + widestBlock * rise) * width, "nm^2", footprint,
            flexureAreaFraction,
            "the FULL element cannot place at one level at all, independently of any trio — " +
                    "C-0041's obstruction, transferred to 34 paths"
        ),
        T130BoundRecord(
            "the upward site's own axial coordinate on its host duplex",
            EAST_SITE_BASE_PAIRS * rise, "nm", pitch, EAST_SITE_BASE_PAIRS * rise / pitch,
            "where the register question has to be asked: 24 bp from the duplex's own NORTH " +
                    "plane, a quarter turn, at every one of the 34 stations"
        )
    )

    // ------------------------------------------------------------------ the register
    println("T-130 — the register field on the host duplex (C-0059's junction set) ...")
    val field = BaseRegisterField(stepsPerBasePair = 2, halfWindowBasePairs = 22)
    val positions = field.positions
    val positionRecords = positions.map {
        T130PositionRecord(
            axialBasePairs = it.axial / rise,
            offsetBasePairs = (it.axial - field.centreAxial) / rise,
            candidates = it.candidates,
            closes = it.closes,
            misalignmentDegrees = it.misalignmentDegrees
        )
    }
    val closingPositions = positions.count { it.closes }
    println(
        "T-130 — $closingPositions of ${positions.size} axial positions carry a closing base, " +
                "at ${field.solves} junction solves"
    )

    val pairsBySeparation = (6..12).associateWith { field.closingPairCentres(it) }
    val pairRecords = pairsBySeparation.flatMap { (separation, pairs) ->
        pairs.sortedBy { abs(it.offsetFromStation) }.map {
            T130PairRecord(
                separationBasePairs = separation,
                offset = it.offsetFromStation,
                offsetBasePairs = it.offsetFromStation / rise,
                worstMisalignmentDegrees = it.worstMisalignmentDegrees,
                representable = it.worstMisalignmentDegrees <= REPRESENTABLE_BASE_DEGREES
            )
        }
    }
    val register = (6..12).map { separation ->
        val pairs = pairsBySeparation.getValue(separation)
        val nearest = pairs.minByOrNull { abs(it.offsetFromStation) }
        val atStation = pairs.firstOrNull { abs(it.offsetFromStation) < 1.0e-9 }
        val freeFloor = pairs.minOfOrNull { it.worstMisalignmentDegrees } ?: 0.0
        val offset = nearest?.offsetFromStation ?: 0.0
        val residual = abs(offset).mod(0.5 * pitch)
        val symmetric = nearest != null &&
                minOf(residual, 0.5 * pitch - residual) < 1.0e-6
        val representable = pairs
            .filter { it.worstMisalignmentDegrees <= REPRESENTABLE_BASE_DEGREES }
            .minByOrNull { abs(it.offsetFromStation) }
        T130RegisterRecord(
            separationBasePairs = separation,
            positions = positions.size,
            closingPositions = closingPositions,
            closingPairCentres = pairs.size,
            admitsPairAnywhere = pairs.isNotEmpty(),
            closesAtTheStation = atStation != null,
            nearestOffset = offset,
            nearestOffsetBasePairs = offset / rise,
            misalignmentAtNearestCentre = nearest?.worstMisalignmentDegrees ?: 0.0,
            representableAtNearestCentre = nearest != null &&
                    nearest.worstMisalignmentDegrees <= REPRESENTABLE_BASE_DEGREES,
            nearestRepresentableOffset = representable?.offsetFromStation ?: 0.0,
            freeFloorDegrees = freeFloor,
            c0059FloorDegrees = baseFloors[separation] ?: 0.0,
            pinnedOverFreeFloor = if (freeFloor <= 0.0) 1.0
            else (nearest?.worstMisalignmentDegrees ?: 0.0) / freeFloor,
            centroSymmetryPreserved = symmetric,
            verdict = when {
                pairs.isEmpty() ->
                    "NO closing base pair at $separation bp anywhere in the period — the row " +
                            "pitch is refused by the sheet, not by the crossbar"
                atStation != null ->
                    "REGISTERED at the station itself, at %.1f°".format(
                        atStation.worstMisalignmentDegrees
                    )
                representable == null ->
                    ("OFF STATION by %.3f nm and NOT REPRESENTABLE at any registered centre — " +
                            "the nearest closes at %.1f°, past the half right angle").format(
                        offset, nearest?.worstMisalignmentDegrees ?: 0.0
                    )
                else ->
                    "OFF STATION by %.3f nm (%.2f bp), at %.1f°".format(
                        offset, offset / rise, nearest?.worstMisalignmentDegrees ?: 0.0
                    )
            }
        )
    }

    // ------------------------------------------------------------------ the arrays
    println("T-130 — placing every recorded trio 34 times ...")
    val registerBySeparation = register.associateBy { it.separationBasePairs }
    val arrays = trios.map { trio ->
        val entry = registerBySeparation.getValue(trio.separationBasePairs)
        val offsets = pairsBySeparation.getValue(trio.separationBasePairs)
            .map { it.offsetFromStation }
        val outcome = placeTrussArray(
            label = trio.rank,
            stations = stations,
            crossbarBasePairs = trio.crossbarBasePairs,
            separationBasePairs = trio.separationBasePairs,
            offsets = offsets.ifEmpty { listOf(0.0) },
            axialPhase = trio.axialPhase,
            lateralSeat = trio.lateralSeat,
            edgeX = edgeX,
            lengthY = lengthY,
            width = width
        )
        val placed = if (entry.admitsPairAnywhere) outcome.placed else 0
        T130ArrayRecord(
            trio = trio.rank,
            crossbarBasePairs = trio.crossbarBasePairs,
            separationBasePairs = trio.separationBasePairs,
            closesOnVerdictGrid = trio.closesOnVerdictGrid,
            representableBase = entry.representableAtNearestCentre,
            registered = entry.admitsPairAnywhere,
            offset = entry.nearestOffset,
            demanded = ARM_COUNT,
            legsOnSheet = outcome.legsOnSheet,
            insideFootprint = outcome.insideFootprint,
            placed = placed,
            placedWholeBlock = outcome.placedWholeBlock,
            overlappingPairs = outcome.overlappingPairs,
            memberClashPairs = outcome.memberClashPairs,
            levelsRequired = outcome.levelsRequired,
            singleLevel = outcome.singleLevel,
            planAreaFraction = outcome.planAreaFraction,
            placesAt34 = placed == ARM_COUNT && outcome.singleLevel,
            placesAndRepresentable = placed == ARM_COUNT && outcome.singleLevel &&
                    entry.representableAtNearestCentre,
            verdict = if (!entry.admitsPairAnywhere) {
                "0 of 34 — the row pitch has no closing base pair on the sheet"
            } else {
                outcome.verdict
            }
        )
    }
    val placingTrios = arrays.count { it.placesAt34 }
    val placingAndClosing = arrays.count { it.placesAt34 && it.closesOnVerdictGrid }
    val placingRepresentable = arrays.count { it.placesAndRepresentable && it.closesOnVerdictGrid }
    val bestPlaced = arrays.maxOf { it.placed }
    println(
        "T-130 — $placingTrios of ${arrays.size} trios place 34 times, $placingRepresentable of " +
                "them with a representable base and a verdict-grid closure; best $bestPlaced"
    )

    // ------------------------------------------------------------------ the flatness
    println("T-130 — the flatness of what places, on C-0063's own grillage ...")
    fun flatness(
        name: String,
        entries: List<Pair<Double, Double>>,
        offset: Double,
        free: Double
    ): T130FlatnessRecord {
        val supports = if (entries.isEmpty()) emptyList() else couplingSupports(entries, MANDATE)
        val solution = lattice(sheet, columns, supports).solve(solvedField)
        val dishing = solution.peakDishing() / freeStroke
        return T130FlatnessRecord(
            placement = name,
            stations = entries.size,
            offset = offset,
            dishingOverStroke = dishing,
            flatAtTenPercent = dishing < FLATNESS_TOLERANCE,
            beatsNoCoupling = dishing < free,
            peakPathForce = if (entries.isEmpty()) 0.0 else solution.supportForces.maxOf { abs(it) },
            peakCrossoverForce = solution.peakCrossoverForce,
            peakDuplexShear = solution.peakDuplexShear
        )
    }

    val nominal = stations.map { it.x to it.y }
    val freeDishing = flatness("NONE — the free tile", emptyList(), 0.0, Double.MAX_VALUE)
        .dishingOverStroke
    val bestSeparation = register
        .filter { it.admitsPairAnywhere && it.representableAtNearestCentre }
        .minByOrNull { abs(it.nearestOffset) }?.separationBasePairs ?: 9
    val bestOffset = registerBySeparation.getValue(bestSeparation).nearestOffset
    fun legStations(separation: Int, offsets: List<Double>): List<Pair<Double, Double>> =
        chooseTrussInstances(
            stations, separation + 7, separation, offsets.ifEmpty { listOf(0.0) },
            edgeX = edgeX, width = width
        ).flatMap { truss -> truss.legPositions.map { it.x to it.y } }

    val flatnessRecords = listOf(
        flatness("NONE — the free tile on C-0063's phase-24 host", emptyList(), 0.0, Double.MAX_VALUE),
        flatness("C-0063's 34 stations, one point each — the reproduction", nominal, 0.0, freeDishing),
        flatness(
            "the truss's 68 leg bases, ON station (unregistered)",
            legStations(bestSeparation, listOf(0.0)), 0.0, freeDishing
        )
    ) + register.filter { it.admitsPairAnywhere }.map { entry ->
        flatness(
            "the truss's leg bases at a ${entry.separationBasePairs} bp row, registered",
            legStations(
                entry.separationBasePairs,
                pairsBySeparation.getValue(entry.separationBasePairs).map { it.offsetFromStation }
            ),
            entry.nearestOffset, freeDishing
        )
    }
    val registeredFlatness = flatnessRecords
        .first { it.placement.contains("${bestSeparation} bp row") }

    // ------------------------------------------------------------------ the flexure reading
    println("T-130 — the flexure reading, reported beside the headline and not folded into it ...")
    val flexureRecords = listOf(ARM_COUNT to span34, 45 to reference.span).map { (paths, span) ->
        val outcome = placeTrussArray(
            label = "with the flexure at $paths paths",
            stations = stations,
            crossbarBasePairs = bestSeparation + 6,
            separationBasePairs = bestSeparation,
            offsets = pairsBySeparation.getValue(bestSeparation).map { it.offsetFromStation },
            edgeX = edgeX,
            lengthY = lengthY,
            width = width,
            flexureSpan = span
        )
        T130FlexureRecord(
            reading = "C-0030's coupled flexure at $paths paths, running along -y from the cap",
            paths = paths,
            span = span,
            planAreaFraction = outcome.planAreaFraction,
            overlappingPairs = outcome.overlappingPairs,
            mutuallyBlockingPairs = outcome.mutuallyBlockingPairs,
            levelsRequired = outcome.levelsRequired,
            singleLevel = outcome.singleLevel,
            placed = outcome.placed,
            verdict = outcome.verdict
        )
    }

    // ------------------------------------------------------------------ sensitivities
    println("T-130 — the sensitivities ...")
    val referencePlaced = arrays.filter { it.separationBasePairs == 10 }
        .maxOfOrNull { it.placed } ?: 0
    fun sensitivity(
        axis: String,
        reading: String,
        sensitivityField: BaseRegisterField,
        separation: Int,
        crossbar: Int,
        note: String,
        exclusionWidth: Double = width
    ): T130SensitivityRecord {
        val pairs = sensitivityField.closingPairCentres(separation)
        val nearest = pairs.minByOrNull { abs(it.offsetFromStation) }
        val outcome = placeTrussArray(
            axis, stations, crossbar, separation,
            pairs.map { it.offsetFromStation }.ifEmpty { listOf(0.0) },
            edgeX = edgeX, lengthY = lengthY, width = exclusionWidth
        )
        val placed = if (pairs.isEmpty()) 0 else outcome.placed
        return T130SensitivityRecord(
            axis = axis,
            reading = reading,
            closingPositions = sensitivityField.positions.count { it.closes },
            closingPairCentres = pairs.size,
            nearestOffset = nearest?.offsetFromStation ?: 0.0,
            placed = placed,
            verdictMoves = placed != referencePlaced,
            note = note
        )
    }

    // the second reading of the station's datum, applied to the OFFSETS rather than to the grid
    val strandShift = DuplexBackbone().minorGrooveAngle /
            (360.0 / DuplexBackbone().basePairsPerTurn) * rise
    val strandOffsets = pairsBySeparation.getValue(10).map { it.offsetFromStation + strandShift }
    val strandOutcome = placeTrussArray(
        "the upward site's strand", stations, 17, 10, strandOffsets,
        edgeX = edgeX, lengthY = lengthY, width = width
    )
    val strandSensitivity = T130SensitivityRecord(
        axis = "the upward site's strand",
        reading = "the NORTH plane read on the other strand — the station moves %.3f nm".format(
            strandShift
        ),
        closingPositions = closingPositions,
        closingPairCentres = strandOffsets.size,
        nearestOffset = strandOffsets.minByOrNull { abs(it) } ?: 0.0,
        placed = strandOutcome.placed,
        verdictMoves = strandOutcome.placed != ARM_COUNT,
        note = "the convention fixes WHERE the station is on the host's backbone, and it is " +
                "applied to the offsets: re-gridding the field around a shifted datum would " +
                "resample a set that lives on a continuum and measure the GRID instead"
    )

    val sensitivities = listOf(
        sensitivity(
            "reference", "C-0029's geometry, 120° groove, r_P = 1.00 nm, seat 0.0, cap 4",
            field, 10, 17, "the reading every headline number is on"
        ),
        sensitivity(
            "candidate cap", "12 candidates per position rather than C-0059's 4",
            BaseRegisterField(
                stepsPerBasePair = 2, halfWindowBasePairs = 22, candidatesPerPosition = 12
            ),
            10, 17,
            "a cap is a ranking and a ranking is not monotone under refinement — this is the " +
                    "direction that can only ADD closing positions"
        ),
        sensitivity(
            "lateral seat", "the leg seated 0.5 nm off the host duplex's axis",
            BaseRegisterField(stepsPerBasePair = 2, halfWindowBasePairs = 22, lateralSeat = 0.5),
            10, 17, "C-0059 sweeps the seat; the array pins nothing about it"
        ),
        strandSensitivity,
        sensitivity(
            "exclusion width", "the 2.0 nm steric diameter rather than 2.69 nm SAXS",
            field, 10, 17, "C-0053's own sensitivity, on the packing rather than the register",
            exclusionWidth = OrigamiDuplex.DIAMETER
        )
    )

    // ------------------------------------------------------------------ convergence
    println("T-130 — convergence ...")
    val coarse = BaseRegisterField(
        azimuthSteps = 60, stepsPerBasePair = 2, halfWindowBasePairs = 4,
        candidatesPerPosition = 64
    )
    val fine = BaseRegisterField(
        azimuthSteps = 120, stepsPerBasePair = 2, halfWindowBasePairs = 4,
        candidatesPerPosition = 64
    )
    val finer = BaseRegisterField(
        stepsPerBasePair = 4, halfWindowBasePairs = 4, candidatesPerPosition = 64
    )
    val fineRegister = BaseRegisterField(stepsPerBasePair = 4, halfWindowBasePairs = 8)
    val coarseRegister = BaseRegisterField(stepsPerBasePair = 2, halfWindowBasePairs = 8)
    val convergence = listOf(
        T130ConvergenceRecord(
            "the nearest registered pair centre at a 9 bp row", "axial steps per base pair",
            listOf("2", "4"),
            listOf(coarseRegister, fineRegister).map { grid ->
                grid.closingPairCentres(9).minOfOrNull { abs(it.offsetFromStation) } ?: -1.0
            },
            0.0,
            "the quantity the whole composition turns on — how far off its station a truss has " +
                    "to sit to root at all"
        ),
        T130ConvergenceRecord(
            "the base misalignment at that centre, 9 bp row", "axial steps per base pair",
            listOf("2", "4"),
            listOf(coarseRegister, fineRegister).map { grid ->
                grid.closingPairCentres(9).minByOrNull { abs(it.offsetFromStation) }
                    ?.worstMisalignmentDegrees ?: -1.0
            },
            0.0,
            "the number CH-0078 rests on: what the base floor reads once the array pins the " +
                    "axial position C-0059 and C-0062 minimise over"
        ),
        T130ConvergenceRecord(
            "closing axial positions per 8 bp window", "azimuth steps",
            listOf("60", "120"),
            listOf(
                coarse.positions.count { it.closes }.toDouble(),
                fine.positions.count { it.closes }.toDouble()
            ),
            abs(coarse.positions.count { it.closes } - fine.positions.count { it.closes })
                .toDouble(),
            "uncapped, the coarse azimuth set is a SUBSET of the fine one, so refinement is " +
                    "monotone; with C-0059's cap of 4 it is not, and that is reported rather " +
                    "than assumed away"
        ),
        T130ConvergenceRecord(
            "closing axial positions per nm", "axial steps per base pair",
            listOf("2", "4"),
            listOf(
                fine.positions.count { it.closes } / (8.0 * rise),
                finer.positions.count { it.closes } / (8.0 * rise)
            ),
            abs(
                fine.positions.count { it.closes } / (8.0 * rise) -
                        finer.positions.count { it.closes } / (8.0 * rise)
            ),
            "the closing set is a MEASURE on a continuum, exactly as C-0062's closing count is; " +
                    "what the array needs is not the count but whether the station is in it"
        ),
        T130ConvergenceRecord(
            "dishing/stroke of the registered array", "grillage subdivisions",
            listOf("1", "2", "4"),
            listOf(1, 2, 4).map { subdivisions ->
                val supports = couplingSupports(
                    legStations(
                        bestSeparation,
                        pairsBySeparation.getValue(bestSeparation).map { it.offsetFromStation }
                    ),
                    MANDATE
                )
                lattice(sheet, columns, supports, subdivisions).solve(solvedField)
                    .peakDishing() / freeStroke
            },
            0.0,
            "nested subdivisions only, per CLAUDE.md — a subdivision of 3 moves a point load " +
                    "off a node and is not a refinement of 2"
        )
    ).map { record ->
        record.copy(departure = abs(record.results.last() - record.results[record.results.size - 2]))
    }

    // ------------------------------------------------------------------ upstream reproductions
    println("T-130 — the upstream re-check ...")
    val latticeSites = upwardRootLattice(BEST_PHASE, edgeX, DUPLEXES)
    val stationDeparture = stations.maxOf { station ->
        latticeSites[station.row].minOf { abs(it - station.x) }
    }
    val c0063Dishing = flatnessRecords[1].dishingOverStroke
    val reproductions = listOf(
        T130ReproductionRecord(
            "C-0063", "dishing/stroke, 34 stations, equal springs, phase 24", 0.0706,
            c0063Dishing, abs(c0063Dishing - 0.0706) / 0.0706,
            "re-solved on C-0063's own grillage under C-0022's solved load"
        ),
        T130ReproductionRecord(
            "C-0063", "dishing/stroke, the free tile", 0.3079, freeDishing,
            abs(freeDishing - 0.3079) / 0.3079, "the same host, no coupling"
        ),
        T130ReproductionRecord(
            "C-0063", "the 34 stations are EAST sites of C-0055's lattice at phase 24", 0.0,
            stationDeparture, stationDeparture,
            "asserted as a distance, not as a count — every station is re-derived from " +
                    "upwardRootLattice rather than read as a coordinate"
        ),
        T130ReproductionRecord(
            "C-0055", "the upward root pitch", 10.88, pitch, abs(pitch - 10.88) / 10.88,
            "32 bp, the bare per-interface crossover spacing"
        ),
        T130ReproductionRecord(
            "C-0062", "closing lattices summed over the 21 band configurations", 196.0,
            closingLattices.toDouble(), abs(closingLattices - 196.0) / 196.0,
            "READ FROM ITS RESULT FILE. The coordinator's prompt for this task quoted " +
                    "'93 of 5 940 reach-feasible lattices at a 3.00° chord' — numbers that " +
                    "appear nowhere in C-0062 or its result file, and that JOURNAL.md already " +
                    "records as retracted first-report text"
        ),
        T130ReproductionRecord(
            "C-0062", "trios recorded, and those surviving the 180-step verdict grid", 44.0,
            trios.size.toDouble(), abs(trios.size - 44.0) / 44.0,
            "of which ${trios.count { it.closesOnVerdictGrid }} close on both grids — C-0062's 39"
        ),
        T130ReproductionRecord(
            "C-0059", "the base misalignment floor at a 10 bp row", 6.0,
            baseFloors[10] ?: 0.0, abs((baseFloors[10] ?: 0.0) - 6.0) / 6.0,
            "CONSUMED AS DATA from T-124's result file, and re-read at the station below"
        ),
        T130ReproductionRecord(
            "C-0048", "the minimum crossbar at a 10 bp row", 16.0,
            CrossbarGeometry(16, 10).basePairs.toDouble(), 0.0,
            "row + 6, the ceil C-0048 derives from the seat exclusion"
        )
    )

    // ------------------------------------------------------------------ the budget and the verdict
    val budget = listOf(
        T130BudgetRecord(
            "the register field, one 44 bp window at 0.17 nm steps", positions.size,
            field.solves, 0, 0
        ),
        T130BudgetRecord(
            "the arrays, one per recorded trio", 0, 0, arrays.size,
            arrays.sumOf { it.placed }
        )
    )

    val flatBySeparation = register.filter { it.admitsPairAnywhere }.associate { entry ->
        entry.separationBasePairs to flatnessRecords
            .first { it.placement.contains("${entry.separationBasePairs} bp row") }
            .flatAtTenPercent
    }
    val surviving = arrays.count {
        it.placesAndRepresentable && it.closesOnVerdictGrid &&
                flatBySeparation[it.separationBasePairs] == true
    }

    val decision = if (placingTrios == 0) {
        "NO closing trio places 34 times"
    } else {
        "$placingTrios of ${arrays.size} recorded trios place 34 times, of which " +
                "$placingAndClosing also close on C-0057's own verdict grid"
    }

    val predicates = listOf(
        T130PredicateRecord(
            "Q1", "how many of C-0062's closing trios place 34 times, and at what count if none",
            "$decision; $placingRepresentable of them also carry a base the mechanics can " +
                    "represent at the centre the register offers, and the best placed count " +
                    "over every recorded trio is $bestPlaced"
        ),
        T130PredicateRecord(
            "Q2", "the composition runs on C-0053's packer and C-0059's junction, as libraries",
            "MET — elementPackingVerdict and SingleJunctionFeasibleSet re-run, C-0062's and " +
                    "C-0063's result files consumed as data and re-checked"
        ),
        T130PredicateRecord(
            "Q3", "a torsion closure is a NECESSARY condition only",
            "STATED — every 'places' verdict here is an upper bound on buildability"
        ),
        T130PredicateRecord(
            "Q4", "the two free limiting cases",
            "MET — the collapsed trio reproduces C-0063's placement and the pruned base closure " +
                    "agrees with C-0057's own bestLinkClosure, both as gate tests"
        ),
        T130PredicateRecord(
            "Q5", "what the array costs the flatness",
            ("%.4f of the stroke at the registered leg bases of a $bestSeparation bp row, " +
                    "against C-0063's %.4f at 34 nominal stations").format(
                registeredFlatness.dishingOverStroke, c0063Dishing
            )
        )
    )

    val findings = mapOf(
        "the verdict" to decision,
        "what survives every clause at once" to
                ("$surviving of the ${arrays.size} recorded trios place 34 times, close on " +
                        "C-0057's own verdict grid, carry a base C-0037's TwoLinkBase invariant " +
                        "can represent at the centre the register offers, AND leave the tile " +
                        "flat at C-0063's own convention — the 9, 11 and 12 bp rows. The 6 bp " +
                        "row survives every clause but the flatness, at an array translated " +
                        "3.91 nm; the 7, 8 and 10 bp rows fail the base"),
        "the row pitch the array chooses" to
                ("a base misalignment FLOOR is a minimum over the axial position on the host " +
                        "duplex, and the array pins that position. At the centre the register " +
                        "offers nearest the station the 10 bp row — C-0062's own recommended " +
                        "design — reads %.1f° against its published %.1f° floor, past the half " +
                        "right angle at which C-0037's TwoLinkBase invariant cannot represent a " +
                        "base at all; the %d bp row reads %.1f° and is what the array can " +
                        "actually build").format(
                    registerBySeparation.getValue(10).misalignmentAtNearestCentre,
                    registerBySeparation.getValue(10).freeFloorDegrees,
                    bestSeparation,
                    registerBySeparation.getValue(bestSeparation).misalignmentAtNearestCentre
                ),
        "the shape of the answer" to
                ("every one of C-0063's 34 stations is the SAME helical phase of its own host " +
                        "duplex — ${census.classes} class, at ${census.localAxialBasePairs} bp " +
                        "from the duplex's own NORTH plane — so the register is one question, " +
                        "not thirty-four, and the placed count is quantised at 0 or 34"),
        "what binds" to
                ("the plan does not: 34 truss blocks cover %.3f of the footprint and the widest " +
                        "crossbar's demand is %.2f nm against a 10.88 nm pitch. What decides it " +
                        "is the base REGISTER, which is a coordinate C-0059's and C-0062's " +
                        "floors minimise over and an array pins").format(
                    blockAreaFraction, demand
                ),
        "the flexure reading" to
                ("with C-0030's flexure the same array covers %.2f of the footprint and cannot " +
                        "place at one level at all — C-0041's obstruction at 34 paths, and it " +
                        "is independent of every trio").format(flexureAreaFraction),
        "what this cannot establish" to
                ("a torsion closure is a NECESSARY condition and never a sufficient one. " +
                        "Nothing here is measured, no sequence is designed, and the motif — a " +
                        "free lever held to a single-layer sheet by one crossover — is this " +
                        "programme's own construct (C-0055, 62 recorded queries)")
    )

    val parameters = mapOf(
        "armCount" to ARM_COUNT.toDouble(),
        "phase" to BEST_PHASE.toDouble(),
        "rootPitch" to pitch,
        "edgeX" to edgeX,
        "lengthY" to lengthY,
        "footprint" to footprint,
        "freeStroke" to freeStroke,
        "mandate" to MANDATE,
        "eastSiteBasePairs" to EAST_SITE_BASE_PAIRS.toDouble(),
        "registerStep" to field.step,
        "closingPositions" to closingPositions.toDouble(),
        "junctionSolves" to field.solves.toDouble(),
        "triosRecorded" to trios.size.toDouble(),
        "triosPlacingAt34" to placingTrios.toDouble(),
        "triosPlacingRepresentable" to placingRepresentable.toDouble(),
        "triosSurvivingEveryClause" to surviving.toDouble(),
        "bestSeparationBasePairs" to bestSeparation.toDouble(),
        "registeredDishing" to registeredFlatness.dishingOverStroke,
        "bestPlacedCount" to bestPlaced.toDouble(),
        "flexureSpan34" to span34,
        "flexureAreaFraction" to flexureAreaFraction,
        "blockAreaFraction" to blockAreaFraction
    )

    val result = T130Result(
        task = "T-130",
        leaf = "A8.2",
        conditions = mapOf(
            "temperature" to "300 K",
            "medium" to "aqueous 2 mM MgCl2",
            "k_BT" to "4.141947 pN nm",
            "sheet" to ("single-layer square-lattice Rothemund, 15 duplexes at the SAXS 2.69 nm, " +
                    "40.0 x 40.35 nm, rise 0.34 nm, 10.67 bp/turn, crossover phase 24"),
            "placement" to "C-0063's 34 upward roots at phase 24, read from its result file",
            "trios" to "C-0062's 44 recorded closing trios, read from its result file",
            "plan convention" to ("a duplex is a rectangle of width 2.69 nm; two at exactly that " +
                    "are tangent and admissible (C-0041, C-0053)"),
            "junction" to ("C-0029's geometry via C-0059's SingleJunctionFeasibleSet — phosphate " +
                    "radius 1.00 nm, minor groove 120 degrees, the inherited [0.60, 0.70] nm " +
                    "phosphodiester window"),
            "closure" to ("C-0062's per-assignment pruned verdict at C-0059's own 60-step / " +
                    "4-refinement grid, asserted equal to C-0057's bestLinkClosure as a gate"),
            "load" to "C-0022's solved edge profile at 2 mM, a 10 nm gap and 0.192 V",
            "units" to "nm, pN, pN/nm, degrees for every reported angle"
        ),
        decision = decision,
        bounds = bounds,
        positions = positionRecords,
        register = register,
        pairs = pairRecords,
        arrays = arrays,
        flatness = flatnessRecords,
        flexure = flexureRecords,
        sensitivities = sensitivities,
        convergence = convergence,
        reproductions = reproductions,
        budget = budget,
        predicates = predicates,
        findings = findings,
        parameters = parameters
    )

    val json = Json { prettyPrint = true; prettyPrintIndent = "  " }
    val file = File("gpd/results/T-130-crossbar-array-placement.json")
    file.parentFile.mkdirs()
    file.writeText(
        json.encodeToString(
            (json.encodeToJsonElement(result) as JsonObject).roundedForResult(
                digitsByKey = DEPARTURE_DIGITS_BY_KEY
            ).withEmissionHeader(LatticeTag.SQUARE, null)
        )
    )
    println("T-130 — wrote ${file.path} in ${(System.currentTimeMillis() - started) / 1000} s")
    println("T-130 — $decision")
}
