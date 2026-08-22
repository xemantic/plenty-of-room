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

package com.xemantic.nano.plentyofroom.synthesis

import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs

/**
 * `T-169` — a study's emitted prose cited the ceiling `C-0049` withdrew, and the verdict beside it
 * is shown unaffected.
 *
 * ```shell
 * tools/study.sh synthesis.WithdrawnCeilingNoteStudyKt
 * ```
 *
 * Emits `gpd/results/T-169-withdrawn-ceiling-note.json`. Reads
 * `gpd/results/T-79-two-spring-elastica.json`, which is the file the defect was in — `C-0101` §4
 * and the `T-169` row both name `synthesis/DesiredStrokeReachStudy.kt`, and the string is in
 * `anchoring/TwoSpringElasticaStudy.kt`. `T-108` never carried it: its `T-107` verdict is where
 * `C-0049` was *derived*.
 */

private const val TARGET_FORCE = 100.0
private const val ACCEPTABLE_STROKE = 3.0
private const val DESIRED_STROKE = 10.0
private const val PATH_COUNT = 45
private const val UNZIP_ALLOWABLE = 10.0
private const val WITHDRAWN = "40 pN/nm ceiling at the desired stroke"

@Serializable
private data class T169RowRecord(
    val anchorageId: String,
    val hingeCount: Int,
    val reachesDesiredStroke: Boolean,
    val tangentAtDesired: Double,
    val secantAtDesired: Double,
    val pastWithdrawnReading: Boolean,
    val pastClauseCorrectReading: Boolean,
    val pastPerPathSecantCeiling: Boolean,
    val readingsAgree: Boolean,
    val quotesWithdrawnCeiling: Boolean,
    val verdict: String
)

@Serializable
private data class T169CeilingRecord(
    val clause: String,
    val stroke: Double,
    val mandate: Double,
    val declaredCeiling: Double,
    val perPathSecantCeilingAt45: Double,
    val perPathSecantCeilingAt15: Double,
    val note: String
)

@Serializable
private data class T169PredicateRecord(
    val name: String,
    val statement: String,
    val verdict: String,
    val met: Boolean
)

@Serializable
private data class T169FalsifierRecord(
    val name: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T169Result(
    val task: String,
    val leaf: String,
    val question: String,
    val conditions: String,
    val ceilings: List<T169CeilingRecord>,
    val rows: List<T169RowRecord>,
    val predicates: List<T169PredicateRecord>,
    val falsifiers: List<T169FalsifierRecord>,
    val findings: List<String>,
    val parameters: Map<String, Double>
)

fun main() {
    val file = ResultInputs.T_79.file()
    require(file.exists()) { "T-79's result file is missing: ${file.path}" }
    val emitted = Json.parseToJsonElement(file.readText()).jsonObject
    val placements = emitted.getValue("placements").jsonArray.map { it.jsonObject }

    val reading = clauseCeilingReading(
        targetForce = TARGET_FORCE,
        placementStroke = ACCEPTABLE_STROKE,
        stroke = DESIRED_STROKE,
        pathCount = PATH_COUNT,
        unzipAllowable = UNZIP_ALLOWABLE
    )

    val ceilings = listOf(ACCEPTABLE_STROKE, DESIRED_STROKE).map { stroke ->
        T169CeilingRecord(
            clause = if (stroke == ACCEPTABLE_STROKE) "acceptable — 100 pN at 3 nm"
            else "desired — 100 pN at ~10 nm",
            stroke = stroke,
            mandate = TARGET_FORCE / stroke,
            declaredCeiling = declaredComplianceCeiling(TARGET_FORCE, stroke),
            perPathSecantCeilingAt45 = perPathSecantCeiling(UNZIP_ALLOWABLE, 45, stroke),
            perPathSecantCeilingAt15 = perPathSecantCeiling(UNZIP_ALLOWABLE, 15, stroke),
            note = if (stroke == ACCEPTABLE_STROKE)
                "the stroke C-0023 wrote 40 pN/nm at, and per C-0049 the only one it is owed at"
            else "the same construction, 1.2 x (100 pN / 10 nm) = 12 pN/nm, 3.33x STRICTER"
        )
    }

    val rows = placements.map { row ->
        fun double(key: String) = row.getValue(key).jsonPrimitive.content.toDouble()
        val verdict = row.getValue("verdict").jsonPrimitive.content
        val reaches = row.getValue("reachesDesiredStroke").jsonPrimitive.content.toBoolean()
        val tangent = double("tangentAtDesired")
        val secant = double("secantAtDesired")
        val pastWithdrawn = reaches && tangent > reading.declaredCeilingAtPlacementClause
        val pastClause = reaches && tangent > reading.declaredCeilingAtThisClause
        T169RowRecord(
            anchorageId = row.getValue("anchorageId").jsonPrimitive.content,
            hingeCount = row.getValue("hingeCount").jsonPrimitive.content.toInt(),
            reachesDesiredStroke = reaches,
            tangentAtDesired = tangent,
            secantAtDesired = secant,
            pastWithdrawnReading = pastWithdrawn,
            pastClauseCorrectReading = pastClause,
            pastPerPathSecantCeiling = reaches && secant > reading.perPathSecantCeiling,
            readingsAgree = pastWithdrawn == pastClause,
            quotesWithdrawnCeiling = verdict.contains(WITHDRAWN),
            verdict = verdict
        )
    }

    val placing = rows.filter { it.reachesDesiredStroke }
    val agreeCount = placing.count { it.readingsAgree }
    val quoting = rows.count { it.quotesWithdrawnCeiling }
    val perPathCount = placing.count { it.pastPerPathSecantCeiling }

    val predicates = listOf(
        T169PredicateRecord(
            "P1", "no emitted verdict of T-79 quotes the withdrawn ceiling at the desired stroke",
            "$quoting of ${rows.size} rows quote it", quoting == 0
        ),
        T169PredicateRecord(
            "P2", "every row's verdict is the same under the withdrawn and the clause-correct " +
                    "reading of C-0023's tolerance",
            "$agreeCount of ${placing.size} placing rows agree", agreeCount == placing.size
        ),
        T169PredicateRecord(
            "P3", "the miss does not rest on a DECLARED tolerance at all: every placing row is " +
                    "also past C-0006's per-path secant ceiling, which is CITED",
            "$perPathCount of ${placing.size} placing rows are past 45 pN/nm on the secant",
            perPathCount == placing.size
        )
    )

    val falsifiers = listOf(
        T169FalsifierRecord(
            "F1", "re-wording the note moves a verdict",
            agreeCount != placing.size,
            ("the two readings differ by exactly 10/3 (%.1f against %.1f pN/nm) and the " +
                    "softest placing row's tangent is %.1f pN/nm, %.1fx the STRICTER of them")
                .format(
                    reading.declaredCeilingAtPlacementClause,
                    reading.declaredCeilingAtThisClause,
                    placing.minOf { it.tangentAtDesired },
                    placing.minOf { it.tangentAtDesired } / reading.declaredCeilingAtThisClause
                )
        ),
        T169FalsifierRecord(
            "F2", "the withdrawn ceiling survives anywhere in T-79's emitted prose",
            emitted.toString().contains(WITHDRAWN),
            "the phrase \"$WITHDRAWN\" occurs $quoting times in the re-emitted file"
        ),
        T169FalsifierRecord(
            "F3", "the clause-correct reading is LOOSER than the withdrawn one, so re-wording " +
                    "would be a relaxation",
            reading.declaredCeilingAtThisClause > reading.declaredCeilingAtPlacementClause,
            ("12 pN/nm against 40: the declared ceiling FALLS with the stroke because the " +
                    "mandate does, so the correction is %.2fx stricter, not looser")
                .format(
                    reading.declaredCeilingAtPlacementClause /
                            reading.declaredCeilingAtThisClause
                )
        )
    )

    val parameters = mapOf(
        "targetForce" to TARGET_FORCE,
        "acceptableStroke" to ACCEPTABLE_STROKE,
        "desiredStroke" to DESIRED_STROKE,
        "pathCount" to PATH_COUNT.toDouble(),
        "unzipAllowable" to UNZIP_ALLOWABLE,
        "declaredCeilingFactor" to DECLARED_CEILING_FACTOR,
        "declaredCeilingAtPlacementClause" to reading.declaredCeilingAtPlacementClause,
        "declaredCeilingAtDesiredClause" to reading.declaredCeilingAtThisClause,
        "perPathSecantCeilingAtDesiredClause" to reading.perPathSecantCeiling,
        "ratioOfTheTwoDeclaredReadings" to
                reading.declaredCeilingAtPlacementClause / reading.declaredCeilingAtThisClause,
        "rowsEmitted" to rows.size.toDouble(),
        "rowsThatPlace" to placing.size.toDouble(),
        "rowsQuotingTheWithdrawnCeiling" to quoting.toDouble(),
        "rowsWhereTheTwoReadingsAgree" to agreeCount.toDouble(),
        "rowsPastThePerPathSecantCeiling" to perPathCount.toDouble(),
        "softestPlacingRowTangentAtDesired" to placing.minOf { it.tangentAtDesired },
        "softestPlacingRowSecantAtDesired" to placing.minOf { it.secantAtDesired }
    )

    val findings = listOf(
        ("THE NOTE WAS WRONG AND THE VERDICT WAS NOT. C-0023's 40 pN/nm is " +
                "1.2 x (100 pN / 3 nm) and carries the ACCEPTABLE clause's stroke inside it; " +
                "the same construction at §3's desired clause is %.1f pN/nm, %.2fx STRICTER. " +
                "Every one of the %d rows of T-79's catalogue that places is past both, so the " +
                "re-wording moves %d verdicts.")
            .format(
                reading.declaredCeilingAtThisClause,
                reading.declaredCeilingAtPlacementClause / reading.declaredCeilingAtThisClause,
                placing.size, placing.size - agreeCount
            ),
        ("THE MISS DOES NOT REST ON A DECLARED NUMBER AT ALL. %d of %d placing rows are also " +
                "past C-0006's per-path secant ceiling n x allowable / s = %.1f pN/nm, which is " +
                "CITED rather than declared and, unlike the tolerance, TIGHTENS as the stroke " +
                "grows. The softest placing row is %.1f pN/nm on the secant, %.2fx past it.")
            .format(
                perPathCount, placing.size, reading.perPathSecantCeiling,
                placing.minOf { it.secantAtDesired },
                placing.minOf { it.secantAtDesired } / reading.perPathSecantCeiling
            ),
        "THE DEFECT WAS IN A FILE NEITHER RECORD NAMES. C-0101 §4 and the T-169 row both point " +
                "at synthesis/DesiredStrokeReachStudy.kt and T-108; the string was in " +
                "anchoring/TwoSpringElasticaStudy.kt and T-79. T-108 is where C-0049 was " +
                "DERIVED and is the one place in the tree that already read the ceiling with " +
                "its stroke — so the correction went to the study that inherited the number, " +
                "not to the one that fixed it."
    )

    val result = T169Result(
        task = "T-169",
        leaf = "A8.2",
        question = "A study's emitted prose cites the 40 pN/nm ceiling C-0049 withdrew — " +
                "is the verdict beside it affected?",
        conditions = "T = 300 K, k_BT = 4.141947 pN nm; §3's 100 pN at the acceptable 3 nm and " +
                "the desired 10 nm; 45 load paths (C-0015); C-0006/CH-0029's 10 pN unzip " +
                "allowable; the numbers read from the re-emitted gpd/results/" +
                "T-79-two-spring-elastica.json rather than recomputed",
        ceilings = ceilings,
        rows = rows,
        predicates = predicates,
        falsifiers = falsifiers,
        findings = findings,
        parameters = parameters
    )

    val json = Json { prettyPrint = true }
    val out = File("gpd/results/T-169-withdrawn-ceiling-note.json")
    out.parentFile.mkdirs()
    out.writeText(json.encodeToString(json.encodeToJsonElement(result).roundedForResult().withEmissionHeader(LatticeTag.SQUARE, null)))

    println("T-169 — the withdrawn ceiling in an emitted note")
    ceilings.forEach {
        println(
            "  %-28s mandate %7.4f  declared %7.4f  per-path 45/15 %7.2f / %7.2f".format(
                it.clause, it.mandate, it.declaredCeiling,
                it.perPathSecantCeilingAt45, it.perPathSecantCeilingAt15
            )
        )
    }
    println("  rows %d, placing %d, quoting the withdrawn ceiling %d, readings agree %d".format(
        rows.size, placing.size, quoting, agreeCount
    ))
    falsifiers.forEach { println("  %s fired %-5s %s".format(it.name, it.fired, it.outcome)) }
    println("written to ${out.path}")
    check(abs(reading.declaredCeilingAtPlacementClause - 40.0) < 1e-12) {
        "C-0023's declared ceiling must reproduce at the placement clause"
    }
}
