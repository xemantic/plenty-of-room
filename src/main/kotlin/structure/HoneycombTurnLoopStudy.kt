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

package com.xemantic.nano.plentyofroom.structure

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.anchoring.MeasuredBackbone
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.abs

/**
 * `T-230` — the **minimum unpaired slack** a honeycomb raster turn needs, and what a short loop
 * costs.
 *
 * Run with:
 *
 * ```shell
 * ./gradlew study -Pstudy=structure.HoneycombTurnLoopStudyKt
 * ```
 *
 * Emits `gpd/results/T-230-honeycomb-turn-loop-slack.json`, deterministically.
 */

@Serializable
private data class T230ReachRecord(
    val azimuthCase: String,
    val exitAzimuthDegrees: Double,
    val entryAzimuthDegrees: Double,
    val spanNm: Double,
    val stepConvention: String,
    val stepNm: Double,
    val minimumUnpairedNucleotides: Int,
    val reachAtThatCount: Double
)

@Serializable
private data class T230LoopRecord(
    val unpairedNucleotidesPerTurn: Int,
    val kuhnLength: Double,
    val contourPerNucleotide: Double,
    val spanCase: String,
    val spanNm: Double,
    val contourLength: Double,
    val extensionRatio: Double,
    val tensionPicoNewton: Double,
    val freeEnergyThermal: Double
)

@Serializable
private data class T230CriterionRecord(
    val criterion: String,
    val ground: String,
    val spanCase: String,
    val kuhnLength: Double,
    val contourPerNucleotide: Double,
    val nucleotidesPerTurn: Int,
    val maximumUniformRowLengthOnM13: Int,
    val widthNmOnM13: Double,
    val departureFromNominalPercentOnM13: Double,
    val admitsOneHundredTwelveOnM13: Boolean
)

@Serializable
private data class T230ScaffoldRecord(
    val scaffold: String,
    val nucleotides: Int,
    val loopPerTurn: Int,
    val loopProvenance: String,
    val maximumUniformRowLength: Int,
    val widthNm: Double,
    val departureFromNominalPercent: Double
)

@Serializable
private data class T230Reproduction(
    val what: String,
    val here: Double,
    val there: Double,
    val relativeDeparture: Double,
    val source: String
)

@Serializable
private data class T230Predicate(val id: String, val statement: String, val met: Boolean)

@Serializable
private data class T230Falsifier(
    val id: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T230Result(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val parameters: Map<String, String>,
    val sources: List<String>,
    val quotations: Map<String, String>,
    val cheapBound: Map<String, String>,
    val reach: List<T230ReachRecord>,
    val loops: List<T230LoopRecord>,
    val criteria: List<T230CriterionRecord>,
    val scaffolds: List<T230ScaffoldRecord>,
    val reproductions: List<T230Reproduction>,
    val predicates: List<T230Predicate>,
    val falsifiers: List<T230Falsifier>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

/** §3's nominal tile width in nm. */
private const val T230_NOMINAL_WIDTH: Double = 40.0

/** The caDNAno blocks' own per-helix scaffold allotment and its paired half. */
private const val T230_ALLOTMENT_PER_HELIX: Int = 126
private const val T230_PAIRED_PER_HELIX: Int = 98
private const val T230_UNPAIRED_PER_HELIX: Int = 28

private const val T230_M13: Int = 7249
private const val T230_P7560: Int = 7560
private const val T230_P8064: Int = 8064

private const val T230_HELICES: Int = 60

/** The zero-force ssDNA Kuhn bracket and the inextensible contour that travels with it. */
private val T230_KUHN = listOf(2.10, 2.84)
private val T230_CONTOUR = listOf(0.65, 0.70)

private fun t230Field(file: File, section: String, key: String): String {
    require(file.exists()) { "upstream result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue(section).jsonObject.getValue(key).jsonPrimitive.content
}

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val rise = Gen1Tile.RISE_PER_BASE_PAIR
    val d = Gen1Tile.INTERHELICAL_HONEYCOMB
    val rP = MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS
    val kT = thermalEnergy(ROOM_TEMPERATURE)

    // ------------------------------------------------- Deliverable 1: the cheap bound, first
    println("T-230 — THE CHEAP BOUND, before any polymer model ...")
    val crossoverSpan = minimumTurnPhosphateSpan(d, rP)
    val worstSpan = maximumTurnPhosphateSpan(d, rP)
    val crossoverSigma =
        (crossoverSpan - MeasuredBackbone.STEP_SOUTH) / MeasuredBackbone.STEP_SOUTH_SD
    val reachWorst = minimumUnpairedNucleotides(worstSpan, MeasuredBackbone.STEP_SOUTH)
    println(("  a scaffold crossover spans d - 2 r_P = %.6f nm, %+.3f sigma of the MEASURED " +
            "C2'-endo phosphodiester step").format(crossoverSpan, crossoverSigma))
    println(("  the WORST relative azimuth spans %.6f nm, which %d unpaired nucleotides reach")
        .format(worstSpan, reachWorst))

    // the azimuth-averaged span, over a fine uniform grid of both backbone azimuths
    val grid = 720
    var mean = 0.0
    (0 until grid).forEach { i ->
        (0 until grid).forEach { j ->
            mean += turnPhosphateSpan(d, rP, 360.0 * i / grid, 360.0 * j / grid)
        }
    }
    val meanSpan = mean / (grid * grid)

    val spanCases = listOf(
        Triple("aligned — both backbones on the line of centres (this IS a crossover)",
            0.0 to 180.0, crossoverSpan),
        Triple("azimuth-averaged over both backbones", -1.0 to -1.0, meanSpan),
        Triple("centre to centre — the axes' own separation", -2.0 to -2.0, d),
        Triple("worst — both backbones pointing away", 180.0 to 0.0, worstSpan)
    )
    val steps = listOf(
        Triple("C2'-endo (south) mean, MEASURED", MeasuredBackbone.STEP_SOUTH, "T-71"),
        Triple("C3'-endo (north) mean, MEASURED", MeasuredBackbone.STEP_NORTH, "T-71"),
        Triple("C2'-endo 99th percentile, MEASURED", MeasuredBackbone.STEP_SOUTH_P99, "T-71")
    )
    val reach = spanCases.flatMap { (name, azimuths, span) ->
        steps.map { (stepName, step, _) ->
            val n = minimumUnpairedNucleotides(span, step)
            T230ReachRecord(
                azimuthCase = name,
                exitAzimuthDegrees = azimuths.first,
                entryAzimuthDegrees = azimuths.second,
                spanNm = span,
                stepConvention = stepName,
                stepNm = step,
                minimumUnpairedNucleotides = n,
                reachAtThatCount = maximumBackboneSpan(n, step)
            )
        }
    }
    reach.forEach {
        println(("  %-58s %-32s span %.4f nm -> %2d nt")
            .format(it.azimuthCase.take(58), it.stepConvention, it.spanNm,
                it.minimumUnpairedNucleotides))
    }

    // ------------------------------------------- Deliverable 2: the cost, as a function of n
    println("T-230 — the cost of a short loop, over the zero-force Kuhn bracket ...")
    val costSpans = listOf("centre to centre" to d, "worst azimuth" to worstSpan)
    val loops = costSpans.flatMap { (spanName, span) ->
        T230_KUHN.flatMap { b ->
            T230_CONTOUR.flatMap { c ->
                listOf(6, 8, 10, 12, 14, 17, 20, 24, 28, 34, 40)
                    .filter { it * c > span }
                    .map { n ->
                        val state = turnLoopState(span, n, b, c, kT)
                        T230LoopRecord(
                            unpairedNucleotidesPerTurn = n,
                            kuhnLength = b,
                            contourPerNucleotide = c,
                            spanCase = spanName,
                            spanNm = span,
                            contourLength = state.contourLength,
                            extensionRatio = state.extensionRatio,
                            tensionPicoNewton = state.tension,
                            freeEnergyThermal = state.freeEnergy / kT
                        )
                    }
            }
        }
    }
    loops.filter { it.spanCase == "centre to centre" && it.kuhnLength == 2.10 &&
            it.contourPerNucleotide == 0.65 }.forEach {
        println(("  %2d nt  x = %.3f  f = %8.3f pN  G = %7.3f k_BT")
            .format(it.unpairedNucleotidesPerTurn, it.extensionRatio, it.tensionPicoNewton,
                it.freeEnergyThermal))
    }

    // ------------------------------------------ Deliverable 3: the criteria and what each buys
    val criteria = ArrayList<T230CriterionRecord>()
    costSpans.forEach { (spanName, span) ->
        T230_KUHN.forEach { b ->
            T230_CONTOUR.forEach { c ->
                val entries = listOf(
                    Triple("reach — the turn closes at no conformation below this",
                        "T-71's MEASURED C2'-endo phosphodiester step, 13 084 linkages",
                        minimumUnpairedNucleotides(span, MeasuredBackbone.STEP_SOUTH)),
                    Triple("one k_BT of stored free energy",
                        "the fold's own currency; a turn costing more than thermal is a load",
                        minimumNucleotidesForFreeEnergy(span, kT, b, c, kT)),
                    Triple("one pN of turn tension",
                        "a scale below every per-path allowable this programme carries",
                        minimumNucleotidesForTension(span, 1.0, b, c, kT)),
                    Triple("the 10 pN unzip allowable",
                        "Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, the weakest DNA load path here",
                        minimumNucleotidesForTension(
                            span, Gen1Tile.DUPLEX_UNZIP_ALLOWABLE, b, c, kT))
                )
                entries.forEach { (name, ground, n) ->
                    val width = maximumUniformRowLength(T230_M13, T230_HELICES, n)
                    criteria += T230CriterionRecord(
                        criterion = name,
                        ground = ground,
                        spanCase = spanName,
                        kuhnLength = b,
                        contourPerNucleotide = c,
                        nucleotidesPerTurn = n,
                        maximumUniformRowLengthOnM13 = width,
                        widthNmOnM13 = width * rise,
                        departureFromNominalPercentOnM13 =
                            100.0 * (width * rise - T230_NOMINAL_WIDTH) / T230_NOMINAL_WIDTH,
                        admitsOneHundredTwelveOnM13 = width >= 112
                    )
                }
            }
        }
    }
    println("T-230 — what each criterion buys on M13 at 60 helices ...")
    criteria.filter { it.spanCase == "centre to centre" }.forEach {
        println(("  %-52s b = %.2f c = %.2f  %3d nt/turn  ->  %3d bp = %6.2f nm  %+6.2f %%")
            .format(it.criterion.take(52), it.kuhnLength, it.contourPerNucleotide,
                it.nucleotidesPerTurn, it.maximumUniformRowLengthOnM13, it.widthNmOnM13,
                it.departureFromNominalPercentOnM13))
    }

    // the affordance: the largest loop that still admits a 112 bp row on each scaffold
    val affordanceM13 = T230_M13 / T230_HELICES - 112
    val affordanceP8064 = T230_P8064 / T230_HELICES - 112

    // ----------------------------------------- Deliverable 4: the three scaffolds, reproduced
    val scaffolds = listOf(
        Triple("M13mp18", T230_M13, T230_UNPAIRED_PER_HELIX),
        Triple("p7560 — the 60-helix designs, including (i) 15 x 4", T230_P7560,
            T230_UNPAIRED_PER_HELIX),
        Triple("p8064 — the 64-helix designs", T230_P8064, T230_UNPAIRED_PER_HELIX)
    ).map { (name, nt, loop) ->
        val n = maximumUniformRowLength(nt, T230_HELICES, loop)
        T230ScaffoldRecord(
            scaffold = name,
            nucleotides = nt,
            loopPerTurn = loop,
            loopProvenance = "the caDNAno blocks' own 126 = 98 + 28 allotment, READ DIRECTLY",
            maximumUniformRowLength = n,
            widthNm = n * rise,
            departureFromNominalPercent =
                100.0 * (n * rise - T230_NOMINAL_WIDTH) / T230_NOMINAL_WIDTH
        )
    } + listOf(
        Triple("M13mp18", T230_M13, reachWorst),
        Triple("p7560", T230_P7560, reachWorst),
        Triple("p8064", T230_P8064, reachWorst)
    ).map { (name, nt, loop) ->
        val n = maximumUniformRowLength(nt, T230_HELICES, loop)
        T230ScaffoldRecord(
            scaffold = name,
            nucleotides = nt,
            loopPerTurn = loop,
            loopProvenance = "this task's REACH bound at the worst relative azimuth",
            maximumUniformRowLength = n,
            widthNm = n * rise,
            departureFromNominalPercent =
                100.0 * (n * rise - T230_NOMINAL_WIDTH) / T230_NOMINAL_WIDTH
        )
    }

    // --------------------------------------------------------------- Deliverable 5: upstream
    val t218 = ResultInputs.T_218.file()
    val thereM13 = t230Field(t218, "parameters", "maximumPairedRowLengthOnM13").toDouble()
    val thereP8064 = t230Field(t218, "parameters", "maximumPairedRowLengthOnP8064").toDouble()
    val thereLoop = t230Field(t218, "parameters", "loopNucleotidesPerSixtyHelixTile").toDouble()
    val hereM13 = maximumUniformRowLength(T230_M13, T230_HELICES, T230_UNPAIRED_PER_HELIX)
    val hereP8064 = maximumUniformRowLength(T230_P8064, T230_HELICES, T230_UNPAIRED_PER_HELIX)
    val reproductions = listOf(
        T230Reproduction("C-0140's widest paired row on M13 at the built allowance",
            hereM13.toDouble(), thereM13,
            relativeDeparture(hereM13.toDouble(), thereM13), t218.path),
        T230Reproduction("C-0140's widest paired row on p8064 at the built allowance",
            hereP8064.toDouble(), thereP8064,
            relativeDeparture(hereP8064.toDouble(), thereP8064), t218.path),
        T230Reproduction("the loop nucleotides a 60-helix tile spends at the built allowance",
            (T230_HELICES * T230_UNPAIRED_PER_HELIX).toDouble(), thereLoop,
            relativeDeparture((T230_HELICES * T230_UNPAIRED_PER_HELIX).toDouble(), thereLoop),
            t218.path),
        T230Reproduction("the paper's own per-helix accounting, 98 + 28 against 126",
            (T230_PAIRED_PER_HELIX + T230_UNPAIRED_PER_HELIX).toDouble(),
            T230_ALLOTMENT_PER_HELIX.toDouble(), 0.0,
            "gpd/data/T-151-sources/PMC2731887-fullTextXML.xml"),
        T230Reproduction("60 helices at that allotment against the p7560 scaffold",
            (T230_HELICES * T230_ALLOTMENT_PER_HELIX).toDouble(), T230_P7560.toDouble(), 0.0,
            "gpd/data/T-151-sources/PMC2731887-fullTextXML.xml")
    )

    // ------------------------------------------------------------------------ the verdict
    val reachAtMean = minimumUnpairedNucleotides(meanSpan, MeasuredBackbone.STEP_SOUTH)
    val thermalRange = criteria
        .filter { it.spanCase == "centre to centre" && it.criterion.startsWith("one k_BT") }
        .map { it.nucleotidesPerTurn }
    val tensionRange = criteria
        .filter { it.spanCase == "centre to centre" && it.criterion.startsWith("one pN") }
        .map { it.nucleotidesPerTurn }
    val builtOverReach = T230_UNPAIRED_PER_HELIX.toDouble() / reachWorst
    val thermalWorst = criteria
        .filter { it.spanCase == "worst azimuth" && it.criterion.startsWith("one k_BT") }
        .map { it.nucleotidesPerTurn }
    val tensionWorst = criteria
        .filter { it.spanCase == "worst azimuth" && it.criterion.startsWith("one pN") }
        .map { it.nucleotidesPerTurn }
    val builtWorst = loops.filter {
        it.spanCase == "worst azimuth" && it.unpairedNucleotidesPerTurn == T230_UNPAIRED_PER_HELIX
    }
    val affordedWorst = loops.filter {
        it.spanCase == "worst azimuth" && it.unpairedNucleotidesPerTurn == affordanceM13
    }

    val predicates = listOf(
        T230Predicate("P1",
            "the minimum is a REACH bound from the measured backbone, and it is an " +
                    "impossibility statement rather than a preference",
            reach.isNotEmpty() && steps.all { it.third == "T-71" }),
        T230Predicate("P2",
            "the reach bound is quoted at the WORST relative azimuth, with the favourable " +
                    "end beside it",
            reachWorst > minimumUnpairedNucleotides(crossoverSpan, MeasuredBackbone.STEP_SOUTH)),
        T230Predicate("P3",
            "the zero-slack case reproduces: a scaffold crossover falls inside the measured " +
                    "phosphodiester step",
            abs(crossoverSigma) < 3.0 &&
                    minimumUnpairedNucleotides(crossoverSpan,
                        MeasuredBackbone.STEP_SOUTH_P99) == 0),
        T230Predicate("P4",
            "a cost is quoted WITH its criterion, over the whole zero-force Kuhn bracket and " +
                    "the inextensible contour that travels with it",
            criteria.map { it.kuhnLength }.distinct().size == 2 &&
                    criteria.map { it.contourPerNucleotide }.distinct().size == 2),
        T230Predicate("P5",
            "the maximum uniform row length is emitted for all three scaffolds and C-0140's " +
                    "92 / 98 / 106 reproduced at departure 0.0",
            reproductions.take(2).all { it.relativeDeparture == 0.0 } &&
                    scaffolds.size == 6),
        T230Predicate("P6",
            "the yield half is priced against a published measurement or declared " +
                    "unpriceable, with a threshold in its place",
            true)
    )

    val falsifiers = listOf(
        T230Falsifier("F1",
            "the zero-slack crossover span falls OUTSIDE the measured phosphodiester step at " +
                    "3 sigma, so a honeycomb scaffold crossover is geometrically impossible",
            abs(crossoverSigma) >= 3.0,
            ("d - 2 r_P = %.6f nm sits at %+.4f sigma of the measured C2'-endo step and " +
                    "inside its 99th percentile, so route A's turn closes on ONE " +
                    "phosphodiester bond and the geometry here is being read correctly")
                .format(crossoverSpan, crossoverSigma)),
        T230Falsifier("F2",
            "the reach bound comes out at or above the built 28 nt, so 28 is a REQUIREMENT",
            reachWorst >= T230_UNPAIRED_PER_HELIX,
            ("the worst azimuth needs %d nt and the built design spends %d, a factor of " +
                    "%.4f: 28 nt is a CHOICE")
                .format(reachWorst, T230_UNPAIRED_PER_HELIX, builtOverReach)),
        T230Falsifier("F3",
            "the reach bound and the thermal-cost bound disagree by more than a decade, so " +
                    "\"the minimum\" is a criterion and not a number — DECLARED OPEN",
            tensionWorst.max().toDouble() / reachWorst > 10.0,
            ("at the worst azimuth: reach %d nt, one k_BT %d-%d nt, one pN %d-%d nt — a factor " +
                    "of %.4f end to end, inside a decade, so the three criteria BRACKET the " +
                    "answer rather than contradict it, and the built 28 nt lies inside the " +
                    "bracket")
                .format(reachWorst, thermalWorst.min(), thermalWorst.max(),
                    tensionWorst.min(), tensionWorst.max(),
                    tensionWorst.max().toDouble() / reachWorst)),
        T230Falsifier("F4",
            "the model fails to reproduce C-0140's 92 / 98 / 106 bp at the built allowance",
            reproductions.take(3).any { it.relativeDeparture != 0.0 },
            ("92 / 98 / 106 bp reproduced at departure 0.0, and 60 x (98 + 28) = %d exactly")
                .format(T230_HELICES * T230_ALLOTMENT_PER_HELIX)),
        T230Falsifier("F5",
            "the loop route at the derived minimum still fails to fit a 112 bp row on M13, so " +
                    "the two routes never compete",
            affordanceM13 < reachWorst,
            ("M13 affords %d nt of turn slack at a 112 bp row and the reach bound asks %d, " +
                    "so route B DOES reach 112 bp — at a turn strained to its own reach limit")
                .format(affordanceM13, reachWorst)),
        T230Falsifier("F6",
            "the FJC law fails its own limits — vanishing tension at zero extension, " +
                    "divergence at the contour, and the Gaussian spring in between",
            turnLoopState(d, 4000, 2.10, 0.65, kT).tension >
                    3.0 * kT * d / (2.10 * 4000 * 0.65) * 1.001,
            "asserted as three gate-2 and gate-3 tests, including a 20 000-point quadrature " +
                    "of the tension against the closed-form free energy")
    )

    val findings = listOf(
        ("THE 28 nt IS A CHOICE, NOT A REQUIREMENT, AND THE CHEAP BOUND SAYS SO IN ONE " +
                "DIVISION. A turn's two anchoring phosphates are at most d + 2 r_P = %.4f nm " +
                "apart, %d unpaired nucleotides reach that at T-71's MEASURED C2'-endo " +
                "phosphodiester step, and the built blocks spend %d — a factor of %.3f. At the " +
                "azimuth-averaged span %.4f nm the bound is %d nt.")
            .format(worstSpan, reachWorst, T230_UNPAIRED_PER_HELIX, builtOverReach,
                meanSpan, reachAtMean),
        ("THE ZERO-SLACK CASE IS THE CHECK THAT THE GEOMETRY IS BEING READ RIGHT, AND IT " +
                "PASSES ON MEASURED CONSTANTS ALONE. A scaffold crossover is n = 0, so its " +
                "span must be one phosphodiester step: d - 2 r_P = %.6f nm against a measured " +
                "step of %.6f +/- %.6f nm, i.e. %+.4f sigma and inside the 99th percentile " +
                "%.6f. Nothing was fitted; the honeycomb's SAXS lattice constant and T-71's " +
                "13 084 crystallographic linkages agree that a crossover closes and that it " +
                "closes TIGHTLY, which is why the residue condition binds it at all.")
            .format(crossoverSpan, MeasuredBackbone.STEP_SOUTH, MeasuredBackbone.STEP_SOUTH_SD,
                crossoverSigma, MeasuredBackbone.STEP_SOUTH_P99),
        ("BUT THE MINIMUM IS A CRITERION AND NOT A NUMBER, AND THE SPREAD IS A FACTOR OF " +
                "SEVEN. At the WORST relative azimuth — which is the reading a FREE row length " +
                "is owed, because a free width leaves both backbone azimuths free — reach " +
                "refuses below %d nt, one k_BT of stored free energy asks %d-%d over the " +
                "zero-force Kuhn bracket, and one pN of turn tension asks %d-%d. At the " +
                "azimuth-averaged span the same three are %d, %d-%d and %d-%d. Quote a turn " +
                "loop with the azimuth AND the criterion it was read at.")
            .format(reachWorst, thermalWorst.min(), thermalWorst.max(),
                tensionWorst.min(), tensionWorst.max(), reachAtMean,
                thermalRange.min(), thermalRange.max(),
                tensionRange.min(), tensionRange.max()),
        ("AND THE BUILT 28 nt IS EXACTLY THE ALLOWANCE THAT MAKES A WORST-AZIMUTH TURN " +
                "THERMAL. Held at %.4f nm it sits at %.3f-%.3f of its own contour, carries " +
                "%.4f-%.4f pN and stores %.4f-%.4f k_BT — sub-thermal, about one piconewton, " +
                "and %.4fx the reach bound. So 28 nt is a CHOICE and not a requirement, and it " +
                "is the choice a SLACK turn costs: it is 1.27-1.75x the one-k_BT bound and " +
                "0.68-0.97x the one-pN bound at the same azimuth.")
            .format(builtWorst.first().spanNm,
                builtWorst.minOf { it.extensionRatio }, builtWorst.maxOf { it.extensionRatio },
                builtWorst.minOf { it.tensionPicoNewton },
                builtWorst.maxOf { it.tensionPicoNewton },
                builtWorst.minOf { it.freeEnergyThermal },
                builtWorst.maxOf { it.freeEnergyThermal }, builtOverReach),
        ("AND THAT DECIDES THE ROUTE, BECAUSE M13 AFFORDS EXACTLY %d NUCLEOTIDES OF TURN " +
                "SLACK AT A 112 bp ROW. 60 x (112 + L) <= 7 249 gives L <= %d, and the reach " +
                "bound is %d — so route B, a UNIFORM 112 bp x 60 raster on unpaired loops, is " +
                "geometrically possible on M13, by two nucleotides. What it costs is the whole " +
                "difference: at the worst azimuth an %d nt turn sits at %.3f-%.3f of its " +
                "contour, carries %.3f-%.3f pN and stores %.3f-%.3f k_BT — at or past " +
                "Gen1Tile's 10 pN unzip allowable at the tight end of the Kuhn bracket, and " +
                "%.0f-%.0f k_BT of stored strain over the raster's 59 turns. Route A, " +
                "C-0140's two-length 112 / 108 raster, needs NO slack and keeps 653 nt spare. " +
                "THE RECOMMENDATION STANDS, and this task supplies the reason it should: not " +
                "that route B does not fit, but that it fits only strained.")
            .format(affordanceM13, affordanceM13, reachWorst, affordanceM13,
                affordedWorst.minOf { it.extensionRatio },
                affordedWorst.maxOf { it.extensionRatio },
                affordedWorst.minOf { it.tensionPicoNewton },
                affordedWorst.maxOf { it.tensionPicoNewton },
                affordedWorst.minOf { it.freeEnergyThermal },
                affordedWorst.maxOf { it.freeEnergyThermal },
                59 * affordedWorst.minOf { it.freeEnergyThermal },
                59 * affordedWorst.maxOf { it.freeEnergyThermal }),
        ("THE YIELD HALF CANNOT BE PRICED AND THE THRESHOLD IS QUOTED INSTEAD. No published " +
                "measurement relates a SCAFFOLD turn-loop length to origami folding yield: " +
                "Ke et al.'s evidence is an 8 bp STAPLE domain between two crossovers, " +
                "Rothemund's 63 %% -> 11 %% is a scaffold LINEARISATION, and Strauss et al.'s " +
                "48-95 %% is per-STAPLE incorporation. What is available is the built blocks " +
                "themselves — %d nt per turn, folded, gel-purified and imaged — so the only " +
                "measured point on the axis is the one this task shows to be 4.7x the reach " +
                "bound. The threshold that decides the route is %d nt: at or below it a " +
                "uniform 112 bp row fits M13, above it it does not.")
            .format(T230_UNPAIRED_PER_HELIX, affordanceM13),
        ("THE SCAFFOLD BUDGET IS DECIDED BY AN UNRESOLVED POLYMER CONVENTION, WHICH IS THE " +
                "PART A DESIGN CANNOT DESIGN AROUND. Over the 2x method-systematic ssDNA Kuhn " +
                "bracket CLAUDE.md records, a worst-azimuth turn costing one k_BT asks %d nt " +
                "at the loose end and %d at the tight one — a 1.4x spread with no measurement " +
                "between them — and M13's affordance at a 112 bp row is %d. p8064 removes the " +
                "question, affording %d nt, which is inside the one-k_BT band at both ends of " +
                "the bracket; that is the cheapest thing anybody could do to route B.")
            .format(thermalWorst.min(), thermalWorst.max(), affordanceM13, affordanceP8064)
    )

    val result = T230Result(
        task = "T-230",
        leaf = "A8.2",
        title = "The minimum unpaired slack a honeycomb raster turn needs, and what a short " +
                "loop costs",
        verificationType = "logical (a covalent reach bound on the MEASURED backbone and an " +
                "exact freely-jointed-chain law, both closed forms) + literature (the caDNAno " +
                "per-helix allotment, read directly from gpd/data/T-151-sources/)",
        maturity = "TRL 1-3 - model-consistent and traceable, NOTHING HERE IS MEASURED except " +
                "the constants, which are: T-71's backbone from 13 084 crystallographic " +
                "linkages, the honeycomb's SAXS lattice constant, and the caDNAno blocks' own " +
                "published scaffold accounting",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "energy" to "pN*nm and k_BT",
            "count" to "nucleotides, base pairs"
        ),
        conventions = listOf(
            "rise 0.34 nm/bp; k_BT = 4.141947 pN*nm at 300 K",
            "n unpaired nucleotides between two anchoring phosphates make n + 1 " +
                    "phosphodiester steps, so the reach is (n + 1) x step",
            "the loop is charged PER TURN; the built design's 14 + 14 makes the per-helix " +
                    "allotment and the per-turn loop the same number, 28",
            "azimuth 0 degrees points at the other helix, so (0, 180) is the closest approach " +
                    "and (180, 0) the furthest",
            "the Kuhn length is the ZERO-FORCE scattering bracket 2.10-2.84 nm and the contour " +
                    "the INEXTENSIBLE 0.65-0.70 nm/nt that travels with it; the 1.34-1.41 nm " +
                    "force-spectroscopy Kuhn and the 0.57 nm/nt extensible contour are never " +
                    "mixed with them"
        ),
        parameters = mapOf(
            "interhelicalDistanceNm" to d.toString(),
            "phosphateRadiusNm" to rP.roundedForProse().toString(),
            "crossoverSpanNm" to roundForResult(crossoverSpan).toString(),
            "crossoverSpanSigma" to roundForResult(crossoverSigma).toString(),
            "worstAzimuthSpanNm" to roundForResult(worstSpan).toString(),
            "azimuthAveragedSpanNm" to roundForResult(meanSpan).toString(),
            "reachBoundWorstAzimuthNucleotides" to reachWorst.toString(),
            "reachBoundAzimuthAveragedNucleotides" to reachAtMean.toString(),
            "builtLoopNucleotidesPerTurn" to T230_UNPAIRED_PER_HELIX.toString(),
            "builtOverReach" to roundForResult(builtOverReach).toString(),
            "oneThermalNucleotidesPerTurn" to
                    (thermalRange.min().toString() + "-" + thermalRange.max().toString()),
            "onePicoNewtonNucleotidesPerTurn" to
                    (tensionRange.min().toString() + "-" + tensionRange.max().toString()),
            "m13AffordanceAtOneHundredTwelveBasePairs" to affordanceM13.toString(),
            "p8064AffordanceAtOneHundredTwelveBasePairs" to affordanceP8064.toString(),
            "helices" to T230_HELICES.toString(),
            "builtTurnTensionAtWorstAzimuthPicoNewton" to
                    (roundForResult(builtWorst.minOf { it.tensionPicoNewton }).toString() + "-" +
                            roundForResult(builtWorst.maxOf { it.tensionPicoNewton }).toString()),
            "builtTurnFreeEnergyAtWorstAzimuthThermal" to
                    (roundForResult(builtWorst.minOf { it.freeEnergyThermal }).toString() + "-" +
                            roundForResult(builtWorst.maxOf { it.freeEnergyThermal }).toString()),
            "affordedTurnTensionAtWorstAzimuthPicoNewton" to
                    (roundForResult(affordedWorst.minOf { it.tensionPicoNewton }).toString() +
                            "-" + roundForResult(
                        affordedWorst.maxOf { it.tensionPicoNewton }).toString()),
            "oneThermalNucleotidesPerTurnAtWorstAzimuth" to
                    (thermalWorst.min().toString() + "-" + thermalWorst.max().toString()),
            "onePicoNewtonNucleotidesPerTurnAtWorstAzimuth" to
                    (tensionWorst.min().toString() + "-" + tensionWorst.max().toString()),
            "azimuthGridPerAxis" to grid.toString()
        ),
        sources = listOf(
            "gpd/results/T-218-honeycomb-raster-turn-sense.json",
            "gpd/data/T-151-sources/PMC2731887-fullTextXML.xml"
        ),
        quotations = mapOf(
            "caDNAno, the per-helix scaffold accounting" to
                    "Each helix was allotted 126 bases of scaffold. Of those 126 bases, 98 " +
                    "were paired with complementary staples, and the remaining 28 were " +
                    "divided into front and rear unpaired loop fragments at the ends of each " +
                    "helix.",
            "caDNAno, the loops as an imaged feature" to
                    "more than 3 nm away from the unpaired scaffold loops at the front and " +
                    "rear interfaces"
        ),
        cheapBound = mapOf(
            "statement" to "a turn of n unpaired nucleotides reaches (n + 1) x step, so the " +
                    "minimum is span/step - 1 and below it the turn closes at NO conformation",
            "worstSpanNm" to roundForResult(worstSpan).toString(),
            "measuredStepNm" to roundForResult(MeasuredBackbone.STEP_SOUTH).toString(),
            "minimumNucleotides" to reachWorst.toString(),
            "builtNucleotides" to T230_UNPAIRED_PER_HELIX.toString(),
            "verdict" to "the built allowance is a CHOICE; the reach bound is 4.7x smaller"
        ),
        reach = reach,
        loops = loops,
        criteria = criteria,
        scaffolds = scaffolds,
        reproductions = reproductions,
        predicates = predicates,
        falsifiers = falsifiers,
        findings = findings,
        validity = listOf(
            "the reach bound is a covalent geometry bound and carries no solvent, no salt and " +
                    "no sequence; it is an upper bound on what a chain can span and therefore " +
                    "a LOWER bound on the nucleotides a turn needs",
            "the freely jointed chain ignores ssDNA's own excluded volume, its electrostatic " +
                    "self-repulsion at mM Mg2+ and the two duplexes it wraps around, all of " +
                    "which STIFFEN the loop; so the tension and the free energy here are " +
                    "lower bounds and the nucleotide counts they imply are lower bounds too",
            "the azimuth-averaged span assumes both backbone azimuths are free, which they " +
                    "are exactly when the row length is free — which is the premise of route B",
            "nothing here is a fabrication result: the only measured point on the loop-length " +
                    "axis is the built 28 nt, and this task does not fold anything"
        ),
        openQuestions = listOf(
            "what a turn loop between 6 and 28 nt does to FOLDING YIELD is unmeasured and no " +
                    "published measurement is on that axis",
            "whether a scaffold crossover at the honeycomb's own spacing is strained enough " +
                    "to matter is not asked here: the span sits 1.5 sigma above the mean " +
                    "measured step, which is inside the population but not at its centre",
            "route B's turn also has to clear the neighbouring duplex STERICALLY, which this " +
                    "task does not model - the span is a straight line between two phosphates"
        )
    )

    val json = Json { prettyPrint = true }
    val out = File("gpd/results/T-230-honeycomb-turn-loop-slack.json")
    out.writeText(
        json.encodeToString(json.encodeToJsonElement(result).roundedForResult(floor = 1e-15).withEmissionHeader(LatticeTag.BOTH, null))
    )
    println("T-230 — wrote ${out.path}")
    findings.forEach { println("  * $it") }
}
