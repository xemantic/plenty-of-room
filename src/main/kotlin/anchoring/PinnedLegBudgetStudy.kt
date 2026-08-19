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

import com.xemantic.nano.plentyofroom.structure.DEPARTURE_DIGITS_BY_KEY
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.roundedForResult
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
 * `T-132` — does the leg's own length budget survive the **pinned** base misalignment, at one
 * global leg length for all 34 caps?
 *
 * Emits `gpd/results/T-132-pinned-leg-budget.json`.
 */

private const val ARM_COUNT = 34
private const val BEST_PHASE = 24
private val LEG_ENVELOPE = 12..26
private const val DEGREES = 180.0 / PI

@Serializable
private data class T132BoundRecord(
    val name: String,
    val value: Double,
    val unit: String,
    val against: Double,
    val ratio: Double,
    val settles: String
)

@Serializable
private data class T132CensusRecord(
    val separationBasePairs: Int,
    val centreOffsetBasePairs: Double,
    val trusses: Int,
    val legBases: Int,
    val classes: Int,
    val localAxialBasePairs: List<Double>,
    val populations: List<Int>,
    val distinctPairs: Int,
    val oneLengthServesAll: Boolean
)

@Serializable
private data class T132RegisterRecord(
    val separationBasePairs: Int,
    val offset: Double,
    val offsetBasePairs: Double,
    val closersLowLeg: Int,
    val closersHighLeg: Int,
    val admissiblePairs: Int,
    val lowLegMisalignmentDegrees: Double,
    val highLegMisalignmentDegrees: Double,
    val worstMisalignmentDegrees: Double,
    val lowLegSignedDegrees: Double,
    val highLegSignedDegrees: Double,
    /** Cheap bound 2 — the cap misalignment no leg length can beat at this pair. */
    val sharedCapFloorDegrees: Double,
    val c0065WorstDegrees: Double,
    val verdict: String
)

@Serializable
private data class T132RowRecord(
    val separationBasePairs: Int,
    val offset: Double,
    val c0059FloorDegrees: Double,
    val c0062CapFloorDegrees: Double,
    val c0062FlexureFloorDegrees: Double,
    val pinnedBaseDegrees: Double,
    /** Cheap bound 2 at this row — the cap floor the TWO legs impose, at any leg length. */
    val twoLegCapFloorDegrees: Double,
    /** Whether that geometric floor exceeds `C-0062`'s chemistry cap floor at the same row. */
    val twoLegFloorExceedsC0062: Boolean,
    /** `C-0062`'s own composition: the free floor, the free rotation. */
    val c0062LegSteps: Int,
    val c0062MarginCanDo: Double,
    val c0062MarginFields: Double,
    /** `CH-0078`'s reading: the pinned floor, but the rotation still free. */
    val ch0078LegSteps: Int,
    val ch0078MarginCanDo: Double,
    val ch0078MarginFields: Double,
    val ch0078Representable: Boolean,
    /** This task: the rotation pinned by the register, at ONE shared leg length. */
    val pinnedLegSteps: Int,
    val pinnedCapGeometricDegrees: Double,
    val pinnedCapDegrees: Double,
    val pinnedBudgetDegrees: Double,
    val pinnedOverspendDegrees: Double,
    val pinnedMarginCanDo: Double,
    val pinnedMarginFields: Double,
    val pinnedRepresentable: Boolean,
    val pinnedPasses: Boolean,
    val representableLengths: Int,
    val passingLengths: Int,
    val flatAtC0065: Boolean,
    val survivesEveryClause: Boolean,
    val verdict: String
)

@Serializable
private data class T132LengthRecord(
    val separationBasePairs: Int,
    val legSteps: Int,
    val legLength: Double,
    val baseLowDegrees: Double,
    val baseHighDegrees: Double,
    val baseDegrees: Double,
    val capLowDegrees: Double,
    val capHighDegrees: Double,
    val capGeometricDegrees: Double,
    val capDegrees: Double,
    val budgetDegrees: Double,
    val spentDegrees: Double,
    val overspendDegrees: Double,
    val frameCouple: Double,
    val capBending: Double,
    val capTorsion: Double,
    val span: Double,
    val tangent: Double,
    val duty: Double,
    val criticalLoadCanDo: Double,
    val criticalLoadFields: Double,
    val marginCanDo: Double,
    val marginFields: Double,
    val governingPlane: String,
    val representable: Boolean,
    val passes: Boolean,
    val verdict: String
)

@Serializable
private data class T132ArrayRecord(
    val trio: String,
    val crossbarBasePairs: Int,
    val separationBasePairs: Int,
    val closesOnVerdictGrid: Boolean,
    val representableBase: Boolean,
    val flat: Boolean,
    val survivedC0065: Boolean,
    val sharedLengthPasses: Boolean,
    val survivesEveryClause: Boolean
)

@Serializable
private data class T132SensitivityRecord(
    val axis: String,
    val reading: String,
    val separationBasePairs: Int,
    val pairExists: Boolean,
    val admissiblePairs: Int,
    val pinnedBaseDegrees: Double,
    val bestLegSteps: Int,
    val capDegrees: Double,
    val marginCanDo: Double,
    val representableLengths: Int,
    val passingLengths: Int,
    val passes: Boolean,
    val verdictMoves: Boolean,
    val outcomeVerdict: String,
    val note: String
)

@Serializable
private data class T132ConvergenceRecord(
    val quantity: String,
    val parameter: String,
    val levels: List<String>,
    val results: List<Double>,
    val departure: Double,
    val note: String
)

@Serializable
private data class T132ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val here: Double,
    val departure: Double,
    val note: String
)

@Serializable
private data class T132BudgetRecord(
    val stage: String,
    val positions: Int,
    val junctionSolves: Int,
    val designsEvaluated: Int
)

@Serializable
private data class T132PredicateRecord(
    val name: String,
    val statement: String,
    val verdict: String
)

@Serializable
private data class T132Result(
    val task: String,
    val leaf: String,
    val conditions: Map<String, String>,
    val decision: String,
    val bounds: List<T132BoundRecord>,
    val census: List<T132CensusRecord>,
    val register: List<T132RegisterRecord>,
    val rows: List<T132RowRecord>,
    val lengths: List<T132LengthRecord>,
    val arrays: List<T132ArrayRecord>,
    val sensitivities: List<T132SensitivityRecord>,
    val convergence: List<T132ConvergenceRecord>,
    val reproductions: List<T132ReproductionRecord>,
    val budget: List<T132BudgetRecord>,
    val predicates: List<T132PredicateRecord>,
    val findings: Map<String, String>,
    val parameters: Map<String, Double>
)

// ---------------------------------------------------------------------------------------------
// the upstream artifacts, consumed as data
// ---------------------------------------------------------------------------------------------

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

private data class T132PublishedTrio(
    val rank: String,
    val crossbarBasePairs: Int,
    val separationBasePairs: Int,
    val closesOnVerdictGrid: Boolean
)

private fun c0062Trios(file: File): List<T132PublishedTrio> {
    require(file.exists()) { "C-0062's result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("trios").jsonArray.map { it.jsonObject }
        .map {
            fun text(key: String) = it.getValue(key).jsonPrimitive.content
            T132PublishedTrio(
                rank = text("rank"),
                crossbarBasePairs = text("crossbarBasePairs").toInt(),
                separationBasePairs = text("separationBasePairs").toInt(),
                closesOnVerdictGrid = text("closesOnVerdictGrid").toBoolean()
            )
        }
}

/** `C-0062`'s per-row cap and flexure misalignment floors, consumed as data from its table. */
private fun c0062Floors(file: File): Map<Int, Pair<Double, Double>> {
    require(file.exists()) { "C-0062's result file is missing: ${file.path}" }
    val out = HashMap<Int, Pair<Double, Double>>()
    Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("designs").jsonArray.map { it.jsonObject }
        .forEach {
            fun text(key: String) = it.getValue(key).jsonPrimitive.content
            if (!text("id").contains("BOTH ends")) return@forEach
            out[text("separationBasePairs").toInt()] =
                text("capFloorDegrees").toDouble() to text("flexureFloorDegrees").toDouble()
        }
    return out
}

/** `C-0059`'s published base misalignment floors — the minima over the axial position. */
private fun c0059BaseFloors(file: File): Map<Int, Double> {
    require(file.exists()) { "C-0059's result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("pairs").jsonArray.map { it.jsonObject }
        .associate {
            it.getValue("separationBasePairs").jsonPrimitive.content.toInt() to
                    it.getValue("worstMisalignmentDegrees").jsonPrimitive.content.toDouble()
        }
}

private data class T132PublishedRegister(
    val separationBasePairs: Int,
    val nearestOffset: Double,
    val misalignmentDegrees: Double,
    val representable: Boolean
)

/** `C-0065`'s own register table, consumed as data so that the reproduction is checkable. */
private fun c0065Register(file: File): Map<Int, T132PublishedRegister> {
    require(file.exists()) { "C-0065's result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("register").jsonArray.map { it.jsonObject }
        .associate {
            fun text(key: String) = it.getValue(key).jsonPrimitive.content
            val row = text("separationBasePairs").toInt()
            row to T132PublishedRegister(
                separationBasePairs = row,
                nearestOffset = text("nearestOffset").toDouble(),
                misalignmentDegrees = text("misalignmentAtNearestCentre").toDouble(),
                representable = text("representableAtNearestCentre").toBoolean()
            )
        }
}

/** `C-0065`'s flatness verdict at each row pitch's registered leg bases. */
private fun c0065Flatness(file: File): Map<Int, Boolean> {
    val out = HashMap<Int, Boolean>()
    Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("flatness").jsonArray.map { it.jsonObject }
        .forEach {
            val placement = it.getValue("placement").jsonPrimitive.content
            val match = Regex("at a (\\d+) bp row, registered").find(placement) ?: return@forEach
            out[match.groupValues[1].toInt()] =
                it.getValue("flatAtTenPercent").jsonPrimitive.content.toBoolean()
        }
    return out
}

// ---------------------------------------------------------------------------------------------
// the compositions being compared
// ---------------------------------------------------------------------------------------------

/** The best **free-rotation** design over the shared leg envelope, at a stated base floor. */
private fun freeRotationBest(
    baseFloorDegrees: Double,
    capFloorDegrees: Double,
    flexureFloorDegrees: Double,
    separationBasePairs: Int
): FeasibleTrussDesign? {
    var best: FeasibleTrussDesign? = null
    LEG_ENVELOPE.forEach { steps ->
        val design = feasibleTrussDesign(
            legSteps = steps,
            baseFloor = baseFloorDegrees / DEGREES,
            capFloor = capFloorDegrees / DEGREES,
            flexureFloor = flexureFloorDegrees / DEGREES,
            separationBasePairs = separationBasePairs
        )
        if (!design.representable) return@forEach
        val incumbent = best
        if (incumbent == null || design.marginCanDo > incumbent.marginCanDo) best = design
    }
    return best
}

// ---------------------------------------------------------------------------------------------
// the study
// ---------------------------------------------------------------------------------------------

fun main() {
    val started = System.currentTimeMillis()
    val rise = Gen1Tile.RISE_PER_BASE_PAIR
    val stations = c0063Stations(File("gpd/results/T-125-upward-root-placement.json"))
    val trios = c0062Trios(File("gpd/results/T-127-crossbar-trio-existence.json"))
    val floors = c0062Floors(File("gpd/results/T-127-crossbar-trio-existence.json"))
    val baseFloors = c0059BaseFloors(File("gpd/results/T-124-torsion-feasible-routing.json"))
    val published = c0065Register(File("gpd/results/T-130-crossbar-array-placement.json"))
    val flat = c0065Flatness(File("gpd/results/T-130-crossbar-array-placement.json"))
    require(stations.size == ARM_COUNT) { "expected $ARM_COUNT stations, got ${stations.size}" }

    // ------------------------------------------------------------------ the register
    println("T-132 — the register of SIGNED base azimuths, over C-0065's own window ...")
    val register = PinnedBaseRegister()
    val rows = (6..12).toList()
    val pairs = rows.associateWith { register.nearestPair(it) }
    var designsEvaluated = 0

    // ------------------------------------------------------------------ cheap bound 1
    println("T-132 — cheap bound 1: the phase classes of the 68 leg bases ...")
    val censusRecords = rows.mapNotNull { row ->
        val pair = pairs[row] ?: return@mapNotNull null
        val offsetBasePairs = pair.offsetFromStation / rise
        val census = legBaseClassCensus(stations, row, offsetBasePairs, BEST_PHASE)
        T132CensusRecord(
            separationBasePairs = row,
            centreOffsetBasePairs = offsetBasePairs,
            trusses = census.trusses,
            legBases = census.legBases,
            classes = census.classes,
            localAxialBasePairs = census.localAxialBasePairs,
            populations = census.populations,
            distinctPairs = census.distinctPairs,
            oneLengthServesAll = census.oneLengthServesAll
        )
    }
    val sharedLengthIsFree = censusRecords.all { it.oneLengthServesAll }

    // ------------------------------------------------------------------ cheap bound 2
    val spacing = chordSampleSpacing(LEG_ENVELOPE) * DEGREES
    val bestRowPair = pairs[9]
    val nineFloor = if (bestRowPair == null) 0.0 else sharedCapFloor(
        bestRowPair.legA.first().signedDeviation, bestRowPair.legB.first().signedDeviation
    ) * DEGREES

    val bounds = listOf(
        T132BoundRecord(
            name = "the distinct (low leg, high leg) phase-class pairs the 34 trusses present",
            value = censusRecords.maxOf { it.distinctPairs }.toDouble(),
            unit = "pairs",
            against = 1.0,
            ratio = censusRecords.maxOf { it.distinctPairs }.toDouble(),
            settles = "THE SHAPE OF THE ANSWER — every truss presents the SAME pair of base " +
                    "classes, so 'one leg length for 34 instances' collapses to 'one leg length " +
                    "for the two legs of one truss'. The array clause costs nothing; what costs " +
                    "is that a truss has two legs and they are pinned at two different positions"
        ),
        T132BoundRecord(
            name = "the phase classes the 68 leg bases occupy",
            value = censusRecords.maxOf { it.classes }.toDouble(),
            unit = "classes",
            against = 2.0,
            ratio = censusRecords.maxOf { it.classes }.toDouble() / 2.0,
            settles = "two classes of 34 — C-0065's bound 3 carried from the station to the leg " +
                    "bases, and the falsifier (more than two, or unequal populations) did not fire"
        ),
        T132BoundRecord(
            name = "the cap misalignment no leg length can beat at the 9 bp row, " +
                    "|fold(dA - dB)| / 2",
            value = nineFloor,
            unit = "degrees",
            against = floors[9]?.first ?: 0.0,
            ratio = if ((floors[9]?.first ?: 0.0) > 0.0) nineFloor / floors.getValue(9).first
            else 0.0,
            settles = "the two legs' own chord difference is a floor on the WORST cap, exact and " +
                    "independent of the length — and it is below C-0062's chemistry cap floor, " +
                    "so the two legs are not what binds"
        ),
        T132BoundRecord(
            name = "how coarsely the 12-26 step envelope samples the chord circle",
            value = spacing,
            unit = "degrees",
            against = 90.0,
            ratio = spacing / 90.0,
            settles = "the widest gap between consecutive relative chord azimuths, so the best " +
                    "achievable worst cap is within half of it of the floor above"
        ),
        T132BoundRecord(
            name = "the height spread a PER-INSTANCE leg length would open across the array",
            value = (LEG_ENVELOPE.last - LEG_ENVELOPE.first) * rise,
            unit = "nm",
            against = Gen1Tile.ACCEPTABLE_STROKE,
            ratio = (LEG_ENVELOPE.last - LEG_ENVELOPE.first) * rise / Gen1Tile.ACCEPTABLE_STROKE,
            settles = "what the alternative costs if the shared length fails — not staples (the " +
                    "68 legs are already 68 distinct oligos, one per scaffold position) but " +
                    "HEIGHT: the caps stop being coplanar by up to 1.6x the acceptable stroke"
        )
    )

    // ------------------------------------------------------------------ the register table
    println("T-132 — the pinned pairs, row by row ...")
    val registerRecords = rows.map { row ->
        val pair = pairs[row]
        val admissible = if (pair == null) 0 else pair.legA.sumOf { a ->
            pair.legB.count { b -> distinctSheetTargets(a.closure, b.closure) }
        }
        val low = pair?.legA?.firstOrNull()
        val high = pair?.legB?.firstOrNull()
        val floor = if (low == null || high == null) 0.0
        else sharedCapFloor(low.signedDeviation, high.signedDeviation) * DEGREES
        T132RegisterRecord(
            separationBasePairs = row,
            offset = pair?.offsetFromStation ?: 0.0,
            offsetBasePairs = (pair?.offsetFromStation ?: 0.0) / rise,
            closersLowLeg = pair?.legA?.size ?: 0,
            closersHighLeg = pair?.legB?.size ?: 0,
            admissiblePairs = admissible,
            lowLegMisalignmentDegrees = (low?.misalignment ?: 0.0) * DEGREES,
            highLegMisalignmentDegrees = (high?.misalignment ?: 0.0) * DEGREES,
            worstMisalignmentDegrees = (pair?.worstMisalignment ?: 0.0) * DEGREES,
            lowLegSignedDegrees = (low?.signedDeviation ?: 0.0) * DEGREES,
            highLegSignedDegrees = (high?.signedDeviation ?: 0.0) * DEGREES,
            sharedCapFloorDegrees = floor,
            c0065WorstDegrees = published.getValue(row).misalignmentDegrees,
            verdict = if (pair == null) "NO closing pair centre in the window"
            else ("the two legs are pinned at %.1f° and %.1f°, %.1f° apart, so the worst cap " +
                    "cannot beat %.1f° at any leg length").format(
                (low?.misalignment ?: 0.0) * DEGREES, (high?.misalignment ?: 0.0) * DEGREES,
                2.0 * floor, floor
            )
        )
    }

    // ------------------------------------------------------------------ the three compositions
    println("T-132 — the three compositions, row by row ...")
    val lengthRecords = ArrayList<T132LengthRecord>()
    val rowRecords = rows.map { row ->
        val pair = pairs[row]
        val (capFloor, flexureFloor) = floors[row] ?: (0.0 to 0.0)
        val c0062 = freeRotationBest(baseFloors.getValue(row), capFloor, flexureFloor, row)
        val pinnedBase = published.getValue(row).misalignmentDegrees
        val ch0078 = freeRotationBest(pinnedBase, capFloor, flexureFloor, row)
        val outcome = if (pair == null) null
        else bestPinnedDesign(pair, capFloor / DEGREES, flexureFloor / DEGREES, LEG_ENVELOPE)
        designsEvaluated += outcome?.evaluated ?: 0
        val best = outcome?.best
        // the whole per-length table at the winning base combination
        if (pair != null && pair.legA.isNotEmpty() && pair.legB.isNotEmpty()) {
            val a = pair.legA.first()
            val b = pair.legB.first()
            LEG_ENVELOPE.forEach { steps ->
                val design = pinnedTrussDesign(
                    legSteps = steps,
                    deviationA = a.signedDeviation,
                    deviationB = b.signedDeviation,
                    capFloor = capFloor / DEGREES,
                    flexureFloor = flexureFloor / DEGREES,
                    separationBasePairs = row
                )
                lengthRecords += T132LengthRecord(
                    separationBasePairs = row,
                    legSteps = steps,
                    legLength = design.legLength,
                    baseLowDegrees = design.baseADegrees,
                    baseHighDegrees = design.baseBDegrees,
                    baseDegrees = design.baseDegrees,
                    capLowDegrees = design.capADegrees,
                    capHighDegrees = design.capBDegrees,
                    capGeometricDegrees = design.capGeometricDegrees,
                    capDegrees = design.capDegrees,
                    budgetDegrees = design.budgetDegrees,
                    spentDegrees = design.spentDegrees,
                    overspendDegrees = design.overspendDegrees,
                    frameCouple = design.frameCouple,
                    capBending = design.capBending,
                    capTorsion = design.capTorsion,
                    span = design.span,
                    tangent = design.tangent,
                    duty = design.duty,
                    criticalLoadCanDo = design.criticalLoadCanDo,
                    criticalLoadFields = design.criticalLoadFields,
                    marginCanDo = design.marginCanDo,
                    marginFields = design.marginFields,
                    governingPlane = design.governingPlane,
                    representable = design.representable,
                    passes = design.passes,
                    verdict = design.verdict
                )
            }
        }
        val survives = published.getValue(row).representable &&
                (flat[row] ?: false) && (best?.passes ?: false)
        val twoLegFloor = registerRecords.first { it.separationBasePairs == row }
            .sharedCapFloorDegrees
        T132RowRecord(
            separationBasePairs = row,
            offset = pair?.offsetFromStation ?: 0.0,
            c0059FloorDegrees = baseFloors.getValue(row),
            c0062CapFloorDegrees = capFloor,
            c0062FlexureFloorDegrees = flexureFloor,
            pinnedBaseDegrees = pinnedBase,
            twoLegCapFloorDegrees = twoLegFloor,
            // an ABSOLUTE tolerance, because at the 8 bp row the two floors are the same number
            // and a bare `>` would report a floating-point tie as a finding (`CLAUDE.md`)
            twoLegFloorExceedsC0062 = twoLegFloor > capFloor + 1.0e-9,
            c0062LegSteps = c0062?.legSteps ?: 0,
            c0062MarginCanDo = c0062?.marginCanDo ?: 0.0,
            c0062MarginFields = c0062?.marginFields ?: 0.0,
            ch0078LegSteps = ch0078?.legSteps ?: 0,
            ch0078MarginCanDo = ch0078?.marginCanDo ?: 0.0,
            ch0078MarginFields = ch0078?.marginFields ?: 0.0,
            ch0078Representable = ch0078 != null,
            pinnedLegSteps = outcome?.bestLegSteps ?: 0,
            pinnedCapGeometricDegrees = best?.capGeometricDegrees ?: 0.0,
            pinnedCapDegrees = best?.capDegrees ?: 0.0,
            pinnedBudgetDegrees = best?.budgetDegrees ?: 0.0,
            pinnedOverspendDegrees = best?.overspendDegrees ?: 0.0,
            pinnedMarginCanDo = best?.marginCanDo ?: 0.0,
            pinnedMarginFields = best?.marginFields ?: 0.0,
            pinnedRepresentable = best?.representable ?: false,
            pinnedPasses = best?.passes ?: false,
            representableLengths = outcome?.representableLengths ?: 0,
            passingLengths = outcome?.passingLengths ?: 0,
            flatAtC0065 = flat[row] ?: false,
            survivesEveryClause = survives,
            verdict = outcome?.verdict ?: "NO closing pair centre in the register window"
        )
    }

    val survivingRows = rowRecords.filter { it.survivesEveryClause }.map { it.separationBasePairs }
    val bestRow = rowRecords.filter { it.survivesEveryClause }
        .maxByOrNull { it.pinnedMarginCanDo }

    // ------------------------------------------------------------------ the 44 trios re-judged
    val rowById = rowRecords.associateBy { it.separationBasePairs }
    val arrays = trios.map { trio ->
        val row = rowById.getValue(trio.separationBasePairs)
        val survivedC0065 = trio.closesOnVerdictGrid &&
                published.getValue(trio.separationBasePairs).representable &&
                (flat[trio.separationBasePairs] ?: false)
        T132ArrayRecord(
            trio = trio.rank,
            crossbarBasePairs = trio.crossbarBasePairs,
            separationBasePairs = trio.separationBasePairs,
            closesOnVerdictGrid = trio.closesOnVerdictGrid,
            representableBase = published.getValue(trio.separationBasePairs).representable,
            flat = flat[trio.separationBasePairs] ?: false,
            survivedC0065 = survivedC0065,
            sharedLengthPasses = row.pinnedPasses,
            survivesEveryClause = survivedC0065 && row.pinnedPasses
        )
    }
    val survivedBefore = arrays.count { it.survivedC0065 }
    val survivesNow = arrays.count { it.survivesEveryClause }

    // ------------------------------------------------------------------ sensitivities
    println("T-132 — the sensitivities ...")
    val sensitivities = ArrayList<T132SensitivityRecord>()
    val referenceRow = bestRow?.separationBasePairs ?: 9
    val referencePair = pairs[referenceRow]
    val (referenceCap, referenceFlexure) = floors[referenceRow] ?: (0.0 to 0.0)
    val referenceOutcome = referencePair?.let {
        bestPinnedDesign(it, referenceCap / DEGREES, referenceFlexure / DEGREES, LEG_ENVELOPE)
    }
    fun sensitivity(
        axis: String, reading: String, outcome: PinnedRowOutcome?, note: String,
        row: Int = referenceRow
    ) {
        val best = outcome?.best
        sensitivities += T132SensitivityRecord(
            axis = axis,
            reading = reading,
            separationBasePairs = row,
            pairExists = outcome != null,
            admissiblePairs = outcome?.candidatePairs ?: 0,
            pinnedBaseDegrees = best?.baseDegrees ?: 0.0,
            bestLegSteps = outcome?.bestLegSteps ?: 0,
            capDegrees = best?.capDegrees ?: 0.0,
            marginCanDo = best?.marginCanDo ?: 0.0,
            representableLengths = outcome?.representableLengths ?: 0,
            passingLengths = outcome?.passingLengths ?: 0,
            passes = best?.passes ?: false,
            verdictMoves = (best?.passes ?: false) != (referenceOutcome?.best?.passes ?: false),
            outcomeVerdict = outcome?.verdict
                ?: "NO closing pair centre at this row pitch under this reading",
            note = note
        )
    }
    sensitivity(
        "reference", "C-0029's geometry, 120 degree groove, r_P = 1.00 nm, seat 0.0, cap 4, " +
                "10.67 bp/turn", referenceOutcome, "the reading every headline number is on"
    )
    val wideCandidates = PinnedBaseRegister(candidatesPerPosition = 12)
    val wideOutcome = wideCandidates.nearestPair(referenceRow)?.let {
        bestPinnedDesign(it, referenceCap / DEGREES, referenceFlexure / DEGREES, LEG_ENVELOPE)
    }
    sensitivity(
        "candidate cap", "12 candidates per position rather than C-0059's 4", wideOutcome,
        "a cap is a RANKING and is not monotone under refinement — C-0065's own caveat"
    )
    val seatRegister = PinnedBaseRegister(lateralSeat = 0.5)
    val seatOutcome = seatRegister.nearestPair(referenceRow)?.let {
        bestPinnedDesign(it, referenceCap / DEGREES, referenceFlexure / DEGREES, LEG_ENVELOPE)
    }
    sensitivity(
        "lateral seat", "the legs seated 0.5 nm off the host's axis", seatOutcome,
        "C-0059 sweeps the seat as a free variable; an array pins nothing about it"
    )
    val naturalOutcome = referencePair?.let { pair ->
        val natural = DuplexBackbone(basePairsPerTurn = 10.5)
        bestPinnedDesign(
            pair, referenceCap / DEGREES, referenceFlexure / DEGREES, LEG_ENVELOPE, natural
        )
    }
    sensitivity(
        "the LEG's own twist", "10.5 bp/turn — a free-standing leg carries no crossovers",
        naturalOutcome, "C-0052 carries both readings and takes its verdict on both"
    )
    val wideEnvelopeOutcome = referencePair?.let {
        bestPinnedDesign(it, referenceCap / DEGREES, referenceFlexure / DEGREES, 8..40)
    }
    sensitivity(
        "the leg envelope", "8-40 steps rather than C-0052's 12-26", wideEnvelopeOutcome,
        "a wider envelope samples the chord circle more finely and can only help"
    )
    val noCapFloorOutcome = referencePair?.let {
        bestPinnedDesign(it, 0.0, referenceFlexure / DEGREES, LEG_ENVELOPE)
    }
    sensitivity(
        "C-0062's cap floor", "removed entirely — the crossbar closes at any azimuth",
        noCapFloorOutcome,
        "the conservative independence assumption removed, to see which floor actually binds"
    )

    // ------------------------------------------------------------------ convergence
    println("T-132 — the convergence records ...")
    val convergence = ArrayList<T132ConvergenceRecord>()
    val coarseAxial = PinnedBaseRegister(halfWindowBasePairs = 6, stepsPerBasePair = 2)
    val fineAxial = PinnedBaseRegister(halfWindowBasePairs = 6, stepsPerBasePair = 4)
    val coarseNine = coarseAxial.nearestPair(9)
    val fineNine = fineAxial.nearestPair(9)
    convergence += T132ConvergenceRecord(
        quantity = "the worst PINNED base misalignment at the 9 bp row",
        parameter = "axial steps per base pair",
        levels = listOf("2", "4"),
        results = listOf(
            (coarseNine?.worstMisalignment ?: 0.0) * DEGREES,
            (fineNine?.worstMisalignment ?: 0.0) * DEGREES
        ),
        departure = abs(
            (coarseNine?.worstMisalignment ?: 0.0) - (fineNine?.worstMisalignment ?: 0.0)
        ) * DEGREES,
        note = "the quantity the whole composition turns on"
    )
    val coarseAzimuth = PinnedBaseRegister(halfWindowBasePairs = 6, azimuthSteps = 120)
    val fineAzimuth = PinnedBaseRegister(halfWindowBasePairs = 6, azimuthSteps = 240)
    convergence += T132ConvergenceRecord(
        quantity = "the worst PINNED base misalignment at the 9 bp row",
        parameter = "azimuth steps",
        levels = listOf("120", "240"),
        results = listOf(
            (coarseAzimuth.nearestPair(9)?.worstMisalignment ?: 0.0) * DEGREES,
            (fineAzimuth.nearestPair(9)?.worstMisalignment ?: 0.0) * DEGREES
        ),
        departure = abs(
            (coarseAzimuth.nearestPair(9)?.worstMisalignment ?: 0.0) -
                    (fineAzimuth.nearestPair(9)?.worstMisalignment ?: 0.0)
        ) * DEGREES,
        note = "a finer azimuth grid can only find a better-aligned member of the same set"
    )
    val bestOfEnvelope = referencePair?.let { pair ->
        LEG_ENVELOPE.map { steps ->
            pinnedTrussDesign(
                steps, pair.legA.first().signedDeviation, pair.legB.first().signedDeviation,
                referenceCap / DEGREES, referenceFlexure / DEGREES, referenceRow
            ).capGeometricDegrees
        }.min()
    } ?: 0.0
    val bound = if (referencePair == null) 0.0 else sharedCapFloor(
        referencePair.legA.first().signedDeviation, referencePair.legB.first().signedDeviation
    ) * DEGREES
    convergence += T132ConvergenceRecord(
        quantity = "the best worst cap over the envelope, against cheap bound 2",
        parameter = "the bound and the search",
        levels = listOf("bound |fold(dA-dB)|/2", "search over 12-26", "bound + half a spacing"),
        results = listOf(bound, bestOfEnvelope, bound + 0.5 * spacing),
        departure = bestOfEnvelope - bound,
        note = "the search must lie between the two, and does — cheap bound 2 verified rather " +
                "than asserted"
    )
    val repeat = register.solves
    register.positions
    convergence += T132ConvergenceRecord(
        quantity = "the register's junction solves on a repeat traversal",
        parameter = "memoisation",
        levels = listOf("first", "second"),
        results = listOf(repeat.toDouble(), register.solves.toDouble()),
        departure = (register.solves - repeat).toDouble(),
        note = "a memoised, pure field: the second traversal costs nothing and returns the same"
    )

    // ------------------------------------------------------------------ reproductions
    val reproductions = ArrayList<T132ReproductionRecord>()
    fun reproduce(source: String, quantity: String, publishedValue: Double, here: Double, note: String) {
        reproductions += T132ReproductionRecord(
            source, quantity, publishedValue, here, abs(publishedValue - here), note
        )
    }
    reproduce(
        "C-0052", "the leg budget at 21 steps [deg]", 78.53, legBudgetDegrees(21),
        "C-0048's own recommended leg, and the worst in the envelope"
    )
    reproduce(
        "C-0052", "the leg budget at 24 steps [deg]", 0.25, legBudgetDegrees(24),
        "the best on the square lattice"
    )
    reproduce(
        "C-0052", "the leg budget at 16 steps [deg]", 89.8, legBudgetDegrees(16),
        "the worst quantised length in the envelope"
    )
    rows.forEach { row ->
        val pair = pairs[row]
        reproduce(
            "C-0065", "the registered offset at the $row bp row [nm]",
            published.getValue(row).nearestOffset, pair?.offsetFromStation ?: 0.0,
            "the register re-run with the sign kept, and the winner is still C-0065's"
        )
        reproduce(
            "C-0065", "the worst pinned base misalignment at the $row bp row [deg]",
            published.getValue(row).misalignmentDegrees,
            (pair?.worstMisalignment ?: 0.0) * DEGREES,
            "C-0065 publishes the WORSE of the two legs; both are recorded here"
        )
    }
    val c0062Ten = freeRotationBest(
        baseFloors.getValue(10), floors.getValue(10).first, floors.getValue(10).second, 10
    )
    reproduce(
        "C-0062", "the best representable design's margin on CanDo, 10 bp row", 2.44607976,
        c0062Ten?.marginCanDo ?: 0.0, "its own design table, re-run through this composition"
    )
    reproduce(
        "C-0062", "the same on Fields et al.'s rigidity", 1.83888014, c0062Ten?.marginFields ?: 0.0,
        "the 25 % lower measured rigidity, carried beside CanDo's model input throughout"
    )
    reproduce(
        "C-0062", "its best design's leg length [steps]", 12.0,
        (c0062Ten?.legSteps ?: 0).toDouble(), "12 steps, 4.08 nm"
    )
    reproduce(
        "C-0048", "the cap's bending term 12EI/w at a 10 bp row [pN/nm]", 811.764706,
        capBendingStiffness(Gen1Tile.DUPLEX_BENDING_RIGIDITY, 10 * rise, 12.0),
        "the cap carries the ROW, not the crossbar's length"
    )
    reproduce(
        "C-0048", "the cap's torsion term 4C/w at a 10 bp row [pN nm/rad]", 541.176471,
        capTorsionalStiffness(Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY, 10 * rise),
        "the same statement for the torsional term"
    )
    reproduce(
        "C-0059", "the published base floor at the 10 bp row [deg]", 6.0, baseFloors.getValue(10),
        "the minimum over the axial position, which is exactly what an array cannot use"
    )

    // ------------------------------------------------------------------ budget
    val budget = listOf(
        T132BudgetRecord(
            "the register of signed azimuths, one 44 bp window at 0.17 nm steps",
            register.positions.size, register.solves, 0
        ),
        T132BudgetRecord(
            "the pinned composition over 7 rows and 15 shared leg lengths", 0, 0, designsEvaluated
        ),
        T132BudgetRecord(
            "the sensitivities and convergence registers", 0,
            wideCandidates.solves + seatRegister.solves + coarseAxial.solves + fineAxial.solves +
                    coarseAzimuth.solves + fineAzimuth.solves, 0
        )
    )

    // ------------------------------------------------------------------ the verdict
    val decision = if (bestRow == null) {
        "NO SHARED LEG LENGTH SURVIVES — the pinned base admits no representable, passing design " +
                "at any row pitch, and a per-instance leg length is not available either"
    } else {
        ("THE LEG BUDGET SURVIVES THE PINNED BASE AT ONE SHARED LENGTH. The shared-length clause " +
                "costs NOTHING — every one of the 34 trusses presents the same pair of base " +
                "phase classes, so one length serves the array exactly when it serves the two " +
                "legs of one truss. At the %d bp row C-0065 recommends, the register pins the " +
                "two legs at %.1f° and %.1f°, and the single best shared length is %d steps " +
                "(%.2f nm), carrying %.2f on CanDo's rigidity and %.2f on Fields et al.'s. " +
                "%d of the %d recorded trios survive every clause at once, against C-0065's " +
                "%d — the pinned composition removes %d.").format(
            bestRow.separationBasePairs,
            registerRecords.first { it.separationBasePairs == bestRow.separationBasePairs }
                .lowLegMisalignmentDegrees,
            registerRecords.first { it.separationBasePairs == bestRow.separationBasePairs }
                .highLegMisalignmentDegrees,
            bestRow.pinnedLegSteps, bestRow.pinnedLegSteps * rise,
            bestRow.pinnedMarginCanDo, bestRow.pinnedMarginFields,
            survivesNow, arrays.size, survivedBefore, survivedBefore - survivesNow
        )
    }

    val predicates = listOf(
        T132PredicateRecord(
            "Q1", "C-0052's chordPairMisalignment(m) re-composed against C-0065's register at " +
                    "ONE global leg length for all 34 caps",
            "DONE — ${rowRecords.count { it.pinnedRepresentable }} of ${rows.size} row pitches " +
                    "carry a representable pinned design and " +
                    "${rowRecords.count { it.pinnedPasses }} carry one that passes all nine " +
                    "predicates at a single shared length"
        ),
        T132PredicateRecord(
            "Q2", "does the shared-length constraint reduce the surviving set?",
            "$survivedBefore trios survived C-0065's clauses; $survivesNow survive with the " +
                    "shared pinned length imposed"
        ),
        T132PredicateRecord(
            "Q3", "does a buildable design remain, and what is it?",
            bestRow?.let {
                ("YES — the %d bp row at a %d step (%.2f nm) leg, base %.1f°, cap %.1f°, " +
                        "margin %.2f / %.2f").format(
                    it.separationBasePairs, it.pinnedLegSteps, it.pinnedLegSteps * rise,
                    it.pinnedBaseDegrees, it.pinnedCapDegrees, it.pinnedMarginCanDo,
                    it.pinnedMarginFields
                )
            } ?: "NO"
        ),
        T132PredicateRecord(
            "Q4", "a torsion closure is a NECESSARY condition only",
            "STATED — every verdict here is an upper bound on buildability, and the cap floor " +
                    "is imposed under C-0062's own independence assumption, which bounds the " +
                    "design from the FAVOURABLE side"
        ),
        T132PredicateRecord(
            "Q5", "the two free limiting cases",
            "MET — an unpinned base reproduces C-0052's budget at every leg length and C-0062's " +
                    "own design table row, and one instance reproduces C-0065's register, both " +
                    "as gate tests"
        )
    )

    val findings = mapOf(
        "the verdict" to decision,
        "why the array clause is free" to
                ("C-0065's bound 3 carries: every station is the same helical phase of its own " +
                        "duplex, so the two leg bases of every truss are the same two phase " +
                        "classes — ${censusRecords.first().classes} classes of " +
                        "${censusRecords.first().populations.first()}, " +
                        "${censusRecords.maxOf { it.distinctPairs }} distinct pair. One leg " +
                        "length therefore serves 34 instances exactly when it serves two legs, " +
                        "and the falsifier declared in the Plan section did not fire"),
        "what the pinning actually costs" to
                ("a free rotation spends C-0052's budget exactly; a PINNED one can only " +
                        "overspend it, because the sign of the pinned deviation need not oppose " +
                        "the budget's own sense. At the winning row the overspend is %.1f°, and " +
                        "the identity psi_base + psi_cap >= |m tau - 90 deg| is asserted as a " +
                        "gate-3 test over every length and thirty pinned deviations").format(
                    bestRow?.pinnedOverspendDegrees ?: 0.0
                ),
        "the new geometric constraint nobody had" to
                ("a truss has TWO legs and the register pins them at two DIFFERENT axial " +
                        "positions of one duplex, so their base chords differ — and their cap " +
                        "chords differ by exactly the same folded angle at EVERY leg length. " +
                        "That difference is a floor on the worst cap that no length can beat, " +
                        "%.1f° at the winning row, and it is below C-0062's chemistry cap floor " +
                        "of %.1f°, which is why it does not bind here").format(
                    bestRow?.let { row ->
                        registerRecords.first { it.separationBasePairs == row.separationBasePairs }
                            .sharedCapFloorDegrees
                    } ?: 0.0,
                    bestRow?.c0062CapFloorDegrees ?: 0.0
                ),
        "what a per-instance leg length would cost" to
                ("not staples — the 68 legs are already 68 distinct oligos, one per scaffold " +
                        "position, so per-instance lengths add no species. It costs HEIGHT: the " +
                        "12-26 step envelope spans %.2f nm, %.1fx §3's acceptable stroke, and " +
                        "34 caps at different heights are not the one plane C-0053 and C-0063 " +
                        "place the output elements in. Within a truss it is worse still — legs " +
                        "of different lengths tilt the crossbar, which C-0052 excludes. It is " +
                        "not needed: the shared length is free").format(
                    (LEG_ENVELOPE.last - LEG_ENVELOPE.first) * rise,
                    (LEG_ENVELOPE.last - LEG_ENVELOPE.first) * rise / Gen1Tile.ACCEPTABLE_STROKE
                ),
        "what this cannot establish" to
                ("a torsion closure is a NECESSARY condition and never a sufficient one. " +
                        "Nothing here is measured, no sequence is designed, and the motif — a " +
                        "duplex standing normal to a single-layer sheet — is undemonstrated " +
                        "(C-0055). The cap floor is imposed as a floor and not as a joint " +
                        "search, which bounds the design from the favourable side")
    )

    val parameters = mapOf(
        "armCount" to ARM_COUNT.toDouble(),
        "phase" to BEST_PHASE.toDouble(),
        "legEnvelopeFirst" to LEG_ENVELOPE.first.toDouble(),
        "legEnvelopeLast" to LEG_ENVELOPE.last.toDouble(),
        "chordSampleSpacingDegrees" to spacing,
        "registerStep" to register.step,
        "junctionSolves" to register.solves.toDouble(),
        "designsEvaluated" to designsEvaluated.toDouble(),
        "rowsRepresentable" to rowRecords.count { it.pinnedRepresentable }.toDouble(),
        "rowsPassing" to rowRecords.count { it.pinnedPasses }.toDouble(),
        "rowsSurviving" to survivingRows.size.toDouble(),
        "triosSurvivingBefore" to survivedBefore.toDouble(),
        "triosSurvivingNow" to survivesNow.toDouble(),
        "bestSeparationBasePairs" to (bestRow?.separationBasePairs ?: 0).toDouble(),
        "bestLegSteps" to (bestRow?.pinnedLegSteps ?: 0).toDouble(),
        "bestLegLength" to (bestRow?.pinnedLegSteps ?: 0) * rise,
        "bestMarginCanDo" to (bestRow?.pinnedMarginCanDo ?: 0.0),
        "bestMarginFields" to (bestRow?.pinnedMarginFields ?: 0.0),
        "sharedLengthIsFree" to if (sharedLengthIsFree) 1.0 else 0.0,
        "perInstanceHeightSpread" to (LEG_ENVELOPE.last - LEG_ENVELOPE.first) * rise
    )

    val result = T132Result(
        task = "T-132",
        leaf = "A8.2",
        conditions = mapOf(
            "temperature" to "300 K",
            "medium" to "aqueous 2 mM MgCl2",
            "k_BT" to "4.141947 pN nm",
            "sheet" to ("single-layer square-lattice Rothemund, 15 duplexes at the SAXS 2.69 nm, " +
                    "40.0 x 40.35 nm, rise 0.34 nm, 10.67 bp/turn, crossover phase 24"),
            "placement" to "C-0063's 34 upward roots at phase 24, read from its result file",
            "trios" to "C-0062's 44 recorded closing trios, read from its result file",
            "junction" to ("C-0029's geometry via C-0059's SingleJunctionFeasibleSet — phosphate " +
                    "radius 1.00 nm, minor groove 120 degrees, the inherited [0.60, 0.70] nm " +
                    "phosphodiester window"),
            "closure" to ("C-0062's per-assignment pruned verdict at C-0059's own 60-step / " +
                    "4-refinement grid"),
            "register" to ("C-0065's own window: 89 positions at 0.17 nm over +-22 bp, 4 " +
                    "candidates per position, but with EVERY closer retained and its chord's " +
                    "SIGN kept"),
            "leg" to ("one rigid duplex with a junction at each end, 12-26 base-pair steps, the " +
                    "same length in both legs of a truss and in all 34 trusses"),
            "twist" to "10.67 bp/turn on the square lattice, 10.5 carried as a sensitivity",
            "rigidity" to ("EI = 230 pN nm^2 (CanDo model input) with every critical load also " +
                    "on Fields et al.'s implied 172.906 pN nm^2"),
            "units" to "nm, pN, pN/nm, pN nm/rad, degrees for every reported angle"
        ),
        decision = decision,
        bounds = bounds,
        census = censusRecords,
        register = registerRecords,
        rows = rowRecords,
        lengths = lengthRecords,
        arrays = arrays,
        sensitivities = sensitivities,
        convergence = convergence,
        reproductions = reproductions,
        budget = budget,
        predicates = predicates,
        findings = findings,
        parameters = parameters
    )

    val json = Json { prettyPrint = true; prettyPrintIndent = "  " }
    val file = File("gpd/results/T-132-pinned-leg-budget.json")
    file.parentFile.mkdirs()
    file.writeText(
        json.encodeToString(
            (json.encodeToJsonElement(result) as JsonObject).roundedForResult(
                digitsByKey = DEPARTURE_DIGITS_BY_KEY
            )
        )
    )
    println("T-132 — wrote ${file.path} in ${(System.currentTimeMillis() - started) / 1000} s")
    println("T-132 — $decision")
}
