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

package com.xemantic.nano.plentyofroom.stability

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.actuator.ActuatorForceBalance
import com.xemantic.nano.plentyofroom.actuator.ActuatorGeometry
import com.xemantic.nano.plentyofroom.actuator.BiasCeiling
import com.xemantic.nano.plentyofroom.actuator.DEFAULT_COARSE_STEPS
import com.xemantic.nano.plentyofroom.actuator.DEFAULT_DIFFUSE_CEILING
import com.xemantic.nano.plentyofroom.actuator.DEFAULT_DIFFUSE_TOLERANCE
import com.xemantic.nano.plentyofroom.actuator.DEFAULT_STROKE_TOLERANCE
import com.xemantic.nano.plentyofroom.actuator.DiffuseParametrisedField
import com.xemantic.nano.plentyofroom.actuator.EquilibriumPath
import com.xemantic.nano.plentyofroom.actuator.FieldSample
import com.xemantic.nano.plentyofroom.actuator.bindingCeiling
import com.xemantic.nano.plentyofroom.actuator.biasMargin
import com.xemantic.nano.plentyofroom.actuator.roundedForActuatorResult
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
import com.xemantic.nano.plentyofroom.electrostatics.DnaOrigamiTile
import com.xemantic.nano.plentyofroom.electrostatics.GapMedium
import com.xemantic.nano.plentyofroom.electrostatics.IonModel
import com.xemantic.nano.plentyofroom.electrostatics.MagnesiumChlorideBuffer
import com.xemantic.nano.plentyofroom.electrostatics.PoissonBoltzmannGap
import com.xemantic.nano.plentyofroom.electrostatics.bjerrumLength
import com.xemantic.nano.plentyofroom.electrostatics.diffusePotentialOfAppliedBias
import com.xemantic.nano.plentyofroom.electrostatics.sternChargeDensityPerVolt
import com.xemantic.nano.plentyofroom.electrostatics.thermalVoltage
import com.xemantic.nano.plentyofroom.electrostatics.uniformMedium
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Task `T-149` — `C-0018`'s pull-in machinery run on the load line `C-0071` actually recommends.
 * Leaf `A2.2`, with `A8.2`.
 *
 * Run with:
 *
 * ```shell
 * tools/study.sh stability.RecommendedElementFoldStudyKt
 * ```
 *
 * Emits `gpd/results/T-149-recommended-element-fold.json`, deterministically — no timestamp, every
 * floating-point number rounded at the serialisation boundary.
 *
 * Consumes `C-0018`'s `EquilibriumPath`, `C-0012`'s force balance and `C-0003`'s six layer models
 * **unchanged**, and `C-0039`'s exact two-spring elastica as the load line. Owns
 * `stability/RecommendedElementFold.kt` and this file, and edits nothing.
 */

// ---------------------------------------------------------------------------------------------
// records — every one prefixed with this study's own name, per CLAUDE.md
// ---------------------------------------------------------------------------------------------

/** One coupling law, read as a design: what it places with and what it stabilises with. */
@Serializable
@Suppress("LongParameterList")
data class T149CouplingRecord(
    val line: String,
    val kind: String,
    val pathCount: Int,
    val length: Double,
    val lengthBasePairs: Double,
    val rootStiffness: Double,
    val tipStiffness: Double,
    val reactionAcceptable: Double,
    val secantAcceptable: Double,
    val tangentAcceptable: Double,
    val tangentToSecant: Double,
    val smallRotationStiffness: Double,
    val minimumTangentTraversed: Double,
    val minimumTangentTraversedStroke: Double,
    val minimumTangentTraversedInterior: Boolean,
    val floorsClearedTwoMillimolar: Int,
    val strainStiffening: Boolean,
    val refusalStrokeCeiling: Double,
    val refusalCeilingIsMonotone: Boolean,
    val branchValidityStrokeCeiling: Double,
    val rotationAtBranchCeiling: Double,
    val rotationAtAcceptableStroke: Double,
    val placementResidual: Double
)

/** The cheap bound at one state: the two corrections a substitution applies at the baseline fold. */
@Serializable
@Suppress("LongParameterList")
data class T149CheapBoundRecord(
    val model: String,
    val layerHeight: Double,
    val concentration: Double,
    val perturbation: FoldPerturbation,
    val solvedBaselineFoldStroke: Double?,
    val solvedSubstitutedFoldStroke: Double?,
    val solvedDirection: String,
    val predictionAgrees: Boolean?
)

/** One `(height, model, buffer, load line)` state: the fold, and the two stability readings. */
@Serializable
@Suppress("LongParameterList")
data class T149FoldRecord(
    val model: String,
    val layerHeight: Double,
    val graftingDensity: Double,
    val concentration: Double,
    val loadLine: String,
    val restingHeight: Double,
    val layerStrokeCeiling: Double,
    val strokeCeiling: Double,
    val strokeCeilingOwner: String,
    // the operating point §3 asks for — identical across the lines by placement
    val operatingBias: Double?,
    val operatingGap: Double?,
    val operatingVolumeFraction: Double?,
    val brushStiffnessAtOperating: Double?,
    val electrostaticStiffnessAtOperating: Double?,
    val effectiveStiffnessAtOperating: Double?,
    val stabilityFloor: Double?,
    val couplingTangentAtOperating: Double,
    val marginAtWorkingPoint: Double?,
    val minimumTangentTraversed: Double,
    val marginOnMinimumTangent: Double?,
    val stableOnMinimumTangent: Boolean?,
    // ceiling 1 — the fold of the path under THIS line
    val pullInBias: Double?,
    val pullInStroke: Double?,
    val pullInGap: Double?,
    val foldAtBranchStart: Boolean,
    val couplingTangentAtFold: Double?,
    val brushStiffnessAtFold: Double?,
    val electrostaticStiffnessAtFold: Double?,
    val effectiveStiffnessAtFold: Double?,
    val coupledTangentAtFold: Double?,
    val tangencyResidual: Double?,
    val branchEndStroke: Double?,
    val branchEndBias: Double?,
    val branchEndedOnTheField: Boolean,
    val branchEndedOnTheElementModel: Boolean,
    // ceiling 2 — the upstream validity boundary that binds almost everywhere in C-0018
    val concentratedCrossoverBias: Double?,
    val concentratedCrossoverBeyondFold: Boolean,
    val pointIonBias: Double,
    // the verdict
    val bindingCeiling: String?,
    val usableBias: Double?,
    val biasMargin: Double?,
    val biasMarginIgnoringElementBoundary: Double?,
    val targetStrokeOnStableSide: Boolean?,
    val operatingPointIsUsable: Boolean?,
    val verdict: String,
    val searchEvaluations: Int
)

/** One **device** — one buffer, one layer, `L₀ → L₀ − 3 nm` — read over its own six models. */
@Serializable
@Suppress("LongParameterList")
data class T149DeviceRecord(
    val device: String,
    val layerHeight: Double,
    val concentration: Double,
    val loadLine: String,
    val isTheRecommendedDevice: Boolean,
    val models: Int,
    val statesWithAFold: Int,
    val pullInBiasMinimum: Double?,
    val pullInBiasMaximum: Double?,
    val foldStrokeMinimum: Double?,
    val foldStrokeMaximum: Double?,
    val biasMarginMinimum: Double?,
    val biasMarginMaximum: Double?,
    val targetStrokeOnStableSide: Int,
    val pullInIsTheBindingCeiling: Int,
    val usableStates: Int,
    val verdict: String
)

/** Upstream numbers reproduced through this study's own pipeline — the gate-5 record. */
@Serializable
data class T149UpstreamRecord(
    val quantity: String,
    val state: String,
    val here: Double,
    val upstream: Double,
    val departure: Double,
    val source: String
)

/** One convergence axis, referred to **its own** finest setting. */
@Serializable
data class T149ConvergenceRecord(
    val axis: String,
    val setting: String,
    val quantity: String,
    val value: Double,
    val departureFromFinest: Double
)

/** The `T-149` result envelope. */
@Serializable
@Suppress("LongParameterList")
data class RecommendedElementFoldResult(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val runParameters: Map<String, String>,
    val innerLoopCosts: Map<String, String>,
    val citedInputs: List<String>,
    val couplings: List<T149CouplingRecord>,
    val cheapBound: List<T149CheapBoundRecord>,
    val folds: List<T149FoldRecord>,
    val devices: List<T149DeviceRecord>,
    val upstreamChecks: List<T149UpstreamRecord>,
    val convergence: List<T149ConvergenceRecord>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

// ---------------------------------------------------------------------------------------------
// the sweep
// ---------------------------------------------------------------------------------------------

/** §3's three layer heights with `C-0001`'s grafting densities — `C-0018`'s own design points. */
private val DESIGN_POINTS = listOf(5.0 to 0.092, 7.0 to 0.045, 10.0 to 0.024)

/** Leaf `A2.2`'s low-screening point, §3's nominal buffer, and §3's high one — `C-0018`'s own. */
private val BUFFERS = listOf(0.5, 2.0, 10.0)

/** `C-0071`'s recommended device is the **10 nm** layer: `C-0068` shows the layer selects the phase. */
private const val RECOMMENDED_LAYER_HEIGHT = 10.0

private const val TRUSTED_BIAS_CEILING = 1.0

private const val CONCENTRATED_CROSSOVER = 0.2

private const val STERN_CAPACITANCE = 20.0

private const val MESH_NODES = 2000

private const val CURVE_LOWEST_GAP = 0.5

private const val OSMOTIC_SECOND_VIRIAL = 1.9e-3

private const val OSMOTIC_THIRD_VIRIAL = 2.0e-2

private const val FOOTPRINT = 1600.0

/** `C-0017`'s six stability floors `|k_eff(3 nm)|` at the 10 nm layer in 2 mM — **CITED**. */
private val FLOORS_TWO_MILLIMOLAR = listOf(
    27.9133262, 23.4139164, 24.9042565, 27.0387111, 23.8036442, 23.9527371
)

/** The safety a path's stroke ceiling keeps below the element model's own branch ceiling, in nm. */
private const val ELEMENT_CEILING_SAFETY = 0.01

/** The one state at which the RECOMMENDED line still folds — where gate 4's axes are read. */
private const val CONVERGENCE_HEIGHT = 7.0

private const val CONVERGENCE_DENSITY = 0.045

private const val CONVERGENCE_BUFFER = 10.0

private const val CONVERGENCE_MODEL = "alexander-box(two-body)"

/** The field, parametrised by the **diffuse-layer drop** — `C-0018`'s cheap direction. */
private class T149Field(
    concentration: Double,
    val tileCharge: Double,
    val bjerrum: Double,
    val nodes: Int = MESH_NODES
) {

    private val ions = IonModel(MagnesiumChlorideBuffer(concentration).magnesiumNumberDensity)

    private val medium = uniformMedium(GapMedium())

    private val stern = sternChargeDensityPerVolt(STERN_CAPACITANCE)

    private val volt = thermalVoltage()

    fun sample(gap: Double, diffusePotential: Double): FieldSample {
        val solution = PoissonBoltzmannGap(gap, ions, medium, bjerrum, nodes = nodes)
            .solve(diffusePotential / volt, tileCharge)
        return FieldSample(
            gap = gap,
            diffusePotential = diffusePotential,
            appliedBias = diffusePotential + solution.electrodeSurfaceChargeDensity / stern,
            force = solution.forceOnTile(FOOTPRINT)
        )
    }

    fun asPath(): DiffuseParametrisedField = DiffuseParametrisedField { gap, psi -> sample(gap, psi) }

    /** The **signed** force in pN at a given **applied** bias — `C-0008`'s own direction. */
    fun forceAtBias(gap: Double, bias: Double): Double {
        val diffuse = diffusePotentialOfAppliedBias(
            gap, bias, tileCharge, stern, ions, medium, bjerrum, nodes = nodes
        )
        return PoissonBoltzmannGap(gap, ions, medium, bjerrum, nodes = nodes)
            .solve(diffuse / volt, tileCharge)
            .forceOnTile(FOOTPRINT)
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

/** Everything a fold search needs about one state, so the two lines are run over the same object. */
private class T149State(
    val model: String,
    val height: Double,
    val density: Double,
    val concentration: Double,
    val balance: ActuatorForceBalance,
    val chain: GraftedChain,
    val field: T149Field
) {

    val resting: Double = balance.restingHeight

    val layerStrokeCeiling: Double =
        resting - max(chain.occupiedThickness * 1.01, CURVE_LOWEST_GAP)
}

@Suppress("LongMethod")
fun main() {
    val peg = PegWater()
    val geometry = ActuatorGeometry()
    val tile = DnaOrigamiTile()
    val lb = bjerrumLength()
    val surviving = tile.manningSurvivingFraction(2, lb)
    val tileCharge = -(tile.projectedChargeDensity * surviving / 2.0)
    val models = layerModels(peg)
    val fields = BUFFERS.associateWith { T149Field(it, tileCharge, lb) }

    println("T-149 — the recommended element's own law, before any field solve ...")
    val mandate = AffineLoadLine("L1 linear mandate (C-0018)", GEN1_MANDATE_STIFFNESS)
    val recommended = recommendedArmLine("LQ5 recommended hinge-rooted arm (C-0071)")
    val refusalCeiling = loadLineStrokeCeiling(recommended, 0.1, recommended.length)
    val branchCeiling = rotationLimitStroke(recommended, 0.1, recommended.length)
    val elementCeiling = min(refusalCeiling, branchCeiling) - ELEMENT_CEILING_SAFETY
    val couplings = listOf(
        couplingRecord(mandate, Double.POSITIVE_INFINITY, Double.POSITIVE_INFINITY, true),
        couplingRecord(recommended, refusalCeiling, branchCeiling, false)
    )
    couplings.forEach {
        println(
            ("  %s: length %.5f nm, secant %.4f, tangent %.4f, t/s %.4f, min %.4f, " +
                    "floors %d of 6, ceiling %.4f nm")
                .format(
                    it.line, it.length, it.secantAcceptable, it.tangentAcceptable,
                    it.tangentToSecant, it.minimumTangentTraversed,
                    it.floorsClearedTwoMillimolar, it.branchValidityStrokeCeiling
                )
        )
    }

    println("T-149 — measuring the inner loop before choosing the outer budget ...")
    val costs = innerLoopCosts(recommended, fields.getValue(2.0))
    costs.forEach { (key, value) -> println("  $key: $value") }

    println("T-149 — the sweep: the baseline fold, the cheap bound, then the recommended fold ...")
    val folds = mutableListOf<T149FoldRecord>()
    val bounds = mutableListOf<T149CheapBoundRecord>()
    DESIGN_POINTS.forEach { (height, density) ->
        models.forEach { (name, model) ->
            val chain = peg.graftedChain(model.chainLengthForHeight(peg, height, density), density)
            val balance = ActuatorForceBalance(model, chain, geometry)
            BUFFERS.forEach { concentration ->
                val state = T149State(
                    name, height, density, concentration, balance, chain,
                    fields.getValue(concentration)
                )
                val baseline = foldRecord(state, mandate, couplings[0], state.layerStrokeCeiling, "layer")
                folds += baseline
                val ceiling = min(state.layerStrokeCeiling, elementCeiling)
                val owner = if (ceiling < state.layerStrokeCeiling) "element model" else "layer"
                val substituted = foldRecord(state, recommended, couplings[1], ceiling, owner)
                folds += substituted
                bounds += cheapBound(state, mandate, recommended, baseline, substituted)
            }
            println("  $height nm  $name done")
        }
    }

    println("T-149 — devices, upstream reproductions and convergence ...")
    val devices = deviceRecords(folds)
    val upstream = upstreamChecks(couplings, folds)
    val convergence = convergence(peg, geometry, tileCharge, lb, recommended, elementCeiling)

    val result = RecommendedElementFoldResult(
        task = "T-149",
        leaf = "A2.2",
        title = "The pull-in fold of the output element C-0071 actually recommends: C-0018's " +
                "equilibrium path re-run with C-0039's exact elastica arm as the load line, with " +
                "the fold's own STROKE and the bias MARGIN read on the axes each is controlled on",
        verificationType = "in-silico (C-0018's stroke-parametrised equilibrium path, solver " +
                "unchanged, with C-0071's recommended 34-arm array substituted for the affine " +
                "R = 33.3333 s over the same (height, model, buffer) grid, graded against the " +
                "tangency identity k_c(s_fold) + k_eff(s_fold) = 0 with k_c from the element's " +
                "own differenced law and k_es from a central difference of a full field re-solve " +
                "at fixed applied bias) + logical (a cheap sign bound evaluated at the BASELINE " +
                "fold, where the coupled tangent vanishes by construction, so the composition is " +
                "exact and the slope term costs one evaluation of each law)",
        acceptance = "Q1 placement: both lines' assembled secant is 33.3333 pN/nm at 3 nm and " +
                "both locate the SAME operating bias at every state. Q2 the cheap bound runs " +
                "FIRST and its predicted direction is recorded before the substituted search. " +
                "Q3 the fold's own STROKE against §3's 3 nm target, per device. Q4 the bias " +
                "MARGIN, on the bias axis, per device. Q5 the verdict at 2 mM and at 0.5 mM, " +
                "since DECISIONS-FOR-NDI decision 1 turns on exactly this margin.",
        maturity = "TRL 1-3 - model-consistent and traceable, NOT empirically demonstrated. " +
                "Every force inherits C-0008's mean-field statement in full (C-0005: the " +
                "one-loop correction is 123-214% of the leading term across this gap range), " +
                "and the MOTIF is not demonstrated either: C-0055's literature finding stands, " +
                "a free lever held to a single-layer sheet by ONE crossover was not found in 62 " +
                "recorded queries. NOTHING HERE IS MEASURED.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm (= 1 mN/m exactly)",
            "rotationalStiffness" to "pN nm/rad",
            "rotation" to "rad",
            "potential" to "V",
            "concentration" to "mM",
            "temperature" to "K"
        ),
        conventions = CONVENTIONS,
        runParameters = mapOf(
            "temperature" to ROOM_TEMPERATURE.toString(),
            "thermalEnergy" to thermalEnergy().toString(),
            "bjerrumLength" to lb.roundedForProse().toString(),
            "footprintArea" to FOOTPRINT.toString(),
            "manningSurvivingFraction" to surviving.roundedForProse().toString(),
            "nominalTileChargeDensity" to tileCharge.roundedForProse().toString(),
            "sternCapacitance" to STERN_CAPACITANCE.toString(),
            "meshNodes" to MESH_NODES.toString(),
            "layerHeights" to DESIGN_POINTS.map { it.first }.toString(),
            "graftingDensities" to DESIGN_POINTS.map { it.second }.toString(),
            "buffers" to BUFFERS.toString(),
            "recommendedLayerHeight" to RECOMMENDED_LAYER_HEIGHT.toString(),
            "loadLines" to listOf(mandate.name, recommended.name).toString(),
            "pathCount" to GEN1_RECOMMENDED_PATH_COUNT.toString(),
            "armRootStiffness" to GEN1_ARM_ROOT_STIFFNESS.roundedForProse().toString(),
            "armTipStiffness" to GEN1_ARM_TIP_STIFFNESS.roundedForProse().toString(),
            "armLength" to recommended.length.roundedForProse().toString(),
            "duplexBendingRigidity" to Gen1Tile.DUPLEX_BENDING_RIGIDITY.toString(),
            "targetForce" to GEN1_TARGET_FORCE.toString(),
            "acceptableStroke" to GEN1_ACCEPTABLE_STROKE.toString(),
            "mandatedCouplingStiffness" to GEN1_MANDATE_STIFFNESS.roundedForProse().toString(),
            "elementStrokeCeiling" to elementCeiling.roundedForProse().toString(),
            "elementCeilingSafety" to ELEMENT_CEILING_SAFETY.toString(),
            "concentratedCrossover" to CONCENTRATED_CROSSOVER.toString(),
            "trustedBiasCeiling" to TRUSTED_BIAS_CEILING.toString(),
            "diffuseCeiling" to DEFAULT_DIFFUSE_CEILING.toString(),
            "diffuseBracketTolerance" to DEFAULT_DIFFUSE_TOLERANCE.toString(),
            "foldCoarseSteps" to DEFAULT_COARSE_STEPS.toString(),
            "foldStrokeTolerance" to DEFAULT_STROKE_TOLERANCE.toString(),
            "curveLowestGap" to CURVE_LOWEST_GAP.toString()
        ),
        innerLoopCosts = costs,
        citedInputs = CITED,
        couplings = couplings,
        cheapBound = bounds,
        folds = folds,
        devices = devices,
        upstreamChecks = upstream,
        convergence = convergence,
        findings = emptyMap(),
        validity = VALIDITY,
        openQuestions = OPEN
    )
    val complete = result.copy(findings = findings(result))
    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-149-recommended-element-fold.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(json.encodeToJsonElement(complete).roundedForActuatorResult()) + "\n"
    )
    report(complete, output)
}

// ---------------------------------------------------------------------------------------------
// the coupling laws
// ---------------------------------------------------------------------------------------------

private fun couplingRecord(
    line: StrokeLoadLine,
    refusalCeiling: Double,
    branchCeiling: Double,
    unbounded: Boolean
): T149CouplingRecord {
    val arm = line as? ElasticaArmLoadLine
    val traversed = line.tangentMinimum(0.0, GEN1_ACCEPTABLE_STROKE)
    val tangent = line.tangent(GEN1_ACCEPTABLE_STROKE)
    val reportedRefusal = if (unbounded) GEN1_ACCEPTABLE_STROKE else refusalCeiling
    val reportedBranch = if (unbounded) GEN1_ACCEPTABLE_STROKE else branchCeiling
    return T149CouplingRecord(
        line = line.name,
        kind = if (arm == null) "affine mandate" else "end-loaded elastica arm (C-0039)",
        pathCount = arm?.count ?: 0,
        length = arm?.length ?: 0.0,
        lengthBasePairs = (arm?.length ?: 0.0) / Gen1Tile.RISE_PER_BASE_PAIR,
        rootStiffness = if (arm == null) 0.0 else GEN1_ARM_ROOT_STIFFNESS,
        tipStiffness = if (arm == null) 0.0 else GEN1_ARM_TIP_STIFFNESS,
        reactionAcceptable = line.reaction(GEN1_ACCEPTABLE_STROKE),
        secantAcceptable = line.secant(GEN1_ACCEPTABLE_STROKE),
        tangentAcceptable = tangent,
        tangentToSecant = line.tangentToSecant(GEN1_ACCEPTABLE_STROKE),
        smallRotationStiffness = arm?.smallRotationStiffness ?: line.tangent(0.0),
        minimumTangentTraversed = traversed.stiffness,
        minimumTangentTraversedStroke = traversed.stroke,
        minimumTangentTraversedInterior = traversed.interior,
        floorsClearedTwoMillimolar = FLOORS_TWO_MILLIMOLAR.count { traversed.stiffness > it },
        strainStiffening = line.tangentToSecant(GEN1_ACCEPTABLE_STROKE) >= 1.0,
        refusalStrokeCeiling = reportedRefusal,
        refusalCeilingIsMonotone =
            unbounded || strokeCeilingIsMonotone(line, refusalCeiling, arm?.length ?: refusalCeiling),
        branchValidityStrokeCeiling = reportedBranch,
        rotationAtBranchCeiling = arm?.maximumRotation(branchCeiling) ?: 0.0,
        rotationAtAcceptableStroke = arm?.maximumRotation(GEN1_ACCEPTABLE_STROKE) ?: 0.0,
        placementResidual = abs(line.secant(GEN1_ACCEPTABLE_STROKE) / GEN1_MANDATE_STIFFNESS - 1.0)
    )
}

// ---------------------------------------------------------------------------------------------
// the cost of the inner loop, measured before the outer budget is chosen
// ---------------------------------------------------------------------------------------------

/**
 * The measured per-call cost of the two inner loops, **printed and not emitted**.
 *
 * A wall-clock timing cannot live in a result file: `gpd/README.md` requires that a re-run which
 * changes nothing produces no diff, and three runs of this study agreed on all 108 folds and
 * differed on exactly these three numbers. It is the same rule as `CLAUDE.md`'s *"emit the answer
 * and a two-significant-digit convergence measure; emit nothing that counts steps"*, one step
 * further out — a timing is less reproducible than a step count, not more. So the measurement is
 * made, reported on the console and quoted in the claim as a wall-clock reading, and what reaches
 * the file is the deterministic part: the solve **count** per path point and the verdict.
 */
private fun innerLoopCosts(arm: ElasticaArmLoadLine, field: T149Field): Map<String, String> {
    val elasticaStart = System.nanoTime()
    repeat(ELASTICA_SAMPLES) { arm.reaction(1.0 + 2.0 * it / ELASTICA_SAMPLES) }
    val elastica = (System.nanoTime() - elasticaStart) / 1.0e6 / ELASTICA_SAMPLES
    val fieldStart = System.nanoTime()
    repeat(FIELD_SAMPLES) { field.sample(8.0, 0.05 + 0.01 * it / FIELD_SAMPLES) }
    val solve = (System.nanoTime() - fieldStart) / 1.0e6 / FIELD_SAMPLES
    println(
        ("  measured, NOT emitted (a wall-clock timing is not reproducible): one elastica " +
                "reaction %.3f ms, one Poisson-Boltzmann solve %.3f ms, ratio of field per path " +
                "point to one elastica reaction %.1f")
            .format(elastica, solve, 42.0 * solve / max(elastica, 1e-9))
    )
    return mapOf(
        "field solves per path point (diffuse bisection to 1e-10 relative over [1e-6, 0.35] V)"
                to "~42",
        "elastica reactions per path point" to "1 (plus 2 for a tangent, at the fold only)",
        "wall-clock per-call costs" to "MEASURED AND PRINTED, NOT EMITTED — a timing is not " +
                "reproducible and gpd/README.md requires that a re-run changing nothing " +
                "produces no diff. See the claim for the reading and its spread over three runs",
        "verdict" to "the elastica is the CHEAP half, which is the opposite of what CH-0083 " +
                "assumed, so the tabulate-and-interpolate route it suggests is not taken: an " +
                "interpolation would add an error term for no measurable saving"
    )
}

private const val ELASTICA_SAMPLES = 20

private const val FIELD_SAMPLES = 20

// ---------------------------------------------------------------------------------------------
// the folds
// ---------------------------------------------------------------------------------------------

@Suppress("LongMethod")
private fun foldRecord(
    state: T149State,
    line: StrokeLoadLine,
    coupling: T149CouplingRecord,
    strokeCeiling: Double,
    ceilingOwner: String
): T149FoldRecord {
    val resting = state.resting
    val balance = state.balance
    val field = state.field
    val path = EquilibriumPath(
        restingHeight = resting,
        strokeCeiling = strokeCeiling,
        field = field.asPath()
    ) { stroke -> line.reaction(stroke) + balance.layerLoad(resting - stroke) }
    val search = path.fold()
    val fold = search.fold
    val operating =
        if (GEN1_ACCEPTABLE_STROKE <= strokeCeiling) path.at(GEN1_ACCEPTABLE_STROKE) else null

    val brushOperating = operating?.let { balance.layerStiffness(it.gap) }
    val esOperating = operating?.let { field.stiffnessAtBias(it.gap, it.appliedBias) }
    val effectiveOperating =
        if (brushOperating != null && esOperating != null) brushOperating + esOperating else null
    val floorOperating = effectiveOperating?.let { if (it < 0.0) -it else 0.0 }
    val tangentOperating = line.tangent(GEN1_ACCEPTABLE_STROKE)
    val minimumTangent = coupling.minimumTangentTraversed

    val brushFold = fold?.let { balance.layerStiffness(it.gap) }
    val esFold = fold?.let { field.stiffnessAtBias(it.gap, it.appliedBias) }
    val effectiveFold = if (brushFold != null && esFold != null) brushFold + esFold else null
    val tangentFold = fold?.let { line.tangent(it.stroke) }
    val coupledFold =
        if (effectiveFold != null && tangentFold != null) tangentFold + effectiveFold else null
    // a boundary maximum is not a stationary point — no residual is reported there
    val atBranchEnd = fold != null && abs(fold.stroke - strokeCeiling) <= 1e-6
    val residual = if (search.foldAtBranchStart || atBranchEnd) null else coupledFold?.let {
        abs(it) / max(
            (tangentFold ?: 0.0) + abs(brushFold ?: 0.0) + abs(esFold ?: 0.0), 1e-12
        )
    }

    val crossoverGap = state.chain.occupiedThickness / CONCENTRATED_CROSSOVER
    val crossoverStroke = resting - crossoverGap
    val crossover =
        if (crossoverStroke <= 0.0 || crossoverStroke > strokeCeiling) null
        else path.at(crossoverStroke)?.appliedBias
    val crossoverBeyond = crossoverStroke > (fold?.stroke ?: strokeCeiling)

    // C-0018's three ceilings, unchanged, so the affine line reproduces it exactly ...
    val inheritedCandidates = listOf(
        BiasCeiling("static stability (pull-in)", fold?.appliedBias),
        BiasCeiling("concentrated crossover (C-0002, phi = 0.2)", if (crossoverBeyond) null else crossover),
        BiasCeiling("point-ion boundary (CH-0007, 1.0 V)", TRUSTED_BIAS_CEILING)
    )
    // ... and a FOURTH that this task's load line introduces and C-0018's could not have. Where
    // the path is truncated by the ELEMENT MODEL and has no fold below it, the largest bias the
    // model describes is the branch end's, and letting CH-0007's 1.0 V bind instead would quote a
    // margin over a state the element model does not cover. It is named a MODEL boundary, not a
    // device ceiling, and biasMarginIgnoringElementBoundary carries the reading without it.
    val elementBoundary =
        fold == null && ceilingOwner == "element model" && !search.reachedDiffuseCeiling
    val candidates =
        if (!elementBoundary) inheritedCandidates
        else inheritedCandidates + BiasCeiling(
            "element model branch end (C-0039's small-rotation branch)",
            search.branchEnd?.appliedBias
        )
    val binding = bindingCeiling(candidates)
    val inherited = bindingCeiling(inheritedCandidates)
    val onStableSide = operating?.let { fold == null || GEN1_ACCEPTABLE_STROKE <= fold.stroke + 1e-9 }
    val usable = operating?.let { point ->
        binding?.bias?.let { ceiling -> point.appliedBias <= ceiling && onStableSide == true }
    }
    val stableMinimum = floorOperating?.let { minimumTangent > it }
    return T149FoldRecord(
        model = state.model,
        layerHeight = state.height,
        graftingDensity = state.density,
        concentration = state.concentration,
        loadLine = line.name,
        restingHeight = resting,
        layerStrokeCeiling = state.layerStrokeCeiling,
        strokeCeiling = strokeCeiling,
        strokeCeilingOwner = ceilingOwner,
        operatingBias = operating?.appliedBias,
        operatingGap = operating?.gap,
        operatingVolumeFraction = operating?.let { state.chain.meanVolumeFraction(it.gap) },
        brushStiffnessAtOperating = brushOperating,
        electrostaticStiffnessAtOperating = esOperating,
        effectiveStiffnessAtOperating = effectiveOperating,
        stabilityFloor = floorOperating,
        couplingTangentAtOperating = tangentOperating,
        marginAtWorkingPoint = effectiveOperating?.let { stabilityMargin(tangentOperating, it) },
        minimumTangentTraversed = minimumTangent,
        marginOnMinimumTangent = effectiveOperating?.let { stabilityMargin(minimumTangent, it) },
        stableOnMinimumTangent = stableMinimum,
        pullInBias = fold?.appliedBias,
        pullInStroke = fold?.stroke,
        pullInGap = fold?.gap,
        foldAtBranchStart = search.foldAtBranchStart,
        couplingTangentAtFold = tangentFold,
        brushStiffnessAtFold = brushFold,
        electrostaticStiffnessAtFold = esFold,
        effectiveStiffnessAtFold = effectiveFold,
        coupledTangentAtFold = coupledFold,
        tangencyResidual = residual,
        branchEndStroke = search.branchEnd?.stroke,
        branchEndBias = search.branchEnd?.appliedBias,
        branchEndedOnTheField = search.reachedDiffuseCeiling,
        branchEndedOnTheElementModel = elementBoundary,
        concentratedCrossoverBias = crossover,
        concentratedCrossoverBeyondFold = crossoverBeyond,
        pointIonBias = TRUSTED_BIAS_CEILING,
        bindingCeiling = binding?.name,
        usableBias = binding?.bias,
        biasMargin = biasMargin(binding?.bias, operating?.appliedBias),
        biasMarginIgnoringElementBoundary =
            biasMargin(inherited?.bias, operating?.appliedBias),
        targetStrokeOnStableSide = onStableSide,
        operatingPointIsUsable = usable,
        verdict = verdict(usable, onStableSide, stableMinimum, binding?.name),
        searchEvaluations = path.evaluations
    )
}

private fun verdict(
    usable: Boolean?,
    onStableSide: Boolean?,
    stableOnMinimum: Boolean?,
    binding: String?
): String = when {
    usable == null -> "no operating point — the target stroke is outside the model floor"
    onStableSide == false -> "FAIL — the fold sits SHALLOWER than §3's 3 nm target stroke"
    !usable -> "FAIL — the operating bias exceeds the binding ceiling ($binding)"
    stableOnMinimum == false -> "PASS on the fold, FAIL on the static floor — stable at the held " +
            "point, but the tangent minimum over the traversed range is below |k_eff| there"
    else -> "PASS"
}

// ---------------------------------------------------------------------------------------------
// the cheap bound, recorded against what the sweep found
// ---------------------------------------------------------------------------------------------

private fun cheapBound(
    state: T149State,
    baseline: StrokeLoadLine,
    substituted: StrokeLoadLine,
    baselineFold: T149FoldRecord,
    substitutedFold: T149FoldRecord
): T149CheapBoundRecord {
    val at = baselineFold.pullInStroke ?: GEN1_ACCEPTABLE_STROKE
    val perturbation = foldPerturbation(baseline, substituted, at)
    val before = baselineFold.pullInStroke
    val after = substitutedFold.pullInStroke
    val solved = when {
        before == null && after == null -> "NEITHER FOLDS"
        before == null -> "FOLD ACQUIRED"
        after == null -> "FOLD REMOVED"
        after > before + FOLD_STROKE_DECISION -> FoldDirection.DEEPER.name
        after < before - FOLD_STROKE_DECISION -> FoldDirection.SHALLOWER.name
        else -> FoldDirection.UNMOVED.name
    }
    // "fold removed" and "fold acquired" are the extreme readings of DEEPER and SHALLOWER: a fold
    // pushed past the branch end has left the model, and one pulled in from beyond it has entered
    val agrees = when (solved) {
        "NEITHER FOLDS" -> null
        "FOLD REMOVED" -> perturbation.predictedDirection == FoldDirection.DEEPER.name
        "FOLD ACQUIRED" -> perturbation.predictedDirection == FoldDirection.SHALLOWER.name
        else -> solved == perturbation.predictedDirection
    }
    return T149CheapBoundRecord(
        model = state.model,
        layerHeight = state.height,
        concentration = state.concentration,
        perturbation = perturbation,
        solvedBaselineFoldStroke = before,
        solvedSubstitutedFoldStroke = after,
        solvedDirection = solved,
        predictionAgrees = agrees
    )
}

/** The stroke movement below which a fold is called UNMOVED — 100× the search's own bracket. */
private const val FOLD_STROKE_DECISION = 1.0e-2

// ---------------------------------------------------------------------------------------------
// the devices
// ---------------------------------------------------------------------------------------------

private fun deviceRecords(folds: List<T149FoldRecord>): List<T149DeviceRecord> =
    folds.groupBy { Triple(it.layerHeight, it.concentration, it.loadLine) }
        .toSortedMap(
            compareBy({ it.first }, { it.second }, { it.third })
        )
        .map { (key, rows) ->
            val (height, concentration, line) = key
            val folded = rows.mapNotNull { it.pullInStroke }
            val biases = rows.mapNotNull { it.pullInBias }
            val margins = rows.mapNotNull { it.biasMargin }
            val onStable = rows.count { it.targetStrokeOnStableSide == true }
            val usable = rows.count { it.operatingPointIsUsable == true }
            T149DeviceRecord(
                device = ("%.0f nm layer, %.1f mM MgCl2, placed at 3 nm").format(height, concentration),
                layerHeight = height,
                concentration = concentration,
                loadLine = line,
                isTheRecommendedDevice = height == RECOMMENDED_LAYER_HEIGHT,
                models = rows.size,
                statesWithAFold = folded.size,
                pullInBiasMinimum = biases.minOrNull(),
                pullInBiasMaximum = biases.maxOrNull(),
                foldStrokeMinimum = folded.minOrNull(),
                foldStrokeMaximum = folded.maxOrNull(),
                biasMarginMinimum = margins.minOrNull(),
                biasMarginMaximum = margins.maxOrNull(),
                targetStrokeOnStableSide = onStable,
                pullInIsTheBindingCeiling =
                    rows.count { it.bindingCeiling == "static stability (pull-in)" },
                usableStates = usable,
                verdict = when {
                    usable == rows.size && folded.isEmpty() ->
                        "PASS — no fold at any model; the ceiling has another owner"
                    usable == rows.size -> "PASS — the fold is deeper than §3's 3 nm at every model"
                    usable == 0 -> "FAIL at every model"
                    else -> "MIXED — $usable of ${rows.size} models usable"
                }
            )
        }

// ---------------------------------------------------------------------------------------------
// gate 5 — the upstream reproductions
// ---------------------------------------------------------------------------------------------

/** `C-0069`'s published `Q5` row, and `C-0018`'s / `C-0032`'s published bands. */
private val C0069 = listOf(
    Triple("C-0069 Q5 arm length [nm]", 8.16439083, "length"),
    Triple("C-0069 Q5 arm length [bp]", 24.0129142, "lengthBasePairs"),
    Triple("C-0069 Q5 assembled secant at 3 nm [pN/nm]", 33.3333333, "secant"),
    Triple("C-0069 Q5 assembled tangent at 3 nm [pN/nm]", 40.8120233, "tangent"),
    Triple("C-0069 Q5 assembled tangent minimum over [0, 3] [pN/nm]", 30.028762, "minimum"),
    Triple("C-0055 one antiparallel crossover [pN nm/rad]", 13.5294118, "root"),
    Triple("C-0034 A2 duplex-end anchorage [pN nm/rad]", 78.2352941, "tip")
)

@Suppress("LongMethod")
private fun upstreamChecks(
    couplings: List<T149CouplingRecord>,
    folds: List<T149FoldRecord>
): List<T149UpstreamRecord> {
    val checks = mutableListOf<T149UpstreamRecord>()
    fun add(quantity: String, state: String, here: Double, upstream: Double, source: String) {
        checks += T149UpstreamRecord(
            quantity = quantity,
            state = state,
            here = here,
            upstream = upstream,
            departure = if (upstream == 0.0) abs(here) else abs(here / upstream - 1.0),
            source = source
        )
    }
    val arm = couplings.first { it.pathCount > 0 }
    C0069.forEach { (quantity, upstream, field) ->
        val here = when (field) {
            "length" -> arm.length
            "lengthBasePairs" -> arm.lengthBasePairs
            "secant" -> arm.secantAcceptable
            "tangent" -> arm.tangentAcceptable
            "minimum" -> arm.minimumTangentTraversed
            "root" -> arm.rootStiffness
            else -> arm.tipStiffness
        }
        add(quantity, "3 nm placement", here, upstream, "C-0069 / C-0055 / C-0034, CITED")
    }
    // C-0017's stability floor at 10 nm, re-derived here as |k_eff| at the located operating point
    listOf(0.5 to (3.86 to 15.94), 2.0 to (23.41 to 27.91)).forEach { (concentration, bracket) ->
        val rows = folds.filter {
            it.layerHeight == RECOMMENDED_LAYER_HEIGHT && it.concentration == concentration &&
                    it.loadLine.startsWith("L1")
        }.mapNotNull { it.stabilityFloor }
        if (rows.isNotEmpty()) {
            add(
                "C-0017 stability floor |k_eff(3 nm)| at 10 nm, six-model minimum [pN/nm]",
                "${concentration.roundedForProse()} mM", rows.min(), bracket.first, "C-0017"
            )
            add(
                "C-0017 stability floor |k_eff(3 nm)| at 10 nm, six-model maximum [pN/nm]",
                "${concentration.roundedForProse()} mM", rows.max(), bracket.second, "C-0017"
            )
        }
    }
    // C-0018's own coupled pull-in band at 10 nm / 2 mM, through this study's pipeline
    val baseline = folds.filter {
        it.layerHeight == RECOMMENDED_LAYER_HEIGHT && it.concentration == 2.0 &&
                it.loadLine.startsWith("L1")
    }
    val biases = baseline.mapNotNull { it.pullInBias }
    if (biases.isNotEmpty()) {
        add(
            "C-0018 pull-in bias at 10 nm / 2 mM, affine line, minimum [V]",
            "10 nm, 2 mM", biases.min(), 0.130, "C-0018 (0.130-0.184 V, 6 of 6 models)"
        )
        add(
            "C-0018 pull-in bias at 10 nm / 2 mM, affine line, maximum [V]",
            "10 nm, 2 mM", biases.max(), 0.184, "C-0018 (0.130-0.184 V, 6 of 6 models)"
        )
    }
    val strokes = baseline.mapNotNull { it.pullInStroke }
    if (strokes.isNotEmpty()) {
        add(
            "C-0032 affine fold STROKE at 10 nm / 2 mM, minimum [nm]",
            "10 nm, 2 mM", strokes.min(), 3.41, "C-0032 (3.41-4.13 nm on the affine line)"
        )
        add(
            "C-0032 affine fold STROKE at 10 nm / 2 mM, maximum [nm]",
            "10 nm, 2 mM", strokes.max(), 4.13, "C-0032 (3.41-4.13 nm on the affine line)"
        )
    }
    val margins = baseline.mapNotNull { it.biasMargin }
    if (margins.isNotEmpty()) {
        add(
            "C-0018 bias margin at 10 nm / 2 mM, affine line, minimum",
            "10 nm, 2 mM", margins.min(), 1.007, "C-0018 (1.007-1.032)"
        )
        add(
            "C-0018 bias margin at 10 nm / 2 mM, affine line, maximum",
            "10 nm, 2 mM", margins.max(), 1.032, "C-0018 (1.007-1.032)"
        )
    }
    // Q1 as an identity: both lines must locate the SAME operating bias, state by state
    folds.filter { it.loadLine.startsWith("L1") }.forEach { reference ->
        folds.filter {
            !it.loadLine.startsWith("L1") && it.model == reference.model &&
                    it.layerHeight == reference.layerHeight &&
                    it.concentration == reference.concentration
        }.forEach { other ->
            val here = other.operatingBias
            val there = reference.operatingBias
            if (here != null && there != null) add(
                "Q1 placement identity: V* on the recommended line against the mandate",
                ("%.0f nm, %.1f mM, %s").format(
                    reference.layerHeight, reference.concentration, reference.model
                ),
                here, there, "identity: both lines deliver 100 pN at 3 nm"
            )
        }
    }
    return checks
}

// ---------------------------------------------------------------------------------------------
// gate 4 — convergence
// ---------------------------------------------------------------------------------------------

@Suppress("LongParameterList")
private fun convergence(
    peg: PegWater,
    geometry: ActuatorGeometry,
    tileCharge: Double,
    bjerrum: Double,
    recommended: ElasticaArmLoadLine,
    elementCeiling: Double
): List<T149ConvergenceRecord> {
    val records = mutableListOf<T149ConvergenceRecord>()

    // axis 1 — the arm length, this task's own new element
    val finestArm = recommendedArmLine("finest", steps = 1600).length
    listOf(100, 200, 400, 800).forEach { steps ->
        val value = recommendedArmLine("coarse", steps = steps).length
        records += T149ConvergenceRecord(
            axis = "elastica RK4 steps",
            setting = steps.toString(),
            quantity = "placed arm length [nm]",
            value = value,
            departureFromFinest = abs(value / finestArm - 1.0)
        )
    }

    // axis 2 — the tangent minimum over the traversed range
    val finestMinimum = recommended.tangentMinimum(
        0.0, GEN1_ACCEPTABLE_STROKE, coarseSteps = 2048, tolerance = 1e-12
    ).stiffness
    listOf(64, 256, 1024).forEach { steps ->
        val value = recommended.tangentMinimum(
            0.0, GEN1_ACCEPTABLE_STROKE, coarseSteps = steps, tolerance = 1e-10
        ).stiffness
        records += T149ConvergenceRecord(
            axis = "tangent-minimum coarse scan steps",
            setting = steps.toString(),
            quantity = "min_s k_tangent over [0, 3] [pN/nm]",
            value = value,
            departureFromFinest = abs(value / finestMinimum - 1.0)
        )
    }

    // axes 3, 4, 5 — the fold, at a state where the RECOMMENDED line actually folds. That is not
    // C-0018's own binding state: the substitution removes the fold there, so a convergence axis
    // read on it would converge on `null` and report nothing. `CLAUDE.md`: convergence is a
    // property of the quantity, and a quantity that does not exist cannot be converged.
    val model = layerModels(peg).first { it.first == CONVERGENCE_MODEL }.second
    val chain = peg.graftedChain(
        model.chainLengthForHeight(peg, CONVERGENCE_HEIGHT, CONVERGENCE_DENSITY),
        CONVERGENCE_DENSITY
    )
    val balance = ActuatorForceBalance(model, chain, geometry)
    val resting = balance.restingHeight
    val ceiling = min(
        resting - max(chain.occupiedThickness * 1.01, CURVE_LOWEST_GAP), elementCeiling
    )

    fun locate(nodes: Int, coarse: Int, strokeTolerance: Double): Double? =
        EquilibriumPath(
            restingHeight = resting,
            strokeCeiling = ceiling,
            field = T149Field(CONVERGENCE_BUFFER, tileCharge, bjerrum, nodes).asPath()
        ) { stroke -> recommended.reaction(stroke) + balance.layerLoad(resting - stroke) }
            .fold(coarseSteps = coarse, strokeTolerance = strokeTolerance).pullInBias

    fun axis(name: String, quantity: String, settings: List<Pair<String, Double?>>) {
        val finest = settings.last().second ?: return
        settings.forEach { (label, value) ->
            if (value != null) records += T149ConvergenceRecord(
                axis = name,
                setting = label,
                quantity = quantity,
                value = value,
                departureFromFinest = abs(value / finest - 1.0)
            )
        }
    }
    val quantity = "pull-in bias on the recommended line at " +
            "$CONVERGENCE_HEIGHT nm / $CONVERGENCE_BUFFER mM, $CONVERGENCE_MODEL [V]"
    axis(
        "Poisson-Boltzmann mesh nodes", quantity,
        listOf(1000, 2000, 4000).map {
            it.toString() to locate(it, DEFAULT_COARSE_STEPS, DEFAULT_STROKE_TOLERANCE)
        }
    )
    axis(
        "fold coarse scan steps", quantity,
        listOf(8, 12, 24).map { it.toString() to locate(MESH_NODES, it, DEFAULT_STROKE_TOLERANCE) }
    )
    axis(
        "golden-section stroke bracket [nm]", quantity,
        listOf(1e-2, 1e-3, 1e-4, 1e-6).map {
            it.toString() to locate(MESH_NODES, DEFAULT_COARSE_STEPS, it)
        }
    )
    return records
}

// ---------------------------------------------------------------------------------------------
// findings, validity, and the report
// ---------------------------------------------------------------------------------------------

@Suppress("LongMethod", "CyclomaticComplexMethod")
private fun findings(result: RecommendedElementFoldResult): Map<String, String> {
    val findings = linkedMapOf<String, String>()
    result.couplings.forEach { record ->
        findings["${record.line}: placed / tangent / t-over-s / minimum tangent"] =
            ("secant %.4f, tangent %.4f, t/s %.4f, min %.4f pN/nm at %.4f nm (%s), " +
                    "strain-%s, clears %d of C-0017's six 2 mM floors")
                .format(
                    record.secantAcceptable, record.tangentAcceptable, record.tangentToSecant,
                    record.minimumTangentTraversed, record.minimumTangentTraversedStroke,
                    if (record.minimumTangentTraversedInterior) "interior" else "boundary",
                    if (record.strainStiffening) "STIFFENING" else "softening",
                    record.floorsClearedTwoMillimolar
                )
    }
    val arm = result.couplings.first { it.pathCount > 0 }
    findings["the recommended element's own stroke ceiling"] =
        ("refusal at %.4f nm, branch validity at %.4f nm (max|phi| = %.4f rad there, %.4f rad " +
                "at the 3 nm placement point), contour %.5f nm")
            .format(
                arm.refusalStrokeCeiling, arm.branchValidityStrokeCeiling,
                arm.rotationAtBranchCeiling, arm.rotationAtAcceptableStroke, arm.length
            )

    val placement = result.couplings.maxOf { it.placementResidual }
    findings["Q1 placement: worst relative departure of the assembled secant from 33.3333"] =
        "%.3e".format(placement)
    val identity = result.upstreamChecks.filter { it.quantity.startsWith("Q1 placement identity") }
    if (identity.isNotEmpty()) findings["Q1 placement identity: worst departure of V* between lines"] =
        ("%.3e over %d comparisons").format(identity.maxOf { it.departure }, identity.size)

    // Q2 — the cheap bound, and whether it agreed
    val graded = result.cheapBound.filter { it.predictionAgrees != null }
    findings["Q2 the cheap bound: states where a direction could be graded"] =
        "${graded.size} of ${result.cheapBound.size}"
    findings["Q2 the cheap bound: states where the SLOPE-term prediction agreed with the solve"] =
        "${graded.count { it.predictionAgrees == true }} of ${graded.size}"
    findings["Q2 the cheap bound: the slope term at the baseline fold"] =
        result.cheapBound.filter { it.solvedBaselineFoldStroke != null }.let { rows ->
            if (rows.isEmpty()) "no baseline fold anywhere" else
                ("delta k_c = %.3f to %.3f pN/nm; delta R = %.3f to %.3f pN")
                    .format(
                        rows.minOf { it.perturbation.tangentChange },
                        rows.maxOf { it.perturbation.tangentChange },
                        rows.minOf { it.perturbation.reactionChange },
                        rows.maxOf { it.perturbation.reactionChange }
                    )
        }
    findings["Q2 the cheap bound: the solved directions"] =
        result.cheapBound.groupingBy { it.solvedDirection }.eachCount().toString()

    result.folds.groupBy { it.loadLine }.forEach { (line, rows) ->
        findings["$line: states with a fold"] = "${rows.count { it.pullInBias != null }} of ${rows.size}"
        val margins = rows.mapNotNull { it.biasMargin }
        if (margins.isNotEmpty()) findings["$line: bias margin over the operating point"] =
            ("%.4f - %.4f").format(margins.min(), margins.max())
        findings["$line: verdicts"] = rows.groupingBy { it.verdict }.eachCount().toString()
        findings["$line: binding ceilings"] =
            rows.groupingBy { it.bindingCeiling ?: "none" }.eachCount().toString()
    }

    // Q3, Q4, Q5 — the devices, and the 10 nm ones first
    result.devices.filter { it.isTheRecommendedDevice }.forEach { device ->
        findings["Q3/Q4 ${device.device} — ${device.loadLine}"] =
            ("folds %d of %d, pull-in %s, fold stroke %s, bias margin %s, target on the stable " +
                    "side %d of %d — %s")
                .format(
                    device.statesWithAFold, device.models,
                    band(device.pullInBiasMinimum, device.pullInBiasMaximum, "V"),
                    band(device.foldStrokeMinimum, device.foldStrokeMaximum, "nm"),
                    band(device.biasMarginMinimum, device.biasMarginMaximum, ""),
                    device.targetStrokeOnStableSide, device.models, device.verdict
                )
    }

    // Q4 — what the element-model boundary is worth, at the headline state
    val headline = result.folds.filter {
        !it.loadLine.startsWith("L1") && it.layerHeight == RECOMMENDED_LAYER_HEIGHT &&
                it.concentration == 2.0
    }
    if (headline.isNotEmpty()) {
        val withBoundary = headline.mapNotNull { it.biasMargin }
        val without = headline.mapNotNull { it.biasMarginIgnoringElementBoundary }
        findings["Q4 the element-model boundary at 10 nm / 2 mM, both readings"] =
            ("with it %.4f - %.4f, without it %.4f - %.4f, over %d of %d states where it binds")
                .format(
                    withBoundary.min(), withBoundary.max(), without.min(), without.max(),
                    headline.count {
                        it.bindingCeiling?.startsWith("element model") == true
                    },
                    headline.size
                )
    }
    findings["states where the element-model branch end is the binding ceiling"] =
        "${result.folds.count { it.bindingCeiling?.startsWith("element model") == true }} " +
                "of ${result.folds.size}"

    val shallow = result.folds.count { it.targetStrokeOnStableSide == false }
    findings["Q3: states whose fold sits SHALLOWER than §3's 3 nm target stroke"] =
        "$shallow of ${result.folds.size}"

    val residuals = result.folds.mapNotNull { it.tangencyResidual }
    if (residuals.isNotEmpty()) findings["gate 3: tangency k_c(s) + k_eff = 0 at the located fold"] =
        ("worst relative residual %.3e over %d INTERIOR folds").format(residuals.max(), residuals.size)
    findings["folds at the branch start (a boundary maximum, no tangency to check)"] =
        "${result.folds.count { it.foldAtBranchStart }} of ${result.folds.size}"
    findings["branches that ended on the ELEMENT MODEL rather than on the field or a fold"] =
        "${result.folds.count { it.branchEndedOnTheElementModel }} of ${result.folds.size}"

    result.convergence.groupBy { it.axis }.forEach { (axis, rows) ->
        findings["convergence: $axis"] =
            rows.joinToString("; ") { ("${it.setting} -> %.2e").format(it.departureFromFinest) }
    }
    val worst = result.upstreamChecks.filterNot { it.quantity.startsWith("Q1 placement identity") }
        .maxByOrNull { it.departure }
    if (worst != null) findings["gate 5: worst upstream departure"] =
        ("%.3e on '%s' (%s)").format(worst.departure, worst.quantity, worst.source)
    return findings
}

private fun band(low: Double?, high: Double?, unit: String): String =
    if (low == null || high == null) "none"
    else ("%.4f - %.4f %s").format(low, high, unit).trim()

private val CONVENTIONS = listOf(
    "z is normal to the electrode, positive AWAY from it; the electrostatic gap IS the layer " +
            "height, exactly and by construction (C-0012)",
    "the STROKE s = L0 - h is positive DOWNWARD, toward the electrode",
    "L0 is a FORCE-ONSET height: the height at which the layer carries 1.0 pN over the " +
            "40 x 40 nm tile (C-0011, CH-0010)",
    "a LOAD LINE R(s) is positive UPWARD, in pN over the WHOLE array. C-0017's 33.3333 pN/nm is " +
            "a SUM, so the path count does not enter the load line at all - it enters the " +
            "per-path allowables, which C-0071 already discharges",
    "both load lines pass through the SAME operating point, 100 pN at 3 nm, and differ only in " +
            "how they leave it - which is what makes a state-by-state comparison of their folds " +
            "a comparison of one device rather than of two",
    "the equilibrium path is parametrised by the STROKE: at each stroke there is one bias " +
            "V_eq(s) that puts an equilibrium there, and the fold is max_s V_eq(s). " +
            "Differentiating the balance at V'(s) = 0 gives k_c(s) + k_eff(s) = 0 exactly, so " +
            "the argmax IS the tangency point - and for a NONLINEAR line k_c is the tangent AT " +
            "THAT STROKE and not a constant",
    "a DEVICE is one buffer, one layer, one bias and L0 -> L0 - s* (C-0064, C-0068). States " +
            "belonging to different devices are NOT intersected. C-0071's device is the 10 nm " +
            "layer, because C-0068 shows the layer selects the crossover phase and 24 is the " +
            "10 nm one",
    "a requirement on a coupling law is owed over [0, s*], the strokes the device TRAVERSES " +
            "(C-0049), and s* = 3 nm because C-0071 is written at §3's ACCEPTABLE clause",
    "k_es = -dF_z/dh is NEGATIVE above the force maximum and POSITIVE below it (CH-0011)",
    "a bias ceiling belongs to a (bias, load line) pair, never to the bias alone (CH-0015), and " +
            "a fold is quoted with BOTH its bias and its stroke, because a nonlinear load line " +
            "moves a fold in its stroke far more than in its bias (C-0032)",
    "an INEXTENSIBLE arm has a kinematic ceiling on the stroke, and C-0039's shooting solve " +
            "enumerates only the small-rotation branch, so it refuses below the contour. A " +
            "branch that ends there has no fold INSIDE THE ELEMENT MODEL'S OWN RANGE - a model " +
            "boundary, reported as one",
    "where k_eff >= 0 there is NO stability requirement at all; the margin is recorded as null, " +
            "not as an infinity (CLAUDE.md)"
)

private val CITED = listOf(
    "C-0017's mandated output-coupling stiffness, 100 pN / 3 nm = 33.3333 pN/nm - CITED, itself " +
            "derived there from §3 alone. It is the placement target both lines are solved to",
    "C-0017's stability floors at the 10 nm design point, 23.41-27.91 pN/nm at 2 mM and " +
            "3.86-15.94 at 0.5 mM - CITED, and re-derived here as a gate-5 check",
    "C-0018's pull-in band at 10 nm / 2 mM, 0.130-0.184 V, and its bias margin 1.007-1.032 - " +
            "CITED, and reproduced here through this study's own pipeline on the affine line",
    "C-0032's affine fold stroke at 10 nm / 2 mM, 3.41-4.13 nm, and its softening-line readings " +
            "(2.80-3.17 nm, margin 1.0000-1.0019) - CITED. The softening line is NOT re-run here",
    "C-0069's Q5 row: arm 8.16439083 nm = 24.0129142 bp, assembled secant 33.3333333, tangent " +
            "40.8120233, tangent minimum 30.028762 - CITED, and every one re-derived here from " +
            "C-0039's and C-0034's own libraries rather than read from a result file",
    "C-0055's one antiparallel crossover, 13.5294118 pN nm/rad, and C-0034's A2 duplex-end " +
            "anchorage, 78.2352941 pN nm/rad - CITED",
    "C-0002's semidilute-to-concentrated crossover phi = 0.2 - CITED, read as a ceiling",
    "CH-0007's point-ion boundary in APPLIED bias, ~1.0 V - CITED",
    "C-0005's one-loop correction, 123-214% of the leading term - CITED. It is larger than " +
            "every margin in this file",
    "the Stern capacitance ~20 uF/cm2 - CITED, and load-bearing for the diffuse-drop to " +
            "applied-bias mapping",
    "Manning surviving fraction 11.90% - CITED FROM C-0005 via C-0008",
    "duplex EI = 230 pN nm2 (CanDo MODEL INPUT, not a measurement) - CITED via C-0009/Gen1Tile",
    "A2, A3, alpha = 1.9e-3, 2.0e-2, 0.49 - CITED FROM C-0003/C-0002",
    "§3's targets: 100 pN, 3 nm acceptable, 10 nm desired, 40 x 40 nm tile, 5/7/10 nm layer - CITED"
)

private val VALIDITY = listOf(
    "TRL 1-3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and the MOTIF is not " +
            "demonstrated either: C-0055's literature finding stands unchanged, and it is " +
            "upstream of the element itself.",
    "Mean field, inherited whole from C-0008 and C-0005: the one-loop correction is 123-214% of " +
            "the leading term over the whole 5-10 nm range for Mg2+. Every margin here is " +
            "smaller than that.",
    "L0 is a FORCE-ONSET height at a defining load of 1.0 pN over the tile (C-0011, CH-0010).",
    "The layer is C-0003's, at C-0001's single grafting density per height - NOT C-0011's solved " +
            "SCF profile. Deliberate, and the same choice C-0017, C-0018 and C-0032 made: the " +
            "load line must be drawn across the same characteristic C-0012 computed.",
    "The RECOMMENDED DEVICE is the 10 nm layer. The 5 nm and 7 nm rows are computed for coverage " +
            "parity with C-0018 and C-0032 and are NOT this element's device: C-0068 shows the " +
            "layer selects the crossover phase, and the 34-root placement is phase 24, the 10 nm " +
            "one. They must not be intersected with the 10 nm rows (C-0064).",
    "The element model's own branch: C-0039's shooting solve enumerates only the small-rotation " +
            "branch and the arm is inextensible, so the equilibrium path is truncated at the " +
            "element's own ceiling wherever that is tighter than the layer's. A branch that ends " +
            "there is reported as ending on the ELEMENT MODEL and is NOT a demonstration that no " +
            "fold exists at a deeper stroke.",
    "The load line is the tile MEAN under a uniform load. A real 34-attachment coupling dishes " +
            "the tile (C-0063, C-0068).",
    "Static only. A bias step faster than drainage can carry the tile past a fold a " +
            "quasi-static ramp stops at; C-0004's corner is 91 kHz-2.3 MHz.",
    "No preload (T-13).",
    "1-D. No edge, no fringing, no lateral load profile. C-0033's collar correction at the fold " +
            "is a SEPARATE composition and is not carried here, exactly as C-0018 and C-0032 did " +
            "not carry it.",
    "The diffuse-layer drop is capped at 0.35 V, the same bracket C-0008's own Stern inversion " +
            "uses. A state needing more is reported as a branch end, not extrapolated."
)

private val OPEN = listOf(
    "The 0.5 mM recommendation is a SPECIFICATION question and not a calculation (T-63). This " +
            "task supplies a seventh route to it, on the recommended element rather than on a " +
            "withdrawn one, and cannot answer it.",
    "C-0033's collar correction composes EXACTLY at a fold and is not composed here. Its sign at " +
            "the affine fold was +2.60 to +4.99 pN/nm, i.e. deeper; whether it stays so at this " +
            "element's fold gap is one evaluation per state and is not done.",
    "The dynamic pull-in is not computed, and a stiffening coupling has a different dynamic " +
            "signature from a softening one.",
    "C-0019's one-loop softening of k_brush and C-0022's finite-tile enhancement of |F_es| are " +
            "NOT carried here, exactly as C-0018 and C-0032 did not carry them.",
    "The arm's LARGE-rotation branch is not enumerated. Where the path is truncated by the " +
            "element model the question of whether a deeper fold exists is open, and it needs a " +
            "multi-branch elastica rather than a shooting solve."
)

private fun report(result: RecommendedElementFoldResult, output: File) {
    println()
    println("T-149 — ${result.title}")
    println(
        "  ${result.couplings.size} coupling records, ${result.cheapBound.size} cheap-bound " +
                "records, ${result.folds.size} fold records, ${result.devices.size} devices, " +
                "${result.upstreamChecks.size} upstream checks, " +
                "${result.convergence.size} convergence"
    )
    result.findings.forEach { (key, value) -> println("  $key: $value") }
    println("written to $output")
}
