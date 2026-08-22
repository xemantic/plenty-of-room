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

package com.xemantic.nano.plentyofroom.structure

import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.equipartitionStiffness
import com.xemantic.nano.plentyofroom.thermalEnergy
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
import com.xemantic.nano.plentyofroom.brush.stiffness
import com.xemantic.nano.plentyofroom.brush.threeBodyInteraction
import com.xemantic.nano.plentyofroom.brush.twoBodyInteraction
import com.xemantic.nano.plentyofroom.lattice.LatticeTag
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.poroelastic.FiberArrayPermeability
import com.xemantic.nano.plentyofroom.poroelastic.FreeDrainingSegments
import com.xemantic.nano.plentyofroom.poroelastic.RectangularFootprint
import com.xemantic.nano.plentyofroom.poroelastic.brinkmanTransmissivity
import com.xemantic.nano.plentyofroom.poroelastic.squeezeDragCoefficient
import com.xemantic.nano.plentyofroom.poroelastic.tileStokesDrag
import com.xemantic.nano.plentyofroom.poroelastic.waterViscosity
import com.xemantic.nano.plentyofroom.structure.withEmissionHeader
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.encodeToJsonElement
import java.io.File
import kotlin.math.sqrt

/**
 * Task `T-8` / leaf `A1.2` — the tile's positional variance at 300 K, resolved by mode,
 * across the `C-0003` stiffness bracket, and split against the ≥ 1 kHz measurement band.
 *
 * ```shell
 * ./gradlew study -Pstudy=structure.TilePositionalVarianceStudyKt
 * ```
 *
 * Emits `gpd/results/T-8-tile-positional-variance.json`, deterministically.
 */

// ------------------------------------------------------------------ parameters

/** The design point every standing claim in the programme is quoted at. */
internal const val LAYER_HEIGHT = 10.0
internal const val GRAFTING_DENSITY = 0.024

/**
 * The piston amplitude to report for a case the study has already declared **undefined** — or
 * `null`, which is most of the time and is the honest answer.
 *
 * `null`, never `Infinity`: an unconfined coordinate has no RMS amplitude at all, and writing one
 * would be a number where the honest answer is "not well posed". (`kotlinx.serialization` also
 * refuses `Infinity`, but that is the smaller reason.)
 *
 * **The guard is on the physics, not on the sign** (`P-15`, `C-0031`). The case this record exists
 * for is the strong-stretching profile at `L₀`, where the disjoining pressure vanishes
 * *quadratically* and the stiffness is **exactly zero** (`C-0003`) — numerically a rounding-level
 * value of either sign. Guarding on `stiffness > 0.0`, as this did, is a sign test on a quantity
 * that is meant to be zero, so the noise decides the answer: repairing `bracketedRoot` moved the
 * solved height by its own `1e-6` tolerance, flipped that zero positive at `2.2e-14 pN/nm`, and
 * turned an emitted `null` into a piston RMS of 13 637 236 nm against a 10 nm layer.
 *
 * The criterion used instead is the one the surrounding block already applies to call the case
 * undefined: **a linearised fluctuation is only meaningful while it stays inside the layer it is
 * fluctuating against.** It is noise-immune, it needs no tolerance, and it is scale-free.
 */
internal fun unconstrainedPistonRms(layerStiffness: Double, layerHeight: Double): Double? {
    require(layerHeight > 0.0) { "layerHeight must be positive, was: $layerHeight" }
    if (layerStiffness <= 0.0) return null
    val rms = sqrt(thermalEnergy() / layerStiffness)
    return if (rms <= layerHeight) rms else null
}

/** §3, via `C-0002`/`C-0003`: the measured PEG/water osmotic virials. */
private const val OSMOTIC_SECOND_VIRIAL = 1.9e-3
private const val OSMOTIC_THIRD_VIRIAL = 2.0e-2

/** `C-0003`'s profile quadrature resolution, so the stiffnesses reproduce its table. */
private const val PROFILE_PANELS = 1024

/** The §6 task 8 acceptance threshold, in nm. */
private const val PREDICATE_RMS = 3.0

/** §3's bandwidth requirement, in Hz — the band the variance is split against. */
private const val BANDWIDTH_TARGET = 1000.0

/** The Ritz basis degree; gate 4 sweeps it. */
private const val BASIS_DEGREE = 12
private val CONVERGENCE_DEGREES = listOf(8, 12, 16, 20)

/** Observation times the unconfined lateral excursion is quoted at, in s. */
private val LATERAL_OBSERVATION_TIMES = listOf(1.0e-6, 1.0e-3, 1.0)

/** Anchor layout for the one confined variant: four tethers of total layer stiffness. */
private const val ANCHOR_SIDE = 2

// ------------------------------------------------------------------ result model

@Serializable
data class VarianceCase(
    val compression: String,
    val profile: String,
    val interaction: String,
    val heightRatio: Double,
    val height: Double,
    val monomersPerChain: Double,
    val volumeFraction: Double,
    val layerStiffness: Double,
    val foundationStiffness: Double,
    val pistonRms: Double,
    val tiltRms: Double,
    val dishingRms: Double,
    val rigidBodyRms: Double,
    val areaRms: Double,
    val centreRms: Double,
    val edgeMidpointRms: Double,
    val cornerRms: Double,
    val dishingOverPiston: Double,
    val screeningLength: Double,
    val squeezeDrag: Double,
    val totalDrag: Double,
    val relaxationTime: Double,
    val cornerFrequency: Double,
    val varianceFractionInBand: Double,
    val areaRmsInBand: Double,
    val cornerRmsInBand: Double,
    val areaRmsMeetsPredicate: Boolean,
    val cornerRmsMeetsPredicate: Boolean
)

@Serializable
data class UndefinedCase(
    val compression: String,
    val profile: String,
    val interaction: String,
    val layerStiffness: Double,
    val unconstrainedPistonRms: Double?,
    val reason: String
)

@Serializable
data class CompressionBracket(
    val compression: String,
    val note: String,
    val layerStiffnessLow: Double,
    val layerStiffnessHigh: Double,
    val pistonRmsHigh: Double,
    val areaRmsHigh: Double,
    val cornerRmsHigh: Double,
    val dishingOverPistonLow: Double,
    val dishingOverPistonHigh: Double,
    val areaRmsInBandHigh: Double,
    val cornerRmsInBandHigh: Double,
    val worstQuantityOverPredicate: Double,
    val verdict: String
)

@Serializable
data class RigidityVariant(
    val sheet: String,
    val alongHelixRigidity: Double,
    val acrossHelixRigidity: Double,
    val pistonRms: Double,
    val dishingRms: Double,
    val areaRms: Double,
    val cornerRms: Double,
    val dishingOverPiston: Double
)

@Serializable
data class AnchoredVarianceCase(
    val anchors: Int,
    val anchorStiffnessEach: Double,
    val layerStiffness: Double,
    val pistonRms: Double,
    val dishingRms: Double,
    val areaRms: Double,
    val cornerRms: Double,
    val note: String
)

@Serializable
data class LateralExcursion(
    val observationTime: Double,
    val excursionRms: Double,
    val timesTilePredicate: Double,
    val timesTileEdge: Double
)

@Serializable
data class LateralMode(
    val restoringStiffnessFromLayer: Double,
    val argument: String,
    val screeningLength: Double,
    val dragCoefficient: Double,
    val diffusivity: Double,
    val excursions: List<LateralExcursion>,
    val requiredStiffnessForPredicate: Double,
    val duplexStrutStiffnessAtTenNanometres: Double,
    val duplexStrutStiffnessAtTwentyNanometres: Double,
    val verdict: String
)

@Serializable
data class BoundTableRow(
    val positionalRms: Double,
    val requiredStiffness: Double,
    val leafA11Value: String
)

@Serializable
data class ConvergenceRow(
    val basisDegree: Int,
    val dishingRms: Double,
    val areaRms: Double,
    val cornerRms: Double
)

@Serializable
data class VarianceParameters(
    val temperature: Double,
    val medium: String,
    val thermalEnergy: Double,
    val thermalEnergyElectronVolts: Double,
    val tileFootprint: String,
    val tileArea: Double,
    val layerHeight: Double,
    val graftingDensity: Double,
    val targetForce: Double,
    val predicateRms: Double,
    val bandwidthTarget: Double,
    val basisDegree: Int,
    val profilePanels: Int,
    val viscosity: Double,
    val permeabilityModel: String,
    val permeabilityProvenance: String,
    val stiffnessSource: String,
    val dragSource: String,
    val acceptanceQuantity: String
)

@Serializable
data class VarianceResult(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val leafAcceptance: String,
    val leafAcceptanceDischarged: List<String>,
    val leafAcceptanceNotDischarged: List<String>,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val validity: List<String>,
    val parameters: VarianceParameters,
    val brackets: List<CompressionBracket>,
    val cases: List<VarianceCase>,
    val undefinedCases: List<UndefinedCase>,
    val rigidityVariants: List<RigidityVariant>,
    val anchored: AnchoredVarianceCase,
    val lateral: LateralMode,
    val boundTable: List<BoundTableRow>,
    val convergence: List<ConvergenceRow>,
    val verdict: Map<String, String>
)

// ------------------------------------------------------------------ the C-0003 layer models

/** One of `C-0003`'s six (profile × interaction) readings of the Gen-1 layer. */
internal data class LayerReading(
    val profile: String,
    val interaction: String,
    val model: GraftedLayerModel,
    val chain: GraftedChain,
    val equilibriumHeight: Double
)

internal fun interactionFor(peg: PegWater, choice: String): InteractionFreeEnergy {
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

/**
 * Rebuilds `C-0003`'s six layer readings at the 10 nm design point.
 *
 * **Derived, not cited.** The stiffness bracket this task quotes is the single most
 * load-bearing input it has, and §7 asks that inherited numbers be re-derived rather than
 * copied; so the free energies are reconstructed from the same measured virials and the
 * same profile models `T-1c` used, and the resulting stiffnesses are asserted against
 * `C-0003`'s published table in the study's own consistency check.
 */
internal fun layerReadings(peg: PegWater): List<LayerReading> =
    listOf("alexander-box", "strong-stretching").flatMap { profile ->
        listOf("two-body", "virial", "des-Cloizeaux").map { choice ->
            val interaction = interactionFor(peg, choice)
            val model: GraftedLayerModel = if (profile == "alexander-box") {
                AlexanderBoxLayer(interaction)
            } else {
                StrongStretchingLayer(interaction, PROFILE_PANELS)
            }
            val length = model.chainLengthForHeight(peg, LAYER_HEIGHT, GRAFTING_DENSITY)
            val chain = peg.graftedChain(length, GRAFTING_DENSITY)
            LayerReading(profile, choice, model, chain, model.equilibriumHeight(chain))
        }
    }

// ------------------------------------------------------------------ the study

fun main() {
    val peg = PegWater()
    val viscosity = waterViscosity()
    val footprint = RectangularFootprint(Gen1Tile.EDGE_X, Gen1Tile.EDGE_Y)
    val variants = gen1SheetVariants()
    val (_, nominalSheet) = variants.first()
    val plate = nominalSheet.plate(Gen1Tile.EDGE_X, Gen1Tile.EDGE_Y)
    val readings = layerReadings(peg)

    // C-0004's least permeable model, so the corner frequency is the LOWEST the three
    // models allow and the in-band variance fraction is therefore an upper bound
    val permeability = FiberArrayPermeability(fiberRadius = peg.kuhnSegmentDiameter / 2.0)
    val segmentScale = FreeDrainingSegments(
        segmentLength = peg.kuhnLength,
        segmentDiameter = peg.kuhnSegmentDiameter,
        segmentVolume = peg.kuhnSegmentVolume
    )

    val compressions = listOf(
        "first-contact" to 1.0,
        "0.9 L0" to 0.9,
        "0.8 L0" to 0.8,
        "working-point" to Double.NaN
    )

    val evaluated = compressions.flatMap { (label, ratio) ->
        readings.map { reading ->
            varianceCase(label, ratio, reading, plate, footprint, permeability, viscosity)
        }
    }
    val cases = evaluated.mapNotNull { it.first }
    val undefinedCases = evaluated.mapNotNull { it.second }

    val brackets = compressions.map { (label, _) ->
        bracketOf(label, cases.filter { it.compression == label })
    }

    val nominal = readings.first { it.profile == "strong-stretching" && it.interaction == "des-Cloizeaux" }
    val nominalWorking = cases.first {
        it.compression == "working-point" && it.profile == "strong-stretching" &&
                it.interaction == "des-Cloizeaux"
    }
    val nominalFoundation = nominalWorking.foundationStiffness

    val rigidityVariants = listOf(0, 4, 5).map { index ->
        val (record, sheet) = variants[index]
        val budget = PlateOnFoundation(
            sheet.plate(Gen1Tile.EDGE_X, Gen1Tile.EDGE_Y), nominalFoundation,
            basisDegree = BASIS_DEGREE
        ).positionalVarianceBudget()
        RigidityVariant(
            sheet = record.name,
            alongHelixRigidity = record.alongHelixRigidity,
            acrossHelixRigidity = record.acrossHelixRigidity,
            pistonRms = budget.pistonRms,
            dishingRms = budget.dishingRms,
            areaRms = budget.areaRms,
            cornerRms = budget.cornerRms,
            dishingOverPiston = budget.dishingOverPiston
        )
    }

    val anchorStiffness = nominalWorking.layerStiffness / (ANCHOR_SIDE * ANCHOR_SIDE)
    val anchoredBudget = PlateOnFoundation(
        plate, nominalFoundation,
        supports = insetGrid(ANCHOR_SIDE, plate.lengthX, plate.lengthY).map { (x, y) ->
            PointSupport(x, y, anchorStiffness)
        },
        basisDegree = BASIS_DEGREE
    ).positionalVarianceBudget()
    val anchored = AnchoredVarianceCase(
        anchors = ANCHOR_SIDE * ANCHOR_SIDE,
        anchorStiffnessEach = anchorStiffness,
        layerStiffness = nominalWorking.layerStiffness,
        pistonRms = anchoredBudget.pistonRms,
        dishingRms = anchoredBudget.dishingRms,
        areaRms = anchoredBudget.areaRms,
        cornerRms = anchoredBudget.cornerRms,
        note = "four tethers of total stiffness equal to the layer's own, at the nominal " +
                "working point. Anchors are the only thing in the §3 stack that could " +
                "confine the lateral mode, and they cost flatness to do it (C-0006)."
    )

    val lateral = lateralMode(peg, segmentScale, viscosity, nominalWorking)

    val convergence = CONVERGENCE_DEGREES.map { degree ->
        val budget = PlateOnFoundation(plate, nominalFoundation, basisDegree = degree)
            .positionalVarianceBudget()
        ConvergenceRow(degree, budget.dishingRms, budget.areaRms, budget.cornerRms)
    }

    val boundTable = listOf(3.0, 1.0, 0.1, 0.03).map { rms ->
        BoundTableRow(
            positionalRms = rms,
            requiredStiffness = equipartitionStiffness(rms),
            leafA11Value = when (rms) {
                3.0 -> "leaf A1.1: k >= ~0.46 pN/nm"
                0.1 -> "leaf A1.1 (prize): k >= ~414 pN/nm"
                0.03 -> "leaf A1.1: k >= ~4.6 N/m"
                else -> "not tabulated by A1.1"
            }
        )
    }

    val worstArea = cases.maxOf { it.areaRms }
    val worstCorner = cases.maxOf { it.cornerRms }

    val result = VarianceResult(
        task = "T-8",
        leaf = "A1.2",
        title = "Tile positional variance at 300 K, by mode, across the C-0003 stiffness bracket",
        verificationType = "in-silico (analytic multi-mode equipartition on the Rayleigh-Ritz " +
                "plate functional; EXACT for a harmonic functional, hence no sampling and no " +
                "sampling confidence interval — the uncertainty carried is the model bracket)",
        acceptance = "sigma_RMS <= $PREDICATE_RMS nm for the nominal Gen-1 tile (§6 task 8)",
        leafAcceptance = "Simulated sigma_RMS <= 3.0 nm for nominal Gen-1 lever; 95% CI " +
                "reported. Method named by the leaf: coarse-grained/MD (oxDNA/Martini) ensemble.",
        leafAcceptanceDischarged = listOf(
            "sigma_RMS <= 3.0 nm, evaluated for the nominal Gen-1 tile at 300 K",
            "reported for EVERY degree of freedom the tile has against the layer — piston, " +
                    "two tilts, and the internal shape modes — not for one",
            "reported across a stated stiffness bracket (C-0003, six models) at four stated " +
                    "compressions, and across the C-0006 crossover-hinge sweep",
            "reported against a stated measurement bandwidth, from C-0004's drainage corner"
        ),
        leafAcceptanceNotDischarged = listOf(
            "NOT DISCHARGED: 'simulated' in the sense the leaf's tool column names — a " +
                    "coarse-grained/MD (oxDNA/Martini) ensemble. This is an analytic " +
                    "multi-mode equipartition result, exact within its model.",
            "NOT DISCHARGED: a 95% confidence interval. A CI on an exact analytic result is " +
                    "a category error; the bracket above is a MODEL range, not a sampling " +
                    "interval, and must not be reported as one.",
            "NOT DISCHARGED: the lateral (in-plane) mode, which the layer does not confine " +
                    "at all — see the lateral block. It is bounded by the anchoring scheme, " +
                    "which §3 does not specify.",
            "NOT ADDRESSED: the lever itself. The leaf says 'lever positional variance'; this " +
                    "task produces the TILE's, and C-0006 shows the two differ by the local " +
                    "dishing wherever the coupling is not effectively continuous."
        ),
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "energy" to "pN*nm",
            "stiffness" to "pN/nm (= mN/m)",
            "foundationStiffness" to "pN/nm^3",
            "flexuralRigidity" to "pN*nm",
            "drag" to "pN*s/nm",
            "diffusivity" to "nm^2/s",
            "frequency" to "Hz",
            "time" to "s",
            "temperature" to "K"
        ),
        conventions = listOf(
            "x along the helices, y across them, origin at the centre of the footprint",
            "w positive DOWNWARD, compressing the polymer layer; every amplitude here is an " +
                    "RMS and therefore unsigned",
            "pistonRms is the fluctuation of the tile's AREA-AVERAGED height, and it is the " +
                    "only mode with a non-zero area average — tilt and dishing average to zero",
            "areaRms is the RMS over the ensemble AND the footprint, " +
                    "sqrt(piston^2 + tilt^2 + dishing^2)",
            "centreRms / edgeMidpointRms / cornerRms are the fluctuations of a MATERIAL POINT, " +
                    "which is what a point-coupled lever samples; they differ by sqrt(7) even " +
                    "for a perfectly rigid tile",
            "the stiffness is quoted at a stated compression throughout, never 'at the resting " +
                    "height' — C-0001 surprise S-1, upheld by C-0003",
            "the in-band figures are the part of the variance below " +
                    "${BANDWIDTH_TARGET.toInt()} Hz, from the Lorentzian of an overdamped mode"
        ),
        validity = listOf(
            "Harmonic (linear) response only. The layer is strongly nonlinear, so each case is " +
                    "the TANGENT stiffness at a stated compression, and a fluctuation " +
                    "comparable to the compression itself would leave this linearisation.",
            "At first contact the strong-stretching models have EXACTLY ZERO stiffness " +
                    "(C-0003), so the unbiased variance is not merely large but UNDEFINED " +
                    "there; those three cases are omitted rather than reported as infinite.",
            "A non-adsorbing layer exerts no upward force at all above L0, so an unbiased " +
                    "free tile is unconfined on the far side too. The predicate is therefore " +
                    "meaningful at the WORKING POINT, which is where the actuator operates.",
            "Kirchhoff plate on a linear Winkler foundation. C-0009 (T-10) has now run the " +
                    "discrete-lattice check and it is a CITED correction rather than an " +
                    "argument: for the THERMAL case specifically the grillage dishes " +
                    "1.113-1.199 times the plate, rising with k_f, so every dishing amplitude " +
                    "here is an UNDERESTIMATE by 11-20%. Scaling the whole dishing component " +
                    "by 1.20 is an upper bound on the correction and moves no verdict.",
            "The drag is C-0004's squeeze-out plus Stokes at the PISTON mode; higher modes " +
                    "have shorter drainage paths and higher stiffness, hence higher corners, " +
                    "so using the piston corner for the whole budget bounds the in-band share " +
                    "from ABOVE.",
            "No electrostatics. Under bias the electrostatic spring constant is NEGATIVE " +
                    "(§1), so k_eff < k_brush and every amplitude here is a LOWER bound under " +
                    "bias. T-4 owns that, and it is the one correction that runs the wrong way.",
            "k_theta, the crossover hinge constant, is the largest open premise under the " +
                    "dishing modes; swept over Chen et al.'s admissible alpha in " +
                    "rigidityVariants, and T-9 is where it gets measured.",
            "Nothing here is measured about this tile. PASS means model-consistent and traceable."
        ),
        parameters = VarianceParameters(
            temperature = ROOM_TEMPERATURE,
            medium = "aqueous buffer, 2/5/10 mM MgCl2",
            thermalEnergy = thermalEnergy(),
            thermalEnergyElectronVolts = thermalEnergy() / 160.2176634,
            tileFootprint = "${Gen1Tile.EDGE_X.toInt()} x ${Gen1Tile.EDGE_Y.toInt()} nm",
            tileArea = plate.area,
            layerHeight = LAYER_HEIGHT,
            graftingDensity = GRAFTING_DENSITY,
            targetForce = Gen1Tile.TARGET_FORCE,
            predicateRms = PREDICATE_RMS,
            bandwidthTarget = BANDWIDTH_TARGET,
            basisDegree = BASIS_DEGREE,
            profilePanels = PROFILE_PANELS,
            viscosity = viscosity,
            permeabilityModel = permeability.name,
            permeabilityProvenance = permeability.provenance +
                    " — used here because it is the SLOWEST of C-0004's three models and " +
                    "therefore gives the lowest corner frequency and the largest in-band share",
            stiffnessSource = "DERIVED — C-0003's six (profile x interaction) layer models " +
                    "rebuilt from the measured PEG/water virials at L0 = $LAYER_HEIGHT nm, " +
                    "sigma = $GRAFTING_DENSITY nm^-2, not copied from its table",
            dragSource = "DERIVED — C-0004's squeeze-out drag eta G A / T plus the tile's " +
                    "broadside Stokes drag, at the compressed height of each case",
            acceptanceQuantity = "areaRms — the RMS of a tile point over both the ensemble " +
                    "and the footprint. cornerRms is reported alongside as the worst point, " +
                    "and it is the reading that fails at the soft end of the bracket."
        ),
        brackets = brackets,
        cases = cases,
        undefinedCases = undefinedCases,
        rigidityVariants = rigidityVariants,
        anchored = anchored,
        lateral = lateral,
        boundTable = boundTable,
        convergence = convergence,
        verdict = mapOf(
            "predicate" to ("sigma_RMS <= $PREDICATE_RMS nm. Worst area RMS anywhere in the " +
                    "bracket %.3f nm (%.0f%% of the predicate); worst point (corner) %.3f nm " +
                    "(%.0f%%).").format(
                worstArea, 100.0 * worstArea / PREDICATE_RMS,
                worstCorner, 100.0 * worstCorner / PREDICATE_RMS
            ),
            "area-rms" to if (worstArea <= PREDICATE_RMS) "PASS everywhere in the bracket"
            else "FAIL somewhere in the bracket",
            "worst-point" to if (worstCorner <= PREDICATE_RMS) "PASS everywhere in the bracket"
            else ("FAIL at the soft end — a point-coupled lever at a tile CORNER sees more " +
                    "than the predicate, because both rigid tilts are at full lever there " +
                    "and the free-edge dishing modes peak there too"),
            "dominant-mode" to ("the tile's internal SHAPE modes, not its position mode: " +
                    "dishing/piston = %.2f at the nominal working point, and it GROWS as the " +
                    "foundation stiffens. C-0006's finding survives the C-0003 bracket.").format(
                nominalWorking.dishingOverPiston
            ),
            "bandwidth" to ("only %.2f%% of the variance lies below %.0f Hz at the nominal " +
                    "working point (drainage corner %.0f kHz), so the in-band area RMS is " +
                    "%.4f nm — %.1f%% of the predicate.").format(
                100.0 * nominalWorking.varianceFractionInBand, BANDWIDTH_TARGET,
                nominalWorking.cornerFrequency / 1000.0, nominalWorking.areaRmsInBand,
                100.0 * nominalWorking.areaRmsInBand / PREDICATE_RMS
            ),
            "lateral" to lateral.verdict,
            "unbiased" to "NOT WELL POSED. Three of C-0003's six models have exactly zero " +
                    "stiffness at first contact and a non-adsorbing layer pushes not at all " +
                    "from above, so an unbiased free tile is unconfined in both directions. " +
                    "The predicate is answered at a stated compression, and at the working " +
                    "point above all.",
            "discrete-lattice-correction" to ("CITED from C-0009/CH-0008: the grillage " +
                    "dishes 1.113-1.199x the plate in the THERMAL case, rising with k_f. " +
                    "Scaling the dishing component by 1.20 — an upper bound — gives an area " +
                    "RMS of %.2f nm and a corner of at most %.2f nm at the nominal working " +
                    "point, against %.2f and %.2f here. No verdict moves.").format(
                sqrt(
                    nominalWorking.pistonRms * nominalWorking.pistonRms +
                            nominalWorking.tiltRms * nominalWorking.tiltRms +
                            1.44 * nominalWorking.dishingRms * nominalWorking.dishingRms
                ),
                1.2 * nominalWorking.cornerRms,
                nominalWorking.areaRms, nominalWorking.cornerRms
            ),
            "leaf-A1.2" to "PARTLY DISCHARGED. The numeric half is answered and strengthened; " +
                    "the 'simulated ensemble with a 95% CI' half is NOT, and is not " +
                    "substituted for. See leafAcceptanceNotDischarged."
        )
    )

    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-8-tile-positional-variance.json")
    output.parentFile.mkdirs()
    output.writeText(json.encodeToString(json.encodeToJsonElement(result).roundedForResult().withEmissionHeader(LatticeTag.SQUARE, null)) + "\n")
    report(result, nominal.chain.monomersPerChain, output)
}

// ------------------------------------------------------------------ one case

private fun varianceCase(
    label: String,
    ratio: Double,
    reading: LayerReading,
    plate: OrthotropicPlate,
    footprint: RectangularFootprint,
    permeability: FiberArrayPermeability,
    viscosity: Double
): Pair<VarianceCase?, UndefinedCase?> {
    val working = label == "working-point"
    val height = if (working) {
        reading.model.heightUnderLoad(reading.chain, Gen1Tile.TARGET_FORCE, plate.area)
    } else {
        ratio * reading.equilibriumHeight
    }
    val layerStiffness = reading.model.stiffness(reading.chain, height, plate.area)
    // C-0003: the strong-stretching pressure vanishes QUADRATICALLY at L0, so its stiffness
    // there is zero — numerically a rounding-level positive, physically nothing. The
    // linearised fluctuation is only meaningful while it stays inside the layer it is
    // fluctuating against, so the domain of the harmonic model is k > k_BT/L0^2: below that
    // the piston RMS exceeds the whole layer height and the tile is not on the layer at all.
    // These cases are recorded as UNDEFINED rather than reported as enormous numbers.
    val floor = thermalEnergy() / (LAYER_HEIGHT * LAYER_HEIGHT)
    if (layerStiffness <= floor) {
        return null to UndefinedCase(
            compression = label,
            profile = reading.profile,
            interaction = reading.interaction,
            layerStiffness = layerStiffness,
            unconstrainedPistonRms = unconstrainedPistonRms(layerStiffness, LAYER_HEIGHT),
            reason = "stiffness at or below k_BT/L0^2 = %.4f pN/nm, so the harmonic ".format(floor) +
                    "fluctuation would exceed the layer height and the linearisation has " +
                    "left its own domain. For the strong-stretching profile at first " +
                    "contact this is EXACT and not marginal: the disjoining pressure " +
                    "vanishes quadratically at L0 (C-0003), so the stiffness there is zero."
        )
    }

    val foundation = layerStiffness / plate.area
    val budget = PlateOnFoundation(plate, foundation, basisDegree = BASIS_DEGREE)
        .positionalVarianceBudget()

    val volumeFraction = reading.chain.meanVolumeFraction(height)
    val permeabilityValue = permeability.permeability(volumeFraction)
    val transmissivity = brinkmanTransmissivity(permeabilityValue, height)
    val squeeze = squeezeDragCoefficient(footprint, transmissivity, viscosity)
    val drag = squeeze + tileStokesDrag(footprint, viscosity)
    val corner = lorentzianCornerFrequency(layerStiffness, drag)
    val fraction = varianceFractionBelow(BANDWIDTH_TARGET, corner)
    val inBand = sqrt(fraction)

    return VarianceCase(
        compression = label,
        profile = reading.profile,
        interaction = reading.interaction,
        heightRatio = height / reading.equilibriumHeight,
        height = height,
        monomersPerChain = reading.chain.monomersPerChain,
        volumeFraction = volumeFraction,
        layerStiffness = layerStiffness,
        foundationStiffness = foundation,
        pistonRms = budget.pistonRms,
        tiltRms = budget.tiltRms,
        dishingRms = budget.dishingRms,
        rigidBodyRms = budget.rigidBodyRms,
        areaRms = budget.areaRms,
        centreRms = budget.centreRms,
        edgeMidpointRms = budget.edgeMidpointRms,
        cornerRms = budget.cornerRms,
        dishingOverPiston = budget.dishingOverPiston,
        screeningLength = sqrt(permeabilityValue),
        squeezeDrag = squeeze,
        totalDrag = drag,
        relaxationTime = drag / layerStiffness,
        cornerFrequency = corner,
        varianceFractionInBand = fraction,
        areaRmsInBand = budget.areaRms * inBand,
        cornerRmsInBand = budget.cornerRms * inBand,
        areaRmsMeetsPredicate = budget.areaRms <= PREDICATE_RMS,
        cornerRmsMeetsPredicate = budget.cornerRms <= PREDICATE_RMS
    ) to null
}

private fun bracketOf(label: String, cases: List<VarianceCase>): CompressionBracket {
    val worstCorner = cases.maxOf { it.cornerRms }
    val worstArea = cases.maxOf { it.areaRms }
    return CompressionBracket(
        compression = label,
        note = when (label) {
            "first-contact" -> "h = L0. The three strong-stretching models are ABSENT here " +
                    "because their stiffness is exactly zero at first contact (C-0003); only " +
                    "the three box models define a variance at all."
            "working-point" -> "h under the §3 100 pN load, tangent stiffness. This is the " +
                    "state the actuator operates in and the one the predicate is answered at."
            else -> "a stated compression, per C-0001 surprise S-1: 'the layer stiffness' is " +
                    "not a single number at the resting height."
        },
        layerStiffnessLow = cases.minOf { it.layerStiffness },
        layerStiffnessHigh = cases.maxOf { it.layerStiffness },
        pistonRmsHigh = cases.maxOf { it.pistonRms },
        areaRmsHigh = worstArea,
        cornerRmsHigh = worstCorner,
        dishingOverPistonLow = cases.minOf { it.dishingOverPiston },
        dishingOverPistonHigh = cases.maxOf { it.dishingOverPiston },
        areaRmsInBandHigh = cases.maxOf { it.areaRmsInBand },
        cornerRmsInBandHigh = cases.maxOf { it.cornerRmsInBand },
        worstQuantityOverPredicate = worstCorner / PREDICATE_RMS,
        verdict = when {
            worstCorner <= PREDICATE_RMS -> "PASS on every quantity"
            worstArea <= PREDICATE_RMS ->
                "PASS on the area RMS, FAIL at the worst point (tile corner)"
            else -> "FAIL on the area RMS"
        }
    )
}

// ------------------------------------------------------------------ the lateral mode

private fun lateralMode(
    peg: PegWater,
    permeability: FreeDrainingSegments,
    viscosity: Double,
    working: VarianceCase
): LateralMode {
    val screening = permeability.screeningLength(working.volumeFraction)
    val drag = brinkmanShearDrag(
        viscosity = viscosity,
        area = Gen1Tile.EDGE_X * Gen1Tile.EDGE_Y,
        screeningLength = screening,
        thickness = working.height
    )
    val diffusivity = einsteinDiffusivity(drag)
    val required = equipartitionStiffness(PREDICATE_RMS)
    val strutTen = cantileverTransverseStiffness(Gen1Tile.DUPLEX_BENDING_RIGIDITY, 10.0)
    val strutTwenty = cantileverTransverseStiffness(Gen1Tile.DUPLEX_BENDING_RIGIDITY, 20.0)
    return LateralMode(
        restoringStiffnessFromLayer = 0.0,
        argument = "EXACTLY ZERO, by symmetry rather than by smallness: the free energy of a " +
                "laterally homogeneous grafted layer under a laterally homogeneous " +
                "non-adsorbing tile is invariant under lateral translation of the tile, so " +
                "the mean lateral restoring force vanishes identically. The same argument " +
                "kills the yaw mode. Equipartition does not apply to an unconfined " +
                "coordinate: sigma^2 = 2 D t grows without bound.",
        screeningLength = screening,
        dragCoefficient = drag,
        diffusivity = diffusivity,
        excursions = LATERAL_OBSERVATION_TIMES.map { time ->
            val excursion = freeDiffusionRms(diffusivity, time)
            LateralExcursion(
                observationTime = time,
                excursionRms = excursion,
                timesTilePredicate = excursion / PREDICATE_RMS,
                timesTileEdge = excursion / Gen1Tile.EDGE_X
            )
        },
        requiredStiffnessForPredicate = required,
        duplexStrutStiffnessAtTenNanometres = strutTen,
        duplexStrutStiffnessAtTwentyNanometres = strutTwenty,
        verdict = ("NOT BOUNDED BY THE LAYER. The layer supplies exactly zero lateral " +
                "stiffness, so over one 1 kHz period the untethered tile wanders %.0f nm — " +
                "%.0f times the predicate and %.1f tile widths. What bounds it is the " +
                "anchoring scheme, which §3 does not specify. The requirement it must meet " +
                "is k_lateral >= %.3f pN/nm (leaf A1.1); a clamped 10 nm duplex strut gives " +
                "%.2f pN/nm and a 20 nm one %.3f pN/nm, so the requirement is reachable but " +
                "only with SHORT, STIFF anchors — stated as a bound on any future design, " +
                "not as a design.").format(
            freeDiffusionRms(diffusivity, 1.0 / BANDWIDTH_TARGET),
            freeDiffusionRms(diffusivity, 1.0 / BANDWIDTH_TARGET) / PREDICATE_RMS,
            freeDiffusionRms(diffusivity, 1.0 / BANDWIDTH_TARGET) / Gen1Tile.EDGE_X,
            required, strutTen, strutTwenty
        )
    ).also { require(peg.kuhnLength > 0.0) }
}

// ------------------------------------------------------------------ report

private fun report(result: VarianceResult, nominalChainLength: Double, output: File) {
    println("T-8 / A1.2 — ${result.title}")
    println("300 K, aqueous buffer, k_BT = ${"%.3f".format(thermalEnergy())} pN*nm")
    println(
        "tile ${result.parameters.tileFootprint}, layer ${result.parameters.layerHeight} nm at " +
                "sigma = ${result.parameters.graftingDensity} nm^-2, N = %.0f".format(nominalChainLength)
    )
    println()
    println("--- the modal budget, by compression and model ".padEnd(140, '-'))
    println(
        "%14s %18s %14s %8s %7s %7s %7s %7s %7s %7s %6s".format(
            "compression", "profile", "interaction", "k[pN/nm]", "piston", "tilt",
            "dish", "area", "centre", "corner", "d/p"
        )
    )
    result.cases.forEach {
        println(
            "%14s %18s %14s %8.2f %7.3f %7.3f %7.3f %7.3f %7.3f %7.3f %6.2f".format(
                it.compression, it.profile, it.interaction, it.layerStiffness, it.pistonRms,
                it.tiltRms, it.dishingRms, it.areaRms, it.centreRms, it.cornerRms,
                it.dishingOverPiston
            )
        )
    }
    println()
    println("--- the bracket, against the ${result.parameters.predicateRms} nm predicate ".padEnd(140, '-'))
    println(
        "%14s %10s %10s %8s %8s %8s %10s %10s   %s".format(
            "compression", "k low", "k high", "piston", "area", "corner",
            "area<1kHz", "corner<1kHz", "verdict"
        )
    )
    result.brackets.forEach {
        println(
            "%14s %10.2f %10.2f %8.3f %8.3f %8.3f %10.4f %10.4f   %s".format(
                it.compression, it.layerStiffnessLow, it.layerStiffnessHigh, it.pistonRmsHigh,
                it.areaRmsHigh, it.cornerRmsHigh, it.areaRmsInBandHigh, it.cornerRmsInBandHigh,
                it.verdict
            )
        )
    }
    println()
    println("--- bandwidth: what fraction of the variance is in band ".padEnd(140, '-'))
    println("%14s %18s %14s %12s %12s %12s".format(
        "compression", "profile", "interaction", "f_c [kHz]", "fraction", "amplitude"
    ))
    result.cases.filter { it.compression == "working-point" || it.compression == "0.9 L0" }
        .forEach {
            println(
                "%14s %18s %14s %12.1f %12.5f %12.4f".format(
                    it.compression, it.profile, it.interaction, it.cornerFrequency / 1000.0,
                    it.varianceFractionInBand, sqrt(it.varianceFractionInBand)
                )
            )
        }
    println()
    println("--- the crossover-hinge sweep, at the nominal working point ".padEnd(140, '-'))
    result.rigidityVariants.forEach {
        println(
            "%36s D_perp = %6.3f  piston %6.3f  dish %6.3f  area %6.3f  corner %6.3f  d/p %5.2f".format(
                it.sheet, it.acrossHelixRigidity, it.pistonRms, it.dishingRms, it.areaRms,
                it.cornerRms, it.dishingOverPiston
            )
        )
    }
    println()
    println("--- the lateral mode ".padEnd(140, '-'))
    val lateral = result.lateral
    println("layer lateral stiffness             %.1f pN/nm (exactly zero, by symmetry)".format(
        lateral.restoringStiffnessFromLayer
    ))
    println("Brinkman screening length           %.3f nm".format(lateral.screeningLength))
    println("lateral drag coefficient            %.4e pN*s/nm".format(lateral.dragCoefficient))
    println("lateral diffusivity                 %.4e nm^2/s".format(lateral.diffusivity))
    lateral.excursions.forEach {
        println("excursion after %9.1e s        %10.1f nm  (%.0fx predicate, %.1f tile widths)".format(
            it.observationTime, it.excursionRms, it.timesTilePredicate, it.timesTileEdge
        ))
    }
    println("required lateral stiffness          %.4f pN/nm (leaf A1.1)".format(
        lateral.requiredStiffnessForPredicate
    ))
    println()
    println("--- gate 4, convergence in the basis degree ".padEnd(140, '-'))
    result.convergence.forEach {
        println("degree %3d   dishing %7.4f   area %7.4f   corner %7.4f".format(
            it.basisDegree, it.dishingRms, it.areaRms, it.cornerRms
        ))
    }
    println()
    println("--- gate 5, leaf A1.1 bound table ".padEnd(140, '-'))
    result.boundTable.forEach {
        println("sigma = %6.3f nm  ->  k >= %10.2f pN/nm   [%s]".format(
            it.positionalRms, it.requiredStiffness, it.leafA11Value
        ))
    }
    println()
    result.verdict.forEach { (key, value) -> println("$key: $value") }
    println()
    println("written: ${output.path}")
}
