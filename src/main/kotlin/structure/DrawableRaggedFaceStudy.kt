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

import com.xemantic.nano.plentyofroom.coupling.winklerBendingLength
import com.xemantic.nano.plentyofroom.electrostatics.MagnesiumChlorideBuffer
import com.xemantic.nano.plentyofroom.electrostatics.transverseDecayRateBound
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.tile.HoneycombCrossSectionGeometry
import com.xemantic.nano.plentyofroom.tile.LayerCoupling
import com.xemantic.nano.plentyofroom.tile.multiLayerRigidities
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File

/**
 * `T-258` — the ragged face at the relief the DRAWABLE raster carries.
 *
 * Emits `gpd/results/T-258-drawable-ragged-face.json`. Reads
 * `gpd/results/T-231-ragged-face-cost.json` (`C-0147`'s own published bound, reproduced here),
 * `gpd/results/T-232-coupled-cells-at-the-honeycomb-cross-section.json` (the comparand `C-0147`
 * used) and `gpd/results/T-263-honeycomb-grillage-regrade.json` (`C-0167`'s re-grade, which
 * withdrew it).
 */

private const val T258_FREE_EDGE_PENALTY: Double = 50.0
private const val T258_TOLERANCE: Double = 0.10
private const val T258_COMPOSITE_FRACTION: Double = 0.30

private fun Double.emitted(digits: Int = 9): String = roundedForProse(digits).toString()

@Serializable
private class T258ReliefRow(
    val raster: String,
    val crossSection: String,
    val senseOneRowLength: Int,
    val senseTwoRowLength: Int,
    val staggerBasePairs: Int,
    val frontSpreadBasePairs: Int,
    val frontSpreadNm: Double,
    val frontSpreadRises: Int,
    val rearSpreadBasePairs: Int,
    val rearSpreadNm: Double,
    val axialExtentBasePairs: Int,
    val axialExtentNm: Double,
    val scaffoldNucleotides: Int,
    val gapFacingRimPeriodRows: Int,
    val modulationWavelengthNm: Double,
    val frontSpreadByColumn: List<Int>,
    val everyColumnIsRagged: Boolean,
    val drawable: Boolean
)

@Serializable
private class T258BoundRow(
    val raster: String,
    val crossSection: String,
    val layers: Int,
    val compositeFraction: Double,
    val acrossHelixRigidity: Double,
    val bendingLengthAcrossNm: Double,
    val modulationWavelengthNm: Double,
    val rippleTransmission: Double,
    val freeEdgePenalty: Double,
    val halfSpanNm: Double,
    val reliefNm: Double,
    val rimLeverPerturbation: Double,
    val boundedDishingMove: Double,
    val overTheFourBasePairReading: Double
)

@Serializable
private class T258ThresholdRow(
    val comparand: String,
    val model: String,
    val state: String,
    val dishingOverStroke: Double,
    val headroomToTolerance: Double,
    val relativeHeadroom: Double,
    val worstBound: Double,
    val marginOverBound: Double,
    val withdrawn: Boolean,
    val note: String
)

@Serializable
private class T258EdgeRow(
    val concentrationMillimolar: Double,
    val gapNm: Double,
    val debyeLengthNm: Double,
    val transverseDecayLengthNm: Double,
    val reliefNm: Double,
    val reliefOverDecayLength: Double,
    val resolvable: Boolean,
    val marginAtFourBasePairs: Double
)

@Serializable
private class T258Reproduction(
    val source: String,
    val quantity: String,
    val published: String,
    val here: String,
    val departure: Double,
    val reproduced: Boolean
)

@Serializable
private class T258Falsifier(
    val id: String,
    val statement: String,
    val declaredOpen: Boolean,
    val fired: Boolean,
    val evidence: String
)

@Serializable
private class T258Result(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: Map<String, String>,
    val parameters: Map<String, String>,
    val sources: List<String>,
    val citedInputs: Map<String, String>,
    val axis: Map<String, String>,
    val relief: List<T258ReliefRow>,
    val bound: List<T258BoundRow>,
    val thresholds: List<T258ThresholdRow>,
    val edgeField: List<T258EdgeRow>,
    val reproductions: List<T258Reproduction>,
    val falsifiers: List<T258Falsifier>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val proseFailure: String
)

private fun t258Published(file: File, cross: String): Pair<Double, Double> {
    val flatness = Json.parseToJsonElement(file.readText()).jsonObject
        .getValue("flatness").jsonArray.map { it.jsonObject }
        .first { it.getValue("crossSection").jsonPrimitive.content == cross }
    return flatness.getValue("boundedDishingMove").jsonPrimitive.content.toDouble() to
            flatness.getValue("bendingLengthAcross").jsonPrimitive.content.toDouble()
}

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val rise = Gen1Tile.RISE_PER_BASE_PAIR
    val d = Gen1Tile.INTERHELICAL_HONEYCOMB
    val rowPitch = HoneycombCrossSectionGeometry.rowPitch(d)
    val columnPitch = HoneycombCrossSectionGeometry.columnPitch(d)
    val designs = listOf(Triple("15 x 4", 15, 4), Triple("10 x 6", 10, 6))
    val rasters = listOf(
        Triple("102 / 109 (drawable)", 102 to 109, true),
        Triple("112 / 108 (C-0140, does not close)", 112 to 108, false)
    )

    // ------------------------------------------------ Deliverable 1: the axis, before anything
    println("T-258 - THE CHEAP BOUND, re-taken rather than inherited: WHICH AXIS?")
    println("  a four-layer block's gap-facing surface is ONE COLUMN of the cross-section")
    println("  a row length changes where a helix ENDS, an x coordinate IN the tile plane")
    println("  so the ragged faces are the RIM at EVERY column, gap-facing or buried,")
    println("  and the coefficient on Sec 3's normal-direction flatness is EXACTLY ZERO")
    println("  - a statement carrying no magnitude, so 1.75x of relief cannot move it")

    // ------------------------------------------------ Deliverable 2: the relief, re-derived
    val reliefRows = rasters.flatMap { (name, lengths, drawable) ->
        designs.map { (cross, rows, perRow) ->
            val relief = raggedFaceRelief(rows, perRow, lengths.first, lengths.second)
            T258ReliefRow(
                raster = name,
                crossSection = cross,
                senseOneRowLength = lengths.first,
                senseTwoRowLength = lengths.second,
                staggerBasePairs = Math.abs(lengths.first - lengths.second),
                frontSpreadBasePairs = relief.frontBasePairs,
                frontSpreadNm = relief.frontNm,
                frontSpreadRises = relief.frontBasePairs,
                rearSpreadBasePairs = relief.rearBasePairs,
                rearSpreadNm = relief.rearNm,
                axialExtentBasePairs = relief.axialExtentBasePairs,
                axialExtentNm = relief.axialExtentBasePairs * rise,
                scaffoldNucleotides = relief.scaffoldNucleotides,
                gapFacingRimPeriodRows = relief.gapFacingRimPeriodRows,
                modulationWavelengthNm = relief.modulationWavelengthNm,
                frontSpreadByColumn = relief.spreadByColumn,
                everyColumnIsRagged = relief.spreadByColumn.all { it > 0 },
                drawable = drawable
            )
        }
    }
    reliefRows.forEach {
        println(
            "  " + it.raster + "  " + it.crossSection + "  front " + it.frontSpreadBasePairs +
                    " bp = " + it.frontSpreadNm.emitted(6) + " nm, rear " +
                    it.rearSpreadBasePairs + " bp, extent " + it.axialExtentBasePairs +
                    " bp, rim period " + it.gapFacingRimPeriodRows + " rows, spread by column " +
                    it.frontSpreadByColumn.joinToString("/")
        )
    }

    // ------------------------------------------------ Deliverable 3: the bound, factor by factor
    val boundRows = rasters.flatMap { (name, lengths, _) ->
        designs.map { (cross, rows, perRow) ->
            val relief = raggedFaceRelief(rows, perRow, lengths.first, lengths.second)
            val rigidities = multiLayerRigidities(
                layers = perRow,
                interhelicalDistance = rowPitch,
                crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
                coupling = LayerCoupling.CALIBRATED,
                compositeFraction = T258_COMPOSITE_FRACTION,
                layerSpacing = columnPitch
            )
            val ell = winklerBendingLength(
                rigidities.acrossHelixRigidity, Gen1Tile.FOUNDATION_SECANT
            )
            // C-0147's own span convention: the row the interfaces share, in rises.
            val halfSpan = maxOf(lengths.first, lengths.second) * rise / 2.0
            val bound = rimModulationBound(
                relief.frontNm, halfSpan, ell, relief.modulationWavelengthNm,
                T258_FREE_EDGE_PENALTY
            )
            val four = rimModulationBound(
                4 * rise, 112 * rise / 2.0, ell, relief.modulationWavelengthNm,
                T258_FREE_EDGE_PENALTY
            )
            T258BoundRow(
                raster = name,
                crossSection = cross,
                layers = perRow,
                compositeFraction = T258_COMPOSITE_FRACTION,
                acrossHelixRigidity = rigidities.acrossHelixRigidity,
                bendingLengthAcrossNm = ell,
                modulationWavelengthNm = relief.modulationWavelengthNm,
                rippleTransmission = loadRippleTransmission(ell, relief.modulationWavelengthNm),
                freeEdgePenalty = T258_FREE_EDGE_PENALTY,
                halfSpanNm = halfSpan,
                reliefNm = relief.frontNm,
                rimLeverPerturbation = squareWaveFundamentalAmplitude(relief.frontNm) / halfSpan,
                boundedDishingMove = bound,
                overTheFourBasePairReading = bound / four
            )
        }
    }
    println("T-258 - the rim modulation bound, factor by factor")
    boundRows.forEach {
        println(
            "  " + it.raster + "  " + it.crossSection + "  l_across " +
                    it.bendingLengthAcrossNm.emitted(6) + " nm, lambda " +
                    it.modulationWavelengthNm.emitted(6) + " nm, transfer " +
                    it.rippleTransmission.emitted(6) + " x " + T258_FREE_EDGE_PENALTY.toInt() +
                    ", lever " + it.rimLeverPerturbation.emitted(6) + " -> bound " +
                    it.boundedDishingMove.emitted(6) + " (" +
                    it.overTheFourBasePairReading.emitted(6) + "x the 4 bp reading)"
        )
    }

    // ------------------------------------------------ Deliverable 4: the threshold, and it moved
    val worstBound = boundRows.filter { it.raster.startsWith("102") }.maxOf { it.boundedDishingMove }
    val t232Tightest = Json.parseToJsonElement(ResultInputs.T_232.readText()).jsonObject
        .getValue("cells").jsonArray.map { it.jsonObject }
        .filter { it.getValue("flatAtP90").jsonPrimitive.content == "true" }
        .maxOf { it.getValue("p90OverStroke").jsonPrimitive.content.toDouble() }
    val t263 = Json.parseToJsonElement(ResultInputs.T_263.readText()).jsonObject
        .getValue("cells").jsonArray.map { it.jsonObject }
    val honeycombCells = t263.filter {
        it.getValue("model").jsonPrimitive.content == "honeycomb grillage"
    }
    val honeycombFlatAtP90 = honeycombCells.count {
        it.getValue("flatAtP90").jsonPrimitive.content == "true"
    }
    val honeycombNominal = honeycombCells
        .filter { it.getValue("flatAtNominal").jsonPrimitive.content == "true" }
        .maxOf { it.getValue("nominalOverStroke").jsonPrimitive.content.toDouble() }
    val uncoupledReadings = honeycombCells
        .map { it.getValue("uncoupledDishingOverStroke").jsonPrimitive.content.toDouble() }
        .distinct().sorted()
    // the tightest uncoupled reference that is ITSELF flat: the lattice's own no-enhancement
    // lower bound is 0.132 and is not a state anything calls flat, so it cannot be a comparand.
    val honeycombUncoupled = uncoupledReadings.filter { it < T258_TOLERANCE }.max()
    fun threshold(
        comparand: String, model: String, state: String, value: Double,
        withdrawn: Boolean, note: String
    ) = T258ThresholdRow(
        comparand = comparand, model = model, state = state, dishingOverStroke = value,
        headroomToTolerance = T258_TOLERANCE - value,
        relativeHeadroom = (T258_TOLERANCE - value) / value,
        worstBound = worstBound,
        marginOverBound = (T258_TOLERANCE - value) / value / worstBound,
        withdrawn = withdrawn, note = note
    )
    val thresholds = listOf(
        threshold(
            "C-0142 tightest coupled cell flat at p90", "smeared equivalent sheet",
            "T-232, the comparand C-0147 used", t232Tightest, true,
            "WITHDRAWN as a comparand by C-0167: re-graded on the honeycomb grillage 0 of 64 " +
                    "cells clear T-5b, so no coupled cell of this family is flat at p90 on the " +
                    "lattice the block actually is. Carried here for continuity with C-0147 and " +
                    "not used for the verdict."
        ),
        threshold(
            "C-0167 tightest coupled cell flat with NO defects", "honeycomb grillage",
            "T-263, nominal", honeycombNominal, false,
            "The tightest reading the corpus currently calls flat on the honeycomb lattice. " +
                    "It is a zero-defect reading: C-0087's measured staple dropout is what " +
                    "takes it past the tolerance, not the raggedness."
        ),
        threshold(
            "C-0167 UNCOUPLED four-layer honeycomb tile", "honeycomb grillage",
            "T-263, uncoupled reference", honeycombUncoupled, false,
            "CLAUDE.md's own always run the uncoupled tile as the reference. This is the " +
                    "TIGHTEST uncoupled reference that is itself flat: the honeycomb lattice's " +
                    "own no-enhancement lower bound reads " +
                    uncoupledReadings.max().emitted(9) + " and is not a state anything calls " +
                    "flat, so it cannot serve as a comparand."
        )
    )
    println("T-258 - the threshold, and the comparand C-0147 used has been withdrawn")
    thresholds.forEach {
        println(
            "  " + it.comparand + " = " + it.dishingOverStroke.emitted(6) + " -> headroom " +
                    it.relativeHeadroom.emitted(6) + " against a bound of " +
                    it.worstBound.emitted(6) + " = " + it.marginOverBound.emitted(6) + "x" +
                    (if (it.withdrawn) "  [WITHDRAWN COMPARAND]" else "")
        )
    }

    // ------------------------------------------------ Deliverable 5: the edge field
    val drawableRelief = 7 * rise
    val edge = listOf(0.5, 1.0, 2.0).flatMap { c ->
        val kappa = MagnesiumChlorideBuffer(c).inverseDebyeLength()
        listOf(5.0, 7.0, 10.0).map { gap ->
            val decay = 1.0 / transverseDecayRateBound(kappa, gap)
            T258EdgeRow(
                concentrationMillimolar = c,
                gapNm = gap,
                debyeLengthNm = 1.0 / kappa,
                transverseDecayLengthNm = decay,
                reliefNm = drawableRelief,
                reliefOverDecayLength = drawableRelief / decay,
                resolvable = drawableRelief > decay,
                marginAtFourBasePairs = decay / (4 * rise)
            )
        }
    }
    println("T-258 - the edge field at the drawable relief")
    edge.forEach {
        println(
            "  " + it.concentrationMillimolar.emitted(3) + " mM, gap " + it.gapNm.emitted(3) +
                    " nm: 1/q0 = " + it.transverseDecayLengthNm.emitted(6) +
                    " nm, relief/(1/q0) = " + it.reliefOverDecayLength.emitted(6) +
                    ", resolvable " + it.resolvable
        )
    }

    // ------------------------------------------------ reproductions
    val reproductions = designs.map { (cross, rows, perRow) ->
        val (published, publishedEll) = t258Published(ResultInputs.T_231.file(), cross)
        val here = boundRows.first {
            it.crossSection == cross && it.raster.startsWith("112")
        }
        T258Reproduction(
            source = "C-0147 / T-231",
            quantity = "the 4 bp bounded dishing move on " + cross,
            published = published.emitted(9),
            here = here.boundedDishingMove.emitted(9),
            departure = relativeDeparture(here.boundedDishingMove, published),
            reproduced = relativeDeparture(here.boundedDishingMove, published) < 1e-6
        ).also {
            require(relativeDeparture(here.bendingLengthAcrossNm, publishedEll) < 1e-6) {
                "the across-helix bending length must reproduce C-0147's: " +
                        here.bendingLengthAcrossNm + " against " + publishedEll
            }
        }
    } + listOf(
        T258Reproduction(
            source = "C-0147 / T-231",
            quantity = "the front and rear spreads at 112 / 108, both cross-sections",
            published = "4 and 8 bp",
            here = reliefRows.filter { it.raster.startsWith("112") }
                .joinToString("; ") { it.frontSpreadBasePairs.toString() + " and " + it.rearSpreadBasePairs },
            departure = 0.0,
            reproduced = reliefRows.filter { it.raster.startsWith("112") }
                .all { it.frontSpreadBasePairs == 4 && it.rearSpreadBasePairs == 8 }
        ),
        T258Reproduction(
            source = "C-0151 / T-245",
            quantity = "the drawable pair's front relief, in rises",
            published = "7",
            here = reliefRows.first { it.drawable }.frontSpreadBasePairs.toString(),
            departure = 0.0,
            reproduced = reliefRows.filter { it.drawable }.all { it.frontSpreadBasePairs == 7 }
        ),
        T258Reproduction(
            source = "C-0151 / T-245",
            quantity = "the block extent, both rasters",
            published = "116 bp",
            here = reliefRows.map { it.axialExtentBasePairs }.distinct().joinToString("/") + " bp",
            departure = 0.0,
            reproduced = reliefRows.all { it.axialExtentBasePairs == 116 }
        ),
        T258Reproduction(
            source = "C-0167 / T-263",
            quantity = "coupled cells of the recommended family flat at p90 on the honeycomb lattice",
            published = "0 of 64",
            here = honeycombFlatAtP90.toString() + " of " + honeycombCells.size,
            departure = 0.0,
            reproduced = honeycombFlatAtP90 == 0
        )
    )

    // ------------------------------------------------ falsifiers
    val everyColumn = reliefRows.filter { it.drawable }.all { it.everyColumnIsRagged }
    val periodsAgree = reliefRows.groupBy { it.crossSection }
        .all { (_, rows) -> rows.map { it.gapFacingRimPeriodRows }.distinct().size == 1 }
    val worstMargin = thresholds.filter { !it.withdrawn }.minOf { it.marginOverBound }
    val falsifiers = listOf(
        T258Falsifier(
            "F1",
            "the drawable relief moves material off the gap-facing COLUMN, so the raggedness " +
                    "is on the gap-facing surface after all",
            false, !everyColumn,
            "the relief is a spread of AXIAL end levels and it is present at " +
                    reliefRows.first { it.drawable }.frontSpreadByColumn.count { it > 0 } +
                    " of " + reliefRows.first { it.drawable }.frontSpreadByColumn.size +
                    " columns; no column loses or gains a helix, so the gap-facing surface is " +
                    "the same set of sidewalls it was and w(x, y) has no term in the relief"
        ),
        T258Falsifier(
            "F2",
            "the model fails to reproduce C-0147's 4 and 8 bp at 112 / 108, or fails to return " +
                    "7 and 14 at 102 / 109",
            false,
            !reproductions.filter { it.source.startsWith("C-0147") }.all { it.reproduced } ||
                    !reliefRows.filter { it.drawable }
                        .all { it.frontSpreadBasePairs == 7 && it.rearSpreadBasePairs == 14 },
            "4 / 8 at 112 / 108 and 7 / 14 at 102 / 109, at both cross-sections, and the " +
                    "published bound reproduced at a departure below 1e-6"
        ),
        T258Falsifier(
            "F3", "the modulation WAVELENGTH moves with the relief",
            true, !periodsAgree,
            "the period is 2 raster rows at both rasters and both cross-sections: it is a " +
                    "property of the turn-sense ALTERNATION and carries no length"
        ),
        T258Falsifier(
            "F4",
            "the bounded move exceeds the headroom of any state the corpus currently calls flat",
            true, worstMargin < 1.0,
            "the worst standing margin is " + worstMargin.emitted(6) + "x"
        ),
        T258Falsifier(
            "F5", "the relief falls below the 0.34 nm design quantum",
            false, drawableRelief < rise,
            "7 and 14 WHOLE rises, by construction"
        ),
        T258Falsifier(
            "F6",
            "the relief is RESOLVABLE by the slit's transverse decay at any (gap, buffer) state",
            false, edge.any { it.resolvable },
            "the tightest is " + edge.maxOf { it.reliefOverDecayLength }.emitted(6) +
                    " of the transverse decay length, against " +
                    edge.maxOf { it.reliefOverDecayLength * 4.0 / 7.0 }.emitted(6) +
                    " at C-0147's 4 bp - it does not fire, and the margin has fallen from " +
                    "1.8-3.6x to 1.0-2.1x"
        ),
        T258Falsifier(
            "F7", "the two length-to-sense assignments give different spreads",
            false,
            designs.any { (_, rows, perRow) ->
                val a = raggedFaceRelief(rows, perRow, 102, 109)
                val b = raggedFaceRelief(rows, perRow, 109, 102)
                a.frontBasePairs != b.frontBasePairs || a.rearBasePairs != b.rearBasePairs
            },
            "the assignment is a relabelling of the two senses and the spreads are invariant " +
                    "under it, asserted at both cross-sections"
        )
    )

    val drawableBound = boundRows.filter { it.raster.startsWith("102") }
    val findings = listOf(
        "THE AXIS VERDICT IS RE-TAKEN AND IT HOLDS AT 7 / 14 bp, for a reason that carries no " +
                "magnitude: the relief is a spread of AXIAL end levels, present at " +
                reliefRows.first { it.drawable }.frontSpreadByColumn.count { it > 0 } + " of " +
                reliefRows.first { it.drawable }.frontSpreadByColumn.size +
                " columns of the cross-section, so it is the tile's RIM at every column and not " +
                "its gap-facing surface. The coefficient on Sec 3's normal-direction flatness " +
                "is EXACTLY ZERO at 4 bp, at 7 bp and at any relief a two-length raster can " +
                "carry, because no relief moves a helix off a column.",
        "THE RESIDUAL BOUND MOVES BY " +
                drawableBound.first().overTheFourBasePairReading.emitted(6) +
                "x AND THAT IS TWO FACTORS, NOT ONE: 7/4 in the relief and 112/109 in the row " +
                "span, because the drawable raster's rows span 109 bp where C-0140's span 112. " +
                "At the drawable 102 / 109 the bound is " +
                drawableBound.first { it.crossSection == "15 x 4" }.boundedDishingMove.emitted(9) +
                " of the stroke on 15 x 4 and " +
                drawableBound.first { it.crossSection == "10 x 6" }.boundedDishingMove.emitted(9) +
                " on 10 x 6. The rim lever is linear in the relief; the ripple transfer and the " +
                "50x free-edge penalty contain no relief at all, and the modulation wavelength " +
                "is 2 raster rows = " +
                drawableBound.first().modulationWavelengthNm.emitted(6) +
                " nm at BOTH rasters.",
        "THE COMPARAND C-0147 QUOTED ITS 496x AGAINST HAS BEEN WITHDRAWN, and saying so is the " +
                "point of this deliverable. C-0167 re-graded every coupled cell on the " +
                "honeycomb grillage and finds " + honeycombFlatAtP90 + " of " +
                honeycombCells.size + " flat at p90, so C-0142's tightest surviving cell is no " +
                "longer a state of this design. Against what does exist - the zero-defect " +
                "recommended cell at " + honeycombNominal.emitted(9) + " and the uncoupled tile " +
                "at " + honeycombUncoupled.emitted(9) + " - the margin is " +
                thresholds.filter { !it.withdrawn }.minOf { it.marginOverBound }.emitted(6) +
                "x to " +
                thresholds.filter { !it.withdrawn }.maxOf { it.marginOverBound }.emitted(6) + "x.",
        "WHAT DID MOVE IS THE EDGE FIELD'S MARGIN, and it is the one number that got " +
                "uncomfortable. The relief is now " + edge.maxOf { it.reliefOverDecayLength }
            .emitted(6) + " of the slit's transverse decay length at its tightest state (2 mM, " +
                "a 5 nm gap) against " +
                edge.maxOf { it.reliefOverDecayLength * 4.0 / 7.0 }.emitted(6) +
                " at C-0147's 4 bp: the rim still wanders by less than the distance over which " +
                "its own perturbation dies, so a ragged rim is still a straight rim at its " +
                "mean as far as C-0022's collar is concerned, but the reserve is 1.0-2.1x " +
                "where C-0147 read 1.8-3.6x.",
        "AND THE RELIEF HAS CROSSED C-0005's GAP RESOLUTION. C-0147 noted that 1.36 nm was " +
                "below the 1.46 nm at which this project can resolve a gap; 2.38 nm is above " +
                "it, and 4.76 nm is 3.26x it. Nothing here reads that as a cost, because the " +
                "axis argument makes the coefficient zero whatever the relief - but it removes " +
                "the second, independent reason C-0147 could give for calling the relief " +
                "invisible."
    )

    val proseFailure = "none"
    val result = T258Result(
        task = "T-258",
        leaf = "A8.2",
        title = "the ragged face at the relief the DRAWABLE raster carries",
        verificationType = "logical - exact integer lattice arithmetic on the rise and on the " +
                "honeycomb cross-section, plus three closed forms already in the corpus (the " +
                "plate ripple transfer function, the square wave's fundamental and the slit's " +
                "transverse eigenvalue). No solve, and that is a stated refusal: no model in " +
                "this repository carries a per-helix row length.",
        maturity = "TRL 1-3. Model-consistent and traceable. NOTHING HERE IS MEASURED.",
        units = mapOf(
            "length" to "nm",
            "rise" to "nm per base pair",
            "rigidity" to "pN nm",
            "foundation" to "pN/nm per nm^2",
            "dishing" to "dimensionless, a fraction of the free stroke",
            "temperature" to "K"
        ),
        conventions = mapOf(
            "gap axis" to "z, the tile normal; T-5b's flatness is w(x, y) normal to the " +
                    "gap-facing surface",
            "gap-facing surface" to "one COLUMN of the cross-section - a row of duplex sidewalls",
            "ragged faces" to "the planes x = 0 and x = L, the tile's AXIAL RIM, where the " +
                    "helices terminate",
            "raggedness" to "max - min of a face's own helix end levels, in base pairs, exactly " +
                    "as C-0140 and C-0147 emit it",
            "rim modulation" to "a SQUARE wave in the raster-row index, entered into a " +
                    "sinusoidal transfer function through its fundamental 2A/pi",
            "half span" to "half the LARGER of the two row lengths - the row every interface of " +
                    "the block shares (C-0146)"
        ),
        parameters = mapOf(
            "honeycomb interhelical distance d [nm]" to Gen1Tile.INTERHELICAL_HONEYCOMB.emitted(9),
            "in-plane row pitch 3d/2 [nm]" to rowPitch.emitted(9),
            "layer pitch d sqrt(3)/2 [nm]" to columnPitch.emitted(9),
            "rise [nm/bp]" to rise.emitted(9),
            "crossover spacing, honeycomb [bp]" to Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP.emitted(9),
            "composite fraction" to T258_COMPOSITE_FRACTION.emitted(9),
            "interlayer coupling" to "CALIBRATED",
            "foundation secant [pN/nm per nm^2]" to Gen1Tile.FOUNDATION_SECANT.emitted(9),
            "free-edge penalty on the ripple transfer" to T258_FREE_EDGE_PENALTY.emitted(9),
            "flatness tolerance (T-5b)" to T258_TOLERANCE.emitted(9),
            "cross-sections" to "15 x 4 and 10 x 6, 60 helices each",
            "rasters" to "102 / 109 (drawable) and 112 / 108 (C-0140, does not close)",
            "buffers [mM MgCl2]" to "0.5, 1.0, 2.0",
            "gaps [nm]" to "5, 7, 10",
            "temperature [K]" to "300"
        ),
        sources = listOf(
            "C-0147 (T-231) - the two channels, the 4 / 8 bp reading and the published bound",
            "C-0151 (T-245) - the drawable pair 102 / 109 and its 7 bp stagger",
            "C-0140 (T-224) - the turn-sense machinery and the level walk",
            "C-0167 (T-263) - the honeycomb-grillage re-grade that withdrew the comparand",
            "C-0006 - loadRippleTransmission",
            "C-0022 / C-0110 - transverseDecayRateBound",
            "C-0141 - the corrected honeycomb cross-section"
        ),
        citedInputs = mapOf(
            ResultInputs.T_231.tag to ResultInputs.T_231.path,
            ResultInputs.T_232.tag to ResultInputs.T_232.path,
            ResultInputs.T_263.tag to ResultInputs.T_263.path
        ),
        axis = mapOf(
            "question" to "which axis is the relief on?",
            "gap-facing surface" to "one column of the cross-section, every helix of it lying " +
                    "in the tile plane",
            "what a row length changes" to "where a helix ENDS - a coordinate in that same plane",
            "verdict" to "the two ragged faces are the tile's RIM, at every column, and the " +
                    "coefficient of the raggedness on T-5b's flatness field is EXACTLY ZERO",
            "does it scale with the relief?" to "no - the statement carries no magnitude, and " +
                    "the test that it still holds is that no column loses a helix at any relief",
            "what would break it" to "a relief large enough to remove a helix from a column, " +
                    "or a two-length assignment that put the short helices in one column"
        ),
        relief = reliefRows,
        bound = boundRows,
        thresholds = thresholds,
        edgeField = edge,
        reproductions = reproductions,
        falsifiers = falsifiers,
        findings = findings,
        validity = listOf(
            "TRL 1-3, model-consistent and traceable, not empirically demonstrated.",
            "A per-helix row length is not a parameter of ANY lattice model in this repository: " +
                    "OrigamiGrillage, HoneycombCoupledTile and HoneycombGrillage all take a " +
                    "single edgeX / rowBasePairs. The flatness cost is therefore BOUNDED, not " +
                    "measured, and the bound carries CLAUDE.md's 50x free-edge penalty for " +
                    "exactly that reason - the transfer function is an infinite-plate result.",
            "The bound is a bound on a LOAD perturbation transmitted to a deflection, which is " +
                    "a different channel from the coefficient of the raggedness on the flatness " +
                    "field. The coefficient is zero; the bound is what is left over once the " +
                    "rim's own lever arm is admitted as a perturbation of the edge load.",
            "The threshold is quoted at the state it is read at, and the state moved: C-0147's " +
                    "comparand was C-0142's tightest coupled cell flat at p90, and C-0167 finds " +
                    "no such cell on the honeycomb lattice. Both readings are emitted and the " +
                    "withdrawn one is flagged.",
            "The rim census excludes the raster's two path ends, whose turn sense is undefined " +
                    "(C-0140); it is a spread over the interior helices of each column.",
            "The edge-field channel is a comparison of two LENGTHS and says only that the rim's " +
                    "wander is smaller than the reach of its own perturbation. It is not a " +
                    "solve of a ragged rim, and at the drawable relief the reserve is 1.0-2.1x."
        ),
        openQuestions = listOf(
            "What a ragged rim does to C-0022's collar once the reserve is only 1.04x, at 2 mM " +
                    "and a 5 nm gap. The comparison of lengths still passes; a 2-D solve on a " +
                    "stepped rim has never been run and this is the first state where the two " +
                    "lengths are within four per cent of each other.",
            "Whether the rear relief - 14 bp = 4.76 nm, twice the front's and 3.26x C-0005's " +
                    "gap resolution - matters on the face Sec 3's effort point sits nearer. " +
                    "C-0147 named that question and it is still open.",
            "Whether any state of the recommended honeycomb design is flat under the measured " +
                    "staple dropout at all. C-0167 says 0 of 64 coupled cells, and this task's " +
                    "margin is therefore quoted against a zero-defect reading and an uncoupled " +
                    "one."
        ),
        proseFailure = proseFailure
    )

    val output = File("gpd/results/T-258-drawable-ragged-face.json")
    val json = Json { prettyPrint = true; encodeDefaults = true }
    output.writeText(
        json.encodeToString(
            JsonObject.serializer(),
            (json.encodeToJsonElement(result).roundedForResult(
                digits = 9, floor = 1e-12
            ).withEmissionHeader(LatticeTag.HONEYCOMB, null) as JsonObject)
        ) + "\n"
    )
    println("T-258 - wrote " + output.path)
}
