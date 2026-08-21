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

package com.xemantic.nano.plentyofroom.crossover

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.actuator.ActuatorForceBalance
import com.xemantic.nano.plentyofroom.actuator.ActuatorGeometry
import com.xemantic.nano.plentyofroom.actuator.BiasCeiling
import com.xemantic.nano.plentyofroom.actuator.DiffuseParametrisedField
import com.xemantic.nano.plentyofroom.actuator.EquilibriumPath
import com.xemantic.nano.plentyofroom.actuator.FieldSample
import com.xemantic.nano.plentyofroom.actuator.biasMargin
import com.xemantic.nano.plentyofroom.actuator.bindingCeiling
import com.xemantic.nano.plentyofroom.actuator.roundedForActuatorResult
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
import com.xemantic.nano.plentyofroom.electrostatics.sternChargeDensityPerVolt
import com.xemantic.nano.plentyofroom.electrostatics.thermalVoltage
import com.xemantic.nano.plentyofroom.electrostatics.uniformMedium
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.material.ReciprocalTemperatureChi
import com.xemantic.nano.plentyofroom.material.WATER_MASS_DENSITY_AT_300K
import com.xemantic.nano.plentyofroom.material.monomerExcludedVolume
import com.xemantic.nano.plentyofroom.material.waterMoleculeVolume
import com.xemantic.nano.plentyofroom.structure.roundedForProse
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Task `T-21` — the semidilute→concentrated crossover **derived for this layer**, replacing the
 * cited `φ ≈ 0.2–0.3` band `C-0002` carried and `C-0018` made load-bearing at 121 of 162 states.
 *
 * Run with:
 *
 * ```shell
 * tools/study.sh crossover.ConcentratedCrossoverStudyKt
 * ```
 *
 * Emits `gpd/results/T-21-concentrated-crossover.json`, deterministically.
 *
 * Three parts:
 *
 * 1. **the derivation** — the crossover family `φ_c(n) = (v_K/b³) n^(−1/2)`, evaluated on
 *    `C-0002`'s measured parameters, with the member each definition selects;
 * 2. **the premise** — the des Cloizeaux window `(φ*, φ**)` at every Gen-1 chain length, and the
 *    exact identity `φ** over φ* = √(N_K/g_T)` that makes it empty;
 * 3. **the propagation** — `C-0018`'s 162 bias ceilings re-read at each candidate crossover, on
 *    `C-0018`'s own `EquilibriumPath` + `PoissonBoltzmannGap` pipeline. The **pull-in** bias does
 *    not depend on the crossover at all, so it is read from `C-0018`'s result file rather than
 *    re-located, and the rebuild is graded by reproducing `C-0018`'s own `φ = 0.2` ceiling.
 */

// ---------------------------------------------------------------------------------------------
// records — every one prefixed T21, because study record classes are package-scoped and collide
// ---------------------------------------------------------------------------------------------

/** One member of the crossover family, with the criterion that selects it. */
@Serializable
data class T21CrossoverRecord(
    val name: String,
    val criterion: String,
    val segmentsPerBlob: Double?,
    val volumeFraction: Double,
    val correlationLength: Double?,
    val provenance: String,
    val note: String
)

/** The des Cloizeaux window at one Gen-1 chain length. */
@Serializable
data class T21WindowRecord(
    val layerHeight: Double,
    val model: String,
    val monomersPerChain: Double,
    val kuhnSegments: Double,
    val thermalBlobSegmentsScaling: Double,
    val thermalBlobSegmentsExact: Double,
    val overlapVolumeFraction: Double,
    val excludedVolumeCrossover: Double,
    val windowWidthRatio: Double,
    val windowIsEmpty: Boolean,
    val restingVolumeFraction: Double,
    val restingOverExcludedVolumeCrossover: Double,
    val chainExtensionRatio: Double
)

/**
 * One corner of the `2 x 2` of (thermal-blob normalisation) x (excluded-volume route).
 *
 * The emptiness of the des Cloizeaux window is **not** robust across all four, and this record
 * exists so that the one corner in which the window exists is reported rather than averaged away.
 */
@Serializable
data class T21CornerRecord(
    val excludedVolumeRoute: String,
    val monomerExcludedVolume: Double,
    val thermalBlobNormalisation: String,
    val thermalBlobSegments: Double,
    val excludedVolumeCrossover: Double,
    val chains: Int,
    val chainsWithEmptyWindow: Int,
    val widestWindowRatio: Double,
    val restingOverCrossoverLow: Double,
    val restingOverCrossoverHigh: Double,
    val compressedOverCrossoverLow: Double,
    val compressedOverCrossoverHigh: Double
)

/** One `(state, candidate crossover)` cell of the propagation. */
@Serializable
data class T21CeilingRecord(
    val model: String,
    val layerHeight: Double,
    val graftingDensity: Double,
    val concentration: Double,
    val loadLine: String,
    val crossoverName: String,
    val crossoverVolumeFraction: Double,
    val dryThickness: Double,
    val restingHeight: Double,
    val crossoverGap: Double,
    val crossoverReachedAboveRest: Boolean,
    val crossoverBias: Double?,
    val correlationBandBias: Double?,
    val pullInBias: Double?,
    val operatingBias: Double?,
    val bindingCeilingName: String?,
    val usableBias: Double?,
    val margin: Double?,
    val operatingPointIsUsable: Boolean
)

/** The binding-ceiling census at one candidate crossover, over all 162 states. */
@Serializable
data class T21CensusRecord(
    val crossoverName: String,
    val crossoverVolumeFraction: Double,
    val states: Int,
    val boundByCrossover: Int,
    val boundByPullIn: Int,
    val boundByCorrelationBand: Int,
    val boundByPointIon: Int,
    val statesViolatedAtRestingHeight: Int,
    val usableBiasLow: Double?,
    val usableBiasHigh: Double?,
    val marginLow: Double?,
    val marginHigh: Double?,
    val operatingPointsUsable: Int,
    val coupledMarginLow: Double?,
    val coupledMarginHigh: Double?,
    val coupledStatesWithMarginBelowOne: Int,
    val coupledStatesWithNoOperatingBias: Int
)

/** A gate-5 reproduction of an upstream number through this task's own rebuild. */
@Serializable
data class T21ReproductionRecord(
    val quantity: String,
    val here: Double,
    val upstream: Double,
    val relativeDeparture: Double,
    val source: String
)

@Serializable
@Suppress("LongParameterList")
data class T21Result(
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
    val family: List<T21CrossoverRecord>,
    val windows: List<T21WindowRecord>,
    val corners: List<T21CornerRecord>,
    val ceilings: List<T21CeilingRecord>,
    val census: List<T21CensusRecord>,
    val reproductions: List<T21ReproductionRecord>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>
)

// ---------------------------------------------------------------------------------------------
// the run parameters, all inherited rather than chosen
// ---------------------------------------------------------------------------------------------

/** `C-0001`'s design points, re-used unchanged so that `C-0018`'s states can be keyed exactly. */
private val DESIGN_POINTS = listOf(5.0 to 0.092, 7.0 to 0.045, 10.0 to 0.024)

private val BUFFERS = listOf(0.5, 2.0, 10.0)

private const val TARGET_FORCE = 100.0

private const val TARGET_STROKE = 3.0

private const val FOOTPRINT = 1600.0

private const val MESH_NODES = 2000

private const val STERN_CAPACITANCE = 20.0

private const val CURVE_LOWEST_GAP = 0.5

/** `C-0005`'s lateral counterion spacing — `C-0018`'s other validity ceiling, carried unchanged. */
private const val CORRELATION_ATTRACTION_GAP = 1.46

/** `CH-0007`'s point-ion boundary in applied bias. */
private const val TRUSTED_BIAS_CEILING = 1.0

/** `C-0003`'s measured osmotic virial coefficients, in mol·cm³/g² and mol·cm⁶/g³. */
private const val OSMOTIC_SECOND_VIRIAL = 1.9e-3

private const val OSMOTIC_THIRD_VIRIAL = 2.0e-2

/**
 * The largest **weight** fraction in the osmometry the adopted equation of state was fitted to,
 * over **all twelve** molecular weights: 67.5 wt %, on PEG-600.
 *
 * **CITED, and it CORRECTS `C-0002`.** `C-0002` records the fit range as "0–50 wt %"; the paper
 * states no range at all, and the source data it names (Rand's tabulation, recovered from the
 * Wayback Machine) run **1.5 – 67.5 wt %**. Confirmed twice over: the rightmost datum of the
 * paper's own Fig. 1 sits at `log C ≈ −0.13`, which is PEG-600 at 67.5 wt % under the caption's
 * own `V̄` conversion; and Marsh (*Biophys. J.* **86**:2630, 2004), fitting the same data, quotes
 * volume fractions to 0.53.
 */
private const val EQUATION_OF_STATE_FIT_WEIGHT_FRACTION = 0.675

/**
 * The largest weight fraction at the molecular weight **closest to the Gen-1 chain**: 54 wt %,
 * on PEG-8000, against a Gen-1 chain of ≈ 8.8 kDa.
 *
 * The des Cloizeaux limb is chain-length independent *by construction* and was fitted jointly to
 * all twelve, so the global range is the support of the limb. This narrower one is carried beside
 * it because a reader who wants the support at *this* chain length is entitled to it, and it is
 * the conservative reading.
 */
private const val EQUATION_OF_STATE_FIT_WEIGHT_FRACTION_AT_GEN1_CHAIN = 0.54

private val UPSTREAM = File("gpd/results/T-4-maximum-usable-bias.json")

// ---------------------------------------------------------------------------------------------

/** The field, exactly as `C-0018` builds it — one Poisson-Boltzmann solve per diffuse drop. */
private class Field(
    concentration: Double,
    private val tileCharge: Double,
    private val bjerrum: Double
) {

    private val ions = IonModel(MagnesiumChlorideBuffer(concentration).magnesiumNumberDensity)

    private val medium = uniformMedium(GapMedium())

    private val stern = sternChargeDensityPerVolt(STERN_CAPACITANCE)

    private val volt = thermalVoltage()

    fun sample(gap: Double, diffusePotential: Double): FieldSample {
        val solution = PoissonBoltzmannGap(gap, ions, medium, bjerrum, nodes = MESH_NODES)
            .solve(diffusePotential / volt, tileCharge)
        return FieldSample(
            gap = gap,
            diffusePotential = diffusePotential,
            appliedBias = diffusePotential + solution.electrodeSurfaceChargeDensity / stern,
            force = solution.forceOnTile(FOOTPRINT)
        )
    }

    fun asPath(): DiffuseParametrisedField = DiffuseParametrisedField { gap, psi -> sample(gap, psi) }
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

private fun layerModels(peg: PegWater): List<GraftedLayerModel> =
    listOf("alexander-box", "strong-stretching").flatMap { profile ->
        listOf("two-body", "virial", "des-Cloizeaux").map { interaction ->
            val energy = interactionFor(peg, interaction)
            if (profile == "alexander-box") AlexanderBoxLayer(energy) else StrongStretchingLayer(energy)
        }
    }

/** One row of `C-0018`'s published ceiling table, keyed on every dimension that sweep varied. */
private data class UpstreamCeiling(
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
    val operatingBias: Double?,
    val pullInBias: Double?,
    val pullInStroke: Double?,
    val correlationBandBias: Double?,
    val concentratedCrossoverBias: Double?
)

private fun readUpstream(): List<UpstreamCeiling> {
    require(UPSTREAM.exists()) {
        "C-0018's result file is required and was not found: ${UPSTREAM.absolutePath}"
    }
    val root = Json.parseToJsonElement(UPSTREAM.readText()).jsonObject
    return root.getValue("ceilings").jsonArray.map { element ->
        val o = element.jsonObject
        fun number(key: String): Double? =
            o[key]?.jsonPrimitive?.takeIf { it.content != "null" }?.content?.toDoubleOrNull()
        UpstreamCeiling(
            model = o.getValue("model").jsonPrimitive.content,
            layerHeight = number("layerHeight")!!,
            graftingDensity = number("graftingDensity")!!,
            concentration = number("concentration")!!,
            loadLine = o.getValue("loadLine").jsonPrimitive.content,
            couplingStiffness = number("couplingStiffness")!!,
            deadLoad = number("deadLoad")!!,
            restingHeight = number("restingHeight")!!,
            dryThickness = number("dryThickness")!!,
            strokeCeiling = number("strokeCeiling")!!,
            operatingBias = number("operatingBias"),
            pullInBias = number("pullInBias"),
            pullInStroke = number("pullInStroke"),
            correlationBandBias = number("correlationBandBias"),
            concentratedCrossoverBias = number("concentratedCrossoverBias")
        )
    }
}

// ---------------------------------------------------------------------------------------------

@Suppress("LongMethod", "CyclomaticComplexMethod")
fun main() {
    val peg = PegWater()
    val correlation = peg.semidiluteCorrelation(OSMOTIC_SECOND_VIRIAL)
    val fitVolumeFraction = weightToVolumeFraction(
        EQUATION_OF_STATE_FIT_WEIGHT_FRACTION, peg.massDensity, WATER_MASS_DENSITY_AT_300K
    )
    val fitAtGen1ChainVolumeFraction = weightToVolumeFraction(
        EQUATION_OF_STATE_FIT_WEIGHT_FRACTION_AT_GEN1_CHAIN, peg.massDensity,
        WATER_MASS_DENSITY_AT_300K
    )

    println("T-21 — the crossover family, derived from C-0002's own parameters ...")
    val family = crossoverFamily(correlation, fitVolumeFraction, fitAtGen1ChainVolumeFraction)

    println("T-21 — the des Cloizeaux window at every Gen-1 chain ...")
    val models = layerModels(peg)
    val windows = DESIGN_POINTS.flatMap { (height, density) ->
        models.map { model ->
            val monomers = model.chainLengthForHeight(peg, height, density)
            val chain = peg.graftedChain(monomers, density)
            val kuhn = monomers / peg.monomersPerKuhnSegment
            val window = correlation.desCloizeauxWindow(kuhn)
            val resting = chain.occupiedThickness / height
            T21WindowRecord(
                layerHeight = height,
                model = model.name,
                monomersPerChain = monomers,
                kuhnSegments = kuhn,
                thermalBlobSegmentsScaling = correlation.thermalBlobSegments,
                thermalBlobSegmentsExact = correlation.exact.thermalBlobSegments,
                overlapVolumeFraction = window.lower,
                excludedVolumeCrossover = window.upper,
                windowWidthRatio = window.widthRatio,
                windowIsEmpty = window.isEmpty,
                restingVolumeFraction = resting,
                restingOverExcludedVolumeCrossover = resting / window.upper,
                // the one thing that could break the layer's inheritance of a bulk crossover:
                // a chain stretched inside its own correlation blob. h over the contour length.
                chainExtensionRatio = height / (kuhn * peg.kuhnLength)
            )
        }
    }

    println("T-21 — the 2 x 2 of thermal-blob normalisation and excluded-volume route ...")
    val corners = corners(peg, windows)

    println("T-21 — re-reading C-0018's 162 ceilings at each candidate crossover ...")
    val upstream = readUpstream()
    val tile = DnaOrigamiTile()
    val lb = bjerrumLength()
    val surviving = tile.manningSurvivingFraction(2, lb)
    val tileCharge = -(tile.projectedChargeDensity * surviving / 2.0)
    val fields = BUFFERS.associateWith { Field(it, tileCharge, lb) }
    val geometry = ActuatorGeometry()
    val candidates = family.filter { it.volumeFraction <= 1.0 }
    val ceilings = mutableListOf<T21CeilingRecord>()
    upstream.forEach { state ->
        val model = models.first { it.name == state.model }
        val chain = peg.graftedChain(
            model.chainLengthForHeight(peg, state.layerHeight, state.graftingDensity),
            state.graftingDensity
        )
        val balance = ActuatorForceBalance(model, chain, geometry)
        val field = fields.getValue(state.concentration)
        val floor = max(chain.occupiedThickness * 1.01, CURVE_LOWEST_GAP)
        val strokeCeiling = balance.restingHeight - floor
        val path = EquilibriumPath(
            restingHeight = balance.restingHeight,
            strokeCeiling = strokeCeiling,
            field = field.asPath()
        ) { stroke ->
            state.deadLoad + state.couplingStiffness * stroke +
                    balance.layerLoad(balance.restingHeight - stroke)
        }
        val foldStroke = state.pullInStroke ?: strokeCeiling
        candidates.forEach { candidate ->
            val gap = gapAtVolumeFraction(chain.occupiedThickness, candidate.volumeFraction)
            val stroke = balance.restingHeight - gap
            val beyondFold = stroke > foldStroke
            val bias = if (stroke <= 0.0 || stroke > strokeCeiling) null
            else path.at(stroke)?.appliedBias
            val ceilingCandidates = listOf(
                BiasCeiling("static stability (pull-in)", state.pullInBias),
                BiasCeiling(
                    "correlation band (C-0005, 1.46 nm)",
                    if (balance.restingHeight - CORRELATION_ATTRACTION_GAP > foldStroke) null
                    else state.correlationBandBias
                ),
                BiasCeiling(
                    "concentrated crossover (T-21, phi = ${candidate.volumeFraction.roundedForProse()})",
                    if (beyondFold) null else bias
                ),
                BiasCeiling("point-ion boundary (CH-0007, 1.0 V)", TRUSTED_BIAS_CEILING)
            )
            val binding = bindingCeiling(ceilingCandidates)
            val usableBias = binding?.bias
            ceilings += T21CeilingRecord(
                model = state.model,
                layerHeight = state.layerHeight,
                graftingDensity = state.graftingDensity,
                concentration = state.concentration,
                loadLine = state.loadLine,
                crossoverName = candidate.name,
                crossoverVolumeFraction = candidate.volumeFraction,
                dryThickness = chain.occupiedThickness,
                restingHeight = balance.restingHeight,
                crossoverGap = gap,
                // the criterion is already violated at the resting height: no bias is usable
                crossoverReachedAboveRest = stroke <= 0.0,
                crossoverBias = bias,
                correlationBandBias = state.correlationBandBias,
                pullInBias = state.pullInBias,
                operatingBias = state.operatingBias,
                bindingCeilingName = binding?.name,
                usableBias = usableBias,
                margin = biasMargin(usableBias, state.operatingBias),
                operatingPointIsUsable = usableBias != null &&
                        state.operatingBias != null && state.operatingBias <= usableBias
            )
        }
        print(".")
    }
    println()

    val census = candidates.map { candidate ->
        val cells = ceilings.filter { it.crossoverName == candidate.name }
        val usable = cells.mapNotNull { it.usableBias }
        val margins = cells.mapNotNull { it.margin }
        val coupled = cells.filter { it.loadLine == "coupled" }
        val coupledMargins = coupled.mapNotNull { it.margin }
        T21CensusRecord(
            crossoverName = candidate.name,
            crossoverVolumeFraction = candidate.volumeFraction,
            states = cells.size,
            boundByCrossover = cells.count {
                it.bindingCeilingName?.startsWith("concentrated crossover") == true
            },
            boundByPullIn = cells.count { it.bindingCeilingName?.startsWith("static") == true },
            boundByCorrelationBand = cells.count {
                it.bindingCeilingName?.startsWith("correlation") == true
            },
            boundByPointIon = cells.count { it.bindingCeilingName?.startsWith("point-ion") == true },
            statesViolatedAtRestingHeight = cells.count { it.crossoverReachedAboveRest },
            usableBiasLow = usable.minOrNull(),
            usableBiasHigh = usable.maxOrNull(),
            marginLow = margins.minOrNull(),
            marginHigh = margins.maxOrNull(),
            operatingPointsUsable = cells.count { it.operatingPointIsUsable },
            coupledMarginLow = coupledMargins.minOrNull(),
            coupledMarginHigh = coupledMargins.maxOrNull(),
            coupledStatesWithMarginBelowOne = coupledMargins.count { it < 1.0 },
            coupledStatesWithNoOperatingBias = coupled.count { it.operatingBias == null }
        )
    }

    println("T-21 — gate 5, reproducing C-0018 through this task's own rebuild ...")
    val reproductions = reproductions(ceilings, upstream, correlation, peg)

    val result = T21Result(
        task = "T-21",
        leaf = "A2.1 (premise), consumed by A2.2",
        title = "The semidilute-to-concentrated crossover derived for the Gen-1 PEG layer: a " +
                "one-parameter family phi_c(n) = (v_K/b^3) n^(-1/2) whose member is chosen by " +
                "naming how many Kuhn segments the correlation blob must keep, the finding that " +
                "the des Cloizeaux window is EMPTY for every Gen-1 chain, and C-0018's 162 bias " +
                "ceilings re-read at each candidate",
        verificationType = "logical (closed-form family and the exact window identity) + " +
                "in-silico (C-0018's own EquilibriumPath and PoissonBoltzmannGap re-run at the " +
                "candidate crossover gaps; the pull-in bias, which cannot depend on the " +
                "crossover, read from C-0018's result file and flagged CITED)",
        acceptance = "P1: an upper crossover DERIVED from C-0002's measured v0, b, n_K and the " +
                "measured excluded volume, with its definition named and the rest of the family " +
                "quoted beside it. P2: the premises of the blob picture checked against the " +
                "actual material before the picture is used. P3: whether a grafted layer " +
                "inherits the crossover at all. P4: C-0018's ceilings re-read, with the " +
                "direction of the movement stated even when it is unfavourable.",
        maturity = "TRL 1-3 - model-consistent and traceable. NOTHING HERE IS MEASURED. The " +
                "crossover family is a scaling construction on measured parameters; the fitted " +
                "range of the equation of state is the only genuinely measured boundary in it.",
        units = mapOf(
            "length" to "nm",
            "volume" to "nm^3",
            "volumeFraction" to "1 (PHYSICAL, phi = c v0 = N sigma v0 / h)",
            "potential" to "V",
            "concentration" to "mM",
            "temperature" to "K"
        ),
        conventions = CONVENTIONS,
        runParameters = mapOf(
            "temperature" to ROOM_TEMPERATURE.toString(),
            "thermalEnergy" to thermalEnergy().toString(),
            "kuhnLength" to peg.kuhnLength.toString(),
            "kuhnSegmentVolume" to peg.kuhnSegmentVolume.roundedForProse().toString(),
            "kuhnPairExcludedVolume" to correlation.kuhnPairExcludedVolume.roundedForProse().toString(),
            "kuhnAspectRatio" to correlation.kuhnAspectRatio.roundedForProse().toString(),
            "monomerVolume" to peg.monomerVolume.roundedForProse().toString(),
            "monomersPerKuhnSegment" to peg.monomersPerKuhnSegment.roundedForProse().toString(),
            "osmoticSecondVirial" to OSMOTIC_SECOND_VIRIAL.toString(),
            "osmoticThirdVirial" to OSMOTIC_THIRD_VIRIAL.toString(),
            "reducedSecondVirial" to peg.reducedSecondVirialCoefficient(OSMOTIC_SECOND_VIRIAL)
                .roundedForProse().toString(),
            "thermalBlobSegmentsScaling" to correlation.thermalBlobSegments.roundedForProse().toString(),
            "thermalBlobSegmentsExact" to correlation.exact.thermalBlobSegments.roundedForProse().toString(),
            "fixmanPrefactor" to correlation.exact.fixmanPrefactor.roundedForProse().toString(),
            "equationOfStateFitWeightFraction" to EQUATION_OF_STATE_FIT_WEIGHT_FRACTION.toString(),
            "equationOfStateFitVolumeFraction" to fitVolumeFraction.roundedForProse().toString(),
            "equationOfStateFitWeightFractionAtGen1Chain" to
                    EQUATION_OF_STATE_FIT_WEIGHT_FRACTION_AT_GEN1_CHAIN.toString(),
            "equationOfStateFitVolumeFractionAtGen1Chain" to
                    fitAtGen1ChainVolumeFraction.roundedForProse().toString(),
            "layerHeights" to DESIGN_POINTS.map { it.first }.toString(),
            "graftingDensities" to DESIGN_POINTS.map { it.second }.toString(),
            "buffers" to BUFFERS.toString(),
            "meshNodes" to MESH_NODES.toString(),
            "correlationAttractionGap" to CORRELATION_ATTRACTION_GAP.toString(),
            "upstreamCeilingFile" to UPSTREAM.path
        ),
        citedInputs = CITED,
        family = family,
        windows = windows,
        corners = corners,
        ceilings = ceilings,
        census = census,
        reproductions = reproductions,
        findings = emptyMap(),
        validity = VALIDITY,
        openQuestions = OPEN
    )
    val complete = result.copy(findings = findings(result))
    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-21-concentrated-crossover.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(json.encodeToJsonElement(complete).roundedForActuatorResult()) + "\n"
    )
    report(complete, output)
}

// ---------------------------------------------------------------------------------------------
// the family
// ---------------------------------------------------------------------------------------------

private fun crossoverFamily(
    correlation: SemidiluteCorrelation,
    fitVolumeFraction: Double,
    fitAtGen1ChainVolumeFraction: Double
): List<T21CrossoverRecord> {
    fun blobRow(name: String, segments: Double, criterion: String, note: String) =
        T21CrossoverRecord(
            name = name,
            criterion = criterion,
            segmentsPerBlob = segments,
            volumeFraction = correlation.volumeFractionAtSegmentsPerBlob(segments),
            correlationLength = correlation.idealBlobSize(
                correlation.volumeFractionAtSegmentsPerBlob(segments)
            ),
            provenance = "DERIVED — phi_c(n) = (v_K/b^3) n^(-1/2) on C-0002's measured b, v_K " +
                    "and the measured Kuhn-pair excluded volume",
            note = note
        )
    return listOf(
        blobRow(
            "excluded-volume crossover (Yamakawa exact g_T)",
            correlation.exact.thermalBlobSegments,
            "the correlation blob stops being swollen: n = g_T with z(g_T) = 1",
            "THE crossover the des Cloizeaux exponent actually has. Below the layer's own " +
                    "resting volume fraction at every design point, and below the " +
                    "dilute-to-semidilute crossover phi# — so the window between them is EMPTY."
        ),
        blobRow(
            "excluded-volume crossover (scaling g_T)",
            correlation.thermalBlobSegments,
            "the correlation blob stops being swollen: n = g_T with g_T = (b^3/v)^2",
            "the same criterion in the scaling normalisation of the thermal blob; the two " +
                    "differ by the published 9.19 convention factor and neither reaches 0.02."
        ),
        blobRow(
            "one Kuhn segment per blob",
            1.0,
            "the correlation blob holds one statistical segment: xi = b",
            "the member of the family the cited 0.2-0.3 band was trying to be. It is the " +
                    "reciprocal of C-0002's Kuhn aspect ratio and NOTHING else, and it is BELOW " +
                    "the cited floor."
        ),
        T21CrossoverRecord(
            name = "correlation length at the monomer scale",
            criterion = "xi = v0^(1/3)",
            segmentsPerBlob = null,
            volumeFraction = correlation.monomerScaleCorrelation,
            correlationLength = correlation.monomerVolume.pow(1.0 / 3.0),
            provenance = "DERIVED — v_K/(b^2 v0^(1/3))",
            note = "not a member of the family: below one Kuhn segment the blob has no Gaussian " +
                    "statistics left to shrink, so this is an extrapolation of a construction " +
                    "past its own floor. Quoted because it is the upper end of the spread."
        ),
        T21CrossoverRecord(
            name = "monomer-level thermal blob (mis-coarse-grained)",
            criterion = "v_m/v0, the excluded-volume criterion read on MONOMERS",
            segmentsPerBlob = null,
            volumeFraction = correlation.monomerLevelCrossover,
            correlationLength = null,
            provenance = "DERIVED, and WRONG BY CONSTRUCTION — kept because it is 0.203",
            note = "identifies the statistical segment with the monomer, which for PEG is wrong " +
                    "by n_K in length and n_K^2 in excluded volume (CH-0020). It lands on the " +
                    "FLOOR OF THE CITED BAND to three digits, and C-0007's parameter sheet " +
                    "reports it as 'the thermal blob volume fraction'. 16.2x the Kuhn reading."
        ),
        T21CrossoverRecord(
            name = "Flory-Huggins 1 - 2 chi (the cited band's likely provenance)",
            criterion = "Rubinstein & Colby eq (5.36) phi** = v/b^3 combined with eq (5.1) " +
                    "v = (1 - 2 chi) b^3, i.e. the lattice site taken to BE the monomer",
            segmentsPerBlob = null,
            volumeFraction = 1.0 - 2.0 * ReciprocalTemperatureChi().chi(ROOM_TEMPERATURE),
            correlationLength = null,
            provenance = "DERIVED from C-0007's measured chi(300 K) = 0.3717, on a convention " +
                    "this project forbids",
            note = "0.257 — the MIDDLE of the cited 0.2-0.3 band, and the most likely thing the " +
                    "band actually is. It identifies the Kuhn length's cube with the monomer " +
                    "volume, which for PEG are 1.331 and 0.0604 nm^3. Read consistently on Kuhn " +
                    "segments the same chi gives 0.032."
        ),
        T21CrossoverRecord(
            name = "equation-of-state support ceiling (all 12 molecular weights)",
            criterion = "the largest volume fraction the adopted equation of state was FITTED at",
            segmentsPerBlob = null,
            volumeFraction = fitVolumeFraction,
            correlationLength = null,
            provenance = "CITED (67.5 wt %, the recovered source data of Cohen et al. 2009 — " +
                    "which CORRECTS C-0002's '0-50 wt %') + DERIVED (the weight-to-volume " +
                    "conversion on C-0002's partial specific volume)",
            note = "NOT a scaling crossover, and it is the axis C-0018 actually consumes: the " +
                    "layer's constitutive law is a FIT, and a fit does not need a blob to be " +
                    "right, it needs data. This is where the data stop."
        ),
        T21CrossoverRecord(
            name = "equation-of-state support ceiling (PEG-8000, the Gen-1 chain length)",
            criterion = "the largest volume fraction fitted at the CLOSEST molecular weight",
            segmentsPerBlob = null,
            volumeFraction = fitAtGen1ChainVolumeFraction,
            correlationLength = null,
            provenance = "CITED (54 wt % on PEG-8000) + DERIVED (the conversion)",
            note = "the conservative reading of the support ceiling, and the one this task " +
                    "recommends: the Gen-1 chain is 8.8 kDa."
        ),
        T21CrossoverRecord(
            name = "the cited band, floor",
            criterion = "none — read off a literature band by C-0001 and carried by C-0002",
            segmentsPerBlob = null,
            volumeFraction = 0.2,
            correlationLength = null,
            provenance = "CITED, UNTRACED — the number this task exists to replace",
            note = "C-0018's binding ceiling at 121 of 162 states."
        ),
        T21CrossoverRecord(
            name = "the cited band, ceiling",
            criterion = "none — read off a literature band",
            segmentsPerBlob = null,
            volumeFraction = 0.3,
            correlationLength = null,
            provenance = "CITED, UNTRACED",
            note = "the end of the band C-0018 did NOT use."
        )
    )
}

// ---------------------------------------------------------------------------------------------
// the four corners
// ---------------------------------------------------------------------------------------------

/**
 * `C-0007`'s Flory-Huggins route to the monomer excluded volume, re-derived here rather than
 * quoted: `v = v0 (v0/v_water)(1 - 2 chi)` at the measured `chi(300 K) = 1.156 - 235.3/T`.
 */
private fun floryHugginsExcludedVolume(peg: PegWater): Double = monomerExcludedVolume(
    ReciprocalTemperatureChi().chi(ROOM_TEMPERATURE),
    peg.monomerVolume,
    waterMoleculeVolume()
)

private fun corners(peg: PegWater, windows: List<T21WindowRecord>): List<T21CornerRecord> {
    val osmotic = peg.reducedSecondVirialCoefficient(OSMOTIC_SECOND_VIRIAL) * peg.monomerVolume
    val floryHuggins = floryHugginsExcludedVolume(peg)
    return listOf(
        "A2 osmometry (C-0003)" to osmotic,
        "Flory-Huggins chi (C-0007)" to floryHuggins
    ).flatMap { (route, volume) ->
        val base = peg.semidiluteCorrelationFromExcludedVolume(volume)
        listOf("scaling g_T = (b^3/v)^2" to base, "Yamakawa exact z(g_T) = 1" to base.exact)
            .map { (normalisation, correlation) ->
                val crossover = correlation.excludedVolumeCrossover
                val resting = windows.map { it.restingVolumeFraction / crossover }
                // the layer compressed to the section 3 target stroke of 3 nm
                val compressed = windows.map {
                    it.restingVolumeFraction * it.layerHeight /
                            ((it.layerHeight - TARGET_STROKE) * crossover)
                }
                T21CornerRecord(
                    excludedVolumeRoute = route,
                    monomerExcludedVolume = volume,
                    thermalBlobNormalisation = normalisation,
                    thermalBlobSegments = correlation.thermalBlobSegments,
                    excludedVolumeCrossover = crossover,
                    chains = windows.size,
                    chainsWithEmptyWindow = windows.count {
                        correlation.desCloizeauxWindow(it.kuhnSegments).isEmpty
                    },
                    widestWindowRatio = windows.maxOf {
                        correlation.desCloizeauxWindow(it.kuhnSegments).widthRatio
                    },
                    restingOverCrossoverLow = resting.min(),
                    restingOverCrossoverHigh = resting.max(),
                    compressedOverCrossoverLow = compressed.min(),
                    compressedOverCrossoverHigh = compressed.max()
                )
            }
    }
}

// ---------------------------------------------------------------------------------------------
// gate 5
// ---------------------------------------------------------------------------------------------

private fun reproductions(
    ceilings: List<T21CeilingRecord>,
    upstream: List<UpstreamCeiling>,
    correlation: SemidiluteCorrelation,
    peg: PegWater
): List<T21ReproductionRecord> {
    val records = mutableListOf<T21ReproductionRecord>()
    // (a) the whole rebuild, graded against C-0018's own phi = 0.2 ceiling
    val citedFloor = ceilings.filter { it.crossoverName == "the cited band, floor" }
    var worst = 0.0
    var worstHere = 0.0
    var worstThere = 0.0
    citedFloor.forEach { cell ->
        val match = upstream.first {
            it.model == cell.model && it.layerHeight == cell.layerHeight &&
                    it.graftingDensity == cell.graftingDensity &&
                    it.concentration == cell.concentration && it.loadLine == cell.loadLine
        }
        val there = match.concentratedCrossoverBias ?: return@forEach
        val here = cell.crossoverBias ?: return@forEach
        val departure = abs(here - there) / abs(there)
        if (departure > worst) {
            worst = departure
            worstHere = here
            worstThere = there
        }
    }
    records += T21ReproductionRecord(
        quantity = "C-0018's phi = 0.2 ceiling bias, worst of ${citedFloor.size} states",
        here = worstHere,
        upstream = worstThere,
        relativeDeparture = worst,
        source = "C-0018 / gpd/results/T-4-maximum-usable-bias.json, field concentratedCrossoverBias"
    )
    // (b) the thermal blob, against CH-0020's corrected count computed independently
    records += T21ReproductionRecord(
        quantity = "thermal blob in Kuhn segments (scaling normalisation)",
        here = correlation.thermalBlobSegments,
        upstream = 126.3,
        relativeDeparture = abs(correlation.thermalBlobSegments - 126.3) / 126.3,
        source = "CH-0020 / brush.thermalBlobKuhnSegmentsCorrected"
    )
    // (c) the aspect ratio, against C-0002's parameter sheet
    records += T21ReproductionRecord(
        quantity = "Kuhn aspect ratio b^3/v_K",
        here = correlation.kuhnAspectRatio,
        upstream = 7.09,
        relativeDeparture = abs(correlation.kuhnAspectRatio - 7.09) / 7.09,
        source = "C-0002 parameter sheet"
    )
    // (d) the textbook athermal limit
    val athermal = SemidiluteCorrelation(1.0, 1.0, 1.0, 1.0)
    records += T21ReproductionRecord(
        quantity = "athermal space-filling-segment limit of phi** (textbook value 1)",
        here = athermal.excludedVolumeCrossover,
        upstream = 1.0,
        relativeDeparture = abs(athermal.excludedVolumeCrossover - 1.0),
        source = "de Gennes / Rubinstein & Colby: the semidilute regime of an athermal solvent " +
                "extends to phi ~ 1 — recovered exactly when v = v_K = b^3"
    )
    // (e) Rubinstein & Colby's PRINTED phi** = v/b^3, which is in the REDUCED convention.
    // This is the whole convention question made falsifiable: our physical crossover multiplied
    // by the Kuhn aspect ratio must reproduce the textbook expression exactly.
    val printed = correlation.kuhnPairExcludedVolume / correlation.kuhnLength.pow(3.0)
    records += T21ReproductionRecord(
        quantity = "phi** in the REDUCED convention, against R&C eq (5.36) phi** = v/b^3",
        here = correlation.reducedFromPhysical(correlation.excludedVolumeCrossover),
        upstream = printed,
        relativeDeparture = abs(
            correlation.reducedFromPhysical(correlation.excludedVolumeCrossover) - printed
        ) / printed,
        source = "Rubinstein & Colby, Polymer Physics (OUP 2003) p. 180 eq (5.36), READ DIRECTLY " +
                "from a page render; their phi is the reduced density n b^3 (their eqs 5.19, " +
                "5.21, 5.1), so the conversion by b^3/v_K = 7.09 is the whole of the difference"
    )
    // (f) Hansen et al. (2003)'s MEASURED des Cloizeaux onset, converted out of its own
    // reduced convention (phi = 0.59 w, i.e. n a^3 on a = 3.5 A) into the physical one.
    records += T21ReproductionRecord(
        quantity = "measured des Cloizeaux onset for PEG-5000, converted to physical phi",
        here = 0.08 * (peg.monomerVolume / peg.effectiveMonomerLength.pow(3.0)),
        upstream = 0.08,
        relativeDeparture = peg.monomerVolume / peg.effectiveMonomerLength.pow(3.0) - 1.0,
        source = "Hansen, Cohen, Podgornik & Parsegian, Biophys. J. 84:350 (2003) p. 352, READ " +
                "DIRECTLY: 'PEG-5000 solutions are not in the scaling regime until the monomer " +
                "volume fraction is larger than phi#5000 = 0.07-0.09'. Their phi = 0.59 w is a " +
                "REDUCED density on a = 3.5 A, so the physical onset is 1.408x larger, 0.10-0.13 " +
                "— ABOVE the Gen-1 layer's own 0.029-0.071 at rest"
    )
    // (g) the window identity, at the longest Gen-1 chain
    val kuhn = 225.0 / peg.monomersPerKuhnSegment
    val window = correlation.desCloizeauxWindow(kuhn)
    records += T21ReproductionRecord(
        quantity = "des Cloizeaux window width at the longest Gen-1 chain, against sqrt(N_K/g_T)",
        here = window.widthRatio,
        upstream = sqrt(kuhn / correlation.thermalBlobSegments),
        relativeDeparture = abs(
            window.widthRatio - sqrt(kuhn / correlation.thermalBlobSegments)
        ) / sqrt(kuhn / correlation.thermalBlobSegments),
        source = "the identity proved in ConcentratedCrossoverTest, evaluated here"
    )
    return records
}

// ---------------------------------------------------------------------------------------------
// findings and prose
// ---------------------------------------------------------------------------------------------

@Suppress("LongMethod")
private fun findings(result: T21Result): Map<String, String> {
    val exact = result.family.first { it.name.contains("Yamakawa") }
    val scaling = result.family.first { it.name.contains("scaling g_T") }
    val oneSegment = result.family.first { it.name == "one Kuhn segment per blob" }
    val fit = result.family.first { it.name.startsWith("equation-of-state support ceiling (all") }
    val mis = result.family.first { it.name.contains("mis-coarse-grained") }
    val emptyWindows = result.windows.count { it.windowIsEmpty }
    val restingRatio = result.windows.map { it.restingOverExcludedVolumeCrossover }
    val citedCensus = result.census.first { it.crossoverName == "the cited band, floor" }
    val oneSegmentCensus = result.census.first { it.crossoverName == "one Kuhn segment per blob" }
    val fitCensus =
        result.census.first { it.crossoverName.startsWith("equation-of-state support ceiling (all") }
    val exactCensus = result.census.first { it.crossoverName.contains("Yamakawa") }
    return mapOf(
        "P1 the derived crossover" to (
                "phi_c(n) = (v_K/b^3) n^(-1/2), one formula and the whole family. " +
                        "The EXCLUDED-VOLUME crossover — where the des Cloizeaux exponent stops " +
                        "being the one the material is entitled to — is " +
                        "%.5f (Yamakawa exact) to %.5f (scaling). ".format(
                            exact.volumeFraction, scaling.volumeFraction
                        ) +
                        "The ONE-KUHN-SEGMENT-PER-BLOB crossover, which is what the cited " +
                        "0.2-0.3 band was trying to be, is %.4f — BELOW the cited floor."
                            .format(oneSegment.volumeFraction)
                ),
        "P1 the cited band has no member of the family in it" to (
                "The family runs %.5f to %.4f. The only construction that lands inside 0.2-0.3 " +
                        "is the excluded-volume criterion mis-coarse-grained onto MONOMERS, " +
                        "which gives %.4f — the floor of the cited band to three digits, and " +
                        "wrong by a factor of %.1f."
                ).format(
                exact.volumeFraction, fit.volumeFraction, mis.volumeFraction,
                mis.volumeFraction / scaling.volumeFraction
            ),
        "P2 the premise fails, and that IS the answer" to (
                "%d of %d Gen-1 chains have an EMPTY des Cloizeaux window: the chain is shorter " +
                        "than a thermal blob (N_K = %.1f-%.1f against g_T = %.0f-%.0f), so " +
                        "phi* > phi** and the exponent never starts. The layer sits at " +
                        "%.1f-%.1f times phi** at its own resting height."
                ).format(
                emptyWindows, result.windows.size,
                result.windows.minOf { it.kuhnSegments }, result.windows.maxOf { it.kuhnSegments },
                result.windows.minOf { it.thermalBlobSegmentsScaling },
                result.windows.maxOf { it.thermalBlobSegmentsExact },
                restingRatio.minOrNull() ?: 0.0, restingRatio.maxOrNull() ?: 0.0
            ),
        "P2 the emptiness is robust in three corners of four, and survives the fourth" to (
                "Across (thermal-blob normalisation) x (excluded-volume route) the window is " +
                        "empty at every chain in %d of 4 corners. In the remaining corner " +
                        "(Flory-Huggins v, scaling g_T) g_T falls to %.0f and the window EXISTS, " +
                        "up to %.2fx wide in phi — but the layer's own volume fraction is " +
                        "%.1f-%.1f times its upper edge at rest and %.1f-%.1f times it at the " +
                        "3 nm target stroke, so the window is never entered."
                ).format(
                result.corners.count { it.chainsWithEmptyWindow == it.chains },
                result.corners.first { it.chainsWithEmptyWindow < it.chains }.thermalBlobSegments,
                result.corners.first { it.chainsWithEmptyWindow < it.chains }.widestWindowRatio,
                result.corners.first { it.chainsWithEmptyWindow < it.chains }
                    .restingOverCrossoverLow,
                result.corners.first { it.chainsWithEmptyWindow < it.chains }
                    .restingOverCrossoverHigh,
                result.corners.first { it.chainsWithEmptyWindow < it.chains }
                    .compressedOverCrossoverLow,
                result.corners.first { it.chainsWithEmptyWindow < it.chains }
                    .compressedOverCrossoverHigh
            ),
        "P3 the grafted layer inherits the UPPER crossover and not the lower one" to (
                "phi_c(n) contains no chain length at all — it is a local structural statement, " +
                        "exactly as C-0019's Ginzburg parameter is — so grafting, which removes " +
                        "chain translational entropy and with it the LOWER crossover " +
                        "(CH-0001/CH-0002), leaves the upper one untouched. The one thing that " +
                        "could break the inheritance is stretching inside the blob, and the " +
                        "Gen-1 chains are extended to only %.3f-%.3f of their contour."
                ).format(
                result.windows.minOf { it.chainExtensionRatio },
                result.windows.maxOf { it.chainExtensionRatio }
            ),
        "P4 the direction, stated plainly" to (
                "The DERIVED scaling crossover is LOWER than the cited 0.2, not higher, and the " +
                        "device is WORSE for it. Read as a regime boundary at %.4f it binds at " +
                        "%d of %d states against %d at 0.2; the coupled margin falls from " +
                        "%.3f-%.3f to %.3f-%.3f and the number of coupled states with a margin " +
                        "below one rises from %d to %d. Read as the EMPTY-WINDOW statement it is " +
                        "not a ceiling at all: the excluded-volume crossover is already violated " +
                        "at the RESTING height at %d of %d states, so no bias whatever would be " +
                        "usable — which is the reductio that shows the regime reading is not the " +
                        "role this number can play."
                ).format(
                oneSegment.volumeFraction, oneSegmentCensus.boundByCrossover,
                oneSegmentCensus.states, citedCensus.boundByCrossover,
                citedCensus.coupledMarginLow ?: 0.0, citedCensus.coupledMarginHigh ?: 0.0,
                oneSegmentCensus.coupledMarginLow ?: 0.0,
                oneSegmentCensus.coupledMarginHigh ?: 0.0,
                citedCensus.coupledStatesWithMarginBelowOne,
                oneSegmentCensus.coupledStatesWithMarginBelowOne,
                exactCensus.statesViolatedAtRestingHeight, exactCensus.states
            ),
        "P4 but the number C-0018 consumes is not that crossover" to (
                "C-0018 reads the number as a ceiling on where the layer's CONSTITUTIVE LAW " +
                        "stops being supported, not on which scaling regime the solution is in. " +
                        "On that axis the replacement is the equation of state's own fitted " +
                        "range, phi = %.3f, and there the crossover binds at only %d of %d " +
                        "states — C-0005's 1.46 nm correlation band takes over at %d. The " +
                        "coupled margin becomes %.3f-%.3f with %d states below one, against " +
                        "%.3f-%.3f and %d at the cited 0.2. The thinnest margin in the " +
                        "programme, 10 nm in 2 mM, is set by PULL-IN and does not move at all."
                ).format(
                fit.volumeFraction, fitCensus.boundByCrossover, fitCensus.states,
                fitCensus.boundByCorrelationBand,
                fitCensus.coupledMarginLow ?: 0.0, fitCensus.coupledMarginHigh ?: 0.0,
                fitCensus.coupledStatesWithMarginBelowOne,
                citedCensus.coupledMarginLow ?: 0.0, citedCensus.coupledMarginHigh ?: 0.0,
                citedCensus.coupledStatesWithMarginBelowOne
            ),
        "gate 5" to (
                "the rebuild reproduces C-0018's own phi = 0.2 ceiling to a worst relative " +
                        "departure of %.2e over %d states"
                ).format(
                result.reproductions.first().relativeDeparture,
                result.ceilings.count { it.crossoverName == "the cited band, floor" }
            )
    )
}

private val CONVENTIONS = listOf(
    "a volume fraction is the PHYSICAL one, phi = c v0 = N sigma v0 / h; the Kuhn-reduced " +
            "density c_K b^3 that the textbook crossover statements use is b^3/v_K = 7.09 LARGER",
    "excluded volume is a PAIR quantity and coarse-grains as n_K^2, never n_K (CH-0020)",
    "a crossover is a CONVENTION until its definition is named: every value here is emitted " +
            "with the number of Kuhn segments per correlation blob that selects it",
    "the thermal blob carries a published 9.19 normalisation bracket (scaling against " +
            "Yamakawa's exact z(g_T) = 1); both ends are carried and neither is called THE value",
    "L0 is a FORCE-ONSET height (C-0011, CH-0010) and the stroke s = L0 - h is positive downward",
    "a bias ceiling belongs to a (bias, load line) pair, never to the bias alone (CH-0015)"
)

private val CITED = listOf(
    "A2 = 1.9e-3 mol cm^3/g^2 — CITED FROM C-0003/C-0002. It sets the excluded volume and " +
            "therefore g_T; the ONE-SEGMENT-PER-BLOB crossover does not depend on it at all",
    "b = 1.1 nm and M_K = 137 g/mol — CITED FROM C-0002 (Rubinstein & Colby Tab. 2.1). The " +
            "aspect ratio b^3/v_K = 7.09 that fixes the whole family is derived from these two",
    "V-bar = 0.825 mL/g — CITED FROM C-0002; it fixes v0 and the weight-to-volume conversion",
    "the 50 wt % fit range of the adopted equation of state — CITED FROM C-0002's reading of " +
            "Cohen et al. (2009). It is the sole input to the support-ceiling row and it is the " +
            "row C-0018 needs, so this task is more sensitive to it than to anything else",
    "C-0018's pull-in bias and correlation-band bias at every state — CITED FROM " +
            "gpd/results/T-4-maximum-usable-bias.json, because neither can depend on the " +
            "crossover. The rebuild is graded by reproducing C-0018's own phi = 0.2 ceiling",
    "C-0005's 1.46 nm correlation band and CH-0007's 1.0 V point-ion boundary — CITED, carried " +
            "unchanged so that the re-ranking is like-for-like"
)

private val VALIDITY = listOf(
    "TRL 1-3. NOTHING HERE IS MEASURED.",
    "The crossover family is a SCALING construction: every member carries an unknown O(1) " +
            "prefactor, and the family's own spread is the honest statement of that ignorance.",
    "The blob construction assumes a solution of FREE chains. The layer's inheritance of it is " +
            "argued from the absence of a chain length in phi_c(n), not demonstrated.",
    "The layer's volume fraction is taken as the MEAN, N sigma v0 / h. C-0011's solved profile " +
            "is not uniform, so the local phi at the grafting surface exceeds the mean and every " +
            "crossover here is crossed there FIRST. This makes the derived ceilings optimistic.",
    "The re-ranking inherits C-0018's whole mean-field statement: C-0005 puts the one-loop " +
            "correction at 123-214 % of the leading electrostatic term, larger than every margin.",
    "The equation-of-state support ceiling is a statement about the INTERACTION term only. It " +
            "says nothing about whether Gaussian chain elasticity or the Alexander/strong-" +
            "stretching profile survive to phi = 0.45.",
    "The layer models were chosen by C-0003 and three of the six are des Cloizeaux constructions " +
            "whose warrant this task removes. They are left in place as a BRACKET, not endorsed."
)

private val OPEN = listOf(
    "The 50 wt % fit range is CITED, not read here from the source. If the true range is lower, " +
            "the support ceiling falls with it and the propagation must be re-run.",
    "No prefactor for the crossover family is available from measurement. The family is quoted " +
            "as a spread; a compression isotherm on a Gen-1-density PEG layer would collapse it.",
    "Three of C-0003's six layer models lose their theoretical warrant here and none of them is " +
            "withdrawn. Whether the two-body/virial half of the bracket should now be the whole " +
            "of it is a separate question, and it belongs to C-0003, not to this task.",
    "The non-uniform profile of C-0011 is not used. The crossing volume fraction of a SOLVED " +
            "layer is a different and lower number, and it is not computed here."
)

@Suppress("LongMethod")
private fun report(result: T21Result, output: File) {
    println()
    println("=".repeat(96))
    println("T-21 — the semidilute-to-concentrated crossover, derived")
    println("=".repeat(96))
    println()
    println("THE FAMILY — phi_c(n) = (v_K/b^3) n^(-1/2), physical volume fraction")
    println("%-46s %10s %10s".format("criterion", "n", "phi"))
    result.family.forEach {
        println(
            "%-46s %10s %10.5f".format(
                it.name, it.segmentsPerBlob?.let { n -> "%.1f".format(n) } ?: "-",
                it.volumeFraction
            )
        )
    }
    println()
    println("THE WINDOW — (phi*, phi**) at every Gen-1 chain")
    println(
        "%-32s %6s %8s %9s %9s %8s %8s".format(
            "model", "L0", "N_K", "phi*", "phi**", "width", "empty"
        )
    )
    result.windows.forEach {
        println(
            "%-32s %6.1f %8.1f %9.5f %9.5f %8.3f %8s".format(
                it.model, it.layerHeight, it.kuhnSegments, it.overlapVolumeFraction,
                it.excludedVolumeCrossover, it.windowWidthRatio, it.windowIsEmpty
            )
        )
    }
    println()
    println("THE FOUR CORNERS — is the des Cloizeaux window empty?")
    println(
        "%-28s %-26s %8s %9s %7s %10s %12s".format(
            "excluded volume", "thermal blob", "g_T", "phi**", "empty", "widest", "layer/phi**"
        )
    )
    result.corners.forEach {
        println(
            "%-28s %-26s %8.1f %9.5f %3d/%-3d %10.3f %5.1f-%5.1f".format(
                it.excludedVolumeRoute, it.thermalBlobNormalisation, it.thermalBlobSegments,
                it.excludedVolumeCrossover, it.chainsWithEmptyWindow, it.chains,
                it.widestWindowRatio, it.restingOverCrossoverLow, it.compressedOverCrossoverHigh
            )
        )
    }
    println()
    println("THE PROPAGATION — C-0018's 162 ceilings re-read")
    println(
        "%-46s %8s %7s %7s %7s %7s %9s".format(
            "crossover", "phi", "bound", "pull-in", "corr", "none", "usable V"
        )
    )
    result.census.forEach {
        println(
            "%-46s %8.4f %7d %7d %7d %7d %9s".format(
                it.crossoverName, it.crossoverVolumeFraction, it.boundByCrossover,
                it.boundByPullIn, it.boundByCorrelationBand, it.statesViolatedAtRestingHeight,
                if (it.usableBiasLow == null) "-"
                else "%.3f-%.3f".format(it.usableBiasLow, it.usableBiasHigh)
            )
        )
    }
    println()
    println("GATE 5")
    result.reproductions.forEach {
        println(
            "  %-64s %12.6g vs %12.6g  (%.2e)".format(
                it.quantity, it.here, it.upstream, it.relativeDeparture
            )
        )
    }
    println()
    result.findings.forEach { (key, value) -> println("[$key] $value"); println() }
    println("written to ${output.path}")
}
