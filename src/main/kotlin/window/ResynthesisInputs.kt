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

package com.xemantic.nano.plentyofroom.window

import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.io.File
import kotlin.math.abs
import kotlin.math.ln

/**
 * The readers `T-25` consumes iteration 4 through.
 *
 * Same discipline as `T-2`'s [readScfResults] and friends: a number copied from a claim's
 * prose is a transcription risk, a number read from the file the claim was written from is
 * not. What is added here is a **keying** discipline — `C-0026` was caught taking the wrong
 * record because it keyed on too few dimensions, so every accessor below takes **every**
 * dimension its sweep varied and `require`s exactly one match.
 *
 * Nothing here computes physics. Everything here is parsing and lookup.
 */

private val reader = Json { ignoreUnknownKeys = true }

/** §3's tile edge in nm — the length an edge collar is referred to. */
const val GEN1_TILE_EDGE: Double = 40.0

/** `C-0017`'s mandate in pN/nm: `100 pN / 3 nm`, from §3 alone. */
const val MANDATED_COUPLING_STIFFNESS: Double = 100.0 / 3.0

/** §3's acceptable stroke in nm. */
const val ACCEPTABLE_STROKE_NM: Double = 3.0

/** `C-0009`'s rigid-plate tolerance, `C-0015`'s convention — a dishing fraction of the stroke. */
const val FLATNESS_TOLERANCE: Double = 0.10

// --- T-3b, the tile edge load profile (C-0022 / CH-0026) ----------------------------------

/** One `T-3b` 2-D edge solve — the `(buffer, gap, bias)` triple it was solved at. */
@Serializable
data class EdgeProfileRecord(
    val concentration: Double,
    val gapHeight: Double,
    val appliedBias: Double,
    val biasSource: String,
    val oneDimensionalLoad: Double,
    val edgeForceFractionMinMargin: Double,
    val edgeForceFractionAdditive: Double,
    val effectiveCollarWidth: Double,
    val taperDepth: Double,
    val taperWidth: Double
)

/**
 * The multiplier `μ` a `T-3b` edge fraction stands for.
 *
 * `T-3b` emits a force **deficit** fraction, so a *negative* fraction is an *enhancement* and
 * the multiplier on the one-dimensional force is `1 − fraction`. The sign convention is
 * asserted in a gate-1 test against the same file's own collar width, because getting it
 * backwards would invert every conclusion in this task.
 */
fun edgeForceMultiplier(edgeForceFraction: Double): Double = 1.0 - edgeForceFraction

/** The logarithmic gradient of the collar multiplier between two gaps, in nm⁻¹. */
fun collarLogGradient(
    lowerMultiplier: Double,
    upperMultiplier: Double,
    lowerGap: Double,
    upperGap: Double
): Double {
    require(upperGap > lowerGap) { "gaps must be ordered and distinct, were: $lowerGap, $upperGap" }
    require(lowerMultiplier > 0.0 && upperMultiplier > 0.0) {
        "multipliers must be positive, were: $lowerMultiplier, $upperMultiplier"
    }
    return ln(upperMultiplier / lowerMultiplier) / (upperGap - lowerGap)
}

/** One difference scheme's estimate of `d ln μ/dh` at one `(buffer, gap)`. */
@Serializable
data class CollarGradient(
    val concentration: Double,
    val gapHeight: Double,
    val scheme: String,
    val logGradient: Double
)

// --- T-1f, the fluctuation corrections (C-0019) -------------------------------------------

/** One `T-1f` propagation record — a licensed multiplicative bracket on a named quantity. */
@Serializable
data class FluctuationPropagation(
    val quantity: String,
    val designPoint: String,
    val baseline: Double,
    val low: Double,
    val high: Double,
    val note: String
)

// --- T-13, the zero-bias resting position (C-0021 / CH-0024) ------------------------------

/** One `T-13` zero-bias equilibrium — `(scenario, model, height, density)` identifies it. */
@Serializable
data class RestingEquilibrium(
    val scenario: String,
    val model: String,
    val layerHeight: Double,
    val graftingDensity: Double,
    val holdDownAtOnsetHeight: Double,
    val restingHeight: Double? = null,
    val strokeLost: Double? = null,
    val deliveredStrokeToWorkingPoint: Double? = null
)

/** Which hold-down stack the delivered stroke is read against. */
enum class HoldDownReading(val scenario: String?) {

    /** No hold-down at all — the `L₀` coordinate `C-0012` and `C-0016` are written in. */
    NONE(null),

    /**
     * `C-0023`'s committed device: van der Waals + the residual field + the coupling, with
     * `C-0014`'s eight substrate tethers **removed**, which is what `CH-0027` takes out of
     * the design. Above `L₀` a two-sided coupling adds no *downward* force, so the descent is
     * the same first-order balance as the one-sided reading and `T-23`'s own descent bracket
     * is the cross-check.
     */
    TETHERLESS("THE DEVICE without any tether: van der Waals + field + K2, METAL electrode"),

    /** `C-0021`'s device as `CH-0024` quotes it — the same stack **with** the eight tethers. */
    TETHERED("THE DEVICE: all mechanisms + C-0017's K2 coupling, METAL electrode")
}

/**
 * The descent below `L₀` a hold-down force produces against a coupling and a layer, in nm.
 *
 * First order in the layer's own stiffness at `L₀`: `d = F/(k_c + k_layer)`. `T-13` solved
 * the same balance non-linearly at one grafting density per height; the transfer onto the
 * window's own grid is **checked** at those shared points by [ResynthesisInputs.descentTransferLicence]
 * and reported as licensed or not, exactly as `C-0016` checks `T-3`'s transfer.
 */
fun descentUnderHoldDown(
    holdDownForce: Double,
    couplingStiffness: Double,
    layerStiffnessAtOnset: Double
): Double {
    require(holdDownForce >= 0.0) { "holdDownForce must not be negative, was: $holdDownForce" }
    require(couplingStiffness >= 0.0) {
        "couplingStiffness must not be negative, was: $couplingStiffness"
    }
    require(layerStiffnessAtOnset >= 0.0) {
        "layerStiffnessAtOnset must not be negative, was: $layerStiffnessAtOnset"
    }
    val total = couplingStiffness + layerStiffnessAtOnset
    require(total > 0.0) { "nothing resists the descent: both stiffnesses are zero" }
    return holdDownForce / total
}

/** Whether the per-point descent transfer lands inside `T-13`'s own bracket at a shared point. */
@Serializable
data class DescentTransferLicence(
    val layerHeight: Double,
    val graftingDensity: Double,
    val holdDownForce: Double,
    val transferred: Double,
    val upstreamLow: Double,
    val upstreamHigh: Double,
    val licensed: Boolean,
    val statement: String
)

// --- T-16, the output coupling (C-0017) ---------------------------------------------------

/** One `T-16` requirement record — `(model, height, buffer)` identifies it. */
@Serializable
data class CouplingRequirement(
    val model: String,
    val layerHeight: Double,
    val graftingDensity: Double,
    val concentration: Double,
    val heldGap: Double,
    val simultaneousTargetBias: Double,
    val electrostaticForceAtTarget: Double,
    val brushStiffnessAtHeldGap: Double,
    val electrostaticStiffnessAtTarget: Double,
    val effectiveStiffnessAtTarget: Double,
    val forceDecayLengthAtTarget: Double,
    val stabilityFloor: Double,
    val stabilityMargin: Double? = null,
    val mandatedStiffnessIsStable: Boolean,
    val heldVolumeFraction: Double
)

// --- T-4, the maximum usable bias (C-0018) ------------------------------------------------

/** One `T-4` ceiling — `(model, height, density, buffer, load line)` identifies it. */
@Serializable
data class UsableBiasCeiling(
    val model: String,
    val layerHeight: Double,
    val graftingDensity: Double,
    val concentration: Double,
    val loadLine: String,
    val operatingBias: Double? = null,
    val pullInBias: Double? = null,
    val pullInStroke: Double? = null,
    val brushStiffnessAtFold: Double? = null,
    val electrostaticStiffnessAtFold: Double? = null,
    val forceDecayLengthAtFold: Double? = null,
    val bindingCeiling: String,
    val usableBias: Double,
    val margin: Double? = null,
    val operatingPointIsUsable: Boolean? = null
)

// --- T-17, the flatness saturation (CH-0034) ----------------------------------------------

/** `CH-0034`'s saturation, read off `T-17`'s own one-row-per-duplex family. */
@Serializable
data class FlatnessSaturation(
    val profile: String,
    val attachmentsAtDesign: Int,
    val dishingAtFortyFive: Double,
    val attachmentsAtSaturation: Int,
    val dishingAtSaturation: Double,
    val boughtByFiveTimesTheAttachments: Double,
    val tolerance: Double,
    val reachesTolerance: Boolean
)

// --- the assembled input set ---------------------------------------------------------------

/** Everything `T-25` reads, with the accessors that key on every dimension. */
class ResynthesisInputs(
    val scf: ScfResults,
    val layout: LayoutResults,
    val edgeProfiles: List<EdgeProfileRecord>,
    val collarGradients: List<CollarGradient>,
    val fluctuation: List<FluctuationPropagation>,
    val restingEquilibria: List<RestingEquilibrium>,
    val couplingRequirements: List<CouplingRequirement>,
    val usableBiasCeilings: List<UsableBiasCeiling>,
    val flatnessSaturation: FlatnessSaturation
) {

    /** `T-1d`'s grafting-density grid, ascending — the axis every window edge sits on. */
    val graftingDensityGrid: List<Double> =
        scf.designPoints.filter { it.layerHeight == 5.0 }.map { it.graftingDensity }.sorted()

    /** The `T-3b` solve at exactly one `(buffer, gap, bias source)`, or a throw. */
    fun edgeProfile(
        concentration: Double,
        gapHeight: Double,
        biasSource: String
    ): EdgeProfileRecord {
        val matches = edgeProfiles.filter {
            it.concentration == concentration && it.gapHeight == gapHeight &&
                    it.biasSource == biasSource
        }
        require(matches.size == 1) {
            "the key ($concentration mM, $gapHeight nm, '$biasSource') identifies " +
                    "${matches.size} T-3b profiles, not one"
        }
        return matches.single()
    }

    /**
     * The edge multiplier at the **held gap** of a layer of resting height [layerHeight].
     *
     * This is the record `CH-0026`'s correction belongs at, and it is not the one a partial
     * key returns: at 2 mM the resting height of a 10 nm layer carries +14.7 % and its held
     * gap of 7 nm carries +10.3 %.
     */
    fun heldGapEdgeMultiplier(layerHeight: Double, concentration: Double = 2.0): Double =
        edgeForceMultiplier(
            edgeProfile(
                concentration = concentration,
                gapHeight = layerHeight - ACCEPTABLE_STROKE_NM,
                biasSource = "held at the 3 nm stroke below L0 = ${"%.1f".format(layerHeight)} nm"
            ).edgeForceFractionMinMargin
        )

    /** The bracketed `d ln μ/dh` at a `(buffer, gap)`, as `(low, high)` over the schemes. */
    fun collarGradientBracket(concentration: Double, gapHeight: Double): Pair<Double, Double> {
        val at = collarGradients.filter {
            it.concentration == concentration && it.gapHeight == gapHeight
        }
        require(at.isNotEmpty()) {
            "no collar gradient at ($concentration mM, $gapHeight nm)"
        }
        return at.minOf { it.logGradient } to at.maxOf { it.logGradient }
    }

    /** The per-gap mean edge multiplier at one buffer, ascending in gap. */
    fun collarMultipliersByGap(concentration: Double): Map<Double, Double> =
        edgeProfiles.filter { it.concentration == concentration }
            .groupBy { it.gapHeight }
            .mapValues { (_, at) ->
                at.map { edgeForceMultiplier(it.edgeForceFractionMinMargin) }.average()
            }
            .toSortedMap()

    /**
     * The edge multiplier at a gap `T-3b` did not sample, log-linearly interpolated.
     *
     * `C-0018`'s fold sits at 5.9–6.6 nm and `T-3b` sampled 5 and 7. **Outside** the sampled
     * range this throws rather than extrapolating: a collar that grows without bound would
     * eventually make the finite-tile force *increase* with the gap, which is unphysical, and
     * an extrapolation that produced it silently would be exactly the failure mode
     * `CH-0026`'s own falsifier 3 warns about.
     */
    fun collarMultiplierAt(concentration: Double, gapHeight: Double): Double {
        val byGap = collarMultipliersByGap(concentration)
        val gaps = byGap.keys.toList()
        require(gapHeight >= gaps.first() && gapHeight <= gaps.last()) {
            "gap $gapHeight nm is outside T-3b's sampled range " +
                    "[${gaps.first()}, ${gaps.last()}] at $concentration mM — this task " +
                    "interpolates and never extrapolates a collar"
        }
        val upper = gaps.indexOfFirst { it >= gapHeight }.coerceAtLeast(1)
        val (low, high) = gaps[upper - 1] to gaps[upper]
        if (high == low) return byGap.getValue(low)
        val fraction = (gapHeight - low) / (high - low)
        return kotlin.math.exp(
            ln(byGap.getValue(low)) +
                    fraction * ln(byGap.getValue(high) / byGap.getValue(low))
        )
    }

    /** The `(low, high)` collar gradient over every sampled gap inside `[gapLow, gapHigh]`. */
    fun collarGradientBracketOver(
        concentration: Double,
        gapLow: Double,
        gapHigh: Double
    ): Pair<Double, Double> {
        val at = collarGradients.filter {
            it.concentration == concentration &&
                    it.gapHeight >= gapLow - 2.0 && it.gapHeight <= gapHigh + 2.0
        }
        require(at.isNotEmpty()) {
            "no collar gradient near [$gapLow, $gapHigh] nm at $concentration mM"
        }
        return at.minOf { it.logGradient } to at.maxOf { it.logGradient }
    }

    private fun propagation(quantity: String, layerHeight: Double): FluctuationPropagation {
        val point = "${layerHeight.toInt()} nm design point"
        val matches = fluctuation.filter { it.quantity == quantity && it.designPoint == point }
        require(matches.size == 1) {
            "the key ('$quantity', '$point') identifies ${matches.size} T-1f " +
                    "propagations, not one"
        }
        return matches.single()
    }

    /** Whether `T-1f` reports a design point at this height at all. */
    fun hasFluctuationRecord(layerHeight: Double): Boolean =
        fluctuation.any { it.designPoint == "${layerHeight.toInt()} nm design point" }

    /**
     * `C-0019`'s combined licensed multiplier on the dead-load stroke at [layerHeight].
     *
     * The interaction channel and the conformational channel are separate one-sided
     * brackets and both run **longer**, so the combined high end is their product. Where
     * `T-1f` has no record — 5 nm — the nearest height's multiplier is used and the transfer
     * is reported as unlicensed rather than silently absorbed.
     */
    fun strokeMultiplier(layerHeight: Double): Double {
        val height = nearestFluctuationHeight(layerHeight)
        val interaction = propagation("stroke at 100 pN [nm], LICENSED", height)
        val conformational = fluctuation.single {
            it.quantity == "stroke at 100 pN [nm]" &&
                    it.designPoint == "${height.toInt()} nm design point" &&
                    it.note.startsWith("conformational")
        }
        return (interaction.high / interaction.baseline) *
                (conformational.high / conformational.baseline)
    }

    /**
     * `C-0019`'s multiplier on the coil overlap at [layerHeight].
     *
     * `T-1f` reports the *edge in `σ`*; at fixed chain length `Σ ∝ σ`, so an edge that moves
     * down by a factor is an overlap that moves up by its reciprocal.
     */
    fun coilOverlapMultiplier(layerHeight: Double): Double {
        val height = nearestFluctuationHeight(layerHeight)
        val edge = fluctuation.single {
            it.quantity == "coil-overlap window edge [nm^-2]" &&
                    it.designPoint == "${height.toInt()} nm design point"
        }
        return edge.baseline / edge.low
    }

    /** `C-0019`'s licensed multiplier on `k_brush` at the held gap — the low end, which binds. */
    fun brushStiffnessMultiplier(layerHeight: Double): Double {
        val height = nearestFluctuationHeight(layerHeight)
        val interaction = propagation(
            "k_brush at the held gap L0 - 3 nm [pN/nm], LICENSED", height
        )
        val conformational = fluctuation.single {
            it.quantity == "k_brush at the held gap L0 - 3 nm [pN/nm]" &&
                    it.designPoint == "${height.toInt()} nm design point" &&
                    it.note.startsWith("conformational")
        }
        return (interaction.low / interaction.baseline) *
                (conformational.low / conformational.baseline)
    }

    private fun nearestFluctuationHeight(layerHeight: Double): Double =
        if (hasFluctuationRecord(layerHeight)) layerHeight
        else listOf(7.0, 10.0).minBy { abs(it - layerHeight) }

    /** The hold-down force in pN a [reading] puts on the tile at [layerHeight]. */
    fun holdDownForce(reading: HoldDownReading, layerHeight: Double): Double {
        if (reading.scenario == null) return 0.0
        val at = restingEquilibria.filter {
            it.scenario == reading.scenario && it.layerHeight == layerHeight
        }
        require(at.isNotEmpty()) { "no T-13 equilibrium for $reading at $layerHeight nm" }
        val forces = at.map { it.holdDownAtOnsetHeight }.distinct()
        require(forces.size == 1) {
            "the hold-down force is not a property of (scenario, height) alone: $forces"
        }
        return forces.single()
    }

    /** `T-13`'s own `(low, high)` descent bracket per height, for a [reading]. */
    fun descentBracket(reading: HoldDownReading): Map<Double, Pair<Double, Double>> {
        if (reading.scenario == null) return emptyMap()
        return restingEquilibria.filter { it.scenario == reading.scenario && it.strokeLost != null }
            .groupBy { it.layerHeight }
            .mapValues { (_, group) ->
                group.minOf { it.strokeLost!! } to group.maxOf { it.strokeLost!! }
            }
    }

    /** The per-point descent transfer, checked at `T-13`'s own design points. */
    fun descentTransferLicence(reading: HoldDownReading): List<DescentTransferLicence> {
        val bracket = descentBracket(reading)
        return bracket.keys.sorted().map { height ->
            val density = restingEquilibria
                .first { it.scenario == reading.scenario && it.layerHeight == height }
                .graftingDensity
            val point = scf.designPoints.filter { it.layerHeight == height }
                .minBy { abs(it.graftingDensity - density) }
            val force = holdDownForce(reading, height)
            // the SOFTEST of the three solved interaction laws, which gives the LARGEST
            // descent and is therefore the conservative reading of a stroke shortfall
            val stiffness = point.solved.minOf { it.equilibriumStiffness }
            val transferred = descentUnderHoldDown(
                force, MANDATED_COUPLING_STIFFNESS, stiffness
            )
            val (low, high) = bracket.getValue(height)
            val licensed = roundedDecision(transferred) in low..high
            DescentTransferLicence(
                layerHeight = height,
                graftingDensity = point.graftingDensity,
                holdDownForce = force,
                transferred = transferred,
                upstreamLow = low,
                upstreamHigh = high,
                licensed = licensed,
                statement = "L0 = $height nm: the first-order descent " +
                        "${"%.4f".format(transferred)} nm against T-13's own " +
                        "${"%.4f".format(low)}-${"%.4f".format(high)} nm — " +
                        if (licensed) "INSIDE, the transfer is licensed"
                        else "OUTSIDE by ${"%.3f".format(transferred / high)}x, reported as " +
                                "an exposure and not absorbed"
            )
        }
    }

    companion object {

        /** Reads every iteration-4 result file `T-25` consumes out of [directory]. */
        fun read(directory: File): ResynthesisInputs {
            val scf = readScfResults(File(directory, "T-1d-scf-density-profile.json"))
            val layout = readLayoutResults(
                File(directory, "T-14-crossover-phase-and-registration.json")
            )
            val edge = File(directory, "T-3b-tile-edge-load-profile.json").array("profiles")
                .map { reader.decodeFromJsonElement(EdgeProfileRecord.serializer(), it) }
            val fluctuation = File(
                directory, "T-1f-mean-field-fluctuation-corrections.json"
            ).array("propagation")
                .map { reader.decodeFromJsonElement(FluctuationPropagation.serializer(), it) }
            val resting = File(directory, "T-13-zero-bias-resting-position.json")
                .array("equilibria")
                .map { reader.decodeFromJsonElement(RestingEquilibrium.serializer(), it) }
            val coupling = File(directory, "T-16-output-coupling-stiffness.json")
                .array("requirements")
                .map { reader.decodeFromJsonElement(CouplingRequirement.serializer(), it) }
            val ceilings = File(directory, "T-4-maximum-usable-bias.json").array("ceilings")
                .map { reader.decodeFromJsonElement(UsableBiasCeiling.serializer(), it) }
            return ResynthesisInputs(
                scf = scf,
                layout = layout,
                edgeProfiles = edge,
                collarGradients = collarGradientsOf(edge),
                fluctuation = fluctuation,
                restingEquilibria = resting,
                couplingRequirements = coupling,
                usableBiasCeilings = ceilings,
                flatnessSaturation = flatnessSaturationOf(
                    File(directory, "T-17-one-row-per-duplex.json")
                )
            )
        }
    }
}

/**
 * The three difference estimates of `d ln μ/dh` at every interior gap of every buffer.
 *
 * `T-3b`'s sweep varies the bias with the gap, so no series holds the bias fixed — but `μ`
 * is a function of the gap to 0.14 % at the one gap sampled at three biases, which is what
 * makes a finite difference across gaps meaningful at all. Forward, backward and central
 * schemes are all computed and the **spread between them is the reported uncertainty**,
 * rather than one scheme being chosen.
 */
private fun collarGradientsOf(profiles: List<EdgeProfileRecord>): List<CollarGradient> =
    profiles.groupBy { it.concentration }.flatMap { (concentration, group) ->
        val byGap = group.groupBy { it.gapHeight }
            .mapValues { (_, at) -> at.map { edgeForceMultiplier(it.edgeForceFractionMinMargin) }.average() }
            .toSortedMap()
        val gaps = byGap.keys.toList()
        gaps.indices.drop(1).dropLast(1).flatMap { index ->
            val (lower, here, upper) = Triple(gaps[index - 1], gaps[index], gaps[index + 1])
            listOf(
                CollarGradient(
                    concentration, here, "backward",
                    collarLogGradient(byGap.getValue(lower), byGap.getValue(here), lower, here)
                ),
                CollarGradient(
                    concentration, here, "forward",
                    collarLogGradient(byGap.getValue(here), byGap.getValue(upper), here, upper)
                ),
                CollarGradient(
                    concentration, here, "central",
                    collarLogGradient(byGap.getValue(lower), byGap.getValue(upper), lower, upper)
                )
            )
        }
    }

/** `CH-0034`'s saturation, read off `T-17`'s one-row-per-duplex family at the design profile. */
private fun flatnessSaturationOf(file: File): FlatnessSaturation {
    val root = reader.parseToJsonElement(file.readText()).jsonObject
    val profile = root.getValue("designPointProfile").toString().trim('"')
    val family = root.getValue("restoredForces").jsonArray.map { it.jsonObject }
        .filter {
            it.scalar("foundationMultiplier") == 1.0 &&
                    it.getValue("profile").toString().trim('"') == profile &&
                    it.scalar("rows") == 15.0
        }
    require(family.size >= 2) { "T-17 has no one-row-per-duplex family at '$profile'" }
    val design = family.single { it.scalar("attachments") == 45.0 }
    val densest = family.maxBy { it.scalar("attachments") }
    return FlatnessSaturation(
        profile = profile,
        attachmentsAtDesign = 45,
        dishingAtFortyFive = design.scalar("dishingOverStroke"),
        attachmentsAtSaturation = densest.scalar("attachments").toInt(),
        dishingAtSaturation = densest.scalar("dishingOverStroke"),
        boughtByFiveTimesTheAttachments =
            design.scalar("dishingOverStroke") - densest.scalar("dishingOverStroke"),
        tolerance = FLATNESS_TOLERANCE,
        reachesTolerance = densest.scalar("dishingOverStroke") <= FLATNESS_TOLERANCE
    )
}

private fun File.array(name: String) =
    reader.parseToJsonElement(readText()).jsonObject.getValue(name).jsonArray

private fun JsonObject.scalar(name: String): Double =
    getValue(name).toString().trim('"').toDouble()
