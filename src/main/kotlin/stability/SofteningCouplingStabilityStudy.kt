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
import com.xemantic.nano.plentyofroom.anchoring.FlexureOrientation
import com.xemantic.nano.plentyofroom.anchoring.StandoffBase
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
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.abs
import kotlin.math.max

/**
 * Task `T-76` — `C-0018`'s pull-in and fold analysis re-run on a coupling whose tangent has an
 * interior minimum **below** its placed secant. Leaf `A8.2`, with `A2.2`.
 *
 * Run with:
 *
 * ```shell
 * tools/study.sh stability.SofteningCouplingStabilityStudyKt
 * ```
 *
 * Emits `gpd/results/T-76-softening-coupling-stability.json`, deterministically — no timestamp,
 * every floating-point number rounded at the serialisation boundary, every decision taken on the
 * **rounded** values with declaration order as the tie-break.
 *
 * Consumes `C-0018`'s `EquilibriumPath` and `C-0012`'s force balance **unchanged** (the path takes
 * its load as an arbitrary function of the stroke, so a nonlinear coupling substitutes without
 * touching the solver) and `C-0030`'s coupled flexure as the load line. Owns
 * `stability/SofteningCouplingStability.kt` and this file, and edits nothing.
 */

// ---------------------------------------------------------------------------------------------
// records — every one prefixed with this study's own name, per CLAUDE.md
// ---------------------------------------------------------------------------------------------

/** One coupling law, read as a design: what it places with and what it stabilises with. */
@Serializable
@Suppress("LongParameterList")
data class T76CouplingRecord(
    val line: String,
    val coupled: Boolean,
    val orientation: String,
    val pathCount: Int,
    val standoffLength: Double,
    val span: Double,
    val spanBasePairs: Double,
    val reactionAcceptable: Double,
    val secantAcceptable: Double,
    val tangentAcceptable: Double,
    val tangentToSecant: Double,
    val tangentAtZeroStroke: Double,
    val minimumTangentFullRange: Double,
    val minimumTangentFullRangeStroke: Double,
    val minimumTangentFullRangeInterior: Boolean,
    val minimumTangentWorkingRange: Double,
    val minimumTangentWorkingRangeStroke: Double,
    val minimumTangentWorkingRangeInterior: Boolean,
    val secantDesired: Double,
    val reactionDesired: Double,
    val axialForceDesired: Double,
    val clearsComplianceCeiling: Boolean,
    val placementResidual: Double
)

/** One `(height, model, buffer, load line)` state: the fold, and the two stability readings. */
@Serializable
@Suppress("LongParameterList")
data class T76FoldRecord(
    val model: String,
    val layerHeight: Double,
    val graftingDensity: Double,
    val concentration: Double,
    val loadLine: String,
    val restingHeight: Double,
    val strokeCeiling: Double,
    // the operating point §3 asks for — identical across the four lines by placement
    val operatingBias: Double?,
    val operatingGap: Double?,
    val operatingVolumeFraction: Double?,
    val brushStiffnessAtOperating: Double?,
    val electrostaticStiffnessAtOperating: Double?,
    val effectiveStiffnessAtOperating: Double?,
    val stabilityFloor: Double?,
    val couplingTangentAtOperating: Double,
    val coupledTangentAtOperating: Double?,
    val marginAtWorkingPoint: Double?,
    val minimumTangent: Double,
    val marginOnMinimumTangent: Double?,
    val stableAtWorkingPointTangent: Boolean?,
    val stableOnMinimumTangent: Boolean?,
    // ceiling 1 — the fold of the path under THIS line
    val pullInBias: Double?,
    val pullInStroke: Double?,
    val pullInGap: Double?,
    val foldAtBranchStart: Boolean,
    val stableShallowBranchExists: Boolean,
    val couplingTangentAtFold: Double?,
    val brushStiffnessAtFold: Double?,
    val electrostaticStiffnessAtFold: Double?,
    val effectiveStiffnessAtFold: Double?,
    val coupledTangentAtFold: Double?,
    val tangencyResidual: Double?,
    val branchEndStroke: Double?,
    val branchEndedOnTheField: Boolean,
    // ceiling 2 — the upstream validity boundary that binds almost everywhere in C-0018
    val concentratedCrossoverBias: Double?,
    val concentratedCrossoverBeyondFold: Boolean,
    val pointIonBias: Double,
    // the verdict
    val bindingCeiling: String?,
    val usableBias: Double?,
    val biasMargin: Double?,
    val targetStrokeOnStableSide: Boolean?,
    val operatingPointIsUsable: Boolean?,
    val verdict: String,
    val searchEvaluations: Int
)

/** `CH-0042`'s named escape, priced: the adverse mounting against `C-0023`'s ceiling. */
@Serializable
data class T76EscapeRecord(
    val standoffLength: Double,
    val orientation: String,
    val span: Double,
    val secantAcceptable: Double,
    val tangentAcceptable: Double,
    val tangentToSecant: Double,
    val minimumTangentFullRange: Double,
    val complianceCeiling: Double,
    val clearsComplianceCeiling: Boolean,
    val exceedanceFactor: Double
)

/** Upstream numbers reproduced through this study's own pipeline — the gate-5 record. */
@Serializable
data class T76UpstreamRecord(
    val quantity: String,
    val state: String,
    val here: Double,
    val upstream: Double,
    val departure: Double,
    val source: String
)

/** One convergence axis, referred to **its own** finest setting. */
@Serializable
data class T76ConvergenceRecord(
    val axis: String,
    val setting: String,
    val quantity: String,
    val value: Double,
    val departureFromFinest: Double
)

/** The `T-76` result envelope. */
@Serializable
@Suppress("LongParameterList")
data class SofteningCouplingStabilityResult(
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
    val couplings: List<T76CouplingRecord>,
    val folds: List<T76FoldRecord>,
    val escape: List<T76EscapeRecord>,
    val upstreamChecks: List<T76UpstreamRecord>,
    val convergence: List<T76ConvergenceRecord>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

// ---------------------------------------------------------------------------------------------
// the sweep
// ---------------------------------------------------------------------------------------------

/** §3's three layer heights with `C-0001`'s grafting densities — `C-0012`'s own design points. */
private val DESIGN_POINTS = listOf(5.0 to 0.092, 7.0 to 0.045, 10.0 to 0.024)

/** Leaf `A2.2`'s low-screening point, §3's nominal buffer, and §3's high one — `C-0018`'s own. */
private val BUFFERS = listOf(0.5, 2.0, 10.0)

private const val TRUSTED_BIAS_CEILING = 1.0

private const val CONCENTRATED_CROSSOVER = 0.2

private const val STERN_CAPACITANCE = 20.0

private const val MESH_NODES = 2000

private const val CURVE_LOWEST_GAP = 0.5

private const val OSMOTIC_SECOND_VIRIAL = 1.9e-3

private const val OSMOTIC_THIRD_VIRIAL = 2.0e-2

private const val FOOTPRINT = 1600.0

/** The standoff lengths `C-0017`'s envelope admits — the axis the escape is priced on. */
private val STANDOFF_LENGTHS = listOf(3.0, 4.0, 5.0, 6.0, 7.0, 8.0, 9.0, 10.0)

private val RECOMMENDED_BASE = StandoffBase.crossovers(2, favourableOrientation = true)

/**
 * The four load lines, all **placed** at 100 pN over §3's 3 nm acceptable stroke.
 *
 * `L1` is `C-0018`'s own affine coupled line and is the reference every other row is read
 * against; `L2` is `C-0028`'s decoupled reading of the same design, which strain-**stiffens** and
 * is the premise `C-0017`'s theorem was stated under; `L3` is `C-0030`'s recommended design, which
 * strain-**softens** and is what `CH-0042` is about; `L4` is the adverse mounting, `CH-0042`'s
 * named escape.
 */
private fun loadLines(): List<StrokeLoadLine> = listOf(
    AffineLoadLine("L1 linear mandate", GEN1_MANDATE_STIFFNESS),
    gen1CouplingLine("L2 decoupled (C-0028)", coupled = false, orientation = FlexureOrientation.FAVOURABLE),
    gen1CouplingLine("L3 coupled favourable (C-0030)", coupled = true, orientation = FlexureOrientation.FAVOURABLE),
    gen1CouplingLine("L4 coupled adverse (C-0030)", coupled = true, orientation = FlexureOrientation.ADVERSE)
)

/** The field, parametrised by the **diffuse-layer drop** — `C-0018`'s cheap direction. */
private class T76Field(
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

@Suppress("LongMethod")
fun main() {
    val peg = PegWater()
    val geometry = ActuatorGeometry()
    val tile = DnaOrigamiTile()
    val lb = bjerrumLength()
    val surviving = tile.manningSurvivingFraction(2, lb)
    val tileCharge = -(tile.projectedChargeDensity * surviving / 2.0)
    val models = layerModels(peg)
    val fields = BUFFERS.associateWith { T76Field(it, tileCharge, lb) }
    val lines = loadLines()

    println("T-76 — the four load lines, as designs ...")
    val couplings = lines.map { couplingRecord(it) }
    couplings.forEach {
        println(
            ("  %s: span %.3f nm, secant %.4f, tangent %.4f, t/s %.4f, min %.4f at %.3f nm")
                .format(
                    it.line, it.span, it.secantAcceptable, it.tangentAcceptable,
                    it.tangentToSecant, it.minimumTangentFullRange,
                    it.minimumTangentFullRangeStroke
                )
        )
    }

    println("T-76 — re-locating C-0018's fold, load line by load line ...")
    val folds = mutableListOf<T76FoldRecord>()
    DESIGN_POINTS.forEach { (height, density) ->
        models.forEach { (name, model) ->
            val chain = peg.graftedChain(model.chainLengthForHeight(peg, height, density), density)
            val balance = ActuatorForceBalance(model, chain, geometry)
            BUFFERS.forEach { concentration ->
                val field = fields.getValue(concentration)
                lines.forEachIndexed { index, line ->
                    folds += foldRecord(
                        name, height, density, concentration, balance, chain, field, line,
                        couplings[index]
                    )
                }
            }
            println("  $height nm  $name done")
        }
    }

    println("T-76 — pricing the escape ...")
    val escape = escapeRecords()

    println("T-76 — upstream reproductions and convergence ...")
    val upstream = upstreamChecks(couplings, folds)
    val convergence = convergence(peg, geometry, tileCharge, lb, lines)

    val result = SofteningCouplingStabilityResult(
        task = "T-76",
        leaf = "A8.2",
        title = "C-0018's pull-in and fold analysis re-run on a strain-SOFTENING output " +
                "coupling: the placement clause fixes the level of the load line and the " +
                "stability clause its slope, and C-0030's element satisfies the first while " +
                "failing the second wherever the fold binds",
        verificationType = "in-silico (C-0018's stroke-parametrised equilibrium path re-run with " +
                "C-0030's nonlinear reaction law substituted for the affine R = 33.333 s, over " +
                "the same (height, model, buffer) grid, graded against the tangency identity " +
                "k_c(s_fold) + k_eff(s_fold) = 0 with k_c from the element's analytic tangent " +
                "and k_es from a central difference of a full field re-solve at fixed applied " +
                "bias) + logical (placement and stability are read on two different slopes of " +
                "one law and are checked separately)",
        acceptance = "Q1 placement: every line's assembled secant is 33.3333 pN/nm at 3 nm and " +
                "all four locate the SAME operating bias at every state. Q2 stability read the " +
                "way CH-0042 requires: min_s k_tangent(s) over the operating range against " +
                "|k_eff| at the operating point. Q3 stability read on the path: the fold sits " +
                "above the operating bias AND at a stroke deeper than 3 nm. Q4 the margin " +
                "reported on the bias axis as well as the stiffness axis. Q5 the adverse " +
                "mounting priced against C-0023's 40 pN/nm compliance ceiling.",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated. " +
                "Every force inherits C-0008's mean-field statement in full (C-0005: the " +
                "one-loop correction is 123-214% of the leading term across this gap range), " +
                "and the MOTIF is not demonstrated either: C-0028's literature finding stands, " +
                "no duplex has been built standing normal to a single-layer sheet. NOTHING " +
                "HERE IS MEASURED.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm (= 1 mN/m exactly)",
            "potential" to "V",
            "concentration" to "mM",
            "temperature" to "K"
        ),
        conventions = CONVENTIONS,
        runParameters = mapOf(
            "temperature" to ROOM_TEMPERATURE.toString(),
            "thermalEnergy" to thermalEnergy().toString(),
            "bjerrumLength" to lb.toString(),
            "footprintArea" to FOOTPRINT.toString(),
            "manningSurvivingFraction" to surviving.toString(),
            "nominalTileChargeDensity" to tileCharge.toString(),
            "sternCapacitance" to STERN_CAPACITANCE.toString(),
            "meshNodes" to MESH_NODES.toString(),
            "layerHeights" to DESIGN_POINTS.map { it.first }.toString(),
            "graftingDensities" to DESIGN_POINTS.map { it.second }.toString(),
            "buffers" to BUFFERS.toString(),
            "loadLines" to lines.map { it.name }.toString(),
            "pathCount" to GEN1_PATH_COUNT.toString(),
            "standoffLength" to GEN1_STANDOFF_LENGTH.toString(),
            "baseRotationalStiffness" to RECOMMENDED_BASE.rotationalStiffness.toString(),
            "duplexBendingRigidity" to Gen1Tile.DUPLEX_BENDING_RIGIDITY.toString(),
            "duplexStretchModulus" to Gen1Tile.DUPLEX_STRETCH_MODULUS.toString(),
            "targetForce" to GEN1_TARGET_FORCE.toString(),
            "acceptableStroke" to GEN1_ACCEPTABLE_STROKE.toString(),
            "desiredStroke" to GEN1_DESIRED_STROKE.toString(),
            "mandatedCouplingStiffness" to GEN1_MANDATE_STIFFNESS.toString(),
            "complianceCeiling" to GEN1_COMPLIANCE_CEILING.toString(),
            "concentratedCrossover" to CONCENTRATED_CROSSOVER.toString(),
            "trustedBiasCeiling" to TRUSTED_BIAS_CEILING.toString(),
            "diffuseCeiling" to DEFAULT_DIFFUSE_CEILING.toString(),
            "diffuseBracketTolerance" to DEFAULT_DIFFUSE_TOLERANCE.toString(),
            "foldCoarseSteps" to DEFAULT_COARSE_STEPS.toString(),
            "foldStrokeTolerance" to DEFAULT_STROKE_TOLERANCE.toString(),
            "curveLowestGap" to CURVE_LOWEST_GAP.toString()
        ),
        citedInputs = CITED,
        couplings = couplings,
        folds = folds,
        escape = escape,
        upstreamChecks = upstream,
        convergence = convergence,
        findings = emptyMap(),
        validity = VALIDITY,
        openQuestions = OPEN
    )
    val complete = result.copy(findings = findings(result))
    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-76-softening-coupling-stability.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(json.encodeToJsonElement(complete).roundedForActuatorResult()) + "\n"
    )
    report(complete, output)
}

// ---------------------------------------------------------------------------------------------
// the coupling laws
// ---------------------------------------------------------------------------------------------

private fun couplingRecord(line: StrokeLoadLine): T76CouplingRecord {
    val assembled = line as? AssembledFlexureLine
    val full = line.tangentMinimum(0.0, GEN1_DESIRED_STROKE)
    val working = line.tangentMinimum(GEN1_ACCEPTABLE_STROKE, GEN1_DESIRED_STROKE)
    val tangentAcceptable = line.tangent(GEN1_ACCEPTABLE_STROKE)
    return T76CouplingRecord(
        line = line.name,
        coupled = assembled != null && assembled.flexure.couplingFactor != 0.0,
        orientation = assembled?.orientation?.name ?: "n/a",
        pathCount = assembled?.count ?: 0,
        standoffLength = if (assembled == null) 0.0 else GEN1_STANDOFF_LENGTH,
        span = assembled?.span ?: 0.0,
        spanBasePairs = (assembled?.span ?: 0.0) / Gen1Tile.RISE_PER_BASE_PAIR,
        reactionAcceptable = line.reaction(GEN1_ACCEPTABLE_STROKE),
        secantAcceptable = line.secant(GEN1_ACCEPTABLE_STROKE),
        tangentAcceptable = tangentAcceptable,
        tangentToSecant = line.tangentToSecant(GEN1_ACCEPTABLE_STROKE),
        tangentAtZeroStroke = line.tangent(0.0),
        minimumTangentFullRange = full.stiffness,
        minimumTangentFullRangeStroke = full.stroke,
        minimumTangentFullRangeInterior = full.interior,
        minimumTangentWorkingRange = working.stiffness,
        minimumTangentWorkingRangeStroke = working.stroke,
        minimumTangentWorkingRangeInterior = working.interior,
        secantDesired = line.secant(GEN1_DESIRED_STROKE),
        reactionDesired = line.reaction(GEN1_DESIRED_STROKE),
        axialForceDesired = assembled?.axialForce(GEN1_DESIRED_STROKE) ?: 0.0,
        clearsComplianceCeiling = tangentAcceptable <= GEN1_COMPLIANCE_CEILING,
        placementResidual = abs(line.secant(GEN1_ACCEPTABLE_STROKE) / GEN1_MANDATE_STIFFNESS - 1.0)
    )
}

// ---------------------------------------------------------------------------------------------
// the folds
// ---------------------------------------------------------------------------------------------

@Suppress("LongParameterList", "LongMethod")
private fun foldRecord(
    name: String,
    height: Double,
    density: Double,
    concentration: Double,
    balance: ActuatorForceBalance,
    chain: GraftedChain,
    field: T76Field,
    line: StrokeLoadLine,
    coupling: T76CouplingRecord
): T76FoldRecord {
    val resting = balance.restingHeight
    val floor = max(chain.occupiedThickness * 1.01, CURVE_LOWEST_GAP)
    val strokeCeiling = resting - floor
    val path = EquilibriumPath(
        restingHeight = resting,
        strokeCeiling = strokeCeiling,
        field = field.asPath()
    ) { stroke -> line.reaction(stroke) + balance.layerLoad(resting - stroke) }
    val search = path.fold()
    val fold = search.fold
    val operating =
        if (GEN1_ACCEPTABLE_STROKE <= strokeCeiling) path.at(GEN1_ACCEPTABLE_STROKE) else null

    // the stability reading at the operating point — C-0017's own, but on THIS line's tangent
    val brushOperating = operating?.let { balance.layerStiffness(it.gap) }
    val esOperating = operating?.let { field.stiffnessAtBias(it.gap, it.appliedBias) }
    val effectiveOperating =
        if (brushOperating != null && esOperating != null) brushOperating + esOperating else null
    val floorOperating = effectiveOperating?.let { if (it < 0.0) -it else 0.0 }
    val tangentOperating = line.tangent(GEN1_ACCEPTABLE_STROKE)
    val minimumTangent = coupling.minimumTangentFullRange

    // the fold read differentially — an independent route to the same point
    val brushFold = fold?.let { balance.layerStiffness(it.gap) }
    val esFold = fold?.let { field.stiffnessAtBias(it.gap, it.appliedBias) }
    val effectiveFold = if (brushFold != null && esFold != null) brushFold + esFold else null
    val tangentFold = fold?.let { line.tangent(it.stroke) }
    val coupledFold =
        if (effectiveFold != null && tangentFold != null) tangentFold + effectiveFold else null
    // a boundary maximum is not a stationary point — no residual is reported there
    val residual = if (search.foldAtBranchStart) null else coupledFold?.let {
        abs(it) / max(
            (tangentFold ?: 0.0) + abs(brushFold ?: 0.0) + abs(esFold ?: 0.0), 1e-12
        )
    }

    val crossoverGap = chain.occupiedThickness / CONCENTRATED_CROSSOVER
    val crossoverStroke = resting - crossoverGap
    val crossover =
        if (crossoverStroke <= 0.0 || crossoverStroke > strokeCeiling) null
        else path.at(crossoverStroke)?.appliedBias
    val crossoverBeyond = crossoverStroke > (fold?.stroke ?: strokeCeiling)

    val candidates = listOf(
        BiasCeiling("static stability (pull-in)", fold?.appliedBias),
        BiasCeiling("concentrated crossover (C-0002, phi = 0.2)", if (crossoverBeyond) null else crossover),
        BiasCeiling("point-ion boundary (CH-0007, 1.0 V)", TRUSTED_BIAS_CEILING)
    )
    val binding = bindingCeiling(candidates)
    val onStableSide = operating?.let { fold == null || GEN1_ACCEPTABLE_STROKE <= fold.stroke + 1e-9 }
    val usable = operating?.let { point ->
        binding?.bias?.let { ceiling -> point.appliedBias <= ceiling && onStableSide == true }
    }
    val stableWorking = floorOperating?.let { tangentOperating > it }
    val stableMinimum = floorOperating?.let { minimumTangent > it }
    return T76FoldRecord(
        model = name,
        layerHeight = height,
        graftingDensity = density,
        concentration = concentration,
        loadLine = line.name,
        restingHeight = resting,
        strokeCeiling = strokeCeiling,
        operatingBias = operating?.appliedBias,
        operatingGap = operating?.gap,
        operatingVolumeFraction = operating?.let { chain.meanVolumeFraction(it.gap) },
        brushStiffnessAtOperating = brushOperating,
        electrostaticStiffnessAtOperating = esOperating,
        effectiveStiffnessAtOperating = effectiveOperating,
        stabilityFloor = floorOperating,
        couplingTangentAtOperating = tangentOperating,
        coupledTangentAtOperating = effectiveOperating?.let { tangentOperating + it },
        marginAtWorkingPoint = effectiveOperating?.let { stabilityMargin(tangentOperating, it) },
        minimumTangent = minimumTangent,
        marginOnMinimumTangent = effectiveOperating?.let { stabilityMargin(minimumTangent, it) },
        stableAtWorkingPointTangent = stableWorking,
        stableOnMinimumTangent = stableMinimum,
        pullInBias = fold?.appliedBias,
        pullInStroke = fold?.stroke,
        pullInGap = fold?.gap,
        foldAtBranchStart = search.foldAtBranchStart,
        stableShallowBranchExists = !search.foldAtBranchStart,
        couplingTangentAtFold = tangentFold,
        brushStiffnessAtFold = brushFold,
        electrostaticStiffnessAtFold = esFold,
        effectiveStiffnessAtFold = effectiveFold,
        coupledTangentAtFold = coupledFold,
        tangencyResidual = residual,
        branchEndStroke = search.branchEnd?.stroke,
        branchEndedOnTheField = search.reachedDiffuseCeiling,
        concentratedCrossoverBias = crossover,
        concentratedCrossoverBeyondFold = crossoverBeyond,
        pointIonBias = TRUSTED_BIAS_CEILING,
        bindingCeiling = binding?.name,
        usableBias = binding?.bias,
        biasMargin = biasMargin(binding?.bias, operating?.appliedBias),
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
    onStableSide == false -> "FAIL Q3 — the fold sits SHALLOWER than §3's 3 nm target stroke"
    !usable -> "FAIL Q3 — the operating bias exceeds the binding ceiling ($binding)"
    stableOnMinimum == false -> "PASS Q3, FAIL Q2 — stable at the held point, but the tangent " +
            "minimum over the operating range is below |k_eff| there"
    else -> "PASS"
}

// ---------------------------------------------------------------------------------------------
// Q5 — the escape, priced
// ---------------------------------------------------------------------------------------------

private fun escapeRecords(): List<T76EscapeRecord> =
    STANDOFF_LENGTHS.flatMap { length ->
        listOf(FlexureOrientation.FAVOURABLE, FlexureOrientation.ADVERSE).map { orientation ->
            val line = gen1CouplingLine(
                "escape", coupled = true, orientation = orientation, standoffLength = length
            )
            val tangent = line.tangent(GEN1_ACCEPTABLE_STROKE)
            T76EscapeRecord(
                standoffLength = length,
                orientation = orientation.name,
                span = line.span,
                secantAcceptable = line.secant(GEN1_ACCEPTABLE_STROKE),
                tangentAcceptable = tangent,
                tangentToSecant = line.tangentToSecant(GEN1_ACCEPTABLE_STROKE),
                minimumTangentFullRange =
                    line.tangentMinimum(0.0, GEN1_DESIRED_STROKE).stiffness,
                complianceCeiling = GEN1_COMPLIANCE_CEILING,
                clearsComplianceCeiling = tangent <= GEN1_COMPLIANCE_CEILING,
                exceedanceFactor = tangent / GEN1_COMPLIANCE_CEILING
            )
        }
    }

// ---------------------------------------------------------------------------------------------
// gate 5 — the upstream reproductions
// ---------------------------------------------------------------------------------------------

/** `C-0030`'s published design table, at the recommended `ℓ = 8 nm`. */
private val C0030 = listOf(
    Triple("C-0030 coupled favourable span [nm]", 31.82, "L3 coupled favourable (C-0030)"),
    Triple("C-0030 coupled favourable tangent at 3 nm [pN/nm]", 25.23, "L3 coupled favourable (C-0030)"),
    Triple("C-0030 coupled favourable t/s at 3 nm", 0.757, "L3 coupled favourable (C-0030)"),
    Triple("C-0030 coupled favourable secant at 10 nm [pN/nm]", 29.81, "L3 coupled favourable (C-0030)"),
    Triple("C-0030 coupled favourable minimum tangent [pN/nm]", 22.88, "L3 coupled favourable (C-0030)"),
    Triple("C-0030 coupled favourable minimum-tangent stroke [nm]", 4.55, "L3 coupled favourable (C-0030)"),
    Triple("C-0028 decoupled span [nm]", 31.06, "L2 decoupled (C-0028)"),
    Triple("C-0028 decoupled tangent at 3 nm [pN/nm]", 36.51, "L2 decoupled (C-0028)"),
    Triple("C-0028 decoupled t/s at 3 nm", 1.095, "L2 decoupled (C-0028)"),
    Triple("C-0030 adverse span [nm]", 40.14, "L4 coupled adverse (C-0030)"),
    Triple("C-0030 adverse tangent at 3 nm [pN/nm]", 44.82, "L4 coupled adverse (C-0030)")
)

/** `C-0017`'s stability floors at the 10 nm design point, six-model bracket. */
private val C0017_FLOOR = mapOf(
    0.5 to (3.86 to 15.94),
    2.0 to (23.41 to 27.91)
)

@Suppress("LongMethod")
private fun upstreamChecks(
    couplings: List<T76CouplingRecord>,
    folds: List<T76FoldRecord>
): List<T76UpstreamRecord> {
    val checks = mutableListOf<T76UpstreamRecord>()
    fun add(quantity: String, state: String, here: Double, upstream: Double, source: String) {
        checks += T76UpstreamRecord(
            quantity = quantity,
            state = state,
            here = here,
            upstream = upstream,
            departure = abs(here / upstream - 1.0),
            source = source
        )
    }
    C0030.forEach { (quantity, upstream, lineName) ->
        val record = couplings.first { it.line == lineName }
        val here = when {
            quantity.contains("span") -> record.span
            quantity.contains("minimum-tangent stroke") -> record.minimumTangentFullRangeStroke
            quantity.contains("minimum tangent") -> record.minimumTangentFullRange
            quantity.contains("secant at 10") -> record.secantDesired
            quantity.contains("t/s") -> record.tangentToSecant
            else -> record.tangentAcceptable
        }
        add(quantity, lineName, here, upstream, "C-0030 / C-0028 design table")
    }
    // C-0017's stability floor at 10 nm, re-derived here as |k_eff| at the located operating point
    C0017_FLOOR.forEach { (concentration, bracket) ->
        val rows = folds.filter {
            it.layerHeight == 10.0 && it.concentration == concentration &&
                    it.loadLine == "L1 linear mandate"
        }.mapNotNull { it.stabilityFloor }
        if (rows.isNotEmpty()) {
            add(
                "C-0017 stability floor |k_eff(3 nm)| at 10 nm, six-model minimum [pN/nm]",
                "$concentration mM", rows.min(), bracket.first, "C-0017 (the located floor)"
            )
            add(
                "C-0017 stability floor |k_eff(3 nm)| at 10 nm, six-model maximum [pN/nm]",
                "$concentration mM", rows.max(), bracket.second, "C-0017 (the located floor)"
            )
        }
    }
    // C-0018's own coupled pull-in band at 10 nm / 2 mM, through this study's pipeline
    val c0018 = folds.filter {
        it.layerHeight == 10.0 && it.concentration == 2.0 && it.loadLine == "L1 linear mandate"
    }.mapNotNull { it.pullInBias }
    if (c0018.isNotEmpty()) {
        add(
            "C-0018 pull-in bias at 10 nm / 2 mM, coupled line, minimum [V]",
            "10 nm, 2 mM", c0018.min(), 0.130, "C-0018 (0.130-0.184 V, 6 of 6 models)"
        )
        add(
            "C-0018 pull-in bias at 10 nm / 2 mM, coupled line, maximum [V]",
            "10 nm, 2 mM", c0018.max(), 0.184, "C-0018 (0.130-0.184 V, 6 of 6 models)"
        )
    }
    // C-0017's own located operating bias V*, which is the SAME number on every line here
    val vStar = folds.filter {
        it.layerHeight == 10.0 && it.concentration == 2.0
    }.mapNotNull { it.operatingBias }
    if (vStar.isNotEmpty()) {
        add("C-0017 V* at 10 nm / 2 mM, minimum [V]", "10 nm, 2 mM", vStar.min(), 0.128, "C-0017")
        add("C-0017 V* at 10 nm / 2 mM, maximum [V]", "10 nm, 2 mM", vStar.max(), 0.180, "C-0017")
    }
    // Q1 as an identity: the four lines must locate the SAME operating bias, state by state
    folds.filter { it.loadLine == "L1 linear mandate" }.forEach { reference ->
        folds.filter {
            it.loadLine != "L1 linear mandate" && it.model == reference.model &&
                    it.layerHeight == reference.layerHeight &&
                    it.concentration == reference.concentration
        }.forEach { other ->
            val here = other.operatingBias
            val there = reference.operatingBias
            if (here != null && there != null) add(
                "Q1 placement identity: V* on ${other.loadLine} against the mandate",
                ("%.0f nm, %.1f mM, %s").format(
                    reference.layerHeight, reference.concentration, reference.model
                ),
                here, there, "identity: every line delivers 100 pN at 3 nm"
            )
        }
    }
    return checks
}

// ---------------------------------------------------------------------------------------------
// gate 4 — convergence
// ---------------------------------------------------------------------------------------------

private fun convergence(
    peg: PegWater,
    geometry: ActuatorGeometry,
    tileCharge: Double,
    bjerrum: Double,
    lines: List<StrokeLoadLine>
): List<T76ConvergenceRecord> {
    val records = mutableListOf<T76ConvergenceRecord>()
    val softening = lines.first { it.name.startsWith("L3") }

    // axis 1 — the tangent minimum, this task's own new quantity
    val finestMinimum = softening.tangentMinimum(
        0.0, GEN1_DESIRED_STROKE, coarseSteps = 8192, tolerance = 1e-12
    ).stiffness
    listOf(64, 256, 1024, 4096).forEach { steps ->
        val value = softening.tangentMinimum(
            0.0, GEN1_DESIRED_STROKE, coarseSteps = steps, tolerance = 1e-10
        ).stiffness
        records += T76ConvergenceRecord(
            axis = "tangent-minimum coarse scan steps",
            setting = steps.toString(),
            quantity = "min_s k_tangent [pN/nm]",
            value = value,
            departureFromFinest = abs(value / finestMinimum - 1.0)
        )
    }

    // axis 2, 3, 4 — the fold, on the softening line at the state where it binds
    val model = layerModels(peg).first { it.first.contains("strong-stretching") }.second
    val density = 0.024
    val chain = peg.graftedChain(model.chainLengthForHeight(peg, 10.0, density), density)
    val balance = ActuatorForceBalance(model, chain, geometry)
    val resting = balance.restingHeight
    val strokeCeiling = resting - max(chain.occupiedThickness * 1.01, CURVE_LOWEST_GAP)

    fun locate(nodes: Int, coarse: Int, strokeTolerance: Double, bracket: Double): Double? =
        EquilibriumPath(
            restingHeight = resting,
            strokeCeiling = strokeCeiling,
            field = T76Field(2.0, tileCharge, bjerrum, nodes).asPath(),
            bracketTolerance = bracket
        ) { stroke -> softening.reaction(stroke) + balance.layerLoad(resting - stroke) }
            .fold(coarseSteps = coarse, strokeTolerance = strokeTolerance).pullInBias

    fun axis(name: String, settings: List<Pair<String, Double?>>) {
        val finest = settings.last().second ?: return
        settings.forEach { (label, value) ->
            if (value != null) records += T76ConvergenceRecord(
                axis = name,
                setting = label,
                quantity = "pull-in bias on the softening line [V]",
                value = value,
                departureFromFinest = abs(value / finest - 1.0)
            )
        }
    }
    axis(
        "Poisson-Boltzmann mesh nodes",
        listOf(1000, 2000, 4000).map {
            it.toString() to locate(it, DEFAULT_COARSE_STEPS, DEFAULT_STROKE_TOLERANCE, DEFAULT_DIFFUSE_TOLERANCE)
        }
    )
    axis(
        "fold coarse scan steps",
        listOf(8, 12, 24).map {
            it.toString() to locate(MESH_NODES, it, DEFAULT_STROKE_TOLERANCE, DEFAULT_DIFFUSE_TOLERANCE)
        }
    )
    axis(
        "golden-section stroke bracket [nm]",
        listOf(1e-2, 1e-3, 1e-4, 1e-6).map {
            it.toString() to locate(MESH_NODES, DEFAULT_COARSE_STEPS, it, DEFAULT_DIFFUSE_TOLERANCE)
        }
    )
    return records
}

// ---------------------------------------------------------------------------------------------
// findings, validity, and the report
// ---------------------------------------------------------------------------------------------

@Suppress("LongMethod", "CyclomaticComplexMethod")
private fun findings(result: SofteningCouplingStabilityResult): Map<String, String> {
    val findings = linkedMapOf<String, String>()
    result.couplings.forEach { record ->
        findings["${record.line}: placed / tangent / t-over-s / minimum tangent"] =
            ("secant %.4f, tangent %.4f, t/s %.4f, min %.4f pN/nm at %.3f nm (%s), " +
                    "delivers %.1f pN at 10 nm")
                .format(
                    record.secantAcceptable, record.tangentAcceptable, record.tangentToSecant,
                    record.minimumTangentFullRange, record.minimumTangentFullRangeStroke,
                    if (record.minimumTangentFullRangeInterior) "interior" else "boundary",
                    record.reactionDesired
                )
    }
    val placement = result.couplings.maxOf { it.placementResidual }
    findings["Q1 placement: worst relative departure of the assembled secant from 33.3333"] =
        "%.3e".format(placement)
    val identity = result.upstreamChecks.filter { it.quantity.startsWith("Q1 placement identity") }
    if (identity.isNotEmpty()) findings["Q1 placement identity: worst departure of V* between lines"] =
        ("%.3e over %d comparisons").format(identity.maxOf { it.departure }, identity.size)

    result.folds.groupBy { it.loadLine }.forEach { (line, rows) ->
        val folded = rows.filter { it.pullInBias != null }
        findings["$line: states with a fold"] = "${folded.size} of ${rows.size}"
        val margins = rows.mapNotNull { it.biasMargin }
        if (margins.isNotEmpty()) findings["$line: bias margin over the operating point"] =
            ("%.3f - %.3f").format(margins.min(), margins.max())
        findings["$line: verdicts"] = rows.groupingBy { it.verdict }.eachCount().toString()
        findings["$line: binding ceilings"] =
            rows.groupingBy { it.bindingCeiling ?: "none" }.eachCount().toString()
    }

    // the headline: the 10 nm design point, buffer by buffer, line by line
    listOf(0.5, 2.0, 10.0).forEach { concentration ->
        result.folds.filter { it.layerHeight == 10.0 && it.concentration == concentration }
            .groupBy { it.loadLine }.forEach { (line, rows) ->
                val floors = rows.mapNotNull { it.stabilityFloor }
                val pullIn = rows.mapNotNull { it.pullInBias }
                val margins = rows.mapNotNull { it.biasMargin }
                findings["10 nm, $concentration mM, $line"] =
                    (("floor |k_eff| %.2f - %.2f, tangent %.2f, min tangent %.2f, " +
                            "Q2 stable %d of %d, pull-in %s, bias margin %s")).format(
                        floors.minOrNull() ?: 0.0, floors.maxOrNull() ?: 0.0,
                        rows.first().couplingTangentAtOperating, rows.first().minimumTangent,
                        rows.count { it.stableOnMinimumTangent == true }, rows.size,
                        if (pullIn.isEmpty()) "none"
                        else ("%.4f - %.4f V").format(pullIn.min(), pullIn.max()),
                        if (margins.isEmpty()) "-"
                        else ("%.3f - %.3f").format(margins.min(), margins.max())
                    )
            }
    }

    val q2Failures = result.folds.count { it.stableOnMinimumTangent == false }
    findings["Q2: states whose minimum tangent is below |k_eff| at the operating point"] =
        "$q2Failures of ${result.folds.size}"
    val q2WorkingFailures = result.folds.count { it.stableAtWorkingPointTangent == false }
    findings["Q2 read at the WORKING POINT tangent instead"] =
        "$q2WorkingFailures of ${result.folds.size} — the difference is the whole of CH-0042"
    val shallow = result.folds.count { it.targetStrokeOnStableSide == false }
    findings["Q3: states whose fold sits SHALLOWER than §3's 3 nm target stroke"] =
        "$shallow of ${result.folds.size}"

    val residuals = result.folds.mapNotNull { it.tangencyResidual }
    if (residuals.isNotEmpty()) findings["gate 3: tangency k_c(s) + k_eff = 0 at the located fold"] =
        ("worst relative residual %.3e over %d INTERIOR folds").format(residuals.max(), residuals.size)
    findings["folds at the branch start (a boundary maximum, no tangency to check)"] =
        "${result.folds.count { it.foldAtBranchStart }} of ${result.folds.size}"

    val escape = result.escape.filter { it.orientation == "ADVERSE" }
    findings["Q5 the escape: the adverse mounting against C-0023's 40 pN/nm ceiling"] =
        ("tangent %.2f - %.2f pN/nm over l = 3-10 nm; clears at %d of %d lengths")
            .format(
                escape.minOf { it.tangentAcceptable }, escape.maxOf { it.tangentAcceptable },
                escape.count { it.clearsComplianceCeiling }, escape.size
            )

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

private val CONVENTIONS = listOf(
    "z is normal to the electrode, positive AWAY from it; the electrostatic gap IS the layer " +
            "height, exactly and by construction (C-0012)",
    "the STROKE s = L0 - h is positive DOWNWARD, toward the electrode",
    "L0 is a FORCE-ONSET height: the height at which the layer carries 1.0 pN over the " +
            "40 x 40 nm tile (C-0011, CH-0010)",
    "a LOAD LINE R(s) is positive UPWARD, in pN over the whole 45-path array. Its SECANT R(s)/s " +
            "is what §3's placement clause is written on and its TANGENT dR/ds is what the " +
            "stability clause is written on; they are the same number only for an affine line " +
            "through the origin",
    "all four load lines pass through the SAME operating point, 100 pN at 3 nm, and differ only " +
            "in how they leave it — which is what makes a state-by-state comparison of their " +
            "folds a comparison of one device rather than of four",
    "the flexure's MOUNTING SENSE is C-0030's: FAVOURABLE is the sense in which the midspan sags " +
            "toward the body its standoff bases stand on, which supplies the draw-in and is the " +
            "softening one; ADVERSE is the other",
    "the equilibrium path is parametrised by the STROKE: at each stroke there is one bias " +
            "V_eq(s) that puts an equilibrium there, and the fold is max_s V_eq(s). " +
            "Differentiating the balance at V'(s) = 0 gives k_c(s) + k_eff(s) = 0 exactly, so " +
            "the argmax IS the tangency point — and for a NONLINEAR line k_c is the tangent AT " +
            "THAT STROKE and not a constant",
    "k_es = -dF_z/dh is NEGATIVE above the force maximum and POSITIVE below it (CH-0011)",
    "a bias ceiling belongs to a (bias, load line) pair, never to the bias alone (CH-0015)",
    "where k_eff >= 0 there is NO stability requirement at all; the margin is recorded as null, " +
            "not as an infinity (CLAUDE.md)"
)

private val CITED = listOf(
    "C-0017's mandated output-coupling stiffness, 100 pN / 3 nm = 33.3333 pN/nm — CITED, itself " +
            "derived there from §3 alone. It is the placement target every line is solved to",
    "C-0017's stability floors at the 10 nm design point, 23.41-27.91 pN/nm at 2 mM and " +
            "3.86-15.94 at 0.5 mM — CITED, and re-derived here as a gate-5 check",
    "C-0018's pull-in band at 10 nm / 2 mM, 0.130-0.184 V — CITED, and reproduced here through " +
            "this study's own pipeline on the affine line",
    "C-0030's design table (span 31.82 nm, tangent 25.23, t/s 0.757, minimum 22.88 at 4.55 nm, " +
            "adverse tangent 44.82) — CITED, and reproduced as gate-5 tests",
    "C-0023's 40 pN/nm compliance ceiling on the assembled tangent — CITED, and the axis the " +
            "escape is priced on",
    "C-0002's semidilute-to-concentrated crossover phi = 0.2 — CITED, read as a ceiling",
    "CH-0007's point-ion boundary in APPLIED bias, ~1.0 V — CITED. It never binds",
    "C-0005's one-loop correction, 123-214% of the leading term — CITED. It is larger than " +
            "every margin in this file",
    "the Stern capacitance ~20 uF/cm2 — CITED, and load-bearing for the diffuse-drop to " +
            "applied-bias mapping",
    "Manning surviving fraction 11.90% — CITED FROM C-0005 via C-0008",
    "duplex EI = 230 pN nm2 (CanDo MODEL INPUT, not a measurement) and S = 1100 pN (MEASURED, " +
            "Wang et al. 1997) — CITED via C-0009/Gen1Tile",
    "A2, A3, alpha = 1.9e-3, 2.0e-2, 0.49 — CITED FROM C-0003/C-0002",
    "§3's targets: 100 pN, 3 nm acceptable, 10 nm desired, 40 x 40 nm tile, 5/7/10 nm layer — CITED"
)

private val VALIDITY = listOf(
    "TRL 1-3. Model-consistent and traceable. NOTHING HERE IS MEASURED, and the MOTIF is not " +
            "demonstrated either: C-0028's literature finding stands unchanged.",
    "Mean field, inherited whole from C-0008 and C-0005: the one-loop correction is 123-214% of " +
            "the leading term over the whole 5-10 nm range for Mg2+. Every margin here is " +
            "smaller than that.",
    "L0 is a FORCE-ONSET height at a defining load of 1.0 pN over the tile (C-0011, CH-0010).",
    "The layer is C-0003's, at C-0001's single grafting density per height — NOT C-0011's solved " +
            "SCF profile. Deliberate, and the same choice C-0017 and C-0018 made: the load line " +
            "must be drawn across the same characteristic C-0012 computed.",
    "SMALL DEFLECTION. C-0030 records the standoff head's rotation at the 10 nm desired stroke " +
            "as 0.63-0.68 rad and its translation as 32-41% of its own length, so every number " +
            "read at a stroke beyond ~5 nm is a linear-theory extrapolation. The 3 nm placement " +
            "point and the tangent there are inside small deflection; the tangent MINIMUM at " +
            "4.55 nm is at its edge, and the 10 nm secant is outside it.",
    "The load line is the tile MEAN under a uniform load, the one case in which C-0006's tile is " +
            "rigid. A real 45-attachment coupling dishes the tile (C-0015, T-17).",
    "Static only. A bias step faster than drainage can carry the tile past a fold a " +
            "quasi-static ramp stops at; C-0004's corner is 91 kHz-2.3 MHz.",
    "No preload (T-13). C-0030's element is two-sided but its law is NOT odd, so a preload would " +
            "not move the two limbs equally.",
    "1-D. No edge, no fringing, no lateral load profile (T-3b, T-60).",
    "The mounting sense is a SPECIFICATION GAP, not a modelling one (C-0030): §3 does not say " +
            "which body carries the standoffs, and it is worth the whole of the difference " +
            "between L3 and L4 here. T-75.",
    "The diffuse-layer drop is capped at 0.35 V, the same bracket C-0008's own Stern inversion " +
            "uses. A state needing more is reported as a branch end, not extrapolated."
)

private val OPEN = listOf(
    "The 0.5 mM recommendation is a SPECIFICATION question and not a calculation (T-63). This " +
            "task adds a sixth independent route to it and cannot answer it.",
    "T-75 owns the mounting sense, and it decides which of L3 and L4 the device actually has.",
    "The tangent minimum sits at a stroke where C-0030's own small-deflection validity is at its " +
            "edge; a large-deflection solve would move it, and its direction is not known.",
    "The dynamic pull-in is not computed, and a softening coupling has a different dynamic " +
            "signature from a stiffening one.",
    "C-0019's one-loop softening of k_brush and C-0022's finite-tile enhancement of |F_es| are " +
            "NOT carried here, exactly as C-0018 did not carry them; C-0027 reports they cancel " +
            "at the fold to within the collar gradient's own spread."
)

private fun report(result: SofteningCouplingStabilityResult, output: File) {
    println()
    println("T-76 — ${result.title}")
    println(
        "  ${result.couplings.size} coupling records, ${result.folds.size} fold records, " +
                "${result.escape.size} escape, ${result.upstreamChecks.size} upstream checks, " +
                "${result.convergence.size} convergence"
    )
    result.findings.forEach { (key, value) -> println("  $key: $value") }
    println("written to $output")
}
