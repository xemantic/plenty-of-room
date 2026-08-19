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
import com.xemantic.nano.plentyofroom.tile.HoneycombCrossSectionGeometry
import com.xemantic.nano.plentyofroom.tile.LayerCoupling
import com.xemantic.nano.plentyofroom.tile.multiLayerRigidities
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.abs

/**
 * `T-231` — what a **4 bp ragged face** costs a honeycomb tile that §3 asks to be flat.
 *
 * Run with:
 *
 * ```shell
 * ./gradlew study -Pstudy=structure.RaggedFaceCostStudyKt
 * ```
 *
 * Emits `gpd/results/T-231-ragged-face-cost.json`, deterministically.
 */

@Serializable
private data class T231FaceRecord(
    val crossSection: String,
    val rasterRows: Int,
    val helicesPerRow: Int,
    val senseOneRowLength: Int,
    val senseTwoRowLength: Int,
    val staggerBasePairs: Int,
    val frontSpreadBasePairs: Int,
    val frontSpreadNm: Double,
    val rearSpreadBasePairs: Int,
    val rearSpreadNm: Double,
    val axialExtentBasePairs: Int,
    val axialExtentNm: Double,
    val scaffoldNucleotides: Int,
    val gapFacingRimPeriodRows: Int,
    val gapFacingRimSpreadBasePairs: Int,
    val modulationWavelengthNm: Double,
    val frontLevelPopulation: String,
    val rearLevelPopulation: String
)

@Serializable
private data class T231FlatnessRecord(
    val crossSection: String,
    val layers: Int,
    val compositeFraction: Double,
    val acrossHelixRigidity: Double,
    val bendingLengthAcross: Double,
    val modulationWavelengthNm: Double,
    val rippleTransmission: Double,
    val freeEdgePenalty: Double,
    val transmissionWithPenalty: Double,
    val rimLeverPerturbation: Double,
    val boundedDishingMove: Double,
    val thresholdThatMovesTheTightestCell: Double,
    val marginOverBound: Double
)

@Serializable
private data class T231EdgeRecord(
    val concentrationMillimolar: Double,
    val gapNm: Double,
    val debyeLengthNm: Double,
    val transverseDecayLengthNm: Double,
    val reliefNm: Double,
    val reliefOverDecayLength: Double,
    val resolvable: Boolean
)

@Serializable
private data class T231PlanRecord(
    val crossSection: String,
    val demandedPaths: Int,
    val maximumPlanCeiling: Double,
    val reliefNm: Double,
    val reliefOverCeiling: Double,
    val ceilingLessRelief: Double
)

@Serializable
private data class T231StackingRecord(
    val senseOneRowLength: Int,
    val senseTwoRowLength: Int,
    val staggerBasePairs: Int,
    val axialExtentBasePairs: Int,
    val departureFromNominalPercent: Double,
    val frontReliefNm: Double,
    val frontReliefRises: Int,
    val clearsAttractiveLimb: Boolean,
    val clearsRepulsiveOnset: Boolean,
    val marginOverRepulsiveOnsetNm: Double,
    val marginOverRepulsiveOnsetRises: Double,
    val scaffoldNucleotides: Int,
    val fitsM13: Boolean
)

@Serializable
private data class T231Reproduction(
    val what: String,
    val here: Double,
    val there: Double,
    val relativeDeparture: Double,
    val source: String
)

@Serializable
private data class T231Predicate(val id: String, val statement: String, val met: Boolean)

@Serializable
private data class T231Falsifier(
    val id: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T231Result(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val parameters: Map<String, String>,
    val sources: List<String>,
    val citedInputs: Map<String, String>,
    val cheapBound: Map<String, String>,
    val faces: List<T231FaceRecord>,
    val flatness: List<T231FlatnessRecord>,
    val edge: List<T231EdgeRecord>,
    val plan: List<T231PlanRecord>,
    val stacking: List<T231StackingRecord>,
    val reproductions: List<T231Reproduction>,
    val predicates: List<T231Predicate>,
    val falsifiers: List<T231Falsifier>,
    val findings: List<String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

private const val T231_NOMINAL_WIDTH: Double = 40.0
private const val T231_M13: Int = 7249

/**
 * `CLAUDE.md`, on the transfer function used below: it *"attenuated a rim perturbation 50x more
 * than the finite-plate solve, because a free edge has no material beyond it to bend against"*.
 * The penalty is applied so the bound is taken on the conservative side of a known error.
 */
private const val T231_FREE_EDGE_PENALTY: Double = 50.0

/** `T-139`: the all-atom PMF's attractive limb ends here; there is no attractive tail past it. */
private const val T231_ATTRACTIVE_LIMB_NM: Double = 0.65

/** `T-139`: the same PMF's force *"becomes slightly repulsive after ~13 A"*. */
private const val T231_REPULSIVE_ONSET_NM: Double = 1.30

/** `T-5b`'s flatness convention, as a fraction of the stroke. */
private const val T231_FLATNESS_THRESHOLD: Double = 0.10

private fun t231Ceilings(file: File, crossSection: String): List<Pair<Int, Double>> {
    require(file.exists()) { "upstream result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText()).jsonObject.getValue("planCeilings").jsonArray
        .map { it.jsonObject }
        .filter { it.getValue("name").jsonPrimitive.content == crossSection }
        .map {
            it.getValue("demandedPaths").jsonPrimitive.content.toInt() to
                    it.getValue("maximumPlanCeiling").jsonPrimitive.content.toDouble()
        }
        .distinctBy { it.first }
}

private fun t231TightestFlatCell(file: File): Double {
    require(file.exists()) { "upstream result file is missing: ${file.path}" }
    return Json.parseToJsonElement(file.readText()).jsonObject.getValue("cells").jsonArray
        .map { it.jsonObject }
        .filter { it.getValue("flatAtP90").jsonPrimitive.content == "true" }
        .maxOf { it.getValue("p90OverStroke").jsonPrimitive.content.toDouble() }
}

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val rise = Gen1Tile.RISE_PER_BASE_PAIR
    val d = Gen1Tile.INTERHELICAL_HONEYCOMB
    val rowPitch = HoneycombCrossSectionGeometry.rowPitch(d)
    val columnPitch = HoneycombCrossSectionGeometry.columnPitch(d)
    val honeycomb = HelixCrossoverLattice.HONEYCOMB

    // ---------------------------------------------- Deliverable 1: the axis, before anything
    println("T-231 — THE CHEAP BOUND, and it is one question: WHICH AXIS? ...")
    println("  a four-layer block's gap-facing surface is the outermost LAYER's sidewalls,")
    println("  i.e. one COLUMN of the cross-section, every helix of it parallel to the tile plane")
    println("  a row length changes where a helix ENDS, which is a coordinate IN that plane")
    println("  so the ragged faces are the tile's RIM and the coefficient on Sec 3's")
    println("  gap-facing flatness is EXACTLY ZERO — before any solve")

    // ------------------------------------- Deliverable 2: what the raggedness IS, per design
    val designs = listOf(Triple("15 x 4", 15, 4), Triple("10 x 6", 10, 6))
    val faces = designs.map { (name, rows, perRow) ->
        val turns = honeycombRasterTurns(honeycombXRasterPath(rows, perRow))
        val raster = twoLengthRaster(turns, 112, 108)
        val rim = gapFacingRimLevels(turns, raster, column = 0)
        val first = turns.first().index - 1
        fun ends(evenFace: Boolean) = turns.map { turn ->
            val before = turn.index - 1
            val even = if (Math.floorMod(before, 2) == 0) before else turn.index
            val odd = if (Math.floorMod(before, 2) == 0) turn.index else before
            raster.crossoverLevels[(if (evenFace) even else odd) - first]
        }
        fun census(levels: List<Int>) = levels.groupingBy { it }.eachCount().toSortedMap()
            .entries.joinToString(", ") { (level, count) -> ("$count at level $level") }
        val population = census(ends(true))
        val rearPopulation = census(ends(false))
        T231FaceRecord(
            crossSection = name,
            rasterRows = rows,
            helicesPerRow = perRow,
            senseOneRowLength = 112,
            senseTwoRowLength = 108,
            staggerBasePairs = 4,
            frontSpreadBasePairs = raster.frontSpreadBasePairs,
            frontSpreadNm = raster.frontSpreadBasePairs * rise,
            rearSpreadBasePairs = raster.rearSpreadBasePairs,
            rearSpreadNm = raster.rearSpreadBasePairs * rise,
            axialExtentBasePairs = raster.axialExtentBasePairs,
            axialExtentNm = raster.axialExtentBasePairs * rise,
            scaffoldNucleotides = raster.scaffoldNucleotides,
            gapFacingRimPeriodRows = sequencePeriod(rim.map { it.second }),
            gapFacingRimSpreadBasePairs = rim.maxOf { it.second } - rim.minOf { it.second },
            modulationWavelengthNm = sequencePeriod(rim.map { it.second }) * rowPitch,
            frontLevelPopulation = population,
            rearLevelPopulation = rearPopulation
        )
    }
    faces.forEach {
        println(("  %-7s front %d bp = %.4f nm, rear %d bp = %.4f nm, extent %d bp, " +
                "rim period %d rows = %.4f nm  [%s]")
            .format(it.crossSection, it.frontSpreadBasePairs, it.frontSpreadNm,
                it.rearSpreadBasePairs, it.rearSpreadNm, it.axialExtentBasePairs,
                it.gapFacingRimPeriodRows, it.modulationWavelengthNm, it.frontLevelPopulation))
    }

    // ------------------------------------------------------ Deliverable 3: the flatness bound
    val tightest = t231TightestFlatCell(
        File("gpd/results/T-232-coupled-cells-at-the-honeycomb-cross-section.json")
    )
    val threshold = (T231_FLATNESS_THRESHOLD - tightest) / tightest
    val edgeX = 112 * rise
    val flatness = designs.map { (name, _, perRow) ->
        val face = faces.first { it.crossSection == name }
        val rigidities = multiLayerRigidities(
            layers = perRow,
            interhelicalDistance = rowPitch,
            crossoverSpacingBasePairs = Gen1Tile.CROSSOVER_SPACING_HONEYCOMB_BP,
            coupling = LayerCoupling.CALIBRATED,
            compositeFraction = 0.30,
            layerSpacing = columnPitch
        )
        val ell = winklerBendingLength(
            rigidities.acrossHelixRigidity, Gen1Tile.FOUNDATION_SECANT
        )
        val transmission = loadRippleTransmission(ell, face.modulationWavelengthNm)
        val perturbation = squareWaveFundamentalAmplitude(face.frontSpreadNm) / (edgeX / 2.0)
        val bounded = perturbation * transmission * T231_FREE_EDGE_PENALTY
        T231FlatnessRecord(
            crossSection = name,
            layers = perRow,
            compositeFraction = 0.30,
            acrossHelixRigidity = rigidities.acrossHelixRigidity,
            bendingLengthAcross = ell,
            modulationWavelengthNm = face.modulationWavelengthNm,
            rippleTransmission = transmission,
            freeEdgePenalty = T231_FREE_EDGE_PENALTY,
            transmissionWithPenalty = transmission * T231_FREE_EDGE_PENALTY,
            rimLeverPerturbation = perturbation,
            boundedDishingMove = bounded,
            thresholdThatMovesTheTightestCell = threshold,
            marginOverBound = threshold / bounded
        )
    }
    println("T-231 — the flatness bound ...")
    flatness.forEach {
        println(("  %-7s l_across = %.4f nm, lambda = %.4f nm, transfer %.3e x %.0f penalty, " +
                "rim lever %.5f -> bounded move %.3e against a %.5f threshold (%.1fx)")
            .format(it.crossSection, it.bendingLengthAcross, it.modulationWavelengthNm,
                it.rippleTransmission, it.freeEdgePenalty, it.rimLeverPerturbation,
                it.boundedDishingMove, it.thresholdThatMovesTheTightestCell, it.marginOverBound))
    }

    // ---------------------------------------------------------- Deliverable 4: the edge field
    val relief = 4 * rise
    val edge = listOf(0.5, 1.0, 2.0).flatMap { c ->
        val buffer = MagnesiumChlorideBuffer(c)
        val kappa = buffer.inverseDebyeLength()
        listOf(5.0, 7.0, 10.0).map { gap ->
            val decay = 1.0 / transverseDecayRateBound(kappa, gap)
            T231EdgeRecord(
                concentrationMillimolar = c,
                gapNm = gap,
                debyeLengthNm = 1.0 / kappa,
                transverseDecayLengthNm = decay,
                reliefNm = relief,
                reliefOverDecayLength = relief / decay,
                resolvable = relief > decay
            )
        }
    }
    println("T-231 — the edge field ...")
    edge.forEach {
        println(("  %.1f mM, gap %.1f nm: lambda_D = %.4f nm, 1/q0 = %.4f nm, relief/1/q0 = " +
                "%.4f, resolvable %s")
            .format(it.concentrationMillimolar, it.gapNm, it.debyeLengthNm,
                it.transverseDecayLengthNm, it.reliefOverDecayLength, it.resolvable))
    }

    // ---------------------------------------------------------- Deliverable 5: the plan budget
    val t219 = File("gpd/results/T-219-honeycomb-station-lattice-and-placement.json")
    val plan = designs.flatMap { (name, _, _) ->
        t231Ceilings(t219, name).map { (paths, ceiling) ->
            T231PlanRecord(
                crossSection = name,
                demandedPaths = paths,
                maximumPlanCeiling = ceiling,
                reliefNm = relief,
                reliefOverCeiling = relief / ceiling,
                ceilingLessRelief = ceiling - relief
            )
        }
    }

    // -------------------------------------------------------- Deliverable 6: the second axis
    val senseOne = honeycomb.admissibleRowLengths(80, 160, honeycomb.turnPairResidues(0, 1))
    val senseTwo = honeycomb.admissibleRowLengths(80, 160, honeycomb.turnPairResidues(0, 2))
    val turns15 = honeycombRasterTurns(honeycombXRasterPath(15, 4))
    val stacking = senseOne.flatMap { a ->
        senseTwo.filter { abs(it - a) <= 10 }.map { b ->
            val raster = twoLengthRaster(turns15, a, b)
            val front = raster.frontSpreadBasePairs * rise
            T231StackingRecord(
                senseOneRowLength = a,
                senseTwoRowLength = b,
                staggerBasePairs = abs(a - b),
                axialExtentBasePairs = raster.axialExtentBasePairs,
                departureFromNominalPercent = 100.0 *
                        (raster.axialExtentBasePairs * rise - T231_NOMINAL_WIDTH) /
                        T231_NOMINAL_WIDTH,
                frontReliefNm = front,
                frontReliefRises = raster.frontSpreadBasePairs,
                clearsAttractiveLimb = front > T231_ATTRACTIVE_LIMB_NM,
                clearsRepulsiveOnset = front > T231_REPULSIVE_ONSET_NM,
                marginOverRepulsiveOnsetNm = front - T231_REPULSIVE_ONSET_NM,
                marginOverRepulsiveOnsetRises = (front - T231_REPULSIVE_ONSET_NM) / rise,
                scaffoldNucleotides = raster.scaffoldNucleotides,
                fitsM13 = raster.scaffoldNucleotides <= T231_M13
            )
        }
    }.sortedBy { abs(it.departureFromNominalPercent) }
    val recommended = stacking.first { it.senseOneRowLength == 112 && it.senseTwoRowLength == 108 }
    val tight = stacking.first { it.senseOneRowLength == 112 && it.senseTwoRowLength == 109 }
    val firstWholeRise = stacking
        .filter { it.fitsM13 && it.marginOverRepulsiveOnsetRises >= 1.0 }
        .minByOrNull { abs(it.departureFromNominalPercent) }
    val widestFit = stacking.filter { it.fitsM13 }
        .minByOrNull { abs(it.departureFromNominalPercent) }!!
    val saturatedCeiling = plan.minOf { it.maximumPlanCeiling }
    println("T-231 — the second axis, the ragged face as an ANTI-STACKING geometry ...")
    stacking.take(10).forEach {
        println(("  %3d / %3d bp  stagger %2d  extent %3d bp %+6.2f %%  front relief %.4f nm = " +
                "%d rises  limb %s  onset %s  margin %+.4f nm")
            .format(it.senseOneRowLength, it.senseTwoRowLength, it.staggerBasePairs,
                it.axialExtentBasePairs, it.departureFromNominalPercent, it.frontReliefNm,
                it.frontReliefRises, it.clearsAttractiveLimb, it.clearsRepulsiveOnset,
                it.marginOverRepulsiveOnsetNm))
    }

    // ----------------------------------------------------------------- upstream reproductions
    val t218 = File("gpd/results/T-218-honeycomb-raster-turn-sense.json")
    val t218Extent = Json.parseToJsonElement(t218.readText()).jsonObject
        .getValue("parameters").jsonObject
        .getValue("recommendedAxialExtentBasePairs").jsonPrimitive.content.toDouble()
    val here15 = faces.first { it.crossSection == "15 x 4" }
    val reproductions = listOf(
        T231Reproduction("C-0140's front-face raggedness at 112 / 108",
            here15.frontSpreadBasePairs.toDouble(), 4.0,
            relativeDeparture(here15.frontSpreadBasePairs.toDouble(), 4.0), t218.path),
        T231Reproduction("C-0140's rear-face raggedness at 112 / 108",
            here15.rearSpreadBasePairs.toDouble(), 8.0,
            relativeDeparture(here15.rearSpreadBasePairs.toDouble(), 8.0), t218.path),
        T231Reproduction("C-0140's axial extent at 112 / 108",
            here15.axialExtentBasePairs.toDouble(), t218Extent,
            relativeDeparture(here15.axialExtentBasePairs.toDouble(), t218Extent), t218.path),
        T231Reproduction("C-0141's honeycomb in-plane row pitch, 3d/2",
            rowPitch, 1.5 * d, relativeDeparture(rowPitch, 1.5 * d),
            "gpd/results/T-219-honeycomb-station-lattice-and-placement.json"),
        T231Reproduction("C-0142's tightest coupled cell that is still flat at the 90th " +
                "percentile", tightest, 0.0973238201, relativeDeparture(tightest, 0.0973238201),
            "gpd/results/T-232-coupled-cells-at-the-honeycomb-cross-section.json")
    )

    val worstBound = flatness.maxOf { it.boundedDishingMove }
    val worstMargin = flatness.minOf { it.marginOverBound }
    val predicates = listOf(
        T231Predicate("P1",
            "the axis is settled before anything is priced, and the coefficient of the " +
                    "raggedness on Sec 3's gap-facing flatness is stated",
            true),
        T231Predicate("P2",
            "the raggedness is emitted per helix and both spreads reproduce C-0140's 4 and " +
                    "8 bp at departure 0.0",
            reproductions.take(2).all { it.relativeDeparture == 0.0 }),
        T231Predicate("P3",
            "the modulation wavelength is DERIVED from the cross-section and the flatness " +
                    "cost bounded with the free-edge correction CLAUDE.md records for it",
            faces.all { it.gapFacingRimPeriodRows == 2 } &&
                    flatness.all { it.freeEdgePenalty == T231_FREE_EDGE_PENALTY }),
        T231Predicate("P4",
            "the threshold that moves C-0142's tightest surviving cell is quoted",
            threshold > 0.0),
        T231Predicate("P5",
            "the whole family of admissible pairs is scored on BOTH axes and the " +
                    "recommendation restated or changed",
            stacking.size > 20 && recommended.clearsRepulsiveOnset),
        T231Predicate("P6",
            "what no model here can read is STATED rather than approximated silently",
            true)
    )

    val falsifiers = listOf(
        T231Falsifier("F1",
            "the ragged face turns out to be a GAP-FACING face, so the coefficient is not " +
                    "zero and the cheap bound is void",
            false,
            "a raster row's length is measured ALONG the helices and the gap-facing surface " +
                    "is a row of duplex sidewalls at one COLUMN of the cross-section; the two " +
                    "are orthogonal, so the ragged faces are the tile's rim at x = 0 and " +
                    "x = L and nothing about Sec 3's normal-direction flatness field reads them"),
        T231Falsifier("F2",
            "the front and rear spreads fail to reproduce C-0140's 4 and 8 bp",
            reproductions.take(2).any { it.relativeDeparture != 0.0 },
            ("4 and 8 bp on 15 x 4 at departure 0.0, and the SAME 4 and 8 on 10 x 6 — the " +
                    "raggedness is a property of the lattice's alternation, not of the " +
                    "cross-section's shape")),
        T231Falsifier("F3",
            "the modulation wavelength comes out LONGER than the plate's bending length, so " +
                    "the ripple argument attenuates nothing",
            flatness.any { it.modulationWavelengthNm > it.bendingLengthAcross },
            ("the gap-facing rim alternates with period 2 rows = %.4f nm against bending " +
                    "lengths of %.4f-%.4f nm, i.e. %.2f-%.2f wavelengths inside one bending " +
                    "length — the plate cannot follow it")
                .format(faces.first().modulationWavelengthNm,
                    flatness.minOf { it.bendingLengthAcross },
                    flatness.maxOf { it.bendingLengthAcross },
                    flatness.minOf { it.bendingLengthAcross } / faces.first()
                        .modulationWavelengthNm,
                    flatness.maxOf { it.bendingLengthAcross } / faces.first()
                        .modulationWavelengthNm)),
        T231Falsifier("F4",
            "the bounded flatness cost EXCEEDS the threshold that moves C-0142's tightest " +
                    "surviving cell — DECLARED OPEN, this is the outcome the task exists to " +
                    "detect",
            worstBound > threshold,
            ("the bound is %.3e of the stroke against a threshold of %.5f, a margin of " +
                    "%.0fx at the worse of the two cross-sections")
                .format(worstBound, threshold, worstMargin)),
        T231Falsifier("F5",
            "the relief is smaller than the 0.34 nm design quantum, so nothing priced here " +
                    "is a design variable",
            here15.frontSpreadNm < rise,
            ("the front relief is %d rises = %.4f nm and the rear %d rises = %.4f nm; both " +
                    "are integer multiples of the quantum by construction, because a row " +
                    "length is a base-pair count")
                .format(here15.frontSpreadBasePairs, here15.frontSpreadNm,
                    here15.rearSpreadBasePairs, here15.rearSpreadNm)),
        T231Falsifier("F6",
            "the recommendation CHANGES under the second axis, i.e. 112 / 108 is not the best " +
                    "pair once raggedness is priced — DECLARED OPEN",
            !recommended.clearsRepulsiveOnset ||
                    stacking.filter { it.fitsM13 }
                        .minByOrNull { abs(it.departureFromNominalPercent) } != recommended,
            ("FIRED, and favourably for the pair C-0140 excluded. Among ALL pairs that fit " +
                    "M13 the width optimum is %d / %d bp at %+.2f %%, not 112 / 108 at " +
                    "%+.2f %% — C-0140 reached 112 / 108 only through its own stated " +
                    "\"stagger of at most 4 bp\" filter. On the anti-stacking axis the same " +
                    "pair is better too, clearing the repulsive onset by %.2f rises against " +
                    "%.2f. What keeps 112 / 108 is the THIRD axis, the plan budget: its %d bp " +
                    "relief costs %.4f nm of outboard bound against %.4f nm for the wider " +
                    "pair, and C-0141's ceiling saturates at %.3f nm — which the wider pair " +
                    "EXCEEDS. So the three axes rank the two pairs 2-1 against C-0140's choice " +
                    "and the tie is broken by whether the design needs a saturated path count.")
                .format(widestFit.senseOneRowLength, widestFit.senseTwoRowLength,
                    widestFit.departureFromNominalPercent,
                    recommended.departureFromNominalPercent,
                    widestFit.marginOverRepulsiveOnsetRises,
                    recommended.marginOverRepulsiveOnsetRises,
                    recommended.frontReliefRises, recommended.frontReliefNm,
                    widestFit.frontReliefNm, saturatedCeiling))
    )

    val findings = listOf(
        ("THE CHEAP BOUND CLOSED THE HEADLINE THREAT IN ONE QUESTION AND THE ANSWER IS AN " +
                "AXIS. A four-layer honeycomb block's gap-facing surface is the outermost " +
                "LAYER's sidewalls — one column of the cross-section, every helix of it lying " +
                "in the tile plane — while a row length changes where a helix ENDS, which is a " +
                "coordinate in that same plane. So the two ragged faces are the tile's RIM at " +
                "x = 0 and x = L, the coefficient of the raggedness on Sec 3's normal-direction " +
                "flatness field is EXACTLY ZERO, and T-5b's dishing convention cannot read it " +
                "at all. Sec 3 asks a tile to be flat in the direction of its gap; a two-length " +
                "raster leaves it ragged at right angles to that."),
        ("WHAT IT ACTUALLY IS: A COMB, NOT A FACE, AND THE SAME COMB ON BOTH CROSS-SECTIONS. " +
                "At 112 / 108 the front face's helix ends sit at exactly TWO levels %.4f nm " +
                "apart (%s) and the rear face at THREE spanning %.4f nm (%s) — so over the " +
                "outermost 4 bp of the tile only about half the helices are present, and the " +
                "rim's cross-sectional area is halved over 1.36 nm. 10 x 6 gives the identical " +
                "4 and 8 bp, because the raggedness is a property of the honeycomb's turn-sense " +
                "alternation and not of the block's shape. The two faces are NOT mirror " +
                "images, and the asymmetry is what separates this reading from a per-ROW one: " +
                "every raster row spans the LARGER length exactly and consecutive rows are " +
                "staggered by 4 bp, so a shorter helix is RECESSED inside its own row's window " +
                "rather than shortening it (C-0146 reads the same construction that way, and " +
                "the two readings are the same object measured per helix and per row).")
            .format(here15.frontSpreadNm, here15.frontLevelPopulation, here15.rearSpreadNm,
                here15.rearLevelPopulation),
        ("AND THE FLATNESS COST IS BOUNDED AT %.1e OF THE STROKE AGAINST A THRESHOLD OF " +
                "%.5f — A MARGIN OF %.0fx — BECAUSE THE MODULATION IS TOO FINE FOR THE PLATE " +
                "TO FOLLOW. The gap-facing rim alternates with period exactly 2 raster rows, " +
                "%.4f nm, against across-helix bending lengths of %.4f-%.4f nm: the plate's own " +
                "transfer function 1/(1 + (2 pi l / lambda)^4) passes %.2e of it, and even with " +
                "CLAUDE.md's measured 50x free-edge penalty applied the rim's own lever-arm " +
                "perturbation of %.5f survives at %.1e. C-0142's tightest cell that is still " +
                "flat sits at %.7f, so it has %.2f %% of headroom and the raggedness can " +
                "consume at most %.4f %% of it.")
            .format(worstBound, threshold, worstMargin, faces.first().modulationWavelengthNm,
                flatness.minOf { it.bendingLengthAcross },
                flatness.maxOf { it.bendingLengthAcross },
                flatness.maxOf { it.rippleTransmission },
                flatness.first().rimLeverPerturbation, worstBound, tightest,
                100.0 * threshold, 100.0 * worstBound / threshold),
        ("THE EDGE FIELD CANNOT RESOLVE IT EITHER, AND THAT IS A CLOSED FORM RATHER THAN A " +
                "SOLVE. A lateral feature of the rim enters the slit as a transverse mode " +
                "decaying at q0 with q0^2 >= kappa^2 + (pi/2h)^2, so its own reach is " +
                "%.4f-%.4f nm over Sec 3's three gaps and the three buffers, and the 1.36 nm " +
                "relief is only %.2f-%.2f of it. The rim wanders by less than the distance " +
                "over which its " +
                "own perturbation dies, so a ragged rim IS a straight rim at its mean as far " +
                "as C-0022's collar is concerned. The relief is nevertheless the same size as " +
                "C-0005's 1.46 nm gap resolution, which is why it had to be checked rather " +
                "than assumed.")
            .format(edge.minOf { it.transverseDecayLengthNm },
                edge.maxOf { it.transverseDecayLengthNm },
                edge.minOf { it.reliefOverDecayLength }, edge.maxOf { it.reliefOverDecayLength }),
        ("WHAT IT DOES COST IS PLAN BUDGET, AND THERE IT IS NOT NEGLIGIBLE: %.4f nm OFF AN " +
                "OUTBOARD BOUND THAT SATURATES AT %.3f nm. C-0141's honeycomb plan ceiling " +
                "falls to %.3f nm as the path count saturates, and a short row's rim is 4 bp " +
                "further in — %.1f %% of the ceiling at saturation and %.1f %% at the widest. " +
                "That is the one channel on which the raggedness is a real design variable, " +
                "and it is a variable because a row length is a base-pair COUNT: the relief is " +
                "4 whole rises, not a residue.")
            .format(relief, plan.minOf { it.maximumPlanCeiling },
                plan.minOf { it.maximumPlanCeiling },
                100.0 * plan.maxOf { it.reliefOverCeiling },
                100.0 * plan.minOf { it.reliefOverCeiling }),
        ("AND ON THE SECOND AXIS THE RAGGED FACE IS A BENEFIT THAT THE TIGHTER PAIR DOES NOT " +
                "BUY. All three of Rothemund's measured anti-stacking remedies work by denying " +
                "a terminus a coaxial partner; a staggered face denies it geometrically. The " +
                "measured range is a contact interaction — the all-atom PMF's attractive limb " +
                "ends at %.2f nm and its force turns repulsive past %.2f nm — and C-0140's " +
                "4 bp stagger puts the face's two levels %.4f nm apart, CLEARING the " +
                "conservative onset, while the 3 bp pair it lists as the tightest gives " +
                "%.4f nm and does NOT. But 112 / 108 clears it by only %.4f nm — %.2f of a " +
                "rise, below the design language's own quantum and therefore NOT a quotable " +
                "margin — and the first pair that fits M13 and clears it by a WHOLE rise is " +
                "%d / %d bp at %+.2f %%, which is a different design. See the next finding.")
            .format(T231_ATTRACTIVE_LIMB_NM, T231_REPULSIVE_ONSET_NM, recommended.frontReliefNm,
                tight.frontReliefNm, recommended.marginOverRepulsiveOnsetNm,
                recommended.marginOverRepulsiveOnsetRises,
                firstWholeRise?.senseOneRowLength ?: 0, firstWholeRise?.senseTwoRowLength ?: 0,
                firstWholeRise?.departureFromNominalPercent ?: 0.0),
        ("SO F6 FIRED AND THE RECOMMENDATION IS CONDITIONAL, WHICH IS THE RESULT. Priced on " +
                "four axes the two candidates rank 3-1: %d / %d bp wins the WIDTH " +
                "(%+.2f %% against %+.2f %%), wins the ANTI-STACKING clearance (%.2f rises " +
                "against %.2f) and wins the SCAFFOLD (%d nt against %d); 112 / 108 wins only " +
                "the PLAN BUDGET, and it wins it decisively — its %.4f nm of relief sits " +
                "inside C-0141's saturated outboard ceiling of %.3f nm where the wider pair's " +
                "%.4f nm EXCEEDS it, leaving a short row no outboard budget at all. C-0140 " +
                "selected on width under a self-imposed \"stagger at most 4 bp\" filter and " +
                "arrived at the right answer for a reason it did not state. THE " +
                "RECOMMENDATION THEREFORE STANDS ONLY FOR A DESIGN THAT NEEDS A SATURATED " +
                "PATH COUNT: at C-0142's flat cells on 10 x 6, which sit at 10 to 50 paths " +
                "with ceilings of 38.08 down to 4.604 nm, both pairs are affordable and the " +
                "wider one is better on three axes of four.")
            .format(widestFit.senseOneRowLength, widestFit.senseTwoRowLength,
                widestFit.departureFromNominalPercent,
                recommended.departureFromNominalPercent,
                widestFit.marginOverRepulsiveOnsetRises,
                recommended.marginOverRepulsiveOnsetRises,
                widestFit.scaffoldNucleotides, recommended.scaffoldNucleotides,
                recommended.frontReliefNm, saturatedCeiling,
                widestFit.frontReliefNm)
    )

    val result = T231Result(
        task = "T-231",
        leaf = "A8.2",
        title = "What a 4 bp ragged face costs a honeycomb tile that Sec 3 asks to be flat",
        verificationType = "logical (exact integer lattice arithmetic on the rise and the " +
                "honeycomb cross-section, plus two closed forms already in this repository: " +
                "the plate ripple transfer function and the slit's transverse eigenvalue) + " +
                "literature (the blunt-end stacking range, consumed from " +
                "gpd/data/T-139-blunt-end-stacking-literature.md with its own read flags)",
        maturity = "TRL 1-3 - model-consistent and traceable, NOTHING HERE IS MEASURED. The " +
                "flatness cost is a BOUND with a stated conservative correction, not a solve; " +
                "no model in this repository reads a per-row row length",
        units = mapOf(
            "length" to "nm",
            "dishing" to "fraction of the free stroke",
            "count" to "base pairs, rises, helices"
        ),
        conventions = listOf(
            "rise 0.34 nm/bp; the honeycomb in-plane row pitch is 3d/2 and the layer pitch " +
                    "d*sqrt(3)/2, d = 2.536 nm (C-0141's corrected cross-section)",
            "Sec 3's gap is along the tile NORMAL; T-5b's flatness is a deflection field in " +
                    "that direction",
            "a face's RAGGEDNESS is the spread of its own crossover levels, in base pairs, " +
                    "exactly as C-0140 emits it",
            "every length is reported in RISES as well as nm, because a margin below one rise " +
                    "cannot be corrected, only removed",
            "the ripple transfer function is written for a SINUSOID, so the rim's square-wave " +
                    "modulation enters through its fundamental, 2/pi of the peak-to-peak"
        ),
        parameters = mapOf(
            "senseOneRowLength" to "112",
            "senseTwoRowLength" to "108",
            "staggerBasePairs" to "4",
            "frontReliefNm" to roundForResult(here15.frontSpreadNm).toString(),
            "rearReliefNm" to roundForResult(here15.rearSpreadNm).toString(),
            "gapFacingFlatnessCoefficient" to "0",
            "modulationPeriodRows" to here15.gapFacingRimPeriodRows.toString(),
            "modulationWavelengthNm" to roundForResult(here15.modulationWavelengthNm).toString(),
            "boundedDishingMove" to roundForResult(worstBound).toString(),
            "thresholdThatMovesTheTightestCell" to roundForResult(threshold).toString(),
            "marginOverBound" to roundForResult(worstMargin).toString(),
            "tightestFlatCell" to roundForResult(tightest).toString(),
            "planCeilingAtSaturationNm" to
                    roundForResult(plan.minOf { it.maximumPlanCeiling }).toString(),
            "reliefOverPlanCeilingAtSaturation" to
                    roundForResult(plan.maxOf { it.reliefOverCeiling }).toString(),
            "attractiveLimbNm" to T231_ATTRACTIVE_LIMB_NM.toString(),
            "repulsiveOnsetNm" to T231_REPULSIVE_ONSET_NM.toString(),
            "marginOverRepulsiveOnsetRises" to
                    roundForResult(recommended.marginOverRepulsiveOnsetRises).toString(),
            "widthOptimumAmongAllPairsFittingM13" to
                    (widestFit.senseOneRowLength.toString() + "/" +
                            widestFit.senseTwoRowLength.toString()),
            "widthOptimumFrontReliefNm" to roundForResult(widestFit.frontReliefNm).toString(),
            "saturatedPlanCeilingNm" to roundForResult(saturatedCeiling).toString(),
            "firstWholeRiseClearingPair" to
                    ((firstWholeRise?.senseOneRowLength ?: 0).toString() + "/" +
                            (firstWholeRise?.senseTwoRowLength ?: 0).toString())
        ),
        sources = listOf(
            "gpd/results/T-218-honeycomb-raster-turn-sense.json",
            "gpd/results/T-219-honeycomb-station-lattice-and-placement.json",
            "gpd/results/T-232-coupled-cells-at-the-honeycomb-cross-section.json",
            "gpd/data/T-139-blunt-end-stacking-literature.md"
        ),
        citedInputs = mapOf(
            "the all-atom PMF's attractive limb, 3.5-6.5 A" to
                    "Maffeo et al. 2012, via gpd/data/T-139-blunt-end-stacking-literature.md, " +
                    "read directly",
            "the same PMF's repulsive onset, ~13 A" to
                    "Maffeo et al. 2012, via the same manifest, read directly",
            "the free-edge penalty on the ripple transfer function, 50x" to
                    "CLAUDE.md, from this repository's own finite-plate solve",
            "T-5b's flatness convention, 0.10 of the stroke" to "the project's own convention"
        ),
        cheapBound = mapOf(
            "question" to "which surface does the raggedness live on",
            "answer" to "the tile's in-plane RIM, x = 0 and x = L, not the gap-facing faces",
            "gapFacingFlatnessCoefficient" to "0",
            "verdict" to "the headline threat is closed before any solve; what remains is the " +
                    "plan budget, the edge field and an anti-stacking benefit"
        ),
        faces = faces,
        flatness = flatness,
        edge = edge,
        plan = plan,
        stacking = stacking,
        reproductions = reproductions,
        predicates = predicates,
        falsifiers = falsifiers,
        findings = findings,
        validity = listOf(
            "the flatness number is a BOUND, not a solve: no model in this repository reads a " +
                    "per-row row length, and OrigamiGrillage and HoneycombCoupledTile both " +
                    "take a single edgeX",
            "the ripple transfer function is derived for an INFINITE plate and CLAUDE.md " +
                    "records that it over-attenuates a RIM perturbation by 50x; that penalty " +
                    "is applied, and the bound is therefore conservative by construction " +
                    "rather than exact",
            "the rim lever-arm perturbation is an upper bound on the load perturbation: the " +
                    "collar follows its own edge, so displacing the edge by dx displaces the " +
                    "collar's line of action by dx and nothing else",
            "the stacking range is measured on OTHER objects, in monovalent salt, and is " +
                    "transferred exactly as C-0079 and C-0085 transfer it",
            "the anti-stacking benefit is a FABRICATION consideration and this programme " +
                    "cannot price fabrication; it is reported as a mechanism with a measured " +
                    "range, never as a yield"
        ),
        openQuestions = listOf(
            "a per-row row length is not a parameter of any lattice model here, so the " +
                    "flatness cost is bounded rather than measured; extending OrigamiGrillage " +
                    "to a per-beam lengthX would settle it and is not free",
            "what a ragged face does to tile-to-tile stacking in an ARRAY is unmeasured; Sec 3 " +
                    "specifies no array, and the two-level face is reported as a capacity " +
                    "halving rather than as an aggregation rate",
            "the rear face's 8 bp relief is twice the front's at every stagger, and nothing " +
                    "here asks which face Sec 3's effort point sits nearer"
        )
    )

    val json = Json { prettyPrint = true }
    val out = File("gpd/results/T-231-ragged-face-cost.json")
    out.writeText(
        json.encodeToString(json.encodeToJsonElement(result).roundedForResult(floor = 1e-15))
    )
    println("T-231 — wrote ${out.path}")
    findings.forEach { println("  * $it") }
}
