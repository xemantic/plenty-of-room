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
import com.xemantic.nano.plentyofroom.brush.heightUnderLoad
import com.xemantic.nano.plentyofroom.brush.reducedSecondVirialCoefficient
import com.xemantic.nano.plentyofroom.brush.reducedThirdVirialCoefficient
import com.xemantic.nano.plentyofroom.brush.threeBodyInteraction
import com.xemantic.nano.plentyofroom.brush.twoBodyInteraction
import com.xemantic.nano.plentyofroom.electrostatics.DEFAULT_GAP_MESH_NODES
import com.xemantic.nano.plentyofroom.electrostatics.DnaOrigamiTile
import com.xemantic.nano.plentyofroom.electrostatics.GapMedium
import com.xemantic.nano.plentyofroom.electrostatics.HYDRATED_CHLORIDE_RADIUS
import com.xemantic.nano.plentyofroom.electrostatics.IonModel
import com.xemantic.nano.plentyofroom.electrostatics.LayerPartitioning
import com.xemantic.nano.plentyofroom.electrostatics.MagnesiumChlorideBuffer
import com.xemantic.nano.plentyofroom.electrostatics.PoissonBoltzmannGap
import com.xemantic.nano.plentyofroom.electrostatics.bjerrumLength
import com.xemantic.nano.plentyofroom.electrostatics.diffusePotentialOfAppliedBias
import com.xemantic.nano.plentyofroom.electrostatics.stericSaturationPotential
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

/**
 * Task `T-3` — stroke and blocking force versus bias, including ionic screening. Leaf `A2.2`.
 *
 * Run with:
 *
 * ```shell
 * ./gradlew study -Pstudy=actuator.StrokeAndBlockingForceStudyKt -PbuildDirectory=build-t3
 * ```
 *
 * Emits `gpd/results/T-3-stroke-and-blocking-force.json`, deterministically — no timestamp, and
 * every floating-point number rounded at the serialisation boundary per [roundActuatorResult].
 *
 * Consumes `C-0008` (`F_es(h, V)` through the same solver, re-run rather than tabulated by hand)
 * and `C-0003` (the layer's `P(h)`, across all six of its models). Owns nothing in `brush/`,
 * `electrostatics/`, `material/`, `structure/` or `poroelastic/`.
 */

/** One (profile × interaction) model of `C-0003`, at one §3 layer height. */
@Serializable
data class ActuatorDesignPoint(
    val model: String,
    val profile: String,
    val interaction: String,
    val layerHeight: Double,
    val graftingDensity: Double,
    val monomersPerChain: Double,
    val chainMolarMass: Double,
    val restingHeight: Double,
    val dryThickness: Double,
    val restingVolumeFraction: Double,
    val stretchingRatio: Double,
    val stiffnessAtRest: Double,
    val stiffnessAtFourFifths: Double,
    val strokeUnderHundredPiconewtonDeadLoad: Double
)

/** One solved (model, design point, buffer, bias) state of the coupled actuator. */
@Serializable
data class ActuatorOperatingRecord(
    val medium: String,
    val model: String,
    val layerHeight: Double,
    val graftingDensity: Double,
    val concentration: Double,
    val bulkDebyeLength: Double,
    val appliedBias: Double,
    val diffuseLayerPotential: Double,
    val pointIonValid: Boolean,
    val withinTrustedBias: Boolean,
    val blockingForce: Double,
    val peakOutputForce: Double,
    val peakOutputForceStroke: Double,
    val peakOverBlockingForce: Double,
    val stroke: Double,
    val operatingHeight: Double,
    val compressionRatio: Double,
    val volumeFraction: Double,
    val electrostaticForceAtOperatingPoint: Double,
    val layerRestoringForce: Double,
    val balanceResidual: Double,
    val brushStiffness: Double,
    val electrostaticStiffness: Double,
    val effectiveStiffness: Double,
    val stiffnessRatio: Double,
    val forceDecayLength: Double,
    val outputForceAtThreeNanometres: Double?,
    val maximumOutputWork: Double,
    val maximumOutputWorkInThermalEnergy: Double,
    val workStroke: Double,
    val drainageCornerFrequency: Double,
    val drainageCornerOverRequirement: Double,
    val compositeWorstCaseCorner: Double,
    val electrostaticStopperGap: Double?,
    val correlationBandBreached: Boolean,
    val layerCrossoverCeilingBreached: Boolean,
    val modelValid: Boolean,
    val loadedOperatingHeight: Double,
    val loadedVolumeFraction: Double,
    val loadedBrushStiffness: Double,
    val loadedElectrostaticStiffness: Double?,
    val loadedEffectiveStiffness: Double?,
    val loadedStiffnessRatio: Double?,
    val loadedDrainageCornerFrequency: Double?,
    val loadedDrainageCornerOverRequirement: Double?,
    val furtherEquilibria: Int,
    val converged: Boolean,
    val meetsBlockingForceTarget: Boolean,
    val meetsStrokeTarget: Boolean,
    val meetsSimultaneousTarget: Boolean
)

/** The bias each acceptance clause is first met at, per (model, design point, buffer). */
@Serializable
data class ActuatorThresholdRecord(
    val model: String,
    val layerHeight: Double,
    val concentration: Double,
    val biasForHundredPiconewtonBlocking: Double?,
    val biasForThreeNanometreStroke: Double?,
    val biasForSimultaneousTarget: Double?,
    val biasBracketForSimultaneousTarget: String,
    val strokeAtTwoVolts: Double,
    val blockingForceAtTwoVolts: Double,
    val strokeAtOneVolt: Double,
    val blockingForceAtOneVolt: Double,
    val strokeAtHalfVolt: Double,
    val smallestStiffnessRatio: Double,
    val biasOfSmallestStiffnessRatio: Double,
    val anyFurtherEquilibria: Boolean,
    val largestModelValidBias: Double?,
    val largestModelValidStroke: Double?,
    val strokeAtLargestModelValidBias: Double?,
    val blockingForceAtLargestModelValidBias: Double?,
    val loadedStiffnessRatioAtSimultaneousTarget: Double?,
    val loadedDrainageCornerAtSimultaneousTarget: Double?
)

/** What the PEG layer as a dielectric-and-partitioning medium does to the coupled answer. */
@Serializable
data class LayerMediumRecord(
    val model: String,
    val layerHeight: Double,
    val concentration: Double,
    val appliedBias: Double,
    val restingVolumeFraction: Double,
    val saltPartitionCoefficientAtRest: Double,
    val blockingForceInFreeBuffer: Double,
    val blockingForceWithLayer: Double,
    val blockingForceAmplification: Double,
    val strokeInFreeBuffer: Double,
    val strokeWithLayer: Double,
    val strokeAmplification: Double,
    val effectiveStiffnessInFreeBuffer: Double,
    val effectiveStiffnessWithLayer: Double
)

/** Whose stroke — the `C-0006` correction, applied to a computed tile-mean stroke. */
@Serializable
data class StrokeReadingRecord(
    val model: String,
    val layerHeight: Double,
    val concentration: Double,
    val appliedBias: Double,
    val tileMeanStroke: Double,
    val couplingScheme: String,
    val dishingOverStroke: Double,
    val leverPointStroke: Double,
    val sensorAreaAveragedStroke: Double,
    val sensorDebyeWeightingOffset: Double,
    val leverMinusSensor: Double,
    val leverMinusSensorOverStroke: Double
)

/** Mesh and grid convergence of the whole pipeline — gate 4, emitted rather than only asserted. */
@Serializable
data class ActuatorConvergenceRecord(
    val quantity: String,
    val setting: String,
    val samples: Int,
    val meshNodes: Int,
    val searchNodes: Int,
    val scanSteps: Int,
    val blockingForce: Double,
    val stroke: Double,
    val relativeDepartureInStroke: Double
)

@Serializable
data class StrokeAndBlockingForceResult(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val tightenedAcceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val runParameters: Map<String, String>,
    val citedInputs: List<String>,
    val geometry: ActuatorGeometry,
    val designPoints: List<ActuatorDesignPoint>,
    val operatingPoints: List<ActuatorOperatingRecord>,
    val thresholds: List<ActuatorThresholdRecord>,
    val layerMedium: List<LayerMediumRecord>,
    val strokeReadings: List<StrokeReadingRecord>,
    val convergence: List<ActuatorConvergenceRecord>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

// ---------------------------------------------------------------------------------------------
// the sweep
// ---------------------------------------------------------------------------------------------

/** §3's three layer heights with `C-0001`'s grafting densities — the standing design points. */
private val DESIGN_POINTS = listOf(5.0 to 0.092, 7.0 to 0.045, 10.0 to 0.024)

/**
 * §3's buffers, plus **0.5 and 1 mM** — leaf `A2.2`'s "low-screening operating point".
 *
 * `A2.2` asks for the stroke at the *low-Mg* point rather than at 10–20 mM. Mg²⁺-**free** is not
 * representable by this solver at all (`IonModel` is a 2:1 electrolyte by construction, and a
 * salt-free gap has no screening length), and the crosslinking that `A2.2` pairs with it is
 * outside every model in this project. What can be honoured is the direction, so the sweep is
 * extended a factor of four below §3's lowest buffer and the trend is reported.
 */
private val BUFFERS = listOf(0.5, 1.0, 2.0, 5.0, 10.0)

/** Applied bias in volt. Dense below 0.5 V, because `C-0008` shows the force saturates above it. */
private val BIASES = listOf(0.02, 0.05, 0.1, 0.25, 0.5, 0.75, 1.0, 1.5, 2.0)

/** Where the layer medium is carried through the coupled solve as well as the free buffer. */
private val LAYER_MEDIUM_BIASES = listOf(0.25, 1.0, 2.0)

private const val LAYER_MEDIUM_CONCENTRATION = 2.0

private const val TARGET_FORCE = 100.0

private const val ACCEPTABLE_STROKE = 3.0

private const val DESIRED_STROKE = 10.0

private const val BIAS_CEILING = 2.0

/** `CH-0007`'s point-ion boundary **in applied bias**, not in diffuse-layer drop. */
private const val TRUSTED_BIAS_CEILING = 1.0

/** `C-0005`'s lateral counterion spacing for Mg2+ — below this gap PB cannot produce the physics. */
private const val CORRELATION_ATTRACTION_GAP = 1.46

/**
 * `C-0002`'s semidilute→concentrated crossover, above which the des Cloizeaux `9/4` exponent
 * is no longer the one the layer is entitled to — §2's own second caveat, read as a ceiling.
 */
private const val CONCENTRATED_CROSSOVER = 0.2

private const val STERN_CAPACITANCE = 20.0

/** Mesh nodes for the Stern-series bisection inside `diffusePotentialOfAppliedBias`. */
private const val SEARCH_NODES = 400

private const val CURVE_SAMPLES = 72

private const val CURVE_LOWEST_GAP = 0.5

private const val CURVE_HIGHEST_GAP = 11.5

/** `C-0004`'s drainage corner and the stiffness it was evaluated at — **CITED**, not re-derived. */
private val DRAINAGE_REFERENCE = mapOf(
    5.0 to (186.0e3 to 111.0),
    7.0 to (130.0e3 to 27.1),
    10.0 to (91.0e3 to 7.39)
)

/** `C-0004`'s composite worst case: largest §3 tile, thickest layer, least permeable model, ¼ k. */
private const val COMPOSITE_WORST_CASE_CORNER = 5.6e3

private const val BANDWIDTH_REQUIREMENT = 1.0e3

/** `C-0006`'s dishing-over-stroke per output-coupling scheme — **CITED from `C-0006`**. */
private val COUPLING_SCHEMES = listOf(
    "continuous, uniform load (the only case where the tile is rigid, and it is rigid exactly)" to 0.0,
    "49 attachments" to 0.11,
    "16 attachments" to 0.34,
    "9 attachments" to 0.64,
    "4 attachments" to 1.41,
    "1 concentrated lever" to 3.69,
    "thermal shape fluctuation, 300 K, unloaded" to 0.257
)

/** `C-0006`'s thermal dishing amplitude in nm — **CITED**, at `C-0001`'s stiffness. */
private const val THERMAL_DISHING_RMS = 1.272

private const val OSMOTIC_SECOND_VIRIAL = 1.9e-3

private const val OSMOTIC_THIRD_VIRIAL = 2.0e-2

private class Sampler(
    val tileCharge: Double,
    val bjerrumLength: Double,
    val ionModel: IonModel,
    val medium: (Double) -> com.xemantic.nano.plentyofroom.electrostatics.GapMediumProfile
) {

    val stern: Double = sternChargeDensityPerVolt(STERN_CAPACITANCE)

    fun diffusePotential(gap: Double, bias: Double): Double = diffusePotentialOfAppliedBias(
        gap, bias, tileCharge, stern, ionModel, medium(gap), bjerrumLength, nodes = SEARCH_NODES
    )

    fun force(gap: Double, bias: Double, nodes: Int = DEFAULT_GAP_MESH_NODES): Double =
        PoissonBoltzmannGap(gap, ionModel, medium(gap), bjerrumLength, nodes = nodes)
            .solve(diffusePotential(gap, bias) / thermalVoltage(), tileCharge)
            .forceOnTile(1600.0)

}

private fun freeBufferSampler(
    concentration: Double,
    tileCharge: Double,
    bjerrumLength: Double
): Sampler {
    val ions = IonModel(MagnesiumChlorideBuffer(concentration).magnesiumNumberDensity)
    val medium = uniformMedium(GapMedium())
    return Sampler(tileCharge, bjerrumLength, ions) { medium }
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
    val fibreRadius = peg.kuhnSegmentDiameter / 2.0
    val models = layerModels(peg)

    println("T-3 — sampling F_es(h) curves in free bulk buffer ...")
    val bareSampling = BUFFERS.associateWith { concentration ->
        val sampler = freeBufferSampler(concentration, tileCharge, lb)
        BIASES.associateWith { bias ->
            attractiveForceCurve(
                gradedGapGrid(CURVE_LOWEST_GAP, CURVE_HIGHEST_GAP, CURVE_SAMPLES)
            ) { sampler.force(it, bias) }
        }
    }
    val bareCurves = bareSampling.mapValues { (_, byBias) -> byBias.mapValues { it.value.curve } }
    val diffusePotentials = BUFFERS.associateWith { concentration ->
        val sampler = freeBufferSampler(concentration, tileCharge, lb)
        BIASES.associateWith { bias -> sampler.diffusePotential(7.0, bias) }
    }
    val pointIonBoundary = BUFFERS.associateWith { concentration ->
        stericSaturationPotential(
            1, MagnesiumChlorideBuffer(concentration).chlorideNumberDensity,
            HYDRATED_CHLORIDE_RADIUS
        )
    }

    println("T-3 — solving the coupled force balance ...")
    val designPoints = mutableListOf<ActuatorDesignPoint>()
    val operating = mutableListOf<ActuatorOperatingRecord>()
    val thresholds = mutableListOf<ActuatorThresholdRecord>()
    val strokeReadings = mutableListOf<StrokeReadingRecord>()
    val balances = mutableMapOf<Pair<String, Double>, ActuatorForceBalance>()

    DESIGN_POINTS.forEach { (height, density) ->
        models.forEach { (name, model) ->
            val chain = peg.graftedChain(
                model.chainLengthForHeight(peg, height, density), density
            )
            val balance = ActuatorForceBalance(model, chain, geometry)
            balances[name to height] = balance
            designPoints += describe(peg, name, model, chain, balance, height, density)
            BUFFERS.forEach { concentration ->
                val buffer = MagnesiumChlorideBuffer(concentration)
                val records = BIASES.map { bias ->
                    record(
                        medium = "free bulk buffer",
                        modelName = name,
                        height = height,
                        density = density,
                        buffer = buffer,
                        bias = bias,
                        diffuse = diffusePotentials.getValue(concentration).getValue(bias),
                        boundary = pointIonBoundary.getValue(concentration),
                        balance = balance,
                        state = balance.solve(bareCurves.getValue(concentration).getValue(bias)),
                        field = bareCurves.getValue(concentration).getValue(bias),
                        repulsiveBelow =
                            bareSampling.getValue(concentration).getValue(bias).repulsiveBelow
                    )
                }
                operating += records
                thresholds += threshold(name, height, concentration, records)
            }
            strokeReadings += strokeReadings(
                name, height, LAYER_MEDIUM_CONCENTRATION,
                operating.first {
                    it.model == name && it.layerHeight == height &&
                            it.concentration == LAYER_MEDIUM_CONCENTRATION &&
                            it.appliedBias == 1.0
                }
            )
        }
    }

    println("T-3 — repeating the 2 mM solve with the PEG layer as the medium ...")
    val layerMedium = mutableListOf<LayerMediumRecord>()
    DESIGN_POINTS.forEach { (height, _) ->
        models.forEach { (name, _) ->
            val balance = balances.getValue(name to height)
            val chain = balance.chain
            val ions = IonModel(
                MagnesiumChlorideBuffer(LAYER_MEDIUM_CONCENTRATION).magnesiumNumberDensity
            )
            val sampler = Sampler(tileCharge, lb, ions) { gap ->
                val partitioning = LayerPartitioning(
                    chain.meanVolumeFraction(gap).coerceAtMost(0.99), fibreRadius
                )
                uniformMedium(
                    GapMedium(
                        relativePermittivity = partitioning.effectivePermittivity,
                        magnesiumPartitionCoefficient = partitioning.magnesiumPartitionCoefficient,
                        chloridePartitionCoefficient = partitioning.chloridePartitionCoefficient
                    )
                )
            }
            val lowest = maxOf(CURVE_LOWEST_GAP, chain.occupiedThickness * 1.05)
            val grid = gradedGapGrid(lowest, balance.restingHeight, CURVE_SAMPLES)
            LAYER_MEDIUM_BIASES.forEach { bias ->
                val layered = attractiveForceCurve(grid) { sampler.force(it, bias) }.curve
                val bare = bareCurves.getValue(LAYER_MEDIUM_CONCENTRATION).getValue(bias)
                val withLayer = balance.solve(layered)
                val withoutLayer = balance.solve(bare)
                val restingFraction = chain.meanVolumeFraction(balance.restingHeight)
                layerMedium += LayerMediumRecord(
                    model = name,
                    layerHeight = height,
                    concentration = LAYER_MEDIUM_CONCENTRATION,
                    appliedBias = bias,
                    restingVolumeFraction = restingFraction,
                    saltPartitionCoefficientAtRest =
                        LayerPartitioning(restingFraction, fibreRadius).saltPartitionCoefficient,
                    blockingForceInFreeBuffer = withoutLayer.blockingForce,
                    blockingForceWithLayer = withLayer.blockingForce,
                    blockingForceAmplification =
                        withLayer.blockingForce / withoutLayer.blockingForce,
                    strokeInFreeBuffer = withoutLayer.stroke,
                    strokeWithLayer = withLayer.stroke,
                    strokeAmplification = withLayer.stroke / withoutLayer.stroke,
                    effectiveStiffnessInFreeBuffer = withoutLayer.effectiveStiffness,
                    effectiveStiffnessWithLayer = withLayer.effectiveStiffness
                )
            }
        }
    }

    println("T-3 — convergence ...")
    val convergence = convergence(tileCharge, lb, peg, geometry)

    val result = StrokeAndBlockingForceResult(
        task = "T-3",
        leaf = "A2.2",
        title = "Stroke and blocking force versus bias for the Gen-1 actuator, including ionic " +
                "screening, from a self-consistent force balance between C-0008's F_es(h, V) " +
                "and C-0003's P(h)",
        verificationType = "in-silico (nonlinear Poisson-Boltzmann force curve x crossover-valid " +
                "grafted-layer free energy, coupled by a bracketed 1-D root find) + logical",
        acceptance = "§6 task 3: stroke >= ~3 nm and force >= 100 pN at <= 2 V, or a " +
                "demonstration that it is unreachable.",
        tightenedAcceptance = "The predicate is decomposed into three, because the two halves " +
                "are different quantities and are not deliverable at the same operating point. " +
                "(a) BLOCKING: |F_es(L0, V)| >= 100 pN for some V <= 2 V — the force at ZERO " +
                "displacement, where the layer carries nothing. (b) FREE STROKE: L0 - h*(V) >= " +
                "3.0 nm for some V <= 2 V, where h*(V) solves |F_es(h, V)| = P(h) A — a ROOT, " +
                "never a force divided by a stiffness. (c) SIMULTANEOUS, the tightened form: " +
                "the actuator characteristic W(s) = |F_es(L0 - s, V)| - P(L0 - s) A satisfies " +
                "W(3 nm) >= 100 pN for some V <= 2 V, i.e. the device delivers 100 pN AT a 3 nm " +
                "stroke rather than 100 pN and 3 nm at two different operating points. " +
                "(c) implies (a) and (b); the verdict is reported separately for each, at every " +
                "one of C-0003's six models, so that a PASS on (a)+(b) with a FAIL on (c) is " +
                "visible rather than hidden. Falsified for a given model and design point if no " +
                "V <= 2 V satisfies the clause; falsified as an APPROACH if the six-model " +
                "bracket straddles the predicate so widely that no verdict is model-independent.",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated. " +
                "Inherits C-0008's mean-field validity statement in full: C-0005 puts the " +
                "one-loop correction at 123-214% of the leading term across this entire gap " +
                "range, so every force here is a MEAN-FIELD number whose error is not bounded " +
                "by its own expansion; and C-0003's profile-model spread, which is a LOWER " +
                "bound on the layer's profile uncertainty, not a full error bar.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm (= 1 mN/m exactly)",
            "pressure" to "pN/nm^2 (= 1 MPa exactly)",
            "energy" to "pN*nm",
            "work" to "pN*nm (also reported in k_BT)",
            "potential" to "V",
            "concentration" to "mM",
            "frequency" to "Hz",
            "temperature" to "K"
        ),
        conventions = listOf(
            "z is normal to the electrode, positive AWAY from it; the electrode surface is z = 0",
            "the polymer layer is grafted at z = 0 and the tile's BOTTOM FACE rests on its outer " +
                    "surface at z = h, so the ELECTROSTATIC GAP IS THE LAYER HEIGHT, exactly and " +
                    "by construction — ActuatorGeometry.electrostaticGap is the identity, and " +
                    "that is the relation §3's three lengths make easy to get wrong",
            "the tile's top face is at z = h + 10 nm and the effort point at z = h + 15 nm, " +
                    "which puts §3's three layer heights at 20 / 22 / 25 nm — §3's own band, " +
                    "both ends",
            "the tile's charge is smeared onto the plane z = h, the Manning-renormalised charge " +
                    "of half the tile — C-0008's NOMINAL convention, inherited unchanged; the " +
                    "tile's own 10 nm of thickness enters only through the effort point",
            "F_es,z < 0 means toward the electrode; k_es = -dF_es,z/dz < 0 — §1",
            "the layer's disjoining pressure is POSITIVE when it pushes the tile along +z, and " +
                    "k_brush = -dP/dh > 0 — C-0003",
            "the STROKE is L0 - h, positive downward; the BLOCKING FORCE is |F_es(L0, V)|, the " +
                    "force at ZERO stroke, where the layer carries nothing",
            "the OUTPUT FORCE at stroke s is W(s) = |F_es(L0-s, V)| - P(L0-s) A, so the whole " +
                    "force-displacement characteristic is one function, the blocking force is " +
                    "W(0), and the free stroke is its root",
            "d W/dh = k_eff = k_brush + k_es EXACTLY, so the slope of the characteristic IS the " +
                    "effective stiffness, and the first root below L0 is stable by construction"
        ),
        runParameters = mapOf(
            "temperature" to ROOM_TEMPERATURE.toString(),
            "thermalEnergy" to thermalEnergy().toString(),
            "bjerrumLength" to lb.roundedForProse().toString(),
            "tileEdge" to geometry.tileEdge.toString(),
            "tileThickness" to geometry.tileThickness.toString(),
            "leverAttachmentHeight" to geometry.leverAttachmentHeight.toString(),
            "footprintArea" to geometry.footprintArea.toString(),
            "manningSurvivingFraction" to surviving.roundedForProse().toString(),
            "nominalTileChargeDensity" to tileCharge.roundedForProse().toString(),
            "sternCapacitance" to STERN_CAPACITANCE.toString(),
            "meshNodes" to DEFAULT_GAP_MESH_NODES.toString(),
            "biasSearchNodes" to SEARCH_NODES.toString(),
            "forceCurveSamples" to CURVE_SAMPLES.toString(),
            "forceCurveGapRange" to "[$CURVE_LOWEST_GAP, $CURVE_HIGHEST_GAP]",
            "forceBalanceScanSteps" to "600",
            "layerHeights" to DESIGN_POINTS.map { it.first }.toString(),
            "graftingDensities" to DESIGN_POINTS.map { it.second }.toString(),
            "buffers" to BUFFERS.toString(),
            "biases" to BIASES.toString(),
            "osmoticSecondVirial" to OSMOTIC_SECOND_VIRIAL.toString(),
            "osmoticThirdVirial" to OSMOTIC_THIRD_VIRIAL.toString(),
            "pegFibreRadius" to fibreRadius.roundedForProse().toString(),
            "targetForce" to TARGET_FORCE.toString(),
            "acceptableStroke" to ACCEPTABLE_STROKE.toString(),
            "desiredStroke" to DESIRED_STROKE.toString(),
            "biasCeiling" to BIAS_CEILING.toString(),
            "trustedBiasCeiling" to TRUSTED_BIAS_CEILING.toString()
        ),
        citedInputs = listOf(
            "C-0003's six (profile x interaction) layer models, their measured A2 = 1.9e-3 and " +
                    "A3 = 2.0e-2, and alpha = 0.49 — CONSUMED as libraries from brush/ and " +
                    "material/, re-run here rather than tabulated, so the layer response is " +
                    "re-derived at every operating height rather than read off a claim table.",
            "C-0008's F_es(h, V) pipeline — likewise CONSUMED and re-run through " +
                    "electrostatics/PoissonBoltzmannGap, not copied. Its numbers are reproduced " +
                    "as a gate-5 cross-check rather than assumed.",
            "The Manning-renormalised tile charge, 11.90% of bare — CITED FROM C-0005 via " +
                    "C-0008. The tile is charge-saturated, so a factor of three here is 7% in " +
                    "sigma_eff.",
            "Stern capacitance ~20 uF/cm^2 — CITED, and load-bearing for the bias mapping " +
                    "(CH-0007), not for the force above ~0.5 V.",
            "C-0004's drainage corners 186 / 130 / 91 kHz at 5 / 7 / 10 nm and the stiffnesses " +
                    "111.0 / 27.1 / 7.39 pN/nm they were evaluated at, its composite worst case " +
                    "of 5.6 kHz, and its verified result that tau is EXACTLY proportional to " +
                    "1/k_layer — CITED FROM C-0004. Drainage is NOT re-derived here.",
            "C-0006's dishing-over-stroke ratios per coupling scheme (0.11 / 0.34 / 0.64 / 1.41 " +
                    "/ 3.69 for 49 / 16 / 9 / 4 / 1 attachments), its 1.272 nm thermal dishing " +
                    "RMS and its 26% lever-versus-sensor difference — CITED FROM C-0006, and " +
                    "computed there at C-0001's foundation stiffness, which C-0003 has since " +
                    "superseded. They are applied as ratios and flagged.",
            "C-0007's <= 0.4% buffer-independence of the layer modulus over 2-10 mM, and its " +
                    "<= 1.7% stroke-dependent layer-local salt term — CITED FROM C-0007 and " +
                    "used to justify holding the layer mechanics fixed across the buffer sweep."
        ),
        geometry = geometry,
        designPoints = designPoints,
        operatingPoints = operating,
        thresholds = thresholds,
        layerMedium = layerMedium,
        strokeReadings = strokeReadings,
        convergence = convergence,
        findings = emptyMap(),
        validity = validity(),
        openQuestions = openQuestions()
    )
    val complete = result.copy(findings = findings(result))
    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-3-stroke-and-blocking-force.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(json.encodeToJsonElement(complete).roundedForActuatorResult()) + "\n"
    )
    report(complete, output)
}

private fun describe(
    peg: PegWater,
    name: String,
    model: GraftedLayerModel,
    chain: GraftedChain,
    balance: ActuatorForceBalance,
    height: Double,
    density: Double
): ActuatorDesignPoint {
    val resting = balance.restingHeight
    val dead = model.heightUnderLoad(chain, TARGET_FORCE, balance.geometry.footprintArea)
    return ActuatorDesignPoint(
        model = name,
        profile = if (name.startsWith("alexander-box")) "alexander-box" else "strong-stretching",
        interaction = name.substringAfter('(').substringBefore(')'),
        layerHeight = height,
        graftingDensity = density,
        monomersPerChain = chain.monomersPerChain,
        chainMolarMass = chain.monomersPerChain * peg.monomerMolarMass,
        restingHeight = resting,
        dryThickness = chain.occupiedThickness,
        restingVolumeFraction = chain.meanVolumeFraction(resting),
        stretchingRatio = chain.stretchingRatio(resting),
        stiffnessAtRest = balance.layerStiffness(resting),
        stiffnessAtFourFifths = balance.layerStiffness(0.8 * resting),
        strokeUnderHundredPiconewtonDeadLoad = resting - dead
    )
}

private fun record(
    medium: String,
    modelName: String,
    height: Double,
    density: Double,
    buffer: MagnesiumChlorideBuffer,
    bias: Double,
    diffuse: Double,
    boundary: Double,
    balance: ActuatorForceBalance,
    state: ActuatorState,
    field: ElectrostaticForceCurve,
    repulsiveBelow: Double?
): ActuatorOperatingRecord {
    val reference = DRAINAGE_REFERENCE.getValue(height)
    val corner = drainageCornerFrequency(
        reference.first, reference.second, maxOf(state.effectiveStiffness, 0.0)
    )
    val threeNanometreHeight = balance.restingHeight - ACCEPTABLE_STROKE
    val reachable = threeNanometreHeight > maxOf(field.minimumGap, balance.dryThickness * 1.001) &&
            threeNanometreHeight <= field.maximumGap
    val atThree = if (reachable) balance.outputForce(field, threeNanometreHeight) else null
    val loadedBrush = balance.layerStiffness(threeNanometreHeight)
    val loadedElectrostatic = if (reachable) field.stiffnessAt(threeNanometreHeight) else null
    val loadedEffective = loadedElectrostatic?.let { loadedBrush + it }
    val loadedCorner = loadedEffective?.let {
        if (it > 0.0) drainageCornerFrequency(reference.first, reference.second, it) else 0.0
    }
    return ActuatorOperatingRecord(
        medium = medium,
        model = modelName,
        layerHeight = height,
        graftingDensity = density,
        concentration = buffer.concentration,
        bulkDebyeLength = buffer.debyeLength(),
        appliedBias = bias,
        diffuseLayerPotential = diffuse,
        pointIonValid = diffuse <= boundary,
        withinTrustedBias = bias <= TRUSTED_BIAS_CEILING,
        blockingForce = state.blockingForce,
        peakOutputForce = state.peakOutputForce,
        peakOutputForceStroke = state.peakOutputForceStroke,
        peakOverBlockingForce = state.peakOutputForce / state.blockingForce,
        stroke = state.stroke,
        operatingHeight = state.operatingHeight,
        compressionRatio = state.compressionRatio,
        volumeFraction = state.volumeFraction,
        electrostaticForceAtOperatingPoint = state.electrostaticForce,
        layerRestoringForce = state.layerRestoringForce,
        balanceResidual = state.balanceResidual,
        brushStiffness = state.brushStiffness,
        electrostaticStiffness = state.electrostaticStiffness,
        effectiveStiffness = state.effectiveStiffness,
        stiffnessRatio = state.stiffnessRatio,
        forceDecayLength = state.forceDecayLength,
        outputForceAtThreeNanometres = atThree,
        maximumOutputWork = state.maximumOutputWork,
        maximumOutputWorkInThermalEnergy = state.maximumOutputWork / thermalEnergy(),
        workStroke = state.workStroke,
        drainageCornerFrequency = corner,
        drainageCornerOverRequirement = corner / BANDWIDTH_REQUIREMENT,
        compositeWorstCaseCorner = COMPOSITE_WORST_CASE_CORNER * state.stiffnessRatio,
        electrostaticStopperGap = repulsiveBelow,
        correlationBandBreached = state.operatingHeight < CORRELATION_ATTRACTION_GAP,
        layerCrossoverCeilingBreached = state.volumeFraction > CONCENTRATED_CROSSOVER,
        modelValid = state.operatingHeight >= CORRELATION_ATTRACTION_GAP &&
                state.volumeFraction <= CONCENTRATED_CROSSOVER,
        loadedOperatingHeight = threeNanometreHeight,
        loadedVolumeFraction = balance.chain.meanVolumeFraction(threeNanometreHeight),
        loadedBrushStiffness = loadedBrush,
        loadedElectrostaticStiffness = loadedElectrostatic,
        loadedEffectiveStiffness = loadedEffective,
        loadedStiffnessRatio = loadedEffective?.let { it / loadedBrush },
        loadedDrainageCornerFrequency = loadedCorner,
        loadedDrainageCornerOverRequirement = loadedCorner?.let { it / BANDWIDTH_REQUIREMENT },
        furtherEquilibria = state.furtherEquilibria,
        converged = state.converged,
        meetsBlockingForceTarget = state.blockingForce >= TARGET_FORCE,
        meetsStrokeTarget = state.stroke >= ACCEPTABLE_STROKE,
        meetsSimultaneousTarget = (atThree ?: Double.NEGATIVE_INFINITY) >= TARGET_FORCE
    )
}

private fun threshold(
    modelName: String,
    height: Double,
    concentration: Double,
    records: List<ActuatorOperatingRecord>
): ActuatorThresholdRecord {
    val biases = records.map { it.appliedBias }.toDoubleArray()
    val blocking = firstCrossing(
        biases, records.map { it.blockingForce }.toDoubleArray(), TARGET_FORCE
    )
    val stroke = firstCrossing(
        biases, records.map { it.stroke }.toDoubleArray(), ACCEPTABLE_STROKE
    )
    val simultaneous = firstCrossing(
        biases,
        records.map { it.outputForceAtThreeNanometres ?: Double.NEGATIVE_INFINITY }.toDoubleArray(),
        TARGET_FORCE
    )
    val softest = records.minBy { it.stiffnessRatio }
    val valid = records.filter { it.modelValid }
    val atTarget = simultaneous?.let { crossing ->
        records.firstOrNull { it.appliedBias >= crossing.bracket.second }
    }
    fun at(bias: Double) = records.first { it.appliedBias == bias }
    return ActuatorThresholdRecord(
        model = modelName,
        layerHeight = height,
        concentration = concentration,
        biasForHundredPiconewtonBlocking = blocking?.value,
        biasForThreeNanometreStroke = stroke?.value,
        biasForSimultaneousTarget = simultaneous?.value,
        biasBracketForSimultaneousTarget =
            simultaneous?.let { "[${it.bracket.first}, ${it.bracket.second}]" } ?: "none",
        strokeAtTwoVolts = at(2.0).stroke,
        blockingForceAtTwoVolts = at(2.0).blockingForce,
        strokeAtOneVolt = at(1.0).stroke,
        blockingForceAtOneVolt = at(1.0).blockingForce,
        strokeAtHalfVolt = at(0.5).stroke,
        smallestStiffnessRatio = softest.stiffnessRatio,
        biasOfSmallestStiffnessRatio = softest.appliedBias,
        anyFurtherEquilibria = records.any { it.furtherEquilibria > 0 },
        largestModelValidBias = valid.maxOfOrNull { it.appliedBias },
        largestModelValidStroke = valid.maxOfOrNull { it.stroke },
        strokeAtLargestModelValidBias = valid.maxByOrNull { it.appliedBias }?.stroke,
        blockingForceAtLargestModelValidBias = valid.maxByOrNull { it.appliedBias }?.blockingForce,
        loadedStiffnessRatioAtSimultaneousTarget = atTarget?.loadedStiffnessRatio,
        loadedDrainageCornerAtSimultaneousTarget = atTarget?.loadedDrainageCornerFrequency
    )
}

private fun strokeReadings(
    modelName: String,
    height: Double,
    concentration: Double,
    record: ActuatorOperatingRecord
): List<StrokeReadingRecord> = COUPLING_SCHEMES.map { (scheme, ratio) ->
    val dishing = ratio * record.stroke
    val debyeOffset = THERMAL_DISHING_RMS * THERMAL_DISHING_RMS / (2.0 * record.bulkDebyeLength)
    StrokeReadingRecord(
        model = modelName,
        layerHeight = height,
        concentration = concentration,
        appliedBias = record.appliedBias,
        tileMeanStroke = record.stroke,
        couplingScheme = scheme,
        dishingOverStroke = ratio,
        leverPointStroke = record.stroke + dishing,
        sensorAreaAveragedStroke = record.stroke + debyeOffset,
        sensorDebyeWeightingOffset = debyeOffset,
        leverMinusSensor = dishing - debyeOffset,
        leverMinusSensorOverStroke =
            if (record.stroke > 0.0) (dishing - debyeOffset) / record.stroke else 0.0
    )
}

private fun convergence(
    tileCharge: Double,
    bjerrumLength: Double,
    peg: PegWater,
    geometry: ActuatorGeometry
): List<ActuatorConvergenceRecord> {
    val model = StrongStretchingLayer(desCloizeauxInteraction(peg.crossoverIndex, peg.monomerVolume))
    val chain = peg.graftedChain(model.chainLengthForHeight(peg, 10.0, 0.024), 0.024)
    val sampler = freeBufferSampler(2.0, tileCharge, bjerrumLength)
    val records = mutableListOf<ActuatorConvergenceRecord>()
    var reference = Double.NaN
    listOf(18, 36, 72, 144).forEach { samples ->
        val curve = attractiveForceCurve(
            gradedGapGrid(CURVE_LOWEST_GAP, CURVE_HIGHEST_GAP, samples)
        ) { sampler.force(it, 1.0) }.curve
        val state = ActuatorForceBalance(model, chain, geometry).solve(curve)
        if (samples == 144) reference = state.stroke
        records += ActuatorConvergenceRecord(
            quantity = "stroke, 10 nm / des-Cloizeaux / strong-stretching / 2 mM / 1 V",
            setting = "force-curve samples",
            samples = samples,
            meshNodes = DEFAULT_GAP_MESH_NODES,
            searchNodes = SEARCH_NODES,
            scanSteps = 600,
            blockingForce = state.blockingForce,
            stroke = state.stroke,
            relativeDepartureInStroke = 0.0
        )
    }
    listOf(1000, 2000, 4000, 8000).forEach { nodes ->
        val curve = attractiveForceCurve(
            gradedGapGrid(CURVE_LOWEST_GAP, CURVE_HIGHEST_GAP, CURVE_SAMPLES)
        ) { sampler.force(it, 1.0, nodes) }.curve
        val state = ActuatorForceBalance(model, chain, geometry).solve(curve)
        records += ActuatorConvergenceRecord(
            quantity = "stroke, 10 nm / des-Cloizeaux / strong-stretching / 2 mM / 1 V",
            setting = "Poisson-Boltzmann mesh nodes",
            samples = CURVE_SAMPLES,
            meshNodes = nodes,
            searchNodes = SEARCH_NODES,
            scanSteps = 600,
            blockingForce = state.blockingForce,
            stroke = state.stroke,
            relativeDepartureInStroke = 0.0
        )
    }
    listOf(300, 600, 2400, 9600).forEach { steps ->
        val curve = attractiveForceCurve(
            gradedGapGrid(CURVE_LOWEST_GAP, CURVE_HIGHEST_GAP, CURVE_SAMPLES)
        ) { sampler.force(it, 1.0) }.curve
        val state = ActuatorForceBalance(model, chain, geometry, scanSteps = steps).solve(curve)
        records += ActuatorConvergenceRecord(
            quantity = "stroke, 10 nm / des-Cloizeaux / strong-stretching / 2 mM / 1 V",
            setting = "force-balance scan steps",
            samples = CURVE_SAMPLES,
            meshNodes = DEFAULT_GAP_MESH_NODES,
            searchNodes = SEARCH_NODES,
            scanSteps = steps,
            blockingForce = state.blockingForce,
            stroke = state.stroke,
            relativeDepartureInStroke = 0.0
        )
    }
    return records.map { it.copy(relativeDepartureInStroke = abs(it.stroke / reference - 1.0)) }
}

private fun findings(result: StrokeAndBlockingForceResult): Map<String, String> {
    fun f(value: Double?, digits: Int = 3) =
        if (value == null) "n/a" else "%.${digits}f".format(value)
    val twoMillimolar = result.operatingPoints.filter { it.concentration == 2.0 }
    fun bracket(
        height: Double,
        bias: Double,
        digits: Int = 3,
        select: (ActuatorOperatingRecord) -> Double
    ): String {
        val values = twoMillimolar.filter { it.layerHeight == height && it.appliedBias == bias }
            .map(select)
        return "${f(values.min(), digits)} – ${f(values.max(), digits)}"
    }
    fun thresholdBracket(height: Double, select: (ActuatorThresholdRecord) -> Double?): String {
        val values = result.thresholds
            .filter { it.concentration == 2.0 && it.layerHeight == height }
            .mapNotNull(select)
        return if (values.isEmpty()) "never within 2 V"
        else "${f(values.min())} – ${f(values.max())} V"
    }
    val breached = result.operatingPoints.count { !it.modelValid }
    val stiffening = result.operatingPoints.count { it.electrostaticStiffness > 0.0 }
    return mapOf(
        "the_predicate_is_met_and_the_free_stroke_is_not_the_operating_point" to
                "All three clauses are met, and at biases an order of magnitude below §3's " +
                "ceiling. At 2 mM the SIMULTANEOUS clause — 100 pN delivered AT a 3 nm stroke — " +
                "is reached at " + thresholdBracket(5.0) { it.biasForSimultaneousTarget } +
                " (5 nm), " + thresholdBracket(7.0) { it.biasForSimultaneousTarget } +
                " (7 nm) and " + thresholdBracket(10.0) { it.biasForSimultaneousTarget } +
                " (10 nm) across C-0003's six models. But the FREE stroke at the same bias is " +
                "much larger and its operating point is much lower: the electrostatic force grows " +
                "as the gap closes faster than the layer's osmotic pressure rises over most of " +
                "the range, so an UNLOADED tile runs to near-contact. " +
                "${breached} of ${result.operatingPoints.size} free operating points in the whole " +
                "sweep sit outside at least one upstream validity range (gap below C-0005's " +
                "1.46 nm correlation band, or phi above C-0002's 0.2 concentrated crossover). " +
                "The actuator is therefore only computable AGAINST A LOAD, and that is a design " +
                "statement, not a numerical caveat.",
        "blocking_force_and_free_stroke_need_biases_that_differ_by_up_to_19x" to
                "At 2 mM the 100 pN BLOCKING force needs " +
                thresholdBracket(5.0) { it.biasForHundredPiconewtonBlocking } + " (5 nm), " +
                thresholdBracket(7.0) { it.biasForHundredPiconewtonBlocking } + " (7 nm) and " +
                thresholdBracket(10.0) { it.biasForHundredPiconewtonBlocking } + " (10 nm), " +
                "while the 3 nm FREE STROKE needs " +
                thresholdBracket(5.0) { it.biasForThreeNanometreStroke } + " (5 nm), " +
                thresholdBracket(7.0) { it.biasForThreeNanometreStroke } + " (7 nm) and " +
                thresholdBracket(10.0) { it.biasForThreeNanometreStroke } + " (10 nm). At 10 nm " +
                "the two differ by a factor of up to 30 and they run in OPPOSITE directions with " +
                "layer height: the thick layer is the easy one for stroke and the hard one for " +
                "force. Reporting a single 'bias needed' would hide both facts.",
        "stroke_bracket_at_2_mM" to
                "Free stroke at 2 mM, six-model bracket: at 0.10 V " +
                bracket(5.0, 0.10) { it.stroke } + " nm (5 nm), " +
                bracket(7.0, 0.10) { it.stroke } + " nm (7 nm), " +
                bracket(10.0, 0.10) { it.stroke } + " nm (10 nm); at 1.0 V " +
                bracket(5.0, 1.0) { it.stroke } + " nm (5 nm), " +
                bracket(7.0, 1.0) { it.stroke } + " nm (7 nm), " +
                bracket(10.0, 1.0) { it.stroke } + " nm (10 nm). The 1 V column is the " +
                "collapsed state and is quoted for completeness, not as an operating point.",
        "blocking_force_bracket_at_2_mM" to
                "Blocking force at 2 mM: at 0.10 V " +
                bracket(5.0, 0.10, 1) { it.blockingForce } + " pN (5 nm), " +
                bracket(7.0, 0.10, 1) { it.blockingForce } + " pN (7 nm), " +
                bracket(10.0, 0.10, 1) { it.blockingForce } + " pN (10 nm); at 2 V " +
                bracket(5.0, 2.0, 1) { it.blockingForce } + " pN (5 nm), " +
                bracket(7.0, 2.0, 1) { it.blockingForce } + " pN (7 nm), " +
                bracket(10.0, 2.0, 1) { it.blockingForce } + " pN (10 nm). The six models agree " +
                "EXACTLY on the blocking force, because it is F_es evaluated at L0 and every " +
                "model is constructed to have the same L0. All disagreement is in the stroke.",
        "the_largest_stroke_any_model_can_defend" to
                "The largest stroke reached at an operating point that is inside BOTH upstream " +
                "validity ranges, at 2 mM: " +
                listOf(5.0, 7.0, 10.0).joinToString("; ") { height ->
                    val valid = result.thresholds.filter {
                        it.concentration == 2.0 && it.layerHeight == height
                    }.mapNotNull { it.largestModelValidStroke }
                    "${f(valid.minOrNull())} – ${f(valid.maxOrNull())} nm at ${f(height, 0)} nm"
                } + ". Beyond that the tile is closer to the electrode than C-0005's correlation " +
                "band or the layer is denser than C-0002's concentrated crossover, and the " +
                "numbers are extrapolations of models outside their own domains.",
        "section_1_gets_the_sign_of_k_es_right_only_above_the_force_maximum" to
                "§1 states k_es < 0 and C-0008 confirms it at every gap it sampled — its smallest " +
                "is 3 nm. |F_es| is NON-MONOTONE below that: it rises to a maximum and then falls " +
                "toward the sign change C-0008 already found at V = 0. Past the maximum k_es is " +
                "POSITIVE and the electrostatics STIFFENS the layer. " +
                "${stiffening} of ${result.operatingPoints.size} free operating points in this " +
                "sweep sit on that branch. It is the mechanism that stops the collapse, and it " +
                "is outside §1's picture entirely.",
        "the_buffer_sets_the_force_and_barely_touches_the_stroke" to
                "At 1 V and 10 nm the blocking force runs " +
                result.operatingPoints.filter {
                    it.layerHeight == 10.0 && it.appliedBias == 1.0 &&
                            it.model.startsWith("strong-stretching(des")
                }.sortedBy { it.concentration }.joinToString("; ") {
                    "${f(it.blockingForce, 1)} pN at ${f(it.concentration, 1)} mM"
                } + " — a factor of 21 across the buffer range — while the free operating height " +
                "moves by under 1%, because at that operating point the LAYER sets the height. " +
                "This is leaf A2.2's low-screening condition, quantified: at 10 mM the 100 pN " +
                "target is unreachable at 7 and 10 nm at any bias; at 0.5 mM it is reachable at " +
                "all three heights.",
        "bandwidth_at_the_loaded_operating_point" to
                "C-0004 verifies tau proportional to 1/k_layer EXACTLY, which licenses " +
                "substituting k_eff for k_brush in its corner. At the LOADED operating point — " +
                "the tile held at a 3 nm stroke, which is where the §6 predicate lives — the " +
                "corner at 2 mM is " +
                listOf(5.0, 7.0, 10.0).joinToString("; ") { height ->
                    val corners = result.thresholds.filter {
                        it.concentration == 2.0 && it.layerHeight == height
                    }.mapNotNull { it.loadedDrainageCornerAtSimultaneousTarget }
                    "${f((corners.minOrNull() ?: 0.0) / 1e3, 1)}–" +
                            "${f((corners.maxOrNull() ?: 0.0) / 1e3, 1)} kHz at ${f(height, 0)} nm"
                } + ", against the §3 requirement of 1 kHz. Drainage stays discharged — but it is " +
                "discharged HERE, at k_eff and at the loaded height, not by C-0004's own number, " +
                "which was evaluated at k_brush(L0) with no electrostatics in the model at all.",
        "whose_stroke" to
                "The computed stroke is the tile's MEAN, which under a perfectly uniform load is " +
                "also every point's, exactly (C-0006). It is not what a point-coupled lever " +
                "travels and not what an area-averaging charge sensor reads: on C-0006's own " +
                "ratios a nine-attachment coupling puts the lever 64% of a stroke away from the " +
                "tile mean and a single lever 369%, while the sensor's Debye-weighted reading " +
                "carries a systematic offset of delta^2/(2 lambda_D). Every stroke quoted here " +
                "is the tile mean unless it says otherwise."
    )
}

private fun validity(): List<String> = listOf(
    "MEAN FIELD, inherited whole from C-0008 and C-0005: the one-loop correction is 123-214% of " +
            "the leading term over the entire 5-10 nm range for Mg2+, so PB here is not merely " +
            "inaccurate but UNCONTROLLED, and for the oppositely charged tile-electrode pair no " +
            "published result gives even the direction of the correction. This is the largest " +
            "single uncertainty on every force in this file and it is not reducible by a better " +
            "mean-field solve.",
    "NO FORCE ABOVE ~1 V OF APPLIED BIAS IS TRUSTWORTHY (CH-0007). The 2 V column is reported " +
            "because §3 asks for it. It is 1.2x past the point-ion boundary and separately " +
            "outside the aqueous electrochemical window (1.23 V thermodynamic), which no model " +
            "here addresses (T-11).",
    "ZERO BIAS IS NOT COMPUTED. C-0008 shows the V = 0 force is a sign-changing near-" +
            "cancellation under 4 pN for which no single number is defensible, so the force " +
            "curve admits only strictly attractive samples and the sweep starts at 0.02 V. The " +
            "resting height is therefore taken as L0 exactly, which is right to within a stroke " +
            "of order 4 pN / k_brush.",
    "SUPERPOSITION IS NOT USED anywhere. C-0008 shows it overstates 3.7x one way and understates " +
            "4.0x the other at the working gap. Neither is exp(-h/lambda_D) with lambda_D = " +
            "4 nm: the force's own decay length is 1.8-2.8 nm at the working gap and is " +
            "bias-dependent, and it is measured from the interpolant at every operating point " +
            "rather than assumed.",
    "THE LAYER'S PROFILE MODEL IS NOT SETTLED. C-0003 carries six models and states that the " +
            "spread between the two PROFILE families is a LOWER bound on the profile " +
            "uncertainty, not a full error bar, because the strong-stretching premise " +
            "(L0/R0 >> 1) is not met anywhere in the Gen-1 box. T-1d is narrowing it with a " +
            "numerical SCF profile and may move every stroke here.",
    "EVERY OSMOTIC INPUT IS A BULK PROPERTY APPLIED TO A BRUSH (C-0003, C-0007, P-9). C-0003 " +
            "bounds the exposure at k proportional to K^(1/(m+1)), so a 16x change in the " +
            "interaction strength is a 25% change in stroke — but a NET ATTRACTIVE interaction " +
            "is outside the family of free energies used here entirely.",
    "THE TILE IS NOT A RIGID PLATE (C-0006, CH-0005). The 1-D balance solved here is the tile's " +
            "MEAN displacement under a uniform load, which is the one case where the tile is " +
            "rigid — and it is rigid there exactly, whatever its rigidity. Every other load " +
            "case dishes by 26-369% of the stroke, and a point-coupled lever and an " +
            "area-averaging sensor do not measure the same displacement.",
    "THE LAYER-LOCAL SALT TERM IS CARRIED AS A BOUND, NOT MODELLED. C-0007 shows the layer-local " +
            "Mg2+ goes as 1/h — 33 mM at a 10 nm gap, 66 mM at 5 nm — worth <= 1.7% of the " +
            "modulus at its own ceiling, i.e. <= 0.5% of the stroke through C-0003's " +
            "k ~ K^(4/13). It is the only positive-feedback term anywhere downstream and it is " +
            "NOT included in the balance solved here.",
    "1-D. No edge, no fringing, no lateral structure, so no lateral load profile and hence no " +
            "dishing amplitude of its own — those are cited from C-0006 as ratios (T-3b would " +
            "supply the profile).",
    "Mg2+-FREE IS NOT COMPUTED. Leaf A2.2 asks for the stroke at the low-screening operating " +
            "point, 'Mg2+-free / low-Mg + crosslink'. The sweep is extended to 1 and 0.5 mM " +
            "MgCl2, which is low-Mg; Mg2+-free is a different electrolyte (this solver is 2:1 " +
            "by construction) and the crosslinking that would replace Mg2+ structurally is " +
            "outside every model in this project.",
    "Nothing here is measured. PASS means model-consistent and traceable."
)

private fun openQuestions(): List<String> = listOf(
    "Whether the 10 nm design point exists at all is decided by the PROFILE model, not the " +
            "interaction (C-0003), and T-1d has not landed. Every 10 nm stroke here is " +
            "conditional on that.",
    "The lateral load profile is not computed and a 1-D treatment cannot compute it (C-0008). " +
            "It is what converts C-0006's exactly-linear dishing result into an amplitude, and " +
            "it is what decides whether the lever and the sensor readings quoted here are " +
            "separated by 26% or by more.",
    "The bias at which the coupled system folds — where the stable and unstable equilibria " +
            "merge — is detected here (furtherEquilibria) but not located to better than the " +
            "bias sample spacing. That is T-4's task, and this file hands it k_eff AT the " +
            "operating point rather than at the resting height, which is the quantity C-0008 " +
            "could not supply.",
    "Whether the electrode can be biased at all in aqueous MgCl2 above ~1.23 V is an " +
            "electrochemistry question no model here touches (T-11). Because the force " +
            "saturates above ~0.5 V it barely moves the verdict, but that is luck.",
    "The output work reported here assumes the load is delivered uniformly. C-0006 shows no " +
            "discrete attachment scheme is flat (>= 55 load paths needed against 43.7 " +
            "independent patches), so the work actually delivered through a real coupling is " +
            "lower by an amount this task cannot compute."
)

private fun report(result: StrokeAndBlockingForceResult, output: File) {
    println()
    println("T-3 — ${result.title}")
    println("leaf ${result.leaf}; 300 K, aqueous MgCl2")
    println()
    println("--- design points ".padEnd(110, '-'))
    println("%34s %6s %8s %10s %10s %12s %12s".format(
        "model", "L0", "N", "L0(solved)", "phi", "k(L0)", "k(0.8L0)"
    ))
    result.designPoints.forEach {
        println("%34s %6.1f %8.1f %10.4f %10.4f %12.3f %12.3f".format(
            it.model.take(34), it.layerHeight, it.monomersPerChain, it.restingHeight,
            it.restingVolumeFraction, it.stiffnessAtRest, it.stiffnessAtFourFifths
        ))
    }
    println()
    println("--- coupled operating points, 2 mM free buffer ".padEnd(110, '-'))
    println("%34s %5s %6s %10s %9s %9s %10s %9s".format(
        "model", "L0", "V", "F_block", "stroke", "k_eff", "k_eff/kb", "W(3nm)"
    ))
    result.operatingPoints.filter { it.concentration == 2.0 && it.appliedBias in listOf(0.25, 1.0, 2.0) }
        .forEach {
            println("%34s %5.1f %6.2f %10.1f %9.3f %9.2f %10.4f %9.1f".format(
                it.model.take(34), it.layerHeight, it.appliedBias, it.blockingForce, it.stroke,
                it.effectiveStiffness, it.stiffnessRatio,
                it.outputForceAtThreeNanometres ?: Double.NaN
            ))
        }
    println()
    println("--- thresholds, 2 mM ".padEnd(110, '-'))
    println("%34s %5s %10s %10s %10s %10s %10s %10s".format(
        "model", "L0", "V(100pN)", "V(3nm)", "V(both)", "V(valid)", "s(valid)", "fc(kHz)"
    ))
    result.thresholds.filter { it.concentration == 2.0 }.forEach {
        println("%34s %5.1f %10.4f %10.4f %10.4f %10.4f %10.3f %10.1f".format(
            it.model.take(34), it.layerHeight,
            it.biasForHundredPiconewtonBlocking ?: Double.NaN,
            it.biasForThreeNanometreStroke ?: Double.NaN,
            it.biasForSimultaneousTarget ?: Double.NaN,
            it.largestModelValidBias ?: Double.NaN,
            it.largestModelValidStroke ?: Double.NaN,
            (it.loadedDrainageCornerAtSimultaneousTarget ?: Double.NaN) / 1e3
        ))
    }
    println()
    println("--- thresholds across the buffer sweep, strong-stretching(des-Cloizeaux) ".padEnd(110, '-'))
    println("%6s %5s %10s %10s %10s %10s %10s".format(
        "c[mM]", "L0", "V(100pN)", "V(3nm)", "V(both)", "V(valid)", "s(valid)"
    ))
    result.thresholds.filter { it.model.startsWith("strong-stretching(des") }.forEach {
        println("%6.1f %5.1f %10.4f %10.4f %10.4f %10.4f %10.3f".format(
            it.concentration, it.layerHeight,
            it.biasForHundredPiconewtonBlocking ?: Double.NaN,
            it.biasForThreeNanometreStroke ?: Double.NaN,
            it.biasForSimultaneousTarget ?: Double.NaN,
            it.largestModelValidBias ?: Double.NaN,
            it.largestModelValidStroke ?: Double.NaN
        ))
    }
    println()
    println("--- the PEG layer as the medium, 2 mM ".padEnd(110, '-'))
    println("%34s %5s %6s %10s %10s %10s %10s".format(
        "model", "L0", "V", "F_bare", "F_layer", "amp(F)", "amp(s)"
    ))
    result.layerMedium.forEach {
        println("%34s %5.1f %6.2f %10.1f %10.1f %10.4f %10.4f".format(
            it.model.take(34), it.layerHeight, it.appliedBias, it.blockingForceInFreeBuffer,
            it.blockingForceWithLayer, it.blockingForceAmplification, it.strokeAmplification
        ))
    }
    println()
    println("--- convergence ".padEnd(110, '-'))
    println("%56s %8s %8s %8s %10s %12s".format(
        "setting", "samples", "nodes", "scan", "stroke", "departure"
    ))
    result.convergence.forEach {
        println("%56s %8d %8d %8d %10.5f %12.3e".format(
            it.setting, it.samples, it.meshNodes, it.scanSteps, it.stroke,
            it.relativeDepartureInStroke
        ))
    }
    println()
    println("--- FINDINGS ".padEnd(110, '-'))
    result.findings.forEach { (key, value) -> println("$key:\n  $value\n") }
    println("written: ${output.path}")
}
