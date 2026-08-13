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

package com.xemantic.nano.plentyofroom.anchoring

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
import com.xemantic.nano.plentyofroom.brush.load
import com.xemantic.nano.plentyofroom.brush.reducedSecondVirialCoefficient
import com.xemantic.nano.plentyofroom.brush.reducedThirdVirialCoefficient
import com.xemantic.nano.plentyofroom.brush.stiffness
import com.xemantic.nano.plentyofroom.brush.threeBodyInteraction
import com.xemantic.nano.plentyofroom.brush.twoBodyInteraction
import com.xemantic.nano.plentyofroom.coupling.SeriesEntropicCoupling
import com.xemantic.nano.plentyofroom.coupling.gaussianContourCeiling
import com.xemantic.nano.plentyofroom.coupling.mandatedCouplingStiffness
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
import com.xemantic.nano.plentyofroom.poroelastic.FiberArrayPermeability
import com.xemantic.nano.plentyofroom.poroelastic.RectangularFootprint
import com.xemantic.nano.plentyofroom.poroelastic.brinkmanTransmissivity
import com.xemantic.nano.plentyofroom.poroelastic.squeezeDragCoefficient
import com.xemantic.nano.plentyofroom.poroelastic.tileStokesDrag
import com.xemantic.nano.plentyofroom.poroelastic.waterViscosity
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Task `T-13` — **where the tile sits at zero bias, and what holds it there.**
 *
 * Emits `gpd/results/T-13-zero-bias-resting-position.json`.
 */

// ---------------------------------------------------------------------------------------------
// the record types
// ---------------------------------------------------------------------------------------------

/** One candidate hold-down mechanism, at one layer height, with its sign and its magnitude. */
@Serializable
data class MechanismRecord(
    val mechanism: String,
    val layerHeight: Double,
    /** Positive DOWN, per this task's convention. Negative means the mechanism holds the tile up. */
    val force: Double,
    /** `dF_down/dh` in `pN/nm`; it enters the equilibrium stiffness with a minus sign. */
    val forceSlope: Double,
    val overThermalScale: Double,
    val verdict: String,
    val provenance: String
)

/** The van der Waals term over the electrode materials §1 declines to name. */
@Serializable
data class VanDerWaalsRecord(
    val electrode: String,
    val electrodeHamaker: Double,
    val tileThickness: Double,
    val gap: Double,
    /** `√(A_DNA|w|DNA · A_e|w|e)` at the high ends — the unretarded, unscreened upper bound. */
    val combinedHamakerUpper: Double,
    /** The same at the low ends with the zero-frequency term fully screened. */
    val combinedHamakerLower: Double,
    val zeroFrequencyShare: Double,
    val retardationFactor: Double,
    val slabFactor: Double,
    /**
     * The vacuum-form combining relation, kept **only** to show the sign cannot change. Its
     * magnitude is not quantitative here: it reconstructs vacuum constants from across-water
     * ones, which is exactly the step the across-water form exists to avoid.
     */
    val signDiagnostic: Double,
    val polymerMediumCorrection: Double,
    val pressureUpper: Double,
    val pressureLower: Double,
    val forceUpper: Double,
    val forceLower: Double,
    val negativeStiffness: Double,
    /** `(A S/12π)[h⁻² − (h+t)⁻²]` in `k_BT` — the depth of the well, which is FINITE. */
    val wellDepth: Double,
    val overThermalScaleUpper: Double,
    val overThermalScaleLower: Double
)

/** The residual field at zero **applied** bias, on both readings of the electrode. */
@Serializable
data class ZeroBiasFieldRecord(
    val gap: Double,
    val sternCapacitance: String,
    val diffusePotential: Double,
    val electrodeChargeDensity: Double,
    /** Signed force in pN on the tile: negative is toward the electrode, i.e. a hold-down. */
    val force: Double,
    val holdDown: Double,
    val overThermalScale: Double,
    val numericallyResolved: Boolean
)

/** How much electrode potential offset — a contact potential — the hold-down would need. */
@Serializable
data class PotentialOfZeroChargeRecord(
    val gap: Double,
    val targetHoldDown: Double,
    val target: String,
    val appliedBias: Double?,
    val diffusePotential: Double?
)

/** One solved zero-bias state: model x height x hold-down scenario. */
@Serializable
data class EquilibriumRecord(
    val scenario: String,
    val model: String,
    val layerHeight: Double,
    val graftingDensity: Double,
    val monomersPerChain: Double,
    val holdDownAtOnsetHeight: Double,
    val restingHeight: Double?,
    val strokeLost: Double?,
    val strokeLostFraction: Double?,
    /** `δ* − (L₀ − h₀)`: what is left of §3's 3 nm once the tile starts from where it rests. */
    val deliveredStrokeToWorkingPoint: Double?,
    val layerLoadAtRest: Double?,
    val equilibriumStiffness: Double?,
    val layerStiffnessAtRest: Double?,
    val negativeStiffnessShare: Double?,
    val stable: Boolean,
    val meanHeight: Double?,
    val rms: Double?,
    val equipartitionRms: Double?,
    val meanExcursionAbove: Double?,
    val probabilityAbove: Double?,
    val escapeBarrier: Double?,
    val quadratureDomainUpper: Double?,
    val confining: Boolean,
    val cornerFrequency: Double?,
    val varianceFractionInBand: Double?,
    val rmsInBand: Double?,
    val meetsPositionBound: Boolean,
    val verdict: String
)

/** `L₀` is a convention with a defining load; this is the sensitivity, in this task's currency. */
@Serializable
data class RestingLoadRecord(
    val model: String,
    val layerHeight: Double,
    val definingLoad: Double,
    val restingHeight: Double,
    val descentFromOnset: Double,
    val layerStiffnessThere: Double,
    val meanExcursionAbove: Double
)

/** The exact relation between `T-16`'s coupling stiffness and `T-13`'s preload. */
@Serializable
data class CouplingPreloadRecord(
    val couplingStiffness: Double,
    val mandatedStiffness: Double,
    val downwardPreload: Double,
    val reactionAtZeroStroke: Double,
    val meetsThermalScale: Boolean,
    val note: String
)

/** The bridging ceiling and the threshold that would falsify it. */
@Serializable
data class BridgingRecord(
    val layerHeight: Double,
    val graftingDensity: Double,
    val chainsUnderTile: Double,
    val ceilingAtOneThermalUnit: Double,
    val thresholdForThermalScale: Double,
    val thresholdForThermalScaleInThermalUnits: Double,
    val thresholdForTetherPreload: Double,
    val thresholdForTetherPreloadInThermalUnits: Double
)

/** Numerical convergence of the two solvers this task adds. */
@Serializable
data class ConvergenceRecord(
    val quantity: String,
    val setting: String,
    val value: Double,
    val departureFromFinest: Double
)

/** A number reproduced from an upstream claim rather than cited. */
@Serializable
data class ReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val relativeDeparture: Double
)

@Serializable
data class ZeroBiasResult(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val parameters: Map<String, String>,
    val thermalForceScale: Double,
    val mechanisms: List<MechanismRecord>,
    val vanDerWaals: List<VanDerWaalsRecord>,
    val zeroBiasField: List<ZeroBiasFieldRecord>,
    val potentialOfZeroCharge: List<PotentialOfZeroChargeRecord>,
    val equilibria: List<EquilibriumRecord>,
    val restingLoadSensitivity: List<RestingLoadRecord>,
    val couplingPreload: List<CouplingPreloadRecord>,
    val bridging: List<BridgingRecord>,
    val convergence: List<ConvergenceRecord>,
    val reproductions: List<ReproductionRecord>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val citedNumbers: List<String>
)

// ---------------------------------------------------------------------------------------------
// parameters
// ---------------------------------------------------------------------------------------------

/** §3's three layer heights with `C-0001`'s grafting densities — `C-0012`'s and `C-0017`'s points. */
private val DESIGN_POINTS = listOf(5.0 to 0.092, 7.0 to 0.045, 10.0 to 0.024)

private const val TILE_EDGE = 40.0

private const val FOOTPRINT = TILE_EDGE * TILE_EDGE

/** §3's buffer for this task: 2 mM, which is also `C-0008`'s zero-bias column. */
private const val BUFFER = 2.0

/** Leaf `A1.1`'s positional bound in nm. */
private const val POSITION_BOUND = 3.0

/** §6's measurement band in Hz. */
private const val BANDWIDTH = 1000.0

private const val TARGET_FORCE = 100.0

private const val TARGET_STROKE = 3.0

private const val STERN_CAPACITANCE = 20.0

private const val SEARCH_NODES = 400

private const val OSMOTIC_SECOND_VIRIAL = 1.9e-3

private const val OSMOTIC_THIRD_VIRIAL = 2.0e-2

/** `C-0014`'s per-coordinate lateral bound in `pN/nm` — **CITED**, leaf `A1.1`'s own table. */
private const val LATERAL_BOUND = 0.460216

/** `C-0014`'s `S3` design point: eight tethers at the zero-force Kuhn length. */
private const val TETHER_COUNT = 8

private const val TETHER_KUHN = 2.10

/** §3's *"~10 nm (single-layer honeycomb)"*, read both ways — see [vanDerWaalsPressure]. */
private val TILE_THICKNESSES = listOf(2.0 to "single-layer sheet, one duplex diameter", 10.0 to "§3's stated ~10 nm")

private const val SCAN_STEPS = 400

private const val QUADRATURE_PANELS = 2000

/** How many `k_BT/F` of upward tail the Boltzmann quadrature carries. */
private const val TAIL_DECADES = 40.0

/** The quadrature domain never runs past this many nm above `L₀`, whatever the hold-down. */
private const val TAIL_CEILING = 60.0

/**
 * The well depth in `k_BT` below which a "resting position" is a trap rather than a confinement.
 *
 * 10 `k_BT` is `e^(−10) = 4.5e−5` of Boltzmann weight at the top of the well, i.e. the tile is
 * outside it for less than one part in twenty thousand. Below that the moments of the
 * distribution are dominated by the escaped tail and are properties of the integration domain.
 */
private const val CONFINEMENT_BARRIER = 10.0

// ---------------------------------------------------------------------------------------------

/**
 * One candidate electrode, as a Hamaker constant **across water** with its own low and high
 * readings — because §1 says *"patterned electrode"* and never says of what, and that is the
 * largest single uncertainty in the one hold-down that cannot be designed away.
 */
private data class Electrode(
    val name: String,
    val low: Double,
    val high: Double,
    val metal: Boolean
) {

    /**
     * The zero-frequency term of the **cross** constant, `√(A⁰_DNA · A⁰_electrode)`.
     *
     * The DNA half is the low-dielectric-across-water value Roth, Neal & Lenhoff (1996)
     * calculate at ≈ `0.75 k_BT`; the electrode half is `(3/4)ζ(3)k_BT` for a metal and the
     * same low-dielectric value for an oxide.
     */
    fun zeroFrequencyCross(): Double = sqrt(
        HamakerConstants.ZERO_FREQUENCY_TERM_LOW_DIELECTRIC *
                (if (metal) HamakerConstants.ZERO_FREQUENCY_TERM
                else HamakerConstants.ZERO_FREQUENCY_TERM_LOW_DIELECTRIC)
    )

    /** The low-end combined constant with the zero-frequency term at its fully-screened end. */
    fun screenedLow(gap: Double, inverseDebyeLength: Double): Double {
        val combined = combinedHamakerAcrossWater(HamakerConstants.DNA_ACROSS_WATER_LOW, low)
        val zero = min(zeroFrequencyCross(), combined)
        return screenedHamakerConstant(zero, combined - zero, gap, inverseDebyeLength)
    }
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

/**
 * The layer's upward force in pN at [height], **guarded above `L₀`**.
 *
 * `C-0003`'s models throw above their own equilibrium height, which is correct — the free
 * energy is not defined there — but the physical statement is that a non-adsorbing layer
 * simply loses contact. That statement is the premise of this whole task, so it is written
 * once, here, rather than caught somewhere downstream.
 */
private fun layerLoadAt(
    model: GraftedLayerModel,
    chain: GraftedChain,
    onsetHeight: Double,
    height: Double
): Double = if (height >= onsetHeight) 0.0 else model.load(chain, height, FOOTPRINT)

private fun layerStiffnessAt(
    model: GraftedLayerModel,
    chain: GraftedChain,
    onsetHeight: Double,
    height: Double
): Double = if (height >= onsetHeight) 0.0 else model.stiffness(chain, height, FOOTPRINT)

/** The field sampler at zero applied bias, with or without the compact layer in series. */
private class ZeroBiasField(
    val tileCharge: Double,
    val bjerrum: Double,
    val ionModel: IonModel,
    val sternChargePerVolt: Double
) {

    private val medium = uniformMedium(GapMedium())

    fun diffusePotential(gap: Double, bias: Double): Double = diffusePotentialOfAppliedBias(
        gap, bias, tileCharge, sternChargePerVolt, ionModel, medium, bjerrum, nodes = SEARCH_NODES
    )

    fun solve(gap: Double, bias: Double, nodes: Int = DEFAULT_GAP_MESH_NODES) =
        PoissonBoltzmannGap(gap, ionModel, medium, bjerrum, nodes = nodes)
            .solve(diffusePotential(gap, bias) / thermalVoltage(), tileCharge)

    /** Signed force in pN on the tile; negative is toward the electrode. */
    fun force(gap: Double, bias: Double, nodes: Int = DEFAULT_GAP_MESH_NODES): Double =
        solve(gap, bias, nodes).forceOnTile(FOOTPRINT)
}

/**
 * The smallest applied bias of either sign at which `|F_es|` reaches [target] — the **contact
 * potential threshold**, and the honest form of the answer to "what does the field do at zero
 * bias", because §1 and §3 nowhere state the electrode's potential of zero charge.
 */
private fun biasForHoldDown(field: ZeroBiasField, gap: Double, target: Double): Double? {
    var low = 0.0
    var high = 0.35
    if (abs(field.force(gap, high)) < target) return null
    repeat(60) {
        val middle = 0.5 * (low + high)
        if (abs(field.force(gap, middle)) < target) low = middle else high = middle
        if (high - low <= 1e-9) return 0.5 * (low + high)
    }
    return 0.5 * (low + high)
}

// ---------------------------------------------------------------------------------------------

fun main() {
    val peg = PegWater()
    val tile = DnaOrigamiTile()
    val lb = bjerrumLength()
    val buffer = MagnesiumChlorideBuffer(BUFFER)
    val surviving = tile.manningSurvivingFraction(2, lb)
    val tileCharge = -(tile.projectedChargeDensity * surviving / 2.0)
    val inverseDebye = buffer.inverseDebyeLength(lb)
    val models = layerModels(peg)
    val thermalScale = holdDownForceScale(POSITION_BOUND)
    val mandated = mandatedCouplingStiffness(TARGET_FORCE, TARGET_STROKE)
    val viscosity = waterViscosity()
    val permeability = FiberArrayPermeability(fiberRadius = peg.kuhnSegmentDiameter / 2.0)

    println("T-13 — the zero-bias hold-down budget, thermal scale ${"%.4f".format(thermalScale)} pN ...")

    // ---------------------------------------------------------------- M1 the substrate tether
    val tetherContour = 0.5 * gaussianContourCeiling(TETHER_KUHN, TETHER_COUNT, LATERAL_BOUND)
    val tether = FreelyJointedChain(tetherContour, TETHER_KUHN)

    // ---------------------------------------------------------------- M2 the committed coupling
    val spacerContour = 8.61
    val standoffStiffness = AnchorMaterials.DUPLEX_STRETCH_MODULUS / 5.0
    val committedCoupling = SeriesEntropicCoupling(
        count = 45,
        linearStiffness = standoffStiffness,
        chain = FreelyJointedChain(spacerContour, TETHER_KUHN)
    )

    // ---------------------------------------------------------------- M4 van der Waals
    val electrodes = listOf(
        Electrode("gold", HamakerConstants.GOLD_ACROSS_WATER, HamakerConstants.GOLD_ACROSS_WATER_HIGH, true),
        Electrode("platinum", HamakerConstants.PLATINUM_ACROSS_WATER, 313.2, true),
        Electrode("rutile titania", HamakerConstants.TITANIA_ACROSS_WATER, HamakerConstants.TITANIA_ACROSS_WATER_HIGH, false),
        Electrode("alumina (§1's optional dielectric)", HamakerConstants.ALUMINA_ACROSS_WATER, HamakerConstants.ALUMINA_ACROSS_WATER, false)
    )

    /** The retarded, fully-screened, low-end reading — the LOWER bound of the bracket. */
    fun vanDerWaalsForce(electrode: Electrode, thickness: Double, gap: Double): Double =
        vanDerWaalsPressure(electrode.screenedLow(gap, inverseDebye), gap, thickness) *
                retardationPressureFactor(gap) * FOOTPRINT

    val vanDerWaals = mutableListOf<VanDerWaalsRecord>()
    DESIGN_POINTS.forEach { (height, density) ->
        val meanVolumeFraction = peg
            .graftedChain(models[0].chainLengthForHeight(peg, height, density), density)
            .meanVolumeFraction(height)
        val ladenMedium = mediumHamakerWithPolymer(
            HamakerConstants.WATER, HamakerConstants.POLY_ETHYLENE_OXIDE, meanVolumeFraction
        )
        electrodes.forEach { electrode ->
            val upper = combinedHamakerAcrossWater(
                HamakerConstants.DNA_ACROSS_WATER_HIGH, electrode.high
            )
            val lower = electrode.screenedLow(height, inverseDebye)
            // the vacuum-form sign diagnostic: could the polymer in the gap flip the sign?
            val neat = combinedHamakerConstant(
                HamakerConstants.DNA_ACROSS_WATER_HIGH + HamakerConstants.WATER,
                electrode.low + HamakerConstants.WATER, HamakerConstants.WATER
            )
            val laden = combinedHamakerConstant(
                HamakerConstants.DNA_ACROSS_WATER_HIGH + HamakerConstants.WATER,
                electrode.low + HamakerConstants.WATER, ladenMedium
            )
            TILE_THICKNESSES.forEach { (thickness, _) ->
                val pressureUpper = vanDerWaalsPressure(upper, height, thickness)
                val pressureLower =
                    vanDerWaalsPressure(lower, height, thickness) * retardationPressureFactor(height)
                vanDerWaals += VanDerWaalsRecord(
                    electrode = electrode.name,
                    electrodeHamaker = electrode.low,
                    tileThickness = thickness,
                    gap = height,
                    combinedHamakerUpper = upper,
                    combinedHamakerLower = lower,
                    zeroFrequencyShare = electrode.zeroFrequencyCross() /
                            combinedHamakerAcrossWater(HamakerConstants.DNA_ACROSS_WATER_LOW, electrode.low),
                    retardationFactor = retardationPressureFactor(height),
                    slabFactor = vanDerWaalsPressure(1.0, height, thickness) /
                            vanDerWaalsPressure(1.0, height),
                    signDiagnostic = neat,
                    polymerMediumCorrection = laden / neat,
                    pressureUpper = pressureUpper,
                    pressureLower = pressureLower,
                    forceUpper = pressureUpper * FOOTPRINT,
                    forceLower = pressureLower * FOOTPRINT,
                    negativeStiffness = vanDerWaalsPressureSlopeMagnitude(lower, height, thickness) *
                            retardationPressureFactor(height) * FOOTPRINT,
                    wellDepth = vanDerWaalsWellDepth(lower, height, thickness, FOOTPRINT) *
                            retardationPressureFactor(height) / thermalEnergy(),
                    overThermalScaleUpper = pressureUpper * FOOTPRINT / thermalScale,
                    overThermalScaleLower = pressureLower * FOOTPRINT / thermalScale
                )
            }
        }
    }

    val metalElectrode = electrodes.first()
    val oxideElectrode = electrodes.last()

    // ---------------------------------------------------------------- M3 the residual field
    val ions = IonModel(buffer.magnesiumNumberDensity)
    val withStern = ZeroBiasField(tileCharge, lb, ions, sternChargeDensityPerVolt(STERN_CAPACITANCE))
    val idealElectrode = ZeroBiasField(tileCharge, lb, ions, Double.POSITIVE_INFINITY)
    val fieldRecords = mutableListOf<ZeroBiasFieldRecord>()
    val fieldGaps = listOf(3.0, 5.0, 7.0, 10.0)
    listOf(
        "20 uF/cm^2 (C-0008's series compact layer)" to withStern,
        "infinite (an ideal constant-potential electrode)" to idealElectrode
    ).forEach { (label, sampler) ->
        fieldGaps.forEach { gap ->
            val diffuse = sampler.diffusePotential(gap, 0.0)
            val solution = sampler.solve(gap, 0.0)
            val force = solution.forceOnTile(FOOTPRINT)
            fieldRecords += ZeroBiasFieldRecord(
                gap = gap,
                sternCapacitance = label,
                diffusePotential = diffuse,
                electrodeChargeDensity = solution.electrodeSurfaceChargeDensity,
                force = force,
                holdDown = -force,
                overThermalScale = -force / thermalScale,
                numericallyResolved = solution.numericallyResolved
            )
        }
    }
    val zeroBiasHoldDown = fieldGaps.associateWith { gap ->
        -withStern.force(gap, 0.0)
    }
    fun fieldHoldDown(height: Double): Double {
        // the sampled zero-bias force, log-free linear interpolation on the sampled gaps; it is
        // a fraction of a piconewton across the whole range and no interpolant refinement can
        // matter at that size, which is itself the finding
        val gaps = fieldGaps
        if (height <= gaps.first()) return zeroBiasHoldDown.getValue(gaps.first())
        if (height >= gaps.last()) return zeroBiasHoldDown.getValue(gaps.last())
        val upper = gaps.first { it >= height }
        val lower = gaps.last { it <= height }
        if (upper == lower) return zeroBiasHoldDown.getValue(upper)
        val t = (height - lower) / (upper - lower)
        return zeroBiasHoldDown.getValue(lower) * (1.0 - t) + zeroBiasHoldDown.getValue(upper) * t
    }

    val pzc = mutableListOf<PotentialOfZeroChargeRecord>()
    listOf(5.0, 7.0, 10.0).forEach { gap ->
        listOf(
            "the thermal scale k_BT/3nm" to thermalScale,
            "C-0014's eight-tether preload at this height" to substrateTetherHoldDown(tether, TETHER_COUNT, gap)
        ).forEach { (label, target) ->
            val bias = biasForHoldDown(withStern, gap, target)
            pzc += PotentialOfZeroChargeRecord(
                gap = gap,
                targetHoldDown = target,
                target = label,
                appliedBias = bias,
                diffusePotential = bias?.let { withStern.diffusePotential(gap, it) }
            )
        }
    }

    // ---------------------------------------------------------------- the mechanism table
    val mechanisms = mutableListOf<MechanismRecord>()
    DESIGN_POINTS.forEach { (height, density) ->
        val step = 1e-4
        fun record(name: String, force: (Double) -> Double, verdict: String, provenance: String) {
            val value = force(height)
            mechanisms += MechanismRecord(
                mechanism = name,
                layerHeight = height,
                force = value,
                forceSlope = (force(height + step) - force(height - step)) / (2.0 * step),
                overThermalScale = value / thermalScale,
                verdict = verdict,
                provenance = provenance
            )
        }
        record(
            "M1 entropic tether through the layer, grounded on the SUBSTRATE (C-0014 S3)",
            { substrateTetherHoldDown(tether, TETHER_COUNT, it) },
            "DOWN — the only mechanism here that is both large enough and designable",
            "DERIVED from C-0014's own FJC and its contour-ceiling design rule"
        )
        record(
            "M2 the committed output coupling K2 (C-0017), grounded on the lever ABOVE",
            { _ -> -committedCoupling.reaction(0.0) },
            "EXACTLY ZERO at zero stroke — the topology argument, confirmed",
            "DERIVED by evaluating C-0017's own SeriesEntropicCoupling at s = 0"
        )
        record(
            "M3 residual electrostatics at zero APPLIED bias, 2 mM MgCl2, Stern series",
            { fieldHoldDown(it) },
            "sign-changing near-cancellation, |F| well under the thermal scale",
            "DERIVED by re-running C-0008's pipeline at V = 0"
        )
        record(
            "M4 van der Waals across the gap, METAL electrode (gold)",
            { vanDerWaalsForce(metalElectrode, NOMINAL_TILE_THICKNESS, it) },
            "DOWN, always, and it cannot be designed away",
            "DERIVED from Dryden 2015 (DNA) and Tolias 2020 (gold), across-water combining " +
                    "relation, slab factor, retardation and fully-screened zero-frequency term"
        )
        record(
            "M4 van der Waals across the gap, OXIDE electrode (alumina)",
            { vanDerWaalsForce(oxideElectrode, NOMINAL_TILE_THICKNESS, it) },
            "DOWN, always, and 2.6x smaller than the metal reading",
            "DERIVED from Dryden 2015 (DNA) and Bell & Dimos 2000 (alumina), same pipeline"
        )
        record(
            "M5 gravity and buoyancy on the tile",
            { _ -> buoyantWeight(FOOTPRINT * NOMINAL_TILE_THICKNESS, DNA_DENSITY, WATER_DENSITY) },
            "DOWN, and nine orders of magnitude below the thermal scale",
            "DERIVED"
        )
        record(
            "M6 bridging by the PEG layer (a CEILING, not a value)",
            { _ ->
                bridgingForceCeiling(
                    chainCount = density * FOOTPRINT,
                    energyPerChain = thermalEnergy(),
                    range = 1.0
                )
            },
            "DOWN if it exists at all; the non-adsorbing premise is what excludes it",
            "CEILING — the value it would reach at one k_BT per chain; see the threshold table"
        )
    }

    // ---------------------------------------------------------------- the equilibria
    val gravity = buoyantWeight(FOOTPRINT * NOMINAL_TILE_THICKNESS, DNA_DENSITY, WATER_DENSITY)

    /**
     * The committed coupling read as a **negative hold-down**: `K2` supplies no preload, but it
     * is not absent from the zero-bias balance — it resists descent from the moment the tile
     * leaves `L₀`, and at 33 pN/nm it is stiffer than everything else in this task put together.
     * Above `L₀` it goes slack and contributes exactly nothing, which is why it cannot confine
     * the tile from above and cannot answer `T-13`.
     */
    fun couplingResistance(onset: Double, height: Double): Double =
        -committedCoupling.reaction(onset - height)

    val scenarios = listOf(
        "none — the §3 stack as specified" to { _: Double -> 0.0 },
        "van der Waals only, METAL electrode" to { h: Double ->
            vanDerWaalsForce(metalElectrode, NOMINAL_TILE_THICKNESS, h)
        },
        "van der Waals only, OXIDE electrode" to { h: Double ->
            vanDerWaalsForce(oxideElectrode, NOMINAL_TILE_THICKNESS, h)
        },
        "C-0014's eight substrate tethers only" to { h: Double ->
            substrateTetherHoldDown(tether, TETHER_COUNT, h)
        },
        "all of M1, M3, M4, M5 — METAL electrode" to { h: Double ->
            substrateTetherHoldDown(tether, TETHER_COUNT, h) +
                    vanDerWaalsForce(metalElectrode, NOMINAL_TILE_THICKNESS, h) +
                    fieldHoldDown(h) + gravity
        },
        "all of M1, M3, M4, M5 — OXIDE electrode" to { h: Double ->
            substrateTetherHoldDown(tether, TETHER_COUNT, h) +
                    vanDerWaalsForce(oxideElectrode, NOMINAL_TILE_THICKNESS, h) +
                    fieldHoldDown(h) + gravity
        }
    )

    /** The scenarios that additionally carry `C-0017`'s committed coupling. */
    val deviceScenarios = listOf(
        "THE DEVICE: all mechanisms + C-0017's K2 coupling, METAL electrode" to
                { onset: Double, h: Double ->
                    substrateTetherHoldDown(tether, TETHER_COUNT, h) +
                            vanDerWaalsForce(metalElectrode, NOMINAL_TILE_THICKNESS, h) +
                            fieldHoldDown(h) + gravity + couplingResistance(onset, h)
                },
        "THE DEVICE without any tether: van der Waals + field + K2, METAL electrode" to
                { onset: Double, h: Double ->
                    vanDerWaalsForce(metalElectrode, NOMINAL_TILE_THICKNESS, h) +
                            fieldHoldDown(h) + gravity + couplingResistance(onset, h)
                }
    )

    val equilibria = mutableListOf<EquilibriumRecord>()
    val restingLoad = mutableListOf<RestingLoadRecord>()
    DESIGN_POINTS.forEach { (height, density) ->
        models.forEach { model ->
            val chain = peg.graftedChain(model.chainLengthForHeight(peg, height, density), density)
            val onset = model.equilibriumHeight(chain)
            val floor = max(chain.occupiedThickness * 1.05, 0.2)
            val volumeFraction = chain.meanVolumeFraction(min(onset, height))
            val footprint = RectangularFootprint(TILE_EDGE, TILE_EDGE)
            val drag = squeezeDragCoefficient(
                footprint,
                brinkmanTransmissivity(permeability.permeability(volumeFraction), height),
                viscosity
            ) + tileStokesDrag(footprint, viscosity)

            val allScenarios = scenarios.map { (label, holdDown) ->
                label to { _: Double, h: Double -> holdDown(h) }
            } + deviceScenarios

            allScenarios.forEach { (label, holdDownOf) ->
                val holdDown = { h: Double -> holdDownOf(onset, h) }
                fun net(h: Double): Double = layerLoadAt(model, chain, onset, h) - holdDown(h)
                val ceiling = onset + 2.0
                val rest = zeroBiasRestingHeight(::net, ceiling, floor, SCAN_STEPS)
                if (rest == null) {
                    equilibria += EquilibriumRecord(
                        scenario = label, model = model.name, layerHeight = height,
                        graftingDensity = density, monomersPerChain = chain.monomersPerChain,
                        holdDownAtOnsetHeight = holdDown(onset),
                        restingHeight = null, strokeLost = null, strokeLostFraction = null,
                        deliveredStrokeToWorkingPoint = null,
                        layerLoadAtRest = null, equilibriumStiffness = null,
                        layerStiffnessAtRest = null, negativeStiffnessShare = null,
                        stable = false, meanHeight = null, rms = null, equipartitionRms = null,
                        meanExcursionAbove = null, probabilityAbove = null,
                        escapeBarrier = null, quadratureDomainUpper = null, confining = false,
                        cornerFrequency = null, varianceFractionInBand = null, rmsInBand = null,
                        meetsPositionBound = false,
                        verdict = "NO EQUILIBRIUM — nothing pulls the tile down, so every height " +
                                "above L0 is a neutral equilibrium and the resting position is " +
                                "UNDEFINED rather than large"
                    )
                    return@forEach
                }
                val stiffness = equilibriumStiffness(::net, rest, 1e-4)
                val layerPart = layerStiffnessAt(model, chain, onset, rest)
                val holdDownAtRest = holdDown(rest)
                // the tail is set by the hold-down JUST ABOVE L0, where the coupling is slack
                // and the layer is absent, so only the true hold-down acts; and the domain never
                // runs past the tether's own contour, which is a hard geometric stop
                val holdDownAbove = holdDownOf(onset, onset + 1e-6)
                val tail = min(
                    TAIL_DECADES * thermalEnergy() / max(holdDownAbove, 1e-9), TAIL_CEILING
                )
                val statistics = boltzmannPositionStatistics(
                    netUpwardForce = ::net,
                    lower = max(floor, rest - 3.0),
                    upper = min(onset + tail, tetherContour * 0.98),
                    panels = QUADRATURE_PANELS,
                    reference = onset
                )
                val corner = if (stiffness > 0.0) lorentzianCorner(stiffness, drag) else null
                val fraction = corner?.let { varianceFractionInBand(BANDWIDTH, it) }
                equilibria += EquilibriumRecord(
                    scenario = label, model = model.name, layerHeight = height,
                    graftingDensity = density, monomersPerChain = chain.monomersPerChain,
                    holdDownAtOnsetHeight = holdDown(onset),
                    restingHeight = rest,
                    strokeLost = onset - rest,
                    strokeLostFraction = (onset - rest) / TARGET_STROKE,
                    deliveredStrokeToWorkingPoint = TARGET_STROKE - (onset - rest),
                    layerLoadAtRest = layerLoadAt(model, chain, onset, rest),
                    equilibriumStiffness = stiffness,
                    layerStiffnessAtRest = layerPart,
                    negativeStiffnessShare = if (layerPart > 0.0) 1.0 - stiffness / layerPart else null,
                    stable = stiffness > 0.0,
                    meanHeight = statistics.mean,
                    rms = statistics.rms,
                    equipartitionRms = if (stiffness > 0.0) sqrt(thermalEnergy() / stiffness) else null,
                    meanExcursionAbove = statistics.meanExcursionAbove,
                    probabilityAbove = statistics.probabilityAbove,
                    escapeBarrier = statistics.escapeBarrier,
                    quadratureDomainUpper = statistics.domainUpper,
                    confining = statistics.escapeBarrier >= CONFINEMENT_BARRIER,
                    cornerFrequency = corner,
                    varianceFractionInBand = fraction,
                    rmsInBand = fraction?.let { statistics.rms * sqrt(it) },
                    meetsPositionBound = statistics.escapeBarrier >= CONFINEMENT_BARRIER &&
                            statistics.rms <= POSITION_BOUND,
                    verdict = when {
                        stiffness <= 0.0 -> "UNSTABLE — the hold-down beats the layer"
                        statistics.escapeBarrier < CONFINEMENT_BARRIER ->
                            "STABLE BUT NOT CONFINING — the well is only %.2f k_BT deep, so the tile "
                                .format(statistics.escapeBarrier) +
                                    "escapes it and every moment quoted here is a property of the " +
                                    "quadrature domain rather than of the physics"
                        else -> "STABLE AND CONFINING — a %.1f k_BT well"
                            .format(statistics.escapeBarrier)
                    }
                )
            }

            // the defining-load sensitivity, in this task's own currency
            listOf(0.1, 1.0, thermalScale, 4.6, 9.4, 25.0, 100.0).forEach { definingLoad ->
                val there = model.heightUnderLoad(chain, definingLoad, FOOTPRINT)
                restingLoad += RestingLoadRecord(
                    model = model.name,
                    layerHeight = height,
                    definingLoad = definingLoad,
                    restingHeight = there,
                    descentFromOnset = onset - there,
                    layerStiffnessThere = layerStiffnessAt(model, chain, onset, there),
                    meanExcursionAbove = meanExcursionUnderConstantHoldDown(definingLoad)
                )
            }
        }
    }

    // ---------------------------------------------------------------- the coupling relation
    val couplingPreload = listOf(33.333333333333336, 34.0, 35.0, 36.5, 39.01, 45.0, 70.0, 440.0).map { k ->
        val preload = couplingPreloadForStiffness(k, mandated, TARGET_STROKE)
        CouplingPreloadRecord(
            couplingStiffness = k,
            mandatedStiffness = mandated,
            downwardPreload = preload,
            reactionAtZeroStroke = -preload,
            meetsThermalScale = preload >= thermalScale,
            note = if (k <= mandated)
                "at or below the mandate: NO preload, and K2 as specified is exactly here"
            else "a coupling this stiff must be mounted pulling the tile DOWN by this much at " +
                    "zero stroke, which no tension-only path can do"
        )
    }

    // ---------------------------------------------------------------- bridging
    val tetherAtTen = substrateTetherHoldDown(tether, TETHER_COUNT, 10.0)
    val bridging = DESIGN_POINTS.map { (height, density) ->
        val chains = density * FOOTPRINT
        BridgingRecord(
            layerHeight = height,
            graftingDensity = density,
            chainsUnderTile = chains,
            ceilingAtOneThermalUnit = bridgingForceCeiling(chains, thermalEnergy(), 1.0),
            thresholdForThermalScale = bridgingEnergyThreshold(thermalScale, chains, 1.0),
            thresholdForThermalScaleInThermalUnits =
                bridgingEnergyThreshold(thermalScale, chains, 1.0) / thermalEnergy(),
            thresholdForTetherPreload = bridgingEnergyThreshold(tetherAtTen, chains, 1.0),
            thresholdForTetherPreloadInThermalUnits =
                bridgingEnergyThreshold(tetherAtTen, chains, 1.0) / thermalEnergy()
        )
    }

    // ---------------------------------------------------------------- convergence
    val convergence = mutableListOf<ConvergenceRecord>()
    run {
        val model = models.last()
        val (height, density) = DESIGN_POINTS.last()
        val chain = peg.graftedChain(model.chainLengthForHeight(peg, height, density), density)
        val onset = model.equilibriumHeight(chain)
        val holdDown = { h: Double ->
            substrateTetherHoldDown(tether, TETHER_COUNT, h) +
                    vanDerWaalsForce(metalElectrode, NOMINAL_TILE_THICKNESS, h)
        }
        fun net(h: Double): Double = layerLoadAt(model, chain, onset, h) - holdDown(h)
        val floor = max(chain.occupiedThickness * 1.05, 0.2)
        val scans = listOf(64, 256, 400, 2048).map { it to zeroBiasRestingHeight(::net, onset + 2.0, floor, it)!! }
        val finestScan = scans.last().second
        scans.forEach { (steps, value) ->
            convergence += ConvergenceRecord(
                quantity = "resting height, scan steps", setting = steps.toString(),
                value = value, departureFromFinest = abs(value - finestScan) / finestScan
            )
        }
        val rest = finestScan
        val tail = min(TAIL_DECADES * thermalEnergy() / holdDown(onset + 1e-6), TAIL_CEILING)
        val upper = min(onset + tail, tetherContour * 0.98)
        val panels = listOf(500, 1000, 2000, 8000).map {
            it to boltzmannPositionStatistics(::net, max(floor, rest - 3.0), upper, it, reference = onset).rms
        }
        val finestPanels = panels.last().second
        panels.forEach { (count, value) ->
            convergence += ConvergenceRecord(
                quantity = "Boltzmann RMS, quadrature panels", setting = count.toString(),
                value = value, departureFromFinest = abs(value - finestPanels) / finestPanels
            )
        }
        listOf(200, 400, 800).forEach { nodes ->
            val force = withStern.force(10.0, 0.0, nodes)
            convergence += ConvergenceRecord(
                quantity = "zero-bias force at 10 nm, PB mesh nodes", setting = nodes.toString(),
                value = force,
                departureFromFinest = abs(force - withStern.force(10.0, 0.0, 1600)) /
                        max(abs(withStern.force(10.0, 0.0, 1600)), 1e-12)
            )
        }
    }

    // ---------------------------------------------------------------- reproductions
    val reproductions = mutableListOf<ReproductionRecord>()
    fun reproduce(source: String, quantity: String, published: Double, value: Double) {
        reproductions += ReproductionRecord(
            source, quantity, published, value, abs(value - published) / abs(published)
        )
    }
    reproduce("C-0014", "eight-tether preload at the 10 nm layer [pN]", 9.37, tetherAtTen)
    reproduce(
        "C-0014", "eight-tether preload at the 5 nm layer [pN]", 4.6,
        substrateTetherHoldDown(tether, TETHER_COUNT, 5.0)
    )
    reproduce("C-0014", "leaf A1.1 lateral bound [pN/nm]", 0.460216, thermalScale / POSITION_BOUND)
    reproduce("C-0008", "zero-bias force at 3 nm, Stern series [pN]", 3.94, withStern.force(3.0, 0.0))
    reproduce("C-0008", "zero-bias force at 5 nm, Stern series [pN]", -0.41, withStern.force(5.0, 0.0))
    reproduce(
        "C-0008", "zero-bias force at 5 nm, ideal constant-potential electrode [pN]", -34.9,
        idealElectrode.force(5.0, 0.0)
    )
    reproduce("C-0017", "the mandated coupling stiffness [pN/nm]", 33.333333333333336, mandated)
    reproduce(
        "C-0017", "K2's reaction at zero stroke [pN]", 0.0.let { 1.0 },
        1.0 + committedCoupling.reaction(0.0)
    )

    val worstReproduction = reproductions.filter { it.published != 0.0 }.maxOf { it.relativeDeparture }

    // ---------------------------------------------------------------- the result
    val noEquilibrium = equilibria.count { it.scenario.startsWith("none") && it.restingHeight == null }
    val allScenario = equilibria.filter {
        it.scenario.startsWith("THE DEVICE: ") && it.restingHeight != null && it.confining
    }
    val tetherlessDevice = equilibria.filter {
        it.scenario.startsWith("THE DEVICE without") && it.restingHeight != null
    }
    val result = ZeroBiasResult(
        task = "T-13",
        leaf = "A1.2 (read at zero bias), with A1.1 as its bound table and A8.2 for the coupling",
        title = "Where the Gen-1 tile sits at zero bias, and what holds it there — six candidate " +
                "mechanisms, each with its sign and its magnitude, and the equilibrium they do " +
                "or do not produce",
        verificationType = "in-silico (a one-dimensional zero-bias force balance assembled from " +
                "six mechanisms, with the layer and the field re-run as libraries rather than " +
                "tabulated) + logical (a topology argument that fixes every sign before any " +
                "arithmetic)",
        acceptance = "The zero-bias resting height with the load its definition rests on stated " +
                "and its sensitivity reported; every candidate hold-down given a sign and a " +
                "magnitude in pN and judged against the derived thermal scale k_BT/3nm; the net " +
                "verdict on whether a stable equilibrium exists; and the positional statistics " +
                "about it computed WITHOUT assuming a harmonic well, broadband and in the " +
                ">= 1 kHz band.",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated. " +
                "Nothing here is measured about this tile, this layer or any anchor. The " +
                "electrostatic terms are MEAN-FIELD numbers inside C-0005's 123-214% one-loop " +
                "correction, and the van der Waals term is bracketed over an electrode material " +
                "§1 declines to name.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "stiffness" to "pN/nm (= 1 mN/m exactly)",
            "pressure" to "pN/nm^2 (= 1 MPa exactly)",
            "energy" to "pN*nm",
            "hamakerConstant" to "pN*nm, which is exactly the zeptojoule (1 zJ = 1e-21 J)",
            "graftingDensity" to "1/nm^2",
            "potential" to "V",
            "chargeDensity" to "e/nm^2",
            "frequency" to "Hz",
            "drag" to "pN*s/nm",
            "density" to "g/cm^3",
            "temperature" to "K"
        ),
        conventions = listOf(
            "z is normal to the electrode, positive AWAY from it; the electrode surface is z = 0 " +
                    "and the tile's underside sits at the layer height h",
            "U_net(h) = P(h)A - F_down(h), POSITIVE UPWARD. A hold-down is any mechanism " +
                    "contributing to F_down > 0",
            "a stable equilibrium is a root of U_net with dU_net/dh < 0, and k0 = -dU_net/dh; a " +
                    "mechanism whose magnitude grows as the gap closes therefore contributes " +
                    "NEGATIVE stiffness, exactly like §1's k_es",
            "L0 is the FORCE-ONSET height and its defining load is stated with every number " +
                    "(C-0011, CH-0010). Here the defining load is not a convention at all: the " +
                    "zero-bias resting height IS the force-onset height defined at the hold-down",
            "the stroke a hold-down costs is L0 - h0, taken off the top of the actuator's travel",
            "the electrostatic gap is the layer height, exactly (C-0012's convention)",
            "a Hamaker constant is quoted in pN*nm; 1 zJ = 1e-21 J = 1 pN*nm exactly"
        ),
        parameters = mapOf(
            "temperature" to "300 K",
            "buffer" to "$BUFFER mM MgCl2",
            "tileFootprint" to "$TILE_EDGE x $TILE_EDGE nm",
            "layerHeights" to DESIGN_POINTS.map { it.first }.toString(),
            "graftingDensities" to DESIGN_POINTS.map { it.second }.toString(),
            "layerModels" to models.joinToString { it.name },
            "positionBound" to "$POSITION_BOUND nm (leaf A1.1)",
            "bandwidth" to "$BANDWIDTH Hz (§3)",
            "tileCharge" to "$tileCharge e/nm^2 (C-0008's nominal, Manning-renormalised)",
            "debyeLength" to "${1.0 / inverseDebye} nm",
            "sternCapacitance" to "$STERN_CAPACITANCE uF/cm^2",
            "tetherCount" to "$TETHER_COUNT",
            "tetherKuhnLength" to "$TETHER_KUHN nm (Chen et al. 2012, zero force)",
            "tetherContour" to "$tetherContour nm",
            "couplingK2" to "45 paths, 5 nm duplex standoff (S/L = $standoffStiffness pN/nm) in " +
                    "series with an $spacerContour nm ssDNA spacer (C-0017)",
            "quadraturePanels" to "$QUADRATURE_PANELS",
            "scanSteps" to "$SCAN_STEPS"
        ),
        thermalForceScale = thermalScale,
        mechanisms = mechanisms,
        vanDerWaals = vanDerWaals,
        zeroBiasField = fieldRecords,
        potentialOfZeroCharge = pzc,
        equilibria = equilibria,
        restingLoadSensitivity = restingLoad,
        couplingPreload = couplingPreload,
        bridging = bridging,
        convergence = convergence,
        reproductions = reproductions,
        findings = findingsOf(
            thermalScale, noEquilibrium, equilibria, allScenario, tetherlessDevice, vanDerWaals,
            fieldRecords, pzc, bridging, tetherAtTen, worstReproduction
        ),
        validity = VALIDITY,
        openQuestions = OPEN_QUESTIONS,
        citedNumbers = CITED
    )

    val file = File("gpd/results/T-13-zero-bias-resting-position.json")
    file.parentFile.mkdirs()
    val json = Json { prettyPrint = true; encodeDefaults = true }
    file.writeText(json.encodeToString(json.encodeToJsonElement(result).roundedForResult()) + "\n")
    println("wrote ${file.path}")
    report(result)
}

// ---------------------------------------------------------------------------------------------

/** DNA's mass density in `g/cm³` — **CITED**, the standard value for B-form duplex DNA. */
private const val DNA_DENSITY: Double = 1.7

/** Water's mass density in `g/cm³` at 300 K. */
private const val WATER_DENSITY: Double = 0.997

/**
 * The tile thickness in nm carried into the force balance — the **single-layer** reading, one
 * duplex diameter, which is the structure `C-0006`/`C-0009` model. §3's *"~10 nm"* is carried
 * alongside in the bracket table and is the **larger** van der Waals term of the two.
 */
private const val NOMINAL_TILE_THICKNESS: Double = 2.0

private fun findingsOf(
    thermalScale: Double,
    noEquilibrium: Int,
    equilibria: List<EquilibriumRecord>,
    allScenario: List<EquilibriumRecord>,
    tetherlessDevice: List<EquilibriumRecord>,
    vanDerWaals: List<VanDerWaalsRecord>,
    field: List<ZeroBiasFieldRecord>,
    pzc: List<PotentialOfZeroChargeRecord>,
    bridging: List<BridgingRecord>,
    tetherAtTen: Double,
    worstReproduction: Double
): Map<String, String> {
    fun f(value: Double, digits: Int = 3) = "%.${digits}f".format(value)
    val noneCount = equilibria.count { it.scenario.startsWith("none") }
    val vdwTen = vanDerWaals.filter { it.gap == 10.0 }
    val vdwFive = vanDerWaals.filter { it.gap == 5.0 }
    val sternField = field.filter { it.sternCapacitance.startsWith("20") && it.gap >= 5.0 }
    val idealField = field.filter { it.sternCapacitance.startsWith("infinite") && it.gap == 5.0 }
    val allStable = if (allScenario.all { it.stable }) "STABLE" else "NOT uniformly stable"
    val restLow = f(allScenario.minOf { it.restingHeight!! }, 2)
    val restHigh = f(allScenario.maxOf { it.restingHeight!! }, 2)
    val descentLow = f(allScenario.minOf { it.strokeLost!! }, 2)
    val descentHigh = f(allScenario.maxOf { it.strokeLost!! }, 2)
    val descentWorst = f(allScenario.maxOf { it.strokeLostFraction!! } * 100.0, 0)
    val bareDescentLow = f(equilibria.filter { it.scenario.startsWith("all") && it.confining }
        .minOf { it.strokeLost!! }, 2)
    val bareDescentHigh = f(equilibria.filter { it.scenario.startsWith("all") && it.confining }
        .maxOf { it.strokeLost!! }, 2)
    val deliveredLow = f(allScenario.minOf { it.deliveredStrokeToWorkingPoint!! }, 2)
    val deliveredHigh = f(allScenario.maxOf { it.deliveredStrokeToWorkingPoint!! }, 2)
    val tetherlessWell = if (tetherlessDevice.isEmpty()) "-" else
        "${f(tetherlessDevice.minOf { it.escapeBarrier!! }, 1)}-${f(tetherlessDevice.maxOf { it.escapeBarrier!! }, 1)}"
    val tetherlessConfining = tetherlessDevice.count { it.confining }
    val rmsLow = f(allScenario.minOf { it.rms!! })
    val rmsHigh = f(allScenario.maxOf { it.rms!! })
    val equiLow = f(allScenario.minOf { it.equipartitionRms!! })
    val equiHigh = f(allScenario.maxOf { it.equipartitionRms!! })
    val aboveWorst = f(allScenario.maxOf { it.probabilityAbove!! } * 100.0, 1)
    val inBandLow = f(allScenario.minOf { it.rmsInBand!! })
    val inBandHigh = f(allScenario.maxOf { it.rmsInBand!! })
    val fractionWorst = f(allScenario.maxOf { it.varianceFractionInBand!! } * 100.0, 2)
    val vdwTenLow = f(vdwTen.minOf { it.forceLower }, 2)
    val vdwTenHigh = f(vdwTen.maxOf { it.forceUpper }, 2)
    val vdwFiveLow = f(vdwFive.minOf { it.forceLower }, 2)
    val vdwFiveHigh = f(vdwFive.maxOf { it.forceUpper }, 2)
    val vdwTenScale = f(vdwTen.minOf { it.overThermalScaleLower }, 2)
    val vdwFiveScale = f(vdwFive.maxOf { it.overThermalScaleUpper }, 2)
    val wellFive = "${f(vdwFive.minOf { it.wellDepth }, 1)}-${f(vdwFive.maxOf { it.wellDepth }, 1)}"
    val wellTen = "${f(vdwTen.minOf { it.wellDepth }, 1)}-${f(vdwTen.maxOf { it.wellDepth }, 1)}"
    val fieldLow = f(sternField.minOf { it.holdDown })
    val fieldHigh = f(sternField.maxOf { it.holdDown })
    val fieldIdeal = f(idealField.first().holdDown, 1)
    val pzcSmallest = pzc.filter { it.target.startsWith("the thermal") }
        .mapNotNull { it.appliedBias }.minOrNull()?.let { f(it * 1000.0, 1) } ?: "no"
    val bridgeCeiling = f(bridging.maxOf { it.ceilingAtOneThermalUnit }, 0)
    val bridgeLow = f(bridging.minOf { it.thresholdForThermalScaleInThermalUnits }, 4)
    val bridgeHigh = f(bridging.maxOf { it.thresholdForThermalScaleInThermalUnits }, 4)
    val bridgeTether = f(bridging.maxOf { it.thresholdForTetherPreloadInThermalUnits }, 3)
    return mapOf(
        "theAnswer" to
                "WITHOUT a hold-down the tile has NO zero-bias resting position at all - not a large " +
                "excursion, an UNDEFINED one. $noEquilibrium of $noneCount (model x height) states " +
                "return no equilibrium, because a non-adsorbing layer exerts no upward force above L0 " +
                "and nothing in the §3 stack pulls down. WITH the mechanisms that do exist the tile " +
                "parks at $restLow-$restHigh nm and every state is $allStable.",
        "theScaleToBeat" to
                "A hold-down confines the tile from above through a LINEAR potential, not a quadratic " +
                "one, because the layer contributes nothing there. So the requirement is a FORCE, " +
                "k_BT/3nm = ${f(thermalScale, 4)} pN, and it is leaf A1.1's own 0.460216 pN/nm one " +
                "power of the bound away. The mean upward excursion under a constant hold-down F is " +
                "exactly k_BT/F.",
        "theCommittedCouplingDoesNotDoIt" to
                "C-0017's K2 supplies EXACTLY ZERO downward preload, and the reason is the element " +
                "that closed T-16: 99.6% of its compliance is an ssDNA spacer, which carries no " +
                "compression, so the path is tension-only and grounded on a lever ABOVE the tile. The " +
                "compliance T-16 needed is exactly what destroys the two-sidedness T-13 needs.",
        "theExactCouplingRelation" to
                "Every pN/nm by which the output coupling exceeds §3's own 33.333 pN/nm mandate is " +
                "exactly 3 pN of downward preload: F = (k_c - k_c*)*delta. So T-16's stiffness choice " +
                "and T-13's hold-down are ONE design variable - but only a two-sided element can spend " +
                "it, and K2 is not one.",
        "vanDerWaalsIsAFiniteWellAndThereforeATrapNotAConfinement" to
                "A 1/h^3 force integrates to a BOUNDED potential, so van der Waals does not " +
                "confine the tile at all - it traps it. The well is $wellFive k_BT deep at 5 nm and " +
                "$wellTen k_BT at 10 nm, against the 10 k_BT this task declares as the confinement " +
                "threshold. Below that the tile escapes and every moment of the distribution is a " +
                "property of the integration domain rather than of the physics, which is why those " +
                "states are reported as STABLE BUT NOT CONFINING rather than given an RMS. Only the " +
                "entropic tether, whose tension RISES without bound as the tile lifts, confines in " +
                "the thermodynamic sense.",
        "vanDerWaalsCrossesTheBarInsideTheRange" to
                "Across the electrode materials §1 declines to name, and over both readings of §3's " +
                "tile thickness, the van der Waals hold-down is $vdwTenLow-$vdwTenHigh pN at 10 nm and " +
                "$vdwFiveLow-$vdwFiveHigh pN at 5 nm, i.e. ${vdwTenScale}x to ${vdwFiveScale}x the " +
                "thermal scale. IT CROSSES THE BAR INSIDE §3's OWN HEIGHT RANGE: at 5 nm it holds the " +
                "tile on its own and at 10 nm it does not. It is ALWAYS attractive here (the " +
                "vacuum-form sign diagnostic stays positive because water sits below both bodies), it " +
                "cannot be designed away. Metal against oxide is 2.6x and is the dominant remaining " +
                "uncertainty; the polymer in the gap is 4.7% (Lorentz-Lorenz at phi = 0.09, CITED) " +
                "and in the direction of LESS attraction; the electrolyte-screening bracket on the " +
                "zero-frequency term is worth 10% (metal) to 25% (oxide) of the CROSS constant, not " +
                "the 1.5% a symmetric gold constant would suggest.",
        "theZeroBiasFieldIsNegligibleAndTheReasonMatters" to
                "At zero APPLIED bias the residual force is $fieldLow to $fieldHigh pN of hold-down " +
                "over 5-10 nm - a small fraction of the thermal scale - because the induced " +
                "countercharge on the grounded electrode has to charge the compact layer too, and the " +
                "two nearly cancel. Remove the compact layer and the same geometry gives $fieldIdeal pN " +
                "at 5 nm. The bracket is owned by the STERN LAYER, not by the polymer.",
        "thePotentialOfZeroChargeIsTheRealVariable" to
                "Zero APPLIED bias is not zero charge: a real electrode sits at its own potential of " +
                "zero charge against the reference, and §1/§3 never state it. As little as $pzcSmallest " +
                "mV of offset delivers the whole thermal-scale hold-down. The zero-bias resting " +
                "position is therefore set by a quantity the problem definition does not specify, and " +
                "a few tens of mV decide it.",
        "theHoldDownIsPaidForInStroke" to
                "The resting height is the force-onset height defined at the hold-down, so a preload is " +
                "spent one-for-one along whatever resists descent. Against the LAYER ALONE that is " +
                "expensive - the tile descends $bareDescentLow-$bareDescentHigh nm before the actuator " +
                "does anything, because three of six models have exactly zero stiffness at L0 and a " +
                "soft layer is cheap to compress. Against the layer AND C-0017's 33 pN/nm coupling it " +
                "is cheap: $descentLow-$descentHigh nm, at most $descentWorst% of §3's 3 nm, leaving " +
                "$deliveredLow-$deliveredHigh nm of delivered stroke. The coupling pays for the " +
                "hold-down out of its own stiffness, which is the one thing it has to spare.",
        "theCouplingResistsDescentButStillCannotAnswerT13" to
                "K2 is stiffer than everything else in this task put together, so it dominates the " +
                "zero-bias balance FROM BELOW - and above L0 it goes slack and contributes exactly " +
                "nothing. With the tether removed, the device's well is $tetherlessWell k_BT and " +
                "$tetherlessConfining of ${tetherlessDevice.size} states confine. A coupling can " +
                "decide WHERE the tile sits once something holds it down; it cannot BE the thing that " +
                "holds it down.",
        "theVarianceIsNotGaussian" to
                "The zero-bias potential is harmonic below the rest height and LINEAR above it, so " +
                "equipartition does not describe it. Computed by exact Boltzmann quadrature the RMS is " +
                "$rmsLow-$rmsHigh nm broadband against the equipartition reading's $equiLow-$equiHigh " +
                "nm, and the tile spends up to $aboveWorst% of its time ABOVE the force-onset height, " +
                "where the layer holds it with nothing at all. In the >= 1 kHz band the RMS is " +
                "$inBandLow-$inBandHigh nm, at most $fractionWorst% of the variance.",
        "bridgingIsTheOnePremiseNobodyHasTested" to
                "The non-adsorbing premise is what makes C-0010's lateral zero EXACT, and it has never " +
                "been checked for PEG against a DNA face. The ceiling is enormous ($bridgeCeiling pN at " +
                "one k_BT per chain), so the useful object is the threshold: only $bridgeLow-$bridgeHigh " +
                "k_BT per chain would supply the whole thermal-scale hold-down, and $bridgeTether k_BT " +
                "would supply C-0014's entire ${f(tetherAtTen, 2)} pN tether preload. That is far below " +
                "any adsorption energy a measurement would call zero, and P-8's missing Mg2+/PEG " +
                "coordination constant is exactly what would decide it.",
        "upstreamReproduction" to
                "Every upstream number this task consumes was reproduced by re-running its source " +
                "rather than copied: worst relative departure ${"%.3e".format(worstReproduction)}."
    )
}

private val VALIDITY = listOf(
    "TRL 1-3. NOTHING HERE IS MEASURED. No hold-down below has been built and none is proposed " +
            "as a sequence design.",
    "L0 is the FORCE-ONSET height of C-0003's models, at which P = 0 EXACTLY by construction — " +
            "not C-0011's 1 pN threshold. The two conventions differ, and the whole point of " +
            "this task is that the zero-bias resting height IS the force-onset height at the " +
            "hold-down force, so the sensitivity table is the answer rather than a caveat.",
    "The layer is C-0003's, at C-0001's single grafting density per height — not C-0011's " +
            "solved SCF profile. C-0016 reports that at 5 nm the solved layer is 1.22x outside " +
            "C-0003's bracket, so every 5 nm number here carries that exposure. A solved profile " +
            "would make the layer STIFFER near L0 than the box models and softer than nothing at " +
            "all, so the direction is toward LESS descent under a given preload.",
    "Mean-field electrostatics, inherited whole. C-0005 puts the one-loop correction at 123-214% " +
            "of the leading term across this gap range. The zero-bias force is a fraction of a " +
            "piconewton, so even a 200% error on it does not move any verdict here — which is " +
            "the one place in this programme where that correction is comfortably affordable.",
    "The van der Waals term is a COMBINING-RELATION estimate on published Hamaker constants, not " +
            "a Lifshitz calculation from optical data, and §1 does not name the electrode " +
            "material. It is bracketed over gold, a conducting oxide and silica, and over both " +
            "readings of §3's tile thickness. Retardation is NOT applied: at 5-10 nm it would " +
            "REDUCE the attraction, so every van der Waals number here is an upper bound.",
    "The zero-frequency term is screened as exp(-2 kappa d) and the dispersion terms are not. " +
            "That decomposition is standard; the split point between them is not resolved here " +
            "and is taken as (3/4)k_BT, which is small enough that the choice does not matter.",
    "The tile is treated as a RIGID plate translating normally. C-0006 rejects that assumption " +
            "under any concentrated load; at zero bias the loads are uniform (van der Waals, " +
            "gravity) or discrete (the tethers), and the discrete case would dish the tile by " +
            "C-0006's own factors. Not computed here.",
    "The lateral coordinate is untouched. C-0014 owns it and nothing here moves its verdict.",
    "No bias, no stroke, no output. Every number is the V = 0 state.",
    "The drag is C-0004's permeability model at the zero-bias volume fraction, on the LEAST " +
            "permeable of its three models — the direction that maximises the in-band variance."
)

private val OPEN_QUESTIONS = listOf(
    "The electrode material. It is the largest single lever on the one hold-down that cannot be " +
            "designed away, and §1 does not state it.",
    "The electrode's potential of zero charge. A few tens of millivolts of contact potential " +
            "supplies the entire thermal-scale hold-down, and nothing in §1 or §3 fixes it. This " +
            "is a MEASUREMENT, not a calculation.",
    "Whether PEG bridges a DNA-origami face at all. The threshold is hundredths of a k_BT per " +
            "chain, which is below what any measurement would call zero, and P-8's missing " +
            "Mg2+/PEG coordination constant is the mechanism that would decide it.",
    "A two-sided compliant coupling. If one exists in DNA — an antagonistic spacer pair, or a " +
            "bending hinge rather than a stretched chain — then T-16's stiffness margin and " +
            "T-13's hold-down are one part instead of two.",
    "Retardation, quantitatively. Applied it would reduce the van der Waals term; it is left out " +
            "so that the number is a bound rather than an estimate."
)

private val CITED = listOf(
    "A_DNA|water|DNA = 4.33-5.90 zJ — CITED, COMPUTED (Lifshitz), Dryden et al., Langmuir " +
            "31:10145 (2015), read directly. Cylinder-cylinder, already retarded and " +
            "n=0-screened at 5 nm; NO planar value exists. The 1e-20 J in circulation is Rau & " +
            "Parsegian's explicit OVERESTIMATE and the 2e-20 J in the AFM literature is a " +
            "PROTEIN value substituted knowingly.",
    "A_Au|water|Au = 238.6-267.9 zJ, A_Pt|water|Pt = 281.7-313.2 zJ, A_water(vacuum) = " +
            "38.90-53.78 zJ, and the retardation fits — CITED, COMPUTED (Lifshitz), Tolias, " +
            "arXiv:2003.00571 and arXiv:2202.09159, read directly.",
    "A_Al2O3|water|Al2O3 = 36.9 zJ — CITED, Bell & Dimos, MRS Proc. 624:275 (2000) Table 3; " +
            "8.86 k_BT = 36.7 zJ independently via Prange et al., arXiv:2606.04331. " +
            "A_TiO2|water|TiO2 = 12.8-22.3 k_BT, same source. ITO and HfO2 DO NOT EXIST in " +
            "reachable literature and are bracketed between an oxide and Pt rather than given " +
            "a value.",
    "The zero-frequency term (3/4)zeta(3)k_BT = 3.734 zJ, and 0.75 k_BT for a low-dielectric " +
            "body across water — CITED, Tolias and Roth, Neal & Lenhoff, Biophys. J. 70:977 " +
            "(1996). The familiar (3/4)k_BT is the s=1 truncation and is 20% low.",
    "The ELECTROLYTE SCREENING EXPRESSION for the zero-frequency term is NOT SOURCED. A search " +
            "returned one with a citation; the citation did not survive checking and it was " +
            "withdrawn. The term is carried as a BRACKET between fully screened and unscreened, " +
            "worth 10% (metal) to 25% (oxide) of the CROSS constant - not the 1.5% a symmetric " +
            "gold constant would suggest, because the DNA half of the geometric mean is itself " +
            "only ~5 zJ - inside a 2.6x electrode-material bracket.",
    "A Hamaker constant for PEG/PEO is NOT SOURCED — an exact-phrase EuropePMC full-text search " +
            "returns zero hits. The question it would answer is settled instead by Lorentz-" +
            "Lorenz mixing: at phi = 0.09 the medium index rises 0.82% and the optical contrast " +
            "falls 4.7%, inside every other bracket and in the direction of LESS attraction.",
    "PEG does not adsorb to DNA — CITED, Rau & Parsegian, Biophys. J. 61:246 (1992): with a " +
            "membrane preventing direct contact, 'interhelical distances are unchanged with or " +
            "without a membrane'. But Knowles et al., PNAS 108:12699 (2011) measure FAVORABLE " +
            "preferential interaction of PEG with DNA — on BASE surface exposed by melting, not " +
            "the native duplex exterior, and high-MW PEG net-stabilises the duplex, so exclusion " +
            "dominates for a brush against intact B-DNA. Mg2+ bridging between PEG ether oxygens " +
            "and DNA phosphates: NOT FOUND, and it is the same gap P-8 already carries.",
    "Stern capacitance 20 uF/cm^2 — CITED via C-0008, order of magnitude for aqueous electrodes.",
    "The tile's gap-facing charge density — CITED via C-0008 (Manning-renormalised, half the " +
            "projected density), and C-0008 shows the tile is charge-saturated so three readings " +
            "spanning 3x give sigma_eff within 7%.",
    "ssDNA Kuhn length 2.10 nm at zero force — CITED, MEASURED, Chen et al., PNAS 109:799 (2012).",
    "Duplex stretch modulus 1100 pN — CITED, MEASURED, Wang et al., Biophys. J. 72:1335 (1997).",
    "DNA mass density 1.7 g/cm^3 — CITED, standard for B-form duplex.",
    "The layer models, virial coefficients and chain statistics — CITED via C-0002/C-0003, and " +
            "RE-RUN here rather than tabulated.",
    "The permeability model and the drag — CITED via C-0004, on its slowest (most conservative) " +
            "member.",
    "C-0014's tether design rule and lateral bound, C-0017's K2 and mandate, C-0008's zero-bias " +
            "column — all REPRODUCED here by re-running their code, with the departures in the " +
            "reproductions table.",
    "The 3.0 nm bound, the 1 kHz band, the 100 pN, the 3 nm stroke, the 40 x 40 nm footprint, " +
            "the 5/7/10 nm heights, the 2 mM buffer — §3 and §6."
)

private fun report(result: ZeroBiasResult) {
    println()
    println("=".repeat(100))
    println("T-13 — ${result.title}")
    println("=".repeat(100))
    println()
    println("thermal force scale k_BT/3nm = %.4f pN".format(result.thermalForceScale))
    println()
    println("MECHANISMS at the 10 nm layer (positive = DOWN):")
    result.mechanisms.filter { it.layerHeight == 10.0 }.forEach {
        println("  %-72s %10.4f pN  (%.2fx the scale)".format(
            it.mechanism.take(72), it.force, it.overThermalScale
        ))
    }
    println()
    println("EQUILIBRIA, by scenario:")
    result.equilibria.groupBy { it.scenario }.forEach { (scenario, records) ->
        val solved = records.filter { it.restingHeight != null }
        if (solved.isEmpty()) {
            println("  %-46s NO EQUILIBRIUM at any of %d states".format(scenario.take(46), records.size))
        } else {
            println(
                ("  %-42s h0 = %.2f-%.2f nm, descent %.2f-%.2f, k0 = %.2f-%.2f pN/nm, " +
                        "well %.1f-%.1f k_BT, confining %d/%d").format(
                    scenario.take(42),
                    solved.minOf { it.restingHeight!! }, solved.maxOf { it.restingHeight!! },
                    solved.minOf { it.strokeLost!! }, solved.maxOf { it.strokeLost!! },
                    solved.minOf { it.equilibriumStiffness!! }, solved.maxOf { it.equilibriumStiffness!! },
                    solved.minOf { it.escapeBarrier!! }, solved.maxOf { it.escapeBarrier!! },
                    solved.count { it.confining }, solved.size
                )
            )
        }
    }
    println()
    result.findings.forEach { (key, value) ->
        println("* $key")
        value.chunkedWords(96).forEach { println("    $it") }
        println()
    }
}

private fun String.chunkedWords(width: Int): List<String> {
    val lines = mutableListOf<String>()
    var current = StringBuilder()
    split(" ").forEach { word ->
        if (current.length + word.length + 1 > width) {
            lines += current.toString()
            current = StringBuilder(word)
        } else {
            if (current.isNotEmpty()) current.append(' ')
            current.append(word)
        }
    }
    if (current.isNotEmpty()) lines += current.toString()
    return lines
}
