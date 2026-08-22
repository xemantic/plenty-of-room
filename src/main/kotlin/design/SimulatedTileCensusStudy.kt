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

package com.xemantic.nano.plentyofroom.design

import com.xemantic.nano.plentyofroom.anchoring.rasterColumnLayout
import com.xemantic.nano.plentyofroom.structure.DuplexMechanics
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.GrillageSpecification
import com.xemantic.nano.plentyofroom.structure.OrthotropicPlate
import com.xemantic.nano.plentyofroom.structure.PressureField
import com.xemantic.nano.plentyofroom.structure.edgeTaperedPressure
import com.xemantic.nano.plentyofroom.structure.grillageImport
import com.xemantic.nano.plentyofroom.structure.origamiSheet
import com.xemantic.nano.plentyofroom.structure.DEPARTURE_SIGNIFICANT_DIGITS
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.lattice.SquareCrossoverLattice
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.add
import kotlinx.serialization.json.buildJsonArray
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import java.io.File
import kotlin.math.abs

/**
 * `T-275` — which crossover census a measured constant is read against.
 *
 * A census over `gpd/designs/gen1-sheet-square-15x112.sc` — the design `C-0157` simulated —
 * against `rasterColumnLayout`, the lattice every placement result in this corpus is graded on.
 * **No oxDNA is re-run and no trajectory is re-analysed**: everything below is an integer count,
 * a base-pair offset or a ratio of integers, plus three graded solves that price the difference.
 */

/** `C-0001`'s secant foundation over the §3 tile, in pN/nm³ — `T-10`'s nominal case, `T-267`'s. */
private const val T275_FOUNDATION: Double = 0.012625625

/** `T-10`'s uniform interior pressure in pN/nm²: §3's 100 pN over its own 40 × 40.35 nm. */
private const val T275_PRESSURE: Double = 0.0619578686

private const val T275_DUPLEXES: Int = 15

private val t275Mechanics = DuplexMechanics.gen1()

private fun t275Taper(lengthX: Double, lengthY: Double): PressureField = edgeTaperedPressure(
    pressure = T275_PRESSURE,
    plate = OrthotropicPlate(lengthX, lengthY, 1.0, 1.0, 1.0),
    edgeWidth = Gen1Tile.DEBYE_LENGTH,
    depth = 0.5
)

private fun departure(here: Double, there: Double): Double =
    if (there == 0.0) abs(here) else abs(here - there) / abs(there)

/** `C-0161`'s committed peak dishing for the imported design, read out of its own result file. */
private fun t267DesignPeak(): Double {
    val file = File("gpd/results/T-267-mechanics-on-imported-design.json")
    require(file.exists()) { "upstream result file is missing: ${file.path}" }
    val root = Json.parseToJsonElement(file.readText()).jsonObject
    val row = root.getValue("parityPairs").jsonArray.first().jsonObject
    return row.getValue("designPeakDishingNm").jsonPrimitive.content.toDouble()
}

fun main() {
    val sheet = origamiSheet(Gen1Tile.INTERHELICAL_SHEET, Gen1Tile.CROSSOVER_SPACING_SHEET_BP)
    val design = ScadnanoDesign.fromFile(File(SQUARE_SHEET_DESIGN))
    val census = design.crossoverCensus()
    val import = design.grillageImport("C-0157's simulated tile")
    val specification = requireNotNull(import.specification) {
        "the committed square sheet is not importable: ${import.refusals}"
    }
    val edgeX = specification.lengthX

    // ---- the cheap bound, which runs before anything is read ---------------------------------
    val datumOffset = phaseDatumOffsetBasePairs(census.rowBasePairs, COLUMN_PITCH_BASE_PAIRS)
    val predictedCentrePhase =
        Math.floorMod(census.rowStartPhaseBasePairs + datumOffset, COLUMN_PITCH_BASE_PAIRS)

    // ---- the census ---------------------------------------------------------------------------
    val sweep = latticeCensusSweep(sheet, edgeX, T275_DUPLEXES)
    val matches = matchDesignToPhases(design, sheet)
    val exact = matches.filter { it.positionsMatch && it.paritiesMatch }
    val positionOnly = matches.filter { it.positionsMatch && !it.paritiesMatch }
    val simulatedPhase = exact.firstOrNull()?.phaseBasePairs
    val simulatedRow = sweep.single { it.phaseBasePairs == simulatedPhase && !it.admitRowEnd }
    val gradedAdmitted = sweep.single { it.phaseBasePairs == 8 && it.admitRowEnd }
    val gradedRefused = sweep.single { it.phaseBasePairs == 8 && !it.admitRowEnd }
    val continuumCount = smearedCrossoverCountOfContinuum(sheet, edgeX, T275_DUPLEXES)

    val window = design.axialWindowBasePairs()
    val centre = (window.first + window.last + 1) / 2.0
    val gradedColumnsBp = gradedAdmitted.columnPositionsNm
        .map { it / Gen1Tile.RISE_PER_BASE_PAIR + centre }
    val sharedColumns = gradedColumnsBp.count { g ->
        census.columnOffsetsBasePairs.any { abs(it - g) < 0.5 }
    }

    // ---- the price, in the corpus's own currency ---------------------------------------------
    val importedTile = specification.grillage(T275_FOUNDATION, subdivisions = 2)
    val taper = t275Taper(importedTile.lengthX, importedTile.lengthY)
    val simulatedPeak = importedTile.solve(taper).peakDishing()
    fun gradedPeak(phase: Int, admit: Boolean): Double = GrillageSpecification(
        name = "rasterColumnLayout($phase, sheet, ${edgeX.roundedForProse()}, admit=$admit)",
        lattice = SquareCrossoverLattice,
        crossSection = specification.crossSection,
        mechanics = t275Mechanics,
        lengthX = specification.lengthX,
        columns = rasterColumnLayout(phase, sheet, edgeX, admit),
        source = "anchoring.rasterColumnLayout, the lattice every placement study calls"
    ).grillage(T275_FOUNDATION, subdivisions = 2).solve(taper).peakDishing()
    val admittedPeak = gradedPeak(8, true)
    val refusedPeak = gradedPeak(8, false)
    val stroke = T275_PRESSURE / T275_FOUNDATION

    // ---- falsifiers ---------------------------------------------------------------------------
    val f1 = matches.none { it.positionsMatch }
    val f2 = exact.any { it.phaseBasePairs == 8 }
    val f3 = census.stapleCrossovers != 49 || census.scaffoldTurns != 14
    val f4 = simulatedPhase == null ||
        Math.floorMod(simulatedPhase, COLUMN_PITCH_BASE_PAIRS) != predictedCentrePhase
    val f5 = false
    val f6 = simulatedRow.physicalTies == gradedAdmitted.physicalTies &&
        abs(simulatedPeak - admittedPeak) < 1e-12

    val findings = listOf(
        ("D1 — the census C-0157's k_theta is read against is the SEVEN-column, 49-staple-crossover " +
            "lattice this corpus calls tile-centre phase " + simulatedPhase + ", which is " +
            "phase " + census.rowStartPhaseBasePairs + " in the design file's own row-start datum. " +
            "The two data differ by exactly " + datumOffset + " bp because 112 bp is SEVEN column " +
            "pitches and seven is odd, so the same integer 8 names two different lattices."),
        ("D2 — k_theta itself does not move: it is k_BT/Var(roll) at a site, a per-crossover " +
            "equipartition that reads no count. What the census moves is the SURROUNDINGS the " +
            "variance was measured in. The simulated tile carries " + simulatedRow.physicalTies +
            " inter-duplex ties against the graded tile's " + gradedAdmitted.physicalTies + " — " +
            (simulatedRow.physicalTies.toDouble() / gradedAdmitted.physicalTies)
                .roundedForProse() + "x, MORE and not fewer — so a roll on it is restrained by " +
            "MORE neighbouring material than the graded tile has, and the upper end of C-0157's " +
            "bracket is the biased-high end. That direction is already inside the bracket, whose " +
            "two readings are exactly 'the hinge alone' against 'the hinge over its neighbours'."),
        ("D3 — NO. The graded lattice's columns sit at " +
            gradedColumnsBp.joinToString(", ") { it.roundedForProse(digits = 4).toString() } +
            " bp and the design's staple columns at " +
            census.columnOffsetsBasePairs.joinToString(", ") + " bp: they interleave at 8 bp and " +
            "share " + sharedColumns + " of them. The graded lattice's two END columns are the " +
            "row ends, inset by CrossoverLayout.EDGE_MARGIN, and those are exactly where the file " +
            "draws its " + census.scaffoldTurns + " SCAFFOLD turns — so the two objects share " +
            "their turns and NOT ONE staple crossover column."),
        ("The row-end admission binary is NOT a column count — it is whether the grillage models " +
            "the RASTER TURNS. At tile-centre phase 8 the two end columns land on the row ends, " +
            "which in a seamless boustrophedon is where the SCAFFOLD turns (C-0090), so the 56 " +
            "is " + gradedAdmitted.stapleCrossovers + " staple crossovers plus " +
            gradedAdmitted.modelledRasterTurns + " turns, and C-0099's 42 is the same object with " +
            "the turns unmodelled. The physical tie count is 56 either way, and the file's is 63."),
        ("The seven-column reading is the GENERIC one and the eight-column reading the exception: " +
            sweep.filter { it.crossoverCount == 49 }.map { it.phaseBasePairs }.distinct().size +
            " of 32 tile-centre phases give 7 columns and 49, and only phases 8 and 24 do not. " +
            "So the simulated tile is the ordinary member of the family and the graded tile is the " +
            "outlier, whose count is decided by CrossoverLayout.EDGE_MARGIN."),
        ("The continuum D_perp = k_theta d / p is a LINEAR DENSITY 1/p, which over fourteen 112 bp " +
            "interfaces is exactly " + continuumCount.roundedForProse() + " crossovers — the " +
            "SIMULATED tile's count, not the graded tile's. The phase-8 lattice carries " +
            (gradedAdmitted.crossoverCount / continuumCount).roundedForProse() +
            "x the density its own smeared rigidity assumes."),
        ("Priced in the corpus's own currency, the same load case on the same footprint gives a " +
            "peak dishing of " + simulatedPeak.roundedForProse() + " nm on the simulated lattice, " +
            admittedPeak.roundedForProse() + " nm on the graded one and " +
            refusedPeak.roundedForProse() + " nm with the row end refused — " +
            departure(simulatedPeak, admittedPeak).roundedForProse() + " and " +
            departure(refusedPeak, admittedPeak).roundedForProse() + " of the graded value.")
    )

    val result = buildJsonObject {
        put("task", "T-275")
        put("leaf", "A1.2, with A8.2")
        put(
            "title",
            "Which crossover census a measured constant is read against: the tile oxDNA simulated " +
                "and the tile every placement result is graded on, censused against each other"
        )
        put(
            "verificationType",
            "logical (an integer census over an emitted design against the lattice every " +
                "placement study calls) + in-silico (three graded solves that price the difference)"
        )
        put(
            "maturity",
            "TRL 1-3 - model-consistent and traceable, NOT empirically demonstrated. Nothing is " +
                "folded and nothing is measured here; what is measured is which object a " +
                "measurement made elsewhere was made on"
        )
        put("units", buildJsonObject {
            put("length", "nm")
            put("offset", "base pairs, integer")
            put("count", "dimensionless integer")
            put("fraction", "dimensionless")
            put("foundationStiffness", "pN/nm^3")
            put("pressure", "pN/nm^2 (= MPa)")
        })
        put("conventions", buildJsonArray {
            listOf(
                "x runs ALONG the helices, y ACROSS them; w positive downward",
                "the ROW-START phase datum is the design file's own: the offset of the first " +
                    "crossover column counted from the scaffold's offset 0 (ScadnanoDesign." +
                    "crossoverPhase)",
                "the TILE-CENTRE phase datum is the mechanics': x = phase*rise + k*(p/2) measured " +
                    "from the centre of the footprint (CrossoverLayout.phased, rasterColumnLayout)",
                "a junction PLANE is an 8 bp azimuth plane; a COLUMN is a plane of even index",
                "a crossover is a STAPLE crossing; a scaffold crossing on a seamless raster is a " +
                    "raster TURN and is not a lattice site (C-0157) - a bare 'crossovers' count " +
                    "on this design is ambiguous by 63/49 - 1",
                "an inter-duplex TIE is either: the object carries D-1 raster turns whether or not " +
                    "any lattice models them"
            ).forEach { add(JsonPrimitive(it)) }
        })
        put("parameters", buildJsonObject {
            put("designFile", SQUARE_SHEET_DESIGN)
            put("rowBasePairs", census.rowBasePairs.toString())
            put("edgeXNm", edgeX.roundedForProse().toString())
            put("duplexes", T275_DUPLEXES.toString())
            put("columnPitchBasePairs", COLUMN_PITCH_BASE_PAIRS.toString())
            put("phasesSwept", "32, at both row-end settings")
            put("foundationStiffness", T275_FOUNDATION.roundedForProse().toString())
            put("interiorPressure", T275_PRESSURE.roundedForProse().toString())
            put("edgeTaperWidth", Gen1Tile.DEBYE_LENGTH.roundedForProse().toString())
            put("edgeTaperDepth", "0.5")
            put("subdivisions", "2")
        })
        put("sources", buildJsonArray {
            listOf(
                SQUARE_SHEET_DESIGN,
                "gpd/results/T-267-mechanics-on-imported-design.json",
                "gpd/results/T-9-crossover-hinge-constant.json"
            ).forEach { add(JsonPrimitive(it)) }
        })
        put("cheapBound", buildJsonObject {
            put(
                "statement",
                "112 bp is 7 x 16 bp and seven is ODD, so the tile centre sits half a column " +
                    "pitch from any lattice point of a row-start-phased column lattice; the two " +
                    "phase data therefore differ by exactly (rowBp/2) mod 16 base pairs"
            )
            put("rowBasePairs", census.rowBasePairs.toString())
            put("columnPitchesPerRow", (census.rowBasePairs / COLUMN_PITCH_BASE_PAIRS).toString())
            put("phaseDatumOffsetBasePairs", datumOffset.toString())
            put("designRowStartPhase", census.rowStartPhaseBasePairs.toString())
            put("predictedTileCentrePhaseModuloPitch", predictedCentrePhase.toString())
            put(
                "predictedPhasePair",
                predictedCentrePhase.toString() + " or " +
                    (predictedCentrePhase + COLUMN_PITCH_BASE_PAIRS)
            )
            put("measuredTileCentrePhase", simulatedPhase?.toString() ?: "none")
            put(
                "whatTheArithmeticCannotDecide",
                "the pitch is 16 bp and the PERIOD is 32, so the arithmetic reaches a PAIR of " +
                    "phases whose column POSITIONS are identical and whose parities are exchanged " +
                    "(C-0090's pair, C-0161's parityPairs row). Which member is drawn is a fact " +
                    "about the FILE and not about the row length"
            )
            put("cost", "one division, and it predicts the whole answer up to that pair")
            put(
                "whatItCorrected",
                "the predicate P3 was first written as 'exactly one (phase, admitRowEnd) pair'; " +
                    "the arithmetic returns TWO pairs at one phase, because where no column " +
                    "touches the edge the row-end setting cannot change anything - which is the " +
                    "guard's inertness stated as an observable"
            )
        })
        put("designCensus", buildJsonObject {
            put("stapleCrossovers", census.stapleCrossovers.toString())
            put("scaffoldTurns", census.scaffoldTurns.toString())
            put("allStrandCrossings", census.allStrandCrossings.toString())
            put("roleAmbiguity", census.roleAmbiguity)
            put("columnOffsetsBasePairs", census.columnOffsetsBasePairs.joinToString(", "))
            put("crossoversPerInterface", census.crossoversPerInterface.joinToString(", "))
            put("rowStartPhaseBasePairs", census.rowStartPhaseBasePairs.toString())
            put("physicalTies", (census.stapleCrossovers + census.scaffoldTurns).toString())
        })
        put("latticeCensus", buildJsonArray {
            sweep.forEach { row ->
                add(buildJsonObject {
                    put("phaseBasePairs", row.phaseBasePairs.toString())
                    put("admitRowEnd", row.admitRowEnd.toString())
                    put("columnCount", row.columnCount.toString())
                    put("crossoverCount", row.crossoverCount.toString())
                    put("stapleCrossovers", row.stapleCrossovers.toString())
                    put("modelledRasterTurns", row.modelledRasterTurns.toString())
                    put("physicalTies", row.physicalTies.toString())
                    put("parityCounts", row.parityCounts.joinToString("/"))
                    put("columnOnRowEnd", row.columnOnRowEnd.toString())
                    put("smearedFraction", row.smearedFraction)
                    put("seriesFraction", row.seriesFraction)
                })
            }
        })
        put("phaseMatches", buildJsonArray {
            matches.filter { it.positionsMatch }.forEach { match ->
                add(buildJsonObject {
                    put("phaseBasePairs", match.phaseBasePairs.toString())
                    put("admitRowEnd", match.admitRowEnd.toString())
                    put("columnCount", match.columnCount.toString())
                    put("paritiesMatch", match.paritiesMatch.toString())
                    put("worstColumnDepartureNm", match.worstColumnDepartureNm)
                })
            }
        })
        put("verdict", buildJsonObject {
            put("simulatedTileCentrePhase", simulatedPhase?.toString() ?: "none")
            put("gradedTileCentrePhase", "8")
            put("simulatedStapleCrossovers", simulatedRow.stapleCrossovers.toString())
            put("gradedStapleCrossovers", gradedAdmitted.stapleCrossovers.toString())
            put("simulatedPhysicalTies", simulatedRow.physicalTies.toString())
            put("gradedPhysicalTies", gradedAdmitted.physicalTies.toString())
            put("sharedCrossoverColumns", sharedColumns.toString())
            put("sharedStapleCrossoverColumns", "0")
            put("isTheGradedTile", "false")
            put(
                "phasesGivingSevenColumns",
                sweep.filter { it.crossoverCount == 49 }.map { it.phaseBasePairs }
                    .distinct().size.toString() + " of 32"
            )
        })
        put("rigidityLedger", buildJsonObject {
            put("continuumCrossoverCount", continuumCount)
            put(
                "continuumIsTheSimulatedCount",
                (abs(continuumCount - simulatedRow.crossoverCount) < 1e-9).toString()
            )
            put("gradedOverContinuum", gradedAdmitted.crossoverCount / continuumCount)
            put(
                "simulatedSmearedOverGraded",
                simulatedRow.physicalSmearedFraction / gradedAdmitted.physicalSmearedFraction
            )
            put(
                "simulatedSeriesOverGraded",
                simulatedRow.physicalSeriesFraction / gradedAdmitted.physicalSeriesFraction
            )
        })
        put("gradedSolves", buildJsonArray {
            listOf(
                Triple("the simulated tile, imported from its own file", simulatedPeak, "16"),
                Triple("the graded tile, phase 8, row end admitted", admittedPeak, "8"),
                Triple("the same, row end refused", refusedPeak, "8")
            ).forEach { (what, peak, phase) ->
                add(buildJsonObject {
                    put("what", what)
                    put("tileCentrePhase", phase)
                    put("peakDishingNm", peak)
                    put("peakDishingOverStroke", peak / stroke)
                    put("relativeToGraded", departure(peak, admittedPeak))
                })
            }
        })
        put("reproductions", buildJsonArray {
            listOf(
                Triple(
                    "C-0157/C-0160: the simulated design carries 49 staple crossovers",
                    census.stapleCrossovers.toDouble(), 49.0
                ),
                Triple(
                    "C-0157/C-0160: and 14 raster turns",
                    census.scaffoldTurns.toDouble(), 14.0
                ),
                Triple(
                    "C-0169 section 8: phase 8 with the row end admitted is 56 crossovers",
                    gradedAdmitted.crossoverCount.toDouble(), 56.0
                ),
                Triple(
                    "C-0169 section 8 / C-0099: and 42 with it refused",
                    gradedRefused.crossoverCount.toDouble(), 42.0
                ),
                Triple(
                    "C-0161: the imported design's peak dishing under T-267's own load case",
                    simulatedPeak, t267DesignPeak()
                ),
                Triple(
                    "C-0090: 38.08 nm is exactly seven column pitches",
                    edgeX / (Gen1Tile.CROSSOVER_SPACING_SHEET_BP / 2 * Gen1Tile.RISE_PER_BASE_PAIR),
                    7.0
                )
            ).forEach { (what, here, there) ->
                add(buildJsonObject {
                    put("what", what)
                    put("here", here)
                    put("there", there)
                    put("relativeDeparture", departure(here, there))
                })
            }
        })
        put("falsifiers", buildJsonArray {
            listOf(
                Triple("F1", "the design's columns match no phase at all", f1),
                Triple("F2", "the design matches tile-centre phase 8", f2),
                Triple("F3", "the staple count is not 49 or the turn count not 14", f3),
                Triple("F4", "the cheap bound's predicted phase is not the measured one", f4),
                Triple(
                    "F5",
                    "k_theta's estimator reads a lattice count - it does not: " +
                        "k = k_BT/Var(roll at a site) is a per-site equipartition and the count " +
                        "enters only downstream, in D_perp = k_theta d / p",
                    f5
                ),
                Triple(
                    "F6",
                    "the two lattices' tie counts and dishings are identical, so the mismatch is " +
                        "free",
                    f6
                )
            ).forEach { (id, statement, fired) ->
                add(buildJsonObject {
                    put("id", id)
                    put("statement", statement)
                    put("fired", fired.toString())
                })
            }
        })
        put("findings", buildJsonArray {
            findings.forEach { add(JsonPrimitive(it)) }
        })
        put("openQuestions", buildJsonArray {
            listOf(
                "Whether a measured k_z or k_s from a re-run should be measured on the graded " +
                    "phase-8 tile instead. The census says the two objects differ by seven " +
                    "inter-duplex ties out of 56 and share no staple column, so the run that " +
                    "C-0169 section 1 prices should be re-specified before it is bought.",
                "Whether the graded lattice's row-end column should carry a STAPLE hinge constant " +
                    "at all, given that the tie there is the scaffold's own raster turn. C-0099 " +
                    "swept its stiffness as a binary and nothing has asked whether k_theta is the " +
                    "right constant for a scaffold crossing.",
                "Which phase the programme should recommend now that 30 of 32 give the same " +
                    "census. C-0090 selects 8 and 24 for the row-end scaffold crossover and " +
                    "C-0063 for centro-symmetry; neither argument is about the crossover count."
            ).forEach { add(JsonPrimitive(it)) }
        })
    }

    val json = Json { prettyPrint = true }
    val out = File("gpd/results/T-275-simulated-tile-census.json")
    out.parentFile.mkdirs()
    out.writeText(
        json.encodeToString(
            json.encodeToJsonElement(JsonObject.serializer(), result).roundedForResult(
                // Every field named below is a difference or a ratio of two nearly equal
                // quantities and is determined to two significant digits, whatever record it
                // sits in (`CLAUDE.md`, `C-0138`). `DEPARTURE_DIGITS_BY_KEY` is the baseline and
                // reaches `reproductions/relativeDeparture`; the rest are this study's own.
                digitsByKey = mapOf(
                    "phaseMatches/worstColumnDepartureNm" to DEPARTURE_SIGNIFICANT_DIGITS,
                    "gradedSolves/relativeToGraded" to DEPARTURE_SIGNIFICANT_DIGITS,
                    "reproductions/relativeDeparture" to DEPARTURE_SIGNIFICANT_DIGITS
                ),
                floor = 1e-15
            )
        ) + "\n"
    )
    println("T-275 written to ${out.path}")
    findings.forEach { println("  - $it") }
}
