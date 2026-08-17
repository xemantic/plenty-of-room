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

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.io.File

/**
 * The readers `T-156` takes its census through.
 *
 * **Nothing here computes physics.** Every number the census quotes is read from the result file of
 * the study that produced it, keyed on **every** dimension that study's sweep varied — `CH-0038`'s
 * lesson, and `T-156`'s own gate 5. A number transcribed from a claim's prose is not evidence that
 * the claim's file says it, and this task is entirely about what the files say.
 */

private val censusReader = Json { ignoreUnknownKeys = true }

/** Where the emitting studies put their files, relative to the repository root. */
const val GEN1_RESULTS_DIRECTORY: String = "gpd/results"

/** The recommended device's layer height in nm (`C-0071`, `C-0068`). */
const val CENSUS_LAYER_HEIGHT: Double = 10.0

/** The buffer this census argues for, in mM. */
const val CENSUS_LOW_SALT: Double = 0.5

/** §3's own buffer, in mM. */
const val CENSUS_HIGH_SALT: Double = 2.0

// ---------------------------------------------------------------- the record shapes

/** One `T-3` threshold row (`C-0012`) — the bias a target needs at one `(model, height, buffer)`. */
@Serializable
data class CensusBlockingThreshold(
    val model: String,
    val layerHeight: Double,
    val concentration: Double,
    val biasForHundredPiconewtonBlocking: Double? = null
)

/** One `T-2` bias clause (`C-0016`) — the window's reading of the same threshold, on a `σ` grid. */
@Serializable
data class CensusWindowBiasClause(
    val layerHeight: Double,
    val concentration: Double,
    val biasForHundredPiconewtonBlocking: Double? = null,
    val readingAModelsPassing: Int,
    val readingAModelsTotal: Int,
    val readingABindingClause: String
)

/** One `T-2` stability clause (`C-0016`) — read at a **fixed applied bias**, not at a held point. */
@Serializable
data class CensusWindowStabilityClause(
    val layerHeight: Double,
    val concentration: Double,
    val appliedBias: Double,
    val modelsTotal: Int,
    val modelsUnstable: Int,
    val requiredCouplingStiffnessLow: Double? = null,
    val requiredCouplingStiffnessHigh: Double? = null
)

/** One `T-16` requirement row (`C-0017`) — the held operating point and its floor. */
@Serializable
data class CensusCouplingRequirement(
    val model: String,
    val layerHeight: Double,
    val concentration: Double,
    val heldGap: Double,
    val simultaneousTargetBias: Double,
    val stabilityFloor: Double,
    val mandatedStiffness: Double,
    val stabilityMargin: Double? = null
)

/** One `T-4` ceiling row (`C-0018`) — a `(model, height, buffer, load line)` fold search. */
@Serializable
data class CensusUsableBiasCeiling(
    val model: String,
    val layerHeight: Double,
    val concentration: Double,
    val loadLine: String,
    val operatingBias: Double? = null,
    val pullInBias: Double? = null,
    val pullInStroke: Double? = null,
    val usableBias: Double? = null,
    val bindingCeiling: String,
    val margin: Double? = null
)

/** One `T-25` buffer comparison (`C-0027`) — the corrected margins, and its declared transfers. */
@Serializable
data class CensusBufferComparison(
    val layerHeight: Double,
    val concentration: Double,
    val statesWithAFold: Int,
    val statesTotal: Int,
    val biasMarginLow: Double,
    val biasMarginHigh: Double,
    val stiffnessMarginBaselineLow: Double? = null,
    val stiffnessMarginBaselineHigh: Double? = null,
    val stiffnessMarginCorrectedLow: Double? = null,
    val stiffnessMarginCorrectedHigh: Double? = null
)

/** One `T-76` fold row (`C-0032`) — read on `C-0030`'s strain-softening flexure. */
@Serializable
data class CensusSofteningFold(
    val model: String,
    val layerHeight: Double,
    val concentration: Double,
    val loadLine: String,
    val pullInStroke: Double? = null,
    val bindingCeiling: String,
    val biasMargin: Double? = null
)

/** One `T-149` coupling row (`C-0084`) — the recommended element's own law. */
@Serializable
data class CensusRecommendedCoupling(
    val line: String,
    val kind: String,
    val pathCount: Int,
    val secantAcceptable: Double,
    val tangentAcceptable: Double,
    val minimumTangentTraversed: Double,
    val floorsClearedTwoMillimolar: Int
)

/** One `T-149` device row (`C-0084`) — a `(height, buffer, load line)` verdict. */
@Serializable
data class CensusRecommendedDevice(
    val device: String,
    val layerHeight: Double,
    val concentration: Double,
    val loadLine: String,
    val isTheRecommendedDevice: Boolean,
    val models: Int,
    val statesWithAFold: Int,
    val biasMarginMinimum: Double? = null,
    val biasMarginMaximum: Double? = null,
    val foldStrokeMinimum: Double? = null,
    val foldStrokeMaximum: Double? = null
)

// ---------------------------------------------------------------- the loader

/** Every result-file row `T-156` reads, loaded once. */
class BufferRouteInputs(
    val blockingThresholds: List<CensusBlockingThreshold>,
    val windowBiasClauses: List<CensusWindowBiasClause>,
    val windowStabilityClauses: List<CensusWindowStabilityClause>,
    val couplingRequirements: List<CensusCouplingRequirement>,
    val usableBiasCeilings: List<CensusUsableBiasCeiling>,
    val bufferComparisons: List<CensusBufferComparison>,
    val softeningFolds: List<CensusSofteningFold>,
    val recommendedCouplings: List<CensusRecommendedCoupling>,
    val recommendedDevices: List<CensusRecommendedDevice>
) {

    companion object {

        /** Reads every input from [directory], failing loudly if a file or a section is missing. */
        fun read(directory: File = File(GEN1_RESULTS_DIRECTORY)): BufferRouteInputs =
            BufferRouteInputs(
                blockingThresholds = File(directory, "T-3-stroke-and-blocking-force.json")
                    .rows("thresholds"),
                windowBiasClauses = File(directory, "T-2-design-window.json")
                    .rows("biasClauses"),
                windowStabilityClauses = File(directory, "T-2-design-window.json")
                    .rows("stabilityClauses"),
                couplingRequirements = File(directory, "T-16-output-coupling-stiffness.json")
                    .rows("requirements"),
                usableBiasCeilings = File(directory, "T-4-maximum-usable-bias.json")
                    .rows("ceilings"),
                bufferComparisons = File(directory, "T-25-window-resynthesis.json")
                    .rows("bufferComparison"),
                softeningFolds = File(directory, "T-76-softening-coupling-stability.json")
                    .rows("folds"),
                recommendedCouplings = File(directory, "T-149-recommended-element-fold.json")
                    .rows("couplings"),
                recommendedDevices = File(directory, "T-149-recommended-element-fold.json")
                    .rows("devices")
            )
    }
}

private inline fun <reified T> File.rows(section: String): List<T> {
    require(exists()) { "the census input $path does not exist" }
    val root = censusReader.parseToJsonElement(readText()).jsonObject
    val array = requireNotNull(root[section]) { "$path carries no '$section' section" }
    return array.jsonArray.map { censusReader.decodeFromJsonElement<T>(it) }
}

/** The one loaded copy — the census is read many times and the files do not change under it. */
val gen1BufferRouteInputs: BufferRouteInputs by lazy { BufferRouteInputs.read() }

// ---------------------------------------------------------------- the per-route readings

/**
 * `C-0012`'s force clause at the recommended device's height: the applied bias each
 * `(model, buffer)` needs for §3's 100 pN of **blocking** force, i.e. at **zero stroke**.
 *
 * The clause is `σ`-free and model-free by construction — `F_es` depends only on the tile, the
 * electrode, the buffer and the gap, and the gap *is* the layer height — and gate 5 asserts that
 * rather than assuming it.
 */
fun blockingBiasAtTenNanometres(): Map<Pair<String, Double>, Double> =
    gen1BufferRouteInputs.blockingThresholds
        .filter { it.layerHeight == CENSUS_LAYER_HEIGHT }
        .mapNotNull { row ->
            row.biasForHundredPiconewtonBlocking?.let { (row.model to row.concentration) to it }
        }
        .toMap()

/**
 * The cheap bound behind `C-0016`'s independence verdict: `T-2`'s own copy of the blocking bias
 * against `T-3`'s, at every one of the fifteen `(height, buffer)` states both files carry.
 */
fun blockingBiasTransferChecks(): List<RouteTransferCheck> {
    val upstream = gen1BufferRouteInputs.blockingThresholds
    return gen1BufferRouteInputs.windowBiasClauses.map { clause ->
        val matches = upstream.filter {
            it.layerHeight == clause.layerHeight && it.concentration == clause.concentration
        }
        require(matches.isNotEmpty()) {
            "T-3 carries no threshold at ${clause.layerHeight} nm, ${clause.concentration} mM"
        }
        val distinct = matches.map { it.biasForHundredPiconewtonBlocking }.distinct()
        require(distinct.size == 1) {
            "T-3's blocking bias is not model-free at ${clause.layerHeight} nm, " +
                    "${clause.concentration} mM: $distinct"
        }
        val there = distinct.single()
        RouteTransferCheck(
            quantity = "biasForHundredPiconewtonBlocking",
            state = "${clause.layerHeight} nm, ${clause.concentration} mM MgCl2",
            here = clause.biasForHundredPiconewtonBlocking,
            there = there,
            hereSource = "C-0016 / T-2 biasClauses",
            thereSource = "C-0012 / T-3 thresholds",
            departure = transferDeparture(clause.biasForHundredPiconewtonBlocking, there),
            transfer = isTransfer(
                clause.biasForHundredPiconewtonBlocking, there, EMITTED_FIELD_SLACK
            )
        )
    }
}

/**
 * The cheap bound behind `C-0027`'s independence verdict: `T-25`'s `bufferComparison` extrema
 * against the extrema of `T-16`'s `stabilityMargin` and `T-4`'s coupled `margin`, at every
 * `(height, buffer)` `T-25` reports.
 */
fun correctedMarginTransferChecks(): List<RouteTransferCheck> {
    val checks = mutableListOf<RouteTransferCheck>()
    gen1BufferRouteInputs.bufferComparisons.forEach { row ->
        val state = "${row.layerHeight} nm, ${row.concentration} mM MgCl2"
        val coupled = gen1BufferRouteInputs.usableBiasCeilings.filter {
            it.layerHeight == row.layerHeight && it.concentration == row.concentration &&
                    it.loadLine == "coupled" && it.margin != null
        }.mapNotNull { it.margin }
        if (coupled.isNotEmpty()) {
            checks += RouteTransferCheck(
                quantity = "biasMarginLow",
                state = state,
                here = row.biasMarginLow,
                there = coupled.min(),
                hereSource = "C-0027 / T-25 bufferComparison",
                thereSource = "C-0018 / T-4 ceilings (loadLine = coupled)",
                departure = transferDeparture(row.biasMarginLow, coupled.min()),
                transfer = isTransfer(row.biasMarginLow, coupled.min(), EMITTED_FIELD_SLACK)
            )
            checks += RouteTransferCheck(
                quantity = "biasMarginHigh",
                state = state,
                here = row.biasMarginHigh,
                there = coupled.max(),
                hereSource = "C-0027 / T-25 bufferComparison",
                thereSource = "C-0018 / T-4 ceilings (loadLine = coupled)",
                departure = transferDeparture(row.biasMarginHigh, coupled.max()),
                transfer = isTransfer(row.biasMarginHigh, coupled.max(), EMITTED_FIELD_SLACK)
            )
        }
        val margins = gen1BufferRouteInputs.couplingRequirements.filter {
            it.layerHeight == row.layerHeight && it.concentration == row.concentration
        }.mapNotNull { it.stabilityMargin }
        if (margins.isNotEmpty() && row.stiffnessMarginBaselineLow != null) {
            checks += RouteTransferCheck(
                quantity = "stiffnessMarginBaselineLow",
                state = state,
                here = row.stiffnessMarginBaselineLow,
                there = margins.min(),
                hereSource = "C-0027 / T-25 bufferComparison",
                thereSource = "C-0017 / T-16 requirements",
                departure = transferDeparture(row.stiffnessMarginBaselineLow, margins.min()),
                transfer = isTransfer(
                    row.stiffnessMarginBaselineLow, margins.min(), EMITTED_FIELD_SLACK
                )
            )
            checks += RouteTransferCheck(
                quantity = "stiffnessMarginBaselineHigh",
                state = state,
                here = row.stiffnessMarginBaselineHigh,
                there = margins.max(),
                hereSource = "C-0027 / T-25 bufferComparison",
                thereSource = "C-0017 / T-16 requirements",
                departure = transferDeparture(row.stiffnessMarginBaselineHigh, margins.max()),
                transfer = isTransfer(
                    row.stiffnessMarginBaselineHigh, margins.max(), EMITTED_FIELD_SLACK
                )
            )
        }
    }
    return checks
}

/**
 * `C-0069`'s `Q5` tangent minimum over the traversed `[0, 3 nm]` in pN/nm, read from `T-149` —
 * the stiffness a stability requirement is owed on (`C-0049`), and **not** the mandated secant.
 */
fun recommendedTangentMinimum(): Double = gen1BufferRouteInputs.recommendedCouplings
    .single { it.kind != "affine mandate" }
    .minimumTangentTraversed

/** `C-0017`'s stability floors at 10 nm, keyed on `(model, buffer)` — element-**independent**. */
fun stabilityFloorsAtTenNanometres(): Map<Pair<String, Double>, Double> =
    gen1BufferRouteInputs.couplingRequirements
        .filter {
            it.layerHeight == CENSUS_LAYER_HEIGHT &&
                    it.concentration in listOf(CENSUS_LOW_SALT, CENSUS_HIGH_SALT)
        }
        .associate { (it.model to it.concentration) to it.stabilityFloor }

/**
 * The force clause read at the state the **device occupies**: per layer model, the ratio of the
 * bias that holds 100 pN at `L₀ − 3 nm` in 2 mM to the same bias in 0.5 mM.
 *
 * `C-0012`'s 4.97× is the same clause at **zero** stroke. `CLAUDE.md`'s discipline — *a quantity is
 * not well posed without the state it is read at* — applied to a buffer advantage.
 */
fun heldOperatingBiasAdvantageAtTenNanometres(): Map<String, Double> {
    val rows = gen1BufferRouteInputs.couplingRequirements
        .filter { it.layerHeight == CENSUS_LAYER_HEIGHT }
    val low = rows.filter { it.concentration == CENSUS_LOW_SALT }
        .associate { it.model to it.simultaneousTargetBias }
    val high = rows.filter { it.concentration == CENSUS_HIGH_SALT }
        .associate { it.model to it.simultaneousTargetBias }
    require(low.keys == high.keys) { "T-16 does not carry the same models at both buffers" }
    return low.mapValues { (model, atLow) ->
        bufferAdvantage(atLow, high.getValue(model), smallerIsBetter = true)
    }
}

/** The worst (over models) coupled bias margin `C-0018` reports at 10 nm and [concentration]. */
private fun worstAffineBiasMargin(concentration: Double): Double =
    gen1BufferRouteInputs.usableBiasCeilings
        .filter {
            it.layerHeight == CENSUS_LAYER_HEIGHT && it.concentration == concentration &&
                    it.loadLine == "coupled"
        }
        .mapNotNull { it.margin }
        .min()

/** The worst (over models) `C-0017` stability margin at 10 nm and [concentration]. */
private fun worstStabilityMargin(concentration: Double): Double =
    gen1BufferRouteInputs.couplingRequirements
        .filter {
            it.layerHeight == CENSUS_LAYER_HEIGHT && it.concentration == concentration
        }
        .mapNotNull { it.stabilityMargin }
        .min()

/** The worst corrected stiffness margin `C-0027` reports at 10 nm and [concentration]. */
private fun worstCorrectedMargin(concentration: Double): Double =
    requireNotNull(
        gen1BufferRouteInputs.bufferComparisons
            .single {
                it.layerHeight == CENSUS_LAYER_HEIGHT && it.concentration == concentration
            }
            .stiffnessMarginCorrectedLow
    ) { "T-25 carries no corrected stiffness margin at $concentration mM" }

/** The worst `C-0032` bias margin on `C-0030`'s favourable mounting at 10 nm and [concentration]. */
private fun worstSofteningBiasMargin(concentration: Double): Double =
    gen1BufferRouteInputs.softeningFolds
        .filter {
            it.layerHeight == CENSUS_LAYER_HEIGHT && it.concentration == concentration &&
                    it.loadLine.startsWith("L3")
        }
        .mapNotNull { it.biasMargin }
        .min()

/** The worst bias margin `C-0084` reports for the recommended arm at 10 nm and [concentration]. */
fun recommendedArmBiasMargin(concentration: Double): Double =
    requireNotNull(
        gen1BufferRouteInputs.recommendedDevices
            .single {
                it.layerHeight == CENSUS_LAYER_HEIGHT && it.concentration == concentration &&
                        it.loadLine.startsWith("LQ5")
            }
            .biasMarginMinimum
    ) { "T-149 carries no recommended-arm bias margin at $concentration mM" }

// ---------------------------------------------------------------- the census itself

/**
 * The six named 0.5 mM routes, classified — `T-156`'s `P1`.
 *
 * Every reading is pulled from the emitting study's own result file; every verdict is a judgement
 * and carries its ground in words. The **order is fixed by declaration** so that the census is
 * reproducible without a sort over a map (`CLAUDE.md`).
 */
fun gen1BufferRouteCensus(): List<BufferRoute> {
    val tangent = recommendedTangentMinimum()
    val blockingLow = blockingBiasAtTenNanometres().filterKeys { it.second == CENSUS_LOW_SALT }
        .values.distinct().single()
    val blockingHigh = blockingBiasAtTenNanometres().filterKeys { it.second == CENSUS_HIGH_SALT }
        .values.distinct().single()
    val windowLow = gen1BufferRouteInputs.windowBiasClauses.single {
        it.layerHeight == CENSUS_LAYER_HEIGHT && it.concentration == CENSUS_LOW_SALT
    }.biasForHundredPiconewtonBlocking!!
    val windowHigh = gen1BufferRouteInputs.windowBiasClauses.single {
        it.layerHeight == CENSUS_LAYER_HEIGHT && it.concentration == CENSUS_HIGH_SALT
    }.biasForHundredPiconewtonBlocking!!
    val heldAdvantage = heldOperatingBiasAdvantageAtTenNanometres()
    return listOf(
        BufferRoute(
            claim = "C-0012",
            clause = "the force clause",
            comparedQuantity = "the applied bias 100 pN of BLOCKING force needs at 10 nm",
            readAt = "zero stroke, h = L0 = 10 nm; at the DEVICE's held gap the same clause is " +
                    "%.4f-%.4f x".format(heldAdvantage.values.min(), heldAdvantage.values.max()),
            obj = RouteObject.UNLOADED_FIELD_BALANCE,
            objectStillInTheDesign = true,
            lowSaltReading = blockingLow,
            highSaltReading = blockingHigh,
            smallerIsBetter = true,
            advantage = bufferAdvantage(blockingLow, blockingHigh, smallerIsBetter = true),
            verdict = RouteVerdict.SURVIVES_SAME_GROUND,
            ground = "the object contains no coupling element at all - the blocking balance is " +
                    "tile, field and layer - so nothing CH-0081 / C-0069 / C-0071 did can reach " +
                    "it. But the 4.97x is a ZERO-STROKE reading: at the held operating point the " +
                    "device occupies it is 1.48-1.57x, and the two are the same clause",
            independence = RouteIndependence.INDEPENDENT,
            transferOf = emptyList(),
            provenance = "T-3 thresholds[layerHeight = 10]"
        ),
        BufferRoute(
            claim = "C-0016",
            clause = "the bias window",
            comparedQuantity = "the same applied bias, re-intersected over a sigma grid",
            readAt = "zero stroke, reading A (the FREE operating point)",
            obj = RouteObject.TRANSFERRED_READINGS,
            objectStillInTheDesign = true,
            lowSaltReading = windowLow,
            highSaltReading = windowHigh,
            smallerIsBetter = true,
            advantage = bufferAdvantage(windowLow, windowHigh, smallerIsBetter = true),
            verdict = RouteVerdict.SURVIVES_SAME_GROUND,
            ground = "not a second derivation: T-2's biasClauses carry T-3's own number at 15 of " +
                    "15 states, and the binding clause at 10 nm is literally 'force'. C-0016's " +
                    "OTHER half - its (f) stability count at a FIXED applied bias - runs the " +
                    "other way, asking 86.1-110.0 pN/nm of coupling at 0.25 V in 0.5 mM against " +
                    "47.6-71.5 in 2 mM",
            independence = RouteIndependence.TRANSFER,
            transferOf = listOf("C-0012"),
            provenance = "T-2 biasClauses[layerHeight = 10] against T-3 thresholds"
        ),
        BufferRoute(
            claim = "C-0017",
            clause = "the stability floor",
            comparedQuantity = "the worst static stability margin k_c/|k_eff| at the held point",
            readAt = "the held operating point, L0 - 3 nm; k_c is the MANDATED SECANT 33.3333",
            obj = RouteObject.HELD_OPERATING_POINT,
            objectStillInTheDesign = true,
            lowSaltReading = worstStabilityMargin(CENSUS_LOW_SALT),
            highSaltReading = worstStabilityMargin(CENSUS_HIGH_SALT),
            smallerIsBetter = false,
            advantage = bufferAdvantage(
                worstStabilityMargin(CENSUS_LOW_SALT),
                worstStabilityMargin(CENSUS_HIGH_SALT),
                smallerIsBetter = false
            ),
            verdict = RouteVerdict.SURVIVES_SAME_GROUND,
            ground = "the FLOOR |k_eff| is a property of the layer, the field and the held gap " +
                    "and contains no coupling element, so it does not move at all. Only the " +
                    "numerator does: C-0049 reads stability on the TANGENT, and Q5's minimum " +
                    "over [0, 3] is %.6f pN/nm, so every margin scales by %.8f and the ADVANTAGE "
                        .format(tangent, tangent / GEN1_MANDATED_SECANT) +
                    "is unchanged. Q5 clears the 2 mM floors at 6 of 6",
            independence = RouteIndependence.INDEPENDENT,
            transferOf = emptyList(),
            provenance = "T-16 requirements[layerHeight = 10]"
        ),
        BufferRoute(
            claim = "C-0018",
            clause = "the usable bias",
            comparedQuantity = "the worst coupled bias margin (usable bias over operating bias)",
            readAt = "the affine mandate's equilibrium path, placed at 100 pN over 3 nm",
            obj = RouteObject.AFFINE_MANDATE_PATH,
            objectStillInTheDesign = false,
            lowSaltReading = worstAffineBiasMargin(CENSUS_LOW_SALT),
            highSaltReading = worstAffineBiasMargin(CENSUS_HIGH_SALT),
            smallerIsBetter = false,
            advantage = bufferAdvantage(
                worstAffineBiasMargin(CENSUS_LOW_SALT),
                worstAffineBiasMargin(CENSUS_HIGH_SALT),
                smallerIsBetter = false
            ),
            verdict = RouteVerdict.SURVIVES_DIFFERENT_GROUND,
            ground = "C-0018's stated ground is that '0.5 mM removes the fold entirely'. On Q5 " +
                    "there is NO FOLD AT 2 mM EITHER (C-0084, 0 of 6), so there is nothing for " +
                    "0.5 mM to remove and the ground is void. What survives is a quantified " +
                    "PREFERENCE on the same axis: %.4f against %.4f, a factor of %.4f"
                        .format(
                            recommendedArmBiasMargin(CENSUS_LOW_SALT),
                            recommendedArmBiasMargin(CENSUS_HIGH_SALT),
                            recommendedArmBiasMargin(CENSUS_LOW_SALT) /
                                    recommendedArmBiasMargin(CENSUS_HIGH_SALT)
                        ),
            independence = RouteIndependence.INDEPENDENT,
            transferOf = emptyList(),
            provenance = "T-4 ceilings[layerHeight = 10, loadLine = coupled]; re-read on Q5 " +
                    "from T-149 devices"
        ),
        BufferRoute(
            claim = "C-0027",
            clause = "the corrected margin",
            comparedQuantity = "the worst iteration-4-corrected stability margin",
            readAt = "the held operating point, corrected by C-0019 and C-0022",
            obj = RouteObject.TRANSFERRED_READINGS,
            objectStillInTheDesign = true,
            lowSaltReading = worstCorrectedMargin(CENSUS_LOW_SALT),
            highSaltReading = worstCorrectedMargin(CENSUS_HIGH_SALT),
            smallerIsBetter = false,
            advantage = bufferAdvantage(
                worstCorrectedMargin(CENSUS_LOW_SALT),
                worstCorrectedMargin(CENSUS_HIGH_SALT),
                smallerIsBetter = false
            ),
            verdict = RouteVerdict.SURVIVES_SAME_GROUND,
            ground = "not a fifth route: T-25's bufferComparison carries C-0017's stabilityMargin " +
                    "extrema and C-0018's coupled margin extrema unchanged, to the emission " +
                    "precision, at every state it reports. It is C-0017 and C-0018 with two " +
                    "corrections applied, and its survival is theirs",
            independence = RouteIndependence.TRANSFER,
            transferOf = listOf("C-0017", "C-0018"),
            provenance = "T-25 bufferComparison[layerHeight = 10] against T-16 and T-4"
        ),
        BufferRoute(
            claim = "C-0032",
            clause = "the realised coupling law",
            comparedQuantity = "the worst bias margin on C-0030's favourable mounting",
            readAt = "C-0030's strain-SOFTENING coupled-standoff flexure, placed at 100 pN / 3 nm",
            obj = RouteObject.SOFTENING_FLEXURE_PATH,
            objectStillInTheDesign = false,
            lowSaltReading = worstSofteningBiasMargin(CENSUS_LOW_SALT),
            highSaltReading = worstSofteningBiasMargin(CENSUS_HIGH_SALT),
            smallerIsBetter = false,
            advantage = bufferAdvantage(
                worstSofteningBiasMargin(CENSUS_LOW_SALT),
                worstSofteningBiasMargin(CENSUS_HIGH_SALT),
                smallerIsBetter = false
            ),
            verdict = RouteVerdict.WITHDRAWN,
            ground = "CH-0098. The element was removed from the output role by CH-0081 / C-0069 " +
                    "and C-0071 does not recommend it. Every number C-0032 reports still stands " +
                    "for the element it was measured on; none of it transfers to Q5, which " +
                    "strain-STIFFENS and does not fold at 2 mM at any layer model",
            independence = RouteIndependence.INDEPENDENT,
            transferOf = emptyList(),
            provenance = "T-76 folds[layerHeight = 10, loadLine = L3 coupled favourable]"
        )
    )
}
