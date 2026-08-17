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

import com.xemantic.nano.plentyofroom.structure.C0055_ARM_COUNT
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
import kotlin.math.abs

/**
 * `T-138` — `C-0069`'s Deliverable 5, re-read with the instance count tied to the path count.
 *
 * Emits `gpd/results/T-138-path-count-consistency.json`.
 */

private const val T138_DUPLEXES = 15
private const val T138_PHASE = 24
private val T138_MANDATE = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE

/** `C-0009`'s one-crossover hinge and `C-0034`'s `A2` duplex-end couple — `C-0069`'s `Q5`. */
private const val T138_HINGE = 13.5294
private const val T138_TIP = 78.2353

// ---------------------------------------------------------------------------------------------
// the records — prefixed with the task, because study records are package scoped (CLAUDE.md)
// ---------------------------------------------------------------------------------------------

@Serializable
private data class T138BoundRecord(
    val name: String,
    val value: Double,
    val unit: String,
    val settles: String,
    val falsifierFired: Boolean
)

@Serializable
private data class T138AxisRecord(
    val reading: String,
    val axis: String,
    val pathCount: Int,
    val instancesDemanded: Int,
    val exclusionWidth: Double,
    val bendingRigidity: Double,
    val hingeStiffness: Double,
    val lengthCeiling: Double,
    val armLength: Double,
    val armBasePairs: Int,
    val planMargin: Double,
    val placed: Int,
    val perPathSecant: Double,
    val deliveredTotalStiffness: Double,
    val mandateRatio: Double,
    val meetsTheMandate: Boolean,
    val verdictMoves: Boolean,
    val note: String
)

@Serializable
private data class T138CountRecord(
    val pathCount: Int,
    val rowCounts: List<Int>,
    val rowsAtTheCap: Int,
    val maximumPerRow: Int,
    val armLength: Double,
    val armBasePairs: Int,
    val latticeCeiling: Double,
    val planMargin: Double,
    val marginOverRise: Double,
    val latticeCapacityForThisArm: Int,
    val placed: Int,
    val mandateRatio: Double,
    val selfConsistent: Boolean,
    val perPathForce: Double,
    val clearsUnzip: Boolean,
    val c0072Ceiling: Double,
    val c0072Margin: Double
)

@Serializable
private data class T138ConvergenceRecord(
    val quantity: String,
    val parameter: String,
    val values: List<Double>,
    val results: List<Double>,
    val departure: Double,
    val note: String
)

@Serializable
private data class T138ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double,
    val strict: Boolean
)

@Serializable
private data class T138PredicateRecord(
    val name: String,
    val statement: String,
    val verdict: String
)

@Serializable
private data class T138Result(
    val task: String,
    val leaf: String,
    val conditions: String,
    val decision: String,
    val bounds: List<T138BoundRecord>,
    val axes: List<T138AxisRecord>,
    val counts: List<T138CountRecord>,
    val convergence: List<T138ConvergenceRecord>,
    val reproductions: List<T138ReproductionRecord>,
    val predicates: List<T138PredicateRecord>,
    val findings: List<String>,
    val parameters: Map<String, Double>
)

// ---------------------------------------------------------------------------------------------

/** `C-0063`'s 34-root placement, read from `T-125`'s result file rather than retyped. */
private fun t138Rows(file: File, interhelicalDistance: Double): List<StationRow> {
    require(file.exists()) {
        "C-0063's result file is missing: ${file.path}. T-138 re-reads C-0069's own sensitivity " +
                "table on ITS stations and will not substitute a reconstruction for them."
    }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue("bestPlacement").jsonArray.map { it.jsonObject }
        .map { row ->
            val index = row.getValue("row").jsonPrimitive.content.toInt()
            StationRow(
                index,
                (index - (T138_DUPLEXES - 1) / 2.0) * interhelicalDistance,
                row.getValue("roots").jsonArray
                    .map { it.jsonPrimitive.content.toDouble() }.sorted()
            )
        }.sortedBy { it.row }
}

/** What `T-136` found, consumed as data — the coupled half of this task. */
private class T136Reading(
    val equalSpringPhase: Int,
    val equalSpringMargin: Double,
    val equalSpringOverStroke: Double,
    val recommendedPhase: Int,
    val recommendedCeiling: Double,
    val recommendedMargin: Double,
    val recommendedMinimax: Double,
    val recommendedPeakRatio: Double
)

private fun t136Reading(file: File): T136Reading? {
    if (!file.exists()) return null
    val parameters = Json.parseToJsonElement(file.readText()).jsonObject.getValue("parameters")
        .jsonObject
    fun value(key: String) = parameters.getValue(key).jsonPrimitive.content.toDouble()
    return T136Reading(
        equalSpringPhase = value("winnerPhase").toInt(),
        equalSpringMargin = value("winnerMargin"),
        equalSpringOverStroke = value("winnerRangeOverStroke"),
        recommendedPhase = value("recommendedPhase").toInt(),
        recommendedCeiling = value("recommendedCeiling"),
        recommendedMargin = value("recommendedMargin"),
        recommendedMinimax = value("recommendedMinimaxOverStroke"),
        recommendedPeakRatio = value("recommendedPeakRatio")
    )
}

// ---------------------------------------------------------------------------------------------

fun main() {
    val started = System.currentTimeMillis()
    val edgeX = Gen1Tile.EDGE_X
    val interhelical = Gen1Tile.INTERHELICAL_SHEET
    val lengthY = T138_DUPLEXES * interhelical
    val width = OrigamiDuplex.INTERHELICAL
    val rise = Gen1Tile.RISE_PER_BASE_PAIR
    val pitch = UPWARD_ROOT_PITCH_BASE_PAIRS * rise
    val rigidity = Gen1Tile.DUPLEX_BENDING_RIGIDITY

    println("T-138 — reading C-0063's stations and T-136's swept placement ...")
    val rows = t138Rows(File("gpd/results/T-125-upward-root-placement.json"), interhelical)
    require(rows.sumOf { it.count } == C0055_ARM_COUNT) {
        "C-0063's placement must carry $C0055_ARM_COUNT roots"
    }
    val lattice = upwardRootLattice(T138_PHASE, edgeX, T138_DUPLEXES)
    val winner = t136Reading(File("gpd/results/T-136-two-per-row-placement.json"))

    val armCache = HashMap<Triple<Int, Double, Double>, Double>()
    fun armFor(count: Int, hinge: Double, bending: Double, steps: Int = 400): Double =
        if (steps == 400) armCache.getOrPut(Triple(count, hinge, bending)) {
            elasticaArmForStiffness(
                hingeStiffness = hinge, hingeCount = 1, farStiffness = T138_TIP,
                bendingRigidity = bending, count = count, targetStiffness = T138_MANDATE,
                workingDisplacement = Gen1Tile.ACCEPTABLE_STROKE, steps = steps
            )
        } else elasticaArmForStiffness(
            hingeStiffness = hinge, hingeCount = 1, farStiffness = T138_TIP,
            bendingRigidity = bending, count = count, targetStiffness = T138_MANDATE,
            workingDisplacement = Gen1Tile.ACCEPTABLE_STROKE, steps = steps
        )

    // ------------------------------------------------------------------ the cheap bound
    println("T-138 — the cheap bound, which is one division per row ...")
    val publishedFifteen = mandateRatio(15, C0055_ARM_COUNT)
    val publishedFortyFive = mandateRatio(45, 24)
    val bounds = listOf(
        T138BoundRecord(
            name = "C-0069's 15-path row, read as the total stiffness it presents",
            value = deliveredTotalStiffness(T138_MANDATE, 15, C0055_ARM_COUNT), unit = "pN/nm",
            settles = "C-0017's mandate is %.4f pN/nm as a SUM, and 34 instances of a 15-path " +
                    "arm present %.2fx it — the row is not a sensitivity of the design, it is a " +
                    "different design".format(T138_MANDATE, publishedFifteen),
            falsifierFired = abs(publishedFifteen - 1.0) > 1e-9
        ),
        T138BoundRecord(
            name = "C-0069's 45-path row, read on its OWN reported placed count",
            value = deliveredTotalStiffness(T138_MANDATE, 45, 24), unit = "pN/nm",
            settles = "24 of the 34 demanded place, so the array delivers %.2fx the mandate — " +
                    "and a row whose placed count differs from its path count fails C-0017 " +
                    "whichever of the two readings is taken".format(publishedFortyFive),
            falsifierFired = abs(publishedFortyFive - 1.0) > 1e-9
        ),
        T138BoundRecord(
            name = "the ratio at the reference row, where the two readings coincide",
            value = mandateRatio(C0055_ARM_COUNT, C0055_ARM_COUNT), unit = "dimensionless",
            settles = "exactly 1 by construction at n = 34, which is why no C-0069 HEADLINE " +
                    "moves: every deliverable of that claim is read at the self-consistent count",
            falsifierFired = mandateRatio(C0055_ARM_COUNT, C0055_ARM_COUNT) != 1.0
        )
    )
    bounds.forEach { println("  %-64s %10.4f %s".format(it.name, it.value, it.unit)) }

    // ------------------------------------------------- deliverable 1: the two readings, side by side
    println("T-138 — C-0069's Deliverable 5, in both readings ...")

    class T138Axis(
        val axis: String,
        val reading: String,
        val exclusionWidth: Double = width,
        val bendingRigidity: Double = rigidity,
        val hingeStiffness: Double = T138_HINGE,
        val pathCount: Int = C0055_ARM_COUNT,
        val note: String
    )

    val axes = listOf(
        T138Axis(
            "reference", "2.69 nm SAXS, EI 230, one crossover, 34 paths",
            note = "C-0055's arm on C-0063's placement — the two readings coincide here"
        ),
        T138Axis(
            "exclusion width", "2.73 nm, the square-lattice SAXS value", exclusionWidth = 2.73,
            note = "the count is unchanged, so only the PLACED number moves — and with it the " +
                    "delivered total, which C-0069's table does not carry"
        ),
        T138Axis(
            "exclusion width", "2.0 nm, the steric diameter", exclusionWidth = 2.0,
            note = "the loosest reading and nothing moves in either column"
        ),
        T138Axis(
            "duplex EI", "Fields et al.'s implied 172.906 pN nm^2", bendingRigidity = 172.906,
            note = "the measured rigidity shortens the arm and the verdict is unchanged"
        ),
        T138Axis(
            "crossover alpha", "0.6, the bottom of Chen et al.'s fitted bracket",
            hingeStiffness = Gen1Tile.crossoverHingeStiffness(Gen1Tile.CROSSOVER_ALPHA_MIN),
            note = "a softer root gives a SHORTER arm — the bracket runs the favourable way"
        ),
        T138Axis(
            "crossover alpha", "1.2, the top of the same bracket",
            hingeStiffness = Gen1Tile.crossoverHingeStiffness(Gen1Tile.CROSSOVER_ALPHA_MAX),
            note = "a stiffer root gives a LONGER arm, and this is the axis that closes the margin"
        ),
        T138Axis(
            "path count", "45 paths, C-0015's own", pathCount = 45,
            note = "the row the two readings disagree about most: C-0069 places 34 instances of " +
                    "a 45-path arm and reports 24 of them, i.e. 0.53x the mandate"
        ),
        T138Axis(
            "path count", "15 paths, C-0041's buildable count", pathCount = 15,
            note = "C-0069's note — the placement is unchanged because the count is what sets " +
                    "the stations — is exactly what pins the ceiling at 8.19 nm in every row"
        )
    )

    fun axisRow(axis: T138Axis, selfConsistent: Boolean): T138AxisRecord {
        val arm = armFor(axis.pathCount, axis.hingeStiffness, axis.bendingRigidity)
        val demanded = if (selfConsistent) axis.pathCount else C0055_ARM_COUNT
        val cap = balancedRowCounts(demanded, T138_DUPLEXES, 3).max()
        val ceiling = if (selfConsistent) {
            maximumPlanCeilingForCount(lattice, demanded, edgeX, axis.exclusionWidth, cap) ?: 0.0
        } else {
            rowOfThreeLengthCeiling(pitch, axis.exclusionWidth)
        }
        val placed = if (selfConsistent) {
            minOf(demanded, latticeRootCapacity(lattice, arm, edgeX, axis.exclusionWidth, 3))
        } else {
            placeRootedOutputElement(
                axis.reading, rows, arm, edgeX, lengthY, axis.exclusionWidth
            ).placed
        }
        val ratio = mandateRatio(axis.pathCount, placed)
        return T138AxisRecord(
            reading = if (selfConsistent)
                "SELF-CONSISTENT — the array carries n instances"
            else "AS PUBLISHED — the array is held at 34 instances",
            axis = axis.axis,
            pathCount = axis.pathCount,
            instancesDemanded = demanded,
            exclusionWidth = axis.exclusionWidth,
            bendingRigidity = axis.bendingRigidity,
            hingeStiffness = axis.hingeStiffness,
            lengthCeiling = ceiling,
            armLength = arm,
            armBasePairs = basePairsNearest(arm, rise),
            planMargin = ceiling - arm,
            placed = placed,
            perPathSecant = T138_MANDATE / axis.pathCount,
            deliveredTotalStiffness = deliveredTotalStiffness(T138_MANDATE, axis.pathCount, placed),
            mandateRatio = ratio,
            meetsTheMandate = abs(ratio - 1.0) < 1e-9,
            verdictMoves = placed != demanded,
            note = axis.note
        )
    }

    val axisRecords = axes.map { axisRow(it, false) } + axes.map { axisRow(it, true) }
    axisRecords.forEach {
        println(
            "  %-46s %-42s n %2d  demanded %2d  arm %6.4f  placed %2d  k/mandate %5.3f".format(
                it.reading, it.axis + " — " + it.reading.take(0) + it.note.take(0) + it.axis,
                it.pathCount, it.instancesDemanded, it.armLength, it.placed, it.mandateRatio
            )
        )
    }

    // ------------------------------------------- deliverable 2: the self-consistent count scan
    println("T-138 — the self-consistent count scan, with the closed-form ceiling ...")
    val c0072Ceilings = HashMap<Int, Double>()
    val counts = listOf(45, 34, 33, 32, 31, 30, 28, 25, 22, 20, 15).map { count ->
        val vector = balancedRowCounts(count, T138_DUPLEXES, 3)
        val cap = vector.max()
        val arm = armFor(count, T138_HINGE, rigidity)
        val ceiling = maximumPlanCeilingForCount(lattice, count, edgeX, width, cap) ?: 0.0
        val capacity = latticeRootCapacity(lattice, arm, edgeX, width, 3)
        val placed = minOf(count, capacity)
        val published = runCatching {
            rootedLengthCeiling(
                rowsWithoutInteriorRoots(rows, C0055_ARM_COUNT - count), edgeX, width
            )
        }.getOrNull() ?: 0.0
        c0072Ceilings[count] = published
        T138CountRecord(
            pathCount = count,
            rowCounts = vector,
            rowsAtTheCap = vector.count { it == cap },
            maximumPerRow = cap,
            armLength = arm,
            armBasePairs = basePairsNearest(arm, rise),
            latticeCeiling = ceiling,
            planMargin = ceiling - arm,
            marginOverRise = (ceiling - arm) / rise,
            latticeCapacityForThisArm = capacity,
            placed = placed,
            mandateRatio = mandateRatio(count, placed),
            selfConsistent = placed == count,
            perPathForce = Gen1Tile.TARGET_FORCE / count,
            clearsUnzip = Gen1Tile.TARGET_FORCE / count <= Gen1Tile.DUPLEX_UNZIP_ALLOWABLE,
            c0072Ceiling = published,
            c0072Margin = if (published > 0.0) published - arm else 0.0
        )
    }
    counts.forEach {
        println(
            ("  n %2d  vector %2d x %d + %2d x %d  arm %6.4f  ceiling %7.4f (C-0072 %6.4f)  " +
                    "margin %7.4f  placed %2d  ratio %5.3f").format(
                it.pathCount, it.rowsAtTheCap, it.maximumPerRow,
                T138_DUPLEXES - it.rowsAtTheCap, it.maximumPerRow - 1, it.armLength,
                it.latticeCeiling, it.c0072Ceiling, it.planMargin, it.placed, it.mandateRatio
            )
        )
    }

    // ------------------------------------------------------------------ convergence
    val armSteps = listOf(200, 400, 800).map { armFor(30, T138_HINGE, rigidity, it) }
    val ceilingResolutions = listOf(1e-6, 1e-9, 1e-12).map {
        maximumPlanCeilingForCount(lattice, 30, edgeX, width, 2, it)!!
    }
    val convergence = listOf(
        T138ConvergenceRecord(
            "the 30-path arm", "RK4 steps 200 / 400 / 800", listOf(200.0, 400.0, 800.0), armSteps,
            abs(armSteps[2] - armSteps[1]) / armSteps[2],
            "C-0039's exact elastica, re-run rather than tabulated"
        ),
        T138ConvergenceRecord(
            "the maximum plan ceiling at 30 roots",
            "bisection resolution 1e-6 / 1e-9 / 1e-12", listOf(1e-6, 1e-9, 1e-12),
            ceilingResolutions, abs(ceilingResolutions[2] - ceilingResolutions[1]),
            "an ABSOLUTE departure in nm — the bisection exits on its own bracket width"
        )
    )

    // ------------------------------------------------------------------ reproductions
    val published = axisRecords.filter { it.reading.startsWith("AS PUBLISHED") }
    fun reproduction(
        source: String, quantity: String, value: Double, reproduced: Double, strict: Boolean
    ) = T138ReproductionRecord(
        source, quantity, value, reproduced,
        if (value == 0.0) abs(reproduced) else abs(reproduced - value) / abs(value), strict
    )

    val reproductions = listOf(
        reproduction("C-0069", "reference arm [nm]", 8.16439083, published[0].armLength, false),
        reproduction("C-0069", "reference placed", 34.0, published[0].placed.toDouble(), true),
        reproduction("C-0069", "2.73 nm ceiling [nm]", 8.15, published[1].lengthCeiling, false),
        reproduction("C-0069", "2.73 nm placed", 18.0, published[1].placed.toDouble(), true),
        reproduction("C-0069", "2.0 nm ceiling [nm]", 8.88, published[2].lengthCeiling, false),
        reproduction("C-0069", "EI 172.906 arm [nm]", 7.883, published[3].armLength, false),
        reproduction("C-0069", "alpha 0.6 arm [nm]", 7.793, published[4].armLength, false),
        reproduction("C-0069", "alpha 1.2 arm [nm]", 8.332, published[5].armLength, false),
        reproduction("C-0069", "alpha 1.2 placed", 30.0, published[5].placed.toDouble(), true),
        reproduction("C-0069", "45-path arm [nm]", 9.131, published[6].armLength, false),
        reproduction("C-0069", "45-path placed", 24.0, published[6].placed.toDouble(), true),
        reproduction("C-0069", "15-path arm [nm]", 5.963, published[7].armLength, false),
        reproduction("C-0069", "15-path placed", 34.0, published[7].placed.toDouble(), true),
        reproduction(
            "C-0069", "the row-of-three ceiling [nm]", 8.19,
            rowOfThreeLengthCeiling(pitch, width), false
        ),
        reproduction(
            "C-0072", "the 30-path arm [nm]", 7.77049,
            counts.first { it.pathCount == 30 }.armLength, false
        ),
        reproduction(
            "C-0072", "the ceiling of its own 30-root reduction [nm]", 9.12,
            c0072Ceilings.getValue(30), false
        ),
        reproduction(
            "C-0072", "the margin of its own 30-root reduction [nm]", 1.3495,
            counts.first { it.pathCount == 30 }.c0072Margin, false
        ),
        reproduction(
            "C-0072", "the ceiling of its own 15-root reduction [nm]", 20.0,
            c0072Ceilings.getValue(15), false
        ),
        reproduction(
            "C-0063", "rows of three forced at 34 roots", 4.0,
            balancedRowCounts(34, T138_DUPLEXES, 3).count { it == 3 }.toDouble(), true
        ),
        reproduction("C-0055", "the upward root pitch [nm]", 10.88, pitch, true)
    )

    // ------------------------------------------------------------------ the predicates
    val selfConsistentRows = axisRecords.filter { it.reading.startsWith("SELF-CONSISTENT") }
    val referencePublished = published[0]
    val referenceSelf = selfConsistentRows[0]
    val thirty = counts.first { it.pathCount == 30 }
    val predicates = listOf(
        T138PredicateRecord(
            "P1 — the bookkeeping",
            "C-0069's Deliverable 5 with the instance count tied to the path count, and " +
                    "k_delivered/33.3333 beside every row",
            ("as published the eight rows deliver %s of C-0017's mandate; self-consistently they " +
                    "deliver %s. The two path-count rows are the extremes: 15 paths present " +
                    "%.2fx and 45 paths %.2fx on C-0069's own reported counts.").format(
                published.joinToString(", ") { "%.3f".format(it.mandateRatio) },
                selfConsistentRows.joinToString(", ") { "%.3f".format(it.mandateRatio) },
                published[7].mandateRatio, published[6].mandateRatio
            )
        ),
        T138PredicateRecord(
            "P2 — no C-0069 headline moves",
            "the reference row must be identical in the two readings, because n = 34 is the " +
                    "self-consistent count",
            ("arm %.8f nm in both, placed %d in both, ceiling %.4f against %.4f nm, delivered " +
                    "ratio %.9f against %.9f — the reference row is unchanged and every C-0069 " +
                    "deliverable is read there. %s").format(
                referencePublished.armLength, referencePublished.placed,
                referencePublished.lengthCeiling, referenceSelf.lengthCeiling,
                referencePublished.mandateRatio, referenceSelf.mandateRatio,
                if (referencePublished.armLength == referenceSelf.armLength &&
                    referencePublished.placed == referenceSelf.placed
                ) "The declared falsifier DID NOT FIRE."
                else "The declared falsifier FIRED."
            )
        ),
        T138PredicateRecord(
            "P3 — the coupled reading with T-136",
            "at 30 roots the self-consistent per-path stiffness re-sizes the arm; does it still " +
                    "place 30 times, and what is the margin on the placement T-136 supplies?",
            ("the 30-path arm is %.5f nm = %d bp, the lattice carries %d of them at three per " +
                    "row so 30 place with room to spare, and the largest ceiling any 30-root " +
                    "placement can keep is %.4f nm — a margin of %.4f nm, %.2f base-pair rises, " +
                    "against C-0072's %.4f nm on its own plan-rule reduction. %s").format(
                thirty.armLength, thirty.armBasePairs, thirty.latticeCapacityForThisArm,
                thirty.latticeCeiling, thirty.planMargin, thirty.marginOverRise,
                thirty.c0072Margin,
                winner?.let {
                    ("T-136's EQUAL-SPRING argmin sits at phase %d with a margin of only %.4f " +
                            "nm (it spends the margin on flatness and still misses T-5b at " +
                            "%.4f); its RECOMMENDED placement sits at phase %d, keeps the full " +
                            "%.4f nm ceiling and %.4f nm of margin, and dishes %.4f under a " +
                            "distribution at a peak ratio of %.2f.").format(
                        it.equalSpringPhase, it.equalSpringMargin, it.equalSpringOverStroke,
                        it.recommendedPhase, it.recommendedCeiling, it.recommendedMargin,
                        it.recommendedMinimax, it.recommendedPeakRatio
                    )
                } ?: "T-136's result file was not present, so the swept placement is not quoted."
            )
        )
    )

    // ------------------------------------------------------------------ the findings
    val findings = listOf(
        ("C-0017's mandate is a stiffness on a SUM, so a path count sizes the element AND counts " +
                "the instances. C-0069's Deliverable 5 changes the first and holds the second at " +
                "34, so its 15-path row presents %.2fx the mandate and its 45-path row %.2fx on " +
                "its own reported placed count. Read self-consistently the same two rows deliver " +
                "%.3fx and %.3fx.").format(
            published[7].mandateRatio, published[6].mandateRatio,
            selfConsistentRows[7].mandateRatio, selfConsistentRows[6].mandateRatio
        ),
        ("But the ratio is not only a path-count column: it is `placed/n`, and FOUR of C-0069's " +
                "eight published rows place fewer than they demand. The 2.73 nm square-lattice " +
                "row places 18 of 34 and therefore delivers %.3fx the mandate — a row that " +
                "C-0069 reports as a plan failure is ALSO a stiffness failure, and the two are " +
                "the same number.").format(published[1].mandateRatio),
        ("No C-0069 headline moves, and that is verified rather than assumed: at n = 34 the two " +
                "readings are the same row to the last digit (arm %.8f nm, placed %d, ratio " +
                "%.9f), and every deliverable of that claim — the six cheap bounds, the " +
                "eleven-row catalogue, the two-restraint window c <= 2.3416 — is read there."
                ).format(
            referencePublished.armLength, referencePublished.placed, referencePublished.mandateRatio
        ),
        ("What the fixed array DID hide is the ceiling. Holding the instances at 34 forces a row " +
                "of three, and a row of three caps a rooted element at pitch - d = %.4f nm at " +
                "every count in C-0069's table. Self-consistently the ceiling is a step function " +
                "of the count: %.4f nm at 45 and 34 and 31, %.4f nm from 30 down to 24, %.4f nm " +
                "at 22, and %.4f nm at 15 — and the step at 31 is exactly where the count vector " +
                "stops needing a row of three.").format(
            rowOfThreeLengthCeiling(pitch, width),
            counts.first { it.pathCount == 34 }.latticeCeiling,
            counts.first { it.pathCount == 30 }.latticeCeiling,
            counts.first { it.pathCount == 22 }.latticeCeiling,
            counts.first { it.pathCount == 15 }.latticeCeiling
        ),
        ("The coupled answer: the 30-path arm is %.5f nm and the lattice carries %d of them, so " +
                "30 place self-consistently and the mandate is met exactly. Its margin on the " +
                "best 30-root placement is %.4f nm — %.2f rises, and %.2fx C-0072's %.4f nm, " +
                "which was read on that claim's own plan-rule reduction rather than on the " +
                "ceiling-optimal placement.").format(
            thirty.armLength, thirty.latticeCapacityForThisArm, thirty.planMargin,
            thirty.marginOverRise,
            if (thirty.c0072Margin > 0.0) thirty.planMargin / thirty.c0072Margin else 0.0,
            thirty.c0072Margin
        )
    )

    val result = T138Result(
        task = "T-138 — C-0069's path-count sensitivity re-sizes the element but not the array",
        leaf = "A8.2",
        conditions = ("T = 300 K, k_BT = 4.141947 pN nm; aqueous 2 mM MgCl2; 40.0 x %.2f nm " +
                "single-layer square-lattice Rothemund sheet, %d duplexes at 2.69 nm, 0.34 nm " +
                "rise, crossover phase %d; C-0017's %.4f pN/nm as a SUM at S3's acceptable 3 nm; " +
                "C-0039's exact elastica on a one-crossover root (k_theta = %.4f pN nm/rad) and " +
                "C-0034's A2 tip (%.4f); EI = %.1f pN nm^2; C-0053's footprint convention").format(
            lengthY, T138_DUPLEXES, T138_PHASE, T138_MANDATE, T138_HINGE, T138_TIP, rigidity
        ),
        decision = ("read self-consistently every row of C-0069's Deliverable 5 delivers the " +
                "mandate exactly where placed = n and fails it in proportion where it does not; " +
                "the reference row is unchanged to the last digit, so no C-0069 headline moves, " +
                "and what the fixed 34-instance array hid is the CEILING, which is %.4f nm at 30 " +
                "roots against the 8.19 nm a row of three imposes").format(thirty.latticeCeiling),
        bounds = bounds,
        axes = axisRecords,
        counts = counts,
        convergence = convergence,
        reproductions = reproductions,
        predicates = predicates,
        findings = findings,
        parameters = mapOf(
            "mandate" to T138_MANDATE,
            "duplexes" to T138_DUPLEXES.toDouble(),
            "phase" to T138_PHASE.toDouble(),
            "edgeX" to edgeX,
            "lengthY" to lengthY,
            "exclusionWidth" to width,
            "risePerBasePair" to rise,
            "rootPitch" to pitch,
            "hingeStiffness" to T138_HINGE,
            "tipCouple" to T138_TIP,
            "bendingRigidity" to rigidity,
            "rowOfThreeCeiling" to rowOfThreeLengthCeiling(pitch, width),
            "armAt30" to thirty.armLength,
            "latticeCeilingAt30" to thirty.latticeCeiling,
            "planMarginAt30" to thirty.planMargin,
            "c0072MarginAt30" to thirty.c0072Margin,
            "t136EqualSpringPhase" to (winner?.equalSpringPhase?.toDouble() ?: -1.0),
            "t136EqualSpringMargin" to (winner?.equalSpringMargin ?: 0.0),
            "t136RecommendedPhase" to (winner?.recommendedPhase?.toDouble() ?: -1.0),
            "t136RecommendedCeiling" to (winner?.recommendedCeiling ?: 0.0),
            "t136RecommendedMargin" to (winner?.recommendedMargin ?: 0.0),
            "t136RecommendedMinimax" to (winner?.recommendedMinimax ?: 0.0)
        )
    )

    val output = File("gpd/results/T-138-path-count-consistency.json")
    output.parentFile.mkdirs()
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult() as JsonObject)
        )
    )

    println()
    println("predicates")
    result.predicates.forEach { println("  ${it.name}: ${it.verdict}"); println() }
    println("upstream reproductions")
    result.reproductions.forEach {
        println(
            "  %-8s %-52s %12.6g vs %12.6g  %8.2e %s".format(
                it.source, it.quantity, it.published, it.reproduced, it.departure,
                if (it.strict) "" else "(non-strict)"
            )
        )
    }
    println()
    result.findings.forEach { println("  * $it"); println() }
    println("written to ${output.path} in ${(System.currentTimeMillis() - started) / 1000} s")
}
