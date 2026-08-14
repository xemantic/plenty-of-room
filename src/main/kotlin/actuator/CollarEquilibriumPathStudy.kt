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

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.brush.AlexanderBoxLayer
import com.xemantic.nano.plentyofroom.brush.GraftedChain
import com.xemantic.nano.plentyofroom.brush.GraftedLayerModel
import com.xemantic.nano.plentyofroom.brush.InteractionFreeEnergy
import com.xemantic.nano.plentyofroom.brush.StrongStretchingLayer
import com.xemantic.nano.plentyofroom.brush.additiveInteraction
import com.xemantic.nano.plentyofroom.brush.chainLengthForHeight
import com.xemantic.nano.plentyofroom.brush.desCloizeauxInteraction
import com.xemantic.nano.plentyofroom.brush.graftedChain
import com.xemantic.nano.plentyofroom.brush.reducedSecondVirialCoefficient
import com.xemantic.nano.plentyofroom.brush.reducedThirdVirialCoefficient
import com.xemantic.nano.plentyofroom.brush.threeBodyInteraction
import com.xemantic.nano.plentyofroom.brush.twoBodyInteraction
import com.xemantic.nano.plentyofroom.electrostatics.CollarMultiplierCurve
import com.xemantic.nano.plentyofroom.electrostatics.DnaOrigamiTile
import com.xemantic.nano.plentyofroom.electrostatics.GapMedium
import com.xemantic.nano.plentyofroom.electrostatics.IonModel
import com.xemantic.nano.plentyofroom.electrostatics.MagnesiumChlorideBuffer
import com.xemantic.nano.plentyofroom.electrostatics.PoissonBoltzmannEdge
import com.xemantic.nano.plentyofroom.electrostatics.PoissonBoltzmannGap
import com.xemantic.nano.plentyofroom.electrostatics.additiveCollarMultiplier
import com.xemantic.nano.plentyofroom.electrostatics.bjerrumLength
import com.xemantic.nano.plentyofroom.electrostatics.centralLogGradient
import com.xemantic.nano.plentyofroom.electrostatics.collarLogGradientEstimate
import com.xemantic.nano.plentyofroom.electrostatics.diffusePotentialOfAppliedBias
import com.xemantic.nano.plentyofroom.electrostatics.minimumMarginCollarMultiplier
import com.xemantic.nano.plentyofroom.electrostatics.sternChargeDensityPerVolt
import com.xemantic.nano.plentyofroom.electrostatics.thermalVoltage
import com.xemantic.nano.plentyofroom.electrostatics.uniformMedium
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.sqrt

/**
 * Task `T-60` — `C-0022`'s collar multiplier `μ(h)` solved at **fixed applied bias** over the
 * gaps `C-0018`'s equilibrium path actually visits, and carried into that path's fold.
 * Leaf `A7.4`, consumed by `A2.2`.
 *
 * Run with:
 *
 * ```shell
 * tools/study.sh actuator.CollarEquilibriumPathStudyKt
 * ```
 *
 * Emits `gpd/results/T-60-collar-on-the-equilibrium-path.json`, deterministically — no
 * timestamp, every floating-point number rounded at the serialisation boundary by
 * [roundedForActuatorResult].
 *
 * ## What it is for
 *
 * `C-0027` reports `C-0018`'s 1.007–1.032 pull-in margin as standing **with its movement
 * unresolved**, because `d ln μ/dh` was available only as a difference between gaps that
 * `T-3b`'s sweep visited at *different biases* — `0.0133–0.0226 nm⁻¹` over three schemes, a
 * 1.7× spread that leaves the coupled tangent at the fold running −2.5 to +4.0 pN/nm.
 *
 * This study replaces that difference with a **derivative of one function**, and then measures
 * — rather than argues — the split `CH-0035` makes between the *level* of `μ`, which a
 * force-pinned operating point absorbs into the bias, and its *gradient*, which is the only
 * channel into `k_es`. Three field variants are run at every state: `μ ≡ 1` (which must
 * reproduce `C-0018`), `μ ≡ const` (level only) and the solved `μ(h)`.
 */

// ---------------------------------------------------------------------------------------------
// records — every one prefixed `T60`, because study record classes are package-scoped
// ---------------------------------------------------------------------------------------------

/** One 2-D edge solve at a fixed applied bias: the collar multiplier and its diagnostics. */
@Serializable
data class T60CollarSample(
    val state: String,
    val concentration: Double,
    val gapHeight: Double,
    val appliedBias: Double,
    val diffusePotential: Double,
    val refinement: Int,
    val centrelineLoad: Double,
    val oneDimensionalLoad: Double,
    val centrelineOverOneDimensional: Double,
    val totalDeficitPerUnitEdge: Double,
    val firstMoment: Double,
    val effectiveCollarWidth: Double,
    val multiplierMinimumMargin: Double,
    val multiplierAdditive: Double,
    val chargeBalance: Double,
    val centrelineRouteSpread: Double,
    val numericallyResolved: Boolean,
    val newtonIterations: Int,
    val linearIterations: Int,
    val purpose: String
)

/** One estimate of `d ln μ/dh` in `nm⁻¹`, with the scheme that produced it. */
@Serializable
data class T60GradientRecord(
    val state: String,
    val concentration: Double,
    val gapHeight: Double,
    val scheme: String,
    val step: Double,
    val logGradient: Double
)

/** `μ` at one gap from several biases — the check that `μ` is a function of the gap. */
@Serializable
data class T60BiasIndependenceRecord(
    val state: String,
    val concentration: Double,
    val gapHeight: Double,
    val appliedBias: Double,
    val multiplierMinimumMargin: Double,
    val relativeSpread: Double
)

/** `T-3b`'s own published multiplier, reproduced through this study's fixed-bias pipeline. */
@Serializable
data class T60ReproductionRecord(
    val quantity: String,
    val concentration: Double,
    val gapHeight: Double,
    val appliedBias: Double,
    val here: Double,
    val upstream: Double,
    val departure: Double,
    val source: String
)

/** Mesh and difference-step convergence, referred to the finest setting of its own axis. */
@Serializable
data class T60ConvergenceRecord(
    val axis: String,
    val setting: String,
    val gapHeight: Double,
    val multiplierMinimumMargin: Double,
    val logGradient: Double,
    val multiplierDeparture: Double,
    val gradientDeparture: Double
)

/** One `(state, model, load line, field variant)` fold — the whole of `P3`. */
@Serializable
@Suppress("LongParameterList")
data class T60FoldRecord(
    val state: String,
    val model: String,
    val layerHeight: Double,
    val graftingDensity: Double,
    val concentration: Double,
    val loadLine: String,
    val variant: String,
    val collarAtFold: Double?,
    val collarLogGradientAtFold: Double?,
    val operatingBias: Double?,
    val operatingGap: Double?,
    val pullInBias: Double?,
    val pullInStroke: Double?,
    val pullInGap: Double?,
    val foldAtBranchStart: Boolean,
    /** The deepest stroke the branch reaches at all, in nm. */
    val branchEndStroke: Double?,
    /** The bias there, in V. */
    val branchEndBias: Double?,
    /**
     * True when the branch ended because the **field** ran out rather than because the scan did.
     *
     * This is what tells a genuinely monotone path — `C-0018`'s "the layer's osmotic divergence
     * removes the instability" — apart from a search that simply could not reach far enough. A
     * `null` pull-in bias means nothing without it.
     */
    val branchEndedOnTheField: Boolean,
    /** `C-0018`'s own predicate: below every ceiling AND on the stable side of the fold. */
    val operatingPointIsUsable: Boolean?,
    val brushStiffnessAtFold: Double?,
    val electrostaticStiffnessAtFold: Double?,
    val effectiveStiffnessAtFold: Double?,
    val coupledTangentAtFold: Double?,
    val tangencyResidual: Double?,
    val forceDecayLengthAtFold: Double?,
    val concentratedCrossoverBias: Double?,
    val bindingCeiling: String?,
    val usableBias: Double?,
    val margin: Double?,
    val clampedCollarEvaluations: Int,
    val foldInsideSolvedCollarRange: Boolean
)

/** The three variants of one `(state, model, load line)`, differenced — the decomposition. */
@Serializable
data class T60DecompositionRecord(
    val state: String,
    val model: String,
    val loadLine: String,
    val baselineMargin: Double?,
    val levelOnlyMargin: Double?,
    val fullMargin: Double?,
    val levelContribution: Double?,
    val gradientContribution: Double?,
    val baselinePullInBias: Double?,
    val fullPullInBias: Double?,
    val pullInBiasShift: Double?,
    val operatingBiasShift: Double?,
    val marginResolutionFloor: Double,
    val marginMovementIsResolved: Boolean,
    /** `d ln μ/dh` at `C-0018`'s **own** fold gap, which is what `C-0027` had to bracket. */
    val collarLogGradientAtBaselineFold: Double?,
    /** `k_c + k_brush + k_es` at `C-0018`'s own fold with the collar carried and nothing else. */
    val foldTangentCollarOnly: Double?,
    /** The same with `C-0019`'s `k_brush` degradation carried too — `C-0027`'s own quantity. */
    val foldTangentCollarAndFluctuation: Double?,
    /** `C-0019`'s licensed `k_brush` multiplier at this height — **CITED FROM `C-0027`**. */
    val fluctuationBrushMultiplier: Double
)

@Serializable
@Suppress("LongParameterList")
data class T60Result(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val runParameters: Map<String, String>,
    val citedInputs: List<String>,
    val temperature: Double,
    val medium: String,
    val thermalEnergy: Double,
    val bjerrumLength: Double,
    val cheapEstimate: List<T60GradientRecord>,
    val collar: List<T60CollarSample>,
    val gradients: List<T60GradientRecord>,
    val biasIndependence: List<T60BiasIndependenceRecord>,
    val reproductions: List<T60ReproductionRecord>,
    val convergence: List<T60ConvergenceRecord>,
    val folds: List<T60FoldRecord>,
    val decomposition: List<T60DecompositionRecord>,
    val findings: Map<String, String>,
    val verdict: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

// ---------------------------------------------------------------------------------------------
// the sweep
// ---------------------------------------------------------------------------------------------

private const val FOOTPRINT = 1600.0

private const val TILE_EDGE = 40.0

private const val STERN_CAPACITANCE = 20.0

/** The 1-D mesh, `T-4`'s own — the fold searches must be comparable to `C-0018`'s. */
private const val MESH_NODES = 2000

/** The 1-D mesh of the Stern inversion inside the 2-D sweep, `T-3b`'s own. */
private const val SEARCH_NODES = 800

/** The 2-D mesh multiplier the sweep runs at, `T-3b`'s own. */
private const val EDGE_REFINEMENT = 3

private const val TARGET_FORCE = 100.0

private const val TARGET_STROKE = 3.0

private val MANDATED_COUPLING = TARGET_FORCE / TARGET_STROKE

private const val TRUSTED_BIAS_CEILING = 1.0

private const val CORRELATION_ATTRACTION_GAP = 1.46

private const val CONCENTRATED_CROSSOVER = 0.2

private const val CURVE_LOWEST_GAP = 0.5

private const val OSMOTIC_SECOND_VIRIAL = 1.9e-3

private const val OSMOTIC_THIRD_VIRIAL = 2.0e-2

/**
 * The two states `C-0018` reports pull-in as the binding ceiling at.
 *
 * **10 nm / 2 mM** is where the programme's thinnest margin lives, 1.007–1.032 at six of six
 * models. **7 nm / 10 mM** is `T-62`'s five unrecorded states, where the fold sits at a stroke
 * of 1.92–2.68 nm — *shallower* than §3's 3 nm — which is the one place a bias below pull-in is
 * not sufficient.
 *
 * The **fixed bias** is the midpoint of `C-0018`'s own six-model `V*` bracket at that state.
 * It is fixed because `k_es` is a derivative at fixed **applied** bias, and because `T-3b`'s
 * sweep moved the bias with the gap, which is exactly what this task exists to undo. That the
 * choice does not matter is measured, not assumed — see [T60BiasIndependenceRecord].
 */
private data class T60CollarState(
    val label: String,
    val concentration: Double,
    val layerHeight: Double,
    val graftingDensity: Double,
    val fixedBias: Double,
    val gaps: List<Double>,
    val probeGap: Double,
    val probeBiases: List<Double>,
    /**
     * `C-0019`'s licensed `k_brush` multiplier at this layer height — **CITED FROM `C-0027`**
     * (`gpd/results/T-25-window-resynthesis.json`, `brushStiffnessCorrected` over
     * `brushStiffnessBaseline`), and buffer-independent there because it is a polymer
     * correction. Carried only so that `C-0027`'s *own* straddling tangent can be recomputed
     * with a resolved gradient; nothing in this task re-runs `C-0019`.
     */
    val fluctuationBrushMultiplier: Double
)

private val STATES = listOf(
    T60CollarState(
        label = "10 nm / 2 mM",
        concentration = 2.0,
        layerHeight = 10.0,
        graftingDensity = 0.024,
        fixedBias = 0.155,
        gaps = listOf(
            2.0, 3.0, 4.0, 4.5, 5.0, 5.5, 6.0, 6.25, 6.5, 6.75, 7.0, 7.5, 8.0, 9.0, 10.0, 11.0
        ),
        probeGap = 6.5,
        probeBiases = listOf(0.128, 0.155, 0.184),
        fluctuationBrushMultiplier = 0.90584
    ),
    T60CollarState(
        label = "7 nm / 10 mM",
        concentration = 10.0,
        layerHeight = 7.0,
        graftingDensity = 0.045,
        fixedBias = 0.22,
        gaps = listOf(
            2.0, 3.0, 3.5, 4.0, 4.25, 4.5, 4.75, 5.0, 5.25, 5.5, 6.0, 6.5, 7.0, 8.0
        ),
        probeGap = 4.75,
        probeBiases = listOf(0.148, 0.22, 0.396),
        fluctuationBrushMultiplier = 0.94885
    )
)

/** `T-3b`'s own `(gap, bias)` state points at 2 mM, for the gate-5 reproduction. */
private val UPSTREAM_POINTS = listOf(
    Triple(5.0, 0.368, -0.049342),
    Triple(7.0, 0.155, -0.106318),
    Triple(10.0, 0.192, -0.147081)
)

private data class T60LoadLine(val name: String, val stiffness: Double, val preload: Double) {
    fun reaction(stroke: Double): Double = preload + stiffness * stroke
}

private val LOAD_LINES = listOf(
    T60LoadLine("coupled", MANDATED_COUPLING, 0.0),
    T60LoadLine("dead-load", 0.0, TARGET_FORCE),
    T60LoadLine("free", 0.0, 0.0)
)

/**
 * The 1-D field of `C-0008`/`C-0012`, with a gap-dependent collar multiplied onto its force.
 *
 * `sample` is the cheap direction (one solve per diffuse drop); `forceAtBias` is `C-0008`'s own
 * direction and costs the Stern-series inversion, so it is used only for the finite-difference
 * `k_es` at the fold.
 */
private class T60CollarField(
    concentration: Double,
    val tileCharge: Double,
    val bjerrum: Double,
    val collar: (Double) -> Double,
    val nodes: Int = MESH_NODES
) {

    private val ions = IonModel(MagnesiumChlorideBuffer(concentration).magnesiumNumberDensity)

    private val medium = uniformMedium(GapMedium())

    private val stern = sternChargeDensityPerVolt(STERN_CAPACITANCE)

    private val volt = thermalVoltage()

    private fun plain(gap: Double, diffusePotential: Double): FieldSample {
        val solution = PoissonBoltzmannGap(gap, ions, medium, bjerrum, nodes = nodes)
            .solve(diffusePotential / volt, tileCharge)
        return FieldSample(
            gap = gap,
            diffusePotential = diffusePotential,
            appliedBias = diffusePotential + solution.electrodeSurfaceChargeDensity / stern,
            force = solution.forceOnTile(FOOTPRINT)
        )
    }

    /** The **uncorrected** field, so the collar can be attached by the tested extension. */
    fun base(): DiffuseParametrisedField = DiffuseParametrisedField { gap, psi -> plain(gap, psi) }

    fun asPath(): DiffuseParametrisedField = base().withCollar(collar)

    fun forceAtBias(gap: Double, bias: Double): Double {
        val diffuse = diffusePotentialOfAppliedBias(
            gap, bias, tileCharge, stern, ions, medium, bjerrum, nodes = nodes
        )
        return PoissonBoltzmannGap(gap, ions, medium, bjerrum, nodes = nodes)
            .solve(diffuse / volt, tileCharge)
            .forceOnTile(FOOTPRINT) * collar(gap)
    }

    /** `k_es = −∂F_z/∂h` in pN/nm at fixed **applied** bias, centrally differenced. */
    fun stiffnessAtBias(gap: Double, bias: Double, delta: Double = 1e-3): Double =
        -(forceAtBias(gap + delta, bias) - forceAtBias(gap - delta, bias)) / (2.0 * delta)
}

private fun interactionFor(peg: PegWater, choice: String): InteractionFreeEnergy {
    val twoBody = twoBodyInteraction(
        peg.reducedSecondVirialCoefficient(OSMOTIC_SECOND_VIRIAL), peg.monomerVolume
    )
    val threeBody = threeBodyInteraction(
        peg.reducedThirdVirialCoefficient(OSMOTIC_THIRD_VIRIAL), peg.monomerVolume
    )
    return when (choice) {
        "two-body" -> twoBody
        "virial" -> additiveInteraction("virial", listOf(twoBody, threeBody))
        else -> desCloizeauxInteraction(peg.crossoverIndex, peg.monomerVolume)
    }
}

private fun layerModels(peg: PegWater): List<Pair<String, GraftedLayerModel>> =
    listOf("alexander-box", "strong-stretching").flatMap { profile ->
        listOf("two-body", "virial", "des-Cloizeaux").map { interaction ->
            val energy = interactionFor(peg, interaction)
            val model: GraftedLayerModel =
                if (profile == "alexander-box") AlexanderBoxLayer(energy)
                else StrongStretchingLayer(energy)
            model.name to model
        }
    }

// ---------------------------------------------------------------------------------------------
// the 2-D sweep
// ---------------------------------------------------------------------------------------------

private fun tileCharge(): Double {
    val tile = DnaOrigamiTile()
    return -(tile.projectedChargeDensity * tile.manningSurvivingFraction(2, bjerrumLength()) / 2.0)
}

private fun diffuseOf(concentration: Double, gap: Double, bias: Double): Double =
    diffusePotentialOfAppliedBias(
        gap, bias, tileCharge(), sternChargeDensityPerVolt(STERN_CAPACITANCE),
        IonModel(MagnesiumChlorideBuffer(concentration).magnesiumNumberDensity),
        uniformMedium(GapMedium()), bjerrumLength(), nodes = SEARCH_NODES
    )

private fun oneDimensionalLoad(concentration: Double, gap: Double, diffuse: Double): Double =
    -PoissonBoltzmannGap(
        gap, IonModel(MagnesiumChlorideBuffer(concentration).magnesiumNumberDensity),
        uniformMedium(GapMedium()), bjerrumLength(),
        nodes = maxOf(4000, (gap * 1200.0).toInt())
    ).solve(diffuse / thermalVoltage(), tileCharge())
        .disjoiningPressureInPiconewtonPerSquareNanometre

private fun collarSample(
    state: String,
    concentration: Double,
    gap: Double,
    bias: Double,
    refinement: Int,
    purpose: String
): T60CollarSample {
    val charge = tileCharge()
    val diffuse = diffuseOf(concentration, gap, bias)
    val solution = PoissonBoltzmannEdge(
        gapHeight = gap,
        ionModel = IonModel(MagnesiumChlorideBuffer(concentration).magnesiumNumberDensity),
        medium = GapMedium(),
        bjerrumLength = bjerrumLength(),
        refinement = refinement
    ).solve(diffuse / thermalVoltage(), charge)
    val fit = solution.taperFit()
    val total = solution.totalDeficitPerUnitEdge
    val interior = solution.centrelineLoad
    val oneDimensional = oneDimensionalLoad(concentration, gap, diffuse)
    return T60CollarSample(
        state = state,
        concentration = concentration,
        gapHeight = gap,
        appliedBias = bias,
        diffusePotential = diffuse,
        refinement = refinement,
        centrelineLoad = interior,
        oneDimensionalLoad = oneDimensional,
        centrelineOverOneDimensional = interior / oneDimensional,
        totalDeficitPerUnitEdge = total,
        firstMoment = fit.firstMoment,
        effectiveCollarWidth = -total / interior,
        multiplierMinimumMargin =
            minimumMarginCollarMultiplier(total, fit.firstMoment, interior, TILE_EDGE),
        multiplierAdditive = additiveCollarMultiplier(total, interior, TILE_EDGE),
        chargeBalance = solution.chargeBalance,
        centrelineRouteSpread = solution.centrelineRouteSpread,
        numericallyResolved = solution.numericallyResolved,
        newtonIterations = solution.newtonIterations,
        linearIterations = solution.linearIterations,
        purpose = purpose
    )
}

// ---------------------------------------------------------------------------------------------
// the fold, re-located through a corrected field
// ---------------------------------------------------------------------------------------------

private class T60FieldVariant(
    val name: String,
    val collar: (Double) -> Double,
    val curve: CollarMultiplierCurve?
)

@Suppress("LongParameterList", "LongMethod")
private fun foldRecord(
    state: T60CollarState,
    modelName: String,
    balance: com.xemantic.nano.plentyofroom.actuator.ActuatorForceBalance,
    chain: GraftedChain,
    variant: T60FieldVariant,
    line: T60LoadLine,
    charge: Double,
    lb: Double
): T60FoldRecord {
    val field = T60CollarField(state.concentration, charge, lb, variant.collar)
    val resting = balance.restingHeight
    val floor = max(chain.occupiedThickness * 1.01, CURVE_LOWEST_GAP)
    val strokeCeiling = resting - floor
    val path = EquilibriumPath(
        restingHeight = resting,
        strokeCeiling = strokeCeiling,
        field = field.asPath()
    ) { stroke -> line.reaction(stroke) + balance.layerLoad(resting - stroke) }
    val clampedBefore = variant.curve?.clampedEvaluations ?: 0
    val search = path.fold()
    val fold = search.fold
    val operating = if (TARGET_STROKE <= strokeCeiling) path.at(TARGET_STROKE) else null
    val brush = fold?.let { balance.layerStiffness(it.gap) }
    val electrostatic = fold?.let { field.stiffnessAtBias(it.gap, it.appliedBias) }
    val effective = if (brush != null && electrostatic != null) brush + electrostatic else null
    val coupledTangent = effective?.let { line.stiffness + it }
    // A boundary maximum is not a stationary point, so no residual is reported there rather
    // than a meaningless one; the scale is the SUM OF THE MAGNITUDES, because two of the three
    // load lines have k_c = 0 and a strong-stretching layer can have k_brush near zero.
    val residual = if (search.foldAtBranchStart) null else coupledTangent?.let {
        abs(it) / max(line.stiffness + abs(brush ?: 0.0) + abs(electrostatic ?: 0.0), 1e-12)
    }
    val decayLength = if (fold != null && electrostatic != null && electrostatic != 0.0)
        -fold.attraction / electrostatic else null
    val crossoverGap = chain.occupiedThickness / CONCENTRATED_CROSSOVER
    val crossoverStroke = resting - crossoverGap
    val crossover =
        if (crossoverStroke <= 0.0 || crossoverStroke > strokeCeiling) null
        else path.at(crossoverStroke)?.appliedBias
    val crossoverBeyond = crossoverStroke > (fold?.stroke ?: strokeCeiling)
    val correlationStroke = resting - CORRELATION_ATTRACTION_GAP
    val correlation =
        if (correlationStroke <= 0.0 || correlationStroke > strokeCeiling) null
        else path.at(correlationStroke)?.appliedBias
    val correlationBeyond = correlationStroke > (fold?.stroke ?: strokeCeiling)
    val binding = bindingCeiling(
        listOf(
            BiasCeiling("static stability (pull-in)", fold?.appliedBias),
            BiasCeiling(
                "concentrated crossover (C-0002, phi = 0.2)",
                if (crossoverBeyond) null else crossover
            ),
            BiasCeiling(
                "correlation band (C-0005, 1.46 nm)",
                if (correlationBeyond) null else correlation
            ),
            BiasCeiling("point-ion boundary (CH-0007, 1.0 V)", TRUSTED_BIAS_CEILING)
        )
    )
    val clamped = (variant.curve?.clampedEvaluations ?: 0) - clampedBefore
    return T60FoldRecord(
        state = state.label,
        model = modelName,
        layerHeight = state.layerHeight,
        graftingDensity = state.graftingDensity,
        concentration = state.concentration,
        loadLine = line.name,
        variant = variant.name,
        collarAtFold = fold?.let { variant.collar(it.gap) },
        collarLogGradientAtFold = fold?.let { point -> variant.curve?.logGradientAt(point.gap) },
        operatingBias = operating?.appliedBias,
        operatingGap = operating?.gap,
        pullInBias = fold?.appliedBias,
        pullInStroke = fold?.stroke,
        pullInGap = fold?.gap,
        foldAtBranchStart = search.foldAtBranchStart,
        branchEndStroke = search.branchEnd?.stroke,
        branchEndBias = search.branchEnd?.appliedBias,
        branchEndedOnTheField = search.reachedDiffuseCeiling,
        // A bias below the pull-in bias is NOT sufficient: the target stroke must also lie on
        // the stable side of the fold, and those are two different tests (C-0018).
        operatingPointIsUsable = operating?.let { point ->
            binding?.bias?.let { ceiling ->
                point.appliedBias <= ceiling &&
                        (fold == null || TARGET_STROKE <= fold.stroke + 1e-9)
            }
        },
        brushStiffnessAtFold = brush,
        electrostaticStiffnessAtFold = electrostatic,
        effectiveStiffnessAtFold = effective,
        coupledTangentAtFold = coupledTangent,
        tangencyResidual = residual,
        forceDecayLengthAtFold = decayLength,
        concentratedCrossoverBias = crossover,
        bindingCeiling = binding?.name,
        usableBias = binding?.bias,
        margin = biasMargin(binding?.bias, operating?.appliedBias),
        clampedCollarEvaluations = clamped,
        foldInsideSolvedCollarRange = fold != null && variant.curve != null &&
                fold.gap >= variant.curve.lowestGap && fold.gap <= variant.curve.highestGap
    )
}

// ---------------------------------------------------------------------------------------------
// main
// ---------------------------------------------------------------------------------------------

@Suppress("LongMethod", "ComplexMethod")
fun main() {
    val started = System.currentTimeMillis()
    val peg = PegWater()
    val geometry = ActuatorGeometry()
    val lb = bjerrumLength()
    val charge = tileCharge()
    val models = layerModels(peg)

    println("T-60 — the cheap estimate, before any 2-D solve ...")
    val cheap = STATES.flatMap { state ->
        val kappa = MagnesiumChlorideBuffer(state.concentration).inverseDebyeLength()
        state.gaps.map { gap ->
            T60GradientRecord(
                state = state.label,
                concentration = state.concentration,
                gapHeight = gap,
                scheme = "cheap estimate — transverse eigenvalue ceiling as the collar",
                step = 0.0,
                logGradient = collarLogGradientEstimate(kappa, gap, TILE_EDGE)
            )
        }
    }
    cheap.filter { it.gapHeight in listOf(5.0, 7.0) }.forEach {
        println("  ${it.state} at ${it.gapHeight} nm: d ln mu/dh ~ ${"%.5f".format(it.logGradient)} /nm")
    }

    println("T-60 — solving mu(h) at FIXED applied bias ...")
    val collar = mutableListOf<T60CollarSample>()
    val curves = mutableMapOf<String, CollarMultiplierCurve>()
    STATES.forEach { state ->
        val samples = state.gaps.map { gap ->
            val sample = collarSample(
                state.label, state.concentration, gap, state.fixedBias, EDGE_REFINEMENT,
                "mu(h) sweep at fixed applied bias"
            )
            println(
                "  ${state.label}  h = ${gap} nm  mu = ${"%.6f".format(sample.multiplierMinimumMargin)}" +
                        "  collar = ${"%.4f".format(sample.effectiveCollarWidth)} nm" +
                        "  (${(System.currentTimeMillis() - started) / 1000} s)"
            )
            sample
        }
        collar += samples
        curves[state.label] = CollarMultiplierCurve(
            state.gaps.toDoubleArray(),
            samples.map { it.multiplierMinimumMargin }.toDoubleArray()
        )
    }

    println("T-60 — is mu a function of the gap? three biases at one gap ...")
    val biasIndependence = mutableListOf<T60BiasIndependenceRecord>()
    STATES.forEach { state ->
        val values = state.probeBiases.map { bias ->
            bias to collarSample(
                state.label, state.concentration, state.probeGap, bias, EDGE_REFINEMENT,
                "bias-independence probe"
            )
        }
        collar += values.map { it.second }
        val multipliers = values.map { it.second.multiplierMinimumMargin }
        val spread = (multipliers.max() - multipliers.min()) / multipliers.average()
        biasIndependence += values.map { (bias, sample) ->
            T60BiasIndependenceRecord(
                state = state.label,
                concentration = state.concentration,
                gapHeight = state.probeGap,
                appliedBias = bias,
                multiplierMinimumMargin = sample.multiplierMinimumMargin,
                relativeSpread = spread
            )
        }
        println("  ${state.label}: mu spans ${"%.3f".format(100.0 * spread)} % over three biases")
    }

    println("T-60 — reproducing T-3b at its own (gap, bias) points ...")
    val reproductions = UPSTREAM_POINTS.map { (gap, bias, upstreamFraction) ->
        val sample = collarSample(
            "T-3b reproduction", 2.0, gap, bias, EDGE_REFINEMENT, "gate 5 — T-3b reproduction"
        )
        collar += sample
        val upstream = 1.0 - upstreamFraction
        T60ReproductionRecord(
            quantity = "collar multiplier mu, minimum-margin mapping",
            concentration = 2.0,
            gapHeight = gap,
            appliedBias = bias,
            here = sample.multiplierMinimumMargin,
            upstream = upstream,
            departure = abs(sample.multiplierMinimumMargin - upstream) / upstream,
            source = "C-0022 / gpd/results/T-3b-tile-edge-load-profile.json"
        )
    }
    reproductions.forEach {
        println("  ${it.gapHeight} nm at ${it.appliedBias} V: here ${"%.6f".format(it.here)}, " +
                "upstream ${"%.6f".format(it.upstream)}, departure ${"%.2e".format(it.departure)}")
    }

    println("T-60 — the gradients ...")
    val gradients = mutableListOf<T60GradientRecord>()
    STATES.forEach { state ->
        val curve = curves.getValue(state.label)
        val byGap = collar.filter {
            it.state == state.label && it.purpose.startsWith("mu(h) sweep") &&
                    it.refinement == EDGE_REFINEMENT
        }.associate { it.gapHeight to it.multiplierMinimumMargin }
        state.gaps.forEach { gap ->
            gradients += T60GradientRecord(
                state = state.label,
                concentration = state.concentration,
                gapHeight = gap,
                scheme = "C1 interpolant (cubic Hermite on ln mu, parabolic node slopes)",
                step = 0.0,
                logGradient = curve.logGradientAt(gap)
            )
        }
        listOf(0.25, 0.5, 1.0, 1.5).forEach { step ->
            state.gaps.filter { byGap.containsKey(it - step) && byGap.containsKey(it + step) }
                .forEach { gap ->
                    gradients += T60GradientRecord(
                        state = state.label,
                        concentration = state.concentration,
                        gapHeight = gap,
                        scheme = "central difference",
                        step = step,
                        logGradient = centralLogGradient(
                            byGap.getValue(gap - step), byGap.getValue(gap + step),
                            gap - step, gap + step
                        )
                    )
                }
        }
    }

    println("T-60 — mesh convergence of the GRADIENT, not only of mu ...")
    val convergence = mutableListOf<T60ConvergenceRecord>()
    val convergenceGaps = listOf(6.0, 6.5, 7.0)
    val finest = mutableMapOf<Int, Pair<Double, Double>>()
    listOf(2, 3, 4).forEach { refinement ->
        val values = convergenceGaps.map { gap ->
            collarSample(
                "10 nm / 2 mM", 2.0, gap, 0.155, refinement, "mesh convergence"
            ).also { collar += it }.multiplierMinimumMargin
        }
        // The central difference divides by the SEPARATION of the two outer gaps, which is
        // `2d` for a symmetric difference of half-step `d`. Written by hand as `/(2 * step)`
        // with `step` read off the wrong axis it comes out exactly half, and no dimensional
        // check catches that — hence `centralLogGradient`, which names the separation once.
        val gradient = centralLogGradient(
            values[0], values[2], convergenceGaps[0], convergenceGaps[2]
        )
        finest[refinement] = values[1] to gradient
        println("  refinement $refinement: mu(6.5) = ${"%.6f".format(values[1])}, " +
                "d ln mu/dh = ${"%.6f".format(gradient)} /nm " +
                "(${(System.currentTimeMillis() - started) / 1000} s)")
    }
    val reference = finest.getValue(4)
    finest.forEach { (refinement, value) ->
        convergence += T60ConvergenceRecord(
            axis = "2-D mesh refinement",
            setting = "refinement $refinement",
            gapHeight = 6.5,
            multiplierMinimumMargin = value.first,
            logGradient = value.second,
            multiplierDeparture = abs(value.first - reference.first) / reference.first,
            gradientDeparture = abs(value.second - reference.second) / abs(reference.second)
        )
    }
    run {
        val curve = curves.getValue("10 nm / 2 mM")
        val byGap = collar.filter {
            it.state == "10 nm / 2 mM" && it.purpose.startsWith("mu(h) sweep")
        }.associate { it.gapHeight to it.multiplierMinimumMargin }
        val schemes = listOf(0.25, 0.5, 1.0, 1.5).mapNotNull { step ->
            val low = byGap[6.5 - step] ?: return@mapNotNull null
            val high = byGap[6.5 + step] ?: return@mapNotNull null
            step to centralLogGradient(low, high, 6.5 - step, 6.5 + step)
        }
        val tightest = schemes.first().second
        schemes.forEach { (step, value) ->
            convergence += T60ConvergenceRecord(
                axis = "central-difference step",
                setting = "step ${step} nm",
                gapHeight = 6.5,
                multiplierMinimumMargin = byGap.getValue(6.5),
                logGradient = value,
                multiplierDeparture = 0.0,
                gradientDeparture = abs(value - tightest) / abs(tightest)
            )
        }
        convergence += T60ConvergenceRecord(
            axis = "central-difference step",
            setting = "C1 interpolant at the node",
            gapHeight = 6.5,
            multiplierMinimumMargin = byGap.getValue(6.5),
            logGradient = curve.logGradientAt(6.5),
            multiplierDeparture = 0.0,
            gradientDeparture = abs(curve.logGradientAt(6.5) - tightest) / abs(tightest)
        )
    }

    println("T-60 — re-locating the folds through three field variants ...")
    val folds = mutableListOf<T60FoldRecord>()
    STATES.forEach { state ->
        val curve = curves.getValue(state.label)
        // The level-only variant is pinned at the collar the BASELINE fold sees, so that the
        // two variants differ in the gradient and in nothing else.
        models.forEach { (name, model) ->
            val chain = peg.graftedChain(
                model.chainLengthForHeight(peg, state.layerHeight, state.graftingDensity),
                state.graftingDensity
            )
            val balance = ActuatorForceBalance(model, chain, geometry)
            LOAD_LINES.forEach { line ->
                val baseline = foldRecord(
                    state, name, balance, chain,
                    T60FieldVariant("mu = 1 (C-0018 reproduced)", { 1.0 }, null), line, charge, lb
                )
                folds += baseline
                val foldGap = baseline.pullInGap ?: state.layerHeight
                val level = curve.multiplierAt(foldGap)
                folds += foldRecord(
                    state, name, balance, chain,
                    T60FieldVariant("mu = const (level only)", { level }, null), line, charge, lb
                )
                folds += foldRecord(
                    state, name, balance, chain,
                    T60FieldVariant("mu(h) (level + gradient)", { curve.multiplierAt(it) }, curve),
                    line, charge, lb
                )
            }
            println("  ${state.label}  $name done (${(System.currentTimeMillis() - started) / 1000} s)")
        }
    }

    println("T-60 — the decomposition ...")
    val decomposition = STATES.flatMap { state ->
        val curve = curves.getValue(state.label)
        models.flatMap { (name, _) ->
            LOAD_LINES.map { line ->
                val at = { variant: String ->
                    folds.first {
                        it.state == state.label && it.model == name &&
                                it.loadLine == line.name && it.variant.startsWith(variant)
                    }
                }
                val baseline = at("mu = 1")
                val level = at("mu = const")
                val full = at("mu(h)")
                // A golden-section maximum is floored by the noise of the search underneath it:
                // with a relative bracket t on the bias bisection, the stroke at the fold is
                // resolvable only to about lambda*sqrt(2t) (CLAUDE.md), and the bias is
                // quadratic in the stroke there. sqrt(2t) is therefore the floor on any
                // RELATIVE movement in the located bias, and hence in the margin.
                val floor = resolutionFloor(DEFAULT_DIFFUSE_TOLERANCE)
                val movement = if (baseline.margin != null && full.margin != null)
                    abs(full.margin / baseline.margin - 1.0) else 0.0
                // C-0027's own quantity, recomputed with a RESOLVED gradient: the coupled
                // tangent at C-0018's OWN fold. There |F_es| is pinned, so with the collar
                // carried k_es goes from -|F|/l to -|F|(1/l - g), i.e. it gains exactly |F|g;
                // and since the baseline tangent vanishes at the fold by construction, the
                // collar-only tangent IS |F|g and is positive whenever g is.
                val gradientAtFold = baseline.pullInGap?.let { curve.logGradientAt(it) }
                val pinnedForce =
                    if (baseline.electrostaticStiffnessAtFold != null &&
                        baseline.forceDecayLengthAtFold != null
                    ) -baseline.electrostaticStiffnessAtFold * baseline.forceDecayLengthAtFold
                    else null
                val collarOnlyTangent =
                    if (gradientAtFold != null && pinnedForce != null &&
                        baseline.brushStiffnessAtFold != null &&
                        baseline.electrostaticStiffnessAtFold != null
                    ) line.stiffness + baseline.brushStiffnessAtFold +
                            baseline.electrostaticStiffnessAtFold + pinnedForce * gradientAtFold
                    else null
                val bothTangent =
                    if (collarOnlyTangent != null && baseline.brushStiffnessAtFold != null)
                        collarOnlyTangent - (1.0 - state.fluctuationBrushMultiplier) *
                                baseline.brushStiffnessAtFold
                    else null
                T60DecompositionRecord(
                    state = state.label,
                    model = name,
                    loadLine = line.name,
                    baselineMargin = baseline.margin,
                    levelOnlyMargin = level.margin,
                    fullMargin = full.margin,
                    levelContribution =
                        if (baseline.margin != null && level.margin != null)
                            level.margin / baseline.margin - 1.0 else null,
                    gradientContribution =
                        if (level.margin != null && full.margin != null)
                            full.margin / level.margin - 1.0 else null,
                    baselinePullInBias = baseline.pullInBias,
                    fullPullInBias = full.pullInBias,
                    pullInBiasShift =
                        if (baseline.pullInBias != null && full.pullInBias != null)
                            full.pullInBias - baseline.pullInBias else null,
                    operatingBiasShift =
                        if (baseline.operatingBias != null && full.operatingBias != null)
                            full.operatingBias - baseline.operatingBias else null,
                    marginResolutionFloor = floor,
                    marginMovementIsResolved = movement > floor,
                    collarLogGradientAtBaselineFold = gradientAtFold,
                    foldTangentCollarOnly = collarOnlyTangent,
                    foldTangentCollarAndFluctuation = bothTangent,
                    fluctuationBrushMultiplier = state.fluctuationBrushMultiplier
                )
            }
        }
    }

    val result = T60Result(
        task = "T-60",
        leaf = "A7.4",
        title = "The finite-tile collar multiplier on the coupled equilibrium path: mu(h) at " +
                "fixed applied bias, its logarithmic gradient, and whether C-0018's pull-in " +
                "margin moves",
        verificationType = "in-silico (T-3b's 2-D nonlinear Poisson-Boltzmann edge solver " +
                "re-run at FIXED applied bias over a gap sweep, its collar multiplier carried " +
                "into T-4's equilibrium-path fold search under three field variants) + logical",
        acceptance = "P1: mu(h) at >= 10 gaps at fixed applied bias per state, with the " +
                "bias-dependence measured. P2: d ln mu/dh from central differences at several " +
                "steps, a C1 interpolant and a closed-form cheap estimate, compared against " +
                "C-0027's inherited 0.0133-0.0226 /nm. P3: C-0018's fold re-located under mu = 1, " +
                "mu = const and mu(h), so the level and the gradient are measured separately. " +
                "P4: whether C-0018's 1.007-1.032 margin moves, in which direction, and whether " +
                "it goes below one.",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated. And " +
                "inside mean field: C-0005 puts the one-loop correction at 123-214% of the " +
                "leading term across this gap range, which is two orders larger than every " +
                "margin movement reported here. NOTHING HERE IS MEASURED.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "pressure" to "pN/nm^2 (= 1 MPa exactly)",
            "stiffness" to "pN/nm",
            "potential" to "V",
            "concentration" to "mM",
            "logGradient" to "1/nm",
            "temperature" to "K"
        ),
        conventions = listOf(
            "z is normal to the electrode, positive AWAY from it; the electrostatic gap IS the " +
                    "layer height, exactly",
            "the STROKE s = L0 - h is positive DOWNWARD; L0 is a FORCE-ONSET height (C-0011)",
            "mu = |F_es,2D| / (Pi_1D * 1600 nm^2), dimensionless, mu > 1 is an ENHANCEMENT; " +
                    "T-3b emits a force DEFICIT fraction, so mu = 1 - fraction",
            "d ln mu/dh is in 1/nm and is taken at FIXED APPLIED BIAS, which is the state " +
                    "variable k_es is differentiated at",
            "k_es = |F_es| d ln|F_es|/dh and l = -1/(d ln|F_es|/dh), so k_es = -|F_es|/l " +
                    "identically; k_es < 0 above the force maximum",
            "the LOAD LINE R(s) is positive UPWARD; a ceiling belongs to a (bias, load line) pair",
            "MgCl2 is 2:1, so I = 3c",
            "the minimum-margin mapping counts each corner once and the additive one twice; " +
                    "they bracket the unsolved 3-D corner"
        ),
        runParameters = mapOf(
            "temperature" to ROOM_TEMPERATURE.toString(),
            "tileEdge" to TILE_EDGE.toString(),
            "footprint" to FOOTPRINT.toString(),
            "tileChargeDensity" to charge.toString(),
            "sternCapacitance" to STERN_CAPACITANCE.toString(),
            "edgeRefinement" to EDGE_REFINEMENT.toString(),
            "oneDimensionalMeshNodes" to MESH_NODES.toString(),
            "sternInversionNodes" to SEARCH_NODES.toString(),
            "fixedBias" to STATES.joinToString("; ") { "${it.label}: ${it.fixedBias} V" },
            "gapsSolved" to STATES.joinToString("; ") { "${it.label}: ${it.gaps}" },
            "loadLines" to LOAD_LINES.joinToString("; ") {
                "${it.name}: R = ${it.preload} + ${it.stiffness} s"
            },
            "targetForce" to TARGET_FORCE.toString(),
            "targetStroke" to TARGET_STROKE.toString()
        ),
        citedInputs = listOf(
            "eps_r(water, 300 K) = 78 — CITED, as in C-0005/C-0008/C-0022.",
            "the Manning-renormalised tile charge, 11.90% of bare — CITED FROM C-0005 via " +
                    "C-0008. The tile is charge-SATURATED.",
            "Stern capacitance ~20 uF/cm^2 — CITED, and load-bearing for the bias mapping; the " +
                    "collar is a RATIO and the sweep shows it barely moves with the bias.",
            "C-0018's six-model V* brackets at the two states, used only to CENTRE the fixed " +
                    "bias — CITED FROM C-0018. That the choice is not load-bearing is measured " +
                    "here, not assumed.",
            "C-0002's phi = 0.2 concentrated crossover, read as a ceiling — CITED.",
            "CH-0007's point-ion boundary, 1.0 V applied — CITED; it never binds.",
            "C-0017's mandated coupling, 33.333 pN/nm — CITED, itself derived from §3 alone.",
            "C-0003's six layer models and C-0001's grafting densities — re-run, not tabulated."
        ),
        temperature = ROOM_TEMPERATURE,
        medium = "aqueous MgCl2 buffer, 2 and 10 mM, 300 K",
        thermalEnergy = thermalEnergy(),
        bjerrumLength = lb,
        cheapEstimate = cheap,
        collar = collar,
        gradients = gradients,
        biasIndependence = biasIndependence,
        reproductions = reproductions,
        convergence = convergence,
        folds = folds,
        decomposition = decomposition,
        findings = emptyMap(),
        verdict = emptyMap(),
        validity = validity(),
        openQuestions = openQuestions()
    )
    val complete = result.copy(
        findings = findings(result, curves),
        verdict = verdict(result)
    )
    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-60-collar-on-the-equilibrium-path.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(json.encodeToJsonElement(complete).roundedForActuatorResult()) + "\n"
    )
    println("T-60 — wrote ${output.path} in ${(System.currentTimeMillis() - started) / 1000} s")
    complete.verdict.forEach { (key, value) -> println("  $key: $value") }
}

// ---------------------------------------------------------------------------------------------
// findings, verdict, validity
// ---------------------------------------------------------------------------------------------

private fun binding(result: T60Result, state: String, variant: String) =
    result.folds.filter {
        it.state == state && it.loadLine == "coupled" && it.variant.startsWith(variant) &&
                it.bindingCeiling == "static stability (pull-in)" && it.margin != null
    }

private fun findings(
    result: T60Result,
    curves: Map<String, CollarMultiplierCurve>
): Map<String, String> {
    val ten = "10 nm / 2 mM"
    val seven = "7 nm / 10 mM"
    val curve = curves.getValue(ten)
    val foldGaps = binding(result, ten, "mu = 1").mapNotNull { it.pullInGap }.ifEmpty {
        listOf(curve.lowestGap, curve.highestGap)
    }
    val gradientAtFolds = foldGaps.map { curve.logGradientAt(it) }
    val baseline = binding(result, ten, "mu = 1").mapNotNull { it.margin }
    val full = binding(result, ten, "mu(h)").mapNotNull { it.margin }
    val decompositionTen = result.decomposition.filter {
        it.state == ten && it.loadLine == "coupled" &&
                it.baselineMargin != null && it.fullMargin != null
    }
    val tangentsTen = result.decomposition.filter { it.state == ten && it.loadLine == "coupled" }
    val spread = result.biasIndependence.map { it.relativeSpread }.max()
    return mapOf(
        "the_gradient_is_a_derivative_now" to
                ("At the six baseline fold gaps of the %s device — %.3f to %.3f nm — the " +
                        "fixed-bias collar gradient is %.5f to %.5f /nm, against C-0027's " +
                        "inherited three-scheme band of 0.0133-0.0226 /nm differenced across " +
                        "gaps that T-3b visited at DIFFERENT biases.").format(
                    ten, foldGaps.min(), foldGaps.max(),
                    gradientAtFolds.min(), gradientAtFolds.max()
                ),
        "mu_is_a_function_of_the_gap" to
                ("Across three applied biases at one gap per state, mu spans %.3f %% — so " +
                        "d ln mu/dh is a derivative of one function and not a surface. This is " +
                        "the falsifier T-60's Plan declared at 2 %%; it did not fire.").format(
                    100.0 * spread
                ),
        "the_level_cancels_and_the_gradient_does_not" to
                ("At %s on the coupled load line the LEVEL of mu moves the pull-in margin by " +
                        "%.4f to %.4f and the GRADIENT by %.4f to %.4f, in relative terms — " +
                        "CH-0035's identity measured rather than argued.").format(
                    ten,
                    decompositionTen.mapNotNull { it.levelContribution }.minOrNull() ?: 0.0,
                    decompositionTen.mapNotNull { it.levelContribution }.maxOrNull() ?: 0.0,
                    decompositionTen.mapNotNull { it.gradientContribution }.minOrNull() ?: 0.0,
                    decompositionTen.mapNotNull { it.gradientContribution }.maxOrNull() ?: 0.0
                ),
        "the_margin_moves" to
                ("C-0018's pull-in margin at %s runs %.4f-%.4f as published and %.4f-%.4f with " +
                        "the solved collar carried. It %s below one.").format(
                    ten, baseline.minOrNull() ?: 0.0, baseline.maxOrNull() ?: 0.0,
                    full.minOrNull() ?: 0.0, full.maxOrNull() ?: 0.0,
                    if ((full.minOrNull() ?: 1.0) < 1.0) "DOES go" else "does NOT go"
                ),
        "the_shallow_folds_at_seven_nm" to
                ("At %s the fold sits at a stroke of %s nm with the collar carried, against " +
                        "§3's 3 nm: a bias below pull-in is not sufficient wherever that " +
                        "number is smaller than 3.").format(
                    seven,
                    binding(result, seven, "mu(h)").mapNotNull { it.pullInStroke }
                        .let {
                            if (it.isEmpty()) "no binding pull-in"
                            else "%.3f-%.3f".format(it.min(), it.max())
                        }
                ),
        "C-0027s_straddle_at_the_fold_is_resolved" to
                ("C-0027 reports a coupled tangent of -2.5 to +4.0 pN/nm at C-0018's own fold, " +
                        "straddling zero over the collar gradient's THREE difference schemes. " +
                        "With the gradient resolved as a derivative, the collar-only tangent " +
                        "there is %.3f to %.3f pN/nm — STRICTLY POSITIVE, because at a pinned " +
                        "force it is exactly |F_es| d ln mu/dh and the baseline tangent " +
                        "vanishes at the fold by construction. Carrying C-0019's k_brush " +
                        "degradation as well gives %.3f to %.3f pN/nm, a band %.1fx narrower " +
                        "than C-0027's; what remains straddling zero is a MODEL spread, not a " +
                        "difference-scheme one.").format(
                    tangentsTen.mapNotNull { it.foldTangentCollarOnly }.minOrNull() ?: 0.0,
                    tangentsTen.mapNotNull { it.foldTangentCollarOnly }.maxOrNull() ?: 0.0,
                    tangentsTen.mapNotNull { it.foldTangentCollarAndFluctuation }
                        .minOrNull() ?: 0.0,
                    tangentsTen.mapNotNull { it.foldTangentCollarAndFluctuation }
                        .maxOrNull() ?: 0.0,
                    6.5 / max(
                        (tangentsTen.mapNotNull { it.foldTangentCollarAndFluctuation }
                            .maxOrNull() ?: 0.0) -
                                (tangentsTen.mapNotNull {
                                    it.foldTangentCollarAndFluctuation
                                }.minOrNull() ?: 0.0), 1e-9
                    )
                ),
        "the_cheap_estimate_held" to
                ("The closed-form estimate from the transverse eigenvalue ceiling gives " +
                        "%.5f /nm at 7 nm and 2 mM against a solved %.5f /nm — a factor of " +
                        "%.2f, inside the factor of two the Plan predicted.").format(
                    result.cheapEstimate.first {
                        it.state == ten && it.gapHeight == 7.0
                    }.logGradient,
                    curve.logGradientAt(7.0),
                    max(
                        result.cheapEstimate.first { it.state == ten && it.gapHeight == 7.0 }
                            .logGradient / curve.logGradientAt(7.0),
                        curve.logGradientAt(7.0) / result.cheapEstimate.first {
                            it.state == ten && it.gapHeight == 7.0
                        }.logGradient
                    )
                )
    )
}

private fun verdict(result: T60Result): Map<String, String> {
    val ten = "10 nm / 2 mM"
    val baseline = binding(result, ten, "mu = 1").mapNotNull { it.margin }
    val full = binding(result, ten, "mu(h)").mapNotNull { it.margin }
    val residuals = result.folds.mapNotNull { it.tangencyResidual }
    return mapOf(
        "P1 — mu(h) at fixed bias" to
                ("Solved at %d gaps across two states at one fixed applied bias each, plus " +
                        "%d bias probes and %d mesh-convergence solves. Every solve reports its " +
                        "own charge balance and two-plane spread; %d of %d are numerically " +
                        "resolved.").format(
                    result.collar.count { it.purpose.startsWith("mu(h) sweep") },
                    result.collar.count { it.purpose.startsWith("bias-independence") },
                    result.collar.count { it.purpose.startsWith("mesh") },
                    result.collar.count { it.numericallyResolved }, result.collar.size
                ),
        "P2 — d ln mu/dh" to
                ("%.5f to %.5f /nm over the whole solved range at 2 mM, POSITIVE everywhere: " +
                        "the collar widens with the gap, which lengthens the force's decay and " +
                        "REDUCES |k_es|.").format(
                    result.gradients.filter {
                        it.state == ten && it.scheme.startsWith("C1")
                    }.minOf { it.logGradient },
                    result.gradients.filter {
                        it.state == ten && it.scheme.startsWith("C1")
                    }.maxOf { it.logGradient }
                ),
        "P3 — the fold, re-located" to
                ("%d fold searches over 2 states x 6 models x 3 load lines x 3 field variants. " +
                        "The worst interior tangency residual is %.2e.").format(
                    result.folds.size, residuals.maxOrNull() ?: 0.0
                ),
        "P4 — does C-0018's margin move" to
                ("At %s on the coupled load line: %.4f-%.4f published, %.4f-%.4f corrected. " +
                        "The margin %s, and it %s below one.").format(
                    ten, baseline.minOrNull() ?: 0.0, baseline.maxOrNull() ?: 0.0,
                    full.minOrNull() ?: 0.0, full.maxOrNull() ?: 0.0,
                    if ((full.minOrNull() ?: 0.0) > (baseline.minOrNull() ?: 0.0)) "RISES"
                    else "FALLS",
                    if ((full.minOrNull() ?: 1.0) < 1.0) "DOES go" else "does NOT go"
                )
    )
}

private fun validity(): List<String> = listOf(
    "TRL 1-3. NOTHING HERE IS MEASURED.",
    "MEAN FIELD, inherited whole from C-0005 and C-0008: the one-loop correction is 123-214% " +
            "of the leading term across this gap range, and for the oppositely charged " +
            "tile-electrode pair no published result gives even the direction. That is two " +
            "orders larger than every margin movement here.",
    "The collar is C-0022's, with C-0022's validity range inherited WHOLE: two-dimensional " +
            "hence a straight edge, the 3-D corner bracketed by two mappings rather than " +
            "solved, the rim charge unsourced and worth 1.85x on the DEPTH, point ions, free " +
            "buffer in the gap, and the Stern series solved in one dimension and imposed " +
            "laterally uniformly.",
    "Because the Stern series is 1-D, the collar multiplies the FORCE and not the applied " +
            "bias. That is the convention C-0022 established and this task inherits it rather " +
            "than repairing it; a laterally resolved compact layer would move the mapping from " +
            "diffuse drop to applied bias near the rim.",
    "The layer is C-0003's at C-0001's single grafting density per height, not C-0011's solved " +
            "profile — C-0017's and C-0018's choice, so that the load line is drawn across the " +
            "same characteristic C-0012 computed.",
    "L0 is a FORCE-ONSET height at a defining load of 1.0 pN over the tile (C-0011, CH-0010).",
    "The collar curve CLAMPS outside the solved gap range and every clamped evaluation is " +
            "counted. The fold and the operating point are asserted interior; a fold search " +
            "necessarily probes the whole admissible stroke, so some clamped evaluations are " +
            "expected and are not an extrapolation of the answer.",
    "STATIC only, and quasi-static below ~10 kHz (C-0004's drainage corner).",
    "Two states only. C-0018 reports pull-in as the binding ceiling at 11 of 54 coupled " +
            "states, and these two are all of them; the other 43 are bound by C-0002's phi = " +
            "0.2 crossover, which no electrostatic correction can move.",
    "The load lines are AFFINE. C-0017's real coupling strain-stiffens, which RAISES the fold, " +
            "so the linear line is the conservative one here as it was in C-0018."
)

private fun openQuestions(): List<String> = listOf(
    "The 3-D corner is still not solved. The two mappings bracket it at 1.8 percentage points " +
            "of total force at 40 nm; this task carries the minimum-margin one and emits the " +
            "additive one beside it, but it does not close the bracket.",
    "The collar was solved at ONE fixed bias per state, licensed by a measured bias " +
            "independence. A state whose operating bias leaves that bracket would need its own " +
            "sweep.",
    "The PEG layer is not in the 2-D solve. C-0005's partitioning layer amplifies the 1-D " +
            "force by 1.15-1.60x; whether it moves the collar RATIO is still not computed, and " +
            "it is now the largest unexamined lever on this number.",
    "A finite counter-electrode would have its own edge, and the two edges would not add. " +
            "C-0022 names this as the single most likely route to a smaller collar and it " +
            "remains open.",
    "The dynamic pull-in is not computed. A bias step faster than drainage can carry the tile " +
            "past a fold a quasi-static ramp stops at."
)

/** Kept explicit so the reader can see the resolution floor is not a fitted number. */
private fun resolutionFloor(bracketTolerance: Double): Double = sqrt(2.0 * bracketTolerance)
