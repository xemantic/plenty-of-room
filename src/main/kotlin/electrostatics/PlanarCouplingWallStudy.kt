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

package com.xemantic.nano.plentyofroom.electrostatics

import com.xemantic.nano.plentyofroom.structure.DEPARTURE_SIGNIFICANT_DIGITS
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.io.File
import kotlin.math.abs
import kotlin.math.ln

/**
 * `T-221` — which wall a **planar wall-wall** coupling criterion is owed at.
 *
 * Emits `gpd/results/T-221-planar-coupling-wall.json`, deterministically — no timestamp, no step
 * count, no wall clock.
 *
 * ## The one move
 *
 * `Ξ` and `D̃` are both linear in the wall's charge density, so `Ξ/D̃ = q² l_B/D` carries no wall
 * convention at all and Kanduč Eq. (64) is **equivalent** to `ln(D/μ) < D/(q² l_B)`. The whole
 * disputed `16.5×` therefore enters once, as `ln 16.5 = 2.80`, and the threshold it is measured
 * against is a closed form. Nothing here needs a field solve, and a primitive-model Monte Carlo
 * would not answer the question at all — it would have to be *told* which wall to build.
 */

/** The gaps the criterion is read at — section 3's own layer heights. */
private val GAPS = listOf(5.0, 7.0, 10.0)

/** The gap `C-0137` reads its binding state at. */
private const val OPERATING_GAP = 7.0

/** The buffer `C-0005`'s saturated charge and `C-0137`'s binding state are read at, in mM. */
private const val OPERATING_BUFFER = 2.0

private const val VALENCY = 2

/** `C-0005`'s published saturated far-field tile charge at 2 mM, in `e/nm²` — **CITED**. */
private const val PUBLISHED_SATURATED_CHARGE = 0.0568

/** `C-0005`'s published `Ξ|P⁽¹⁾|/P_PB` at [ONE_LOOP_GAPS], bare `σ`, `Mg²⁺` — **CITED**. */
private val ONE_LOOP_PUBLISHED = listOf(2.14, 1.63, 1.23, 0.89, 0.70)

private val ONE_LOOP_GAPS = listOf(5.0, 7.0, 10.0, 15.0, 20.0)

/** `T-6`'s own emitted `loopExpansionValidityGap` for the bare duplex at `q = 2` — **CITED**. */
private const val PUBLISHED_VALIDITY_GAP = 13.517697558570946

/** `C-0137` §`P4`'s two published bounds at the 7 nm gap — **CITED**. */
private const val PUBLISHED_BARE_BOUND = 14.43

private const val PUBLISHED_SATURATED_BOUND = 2.80

/** The sample counts the `ζ` scan's convergence is measured over. */
private val SCAN_SAMPLES = listOf(5_000, 20_000, 80_000)

@Serializable
private data class T221Wall(
    val id: String,
    val label: String,
    val geometry: String,
    val chargeConvention: String,
    val surfaceChargeDensity: Double,
    val gouyChapmanLength: Double,
    val coupling: Double,
    val provenance: String
)

@Serializable
private data class T221RepulsiveReading(
    val wall: String,
    val geometry: String,
    val chargeConvention: String,
    val gap: Double,
    val reducedGap: Double,
    val coupling: Double,
    val bound: Double,
    val logResidual: Double,
    val margin: Double,
    val satisfied: Boolean,
    val couplingOverReducedGap: Double,
    val couplingOverReducedGapDeparture: Double
)

@Serializable
private data class T221Threshold(
    val gap: Double,
    val thresholdGouyChapmanLength: Double,
    val thresholdCoupling: Double,
    val thresholdChargeDensity: Double,
    val bisectionDeparture: Double,
    val note: String
)

@Serializable
private data class T221ValidityGap(
    val wall: String,
    val validityGap: Double?,
    val logResidualAtRoot: Double?,
    val insideSectionThreeRange: Boolean,
    val note: String
)

@Serializable
private data class T221SquareCell(
    val geometry: String,
    val chargeConvention: String,
    val wall: String,
    val coupling: Double,
    val logCoupling: Double,
    val satisfiedAtFive: Boolean,
    val satisfiedAtSeven: Boolean,
    val satisfiedAtTen: Boolean
)

@Serializable
private data class T221SquareDecomposition(
    val total: Double,
    val geometryFirstGeometryTerm: Double,
    val geometryFirstChargeTerm: Double,
    val chargeFirstChargeTerm: Double,
    val chargeFirstGeometryTerm: Double,
    val interaction: Double,
    val orderingsAgreeOnTotal: Double,
    val verdictIsConstantDownTheChargeColumn: Boolean,
    val verdictIsConstantAcrossTheGeometryRow: Boolean,
    val note: String
)

@Serializable
private data class T221Domain(
    val wall: String,
    val chargeConvention: String,
    val gap: Double,
    val reducedGap: Double,
    val logReducedGap: Double,
    val aboveOneLoopCoefficientDomain: Boolean,
    val aboveBoundMinimum: Boolean,
    val aboveOperationalAsymptotic: Boolean,
    val boundOverItsGlobalMinimum: Double
)

@Serializable
private data class T221AttractiveReading(
    val wall: String,
    val gap: Double,
    val reducedGap: Double,
    val coupling: Double,
    val branchBoundaryAsymmetry: Double,
    val criterionThresholdAsymmetry: Double?,
    val excludedSliverFraction: Double,
    val infimumBound: Double,
    val infimumOverRepulsiveBound: Double,
    val satisfiedAtHalfAsymmetry: Boolean
)

@Serializable
private data class T221Convergence(
    val quantity: String,
    val axis: String,
    val values: List<Double>,
    val departure: Double,
    val settles: String
)

@Serializable
private data class T221Reproduction(
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val relativeDeparture: Double,
    val source: String
)

@Serializable
private data class T221Falsifier(
    val id: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T221Result(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val sources: List<String>,
    val citedInputs: List<String>,
    val runParameters: Map<String, String>,
    val walls: List<T221Wall>,
    val repulsiveReadings: List<T221RepulsiveReading>,
    val thresholds: List<T221Threshold>,
    val validityGaps: List<T221ValidityGap>,
    val domains: List<T221Domain>,
    val square: List<T221SquareCell>,
    val squareDecomposition: T221SquareDecomposition,
    val attractiveReadings: List<T221AttractiveReading>,
    val downstreamSensitivity: Map<String, Double>,
    val convergence: List<T221Convergence>,
    val reproductions: List<T221Reproduction>,
    val falsifiers: List<T221Falsifier>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

private val json = Json { prettyPrint = true; encodeDefaults = true }

/** Reads one numeric field out of every element of a named array in a result file. */
private fun readColumn(file: File, array: String, field: String): List<Double> =
    json.parseToJsonElement(file.readText()).jsonObject[array]!!.jsonArray
        .map { it.jsonObject[field]!!.toString().trim('"').toDouble() }

/** Reads one numeric field out of every element, keyed on a string field's value. */
private fun readKeyed(
    file: File,
    array: String,
    keyField: String,
    field: String
): Map<String, Double> =
    json.parseToJsonElement(file.readText()).jsonObject[array]!!.jsonArray.associate {
        it.jsonObject[keyField]!!.toString().trim('"') to
                it.jsonObject[field]!!.toString().trim('"').toDouble()
    }

fun main(arguments: Array<String>) {
    val output = File(
        if (arguments.isNotEmpty()) arguments[0]
        else "gpd/results/T-221-planar-coupling-wall.json"
    )
    val resultsDirectory = output.parentFile

    val bjerrum = bjerrumLength()
    val tile = DnaOrigamiTile()
    val buffer = MagnesiumChlorideBuffer(OPERATING_BUFFER)

    // ---- the candidate walls ------------------------------------------------------------

    val bareCylinder = tile.duplexSurfaceChargeDensity
    val bareSingleLayer = singleHelixLayerChargeDensity(tile)
    val bareFace = tile.projectedChargeDensity / 2.0
    val manningCylinder = bareCylinder * tile.manningSurvivingFraction(VALENCY, bjerrum)
    val saturatedFace = buffer.inverseDebyeLength() / (Math.PI * bjerrum * VALENCY)

    data class Candidate(
        val id: String,
        val label: String,
        val geometry: String,
        val convention: String,
        val sigma: Double,
        val provenance: String
    )

    val candidates = listOf(
        Candidate(
            "bare-cylinder", "duplex cylinder, bare", "duplex cylinder", "bare", bareCylinder,
            "DERIVED: tau/(2 pi R) with tau = 1/b, b = rise/2 - the reading CLAUDE.md adopts " +
                "for the LOCAL coupling, and C-0005's Xi = 24.0"
        ),
        Candidate(
            "bare-single-layer", "single row of duplexes, projected, bare",
            "smeared gap face", "bare", bareSingleLayer,
            "DERIVED: section 3's other reading of 'single-layer honeycomb'"
        ),
        Candidate(
            "bare-face", "gap-facing plane, Gauss partition, bare", "smeared gap face", "bare",
            bareFace,
            "DERIVED: CLAUDE.md's sigma_face = rho t/2, which is Gauss's law on a slab and " +
                "not a convention"
        ),
        Candidate(
            "manning-cylinder", "duplex cylinder, Manning-renormalised", "duplex cylinder",
            "renormalised", manningCylinder,
            "DERIVED: the surviving fraction 1/(q xi_M); its Gouy-Chapman length is EXACTLY " +
                "the helix radius, an identity carrying no rise, no l_B and no valency"
        ),
        Candidate(
            "saturated-face", "gap-facing plane, charge-saturated", "smeared gap face",
            "renormalised", saturatedFace,
            "DERIVED as kappa/(pi l_B q) at 2 mM; C-0005 publishes 0.0568 e/nm^2 and C-0137 " +
                "reads Xi = 1.455 from it"
        ),
        Candidate(
            "bare-projected", "all the tile's charge on its footprint, bare",
            "smeared gap face", "bare", tile.projectedChargeDensity,
            "DERIVED: the far-field reading CLAUDE.md explicitly refuses for Xi; carried so " +
                "the geometry axis is bracketed at both ends"
        )
    )

    val walls = candidates.map {
        val surface = ChargedSurface(it.sigma, VALENCY)
        T221Wall(
            id = it.id,
            label = it.label,
            geometry = it.geometry,
            chargeConvention = it.convention,
            surfaceChargeDensity = it.sigma,
            gouyChapmanLength = surface.gouyChapmanLength(bjerrum),
            coupling = surface.couplingParameter(bjerrum),
            provenance = it.provenance
        )
    }

    // ---- the repulsive branch, Kanduc Eq. (64) --------------------------------------------

    val identity = couplingOverReducedGap(OPERATING_GAP, VALENCY, bjerrum)

    val repulsiveReadings = candidates.flatMap { candidate ->
        val surface = ChargedSurface(candidate.sigma, VALENCY)
        val mu = surface.gouyChapmanLength(bjerrum)
        val coupling = surface.couplingParameter(bjerrum)
        GAPS.map { gap ->
            val reduced = gap / mu
            val bound = weakCouplingValidityCoupling(reduced)
            val ratio = coupling / reduced
            T221RepulsiveReading(
                wall = candidate.id,
                geometry = candidate.geometry,
                chargeConvention = candidate.convention,
                gap = gap,
                reducedGap = reduced,
                coupling = coupling,
                bound = bound,
                logResidual = repulsiveBranchLogResidual(gap, mu, VALENCY, bjerrum),
                margin = bound / coupling,
                satisfied = coupling < bound,
                couplingOverReducedGap = ratio,
                couplingOverReducedGapDeparture =
                    abs(ratio / couplingOverReducedGap(gap, VALENCY, bjerrum) - 1.0)
            )
        }
    }

    val thresholds = GAPS.map { gap ->
        val closed = repulsiveBranchThresholdCoupling(gap, VALENCY, bjerrum)
        val muStar = repulsiveBranchThresholdGouyChapmanLength(gap, VALENCY, bjerrum)
        // an independent bisection on the criterion itself, to check the closed form
        var low = 1e-6
        var high = 1e3
        repeat(300) {
            val middle = 0.5 * (low + high)
            val mu = ChargedSurface(middle, VALENCY).gouyChapmanLength(bjerrum)
            if (repulsiveBranchLogResidual(gap, mu, VALENCY, bjerrum) > 0.0) low = middle
            else high = middle
        }
        val bisected = ChargedSurface(0.5 * (low + high), VALENCY).couplingParameter(bjerrum)
        T221Threshold(
            gap = gap,
            thresholdGouyChapmanLength = muStar,
            thresholdCoupling = closed,
            thresholdChargeDensity =
                repulsiveBranchThresholdChargeDensity(gap, VALENCY, bjerrum),
            bisectionDeparture = abs(bisected / closed - 1.0),
            note = "every bare reading is above this charge density and every renormalised " +
                "one is below it"
        )
    }

    val validityGaps = candidates.map { candidate ->
        val surface = ChargedSurface(candidate.sigma, VALENCY)
        val mu = surface.gouyChapmanLength(bjerrum)
        val root = loopExpansionValidityGap(surface.couplingParameter(bjerrum), mu)
        T221ValidityGap(
            wall = candidate.id,
            validityGap = root,
            logResidualAtRoot =
                root?.let { repulsiveBranchLogResidual(it, mu, VALENCY, bjerrum) },
            insideSectionThreeRange = root != null && root <= 10.0,
            note = if (root == null)
                "NO ROOT: Xi is below the criterion's own global minimum e, so the criterion " +
                    "holds at every separation - CH-0178"
            else "the gap above which Kanduc Eq. (64) is satisfied at this wall"
        )
    }

    val domains = candidates.flatMap { candidate ->
        val mu = ChargedSurface(candidate.sigma, VALENCY).gouyChapmanLength(bjerrum)
        GAPS.map { gap ->
            val reduced = gap / mu
            T221Domain(
                wall = candidate.id,
                chargeConvention = candidate.convention,
                gap = gap,
                reducedGap = reduced,
                logReducedGap = ln(reduced),
                aboveOneLoopCoefficientDomain = reduced > Math.PI,
                aboveBoundMinimum = reduced > Math.E,
                aboveOperationalAsymptotic = reduced > 10.0,
                boundOverItsGlobalMinimum =
                    if (reduced > 1.0) weakCouplingValidityCoupling(reduced) / Math.E
                    else Double.NaN
            )
        }
    }.filter { it.boundOverItsGlobalMinimum.isFinite() }

    // ---- the two-by-two -------------------------------------------------------------------

    fun satisfiedAt(sigma: Double, gap: Double): Boolean {
        val surface = ChargedSurface(sigma, VALENCY)
        return surface.couplingParameter(bjerrum) <
                weakCouplingValidityCoupling(gap / surface.gouyChapmanLength(bjerrum))
    }

    val squareIds = listOf("bare-cylinder", "bare-face", "manning-cylinder", "saturated-face")
    val square = candidates.filter { it.id in squareIds }.map { candidate ->
        val coupling = ChargedSurface(candidate.sigma, VALENCY).couplingParameter(bjerrum)
        T221SquareCell(
            geometry = candidate.geometry,
            chargeConvention = candidate.convention,
            wall = candidate.id,
            coupling = coupling,
            logCoupling = ln(coupling),
            satisfiedAtFive = satisfiedAt(candidate.sigma, 5.0),
            satisfiedAtSeven = satisfiedAt(candidate.sigma, 7.0),
            satisfiedAtTen = satisfiedAt(candidate.sigma, 10.0)
        )
    }

    val bareCylinderLog = ln(bareCylinder)
    val bareFaceLog = ln(bareFace)
    val manningLog = ln(manningCylinder)
    val saturatedLog = ln(saturatedFace)
    val total = saturatedLog - bareCylinderLog
    val geometryFirstGeometry = bareFaceLog - bareCylinderLog
    val geometryFirstCharge = saturatedLog - bareFaceLog
    val chargeFirstCharge = manningLog - bareCylinderLog
    val chargeFirstGeometry = saturatedLog - manningLog

    val squareDecomposition = T221SquareDecomposition(
        total = total,
        geometryFirstGeometryTerm = geometryFirstGeometry,
        geometryFirstChargeTerm = geometryFirstCharge,
        chargeFirstChargeTerm = chargeFirstCharge,
        chargeFirstGeometryTerm = chargeFirstGeometry,
        interaction = chargeFirstGeometry - geometryFirstGeometry,
        orderingsAgreeOnTotal = abs(
            (geometryFirstGeometry + geometryFirstCharge) -
                (chargeFirstCharge + chargeFirstGeometry)
        ),
        verdictIsConstantDownTheChargeColumn = square.filter { it.chargeConvention == "bare" }
            .all { !it.satisfiedAtFive && !it.satisfiedAtSeven && !it.satisfiedAtTen } &&
            square.filter { it.chargeConvention == "renormalised" }
                .all { it.satisfiedAtFive && it.satisfiedAtSeven && it.satisfiedAtTen },
        verdictIsConstantAcrossTheGeometryRow = square.groupBy { it.geometry }
            .values.all { row -> row.map { it.satisfiedAtSeven }.distinct().size == 1 },
        note = "the two orderings disagree on the SIGN of the geometry term, because the " +
            "smeared face is more charged than the cylinder when both are bare and less " +
            "charged when both are renormalised; the total is the quotable number"
    )

    // ---- the attractive branch, Kanduc Eq. (65) --------------------------------------------

    val attractiveReadings = candidates.flatMap { candidate ->
        val surface = ChargedSurface(candidate.sigma, VALENCY)
        val mu = surface.gouyChapmanLength(bjerrum)
        val coupling = surface.couplingParameter(bjerrum)
        GAPS.map { gap ->
            val reduced = gap / mu
            val ceiling = attractiveBranchAsymmetryCeiling(reduced)
            val threshold = attractiveBranchAsymmetryThreshold(coupling, reduced)
            val infimum = attractiveBranchInfimumCoupling(reduced)
            T221AttractiveReading(
                wall = candidate.id,
                gap = gap,
                reducedGap = reduced,
                coupling = coupling,
                branchBoundaryAsymmetry = ceiling,
                criterionThresholdAsymmetry = threshold,
                excludedSliverFraction =
                    if (threshold == null) 0.0 else (ceiling - threshold) / (1.0 + ceiling),
                infimumBound = infimum,
                infimumOverRepulsiveBound = infimum / weakCouplingValidityCoupling(reduced),
                satisfiedAtHalfAsymmetry =
                    attractiveBranchValidityCoupling(-0.5, reduced) > coupling
            )
        }
    }

    // ---- what the wall choice is worth downstream ------------------------------------------

    val t50 = File(resultsDirectory, "T-50-beyond-mean-field-gap.json")
    val boundaryMarginRatios = json.parseToJsonElement(t50.readText())
        .jsonObject["memberEffects"]!!.jsonArray
        .filter { it.jsonObject["channel"]!!.toString().trim('"') == "boundary condition" }
        .map { it.jsonObject["marginRatio"]!!.toString().trim('"').toDouble() }
    val largestMarginMovement = boundaryMarginRatios.maxOf { abs(it - 1.0) }

    val downstreamSensitivity = mapOf(
        "wallChargeRatioBareOverSaturated" to bareCylinder / saturatedFace,
        "logOfThatRatio" to ln(bareCylinder / saturatedFace),
        "C0137BoundaryChannelSweepSpan" to 16.0,
        "C0137LargestMarginMovement" to largestMarginMovement,
        "C0137MarginRatioRange" to (boundaryMarginRatios.max() - boundaryMarginRatios.min()),
        "oneLoopDeviationAtSevenBare" to meanFieldDeviation(
            ChargedSurface(bareCylinder, VALENCY).couplingParameter(bjerrum),
            OPERATING_GAP / ChargedSurface(bareCylinder, VALENCY).gouyChapmanLength(bjerrum)
        ),
        "oneLoopDeviationAtSevenSaturated" to meanFieldDeviation(
            ChargedSurface(saturatedFace, VALENCY).couplingParameter(bjerrum),
            OPERATING_GAP / ChargedSurface(saturatedFace, VALENCY).gouyChapmanLength(bjerrum)
        )
    )

    // ---- convergence -----------------------------------------------------------------------

    val scanned = SCAN_SAMPLES.map {
        attractiveBranchScannedInfimumCoupling(
            OPERATING_GAP / ChargedSurface(bareCylinder, VALENCY).gouyChapmanLength(bjerrum), it
        )
    }
    val convergence = listOf(
        T221Convergence(
            quantity = "the infimum of Kanduc Eq. (65) over its own branch, at the bare duplex " +
                "wall and the 7 nm gap",
            axis = "asymmetry scan samples 5000 / 20000 / 80000",
            values = scanned,
            departure = abs(scanned.last() / scanned.first() - 1.0),
            settles = "the scan includes the branch boundary at every count and the boundary IS " +
                "the argmin, so the scan is exact at every count; the closed form is what is " +
                "quoted and the scan is the gate that says the argmin is not an artefact"
        ),
        T221Convergence(
            quantity = "the scale-covariance identity Xi/Dtilde - q^2 l_B/D, over every wall " +
                "and every gap",
            axis = "the wall convention itself",
            values = listOf(repulsiveReadings.maxOf { it.couplingOverReducedGapDeparture }),
            departure = repulsiveReadings.maxOf { it.couplingOverReducedGapDeparture },
            settles = "exact to the last unit in the last place at all 18 readings, which is " +
                "the cheap bound this task rests on"
        )
    )

    // ---- reproductions ----------------------------------------------------------------------

    val bareSurface = ChargedSurface(bareCylinder, VALENCY)
    val bareMu = bareSurface.gouyChapmanLength(bjerrum)
    val bareCoupling = bareSurface.couplingParameter(bjerrum)
    val saturatedSurface = ChargedSurface(saturatedFace, VALENCY)

    val reproductions = buildList {
        ONE_LOOP_GAPS.forEachIndexed { index, gap ->
            val reproduced = meanFieldDeviation(bareCoupling, gap / bareMu)
            add(
                T221Reproduction(
                    quantity = "C-0005's one-loop deviation at ${gap.roundedForProse()} nm, BARE DUPLEX wall",
                    published = ONE_LOOP_PUBLISHED[index],
                    reproduced = reproduced,
                    relativeDeparture = abs(reproduced / ONE_LOOP_PUBLISHED[index] - 1.0),
                    source = "C-0005 / gpd/results/T-6-mean-field-screening-validity.json"
                )
            )
        }
        val emittedValidityGap = loopExpansionValidityGap(bareCoupling, bareMu)!!
        add(
            T221Reproduction(
                quantity = "T-6's emitted loopExpansionValidityGap for the bare duplex, q = 2 " +
                    "- which IS Kanduc Eq. (64), under Naji's Eq. (20) numbering",
                published = PUBLISHED_VALIDITY_GAP,
                reproduced = emittedValidityGap,
                relativeDeparture = abs(emittedValidityGap / PUBLISHED_VALIDITY_GAP - 1.0),
                source = "gpd/results/T-6-mean-field-screening-validity.json, surfaces[]"
            )
        )
        add(
            T221Reproduction(
                quantity = "C-0005's Xi at the bare duplex cylinder with Mg2+",
                published = 24.0,
                reproduced = bareCoupling,
                relativeDeparture = abs(bareCoupling / 24.0 - 1.0),
                source = "C-0005"
            )
        )
        add(
            T221Reproduction(
                quantity = "C-0005's saturated far-field charge at 2 mM",
                published = PUBLISHED_SATURATED_CHARGE,
                reproduced = saturatedFace,
                relativeDeparture = abs(saturatedFace / PUBLISHED_SATURATED_CHARGE - 1.0),
                source = "C-0005, re-derived here as kappa/(pi l_B q)"
            )
        )
        val bareBound = weakCouplingValidityCoupling(OPERATING_GAP / bareMu)
        add(
            T221Reproduction(
                quantity = "C-0137's Eq. (64) bound at 7 nm, bare duplex wall",
                published = PUBLISHED_BARE_BOUND,
                reproduced = bareBound,
                relativeDeparture = abs(bareBound / PUBLISHED_BARE_BOUND - 1.0),
                source = "C-0137 section P4"
            )
        )
        val saturatedBound = weakCouplingValidityCoupling(
            OPERATING_GAP / saturatedSurface.gouyChapmanLength(bjerrum)
        )
        add(
            T221Reproduction(
                quantity = "C-0137's Eq. (64) bound at 7 nm, saturated gap face",
                published = PUBLISHED_SATURATED_BOUND,
                reproduced = saturatedBound,
                relativeDeparture = abs(saturatedBound / PUBLISHED_SATURATED_BOUND - 1.0),
                source = "C-0137 section P4"
            )
        )
    }

    // ---- falsifiers ---------------------------------------------------------------------------

    val geometryFlips = GAPS.any { gap ->
        listOf("bare", "renormalised").any { convention ->
            candidates.filter { it.convention == convention }
                .map { satisfiedAt(it.sigma, gap) }.distinct().size > 1
        }
    }
    val chargeFlips = GAPS.all { gap ->
        candidates.filter { it.convention == "bare" }.none { satisfiedAt(it.sigma, gap) } &&
            candidates.filter { it.convention == "renormalised" }
                .all { satisfiedAt(it.sigma, gap) }
    }
    val infimumRatios = attractiveReadings.map { it.infimumOverRepulsiveBound }
    val worstSliver = attractiveReadings.maxOf { it.excludedSliverFraction }
    val renormalisedReducedGaps = candidates.filter { it.convention == "renormalised" }
        .map { OPERATING_GAP / ChargedSurface(it.sigma, VALENCY).gouyChapmanLength(bjerrum) }

    val falsifiers = listOf(
        T221Falsifier(
            id = "F1",
            statement = "the GEOMETRY axis - duplex cylinder against a smeared gap-facing " +
                "plane - flips Kanduc Eq. (64)'s verdict at some candidate and some gap",
            fired = geometryFlips,
            outcome = ("all four bare readings (Xi = %.4f, %.4f, %.4f, %.4f) FAIL at 5, 7 and " +
                "10 nm and both renormalised readings PASS at all three; the geometry axis " +
                "moves Xi by %.2fx within the bare family and moves no verdict").format(
                ChargedSurface(bareCylinder, VALENCY).couplingParameter(bjerrum),
                ChargedSurface(bareSingleLayer, VALENCY).couplingParameter(bjerrum),
                ChargedSurface(bareFace, VALENCY).couplingParameter(bjerrum),
                ChargedSurface(tile.projectedChargeDensity, VALENCY).couplingParameter(bjerrum),
                tile.projectedChargeDensity / bareCylinder
            )
        ),
        T221Falsifier(
            id = "F2",
            statement = "the CHARGE-CONVENTION axis - bare against renormalised - does NOT " +
                "flip the verdict",
            fired = !chargeFlips,
            outcome = ("every bare reading fails and every renormalised one passes, at 5, 7 " +
                "and 10 nm: the whole %.3fx straddle is the renormalisation and none of it " +
                "is the geometry").format(bareCylinder / saturatedFace)
        ),
        T221Falsifier(
            id = "F3",
            statement = "Kanduc Eq. (65)'s infimum over its own admissible asymmetry exceeds " +
                "Eq. (64)'s bound by more than 2x, i.e. 'exponentially large' is operative",
            fired = infimumRatios.any { it > 2.0 },
            outcome = ("the infimum is attained AT the branch boundary and sits at %.4f to " +
                "%.4f of Eq. (64)'s bound over all 18 readings - the two criteria agree to " +
                "within a sixth where continuity across p0 = 0 requires them to").format(
                infimumRatios.min(), infimumRatios.max()
            )
        ),
        T221Falsifier(
            id = "F4",
            statement = "C-0005's published 123-214 % one-loop deviation is NOT reproduced by " +
                "the bare duplex reading, i.e. the corpus has not in fact committed to a wall",
            fired = reproductions.take(5).any { it.relativeDeparture > 0.005 },
            outcome = ("the five published deviations reproduce to %.2e - %.2e at the bare " +
                "duplex wall, and T-6's own emitted loopExpansionValidityGap - which IS this " +
                "criterion - reproduces to %.1e").format(
                reproductions.take(5).minOf { it.relativeDeparture },
                reproductions.take(5).maxOf { it.relativeDeparture },
                reproductions[5].relativeDeparture
            )
        ),
        T221Falsifier(
            id = "F5",
            statement = "adopting the saturated reading moves C-0017's stability margin by " +
                "more than 2 %, i.e. past the band C-0137 measured for the same factor",
            fired = largestMarginMovement > 0.02,
            outcome = ("C-0137 swept the effective wall charge over 16x - which contains the " +
                "%.3fx this convention is worth - and the margin moved by at most %.4f %%, " +
                "which is C-0137's own published 1.44 %%").format(
                bareCylinder / saturatedFace, 100.0 * largestMarginMovement
            )
        ),
        T221Falsifier(
            id = "F6",
            statement = "the renormalised readings sit inside Eq. (64)'s own asymptotic domain " +
                "D/mu >> 1, so their PASS is entitled",
            fired = renormalisedReducedGaps.all { it > 10.0 },
            outcome = ("the renormalised readings sit at D/mu = %.4f and %.4f at the 7 nm " +
                "gap, where ln(D/mu) is %.4f and %.4f - order one, not large - against " +
                "%.1f and %.1f for the bare readings; the saturated wall's bound sits %.2f %% " +
                "above the SMALLEST value D/ln D can ever take, and at the 5 nm gap its " +
                "D/mu = %.4f falls below Naji Eq. (19)'s own domain of pi, where the one-loop " +
                "coefficient is not defined at all").format(
                renormalisedReducedGaps.min(), renormalisedReducedGaps.max(),
                ln(renormalisedReducedGaps.min()), ln(renormalisedReducedGaps.max()),
                OPERATING_GAP / bareMu,
                OPERATING_GAP / ChargedSurface(bareFace, VALENCY).gouyChapmanLength(bjerrum),
                100.0 * (weakCouplingValidityCoupling(
                    OPERATING_GAP / saturatedSurface.gouyChapmanLength(bjerrum)
                ) / Math.E - 1.0),
                5.0 / saturatedSurface.gouyChapmanLength(bjerrum)
            )
        )
    )

    val result = T221Result(
        task = "T-221",
        leaf = "A7.4",
        title = "Which wall a planar wall-wall coupling criterion is owed at: the criterion's " +
            "scale covariance, the two-by-two the question bundles, and the branch the device " +
            "is actually on",
        verificationType = "logical (a scale-covariance identity that reduces Kanduc Eq. (64) " +
            "to one closed form, and a two-by-two decomposition of the disputed factor) + " +
            "in-silico (both of Kanduc's branches evaluated over six candidate walls, three " +
            "gaps and the whole admissible asymmetry range) + literature (the criterion's own " +
            "derivation, read directly, for what its variables are DEFINED to be)",
        acceptance = "P1 the wall dependence isolated in closed form with its threshold; P2 " +
            "the disputed factor decomposed over geometry and charge convention; P3 a stated " +
            "rule with a ground in the criterion's own derivation; P4 both branches evaluated; " +
            "P5 whether any verdict moves, measured; P6 a ceiling and a threshold",
        maturity = "TRL 1-3. Model-consistent and traceable. NOTHING HERE IS MEASURED. This " +
            "task settles which INPUT a published inequality is owed, not whether the " +
            "inequality is right; the intermediate-coupling regime Xi = 17-24 still has no " +
            "systematic theory (C-0005) and no primitive-model Monte Carlo of this gap exists.",
        units = mapOf(
            "length" to "nm",
            "chargeDensity" to "e/nm^2",
            "coupling" to "dimensionless",
            "reducedGap" to "dimensionless",
            "asymmetry" to "dimensionless, in (-1, 0) for oppositely charged walls",
            "concentration" to "mM",
            "temperature" to "K"
        ),
        conventions = listOf(
            "sigma is always a MAGNITUDE in e/nm^2; q = 2 for Mg2+; l_B = 0.7141 nm at 300 K, " +
                "eps_r = 78",
            "mu = mu_GC = 1/(2 pi q l_B sigma) in nm; Xi = q^2 l_B/mu; Dtilde = D/mu",
            "Kanduc's sigma_1 is the LARGER-magnitude wall by his own Eq. (3) " +
                "(sigma_1 + sigma_2 < 0, sigma_2 > sigma_1), and zeta = sigma_2/sigma_1",
            "oppositely charged walls are -1 < zeta < 0; zeta = -1 is excluded by Eq. (3) " +
                "because it leaves no counterions at all",
            "Eq. (64) is the criterion where the mean-field pressure is REPULSIVE and Eq. (65) " +
                "where it is ATTRACTIVE; which holds is set by the sign of p0, not by the wall",
            "a criterion is quoted WITH its wall and WITH its gap",
            "MgCl2 is 2:1, so I = 3c"
        ),
        sources = listOf(
            "gpd/results/T-6-mean-field-screening-validity.json (C-0005's coupling " +
                "parameters, saturated charge and the emitted loopExpansionValidityGap; read " +
                "as literals for the reproduction records)",
            "gpd/results/T-50-beyond-mean-field-gap.json (C-0137's boundary-channel margin " +
                "ratios, read at run time)",
            "gpd/data/T-50-beyond-mean-field-literature.md (rows 25-30, the criterion and its " +
                "two branches, READ DIRECTLY)",
            "Kanduc, Trulsson, Naji, Burak, Forsman & Podgornik, arXiv:0905.3851, Eqs. (3), " +
                "(5), (6), (7), (14), (18), (61), (62), (64), (65) - READ DIRECTLY",
            "Naji, Jungblut, Moreira & Netz, Physica A 352:131 (2005), Eqs. (3), (4), (9), " +
                "(19), (20) - the same closed form as Kanduc Eq. (64), already implemented"
        ),
        citedInputs = listOf(
            "C-0005's Xi = 24.0 at the bare duplex cylinder - CITED and REPRODUCED here",
            "C-0005's saturated far-field charge 0.0568 e/nm^2 at 2 mM - CITED and " +
                "re-derived here as kappa/(pi l_B q)",
            "C-0005's five one-loop deviations 2.14/1.63/1.23/0.89/0.70 - CITED and " +
                "REPRODUCED here at the bare duplex wall",
            "T-6's emitted loopExpansionValidityGap " +
                "${PUBLISHED_VALIDITY_GAP.roundedForProse()} nm - CITED as a " +
                "literal and REPRODUCED here",
            "C-0137's two Eq. (64) bounds 14.43 and 2.80 at 7 nm - CITED and REPRODUCED here",
            "C-0137's boundary-channel margin ratios - READ from its result file at run time",
            "B-DNA rise 0.34 nm and duplex radius 1.0 nm - CITED, through DnaOrigamiTile",
            "honeycomb interhelical distance 2.6 nm - CITED, through DnaOrigamiTile"
        ),
        runParameters = mapOf(
            "temperature" to "300.0",
            "waterRelativePermittivity" to "78.0",
            "bjerrumLength" to bjerrum.roundedForProse().toString(),
            "counterionValency" to VALENCY.toString(),
            "buffer" to "$OPERATING_BUFFER mM MgCl2",
            "gaps" to GAPS.toString(),
            "operatingGap" to OPERATING_GAP.toString(),
            "tileEdge" to tile.edge.toString(),
            "tileThickness" to tile.thickness.toString(),
            "interhelicalDistance" to tile.interhelicalDistance.toString(),
            "risePerBasePair" to tile.risePerBasePair.toString(),
            "helixRadius" to tile.helixRadius.toString(),
            "asymmetryScanSamples" to SCAN_SAMPLES.toString()
        ),
        walls = walls,
        repulsiveReadings = repulsiveReadings,
        thresholds = thresholds,
        validityGaps = validityGaps,
        domains = domains,
        square = square,
        squareDecomposition = squareDecomposition,
        attractiveReadings = attractiveReadings,
        downstreamSensitivity = downstreamSensitivity,
        convergence = convergence,
        reproductions = reproductions,
        falsifiers = falsifiers,
        findings = emptyMap(),
        validity = emptyList(),
        openQuestions = emptyList()
    )

    val sevenNanometreThreshold = thresholds.first { it.gap == OPERATING_GAP }
    val bareSeven = repulsiveReadings.first { it.wall == "bare-cylinder" && it.gap == OPERATING_GAP }
    val faceSeven = repulsiveReadings.first { it.wall == "bare-face" && it.gap == OPERATING_GAP }
    val saturatedSeven =
        repulsiveReadings.first { it.wall == "saturated-face" && it.gap == OPERATING_GAP }
    val bareAttractiveSeven =
        attractiveReadings.first { it.wall == "bare-cylinder" && it.gap == OPERATING_GAP }

    val findings = mutableMapOf<String, String>()
    val validity = mutableListOf<String>()
    val openQuestions = mutableListOf<String>()

    try {
        findings["cheapBound"] = (
            "Xi and Dtilde are BOTH linear in the wall's charge density, so Xi/Dtilde = " +
                "q^2 l_B/D = %.6f carries no wall convention at all - the largest departure " +
                "over all 18 readings is %.1e, below this file's own 1e-14 emission floor and " +
                "therefore emitted as 0. Kanduc Eq. (64) is therefore EQUIVALENT to " +
                "ln(D/mu) < D/(q^2 l_B), and the whole disputed %.3fx enters once, as " +
                "ln = %.4f, against a threshold distance of %.4f at the bare duplex wall and " +
                "%.4f at the saturated one. The naive '16.5x apart' overstates the " +
                "disagreement by %.2fx in the variable that decides - and it still flips it."
            ).format(
                identity, repulsiveReadings.maxOf { it.couplingOverReducedGapDeparture },
                bareCylinder / saturatedFace, ln(bareCylinder / saturatedFace),
                abs(bareSeven.logResidual), abs(saturatedSeven.logResidual),
                (bareCylinder / saturatedFace) / ln(bareCylinder / saturatedFace)
            )

        findings["threshold"] = (
            "The criterion at a %.1f nm gap admits Xi < %.4f, i.e. sigma < %.4f e/nm^2 " +
                "(mu > %.4f nm) - a closed form, mu* = D exp(-D/(q^2 l_B)), reproduced by an " +
                "independent bisection to %.1e. EVERY bare reading of the Gen-1 gap-facing " +
                "wall is above that charge density (%.4f to %.4f e/nm^2) and BOTH " +
                "renormalised ones are below it (%.4f and %.4f)."
            ).format(
                OPERATING_GAP, sevenNanometreThreshold.thresholdCoupling,
                sevenNanometreThreshold.thresholdChargeDensity,
                sevenNanometreThreshold.thresholdGouyChapmanLength,
                sevenNanometreThreshold.bisectionDeparture,
                bareCylinder, tile.projectedChargeDensity, manningCylinder, saturatedFace
            )

        findings["theTwoByTwo"] = (
            "'Bare duplex versus charge-saturated gap face' moves TWO things at once. Swept " +
                "as a square, the verdict is constant down the charge column (%s) and NOT " +
                "constant across the geometry row (%s): all four bare readings FAIL at 5, 7 " +
                "and 10 nm (Xi = %.2f cylinder, %.2f single layer, %.2f Gauss-partitioned " +
                "face, %.2f projected) and both renormalised readings PASS (%.4f Manning " +
                "cylinder, %.4f saturated face). The geometry axis is worth %.2fx in Xi and " +
                "ZERO verdicts. In logarithms the total move is %.4f, and the two orderings " +
                "disagree on the SIGN of the geometry term (%+.4f geometry-first against " +
                "%+.4f charge-first, interaction %+.4f) while agreeing on the total to %.1e."
            ).format(
                squareDecomposition.verdictIsConstantDownTheChargeColumn.toString(),
                squareDecomposition.verdictIsConstantAcrossTheGeometryRow.toString(),
                ChargedSurface(bareCylinder, VALENCY).couplingParameter(bjerrum),
                ChargedSurface(bareSingleLayer, VALENCY).couplingParameter(bjerrum),
                ChargedSurface(bareFace, VALENCY).couplingParameter(bjerrum),
                ChargedSurface(tile.projectedChargeDensity, VALENCY).couplingParameter(bjerrum),
                ChargedSurface(manningCylinder, VALENCY).couplingParameter(bjerrum),
                ChargedSurface(saturatedFace, VALENCY).couplingParameter(bjerrum),
                tile.projectedChargeDensity / bareCylinder,
                total, geometryFirstGeometry, chargeFirstGeometry,
                squareDecomposition.interaction, squareDecomposition.orderingsAgreeOnTotal
            )

        findings["theRule"] = (
            "TWO clauses, and only the first is a choice. (1) Xi and mu are owed at the BARE " +
                "charge, never at a renormalised one. The ground is four properties of " +
                "Kanduc's own derivation rather than a preference: his Eq. (14) fixes the " +
                "counterion population by ELECTRONEUTRALITY against sigma_1 + sigma_2, and " +
                "those are exactly the ions whose correlations Xi measures, so a renormalised " +
                "sigma deletes from the slit the very population the parameter counts; his " +
                "Eq. (6) defines mu as the distance at which a counterion interacts with the " +
                "WALL at k_BT, a Gauss's-law quantity and not a far-field fitting amplitude; " +
                "the contact-value theorem rho(0) = 2 pi l_B sigma^2 is exact BEYOND mean " +
                "field (Naji, after Eq. 9) and is written on the bare sigma, so a sigma that " +
                "does not reproduce the exact contact density is not the sigma these " +
                "variables are defined with; and a saturated sigma_eff is the nonlinear-PB " +
                "far-field amplitude while a Manning fraction is a mean-field two-state " +
                "association model, so using either as the INPUT to a mean-field validity " +
                "criterion assumes the answer. (2) WITHIN the bare family the geometry is a " +
                "BRACKET and not a choice, because the Gen-1 tile is not the model's wall: " +
                "Kanduc's sigma_1 sits in a delta sheet at the contact plane, and this tile's " +
                "charge is distributed through 10 nm of duplex lattice. The duplex cylinder " +
                "(%.2f) is what a CONTACT counterion sits on at mu = %.4f nm and the " +
                "Gauss-partitioned gap face (%.2f) is what the slit's field integrates to; " +
                "they differ by %.2fx and THE VERDICT IS INVARIANT ACROSS THE BRACKET at 5, 7 " +
                "and 10 nm. Carry both, quote the cylinder as the optimistic end - which is " +
                "exactly what CLAUDE.md's standing entry already does, and it needs no change."
            ).format(
                ChargedSurface(bareCylinder, VALENCY).couplingParameter(bjerrum),
                ChargedSurface(bareCylinder, VALENCY).gouyChapmanLength(bjerrum),
                ChargedSurface(bareFace, VALENCY).couplingParameter(bjerrum),
                bareFace / bareCylinder
            )

        val saturatedDeviations = ONE_LOOP_GAPS.joinToString("/") { gap ->
            val reduced = gap / saturatedSurface.gouyChapmanLength(bjerrum)
            if (reduced > Math.PI) {
                "%.4f".format(
                    meanFieldDeviation(saturatedSurface.couplingParameter(bjerrum), reduced)
                )
            } else "UNDEFINED"
        }
        findings["theCorpusAlreadyChose"] = (
            "Kanduc Eq. (64) is the SAME closed form as Naji Eq. (20), which this repository " +
                "has implemented as loopExpansionValidityGap since T-6 and emits in T-6's own " +
                "surfaces table at the BARE duplex wall - %.6f nm, reproduced here to %.1e. " +
                "C-0005's whole band structure IS that evaluation, and its headline " +
                "123-214 %% one-loop deviation reproduces at the bare wall to %.2e-%.2e. At " +
                "the saturated wall the same five gaps would read %s - the 5 nm one is not defined " +
                "at all, because D/mu = 2.55 there falls below Naji Eq. (19)'s own domain of " +
                "pi - and the four that are defined run 40-87 %%. So the " +
                "straddle was never symmetric: adopting the saturated reading requires " +
                "RETRACTING C-0005's 123-214 %%, which is the number this corpus's entire " +
                "beyond-mean-field exposure - CH-0019, C-0137, CH-0167 - is written on."
            ).format(
                loopExpansionValidityGap(bareCoupling, bareMu)!!,
                reproductions[5].relativeDeparture,
                reproductions.take(5).minOf { it.relativeDeparture },
                reproductions.take(5).maxOf { it.relativeDeparture },
                saturatedDeviations
            )

        findings["theBranchTheDeviceIsOn"] = (
            "Eq. (64) is derived for a REPULSIVE mean-field pressure and this device's walls " +
                "are oppositely charged, so Eq. (65) is what is owed. C-0137 and " +
                "BeyondMeanFieldGap.kt both dispose of it as 'the right hand side here is " +
                "exponentially large' and neither evaluates it. The exponential is at FIXED " +
                "zeta. Over the branch's own domain - attraction needs " +
                "Dtilde > (1+zeta)/|zeta|, derived here from Kanduc Eq. (18) in the " +
                "vanishing-alpha limit - the bound is minimised AT the branch boundary and " +
                "sits at %.4f to %.4f of Eq. (64)'s bound over all 18 readings. The two " +
                "criteria agree to within a sixth where continuity across p0 = 0 requires " +
                "them to, and 'exponentially large' is not an operative statement about this " +
                "device."
            ).format(infimumRatios.min(), infimumRatios.max())

        findings["whatTheWallIsWorthOnTheRightBranch"] = (
            "On Eq. (65) the criterion is satisfied for every zeta more negative than a " +
                "threshold barely past the branch boundary. At the bare duplex wall and the " +
                "7 nm gap the boundary is zeta = %.5f and the threshold zeta = %.5f, so the " +
                "excluded sliver is %.4f %% of the attractive branch; at the " +
                "Gauss-partitioned face it is %.4f %%; and at BOTH renormalised readings it " +
                "is exactly zero. The worst sliver over all 18 readings is %.4f %%, and it " +
                "sits adjacent to the p0 = 0 locus where Kanduc is explicit that NEITHER " +
                "criterion applies - 'the leading order term is zero and the fluctuations are " +
                "dominant at any finite value of Xi'. So on the branch this device occupies " +
                "the %.3fx wall disagreement is worth under one per cent of an asymmetry " +
                "range, not a verdict."
            ).format(
                bareAttractiveSeven.branchBoundaryAsymmetry,
                bareAttractiveSeven.criterionThresholdAsymmetry ?: 0.0,
                100.0 * bareAttractiveSeven.excludedSliverFraction,
                100.0 * attractiveReadings.first {
                    it.wall == "bare-face" && it.gap == OPERATING_GAP
                }.excludedSliverFraction,
                100.0 * worstSliver, bareCylinder / saturatedFace
            )

        findings["theWallDoesConstrainSomething"] = (
            "It constrains how much electrode charge the device needs before it is on the " +
                "easy branch at all. Attraction requires |zeta| > 1/(1 + Dtilde), which at " +
                "the 7 nm gap is %.5f at the bare duplex wall and %.5f at the saturated one - " +
                "a factor of %.2f. The Gen-1 gap force IS an attraction at every operating " +
                "state (C-0008 solves it; it is the actuation force), so the reading with the " +
                "smaller Dtilde makes the stronger implicit claim about the electrode. The " +
                "bare rule is the PERMISSIVE one here, which is the opposite direction from " +
                "the one it takes on Eq. (64)."
            ).format(
                abs(bareAttractiveSeven.branchBoundaryAsymmetry),
                abs(
                    attractiveReadings.first {
                        it.wall == "saturated-face" && it.gap == OPERATING_GAP
                    }.branchBoundaryAsymmetry
                ),
                abs(
                    attractiveReadings.first {
                        it.wall == "saturated-face" && it.gap == OPERATING_GAP
                    }.branchBoundaryAsymmetry
                ) / abs(bareAttractiveSeven.branchBoundaryAsymmetry)
            )

        findings["doesAnyVerdictMove"] = (
            "No. The criterion is a validity FLAG and not a term in any answer, and C-0137 " +
                "already measured what the same factor is worth where it IS a term: a sweep " +
                "of the effective wall charge over 16x - which CONTAINS the %.3fx this " +
                "convention spans - moves C-0017's stability margin by at most %.4f. So the " +
                "flag is maximally sensitive to exactly the factor the answer is insensitive " +
                "to. C-0017's 10 nm / 2 mM verdict stays NOT EXCLUDED, never established; " +
                "C-0005's bands stand as published; and CLAUDE.md's cylinder rule stands. " +
                "What changes is that the loop expansion is now known to be OUTSIDE its own " +
                "validity range at the actuated gap on the repulsive branch and INSIDE it on " +
                "the attractive one, rather than queued."
            ).format(bareCylinder / saturatedFace, largestMarginMovement)

        findings["ceilingAndThreshold"] = (
            "P-6's shape, for whoever wants to reopen it. The criterion would change verdict " +
                "at the bare wall if sigma_face fell below %.4f e/nm^2 at 7 nm - a %.2fx " +
                "renormalisation of the tile's gap-facing charge, deeper than Manning " +
                "condensation under Mg2+ delivers on a duplex (%.4f e/nm^2, %.2fx). " +
                "Equivalently, the bare readings become controlled at gaps above %.4f nm " +
                "(duplex cylinder) and %.4f nm (Gauss-partitioned face), which is OUTSIDE " +
                "section 3's 5-10 nm band and inside the 17-26 nm tall-layer range C-0110 " +
                "examined and refused on reach. No published sigma_eff for an origami face in " +
                "mM Mg2+ was found, and none is needed: the two ends of the family are both in " +
                "print and the verdict is constant across each."
            ).format(
                sevenNanometreThreshold.thresholdChargeDensity,
                bareFace / sevenNanometreThreshold.thresholdChargeDensity,
                manningCylinder, bareCylinder / manningCylinder,
                validityGaps.first { it.wall == "bare-cylinder" }.validityGap!!,
                validityGaps.first { it.wall == "bare-face" }.validityGap!!
            )

        findings["theManningIdentity"] = (
            "A Manning-renormalised cylinder's Gouy-Chapman length is EXACTLY the helix " +
                "radius, %.4f nm, and its Xi is exactly q^2 l_B/R = %.4f - carrying no rise, " +
                "no linear charge density, no Bjerrum length and no valency beyond the q^2. " +
                "Manning condensation is defined so that the surviving linear density is " +
                "1/(q l_B) per nm, which is precisely what mu = 1/(2 pi q l_B sigma) inverts. " +
                "So the 'renormalised duplex' reading contains no DNA chemistry at all, only " +
                "a geometric radius - which is one more reason it cannot be the input to a " +
                "criterion about counterion correlations."
            ).format(
                ChargedSurface(manningCylinder, VALENCY).gouyChapmanLength(bjerrum),
                ChargedSurface(manningCylinder, VALENCY).couplingParameter(bjerrum)
            )

        validity += listOf(
            "TRL 1-3, nothing measured. This task settles which INPUT a published inequality " +
                "is owed. It does not compute a correlation correction and it does not make " +
                "Xi = 17-24 tractable; C-0005's 'no systematic theory in the intermediate " +
                "regime' stands verbatim.",
            "Kanduc et al. is COUNTERION-ONLY - 'neglecting completely the effects of salt' - " +
                "so it has no Debye length. Every number here is a statement about that model " +
                "transferred to this gap on C-0008's counterion-dominance finding, which is a " +
                "TRANSFER and is why the whole deliverable is a rule rather than a prediction.",
            "The attractive branch's zeta is NOT measured here. What is measured is the " +
                "criterion as a function of zeta over the whole admissible range, and the " +
                "width of the excluded sliver. The device's own zeta - the ratio of the " +
                "electrode's charge to the tile's - has never been computed in this corpus.",
            "The branch boundary Dtilde* = (1+zeta)/|zeta| is DERIVED here from Kanduc " +
                "Eq. (18) in the alpha -> 0 limit, not quoted; the paper states the " +
                "qualitative fact ('they attract at large separations and a repulsion emerges " +
                "only at sufficiently small separations') and prints the locus only as a " +
                "figure. The derivation is asserted as a gate against Eq. (18) itself.",
            "AT the branch boundary neither criterion applies, by the paper's own words: " +
                "'for p0 = 0 ... the leading order term is zero and the fluctuations are " +
                "dominant at any finite value of Xi'. The excluded slivers reported here sit " +
                "adjacent to exactly that locus.",
            "sigma_face = rho t/2 is Gauss's law on a UNIFORMLY charged slab. The Gen-1 tile " +
                "is a lattice of duplexes and its face is corrugated at the interhelical " +
                "pitch; the smearing is the criterion's own idealisation and is exact only " +
                "for the exterior field far from the corrugation.",
            "The tile thickness used is section 3's 10 nm, which DnaOrigamiTile carries with " +
                "its own recorded inconsistency against 'single-layer honeycomb'. Both " +
                "readings are carried as separate candidates and both fail.",
            "The criterion is evaluated at the TILE, which is Kanduc's sigma_1 (the " +
                "larger-magnitude wall) at the operating biases this programme uses. If a " +
                "bias made the electrode the larger-magnitude wall, sigma_1 would be the " +
                "electrode's and the whole question would not arise - a metal's charge has no " +
                "bare/renormalised ambiguity."
        )

        openQuestions += listOf(
            "The device's asymmetry parameter zeta has never been computed. It decides which " +
                "of Kanduc's two branches the gap is on at any given bias, and at zero " +
                "applied bias - where C-0021's contact potential and the gold PZC decide the " +
                "electrode's sign and magnitude - it may put the gap back on the REPULSIVE " +
                "branch, where the bare reading fails. One Stern-series evaluation per state " +
                "would settle it.",
            "Whether a criterion should ever be evaluated at a renormalised charge is a " +
                "general question this task answers only for Kanduc Eq. (64)/(65). The same " +
                "argument would have to be re-made for any criterion whose derivation does " +
                "not fix the counterion count by electroneutrality.",
            "No published effective charge for an origami face in mM Mg2+ was found. It is " +
                "not needed for this verdict, because both ends of the family are in print " +
                "and the verdict is constant across each - but it is the quantity a bench " +
                "measurement would supply."
        )
    } catch (exception: Exception) {
        findings["PROSE_FORMATTING_FAILED"] = exception.toString()
        output.writeText(
            json.encodeToString(
                json.encodeToJsonElement(
                    result.copy(
                        findings = findings, validity = validity, openQuestions = openQuestions
                    )
                ).roundedForResult(floor = 1.0e-14)
            ) + "\n"
        )
        throw exception
    }

    output.writeText(
        json.encodeToString(
            json.encodeToJsonElement(
                result.copy(findings = findings, validity = validity, openQuestions = openQuestions)
            ).roundedForResult(
                digitsByKey = mapOf(
                    "relativeDeparture" to DEPARTURE_SIGNIFICANT_DIGITS,
                    "departure" to DEPARTURE_SIGNIFICANT_DIGITS,
                    "bisectionDeparture" to DEPARTURE_SIGNIFICANT_DIGITS,
                    "orderingsAgreeOnTotal" to DEPARTURE_SIGNIFICANT_DIGITS,
                    "couplingOverReducedGapDeparture" to DEPARTURE_SIGNIFICANT_DIGITS,
                    "logResidualAtRoot" to DEPARTURE_SIGNIFICANT_DIGITS
                ),
                floor = 1.0e-14
            )
        ) + "\n"
    )
    println("wrote ${output.path}")
    println(
        "threshold at $OPERATING_GAP nm: Xi < ${sevenNanometreThreshold.thresholdCoupling}, " +
            "sigma < ${sevenNanometreThreshold.thresholdChargeDensity} e/nm^2"
    )
    println("bare cylinder ${bareSeven.coupling} vs ${bareSeven.bound}: ${bareSeven.satisfied}")
    println("bare face     ${faceSeven.coupling} vs ${faceSeven.bound}: ${faceSeven.satisfied}")
    println(
        "saturated     ${saturatedSeven.coupling} vs ${saturatedSeven.bound}: " +
            "${saturatedSeven.satisfied}"
    )
    falsifiers.forEach { println("${it.id} fired=${it.fired}: ${it.outcome}") }
}
