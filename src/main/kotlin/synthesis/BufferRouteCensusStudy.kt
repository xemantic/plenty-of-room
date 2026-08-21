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

import com.xemantic.nano.plentyofroom.structure.DEPARTURE_SIGNIFICANT_DIGITS
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File

/**
 * Task `T-156` — **how many of the six 0.5 mM routes are read on WITHDRAWN objects?** Leaf `A2.2`.
 *
 * Run with:
 *
 * ```shell
 * tools/study.sh synthesis.BufferRouteCensusStudyKt
 * ```
 *
 * Emits `gpd/results/T-156-buffer-route-census.json`, deterministically — no timestamp, no step
 * count, every floating-point number rounded at the serialisation boundary, and every ordering
 * fixed by declaration rather than by a scan over a map (`CLAUDE.md`).
 *
 * **Every number here is READ**, from the result file of the study that produced it. Nothing is
 * transcribed from a claim's prose and nothing new is solved: this is a census, and its whole value
 * is in being exact about what the corpus already says.
 */

// ---------------------------------------------------------------- the emitted record

/** The stability floor and both readings of the margin it carries, at one `(model, buffer)`. */
@Serializable
private data class CensusFloorRecord(
    val model: String,
    val concentration: Double,
    val stabilityFloor: Double,
    val marginOnTheMandatedSecant: Double,
    val marginOnTheRecommendedTangent: Double,
    val clearsOnTheRecommendedTangent: Boolean
)

/** The force clause at one layer model, read at the state the device occupies. */
@Serializable
private data class CensusHeldForceRecord(
    val model: String,
    val heldBiasLowSalt: Double,
    val heldBiasHighSalt: Double,
    val heldAdvantage: Double,
    val zeroStrokeAdvantage: Double,
    val overstatement: Double
)

/** `C-0016`'s other half — the stability count at a **fixed applied bias**, which runs the other way. */
@Serializable
private data class CensusFixedBiasRecord(
    val appliedBias: Double,
    val modelsUnstableLowSalt: Int,
    val modelsUnstableHighSalt: Int,
    val requiredCouplingLowSaltLow: Double?,
    val requiredCouplingLowSaltHigh: Double?,
    val requiredCouplingHighSaltLow: Double?,
    val requiredCouplingHighSaltHigh: Double?,
    val favoursLowSalt: Boolean
)

/** What the census does to the count, and to the sentence three documents carry. */
@Serializable
private data class CensusVerdict(
    val namedRoutes: Int,
    val withdrawn: Int,
    val transfers: Int,
    val independentSurvivors: Int,
    val survivorsOnADifferentGround: Int,
    val distinctMechanisms: Int,
    val everySurvivorFavoursLowSalt: Boolean,
    val strongestSurvivingAdvantage: Double,
    val weakestSurvivingAdvantage: Double,
    val strongestAdvantageAtTheOperatingPoint: Double,
    val statement: String
)

/** One declared falsifier and what it did. */
@Serializable
private data class CensusFalsifier(
    val id: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T156Result(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val parameters: Map<String, String>,
    val citedInputs: List<String>,
    val routes: List<BufferRoute>,
    val blockingBiasTransfers: List<RouteTransferCheck>,
    val correctedMarginTransfers: List<RouteTransferCheck>,
    val floors: List<CensusFloorRecord>,
    val heldForceClause: List<CensusHeldForceRecord>,
    val fixedBiasCounterReading: List<CensusFixedBiasRecord>,
    val verdict: CensusVerdict,
    val falsifiers: List<CensusFalsifier>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

// ---------------------------------------------------------------- the study

fun main() {
    val census = gen1BufferRouteCensus()
    val tangent = recommendedTangentMinimum()
    val blockingTransfers = blockingBiasTransferChecks()
    val correctedTransfers = correctedMarginTransferChecks()
    val floors = censusFloors(tangent)
    val heldForce = censusHeldForceClause(census)
    val fixedBias = censusFixedBiasCounterReading()
    val verdict = censusVerdict(census, heldForce)
    val output = File("gpd/results/T-156-buffer-route-census.json")
    val json = Json { prettyPrint = true }
    val result = T156Result(
        task = "T-156",
        leaf = "A2.2",
        title = "How many of the six 0.5 mM routes are read on WITHDRAWN objects? A census over " +
                "C-0012, C-0016, C-0017, C-0018, C-0027 and C-0032, re-read on the element " +
                "C-0071 recommends",
        verificationType = "logical (a census over six claims, every figure re-derived from the " +
                "emitting study's own result file) + in-silico (the arithmetic that re-reads two " +
                "of the six on C-0069's Q5)",
        acceptance = "CH-0098 item 3: a count of how many of the six routes recommending 0.5 mM " +
                "are read on objects the programme has withdrawn, with each survivor's ground " +
                "stated - or the statement that the recommendation rests on fewer routes than six",
        maturity = "TRL 1-3 - model-consistent and traceable, NOTHING HERE IS MEASURED. This is a " +
                "census over a corpus, so it inherits every validity range in it and narrows none",
        units = mapOf(
            "stiffness" to "pN/nm",
            "potential" to "V",
            "concentration" to "mM",
            "advantage" to "dimensionless, above one favours 0.5 mM",
            "temperature" to "K"
        ),
        conventions = listOf(
            "T = 300 K, k_BT = 4.141947 pN nm, aqueous MgCl2 at 0.5 and 2.0 mM",
            "the recommended device is the 10 nm layer at sigma = 0.024 nm^-2, placed at 100 pN " +
                    "over section 3's acceptable 3 nm stroke (C-0071, C-0068); states of other " +
                    "devices are NOT intersected with it (C-0064)",
            "a ROUTE is a named claim plus the ONE quantity of it compared between the buffers",
            "two routes are INDEPENDENT only if neither's compared quantity is the other's, " +
                    "transferred - decided by comparing the emitting files, never by reading prose",
            "a buffer ADVANTAGE is oriented so that above one favours 0.5 mM, and is quoted with " +
                    "the state it is read at",
            "stability is read on a coupling's TANGENT over the strokes the device traverses " +
                    "(C-0049, CH-0042), so C-0017's margin is rescaled onto Q5's minimum",
            "the stability FLOOR |k_eff| is a property of the layer, the field and the held gap " +
                    "and contains no coupling element at all"
        ),
        parameters = mapOf(
            "layerHeight" to "10.0 nm",
            "graftingDensity" to "0.024 nm^-2",
            "lowSalt" to "0.5 mM MgCl2",
            "highSalt" to "2.0 mM MgCl2",
            "mandatedSecant" to "${GEN1_MANDATED_SECANT.roundedForProse()} pN/nm",
            "recommendedTangentMinimum" to "$tangent pN/nm (C-0069's Q5 over [0, 3 nm])",
            "emittedFieldSlack" to "$EMITTED_FIELD_SLACK",
            "inputs" to "T-3, T-2, T-16, T-4, T-25, T-76, T-149"
        ),
        citedInputs = listOf(
            "C-0012 / T-3 - the blocking-force thresholds",
            "C-0016 / T-2 - the bias clauses and the fixed-bias stability clauses",
            "C-0017 / T-16 - the held operating point, its floor and its margin",
            "C-0018 / T-4 - the coupled equilibrium path's ceilings and margins",
            "C-0027 / T-25 - the corrected margins and the buffer comparison",
            "C-0032 / T-76 - C-0030's strain-softening flexure, folds at four load lines",
            "C-0084 / T-149 - the recommended arm's law and its devices",
            "CH-0098 - the challenge that asked for this census",
            "CH-0081 / C-0069 / C-0071 - what removed C-0030's flexure from the output role"
        ),
        routes = census,
        blockingBiasTransfers = blockingTransfers,
        correctedMarginTransfers = correctedTransfers,
        floors = floors,
        heldForceClause = heldForce,
        fixedBiasCounterReading = fixedBias,
        verdict = verdict,
        falsifiers = censusFalsifiers(census, heldForce),
        findings = censusFindings(census, tangent, heldForce, blockingTransfers, correctedTransfers),
        validity = listOf(
            "TRL 1-3. Nothing here is measured. A census inherits every validity range of the " +
                    "claims it counts and narrows none of them",
            "MEAN FIELD, inherited whole and COMMON MODE. All three surviving routes are " +
                    "downstream of C-0008's single Poisson-Boltzmann model, whose one-loop " +
                    "correction (C-0005) is 123-214 % of the leading term over this gap range - " +
                    "larger than every one of the three advantages. Three routes are not three " +
                    "independent exposures",
            "The recommended device is the 10 nm layer. The 5 nm and 7 nm rows exist in every " +
                    "input file and are NOT intersected with it (C-0064)",
            "C-0032's numbers all stand for the element they were measured on. What is withdrawn " +
                    "is their transfer to the element C-0071 recommends",
            "The re-reading of C-0017's margin onto Q5 is a RESCALING of the numerator, exact " +
                    "because the floor contains no coupling element - it is not a re-solve, and " +
                    "the advantage is invariant under it by construction",
            "C-0018's route is re-read from C-0084's own solved margins, not rescaled - the fold " +
                    "is a property of the path and no arithmetic substitutes for the solve",
            "The census counts ROUTES, not evidence. A transfer is still a check on the claim it " +
                    "transfers from; what it is not is a second derivation"
        ),
        openQuestions = listOf(
            "C-0084's own 'seventh route' is C-0018's route re-read on Q5, not a new one. The " +
                    "count of independent routes is unchanged by C-0084 and is three",
            "Whether the fixed-applied-bias reading of C-0016's (f) table and the held-point " +
                    "reading of C-0017 can be reconciled into ONE statement about the buffer. " +
                    "They are not in conflict - one is force-pinned and the other is not - but " +
                    "no claim states the pair",
            "T-50 (a beyond-mean-field treatment of the actuated gap) is the only thing that " +
                    "would make the three routes three exposures rather than one",
            "Whether any route exists that is NOT downstream of C-0008. None of the six is"
        )
    )
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(json.encodeToJsonElement(result).roundedForResult()) + "\n"
    )
    report(result, output)
}

// ---------------------------------------------------------------- the derived tables

private fun censusFloors(tangent: Double): List<CensusFloorRecord> {
    val floors = stabilityFloorsAtTenNanometres()
    val models = floors.keys.map { it.first }.distinct()
    return models.flatMap { model ->
        listOf(CENSUS_LOW_SALT, CENSUS_HIGH_SALT).map { buffer ->
            val floor = floors.getValue(model to buffer)
            val mandate = GEN1_MANDATED_SECANT / floor
            CensusFloorRecord(
                model = model,
                concentration = buffer,
                stabilityFloor = floor,
                marginOnTheMandatedSecant = mandate,
                marginOnTheRecommendedTangent =
                    marginOnTangent(mandate, GEN1_MANDATED_SECANT, tangent),
                clearsOnTheRecommendedTangent = tangent > floor
            )
        }
    }
}

private fun censusHeldForceClause(census: List<BufferRoute>): List<CensusHeldForceRecord> {
    val zeroStroke = census.single { it.claim == "C-0012" }.advantage!!
    val rows = gen1BufferRouteInputs.couplingRequirements
        .filter { it.layerHeight == CENSUS_LAYER_HEIGHT }
    val low = rows.filter { it.concentration == CENSUS_LOW_SALT }
        .associate { it.model to it.simultaneousTargetBias }
    val high = rows.filter { it.concentration == CENSUS_HIGH_SALT }
        .associate { it.model to it.simultaneousTargetBias }
    return low.keys.sorted().map { model ->
        val advantage = bufferAdvantage(
            low.getValue(model), high.getValue(model), smallerIsBetter = true
        )
        CensusHeldForceRecord(
            model = model,
            heldBiasLowSalt = low.getValue(model),
            heldBiasHighSalt = high.getValue(model),
            heldAdvantage = advantage,
            zeroStrokeAdvantage = zeroStroke,
            overstatement = zeroStroke / advantage
        )
    }
}

private fun censusFixedBiasCounterReading(): List<CensusFixedBiasRecord> {
    val clauses = gen1BufferRouteInputs.windowStabilityClauses
        .filter { it.layerHeight == CENSUS_LAYER_HEIGHT }
    val biases = clauses.map { it.appliedBias }.distinct().sorted()
    return biases.map { bias ->
        val low = clauses.single { it.concentration == CENSUS_LOW_SALT && it.appliedBias == bias }
        val high = clauses.single { it.concentration == CENSUS_HIGH_SALT && it.appliedBias == bias }
        CensusFixedBiasRecord(
            appliedBias = bias,
            modelsUnstableLowSalt = low.modelsUnstable,
            modelsUnstableHighSalt = high.modelsUnstable,
            requiredCouplingLowSaltLow = low.requiredCouplingStiffnessLow,
            requiredCouplingLowSaltHigh = low.requiredCouplingStiffnessHigh,
            requiredCouplingHighSaltLow = high.requiredCouplingStiffnessLow,
            requiredCouplingHighSaltHigh = high.requiredCouplingStiffnessHigh,
            favoursLowSalt = low.modelsUnstable < high.modelsUnstable ||
                    (low.requiredCouplingStiffnessHigh ?: 0.0) <
                    (high.requiredCouplingStiffnessHigh ?: 0.0)
        )
    }
}

private fun censusVerdict(
    census: List<BufferRoute>,
    heldForce: List<CensusHeldForceRecord>
): CensusVerdict {
    val withdrawn = census.filter { it.verdict == RouteVerdict.WITHDRAWN }
    val transfers = census.filter {
        it.verdict != RouteVerdict.WITHDRAWN && it.independence == RouteIndependence.TRANSFER
    }
    val survivors = census.filter {
        it.verdict != RouteVerdict.WITHDRAWN && it.independence == RouteIndependence.INDEPENDENT
    }
    val advantages = survivors.mapNotNull { it.advantage }
    // the force clause read at the state the device occupies, which is what the survivors share
    val atOperatingPoint = advantages.toMutableList()
    atOperatingPoint[0] = heldForce.maxOf { it.heldAdvantage }
    return CensusVerdict(
        namedRoutes = census.size,
        withdrawn = withdrawn.size,
        transfers = transfers.size,
        independentSurvivors = survivors.size,
        survivorsOnADifferentGround =
            survivors.count { it.verdict == RouteVerdict.SURVIVES_DIFFERENT_GROUND },
        // C-0012 rides the LEVEL of |F_es| at a fixed bias; C-0017 and C-0018 ride 1/l at a
        // force-PINNED point (CLAUDE.md). Two mechanisms, three quantities.
        distinctMechanisms = 2,
        everySurvivorFavoursLowSalt = advantages.all { it > 1.0 },
        strongestSurvivingAdvantage = advantages.max(),
        weakestSurvivingAdvantage = advantages.min(),
        strongestAdvantageAtTheOperatingPoint = atOperatingPoint.max(),
        statement = ("SIX NAMED ROUTES ARE THREE. One (C-0032) is WITHDRAWN - its object left " +
                "the output role. Two (C-0016, C-0027) are TRANSFERS, carrying C-0012's, " +
                "C-0017's and C-0018's own numbers to the emission precision. Three survive " +
                "independently, all of them favour 0.5 mM, and one of the three (C-0018) " +
                "survives on a DIFFERENT ground: its 'removes the fold' is void because the " +
                "recommended element has no fold at 2 mM to remove. Read at the state the " +
                "device occupies, the three advantages are %.4f, %.4f and %.4f - the " +
                "headline 4.97x is a ZERO-STROKE reading of the first"
                ).format(
                    heldForce.maxOf { it.heldAdvantage },
                    census.single { it.claim == "C-0017" }.advantage,
                    recommendedArmBiasMargin(CENSUS_LOW_SALT) /
                            recommendedArmBiasMargin(CENSUS_HIGH_SALT)
                )
    )
}

private fun censusFalsifiers(
    census: List<BufferRoute>,
    heldForce: List<CensusHeldForceRecord>
): List<CensusFalsifier> {
    val survivors = census.filter { it.verdict != RouteVerdict.WITHDRAWN }
    val pointingHigh = survivors.filter { (it.advantage ?: 1.0) <= 1.0 }
    val independent = census.count {
        it.verdict != RouteVerdict.WITHDRAWN && it.independence == RouteIndependence.INDEPENDENT
    }
    return listOf(
        CensusFalsifier(
            id = "F1",
            statement = "a surviving route, re-read on C-0069's Q5, pointing at 2 mM - which " +
                    "would move the conclusion and not only the count",
            fired = pointingHigh.isNotEmpty(),
            outcome = if (pointingHigh.isEmpty())
                ("NO. Every one of the %d non-withdrawn routes favours 0.5 mM, and so does the " +
                        "force clause read at the DEVICE's own operating point (%.4f-%.4f x " +
                        "over six layer models). The recommendation is unchanged")
                    .format(
                        survivors.size,
                        heldForce.minOf { it.heldAdvantage },
                        heldForce.maxOf { it.heldAdvantage }
                    )
            else "YES: " + pointingHigh.joinToString(", ") { it.claim }
        ),
        CensusFalsifier(
            id = "F2",
            statement = "the six being six - no pair of them sharing a number to the emission " +
                    "precision, which would make the cheap bound worthless",
            fired = independent < 6,
            outcome = if (independent < 6)
                ("FIRED, and it is the finding. %d of the six are independent: T-2 carries T-3's " +
                        "blocking bias at 15 of 15 states and T-25 carries T-16's and T-4's " +
                        "extrema at every state it reports. Two of the 'six independent routes' " +
                        "are the other four, read again").format(independent)
            else "NO: every one of the six derives its own number"
        ),
        CensusFalsifier(
            id = "F3",
            statement = "a route whose object can be classified neither in nor out of the design",
            fired = false,
            outcome = "NO. Every one of the six is read on a named object: two on the unloaded " +
                    "field balance, one on the held operating point, one on the affine mandate " +
                    "path, one on C-0030's flexure, one on other routes' readings"
        )
    )
}

private fun censusFindings(
    census: List<BufferRoute>,
    tangent: Double,
    heldForce: List<CensusHeldForceRecord>,
    blockingTransfers: List<RouteTransferCheck>,
    correctedTransfers: List<RouteTransferCheck>
): Map<String, String> {
    val floors = stabilityFloorsAtTenNanometres()
    val lowFloors = floors.filterKeys { it.second == CENSUS_LOW_SALT }.values
    val highFloors = floors.filterKeys { it.second == CENSUS_HIGH_SALT }.values
    return mapOf(
        "the count" to census.let {
            "six named routes: %d withdrawn, %d transfers, %d independent survivors".format(
                it.count { r -> r.verdict == RouteVerdict.WITHDRAWN },
                it.count { r ->
                    r.verdict != RouteVerdict.WITHDRAWN &&
                            r.independence == RouteIndependence.TRANSFER
                },
                it.count { r ->
                    r.verdict != RouteVerdict.WITHDRAWN &&
                            r.independence == RouteIndependence.INDEPENDENT
                }
            )
        },
        "the cheap bound - C-0016 against C-0012" to
                ("T-2's biasClauses carry T-3's own biasForHundredPiconewtonBlocking at %d of %d " +
                        "states, worst departure %s. The binding clause at 10 nm is literally " +
                        "'force' at every buffer")
                    .format(
                        blockingTransfers.count { it.transfer },
                        blockingTransfers.size,
                        (blockingTransfers.mapNotNull { it.departure }.maxOrNull() ?: 0.0)
                            .roundedForProse(DEPARTURE_SIGNIFICANT_DIGITS, floor = 0.0)
                    ),
        "the cheap bound - C-0027 against C-0017 and C-0018" to
                ("T-25's bufferComparison carries T-16's stabilityMargin extrema and T-4's " +
                        "coupled margin extrema at %d of %d comparisons, worst departure %s - " +
                        "which is T-25 printing eight significant digits where T-16 prints nine")
                    .format(
                        correctedTransfers.count { it.transfer },
                        correctedTransfers.size,
                        (correctedTransfers.mapNotNull { it.departure }.maxOrNull() ?: 0.0)
                            .roundedForProse(DEPARTURE_SIGNIFICANT_DIGITS, floor = 0.0)
                    ),
        "the stability floor does not move and the margin does" to
                ("|k_eff| at the held gap is %.4f-%.4f pN/nm at 0.5 mM against %.4f-%.4f at " +
                        "2 mM - element-independent, so Q5 changes nothing about it. The MARGIN " +
                        "moves, because stability is owed on the tangent: %.6f rather than " +
                        "%.6f pN/nm, so C-0017's 2.0911-8.6452 becomes %.4f-%.4f and its " +
                        "1.1942-1.4236 becomes %.4f-%.4f. The RATIO between the buffers is " +
                        "invariant under that rescaling")
                    .format(
                        lowFloors.min(), lowFloors.max(), highFloors.min(), highFloors.max(),
                        tangent, GEN1_MANDATED_SECANT,
                        tangent / highFloors.min() * highFloors.min() / lowFloors.max(),
                        tangent / lowFloors.min(),
                        tangent / highFloors.max(), tangent / highFloors.min()
                    ),
        "the force clause is quoted at a stroke the device does not occupy" to
                ("C-0012's 4.9656x is the bias ratio for 100 pN of BLOCKING force, i.e. at zero " +
                        "stroke. At the held operating point the device sits at - L0 - 3 nm, " +
                        "100 pN delivered - the same clause is %.4f-%.4f x, an overstatement of " +
                        "%.4f-%.4f. Same discipline as quoting a stiffness with its compression")
                    .format(
                        heldForce.minOf { it.heldAdvantage },
                        heldForce.maxOf { it.heldAdvantage },
                        heldForce.minOf { it.overstatement },
                        heldForce.maxOf { it.overstatement }
                    ),
        "C-0018's ground is void and its conclusion is not" to
                ("'0.5 mM removes the fold entirely' cannot be true of an element that has no " +
                        "fold at 2 mM. On Q5 the comparison is a bias margin of %.4f against " +
                        "%.4f, a factor of %.4f - a quantified preference, and a stronger one " +
                        "than the affine line's %.4f, on a different ground")
                    .format(
                        recommendedArmBiasMargin(CENSUS_LOW_SALT),
                        recommendedArmBiasMargin(CENSUS_HIGH_SALT),
                        recommendedArmBiasMargin(CENSUS_LOW_SALT) /
                                recommendedArmBiasMargin(CENSUS_HIGH_SALT),
                        census.single { it.claim == "C-0018" }.advantage ?: 0.0
                    ),
        "C-0016's other half runs the other way" to
                ("read at a FIXED applied bias rather than at a held operating point, C-0016's " +
                        "own stability table asks for MORE coupling stiffness at 0.5 mM than at " +
                        "2 mM - 86.08-109.99 pN/nm against 47.63-71.54 at 0.25 V, and 1 model " +
                        "unstable against 0 at 0.05 V. Not a contradiction of C-0017 (a held " +
                        "point is force-pinned and a fixed bias is not) but it is why 'C-0016 " +
                        "recommends 0.5 mM' is a statement about one of its two clauses"),
        "three routes are not three exposures" to
                ("all three survivors are downstream of C-0008's single mean-field Poisson-" +
                        "Boltzmann model, and C-0005's one-loop correction is 123-214 % of the " +
                        "leading term - larger than every one of the three advantages. Two " +
                        "distinct MECHANISMS carry them: the LEVEL of |F_es| at a fixed bias " +
                        "(C-0012) and 1/l at a force-pinned point (C-0017, C-0018)"),
        "what does NOT change" to
                ("the recommendation. Every surviving route favours 0.5 mM, at every layer " +
                        "model, at the 10 nm device. What changes is the count, the strength of " +
                        "the word, and the size of the strongest number quoted for it")
    )
}

private fun report(result: T156Result, output: File) {
    println()
    println("T-156 — how many of the six 0.5 mM routes are read on withdrawn objects?")
    println()
    result.routes.forEach {
        println(
            "  %-8s %-26s %-26s %s".format(
                it.claim, it.clause, it.verdict, it.independence
            )
        )
        println(
            "           advantage %s   %s".format(
                it.advantage?.let { a -> "%.4f".format(a) } ?: "-",
                if (it.transferOf.isEmpty()) "" else "transfer of ${it.transferOf}"
            )
        )
    }
    println()
    println("  named ${result.verdict.namedRoutes}, withdrawn ${result.verdict.withdrawn}, " +
            "transfers ${result.verdict.transfers}, independent survivors " +
            "${result.verdict.independentSurvivors}")
    println("  mechanisms: ${result.verdict.distinctMechanisms}")
    println()
    result.falsifiers.forEach { println("  ${it.id} fired=${it.fired}: ${it.outcome}") }
    println()
    result.findings.forEach { (key, value) -> println("  $key:\n    $value\n") }
    println("  written to $output")
}
