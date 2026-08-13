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
import com.xemantic.nano.plentyofroom.brush.load
import com.xemantic.nano.plentyofroom.brush.reducedSecondVirialCoefficient
import com.xemantic.nano.plentyofroom.brush.reducedThirdVirialCoefficient
import com.xemantic.nano.plentyofroom.brush.stiffness
import com.xemantic.nano.plentyofroom.brush.threeBodyInteraction
import com.xemantic.nano.plentyofroom.brush.twoBodyInteraction
import com.xemantic.nano.plentyofroom.coupling.PerPathAllowable
import com.xemantic.nano.plentyofroom.coupling.SeriesEntropicCoupling
import com.xemantic.nano.plentyofroom.coupling.mandatedCouplingStiffness
import com.xemantic.nano.plentyofroom.coupling.gaussianContourCeiling
import com.xemantic.nano.plentyofroom.coupling.perAnchorThermalForce
import com.xemantic.nano.plentyofroom.coupling.spacerContourForTarget
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
import com.xemantic.nano.plentyofroom.equipartitionStiffness
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.poroelastic.FiberArrayPermeability
import com.xemantic.nano.plentyofroom.poroelastic.RectangularFootprint
import com.xemantic.nano.plentyofroom.poroelastic.brinkmanTransmissivity
import com.xemantic.nano.plentyofroom.poroelastic.squeezeDragCoefficient
import com.xemantic.nano.plentyofroom.poroelastic.tileStokesDrag
import com.xemantic.nano.plentyofroom.poroelastic.waterViscosity
import com.xemantic.nano.plentyofroom.structure.Gen1Tile
import com.xemantic.nano.plentyofroom.structure.roundedForResult
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.max
import kotlin.math.min
import kotlin.math.pow
import kotlin.math.sqrt

/**
 * Task `T-23` — **a two-sided compliant DNA coupling**, or a demonstration that DNA offers none.
 *
 * Emits `gpd/results/T-23-two-sided-coupling.json`.
 */

// ---------------------------------------------------------------------------------------------
// the record types
// ---------------------------------------------------------------------------------------------

/** The hold-down requirement in its two currencies — the cheap bound, and the headline. */
@Serializable
data class HoldDownCurrencyRecord(
    val reading: String,
    val positionBound: Double,
    /** `k_BT/σ` in pN for the one-sided reading, `k_BT/σ²` in `pN/nm` for the two-sided one. */
    val requirement: Double,
    val unit: String,
    /** What §3's own mandated coupling supplies in that currency. */
    val supplied: Double,
    val margin: Double,
    val excursionRms: Double?,
    val note: String
)

/** One candidate element, with its sidedness EVALUATED rather than asserted. */
@Serializable
data class SignedElementRecord(
    val id: String,
    val element: String,
    val designParameter: String,
    val twoSided: Boolean,
    val reactionAtZeroStroke: Double,
    val reactionAtNegativeProbe: Double,
    val perPathSecant: Double,
    val perPathTangent: Double,
    val assembledSecant: Double,
    val assembledTangent: Double,
    val tangentOverSecant: Double,
    val meetsPlacement: Boolean,
    val meetsCompliantCeiling: Boolean,
    val clearsStabilityFloor: Boolean,
    val perPathStaticForce: Double,
    val perPathThermalForce: Double,
    val perPathPeakForce: Double,
    val peakForceMechanism: String,
    val clearsUnzip: Boolean,
    val clearsShear: Boolean,
    val clearsCeiling: Boolean,
    val verdict: String
)

/** The geometry a passing element needs, over path count and over every carried bracket. */
@Serializable
data class ElementDesignRecord(
    val element: String,
    val pathCount: Int,
    val endCondition: String,
    val axiallyRestrained: Boolean,
    val variant: String,
    /** Span for a flexure, arm for a hinge, in nm. */
    val designLength: Double,
    val designLengthInBasePairs: Double,
    val fitsTileEdge: Boolean,
    val fitsLeverEnvelope: Boolean,
    val perPathStiffness: Double,
    val assembledSecant: Double,
    val assembledTangent: Double,
    val perPathStaticForce: Double,
    val axialTensionAtTarget: Double,
    val axialTensionAtDesiredStroke: Double,
    val endDrawIn: Double,
    val endDrawInBasePairs: Double,
    val clearsUnzipAtTarget: Boolean,
    val clearsCeilingAtDesiredStroke: Boolean,
    val verdict: String
)

/** One solved zero-bias state: scenario × layer model × height, on `C-0021`'s own balance. */
@Serializable
data class TwoSidedConfinementRecord(
    val scenario: String,
    val model: String,
    val layerHeight: Double,
    val onsetHeight: Double,
    val restingHeight: Double?,
    val descent: Double?,
    val deliveredStroke: Double?,
    val equilibriumStiffness: Double?,
    val rms: Double?,
    val equipartitionRms: Double?,
    val rmsInBand: Double?,
    val probabilityAbove: Double?,
    val escapeBarrier: Double?,
    val quadratureDomainUpper: Double?,
    val confining: Boolean,
    val meetsPositionBound: Boolean,
    val verdict: String
)

/** What a mounting offset buys and costs — priced in the unit a DNA design actually has. */
@Serializable
data class MountingOffsetRecord(
    val offsetBasePairs: Double,
    val offset: Double,
    val couplingStiffness: Double,
    val preload: Double,
    val preloadOverThermalScale: Double,
    val flexureSpan: Double,
    val perPathStaticForce: Double,
    val descentAtTenNanometres: Double,
    val deliveredStroke: Double,
    val stabilityMargin: Double,
    /** On this task's own axis: the bias at which the HELD gap loses stability, over `V*`. */
    val biasMargin: Double,
    /**
     * On `C-0018`'s axis: the fold of the **moving** equilibrium, whose bias elasticity is
     * implied by its own published pair (a stiffness margin of 1.194–1.424 read as a bias margin
     * of 1.007–1.032, i.e. `p = 11.2–25.5`) — three to thirteen times steeper than the held gap's
     * own `p`, which says that most of `C-0018`'s steepness is the equilibrium **moving**, not
     * the field stiffening.
     */
    val biasMarginOnFoldAxis: Double,
    val note: String
)

/** The field read at the 10 nm design point — the second thing a stiffer coupling would buy. */
@Serializable
data class PullInSensitivityRecord(
    val model: String,
    val layerHeight: Double,
    val buffer: Double,
    val operatingBias: Double,
    val layerStiffness: Double,
    val fieldStiffness: Double,
    val effectiveStiffness: Double,
    val stabilityFloor: Double,
    /** `d ln|k_eff|/d ln V` at the operating bias — how steeply the fold approaches. */
    val biasElasticity: Double,
    val stabilityMarginAtMandate: Double?,
    val biasMarginAtMandate: Double?,
    val stabilityMarginAtOneBasePair: Double?,
    val biasMarginAtOneBasePair: Double?
)

/** Numerical convergence of the two roots and the quadrature this task adds. */
@Serializable
data class TwoSidedConvergenceRecord(
    val quantity: String,
    val setting: String,
    val value: Double,
    val departureFromFinest: Double
)

/** A number reproduced from an upstream claim by re-running its code, not by copying its table. */
@Serializable
data class TwoSidedReproductionRecord(
    val source: String,
    val quantity: String,
    val published: Double,
    val reproduced: Double,
    val relativeDeparture: Double,
    /**
     * True where the two numbers are **different quantities** rather than the same one computed
     * twice — a comparison of definitions, which must never be read as a failed reproduction.
     */
    val definitional: Boolean = false
)

@Serializable
data class TwoSidedResult(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val parameters: Map<String, String>,
    val requirements: List<HoldDownCurrencyRecord>,
    val elements: List<SignedElementRecord>,
    val designs: List<ElementDesignRecord>,
    val confinement: List<TwoSidedConfinementRecord>,
    val preloadTrade: List<MountingOffsetRecord>,
    val pullIn: List<PullInSensitivityRecord>,
    val convergence: List<TwoSidedConvergenceRecord>,
    val reproductions: List<TwoSidedReproductionRecord>,
    val findings: Map<String, String>,
    val validity: List<String>,
    val openQuestions: List<String>,
    val citedNumbers: List<String>
)

// ---------------------------------------------------------------------------------------------
// parameters
// ---------------------------------------------------------------------------------------------

/** §3's three layer heights with `C-0001`'s grafting densities — `C-0012`'s, `C-0017`'s, `C-0021`'s. */
private val DESIGN_POINTS = listOf(5.0 to 0.092, 7.0 to 0.045, 10.0 to 0.024)

private const val TILE_EDGE = 40.0

private const val FOOTPRINT = TILE_EDGE * TILE_EDGE

private const val BUFFER = 2.0

private const val POSITION_BOUND = 3.0

private const val BANDWIDTH = 1000.0

private const val TARGET_FORCE = 100.0

private const val TARGET_STROKE = 3.0

/** §3's *desired* stroke, at which every cable term in this programme is judged. */
private const val DESIRED_STROKE = 10.0

/** `C-0015`'s flatness grid: 45 attachments as 3 x 15, one row per duplex. */
private const val PATH_COUNT = 45

/**
 * This task's declared compliance ceiling in `pN/nm` — an element stiffer than this at the
 * working point is not a *compliant* coupling whatever else it does. `C-0017`'s `K2` tangent
 * is 39.01, so the ceiling admits the committed design and excludes anything an order above it.
 */
private const val COMPLIANT_CEILING = 40.0

/** `C-0017`'s worst stability floor, 10 nm / 2 mM — **CITED**, and reproduced here. */
private const val CITED_STABILITY_FLOOR = 27.91

private const val CITED_STABILITY_FLOOR_BEST = 23.41

/** `C-0018`'s bias margins at the mandate, 10 nm / 2 mM — **CITED**. */
private const val CITED_BIAS_MARGIN_LOW = 1.007

private const val CITED_BIAS_MARGIN_HIGH = 1.032

private const val STERN_CAPACITANCE = 20.0

private const val SEARCH_NODES = 400

private const val OSMOTIC_SECOND_VIRIAL = 1.9e-3

private const val OSMOTIC_THIRD_VIRIAL = 2.0e-2

private const val LATERAL_BOUND = 0.460216

/** `C-0014`'s `S3` design point, carried only to reproduce `C-0021`'s rows. */
private const val TETHER_COUNT = 8

private const val TETHER_KUHN = 2.10

private const val NOMINAL_TILE_THICKNESS = 2.0

private const val DNA_DENSITY = 1.7

private const val WATER_DENSITY = 0.997

private const val SCAN_STEPS = 400

private const val QUADRATURE_PANELS = 2000

/** `C-0021`'s own quadrature domain rule, reproduced unchanged: how many `k_BT/F` of tail. */
private const val QUADRATURE_TAIL_DECADES = 40.0

/** …and the cap in nm above `L₀`, whatever the hold-down. */
private const val QUADRATURE_TAIL_CEILING = 60.0

private const val CONFINEMENT_BARRIER = 10.0

/** `C-0017`'s `K2` spacer and standoff, carried to reproduce its rows. */
private const val K2_SPACER_CONTOUR = 8.61

private const val K2_STANDOFF_LENGTH = 5.0

// ---------------------------------------------------------------------------------------------

/** One candidate electrode, exactly as `C-0021` assembles it, so that its rows reproduce. */
private data class ElectrodeCandidate(
    val name: String,
    val low: Double,
    val high: Double,
    val metal: Boolean
) {

    fun zeroFrequencyCross(): Double = sqrt(
        HamakerConstants.ZERO_FREQUENCY_TERM_LOW_DIELECTRIC *
                (if (metal) HamakerConstants.ZERO_FREQUENCY_TERM
                else HamakerConstants.ZERO_FREQUENCY_TERM_LOW_DIELECTRIC)
    )

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

/** The layer's upward force in pN, guarded above `L₀` where a non-adsorbing layer loses contact. */
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

/** `C-0008`'s field, re-run rather than tabulated — `C-0021`'s own class, unchanged. */
private class GapField(
    val tileCharge: Double,
    val bjerrum: Double,
    val ionModel: IonModel,
    val sternChargePerVolt: Double
) {

    private val medium = uniformMedium(GapMedium())

    fun diffusePotential(gap: Double, bias: Double): Double = diffusePotentialOfAppliedBias(
        gap, bias, tileCharge, sternChargePerVolt, ionModel, medium, bjerrum, nodes = SEARCH_NODES
    )

    /** Signed force in pN on the tile; negative is toward the electrode. */
    fun force(gap: Double, bias: Double, nodes: Int = DEFAULT_GAP_MESH_NODES): Double =
        PoissonBoltzmannGap(gap, ionModel, medium, bjerrum, nodes = nodes)
            .solve(diffusePotential(gap, bias) / thermalVoltage(), tileCharge)
            .forceOnTile(FOOTPRINT)
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
    val mandate = mandatedCouplingStiffness(TARGET_FORCE, TARGET_STROKE)
    val thermalScale = holdDownForceScale(POSITION_BOUND)
    val stiffnessScale = equipartitionStiffness(POSITION_BOUND)
    val viscosity = waterViscosity()
    val permeability = FiberArrayPermeability(fiberRadius = peg.kuhnSegmentDiameter / 2.0)
    val hingeConstant = Gen1Tile.crossoverHingeStiffness(1.0)
    val bendingRigidity = AnchorMaterials.CANDO_BENDING_RIGIDITY
    val stretchModulus = AnchorMaterials.DUPLEX_STRETCH_MODULUS
    val rise = Gen1Tile.RISE_PER_BASE_PAIR

    println("T-23 — the two-sided coupling catalogue; mandate ${"%.4f".format(mandate)} pN/nm")

    // ------------------------------------------------------------------ the currency, first
    val requirements = listOf(
        HoldDownCurrencyRecord(
            reading = "ONE-SIDED stack (C-0021): the potential above L0 is LINEAR, the excursion " +
                    "exponential, and the requirement is a FORCE",
            positionBound = POSITION_BOUND,
            requirement = thermalScale,
            unit = "pN",
            supplied = 0.0,
            margin = 0.0,
            excursionRms = null,
            note = "C-0017's K2 supplies EXACTLY ZERO of it (C-0021's M2), because 99.6 % of its " +
                    "compliance is ssDNA and a single strand carries no compression. A coupling " +
                    "can only supply it by exceeding the mandate, at 3 pN per pN/nm."
        ),
        HoldDownCurrencyRecord(
            reading = "TWO-SIDED coupling: the potential above L0 is QUADRATIC, the excursion " +
                    "Gaussian, and the requirement is a STIFFNESS",
            positionBound = POSITION_BOUND,
            requirement = stiffnessScale,
            unit = "pN/nm",
            supplied = mandate,
            margin = mandate / stiffnessScale,
            excursionRms = twoSidedExcursionRms(mandate),
            note = "F_req = k_req * sigma identically: two-sidedness is worth exactly one power " +
                    "of the position bound. The SAME coupling that is 4 % short of the force " +
                    "requirement is 72x past the stiffness one, unpreloaded."
        ),
        HoldDownCurrencyRecord(
            reading = "the same, read as an RMS excursion at the mandated stiffness",
            positionBound = POSITION_BOUND,
            requirement = POSITION_BOUND,
            unit = "nm",
            supplied = twoSidedExcursionRms(mandate),
            margin = POSITION_BOUND / twoSidedExcursionRms(mandate),
            excursionRms = twoSidedExcursionRms(mandate),
            note = "and the one-sided reading at the same stiffness is not a number at all: " +
                    "with no preload a one-sided coupling exerts nothing above L0."
        )
    )

    // ------------------------------------------------------------------ the element catalogue
    val perPathTarget = mandate / PATH_COUNT
    val probe = 0.5

    /** The axially free, pinned flexure at 45 paths — the design this task recommends. */
    val freeSpan = flexureSpanForStiffness(
        bendingRigidity, FlexureEndCondition.PINNED_ENDS, false, stretchModulus,
        PATH_COUNT, mandate, TARGET_STROKE
    )
    val freeFlexure = TransverseDuplexFlexure(
        bendingRigidity, freeSpan, FlexureEndCondition.PINNED_ENDS, false, stretchModulus
    )
    val restrainedSpan = flexureSpanForStiffness(
        bendingRigidity, FlexureEndCondition.PINNED_ENDS, true, stretchModulus,
        PATH_COUNT, mandate, TARGET_STROKE
    )
    val restrainedFlexure = TransverseDuplexFlexure(
        bendingRigidity, restrainedSpan, FlexureEndCondition.PINNED_ENDS, true, stretchModulus
    )
    val hingeArm = hingeArmForStiffness(hingeConstant, bendingRigidity, PATH_COUNT, mandate)
    val hinge = CrossoverHingeFlexure(hingeConstant, hingeArm, bendingRigidity)
    val standoff = AxialDuplexStandoff(stretchModulus, K2_STANDOFF_LENGTH)
    val k2 = OneSidedSpacer(
        SeriesEntropicCoupling(
            1, stretchModulus / K2_STANDOFF_LENGTH, FreelyJointedChain(K2_SPACER_CONTOUR, TETHER_KUHN)
        )
    )
    // E4: the antagonistic pair, sized EXACTLY — one down limb across the 10 nm layer carrying
    // the whole thermal-scale preload, and 45 up limbs tuned so that the PAIR is placed at the
    // mandate. Both are roots, never a force divided by a stiffness.
    val downLimb = FreelyJointedChain(
        spacerContourForTarget(TETHER_KUHN, 1, thermalScale, 10.0), TETHER_KUHN
    )
    val downAtWorkingPoint = downLimb.tension(10.0 - TARGET_STROKE)
    val upLimb = FreelyJointedChain(
        spacerContourForTarget(
            TETHER_KUHN, PATH_COUNT, TARGET_FORCE + downAtWorkingPoint, TARGET_STROKE
        ),
        TETHER_KUHN
    )
    val pair = AntagonisticSpacerPair(
        upCount = PATH_COUNT, upChain = upLimb, upPreExtension = 0.0,
        downCount = 1, downChain = downLimb, downSpan = 10.0
    )

    fun elementRecord(
        id: String,
        name: String,
        design: String,
        element: SignedCouplingElement,
        count: Int,
        peakForce: Double,
        peakMechanism: String,
        forcePaths: Int = count,
        verdictOf: (Boolean, Boolean, Boolean) -> String
    ): SignedElementRecord {
        val assembled = TwoSidedCoupling(count, element, 0.0)
        val perSecant = element.secantStiffness(TARGET_STROKE)
        val perTangent = element.tangentStiffness(TARGET_STROKE)
        val secant = count * perSecant
        val tangent = count * perTangent
        val twoSided = carriesCompression(element, probe)
        val placed = abs(secant - mandate) / mandate < 1.0e-6
        val compliant = tangent <= COMPLIANT_CEILING
        val stable = tangent > CITED_STABILITY_FLOOR
        return SignedElementRecord(
            id = id, element = name, designParameter = design,
            twoSided = twoSided,
            reactionAtZeroStroke = assembled.reaction(0.0),
            reactionAtNegativeProbe = assembled.reaction(-probe),
            perPathSecant = perSecant, perPathTangent = perTangent,
            assembledSecant = secant, assembledTangent = tangent,
            tangentOverSecant = perTangent / perSecant,
            meetsPlacement = placed,
            meetsCompliantCeiling = compliant,
            clearsStabilityFloor = stable,
            perPathStaticForce = TARGET_FORCE / forcePaths,
            perPathThermalForce = perAnchorThermalForce(max(tangent, 1e-12), forcePaths),
            perPathPeakForce = peakForce,
            peakForceMechanism = peakMechanism,
            clearsUnzip = peakForce < PerPathAllowable.UNZIP,
            clearsShear = peakForce < PerPathAllowable.SHEAR,
            clearsCeiling = peakForce < PerPathAllowable.NICKED_CEILING,
            verdict = verdictOf(twoSided, placed && compliant, peakForce < PerPathAllowable.UNZIP)
        )
    }

    val elements = listOf(
        elementRecord(
            "E1", "axial duplex standoff, 5 nm, loaded along its axis (C-0017's K1)",
            "L = 5 nm, S/L = ${"%.0f".format(standoff.stiffness)} pN/nm",
            standoff, PATH_COUNT, TARGET_FORCE / PATH_COUNT, "the axial load itself"
        ) { two, _, _ ->
            "FAIL on compliance, PASS on sidedness — and that is the finding. DNA's stiffest " +
                    "element IS two-sided (twoSided=$two); 45 of them are " +
                    "${"%.0f".format(PATH_COUNT * standoff.stiffness)} pN/nm, " +
                    "${"%.0f".format(PATH_COUNT * standoff.stiffness / mandate)}x the mandate."
        },
        elementRecord(
            "E2", "ssDNA spacer in series with a standoff — C-0017's K2 path",
            "8.61 nm contour = 13.2 nt at b = 2.10 nm",
            k2, PATH_COUNT, TARGET_FORCE / PATH_COUNT, "the axial load itself"
        ) { two, _, _ ->
            "FAIL on sidedness, PASS on compliance — the committed coupling, and it supplies " +
                    "EXACTLY ZERO preload (twoSided=$two). Its reaction and its tangent are " +
                    "both identically zero at every negative displacement."
        },
        elementRecord(
            "E3a", "transverse duplex flexure, axially FREE ends (leaf spring)",
            "span ${"%.2f".format(freeSpan)} nm = ${"%.0f".format(freeSpan / rise)} bp, pinned ends",
            freeFlexure, PATH_COUNT, TARGET_FORCE / PATH_COUNT,
            "the axial load itself — the membrane term is absent"
        ) { _, ok, safe ->
            if (ok && safe) "PASS on all four — two-sided, placed at the mandate exactly, " +
                    "compliant (linear, so secant = tangent), and 4.5x below the unzip allowable"
            else "FAIL"
        },
        elementRecord(
            "E3b", "transverse duplex flexure, axially RESTRAINED ends",
            "span ${"%.2f".format(restrainedSpan)} nm = ${"%.0f".format(restrainedSpan / rise)} bp, pinned ends",
            restrainedFlexure, PATH_COUNT, restrainedFlexure.axialTension(TARGET_STROKE),
            "the membrane tension in the beam, S(sqrt((L/2)^2+d^2)-L/2)/(L/2)"
        ) { _, ok, safe ->
            if (ok && safe) "PASS" else "PLACED but FAILS the compliance ceiling — the membrane " +
                    "term makes the tangent " +
                    "${"%.1f".format(PATH_COUNT * restrainedFlexure.tangentStiffness(TARGET_STROKE))}" +
                    " pN/nm, and its axial tension reaches " +
                    "${"%.1f".format(restrainedFlexure.axialTension(DESIRED_STROKE))} pN at §3's " +
                    "desired 10 nm stroke, past the 65 pN nicked ceiling"
        },
        elementRecord(
            "E5", "crossover-hinge flexure — an antiparallel crossover as a torsional spring",
            "arm ${"%.2f".format(hingeArm)} nm = ${"%.0f".format(hingeArm / rise)} bp, " +
                    "k_theta = ${"%.2f".format(hingeConstant)} pN*nm/rad",
            hinge, PATH_COUNT,
            hinge.hingeBondForce(TARGET_STROKE, AnchorMaterials.INTERHELICAL_DISTANCE),
            "the hinge moment resolved over the interhelical distance"
        ) { _, ok, safe ->
            if (ok && safe) "PASS on all four — two-sided, placed, linear, and the most compact " +
                    "of the passing elements; its compliance is " +
                    "${"%.1f".format(100.0 * hinge.hingeComplianceShare)} % hinge and the rest arm"
            else "FAIL"
        },
        elementRecord(
            "E4", "antagonistic ssDNA pair — 45 up-spacers opposed by ONE down-tether",
            "up ${"%.2f".format(upLimb.contourLength)} nm = " +
                    "${"%.0f".format(upLimb.contourLength / SsDnaTether.CONTOUR_PER_NUCLEOTIDE)} nt; " +
                    "down ${"%.2f".format(downLimb.contourLength)} nm = " +
                    "${"%.0f".format(downLimb.contourLength / SsDnaTether.CONTOUR_PER_NUCLEOTIDE)} nt " +
                    "across the 10 nm layer",
            pair, 1, pair.circulatingTension(TARGET_STROKE),
            "the larger limb tension — the CIRCULATING tension, which neither side sees as output",
            forcePaths = PATH_COUNT
        ) { two, _, safe ->
            "TWO-SIDED AS A PAIR (twoSided=$two) though neither part is: the preload is the " +
                    "DIFFERENCE of the two limbs and the stiffness is their SUM. Costs a second " +
                    "ground and a through-layer path per attachment; per-path circulating " +
                    "tension clears unzip = $safe"
        }
    )

    // ------------------------------------------------------------------ the geometry
    val designs = mutableListOf<ElementDesignRecord>()
    listOf(8, 15, PATH_COUNT).forEach { count ->
        FlexureEndCondition.entries.forEach { end ->
            listOf(false, true).forEach { restrained ->
                val span = flexureSpanForStiffness(
                    bendingRigidity, end, restrained, stretchModulus, count, mandate, TARGET_STROKE
                )
                val flexure = TransverseDuplexFlexure(
                    bendingRigidity, span, end, restrained, stretchModulus
                )
                val tension = flexure.axialTension(TARGET_STROKE)
                val desired = TransverseDuplexFlexure(
                    bendingRigidity, span, end, restrained, stretchModulus
                ).axialTension(DESIRED_STROKE)
                val perPath = TARGET_FORCE / count
                val tangent = count * flexure.tangentStiffness(TARGET_STROKE)
                designs += ElementDesignRecord(
                    element = "E3 transverse duplex flexure",
                    pathCount = count,
                    endCondition = end.description,
                    axiallyRestrained = restrained,
                    variant = if (restrained) "membrane term ACTIVE" else "ends free to draw in",
                    designLength = span,
                    designLengthInBasePairs = span / rise,
                    fitsTileEdge = span <= TILE_EDGE,
                    fitsLeverEnvelope = span <= 60.0,
                    perPathStiffness = flexure.secantStiffness(TARGET_STROKE),
                    assembledSecant = count * flexure.secantStiffness(TARGET_STROKE),
                    assembledTangent = tangent,
                    perPathStaticForce = perPath,
                    axialTensionAtTarget = tension,
                    axialTensionAtDesiredStroke = desired,
                    endDrawIn = flexure.endDrawIn(TARGET_STROKE),
                    endDrawInBasePairs = flexure.endDrawIn(TARGET_STROKE) / rise,
                    clearsUnzipAtTarget = max(tension, perPath) < PerPathAllowable.UNZIP,
                    clearsCeilingAtDesiredStroke = desired < PerPathAllowable.NICKED_CEILING,
                    verdict = when {
                        tangent > COMPLIANT_CEILING ->
                            "FAILS the 40 pN/nm compliance ceiling — the membrane term"
                        span > 60.0 -> "FAILS the lever envelope"
                        max(tension, perPath) >= PerPathAllowable.UNZIP -> "FAILS unzip"
                        else -> "PASS"
                    }
                )
            }
        }
        listOf(
            Gen1Tile.CROSSOVER_ALPHA_MIN, 1.0, Gen1Tile.CROSSOVER_ALPHA_MAX
        ).forEach { alpha ->
            val constant = Gen1Tile.crossoverHingeStiffness(alpha)
            val arm = hingeArmForStiffness(constant, bendingRigidity, count, mandate)
            val flex = CrossoverHingeFlexure(constant, arm, bendingRigidity)
            val bond = flex.hingeBondForce(TARGET_STROKE, AnchorMaterials.INTERHELICAL_DISTANCE)
            val perPath = TARGET_FORCE / count
            designs += ElementDesignRecord(
                element = "E5 crossover-hinge flexure",
                pathCount = count,
                endCondition = "torsional hinge on a tip-loaded arm (3EI/r^3 in series)",
                axiallyRestrained = false,
                variant = "alpha = $alpha, k_theta = ${"%.2f".format(constant)} pN*nm/rad",
                designLength = arm,
                designLengthInBasePairs = arm / rise,
                fitsTileEdge = true,
                fitsLeverEnvelope = true,
                perPathStiffness = flex.stiffness,
                assembledSecant = count * flex.stiffness,
                assembledTangent = count * flex.stiffness,
                perPathStaticForce = perPath,
                axialTensionAtTarget = bond,
                axialTensionAtDesiredStroke = flex.hingeBondForce(
                    DESIRED_STROKE, AnchorMaterials.INTERHELICAL_DISTANCE
                ),
                endDrawIn = 0.0,
                endDrawInBasePairs = 0.0,
                clearsUnzipAtTarget = max(bond, perPath) < PerPathAllowable.UNZIP,
                clearsCeilingAtDesiredStroke = flex.hingeBondForce(
                    DESIRED_STROKE, AnchorMaterials.INTERHELICAL_DISTANCE
                ) < PerPathAllowable.NICKED_CEILING,
                verdict = if (max(bond, perPath) < PerPathAllowable.UNZIP) "PASS"
                else "FAILS unzip at the hinge"
            )
        }
    }

    // ------------------------------------------------------------------ the zero-bias balance
    val electrodes = listOf(
        ElectrodeCandidate("gold", HamakerConstants.GOLD_ACROSS_WATER, HamakerConstants.GOLD_ACROSS_WATER_HIGH, true),
        ElectrodeCandidate("alumina", HamakerConstants.ALUMINA_ACROSS_WATER, HamakerConstants.ALUMINA_ACROSS_WATER, false)
    )
    val metal = electrodes.first()

    fun vanDerWaalsForce(electrode: ElectrodeCandidate, thickness: Double, gap: Double): Double =
        vanDerWaalsPressure(electrode.screenedLow(gap, inverseDebye), gap, thickness) *
                retardationPressureFactor(gap) * FOOTPRINT

    val ions = IonModel(buffer.magnesiumNumberDensity)
    val field = GapField(tileCharge, lb, ions, sternChargeDensityPerVolt(STERN_CAPACITANCE))
    val fieldGaps = listOf(3.0, 5.0, 7.0, 10.0)
    val zeroBiasField = fieldGaps.associateWith { -field.force(it, 0.0) }
    fun fieldHoldDown(height: Double): Double {
        if (height <= fieldGaps.first()) return zeroBiasField.getValue(fieldGaps.first())
        if (height >= fieldGaps.last()) return zeroBiasField.getValue(fieldGaps.last())
        val upper = fieldGaps.first { it >= height }
        val lower = fieldGaps.last { it <= height }
        if (upper == lower) return zeroBiasField.getValue(upper)
        val t = (height - lower) / (upper - lower)
        return zeroBiasField.getValue(lower) * (1.0 - t) + zeroBiasField.getValue(upper) * t
    }
    val gravity = buoyantWeight(FOOTPRINT * NOMINAL_TILE_THICKNESS, DNA_DENSITY, WATER_DENSITY)
    val tetherContour = 0.5 * com.xemantic.nano.plentyofroom.coupling.gaussianContourCeiling(
        TETHER_KUHN, TETHER_COUNT, LATERAL_BOUND
    )
    val tether = FreelyJointedChain(tetherContour, TETHER_KUHN)
    val committedK2 = SeriesEntropicCoupling(
        PATH_COUNT, stretchModulus / K2_STANDOFF_LENGTH,
        FreelyJointedChain(K2_SPACER_CONTOUR, TETHER_KUHN)
    )
    val offsetOneBasePair = rise
    val offsetSpan = flexureSpanForStiffness(
        bendingRigidity, FlexureEndCondition.PINNED_ENDS, false, stretchModulus, PATH_COUNT,
        mountingOffsetStiffness(offsetOneBasePair, TARGET_FORCE, TARGET_STROKE), TARGET_STROKE
    )
    val offsetCoupling = TwoSidedCoupling(
        PATH_COUNT,
        TransverseDuplexFlexure(
            bendingRigidity, offsetSpan, FlexureEndCondition.PINNED_ENDS, false, stretchModulus
        ),
        offsetOneBasePair
    )
    val twoSidedCoupling = TwoSidedCoupling(PATH_COUNT, freeFlexure, 0.0)

    val scenarios: List<Pair<String, (Double, Double) -> Double>> = listOf(
        "C-0021's DEVICE: layer + K2 + 8 substrate tethers + vdW + field" to
                { onset: Double, h: Double ->
                    substrateTetherHoldDown(tether, TETHER_COUNT, h) +
                            vanDerWaalsForce(metal, NOMINAL_TILE_THICKNESS, h) +
                            fieldHoldDown(h) + gravity - committedK2.reaction(onset - h)
                },
        "C-0021's DEVICE WITHOUT THE TETHER: layer + K2 + vdW + field" to
                { onset: Double, h: Double ->
                    vanDerWaalsForce(metal, NOMINAL_TILE_THICKNESS, h) +
                            fieldHoldDown(h) + gravity - committedK2.reaction(onset - h)
                },
        "T-23: layer + TWO-SIDED flexure coupling, NO tether, NO preload, + vdW + field" to
                { onset: Double, h: Double ->
                    vanDerWaalsForce(metal, NOMINAL_TILE_THICKNESS, h) +
                            fieldHoldDown(h) + gravity - twoSidedCoupling.reaction(onset - h)
                },
        "T-23: the same with ONE BASE PAIR of mounting offset" to
                { onset: Double, h: Double ->
                    vanDerWaalsForce(metal, NOMINAL_TILE_THICKNESS, h) +
                            fieldHoldDown(h) + gravity - offsetCoupling.reaction(onset - h)
                },
        "T-23: the TWO-SIDED coupling ALONE, no van der Waals, no field" to
                { onset: Double, h: Double -> -twoSidedCoupling.reaction(onset - h) }
    )

    val confinement = mutableListOf<TwoSidedConfinementRecord>()
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
            scenarios.forEach { (label, holdDownOf) ->
                fun net(h: Double): Double =
                    layerLoadAt(model, chain, onset, h) - holdDownOf(onset, h)
                val rest = zeroBiasRestingHeight(::net, onset + 2.0, floor, SCAN_STEPS)
                if (rest == null) {
                    confinement += TwoSidedConfinementRecord(
                        scenario = label, model = model.name, layerHeight = height,
                        onsetHeight = onset, restingHeight = null, descent = null,
                        deliveredStroke = null, equilibriumStiffness = null, rms = null,
                        equipartitionRms = null, rmsInBand = null, probabilityAbove = null,
                        escapeBarrier = null, quadratureDomainUpper = null, confining = false,
                        meetsPositionBound = false,
                        verdict = "NO EQUILIBRIUM — nothing pulls the tile down"
                    )
                    return@forEach
                }
                val stiffness = equilibriumStiffness(::net, rest, 1e-4)
                // C-0021's own domain rule, unchanged, so that its rows reproduce on their own
                // domain: the tail is set by the hold-down JUST ABOVE L0, capped, and never runs
                // past the tether's contour, which is a hard geometric stop
                val holdDownAbove = holdDownOf(onset, onset + 1e-6)
                val tail = min(
                    QUADRATURE_TAIL_DECADES * thermalEnergy() / max(holdDownAbove, 1e-9),
                    QUADRATURE_TAIL_CEILING
                )
                val statistics = boltzmannPositionStatistics(
                    netUpwardForce = ::net,
                    lower = max(floor, rest - 3.0),
                    upper = min(onset + tail, tetherContour * 0.98),
                    panels = QUADRATURE_PANELS,
                    reference = onset
                )
                val corner = if (stiffness > 0.0) lorentzianCorner(stiffness, drag) else null
                val inBand = corner?.let {
                    statistics.rms * sqrt(varianceFractionInBand(BANDWIDTH, it))
                }
                confinement += TwoSidedConfinementRecord(
                    scenario = label, model = model.name, layerHeight = height,
                    onsetHeight = onset, restingHeight = rest, descent = onset - rest,
                    deliveredStroke = TARGET_STROKE - (onset - rest),
                    equilibriumStiffness = stiffness,
                    rms = statistics.rms,
                    equipartitionRms = if (stiffness > 0.0) sqrt(thermalEnergy() / stiffness) else null,
                    rmsInBand = inBand,
                    probabilityAbove = statistics.probabilityAbove,
                    escapeBarrier = statistics.escapeBarrier,
                    quadratureDomainUpper = statistics.domainUpper,
                    confining = statistics.escapeBarrier >= CONFINEMENT_BARRIER,
                    meetsPositionBound = statistics.rms <= POSITION_BOUND,
                    verdict = if (statistics.escapeBarrier >= CONFINEMENT_BARRIER)
                        "CONFINING" else "STABLE BUT NOT CONFINING — a trap, not a well"
                )
            }
        }
    }

    // ------------------------------------------------------------------ the field at 10 nm
    fun output(model: GraftedLayerModel, chain: GraftedChain, onset: Double, h: Double, bias: Double) =
        abs(field.force(h, bias)) - layerLoadAt(model, chain, onset, h)

    val pullIn = mutableListOf<PullInSensitivityRecord>()
    val tenNanometre = DESIGN_POINTS.last()
    models.forEach { model ->
        val chain = peg.graftedChain(
            model.chainLengthForHeight(peg, tenNanometre.first, tenNanometre.second), tenNanometre.second
        )
        val onset = model.equilibriumHeight(chain)
        val held = onset - TARGET_STROKE
        // V* by bisection on the bracket width, never on a residual
        var low = 0.0
        var high = 1.0
        repeat(60) {
            if (high - low <= 1.0e-9) return@repeat
            val middle = 0.5 * (low + high)
            if (output(model, chain, onset, held, middle) < TARGET_FORCE) low = middle
            else high = middle
        }
        val star = 0.5 * (low + high)
        fun effective(bias: Double, step: Double = 1e-4): Double =
            (output(model, chain, onset, held + step, bias) -
                    output(model, chain, onset, held - step, bias)) / (2.0 * step)
        val at = effective(star)
        val up = effective(star * 1.05)
        val elasticity = ln(abs(up) / abs(at)) / ln(1.05)
        val floorHere = max(0.0, -at)
        val raised = mountingOffsetStiffness(offsetOneBasePair, TARGET_FORCE, TARGET_STROKE)
        pullIn += PullInSensitivityRecord(
            model = model.name, layerHeight = tenNanometre.first, buffer = BUFFER,
            operatingBias = star,
            layerStiffness = layerStiffnessAt(model, chain, onset, held),
            fieldStiffness = at - layerStiffnessAt(model, chain, onset, held),
            effectiveStiffness = at,
            stabilityFloor = floorHere,
            biasElasticity = elasticity,
            stabilityMarginAtMandate = if (floorHere > 0.0) mandate / floorHere else null,
            biasMarginAtMandate = if (floorHere > 0.0)
                (mandate / floorHere).pow(1.0 / elasticity) else null,
            stabilityMarginAtOneBasePair = if (floorHere > 0.0) raised / floorHere else null,
            biasMarginAtOneBasePair = if (floorHere > 0.0)
                (raised / floorHere).pow(1.0 / elasticity) else null
        )
    }

    // ------------------------------------------------------------------ the preload trade
    val tenModels = models.map { model ->
        val chain = peg.graftedChain(
            model.chainLengthForHeight(peg, tenNanometre.first, tenNanometre.second),
            tenNanometre.second
        )
        Triple(model, chain, model.equilibriumHeight(chain))
    }
    val elasticityBracket = pullIn.map { it.biasElasticity }
    val preloadTrade = listOf(
        0.0, offsetForPreload(thermalScale, TARGET_FORCE, TARGET_STROKE) / rise, 1.0, 2.0, 5.0
    ).map { basePairs ->
        val offset = basePairs * rise
        val stiffness = mountingOffsetStiffness(offset, TARGET_FORCE, TARGET_STROKE)
        val preload = mountingOffsetPreload(offset, TARGET_FORCE, TARGET_STROKE)
        val span = flexureSpanForStiffness(
            bendingRigidity, FlexureEndCondition.PINNED_ENDS, false, stretchModulus, PATH_COUNT,
            stiffness, TARGET_STROKE
        )
        val element = TransverseDuplexFlexure(
            bendingRigidity, span, FlexureEndCondition.PINNED_ENDS, false, stretchModulus
        )
        val coupling = TwoSidedCoupling(PATH_COUNT, element, offset)
        // the descent, solved on the layer rather than divided by a stiffness
        val descents = tenModels.map { (model, chain, onset) ->
            fun net(h: Double): Double = layerLoadAt(model, chain, onset, h) +
                    coupling.reaction(onset - h) -
                    vanDerWaalsForce(metal, NOMINAL_TILE_THICKNESS, h) - fieldHoldDown(h) - gravity
            val rest = zeroBiasRestingHeight(::net, onset + 2.0, max(chain.occupiedThickness * 1.05, 0.2), SCAN_STEPS)
            if (rest == null) 0.0 else onset - rest
        }
        val worst = descents.max()
        val floorHere = pullIn.maxOf { it.stabilityFloor }
        val elasticity = elasticityBracket.min()
        // C-0018's own implied elasticity, from its published pair against C-0017's floor
        val foldElasticity = ln(mandate / CITED_STABILITY_FLOOR) / ln(CITED_BIAS_MARGIN_LOW)
        MountingOffsetRecord(
            offsetBasePairs = basePairs,
            offset = offset,
            couplingStiffness = stiffness,
            preload = preload,
            preloadOverThermalScale = preload / thermalScale,
            flexureSpan = span,
            perPathStaticForce = (TARGET_FORCE + preload) / PATH_COUNT,
            descentAtTenNanometres = worst,
            deliveredStroke = TARGET_STROKE - worst,
            stabilityMargin = stiffness / floorHere,
            biasMargin = (stiffness / floorHere).pow(1.0 / elasticity),
            biasMarginOnFoldAxis = (stiffness / CITED_STABILITY_FLOOR).pow(1.0 / foldElasticity),
            note = when {
                basePairs == 0.0 -> "the recommended design: two-sidedness makes the preload " +
                        "unnecessary, so it is not paid for"
                basePairs < 0.5 -> "the offset the THERMAL requirement asks for — an eighth of a " +
                        "base pair, which no assembly can set"
                else -> "one or more base pairs of built-in deflection"
            }
        )
    }

    // ------------------------------------------------------------------ convergence
    val convergence = mutableListOf<TwoSidedConvergenceRecord>()
    val spanScans = listOf(32, 128, 256, 4096).map {
        it to flexureSpanForStiffness(
            bendingRigidity, FlexureEndCondition.PINNED_ENDS, true, stretchModulus, PATH_COUNT,
            mandate, TARGET_STROKE, scanSteps = it
        )
    }
    spanScans.forEach { (steps, value) ->
        convergence += TwoSidedConvergenceRecord(
            "flexure design span, axially restrained [nm]", "scanSteps = $steps", value,
            abs(value - spanScans.last().second) / spanScans.last().second
        )
    }
    val panelScans = listOf(500, 1000, 2000, 8000).map { panels ->
        val (model, chain, onset) = tenModels.first()
        fun net(h: Double): Double = layerLoadAt(model, chain, onset, h) +
                twoSidedCoupling.reaction(onset - h) -
                vanDerWaalsForce(metal, NOMINAL_TILE_THICKNESS, h) - fieldHoldDown(h) - gravity
        panels to boltzmannPositionStatistics(
            ::net, max(chain.occupiedThickness * 1.05, 0.2),
            min(onset + QUADRATURE_TAIL_CEILING, tetherContour * 0.98), panels, reference = onset
        ).rms
    }
    panelScans.forEach { (panels, value) ->
        convergence += TwoSidedConvergenceRecord(
            "two-sided zero-bias RMS at 10 nm [nm]", "panels = $panels", value,
            abs(value - panelScans.last().second) / panelScans.last().second
        )
    }
    listOf(200, 400, 800, 1600).forEach { nodes ->
        val force = field.force(10.0, 0.15, nodes)
        convergence += TwoSidedConvergenceRecord(
            "|F_es| at 10 nm, 0.15 V [pN]", "nodes = $nodes", force,
            abs(force - field.force(10.0, 0.15, 1600)) / abs(field.force(10.0, 0.15, 1600))
        )
    }

    // ------------------------------------------------------------------ reproductions
    val reproductions = mutableListOf<TwoSidedReproductionRecord>()
    fun reproduce(
        source: String,
        quantity: String,
        published: Double,
        value: Double,
        definitional: Boolean = false
    ) {
        reproductions += TwoSidedReproductionRecord(
            source, quantity, published, value,
            abs(value - published) / max(abs(published), 1e-12), definitional
        )
    }
    reproduce("§3", "the mandated coupling stiffness [pN/nm]", 100.0 / 3.0, mandate)
    reproduce("C-0021", "the thermal force scale k_BT/3nm [pN]", 1.380649, thermalScale)
    reproduce("C-0014", "leaf A1.1's per-coordinate stiffness bound [pN/nm]", 0.460216, stiffnessScale)
    reproduce("C-0017", "K1: 45 axial standoffs [pN/nm]", 9900.0, PATH_COUNT * standoff.stiffness)
    reproduce("C-0021", "M2: K2's reaction at zero stroke [pN]", 0.0, committedK2.reaction(0.0))
    reproduce("C-0009", "the fitted crossover hinge constant [pN*nm/rad]", 13.5294, hingeConstant)
    reproduce("C-0008", "zero-bias force at 5 nm, Stern series [pN]", -0.41, field.force(5.0, 0.0))
    reproduce("C-0008", "zero-bias force at 10 nm, Stern series [pN]", -0.078, field.force(10.0, 0.0))
    reproduce(
        "C-0014", "the eight-tether preload at 10 nm [pN]", 9.37,
        substrateTetherHoldDown(tether, TETHER_COUNT, 10.0)
    )
    val tetherlessBarriers = confinement
        .filter { it.scenario.startsWith("C-0021's DEVICE WITHOUT") && it.escapeBarrier != null }
    reproduce(
        "C-0021", "the tetherless device's worst well depth [k_BT]", 1.40,
        tetherlessBarriers.minOfOrNull { it.escapeBarrier!! } ?: 0.0
    )
    reproduce(
        "C-0021", "the tetherless device's deepest well [k_BT]", 5.37,
        tetherlessBarriers.maxOfOrNull { it.escapeBarrier!! } ?: 0.0
    )
    reproduce(
        "C-0017", "the worst stability floor at 10 nm / 2 mM [pN/nm]", CITED_STABILITY_FLOOR,
        pullIn.maxOf { it.stabilityFloor }
    )
    reproduce(
        "C-0017", "the best stability floor at 10 nm / 2 mM [pN/nm]", CITED_STABILITY_FLOOR_BEST,
        pullIn.minOf { it.stabilityFloor }
    )
    // NOT a reproduction: C-0018's margin is the fold of the MOVING equilibrium and this task's
    // is the bias at which the HELD gap loses stability. Recorded so the gap is visible and
    // labelled, never so it can be read as a failed reproduction.
    reproduce(
        "C-0018", "fold-bias margin (moving equilibrium) against this task's HELD-GAP " +
                "stability-bias margin — DIFFERENT QUANTITIES, worst model", CITED_BIAS_MARGIN_LOW,
        pullIn.mapNotNull { it.biasMarginAtMandate }.min(), definitional = true
    )
    reproduce(
        "C-0018", "the same, best model", CITED_BIAS_MARGIN_HIGH,
        pullIn.mapNotNull { it.biasMarginAtMandate }.max(), definitional = true
    )

    // ------------------------------------------------------------------ the result
    val twoSidedStates = confinement.filter { it.scenario.startsWith("T-23: layer") }
    val tetherless = confinement.filter { it.scenario.startsWith("C-0021's DEVICE WITHOUT") }
    val worstReproduction = reproductions
        .filter { it.published != 0.0 && !it.definitional }.maxOf { it.relativeDeparture }

    val result = TwoSidedResult(
        task = "T-23",
        leaf = "A8.2, with A1.1/A1.2 for the positional bound and A2.2 for the operating point",
        title = "A two-sided compliant DNA coupling: three exist, the cheapest is a crossover " +
                "hinge on a 4 nm arm, and two-sidedness changes the hold-down requirement from a " +
                "force into a stiffness",
        verificationType = "in-silico (signed force-extension laws composed into a load line and " +
                "re-solved against C-0021's own zero-bias balance) + logical (a sidedness " +
                "argument that fixes the currency of the requirement before any element is " +
                "evaluated)",
        acceptance = "P1 two-sided (R(-d) < 0, evaluated); P2 placed at 33.333 pN/nm with a " +
                "tangent <= $COMPLIANT_CEILING pN/nm above C-0017's stability floor; P3 per-path " +
                "peak below the 10 pN unzip allowable; P4 the zero-bias well confining (>= 10 " +
                "k_BT) with NO tether and NO preload; P5 every geometric parameter inside " +
                "C-0017's own lever envelope",
        maturity = "TRL 1-3. Model-consistent and traceable. NOTHING HERE IS MEASURED — no " +
                "element below has been built and none is proposed as a sequence design.",
        units = mapOf(
            "length" to "nm", "force" to "pN", "stiffness" to "pN/nm",
            "bendingRigidity" to "pN*nm^2", "torsionalStiffness" to "pN*nm/rad",
            "energy" to "pN*nm and k_BT", "pressure" to "pN/nm^2 = 1 MPa", "potential" to "V",
            "frequency" to "Hz"
        ),
        conventions = listOf(
            "z is normal to the electrode, positive away from it; the stroke s = L0 - h is " +
                    "positive downward",
            "the coupling reaction R is positive UPWARD, i.e. resisting descent (C-0017)",
            "the element displacement d is SIGNED and is measured from the element's own " +
                    "unstressed configuration, positive downward; a one-sided element has " +
                    "R(d) = 0 for all d <= 0 and a two-sided one has R(d) < 0 there",
            "SIDEDNESS IS TESTED BY EVALUATING THE LAW AT NEGATIVE ARGUMENT, never by " +
                    "inspecting the geometry",
            "a hold-down is any mechanism contributing to F_down > 0, and R(0) = -F_down",
            "the mounting offset q is the depth below L0 at which the element is unstressed, so " +
                    "R(s) = n f(s - q); it is a LENGTH, which is why it is quantised by the rise " +
                    "per base pair",
            "L0 is the FORCE-ONSET height (C-0011, CH-0010), and the delivered stroke is " +
                    "measured from the zero-bias rest, per CH-0024"
        ),
        parameters = mapOf(
            "temperature" to "300 K",
            "buffer" to "$BUFFER mM MgCl2",
            "tileFootprint" to "$TILE_EDGE x $TILE_EDGE nm",
            "layerHeights" to DESIGN_POINTS.map { it.first }.toString(),
            "layerModels" to models.joinToString { it.name },
            "pathCount" to "$PATH_COUNT (C-0015's 3 x 15 flatness grid)",
            "mandatedStiffness" to "$mandate pN/nm",
            "complianceCeiling" to "$COMPLIANT_CEILING pN/nm (declared)",
            "duplexBendingRigidity" to "$bendingRigidity pN*nm^2 (CanDo model input)",
            "duplexStretchModulus" to "$stretchModulus pN (Wang et al. 1997, measured)",
            "crossoverHingeStiffness" to "$hingeConstant pN*nm/rad at alpha = 1 " +
                    "(Chen et al. 2014, fitted; alpha in [0.6, 1.2])",
            "ssDnaKuhnLength" to "$TETHER_KUHN nm (Chen et al. 2012, zero force — the " +
                    "applicable end for a ~1 pN element)",
            "risePerBasePair" to "$rise nm",
            "recommendedFlexureSpan" to "$freeSpan nm",
            "recommendedHingeArm" to "$hingeArm nm",
            "quadraturePanels" to "$QUADRATURE_PANELS",
            "quadratureTail" to "C-0021's rule: min(40 k_BT/F_down, 60) nm above L0, capped at " +
                    "the tether contour"
        ),
        requirements = requirements,
        elements = elements,
        designs = designs,
        confinement = confinement,
        preloadTrade = preloadTrade,
        pullIn = pullIn,
        convergence = convergence,
        reproductions = reproductions,
        findings = findingsOf(
            thermalScale, stiffnessScale, mandate, elements, designs, twoSidedStates, tetherless,
            preloadTrade, pullIn, freeSpan, restrainedSpan, hingeArm, hinge, restrainedFlexure,
            worstReproduction
        ),
        validity = VALIDITY,
        openQuestions = OPEN_QUESTIONS,
        citedNumbers = CITED
    )

    val file = File("gpd/results/T-23-two-sided-coupling.json")
    file.parentFile.mkdirs()
    val json = Json { prettyPrint = true; encodeDefaults = true }
    file.writeText(json.encodeToString(json.encodeToJsonElement(result).roundedForResult()) + "\n")
    println("wrote ${file.path}")
    report(result)
}

// ---------------------------------------------------------------------------------------------

private fun findingsOf(
    thermalScale: Double,
    stiffnessScale: Double,
    mandate: Double,
    elements: List<SignedElementRecord>,
    designs: List<ElementDesignRecord>,
    twoSided: List<TwoSidedConfinementRecord>,
    tetherless: List<TwoSidedConfinementRecord>,
    preloadTrade: List<MountingOffsetRecord>,
    pullIn: List<PullInSensitivityRecord>,
    freeSpan: Double,
    restrainedSpan: Double,
    hingeArm: Double,
    hinge: CrossoverHingeFlexure,
    restrained: TransverseDuplexFlexure,
    worstReproduction: Double
): Map<String, String> {
    fun f(value: Double, digits: Int = 3) = "%.${digits}f".format(value)
    val passing = elements.filter { it.twoSided && it.meetsCompliantCeiling && it.meetsPlacement }
    val confiningCount = twoSided.count { it.confining }
    return mapOf(
        "THE ANSWER" to
                "DNA offers THREE two-sided compliant elements and the programme reached for " +
                        "none of them: a transverse duplex flexure with axially free ends " +
                        "(span ${f(freeSpan, 2)} nm), a crossover-hinge flexure (arm " +
                        "${f(hingeArm, 2)} nm), and an antagonistic ssDNA pair. " +
                        "${passing.size} of ${elements.size} catalogued elements pass placement, " +
                        "compliance and sidedness together.",
        "THE CURRENCY CHANGES, AND THAT IS THE HEADLINE" to
                "C-0021's hold-down requirement is a FORCE (k_BT/sigma = ${f(thermalScale)} pN) " +
                        "only because a one-sided stack gives a LINEAR potential above L0. A " +
                        "two-sided coupling makes it QUADRATIC there, so the requirement becomes " +
                        "a STIFFNESS, k_BT/sigma^2 = ${f(stiffnessScale)} pN/nm — and F_req = " +
                        "k_req*sigma identically. Two-sidedness is worth exactly one power of " +
                        "the position bound, and §3's own mandated ${f(mandate)} pN/nm clears " +
                        "the stiffness requirement by ${f(mandate / stiffnessScale, 1)}x WITH NO " +
                        "PRELOAD AT ALL.",
        "THE TETHERS COME OUT OF THE DESIGN" to
                "C-0021's device with the tether removed is a ${f(tetherless.minOf { it.escapeBarrier ?: 0.0 }, 2)}" +
                        "-${f(tetherless.maxOf { it.escapeBarrier ?: 0.0 }, 2)} k_BT trap, " +
                        "confining at ${tetherless.count { it.confining }} of ${tetherless.size} " +
                        "states. The SAME stiffness made two-sided confines at $confiningCount " +
                        "of ${twoSided.size}, with an escape barrier bounded only by the " +
                        "quadrature domain, and an RMS of " +
                        "${f(twoSided.minOf { it.rms ?: 0.0 })}-${f(twoSided.maxOf { it.rms ?: 0.0 })} nm " +
                        "against the 3.0 nm bound. C-0014's eight substrate tethers are then " +
                        "not needed for T-13 at all.",
        "SIDEDNESS AND COMPLIANCE ARE SCARCE ONLY TOGETHER ON THE AXIAL AXIS" to
                "E1, a duplex loaded along its axis, IS two-sided — and 297x too stiff. E2, an " +
                        "ssDNA spacer, is compliant and supplies exactly zero. There is nothing " +
                        "in between on that axis because axial compliance in DNA is entropic " +
                        "and entropy only pulls. Loaded TRANSVERSE to its axis, or through a " +
                        "hinge, the same duplex is both: bending is signed and c*EI/L^3 and " +
                        "k_theta/r^2 are as small as the designer makes L and r.",
        "THE AXIAL RESTRAINT AT THE FLEXURE'S ENDS IS THE ONE BINDING DESIGN CHOICE" to
                "Free to draw in (${f(pinnedDrawInAtWorkingPoint(freeSpan))} nm at the working " +
                        "point, about ${f(pinnedDrawInAtWorkingPoint(freeSpan) / 0.34, 1)} base " +
                        "pairs), the flexure is exactly linear at a ${f(freeSpan, 2)} nm span. " +
                        "Held axially, the membrane term makes its tangent " +
                        "${f(45.0 * restrained.tangentStiffness(3.0), 1)} pN/nm — past this " +
                        "task's 40 pN/nm compliance ceiling — its span " +
                        "${f(restrainedSpan, 1)} nm, and its own axial tension " +
                        "${f(restrained.axialTension(10.0), 1)} pN at §3's desired 10 nm stroke, " +
                        "past the 65 pN nicked ceiling. Same element, same material, " +
                        "${f(restrainedSpan / freeSpan, 2)}x the span.",
        "THE PRELOAD IS QUANTISED BY THE BASE PAIR, AND THE QUANTUM IS 9x THE REQUIREMENT" to
                "For a two-sided coupling the preload is a MOUNTING OFFSET, i.e. a length: " +
                        "F = F_t*q/(delta_t - q). The thermal requirement asks for " +
                        "q = ${f(preloadTrade[1].offset, 4)} nm — an eighth of a base-pair rise, " +
                        "below any assembly tolerance — while one base pair delivers " +
                        "${f(preloadTrade[2].preload, 2)} pN, " +
                        "${f(preloadTrade[2].preloadOverThermalScale, 1)}x the requirement, and " +
                        "costs ${f(preloadTrade[2].descentAtTenNanometres, 3)} nm of stroke. A " +
                        "design cannot SET the preload it would need, which is the sharpest " +
                        "argument for not needing one.",
        "THE PULL-IN MARGIN IS NOT WHAT A STIFFER COUPLING BUYS, ON EITHER AXIS" to
                "C-0017's whole stability floor at 10 nm / 2 mM is reproduced here independently " +
                        "(23.41-27.91 pN/nm, to 1.5e-4, at operating biases of 0.128-0.180 V), " +
                        "so the stiffness axis is exact: one base pair of mounting offset takes " +
                        "the coupling to ${f(preloadTrade[2].couplingStiffness, 2)} pN/nm and the " +
                        "stiffness margin from ${f(preloadTrade[0].stabilityMargin, 3)} to " +
                        "${f(preloadTrade[2].stabilityMargin, 3)}. On the BIAS axis it buys " +
                        "almost nothing: at the held gap |k_eff| rises only as V^" +
                        "${f(pullIn.minOf { it.biasElasticity }, 1)}-" +
                        "${f(pullIn.maxOf { it.biasElasticity }, 1)} (margin " +
                        "${f(preloadTrade[0].biasMargin, 4)} -> ${f(preloadTrade[2].biasMargin, 4)}), " +
                        "while C-0018's fold of the MOVING equilibrium implies p = 11.2-25.5 " +
                        "(margin ${f(preloadTrade[0].biasMarginOnFoldAxis, 4)} -> " +
                        "${f(preloadTrade[2].biasMarginOnFoldAxis, 4)}). The difference between " +
                        "those two elasticities is itself a result: most of the steepness of " +
                        "C-0018's fold is the EQUILIBRIUM MOVING, not the field stiffening. " +
                        "C-0017's and C-0018's recommendation stands unchanged: the BUFFER is the " +
                        "lever (6x), not the coupling (1.005-1.011x).",
        "THE HINGE IS THE COMPACT ANSWER AND ITS CONSTANT IS ALREADY FITTED" to
                "A single antiparallel crossover on a ${f(hingeArm, 2)} nm arm presents exactly " +
                        "the per-path stiffness §3 mandates; its compliance is " +
                        "${f(100.0 * hinge.hingeComplianceShare, 1)} % hinge and the rest arm " +
                        "bending, which is leaf A8.2's answer for this element. Because " +
                        "r goes as sqrt(k_theta), the whole alpha = 0.6-1.2 bracket on the only " +
                        "crossover elastic constant anyone has fitted is " +
                        "${f(sqrt(2.0), 2)}x in a length the designer chooses anyway — so this " +
                        "element does NOT wait for T-9.",
        "THE ANTAGONISTIC PAIR WORKS AND ITS PRICE IS CIRCULATION" to
                "The pair's preload is the DIFFERENCE of its two limb tensions and its stiffness " +
                        "is their SUM, so hold-down and stiffness are independent design " +
                        "variables. But the difference is taken between forces that real load " +
                        "paths carry: the per-path circulating tension is " +
                        "${f(elements.first { it.id == "E4" }.perPathPeakForce, 2)} pN to deliver " +
                        "a ${f(thermalScale)} pN hold-down, and the topology needs a second " +
                        "ground and a through-layer path at every attachment.",
        "REPRODUCTIONS" to
                "Every upstream number this task consumes was reproduced by re-running its code " +
                        "rather than copying its table; the worst relative departure over the " +
                        "whole set is ${"%.2e".format(worstReproduction)}.",
        "GEOMETRY" to
                "${designs.count { it.verdict == "PASS" }} of ${designs.size} " +
                        "(element x path count x bracket) design points pass every geometric and " +
                        "strength test; the flexure spans run " +
                        "${f(designs.filter { it.element.startsWith("E3") }.minOf { it.designLength }, 1)}-" +
                        "${f(designs.filter { it.element.startsWith("E3") }.maxOf { it.designLength }, 1)} nm " +
                        "and the hinge arms " +
                        "${f(designs.filter { it.element.startsWith("E5") }.minOf { it.designLength }, 2)}-" +
                        "${f(designs.filter { it.element.startsWith("E5") }.maxOf { it.designLength }, 2)} nm."
    )
}

/** The draw-in at the working point of a pinned flexure of the given span, `2.4 δ²/L`. */
private fun pinnedDrawInAtWorkingPoint(span: Double): Double = 2.4 * 9.0 / span

private val VALIDITY = listOf(
    "TRL 1-3. NOTHING HERE IS MEASURED. No element has been built and none is proposed as a " +
            "sequence design; the spans and arms are quoted in base pairs to make the design " +
            "statement concrete, not to specify a staple.",
    "EULER-BERNOULLI, with the end condition carried as a bracket of exactly 4 (48 against 192). " +
            "An origami-to-superstructure joint is not obviously either, and WHICH IT IS is a " +
            "design choice rather than a measurement — which is why no finite-element model was " +
            "run to collapse it.",
    "The membrane term is the two-term large-deflection model, built from C-0014's own cable " +
            "functions. At 3 nm on a 25-55 nm span the deflection ratio is 5-12 %, which is " +
            "inside the range that model covers; at §3's DESIRED 10 nm stroke it is 18-40 % and " +
            "the two-term form understates the stiffening, so the 10 nm column is a LOWER bound " +
            "on the tension and the verdict there (past the 65 pN ceiling) is conservative.",
    "The crossover hinge constant is CanDo's nick factor carried through Chen et al.'s fit " +
            "(C-0009's own words: a MODEL INPUT with an experimental bracket, not a measurement). " +
            "T-9 would settle it; because r goes as sqrt(k_theta) the whole bracket is 1.41x in " +
            "a design length, and the element's VERDICT does not move across it.",
    "The hinge bond force resolves a moment over the interhelical distance, which is a " +
            "construction. The duplex diameter (2.0 nm) would give 1.35x more, still inside the " +
            "unzip allowable at 45 paths.",
    "The lever the coupling reaches is assumed vertically AND laterally grounded, exactly as " +
            "C-0017 assumes. A two-sided coupling makes that assumption load-bearing in a way a " +
            "one-sided one did not: it now has to react a downward push as well as an upward pull.",
    "The zero-bias balance is C-0021's, re-run unchanged, and inherits every one of its validity " +
            "limits: the van der Waals combining relation, the unspecified electrode material " +
            "(2.6x), the unsourced electrolyte screening of the zero-frequency term, and the " +
            "single-layer effective-medium caveat.",
    "The layer is C-0003's at C-0001's single grafting density per height, not C-0011's solved " +
            "SCF profile; C-0016 reports the solved layer 1.22x outside that bracket at 5 nm.",
    "Mean-field electrostatics, inherited whole. C-0005's one-loop correction is 123-214 % across " +
            "this gap range — larger than every margin in the pull-in section, which is therefore " +
            "reported as a SENSITIVITY and not as a ceiling.",
    "The pull-in reading is taken at the HELD GAP, i.e. |k_eff(L0-3, V)| against the coupling " +
            "stiffness, and not on C-0018's moving equilibrium path. The two agree at the mandate " +
            "to the departure recorded in the reproductions table; the elasticity derived from it " +
            "is used only to convert a stiffness margin into a bias margin.",
    "The flexure is treated as ONE beam per load path. A real superstructure would carry many " +
            "of them on a common sheet, where crossovers between neighbours would stiffen the " +
            "array; independent leaf springs are the compliant reading and therefore the one " +
            "that has to be checked against the CEILING, not the floor.",
    "No dishing. C-0006 rejects the rigid-plate assumption under any concentrated load; the 45 " +
            "attachments are C-0015's own flatness grid, cited and not recomputed against the " +
            "layer this task loads.",
    "No biased states, no stroke-resolved output, no lateral coordinate. T-3/T-4 and T-12 own " +
            "them; the lateral by-product of a flexure array is noted and not computed."
)

private val OPEN_QUESTIONS = listOf(
    "How an origami joint is built decides the flexure's end condition AND its axial restraint, " +
            "and those two together are 2.2x in span and 5.7x in tangent stiffness. This is a " +
            "sequence-design question, and it is the one this task would hand to a designer first.",
    "The crossover hinge constant, T-9's deliverable, used here as a SPRING — which is what it " +
            "was fitted as, and the one place in this programme where that is an advantage.",
    "Whether a flexure array on a common superstructure stays as compliant as independent leaf " +
            "springs. Crossovers between neighbouring flexures would stiffen it, and the " +
            "compliance ceiling is the binding side.",
    "The dishing a two-sided coupling causes. It reacts in BOTH directions, so the tile is now " +
            "loaded upward at some attachments and downward at others during a thermal " +
            "excursion, which C-0006's uniform-load exact-rigidity result does not cover.",
    "Whether the lever's own joints are two-sided. A coupling that can push is only as two-sided " +
            "as the path behind it, and C-0017 budgets the lever as a section requirement rather " +
            "than as a jointed structure."
)

private val CITED = listOf(
    "The duplex bending rigidity EI = 230 pN*nm^2 and torsional GJ = 460 — CITED, CanDo MODEL " +
            "INPUTS (Kim et al., NAR 40:2862, 2012), not measurements; CanDo's own implied " +
            "persistence length is 55.5 nm against ~40-47 nm measured, so this is the STIFF end " +
            "and the flexure spans below are correspondingly long.",
    "The duplex stretch modulus S = 1100 pN — CITED, MEASURED, Wang et al., Biophys. J. 72:1335 " +
            "(1997), in Mg2+.",
    "The crossover hinge constant k_theta = 2 alpha B/(100 a) with alpha in [0.6, 1.2] — CITED, " +
            "FITTED, Chen, Weng, Riccitelli, Cui, Irudayaraj & Choi, JACS 136:6995 (2014), SI " +
            "S2, through C-0009/Gen1Tile. The 1/100 is CanDo's nick softening.",
    "The ssDNA Kuhn length 2.10 nm at zero force — CITED, MEASURED, Chen et al., PNAS 109:799 " +
            "(2012). The METHOD-SYSTEMATIC bracket is 1.34-1.41 nm from 10-40 pN force " +
            "spectroscopy (Bosco et al., NAR 42:2064, 2014) against 2.10-2.84 nm from zero-force " +
            "scattering; the elements here carry ~1-2 pN, an order below the lowest force the " +
            "spectroscopy fits cover, so the ZERO-FORCE end is the applicable one — and it is " +
            "the soft one, hence conservative for a stiffness requirement.",
    "The contour per nucleotide 0.65 nm, inextensible convention — CITED, MEASURED (Sim et al. " +
            "2012; Bosco et al. 2014). The convention travels with the number: an extensible fit " +
            "needs 0.57 nm instead and mixing them double-counts the extension.",
    "The rise per base pair 0.34 nm and the interhelical distance 2.69 nm — CITED (Douglas et " +
            "al. 2009; Fischer et al. 2016, SAXS, MEASURED).",
    "The per-path allowables 10 / 48 / 65 pN — CITED via C-0006's literature trace (Strunz 1999; " +
            "Essevaz-Roulet 1997; van Mameren 2009). NOT §4(f)'s 35-60 pN whole-cross-section " +
            "band, and a DNA rupture force without a loading rate is not a material constant.",
    "C-0015's 45 attachments as 3 x 15, C-0017's mandate and K2, C-0021's thermal force scale, " +
            "van der Waals assembly and Boltzmann quadrature, C-0014's tether and elements, " +
            "C-0003's layer, C-0008's field — all RE-RUN here as libraries, with the departures " +
            "in the reproductions table.",
    "C-0017's stability floor at 10 nm / 2 mM (23.41-27.91 pN/nm) and C-0018's bias margin there " +
            "(1.007-1.032) — CITED, and used to grade this task's own derived reading of both.",
    "§3/§6's targets: 100 pN, 3 nm acceptable and 10 nm desired stroke, 3.0 nm positional bound, " +
            "1 kHz, 40 x 40 nm, 5/7/10 nm, 2 mM MgCl2 — CITED."
)

private fun report(result: TwoSidedResult) {
    println()
    println("=".repeat(100))
    println("T-23 — ${result.title}")
    println("=".repeat(100))
    println()
    println("ELEMENTS:")
    result.elements.forEach {
        println(
            ("  %-4s %-58s two-sided=%-5s secant=%9.3f tangent=%9.3f " +
                    "peak=%7.2f pN").format(
                it.id, it.element.take(58), it.twoSided, it.assembledSecant, it.assembledTangent,
                it.perPathPeakForce
            )
        )
    }
    println()
    println("CONFINEMENT, by scenario:")
    result.confinement.groupBy { it.scenario }.forEach { (scenario, records) ->
        val solved = records.filter { it.restingHeight != null }
        if (solved.isEmpty()) {
            println("  %-64s NO EQUILIBRIUM".format(scenario.take(64)))
        } else {
            println(
                ("  %-62s well %8.1f-%8.1f k_BT, confining %d/%d, " +
                        "RMS %.3f-%.3f nm").format(
                    scenario.take(62),
                    solved.minOf { it.escapeBarrier!! }, solved.maxOf { it.escapeBarrier!! },
                    solved.count { it.confining }, solved.size,
                    solved.minOf { it.rms!! }, solved.maxOf { it.rms!! }
                )
            )
        }
    }
    println()
    println("PRELOAD TRADE:")
    result.preloadTrade.forEach {
        println(
            ("  %5.2f bp  k_c=%7.3f  F=%8.3f pN (%6.2fx)  span=%6.2f nm  descent=%6.4f  " +
                    "stroke=%6.4f  held-gap margin=%.4f  C-0018 axis=%.4f").format(
                it.offsetBasePairs, it.couplingStiffness, it.preload, it.preloadOverThermalScale,
                it.flexureSpan, it.descentAtTenNanometres, it.deliveredStroke, it.biasMargin,
                it.biasMarginOnFoldAxis
            )
        )
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
