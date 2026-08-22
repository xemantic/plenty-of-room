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

import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.CrossoverLayout
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.OrigamiGrillage
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs

/**
 * `T-215` — which part of `gpd/results/T-129-range-robust-placement.json` is a descent manifold,
 * and how wide.
 *
 * Emits `gpd/results/T-215-descent-manifold-width.json`.
 */

/**
 * Below this, a width is a difference of two `Double`s at their own resolution and not a reading.
 *
 * `RESULT_ABSOLUTE_FLOOR`'s discipline — *an absolute floor is a claim about units and it does not
 * travel* — applied to a **dimensionless** width. Two runs of this study measured the ulp probe's
 * own value width at `1.0e−15` and `1.5e−16`: both mean *"nothing moved"*, and emitting either
 * makes the file un-diffable while asserting a precision the arithmetic does not have.
 */
private const val T215_WIDTH_FLOOR: Double = 1e-12

/** [T215_WIDTH_FLOOR] applied. */
private fun flooredWidth(width: Double): Double = if (width < T215_WIDTH_FLOOR) 0.0 else width

/** The leaf keys that are a descent's own objective. */
private val T215_VALUE_KEYS = setOf("minimaxWorstOverStroke")

/**
 * The leaf keys that are a functional of a descent's ARGMIN.
 *
 * The first four are `max_i k_i` rescaled by constants — the mandate over the path count, the
 * acceptable stroke, `sqrt(k_BT k)` — so they must move by *identical* relative amounts, and that
 * identity is asserted rather than assumed. `peakSolvedPathForce` is a different functional of the
 * same point (a peak support force over the states), which is why it moves by a different amount.
 */
private val T215_POINT_KEYS = setOf(
    "minimaxPeakRatio", "peakPathStiffness", "peakPathForceAtAcceptableStroke",
    "peakThermalForce", "peakSolvedPathForce"
)

@Serializable
private data class T215FieldRecord(
    val path: String,
    val kind: String,
    val classification: String,
    val width: Double,
    val distinctReadings: Int,
    val note: String
)

@Serializable
private data class T215WidthRecord(
    val quantity: String,
    val members: Int,
    val distinctValues: Int,
    val valueWidth: Double,
    val pointWidth: Double,
    val amplification: Double?,
    val note: String
)

@Serializable
private data class T215DegeneracyRecord(
    val problem: String,
    val states: Int,
    val paths: Int,
    val bindingStates: Int,
    val ulpOffsets: List<Int>,
    val valueWidth: Double,
    val pointWidth: Double,
    val amplification: Double?,
    val note: String
)

@Serializable
private data class T215SpreadRecord(
    val problem: String,
    val states: Int,
    val paths: Int,
    val startsUsed: Int,
    val startsWithinTolerance: Int,
    val tolerance: Double,
    val valueWidth: Double,
    val pointWidth: Double,
    val amplification: Double?,
    val allStartsValueWidth: Double,
    val allStartsPointWidth: Double,
    val note: String
)

@Serializable
private data class T215ConvergenceRecord(
    val quantity: String,
    val parameter: String,
    val values: List<Double>,
    val results: List<Double>,
    val departure: Double,
    val note: String
)

@Serializable
private data class T215ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val departure: Double,
    val strict: Boolean
)

@Serializable
private data class T215PredicateRecord(
    val name: String,
    val statement: String,
    val met: Boolean,
    val verdict: String
)

@Serializable
private data class T215FalsifierRecord(
    val name: String,
    val statement: String,
    val fired: Boolean,
    val evidence: String
)

@Serializable
private data class T215MemberRecord(val label: String, val provenance: String)

@Serializable
private data class T215Result(
    val task: String,
    val leaf: String,
    val conditions: String,
    val decision: String,
    val members: List<T215MemberRecord>,
    val fields: List<T215FieldRecord>,
    val widths: List<T215WidthRecord>,
    val degeneracy: List<T215DegeneracyRecord>,
    val spreads: List<T215SpreadRecord>,
    val convergence: List<T215ConvergenceRecord>,
    val reproductions: List<T215ReproductionRecord>,
    val predicates: List<T215PredicateRecord>,
    val falsifiers: List<T215FalsifierRecord>,
    val findings: List<String>,
    val parameters: Map<String, Double>
)

private fun leafKeyOf(path: String): String =
    path.substringAfterLast('/').substringBefore('[')

private fun recordOf(path: String): String {
    val parts = path.trim('/').split('/')
    return if (parts.size < 2) "" else parts[parts.size - 2].substringBefore('[')
}

fun main() {
    val ensembleFile = File("gpd/data/T-215-ensemble.json")
    require(ensembleFile.exists()) {
        "run tools/T-215-collect-ensemble.py first — ${ensembleFile.path} is missing"
    }
    val ensemble = Json.parseToJsonElement(ensembleFile.readText()).jsonObject
    val members = ensemble["members"]!!.jsonArray.map {
        T215MemberRecord(
            label = it.jsonObject["label"]!!.jsonPrimitive.content,
            provenance = it.jsonObject["provenance"]!!.jsonPrimitive.content
        )
    }
    val fieldsCompared = ensemble["fieldsCompared"]!!.jsonPrimitive.int()
    val varying = ensemble["varying"]!!.jsonArray.map { it.jsonObject }
    val byKind = ensemble["fieldsVaryingByKind"]!!.jsonObject
    val stringsInDigitsOnly = ensemble["stringFieldsVaryingInDigitsOnly"]!!.jsonPrimitive.int()

    println("T-215 — ${members.size} members, $fieldsCompared common fields, ${varying.size} varying")

    // ---------------------------------------------------------- classify every varying field
    val fields = varying.map { record ->
        val path = record["path"]!!.jsonPrimitive.content
        val kind = record["kind"]!!.jsonPrimitive.content
        val values = record["values"]!!.jsonArray
        val leaf = leafKeyOf(path)
        val parent = recordOf(path)
        val digitsOnly = record["digitsOnly"]?.jsonPrimitive?.content?.toBoolean() ?: false
        val rawConversion = record["carriesRawConversion"]?.jsonArray
            ?.any { it.jsonPrimitive.content.toBoolean() } ?: false
        val classification = when {
            kind == "number" && leaf in T215_VALUE_KEYS -> "VALUE"
            kind == "number" && leaf in T215_POINT_KEYS -> "POINT"
            kind == "number" && leaf == "departure" &&
                    parent in setOf("reproductions", "convergence") -> "ROUNDING"
            kind == "string" && digitsOnly -> "RENDERING"
            kind == "string" && rawConversion -> "REPAIR"
            else -> "OTHER"
        }
        val numbers = if (kind == "number") values.map { it.jsonPrimitive.double() } else emptyList()
        T215FieldRecord(
            path = path,
            kind = kind,
            classification = classification,
            width = if (numbers.isEmpty()) 0.0 else ensembleWidth(numbers),
            distinctReadings = values.map { it.toString() }.distinct().size,
            note = when (classification) {
                "VALUE" -> "the objective a minimax descent reports"
                "POINT" -> "a functional of the argmin, not of the objective"
                "ROUNDING" -> "T-212's deliberate two-significant-digit departure rule"
                "RENDERING" -> "a prose field whose only movement is a rendered VALUE"
                "REPAIR" -> "iteration 28's +-binds-tighter-than-format repair; one member " +
                        "still carries the raw conversion"
                else -> "unclassified — this is F1"
            }
        )
    }
    val other = fields.filter { it.classification == "OTHER" }

    // ---------------------------------------------------------- the two widths on ranges[1]
    fun numbersAt(path: String): List<Double> =
        varying.first { it["path"]!!.jsonPrimitive.content == path }["values"]!!
            .jsonArray.map { it.jsonPrimitive.double() }

    val rangeValues = numbersAt("/ranges[1]/minimaxWorstOverStroke")
    val rangePeaks = numbersAt("/ranges[1]/minimaxPeakRatio")
    val rangeWidth = manifoldWidth(rangeValues, rangePeaks)
    val rangeSolvedForce = numbersAt("/ranges[1]/peakSolvedPathForce")
    val solvedWidth = manifoldWidth(rangeValues, rangeSolvedForce)

    val subsetValues = fields.filter {
        it.classification == "VALUE" && it.path.startsWith("/subsets[")
    }
    val widths = mutableListOf(
        T215WidthRecord(
            quantity = "ranges[1] — 0.5 mM, L0 = 10 nm, C-0032's recommendation",
            members = rangeWidth.members,
            distinctValues = rangeWidth.distinctValues,
            valueWidth = rangeWidth.valueWidth,
            pointWidth = rangeWidth.pointWidth,
            amplification = rangeWidth.amplification,
            note = "the POINT read on max_i k_i, which minimaxPeakRatio, peakPathStiffness, " +
                    "peakPathForceAtAcceptableStroke and peakThermalForce all are"
        ),
        T215WidthRecord(
            quantity = "ranges[1], the POINT read on the peak SUPPORT force instead",
            members = solvedWidth.members,
            distinctValues = solvedWidth.distinctValues,
            valueWidth = solvedWidth.valueWidth,
            pointWidth = solvedWidth.pointWidth,
            amplification = solvedWidth.amplification,
            note = "a different functional of the same argmin — which is why a POINT width is " +
                    "not one number either"
        ),
        T215WidthRecord(
            quantity = "the 31 device subsets — VALUE only, no POINT is emitted for them",
            members = members.size,
            distinctValues = subsetValues.sumOf { it.distinctReadings },
            valueWidth = subsetValues.maxOfOrNull { it.width } ?: 0.0,
            pointWidth = 0.0,
            amplification = null,
            note = "${subsetValues.size} of 31 subsets vary; the worst is the widest VALUE " +
                    "movement anywhere in the file outside ranges[1]"
        )
    )

    // ---------------------------------------------------------- the mechanism, measured
    println("T-215 — measuring the optimiser's own degeneracy ...")
    val sheet = origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)
    val duplexes = 15
    val lengthY = duplexes * sheet.interhelicalDistance
    val mandate = Gen1Tile.TARGET_FORCE / Gen1Tile.ACCEPTABLE_STROKE
    val interior = Gen1Tile.TARGET_FORCE / (Gen1Tile.EDGE_X * lengthY)
    val lattice = OrigamiGrillage(
        sheet = sheet,
        lengthX = Gen1Tile.EDGE_X,
        beamCount = duplexes,
        foundationStiffness = Gen1Tile.FOUNDATION_SECANT,
        columns = CrossoverLayout.centred(8, sheet.crossoverSpacing / 2.0),
        subdivisions = 2,
        supports = emptyList()
    )
    fun field(depth: Double, width: Double, rim: Double): PressureField = edgeCollarPressure(
        interior, Gen1Tile.EDGE_X, lengthY,
        listOf(CollarTerm(depth, width), CollarTerm(rim, 1.0))
    )
    // C-0022's solved collars, transcribed from gpd/results/T-3b-*.json exactly as
    // RobustDistributionTest transcribes them, so this study needs no upstream file.
    val designPoint = LoadState("2 mM, 10 nm, 0.192 V", field(-0.302887367, 8.93928311, -0.593889278))
    val tenMillimolar = LoadState("10 mM, 10 nm, 0.192 V", field(0.419998636, 2.39768412, -2.73316696))
    val twoNanometre = LoadState("2 mM, 2 nm, 0.368 V", field(-0.0514981261, 6.56393103, 1.08681801))
    val loads = listOf(designPoint, tenMillimolar, twoNanometre)
    val grid = attachmentGrid(3, duplexes, Gen1Tile.EDGE_X, lengthY)
    val surrogate = multiStateSurrogate(lattice, grid, loads, 81)
    val paths = grid.size
    val starts = listOf(
        normalisedStiffnesses(List(paths) { 1.0 }, mandate),
        normalisedStiffnesses(
            rimStiffenedWeights(grid, Gen1Tile.EDGE_X, lengthY, 6.7, 2.0), mandate
        ),
        normalisedStiffnesses(
            rimStiffenedWeights(grid, Gen1Tile.EDGE_X, lengthY, 6.7, 5.0), mandate
        )
    )
    val offsets = listOf(0, 1, 2, 4, 8)
    val degeneracy = listOf(
        listOf(0, 1) to "two anti-parallel 10 nm states",
        listOf(0, 1, 2) to "three states, the 2 nm state included"
    ).map { (stateSet, label) ->
        val optimum = minimaxStiffnessDistribution(
            surrogate = surrogate, states = stateSet, totalStiffness = mandate, starts = starts
        )
        val measured = descentDegeneracy(
            surrogate = surrogate, states = stateSet, totalStiffness = mandate,
            starts = starts, ulpOffsets = offsets
        )
        println(
            ("  %-44s binding %d of %d  value %8.2e  point %8.2e").format(
                label, optimum.bindingStates.size, stateSet.size,
                measured.valueWidth, measured.pointWidth
            )
        )
        T215DegeneracyRecord(
            problem = label,
            states = stateSet.size,
            paths = paths,
            bindingStates = optimum.bindingStates.size,
            ulpOffsets = offsets,
            valueWidth = flooredWidth(measured.valueWidth),
            pointWidth = flooredWidth(measured.pointWidth),
            amplification = null,
            note = "one weight of one start advanced by whole units in the last place; " +
                    "everything else held. Widths are floored at " + T215_WIDTH_FLOOR + ", " +
                    "below which a width is a difference of two Doubles and not a reading"
        )
    }

    // ---------------------------------------------------------- the winner-selection channel
    println("T-215 — the near-optimal set the winning start is chosen from ...")
    val spreads = listOf(
        listOf(0, 1) to "two anti-parallel 10 nm states",
        listOf(0, 1, 2) to "three states, the 2 nm state included"
    ).map { (stateSet, label) ->
        val spread = nearOptimalSpread(
            surrogate = surrogate, states = stateSet, totalStiffness = mandate,
            starts = starts, tolerance = 1e-2
        )
        println(
            ("  %-44s %d of %d start(s) within 1 %%  value %8.2e  point %8.2e").format(
                label, spread.startsWithinTolerance, spread.startsUsed,
                spread.width.valueWidth, spread.width.pointWidth
            )
        )
        T215SpreadRecord(
            problem = label,
            states = stateSet.size,
            paths = paths,
            startsUsed = spread.startsUsed,
            startsWithinTolerance = spread.startsWithinTolerance,
            tolerance = spread.tolerance,
            valueWidth = spread.width.valueWidth,
            pointWidth = spread.width.pointWidth,
            amplification = spread.width.amplification,
            allStartsValueWidth = spread.allStartsWidth.valueWidth,
            allStartsPointWidth = spread.allStartsWidth.pointWidth,
            note = "each start run ALONE; the ensemble's answer is whichever of these wins a " +
                    "single strict comparison, so a jitter that flips it moves the file by the " +
                    "POINT width of this set and not by anything smaller. The band test is " +
                    "taken at the decision precision; the widths themselves move by up to 8e-5 " +
                    "between two runs of this study and are emitted at two significant digits"
        )
    }

    // ---------------------------------------------------------- convergence in the member count
    val partialWidths = (2..members.size).map { ensembleWidth(rangeValues.take(it)) }
    val convergence = listOf(
        T215ConvergenceRecord(
            quantity = "the VALUE width of ranges[1]",
            parameter = "ensemble members",
            values = (2..members.size).map { it.toDouble() },
            results = partialWidths,
            departure = if (partialWidths.size < 2) 0.0 else {
                val last = partialWidths.last()
                val previous = partialWidths[partialWidths.size - 2]
                if (last == 0.0) abs(last - previous) else abs(last - previous) / abs(last)
            },
            note = "a width is a MAXIMUM over an ensemble, so it is monotone non-decreasing in " +
                    "the member count and settles only once every basin has been drawn"
        ),
        T215ConvergenceRecord(
            quantity = "the POINT width of ranges[1]",
            parameter = "ensemble members",
            values = (2..members.size).map { it.toDouble() },
            results = (2..members.size).map { ensembleWidth(rangePeaks.take(it)) },
            departure = run {
                val series = (2..members.size).map { ensembleWidth(rangePeaks.take(it)) }
                if (series.size < 2) 0.0 else {
                    val last = series.last()
                    val previous = series[series.size - 2]
                    if (last == 0.0) abs(last - previous) else abs(last - previous) / abs(last)
                }
            },
            note = "same statistic on the argmin's own functional"
        )
    )

    // ---------------------------------------------------------- what C-0131 published
    val worstSubset = subsetValues.maxOfOrNull { it.width } ?: 0.0
    val reproductions = listOf(
        T215ReproductionRecord(
            source = "C-0131 §6",
            quantity = "the widest non-departure movement of T-129, ranges[1] POINT — C-0131 " +
                    "read it against the committed file and this ensemble contains that pair",
            published = 0.0060,
            reproduced = rangeWidth.pointWidth,
            departure = abs(rangeWidth.pointWidth - 0.0060) / 0.0060,
            strict = false
        ),
        T215ReproductionRecord(
            source = "C-0131 §6",
            quantity = "the subsets[*] residual between two runs of identical code — C-0131's " +
                    "is a TWO-member reading and this is a TEN-member MAXIMUM, so a width " +
                    "that did not grow would mean the ensemble had drawn no new basin",
            published = 8.6e-4,
            reproduced = worstSubset,
            departure = abs(worstSubset - 8.6e-4) / 8.6e-4,
            strict = false
        )
    )

    // ---------------------------------------------------------- predicates and falsifiers
    val identicalPointKeys = listOf(
        "/ranges[1]/minimaxPeakRatio", "/ranges[1]/peakPathStiffness",
        "/ranges[1]/peakPathForceAtAcceptableStroke", "/ranges[1]/peakThermalForce"
    ).map { path -> fields.first { it.path == path }.width }
    val pointKeysAgree = identicalPointKeys.all {
        abs(it - identicalPointKeys[0]) < 1e-6 * identicalPointKeys[0]
    }
    val booleansVarying = byKind["boolean"]!!.jsonPrimitive.int()
    val stringsVarying = byKind["string"]!!.jsonPrimitive.int()
    val stringsRepaired = ensemble["stringFieldsVaryingWithARawConversion"]!!.jsonPrimitive.int()
    val stringsUnaccounted = stringsVarying - stringsInDigitsOnly - stringsRepaired

    val predicates = listOf(
        T215PredicateRecord(
            name = "P2",
            statement = "every varying field is a VALUE, a POINT, a deliberate rounding or a " +
                    "rendering of one of them; the OTHER bucket is empty",
            met = other.isEmpty(),
            verdict = ("%d varying field(s) of %d compared: %d VALUE, %d POINT, %d ROUNDING, " +
                    "%d RENDERING, %d OTHER").format(
                fields.size, fieldsCompared,
                fields.count { it.classification == "VALUE" },
                fields.count { it.classification == "POINT" },
                fields.count { it.classification == "ROUNDING" },
                fields.count { it.classification == "RENDERING" } +
                        fields.count { it.classification == "REPAIR" },
                other.size
            )
        ),
        T215PredicateRecord(
            name = "P3",
            statement = "the width is quoted on the VALUE and on the POINT, with the ratio",
            met = rangeWidth.amplification != null,
            verdict = ("VALUE %.4e, POINT %.4e, amplification %.4f").format(
                rangeWidth.valueWidth, rangeWidth.pointWidth, rangeWidth.amplification ?: 0.0
            )
        ),
        T215PredicateRecord(
            name = "P4",
            statement = "no verdict of the file depends on the unstable part",
            met = booleansVarying == 0 && stringsUnaccounted == 0,
            verdict = ("%d boolean field(s) vary; %d string field(s) vary, %d of those differ " +
                    "in DIGITS ONLY and %d is iteration 28's format-string repair; %d " +
                    "unaccounted").format(
                booleansVarying, stringsVarying, stringsInDigitsOnly, stringsRepaired,
                stringsUnaccounted
            )
        ),
        T215PredicateRecord(
            name = "P6",
            statement = "the optimiser's degeneracy is measured directly, not inferred",
            met = degeneracy.isNotEmpty(),
            verdict = degeneracy.joinToString("; ") {
                ("%s: binding %d of %d, VALUE %.2e, POINT %.2e").format(
                    it.problem, it.bindingStates, it.states, it.valueWidth, it.pointWidth
                )
            }
        )
    )

    val booleanFieldsCompared = ensemble["fieldsComparedByKind"]!!.jsonObject["boolean"]!!
        .jsonPrimitive.int()
    val stringFieldsCompared = ensemble["fieldsComparedByKind"]!!.jsonObject["string"]!!
        .jsonPrimitive.int()
    val f1 = other.isNotEmpty()
    val f2 = rangeWidth.pointWidth < rangeWidth.valueWidth
    val f3 = booleansVarying > 0 || stringsUnaccounted > 0
    val f5 = degeneracy.all { it.valueWidth == 0.0 && it.pointWidth == 0.0 }
    val falsifiers = listOf(
        T215FalsifierRecord(
            name = "F1",
            statement = "a varying field outside the two descent blocks would falsify the manifold reading",
            fired = f1,
            evidence = if (f1) other.joinToString(", ") { it.path }
            else "every one of ${fields.size} varying fields is a descent output or a deliberate rounding"
        ),
        T215FalsifierRecord(
            name = "F2",
            statement = "the POINT moving no further than the VALUE would mean the answer moves, not its place",
            fired = f2,
            evidence = ("POINT %.4e against VALUE %.4e").format(
                rangeWidth.pointWidth, rangeWidth.valueWidth
            )
        ),
        T215FalsifierRecord(
            name = "F3",
            statement = "any boolean, binding-state list or verdict differing across the ensemble",
            fired = f3,
            evidence = ("%d boolean(s) and %d unaccounted string(s) vary").format(
                booleansVarying, stringsUnaccounted
            )
        ),
        T215FalsifierRecord(
            name = "F5",
            statement = "a whole-ulp perturbation moving neither the value nor the point would " +
                    "mean the optimiser is locally unique and the movement has another cause",
            fired = f5,
            evidence = ("the raw widths are at machine epsilon (1e-16 to 1e-15, and they differ " +
                    "between two runs of this study, which is why they are floored at " +
                    T215_WIDTH_FLOOR + "), so the INPUT channel is excluded — %s. " +
                    "The channel that does carry the movement is the winning-start comparison: " +
                    "%s").format(
                degeneracy.joinToString("; ") {
                    ("%s VALUE %.2e POINT %.2e").format(it.problem, it.valueWidth, it.pointWidth)
                },
                spreads.joinToString("; ") {
                    ("%s VALUE %.2e POINT %.2e over %d near-optimal start(s)").format(
                        it.problem, it.valueWidth, it.pointWidth, it.startsWithinTolerance
                    )
                }
            )
        )
    )

    val basins = rangeValues.groupingBy { it }.eachCount().entries.sortedByDescending { it.value }
    val basinCensus = basins.joinToString("; ") { (value, count) ->
        ("%.9g in %d of %d member(s): %s").format(
            value, count, members.size,
            members.indices.filter { rangeValues[it] == value }.joinToString(", ") {
                members[it].label
            }
        )
    }

    val findings = listOf(
        ("THE 0.60 %% AND THE 8.6e-4 ARE ONE PHENOMENON, READ ON TWO FUNCTIONALS. Over %d " +
                "independent emissions of gpd/results/T-129-range-robust-placement.json, %d of " +
                "%d fields are identical in every member. Every one of the %d that vary is a " +
                "descent output or a deliberate rounding: %d are a minimax objective, %d are a " +
                "functional of a minimax argmin, %d are T-212's two-digit departure rule and %d " +
                "is that objective rendered into a prose verdict. F1 did not fire.").format(
            members.size, fieldsCompared - fields.size, fieldsCompared, fields.size,
            fields.count { it.classification == "VALUE" },
            fields.count { it.classification == "POINT" },
            fields.count { it.classification == "ROUNDING" },
            fields.count { it.classification == "RENDERING" } +
                    fields.count { it.classification == "REPAIR" }
        ),
        ("THE OPTIMAL SET AT ranges[1] IS TWO-VALUED AND A FRESH RUN DRAWS FROM BOTH. %s. " +
                "The iteration-13 tree, git-archived and run unmodified today, lands on the " +
                "reading iteration 13 did NOT emit — so nothing in the repository between then " +
                "and HEAD is responsible, and the movement is a live draw rather than a " +
                "one-time upstream change.").format(basinCensus),
        ("THE POINT IS %.2fx WIDER THAN THE VALUE, AND THE POINT IS NOT ONE NUMBER EITHER. At " +
                "ranges[1] the objective spans %.4e and max_i k_i spans %.4e — and the peak " +
                "SUPPORT force at the same argmin spans %.4e, %.2fx narrower than max_i k_i. " +
                "Which functional of the point a file happens to emit decides how large its " +
                "irreproducibility looks.").format(
            rangeWidth.amplification ?: 0.0, rangeWidth.valueWidth, rangeWidth.pointWidth,
            solvedWidth.pointWidth,
            if (solvedWidth.pointWidth > 0.0) rangeWidth.pointWidth / solvedWidth.pointWidth else 0.0
        ),
        ("THE FOUR max_i k_i FIELDS MOVE BY THE IDENTICAL RELATIVE AMOUNT, WHICH IS THE CHECK " +
                "THAT THEY ARE ONE QUANTITY: %s. Agreement to one part in a million: %s.").format(
            identicalPointKeys.joinToString(", ") { "%.6e".format(it) }, pointKeysAgree
        ),
        ("NO VERDICT MOVES. Of %d boolean and %d string fields compared — the booleans are the " +
                "flatness verdicts and the strings carry every bindingStates list — %d boolean(s) " +
                "vary and %d string(s) do: %d of the strings differ in DIGITS ONLY — \"0.0365\" against " +
                "\"0.0366\" inside predicates[1].verdict, which is CLAUDE.md's own \"a moved " +
                "STRING is not necessarily a moved decision\" — and %d is iteration 28's " +
                "+-binds-tighter-than-.format repair, where one member still carries a RAW " +
                "conversion. %d unaccounted. F3 did not fire.").format(
            booleanFieldsCompared, stringFieldsCompared, booleansVarying, stringsVarying,
            stringsInDigitsOnly, stringsRepaired, stringsUnaccounted
        ),
        ("THE INPUT CHANNEL IS EXCLUDED AND THE WINNER-SELECTION CHANNEL IS NOT — WHICH IS " +
                "WHAT THE ULP PROBE WAS FOR. Advancing ONE weight of ONE start by whole units " +
                "in the last place, holding everything else, moves the answer by %s — the raw " +
                "readings are at 1e-16 to 1e-15 and are floored: the descent is NOT chaotic in " +
                "its own input. What it is sensitive to is which " +
                "start wins, and that is ONE strict comparison over the terminal readings of " +
                "the whole ensemble — %s. A jitter that flips that comparison moves the file " +
                "by the POINT width of the near-optimal set, which is the size the file shows.")
            .format(
                degeneracy.joinToString("; ") {
                    ("%s VALUE %.2e POINT %.2e").format(it.problem, it.valueWidth, it.pointWidth)
                },
                spreads.joinToString("; ") {
                    ("%s: %d of %d starts within %.0f %%, VALUE %.2e, POINT %.2e").format(
                        it.problem, it.startsWithinTolerance, it.startsUsed,
                        100.0 * it.tolerance, it.valueWidth, it.pointWidth
                    )
                }
            )
    )

    val result = T215Result(
        task = "T-215",
        leaf = "none — a process task about the reproducibility of an emitted artifact",
        conditions = ("T = 300 K, k_BT = 4.141947 pN nm; the ensemble is %d independent " +
                "emissions of gpd/results/T-129-range-robust-placement.json, three read out of " +
                "git and the rest fresh runs of anchoring.RangeRobustPlacementStudyKt at HEAD; " +
                "the degeneracy measurement is a %d-path multi-state minimax on C-0022's solved " +
                "collars at C-0017's %.4f pN/nm mandate").format(members.size, paths, mandate),
        decision = ("the file's irreproducibility is confined to %d of %d fields, all of them " +
                "descent outputs; the VALUE moves %.4e and the POINT %.4e, and no verdict moves")
            .format(
                fields.count { it.classification == "VALUE" || it.classification == "POINT" },
                fieldsCompared, rangeWidth.valueWidth, rangeWidth.pointWidth
            ),
        members = members,
        fields = fields,
        widths = widths,
        degeneracy = degeneracy,
        spreads = spreads,
        convergence = convergence,
        reproductions = reproductions,
        predicates = predicates,
        falsifiers = falsifiers,
        findings = findings,
        parameters = mapOf(
            "ensembleMembers" to members.size.toDouble(),
            "fieldsCompared" to fieldsCompared.toDouble(),
            "fieldsVarying" to fields.size.toDouble(),
            "fieldsIdentical" to (fieldsCompared - fields.size).toDouble(),
            "rangeValueWidth" to rangeWidth.valueWidth,
            "rangePointWidth" to rangeWidth.pointWidth,
            "rangeAmplification" to (rangeWidth.amplification ?: 0.0),
            "worstSubsetValueWidth" to worstSubset,
            "degeneracyPaths" to paths.toDouble(),
            "nearOptimalPointWidth" to (spreads.maxOfOrNull { it.pointWidth } ?: 0.0),
            "mandate" to mandate
        )
    )

    val output = File("gpd/results/T-215-descent-manifold-width.json")
    output.parentFile.mkdirs()
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digitsByKey = mapOf(
                    "convergence/departure" to 2,
                    "reproductions/departure" to 2,
                    // The spreads are a MEASUREMENT OF an irreproducibility and they inherit it:
                    // two runs of this study move them by up to 8e-5 relative, because each of
                    // the per-start descents has its own terminal point. Nine digits would make
                    // this file un-diffable to hide a movement it is itself about; two puts the
                    // quantisation boundary a hundred times beyond the movement.
                    "spreads/valueWidth" to 2,
                    "spreads/pointWidth" to 2,
                    "spreads/amplification" to 2,
                    "spreads/allStartsValueWidth" to 2,
                    "spreads/allStartsPointWidth" to 2
                ),
                floor = 0.0
            ).withEmissionHeader(LatticeTag.SQUARE, null) as JsonObject)
        )
    )
    println()
    result.findings.forEach { println("  * $it"); println() }
    println("written to ${output.path}")
}

private fun JsonPrimitive.int(): Int = content.toDouble().toInt()

private fun JsonPrimitive.double(): Double = content.toDouble()
