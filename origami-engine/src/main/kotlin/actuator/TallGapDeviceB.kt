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

package com.xemantic.nano.plentyofroom.actuator

import com.xemantic.nano.plentyofroom.brush.AlexanderBoxLayer
import com.xemantic.nano.plentyofroom.brush.GraftedChain
import com.xemantic.nano.plentyofroom.brush.GraftedLayerModel
import com.xemantic.nano.plentyofroom.brush.InteractionFreeEnergy
import com.xemantic.nano.plentyofroom.brush.StrongStretchingLayer
import com.xemantic.nano.plentyofroom.brush.additiveInteraction
import com.xemantic.nano.plentyofroom.brush.chainLengthForHeight
import com.xemantic.nano.plentyofroom.brush.desCloizeauxInteraction
import com.xemantic.nano.plentyofroom.brush.graftedChain
import com.xemantic.nano.plentyofroom.brush.load
import com.xemantic.nano.plentyofroom.brush.stiffness
import com.xemantic.nano.plentyofroom.brush.reducedSecondVirialCoefficient
import com.xemantic.nano.plentyofroom.brush.reducedThirdVirialCoefficient
import com.xemantic.nano.plentyofroom.brush.thermalBlobKuhnSegments
import com.xemantic.nano.plentyofroom.brush.threeBodyInteraction
import com.xemantic.nano.plentyofroom.brush.twoBodyInteraction
import com.xemantic.nano.plentyofroom.electrostatics.CounterionDominatedGap
import com.xemantic.nano.plentyofroom.electrostatics.DnaOrigamiTile
import com.xemantic.nano.plentyofroom.electrostatics.MagnesiumChlorideBuffer
import com.xemantic.nano.plentyofroom.electrostatics.bjerrumLength
import com.xemantic.nano.plentyofroom.material.PegWater
import kotlinx.serialization.Serializable
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.exp
import kotlin.math.floor
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.roundToLong
import kotlin.math.sqrt

/**
 * `T-192` — the library behind device B: a 10 pN/nm coupling on a **17–26 nm** layer at 0.5 mM,
 * the corner NDI's answers to `DECISIONS-FOR-NDI.md` decisions 2 and 4 point at together.
 *
 * ## What is new here and what is re-run
 *
 * Nothing about the *field* is new: `C-0008`'s solver already sweeps 3–30 nm, and `C-0018`'s
 * [EquilibriumPath] already locates a fold as `max_s V_eq(s)`. Both are re-run as libraries.
 *
 * What is new is that **a layer height alone does not name a layer**. NDI's answer to decision 2
 * names a thickness — *"17-26 nm of polymer thickness"* — and a grafted layer needs a grafting
 * density as well. Two explicit extrapolation rules are carried, both labelled as extrapolations
 * ([tallGapHeldDensityRule] and [tallGapPowerLawFit] through §3's own three design points),
 * because reporting one would be choosing a design the specification does not contain.
 *
 * ## The conventions
 *
 * Inherited unchanged from `C-0012`/`C-0018`: `z` is normal to the electrode and positive away
 * from it, **the electrostatic gap IS the layer height**, the stroke `s = L₀ − h` is positive
 * downward, `L₀` is a **force-onset** height (`C-0011`, `CH-0010`) and not a first moment, and
 * `k_es = −∂F_z/∂h` is negative above the force maximum and positive below it (`CH-0011`).
 */

// ---------------------------------------------------------------- the placement arithmetic

/**
 * `C-0046`'s `P10`: §3's **desired** clause placed on its own arithmetic, `100 pN / 10 nm`.
 *
 * `C-0017`'s mandate is an equality on a SUM and `k_c = F/δ` contains no physics at all, so
 * reading the desired clause on the *acceptable* clause's 33.333 pN/nm is reading the wrong
 * clause's number. NDI's answer to decision 4 — *"2 devices"* — is what makes this a device
 * rather than a stretch goal.
 */
const val TALL_GAP_DEVICE_B_STIFFNESS: Double = 10.0

/** `C-0017`'s mandate, `100 pN / 3 nm` — device A, §3's **acceptable** clause. */
const val TALL_GAP_DEVICE_A_STIFFNESS: Double = 100.0 / 3.0

/** §3's force target in pN, over the 40 × 40 nm footprint. */
const val TALL_GAP_TARGET_FORCE: Double = 100.0

/** §3's **desired** stroke in nm — device B's own. */
const val TALL_GAP_DEVICE_B_STROKE: Double = 10.0

/** §3's three design points, `(L₀ in nm, σ in nm⁻²)`, as `C-0001`/`C-0012` fixed them. */
val TALL_GAP_SECTION_3_DESIGN_POINTS: List<Pair<Double, Double>> =
    listOf(5.0 to 0.092, 7.0 to 0.045, 10.0 to 0.024)

/**
 * `C-0017`'s stability floor in pN/nm — the coupling stiffness a state demands, which is
 * `|k_eff|` where `k_eff < 0` and **exactly zero** where the state is already stable.
 *
 * Zero is not a small number here: it is the statement that the state imposes no stability
 * requirement at all, and a margin taken against it is `null` rather than `Infinity`.
 */
fun tallGapStabilityFloor(effectiveStiffness: Double): Double = max(0.0, -effectiveStiffness)

/**
 * `C-0046`'s composed cap `δ ≤ F/|k_eff|` in nm — placement and stability read on opposite
 * sides of the same inequality — or `null` where the state imposes no stability floor.
 *
 * It names no coupling element at all, which is why `C-0046` could quote it as a bound on §3's
 * desired stroke owing nothing to the flexure branch.
 */
fun tallGapStrokeCap(force: Double, effectiveStiffness: Double): Double? {
    require(force > 0.0) { "force must be positive, was: $force" }
    val floor = tallGapStabilityFloor(effectiveStiffness)
    return if (floor <= 0.0) null else force / floor
}

// ---------------------------------------------------------------- the grafting-density rules

/**
 * A grafting density as a power law in the layer height, `σ = A L₀^p`.
 *
 * Both rules this task carries are members of this one family, which is what lets the study
 * report them side by side and lets the claim say what changes between them.
 */
@Serializable
data class TallGapDeviceBDensityRule(
    val name: String,
    val amplitude: Double,
    val exponent: Double
) {

    init {
        require(amplitude > 0.0) { "amplitude must be positive, was: $amplitude" }
    }

    /** `σ` in nm⁻² at layer height [height] nm. */
    fun densityAt(height: Double): Double {
        require(height > 0.0) { "height must be positive, was: $height" }
        return amplitude * height.pow(exponent)
    }
}

/**
 * The rule that holds `σ` at [density] and lengthens the chain instead.
 *
 * For a power-law interaction `L₀ ∝ N σ^q v^r`, so `φ(L₀) = N σ v₀/L₀` is **independent of `N`**:
 * holding `σ` therefore holds the layer's own volume fraction at exactly the §3 design point's
 * value and **only the height leaves the range**. That is the more conservative of the two rules
 * and the one whose thermodynamics `C-0003` actually validated.
 */
fun tallGapHeldDensityRule(density: Double): TallGapDeviceBDensityRule =
    TallGapDeviceBDensityRule("held-density", density, 0.0)

/**
 * The least-squares power law through [points], `(height, density)`, fitted in log-log.
 *
 * @throws IllegalArgumentException if fewer than two points are given or any is non-positive.
 */
fun tallGapPowerLawFit(
    name: String,
    points: List<Pair<Double, Double>>
): TallGapDeviceBDensityRule {
    require(points.size >= 2) {
        "a power law needs at least two points, was: ${points.size}"
    }
    points.forEach { (height, density) ->
        require(height > 0.0 && density > 0.0) {
            "every point must be positive, was: ($height, $density)"
        }
    }
    val x = points.map { ln(it.first) }
    val y = points.map { ln(it.second) }
    val meanX = x.average()
    val meanY = y.average()
    var covariance = 0.0
    var variance = 0.0
    for (i in x.indices) {
        covariance += (x[i] - meanX) * (y[i] - meanY)
        variance += (x[i] - meanX) * (x[i] - meanX)
    }
    require(variance > 0.0) { "the heights must not all be equal" }
    val exponent = covariance / variance
    return TallGapDeviceBDensityRule(name, exp(meanY - exponent * meanX), exponent)
}

// ---------------------------------------------------------------- the two decay lengths

/**
 * `ℓ = −1/(d ln|F|/dh)` in nm at [gap], central-differenced on [attraction] over `2·`[step].
 *
 * The separation of the two samples is named **once** here, because `CLAUDE.md` records a
 * hand-written `/(2 * step)` at three call sites as three chances to halve a gradient with no
 * dimensional check able to catch it.
 */
fun tallGapDecayLength(
    gap: Double,
    step: Double = 1e-3,
    attraction: (Double) -> Double
): Double {
    require(gap > step) { "gap must exceed the step, was: $gap against $step" }
    require(step > 0.0) { "step must be positive, was: $step" }
    val separation = 2.0 * step
    val slope = (ln(attraction(gap + step)) - ln(attraction(gap - step))) / separation
    return -1.0 / slope
}

/**
 * `CH-0004`'s counterion-dominance ratio at [gap] nm and [concentration] mM — how many of the
 * tile's own counterions the gap must hold against how many the bulk buffer would put there.
 *
 * `C-0005` measured 3–33× over the 5–10 nm §3 box and `C-0008` then showed that the dominance
 * is **not** the force's decay length. It is computed rather than transferred here because the
 * ratio is exactly `∝ 1/(c·h)` and a tall gap is where a transferred value would be wrong.
 */
fun tallGapCounterionDominance(
    gap: Double,
    concentration: Double,
    tile: DnaOrigamiTile = DnaOrigamiTile(),
    counterionValency: Int = 2,
    bjerrum: Double = bjerrumLength()
): Double = CounterionDominatedGap(
    tile = tile,
    buffer = MagnesiumChlorideBuffer(concentration),
    gapHeight = gap,
    counterionValency = counterionValency,
    chargeFraction = tile.manningSurvivingFraction(counterionValency, bjerrum)
).dominanceRatio

/** The same gap's uniform-density counterion screening length in nm — `CH-0004`'s candidate. */
fun tallGapCounterionScreeningLength(
    gap: Double,
    concentration: Double,
    tile: DnaOrigamiTile = DnaOrigamiTile(),
    counterionValency: Int = 2,
    bjerrum: Double = bjerrumLength()
): Double = CounterionDominatedGap(
    tile = tile,
    buffer = MagnesiumChlorideBuffer(concentration),
    gapHeight = gap,
    counterionValency = counterionValency,
    chargeFraction = tile.manningSurvivingFraction(counterionValency, bjerrum)
).localScreeningLength(bjerrum)

// ---------------------------------------------------------------- the layer at a tall height

/** `C-0002`'s measured osmotic second virial coefficient in `mol·cm³/g²`. */
const val TALL_GAP_OSMOTIC_SECOND_VIRIAL: Double = 1.9e-3

/** `C-0002`'s third virial coefficient in the same units. */
const val TALL_GAP_OSMOTIC_THIRD_VIRIAL: Double = 2.0e-2

/** `C-0003`'s six (profile × interaction) layer models, built here rather than tabulated. */
fun tallGapLayerModels(peg: PegWater = PegWater()): List<Pair<String, GraftedLayerModel>> =
    listOf("alexander-box", "strong-stretching").flatMap { profile ->
        listOf("two-body", "virial", "des-Cloizeaux").map { interaction ->
            val energy = tallGapInteraction(peg, interaction)
            val model: GraftedLayerModel =
                if (profile == "alexander-box") AlexanderBoxLayer(energy)
                else StrongStretchingLayer(energy)
            model.name to model
        }
    }

private fun tallGapInteraction(peg: PegWater, choice: String): InteractionFreeEnergy {
    val twoBody = twoBodyInteraction(
        peg.reducedSecondVirialCoefficient(TALL_GAP_OSMOTIC_SECOND_VIRIAL), peg.monomerVolume
    )
    val threeBody = threeBodyInteraction(
        peg.reducedThirdVirialCoefficient(TALL_GAP_OSMOTIC_THIRD_VIRIAL), peg.monomerVolume
    )
    return when (choice) {
        "two-body" -> twoBody
        "virial" -> additiveInteraction("virial", listOf(twoBody, threeBody))
        else -> desCloizeauxInteraction(peg.crossoverIndex, peg.monomerVolume)
    }
}

/**
 * The reference model for quantities that are properties of the **chain** rather than of the
 * profile — the two-body Alexander box, which is `C-0003`'s own cheapest limb.
 */
const val TALL_GAP_REFERENCE_MODEL: String = "alexander-box(two-body)"

/** Returns the model named [name], or throws — a name is a key, not a description. */
fun tallGapLayerModel(name: String, peg: PegWater = PegWater()): GraftedLayerModel =
    tallGapLayerModels(peg).firstOrNull { it.first == name }?.second
        ?: throw IllegalArgumentException(
            "no such layer model: $name, have ${tallGapLayerModels(peg).map { it.first }}"
        )

/** The chain a model needs to reach [height] nm at [graftingDensity] nm⁻². */
fun tallGapChain(
    modelName: String,
    height: Double,
    graftingDensity: Double,
    peg: PegWater = PegWater()
): GraftedChain {
    val model = tallGapLayerModel(modelName, peg)
    return peg.graftedChain(model.chainLengthForHeight(peg, height, graftingDensity), graftingDensity)
}

/** What a 17–26 nm layer costs in chain, and what it is worth in volume fraction. */
@Serializable
@Suppress("LongParameterList")
data class TallGapDeviceBLayerRecord(
    val modelName: String,
    val densityRule: String,
    val nominalHeight: Double,
    val graftingDensity: Double,
    val graftingSpacing: Double,
    val monomersPerChain: Double,
    val chainMolarMass: Double,
    val monomerVolume: Double,
    val restingHeight: Double,
    val dryThickness: Double,
    val volumeFractionAtRest: Double,
    val volumeFractionAtDeviceBStroke: Double?,
    val stretchingRatioAtRest: Double,
    val idealEndToEnd: Double,
    val layerStiffnessAtRest: Double,
    val layerLoadAtDeviceBStroke: Double?
)

/**
 * The layer census at one `(model, height, density)`, with the device-B held state beside it.
 *
 * The held state is `null` where §3's desired 10 nm stroke would take the layer below its own
 * dry thickness — `s < L₀` is `C-0050`'s identity and the dry thickness is a harder floor still.
 */
fun tallGapLayerCensus(
    modelName: String,
    height: Double,
    graftingDensity: Double,
    densityRule: String = "explicit",
    footprintArea: Double = TALL_GAP_FOOTPRINT,
    peg: PegWater = PegWater()
): TallGapDeviceBLayerRecord {
    val model = tallGapLayerModel(modelName, peg)
    val chain = peg.graftedChain(
        model.chainLengthForHeight(peg, height, graftingDensity), graftingDensity
    )
    val resting = model.equilibriumHeight(chain)
    val held = resting - TALL_GAP_DEVICE_B_STROKE
    val heldIsReal = held > chain.occupiedThickness * 1.01
    return TallGapDeviceBLayerRecord(
        modelName = modelName,
        densityRule = densityRule,
        nominalHeight = height,
        graftingDensity = graftingDensity,
        graftingSpacing = chain.graftingSpacing,
        monomersPerChain = chain.monomersPerChain,
        chainMolarMass = chain.monomersPerChain * peg.monomerMolarMass,
        monomerVolume = chain.monomerVolume,
        restingHeight = resting,
        dryThickness = chain.occupiedThickness,
        volumeFractionAtRest = chain.meanVolumeFraction(resting),
        volumeFractionAtDeviceBStroke = if (heldIsReal) chain.meanVolumeFraction(held) else null,
        stretchingRatioAtRest = chain.stretchingRatio(resting),
        idealEndToEnd = chain.idealEndToEnd,
        layerStiffnessAtRest = model.stiffness(chain, resting, footprintArea),
        layerLoadAtDeviceBStroke = if (heldIsReal) model.load(chain, held, footprintArea) else null
    )
}

/** The 40 × 40 nm footprint in nm². */
const val TALL_GAP_FOOTPRINT: Double = 1600.0

// ---------------------------------------------------------------- the scaling premises

/**
 * The premises of every scaling law this task invokes, checked against **this** material at
 * **this** layer's own working volume fraction rather than assumed from the textbook.
 */
@Serializable
@Suppress("LongParameterList")
data class TallGapDeviceBPremiseRecord(
    val nominalHeight: Double,
    val graftingDensity: Double,
    val monomersPerChain: Double,
    val kuhnSegments: Double,
    val thermalBlobKuhnSegments: Double,
    val desCloizeauxWindowRatio: Double,
    val coilOverlap: Double,
    val stretchingRatio: Double,
    val volumeFraction: Double,
    val idealEndToEnd: Double,
    val chainMolarMass: Double
)

/**
 * The premise census for a layer of [height] nm at [graftingDensity], on the reference model.
 *
 * The two that decide anything: `√(N_K/g_T)` is the des Cloizeaux window's width **exactly**
 * (`CLAUDE.md`), non-empty if and only if the chain exceeds a thermal blob; and `Σ = πR₀²σ` is
 * the only brush criterion `CLAUDE.md` says bounds anything at all.
 */
fun tallGapScalingPremises(
    height: Double,
    graftingDensity: Double,
    peg: PegWater = PegWater(),
    modelName: String = TALL_GAP_REFERENCE_MODEL
): TallGapDeviceBPremiseRecord {
    val chain = tallGapChain(modelName, height, graftingDensity, peg)
    val blob = peg.thermalBlobKuhnSegments(
        peg.reducedSecondVirialCoefficient(TALL_GAP_OSMOTIC_SECOND_VIRIAL)
    )
    return TallGapDeviceBPremiseRecord(
        nominalHeight = height,
        graftingDensity = graftingDensity,
        monomersPerChain = chain.monomersPerChain,
        kuhnSegments = chain.kuhnSegments,
        thermalBlobKuhnSegments = blob,
        desCloizeauxWindowRatio = sqrt(chain.kuhnSegments / blob),
        coilOverlap = PI * chain.idealEndToEnd * chain.idealEndToEnd * chain.graftingDensity,
        stretchingRatio = chain.stretchingRatio(height),
        volumeFraction = chain.meanVolumeFraction(height),
        idealEndToEnd = chain.idealEndToEnd,
        chainMolarMass = chain.monomersPerChain * peg.monomerMolarMass
    )
}

// ---------------------------------------------------------------- emission

/**
 * Rounds a **dimensionless** convergence or reproduction departure to two significant digits.
 *
 * `RESULT_ABSOLUTE_FLOOR` is a claim about magnitudes **in the locked units** and a ratio of two
 * nearly equal dimensionless quantities is not in them, so nine significant digits on such a
 * field makes a result file permanently un-diffable (`C-0093`, and the entry `CLAUDE.md` carries
 * about it). Two is what `CLAUDE.md` asks for and what nothing was enforcing.
 */
fun tallGapTwoSignificantDigits(value: Double): Double {
    if (!value.isFinite() || value == 0.0) return 0.0
    val scale = 10.0.pow(1.0 - floor(log10(abs(value))))
    return (value * scale).roundToLong() / scale
}

/**
 * The deepest gap in nm at which [attraction] still reaches [load] pN, or `null` when no gap in
 * `[low, high]` does.
 *
 * A threshold rather than a table: the reachability sweep answers *"does 100 pN arrive at 17,
 * 20, 23 and 26 nm"* and this answers *"how far does 100 pN arrive at all"*, which is the number
 * a specification conversation needs and the one a grid of four heights cannot carry.
 *
 * `null` is a **verdict**, not a failure — `CLAUDE.md`: a root-finder handed a target the
 * function never reaches must return `null` rather than clamp, because *"the affordable width is
 * below the model floor"* is the answer. Bisected on the **bracket width**, never on a residual.
 *
 * @throws IllegalArgumentException if the bracket is not positive and ascending.
 */
fun tallGapDeepestReachableGap(
    load: Double,
    low: Double,
    high: Double,
    tolerance: Double = 1e-7,
    attraction: (Double) -> Double
): Double? {
    require(load > 0.0) { "load must be positive, was: $load" }
    require(low > 0.0 && high > low) { "the bracket must be positive and ascending" }
    if (attraction(low) < load) return null
    if (attraction(high) >= load) return high
    var lower = low
    var upper = high
    while (upper - lower > tolerance) {
        val middle = 0.5 * (lower + upper)
        if (attraction(middle) >= load) lower = middle else upper = middle
    }
    return 0.5 * (lower + upper)
}
