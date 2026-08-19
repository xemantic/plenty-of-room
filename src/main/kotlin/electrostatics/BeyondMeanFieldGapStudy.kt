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

import com.xemantic.nano.plentyofroom.brush.bracketedRoot
import com.xemantic.nano.plentyofroom.structure.DEPARTURE_SIGNIFICANT_DIGITS
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sqrt

/**
 * `T-50` — the beyond-mean-field exposure of the actuated tile-electrode gap, delivered as a
 * **ceiling and a threshold** rather than as the primitive-model Monte Carlo `C-0005` prices at
 * one to three weeks of wall clock.
 *
 * Emits `gpd/results/T-50-beyond-mean-field-gap.json`, deterministically — no timestamp, no step
 * count, no wall clock.
 *
 * ## The one move
 *
 * `|F_true(h, V)| = μ(h, V)·|F_PB(h, V)|`. `C-0005`'s 123–214 % is the **level** of `μ − 1`;
 * `C-0017`'s margin is a **stiffness**. At a force-pinned operating point the level is absorbed
 * into the bias (`CH-0035`), so the exposure is `g = d ln μ/dh` and nothing else — and `g` is a
 * quantity a bound can reach where the level is not.
 *
 * ## What is solved and what is arithmetic
 *
 * The threshold is arithmetic on `C-0017`'s own result file: one division per state. The ceiling
 * needs solves, and they are of the two channels through which any correction can act —
 * the **boundary condition** (an effective wall charge) and the **constitutive relation**
 * (finite ion size, the one beyond-mean-field member this repository implements).
 */

private const val FOOTPRINT = 1600.0

private const val STERN_CAPACITANCE = 20.0

private const val SEARCH_NODES = 400

private const val OPERATING_GAP = 7.0

private const val OPERATING_BUFFER = 2.0

private const val OPERATING_LAYER = 10.0

/** `C-0033`'s own fixed bias at the 10 nm / 2 mM state — the midpoint of `C-0017`'s `V*` bracket. */
private const val CHANNEL_BIAS = 0.155

private const val MANDATE = 100.0 / 3.0

/** `C-0005`'s point-ion boundary at 2 mM, `Mg²⁺` at a negative electrode / `Cl⁻` at a positive one. */
private const val POINT_ION_BOUNDARY_POSITIVE = 0.197

private const val TRUSTED_BIAS = 1.0

private const val SPECIFICATION_BIAS = 2.0

/**
 * The bias above which the search for a level-corrected operating bias gives up.
 *
 * Four times section 3's own 2 V, so a demand it cannot meet is unreachable by any margin the
 * specification could be relaxed by. The electrode charge is Stern-limited, so `|F_es|`
 * **saturates** in the bias and the demand is not merely expensive above it — it does not exist.
 */
private const val BIAS_CEILING = 8.0

/**
 * The level multipliers the leak is measured over — `C-0005`'s own 123–214 % band read as an
 * enhancement (`μ = 2.23`, `3.14`) and as the same fractional correction applied the other way
 * (`μ = 1/2.23`, `1/3.14`), with the null in the middle as the gate-2 reproduction.
 */
private val LEVEL_MULTIPLIERS = listOf(1.0 / 3.14, 1.0 / 2.23, 1.0, 2.23, 3.14)

/** The effective-charge factors the boundary channel is swept over. */
private val CHARGE_FACTORS = listOf(0.25, 0.5, 1.0, 2.0, 4.0)

private val CHANNEL_GAPS = listOf(5.0, 5.5, 6.0, 6.5, 7.0, 7.5, 8.0, 8.5, 9.0)

/** The half-step of the central difference the channel gradients are taken with. */
private val DIFFERENCE_STEPS = listOf(0.25, 0.5, 1.0)

/** `C-0005`'s own gap ladder for the one-loop ratio, so the reproduction is against its table. */
private val ONE_LOOP_GAPS = listOf(5.0, 7.0, 10.0, 15.0, 20.0)

/** `C-0005`'s published `Ξ|P⁽¹⁾|/P_PB` at [ONE_LOOP_GAPS], bare `σ`, `Mg²⁺` — **CITED**. */
private val ONE_LOOP_PUBLISHED = listOf(2.14, 1.63, 1.23, 0.89, 0.70)

/**
 * The hydrated contact distance of `Mg²⁺` and `Cl⁻` — Nightingale radii, `C-0005`'s own choice.
 * It is **larger** than the Bjerrum critical separation at `l_B = 0.714 nm`, which is the finding.
 */
private val ASSOCIATION_CONTACT_DISTANCES = listOf(
    "hydrated (Nightingale)" to HYDRATED_MAGNESIUM_RADIUS + HYDRATED_CHLORIDE_RADIUS,
    "outer-sphere, one water" to 0.50,
    "bare crystal radii" to 0.253
)

private val BUFFERS = listOf(0.5, 1.0, 2.0, 10.0)

// ------------------------------------------------------------------ upstream

/** One `C-0017` requirement record, read from `gpd/results/T-16-output-coupling-stiffness.json`. */
@Serializable
private data class T50CouplingRequirement(
    val model: String,
    val layerHeight: Double,
    val graftingDensity: Double,
    val concentration: Double,
    val heldGap: Double,
    val simultaneousTargetBias: Double,
    val targetElectrostaticForce: Double,
    val brushStiffnessAtHeldGap: Double,
    val electrostaticStiffnessAtTarget: Double,
    val effectiveStiffnessAtTarget: Double,
    val forceDecayLengthAtTarget: Double,
    val stabilityFloor: Double,
    val stabilityMargin: Double? = null
)

private fun readCouplingRequirements(file: File): List<T50CouplingRequirement> {
    val reader = Json { ignoreUnknownKeys = true }
    return reader.parseToJsonElement(file.readText()).jsonObject
        .getValue("requirements").jsonArray
        .map { reader.decodeFromJsonElement(T50CouplingRequirement.serializer(), it) }
}

// ------------------------------------------------------------------ records

@Serializable
private data class T50Threshold(
    val model: String,
    val layerHeight: Double,
    val graftingDensity: Double,
    val concentration: Double,
    val heldGap: Double,
    val pinnedForce: Double,
    val meanFieldDecayLength: Double,
    val brushStiffness: Double,
    val meanFieldEffectiveStiffness: Double,
    val meanFieldFloor: Double,
    val meanFieldMargin: Double?,
    val hasFloor: Boolean,
    val thresholdGradient: Double,
    val requiredDecayLength: Double?,
    val requiredDecayLengthRatio: Double?,
    val favourableGradientCeiling: Double,
    val thresholdOverFavourableCeiling: Double
)

@Serializable
private data class T50LevelLeak(
    val model: String,
    val concentration: Double,
    val heldGap: Double,
    val levelMultiplier: Double,
    val pinnedForce: Double,
    val demandedMeanFieldForce: Double,
    val attainableAtBiasCeiling: Double,
    val attainableAtSpecificationBias: Double,
    val reachable: Boolean,
    val reachableWithinSpecificationBias: Boolean,
    val meanFieldBias: Double,
    val correctedBias: Double?,
    val biasRatio: Double?,
    val meanFieldDecayLength: Double,
    val correctedDecayLength: Double?,
    val decayLengthRatio: Double?,
    val meanFieldFloor: Double,
    val correctedFloor: Double?,
    val floorLeak: Double?,
    val equivalentGradient: Double?,
    val leakOverThreshold: Double?,
    val meanFieldMargin: Double?,
    val correctedMargin: Double?,
    val withinPointIonBoundary: Boolean,
    val withinTrustedBias: Boolean,
    val withinSpecificationBias: Boolean
)

@Serializable
private data class T50MultiplierPoint(
    val channel: String,
    val label: String,
    val concentration: Double,
    val appliedBias: Double,
    val gapHeight: Double,
    val referenceForce: Double,
    val modifiedForce: Double,
    val multiplier: Double
)

@Serializable
private data class T50ChannelGradient(
    val channel: String,
    val label: String,
    val concentration: Double,
    val appliedBias: Double,
    val gapHeight: Double,
    val differenceStep: Double,
    val level: Double,
    val gradient: Double,
    val gradientOverThreshold: Double,
    val favourable: Boolean
)

/**
 * One measured family member carried onto `C-0017`'s binding state as a **net** movement.
 *
 * A correction reaches the floor through both of its channels at once — the level, which re-solves
 * the bias and therefore moves the decay length, and the gradient, which enters as `−|F| g`. The
 * two are added here rather than reported apart, because a member whose level is adverse and whose
 * gradient is favourable is neither.
 */
@Serializable
private data class T50MemberEffect(
    val channel: String,
    val label: String,
    val level: Double,
    val gradient: Double,
    val correctedBias: Double?,
    val correctedDecayLength: Double?,
    val levelContribution: Double?,
    val gradientContribution: Double,
    val correctedFloor: Double?,
    val correctedMargin: Double?,
    val marginRatio: Double?,
    val favourable: Boolean?
)

/** The level multiplier at which `C-0017`'s margin reaches exactly one, at one state. */
@Serializable
private data class T50LevelThreshold(
    val model: String,
    val concentration: Double,
    val heldGap: Double,
    val meanFieldFloor: Double,
    val meanFieldMargin: Double?,
    val criticalLevelMultiplier: Double?,
    val criticalBias: Double?,
    val criticalDecayLength: Double?,
    val direction: String,
    val floorPerEfoldOfLevel: Double
)

@Serializable
private data class T50BulkDecay(
    val concentration: Double,
    val ionicStrength: Double,
    val debyeLength: Double,
    val inverseDebyeLength: Double,
    val ionDiameter: Double,
    val reducedIonDiameter: Double,
    val msaInverseScreeningLength: Double,
    val msaScreeningLength: Double,
    val msaGradient: Double,
    val associationLabel: String,
    val contactDistance: Double,
    val bjerrumCriticalSeparation: Double,
    val associationVolume: Double,
    val pairedFraction: Double,
    val associatedIonicStrength: Double,
    val associationGradient: Double,
    val bulkGradient: Double,
    val bulkGradientOverThreshold: Double,
    val withinLimitingLawWindow: Boolean,
    val kirkwoodConcentration: Double
)

/**
 * One criterion or closed form the literature supplies, evaluated on this device.
 *
 * `gpd/data/T-50-beyond-mean-field-literature.md` carries the sources and the verbatim passages
 * with their read flags; every number here is re-derived from the published closed form rather
 * than transcribed.
 */
@Serializable
private data class T50LiteratureCriterion(
    val name: String,
    val reading: String,
    val quantity: Double,
    val bound: Double,
    val satisfied: Boolean,
    val consequenceForGradient: String
)

@Serializable
private data class T50OneLoopShape(
    val gapHeight: Double,
    val reducedGap: Double,
    val deviation: Double,
    val publishedDeviation: Double,
    val logGradient: Double?
)

@Serializable
private data class T50Ceiling(
    val channel: String,
    val statement: String,
    val kind: String,
    val largestGradientMagnitude: Double,
    val sign: String,
    val overThreshold: Double,
    val movesTheVerdict: Boolean
)

@Serializable
private data class T50Convergence(
    val quantity: String,
    val axis: String,
    val coarse: Double,
    val fine: Double,
    val finer: Double,
    val departure: Double
)

@Serializable
private data class T50Reproduction(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val relativeDeparture: Double
)

@Serializable
private data class T50Falsifier(
    val id: String,
    val statement: String,
    val fired: Boolean,
    val outcome: String
)

@Serializable
private data class T50Result(
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
    val thresholds: List<T50Threshold>,
    val bindingThreshold: T50Threshold,
    val levelLeak: List<T50LevelLeak>,
    val levelThresholds: List<T50LevelThreshold>,
    val memberEffects: List<T50MemberEffect>,
    val multiplierPoints: List<T50MultiplierPoint>,
    val channelGradients: List<T50ChannelGradient>,
    val bulkDecay: List<T50BulkDecay>,
    val oneLoopShape: List<T50OneLoopShape>,
    val literatureCriteria: List<T50LiteratureCriterion>,
    val ceilings: List<T50Ceiling>,
    val convergence: List<T50Convergence>,
    val reproductions: List<T50Reproduction>,
    val falsifiers: List<T50Falsifier>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

// ------------------------------------------------------------------ the field

private class T50Sampler(
    val tileCharge: Double,
    val bjerrumLength: Double,
    val ionModel: IonModel,
    val nodes: Int = DEFAULT_GAP_MESH_NODES
) {

    private val medium = uniformMedium(GapMedium())

    private val stern = sternChargeDensityPerVolt(STERN_CAPACITANCE)

    /**
     * Memoised on the exact `(gap, bias)` pair.
     *
     * Every channel below is a **ratio** of two solves at the same abscissa, and a bisection
     * re-visits its own endpoints, so the same solve is asked for many times. The cache is keyed
     * on the `Double` pair rather than on a rounded one, so it can only ever return the value the
     * solver would have returned — it changes the cost and not the answer.
     */
    private val cache = HashMap<Pair<Double, Double>, Double>()

    fun force(gap: Double, bias: Double): Double = cache.getOrPut(gap to bias) {
        val diffuse = diffusePotentialOfAppliedBias(
            gap, bias, tileCharge, stern, ionModel, medium, bjerrumLength, nodes = SEARCH_NODES
        )
        PoissonBoltzmannGap(gap, ionModel, medium, bjerrumLength, nodes = nodes)
            .solve(diffuse / thermalVoltage(), tileCharge)
            .forceOnTile(FOOTPRINT)
    }

    fun magnitude(gap: Double, bias: Double): Double = abs(force(gap, bias))

    /** `d ln|F|/dh` at fixed applied bias, by central difference over `2·step`. */
    fun logGradient(gap: Double, bias: Double, step: Double): Double =
        (ln(magnitude(gap + step, bias)) - ln(magnitude(gap - step, bias))) / (2.0 * step)

    /** `ℓ = −1/(d ln|F|/dh)`, `C-0017`'s convention. */
    fun decayLength(gap: Double, bias: Double, step: Double): Double = -1.0 / logGradient(gap, bias, step)

    /** The applied bias at which `|F_es(gap, V)|` equals [target] pN. */
    fun biasForMagnitude(gap: Double, target: Double, low: Double = 1.0e-3, high: Double = 8.0): Double =
        bracketedRoot(low, high, tolerance = 1.0e-11) { bias -> magnitude(gap, bias) - target }

}

// ------------------------------------------------------------------ deliverables

private fun thresholds(requirements: List<T50CouplingRequirement>): List<T50Threshold> =
    requirements.map { state ->
        val gradient = thresholdGradient(
            state.effectiveStiffnessAtTarget, state.targetElectrostaticForce, MANDATE
        )
        val required = decayLengthUnderGradient(state.forceDecayLengthAtTarget, gradient)
        val ceiling = unscreenedGradientCeiling(state.forceDecayLengthAtTarget)
        T50Threshold(
            model = state.model,
            layerHeight = state.layerHeight,
            graftingDensity = state.graftingDensity,
            concentration = state.concentration,
            heldGap = state.heldGap,
            pinnedForce = state.targetElectrostaticForce,
            meanFieldDecayLength = state.forceDecayLengthAtTarget,
            brushStiffness = state.brushStiffnessAtHeldGap,
            meanFieldEffectiveStiffness = state.effectiveStiffnessAtTarget,
            meanFieldFloor = state.stabilityFloor,
            meanFieldMargin = state.stabilityMargin,
            hasFloor = state.stabilityFloor > 0.0,
            thresholdGradient = gradient,
            requiredDecayLength = required,
            requiredDecayLengthRatio = required?.let { it / state.forceDecayLengthAtTarget },
            favourableGradientCeiling = ceiling,
            thresholdOverFavourableCeiling = abs(gradient) / ceiling
        )
    }

private fun levelLeak(
    sampler: T50Sampler,
    requirements: List<T50CouplingRequirement>,
    binding: Double
): List<T50LevelLeak> = requirements
    .filter { it.layerHeight == OPERATING_LAYER && it.concentration == OPERATING_BUFFER }
    .flatMap { state ->
        LEVEL_MULTIPLIERS.map { level ->
            val demanded = state.targetElectrostaticForce / level
            val attainable = sampler.magnitude(state.heldGap, BIAS_CEILING)
            val attainableAtSpec = sampler.magnitude(state.heldGap, SPECIFICATION_BIAS)
            val reachable = demanded < attainable
            val bias = if (reachable) sampler.biasForMagnitude(state.heldGap, demanded) else null
            val decay = bias?.let { sampler.decayLength(state.heldGap, it, 0.5) }
            val floor = decay?.let {
                max(0.0, state.targetElectrostaticForce / it - state.brushStiffnessAtHeldGap)
            }
            val leak = floor?.let { it - state.stabilityFloor }
            T50LevelLeak(
                model = state.model,
                concentration = state.concentration,
                heldGap = state.heldGap,
                levelMultiplier = level,
                pinnedForce = state.targetElectrostaticForce,
                demandedMeanFieldForce = demanded,
                attainableAtBiasCeiling = attainable,
                attainableAtSpecificationBias = attainableAtSpec,
                reachable = reachable,
                reachableWithinSpecificationBias = demanded < attainableAtSpec,
                meanFieldBias = state.simultaneousTargetBias,
                correctedBias = bias,
                biasRatio = bias?.let { it / state.simultaneousTargetBias },
                meanFieldDecayLength = state.forceDecayLengthAtTarget,
                correctedDecayLength = decay,
                decayLengthRatio = decay?.let { it / state.forceDecayLengthAtTarget },
                meanFieldFloor = state.stabilityFloor,
                correctedFloor = floor,
                floorLeak = leak,
                equivalentGradient = leak?.let { -it / state.targetElectrostaticForce },
                leakOverThreshold = leak?.let { abs(it / state.targetElectrostaticForce) / binding },
                meanFieldMargin = state.stabilityMargin,
                correctedMargin = floor?.let { if (it > 0.0) MANDATE / it else null },
                withinPointIonBoundary = bias != null && bias < POINT_ION_BOUNDARY_POSITIVE,
                withinTrustedBias = bias != null && bias < TRUSTED_BIAS,
                withinSpecificationBias = bias != null && bias < SPECIFICATION_BIAS
            )
        }
    }

/**
 * The **net** movement of `C-0017`'s binding state under one measured member, level and gradient
 * together.
 *
 * `floor = |F| (1/ℓ(V*) − g) − k_brush` with `V*` the bias at which `μ(h)|F_PB(h, V*)| = |F|`.
 * Split into the two contributions so the reader can see which one the member acts through, and
 * summed because a design feels only the sum.
 */
private fun memberEffect(
    sampler: T50Sampler,
    state: T50CouplingRequirement,
    channel: T50ChannelGradient
): T50MemberEffect {
    val demanded = state.targetElectrostaticForce / channel.level
    val reachable = demanded < sampler.magnitude(state.heldGap, BIAS_CEILING)
    val bias = if (reachable) sampler.biasForMagnitude(state.heldGap, demanded) else null
    val decay = bias?.let { sampler.decayLength(state.heldGap, it, 0.5) }
    val levelTerm = decay?.let {
        state.targetElectrostaticForce / it - state.targetElectrostaticForce / state.forceDecayLengthAtTarget
    }
    val gradientTerm = -state.targetElectrostaticForce * channel.gradient
    val floor = levelTerm?.let { max(0.0, state.stabilityFloor + it + gradientTerm) }
    val margin = floor?.let { if (it > 0.0) MANDATE / it else null }
    return T50MemberEffect(
        channel = channel.channel,
        label = channel.label,
        level = channel.level,
        gradient = channel.gradient,
        correctedBias = bias,
        correctedDecayLength = decay,
        levelContribution = levelTerm,
        gradientContribution = gradientTerm,
        correctedFloor = floor,
        correctedMargin = margin,
        marginRatio = margin?.let { state.stabilityMargin?.let { base -> it / base } },
        favourable = floor?.let { it < state.stabilityFloor }
    )
}

/**
 * The level multiplier at which the margin reaches exactly one, by bisection on `ln μ`.
 *
 * The floor is **monotone decreasing** in `μ` — a larger true force is delivered at a smaller
 * bias, where the layer's counterion content is lower and the decay length is longer — so the
 * threshold is one-sided: it lies below one, and every enhancement improves the margin.
 */
private fun levelThreshold(
    sampler: T50Sampler,
    state: T50CouplingRequirement
): T50LevelThreshold {
    fun floorAt(level: Double): Double? {
        val demanded = state.targetElectrostaticForce / level
        if (demanded >= sampler.magnitude(state.heldGap, BIAS_CEILING)) return null
        val bias = sampler.biasForMagnitude(state.heldGap, demanded)
        return state.targetElectrostaticForce / sampler.decayLength(state.heldGap, bias, 0.5) -
            state.brushStiffnessAtHeldGap
    }
    val perEfold = ((floorAt(kotlin.math.E) ?: Double.NaN) - (floorAt(1.0) ?: Double.NaN))
    val low = 1.0 / (state.targetElectrostaticForce.let { f ->
        sampler.magnitude(state.heldGap, BIAS_CEILING) / f
    }) * 1.0000001
    val critical = if ((floorAt(low) ?: 0.0) > MANDATE && (floorAt(1.0) ?: 0.0) < MANDATE) {
        bracketedRoot(ln(low), 0.0, tolerance = 1.0e-9) { logLevel ->
            (floorAt(exp(logLevel)) ?: Double.MAX_VALUE) - MANDATE
        }.let { exp(it) }
    } else null
    val criticalBias = critical?.let {
        sampler.biasForMagnitude(state.heldGap, state.targetElectrostaticForce / it)
    }
    return T50LevelThreshold(
        model = state.model,
        concentration = state.concentration,
        heldGap = state.heldGap,
        meanFieldFloor = state.stabilityFloor,
        meanFieldMargin = state.stabilityMargin,
        criticalLevelMultiplier = critical,
        criticalBias = criticalBias,
        criticalDecayLength = criticalBias?.let { sampler.decayLength(state.heldGap, it, 0.5) },
        direction = "a SUPPRESSION of the mean-field force; every enhancement raises the margin",
        floorPerEfoldOfLevel = perEfold
    )
}

/** `mu_GC = 1/(2 pi q l_B sigma_s)` at the duplex surface — `C-0005`'s own construction. */
private fun gouyChapmanOf(tile: DnaOrigamiTile, bjerrumLength: Double): Double =
    1.0 / (2.0 * PI * 2.0 * bjerrumLength * tile.duplexSurfaceChargeDensity)

/** `Xi = q^2 l_B/mu_GC` at the same surface. */
private fun couplingParameterOf(tile: DnaOrigamiTile, bjerrumLength: Double): Double =
    4.0 * bjerrumLength / gouyChapmanOf(tile, bjerrumLength)

/**
 * `d ln(1 + ratio)/dh` at the operating gap, by central difference on `C-0005`'s own closed form.
 *
 * Not `d ln(ratio)/dh`: the transfer is `mu = 1 + ratio`, so the `1` is in the logarithm and it
 * damps the gradient by `ratio/(1 + ratio)`. Reporting the ratio's own log-gradient instead would
 * overstate the pessimistic corner by that factor.
 */
private fun oneLoopTransferGradient(coupling: Double, gouyChapman: Double): Double {
    val step = 0.5
    val above = 1.0 + meanFieldDeviation(coupling, (OPERATING_GAP + step) / gouyChapman)
    val below = 1.0 + meanFieldDeviation(coupling, (OPERATING_GAP - step) / gouyChapman)
    return (ln(above) - ln(below)) / (2.0 * step)
}

private fun boundaryChannel(
    tileCharge: Double,
    bjerrumLength: Double,
    ions: IonModel
): Pair<List<T50MultiplierPoint>, List<T50ChannelGradient>> {
    val reference = T50Sampler(tileCharge, bjerrumLength, ions)
    val points = mutableListOf<T50MultiplierPoint>()
    val gradients = mutableListOf<T50ChannelGradient>()
    val referenceByGap = CHANNEL_GAPS.associateWith { reference.magnitude(it, CHANNEL_BIAS) }
    for (factor in CHARGE_FACTORS) {
        val modified = T50Sampler(tileCharge * factor, bjerrumLength, ions)
        val label = "effective wall charge x $factor"
        val byGap = CHANNEL_GAPS.associateWith { modified.magnitude(it, CHANNEL_BIAS) }
        for (gap in CHANNEL_GAPS) {
            points += T50MultiplierPoint(
                channel = "boundary condition",
                label = label,
                concentration = OPERATING_BUFFER,
                appliedBias = CHANNEL_BIAS,
                gapHeight = gap,
                referenceForce = referenceByGap.getValue(gap),
                modifiedForce = byGap.getValue(gap),
                multiplier = byGap.getValue(gap) / referenceByGap.getValue(gap)
            )
        }
        for (step in DIFFERENCE_STEPS) {
            val above = modified.magnitude(OPERATING_GAP + step, CHANNEL_BIAS) /
                reference.magnitude(OPERATING_GAP + step, CHANNEL_BIAS)
            val below = modified.magnitude(OPERATING_GAP - step, CHANNEL_BIAS) /
                reference.magnitude(OPERATING_GAP - step, CHANNEL_BIAS)
            gradients += T50ChannelGradient(
                channel = "boundary condition",
                label = label,
                concentration = OPERATING_BUFFER,
                appliedBias = CHANNEL_BIAS,
                gapHeight = OPERATING_GAP,
                differenceStep = step,
                level = byGap.getValue(OPERATING_GAP) / referenceByGap.getValue(OPERATING_GAP),
                gradient = (ln(above) - ln(below)) / (2.0 * step),
                gradientOverThreshold = 0.0,
                favourable = (ln(above) - ln(below)) > 0.0
            )
        }
    }
    return points to gradients
}

private fun finiteSizeChannel(
    tileCharge: Double,
    bjerrumLength: Double,
    biases: List<Double>
): Pair<List<T50MultiplierPoint>, List<T50ChannelGradient>> {
    val buffer = MagnesiumChlorideBuffer(OPERATING_BUFFER)
    val point = IonModel(buffer.magnesiumNumberDensity)
    val reference = T50Sampler(tileCharge, bjerrumLength, point)
    val points = mutableListOf<T50MultiplierPoint>()
    val gradients = mutableListOf<T50ChannelGradient>()
    for (radius in listOf(HYDRATED_MAGNESIUM_RADIUS, HYDRATED_CHLORIDE_RADIUS)) {
        val sized = IonModel(buffer.magnesiumNumberDensity, closePackedNumberDensity(radius))
        val modified = T50Sampler(tileCharge, bjerrumLength, sized)
        val label = "finite ion size (Bikerman), close-packed radius $radius nm"
        for (bias in biases) {
            for (gap in CHANNEL_GAPS) {
                val ref = reference.magnitude(gap, bias)
                val mod = modified.magnitude(gap, bias)
                points += T50MultiplierPoint(
                    channel = "constitutive relation",
                    label = label,
                    concentration = OPERATING_BUFFER,
                    appliedBias = bias,
                    gapHeight = gap,
                    referenceForce = ref,
                    modifiedForce = mod,
                    multiplier = mod / ref
                )
            }
            for (step in DIFFERENCE_STEPS) {
                val above = modified.magnitude(OPERATING_GAP + step, bias) /
                    reference.magnitude(OPERATING_GAP + step, bias)
                val below = modified.magnitude(OPERATING_GAP - step, bias) /
                    reference.magnitude(OPERATING_GAP - step, bias)
                val level = modified.magnitude(OPERATING_GAP, bias) /
                    reference.magnitude(OPERATING_GAP, bias)
                gradients += T50ChannelGradient(
                    channel = "constitutive relation",
                    label = label,
                    concentration = OPERATING_BUFFER,
                    appliedBias = bias,
                    gapHeight = OPERATING_GAP,
                    differenceStep = step,
                    level = level,
                    gradient = (ln(above) - ln(below)) / (2.0 * step),
                    gradientOverThreshold = 0.0,
                    favourable = (ln(above) - ln(below)) > 0.0
                )
            }
        }
    }
    return points to gradients
}

private fun bulkDecay(bjerrumLength: Double): List<T50BulkDecay> {
    val diameter = 2.0 * HYDRATED_MAGNESIUM_RADIUS
    return BUFFERS.flatMap { concentration ->
        val buffer = MagnesiumChlorideBuffer(concentration)
        val kappa = buffer.inverseDebyeLength()
        val msa = msaInverseScreeningLength(kappa, diameter)
        val critical = bjerrumCriticalSeparation(bjerrumLength, 2)
        ASSOCIATION_CONTACT_DISTANCES.map { (label, contact) ->
            val volume = bjerrumAssociationVolume(bjerrumLength, 2, contact)
            val paired = pairedMagnesiumFraction(buffer.magnesiumNumberDensity, volume)
            val strength = associatedIonicStrength(concentration, paired)
            val associated = kappa * sqrt(strength / buffer.ionicStrength)
            val msaGradient = kappa - msa
            val associationGradient = kappa - associated
            val bulk = msaGradient + associationGradient
            T50BulkDecay(
                concentration = concentration,
                ionicStrength = buffer.ionicStrength,
                debyeLength = buffer.debyeLength(),
                inverseDebyeLength = kappa,
                ionDiameter = diameter,
                reducedIonDiameter = kappa * diameter,
                msaInverseScreeningLength = msa,
                msaScreeningLength = 1.0 / msa,
                msaGradient = msaGradient,
                associationLabel = label,
                contactDistance = contact,
                bjerrumCriticalSeparation = critical,
                associationVolume = volume,
                pairedFraction = paired,
                associatedIonicStrength = strength,
                associationGradient = associationGradient,
                bulkGradient = bulk,
                bulkGradientOverThreshold = 0.0,
                withinLimitingLawWindow = kappa * diameter < LIMITING_LAW_REDUCED_DIAMETER,
                kirkwoodConcentration = concentration *
                    (KIRKWOOD_REDUCED_DIAMETER / (kappa * diameter)).let { it * it }
            )
        }
    }
}

private fun oneLoopShape(coupling: Double, gouyChapman: Double): List<T50OneLoopShape> {
    val deviations = ONE_LOOP_GAPS.map { meanFieldDeviation(coupling, it / gouyChapman) }
    return ONE_LOOP_GAPS.indices.map { index ->
        val gradient = when (index) {
            0, ONE_LOOP_GAPS.size - 1 -> null
            else -> (ln(deviations[index + 1]) - ln(deviations[index - 1])) /
                (ONE_LOOP_GAPS[index + 1] - ONE_LOOP_GAPS[index - 1])
        }
        T50OneLoopShape(
            gapHeight = ONE_LOOP_GAPS[index],
            reducedGap = ONE_LOOP_GAPS[index] / gouyChapman,
            deviation = deviations[index],
            publishedDeviation = ONE_LOOP_PUBLISHED[index],
            logGradient = gradient
        )
    }
}

// ------------------------------------------------------------------ the study

fun main() {
    val tile = DnaOrigamiTile()
    val lb = bjerrumLength()
    val surviving = tile.manningSurvivingFraction(2, lb)
    val tileCharge = -(tile.projectedChargeDensity * surviving / 2.0)
    val buffer = MagnesiumChlorideBuffer(OPERATING_BUFFER)
    val ions = IonModel(buffer.magnesiumNumberDensity)

    val requirements = readCouplingRequirements(
        File("gpd/results/T-16-output-coupling-stiffness.json")
    )
    val thresholdRecords = thresholds(requirements)
    val withFloor = thresholdRecords.filter { it.hasFloor }
    val binding = withFloor.minBy { abs(it.thresholdGradient) }
    val bindingGradient = abs(binding.thresholdGradient)

    val sampler = T50Sampler(tileCharge, lb, ions)
    val leaks = levelLeak(sampler, requirements, bindingGradient)

    val (boundaryPoints, boundaryGradientsRaw) = boundaryChannel(tileCharge, lb, ions)
    val operatingBiases = requirements
        .filter { it.layerHeight == OPERATING_LAYER && it.concentration == OPERATING_BUFFER }
        .map { it.simultaneousTargetBias }
    // The operating triple, plus T-3a's own 1 V and 2 V so the finite-size channel is a
    // REPRODUCTION of a committed result file as well as a measurement, and so the ceiling row
    // that reads "over the whole solved bias ladder" reaches biases the device never uses.
    val biasLadder = listOf(operatingBiases.min(), CHANNEL_BIAS, operatingBiases.max(), 1.0, 2.0)
    val (finitePoints, finiteGradientsRaw) = finiteSizeChannel(tileCharge, lb, biasLadder)

    val gradients = (boundaryGradientsRaw + finiteGradientsRaw).map {
        it.copy(gradientOverThreshold = abs(it.gradient) / bindingGradient)
    }
    // A ratio to "the" threshold is a ratio to the threshold OF THAT BUFFER. The 10 nm states
    // are the only ones with a floor, and their binding threshold differs 3.9x between 0.5 and
    // 2 mM, so dividing every buffer's bulk gradient by the 2 mM one would be exactly the
    // "quote it with the state it is read at" failure this corpus records twelve times.
    val thresholdByBuffer = withFloor.groupBy { it.concentration }
        .mapValues { (_, group) -> group.minOf { abs(it.thresholdGradient) } }
    val bulk = bulkDecay(lb).map {
        val own = thresholdByBuffer[it.concentration]
        it.copy(bulkGradientOverThreshold = own?.let { threshold -> abs(it.bulkGradient) / threshold } ?: 0.0)
    }

    val bindingState = requirements.first {
        it.model == binding.model && it.layerHeight == binding.layerHeight &&
            it.concentration == binding.concentration
    }
    val levelThresholds = requirements
        .filter { it.layerHeight == OPERATING_LAYER && it.concentration == OPERATING_BUFFER }
        .map { levelThreshold(sampler, it) }
    val measuredMembers = gradients
        .filter { it.differenceStep == 0.5 && (it.appliedBias == CHANNEL_BIAS || it.channel == "boundary condition") }
    // The pessimistic transfer, carried explicitly BECAUSE it is inapplicable. C-0005's ratio is
    // a LIKE-charged, counterion-only, salt-free one-loop pressure over its own leading term, and
    // the expansion it comes from has broken down. Transferred here as `mu = 1 + ratio` — the
    // reading in which the correlation term ADDS to an attraction rather than cancelling a
    // repulsion, which is the only reading with a defensible sign for two oppositely charged
    // walls — it is the largest correction anything in this corpus can be made to produce.
    val oneLoopAtOperating = meanFieldDeviation(couplingParameterOf(tile, lb), OPERATING_GAP / gouyChapmanOf(tile, lb))
    val oneLoopGradientAtOperating = oneLoopTransferGradient(couplingParameterOf(tile, lb), gouyChapmanOf(tile, lb))
    val pessimistic = T50ChannelGradient(
        channel = "C-0005's one-loop ratio, transferred",
        label = "mu = 1 + Xi|P1|/P_PB, INAPPLICABLE GEOMETRY, carried as the pessimistic corner",
        concentration = OPERATING_BUFFER,
        appliedBias = CHANNEL_BIAS,
        gapHeight = OPERATING_GAP,
        differenceStep = 0.5,
        level = 1.0 + oneLoopAtOperating,
        gradient = oneLoopGradientAtOperating,
        gradientOverThreshold = abs(oneLoopGradientAtOperating) / bindingGradient,
        favourable = oneLoopGradientAtOperating > 0.0
    )
    val memberEffects = (measuredMembers + pessimistic).map { memberEffect(sampler, bindingState, it) }
    val pessimisticEffect = memberEffects.first { it.channel == "C-0005's one-loop ratio, transferred" }
    val measuredNet = memberEffects
        .filter { it.channel != "C-0005's one-loop ratio, transferred" }
        .mapNotNull { it.correctedMargin }

    val gouyChapman = gouyChapmanOf(tile, lb)
    val couplingParameter = couplingParameterOf(tile, lb)
    val oneLoop = oneLoopShape(couplingParameter, gouyChapman)

    val twoMillimolarBulk = bulk.filter { it.concentration == OPERATING_BUFFER }
        .maxOf { abs(it.bulkGradient) }
    val measuredNetWorst = measuredNet.min()

    // ---- what the literature supplies, re-derived on this device

    val saturatedCharge = buffer.inverseDebyeLength() / (PI * lb * 2.0)
    val saturatedGouyChapman = 1.0 / (2.0 * PI * 2.0 * lb * saturatedCharge)
    val saturatedCoupling = 4.0 * lb / saturatedGouyChapman
    val hydratedDiameter = 2.0 * HYDRATED_MAGNESIUM_RADIUS
    val reducedDiameter = buffer.inverseDebyeLength() * hydratedDiameter
    val dressedB = dressedIonSecondVirialCoefficient(2)
    val dressedGradient = -dressedB * buffer.inverseDebyeLength()
    val literature = listOf(
        T50LiteratureCriterion(
            name = "Cats/Evans/Haertel/van Roij limiting-law window, d kappa_D < 0.5",
            reading = "hydrated Mg2+ diameter at " + OPERATING_BUFFER + " mM MgCl2",
            quantity = reducedDiameter,
            bound = LIMITING_LAW_REDUCED_DIAMETER,
            satisfied = reducedDiameter < LIMITING_LAW_REDUCED_DIAMETER,
            consequenceForGradient = "MSA integral-equation theory, two classical DFTs and MD " +
                "agree that the bulk charge decay length IS the Debye length inside this " +
                "window, so the bulk channel contributes ZERO to d ln mu/dh. Above the window " +
                "the decay is found SHORTER than Debye, which is the adverse direction - the " +
                "device is inside it at every buffer from 0.5 to 10 mM"
        ),
        T50LiteratureCriterion(
            name = "Kirkwood crossover of the primitive model, d kappa_D = 1.229",
            reading = "MgCl2 concentration at which this device would reach it",
            quantity = OPERATING_BUFFER *
                (KIRKWOOD_REDUCED_DIAMETER / reducedDiameter).let { it * it },
            bound = 10.0,
            satisfied = OPERATING_BUFFER *
                (KIRKWOOD_REDUCED_DIAMETER / reducedDiameter).let { it * it } > 10.0,
            consequenceForGradient = "There is no damped-oscillatory decay regime anywhere in " +
                "section 3's buffer range; the crossover is far above its top"
        ),
        T50LiteratureCriterion(
            name = "Kanduc et al. Eq. (64), weak-coupling validity, Xi < D/mu / ln(D/mu)",
            reading = "at the BARE duplex wall, mu_GC = " + gouyChapman + " nm, 7 nm gap",
            quantity = couplingParameter,
            bound = weakCouplingValidityCoupling(OPERATING_GAP / gouyChapman),
            satisfied = couplingParameter < weakCouplingValidityCoupling(OPERATING_GAP / gouyChapman),
            consequenceForGradient = "FAILS - this is the reading C-0005 uses, and it is the " +
                "reason the loop expansion is uncontrolled. But Eq. (64) is the criterion for a " +
                "REPULSIVE mean-field pressure; Eq. (65), the oppositely charged branch this " +
                "device is on, has an exponentially larger right-hand side"
        ),
        T50LiteratureCriterion(
            name = "Kanduc et al. Eq. (64), weak-coupling validity, Xi < D/mu / ln(D/mu)",
            reading = "at the charge-SATURATED gap-facing wall, mu_GC = " + saturatedGouyChapman +
                " nm, 7 nm gap",
            quantity = saturatedCoupling,
            bound = weakCouplingValidityCoupling(OPERATING_GAP / saturatedGouyChapman),
            satisfied = saturatedCoupling <
                weakCouplingValidityCoupling(OPERATING_GAP / saturatedGouyChapman),
            consequenceForGradient = "PASSES. The criterion is written for a planar wall bounding " +
                "the gap, and the gap-facing wall of this device is charge-saturated. Which " +
                "wall it is owed at is NOT settled here and the two readings are on opposite " +
                "sides of the same inequality"
        ),
        T50LiteratureCriterion(
            name = "Kanduc et al. 2017 dressed-ion second virial, b = q/(2(q+1)) exactly",
            reading = "g = -b kappa at " + OPERATING_BUFFER + " mM, read with K = 1",
            quantity = abs(dressedGradient),
            bound = bindingGradient,
            satisfied = abs(dressedGradient) < bindingGradient,
            consequenceForGradient = "The one published CLOSED FORM for the quantity T-50 wants, " +
                "and it carries no concentration, no Bjerrum length and no temperature - b is " +
                "1/3 exactly for any 2:1 salt. Read with K = 1 it is adverse and over the " +
                "threshold; but its own expansion parameter bC is 5.4 at the saturated wall and " +
                "1e11 at the bare duplex wall, so the theory refuses its own evaluation here"
        ),
        T50LiteratureCriterion(
            name = "the same theory's own validity parameter, b C",
            reading = "at the charge-saturated gap-facing wall, kappa mu_GC = " +
                (buffer.inverseDebyeLength() * saturatedGouyChapman),
            quantity = dressedB * dressedIonValidityConstant(
                buffer.inverseDebyeLength() * saturatedGouyChapman
            ),
            bound = 1.0,
            satisfied = dressedB * dressedIonValidityConstant(
                buffer.inverseDebyeLength() * saturatedGouyChapman
            ) < 1.0,
            consequenceForGradient = "K(h) = 1 - b(C + kappa h) is then large and NEGATIVE: the " +
                "second-virial expansion has failed outright, on the same axis and for a " +
                "different reason than C-0005's 123-214 %. An expansion cannot supply this answer"
        ),
        T50LiteratureCriterion(
            name = "Borukhov-Andelman-Orland / Kilic-Bazant-Ajdari steric MPB, linearised",
            reading = "the steric denominator is 1 + O(psi^2), so the linearised operator is " +
                "kappa^2 psi with the UNMODIFIED kappa",
            quantity = 0.0,
            bound = bindingGradient,
            satisfied = true,
            consequenceForGradient = "Finite ion size contributes EXACTLY zero to the far-field " +
                "gradient, at every packing fraction - one line of algebra on a published " +
                "equation. The measured near-field residue at the operating bias is what this " +
                "study's constitutive channel reports, and it is 1.4 % of the threshold"
        )
    )

    // ---- the ceilings, one per channel

    fun largest(channel: String) = gradients.filter { it.channel == channel }
        .maxOf { abs(it.gradient) }
    val boundaryCeiling = largest("boundary condition")
    val finiteCeiling = largest("constitutive relation")
    val finiteAtOperating = gradients
        .filter { it.channel == "constitutive relation" && it.appliedBias < POINT_ION_BOUNDARY_POSITIVE }
        .maxOf { abs(it.gradient) }
    val bulkCeiling = bulk.maxOf { abs(it.bulkGradient) }
    val oneLoopCeiling = abs(oneLoopGradientAtOperating)

    val ceilings = listOf(
        T50Ceiling(
            channel = "boundary condition",
            statement = "Any correction that acts by renormalising a WALL - correlation-corrected " +
                "surface charge, image charges, specific adsorption, charge regulation - reaches " +
                "the gap only through the effective boundary condition. Swept over a factor of " +
                "sixteen in effective wall charge, the largest |d ln mu/dh| that channel produces " +
                "at the operating gap is this",
            kind = "measured, in-house, over a family that brackets every surface reading in C-0005",
            largestGradientMagnitude = boundaryCeiling,
            sign = if (gradients.filter { it.channel == "boundary condition" }
                    .maxByOrNull { abs(it.gradient) }!!.favourable
            ) "favourable" else "adverse",
            overThreshold = boundaryCeiling / bindingGradient,
            movesTheVerdict = boundaryCeiling > bindingGradient
        ),
        T50Ceiling(
            channel = "constitutive relation, at the operating bias",
            statement = "Finite ion size (Bikerman) is the one beyond-mean-field member this " +
                "repository implements, and it acts on the constitutive relation rather than on " +
                "the boundary. Measured at the biases C-0017's own operating point uses",
            kind = "measured, in-house",
            largestGradientMagnitude = finiteAtOperating,
            sign = if (gradients.filter {
                    it.channel == "constitutive relation" && it.appliedBias < POINT_ION_BOUNDARY_POSITIVE
                }.maxByOrNull { abs(it.gradient) }!!.favourable
            ) "favourable" else "adverse",
            overThreshold = finiteAtOperating / bindingGradient,
            movesTheVerdict = finiteAtOperating > bindingGradient
        ),
        T50Ceiling(
            channel = "constitutive relation, over the whole solved bias ladder",
            statement = "The same member read over every bias solved here, including ones above " +
                "C-0005's own point-ion boundary where the model is outside its validity range",
            kind = "measured, in-house, PARTLY OUTSIDE ITS OWN VALIDITY RANGE",
            largestGradientMagnitude = finiteCeiling,
            sign = "mixed - the sign reverses with the bias",
            overThreshold = finiteCeiling / bindingGradient,
            movesTheVerdict = finiteCeiling > bindingGradient
        ),
        T50Ceiling(
            channel = "bulk decay constant, as the literature measures it",
            statement = "Surface forces decay at large separation with the BULK electrolyte's " +
                "own decay length; only the amplitude depends on the bodies (Haertel and " +
                "Kjellander, dressed-ion theory, read directly). So no surface convention this " +
                "project carries - Manning fraction, saturation, rim charge, Stern layer, image " +
                "charges, finite ion size at the wall - can enter the gradient at all. And the " +
                "bulk decay length of 0.5-10 mM MgCl2 IS the Debye length: the device sits at " +
                "d kappa_D = 0.089-0.488, inside the window where MSA-IET, two DFTs and MD all " +
                "agree, and the Kirkwood crossover is above 60 mM",
            kind = "CITED theorem plus CITED measurement, evaluated here",
            largestGradientMagnitude = 0.0,
            sign = "none - the channel is empty inside the limiting-law window",
            overThreshold = 0.0,
            movesTheVerdict = false
        ),
        T50Ceiling(
            channel = "bulk decay constant, the MSA trap",
            statement = "Evaluating the Waisman-Lebowitz/Blum MSA screening parameter 2 Gamma " +
                "as if it were a screening length gives a decay length 5.2 to 20.3 % LONGER " +
                "than Debye over this buffer range - the same size as the threshold and in the " +
                "opposite direction. The paper that supplies the closed form says verbatim that " +
                "1/(2 Gamma) is merely an intermediate parameter of the theory and should not " +
                "be regarded as a physical screening length. Carried here as the number that " +
                "would have been quoted and would have been wrong",
            kind = "DERIVED from a published equation, and DISAVOWED by its own source",
            largestGradientMagnitude = twoMillimolarBulk,
            sign = "favourable, and not a physical channel",
            overThreshold = twoMillimolarBulk / bindingGradient,
            movesTheVerdict = false
        ),
        T50Ceiling(
            channel = "the one-loop ratio read as a shape",
            statement = "C-0005's own Xi|P1|/P_PB differenced in the gap. It is a LIKE-charged, " +
                "counterion-only, salt-free quantity and it is NOT the multiplier - the expansion " +
                "it comes from has broken down, so mu = 1 - ratio is negative. It is carried only " +
                "as a pessimistic scale for how fast a correlation correction can vary with the gap",
            kind = "CITED formula, differenced here; INAPPLICABLE GEOMETRY, carried as a scale only",
            largestGradientMagnitude = oneLoopCeiling,
            sign = "adverse if transferred, which it must not be",
            overThreshold = oneLoopCeiling / bindingGradient,
            movesTheVerdict = oneLoopCeiling > bindingGradient
        ),
        T50Ceiling(
            channel = "the rigorous favourable bound",
            statement = "g < 1/l_PB, exactly: at g = 1/l_PB the true force is gap-independent, " +
                "which is two oppositely charged plates with no mobile ions between them at all. " +
                "Nothing screens less than nothing",
            kind = "rigorous, needs no model",
            largestGradientMagnitude = binding.favourableGradientCeiling,
            sign = "favourable",
            overThreshold = binding.favourableGradientCeiling / bindingGradient,
            movesTheVerdict = false
        )
    )

    // ---- convergence

    val convergenceRecords = mutableListOf<T50Convergence>()
    val meshSampler = { nodes: Int -> T50Sampler(tileCharge, lb, ions, nodes) }
    val sizedIons = IonModel(
        buffer.magnesiumNumberDensity, closePackedNumberDensity(HYDRATED_MAGNESIUM_RADIUS)
    )
    val meshLevels = listOf(1000, 2000, 4000)
    val meshMultiplier = meshLevels.map { nodes ->
        T50Sampler(tileCharge, lb, sizedIons, nodes).magnitude(OPERATING_GAP, CHANNEL_BIAS) /
            meshSampler(nodes).magnitude(OPERATING_GAP, CHANNEL_BIAS)
    }
    val meshGradient = meshLevels.map { nodes ->
        val sizedS = T50Sampler(tileCharge, lb, sizedIons, nodes)
        val pointS = meshSampler(nodes)
        val above = sizedS.magnitude(OPERATING_GAP + 0.5, CHANNEL_BIAS) /
            pointS.magnitude(OPERATING_GAP + 0.5, CHANNEL_BIAS)
        val below = sizedS.magnitude(OPERATING_GAP - 0.5, CHANNEL_BIAS) /
            pointS.magnitude(OPERATING_GAP - 0.5, CHANNEL_BIAS)
        (ln(above) - ln(below)) / 1.0
    }
    convergenceRecords += T50Convergence(
        quantity = "finite-size multiplier mu_B at the operating gap",
        axis = "gap mesh nodes 1000 / 2000 / 4000",
        coarse = meshMultiplier[0], fine = meshMultiplier[1], finer = meshMultiplier[2],
        departure = abs(meshMultiplier[1] - meshMultiplier[2]) / abs(meshMultiplier[2])
    )
    convergenceRecords += T50Convergence(
        quantity = "d ln mu_B/dh at the operating gap",
        axis = "gap mesh nodes 1000 / 2000 / 4000",
        coarse = meshGradient[0], fine = meshGradient[1], finer = meshGradient[2],
        departure = abs(meshGradient[1] - meshGradient[2]) / abs(meshGradient[2])
    )
    val stepGradients = DIFFERENCE_STEPS.map { step ->
        gradients.first {
            it.channel == "constitutive relation" &&
                it.differenceStep == step &&
                it.appliedBias == CHANNEL_BIAS &&
                it.label.contains(HYDRATED_MAGNESIUM_RADIUS.toString())
        }.gradient
    }
    convergenceRecords += T50Convergence(
        quantity = "d ln mu_B/dh at the operating gap",
        axis = "central-difference half-step 0.25 / 0.5 / 1.0 nm",
        coarse = stepGradients[2], fine = stepGradients[1], finer = stepGradients[0],
        departure = abs(stepGradients[1] - stepGradients[0]) / abs(stepGradients[0])
    )

    // ---- reproductions

    val reproductions = ONE_LOOP_GAPS.indices.map { index ->
        T50Reproduction(
            source = "C-0005 (T-6), one-loop deviation Xi|P1|/P_PB, Mg2+ bare sigma",
            quantity = "deviation at ${ONE_LOOP_GAPS[index]} nm",
            published = ONE_LOOP_PUBLISHED[index],
            reproduced = oneLoop[index].deviation,
            relativeDeparture = abs(oneLoop[index].deviation - ONE_LOOP_PUBLISHED[index]) /
                ONE_LOOP_PUBLISHED[index]
        )
    } + T50Reproduction(
        source = "C-0017 (T-16), binding stability floor at 10 nm / 2 mM",
        quantity = "stability floor, alexander-box(two-body)",
        published = 27.9133262,
        reproduced = stabilityFloorUnderGradient(
            binding.meanFieldEffectiveStiffness, binding.pinnedForce, 0.0
        ),
        relativeDeparture = abs(
            stabilityFloorUnderGradient(binding.meanFieldEffectiveStiffness, binding.pinnedForce, 0.0) -
                27.9133262
        ) / 27.9133262
    ) + T50Reproduction(
        source = "C-0005 (T-6), bulk Debye length at 2 mM MgCl2",
        quantity = "lambda_D",
        published = 3.927,
        reproduced = buffer.debyeLength(),
        relativeDeparture = abs(buffer.debyeLength() - 3.927) / 3.927
    ) + listOf(
        Triple(5.0, 1.0, 1.1233), Triple(7.0, 1.0, 1.1092), Triple(5.0, 2.0, 1.3173),
        Triple(7.0, 2.0, 1.2589)
    ).map { (gap, bias, published) ->
        val reproduced = finitePoints.first {
            it.gapHeight == gap && it.appliedBias == bias &&
                it.label.contains(HYDRATED_MAGNESIUM_RADIUS.toString())
        }.multiplier
        T50Reproduction(
            source = "C-0022 pipeline via T-3a's bikerman block, 2 mM, hydrated Mg2+ radius",
            quantity = "size-modified force ratio at $gap nm and $bias V",
            published = published,
            reproduced = reproduced,
            relativeDeparture = abs(reproduced - published) / published
        )
    } + T50Reproduction(
        source = "C-0005 (T-6), coupling parameter Xi at the duplex surface, Mg2+ bare sigma",
        quantity = "Xi",
        published = 24.0,
        reproduced = couplingParameter,
        relativeDeparture = abs(couplingParameter - 24.0) / 24.0
    )

    // ---- falsifiers

    val forcePinnedSpread = requirements
        .groupBy { it.model to it.layerHeight }
        .values
        .maxOf { group ->
            val values = group.map { it.targetElectrostaticForce }
            (values.max() - values.min()) / values.max()
        }
    val reached = leaks.filter { it.reachable }
    val nullLeak = leaks.filter { it.levelMultiplier == 1.0 }.maxOf { abs(it.floorLeak!!) }
    val worstLeak = reached.maxOf { abs(it.floorLeak!!) }
    val worstLeakOverThreshold = reached.maxOf { it.leakOverThreshold!! }
    val unreachable = leaks.count { !it.reachable }
    val biasBreach = reached.count { !it.withinPointIonBoundary }
    val trustedBreach = reached.count { !it.withinTrustedBias }
    val specBreach = reached.count { !it.withinSpecificationBias }
    val farFieldRatio = binding.meanFieldDecayLength / buffer.debyeLength()
    val meshDeparture = convergenceRecords[1].departure
    val stepSpread = stepGradients.max() / stepGradients.min()

    val falsifiers = listOf(
        T50Falsifier(
            id = "F1",
            statement = "The binding states are not force-pinned, so the level/gradient split is " +
                "unavailable and the exposure is the full 123-214 %",
            fired = forcePinnedSpread > 1e-9,
            outcome = ("The pinned force is identical across all three buffers at every " +
                "(model, height), to a worst relative spread of %.1e over the 18 groups. " +
                "All 54 states are force-pinned.").format(forcePinnedSpread)
        ),
        T50Falsifier(
            id = "F2",
            statement = "The level channel's residual leak is not zero - CH-0035 asserts it is " +
                "EXACTLY zero, and the identity it is asserted from does not contain the premise " +
                "that the decay length is bias-independent",
            fired = worstLeak > 1e-6,
            outcome = ("The null multiplier leaks %.2e pN/nm, which is the arithmetic's own " +
                "reproduction of C-0017. Over C-0005's own band the leak reaches %.4f pN/nm, " +
                "%.4f of the threshold, because a level correction re-solves the bias and the " +
                "decay length depends on it. The assertion is right in shape and not exactly " +
                "zero in value.").format(nullLeak, worstLeak, worstLeakOverThreshold)
        ),
        T50Falsifier(
            id = "F3",
            statement = "A measured family member's |d ln mu/dh| exceeds the threshold at a " +
                "10 nm state, so the exposure survives",
            fired = maxOf(boundaryCeiling, finiteAtOperating, twoMillimolarBulk) > bindingGradient ||
                measuredNetWorst < 1.0,
            outcome = ("Read as GRADIENTS against the 2 mM threshold of %.5f nm^-1: boundary " +
                "channel %.5f (%.3f of it), finite ion size at the operating bias %.5f (%.3f), " +
                "bulk decay at the SAME buffer %.5f (%.3f). None exceeds it, and the two that " +
                "come close run the favourable way. Read as a NET margin, which is what a " +
                "design feels, the worst of the seven measured members is %.4f against a " +
                "mean-field %.4f. The falsifier is recorded as NOT fired on the measured " +
                "family; C-0005's own one-loop ratio transferred into a geometry it does not " +
                "describe reaches %.5f nm^-1 and a net margin of %.4f, which is the " +
                "pessimistic corner and still above one.").format(
                bindingGradient, boundaryCeiling, boundaryCeiling / bindingGradient,
                finiteAtOperating, finiteAtOperating / bindingGradient, twoMillimolarBulk,
                twoMillimolarBulk / bindingGradient, measuredNetWorst,
                bindingState.stabilityMargin!!, abs(pessimisticEffect.gradient),
                pessimisticEffect.correctedMargin!!
            )
        ),
        T50Falsifier(
            id = "F4",
            statement = "The finite-size gradient is not converged to better than the spread it " +
                "is asked to resolve",
            fired = meshDeparture > 0.05,
            outcome = ("The gradient departs %.1e between the two finest meshes and spreads " +
                "%.3fx over a 4x change of difference step; the multiplier it differentiates " +
                "departs %.1e on the same solves.").format(
                meshDeparture, stepSpread, convergenceRecords[0].departure
            )
        ),
        T50Falsifier(
            id = "F5",
            statement = "The operating gap is not in the far field, so the asymptotic " +
                "level/gradient separation theorem does not apply there",
            fired = farFieldRatio < 0.95,
            outcome = ("The mean-field decay length at the binding state is %.4f nm against a " +
                "bulk Debye length of %.4f nm - a ratio of %.4f. The far-field theorem is a " +
                "STRUCTURE argument here and not a quantitative ceiling; the ceiling rests on " +
                "the two measured channels.").format(
                binding.meanFieldDecayLength, buffer.debyeLength(), farFieldRatio
            )
        ),
        T50Falsifier(
            id = "F6",
            statement = "A level correction inside C-0005's band pushes the required bias past a " +
                "ceiling",
            fired = biasBreach > 0 || trustedBreach > 0 || specBreach > 0 || unreachable > 0,
            outcome = ("Of %d level-corrected states, %d are UNREACHABLE at any bias below " +
                "%.0f V - the electrode charge is Stern-limited so |F_es| saturates - and of " +
                "the %d that are reached, %d leave C-0005's %.3f V point-ion boundary, %d leave " +
                "C-0017's %.1f V trusted ceiling and %d leave section 3's %.1f V. The level is " +
                "free in the STIFFNESS and it is not free in the BIAS.")
                .format(
                    leaks.size, unreachable, BIAS_CEILING, reached.size, biasBreach,
                    POINT_ION_BOUNDARY_POSITIVE, trustedBreach, TRUSTED_BIAS, specBreach,
                    SPECIFICATION_BIAS
                )
        )
    )

    val output = File("gpd/results/T-50-beyond-mean-field-gap.json")
    val json = Json { prettyPrint = true }
    val result = T50Result(
        task = "T-50",
        leaf = "A7.4",
        title = "Beyond mean field at the actuated tile-electrode gap: the correction reaches " +
            "C-0017's margin only through d ln mu/dh, the threshold is 0.038 nm^-1, and every " +
            "member of the family this repository can measure is far below it",
        verificationType = "logical (the level/gradient decomposition, and a threshold read off " +
            "C-0017's own force balance by one division per state) + in-silico (the level " +
            "channel's residual leak and two correction channels, solved on C-0008's 1-D " +
            "nonlinear Poisson-Boltzmann pipeline) + literature",
        acceptance = "T-50: the direction and size of the correlation correction for OPPOSITELY " +
            "charged 2:1 walls at 2-7 nm, or a demonstration that no method reaches it. " +
            "Delivered as P-6's ceiling and threshold: the largest effect any member of the " +
            "family reaches, and the value the unknown would need for the answer to change",
        maturity = "TRL 1-3. Model-consistent and traceable. NOTHING HERE IS MEASURED. The " +
            "threshold is exact given C-0017; the ceiling is a maximum over the members of the " +
            "family that can be evaluated, and the intermediate-coupling regime Xi = 17-24 " +
            "still has no systematic theory - what this task changes is what that ignorance is " +
            "ignorance ABOUT",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm",
            "gradient" to "nm^-1",
            "potential" to "V",
            "concentration" to "mM",
            "numberDensity" to "nm^-3",
            "volume" to "nm^3",
            "temperature" to "K"
        ),
        conventions = listOf(
            "z is normal to the electrode, positive AWAY from it; the electrostatic gap IS the " +
                "layer height, exactly (C-0012)",
            "the stroke s = L0 - h is positive downward; L0 is a FORCE-ONSET height (C-0011)",
            "mu(h, V) = |F_true(h, V)| / |F_PB(h, V)|, dimensionless, mu > 1 an enhancement",
            "g = d ln mu/dh in nm^-1, taken at FIXED APPLIED BIAS; g > 0 means the true force " +
                "decays more SLOWLY than the mean-field one, which makes |k_es| smaller and the " +
                "actuator MORE stable",
            "k_es = |F_es| d ln|F_es|/dh and l = -1/(d ln|F_es|/dh), so k_es = -|F_es|/l " +
                "identically; k_es < 0 above the force maximum",
            "the stability floor is max(0, -k_eff) and the margin is 33.3333 pN/nm over it " +
                "(C-0017's mandate)",
            "Xi = q^2 l_B/mu_GC is a property of ONE surface, read from the duplex CYLINDER " +
                "charge density and not the projected one (C-0005)",
            "MgCl2 is 2:1, so I = 3c at full dissociation",
            "k_BT = 4.141947 pN nm at 300 K, l_B = 0.7141 nm, eps_r = 78"
        ),
        sources = listOf("gpd/results/T-16-output-coupling-stiffness.json"),
        citedInputs = listOf(
            "C-0017's 54 requirement records - the pinned force, the decay length, the brush " +
                "stiffness, the effective stiffness and the stability floor at every state - " +
                "READ from gpd/results/T-16-output-coupling-stiffness.json and NOT re-solved",
            "C-0005's 123-214 % one-loop ratio, its Xi = 24.0 and its 0.197 V point-ion " +
                "boundary - CITED, and the ratio is REPRODUCED here from the same closed forms",
            "CH-0035 and C-0033's level/gradient split at a force-pinned operating point - the " +
                "identity this task applies to a different correction",
            "C-0008's 1-D nonlinear Poisson-Boltzmann pipeline and its Stern-series bias " +
                "inversion - CONSUMED as a library and RE-RUN, not tabulated",
            "Nightingale hydrated radii (Mg2+ 0.428 nm, Cl- 0.332 nm) - CITED through C-0005",
            "four size-modified force ratios from gpd/results/T-3a-nonlinear-pb-profile.json's " +
                "bikerman block (1.1233, 1.1092, 1.3173, 1.2589) - CITED as literals and " +
                "REPRODUCED here from the same pipeline at the same gaps and biases",
            "the MSA screening parameter closed form and Bjerrum's association integral - " +
                "CITED formulas, DERIVED evaluations, with their limits asserted as gates"
        ),
        runParameters = mapOf(
            "temperature" to "300.0",
            "bjerrumLength" to lb.toString(),
            "tileChargeDensity" to tileCharge.toString(),
            "manningSurvivingFraction" to surviving.toString(),
            "footprintArea" to FOOTPRINT.toString(),
            "sternCapacitance" to STERN_CAPACITANCE.toString(),
            "gapMeshNodes" to DEFAULT_GAP_MESH_NODES.toString(),
            "biasSearchNodes" to SEARCH_NODES.toString(),
            "operatingGap" to OPERATING_GAP.toString(),
            "operatingBuffer" to OPERATING_BUFFER.toString(),
            "channelBias" to CHANNEL_BIAS.toString(),
            "mandatedCouplingStiffness" to MANDATE.toString(),
            "levelMultipliers" to LEVEL_MULTIPLIERS.joinToString(", "),
            "chargeFactors" to CHARGE_FACTORS.joinToString(", "),
            "channelGaps" to CHANNEL_GAPS.joinToString(", "),
            "differenceSteps" to DIFFERENCE_STEPS.joinToString(", "),
            "gouyChapmanLength" to gouyChapman.toString(),
            "couplingParameter" to couplingParameter.toString()
        ),
        thresholds = thresholdRecords,
        bindingThreshold = binding,
        levelLeak = leaks,
        levelThresholds = levelThresholds,
        memberEffects = memberEffects,
        multiplierPoints = boundaryPoints + finitePoints,
        channelGradients = gradients,
        bulkDecay = bulk,
        oneLoopShape = oneLoop,
        literatureCriteria = literature,
        ceilings = ceilings,
        convergence = convergenceRecords,
        reproductions = reproductions,
        falsifiers = falsifiers,
        findings = emptyMap(),
        validity = emptyList(),
        openQuestions = emptyList()
    )

    val findings = mutableMapOf<String, String>()
    val criticalLevels = levelThresholds.mapNotNull { it.criticalLevelMultiplier }
    findings["P1 - the level channel is absorbed into the bias, and NOT exactly"] =
        ("All 54 of C-0017's states are force-pinned: the pinned electrostatic force is " +
            "identical across 0.5, 1 and 2 mM at every (model, height) to a relative spread of " +
            "%.1e. So a LEVEL correction of any size is absorbed into the bias, which is what " +
            "makes C-0005's 123-214 %% NOT the error bar on C-0017's margin. But it is absorbed " +
            "only to the extent that the decay length is bias-independent, and it is not: the " +
            "re-solved bias moves l from %.4f to %.4f nm over C-0005's own band, worth " +
            "%.4f pN/nm of floor at the worst - %.2fx the gradient threshold. CH-0035 says " +
            "EXACTLY zero; the true statement is %.4f pN/nm per e-fold of level, and it runs " +
            "FAVOURABLY for an enhancement.").format(
            forcePinnedSpread, reached.minOf { it.correctedDecayLength!! },
            reached.maxOf { it.correctedDecayLength!! }, worstLeak, worstLeakOverThreshold,
            levelThresholds.map { it.floorPerEfoldOfLevel }.maxOf { abs(it) }
        )
    findings["P2 - the two thresholds"] =
        ("GRADIENT: the margin reaches 1.0 at d ln mu/dh = %.5f nm^-1 - the %s state of the " +
            "%.0f nm layer in %.1f mM - equivalently the true force's decay length would have to " +
            "be %.4f nm against mean field's %.4f, i.e. %.2f %% SHORTER. Over the 18 states with " +
            "a floor the gradient threshold runs %.5f to %.5f nm^-1. LEVEL: the margin reaches " +
            "1.0 at mu = %.4f to %.4f, i.e. the true force would have to be %.2f to %.2fx " +
            "SMALLER than the mean-field one. Both thresholds are on the SUPPRESSION side: the " +
            "floor is monotone decreasing in the level, so every enhancement of |F_es| raises " +
            "the margin.").format(
            binding.thresholdGradient, binding.model, binding.layerHeight, binding.concentration,
            binding.requiredDecayLength!!, binding.meanFieldDecayLength,
            100.0 * (1.0 - binding.requiredDecayLengthRatio!!),
            withFloor.maxOf { it.thresholdGradient }, withFloor.minOf { it.thresholdGradient },
            criticalLevels.min(), criticalLevels.max(),
            1.0 / criticalLevels.max(), 1.0 / criticalLevels.min()
        )
    findings["P3 - the ceiling, as a NET margin"] =
        ("A correction reaches the floor through exactly two channels and a design feels only " +
            "their sum. Carried together onto C-0017's binding state, every family member this " +
            "repository can measure - a factor of SIXTEEN swept in effective wall charge, and " +
            "finite ion size at both hydrated radii - moves the margin from %.4f to between " +
            "%.4f and %.4f, i.e. by at most %.2f %%. The two channels largely cancel: at a " +
            "quarter of the wall charge the level term is %+.3f pN/nm and the gradient term " +
            "%+.3f pN/nm. Six of the seven measured members improve the margin.").format(
            bindingState.stabilityMargin!!, measuredNet.min(), measuredNet.max(),
            100.0 * maxOf(
                abs(measuredNet.max() / bindingState.stabilityMargin - 1.0),
                abs(measuredNet.min() / bindingState.stabilityMargin - 1.0)
            ),
            memberEffects.first { it.label.contains("x 0.25") }.levelContribution!!,
            memberEffects.first { it.label.contains("x 0.25") }.gradientContribution
        )
    findings["P3 - the pessimistic corner, and it does not cross"] =
        ("C-0005's own one-loop ratio is a LIKE-charged, counterion-only, salt-free pressure " +
            "over its own leading term, from an expansion that has broken down - so it is not " +
            "the multiplier and must not be transferred as a value. Transferred anyway, in the " +
            "only reading with a defensible sign for two OPPOSITELY charged walls (the " +
            "correlation term is an attraction, and the mean-field force here is already an " +
            "attraction, so it ADDS: mu = 1 + ratio = %.4f at the 7 nm gap), its gradient is " +
            "%.5f nm^-1, %.2fx the adverse threshold and the largest correction anything in " +
            "this corpus can be made to produce. Level and gradient together it takes the " +
            "margin %.4f -> %.4f. It does not cross one, and the level channel is why: an " +
            "enhancement buys %+.3f pN/nm of floor where the gradient costs %+.3f.").format(
            pessimisticEffect.level, pessimisticEffect.gradient,
            abs(pessimisticEffect.gradient) / bindingGradient, bindingState.stabilityMargin,
            pessimisticEffect.correctedMargin!!, pessimisticEffect.levelContribution!!,
            pessimisticEffect.gradientContribution
        )
    findings["P3 - the bulk channel is EMPTY, and the arithmetic that says otherwise is a trap"] =
        ("The decay length of a surface force at large separation is the BULK electrolyte's " +
            "own, and only the amplitude depends on the bodies - Haertel and Kjellander state " +
            "it in one sentence (dressed-ion theory, exact). And the bulk decay length of this " +
            "buffer IS the Debye length: the device sits at d kappa_D = %.4f at %.1f mM, inside " +
            "the d kappa_D < 0.5 window where MSA integral-equation theory, two classical DFTs " +
            "and molecular dynamics all agree, with the Kirkwood crossover at %.0f mM. So the " +
            "bulk channel's gradient is ZERO to the resolution of the four methods that have " +
            "looked. The MSA screening parameter 2 Gamma, evaluated as if it were a screening " +
            "length, gives %.5f nm^-1 - %.3f of the threshold, favourable - and its own source " +
            "says in as many words that it is not a screening length. It is carried here as the " +
            "number that would have been quoted and would have been wrong.").format(
            reducedDiameter, OPERATING_BUFFER,
            bulk.first { it.concentration == OPERATING_BUFFER }.kirkwoodConcentration,
            twoMillimolarBulk, twoMillimolarBulk / bindingGradient
        )
    findings["P4 - and mean field is BETTER behaved for oppositely charged walls, with MC"] =
        ("Kanduc, Trulsson, Naji, Burak, Forsman and Podgornik give the weak-coupling validity " +
            "criterion as Xi < D/mu / ln(D/mu) for a REPULSIVE mean-field pressure and as an " +
            "exponentially larger bound on the ATTRACTIVE branch, which is the oppositely " +
            "charged one this device is on - and back it with Monte Carlo at Xi = 0.32, 8.6 " +
            "and 86, finding that for opposite signs the weak- and strong-coupling pressures " +
            "nearly coincide at every separation. C-0005's Xi = 17-24 alarm is calibrated on " +
            "the LIKE-charged problem. Evaluated here the repulsive criterion gives %.2f at the " +
            "bare duplex wall (Xi = %.2f, FAILS) and %.2f at the charge-saturated gap-facing " +
            "wall (Xi = %.3f, PASSES) - 16.5x apart and on opposite sides of one inequality, " +
            "which this task does NOT settle.").format(
            weakCouplingValidityCoupling(OPERATING_GAP / gouyChapman), couplingParameter,
            weakCouplingValidityCoupling(OPERATING_GAP / saturatedGouyChapman), saturatedCoupling
        )
    findings["P4 - the Mg2+-does-not-condense bound does NOT transfer"] =
        "The empirical fact that Mg2+ does not condense duplex DNA at any concentration is this " +
            "project's only bound on strong-coupling correlation attraction, and it bounds the " +
            "SIGN of the force between LIKE charges - it says the correlation attraction never " +
            "exceeds the mean-field repulsion it is subtracted from. Between OPPOSITELY charged " +
            "walls there is no sign to reverse: the mean-field force is already an attraction " +
            "and the correlation term ADDS to it. So the bound has nothing to bound, and the " +
            "direction it does fix is the one that RAISES the margin here. What replaces it is " +
            "the pair of thresholds above, both of which sit on the suppression side, plus the " +
            "measured statement that no member of the family produces a suppression at all."
    findings["P5 - the verdict"] =
        ("C-0017's 10 nm / 2 mM verdict is NOT EXCLUDED, and this task does not establish it - " +
            "the intermediate-coupling regime Xi = 17-24 still has no systematic theory " +
            "(C-0005). What it does is replace an unbounded exposure with a bounded one and " +
            "name the two numbers a Monte Carlo would have to return to change the answer: a " +
            "force %.2fx smaller than mean field, or a decay length %.2f %% shorter, at a 7 nm " +
            "gap in 2 mM MgCl2. Every measured member of the family is at most %.2f %% of the " +
            "first and %.3f of the second, and NET - level and gradient together, which is what a " +
            "design feels - they move the margin by at most %.2f %%. The pessimistic transfer " +
            "of C-0005's own broken expansion lands at %.4f rather than below 1.0. The " +
            "123-214 %% is a LEVEL and the margin is not a level - that is the whole of it.")
            .format(
                1.0 / criticalLevels.max(), 100.0 * (1.0 - binding.requiredDecayLengthRatio),
                100.0 * (1.0 - memberEffects
                    .filter { it.channel != "C-0005's one-loop ratio, transferred" }
                    .minOf { it.level }),
                maxOf(boundaryCeiling, finiteAtOperating) / bindingGradient,
                100.0 * maxOf(
                    abs(measuredNet.max() / bindingState.stabilityMargin!! - 1.0),
                    abs(measuredNet.min() / bindingState.stabilityMargin!! - 1.0)
                ),
                pessimisticEffect.correctedMargin
            )
    findings["P6 - the bias is where the level DOES bite"] =
        ("A level correction is free in the stiffness and it is not free in the bias. %d of %d " +
            "level-corrected states are UNREACHABLE at any bias up to %.0f V, because the " +
            "Stern-limited electrode makes |F_es| saturate at %.1f pN at the 7 nm held gap " +
            "against %.1f pN at section 3's own 2 V; of the %d reached, %d leave C-0005's " +
            "%.3f V point-ion boundary, %d leave C-0017's 1 V trusted ceiling and %d leave " +
            "section 3's 2 V. The required bias moves %.3fx to %.3fx over C-0005's band. So a " +
            "large SUPPRESSION of the force is refused by the bias budget as well as being " +
            "unproduced by any mechanism - two independent reasons on the same side.").format(
            unreachable, leaks.size, BIAS_CEILING, leaks.maxOf { it.attainableAtBiasCeiling },
            leaks.maxOf { it.attainableAtSpecificationBias }, reached.size, biasBreach,
            POINT_ION_BOUNDARY_POSITIVE, trustedBreach, specBreach,
            reached.minOf { it.biasRatio!! }, reached.maxOf { it.biasRatio!! }
        )

    val validity = listOf(
        "TRL 1-3. Nothing here is measured. The threshold is exact GIVEN C-0017's mean-field " +
            "force balance; if that balance moves, the threshold moves with it.",
        "The level/gradient split is a property of a FORCE-PINNED operating point. It does not " +
            "hold at a fixed-bias operating point, where the level reaches the answer in full - " +
            "C-0018's free load line is exactly that case (CH-0035's own item 2).",
        "The far-field theorem that all surface physics enters the amplitude and the decay " +
            "constant is bulk is a STRUCTURE argument here, not a quantitative ceiling: the " +
            ("mean-field decay length at the operating gap is %.4f nm against a %.4f nm bulk " +
                "Debye length, a ratio of %.4f. Falsifier F5 fired and is recorded as fired.")
                .format(binding.meanFieldDecayLength, buffer.debyeLength(), farFieldRatio),
        "The boundary-condition sweep varies ONE wall. The electrode's own effective charge is " +
            "set by the Stern series at the applied bias and is not swept independently.",
        "The finite-size member is Bikerman's lattice-gas modification of PB - a STERIC " +
            "correction, not a correlation one. It is a member of the family and it is not a " +
            "proxy for the correlation term.",
        "The Bjerrum association arithmetic uses a mass-action model with a distance-cut-off " +
            "association constant and no activity coefficients. It is a bound on the direction " +
            "and an order of magnitude on the size, not a measured association constant.",
        "Xi = 17-24 at the duplex surface remains without a systematic theory (C-0005). Nothing " +
            "here computes the correlation correction; what is computed is what it would have " +
            "to do to matter.",
        "The oppositely charged pair is NOT covered by the Mg2+-does-not-condense empirical " +
            "bound - see the findings and the claim. That bound is about the SIGN of the force " +
            "between LIKE charges."
    )

    val openQuestions = listOf(
        "The intermediate-coupling gap 1 < Xi < 100 still has no systematic theory, and the " +
            "primitive-model Monte Carlo that would settle it is still priced at 1-3 weeks of " +
            "wall clock (C-0005). What has changed is the question it would be asked: not " +
            "'how big is the correction' but 'does it shorten the force's decay length by 9.7 % " +
            "at a 7 nm gap in 2 mM MgCl2'.",
        "No published Xi criterion or simulation exists for OPPOSITELY charged walls, which is " +
            "the actuated configuration - C-0005's own open item 4, and this task does not " +
            "close it.",
        "The level channel's residual leak measured here is a movement of C-0017's floor that " +
            "no claim in the corpus carries. It is small and it is not zero, and CH-0035 says " +
            "exactly zero."
    )

    output.writeText(
        json.encodeToString(
            json.encodeToJsonElement(
                result.copy(findings = findings, validity = validity, openQuestions = openQuestions)
            ).roundedForResult(
                digitsByKey = mapOf(
                    "gradient" to DEPARTURE_GRADIENT_DIGITS,
                    "floorLeak" to DEPARTURE_GRADIENT_DIGITS,
                    "equivalentGradient" to DEPARTURE_GRADIENT_DIGITS,
                    "leakOverThreshold" to DEPARTURE_GRADIENT_DIGITS,
                    "gradientOverThreshold" to DEPARTURE_GRADIENT_DIGITS
                ),
                floor = 1.0e-12
            )
        ) + "\n"
    )
    println("wrote ${output.path}")
    println("binding threshold ${binding.thresholdGradient} nm^-1 at ${binding.model}")
    falsifiers.forEach { println("${it.id} fired=${it.fired}: ${it.outcome}") }
    ceilings.forEach { println("${it.channel}: ${it.largestGradientMagnitude} = ${it.overThreshold} of threshold") }
}

/**
 * The digits a **gradient** is emitted at.
 *
 * Six, not nine. A gradient here is a difference of two logarithms of two nearly equal nonlinear
 * Poisson-Boltzmann solves, which is exactly the field `CLAUDE.md` records as the one a JIT
 * recompilation moves — the same argument as [DEPARTURE_SIGNIFICANT_DIGITS], applied to an
 * *answer* rather than to a diagnostic, so it is six rather than two.
 */
private const val DEPARTURE_GRADIENT_DIGITS: Int = 6
