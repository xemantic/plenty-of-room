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

package com.xemantic.nano.plentyofroom.tile

import com.xemantic.nano.plentyofroom.anchoring.MeasuredBackbone
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.ResultInputs
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs

// ---------------------------------------------------------------------------------------------
// T-246 -- what a FORCED scaffold crossover costs.
//
// CH-0188 shows that C-0140's recommended 112 / 108 bp raster does not close on caDNAno's own
// +-5 bp scaffold rule and needs 10 of 59 raster crossovers FORCED. The challenge's severity
// rests entirely on that being bad, and it says so: "this repository has no way to price it".
//
// The cheap bound decides the task. A forced crossover's departure is an AZIMUTH, and because
// one turn is 10.5 bp the smallest one the lattice offers is HALF a base-pair step. That span
// then lands outside T-71's MEASURED phosphodiester step, so the crossover does not close as a
// bond at rigid geometry and the price is elastic -- for which the rigid-duplex limit of
// C-0104's own roll mapping is a strict CEILING, needing no solve at all.
// ---------------------------------------------------------------------------------------------

private const val T246_PERIOD: Int = 21

/** The two conventions the ±5 bp rule can be read under, in degrees of azimuth. */
private const val T246_CADNANO_BASELINE: Double = 0.0
private const val T246_EXACT_BASELINE: Double = 0.25 * AZIMUTH_PER_BASE_PAIR

@Serializable
private class T246Rung(
    val residueDepartureBasePairs: Int,
    val azimuthalDepartureDegrees: Double,
    val spanNm: Double,
    val sigmaOfMeasuredStep: Double,
    val spanOverP99: Double,
    val closesAsABond: Boolean
)

@Serializable
private class T246Closure(
    val senseOneBasePairs: Int,
    val senseTwoBasePairs: Int,
    val crossSection: String,
    val firstAxialSign: Int,
    val mirrored: Boolean,
    val axialReversed: Boolean,
    val rasterCrossovers: Int,
    val forcedCrossovers: Int,
    val classZeroResidue: Int,
    val worstAzimuthalDepartureDegrees: Double,
    val distinctResidueDepartures: List<Int>
)

/**
 * The span an **allowed** scaffold crossover carries under each reading of the `±5 bp` rule.
 *
 * caDNAno writes *"five base pairs, or half a turn"*, but the exact half turn at 10.5 bp per turn
 * is **5.25** bp — so under the exact geometry an allowed scaffold crossover is already `0.25 bp`
 * off the line of centres, on either side, and the aligned reading is the idealisation's.
 */
@Serializable
private class T246AllowedReading(
    val convention: String,
    val azimuthDegrees: Double,
    val spanNm: Double,
    val sigmaOfMeasuredStep: Double,
    val insideP99: Boolean
)

@Serializable
private class T246Approach(
    val azimuthalDepartureDegrees: Double,
    val targetStepNm: Double,
    val targetName: String,
    val smallestReachableSpanNm: Double,
    val reachableAtAnySeparation: Boolean,
    val interhelicalDistanceNm: Double,
    val approachNm: Double,
    val insideStericFloor: Boolean
)

@Serializable
private class T246Ceiling(
    val baselineConvention: String,
    val baselineDegrees: Double,
    val departureDegrees: Double,
    val crossoverAlpha: Double,
    val hingeStiffness: Double,
    val channel: String,
    val crossoverSpacingNm: Double,
    val effectiveStiffness: Double,
    val energyPnNm: Double,
    val energyThermalUnits: Double,
    val forcedCrossovers: Int,
    val blockEnergyThermalUnits: Double,
    val blockOverHostSheetColumn: Double
)

/**
 * A published **twist-density** rung, converted onto this study's own azimuthal-departure axis.
 *
 * Ke et al. underwind a honeycomb 24-helix bundle by inserting base pairs into its 21 bp
 * crossover period, so `n` base pairs of insertion per interface is `n` base pairs of azimuthal
 * register that the duplex must absorb — the same axis a forced crossover sits on, reached from
 * the other side and, uniquely in this literature, with a folding outcome attached.
 */
@Serializable
private class T246TwistRung(
    val basePairsPerTurn: Double,
    val basePairsPerInterface: Int,
    val insertedBasePairs: Int,
    val azimuthalDepartureDegrees: Double,
    val overForcedCrossover: Double,
    val outcome: String
)

@Serializable
private class T246Threshold(
    val axis: String,
    val quantity: String,
    val ceilingHere: String,
    val valueThatWouldChangeTheAnswer: String,
    val factor: String,
    val licensed: Boolean,
    val note: String
)

@Serializable
private class T246Source(
    val what: String,
    val citation: String,
    val provenance: String,
    val quotation: String
)

@Serializable
private class T246Query(val endpoint: String, val queryString: String, val hits: Int)

@Serializable
private class T246Reproduction(
    val what: String,
    val published: String,
    val here: String,
    val relativeDeparture: String,
    val reproduced: Boolean
)

@Serializable
private class T246Convergence(
    val quantity: String,
    val axis: String,
    val relativeDeparture: String,
    val note: String
)

@Serializable
private class T246Falsifier(
    val name: String,
    val statement: String,
    val fired: Boolean,
    val note: String
)

@Serializable
private class T246Result(
    val task: String,
    val leaf: String,
    val units: Map<String, String>,
    val conventions: Map<String, String>,
    val parameters: Map<String, String>,
    val sources: List<String>,
    val citedInputs: List<String>,
    val cheapBound: Map<String, String>,
    val allowedCrossoverReadings: List<T246AllowedReading>,
    val azimuthLadder: List<T246Rung>,
    val closure: List<T246Closure>,
    val approach: List<T246Approach>,
    val ceiling: List<T246Ceiling>,
    val thresholds: List<T246Threshold>,
    val publishedTwistLadder: List<T246TwistRung>,
    val literature: List<T246Source>,
    val literatureQueries: List<T246Query>,
    val literatureVerdict: Map<String, String>,
    val reproductions: List<T246Reproduction>,
    val convergence: List<T246Convergence>,
    val falsifiers: List<T246Falsifier>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val proseFailure: String
)

private fun t246Field(file: File, section: String, index: Int, key: String): Double {
    require(file.exists()) { "upstream result file is missing: " + file.path }
    return Json.parseToJsonElement(file.readText())
        .jsonObject.getValue(section).jsonArray[index].jsonObject
        .getValue(key).jsonPrimitive.content.toDouble()
}

private fun departureOf(value: Double, reference: Double): Double =
    if (reference == 0.0) abs(value) else abs(value - reference) / abs(reference)

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val d = Gen1Tile.INTERHELICAL_HONEYCOMB
    val rP = MeasuredBackbone.B_SOUTH_POPULATION_PHOSPHATE_RADIUS
    val stericFloor = 2.0 * rP
    val kT = 4.141947
    val minimalDeparture = abs(azimuthalDepartureDegrees(10))

    println("T-246 - what a FORCED scaffold crossover costs")
    println("  CHEAP BOUND, before any elastic model:")

    // ------------------------------------------- Deliverable 1: the azimuth ladder, and F2/F3
    val ladder = (0 until T246_PERIOD).map { k ->
        val azimuth = azimuthalDepartureDegrees(k)
        val span = forcedCrossoverSpan(d, rP, azimuth)
        T246Rung(
            residueDepartureBasePairs = k,
            azimuthalDepartureDegrees = azimuth,
            spanNm = span,
            sigmaOfMeasuredStep =
                (span - MeasuredBackbone.STEP_SOUTH) / MeasuredBackbone.STEP_SOUTH_SD,
            spanOverP99 = span / MeasuredBackbone.STEP_SOUTH_P99,
            closesAsABond = span <= MeasuredBackbone.STEP_SOUTH_P99
        )
    }
    val minimalRung = ladder.filter { it.residueDepartureBasePairs > 0 }
        .minByOrNull { abs(it.azimuthalDepartureDegrees) }!!
    println(
        "    one bp is " + AZIMUTH_PER_BASE_PAIR.roundedForProse() + " deg, and 21 bp is 720 deg" +
                " exactly, so the SMALLEST nonzero departure is " +
                minimalDeparture.roundedForProse() + " deg at " +
                minimalRung.residueDepartureBasePairs + " bp - HALF a base-pair step"
    )
    println(
        "    its span is " + minimalRung.spanNm.roundedForProse() + " nm, " +
                minimalRung.sigmaOfMeasuredStep.roundedForProse(3) +
                " sigma of the MEASURED phosphodiester step: it does NOT close as a bond"
    )

    // ---- the ALLOWED crossover under both readings of the rule: the lattice's own calibration
    val allowedReadings = listOf(
        ("caDNAno's idealisation - '5 bp, OR HALF A TURN', read as an aligned crossover"
                to T246_CADNANO_BASELINE),
        ("exact 10.5 bp per turn - the half turn is 5.25 bp, so +-5 is 0.25 bp off it"
                to T246_EXACT_BASELINE)
    ).map { (name, azimuth) ->
        val span = forcedCrossoverSpan(d, rP, azimuth)
        T246AllowedReading(
            convention = name,
            azimuthDegrees = azimuth,
            spanNm = span,
            sigmaOfMeasuredStep =
                (span - MeasuredBackbone.STEP_SOUTH) / MeasuredBackbone.STEP_SOUTH_SD,
            insideP99 = span <= MeasuredBackbone.STEP_SOUTH_P99
        )
    }
    println(
        "    an ALLOWED crossover reads " +
                allowedReadings[0].sigmaOfMeasuredStep.roundedForProse(3) + " sigma aligned and " +
                allowedReadings[1].sigmaOfMeasuredStep.roundedForProse(3) +
                " sigma at exact geometry - inside P99 " + allowedReadings[0].insideP99 +
                " and " + allowedReadings[1].insideP99
    )

    // ------------------------------------------------- Deliverable 2: the raster's own census
    val pairs = listOf(112 to 108, 101 to 109, 102 to 109, 112 to 109, 122 to 119)
    val sections = listOf("10 x 6" to (10 to 6), "15 x 4" to (15 to 4))
    val closure = pairs.flatMap { (a, b) ->
        sections.flatMap { (name, shape) ->
            listOf(1, -1).flatMap { sign ->
                listOf(false to false, true to true).map { (mirror, reversed) ->
                    val residues = HoneycombRasterResidues(
                        rasterRows = shape.first, helicesPerRow = shape.second,
                        senseOneBasePairs = a, senseTwoBasePairs = b,
                        firstAxialSign = sign, mirrored = mirror, axialReversed = reversed
                    )
                    val census = forcedCrossoverCensus(residues)
                    T246Closure(
                        senseOneBasePairs = a, senseTwoBasePairs = b, crossSection = name,
                        firstAxialSign = sign, mirrored = mirror, axialReversed = reversed,
                        rasterCrossovers = census.rasterCrossovers,
                        forcedCrossovers = census.forcedCrossovers,
                        classZeroResidue = census.classZeroResidue,
                        worstAzimuthalDepartureDegrees = census.worstAzimuthalDepartureDegrees,
                        distinctResidueDepartures =
                            census.residueDeparturesBasePairs.distinct().sorted()
                    )
                }
            }
        }
    }
    val recommended = closure.filter {
        it.senseOneBasePairs == 112 && it.senseTwoBasePairs == 108 && it.crossSection == "10 x 6"
    }
    val forcedOnRecommended = recommended.first().forcedCrossovers
    val worstOnRecommended = recommended.maxOf { it.worstAzimuthalDepartureDegrees }
    println(
        "    the 112 / 108 raster forces " + forcedOnRecommended + " of " +
                recommended.first().rasterCrossovers + " on the 10 x 6 block, every one of them" +
                " at " + worstOnRecommended.roundedForProse() + " deg"
    )

    // ------------------------------- Deliverable 3: can the AXES pay? a root, not an assertion
    val approachTargets = listOf(
        "the measured C2'-endo mean" to MeasuredBackbone.STEP_SOUTH,
        "its 99th percentile" to MeasuredBackbone.STEP_SOUTH_P99,
        "the aligned crossover's own span" to (d - stericFloor)
    )
    val approach = listOf(minimalDeparture, AZIMUTH_PER_BASE_PAIR).flatMap { azimuth ->
        approachTargets.map { (name, target) ->
            val root = interhelicalDistanceClosingSpanOrNull(target, rP, azimuth)
            T246Approach(
                azimuthalDepartureDegrees = azimuth,
                targetStepNm = target,
                targetName = name,
                smallestReachableSpanNm = smallestReachableSpan(rP, azimuth),
                reachableAtAnySeparation = root != null,
                interhelicalDistanceNm = root ?: -1.0,
                approachNm = if (root == null) -1.0 else d - root,
                insideStericFloor = root != null && root < stericFloor
            )
        }
    }
    val reachableApproach = approach.filter { it.reachableAtAnySeparation }
    val approachCeilingDegrees = Math.toDegrees(
        kotlin.math.asin(MeasuredBackbone.STEP_SOUTH / (2.0 * rP))
    )

    // -------------------------------------- Deliverable 4: the elastic ceiling, in one product
    val spacings = listOf(
        "every azimuth, 7 bp" to Gen1Tile.RISE_PER_BASE_PAIR * 7.0,
        "one pair, 21 bp" to Gen1Tile.RISE_PER_BASE_PAIR * 21.0
    )
    val baselines = listOf(
        "caDNAno's own idealisation - 5 bp IS a half turn, so an allowed crossover is aligned"
                to T246_CADNANO_BASELINE,
        "exact 10.5 bp per turn - a half turn is 5.25 bp, so an allowed crossover already carries"
                to T246_EXACT_BASELINE
    )
    val alphas = listOf(
        Gen1Tile.CROSSOVER_ALPHA_MIN, 1.0, Gen1Tile.CROSSOVER_ALPHA_MAX
    )
    val ceiling = baselines.flatMap { (baseName, baseline) ->
        val departure = minimalDeparture + baseline
        alphas.flatMap { alpha ->
            val hinge = Gen1Tile.crossoverHingeStiffness(alpha)
            val rigid = T246Ceiling(
                baselineConvention = baseName, baselineDegrees = baseline,
                departureDegrees = departure, crossoverAlpha = alpha, hingeStiffness = hinge,
                channel = "rigid duplex - the crossover hinge carries all of it (the CEILING)",
                crossoverSpacingNm = 0.0, effectiveStiffness = hinge,
                energyPnNm = forcedCrossoverEnergy(hinge, departure, baseline),
                energyThermalUnits = forcedCrossoverEnergy(hinge, departure, baseline) / kT,
                forcedCrossovers = forcedOnRecommended,
                blockEnergyThermalUnits =
                    forcedOnRecommended * forcedCrossoverEnergy(hinge, departure, baseline) / kT,
                blockOverHostSheetColumn =
                    forcedOnRecommended * forcedCrossoverEnergy(hinge, departure, baseline) /
                            kT / HOST_SHEET_COLUMN_ENERGY_KT
            )
            listOf(rigid) + spacings.map { (spacingName, p) ->
                val relieved = twistRelievedHingeStiffness(
                    hinge, Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY, p
                )
                val energy = forcedCrossoverEnergy(relieved, departure, baseline)
                T246Ceiling(
                    baselineConvention = baseName, baselineDegrees = baseline,
                    departureDegrees = departure, crossoverAlpha = alpha, hingeStiffness = hinge,
                    channel = "duplex torsion relieves the hinge over lambda - " + spacingName,
                    crossoverSpacingNm = p, effectiveStiffness = relieved,
                    energyPnNm = energy, energyThermalUnits = energy / kT,
                    forcedCrossovers = forcedOnRecommended,
                    blockEnergyThermalUnits = forcedOnRecommended * energy / kT,
                    blockOverHostSheetColumn =
                        forcedOnRecommended * energy / kT / HOST_SHEET_COLUMN_ENERGY_KT
                )
            }
        }
    }
    val worstCeiling = ceiling.maxByOrNull { it.energyThermalUnits }!!
    println(
        "    the ELASTIC CEILING is " + worstCeiling.energyThermalUnits.roundedForProse() +
                " kT per forced crossover, so " +
                worstCeiling.blockEnergyThermalUnits.roundedForProse() +
                " kT for all " + forcedOnRecommended + " - SUB-THERMAL, and " +
                worstCeiling.blockOverHostSheetColumn.roundedForProse() +
                " of ONE crossover column of the host sheet"
    )

    // ----------------------------------------------- Deliverable 5: the thresholds, per P-6
    val perCrossoverCeiling = worstCeiling.energyThermalUnits
    val toOneColumn = HOST_SHEET_COLUMN_ENERGY_KT / worstCeiling.blockEnergyThermalUnits
    val toThermal = 1.0 / perCrossoverCeiling
    val flatnessThresholdDegrees = 15.4497275
    val nextRungBlockKt = forcedOnRecommended * forcedCrossoverEnergy(
        Gen1Tile.crossoverHingeStiffness(Gen1Tile.CROSSOVER_ALPHA_MAX),
        AZIMUTH_PER_BASE_PAIR + T246_EXACT_BASELINE, T246_EXACT_BASELINE
    ) / kT
    val thresholds = listOf(
        T246Threshold(
            axis = "elastic energy, per forced crossover",
            quantity = "the price of one forced crossover in kT",
            ceilingHere = perCrossoverCeiling.roundedForProse().toString() + " kT",
            valueThatWouldChangeTheAnswer = "1 kT - the fold's own currency",
            factor = toThermal.roundedForProse().toString() + "x the ceiling",
            licensed = true,
            note = "The ceiling is sub-thermal, so a forced crossover of the MINIMAL departure " +
                    "this lattice offers cannot be argued out of a fold on elastic grounds."
        ),
        T246Threshold(
            axis = "elastic energy, whole block",
            quantity = "the price of all " + forcedOnRecommended + " forced crossovers in kT",
            ceilingHere =
                worstCeiling.blockEnergyThermalUnits.roundedForProse().toString() + " kT",
            valueThatWouldChangeTheAnswer =
                HOST_SHEET_COLUMN_ENERGY_KT.roundedForProse().toString() +
                        " kT - what ONE crossover column of the host sheet already pays (C-0079)",
            factor = toOneColumn.roundedForProse().toString() + "x the ceiling",
            licensed = true,
            note = "The whole block's forcing costs a fraction of what one crossover of the " +
                    "DEMONSTRATED square-lattice sheet pays to hold its own two duplexes."
        ),
        T246Threshold(
            axis = "crossover hinge stiffness",
            quantity = "k_theta in pN nm/rad",
            ceilingHere = Gen1Tile.crossoverHingeStiffness(Gen1Tile.CROSSOVER_ALPHA_MAX)
                .roundedForProse().toString(),
            valueThatWouldChangeTheAnswer =
                (Gen1Tile.crossoverHingeStiffness(Gen1Tile.CROSSOVER_ALPHA_MAX) * toOneColumn)
                    .roundedForProse().toString() +
                        " - where the block would cost one host-sheet column",
            factor = toOneColumn.roundedForProse().toString() + "x",
            licensed = true,
            note = "k_theta is the one measured-ish input in the ceiling; the answer needs it " +
                    "wrong by this factor, well outside Gen1Tile's own 0.6-1.2 alpha bracket."
        ),
        T246Threshold(
            axis = "azimuthal departure - and the lattice QUANTISES it",
            quantity = "the departure a forced crossover carries, in degrees",
            ceilingHere = minimalDeparture.roundedForProse().toString() +
                    " deg, the lattice's smallest nonzero rung",
            valueThatWouldChangeTheAnswer =
                (minimalDeparture * kotlin.math.sqrt(toOneColumn)).roundedForProse().toString() +
                        " deg - which falls BETWEEN two rungs, so the first rung that would " +
                        "change the answer is the NEXT one, " +
                        AZIMUTH_PER_BASE_PAIR.roundedForProse().toString() +
                        " deg (a 1 bp displacement), where the block costs " +
                        nextRungBlockKt.roundedForProse().toString() + " kT",
            factor = kotlin.math.sqrt(toOneColumn).roundedForProse().toString() +
                    "x is demanded and the lattice's next rung supplies 2x, i.e. " +
                    (nextRungBlockKt / HOST_SHEET_COLUMN_ENERGY_KT).roundedForProse().toString() +
                    "x of a host-sheet column",
            licensed = true,
            note = "The energy is quadratic and the departure axis is discrete, so the threshold " +
                    "is bracketed rather than crossed: the departure this raster actually needs " +
                    "is a factor of two below the first rung that would exceed the calibration. " +
                    "The 112 / 108 raster's stray residue is one bp from an allowed residue and " +
                    "ELEVEN from the other, and it is the eleven that the azimuth prefers."
        ),
        T246Threshold(
            axis = "PUBLISHED FOLDING OUTCOME - the one measurement on this axis",
            quantity = "azimuthal register departure per crossover interface, in degrees",
            ceilingHere = minimalDeparture.roundedForProse().toString() +
                    " deg, at 10 of 59 SCAFFOLD crossovers",
            valueThatWouldChangeTheAnswer = "34.2857143 deg at EVERY interface is the smallest " +
                    "departure Ke et al. tested, and it folds - ambiguously, improving a 60hb " +
                    "and degrading a 24hb; 102.857143 deg at every interface abolishes folding",
            factor = "the forced crossover is HALF the smallest rung anyone has measured, and " +
                    "it is applied to a sixth of the interfaces rather than to all of them",
            licensed = false,
            note = "NOT a transfer. Ke et al. change the twist of the WHOLE lattice by inserting " +
                    "base pairs, so every interface carries the departure and the duplex relieves " +
                    "it by actually adopting the non-canonical twist; here the departure is LOCAL " +
                    "and unrelieved. What the ladder licenses is a scale, not a verdict - and the " +
                    "scale says the forced crossover is below the bottom rung of the only " +
                    "published ladder."
        ),
        T246Threshold(
            axis = "FLATNESS - the prestrain-as-a-LOAD channel",
            quantity = "the uniform crossover prestrain a tile tolerates, in degrees",
            ceilingHere = minimalDeparture.roundedForProse().toString() +
                    " deg is what a forced crossover imposes",
            valueThatWouldChangeTheAnswer = flatnessThresholdDegrees.roundedForProse().toString() +
                    " deg - C-0104's threshold on the SINGLE-LAYER SQUARE-LATTICE 40 nm tile",
            factor = (minimalDeparture / flatnessThresholdDegrees).roundedForProse().toString() +
                    "x, i.e. the departure EXCEEDS that threshold",
            licensed = false,
            note = "NOT LICENSED and that is the finding. C-0104's number is a UNIFORM prestrain " +
                    "on ALL 56 crossovers of a single-layer SQUARE-lattice tile at one placement; " +
                    "here it is 10 of 59 SCAFFOLD crossovers on a four-layer HONEYCOMB block. " +
                    "CLAUDE.md records that OrigamiGrillage never reads layers and that " +
                    "CrossoverLayout's two-parity alternation makes the lattice machinery " +
                    "square-lattice, so this repository cannot evaluate the channel on a " +
                    "honeycomb face at all. The number is quoted to say WHICH measurement is " +
                    "owed, never to transfer a verdict."
        )
    )

    // ------------- Deliverable 6: the ONE published measurement on this axis, converted onto it
    val twistLadder = listOf(
        Triple(10.5, 0, "the design twist - the control"),
        Triple(
            11.0, 1,
            "'underwinding 24hb to 11.0 bp/turn appeared to decrease the efficiency of folding' " +
                    "- AND the SIGN REVERSES between architectures of the same laboratory: " +
                    "'the previously reported 60hb folded better at 11.0 bp/turn'"
        ),
        Triple(
            11.5, 2,
            "'folding performance on par with 24hb underwound to 11.0 bp/turn'"
        ),
        Triple(
            12.0, 3,
            "'underwinding 24hb to 12.0 bp/turn abolished productive folding completely' - and " +
                    "the authors name the mechanism: 'the penalty from torsional strain energy'"
        )
    ).map { (perTurn, inserted, outcome) ->
        val azimuth = abs(azimuthalDepartureDegrees(inserted))
        T246TwistRung(
            basePairsPerTurn = perTurn,
            basePairsPerInterface = T246_PERIOD + inserted,
            insertedBasePairs = inserted,
            azimuthalDepartureDegrees = azimuth,
            overForcedCrossover = azimuth / minimalDeparture,
            outcome = outcome
        )
    }

    // ------------------------------------------------------- Deliverable 7: the literature
    val literature = listOf(
        T246Source(
            what = "what caDNAno permits, and its only stated warning",
            citation = "Douglas et al., Nucleic Acids Res. 37:5001 (2009), PMC2731887",
            provenance = "read directly (gpd/data/T-151-sources/PMC2731887-fullTextXML.xml)",
            quotation = "However, caDNAno permits the user to force crossovers between any two " +
                    "staple bases or between any two scaffold bases. Users should take care when " +
                    "forcing crossovers, as departure from the default rules may lead to folding " +
                    "failure if too much deviation from canonical DNA geometry is implied."
        ),
        T246Source(
            what = "the SOURCE'S OWN statement that the quantity is unpredicted - the strongest " +
                    "evidence here, and it is an explicit exclusion rather than a null search",
            citation = "Douglas et al., Nucleic Acids Res. 37:5001 (2009), PMC2731887, Discussion",
            provenance = "read directly (gpd/data/T-151-sources/PMC2731887-fullTextXML.xml)",
            quotation = "caDNAno provides tools to introduce deviations from the basic honeycomb " +
                    "architecture, such as forced crossovers, to create very complicated " +
                    "designs. Additional software development will be required to make designs " +
                    "of these non-standard motifs more natural, for example for caDNAno to " +
                    "predict the structural consequences of these changes. More work is also " +
                    "needed to see what design rules lead to stable structures; for examples of " +
                    "designs that folded successfully, although with varying yields, see the " +
                    "gallery section at http://cadnano.org/."
        ),
        T246Source(
            what = "the ONLY published measurement on the same AXIS - a systematic twist-density " +
                    "sweep of a honeycomb bundle, whose rungs convert to azimuthal register " +
                    "departures per crossover interface",
            citation = "Ke, Bellot, Voigt, Fradkov & Shih, Chem. Sci. 3:2587 (2012), PMC3957201",
            provenance = "read directly (gpd/data/T-246-sources/PMC3957201.txt)",
            quotation = "Whereas the previously reported 60hb folded better at 11.0 bp/turn, " +
                    "here underwinding 24hb to 11.0 bp/turn appeared to decrease the efficiency " +
                    "of folding ... However, underwinding 24hb to 12.0 bp/turn abolished " +
                    "productive folding completely ... We hypothesized that for >= 12.0 bp/turn " +
                    "underwinding, the penalty from torsional strain energy was greater than the " +
                    "energetic gain from increased helical bowing and decreased electrostatic " +
                    "repulsion."
        ),
        T246Source(
            what = "the nearest analogue this repository already cites - and the citation is " +
                    "OVER-READ: the yield observation is published, the ATTRIBUTION is not",
            citation = "Ke et al., J. Am. Chem. Soc. 131:15903 (2009), PMC2821935",
            provenance = "read directly (gpd/data/T-246-sources/PMC2821935.txt)",
            quotation = "We observed significantly lower yield for these structures. Introducing " +
                    "these breaks may be destabilizing for the structure. Alternatively, simply " +
                    "having a large number of layers with our default crossover pattern may be " +
                    "destabilizing, irrespective of the position of the breaks. ... Future " +
                    "systematic studies will be required to determine the relative importance of " +
                    "these staple breaks toward affecting folding efficiency."
        ),
        T246Source(
            what = "caDNAno's FORCING TOOL is demonstrated in a folded, high-yield published " +
                    "structure - but for a LATTICE change, not for an off-register position",
            citation = "Ke, Voigt, Shih et al., J. Am. Chem. Soc. 134:1770 (2012), PMC3336742",
            provenance = "read directly (gpd/data/T-246-sources/PMC3336742.txt)",
            quotation = "Currently, all crossovers need to be manually implemented in caDNAno " +
                    "for hexagonal-lattice or hybrid origami. ... high yields, as indicated by " +
                    "agarose-gel electrophoresis and TEM analysis. [No percentage is stated.]"
        ),
        T246Source(
            what = "the yield measurement of a ROUTING change this repository cites - and its " +
                    "own author hedges the cause away from the routing",
            citation = "Rothemund, Nature 440:297 (2006)",
            provenance = "read directly (gpd/data/T-151-sources/DNAorigami-nature.txt)",
            quotation = "The low yield of stars ... may be due to strand breakage occurring " +
                    "during BsrBI digestion or subsequent steps to remove the enzyme. [So the " +
                    "63 % to 11 % is the cost of enzymatic linearisation, hedged by its author, " +
                    "and not a crossover-position measurement at all.]"
        ),
        T246Source(
            what = "the calibration the ceiling is read against",
            citation = "C-0079 / gpd/results/T-139-duplex-pair-separation.json, calibration[4]",
            provenance = "read directly (this repository's own emitted result file)",
            quotation = "7.99969697 kT per crossover column - what one crossover of the host " +
                    "sheet demonstrably pays to hold its own two duplexes at the SAXS 2.69 nm, " +
                    "and the sheet folds."
        )
    )

    // --------------------------------------------------------------- reproductions and gates
    val publishedColumn = t246Field(
        ResultInputs.T_139.file(), "calibration", 4, "value"
    )
    val alignedSpan = forcedCrossoverSpan(d, rP, 0.0)
    val opposedSpan = forcedCrossoverSpan(d, rP, 180.0)
    val reproductions = listOf(
        T246Reproduction(
            what = "CH-0188 - forced crossovers on the 112 / 108 raster, 10 x 6",
            published = "10 of 59", here = forcedOnRecommended.toString() + " of " +
                    recommended.first().rasterCrossovers,
            relativeDeparture = "0.0",
            reproduced = forcedOnRecommended == 10 && recommended.first().rasterCrossovers == 59
        ),
        T246Reproduction(
            what = "CH-0188 - forced crossovers on the 112 / 108 raster, 15 x 4",
            published = "8 of 59",
            here = closure.first {
                it.senseOneBasePairs == 112 && it.senseTwoBasePairs == 108 &&
                        it.crossSection == "15 x 4"
            }.forcedCrossovers.toString() + " of 59",
            relativeDeparture = "0.0",
            reproduced = closure.first {
                it.senseOneBasePairs == 112 && it.senseTwoBasePairs == 108 &&
                        it.crossSection == "15 x 4"
            }.forcedCrossovers == 8
        ),
        T246Reproduction(
            what = "CH-0188 - 102 / 109 closes at zero forced crossovers",
            published = "0",
            here = closure.filter { it.senseOneBasePairs == 102 }
                .map { it.forcedCrossovers }.distinct().joinToString(),
            relativeDeparture = "0.0",
            reproduced = closure.filter { it.senseOneBasePairs == 102 }
                .all { it.forcedCrossovers == 0 }
        ),
        T246Reproduction(
            what = "C-0147 - the aligned crossover span d - 2 r_P",
            published = "0.718724283",
            here = alignedSpan.roundedForProse().toString(),
            relativeDeparture = departureOf(alignedSpan, d - stericFloor)
                .roundedForProse(2, 0.0).toString(),
            reproduced = departureOf(alignedSpan, d - stericFloor) < 1e-12
        ),
        T246Reproduction(
            what = "C-0147 - the opposed span d + 2 r_P",
            published = "4.35327572",
            here = opposedSpan.roundedForProse().toString(),
            relativeDeparture = departureOf(opposedSpan, d + stericFloor)
                .roundedForProse(2, 0.0).toString(),
            reproduced = departureOf(opposedSpan, d + stericFloor) < 1e-12
        ),
        T246Reproduction(
            what = "C-0079 - the host sheet's own cost per crossover column",
            published = publishedColumn.toString(),
            here = HOST_SHEET_COLUMN_ENERGY_KT.toString(),
            relativeDeparture = departureOf(HOST_SHEET_COLUMN_ENERGY_KT, publishedColumn)
                .roundedForProse(2, 0.0).toString(),
            reproduced = departureOf(HOST_SHEET_COLUMN_ENERGY_KT, publishedColumn) < 1e-9
        )
    )

    val rootRoundTrip = reachableApproach.maxOf {
        departureOf(
            forcedCrossoverSpan(it.interhelicalDistanceNm, rP, it.azimuthalDepartureDegrees),
            it.targetStepNm
        )
    }
    val convergence = listOf(
        T246Convergence(
            quantity = "the azimuth ladder, the span, the census and the elastic ceiling",
            axis = "none - every one is a closed form in exact integer or elementary arithmetic",
            relativeDeparture = "0.0",
            note = "There is no mesh, no timestep, no sampling and no root-finder in any of them, " +
                    "so a convergence axis would be a measurement of the floating-point unit."
        ),
        T246Convergence(
            quantity = "the interhelical approach that would close a forced crossover",
            axis = "the quadratic root, asserted back through the span it was solved from",
            relativeDeparture = rootRoundTrip.roundedForProse(2, 0.0).toString(),
            note = "The one inversion in this study, and it is closed form; the round trip is " +
                    "the whole of its convergence."
        )
    )

    val falsifiers = listOf(
        T246Falsifier(
            name = "F1",
            statement = "the span identity reproduces C-0147's d - 2 r_P and d + 2 r_P",
            fired = !(departureOf(alignedSpan, d - stericFloor) < 1e-12 &&
                    departureOf(opposedSpan, d + stericFloor) < 1e-12),
            note = "Both endpoints reproduce to the last ulp, so the geometry is being read the " +
                    "same way the standing turn-slack claim reads it and no new convention enters."
        ),
        T246Falsifier(
            name = "F2",
            statement = "17.142857 deg is the SMALLEST nonzero azimuthal departure the " +
                    "21-residue lattice offers",
            fired = (1 until T246_PERIOD).any {
                abs(azimuthalDepartureDegrees(it)) < minimalDeparture - 1e-12
            },
            note = "The minimum is reached at 10 and at 11 bp and nowhere else, because one turn " +
                    "is 10.5 bp - so the cheapest forcing is the one displaced FURTHEST in base " +
                    "pairs, which no count of base pairs can see."
        ),
        T246Falsifier(
            name = "F3",
            statement = "a forced crossover does NOT close as a bond at rigid ideal geometry - " +
                    "written the favourable way round, so its not firing is the finding",
            fired = minimalRung.spanNm <= MeasuredBackbone.STEP_SOUTH_P99,
            note = "The minimal forced span is " + minimalRung.spanNm.roundedForProse() +
                    " nm against a 99th percentile of " +
                    MeasuredBackbone.STEP_SOUTH_P99.roundedForProse() +
                    " nm, so there IS something to price."
        ),
        T246Falsifier(
            name = "F4",
            statement = "the elastic ceiling for all " + forcedOnRecommended +
                    " forced crossovers is below the host sheet's own cost per crossover column",
            fired = worstCeiling.blockEnergyThermalUnits >= HOST_SHEET_COLUMN_ENERGY_KT,
            note = "The block's whole forcing costs " +
                    worstCeiling.blockOverHostSheetColumn.roundedForProse() +
                    " of what ONE crossover of the demonstrated sheet already pays."
        ),
        T246Falsifier(
            name = "F5",
            statement = "no published yield or stability cost for a FORCED crossover exists - " +
                    "one paper falsifies it, which would be the better outcome",
            fired = false,
            note = "See literatureVerdict. The source that DEFINES the operation states in its " +
                    "own discussion that its structural consequences are not predicted."
        )
    )

    // ---------------------------------------------------------------- BUILD AND WRITE FIRST
    var prose = "not attempted"
    var findings: Map<String, String> = mapOf("status" to "prose not yet built")
    var literatureVerdict: Map<String, String> = mapOf("status" to "not yet built")
    var literatureQueries: List<T246Query> = emptyList()

    fun assemble(): T246Result = T246Result(
        task = "T-246",
        leaf = "A8.2",
        units = mapOf(
            "length" to "nm", "angle" to "degrees at the API, radians inside",
            "energy" to "pN nm, and kT with k_BT = 4.141947 pN nm at 300 K",
            "rotationalStiffness" to "pN nm per rad",
            "torsionalRigidity" to "pN nm^2"
        ),
        conventions = mapOf(
            "azimuth" to "0 deg points at the other helix, so (0, 180) is closest approach; a " +
                    "departure rotates BOTH backbones by the same angle in the same sense, " +
                    "because the two helices are parallel and same-handed",
            "departureSign" to "folded to (-180, +180]; the span is even in it, so only the " +
                    "magnitude is ever priced",
            "residues" to "mod 21, non-negative, on C-0140's global axial datum",
            "baseline" to "the departure an ALLOWED crossover already carries is SUBTRACTED, " +
                    "never added - both readings of the +-5 bp rule are carried",
            "ceiling" to "the structure minimises over deformation channels, so ANY admissible " +
                    "channel's cost is an upper bound on the true price"
        ),
        parameters = mapOf(
            "interhelicalDistanceHoneycomb" to d.toString(),
            "phosphateRadiusMeasured" to rP.roundedForProse().toString(),
            "stericFloor" to stericFloor.roundedForProse().toString(),
            "risePerBasePair" to Gen1Tile.RISE_PER_BASE_PAIR.toString(),
            "basePairsPerTurn" to "10.5 (caDNAno honeycomb)",
            "azimuthPerBasePair" to AZIMUTH_PER_BASE_PAIR.roundedForProse().toString(),
            "residuePeriod" to T246_PERIOD.toString(),
            "scaffoldOffsetBasePairs" to HoneycombCrossoverRule.SCAFFOLD_OFFSET_BP.toString(),
            "measuredStepSouth" to MeasuredBackbone.STEP_SOUTH.roundedForProse().toString(),
            "measuredStepSouthSd" to MeasuredBackbone.STEP_SOUTH_SD.roundedForProse().toString(),
            "measuredStepSouthP99" to MeasuredBackbone.STEP_SOUTH_P99.roundedForProse().toString(),
            "duplexTorsionalRigidity" to Gen1Tile.DUPLEX_TORSIONAL_RIGIDITY.toString(),
            "crossoverAlphaBracket" to (Gen1Tile.CROSSOVER_ALPHA_MIN.toString() + " to " +
                    Gen1Tile.CROSSOVER_ALPHA_MAX.toString()),
            "thermalEnergy" to kT.toString(),
            "hostSheetColumnEnergyKt" to HOST_SHEET_COLUMN_ENERGY_KT.toString(),
            "temperature" to "300 K, aqueous 2 mM MgCl2 (the buffer the calibration is read at)"
        ),
        sources = listOf(
            "gpd/results/T-139-duplex-pair-separation.json",
            "gpd/data/T-151-sources/PMC2731887-fullTextXML.xml",
            "gpd/data/T-246-sources/"
        ),
        citedInputs = listOf(
            "C-0148 / CH-0188 - the residue-lattice closure test and its 10-of-59 count, " +
                    "reproduced here before anything new is read off it",
            "C-0140 - the honeycomb x-raster path, its turn senses and its level walk",
            "C-0147 - turnPhosphateSpan and the n = 0 scaffold-crossover check, consumed unmodified",
            "C-0104 / T-182 - the mapping from an azimuthal register error to a crossover ROLL, " +
                    "and the 15.4497275 deg flatness threshold quoted as a NOT-LICENSED marker",
            "C-0079 - the host sheet's own 7.99969697 kT per crossover column, read from its file",
            "T-71 - the 13 084-linkage measured phosphodiester step and phosphate radius"
        ),
        cheapBound = mapOf(
            "whatItSaid" to ("The departure a forced crossover implies is an AZIMUTH, and " +
                    "because one turn is 10.5 bp the smallest one the 21-residue lattice offers " +
                    "is HALF a base-pair step - 17.142857 deg, at a displacement of TEN or " +
                    "ELEVEN base pairs, not one. That span is 0.96232 nm against a MEASURED " +
                    "phosphodiester step whose 99th percentile is 0.756745 nm, so a forced " +
                    "crossover does not close as a covalent bond at rigid geometry and its " +
                    "price is elastic. The rigid-duplex limit of C-0104's own roll mapping is " +
                    "then a strict CEILING on that price, and it is one multiplication."),
            "cost" to "closed form throughout; no solve, no mesh, no sampling, no minimiser",
            "minimalAzimuthalDepartureDegrees" to minimalDeparture.roundedForProse().toString(),
            "achievedAtBasePairs" to "10 and 11",
            "minimalForcedSpanNm" to minimalRung.spanNm.roundedForProse().toString(),
            "sigmaOfMeasuredStep" to
                    minimalRung.sigmaOfMeasuredStep.roundedForProse().toString(),
            "elasticCeilingPerCrossoverKt" to perCrossoverCeiling.roundedForProse().toString(),
            "elasticCeilingWholeBlockKt" to
                    worstCeiling.blockEnergyThermalUnits.roundedForProse().toString()
        ),
        allowedCrossoverReadings = allowedReadings,
        azimuthLadder = ladder,
        closure = closure,
        approach = approach,
        ceiling = ceiling,
        thresholds = thresholds,
        publishedTwistLadder = twistLadder,
        literature = literature,
        literatureQueries = literatureQueries,
        literatureVerdict = literatureVerdict,
        reproductions = reproductions,
        convergence = convergence,
        falsifiers = falsifiers,
        findings = findings,
        validity = listOf(
            "TRL 1-3. Nothing here is measured on a folded object. The geometry is a LATTICE " +
                    "statement and the price is an ELASTIC model; what is measured is the " +
                    "phosphodiester step, the phosphate radius and the host sheet's own pair " +
                    "interaction, all consumed from this repository's own measured inputs.",
            "AN ELASTIC ENERGY IS NOT A FOLDING YIELD. Folding is kinetic and cooperative; a " +
                    "sub-thermal strain says a forced crossover cannot be argued out of a fold " +
                    "on ELASTIC grounds and says nothing about a kinetic trap. That is exactly " +
                    "why the literature branch is run and why its null result is recorded.",
            "The ceiling is a ceiling WITHIN C-0104's mapping of an azimuthal register error " +
                    "onto a crossover roll at k_theta. A mechanism outside that mapping - base " +
                    "pair unstacking, backbone strain, a local melt - is not bounded by it.",
            "k_theta is Gen1Tile's SQUARE-LATTICE crossover hinge, swept over its own 0.6-1.2 " +
                    "alpha bracket. No honeycomb measurement of it exists here.",
            "The half turn is 5.25 bp at 10.5 bp per turn and caDNAno writes 5. Both readings of " +
                    "that rounding are carried, and they differ by EXACTLY a factor of two in " +
                    "the energy, because (3x)^2 - x^2 = 8x^2 against (2x)^2 = 4x^2.",
            "The FLATNESS channel is NOT evaluated. CLAUDE.md records that OrigamiGrillage never " +
                    "reads layers or interlayerCoupling and that CrossoverLayout's two-parity " +
                    "alternation makes the crossover combinatorics square-lattice, so a " +
                    "honeycomb prestrain solve does not exist in this repository. The threshold " +
                    "is quoted to say which measurement is owed, never to transfer a verdict.",
            "Every raster crossover of an x-raster sits at a row TURN, which is the block's " +
                    "axial rim rather than its gap-facing face. That is an observation, not a " +
                    "result: C-0147 proved the coefficient is zero for the RAGGEDNESS, and " +
                    "nothing here proves it for a prestrain."
        ),
        openQuestions = listOf(
            "What does a four-layer HONEYCOMB tile do under a prestrain on 10 of 59 of its " +
                    "SCAFFOLD crossovers? The lattice machinery is single-layer square-lattice, " +
                    "so the question needs a honeycomb grillage before it can be asked.",
            "Is a forced crossover a KINETIC cost rather than an elastic one? Nothing in this " +
                    "repository can see a folding pathway, and the elastic ceiling explicitly " +
                    "does not bound one.",
            "Does the forced crossover's position at the raster TURN - the block's axial rim - " +
                    "give it the zero coefficient on flatness that C-0147 proved for the " +
                    "raggedness? Not established here.",
            "caDNAno's gallery is named by its own paper as the record of what folded and with " +
                    "what yield. Whether any gallery design carries a forced crossover, and " +
                    "with what yield, is a data question this repository has not closed."
        ),
        proseFailure = prose
    )

    val output = File("gpd/results/T-246-forced-scaffold-crossover-price.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    fun write(result: T246Result) {
        output.writeText(
            json.encodeToString(
                JsonObject.serializer(),
                (json.encodeToJsonElement(result).roundedForResult(digits = 9).withEmissionHeader(LatticeTag.BOTH, null) as JsonObject)
            ) + "\n"
        )
    }
    write(assemble())
    println("T-246 - wrote " + output.path + " (numbers first, prose next)")

    // ---------------------------------------------------- prose LAST, and guarded, per CLAUDE.md
    try {
        literatureQueries = readQueryLog()
        literatureVerdict = mapOf(
            "question" to "Is there ANY published measurement - folding yield, thermal " +
                    "stability, defect rate, structural quality or simulated strain energy - " +
                    "attributable to a FORCED (off-rule) crossover in DNA origami?",
            "verdict" to "NOT FOUND. No published price for a forced crossover was located.",
            "whatWasFoundInstead" to "Every crossover variable anyone has swept moves the WHOLE " +
                    "lattice or the WHOLE staple set - spacing, density, count, type, " +
                    "staple-break position, twist density. NOTHING isolates a single junction. " +
                    "The closest is Ke et al.'s twist-density ladder (publishedTwistLadder), " +
                    "whose bottom rung is TWICE this study's departure and folds.",
            "twoCorrectionsToThisRepository" to "Ke et al. 2009 is OVER-READ in CLAUDE.md as 'a " +
                    "published YIELD cost' of an 8 bp staple break: the yield observation is " +
                    "published, and the same paragraph offers an alternative cause 'irrespective " +
                    "of the position of the breaks' and defers the attribution to 'future " +
                    "systematic studies'. And Rothemund's 63 % to 11 % is hedged by its own " +
                    "author onto BsrBI digestion, not onto the routing. Both are raised as " +
                    "challenges.",
            "strongestEvidence" to "It is an EXPLICIT EXCLUSION rather than a null search: the " +
                    "paper that DEFINES the operation states in its own discussion that " +
                    "'additional software development will be required ... for caDNAno to " +
                    "predict the structural consequences of these changes' and that 'more work " +
                    "is also needed to see what design rules lead to stable structures'.",
            "queriesRun" to "68 in 7 declared families - 54 EuropePMC (8 s spacing, 5 retries), " +
                    "10 arXiv, 4 OpenAlex, 8 direct fetches; 14 candidate papers examined, 13 " +
                    "READ DIRECTLY, 1 abstract only, 0 not found. The full log with every query " +
                    "verbatim is gpd/data/T-246-sources/queries.md",
            "queriesRecordedInThisField" to (literatureQueries.size.toString() +
                    " - the MACHINE-READABLE subset, recovered from the retained JSON responses " +
                    "by this study rather than transcribed; the arXiv (XML) and direct-fetch " +
                    "queries carry no such field and are in queries.md only"),
            "rawResponses" to "gpd/data/T-246-sources/ - every search response retained so the " +
                    "negative is auditable and falsifiable by one paper",
            "whatWouldFalsifyIt" to "One published measurement of yield, melting temperature, " +
                    "defect rate or simulated strain for a design differing from a control only " +
                    "in a forced crossover."
        )
        findings = mapOf(
            "theCheapBoundDecidedIt" to ("The departure is an AZIMUTH and one turn is 10.5 bp, " +
                    "so the smallest nonzero departure the 21-residue lattice offers is HALF a " +
                    "base-pair step - " + minimalDeparture.roundedForProse() + " deg at TEN or " +
                    "ELEVEN base pairs of displacement, not the " +
                    AZIMUTH_PER_BASE_PAIR.roundedForProse() + " deg one base pair costs. The " +
                    "cheapest forcing is the one displaced FURTHEST in base pairs, and no count " +
                    "of base pairs can see that."),
            "itDoesNotCloseAsABond" to ("At rigid ideal geometry the minimal forced crossover " +
                    "spans " + minimalRung.spanNm.roundedForProse() + " nm, which is " +
                    minimalRung.sigmaOfMeasuredStep.roundedForProse(3) + " sigma of T-71's " +
                    "MEASURED C2'-endo phosphodiester step and " +
                    minimalRung.spanOverP99.roundedForProse() + " times its 99th percentile - " +
                    "against an ALLOWED crossover's " + alignedSpan.roundedForProse() +
                    " nm, which C-0147 already showed sits INSIDE it at +1.5 sigma. So a forced " +
                    "crossover is not free and there is something to price."),
            "theLATTICEsOwnCALIBRATION" to ("An ALLOWED scaffold crossover is aligned only to " +
                    "caDNAno's own idealisation. The exact half turn at 10.5 bp per turn is 5.25 " +
                    "bp, so the +-5 bp rule places every allowed scaffold crossover " +
                    T246_EXACT_BASELINE.roundedForProse() + " deg off the line of centres - " +
                    "spanning " + allowedReadings[1].spanNm.roundedForProse() + " nm at " +
                    allowedReadings[1].sigmaOfMeasuredStep.roundedForProse(3) + " sigma, which " +
                    "is OUTSIDE the measured 99th percentile, against the aligned reading's " +
                    allowedReadings[0].sigmaOfMeasuredStep.roundedForProse(3) + " sigma inside " +
                    "it. So the rigid model is already at its limit for a crossover that " +
                    "demonstrably folds, and the structure absorbs " +
                    T246_EXACT_BASELINE.roundedForProse() + " deg at EVERY scaffold crossover of " +
                    "every honeycomb origami ever built. A forced crossover adds exactly TWICE " +
                    "that. This is a calibration the lattice supplies for free, and it needs no " +
                    "measurement that does not already exist."),
            "theAxesCannotPayItAlone" to ("The span is minimised over the interhelical distance " +
                    "at 2 r_P cos theta, which is inside the steric floor, so the approach that " +
                    "would close the bond without any twist is " +
                    approach.first().interhelicalDistanceNm.roundedForProse() + " nm - a " +
                    approach.first().approachNm.roundedForProse() + " nm approach on a " +
                    d.roundedForProse() + " nm lattice constant, " +
                    (100.0 * approach.first().approachNm / d).roundedForProse(3) +
                    " % of it. It is geometrically available and it is not what the structure " +
                    "will choose; either way it can only LOWER the ceiling."),
            "theCeiling" to ("Any admissible deformation channel's cost is an upper bound on the " +
                    "true price, because the structure minimises over channels. On C-0104's own " +
                    "mapping of an azimuthal register error to a crossover ROLL, the " +
                    "rigid-duplex limit is half k_theta theta squared and it is " +
                    perCrossoverCeiling.roundedForProse() + " kT per forced crossover at the " +
                    "STIFFEST end of Gen1Tile's alpha bracket and the more expensive of the two " +
                    "baseline conventions. That is SUB-THERMAL - " + toThermal.roundedForProse() +
                    " times below one k_BT - and all " + forcedOnRecommended + " of the 112 / " +
                    "108 raster's forced crossovers together cost " +
                    worstCeiling.blockEnergyThermalUnits.roundedForProse() + " kT, which is " +
                    worstCeiling.blockOverHostSheetColumn.roundedForProse() + " of what ONE " +
                    "crossover column of the DEMONSTRATED host sheet already pays to hold its " +
                    "own two duplexes (C-0079), and origami folds."),
            "theTwoBaselineReadingsDifferByExactlyTwo" to ("caDNAno's +-5 bp is half a turn only " +
                    "to its own idealisation - the exact half turn is 5.25 bp - so an ALLOWED " +
                    "scaffold crossover already sits " + T246_EXACT_BASELINE.roundedForProse() +
                    " deg off the line of centres and a forced one sits at three times that. " +
                    "The excess is then 8 x squared against the idealised reading's 4 x " +
                    "squared: EXACTLY a factor of two, at every stiffness and every alpha. The " +
                    "expensive reading is the one quoted."),
            "whatIsStillNotPriced" to ("An elastic energy is not a folding yield. The ceiling " +
                    "says a forced crossover cannot be argued out of a fold on ELASTIC grounds; " +
                    "it says nothing about a kinetic trap, and no published measurement of one " +
                    "was found. What the FLATNESS channel would say cannot be computed here at " +
                    "all: the departure, " + minimalDeparture.roundedForProse() + " deg, is " +
                    (minimalDeparture / flatnessThresholdDegrees).roundedForProse() +
                    " times C-0104's " + flatnessThresholdDegrees.roundedForProse() +
                    " deg threshold, and that threshold is a UNIFORM prestrain on ALL 56 " +
                    "crossovers of a SINGLE-LAYER SQUARE-LATTICE tile. It does not transfer, " +
                    "and this repository's lattice machinery cannot run the honeycomb case."),
            "thePublishedLadderTheAnswerSitsUnDER" to ("No published price for a forced " +
                    "crossover exists - 68 queries in 7 families, every raw response retained. " +
                    "What exists is one systematic sweep on the same AXIS: Ke et al. underwind a " +
                    "honeycomb 24-helix bundle by inserting base pairs into its 21 bp crossover " +
                    "period, so each rung is a whole number of base pairs of azimuthal register " +
                    "per interface. One base pair (" +
                    AZIMUTH_PER_BASE_PAIR.roundedForProse() + " deg) folds - and AMBIGUOUSLY, " +
                    "improving a 60hb while degrading a 24hb; three base pairs (102.857143 deg) " +
                    "abolish folding, which the authors attribute to 'the penalty from torsional " +
                    "strain energy'. A forced crossover here is " +
                    minimalDeparture.roundedForProse() + " deg - HALF the bottom rung, and at 10 " +
                    "of 59 interfaces rather than all of them. It is not a transfer, because " +
                    "their departure is global and relieved by an actual change of twist and " +
                    "this one is local and unrelieved; what it licenses is a SCALE."),
            "whatItMeansForCH0188" to ("CH-0188's geometric verdict is untouched: the 112 / 108 " +
                    "raster does not close, and 102 / 109 does. What changes is the SEVERITY. " +
                    "On the one axis this repository can price, forcing ten crossovers is " +
                    "sub-thermal and cheap against the fold's own demonstrated currency - so " +
                    "closure is a REASON to prefer 102 / 109 and not a proof that 112 / 108 is " +
                    "unbuildable. The unpriced risk is kinetic and flatness-side, and both are " +
                    "now named rather than assumed.")
        )
        prose = "none"
    } catch (e: Exception) {
        prose = e.toString()
        write(assemble())
        println("T-246 - PROSE FAILED, recorded in the result file: " + e)
        throw e
    }
    write(assemble())

    println("  ceiling  " + perCrossoverCeiling.roundedForProse() + " kT per forced crossover")
    println("  block    " + worstCeiling.blockEnergyThermalUnits.roundedForProse() + " kT for " +
            forcedOnRecommended + ", " + worstCeiling.blockOverHostSheetColumn.roundedForProse() +
            " of one host-sheet crossover column")
    println("  queries  " + literatureQueries.size + " recorded")
    falsifiers.forEach { println("  " + it.name + " fired=" + it.fired) }
    println("T-246 - rewrote " + output.path)
}

/** Every search this task's literature sweep ran, read back out of its own retained responses. */
private fun readQueryLog(): List<T246Query> {
    val dir = File("gpd/data/T-246-sources")
    if (!dir.isDirectory) return emptyList()
    return dir.listFiles()!!.filter { it.name.endsWith(".json") }.sortedBy { it.name }
        .mapNotNull { file ->
            val root = runCatching { Json.parseToJsonElement(file.readText()).jsonObject }
                .getOrNull() ?: return@mapNotNull null
            val request = root["request"]?.jsonObject
            val query = request?.get("queryString")?.jsonPrimitive?.content
                ?: root["query"]?.jsonPrimitive?.content
                ?: return@mapNotNull null
            val hits = root["hitCount"]?.jsonPrimitive?.content?.toIntOrNull()
                ?: root["meta"]?.jsonObject?.get("count")?.jsonPrimitive?.content?.toIntOrNull()
                ?: -1
            T246Query(
                endpoint = if (file.name.startsWith("openalex")) "OpenAlex" else "EuropePMC",
                queryString = query,
                hits = hits
            )
        }
}
