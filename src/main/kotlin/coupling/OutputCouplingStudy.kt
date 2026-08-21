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

package com.xemantic.nano.plentyofroom.coupling

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.actuator.ActuatorForceBalance
import com.xemantic.nano.plentyofroom.actuator.ActuatorGeometry
import com.xemantic.nano.plentyofroom.actuator.ElectrostaticForceCurve
import com.xemantic.nano.plentyofroom.actuator.attractiveForceCurve
import com.xemantic.nano.plentyofroom.actuator.gradedGapGrid
import com.xemantic.nano.plentyofroom.anchoring.AnchorMaterials
import com.xemantic.nano.plentyofroom.anchoring.BeamEndCondition
import com.xemantic.nano.plentyofroom.anchoring.FreelyJointedChain
import com.xemantic.nano.plentyofroom.anchoring.SsDnaTether
import com.xemantic.nano.plentyofroom.anchoring.beamTransverseStiffness
import com.xemantic.nano.plentyofroom.anchoring.bundleBendingRigidity
import com.xemantic.nano.plentyofroom.anchoring.rodAxialStiffness
import com.xemantic.nano.plentyofroom.brush.AlexanderBoxLayer
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
import com.xemantic.nano.plentyofroom.electrostatics.DEFAULT_GAP_MESH_NODES
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
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.abs
import kotlin.math.max

/**
 * Task `T-16` — the minimum output-coupling stiffness, and what a DNA-origami lever can
 * actually deliver. Leaf `A8.2`.
 *
 * Run with:
 *
 * ```shell
 * ./gradlew study -Pstudy=coupling.OutputCouplingStudyKt -PbuildDirectory=build-t16
 * ```
 *
 * Emits `gpd/results/T-16-output-coupling-stiffness.json`, deterministically — no timestamp,
 * every floating-point number rounded at the serialisation boundary per [roundCouplingResult].
 *
 * Consumes `C-0012`'s pipeline (`actuator/`, `electrostatics/`, `brush/`, `material/`) and
 * `C-0014`'s element mechanics (`anchoring/`) as **libraries, re-run rather than tabulated**.
 * Owns nothing outside `coupling/`.
 */

// ---------------------------------------------------------------------------------------------
// records
// ---------------------------------------------------------------------------------------------

/** The coupling requirement at one `(layer height, model, buffer)` state. */
@Serializable
data class CouplingRequirementRecord(
    val model: String,
    val layerHeight: Double,
    val graftingDensity: Double,
    val concentration: Double,
    val restingHeight: Double,
    val heldGap: Double,
    val layerLoadAtHeldGap: Double,
    val targetElectrostaticForce: Double,
    val simultaneousTargetBias: Double?,
    val simultaneousTargetBiasInC0012: Double?,
    val simultaneousTargetBiasBracketInC0012: String,
    val simultaneousTargetBiasDeparture: Double?,
    val withinTrustedBias: Boolean,
    val electrostaticForceAtTarget: Double?,
    val outputForceAtTarget: Double?,
    val brushStiffnessAtHeldGap: Double,
    val electrostaticStiffnessAtTarget: Double?,
    val effectiveStiffnessAtTarget: Double?,
    val forceDecayLengthAtTarget: Double?,
    val stabilityFloor: Double?,
    val mandatedStiffness: Double,
    val stabilityMargin: Double?,
    val mandatedStiffnessIsStable: Boolean?,
    val unpreloadedPlacementStiffness: Double?,
    val unpreloadedWindowIsEmpty: Boolean?,
    val closedFormMargin: Double?,
    val closedFormDeparture: Double?,
    val strokeAgainstMandatedCoupling: Double?,
    val freeStrokeAtTargetBias: Double?,
    val heldVolumeFraction: Double,
    val heldGapAboveCorrelationBand: Boolean,
    val heldVolumeFractionBelowCrossover: Boolean
)

/** `C-0012`'s own grid biases, reproduced — the gate-5 record. */
@Serializable
data class UpstreamReproductionRecord(
    val model: String,
    val layerHeight: Double,
    val concentration: Double,
    val appliedBias: Double,
    val blockingForceHere: Double,
    val blockingForceInC0012: Double,
    val blockingForceDeparture: Double,
    val outputForceAtThreeNanometresHere: Double,
    val outputForceAtThreeNanometresInC0012: Double,
    val outputForceDeparture: Double,
    val effectiveStiffnessHere: Double,
    val effectiveStiffnessInC0012: Double,
    val effectiveStiffnessDeparture: Double
)

/** One candidate coupling, evaluated at one solved actuator state. */
@Serializable
data class SchemeRecord(
    val scheme: String,
    val model: String,
    val layerHeight: Double,
    val concentration: Double,
    val appliedBias: Double,
    val verdict: CouplingSchemeVerdict
)

/** The lever's own section requirement — leaf `A8.2`'s stiffness budget. */
@Serializable
data class LeverBudgetRecord(
    val support: String,
    val span: Double,
    val requiredStiffness: Double,
    val requiredBendingRigidity: Double,
    val duplexLayersNeeded: Int?,
    val blockThickness: Double?,
    val fitsInsideTheTileThickness: Boolean,
    val rigidityOfFourHelixBundle: Double,
    val stiffnessOfFourHelixBundle: Double
)

/** The spacer design: contour length against the count it is spread over. */
@Serializable
data class SpacerDesignRecord(
    val kuhnLengthConvention: String,
    val kuhnLength: Double,
    val attachmentCount: Int,
    val standoffStiffness: Double,
    val requiredPerPathStiffness: Double,
    val spacerContourLength: Double,
    val spacerNucleotides: Double,
    val perPathTension: Double,
    val couplingSecantStiffness: Double,
    val couplingTangentStiffness: Double,
    val lateralStiffness: Double,
    val lateralOverBound: Double,
    val yawStiffness: Double,
    val yawOverBound: Double,
    val perPathThermalForce: Double
)

/** Convergence of the whole pipeline — gate 4, emitted rather than only asserted. */
@Serializable
data class CouplingConvergenceRecord(
    val quantity: String,
    val setting: String,
    val samples: Int,
    val meshNodes: Int,
    val simultaneousTargetBias: Double,
    val effectiveStiffnessAtTarget: Double,
    val stabilityMargin: Double,
    val relativeDeparture: Double
)

@Serializable
data class OutputCouplingResult(
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
    val requirements: List<CouplingRequirementRecord>,
    val upstreamReproduction: List<UpstreamReproductionRecord>,
    val schemes: List<SchemeRecord>,
    val leverBudget: List<LeverBudgetRecord>,
    val spacerDesign: List<SpacerDesignRecord>,
    val convergence: List<CouplingConvergenceRecord>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

// ---------------------------------------------------------------------------------------------
// the sweep
// ---------------------------------------------------------------------------------------------

/** §3's three layer heights with `C-0001`'s grafting densities — `C-0012`'s own design points. */
private val DESIGN_POINTS = listOf(5.0 to 0.092, 7.0 to 0.045, 10.0 to 0.024)

/**
 * §3's lowest buffer and leaf `A2.2`'s low-screening operating point.
 *
 * `C-0012` establishes that the buffer sets the **force** and barely touches the layer, so the
 * coupling requirement moves with it through `F_es` alone. 5 and 10 mM are omitted because at
 * 7 and 10 nm the §3 force target is unreachable there at any bias (`C-0012`), so there is no
 * operating point for a coupling to be sized at.
 */
private val BUFFERS = listOf(0.5, 1.0, 2.0)

private const val TARGET_FORCE = 100.0

private const val TARGET_STROKE = 3.0

private const val BIAS_CEILING = 2.0

/** `CH-0007`'s point-ion boundary **in applied bias**, not in diffuse-layer drop. */
private const val TRUSTED_BIAS_CEILING = 1.0

/** `C-0005`'s lateral counterion spacing — below this gap PB cannot produce the physics. */
private const val CORRELATION_ATTRACTION_GAP = 1.46

/** `C-0002`'s semidilute→concentrated crossover, read as a ceiling per §2's second caveat. */
private const val CONCENTRATED_CROSSOVER = 0.2

private const val STERN_CAPACITANCE = 20.0

private const val SEARCH_NODES = 400

private const val CURVE_SAMPLES = 72

/**
 * Steps in the downward scan for the first crossing of the load line.
 *
 * `C-0012`'s own value. Only the **bracket** depends on it — the root is then bisected to the
 * double-precision floor — and `C-0012` shows 500 and 16 000 give the same operating height to
 * `1e-9`. It is kept modest because a strong-stretching evaluation of `P(h)` costs a
 * Newton-solved profile quadrature, and this task takes eight scans per solved state.
 */
private const val SCAN_STEPS = 600

private const val CURVE_LOWEST_GAP = 0.5

private const val CURVE_HIGHEST_GAP = 11.5

private const val OSMOTIC_SECOND_VIRIAL = 1.9e-3

private const val OSMOTIC_THIRD_VIRIAL = 2.0e-2

/** `C-0014`'s per-coordinate lateral bound in `pN/nm` — **CITED**, leaf `A1.1`'s own table. */
private const val LATERAL_BOUND = 0.460216

/** `C-0014`'s yaw bound in `pN·nm/rad`, budgeted at the tile's **corner** — **CITED**. */
private const val YAW_BOUND = 368.173

/** `C-0009`'s worst-case out-of-plane load concentration at a discrete anchor — **CITED**. */
private const val CONCENTRATION_FACTOR = 7.6

/** `C-0015`'s flatness scheme: 45 attachments as 3 × 15, one row per duplex — **CITED**. */
private const val FLATNESS_COLUMNS = 3

private const val FLATNESS_ROWS = 15

/** The tile, in `C-0015`'s own dimensions: 15 duplexes at the measured 2.69 nm. */
private const val TILE_EDGE_X = 40.0

private const val TILE_EDGE_Y = 40.35

/** The standoff between the tile's top face and the lever, per `ActuatorGeometry`. */
private const val STANDOFF_LENGTH = 5.0

private class Sampler(
    val tileCharge: Double,
    val bjerrumLength: Double,
    val ionModel: IonModel
) {

    private val medium = uniformMedium(GapMedium())

    val stern: Double = sternChargeDensityPerVolt(STERN_CAPACITANCE)

    fun diffusePotential(gap: Double, bias: Double): Double = diffusePotentialOfAppliedBias(
        gap, bias, tileCharge, stern, ionModel, medium, bjerrumLength, nodes = SEARCH_NODES
    )

    fun force(gap: Double, bias: Double, nodes: Int = DEFAULT_GAP_MESH_NODES): Double =
        PoissonBoltzmannGap(gap, ionModel, medium, bjerrumLength, nodes = nodes)
            .solve(diffusePotential(gap, bias) / thermalVoltage(), tileCharge)
            .forceOnTile(TILE_EDGE_X * TILE_EDGE_X)

    fun curve(bias: Double, samples: Int = CURVE_SAMPLES, nodes: Int = DEFAULT_GAP_MESH_NODES) =
        attractiveForceCurve(gradedGapGrid(CURVE_LOWEST_GAP, CURVE_HIGHEST_GAP, samples)) {
            force(it, bias, nodes)
        }.curve
}

private fun sampler(concentration: Double, tileCharge: Double, bjerrum: Double) =
    Sampler(tileCharge, bjerrum, IonModel(MagnesiumChlorideBuffer(concentration).magnesiumNumberDensity))

/**
 * The bias at which `|F_es(gap, V)| = target`, bisected on the **bracket width**.
 *
 * `|F_es|` is monotone increasing in bias and saturating, so a bracket is all that is needed;
 * `null` means the target is unreachable at or below [ceiling], which is `C-0012`'s own
 * finding at 7 and 10 nm in 10 mM buffer.
 */
private fun biasForForce(
    sampler: Sampler,
    gap: Double,
    target: Double,
    ceiling: Double = BIAS_CEILING,
    nodes: Int = DEFAULT_GAP_MESH_NODES
): Double? {
    var low = 1e-4
    var high = ceiling
    if (abs(sampler.force(gap, high, nodes)) < target) return null
    if (abs(sampler.force(gap, low, nodes)) > target) return low
    repeat(200) {
        val middle = 0.5 * (low + high)
        if (abs(sampler.force(gap, middle, nodes)) < target) low = middle else high = middle
        if (high - low <= 1e-12 * high) return 0.5 * (low + high)
    }
    return 0.5 * (low + high)
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

fun main() {
    val peg = PegWater()
    val geometry = ActuatorGeometry()
    val tile = DnaOrigamiTile()
    val lb = bjerrumLength()
    val surviving = tile.manningSurvivingFraction(2, lb)
    val tileCharge = -(tile.projectedChargeDensity * surviving / 2.0)
    val models = layerModels(peg)
    val mandated = mandatedCouplingStiffness(TARGET_FORCE, TARGET_STROKE)
    val grid = attachmentGrid(FLATNESS_COLUMNS, FLATNESS_ROWS, TILE_EDGE_X, TILE_EDGE_Y)
    val upstreamThresholds = readCoupledThresholds(
        File("gpd/results/T-3-stroke-and-blocking-force.json")
    )

    println("T-16 — locating the bias that delivers ${TARGET_FORCE} pN at a ${TARGET_STROKE} nm stroke ...")
    val requirements = mutableListOf<CouplingRequirementRecord>()
    val schemeRecords = mutableListOf<SchemeRecord>()
    val samplers = BUFFERS.associateWith { sampler(it, tileCharge, lb) }

    DESIGN_POINTS.forEach { (height, density) ->
        models.forEach { (name, model) ->
            val chain = peg.graftedChain(model.chainLengthForHeight(peg, height, density), density)
            val balance = ActuatorForceBalance(model, chain, geometry)
            val resting = balance.restingHeight
            val heldGap = resting - TARGET_STROKE
            val layerLoad = balance.layerLoad(heldGap)
            val brush = balance.layerStiffness(heldGap)
            val target = TARGET_FORCE + layerLoad
            BUFFERS.forEach { concentration ->
                val field = samplers.getValue(concentration)
                val bias = biasForForce(field, heldGap, target)
                val curve = bias?.let { field.curve(it) }
                val upstream = upstreamThresholds.firstOrNull {
                    it.model == name && it.layerHeight == height &&
                            it.concentration == concentration
                }
                val record = requirement(
                    name, height, density, concentration, balance, chain,
                    heldGap, layerLoad, brush, target, bias, curve, mandated, upstream
                )
                requirements += record
                if (curve != null && bias != null) {
                    schemeRecords += schemes(
                        name, height, concentration, bias, balance, curve, record, grid, mandated
                    )
                }
            }
        }
    }

    println("T-16 — reproducing C-0012 at its own grid biases ...")
    val reproduction = reproduce(peg, geometry, models, tileCharge, lb)

    println("T-16 — the lever's section requirement and the spacer design ...")
    val leverBudget = leverBudget(mandated)
    val spacerDesign = spacerDesign(grid, mandated)

    println("T-16 — convergence ...")
    val convergence = convergence(peg, geometry, tileCharge, lb, mandated)

    val result = OutputCouplingResult(
        task = "T-16",
        leaf = "A8.2",
        title = "The minimum output-coupling stiffness of the Gen-1 actuator, read at the bias " +
                "that delivers §3's own targets, and what a DNA-origami lever can deliver " +
                "against it",
        verificationType = "in-silico (C-0012's coupled force balance re-solved against a LOAD " +
                "LINE rather than against zero, on a bias located by bisection rather than " +
                "read off a grid) + logical (a load-line argument fixing the required " +
                "stiffness from §3 alone, before any solve)",
        acceptance = "P1: at every (layer height, layer model, buffer) state, report " +
                "|k_eff(L0 - 3 nm)| at the bias where W(3 nm) = 100 pN, and the coupling " +
                "window it defines. P2: identify a DNA-origami output coupling that supplies " +
                "that stiffness at the tile, with its per-load-path force against C-0006's " +
                "10 / 48 / 65 pN allowables, its attachment count against C-0015's 45, and the " +
                "stroke it actually delivers computed as a ROOT of W(s) = R(s) — or show that " +
                "none does and name the binding constraint. P3: report whether normal " +
                "stabilisation and C-0014's lateral confinement want the same anchors or " +
                "opposite ones. P4: state whether the answer rests on the crossover's axial " +
                "compliance, which C-0009 models as a rigid constraint and T-9 has not produced.",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated. No " +
                "coupling here has been built, and none is proposed as a sequence design. " +
                "Every force inherits C-0008's mean-field statement in full: C-0005 puts the " +
                "one-loop correction at 123-214% of the leading term across this gap range, " +
                "which is one to two orders of magnitude larger than the stability margin " +
                "this task reports. The verdict is therefore NOT EXCLUDED, never established.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm (= 1 mN/m exactly)",
            "rotationalStiffness" to "pN*nm/rad",
            "bendingRigidity" to "pN*nm^2",
            "energy" to "pN*nm",
            "potential" to "V",
            "concentration" to "mM",
            "temperature" to "K"
        ),
        conventions = listOf(
            "z is normal to the electrode, positive AWAY from it; the electrode surface is z = 0",
            "the electrostatic gap IS the layer height, exactly and by construction (C-0012)",
            "the STROKE s = L0 - h is positive DOWNWARD, toward the electrode",
            "L0 is a FORCE-ONSET height: the height at which the layer carries 1.0 pN over the " +
                    "40 x 40 nm tile (C-0011, CH-0010). The first-moment thickness of the same " +
                    "layer is 1.71-2.16x smaller",
            "the actuator characteristic is W(s) = |F_es(L0-s, V)| - P(L0-s) A and dW/ds = -k_eff",
            "the COUPLING REACTION R(s) is positive UPWARD, i.e. resisting the tile's motion " +
                    "toward the electrode. A coupling supplies stabilising stiffness only " +
                    "through dR/ds > 0; an element that goes slack as the tile descends " +
                    "supplies exactly nothing",
            "the operating point is the FIRST root of W(s) = R(s), and it is stable iff " +
                    "k_c = dR/ds exceeds |k_eff| there. Those are TWO conditions and C-0012 " +
                    "states one of them",
            "the force delivered to the load over a stroke is k_c times that stroke, " +
                    "INDEPENDENT of any preload, which is why §3's 100 pN and 3 nm fix the " +
                    "coupling stiffness at 100/3 = 33.333 pN/nm by arithmetic alone"
        ),
        runParameters = mapOf(
            "temperature" to ROOM_TEMPERATURE.toString(),
            "thermalEnergy" to thermalEnergy().toString(),
            "bjerrumLength" to lb.roundedForProse().toString(),
            "tileEdgeX" to TILE_EDGE_X.toString(),
            "tileEdgeY" to TILE_EDGE_Y.toString(),
            "footprintArea" to geometry.footprintArea.toString(),
            "standoffLength" to STANDOFF_LENGTH.toString(),
            "manningSurvivingFraction" to surviving.roundedForProse().toString(),
            "nominalTileChargeDensity" to tileCharge.roundedForProse().toString(),
            "sternCapacitance" to STERN_CAPACITANCE.toString(),
            "meshNodes" to DEFAULT_GAP_MESH_NODES.toString(),
            "biasSearchNodes" to SEARCH_NODES.toString(),
            "forceCurveSamples" to CURVE_SAMPLES.toString(),
            "forceCurveGapRange" to "[$CURVE_LOWEST_GAP, $CURVE_HIGHEST_GAP]",
            "layerHeights" to DESIGN_POINTS.map { it.first }.toString(),
            "graftingDensities" to DESIGN_POINTS.map { it.second }.toString(),
            "buffers" to BUFFERS.toString(),
            "targetForce" to TARGET_FORCE.toString(),
            "targetStroke" to TARGET_STROKE.toString(),
            "mandatedCouplingStiffness" to mandated.roundedForProse().toString(),
            "biasCeiling" to BIAS_CEILING.toString(),
            "trustedBiasCeiling" to TRUSTED_BIAS_CEILING.toString(),
            "flatnessAttachments" to (FLATNESS_COLUMNS * FLATNESS_ROWS).toString(),
            "flatnessGridShape" to "$FLATNESS_COLUMNS x $FLATNESS_ROWS",
            "lateralBound" to LATERAL_BOUND.toString(),
            "yawBound" to YAW_BOUND.toString(),
            "concentrationFactor" to CONCENTRATION_FACTOR.toString()
        ),
        citedInputs = CITED,
        requirements = requirements,
        upstreamReproduction = reproduction,
        schemes = schemeRecords,
        leverBudget = leverBudget,
        spacerDesign = spacerDesign,
        convergence = convergence,
        findings = emptyMap(),
        validity = VALIDITY,
        openQuestions = OPEN
    )
    val complete = result.copy(findings = findings(result, mandated))
    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-16-output-coupling-stiffness.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(json.encodeToJsonElement(complete).roundedForCouplingResult()) + "\n"
    )
    report(complete, output, mandated)
}

// ---------------------------------------------------------------------------------------------
// the requirement
// ---------------------------------------------------------------------------------------------

@Suppress("LongParameterList")
private fun requirement(
    name: String,
    height: Double,
    density: Double,
    concentration: Double,
    balance: ActuatorForceBalance,
    chain: com.xemantic.nano.plentyofroom.brush.GraftedChain,
    heldGap: Double,
    layerLoad: Double,
    brush: Double,
    target: Double,
    bias: Double?,
    curve: ElectrostaticForceCurve?,
    mandated: Double,
    upstream: CoupledThreshold?
): CouplingRequirementRecord {
    val magnitude = curve?.magnitudeAt(heldGap)
    val electrostatic = curve?.stiffnessAt(heldGap)
    val decay = curve?.decayLengthAt(heldGap)
    val effective = electrostatic?.let { brush + it }
    val output = magnitude?.let { it - layerLoad }
    val window = if (effective != null && output != null) CouplingWindow(
        targetStroke = TARGET_STROKE,
        outputForceAtTarget = output,
        effectiveStiffnessAtTarget = effective,
        mandatedStiffness = mandated
    ) else null
    // the closed form of the Plan: the margin is bias-free once §3's force target is imposed
    val closedForm = if (magnitude != null && decay != null) {
        mandated + brush - magnitude / decay
    } else null
    val departure = if (closedForm != null && effective != null) {
        val direct = mandated + effective
        abs(closedForm - direct) / max(abs(direct), 1e-12)
    } else null
    val characteristic = if (curve != null) OutputCharacteristic {
        balance.outputForce(curve, balance.restingHeight - it)
    } else null
    val ceiling = balance.restingHeight - max(balance.dryThickness * 1.01, CURVE_LOWEST_GAP)
    val strokeAgainstMandated = characteristic?.let {
        firstOperatingStroke(it, LinearCoupling(mandated), ceiling, SCAN_STEPS)
    }
    val free = characteristic?.let {
        firstOperatingStroke(it, LinearCoupling(0.0), ceiling, SCAN_STEPS)
    }
    return CouplingRequirementRecord(
        model = name,
        layerHeight = height,
        graftingDensity = density,
        concentration = concentration,
        restingHeight = balance.restingHeight,
        heldGap = heldGap,
        layerLoadAtHeldGap = layerLoad,
        targetElectrostaticForce = target,
        simultaneousTargetBias = bias,
        simultaneousTargetBiasInC0012 = upstream?.biasForSimultaneousTarget,
        simultaneousTargetBiasBracketInC0012 =
            upstream?.biasBracketForSimultaneousTarget ?: "not in C-0012's sweep",
        simultaneousTargetBiasDeparture =
            if (bias != null && upstream?.biasForSimultaneousTarget != null)
                abs(bias / upstream.biasForSimultaneousTarget - 1.0) else null,
        withinTrustedBias = (bias ?: 0.0) <= TRUSTED_BIAS_CEILING,
        electrostaticForceAtTarget = magnitude,
        outputForceAtTarget = output,
        brushStiffnessAtHeldGap = brush,
        electrostaticStiffnessAtTarget = electrostatic,
        effectiveStiffnessAtTarget = effective,
        forceDecayLengthAtTarget = decay,
        stabilityFloor = window?.stabilityFloor,
        mandatedStiffness = mandated,
        // infinite where the point is already stable without a coupling; JSON has no
        // representation for that, and `null` is the honest one — "no floor to clear"
        stabilityMargin = window?.stabilityMargin?.takeIf { it.isFinite() },
        mandatedStiffnessIsStable = window?.mandatedStiffnessIsStable,
        unpreloadedPlacementStiffness = window?.unpreloadedPlacementStiffness,
        unpreloadedWindowIsEmpty = window?.unpreloadedWindowIsEmpty,
        closedFormMargin = closedForm,
        closedFormDeparture = departure,
        strokeAgainstMandatedCoupling = strokeAgainstMandated,
        freeStrokeAtTargetBias = free,
        heldVolumeFraction = chain.meanVolumeFraction(heldGap),
        heldGapAboveCorrelationBand = heldGap >= CORRELATION_ATTRACTION_GAP,
        heldVolumeFractionBelowCrossover =
            chain.meanVolumeFraction(heldGap) <= CONCENTRATED_CROSSOVER
    )
}

// ---------------------------------------------------------------------------------------------
// the schemes
// ---------------------------------------------------------------------------------------------

/** The design spacer's contour, tuned so the whole coupling lands on §3's mandated stiffness. */
private fun designedSpacer(
    kuhn: Double,
    count: Int,
    standoff: Double
): FreelyJointedChain {
    val perPathTension = TARGET_FORCE / count
    val standoffExtension = perPathTension / standoff
    return FreelyJointedChain(
        spacerContourForTarget(kuhn, count, TARGET_FORCE, TARGET_STROKE - standoffExtension),
        kuhn
    )
}

@Suppress("LongParameterList")
private fun schemes(
    name: String,
    height: Double,
    concentration: Double,
    bias: Double,
    balance: ActuatorForceBalance,
    curve: ElectrostaticForceCurve,
    requirement: CouplingRequirementRecord,
    grid: List<Pair<Double, Double>>,
    mandated: Double
): List<SchemeRecord> {
    val characteristic = OutputCharacteristic {
        balance.outputForce(curve, balance.restingHeight - it)
    }
    val ceiling = balance.restingHeight - max(balance.dryThickness * 1.01, CURVE_LOWEST_GAP)
    val standoff = rodAxialStiffness(AnchorMaterials.DUPLEX_STRETCH_MODULUS, STANDOFF_LENGTH)
    val count = grid.size
    val spacer = designedSpacer(SsDnaTether.KUHN_LENGTH_ZERO_FORCE, count, standoff)
    val perPathTension = TARGET_FORCE / count
    val spacerTangent = spacer.tangentStiffness(perPathTension)
    val spacerSecant = spacer.transverseStiffness(perPathTension)
    val standoffTransverse = beamTransverseStiffness(
        AnchorMaterials.CANDO_BENDING_RIGIDITY, STANDOFF_LENGTH, BeamEndCondition.GUIDED_HEAD
    )
    val strutRigidity = bundleBendingRigidity(
        List(1) { 0.0 }, AnchorMaterials.CANDO_BENDING_RIGIDITY,
        AnchorMaterials.DUPLEX_STRETCH_MODULUS
    )
    val throughLayer = FreelyJointedChain(51.7, SsDnaTether.KUHN_LENGTH_ZERO_FORCE)

    val candidates = listOf(
        Triple(
            CouplingScheme(
                name = "K1 — 45 hybridised duplex standoffs (5 nm) to a rigid lever, no spacer",
                attachmentCount = count,
                path = listOf(CouplingPathElement("duplex standoff, axial", standoff)),
                loadPathCrossesLattice = false,
                reusesFlatnessAttachments = true
            ),
            LinearCoupling(count * standoff) as CouplingReaction,
            count * standoffTransverse
        ),
        Triple(
            CouplingScheme(
                name = "K2 — 45 (duplex standoff + tuned ssDNA spacer) to a rigid lever",
                attachmentCount = count,
                path = listOf(
                    CouplingPathElement("duplex standoff, axial", standoff),
                    CouplingPathElement("ssDNA spacer, tangent at working tension", spacerTangent)
                ),
                loadPathCrossesLattice = false,
                reusesFlatnessAttachments = true
            ),
            SeriesEntropicCoupling(count, standoff, spacer),
            count * seriesStiffness(listOf(standoffTransverse, spacerSecant))
        ),
        Triple(
            CouplingScheme(
                name = "K3 — 8 ssDNA tethers through the layer to the substrate (C-0014's S3)",
                attachmentCount = 8,
                path = listOf(
                    CouplingPathElement(
                        "ssDNA tether, tangent", throughLayer.tangentStiffness(1.17)
                    )
                ),
                loadPathCrossesLattice = true,
                reusesFlatnessAttachments = false
            ),
            EntropicCoupling(8, throughLayer),
            8 * throughLayer.transverseStiffness(1.17)
        ),
        Triple(
            CouplingScheme(
                name = "K4 — 4 in-plane 40 nm tangential duplex tethers (C-0014's S4)",
                attachmentCount = 4,
                path = listOf(
                    CouplingPathElement(
                        "duplex tether, transverse (bending across a 40 nm span)",
                        beamTransverseStiffness(
                            AnchorMaterials.CANDO_BENDING_RIGIDITY, 40.0,
                            BeamEndCondition.PINNED_HEAD
                        )
                    )
                ),
                loadPathCrossesLattice = true,
                reusesFlatnessAttachments = false
            ),
            LinearCoupling(
                4 * beamTransverseStiffness(
                    AnchorMaterials.CANDO_BENDING_RIGIDITY, 40.0, BeamEndCondition.PINNED_HEAD
                )
            ),
            4 * rodAxialStiffness(AnchorMaterials.DUPLEX_STRETCH_MODULUS, 40.0)
        ),
        Triple(
            CouplingScheme(
                name = "K5 — 4 vertical duplex struts standing in the layer (C-0014's S1)",
                attachmentCount = 4,
                path = listOf(
                    CouplingPathElement(
                        "duplex strut, axial",
                        rodAxialStiffness(AnchorMaterials.DUPLEX_STRETCH_MODULUS, height)
                    )
                ),
                loadPathCrossesLattice = true,
                dependsOnCrossoverAxialCompliance = true,
                reusesFlatnessAttachments = false
            ),
            LinearCoupling(
                4 * rodAxialStiffness(AnchorMaterials.DUPLEX_STRETCH_MODULUS, height)
            ),
            4 * beamTransverseStiffness(
                strutRigidity, height, BeamEndCondition.PINNED_HEAD
            )
        ),
        Triple(
            CouplingScheme(
                name = "K6 — one concentrated lever attachment at the effort point",
                attachmentCount = 1,
                path = listOf(
                    CouplingPathElement(
                        "duplex link, axial",
                        rodAxialStiffness(AnchorMaterials.DUPLEX_STRETCH_MODULUS, 15.0)
                    )
                ),
                loadPathCrossesLattice = true,
                dependsOnCrossoverAxialCompliance = true,
                reusesFlatnessAttachments = false
            ),
            LinearCoupling(rodAxialStiffness(AnchorMaterials.DUPLEX_STRETCH_MODULUS, 15.0)),
            beamTransverseStiffness(
                AnchorMaterials.CANDO_BENDING_RIGIDITY, 15.0, BeamEndCondition.PINNED_HEAD
            )
        )
    )

    return candidates.map { (scheme, reaction, lateral) ->
        val stroke = firstOperatingStroke(characteristic, reaction, ceiling, SCAN_STEPS)
        val delivered = stroke?.let { reaction.reaction(it) - reaction.reaction(0.0) }
        val floor = requirement.stabilityFloor ?: 0.0
        val tangentAtWork = reaction.tangentStiffness(stroke ?: TARGET_STROKE)
        val peak = scheme.concentratedPathForce(TARGET_FORCE, CONCENTRATION_FACTOR)
        val perAnchorLateral = lateral / scheme.attachmentCount
        val schemeGrid = if (scheme.attachmentCount == count) grid
        else attachmentGrid(2, scheme.attachmentCount / 2 + scheme.attachmentCount % 2, TILE_EDGE_X, TILE_EDGE_Y)
            .take(scheme.attachmentCount)
        val yaw = yawStiffness(perAnchorLateral, schemeGrid)
        SchemeRecord(
            scheme = scheme.name,
            model = name,
            layerHeight = height,
            concentration = concentration,
            appliedBias = bias,
            verdict = CouplingSchemeVerdict(
                scheme = scheme.name,
                attachmentCount = scheme.attachmentCount,
                couplingStiffness = tangentAtWork,
                dominantComplianceTerm = scheme.dominantComplianceTerm,
                dominantComplianceShare = scheme.complianceBudget.max(),
                mandatedStiffness = mandated,
                stiffnessOverMandated = tangentAtWork / mandated,
                stabilityFloor = floor,
                meetsStabilityFloor = tangentAtWork > floor,
                deliveredStroke = stroke,
                deliveredForce = delivered,
                strokeOverTarget = stroke?.let { it / TARGET_STROKE },
                perPathStaticForce = scheme.perPathStaticForce(TARGET_FORCE),
                perPathPeakForce = peak,
                perPathThermalForce = scheme.perPathThermalForce,
                clearsUnzip = peak <= PerPathAllowable.UNZIP,
                clearsShear = peak <= PerPathAllowable.SHEAR,
                lateralStiffness = lateral,
                lateralOverBound = lateral / LATERAL_BOUND,
                yawStiffness = yaw,
                yawOverBound = yaw / YAW_BOUND,
                reusesFlatnessAttachments = scheme.reusesFlatnessAttachments,
                dependsOnCrossoverAxialCompliance = scheme.dependsOnCrossoverAxialCompliance,
                verdict = verdictOf(stroke, tangentAtWork, floor, peak)
            )
        )
    }
}

private fun verdictOf(
    stroke: Double?,
    stiffness: Double,
    floor: Double,
    peakForce: Double
): String = when {
    stroke == null -> "FAIL — too soft: the load line never meets the characteristic, so the " +
            "tile has no equilibrium and runs to near-contact"
    stiffness <= floor -> "FAIL — the operating point it places is statically unstable"
    stroke < 0.5 * TARGET_STROKE -> "FAIL — too stiff: it places the operating point at " +
            "%.3f nm, below §3's %.1f nm".format(stroke, TARGET_STROKE)
    peakForce > PerPathAllowable.NICKED_CEILING -> "FAIL — the per-load-path force exceeds the " +
            "65 pN nicked-duplex ceiling"
    peakForce > PerPathAllowable.SHEAR -> "MARGINAL — the per-load-path force is past the " +
            "quasi-static duplex shear allowable"
    peakForce > PerPathAllowable.UNZIP -> "MARGINAL — clears shear but not unzip geometry, so " +
            "every joint must be presented in shear"
    else -> "PASS — placed, stable, and clear of every per-load-path allowable"
}

// ---------------------------------------------------------------------------------------------
// the lever and the spacer
// ---------------------------------------------------------------------------------------------

private fun leverBudget(mandated: Double): List<LeverBudgetRecord> {
    val fourHelix = bundleBendingRigidity(
        listOf(1.345, -1.345, 1.345, -1.345),
        AnchorMaterials.CANDO_BENDING_RIGIDITY,
        AnchorMaterials.DUPLEX_STRETCH_MODULUS
    )
    val columns = (TILE_EDGE_Y / AnchorMaterials.INTERHELICAL_DISTANCE).toInt()
    return LeverSupport.entries.flatMap { support ->
        listOf(40.0, 60.0).map { span ->
            val rigidity = requiredBendingRigidity(mandated, span, support)
            val layers = layersForBendingRigidity(
                rigidity, columns, AnchorMaterials.INTERHELICAL_DISTANCE,
                AnchorMaterials.CANDO_BENDING_RIGIDITY, AnchorMaterials.DUPLEX_STRETCH_MODULUS
            )
            LeverBudgetRecord(
                support = support.description,
                span = span,
                requiredStiffness = mandated,
                requiredBendingRigidity = rigidity,
                duplexLayersNeeded = layers,
                blockThickness = layers?.let { it * AnchorMaterials.INTERHELICAL_DISTANCE },
                fitsInsideTheTileThickness =
                    (layers?.let { it * AnchorMaterials.INTERHELICAL_DISTANCE } ?: 1e9) <= 10.0,
                rigidityOfFourHelixBundle = fourHelix,
                stiffnessOfFourHelixBundle =
                    support.stiffnessFactor * fourHelix / (span * span * span)
            )
        }
    }
}

private fun spacerDesign(
    grid: List<Pair<Double, Double>>,
    mandated: Double
): List<SpacerDesignRecord> {
    val standoff = rodAxialStiffness(AnchorMaterials.DUPLEX_STRETCH_MODULUS, STANDOFF_LENGTH)
    val standoffTransverse = beamTransverseStiffness(
        AnchorMaterials.CANDO_BENDING_RIGIDITY, STANDOFF_LENGTH, BeamEndCondition.GUIDED_HEAD
    )
    val conventions = listOf(
        "zero-force SAXS/smFRET (Chen et al. 2012), the applicable end at ~2 pN"
                to SsDnaTether.KUHN_LENGTH_ZERO_FORCE,
        "zero-force, 2 mM MgCl2 (Chen et al. 2012)"
                to SsDnaTether.KUHN_LENGTH_ZERO_FORCE_TWO_MILLIMOLAR,
        "10-40 pN force spectroscopy, 2 mM MgCl2 (Bosco et al. 2014)"
                to SsDnaTether.KUHN_LENGTH_FORCE_SPECTROSCOPY_TWO_MILLIMOLAR
    )
    return conventions.flatMap { (label, kuhn) ->
        listOf(grid.size, 15, 8).map { count ->
            val spacer = designedSpacer(kuhn, count, standoff)
            val tension = TARGET_FORCE / count
            val coupling = SeriesEntropicCoupling(count, standoff, spacer)
            val lateral = count * seriesStiffness(
                listOf(standoffTransverse, spacer.transverseStiffness(tension))
            )
            val placement = if (count == grid.size) grid
            else attachmentGrid(1, count, TILE_EDGE_X, TILE_EDGE_Y)
            SpacerDesignRecord(
                kuhnLengthConvention = label,
                kuhnLength = kuhn,
                attachmentCount = count,
                standoffStiffness = standoff,
                requiredPerPathStiffness = mandated / count,
                spacerContourLength = spacer.contourLength,
                spacerNucleotides = spacer.contourLength / SsDnaTether.CONTOUR_PER_NUCLEOTIDE,
                perPathTension = tension,
                couplingSecantStiffness = coupling.reaction(TARGET_STROKE) / TARGET_STROKE,
                couplingTangentStiffness = coupling.tangentStiffness(TARGET_STROKE),
                lateralStiffness = lateral,
                lateralOverBound = lateral / LATERAL_BOUND,
                yawStiffness = yawStiffness(lateral / count, placement),
                yawOverBound = yawStiffness(lateral / count, placement) / YAW_BOUND,
                perPathThermalForce =
                    perAnchorThermalForce(coupling.tangentStiffness(TARGET_STROKE), count)
            )
        }
    }
}

// ---------------------------------------------------------------------------------------------
// gate 5 and gate 4
// ---------------------------------------------------------------------------------------------

/** `C-0012`'s own published numbers at 2 mM — **CITED**, and reproduced by re-running its solver. */
private val C0012_AT_TWO_MILLIMOLAR = mapOf(
    (5.0 to 0.10) to 167.2, (7.0 to 0.10) to 86.7, (10.0 to 0.10) to 34.5,
    (5.0 to 0.25) to 490.4, (7.0 to 0.25) to 214.7, (10.0 to 0.25) to 73.6
)

private fun reproduce(
    peg: PegWater,
    geometry: ActuatorGeometry,
    models: List<Pair<String, GraftedLayerModel>>,
    tileCharge: Double,
    lb: Double
): List<UpstreamReproductionRecord> {
    val published = readCoupledOperatingPoints(
        File("gpd/results/T-3-stroke-and-blocking-force.json")
    ).filter { it.medium == "free bulk buffer" && it.concentration == 2.0 }
    val field = sampler(2.0, tileCharge, lb)
    val curves = listOf(0.10, 0.25).associateWith { field.curve(it) }
    return DESIGN_POINTS.flatMap { (height, density) ->
        models.flatMap { (name, model) ->
            val chain = peg.graftedChain(model.chainLengthForHeight(peg, height, density), density)
            val balance = ActuatorForceBalance(model, chain, geometry)
            val held = balance.restingHeight - TARGET_STROKE
            listOf(0.10, 0.25).map { bias ->
                val curve = curves.getValue(bias)
                val record = published.first {
                    it.model == name && it.layerHeight == height && it.appliedBias == bias
                }
                val blocking = curve.magnitudeAt(balance.restingHeight)
                val output = curve.magnitudeAt(held) - balance.layerLoad(held)
                val effective = balance.layerStiffness(held) + curve.stiffnessAt(held)
                UpstreamReproductionRecord(
                    model = name,
                    layerHeight = height,
                    concentration = 2.0,
                    appliedBias = bias,
                    blockingForceHere = blocking,
                    blockingForceInC0012 = record.blockingForce,
                    blockingForceDeparture = abs(blocking / record.blockingForce - 1.0),
                    outputForceAtThreeNanometresHere = output,
                    outputForceAtThreeNanometresInC0012 = record.outputForceAtThreeNanometres!!,
                    outputForceDeparture =
                        abs(output / record.outputForceAtThreeNanometres - 1.0),
                    effectiveStiffnessHere = effective,
                    effectiveStiffnessInC0012 = record.loadedEffectiveStiffness!!,
                    effectiveStiffnessDeparture =
                        abs(effective / record.loadedEffectiveStiffness - 1.0)
                )
            }
        }
    }
}

private fun convergence(
    peg: PegWater,
    geometry: ActuatorGeometry,
    tileCharge: Double,
    lb: Double,
    mandated: Double
): List<CouplingConvergenceRecord> {
    val model = StrongStretchingLayer(
        desCloizeauxInteraction(peg.crossoverIndex, peg.monomerVolume)
    )
    val chain = peg.graftedChain(model.chainLengthForHeight(peg, 10.0, 0.024), 0.024)
    val balance = ActuatorForceBalance(model, chain, geometry)
    val held = balance.restingHeight - TARGET_STROKE
    val brush = balance.layerStiffness(held)
    val target = TARGET_FORCE + balance.layerLoad(held)
    val field = sampler(2.0, tileCharge, lb)
    // the bias does not depend on the force-curve sampling at all — it is bisected on the
    // solver directly — so it is located once and shared across that axis
    val biasAtDefaultMesh = biasForForce(field, held, target)!!
    val samplesAxis = listOf(36, 72, 144).map { samples ->
        val curve = field.curve(biasAtDefaultMesh, samples = samples)
        val effective = brush + curve.stiffnessAt(held)
        CouplingConvergenceRecord(
            quantity = "stability margin, 10 nm / des-Cloizeaux / strong-stretching / 2 mM",
            setting = "force-curve samples",
            samples = samples,
            meshNodes = DEFAULT_GAP_MESH_NODES,
            simultaneousTargetBias = biasAtDefaultMesh,
            effectiveStiffnessAtTarget = effective,
            stabilityMargin = mandated / max(-effective, 1e-12),
            relativeDeparture = 0.0
        )
    }
    val meshAxis = listOf(2000, 4000, 8000).map { nodes ->
        val bias = biasForForce(field, held, target, nodes = nodes)!!
        val curve = field.curve(bias, nodes = nodes)
        val effective = brush + curve.stiffnessAt(held)
        CouplingConvergenceRecord(
            quantity = "stability margin, 10 nm / des-Cloizeaux / strong-stretching / 2 mM",
            setting = "Poisson-Boltzmann mesh nodes",
            samples = CURVE_SAMPLES,
            meshNodes = nodes,
            simultaneousTargetBias = bias,
            effectiveStiffnessAtTarget = effective,
            stabilityMargin = mandated / max(-effective, 1e-12),
            relativeDeparture = 0.0
        )
    }
    // each axis is referred to ITS OWN finest setting. Referring both to one reference
    // would fold the sampling error into the mesh departure and report a convergence the
    // mesh axis has not demonstrated.
    return (samplesAxis.againstFinest() + meshAxis.againstFinest())
}

/** Relative departure of each record from the last — the finest setting on that axis. */
private fun List<CouplingConvergenceRecord>.againstFinest(): List<CouplingConvergenceRecord> {
    val reference = last().stabilityMargin
    return map { it.copy(relativeDeparture = abs(it.stabilityMargin / reference - 1.0)) }
}

// ---------------------------------------------------------------------------------------------
// findings, validity, provenance
// ---------------------------------------------------------------------------------------------

private fun findings(result: OutputCouplingResult, mandated: Double): Map<String, String> {
    fun f(value: Double?, digits: Int = 3) =
        if (value == null || !value.isFinite()) "n/a" else "%.${digits}f".format(value)

    fun bracket(
        height: Double,
        concentration: Double,
        digits: Int = 2,
        select: (CouplingRequirementRecord) -> Double?
    ): String {
        val values = result.requirements
            .filter { it.layerHeight == height && it.concentration == concentration }
            .mapNotNull(select).filter { it.isFinite() }
        return if (values.isEmpty()) "n/a"
        else "${f(values.min(), digits)} – ${f(values.max(), digits)}"
    }

    val twoMillimolar = result.requirements.filter { it.concentration == 2.0 }
    val unstable = twoMillimolar.count { it.mandatedStiffnessIsStable == false }
    val worstReproduction = result.upstreamReproduction.maxOf {
        maxOf(it.blockingForceDeparture, it.outputForceDeparture, it.effectiveStiffnessDeparture)
    }
    val passing = result.schemes.filter { it.verdict.verdict.startsWith("PASS") }
        .map { it.scheme }.distinct().sorted()
    return mapOf(
        "the_requirement_is_fixed_by_section_3_alone" to
                "The force delivered to the load over a stroke is k_c times that stroke, " +
                "independent of any preload, so §3's 100 pN and 3 nm fix the output-coupling " +
                "stiffness at ${f(mandated)} pN/nm by arithmetic. That is the whole PLACEMENT " +
                "condition, and it inverts the task: a duplex in tension is 110 pN/nm at 10 nm " +
                "and forty-five of them are 4950, so the question is not whether a DNA-origami " +
                "coupling can be stiff enough but whether it can be made COMPLIANT enough.",
        "the_stability_requirement_read_at_the_operating_bias" to
                "At 2 mM, the bias that delivers 100 pN AT a 3 nm stroke is " +
                bracket(5.0, 2.0, 3) { it.simultaneousTargetBias } + " V (5 nm), " +
                bracket(7.0, 2.0, 3) { it.simultaneousTargetBias } + " V (7 nm) and " +
                bracket(10.0, 2.0, 3) { it.simultaneousTargetBias } + " V (10 nm). At those " +
                "biases the coupling has to exceed a stability floor of " +
                bracket(5.0, 2.0) { it.stabilityFloor } + " pN/nm (5 nm), " +
                bracket(7.0, 2.0) { it.stabilityFloor } + " (7 nm) and " +
                bracket(10.0, 2.0) { it.stabilityFloor } + " (10 nm), against §3's own " +
                "${f(mandated)}. ${unstable} of ${twoMillimolar.size} states at 2 mM fail it.",
        "C-0012s_coupling_table_is_quoted_off_the_operating_point" to
                "C-0012 reports 5.3-16.0 pN/nm at 10 nm / 0.10 V rising to 47.6-71.5 at " +
                "0.25 V, and 85.6-276.6 at 7 nm / 0.25 V. Neither 0.10 V nor 0.25 V is an " +
                "operating bias: its own biasForSimultaneousTarget is 0.082-0.155 V at 7 nm " +
                "and 0.134-0.192 V at 10 nm, i.e. BETWEEN the two grid points the table is " +
                "quoted at, and its bias grid has no sample there. Every number in that table " +
                "is reproduced here from C-0012's own file; what is challenged is its SCOPE.",
        "the_stability_margin_and_what_it_is_worth" to
                "At 2 mM the margin k_c* over |k_eff| runs " +
                bracket(7.0, 2.0) { it.stabilityMargin } + " at 7 nm and " +
                bracket(10.0, 2.0) { it.stabilityMargin } + " at 10 nm. Whatever its value, it " +
                "is quoted against an inherited mean-field error of 123-214% (C-0005). The " +
                "verdict is NOT EXCLUDED, never established.",
        "what_a_dna_origami_coupling_delivers" to
                "Schemes reaching PASS on placement, stability and every per-load-path " +
                "allowable: " + (if (passing.isEmpty()) "NONE" else passing.joinToString("; ")) +
                ". The stiffness itself is never the binding constraint — it is abundantly " +
                "available and the design problem is to spend it as compliance in a tuned " +
                "ssDNA spacer.",
        "the_operating_bias_C-0012_never_located" to
                "C-0012 obtains its own biasForSimultaneousTarget by INTERPOLATING the first " +
                "crossing across its bias grid, and at 10 nm all six models put that crossing " +
                "inside the bracket [0.1, 0.25] — a 2.5x interval its grid does not sample. " +
                "Bisecting for the same quantity instead moves it by up to " +
                "%.1f".format(
                    100.0 * result.requirements.mapNotNull {
                        it.simultaneousTargetBiasDeparture
                    }.max()
                ) + "% over the 54 states here. That is the size of the effect CH-0016 is " +
                "about, and it is why the requirement is re-solved rather than read off.",
        "gate_5_reproduction" to
                "C-0012's blocking force, W(3 nm) and k_eff(3 nm) are reproduced at both of " +
                "its grid biases at 2 mM to a worst relative departure of " +
                "%.2e".format(worstReproduction) + " over all 36 comparisons, because the " +
                "same solver was re-run rather than a table copied."
    )
}

private val CITED = listOf(
    "C-0012's coupled force balance, C-0003's six (profile x interaction) layer models and " +
            "C-0008's F_es(h, V) pipeline — CONSUMED as libraries from actuator/, brush/, " +
            "material/ and electrostatics/ and RE-RUN here, not tabulated. C-0012's own " +
            "published numbers are reproduced as a gate-5 check against its result file.",
    "C-0014's element mechanics — rodAxialStiffness, beamTransverseStiffness, " +
            "bundleBendingRigidity and FreelyJointedChain — CONSUMED from anchoring/ " +
            "unchanged. This task owns none of them.",
    "C-0014's lateral bound 0.460216 pN/nm and yaw bound 368.173 pN*nm/rad — CITED, and " +
            "themselves derived there from k_BT and leaf A1.1's 3.0 nm.",
    "C-0015's 45 attachments as 3 x 15, one row per duplex, and its result that such a " +
            "scheme zeroes the per-load-path CROSSOVER force EXACTLY under a uniform load " +
            "— CITED. It is what licenses not applying C-0009's concentration factor to a " +
            "matched distributed coupling.",
    "C-0009's 2.3-7.6x out-of-plane load concentration at a discrete anchor and its 56 " +
            "crossovers — CITED, applied at the worst value to every scheme whose reaction " +
            "does cross the lattice.",
    "C-0006's per-load-path allowables 10 pN unzip, 48 pN quasi-static duplex shear and a " +
            "65 pN nicked-duplex ceiling — CITED, MEASURED, and loading-rate dependent. NOT " +
            "§4(f)'s 35-60 pN, which is a whole-cross-section number.",
    "S = 1100 pN and L_p = 40 nm in Mg2+ — CITED, MEASURED, Wang et al., Biophys. J. 72:1335 " +
            "(1997). EI = 230 pN*nm^2 and GJ = 460 — CITED, CanDo MODEL INPUTS (Kim et al., " +
            "NAR 40:2862, 2012), not measurements. Interhelical distance 2.69 nm — CITED, " +
            "MEASURED (SAXS), Fischer et al., Nano Lett. 16:4282 (2016).",
    "The ssDNA Kuhn length bracket 1.34-2.84 nm and the contour 0.65 nm/nt — CITED, MEASURED, " +
            "Bosco et al., NAR 42:2064 (2014) and Chen et al., PNAS 109:799 (2012). The " +
            "spacers here carry ~2 pN, an order of magnitude below the lowest force the " +
            "spectroscopy fits cover, so the ZERO-FORCE end is the applicable one — and it " +
            "is the soft one, hence the conservative one for a stiffness requirement.",
    "The 100 pN, the 3 nm stroke, the 40 x 40 nm footprint, the 5/7/10 nm heights and the " +
            "2 V bias ceiling — §3 and §6."
)

private val VALIDITY = listOf(
    "TRL 1-3. Nothing here is measured. No coupling below has been built and none is " +
            "proposed as a sequence design.",
    "L0 is a FORCE-ONSET height (C-0011, CH-0010) at a defining load of 1.0 pN over the tile. " +
            "The held gap L0 - 3 nm therefore inherits that convention, and a bench reading " +
            "these numbers in the first-moment convention would be off by 1.71-2.16x in " +
            "thickness.",
    "The characteristic is C-0003's, at C-0001's single grafting density per height — NOT " +
            "C-0011's solved SCF profile. That is deliberate: C-0012's characteristic, the " +
            "object a load line is drawn across, was computed on C-0003, and mixing the two " +
            "would compare a load line against a different curve. C-0016 reports that at " +
            "5 nm the solved layer is 1.22x outside C-0003's bracket, so every 5 nm number " +
            "here carries that exposure.",
    "Mean-field electrostatics, inherited whole. C-0005 puts the one-loop correction at " +
            "123-214% of the leading term across the entire 5-10 nm range for Mg2+. This is " +
            "one to two orders of magnitude larger than the stability margin reported here " +
            "and it is NOT reducible by a better Poisson-Boltzmann solve.",
    "The coupling is treated as a LOAD LINE in one coordinate: the tile mean, under a " +
            "uniform load, which is the only case in which C-0006's tile is rigid. A real " +
            "coupling dishes the tile and C-0006 rejects the rigid-plate assumption for every " +
            "discrete scheme; the 45-attachment count is C-0015's answer to exactly that, and " +
            "it is CITED rather than recomputed.",
    "The lever is a SECTION REQUIREMENT, not a design. No lever geometry is specified in §1 " +
            "or §3, so what is delivered is the bending rigidity a beam of a given span needs " +
            "and the number of duplex layers that implies — under three end conditions " +
            "spanning 25.6x, which is the largest spread in this task.",
    "The superstructure a distributed coupling gathers into is assumed to be MULTILAYER. " +
            "CanDo's rigid-crossover treatment is defensible there (the degree of freedom is " +
            "geometrically frustrated in a bundle) and is not defensible for a single-layer " +
            "sheet, where it is the only across-helix compliance.",
    "The zero-bias state is not solved. C-0012 shows the zero-bias force is a sign-changing " +
            "near-cancellation under 4 pN for which no single number is defensible, so every " +
            "coupling here is taken as unpreloaded and the preload a stiffer coupling would " +
            "need is reported as a relation rather than evaluated. That is T-13's question.",
    "5 and 10 mM MgCl2 are not swept: C-0012 shows §3's 100 pN force target is unreachable " +
            "at ANY bias at 7 and 10 nm in 10 mM, so there is no operating point for a " +
            "coupling to be sized at.",
    "The lateral and yaw by-products assume the coupling's far ends are laterally fixed to " +
            "the substrate. A superstructure free to translate supplies exactly zero lateral " +
            "stiffness, by the same symmetry argument C-0010 makes about the layer."
)

private val OPEN = listOf(
    "The crossover's VERTICAL/AXIAL compliance is a rigid constraint in C-0009 with nothing " +
            "cited behind it, and T-9 has not run. It does NOT gate a distributed coupling " +
            "matched one row per duplex, because C-0015 shows no interface transmits anything " +
            "there. It DOES gate every concentrated scheme, whose reaction must cross the " +
            "lattice. So the answer depends on T-9 exactly to the extent that the coupling is " +
            "concentrated, and that is a design choice rather than a missing measurement.",
    "The dishing a real 45-attachment coupling causes is C-0015's, computed on a Winkler " +
            "foundation at C-0001's stiffness, and it is CITED here rather than recomputed " +
            "against the layer this task loads.",
    "The zero-bias rest position under a preloaded coupling — T-13. A coupling stiffer than " +
            "the placement value needs a DOWNWARD preload, which the layer must carry at zero " +
            "bias, and three of six layer models have exactly zero stiffness at L0.",
    "The in-plane load path into the tile is C-0009's out-of-plane concentration used as a " +
            "conservative stand-in — T-15.",
    "No 2-D field solve and no lateral load profile — T-3b. The load line here is the tile " +
            "mean.",
    "The lever's own joints are budgeted as a section requirement, not solved. A finite-" +
            "element model of an origami lever on its fulcrum would replace the three end " +
            "conditions with one number; it is not run here because the spread it would " +
            "remove, 25.6x, is still smaller than the mean-field error already carried."
)

private fun report(result: OutputCouplingResult, output: File, mandated: Double) {
    println()
    println("T-16 — output-coupling stiffness")
    println("  §3's mandated coupling stiffness: %.4f pN/nm".format(mandated))
    println("  requirement records: ${result.requirements.size}")
    println("  scheme records: ${result.schemes.size}")
    println("  upstream reproductions: ${result.upstreamReproduction.size}")
    println()
    result.findings.forEach { (key, value) ->
        println("  [$key]")
        println("    $value")
        println()
    }
    println("  written to ${output.path}")
}
