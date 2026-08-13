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

package com.xemantic.nano.plentyofroom.brush

import com.xemantic.nano.plentyofroom.ELECTRON_VOLT
import com.xemantic.nano.plentyofroom.ROOM_TEMPERATURE
import com.xemantic.nano.plentyofroom.equipartitionRms
import com.xemantic.nano.plentyofroom.material.PegWater
import com.xemantic.nano.plentyofroom.thermalEnergy
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.doubleOrNull
import java.io.File
import java.util.concurrent.ForkJoinPool
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlin.math.floor
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.roundToLong

/**
 * Task `T-1d` / leaf `A2.1` — the Gen-1 layer response from a **numerical self-consistent-field
 * density profile**, against the same measurement-anchored interaction free energies `T-1c` used.
 *
 * Run with:
 *
 * ```shell
 * ./gradlew study -Pstudy=brush.ScfDensityProfileStudyKt
 * ```
 *
 * Emits `gpd/results/T-1d-scf-density-profile.json`, deterministically — no timestamp, and every
 * floating-point number rounded at the serialisation boundary, so that a re-run that changes
 * nothing produces no diff even though the sweep is executed on several threads.
 */

/** Which interaction free energy a response was computed with — `T-1c`'s bracket, unchanged. */
private enum class ScfInteractionChoice(val label: String, val exponent: String) {
    TWO_BODY("two-body", "2"),
    VIRIAL("virial", "2 + 3"),
    DES_CLOIZEAUX("des-Cloizeaux", "9/4")
}

/** Which family of density profiles the free energy was minimised over. */
private enum class ScfProfileChoice(val label: String) {
    BOX("alexander-box"),
    STRONG_STRETCHING("strong-stretching"),
    SCF("scf")
}

/** The response of one (profile, interaction) pair at one point of the design space. */
@Serializable
data class ScfLayerResponse(

    val profile: String,

    val interaction: String,

    /** The osmotic exponent that interaction carries — a consequence, not a setting. */
    val interactionExponent: String,

    /** `N` — inverted from the specified layer height through THIS profile model. */
    val monomersPerChain: Double,

    /** `N M₀` in g/mol. */
    val chainMolarMass: Double,

    /** `L₀/N` in nm — the height relation this model implies, with no convention prefactor. */
    val heightPerMonomer: Double,

    /** `φ = N σ v₀ / L₀`, the **mean** volume fraction of the unperturbed layer. */
    val meanVolumeFraction: Double,

    /** The largest volume fraction anywhere in the profile — `= φ` only for a box. */
    val peakVolumeFraction: Double,

    /** `2⟨z⟩` in nm — the first-moment thickness, exactly `L₀` for a box profile. */
    val firstMomentHeight: Double,

    /** `R₀ = b √N_K` in nm. */
    val idealEndToEnd: Double,

    /** `L₀/R₀` — the strong-stretching premise, as a number. */
    val stretchingRatio: Double,

    /**
     * `Σ = π R₀² σ` — the number of chains whose unperturbed coils overlap one coil footprint.
     *
     * The premise of a **one-dimensional** mean field, and the one criterion neither `T-1c` nor
     * `P-5` carries. `L₀/R₀ ≥ 1` does not detect its failure: an SCF layer whose resting height
     * is threshold-defined at the coil tail has `L₀/R₀ > 1` at *every* grafting density, including
     * densities at which the chains do not touch each other and the layer is a carpet of isolated
     * mushrooms with no lateral homogeneity for a 1-D profile to describe.
     */
    val coilOverlap: Double,

    /** `d lnΠ_int/d lnφ` of the grafted layer's own interaction at the mean volume fraction. */
    val interactionLocalExponent: Double,

    /** `k` in `pN/nm` at the resting height. */
    val equilibriumStiffness: Double,

    /** `k` in `pN/nm` at `h = 0.9 L₀`. */
    val stiffnessAtNineTenths: Double,

    /** `k` in `pN/nm` at `h = 0.8 L₀`. */
    val stiffnessAtFourFifths: Double,

    /** `k` in `pN/nm` at `h = 0.7 L₀`. */
    val stiffnessAtSevenTenths: Double,

    /** The height in nm at which the layer carries the §3 target force over the tile. */
    val heightUnderTargetForce: Double,

    /** `L₀ − h` in nm, the stroke the target force delivers. */
    val strokeUnderTargetForce: Double,

    /** `F/(L₀ − h)` in `pN/nm` — the stiffness that governs the stroke. */
    val secantStiffness: Double,

    /** `k(h)` in `pN/nm` at the working height — the stiffness that governs the noise. */
    val tangentStiffness: Double,

    /** `sqrt(k_BT/k)` in nm at the working point. */
    val positionalRms: Double,

    /** `φ` at the working height. */
    val workingVolumeFraction: Double
)

/** One point of the (layer height, grafting density) design space. */
@Serializable
data class ScfDesignPoint(
    val layerHeight: Double,
    val graftingDensity: Double,
    val graftingSpacing: Double,
    val responses: List<ScfLayerResponse>
)

/** The `σ` interval at one layer height over which a model satisfies both stated criteria. */
@Serializable
data class ScfStrokeWindow(
    val layerHeight: Double,
    val profile: String,
    val interaction: String,
    val requiredStroke: Double,
    val requiredStretchingRatio: Double,
    val requiredCoilOverlap: Double,
    val lowestGraftingDensity: Double?,
    val highestGraftingDensity: Double?,
    val empty: Boolean
)

/** One sample of the three density profiles at the same chain, grafting density and height. */
@Serializable
data class ScfProfileSample(
    val height: Double,
    val scf: Double,
    val alexanderBox: Double,
    val strongStretching: Double
)

/** The like-for-like profile comparison at one design point. */
@Serializable
data class ScfProfileComparison(
    val layerHeight: Double,
    val graftingDensity: Double,
    val monomersPerChain: Double,
    val interaction: String,
    val meanVolumeFraction: Double,
    val scfPeakVolumeFraction: Double,
    val scfFirstMomentHeight: Double,
    val boxFirstMomentHeight: Double,
    val strongStretchingFirstMomentHeight: Double,
    val coverageAboveTheWall: Double,
    val samples: List<ScfProfileSample>
)

/** How the answer moves with the threshold that defines the resting height. */
@Serializable
data class RestingLoadSensitivity(
    val restingLoad: Double,
    val layerHeight: Double,
    val graftingDensity: Double,
    val monomersPerChain: Double,
    val strokeUnderTargetForce: Double,
    val secantStiffness: Double,
    val stiffnessAtFourFifths: Double,
    val windowLowestGraftingDensity: Double?,
    val windowHighestGraftingDensity: Double?,
    val windowEmpty: Boolean
)

/** Absorbing against reflecting, the one boundary condition the task asks be justified. */
@Serializable
data class WallConditionSensitivity(
    val wallCondition: String,
    val layerHeight: Double,
    val graftingDensity: Double,
    val monomersPerChain: Double,
    val wallVolumeFraction: Double,
    val pressureAtNineTenths: Double,
    val strokeUnderTargetForce: Double,
    val secantStiffness: Double
)

/** Gate 4, as data rather than as an assertion. */
@Serializable
data class ScfConvergenceRow(
    val axis: String,
    val nodeSpacing: Double,
    val contourStepsPerMonomer: Double,
    val nodes: Int,
    val contourSteps: Int,
    val iterations: Int,
    val converged: Boolean,
    val residual: Double,

    /** `log10` of the residual, because the rounding floor would report `1e−11` as zero. */
    val residualExponent: Double,

    val coverageError: Double,

    /** `log10` of the coverage error, for the same reason. */
    val coverageErrorExponent: Double,

    val pressure: Double,
    val relativeError: Double,
    val observedOrder: Double?
)

/** The two independent pressure routes, checked against each other. */
@Serializable
data class PressureRouteCheck(
    val layerHeight: Double,
    val thermodynamicPressure: Double,
    val contactPressure: Double,
    val ratio: Double
)

/** What `C-0003` said at the same design point, and what the SCF profile says instead. */
@Serializable
data class StandingClaimCheck(
    val quantity: String,
    val standingLow: Double,
    val standingHigh: Double,
    val selfConsistentField: Double,
    val insideStandingBracket: Boolean
)

@Serializable
data class ScfDensityProfileResult(
    val task: String,
    val leaf: String,
    val title: String,
    val verificationType: String,
    val acceptance: String,
    val maturity: String,
    val units: Map<String, String>,
    val conventions: List<String>,
    val validity: List<String>,
    val parameters: Map<String, String>,
    val profileComparison: List<ScfProfileComparison>,
    val convergence: List<ScfConvergenceRow>,
    val pressureRoutes: List<PressureRouteCheck>,
    val restingLoadSensitivity: List<RestingLoadSensitivity>,
    val wallConditionSensitivity: List<WallConditionSensitivity>,
    val standingClaimCheck: List<StandingClaimCheck>,
    val strokeWindows: List<ScfStrokeWindow>,
    val designPoints: List<ScfDesignPoint>
)

private const val TILE_EDGE = 40.0

private const val TARGET_FORCE = 100.0

private const val ACCEPTABLE_STROKE = 3.0

private const val DESIRED_STROKE = 10.0

private val LAYER_HEIGHTS = listOf(5.0, 7.0, 10.0)

private const val GRAFTING_DENSITY_MIN = 0.002

private const val GRAFTING_DENSITY_MAX = 1.0

private const val GRAFTING_DENSITY_SAMPLES = 61

/**
 * The load in pN over the 40 × 40 nm tile at which the layer is declared to be at rest.
 *
 * `T-1c`'s two profile models have a sharp `L₀` because both are trial functions that terminate;
 * an SCF layer has a real decaying tail and reaches `P = 0` only asymptotically, so the resting
 * height does not exist without a threshold. One percent of the §3 target force is the primary
 * value and the sensitivity block carries a decade either side.
 */
private const val PRIMARY_RESTING_LOAD = 1.0

private val RESTING_LOADS = listOf(0.1, 1.0, 10.0)

private val PRODUCTION_GRID = ScfDiscretisation(
    nodeSpacing = 0.2,
    contourStepsPerMonomer = 2.0
)

/** `A₂` in `mol·cm³/g²` for PEG in water at 25 °C — MEASURED, via `C-0003`. */
private const val OSMOTIC_SECOND_VIRIAL = 1.9e-3

/** `A₃` in `cm⁶·mol/g³`, same convention and same source chain — MEASURED, via `C-0003`. */
private const val OSMOTIC_THIRD_VIRIAL = 2.0e-2

/** The `C-0001`/`C-0003` 10 nm design point, so every comparison is like for like. */
private const val REFERENCE_HEIGHT = 10.0

private const val REFERENCE_DENSITY = 0.024

private val SWEPT_MODELS = listOf(
    ScfProfileChoice.SCF to ScfInteractionChoice.TWO_BODY,
    ScfProfileChoice.SCF to ScfInteractionChoice.VIRIAL,
    ScfProfileChoice.SCF to ScfInteractionChoice.DES_CLOIZEAUX,
    ScfProfileChoice.BOX to ScfInteractionChoice.DES_CLOIZEAUX,
    ScfProfileChoice.STRONG_STRETCHING to ScfInteractionChoice.DES_CLOIZEAUX
)

private const val WORKER_THREADS = 4

fun main() {
    val peg = PegWater()
    val tileArea = TILE_EDGE * TILE_EDGE
    val densities = logarithmicSweep(
        GRAFTING_DENSITY_MIN, GRAFTING_DENSITY_MAX, GRAFTING_DENSITY_SAMPLES
    )
    val started = System.nanoTime()
    val tasks = LAYER_HEIGHTS.flatMap { height -> densities.map { height to it } }
    val pool = ForkJoinPool(WORKER_THREADS)
    val designPoints = try {
        pool.submit<List<ScfDesignPoint>> {
            tasks.parallelStream().map { (height, density) ->
                ScfDesignPoint(
                    layerHeight = height,
                    graftingDensity = density,
                    graftingSpacing = 1.0 / density.pow(0.5),
                    responses = SWEPT_MODELS.map { (profile, choice) ->
                        response(peg, profile, choice, height, density, tileArea)
                    }
                )
            }.toList()
        }.get(6, TimeUnit.HOURS)
    } finally {
        pool.shutdown()
    }
    val ordered = tasks.map { (height, density) ->
        designPoints.first { it.layerHeight == height && it.graftingDensity == density }
    }
    val result = ScfDensityProfileResult(
        task = "T-1d",
        leaf = "A2.1",
        title = "Numerical self-consistent-field density profile for the Gen-1 grafted layer",
        verificationType = "in-silico (numerical SCF, Edwards propagator), against the same " +
                "measurement-anchored interaction free energies as T-1c",
        acceptance = "the 10 nm design window decided by a profile whose premise is met: the " +
                "density profile solved rather than assumed, the interaction bracket of C-0003 " +
                "carried unchanged, convergence demonstrated as an order rather than asserted, " +
                "and the two pressure routes checked against each other",
        maturity = "TRL 1-3 — model-consistent and traceable, NOT empirically demonstrated. " +
                "The interaction free energy and the chain statistics are anchored to " +
                "measurement; nothing about THIS layer is measured.",
        units = mapOf(
            "length" to "nm",
            "force" to "pN",
            "energy" to "pN*nm",
            "pressure" to "pN/nm^2 (= MPa)",
            "stiffness" to "pN/nm (= mN/m)",
            "graftingDensity" to "chains/nm^2",
            "molarMass" to "g/mol",
            "volume" to "nm^3",
            "temperature" to "K"
        ),
        conventions = listOf(
            "z normal to the electrode, positive away from it, origin at the electrode surface",
            "chains grafted at z = 0, one end fixed and the other free",
            "the tile is a rigid non-adsorbing wall at height h; compression means h < L0",
            "disjoining pressure positive when the layer pushes the tile away",
            "stiffness k = -dF/dh = -A dP/dh, positive for a restoring layer",
            "the layer height is the independent variable; N follows from it, by inverting the " +
                    "resting height of EACH profile model",
            "phi ALWAYS means the physical volume fraction, resolved in z for the SCF profile " +
                    "and equal to N sigma v0/h only on average",
            "a GRAFTED layer carries no chain translational entropy, so the van't Hoff limb of " +
                    "the measured equation of state is removed before the free energy is used",
            "the propagator is ABSORBING at both the grafting surface and the tile, which is " +
                    "what a rigid impenetrable wall is; the reflecting alternative is the " +
                    "mid-plane of two identical brushes and is carried as a sensitivity only",
            "L0 for the SCF profile is DEFINED as the height at which the layer carries " +
                    "$PRIMARY_RESTING_LOAD pN over the 40 x 40 nm tile, because an SCF layer " +
                    "reaches P = 0 only asymptotically; the threshold travels with every number"
        ),
        validity = listOf(
            "N sigma v0 / 0.8 < h — below that the layer would be a melt on average and the " +
                    "equation of state is far outside the 0-50 wt % it was fitted over",
            "THE STRONG-STRETCHING PREMISE IS STILL NOT MET, and that is now a statement about " +
                    "T-1c rather than about this task: L0/R0 stays of order one across the whole " +
                    "design space. The SCF profile does not need it.",
            "the chain is treated as GAUSSIAN on the measured Kuhn parameters, which C-0003 " +
                    "earns rather than assumes: 0.02-0.10 thermal blobs per chain",
            "MEAN FIELD: no fluctuation corrections, no lateral inhomogeneity, no correlation " +
                    "hole. The ground-state-dominance approximation is NOT made — the full " +
                    "contour-resolved propagator is used — but the field is a mean field",
            "the interaction free energy below phi# is NOT measured; the two-body and des " +
                    "Cloizeaux limbs are carried as a bracket, exactly as in T-1c",
            "every osmotic input is a BULK property applied to a BRUSH (P-9)",
            "the equation of state was fitted to LINEAR PEG in PURE WATER; the Gen-1 buffer is " +
                    "2-10 mM MgCl2 and the §3 chemistry allows a PS->PEG block copolymer",
            "purely mechanical: no electrostatics, no ion partitioning, no poroelasticity, " +
                    "no tile compliance"
        ),
        parameters = mapOf(
            "temperature" to ROOM_TEMPERATURE.toString(),
            "medium" to "aqueous buffer, 2/5/10 mM MgCl2 (not entering this task)",
            "thermalEnergy" to thermalEnergy().toString(),
            "thermalEnergyElectronVolts" to (thermalEnergy() / ELECTRON_VOLT).toString(),
            "tileFootprint" to "${TILE_EDGE.toInt()} x ${TILE_EDGE.toInt()} nm",
            "tileArea" to tileArea.toString(),
            "targetForce" to TARGET_FORCE.toString(),
            "targetStrokeAcceptable" to ACCEPTABLE_STROKE.toString(),
            "targetStrokeDesired" to DESIRED_STROKE.toString(),
            "restingLoad" to PRIMARY_RESTING_LOAD.toString(),
            "restingLoadSensitivity" to RESTING_LOADS.toString(),
            "monomerVolume" to peg.monomerVolume.toString(),
            "kuhnLength" to peg.kuhnLength.toString(),
            "monomersPerKuhnSegment" to peg.monomersPerKuhnSegment.toString(),
            "contourDiffusion" to (peg.kuhnLength * peg.kuhnLength /
                    (6.0 * peg.monomersPerKuhnSegment)).toString(),
            "contourDiffusionMeaning" to "b^2/(6 n_K) in nm^2 per monomer, so that <R^2> = " +
                    "6 D N = N_K b^2 exactly — the Edwards diffusion coefficient written on the " +
                    "MEASURED Kuhn parameters of C-0002",
            "crossoverIndex" to peg.crossoverIndex.toString(),
            "osmoticSecondVirial" to OSMOTIC_SECOND_VIRIAL.toString(),
            "osmoticThirdVirial" to OSMOTIC_THIRD_VIRIAL.toString(),
            "virialProvenance" to "MEASURED, PEG/water 25 C, convention Pi/(RT) = c/M + A2 c^2 " +
                    "+ A3 c^3 with c in g/cm^3 and NO factor of two — carried unchanged from " +
                    "C-0003 so that this profile and T-1c's differ ONLY in the profile",
            "layerHeights" to LAYER_HEIGHTS.toString(),
            "graftingDensityRange" to listOf(GRAFTING_DENSITY_MIN, GRAFTING_DENSITY_MAX).toString(),
            "graftingDensitySamples" to GRAFTING_DENSITY_SAMPLES.toString(),
            "nodeSpacing" to PRODUCTION_GRID.nodeSpacing.toString(),
            "contourStepsPerMonomer" to PRODUCTION_GRID.contourStepsPerMonomer.toString(),
            "maximumDiffusionRatio" to PRODUCTION_GRID.maximumDiffusionRatio.toString(),
            "selfConsistencyTolerance" to PRODUCTION_GRID.tolerance.toString(),
            "workerThreads" to WORKER_THREADS.toString()
        ),
        profileComparison = ScfInteractionChoice.entries.map {
            profileComparison(peg, it, tileArea)
        },
        convergence = convergence(peg),
        pressureRoutes = pressureRoutes(peg),
        restingLoadSensitivity = RESTING_LOADS.map {
            restingLoadSensitivity(peg, it, densities, tileArea)
        },
        wallConditionSensitivity = ScfWallCondition.entries.map {
            wallConditionSensitivity(peg, it, tileArea)
        },
        standingClaimCheck = standingClaimCheck(ordered),
        strokeWindows = strokeWindows(ordered),
        designPoints = ordered
    )
    val elapsed = (System.nanoTime() - started) / 1e9
    val json = Json { prettyPrint = true }
    val output = File("gpd/results/T-1d-scf-density-profile.json")
    output.parentFile.mkdirs()
    output.writeText(
        json.encodeToString(
            json.parseToJsonElement(json.encodeToString(result)).roundedForResult()
        ) + "\n"
    )
    report(result, output, elapsed)
}

private fun interactionFor(peg: PegWater, choice: ScfInteractionChoice): InteractionFreeEnergy {
    val twoBody = twoBodyInteraction(
        peg.reducedSecondVirialCoefficient(OSMOTIC_SECOND_VIRIAL), peg.monomerVolume
    )
    val threeBody = threeBodyInteraction(
        peg.reducedThirdVirialCoefficient(OSMOTIC_THIRD_VIRIAL), peg.monomerVolume
    )
    return when (choice) {
        ScfInteractionChoice.TWO_BODY -> twoBody
        ScfInteractionChoice.VIRIAL -> additiveInteraction("virial", listOf(twoBody, threeBody))
        ScfInteractionChoice.DES_CLOIZEAUX ->
            desCloizeauxInteraction(peg.crossoverIndex, peg.monomerVolume)
    }
}

private fun layerModel(
    profile: ScfProfileChoice,
    interaction: InteractionFreeEnergy,
    restingLoad: Double = PRIMARY_RESTING_LOAD,
    wallCondition: ScfWallCondition = ScfWallCondition.ABSORBING
): GraftedLayerModel = when (profile) {
    ScfProfileChoice.BOX -> AlexanderBoxLayer(interaction)
    ScfProfileChoice.STRONG_STRETCHING -> StrongStretchingLayer(interaction)
    ScfProfileChoice.SCF -> SelfConsistentFieldLayer(
        interaction, PRODUCTION_GRID, restingLoad / (TILE_EDGE * TILE_EDGE), wallCondition
    )
}

/**
 * The chain length whose resting height is [height], through the root finder each model needs.
 *
 * `L₀ ∝ N` exactly for the two analytic profiles, so the fixed point of [chainLengthForHeight]
 * lands in one pass; the SCF resting height goes as roughly `N^0.55`, so it needs bracketing.
 */
private fun chainLengthFor(
    model: GraftedLayerModel,
    peg: PegWater,
    height: Double,
    graftingDensity: Double
): Double = if (model is SelfConsistentFieldLayer) {
    model.chainLengthAtRestingHeight(peg, height, graftingDensity)
} else model.chainLengthForHeight(peg, height, graftingDensity)

/** The height at which [chain] carries the §3 target force over [tileArea]. */
private fun workingHeight(
    model: GraftedLayerModel,
    chain: GraftedChain,
    tileArea: Double
): Double = if (model is SelfConsistentFieldLayer) {
    model.heightAtPressure(chain, TARGET_FORCE / tileArea)
} else model.heightUnderLoad(chain, TARGET_FORCE, tileArea)

/** `d lnΠ_int/d lnφ` of [interaction] at [volumeFraction]. */
private fun localExponent(interaction: InteractionFreeEnergy, volumeFraction: Double): Double =
    interaction.osmoticPressureSlope(volumeFraction) * volumeFraction /
            interaction.osmoticPressure(volumeFraction)

private fun response(
    peg: PegWater,
    profile: ScfProfileChoice,
    choice: ScfInteractionChoice,
    layerHeight: Double,
    graftingDensity: Double,
    tileArea: Double,
    restingLoad: Double = PRIMARY_RESTING_LOAD
): ScfLayerResponse {
    val model = layerModel(profile, interactionFor(peg, choice), restingLoad)
    val length = chainLengthFor(model, peg, layerHeight, graftingDensity)
    val chain = peg.graftedChain(length, graftingDensity)
    val equilibrium = model.equilibriumHeight(chain)
    val working = workingHeight(model, chain, tileArea)
    val tangent = model.stiffness(chain, working, tileArea)
    val scfProfile = (model as? SelfConsistentFieldLayer)?.profile(chain, equilibrium)
    return ScfLayerResponse(
        profile = profile.label,
        interaction = choice.label,
        interactionExponent = choice.exponent,
        monomersPerChain = length,
        chainMolarMass = length * peg.monomerMolarMass,
        heightPerMonomer = equilibrium / length,
        meanVolumeFraction = chain.meanVolumeFraction(equilibrium),
        peakVolumeFraction = scfProfile?.peakVolumeFraction
            ?: peakVolumeFraction(model, chain, equilibrium),
        firstMomentHeight = scfProfile?.firstMomentHeight
            ?: firstMomentHeight(model, chain, equilibrium),
        idealEndToEnd = chain.idealEndToEnd,
        stretchingRatio = chain.stretchingRatio(equilibrium),
        coilOverlap = PI * chain.idealEndToEnd * chain.idealEndToEnd * graftingDensity,
        interactionLocalExponent = localExponent(
            model.interaction, chain.meanVolumeFraction(equilibrium)
        ),
        equilibriumStiffness = model.stiffness(chain, equilibrium, tileArea),
        stiffnessAtNineTenths = model.stiffness(chain, 0.9 * equilibrium, tileArea),
        stiffnessAtFourFifths = model.stiffness(chain, 0.8 * equilibrium, tileArea),
        stiffnessAtSevenTenths = model.stiffness(chain, 0.7 * equilibrium, tileArea),
        heightUnderTargetForce = working,
        strokeUnderTargetForce = equilibrium - working,
        secantStiffness = TARGET_FORCE / (equilibrium - working),
        tangentStiffness = tangent,
        positionalRms = equipartitionRms(tangent),
        workingVolumeFraction = chain.meanVolumeFraction(working)
    )
}

/** The largest volume fraction of an analytic profile — the box value, or the parabola apex. */
private fun peakVolumeFraction(
    model: GraftedLayerModel,
    chain: GraftedChain,
    height: Double
): Double = when (model) {
    is AlexanderBoxLayer -> chain.meanVolumeFraction(height)
    is StrongStretchingLayer -> model.volumeFractionAt(chain, height, 0.0)
    else -> chain.meanVolumeFraction(height)
}

/** `2⟨z⟩` of an analytic profile, by the same definition the SCF profile reports. */
private fun firstMomentHeight(
    model: GraftedLayerModel,
    chain: GraftedChain,
    height: Double
): Double = when (model) {
    is AlexanderBoxLayer -> height
    is StrongStretchingLayer -> {
        val panels = 2048
        var moment = 0.0
        var total = 0.0
        (0..panels).forEach { i ->
            val weight = if (i == 0 || i == panels) 1.0 else if (i % 2 == 1) 4.0 else 2.0
            val z = height * i / panels
            val value = model.volumeFractionAt(chain, height, z)
            moment += weight * z * value
            total += weight * value
        }
        2.0 * moment / total
    }
    else -> height
}

private fun profileComparison(
    peg: PegWater,
    choice: ScfInteractionChoice,
    tileArea: Double
): ScfProfileComparison {
    val interaction = interactionFor(peg, choice)
    val scf = SelfConsistentFieldLayer(
        interaction, PRODUCTION_GRID, PRIMARY_RESTING_LOAD / tileArea
    )
    val length = scf.chainLengthAtRestingHeight(peg, REFERENCE_HEIGHT, REFERENCE_DENSITY)
    val chain = peg.graftedChain(length, REFERENCE_DENSITY)
    val box = AlexanderBoxLayer(interaction)
    val sst = StrongStretchingLayer(interaction)
    val boxHeight = box.equilibriumHeight(chain)
    val sstHeight = sst.equilibriumHeight(chain)
    val profile = scf.profile(chain, REFERENCE_HEIGHT)
    val samples = (1..40).map { step ->
        val z = REFERENCE_HEIGHT * step / 40.0
        ScfProfileSample(
            height = z,
            scf = profile.volumeFractionAt(z),
            alexanderBox = if (z <= boxHeight) chain.meanVolumeFraction(boxHeight) else 0.0,
            strongStretching = if (z <= sstHeight) {
                sst.volumeFractionAt(chain, sstHeight, z)
            } else 0.0
        )
    }
    val tallProfile = scf.profile(chain, 3.0 * REFERENCE_HEIGHT)
    var beyond = 0.0
    (0 until tallProfile.nodes).forEach { node ->
        val z = (node + 1) * tallProfile.nodeSpacing
        if (z > REFERENCE_HEIGHT) beyond += tallProfile.volumeFraction[node]
    }
    beyond *= tallProfile.nodeSpacing / peg.monomerVolume
    return ScfProfileComparison(
        layerHeight = REFERENCE_HEIGHT,
        graftingDensity = REFERENCE_DENSITY,
        monomersPerChain = length,
        interaction = choice.label,
        meanVolumeFraction = chain.meanVolumeFraction(REFERENCE_HEIGHT),
        scfPeakVolumeFraction = profile.peakVolumeFraction,
        scfFirstMomentHeight = tallProfile.firstMomentHeight,
        boxFirstMomentHeight = boxHeight,
        strongStretchingFirstMomentHeight = firstMomentHeight(sst, chain, sstHeight),
        coverageAboveTheWall = beyond / chain.coverage,
        samples = samples
    )
}

private fun convergence(peg: PegWater): List<ScfConvergenceRow> {
    val interaction = interactionFor(peg, ScfInteractionChoice.DES_CLOIZEAUX)
    val chain = peg.graftedChain(250.0, REFERENCE_DENSITY)
    val height = REFERENCE_HEIGHT
    val spatialReference = SelfConsistentFieldLayer(
        interaction, ScfDiscretisation(nodeSpacing = 0.05, contourStepsPerMonomer = 8.0)
    ).pressureAt(chain, height)
    val spatial = listOf(0.4, 0.2, 0.1).map { spacing ->
        val grid = ScfDiscretisation(nodeSpacing = spacing, contourStepsPerMonomer = 8.0)
        row("nodeSpacing", peg, interaction, chain, height, grid, spatialReference)
    }
    val contourReference = SelfConsistentFieldLayer(
        interaction, ScfDiscretisation(nodeSpacing = 0.4, contourStepsPerMonomer = 16.0)
    ).pressureAt(chain, height)
    val contour = listOf(0.5, 1.0, 2.0).map { steps ->
        val grid = ScfDiscretisation(nodeSpacing = 0.4, contourStepsPerMonomer = steps)
        row("contourStep", peg, interaction, chain, height, grid, contourReference)
    }
    return withOrders(spatial, 2.0) + withOrders(contour, 2.0)
}

private fun row(
    axis: String,
    peg: PegWater,
    interaction: InteractionFreeEnergy,
    chain: GraftedChain,
    height: Double,
    grid: ScfDiscretisation,
    reference: Double
): ScfConvergenceRow {
    val layer = SelfConsistentFieldLayer(interaction, grid)
    val profile = layer.profile(chain, height)
    val pressure = layer.pressureAt(chain, height)
    return ScfConvergenceRow(
        axis = axis,
        nodeSpacing = grid.nodeSpacing,
        contourStepsPerMonomer = grid.contourStepsPerMonomer,
        nodes = profile.nodes,
        contourSteps = contourSteps(chain, profile.nodeSpacing, grid),
        iterations = profile.iterations,
        converged = profile.converged,
        residual = profile.residual,
        residualExponent = log10(profile.residual),
        coverageError = abs(profile.coverage - chain.coverage) / chain.coverage,
        coverageErrorExponent = log10(
            abs(profile.coverage - chain.coverage) / chain.coverage
        ),
        pressure = pressure,
        relativeError = abs(pressure - reference) / reference,
        observedOrder = null
    )
}

/** Fills in the observed convergence order of successive rows on the same axis. */
private fun withOrders(rows: List<ScfConvergenceRow>, refinement: Double): List<ScfConvergenceRow> =
    rows.mapIndexed { index, row ->
        if (index == 0) row
        else row.copy(
            observedOrder = ln(rows[index - 1].relativeError / row.relativeError) /
                    ln(refinement)
        )
    }

private fun pressureRoutes(peg: PegWater): List<PressureRouteCheck> {
    val interaction = interactionFor(peg, ScfInteractionChoice.DES_CLOIZEAUX)
    val layer = SelfConsistentFieldLayer(interaction, PRODUCTION_GRID)
    val chain = peg.graftedChain(250.0, REFERENCE_DENSITY)
    return listOf(6.0, 8.0, 10.0, 13.0, 16.0).map { height ->
        val thermodynamic = layer.pressureAt(chain, height)
        val contact = layer.profile(chain, height).contactPressure
        PressureRouteCheck(
            layerHeight = height,
            thermodynamicPressure = thermodynamic,
            contactPressure = contact,
            ratio = contact / thermodynamic
        )
    }
}

private fun restingLoadSensitivity(
    peg: PegWater,
    restingLoad: Double,
    densities: List<Double>,
    tileArea: Double
): RestingLoadSensitivity {
    val at = response(
        peg, ScfProfileChoice.SCF, ScfInteractionChoice.DES_CLOIZEAUX,
        REFERENCE_HEIGHT, REFERENCE_DENSITY, tileArea, restingLoad
    )
    val surviving = densities.mapNotNull { density ->
        val candidate = response(
            peg, ScfProfileChoice.SCF, ScfInteractionChoice.DES_CLOIZEAUX,
            REFERENCE_HEIGHT, density, tileArea, restingLoad
        )
        if (candidate.strokeUnderTargetForce >= ACCEPTABLE_STROKE &&
            candidate.stretchingRatio >= 1.0 && candidate.coilOverlap >= 1.0
        ) density else null
    }
    return RestingLoadSensitivity(
        restingLoad = restingLoad,
        layerHeight = REFERENCE_HEIGHT,
        graftingDensity = REFERENCE_DENSITY,
        monomersPerChain = at.monomersPerChain,
        strokeUnderTargetForce = at.strokeUnderTargetForce,
        secantStiffness = at.secantStiffness,
        stiffnessAtFourFifths = at.stiffnessAtFourFifths,
        windowLowestGraftingDensity = surviving.minOrNull(),
        windowHighestGraftingDensity = surviving.maxOrNull(),
        windowEmpty = surviving.isEmpty()
    )
}

private fun wallConditionSensitivity(
    peg: PegWater,
    wallCondition: ScfWallCondition,
    tileArea: Double
): WallConditionSensitivity {
    val interaction = interactionFor(peg, ScfInteractionChoice.DES_CLOIZEAUX)
    val model = SelfConsistentFieldLayer(
        interaction, PRODUCTION_GRID, PRIMARY_RESTING_LOAD / tileArea, wallCondition
    )
    val length = model.chainLengthAtRestingHeight(peg, REFERENCE_HEIGHT, REFERENCE_DENSITY)
    val chain = peg.graftedChain(length, REFERENCE_DENSITY)
    val equilibrium = model.equilibriumHeight(chain)
    val working = model.heightAtPressure(chain, TARGET_FORCE / tileArea)
    return WallConditionSensitivity(
        wallCondition = wallCondition.name,
        layerHeight = REFERENCE_HEIGHT,
        graftingDensity = REFERENCE_DENSITY,
        monomersPerChain = length,
        wallVolumeFraction = model.profile(chain, 0.9 * equilibrium).wallVolumeFraction,
        pressureAtNineTenths = model.disjoiningPressure(chain, 0.9 * equilibrium),
        strokeUnderTargetForce = equilibrium - working,
        secantStiffness = TARGET_FORCE / (equilibrium - working)
    )
}

/** `C-0003`'s headline brackets at the 10 nm design point, against what the SCF profile gives. */
private fun standingClaimCheck(designPoints: List<ScfDesignPoint>): List<StandingClaimCheck> {
    val nearest = designPoints
        .filter { it.layerHeight == REFERENCE_HEIGHT }
        .minByOrNull { abs(ln(it.graftingDensity / REFERENCE_DENSITY)) }
        ?: return emptyList()
    val scf = nearest.responses.first {
        it.profile == ScfProfileChoice.SCF.label &&
                it.interaction == ScfInteractionChoice.DES_CLOIZEAUX.label
    }
    return listOf(
        StandingClaimCheck("monomersPerChain", 224.8, 374.3, scf.monomersPerChain, false),
        StandingClaimCheck("strokeAt100pN", 3.83, 6.01, scf.strokeUnderTargetForce, false),
        StandingClaimCheck("stiffnessAtFourFifths", 7.0, 24.0, scf.stiffnessAtFourFifths, false),
        StandingClaimCheck("secantStiffness", 16.6, 26.1, scf.secantStiffness, false),
        StandingClaimCheck("meanVolumeFraction", 0.0326, 0.0543, scf.meanVolumeFraction, false)
    ).map { it.copy(insideStandingBracket = it.selfConsistentField in it.standingLow..it.standingHigh) }
}

/**
 * `(required stroke, required L₀/R₀, required Σ)`.
 *
 * The third entry is new here. `P-5` adopted `L₀/R₀ ≥ 1` as the brush criterion on `T-1c`'s
 * recommendation, and against an SCF profile that criterion turns out to be **vacuous** — it is
 * satisfied at every grafting density in the sweep, including ones where the coils do not touch.
 * Coil overlap `Σ ≥ 1` is carried alongside it, and windows are emitted under each so the two
 * contributions stay separable, exactly as `T-1c` kept the stretching criterion separable.
 */
private val WINDOW_CRITERIA = listOf(
    Triple(ACCEPTABLE_STROKE, 0.0, 0.0),
    Triple(ACCEPTABLE_STROKE, 1.0, 0.0),
    Triple(ACCEPTABLE_STROKE, 1.0, 1.0),
    Triple(DESIRED_STROKE, 0.0, 0.0),
    Triple(DESIRED_STROKE, 1.0, 1.0)
)

private fun strokeWindows(designPoints: List<ScfDesignPoint>): List<ScfStrokeWindow> =
    LAYER_HEIGHTS.flatMap { height ->
        SWEPT_MODELS.flatMap { (profile, choice) ->
            WINDOW_CRITERIA.map { (required, stretching, overlap) ->
                val surviving = designPoints
                    .filter { it.layerHeight == height }
                    .mapNotNull { point ->
                        val response = point.responses.first {
                            it.profile == profile.label && it.interaction == choice.label
                        }
                        if (response.strokeUnderTargetForce >= required &&
                            response.stretchingRatio >= stretching &&
                            response.coilOverlap >= overlap
                        ) point.graftingDensity else null
                    }
                ScfStrokeWindow(
                    layerHeight = height,
                    profile = profile.label,
                    interaction = choice.label,
                    requiredStroke = required,
                    requiredStretchingRatio = stretching,
                    requiredCoilOverlap = overlap,
                    lowestGraftingDensity = surviving.minOrNull(),
                    highestGraftingDensity = surviving.maxOrNull(),
                    empty = surviving.isEmpty()
                )
            }
        }
    }

private fun report(result: ScfDensityProfileResult, output: File, elapsed: Double) {
    println("T-1d / A2.1 — ${result.title}")
    println("300 K, aqueous buffer, k_BT = ${"%.3f".format(thermalEnergy())} pN*nm")
    println()
    println("--- the profile, at L0 = 10 nm, sigma = 0.024 nm^-2 ".padEnd(96, '-'))
    result.profileComparison.forEach {
        println(
            ("%-14s N = %6.1f  phi_mean = %.4f  phi_peak = %.4f  2<z> = %5.2f nm  " +
                    "box = %5.2f  SST = %5.2f  coverage above 10 nm = %4.1f %%").format(
                it.interaction, it.monomersPerChain, it.meanVolumeFraction,
                it.scfPeakVolumeFraction, it.scfFirstMomentHeight, it.boxFirstMomentHeight,
                it.strongStretchingFirstMomentHeight, 100.0 * it.coverageAboveTheWall
            )
        )
    }
    println()
    println("--- gate 4, convergence ".padEnd(96, '-'))
    println(
        "%-12s %8s %8s %6s %8s %6s %10s %11s %8s".format(
            "axis", "dz", "steps/N", "nodes", "contour", "iters", "coverage", "rel error", "order"
        )
    )
    result.convergence.forEach {
        println(
            "%-12s %8.3f %8.1f %6d %8d %6d %10.1f %10.1f %11.3e %8s".format(
                it.axis, it.nodeSpacing, it.contourStepsPerMonomer, it.nodes, it.contourSteps,
                it.iterations, it.residualExponent, it.coverageErrorExponent, it.relativeError,
                it.observedOrder?.let { order -> "%.2f".format(order) } ?: "-"
            )
        )
    }
    println()
    println("--- the two pressure routes ".padEnd(96, '-'))
    result.pressureRoutes.forEach {
        println(
            "h = %5.1f nm  -dF/dh = %.5f MPa  contact = %.5f MPa  ratio = %.4f".format(
                it.layerHeight, it.thermodynamicPressure, it.contactPressure, it.ratio
            )
        )
    }
    println()
    LAYER_HEIGHTS.forEach { height ->
        println("--- layer height L0 = $height nm ".padEnd(96, '-'))
        println(
            "%9s %-18s %-14s %7s %7s %8s %8s %9s %9s".format(
                "sigma", "profile", "interaction", "N", "L0/R0", "Sigma",
                "k(0.8L0)", "stroke", "k_sec"
            )
        )
        result.designPoints
            .filter { it.layerHeight == height }
            .filter { it.graftingDensity > 0.008 && it.graftingDensity < 0.3 }
            .forEach { point ->
                point.responses.forEach { response ->
                    println(
                        "%9.4f %-18s %-14s %7.1f %7.2f %8.4f %8.2f %9.2f %9.2f".format(
                            point.graftingDensity, response.profile, response.interaction,
                            response.monomersPerChain, response.stretchingRatio,
                            response.coilOverlap, response.stiffnessAtFourFifths,
                            response.strokeUnderTargetForce, response.secantStiffness
                        )
                    )
                }
            }
        println()
    }
    println("--- design windows in sigma [nm^-2] ".padEnd(96, '-'))
    result.strokeWindows.filter { it.requiredStroke == ACCEPTABLE_STROKE }.forEach {
        val window = if (it.empty) "EMPTY"
        else "[${"%.4f".format(it.lowestGraftingDensity)}, " +
                "${"%.4f".format(it.highestGraftingDensity)}]"
        println(
            ("L0 = %5.1f nm  %-18s %-14s stroke >= %4.1f nm, L0/R0 >= %3.1f, " +
                    "Sigma >= %3.1f : %s").format(
                it.layerHeight, it.profile, it.interaction, it.requiredStroke,
                it.requiredStretchingRatio, it.requiredCoilOverlap, window
            )
        )
    }
    println(
        "stroke >= ${DESIRED_STROKE.toInt()} nm anywhere: " +
                result.strokeWindows.filter { it.requiredStroke == DESIRED_STROKE }
                    .any { !it.empty }
    )
    println()
    println("--- sensitivity to the resting-load threshold that DEFINES L0 ".padEnd(96, '-'))
    result.restingLoadSensitivity.forEach {
        val window = if (it.windowEmpty) "EMPTY"
        else "[${"%.4f".format(it.windowLowestGraftingDensity)}, " +
                "${"%.4f".format(it.windowHighestGraftingDensity)}]"
        println(
            ("resting load %5.2f pN : N = %6.1f  stroke = %5.2f nm  " +
                    "k_sec = %6.2f pN/nm  window %s").format(
                it.restingLoad, it.monomersPerChain, it.strokeUnderTargetForce,
                it.secantStiffness, window
            )
        )
    }
    println()
    println("--- sensitivity to the wall boundary condition ".padEnd(96, '-'))
    result.wallConditionSensitivity.forEach {
        println(
            "%-11s N = %6.1f  phi(wall) at 0.9 L0 = %.5f  P = %.5f MPa  stroke = %5.2f nm".format(
                it.wallCondition, it.monomersPerChain, it.wallVolumeFraction,
                it.pressureAtNineTenths, it.strokeUnderTargetForce
            )
        )
    }
    println()
    println("--- does C-0003 survive? ".padEnd(96, '-'))
    result.standingClaimCheck.forEach {
        println(
            "%-24s C-0003 [%9.4f, %9.4f]  SCF %9.4f  %s".format(
                it.quantity, it.standingLow, it.standingHigh, it.selfConsistentField,
                if (it.insideStandingBracket) "inside" else "OUTSIDE"
            )
        )
    }
    println()
    println("written: ${output.path} (${result.designPoints.size} design points, " +
            "${result.designPoints.sumOf { it.responses.size }} responses) " +
            "in ${"%.1f".format(elapsed)} s")
}

/**
 * The number of significant digits every floating-point number in this result is rounded to.
 *
 * The same reproducibility device `structure/ResultRounding.kt` introduced, restated here rather
 * than imported because this task does not own that package. It matters more here than it did
 * there: the sweep is executed on several threads, so the summation order of a reduction is not
 * even fixed within one run, and a bare `Double` would make the file a function of the thread
 * schedule rather than of the model.
 */
private const val RESULT_SIGNIFICANT_DIGITS = 9

/** The magnitude below which a result is reported as exactly zero. */
private const val RESULT_ABSOLUTE_FLOOR = 1e-9

private fun roundForResult(value: Double): Double {
    if (!value.isFinite()) return value
    if (abs(value) < RESULT_ABSOLUTE_FLOOR) return 0.0
    val scale = 10.0.pow(RESULT_SIGNIFICANT_DIGITS - 1 - floor(log10(abs(value))))
    return (value * scale).roundToLong() / scale
}

/** Returns this element with every non-integral number rounded by [roundForResult]. */
private fun JsonElement.roundedForResult(): JsonElement = when (this) {
    is JsonObject -> JsonObject(mapValues { (_, value) -> value.roundedForResult() })
    is JsonArray -> JsonArray(map { it.roundedForResult() })
    is JsonPrimitive -> when {
        isString -> this
        content.none { it == '.' || it == 'e' || it == 'E' } -> this
        else -> doubleOrNull?.let { JsonPrimitive(roundForResult(it)) } ?: this
    }
    else -> this
}
