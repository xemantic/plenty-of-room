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
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

/**
 * Task `T-4` — electrostatic softening and pull-in: the **maximum usable bias, with margin**,
 * separated into the three ceilings it is actually made of. Leaf `A2.2`.
 *
 * Run with:
 *
 * ```shell
 * ./gradlew study -Pstudy=actuator.MaximumUsableBiasStudyKt -PbuildDirectory=build-t4
 * ```
 *
 * Emits `gpd/results/T-4-maximum-usable-bias.json`, deterministically — no timestamp, every
 * floating-point number rounded at the serialisation boundary by [roundedForActuatorResult],
 * and every decision (the binding ceiling) taken on the **rounded** values with declaration
 * order as the tie-break.
 *
 * Consumes `C-0012`'s pipeline (`ActuatorForceBalance`, `ActuatorGeometry`, `brush/`,
 * `electrostatics/`, `material/`) and `C-0017`'s mandated coupling stiffness as libraries,
 * **re-run rather than tabulated**. Owns `PullInStability.kt` and this file, and edits nothing.
 */

// ---------------------------------------------------------------------------------------------
// records
// ---------------------------------------------------------------------------------------------

/** The three ceilings at one `(layer height, model, buffer, load line)` state. */
@Serializable
@Suppress("LongParameterList")
data class UsableBiasRecord(
    val model: String,
    val layerHeight: Double,
    val graftingDensity: Double,
    val concentration: Double,
    val loadLine: String,
    val couplingStiffness: Double,
    val deadLoad: Double,
    val restingHeight: Double,
    val dryThickness: Double,
    val strokeCeiling: Double,
    // the operating point §3 asks for
    val operatingBias: Double?,
    val operatingGap: Double?,
    val operatingVolumeFraction: Double?,
    // ceiling 1 — static stability
    val pullInBias: Double?,
    val pullInStroke: Double?,
    val pullInGap: Double?,
    val foldAtBranchStart: Boolean,
    val stableShallowBranchExists: Boolean,
    val branchEndStroke: Double?,
    val branchEndBias: Double?,
    val branchEndedOnTheField: Boolean,
    val brushStiffnessAtFold: Double?,
    val electrostaticStiffnessAtFold: Double?,
    val effectiveStiffnessAtFold: Double?,
    val coupledTangentAtFold: Double?,
    val tangencyResidual: Double?,
    val forceDecayLengthAtFold: Double?,
    // ceiling 2 — upstream validity, re-read at THIS load line's operating point
    val correlationBandBias: Double?,
    val correlationBandBeyondFold: Boolean,
    val concentratedCrossoverBias: Double?,
    val concentratedCrossoverBeyondFold: Boolean,
    val pointIonBias: Double,
    // ceiling 3 — electrochemistry, quoted as a bound
    val electrochemicalBias: Double,
    // the verdict
    val bindingCeiling: String?,
    val usableBias: Double?,
    val margin: Double?,
    val operatingPointIsUsable: Boolean?,
    val searchEvaluations: Int
)

/** `CH-0011` made executable: where `k_es` changes sign, and where the force turns repulsive. */
@Serializable
data class SmallGapRecord(
    val concentration: Double,
    val appliedBias: Double,
    val forceMaximumGap: Double?,
    val maximumAttraction: Double?,
    val electrostaticStiffnessAbovePeak: Double?,
    val electrostaticStiffnessBelowPeak: Double?,
    val signReversesAtTheMaximum: Boolean,
    val repulsionOnsetGap: Double?,
    val attractionAtSampledFloor: Double,
    val sampledFloor: Double
)

/**
 * Which stopper the descending tile meets first — the electrostatic one or the osmotic one.
 *
 * Both mechanisms are sufficient on their own, so the question `CH-0011` poses is not which
 * exists but which acts at the **larger gap**, i.e. which the tile meets first on its way down.
 * The electrostatic stopper proper is the **repulsion onset**, where the field can no longer
 * push the tile at all; the force maximum is reported beside it because that is where `k_es`
 * changes sign, which is the other half of `CH-0011`'s statement.
 */
@Serializable
data class ArrestRecord(
    val model: String,
    val layerHeight: Double,
    val concentration: Double,
    val appliedBias: Double,
    val forceMaximumGap: Double?,
    val repulsionOnsetGap: Double?,
    val osmoticStopperGap: Double?,
    val arrestedBy: String,
    val separation: Double?
)

/** `C-0008`, `C-0012` and `C-0017` reproduced through this solver — the gate-5 record. */
@Serializable
data class UpstreamCheckRecord(
    val quantity: String,
    val model: String,
    val layerHeight: Double,
    val concentration: Double,
    val here: Double,
    val upstream: Double,
    val departure: Double,
    val source: String
)

/** One convergence axis, referred to **its own** finest setting. */
@Serializable
data class ConvergenceRecord(
    val axis: String,
    val setting: String,
    val pullInBias: Double,
    val departureFromFinest: Double
)

/** The `T-4` result envelope — the same shape every study in this project writes through. */
@Serializable
@Suppress("LongParameterList")
data class MaximumUsableBiasResult(
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
    val ceilings: List<UsableBiasRecord>,
    val smallGap: List<SmallGapRecord>,
    val arrest: List<ArrestRecord>,
    val upstreamChecks: List<UpstreamCheckRecord>,
    val convergence: List<ConvergenceRecord>,
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
 * Leaf `A2.2`'s low-screening point, §3's nominal buffer, and §3's high one.
 *
 * 10 mM is kept even though `C-0012` shows §3's 100 pN target is unreachable there at 7 and
 * 10 nm: **a pull-in ceiling exists whether or not the force target is met**, and leaving the
 * strongest buffer out would hide the direction the ceiling moves in.
 */
private val BUFFERS = listOf(0.5, 2.0, 10.0)

/** §3's force and stroke targets. */
private const val TARGET_FORCE = 100.0

private const val TARGET_STROKE = 3.0

/** `C-0017`: the coupling §3 fixes by arithmetic, `100 pN / 3 nm`. */
private val MANDATED_COUPLING = TARGET_FORCE / TARGET_STROKE

/** `CH-0007`'s point-ion boundary **in applied bias**, not in diffuse-layer drop. */
private const val TRUSTED_BIAS_CEILING = 1.0

/** `T-11`: the thermodynamic aqueous electrochemical window, quoted as a bound. */
private const val ELECTROCHEMICAL_CEILING = 1.23

/** `C-0005`'s lateral counterion spacing — below this gap PB cannot produce the physics. */
private const val CORRELATION_ATTRACTION_GAP = 1.46

/** `C-0002`'s semidilute→concentrated crossover, read as a ceiling per §2's second caveat. */
private const val CONCENTRATED_CROSSOVER = 0.2

private const val STERN_CAPACITANCE = 20.0

/**
 * The Poisson-Boltzmann mesh this study runs on.
 *
 * `T-16` reports the coupling margin moving by `7.3e−6` between 2000 and 4000 nodes and
 * `1.5e−6` between 4000 and 8000; the convergence record below repeats that measurement on
 * *this* task's own quantity, the pull-in bias. 2000 is chosen because the sweep is 162 fold
 * searches deep and the mesh cost is linear in none of the physics.
 */
private const val MESH_NODES = 2000

/** The smallest gap the field is asked about, in nm — `C-0012`'s own force-curve floor. */
private const val CURVE_LOWEST_GAP = 0.5

/** The small-gap diagnostics reach below the force curve's floor, because `CH-0011` is there. */
private const val DIAGNOSTIC_LOWEST_GAP = 0.35

private const val OSMOTIC_SECOND_VIRIAL = 1.9e-3

private const val OSMOTIC_THIRD_VIRIAL = 2.0e-2

/**
 * The biases the small-gap diagnostics are read at — `C-0012`'s own grid, extended **both** ways.
 *
 * Down to 0.02 V because that is where `C-0012` located the repulsion onset at 1.107 nm, and up
 * to 1.0 V because that is where the *collapse* happens: an arrest is a statement about the
 * post-fold state, and reading it only at biases below the fold would answer a different
 * question. 1.0 V is `CH-0007`'s point-ion boundary and the largest bias any of this is
 * trustworthy at.
 */
private val DIAGNOSTIC_BIASES = listOf(0.02, 0.05, 0.10, 0.25, 0.50, 1.00)

/** A load line, in the two numbers that define an affine one. */
private data class LoadLine(
    val name: String,
    val stiffness: Double,
    val preload: Double
) {

    fun reaction(stroke: Double): Double = preload + stiffness * stroke
}

/**
 * The three load lines the same actuator is read against.
 *
 * `CH-0015` says a bias ceiling must be quoted with the load it was evaluated at. This is that
 * statement made executable: the **coupled** line is the device `C-0017` closed the programme
 * on, the **dead load** is the constant-force load `C-0012`'s `k_eff < 0` table describes, and
 * the **free** line is the unloaded tile whose ceiling `C-0012` reported and `CH-0015` flagged.
 * The first two pass through the *same* operating point — 100 pN at 3 nm — and differ only in
 * slope, which is the whole of the finding.
 */
private val LOAD_LINES = listOf(
    LoadLine("coupled", MANDATED_COUPLING, 0.0),
    LoadLine("dead-load", 0.0, TARGET_FORCE),
    LoadLine("free", 0.0, 0.0)
)

/**
 * The field, parametrised by the **diffuse-layer drop** — one Poisson-Boltzmann solve per sample.
 *
 * [forceAtBias] is the other direction, which needs `C-0008`'s Stern-series inversion and costs
 * 34 solves; it is used only where the applied bias is the given, i.e. for the finite-difference
 * `k_es` at the fold and for the small-gap diagnostics.
 */
private class Field(
    concentration: Double,
    val tileCharge: Double,
    val bjerrum: Double,
    val nodes: Int = MESH_NODES
) {

    private val ions = IonModel(MagnesiumChlorideBuffer(concentration).magnesiumNumberDensity)

    private val medium = uniformMedium(GapMedium())

    private val stern = sternChargeDensityPerVolt(STERN_CAPACITANCE)

    private val volt = thermalVoltage()

    /** One solve: the force **and** the applied bias that produced it. */
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

private const val FOOTPRINT = 1600.0

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
    val fields = BUFFERS.associateWith { Field(it, tileCharge, lb) }

    println("T-4 — locating the fold of the equilibrium path, load line by load line ...")
    val ceilings = mutableListOf<UsableBiasRecord>()
    DESIGN_POINTS.forEach { (height, density) ->
        models.forEach { (name, model) ->
            val chain = peg.graftedChain(model.chainLengthForHeight(peg, height, density), density)
            val balance = ActuatorForceBalance(model, chain, geometry)
            BUFFERS.forEach { concentration ->
                val field = fields.getValue(concentration)
                LOAD_LINES.forEach { line ->
                    ceilings += ceiling(name, height, density, concentration, balance, chain, field, line)
                }
            }
            println("  ${height} nm  ${name} done")
        }
    }

    println("T-4 — CH-0011: the force maximum, the sign of k_es, and the repulsion onset ...")
    val smallGap = BUFFERS.flatMap { concentration ->
        DIAGNOSTIC_BIASES.map { bias -> smallGap(fields.getValue(concentration), concentration, bias) }
    }

    println("T-4 — which stopper the descending tile meets first ...")
    val arrest = mutableListOf<ArrestRecord>()
    DESIGN_POINTS.forEach { (height, density) ->
        models.forEach { (name, model) ->
            val chain = peg.graftedChain(model.chainLengthForHeight(peg, height, density), density)
            val balance = ActuatorForceBalance(model, chain, geometry)
            smallGap.forEach { record ->
                arrest += arrest(name, height, balance, chain.occupiedThickness, record)
            }
        }
    }

    println("T-4 — reproducing C-0008, C-0012 and C-0017 through this solver ...")
    val upstream = upstreamChecks(fields, ceilings)

    println("T-4 — convergence ...")
    val convergence = convergence(peg, geometry, tileCharge, lb)

    val result = MaximumUsableBiasResult(
        task = "T-4",
        leaf = "A2.2",
        title = "Electrostatic softening and pull-in: the maximum usable bias of the Gen-1 " +
                "actuator, separated into a static-stability ceiling, an upstream-validity " +
                "ceiling and an electrochemical bound, each quoted with the load line it " +
                "belongs to",
        verificationType = "in-silico (the equilibrium path of C-0012's coupled balance " +
                "parametrised by the STROKE and its fold located as the maximum of the bias " +
                "along it, graded against the tangency condition k_c + k_eff = 0 computed " +
                "independently by finite difference at fixed applied bias) + logical",
        acceptance = "P1: the maximum usable bias at every (layer height, layer model, buffer, " +
                "load line) state, as three separated ceilings — static stability (the fold of " +
                "the equilibrium path under THAT load line), upstream validity (C-0005's 1.46 nm " +
                "correlation band and C-0002's phi = 0.2 crossover, re-read at the operating " +
                "point of THAT load line) and CH-0007's point-ion boundary — with the margin to " +
                "the bias that delivers §3's own targets. P2: the coupled ceiling (k_c + k_eff, " +
                "C-0017's 33.333 pN/nm) delivered as the headline and the unloaded one beside " +
                "it. P3: CH-0011 settled as an executable check — the collapse is arrested by " +
                "k_es reversing sign, or it is not.",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated. " +
                "Every force inherits C-0008's mean-field statement in full: C-0005 puts the " +
                "one-loop correction at 123-214% of the leading term across this gap range, " +
                "which is larger than every margin reported here. NOTHING HERE IS MEASURED.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm (= 1 mN/m exactly)",
            "potential" to "V",
            "concentration" to "mM",
            "temperature" to "K"
        ),
        conventions = listOf(
            "z is normal to the electrode, positive AWAY from it; the electrode surface is z = 0",
            "the electrostatic gap IS the layer height, exactly and by construction (C-0012)",
            "the STROKE s = L0 - h is positive DOWNWARD, toward the electrode",
            "L0 is a FORCE-ONSET height: the height at which the layer carries 1.0 pN over the " +
                    "40 x 40 nm tile (C-0011, CH-0010)",
            "the LOAD LINE R(s) is positive UPWARD. free: R = 0. dead-load: R = 100 pN, which is " +
                    "the constant-force load C-0012's k_eff table describes. coupled: " +
                    "R = 33.333 s, C-0017's own mandated output coupling. The coupled and " +
                    "dead-load lines pass through the SAME operating point, 100 pN at 3 nm, and " +
                    "differ only in slope",
            "the equilibrium path is parametrised by the STROKE: at each stroke there is one bias " +
                    "V_eq(s) that puts an equilibrium there, and the fold is max_s V_eq(s). " +
                    "Differentiating the balance at V'(s) = 0 gives k_c + k_eff = 0 exactly, so " +
                    "the argmax IS the tangency point",
            "k_es = -dF_z/dh is NEGATIVE above the force maximum and POSITIVE below it (CH-0011); " +
                    "every sign here is quoted with the gap it applies to",
            "a bias ceiling belongs to a (bias, load line) pair, never to the bias alone (CH-0015)"
        ),
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
            "loadLines" to LOAD_LINES.map { "${it.name}: R = ${it.preload} + ${it.stiffness} s" }
                .toString(),
            "targetForce" to TARGET_FORCE.toString(),
            "targetStroke" to TARGET_STROKE.toString(),
            "mandatedCouplingStiffness" to MANDATED_COUPLING.toString(),
            "trustedBiasCeiling" to TRUSTED_BIAS_CEILING.toString(),
            "electrochemicalCeiling" to ELECTROCHEMICAL_CEILING.toString(),
            "correlationAttractionGap" to CORRELATION_ATTRACTION_GAP.toString(),
            "concentratedCrossover" to CONCENTRATED_CROSSOVER.toString(),
            "diffuseCeiling" to DEFAULT_DIFFUSE_CEILING.toString(),
            "diffuseBracketTolerance" to DEFAULT_DIFFUSE_TOLERANCE.toString(),
            "foldCoarseSteps" to DEFAULT_COARSE_STEPS.toString(),
            "foldStrokeTolerance" to DEFAULT_STROKE_TOLERANCE.toString(),
            "curveLowestGap" to CURVE_LOWEST_GAP.toString(),
            "diagnosticLowestGap" to DIAGNOSTIC_LOWEST_GAP.toString(),
            "diagnosticBiases" to DIAGNOSTIC_BIASES.toString()
        ),
        citedInputs = CITED,
        ceilings = ceilings,
        smallGap = smallGap,
        arrest = arrest,
        upstreamChecks = upstream,
        convergence = convergence,
        findings = emptyMap(),
        validity = VALIDITY,
        openQuestions = OPEN
    )
    val complete = result.copy(findings = findings(result))
    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-4-maximum-usable-bias.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(json.encodeToJsonElement(complete).roundedForActuatorResult()) + "\n"
    )
    report(complete, output)
}

// ---------------------------------------------------------------------------------------------
// the ceilings
// ---------------------------------------------------------------------------------------------

@Suppress("LongParameterList", "LongMethod")
private fun ceiling(
    name: String,
    height: Double,
    density: Double,
    concentration: Double,
    balance: ActuatorForceBalance,
    chain: com.xemantic.nano.plentyofroom.brush.GraftedChain,
    field: Field,
    line: LoadLine
): UsableBiasRecord {
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
    val operating = if (TARGET_STROKE <= strokeCeiling) path.at(TARGET_STROKE) else null

    // the fold read differentially, which is an independent route to the same point
    val brush = fold?.let { balance.layerStiffness(it.gap) }
    val electrostatic = fold?.let { field.stiffnessAtBias(it.gap, it.appliedBias) }
    val effective = if (brush != null && electrostatic != null) brush + electrostatic else null
    val coupledTangent = effective?.let { line.stiffness + it }
    // The tangency identity `k_c + k_eff = 0` holds at an INTERIOR maximum of the path. Where the
    // maximum is at the branch start the derivative need not vanish there at all — it is a
    // boundary maximum — so no residual is reported rather than a meaningless one. Scaled by the
    // three stiffnesses that make it up, because two of the load lines have `k_c = 0` and a
    // strong-stretching layer can have `k_brush` near zero, so their sum is not a scale.
    val residual = if (search.foldAtBranchStart) null else coupledTangent?.let {
        abs(it) / max(
            line.stiffness + abs(brush ?: 0.0) + abs(electrostatic ?: 0.0), 1e-12
        )
    }
    // `ℓ = |F_es| over k_es`, C-0008's own definition, at the fold
    val decayLength = if (fold != null && electrostatic != null && electrostatic != 0.0)
        -fold.attraction / electrostatic else null

    // ceiling 2 — the upstream ranges, re-read on THIS load line's own path
    val foldStroke = fold?.stroke ?: strokeCeiling
    val correlation = validityBias(path, resting, CORRELATION_ATTRACTION_GAP, strokeCeiling)
    val crossoverGap = chain.occupiedThickness / CONCENTRATED_CROSSOVER
    val crossover = validityBias(path, resting, crossoverGap, strokeCeiling)
    val correlationBeyond = (resting - CORRELATION_ATTRACTION_GAP) > foldStroke
    val crossoverBeyond = (resting - crossoverGap) > foldStroke

    val candidates = listOf(
        BiasCeiling("static stability (pull-in)", fold?.appliedBias),
        BiasCeiling("correlation band (C-0005, 1.46 nm)", if (correlationBeyond) null else correlation),
        BiasCeiling("concentrated crossover (C-0002, phi = 0.2)", if (crossoverBeyond) null else crossover),
        BiasCeiling("point-ion boundary (CH-0007, 1.0 V)", TRUSTED_BIAS_CEILING)
    )
    val binding = bindingCeiling(candidates)
    return UsableBiasRecord(
        model = name,
        layerHeight = height,
        graftingDensity = density,
        concentration = concentration,
        loadLine = line.name,
        couplingStiffness = line.stiffness,
        deadLoad = line.preload,
        restingHeight = resting,
        dryThickness = chain.occupiedThickness,
        strokeCeiling = strokeCeiling,
        operatingBias = operating?.appliedBias,
        operatingGap = operating?.gap,
        operatingVolumeFraction = operating?.let { chain.meanVolumeFraction(it.gap) },
        pullInBias = fold?.appliedBias,
        pullInStroke = fold?.stroke,
        pullInGap = fold?.gap,
        foldAtBranchStart = search.foldAtBranchStart,
        // A fold at the branch start is not a ceiling with room under it: the path descends from
        // zero stroke, so EVERY compressed equilibrium under this load line is on the unstable
        // branch and the quoted bias is only where the unstable branch meets zero stroke — which,
        // for a dead load, is C-0008's blocking bias. The device has no shallow stable state at
        // any bias there, and a margin above 1 must not be read as one.
        stableShallowBranchExists = !search.foldAtBranchStart,
        branchEndStroke = search.branchEnd?.stroke,
        branchEndBias = search.branchEnd?.appliedBias,
        branchEndedOnTheField = search.reachedDiffuseCeiling,
        brushStiffnessAtFold = brush,
        electrostaticStiffnessAtFold = electrostatic,
        effectiveStiffnessAtFold = effective,
        coupledTangentAtFold = coupledTangent,
        tangencyResidual = residual,
        forceDecayLengthAtFold = decayLength,
        correlationBandBias = correlation,
        correlationBandBeyondFold = correlationBeyond,
        concentratedCrossoverBias = crossover,
        concentratedCrossoverBeyondFold = crossoverBeyond,
        pointIonBias = TRUSTED_BIAS_CEILING,
        electrochemicalBias = ELECTROCHEMICAL_CEILING,
        bindingCeiling = binding?.name,
        usableBias = binding?.bias,
        margin = biasMargin(binding?.bias, operating?.appliedBias),
        // usable means BOTH below every ceiling AND on the stable side of the fold: a state whose
        // fold sits at a shallower stroke than §3's 3 nm has an equilibrium there and it is a
        // maximum of the potential, not a minimum
        operatingPointIsUsable = operating?.let { point ->
            binding?.bias?.let { ceiling ->
                point.appliedBias <= ceiling &&
                        (fold == null || TARGET_STROKE <= fold.stroke + 1e-9)
            }
        },
        searchEvaluations = path.evaluations
    )
}

/** The bias at which the path reaches [gap], or `null` when it never does. */
private fun validityBias(
    path: EquilibriumPath,
    resting: Double,
    gap: Double,
    strokeCeiling: Double
): Double? {
    val stroke = resting - gap
    if (stroke <= 0.0 || stroke > strokeCeiling) return null
    return path.at(stroke)?.appliedBias
}

// ---------------------------------------------------------------------------------------------
// CH-0011 — the small-gap diagnostics
// ---------------------------------------------------------------------------------------------

private fun smallGap(field: Field, concentration: Double, bias: Double): SmallGapRecord {
    val peak = forceMaximumGap(DIAGNOSTIC_LOWEST_GAP, 6.0, coarseSteps = 24, tolerance = 1e-4) {
        -field.forceAtBias(it, bias)
    }
    val onset = repulsionOnsetGap(DIAGNOSTIC_LOWEST_GAP, 6.0, coarseSteps = 24, tolerance = 1e-6) {
        field.forceAtBias(it, bias)
    }
    val above = peak?.let { field.stiffnessAtBias(it + 0.2, bias) }
    val below = peak?.let { if (it - 0.2 > 0.1) field.stiffnessAtBias(it - 0.15, bias) else null }
    return SmallGapRecord(
        concentration = concentration,
        appliedBias = bias,
        forceMaximumGap = peak,
        maximumAttraction = peak?.let { -field.forceAtBias(it, bias) },
        electrostaticStiffnessAbovePeak = above,
        electrostaticStiffnessBelowPeak = below,
        signReversesAtTheMaximum = above != null && below != null && above < 0.0 && below > 0.0,
        repulsionOnsetGap = onset,
        attractionAtSampledFloor = -field.forceAtBias(DIAGNOSTIC_LOWEST_GAP, bias),
        sampledFloor = DIAGNOSTIC_LOWEST_GAP
    )
}

/**
 * Which stopper the descending tile meets first.
 *
 * The **electrostatic** stopper is the gap at which `|F_es|` stops growing — below it the field
 * pulls *less* the closer the tile comes. The **osmotic** one is the gap at which the layer's own
 * load would balance the largest attraction the field can ever exert at that bias, which is the
 * counterfactual `CH-0011` needs: it is where the tile would stop *if the force never fell*.
 * Whichever sits at the larger gap is the one the tile meets first, and that is the arrest.
 */
private fun arrest(
    name: String,
    height: Double,
    balance: ActuatorForceBalance,
    dryThickness: Double,
    record: SmallGapRecord
): ArrestRecord {
    val maximum = record.maximumAttraction ?: record.attractionAtSampledFloor
    val osmotic = osmoticStopperGap(balance, dryThickness, maximum)
    // the electrostatic stopper is the gap below which the field is repulsive; where it sits
    // below the sampled floor there is no electrostatic stop inside the model at all
    val electrostatic = record.repulsionOnsetGap
    val arrestedBy = when {
        osmotic == null -> "not resolved"
        electrostatic == null -> "osmotic (no electrostatic stop above the sampled floor)"
        electrostatic > osmotic -> "electrostatic (the field reverses first)"
        else -> "osmotic (the layer wins first)"
    }
    return ArrestRecord(
        model = name,
        layerHeight = height,
        concentration = record.concentration,
        appliedBias = record.appliedBias,
        forceMaximumGap = record.forceMaximumGap,
        repulsionOnsetGap = electrostatic,
        osmoticStopperGap = osmotic,
        arrestedBy = arrestedBy,
        separation = if (electrostatic != null && osmotic != null) electrostatic - osmotic else null
    )
}

/** The gap at which the layer alone carries [load] pN, bisected on the bracket width. */
private fun osmoticStopperGap(
    balance: ActuatorForceBalance,
    dryThickness: Double,
    load: Double
): Double? {
    var low = dryThickness * 1.0001
    var high = balance.restingHeight
    if (balance.layerLoad(high) >= load) return high
    if (balance.layerLoad(low) <= load) return null
    while (high - low > 1e-9) {
        val middle = 0.5 * (low + high)
        if (balance.layerLoad(middle) > load) low = middle else high = middle
    }
    return 0.5 * (low + high)
}

// ---------------------------------------------------------------------------------------------
// gate 5 — the upstream reproductions
// ---------------------------------------------------------------------------------------------

/** `C-0008`'s bias for a 100 pN blocking force at 2 mM, at §3's three nominal gaps. */
private val C0008_BLOCKING_BIAS = mapOf(5.0 to 0.067, 7.0 to 0.113, 10.0 to 0.679)

/** `C-0008`'s `F_es` at 2 mM and 0.10 V, at the same three gaps — `C-0012` reproduces it too. */
private val C0008_FORCE_AT_TENTH_VOLT = mapOf(5.0 to 167.2, 7.0 to 86.7, 10.0 to 34.5)

private fun upstreamChecks(
    fields: Map<Double, Field>,
    ceilings: List<UsableBiasRecord>
): List<UpstreamCheckRecord> {
    val checks = mutableListOf<UpstreamCheckRecord>()
    val field = fields.getValue(2.0)
    C0008_BLOCKING_BIAS.forEach { (gap, upstream) ->
        val here = holdingBias(field.asPath(), gap, TARGET_FORCE)?.appliedBias
        if (here != null) checks += UpstreamCheckRecord(
            quantity = "applied bias for a 100 pN blocking force at the nominal gap",
            model = "field only",
            layerHeight = gap,
            concentration = 2.0,
            here = here,
            upstream = upstream,
            departure = abs(here / upstream - 1.0),
            source = "C-0008 (table: bias needed for 100 pN, 2 mM)"
        )
    }
    C0008_FORCE_AT_TENTH_VOLT.forEach { (gap, upstream) ->
        val here = -field.forceAtBias(gap, 0.10)
        checks += UpstreamCheckRecord(
            quantity = "|F_es| at 0.10 V and the nominal gap",
            model = "field only",
            layerHeight = gap,
            concentration = 2.0,
            here = here,
            upstream = upstream,
            departure = abs(here / upstream - 1.0),
            source = "C-0012 (blocking force at 2 mM, 0.10 V), itself reproducing C-0008"
        )
    }
    // C-0017's located operating bias V*, which is this task's `operatingBias` on the coupled and
    // the dead-load lines alike — they pass through the same point, so both must return it
    val c0017 = mapOf(
        Triple(5.0, 2.0, "min") to 0.128, Triple(5.0, 2.0, "max") to 0.349,
        Triple(7.0, 2.0, "min") to 0.083, Triple(7.0, 2.0, "max") to 0.157,
        Triple(10.0, 2.0, "min") to 0.128, Triple(10.0, 2.0, "max") to 0.180
    )
    listOf(5.0, 7.0, 10.0).forEach { height ->
        val biases = ceilings.filter {
            it.layerHeight == height && it.concentration == 2.0 && it.loadLine == "coupled"
        }.mapNotNull { it.operatingBias }
        if (biases.isNotEmpty()) {
            listOf("min" to biases.min(), "max" to biases.max()).forEach { (end, value) ->
                val upstream = c0017.getValue(Triple(height, 2.0, end))
                checks += UpstreamCheckRecord(
                    quantity = "V* where W(3 nm) = 100 pN, six-model $end",
                    model = "six-model bracket",
                    layerHeight = height,
                    concentration = 2.0,
                    here = value,
                    upstream = upstream,
                    departure = abs(value / upstream - 1.0),
                    source = "C-0017 (the located operating bias, 2 mM)"
                )
            }
        }
    }
    // the coupled and dead-load lines must agree on V* exactly — the same point, two slopes
    ceilings.filter { it.loadLine == "coupled" }.forEach { coupled ->
        val dead = ceilings.first {
            it.loadLine == "dead-load" && it.model == coupled.model &&
                    it.layerHeight == coupled.layerHeight &&
                    it.concentration == coupled.concentration
        }
        val here = coupled.operatingBias
        val there = dead.operatingBias
        if (here != null && there != null) checks += UpstreamCheckRecord(
            quantity = "V* on the coupled line against V* on the dead-load line",
            model = coupled.model,
            layerHeight = coupled.layerHeight,
            concentration = coupled.concentration,
            here = here,
            upstream = there,
            departure = abs(here / there - 1.0),
            source = "identity: R_coupled(3 nm) = 33.333 x 3 = 100 pN = R_dead"
        )
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
    bjerrum: Double
): List<ConvergenceRecord> {
    val model = layerModels(peg).first { it.first.contains("strong-stretching") }.second
    val density = 0.024
    val chain = peg.graftedChain(model.chainLengthForHeight(peg, 10.0, density), density)
    val balance = ActuatorForceBalance(model, chain, geometry)
    val resting = balance.restingHeight
    val strokeCeiling = resting - max(chain.occupiedThickness * 1.01, CURVE_LOWEST_GAP)
    val line = LOAD_LINES.first()

    fun locate(
        nodes: Int,
        coarse: Int,
        strokeTolerance: Double,
        bracket: Double
    ): Double? = EquilibriumPath(
        restingHeight = resting,
        strokeCeiling = strokeCeiling,
        field = Field(2.0, tileCharge, bjerrum, nodes).asPath(),
        bracketTolerance = bracket
    ) { stroke -> line.reaction(stroke) + balance.layerLoad(resting - stroke) }
        .fold(coarseSteps = coarse, strokeTolerance = strokeTolerance).pullInBias

    val records = mutableListOf<ConvergenceRecord>()
    fun axis(name: String, settings: List<Pair<String, Double?>>) {
        val finest = settings.last().second ?: return
        settings.forEach { (label, value) ->
            if (value != null) records += ConvergenceRecord(
                axis = name,
                setting = label,
                pullInBias = value,
                departureFromFinest = abs(value / finest - 1.0)
            )
        }
    }
    axis(
        "Poisson-Boltzmann mesh nodes",
        listOf(1000, 2000, 4000, 8000).map {
            it.toString() to locate(it, DEFAULT_COARSE_STEPS, DEFAULT_STROKE_TOLERANCE, DEFAULT_DIFFUSE_TOLERANCE)
        }
    )
    axis(
        "fold coarse scan steps",
        listOf(8, 12, 24, 48).map {
            it.toString() to locate(MESH_NODES, it, DEFAULT_STROKE_TOLERANCE, DEFAULT_DIFFUSE_TOLERANCE)
        }
    )
    axis(
        "golden-section stroke bracket [nm]",
        listOf(1e-2, 1e-3, 1e-4, 1e-6).map {
            it.toString() to locate(MESH_NODES, DEFAULT_COARSE_STEPS, it, DEFAULT_DIFFUSE_TOLERANCE)
        }
    )
    axis(
        "diffuse-drop bisection bracket [relative]",
        listOf(1e-6, 1e-8, 1e-10, 1e-12).map {
            it.toString() to locate(MESH_NODES, DEFAULT_COARSE_STEPS, DEFAULT_STROKE_TOLERANCE, it)
        }
    )
    return records
}

// ---------------------------------------------------------------------------------------------
// findings, validity, and the report
// ---------------------------------------------------------------------------------------------

@Suppress("LongMethod")
private fun findings(result: MaximumUsableBiasResult): Map<String, String> {
    val findings = linkedMapOf<String, String>()
    LOAD_LINES.forEach { line ->
        val rows = result.ceilings.filter { it.loadLine == line.name }
        val folded = rows.filter { it.pullInBias != null }
        findings["${line.name}: states with a fold below the model floor"] =
            "${folded.size} of ${rows.size}"
        if (folded.isNotEmpty()) {
            findings["${line.name}: pull-in bias range [V]"] =
                "%.4f - %.4f".format(
                    folded.minOf { it.pullInBias!! }, folded.maxOf { it.pullInBias!! }
                )
        }
        val usable = rows.mapNotNull { it.usableBias }
        if (usable.isNotEmpty()) {
            findings["${line.name}: usable bias range [V]"] =
                "%.4f - %.4f".format(usable.min(), usable.max())
        }
        val margins = rows.mapNotNull { it.margin }
        if (margins.isNotEmpty()) {
            findings["${line.name}: margin over the operating bias"] =
                "%.3f - %.3f".format(margins.min(), margins.max())
        }
        findings["${line.name}: binding ceilings"] =
            rows.groupingBy { it.bindingCeiling ?: "none" }.eachCount().toString()
        val unusable = rows.count { it.operatingPointIsUsable == false }
        findings["${line.name}: states whose own operating point is NOT usable"] =
            "$unusable of ${rows.size}"
        findings["${line.name}: states with no stable shallow branch at any bias"] =
            "${rows.count { !it.stableShallowBranchExists }} of ${rows.size}"
    }
    listOf(5.0, 7.0, 10.0).forEach { height ->
        val rows = result.ceilings.filter {
            it.loadLine == "coupled" && it.layerHeight == height && it.concentration == 2.0
        }
        val usable = rows.mapNotNull { it.usableBias }
        val margins = rows.mapNotNull { it.margin }
        if (usable.isNotEmpty()) findings["coupled, $height nm, 2 mM: usable bias [V]"] =
            "%.4f - %.4f, margin %.2f - %.2f".format(
                usable.min(), usable.max(), margins.min(), margins.max()
            )
    }
    val residuals = result.ceilings.mapNotNull { it.tangencyResidual }
    if (residuals.isNotEmpty()) {
        findings["tangency k_c + k_eff = 0 at the located fold, worst relative residual"] =
            "%.3e over ${residuals.size} INTERIOR folds".format(residuals.max())
    }
    findings["folds at the branch start (a boundary maximum, no tangency to check)"] =
        "${result.ceilings.count { it.foldAtBranchStart }} of ${result.ceilings.size}"
    val reversals = result.smallGap.count { it.signReversesAtTheMaximum }
    findings["CH-0011: k_es reverses sign at the force maximum"] =
        "$reversals of ${result.smallGap.size} (buffer, bias) states"
    val peaks = result.smallGap.mapNotNull { it.forceMaximumGap }
    if (peaks.isNotEmpty()) findings["CH-0011: force maximum gap [nm]"] =
        "%.3f - %.3f".format(peaks.min(), peaks.max())
    val onsets = result.smallGap.mapNotNull { it.repulsionOnsetGap }
    findings["CH-0011: repulsion onset gap [nm]"] =
        if (onsets.isEmpty()) "below the ${DIAGNOSTIC_LOWEST_GAP} nm sampled floor everywhere"
        else "%.3f - %.3f, in %d of %d states".format(
            onsets.min(), onsets.max(), onsets.size, result.smallGap.size
        )
    findings["CH-0011: what arrests the collapse"] =
        result.arrest.groupingBy { it.arrestedBy }.eachCount().toString()
    DIAGNOSTIC_BIASES.forEach { bias ->
        val rows = result.arrest.filter { it.appliedBias == bias }
        val gaps = rows.mapNotNull { it.osmoticStopperGap }
        findings["CH-0011: arrest at $bias V"] =
            rows.groupingBy { it.arrestedBy }.eachCount().toString() +
                    if (gaps.isEmpty()) "" else
                        ", osmotic stopper %.3f - %.3f nm".format(gaps.min(), gaps.max())
    }
    result.convergence.groupBy { it.axis }.forEach { (axis, rows) ->
        findings["convergence: $axis"] =
            rows.joinToString("; ") { "${it.setting} -> %.2e".format(it.departureFromFinest) }
    }
    val worst = result.upstreamChecks.maxByOrNull { it.departure }
    if (worst != null) findings["gate 5: worst upstream departure"] =
        "%.3e on '%s' (%s)".format(worst.departure, worst.quantity, worst.source)
    return findings
}

private val CITED = listOf(
    "C-0017's mandated output-coupling stiffness, 100 pN / 3 nm = 33.333 pN/nm — CITED, and " +
            "itself derived there from §3 alone. It is the slope of the coupled load line and " +
            "nothing else here depends on it",
    "C-0005's lateral counterion spacing a_perp = 1.46 nm — CITED, used as the validity floor " +
            "on the gap",
    "C-0002's semidilute-to-concentrated crossover phi = 0.2 — CITED, read as a ceiling per " +
            "§2's second caveat",
    "CH-0007's point-ion boundary in APPLIED bias, ~1.0 V — CITED. It is a flat ceiling on the " +
            "bias axis and it is the one that binds wherever the fold does not",
    "T-11's aqueous electrochemical window, 1.23 V thermodynamic — CITED, quoted as a bound. It " +
            "never binds, and that is the whole of its content here",
    "C-0005's one-loop correction, 123-214% of the leading term — CITED. It is larger than " +
            "every margin in this file",
    "the Stern capacitance ~20 uF/cm2 — CITED, and load-bearing for the diffuse-drop to " +
            "applied-bias mapping this task is built on",
    "Manning surviving fraction 11.90% — CITED FROM C-0005 via C-0008; the tile is " +
            "charge-saturated, so a factor of three here is 7% in sigma_eff",
    "A2, A3, alpha = 1.9e-3, 2.0e-2, 0.49 — CITED FROM C-0003/C-0002",
    "epsilon_r of water at 300 K = 78 — CITED; ~3% on F_es, moves no verdict",
    "§3's targets: 100 pN, 3 nm, 40 x 40 nm tile, 5/7/10 nm layer, 2 V ceiling — CITED"
)

private val VALIDITY = listOf(
    "TRL 1-3. Model-consistent and traceable. NOTHING HERE IS MEASURED.",
    "Mean field, inherited whole from C-0008 and C-0005: the one-loop correction is 123-214% of " +
            "the leading term over the whole 5-10 nm range for Mg2+, and for the OPPOSITELY " +
            "charged tile-electrode pair no published result gives even the direction. Every " +
            "margin reported here is smaller than that.",
    "L0 is a FORCE-ONSET height at a defining load of 1.0 pN over the tile (C-0011, CH-0010). " +
            "A bench reading these numbers in the first-moment convention would be off by " +
            "1.71-2.16x in thickness.",
    "The layer is C-0003's, at C-0001's single grafting density per height — NOT C-0011's " +
            "solved SCF profile. Deliberate, and the same choice C-0017 made: the load line must " +
            "be drawn across the same characteristic C-0012 computed. C-0016 reports the solved " +
            "layer 1.22x outside C-0003's bracket at 5 nm, so every 5 nm number carries that.",
    "The fold is located on the FIRST descent of the equilibrium path. A later maximum is a " +
            "state the device reaches only after having already folded, so it is not a ceiling.",
    "The path is parametrised by the diffuse-layer drop and capped at 0.35 V, which is the same " +
            "bracket C-0008's own Stern inversion uses. A state needing more diffuse drop than " +
            "that is reported as a branch end, not extrapolated.",
    "The smallest gap the path is allowed to reach is max(1.01 x dry thickness, 0.5 nm), " +
            "C-0012's own force-curve floor. The small-gap diagnostics reach to 0.35 nm because " +
            "CH-0011's sign change is there, and that is BELOW C-0005's 1.46 nm correlation " +
            "band and above C-0002's phi = 0.2 crossover: the sign of k_es there is a statement " +
            "about the model, not about the device.",
    "Zero bias is not computed. C-0008 shows it is a sign-changing near-cancellation under 4 pN " +
            "for which no single number is defensible; a branch point that lands on the search " +
            "floor is flagged rather than quoted.",
    "1-D. No edge, no fringing, no lateral load profile. The tile is the rigid mean, which is " +
            "the one load case in which C-0006's tile is rigid at all.",
    "The load lines are AFFINE. A real coupling strain-stiffens (C-0017's spacer has a " +
            "tangent-over-secant of 1.17), which raises the fold; the linear line is therefore " +
            "the conservative one and the margin reported is a lower bound in that respect.",
    "No preload is carried at zero bias, so the coupled line passes through the origin. A " +
            "preloaded coupling is T-13's question and moves the fold.",
    "The electrochemical ceiling is thermodynamic (1.23 V) and kinetic overpotentials are not " +
            "modelled. It never binds here, so nothing rests on it."
)

private val OPEN = listOf(
    "The sub-1.5 nm region where k_es reverses is inside C-0005's correlation band and above " +
            "C-0002's concentrated crossover, so the LOCATION of the reversal is a model " +
            "statement. Its EXISTENCE is established inside C-0008's own validity range, at the " +
            "zero-bias sign change between 4 and 5 nm. Explicit-ion simulation at 1-1.5 nm is " +
            "the only route to the location, at C-0005's 1-3 week cost.",
    "The mean-field error is larger than every margin here and no better Poisson-Boltzmann " +
            "solve reduces it. T-1f is the queued task that bounds the layer half of it.",
    "The preloaded branch is not evaluated (T-13), and a preload moves the fold in the " +
            "unfavourable direction for a downward preload.",
    "The dynamic pull-in is not computed. Everything here is static: a bias step faster than " +
            "the drainage time can carry the tile past a fold that a quasi-static ramp would " +
            "stop at. C-0004's corner is 91 kHz-2.3 MHz, so the quasi-static reading is the " +
            "right one below ~10 kHz and this is stated rather than assumed.",
    "The lateral coordinate has no fold at all, because the layer's lateral stiffness is " +
            "exactly zero by symmetry (C-0010). Nothing here says anything about lateral " +
            "stability, which is T-12's."
)

private fun report(result: MaximumUsableBiasResult, output: File) {
    println()
    println("T-4 — ${result.title}")
    println("  ${result.ceilings.size} ceiling records, ${result.smallGap.size} small-gap, " +
            "${result.arrest.size} arrest, ${result.upstreamChecks.size} upstream checks, " +
            "${result.convergence.size} convergence")
    result.findings.forEach { (key, value) -> println("  $key: $value") }
    println("written to $output")
}
